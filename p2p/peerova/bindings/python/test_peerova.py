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
        try:
            cli.send(peer, b"x" * 2000, reliable=False)
            raise AssertionError("oversized unreliable send accepted")
        except peerova.PeerovaError as err:
            assert err.code == peerova.E_TOO_LARGE and str(err).startswith("E_TOO_LARGE: "), (err.code, err)
        try:
            srv.send(424242, b"x")
            raise AssertionError("send to unknown peer accepted")
        except peerova.PeerovaError as err:
            assert err.code == peerova.E_UNKNOWN_PEER, (err.code, err)
        print(f"python OK: peerova {peerova.version()}, 200 reliable msgs in order, {1000*(time.perf_counter()-t):.1f} ms, peers={srv.peer_count}")

def test_error_codes():
    try:
        peerova.Client("pvt_@@@")
        raise AssertionError("bad ticket accepted")
    except peerova.PeerovaError as err:
        assert err.code == peerova.E_INVALID_TICKET and str(err).isascii(), (err.code, err)
    assert peerova.error_code_name(peerova.E_CODE_NOT_FOUND) == "E_CODE_NOT_FOUND"
    ev = peerova.Event(peerova.CONNECT_FAILED, 0, 0, False, b"E_TIMEOUT: could not connect")
    assert ev.error_code == peerova.E_TIMEOUT
    print("python error codes OK")

if __name__ == "__main__":
    test_roundtrip()
    test_error_codes()
