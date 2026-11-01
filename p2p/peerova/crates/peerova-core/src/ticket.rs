//! Shareable codes.
//!
//! * Server ticket (`pvt_...`): server static public key (authenticates the
//!   server — no MITM possible, and required to even talk to a dark server),
//!   optional candidate addresses, supernodes to ask.
//!   A *private* ticket omits addresses: the IP is only revealed through a
//!   supernode to someone who proves possession of the ticket.
//! * Join code (`pvj_...`): a client's candidates, pasted into the server
//!   console for fully manual rendezvous (zero infrastructure at all).

use std::net::SocketAddr;

use crate::crypto::key_id;
use crate::util::{b64_decode, b64_encode};
use crate::wire::{Candidate, R, W};

pub const TICKET_PREFIX: &str = "pvt_";
pub const JOIN_PREFIX: &str = "pvj_";

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct Ticket {
    pub server_pub: [u8; 32],
    pub private: bool,
    /// Server also accepts TCP fallback on the same ports (UDP-blocked networks).
    pub tcp: bool,
    /// Server announces itself at the website rendezvous (no supernode/VPS needed).
    pub rdv: bool,
    pub candidates: Vec<Candidate>,
    pub supernodes: Vec<SocketAddr>,
}

impl Ticket {
    pub fn server_id(&self) -> [u8; 16] {
        key_id(&self.server_pub)
    }

    pub fn encode(&self) -> String {
        let mut w = W::new();
        w.u8(2)
            .bytes(&self.server_pub)
            .u8(self.private as u8 | (self.tcp as u8) << 1 | (self.rdv as u8) << 2);
        w.cands(if self.private { &[] } else { &self.candidates });
        let sn = &self.supernodes[..self.supernodes.len().min(8)];
        w.u8(sn.len() as u8);
        for a in sn {
            w.addr(a);
        }
        format!("{TICKET_PREFIX}{}", b64_encode(&w.0))
    }

    pub fn decode(s: &str) -> Result<Ticket, String> {
        let s = s.trim();
        if s.starts_with("pvt1_") {
            return Err("ticket v1 (PoC) không còn hỗ trợ, hãy lấy ticket mới từ server".into());
        }
        let body = s
            .strip_prefix(TICKET_PREFIX)
            .ok_or("ticket phải bắt đầu bằng pvt_")?;
        let raw = b64_decode(body).ok_or("ticket không phải base64 hợp lệ")?;
        let mut r = R::new(&raw);
        let bad = || "ticket hỏng".to_string();
        if r.u8().ok_or_else(bad)? != 2 {
            return Err("phiên bản ticket không hỗ trợ".into());
        }
        let server_pub: [u8; 32] = r.take(32).ok_or_else(bad)?.try_into().unwrap();
        let flags = r.u8().ok_or_else(bad)?;
        let (private, tcp, rdv) = (flags & 1 != 0, flags & 2 != 0, flags & 4 != 0);
        let candidates = r.cands().ok_or_else(bad)?;
        let n = r.u8().ok_or_else(bad)? as usize;
        let supernodes = (0..n.min(8))
            .map(|_| r.addr())
            .collect::<Option<Vec<_>>>()
            .ok_or_else(bad)?;
        Ok(Ticket {
            server_pub,
            private,
            tcp,
            rdv,
            candidates,
            supernodes,
        })
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct JoinCode {
    pub session: u64,
    pub candidates: Vec<Candidate>,
}

impl JoinCode {
    pub fn encode(&self) -> String {
        let mut w = W::new();
        w.u8(1).u64(self.session).cands(&self.candidates);
        format!("{JOIN_PREFIX}{}", b64_encode(&w.0))
    }

    pub fn decode(s: &str) -> Result<JoinCode, String> {
        let body = s
            .trim()
            .strip_prefix(JOIN_PREFIX)
            .ok_or("join code phải bắt đầu bằng pvj_")?;
        let raw = b64_decode(body).ok_or("join code không phải base64 hợp lệ")?;
        let mut r = R::new(&raw);
        let bad = || "join code hỏng".to_string();
        if r.u8().ok_or_else(bad)? != 1 {
            return Err("phiên bản join code không hỗ trợ".into());
        }
        Ok(JoinCode {
            session: r.u64().ok_or_else(bad)?,
            candidates: r.cands().ok_or_else(bad)?,
        })
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::wire::CandKind;

    #[test]
    fn ticket_roundtrip() {
        let mut t = Ticket {
            server_pub: [9; 32],
            private: false,
            tcp: true,
            rdv: true,
            candidates: vec![
                Candidate {
                    kind: CandKind::Mapped,
                    addr: "113.161.2.3:47000".parse().unwrap(),
                },
                Candidate {
                    kind: CandKind::Ipv6,
                    addr: "[2402:800:6314::5]:47000".parse().unwrap(),
                },
            ],
            supernodes: vec!["14.225.1.2:47000".parse().unwrap()],
        };
        assert_eq!(Ticket::decode(&t.encode()).unwrap(), t);
        t.private = true;
        let d = Ticket::decode(&t.encode()).unwrap();
        assert!(
            d.private && d.candidates.is_empty(),
            "private ticket must not leak IPs"
        );
    }

    #[test]
    fn join_roundtrip() {
        let j = JoinCode {
            session: 42,
            candidates: vec![Candidate {
                kind: CandKind::Host,
                addr: "192.168.1.9:5000".parse().unwrap(),
            }],
        };
        assert_eq!(JoinCode::decode(&j.encode()).unwrap(), j);
    }
}
