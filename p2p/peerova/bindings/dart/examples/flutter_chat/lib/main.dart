// How to run: `flutter create my_chat`, copy this file to my_chat/lib/main.dart, add the
// `peerova` dependency from ../pubspec.yaml, bundle libpeerova (README "Flutter"), `flutter run`.
//
// One screen: "Host" starts a server and shows its ticket (and connect code, if
// configured); "Join" connects with a ticket or 6-character code. Events are
// drained on the UI isolate with the non-blocking poll() from Timer.periodic.
import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:peerova/peerova.dart';

void main() => runApp(const MaterialApp(home: ChatPage()));

class ChatPage extends StatefulWidget {
  const ChatPage({super.key});

  @override
  State<ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  final _ticketField = TextEditingController();
  final _chatField = TextEditingController();
  final _lines = <String>[];
  final _peers = <int>{};
  PeerovaNode? _node;
  Timer? _poller;
  String? _ticket;

  bool get _isServer => _node is PeerovaServer;

  void _log(String s) => setState(() => _lines.add(s));

  void _host() {
    // Blocks the UI for up to ~3 s while the ticket is prepared.
    // key_file must be writable: in a real app use the app support directory
    // (package path_provider) so the ticket stays the same across restarts.
    final key = '${Directory.systemTemp.path}/peerova-chat-server.key';
    final srv = PeerovaServer.start({'port': 0, 'key_file': key});
    _start(srv);
    setState(() => _ticket = srv.ticket);
    _log('hosting; share the ticket');
  }

  void _join() {
    final t = _ticketField.text.trim();
    if (t.isEmpty) return;
    try {
      _start(PeerovaClient.connect(t)); // ticket (pvt_...) or 6-char code
      _log('connecting...');
    } on PeerovaException catch (e) {
      _log('error: ${e.message}');
    }
  }

  void _start(PeerovaNode node) {
    _node = node;
    _poller = Timer.periodic(const Duration(milliseconds: 16), (_) => _drain());
  }

  void _drain() {
    final node = _node;
    if (node == null) return;
    PeerovaEvent? ev;
    while ((ev = node.poll()) != null) {
      switch (ev!) {
        case ConnectedEvent(:final peer):
          _peers.add(peer);
          _log('peer $peer connected');
        case DisconnectedEvent(:final peer, :final reason):
          _peers.remove(peer);
          _log('peer $peer left: $reason');
        case ConnectFailedEvent(:final reason):
          _log('connect failed: $reason');
          _stop();
          return;
        case MessageEvent(:final peer, :final text, :final data):
          _log('[$peer] $text');
          if (_isServer) {
            // Forward chat to the other clients.
            for (final p in _peers) {
              if (p != peer) node.send(p, data);
            }
          }
      }
    }
    // A connect code arrives a few seconds after start (servers with `app` + `share`).
    final code = node is PeerovaServer ? node.roomCode : null;
    if (code != null && !(_ticket ?? '').contains(code)) {
      setState(() => _ticket = '${_ticket ?? ''}\ncode: $code');
    }
  }

  void _send() {
    final node = _node, text = _chatField.text;
    if (node == null || text.isEmpty) return;
    for (final p in _peers) {
      node.sendText(p, text); // reliable, ordered (channel 0)
    }
    _chatField.clear();
    _log('me: $text');
  }

  void _stop() {
    _poller?.cancel(); // stop polling before freeing the node
    _poller = null;
    _node?.close();
    _node = null;
    _peers.clear();
    setState(() => _ticket = null);
  }

  @override
  void dispose() {
    _stop();
    _ticketField.dispose();
    _chatField.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final running = _node != null;
    return Scaffold(
      appBar: AppBar(title: const Text('Peerova chat')),
      body: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(children: [
          if (!running)
            Row(children: [
              Expanded(
                child: TextField(
                  controller: _ticketField,
                  decoration: const InputDecoration(labelText: 'Ticket or code'),
                ),
              ),
              TextButton(onPressed: _join, child: const Text('Join')),
              FilledButton(onPressed: _host, child: const Text('Host')),
            ])
          else
            Row(children: [
              Expanded(child: SelectableText(_ticket ?? 'client', maxLines: 3)),
              if (_ticket != null)
                IconButton(
                  icon: const Icon(Icons.copy),
                  onPressed: () => Clipboard.setData(ClipboardData(text: _ticket!.split('\n').first)),
                ),
              TextButton(onPressed: _stop, child: const Text('Leave')),
            ]),
          const Divider(),
          Expanded(
            child: ListView(children: [for (final l in _lines) Text(l)]),
          ),
          Row(children: [
            Expanded(
              child: TextField(controller: _chatField, onSubmitted: (_) => _send()),
            ),
            IconButton(icon: const Icon(Icons.send), onPressed: running ? _send : null),
          ]),
        ]),
      ),
    );
  }
}
