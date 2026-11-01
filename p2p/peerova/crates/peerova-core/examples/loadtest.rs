//! Load test: one server process, many simulated players.
//!   cargo run --release --example loadtest -- server <ticket-file> <seconds>
//!   cargo run --release --example loadtest -- client <ticket-file> <players> <seconds>
//!   cargo run --release --example loadtest -- swarm <ticket-file> <players> <seconds> [threads]
//! `swarm` is a lightweight client: the same protocol (Noise handshake, sealed packets,
//! `Conn` state machine) but many players multiplexed on a few sockets/threads, so one
//! machine can generate far more load than with one full client engine per player.
//! Server worker threads: `PEEROVA_WORKERS=<n>` (default 0 = one per CPU core).
//! Each player sends a 64-byte "game state" datagram at 20 Hz and a reliable message
//! every second; the server echoes everything. Clients report connect time, RTT and loss.

use std::collections::HashMap;
use std::time::{Duration, Instant};

use peerova_core::crypto::Keypair;
use peerova_core::engine::{ClientConfig, Cmd, Engine, EngineEvent, ServerConfig};

fn rss_mb() -> u64 {
    std::fs::read_to_string("/proc/self/status")
        .ok()
        .and_then(|s| s.lines().find(|l| l.starts_with("VmRSS")).and_then(|l| l.split_whitespace().nth(1)?.parse::<u64>().ok()))
        .unwrap_or(0)
        / 1024
}

fn cpu_secs() -> f64 {
    let s = std::fs::read_to_string("/proc/self/stat").unwrap_or_default();
    let f: Vec<&str> = s.rsplit(')').next().unwrap_or("").split_whitespace().collect();
    let t = |i: usize| f.get(i).and_then(|v| v.parse::<f64>().ok()).unwrap_or(0.0);
    (t(11) + t(12)) / 100.0
}

fn server(ticket_file: &str, secs: u64) {
    let mut cfg = ServerConfig::new(Keypair::generate());
    cfg.port = 0;
    cfg.try_map = false;
    cfg.max_ccu = 1_000_000;
    cfg.auto_supernode = false;
    cfg.workers = std::env::var("PEEROVA_WORKERS").ok().and_then(|v| v.parse().ok()).unwrap_or(0);
    let s = Engine::start_server(cfg).expect("server");
    eprintln!("[server] {} worker", s.workers());
    let t0 = Instant::now();
    let (mut ccu, mut peak, mut dgrams, mut msgs) = (0usize, 0usize, 0u64, 0u64);
    let mut last = Instant::now();
    let c0 = cpu_secs();
    let (mut last_cpu, mut last_dgrams, mut best) = (c0, 0u64, 0f64);
    while t0.elapsed() < Duration::from_secs(secs) {
        let Some(ev) = s.recv_timeout(Duration::from_millis(200)) else { continue };
        match ev {
            EngineEvent::Ready { ticket: Some(t), .. } => std::fs::write(ticket_file, t).unwrap(),
            EngineEvent::Connected { .. } => {
                ccu += 1;
                peak = peak.max(ccu);
            }
            EngineEvent::Disconnected { .. } => ccu = ccu.saturating_sub(1),
            EngineEvent::Datagram { conn, data } => {
                dgrams += 1;
                s.handle.send(Cmd::Datagram { conn, data });
            }
            EngineEvent::StreamOpened { .. } => {}
            EngineEvent::StreamData { conn, id, data } => {
                msgs += 1;
                s.handle.send(Cmd::StreamConsumed { conn, id, n: data.len() });
                s.handle.send(Cmd::StreamWrite { conn, id, data });
            }
            _ => {}
        }
        if last.elapsed() >= Duration::from_secs(5) {
            let win = last.elapsed().as_secs_f64();
            last = Instant::now();
            let el = t0.elapsed().as_secs_f64();
            let cpu = cpu_secs();
            let rate = (dgrams - last_dgrams) as f64 / win;
            best = best.max(rate);
            eprintln!(
                "[server {el:5.0}s] online {ccu} (đỉnh {peak}) · {dgrams} gói nhanh ({rate:.0}/s echo) · {msgs} tin tin cậy · CPU {:.0}% (5 s qua) · RAM {} MB",
                (cpu - last_cpu) / win * 100.0,
                rss_mb()
            );
            (last_cpu, last_dgrams) = (cpu, dgrams);
        }
    }
    println!("SERVER đỉnh {peak} người chơi · {dgrams} gói nhanh + {msgs} tin tin cậy đã xử lý · đỉnh {best:.0} gói/s echo · CPU trung bình {:.0}% của 1 nhân · RAM {} MB", (cpu_secs() - c0) / secs as f64 * 100.0, rss_mb());
}

fn client(ticket_file: &str, n: usize, secs: u64) {
    let ticket = std::fs::read_to_string(ticket_file).expect("ticket").trim().to_string();
    let t0 = Instant::now();
    // Ramp up: 50 players every 100 ms (a realistic flood of joins).
    let mut engines: Vec<(Engine, Option<u32>, Instant, Option<f64>)> = vec![];
    for i in 0..n {
        let cfg = ClientConfig { rendezvous: None, no_relay: true, timeout: Duration::from_secs(30), ..Default::default() };
        engines.push((Engine::connect(&ticket, cfg).expect("client"), None, Instant::now(), None));
        if i % 50 == 49 {
            std::thread::sleep(Duration::from_millis(100));
        }
    }
    let mut rtts: Vec<f64> = vec![];
    let (mut sent, mut got, mut rel_sent, mut rel_got, mut failed) = (0u64, 0u64, 0u64, 0u64, 0usize);
    let mut next_tick = Instant::now();
    let mut tick = 0u64;
    let mut streams: HashMap<usize, u32> = HashMap::new();
    while t0.elapsed() < Duration::from_secs(secs) {
        for (idx, (e, conn, started, conn_time)) in engines.iter_mut().enumerate() {
            while let Some(ev) = e.try_recv() {
                match ev {
                    EngineEvent::Connected { conn: c, .. } => {
                        *conn = Some(c);
                        *conn_time = Some(started.elapsed().as_secs_f64());
                    }
                    EngineEvent::ConnectFailed(_) => failed += 1,
                    EngineEvent::Datagram { data, .. } if data.len() >= 16 => {
                        let ts = u128::from_le_bytes(data[..16].try_into().unwrap());
                        let now = t0.elapsed().as_micros();
                        rtts.push((now.saturating_sub(ts)) as f64 / 1000.0);
                        got += 1;
                    }
                    EngineEvent::StreamData { data, .. } => rel_got += data.len() as u64 / 32,
                    EngineEvent::StreamOpened { conn: c, id } => {
                        let _ = (c, id);
                    }
                    _ => {}
                }
            }
            let _ = idx;
        }
        if Instant::now() >= next_tick {
            next_tick += Duration::from_millis(50);
            tick += 1;
            for (idx, (e, conn, _, _)) in engines.iter().enumerate() {
                let Some(c) = *conn else { continue };
                let mut d = t0.elapsed().as_micros().to_le_bytes().to_vec();
                d.resize(64, 7);
                e.handle.send(Cmd::Datagram { conn: c, data: d });
                sent += 1;
                if tick % 20 == (idx as u64 % 20) {
                    let id = *streams.entry(idx).or_insert_with(|| { let _ = c; e.handle.alloc_stream() });
                    e.handle.send(Cmd::StreamWrite { conn: c, id, data: vec![1u8; 32] });
                    rel_sent += 1;
                }
            }
        }
        std::thread::sleep(Duration::from_millis(2));
    }
    let connected = engines.iter().filter(|e| e.1.is_some()).count();
    let mut ct: Vec<f64> = engines.iter().filter_map(|e| e.3).collect();
    ct.sort_by(|a, b| a.partial_cmp(b).unwrap());
    rtts.sort_by(|a, b| a.partial_cmp(b).unwrap());
    let p = |v: &Vec<f64>, q: f64| if v.is_empty() { 0.0 } else { v[((v.len() - 1) as f64 * q) as usize] };
    println!(
        "CLIENT {connected}/{n} vào được (thất bại {failed}) · thời gian vào p50 {:.2}s p99 {:.2}s · gói nhanh {got}/{sent} ({:.2}% về) · RTT p50 {:.1} ms p99 {:.1} ms · tin tin cậy {rel_got}/{rel_sent}",
        p(&ct, 0.5),
        p(&ct, 0.99),
        if sent > 0 { got as f64 * 100.0 / sent as f64 } else { 0.0 },
        p(&rtts, 0.5),
        p(&rtts, 0.99)
    );
}

/// Many players share one swarm socket: give it a big receive buffer (Linux).
#[cfg(target_os = "linux")]
fn grow_buffers(s: &std::net::UdpSocket) {
    use std::os::fd::AsRawFd;
    extern "C" {
        fn setsockopt(fd: i32, level: i32, name: i32, val: *const std::ffi::c_void, len: u32) -> i32;
    }
    let v: i32 = 8 << 20;
    for name in [7, 8] {
        // SAFETY: valid fd, pointer to a live i32.
        unsafe { setsockopt(s.as_raw_fd(), 1, name, &v as *const i32 as *const _, 4) };
    }
}
#[cfg(not(target_os = "linux"))]
fn grow_buffers(_: &std::net::UdpSocket) {}

/// One simulated player of the swarm.
struct Bot {
    init: Option<peerova_core::crypto::Initiator>,
    sess: Option<peerova_core::crypto::Session>,
    conn: Option<peerova_core::conn::Conn>,
    stream: u32,
    started: Instant,
    next_init: Instant,
}

#[derive(Default)]
struct SwarmStats {
    connected: usize,
    sent: u64,
    got: u64,
    rel_sent: u64,
    rel_got: u64,
    rtts: Vec<f64>,
    conn_times: Vec<f64>,
}

fn swarm_thread(server: std::net::SocketAddr, spub: [u8; 32], n: usize, base_idx: u32, secs: u64, t0: Instant) -> SwarmStats {
    use peerova_core::conn::{Conn, ConnEvent};
    use peerova_core::crypto::{self, Session};
    let sock = std::net::UdpSocket::bind("0.0.0.0:0").unwrap();
    grow_buffers(&sock);
    sock.set_read_timeout(Some(Duration::from_millis(1))).unwrap();
    let mut bots: Vec<Bot> = vec![];
    let mut st = SwarmStats::default();
    let mut buf = vec![0u8; 65536];
    let mut next_tick = Instant::now();
    let mut next_timers = Instant::now();
    let mut tick = 0u64;
    let mut dirty: Vec<usize> = vec![];
    let flush = |b: &mut Bot, sock: &std::net::UdpSocket, now: Instant| {
        if let (Some(c), Some(s)) = (b.conn.as_mut(), b.sess.as_ref()) {
            while let Some((pn, pt)) = c.poll_transmit(now) {
                let _ = sock.send_to(&s.seal(pn, &pt), server);
            }
        }
    };
    while t0.elapsed() < Duration::from_secs(secs) {
        let now = Instant::now();
        // Ramp up like the full client: 50 players per 100 ms (per process).
        while bots.len() < n && bots.len() < ((now - t0).as_millis() as usize / 100 + 1) * 50 {
            let key = crypto::Keypair::generate();
            let idx = base_idx + bots.len() as u32;
            let payload = (std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_nanos() as u64).to_be_bytes();
            let init = crypto::initiate(&key, &spub, idx, &payload);
            bots.push(Bot { init: Some(init), sess: None, conn: None, stream: 0, started: now, next_init: now });
        }
        for b in bots.iter_mut() {
            if let Some(i) = b.init.as_ref().filter(|_| now >= b.next_init) {
                let _ = sock.send_to(&i.packet, server);
                b.next_init = now + Duration::from_millis(500);
            }
        }
        // Receive everything that is waiting.
        while let Ok((len, _)) = sock.recv_from(&mut buf) {
            let d = &buf[..len];
            let now = Instant::now();
            match d.first() {
                Some(&crypto::T_RESP) if len >= 9 => {
                    let i = u32::from_le_bytes(d[5..9].try_into().unwrap()).wrapping_sub(base_idx) as usize;
                    let Some(b) = bots.get_mut(i) else { continue };
                    let Some(sess) = b.init.as_ref().and_then(|x| x.finish(d)) else { continue };
                    b.init = None;
                    b.sess = Some(sess);
                    let mut c = Conn::new(true, now);
                    c.ping();
                    b.stream = c.open_stream();
                    b.conn = Some(c);
                    st.connected += 1;
                    st.conn_times.push(b.started.elapsed().as_secs_f64());
                    dirty.push(i);
                }
                Some(&crypto::T_DATA) => {
                    let Some(i) = Session::packet_index(d).map(|x| x.wrapping_sub(base_idx) as usize) else { continue };
                    let Some(b) = bots.get_mut(i) else { continue };
                    let (Some(s), Some(c)) = (b.sess.as_mut(), b.conn.as_mut()) else { continue };
                    let Some((pn, pt)) = s.open(d) else { continue };
                    c.on_packet(pn, &pt, now);
                    while let Some(e) = c.poll_event() {
                        match e {
                            ConnEvent::Datagram(data) if data.len() >= 16 => {
                                let ts = u128::from_le_bytes(data[..16].try_into().unwrap());
                                st.rtts.push(t0.elapsed().as_micros().saturating_sub(ts) as f64 / 1000.0);
                                st.got += 1;
                            }
                            ConnEvent::StreamData(id, data) => {
                                c.stream_consumed(id, data.len());
                                st.rel_got += data.len() as u64 / 32;
                            }
                            _ => {}
                        }
                    }
                    dirty.push(i);
                }
                _ => {}
            }
            if dirty.len() >= 64 {
                for i in dirty.drain(..) {
                    flush(&mut bots[i], &sock, now);
                }
            }
        }
        let now = Instant::now();
        if now >= next_tick {
            next_tick += Duration::from_millis(50);
            tick += 1;
            for (i, b) in bots.iter_mut().enumerate() {
                let Some(c) = b.conn.as_mut() else { continue };
                let mut d = t0.elapsed().as_micros().to_le_bytes().to_vec();
                d.resize(64, 7);
                c.send_datagram(d);
                st.sent += 1;
                if tick % 20 == (i as u64 % 20) {
                    c.stream_write(b.stream, &[1u8; 32]);
                    st.rel_sent += 1;
                }
                dirty.push(i);
            }
        }
        if now >= next_timers {
            next_timers = now + Duration::from_millis(5);
            for (i, b) in bots.iter_mut().enumerate() {
                if let Some(c) = b.conn.as_mut() {
                    if c.poll_timeout().is_some_and(|t| now >= t) {
                        c.on_timeout(now);
                        dirty.push(i);
                    }
                }
            }
        }
        dirty.sort_unstable();
        dirty.dedup();
        for i in dirty.drain(..) {
            flush(&mut bots[i], &sock, now);
        }
    }
    st
}

fn swarm(ticket_file: &str, n: usize, secs: u64, threads: usize) {
    let ticket = std::fs::read_to_string(ticket_file).expect("ticket").trim().to_string();
    let t = peerova_core::Ticket::decode(&ticket).expect("ticket");
    // Loopback: same machine, and exempt from the per-IP handshake budget (one test IP).
    let port = t.candidates.iter().find(|c| c.addr.is_ipv4()).expect("ipv4 candidate").addr.port();
    let server = std::net::SocketAddr::from(([127, 0, 0, 1], port));
    let t0 = Instant::now();
    let per = n.div_ceil(threads);
    let base = peerova_core::crypto::random_u32() & 0x7fff_0000;
    let results: Vec<SwarmStats> = std::thread::scope(|sc| {
        let hs: Vec<_> = (0..threads).map(|k| sc.spawn(move || swarm_thread(server, t.server_pub, per.min(n - (per * k).min(n)), base + (k as u32) * 0x10000, secs, t0))).collect();
        hs.into_iter().map(|h| h.join().unwrap()).collect()
    });
    let mut all = SwarmStats::default();
    for r in results {
        all.connected += r.connected;
        all.sent += r.sent;
        all.got += r.got;
        all.rel_sent += r.rel_sent;
        all.rel_got += r.rel_got;
        all.rtts.extend(r.rtts);
        all.conn_times.extend(r.conn_times);
    }
    all.rtts.sort_by(|a, b| a.partial_cmp(b).unwrap());
    all.conn_times.sort_by(|a, b| a.partial_cmp(b).unwrap());
    let p = |v: &Vec<f64>, q: f64| if v.is_empty() { 0.0 } else { v[((v.len() - 1) as f64 * q) as usize] };
    println!(
        "SWARM {}/{n} vào được · thời gian vào p50 {:.2}s p99 {:.2}s · gói nhanh {}/{} ({:.2}% về) · RTT p50 {:.1} ms p99 {:.1} ms · tin tin cậy {}/{}",
        all.connected,
        p(&all.conn_times, 0.5),
        p(&all.conn_times, 0.99),
        all.got,
        all.sent,
        if all.sent > 0 { all.got as f64 * 100.0 / all.sent as f64 } else { 0.0 },
        p(&all.rtts, 0.5),
        p(&all.rtts, 0.99),
        all.rel_got,
        all.rel_sent
    );
}

fn main() {
    let a: Vec<String> = std::env::args().collect();
    match a.get(1).map(String::as_str) {
        Some("server") => server(&a[2], a[3].parse().unwrap()),
        Some("client") => client(&a[2], a[3].parse().unwrap(), a[4].parse().unwrap()),
        Some("swarm") => swarm(&a[2], a[3].parse().unwrap(), a[4].parse().unwrap(), a.get(5).and_then(|v| v.parse().ok()).unwrap_or(4)),
        _ => eprintln!("usage: loadtest server <ticket-file> <secs> | client <ticket-file> <players> <secs> | swarm <ticket-file> <players> <secs> [threads]"),
    }
}
