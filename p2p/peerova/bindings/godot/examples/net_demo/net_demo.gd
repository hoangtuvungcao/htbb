# How to run: GDScript version of NetDemo.cs. Needs the Godot 4 .NET editor with the peerova addon enabled
# (the game code itself can be pure GDScript). Create a scene with a Node2D root, attach this script, run it twice:
#   godot --path <project> res://net_demo.tscn -- server
#   godot --path <project> res://net_demo.tscn -- client <ticket|code> [--auto]
# Arrow keys move your circle; --auto moves it by itself.
#
#   channel 0, reliable   : chat lines (UTF-8)
#   channel 1, unreliable : positions at 20 Hz, 12 bytes = [u32 player][f32 x][f32 y] little-endian
# The host forwards chat and positions to the other clients (player = sender's peer id; the host is player 0).
extends Node2D

const CHAT_CHANNEL := 0
const POSITION_CHANNEL := 1
const SEND_INTERVAL := 0.05  # 20 Hz

## Ticket or 6-character connect code to join. Empty = host a server.
@export var join_ticket := ""
## Move the local circle automatically instead of with the arrow keys.
@export var auto_move := false

var net  # PeerovaNode (C#), created in _ready
var others := {}  # player id -> Vector2
var _time := 0.0
var _next_send := 0.0

func _ready() -> void:
	var args := OS.get_cmdline_user_args()
	if args.size() >= 2 and args[0] == "client":
		join_ticket = args[1]
	if "--auto" in args:
		auto_move = true

	net = load("res://addons/peerova/PeerovaNode.cs").new()
	add_child(net)
	net.connect("HostStarted", func(ticket): print("TICKET ", ticket))
	net.connect("RoomCodeReady", func(code): print("CODE ", code))
	net.connect("Connected", _on_connected)
	net.connect("Disconnected", _on_disconnected)
	net.connect("ConnectFailed", func(reason): push_error("peerova: " + reason))
	net.connect("Message", _on_message)

	if join_ticket.is_empty():
		net.Host()
	else:
		net.Join(join_ticket)

func _on_connected(peer: int) -> void:
	print("peer ", peer, " connected")
	if not net.IsHost:
		net.SendText(peer, "hello from GDScript", CHAT_CHANNEL, true)

func _on_disconnected(peer: int) -> void:
	print("peer ", peer, " left")
	others.erase(peer)

func _on_message(peer: int, channel: int, data: PackedByteArray, _reliable: bool) -> void:
	if channel == CHAT_CHANNEL:
		var line := data.get_string_from_utf8()
		if net.IsHost:
			line = "[peer %d] %s" % [peer, line]
			_forward(peer, line.to_utf8_buffer(), CHAT_CHANNEL, true)
		print(line)
	elif channel == POSITION_CHANNEL and data.size() == 12:
		var player: int = peer if net.IsHost else data.decode_u32(0)
		others[player] = Vector2(data.decode_float(4), data.decode_float(8))
		if net.IsHost:
			var fwd := data.duplicate()
			fwd.encode_u32(0, peer)
			_forward(peer, fwd, POSITION_CHANNEL, false)

func _forward(from: int, data: PackedByteArray, channel: int, reliable: bool) -> void:
	for p in net.GetPeers():
		if p != from:
			net.SendBytes(p, data, channel, reliable)

## Sends a chat line to everyone (host: all clients; client: the host, which forwards it).
func send_chat(text: String) -> void:
	var line := ("[host] " + text) if net.IsHost else text
	net.Broadcast(line.to_utf8_buffer(), CHAT_CHANNEL, true)

func _process(delta: float) -> void:
	_time += delta
	if auto_move:
		position = Vector2(400 + cos(_time) * 150, 300 + sin(_time) * 150)
	else:
		position += Input.get_vector("ui_left", "ui_right", "ui_up", "ui_down") * 200 * delta

	if net.GetPeerCount() > 0 and _time >= _next_send:
		_next_send = _time + SEND_INTERVAL
		var packet := PackedByteArray()
		packet.resize(12)
		packet.encode_u32(0, 0)
		packet.encode_float(4, position.x)
		packet.encode_float(8, position.y)
		net.Broadcast(packet, POSITION_CHANNEL, false)
	queue_redraw()

func _draw() -> void:
	draw_circle(Vector2.ZERO, 16, Color(0.2, 0.8, 0.3))
	for id in others:
		draw_circle(others[id] - position, 12, Color(0.9, 0.3, 0.2))
