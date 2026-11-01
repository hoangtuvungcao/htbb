//! Wire protocol v1 (PoC).
//!
//! Control datagrams (supernode / reflection): `"PT"` magic | version u8 | type u8 | payload.
//! Integers are big-endian. Addresses: family u8 (4|6, 0 = none) | ip | port u16.
//!
//! Type ranges are reserved so Phase 1 can add the encrypted transport
//! (TCP streams + UDP datagrams tunnelled over one P2P link) without breaking v1:
//!   0x01-0x1F  control / NAT traversal (this file)
//!   0x20-0x3F  reserved: Noise handshake
//!   0x40-0x7F  reserved: encrypted transport frames (streams, datagrams, acks)

use std::net::{IpAddr, Ipv4Addr, Ipv6Addr, SocketAddr};

pub const MAGIC: [u8; 2] = *b"PV";
pub const VERSION: u8 = 1;
pub const MAX_CANDIDATES: usize = 16;

#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
pub enum CandKind {
    /// LAN address of the interface.
    Host = 1,
    /// Global IPv6 address (no NAT, only a firewall to punch).
    Ipv6 = 2,
    /// Port opened on the router via UPnP-IGD / NAT-PMP.
    Mapped = 3,
    /// Public address as observed by another node (our own STUN).
    Reflexive = 4,
}

impl CandKind {
    fn from_u8(v: u8) -> Option<Self> {
        Some(match v {
            1 => Self::Host,
            2 => Self::Ipv6,
            3 => Self::Mapped,
            4 => Self::Reflexive,
            _ => return None,
        })
    }

    pub fn label(self) -> &'static str {
        match self {
            Self::Host => "lan",
            Self::Ipv6 => "ipv6",
            Self::Mapped => "mapped",
            Self::Reflexive => "reflexive",
        }
    }
}

#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
pub struct Candidate {
    pub kind: CandKind,
    pub addr: SocketAddr,
}

#[derive(Clone, Debug, PartialEq, Eq)]
pub enum Packet {
    /// Hole-punch probe. Any node answers with `Pong`.
    Ping {
        session: u64,
        nonce: u64,
    },
    Pong {
        session: u64,
        nonce: u64,
        observed: SocketAddr,
    },
    /// Our own STUN: "what address do you see me as?"
    ReflectReq {
        txid: u64,
    },
    ReflectResp {
        txid: u64,
        observed: SocketAddr,
    },
    /// Server -> supernode keepalive registration (also keeps NAT binding alive).
    /// `lease`: authority-signed lease token (empty = unlicensed). Licensed
    /// supernodes only serve registrations with a valid lease bound to `server_pub`.
    Register {
        server_pub: [u8; 32],
        lease: Vec<u8>,
    },
    RegisterAck {
        observed: SocketAddr,
    },
    /// Client -> supernode: please introduce me to `server_id`.
    /// `proof` = mac1(server_pub, session || server_id): only ticket holders can ask.
    IntroReq {
        server_id: [u8; 16],
        session: u64,
        candidates: Vec<Candidate>,
        proof: [u8; 16],
    },
    /// Supernode -> server: a client wants to connect, punch towards these.
    Intro {
        session: u64,
        candidates: Vec<Candidate>,
    },
    /// Supernode -> client. status 0 = forwarded, 1 = server unknown.
    IntroResp {
        session: u64,
        status: u8,
        server_addr: Option<SocketAddr>,
    },
    /// PoC application data (echo test). Replaced by encrypted frames in Phase 1.
    Data {
        session: u64,
        payload: Vec<u8>,
    },
}

mod ty {
    pub const PING: u8 = 0x01;
    pub const PONG: u8 = 0x02;
    pub const REFLECT_REQ: u8 = 0x03;
    pub const REFLECT_RESP: u8 = 0x04;
    pub const REGISTER: u8 = 0x05;
    pub const REGISTER_ACK: u8 = 0x06;
    pub const INTRO_REQ: u8 = 0x07;
    pub const INTRO: u8 = 0x08;
    pub const INTRO_RESP: u8 = 0x09;
    pub const DATA: u8 = 0x0A;
}

pub(crate) struct W(pub Vec<u8>);

impl W {
    pub fn new() -> Self {
        W(Vec::with_capacity(64))
    }
    pub fn u8(&mut self, v: u8) -> &mut Self {
        self.0.push(v);
        self
    }
    pub fn u16(&mut self, v: u16) -> &mut Self {
        self.0.extend_from_slice(&v.to_be_bytes());
        self
    }
    pub fn u64(&mut self, v: u64) -> &mut Self {
        self.0.extend_from_slice(&v.to_be_bytes());
        self
    }
    pub fn bytes(&mut self, v: &[u8]) -> &mut Self {
        self.0.extend_from_slice(v);
        self
    }
    pub fn addr(&mut self, a: &SocketAddr) -> &mut Self {
        match a.ip() {
            IpAddr::V4(ip) => self.u8(4).bytes(&ip.octets()),
            IpAddr::V6(ip) => self.u8(6).bytes(&ip.octets()),
        };
        self.u16(a.port())
    }
    pub fn opt_addr(&mut self, a: &Option<SocketAddr>) -> &mut Self {
        match a {
            Some(a) => self.addr(a),
            None => self.u8(0),
        }
    }
    pub fn cands(&mut self, c: &[Candidate]) -> &mut Self {
        let c = &c[..c.len().min(MAX_CANDIDATES)];
        self.u8(c.len() as u8);
        for cand in c {
            self.u8(cand.kind as u8).addr(&cand.addr);
        }
        self
    }
}

pub(crate) struct R<'a> {
    b: &'a [u8],
    p: usize,
}

impl<'a> R<'a> {
    pub fn new(b: &'a [u8]) -> Self {
        R { b, p: 0 }
    }
    pub fn take(&mut self, n: usize) -> Option<&'a [u8]> {
        let s = self.b.get(self.p..self.p.checked_add(n)?)?;
        self.p += n;
        Some(s)
    }
    pub fn rest(&mut self) -> &'a [u8] {
        let s = &self.b[self.p..];
        self.p = self.b.len();
        s
    }
    pub fn u8(&mut self) -> Option<u8> {
        Some(self.take(1)?[0])
    }
    pub fn u16(&mut self) -> Option<u16> {
        Some(u16::from_be_bytes(self.take(2)?.try_into().ok()?))
    }
    pub fn u64(&mut self) -> Option<u64> {
        Some(u64::from_be_bytes(self.take(8)?.try_into().ok()?))
    }
    pub fn id16(&mut self) -> Option<[u8; 16]> {
        self.take(16)?.try_into().ok()
    }
    fn addr_body(&mut self, fam: u8) -> Option<SocketAddr> {
        let ip = match fam {
            4 => IpAddr::V4(Ipv4Addr::from(<[u8; 4]>::try_from(self.take(4)?).ok()?)),
            6 => IpAddr::V6(Ipv6Addr::from(<[u8; 16]>::try_from(self.take(16)?).ok()?)),
            _ => return None,
        };
        Some(SocketAddr::new(ip, self.u16()?))
    }
    pub fn addr(&mut self) -> Option<SocketAddr> {
        let fam = self.u8()?;
        self.addr_body(fam)
    }
    pub fn opt_addr(&mut self) -> Option<Option<SocketAddr>> {
        match self.u8()? {
            0 => Some(None),
            fam => self.addr_body(fam).map(Some),
        }
    }
    pub fn cands(&mut self) -> Option<Vec<Candidate>> {
        let n = self.u8()? as usize;
        if n > MAX_CANDIDATES {
            return None;
        }
        (0..n)
            .map(|_| {
                let kind = CandKind::from_u8(self.u8()?)?;
                Some(Candidate {
                    kind,
                    addr: self.addr()?,
                })
            })
            .collect()
    }
}

impl Packet {
    pub fn encode(&self) -> Vec<u8> {
        let mut w = W::new();
        w.bytes(&MAGIC).u8(VERSION);
        match self {
            Packet::Ping { session, nonce } => {
                w.u8(ty::PING).u64(*session).u64(*nonce);
            }
            Packet::Pong {
                session,
                nonce,
                observed,
            } => {
                w.u8(ty::PONG).u64(*session).u64(*nonce).addr(observed);
            }
            Packet::ReflectReq { txid } => {
                w.u8(ty::REFLECT_REQ).u64(*txid);
            }
            Packet::ReflectResp { txid, observed } => {
                w.u8(ty::REFLECT_RESP).u64(*txid).addr(observed);
            }
            Packet::Register { server_pub, lease } => {
                w.u8(ty::REGISTER).bytes(server_pub).bytes(lease);
            }
            Packet::RegisterAck { observed } => {
                w.u8(ty::REGISTER_ACK).addr(observed);
            }
            Packet::IntroReq {
                server_id,
                session,
                candidates,
                proof,
            } => {
                w.u8(ty::INTRO_REQ)
                    .bytes(server_id)
                    .u64(*session)
                    .cands(candidates)
                    .bytes(proof);
            }
            Packet::Intro {
                session,
                candidates,
            } => {
                w.u8(ty::INTRO).u64(*session).cands(candidates);
            }
            Packet::IntroResp {
                session,
                status,
                server_addr,
            } => {
                w.u8(ty::INTRO_RESP)
                    .u64(*session)
                    .u8(*status)
                    .opt_addr(server_addr);
            }
            Packet::Data { session, payload } => {
                w.u8(ty::DATA).u64(*session).bytes(payload);
            }
        }
        w.0
    }

    pub fn decode(buf: &[u8]) -> Option<Packet> {
        let mut r = R::new(buf);
        if r.take(2)? != MAGIC || r.u8()? != VERSION {
            return None;
        }
        Some(match r.u8()? {
            ty::PING => Packet::Ping {
                session: r.u64()?,
                nonce: r.u64()?,
            },
            ty::PONG => Packet::Pong {
                session: r.u64()?,
                nonce: r.u64()?,
                observed: r.addr()?,
            },
            ty::REFLECT_REQ => Packet::ReflectReq { txid: r.u64()? },
            ty::REFLECT_RESP => Packet::ReflectResp {
                txid: r.u64()?,
                observed: r.addr()?,
            },
            ty::REGISTER => Packet::Register {
                server_pub: r.take(32)?.try_into().ok()?,
                lease: r.rest().to_vec(),
            },
            ty::REGISTER_ACK => Packet::RegisterAck {
                observed: r.addr()?,
            },
            ty::INTRO_REQ => Packet::IntroReq {
                server_id: r.id16()?,
                session: r.u64()?,
                candidates: r.cands()?,
                proof: r.id16()?,
            },
            ty::INTRO => Packet::Intro {
                session: r.u64()?,
                candidates: r.cands()?,
            },
            ty::INTRO_RESP => Packet::IntroResp {
                session: r.u64()?,
                status: r.u8()?,
                server_addr: r.opt_addr()?,
            },
            ty::DATA => Packet::Data {
                session: r.u64()?,
                payload: r.rest().to_vec(),
            },
            _ => return None,
        })
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn roundtrip_all() {
        let a4: SocketAddr = "203.0.113.7:47000".parse().unwrap();
        let a6: SocketAddr = "[2402:800::1]:47001".parse().unwrap();
        let cands = vec![
            Candidate {
                kind: CandKind::Host,
                addr: a4,
            },
            Candidate {
                kind: CandKind::Ipv6,
                addr: a6,
            },
        ];
        let pkts = vec![
            Packet::Ping {
                session: 1,
                nonce: 2,
            },
            Packet::Pong {
                session: 1,
                nonce: 2,
                observed: a6,
            },
            Packet::ReflectReq { txid: 9 },
            Packet::ReflectResp {
                txid: 9,
                observed: a4,
            },
            Packet::Register {
                server_pub: [7; 32],
                lease: b"pvl1.x.y".to_vec(),
            },
            Packet::RegisterAck { observed: a4 },
            Packet::IntroReq {
                server_id: [3; 16],
                session: 5,
                candidates: cands.clone(),
                proof: [1; 16],
            },
            Packet::Intro {
                session: 5,
                candidates: cands,
            },
            Packet::IntroResp {
                session: 5,
                status: 0,
                server_addr: Some(a4),
            },
            Packet::IntroResp {
                session: 5,
                status: 1,
                server_addr: None,
            },
            Packet::Data {
                session: 5,
                payload: b"hello".to_vec(),
            },
        ];
        for p in pkts {
            assert_eq!(Packet::decode(&p.encode()), Some(p));
        }
    }

    #[test]
    fn rejects_garbage() {
        assert_eq!(Packet::decode(b""), None);
        assert_eq!(Packet::decode(b"XX\x01\x01"), None);
        assert_eq!(Packet::decode(b"PT\x01\x01\x00"), None); // truncated
    }
}
