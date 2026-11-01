//! Website rendezvous (docs/CONTRACT.md §8): the vendor website — not a VPS,
//! not a relay — tells each side the other's public address at connect time.
//! No data ever goes through it; it is only used when no supernode is configured.

use std::net::{IpAddr, SocketAddr};
use std::time::{Duration, Instant};

use crate::json::{escape, Json};
use crate::license::curl;
use crate::pathcache::Nat;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct Endpoint {
    pub ip: IpAddr,
    pub port: u16,
    pub lan: Vec<SocketAddr>,
    pub v6: Vec<SocketAddr>,
    /// Relay base URL announced by the website (overrides the built-in default), if any.
    pub relay: Option<String>,
}

/// Fast-punching hints (CONTRACT §8.5); all optional (older websites send none).
#[derive(Debug, Clone, Copy, Default, PartialEq, Eq)]
pub struct Hints {
    /// Common start of the first full-TTL bursts, mapped onto our own clock.
    pub punch_at: Option<Instant>,
    /// The client's NAT (as the client remembers it for its network).
    pub nat: Option<Nat>,
    /// The server's NAT (as the client remembers it from earlier connections).
    pub peer_nat: Option<Nat>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ClientReq {
    pub session: u64,
    pub ep: Endpoint,
    pub hints: Hints,
}

/// Map a website timestamp (`punch_at`, ms) onto our clock using the website's `now` from
/// the same response; the response took about half the request time to reach us.
fn local_instant(j: &Json, punch_at: Option<u64>, sent: Instant, recv: Instant) -> Option<Instant> {
    let (at, now) = (punch_at?, j.u64("now")?);
    let ahead = Duration::from_millis(at.saturating_sub(now).min(5_000));
    // Half the RTT, capped (the request time includes starting curl).
    let half = (recv.duration_since(sent) / 2).min(Duration::from_millis(150));
    Some(recv + ahead.saturating_sub(half))
}

fn addr_list(v: &[SocketAddr]) -> String {
    let items: Vec<String> = v.iter().take(8).map(|a| escape(&a.to_string())).collect();
    format!("[{}]", items.join(","))
}

fn parse_ep(j: &Json) -> Option<Endpoint> {
    Some(Endpoint {
        ip: j.str("ip")?.parse().ok()?,
        port: j.u64("port").filter(|p| (1..=65535).contains(p))? as u16,
        lan: j.str_list("lan").iter().filter_map(|s| s.parse().ok()).collect(),
        v6: j.str_list("v6").iter().filter_map(|s| s.parse().ok()).collect(),
        relay: None,
    })
}

fn post(base: &str, path: &str, body: &str, lease: Option<&str>) -> Result<Json, String> {
    let url = format!("{}{path}", base.trim_end_matches('/'));
    let auth = lease.map(|l| format!("Authorization: Lease {l}"));
    let mut args = vec!["-X", "POST", "-H", "Content-Type: application/json"];
    if let Some(a) = &auth {
        args.extend(["-H", a.as_str()]);
    }
    if path.ends_with("/poll") {
        args.extend(["--max-time", "30"]); // long-poll (last --max-time wins)
    }
    args.extend(["--data-binary", "@-", &url]);
    // Reach the website over IPv4 so it sees our public IPv4 (what the peer needs for
    // NAT punching); IPv6 candidates travel in the body. IPv6-only hosts fall back.
    let mut v4 = vec!["-4"];
    v4.extend(args.iter().copied());
    let (resp, code) = match curl(&v4, Some(body)) {
        Ok(r) => r,
        Err(_) => curl(&args, Some(body))?,
    };
    let j = Json::parse(&resp).map_err(|_| format!("điểm hẹn trả về không phải JSON (HTTP {code})"))?;
    if code != "200" {
        return Err(j.str("error").unwrap_or("lỗi").to_string());
    }
    Ok(j)
}

/// Server: announce ourselves and collect clients waiting to connect.
/// Returns (our public IP as seen by the website, pending clients, poll interval s).
/// Random client id for the server's standby fallback room.
pub fn random_cid() -> u64 {
    crate::util::random_u64()
}

/// Long-poll: the website holds the request up to `POLL_WAIT` s and answers as soon as a
/// client arrives (older websites ignore `wait` and answer at once).
pub const POLL_WAIT: u64 = 15;

/// Relay URL the website wants clients/servers to use (validated: http(s) only).
fn relay_of(j: &Json) -> Option<String> {
    j.str("relay").filter(|u| (u.starts_with("https://") || u.starts_with("http://")) && u.len() < 300).map(String::from)
}

/// Server: announce ourselves and collect clients waiting to connect (long-poll).
/// `tok`: the poll token the website issued last time (binds this sid to us; CONTRACT §8.4);
/// updated in place when the website issues a new one.
pub fn poll(base: &str, sid: &str, port: u16, lan: &[SocketAddr], v6: &[SocketAddr], tcp: bool, lease: Option<&str>, tok: &mut Option<String>) -> Result<(Option<IpAddr>, Vec<ClientReq>, u64, Option<String>), String> {
    let tok_field = tok.as_deref().map(|t| format!(",\"tok\":{}", escape(t))).unwrap_or_default();
    let body = format!(
        "{{\"sid\":{},\"port\":{port},\"lan\":{},\"v6\":{},\"tcp\":{tcp},\"wait\":{POLL_WAIT}{tok_field}}}",
        escape(sid),
        addr_list(lan),
        addr_list(v6)
    );
    let sent = Instant::now();
    let j = match post(base, "/api/v1/rdv/poll", &body, lease) {
        Ok(j) => j,
        Err(e) => {
            if e == "unauthorized" {
                *tok = None; // token expired/rejected: the next poll asks for a fresh one
            }
            return Err(e);
        }
    };
    if let Some(t) = j.str("tok").filter(|t| t.len() <= 128) {
        *tok = Some(t.to_string());
    }
    let recv = Instant::now();
    let clients = match j.get("clients") {
        Some(Json::Arr(list)) => list
            .iter()
            .filter_map(|c| {
                let hints = Hints {
                    punch_at: local_instant(&j, c.u64("punch_at"), sent, recv),
                    nat: c.str("nat").and_then(Nat::parse),
                    peer_nat: c.str("peer_nat").and_then(Nat::parse),
                };
                Some(ClientReq { session: u64::from_str_radix(c.str("session")?, 16).ok()?, ep: parse_ep(c)?, hints })
            })
            .collect(),
        _ => vec![],
    };
    Ok((j.str("ip").and_then(|s| s.parse().ok()), clients, j.u64("poll").unwrap_or(2).clamp(1, 10), relay_of(&j)))
}

/// Client: ask for the server's public address (and queue ourselves for its next poll).
/// `hints.nat` / `hints.peer_nat` are sent along; the returned hints carry `punch_at`.
pub fn connect(base: &str, sid: &str, session: u64, port: u16, lan: &[SocketAddr], v6: &[SocketAddr], hints: Hints) -> Result<(Option<IpAddr>, Endpoint, Hints), String> {
    let hint = |k: &str, n: Option<Nat>| n.map(|n| format!(",\"{k}\":\"{}\"", n.as_str())).unwrap_or_default();
    let body = format!(
        "{{\"sid\":{},\"session\":\"{session:016x}\",\"port\":{port},\"lan\":{},\"v6\":{}{}{}}}",
        escape(sid),
        addr_list(lan),
        addr_list(v6),
        hint("nat", hints.nat),
        hint("peer_nat", hints.peer_nat)
    );
    let sent = Instant::now();
    let j = post(base, "/api/v1/rdv/connect", &body, None).map_err(|e| if e == "offline" { "server chưa online ở điểm hẹn (website)".into() } else { e })?;
    let recv = Instant::now();
    let mut server = j.get("server").and_then(parse_ep).ok_or("điểm hẹn trả về thiếu địa chỉ server")?;
    server.relay = relay_of(&j);
    let out = Hints { punch_at: local_instant(&j, j.u64("punch_at"), sent, recv), ..hints };
    Ok((j.str("ip").and_then(|s| s.parse().ok()), server, out))
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parses_endpoint() {
        let j = Json::parse(r#"{"ip":"203.0.113.5","port":47000,"lan":["10.0.0.2:47000","bad"],"v6":["[2001:db8::1]:47000"]}"#).unwrap();
        let e = parse_ep(&j).unwrap();
        assert_eq!((e.ip.to_string().as_str(), e.port, e.lan.len(), e.v6.len()), ("203.0.113.5", 47000, 1, 1));
        assert!(parse_ep(&Json::parse(r#"{"ip":"x","port":1}"#).unwrap()).is_none());
        assert_eq!(addr_list(&["1.2.3.4:5".parse().unwrap()]), r#"["1.2.3.4:5"]"#);
    }

    #[test]
    fn maps_punch_time_onto_local_clock() {
        let j = Json::parse(r#"{"now":1000000,"punch_at":1000400}"#).unwrap();
        let sent = Instant::now();
        let recv = sent + Duration::from_millis(100);
        let at = local_instant(&j, j.u64("punch_at"), sent, recv).unwrap();
        assert_eq!(at.duration_since(recv), Duration::from_millis(350), "400 ms ahead minus half the RTT");
        // In the past / absurdly far: clamped; missing fields: none.
        let j = Json::parse(r#"{"now":1000000,"punch_at":900000}"#).unwrap();
        assert_eq!(local_instant(&j, j.u64("punch_at"), sent, recv), Some(recv));
        assert!(local_instant(&Json::parse("{}").unwrap(), Some(5), sent, recv).is_none());
    }
}
