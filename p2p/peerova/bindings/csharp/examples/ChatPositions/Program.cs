// How to run:  dotnet run -c Release -- server [port]          (prints TICKET ...)
//              dotnet run -c Release -- client <ticket|code> [seconds]
//
// Tiny chat + position sync over Peerova.
//   channel 0, reliable   : chat lines (UTF-8). Type a line + Enter to send it.
//   channel 1, unreliable : positions, 20 times per second, 12 bytes = [u32 player][f32 x][f32 y] little-endian.
// The server forwards chat and positions to the other clients (it rewrites "player" to the sender's peer id;
// the server's own player id is 0). The client sends a chat line every second and disconnects after
// <seconds> (default 10, 0 = until Ctrl+C). Set PEEROVA_EXAMPLE_EXIT_WHEN_EMPTY=1 to make the server exit
// once its last client has left (useful for scripted runs).
using System;
using System.Buffers.Binary;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Threading;
using Peerova;

static class Program
{
    const byte ChatChannel = 0;
    const byte PositionChannel = 1;
    const int SendIntervalMs = 50; // 20 Hz

    static volatile bool _quit;
    static readonly ConcurrentQueue<string> _typed = new ConcurrentQueue<string>();

    static int Main(string[] args)
    {
        Console.CancelKeyPress += (_, e) => { e.Cancel = true; _quit = true; };
        // Read typed chat lines on a background thread; the network loop stays on the main thread.
        new Thread(() => { string l; while ((l = Console.In.ReadLine()) != null) _typed.Enqueue(l); }) { IsBackground = true }.Start();

        try
        {
            if (args.Length >= 1 && args[0] == "server")
                return RunServer(args.Length >= 2 ? int.Parse(args[1]) : 0);
            if (args.Length >= 2 && args[0] == "client")
                return RunClient(args[1], args.Length >= 3 ? int.Parse(args[2]) : 10);
        }
        catch (PeerovaException e) { Console.WriteLine("peerova error: " + e.Message); return 1; }
        catch (DllNotFoundException e) { Console.WriteLine("native library not found: " + e.Message); return 1; }

        Console.WriteLine("usage: server [port] | client <ticket|code> [seconds]");
        return 2;
    }

    static byte[] EncodePosition(uint player, float x, float y)
    {
        var b = new byte[12];
        BinaryPrimitives.WriteUInt32LittleEndian(b.AsSpan(0), player);
        BinaryPrimitives.WriteInt32LittleEndian(b.AsSpan(4), BitConverter.SingleToInt32Bits(x));
        BinaryPrimitives.WriteInt32LittleEndian(b.AsSpan(8), BitConverter.SingleToInt32Bits(y));
        return b;
    }

    static (uint player, float x, float y) DecodePosition(byte[] b) => (
        BinaryPrimitives.ReadUInt32LittleEndian(b.AsSpan(0)),
        BitConverter.Int32BitsToSingle(BinaryPrimitives.ReadInt32LittleEndian(b.AsSpan(4))),
        BitConverter.Int32BitsToSingle(BinaryPrimitives.ReadInt32LittleEndian(b.AsSpan(8))));

    // ---------------------------------------------------------------- server
    static int RunServer(int port)
    {
        // Keep the identity key out of the working directory for this demo.
        string key = Path.Combine(Path.GetTempPath(), "peerova-csharp-example.key");
        using var server = Server.Start(new ServerConfig { Port = port, KeyFile = key });
        Console.WriteLine("TICKET " + server.Ticket);
        Console.WriteLine($"peerova {Library.Version}; waiting for clients (Ctrl+C to stop)");

        var peers = new HashSet<uint>();
        var positionsFrom = new Dictionary<uint, int>();
        bool hadClients = false, codePrinted = false;
        var clock = Stopwatch.StartNew();
        long nextSend = 0, nextReport = 1000;

        while (!_quit)
        {
            // Drain all pending events, waiting at most 10 ms for the first one.
            uint wait = 10;
            while (server.Poll(out var ev, wait))
            {
                wait = 0;
                switch (ev.Type)
                {
                    case EventType.Connected:
                        peers.Add(ev.Peer); hadClients = true;
                        Console.WriteLine($"peer {ev.Peer} connected ({server.PeerCount} online)");
                        break;
                    case EventType.Disconnected:
                        peers.Remove(ev.Peer); positionsFrom.Remove(ev.Peer);
                        Console.WriteLine($"peer {ev.Peer} left");
                        break;
                    case EventType.Message when ev.Channel == ChatChannel:
                        Console.WriteLine($"[peer {ev.Peer}] {ev.Text}");
                        foreach (var p in peers) if (p != ev.Peer) server.TrySend(p, Encoding.UTF8.GetBytes($"[peer {ev.Peer}] {ev.Text}"), ChatChannel, true);
                        break;
                    case EventType.Message when ev.Channel == PositionChannel && ev.Data.Length == 12:
                        positionsFrom[ev.Peer] = positionsFrom.TryGetValue(ev.Peer, out var n) ? n + 1 : 1;
                        var (_, x, y) = DecodePosition(ev.Data);
                        var fwd = EncodePosition(ev.Peer, x, y);
                        foreach (var p in peers) if (p != ev.Peer) server.TrySend(p, fwd, PositionChannel, false);
                        break;
                }
            }

            long now = clock.ElapsedMilliseconds;
            if (now >= nextSend)
            {
                nextSend = now + SendIntervalMs;
                float t = now / 1000f;
                var pos = EncodePosition(0, MathF.Cos(t) * 5, MathF.Sin(t) * 5);
                foreach (var p in peers) server.TrySend(p, pos, PositionChannel, false); // a lost update is fine
            }
            while (_typed.TryDequeue(out var line))
                foreach (var p in peers) server.TrySend(p, Encoding.UTF8.GetBytes("[server] " + line), ChatChannel, true);
            if (now >= nextReport)
            {
                nextReport = now + 1000;
                if (!codePrinted && server.RoomCode is string code) { codePrinted = true; Console.WriteLine("CODE " + code); }
                foreach (var kv in positionsFrom) Console.WriteLine($"peer {kv.Key}: {kv.Value} position updates so far");
            }
            if (hadClients && peers.Count == 0 && Environment.GetEnvironmentVariable("PEEROVA_EXAMPLE_EXIT_WHEN_EMPTY") == "1") break;
        }
        Console.WriteLine("server: shutting down");
        return 0; // `using` disposes the server
    }

    // ---------------------------------------------------------------- client
    static int RunClient(string ticketOrCode, int seconds)
    {
        using var client = Client.Connect(ticketOrCode, new ClientConfig { TimeoutMs = 20000 });
        uint server = 0;
        int positionsReceived = 0, chatReceived = 0;
        var clock = Stopwatch.StartNew();
        long connectedAt = 0, nextSend = 0, nextChat = 0;
        int chatSent = 0;

        while (!_quit)
        {
            uint wait = 10;
            while (client.Poll(out var ev, wait))
            {
                wait = 0;
                switch (ev.Type)
                {
                    case EventType.Connected:
                        server = ev.Peer; connectedAt = clock.ElapsedMilliseconds;
                        Console.WriteLine($"connected in {connectedAt} ms");
                        break;
                    case EventType.ConnectFailed:
                        Console.WriteLine("connect failed: " + ev.Text);
                        return 1;
                    case EventType.Disconnected:
                        Console.WriteLine("server closed the connection");
                        return server != 0 ? 0 : 1;
                    case EventType.Message when ev.Channel == ChatChannel:
                        chatReceived++;
                        Console.WriteLine(ev.Text);
                        break;
                    case EventType.Message when ev.Channel == PositionChannel && ev.Data.Length == 12:
                        positionsReceived++;
                        break;
                }
            }
            if (server == 0) continue;

            long now = clock.ElapsedMilliseconds;
            if (now >= nextSend)
            {
                nextSend = now + SendIntervalMs;
                float t = now / 1000f;
                client.TrySend(server, EncodePosition(0, MathF.Sin(t) * 3, MathF.Cos(t) * 3), PositionChannel, false);
            }
            if (now >= nextChat)
            {
                nextChat = now + 1000;
                client.Send(server, $"hello #{++chatSent} from C#", ChatChannel, true);
            }
            while (_typed.TryDequeue(out var line)) client.Send(server, line, ChatChannel, true);
            if (seconds > 0 && now - connectedAt >= seconds * 1000L) break;
        }

        Console.WriteLine($"client: received {positionsReceived} position updates, {chatReceived} chat lines; disconnecting");
        if (server != 0) client.Disconnect(server);
        return positionsReceived > 0 ? 0 : 1;
    }
}
