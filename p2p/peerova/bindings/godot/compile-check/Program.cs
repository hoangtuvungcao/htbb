// Drives a host and a joining PeerovaNode by calling _Ready/_Process like Godot's main loop.
using System;
using System.Diagnostics;
using System.Text;
using System.Threading;
using Peerova.GodotAddon;

static class Program
{
    static int Main()
    {
        var host = new PeerovaNode { Upnp = false, KeyFile = $"user://peerova-godot-stub-{Guid.NewGuid():N}.key" };
        var join = new PeerovaNode();
        host._Ready(); join._Ready();
        string ticket = null, reply = null; int msgs = 0;
        host.SignalSink = (sig, a) =>
        {
            if (sig == "HostStarted") ticket = (string)a[0].Obj;
            if (sig == "Message") { msgs++; host.SendText((long)a[0].Obj, "ack:" + Encoding.UTF8.GetString((byte[])a[2].Obj), 0, true); }
        };
        join.SignalSink = (sig, a) =>
        {
            if (sig == "Connected") join.SendText((long)a[0].Obj, "hello godot", 1, true);
            if (sig == "Message") reply = Encoding.UTF8.GetString((byte[])a[2].Obj);
            if (sig == "ConnectFailed") Console.WriteLine("connect failed: " + a[0].Obj);
        };
        host.Host();
        var sw = Stopwatch.StartNew();
        while (sw.ElapsedMilliseconds < 15000 && reply == null)
        {
            host._Process(0.016); join._Process(0.016);
            if (ticket != null && !join.IsRunning) join.Join(ticket);
            Thread.Sleep(16);
        }
        long peers = host.GetPeerCount();
        host._ExitTree(); join._ExitTree();
        System.IO.File.Delete(Godot.ProjectSettings.GlobalizePath(host.KeyFile));
        bool ok = reply == "ack:hello godot" && msgs == 1 && peers == 1;
        Console.WriteLine(ok ? "PASS (PeerovaNode host/join via stub _Process loop)" : $"FAIL ticket={ticket != null} msgs={msgs} reply={reply} peers={peers}");
        if (!ok) return 1;
        return RunNetDemo();
    }

    // examples/net_demo/NetDemo.cs: host + auto-moving client for ~2 s, both must see positions; host gets chat.
    static int RunNetDemo()
    {
        var host = new NetDemo { Upnp = false, AutoMove = true };
        host._Ready();
        NetDemo client = null;
        var sw = Stopwatch.StartNew();
        long connectedAt = -1;
        while (sw.ElapsedMilliseconds < 15000)
        {
            host._Process(0.016); host.Net._Process(0.016);
            if (client == null && host.Net.GetTicket() != "")
            {
                client = new NetDemo { JoinTicket = host.Net.GetTicket(), AutoMove = true };
                client._Ready();
            }
            if (client != null)
            {
                client._Process(0.016); client.Net._Process(0.016);
                if (connectedAt < 0 && client.Net.GetPeerCount() > 0) connectedAt = sw.ElapsedMilliseconds;
                if (connectedAt >= 0 && sw.ElapsedMilliseconds - connectedAt > 2000) break;
            }
            Thread.Sleep(16);
        }
        int hostPos = host.PositionsReceived, clientPos = client?.PositionsReceived ?? 0, chat = host.ChatReceived;
        client?.Net._ExitTree(); host.Net._ExitTree();
        System.IO.File.Delete(Godot.ProjectSettings.GlobalizePath(host.Net.KeyFile));
        bool ok = hostPos > 5 && clientPos > 5 && chat == 1;
        Console.WriteLine(ok ? $"PASS (NetDemo example: host got {hostPos} positions + chat, client got {clientPos} positions)"
                             : $"FAIL NetDemo hostPos={hostPos} clientPos={clientPos} chat={chat}");
        return ok ? 0 : 1;
    }
}
