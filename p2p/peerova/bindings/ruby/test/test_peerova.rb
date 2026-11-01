# Run: ruby -Ilib test/test_peerova.rb   (needs target/release/libpeerova.so or PEEROVA_LIB)
require "minitest/autorun"
require "tmpdir"
require "peerova"

class TestPeerova < Minitest::Test
  N = 200

  def test_roundtrip
    key = File.join(Dir.mktmpdir, "rb.key")
    Peerova::Server.open(port: 0, upnp: false, key_file: key) do |srv|
      assert_match(/\Apvt_/, srv.ticket)
      Peerova::Client.open(srv.ticket) do |cli|
        ev = cli.poll(10)
        refute_nil ev
        assert_equal :connected, ev.type
        N.times { |i| cli.send_message(ev.peer, "msg #{i}", channel: 1, reliable: true) }
        cli.send_message(ev.peer, "realtime", channel: 2, reliable: false)
        got = []
        sp = nil
        srv.each_event(timeout: 3) do |e|
          sp = e.peer if e.type == :connected
          if e.message? && e.reliable
            assert_equal 1, e.channel
            got << e.text
            break if got.size == N
          end
        end
        assert_equal (0...N).map { |i| "msg #{i}" }, got
        srv.send_message(sp, "pong")
        m = nil
        while (e = cli.poll(5))
          (m = e; break) if e.message? && e.reliable
        end
        refute_nil m
        assert_equal "pong", m.data
        puts "ruby OK: peerova #{Peerova.version}, #{N} reliable msgs in order, peers=#{srv.peer_count}"
      end
    end
  end
end
