//! Robustness fuzzing (std-only, deterministic): every decoder that sees
//! network or user input must never panic on garbage or on mutated valid input.
//! Run: `cargo test --release -p peerova-core fuzz`.

#[cfg(test)]
mod tests {
    use std::collections::HashSet;
    use std::time::{Duration, Instant};

    use crate::conn::{Conn, Frame};
    use crate::crypto::{self, Keypair, Session};
    use crate::json::Json;
    use crate::ticket::{JoinCode, Ticket};
    use crate::wire::{CandKind, Candidate, Packet};

    struct Rng(u64);
    impl Rng {
        fn next(&mut self) -> u64 {
            self.0 ^= self.0 << 13;
            self.0 ^= self.0 >> 7;
            self.0 ^= self.0 << 17;
            self.0
        }
        fn below(&mut self, n: usize) -> usize {
            (self.next() % n.max(1) as u64) as usize
        }
        fn bytes(&mut self, max: usize) -> Vec<u8> {
            let n = self.below(max + 1);
            (0..n).map(|_| self.next() as u8).collect()
        }
        /// Flip/insert/delete/truncate/duplicate bytes of a valid sample.
        fn mutate(&mut self, src: &[u8]) -> Vec<u8> {
            let mut v = src.to_vec();
            for _ in 0..1 + self.below(4) {
                match self.below(6) {
                    0 if !v.is_empty() => {
                        let i = self.below(v.len());
                        v[i] ^= 1 << self.below(8);
                    }
                    1 if !v.is_empty() => {
                        let i = self.below(v.len());
                        v[i] = self.next() as u8;
                    }
                    2 => {
                        let i = self.below(v.len() + 1);
                        v.insert(i, self.next() as u8);
                    }
                    3 if !v.is_empty() => {
                        let i = self.below(v.len());
                        v.remove(i);
                    }
                    4 => v.truncate(self.below(v.len() + 1)),
                    _ => {
                        let i = self.below(v.len() + 1);
                        let extra = self.bytes(16);
                        v.splice(i..i, extra);
                    }
                }
            }
            v
        }
    }

    const N: usize = 60_000;

    #[test]
    fn fuzz_wire_and_frames() {
        let mut r = Rng(0x9E37_79B9_7F4A_7C15);
        let cands = vec![Candidate { kind: CandKind::Host, addr: "10.0.0.2:47000".parse().unwrap() }];
        let samples: Vec<Vec<u8>> = vec![
            Packet::Ping { session: 1, nonce: 2 }.encode(),
            Packet::IntroReq { server_id: [1; 16], session: 5, candidates: cands.clone(), proof: [2; 16] }.encode(),
            Packet::Intro { session: 9, candidates: cands }.encode(),
            Packet::Register { server_pub: [3; 32], lease: b"pvl1.x.y".to_vec() }.encode(),
        ];
        let mut frames = vec![];
        for f in [
            Frame::Ack { delay_ms: 3, ranges: vec![(10, 20), (1, 5)] },
            Frame::Stream { id: 7, off: 99, fin: true, data: b"abc".to_vec() },
            Frame::Datagram { seq: 4, data: b"udp".to_vec() },
            Frame::MaxData { id: 7, max: 1 << 20 },
        ] {
            f.encode(&mut frames);
        }
        for i in 0..N {
            let input = if i % 3 == 0 { r.bytes(300) } else { r.mutate(&samples[i % samples.len()]) };
            let _ = Packet::decode(&input);
            let fin = if i % 2 == 0 { r.mutate(&frames) } else { r.bytes(400) };
            let _ = Frame::decode_all(&fin);
        }
    }

    #[test]
    fn fuzz_text_formats() {
        let mut r = Rng(42);
        let t = Ticket {
            server_pub: [9; 32],
            private: false,
            tcp: true,
            rdv: true,
            hidden: false,
            candidates: vec![Candidate { kind: CandKind::Ipv6, addr: "[2402:800::5]:47000".parse().unwrap() }],
            supernodes: vec![],
        }
        .encode();
        let j = JoinCode { session: 7, candidates: vec![] }.encode();
        let json = "{\"a\":[1,2,{\"b\":\"c\\u00e9\"}],\"d\":-1.5e3,\"e\":true,\"f\":null}".as_bytes().to_vec();
        for i in 0..N {
            let s = String::from_utf8_lossy(&r.mutate(t.as_bytes())).to_string();
            let _ = Ticket::decode(&s);
            let s = String::from_utf8_lossy(&r.mutate(j.as_bytes())).to_string();
            let _ = JoinCode::decode(&s);
            let raw = if i % 4 == 0 { r.bytes(200) } else { r.mutate(&json) };
            let _ = Json::parse(&String::from_utf8_lossy(&raw));
            let _ = crate::lobby::is_room_code(&String::from_utf8_lossy(&r.bytes(8)));
        }
        // Deep nesting must be rejected, not overflow the stack.
        assert!(Json::parse(&"[".repeat(100_000)).is_err());
    }

    #[test]
    fn fuzz_tokens() {
        use ed25519_dalek::{Signer, SigningKey};
        let mut r = Rng(7);
        let sk = SigningKey::from_bytes(&[5; 32]);
        let pk = sk.verifying_key().to_bytes();
        let payload = r#"{"v":1,"lid":"l","pid":"p","plan":"indie","srv":3,"ccu":200,"mid":"m","iat":1,"exp":99999999999}"#;
        let signed = format!("pvl1.{}", crate::util::b64_encode(payload.as_bytes()));
        let lease = format!("{signed}.{}", crate::util::b64_encode(&sk.sign(signed.as_bytes()).to_bytes()));
        let access = crate::access::issue(&[3; 32], "alice", &["tcp:443".into()], 3600, 1000);
        let apk = crate::access::public_key(&[3; 32]);
        let none = HashSet::new();
        for _ in 0..N / 2 {
            let m = String::from_utf8_lossy(&r.mutate(lease.as_bytes())).to_string();
            if let Ok(l) = crate::license::verify(&m, &pk, None, 2000) {
                assert_eq!(l.ccu, 200, "a mutated lease must not change its terms");
            }
            let a = r.mutate(access.as_bytes());
            if let Ok(t) = crate::access::verify(&a, &apk, &none, 2000) {
                assert_eq!(t.sub, "alice");
            }
        }
    }

    #[test]
    fn fuzz_handshake_and_session() {
        let mut r = Rng(99);
        let server = Keypair::generate();
        let client = Keypair::generate();
        let init = crypto::initiate(&client, &server.public, 1, b"tok");
        let resp_r = crypto::read_init(&server, &init.packet).unwrap();
        let (resp, mut s_sess) = resp_r.accept(2);
        let c_sess = init.finish(&resp).unwrap();
        let good = c_sess.seal(1, b"payload");
        let mut accepted = 0;
        for i in 0..N / 2 {
            let m = r.mutate(if i % 2 == 0 { &init.packet } else { &good });
            if crypto::read_init(&server, &m).is_some() && m != init.packet {
                accepted += 1; // mac1 + AEAD must reject any modification
            }
            let _ = init.finish(&r.mutate(&resp));
            if let Some((_, pt)) = s_sess.open(&m) {
                assert_eq!(pt, b"payload", "forged data must never decrypt");
            }
            let _ = Session::packet_index(&m);
        }
        assert_eq!(accepted, 0, "mutated handshakes were accepted");
    }

    #[test]
    fn fuzz_conn_state_machine_bounded() {
        let mut r = Rng(1234);
        let now0 = Instant::now();
        let mut c = Conn::new(false, now0);
        let mut now = now0;
        for i in 0..N {
            // Random but well-formed frames: huge offsets, fins, resets, acks of unsent pns…
            let mut buf = vec![];
            let f = match r.below(6) {
                0 => Frame::Stream { id: r.next() as u32, off: r.next() % (1 << 40), fin: r.below(2) == 0, data: r.bytes(900) },
                1 => Frame::Ack { delay_ms: r.next() as u16, ranges: vec![(r.next() % 1000, r.next() % 1000 + 1000)] },
                2 => Frame::MaxData { id: r.next() as u32, max: r.next() },
                3 => Frame::Reset { id: r.next() as u32 },
                4 => Frame::Datagram { seq: r.next() as u32, data: r.bytes(1000) },
                _ => Frame::Ping,
            };
            f.encode(&mut buf);
            c.on_packet(i as u64, &buf, now);
            while c.poll_event().is_some() {}
            while c.poll_transmit(now).is_some() {}
            now += Duration::from_micros(200);
            if c.is_closed() {
                c = Conn::new(false, now);
            }
        }
    }
}
