// Peerova — peer-to-peer networking SDK (end-to-end encrypted). C# binding over the C ABI (peerova.h).
//
// Targets: .NET Standard 2.1 (Unity 2021.3+), .NET 6+ (Godot 4 .NET, console apps, servers).
// No `unsafe` code required.
//
//   using var server = Peerova.Server.Start(new Peerova.ServerConfig { Port = 0 });
//   Console.WriteLine(server.Ticket);                       // give this to clients
//   using var client = Peerova.Client.Connect(ticketOrCode);
//   while (client.Poll(out var ev)) { ... }                 // non-blocking, call every frame
//   client.Send(ev.Peer, bytes, channel: 0, reliable: true);
//
// Threading: every node has an internal lock, so Send / Poll / Disconnect may be called from any
// thread. Event.Data is a managed copy (safe to keep). Dispose() frees the native node; do not
// dispose a node while another thread is still using it.
//
// Native library lookup: DllImport("peerova") -> peerova.dll / libpeerova.so / libpeerova.dylib.
// On .NET 6+ an extra resolver first tries Native.LibraryPath, the PEEROVA_LIB environment variable
// and the application base directory. On iOS (Unity, IL2CPP) the static library is linked: "__Internal".
using System;
using System.ComponentModel;
using System.Runtime.InteropServices;
using System.Text;

namespace Peerova
{
    /// <summary>Kind of a network <see cref="Event"/>.</summary>
    public enum EventType
    {
        /// <summary>No event (default value of an empty <see cref="Event"/>).</summary>
        None = 0,
        /// <summary>A peer connected. Server: a new client (<see cref="Event.Peer"/> is its id). Client: the server.</summary>
        Connected = 1,
        /// <summary>A peer disconnected (closed, timed out or was disconnected locally).</summary>
        Disconnected = 2,
        /// <summary>A message arrived. <see cref="Event.Data"/>, <see cref="Event.Channel"/> and <see cref="Event.Reliable"/> are set.</summary>
        Message = 3,
        /// <summary>Client only: the connection could not be established. <see cref="Event.Text"/> is the reason.</summary>
        ConnectFailed = 4,
    }

    /// <summary>One network event returned by <see cref="Endpoint.Poll(out Event)"/>.</summary>
    /// <remarks><see cref="Data"/> is a managed copy, so the event can be stored or passed to other threads.</remarks>
    public readonly struct Event
    {
        /// <summary>What happened.</summary>
        public readonly EventType Type;
        /// <summary>Peer (connection) id. Use it with <see cref="Endpoint.Send(uint, ReadOnlySpan{byte}, byte, bool)"/> and <see cref="Endpoint.Disconnect"/>.</summary>
        public readonly uint Peer;
        /// <summary>Channel the message arrived on (0..255). Only meaningful for <see cref="EventType.Message"/>.</summary>
        public readonly byte Channel;
        /// <summary>True if the message was sent reliably. Only meaningful for <see cref="EventType.Message"/>.</summary>
        public readonly bool Reliable;
        /// <summary>Payload (message bytes, or the UTF-8 reason for <see cref="EventType.ConnectFailed"/>). Never null.</summary>
        public readonly byte[] Data;

        /// <summary>Creates an event. Normally events come from <see cref="Endpoint.Poll(out Event)"/>.</summary>
        public Event(EventType type, uint peer, byte channel, bool reliable, byte[] data)
        {
            Type = type; Peer = peer; Channel = channel; Reliable = reliable; Data = data ?? Array.Empty<byte>();
        }

        /// <summary><see cref="Data"/> decoded as UTF-8 (for text messages and the <see cref="EventType.ConnectFailed"/> reason).</summary>
        public string Text => Encoding.UTF8.GetString(Data);

        /// <inheritdoc/>
        public override string ToString() => $"{Type} peer={Peer} ch={Channel} reliable={Reliable} len={Data.Length}";
    }

    /// <summary>Thrown when a native Peerova call fails. <see cref="Exception.Message"/> is the text of <c>peerova_last_error()</c>.</summary>
    public class PeerovaException : Exception
    {
        /// <summary>Creates the exception with the given message.</summary>
        public PeerovaException(string message) : base(message) { }
    }

    /// <summary>Owns a native <c>peerova_node*</c>; released with <c>peerova_free</c> when disposed or finalized.</summary>
    public sealed class NodeHandle : SafeHandle
    {
        /// <summary>Creates an empty (invalid) handle; filled in by the interop marshaller.</summary>
        public NodeHandle() : base(IntPtr.Zero, true) { }

        /// <inheritdoc/>
        public override bool IsInvalid => handle == IntPtr.Zero;

        /// <inheritdoc/>
        protected override bool ReleaseHandle()
        {
            Native.peerova_free(handle);
            return true;
        }
    }

    /// <summary>
    /// Raw P/Invoke declarations for the C ABI (peerova.h). Prefer <see cref="Server"/> and <see cref="Client"/>;
    /// use this only if you need the exact C functions.
    /// </summary>
    public static class Native
    {
#if (UNITY_IOS || UNITY_TVOS) && !UNITY_EDITOR
        /// <summary>Library name used by DllImport ("__Internal" on iOS, where the static library is linked in).</summary>
        public const string Lib = "__Internal";
#else
        /// <summary>Library name used by DllImport: peerova.dll / libpeerova.so / libpeerova.dylib.</summary>
        public const string Lib = "peerova";
#endif

        /// <summary>Runs the static constructor (installs the .NET 6+ library resolver); used by <see cref="Tunnel"/>.</summary>
        internal static void EnsureResolver() { }

        /// <summary>Mirror of the C struct <c>peerova_event</c> (sequential layout matches C on 32- and 64-bit).</summary>
        [StructLayout(LayoutKind.Sequential)]
        public struct RawEvent
        {
            /// <summary>Event type (see <see cref="EventType"/>).</summary>
            public int type;
            /// <summary>Peer id.</summary>
            public uint peer;
            /// <summary>Channel 0..255.</summary>
            public byte channel;
            /// <summary>1 if the message arrived on a reliable channel.</summary>
            public byte reliable;
            /// <summary>Payload pointer, valid only until the next poll / free on this node.</summary>
            public IntPtr data;
            /// <summary>Payload length in bytes.</summary>
            public uint len;
        }

#if NET6_0_OR_GREATER
        /// <summary>
        /// Optional absolute path to the native library (.NET 6+ only). Set it before the first Peerova call.
        /// If null, the PEEROVA_LIB environment variable, then the application base directory, then the
        /// default OS search path are tried.
        /// </summary>
        public static string LibraryPath;

        static Native()
        {
            try
            {
                NativeLibrary.SetDllImportResolver(typeof(Native).Assembly, Resolve);
            }
            catch (InvalidOperationException) { /* a resolver is already set for this assembly */ }
        }

        static IntPtr Resolve(string name, System.Reflection.Assembly asm, DllImportSearchPath? path)
        {
            if (name != Lib) return IntPtr.Zero;
            foreach (var p in new[] { LibraryPath, Environment.GetEnvironmentVariable("PEEROVA_LIB") })
                if (!string.IsNullOrEmpty(p) && NativeLibrary.TryLoad(p, out var h)) return h;
            string file = OperatingSystem.IsWindows() ? "peerova.dll" : OperatingSystem.IsMacOS() ? "libpeerova.dylib" : "libpeerova.so";
            if (NativeLibrary.TryLoad(System.IO.Path.Combine(AppContext.BaseDirectory, file), out var h2)) return h2;
            return IntPtr.Zero; // fall back to default probing
        }
#endif

        /// <summary><c>peerova_server_start</c>: NUL-terminated UTF-8 JSON (or null). Invalid handle on error.</summary>
        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern NodeHandle peerova_server_start(byte[] configJson);

        /// <summary><c>peerova_client_connect</c>: ticket or 6-character connect code, plus optional JSON config.</summary>
        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern NodeHandle peerova_client_connect(byte[] ticket, byte[] configJson);

        /// <summary><c>peerova_ticket</c>: copies the ticket into buf; returns the required size including the NUL (0 = none).</summary>
        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern int peerova_ticket(NodeHandle node, byte[] buf, int bufLen);

        /// <summary><c>peerova_room_code</c>: copies the connect code into buf; returns the required size including the NUL (0 = not available yet).</summary>
        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern int peerova_room_code(NodeHandle node, byte[] buf, int bufLen);

        /// <summary><c>peerova_send</c>: returns 0 on success.</summary>
        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern int peerova_send(NodeHandle node, uint peer, byte channel, ref byte data, uint len, int reliable);

        /// <summary><c>peerova_poll</c>: non-blocking; returns 1 and fills ev if an event is available.</summary>
        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern int peerova_poll(NodeHandle node, out RawEvent ev);

        /// <summary><c>peerova_poll_wait</c>: like peerova_poll but waits up to timeoutMs.</summary>
        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern int peerova_poll_wait(NodeHandle node, out RawEvent ev, uint timeoutMs);

        /// <summary><c>peerova_disconnect</c>: closes the connection to one peer.</summary>
        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern void peerova_disconnect(NodeHandle node, uint peer);

        /// <summary><c>peerova_peer_count</c>: number of connected peers.</summary>
        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern uint peerova_peer_count(NodeHandle node);

        /// <summary><c>peerova_free</c>: frees a node. Called by <see cref="NodeHandle"/>; do not call it yourself.</summary>
        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern void peerova_free(IntPtr node);

        /// <summary><c>peerova_last_error</c>: UTF-8 error text of the last failed call on this thread.</summary>
        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr peerova_last_error();

        /// <summary><c>peerova_version</c>: UTF-8 version string of the native library.</summary>
        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr peerova_version();

        internal static byte[] Utf8Z(string s)
        {
            if (s == null) return null;
            var n = Encoding.UTF8.GetByteCount(s);
            var b = new byte[n + 1];
            Encoding.UTF8.GetBytes(s, 0, s.Length, b, 0);
            return b;
        }

        internal static string FromUtf8Z(IntPtr p)
        {
            if (p == IntPtr.Zero) return null;
            int n = 0;
            while (Marshal.ReadByte(p, n) != 0) n++;
            var b = new byte[n];
            Marshal.Copy(p, b, 0, n);
            return Encoding.UTF8.GetString(b);
        }

        /// <summary>Reads a NUL-terminated string through a "size, then fill" C function. Null if none.</summary>
        internal static string ReadString(NodeHandle h, Func<NodeHandle, byte[], int, int> fn)
        {
            int n = fn(h, null, 0);
            if (n <= 1) return null;
            var buf = new byte[n];
            int m = fn(h, buf, n);
            if (m <= 1 || m > n) return null;
            return Encoding.UTF8.GetString(buf, 0, m - 1);
        }

        internal static string LastError() => FromUtf8Z(peerova_last_error()) ?? "unknown peerova error";
    }

    /// <summary>Library-wide information.</summary>
    public static class Library
    {
        /// <summary>Version string of the loaded native library. Throws <see cref="DllNotFoundException"/> if it cannot be loaded.</summary>
        public static string Version => Native.FromUtf8Z(Native.peerova_version());

        /// <summary>Maximum payload size in bytes for unreliable sends (1180). Larger unreliable sends fail.</summary>
        public const int MaxUnreliablePayload = 1180;
    }

    /// <summary>
    /// Common functionality of <see cref="Server"/> and <see cref="Client"/> (named Endpoint to avoid clashing with Godot.Node).
    /// </summary>
    /// <remarks>
    /// All members are thread-safe. Calling any member after <see cref="Dispose"/> throws <see cref="ObjectDisposedException"/>.
    /// </remarks>
    public abstract class Endpoint : IDisposable
    {
        /// <summary>The native node handle.</summary>
        protected readonly NodeHandle Handle;

        /// <summary>Wraps a handle returned by a native start/connect call; throws <see cref="PeerovaException"/> if it is invalid.</summary>
        protected Endpoint(NodeHandle handle)
        {
            if (handle == null || handle.IsInvalid)
            {
                var err = Native.LastError();
                handle?.Dispose();
                throw new PeerovaException(err);
            }
            Handle = handle;
        }

        /// <summary>True after <see cref="Dispose"/>.</summary>
        public bool IsDisposed => Handle.IsClosed;

        /// <summary>Non-blocking poll. Returns false when no event is pending. Call it in a loop until it returns false.</summary>
        /// <param name="ev">The event, or default when the method returns false.</param>
        public bool Poll(out Event ev) => Poll(out ev, 0);

        /// <summary>Polls for one event, waiting up to <paramref name="timeoutMs"/> milliseconds (0 = do not wait).</summary>
        /// <param name="ev">The event, or default when the method returns false.</param>
        /// <param name="timeoutMs">Maximum time to block. Do not block on a game's main thread; use 0 there.</param>
        /// <returns>True if an event was returned.</returns>
        public bool Poll(out Event ev, uint timeoutMs)
        {
            Native.RawEvent raw;
            int got = timeoutMs == 0 ? Native.peerova_poll(Handle, out raw) : Native.peerova_poll_wait(Handle, out raw, timeoutMs);
            if (got == 0) { ev = default; return false; }
            byte[] data;
            if (raw.len == 0 || raw.data == IntPtr.Zero) data = Array.Empty<byte>();
            else { data = new byte[raw.len]; Marshal.Copy(raw.data, data, 0, (int)raw.len); } // copy now: valid only until next poll
            ev = new Event((EventType)raw.type, raw.peer, raw.channel, raw.reliable != 0, data);
            return true;
        }

        /// <summary>Returns the next event, or null if none arrived within <paramref name="timeoutMs"/> (0 = do not wait).</summary>
        public Event? Poll(uint timeoutMs = 0) => Poll(out var ev, timeoutMs) ? ev : (Event?)null;

        /// <summary>Sends bytes to a peer.</summary>
        /// <param name="peer">Peer id from a <see cref="EventType.Connected"/> event.</param>
        /// <param name="data">Payload. Copied before the call returns.</param>
        /// <param name="channel">Channel 0..255. Channels are independent: a slow reliable channel does not delay others.</param>
        /// <param name="reliable">
        /// true: ordered and retransmitted within the channel, any size (chat, RPC, inventory, file chunks).
        /// false: datagram with the lowest latency, may be lost or reordered, at most <see cref="Library.MaxUnreliablePayload"/> bytes
        /// (positions, inputs, other state that is resent often).
        /// </param>
        /// <exception cref="PeerovaException">The send failed (unknown peer, payload too large, ...). Message = native error text.</exception>
        public void Send(uint peer, ReadOnlySpan<byte> data, byte channel = 0, bool reliable = true)
        {
            if (!TrySend(peer, data, channel, reliable)) throw new PeerovaException(Native.LastError());
        }

        /// <summary>Sends a string encoded as UTF-8. See <see cref="Send(uint, ReadOnlySpan{byte}, byte, bool)"/>.</summary>
        /// <exception cref="PeerovaException">The send failed.</exception>
        public void Send(uint peer, string text, byte channel = 0, bool reliable = true) =>
            Send(peer, Encoding.UTF8.GetBytes(text), channel, reliable);

        /// <summary>Like <see cref="Send(uint, ReadOnlySpan{byte}, byte, bool)"/> but returns false instead of throwing.</summary>
        /// <remarks>The reason for a failure is available from <see cref="LastError"/> on the same thread.</remarks>
        public bool TrySend(uint peer, ReadOnlySpan<byte> data, byte channel = 0, bool reliable = true)
        {
            byte dummy = 0;
            ref byte first = ref data.Length == 0 ? ref dummy : ref MemoryMarshal.GetReference(data);
            return Native.peerova_send(Handle, peer, channel, ref first, (uint)data.Length, reliable ? 1 : 0) == 0;
        }

        /// <summary>Closes the connection to one peer. A <see cref="EventType.Disconnected"/> event follows.</summary>
        public void Disconnect(uint peer) => Native.peerova_disconnect(Handle, peer);

        /// <summary>Number of currently connected peers (server: clients; client: 0 or 1).</summary>
        public uint PeerCount => Native.peerova_peer_count(Handle);

        /// <summary>Error text of the last failed Peerova call on the calling thread.</summary>
        public static string LastError => Native.LastError();

        /// <summary>Closes all connections and frees the native node. Safe to call more than once.</summary>
        public void Dispose()
        {
            Handle.Dispose();
            GC.SuppressFinalize(this);
        }
    }

    /// <summary>A Peerova server (host). Accepts clients that connect with its <see cref="Ticket"/> or <see cref="RoomCode"/>.</summary>
    public sealed class Server : Endpoint
    {
        /// <summary>
        /// The server's ticket: a public string clients pass to <see cref="Client.Connect(string, string)"/>.
        /// It stays the same across restarts as long as the same key file is used.
        /// </summary>
        public string Ticket { get; }

        Server(NodeHandle h) : base(h)
        {
            Ticket = Native.ReadString(Handle, Native.peerova_ticket) ?? "";
        }

        /// <summary>
        /// The 6-character connect code (for example "K7Q2XD"), or null if it is not available (yet).
        /// </summary>
        /// <remarks>
        /// Only assigned when the server was started with <see cref="ServerConfig.App"/> and <see cref="ServerConfig.Share"/>;
        /// it appears shortly after start, so poll this property (for example once per second) until it is non-null.
        /// Clients can pass the code to <see cref="Client.Connect(string, string)"/> instead of the ticket.
        /// </remarks>
        public string RoomCode => Native.ReadString(Handle, Native.peerova_room_code);

        /// <summary>Starts a server. Blocks until the ticket is ready (up to about 3 seconds).</summary>
        /// <param name="configJson">JSON config (see <see cref="ServerConfig"/>), or null for defaults.</param>
        /// <exception cref="PeerovaException">Start failed (port in use, invalid config or license, ...).</exception>
        /// <exception cref="DllNotFoundException">The native library could not be found.</exception>
        public static Server Start(string configJson = null) => new Server(Native.peerova_server_start(Native.Utf8Z(configJson)));

        /// <summary>Starts a server from a typed config. See <see cref="Start(string)"/>.</summary>
        public static Server Start(ServerConfig config) => Start(config?.ToJson());
    }

    /// <summary>A Peerova client. Connects to one server.</summary>
    public sealed class Client : Endpoint
    {
        Client(NodeHandle h) : base(h) { }

        /// <summary>
        /// Starts connecting to a server. Returns immediately; poll for <see cref="EventType.Connected"/>
        /// (its <see cref="Event.Peer"/> is the server's peer id) or <see cref="EventType.ConnectFailed"/>.
        /// </summary>
        /// <param name="ticket">The server's ticket, or its 6-character connect code.</param>
        /// <param name="configJson">JSON config (see <see cref="ClientConfig"/>), or null for defaults.</param>
        /// <exception cref="ArgumentException"><paramref name="ticket"/> is null or empty.</exception>
        /// <exception cref="PeerovaException">The ticket is malformed or the config is invalid.</exception>
        public static Client Connect(string ticket, string configJson = null)
        {
            if (string.IsNullOrEmpty(ticket)) throw new ArgumentException("ticket is empty", nameof(ticket));
            return new Client(Native.peerova_client_connect(Native.Utf8Z(ticket.Trim()), Native.Utf8Z(configJson)));
        }

        /// <summary>Starts connecting with a typed config. See <see cref="Connect(string, string)"/>.</summary>
        public static Client Connect(string ticket, ClientConfig config) => Connect(ticket, config?.ToJson());
    }

    /// <summary>Settings for listing the server under a 6-character connect code (used with <see cref="ServerConfig.App"/>).</summary>
    public sealed class ShareConfig
    {
        /// <summary>Display name shown on the Peerova website.</summary>
        public string Name;
        /// <summary>True to list it publicly; false = reachable only by people who know the code.</summary>
        public bool? Public;
        /// <summary>Maximum number of players / users advertised.</summary>
        public int? Max;
        /// <summary>Free-form mode label (game mode, service type, ...).</summary>
        public string Mode;
        /// <summary>Region label, for example "VN".</summary>
        public string Region;

        /// <summary>Serializes the set fields to a JSON object.</summary>
        public string ToJson()
        {
            var w = new JsonObjectWriter();
            w.Str("name", Name);
            w.Bool("public", Public);
            w.Int("max", Max);
            w.Str("mode", Mode);
            w.Str("region", Region);
            return w.ToString();
        }
    }

    /// <summary>Typed builder for the server JSON config. Only fields that are set are emitted; the rest use defaults.</summary>
    public sealed class ServerConfig
    {
        /// <summary>UDP port. Default 47000; 0 = pick a random free port.</summary>
        public int? Port;
        /// <summary>Path of the server identity key (default "server.key", created if missing). Same key = same ticket. Keep it secret.</summary>
        public string KeyFile;
        /// <summary>License key ("PVA-..."). Without it the server runs in dev mode (max 3 clients).</summary>
        public string License;
        /// <summary>Advanced; leave unset.</summary>
        [EditorBrowsable(EditorBrowsableState.Never)] public string LicenseServer;
        /// <summary>Shared secret clients must present (<see cref="ClientConfig.Token"/>). Null = no token required.</summary>
        public string Token;
        /// <summary>Maximum number of concurrent clients (capped by the license).</summary>
        public int? MaxCcu;
        /// <summary>Try to open the port on the router via UPnP (default true).</summary>
        public bool? Upnp;
        /// <summary>Private ticket: the ticket does not contain the server's IP addresses (default false).</summary>
        public bool? Private;
        /// <summary>Project id from the Peerova website ("prj_..."). Needed, with <see cref="Share"/>, to get a connect code.</summary>
        public string App;
        /// <summary>Listing settings for the connect code. Needs <see cref="App"/>.</summary>
        public ShareConfig Share;
        /// <summary>Advanced; leave unset.</summary>
        [EditorBrowsable(EditorBrowsableState.Never)] public string[] Supernodes;

        /// <summary>Serializes the set fields to the JSON accepted by <see cref="Server.Start(string)"/>.</summary>
        public string ToJson()
        {
            var w = new JsonObjectWriter();
            w.Int("port", Port);
            w.Str("key_file", KeyFile);
            w.Str("license", License);
            w.Str("license_server", LicenseServer);
            w.Str("token", Token);
            w.Int("max_ccu", MaxCcu);
            w.Bool("upnp", Upnp);
            w.Bool("private", Private);
            w.Str("app", App);
            if (Share != null) w.Raw("share", Share.ToJson());
            if (Supernodes != null)
            {
                var sb = new StringBuilder("[");
                for (int i = 0; i < Supernodes.Length; i++) { if (i > 0) sb.Append(','); sb.Append(QuoteJson(Supernodes[i])); }
                w.Raw("supernodes", sb.Append(']').ToString());
            }
            return w.ToString();
        }

        /// <summary>Returns <paramref name="s"/> as a JSON string literal, including the quotes.</summary>
        public static string QuoteJson(string s)
        {
            var sb = new StringBuilder("\"");
            foreach (var c in s)
            {
                switch (c)
                {
                    case '"': sb.Append("\\\""); break;
                    case '\\': sb.Append("\\\\"); break;
                    case '\n': sb.Append("\\n"); break;
                    case '\r': sb.Append("\\r"); break;
                    case '\t': sb.Append("\\t"); break;
                    default:
                        if (c < 0x20) sb.Append("\\u").Append(((int)c).ToString("x4")); else sb.Append(c);
                        break;
                }
            }
            return sb.Append('"').ToString();
        }
    }

    /// <summary>Typed builder for the client JSON config. Only fields that are set are emitted.</summary>
    public sealed class ClientConfig
    {
        /// <summary>Shared secret required by the server (<see cref="ServerConfig.Token"/>).</summary>
        public string Token;
        /// <summary>Give up and report <see cref="EventType.ConnectFailed"/> after this many milliseconds (default 20000).</summary>
        public int? TimeoutMs;

        /// <summary>Serializes the set fields to the JSON accepted by <see cref="Client.Connect(string, string)"/>.</summary>
        public string ToJson()
        {
            var w = new JsonObjectWriter();
            w.Str("token", Token);
            w.Int("timeout_ms", TimeoutMs);
            return w.ToString();
        }
    }

    /// <summary>Minimal JSON object writer used by the config builders.</summary>
    internal sealed class JsonObjectWriter
    {
        readonly StringBuilder _sb = new StringBuilder("{");

        public void Raw(string key, string json)
        {
            if (json == null) return;
            if (_sb.Length > 1) _sb.Append(',');
            _sb.Append('"').Append(key).Append("\":").Append(json);
        }

        public void Str(string key, string v) { if (v != null) Raw(key, ServerConfig.QuoteJson(v)); }
        public void Int(string key, int? v) { if (v.HasValue) Raw(key, v.Value.ToString(System.Globalization.CultureInfo.InvariantCulture)); }
        public void Bool(string key, bool? v) { if (v.HasValue) Raw(key, v.Value ? "true" : "false"); }

        public override string ToString() => _sb.ToString() + "}";
    }

    /// <summary>State of a <see cref="Tunnel"/>.</summary>
    public enum TunnelState
    {
        /// <summary>Connecting (client) / starting (server).</summary>
        Connecting = 0,
        /// <summary>Server: ticket ready. Client: connected and local ports listening.</summary>
        Up = 1,
        /// <summary>Final: failed, disconnected or stopped (see <see cref="Tunnel.Error"/>).</summary>
        Down = 2,
    }

    /// <summary>
    /// In-process port tunnel (C ABI <c>peerova_tunnel_*</c>, = CLI <c>server --expose</c> / <c>client --forward</c>) for
    /// apps that already speak TCP: the client gets 127.0.0.1 ports whose TCP connections are tunnelled, each as an
    /// independent reliable stream, to services exposed by the server.
    /// <code>
    /// using var t = Peerova.Tunnel.Client(ticket, "{\"forward\":[\"tcp:0=19129\"]}");
    /// if (t.Wait(20000) == Peerova.TunnelState.Up) new System.Net.Sockets.TcpClient("127.0.0.1", t.LocalPort(0));
    /// </code>
    /// </summary>
    public sealed class Tunnel : IDisposable
    {
        private static class N
        {
            [DllImport(Native.Lib, CallingConvention = CallingConvention.Cdecl)] internal static extern IntPtr peerova_tunnel_server(byte[] cfg);
            [DllImport(Native.Lib, CallingConvention = CallingConvention.Cdecl)] internal static extern IntPtr peerova_tunnel_client(byte[] ticket, byte[] cfg);
            [DllImport(Native.Lib, CallingConvention = CallingConvention.Cdecl)] internal static extern int peerova_tunnel_state(IntPtr t);
            [DllImport(Native.Lib, CallingConvention = CallingConvention.Cdecl)] internal static extern int peerova_tunnel_wait(IntPtr t, uint ms);
            [DllImport(Native.Lib, CallingConvention = CallingConvention.Cdecl)] internal static extern int peerova_tunnel_local_port(IntPtr t, int index);
            [DllImport(Native.Lib, CallingConvention = CallingConvention.Cdecl)] internal static extern int peerova_tunnel_ticket(IntPtr t, byte[] buf, int len);
            [DllImport(Native.Lib, CallingConvention = CallingConvention.Cdecl)] internal static extern int peerova_tunnel_error(IntPtr t, byte[] buf, int len);
            [DllImport(Native.Lib, CallingConvention = CallingConvention.Cdecl)] internal static extern int peerova_tunnel_next_log(IntPtr t, byte[] buf, int len);
            [DllImport(Native.Lib, CallingConvention = CallingConvention.Cdecl)] internal static extern int peerova_tunnel_peer_of(IntPtr t, ushort port, uint waitMs, byte[] buf, int len);
            [DllImport(Native.Lib, CallingConvention = CallingConvention.Cdecl)] internal static extern uint peerova_tunnel_peer_count(IntPtr t);
            [DllImport(Native.Lib, CallingConvention = CallingConvention.Cdecl)] internal static extern void peerova_tunnel_free(IntPtr t);
            [DllImport(Native.Lib, CallingConvention = CallingConvention.Cdecl)] internal static extern IntPtr peerova_last_error();
        }

        private IntPtr h;
        private readonly object gate = new object();

        static Tunnel() { Native.EnsureResolver(); }

        private Tunnel(IntPtr handle)
        {
            if (handle == IntPtr.Zero) throw new PeerovaException(LastError());
            h = handle;
        }

        private static byte[] Z(string s) => s == null ? null : Encoding.UTF8.GetBytes(s + "\0");

        private static string LastError()
        {
            IntPtr p = N.peerova_last_error();
            if (p == IntPtr.Zero) return "unknown peerova error";
            int n = 0;
            while (Marshal.ReadByte(p, n) != 0) n++;
            byte[] b = new byte[n];
            Marshal.Copy(p, b, 0, n);
            return n == 0 ? "unknown peerova error" : Encoding.UTF8.GetString(b);
        }

        private delegate int StrFn(IntPtr t, byte[] buf, int len);

        private string Str(StrFn f)
        {
            lock (gate)
            {
                if (h == IntPtr.Zero) return null;
                int need = f(h, null, 0);
                if (need <= 0) return null;
                byte[] b = new byte[need];
                if (f(h, b, need) <= 0) return null;
                return Encoding.UTF8.GetString(b, 0, need - 1);
            }
        }

        /// <summary>Starts a tunnel server (config = <see cref="Server"/> JSON + <c>"expose":["tcp:PORT",...]</c>); blocks until the ticket is ready.</summary>
        public static Tunnel StartServer(string configJson) => new Tunnel(N.peerova_tunnel_server(Z(configJson)));

        /// <summary>Starts a tunnel client (config: <c>"forward":["tcp:LOCAL=REMOTE"]</c>, LOCAL 0 = free port); returns immediately.</summary>
        public static Tunnel Client(string ticket, string configJson)
        {
            if (string.IsNullOrWhiteSpace(ticket)) throw new ArgumentException("ticket is empty", nameof(ticket));
            return new Tunnel(N.peerova_tunnel_client(Z(ticket.Trim()), Z(configJson)));
        }

        /// <summary>Current state.</summary>
        public TunnelState State { get { lock (gate) return h == IntPtr.Zero ? TunnelState.Down : (TunnelState)N.peerova_tunnel_state(h); } }

        /// <summary>Waits up to <paramref name="timeoutMs"/> while connecting; returns the state. Do not call from the Unity main thread.</summary>
        public TunnelState Wait(int timeoutMs)
        {
            IntPtr x;
            lock (gate) x = h;
            return x == IntPtr.Zero ? TunnelState.Down : (TunnelState)N.peerova_tunnel_wait(x, (uint)Math.Max(0, timeoutMs));
        }

        /// <summary>Client: local 127.0.0.1 port of forward rule #<paramref name="index"/>.</summary>
        public int LocalPort(int index) { lock (gate) return h == IntPtr.Zero ? 0 : N.peerova_tunnel_local_port(h, index); }

        /// <summary>Server: the ticket.</summary>
        public string Ticket => Str(N.peerova_tunnel_ticket);

        /// <summary>Last error / disconnect reason, or null.</summary>
        public string Error => Str(N.peerova_tunnel_error);

        /// <summary>Oldest pending log line, or null.</summary>
        public string NextLog() => Str(N.peerova_tunnel_next_log);

        /// <summary>Server: "ip:port" of the peer behind the loopback socket with this source port (null if unknown / relayed).</summary>
        public string PeerOf(int srcPort, int waitMs) => Str((t, b, n) => N.peerova_tunnel_peer_of(t, (ushort)srcPort, (uint)Math.Max(0, waitMs), b, n));

        /// <summary>Server: connected peers.</summary>
        public uint PeerCount { get { lock (gate) return h == IntPtr.Zero ? 0 : N.peerova_tunnel_peer_count(h); } }

        /// <summary>Stops the tunnel and frees the native object.</summary>
        public void Dispose()
        {
            IntPtr x;
            lock (gate) { x = h; h = IntPtr.Zero; }
            if (x != IntPtr.Zero) N.peerova_tunnel_free(x);
            GC.SuppressFinalize(this);
        }

        /// <summary>Frees the native object if <see cref="Dispose"/> was not called.</summary>
        ~Tunnel() { Dispose(); }
    }
}
