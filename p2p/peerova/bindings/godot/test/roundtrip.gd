# Same round-trip as RoundTrip.cs, written in GDScript against the C# PeerovaNode (run by test.sh, headless).
# Exits with code 0 on success, 1 on failure or after a 20 s timeout.
extends Node

const N := 100
const BINARY := [0x70, 0x6f, 0x6e, 0x67, 0x00, 0xff]

var host
var client
var next_expected := 0
var host_unreliable := 0
var replied := false
var got_binary := false
var got_back_unreliable := false
var client_closed := false
var host_side_peer := -1
var elapsed := 0.0
var done := false

func _ready() -> void:
	var node_script = load("res://addons/peerova/PeerovaNode.cs")
	host = node_script.new()
	host.name = "Host"
	host.Port = 0
	host.Upnp = false
	host.KeyFile = "user://peerova-test-server-gd.key"
	client = node_script.new()
	client.name = "Client"
	client.ConnectTimeoutMs = 10000
	add_child(host)
	add_child(client)

	host.connect("HostStarted", func(ticket: String): client.Join(ticket))
	host.connect("ConnectFailed", func(reason: String): fail("host: " + reason))
	host.connect("Connected", func(peer: int): host_side_peer = peer)
	host.connect("Message", _on_host_message)
	host.connect("Disconnected", func(peer: int):
		if client_closed and peer == host_side_peer:
			pass_test())

	client.connect("ConnectFailed", func(reason: String): fail("client: " + reason))
	client.connect("Connected", _on_client_connected)
	client.connect("Message", _on_client_message)
	host.Host()

func _on_client_connected(peer: int) -> void:
	for i in N:
		if not client.SendText(peer, "msg %d" % i, 1, true):
			fail("reliable send failed")
	if not client.SendText(peer, "realtime", 2, false):
		fail("unreliable send failed")
	var big := PackedByteArray()
	big.resize(2000)
	if client.SendBytes(peer, big, 0, false):
		fail("oversized unreliable send must fail")

func _on_host_message(peer: int, channel: int, data: PackedByteArray, reliable: bool) -> void:
	var text := data.get_string_from_utf8()
	if reliable:
		if channel != 1:
			fail("reliable on channel %d" % channel)
			return
		if text != "msg %d" % next_expected:
			fail("out of order: expected msg %d, got %s" % [next_expected, text])
			return
		next_expected += 1
	elif channel == 2 and text == "realtime":
		host_unreliable += 1
	if next_expected == N and host_unreliable == 1 and not replied:
		replied = true
		if not host.SendBytes(peer, PackedByteArray(BINARY), 0, true):
			fail("reply send failed")
		if not host.SendText(peer, "srv-realtime", 3, false):
			fail("unreliable reply send failed")

func _on_client_message(_peer: int, channel: int, data: PackedByteArray, reliable: bool) -> void:
	if reliable and channel == 0 and data == PackedByteArray(BINARY):
		got_binary = true
	elif not reliable and channel == 3 and data.get_string_from_utf8() == "srv-realtime":
		got_back_unreliable = true
	if got_binary and got_back_unreliable and not client_closed:
		client_closed = true
		client.call_deferred("Shutdown")

func _process(delta: float) -> void:
	elapsed += delta
	if not done and elapsed > 20.0:
		fail("timeout: reliable=%d/%d unreliable=%d binary=%s back_unreliable=%s client_closed=%s" % [
			next_expected, N, host_unreliable, got_binary, got_back_unreliable, client_closed])

func pass_test() -> void:
	if done:
		return
	done = true
	print("godot-gd PASS: Godot %s, %d reliable msgs in order, unreliable both ways, binary reply ok, disconnect ok" % [
		Engine.get_version_info()["string"], N])
	get_tree().quit(0)

func fail(why: String) -> void:
	if done:
		return
	done = true
	printerr("godot-gd FAIL: " + why)
	get_tree().quit(1)
