"""peerova — direct P2P networking (no relay, E2E encrypted). Python binding via ctypes.

    import peerova
    srv = peerova.Server(port=47000, license="PVA-...")
    print(srv.ticket)
    cli = peerova.Client(srv.ticket)
    for ev in cli.events(timeout=10):
        if ev.type == peerova.CONNECTED:
            cli.send(ev.peer, b"hello", channel=0, reliable=True)
"""
import ctypes, ctypes.util, json, os, sys
from dataclasses import dataclass

CONNECTED, DISCONNECTED, MESSAGE, CONNECT_FAILED = 1, 2, 3, 4

class _Event(ctypes.Structure):
    _fields_ = [("type", ctypes.c_int32), ("peer", ctypes.c_uint32), ("channel", ctypes.c_uint8),
                ("reliable", ctypes.c_uint8), ("data", ctypes.POINTER(ctypes.c_uint8)), ("len", ctypes.c_uint32)]

@dataclass
class Event:
    type: int
    peer: int
    channel: int
    reliable: bool
    data: bytes

def _load():
    names = {"win32": "peerova.dll", "darwin": "libpeerova.dylib"}.get(sys.platform, "libpeerova.so")
    here = os.path.dirname(os.path.abspath(__file__))
    for p in [os.environ.get("PEEROVA_LIB"), os.path.join(here, names),
              os.path.join(here, "..", "..", "target", "release", names), ctypes.util.find_library("peerova")]:
        if p and os.path.exists(p):
            return ctypes.CDLL(p)
    raise OSError(f"không tìm thấy {names} (đặt biến PEEROVA_LIB)")

_lib = _load()
_lib.peerova_server_start.restype = ctypes.c_void_p
_lib.peerova_server_start.argtypes = [ctypes.c_char_p]
_lib.peerova_client_connect.restype = ctypes.c_void_p
_lib.peerova_client_connect.argtypes = [ctypes.c_char_p, ctypes.c_char_p]
_lib.peerova_ticket.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_int32]
_lib.peerova_room_code.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_int32]
_lib.peerova_send.argtypes = [ctypes.c_void_p, ctypes.c_uint32, ctypes.c_uint8, ctypes.c_char_p, ctypes.c_uint32, ctypes.c_int32]
_lib.peerova_poll.argtypes = [ctypes.c_void_p, ctypes.POINTER(_Event)]
_lib.peerova_poll_wait.argtypes = [ctypes.c_void_p, ctypes.POINTER(_Event), ctypes.c_uint32]
_lib.peerova_disconnect.argtypes = [ctypes.c_void_p, ctypes.c_uint32]
_lib.peerova_peer_count.argtypes = [ctypes.c_void_p]
_lib.peerova_peer_count.restype = ctypes.c_uint32
_lib.peerova_free.argtypes = [ctypes.c_void_p]
_lib.peerova_last_error.restype = ctypes.c_char_p
_lib.peerova_version.restype = ctypes.c_char_p

class PeerovaError(RuntimeError):
    pass

def version() -> str:
    return _lib.peerova_version().decode()

class _Node:
    def __init__(self, handle):
        if not handle:
            raise PeerovaError(_lib.peerova_last_error().decode())
        self._h = handle

    def send(self, peer: int, data: bytes, channel: int = 0, reliable: bool = True):
        if _lib.peerova_send(self._h, peer, channel, data, len(data), 1 if reliable else 0) != 0:
            raise PeerovaError(_lib.peerova_last_error().decode())

    def poll(self, timeout: float = 0):
        ev = _Event()
        got = _lib.peerova_poll_wait(self._h, ctypes.byref(ev), int(timeout * 1000)) if timeout else _lib.peerova_poll(self._h, ctypes.byref(ev))
        if not got:
            return None
        return Event(ev.type, ev.peer, ev.channel, bool(ev.reliable), ctypes.string_at(ev.data, ev.len) if ev.len else b"")

    def events(self, timeout: float = 1.0):
        """Yield events until `timeout` seconds pass with no event."""
        while (ev := self.poll(timeout)) is not None:
            yield ev

    def disconnect(self, peer: int):
        _lib.peerova_disconnect(self._h, peer)

    @property
    def peer_count(self) -> int:
        return _lib.peerova_peer_count(self._h)

    def close(self):
        if self._h:
            _lib.peerova_free(self._h)
            self._h = None

    def __enter__(self):
        return self

    def __exit__(self, *a):
        self.close()

    def __del__(self):
        try:
            self.close()
        except Exception:
            pass

class Server(_Node):
    def __init__(self, **config):
        super().__init__(_lib.peerova_server_start(json.dumps(config).encode()))
        n = _lib.peerova_ticket(self._h, None, 0)
        buf = ctypes.create_string_buffer(n)
        _lib.peerova_ticket(self._h, buf, n)
        self.ticket = buf.value.decode()

    @property
    def room_code(self):
        """6-character connect code (if started with app + lobby config), else None."""
        n = _lib.peerova_room_code(self._h, None, 0)
        if not n:
            return None
        buf = ctypes.create_string_buffer(n)
        _lib.peerova_room_code(self._h, buf, n)
        return buf.value.decode()

class Client(_Node):
    def __init__(self, ticket: str, **config):
        super().__init__(_lib.peerova_client_connect(ticket.encode(), json.dumps(config).encode()))
