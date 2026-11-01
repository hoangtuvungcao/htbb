// Round-trip test inside a real Godot 4 .NET engine (run by test.sh, headless):
// a host PeerovaNode and a client PeerovaNode in the same scene tree, driven by Godot's _Process.
//   client -> host : N reliable messages (channel 1, must arrive in order) + 1 unreliable (channel 2)
//   host -> client : binary reliable reply (channel 0) + 1 unreliable (channel 3)
//   client shuts down -> host gets Disconnected
// Exits with code 0 on success, 1 on failure or after a 20 s timeout.
using System;
using System.Linq;
using System.Text;
using Godot;
using Peerova.GodotAddon;

public partial class RoundTrip : Node
{
    const int N = 100;
    static readonly byte[] Binary = { 0x70, 0x6f, 0x6e, 0x67, 0x00, 0xff };

    PeerovaNode _host, _client;
    int _nextExpected, _hostUnreliable;
    bool _replied, _gotBinary, _gotBackUnreliable, _clientClosed;
    long _hostSidePeer = -1;
    double _elapsed;
    bool _done;

    public override void _Ready()
    {
        _host = new PeerovaNode { Name = "Host", Port = 0, Upnp = false, KeyFile = "user://peerova-test-server.key" };
        _client = new PeerovaNode { Name = "Client", ConnectTimeoutMs = 10000 };
        AddChild(_host);
        AddChild(_client);

        _host.HostStarted += ticket =>
        {
            GD.Print("host started, ticket ", ticket.Substring(0, 12), "...");
            _client.Join(ticket);
        };
        _host.ConnectFailed += reason => Fail("host: " + reason);
        _host.Connected += peer => _hostSidePeer = peer;
        _host.Message += OnHostMessage;
        _host.Disconnected += peer =>
        {
            if (_clientClosed && peer == _hostSidePeer)
                Pass();
        };

        _client.ConnectFailed += reason => Fail("client: " + reason);
        _client.Connected += peer =>
        {
            for (int i = 0; i < N; i++)
                if (!_client.SendText(peer, $"msg {i}", 1, true)) Fail("reliable send failed");
            if (!_client.SendText(peer, "realtime", 2, false)) Fail("unreliable send failed");
            if (_client.SendBytes(peer, new byte[2000], 0, false)) Fail("oversized unreliable send must fail");
        };
        _client.Message += (peer, channel, data, reliable) =>
        {
            if (reliable && channel == 0 && data.SequenceEqual(Binary)) _gotBinary = true;
            else if (!reliable && channel == 3 && Encoding.UTF8.GetString(data) == "srv-realtime") _gotBackUnreliable = true;
            if (_gotBinary && _gotBackUnreliable && !_clientClosed)
            {
                _clientClosed = true;
                // Deferred: never free the native node while its own _Process loop is emitting this signal.
                Callable.From(() => _client.Shutdown()).CallDeferred();
            }
        };

        _host.Host();
    }

    void OnHostMessage(long peer, long channel, byte[] data, bool reliable)
    {
        string text = Encoding.UTF8.GetString(data);
        if (reliable)
        {
            if (channel != 1) { Fail($"reliable on channel {channel}"); return; }
            if (text != $"msg {_nextExpected}") { Fail($"out of order: expected msg {_nextExpected}, got {text}"); return; }
            _nextExpected++;
        }
        else if (channel == 2 && text == "realtime") _hostUnreliable++;

        if (_nextExpected == N && _hostUnreliable == 1 && !_replied)
        {
            _replied = true;
            if (!_host.SendBytes(peer, Binary, 0, true)) Fail("reply send failed");
            if (!_host.SendText(peer, "srv-realtime", 3, false)) Fail("unreliable reply send failed");
        }
    }

    public override void _Process(double delta)
    {
        _elapsed += delta;
        if (!_done && _elapsed > 20)
            Fail($"timeout: reliable={_nextExpected}/{N} unreliable={_hostUnreliable} binary={_gotBinary} " +
                 $"backUnreliable={_gotBackUnreliable} clientClosed={_clientClosed}");
    }

    void Pass()
    {
        if (_done) return;
        _done = true;
        GD.Print($"godot-cs PASS: Godot {Engine.GetVersionInfo()["string"]}, {N} reliable msgs in order, " +
                 "unreliable both ways, binary reply ok, disconnect ok");
        GetTree().Quit(0);
    }

    void Fail(string why)
    {
        if (_done) return;
        _done = true;
        GD.PrintErr("godot-cs FAIL: " + why);
        GetTree().Quit(1);
    }
}
