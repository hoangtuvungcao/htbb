//! peerova CLI — server agent, client forwarder, diagnostics, benchmarks.

use std::io::BufRead;
use std::net::{IpAddr, Ipv4Addr, SocketAddr, ToSocketAddrs};
use std::path::{Path, PathBuf};
use std::process::ExitCode;
use std::sync::OnceLock;
use std::thread;
use std::time::{Duration, Instant};

use peerova_core::crypto::Keypair;
use peerova_core::engine::{DEFAULT_PORT, DEV_MODE_CCU};
use peerova_core::license::{self, Lease};
use peerova_core::netinfo::{self, V4Class};
use peerova_core::tunnel::{self, HDR_BENCH, HDR_PING};
use peerova_core::util::{b64_encode, hex};
use peerova_core::{nat, Access, ClientConfig, Cmd, Engine, EngineEvent, JoinCode, ServerConfig};

static START: OnceLock<Instant> = OnceLock::new();

macro_rules! log {
    ($($a:tt)*) => {{
        let t = START.get_or_init(Instant::now).elapsed().as_secs_f32();
        println!("[{t:8.2}] {}", format!($($a)*));
    }};
}

// ---------------------------------------------------------------- args

struct Args {
    pos: Vec<String>,
    opts: Vec<(String, Option<String>)>,
}

const FLAGS: &[&str] = &["access", "rdv", "no-rdv", "vpn", "public", "no-map", "upnp", "help", "private", "supernode-mode", "reconnect", "public-bind", "no-tcp", "tcp", "no-auto-supernode", "no-relay", "hide-ip"];

impl Args {
    fn parse(raw: impl Iterator<Item = String>) -> Args {
        let raw: Vec<String> = raw.collect();
        let mut a = Args { pos: vec![], opts: vec![] };
        let mut i = 0;
        while i < raw.len() {
            if let Some(name) = raw[i].strip_prefix("--") {
                if !FLAGS.contains(&name) && i + 1 < raw.len() {
                    a.opts.push((name.into(), Some(raw[i + 1].clone())));
                    i += 1;
                } else {
                    a.opts.push((name.into(), None));
                }
            } else {
                a.pos.push(raw[i].clone());
            }
            i += 1;
        }
        a
    }
    /// Merge `--config file.json` (keys = flag names, `_` or `-`): CLI flags win.
    fn with_config(mut self) -> Result<Args, String> {
        let Some(path) = self.opt("config").map(String::from) else { return Ok(self) };
        let text = std::fs::read_to_string(&path).map_err(|e| format!("{path}: {e}"))?;
        let j = peerova_core::json::Json::parse(&text).map_err(|e| format!("{path}: {e}"))?;
        let peerova_core::json::Json::Obj(items) = j else { return Err(format!("{path}: phải là JSON object")) };
        use peerova_core::json::Json;
        for (k, v) in items {
            let name = k.replace('_', "-");
            if self.opts.iter().any(|(n, _)| *n == name) {
                continue;
            }
            match v {
                Json::Bool(true) => self.opts.push((name, None)),
                Json::Bool(false) | Json::Null => {}
                Json::Str(s) => self.opts.push((name, Some(s))),
                Json::Num(n) => self.opts.push((name, Some(if n.fract() == 0.0 { format!("{}", n as i64) } else { n.to_string() }))),
                Json::Arr(list) => {
                    for x in list {
                        if let Json::Str(s) = x {
                            self.opts.push((name.clone(), Some(s)));
                        }
                    }
                }
                Json::Obj(_) => return Err(format!("{path}: khoá {k} không được là object")),
            }
        }
        Ok(self)
    }
    fn flag(&self, n: &str) -> bool {
        self.opts.iter().any(|(k, _)| k == n)
    }
    fn opt(&self, n: &str) -> Option<&str> {
        self.opts.iter().rev().find(|(k, _)| k == n).and_then(|(_, v)| v.as_deref())
    }
    fn all(&self, n: &str) -> Vec<&str> {
        self.opts.iter().filter(|(k, _)| k == n).filter_map(|(_, v)| v.as_deref()).collect()
    }
    fn num<T: std::str::FromStr>(&self, n: &str, default: T) -> Result<T, String> {
        match self.opt(n) {
            Some(v) => v.parse().map_err(|_| format!("--{n} không hợp lệ: {v}")),
            None => Ok(default),
        }
    }
}

fn resolve_all(list: &[&str]) -> Result<Vec<SocketAddr>, String> {
    let mut out = vec![];
    for s in list {
        out.extend(s.to_socket_addrs().map_err(|e| format!("không phân giải được {s}: {e}"))?);
    }
    Ok(out)
}

/// Website used for room codes / lobbies (set from --site / --rendezvous / --license-server).
static SITE: OnceLock<String> = OnceLock::new();

fn site() -> &'static str {
    SITE.get().map(String::as_str).unwrap_or(license::DEFAULT_LICENSE_SERVER)
}

/// Ticket may be given inline, as @file, or as a 6-character connect code (resolved via the website).
fn read_ticket(s: &str) -> Result<String, String> {
    if peerova_core::lobby::is_room_code(s) {
        let t = peerova_core::lobby::resolve(site(), s)?;
        log!("mã kết nối {} → đã lấy ticket từ {}", s.to_ascii_uppercase(), site());
        return Ok(t);
    }
    match s.strip_prefix('@') {
        Some(path) => std::fs::read_to_string(path).map(|t| t.trim().to_string()).map_err(|e| format!("{path}: {e}")),
        None => Ok(s.to_string()),
    }
}

/// Minimal Prometheus text endpoint (GET anything → metrics). Bind to 127.0.0.1 by default.
fn spawn_metrics(addr: &str, h: peerova_core::Handle) -> Result<(), String> {
    use std::io::{Read, Write};
    let l = std::net::TcpListener::bind(addr).map_err(|e| format!("--metrics {addr}: {e}"))?;
    log!("metrics Prometheus: http://{addr}/metrics");
    thread::spawn(move || {
        for s in l.incoming().flatten() {
            let mut s = s;
            let _ = s.set_read_timeout(Some(Duration::from_secs(2)));
            let mut buf = [0u8; 1024];
            let _ = s.read(&mut buf);
            let st = h.stats.lock().unwrap().clone();
            let mut body = String::new();
            body.push_str("# HELP peerova_ccu Connected peers.\n# TYPE peerova_ccu gauge\n");
            body.push_str(&format!("peerova_ccu {}\npeerova_max_ccu {}\n", st.ccu, st.max_ccu));
            body.push_str("# TYPE peerova_connects_total counter\n");
            for (path, n) in &st.connects {
                body.push_str(&format!("peerova_connects_total{{path=\"{path}\"}} {n}\n"));
            }
            body.push_str(&format!(
                "# TYPE peerova_rejected_total counter\npeerova_rejected_total {}\n# TYPE peerova_dropped_unauthenticated_total counter\npeerova_dropped_unauthenticated_total {}\n# TYPE peerova_failures_total counter\npeerova_failures_total {}\n",
                st.rejected, st.dropped_unauthenticated, st.failures
            ));
            body.push_str(&format!("peerova_supernode {}\n", st.supernode_addr.is_some() as u8));
            let _ = write!(s, "HTTP/1.1 200 OK\r\nContent-Type: text/plain; version=0.0.4\r\nContent-Length: {}\r\nConnection: close\r\n\r\n{body}", body.len());
        }
    });
    Ok(())
}

// ---------------------------------------------------------------- keys

fn load_or_create_key(path: &Path) -> Result<Keypair, String> {
    let (k, created) = peerova_core::keyfile::load_or_create(path)?;
    if created {
        log!("đã tạo server key mới: {} (giữ bí mật; mất file = đổi ticket)", path.display());
    }
    Ok(k)
}

const ACCESS_KEY_PREFIX: &str = "pvak_";

fn load_access_seed(path: &str) -> Result<[u8; 32], String> {
    let s = std::fs::read_to_string(path).map_err(|e| format!("{path}: {e}"))?;
    s.trim()
        .strip_prefix(ACCESS_KEY_PREFIX)
        .and_then(peerova_core::util::b64_decode)
        .and_then(|v| v.try_into().ok())
        .ok_or_else(|| format!("{path}: không phải access key pvak_"))
}

fn parse_ttl(s: &str) -> Result<u64, String> {
    let (n, unit) = s.split_at(s.find(|c: char| !c.is_ascii_digit()).unwrap_or(s.len()));
    let n: u64 = n.parse().map_err(|_| format!("TTL không hợp lệ: {s}"))?;
    Ok(n * match unit {
        "" | "s" => 1,
        "m" => 60,
        "h" => 3600,
        "d" => 86400,
        _ => return Err(format!("đơn vị TTL không hợp lệ: {s} (dùng s/m/h/d)")),
    })
}

/// `peerova token issue --access-key admin.key --sub alice --ports tcp:443,udp:19132 --ttl 8h`
fn cmd_token(a: &Args) -> Result<(), String> {
    if a.pos.get(1).map(String::as_str) != Some("issue") {
        return Err("dùng: peerova token issue --access-key admin.key --sub NAME --ports tcp:443,udp:* --ttl 8h".into());
    }
    let seed = load_access_seed(a.opt("access-key").unwrap_or("access.key"))?;
    let ports: Vec<String> = a.opt("ports").unwrap_or("*").split(',').map(|s| s.trim().to_string()).filter(|s| !s.is_empty()).collect();
    let ttl = parse_ttl(a.opt("ttl").unwrap_or("8h"))?;
    let sub = a.opt("sub").ok_or("cần --sub (người dùng / thiết bị)")?;
    println!("{}", peerova_core::access::issue(&seed, sub, &ports, ttl, license::now_unix()));
    Ok(())
}

/// `peerova ticket merge <T1> <T2>...`: one ticket for several machines running
/// the SAME server key (copy server.key). Clients try all addresses in parallel
/// and fail over automatically when one machine is down or under attack.
fn cmd_ticket(a: &Args) -> Result<(), String> {
    if a.pos.get(1).map(String::as_str) != Some("merge") || a.pos.len() < 4 {
        return Err("dùng: peerova ticket merge <TICKET|@file> <TICKET|@file> ...".into());
    }
    let mut out: Option<peerova_core::Ticket> = None;
    for t in &a.pos[2..] {
        let t = peerova_core::Ticket::decode(&read_ticket(t)?)?;
        match &mut out {
            None => out = Some(t),
            Some(m) => {
                if m.server_pub != t.server_pub {
                    return Err("các ticket phải cùng server key (copy server.key sang mọi máy)".into());
                }
                for c in t.candidates {
                    if !m.candidates.iter().any(|x| x.addr == c.addr) {
                        m.candidates.push(c);
                    }
                }
                for s in t.supernodes {
                    if !m.supernodes.contains(&s) {
                        m.supernodes.push(s);
                    }
                }
                m.tcp |= t.tcp;
                m.private &= t.private;
            }
        }
    }
    let m = out.unwrap();
    eprintln!("đã gộp {} địa chỉ máy chủ vào một ticket", m.candidates.len());
    println!("{}", m.encode());
    Ok(())
}

/// `peerova stdio server ['<json config>']` | `peerova stdio client <TICKET> ['<json config>']`
///
/// Language-agnostic sidecar: JSON lines on stdin/stdout. Any language that can
/// spawn a process (Node, PHP, Ruby, Erlang, Lua, shell…) gets the full SDK.
///   out: {"event":"ready","ticket":"pvt_..."}            (server; client: no ticket)
///        {"event":"connected","peer":N}
///        {"event":"message","peer":N,"channel":C,"reliable":true,"data":"<base64url>"}
///        {"event":"disconnected","peer":N,"reason":"...","code":"E_CLOSED"}
///        {"event":"connect_failed","reason":"...","code":"E_TIMEOUT"}
///        {"event":"error","message":"...","code":"E_UNKNOWN_PEER"}   (also once before exiting if start fails)
///   reason/message are English SDK text "E_NAME: ..." (stable codes, see peerova.h); this is a
///   machine protocol for SDK users, unlike the CLI's Vietnamese human output.
///   in:  {"cmd":"send","peer":N,"channel":C,"reliable":true,"data":"<base64url>"}
///        {"cmd":"disconnect","peer":N}   {"cmd":"quit"}
fn cmd_stdio(a: &Args) -> Result<(), String> {
    use peerova_core::json::{escape, Json};
    use peerova_core::util::b64_decode;
    use peerova::{classify, Sdk, SdkError, EV_CONNECTED, EV_CONNECT_FAILED, EV_DISCONNECTED, EV_MESSAGE};
    use std::io::Write;
    let mode = a.pos.get(1).map(String::as_str).ok_or("dùng: peerova stdio server ['<json>'] | peerova stdio client <TICKET> ['<json>']")?;
    let out = std::io::stdout();
    let emit = |line: String| {
        let mut o = out.lock();
        let _ = writeln!(o, "{line}");
        let _ = o.flush();
    };
    // `"<field>":"E_NAME: English","code":"E_NAME"` for one SDK error.
    let coded = |field: &str, e: SdkError| format!("{}:{},\"code\":{}", escape(field), escape(&e.to_string()), escape(e.code.name()));
    let started = match mode {
        "server" => Sdk::server(a.pos.get(2).map(String::as_str)),
        "client" => match a.pos.get(2) {
            None => Err("thiếu TICKET".to_string()),
            Some(t) => read_ticket(t).and_then(|t| Sdk::client(&t, a.pos.get(3).map(String::as_str))),
        },
        _ => return Err("mode phải là server hoặc client".into()),
    };
    let mut sdk = match started {
        Ok(s) => s,
        Err(e) => {
            emit(format!("{{\"event\":\"error\",{}}}", coded("message", classify(&e))));
            return Err(e); // stderr keeps the Vietnamese text for humans
        }
    };
    match sdk.ticket() {
        Some(t) => emit(format!("{{\"event\":\"ready\",\"ticket\":{}}}", escape(t))),
        None => emit("{\"event\":\"ready\"}".into()),
    }
    let (tx, rx) = std::sync::mpsc::channel::<String>();
    thread::spawn(move || {
        for line in std::io::stdin().lock().lines() {
            let Ok(l) = line else { break };
            if tx.send(l).is_err() {
                break;
            }
        }
        let _ = tx.send("{\"cmd\":\"quit\"}".into()); // stdin closed → exit cleanly
    });
    loop {
        while let Ok(line) = rx.try_recv() {
            if line.trim().is_empty() {
                continue;
            }
            let j = match Json::parse(&line) {
                Ok(j) => j,
                Err(_) => {
                    let err = SdkError::new(peerova::ErrorCode::InvalidArg, "invalid JSON command");
                    emit(format!("{{\"event\":\"error\",{}}}", coded("message", err)));
                    continue;
                }
            };
            let peer = j.u64("peer").unwrap_or(0) as u32;
            match j.str("cmd") {
                Some("send") => {
                    let data = j.str("data").and_then(b64_decode).unwrap_or_default();
                    let ch = j.u64("channel").unwrap_or(0).min(255) as u8;
                    if let Err(e) = sdk.send(peer, ch, &data, j.bool("reliable").unwrap_or(true)) {
                        emit(format!("{{\"event\":\"error\",{}}}", coded("message", classify(&e))));
                    }
                }
                Some("disconnect") => sdk.disconnect(peer),
                Some("quit") => return Ok(()),
                _ => emit(format!("{{\"event\":\"error\",{}}}", coded("message", SdkError::new(peerova::ErrorCode::InvalidArg, "unknown cmd")))),
            }
        }
        if let Some(ev) = sdk.poll_wait(Duration::from_millis(5)) {
            let reason = || coded("reason", peerova::event_error(&ev).unwrap_or_else(|| classify("")));
            emit(match ev.kind {
                EV_CONNECTED => format!("{{\"event\":\"connected\",\"peer\":{}}}", ev.peer),
                EV_DISCONNECTED => format!("{{\"event\":\"disconnected\",\"peer\":{},{}}}", ev.peer, reason()),
                EV_CONNECT_FAILED => format!("{{\"event\":\"connect_failed\",{}}}", reason()),
                EV_MESSAGE => format!(
                    "{{\"event\":\"message\",\"peer\":{},\"channel\":{},\"reliable\":{},\"data\":\"{}\"}}",
                    ev.peer,
                    ev.channel,
                    ev.reliable,
                    b64_encode(&ev.data)
                ),
                _ => continue,
            });
        }
    }
}

fn cmd_keygen(a: &Args) -> Result<(), String> {
    if a.flag("access") {
        let path = PathBuf::from(a.opt("key").unwrap_or("access.key"));
        if path.exists() {
            return Err(format!("{} đã tồn tại", path.display()));
        }
        let seed: [u8; 32] = peerova_core::crypto::random_bytes();
        std::fs::write(&path, format!("{ACCESS_KEY_PREFIX}{}\n", b64_encode(&seed))).map_err(|e| e.to_string())?;
        #[cfg(unix)]
        {
            use std::os::unix::fs::PermissionsExt;
            let _ = std::fs::set_permissions(&path, std::fs::Permissions::from_mode(0o600));
        }
        println!("access key (BÍ MẬT, giữ ở backend cấp quyền / SSO): {}", path.display());
        println!("public key cho server (--access-key): {}", b64_encode(&peerova_core::access::public_key(&seed)));
        return Ok(());
    }
    let path = PathBuf::from(a.opt("key").unwrap_or("server.key"));
    if path.exists() {
        return Err(format!("{} đã tồn tại — xoá nó trước nếu muốn tạo key mới", path.display()));
    }
    let k = load_or_create_key(&path)?;
    println!("public key: {}", b64_encode(&k.public));
    Ok(())
}

// ---------------------------------------------------------------- doctor

fn cmd_doctor() -> Result<(), String> {
    println!("=== peerova doctor — chẩn đoán khả năng kết nối P2P ===\n");
    println!("OS           : {} / {}", std::env::consts::OS, std::env::consts::ARCH);
    let v4 = netinfo::local_ipv4();
    let v6 = netinfo::global_ipv6();
    let gw = netinfo::default_gateway_v4();
    println!("IPv4 LAN     : {}", v4.map(|i| i.to_string()).unwrap_or("không có".into()));
    println!("Gateway      : {}", gw.map(|i| i.to_string()).unwrap_or("không rõ".into()));
    println!("IPv6 global  : {}", v6.map(|i| i.to_string()).unwrap_or("KHÔNG CÓ".into()));
    // Automatic fallback route: open a test room (both roles) and exchange one message.
    let relay_url = std::env::var("PEEROVA_RELAY").unwrap_or(peerova_core::license::DEFAULT_RELAY.to_string());
    let t0 = std::time::Instant::now();
    let room = peerova_core::relay::room(&[0x5a; 32], peerova_core::crypto::random_u32() as u64);
    let check = (|| -> Result<u128, String> {
        let mut s = peerova_core::relay::connect(&peerova_core::relay::url(&relay_url, &room, "s", 0), None).map_err(|e| e.to_string())?;
        let mut c = peerova_core::relay::connect(&peerova_core::relay::url(&relay_url, &room, "c", 0), None).map_err(|e| e.to_string())?;
        s.tcp.set_read_timeout(Some(Duration::from_millis(100))).ok();
        let t1 = std::time::Instant::now();
        for _ in 0..30 {
            c.send(b"ping").map_err(|e| e.to_string())?;
            let mut got = vec![];
            s.recv(&mut got).map_err(|e| e.to_string())?;
            if !got.is_empty() {
                return Ok(t1.elapsed().as_millis());
            }
        }
        Err("không nhận được dữ liệu qua đường dự phòng".into())
    })();
    match &check {
        Ok(ms) => println!("Dự phòng     : ✔ sẵn sàng (mở {} ms, trễ một chiều ~{} ms)", t0.elapsed().as_millis(), ms),
        Err(e) => println!("Dự phòng     : ✘ {e}"),
    }
    let mut ext: Option<Ipv4Addr> = v4.filter(|ip| netinfo::classify_v4(ip) == V4Class::Public);
    if ext.is_some() {
        println!("             → máy có IPv4 public trực tiếp (VPS / modem bridge)");
    }
    let mut mapped = false;
    print!("\nNAT-PMP/PCP  : ");
    match gw.map(nat::natpmp::external_address) {
        Some(Ok(ip)) => {
            println!("có, IP ngoài {ip}");
            ext.get_or_insert(ip);
        }
        Some(Err(e)) => println!("{e}"),
        None => println!("không có gateway"),
    }
    print!("UPnP-IGD     : ");
    if let Some(ip) = v4 {
        match nat::upnp::discover(ip, Duration::from_millis(2500)) {
            Ok(igd) => {
                println!("có");
                if let Ok(e) = igd.external_ip() {
                    println!("  IP ngoài   : {e}");
                    ext.get_or_insert(e);
                }
                match igd.add_udp_mapping(47999, 47999, ip, 60) {
                    Ok(_) => {
                        println!("  Thử mở port: ✔ được (đã xoá lại)");
                        mapped = true;
                        let _ = igd.delete_udp_mapping(47999);
                    }
                    Err(e) => println!("  Thử mở port: ✘ {e}"),
                }
            }
            Err(e) => println!("{e}"),
        }
    }
    println!("Machine ID   : {}", license::machine_id());
    println!("\n=== Kết luận ===");
    if v6.is_some() {
        println!("✔ Có IPv6 global → kết nối IPv6 trực tiếp gần như luôn thành công.");
    } else {
        println!("✘ Không có IPv6. Bật IPv6 trên router/modem (Viettel/VNPT/FPT đều hỗ trợ) — cải thiện lớn nhất.");
    }
    match ext.map(|e| (e, netinfo::classify_v4(&e))) {
        Some((_, V4Class::Public)) if mapped || v4.is_some_and(|ip| netinfo::classify_v4(&ip) == V4Class::Public) => {
            println!("✔ Reachable qua IPv4 public → làm SERVER/SUPERNODE tốt. Chỉ cần cho phép UDP port của peerova qua firewall.")
        }
        Some((_, V4Class::Public)) => println!("~ IP ngoài public nhưng chưa mở port tự động được → bật UPnP, hoặc forward 1 port UDP."),
        Some((e, V4Class::Cgnat)) => println!("✘ IP ngoài {e} là CGNAT: dùng IPv6 hoặc xin nhà mạng IP public."),
        Some((e, V4Class::Private)) => println!("✘ IP ngoài {e} vẫn là IP nội bộ → double NAT: bật bridge mode trên modem."),
        _ => println!("? Chưa xác định được IP ngoài — không sao: khi chạy server, kết nối vẫn tự thiết lập qua website."),
    }
    Ok(())
}

// ---------------------------------------------------------------- license

fn license_opts(a: &Args, key_path: &Path, spk: [u8; 32]) -> Result<Option<license::LicenseConfig>, String> {
    let Some(key) = a.opt("license").map(String::from).or_else(|| std::env::var("PEEROVA_LICENSE").ok()) else { return Ok(None) };
    let auth = a
        .opt("authority-pubkey")
        .map(String::from)
        .or_else(|| license::DEFAULT_AUTHORITY_PUBKEY.map(String::from))
        .ok_or("build này chưa nhúng authority public key: thêm --authority-pubkey <b64>")?;
    let dir = key_path.parent().filter(|p| !p.as_os_str().is_empty()).unwrap_or(Path::new("."));
    Ok(Some(license::LicenseConfig {
        key,
        server: a.opt("license-server").unwrap_or(license::DEFAULT_LICENSE_SERVER).to_string(),
        authority: license::parse_pubkey(&auth)?,
        cache: license::cache_path(dir),
        spk,
    }))
}

fn describe(l: &Lease) -> String {
    let days = l.exp.saturating_sub(license::now_unix()) / 86400;
    format!("plan {} · {} CCU · {} server · lease còn {} ngày", l.plan, l.ccu, l.srv, days)
}

// ---------------------------------------------------------------- server

fn cmd_server(a: &Args) -> Result<(), String> {
    let key_path = PathBuf::from(a.opt("key").unwrap_or("server.key"));
    let key = load_or_create_key(&key_path)?;
    let spk = key.public;
    let mut cfg = ServerConfig::new(key);
    cfg.port = a.num("port", DEFAULT_PORT)?;
    cfg.workers = a.num("workers", 0)?;
    cfg.supernodes = resolve_all(&a.all("supernode"))?;
    cfg.supernode_mode = a.flag("supernode-mode");
    cfg.try_map = !a.flag("no-map");
    cfg.private_ticket = a.flag("private");
    cfg.tcp_fallback = !a.flag("no-tcp");
    cfg.auto_supernode = !a.flag("no-auto-supernode");
    // Website rendezvous is ON by default (no VPS / supernode needed); `--no-rdv` turns it off.
    cfg.rendezvous = if a.flag("no-rdv") {
        None
    } else {
        Some(a.opt("rendezvous").unwrap_or(site()).to_string())
    };
    // Hide the server's IP: clients only ever see relays (anti-DDoS). Needs a relay.
    cfg.hide_ip = a.flag("hide-ip");
    // Keep the website binding token next to the key: restarts are recognised at once.
    cfg.rdv_token_file = Some(PathBuf::from(format!("{}.rdv", key_path.display())));
    if cfg.hide_ip && a.flag("no-relay") {
        return Err("--hide-ip cần đường dự phòng (bỏ --no-relay)".into());
    }
    // Last-resort relay (only when punching fails): ON by default; `--relay URL` / `--no-relay`.
    cfg.relay = if a.flag("no-relay") {
        None
    } else {
        Some(a.opt("relay").map(String::from).or(a.opt("rendezvous").map(String::from)).unwrap_or(peerova_core::license::DEFAULT_RELAY.to_string()))
    };
    if let Some(t) = a.opt("token") {
        cfg.access = Access::Token(t.as_bytes().to_vec());
    }
    let revoked_file = a.opt("revoked").map(PathBuf::from);
    let read_revoked = |p: &Path| -> std::collections::HashSet<String> {
        std::fs::read_to_string(p)
            .map(|s| s.lines().map(|l| l.trim().to_string()).filter(|l| !l.is_empty() && !l.starts_with('#')).collect())
            .unwrap_or_default()
    };
    if let Some(k) = a.opt("access-key") {
        let authority = license::parse_pubkey(k).map_err(|_| "--access-key phải là public key 32 byte b64url (từ `peerova keygen --access`)")?;
        let revoked = revoked_file.as_deref().map(read_revoked).unwrap_or_default();
        log!("chế độ doanh nghiệp: chỉ nhận access token ký bởi {}… ({} mục thu hồi)", &k[..10.min(k.len())], revoked.len());
        cfg.access = Access::Signed { authority, revoked };
    }
    let mut rules = vec![];
    for e in a.all("expose") {
        rules.extend(tunnel::parse_expose_list(e)?);
    }
    let audit_file = match a.opt("audit-log") {
        Some(p) => Some(std::sync::Arc::new(std::sync::Mutex::new(
            std::fs::OpenOptions::new().create(true).append(true).open(p).map_err(|e| format!("{p}: {e}"))?,
        ))),
        None => None,
    };

    let lic = license_opts(a, &key_path, spk)?;
    let mut ccu = DEV_MODE_CCU;
    if let Some(l) = &lic {
        let (lease, sns) = license::prepare(l);
        match lease {
            Ok(lease) => {
                log!("✔ license hợp lệ: {}", describe(&lease));
                ccu = lease.ccu as usize;
                cfg.lease = Some(lease.token);
                cfg.authority = Some(l.authority);
            }
            Err(e) => {
                log!("✘ license: {e}");
                log!("→ chạy ở chế độ DEV (tối đa {DEV_MODE_CCU} người kết nối)");
            }
        }
        for sn in sns {
            if !cfg.supernodes.contains(&sn) {
                cfg.supernodes.push(sn);
            }
        }
        if !cfg.supernodes.is_empty() {
            if std::env::var_os("PEEROVA_DEBUG").is_some() {
                log!("[debug] supernodes: {:?}", cfg.supernodes);
            }
        }
    } else {
        log!("chưa có license → chế độ DEV, tối đa {DEV_MODE_CCU} client. Dùng thử 14 ngày tại website.");
    }
    if let Some(m) = a.opt("max-ccu") {
        ccu = ccu.min(m.parse().map_err(|_| "--max-ccu không hợp lệ")?);
    }
    cfg.max_ccu = ccu;

    // `--share "Name"` (alias `--lobby`): publish a 6-char connect code for this service.
    let lobby_info = a.opt("share").or(a.opt("lobby")).map(|name| peerova_core::lobby::LobbyInfo {
        app: a.opt("app").unwrap_or("").to_string(),
        name: name.to_string(),
        public: a.flag("public"),
        max: a.opt("max").and_then(|m| m.parse().ok()).unwrap_or(0),
        mode: a.opt("mode").unwrap_or("").to_string(),
        region: a.opt("region").unwrap_or("").to_string(),
        version: a.opt("app-version").unwrap_or("").to_string(),
        tags: a.all("tag").iter().map(|t| t.to_string()).collect(),
    });
    if lobby_info.as_ref().is_some_and(|l| l.app.is_empty()) {
        return Err("--share cần --app <project id> (xem trong dashboard website)".into());
    }
    let engine = Engine::start_server(cfg).map_err(|e| e.to_string())?;
    if let Some(m) = a.opt("metrics") {
        spawn_metrics(m, engine.handle.clone())?;
    }
    let mut lobby_once = lobby_info;
    let lobby_handle = engine.handle.clone();
    let handle = engine.handle.clone();
    if let Some(l) = lic {
        license::spawn_renewal(l, handle.clone());
    }
    // Hot-reload the revocation list: revoked users are kicked within ~10 s.
    if let Some(p) = revoked_file.clone() {
        let h = handle.clone();
        thread::spawn(move || {
            let mut last = std::fs::metadata(&p).and_then(|m| m.modified()).ok();
            loop {
                thread::sleep(Duration::from_secs(10));
                let now = std::fs::metadata(&p).and_then(|m| m.modified()).ok();
                if now != last {
                    last = now;
                    let list = read_revoked(&p);
                    println!("đã nạp lại danh sách thu hồi ({} mục)", list.len());
                    h.send(Cmd::SetRevoked(list));
                }
            }
        });
    }

    // Console: paste join codes, show stats.
    let h = handle.clone();
    thread::spawn(move || {
        for line in std::io::stdin().lock().lines() {
            let Ok(l) = line else { break };
            let l = l.trim();
            if l.starts_with("pvj_") {
                match JoinCode::decode(l) {
                    Ok(j) => h.send(Cmd::Punch(j.candidates)),
                    Err(e) => println!("✘ {e}"),
                }
            } else if l == "stats" {
                let s = h.stats.lock().unwrap().clone();
                println!("  CCU {}/{} · kết nối theo đường {:?} · gói lạ bị bỏ {} · từ chối {}", s.ccu, s.max_ccu, s.connects, s.dropped_unauthenticated, s.rejected);
            } else if !l.is_empty() {
                println!("  lệnh: stats | <dán join code pvj_...>");
            }
        }
    });

    let ticket_file = a.opt("ticket-file").map(PathBuf::from);
    if rules.is_empty() {
        log!("không có --expose: chỉ phục vụ SDK/ping/bench. Ví dụ: --expose tcp:25565 --expose udp:19132 --expose https:443");
    }
    if rules.len() <= 12 {
        for r in &rules {
            log!("expose {:?} port {} → {}", r.proto, r.port, r.target);
        }
    } else {
        log!("expose {} port (dải port)", rules.len());
    }
    let audit = |rec: String| {
        if let Some(f) = &audit_file {
            use std::io::Write;
            let _ = writeln!(f.lock().unwrap(), "{rec}");
        }
    };
    let ts = || license::now_unix();
    let audit_ev = audit.clone();
    let audit_open = audit;
    let on_open = move |o: &tunnel::Audit| {
        audit_open(format!(
            "{{\"ts\":{},\"event\":\"open\",\"conn\":\"{:08x}\",\"proto\":\"{}\",\"port\":{},\"allowed\":{}}}",
            ts(),
            o.conn,
            if o.proto == tunnel::Proto::Tcp { "tcp" } else { "udp" },
            o.port,
            o.allowed
        ))
    };
    if a.flag("vpn") {
        let subnet = a.opt("subnet").unwrap_or("10.77.0.0/16").to_string();
        let opts = peerova_core::vpn::VpnOptions { allow: a.opt("vpn-allow").map(peerova_core::vpn::parse_allow).transpose()? };
        if let Some(f) = a.opt("banned") {
            tunnel::watch_banned(PathBuf::from(f), |n| log!("danh sách cấm: {n} người chơi"));
        }
        return peerova_core::vpn::serve_with(&engine, &subnet, opts, |e| match e {
            EngineEvent::Ready { ticket: Some(t), .. } => {
                println!("\n┌─ TICKET (VPN) ────────────────────────────────────────");
                println!("{t}");
                println!("└───────────────────────────────────────────────────────");
                if let Some(p) = &ticket_file {
                    let _ = std::fs::write(p, t);
                }
                let admin = if cfg!(windows) { "(Administrator) " } else { "sudo " };
                println!("Client: {admin}peerova client <TICKET> --vpn   → ping / FTP / mọi giao thức IP\n");
            }
            EngineEvent::Connected { conn, addr, path, .. } => log!("✔ client {conn:08x} kết nối từ {addr} [{path}]"),
            EngineEvent::Disconnected { conn, reason } => log!("client {conn:08x} ngắt: {reason}"),
            EngineEvent::Log(m) => log!("{m}"),
            _ => {}
        });
    }
    // Ban list (player addresses as seen by the game, 127.a.b.c), hot-reloaded.
    if let Some(f) = a.opt("banned") {
        tunnel::watch_banned(PathBuf::from(f), |n| log!("danh sách cấm: {n} người chơi"));
    }
    tunnel::serve_audited(&engine, rules, |e| match e {
        EngineEvent::Ready { ticket: Some(t), candidates, .. } => {
            println!("\nCandidates:");
            for c in candidates {
                println!("    {:<10} {}", c.kind.label(), c.addr);
            }
            println!("\n┌─ TICKET ──────────────────────────────────────────────");
            println!("{t}");
            println!("└───────────────────────────────────────────────────────");
            if let Some(p) = &ticket_file {
                if std::fs::write(p, t).is_ok() {
                    println!("(đã lưu vào {})", p.display());
                }
            }
            println!("Client:  peerova client <TICKET> --forward tcp:PORT   |   đo ping: peerova ping <TICKET>");
            println!("Console: stats | dán join code pvj_...\n");
            if let Some(info) = lobby_once.take() {
                let h = lobby_handle.clone();
                let name = info.name.clone();
                peerova_core::lobby::spawn_heartbeat(
                    site().to_string(),
                    info,
                    t.clone(),
                    { let h = h.clone(); move || h.stats.lock().unwrap().ccu as u32 },
                    move || h.lease.lock().unwrap().clone(),
                    move |r| match r {
                        Ok(code) => {
                            println!("\n┌─ MÃ KẾT NỐI ──────────────");
                            println!("│   {code}      ({name})");
                            println!("└───────────────────────────");
                            println!("Người dùng chỉ cần: peerova client {code} --forward ...\n");
                        }
                        Err(e) => println!("lobby: {e}"),
                    },
                );
            }
        }
        EngineEvent::Connected { conn, addr, path, peer_key, subject, scopes } => {
            log!(
                "✔ client {conn:08x} kết nối từ {addr} [{path}] key {}{}",
                &hex(peer_key)[..12],
                subject.as_ref().map(|s| format!(" · {s} {:?}", scopes.as_deref().unwrap_or(&[]))).unwrap_or_default()
            );
            audit_ev(format!(
                "{{\"ts\":{},\"event\":\"connect\",\"conn\":\"{conn:08x}\",\"addr\":\"{addr}\",\"path\":\"{path}\",\"key\":\"{}\",\"sub\":{}}}",
                ts(),
                hex(peer_key),
                subject.as_deref().map(peerova_core::json::escape).unwrap_or("null".into())
            ));
        }
        EngineEvent::Disconnected { conn, reason } => {
            log!("client {conn:08x} ngắt: {reason}");
            audit_ev(format!("{{\"ts\":{},\"event\":\"disconnect\",\"conn\":\"{conn:08x}\",\"reason\":{}}}", ts(), peerova_core::json::escape(reason)));
        }
        EngineEvent::Log(m) => {
            log!("{m}");
            if m.starts_with("từ chối") || m.contains("thu hồi") || m.contains("CCU") {
                audit_ev(format!("{{\"ts\":{},\"event\":\"security\",\"msg\":{}}}", ts(), peerova_core::json::escape(m)));
            }
        }
        _ => {}
    }, on_open);
    Ok(())
}

// ---------------------------------------------------------------- client

/// The client's identity key, kept across runs so a player keeps the same identity (and
/// the same address in the server's game). `--key FILE` overrides the default location.
fn client_key(a: &Args) -> Result<peerova_core::crypto::Keypair, String> {
    Ok(peerova_core::keyfile::load_or_create(&client_key_path(a)?)?.0)
}

fn client_key_path(a: &Args) -> Result<PathBuf, String> {
    let path = match a.opt("key") {
        Some(p) => PathBuf::from(p),
        None => {
            let base = std::env::var_os("APPDATA")
                .map(PathBuf::from)
                .or_else(|| std::env::var_os("XDG_CONFIG_HOME").map(PathBuf::from))
                .or_else(|| std::env::var_os("HOME").map(|h| PathBuf::from(h).join(".config")))
                .ok_or("không xác định được thư mục cấu hình; dùng --key FILE")?;
            let dir = base.join("peerova");
            std::fs::create_dir_all(&dir).map_err(|e| format!("tạo {}: {e}", dir.display()))?;
            dir.join("client.key")
        }
    };
    Ok(path)
}

fn client_cfg(a: &Args) -> Result<ClientConfig, String> {
    Ok(ClientConfig {
        key: client_key(a)?,
        token: a.opt("access-token").or(a.opt("token")).unwrap_or("").trim().as_bytes().to_vec(),
        timeout: Duration::from_secs(a.num("timeout", 20)?),
        extra_supernodes: resolve_all(&a.all("supernode"))?,
        try_map: a.flag("upnp"),
        force_tcp: a.flag("tcp"),
        rendezvous: a.opt("rendezvous").map(String::from),
        relay: a.opt("relay").map(String::from),
        no_relay: a.flag("no-relay"),
        // Working paths / NAT classes from earlier runs (next to the identity key): faster reconnects.
        cache_file: client_key_path(a)?.parent().map(|d| d.join("paths.cache")),
        ..Default::default()
    })
}

fn print_client_event(e: &EngineEvent, show_join: bool) {
    match e {
        // Manual join codes are a debugging aid only (connections are automatic now).
        EngineEvent::Ready { candidates, session, .. } if show_join && std::env::var_os("PEEROVA_DEBUG").is_some() => {
            let code = JoinCode { session: *session, candidates: candidates.clone() }.encode();
            println!("[debug] join code: {code}");
        }
        EngineEvent::Connected { addr, path, .. } if *path == "relay" => log!("✔ ĐÃ KẾT NỐI qua đường dự phòng tự động — mã hoá E2E (sẽ tự chuyển sang trực tiếp khi mạng cho phép)"),
        EngineEvent::Connected { addr, path, .. } => log!("✔ ĐÃ KẾT NỐI TRỰC TIẾP tới {addr} [{path}] — mã hoá E2E"),
        EngineEvent::Disconnected { reason, .. } => log!("mất kết nối: {reason}"),
        EngineEvent::ConnectFailed(e) => log!("✘ {e}"),
        EngineEvent::Log(m) => log!("{m}"),
        _ => {}
    }
}

fn cmd_client(a: &Args) -> Result<(), String> {
    let ticket = read_ticket(a.pos.get(1).ok_or("thiếu TICKET. Dùng: peerova client <TICKET|@file> --forward tcp:PORT")?)?;
    let bind: IpAddr = if a.flag("public-bind") { IpAddr::V4(Ipv4Addr::UNSPECIFIED) } else { a.opt("bind").unwrap_or("127.0.0.1").parse().map_err(|_| "--bind không hợp lệ")? };
    let mut rules = vec![];
    for f in a.all("forward") {
        rules.extend(tunnel::parse_forward_list(f, bind)?);
    }
    if rules.is_empty() && !a.flag("vpn") {
        return Err("cần ít nhất một --forward (hoặc --vpn), ví dụ --forward tcp:25565 --forward udp:19132 --forward tcp:8443=443".into());
    }
    let manual = peerova_core::Ticket::decode(&ticket)?.supernodes.is_empty() && a.all("supernode").is_empty();
    loop {
        let engine = Engine::connect(&ticket, client_cfg(a)?)?;
        let r = if a.flag("vpn") {
            peerova_core::vpn::client(&engine, |e| print_client_event(e, manual))
        } else {
            tunnel::forward(&engine, rules.clone(), |e| print_client_event(e, manual))
        };
        if !a.flag("reconnect") {
            return r;
        }
        log!("kết nối lại sau 2s...");
        thread::sleep(Duration::from_secs(2));
    }
}

// ---------------------------------------------------------------- ping / bench

fn cmd_ping(a: &Args) -> Result<(), String> {
    let ticket = read_ticket(a.pos.get(1).ok_or("thiếu TICKET")?)?;
    let count: u32 = a.num("count", 50)?;
    let interval = Duration::from_millis(a.num("interval-ms", 100)?);
    let bench_mb: usize = a.num("bench-mb", 8)?;
    let t_connect = Instant::now();
    let engine = Engine::connect(&ticket, client_cfg(a)?)?;
    let (conn, path, addr) = loop {
        match engine.recv_timeout(Duration::from_secs(60)) {
            Some(EngineEvent::Connected { conn, path, addr, .. }) => break (conn, path, addr),
            Some(EngineEvent::ConnectFailed(e)) => return Err(e),
            Some(e) => print_client_event(&e, false),
            None => return Err("timeout".into()),
        }
    };
    let hs = t_connect.elapsed();
    println!("Kết nối tới {addr} [{path}] trong {:.0} ms (gồm gather + punch + Noise handshake)\n", hs.as_secs_f64() * 1000.0);

    // --- latency (unreliable datagram echo, same path as game traffic)
    let t0 = Instant::now();
    let mut rtts: Vec<f64> = vec![];
    let mut next = Instant::now();
    let mut sent = 0u32;
    let end_wait = |sent: u32| sent >= count;
    let mut deadline = None;
    loop {
        let now = Instant::now();
        if !end_wait(sent) && now >= next {
            let mut d = vec![HDR_PING];
            d.extend_from_slice(&sent.to_be_bytes());
            d.extend_from_slice(&(now - t0).as_nanos().to_be_bytes()[8..]);
            engine.handle.send(Cmd::Datagram { conn, data: d });
            sent += 1;
            next = now + interval;
            if end_wait(sent) {
                deadline = Some(now + Duration::from_secs(2));
            }
        }
        if deadline.is_some_and(|d| now >= d) {
            break;
        }
        let ev = engine.recv_timeout(Duration::from_millis(5));
        if let Some(e @ EngineEvent::Log(_)) = &ev {
            print_client_event(e, false); // e.g. "switched to direct" after a fallback start
        }
        if let Some(EngineEvent::Datagram { data, .. }) = ev {
            if data.len() == 13 && data[0] == HDR_PING {
                let ts = u64::from_be_bytes(data[5..13].try_into().unwrap());
                let rtt = (Instant::now() - t0).as_secs_f64() * 1000.0 - ts as f64 / 1e6;
                let seq = u32::from_be_bytes(data[1..5].try_into().unwrap());
                if seq % 10 == 0 || count <= 20 {
                    log!("ping #{seq}: {rtt:.2} ms");
                }
                rtts.push(rtt);
            }
        }
    }
    if !rtts.is_empty() {
        let mut s = rtts.clone();
        s.sort_by(|a, b| a.partial_cmp(b).unwrap());
        let avg = s.iter().sum::<f64>() / s.len() as f64;
        let p95 = s[((s.len() as f64 * 0.95) as usize).min(s.len() - 1)];
        let jitter = rtts.windows(2).map(|w| (w[1] - w[0]).abs()).sum::<f64>() / (rtts.len().max(2) - 1) as f64;
        println!(
            "\nLATENCY  {}/{} gói (mất {:.1}%) · min {:.2} · avg {:.2} · p95 {:.2} · max {:.2} ms · jitter {:.2} ms",
            rtts.len(),
            count,
            100.0 * (count as f64 - rtts.len() as f64) / count as f64,
            s[0],
            avg,
            p95,
            s[s.len() - 1],
            jitter
        );
    }

    // --- throughput (reliable stream, congestion controlled)
    if bench_mb > 0 {
        let id = engine.handle.alloc_stream();
        let total = bench_mb << 20;
        let t = Instant::now();
        engine.handle.send(Cmd::StreamWrite { conn, id, data: vec![HDR_BENCH, 0, 0] });
        let chunk = vec![0x5au8; 64 << 10];
        let mut queued = 0;
        while queued < total {
            engine.handle.send(Cmd::StreamWrite { conn, id, data: chunk.clone() });
            queued += chunk.len();
        }
        engine.handle.send(Cmd::StreamFinish { conn, id });
        let got = loop {
            match engine.recv_timeout(Duration::from_secs(120)) {
                Some(EngineEvent::StreamData { data, .. }) if data.len() == 8 => break u64::from_be_bytes(data.try_into().unwrap()),
                Some(EngineEvent::Disconnected { reason, .. }) => return Err(reason),
                Some(_) => {}
                None => return Err("bench timeout".into()),
            }
        };
        let secs = t.elapsed().as_secs_f64();
        println!(
            "UPLOAD   {:.1} MB trong {:.2}s = {:.2} MB/s ({:.1} Mbit/s) · server nhận đủ {} byte {}",
            total as f64 / 1048576.0,
            secs,
            total as f64 / 1048576.0 / secs,
            total as f64 * 8.0 / 1e6 / secs,
            got,
            if got as usize == total { "✔" } else { "✘ THIẾU" }
        );
    }
    let st = engine.handle.stats.lock().unwrap().clone();
    let _ = st;
    Ok(())
}

// ---------------------------------------------------------------- misc

fn cmd_lease_info(a: &Args) -> Result<(), String> {
    let token = read_ticket(a.pos.get(1).ok_or("thiếu lease token")?)?;
    let auth = a.opt("authority-pubkey").map(String::from).or(license::DEFAULT_AUTHORITY_PUBKEY.map(String::from)).ok_or("cần --authority-pubkey")?;
    let l = license::verify(&token, &license::parse_pubkey(&auth)?, None, license::now_unix())?;
    println!("✔ chữ ký hợp lệ · {} · lid {} · máy {}", describe(&l), l.lid, l.mid);
    println!("  machine id máy này: {} {}", license::machine_id(), if l.mid == license::machine_id() { "(khớp)" } else { "(KHÁC)" });
    Ok(())
}

/// `peerova relay [--port 8443] [--bind 0.0.0.0]`: standalone fallback relay for any
/// machine with a public IP. Put it behind nginx/Caddy for TLS, then point servers and
/// clients at it with `--relay https://relay.example.com` (or set RELAY_URL on the website).
fn cmd_relay(a: &Args) -> Result<(), String> {
    let addr = format!("{}:{}", a.opt("bind").unwrap_or("0.0.0.0"), a.num("port", 8443)?);
    peerova_core::relay::server::serve(
        &addr,
        |l| {
            println!("relay dự phòng đang chạy trên {l}");
            println!("  dùng: --relay http://<IP-hoặc-tên-miền>:{}   (đặt sau nginx/Caddy để có https)", l.port());
            println!("  dữ liệu đi qua đây vẫn mã hoá đầu-cuối; relay không giải mã được.");
        },
        |m| eprintln!("{m}"),
    )
    .map_err(|e| format!("không mở được {addr}: {e}"))
}

/// `peerova verify <file> [--sums SHA256SUMS.txt] [--sig SHA256SUMS.sig]`
fn cmd_verify(a: &Args) -> Result<(), String> {
    use peerova_core::release;
    let file = a.pos.get(1).ok_or("thiếu file cần kiểm tra")?;
    let dir = std::path::Path::new(file).parent().map(|d| d.to_path_buf()).unwrap_or_default();
    let sums_path = a.opt("sums").map(Into::into).unwrap_or_else(|| dir.join("SHA256SUMS.txt"));
    let sig_path = a.opt("sig").map(Into::into).unwrap_or_else(|| dir.join("SHA256SUMS.sig"));
    let pk = a.opt("release-pubkey").map(String::from).or(release::RELEASE_PUBKEY.map(String::from)).ok_or("bản build này không có khoá phát hành; dùng --release-pubkey")?;
    let sums = std::fs::read(&sums_path).map_err(|e| format!("đọc {}: {e}", sums_path.display()))?;
    let sig = std::fs::read_to_string(&sig_path).map_err(|e| format!("đọc {}: {e}", sig_path.display()))?;
    release::verify_sums(&sums, &sig, &license::parse_pubkey(&pk)?)?;
    let sums = String::from_utf8_lossy(&sums);
    let name = std::path::Path::new(file).file_name().and_then(|n| n.to_str()).unwrap_or(file);
    let want = release::expected_hash(&sums, name).ok_or_else(|| format!("{name} không có trong danh sách phát hành"))?;
    let got = release::sha256_hex(&std::fs::read(file).map_err(|e| format!("đọc {file}: {e}"))?);
    if got != want {
        return Err(format!("SHA-256 KHÔNG KHỚP — {name} đã bị sửa hoặc tải lỗi. Đừng chạy file này."));
    }
    println!("✔ {name}: chữ ký Peerova hợp lệ, SHA-256 khớp ({}…)", &got[..16]);
    Ok(())
}

const HELP: &str = "peerova — kết nối P2P trực tiếp cho mọi giao thức · mã hoá đầu-cuối · tự động đường dự phòng

  peerova doctor                         chẩn đoán mạng (IPv6, UPnP, NAT-PMP, CGNAT, double NAT)
  peerova verify <file.zip>               kiểm tra file tải về là bản chính thức (chữ ký + SHA-256)
  peerova keygen [--key server.key]      tạo danh tính server (ticket cố định theo key)
  peerova keygen --access [--key access.key]   tạo khoá cấp quyền doanh nghiệp (giữ ở backend/SSO)
  peerova token issue --access-key access.key --sub alice --ports tcp:443,udp:10000-10100 --ttl 8h

  peerova server [--config peerova.json] [--port 47000] [--key server.key]
              [--expose PROTO:PORT[-PORT][=HOST:PORT]]...
              [--license PVA-...] [--private] [--no-map] [--no-tcp] [--max-ccu N] [--ticket-file FILE]
              [--token SECRET | --access-key PUB [--revoked revoked.txt]] [--audit-log audit.jsonl]
              [--banned banned.txt]   cấm người chơi theo IP game thấy (127.x.x.x), tự cập nhật
              [--hide-ip]             giấu IP máy chủ: người chơi chỉ thấy máy chủ dự phòng (chống DDoS)
              [--workers N]           số nhân CPU cho engine (mặc định 0 = mọi nhân, tối đa 16)
              [--supernode HOST:PORT]... [--supernode-mode] [--no-auto-supernode]
              [--rendezvous URL | --no-rdv]   website làm điểm hẹn (MẶC ĐỊNH BẬT) → không cần mở port
      PROTO: tcp http https ws wss ssh rdp grpc (TCP) · udp quic game (UDP). UDP tới 64 KB.
      Server 'dark': không trả lời gói nào thiếu ticket hợp lệ, quét port không thấy.

  peerova client <TICKET|@file|MÃ> --forward PROTO:LOCAL[-LOCAL][=REMOTE]... [--bind IP]
              [--key client.key]   danh tính người chơi (mặc định tự lưu trong thư mục cấu hình người dùng)
              [--access-token TOKEN | --token SECRET] [--reconnect] [--tcp]
      Tự thử mọi đường: LAN, IPv6, IP public, UPnP, punch qua supernode (+ dự đoán port),
      rồi TCP đa luồng trực tiếp nếu mạng chặn UDP.
  peerova ping   <TICKET|@file> [--count 50] [--interval-ms 100] [--bench-mb 8] [--tcp]
  peerova stdio server ['<json>'] | stdio client <TICKET> ['<json>']
      SDK cho MỌI ngôn ngữ qua JSON lines stdin/stdout (Node, PHP, Ruby, Lua, Erlang, shell…)
  Mã kết nối 6 ký tự cho MỌI dịch vụ (web/HTTPS, SSH, RDP, database, game…):
    peerova server --app <project id> --share \"Tên dịch vụ\" --expose https:443 [--public] [--max N] [--mode M] [--region VN] [--tag T]...
    peerova client K7Q2XD --forward ...   (thay cho ticket dài; --site URL nếu không dùng website mặc định)
  peerova server ... --metrics 127.0.0.1:9184    endpoint Prometheus
  Chế độ VPN L3 (Linux/macOS: sudo · Windows: Administrator + wintun.dll cạnh peerova.exe):
    ping/ICMP, FTP active+passive, broadcast LAN, mọi giao thức IP
    sudo peerova server --vpn [--subnet 10.77.0.0/16] [--vpn-allow tcp:8888,udp:8888,icmp] [--banned banned.txt] ...    sudo peerova client <TICKET|MÃ> --vpn
  peerova ticket merge <T1> <T2>...   gộp ticket nhiều máy cùng server.key → client tự failover
  peerova lease-info <LEASE> [--authority-pubkey B64]

  Mọi tuỳ chọn đều đặt được trong --config peerova.json, ví dụ:
    {\"port\":47000,\"license\":\"PVA-...\",\"expose\":[\"https:443\",\"udp:10000-10100\"],
     \"access_key\":\"...\",\"revoked\":\"revoked.txt\",\"audit_log\":\"audit.jsonl\",\"private\":true}";

fn main() -> ExitCode {
    START.get_or_init(Instant::now);
    let a = match Args::parse(std::env::args().skip(1)).with_config() {
        Ok(a) => a,
        Err(e) => {
            eprintln!("lỗi config: {e}");
            return ExitCode::FAILURE;
        }
    };
    if a.flag("help") || a.pos.first().map(String::as_str) == Some("help") {
        println!("{HELP}");
        return ExitCode::SUCCESS;
    }
    let _ = SITE.set(
        a.opt("site")
            .or(a.opt("rendezvous"))
            .or(a.opt("license-server"))
            .unwrap_or(license::DEFAULT_LICENSE_SERVER)
            .to_string(),
    );
    let res = match a.pos.first().map(String::as_str) {
        Some("doctor") => cmd_doctor(),
        Some("keygen") => cmd_keygen(&a),
        Some("server") => cmd_server(&a),
        Some("client") => cmd_client(&a),
        Some("ping") => cmd_ping(&a),
        Some("lease-info") => cmd_lease_info(&a),
        Some("verify") => cmd_verify(&a),
        Some("relay") => cmd_relay(&a),
        Some("token") => cmd_token(&a),
        Some("ticket") => cmd_ticket(&a),
        Some("stdio") => cmd_stdio(&a),
        _ => {
            println!("{HELP}");
            Ok(())
        }
    };
    match res {
        Ok(()) => ExitCode::SUCCESS,
        Err(e) => {
            eprintln!("lỗi: {e}");
            ExitCode::FAILURE
        }
    }
}
