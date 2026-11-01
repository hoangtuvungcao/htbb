//! Lobbies & 6-character room codes via the website (CONTRACT §9).
//! Metadata only (name, players, ticket); service data stays peer-to-peer.

use crate::json::{escape, Json};
use crate::license::curl;

pub const CODE_ALPHABET: &str = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

#[derive(Debug, Clone, Default)]
pub struct LobbyInfo {
    pub app: String,
    pub name: String,
    pub public: bool,
    pub max: u32,
    pub mode: String,
    pub region: String,
    pub version: String,
    pub tags: Vec<String>,
}

/// True for strings that look like a room code (6 chars from the code alphabet, any case).
pub fn is_room_code(s: &str) -> bool {
    let s = s.trim();
    s.len() == 6
        && s.chars()
            .all(|c| CODE_ALPHABET.contains(c.to_ascii_uppercase()))
}

fn base(url: &str) -> String {
    url.trim_end_matches('/').to_string()
}

/// Create or refresh the lobby. Returns (room code, owner secret).
pub fn upsert(
    url: &str,
    info: &LobbyInfo,
    ticket: &str,
    players: u32,
    secret: &str,
    lease: Option<&str>,
) -> Result<(String, String), String> {
    let tags: Vec<String> = info.tags.iter().take(8).map(|t| escape(t)).collect();
    let body = format!(
        "{{\"app\":{},\"ticket\":{},\"name\":{},\"public\":{},\"players\":{players},\"max\":{},\"mode\":{},\"region\":{},\"version\":{},\"tags\":[{}],\"secret\":{}}}",
        escape(&info.app),
        escape(ticket),
        escape(&info.name),
        info.public,
        info.max,
        escape(&info.mode),
        escape(&info.region),
        escape(&info.version),
        tags.join(","),
        escape(secret)
    );
    let u = format!("{}/api/v1/lobby/upsert", base(url));
    let auth = lease.map(|l| format!("Authorization: Lease {l}"));
    let mut args = vec!["-X", "POST", "-H", "Content-Type: application/json"];
    if let Some(a) = &auth {
        args.extend(["-H", a.as_str()]);
    }
    args.extend(["--data-binary", "@-", u.as_str()]);
    let (resp, code) = curl(&args, Some(&body))?;
    let j =
        Json::parse(&resp).map_err(|_| format!("lobby: phản hồi không phải JSON (HTTP {code})"))?;
    if code != "200" {
        return Err(format!("lobby: {}", j.str("error").unwrap_or("lỗi")));
    }
    Ok((
        j.str("code").unwrap_or("").to_string(),
        j.str("secret").unwrap_or(secret).to_string(),
    ))
}

/// Room code → ticket.
pub fn resolve(url: &str, code: &str) -> Result<String, String> {
    let code = code.trim().to_ascii_uppercase();
    if !is_room_code(&code) {
        return Err("mã phòng không hợp lệ".into());
    }
    let u = format!("{}/api/v1/lobby/resolve?code={code}", base(url));
    let (resp, status) = curl(&[&u], None)?;
    let j = Json::parse(&resp)
        .map_err(|_| format!("lobby: phản hồi không phải JSON (HTTP {status})"))?;
    match (status.as_str(), j.str("ticket")) {
        ("200", Some(t)) => Ok(t.to_string()),
        _ => Err(format!(
            "không tìm thấy phòng {code} (phòng đã đóng hoặc sai mã)"
        )),
    }
}

/// Keep the lobby alive with the live player count. Calls `on_code` once the code is known.
pub fn spawn_heartbeat(
    url: String,
    info: LobbyInfo,
    ticket: String,
    players: impl Fn() -> u32 + Send + 'static,
    lease: impl Fn() -> Option<String> + Send + 'static,
    on_event: impl Fn(Result<String, String>) + Send + 'static,
) {
    std::thread::Builder::new()
        .name("peerova-lobby".into())
        .spawn(move || {
            let mut secret = String::new();
            let mut announced = false;
            loop {
                match upsert(&url, &info, &ticket, players(), &secret, lease().as_deref()) {
                    Ok((code, s)) => {
                        secret = s;
                        if !announced {
                            announced = true;
                            on_event(Ok(code));
                        }
                    }
                    Err(e) => on_event(Err(e)),
                }
                std::thread::sleep(std::time::Duration::from_secs(15));
            }
        })
        .expect("spawn lobby thread");
}

#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn room_codes() {
        assert!(is_room_code("K7Q2XD") && is_room_code("k7q2xd"));
        assert!(!is_room_code("K7Q2X") && !is_room_code("K7Q2XO") && !is_room_code("pvt_abc"));
    }
}
