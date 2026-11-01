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
native library's English error text (``"E_NAME: message"``) and ``.code`` is the
stable error code (one of the ``E_*`` constants, same values as ``PEEROVA_E_*``).

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
    "Event", "PeerovaError", "Server", "Client", "version", "error_code_name",
    "E_OK", "E_INVALID_ARG", "E_TOO_LARGE", "E_UNKNOWN_PEER", "E_CLOSED", "E_CODE_NOT_FOUND",
    "E_NETWORK", "E_TIMEOUT", "E_LICENSE", "E_CCU_LIMIT", "E_ACCESS_DENIED", "E_INVALID_TICKET",
    "E_IO", "E_INTERNAL",
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

# Stable error codes (``PEEROVA_E_*`` in peerova.h). ``PeerovaError.code`` and ``Event.error_code``.
E_OK = 0  #: no error
E_INVALID_ARG = 1  #: invalid argument / config JSON / connect code format
E_TOO_LARGE = 2  #: unreliable payload larger than 1180 bytes
E_UNKNOWN_PEER = 3  #: peer id not connected
E_CLOSED = 4  #: connection closed (node closed, disconnect reasons)
E_CODE_NOT_FOUND = 5  #: 6-character connect code unknown or expired
E_NETWORK = 6  #: socket / port in use / website or license server unreachable
E_TIMEOUT = 7  #: connect timed out, server not ready, peer stopped responding
E_LICENSE = 8  #: license / lease invalid, expired or rejected
E_CCU_LIMIT = 9  #: server's concurrent-client limit reached
E_ACCESS_DENIED = 10  #: access token wrong / expired / revoked
E_INVALID_TICKET = 11  #: ticket malformed, corrupted or unsupported
E_IO = 12  #: server key file unreadable / unwritable / corrupted
E_INTERNAL = 99  #: internal or unclassified error

_CODE_NAMES = {v: k for k, v in globals().items() if k.startswith("E_") and isinstance(v, int)}


def error_code_name(code: int) -> str:
    """``"E_TOO_LARGE"`` for ``E_TOO_LARGE`` etc."""
    return _CODE_NAMES.get(code, f"E_{code}")


def _code_from_text(text: str, default: int = E_INTERNAL) -> int:
    name = text.split(":", 1)[0].strip()
    for code, n in _CODE_NAMES.items():
        if n == name:
            return code
    return default


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

    @property
    def error_code(self) -> int:
        """For ``CONNECT_FAILED`` / ``DISCONNECTED``: the ``E_*`` code of the reason (``text`` is
        English, ``"E_TIMEOUT: ..."``). ``E_OK`` for other events."""
        if self.type in (CONNECT_FAILED, DISCONNECTED):
            return _code_from_text(self.text)
        return E_OK


class PeerovaError(RuntimeError):
    """Raised when a call fails. ``str(err)`` is English text ``"E_NAME: message"``;
    ``err.code`` is the stable ``E_*`` code."""

    def __init__(self, message: str, code: Optional[int] = None):
        super().__init__(message)
        self.code = _code_from_text(message) if code is None else code


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
_has_code = hasattr(_lib, "peerova_last_error_code")  # absent in older native libraries
if _has_code:
    _lib.peerova_last_error_code.restype = ctypes.c_int32
    _lib.peerova_last_error_code.argtypes = []
_lib.peerova_version.restype = ctypes.c_char_p
_lib.peerova_version.argtypes = []


def _last_error() -> PeerovaError:
    """PeerovaError for the last failed native call on this thread."""
    msg = _lib.peerova_last_error()
    text = msg.decode("utf-8", "replace") if msg else "E_INTERNAL: unknown error"
    return PeerovaError(text, _lib.peerova_last_error_code() if _has_code else None)


def _err(code: int, text: str) -> PeerovaError:
    return PeerovaError(f"{error_code_name(code)}: {text}", code)


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
            raise _last_error()
        self._h = handle

    def _handle(self):
        if not self._h:
            raise _err(E_CLOSED, "node is closed")
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
            raise _err(E_INVALID_ARG, "channel must be 0..255")
        if _lib.peerova_send(self._handle(), peer, channel, data, len(data), 1 if reliable else 0) != 0:
            raise _last_error()

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
    * ``hide_ip`` (bool): hide the server IP — players never learn its real
      address (DDoS protection); clients connect through the automatic
      encrypted route, with slightly higher latency.
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
        **config: ``token`` (str, the server's shared secret),
            ``timeout_ms`` (int, default 20000) and ``key_file`` (str, optional:
            the client's identity key, created if missing — keep it, e.g. in
            ``<appdata>/<game>/peerova-client.key``, so the player keeps the same
            identity and the same stable address on the server across runs;
            without it every run uses a new random key. Bans by address only hold
            while the player keeps the file; accounts/tokens are the robust way).

    Returns immediately; wait for a ``CONNECTED`` or ``CONNECT_FAILED`` event
    with :meth:`poll`. Connections are automatic: direct first, with an
    automatic encrypted fallback route when networks block direct connections.

    Raises:
        PeerovaError: the ticket is malformed or the config is invalid.
    """

    def __init__(self, ticket: str, **config):
        super().__init__(_lib.peerova_client_connect(ticket.encode(), json.dumps(config).encode()))
