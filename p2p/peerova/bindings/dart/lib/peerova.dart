/// peerova — direct P2P networking (no relay, end-to-end encrypted).
///
/// dart:ffi binding over the C ABI in `crates/peerova-ffi/include/peerova.h`.
/// Works in the Dart VM and in Flutter (Android, iOS, macOS, Windows, Linux).
///
/// ```dart
/// import 'package:peerova/peerova.dart';
///
/// final srv = PeerovaServer.start({'port': 0, 'upnp': false});
/// final cli = PeerovaClient.connect(srv.ticket);
/// final ev = cli.pollWait(const Duration(seconds: 10));   // ConnectedEvent
/// cli.sendText(ev!.peer, 'hello');
/// cli.close(); srv.close();
/// ```
///
/// Threading: every native node has its own mutex, so `send`/`poll` may be
/// called from any isolate that holds the handle address. Do not call
/// [PeerovaNode.close] while another isolate is inside `pollWait` on the same node.
library peerova;

import 'dart:convert';
import 'dart:ffi';
import 'dart:io';
import 'dart:typed_data';

import 'package:ffi/ffi.dart';

/// Max payload of an unreliable datagram.
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

/// Explicit library path; set before the first peerova call to override lookup.
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
    // Dev convenience: running from bindings/dart inside the repo.
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

/// Native library version string.
String peerovaVersion() => _lib.version().toDartString();

// ---------------------------------------------------------------- public API

class PeerovaException implements Exception {
  PeerovaException(this.message);
  final String message;
  @override
  String toString() => 'PeerovaException: $message';
}

/// A network event. All payloads are Dart copies, safe to keep.
sealed class PeerovaEvent {
  const PeerovaEvent(this.peer);

  /// Peer (connection) id. 0 for [ConnectFailedEvent].
  final int peer;
}

/// Server: a client connected. Client: connected to the server.
final class ConnectedEvent extends PeerovaEvent {
  const ConnectedEvent(super.peer);
  @override
  String toString() => 'Connected(peer=$peer)';
}

final class DisconnectedEvent extends PeerovaEvent {
  const DisconnectedEvent(super.peer, this.reason);
  final String reason;
  @override
  String toString() => 'Disconnected(peer=$peer, $reason)';
}

final class MessageEvent extends PeerovaEvent {
  const MessageEvent(super.peer, this.channel, this.reliable, this.data);
  final int channel;
  final bool reliable;
  final Uint8List data;
  String get text => utf8.decode(data, allowMalformed: true);
  @override
  String toString() => 'Message(peer=$peer, ch=$channel, reliable=$reliable, ${data.length} B)';
}

final class ConnectFailedEvent extends PeerovaEvent {
  const ConnectFailedEvent(this.reason) : super(0);
  final String reason;
  @override
  String toString() => 'ConnectFailed($reason)';
}

/// Common base of [PeerovaServer] and [PeerovaClient].
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

  /// Raw native `peerova_node*` address (e.g. to call the C ABI from another isolate).
  int get address => _handle.address;

  bool get isClosed => _h == nullptr;

  /// Non-blocking. Returns null when no event is pending. Use from a frame
  /// callback / `Timer.periodic` in Flutter.
  PeerovaEvent? poll() => _lib.poll(_handle, _ev) == 1 ? _convert() : null;

  /// Blocks up to [timeout] (default: zero) waiting for an event. Blocks the
  /// isolate — use it in a background isolate or a dedicated server, not on the UI isolate.
  PeerovaEvent? pollWait([Duration timeout = Duration.zero]) =>
      _lib.pollWait(_handle, _ev, timeout.inMilliseconds) == 1 ? _convert() : null;

  /// Yields events until [idle] passes with no event.
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

  /// reliable=true: ordered + retransmitted per channel (0..255).
  /// reliable=false: datagram, may be lost/reordered, max [maxUnreliablePayload] bytes.
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

  void sendText(int peer, String text, {int channel = 0, bool reliable = true}) =>
      send(peer, utf8.encode(text), channel: channel, reliable: reliable);

  void disconnect(int peer) => _lib.disconnect(_handle, peer);

  int get peerCount => _lib.peerCount(_handle);

  /// Frees the native node. Idempotent.
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

  /// Starts a server. Blocks up to ~3 s until the ticket is ready.
  /// Config keys: see peerova.h (`port`, `key_file`, `license`, `upnp`, `token`, ...).
  factory PeerovaServer.start([Map<String, Object?>? config]) {
    final c = _cstr(_json(config));
    try {
      return PeerovaServer._(_lib.serverStart(c));
    } finally {
      if (c != nullptr) calloc.free(c);
    }
  }

  /// Share this with clients (`pvt_...`).
  late final String ticket;
}

class PeerovaClient extends PeerovaNode {
  PeerovaClient._(super.h) : super._();

  /// Returns immediately; wait for [ConnectedEvent] / [ConnectFailedEvent].
  /// Config keys: `token`, `timeout_ms`, `supernodes`, `force_tcp`.
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
