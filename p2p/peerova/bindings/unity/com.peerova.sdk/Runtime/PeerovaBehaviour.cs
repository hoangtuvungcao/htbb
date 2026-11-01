// Unity component: owns one peerova server or client, polls it in Update() and raises UnityEvents
// on the main thread. Add it to a GameObject, then call Host() or Join(ticket).
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using UnityEngine;
using UnityEngine.Events;

namespace Peerova.Unity
{
    [Serializable] public class PeerEvent : UnityEvent<uint> { }
    /// <summary>(peer, channel, data, reliable)</summary>
    [Serializable] public class MessageEvent : UnityEvent<uint, byte, byte[], bool> { }
    [Serializable] public class TextEvent : UnityEvent<string> { }

    [AddComponentMenu("Networking/peerova Node")]
    public class PeerovaBehaviour : MonoBehaviour
    {
        public enum Role { None, Host, Join }

        [Header("Server (Host)")]
        [Tooltip("UDP port, 0 = random")] public int port = 0;
        [Tooltip("License key; empty = dev mode (3 clients)")] public string license = "";
        public bool upnp = true;
        [Tooltip("Optional shared secret clients must present")] public string token = "";
        [Tooltip("Relative to Application.persistentDataPath. Keeps the ticket stable across runs.")]
        public string keyFile = "peerova-server.key";

        [Header("Polling")]
        [Tooltip("Max events dispatched per frame (0 = unlimited)")] public int maxEventsPerFrame = 256;
        public bool dontDestroyOnLoad = false;

        [Header("Events (main thread)")]
        public TextEvent OnHostStarted = new TextEvent();      // ticket
        public PeerEvent OnConnected = new PeerEvent();
        public MessageEvent OnMessage = new MessageEvent();
        public PeerEvent OnDisconnected = new PeerEvent();
        public TextEvent OnConnectFailed = new TextEvent();    // reason (also used for host start errors)

        /// <summary>Raw C# event with the full struct, raised alongside the UnityEvents.</summary>
        public event Action<Peerova.Event> EventReceived;

        public Role CurrentRole { get; private set; }
        public string Ticket { get; private set; }
        public bool IsRunning => _node != null;
        public IReadOnlyCollection<uint> Peers => _peers;

        Endpoint _node;
        Task<Server> _starting;
        readonly HashSet<uint> _peers = new HashSet<uint>();

        void Awake()
        {
            if (dontDestroyOnLoad) DontDestroyOnLoad(gameObject);
        }

        /// <summary>Start a server. peerova_server_start blocks up to ~3 s, so it runs on a worker thread;
        /// OnHostStarted fires (main thread) when the ticket is ready.</summary>
        public void Host()
        {
            Shutdown();
            var cfg = new ServerConfig
            {
                Port = port,
                Upnp = upnp,
                License = string.IsNullOrEmpty(license) ? null : license,
                Token = string.IsNullOrEmpty(token) ? null : token,
                KeyFile = string.IsNullOrEmpty(keyFile) ? null : Path.Combine(Application.persistentDataPath, keyFile),
            };
            CurrentRole = Role.Host;
            string json = cfg.ToJson();
            _starting = Task.Run(() => Server.Start(json));
        }

        /// <summary>Connect to a host ticket. OnConnected / OnConnectFailed fire later.</summary>
        public void Join(string ticket, string clientToken = null)
        {
            Shutdown();
            string cfg = string.IsNullOrEmpty(clientToken) ? null : "{\"token\":" + ServerConfig.QuoteJson(clientToken) + "}";
            _node = Client.Connect(ticket.Trim(), cfg);
            CurrentRole = Role.Join;
        }

        public bool Send(uint peer, byte[] data, byte channel = 0, bool reliable = true) =>
            _node != null && _node.TrySend(peer, data, channel, reliable);

        public bool Send(uint peer, ReadOnlySpan<byte> data, byte channel = 0, bool reliable = true) =>
            _node != null && _node.TrySend(peer, data, channel, reliable);

        /// <summary>Send to every connected peer (host: all clients; client: the server).</summary>
        public void Broadcast(byte[] data, byte channel = 0, bool reliable = true)
        {
            if (_node == null) return;
            foreach (var p in _peers) _node.TrySend(p, data, channel, reliable);
        }

        public void Disconnect(uint peer) => _node?.Disconnect(peer);

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
