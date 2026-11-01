//! L3 VPN mode: a virtual network interface (TUN) on every peer, so
//! EVERY IP protocol works — ICMP ping, FTP active+passive, LAN broadcast
//! discovery, SMB, anything — not just forwarded ports.
//!
//! * Server: 10.77.0.1/24 (configurable), assigns 10.77.0.N to each client.
//! * IP packets travel as encrypted datagrams `0x07 | packet` (never retransmitted;
//!   TCP inside the VPN does its own recovery). Client↔client traffic is switched
//!   by the server; IPv4 broadcast is flooded to all peers (LAN game discovery).
//! * Platform TUN backends (`vpn/*.rs`): Linux `/dev/net/tun` (`pv0`…, root /
//!   CAP_NET_ADMIN), Windows Wintun (adapter "Peerova", Administrator, wintun.dll
//!   next to peerova.exe), macOS `utunN` (root).

use std::collections::HashMap;
use std::io;
use std::net::Ipv4Addr;
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::{Arc, Mutex};
use std::thread;
use std::time::Duration;

use crate::engine::{Cmd, Engine, EngineEvent, Handle};

#[cfg(target_os = "linux")]
#[path = "vpn/linux.rs"]
mod sys;
#[cfg(target_os = "macos")]
#[path = "vpn/macos.rs"]
mod sys;
#[cfg(windows)]
#[path = "vpn/windows.rs"]
mod sys;
#[cfg(not(any(target_os = "linux", target_os = "macos", windows)))]
#[path = "vpn/unsupported.rs"]
mod sys;

const KIND_IP: u8 = 0x07;
/// Control channel stream (reliable, bidirectional): IP assignment.
const CTRL_STREAM: u32 = 0x8000_00FF;
/// Fits one tunnel datagram (MAX_DATAGRAM − 1 byte kind) with headroom.
pub const VPN_MTU: u32 = 1180;

/// A platform TUN device. Dropping it stops the reader thread; the OS interface
/// goes away once that thread has let go (≤ ~250 ms).
pub struct Tun {
    dev: Arc<sys::Device>,
    stop: Arc<AtomicBool>,
    pub name: String,
}

impl Tun {
    /// Create a new interface (Linux `pvN`, Windows "Peerova", macOS `utunN`).
    pub fn open() -> io::Result<Tun> {
        let (dev, name) = sys::Device::open()?;
        Ok(Tun {
            dev: Arc::new(dev),
            stop: Arc::default(),
            name,
        })
    }

    /// Assign `addr/prefix` (`peer` = the server's address, used for point-to-point
    /// links), set MTU [`VPN_MTU`] and bring the interface up.
    pub fn configure(&self, addr: Ipv4Addr, prefix: u8, peer: Ipv4Addr) -> io::Result<()> {
        sys::configure(&self.dev, &self.name, addr, prefix, peer, VPN_MTU)
    }

    /// Write one IP packet into the local stack.
    pub fn send(&self, pkt: &[u8]) -> io::Result<()> {
        self.dev.send(pkt)
    }

    /// Pump packets read from the interface to the connections chosen by `route`.
    fn pump_to(&self, h: Handle, route: impl Fn(&[u8]) -> Vec<u32> + Send + 'static) {
        let (dev, stop) = (self.dev.clone(), self.stop.clone());
        thread::spawn(move || {
            let mut buf = vec![0u8; 65536];
            while !stop.load(Ordering::Relaxed) {
                match dev.recv(&mut buf) {
                    Ok(None) => {}
                    Ok(Some(0)) => break,
                    Ok(Some(n)) => {
                        if n as u32 > VPN_MTU {
                            continue;
                        }
                        for conn in route(&buf[..n]) {
                            h.send(Cmd::Datagram {
                                conn,
                                data: frame(&buf[..n]),
                            });
                        }
                    }
                    Err(e) if e.kind() == io::ErrorKind::Interrupted => {}
                    Err(_) => break,
                }
            }
        });
    }
}

impl Drop for Tun {
    fn drop(&mut self) {
        self.stop.store(true, Ordering::Relaxed);
    }
}

fn dst_v4(pkt: &[u8]) -> Option<Ipv4Addr> {
    (pkt.len() >= 20 && pkt[0] >> 4 == 4).then(|| Ipv4Addr::new(pkt[16], pkt[17], pkt[18], pkt[19]))
}

fn frame(pkt: &[u8]) -> Vec<u8> {
    let mut d = Vec::with_capacity(1 + pkt.len());
    d.push(KIND_IP);
    d.extend_from_slice(pkt);
    d
}

/// Parse "10.77.0.0/24" → (network, prefix).
pub fn parse_subnet(s: &str) -> Result<(Ipv4Addr, u8), String> {
    let (ip, p) = s.split_once('/').ok_or("subnet dạng a.b.c.d/nn")?;
    let ip: Ipv4Addr = ip.parse().map_err(|_| "subnet không hợp lệ")?;
    let p: u8 = p.parse().map_err(|_| "prefix không hợp lệ")?;
    if !(16..=29).contains(&p) {
        return Err("prefix VPN phải từ /16 tới /29".into());
    }
    let mask = u32::MAX << (32 - p);
    Ok((Ipv4Addr::from(u32::from(ip) & mask), p))
}

fn broadcast(net: Ipv4Addr, prefix: u8) -> Ipv4Addr {
    Ipv4Addr::from(u32::from(net) | !(u32::MAX << (32 - prefix)))
}

/// Server side: own the subnet, assign addresses, switch packets.
pub fn serve(
    engine: &Engine,
    subnet: &str,
    mut on_event: impl FnMut(&EngineEvent),
) -> Result<(), String> {
    let (net, prefix) = parse_subnet(subnet)?;
    let me = Ipv4Addr::from(u32::from(net) + 1);
    let bcast = broadcast(net, prefix);
    let tun = Tun::open().map_err(|e| e.to_string())?;
    tun.configure(me, prefix, me).map_err(|e| e.to_string())?;
    on_event(&EngineEvent::Log(format!(
        "VPN: {} = {me}/{prefix} — ping, FTP, mọi giao thức IP",
        tun.name
    )));
    #[cfg(windows)]
    on_event(&EngineEvent::Log("VPN: Windows Firewall có thể chặn ping/dịch vụ đến từ mạng VPN — mở rule cho subnet nếu cần".into()));
    let peers: Arc<Mutex<HashMap<Ipv4Addr, u32>>> = Arc::default();
    let by_conn: Arc<Mutex<HashMap<u32, Ipv4Addr>>> = Arc::default();
    let h = engine.handle.clone();
    {
        let peers = peers.clone();
        tun.pump_to(h.clone(), move |pkt| {
            let m = peers.lock().unwrap();
            match dst_v4(pkt) {
                Some(d) if d == bcast || d.is_multicast() => m.values().copied().collect(),
                Some(d) => m.get(&d).copied().into_iter().collect(),
                None => vec![],
            }
        });
    }
    let mut next_host: u32 = 2;
    loop {
        let ev = match engine.events().recv_timeout(Duration::from_secs(1)) {
            Ok(ev) => ev,
            Err(std::sync::mpsc::RecvTimeoutError::Timeout) => continue,
            Err(_) => return Ok(()),
        };
        on_event(&ev);
        match ev {
            EngineEvent::Connected { conn, .. } => {
                // Find a free address in the subnet.
                let mut ip = None;
                let size = 1u32 << (32 - prefix);
                for _ in 0..size {
                    let cand = Ipv4Addr::from(u32::from(net) + next_host);
                    next_host = if next_host + 1 >= size - 1 {
                        2
                    } else {
                        next_host + 1
                    };
                    if !peers.lock().unwrap().contains_key(&cand) {
                        ip = Some(cand);
                        break;
                    }
                }
                let Some(ip) = ip else {
                    on_event(&EngineEvent::Log("VPN: hết địa chỉ trong subnet".into()));
                    h.send(Cmd::Close { conn });
                    continue;
                };
                peers.lock().unwrap().insert(ip, conn);
                by_conn.lock().unwrap().insert(conn, ip);
                let msg = format!("{ip}/{prefix} {me}\n");
                let mut d = (msg.len() as u32).to_be_bytes().to_vec();
                d.extend_from_slice(msg.as_bytes());
                h.send(Cmd::StreamWrite {
                    conn,
                    id: CTRL_STREAM,
                    data: d,
                });
                on_event(&EngineEvent::Log(format!(
                    "VPN: cấp {ip} cho client {conn:08x}"
                )));
            }
            EngineEvent::Disconnected { conn, .. } => {
                if let Some(ip) = by_conn.lock().unwrap().remove(&conn) {
                    peers.lock().unwrap().remove(&ip);
                }
            }
            EngineEvent::StreamData { conn, id, data } => h.send(Cmd::StreamConsumed {
                conn,
                id,
                n: data.len(),
            }),
            EngineEvent::Datagram { conn, data } if data.first() == Some(&KIND_IP) => {
                let pkt = &data[1..];
                // Anti-spoofing: a client may only send from its own address.
                let src_ok = pkt.len() >= 20
                    && by_conn
                        .lock()
                        .unwrap()
                        .get(&conn)
                        .is_some_and(|ip| pkt[12..16] == ip.octets());
                if !src_ok {
                    continue;
                }
                let dst = dst_v4(pkt);
                let target = dst.and_then(|d| peers.lock().unwrap().get(&d).copied());
                if let Some(other) = target {
                    h.send(Cmd::Datagram { conn: other, data }); // client ↔ client, switched here
                } else {
                    if dst.is_some_and(|d| d == bcast || d.is_multicast()) {
                        for (&c, _) in by_conn.lock().unwrap().iter().filter(|(&c, _)| c != conn) {
                            h.send(Cmd::Datagram {
                                conn: c,
                                data: data.clone(),
                            });
                        }
                    }
                    let _ = tun.send(pkt);
                }
            }
            _ => {}
        }
    }
}

/// Client side: wait for the address, create the interface, pump packets.
pub fn client(engine: &Engine, mut on_event: impl FnMut(&EngineEvent)) -> Result<(), String> {
    let h = engine.handle.clone();
    let mut conn_id = 0u32;
    let mut ctrl = Vec::new();
    let mut writer: Option<Tun> = None;
    loop {
        let Ok(ev) = engine.events().recv() else {
            return Err("engine dừng".into());
        };
        on_event(&ev);
        match ev {
            EngineEvent::Connected { conn, .. } => conn_id = conn,
            EngineEvent::StreamData { conn, id, data } if id == CTRL_STREAM && writer.is_none() => {
                h.send(Cmd::StreamConsumed {
                    conn,
                    id,
                    n: data.len(),
                });
                ctrl.extend_from_slice(&data);
                if ctrl.len() < 4 {
                    continue;
                }
                let n = u32::from_be_bytes(ctrl[..4].try_into().unwrap()) as usize;
                if ctrl.len() < 4 + n {
                    continue;
                }
                let line = String::from_utf8_lossy(&ctrl[4..4 + n]).to_string();
                let mut parts = line.split_whitespace();
                let (Some(cidr), Some(gw)) = (parts.next(), parts.next()) else {
                    return Err("VPN: gói cấp địa chỉ hỏng".into());
                };
                let (ip, prefix) = cidr.split_once('/').ok_or("VPN: địa chỉ hỏng")?;
                let ip: Ipv4Addr = ip.parse().map_err(|_| "VPN: địa chỉ hỏng")?;
                let prefix: u8 = prefix.parse().map_err(|_| "VPN: prefix hỏng")?;
                let gw_ip: Ipv4Addr = gw.parse().map_err(|_| "VPN: địa chỉ server hỏng")?;
                let tun = Tun::open().map_err(|e| e.to_string())?;
                tun.configure(ip, prefix, gw_ip)
                    .map_err(|e| e.to_string())?;
                let c = conn_id;
                tun.pump_to(h.clone(), move |_| vec![c]);
                on_event(&EngineEvent::Log(format!(
                    "✔ VPN: {} = {ip}/{prefix}, server {gw} — thử: ping {gw}",
                    tun.name
                )));
                writer = Some(tun);
            }
            EngineEvent::StreamData { conn, id, data } => h.send(Cmd::StreamConsumed {
                conn,
                id,
                n: data.len(),
            }),
            EngineEvent::Datagram { data, .. } if data.first() == Some(&KIND_IP) => {
                if let Some(w) = writer.as_ref() {
                    let _ = w.send(&data[1..]);
                }
            }
            EngineEvent::ConnectFailed(e) => return Err(e),
            EngineEvent::Disconnected { reason, .. } => {
                return Err(format!("mất kết nối: {reason}"))
            }
            _ => {}
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn subnets() {
        assert_eq!(
            parse_subnet("10.77.0.9/24").unwrap(),
            ("10.77.0.0".parse().unwrap(), 24)
        );
        assert_eq!(
            broadcast("10.77.0.0".parse().unwrap(), 24),
            "10.77.0.255".parse::<Ipv4Addr>().unwrap()
        );
        assert!(parse_subnet("10.0.0.0/8").is_err());
        let mut p = vec![0x45u8; 20];
        p[16..20].copy_from_slice(&[10, 77, 0, 2]);
        assert_eq!(dst_v4(&p), Some("10.77.0.2".parse().unwrap()));
    }
}
