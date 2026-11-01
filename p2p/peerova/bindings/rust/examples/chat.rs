//! How to run (from bindings/rust):
//!
//!   cargo run --release --example chat -- server [port] [key_file]
//!   cargo run --release --example chat -- client <TICKET or CONNECT CODE>
//!
//! Tiny chat + position-sync demo. The server prints its ticket, forwards chat lines (reliable, channel 0)
//! and positions (unreliable, channel 1) to the other clients. The client sends its position 20 times per
//! second and every stdin line as chat; "/quit" or end of input disconnects cleanly. Stop the server with
//! Ctrl+C (the process exit frees the node).
use std::collections::HashSet;
use std::io::BufRead;
use std::sync::mpsc;
use std::time::{Duration, Instant};

use peerova::{Sdk, EV_CONNECTED, EV_CONNECT_FAILED, EV_DISCONNECTED, EV_MESSAGE};

/// Reliable: ordered, never lost.
const CH_CHAT: u8 = 0;
/// Unreliable: latest state wins, may be dropped.
const CH_POSITION: u8 = 1;

/// Runs a server until the process is stopped, forwarding chat and positions between clients.
fn server(port: u16, key_file: &str) -> Result<(), String> {
    let cfg = format!(r#"{{"port":{port},"key_file":{key_file:?}}}"#);
    let mut srv = Sdk::server(Some(&cfg))?;
    println!("ticket: {}", srv.ticket().unwrap_or_default());
    let mut peers = HashSet::new();
    let (mut code_shown, mut positions) = (false, 0u64);
    loop {
        if !code_shown {
            // Only with "app" + "share" in the config.
            if let Some(code) = srv.room_code() {
                println!("connect code: {code}");
                code_shown = true;
            }
        }
        let Some(ev) = srv.poll_wait(Duration::from_millis(100)) else { continue };
        match ev.kind {
            EV_CONNECTED => {
                peers.insert(ev.peer);
                println!("peer {} joined ({} online)", ev.peer, srv.peer_count());
            }
            EV_DISCONNECTED => {
                peers.remove(&ev.peer);
                println!("peer {} left: {}", ev.peer, String::from_utf8_lossy(&ev.data));
            }
            EV_MESSAGE if ev.channel == CH_CHAT => {
                let line = format!("peer {}: {}", ev.peer, String::from_utf8_lossy(&ev.data));
                println!("{line}");
                for &p in peers.iter().filter(|&&p| p != ev.peer) {
                    let _ = srv.send(p, CH_CHAT, line.as_bytes(), true);
                }
            }
            EV_MESSAGE if ev.channel == CH_POSITION => {
                positions += 1;
                if positions % 20 == 0 && ev.data.len() == 8 {
                    let x = f32::from_be_bytes(ev.data[0..4].try_into().unwrap());
                    let y = f32::from_be_bytes(ev.data[4..8].try_into().unwrap());
                    println!("peer {} position {x:.1},{y:.1} ({positions} updates)", ev.peer);
                }
                for &p in peers.iter().filter(|&&p| p != ev.peer) {
                    let _ = srv.send(p, CH_POSITION, &ev.data, false);
                }
            }
            _ => {}
        }
    }
}

/// Connects with a ticket or connect code, then sends positions at ~20 Hz and stdin lines as chat.
fn client(ticket_or_code: &str) -> Result<(), String> {
    let mut cli = Sdk::client(ticket_or_code, Some(r#"{"timeout_ms":20000}"#))?;
    let server = loop {
        match cli.poll_wait(Duration::from_millis(500)) {
            Some(ev) if ev.kind == EV_CONNECTED => break ev.peer,
            Some(ev) if ev.kind == EV_CONNECT_FAILED => {
                return Err(format!("connect failed: {}", String::from_utf8_lossy(&ev.data)))
            }
            _ => {}
        }
    };
    println!("connected, type chat lines (/quit to leave)");

    // stdin on its own thread; it only hands lines over a channel and never touches the Sdk.
    let (tx, typed) = mpsc::channel::<String>();
    std::thread::spawn(move || {
        for line in std::io::stdin().lock().lines().map_while(Result::ok) {
            if tx.send(line).is_err() {
                return;
            }
        }
        let _ = tx.send("/quit".into());
    });

    let start = Instant::now();
    let (mut next_pos, mut sent) = (Instant::now(), 0u64);
    'run: loop {
        if Instant::now() >= next_pos {
            let t = start.elapsed().as_secs_f32();
            let mut pos = [0u8; 8];
            pos[..4].copy_from_slice(&(t.cos() * 10.0).to_be_bytes());
            pos[4..].copy_from_slice(&(t.sin() * 10.0).to_be_bytes());
            if cli.send(server, CH_POSITION, &pos, false).is_ok() {
                sent += 1;
            }
            next_pos += Duration::from_millis(50);
        }
        while let Ok(line) = typed.try_recv() {
            if line == "/quit" {
                break 'run;
            }
            cli.send(server, CH_CHAT, line.as_bytes(), true)?;
        }
        if let Some(ev) = cli.poll_wait(Duration::from_millis(10)) {
            match ev.kind {
                EV_MESSAGE if ev.channel == CH_CHAT => println!("{}", String::from_utf8_lossy(&ev.data)),
                EV_DISCONNECTED => {
                    println!("server closed: {}", String::from_utf8_lossy(&ev.data));
                    break 'run;
                }
                _ => {}
            }
        }
    }
    // Give queued reliable messages a moment to arrive, then disconnect; dropping `cli` frees the node.
    let flush_until = Instant::now() + Duration::from_millis(300);
    while Instant::now() < flush_until {
        let _ = cli.poll_wait(Duration::from_millis(50));
    }
    cli.disconnect(server);
    println!("bye ({sent} position updates sent)");
    Ok(())
}

fn main() {
    let args: Vec<String> = std::env::args().skip(1).collect();
    let r = match args.first().map(String::as_str) {
        Some("server") => server(
            args.get(1).and_then(|p| p.parse().ok()).unwrap_or(47000),
            args.get(2).map(String::as_str).unwrap_or("server.key"),
        ),
        Some("client") if args.len() >= 2 => client(&args[1]),
        _ => Err("usage: chat server [port] [key_file] | chat client <TICKET or CODE>".into()),
    };
    if let Err(e) = r {
        eprintln!("error: {e}");
        std::process::exit(1);
    }
}
