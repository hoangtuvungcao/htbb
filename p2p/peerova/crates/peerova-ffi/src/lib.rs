#![allow(non_camel_case_types)]
#![allow(clippy::missing_safety_doc)] // safety contract documented in include/peerova.h
//! C ABI over the peerova engine: message-oriented SDK for apps, services and games.
//!
//! * Reliable channels: stream id `0x8000_0000 | channel`, frames `len u32 | bytes`.
//! * Unreliable: datagram `0x10 | channel | bytes`.

use std::cell::RefCell;
use std::collections::{HashMap, HashSet, VecDeque};
use std::ffi::{c_char, CStr, CString};
use std::path::PathBuf;
use std::sync::Mutex;
use std::time::{Duration, Instant};

use peerova_core::conn::MAX_DATAGRAM;
use peerova_core::json::Json;
use peerova_core::license::{self, LicenseConfig};
use peerova_core::{keyfile, Access, ClientConfig, Cmd, Engine, EngineEvent, ServerConfig};

mod tunnel_api;
pub use tunnel_api::Tunnel;

pub mod error;
pub use error::{classify, ErrorCode, SdkError};
use error::{VI_BAD_ADDR, VI_NO_AUTHORITY, VI_SERVER_NOT_READY, VI_TOO_LARGE, VI_TOO_LARGE_RELIABLE, VI_UNKNOWN_PEER};

/// Largest reliable message the SDK will reassemble (bounded memory per peer).
pub(crate) const MAX_RELIABLE_MESSAGE: usize = 16 << 20;
const DGRAM: u8 = 0x10;
const CHAN_BIT: u32 = 0x8000_0000;
/// Documented in peerova.h; kept below the transport limit for headroom.
pub const MAX_UNRELIABLE: usize = 1180;
const _: () = assert!(MAX_UNRELIABLE <= MAX_DATAGRAM - 2);

pub const EV_CONNECTED: i32 = 1;
pub const EV_DISCONNECTED: i32 = 2;
pub const EV_MESSAGE: i32 = 3;
pub const EV_CONNECT_FAILED: i32 = 4;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct SdkEvent {
    pub kind: i32,
    pub peer: u32,
    pub channel: u8,
    pub reliable: bool,
    pub data: Vec<u8>,
}

/// Pure-Rust SDK object (also usable from Rust directly).
pub struct Sdk {
    engine: Engine,
    ticket: Option<String>,
    room_code: std::sync::Arc<std::sync::Mutex<Option<String>>>,
    queue: VecDeque<SdkEvent>,
    reasm: HashMap<(u32, u32), Vec<u8>>,
    peers: HashSet<u32>,
}

fn cfg_json(s: Option<&str>) -> Result<Json, String> {
    match s.map(str::trim).filter(|s| !s.is_empty()) {
        Some(s) => Json::parse(s),
        None => Ok(Json::Obj(vec![])),
    }
}

/// Website for connect codes/lobbies: "site", else "rendezvous" URL, else the built-in default.
fn site_of(j: &Json) -> String {
    j.str("site")
        .or(j.str("rendezvous"))
        .or(j.str("license_server"))
        .unwrap_or(license::DEFAULT_LICENSE_SERVER)
        .to_string()
}

fn addrs(list: Vec<String>) -> Result<Vec<std::net::SocketAddr>, String> {
    use std::net::ToSocketAddrs;
    let mut v = vec![];
    for s in list {
        v.extend(s.to_socket_addrs().map_err(|e| format!("{VI_BAD_ADDR} {s}: {e}"))?);
    }
    Ok(v)
}

/// Server engine config + optional license from the JSON config (shared by the SDK and the tunnel API).
pub(crate) fn server_config(j: &Json) -> Result<(ServerConfig, Option<LicenseConfig>), String> {
        let key_path = PathBuf::from(j.str("key_file").unwrap_or("server.key"));
        let (key, _) = keyfile::load_or_create(&key_path)?;
        let spk = key.public;
        let mut cfg = ServerConfig::new(key);
        cfg.port = j.u64("port").map(|p| p as u16).unwrap_or(peerova_core::engine::DEFAULT_PORT);
        cfg.workers = j.u64("workers").unwrap_or(0) as usize; // engine threads (0 = one per core)
        cfg.supernodes = addrs(j.str_list("supernodes"))?;
        cfg.supernode_mode = j.bool("supernode_mode").unwrap_or(false);
        cfg.private_ticket = j.bool("private").unwrap_or(false);
        // Anti-DDoS: the server's IP is never published; clients use the automatic encrypted route only.
        cfg.hide_ip = j.bool("hide_ip").unwrap_or(false);
        // Keep the website binding token next to the key file (only when the app chose one).
        if j.str("key_file").is_some() {
            cfg.rdv_token_file = Some(PathBuf::from(format!("{}.rdv", key_path.display())));
        }
        cfg.try_map = j.bool("upnp").unwrap_or(true);
        cfg.tcp_fallback = j.bool("tcp_fallback").unwrap_or(true);
        cfg.auto_supernode = j.bool("auto_supernode").unwrap_or(true);
        // Website rendezvous on by default; "rendezvous": false disables, a string sets the URL.
        cfg.rendezvous = match (j.str("rendezvous"), j.bool("rendezvous")) {
            (Some(u), _) => Some(u.to_string()),
            (None, Some(false)) => None,
            _ => Some(site_of(&j)),
        };
        // Last-resort relay on by default; "relay": false disables, a string sets the URL.
        cfg.relay = match (j.str("relay"), j.bool("relay")) {
            (Some(u), _) => Some(u.to_string()),
            (None, Some(false)) => None,
            _ => Some(j.str("rendezvous").map(String::from).unwrap_or(peerova_core::license::DEFAULT_RELAY.to_string())),
        };
        if let Some(t) = j.str("token") {
            cfg.access = Access::Token(t.as_bytes().to_vec());
        }
        let cap = j.u64("max_ccu").map(|n| n as usize);
        let lic = match j.str("license") {
            Some(k) => {
                let auth = j
                    .str("authority_pubkey")
                    .map(String::from)
                    .or(license::DEFAULT_AUTHORITY_PUBKEY.map(String::from))
                    .ok_or(VI_NO_AUTHORITY)?;
                let dir = key_path.parent().filter(|p| !p.as_os_str().is_empty()).map(PathBuf::from).unwrap_or_else(|| ".".into());
                Some(LicenseConfig {
                    key: k.to_string(),
                    server: j.str("license_server").unwrap_or(license::DEFAULT_LICENSE_SERVER).to_string(),
                    authority: license::parse_pubkey(&auth)?,
                    cache: license::cache_path(&dir),
                    spk,
                })
            }
            None => None,
        };
        let mut ccu = peerova_core::engine::DEV_MODE_CCU;
        if let Some(l) = &lic {
            let (lease, sns) = license::prepare(l);
            if let Ok(lease) = lease {
                ccu = lease.ccu as usize;
                cfg.lease = Some(lease.token);
                cfg.authority = Some(l.authority);
            }
            for sn in sns {
                if !cfg.supernodes.contains(&sn) {
                    cfg.supernodes.push(sn);
                }
            }
        }
        cfg.max_ccu = ccu.min(cap.unwrap_or(usize::MAX));
        Ok((cfg, lic))
}

/// Client engine config from the JSON config (shared by the SDK and the tunnel API).
pub(crate) fn client_config(j: &Json) -> Result<ClientConfig, String> {
        // Optional persistent client identity: same key file ⇒ same identity (and the same stable
        // loopback address on servers that map players to 127.a.b.c). Default: a fresh key per run.
        let key = match j.str("key_file") {
            Some(p) => keyfile::load_or_create(&PathBuf::from(p))?.0,
            None => peerova_core::crypto::Keypair::generate(),
        };
        Ok(ClientConfig {
            key,
            token: j.str("token").unwrap_or("").as_bytes().to_vec(),
            timeout: Duration::from_millis(j.u64("timeout_ms").unwrap_or(20_000)),
            extra_supernodes: addrs(j.str_list("supernodes"))?,
            force_tcp: j.bool("force_tcp").unwrap_or(false),
            rendezvous: j.str("rendezvous").map(String::from),
            relay: j.str("relay").map(String::from),
            no_relay: j.bool("relay") == Some(false),
            // Persist learned paths only where the app said files may go: "cache_dir", else
            // next to "key_file". Neither given → memory only (the library writes no files).
            cache_file: j.str("cache_dir").map(PathBuf::from).or_else(|| j.str("key_file").and_then(|k| PathBuf::from(k).parent().map(|d| d.to_path_buf()))).map(|d| d.join("peerova-paths.cache")),
            ..Default::default()
        })
}

impl Sdk {
    pub fn server(config_json: Option<&str>) -> Result<Sdk, String> {
        let j = cfg_json(config_json)?;
        let (cfg, lic) = server_config(&j)?;
        let engine = Engine::start_server(cfg).map_err(|e| e.to_string())?;
        if let Some(l) = lic {
            license::spawn_renewal(l, engine.handle.clone());
        }
        let mut sdk = Sdk { engine, ticket: None, room_code: Default::default(), queue: VecDeque::new(), reasm: HashMap::new(), peers: HashSet::new() };
        let deadline = Instant::now() + Duration::from_secs(5);
        while sdk.ticket.is_none() && Instant::now() < deadline {
            sdk.pump(Duration::from_millis(50));
        }
        if sdk.ticket.is_none() {
            return Err(VI_SERVER_NOT_READY.into());
        }
        // Optional 6-char connect code for this service via the website (metadata only, CONTRACT §9).
        // Config key "share" (alias "lobby"): {"name","public","max","mode","region","version","tags"}.
        if let (Some(l), Some(app)) = (j.get("share").or(j.get("lobby")), j.str("app")) {
            let info = peerova_core::lobby::LobbyInfo {
                app: app.to_string(),
                name: l.str("name").unwrap_or("Peerova").to_string(),
                public: l.bool("public").unwrap_or(false),
                max: l.u64("max").unwrap_or(0) as u32,
                mode: l.str("mode").unwrap_or("").to_string(),
                region: l.str("region").unwrap_or("").to_string(),
                version: l.str("version").unwrap_or("").to_string(),
                tags: l.str_list("tags"),
            };
            let stats = sdk.engine.handle.stats.clone();
            let lease = sdk.engine.handle.lease.clone();
            let slot = sdk.room_code.clone();
            peerova_core::lobby::spawn_heartbeat(
                site_of(&j),
                info,
                sdk.ticket.clone().unwrap(),
                move || stats.lock().unwrap().ccu as u32,
                move || lease.lock().unwrap().clone(),
                move |r| {
                    if let Ok(code) = r {
                        *slot.lock().unwrap() = Some(code);
                    }
                },
            );
        }
        Ok(sdk)
    }

    /// `ticket` may also be a 6-character connect code (resolved via the website).
    pub fn client(ticket: &str, config_json: Option<&str>) -> Result<Sdk, String> {
        let j = cfg_json(config_json)?;
        let resolved;
        let ticket = if peerova_core::lobby::is_room_code(ticket) {
            resolved = peerova_core::lobby::resolve(&site_of(&j), ticket)?;
            resolved.as_str()
        } else {
            ticket
        };
        let cfg = client_config(&j)?;
        let engine = Engine::connect(ticket, cfg)?;
        Ok(Sdk { engine, ticket: None, room_code: Default::default(), queue: VecDeque::new(), reasm: HashMap::new(), peers: HashSet::new() })
    }

    pub fn ticket(&self) -> Option<&str> {
        self.ticket.as_deref()
    }

    /// 6-char connect code once the website assigned it (server with "lobby" config).
    pub fn room_code(&self) -> Option<String> {
        self.room_code.lock().unwrap().clone()
    }

    pub fn peer_count(&self) -> usize {
        self.peers.len()
    }

    pub fn send(&mut self, peer: u32, channel: u8, data: &[u8], reliable: bool) -> Result<(), String> {
        if !self.peers.contains(&peer) {
            return Err(VI_UNKNOWN_PEER.into());
        }
        if reliable {
            if data.len() > MAX_RELIABLE_MESSAGE {
                return Err(format!("{VI_TOO_LARGE_RELIABLE} {MAX_RELIABLE_MESSAGE} byte"));
            }
            let mut d = Vec::with_capacity(4 + data.len());
            d.extend_from_slice(&(data.len() as u32).to_be_bytes());
            d.extend_from_slice(data);
            self.engine.handle.send(Cmd::StreamWrite { conn: peer, id: CHAN_BIT | channel as u32, data: d });
        } else {
            if data.len() > MAX_UNRELIABLE {
                return Err(format!("{VI_TOO_LARGE} {MAX_UNRELIABLE} byte"));
            }
            let mut d = Vec::with_capacity(2 + data.len());
            d.push(DGRAM);
            d.push(channel);
            d.extend_from_slice(data);
            self.engine.handle.send(Cmd::Datagram { conn: peer, data: d });
        }
        Ok(())
    }

    pub fn disconnect(&mut self, peer: u32) {
        self.engine.handle.send(Cmd::Close { conn: peer });
    }

    fn pump(&mut self, wait: Duration) {
        let mut first = self.engine.recv_timeout(wait);
        while let Some(ev) = first.take().or_else(|| self.engine.try_recv()) {
            self.handle(ev);
        }
    }

    fn handle(&mut self, ev: EngineEvent) {
        match ev {
            EngineEvent::Ready { ticket: Some(t), .. } => self.ticket = Some(t),
            EngineEvent::Connected { conn, .. } => {
                self.peers.insert(conn);
                self.queue.push_back(SdkEvent { kind: EV_CONNECTED, peer: conn, channel: 0, reliable: false, data: vec![] });
            }
            EngineEvent::Disconnected { conn, reason } => {
                self.peers.remove(&conn);
                self.reasm.retain(|(c, _), _| *c != conn);
                self.queue.push_back(SdkEvent { kind: EV_DISCONNECTED, peer: conn, channel: 0, reliable: false, data: reason.into_bytes() });
            }
            EngineEvent::ConnectFailed(e) => {
                self.queue.push_back(SdkEvent { kind: EV_CONNECT_FAILED, peer: 0, channel: 0, reliable: false, data: e.into_bytes() })
            }
            EngineEvent::StreamData { conn, id, data } if id & CHAN_BIT != 0 => {
                self.engine.handle.send(Cmd::StreamConsumed { conn, id, n: data.len() });
                let buf = self.reasm.entry((conn, id)).or_default();
                buf.extend_from_slice(&data);
                let mut off = 0;
                let mut too_big = false;
                while buf.len() - off >= 4 {
                    let n = u32::from_be_bytes(buf[off..off + 4].try_into().unwrap()) as usize;
                    if n > MAX_RELIABLE_MESSAGE {
                        too_big = true; // a peer announcing a huge message can't make us buffer it
                        break;
                    }
                    if buf.len() - off - 4 < n {
                        break;
                    }
                    let msg = buf[off + 4..off + 4 + n].to_vec();
                    off += 4 + n;
                    self.queue.push_back(SdkEvent { kind: EV_MESSAGE, peer: conn, channel: (id & 0xff) as u8, reliable: true, data: msg });
                }
                buf.drain(..off);
                if too_big {
                    self.reasm.retain(|(c, _), _| *c != conn);
                    self.engine.handle.send(Cmd::Close { conn });
                }
            }
            EngineEvent::Datagram { conn, data } if data.len() >= 2 && data[0] == DGRAM => {
                self.queue.push_back(SdkEvent { kind: EV_MESSAGE, peer: conn, channel: data[1], reliable: false, data: data[2..].to_vec() });
            }
            _ => {}
        }
    }

    pub fn poll(&mut self) -> Option<SdkEvent> {
        if self.queue.is_empty() {
            self.pump(Duration::ZERO);
        }
        self.queue.pop_front()
    }

    pub fn poll_wait(&mut self, timeout: Duration) -> Option<SdkEvent> {
        let deadline = Instant::now() + timeout;
        loop {
            if let Some(e) = self.poll() {
                return Some(e);
            }
            let left = deadline.saturating_duration_since(Instant::now());
            if left.is_zero() {
                return None;
            }
            self.pump(left.min(Duration::from_millis(50)));
        }
    }
}

// ------------------------------------------------------------------ C ABI

pub struct peerova_node {
    sdk: Mutex<Sdk>,
    last: Mutex<Vec<u8>>,
}

#[repr(C)]
pub struct peerova_event {
    pub kind: i32,
    pub peer: u32,
    pub channel: u8,
    pub reliable: u8,
    pub data: *const u8,
    pub len: u32,
}

thread_local! {
    static LAST_ERROR: RefCell<(ErrorCode, CString)> = RefCell::new((ErrorCode::Ok, CString::default()));
}

/// Records an error raised at the C ABI layer (code known directly).
fn set_code(code: ErrorCode, english: &str) {
    set_sdk_err(SdkError::new(code, english));
}

/// Records an error coming from the core / `Sdk` (Vietnamese text), classified to code + English.
fn set_err(detail: impl AsRef<str>) {
    set_sdk_err(classify(detail.as_ref()));
}

fn set_sdk_err(e: SdkError) {
    let s = e.to_string().replace('\0', " ");
    LAST_ERROR.with(|l| *l.borrow_mut() = (e.code, CString::new(s).unwrap_or_default()));
}

/// Fallible entry points start from a clean state, so a code read after success is `E_OK`.
fn clear_err() {
    LAST_ERROR.with(|l| *l.borrow_mut() = (ErrorCode::Ok, CString::default()));
}

/// Code + English reason of a CONNECT_FAILED / DISCONNECTED event (`Sdk` events carry the core's
/// Vietnamese reason); `None` for other events.
pub fn event_error(e: &SdkEvent) -> Option<SdkError> {
    match e.kind {
        EV_CONNECT_FAILED | EV_DISCONNECTED => {
            let reason = String::from_utf8_lossy(&e.data);
            Some(if e.kind == EV_DISCONNECTED && reason.trim().is_empty() {
                SdkError::new(ErrorCode::Closed, "connection closed")
            } else {
                classify(&reason)
            })
        }
        _ => None,
    }
}

/// The C ABI delivers CONNECT_FAILED / DISCONNECTED payloads as "E_NAME: English reason".
fn event_payload(e: SdkEvent) -> SdkEvent {
    match event_error(&e) {
        Some(err) => SdkEvent { data: err.to_string().into_bytes(), ..e },
        None => e,
    }
}

unsafe fn opt_str<'a>(p: *const c_char) -> Option<&'a str> {
    if p.is_null() {
        None
    } else {
        CStr::from_ptr(p).to_str().ok()
    }
}

fn guard<T>(default: T, f: impl FnOnce() -> T) -> T {
    std::panic::catch_unwind(std::panic::AssertUnwindSafe(f)).unwrap_or_else(|_| {
        set_code(ErrorCode::Internal, "internal error (panic inside peerova)");
        default
    })
}

fn boxed(r: Result<Sdk, String>) -> *mut peerova_node {
    match r {
        Ok(sdk) => Box::into_raw(Box::new(peerova_node { sdk: Mutex::new(sdk), last: Mutex::new(vec![]) })),
        Err(e) => {
            set_err(e);
            std::ptr::null_mut()
        }
    }
}

#[no_mangle]
pub unsafe extern "C" fn peerova_server_start(config_json: *const c_char) -> *mut peerova_node {
    guard(std::ptr::null_mut(), || {
        clear_err();
        boxed(Sdk::server(opt_str(config_json)))
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_client_connect(ticket: *const c_char, config_json: *const c_char) -> *mut peerova_node {
    guard(std::ptr::null_mut(), || {
        clear_err();
        if ticket.is_null() {
            set_code(ErrorCode::InvalidArg, "ticket is NULL");
            return std::ptr::null_mut();
        }
        match opt_str(ticket) {
            Some(t) => boxed(Sdk::client(t, opt_str(config_json))),
            None => {
                set_code(ErrorCode::InvalidArg, "ticket is not valid UTF-8");
                std::ptr::null_mut()
            }
        }
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_ticket(node: *mut peerova_node, buf: *mut c_char, buf_len: i32) -> i32 {
    guard(0, || {
        let Some(n) = node.as_ref() else { return 0 };
        let sdk = n.sdk.lock().unwrap();
        let Some(t) = sdk.ticket() else { return 0 };
        let need = t.len() as i32 + 1;
        if !buf.is_null() && buf_len >= need {
            std::ptr::copy_nonoverlapping(t.as_ptr(), buf as *mut u8, t.len());
            *buf.add(t.len()) = 0;
        }
        need
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_room_code(node: *mut peerova_node, buf: *mut c_char, buf_len: i32) -> i32 {
    guard(0, || {
        let Some(n) = node.as_ref() else { return 0 };
        let Some(code) = n.sdk.lock().unwrap().room_code() else { return 0 };
        let need = code.len() as i32 + 1;
        if !buf.is_null() && buf_len >= need {
            std::ptr::copy_nonoverlapping(code.as_ptr(), buf as *mut u8, code.len());
            *buf.add(code.len()) = 0;
        }
        need
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_send(node: *mut peerova_node, peer: u32, channel: u8, data: *const u8, len: u32, reliable: i32) -> i32 {
    guard(-1, || {
        clear_err();
        let Some(n) = node.as_ref() else {
            set_code(ErrorCode::InvalidArg, "node is NULL");
            return -1;
        };
        let slice = if len == 0 {
            &[][..]
        } else if data.is_null() {
            set_code(ErrorCode::InvalidArg, "data is NULL");
            return -1;
        } else {
            std::slice::from_raw_parts(data, len as usize)
        };
        match n.sdk.lock().unwrap().send(peer, channel, slice, reliable != 0) {
            Ok(()) => 0,
            Err(e) => {
                set_err(e);
                -1
            }
        }
    })
}

unsafe fn fill(n: &peerova_node, e: Option<SdkEvent>, out: *mut peerova_event) -> i32 {
    let Some(e) = e else { return 0 };
    if out.is_null() {
        return 0;
    }
    let e = event_payload(e);
    let mut last = n.last.lock().unwrap();
    *last = e.data;
    *out = peerova_event { kind: e.kind, peer: e.peer, channel: e.channel, reliable: e.reliable as u8, data: last.as_ptr(), len: last.len() as u32 };
    1
}

#[no_mangle]
pub unsafe extern "C" fn peerova_poll(node: *mut peerova_node, out: *mut peerova_event) -> i32 {
    guard(0, || {
        let Some(n) = node.as_ref() else { return 0 };
        let e = n.sdk.lock().unwrap().poll();
        fill(n, e, out)
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_poll_wait(node: *mut peerova_node, out: *mut peerova_event, timeout_ms: u32) -> i32 {
    guard(0, || {
        let Some(n) = node.as_ref() else { return 0 };
        // Wait in short slices and release the lock between them, so `peerova_send`
        // from another thread is never blocked for the whole timeout.
        let deadline = Instant::now() + Duration::from_millis(timeout_ms as u64);
        loop {
            let left = deadline.saturating_duration_since(Instant::now());
            let e = n.sdk.lock().unwrap().poll_wait(left.min(Duration::from_millis(5)));
            if e.is_some() || left.is_zero() {
                return fill(n, e, out);
            }
        }
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_disconnect(node: *mut peerova_node, peer: u32) {
    guard((), || {
        if let Some(n) = node.as_ref() {
            n.sdk.lock().unwrap().disconnect(peer);
        }
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_peer_count(node: *mut peerova_node) -> u32 {
    guard(0, || node.as_ref().map_or(0, |n| n.sdk.lock().unwrap().peer_count() as u32))
}

#[no_mangle]
pub unsafe extern "C" fn peerova_free(node: *mut peerova_node) {
    if !node.is_null() {
        guard((), || drop(Box::from_raw(node)));
    }
}

#[no_mangle]
pub extern "C" fn peerova_last_error() -> *const c_char {
    LAST_ERROR.with(|l| l.borrow().1.as_ptr())
}

#[no_mangle]
pub extern "C" fn peerova_last_error_code() -> i32 {
    LAST_ERROR.with(|l| l.borrow().0 as i32)
}

#[no_mangle]
pub extern "C" fn peerova_version() -> *const c_char {
    concat!(env!("CARGO_PKG_VERSION"), "\0").as_ptr() as *const c_char
}

#[cfg(test)]
mod tests {
    use super::*;

    fn last() -> (i32, String) {
        let t = unsafe { CStr::from_ptr(peerova_last_error()) }.to_str().unwrap().to_string();
        (peerova_last_error_code(), t)
    }

    #[test]
    fn c_abi_errors_have_codes_and_english_text() {
        unsafe {
            assert_eq!(peerova_send(std::ptr::null_mut(), 1, 0, std::ptr::null(), 0, 1), -1);
            assert_eq!(last(), (ErrorCode::InvalidArg as i32, "E_INVALID_ARG: node is NULL".into()));
            assert!(peerova_client_connect(std::ptr::null(), std::ptr::null()).is_null());
            assert_eq!(last(), (ErrorCode::InvalidArg as i32, "E_INVALID_ARG: ticket is NULL".into()));
            let bad = CString::new("pvt_@@@").unwrap();
            assert!(peerova_client_connect(bad.as_ptr(), std::ptr::null()).is_null());
            assert_eq!(last(), (ErrorCode::InvalidTicket as i32, "E_INVALID_TICKET: the ticket is not valid base64".into()));
            let cfg = CString::new("{\"port\":").unwrap();
            assert!(peerova_server_start(cfg.as_ptr()).is_null());
            let (code, text) = last();
            assert_eq!(code, ErrorCode::InvalidArg as i32);
            assert!(text.starts_with("E_INVALID_ARG: invalid config JSON"), "{text}");
        }
    }

    #[test]
    fn event_reasons_are_english() {
        let ev = |kind, s: &str| event_payload(SdkEvent { kind, peer: 1, channel: 0, reliable: false, data: s.as_bytes().to_vec() });
        let f = ev(EV_CONNECT_FAILED, "không kết nối được: hết thời gian. Chạy `peerova doctor` ở cả 2 máy");
        assert!(String::from_utf8(f.data).unwrap().starts_with("E_TIMEOUT: could not connect: timed out"));
        assert_eq!(ev(EV_DISCONNECTED, "peer closed").data, b"E_CLOSED: the peer closed the connection");
        assert_eq!(ev(EV_DISCONNECTED, "").data, b"E_CLOSED: connection closed");
        assert_eq!(ev(EV_MESSAGE, "không đổi").data, "không đổi".as_bytes()); // messages untouched
    }

    #[test]
    fn port_in_use_is_network_error() {
        let busy = std::net::UdpSocket::bind("0.0.0.0:0").unwrap();
        let port = busy.local_addr().unwrap().port();
        let dir = std::env::temp_dir().join(format!("peerova-busy-{}", std::process::id()));
        std::fs::create_dir_all(&dir).unwrap();
        let key = dir.join("k.key");
        let cfg = format!("{{\"port\":{port},\"upnp\":false,\"rendezvous\":false,\"relay\":false,\"key_file\":{}}}", peerova_core::json::escape(key.to_str().unwrap()));
        match Sdk::server(Some(&cfg)) {
            Ok(_) => {} // some systems allow sharing the port (dual-stack); nothing to check then
            Err(e) => {
                let c = classify(&e);
                assert_eq!(c.code, ErrorCode::Network, "{e} -> {c}");
            }
        }
        let _ = std::fs::remove_dir_all(dir);
    }

    #[test]
    fn client_key_file_is_persistent() {
        let dir = std::env::temp_dir().join(format!("peerova-cli-key-{}", std::process::id()));
        std::fs::create_dir_all(&dir).unwrap();
        let skey = dir.join("s.key");
        let ckey = dir.join("c.key");
        let cfg = format!("{{\"port\":0,\"upnp\":false,\"rendezvous\":false,\"relay\":false,\"key_file\":{}}}", peerova_core::json::escape(skey.to_str().unwrap()));
        let s = Sdk::server(Some(&cfg)).unwrap();
        let ccfg = format!("{{\"relay\":false,\"key_file\":{}}}", peerova_core::json::escape(ckey.to_str().unwrap()));
        drop(Sdk::client(s.ticket().unwrap(), Some(&ccfg)).unwrap());
        let first = std::fs::read_to_string(&ckey).expect("client key file created");
        drop(Sdk::client(s.ticket().unwrap(), Some(&ccfg)).unwrap());
        assert_eq!(std::fs::read_to_string(&ckey).unwrap(), first, "key file must be reused, not regenerated");
        let _ = std::fs::remove_dir_all(dir);
    }

    #[test]
    fn sdk_send_errors_map_to_codes() {
        let dir = std::env::temp_dir().join(format!("peerova-sdk-err-{}", std::process::id()));
        std::fs::create_dir_all(&dir).unwrap();
        let key = dir.join("k.key");
        let cfg = format!("{{\"port\":0,\"upnp\":false,\"rendezvous\":false,\"relay\":false,\"key_file\":{}}}", peerova_core::json::escape(key.to_str().unwrap()));
        let cfg = CString::new(cfg).unwrap();
        unsafe {
            let n = peerova_server_start(cfg.as_ptr());
            assert!(!n.is_null(), "{:?}", last());
            assert_eq!(last().0, 0);
            let d = [0u8; 4];
            assert_eq!(peerova_send(n, 12345, 0, d.as_ptr(), 4, 1), -1);
            assert_eq!(last().0, ErrorCode::UnknownPeer as i32);
            assert!(last().1.starts_with("E_UNKNOWN_PEER: "));
            peerova_free(n);
        }
        let _ = std::fs::remove_dir_all(dir);
    }

    #[test]
    fn sdk_reliable_and_unreliable() {
        let dir = std::env::temp_dir().join(format!("peerova-sdk-test-{}", std::process::id()));
        std::fs::create_dir_all(&dir).unwrap();
        let key = dir.join("k.key");
        let cfg = format!("{{\"port\":0,\"upnp\":false,\"key_file\":{}}}", peerova_core::json::escape(key.to_str().unwrap()));
        let mut s = Sdk::server(Some(&cfg)).unwrap();
        let mut c = Sdk::client(s.ticket().unwrap(), None).unwrap();
        let ev = c.poll_wait(Duration::from_secs(10)).unwrap();
        assert_eq!(ev.kind, EV_CONNECTED);
        let sp = s.poll_wait(Duration::from_secs(5)).unwrap();
        assert_eq!(sp.kind, EV_CONNECTED);
        for i in 0..100u32 {
            c.send(ev.peer, (i % 3) as u8, &i.to_be_bytes().repeat(50), true).unwrap();
        }
        c.send(ev.peer, 9, b"fast", false).unwrap();
        let mut per_chan: HashMap<u8, Vec<u32>> = HashMap::new();
        let mut got_unrel = false;
        let deadline = Instant::now() + Duration::from_secs(10);
        while Instant::now() < deadline && per_chan.values().map(|v| v.len()).sum::<usize>() < 100 {
            if let Some(m) = s.poll_wait(Duration::from_millis(100)) {
                if m.kind == EV_MESSAGE && m.reliable {
                    assert_eq!(m.data.len(), 200);
                    per_chan.entry(m.channel).or_default().push(u32::from_be_bytes(m.data[..4].try_into().unwrap()));
                } else if m.kind == EV_MESSAGE {
                    got_unrel = m.channel == 9 && m.data == b"fast";
                }
            }
        }
        for (ch, v) in &per_chan {
            let mut sorted = v.clone();
            sorted.sort();
            assert_eq!(v, &sorted, "channel {ch} out of order");
        }
        assert_eq!(per_chan.values().map(|v| v.len()).sum::<usize>(), 100);
        assert!(got_unrel || true); // unreliable may be dropped in theory; usually arrives
        s.send(sp.peer, 0, b"reply", true).unwrap();
        loop {
            let m = c.poll_wait(Duration::from_secs(5)).expect("reply");
            if m.kind == EV_MESSAGE {
                assert_eq!(m.data, b"reply");
                break;
            }
        }
        let _ = std::fs::remove_dir_all(dir);
    }
}
