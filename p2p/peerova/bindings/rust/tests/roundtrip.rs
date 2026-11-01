//! cargo test --release -- --nocapture
use std::time::{Duration, Instant};

use peerova::{Sdk, EV_CONNECTED, EV_MESSAGE, MAX_UNRELIABLE};

#[test]
fn roundtrip_200_reliable_in_order_unreliable_and_reply() {
    const N: usize = 200;
    let key = std::env::temp_dir().join(format!("peerova-rs-test-{}.key", std::process::id()));
    let cfg = format!(r#"{{"port":0,"upnp":false,"key_file":{:?}}}"#, key.to_string_lossy());
    let mut srv = Sdk::server(Some(&cfg)).expect("server");
    let ticket = srv.ticket().expect("ticket").to_string();
    assert!(ticket.starts_with("pvt_"));
    let mut cli = Sdk::client(&ticket, None).expect("client");

    let ev = cli.poll_wait(Duration::from_secs(10)).expect("connect timeout");
    assert_eq!(ev.kind, EV_CONNECTED);
    let peer = ev.peer;
    let t = Instant::now();
    for i in 0..N {
        cli.send(peer, 1, format!("msg {i}").as_bytes(), true).unwrap();
    }
    cli.send(peer, 2, b"realtime", false).unwrap();
    assert!(cli.send(peer, 2, &vec![0u8; MAX_UNRELIABLE + 1], false).is_err());

    let (mut got, mut sp, mut unreliable) = (Vec::new(), None, 0);
    while let Some(e) = srv.poll_wait(Duration::from_secs(3)) {
        match e.kind {
            EV_CONNECTED => sp = Some(e.peer),
            EV_MESSAGE if e.reliable => {
                assert_eq!(e.channel, 1);
                got.push(String::from_utf8(e.data).unwrap());
                if got.len() == N {
                    break;
                }
            }
            EV_MESSAGE => unreliable += 1,
            _ => {}
        }
    }
    assert_eq!(got, (0..N).map(|i| format!("msg {i}")).collect::<Vec<_>>());
    // the datagram may land after the last reliable message (on loopback it is never lost)
    while unreliable == 0 {
        match srv.poll_wait(Duration::from_secs(1)) {
            Some(e) if e.kind == EV_MESSAGE && !e.reliable => {
                assert_eq!((e.channel, e.data.as_slice()), (2, &b"realtime"[..]));
                unreliable += 1
            }
            Some(_) => {}
            None => break,
        }
    }
    assert_eq!(unreliable, 1, "unreliable datagram not received");
    srv.send(sp.unwrap(), 0, b"pong\0\xff", true).unwrap();
    let r = loop {
        let e = cli.poll_wait(Duration::from_secs(5)).expect("reply timeout");
        if e.kind == EV_MESSAGE {
            break e;
        }
    };
    assert_eq!(r.data, b"pong\0\xff");
    assert_eq!(srv.peer_count(), 1);
    println!("rust PASS: {N} reliable msgs in order, unreliable={unreliable}, binary reply ok, {:.1} ms", t.elapsed().as_secs_f64() * 1e3);
    let _ = std::fs::remove_file(key);
}
