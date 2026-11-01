//! Port-tunnel C ABI (`peerova_tunnel_*`): the CLI's `server --expose` / `client --forward`
//! running in-process, so an app with an existing TCP protocol (e.g. a game socket) gets P2P
//! without rewriting its network code and without a sidecar process.
//!
//! * Server: exposes local services (`"expose":["tcp:19129"]`); every forwarded TCP connection is
//!   a plain loopback connection to the service, from the player's own stable 127.a.b.c address
//!   (Peerova `tunnel::player_ip`), so per-IP limits / bans / logs in the app work per player.
//! * Client: listens on `127.0.0.1:<local>` (`"forward":["tcp:0=19129"]`, 0 = free port) and
//!   tunnels each accepted connection as an independent reliable stream.

use std::collections::{HashMap, VecDeque};
use std::ffi::c_char;
use std::net::{IpAddr, Ipv4Addr, SocketAddr, TcpListener};
use std::sync::{Arc, Condvar, Mutex};
use std::thread::{self, JoinHandle};
use std::time::{Duration, Instant};

use peerova_core::json::Json;
use peerova_core::tunnel::{self, Rule};
use peerova_core::{Cmd, Engine, EngineEvent, Handle};

use super::{cfg_json, client_config, guard, opt_str, server_config, set_err, site_of};

pub const TUNNEL_CONNECTING: i32 = 0;
pub const TUNNEL_UP: i32 = 1;
pub const TUNNEL_DOWN: i32 = 2;

const MAX_LOGS: usize = 256;

#[derive(Default)]
struct St {
    state: i32,
    ticket: Option<String>,
    error: String,
    logs: VecDeque<String>,
    /// conn id -> remote address of the peer.
    peers: HashMap<u32, SocketAddr>,
    /// client: (rule index -> local port), listeners still pending.
    local_ports: Vec<u16>,
    pending_listeners: usize,
}

struct Shared {
    st: Mutex<St>,
    cv: Condvar,
}

impl Shared {
    fn log(&self, m: String) {
        let mut g = self.st.lock().unwrap();
        if g.logs.len() >= MAX_LOGS {
            g.logs.pop_front();
        }
        g.logs.push_back(m);
    }
    fn set_state(&self, s: i32, err: Option<String>) {
        let mut g = self.st.lock().unwrap();
        // DOWN is final for this tunnel object.
        if g.state != TUNNEL_DOWN {
            g.state = s;
        }
        if let Some(e) = err {
            if g.error.is_empty() || s == TUNNEL_DOWN {
                g.error = e;
            }
        }
        self.cv.notify_all();
    }
}

/// In-process port tunnel (server expose or client forward). Rust API; C ABI below.
pub struct Tunnel {
    shared: Arc<Shared>,
    handle: Handle,
    thread: Option<JoinHandle<()>>,
}

fn rules_expose(j: &Json) -> Result<Vec<Rule>, String> {
    let mut v = vec![];
    for e in j.str_list("expose") {
        v.extend(tunnel::parse_expose_list(&e)?);
    }
    if v.is_empty() {
        return Err("tunnel server cần \"expose\":[\"tcp:PORT\"]".into());
    }
    Ok(v)
}

fn rules_forward(j: &Json) -> Result<Vec<Rule>, String> {
    let bind: IpAddr = match j.str("bind") {
        Some(b) => b.parse().map_err(|_| format!("bind không hợp lệ: {b}"))?,
        None => IpAddr::V4(Ipv4Addr::LOCALHOST),
    };
    let mut v = vec![];
    for f in j.str_list("forward") {
        for mut r in tunnel::parse_forward_list(&f, bind)? {
            if r.target.port() == 0 {
                // Pick a free local port now so the app can learn it before the tunnel is up.
                let l = TcpListener::bind(SocketAddr::new(bind, 0))
                    .map_err(|e| format!("không chọn được port local: {e}"))?;
                r.target = l.local_addr().map_err(|e| e.to_string())?;
            }
            v.push(r);
        }
    }
    if v.is_empty() {
        return Err("tunnel client cần \"forward\":[\"tcp:LOCAL=REMOTE\"]".into());
    }
    Ok(v)
}

impl Tunnel {
    /// Starts the server and blocks until the ticket is ready (≤ ~5 s).
    pub fn server(config_json: Option<&str>) -> Result<Tunnel, String> {
        let j = cfg_json(config_json)?;
        let rules = rules_expose(&j)?;
        let (cfg, lic) = server_config(&j)?;
        let max_ccu = cfg.max_ccu;
        let engine = Engine::start_server(cfg).map_err(|e| e.to_string())?;
        if let Some(l) = lic {
            peerova_core::license::spawn_renewal(l, engine.handle.clone());
        }
        let shared = Arc::new(Shared {
            st: Mutex::new(St::default()),
            cv: Condvar::new(),
        });
        shared.log(format!("tunnel server: tối đa {max_ccu} CCU"));
        for r in &rules {
            shared.log(format!(
                "expose {:?} port {} → {}",
                r.proto, r.port, r.target
            ));
        }
        let handle = engine.handle.clone();
        let (sh, sh3) = (shared.clone(), shared.clone());
        let thread = thread::Builder::new()
            .name("peerova-tunnel-srv".into())
            .spawn(move || {
                tunnel::serve_audited(
                    &engine,
                    rules,
                    |e| match e {
                        EngineEvent::Ready {
                            ticket: Some(t),
                            candidates,
                            ..
                        } => {
                            for c in candidates {
                                sh.log(format!("candidate {} {}", c.kind.label(), c.addr));
                            }
                            sh.st.lock().unwrap().ticket = Some(t.clone());
                            sh.set_state(TUNNEL_UP, None);
                        }
                        EngineEvent::Connected {
                            conn, addr, path, ..
                        } => {
                            sh.st.lock().unwrap().peers.insert(*conn, *addr);
                            sh.log(format!("client {conn:08x} kết nối từ {addr} [{path}]"));
                        }
                        EngineEvent::Disconnected { conn, reason } => {
                            {
                                let mut g = sh.st.lock().unwrap();
                                g.peers.remove(conn);
                            }
                            sh.log(format!("client {conn:08x} ngắt: {reason}"));
                        }
                        EngineEvent::Log(m) => sh.log(m.clone()),
                        _ => {}
                    },
                    |_| {},
                );
                sh3.set_state(TUNNEL_DOWN, Some("tunnel server đã dừng".into()));
            })
            .map_err(|e| e.to_string())?;
        let t = Tunnel {
            shared,
            handle,
            thread: Some(thread),
        };
        if t.wait(Duration::from_secs(5)) != TUNNEL_UP || t.ticket().is_none() {
            return Err("server không sẵn sàng (timeout chờ ticket)".into());
        }
        Ok(t)
    }

    /// Connects in the background; local ports are known immediately (`local_port`), and become
    /// usable when `state()` is `TUNNEL_UP`.
    pub fn client(ticket: &str, config_json: Option<&str>) -> Result<Tunnel, String> {
        let j = cfg_json(config_json)?;
        let rules = rules_forward(&j)?;
        let resolved;
        let ticket = if peerova_core::lobby::is_room_code(ticket) {
            resolved = peerova_core::lobby::resolve(&site_of(&j), ticket)?;
            resolved.as_str()
        } else {
            ticket
        };
        let engine = Engine::connect(ticket, client_config(&j)?)?;
        let shared = Arc::new(Shared {
            st: Mutex::new(St {
                local_ports: rules.iter().map(|r| r.target.port()).collect(),
                pending_listeners: rules.len(),
                ..Default::default()
            }),
            cv: Condvar::new(),
        });
        let handle = engine.handle.clone();
        let sh = shared.clone();
        let thread = thread::Builder::new()
            .name("peerova-tunnel-cli".into())
            .spawn(move || {
                let r = tunnel::forward_with(
                    &engine,
                    rules,
                    |e| match e {
                        EngineEvent::Connected { addr, path, .. } => {
                            sh.log(format!("đã kết nối P2P tới {addr} [{path}]"))
                        }
                        EngineEvent::ConnectFailed(e) => {
                            sh.log(format!("kết nối P2P thất bại: {e}"));
                            sh.set_state(TUNNEL_DOWN, Some(e.clone()));
                        }
                        EngineEvent::Disconnected { reason, .. } => {
                            sh.log(format!("mất kết nối P2P: {reason}"));
                            sh.set_state(TUNNEL_DOWN, Some(format!("mất kết nối: {reason}")));
                        }
                        EngineEvent::Log(m) => sh.log(m.clone()),
                        _ => {}
                    },
                    |rule, res| match res {
                        Ok(()) => {
                            let left = {
                                let mut g = sh.st.lock().unwrap();
                                g.pending_listeners = g.pending_listeners.saturating_sub(1);
                                g.pending_listeners
                            };
                            if left == 0 {
                                sh.set_state(TUNNEL_UP, None);
                            }
                        }
                        Err(e) => sh.set_state(
                            TUNNEL_DOWN,
                            Some(format!("không mở được {}: {e}", rule.target)),
                        ),
                    },
                );
                sh.set_state(
                    TUNNEL_DOWN,
                    Some(r.err().unwrap_or_else(|| "tunnel đã dừng".into())),
                );
            })
            .map_err(|e| e.to_string())?;
        Ok(Tunnel {
            shared,
            handle,
            thread: Some(thread),
        })
    }

    pub fn state(&self) -> i32 {
        self.shared.st.lock().unwrap().state
    }

    /// Waits while CONNECTING (up to `timeout`); returns the state.
    pub fn wait(&self, timeout: Duration) -> i32 {
        let deadline = Instant::now() + timeout;
        let mut g = self.shared.st.lock().unwrap();
        while g.state == TUNNEL_CONNECTING {
            let left = deadline.saturating_duration_since(Instant::now());
            if left.is_zero() {
                break;
            }
            g = self.shared.cv.wait_timeout(g, left).unwrap().0;
        }
        g.state
    }

    pub fn ticket(&self) -> Option<String> {
        self.shared.st.lock().unwrap().ticket.clone()
    }

    pub fn error(&self) -> String {
        self.shared.st.lock().unwrap().error.clone()
    }

    pub fn local_port(&self, index: usize) -> u16 {
        self.shared
            .st
            .lock()
            .unwrap()
            .local_ports
            .get(index)
            .copied()
            .unwrap_or(0)
    }

    pub fn peer_count(&self) -> usize {
        self.shared.st.lock().unwrap().peers.len()
    }

    pub fn next_log(&self) -> Option<String> {
        self.shared.st.lock().unwrap().logs.pop_front()
    }

}

impl Drop for Tunnel {
    fn drop(&mut self) {
        self.handle.send(Cmd::Shutdown);
        if let Some(t) = self.thread.take() {
            let _ = t.join();
        }
    }
}

// ------------------------------------------------------------------ C ABI

pub struct peerova_tunnel {
    t: Tunnel,
}

fn boxed(r: Result<Tunnel, String>) -> *mut peerova_tunnel {
    match r {
        Ok(t) => Box::into_raw(Box::new(peerova_tunnel { t })),
        Err(e) => {
            set_err(e);
            std::ptr::null_mut()
        }
    }
}

unsafe fn copy_out(s: &str, buf: *mut c_char, buf_len: i32) -> i32 {
    let s = s.replace('\0', " ");
    let need = s.len() as i32 + 1;
    if !buf.is_null() && buf_len >= need {
        std::ptr::copy_nonoverlapping(s.as_ptr(), buf as *mut u8, s.len());
        *buf.add(s.len()) = 0;
    }
    need
}

#[no_mangle]
pub unsafe extern "C" fn peerova_tunnel_server(config_json: *const c_char) -> *mut peerova_tunnel {
    guard(std::ptr::null_mut(), || {
        boxed(Tunnel::server(opt_str(config_json)))
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_tunnel_client(
    ticket: *const c_char,
    config_json: *const c_char,
) -> *mut peerova_tunnel {
    guard(std::ptr::null_mut(), || {
        match opt_str(ticket).map(str::trim).filter(|t| !t.is_empty()) {
            Some(t) => boxed(Tunnel::client(t, opt_str(config_json))),
            None => {
                set_err("ticket rỗng");
                std::ptr::null_mut()
            }
        }
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_tunnel_state(t: *mut peerova_tunnel) -> i32 {
    guard(TUNNEL_DOWN, || {
        t.as_ref().map_or(TUNNEL_DOWN, |t| t.t.state())
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_tunnel_wait(t: *mut peerova_tunnel, timeout_ms: u32) -> i32 {
    guard(TUNNEL_DOWN, || {
        t.as_ref().map_or(TUNNEL_DOWN, |t| {
            t.t.wait(Duration::from_millis(timeout_ms as u64))
        })
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_tunnel_local_port(t: *mut peerova_tunnel, index: i32) -> i32 {
    guard(0, || {
        t.as_ref().map_or(0, |t| {
            if index < 0 {
                0
            } else {
                t.t.local_port(index as usize) as i32
            }
        })
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_tunnel_ticket(
    t: *mut peerova_tunnel,
    buf: *mut c_char,
    buf_len: i32,
) -> i32 {
    guard(0, || match t.as_ref().and_then(|t| t.t.ticket()) {
        Some(s) => copy_out(&s, buf, buf_len),
        None => 0,
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_tunnel_error(
    t: *mut peerova_tunnel,
    buf: *mut c_char,
    buf_len: i32,
) -> i32 {
    guard(0, || {
        match t.as_ref().map(|t| t.t.error()).filter(|e| !e.is_empty()) {
            Some(s) => copy_out(&s, buf, buf_len),
            None => 0,
        }
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_tunnel_next_log(
    t: *mut peerova_tunnel,
    buf: *mut c_char,
    buf_len: i32,
) -> i32 {
    guard(0, || {
        let Some(t) = t.as_ref() else { return 0 };
        let mut g = t.t.shared.st.lock().unwrap();
        let Some(front) = g.logs.front() else {
            return 0;
        };
        let need = copy_out(front, buf, buf_len);
        if !buf.is_null() && buf_len >= need {
            g.logs.pop_front();
        }
        need
    })
}

#[no_mangle]
pub unsafe extern "C" fn peerova_tunnel_peer_count(t: *mut peerova_tunnel) -> u32 {
    guard(0, || t.as_ref().map_or(0, |t| t.t.peer_count() as u32))
}

#[no_mangle]
pub unsafe extern "C" fn peerova_tunnel_free(t: *mut peerova_tunnel) {
    if !t.is_null() {
        guard((), || drop(Box::from_raw(t)));
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::io::{Read, Write};
    use std::net::TcpStream;

    /// Echo service exposed through the tunnel; two concurrent TCP connections through one P2P
    /// connection, and the server maps each loopback socket back to the peer.
    #[test]
    fn tunnel_tcp_round_trip_and_player_ip() {
        let dir = std::env::temp_dir().join(format!("peerova-tunnel-test-{}", std::process::id()));
        std::fs::create_dir_all(&dir).unwrap();
        let svc = TcpListener::bind("127.0.0.1:0").unwrap();
        let svc_port = svc.local_addr().unwrap().port();
        let srcs = Arc::new(Mutex::new(vec![]));
        let srcs2 = srcs.clone();
        thread::spawn(move || {
            for s in svc.incoming() {
                let mut s = s.unwrap();
                srcs2.lock().unwrap().push(s.peer_addr().unwrap().ip());
                thread::spawn(move || {
                    let mut b = [0u8; 4096];
                    while let Ok(n) = s.read(&mut b) {
                        if n == 0 || s.write_all(&b[..n]).is_err() {
                            break;
                        }
                    }
                });
            }
        });
        let key = dir.join("t.key");
        let cfg = format!(
            "{{\"port\":0,\"upnp\":false,\"auto_supernode\":false,\"key_file\":{},\"expose\":[\"tcp:19129=127.0.0.1:{svc_port}\"]}}",
            peerova_core::json::escape(key.to_str().unwrap())
        );
        let srv = Tunnel::server(Some(&cfg)).expect("server");
        let ticket = srv.ticket().unwrap();
        let cli = Tunnel::client(&ticket, Some("{\"forward\":[\"tcp:0=19129\"]}")).expect("client");
        let lp = cli.local_port(0);
        assert_ne!(lp, 0);
        assert_eq!(
            cli.wait(Duration::from_secs(15)),
            TUNNEL_UP,
            "err={}",
            cli.error()
        );
        let mut socks = vec![];
        for i in 0..2u8 {
            let mut s = TcpStream::connect(("127.0.0.1", lp)).unwrap();
            s.set_read_timeout(Some(Duration::from_secs(5))).unwrap();
            let msg = vec![i + 1; 70_000];
            s.write_all(&msg).unwrap();
            let mut got = vec![0u8; msg.len()];
            s.read_exact(&mut got).unwrap();
            assert_eq!(got, msg);
            socks.push(s);
        }
        let ips = srcs.lock().unwrap().clone();
        assert_eq!(ips.len(), 2);
        // the service sees the player's own loopback address (same peer -> same 127.a.b.c)
        assert!(ips.iter().all(|ip| ip.is_loopback()));
        assert_eq!(ips[0], ips[1]);
        assert_eq!(srv.peer_count(), 1);
        drop(socks);
        drop(cli);
        drop(srv);
        let _ = std::fs::remove_dir_all(&dir);
    }

    /// Clients come and go: the server must keep accepting new P2P connections after earlier ones closed.
    #[test]
    fn tunnel_server_survives_sequential_clients() {
        let dir = std::env::temp_dir().join(format!("peerova-tunnel-seq-{}", std::process::id()));
        std::fs::create_dir_all(&dir).unwrap();
        let svc = TcpListener::bind("127.0.0.1:0").unwrap();
        let svc_port = svc.local_addr().unwrap().port();
        thread::spawn(move || {
            for s in svc.incoming() {
                let mut s = s.unwrap();
                thread::spawn(move || {
                    let mut b = [0u8; 4096];
                    while let Ok(n) = s.read(&mut b) {
                        if n == 0 || s.write_all(&b[..n]).is_err() {
                            break;
                        }
                    }
                });
            }
        });
        let key = dir.join("s.key");
        let cfg = format!(
            "{{\"port\":0,\"upnp\":false,\"auto_supernode\":false,\"key_file\":{},\"expose\":[\"tcp:19129=127.0.0.1:{svc_port}\"]}}",
            peerova_core::json::escape(key.to_str().unwrap())
        );
        let srv = Tunnel::server(Some(&cfg)).expect("server");
        let ticket = srv.ticket().unwrap();
        for round in 0..4 {
            let cli =
                Tunnel::client(&ticket, Some("{\"forward\":[\"tcp:0=19129\"]}")).expect("client");
            assert_eq!(
                cli.wait(Duration::from_secs(15)),
                TUNNEL_UP,
                "round {round}: {}",
                cli.error()
            );
            let mut s = TcpStream::connect(("127.0.0.1", cli.local_port(0))).unwrap();
            s.set_read_timeout(Some(Duration::from_secs(5))).unwrap();
            s.write_all(b"ping").unwrap();
            let mut got = [0u8; 4];
            s.read_exact(&mut got).unwrap();
            assert_eq!(&got, b"ping");
            if round % 2 == 0 {
                drop(s); // close the app socket first, then the tunnel
            }
            drop(cli);
            thread::sleep(Duration::from_millis(300));
        }
        while let Some(l) = srv.next_log() {
            eprintln!("srv: {l}");
        }
        let _ = std::fs::remove_dir_all(&dir);
    }

    #[test]
    fn tunnel_client_bad_ticket_is_error() {
        assert!(Tunnel::client("pvt_nope", Some("{\"forward\":[\"tcp:0=1\"]}")).is_err());
        assert!(Tunnel::server(Some("{\"port\":0}")).is_err()); // no expose
    }
}
