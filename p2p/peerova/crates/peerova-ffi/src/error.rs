//! Stable SDK error codes (`PEEROVA_E_*` in peerova.h) and English error text.
//!
//! The core (shared with the Vietnamese CLI) reports errors as Vietnamese strings. At the C ABI
//! boundary every error is turned into a code plus an English message by [`classify`], the single
//! place that knows those strings. Unknown text maps to [`ErrorCode::Internal`] with a generic
//! English message — the original (possibly Vietnamese) text is never passed through.
//!
//! Text format seen by SDK users (`peerova_last_error`, CONNECT_FAILED / DISCONNECTED payloads):
//! `"<NAME>: <english message>"`, e.g. `"E_TOO_LARGE: unreliable messages are limited to 1180 bytes"`.

use std::fmt;

/// Error codes. The numeric values are part of the C ABI and never change; new codes may be added.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
#[repr(i32)]
pub enum ErrorCode {
    /// No error (the last call on this thread succeeded, or no call failed yet).
    Ok = 0,
    /// Invalid argument or config (NULL pointer, malformed JSON, bad connect code format...).
    InvalidArg = 1,
    /// Payload too large (unreliable messages are limited to 1180 bytes).
    TooLarge = 2,
    /// The peer id is not (or no longer) connected.
    UnknownPeer = 3,
    /// The connection was closed (by the peer or locally).
    Closed = 4,
    /// The 6-character connect code is unknown or expired (the room was closed).
    CodeNotFound = 5,
    /// Network or service error (socket, port in use, website / license server unreachable).
    Network = 6,
    /// Timed out (connecting, waiting for the server, or the peer stopped responding).
    Timeout = 7,
    /// License / lease problem (invalid, expired, rejected, wrong machine).
    License = 8,
    /// The server reached its concurrent-client (CCU) limit.
    CcuLimit = 9,
    /// Access denied (wrong / expired / revoked access token, key not allowed).
    AccessDenied = 10,
    /// The ticket is malformed, corrupted or from an unsupported version.
    InvalidTicket = 11,
    /// Local file error (server or client key file unreadable / unwritable / corrupted).
    Io = 12,
    /// Internal or unclassified error.
    Internal = 99,
}

impl ErrorCode {
    pub const ALL: [ErrorCode; 14] = [
        ErrorCode::Ok,
        ErrorCode::InvalidArg,
        ErrorCode::TooLarge,
        ErrorCode::UnknownPeer,
        ErrorCode::Closed,
        ErrorCode::CodeNotFound,
        ErrorCode::Network,
        ErrorCode::Timeout,
        ErrorCode::License,
        ErrorCode::CcuLimit,
        ErrorCode::AccessDenied,
        ErrorCode::InvalidTicket,
        ErrorCode::Io,
        ErrorCode::Internal,
    ];

    /// Stable symbolic name, e.g. `"E_TOO_LARGE"` (the C constant is `PEEROVA_` + this).
    pub fn name(self) -> &'static str {
        match self {
            ErrorCode::Ok => "E_OK",
            ErrorCode::InvalidArg => "E_INVALID_ARG",
            ErrorCode::TooLarge => "E_TOO_LARGE",
            ErrorCode::UnknownPeer => "E_UNKNOWN_PEER",
            ErrorCode::Closed => "E_CLOSED",
            ErrorCode::CodeNotFound => "E_CODE_NOT_FOUND",
            ErrorCode::Network => "E_NETWORK",
            ErrorCode::Timeout => "E_TIMEOUT",
            ErrorCode::License => "E_LICENSE",
            ErrorCode::CcuLimit => "E_CCU_LIMIT",
            ErrorCode::AccessDenied => "E_ACCESS_DENIED",
            ErrorCode::InvalidTicket => "E_INVALID_TICKET",
            ErrorCode::Io => "E_IO",
            ErrorCode::Internal => "E_INTERNAL",
        }
    }

    pub fn from_i32(v: i32) -> Option<ErrorCode> {
        Self::ALL.into_iter().find(|c| *c as i32 == v)
    }

    /// Parses the `"E_NAME: ..."` prefix of an SDK error text (event payloads, last error).
    pub fn from_text(text: &str) -> Option<ErrorCode> {
        let name = text.split(':').next()?.trim();
        Self::ALL.into_iter().find(|c| c.name() == name)
    }
}

/// A classified error: stable code + English message.
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct SdkError {
    pub code: ErrorCode,
    /// English, without the code prefix.
    pub message: String,
}

impl SdkError {
    pub fn new(code: ErrorCode, message: impl Into<String>) -> SdkError {
        SdkError { code, message: message.into() }
    }
}

/// `"E_NAME: message"`.
impl fmt::Display for SdkError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}: {}", self.code.name(), self.message)
    }
}

// Messages produced by this crate's `Sdk` (Vietnamese, because the Vietnamese CLI shows them too).
// Shared by the producer and `classify`, so the mapping cannot drift.
pub(crate) const VI_UNKNOWN_PEER: &str = "peer không tồn tại";
pub(crate) const VI_TOO_LARGE: &str = "gói unreliable tối đa";
pub(crate) const VI_TOO_LARGE_RELIABLE: &str = "tin reliable tối đa";
pub(crate) const VI_SERVER_NOT_READY: &str = "server không sẵn sàng (timeout)";
pub(crate) const VI_NO_AUTHORITY: &str = "thiếu authority_pubkey";
pub(crate) const VI_BAD_ADDR: &str = "địa chỉ không hợp lệ";

/// Number after `marker` in `s` (e.g. "vị trí 12" → 12).
fn num_after(s: &str, marker: &str) -> Option<u64> {
    let rest = &s[s.find(marker)? + marker.len()..];
    let digits: String = rest.trim_start().chars().take_while(|c| c.is_ascii_digit()).collect();
    digits.parse().ok()
}

/// "(HTTP 404)" → " (HTTP 404)" suffix; empty if absent.
fn http_suffix(s: &str) -> String {
    num_after(s, "HTTP ").map(|n| format!(" (HTTP {n})")).unwrap_or_default()
}

/// "(os error 98)" → " (os error 98)" suffix; empty if absent. The OS's own text is dropped
/// (it is localized on Windows).
fn os_error_suffix(s: &str) -> String {
    num_after(s, "os error ").map(|n| format!(" (os error {n})")).unwrap_or_default()
}

/// Maps any error text from the core or this crate to a code and an English message.
/// Never returns the input text itself: unknown text becomes `E_INTERNAL: internal error`.
pub fn classify(detail: &str) -> SdkError {
    use ErrorCode::*;
    let d = detail.trim();
    let has = |p: &str| d.contains(p);
    fn e(code: ErrorCode, m: impl Into<String>) -> SdkError {
        SdkError::new(code, m)
    }

    // Already classified (idempotent).
    if let Some(code) = ErrorCode::from_text(d) {
        if let Some((_, m)) = d.split_once(':') {
            return e(code, m.trim());
        }
    }

    // ---- send / peers
    if has(VI_UNKNOWN_PEER) {
        return e(UnknownPeer, "unknown peer (not connected or already disconnected)");
    }
    if has(VI_TOO_LARGE_RELIABLE) {
        return e(TooLarge, format!("reliable messages are limited to {} bytes", crate::MAX_RELIABLE_MESSAGE));
    }
    if has(VI_TOO_LARGE) {
        return e(TooLarge, format!("unreliable messages are limited to {} bytes", crate::MAX_UNRELIABLE));
    }

    // ---- connect codes (lobby.rs)
    if has("không tìm thấy phòng") {
        let code = d
            .strip_prefix("không tìm thấy phòng")
            .and_then(|r| r.split_whitespace().next())
            .filter(|c| peerova_core::lobby::is_room_code(c));
        return e(
            CodeNotFound,
            match code {
                Some(c) => format!("connect code {c} not found (the room was closed or the code is wrong)"),
                None => "connect code not found (the room was closed or the code is wrong)".to_string(),
            },
        );
    }
    if has("mã phòng không hợp lệ") {
        return e(InvalidArg, "invalid connect code (expected 6 letters/digits)");
    }
    if d.starts_with("lobby:") {
        return if has("không phải JSON") {
            e(Network, format!("the Peerova website returned an invalid response{}", http_suffix(d)))
        } else {
            e(Network, "the Peerova website rejected the request")
        };
    }

    // ---- network helpers (license.rs curl / PowerShell)
    if has("không kết nối được license server") || has("không chạy được curl") {
        return e(Network, "cannot reach the Peerova service (check the Internet connection / firewall)");
    }
    if has("license server trả về không phải JSON") {
        return e(Network, format!("the license server returned an invalid response{}", http_suffix(d)));
    }

    // ---- timeouts / connection state (engine.rs, conn.rs)
    if has("hết thời gian") {
        return e(
            Timeout,
            "could not connect: timed out. Run `peerova doctor` on both machines \
             (usually both sides are behind CGNAT/symmetric NAT without IPv6)",
        );
    }
    if has(VI_SERVER_NOT_READY) {
        return e(Timeout, "the server did not become ready in time");
    }
    if has("server không phản hồi") {
        return e(Timeout, "the server stopped responding (connection lost)");
    }
    if d == "timeout" {
        return e(Timeout, "connection timed out (the peer stopped responding)");
    }
    if d == "peer closed" {
        return e(Closed, "the peer closed the connection");
    }
    if d == "đóng" {
        return e(Closed, "connection closed");
    }
    if has("client kết nối lại") {
        return e(Closed, "the client reconnected (this connection was replaced)");
    }

    // ---- authenticated refusals from the server (engine.rs `reject_text`)
    if has("Máy chủ đã đầy") || has("máy chủ đã đầy") {
        let cur = num_after(d, "đầy (");
        let max = d.find("đầy (").and_then(|i| num_after(&d[i..], "/"));
        return e(
            CcuLimit,
            match (cur, max) {
                (Some(c), Some(m)) => format!("the server is full ({c}/{m}); try again later"),
                _ => "the server is full; try again later".to_string(),
            },
        );
    }
    if has("Máy chủ tạm chặn") {
        return e(AccessDenied, "temporarily banned by the server (too much traffic); try again in a few minutes");
    }
    if has("Máy chủ từ chối truy cập") {
        return e(AccessDenied, "access denied by the server (invalid, expired or revoked access token, or key not allowed)");
    }

    // ---- CCU / access (engine.rs, access.rs)
    if has("giới hạn CCU") {
        return e(
            CcuLimit,
            match num_after(d, "CCU (") {
                Some(n) => format!("the server's concurrent-client limit ({n}) was reached"),
                None => "the server's concurrent-client limit was reached".to_string(),
            },
        );
    }
    if has("access token") || has("allow-list") || has("token không phải UTF-8") || has("access public key") {
        let why = if has("hết hạn") {
            "the access token has expired"
        } else if has("thu hồi") {
            "the access token has been revoked"
        } else if has("allow-list") {
            "the client key is not in the server's allow-list"
        } else if has("không gửi") {
            "the client did not send an access token"
        } else {
            "the access token is invalid"
        };
        return e(AccessDenied, why);
    }

    // ---- license / lease (license.rs, engine.rs, this crate)
    if has("ẩn IP máy chủ") {
        return e(License, "hiding the server IP needs the Pro plan or higher (or a trial); upgrade on the Peerova website");
    }
    if has(VI_NO_AUTHORITY) {
        return e(License, "missing authority_pubkey for license verification");
    }
    if has("license bị từ chối") {
        return e(License, format!("the license was rejected by the license server{}", http_suffix(d)));
    }
    if has("lease") || has("license") || has("authority") {
        let why = if has("hết hạn") {
            "the lease has expired; connect to the Internet to renew it"
        } else if has("máy khác") {
            "the lease was issued for another machine"
        } else if has("server key khác") {
            "the lease was issued for another server key"
        } else if has("chữ ký") {
            "invalid lease signature"
        } else if has("authority") {
            "invalid license authority public key"
        } else {
            "invalid license or lease"
        };
        return e(License, why);
    }

    // ---- tickets (ticket.rs, engine.rs, CLI)
    if has("thiếu TICKET") {
        return e(InvalidArg, "missing ticket");
    }
    if has("ticket") {
        let why = if has("v1") {
            "ticket v1 is no longer supported; get a new ticket from the server"
        } else if has("pvt_") {
            "a ticket must start with pvt_"
        } else if has("base64") {
            "the ticket is not valid base64"
        } else if has("phiên bản") {
            "unsupported ticket version"
        } else if has("private") {
            "private ticket without any supernode to look up the server address"
        } else if has("NULL") {
            return e(InvalidArg, "ticket is NULL");
        } else {
            "the ticket is corrupted"
        };
        return e(InvalidTicket, why);
    }

    // ---- config (json.rs, this crate)
    if d.starts_with("JSON") {
        return e(
            InvalidArg,
            match num_after(d, "vị trí") {
                Some(n) => format!("invalid config JSON at position {n}"),
                None => "invalid config JSON".to_string(),
            },
        );
    }
    if has(VI_BAD_ADDR) {
        return e(InvalidArg, "invalid address in config");
    }

    // ---- key file (keyfile.rs)
    if has("file key hỏng") {
        return e(Io, "the key file is corrupted");
    }
    if has("không ghi được") {
        return e(Io, format!("cannot write the key file{}", os_error_suffix(d)));
    }

    // ---- sockets (std::io::Error from the engine / node.rs)
    if has("không bind được") {
        return e(
            Network,
            match num_after(d, "port") {
                Some(p) => format!("the port is already in use (UDP port {p})"),
                None => "the port is already in use".to_string(),
            },
        );
    }
    if has("os error") {
        let in_use = ["os error 98)", "os error 48)", "os error 10048)"].iter().any(|p| d.contains(p));
        let denied = ["os error 13)", "os error 10013)"].iter().any(|p| d.contains(p));
        let msg = if in_use {
            "the port is already in use"
        } else if denied {
            "permission denied while opening the socket"
        } else {
            "network socket error"
        };
        return e(Network, format!("{msg}{}", os_error_suffix(d)));
    }

    if has("panic") {
        return e(Internal, "internal error (panic inside peerova)");
    }
    e(Internal, "internal error")
}

#[cfg(test)]
mod tests {
    use super::*;
    use ErrorCode::*;

    fn c(s: &str) -> (ErrorCode, String) {
        let e = classify(s);
        (e.code, e.to_string())
    }

    fn assert_english(s: &str) {
        assert!(s.is_ascii(), "non-English text leaked: {s:?}");
    }

    #[test]
    fn codes_are_stable() {
        let want = [
            (Ok, 0, "E_OK"),
            (InvalidArg, 1, "E_INVALID_ARG"),
            (TooLarge, 2, "E_TOO_LARGE"),
            (UnknownPeer, 3, "E_UNKNOWN_PEER"),
            (Closed, 4, "E_CLOSED"),
            (CodeNotFound, 5, "E_CODE_NOT_FOUND"),
            (Network, 6, "E_NETWORK"),
            (Timeout, 7, "E_TIMEOUT"),
            (License, 8, "E_LICENSE"),
            (CcuLimit, 9, "E_CCU_LIMIT"),
            (AccessDenied, 10, "E_ACCESS_DENIED"),
            (InvalidTicket, 11, "E_INVALID_TICKET"),
            (Io, 12, "E_IO"),
            (Internal, 99, "E_INTERNAL"),
        ];
        assert_eq!(want.len(), ErrorCode::ALL.len());
        for (code, n, name) in want {
            assert_eq!(code as i32, n);
            assert_eq!(code.name(), name);
            assert_eq!(ErrorCode::from_i32(n), Some(code));
            assert_eq!(ErrorCode::from_text(&format!("{name}: x")), Some(code));
        }
        assert_eq!(ErrorCode::from_i32(5000), None);
        assert_eq!(ErrorCode::from_text("hello"), None);
    }

    #[test]
    fn header_matches_codes() {
        let h = include_str!("../include/peerova.h");
        for code in ErrorCode::ALL {
            let line = format!("PEEROVA_{} ", code.name());
            let at = h.find(&line).unwrap_or_else(|| panic!("{} missing from peerova.h", code.name()));
            let rest = h[at + line.len()..].trim_start().trim_start_matches('=').trim_start();
            let n: String = rest.chars().take_while(|c| c.is_ascii_digit()).collect();
            assert_eq!(n.parse::<i32>().unwrap(), code as i32, "{}", code.name());
        }
    }

    #[test]
    fn sdk_layer_messages() {
        assert_eq!(c(VI_UNKNOWN_PEER), (UnknownPeer, "E_UNKNOWN_PEER: unknown peer (not connected or already disconnected)".into()));
        assert_eq!(c("gói unreliable tối đa 1180 byte"), (TooLarge, "E_TOO_LARGE: unreliable messages are limited to 1180 bytes".into()));
        assert_eq!(c(VI_SERVER_NOT_READY).0, Timeout);
        assert_eq!(c(VI_NO_AUTHORITY).0, License);
        assert_eq!(c("địa chỉ không hợp lệ foo:1: failed to lookup address").0, InvalidArg);
        assert_eq!(c("ticket NULL"), (InvalidArg, "E_INVALID_ARG: ticket is NULL".into()));
        assert_eq!(c("thiếu TICKET"), (InvalidArg, "E_INVALID_ARG: missing ticket".into()));
        assert_eq!(c("panic bên trong peerova").0, Internal);
    }

    #[test]
    fn connect_codes() {
        assert_eq!(
            c("không tìm thấy phòng K7Q2XD (phòng đã đóng hoặc sai mã)"),
            (CodeNotFound, "E_CODE_NOT_FOUND: connect code K7Q2XD not found (the room was closed or the code is wrong)".into())
        );
        assert_eq!(c("mã phòng không hợp lệ").0, InvalidArg);
        assert_eq!(c("lobby: phản hồi không phải JSON (HTTP 502)"), (Network, "E_NETWORK: the Peerova website returned an invalid response (HTTP 502)".into()));
        assert_eq!(c("lobby: lỗi").0, Network);
        assert_eq!(c("không kết nối được license server: curl: (6) Could not resolve host").0, Network);
        assert_eq!(c("không chạy được curl: No such file").0, Network);
        assert_eq!(c("không chạy được curl hay PowerShell: x").0, Network);
    }

    #[test]
    fn server_refusals() {
        assert_eq!(c("Máy chủ đã đầy (3/3) — thử lại sau"), (CcuLimit, "E_CCU_LIMIT: the server is full (3/3); try again later".into()));
        assert_eq!(c("từ chối người chơi từ 1.2.3.4:5: máy chủ đã đầy (10/10) — đạt giới hạn CCU của license").0, CcuLimit);
        let t = c("Máy chủ từ chối truy cập: token / key không hợp lệ, hết hạn hoặc đã bị thu hồi");
        assert_eq!(t.0, AccessDenied);
        assert!(t.1.starts_with("E_ACCESS_DENIED: access denied by the server"));
        let t = c("Máy chủ tạm chặn kết nối này (gửi dữ liệu quá dồn dập) — thử lại sau ít phút");
        assert_eq!(t.0, AccessDenied);
        assert!(t.1.contains("temporarily banned"));
        for s in [&c("Máy chủ đã đầy (3/3) — thử lại sau").1, &t.1] {
            assert_english(s);
        }
    }

    #[test]
    fn connection_events() {
        let t = c("không kết nối được: hết thời gian. Chạy `peerova doctor` ở cả 2 máy (thường do cả 2 sau CGNAT/symmetric NAT và không có IPv6)");
        assert_eq!(t.0, Timeout);
        assert!(t.1.starts_with("E_TIMEOUT: could not connect: timed out"));
        assert_eq!(c("timeout").0, Timeout);
        assert_eq!(c("server không phản hồi (mất kết nối)").0, Timeout);
        assert_eq!(c("peer closed"), (Closed, "E_CLOSED: the peer closed the connection".into()));
        assert_eq!(c("đóng").0, Closed);
        assert_eq!(c("client kết nối lại").0, Closed);
        assert_eq!(c("đạt giới hạn CCU (3) của license"), (CcuLimit, "E_CCU_LIMIT: the server's concurrent-client limit (3) was reached".into()));
    }

    #[test]
    fn access_license_ticket_config_io() {
        assert_eq!(c("access token của alice đã hết hạn"), (AccessDenied, "E_ACCESS_DENIED: the access token has expired".into()));
        assert_eq!(c("access token của alice đã bị thu hồi").0, AccessDenied);
        assert_eq!(c("key không trong allow-list").0, AccessDenied);
        assert_eq!(c("client không gửi access token").0, AccessDenied);
        assert_eq!(c("chữ ký access token không hợp lệ").0, AccessDenied);
        assert_eq!(c("lease đã hết hạn 30 giây trước — cần kết nối web để gia hạn").0, License);
        assert_eq!(c("lease cấp cho máy khác (machine id không khớp)").0, License);
        assert_eq!(c("license bị từ chối: bad key (HTTP 403)"), (License, "E_LICENSE: the license was rejected by the license server (HTTP 403)".into()));
        assert_eq!(c("public key authority phải là 32 byte base64url").0, License);
        assert_eq!(c("ẩn IP máy chủ có ở gói Pro trở lên (và khi dùng thử) — nâng cấp tại website").0, License);
        assert_eq!(c("ticket v1 (PoC) không còn hỗ trợ, hãy lấy ticket mới từ server").0, InvalidTicket);
        assert_eq!(c("ticket phải bắt đầu bằng pvt_"), (InvalidTicket, "E_INVALID_TICKET: a ticket must start with pvt_".into()));
        assert_eq!(c("ticket không phải base64 hợp lệ").0, InvalidTicket);
        assert_eq!(c("ticket hỏng").0, InvalidTicket);
        assert_eq!(c("phiên bản ticket không hỗ trợ").0, InvalidTicket);
        assert_eq!(c("ticket private nhưng không có supernode nào để hỏi địa chỉ server").0, InvalidTicket);
        assert_eq!(c("JSON lỗi ở vị trí 7: thiếu :"), (InvalidArg, "E_INVALID_ARG: invalid config JSON at position 7".into()));
        assert_eq!(c("JSON thừa ký tự ở vị trí 3").0, InvalidArg);
        assert_eq!(c("file key hỏng").0, Io);
        assert_eq!(c("không ghi được /x/server.key: Permission denied (os error 13)"), (Io, "E_IO: cannot write the key file (os error 13)".into()));
        assert_eq!(c("không bind được UDP port 47000"), (Network, "E_NETWORK: the port is already in use (UDP port 47000)".into()));
        assert_eq!(c("Address already in use (os error 98)"), (Network, "E_NETWORK: the port is already in use (os error 98)".into()));
        assert_eq!(c("Chỉ một cách sử dụng địa chỉ socket thường được phép. (os error 10048)").0, Network);
    }

    #[test]
    fn real_core_errors_are_classified() {
        // Exercise the actual core functions so a reworded core message shows up here.
        let ticket = peerova_core::ticket::Ticket::decode("nope").unwrap_err();
        assert_eq!(classify(&ticket).code, InvalidTicket);
        let ticket = peerova_core::ticket::Ticket::decode("pvt_!!!").unwrap_err();
        assert_eq!(classify(&ticket).code, InvalidTicket);
        let json = peerova_core::json::Json::parse("{\"a\":").unwrap_err();
        assert_eq!(classify(&json).code, InvalidArg);
        let json = peerova_core::json::Json::parse("{} x").unwrap_err();
        assert_eq!(classify(&json).code, InvalidArg);
        let code = peerova_core::lobby::resolve("http://127.0.0.1:9", "bad").unwrap_err();
        assert_eq!(classify(&code).code, InvalidArg);
        let pk = peerova_core::license::parse_pubkey("xx").unwrap_err();
        assert_eq!(classify(&pk).code, License);
    }

    #[test]
    fn fallback_never_leaks_input() {
        for s in ["", "lỗi lạ chưa biết", "something new", "ủa"] {
            let (code, text) = c(s);
            assert_eq!(code, Internal);
            assert_eq!(text, "E_INTERNAL: internal error");
        }
    }

    #[test]
    fn all_outputs_are_english() {
        let samples = [
            "peer không tồn tại",
            "gói unreliable tối đa 1180 byte",
            "không tìm thấy phòng ABCDEF (phòng đã đóng hoặc sai mã)",
            "không tìm thấy phòng đó",
            "lobby: lỗi",
            "không kết nối được: hết thời gian",
            "đạt giới hạn CCU của license",
            "access token sai định dạng",
            "lease không đúng định dạng pvl1.<payload>.<sig>",
            "ticket hỏng",
            "JSON lỗi ở vị trí 1: ký tự lạ",
            "không ghi được C:\\Người dùng\\server.key: Từ chối truy cập. (os error 5)",
            "Lỗi socket lạ (os error 10049)",
        ];
        for s in samples {
            let e = classify(s);
            assert_english(&e.to_string());
            assert_ne!(e.code, Ok);
            // Idempotent: classifying the English text again keeps code and message.
            assert_eq!(classify(&e.to_string()), e);
        }
    }
}
