// Godot 4 (.NET) node wrapping one peerova server or client. Polls in _Process and emits signals
// on the main thread, so it is usable from both C# and GDScript. C# signal/method names are kept
// as-is when seen from GDScript:
//
//   var net = $PeerovaNode
//   net.connect("HostStarted", func(ticket): print(ticket))
//   net.connect("Message", func(peer, channel, data, reliable): print(data.get_string_from_utf8()))
//   net.Host()                     # or net.Join(ticket)
//   net.SendText(peer, "hi", 0, true)
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using Godot;

namespace Peerova.GodotAddon
{
    [GlobalClass]
    public partial class PeerovaNode : Node
    {
        [Signal] public delegate void HostStartedEventHandler(string ticket);
        [Signal] public delegate void ConnectedEventHandler(long peer);
        [Signal] public delegate void MessageEventHandler(long peer, long channel, byte[] data, bool reliable);
        [Signal] public delegate void DisconnectedEventHandler(long peer);
        [Signal] public delegate void ConnectFailedEventHandler(string reason);

        [ExportGroup("Server (Host)")]
        [Export] public int Port { get; set; } = 0;
        [Export] public string License { get; set; } = "";
        [Export] public bool Upnp { get; set; } = true;
        [Export] public string Token { get; set; } = "";
        /// <summary>Server identity key (keeps the ticket stable). user:// and res:// paths are globalized.</summary>
        [Export] public string KeyFile { get; set; } = "user://peerova-server.key";

        [ExportGroup("Polling")]
        [Export] public int MaxEventsPerFrame { get; set; } = 256;

        public bool IsHost { get; private set; }
        public bool IsRunning => _node != null;
        public string Ticket { get; private set; } = "";
        public IReadOnlyCollection<uint> Peers => _peers;

        Endpoint _node;
        Task<Server> _starting;
        readonly HashSet<uint> _peers = new HashSet<uint>();
        static bool _libConfigured;

        /// <summary>Point the C# binding at the native lib: res://addons/peerova/bin/&lt;os&gt;/ in the editor,
        /// next to the executable in exported builds. Call before any other peerova API if you use Peerova.* directly.</summary>
        public static void ConfigureNativeLibrary()
        {
            if (_libConfigured || !string.IsNullOrEmpty(Native.LibraryPath)) { _libConfigured = true; return; }
            _libConfigured = true;
            string os = OS.GetName();
            string file, dir;
            switch (os)
            {
                case "Windows": file = "peerova.dll"; dir = "windows"; break;
                case "macOS": file = "libpeerova.dylib"; dir = "macos"; break;
                default: file = "libpeerova.so"; dir = "linux"; break; // Linux, *BSD, Android (APK lib dir via default probing)
            }
            var candidates = new[]
            {
                ProjectSettings.GlobalizePath($"res://addons/peerova/bin/{dir}/{file}"),
                Path.Combine(Path.GetDirectoryName(OS.GetExecutablePath()) ?? "", file),
            };
            foreach (var c in candidates)
                if (File.Exists(c)) { Native.LibraryPath = c; return; }
            // else: DllImport default probing (PATH / LD_LIBRARY_PATH / APK native lib dir)
        }

        public override void _Ready() => ConfigureNativeLibrary();

        /// <summary>Start a server on a worker thread (start blocks ≤ ~3 s). Emits HostStarted(ticket) or ConnectFailed(reason).</summary>
        public void Host()
        {
            Shutdown();
            ConfigureNativeLibrary();
            var cfg = new ServerConfig
            {
                Port = Port,
                Upnp = Upnp,
                License = string.IsNullOrEmpty(License) ? null : License,
                Token = string.IsNullOrEmpty(Token) ? null : Token,
                KeyFile = string.IsNullOrEmpty(KeyFile) ? null : ProjectSettings.GlobalizePath(KeyFile),
            };
            string json = cfg.ToJson();
            IsHost = true;
            _starting = Task.Run(() => Server.Start(json));
        }

        /// <summary>Connect to a host ticket. Emits Connected(peer) or ConnectFailed(reason) later.</summary>
        public void Join(string ticket) => JoinWithToken(ticket, "");

        public void JoinWithToken(string ticket, string token)
        {
            Shutdown();
            ConfigureNativeLibrary();
            string cfg = string.IsNullOrEmpty(token) ? null : "{\"token\":" + ServerConfig.QuoteJson(token) + "}";
            try { _node = Client.Connect(ticket.Trim(), cfg); IsHost = false; }
            catch (Exception e) { EmitSignal(SignalName.ConnectFailed, e.Message); }
        }

        // Distinct method names (no overloads) so GDScript can call them.
        public bool SendBytes(long peer, byte[] data, int channel = 0, bool reliable = true) =>
            _node != null && _node.TrySend((uint)peer, data, (byte)channel, reliable);

        public bool SendText(long peer, string text, int channel = 0, bool reliable = true) =>
            SendBytes(peer, System.Text.Encoding.UTF8.GetBytes(text), channel, reliable);

        /// <summary>C#-only zero-copy path.</summary>
        public bool SendSpan(uint peer, ReadOnlySpan<byte> data, byte channel = 0, bool reliable = true) =>
            _node != null && _node.TrySend(peer, data, channel, reliable);

        public void Broadcast(byte[] data, int channel = 0, bool reliable = true)
        {
            if (_node == null) return;
            foreach (var p in _peers) _node.TrySend(p, data, (byte)channel, reliable);
        }

        public void DisconnectPeer(long peer) => _node?.Disconnect((uint)peer);
        public string GetTicket() => Ticket;
        public long GetPeerCount() => _node == null ? 0 : _node.PeerCount;
        public long[] GetPeers() { var a = new long[_peers.Count]; int i = 0; foreach (var p in _peers) a[i++] = p; return a; }

        public void Shutdown()
        {
            if (_starting != null)
            {
                var t = _starting; _starting = null;
                t.ContinueWith(x => { if (x.Status == TaskStatus.RanToCompletion) x.Result.Dispose(); });
            }
            _node?.Dispose();
            _node = null;
            _peers.Clear();
            Ticket = "";
        }

        public override void _Process(double delta)
        {
            if (_starting != null && _starting.IsCompleted)
            {
                var t = _starting; _starting = null;
                if (t.Status == TaskStatus.RanToCompletion)
                {
                    _node = t.Result; Ticket = t.Result.Ticket;
                    EmitSignal(SignalName.HostStarted, Ticket);
                }
                else
                {
                    var msg = t.Exception?.GetBaseException().Message ?? "server start cancelled";
                    GD.PushError("peerova: server start failed: " + msg);
                    EmitSignal(SignalName.ConnectFailed, msg);
                }
            }
            int n = 0;
            while (_node != null && (MaxEventsPerFrame <= 0 || n < MaxEventsPerFrame) && _node.Poll(out Peerova.Event ev))
            {
                n++;
                switch (ev.Type)
                {
                    case EventType.Connected: _peers.Add(ev.Peer); EmitSignal(SignalName.Connected, (long)ev.Peer); break;
                    case EventType.Disconnected: _peers.Remove(ev.Peer); EmitSignal(SignalName.Disconnected, (long)ev.Peer); break;
                    case EventType.Message: EmitSignal(SignalName.Message, (long)ev.Peer, (long)ev.Channel, ev.Data, ev.Reliable); break;
                    case EventType.ConnectFailed: EmitSignal(SignalName.ConnectFailed, ev.Text); break;
                }
            }
        }

        public override void _ExitTree() => Shutdown();
    }
}
