// peerova — direct P2P networking (no relay, E2E encrypted). C# binding over the C ABI (peerova.h).
//
// Targets: .NET Standard 2.1 (Unity 2021.2+), .NET 6+ (Godot 4 .NET, console, servers).
// No `unsafe` code required.
//
//   using var server = Peerova.Server.Start(new Peerova.ServerConfig { Port = 0 }.ToJson());
//   using var client = Peerova.Client.Connect(server.Ticket);
//   while (client.Poll(out var ev)) { ... }          // non-blocking, call every frame
//   client.Send(ev.Peer, bytes, channel: 0, reliable: true);
//
// Threading: each node has an internal mutex, so Send/Poll may be called from any thread.
// Event.Data is a managed copy (safe to keep). Dispose() frees the native node.
//
// Native library lookup: DllImport("peerova") -> peerova.dll / libpeerova.so / libpeerova.dylib.
// On .NET 6+ an extra resolver also tries Peerova.Native.LibraryPath, the PEEROVA_LIB env var
// and the application base directory. On iOS (Unity, IL2CPP) the static lib is linked: "__Internal".
using System;
using System.Runtime.InteropServices;
using System.Text;

namespace Peerova
{
    public enum EventType
    {
        None = 0,
        Connected = 1,
        Disconnected = 2,
        Message = 3,
        ConnectFailed = 4,
    }

    /// <summary>One network event. <see cref="Data"/> is a managed copy (empty array if no payload).</summary>
    public readonly struct Event
    {
        public readonly EventType Type;
        public readonly uint Peer;
        public readonly byte Channel;
        public readonly bool Reliable;
        public readonly byte[] Data;

        public Event(EventType type, uint peer, byte channel, bool reliable, byte[] data)
        {
            Type = type; Peer = peer; Channel = channel; Reliable = reliable; Data = data ?? Array.Empty<byte>();
        }

        /// <summary>Payload decoded as UTF-8 (CONNECT_FAILED carries a UTF-8 reason).</summary>
        public string Text => Encoding.UTF8.GetString(Data);

        public override string ToString() => $"{Type} peer={Peer} ch={Channel} reliable={Reliable} len={Data.Length}";
    }

    public class PeerovaException : Exception
    {
        public PeerovaException(string message) : base(message) { }
    }

    /// <summary>Owns a native peerova_node*; released with peerova_free.</summary>
    public sealed class NodeHandle : SafeHandle
    {
        public NodeHandle() : base(IntPtr.Zero, true) { }
        public override bool IsInvalid => handle == IntPtr.Zero;
        protected override bool ReleaseHandle()
        {
            Native.peerova_free(handle);
            return true;
        }
    }

    public static class Native
    {
#if (UNITY_IOS || UNITY_TVOS) && !UNITY_EDITOR
        public const string Lib = "__Internal";
#else
        public const string Lib = "peerova";
#endif

        // Mirrors peerova_event (x64/arm64: 32 bytes; 32-bit: 20 bytes — Sequential matches C layout).
        [StructLayout(LayoutKind.Sequential)]
        public struct RawEvent
        {
            public int type;
            public uint peer;
            public byte channel;
            public byte reliable;
            public IntPtr data;
            public uint len;
        }

#if NET6_0_OR_GREATER
        /// <summary>Optional absolute path to the native library. Set before the first peerova call.</summary>
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

        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern NodeHandle peerova_server_start(byte[] configJson);

        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern NodeHandle peerova_client_connect(byte[] ticket, byte[] configJson);

        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern int peerova_ticket(NodeHandle node, byte[] buf, int bufLen);

        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern int peerova_send(NodeHandle node, uint peer, byte channel, ref byte data, uint len, int reliable);

        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern int peerova_poll(NodeHandle node, out RawEvent ev);

        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern int peerova_poll_wait(NodeHandle node, out RawEvent ev, uint timeoutMs);

        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern void peerova_disconnect(NodeHandle node, uint peer);

        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern uint peerova_peer_count(NodeHandle node);

        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern void peerova_free(IntPtr node);

        [DllImport(Lib, CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr peerova_last_error();

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

        internal static string LastError() => FromUtf8Z(peerova_last_error()) ?? "unknown peerova error";
    }

    public static class Library
    {
        /// <summary>Native library version string.</summary>
        public static string Version => Native.FromUtf8Z(Native.peerova_version());

        /// <summary>Max payload for unreliable sends.</summary>
        public const int MaxUnreliablePayload = 1180;
    }

    /// <summary>Common functionality of servers and clients (named Endpoint to avoid clashing with Godot.Node).</summary>
    public abstract class Endpoint : IDisposable
    {
        protected readonly NodeHandle Handle;

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

        public bool IsDisposed => Handle.IsClosed;

        /// <summary>Non-blocking poll. Returns false when no event is pending.</summary>
        public bool Poll(out Event ev) => Poll(out ev, 0);

        /// <summary>Poll, waiting up to <paramref name="timeoutMs"/> ms (0 = don't wait).</summary>
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

        /// <summary>Returns the next event or null (non-blocking unless timeoutMs &gt; 0).</summary>
        public Event? Poll(uint timeoutMs = 0) => Poll(out var ev, timeoutMs) ? ev : (Event?)null;

        /// <summary>Send bytes. reliable=true: ordered + retransmitted per channel. reliable=false: datagram, ≤1180 bytes.</summary>
        public void Send(uint peer, ReadOnlySpan<byte> data, byte channel = 0, bool reliable = true)
        {
            if (!TrySend(peer, data, channel, reliable)) throw new PeerovaException(Native.LastError());
        }

        public void Send(uint peer, string text, byte channel = 0, bool reliable = true) =>
            Send(peer, Encoding.UTF8.GetBytes(text), channel, reliable);

        /// <summary>Like Send but returns false instead of throwing.</summary>
        public bool TrySend(uint peer, ReadOnlySpan<byte> data, byte channel = 0, bool reliable = true)
        {
            byte dummy = 0;
            ref byte first = ref data.Length == 0 ? ref dummy : ref MemoryMarshal.GetReference(data);
            return Native.peerova_send(Handle, peer, channel, ref first, (uint)data.Length, reliable ? 1 : 0) == 0;
        }

        public void Disconnect(uint peer) => Native.peerova_disconnect(Handle, peer);

        public uint PeerCount => Native.peerova_peer_count(Handle);

        public void Dispose()
        {
            Handle.Dispose();
            GC.SuppressFinalize(this);
        }
    }

    public sealed class Server : Endpoint
    {
        public string Ticket { get; }

        Server(NodeHandle h) : base(h)
        {
            int n = Native.peerova_ticket(Handle, null, 0);
            if (n <= 0) { Ticket = ""; return; }
            var buf = new byte[n];
            Native.peerova_ticket(Handle, buf, n);
            Ticket = Encoding.UTF8.GetString(buf, 0, n - 1);
        }

        /// <summary>Start a server. Blocks until the ticket is ready (≤ ~3 s). Throws PeerovaException on error.</summary>
        public static Server Start(string configJson = null) => new Server(Native.peerova_server_start(Native.Utf8Z(configJson)));

        public static Server Start(ServerConfig config) => Start(config?.ToJson());
    }

    public sealed class Client : Endpoint
    {
        Client(NodeHandle h) : base(h) { }

        /// <summary>Connect to a server ticket. Returns immediately; wait for Connected / ConnectFailed via Poll.</summary>
        public static Client Connect(string ticket, string configJson = null)
        {
            if (string.IsNullOrEmpty(ticket)) throw new ArgumentException("ticket is empty", nameof(ticket));
            return new Client(Native.peerova_client_connect(Native.Utf8Z(ticket), Native.Utf8Z(configJson)));
        }
    }

    /// <summary>Convenience builder for the server JSON config (only set fields are emitted).</summary>
    public sealed class ServerConfig
    {
        public int? Port;
        public string KeyFile;
        public string License;
        public string LicenseServer;
        public string Token;
        public int? MaxCcu;
        public bool? Upnp;
        public bool? Private;
        public string[] Supernodes;

        public string ToJson()
        {
            var sb = new StringBuilder("{");
            void Sep() { if (sb.Length > 1) sb.Append(','); }
            void Str(string k, string v) { if (v == null) return; Sep(); sb.Append('"').Append(k).Append("\":").Append(QuoteJson(v)); }
            void Raw(string k, string v) { if (v == null) return; Sep(); sb.Append('"').Append(k).Append("\":").Append(v); }
            Raw("port", Port?.ToString(System.Globalization.CultureInfo.InvariantCulture));
            Str("key_file", KeyFile);
            Str("license", License);
            Str("license_server", LicenseServer);
            Str("token", Token);
            Raw("max_ccu", MaxCcu?.ToString(System.Globalization.CultureInfo.InvariantCulture));
            Raw("upnp", Upnp.HasValue ? (Upnp.Value ? "true" : "false") : null);
            Raw("private", Private.HasValue ? (Private.Value ? "true" : "false") : null);
            if (Supernodes != null)
            {
                Sep(); sb.Append("\"supernodes\":[");
                for (int i = 0; i < Supernodes.Length; i++) { if (i > 0) sb.Append(','); sb.Append(QuoteJson(Supernodes[i])); }
                sb.Append(']');
            }
            return sb.Append('}').ToString();
        }

        /// <summary>JSON string literal (with quotes) for s.</summary>
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
}
