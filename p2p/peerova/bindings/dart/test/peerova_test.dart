// Run: dart run test/peerova_test.dart   (from bindings/dart; PEEROVA_LIB optional)
import 'dart:io';

import 'package:peerova/peerova.dart';

void check(bool ok, String what) {
  if (!ok) {
    print('FAIL: $what');
    exit(1);
  }
}

void main() {
  print('peerova native ${peerovaVersion()}');
  final key = '${Directory.systemTemp.createTempSync('peerova-dart').path}/dart.key';
  final srv = PeerovaServer.start({'port': 0, 'upnp': false, 'key_file': key});
  final cli = PeerovaClient.connect(srv.ticket);
  try {
    check(srv.ticket.startsWith('pvt_'), 'ticket ${srv.ticket}');
    check(srv.roomCode == null, 'no connect code without app/share');
    int? peer;
    final end = DateTime.now().add(const Duration(seconds: 10));
    while (peer == null && DateTime.now().isBefore(end)) {
      final ev = cli.pollWait(const Duration(milliseconds: 500));
      check(ev is! ConnectFailedEvent, 'connect failed: $ev');
      if (ev is ConnectedEvent) peer = ev.peer;
    }
    check(peer != null, 'client did not connect');

    const n = 100;
    final sw = Stopwatch()..start();
    for (var i = 0; i < n; i++) {
      cli.sendText(peer!, 'msg $i', channel: 1);
    }
    cli.sendText(peer!, 'realtime', channel: 2, reliable: false);

    final got = <String>[];
    var unreliable = false;
    int? sp;
    final end2 = DateTime.now().add(const Duration(seconds: 5));
    while ((got.length < n || !unreliable) && DateTime.now().isBefore(end2)) {
      switch (srv.pollWait(const Duration(milliseconds: 200))) {
        case ConnectedEvent(:final peer):
          sp = peer;
        case MessageEvent(reliable: true, channel: 1, :final text):
          got.add(text);
        case MessageEvent(reliable: false, channel: 2, :final text):
          unreliable = text == 'realtime';
        default:
      }
    }
    check(got.length == n, 'got ${got.length}/$n reliable messages');
    for (var i = 0; i < n; i++) {
      check(got[i] == 'msg $i', 'order at $i: ${got[i]}');
    }
    // Unreliable may legitimately be lost; on loopback it should arrive.
    check(unreliable, 'unreliable datagram not received');
    check(sp != null && srv.peerCount == 1, 'server peer count ${srv.peerCount}');

    srv.sendText(sp!, 'pong', channel: 0);
    PeerovaEvent? r;
    final end3 = DateTime.now().add(const Duration(seconds: 5));
    while (r is! MessageEvent && DateTime.now().isBefore(end3)) {
      r = cli.pollWait(const Duration(milliseconds: 200));
    }
    check(r is MessageEvent && r.text == 'pong', 'reply: $r');
    print('dart PASS: $n reliable msgs in order + 1 unreliable + reply, ${sw.elapsedMilliseconds} ms');
  } finally {
    cli.close();
    srv.close();
  }
}
