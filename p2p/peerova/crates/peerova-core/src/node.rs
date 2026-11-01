//! A node owns one IPv4 and one IPv6 UDP socket. The same sockets are used for
//! router mapping, reflection and punching, so the NAT binding stays identical
//! across all of them (essential for hole punching). Works on Linux, Windows, macOS.
//!
//! TCP fallback: for networks that block UDP (corporate/school Wi-Fi), the same
//! packets travel over a direct TCP connection framed as `len u16 | packet`.
//! Still peer-to-peer (no relay); the server stays silent to anything without a
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
}

type TcpMap = Arc<Mutex<HashMap<SocketAddr, Vec<TcpLane>>>>;

/// Parallel TCP connections opened by a client in fallback mode.
pub const TCP_LANES: usize = 3;
const LANE_QUEUE: usize = 256;
const LANE_MAX_AGE: Duration = Duration::from_millis(150);
const DUP_MAX: usize = 300;
static LANE_ID: AtomicU64 = AtomicU64::new(1);

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
    });
    thread::spawn(move || {
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
    thread::spawn(move || {
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
        if self.tcp.lock().unwrap().contains_key(&addr) {
            return;
        }
        for _ in 0..TCP_LANES {
            let (sink, map, stop) = (self.sink.clone(), self.tcp.clone(), self.stop.clone());
            thread::spawn(move || {
                if let Ok(s) = TcpStream::connect_timeout(&addr, Duration::from_secs(4)) {
                    add_lane(s, addr, sink, map, stop);
                }
            });
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
            match lanes[(start + k) % n].tx.try_send((now, f.clone())) {
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
