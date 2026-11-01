//! Direct use of the `peerova` SDK crate from Rust — no C ABI, no unsafe.
//!
//!   cargo run --release                           # in-process demo: server + client, echo
//!   cargo run --release -- server [port]          # print a ticket, echo every message back
//!   cargo run --release -- client <TICKET|CODE>   # send stdin lines, print replies
//!
//! See examples/chat.rs for a chat + position-sync demo.
use std::io::BufRead;
use std::time::Duration;

use peerova::{Sdk, SdkEvent, EV_CONNECTED, EV_CONNECT_FAILED, EV_DISCONNECTED, EV_MESSAGE};

/// One-line, human-readable description of an event.
fn describe(ev: &SdkEvent) -> String {
    let text = String::from_utf8_lossy(&ev.data);
    match ev.kind {
        EV_CONNECTED => format!("peer {} connected", ev.peer),
        EV_DISCONNECTED => format!("peer {} disconnected: {text}", ev.peer),
        EV_CONNECT_FAILED => format!("connect failed: {text}"),
        EV_MESSAGE => format!("peer {} ch{} {}: {text}", ev.peer, ev.channel, if ev.reliable { "reliable" } else { "unreliable" }),
        k => format!("event {k}"),
    }
}

/// Echo server: prints its ticket, then sends every message back on the same channel and mode.
fn server(port: u16) -> Result<(), String> {
    let mut srv = Sdk::server(Some(&format!(r#"{{"port":{port},"key_file":"server.key"}}"#)))?;
    println!("{}", srv.ticket().unwrap_or_default());
    loop {
        if let Some(ev) = srv.poll_wait(Duration::from_secs(1)) {
            eprintln!("{}", describe(&ev));
            if ev.kind == EV_MESSAGE {
                srv.send(ev.peer, ev.channel, &ev.data, ev.reliable)?; // echo
            }
        }
    }
}

/// Interactive client: `ticket` may be a ticket or a 6-character connect code.
fn client(ticket: &str) -> Result<(), String> {
    let mut cli = Sdk::client(ticket, None)?;
    let peer = match cli.poll_wait(Duration::from_secs(20)) {
        Some(ev) if ev.kind == EV_CONNECTED => ev.peer,
        Some(ev) => return Err(describe(&ev)),
        None => return Err("timeout".into()),
    };
    eprintln!("connected — type lines");
    for line in std::io::stdin().lock().lines() {
        cli.send(peer, 0, line.map_err(|e| e.to_string())?.as_bytes(), true)?;
        if let Some(ev) = cli.poll_wait(Duration::from_secs(5)) {
            println!("{}", describe(&ev));
        }
    }
    Ok(())
}

/// Server and client in one process: connect, send one message, receive the reply.
fn demo() -> Result<(), String> {
    let key = std::env::temp_dir().join(format!("peerova-rs-demo-{}.key", std::process::id()));
    let cfg = format!(r#"{{"port":0,"upnp":false,"key_file":{:?}}}"#, key.to_string_lossy());
    let mut srv = Sdk::server(Some(&cfg))?;
    let ticket = srv.ticket().ok_or("no ticket")?.to_string();
    println!("ticket: {ticket}");
    let mut cli = Sdk::client(&ticket, None)?;
    let ev = cli.poll_wait(Duration::from_secs(10)).ok_or("connect timeout")?;
    println!("client: {}", describe(&ev));
    cli.send(ev.peer, 0, b"hello from rust", true)?;
    while let Some(e) = srv.poll_wait(Duration::from_secs(3)) {
        println!("server: {}", describe(&e));
        if e.kind == EV_MESSAGE {
            srv.send(e.peer, 0, b"hi back", true)?;
            break;
        }
    }
    let reply = cli.poll_wait(Duration::from_secs(5)).ok_or("no reply")?;
    println!("client: {}", describe(&reply));
    let _ = std::fs::remove_file(key);
    Ok(())
}

fn main() {
    let args: Vec<String> = std::env::args().skip(1).collect();
    let r = match args.first().map(String::as_str) {
        Some("server") => server(args.get(1).and_then(|p| p.parse().ok()).unwrap_or(47000)),
        Some("client") => args.get(1).ok_or_else(|| "usage: client <TICKET>".to_string()).and_then(|t| client(t)),
        _ => demo(),
    };
    if let Err(e) = r {
        eprintln!("error: {e}");
        std::process::exit(1);
    }
}
