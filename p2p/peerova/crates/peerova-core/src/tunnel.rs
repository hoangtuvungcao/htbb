//! Port tunnelling over an engine connection — every protocol:
//! * TCP (HTTP, HTTPS end-to-end, WebSocket, SSH, gRPC, RDP, databases, TCP games)
//!   → one reliable stream per TCP connection, independent (no head-of-line
//!   blocking between connections), TCP_NODELAY for low latency.
//! * UDP (game traffic, QUIC/HTTP3, voice, DNS) → unreliable datagrams with
//!   per-source flows, never retransmitted (stale realtime data is dropped).
//!
//! Stream header (first bytes of each tunnel stream): `0x01 | port u16`.
//! Datagram: `0x02 | port u16 | flow u32 | payload`.

use std::collections::HashMap;
use std::io::{Read, Write};
use std::net::{
    IpAddr, Ipv4Addr, Ipv6Addr, Shutdown, SocketAddr, TcpListener, TcpStream, ToSocketAddrs,
    UdpSocket,
};
use std::sync::atomic::{AtomicBool, AtomicU32, AtomicU8, Ordering};
use std::sync::mpsc::{self, Receiver, Sender};
use std::sync::{Arc, Condvar, Mutex};
use std::thread;
use std::time::{Duration, Instant};

use crate::conn::MAX_DATAGRAM;
use crate::engine::{Cmd, Engine, EngineEvent, Handle};

const HDR_TCP: u8 = 0x01;
const HDR_UDP: u8 = 0x02;
pub const HDR_BENCH: u8 = 0x03;
pub const HDR_PING: u8 = 0x04;
/// Fragment of a large UDP datagram: `0x05 | port | flow | msg u16 | idx u8 | count u8 | chunk`.
const HDR_UDPF: u8 = 0x05;
const FRAG_CHUNK: usize = MAX_DATAGRAM - 11;
/// Largest UDP payload tunnelled (IPv4 UDP maximum).
pub const MAX_UDP_DATAGRAM: usize = 65507;
const MAX_RANGE: u32 = 1024;

/// One access decision on the server (for audit logs / SIEM).
#[derive(Debug, Clone)]
pub struct Audit {
    pub conn: u32,
    pub proto: Proto,
    pub port: u16,
    pub allowed: bool,
}

/// Split a UDP payload into tunnel datagrams (one, or fragments when > MTU).
fn udp_frames(port: u16, flow: u32, msg: u16, payload: &[u8]) -> Vec<Vec<u8>> {
    if payload.len() <= MAX_UDP_PAYLOAD {
        return vec![udp_frame(port, flow, payload)];
    }
    if payload.len() > MAX_UDP_DATAGRAM {
        return vec![];
    }
    let chunks: Vec<&[u8]> = payload.chunks(FRAG_CHUNK).collect();
    let count = chunks.len() as u8;
    chunks
        .iter()
        .enumerate()
        .map(|(i, c)| {
            let mut d = Vec::with_capacity(11 + c.len());
            d.push(HDR_UDPF);
            d.extend_from_slice(&port.to_be_bytes());
            d.extend_from_slice(&flow.to_be_bytes());
            d.extend_from_slice(&msg.to_be_bytes());
            d.push(i as u8);
            d.push(count);
            d.extend_from_slice(c);
            d
        })
        .collect()
}

type FragKey = (u32, u32, u16);

/// Reassembles fragmented datagrams; incomplete ones expire (never block).
#[derive(Default)]
struct Defrag {
    parts: HashMap<FragKey, (Instant, Vec<Option<Vec<u8>>>, usize)>,
}

impl Defrag {
    fn push(&mut self, conn: u32, d: &[u8]) -> Option<(u16, u32, Vec<u8>)> {
        if let Some((port, flow, p)) = parse_udp(d) {
            return Some((port, flow, p.to_vec()));
        }
        if d.len() < 11 || d[0] != HDR_UDPF {
            return None;
        }
        let port = u16::from_be_bytes([d[1], d[2]]);
        let flow = u32::from_be_bytes([d[3], d[4], d[5], d[6]]);
        let msg = u16::from_be_bytes([d[7], d[8]]);
        let (idx, count) = (d[9] as usize, d[10] as usize);
        if count == 0 || idx >= count {
            return None;
        }
        if self.parts.len() > 4096 {
            let now = Instant::now();
            self.parts
                .retain(|_, (t, _, _)| now.duration_since(*t) < Duration::from_secs(2));
        }
        let e = self
            .parts
            .entry((conn, flow, msg))
            .or_insert_with(|| (Instant::now(), vec![None; count], 0));
        if e.1.len() != count || e.1[idx].is_some() {
            return None;
        }
        e.1[idx] = Some(d[11..].to_vec());
        e.2 += 1;
        if e.2 < count {
            return None;
        }
        let (_, pieces, _) = self.parts.remove(&(conn, flow, msg)).unwrap();
        Some((port, flow, pieces.into_iter().flatten().flatten().collect()))
    }
}

fn split_range(spec: &str) -> Result<(u16, u16), String> {
    match spec.split_once('-') {
        Some((a, b)) => {
            let (a, b): (u16, u16) = (
                a.parse().map_err(|_| "port không hợp lệ")?,
                b.parse().map_err(|_| "port không hợp lệ")?,
            );
            if a > b || (b - a) as u32 + 1 > MAX_RANGE {
                return Err(format!(
                    "dải port {a}-{b} không hợp lệ (tối đa {MAX_RANGE} port)"
                ));
            }
            Ok((a, b))
        }
        None => {
            let p = spec.parse().map_err(|_| "port không hợp lệ")?;
            Ok((p, p))
        }
    }
}

/// Like `parse_expose` but accepts ranges: `udp:10000-10100[=host:20000]` (target base port).
pub fn parse_expose_list(s: &str) -> Result<Vec<Rule>, String> {
    let (proto, rest) = s
        .split_once(':')
        .ok_or("dạng đúng: PROTO:PORT[-PORT][=HOST:PORT]")?;
    let (ports, target) = rest
        .split_once('=')
        .map(|(a, b)| (a, Some(b)))
        .unwrap_or((rest, None));
    let (lo, hi) = split_range(ports)?;
    if lo == hi {
        return Ok(vec![parse_expose(s)?]);
    }
    let base = match target {
        Some(t) => resolve(t)?,
        None => SocketAddr::new(IpAddr::V4(Ipv4Addr::LOCALHOST), lo),
    };
    let proto = proto_of(proto)?;
    Ok((lo..=hi)
        .map(|p| Rule {
            proto,
            port: p,
            target: SocketAddr::new(base.ip(), base.port() + (p - lo)),
        })
        .collect())
}

/// Like `parse_forward` but accepts ranges: `udp:10000-10100` or `tcp:8000-8010=9000` (remote base).
pub fn parse_forward_list(s: &str, bind: IpAddr) -> Result<Vec<Rule>, String> {
    let (proto, rest) = s
        .split_once(':')
        .ok_or("dạng đúng: PROTO:LOCAL[-LOCAL][=REMOTE]")?;
    let (local, remote) = rest
        .split_once('=')
        .map(|(a, b)| (a, Some(b)))
        .unwrap_or((rest, None));
    let (lo, hi) = split_range(local)?;
    if lo == hi {
        return Ok(vec![parse_forward(s, bind)?]);
    }
    let rbase: u16 = match remote {
        Some(r) => r.parse().map_err(|_| "port remote không hợp lệ")?,
        None => lo,
    };
    let proto = proto_of(proto)?;
    Ok((lo..=hi)
        .map(|p| Rule {
            proto,
            port: rbase + (p - lo),
            target: SocketAddr::new(bind, p),
        })
        .collect())
}
const STREAM_CREDIT: usize = 512 << 10;
const UDP_IDLE: Duration = Duration::from_secs(120);
pub const MAX_UDP_PAYLOAD: usize = MAX_DATAGRAM - 7;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Proto {
    Tcp,
    Udp,
}

/// Server: expose `port` → forward to `target`. Client: listen on `target`, tunnel to remote `port`.
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct Rule {
    pub proto: Proto,
    pub port: u16,
    pub target: SocketAddr,
}

fn proto_of(s: &str) -> Result<Proto, String> {
    match s.to_ascii_lowercase().as_str() {
        "tcp" | "http" | "https" | "ws" | "wss" | "ssh" | "rdp" => Ok(Proto::Tcp),
        "udp" | "quic" | "game" => Ok(Proto::Udp),
        o => Err(format!(
            "giao thức không hỗ trợ: {o} (dùng tcp/udp/http/https/ws/ssh/quic)"
        )),
    }
}

fn resolve(s: &str) -> Result<SocketAddr, String> {
    s.to_socket_addrs()
        .map_err(|e| format!("{s}: {e}"))?
        .next()
        .ok_or_else(|| format!("{s}: không phân giải được"))
}

/// `tcp:25565` | `https:443=10.0.0.5:8443` | `udp:19132=127.0.0.1:19133`
pub fn parse_expose(s: &str) -> Result<Rule, String> {
    let (proto, rest) = s
        .split_once(':')
        .ok_or("dạng đúng: PROTO:PORT[=HOST:PORT]")?;
    let proto = proto_of(proto)?;
    let (port, target) = match rest.split_once('=') {
        Some((p, t)) => (p, resolve(t)?),
        None => (
            rest,
            SocketAddr::new(
                IpAddr::V4(Ipv4Addr::LOCALHOST),
                rest.parse().map_err(|_| "port không hợp lệ")?,
            ),
        ),
    };
    Ok(Rule {
        proto,
        port: port.parse().map_err(|_| "port không hợp lệ")?,
        target,
    })
}

/// `tcp:25565` (local 25565 → remote 25565) | `tcp:8443=443` | `udp:7777`
pub fn parse_forward(s: &str, bind: IpAddr) -> Result<Rule, String> {
    let (proto, rest) = s.split_once(':').ok_or("dạng đúng: PROTO:LOCAL[=REMOTE]")?;
    let proto = proto_of(proto)?;
    let (local, remote) = rest.split_once('=').unwrap_or((rest, rest));
    let local: u16 = local.parse().map_err(|_| "port local không hợp lệ")?;
    Ok(Rule {
        proto,
        port: remote.parse().map_err(|_| "port remote không hợp lệ")?,
        target: SocketAddr::new(bind, local),
    })
}

// ------------------------------------------------------------- plumbing

/// Bounded in-flight bytes per stream: TCP reader blocks when the peer is slow.
struct Credit {
    m: Mutex<(usize, bool)>,
    cv: Condvar,
}

impl Credit {
    fn new() -> Arc<Self> {
        Arc::new(Credit {
            m: Mutex::new((STREAM_CREDIT, false)),
            cv: Condvar::new(),
        })
    }
    fn acquire(&self, n: usize) -> bool {
        let mut g = self.m.lock().unwrap();
        while g.0 < n && !g.1 {
            g = self.cv.wait(g).unwrap();
        }
        if g.1 {
            return false;
        }
        g.0 -= n;
        true
    }
    fn release(&self, n: usize) {
        let mut g = self.m.lock().unwrap();
        g.0 = (g.0 + n).min(STREAM_CREDIT);
        self.cv.notify_all();
    }
    fn close(&self) {
        self.m.lock().unwrap().1 = true;
        self.cv.notify_all();
    }
}

enum WMsg {
    Data(Vec<u8>),
    Fin,
    Reset,
}

struct Ctx {
    writer: Sender<WMsg>,
    credit: Arc<Credit>,
    /// Server side: header not parsed yet.
    header: Option<Vec<u8>>,
    pending_rx: Option<Receiver<WMsg>>,
    /// u64::MAX = not a bench stream.
    bench: u64,
}

impl Ctx {
    fn bench_mode(&self) -> bool {
        self.bench != u64::MAX
    }
}

type Streams = Arc<Mutex<HashMap<(u32, u32), Ctx>>>;

fn spawn_pipe(
    h: Handle,
    conn: u32,
    sid: u32,
    tcp: TcpStream,
    wrx: Receiver<WMsg>,
    credit: Arc<Credit>,
    streams: Streams,
) {
    tcp.set_nodelay(true).ok();
    let done = Arc::new(AtomicU8::new(0));
    let finish = {
        let streams = streams.clone();
        let done = done.clone();
        move || {
            if done.fetch_add(1, Ordering::SeqCst) == 1 {
                streams.lock().unwrap().remove(&(conn, sid));
            }
        }
    };
    let mut w = match tcp.try_clone() {
        Ok(w) => w,
        Err(_) => {
            h.send(Cmd::StreamReset { conn, id: sid });
            return;
        }
    };
    let (h2, credit2, finish2) = (h.clone(), credit.clone(), finish.clone());
    thread::spawn(move || {
        for m in wrx {
            match m {
                WMsg::Data(d) => {
                    if w.write_all(&d).is_err() {
                        h2.send(Cmd::StreamReset { conn, id: sid });
                        credit2.close();
                        break;
                    }
                    h2.send(Cmd::StreamConsumed {
                        conn,
                        id: sid,
                        n: d.len(),
                    });
                }
                WMsg::Fin => {
                    let _ = w.shutdown(Shutdown::Write);
                    break;
                }
                WMsg::Reset => {
                    let _ = w.shutdown(Shutdown::Both);
                    credit2.close();
                    break;
                }
            }
        }
        finish2();
    });
    let mut r = tcp;
    thread::spawn(move || {
        let mut buf = vec![0u8; 32 << 10];
        loop {
            match r.read(&mut buf) {
                Ok(0) => {
                    h.send(Cmd::StreamFinish { conn, id: sid });
                    break;
                }
                Ok(n) => {
                    if !credit.acquire(n) {
                        break;
                    }
                    h.send(Cmd::StreamWrite {
                        conn,
                        id: sid,
                        data: buf[..n].to_vec(),
                    });
                }
                Err(_) => {
                    h.send(Cmd::StreamReset { conn, id: sid });
                    credit.close();
                    break;
                }
            }
        }
        finish();
    });
}

/// Route common stream events. Returns false if the event was not a stream event.
fn route_stream_event(ev: &EngineEvent, streams: &Streams) -> bool {
    match ev {
        EngineEvent::StreamAcked { conn, id, n } => {
            if let Some(c) = streams.lock().unwrap().get(&(*conn, *id)) {
                c.credit.release(*n);
            }
        }
        EngineEvent::StreamFin { conn, id } => {
            if let Some(c) = streams.lock().unwrap().get(&(*conn, *id)) {
                let _ = c.writer.send(WMsg::Fin);
            }
        }
        EngineEvent::StreamReset { conn, id } => {
            if let Some(c) = streams.lock().unwrap().remove(&(*conn, *id)) {
                let _ = c.writer.send(WMsg::Reset);
                c.credit.close();
            }
        }
        _ => return false,
    }
    true
}

fn reset_conn(streams: &Streams, conn: u32) {
    let mut m = streams.lock().unwrap();
    let keys: Vec<(u32, u32)> = m.keys().filter(|(c, _)| *c == conn).copied().collect();
    for k in keys {
        if let Some(c) = m.remove(&k) {
            let _ = c.writer.send(WMsg::Reset);
            c.credit.close();
        }
    }
}

fn udp_frame(port: u16, flow: u32, payload: &[u8]) -> Vec<u8> {
    let mut d = Vec::with_capacity(7 + payload.len());
    d.push(HDR_UDP);
    d.extend_from_slice(&port.to_be_bytes());
    d.extend_from_slice(&flow.to_be_bytes());
    d.extend_from_slice(payload);
    d
}

fn parse_udp(d: &[u8]) -> Option<(u16, u32, &[u8])> {
    (d.len() >= 7 && d[0] == HDR_UDP).then(|| {
        (
            u16::from_be_bytes([d[1], d[2]]),
            u32::from_be_bytes([d[3], d[4], d[5], d[6]]),
            &d[7..],
        )
    })
}

// ------------------------------------------------------------- server

struct UdpFlow {
    sock: Arc<UdpSocket>,
    last: Instant,
    alive: Arc<AtomicBool>,
}

/// Server side: accept tunnel streams/datagrams and forward to exposed local services.
/// `on_event` sees every engine event (for logging / SDK hooks). Returns when the engine stops.
pub fn serve(engine: &Engine, rules: Vec<Rule>, on_event: impl FnMut(&EngineEvent)) {
    serve_audited(engine, rules, on_event, |_| {})
}

/// `serve` + per-connection port ACL (from access-token scopes) + audit callback.
pub fn serve_audited(
    engine: &Engine,
    rules: Vec<Rule>,
    on_event: impl FnMut(&EngineEvent),
    audit: impl FnMut(&Audit),
) {
    serve_hooked(engine, rules, on_event, audit, Arc::new(|_, _| {}))
}

/// Callback with (connection id, local address of the TCP socket the server opened to the exposed
/// service). Lets an embedding app map an accepted loopback socket back to the remote peer.
pub type TcpHook = Arc<dyn Fn(u32, SocketAddr) + Send + Sync>;

/// `serve_audited` + `on_tcp` called right after each outbound TCP connection to an exposed service.
pub fn serve_hooked(
    engine: &Engine,
    rules: Vec<Rule>,
    mut on_event: impl FnMut(&EngineEvent),
    mut audit: impl FnMut(&Audit),
    on_tcp: TcpHook,
) {
    let h = engine.handle.clone();
    let mut scopes: HashMap<u32, Option<Vec<String>>> = HashMap::new();
    let mut defrag = Defrag::default();
    let streams: Streams = Arc::new(Mutex::new(HashMap::new()));
    let mut flows: HashMap<(u32, u32), UdpFlow> = HashMap::new();
    let mut last_gc = Instant::now();
    loop {
        let ev = match engine.events().recv_timeout(Duration::from_secs(1)) {
            Ok(ev) => ev,
            Err(mpsc::RecvTimeoutError::Timeout) => {
                if !engine.is_running() {
                    return; // engine stopped; relay helper threads may still hold the channel
                }
                gc_flows(&mut flows, &mut last_gc);
                continue;
            }
            Err(_) => return,
        };
        on_event(&ev);
        match &ev {
            EngineEvent::Connected {
                conn, scopes: sc, ..
            } => {
                scopes.insert(*conn, sc.clone());
            }
            EngineEvent::Disconnected { conn, .. } => {
                scopes.remove(conn);
            }
            _ => {}
        }
        if let EngineEvent::StreamFin { conn, id } = ev {
            let mut m = streams.lock().unwrap();
            if let Some(c) = m.get(&(conn, id)).filter(|c| c.bench_mode()) {
                h.send(Cmd::StreamWrite {
                    conn,
                    id,
                    data: c.bench.to_be_bytes().to_vec(),
                });
                h.send(Cmd::StreamFinish { conn, id });
                m.remove(&(conn, id));
                continue;
            }
        }
        if route_stream_event(&ev, &streams) {
            continue;
        }
        match ev {
            EngineEvent::StreamOpened { conn, id } => {
                let (wtx, wrx) = mpsc::channel();
                streams.lock().unwrap().insert(
                    (conn, id),
                    Ctx {
                        writer: wtx,
                        credit: Credit::new(),
                        header: Some(vec![]),
                        pending_rx: Some(wrx),
                        bench: u64::MAX,
                    },
                );
            }
            EngineEvent::StreamData { conn, id, data } => {
                let mut m = streams.lock().unwrap();
                let Some(ctx) = m.get_mut(&(conn, id)) else {
                    continue;
                };
                if ctx.bench_mode() {
                    ctx.bench += data.len() as u64;
                    h.send(Cmd::StreamConsumed {
                        conn,
                        id,
                        n: data.len(),
                    });
                    continue;
                }
                let Some(hdr) = ctx.header.as_mut() else {
                    let _ = ctx.writer.send(WMsg::Data(data));
                    continue;
                };
                hdr.extend_from_slice(&data);
                if hdr.len() < 3 {
                    continue;
                }
                let hdr = ctx.header.take().unwrap();
                if hdr[0] == HDR_BENCH {
                    // Throughput sink: count bytes, reply with the total on FIN.
                    h.send(Cmd::StreamConsumed {
                        conn,
                        id,
                        n: hdr.len(),
                    });
                    ctx.bench = (hdr.len() - 3) as u64;
                    ctx.header = None;
                    continue;
                }
                let port = u16::from_be_bytes([hdr[1], hdr[2]]);
                let permitted = scopes
                    .get(&conn)
                    .and_then(|s| s.as_ref())
                    .is_none_or(|s| crate::access::scope_allows(s, Proto::Tcp, port));
                let rule = rules
                    .iter()
                    .find(|r| r.proto == Proto::Tcp && r.port == port && hdr[0] == HDR_TCP)
                    .filter(|_| permitted);
                audit(&Audit {
                    conn,
                    proto: Proto::Tcp,
                    port,
                    allowed: rule.is_some(),
                });
                h.send(Cmd::StreamConsumed { conn, id, n: 3 });
                let Some(rule) = rule else {
                    m.remove(&(conn, id));
                    h.send(Cmd::StreamReset { conn, id });
                    on_event(&EngineEvent::Log(format!(
                        "client {conn:08x} xin TCP {port}: {} — từ chối",
                        if permitted {
                            "không được expose"
                        } else {
                            "token không có quyền"
                        }
                    )));
                    continue;
                };
                if hdr.len() > 3 {
                    let _ = ctx.writer.send(WMsg::Data(hdr[3..].to_vec()));
                }
                let wrx = ctx.pending_rx.take().unwrap();
                let credit = ctx.credit.clone();
                let (target, h2, streams2, hook) =
                    (rule.target, h.clone(), streams.clone(), on_tcp.clone());
                drop(m);
                thread::spawn(move || {
                    match TcpStream::connect_timeout(&target, Duration::from_secs(5)) {
                        Ok(tcp) => {
                            if let Ok(local) = tcp.local_addr() {
                                hook(conn, local);
                            }
                            spawn_pipe(h2, conn, id, tcp, wrx, credit, streams2)
                        }
                        Err(_) => {
                            streams2.lock().unwrap().remove(&(conn, id));
                            h2.send(Cmd::StreamReset { conn, id });
                        }
                    }
                });
            }
            EngineEvent::Datagram { conn, data } if data.first() == Some(&HDR_PING) => {
                h.send(Cmd::Datagram { conn, data }); // latency probe: echo as-is
            }
            EngineEvent::Datagram { conn, data } => {
                let Some((port, flow, payload)) = defrag.push(conn, &data) else {
                    continue;
                };
                let key = (conn, flow);
                if let std::collections::hash_map::Entry::Vacant(e) = flows.entry(key) {
                    let permitted = scopes
                        .get(&conn)
                        .and_then(|s| s.as_ref())
                        .is_none_or(|s| crate::access::scope_allows(s, Proto::Udp, port));
                    let rule = rules
                        .iter()
                        .find(|r| r.proto == Proto::Udp && r.port == port)
                        .filter(|_| permitted);
                    audit(&Audit {
                        conn,
                        proto: Proto::Udp,
                        port,
                        allowed: rule.is_some(),
                    });
                    let Some(rule) = rule else { continue };
                    let bind: SocketAddr = if rule.target.is_ipv6() {
                        (Ipv6Addr::UNSPECIFIED, 0).into()
                    } else {
                        (Ipv4Addr::UNSPECIFIED, 0).into()
                    };
                    let Ok(sock) = UdpSocket::bind(bind) else {
                        continue;
                    };
                    if sock.connect(rule.target).is_err() {
                        continue;
                    }
                    sock.set_read_timeout(Some(Duration::from_secs(1))).ok();
                    let sock = Arc::new(sock);
                    let alive = Arc::new(AtomicBool::new(true));
                    let (s2, a2, h2) = (sock.clone(), alive.clone(), h.clone());
                    thread::spawn(move || {
                        let mut buf = vec![0u8; 65536];
                        let mut msg = 0u16;
                        while a2.load(Ordering::Relaxed) {
                            if let Ok(n) = s2.recv(&mut buf) {
                                msg = msg.wrapping_add(1);
                                for d in udp_frames(port, flow, msg, &buf[..n]) {
                                    h2.send(Cmd::Datagram { conn, data: d });
                                }
                            }
                        }
                    });
                    e.insert(UdpFlow {
                        sock,
                        last: Instant::now(),
                        alive,
                    });
                }
                let f = flows.get_mut(&key).unwrap();
                f.last = Instant::now();
                let _ = f.sock.send(&payload);
            }
            EngineEvent::Disconnected { conn, .. } => {
                reset_conn(&streams, conn);
                flows.retain(|(c, _), f| {
                    if *c == conn {
                        f.alive.store(false, Ordering::Relaxed);
                    }
                    *c != conn
                });
            }
            _ => {}
        }
        gc_flows(&mut flows, &mut last_gc);
    }
}

fn gc_flows(flows: &mut HashMap<(u32, u32), UdpFlow>, last_gc: &mut Instant) {
    if last_gc.elapsed() < Duration::from_secs(5) {
        return;
    }
    *last_gc = Instant::now();
    flows.retain(|_, f| {
        let keep = f.last.elapsed() < UDP_IDLE;
        if !keep {
            f.alive.store(false, Ordering::Relaxed);
        }
        keep
    });
}

// ------------------------------------------------------------- client

/// Client side: listen locally and tunnel to the server. Returns when the
/// connection drops (caller may reconnect) or fails.
pub fn forward(
    engine: &Engine,
    rules: Vec<Rule>,
    on_event: impl FnMut(&EngineEvent),
) -> Result<(), String> {
    forward_with(engine, rules, on_event, |_, _| {})
}

/// `forward` + `on_listen(rule, result)` once each local listener is bound (or failed), i.e. the
/// moment an app may connect to the forwarded local port.
pub fn forward_with(
    engine: &Engine,
    rules: Vec<Rule>,
    mut on_event: impl FnMut(&EngineEvent),
    mut on_listen: impl FnMut(&Rule, Result<(), String>),
) -> Result<(), String> {
    let h = engine.handle.clone();
    let streams: Streams = Arc::new(Mutex::new(HashMap::new()));
    let conn_id = Arc::new(AtomicU32::new(0));
    let stop = Arc::new(AtomicBool::new(false));
    // flow id -> (local socket, app source address)
    let udp_back: Arc<Mutex<HashMap<u32, (Arc<UdpSocket>, SocketAddr)>>> =
        Arc::new(Mutex::new(HashMap::new()));
    let mut started = false;
    let mut defrag = Defrag::default();

    let result = loop {
        let ev = match engine.events().recv_timeout(Duration::from_secs(1)) {
            Ok(ev) => ev,
            Err(mpsc::RecvTimeoutError::Timeout) if engine.is_running() => continue,
            Err(_) => break Err("engine dừng".into()),
        };
        on_event(&ev);
        if route_stream_event(&ev, &streams) {
            continue;
        }
        match ev {
            EngineEvent::Connected { conn, .. } => {
                conn_id.store(conn, Ordering::SeqCst);
                if !started {
                    started = true;
                    for r in &rules {
                        if let Err(e) = start_listener(
                            r.clone(),
                            h.clone(),
                            conn_id.clone(),
                            streams.clone(),
                            udp_back.clone(),
                            stop.clone(),
                        ) {
                            on_event(&EngineEvent::Log(format!(
                                "✘ không mở được {:?} {}: {e}",
                                r.proto, r.target
                            )));
                            on_listen(r, Err(e.to_string()));
                        } else {
                            on_event(&EngineEvent::Log(format!(
                                "✔ {:?} {} → server port {}",
                                r.proto, r.target, r.port
                            )));
                            on_listen(r, Ok(()));
                        }
                    }
                }
            }
            EngineEvent::StreamData { conn, id, data } => {
                if let Some(c) = streams.lock().unwrap().get(&(conn, id)) {
                    let _ = c.writer.send(WMsg::Data(data));
                }
            }
            EngineEvent::Datagram { conn, data } => {
                if let Some((_, flow, payload)) = defrag.push(conn, &data) {
                    if let Some((sock, src)) = udp_back.lock().unwrap().get(&flow) {
                        let _ = sock.send_to(&payload, src);
                    }
                }
            }
            EngineEvent::ConnectFailed(e) => break Err(e),
            EngineEvent::Disconnected { conn, reason } => {
                reset_conn(&streams, conn);
                break Err(format!("mất kết nối: {reason}"));
            }
            _ => {}
        }
    };
    stop.store(true, Ordering::SeqCst);
    result
}

fn start_listener(
    r: Rule,
    h: Handle,
    conn_id: Arc<AtomicU32>,
    streams: Streams,
    udp_back: Arc<Mutex<HashMap<u32, (Arc<UdpSocket>, SocketAddr)>>>,
    stop: Arc<AtomicBool>,
) -> std::io::Result<()> {
    match r.proto {
        Proto::Tcp => {
            let l = TcpListener::bind(r.target)?;
            l.set_nonblocking(true)?;
            thread::spawn(move || {
                while !stop.load(Ordering::Relaxed) {
                    match l.accept() {
                        Ok((tcp, _)) => {
                            tcp.set_nonblocking(false).ok();
                            let conn = conn_id.load(Ordering::SeqCst);
                            let sid = h.alloc_stream();
                            let (wtx, wrx) = mpsc::channel();
                            let credit = Credit::new();
                            streams.lock().unwrap().insert(
                                (conn, sid),
                                Ctx {
                                    writer: wtx,
                                    credit: credit.clone(),
                                    header: None,
                                    pending_rx: None,
                                    bench: u64::MAX,
                                },
                            );
                            let mut hdr = vec![HDR_TCP];
                            hdr.extend_from_slice(&r.port.to_be_bytes());
                            h.send(Cmd::StreamWrite {
                                conn,
                                id: sid,
                                data: hdr,
                            });
                            spawn_pipe(h.clone(), conn, sid, tcp, wrx, credit, streams.clone());
                        }
                        Err(_) => thread::sleep(Duration::from_millis(10)),
                    }
                }
            });
        }
        Proto::Udp => {
            let sock = Arc::new(UdpSocket::bind(r.target)?);
            sock.set_read_timeout(Some(Duration::from_millis(500))).ok();
            thread::spawn(move || {
                let mut by_src: HashMap<SocketAddr, u32> = HashMap::new();
                let mut next_flow = 1u32;
                let mut msg = 0u16;
                let mut buf = vec![0u8; 65536];
                while !stop.load(Ordering::Relaxed) {
                    let Ok((n, src)) = sock.recv_from(&mut buf) else {
                        continue;
                    };
                    let flow = *by_src.entry(src).or_insert_with(|| {
                        let f = next_flow;
                        next_flow += 1;
                        udp_back.lock().unwrap().insert(f, (sock.clone(), src));
                        f
                    });
                    let conn = conn_id.load(Ordering::SeqCst);
                    msg = msg.wrapping_add(1);
                    for d in udp_frames(r.port, flow, msg, &buf[..n]) {
                        h.send(Cmd::Datagram { conn, data: d });
                    }
                }
            });
        }
    }
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::crypto::Keypair;
    use crate::engine::{ClientConfig, ServerConfig};

    #[test]
    fn parse_rules() {
        let r = parse_expose("https:443=127.0.0.1:8443").unwrap();
        assert_eq!((r.proto, r.port, r.target.port()), (Proto::Tcp, 443, 8443));
        let r = parse_expose("udp:19132").unwrap();
        assert_eq!(
            (r.proto, r.target),
            (Proto::Udp, "127.0.0.1:19132".parse().unwrap())
        );
        let r = parse_forward("tcp:8443=443", IpAddr::V4(Ipv4Addr::LOCALHOST)).unwrap();
        assert_eq!((r.port, r.target.port()), (443, 8443));
        assert!(parse_expose("sctp:1").is_err());
        let r = parse_expose_list("udp:10000-10002=127.0.0.1:20000").unwrap();
        assert_eq!(
            r.iter()
                .map(|x| (x.port, x.target.port()))
                .collect::<Vec<_>>(),
            vec![(10000, 20000), (10001, 20001), (10002, 20002)]
        );
        let r = parse_forward_list("tcp:8000-8001=9000", IpAddr::V4(Ipv4Addr::LOCALHOST)).unwrap();
        assert_eq!(
            r.iter()
                .map(|x| (x.target.port(), x.port))
                .collect::<Vec<_>>(),
            vec![(8000, 9000), (8001, 9001)]
        );
        assert!(parse_expose_list("udp:5-1").is_err() && parse_expose_list("udp:1-5000").is_err());
        let mut d = Defrag::default();
        let big: Vec<u8> = (0..5000).map(|i| i as u8).collect();
        let mut frames = udp_frames(7, 1, 9, &big);
        frames.reverse();
        let mut out = None;
        for f in frames {
            out = d.push(1, &f).or(out);
        }
        assert_eq!(out.unwrap().2, big);
    }

    fn free_port() -> u16 {
        TcpListener::bind("127.0.0.1:0")
            .unwrap()
            .local_addr()
            .unwrap()
            .port()
    }

    #[test]
    fn tunnels_tcp_and_udp_end_to_end() {
        // Local "services" on the server machine: a TCP echo (upper-cases) and a UDP echo.
        let svc = TcpListener::bind("127.0.0.1:0").unwrap();
        let tcp_port = svc.local_addr().unwrap().port();
        thread::spawn(move || {
            for s in svc.incoming().flatten() {
                thread::spawn(move || {
                    let mut s = s;
                    let mut buf = vec![0u8; 65536];
                    while let Ok(n) = s.read(&mut buf) {
                        if n == 0 {
                            break;
                        }
                        let up: Vec<u8> = buf[..n].iter().map(|b| b.to_ascii_uppercase()).collect();
                        if s.write_all(&up).is_err() {
                            break;
                        }
                    }
                });
            }
        });
        let usvc = UdpSocket::bind("127.0.0.1:0").unwrap();
        let udp_port = usvc.local_addr().unwrap().port();
        thread::spawn(move || {
            let mut buf = vec![0u8; 65536];
            while let Ok((n, from)) = usvc.recv_from(&mut buf) {
                let _ = usvc.send_to(&buf[..n], from);
            }
        });

        let mut cfg = ServerConfig::new(Keypair::generate());
        cfg.port = 0;
        cfg.try_map = false;
        let server = Engine::start_server(cfg).unwrap();
        let ticket = loop {
            if let Some(EngineEvent::Ready {
                ticket: Some(t), ..
            }) = server.recv_timeout(Duration::from_secs(5))
            {
                break t;
            }
        };
        let rules = vec![
            parse_expose(&format!("tcp:{tcp_port}")).unwrap(),
            parse_expose(&format!("udp:{udp_port}")).unwrap(),
        ];
        thread::spawn(move || serve(&server, rules, |_| {}));

        let (lt, lu) = (free_port(), free_port());
        let fwd = vec![
            parse_forward(
                &format!("tcp:{lt}={tcp_port}"),
                IpAddr::V4(Ipv4Addr::LOCALHOST),
            )
            .unwrap(),
            parse_forward(
                &format!("udp:{lu}={udp_port}"),
                IpAddr::V4(Ipv4Addr::LOCALHOST),
            )
            .unwrap(),
        ];
        let (ready_tx, ready_rx) = mpsc::channel();
        thread::spawn(move || {
            let client = Engine::connect(&ticket, ClientConfig::default()).unwrap();
            let _ = forward(&client, fwd, |e| {
                if let EngineEvent::Log(m) = e {
                    if m.starts_with('✔') {
                        let _ = ready_tx.send(());
                    }
                }
            });
        });
        ready_rx.recv_timeout(Duration::from_secs(10)).unwrap();
        ready_rx.recv_timeout(Duration::from_secs(2)).unwrap();

        // Several concurrent TCP connections with 1 MB each.
        let mut joins = vec![];
        for k in 0..4u8 {
            joins.push(thread::spawn(move || {
                let mut s = TcpStream::connect(("127.0.0.1", lt)).unwrap();
                s.set_read_timeout(Some(Duration::from_secs(20))).unwrap();
                let data: Vec<u8> = (0..1_000_000)
                    .map(|i| b'a' + ((i + k as usize) % 26) as u8)
                    .collect();
                let expect: Vec<u8> = data.iter().map(|b| b.to_ascii_uppercase()).collect();
                let mut w = s.try_clone().unwrap();
                let sender = thread::spawn(move || w.write_all(&data).unwrap());
                let mut got = vec![0u8; expect.len()];
                s.read_exact(&mut got).unwrap();
                sender.join().unwrap();
                assert!(got == expect, "tcp data mismatch on conn {k}");
            }));
        }
        for j in joins {
            j.join().unwrap();
        }

        // UDP round trip.
        let u = UdpSocket::bind("127.0.0.1:0").unwrap();
        u.set_read_timeout(Some(Duration::from_millis(500)))
            .unwrap();
        let mut ok = 0;
        for i in 0..20u8 {
            u.send_to(&[i; 100], ("127.0.0.1", lu)).unwrap();
            let mut b = [0u8; 2048];
            if let Ok((n, _)) = u.recv_from(&mut b) {
                if n == 100 && b[0] == i {
                    ok += 1;
                }
            }
        }
        assert!(ok >= 18, "udp echo {ok}/20");

        // Large UDP datagrams (QUIC/HTTP3, voice, big game snapshots) via fragmentation.
        let mut big_ok = 0;
        for size in [1300usize, 4000, 16000, 60000] {
            let payload: Vec<u8> = (0..size).map(|i| (i * 7 % 251) as u8).collect();
            u.send_to(&payload, ("127.0.0.1", lu)).unwrap();
            let mut b = vec![0u8; 65536];
            if let Ok((n, _)) = u.recv_from(&mut b) {
                if b[..n] == payload[..] {
                    big_ok += 1;
                }
            }
        }
        assert!(big_ok >= 3, "large udp {big_ok}/4");
    }
}
