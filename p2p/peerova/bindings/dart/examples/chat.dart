// How to run (from bindings/dart):
//   dart run examples/chat.dart server                     # prints the ticket
//   dart run examples/chat.dart client <ticket|code>       # type lines to chat, /quit to leave
// Optional: --seconds N (client quits after N s, for scripted runs), --key FILE (server identity).
//
// Tiny chat + position-sync demo. Positions go unreliable on channel 1 at 20 Hz
// (a lost update is replaced by the next one); chat lines go reliable on
// channel 0. Events are drained with the non-blocking poll() from
// Timer.periodic — the same pattern a Flutter app uses on its UI isolate.
import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:math' as math;
import 'dart:typed_data';

import 'package:peerova/peerova.dart';

const chatChannel = 0;
const positionChannel = 1;

void main(List<String> args) {
  if (args.isEmpty || (args[0] == 'client' && args.length < 2)) {
    stderr.writeln('usage: chat.dart server [--key FILE] | chat.dart client <ticket|code> [--seconds N]');
    exit(2);
  }
  String? opt(String name) {
    final i = args.indexOf(name);
    return i >= 0 && i + 1 < args.length ? args[i + 1] : null;
  }

  if (args[0] == 'server') {
    runServer(opt('--key') ?? '${Directory.systemTemp.path}/peerova-chat-example.key');
  } else {
    final secs = opt('--seconds');
    runClient(args[1], secs == null ? null : Duration(seconds: int.parse(secs)));
  }
}

// ------------------------------------------------------------------ server

void runServer(String keyFile) {
  // Blocks up to ~3 s until the ticket is ready.
  final srv = PeerovaServer.start({'port': 0, 'key_file': keyFile});
  print('ticket: ${srv.ticket}');
  print('run:    dart run examples/chat.dart client ${srv.ticket}');
  final peers = <int>{};

  void broadcast(int from, Uint8List data, {required int channel, required bool reliable}) {
    for (final p in peers) {
      if (p == from) continue;
      try {
        srv.send(p, data, channel: channel, reliable: reliable);
      } on PeerovaException catch (e) {
        stderr.writeln('send to $p failed: ${e.message}');
      }
    }
  }

  final timer = Timer.periodic(const Duration(milliseconds: 10), (_) {
    PeerovaEvent? ev;
    while ((ev = srv.poll()) != null) {
      switch (ev!) {
        case ConnectedEvent(:final peer):
          peers.add(peer);
          print('peer $peer joined (${srv.peerCount} online)');
        case DisconnectedEvent(:final peer, :final reason):
          peers.remove(peer);
          print('peer $peer left: $reason');
        case MessageEvent(:final peer, channel: chatChannel, :final data, :final text):
          print('[$peer] $text');
          broadcast(peer, data, channel: chatChannel, reliable: true);
        case MessageEvent(:final peer, channel: positionChannel, :final data):
          broadcast(peer, data, channel: positionChannel, reliable: false);
        case MessageEvent() || ConnectFailedEvent():
          break;
      }
    }
  });

  // Ctrl+C: stop polling first, then free the node.
  late final StreamSubscription<ProcessSignal> sig;
  sig = ProcessSignal.sigint.watch().listen((_) {
    timer.cancel();
    srv.close();
    sig.cancel();
    print('server stopped');
  });
}

// ------------------------------------------------------------------ client

void runClient(String ticketOrCode, Duration? runFor) {
  final cli = PeerovaClient.connect(ticketOrCode); // returns immediately
  int? server;
  var sent = 0, received = 0;
  final start = DateTime.now();
  Timer? poller, mover;
  StreamSubscription<String>? input;

  void shutdown(String why) {
    if (cli.isClosed) return;
    poller?.cancel();
    mover?.cancel();
    input?.cancel();
    cli.close(); // nothing polls any more, so closing is safe
    print('client stopped ($why): sent $sent positions, received $received');
  }

  poller = Timer.periodic(const Duration(milliseconds: 10), (_) {
    PeerovaEvent? ev;
    while ((ev = cli.poll()) != null) {
      switch (ev!) {
        case ConnectedEvent(:final peer):
          server = peer;
          print('connected; type a line to chat, /quit to leave');
          cli.sendText(peer, 'hello from ${Platform.localHostname}', channel: chatChannel);
        case ConnectFailedEvent(:final reason):
          print('connect failed: $reason');
          return shutdown('connect failed');
        case DisconnectedEvent(:final reason):
          print('disconnected: $reason');
          return shutdown('server gone');
        case MessageEvent(channel: chatChannel, :final text):
          print('chat: $text');
        case MessageEvent(channel: positionChannel, :final data) when data.length == 8:
          received++;
          if (received % 20 == 1) {
            final v = ByteData.sublistView(data);
            print('other player at (${v.getFloat32(0).toStringAsFixed(2)}, ${v.getFloat32(4).toStringAsFixed(2)})');
          }
        case MessageEvent():
          break;
      }
    }
    if (runFor != null && DateTime.now().difference(start) > runFor) shutdown('time is up');
  });

  // 20 Hz position updates: small, frequent, superseded by the next one → unreliable.
  mover = Timer.periodic(const Duration(milliseconds: 50), (_) {
    final s = server;
    if (s == null) return;
    final t = DateTime.now().difference(start).inMilliseconds / 1000;
    final pos = ByteData(8)
      ..setFloat32(0, math.cos(t))
      ..setFloat32(4, math.sin(t));
    cli.send(s, pos.buffer.asUint8List(), channel: positionChannel, reliable: false);
    sent++;
  });

  input = stdin.transform(utf8.decoder).transform(const LineSplitter()).listen((line) {
    if (line == '/quit') return shutdown('quit');
    final s = server;
    if (s != null && line.isNotEmpty) cli.sendText(s, line, channel: chatChannel); // reliable, ordered
  });
}
