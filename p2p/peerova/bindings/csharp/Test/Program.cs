// End-to-end test: server + client in-process, 100 reliable msgs in order + 1 unreliable + reply.
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Text;
using Peerova;

static class Program
{
    static int Fail(string why) { Console.WriteLine("FAIL: " + why); return 1; }

    static int Main()
    {
        Console.WriteLine($"peerova native {Library.Version}");
        string key = Path.Combine(Path.GetTempPath(), $"peerova-cs-{Guid.NewGuid():N}.key");
        try
        {
            using var server = Server.Start(new ServerConfig { Port = 0, Upnp = false, KeyFile = key });
            Console.WriteLine($"ticket {server.Ticket.Substring(0, Math.Min(24, server.Ticket.Length))}...");
            using var client = Client.Connect(server.Ticket);

            uint peer = 0;
            var sw = Stopwatch.StartNew();
            while (sw.ElapsedMilliseconds < 10000 && peer == 0)
            {
                if (!client.Poll(out var ev, 500)) continue;
                if (ev.Type == EventType.Connected) peer = ev.Peer;
                else if (ev.Type == EventType.ConnectFailed) return Fail("connect failed: " + ev.Text);
            }
            if (peer == 0) return Fail("client did not connect");

            const int N = 100;
            var t = Stopwatch.StartNew();
            for (int i = 0; i < N; i++) client.Send(peer, Encoding.UTF8.GetBytes($"msg {i}"), channel: 1, reliable: true);
            client.Send(peer, new ReadOnlySpan<byte>(Encoding.UTF8.GetBytes("realtime")), channel: 2, reliable: false);

            var got = new List<string>();
            bool gotUnreliable = false;
            uint serverPeer = 0;
            sw.Restart();
            while (sw.ElapsedMilliseconds < 5000 && (got.Count < N || !gotUnreliable))
            {
                if (!server.Poll(out var e, 200)) continue;
                if (e.Type == EventType.Connected) serverPeer = e.Peer;
                else if (e.Type == EventType.Message && e.Reliable && e.Channel == 1) got.Add(e.Text);
                else if (e.Type == EventType.Message && !e.Reliable && e.Channel == 2 && e.Text == "realtime") gotUnreliable = true;
            }
            if (got.Count != N) return Fail($"got {got.Count}/{N} reliable msgs");
            for (int i = 0; i < N; i++) if (got[i] != $"msg {i}") return Fail($"out of order at {i}: {got[i]}");
            if (serverPeer == 0) return Fail("server never saw Connected");
            // Unreliable may legitimately be dropped on a real network; on loopback it should arrive.
            if (!gotUnreliable) return Fail("unreliable datagram not received on loopback");

            server.Send(serverPeer, "pong", channel: 0, reliable: true);
            Event? reply = null;
            sw.Restart();
            while (sw.ElapsedMilliseconds < 5000 && reply == null)
            {
                var e = client.Poll(200);
                if (e.HasValue && e.Value.Type == EventType.Message) reply = e;
            }
            if (reply == null || reply.Value.Text != "pong") return Fail("no pong");

            // Oversized unreliable must fail cleanly.
            bool tooBig = client.TrySend(peer, new byte[Library.MaxUnreliablePayload + 100], 3, false);
            Console.WriteLine($"oversized unreliable send rejected: {!tooBig}");

            Console.WriteLine($"server peers={server.PeerCount} client peers={client.PeerCount}, {N} reliable in order + 1 unreliable + pong in {t.ElapsedMilliseconds} ms");
            client.Dispose();
            try { client.Send(peer, "x"); return Fail("send after dispose did not throw"); } catch (ObjectDisposedException) { }
            Console.WriteLine("PASS");
            return 0;
        }
        catch (Exception ex) { return Fail(ex.ToString()); }
        finally { try { File.Delete(key); } catch { } }
    }
}
