/// Peerova — direct peer-to-peer networking with end-to-end encryption.
///
/// `dart:ffi` binding over the Peerova C ABI (`include/peerova.h`). Works in the
/// Dart VM and in Flutter (Android, iOS, macOS, Windows, Linux).
///
/// ```dart
/// import 'package:peerova/peerova.dart';
///
/// final srv = PeerovaServer.start({'port': 0, 'upnp': false});
/// final cli = PeerovaClient.connect(srv.ticket);           // or a 6-char connect code
/// final ev = cli.pollWait(const Duration(seconds: 10));   // ConnectedEvent
/// cli.sendText(ev!.peer, 'hello');
/// cli.close(); srv.close();
/// ```
///
/// Threading: every native node has its own internal mutex, so [PeerovaNode.send]
/// and [PeerovaNode.poll] may be called from any isolate that knows the node's
/// [PeerovaNode.address]. Never call [PeerovaNode.close] while another isolate
/// is inside [PeerovaNode.pollWait] on the same node.
///
/// Errors: native failures throw [PeerovaException] whose message is the text
/// reported by the native library (`peerova_last_error`).
library peerova;

import 'dart:convert';
import 'dart:ffi';
import 'dart:io';
import 'dart:typed_data';

import 'package:ffi/ffi.dart';

/// Maximum payload, in bytes, of one unreliable message (`reliable: false`).
/// Larger unreliable sends are rejected with a [PeerovaException].
const int maxUnreliablePayload = 1180;

// ---------------------------------------------------------------- native

final class _PeerovaEvent extends Struct {
  @Int32()
  external int type;
  @Uint32()
  external int peer;
  @Uint8()
  external int channel;
  @Uint8()
  external int reliable;
  external Pointer<Uint8> data;
  @Uint32()
  external int len;
}

typedef _StartC = Pointer<Void> Function(Pointer<Utf8>);
typedef _ConnectC = Pointer<Void> Function(Pointer<Utf8>, Pointer<Utf8>);
typedef _TicketC = Int32 Function(Pointer<Void>, Pointer<Utf8>, Int32);
typedef _TicketD = int Function(Pointer<Void>, Pointer<Utf8>, int);
typedef _SendC = Int32 Function(Pointer<Void>, Uint32, Uint8, Pointer<Uint8>, Uint32, Int32);
typedef _SendD = int Function(Pointer<Void>, int, int, Pointer<Uint8>, int, int);
typedef _PollC = Int32 Function(Pointer<Void>, Pointer<_PeerovaEvent>);
typedef _PollD = int Function(Pointer<Void>, Pointer<_PeerovaEvent>);
typedef _PollWaitC = Int32 Function(Pointer<Void>, Pointer<_PeerovaEvent>, Uint32);
typedef _PollWaitD = int Function(Pointer<Void>, Pointer<_PeerovaEvent>, int);
typedef _NodeVoidC = Void Function(Pointer<Void>);
typedef _NodeVoidD = void Function(Pointer<Void>);
typedef _DisconnectC = Void Function(Pointer<Void>, Uint32);
typedef _DisconnectD = void Function(Pointer<Void>, int);
typedef _CountC = Uint32 Function(Pointer<Void>);
typedef _CountD = int Function(Pointer<Void>);
typedef _StrC = Pointer<Utf8> Function();

/// Explicit path of the native library (`libpeerova.so`, `peerova.dll`,
/// `libpeerova.dylib`). Set it before the first Peerova call to override the
/// default lookup; changing it afterwards has no effect.
///
/// Lookup order when it is null: the `PEEROVA_LIB` environment variable, then
/// (desktop only) the library next to the working directory / script, then on
/// macOS `Contents/Frameworks/` of the app bundle, then the platform loader (`DynamicLibrary.open`), which finds Flutter-bundled
/// libraries and Android `jniLibs`. iOS always uses the statically linked
/// symbols of the app (`DynamicLibrary.process()`).
String? peerovaLibraryPath;

DynamicLibrary _open() {
  final explicit = peerovaLibraryPath ?? Platform.environment['PEEROVA_LIB'];
  if (explicit != null && explicit.isNotEmpty) return DynamicLibrary.open(explicit);
  // iOS: libpeerova.a is statically linked into the app binary.
  if (Platform.isIOS) return DynamicLibrary.process();
  final name = Platform.isWindows
      ? 'peerova.dll'
      : Platform.isMacOS
          ? 'libpeerova.dylib'
          : 'libpeerova.so';
  if (!Platform.isAndroid) {
    // Desktop convenience: a library next to the working directory or the script,
    // or in the SDK development tree.
    final bases = <String>{
      Directory.current.path,
      File.fromUri(Platform.script).parent.path,
      File.fromUri(Platform.script).parent.parent.path,
    };
    for (final b in bases) {
      for (final rel in [name, '../../target/release/$name']) {
        final f = File('$b/$rel');
        if (f.existsSync()) return DynamicLibrary.open(f.absolute.path);
      }
    }
  }
  if (Platform.isMacOS) {
    // App bundle: <App>.app/Contents/MacOS/<exe> -> Contents/Frameworks/libpeerova.dylib
    final f = File('${File(Platform.resolvedExecutable).parent.parent.path}/Frameworks/$name');
    if (f.existsSync()) return DynamicLibrary.open(f.path);
  }
  try {
    return DynamicLibrary.open(name); // Android jniLibs, Flutter bundle, system path
  } on ArgumentError {
    if (Platform.isMacOS) return DynamicLibrary.process(); // statically linked
    rethrow;
  }
}

class _Lib {
  _Lib(DynamicLibrary l)
      : serverStart = l.lookupFunction<_StartC, _StartC>('peerova_server_start'),
        clientConnect = l.lookupFunction<_ConnectC, _ConnectC>('peerova_client_connect'),
        ticket = l.lookupFunction<_TicketC, _TicketD>('peerova_ticket'),
        roomCode = l.lookupFunction<_TicketC, _TicketD>('peerova_room_code'),
        send = l.lookupFunction<_SendC, _SendD>('peerova_send'),
        poll = l.lookupFunction<_PollC, _PollD>('peerova_poll'),
        pollWait = l.lookupFunction<_PollWaitC, _PollWaitD>('peerova_poll_wait'),
        disconnect = l.lookupFunction<_DisconnectC, _DisconnectD>('peerova_disconnect'),
        peerCount = l.lookupFunction<_CountC, _CountD>('peerova_peer_count'),
        free = l.lookupFunction<_NodeVoidC, _NodeVoidD>('peerova_free'),
        lastError = l.lookupFunction<_StrC, _StrC>('peerova_last_error'),
        version = l.lookupFunction<_StrC, _StrC>('peerova_version');

  final _StartC serverStart;
  final _ConnectC clientConnect;
  final _TicketD ticket;
  final _TicketD roomCode;
  final _SendD send;
  final _PollD poll;
  final _PollWaitD pollWait;
  final _DisconnectD disconnect;
  final _CountD peerCount;
  final _NodeVoidD free;
  final _StrC lastError;
  final _StrC version;
}

DynamicLibrary? _dylibInstance;
DynamicLibrary get _dylib => _dylibInstance ??= _open();
_Lib? _libInstance;
_Lib get _lib => _libInstance ??= _Lib(_dylib);

String _lastError() {
  final p = _lib.lastError();
  final s = p == nullptr ? '' : p.toDartString();
  return s.isEmpty ? 'unknown peerova error' : s;
}

/// Version string of the loaded native library, e.g. `0.1.0`.
///
/// Throws [ArgumentError] (from `dart:ffi`) if the library cannot be found.
String peerovaVersion() => _lib.version().toDartString();

// ---------------------------------------------------------------- public API

/// Thrown when a native Peerova call fails.
class PeerovaException implements Exception {
  /// Creates an exception with [message].
  PeerovaException(this.message);

  /// Error text from the native library (`peerova_last_error`).
  final String message;
  @override
  String toString() => 'PeerovaException: $message';
}

/// A network event returned by [PeerovaNode.poll] / [PeerovaNode.pollWait].
///
/// Sealed: switch over [ConnectedEvent], [MessageEvent], [DisconnectedEvent]
/// and [ConnectFailedEvent]. Payloads are Dart copies, safe to keep.
sealed class PeerovaEvent {
  /// Base constructor.
  const PeerovaEvent(this.peer);

  /// Peer (connection) id. 0 for [ConnectFailedEvent].
  final int peer;
}

/// Server: a client connected ([peer] identifies it). Client: the connection
/// to the server is up ([peer] identifies the server).
final class ConnectedEvent extends PeerovaEvent {
  /// Creates the event for [peer].
  const ConnectedEvent(super.peer);
  @override
  String toString() => 'Connected(peer=$peer)';
}

/// The connection to [peer] has closed (remote close, timeout, or
/// [PeerovaNode.disconnect]).
final class DisconnectedEvent extends PeerovaEvent {
  /// Creates the event for [peer].
  const DisconnectedEvent(super.peer, this.reason);

  /// Human-readable reason (may be empty).
  final String reason;
  @override
  String toString() => 'Disconnected(peer=$peer, $reason)';
}

/// A message from [peer].
final class MessageEvent extends PeerovaEvent {
  /// Creates the event.
  const MessageEvent(super.peer, this.channel, this.reliable, this.data);

  /// Channel the message was sent on (0..255).
  final int channel;

  /// True if it was sent reliably (ordered, retransmitted).
  final bool reliable;

  /// Payload bytes (a copy owned by Dart).
  final Uint8List data;

  /// [data] decoded as UTF-8 (malformed sequences are replaced).
  String get text => utf8.decode(data, allowMalformed: true);
  @override
  String toString() => 'Message(peer=$peer, ch=$channel, reliable=$reliable, ${data.length} B)';
}

/// Client only: the connection attempt failed (timeout, wrong token, server
/// full, ...). [peer] is 0. The client node stays valid
/// but will not connect; close it.
final class ConnectFailedEvent extends PeerovaEvent {
  /// Creates the event.
  const ConnectFailedEvent(this.reason) : super(0);

  /// Human-readable reason.
  final String reason;
  @override
  String toString() => 'ConnectFailed($reason)';
}

/// Common base of [PeerovaServer] and [PeerovaClient]: one native node.
///
/// Call [close] when done; a [NativeFinalizer] frees forgotten nodes, but only
/// at garbage-collection time.
abstract class PeerovaNode implements Finalizable {
  PeerovaNode._(Pointer<Void> h) : _h = h {
    if (h == nullptr) throw PeerovaException(_lastError());
    _finalizer.attach(this, h.cast(), detach: this);
  }

  static final _finalizer = NativeFinalizer(
      _dylib.lookup<NativeFunction<_NodeVoidC>>('peerova_free').cast());

  Pointer<Void> _h;
  final Pointer<_PeerovaEvent> _ev = calloc<_PeerovaEvent>();

  Pointer<Void> get _handle {
    if (_h == nullptr) throw StateError('peerova node is closed');
    return _h;
  }

  /// Raw native `peerova_node*` address (e.g. to call the C ABI from another
  /// isolate). Throws [StateError] if the node is closed.
  int get address => _handle.address;

  /// True after [close].
  bool get isClosed => _h == nullptr;

  /// Returns the next pending event, or null if none. Never blocks — safe on
  /// the Flutter UI isolate (call it from `Timer.periodic` or a frame callback,
  /// draining until it returns null).
  ///
  /// Throws [StateError] if the node is closed.
  PeerovaEvent? poll() => _lib.poll(_handle, _ev) == 1 ? _convert() : null;

  /// Waits up to [timeout] (default: zero = like [poll]) for the next event;
  /// returns null on timeout. Blocks the whole isolate — use it in a CLI tool,
  /// a dedicated server or a background isolate, not on the UI isolate.
  ///
  /// Throws [StateError] if the node is closed.
  PeerovaEvent? pollWait([Duration timeout = Duration.zero]) =>
      _lib.pollWait(_handle, _ev, timeout.inMilliseconds) == 1 ? _convert() : null;

  /// Yields events (via [pollWait]) until [idle] passes with no event.
  /// Blocking, like [pollWait].
  Iterable<PeerovaEvent> events({Duration idle = const Duration(seconds: 1)}) sync* {
    PeerovaEvent? e;
    while ((e = pollWait(idle)) != null) {
      yield e!;
    }
  }

  // event.data is valid only until the next poll → copy right away.
  PeerovaEvent _convert() {
    final e = _ev.ref;
    final bytes = e.len == 0 ? Uint8List(0) : Uint8List.fromList(e.data.asTypedList(e.len));
    switch (e.type) {
      case 1:
        return ConnectedEvent(e.peer);
      case 2:
        return DisconnectedEvent(e.peer, utf8.decode(bytes, allowMalformed: true));
      case 3:
        return MessageEvent(e.peer, e.channel, e.reliable != 0, bytes);
      case 4:
        return ConnectFailedEvent(utf8.decode(bytes, allowMalformed: true));
      default:
        throw PeerovaException('unknown event type ${e.type}');
    }
  }

  /// Sends [data] to [peer] on [channel] (0..255).
  ///
  /// * `reliable: true` (default): ordered and retransmitted per channel; any
  ///   size. Use for chat, inventory, RPCs, file chunks.
  /// * `reliable: false`: datagram, lowest latency, may be lost or reordered,
  ///   at most [maxUnreliablePayload] bytes. Use for positions/inputs that are
  ///   superseded by the next update.
  ///
  /// Different channels never block each other. Thread-safe.
  /// Throws [RangeError] for a bad channel, [PeerovaException] if the native
  /// send fails (unknown peer, payload too large, ...), [StateError] if closed.
  void send(int peer, List<int> data, {int channel = 0, bool reliable = true}) {
    if (channel < 0 || channel > 255) throw RangeError.range(channel, 0, 255, 'channel');
    final n = data.length;
    final buf = calloc<Uint8>(n == 0 ? 1 : n);
    try {
      buf.asTypedList(n == 0 ? 1 : n).setRange(0, n, data);
      if (_lib.send(_handle, peer, channel, buf, n, reliable ? 1 : 0) != 0) {
        throw PeerovaException(_lastError());
      }
    } finally {
      calloc.free(buf);
    }
  }

  /// Sends [text] encoded as UTF-8; see [send].
  void sendText(int peer, String text, {int channel = 0, bool reliable = true}) =>
      send(peer, utf8.encode(text), channel: channel, reliable: reliable);

  /// Closes the connection to [peer]; a [DisconnectedEvent] follows.
  /// Unknown peers are ignored.
  void disconnect(int peer) => _lib.disconnect(_handle, peer);

  /// Number of currently connected peers (server: clients; client: 0 or 1).
  int get peerCount => _lib.peerCount(_handle);

  /// Disconnects all peers and frees the native node. Idempotent. Must not be
  /// called while another isolate is polling or sending on this node.
  void close() {
    if (_h == nullptr) return;
    _finalizer.detach(this);
    _lib.free(_h);
    _h = nullptr;
    calloc.free(_ev);
  }
}

String? _json(Map<String, Object?>? config) => config == null ? null : jsonEncode(config);

Pointer<Utf8> _cstr(String? s) => s == null ? nullptr : s.toNativeUtf8(allocator: calloc);

/// A Peerova server: accepts clients that know its [ticket] (or connect code).
class PeerovaServer extends PeerovaNode {
  PeerovaServer._(super.h) : super._() {
    final n = _lib.ticket(_h, nullptr, 0);
    if (n <= 0) {
      ticket = '';
      return;
    }
    final buf = calloc<Uint8>(n).cast<Utf8>();
    try {
      _lib.ticket(_h, buf, n);
      ticket = buf.toDartString();
    } finally {
      calloc.free(buf);
    }
  }

  /// Starts a server. Blocks up to ~3 s until the ticket is ready (on Flutter,
  /// show a spinner first or start it from a background isolate).
  ///
  /// [config] keys (all optional): `port` (default 47000, 0 = random),
  /// `key_file` (default `server.key`; same key ⇒ same ticket), `license`
  /// (`PVA-...`; without it: dev mode, max 3 clients), `max_ccu`, `token`
  /// (shared secret clients must present), `upnp` (default true), `private`,
  /// and for a connect code `app` (project id) + `share`
  /// (`{'name', 'public', 'max', 'mode', 'region'}`).
  ///
  /// Throws [PeerovaException] on failure (bad config, port in use, ...).
  factory PeerovaServer.start([Map<String, Object?>? config]) {
    final c = _cstr(_json(config));
    try {
      return PeerovaServer._(_lib.serverStart(c));
    } finally {
      if (c != nullptr) calloc.free(c);
    }
  }

  /// The server's ticket (`pvt_...`). Share it with clients; it is derived
  /// from the key file, so it stays the same across restarts.
  late final String ticket;

  /// The 6-character connect code (e.g. `K7Q2XD`) assigned by the Peerova
  /// website, or null while not (yet) available. Only servers started with
  /// `app` + `share` get one; it usually appears a few seconds after start,
  /// so read it again later if it is null. Clients may pass it to
  /// [PeerovaClient.connect] instead of the ticket.
  String? get roomCode {
    final h = _handle;
    final n = _lib.roomCode(h, nullptr, 0);
    if (n <= 0) return null;
    final buf = calloc<Uint8>(n).cast<Utf8>();
    try {
      final m = _lib.roomCode(h, buf, n);
      if (m <= 0 || m > n) return null;
      final s = buf.toDartString();
      return s.isEmpty ? null : s;
    } finally {
      calloc.free(buf);
    }
  }
}

/// A Peerova client: one connection to one server.
class PeerovaClient extends PeerovaNode {
  PeerovaClient._(super.h) : super._();

  /// Starts connecting to a server and returns immediately; poll for a
  /// [ConnectedEvent] or [ConnectFailedEvent].
  ///
  /// [ticket] is the server's `pvt_...` ticket or its 6-character connect
  /// code. [config] keys (optional): `token` (must match the server's),
  /// `timeout_ms` (default 20000).
  ///
  /// With a connect code the call first looks the code up on the Peerova
  /// website, which blocks briefly (network round trip).
  ///
  /// Throws [ArgumentError] for an empty ticket and [PeerovaException] for an
  /// invalid ticket or an unknown / closed connect code.
  factory PeerovaClient.connect(String ticket, [Map<String, Object?>? config]) {
    if (ticket.isEmpty) throw ArgumentError.value(ticket, 'ticket', 'empty');
    final t = _cstr(ticket), c = _cstr(_json(config));
    try {
      return PeerovaClient._(_lib.clientConnect(t, c));
    } finally {
      calloc.free(t);
      if (c != nullptr) calloc.free(c);
    }
  }
}
