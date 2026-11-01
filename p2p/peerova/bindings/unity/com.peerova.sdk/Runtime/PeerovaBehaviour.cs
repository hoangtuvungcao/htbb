// Unity component: owns one Peerova server or client, polls it in Update() and raises UnityEvents
// on the main thread. Add it to a GameObject, then call Host() or Join(ticketOrCode).
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using UnityEngine;
using UnityEngine.Events;

namespace Peerova.Unity
{
    /// <summary>UnityEvent with a peer id.</summary>
    [Serializable] public class PeerEvent : UnityEvent<uint> { }
    /// <summary>UnityEvent with (peer, channel, data, reliable).</summary>
    [Serializable] public class MessageEvent : UnityEvent<uint, byte, byte[], bool> { }
    /// <summary>UnityEvent with a string (ticket, connect code or error reason).</summary>
    [Serializable] public class TextEvent : UnityEvent<string> { }

    /// <summary>
    /// One Peerova node (server or client) driven by Unity's player loop. All events are raised on the main
    /// thread from <c>Update()</c>; all methods must be called from the main thread.
    /// </summary>
    /// <remarks>
    /// The node is shut down automatically in <c>OnDestroy</c> / <c>OnApplicationQuit</c>.
    /// Use channel 0..255; send frequently-updated state (positions, inputs) with <c>reliable: false</c>
    /// (max <see cref="Library.MaxUnreliablePayload"/> bytes) and everything that must arrive (chat, RPC) reliably.
    /// </remarks>
    [AddComponentMenu("Networking/Peerova Node")]
    public class PeerovaBehaviour : MonoBehaviour
    {
        /// <summary>What this component is currently doing.</summary>
        public enum Role
        {
            /// <summary>Idle.</summary>
            None,
            /// <summary>Running (or starting) a server.</summary>
            Host,
            /// <summary>Connected (or connecting) to a server as a client.</summary>
            Join,
        }

        [Header("Server (Host)")]
        [Tooltip("UDP port. 0 = random free port; the server default without this component is 47000.")]
        public int port = 0;
        [Tooltip("License key (PVA-...). Empty = dev mode (max 3 clients).")]
        public string license = "";
        [Tooltip("Try to open the port on the router via UPnP.")]
        public bool upnp = true;
        [Tooltip("Optional shared secret clients must present.")]
        public string token = "";
        [Tooltip("Max concurrent clients (0 = license limit).")]
        public int maxCcu = 0;
        [Tooltip("Server identity key, relative to Application.persistentDataPath. Same key = same ticket. Keep it secret.")]
        public string keyFile = "peerova-server.key";

        [Header("Connect code (optional)")]
        [Tooltip("Project id from the Peerova website (prj_...). Set it to get a 6-character connect code.")]
        public string app = "";
        [Tooltip("Name shown on the Peerova website.")]
        public string shareName = "";
        [Tooltip("List the server publicly (otherwise only people with the code can join).")]
        public bool sharePublic = false;
        [Tooltip("Max players advertised (0 = not set).")]
        public int shareMax = 0;
        [Tooltip("Free-form mode label.")]
        public string shareMode = "";
        [Tooltip("Region label, e.g. VN.")]
        public string shareRegion = "";

        [Header("Client (Join)")]
        [Tooltip("Give up connecting after this many milliseconds (0 = default 20000).")]
        public int connectTimeoutMs = 0;

        [Header("Polling")]
        [Tooltip("Max events dispatched per frame (0 = unlimited).")]
        public int maxEventsPerFrame = 256;
        [Tooltip("Keep this GameObject (and the connection) across scene loads.")]
        public bool dontDestroyOnLoad = false;

        /// <summary>Host: the server started; argument = ticket to give to clients.</summary>
        [Header("Events (main thread)")]
        public TextEvent OnHostStarted = new TextEvent();
        /// <summary>Host: the 6-character connect code became available (only when <see cref="app"/> is set).</summary>
        public TextEvent OnRoomCode = new TextEvent();
        /// <summary>A peer connected (host: a client; client: the server).</summary>
        public PeerEvent OnConnected = new PeerEvent();
        /// <summary>A message arrived: (peer, channel, data, reliable). <c>data</c> is a copy you may keep.</summary>
        public MessageEvent OnMessage = new MessageEvent();
        /// <summary>A peer disconnected.</summary>
        public PeerEvent OnDisconnected = new PeerEvent();
        /// <summary>Client: connecting failed. Host: the server could not start. Argument = reason.</summary>
        public TextEvent OnConnectFailed = new TextEvent();

        /// <summary>Raw C# event with the full struct, raised (main thread) after the matching UnityEvent.</summary>
        public event Action<Peerova.Event> EventReceived;

        /// <summary>Current role.</summary>
        public Role CurrentRole { get; private set; }
        /// <summary>Host: the server ticket once started, else null.</summary>
        public string Ticket { get; private set; }
        /// <summary>Host: the 6-character connect code once assigned, else null.</summary>
        public string RoomCode { get; private set; }
        /// <summary>True while a server is running or a client node exists (also while still connecting).</summary>
        public bool IsRunning => _node != null;
        /// <summary>Peers currently connected (host: clients; client: the server).</summary>
        public IReadOnlyCollection<uint> Peers => _peers;

        Endpoint _node;
        Task<Server> _starting;
        int _codeCheckCountdown;
        readonly HashSet<uint> _peers = new HashSet<uint>();

        void Awake()
        {
            if (dontDestroyOnLoad) DontDestroyOnLoad(gameObject);
        }

        /// <summary>
        /// Starts a server with the inspector settings (stops anything running first). Starting blocks up to
        /// ~3 s, so it runs on a worker thread; <see cref="OnHostStarted"/> or <see cref="OnConnectFailed"/> fires later.
        /// </summary>
        public void Host()
        {
            Shutdown();
            var cfg = new ServerConfig
            {
                Port = port,
                Upnp = upnp,
                License = string.IsNullOrEmpty(license) ? null : license,
                Token = string.IsNullOrEmpty(token) ? null : token,
                MaxCcu = maxCcu > 0 ? maxCcu : (int?)null,
                KeyFile = string.IsNullOrEmpty(keyFile) ? null : Path.Combine(Application.persistentDataPath, keyFile),
            };
            if (!string.IsNullOrEmpty(app))
            {
                cfg.App = app;
                cfg.Share = new ShareConfig
                {
                    Name = string.IsNullOrEmpty(shareName) ? null : shareName,
                    Public = sharePublic,
                    Max = shareMax > 0 ? shareMax : (int?)null,
                    Mode = string.IsNullOrEmpty(shareMode) ? null : shareMode,
                    Region = string.IsNullOrEmpty(shareRegion) ? null : shareRegion,
                };
            }
            CurrentRole = Role.Host;
            string json = cfg.ToJson();
            _starting = Task.Run(() => Server.Start(json));
        }

        /// <summary>Connects to a server (stops anything running first). <see cref="OnConnected"/> or <see cref="OnConnectFailed"/> fires later.</summary>
        /// <param name="ticket">The server's ticket, or its 6-character connect code.</param>
        /// <param name="clientToken">Shared secret if the server requires one, else null.</param>
        /// <exception cref="PeerovaException">The ticket/code is malformed.</exception>
        /// <exception cref="ArgumentException">The ticket is empty.</exception>
        public void Join(string ticket, string clientToken = null)
        {
            Shutdown();
            var cfg = new ClientConfig
            {
                Token = string.IsNullOrEmpty(clientToken) ? null : clientToken,
                TimeoutMs = connectTimeoutMs > 0 ? connectTimeoutMs : (int?)null,
            };
            _node = Client.Connect(ticket, cfg);
            CurrentRole = Role.Join;
        }

        /// <summary>Sends bytes to a peer. Returns false if not running or the send failed (see <see cref="Endpoint.LastError"/>).</summary>
        public bool Send(uint peer, byte[] data, byte channel = 0, bool reliable = true) =>
            _node != null && _node.TrySend(peer, data, channel, reliable);

        /// <summary>Allocation-free overload of <see cref="Send(uint, byte[], byte, bool)"/>.</summary>
        public bool Send(uint peer, ReadOnlySpan<byte> data, byte channel = 0, bool reliable = true) =>
            _node != null && _node.TrySend(peer, data, channel, reliable);

        /// <summary>Sends to every connected peer (host: all clients; client: the server).</summary>
        public void Broadcast(byte[] data, byte channel = 0, bool reliable = true)
        {
            if (_node == null) return;
            foreach (var p in _peers) _node.TrySend(p, data, channel, reliable);
        }

        /// <summary>Closes the connection to one peer; <see cref="OnDisconnected"/> follows.</summary>
        public void Disconnect(uint peer) => _node?.Disconnect(peer);

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
            Ticket = null;
            RoomCode = null;
            CurrentRole = Role.None;
        }

        void Update()
        {
            if (_starting != null && _starting.IsCompleted)
            {
                var t = _starting; _starting = null;
                if (t.Status == TaskStatus.RanToCompletion)
                {
                    _node = t.Result;
                    Ticket = t.Result.Ticket;
                    OnHostStarted.Invoke(Ticket);
                }
                else
                {
                    CurrentRole = Role.None;
                    var msg = t.Exception?.GetBaseException().Message ?? "server start cancelled";
                    Debug.LogError("peerova: server start failed: " + msg);
                    OnConnectFailed.Invoke(msg);
                }
            }
            if (_node == null) return;

            // The connect code arrives a little after start; check about twice a second.
            if (RoomCode == null && _node is Server srv && !string.IsNullOrEmpty(app) && --_codeCheckCountdown <= 0)
            {
                _codeCheckCountdown = 30;
                RoomCode = srv.RoomCode;
                if (RoomCode != null) OnRoomCode.Invoke(RoomCode);
            }

            int n = 0;
            while ((maxEventsPerFrame <= 0 || n < maxEventsPerFrame) && _node != null && _node.Poll(out Peerova.Event ev))
            {
                n++;
                switch (ev.Type)
                {
                    case EventType.Connected: _peers.Add(ev.Peer); OnConnected.Invoke(ev.Peer); break;
                    case EventType.Disconnected: _peers.Remove(ev.Peer); OnDisconnected.Invoke(ev.Peer); break;
                    case EventType.Message: OnMessage.Invoke(ev.Peer, ev.Channel, ev.Data, ev.Reliable); break;
                    case EventType.ConnectFailed: OnConnectFailed.Invoke(ev.Text); break;
                }
                EventReceived?.Invoke(ev);
            }
        }

        void OnDestroy() => Shutdown();
        void OnApplicationQuit() => Shutdown();
    }
}
