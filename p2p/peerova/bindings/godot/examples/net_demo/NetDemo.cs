// How to run: copy this folder into a Godot 4 .NET project that has the peerova addon enabled, create a scene
// with a Node2D root, attach NetDemo.cs and run it twice:
//   godot --path <project> res://net_demo.tscn -- server
//   godot --path <project> res://net_demo.tscn -- client <ticket|code> [--auto]
// (or set JoinTicket in the inspector; empty = host). Arrow keys move your circle; --auto moves it by itself.
//
// Position sync + chat over Peerova:
//   channel 0, reliable   : chat lines (UTF-8)
//   channel 1, unreliable : positions, 20 times per second, 12 bytes = [u32 player][f32 x][f32 y] little-endian
// The host forwards chat and positions to the other clients and writes the sender's peer id into "player"
// (the host itself is player 0). A lost position update is fine: the next one replaces it.
using System;
using System.Buffers.Binary;
using System.Collections.Generic;
using System.Text;
using Godot;
using Peerova.GodotAddon;

public partial class NetDemo : Node2D
{
    const int ChatChannel = 0;
    const int PositionChannel = 1;
    const double SendInterval = 0.05; // 20 Hz

    /// <summary>Ticket or 6-character connect code to join. Empty = host a server.</summary>
    [Export] public string JoinTicket { get; set; } = "";
    /// <summary>Move the local circle automatically instead of with the arrow keys.</summary>
    [Export] public bool AutoMove { get; set; } = false;
    /// <summary>Host: try to open the port on the router via UPnP.</summary>
    [Export] public bool Upnp { get; set; } = true;

    /// <summary>The networking node (created in _Ready).</summary>
    public PeerovaNode Net { get; private set; }
    /// <summary>Number of position updates received so far.</summary>
    public int PositionsReceived { get; private set; }
    /// <summary>Number of chat lines received so far.</summary>
    public int ChatReceived { get; private set; }

    readonly Dictionary<long, Vector2> _others = new Dictionary<long, Vector2>();
    readonly byte[] _packet = new byte[12];
    double _time, _nextSend;

    public override void _Ready()
    {
        var args = OS.GetCmdlineUserArgs();
        if (args.Length >= 2 && args[0] == "client") JoinTicket = args[1];
        if (Array.IndexOf(args, "--auto") >= 0) AutoMove = true;

        Net = new PeerovaNode { Upnp = Upnp };
        AddChild(Net);
        Net.HostStarted += ticket => GD.Print("TICKET ", ticket);
        Net.RoomCodeReady += code => GD.Print("CODE ", code);
        Net.Connected += peer =>
        {
            GD.Print("peer ", peer, " connected");
            if (!Net.IsHost) Net.SendText(peer, "hello from Godot", ChatChannel, true);
        };
        Net.Disconnected += peer => { GD.Print("peer ", peer, " left"); _others.Remove(peer); };
        Net.ConnectFailed += reason => GD.PushError("peerova: " + reason);
        Net.Message += OnMessage;

        if (string.IsNullOrEmpty(JoinTicket)) Net.Host(); else Net.Join(JoinTicket);
    }

    void OnMessage(long peer, long channel, byte[] data, bool reliable)
    {
        if (channel == ChatChannel)
        {
            ChatReceived++;
            string line = Net.IsHost ? $"[peer {peer}] {Encoding.UTF8.GetString(data)}" : Encoding.UTF8.GetString(data);
            GD.Print(line);
            if (Net.IsHost) Forward(peer, Encoding.UTF8.GetBytes(line), ChatChannel, true);
        }
        else if (channel == PositionChannel && data.Length == 12)
        {
            PositionsReceived++;
            long player = Net.IsHost ? peer : BinaryPrimitives.ReadUInt32LittleEndian(data);
            _others[player] = new Vector2(ReadFloat(data, 4), ReadFloat(data, 8));
            if (Net.IsHost)
            {
                var fwd = (byte[])data.Clone();
                BinaryPrimitives.WriteUInt32LittleEndian(fwd, (uint)peer);
                Forward(peer, fwd, PositionChannel, false);
            }
        }
    }

    void Forward(long from, byte[] data, int channel, bool reliable)
    {
        foreach (var p in Net.GetPeers()) if (p != from) Net.SendBytes(p, data, channel, reliable);
    }

    /// <summary>Sends a chat line to everyone (host: to all clients; client: to the host, which forwards it).</summary>
    public void SendChat(string text) =>
        Net.Broadcast(Encoding.UTF8.GetBytes(Net.IsHost ? "[host] " + text : text), ChatChannel, true);

    public override void _Process(double delta)
    {
        _time += delta;
        if (AutoMove)
            Position = new Vector2(400 + Mathf.Cos((float)_time) * 150, 300 + Mathf.Sin((float)_time) * 150);
        else
            Position += Input.GetVector("ui_left", "ui_right", "ui_up", "ui_down") * (float)(200 * delta);

        if (Net.GetPeerCount() > 0 && _time >= _nextSend)
        {
            _nextSend = _time + SendInterval;
            BinaryPrimitives.WriteUInt32LittleEndian(_packet, 0);
            WriteFloat(_packet, 4, Position.X);
            WriteFloat(_packet, 8, Position.Y);
            Net.Broadcast(_packet, PositionChannel, false);
        }
        QueueRedraw();
    }

    public override void _Draw()
    {
        // _Draw uses local coordinates; this node sits at our own position.
        DrawCircle(Vector2.Zero, 16, new Color(0.2f, 0.8f, 0.3f));
        foreach (var kv in _others) DrawCircle(kv.Value - Position, 12, new Color(0.9f, 0.3f, 0.2f));
    }

    static float ReadFloat(byte[] b, int at) => BitConverter.Int32BitsToSingle(BinaryPrimitives.ReadInt32LittleEndian(b.AsSpan(at)));
    static void WriteFloat(byte[] b, int at, float v) => BinaryPrimitives.WriteInt32LittleEndian(b.AsSpan(at), BitConverter.SingleToInt32Bits(v));
}
