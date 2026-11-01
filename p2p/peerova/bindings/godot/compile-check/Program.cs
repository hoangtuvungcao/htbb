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
        bool ok = reply == "ack:hello godot" && msgs == 1 && peers == 1;
        Console.WriteLine(ok ? "PASS (PeerovaNode host/join via stub _Process loop)" : $"FAIL ticket={ticket != null} msgs={msgs} reply={reply} peers={peers}");
        return ok ? 0 : 1;
    }
}
