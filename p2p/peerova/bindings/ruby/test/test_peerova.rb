# Run: ruby -Ilib test/test_peerova.rb   (needs libpeerova next to lib/peerova.rb, or PEEROVA_LIB)
require "minitest/autorun"
require "tmpdir"
require "peerova"

class TestPeerova < Minitest::Test
  N = 200

  def test_roundtrip
    key = File.join(Dir.mktmpdir, "rb.key")
    Peerova::Server.open(port: 0, upnp: false, key_file: key) do |srv|
      assert_match(/\Apvt_/, srv.ticket)
      assert_nil srv.room_code # no connect code without app/share
      Peerova::Client.open(srv.ticket) do |cli|
        ev = cli.poll(10)
        refute_nil ev
        assert_equal :connected, ev.type
        N.times { |i| cli.send_message(ev.peer, "msg #{i}", channel: 1, reliable: true) }
        cli.send_message(ev.peer, "realtime", channel: 2, reliable: false)
        assert_raises(Peerova::Error) { cli.send_message(ev.peer, "x" * 2000, reliable: false) }
        got = []
        sp = nil
        unreliable = 0
        srv.each_event(timeout: 3) do |e|
          sp = e.peer if e.type == :connected
          if e.message? && e.reliable
            assert_equal 1, e.channel
            got << e.text
            break if got.size == N
          elsif e.message?
            unreliable += 1 if e.channel == 2 && e.text == "realtime"
          end
        end
        assert_equal (0...N).map { |i| "msg #{i}" }, got
        # the datagram may land after the last reliable message (on loopback it is never lost)
        while unreliable.zero? && (e = srv.poll(1))
          unreliable += 1 if e.message? && !e.reliable && e.text == "realtime"
        end
        assert_equal 1, unreliable, "client->server unreliable datagram not received"

        srv.send_message(sp, "pong\x00\xff".b)
        srv.send_message(sp, "srv-realtime", channel: 3, reliable: false)
        m = nil
        back_unreliable = false
        20.times do
          break if m && back_unreliable
          e = cli.poll(1)
          next unless e&.message?
          if e.reliable then m = e
          elsif e.channel == 3 && e.text == "srv-realtime" then back_unreliable = true
          end
        end
        refute_nil m
        assert_equal "pong\x00\xff".b, m.data
        assert back_unreliable, "server->client unreliable datagram not received"
        peers = srv.peer_count

        # client leaves -> server sees :disconnected
        cli.close
        assert cli.closed?
        assert_raises(Peerova::Error) { cli.poll }
        gone = false
        10.times do
          e = srv.poll(1)
          (gone = true; break) if e && e.type == :disconnected && e.peer == sp
        end
        assert gone, "server did not see :disconnected"
        puts "ruby PASS: peerova #{Peerova.version}, #{N} reliable msgs in order, unreliable both ways, " \
             "binary reply ok, peers=#{peers}, disconnect ok"
      end
    end
  end
end
