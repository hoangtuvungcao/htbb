// Godot 4 (.NET) node wrapping one Peerova server or client. Polls in _Process and emits signals
// on the main thread, so it is usable from both C# and GDScript. C# signal/method names are kept
// as-is when seen from GDScript:
//
//   var net = $PeerovaNode
//   net.connect("HostStarted", func(ticket): print(ticket))
//   net.connect("Message", func(peer, channel, data, reliable): print(data.get_string_from_utf8()))
//   net.Host()                     # or net.Join(ticket_or_code)
//   net.SendText(peer, "hi", 0, true)
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using Godot;

namespace Peerova.GodotAddon
{
    /// <summary>
    /// One Peerova node (server or client) driven by Godot's main loop. Signals are emitted from <c>_Process</c>
    /// on the main thread; call all methods from the main thread. The node shuts down in <c>_ExitTree</c>.
    /// </summary>
    /// <remarks>
    /// Channels are 0..255. Send frequently-updated state (positions, inputs) with <c>reliable = false</c>
    /// (max 1180 bytes, may be lost or reordered) and everything that must arrive (chat, RPC) with <c>reliable = true</c>.
    /// </remarks>
    [GlobalClass]
    public partial class PeerovaNode : Node
    {
        /// <summary>Host: the server started. <paramref name="ticket"/> is the string to give to clients.</summary>
        [Signal] public delegate void HostStartedEventHandler(string ticket);
        /// <summary>Host: the 6-character connect code became available (only when <see cref="App"/> is set).</summary>
        [Signal] public delegate void RoomCodeReadyEventHandler(string code);
        /// <summary>A peer connected (host: a client; client: the server).</summary>
        [Signal] public delegate void ConnectedEventHandler(long peer);
        /// <summary>A message arrived. <paramref name="data"/> is a copy (PackedByteArray in GDScript).</summary>
        [Signal] public delegate void MessageEventHandler(long peer, long channel, byte[] data, bool reliable);
        /// <summary>A peer disconnected.</summary>
        [Signal] public delegate void DisconnectedEventHandler(long peer);
        /// <summary>Client: connecting failed. Host: the server could not start. <paramref name="reason"/> is the error text.</summary>
        [Signal] public delegate void ConnectFailedEventHandler(string reason);

        /// <summary>UDP port. 0 = random free port (the server default without this node is 47000).</summary>
        [ExportGroup("Server (Host)")]
        [Export] public int Port { get; set; } = 0;
        /// <summary>License key (PVA-...). Empty = dev mode (max 3 clients).</summary>
        [Export] public string License { get; set; } = "";
        /// <summary>Try to open the port on the router via UPnP.</summary>
        [Export] public bool Upnp { get; set; } = true;
        /// <summary>Optional shared secret clients must present (see <see cref="JoinWithToken"/>).</summary>
        [Export] public string Token { get; set; } = "";
        /// <summary>Maximum concurrent clients (0 = license limit).</summary>
        [Export] public int MaxCcu { get; set; } = 0;
        /// <summary>Server identity key (same key = same ticket; keep it secret). user:// and res:// paths are globalized.</summary>
        [Export] public string KeyFile { get; set; } = "user://peerova-server.key";

        /// <summary>Project id from the Peerova website (prj_...). Set it to get a 6-character connect code.</summary>
        [ExportGroup("Connect code (optional)")]
        [Export] public string App { get; set; } = "";
        /// <summary>Name shown on the Peerova website.</summary>
        [Export] public string ShareName { get; set; } = "";
        /// <summary>List the server publicly (otherwise only people with the code can join).</summary>
        [Export] public bool SharePublic { get; set; } = false;
        /// <summary>Maximum players advertised (0 = not set).</summary>
        [Export] public int ShareMax { get; set; } = 0;
        /// <summary>Free-form mode label.</summary>
        [Export] public string ShareMode { get; set; } = "";
        /// <summary>Region label, e.g. "VN".</summary>
        [Export] public string ShareRegion { get; set; } = "";

        /// <summary>Client: give up connecting after this many milliseconds (0 = default 20000).</summary>
        [ExportGroup("Client (Join)")]
        [Export] public int ConnectTimeoutMs { get; set; } = 0;

        /// <summary>Maximum events handled per frame (0 = unlimited).</summary>
        [ExportGroup("Polling")]
        [Export] public int MaxEventsPerFrame { get; set; } = 256;

        /// <summary>True when the last <see cref="Host"/> call made this node a server.</summary>
        public bool IsHost { get; private set; }
        /// <summary>True while a server is running or a client node exists (also while still connecting).</summary>
        public bool IsRunning => _node != null;
        /// <summary>Host: the ticket once started, else "".</summary>
        public string Ticket { get; private set; } = "";
        /// <summary>Host: the connect code once assigned, else "".</summary>
        public string RoomCode { get; private set; } = "";
        /// <summary>Peers currently connected.</summary>
        public IReadOnlyCollection<uint> Peers => _peers;

        Endpoint _node;
        Task<Server> _starting;
        double _codeCheckIn;
        readonly HashSet<uint> _peers = new HashSet<uint>();
        static bool _libConfigured;

        /// <summary>
        /// Points the C# binding at the native library: res://addons/peerova/bin/&lt;os&gt;/ in the editor, next to the
        /// executable in exported builds, otherwise the OS default search. Called automatically by this node;
        /// call it yourself before using <c>Peerova.Server</c> / <c>Peerova.Client</c> directly.
        /// </summary>
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

        /// <inheritdoc/>
        public override void _Ready() => ConfigureNativeLibrary();

        /// <summary>
        /// Starts a server with the exported settings (stops anything running first). Starting blocks up to ~3 s,
        /// so it runs on a worker thread; emits HostStarted(ticket) or ConnectFailed(reason) later.
        /// </summary>
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
                MaxCcu = MaxCcu > 0 ? MaxCcu : (int?)null,
                KeyFile = string.IsNullOrEmpty(KeyFile) ? null : ProjectSettings.GlobalizePath(KeyFile),
            };
            if (!string.IsNullOrEmpty(App))
            {
                cfg.App = App;
                cfg.Share = new ShareConfig
                {
                    Name = string.IsNullOrEmpty(ShareName) ? null : ShareName,
                    Public = SharePublic,
                    Max = ShareMax > 0 ? ShareMax : (int?)null,
                    Mode = string.IsNullOrEmpty(ShareMode) ? null : ShareMode,
                    Region = string.IsNullOrEmpty(ShareRegion) ? null : ShareRegion,
                };
            }
            string json = cfg.ToJson();
            IsHost = true;
            _starting = Task.Run(() => Server.Start(json));
        }

        /// <summary>Connects to a server by ticket or 6-character connect code. Emits Connected(peer) or ConnectFailed(reason) later.</summary>
        public void Join(string ticket) => JoinWithToken(ticket, "");

        /// <summary>Like <see cref="Join"/>, presenting <paramref name="token"/> to a server that requires one.</summary>
        public void JoinWithToken(string ticket, string token)
        {
            Shutdown();
            ConfigureNativeLibrary();
            var cfg = new ClientConfig
            {
                Token = string.IsNullOrEmpty(token) ? null : token,
                TimeoutMs = ConnectTimeoutMs > 0 ? ConnectTimeoutMs : (int?)null,
            };
            try { _node = Client.Connect(ticket ?? "", cfg); IsHost = false; }
            catch (Exception e) { EmitSignal(SignalName.ConnectFailed, e.Message); }
        }

        // Distinct method names (no overloads) so GDScript can call them.

        /// <summary>Sends bytes to a peer. Returns false if not running or the send failed (unknown peer, unreliable &gt; 1180 bytes, ...).</summary>
        public bool SendBytes(long peer, byte[] data, int channel = 0, bool reliable = true) =>
            _node != null && _node.TrySend((uint)peer, data, (byte)channel, reliable);

        /// <summary>Sends a UTF-8 string. See <see cref="SendBytes"/>.</summary>
        public bool SendText(long peer, string text, int channel = 0, bool reliable = true) =>
            SendBytes(peer, System.Text.Encoding.UTF8.GetBytes(text), channel, reliable);

        /// <summary>C#-only allocation-free send. See <see cref="SendBytes"/>.</summary>
        public bool SendSpan(uint peer, ReadOnlySpan<byte> data, byte channel = 0, bool reliable = true) =>
            _node != null && _node.TrySend(peer, data, channel, reliable);

        /// <summary>Sends to every connected peer (host: all clients; client: the server).</summary>
        public void Broadcast(byte[] data, int channel = 0, bool reliable = true)
        {
            if (_node == null) return;
            foreach (var p in _peers) _node.TrySend(p, data, (byte)channel, reliable);
        }

        /// <summary>Closes the connection to one peer; Disconnected(peer) follows.</summary>
        public void DisconnectPeer(long peer) => _node?.Disconnect((uint)peer);
        /// <summary>Host: the ticket ("" if not hosting).</summary>
        public string GetTicket() => Ticket;
        /// <summary>Host: the connect code ("" if not assigned).</summary>
        public string GetRoomCode() => RoomCode;
        /// <summary>Number of connected peers.</summary>
        public long GetPeerCount() => _node == null ? 0 : _node.PeerCount;
        /// <summary>Ids of connected peers.</summary>
        public long[] GetPeers() { var a = new long[_peers.Count]; int i = 0; foreach (var p in _peers) a[i++] = p; return a; }

        /// <summary>Stops the server or client and frees the native node. Safe to call at any time.</summary>
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
            RoomCode = "";
        }

        /// <inheritdoc/>
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

            // The connect code arrives a little after start; check twice a second.
            if (RoomCode.Length == 0 && _node is Server srv && !string.IsNullOrEmpty(App) && (_codeCheckIn -= delta) <= 0)
            {
                _codeCheckIn = 0.5;
                var code = srv.RoomCode;
                if (code != null) { RoomCode = code; EmitSignal(SignalName.RoomCodeReady, code); }
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

        /// <inheritdoc/>
        public override void _ExitTree() => Shutdown();
    }
}
