//! NAT-PMP (RFC 6886) client. PCP (RFC 6887) routers answer NAT-PMP v0 too.

use std::net::{Ipv4Addr, SocketAddrV4, UdpSocket};
use std::time::Duration;

const PORT: u16 = 5351;

fn request(gw: Ipv4Addr, req: &[u8], expect_op: u8) -> Result<Vec<u8>, String> {
    let sock = UdpSocket::bind("0.0.0.0:0").map_err(|e| e.to_string())?;
    let mut wait = Duration::from_millis(250);
    let mut buf = [0u8; 64];
    for _ in 0..3 {
        sock.send_to(req, SocketAddrV4::new(gw, PORT)).map_err(|e| e.to_string())?;
        sock.set_read_timeout(Some(wait)).map_err(|e| e.to_string())?;
        while let Ok((n, from)) = sock.recv_from(&mut buf) {
            if from.ip() != std::net::IpAddr::V4(gw) || n < 8 || buf[0] != 0 || buf[1] != expect_op {
                continue;
            }
            let code = u16::from_be_bytes([buf[2], buf[3]]);
            if code != 0 {
                return Err(format!("router trả mã lỗi NAT-PMP {code}"));
            }
            return Ok(buf[..n].to_vec());
        }
        wait *= 2;
    }
    Err("router không hỗ trợ NAT-PMP/PCP (timeout)".into())
}

pub fn external_address(gw: Ipv4Addr) -> Result<Ipv4Addr, String> {
    let r = request(gw, &[0, 0], 128)?;
    if r.len() < 12 {
        return Err("phản hồi NAT-PMP quá ngắn".into());
    }
    Ok(Ipv4Addr::new(r[8], r[9], r[10], r[11]))
}

/// Map a UDP port. Returns (external_port, lifetime_secs).
pub fn map_udp(gw: Ipv4Addr, internal: u16, suggested: u16, lifetime: u32) -> Result<(u16, u32), String> {
    map(gw, false, internal, suggested, lifetime)
}

/// Map a UDP (op 1) or TCP (op 2) port.
pub fn map(gw: Ipv4Addr, tcp: bool, internal: u16, suggested: u16, lifetime: u32) -> Result<(u16, u32), String> {
    let op = if tcp { 2 } else { 1 };
    let mut req = vec![0u8, op, 0, 0];
    req.extend_from_slice(&internal.to_be_bytes());
    req.extend_from_slice(&suggested.to_be_bytes());
    req.extend_from_slice(&lifetime.to_be_bytes());
    let r = request(gw, &req, 128 + op)?;
    if r.len() < 16 {
        return Err("phản hồi map NAT-PMP quá ngắn".into());
    }
    let ext = u16::from_be_bytes([r[10], r[11]]);
    let life = u32::from_be_bytes([r[12], r[13], r[14], r[15]]);
    Ok((ext, life))
}
