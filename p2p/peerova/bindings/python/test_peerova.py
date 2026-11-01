import os, sys, tempfile, time
sys.path.insert(0, os.path.dirname(__file__))
import peerova

def test_roundtrip():
    key = os.path.join(tempfile.mkdtemp(), "py.key")
    with peerova.Server(port=0, upnp=False, key_file=key) as srv, peerova.Client(srv.ticket) as cli:
        ev = cli.poll(10)
        assert ev and ev.type == peerova.CONNECTED, ev
        peer = ev.peer
        t = time.perf_counter()
        for i in range(200):
            cli.send(peer, f"msg {i}".encode(), channel=1, reliable=True)
        cli.send(peer, b"realtime", channel=2, reliable=False)
        got = []
        sp = None
        for e in srv.events(timeout=3):
            if e.type == peerova.CONNECTED:
                sp = e.peer
            elif e.type == peerova.MESSAGE and e.reliable:
                got.append(e.data.decode())
                if len(got) == 200:
                    break
        assert got == [f"msg {i}" for i in range(200)], got[:5]
        srv.send(sp, b"pong")
        e = cli.poll(5)
        assert e.type == peerova.MESSAGE and e.data == b"pong"
        print(f"python OK: peerova {peerova.version()}, 200 reliable msgs in order, {1000*(time.perf_counter()-t):.1f} ms, peers={srv.peer_count}")

if __name__ == "__main__":
    test_roundtrip()
