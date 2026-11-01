//! I/O engine: sockets + NAT traversal + handshake + reliable connections.
//!
//! Engine threads ("shards") own all connection state; UDP receive threads and the
//! application talk to them through channels. Applications (tunnel, SDK, FFI) send
//! `Cmd`s via a cloneable `Handle` and receive `EngineEvent`s.
//!
//! Multi-core server: a server runs K shards (`ServerConfig::workers`, default one per
//! CPU core). They share one `Node` (sockets, TCP/relay lanes). Each shard owns the
//! connections whose session index ≡ shard (mod K): data packets are routed by the
//! index in their header, handshake inits by a hash of the packet (retransmits over
//! any path reach the same shard and its caches), control packets to shard 0. Shard 0
//! is the primary: it alone does server housekeeping (ticket, rendezvous, supernodes,
//! punching, standby relays, UPnP). CCU, the peer index and flood-guard decisions are
//! engine-wide. Clients always run a single shard.
//!
//! Security model (server):
//! * Dark: nothing is ever sent in reply to a packet without a valid mac1
//!   (requires the server public key from a ticket). Port scans see nothing.
//! * Authenticated: Noise IK — the client proves its static key, the server
//!   proves possession of the ticket's key (no MITM).
//! * Authorized: optional access token / client key allow-list checked inside
//!   the encrypted handshake payload, before any state is allocated.
//! * Replay-safe: handshake timestamps per client key + per-packet replay window.
//! * Rate limited: per-IP handshake budget; CCU limit from the license lease.

use std::collections::{HashMap, VecDeque};
use std::net::{IpAddr, SocketAddr};
use std::hash::{Hash, Hasher};
use std::sync::atomic::{AtomicU32, AtomicUsize, Ordering};
use std::sync::mpsc::{self, Receiver, RecvTimeoutError, Sender};
use std::sync::{Arc, Mutex};
use std::thread::{self, JoinHandle};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

use crate::conn::{Conn, ConnEvent};
use crate::crypto::{self, Initiator, Keypair, Session, T_DATA, T_INIT, T_PUNCH, T_REJECT, T_RESP};
use crate::nat::{self, Mapping};
use crate::pathcache::{self, Nat};
use crate::netinfo::{self, V4Class};
use crate::node::{Node, Via, SPRAY_SOCKETS};
use crate::ticket::Ticket;
use crate::util::random_u64;
use crate::wire::{CandKind, Candidate, Packet, MAGIC};

pub const DEFAULT_PORT: u16 = 47000;
pub const DEV_MODE_CCU: usize = 3;
const PROBE_INTERVAL: Duration = Duration::from_millis(200);
const PUNCH_FOR: Duration = Duration::from_secs(15);
const REGISTER_EVERY: Duration = Duration::from_secs(15);
/// Per source IP. Generous because many mobile users share one CGNAT address;
/// retransmissions of already-answered handshakes are served from cache first.
const HANDSHAKES_PER_IP_PER_SEC: u32 = 200;
/// Invalid packets per second from one IP before it is blocked.
const BAD_PPS_PER_IP: u32 = 500;
const BLOCK_FOR: Duration = Duration::from_secs(60);
/// Authenticated packets per second per peer considered abusive (sustained 3 s).
/// ~50k pps ≈ 500 Mbit/s of small packets — far above any game/app, below a flood.
const PEER_MAX_PPS: u32 = 50_000;
const BAN_FOR: Duration = Duration::from_secs(600);
const OVERFLOW_LIMIT: usize = 64 << 20;
/// Packets one shard may have waiting; beyond this the engine is overloaded and new
/// packets are dropped (like a full NIC ring) instead of queueing seconds of latency
/// and unbounded memory. The protocol treats it as loss.
const MAX_QUEUED: usize = 8192;
/// UDP receive threads per socket for a multi-shard server (the kernel hands each
/// datagram to one waiting thread; more than a few only adds contention).
const MAX_RX_THREADS: usize = 2;
/// Kernel UDP buffers for servers: the default (~200 KB ≈ 300 packets) overflows in
/// milliseconds at 100k packets/s.
const SERVER_SOCKET_BUFFER: usize = 8 << 20;
/// Upper bound for the automatic worker count (`ServerConfig::workers` = 0).
pub const MAX_AUTO_WORKERS: usize = 16;

/// Refusal replies (T_REJECT) per source IP per second and shard (they're never larger than
/// the init that caused them, so they can't amplify; this only bounds the work).
const REJECT_REPLIES_PER_IP: u32 = 10;
/// Reason codes inside a T_REJECT: code u8 | current u32 | max u32.
const REJECT_FULL: u8 = 1;
const REJECT_DENIED: u8 = 2;
const REJECT_BANNED: u8 = 3;

fn reject_reason(code: u8, cur: usize, max: usize) -> Vec<u8> {
    let mut r = vec![code];
    r.extend_from_slice(&(cur.min(u32::MAX as usize) as u32).to_be_bytes());
    r.extend_from_slice(&(max.min(u32::MAX as usize) as u32).to_be_bytes());
    r
}

/// What the player sees when the server refuses (also classified by the FFI error mapping).
fn reject_text(r: &[u8]) -> String {
    let n = |i: usize| r.get(i..i + 4).map_or(0, |b| u32::from_be_bytes(b.try_into().unwrap()));
    match r.first() {
        Some(&REJECT_FULL) => format!("Máy chủ đã đầy ({}/{}) — thử lại sau", n(1), n(5)),
        Some(&REJECT_BANNED) => "Máy chủ tạm chặn kết nối này (gửi dữ liệu quá dồn dập) — thử lại sau ít phút".into(),
        _ => "Máy chủ từ chối truy cập: token / key không hợp lệ, hết hạn hoặc đã bị thu hồi".into(),
    }
}

/// Technical connection details (NAT punching, relays, supernodes) are logged only with
/// `PEEROVA_DEBUG=1`; the default log stays plain for operators and players.
fn debug_enabled() -> bool {
    static ON: std::sync::OnceLock<bool> = std::sync::OnceLock::new();
    *ON.get_or_init(|| std::env::var("PEEROVA_DEBUG").is_ok_and(|v| !v.is_empty() && v != "0"))
}

/// Worker shards for a server: 0 = one per CPU core (1..=MAX_AUTO_WORKERS).
fn worker_count(n: usize) -> usize {
    match n {
        0 => thread::available_parallelism().map_or(1, |n| n.get()).clamp(1, MAX_AUTO_WORKERS),
        n => n.min(64),
    }
}
const TCP_FALLBACK_AFTER: Duration = Duration::from_millis(3000);
/// Client: direct paths had this long → also open the website relay (last resort).
/// Plans that include the hide-IP mode (all its traffic uses the shared fallback route).
pub const HIDE_IP_PLANS: &[&str] = &["trial", "pro", "studio", "enterprise"];

/// Hide-IP is a paid feature: needs a valid lease on an eligible plan. Builds without a
/// license authority (development/self-hosted) allow it.
fn hide_ip_allowed(cfg: &ServerConfig) -> bool {
    let Some(auth) = cfg.authority else { return true };
    cfg.lease
        .as_deref()
        .and_then(|l| crate::license::verify(l, &auth, None, crate::license::now_unix()).ok())
        .is_some_and(|l| HIDE_IP_PLANS.contains(&l.plan.as_str()))
}


/// Client: open the fallback route in parallel from the start (racing the direct paths);
/// whichever completes the handshake first wins, and relay → direct upgrades later.
const RELAY_AFTER: Duration = Duration::from_millis(0);
/// Supernode registry: an instance that hasn't re-registered for this long is dropped.
const REGISTRY_STALE: Duration = Duration::from_secs(90);
/// Punch budget (anti-amplification): per-IP window, concurrent jobs, targets per job.
const PUNCH_IP_COOLDOWN: Duration = Duration::from_secs(5);
/// Punch jobs toward one IP per window (several players may share one NAT/CGNAT IP).
const PUNCH_PER_IP: u32 = 8;
const MAX_PUNCH_JOBS: usize = 64;
/// Joins verified by the website (real players, e.g. a server launch rush) get a far larger budget.
const MAX_PUNCH_JOBS_VERIFIED: usize = 4096;
const MAX_PUNCH_TARGETS: usize = 16;
/// Housekeeping that scans all peers runs at most this often (thousands of players).
const SWEEP_EVERY: Duration = Duration::from_millis(100);
/// A fallback path with no live lane for this long is failed over to another relay.
const RELAY_FAILOVER_GRACE: Duration = Duration::from_secs(3);
/// Relay → direct upgrade: first probe, fast phase (count × interval), then slow probing.
const UPGRADE_FIRST: Duration = Duration::from_millis(500);
const UPGRADE_FAST_PROBES: u32 = 30;
const UPGRADE_EVERY_FAST: Duration = Duration::from_secs(1);
const UPGRADE_EVERY_SLOW: Duration = Duration::from_secs(20);
const PREDICT_PORTS: u16 = 12;
const BIRTHDAY_AFTER: Duration = Duration::from_millis(1200);
/// Client probes website-rendezvous targets with a low TTL for this long (opens our NAT first).
const RDV_LOW_TTL_FOR: Duration = Duration::from_millis(2500);
/// Client waits this long before probing an address learned from a supernode, so the
/// server's low-TTL punch has opened the server-side NAT first.
const CLIENT_PROBE_DELAY: Duration = Duration::from_millis(300);
/// Low-TTL punching phase: TTL 3 passes up to two NAT layers on our side, then expires
/// inside the ISP network (Internet paths are typically 8–20 hops).
const LOW_TTL_FOR: Duration = Duration::from_millis(1500);
const PUNCH_TTL: u32 = 3;
/// Random-port probes per tick towards a symmetric-NAT peer (~850 pps, ~50 KB/s, at most 15 s).
const SPRAY_PER_TICK: usize = 128;
/// Two-sided birthday for symmetric ↔ symmetric (port-block CGNAT).
const SYM_AFTER: Duration = Duration::from_millis(3000);
/// Half-width of the port window around the peer's observed port (covers 1–2k port blocks).
const SYM_WINDOW: u16 = 1024;
/// Max probes per side: ~4k mappings, below typical CGNAT per-subscriber session limits.
const SYM_MAX_PROBES: usize = 4096;
// Fast connect (website rendezvous): start hard-NAT techniques as soon as they're known to be
// needed instead of after fixed delays.
/// Client: birthday sockets this long after the server's address is known (NAT class unknown);
/// at once when either side is remembered as hard; `BIRTHDAY_AFTER` when both are easy.
const FAST_BIRTHDAY_AFTER: Duration = Duration::from_millis(300);
/// Client + server: two-sided window spray this long after the address is known / the job starts
/// (unknown classes); right at the punch time when both sides are remembered as hard.
const FAST_SYM_AFTER: Duration = Duration::from_millis(1200);
/// Server: birthday sockets for website-verified joins (cheap: the fallback route exists).
const RDV_BIRTHDAY_AFTER: Duration = Duration::from_millis(400);
/// Random-port sprays (both sides) stop this long after they started: symmetric ↔ symmetric
/// pairs keep the fallback route and the low-rate upgrade probes instead.
const SPRAY_FOR: Duration = Duration::from_secs(5);
/// Client: stay at low TTL until this long after the common punch time (server NAT opened by then).
const PUNCH_AT_GRACE: Duration = Duration::from_millis(150);

/// Random port within ±SYM_WINDOW of `center` (clamped to 1024..=65535).
fn window_port(center: u16) -> u16 {
    let lo = center.saturating_sub(SYM_WINDOW).max(1024);
    let hi = center.saturating_add(SYM_WINDOW).max(lo + 1);
    lo + (crypto::random_u32() % (hi - lo) as u32) as u16
}

/// Many symmetric NATs allocate external ports sequentially: probe the next few.
/// Skipped when the peer's NAT is known to preserve ports or to allocate them randomly.
fn predict_ports_for(a: &SocketAddr, nat: Option<Nat>) -> Vec<SocketAddr> {
    if matches!(nat, Some(Nat::Easy | Nat::Hard)) {
        return vec![];
    }
    predict_ports(a)
}

fn predict_ports(a: &SocketAddr) -> Vec<SocketAddr> {
    match a.ip() {
        IpAddr::V4(ip) if netinfo::classify_v4(&ip) == V4Class::Public => {
            (1..=PREDICT_PORTS).filter_map(|d| a.port().checked_add(d)).map(|p| SocketAddr::new(a.ip(), p)).collect()
        }
        _ => vec![],
    }
}

#[derive(Clone, Debug)]
pub enum Access {
    /// Anyone holding the ticket may connect.
    Open,
    /// Client must present this secret (e.g. issued by the app's login backend / SSO).
    Token(Vec<u8>),
    /// Only these client static public keys.
    Keys(Vec<[u8; 32]>),
    /// Enterprise: short-lived Ed25519-signed access tokens with port scopes
    /// (issued by the customer's own SSO/IdP backend), plus a revocation list.
    Signed { authority: [u8; 32], revoked: std::collections::HashSet<String> },
}

pub struct ServerConfig {
    pub port: u16,
    pub key: Keypair,
    pub supernodes: Vec<SocketAddr>,
    /// Also act as STUN + introducer for other servers (not dark on control packets).
    pub supernode_mode: bool,
    pub try_map: bool,
    pub access: Access,
    pub private_ticket: bool,
    pub max_ccu: usize,
    /// Accept direct TCP fallback on the same port (for UDP-blocked clients).
    pub tcp_fallback: bool,
    /// Current lease token (sent to supernodes; enables auto-supernode).
    pub lease: Option<String>,
    /// License authority key: when set, our supernode service only accepts
    /// registrations carrying a valid lease (cracked builds are excluded).
    pub authority: Option<[u8; 32]>,
    /// Become a supernode automatically when licensed and publicly reachable.
    pub auto_supernode: bool,
    /// Website rendezvous base URL (CONTRACT §8): no VPS/supernode needed.
    pub rendezvous: Option<String>,
    /// Last-resort relay (only when punching fails). None = off.
    /// Usually the website; any host running the relay endpoint works (VPS, `peerova relay`).
    pub relay: Option<String>,
    /// Hide the server's IP: no address in tickets, no rendezvous, no punching; clients
    /// reach it only through relays and direct packets are ignored (anti-DDoS).
    pub hide_ip: bool,
    /// Where to keep the website rendezvous binding token across restarts (next to the
    /// server key). Without it a restarted dev-mode server waits up to ~2 min before the
    /// website accepts it again (anti-impersonation binding).
    pub rdv_token_file: Option<std::path::PathBuf>,
    /// Engine worker threads (CPU cores used for crypto + connections). 0 = one per core
    /// (capped at `MAX_AUTO_WORKERS`). Each player is served by one worker.
    pub workers: usize,
    /// Authenticated packets/s per peer considered a flood (tests lower it).
    pub(crate) flood_pps: u32,
}

impl ServerConfig {
    pub fn new(key: Keypair) -> Self {
        ServerConfig {
            port: DEFAULT_PORT,
            key,
            supernodes: vec![],
            supernode_mode: false,
            try_map: true,
            access: Access::Open,
            private_ticket: false,
            max_ccu: DEV_MODE_CCU,
            tcp_fallback: true,
            lease: None,
            authority: None,
            auto_supernode: true,
            rendezvous: None,
            relay: None,
            hide_ip: false,
            rdv_token_file: None,
            workers: 0,
            flood_pps: PEER_MAX_PPS,
        }
    }
}

pub struct ClientConfig {
    pub key: Keypair,
    pub token: Vec<u8>,
    pub timeout: Duration,
    pub extra_supernodes: Vec<SocketAddr>,
    pub try_map: bool,
    /// Skip UDP entirely and go straight to the TCP fallback (UDP-hostile networks).
    pub force_tcp: bool,
    /// Website rendezvous base URL; used when the ticket says the server announces there.
    pub rendezvous: Option<String>,
    /// Last-resort relay URL (default: the rendezvous website). `no_relay` disables it.
    pub relay: Option<String>,
    pub no_relay: bool,
    /// File remembering working paths / NAT classes across runs (`pathcache`). None = memory only.
    pub cache_file: Option<std::path::PathBuf>,
}

impl Default for ClientConfig {
    fn default() -> Self {
        ClientConfig {
            key: Keypair::generate(),
            token: vec![],
            timeout: Duration::from_secs(20),
            extra_supernodes: vec![],
            try_map: false,
            force_tcp: false,
            rendezvous: None,
            relay: None,
            no_relay: false,
            cache_file: None,
        }
    }
}

#[derive(Debug, Clone)]
pub enum EngineEvent {
    /// Server: ticket ready to share. Client: own candidates (for manual join code).
    Ready { ticket: Option<String>, candidates: Vec<Candidate>, session: u64 },
    /// `subject`/`scopes`: identity and allowed ports from a signed access token (None = unrestricted).
    Connected { conn: u32, addr: SocketAddr, path: &'static str, peer_key: [u8; 32], subject: Option<String>, scopes: Option<Vec<String>> },
    Disconnected { conn: u32, reason: String },
    ConnectFailed(String),
    StreamOpened { conn: u32, id: u32 },
    StreamData { conn: u32, id: u32, data: Vec<u8> },
    StreamFin { conn: u32, id: u32 },
    StreamReset { conn: u32, id: u32 },
    StreamAcked { conn: u32, id: u32, n: usize },
    Datagram { conn: u32, data: Vec<u8> },
    Log(String),
}

pub enum Cmd {
    StreamWrite { conn: u32, id: u32, data: Vec<u8> },
    StreamFinish { conn: u32, id: u32 },
    StreamReset { conn: u32, id: u32 },
    StreamConsumed { conn: u32, id: u32, n: usize },
    Datagram { conn: u32, data: Vec<u8> },
    Close { conn: u32 },
    SetMaxCcu(usize),
    SetLease(Option<String>),
    /// Replace the revocation list (token ids or subjects); revoked peers are disconnected.
    SetRevoked(std::collections::HashSet<String>),
    /// Manual rendezvous: punch towards a client's join-code candidates.
    Punch(Vec<Candidate>),
    /// Internal: clients waiting at the website rendezvous (server side).
    RdvClients(Vec<crate::rdv::ClientReq>),
    /// Internal: server address learned from the website rendezvous (client side).
    RdvServer(crate::rdv::Endpoint, crate::rdv::Hints),
    /// Website announced where the relay lives (lets us move it without new builds).
    SetRelay(String),
    /// A fallback lane just opened: send our handshake through it right away.
    RelayUp,
    /// Internal: log line from a helper thread.
    Note(String),
    Shutdown,
}

enum Input {
    Packet(SocketAddr, Vec<u8>, Via),
    Cmd(Cmd),
    /// From another shard: this client reconnected there as connection `.1`. Drop the old
    /// connection, announce it, then let `.2` (that shard) announce the new one.
    Kick(u32, u32, usize),
    /// The old connection is gone: announce (and release the events of) this one.
    KickDone(u32),
    /// Flood-guard decisions are engine-wide.
    Block(IpAddr, Instant),
    Unblock(IpAddr),
    Ban([u8; 32], Instant),
    /// Shard 0: a client from this IP connected on another shard (stop punching to it).
    PeerUp(IpAddr),
}

/// A counter on its own cache line (no false sharing between shards).
#[repr(align(64))]
#[derive(Default)]
struct Padded(AtomicUsize);

impl std::ops::Deref for Padded {
    type Target = AtomicUsize;
    fn deref(&self) -> &AtomicUsize {
        &self.0
    }
}

/// Fans packets and commands out to the shards.
struct Router {
    txs: Vec<Sender<Input>>,
    /// Packets waiting per shard (load shedding, see MAX_QUEUED).
    queued: Vec<Padded>,
}

impl Router {
    fn k(&self) -> usize {
        self.txs.len()
    }

    /// Which shard handles a packet: data by session index, handshake inits by a hash of
    /// the packet itself (every copy, over any path, lands on the same shard), the rest
    /// (control, punches, garbage) on shard 0.
    fn shard_for(data: &[u8], k: usize) -> usize {
        if k <= 1 {
            return 0;
        }
        match data.first() {
            Some(&T_DATA) => Session::packet_index(data).map_or(0, |i| i as usize % k),
            Some(&T_INIT) => {
                let mut h = std::collections::hash_map::DefaultHasher::new();
                data.hash(&mut h);
                (h.finish() % k as u64) as usize
            }
            _ => 0,
        }
    }

    fn packet(&self, from: SocketAddr, data: &[u8], via: Via) {
        let i = Self::shard_for(data, self.k());
        if self.queued[i].fetch_add(1, Ordering::Relaxed) >= MAX_QUEUED {
            self.queued[i].fetch_sub(1, Ordering::Relaxed);
            return; // overloaded: shed load
        }
        if self.txs[i].send(Input::Packet(from, data.to_vec(), via)).is_err() {
            self.queued[i].fetch_sub(1, Ordering::Relaxed);
        }
    }

    fn to(&self, shard: usize, i: Input) -> bool {
        self.txs[shard].send(i).is_ok()
    }

    /// Send to every shard except `me`.
    fn others(&self, me: usize, f: impl Fn() -> Input) {
        for (i, tx) in self.txs.iter().enumerate() {
            if i != me {
                let _ = tx.send(f());
            }
        }
    }

    /// Route an app/helper command: per-connection commands to the owning shard, config
    /// changes to every shard, the rest to the primary. False once the engine is gone.
    fn cmd(&self, c: Cmd) -> bool {
        let k = self.k();
        match c {
            Cmd::StreamWrite { conn, .. }
            | Cmd::StreamFinish { conn, .. }
            | Cmd::StreamReset { conn, .. }
            | Cmd::StreamConsumed { conn, .. }
            | Cmd::Datagram { conn, .. }
            | Cmd::Close { conn } => self.to(conn as usize % k, Input::Cmd(c)),
            Cmd::SetLease(l) => (0..k).fold(true, |ok, i| self.to(i, Input::Cmd(Cmd::SetLease(l.clone()))) && ok),
            Cmd::SetRevoked(l) => (0..k).fold(true, |ok, i| self.to(i, Input::Cmd(Cmd::SetRevoked(l.clone()))) && ok),
            Cmd::Shutdown => (0..k).fold(true, |ok, i| self.to(i, Input::Cmd(Cmd::Shutdown)) && ok),
            c => self.to(0, Input::Cmd(c)),
        }
    }
}

/// Who is connected, engine-wide (all shards). Touched on connect/disconnect/roam only.
#[derive(Default)]
struct PeerIndex {
    /// Peer static key → its connection.
    keys: HashMap<[u8; 32], u32>,
    /// Connected peers per IP (flood guard never blocks these; punch jobs stop).
    ips: HashMap<IpAddr, u32>,
    /// Peers using a birthday socket (those sockets must stay open).
    spray: usize,
}

struct Shared {
    ccu: AtomicUsize,
    max_ccu: AtomicUsize,
    index: Mutex<PeerIndex>,
}

#[derive(Default, Clone, Debug)]
pub struct EngineStats {
    pub ccu: usize,
    pub max_ccu: usize,
    pub connects: HashMap<&'static str, u64>,
    pub failures: u64,
    pub dropped_unauthenticated: u64,
    pub rejected: u64,
    /// Set when this server acts as a supernode (announced to the website).
    pub supernode_addr: Option<String>,
}

#[derive(Clone)]
pub struct Handle {
    router: Arc<Router>,
    next_stream: Arc<AtomicU32>,
    pub stats: Arc<Mutex<EngineStats>>,
    /// Current license lease (shared with helper threads: rendezvous, connect codes).
    pub lease: Arc<Mutex<Option<String>>>,
}

impl Handle {
    pub fn send(&self, c: Cmd) {
        self.router.cmd(c);
    }
    /// New locally-initiated stream id (client odd, server even).
    pub fn alloc_stream(&self) -> u32 {
        self.next_stream.fetch_add(2, Ordering::Relaxed)
    }
}

pub struct Engine {
    pub handle: Handle,
    events: Receiver<EngineEvent>,
    threads: Vec<JoinHandle<()>>,
}

impl Engine {
    pub fn recv_timeout(&self, d: Duration) -> Option<EngineEvent> {
        self.events.recv_timeout(d).ok()
    }
    pub fn try_recv(&self) -> Option<EngineEvent> {
        self.events.try_recv().ok()
    }
    /// Blocking iterator over events until the engine stops.
    pub fn events(&self) -> &Receiver<EngineEvent> {
        &self.events
    }
    /// False once the engine thread has exited (after `Cmd::Shutdown`). Relay helper threads may
    /// keep the event channel open a while longer, so loops over `events()` should also check this.
    pub fn is_running(&self) -> bool {
        self.threads.iter().any(|t| !t.is_finished())
    }

    pub fn start_server(cfg: ServerConfig) -> std::io::Result<Engine> {
        if cfg.hide_ip && !hide_ip_allowed(&cfg) {
            return Err(std::io::Error::new(
                std::io::ErrorKind::PermissionDenied,
                "ẩn IP máy chủ có ở gói Pro trở lên (và khi dùng thử) — nâng cấp tại website",
            ));
        }
        let k = worker_count(cfg.workers);
        let (mut cores, handle, events) = Core::new(Role::Server, cfg.key.clone(), cfg.port, 2, k)?;
        *handle.lease.lock().unwrap() = cfg.lease.clone();
        for (core, _) in cores.iter_mut() {
            core.shared.max_ccu.store(cfg.max_ccu, Ordering::Relaxed);
            core.flood_pps = cfg.flood_pps;
            let primary = core.shard == 0;
            // Secondary shards only accept handshakes and run their peers.
            core.srv = Some(ServerState {
                supernodes: if cfg.hide_ip || !primary { vec![] } else { cfg.supernodes.clone() },
                supernode_mode: cfg.supernode_mode && primary,
                access: Some(cfg.access.clone()),
                rendezvous: if cfg.hide_ip || !primary { None } else { cfg.rendezvous.clone() },
                rdv_token_file: cfg.rdv_token_file.clone(),
                hidden: cfg.hide_ip,
                relay: if primary { cfg.relay.clone() } else { None },
                tcp_fallback: cfg.tcp_fallback,
                lease: cfg.lease.clone(),
                authority: cfg.authority,
                auto_supernode: cfg.auto_supernode && !cfg.hide_ip && primary,
                private_ticket: cfg.private_ticket,
                try_map: cfg.try_map && !cfg.hide_ip && primary,
                ready_sent: !primary,
                ..Default::default()
            });
        }
        Ok(Engine::spawn(cores, handle, events))
    }

    pub fn connect(ticket: &str, cfg: ClientConfig) -> Result<Engine, String> {
        let ticket = Ticket::decode(ticket)?;
        let mut supernodes = ticket.supernodes.clone();
        supernodes.extend(cfg.extra_supernodes.iter().copied());
        if ticket.candidates.is_empty() && supernodes.is_empty() && !ticket.rdv && !ticket.hidden {
            return Err("ticket private nhưng không có supernode nào để hỏi địa chỉ server".into());
        }
        let (mut cores, handle, events) = Core::new(Role::Client, cfg.key.clone(), 0, 1, 1).map_err(|e| e.to_string())?;
        cores[0].0.cli = Some(ClientState {
            ticket: Some(ticket),
            supernodes,
            token: cfg.token,
            timeout: cfg.timeout,
            try_map: cfg.try_map,
            force_tcp: cfg.force_tcp,
            cache_file: cfg.cache_file,
            relay: if cfg.no_relay { None } else { cfg.relay.or(cfg.rendezvous.clone()).or_else(|| Some(crate::license::DEFAULT_RELAY.to_string())) },
            rendezvous: cfg.rendezvous.or_else(|| Some(crate::license::DEFAULT_LICENSE_SERVER.to_string())),
            ..Default::default()
        });
        Ok(Engine::spawn(cores, handle, events))
    }

    fn spawn(cores: Vec<(Core, Receiver<Input>)>, handle: Handle, events: Receiver<EngineEvent>) -> Engine {
        let threads = cores
            .into_iter()
            .map(|(core, rx)| {
                let name = if core.shard == 0 { "peerova-engine".to_string() } else { format!("peerova-shard{}", core.shard) };
                thread::Builder::new().name(name).spawn(move || core.run(rx)).expect("spawn engine")
            })
            .collect();
        Engine { handle, events, threads }
    }

    /// Engine worker threads (1 for clients).
    pub fn workers(&self) -> usize {
        self.handle.router.k()
    }
}

impl Drop for Engine {
    fn drop(&mut self) {
        self.handle.send(Cmd::Shutdown);
        for t in self.threads.drain(..) {
            let _ = t.join();
        }
    }
}

// ------------------------------------------------------------------ internals

#[derive(PartialEq, Eq, Clone, Copy)]
enum Role {
    Server,
    Client,
}

struct Peer {
    /// Authenticated-flood detection: (window start, packets in window, strikes).
    rate: (Instant, u32, u8),
    /// Path goes over the TCP fallback.
    tcp: bool,
    /// Local UDP socket for this peer (0 = main, >0 = winning birthday socket).
    sock: u16,
    /// All TCP connections (lanes) known for this peer.
    tcp_paths: Vec<SocketAddr>,
    /// From the access token: (subject, token id).
    ident: Option<(String, String)>,
    conn: Conn,
    session: Session,
    addr: SocketAddr,
    key: [u8; 32],
    overflow: HashMap<u32, (VecDeque<u8>, bool)>,
    /// Connected through the relay: keep probing direct paths and move off the relay as
    /// soon as one answers ("relay first, P2P after"). (targets, next probe, probes sent)
    upgrade: Option<(Vec<SocketAddr>, Instant, u32)>,
    /// One-shot: also send the next sealed packets over UDP to these addresses (and sockets).
    probe_udp: Vec<(Via, SocketAddr)>,
    /// Fallback path: when it was (re)assigned — failover waits a grace period after this.
    relay_since: Option<Instant>,
    /// Deadline currently in the timer heap for this peer (avoids duplicate entries).
    armed: Option<Instant>,
    /// `Connected` not announced yet (waiting for another shard to drop this client's old
    /// connection, so the app sees Disconnected(old) first); events are held until then.
    hold: Option<Box<EngineEvent>>,
}

struct PunchJob {
    /// Birthday spray: client's public IPv4 (random-port symmetric NAT).
    spray_ip: Option<IpAddr>,
    /// When to also punch from the server's own birthday sockets (server behind symmetric NAT).
    birthday_at: Instant,
    /// Until then, punches use a low TTL (open our NAT without touching the peer's).
    low_ttl_until: Instant,
    /// Two-sided birthday (symmetric ↔ symmetric) starts here.
    sym_at: Instant,
    /// Client's public port as seen by a supernode: centre of the window we spray.
    center_port: Option<u16>,
    sym_sent: usize,
    /// Random-port spray towards `spray_ip` only until then (bounded active punching).
    spray_until: Instant,
    targets: Vec<SocketAddr>,
    until: Instant,
    next: Instant,
}

#[derive(Default)]
struct ServerState {
    rdv_token_file: Option<std::path::PathBuf>,
    rendezvous: Option<String>,
    relay: Option<String>,
    /// Relays we keep a standby room on (every configured/announced relay).
    standby: Vec<String>,
    /// Anti-amplification: when we last started punching toward each IP.
    punch_recent: HashMap<IpAddr, (Instant, u32)>,
    hidden: bool,
    tcp_fallback: bool,
    lease: Option<String>,
    authority: Option<[u8; 32]>,
    auto_supernode: bool,
    supernodes: Vec<SocketAddr>,
    supernode_mode: bool,
    access: Option<Access>,
    private_ticket: bool,
    try_map: bool,
    candidates: Vec<Candidate>,
    mapping: Option<Mapping>,
    map_at: Option<Instant>,
    ready_at: Option<Instant>,
    ready_sent: bool,
    acks: usize,
    /// server id → every instance running that key (multi-server failover), max 8.
    registry: HashMap<[u8; 16], Vec<(SocketAddr, [u8; 32], Instant)>>,
    last_ts: HashMap<[u8; 32], u64>,
    resp_cache: HashMap<[u8; 32], (Vec<u8>, Instant)>,
    /// Hashes of rejected handshakes: identical retransmissions are dropped for free.
    reject_cache: HashMap<[u8; 32], (Instant, Option<Vec<u8>>)>,
    /// Refusal replies sent per source IP (window start, count).
    reject_replies: HashMap<IpAddr, (Instant, u32)>,
    /// Throttle rejection logs per source IP (no log/disk flooding).
    reject_log: HashMap<IpAddr, Instant>,
    rate: HashMap<IpAddr, (Instant, u32)>,
    jobs: Vec<PunchJob>,
    /// Server-side birthday sockets (opened on demand, closed when idle).
    spray: Vec<u16>,
    spray_used: Option<Instant>,
    last_register: Option<Instant>,
}

#[derive(Default)]
struct ClientState {
    ticket: Option<Ticket>,
    supernodes: Vec<SocketAddr>,
    token: Vec<u8>,
    timeout: Duration,
    try_map: bool,
    initiator: Option<Initiator>,
    targets: Vec<SocketAddr>,
    intro_addr: Option<SocketAddr>,
    mine: Vec<Candidate>,
    session: u64,
    deadline: Option<Instant>,
    next_probe: Option<Instant>,
    next_intro: Option<Instant>,
    intro_ok: bool,
    done: bool,
    started: Option<Instant>,
    tcp_started: bool,
    force_tcp: bool,
    sym_sent: usize,
    rendezvous: Option<String>,
    /// Targets learned from the website: probe them with a low TTL first so our NAT
    /// mapping exists before the server's punch, without polluting the server's NAT.
    rdv_targets: Vec<SocketAddr>,
    rdv_low_ttl_until: Option<Instant>,
    spray: Vec<u16>,
    relay: Option<String>,
    relay_started: bool,
    /// Fallback relays tried by this client: (pseudo path, relay base URL).
    relay_paths: Vec<(SocketAddr, String)>,
    relay_room: String,
    relay_cid: String,
    cache_file: Option<std::path::PathBuf>,
    /// Our local network (pathcache key) and what we remember about both NATs.
    net: Option<String>,
    own_nat: Option<Nat>,
    server_nat: Option<Nat>,
    /// When the server's address became known, and the common punch time from the website.
    addr_at: Option<Instant>,
    punch_at: Option<Instant>,
    spray_at: Option<Instant>,
    /// Connected through the fallback route: keep punching (bounded) so the direct path appears.
    relay_conn: Option<u32>,
}


/// Flood protection state (per source IP / per authenticated key).
#[derive(Default)]
struct Guard {
    bad: HashMap<IpAddr, (Instant, u32)>,
    blocked: HashMap<IpAddr, Instant>,
    banned_keys: HashMap<[u8; 32], Instant>,
}

struct Core {
    /// Periodic O(peers) housekeeping runs at most every SWEEP_EVERY.
    next_sweep: Instant,
    next_upgrade_sweep: Instant,
    /// Connection timers as a min-heap (deadline, peer id): waking up and finding due
    /// connections is O(log n) instead of scanning every peer (thousands of players).
    /// Entries may be stale; they're re-checked against the connection when popped.
    timers: std::collections::BinaryHeap<std::cmp::Reverse<(Instant, u32)>>,
    guard: Guard,
    lease_slot: Arc<Mutex<Option<String>>>,
    /// This shard's number (0 = primary) and the routes to all shards.
    shard: usize,
    router: Arc<Router>,
    shared: Arc<Shared>,
    flood_pps: u32,
    role: Role,
    node: Arc<Node>,
    key: Keypair,
    out: Sender<EngineEvent>,
    stats: Arc<Mutex<EngineStats>>,
    peers: HashMap<u32, Peer>,
    /// Peers with queued app commands / new state: flushed on the next tick
    /// (avoids walking every connection on every packet → scales to thousands).
    dirty: std::collections::HashSet<u32>,
    srv: Option<ServerState>,
    cli: Option<ClientState>,
}

fn now_unix_nanos() -> u64 {
    SystemTime::now().duration_since(UNIX_EPOCH).map(|d| d.as_nanos() as u64).unwrap_or(0)
}

fn path_of(addr: &SocketAddr) -> &'static str {
    match addr.ip() {
        IpAddr::V6(_) => "ipv6",
        IpAddr::V4(ip) => match netinfo::classify_v4(&ip) {
            V4Class::Private | V4Class::Other => "lan",
            _ => "internet",
        },
    }
}

fn intro_proof(server_pub: &[u8; 32], session: u64, server_id: &[u8; 16]) -> [u8; 16] {
    let mut d = session.to_be_bytes().to_vec();
    d.extend_from_slice(server_id);
    crypto::mac1(server_pub, &d)
}

impl Core {
    /// `k` shards sharing one node, one event stream and one handle.
    #[allow(clippy::type_complexity)]
    fn new(role: Role, key: Keypair, port: u16, first_stream: u32, k: usize) -> std::io::Result<(Vec<(Core, Receiver<Input>)>, Handle, Receiver<EngineEvent>)> {
        let (txs, rxs): (Vec<Sender<Input>>, Vec<Receiver<Input>>) = (0..k).map(|_| mpsc::channel::<Input>()).unzip();
        let router = Arc::new(Router { txs, queued: (0..k).map(|_| Padded::default()).collect() });
        let (etx, erx) = mpsc::channel::<EngineEvent>();
        let r2 = router.clone();
        let node = Arc::new(Node::bind_with(
            port,
            netinfo::global_ipv6(),
            Arc::new(move |from, data: &[u8], via: Via| r2.packet(from, data, via)),
            k.min(MAX_RX_THREADS),
            (role == Role::Server).then_some(SERVER_SOCKET_BUFFER),
        )?);
        let stats = Arc::new(Mutex::new(EngineStats::default()));
        let shared = Arc::new(Shared { ccu: AtomicUsize::new(0), max_ccu: AtomicUsize::new(DEV_MODE_CCU), index: Mutex::default() });
        let handle = Handle { router: router.clone(), next_stream: Arc::new(AtomicU32::new(first_stream)), stats: stats.clone(), lease: Arc::default() };
        let cores = rxs
            .into_iter()
            .enumerate()
            .map(|(shard, rx)| {
                let core = Core {
                    next_sweep: Instant::now(),
                    next_upgrade_sweep: Instant::now(),
                    timers: Default::default(),
                    guard: Guard::default(),
                    lease_slot: handle.lease.clone(),
                    shard,
                    router: router.clone(),
                    shared: shared.clone(),
                    flood_pps: PEER_MAX_PPS,
                    role,
                    node: node.clone(),
                    key: key.clone(),
                    out: etx.clone(),
                    stats: stats.clone(),
                    peers: HashMap::new(),
                    dirty: Default::default(),
                    srv: None,
                    cli: None,
                };
                (core, rx)
            })
            .collect();
        Ok((cores, handle, erx))
    }

    fn primary(&self) -> bool {
        self.shard == 0
    }

    /// Server: a fresh session index owned by this shard (idx ≡ shard mod K), else random.
    fn alloc_index(&self) -> u32 {
        let k = self.router.k() as u32;
        loop {
            let r = crypto::random_u32();
            let Some(idx) = (r - r % k).checked_add(self.shard as u32) else { continue };
            if idx != 0 && !self.peers.contains_key(&idx) {
                return idx;
            }
        }
    }

    /// Reserve one CCU slot engine-wide (`replacing`: this client's old connection will go).
    fn reserve_ccu(&self, replacing: bool) -> bool {
        let max = self.shared.max_ccu.load(Ordering::Relaxed);
        self.shared
            .ccu
            .fetch_update(Ordering::AcqRel, Ordering::Acquire, |cur| (cur.saturating_sub(replacing as usize) < max).then_some(cur + 1))
            .is_ok()
    }

    /// Insert a connected peer (its CCU slot is already counted).
    fn add_peer(&mut self, idx: u32, p: Peer) {
        {
            let mut ix = self.shared.index.lock().unwrap();
            ix.keys.insert(p.key, idx);
            *ix.ips.entry(p.addr.ip()).or_default() += 1;
            ix.spray += (p.sock > 0) as usize;
        }
        self.peers.insert(idx, p);
    }

    fn remove_peer(&mut self, idx: u32) -> Option<Peer> {
        let p = self.peers.remove(&idx)?;
        let ccu = self.shared.ccu.fetch_sub(1, Ordering::AcqRel).saturating_sub(1);
        {
            let mut ix = self.shared.index.lock().unwrap();
            if ix.keys.get(&p.key) == Some(&idx) {
                ix.keys.remove(&p.key);
            }
            Self::unindex_ip(&mut ix, p.addr.ip());
            ix.spray -= (p.sock > 0) as usize;
        }
        self.stats.lock().unwrap().ccu = ccu;
        Some(p)
    }

    fn unindex_ip(ix: &mut PeerIndex, ip: IpAddr) {
        if let Some(n) = ix.ips.get_mut(&ip) {
            *n -= 1;
            if *n == 0 {
                ix.ips.remove(&ip);
            }
        }
    }

    /// A peer moved (roaming / relay failover): keep the engine-wide index right.
    fn reindex(&self, old: (IpAddr, u16), new: (IpAddr, u16)) {
        if old == new {
            return;
        }
        let mut ix = self.shared.index.lock().unwrap();
        Self::unindex_ip(&mut ix, old.0);
        *ix.ips.entry(new.0).or_default() += 1;
        ix.spray = ix.spray + (new.1 > 0) as usize - (old.1 > 0) as usize;
    }

    fn ip_connected(&self, ip: &IpAddr) -> bool {
        self.shared.index.lock().unwrap().ips.contains_key(ip)
    }

    fn emit(&self, e: EngineEvent) {
        let _ = self.out.send(e);
    }
    fn log(&self, m: String) {
        self.emit(EngineEvent::Log(m));
    }
    /// Technical detail: only with PEEROVA_DEBUG=1.
    fn debug(&self, m: String) {
        if debug_enabled() {
            self.emit(EngineEvent::Log(m));
        }
    }
    /// How the log names the other side of connection `idx`.
    fn who(&self, idx: u32) -> String {
        match self.role {
            Role::Server => format!("người chơi {idx:08x}"),
            Role::Client => "kết nối".into(),
        }
    }

    fn gather(&mut self, try_map: bool) -> (Vec<Candidate>, Option<Mapping>) {
        let mut c = vec![];
        let v4 = netinfo::local_ipv4();
        if let (Some(ip), Some(p)) = (v4, self.node.port_v4()) {
            c.push(Candidate { kind: CandKind::Host, addr: SocketAddr::new(IpAddr::V4(ip), p) });
        }
        if let (Some(ip), Some(p)) = (netinfo::global_ipv6(), self.node.port_v6()) {
            c.push(Candidate { kind: CandKind::Ipv6, addr: SocketAddr::new(IpAddr::V6(ip), p) });
        }
        let mut mapping = None;
        if try_map {
            if let (Some(ip), Some(p)) = (v4, self.node.port_v4()) {
                let mut logs = vec![];
                mapping = nat::map_udp_port(ip, p, &mut |m| logs.push(m));
                for l in logs {
                    self.debug(l);
                }
                if let Some(m) = &mapping {
                    let public = netinfo::classify_v4(m.external.ip()) == V4Class::Public;
                    self.debug(format!(
                        "router mở port {} qua {:?}{}",
                        m.external,
                        m.method,
                        if public { "" } else { " (⚠ IP ngoài không public: double NAT/CGNAT)" }
                    ));
                    c.push(Candidate { kind: CandKind::Mapped, addr: SocketAddr::V4(m.external) });
                }
            }
        }
        (c, mapping)
    }

    fn run(mut self, rx: Receiver<Input>) {
        let now = Instant::now();
        match self.role {
            Role::Server if !self.primary() => {}
            Role::Server => {
                let (try_map, tcp) = { let s = self.srv.as_ref().unwrap(); (s.try_map, s.tcp_fallback) };
                let (cands, mapping) = self.gather(try_map);
                if tcp {
                    let port = self.node.port_v4().or(self.node.port_v6()).unwrap_or(0);
                    match self.node.listen_tcp(port, self.node.port_v6().is_some()) {
                        Ok(_) => {
                            if try_map && mapping.is_some() {
                                if let Some(ip) = netinfo::local_ipv4() {
                                    let _ = nat::map_port(ip, port, true, &mut |_| {});
                                }
                            }
                        }
                        Err(e) => {
                            self.log(format!("không mở được cổng TCP {port} ({e}) — người chơi ở mạng chặn UDP sẽ không vào được; kiểm tra chương trình khác đang dùng cổng này"));
                            self.srv.as_mut().unwrap().tcp_fallback = false;
                        }
                    }
                }
                let reachable = cands.iter().any(|c| match c.addr.ip() {
                    IpAddr::V6(_) => true,
                    IpAddr::V4(ip) => netinfo::classify_v4(&ip) == V4Class::Public,
                });
                let s = self.srv.as_mut().unwrap();
                if !s.supernode_mode && s.auto_supernode && reachable && s.lease.is_some() && s.authority.is_some() {
                    s.supernode_mode = true;
                    let public = cands.iter().find(|c| c.addr.is_ipv4() && matches!(c.addr.ip(), IpAddr::V4(ip) if netinfo::classify_v4(&ip) == V4Class::Public)).or(cands.first());
                    self.stats.lock().unwrap().supernode_addr = public.map(|c| c.addr.to_string());
                    self.debug("server reachable + có license → tự động làm supernode (chỉ phục vụ server có lease hợp lệ)".into());
                }
                if let Some(url) = self.srv.as_ref().unwrap().rendezvous.clone() {
                    self.spawn_rdv_poll(url, &cands);
                }
                let s = self.srv.as_mut().unwrap();
                s.candidates = cands;
                s.mapping = mapping;
                s.map_at = Some(now);
                s.ready_at = Some(now + if s.supernodes.is_empty() { Duration::ZERO } else { Duration::from_millis(1500) });
                self.register(now);
            }
            Role::Client => {
                let try_map = self.cli.as_ref().unwrap().try_map;
                let (mine, _) = self.gather(try_map);
                let node_supports: Vec<SocketAddr>;
                {
                    let c = self.cli.as_ref().unwrap();
                    node_supports = c.ticket.as_ref().unwrap().candidates.iter().map(|c| c.addr).filter(|a| self.node.supports(a)).collect();
                }
                let session = random_u64();
                let c = self.cli.as_mut().unwrap();
                c.targets = node_supports;
                // Reconnect fast: the direct address that worked last time goes first, and what we
                // learned about both NATs picks the punching techniques from t=0.
                let known = pathcache::server(c.cache_file.as_deref(), &c.ticket.as_ref().unwrap().server_pub);
                if let Some(a) = known.and_then(|e| e.addr) {
                    if self.node.supports(&a) && !c.targets.contains(&a) {
                        c.targets.insert(0, a);
                        if c.ticket.as_ref().is_some_and(|t| t.rdv) {
                            // Probably behind the server's NAT: open ours at low TTL until the
                            // server punches (punch time from the website), like rendezvous targets.
                            c.rdv_targets.push(a);
                            c.rdv_low_ttl_until = Some(now + RDV_LOW_TTL_FOR);
                        }
                    }
                }
                c.server_nat = known.and_then(|e| e.nat);
                c.net = pathcache::net_key(netinfo::local_ipv4());
                c.own_nat = c.net.as_deref().and_then(|n| pathcache::own_nat(c.cache_file.as_deref(), n));
                c.mine = mine.clone();
                c.session = session;
                c.deadline = Some(now + c.timeout);
                c.next_probe = Some(now);
                c.next_intro = Some(now);
                c.started = Some(now);
                let ticket = c.ticket.as_ref().unwrap();
                let mut payload = now_unix_nanos().to_be_bytes().to_vec();
                payload.extend_from_slice(&c.token);
                c.initiator = Some(crypto::initiate(&self.key, &ticket.server_pub, crypto::random_u32() | 1, &payload));
                let rdv = c.ticket.as_ref().is_some_and(|t| t.rdv).then(|| c.rendezvous.clone()).flatten();
                if let Some(url) = rdv {
                    let sid = crate::util::b64_encode(&c.ticket.as_ref().unwrap().server_pub);
                    let port = self.node.port_v4().unwrap_or(0);
                    let lan: Vec<SocketAddr> = mine.iter().filter(|c| c.kind == CandKind::Host).map(|c| c.addr).collect();
                    let v6: Vec<SocketAddr> = mine.iter().filter(|c| c.kind == CandKind::Ipv6).map(|c| c.addr).collect();
                    let tx = self.router.clone();
                    let hints = crate::rdv::Hints { punch_at: None, nat: c.own_nat, peer_nat: c.server_nat };
                    thread::spawn(move || {
                        let msg = match crate::rdv::connect(&url, &sid, session, port, &lan, &v6, hints) {
                            Ok((_, ep, h)) => Cmd::RdvServer(ep, h),
                            Err(e) => Cmd::Note(if debug_enabled() { format!("điểm hẹn website: {e}") } else { String::new() }),
                        };
                        tx.cmd(msg);
                    });
                }
                self.emit(EngineEvent::Ready { ticket: None, candidates: mine, session });
                self.log("đang tìm đường kết nối tới máy chủ…".into());
            }
        }

        loop {
            let now = Instant::now();
            let wait = self.next_wake(now).saturating_duration_since(now).min(Duration::from_millis(200));
            match rx.recv_timeout(wait) {
                Ok(i) => {
                    if !self.input(i) {
                        break;
                    }
                    for _ in 0..512 {
                        match rx.try_recv() {
                            Ok(i) => {
                                if !self.input(i) {
                                    return self.shutdown();
                                }
                            }
                            Err(_) => break,
                        }
                    }
                }
                Err(RecvTimeoutError::Timeout) => {}
                Err(RecvTimeoutError::Disconnected) => break,
            }
            self.tick(Instant::now());
        }
        self.shutdown();
    }

    fn shutdown(&mut self) {
        let ids: Vec<u32> = self.peers.keys().copied().collect();
        let now = Instant::now();
        for id in ids {
            if let Some(p) = self.peers.get_mut(&id) {
                p.conn.close();
            }
            self.flush(id, now);
        }
        if let Some(s) = &self.srv {
            if let Some(m) = &s.mapping {
                if m.method == nat::MapMethod::Upnp {
                    if let Some(ip) = netinfo::local_ipv4() {
                        if let Ok(igd) = nat::upnp::discover(ip, Duration::from_millis(1000)) {
                            let _ = igd.delete_udp_mapping(m.external.port());
                        }
                    }
                }
            }
        }
    }

    fn next_wake(&self, now: Instant) -> Instant {
        let mut t = now + Duration::from_millis(200);
        if let Some(std::cmp::Reverse((x, _))) = self.timers.peek() {
            t = t.min(*x);
        }
        if let Some(s) = &self.srv {
            if !s.jobs.is_empty() || !s.ready_sent {
                t = t.min(now + Duration::from_millis(20));
            }
        }
        if let Some(c) = &self.cli {
            if !c.done {
                t = t.min(now + Duration::from_millis(20));
            }
        }
        t
    }

    fn input(&mut self, i: Input) -> bool {
        let now = Instant::now();
        match i {
            Input::Packet(from, data, via) => {
                self.router.queued[self.shard].fetch_sub(1, Ordering::Relaxed);
                self.packet(from, &data, via, now)
            }
            Input::Cmd(Cmd::Shutdown) => return false,
            Input::Cmd(c) => self.cmd(c, now),
            Input::Kick(old, new, from_shard) => {
                if self.remove_peer(old).is_some_and(|p| p.hold.is_none()) {
                    self.emit(EngineEvent::Disconnected { conn: old, reason: "client kết nối lại".into() });
                }
                self.router.to(from_shard, Input::KickDone(new));
            }
            Input::KickDone(id) => {
                if let Some(ev) = self.peers.get_mut(&id).and_then(|p| p.hold.take()) {
                    self.emit(*ev);
                    self.dirty.insert(id);
                }
            }
            Input::Block(ip, until) => {
                self.guard.blocked.insert(ip, until);
            }
            Input::Unblock(ip) => {
                self.guard.blocked.remove(&ip);
            }
            Input::Ban(key, until) => {
                self.guard.banned_keys.insert(key, until);
            }
            Input::PeerUp(ip) => {
                if let Some(s) = self.srv.as_mut() {
                    s.jobs.retain(|j| !j.targets.iter().any(|t| t.ip() == ip));
                }
            }
        }
        true
    }

    fn cmd(&mut self, c: Cmd, now: Instant) {
        if let Cmd::StreamWrite { conn, .. }
        | Cmd::StreamFinish { conn, .. }
        | Cmd::StreamReset { conn, .. }
        | Cmd::StreamConsumed { conn, .. }
        | Cmd::Datagram { conn, .. }
        | Cmd::Close { conn } = &c
        {
            self.dirty.insert(*conn);
        }
        match c {
            Cmd::StreamWrite { conn, id, data } => {
                if let Some(p) = self.peers.get_mut(&conn) {
                    let ov = p.overflow.entry(id).or_default();
                    if ov.0.is_empty() {
                        let n = p.conn.stream_write(id, &data);
                        ov.0.extend(&data[n..]);
                    } else {
                        ov.0.extend(&data);
                    }
                    if ov.0.len() > OVERFLOW_LIMIT {
                        p.conn.stream_reset(id);
                        p.overflow.remove(&id);
                    }
                }
            }
            Cmd::StreamFinish { conn, id } => {
                if let Some(p) = self.peers.get_mut(&conn) {
                    match p.overflow.get_mut(&id) {
                        Some(ov) if !ov.0.is_empty() => ov.1 = true,
                        _ => p.conn.stream_finish(id),
                    }
                }
            }
            Cmd::StreamReset { conn, id } => {
                if let Some(p) = self.peers.get_mut(&conn) {
                    p.overflow.remove(&id);
                    p.conn.stream_reset(id);
                }
            }
            Cmd::StreamConsumed { conn, id, n } => {
                if let Some(p) = self.peers.get_mut(&conn) {
                    p.conn.stream_consumed(id, n);
                }
            }
            Cmd::Datagram { conn, data } => {
                if let Some(p) = self.peers.get_mut(&conn) {
                    p.conn.send_datagram(data);
                }
            }
            Cmd::Close { conn } => {
                if let Some(p) = self.peers.get_mut(&conn) {
                    p.conn.close();
                }
            }
            Cmd::SetRevoked(list) => {
                let kick: Vec<u32> = self
                    .peers
                    .iter()
                    .filter(|(_, p)| p.ident.as_ref().is_some_and(|(sub, jti)| list.contains(sub) || list.contains(jti)))
                    .map(|(&k, _)| k)
                    .collect();
                for id in kick {
                    if let Some(p) = self.peers.get_mut(&id) {
                        p.conn.close();
                    }
                    self.flush(id, now);
                    self.log(format!("đã ngắt người chơi {id:08x}: quyền truy cập bị thu hồi"));
                }
                if let Some(Access::Signed { revoked, .. }) = self.srv.as_mut().and_then(|s| s.access.as_mut()) {
                    *revoked = list;
                }
            }
            Cmd::SetLease(l) => {
                *self.lease_slot.lock().unwrap() = l.clone();
                if let Some(s) = self.srv.as_mut() {
                    s.lease = l;
                }
            }
            Cmd::SetMaxCcu(n) => {
                self.shared.max_ccu.store(n, Ordering::Relaxed);
                self.stats.lock().unwrap().max_ccu = n;
            }
            Cmd::Punch(cands) => self.start_punch(cands.iter().map(|c| c.addr).collect(), now),
            Cmd::Note(m) => {
                if !m.is_empty() {
                    self.log(m)
                }
            }
            Cmd::RdvClients(list) => {
                for r in list {
                    if self.ip_connected(&r.ep.ip) {
                        continue;
                    }
                    let mut t = vec![SocketAddr::new(r.ep.ip, r.ep.port)];
                    // Self-declared addresses are unverified: keep only a couple of each.
                    t.extend(r.ep.lan.iter().take(2).chain(r.ep.v6.iter().take(2)).copied());
                    let spray = matches!(r.ep.ip, IpAddr::V4(v4) if netinfo::classify_v4(&v4) == V4Class::Public).then_some(r.ep.ip);
                    self.debug(format!("điểm hẹn: client {} muốn vào → đục NAT", r.ep.ip));
                    // The client already opened its NAT with low-TTL probes: punch at full TTL now.
                    // Its public IP comes from the website (HTTPS) and is the requester's own.
                    if !self.start_punch_verified(t, spray, false, Some(r.ep.ip), now) {
                        continue;
                    }
                    // Fast connect: a website-verified join (fallback route available, failures are
                    // cheap) gets the hard-NAT techniques early; at once when the client says so.
                    let h = r.hints;
                    let at = h.punch_at.filter(|t| *t > now).unwrap_or(now);
                    let (client_hard, server_hard) = (h.nat == Some(Nat::Hard), h.peer_nat == Some(Nat::Hard));
                    let j = self.srv.as_mut().unwrap().jobs.last_mut().unwrap();
                    j.birthday_at = if client_hard || server_hard { now } else { now + RDV_BIRTHDAY_AFTER };
                    j.sym_at = match (client_hard && server_hard, h.nat) {
                        (true, _) => at,
                        (false, Some(Nat::Easy)) => now + SYM_AFTER,
                        _ => now + FAST_SYM_AFTER,
                    };
                    if h.nat == Some(Nat::Easy) {
                        j.spray_until = now; // port-preserving client: plain punching is enough
                    }
                    if h.nat.is_some() {
                        // Prediction only for sequential NATs (targets[0..] = client addresses + predictions).
                        let keep = predict_ports_for(&SocketAddr::new(r.ep.ip, r.ep.port), h.nat);
                        let base: Vec<SocketAddr> = predict_ports(&SocketAddr::new(r.ep.ip, r.ep.port));
                        j.targets.retain(|t| !base.contains(t) || keep.contains(t));
                    }
                    let m = format!("điểm hẹn: NAT client {:?} / server {:?} → birthday sau {} ms, sym sau {} ms", h.nat, h.peer_nat, j.birthday_at.saturating_duration_since(now).as_millis(), j.sym_at.saturating_duration_since(now).as_millis());
                    self.debug(m);
                }
            }
            Cmd::RelayUp => {
                if let Some(c) = self.cli.as_mut() {
                    if !c.done {
                        c.next_probe = Some(now);
                    }
                }
            }
            Cmd::SetRelay(url) => {
                if let Some(s) = self.srv.as_mut() {
                    if s.relay.is_some() {
                        s.relay = Some(url.clone());
                        // Newly announced relays: join their standby rooms too (old ones stay).
                        for base in crate::relay::list(&url) {
                            self.start_standby(base);
                        }
                    }
                }
            }
            Cmd::RdvServer(ep, hints) => {
                if let Some(c) = self.cli.as_mut() {
                    let mut redial = false;
                    if let (Some(r), true) = (&ep.relay, c.relay.is_some()) {
                        c.relay = Some(r.clone());
                        redial = c.relay_started && !c.done;
                    }
                    let guess = SocketAddr::new(ep.ip, ep.port);
                    let mut t = vec![guess];
                    t.extend(predict_ports_for(&guess, c.server_nat));
                    t.extend(ep.lan.iter().chain(ep.v6.iter()).copied());
                    t.retain(|a| self.node.supports(a));
                    for a in &t {
                        if !c.targets.contains(a) {
                            c.targets.push(*a);
                        }
                    }
                    let cached: Vec<SocketAddr> = c.rdv_targets.iter().filter(|a| !t.contains(a)).copied().collect();
                    c.rdv_targets = t;
                    c.rdv_targets.extend(cached);
                    // Low TTL until the server has punched (common punch time from the website);
                    // older websites send none → the fixed window.
                    c.punch_at = hints.punch_at;
                    c.rdv_low_ttl_until = Some(hints.punch_at.map_or(now + RDV_LOW_TTL_FOR, |t| t.max(now) + PUNCH_AT_GRACE));
                    c.addr_at.get_or_insert(now);
                    c.intro_addr.get_or_insert(guess);
                    c.next_probe = Some(now);
                    self.debug(format!("điểm hẹn website: server ở {guess} → đang đục NAT"));
                    if redial {
                        self.dial_relays(); // the website announced relays we haven't tried yet
                    }
                }
            }
            Cmd::Shutdown => {}
        }
        let _ = now;
    }

    /// Count an invalid packet from `ip`; block the IP for a while above the threshold.
    /// Never blocks an address that currently carries an authenticated peer (anti-framing).
    fn bad_packet(&mut self, ip: IpAddr, now: Instant) {
        self.stats.lock().unwrap().dropped_unauthenticated += 1;
        let e = self.guard.bad.entry(ip).or_insert((now, 0));
        if now.duration_since(e.0) >= Duration::from_secs(1) {
            *e = (now, 0);
        }
        e.1 += 1;
        if e.1 > BAD_PPS_PER_IP && !self.guard.blocked.contains_key(&ip) && !self.ip_connected(&ip) {
            let until = now + BLOCK_FOR;
            self.guard.blocked.insert(ip, until);
            self.router.others(self.shard, || Input::Block(ip, until));
            self.log(format!("chặn {ip} trong {}s: gửi dồn dập gói không hợp lệ (chống tấn công)", BLOCK_FOR.as_secs()));
        }
        if self.guard.bad.len() > 50_000 {
            self.guard.bad.clear();
        }
    }

    fn packet(&mut self, from: SocketAddr, data: &[u8], via: Via, now: Instant) {
        let Some(&t) = data.first() else { return };
        if self.srv.as_ref().is_some_and(|s| s.hidden) && !crate::relay::is_relay(&from) {
            return; // hidden server: only relayed traffic exists
        }
        if let Some(until) = self.guard.blocked.get(&from.ip()) {
            // Handshakes still pass (mac1-checked and rate-limited on their own): a spoofed
            // flood "from" a victim's IP must not lock the real player out.
            if now < *until && t != crypto::T_INIT {
                return; // cheapest possible drop: no parsing, no crypto
            }
            if now >= *until {
                self.guard.blocked.remove(&from.ip());
            }
        }
        let tcp = via == Via::Tcp;
        let sock = match via {
            Via::Udp(s) => s,
            Via::Tcp => 0,
        };
        match (t, self.role) {
            (T_INIT, Role::Server) => self.on_init(from, data, tcp, sock, now),
            (T_RESP, Role::Client) => self.on_resp(from, data, tcp, sock, now),
            (T_DATA, _) => self.on_data(from, data, tcp, sock, now),
            (T_PUNCH, Role::Client) => self.on_punch(from, via),
            (T_REJECT, Role::Client) => self.on_reject(data),
            (T_PUNCH, _) => {}
            (b'P', _) if data.starts_with(&MAGIC) => {
                if let Some(p) = Packet::decode(data) {
                    self.on_control(from, p, now);
                }
            }
            _ => self.bad_packet(from.ip(), now),
        }
    }

    fn rate_ok(&mut self, ip: IpAddr, now: Instant) -> bool {
        // Loopback can't be spoofed from the network (the kernel drops such packets): local
        // sidecars / test harnesses may open many sessions from one address.
        if ip.is_loopback() {
            return true;
        }
        // Each shard sees ~1/K of an IP's handshakes: split the budget.
        let budget = HANDSHAKES_PER_IP_PER_SEC.div_ceil(self.router.k() as u32);
        let s = self.srv.as_mut().unwrap();
        let e = s.rate.entry(ip).or_insert((now, 0));
        if now.duration_since(e.0) >= Duration::from_secs(1) {
            *e = (now, 0);
        }
        e.1 += 1;
        e.1 <= budget
    }

    fn on_init(&mut self, from: SocketAddr, data: &[u8], tcp: bool, sock: u16, now: Instant) {
        let via = if tcp { Via::Tcp } else { Via::Udp(sock) };
        if !crypto::check_init_mac(&self.key.public, data) {
            self.stats.lock().unwrap().dropped_unauthenticated += 1;
            return; // dark: silently ignore
        }
        let h = crypto::hash(&[data]);
        if let Some((_, reply)) = self.srv.as_ref().unwrap().reject_cache.get(&h) {
            // Same rejected handshake retransmitted: no crypto, no log; repeat the answer.
            if let Some(reply) = reply.clone() {
                self.send_refusal(from, via, &reply, now);
            }
            return;
        }
        if let Some((resp, _)) = self.srv.as_ref().unwrap().resp_cache.get(&h) {
            let _ = self.node.send_on(via, from, resp); // retransmitted init over another path
            return;
        }
        // Only NEW handshakes (expensive DH) count against the per-IP budget.
        if !self.rate_ok(from.ip(), now) {
            return;
        }
        let Some(r) = crypto::read_init(&self.key, data) else { return };
        if r.payload.len() < 8 {
            return;
        }
        let ts = u64::from_be_bytes(r.payload[..8].try_into().unwrap());
        let token = &r.payload[8..];
        let s = self.srv.as_ref().unwrap();
        let mut ident: Option<crate::access::AccessToken> = None;
        let authorized = match s.access.as_ref().unwrap_or(&Access::Open) {
            Access::Open => Ok(()),
            Access::Token(t) => if crypto::ct_eq(token, t) { Ok(()) } else { Err("sai token".to_string()) },
            Access::Keys(keys) => if keys.iter().any(|k| crypto::ct_eq(k, &r.initiator_static)) { Ok(()) } else { Err("key không trong allow-list".into()) },
            Access::Signed { .. } if token.is_empty() => Err("client không gửi access token".into()),
            Access::Signed { authority, revoked } => {
                crate::access::verify(token, authority, revoked, crate::license::now_unix()).map(|t| ident = Some(t))
            }
        };
        // From here on the client proved it holds the ticket (valid mac1 + Noise IK): it
        // gets an authenticated refusal with a reason instead of a silent timeout.
        if let Err(why) = authorized {
            let reply = r.reject(&reject_reason(REJECT_DENIED, 0, 0));
            self.reject(h, from, &why, now, Some((via, reply)));
            return;
        }
        if self.guard.banned_keys.get(&r.initiator_static).is_some_and(|until| now < *until) {
            // Temporarily banned for flooding: answer (rate-limited), don't log every attempt.
            let reply = r.reject(&reject_reason(REJECT_BANNED, 0, 0));
            self.send_refusal(from, via, &reply, now);
            self.srv.as_mut().unwrap().reject_cache.insert(h, (now, Some(reply)));
            return;
        }
        if s.last_ts.get(&r.initiator_static).is_some_and(|&last| ts <= last) {
            return; // replayed handshake
        }
        // Same client key already connected (any shard): the new session replaces it.
        let existing = self.shared.index.lock().unwrap().keys.get(&r.initiator_static).copied();
        if !self.reserve_ccu(existing.is_some()) {
            let (cur, max) = (self.shared.ccu.load(Ordering::Relaxed), self.shared.max_ccu.load(Ordering::Relaxed));
            let why = format!("máy chủ đã đầy ({}/{max}) — đạt giới hạn CCU của license", cur.min(max));
            let reply = r.reject(&reject_reason(REJECT_FULL, cur.min(max), max));
            self.reject(h, from, &why, now, Some((via, reply)));
            return;
        }
        let k = self.router.k();
        let mut remote_old = None;
        if let Some(id) = existing {
            if id as usize % k == self.shard {
                if self.remove_peer(id).is_some_and(|p| p.hold.is_none()) {
                    self.emit(EngineEvent::Disconnected { conn: id, reason: "client kết nối lại".into() });
                }
            } else {
                remote_old = Some(id);
            }
        }
        let idx = self.alloc_index();
        let key = r.initiator_static;
        let (resp, session) = r.accept(idx);
        let _ = self.node.send_on(via, from, &resp);
        let s = self.srv.as_mut().unwrap();
        s.resp_cache.insert(h, (resp, now));
        s.last_ts.insert(key, ts);
        // Stop punching toward this client only (others may be connecting right now).
        s.jobs.retain(|j| !j.targets.iter().any(|t| t.ip() == from.ip()));
        if !self.primary() {
            self.router.to(0, Input::PeerUp(from.ip()));
        }
        let (subject, scopes) = match &ident {
            Some(t) => (Some(t.sub.clone()), Some(t.ports.clone())),
            None => (None, None),
        };
        let pident = ident.map(|t| (t.sub, t.jti));
        self.add_peer(idx, Peer { rate: (now, 0, 0), tcp, sock: if tcp { 0 } else { sock }, tcp_paths: if tcp { vec![from] } else { vec![] }, ident: pident, conn: Conn::new(false, now), session, addr: from, key, overflow: HashMap::new(), upgrade: None, probe_udp: vec![], relay_since: crate::relay::is_relay(&from).then_some(now), armed: None, hold: None });
        // A real, authenticated client: never keep it blocked (on any shard).
        if self.guard.blocked.remove(&from.ip()).is_some() {
            let ip = from.ip();
            self.router.others(self.shard, || Input::Unblock(ip));
        }
        let path = if crate::relay::is_relay(&from) { "relay" } else if tcp { "tcp" } else { path_of(&from) };
        // Replacing a connection on another shard: announce this one only after that shard
        // dropped the old one (the app always sees Disconnected(old) before Connected(new)).
        self.on_connected_ident(idx, from, path, key, subject, scopes, remote_old.is_some());
        if let Some(old) = remote_old {
            self.router.to(old as usize % k, Input::Kick(old, idx, self.shard));
        }
    }

    /// Send a T_REJECT, at most REJECT_REPLIES_PER_IP per second to one IP.
    fn send_refusal(&mut self, to: SocketAddr, via: Via, reply: &[u8], now: Instant) {
        let s = self.srv.as_mut().unwrap();
        let e = s.reject_replies.entry(to.ip()).or_insert((now, 0));
        if now.duration_since(e.0) >= Duration::from_secs(1) {
            *e = (now, 0);
        }
        e.1 += 1;
        if e.1 <= REJECT_REPLIES_PER_IP {
            let _ = self.node.send_on(via, to, reply);
        }
    }

    fn reject(&mut self, h: [u8; 32], from: SocketAddr, why: &str, now: Instant, reply: Option<(Via, Vec<u8>)>) {
        self.stats.lock().unwrap().rejected += 1;
        if let Some((via, r)) = &reply {
            self.send_refusal(from, *via, r, now);
        }
        let s = self.srv.as_mut().unwrap();
        s.reject_cache.insert(h, (now, reply.map(|(_, r)| r)));
        let log_it = s.reject_log.get(&from.ip()).is_none_or(|t| now.duration_since(*t) >= Duration::from_secs(30));
        if log_it {
            s.reject_log.insert(from.ip(), now);
            self.log(format!("từ chối người chơi từ {from}: {why}"));
        }
    }

    fn on_connected(&mut self, conn: u32, addr: SocketAddr, path: &'static str, peer_key: [u8; 32]) {
        self.on_connected_ident(conn, addr, path, peer_key, None, None, false)
    }

    /// `hold`: keep the `Connected` event (and the connection's events) until `KickDone`.
    #[allow(clippy::too_many_arguments)]
    fn on_connected_ident(&mut self, conn: u32, addr: SocketAddr, path: &'static str, peer_key: [u8; 32], subject: Option<String>, scopes: Option<Vec<String>>, hold: bool) {
        {
            let mut st = self.stats.lock().unwrap();
            *st.connects.entry(path).or_default() += 1;
            st.ccu = self.shared.ccu.load(Ordering::Relaxed);
        }
        self.dirty.insert(conn);
        let ev = EngineEvent::Connected { conn, addr, path, peer_key, subject, scopes };
        match self.peers.get_mut(&conn) {
            Some(p) if hold => p.hold = Some(Box::new(ev)),
            _ => self.emit(ev),
        }
    }

    /// The server refused our (valid) handshake and said why: fail now with that reason.
    fn on_reject(&mut self, data: &[u8]) {
        let Some(c) = self.cli.as_mut() else { return };
        if c.done {
            return;
        }
        let Some(reason) = c.initiator.as_ref().and_then(|i| i.finish_reject(data)) else { return };
        c.done = true;
        c.initiator = None;
        self.stats.lock().unwrap().failures += 1;
        self.node.close_tcp();
        self.node.close_spray_except(None);
        self.emit(EngineEvent::ConnectFailed(reject_text(&reason)));
    }

    /// A server probe reached us (possibly on a birthday socket): answer with
    /// our handshake on the very same socket so the server's NAT opens too.
    fn on_punch(&mut self, from: SocketAddr, via: Via) {
        let Some(c) = self.cli.as_ref() else { return };
        let known = c.targets.iter().chain(c.intro_addr.iter()).any(|t| t.ip() == from.ip());
        if c.done {
            // On the fallback route and a server punch got through (maybe on a birthday socket):
            // answer with a sealed ping on that very socket — the server roams onto it.
            if let (true, Some(id)) = (known, c.relay_conn) {
                if let Some(p) = self.peers.get_mut(&id) {
                    if p.probe_udp.len() < 4 * SPRAY_PER_TICK {
                        p.probe_udp.push((via, from));
                    }
                    p.conn.ping();
                    self.dirty.insert(id);
                }
            }
            return;
        }
        if let (true, Some(init)) = (known, &c.initiator) {
            let _ = self.node.send_on(via, from, &init.packet);
        }
    }

    fn on_resp(&mut self, from: SocketAddr, data: &[u8], tcp: bool, sock: u16, now: Instant) {
        let Some(c) = self.cli.as_mut() else { return };
        if c.done {
            return;
        }
        let Some(session) = c.initiator.as_ref().and_then(|i| i.finish(data)) else { return };
        c.done = true;
        c.initiator = None;
        let ticket = c.ticket.as_ref().unwrap();
        let path = ticket
            .candidates
            .iter()
            .find(|x| x.addr == from)
            .map(|x| x.kind.label())
            .unwrap_or(if Some(from) == c.intro_addr { "reflexive" } else if from.is_ipv6() { "ipv6-punched" } else { "punched" });
        let path = if crate::relay::is_relay(&from) { "relay" } else if tcp { "tcp" } else if sock > 0 { "birthday" } else { path };
        let key = ticket.server_pub;
        if !tcp {
            // Remember the working path and what it says about both NATs (`pathcache`):
            // the server's port vs the one it announced; a win on one of our birthday sockets
            // means our NAT is hard; a win on the main socket before any birthday means easy.
            let c = self.cli.as_ref().unwrap();
            let server_nat = c.intro_addr.filter(|a| a.ip() == from.ip()).map(|a| Nat::from_ports(a.port(), from.port()));
            let own_nat = if sock > 0 { Some(Nat::Hard) } else if c.spray_at.is_none() { Some(Nat::Easy) } else { None };
            let file = c.cache_file.clone();
            pathcache::remember_server(file.as_deref(), &key, pathcache::cacheable(&from).then_some(from), server_nat);
            if let (Some(n), Some(net)) = (own_nat, c.net.clone()) {
                pathcache::remember_own_nat(file.as_deref(), &net, n);
            }
            self.debug(format!("ghi nhớ: NAT server {server_nat:?}, NAT mình {own_nat:?}"));
            self.node.close_tcp(); // UDP won: drop fallback connections
            if sock > 0 {
                self.debug(format!("birthday punching thành công (socket #{sock}) — NAT khó đã được đục"));
            }
        }
        let via_relay = crate::relay::is_relay(&from);
        if !via_relay {
            self.node.close_spray_except(if sock > 0 && !tcp { Some(sock) } else { None });
        }
        let idx = session.local_idx;
        if via_relay {
            self.cli.as_mut().unwrap().relay_conn = Some(idx);
        }
        let mut conn = Conn::new(true, now);
        conn.ping();
        if crate::relay::is_relay(&from) {
            let losers: Vec<SocketAddr> = self.cli.as_ref().map(|c| c.relay_paths.iter().map(|(a, _)| *a).filter(|a| *a != from).collect()).unwrap_or_default();
            for a in losers {
                self.node.close_lanes(&a);
            }
        }
        let force_tcp = self.cli.as_ref().is_some_and(|c| c.force_tcp);
        let upgrade = (tcp && !force_tcp).then(|| {
            let t: Vec<SocketAddr> = self.cli.as_ref().map(|c| c.targets.iter().filter(|t| !crate::relay::is_relay(t)).copied().collect()).unwrap_or_default();
            (t, now + UPGRADE_FIRST, 0)
        }).filter(|u| !u.0.is_empty());
        self.shared.ccu.fetch_add(1, Ordering::AcqRel);
        self.add_peer(idx, Peer { rate: (now, 0, 0), tcp, sock, tcp_paths: if tcp { vec![from] } else { vec![] }, ident: None, conn, session, addr: from, key, overflow: HashMap::new(), upgrade, probe_udp: vec![], relay_since: crate::relay::is_relay(&from).then_some(now), armed: None, hold: None });
        self.on_connected(idx, from, path, key);
    }

    fn on_data(&mut self, from: SocketAddr, data: &[u8], tcp: bool, sock: u16, now: Instant) {
        let Some(idx) = Session::packet_index(data) else { return self.bad_packet(from.ip(), now) };
        let Some(p) = self.peers.get_mut(&idx) else { return self.bad_packet(from.ip(), now) };
        let Some((pn, pt)) = p.session.open(data) else { return self.bad_packet(from.ip(), now) };
        // Authenticated flood: an authorized client hammering the server is disconnected
        // and its key banned for a while (legit traffic, even bulk transfers, stays far below).
        if now.duration_since(p.rate.0) >= Duration::from_secs(1) {
            p.rate.2 = if p.rate.1 > self.flood_pps { p.rate.2 + 1 } else { 0 };
            p.rate.0 = now;
            p.rate.1 = 0;
        }
        p.rate.1 += 1;
        if p.rate.2 >= 3 {
            let key = p.key;
            p.conn.close();
            let until = now + BAN_FOR;
            self.guard.banned_keys.insert(key, until);
            self.router.others(self.shard, || Input::Ban(key, until));
            self.stats.lock().unwrap().rejected += 1;
            self.log(format!("từ chối người chơi {idx:08x}: gửi dữ liệu dồn dập (quá {} gói/s) → ngắt và cấm {} phút", self.flood_pps, BAN_FOR.as_secs() / 60));
            self.flush(idx, now);
            return;
        }
        let mut roamed = None;
        if tcp && p.tcp && from != p.addr {
            // Another TCP lane of the same peer (multipath): just remember it.
            if !p.tcp_paths.contains(&from) && p.tcp_paths.len() < 8 {
                p.tcp_paths.push(from);
            }
        } else if p.addr != from || p.tcp != tcp || (!tcp && p.sock != sock) {
            // Authenticated packet from a new address/transport: the peer roamed (Wi-Fi → 4G...).
            roamed = Some((p.addr, p.sock));
            p.addr = from;
            p.tcp = tcp;
            p.sock = sock;
            p.tcp_paths = if tcp { vec![from] } else { vec![] };
            p.conn.ping();
        }
        p.conn.on_packet(pn, &pt, now);
        if let Some((old, old_sock)) = roamed {
            self.reindex((old.ip(), old_sock), (from.ip(), sock));
            let was_reliable = self.peers.get(&idx).is_some_and(|p| p.upgrade.is_some()) || crate::relay::is_relay(&old);
            if was_reliable && !tcp {
                if let Some(p) = self.peers.get_mut(&idx) {
                    p.upgrade = None;
                }
                if let Some(c) = self.cli.as_mut() {
                    self.node.close_tcp(); // client: the UDP path works, drop TCP / fallback lanes
                    self.node.close_spray_except((sock > 0).then_some(sock));
                    c.relay_conn = None;
                }
                self.log(format!("{}: đã chuyển sang kết nối trực tiếp", self.who(idx)));
                self.debug(format!("peer {idx:08x}: UDP trực tiếp {from} (trước đó {old})"));
            } else {
                self.log(format!("{} đổi mạng (Wi-Fi/4G…) — vẫn giữ kết nối", self.who(idx)));
                self.debug(format!("peer {idx:08x} đổi mạng {old} → {from}"));
            }
        }
        self.flush(idx, now);
    }

    fn on_control(&mut self, from: SocketAddr, p: Packet, now: Instant) {
        let supernode = self.srv.as_ref().is_some_and(|s| s.supernode_mode);
        match p {
            Packet::ReflectReq { txid } if supernode => {
                let _ = self.node.send(from, &Packet::ReflectResp { txid, observed: from }.encode());
            }
            Packet::Register { server_pub, lease } if supernode => {
                let s = self.srv.as_ref().unwrap();
                if let Some(auth) = &s.authority {
                    // Licensed supernode: serve only servers with a valid, key-bound lease.
                    if !crate::license::verify_for_supernode(&lease, auth, &server_pub, crate::license::now_unix()) {
                        self.stats.lock().unwrap().rejected += 1;
                        return; // silent: unlicensed / cracked builds get nothing
                    }
                }
                let id = crypto::key_id(&server_pub);
                let s = self.srv.as_mut().unwrap();
                let list = s.registry.entry(id).or_default();
                let fresh = !list.iter().any(|(a, _, _)| *a == from);
                // Registration can't prove key ownership (no lease without an authority), so a
                // newcomer may never evict a live instance: drop only stale entries, cap entries
                // per source IP, and refuse when the list is full of live servers.
                list.retain(|(a, _, t)| *a != from && now.duration_since(*t) < REGISTRY_STALE);
                let same_ip = list.iter().filter(|(a, _, _)| a.ip() == from.ip()).count();
                if fresh && (list.len() >= 8 || same_ip >= 2) {
                    return;
                }
                list.push((from, server_pub, now));
                if fresh {
                    let m = format!("[supernode] server {} đăng ký từ {from}", &crate::util::hex(&id)[..8]);
                    self.debug(m);
                }
                let _ = self.node.send(from, &Packet::RegisterAck { observed: from }.encode());
            }
            Packet::RegisterAck { observed } => {
                let Some(s) = self.srv.as_mut() else { return };
                if !s.supernodes.contains(&from) {
                    return;
                }
                s.acks += 1;
                if !s.candidates.iter().any(|c| c.addr == observed) && !s.ready_sent {
                    s.candidates.push(Candidate { kind: CandKind::Reflexive, addr: observed });
                    self.debug(format!("supernode {from} thấy server là {observed}"));
                }
            }
            Packet::IntroReq { server_id, session, mut candidates, proof } => {
                let Some(s) = self.srv.as_ref() else { return };
                if server_id == crypto::key_id(&self.key.public) {
                    if !crypto::ct_eq(&proof, &intro_proof(&self.key.public, session, &server_id)) {
                        return;
                    }
                    // Anti-amplification: a direct request may only make us punch back to
                    // its own source address, never to addresses it names.
                    let mut t: Vec<SocketAddr> = candidates.iter().map(|c| c.addr).filter(|a| a.ip() == from.ip()).take(4).collect();
                    t.push(from);
                    self.start_punch(t, now);
                    let _ = self.node.send(from, &Packet::IntroResp { session, status: 0, server_addr: None }.encode());
                    return;
                }
                if !s.supernode_mode {
                    return;
                }
                let instances = s.registry.get(&server_id).cloned().unwrap_or_default();
                let Some((_, spub, _)) = instances.first().copied() else {
                    let _ = self.node.send(from, &Packet::IntroResp { session, status: 1, server_addr: None }.encode());
                    return;
                };
                if !crypto::ct_eq(&proof, &intro_proof(&spub, session, &server_id)) {
                    return; // bad proof: stay silent
                }
                candidates.push(Candidate { kind: CandKind::Reflexive, addr: from });
                // Introduce the client to EVERY instance of this server (failover).
                for (saddr, _, _) in &instances {
                    let _ = self.node.send(*saddr, &Packet::Intro { session, candidates: candidates.clone() }.encode());
                    let _ = self.node.send(from, &Packet::IntroResp { session, status: 0, server_addr: Some(*saddr) }.encode());
                }
                self.debug(format!("[supernode] giới thiệu {from} → server {} ({} máy)", &crate::util::hex(&server_id)[..8], instances.len()));
            }
            Packet::Intro { candidates, .. } => {
                let Some(s) = self.srv.as_ref() else { return };
                if !s.supernodes.contains(&from) {
                    return; // only trust our configured supernodes
                }
                let spray_ip = candidates
                    .iter()
                    .filter(|c| c.kind == CandKind::Reflexive)
                    .map(|c| c.addr.ip())
                    .find(|ip| matches!(ip, IpAddr::V4(v4) if netinfo::classify_v4(v4) == V4Class::Public));
                self.start_punch_ex(candidates.iter().map(|c| c.addr).collect(), spray_ip, true, now);
            }
            Packet::IntroResp { session, status, server_addr } => {
                let Some(c) = self.cli.as_mut() else { return };
                if session != c.session || !c.supernodes.contains(&from) {
                    return;
                }
                c.intro_ok = true;
                if status != 0 {
                    let m = format!("supernode {from} không biết server này (server chưa đăng ký?)");
                    self.debug(m);
                    return;
                }
                if let Some(sa) = server_addr {
                    if self.node.supports(&sa) && !c.targets.contains(&sa) {
                        c.targets.push(sa);
                        c.intro_addr = Some(sa);
                        c.addr_at.get_or_insert(now);
                        // Let the server's low-TTL punch open ITS NAT first; if our packet
                        // reached its NAT before that, the NAT would remap the server's port.
                        c.next_probe = Some(now + CLIENT_PROBE_DELAY);
                    }
                }
            }
            _ => {}
        }
    }

    /// Server: announce to the website rendezvous and collect waiting clients (CONTRACT §8).
    fn spawn_rdv_poll(&self, url: String, cands: &[Candidate]) {
        let tok_file = self.srv.as_ref().and_then(|s| s.rdv_token_file.clone());
        let sid = crate::util::b64_encode(&self.key.public);
        let port = self.node.port_v4().unwrap_or(0);
        let lan: Vec<SocketAddr> = cands.iter().filter(|c| c.kind == CandKind::Host).map(|c| c.addr).collect();
        let v6: Vec<SocketAddr> = cands.iter().filter(|c| c.kind == CandKind::Ipv6).map(|c| c.addr).collect();
        let tcp = self.srv.as_ref().is_some_and(|s| s.tcp_fallback);
        let tx = self.router.clone();
        let lease = self.lease_slot.clone();
        thread::Builder::new()
            .name("peerova-rdv".into())
            .spawn(move || {
                let mut announced = false;
                let mut last_err = String::new();
                // Reuse the binding token across restarts so the website recognises us at once.
                let mut tok: Option<String> = tok_file.as_ref().and_then(|f| std::fs::read_to_string(f).ok()).map(|t| t.trim().to_string()).filter(|t| !t.is_empty() && t.len() <= 128);
                let mut saved = tok.clone();
                loop {
                    let cur = lease.lock().unwrap().clone();
                    let t0 = Instant::now();
                    let res = crate::rdv::poll(&url, &sid, port, &lan, &v6, tcp, cur.as_deref(), &mut tok);
                    if tok != saved {
                        if let Some(f) = &tok_file {
                            let _ = match &tok {
                                Some(t) => std::fs::write(f, t),
                                None => std::fs::remove_file(f),
                            };
                        }
                        saved = tok.clone();
                    }
                    let wait = match res {
                        Ok((ip, clients, secs, relay)) => {
                            if let Some(r) = relay {
                                if !tx.cmd(Cmd::SetRelay(r)) {
                                    return;
                                }
                            }
                            if !announced {
                                announced = true;
                                let ip = ip.map(|i| i.to_string()).unwrap_or("?".into());
                                if !tx.cmd(Cmd::Note(format!("đã đăng ký với website Peerova (IP public {ip}) — người chơi vào được mà không cần mở port"))) {
                                    return;
                                }
                            }
                            let got = !clients.is_empty();
                            if got && !tx.cmd(Cmd::RdvClients(clients)) {
                                return;
                            }
                            // Long-poll answered (client arrived or the wait elapsed): ask again
                            // at once. An older website answers immediately: keep its interval.
                            if got || t0.elapsed() >= Duration::from_secs(crate::rdv::POLL_WAIT - 2) { 0 } else { secs }
                        }
                        Err(e) => {
                            if e != last_err {
                                last_err = e.clone();
                                if !tx.cmd(Cmd::Note(format!("chưa liên lạc được website Peerova: {e} — tự thử lại; người chơi trong LAN / có ticket trực tiếp vẫn vào được"))) {
                                    return;
                                }
                            }
                            5
                        }
                    };
                    // Stop when the engine is gone (channel closed).
                    if !tx.cmd(Cmd::Note(String::new())) {
                        return;
                    }
                    if wait > 0 {
                        thread::sleep(Duration::from_secs(wait));
                    }
                }
            })
            .expect("spawn rdv thread");
    }

    /// Server: keep a standby room on `base` (once per relay).
    fn start_standby(&mut self, base: String) {
        let Some(s) = self.srv.as_mut() else { return };
        if s.standby.contains(&base) {
            return;
        }
        s.standby.push(base.clone());
        let lease = self.lease_slot.clone();
        let out = self.out.clone();
        let room = crate::relay::room(&self.key.public, 0);
        self.node.relay_standby(&base, &room, Arc::new(move || lease.lock().unwrap().clone().map(|l| format!("Lease {l}"))), move |m| {
            if debug_enabled() {
                let _ = out.send(EngineEvent::Log(m));
            }
        });
    }

    /// Client: open the fallback route on up to `relay::RACE` relays not tried yet.
    /// They race: the first handshake to complete picks the fastest relay.
    fn dial_relays(&mut self) {
        let Some(c) = self.cli.as_mut() else { return };
        let all = crate::relay::list(c.relay.as_deref().unwrap_or(""));
        let fresh: Vec<String> = all.into_iter().filter(|u| !c.relay_paths.iter().any(|(_, x)| x == u)).collect();
        let budget = crate::relay::RACE.saturating_sub(c.relay_paths.len());
        let (room, cid) = (c.relay_room.clone(), c.relay_cid.clone());
        for base in fresh.into_iter().take(budget) {
            let addr = self.open_relay(&base, &room, &cid);
            let c = self.cli.as_mut().unwrap();
            c.targets.push(addr);
            c.relay_paths.push((addr, base));
        }
    }

    fn open_relay(&self, base: &str, room: &str, cid: &str) -> SocketAddr {
        let (out, me) = (self.out.clone(), self.router.clone());
        self.node.connect_relay(base, room, "c", None, cid, move |m| {
            if debug_enabled() {
                let _ = out.send(EngineEvent::Log(m));
            }
        }, move || {
            me.cmd(Cmd::RelayUp);
        })
    }

    fn start_punch(&mut self, targets: Vec<SocketAddr>, now: Instant) {
        self.start_punch_ex(targets, None, true, now)
    }

    fn start_punch_ex(&mut self, targets: Vec<SocketAddr>, spray_ip: Option<IpAddr>, low_ttl: bool, now: Instant) {
        self.start_punch_verified(targets, spray_ip, low_ttl, None, now);
    }

    /// `verified`: an IP proven by the website (seen over HTTPS/TCP, can't be spoofed) —
    /// it is the requester's own address, so it's exempt from the per-IP quota. Spoofable
    /// sources (direct UDP requests) and self-declared addresses stay rate-limited.
    /// Returns whether a job was started (it is then the last one in `jobs`).
    fn start_punch_verified(&mut self, mut targets: Vec<SocketAddr>, spray_ip: Option<IpAddr>, low_ttl: bool, verified: Option<IpAddr>, now: Instant) -> bool {
        // Anti-amplification budget: each IP at most once per PUNCH_IP_COOLDOWN, a bounded
        // number of concurrent jobs, and a bounded target list per job.
        if let Some(s) = self.srv.as_mut() {
            s.punch_recent.retain(|_, (t, _)| now.duration_since(*t) < PUNCH_IP_COOLDOWN);
            targets.retain(|t| Some(t.ip()) == verified || s.punch_recent.get(&t.ip()).is_none_or(|(_, n)| *n < PUNCH_PER_IP));
            let cap = if verified.is_some() { MAX_PUNCH_JOBS_VERIFIED } else { MAX_PUNCH_JOBS };
            if targets.is_empty() || s.jobs.len() >= cap {
                return false;
            }
            let ips: std::collections::HashSet<IpAddr> = targets.iter().map(|t| t.ip()).filter(|ip| Some(*ip) != verified).collect();
            for ip in ips {
                s.punch_recent.entry(ip).or_insert((now, 0)).1 += 1;
            }
            if s.punch_recent.len() > 100_000 {
                s.punch_recent.clear();
            }
        }
        targets.truncate(MAX_PUNCH_TARGETS);
        let extra: Vec<SocketAddr> = targets.iter().take(2).flat_map(predict_ports).collect();
        targets.extend(extra);
        targets.retain(|t| self.node.supports(t));
        targets.dedup();
        if targets.is_empty() {
            return false;
        }
        self.debug(format!("punch NAT tới {targets:?}"));
        if let Some(s) = self.srv.as_mut() {
            // Start slightly late so the client's first packet leaves its NAT before our punch arrives.
            let low_ttl_until = if low_ttl { now + LOW_TTL_FOR } else { now };
            let center_port = spray_ip.and_then(|ip| targets.iter().find(|t| t.ip() == ip).map(|t| t.port()));
            s.jobs.push(PunchJob {
                spray_ip,
                birthday_at: now + BIRTHDAY_AFTER,
                low_ttl_until,
                sym_at: now + SYM_AFTER,
                center_port,
                sym_sent: 0,
                spray_until: now + SPRAY_FOR,
                targets,
                until: now + PUNCH_FOR,
                next: now,
            });
            return true;
        }
        false
    }

    /// Client on the fallback route: the direct handshake is over, but the hard-NAT techniques
    /// keep running (bounded by `SPRAY_FOR`) with sealed pings instead of handshakes, so a
    /// symmetric NAT gets its direct path too; the low-rate upgrade probes continue afterwards.
    fn upgrade_punch(&mut self, now: Instant) {
        let Some(c) = self.cli.as_mut() else { return };
        let Some(id) = c.relay_conn else { return };
        let on_relay = self.peers.get(&id).is_some_and(|p| p.tcp && crate::relay::is_relay(&p.addr));
        let pub_v4 = c.intro_addr.filter(|a| matches!(a.ip(), IpAddr::V4(ip) if netinfo::classify_v4(&ip) == V4Class::Public));
        let start = c.addr_at.or(c.started).unwrap_or(now);
        let (Some(target), true, false) = (pub_v4, on_relay, c.force_tcp) else {
            if !on_relay {
                c.relay_conn = None;
            }
            return;
        };
        if now.duration_since(start) > SPRAY_FOR + FAST_SYM_AFTER {
            c.relay_conn = None; // bounded: give up active punching, keep the fallback route
            self.node.close_spray_except(None);
            return;
        }
        if c.next_probe.is_some_and(|t| now < t) {
            return;
        }
        c.next_probe = Some(now + PROBE_INTERVAL);
        if c.spray.is_empty() && c.spray_at.is_none() {
            let hard = c.own_nat == Some(Nat::Hard) || c.server_nat == Some(Nat::Hard);
            let easy = c.own_nat == Some(Nat::Easy) && c.server_nat == Some(Nat::Easy);
            let delay = if hard { Duration::ZERO } else if easy { BIRTHDAY_AFTER } else { FAST_BIRTHDAY_AFTER };
            if now.duration_since(start) >= delay {
                c.spray_at = Some(now);
                c.spray = self.node.open_spray(SPRAY_SOCKETS);
            }
        }
        let mut probes: Vec<(Via, SocketAddr)> = vec![(Via::Udp(0), target)];
        if c.spray_at.is_some_and(|t| now.duration_since(t) < SPRAY_FOR) {
            // Open our NAT from every birthday socket (low TTL: never reaches the server's NAT).
            let mut pkt = vec![T_PUNCH];
            pkt.extend_from_slice(&crypto::random_bytes::<8>());
            for sid in &c.spray {
                let _ = self.node.send_on_low_ttl(Via::Udp(*sid), target, &pkt, PUNCH_TTL);
            }
            if !c.spray.is_empty() {
                // Server may be the symmetric side: sealed pings to random ports of its IP.
                for _ in 0..SPRAY_PER_TICK {
                    let port = 1024 + (crypto::random_u32() % (65535 - 1024)) as u16;
                    probes.push((Via::Udp(0), SocketAddr::new(target.ip(), port)));
                }
            }
            let both_hard = c.own_nat == Some(Nat::Hard) && c.server_nat == Some(Nat::Hard);
            let sym_due = if both_hard { c.punch_at.is_none_or(|t| now >= t) } else { now.duration_since(start) >= FAST_SYM_AFTER };
            if sym_due && c.sym_sent < SYM_MAX_PROBES {
                for sid in &c.spray {
                    probes.push((Via::Udp(*sid), SocketAddr::new(target.ip(), window_port(target.port()))));
                    c.sym_sent += 1;
                }
            }
        }
        if let Some(p) = self.peers.get_mut(&id) {
            p.probe_udp.extend(probes);
            p.conn.ping();
            self.dirty.insert(id);
        }
    }

    fn register(&mut self, now: Instant) {
        let Some(s) = self.srv.as_mut() else { return };
        s.last_register = Some(now);
        let lease = s.lease.clone().unwrap_or_default().into_bytes();
        let pkt = Packet::Register { server_pub: self.key.public, lease }.encode();
        for sn in s.supernodes.clone() {
            let _ = self.node.send(sn, &pkt);
        }
    }

    fn tick(&mut self, now: Instant) {
        // Periodic O(n) cleanups: at most every SWEEP_EVERY, not per packet batch.
        let sweep = now >= self.next_sweep;
        if sweep {
            self.next_sweep = now + SWEEP_EVERY;
            if let Some(s) = self.srv.as_mut() {
                s.resp_cache.retain(|_, (_, t)| now.duration_since(*t) < Duration::from_secs(10));
                s.reject_cache.retain(|_, (t, _)| now.duration_since(*t) < Duration::from_secs(30));
                if s.reject_replies.len() > 10_000 {
                    s.reject_replies.clear();
                }
                if s.reject_log.len() > 10_000 {
                    s.reject_log.clear();
                }
                if s.rate.len() > 10_000 {
                    s.rate.clear();
                }
                for list in s.registry.values_mut() {
                    list.retain(|(_, _, t)| now.duration_since(*t) < Duration::from_secs(60));
                }
                s.registry.retain(|_, l| !l.is_empty());
            }
        }
        // Server housekeeping: the primary shard only.
        if self.srv.is_some() && self.primary() {
            let s = self.srv.as_mut().unwrap();
            if !s.ready_sent && (s.ready_at.is_some_and(|t| now >= t) || (s.acks >= s.supernodes.len() && !s.supernodes.is_empty())) {
                s.ready_sent = true;
                let ticket = Ticket {
                    server_pub: self.key.public,
                    private: s.private_ticket,
                    tcp: s.tcp_fallback,
                    rdv: s.rendezvous.is_some(),
                    hidden: s.hidden,
                    candidates: s.candidates.clone(),
                    supernodes: s.supernodes.clone(),
                };
                let cands = s.candidates.clone();
                self.stats.lock().unwrap().max_ccu = self.shared.max_ccu.load(Ordering::Relaxed);
                // Standby in our own fallback room: clients that can't come direct reach us
                // without waiting for the website or a fresh handshake on our side.
                for base in crate::relay::list(s.relay.as_deref().unwrap_or("")) {
                    self.start_standby(base);
                }
                self.emit(EngineEvent::Ready { ticket: Some(ticket.encode()), candidates: cands, session: 0 });
            }
            // A job is done as soon as that client is connected (only scan when jobs exist,
            // and at most every SWEEP_EVERY: O(peers) work must not run per packet batch).
            let has_jobs = !self.srv.as_ref().unwrap().jobs.is_empty();
            if sweep && has_jobs {
                let ix = self.shared.index.lock().unwrap();
                let s = self.srv.as_mut().unwrap();
                s.jobs.retain(|j| j.spray_ip.is_none_or(|ip| !ix.ips.contains_key(&ip)));
            }
            let s = self.srv.as_mut().unwrap();
            let need_spray = s.jobs.iter().any(|j| j.spray_ip.is_some() && now >= j.birthday_at && (now < j.birthday_at + SPRAY_FOR || j.sym_sent < SYM_MAX_PROBES));
            if need_spray && s.spray.is_empty() {
                s.spray = self.node.open_spray(SPRAY_SOCKETS);
                if debug_enabled() {
                    let _ = self.out.send(EngineEvent::Log(format!("client NAT khó → server mở {} socket birthday", s.spray.len())));
                }
            }
            if need_spray {
                s.spray_used = Some(now);
            } else if s.spray_used.is_some_and(|t| now.duration_since(t) > Duration::from_secs(30)) && !s.spray.is_empty() {
                // Keep them while any peer (on any shard) still talks through one.
                if self.shared.index.lock().unwrap().spray == 0 {
                    self.node.close_spray_except(None);
                    s.spray.clear();
                }
            }
            for j in s.jobs.iter_mut() {
                if now >= j.next {
                    j.next = now + Duration::from_millis(150);
                    let mut pkt = vec![T_PUNCH];
                    pkt.extend_from_slice(&crypto::random_bytes::<8>());
                    for t in &j.targets {
                        if now < j.low_ttl_until {
                            let _ = self.node.send_low_ttl(*t, &pkt, PUNCH_TTL);
                        } else {
                            let _ = self.node.send(*t, &pkt);
                        }
                    }
                    if let Some(ip) = j.spray_ip {
                        for _ in 0..if now < j.spray_until { SPRAY_PER_TICK } else { 0 } {
                            let port = 1024 + (crypto::random_u32() % (65535 - 1024)) as u16;
                            let _ = self.node.send(SocketAddr::new(ip, port), &pkt);
                        }
                        if now >= j.birthday_at && now < j.birthday_at + SPRAY_FOR {
                            // Our own NAT may be symmetric: punch from many sockets towards the
                            // client's public addresses; the client sprays our IP's ports.
                            for id in &s.spray {
                                for t in j.targets.iter().filter(|t| t.ip() == ip) {
                                    let _ = self.node.send_on_low_ttl(Via::Udp(*id), *t, &pkt, PUNCH_TTL);
                                }
                            }
                        }
                        // Symmetric ↔ symmetric: carrier NATs (CGNAT) allocate each subscriber a
                        // small PORT BLOCK (RFC 7422). Spray, at full TTL, from every birthday
                        // socket into a window around the client's observed port; the client does
                        // the same towards us. One matching pair is enough. Capped per side.
                        if let (true, Some(c)) = (now >= j.sym_at && j.sym_sent < SYM_MAX_PROBES, j.center_port) {
                            for id in &s.spray {
                                let port = window_port(c);
                                let _ = self.node.send_on(Via::Udp(*id), SocketAddr::new(ip, port), &pkt);
                                j.sym_sent += 1;
                            }
                        }
                    }
                }
            }
            s.jobs.retain(|j| now < j.until);
            if s.last_register.is_some_and(|t| now.duration_since(t) >= REGISTER_EVERY) {
                self.register(now);
            }
            let s = self.srv.as_mut().unwrap();
            if let (Some(m), Some(at)) = (&s.mapping, s.map_at) {
                if m.lifetime > 0 && now.duration_since(at) > Duration::from_secs(m.lifetime as u64 / 2) {
                    s.map_at = Some(now);
                    if let (Some(ip), Some(p)) = (netinfo::local_ipv4(), self.node.port_v4()) {
                        s.mapping = nat::map_udp_port(ip, p, &mut |_| {}).or(s.mapping.clone());
                    }
                }
            }
        }

        // Client probing.
        if let Some(c) = self.cli.as_mut() {
            if !c.done {
                if c.deadline.is_some_and(|d| now >= d) {
                    c.done = true;
                    self.stats.lock().unwrap().failures += 1;
                    self.emit(EngineEvent::ConnectFailed(
                        "không kết nối được: hết thời gian. Chạy `peerova doctor` ở cả 2 máy (thường do cả 2 sau CGNAT/symmetric NAT và không có IPv6)".into(),
                    ));
                } else {
                    if !c.intro_ok && c.next_intro.is_some_and(|t| now >= t) {
                        c.next_intro = Some(now + Duration::from_secs(1));
                        let ticket = c.ticket.as_ref().unwrap();
                        let id = ticket.server_id();
                        let pkt = Packet::IntroReq {
                            server_id: id,
                            session: c.session,
                            candidates: c.mine.clone(),
                            proof: intro_proof(&ticket.server_pub, c.session, &id),
                        }
                        .encode();
                        for sn in &c.supernodes {
                            let _ = self.node.send(*sn, &pkt);
                        }
                    }
                    if c.next_probe.is_some_and(|t| now >= t) {
                        c.next_probe = Some(now + PROBE_INTERVAL);
                        // Leaving the low-TTL phase: send the first full-TTL burst right then.
                        if let Some(u) = c.rdv_low_ttl_until.filter(|u| *u > now && *u < now + PROBE_INTERVAL) {
                            c.next_probe = Some(u);
                        }
                        // Symmetric-NAT helper: also hit the next ports of reflexive addresses.
                        let predicted: Vec<SocketAddr> = c.intro_addr.iter().flat_map(|a| predict_ports_for(a, c.server_nat)).collect();
                        if let Some(init) = &c.initiator {
                            if !c.force_tcp {
                                let low = c.rdv_low_ttl_until.is_some_and(|u| now < u);
                                for t in c.targets.iter().chain(predicted.iter()).filter(|t| !crate::relay::is_relay(t)) {
                                    if low && (c.rdv_targets.contains(t) || predicted.contains(t)) {
                                        let _ = self.node.send_low_ttl(*t, &init.packet, PUNCH_TTL);
                                    } else {
                                        let _ = self.node.send(*t, &init.packet);
                                    }
                                }
                            }
                            for t in &c.targets {
                                if self.node.has_tcp(t) {
                                    let _ = self.node.send_via(*t, &init.packet, true);
                                }
                            }
                        }
                        // Still silent and we know the server's public address: birthday punching
                        // (many sockets = many NAT mappings for the server's random probes to hit).
                        let pub_v4 = c.intro_addr.filter(|a| matches!(a.ip(), IpAddr::V4(ip) if netinfo::classify_v4(&ip) == V4Class::Public));
                        if let (Some(target), true, false) = (pub_v4, c.spray.is_empty() && c.spray_at.is_none(), c.force_tcp) {
                            // As soon as it's known to be needed: at once for a NAT remembered as
                            // hard, soon for unknown ones, late when both are remembered as easy.
                            let hard = c.own_nat == Some(Nat::Hard) || c.server_nat == Some(Nat::Hard);
                            let easy = c.own_nat == Some(Nat::Easy) && c.server_nat == Some(Nat::Easy);
                            let delay = if hard { Duration::ZERO } else if easy { BIRTHDAY_AFTER } else { FAST_BIRTHDAY_AFTER };
                            let due = match c.addr_at {
                                Some(t) => now.duration_since(t) >= delay,
                                None => c.started.is_some_and(|s| now.duration_since(s) >= BIRTHDAY_AFTER),
                            };
                            if due {
                                c.spray_at = Some(now);
                                c.spray = self.node.open_spray(SPRAY_SOCKETS);
                                if debug_enabled() {
                                    let _ = self.out.send(EngineEvent::Log(format!("NAT khó → birthday punching với {} socket tới {target}", c.spray.len())));
                                }
                            }
                        }
                        // Bounded active punching: sprays stop SPRAY_FOR after they started (the
                        // sockets stay open for late answers; plain probes continue).
                        let spraying = c.spray_at.is_some_and(|t| now.duration_since(t) < SPRAY_FOR);
                        if let (Some(target), Some(init), true) = (pub_v4, &c.initiator, spraying) {
                            // Many sockets, LOW TTL: open 128 mappings on our NAT without letting
                            // the packets hit (and pollute) the server's NAT; the server's
                            // random-port spray then lands on one of them.
                            for id in &c.spray {
                                let _ = self.node.send_on_low_ttl(Via::Udp(*id), target, &init.packet, PUNCH_TTL);
                            }
                            if !c.spray.is_empty() {
                                // Server may be the symmetric side: hit random ports of its IP.
                                for _ in 0..SPRAY_PER_TICK {
                                    let port = 1024 + (crypto::random_u32() % (65535 - 1024)) as u16;
                                    let _ = self.node.send(SocketAddr::new(target.ip(), port), &init.packet);
                                }
                            }
                            // Symmetric ↔ symmetric: two-sided birthday inside the server's port
                            // window, from every birthday socket (full TTL), capped.
                            let both_hard = c.own_nat == Some(Nat::Hard) && c.server_nat == Some(Nat::Hard);
                            let sym_due = match (c.addr_at, both_hard) {
                                (Some(_), true) => c.punch_at.is_none_or(|t| now >= t),
                                (Some(t), false) => now.duration_since(t) >= FAST_SYM_AFTER,
                                (None, _) => c.started.is_some_and(|s| now.duration_since(s) >= SYM_AFTER),
                            };
                            if sym_due && !c.spray.is_empty() && c.sym_sent < SYM_MAX_PROBES {
                                for id in &c.spray {
                                    let port = window_port(target.port());
                                    let _ = self.node.send_on(Via::Udp(*id), SocketAddr::new(target.ip(), port), &init.packet);
                                    c.sym_sent += 1;
                                }
                            }
                        }
                        // Last resort: P2P still silent → relay through the website (E2E-encrypted).
                        let relay_due = c.started.is_some_and(|s| now.duration_since(s) >= RELAY_AFTER);
                        let rdv_ticket = c.ticket.as_ref().is_some_and(|t| t.rdv);
                        let hidden = c.ticket.as_ref().is_some_and(|t| t.hidden);
                        if relay_due && (rdv_ticket || hidden) && !c.relay_started && !c.force_tcp && c.relay.is_some() {
                            c.relay_started = true;
                            c.relay_room = crate::relay::room(&c.ticket.as_ref().unwrap().server_pub, 0);
                            c.relay_cid = format!("&cid={:016x}", crate::rdv::random_cid());
                            self.dial_relays();
                        }
                        let Some(c) = self.cli.as_mut() else { return };
                        // UDP silent for a while: open direct TCP fallbacks ().
                        let tcp_ok = c.ticket.as_ref().is_some_and(|t| t.tcp);
                        let waited = c.started.is_some_and(|s| now.duration_since(s) >= TCP_FALLBACK_AFTER);
                        if tcp_ok && !c.tcp_started && (waited || c.force_tcp) {
                            c.tcp_started = true;
                            for t in c.targets.clone() {
                                self.node.connect_tcp(t);
                            }
                            if debug_enabled() {
                                let _ = self.out.send(EngineEvent::Log("UDP chưa thông → thử TCP trực tiếp (mạng có thể chặn UDP)".into()));
                            }
                        }
                    }
                }
            }
        }

        self.upgrade_punch(now);

        // Relay failover: the relay carrying a connection died (outage, quota) → move the
        // same encrypted connection to another relay; the server roams on the first packet.
        if let Some(c) = self.cli.as_ref() {
            let failing: Vec<u32> = self
                .peers
                .iter()
                .filter(|(_, p)| p.tcp && crate::relay::is_relay(&p.addr) && !self.node.has_tcp(&p.addr))
                .filter(|(_, p)| p.relay_since.is_some_and(|t| now.duration_since(t) >= RELAY_FAILOVER_GRACE))
                .map(|(&id, _)| id)
                .collect();
            let all = crate::relay::list(c.relay.as_deref().unwrap_or(""));
            let (room, cid) = (c.relay_room.clone(), c.relay_cid.clone());
            for id in failing {
                let cur = self.cli.as_ref().unwrap().relay_paths.iter().find(|(a, _)| Some(*a) == self.peers.get(&id).map(|p| p.addr)).map(|(_, u)| u.clone());
                // Next relay after the current one (wraps; with a single relay, retry it).
                let i = cur.as_ref().and_then(|u| all.iter().position(|x| x == u)).map_or(0, |i| i + 1);
                let Some(base) = all.get(i % all.len().max(1)).cloned() else { continue };
                let addr = self.open_relay(&base, &room, &cid);
                self.cli.as_mut().unwrap().relay_paths.push((addr, base.clone()));
                if let Some(p) = self.peers.get_mut(&id) {
                    let old = (p.addr.ip(), p.sock);
                    p.addr = addr;
                    p.tcp_paths = vec![addr];
                    p.relay_since = Some(now);
                    self.reindex(old, (addr.ip(), old.1));
                }
                self.log("đường truyền bị gián đoạn → tự chuyển sang đường khác…".into());
                self.debug(format!("đường dự phòng bị ngắt → chuyển sang relay #{}", i % all.len().max(1) + 1));
            }
        }

        // Relay → direct upgrade probes (sealed pings over UDP; the peer roams on arrival).
        // Periodic sweep only: probes are seconds apart anyway.
        let upgrade_sweep = now >= self.next_upgrade_sweep;
        if upgrade_sweep {
            self.next_upgrade_sweep = now + SWEEP_EVERY;
        }
        for (&id, p) in self.peers.iter_mut().filter(|_| upgrade_sweep) {
            if !p.tcp {
                p.upgrade = None;
                continue;
            }
            if let Some((targets, next, sent)) = p.upgrade.as_mut() {
                if now >= *next {
                    *sent += 1;
                    *next = now + if *sent < UPGRADE_FAST_PROBES { UPGRADE_EVERY_FAST } else { UPGRADE_EVERY_SLOW };
                    p.probe_udp = targets.iter().map(|t| (Via::Udp(0), *t)).collect();
                    p.conn.ping();
                    self.dirty.insert(id);
                }
            }
        }

        // Connections: only those with a due timer or pending work.
        let mut work: Vec<u32> = self.dirty.drain().collect();
        while let Some(std::cmp::Reverse((t, id))) = self.timers.peek().copied() {
            if t > now {
                break;
            }
            self.timers.pop();
            if let Some(p) = self.peers.get_mut(&id) {
                if p.armed == Some(t) {
                    p.armed = None;
                }
                if p.conn.poll_timeout().is_some_and(|t| now >= t) {
                    p.conn.on_timeout(now);
                }
                work.push(id); // flush re-arms its timer (or it was a stale entry)
            }
        }
        work.sort_unstable();
        work.dedup();
        for id in work {
            self.flush(id, now);
        }
    }

    fn flush(&mut self, id: u32, now: Instant) {
        let Some(p) = self.peers.get_mut(&id) else { return };
        // Move app overflow into the connection as the send buffer drains.
        let mut finished = vec![];
        for (sid, (buf, fin)) in p.overflow.iter_mut() {
            if !buf.is_empty() {
                let n = p.conn.stream_write(*sid, buf.make_contiguous());
                buf.drain(..n);
            }
            if buf.is_empty() {
                if *fin {
                    p.conn.stream_finish(*sid);
                }
                finished.push(*sid);
            }
        }
        for sid in finished {
            p.overflow.remove(&sid);
        }
        p.conn.set_reliable_path(p.tcp);
        let probes = std::mem::take(&mut p.probe_udp);
        while let Some((pn, pt)) = p.conn.poll_transmit(now) {
            let pkt = p.session.seal(pn, &pt);
            for (via, a) in &probes {
                let _ = self.node.send_on(*via, *a, &pkt); // duplicate over UDP: the first copy to arrive wins
            }
            if p.tcp {
                let _ = self.node.send_tcp(&p.tcp_paths, &pkt);
            } else {
                let _ = self.node.send_on(Via::Udp(p.sock), p.addr, &pkt);
            }
        }
        let mut closed = None;
        let mut evs = vec![];
        // Not announced yet (see `hold`): leave its events queued in the connection.
        while let Some(e) = if p.hold.is_some() { None } else { p.conn.poll_event() } {
            evs.push(match e {
                ConnEvent::StreamOpened(sid) => EngineEvent::StreamOpened { conn: id, id: sid },
                ConnEvent::StreamData(sid, data) => EngineEvent::StreamData { conn: id, id: sid, data },
                ConnEvent::StreamFin(sid) => EngineEvent::StreamFin { conn: id, id: sid },
                ConnEvent::StreamReset(sid) => EngineEvent::StreamReset { conn: id, id: sid },
                ConnEvent::StreamAcked(sid, n) => EngineEvent::StreamAcked { conn: id, id: sid, n },
                ConnEvent::Datagram(data) => EngineEvent::Datagram { conn: id, data },
                ConnEvent::Closed(r) => {
                    closed = Some(r.to_string());
                    continue;
                }
            });
        }
        if p.tcp && p.tcp_paths.len() > 1 {
            let node = &self.node;
            p.tcp_paths.retain(|a| node.has_tcp(a));
        }
        if p.conn.is_closed() && closed.is_none() {
            closed = Some("đóng".into());
        }
        for e in evs {
            self.emit(e);
        }
        if let Some(reason) = closed {
            // Never announced → nothing to take back.
            if self.remove_peer(id).is_some_and(|p| p.hold.is_none()) {
                self.emit(EngineEvent::Disconnected { conn: id, reason });
            }
        } else if let Some(p) = self.peers.get_mut(&id) {
            // Re-arm the timer only if it moved earlier (or nothing is armed / it fired).
            if let Some(t) = p.conn.poll_timeout() {
                if p.armed.is_none_or(|a| t < a || a <= now) {
                    p.armed = Some(t);
                    self.timers.push(std::cmp::Reverse((t, id)));
                }
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn wait<F: FnMut(&EngineEvent) -> bool>(e: &Engine, secs: u64, mut f: F) -> EngineEvent {
        let deadline = Instant::now() + Duration::from_secs(secs);
        while Instant::now() < deadline {
            if let Some(ev) = e.recv_timeout(Duration::from_millis(50)) {
                if f(&ev) {
                    return ev;
                }
            }
        }
        panic!("timeout waiting for event");
    }

    fn server(cfg_mut: impl FnOnce(&mut ServerConfig)) -> (Engine, String) {
        let mut cfg = ServerConfig::new(Keypair::generate());
        cfg.port = 0;
        cfg.try_map = false;
        cfg.workers = 4; // every engine test runs the multi-core server unless it says otherwise
        cfg_mut(&mut cfg);
        let s = Engine::start_server(cfg).unwrap();
        let ticket = match wait(&s, 5, |e| matches!(e, EngineEvent::Ready { .. })) {
            EngineEvent::Ready { ticket, .. } => ticket.unwrap(),
            _ => unreachable!(),
        };
        (s, ticket)
    }

    #[test]
    fn falls_back_to_website_relay_when_punching_fails() {
        // PEEROVA_TEST_RELAY=https://… runs this against a real deployed relay.
        let base = std::env::var("PEEROVA_TEST_RELAY").unwrap_or_else(|_| crate::relay::testserver::start());
        let b2 = base.clone();
        let (s, ticket) = server(move |c| c.relay = Some(b2));
        // Ticket whose only candidate is unreachable: direct P2P can never succeed.
        let mut t = Ticket::decode(&ticket).unwrap();
        t.candidates = vec![Candidate { kind: CandKind::Host, addr: "192.0.2.1:9".parse().unwrap() }];
        t.rdv = true;
        t.tcp = false;
        let cfg = ClientConfig { rendezvous: Some("http://127.0.0.1:1".into()), relay: Some(base), ..Default::default() };
        let c = Engine::connect(&t.encode(), cfg).unwrap();
        let session = match wait(&c, 5, |e| matches!(e, EngineEvent::Ready { .. })) {
            EngineEvent::Ready { session, .. } => session,
            _ => unreachable!(),
        };
        // What the website rendezvous would hand the server.
        let ep = crate::rdv::Endpoint { ip: "127.0.0.2".parse().unwrap(), port: 9, lan: vec![], v6: vec![], relay: None };
        s.handle.send(Cmd::RdvClients(vec![crate::rdv::ClientReq { session, ep, hints: Default::default() }]));
        let conn = match wait(&c, 15, |e| matches!(e, EngineEvent::Connected { .. })) {
            EngineEvent::Connected { conn, path, .. } => {
                assert_eq!(path, "relay");
                conn
            }
            _ => unreachable!(),
        };
        // Realtime datagrams flow both ways through the relay.
        let t0 = Instant::now();
        c.handle.send(Cmd::Datagram { conn, data: b"via-relay".to_vec() });
        let sconn = match wait(&s, 5, |e| matches!(e, EngineEvent::Datagram { data, .. } if data == b"via-relay")) {
            EngineEvent::Datagram { conn, .. } => conn,
            _ => unreachable!(),
        };
        s.handle.send(Cmd::Datagram { conn: sconn, data: b"back".to_vec() });
        wait(&c, 5, |e| matches!(e, EngineEvent::Datagram { data, .. } if data == b"back"));
        assert!(t0.elapsed() < Duration::from_millis(200), "relay round trip too slow: {:?}", t0.elapsed());
    }

    /// Client + server whose direct path can never work, with the given relay list.
    fn relay_only_pair(relays: String) -> (Engine, Engine, u32) {
        let r2 = relays.clone();
        let (s, ticket) = server(move |c| c.relay = Some(r2));
        let mut t = Ticket::decode(&ticket).unwrap();
        t.candidates = vec![Candidate { kind: CandKind::Host, addr: "192.0.2.1:9".parse().unwrap() }];
        t.rdv = true;
        t.tcp = false;
        let cfg = ClientConfig { rendezvous: Some("http://127.0.0.1:1".into()), relay: Some(relays), ..Default::default() };
        let c = Engine::connect(&t.encode(), cfg).unwrap();
        let conn = match wait(&c, 15, |e| matches!(e, EngineEvent::Connected { .. })) {
            EngineEvent::Connected { conn, path, .. } => {
                assert_eq!(path, "relay");
                conn
            }
            _ => unreachable!(),
        };
        (s, c, conn)
    }

    #[test]
    fn hidden_server_never_reveals_its_address() {
        let base = crate::relay::testserver::start();
        let b2 = base.clone();
        let (s, ticket) = server(move |c| {
            c.relay = Some(b2);
            c.hide_ip = true;
        });
        let t = Ticket::decode(&ticket).unwrap();
        assert!(t.hidden && t.candidates.is_empty() && t.supernodes.is_empty() && !t.rdv, "ticket must carry no address");
        let cfg = ClientConfig { relay: Some(base), ..Default::default() };
        let c = Engine::connect(&ticket, cfg).unwrap();
        let conn = match wait(&c, 15, |e| matches!(e, EngineEvent::Connected { .. })) {
            EngineEvent::Connected { conn, path, addr, .. } => {
                assert_eq!(path, "relay");
                assert!(crate::relay::is_relay(&addr), "client only ever talks to the relay");
                conn
            }
            _ => unreachable!(),
        };
        c.handle.send(Cmd::Datagram { conn, data: b"hidden".to_vec() });
        wait(&s, 5, |e| matches!(e, EngineEvent::Datagram { data, .. } if data == b"hidden"));
    }

    #[test]
    fn picks_a_working_relay_among_several() {
        let good = crate::relay::testserver::start();
        // First relay in the list is down: the race must still connect through the second.
        let (s, c, conn) = relay_only_pair(format!("http://127.0.0.1:1,{good}"));
        c.handle.send(Cmd::Datagram { conn, data: b"hi".to_vec() });
        wait(&s, 5, |e| matches!(e, EngineEvent::Datagram { data, .. } if data == b"hi"));
    }

    #[test]
    fn survives_relay_outages_by_failing_over() {
        // Both relays cut every connection after 4 s: the same connection must keep
        // working by moving between relays.
        let a = crate::relay::testserver::start_with(Some(Duration::from_secs(4)));
        let b = crate::relay::testserver::start_with(Some(Duration::from_secs(4)));
        let (s, c, conn) = relay_only_pair(format!("{a},{b}"));
        let t0 = Instant::now();
        let mut late = 0;
        let mut n = 0u32;
        while t0.elapsed() < Duration::from_secs(12) {
            n += 1;
            c.handle.send(Cmd::Datagram { conn, data: n.to_be_bytes().to_vec() });
            let deadline = Instant::now() + Duration::from_millis(100);
            while Instant::now() < deadline {
                if let Some(EngineEvent::Datagram { data, .. }) = s.recv_timeout(Duration::from_millis(20)) {
                    if t0.elapsed() > Duration::from_secs(8) && data.len() == 4 {
                        late += 1;
                    }
                }
            }
            while c.recv_timeout(Duration::from_millis(1)).is_some() {}
        }
        assert!(late > 10, "traffic must keep flowing after relay outages (got {late} late datagrams)");
    }

    /// UDP proxy that drops everything until `open_after`, then forwards both ways.
    fn flaky_udp_proxy(server: SocketAddr, open_after: Duration) -> SocketAddr {
        let sock = std::net::UdpSocket::bind("127.0.0.1:0").unwrap();
        let addr = sock.local_addr().unwrap();
        let up = std::net::UdpSocket::bind("127.0.0.1:0").unwrap();
        let (s2, u2) = (sock.try_clone().unwrap(), up.try_clone().unwrap());
        let start = Instant::now();
        let client: Arc<Mutex<Option<SocketAddr>>> = Arc::new(Mutex::new(None));
        let c2 = client.clone();
        thread::spawn(move || {
            let mut b = [0u8; 2048];
            while let Ok((n, from)) = sock.recv_from(&mut b) {
                *client.lock().unwrap() = Some(from);
                if start.elapsed() >= open_after {
                    let _ = up.send_to(&b[..n], server);
                }
            }
        });
        thread::spawn(move || {
            let mut b = [0u8; 2048];
            while let Ok((n, _)) = u2.recv_from(&mut b) {
                if let (true, Some(c)) = (start.elapsed() >= open_after, *c2.lock().unwrap()) {
                    let _ = s2.send_to(&b[..n], c);
                }
            }
        });
        addr
    }

    #[test]
    fn relay_first_then_upgrades_to_direct_p2p() {
        let base = crate::relay::testserver::start();
        let b2 = base.clone();
        let (s, ticket) = server(move |c| c.relay = Some(b2));
        let mut t = Ticket::decode(&ticket).unwrap();
        let real = t.candidates.iter().find(|c| c.addr.is_ipv4()).unwrap().addr;
        let real = SocketAddr::new("127.0.0.1".parse().unwrap(), real.port());
        // Direct path blocked for 6 s (punching fails → relay), then the network opens up.
        let proxy = flaky_udp_proxy(real, Duration::from_secs(6));
        t.candidates = vec![Candidate { kind: CandKind::Host, addr: proxy }];
        t.rdv = true;
        t.tcp = false;
        let cfg = ClientConfig { rendezvous: Some("http://127.0.0.1:1".into()), relay: Some(base), timeout: Duration::from_secs(30), ..Default::default() };
        let c = Engine::connect(&t.encode(), cfg).unwrap();
        let session = match wait(&c, 5, |e| matches!(e, EngineEvent::Ready { .. })) {
            EngineEvent::Ready { session, .. } => session,
            _ => unreachable!(),
        };
        let ep = crate::rdv::Endpoint { ip: "127.0.0.3".parse().unwrap(), port: 9, lan: vec![], v6: vec![], relay: None };
        s.handle.send(Cmd::RdvClients(vec![crate::rdv::ClientReq { session, ep, hints: Default::default() }]));
        let conn = match wait(&c, 15, |e| matches!(e, EngineEvent::Connected { .. })) {
            EngineEvent::Connected { conn, path, .. } => {
                assert_eq!(path, "relay");
                conn
            }
            _ => unreachable!(),
        };
        // Keep traffic flowing; the engine must move to the direct path by itself.
        let deadline = Instant::now() + Duration::from_secs(20);
        let mut upgraded = false;
        while Instant::now() < deadline && !upgraded {
            c.handle.send(Cmd::Datagram { conn, data: b"tick".to_vec() });
            if let Some(EngineEvent::Log(m)) = c.recv_timeout(Duration::from_millis(200)) {
                upgraded = m.contains("sang kết nối trực tiếp");
            }
            while let Some(EngineEvent::Log(m)) = s.recv_timeout(Duration::from_millis(1)) {
                let _ = m;
            }
        }
        assert!(upgraded, "connection should leave the relay once a direct path works");
        // Same connection keeps working over the direct path.
        c.handle.send(Cmd::Datagram { conn, data: b"direct".to_vec() });
        wait(&s, 5, |e| matches!(e, EngineEvent::Datagram { data, .. } if data == b"direct"));
    }

    #[test]
    fn connect_stream_echo_and_datagram() {
        for workers in [1, 4] {
            stream_echo_and_datagram(workers);
        }
    }

    fn stream_echo_and_datagram(workers: usize) {
        let (s, ticket) = server(|c| c.workers = workers);
        assert_eq!(s.workers(), workers);
        let c = Engine::connect(&ticket, ClientConfig::default()).unwrap();
        let conn = match wait(&c, 10, |e| matches!(e, EngineEvent::Connected { .. })) {
            EngineEvent::Connected { conn, .. } => conn,
            _ => unreachable!(),
        };
        let sid = c.handle.alloc_stream();
        let big: Vec<u8> = (0..300_000).map(|i| (i % 253) as u8).collect();
        c.handle.send(Cmd::StreamWrite { conn, id: sid, data: big.clone() });
        c.handle.send(Cmd::StreamFinish { conn, id: sid });
        c.handle.send(Cmd::Datagram { conn, data: b"dgram".to_vec() });
        let mut got = vec![];
        let mut sconn = 0;
        let mut dgram = false;
        let deadline = Instant::now() + Duration::from_secs(10);
        while Instant::now() < deadline {
            match s.recv_timeout(Duration::from_millis(50)) {
                Some(EngineEvent::Connected { conn, .. }) => sconn = conn,
                Some(EngineEvent::StreamData { conn, id, data }) => {
                    s.handle.send(Cmd::StreamConsumed { conn, id, n: data.len() });
                    got.extend(data);
                }
                Some(EngineEvent::Datagram { data, .. }) => dgram = data == b"dgram",
                Some(EngineEvent::StreamFin { conn, id }) => {
                    assert_eq!(id, sid);
                    s.handle.send(Cmd::StreamWrite { conn, id, data: b"ok".to_vec() });
                    s.handle.send(Cmd::StreamFinish { conn, id });
                    break;
                }
                _ => {}
            }
        }
        assert!(sconn != 0);
        assert_eq!(got.len(), big.len());
        assert_eq!(got, big);
        assert!(dgram);
        wait(&c, 5, |e| matches!(e, EngineEvent::StreamData { data, .. } if data == b"ok"));
    }

    #[test]
    fn wrong_token_is_refused_with_a_reason() {
        let (_s, ticket) = server(|c| c.access = Access::Token(b"secret".to_vec()));
        let cfg = ClientConfig { token: b"wrong".to_vec(), timeout: Duration::from_secs(20), ..Default::default() };
        let t0 = Instant::now();
        let c = Engine::connect(&ticket, cfg).unwrap();
        match wait(&c, 5, |e| matches!(e, EngineEvent::ConnectFailed(_))) {
            EngineEvent::ConnectFailed(m) => assert!(m.contains("từ chối truy cập"), "{m}"),
            _ => unreachable!(),
        }
        assert!(t0.elapsed() < Duration::from_secs(2), "ticket holders get an immediate answer");
        let cfg = ClientConfig { token: b"secret".to_vec(), ..Default::default() };
        let c = Engine::connect(&ticket, cfg).unwrap();
        wait(&c, 5, |e| matches!(e, EngineEvent::Connected { .. }));
    }

    #[test]
    fn ccu_limit_enforced() {
        let (_s, ticket) = server(|c| c.max_ccu = 1);
        let a = Engine::connect(&ticket, ClientConfig::default()).unwrap();
        wait(&a, 5, |e| matches!(e, EngineEvent::Connected { .. }));
        let b = Engine::connect(&ticket, ClientConfig { timeout: Duration::from_secs(2), ..Default::default() }).unwrap();
        wait(&b, 5, |e| matches!(e, EngineEvent::ConnectFailed(_)));
    }

    #[test]
    fn full_server_says_so_immediately() {
        let (s, ticket) = server(|c| c.max_ccu = 3);
        let three: Vec<Engine> = (0..3).map(|_| Engine::connect(&ticket, ClientConfig::default()).unwrap()).collect();
        for c in &three {
            connected(c, 10);
        }
        let t0 = Instant::now();
        let fourth = Engine::connect(&ticket, ClientConfig { timeout: Duration::from_secs(20), ..Default::default() }).unwrap();
        match wait(&fourth, 5, |e| matches!(e, EngineEvent::ConnectFailed(_) | EngineEvent::Connected { .. })) {
            EngineEvent::ConnectFailed(m) => assert_eq!(m, "Máy chủ đã đầy (3/3) — thử lại sau"),
            e => panic!("4th player must be refused, got {e:?}"),
        }
        assert!(t0.elapsed() < Duration::from_secs(2), "refusal took {:?}", t0.elapsed());
        // One player leaves → the next one gets in.
        drop(three.into_iter().next());
        wait(&s, 5, |e| matches!(e, EngineEvent::Disconnected { .. }));
        let again = Engine::connect(&ticket, ClientConfig::default()).unwrap();
        connected(&again, 10);
    }

    #[test]
    fn dark_server_ignores_scans() {
        let (s, ticket) = server(|_| {});
        let t = Ticket::decode(&ticket).unwrap();
        let addr = t.candidates[0].addr;
        let probe = std::net::UdpSocket::bind("0.0.0.0:0").unwrap();
        probe.set_read_timeout(Some(Duration::from_millis(700))).unwrap();
        // Garbage, a fake init with wrong key, control packets: no answer at all.
        let fake = crypto::initiate(&Keypair::generate(), &Keypair::generate().public, 1, b"x");
        for pkt in [b"hello".to_vec(), fake.packet, Packet::ReflectReq { txid: 1 }.encode(), Packet::Ping { session: 1, nonce: 1 }.encode()] {
            probe.send_to(&pkt, addr).unwrap();
        }
        let mut buf = [0u8; 2048];
        assert!(probe.recv_from(&mut buf).is_err(), "dark server must not answer");
        assert!(s.handle.stats.lock().unwrap().dropped_unauthenticated >= 2);
    }

    #[test]
    fn tcp_fallback_when_udp_blocked() {
        let (s, ticket) = server(|_| {});
        assert!(Ticket::decode(&ticket).unwrap().tcp);
        let c = Engine::connect(&ticket, ClientConfig { force_tcp: true, ..Default::default() }).unwrap();
        let conn = match wait(&c, 10, |e| matches!(e, EngineEvent::Connected { .. })) {
            EngineEvent::Connected { conn, path, .. } => {
                assert_eq!(path, "tcp");
                conn
            }
            _ => unreachable!(),
        };
        let sid = c.handle.alloc_stream();
        let data: Vec<u8> = (0..200_000).map(|i| (i % 249) as u8).collect();
        c.handle.send(Cmd::StreamWrite { conn, id: sid, data: data.clone() });
        let mut got = vec![];
        let deadline = Instant::now() + Duration::from_secs(10);
        while got.len() < data.len() && Instant::now() < deadline {
            if let Some(EngineEvent::StreamData { conn, id, data: d }) = s.recv_timeout(Duration::from_millis(50)) {
                s.handle.send(Cmd::StreamConsumed { conn, id, n: d.len() });
                got.extend(d);
            }
        }
        assert_eq!(got, data);
    }

    #[test]
    fn failover_to_live_instance_of_same_key() {
        // Two machines run the SAME server key; one is down (e.g. under DDoS).
        let key = Keypair::generate();
        let mut cfg = ServerConfig::new(key.clone());
        cfg.port = 0;
        cfg.try_map = false;
        let live = Engine::start_server(cfg).unwrap();
        let live_ticket = match wait(&live, 5, |e| matches!(e, EngineEvent::Ready { .. })) {
            EngineEvent::Ready { ticket, .. } => Ticket::decode(&ticket.unwrap()).unwrap(),
            _ => unreachable!(),
        };
        let dead = std::net::UdpSocket::bind("127.0.0.1:0").unwrap(); // silent black hole
        let mut merged = live_ticket.clone();
        merged.candidates.insert(0, crate::wire::Candidate { kind: CandKind::Host, addr: dead.local_addr().unwrap() });
        let c = Engine::connect(&merged.encode(), ClientConfig::default()).unwrap();
        match wait(&c, 10, |e| matches!(e, EngineEvent::Connected { .. })) {
            EngineEvent::Connected { addr, .. } => assert_ne!(addr, dead.local_addr().unwrap()),
            _ => unreachable!(),
        }
    }

    #[test]
    fn garbage_flood_gets_ip_blocked_but_peers_keep_working() {
        let (s, ticket) = server(|_| {});
        let addr = Ticket::decode(&ticket).unwrap().candidates[0].addr;
        let c = Engine::connect(&ticket, ClientConfig::default()).unwrap();
        let conn = match wait(&c, 5, |e| matches!(e, EngineEvent::Connected { .. })) {
            EngineEvent::Connected { conn, .. } => conn,
            _ => unreachable!(),
        };
        // Attacker on another socket floods forged data packets with a guessed session index.
        let atk = std::net::UdpSocket::bind("127.0.0.2:0").unwrap();
        let mut pkt = vec![crypto::T_DATA];
        pkt.extend_from_slice(&conn.to_le_bytes());
        pkt.extend_from_slice(&[0u8; 60]);
        for _ in 0..3000 {
            let _ = atk.send_to(&pkt, addr);
        }
        let deadline = Instant::now() + Duration::from_secs(3);
        let mut blocked = false;
        while Instant::now() < deadline && !blocked {
            if let Some(EngineEvent::Log(m)) = s.recv_timeout(Duration::from_millis(50)) {
                blocked = m.contains("chặn 127.0.0.2");
            }
        }
        assert!(blocked, "flooding IP should be blocked");
        // The legitimate client still works.
        c.handle.send(Cmd::Datagram { conn, data: b"still-here".to_vec() });
        wait(&s, 5, |e| matches!(e, EngineEvent::Datagram { data, .. } if data == b"still-here"));
    }

    #[test]
    fn port_prediction() {
        let p = predict_ports(&"14.160.1.2:40000".parse().unwrap());
        assert_eq!(p.len(), PREDICT_PORTS as usize);
        assert_eq!(p[0].port(), 40001);
        assert!(predict_ports(&"192.168.1.2:40000".parse().unwrap()).is_empty());
    }

    fn signed_lease(seed: u8, spk: &[u8; 32]) -> ([u8; 32], String) {
        use ed25519_dalek::{Signer, SigningKey};
        let sk = SigningKey::from_bytes(&[seed; 32]);
        let payload = format!(
            r#"{{"v":1,"lid":"lic_t","pid":"prj_t","plan":"indie","srv":3,"ccu":200,"mid":"m","iat":1,"exp":{},"spk":"{}"}}"#,
            crate::license::now_unix() + 3600,
            crate::util::b64_encode(spk)
        );
        let signed = format!("pvl1.{}", crate::util::b64_encode(payload.as_bytes()));
        let sig = sk.sign(signed.as_bytes()).to_bytes();
        (sk.verifying_key().to_bytes(), format!("{signed}.{}", crate::util::b64_encode(&sig)))
    }

    #[test]
    fn licensed_supernode_rejects_unlicensed_servers() {
        let sn_key = Keypair::generate();
        let (authority, sn_lease) = signed_lease(5, &sn_key.public);
        let mut cfg = ServerConfig::new(sn_key);
        cfg.port = 0;
        cfg.try_map = false;
        cfg.supernode_mode = true;
        cfg.authority = Some(authority);
        cfg.lease = Some(sn_lease);
        let sn = Engine::start_server(cfg).unwrap();
        let sn_ticket = match wait(&sn, 5, |e| matches!(e, EngineEvent::Ready { .. })) {
            EngineEvent::Ready { ticket, .. } => ticket.unwrap(),
            _ => unreachable!(),
        };
        let sn_addr = Ticket::decode(&sn_ticket).unwrap().candidates[0].addr;

        // Unlicensed (e.g. cracked) server: registration silently ignored → private ticket unusable.
        let (_bad, bad_ticket) = server(|c| {
            c.supernodes = vec![sn_addr];
            c.private_ticket = true;
        });
        let c = Engine::connect(&bad_ticket, ClientConfig { timeout: Duration::from_secs(3), ..Default::default() }).unwrap();
        wait(&c, 6, |e| matches!(e, EngineEvent::ConnectFailed(_)));

        // Licensed server with a lease bound to its key: works.
        let key = Keypair::generate();
        let (_, lease) = signed_lease(5, &key.public);
        let mut cfg = ServerConfig::new(key);
        cfg.port = 0;
        cfg.try_map = false;
        cfg.supernodes = vec![sn_addr];
        cfg.private_ticket = true;
        cfg.lease = Some(lease);
        let good = Engine::start_server(cfg).unwrap();
        let good_ticket = match wait(&good, 5, |e| matches!(e, EngineEvent::Ready { .. })) {
            EngineEvent::Ready { ticket, .. } => ticket.unwrap(),
            _ => unreachable!(),
        };
        let c = Engine::connect(&good_ticket, ClientConfig::default()).unwrap();
        wait(&c, 10, |e| matches!(e, EngineEvent::Connected { .. }));

        // A lease stolen from another server (spk mismatch) is rejected too.
        let thief = Keypair::generate();
        let (_, stolen) = signed_lease(5, &Keypair::generate().public);
        let mut cfg = ServerConfig::new(thief);
        cfg.port = 0;
        cfg.try_map = false;
        cfg.supernodes = vec![sn_addr];
        cfg.private_ticket = true;
        cfg.lease = Some(stolen);
        let t = Engine::start_server(cfg).unwrap();
        let t_ticket = match wait(&t, 5, |e| matches!(e, EngineEvent::Ready { .. })) {
            EngineEvent::Ready { ticket, .. } => ticket.unwrap(),
            _ => unreachable!(),
        };
        let c = Engine::connect(&t_ticket, ClientConfig { timeout: Duration::from_secs(3), ..Default::default() }).unwrap();
        wait(&c, 6, |e| matches!(e, EngineEvent::ConnectFailed(_)));
    }

    #[test]
    fn supernode_introduction_private_ticket() {
        let (_sn, sn_ticket) = server(|c| c.supernode_mode = true);
        let sn_addr = Ticket::decode(&sn_ticket).unwrap().candidates[0].addr;
        let (_s, ticket) = server(|c| {
            c.supernodes = vec![sn_addr];
            c.private_ticket = true;
        });
        let t = Ticket::decode(&ticket).unwrap();
        assert!(t.candidates.is_empty() && t.private);
        let c = Engine::connect(&ticket, ClientConfig::default()).unwrap();
        match wait(&c, 10, |e| matches!(e, EngineEvent::Connected { .. })) {
            EngineEvent::Connected { path, .. } => assert_eq!(path, "reflexive"),
            _ => unreachable!(),
        }
    }

    // ------------------------------------------------------------ multi-core server

    fn connected(e: &Engine, secs: u64) -> u32 {
        match wait(e, secs, |e| matches!(e, EngineEvent::Connected { .. })) {
            EngineEvent::Connected { conn, .. } => conn,
            _ => unreachable!(),
        }
    }

    /// Collect server events until `done` says stop (or `secs` elapse).
    fn pump(s: &Engine, secs: u64, mut f: impl FnMut(EngineEvent) -> bool) {
        let deadline = Instant::now() + Duration::from_secs(secs);
        while Instant::now() < deadline {
            if let Some(ev) = s.recv_timeout(Duration::from_millis(20)) {
                if f(ev) {
                    return;
                }
            }
        }
    }

    #[test]
    fn sharded_server_serves_many_clients() {
        const N: usize = 24;
        let (s, ticket) = server(|c| c.max_ccu = 1000);
        assert_eq!(s.workers(), 4);
        let clients: Vec<Engine> = (0..N).map(|_| Engine::connect(&ticket, ClientConfig::default()).unwrap()).collect();
        let conns: Vec<u32> = clients.iter().map(|c| connected(c, 10)).collect();
        // Every client says hello on a datagram and on a reliable stream (50 KB).
        let big: Vec<u8> = (0..50_000).map(|i| (i % 251) as u8).collect();
        for (i, (c, &conn)) in clients.iter().zip(&conns).enumerate() {
            c.handle.send(Cmd::Datagram { conn, data: format!("dg{i}").into_bytes() });
            let sid = c.handle.alloc_stream();
            let mut d = (i as u32).to_be_bytes().to_vec();
            d.extend_from_slice(&big);
            c.handle.send(Cmd::StreamWrite { conn, id: sid, data: d });
            c.handle.send(Cmd::StreamFinish { conn, id: sid });
        }
        // Server: one connection per client, spread over the shards; echo everything back.
        let mut sconns = std::collections::HashSet::new();
        let mut dgrams = 0;
        let mut streams: HashMap<(u32, u32), Vec<u8>> = HashMap::new();
        let mut fins = 0;
        pump(&s, 20, |ev| {
            match ev {
                EngineEvent::Connected { conn, .. } => {
                    sconns.insert(conn);
                }
                EngineEvent::Datagram { conn, data } => {
                    assert!(data.starts_with(b"dg"));
                    dgrams += 1;
                    s.handle.send(Cmd::Datagram { conn, data });
                }
                EngineEvent::StreamData { conn, id, data } => {
                    s.handle.send(Cmd::StreamConsumed { conn, id, n: data.len() });
                    streams.entry((conn, id)).or_default().extend(data);
                }
                EngineEvent::StreamFin { conn, id } => {
                    let got = streams.remove(&(conn, id)).unwrap_or_default();
                    assert_eq!(&got[4..], &big[..], "stream from {conn:08x} corrupted");
                    s.handle.send(Cmd::StreamWrite { conn, id, data: got[..4].to_vec() });
                    s.handle.send(Cmd::StreamFinish { conn, id });
                    fins += 1;
                }
                _ => {}
            }
            sconns.len() == N && dgrams == N && fins == N
        });
        assert_eq!((sconns.len(), dgrams, fins), (N, N, N));
        let shards: std::collections::HashSet<u32> = sconns.iter().map(|c| c % 4).collect();
        assert!(shards.len() >= 3, "connections should be spread over the shards: {shards:?}");
        assert_eq!(s.handle.stats.lock().unwrap().ccu, N);
        // Each client gets its own datagram and its own stream reply back.
        for (i, c) in clients.iter().enumerate() {
            let want = format!("dg{i}").into_bytes();
            let mut got_dg = false;
            let mut reply = vec![];
            let deadline = Instant::now() + Duration::from_secs(10);
            while Instant::now() < deadline && !(got_dg && reply.len() == 4) {
                match c.recv_timeout(Duration::from_millis(20)) {
                    Some(EngineEvent::Datagram { data, .. }) => got_dg |= data == want,
                    Some(EngineEvent::StreamData { conn, id, data }) => {
                        c.handle.send(Cmd::StreamConsumed { conn, id, n: data.len() });
                        reply.extend(data);
                    }
                    _ => {}
                }
            }
            assert!(got_dg, "client {i}: datagram echo missing");
            assert_eq!(reply, (i as u32).to_be_bytes(), "client {i}: stream reply");
        }
        // Clients leave: the server reports every disconnect and CCU drops to zero.
        drop(clients);
        let mut gone = std::collections::HashSet::new();
        pump(&s, 10, |ev| {
            if let EngineEvent::Disconnected { conn, .. } = ev {
                assert!(sconns.contains(&conn));
                gone.insert(conn);
            }
            gone.len() == N
        });
        assert_eq!(gone.len(), N);
        assert_eq!(s.handle.stats.lock().unwrap().ccu, 0);
    }

    #[test]
    fn ccu_limit_enforced_across_shards() {
        let (s, ticket) = server(|c| c.max_ccu = 5);
        let cfg = || ClientConfig { timeout: Duration::from_secs(4), ..Default::default() };
        let clients: Vec<Engine> = (0..12).map(|_| Engine::connect(&ticket, cfg()).unwrap()).collect();
        let (mut ok, mut failed) = (0, 0);
        for c in &clients {
            match wait(c, 8, |e| matches!(e, EngineEvent::Connected { .. } | EngineEvent::ConnectFailed(_))) {
                EngineEvent::Connected { .. } => ok += 1,
                _ => failed += 1,
            }
        }
        assert_eq!((ok, failed), (5, 7));
        assert_eq!(s.handle.stats.lock().unwrap().ccu, 5);
        // A slot frees up when someone leaves (whatever shard it was on).
        drop(clients);
        wait(&s, 5, |e| matches!(e, EngineEvent::Disconnected { .. }));
        let c = Engine::connect(&ticket, cfg()).unwrap();
        connected(&c, 8);
    }

    #[test]
    fn revocation_applies_on_every_shard() {
        let seed = [7u8; 32];
        let authority = crate::access::public_key(&seed);
        let (s, ticket) = server(|c| {
            c.max_ccu = 100;
            c.access = Access::Signed { authority, revoked: Default::default() };
        });
        let now = crate::license::now_unix();
        let token = |sub: &str| crate::access::issue(&seed, sub, &["tcp:*".into()], 600, now).into_bytes();
        let mk = |sub: &str| Engine::connect(&ticket, ClientConfig { token: token(sub), timeout: Duration::from_secs(3), ..Default::default() }).unwrap();
        let bad: Vec<Engine> = (0..8).map(|_| mk("mallory")).collect();
        let good: Vec<Engine> = (0..4).map(|_| mk("alice")).collect();
        let good_conns: Vec<u32> = good.iter().map(|c| connected(c, 10)).collect();
        for c in &bad {
            connected(c, 10);
        }
        let mut subjects: HashMap<u32, String> = HashMap::new();
        pump(&s, 10, |ev| {
            if let EngineEvent::Connected { conn, subject, .. } = ev {
                subjects.insert(conn, subject.unwrap());
            }
            subjects.len() == 12
        });
        let bad_conns: std::collections::HashSet<u32> = subjects.iter().filter(|(_, s)| *s == "mallory").map(|(c, _)| *c).collect();
        assert!(bad_conns.iter().map(|c| c % 4).collect::<std::collections::HashSet<_>>().len() >= 2);
        s.handle.send(Cmd::SetRevoked(["mallory".to_string()].into_iter().collect()));
        let mut kicked = std::collections::HashSet::new();
        pump(&s, 10, |ev| {
            if let EngineEvent::Disconnected { conn, .. } = ev {
                assert!(bad_conns.contains(&conn), "only revoked subjects are kicked");
                kicked.insert(conn);
            }
            kicked.len() == bad_conns.len()
        });
        assert_eq!(kicked, bad_conns);
        // The revoked subject can't come back on any shard; others still work.
        let retry: Vec<Engine> = (0..6).map(|_| mk("mallory")).collect();
        for c in &retry {
            wait(c, 8, |e| matches!(e, EngineEvent::ConnectFailed(_)));
        }
        for (c, &conn) in good.iter().zip(&good_conns) {
            c.handle.send(Cmd::Datagram { conn, data: b"still-in".to_vec() });
        }
        let mut n = 0;
        pump(&s, 5, |ev| {
            n += matches!(ev, EngineEvent::Datagram { ref data, .. } if data == b"still-in") as usize;
            n == good.len()
        });
        assert_eq!(n, good.len());
    }

    /// Raw handshake init for `key` that the server routes to `shard` (of `k`).
    fn init_for_shard(key: &Keypair, server_pub: &[u8; 32], shard: usize, k: usize) -> Vec<u8> {
        loop {
            let payload = now_unix_nanos().to_be_bytes();
            let i = crypto::initiate(key, server_pub, crypto::random_u32() | 1, &payload);
            if Router::shard_for(&i.packet, k) == shard {
                return i.packet;
            }
        }
    }

    fn answers(sock: &std::net::UdpSocket, to: SocketAddr, pkt: &[u8]) -> bool {
        sock.send_to(pkt, to).unwrap();
        let mut b = [0u8; 2048];
        let deadline = Instant::now() + Duration::from_millis(800);
        while Instant::now() < deadline {
            if let Ok((n, _)) = sock.recv_from(&mut b) {
                if n > 0 && b[0] == T_RESP {
                    return true;
                }
            }
        }
        false
    }

    #[test]
    fn flood_ban_applies_on_every_shard() {
        let (s, ticket) = server(|c| {
            c.max_ccu = 100;
            c.flood_pps = 40;
        });
        let t = Ticket::decode(&ticket).unwrap();
        let addr = t.candidates.iter().find(|c| c.addr.is_ipv4()).unwrap().addr;
        let key = Keypair::generate();
        let c = Engine::connect(&ticket, ClientConfig { key: key.clone(), ..Default::default() }).unwrap();
        let conn = connected(&c, 10);
        // Hammer the server well above the (lowered) flood threshold until it bans the key.
        let t0 = Instant::now();
        let mut banned = false;
        while t0.elapsed() < Duration::from_secs(10) && !banned {
            c.handle.send(Cmd::Datagram { conn, data: vec![0; 32] });
            thread::sleep(Duration::from_millis(4));
            while let Some(ev) = s.try_recv() {
                banned |= matches!(ev, EngineEvent::Log(ref m) if m.contains("dồn dập"));
            }
        }
        assert!(banned, "flooding peer must be banned");
        // The key is refused by every shard; a fresh key is still accepted by every shard.
        let sock = std::net::UdpSocket::bind("0.0.0.0:0").unwrap();
        sock.set_read_timeout(Some(Duration::from_millis(50))).unwrap();
        let fresh = Keypair::generate();
        for shard in 0..4 {
            assert!(!answers(&sock, addr, &init_for_shard(&key, &t.server_pub, shard, 4)), "shard {shard} accepted a banned key");
            assert!(answers(&sock, addr, &init_for_shard(&fresh, &t.server_pub, shard, 4)), "shard {shard} must still accept others");
        }
    }

    #[test]
    fn reconnect_on_another_shard_announces_disconnect_first() {
        let (s, ticket) = server(|c| c.max_ccu = 100);
        let key = Keypair::generate();
        let mut prev: Option<(Engine, u32)> = None;
        let mut crossed = 0;
        for _ in 0..12 {
            let c = Engine::connect(&ticket, ClientConfig { key: key.clone(), ..Default::default() }).unwrap();
            connected(&c, 10);
            // Server side: Disconnected(old) must come before Connected(new).
            let mut seen_old_gone = prev.is_none();
            let new = match wait(&s, 10, |e| {
                if let (EngineEvent::Disconnected { conn, .. }, Some((_, old))) = (e, &prev) {
                    seen_old_gone |= conn == old;
                }
                matches!(e, EngineEvent::Connected { .. })
            }) {
                EngineEvent::Connected { conn, .. } => conn,
                _ => unreachable!(),
            };
            assert!(seen_old_gone, "Connected(new) arrived before Disconnected(old)");
            if let Some((_, old)) = &prev {
                crossed += (old % 4 != new % 4) as usize;
            }
            assert_eq!(s.handle.stats.lock().unwrap().ccu, 1);
            // Keep the old engine alive (no clean close) so the server must replace it.
            prev = Some((c, new));
            if crossed >= 3 {
                break;
            }
        }
        assert!(crossed >= 1, "test never exercised a cross-shard reconnect");
        // The latest connection works.
        let (c, conn) = prev.unwrap();
        s.handle.send(Cmd::Datagram { conn, data: b"latest".to_vec() });
        wait(&c, 5, |e| matches!(e, EngineEvent::Datagram { data, .. } if data == b"latest"));
    }

    #[test]
    fn session_indices_belong_to_their_shard() {
        for k in [1usize, 3, 4, 7, 16] {
            let txs = (0..k).map(|_| mpsc::channel::<Input>().0).collect();
            let router = Arc::new(Router { txs, queued: (0..k).map(|_| Padded::default()).collect() });
            for shard in 0..k {
                let c = test_core(router.clone(), shard);
                for _ in 0..200 {
                    let idx = c.alloc_index();
                    assert!(idx != 0 && idx as usize % k == shard);
                    let mut pkt = vec![T_DATA];
                    pkt.extend_from_slice(&idx.to_le_bytes());
                    pkt.extend_from_slice(&[0u8; 64]);
                    assert_eq!(Router::shard_for(&pkt, k), shard);
                }
            }
        }
    }

    fn test_core(router: Arc<Router>, shard: usize) -> Core {
        let (etx, _) = mpsc::channel();
        Core {
            next_sweep: Instant::now(),
            next_upgrade_sweep: Instant::now(),
            timers: Default::default(),
            guard: Guard::default(),
            lease_slot: Arc::default(),
            shard,
            router,
            shared: Arc::new(Shared { ccu: AtomicUsize::new(0), max_ccu: AtomicUsize::new(1), index: Mutex::default() }),
            flood_pps: PEER_MAX_PPS,
            role: Role::Server,
            node: Arc::new(Node::bind(0, None, Arc::new(|_, _, _| {})).unwrap()),
            key: Keypair::generate(),
            out: etx,
            stats: Arc::default(),
            peers: HashMap::new(),
            dirty: Default::default(),
            srv: None,
            cli: None,
        }
    }
}
