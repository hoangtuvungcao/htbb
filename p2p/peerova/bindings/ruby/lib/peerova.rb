# frozen_string_literal: true
#
# Peerova — direct peer-to-peer networking with end-to-end encryption. Ruby binding via Fiddle
# (stdlib, no compiler needed).
#
#   require "peerova"
#   srv = Peerova::Server.new(port: 0, upnp: false)
#   puts srv.ticket
#   cli = Peerova::Client.new(srv.ticket)   # or a 6-character connect code
#   ev = cli.poll(10)                       # => #<Peerova::Event type=:connected ...>
#   cli.send_message(ev.peer, "hello", channel: 0, reliable: true)
#   srv.each_event(timeout: 2) { |e| p e }
#   cli.close; srv.close
#
# Library lookup: ENV["PEEROVA_LIB"], next to this file, the SDK development tree, then the system
# loader (libpeerova.so / peerova.dll / libpeerova.dylib).
# Event data is copied into a Ruby String (binary) immediately, so events can be kept freely.
# Errors: native failures raise Peerova::Error with the native error text (peerova_last_error).
require "fiddle"
require "json"

# Peerova SDK namespace.
module Peerova
  # Event type numbers (C ABI values); Event#type uses the symbols in TYPE_NAMES.
  CONNECTED      = 1
  DISCONNECTED   = 2
  MESSAGE        = 3
  CONNECT_FAILED = 4
  # Maximum bytes of one unreliable message.
  MAX_UNRELIABLE_PAYLOAD = 1180

  # Event type number => symbol (:connected, :disconnected, :message, :connect_failed).

  TYPE_NAMES = { CONNECTED => :connected, DISCONNECTED => :disconnected,
                 MESSAGE => :message, CONNECT_FAILED => :connect_failed }.freeze

  # Raised when a Peerova call fails; the message is the native error text.
  class Error < StandardError; end

  # One network event.
  # - +type+: :connected, :message, :disconnected or :connect_failed
  # - +peer+: peer (connection) id; 0 for :connect_failed
  # - +channel+: 0..255 (:message)
  # - +reliable+: true if sent reliably (:message)
  # - +data+: binary String — payload for :message, UTF-8 reason for :disconnected / :connect_failed
  Event = Struct.new(:type, :peer, :channel, :reliable, :data) do
    # +data+ as a UTF-8 String.
    def text = data.dup.force_encoding(Encoding::UTF_8)
    # True for a :message event.
    def message? = type == :message
  end

  # Native layer (internal). peerova_event (x86_64/arm64): int32 type @0, uint32 peer @4, uint8 channel @8,
  # uint8 reliable @9, pad, const uint8_t* data @16, uint32 len @24; sizeof = 32
  # (on 32-bit targets: data @12, len @16, sizeof 20 — computed from Fiddle::SIZEOF_VOIDP below).
  module Native
    PTR = Fiddle::SIZEOF_VOIDP
    DATA_OFF = PTR == 8 ? 16 : 12
    LEN_OFF  = DATA_OFF + PTR
    EVENT_SIZE = (LEN_OFF + 4 + PTR - 1) / PTR * PTR

    def self.lib_name
      case RbConfig::CONFIG["host_os"]
      when /mswin|mingw|cygwin/ then "peerova.dll"
      when /darwin/ then "libpeerova.dylib"
      else "libpeerova.so"
      end
    end

    def self.open_lib
      here = __dir__
      cands = [ENV["PEEROVA_LIB"], File.join(here, lib_name),
               File.expand_path("../../../target/release/#{lib_name}", here)]
      path = cands.compact.find { |p| File.exist?(p) }
      Fiddle.dlopen(path || lib_name)
    rescue Fiddle::DLError => e
      raise Error, "cannot load #{lib_name} (set PEEROVA_LIB): #{e.message}"
    end

    LIB = open_lib
    T = Fiddle
    def self.fn(name, args, ret) = Fiddle::Function.new(LIB[name], args, ret)

    U8  = -T::TYPE_CHAR # unsigned char
    U32 = -T::TYPE_INT
    SERVER_START   = fn("peerova_server_start",   [T::TYPE_VOIDP], T::TYPE_VOIDP)
    CLIENT_CONNECT = fn("peerova_client_connect", [T::TYPE_VOIDP, T::TYPE_VOIDP], T::TYPE_VOIDP)
    TICKET     = fn("peerova_ticket",     [T::TYPE_VOIDP, T::TYPE_VOIDP, T::TYPE_INT], T::TYPE_INT)
    ROOM_CODE  = fn("peerova_room_code",  [T::TYPE_VOIDP, T::TYPE_VOIDP, T::TYPE_INT], T::TYPE_INT)
    SEND       = fn("peerova_send",       [T::TYPE_VOIDP, U32, U8, T::TYPE_VOIDP, U32, T::TYPE_INT], T::TYPE_INT)
    POLL       = fn("peerova_poll",       [T::TYPE_VOIDP, T::TYPE_VOIDP], T::TYPE_INT)
    POLL_WAIT  = fn("peerova_poll_wait",  [T::TYPE_VOIDP, T::TYPE_VOIDP, U32], T::TYPE_INT)
    DISCONNECT = fn("peerova_disconnect", [T::TYPE_VOIDP, U32], T::TYPE_VOID)
    PEER_COUNT = fn("peerova_peer_count", [T::TYPE_VOIDP], U32)
    FREE       = fn("peerova_free",       [T::TYPE_VOIDP], T::TYPE_VOID)
    LAST_ERROR = fn("peerova_last_error", [], T::TYPE_VOIDP)
    VERSION    = fn("peerova_version",    [], T::TYPE_VOIDP)

    def self.cstr(ptr) = ptr.null? ? "" : ptr.to_s.force_encoding(Encoding::UTF_8)
    def self.last_error = cstr(LAST_ERROR.call)
  end

  # Version string of the native library, e.g. "0.1.0".
  def self.version = Native.cstr(Native::VERSION.call)

  # Base class for Server and Client: one native node. Methods may be called from several threads,
  # but never call #close while another thread is inside #poll / #send_message on the same node.
  class Node
    # @api private
    # @param handle [Fiddle::Pointer]
    def initialize(handle)
      raise Error, Native.last_error if handle.null?
      @h = handle
      @ev = Fiddle::Pointer.malloc(Native::EVENT_SIZE, Fiddle::RUBY_FREE)
      ObjectSpace.define_finalizer(self, self.class.finalizer(handle.to_i))
    end

    # Frees the native node if the Ruby object is GC'd without #close (close undefines it).
    def self.finalizer(addr) = proc { Native::FREE.call(addr) }

    # Sends +data+ (any String, sent as raw bytes) to +peer+ on +channel+ (0..255).
    # reliable: true = ordered + retransmitted per channel, any size (chat, RPC, file chunks);
    # false = datagram, may be lost/reordered, max 1180 bytes (positions, inputs).
    # @return [self]
    # @raise [Error] if rejected (unknown peer, payload too large, closed node)
    def send_message(peer, data, channel: 0, reliable: true)
      data = data.to_s.b
      rc = Native::SEND.call(handle, peer, channel, data, data.bytesize, reliable ? 1 : 0)
      raise Error, Native.last_error unless rc.zero?
      self
    end

    # Returns the next Event, or nil if none arrives within +timeout+ seconds (0 = non-blocking).
    # Waiting does not hold the GVL, so other Ruby threads keep running.
    # @raise [Error] if the node is closed
    def poll(timeout = 0)
      got = if timeout.to_f > 0
              Native::POLL_WAIT.call(handle, @ev, (timeout * 1000).to_i)
            else
              Native::POLL.call(handle, @ev)
            end
      return nil if got.zero?
      raw = @ev[0, Native::EVENT_SIZE]
      type, peer, channel, reliable = raw.unpack("l<L<CC")
      len = raw.byteslice(Native::LEN_OFF, 4).unpack1("L<")
      dptr = Fiddle::Pointer.new(raw.byteslice(Native::DATA_OFF, Native::PTR).unpack1(Native::PTR == 8 ? "Q<" : "L<"))
      data = len.zero? ? "".b : dptr[0, len] # copy now: valid only until next poll
      Event.new(TYPE_NAMES.fetch(type, type), peer, channel, reliable == 1, data)
    end

    # Yields events until +timeout+ seconds pass with no event. Returns an Enumerator without a block.
    def each_event(timeout: 1.0)
      return enum_for(:each_event, timeout: timeout) unless block_given?
      while (ev = poll(timeout))
        yield ev
      end
    end

    # Closes the connection to +peer+; a :disconnected event follows. Unknown peers are ignored.
    def disconnect(peer) = Native::DISCONNECT.call(handle, peer)
    # Number of connected peers (server: clients; client: 0 or 1).
    def peer_count = Native::PEER_COUNT.call(handle)
    # True after #close.
    def closed? = @h.nil?

    # Disconnects all peers and frees the native node. Idempotent (also done by a finalizer).
    def close
      return unless @h
      ObjectSpace.undefine_finalizer(self)
      Native::FREE.call(@h)
      @h = nil
    end

    private

    def handle = @h || raise(Error, "node is closed")
  end

  # A Peerova server. ::new blocks up to ~3 s until the ticket is ready.
  #   Peerova::Server.new(port: 0, upnp: false, key_file: "server.key", license: "PVA-...")
  #
  # Config keys (all optional): port (default 47000, 0 = random), key_file (default "server.key";
  # same key => same ticket), license ("PVA-..."; without it dev mode, max 3 clients), max_ccu,
  # token (shared secret clients must present), upnp (default true), private, and for a connect
  # code app (project id) + share ({name:, public:, max:, mode:, region:}).
  # Raises Error on failure (bad config, port in use, ...).
  class Server < Node
    # The server ticket ("pvt_..."); share it with clients.
    attr_reader :ticket

    def initialize(**config)
      super(Native::SERVER_START.call(JSON.generate(config)))
      n = Native::TICKET.call(@h, nil, 0)
      buf = Fiddle::Pointer.malloc(n, Fiddle::RUBY_FREE)
      Native::TICKET.call(@h, buf, n)
      @ticket = buf.to_s
    end

    # The 6-character connect code (e.g. "K7Q2XD") assigned by the Peerova website, or nil while not
    # (yet) available. Only servers started with +app+ + +share+ get one; it usually appears a few
    # seconds after start, so call again later. Clients may pass it to Client.new instead of the ticket.
    def room_code
      n = Native::ROOM_CODE.call(handle, nil, 0)
      return nil if n <= 0
      buf = Fiddle::Pointer.malloc(n, Fiddle::RUBY_FREE)
      m = Native::ROOM_CODE.call(handle, buf, n)
      return nil if m <= 0 || m > n
      code = buf.to_s
      code.empty? ? nil : code
    end

    # Like ::new; with a block, yields the server and closes it afterwards.
    def self.open(**config)
      s = new(**config)
      return s unless block_given?
      begin; yield s; ensure; s.close; end
    end
  end

  # A Peerova client: one connection to one server. ::new returns immediately (with a connect code
  # it first looks the code up on the Peerova website, which blocks briefly); poll for a :connected
  # or :connect_failed event.
  #   Peerova::Client.new(ticket_or_code, token: "secret", timeout_ms: 20000)
  # Raises Error for a malformed ticket or an unknown connect code.
  class Client < Node
    # @param ticket [String] server ticket ("pvt_...") or 6-character connect code
    def initialize(ticket, **config)
      super(Native::CLIENT_CONNECT.call(ticket.to_s, JSON.generate(config)))
    end

    # Like ::new; with a block, yields the client and closes it afterwards.
    def self.open(ticket, **config)
      c = new(ticket, **config)
      return c unless block_given?
      begin; yield c; ensure; c.close; end
    end
  end
end
