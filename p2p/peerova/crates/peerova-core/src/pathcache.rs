//! What earlier connections taught us, so the next one goes straight to the technique that
//! works: per server key the last direct address and the server's NAT behaviour, per local
//! network our own NAT behaviour. Process-wide in memory; also persisted to a small text file
//! when the application gives a location (`ClientConfig::cache_file`) — the library never
//! writes files on its own. Entries expire after `TTL`.

use std::collections::HashMap;
use std::net::{IpAddr, SocketAddr};
use std::path::{Path, PathBuf};
use std::sync::Mutex;
use std::time::Duration;

/// Forget what we learned after a day (networks and NAT boxes change).
pub const TTL: Duration = Duration::from_secs(24 * 3600);
const MAX_ENTRIES: usize = 256;

/// How a NAT allocates the external port of a new flow.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Nat {
    /// Keeps the local port (or one mapping for all destinations): plain punching works.
    Easy,
    /// New port per destination, close to the previous one: port prediction helps.
    Seq,
    /// New random port per destination: birthday techniques from the start.
    Hard,
}

impl Nat {
    pub fn as_str(self) -> &'static str {
        match self {
            Nat::Easy => "easy",
            Nat::Seq => "seq",
            Nat::Hard => "hard",
        }
    }
    pub fn parse(s: &str) -> Option<Nat> {
        match s {
            "easy" => Some(Nat::Easy),
            "seq" => Some(Nat::Seq),
            "hard" => Some(Nat::Hard),
            _ => None,
        }
    }
    /// Class from the external port a peer saw vs the port we expected (local / announced).
    pub fn from_ports(expected: u16, seen: u16) -> Nat {
        match seen.abs_diff(expected) {
            0 => Nat::Easy,
            d if d <= 32 => Nat::Seq,
            _ => Nat::Hard,
        }
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct ServerEntry {
    pub addr: Option<SocketAddr>,
    pub nat: Option<Nat>,
    pub ts: u64,
}

#[derive(Default)]
struct Cache {
    servers: HashMap<[u8; 32], ServerEntry>,
    nets: HashMap<String, (Nat, u64)>,
    loaded: Vec<PathBuf>,
}

fn cache() -> &'static Mutex<Cache> {
    static C: std::sync::OnceLock<Mutex<Cache>> = std::sync::OnceLock::new();
    C.get_or_init(|| Mutex::new(Cache::default()))
}

fn unhex(s: &str) -> Option<Vec<u8>> {
    if s.len() % 2 != 0 || !s.is_ascii() {
        return None;
    }
    (0..s.len()).step_by(2).map(|i| u8::from_str_radix(&s[i..i + 2], 16).ok()).collect()
}

fn now() -> u64 {
    crate::license::now_unix()
}

fn fresh(ts: u64, now: u64) -> bool {
    ts <= now + 60 && now.saturating_sub(ts) < TTL.as_secs()
}

/// Our network's identity: the local /24 (home LAN) — the public IP is checked separately
/// by the caller when it becomes known. `None` when we have no IPv4.
pub fn net_key(local: Option<std::net::Ipv4Addr>) -> Option<String> {
    let o = local?.octets();
    Some(format!("{}.{}.{}.0/24", o[0], o[1], o[2]))
}

fn load(c: &mut Cache, file: &Path) {
    if c.loaded.iter().any(|p| p == file) {
        return;
    }
    c.loaded.push(file.to_path_buf());
    let Ok(text) = std::fs::read_to_string(file) else { return };
    let now = now();
    for line in text.lines().take(4 * MAX_ENTRIES) {
        let f: Vec<&str> = line.split_whitespace().collect();
        match f.as_slice() {
            ["s", key, addr, nat, ts] => {
                let (Some(k), Ok(ts)) = (unhex(key).filter(|k| k.len() == 32), ts.parse::<u64>()) else { continue };
                if !fresh(ts, now) {
                    continue;
                }
                let mut key = [0u8; 32];
                key.copy_from_slice(&k);
                let e = ServerEntry { addr: addr.parse().ok(), nat: Nat::parse(nat), ts };
                c.servers.entry(key).and_modify(|x| if x.ts < ts { *x = e }).or_insert(e);
            }
            ["n", net, nat, ts] => {
                let (Some(nat), Ok(ts)) = (Nat::parse(nat), ts.parse::<u64>()) else { continue };
                if fresh(ts, now) && net.len() < 64 {
                    c.nets.entry(net.to_string()).and_modify(|x| if x.1 < ts { *x = (nat, ts) }).or_insert((nat, ts));
                }
            }
            _ => {}
        }
    }
}

fn save(c: &mut Cache, file: &Path) {
    let now = now();
    c.servers.retain(|_, e| fresh(e.ts, now));
    c.nets.retain(|_, e| fresh(e.1, now));
    let mut out = String::from("# peerova path cache (safe to delete)\n");
    let mut servers: Vec<_> = c.servers.iter().collect();
    servers.sort_by_key(|(_, e)| std::cmp::Reverse(e.ts));
    for (k, e) in servers.into_iter().take(MAX_ENTRIES) {
        let addr = e.addr.map_or("-".into(), |a| a.to_string());
        out += &format!("s {} {addr} {} {}\n", crate::util::hex(k), e.nat.map_or("-", Nat::as_str), e.ts);
    }
    for (n, (nat, ts)) in c.nets.iter().take(MAX_ENTRIES) {
        out += &format!("n {n} {} {ts}\n", nat.as_str());
    }
    let tmp = file.with_extension("tmp");
    if std::fs::write(&tmp, out).is_ok() {
        let _ = std::fs::rename(&tmp, file);
    }
}

fn with<R>(file: Option<&Path>, f: impl FnOnce(&mut Cache) -> R) -> R {
    let mut c = cache().lock().unwrap_or_else(|e| e.into_inner());
    if let Some(p) = file {
        load(&mut c, p);
    }
    f(&mut c)
}

/// What we know about this server (fresh entries only).
pub fn server(file: Option<&Path>, key: &[u8; 32]) -> Option<ServerEntry> {
    let now = now();
    with(file, |c| c.servers.get(key).copied().filter(|e| fresh(e.ts, now)))
}

/// Remember a working direct address and/or the server's NAT class.
pub fn remember_server(file: Option<&Path>, key: &[u8; 32], addr: Option<SocketAddr>, nat: Option<Nat>) {
    with(file, |c| {
        if c.servers.len() > 4 * MAX_ENTRIES {
            c.servers.clear();
        }
        let e = c.servers.entry(*key).or_insert(ServerEntry { addr: None, nat: None, ts: 0 });
        e.addr = addr.or(e.addr);
        e.nat = nat.or(e.nat);
        e.ts = now();
        if let Some(p) = file {
            save(c, p);
        }
    })
}

/// Our own NAT class on this network, if learned recently.
pub fn own_nat(file: Option<&Path>, net: &str) -> Option<Nat> {
    let now = now();
    with(file, |c| c.nets.get(net).filter(|e| fresh(e.1, now)).map(|e| e.0))
}

pub fn remember_own_nat(file: Option<&Path>, net: &str, nat: Nat) {
    with(file, |c| {
        if c.nets.len() > 4 * MAX_ENTRIES {
            c.nets.clear();
        }
        c.nets.insert(net.to_string(), (nat, now()));
        if let Some(p) = file {
            save(c, p);
        }
    })
}

/// Only addresses we could have reached directly are worth caching.
pub fn cacheable(a: &SocketAddr) -> bool {
    !crate::relay::is_relay(a) && !matches!(a.ip(), IpAddr::V4(v) if v.is_unspecified())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn classes_and_persistence() {
        assert_eq!(Nat::from_ports(47000, 47000), Nat::Easy);
        assert_eq!(Nat::from_ports(47000, 47003), Nat::Seq);
        assert_eq!(Nat::from_ports(47000, 1200), Nat::Hard);
        let dir = std::env::temp_dir().join(format!("peerova-pc-{:x}", crate::util::random_u64()));
        std::fs::create_dir_all(&dir).unwrap();
        let file = dir.join("paths.cache");
        let key = [7u8; 32];
        let a: SocketAddr = "203.0.113.5:47000".parse().unwrap();
        remember_server(Some(&file), &key, Some(a), Some(Nat::Hard));
        remember_own_nat(Some(&file), "10.9.8.0/24", Nat::Seq);
        // A fresh process (simulated: clear memory) reloads the file.
        {
            let mut c = cache().lock().unwrap();
            c.servers.remove(&key);
            c.nets.remove("10.9.8.0/24");
            c.loaded.retain(|p| p != &file);
        }
        let e = server(Some(&file), &key).unwrap();
        assert_eq!((e.addr, e.nat), (Some(a), Some(Nat::Hard)));
        assert_eq!(own_nat(Some(&file), "10.9.8.0/24"), Some(Nat::Seq));
        // Expired lines are ignored; garbage is skipped.
        let old = now() - TTL.as_secs() - 10;
        std::fs::write(&file, format!("s {} 1.2.3.4:5 easy {old}\ngarbage\nn 10.1.1.0/24 hard {old}\n", crate::util::hex(&[9u8; 32]))).unwrap();
        cache().lock().unwrap().loaded.retain(|p| p != &file);
        assert!(server(Some(&file), &[9u8; 32]).is_none());
        assert_eq!(own_nat(Some(&file), "10.1.1.0/24"), None);
        assert_eq!(net_key(Some("192.168.1.23".parse().unwrap())).as_deref(), Some("192.168.1.0/24"));
        let _ = std::fs::remove_dir_all(&dir);
    }
}
