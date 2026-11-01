# frozen_string_literal: true
# How to run (from bindings/ruby):
#   ruby -Ilib examples/chat.rb server [keyfile]                # prints the ticket
#   ruby -Ilib examples/chat.rb client <ticket|code> [seconds]  # type lines to chat, /quit to leave
#
# Tiny chat + position-sync demo. Positions go unreliable on channel 1 at 20 Hz (a lost update is
# replaced by the next one); chat lines go reliable on channel 0. The main thread runs the network
# loop; a second thread reads the keyboard into a Queue (poll waits without holding the GVL).
require "peerova"
require "tmpdir"

CHAT = 0
POSITION = 1

def run_server(key_file)
  srv = Peerova::Server.new(port: 0, key_file: key_file) # blocks up to ~3 s
  puts "ticket: #{srv.ticket}"
  puts "run:    ruby -Ilib examples/chat.rb client #{srv.ticket}"
  $stdout.flush
  peers = {}
  stop = false
  trap("INT") { stop = true }
  trap("TERM") { stop = true }
  until stop
    ev = srv.poll(0.05) or next
    case ev.type
    when :connected
      peers[ev.peer] = true
      puts "peer #{ev.peer} joined (#{srv.peer_count} online)"
    when :disconnected
      peers.delete(ev.peer)
      puts "peer #{ev.peer} left: #{ev.text}"
    when :message
      puts "[#{ev.peer}] #{ev.text}" if ev.channel == CHAT
      peers.each_key do |p| # forward to everyone else, same delivery mode
        next if p == ev.peer
        begin
          srv.send_message(p, ev.data, channel: ev.channel, reliable: ev.reliable)
        rescue Peerova::Error => e
          warn "send to #{p} failed: #{e.message}"
        end
      end
    end
    $stdout.flush
  end
  srv.close
  puts "server stopped"
end

def run_client(ticket, seconds)
  cli = Peerova::Client.new(ticket) # returns immediately (a connect code is looked up first)
  lines = Queue.new
  reader = Thread.new { $stdin.each_line { |l| lines << l.chomp }; lines << :eof }
  start = Process.clock_gettime(Process::CLOCK_MONOTONIC)
  next_move = 0.0
  server = nil
  sent = received = 0
  why = "time is up"
  loop do
    now = Process.clock_gettime(Process::CLOCK_MONOTONIC)
    break if seconds && now - start >= seconds

    # 1. network events (waits at most 10 ms)
    while (ev = cli.poll(server ? 0 : 0.01))
      case ev.type
      when :connected
        server = ev.peer
        puts "connected; type a line to chat, /quit to leave"
        cli.send_message(server, "hello from ruby", channel: CHAT)
      when :connect_failed then why = "connect failed: #{ev.text}"; break
      when :disconnected then why = "disconnected: #{ev.text}"; break
      when :message
        if ev.channel == CHAT
          puts "chat: #{ev.text}"
        elsif ev.channel == POSITION && ev.data.bytesize == 8
          received += 1
          puts format("other player at (%.2f, %.2f)", *ev.data.unpack("g2")) if received % 20 == 1
        end
      end
    end
    break if why != "time is up"

    # 2. 20 Hz position: small, frequent, superseded by the next one -> unreliable
    if server && now >= next_move
      t = now - start
      cli.send_message(server, [Math.cos(t), Math.sin(t)].pack("g2"), channel: POSITION, reliable: false)
      sent += 1
      next_move = now + 0.05
    end

    # 3. chat input
    unless lines.empty?
      line = lines.pop
      if line == :eof || line == "/quit"
        why = "quit" if line == "/quit"
        lines = Queue.new if line == :eof # stdin closed: keep running without input
        break if line == "/quit"
      elsif server && !line.empty?
        cli.send_message(server, line, channel: CHAT, reliable: true)
      end
    end
    sleep 0.01 if server
    $stdout.flush
  end
  reader.kill
  cli.close
  puts "client stopped (#{why}): sent #{sent} positions, received #{received}"
end

case ARGV[0]
when "server" then run_server(ARGV[1] || File.join(Dir.tmpdir, "peerova-chat-example.key"))
when "client"
  abort "usage: chat.rb client <ticket|code> [seconds]" unless ARGV[1]
  run_client(ARGV[1], ARGV[2]&.to_f)
else
  abort "usage: chat.rb server [keyfile] | chat.rb client <ticket|code> [seconds]"
end
