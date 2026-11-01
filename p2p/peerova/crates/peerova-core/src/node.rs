//! A node owns one IPv4 and one IPv6 UDP socket. The same sockets are used for
//! router mapping, reflection and punching, so the NAT binding stays identical
//! across all of them (essential for hole punching). Works on Linux, Windows, macOS.
//!
//! TCP fallback: for networks that block UDP (corporate/school Wi-Fi), the same
//! packets travel over a direct TCP connection framed as `len u16 | packet`.
//! Still peer-to-peer (); the server stays silent to anything without a
//! valid handshake, TCP or UDP.
//!
//! TCP multipath (smoothness): the client opens several parallel TCP connections.
//! Packets are striped across them, small packets (inputs, acks) are duplicated
//! on two, and each connection has its own bounded queue where packets older
//! than 150 ms are dropped. A loss on one TCP connection therefore no longer
//! stalls everything behind it (head-of-line blocking is confined to one lane),
//! and a stuck connection can never block the engine.

use std::collections::HashMap;
use std::io::{self, Read, Write};
use std::net::{
    IpAddr, Ipv4Addr, Ipv6Addr, Shutdown, SocketAddr, TcpListener, TcpStream, UdpSocket,
};
use std::sync::atomic::AtomicU64;
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::mpsc::{sync_channel, SyncSender, TrySendError};
use std::sync::Arc;
use std::sync::Mutex;
use std::thread;
use std::time::Duration;
use std::time::Instant;

/// Which local transport a packet arrived on / should leave from.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Via {
    /// UDP: 0 = main socket, >0 = birthday "spray" socket id.
    Udp(u16),
    Tcp,
}

/// (from, packet, via)
pub type Sink = Arc<dyn Fn(SocketAddr, &[u8], Via) + Send + Sync>;

/// Birthday-punching sockets: each one gets its own NAT mapping on a
/// symmetric NAT, multiplying the chance that the peer's probes hit one.
pub const SPRAY_SOCKETS: usize = 128;

struct TcpLane {
    id: u64,
    tx: SyncSender<(Instant, Vec<u8>)>,
    stream: TcpStream,
    /// Standby relay room: every message to this peer starts with its 8-byte client id.
    prefix: Option<[u8; 8]>,
}

type TcpMap = Arc<Mutex<HashMap<SocketAddr, Vec<TcpLane>>>>;

/// Parallel TCP connections opened by a client in fallback mode.
pub const TCP_LANES: usize = 3;
const LANE_QUEUE: usize = 256;
const LANE_MAX_AGE: Duration = Duration::from_millis(150);
/// Fallback-route lanes have a longer path (RTT ~100 ms): only drop truly stale data.
const RELAY_MAX_AGE: Duration = Duration::from_millis(600);
/// Deeper queues on fallback lanes: a full queue drops packets, which the transport
/// reads as loss; the stale-age cap above still bounds the added delay.
const RELAY_QUEUE: usize = 4096;
const DUP_MAX: usize = 300;
static LANE_ID: AtomicU64 = AtomicU64::new(1);
/// Small stacks for I/O threads (they never recurse): less memory per connection.
const LANE_STACK: usize = 256 * 1024;

fn spawn_small(f: impl FnOnce() + Send + 'static) {
    let _ = thread::Builder::new().stack_size(LANE_STACK).spawn(f);
}

pub struct Node {
    v4: Option<UdpSocket>,
    v6: Option<UdpSocket>,
    stop: Arc<AtomicBool>,
    sink: Sink,
    tcp: TcpMap,
    rr: AtomicU64,
    spray: Mutex<HashMap<u16, (UdpSocket, Arc<AtomicBool>)>>,
    next_spray: std::sync::atomic::AtomicU16,
}

const MAX_TCP_PEERS: usize = 4096;

/// Register a TCP connection as a lane for `peer`: writer thread + reader thread.
fn add_lane(s: TcpStream, peer: SocketAddr, sink: Sink, map: TcpMap, stop: Arc<AtomicBool>) {
    let (Ok(mut w), Ok(shut)) = (s.try_clone(), s.try_clone()) else {
        return;
    };
    w.set_nodelay(true).ok();
    let id = LANE_ID.fetch_add(1, Ordering::Relaxed);
    let (tx, rx) = sync_channel::<(Instant, Vec<u8>)>(LANE_QUEUE);
    map.lock().unwrap().entry(peer).or_default().push(TcpLane {
        id,
        tx,
        stream: shut,
        prefix: None,
    });
    spawn_small(move || {
        for (t, frame) in rx {
            if t.elapsed() > LANE_MAX_AGE {
                continue; // stale realtime data: dropping beats delaying (reliable data is retransmitted)
            }
            if w.write_all(&frame).is_err() {
                break;
            }
        }
    });
    tcp_reader(s, peer, id, sink, map, stop);
}

fn tcp_reader(
    mut s: TcpStream,
    peer: SocketAddr,
    lane: u64,
    sink: Sink,
    map: TcpMap,
    stop: Arc<AtomicBool>,
) {
    s.set_nodelay(true).ok();
    s.set_read_timeout(Some(Duration::from_secs(60))).ok();
    spawn_small(move || {
        let mut len = [0u8; 2];
        let mut buf = vec![0u8; 65536];
        while !stop.load(Ordering::Relaxed) {
            if s.read_exact(&mut len).is_err() {
                break;
            }
            let n = u16::from_be_bytes(len) as usize;
            if n == 0 || s.read_exact(&mut buf[..n]).is_err() {
                break;
            }
            sink(peer, &buf[..n], Via::Tcp);
        }
        let _ = s.shutdown(Shutdown::Both);
        let mut m = map.lock().unwrap();
        if let Some(v) = m.get_mut(&peer) {
            v.retain(|l| l.id != lane);
            if v.is_empty() {
                m.remove(&peer);
            }
        }
    });
}

/// One relay WebSocket used as a lane. Reader and writer run independently and block
/// on real events only (socket data / queued packets): ~0% CPU when idle, no added delay.
fn relay_lane(
    ws: crate::relay::Ws,
    peer: SocketAddr,
    sink: Sink,
    map: TcpMap,
    stop: Arc<AtomicBool>,
    up: impl Fn(),
) {
    let (Ok(shut), Ok(lane_sock)) = (ws.tcp.try_clone(), ws.tcp.try_clone()) else {
        return;
    };
    let Ok((mut reader, mut writer)) = ws.split() else {
        return;
    };
    let id = LANE_ID.fetch_add(1, Ordering::Relaxed);
    let (tx, rx) = sync_channel::<(Instant, Vec<u8>)>(RELAY_QUEUE);
    map.lock().unwrap().entry(peer).or_default().push(TcpLane {
        id,
        tx,
        stream: lane_sock,
        prefix: None,
    });
    up();
    let _ = thread::Builder::new()
        .name("peerova-relay-tx".into())
        .stack_size(LANE_STACK)
        .spawn(move || {
            let mut out = Vec::with_capacity(16 * 1024);
            while let Ok((t, f)) = rx.recv() {
                out.clear();
                if t.elapsed() <= RELAY_MAX_AGE {
                    out.extend_from_slice(&f);
                }
                // Whatever else is already queued goes in the same message (no waiting).
                while out.len() < 16 * 1024 {
                    match rx.try_recv() {
                        Ok((t, f)) if t.elapsed() <= RELAY_MAX_AGE => out.extend_from_slice(&f),
                        Ok(_) => {}
                        Err(_) => break,
                    }
                }
                if !out.is_empty() && writer.send(&out).is_err() {
                    break;
                }
            }
        });
    let mut msgs = vec![];
    while !stop.load(Ordering::Relaxed) {
        msgs.clear();
        if reader.recv(&mut msgs).is_err() {
            break;
        }
        for m in &msgs {
            let mut b = &m[..];
            while b.len() >= 2 {
                let n = u16::from_be_bytes([b[0], b[1]]) as usize;
                if n == 0 || b.len() < 2 + n {
                    break;
                }
                sink(peer, &b[2..2 + n], Via::Tcp);
                b = &b[2 + n..];
            }
        }
    }
    let _ = shut.shutdown(Shutdown::Both);
    let mut m = map.lock().unwrap();
    if let Some(v) = m.get_mut(&peer) {
        v.retain(|l| l.id != id);
        if v.is_empty() {
            m.remove(&peer);
        }
    }
}

/// One standby connection: demultiplex clients by their 8-byte id.
fn standby_lane(
    ws: crate::relay::Ws,
    sink: &Sink,
    map: &TcpMap,
    stop: &Arc<AtomicBool>,
    cids: &Arc<Mutex<HashMap<[u8; 8], SocketAddr>>>,
) {
    let Ok(sock) = ws.tcp.try_clone() else { return };
    let Ok((mut reader, mut writer)) = ws.split() else {
        return;
    };
    let id = LANE_ID.fetch_add(1, Ordering::Relaxed);
    let (tx, rx) = sync_channel::<(Instant, Vec<u8>)>(RELAY_QUEUE);
    let _ = thread::Builder::new()
        .name("peerova-standby-tx".into())
        .stack_size(LANE_STACK)
        .spawn(move || {
            // Each item is already `cid | len | packet`: one WebSocket message per item.
            while let Ok((t, f)) = rx.recv() {
                if t.elapsed() <= RELAY_MAX_AGE && writer.send(&f).is_err() {
                    break;
                }
            }
        });
    let mut msgs = vec![];
    while !stop.load(Ordering::Relaxed) {
        msgs.clear();
        if reader.recv(&mut msgs).is_err() {
            break;
        }
        for m in &msgs {
            if m.len() < 10 {
                continue;
            }
            let cid: [u8; 8] = m[..8].try_into().unwrap();
            let peer = {
                let mut c = cids.lock().unwrap();
                if c.len() > 65_536 {
                    c.clear();
                }
                *c.entry(cid).or_insert_with(crate::relay::pseudo_addr)
            };
            {
                let mut mp = map.lock().unwrap();
                let lanes = mp.entry(peer).or_default();
                if !lanes.iter().any(|l| l.id == id) {
                    if let Ok(s) = sock.try_clone() {
                        lanes.push(TcpLane {
                            id,
                            tx: tx.clone(),
                            stream: s,
                            prefix: Some(cid),
                        });
                    }
                }
            }
            let mut b = &m[8..];
            while b.len() >= 2 {
                let n = u16::from_be_bytes([b[0], b[1]]) as usize;
                if n == 0 || b.len() < 2 + n {
                    break;
                }
                sink(peer, &b[2..2 + n], Via::Tcp);
                b = &b[2 + n..];
            }
        }
    }
    let _ = sock.shutdown(Shutdown::Both);
    let mut mp = map.lock().unwrap();
    mp.retain(|_, v| {
        v.retain(|l| l.id != id);
        !v.is_empty()
    });
}

fn canonical(a: SocketAddr) -> SocketAddr {
    SocketAddr::new(a.ip().to_canonical(), a.port())
}

fn spawn_recv(sock: UdpSocket, sink: Sink, stop: Arc<AtomicBool>) {
    spawn_recv_as(sock, sink, stop, 0)
}

fn spawn_recv_as(sock: UdpSocket, sink: Sink, stop: Arc<AtomicBool>, id: u16) {
    sock.set_read_timeout(Some(Duration::from_millis(500))).ok();
    thread::Builder::new()
        .name("peerova-udp-rx".into())
        .stack_size(LANE_STACK)
        .spawn(move || {
            let mut buf = vec![0u8; 65536];
            while !stop.load(Ordering::Relaxed) {
                match sock.recv_from(&mut buf) {
                    Ok((n, from)) => sink(canonical(from), &buf[..n], Via::Udp(id)),
                    // Timeout, or Windows reporting ICMP port-unreachable as ConnectionReset.
                    Err(e)
                        if matches!(
                            e.kind(),
                            io::ErrorKind::WouldBlock
                                | io::ErrorKind::TimedOut
                                | io::ErrorKind::ConnectionReset
                        ) => {}
                    Err(_) => thread::sleep(Duration::from_millis(5)),
                }
            }
        })
        .expect("spawn udp thread");
}

impl Node {
    /// `port` 0 = random. `v6` = our global IPv6 (None disables IPv6).
    pub fn bind(port: u16, v6: Option<Ipv6Addr>, sink: Sink) -> io::Result<Node> {
        let s4 = UdpSocket::bind((Ipv4Addr::UNSPECIFIED, port)).ok();
        let p = match &s4 {
            Some(s) => s.local_addr()?.port(),
            None => port,
        };
        // Linux refuses a dual-stack [::]:p when 0.0.0.0:p is taken, so fall
        // back to binding the specific global address (v6-only by nature).
        let s6 = v6.and_then(|ip| {
            UdpSocket::bind((Ipv6Addr::UNSPECIFIED, p))
                .or_else(|_| UdpSocket::bind((ip, p)))
                .or_else(|_| UdpSocket::bind((ip, 0)))
                .ok()
        });
        if s4.is_none() && s6.is_none() {
            return Err(io::Error::new(
                io::ErrorKind::AddrInUse,
                format!("không bind được UDP port {port}"),
            ));
        }
        let stop = Arc::new(AtomicBool::new(false));
        for s in [&s4, &s6].into_iter().flatten() {
            spawn_recv(s.try_clone()?, sink.clone(), stop.clone());
        }
        Ok(Node {
            v4: s4,
            v6: s6,
            stop,
            sink,
            tcp: Arc::new(Mutex::new(HashMap::new())),
            rr: AtomicU64::new(0),
            spray: Mutex::new(HashMap::new()),
            next_spray: std::sync::atomic::AtomicU16::new(1),
        })
    }

    /// Open `n` extra IPv4 UDP sockets for birthday punching. Returns their ids.
    pub fn open_spray(&self, n: usize) -> Vec<u16> {
        let mut ids = vec![];
        let mut m = self.spray.lock().unwrap();
        for _ in 0..n {
            let Ok(s) = UdpSocket::bind((Ipv4Addr::UNSPECIFIED, 0)) else {
                break;
            };
            let Ok(r) = s.try_clone() else { break };
            let id = self.next_spray.fetch_add(1, Ordering::Relaxed);
            let stop = Arc::new(AtomicBool::new(false));
            spawn_recv_as(r, self.sink.clone(), stop.clone(), id);
            m.insert(id, (s, stop));
            ids.push(id);
        }
        ids
    }

    /// Keep only the winning spray socket (if any); close the rest.
    pub fn close_spray_except(&self, keep: Option<u16>) {
        self.spray.lock().unwrap().retain(|id, (_, stop)| {
            let k = Some(*id) == keep;
            if !k {
                stop.store(true, Ordering::Relaxed);
            }
            k
        });
    }

    /// IPv4 send with a small TTL: the packet opens OUR NAT mapping but expires
    /// in the ISP network before it can reach (and pollute) the peer's NAT.
    pub fn send_low_ttl(&self, to: SocketAddr, data: &[u8], ttl: u32) -> io::Result<usize> {
        let Some(s) = self.v4.as_ref().filter(|_| to.is_ipv4()) else {
            return self.send(to, data);
        };
        let old = s.ttl().unwrap_or(64);
        s.set_ttl(ttl)?;
        let r = s.send_to(data, to);
        let _ = s.set_ttl(old);
        r
    }

    /// Like `send_on` but with a low TTL (birthday sockets: open our mappings only).
    pub fn send_on_low_ttl(
        &self,
        via: Via,
        to: SocketAddr,
        data: &[u8],
        ttl: u32,
    ) -> io::Result<usize> {
        match via {
            Via::Udp(0) => self.send_low_ttl(to, data, ttl),
            Via::Udp(id) => match self.spray.lock().unwrap().get(&id) {
                Some((s, _)) => {
                    let old = s.ttl().unwrap_or(64);
                    s.set_ttl(ttl)?;
                    let r = s.send_to(data, to);
                    let _ = s.set_ttl(old);
                    r
                }
                None => self.send_low_ttl(to, data, ttl),
            },
            Via::Tcp => self.send_tcp(&[to], data),
        }
    }

    /// Send on a specific transport (main UDP, a spray socket, or TCP lanes).
    pub fn send_on(&self, via: Via, to: SocketAddr, data: &[u8]) -> io::Result<usize> {
        match via {
            Via::Udp(0) => self.send(to, data),
            Via::Udp(id) => match self.spray.lock().unwrap().get(&id) {
                Some((s, _)) => s.send_to(data, to),
                None => self.send(to, data),
            },
            Via::Tcp => self.send_tcp(&[to], data),
        }
    }

    /// Accept TCP-fallback connections on `port` (IPv4 + IPv6). Returns the bound port.
    pub fn listen_tcp(&self, port: u16, v6: bool) -> io::Result<u16> {
        let l4 = TcpListener::bind((Ipv4Addr::UNSPECIFIED, port))?;
        let p = l4.local_addr()?.port();
        let mut ls = vec![l4];
        if v6 {
            if let Ok(l6) = TcpListener::bind((Ipv6Addr::UNSPECIFIED, p)) {
                ls.push(l6);
            }
        }
        for l in ls {
            l.set_nonblocking(true)?;
            let (sink, map, stop) = (self.sink.clone(), self.tcp.clone(), self.stop.clone());
            thread::Builder::new()
                .name("peerova-tcp-accept".into())
                .spawn(move || {
                    while !stop.load(Ordering::Relaxed) {
                        match l.accept() {
                            Ok((s, peer)) => {
                                let peer = canonical(peer);
                                s.set_nonblocking(false).ok();
                                if map.lock().unwrap().len() >= MAX_TCP_PEERS {
                                    continue; // drop: protects against connection floods
                                }
                                add_lane(s, peer, sink.clone(), map.clone(), stop.clone());
                            }
                            Err(_) => thread::sleep(Duration::from_millis(20)),
                        }
                    }
                })?;
        }
        Ok(p)
    }

    /// Open `TCP_LANES` parallel TCP-fallback connections to `addr` in the background.
    pub fn connect_tcp(&self, addr: SocketAddr) {
        if crate::relay::is_relay(&addr) || self.tcp.lock().unwrap().contains_key(&addr) {
            return;
        }
        for _ in 0..TCP_LANES {
            let (sink, map, stop) = (self.sink.clone(), self.tcp.clone(), self.stop.clone());
            spawn_small(move || {
                if let Ok(s) = TcpStream::connect_timeout(&addr, Duration::from_secs(4)) {
                    add_lane(s, addr, sink, map, stop);
                }
            });
        }
    }

    /// Last resort: open `RELAY_LANES` WebSockets to the website relay room. Returns the
    /// pseudo address the engine uses for this path (packets flow like a TCP lane).
    pub fn connect_relay(
        &self,
        base: &str,
        room: &str,
        role: &'static str,
        auth: Option<String>,
        extra: &str,
        log: impl Fn(String) + Send + Sync + 'static,
        up: impl Fn() + Send + Sync + 'static,
    ) -> SocketAddr {
        let peer = crate::relay::pseudo_addr();
        let (log, up) = (Arc::new(log), Arc::new(up));
        for lane in 0..crate::relay::RELAY_LANES {
            let (sink, map, stop, log, up) = (
                self.sink.clone(),
                self.tcp.clone(),
                self.stop.clone(),
                log.clone(),
                up.clone(),
            );
            let (url, auth) = (
                crate::relay::url(base, room, role, lane) + extra,
                auth.clone(),
            );
            thread::Builder::new()
                .name("peerova-relay".into())
                .stack_size(LANE_STACK)
                .spawn(move || match crate::relay::connect(&url, auth.as_deref()) {
                    Ok(ws) => relay_lane(ws, peer, sink, map, stop, move || up()),
                    Err(e) => {
                        if lane == 0 {
                            log(format!("đường dự phòng lỗi: {e}"));
                        }
                    }
                })
                .expect("spawn relay");
        }
        peer
    }

    /// Server: keep `RELAY_LANES` standby connections in our own relay room (reconnecting
    /// forever). Clients join with a client id and reach us with zero extra handshakes;
    /// each client id becomes its own pseudo peer address.
    pub fn relay_standby(
        &self,
        base: &str,
        room: &str,
        auth: Arc<dyn Fn() -> Option<String> + Send + Sync>,
        log: impl Fn(String) + Send + Sync + 'static,
    ) {
        let log = Arc::new(log);
        let cids: Arc<Mutex<HashMap<[u8; 8], SocketAddr>>> = Arc::new(Mutex::new(HashMap::new()));
        for lane in 0..crate::relay::RELAY_LANES {
            let (sink, map, stop, log, auth, cids) = (
                self.sink.clone(),
                self.tcp.clone(),
                self.stop.clone(),
                log.clone(),
                auth.clone(),
                cids.clone(),
            );
            let url = crate::relay::url(base, room, "s", lane) + "&mux=1";
            let _ = thread::Builder::new()
                .name("peerova-standby".into())
                .stack_size(LANE_STACK)
                .spawn(move || {
                    let mut backoff = 1;
                    let mut warned = false;
                    while !stop.load(Ordering::Relaxed) {
                        match crate::relay::connect(&url, auth().as_deref()) {
                            Ok(ws) => {
                                backoff = 1;
                                warned = false;
                                standby_lane(ws, &sink, &map, &stop, &cids);
                            }
                            Err(e) => {
                                if lane == 0 && !warned {
                                    warned = true;
                                    log(format!("đường dự phòng chưa sẵn sàng: {e} (tự thử lại)"));
                                }
                            }
                        }
                        thread::sleep(Duration::from_secs(backoff));
                        backoff = (backoff * 2).min(30);
                    }
                });
        }
    }

    /// Close the lanes of one path (e.g. the relays that lost the race).
    pub fn close_lanes(&self, addr: &SocketAddr) {
        if let Some(lanes) = self.tcp.lock().unwrap().remove(addr) {
            for l in lanes {
                let _ = l.stream.shutdown(Shutdown::Both);
            }
        }
    }

    /// Close every TCP-fallback connection (UDP won the race).
    pub fn close_tcp(&self) {
        for (_, lanes) in self.tcp.lock().unwrap().drain() {
            for l in lanes {
                let _ = l.stream.shutdown(Shutdown::Both);
            }
        }
    }

    /// Stripe a packet over all TCP lanes of `paths`; duplicate small packets on
    /// a second lane; skip lanes whose queue is full (congested).
    pub fn send_tcp(&self, paths: &[SocketAddr], data: &[u8]) -> io::Result<usize> {
        let m = self.tcp.lock().unwrap();
        let lanes: Vec<&TcpLane> = paths.iter().filter_map(|p| m.get(p)).flatten().collect();
        if lanes.is_empty() {
            return Err(io::Error::new(
                io::ErrorKind::NotConnected,
                "chưa có kết nối TCP",
            ));
        }
        let mut f = Vec::with_capacity(2 + data.len());
        f.extend_from_slice(&(data.len() as u16).to_be_bytes());
        f.extend_from_slice(data);
        let n = lanes.len();
        let start = self.rr.fetch_add(1, Ordering::Relaxed) as usize;
        let copies = if data.len() <= DUP_MAX && n > 1 { 2 } else { 1 };
        let now = Instant::now();
        let mut sent = 0;
        for k in 0..n {
            if sent == copies {
                break;
            }
            let lane = lanes[(start + k) % n];
            let item = match lane.prefix {
                Some(p) => [&p[..], &f[..]].concat(),
                None => f.clone(),
            };
            match lane.tx.try_send((now, item)) {
                Ok(()) => sent += 1,
                Err(TrySendError::Full(_)) | Err(TrySendError::Disconnected(_)) => continue,
            }
        }
        if sent == 0 {
            return Err(io::Error::new(
                io::ErrorKind::WouldBlock,
                "mọi làn TCP đang nghẽn",
            ));
        }
        Ok(data.len())
    }

    pub fn has_tcp(&self, addr: &SocketAddr) -> bool {
        self.tcp.lock().unwrap().contains_key(addr)
    }

    pub fn port_v4(&self) -> Option<u16> {
        self.v4
            .as_ref()
            .and_then(|s| s.local_addr().ok())
            .map(|a| a.port())
    }

    pub fn port_v6(&self) -> Option<u16> {
        self.v6
            .as_ref()
            .and_then(|s| s.local_addr().ok())
            .map(|a| a.port())
    }

    pub fn supports(&self, a: &SocketAddr) -> bool {
        match a.ip() {
            IpAddr::V4(_) => self.v4.is_some(),
            IpAddr::V6(_) => self.v6.is_some(),
        }
    }

    pub fn send(&self, to: SocketAddr, data: &[u8]) -> io::Result<usize> {
        self.send_via(to, data, false)
    }

    /// `tcp`: prefer an existing TCP-fallback connection to `to`.
    pub fn send_via(&self, to: SocketAddr, data: &[u8], tcp: bool) -> io::Result<usize> {
        if tcp {
            return self.send_tcp(&[to], data);
        }
        let sock = match to.ip() {
            IpAddr::V4(_) => self.v4.as_ref(),
            IpAddr::V6(_) => self.v6.as_ref(),
        };
        match sock {
            Some(s) => s.send_to(data, to),
            None => Err(io::Error::new(
                io::ErrorKind::Unsupported,
                "không có socket cho họ địa chỉ này",
            )),
        }
    }
}

impl Drop for Node {
    fn drop(&mut self) {
        self.stop.store(true, Ordering::Relaxed);
        self.close_tcp();
        self.close_spray_except(None);
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::sync::mpsc;

    #[test]
    fn spray_sockets_send_and_receive() {
        let (tx, rx) = mpsc::channel::<(SocketAddr, Vec<u8>, Via)>();
        let tx = std::sync::Mutex::new(tx);
        let a = Node::bind(
            0,
            None,
            Arc::new(move |f, d, v| {
                let _ = tx.lock().unwrap().send((f, d.to_vec(), v));
            }),
        )
        .unwrap();
        let ids = a.open_spray(8);
        assert_eq!(ids.len(), 8);
        let peer = UdpSocket::bind("127.0.0.1:0").unwrap();
        a.send_on(Via::Udp(ids[3]), peer.local_addr().unwrap(), b"probe")
            .unwrap();
        let mut buf = [0u8; 16];
        let (n, from) = peer.recv_from(&mut buf).unwrap();
        assert_eq!(&buf[..n], b"probe");
        peer.send_to(b"back", from).unwrap();
        let (_, d, via) = rx.recv_timeout(Duration::from_secs(2)).unwrap();
        assert_eq!((d.as_slice(), via), (&b"back"[..], Via::Udp(ids[3])));
        a.close_spray_except(Some(ids[3]));
        assert_eq!(a.spray.lock().unwrap().len(), 1);
    }

    #[test]
    fn loopback() {
        let (tx, rx) = mpsc::channel::<(SocketAddr, Vec<u8>)>();
        let tx = std::sync::Mutex::new(tx);
        let b = Node::bind(
            0,
            None,
            Arc::new(move |f, d, _| {
                let _ = tx.lock().unwrap().send((f, d.to_vec()));
            }),
        )
        .unwrap();
        let a = Node::bind(0, None, Arc::new(|_, _, _| {})).unwrap();
        a.send((Ipv4Addr::LOCALHOST, b.port_v4().unwrap()).into(), b"hi")
            .unwrap();
        let (from, d) = rx.recv_timeout(Duration::from_secs(2)).unwrap();
        assert_eq!(d, b"hi");
        assert_eq!(from.port(), a.port_v4().unwrap());
    }
}
