//! Website rendezvous (docs/CONTRACT.md §8): the vendor website — not a VPS,
//! not a relay — tells each side the other's public address at connect time.
//! No data ever goes through it; it is only used when no supernode is configured.

use std::net::{IpAddr, SocketAddr};

use crate::json::{escape, Json};
use crate::license::curl;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct Endpoint {
    pub ip: IpAddr,
    pub port: u16,
    pub lan: Vec<SocketAddr>,
    pub v6: Vec<SocketAddr>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ClientReq {
    pub session: u64,
    pub ep: Endpoint,
}

fn addr_list(v: &[SocketAddr]) -> String {
    let items: Vec<String> = v.iter().take(8).map(|a| escape(&a.to_string())).collect();
    format!("[{}]", items.join(","))
}

fn parse_ep(j: &Json) -> Option<Endpoint> {
    Some(Endpoint {
        ip: j.str("ip")?.parse().ok()?,
        port: j.u64("port").filter(|p| (1..=65535).contains(p))? as u16,
        lan: j
            .str_list("lan")
            .iter()
            .filter_map(|s| s.parse().ok())
            .collect(),
        v6: j
            .str_list("v6")
            .iter()
            .filter_map(|s| s.parse().ok())
            .collect(),
    })
}

fn post(base: &str, path: &str, body: &str) -> Result<Json, String> {
    let url = format!("{}{path}", base.trim_end_matches('/'));
    let (resp, code) = curl(
        &[
            "-X",
            "POST",
            "-H",
            "Content-Type: application/json",
            "--data-binary",
            "@-",
            &url,
        ],
        Some(body),
    )?;
    let j =
        Json::parse(&resp).map_err(|_| format!("điểm hẹn trả về không phải JSON (HTTP {code})"))?;
    if code != "200" {
        return Err(j.str("error").unwrap_or("lỗi").to_string());
    }
    Ok(j)
}

/// Server: announce ourselves and collect clients waiting to connect.
/// Returns (our public IP as seen by the website, pending clients, poll interval s).
pub fn poll(
    base: &str,
    sid: &str,
    port: u16,
    lan: &[SocketAddr],
    v6: &[SocketAddr],
    tcp: bool,
) -> Result<(Option<IpAddr>, Vec<ClientReq>, u64), String> {
    let body = format!(
        "{{\"sid\":{},\"port\":{port},\"lan\":{},\"v6\":{},\"tcp\":{tcp}}}",
        escape(sid),
        addr_list(lan),
        addr_list(v6)
    );
    let j = post(base, "/api/v1/rdv/poll", &body)?;
    let clients = match j.get("clients") {
        Some(Json::Arr(list)) => list
            .iter()
            .filter_map(|c| {
                Some(ClientReq {
                    session: u64::from_str_radix(c.str("session")?, 16).ok()?,
                    ep: parse_ep(c)?,
                })
            })
            .collect(),
        _ => vec![],
    };
    Ok((
        j.str("ip").and_then(|s| s.parse().ok()),
        clients,
        j.u64("poll").unwrap_or(2).clamp(1, 10),
    ))
}

/// Client: ask for the server's public address (and queue ourselves for its next poll).
pub fn connect(
    base: &str,
    sid: &str,
    session: u64,
    port: u16,
    lan: &[SocketAddr],
    v6: &[SocketAddr],
) -> Result<(Option<IpAddr>, Endpoint), String> {
    let body = format!(
        "{{\"sid\":{},\"session\":\"{session:016x}\",\"port\":{port},\"lan\":{},\"v6\":{}}}",
        escape(sid),
        addr_list(lan),
        addr_list(v6)
    );
    let j = post(base, "/api/v1/rdv/connect", &body).map_err(|e| {
        if e == "offline" {
            "server chưa online ở điểm hẹn (website)".into()
        } else {
            e
        }
    })?;
    let server = j
        .get("server")
        .and_then(parse_ep)
        .ok_or("điểm hẹn trả về thiếu địa chỉ server")?;
    Ok((j.str("ip").and_then(|s| s.parse().ok()), server))
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parses_endpoint() {
        let j = Json::parse(r#"{"ip":"203.0.113.5","port":47000,"lan":["10.0.0.2:47000","bad"],"v6":["[2001:db8::1]:47000"]}"#).unwrap();
        let e = parse_ep(&j).unwrap();
        assert_eq!(
            (e.ip.to_string().as_str(), e.port, e.lan.len(), e.v6.len()),
            ("203.0.113.5", 47000, 1, 1)
        );
        assert!(parse_ep(&Json::parse(r#"{"ip":"x","port":1}"#).unwrap()).is_none());
        assert_eq!(
            addr_list(&["1.2.3.4:5".parse().unwrap()]),
            r#"["1.2.3.4:5"]"#
        );
    }
}
