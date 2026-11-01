//! Last-resort relay through the website (Cloudflare), used only when hole punching
//! fails (e.g. symmetric NAT on both sides). The relay only forwards already
//! Noise-encrypted Peerova packets: it never sees keys or plaintext.
//!
//! Transport: WebSocket (RFC 6455) over TLS (rustls) or plain TCP for local tests.
//! Each WebSocket binary message carries one or more `len u16 | packet` frames — the
//! same framing as the direct TCP fallback — so the engine treats a relay
//! connection exactly like a TCP lane. For smoothness:
//!   * TCP_NODELAY, no coalescing delay: whatever is queued is sent at once;
//!   * realtime packets older than 150 ms are dropped instead of sent late;
//!   * the node opens two lanes per relay and duplicates small packets.

use std::io::{self, Read, Write};
use std::net::{IpAddr, Ipv6Addr, SocketAddr, TcpStream, ToSocketAddrs};
use std::sync::atomic::{AtomicU32, Ordering};
use std::sync::Arc;
use std::time::Duration;

use blake2::{Blake2s256, Digest};

/// Parallel relay lanes (WebSocket connections) per peer.
pub const RELAY_LANES: usize = 2;
/// Largest WebSocket message we accept (several packets).
const MAX_MSG: usize = 64 * 1024;

static NEXT: AtomicU32 = AtomicU32::new(1);

/// Pseudo address identifying a relay connection inside the engine (IPv6 discard
/// prefix 100::/64 — never routable, so it can't collide with a real peer).
pub fn pseudo_addr() -> SocketAddr {
    let n = NEXT.fetch_add(1, Ordering::Relaxed);
    SocketAddr::new(IpAddr::V6(Ipv6Addr::new(0x100, 0, 0, 0, 0, 0x7065, (n >> 16) as u16, n as u16)), 1)
}

pub fn is_relay(a: &SocketAddr) -> bool {
    matches!(a.ip(), IpAddr::V6(v6) if v6.segments()[..6] == [0x100, 0, 0, 0, 0, 0x7065])
}

/// Room name shared by both sides: unguessable without the session id the client
/// gave the server through the rendezvous.
pub fn room(server_pub: &[u8; 32], session: u64) -> String {
    let mut h = Blake2s256::new();
    h.update(b"peerova relay room v1");
    h.update(server_pub);
    h.update(session.to_le_bytes());
    crate::util::hex(&h.finalize()[..16])
}

/// A relay setting may list several relays: "https://a,https://b". Max 8, http(s) only.
pub fn list(spec: &str) -> Vec<String> {
    let mut out: Vec<String> = vec![];
    for u in spec.split([',', ' ', '\n', '\t']).map(|u| u.trim().trim_end_matches('/')) {
        if (u.starts_with("https://") || u.starts_with("http://")) && !out.iter().any(|x| x == u) && out.len() < 8 {
            out.push(u.to_string());
        }
    }
    out
}

/// Relays a client races at once (the first to complete the handshake wins).
pub const RACE: usize = 3;

pub fn url(base: &str, room: &str, role: &str, lane: usize) -> String {
    format!("{}/api/v1/relay/{room}?role={role}&lane={lane}", base.trim_end_matches('/'))
}

pub trait Stream: Read + Write + Send {}
impl<T: Read + Write + Send> Stream for T {}

/// An open WebSocket. `tcp` is a clone of the socket (for shutdown / timeouts).
pub struct Ws {
    pub io: Box<dyn Stream>,
    pub tcp: TcpStream,
    inbuf: Vec<u8>,
    /// Shared TLS state (None = plain TCP), so the connection can be split into an
    /// independent reader and writer (no polling, no added latency).
    tls: Option<Arc<std::sync::Mutex<rustls::ClientConnection>>>,
}

/// TLS stream over a shared connection state: each call locks only for the TLS work.
struct TlsIo {
    conn: Arc<std::sync::Mutex<rustls::ClientConnection>>,
    sock: TcpStream,
}
impl Read for TlsIo {
    fn read(&mut self, b: &mut [u8]) -> io::Result<usize> {
        rustls::Stream::new(&mut *self.conn.lock().unwrap(), &mut self.sock).read(b)
    }
}
impl Write for TlsIo {
    fn write(&mut self, b: &[u8]) -> io::Result<usize> {
        rustls::Stream::new(&mut *self.conn.lock().unwrap(), &mut self.sock).write(b)
    }
    fn flush(&mut self) -> io::Result<()> {
        rustls::Stream::new(&mut *self.conn.lock().unwrap(), &mut self.sock).flush()
    }
}

/// Encode one client (masked) WebSocket frame.
fn client_frame(op: u8, payload: &[u8]) -> Vec<u8> {
    let mut f = Vec::with_capacity(payload.len() + 14);
    f.push(0x80 | op);
    match payload.len() {
        n if n < 126 => f.push(0x80 | n as u8),
        n if n <= 0xFFFF => {
            f.push(0x80 | 126);
            f.extend_from_slice(&(n as u16).to_be_bytes());
        }
        n => {
            f.push(0x80 | 127);
            f.extend_from_slice(&(n as u64).to_be_bytes());
        }
    }
    let mask = crate::crypto::random_u32().to_be_bytes();
    f.extend_from_slice(&mask);
    f.extend(payload.iter().enumerate().map(|(i, b)| b ^ mask[i & 3]));
    f
}

/// Sending half of a split relay connection.
pub struct WsWriter {
    tls: Option<Arc<std::sync::Mutex<rustls::ClientConnection>>>,
    sock: TcpStream,
}
impl WsWriter {
    pub fn send(&mut self, payload: &[u8]) -> io::Result<()> {
        let f = client_frame(0x2, payload);
        match &self.tls {
            Some(c) => {
                let mut c = c.lock().unwrap();
                c.writer().write_all(&f)?;
                while c.wants_write() {
                    c.write_tls(&mut self.sock)?;
                }
                Ok(())
            }
            None => self.sock.write_all(&f),
        }
    }
}

/// Receiving half: blocks on the socket (no timeout polling) and yields whole messages.
pub struct WsReader {
    tls: Option<Arc<std::sync::Mutex<rustls::ClientConnection>>>,
    sock: TcpStream,
    ws: Ws,
    raw: Vec<u8>,
}
impl WsReader {
    pub fn recv(&mut self, out: &mut Vec<Vec<u8>>) -> io::Result<()> {
        self.ws.drain(out)?;
        if !out.is_empty() {
            return Ok(());
        }
        let n = self.sock.read(&mut self.raw)?;
        if n == 0 {
            return Err(io::Error::new(io::ErrorKind::ConnectionAborted, "relay đóng"));
        }
        match &self.tls {
            Some(c) => {
                let mut c = c.lock().unwrap();
                let mut rd = &self.raw[..n];
                let mut plain = [0u8; 16 * 1024];
                while !rd.is_empty() {
                    c.read_tls(&mut rd)?;
                    c.process_new_packets().map_err(io::Error::other)?;
                    loop {
                        match c.reader().read(&mut plain) {
                            Ok(0) => break,
                            Ok(k) => self.ws.inbuf.extend_from_slice(&plain[..k]),
                            Err(e) if e.kind() == io::ErrorKind::WouldBlock => break,
                            Err(e) => return Err(e),
                        }
                    }
                }
                while c.wants_write() {
                    c.write_tls(&mut self.sock)?; // TLS-level replies (alerts, key updates)
                }
            }
            None => self.ws.inbuf.extend_from_slice(&self.raw[..n]),
        }
        self.ws.drain(out)
    }
}

fn tls_config() -> Arc<rustls::ClientConfig> {
    static CFG: std::sync::OnceLock<Arc<rustls::ClientConfig>> = std::sync::OnceLock::new();
    CFG.get_or_init(|| {
        let roots = rustls::RootCertStore { roots: webpki_roots::TLS_SERVER_ROOTS.to_vec() };
        let provider = Arc::new(rustls::crypto::ring::default_provider());
        Arc::new(
            rustls::ClientConfig::builder_with_provider(provider)
                .with_safe_default_protocol_versions()
                .expect("tls versions")
                .with_root_certificates(roots)
                .with_no_client_auth(),
        )
    })
    .clone()
}

/// Connect `http(s)://host[:port]/path` as a WebSocket. `auth` = extra header value
/// for `Authorization` (the server's lease).
pub fn connect(url: &str, auth: Option<&str>) -> io::Result<Ws> {
    let bad = || io::Error::new(io::ErrorKind::InvalidInput, "URL relay không hợp lệ");
    let (tls, rest) = if let Some(r) = url.strip_prefix("https://") {
        (true, r)
    } else if let Some(r) = url.strip_prefix("http://") {
        (false, r)
    } else {
        return Err(bad());
    };
    let (hostport, path) = rest.find('/').map(|i| (&rest[..i], &rest[i..])).unwrap_or((rest, "/"));
    let (host, port) = match hostport.rsplit_once(':') {
        Some((h, p)) if !h.contains(']') || h.ends_with(']') => (h.trim_matches(['[', ']']), p.parse().map_err(|_| bad())?),
        _ => (hostport, if tls { 443 } else { 80 }),
    };
    // Prefer IPv4 (wider reach); fall back to anything that resolves.
    let addrs: Vec<SocketAddr> = (host, port).to_socket_addrs()?.collect();
    let mut last = io::Error::new(io::ErrorKind::NotFound, "không phân giải được relay");
    let mut tcp = None;
    for a in addrs.iter().filter(|a| a.is_ipv4()).chain(addrs.iter().filter(|a| a.is_ipv6())) {
        match TcpStream::connect_timeout(a, Duration::from_secs(5)) {
            Ok(s) => {
                tcp = Some(s);
                break;
            }
            Err(e) => last = e,
        }
    }
    let tcp = tcp.ok_or(last)?;
    tcp.set_nodelay(true)?;
    tcp.set_read_timeout(Some(Duration::from_secs(8)))?;
    let mut shared = None;
    let mut io: Box<dyn Stream> = if tls {
        let name = rustls::pki_types::ServerName::try_from(host.to_string()).map_err(|_| bad())?;
        let conn = Arc::new(std::sync::Mutex::new(rustls::ClientConnection::new(tls_config(), name).map_err(io::Error::other)?));
        shared = Some(conn.clone());
        Box::new(TlsIo { conn, sock: tcp.try_clone()? })
    } else {
        Box::new(tcp.try_clone()?)
    };
    let mut key = [0u8; 16];
    getrandom::getrandom(&mut key).map_err(|_| io::Error::other("rng"))?;
    let mut req = format!(
        "GET {path} HTTP/1.1\r\nHost: {hostport}\r\nUser-Agent: peerova\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n\
         Sec-WebSocket-Key: {}\r\nSec-WebSocket-Version: 13\r\n",
        b64_std(&key)
    );
    if let Some(a) = auth {
        req += &format!("Authorization: {a}\r\n");
    }
    req += "\r\n";
    io.write_all(req.as_bytes())?;
    io.flush()?;
    // Read the HTTP response head; keep anything after it (first WS frames).
    let mut buf = Vec::with_capacity(1024);
    let mut chunk = [0u8; 1024];
    let head_end = loop {
        let n = io.read(&mut chunk)?;
        if n == 0 {
            return Err(io::Error::new(io::ErrorKind::ConnectionAborted, "relay đóng kết nối"));
        }
        buf.extend_from_slice(&chunk[..n]);
        if let Some(i) = buf.windows(4).position(|w| w == b"\r\n\r\n") {
            break i + 4;
        }
        if buf.len() > 16 * 1024 {
            return Err(io::Error::other("phản hồi relay quá dài"));
        }
    };
    let head = String::from_utf8_lossy(&buf[..head_end]).to_string();
    let status = head.split_whitespace().nth(1).unwrap_or("");
    if status != "101" {
        let line = head.lines().next().unwrap_or("").to_string();
        return Err(io::Error::new(io::ErrorKind::PermissionDenied, format!("relay từ chối: {line}")));
    }
    Ok(Ws { io, tcp, inbuf: buf[head_end..].to_vec(), tls: shared })
}

impl Ws {
    /// Split into independent reader/writer halves (for the engine's relay lanes).
    pub fn split(self) -> io::Result<(WsReader, WsWriter)> {
        self.tcp.set_read_timeout(None)?;
        let w = WsWriter { tls: self.tls.clone(), sock: self.tcp.try_clone()? };
        let r = WsReader { tls: self.tls.clone(), sock: self.tcp.try_clone()?, raw: vec![0u8; 32 * 1024], ws: self };
        Ok((r, w))
    }

    /// Send one binary message (client frames are masked, RFC 6455 §5.3).
    pub fn send(&mut self, payload: &[u8]) -> io::Result<()> {
        self.send_op(0x2, payload)
    }

    fn send_op(&mut self, op: u8, payload: &[u8]) -> io::Result<()> {
        let f = client_frame(op, payload);
        self.io.write_all(&f)?;
        self.io.flush()
    }

    /// Parse every complete message in the buffer. Returns Err on close/protocol error.
    fn drain(&mut self, out: &mut Vec<Vec<u8>>) -> io::Result<()> {
        loop {
            let b = &self.inbuf;
            if b.len() < 2 {
                return Ok(());
            }
            let (fin, op, masked) = (b[0] & 0x80 != 0, b[0] & 0x0F, b[1] & 0x80 != 0);
            let (len, mut off) = match b[1] & 0x7F {
                126 if b.len() >= 4 => (u16::from_be_bytes([b[2], b[3]]) as usize, 4),
                127 if b.len() >= 10 => (u64::from_be_bytes(b[2..10].try_into().unwrap()) as usize, 10),
                126 | 127 => return Ok(()),
                n => (n as usize, 2),
            };
            if len > MAX_MSG || !fin {
                return Err(io::Error::new(io::ErrorKind::InvalidData, "khung relay không hợp lệ"));
            }
            let mask = if masked {
                if b.len() < off + 4 {
                    return Ok(());
                }
                off += 4;
                Some([b[off - 4], b[off - 3], b[off - 2], b[off - 1]])
            } else {
                None
            };
            if b.len() < off + len {
                return Ok(());
            }
            let mut p = b[off..off + len].to_vec();
            if let Some(m) = mask {
                p.iter_mut().enumerate().for_each(|(i, x)| *x ^= m[i & 3]);
            }
            self.inbuf.drain(..off + len);
            match op {
                0x2 => out.push(p),
                0x8 => return Err(io::Error::new(io::ErrorKind::ConnectionAborted, "relay đóng")),
                0x9 => self.send_op(0xA, &p)?,
                _ => {}
            }
        }
    }

    /// Read whatever arrived (bounded by the socket read timeout) into messages.
    pub fn recv(&mut self, out: &mut Vec<Vec<u8>>) -> io::Result<()> {
        self.drain(out)?;
        let mut chunk = [0u8; 16 * 1024];
        match self.io.read(&mut chunk) {
            Ok(0) => Err(io::Error::new(io::ErrorKind::ConnectionAborted, "relay đóng")),
            Ok(n) => {
                self.inbuf.extend_from_slice(&chunk[..n]);
                self.drain(out)
            }
            Err(e) if matches!(e.kind(), io::ErrorKind::WouldBlock | io::ErrorKind::TimedOut) => Ok(()),
            Err(e) => Err(e),
        }
    }
}

fn b64_std(data: &[u8]) -> String {
    const T: &[u8; 64] = b"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    let mut o = String::new();
    for c in data.chunks(3) {
        let n = (c[0] as u32) << 16 | (*c.get(1).unwrap_or(&0) as u32) << 8 | *c.get(2).unwrap_or(&0) as u32;
        for (k, sh) in [18, 12, 6, 0].iter().enumerate() {
            o.push(if k <= c.len() { T[(n >> sh & 63) as usize] as char } else { '=' });
        }
    }
    o
}

/// Standalone relay (`peerova relay`): runs on any machine with a public IP — VPS,
/// office server, or behind nginx/Caddy for TLS. Same protocol as the website relay:
/// `GET /api/v1/relay/<room>?role=s|c&lane=N` (WebSocket). Pairs server and client of a
/// room lane by lane and forwards opaque, end-to-end encrypted packets.
pub mod server {
    use super::*;
    use std::collections::HashMap;
    use std::net::TcpListener;
    use std::sync::Mutex;
    use std::thread;

    const MAX_ROOMS: usize = 20_000;
    const MAX_CONN_PER_IP: usize = 64;
    /// A side waiting alone (or an idle tunnel — engines keepalive every 8 s) is closed after this.
    const IDLE: Duration = Duration::from_secs(60);

    type Peer = Arc<Mutex<TcpStream>>;
    #[derive(Default)]
    struct State {
        lanes: HashMap<String, Peer>,
        per_ip: HashMap<IpAddr, usize>,
    }

    fn frame(payload: &[u8]) -> Vec<u8> {
        let mut f = vec![0x82];
        match payload.len() {
            n if n < 126 => f.push(n as u8),
            n if n <= 0xFFFF => {
                f.push(126);
                f.extend_from_slice(&(n as u16).to_be_bytes());
            }
            n => {
                f.push(127);
                f.extend_from_slice(&(n as u64).to_be_bytes());
            }
        }
        f.extend_from_slice(payload);
        f
    }

    /// Blocks forever serving relay connections on `addr`. Returns the bound address via `ready`.
    pub fn serve(addr: &str, ready: impl FnOnce(SocketAddr), log: impl Fn(String) + Send + Sync + 'static) -> io::Result<()> {
        serve_with(addr, None, ready, log)
    }

    /// `lifetime`: close every connection after this long (tests simulate relay outages).
    pub fn serve_with(addr: &str, lifetime: Option<Duration>, ready: impl FnOnce(SocketAddr), log: impl Fn(String) + Send + Sync + 'static) -> io::Result<()> {
        let l = TcpListener::bind(addr)?;
        ready(l.local_addr()?);
        let st = Arc::new(Mutex::new(State::default()));
        let log = Arc::new(log);
        for s in l.incoming().flatten() {
            let (st, log) = (st.clone(), log.clone());
            thread::spawn(move || {
                let ip = s.peer_addr().map(|a| a.ip().to_canonical()).unwrap_or(IpAddr::V4([0, 0, 0, 0].into()));
                {
                    let mut g = st.lock().unwrap();
                    let n = g.per_ip.entry(ip).or_default();
                    if *n >= MAX_CONN_PER_IP {
                        return;
                    }
                    *n += 1;
                }
                if let Some(d) = lifetime {
                    if let Ok(k) = s.try_clone() {
                        thread::spawn(move || {
                            thread::sleep(d);
                            let _ = k.shutdown(std::net::Shutdown::Both);
                        });
                    }
                }
                if let Err(e) = conn(s, &st) {
                    if e.kind() == io::ErrorKind::PermissionDenied {
                        log(format!("relay: từ chối {ip}: {e}"));
                    }
                }
                let mut g = st.lock().unwrap();
                if let Some(n) = g.per_ip.get_mut(&ip) {
                    *n -= 1;
                    if *n == 0 {
                        g.per_ip.remove(&ip);
                    }
                }
            });
        }
        Ok(())
    }

    fn conn(mut s: TcpStream, st: &Arc<Mutex<State>>) -> io::Result<()> {
        s.set_nodelay(true)?;
        s.set_read_timeout(Some(Duration::from_secs(10)))?;
        let mut head = Vec::with_capacity(512);
        let mut b = [0u8; 1];
        while !head.ends_with(b"\r\n\r\n") {
            if s.read(&mut b)? == 0 || head.len() > 8192 {
                return Ok(());
            }
            head.push(b[0]);
        }
        let head = String::from_utf8_lossy(&head).to_string();
        let path = head.split_whitespace().nth(1).unwrap_or("").to_string();
        let hdr = |k: &str| {
            head.lines().find_map(|l| l.split_once(':').filter(|(n, _)| n.trim().eq_ignore_ascii_case(k)).map(|(_, v)| v.trim().to_string()))
        };
        let (p, q) = path.split_once('?').unwrap_or((&path, ""));
        let room = p.strip_prefix("/api/v1/relay/").unwrap_or("").trim_end_matches('/');
        let arg = |k: &str| q.split('&').find_map(|kv| kv.strip_prefix(&format!("{k}="))).unwrap_or("");
        let (role, lane) = (arg("role"), arg("lane").parse::<u8>().unwrap_or(0));
        let ok = room.len() == 32 && room.bytes().all(|c| c.is_ascii_hexdigit()) && matches!(role, "s" | "c") && lane < 4;
        let Some(key) = hdr("sec-websocket-key").filter(|_| ok) else {
            s.write_all(b"HTTP/1.1 400 Bad Request\r\nConnection: close\r\n\r\n")?;
            return Err(io::Error::new(io::ErrorKind::PermissionDenied, "yêu cầu không hợp lệ"));
        };
        let accept = {
            use sha1::{Digest, Sha1};
            b64_std(&Sha1::digest(format!("{key}258EAFA5-E914-47DA-95CA-C5AB0DC85B11").as_bytes()))
        };
        // Standby ("mux") rooms: one per server, many clients with an 8-byte cid each.
        let cid = if role == "c" { arg("cid").to_string() } else { String::new() };
        let mux = role == "s" && arg("mux") == "1";
        if !cid.is_empty() && (cid.len() != 16 || !cid.bytes().all(|c| c.is_ascii_hexdigit())) {
            return Ok(());
        }
        let cid_bytes: Vec<u8> = (0..cid.len()).step_by(2).filter_map(|i| u8::from_str_radix(&cid[i..i + 2], 16).ok()).collect();
        let me = if cid.is_empty() { format!("{room}/{lane}/{role}") } else { format!("{room}/{lane}/k{cid}") };
        let other = format!("{room}/{lane}/{}", if role == "s" { "c" } else { "s" });
        let any_other = format!("{room}/");
        let peer_role = if role == "s" { "/c" } else { "/s" };
        let mine: Peer = Arc::new(Mutex::new(s.try_clone()?));
        {
            let mut g = st.lock().unwrap();
            if g.lanes.len() >= MAX_ROOMS * 8 {
                drop(g);
                s.write_all(b"HTTP/1.1 503 Busy\r\nConnection: close\r\n\r\n")?;
                return Ok(());
            }
            if let Some(old) = g.lanes.insert(me.clone(), mine.clone()) {
                let _ = old.lock().unwrap().shutdown(std::net::Shutdown::Both);
            }
        }
        s.write_all(format!("HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: {accept}\r\n\r\n").as_bytes())?;
        s.set_read_timeout(Some(IDLE))?;
        let mut ws = Ws { io: Box::new(s.try_clone()?), tcp: s, inbuf: vec![], tls: None };
        let mut msgs = vec![];
        let res = loop {
            msgs.clear();
            // Blocking read (timeout = IDLE): an error or timeout ends the connection.
            let mut chunk = [0u8; 16 * 1024];
            match ws.io.read(&mut chunk) {
                Ok(0) => break Ok(()),
                Ok(n) => ws.inbuf.extend_from_slice(&chunk[..n]),
                Err(e) => break Err(e),
            }
            if let Err(e) = ws.drain(&mut msgs) {
                break Err(e);
            }
            if msgs.is_empty() {
                continue;
            }
            for m in &msgs {
                let (key, payload): (Option<String>, Vec<u8>) = if mux {
                    if m.len() < 9 {
                        continue;
                    }
                    (Some(format!("k{}", crate::util::hex(&m[..8]))), m[8..].to_vec())
                } else if !cid_bytes.is_empty() {
                    let mut v = cid_bytes.clone();
                    v.extend_from_slice(m);
                    (None, v)
                } else {
                    (None, m.clone())
                };
                let peer = {
                    let g = st.lock().unwrap();
                    match &key {
                        Some(k) => g.lanes.get(&format!("{room}/{lane}/{k}")).cloned().or_else(|| g.lanes.iter().find(|(n, _)| n.starts_with(&any_other) && n.ends_with(&format!("/{k}"))).map(|(_, v)| v.clone())),
                        None if !cid_bytes.is_empty() => g.lanes.get(&format!("{room}/{lane}/s")).cloned().or_else(|| g.lanes.iter().find(|(n, _)| n.starts_with(&any_other) && n.ends_with("/s")).map(|(_, v)| v.clone())),
                        None => g.lanes.get(&other).cloned().or_else(|| g.lanes.iter().find(|(n, _)| n.starts_with(&any_other) && n.ends_with(peer_role)).map(|(_, v)| v.clone())),
                    }
                };
                if let Some(p) = peer {
                    let _ = p.lock().unwrap().write_all(&frame(&payload));
                }
            }
        };
        let mut g = st.lock().unwrap();
        if g.lanes.get(&me).is_some_and(|p| Arc::ptr_eq(p, &mine)) {
            g.lanes.remove(&me);
        }
        res
    }
}

#[cfg(test)]
pub mod testserver {
    /// Starts a standalone relay on a random local port; returns its base URL.
    pub fn start() -> String {
        start_with(None)
    }

    /// A relay that cuts every connection after `lifetime` (simulated outage / quota).
    pub fn start_with(lifetime: Option<std::time::Duration>) -> String {
        let (tx, rx) = std::sync::mpsc::channel();
        std::thread::spawn(move || {
            let _ = super::server::serve_with("127.0.0.1:0", lifetime, move |a| tx.send(a).unwrap(), |_| {});
        });
        format!("http://{}", rx.recv().unwrap())
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn relay_lists() {
        assert_eq!(list("https://a/, http://b ,ftp://x,https://a"), vec!["https://a", "http://b"]);
        assert!(list("").is_empty());
    }

    #[test]
    fn pseudo_addresses_are_recognised() {
        let a = pseudo_addr();
        assert!(is_relay(&a));
        assert!(!is_relay(&"[2402:800::1]:5".parse().unwrap()));
        assert_ne!(a, pseudo_addr());
        assert_eq!(room(&[1; 32], 7), room(&[1; 32], 7));
        assert_ne!(room(&[1; 32], 7), room(&[1; 32], 8));
    }

    #[test]
    fn websocket_roundtrip_through_test_relay() {
        let base = testserver::start();
        let r = room(&[3; 32], 9);
        let mut s = connect(&url(&base, &r, "s", 0), None).unwrap();
        let mut c = connect(&url(&base, &r, "c", 0), None).unwrap();
        std::thread::sleep(Duration::from_millis(100));
        c.tcp.set_read_timeout(Some(Duration::from_millis(50))).ok();
        s.tcp.set_read_timeout(Some(Duration::from_millis(50))).ok();
        let big = vec![7u8; 3000];
        c.send(b"hello").unwrap();
        c.send(&big).unwrap();
        let mut got = vec![];
        for _ in 0..40 {
            s.recv(&mut got).unwrap();
            if got.len() == 2 {
                break;
            }
        }
        assert_eq!(got, vec![b"hello".to_vec(), big]);
    }
}
