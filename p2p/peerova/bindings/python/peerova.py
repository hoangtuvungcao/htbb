"""peerova — direct peer-to-peer networking (end-to-end encrypted) for Python 3.8+.

This module is a thin ``ctypes`` wrapper around the native Peerova library
(``libpeerova.so`` / ``peerova.dll`` / ``libpeerova.dylib``). It has no
dependencies outside the standard library.

Quick start::

    import peerova

    srv = peerova.Server(port=47000)          # blocks until the ticket is ready (<= ~3 s)
    print(srv.ticket)                         # share this with clients
    cli = peerova.Client(srv.ticket)          # or a 6-character connect code
    for ev in cli.events(timeout=10):
        if ev.type == peerova.CONNECTED:
            cli.send(ev.peer, b"hello", channel=0, reliable=True)

Threading: a node (``Server`` / ``Client``) is internally synchronised, so
``send`` / ``disconnect`` / ``peer_count`` may be called from any thread,
including while another thread is blocked in ``poll(timeout)``. The only rule
is: never call ``close()`` while another thread is still inside ``poll`` on
the same node — stop the polling thread first, then close.

Errors: functions that fail raise :class:`PeerovaError`; its message is the
native library's error text.

Library lookup order: ``$PEEROVA_LIB`` (full path to the file) → next to this
file → the SDK's ``lib/<platform>`` folder → the system loader search path.
"""
import ctypes
import ctypes.util
import json
import os
import platform
import sys
from dataclasses import dataclass
from typing import Iterator, Optional, Union

__all__ = [
    "CONNECTED", "DISCONNECTED", "MESSAGE", "CONNECT_FAILED", "MAX_UNRELIABLE_PAYLOAD",
    "Event", "PeerovaError", "Server", "Client", "version",
]

#: Event type: a peer connected (server: a new client; client: the server accepted us).
CONNECTED = 1
#: Event type: a peer went away. ``Event.data`` holds the UTF-8 reason (may be empty).
DISCONNECTED = 2
#: Event type: a message arrived. ``Event.data`` is the payload, ``channel``/``reliable`` are set.
MESSAGE = 3
#: Event type (client only): the connection attempt failed. ``Event.data`` is the UTF-8 reason.
CONNECT_FAILED = 4

#: Largest payload accepted by ``send(..., reliable=False)``, in bytes.
MAX_UNRELIABLE_PAYLOAD = 1180

Bytes = Union[bytes, bytearray, memoryview, str]


class _Event(ctypes.Structure):
    # Mirrors ``peerova_event`` in peerova.h.
    _fields_ = [("type", ctypes.c_int32), ("peer", ctypes.c_uint32), ("channel", ctypes.c_uint8),
                ("reliable", ctypes.c_uint8), ("data", ctypes.POINTER(ctypes.c_uint8)), ("len", ctypes.c_uint32)]


@dataclass
class Event:
    """One event returned by :meth:`Server.poll` / :meth:`Client.poll`.

    Attributes:
        type: ``CONNECTED``, ``DISCONNECTED``, ``MESSAGE`` or ``CONNECT_FAILED``.
        peer: Peer id (connection id). On a client this is the server's id.
        channel: Channel 0..255 the message arrived on (``MESSAGE`` only).
        reliable: True if the message was sent reliably (``MESSAGE`` only).
        data: Payload for ``MESSAGE``; UTF-8 reason for ``DISCONNECTED`` /
            ``CONNECT_FAILED``. Already copied out of native memory, safe to keep.
    """
    type: int
    peer: int
    channel: int
    reliable: bool
    data: bytes

    @property
    def text(self) -> str:
        """``data`` decoded as UTF-8 (invalid bytes are replaced)."""
        return self.data.decode("utf-8", "replace")


class PeerovaError(RuntimeError):
    """Raised when a native call fails. The message is the library's error text."""


def _platform_dir() -> str:
    os_name = {"win32": "windows", "darwin": "macos"}.get(sys.platform, "linux")
    arch = {"amd64": "x86_64", "x86_64": "x86_64", "arm64": "aarch64", "aarch64": "aarch64"}.get(
        platform.machine().lower(), platform.machine().lower())
    return f"{os_name}-{arch}"


def _load() -> ctypes.CDLL:
    name = {"win32": "peerova.dll", "darwin": "libpeerova.dylib"}.get(sys.platform, "libpeerova.so")
    here = os.path.dirname(os.path.abspath(__file__))
    candidates = [
        os.environ.get("PEEROVA_LIB"),
        os.path.join(here, name),
        os.path.join(here, "..", "..", "lib", _platform_dir(), name),
        os.path.join(here, "..", "..", "target", "release", name),
    ]
    for p in candidates:
        if p and os.path.exists(p):
            return ctypes.CDLL(p)
    found = ctypes.util.find_library("peerova")
    if found:
        return ctypes.CDLL(found)
    raise OSError(f"cannot find {name}: put it next to peerova.py or set PEEROVA_LIB to its full path")


_lib = _load()
_lib.peerova_server_start.restype = ctypes.c_void_p
_lib.peerova_server_start.argtypes = [ctypes.c_char_p]
_lib.peerova_client_connect.restype = ctypes.c_void_p
_lib.peerova_client_connect.argtypes = [ctypes.c_char_p, ctypes.c_char_p]
_lib.peerova_ticket.restype = ctypes.c_int32
_lib.peerova_ticket.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_int32]
_lib.peerova_room_code.restype = ctypes.c_int32
_lib.peerova_room_code.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_int32]
_lib.peerova_send.restype = ctypes.c_int32
_lib.peerova_send.argtypes = [ctypes.c_void_p, ctypes.c_uint32, ctypes.c_uint8, ctypes.c_char_p, ctypes.c_uint32, ctypes.c_int32]
_lib.peerova_poll.restype = ctypes.c_int32
_lib.peerova_poll.argtypes = [ctypes.c_void_p, ctypes.POINTER(_Event)]
_lib.peerova_poll_wait.restype = ctypes.c_int32
_lib.peerova_poll_wait.argtypes = [ctypes.c_void_p, ctypes.POINTER(_Event), ctypes.c_uint32]
_lib.peerova_disconnect.restype = None
_lib.peerova_disconnect.argtypes = [ctypes.c_void_p, ctypes.c_uint32]
_lib.peerova_peer_count.restype = ctypes.c_uint32
_lib.peerova_peer_count.argtypes = [ctypes.c_void_p]
_lib.peerova_free.restype = None
_lib.peerova_free.argtypes = [ctypes.c_void_p]
_lib.peerova_last_error.restype = ctypes.c_char_p
_lib.peerova_last_error.argtypes = []
_lib.peerova_version.restype = ctypes.c_char_p
_lib.peerova_version.argtypes = []


def _last_error() -> str:
    msg = _lib.peerova_last_error()
    return msg.decode("utf-8", "replace") if msg else "unknown error"


def _read_string(fn, handle) -> Optional[str]:
    """Call a size-then-fill native getter (peerova_ticket / peerova_room_code)."""
    n = fn(handle, None, 0)
    if n <= 0:
        return None
    buf = ctypes.create_string_buffer(n)
    fn(handle, buf, n)
    return buf.value.decode()


def version() -> str:
    """Version string of the loaded native library."""
    return _lib.peerova_version().decode()


class _Node:
    """Shared implementation of :class:`Server` and :class:`Client`."""

    def __init__(self, handle):
        if not handle:
            raise PeerovaError(_last_error())
        self._h = handle

    def _handle(self):
        if not self._h:
            raise PeerovaError("node is closed")
        return self._h

    def send(self, peer: int, data: Bytes, channel: int = 0, reliable: bool = True) -> None:
        """Send a message to a peer.

        Args:
            peer: Peer id from a ``CONNECTED`` / ``MESSAGE`` event.
            data: Payload (``bytes``-like; ``str`` is sent as UTF-8).
            channel: 0..255. Channels are independent: a slow reliable channel
                never delays another channel.
            reliable: True (default) = ordered and retransmitted per channel, any
                size. False = datagram with the lowest latency; may be lost or
                reordered; at most ``MAX_UNRELIABLE_PAYLOAD`` (1180) bytes.

        Raises:
            PeerovaError: the peer is gone, the payload is too large for an
                unreliable send, the channel is out of range, or the node is closed.
        """
        if isinstance(data, str):
            data = data.encode("utf-8")
        elif not isinstance(data, bytes):
            data = bytes(data)
        if not 0 <= channel <= 255:
            raise PeerovaError("channel must be 0..255")
        if _lib.peerova_send(self._handle(), peer, channel, data, len(data), 1 if reliable else 0) != 0:
            raise PeerovaError(_last_error())

    def poll(self, timeout: float = 0) -> Optional[Event]:
        """Return the next event, or ``None`` if there is none.

        Args:
            timeout: Seconds to wait. ``0`` (default) returns immediately; a
                positive value blocks the calling thread up to that long (the GIL
                is released while waiting, so other Python threads keep running).

        Only one thread should poll a given node at a time.
        """
        h = self._handle()
        ev = _Event()
        ms = int(timeout * 1000)
        got = _lib.peerova_poll_wait(h, ctypes.byref(ev), ms) if ms > 0 else _lib.peerova_poll(h, ctypes.byref(ev))
        if not got:
            return None
        return Event(ev.type, ev.peer, ev.channel, bool(ev.reliable), ctypes.string_at(ev.data, ev.len) if ev.len else b"")

    def events(self, timeout: float = 1.0) -> Iterator[Event]:
        """Yield events until ``timeout`` seconds pass with no event."""
        while True:
            ev = self.poll(timeout)
            if ev is None:
                return
            yield ev

    def disconnect(self, peer: int) -> None:
        """Close the connection to ``peer``. Unknown ids are ignored."""
        _lib.peerova_disconnect(self._handle(), peer)

    @property
    def peer_count(self) -> int:
        """Number of currently connected peers."""
        return _lib.peerova_peer_count(self._handle())

    @property
    def closed(self) -> bool:
        """True after :meth:`close`."""
        return not self._h

    def close(self) -> None:
        """Disconnect everyone and free the node. Safe to call twice.

        Never call this while another thread is inside :meth:`poll` on this node.
        """
        if self._h:
            _lib.peerova_free(self._h)
            self._h = None

    def __enter__(self):
        return self

    def __exit__(self, *exc):
        self.close()

    def __del__(self):
        try:
            self.close()
        except Exception:
            pass


class Server(_Node):
    """A server node: accepts client connections.

    Keyword arguments are the server config, passed to the library as JSON:

    * ``port`` (int, default 47000; 0 = random free port)
    * ``key_file`` (str, default ``"server.key"``): server identity. The same
      key always gives the same ticket. Keep it secret; do not commit it.
    * ``license`` (str, ``"PVA-..."``): without it the server runs in dev mode
      (at most 3 clients).
    * ``max_ccu`` (int): maximum concurrent clients.
    * ``token`` (str): shared secret that clients must present.
    * ``upnp`` (bool, default True): try to open the port on the router.
    * ``private`` (bool): the ticket does not contain the server's IP addresses.
    * ``app`` (str, project id from the Peerova website) and ``share``
      (dict: ``name``, ``public``, ``max``, ``mode``, ``region``): publish a
      6-character connect code, see :attr:`room_code`.

    The constructor blocks until the ticket is ready (<= ~3 s).

    Raises:
        PeerovaError: the server could not start (port in use, bad config, ...).
    """

    def __init__(self, **config):
        super().__init__(_lib.peerova_server_start(json.dumps(config).encode()))
        #: The server ticket (``"pvt_..."``). Give it to clients.
        self.ticket: str = _read_string(_lib.peerova_ticket, self._h) or ""

    @property
    def room_code(self) -> Optional[str]:
        """The 6-character connect code (e.g. ``"K7Q2XD"``), or ``None``.

        Only available when the server was started with ``app`` and ``share``,
        and only once the website has assigned the code (shortly after start),
        so poll it again later if it is ``None``. Clients can pass this code to
        :class:`Client` instead of the ticket.
        """
        return _read_string(_lib.peerova_room_code, self._handle())


class Client(_Node):
    """A client node: connects to one server.

    Args:
        ticket: The server's ticket (``"pvt_..."``) or its 6-character connect code.
        **config: ``token`` (str, the server's shared secret) and
            ``timeout_ms`` (int, default 20000).

    Returns immediately; wait for a ``CONNECTED`` or ``CONNECT_FAILED`` event
    with :meth:`poll`. Connections are automatic: direct first, with an
    automatic encrypted fallback route when networks block direct connections.

    Raises:
        PeerovaError: the ticket is malformed or the config is invalid.
    """

    def __init__(self, ticket: str, **config):
        super().__init__(_lib.peerova_client_connect(ticket.encode(), json.dumps(config).encode()))
