//! Cryptography: Noise_IK_25519_ChaChaPoly_BLAKE2s handshake + transport AEAD.
//!
//! Primitives come from audited RustCrypto / dalek crates; the protocol glue
//! (Noise state machine, MAC1, replay window) is ours and covered by tests.
//!
//! Dark-server property: every handshake initiation carries `mac1`, keyed by
//! the server's static public key (only known from a ticket). The server drops
//! anything without a valid mac1 *before* doing any expensive work and never
//! answers, so port scanners see a closed/filtered port.

use blake2::{Blake2s256, Digest};
use chacha20poly1305::aead::{Aead, KeyInit, Payload};
use chacha20poly1305::{ChaCha20Poly1305, Key, Nonce};
use x25519_dalek::{PublicKey, StaticSecret};

pub const PROTOCOL: &[u8] = b"Noise_IK_25519_ChaChaPoly_BLAKE2s";
pub const PROLOGUE: &[u8] = b"peerova v2";
pub const TAG: usize = 16;

pub const T_INIT: u8 = 0x20;
pub const T_RESP: u8 = 0x21;
/// Authenticated refusal of a valid handshake (server full, access denied...): same shape as
/// T_RESP with an encrypted reason instead of an empty payload. Only a holder of the server's
/// static key can produce it, and only a client that proved it holds the ticket receives it.
pub const T_REJECT: u8 = 0x22;
/// Longest reason payload in a T_REJECT.
pub const REJECT_MAX: usize = 64;
pub const T_PUNCH: u8 = 0x30;
pub const T_DATA: u8 = 0x40;

pub const INIT_MIN: usize = 1 + 4 + 32 + (32 + TAG) + TAG + 16;
pub const RESP_LEN: usize = 1 + 4 + 4 + 32 + TAG + 16;
pub const DATA_HDR: usize = 1 + 4 + 8;

pub fn random_bytes<const N: usize>() -> [u8; N] {
    let mut b = [0u8; N];
    getrandom::getrandom(&mut b).expect("OS RNG unavailable");
    b
}

pub fn random_u32() -> u32 {
    u32::from_le_bytes(random_bytes())
}

pub fn hash(parts: &[&[u8]]) -> [u8; 32] {
    let mut h = Blake2s256::new();
    for p in parts {
        h.update(p);
    }
    h.finalize().into()
}

pub fn hmac(key: &[u8], data: &[&[u8]]) -> [u8; 32] {
    let mut k = [0u8; 64];
    if key.len() > 64 {
        k[..32].copy_from_slice(&hash(&[key]));
    } else {
        k[..key.len()].copy_from_slice(key);
    }
    let mut ipad = [0x36u8; 64];
    let mut opad = [0x5cu8; 64];
    for i in 0..64 {
        ipad[i] ^= k[i];
        opad[i] ^= k[i];
    }
    let mut inner = Blake2s256::new();
    inner.update(ipad);
    for d in data {
        inner.update(d);
    }
    let ih = inner.finalize();
    let mut outer = Blake2s256::new();
    outer.update(opad);
    outer.update(ih);
    outer.finalize().into()
}

fn hkdf2(ck: &[u8; 32], ikm: &[u8]) -> ([u8; 32], [u8; 32]) {
    let t = hmac(ck, &[ikm]);
    let o1 = hmac(&t, &[&[1]]);
    let o2 = hmac(&t, &[&o1, &[2]]);
    (o1, o2)
}

fn nonce(n: u64) -> [u8; 12] {
    let mut b = [0u8; 12];
    b[4..].copy_from_slice(&n.to_le_bytes());
    b
}

fn seal(key: &[u8; 32], n: u64, aad: &[u8], pt: &[u8]) -> Vec<u8> {
    ChaCha20Poly1305::new(Key::from_slice(key))
        .encrypt(Nonce::from_slice(&nonce(n)), Payload { msg: pt, aad })
        .expect("chacha encrypt")
}

fn open(key: &[u8; 32], n: u64, aad: &[u8], ct: &[u8]) -> Option<Vec<u8>> {
    ChaCha20Poly1305::new(Key::from_slice(key))
        .decrypt(Nonce::from_slice(&nonce(n)), Payload { msg: ct, aad })
        .ok()
}

pub fn ct_eq(a: &[u8], b: &[u8]) -> bool {
    a.len() == b.len() && a.iter().zip(b).fold(0u8, |acc, (x, y)| acc | (x ^ y)) == 0
}

/// 16-byte MAC proving the sender knows `key_pub` (the receiver's static key).
pub fn mac1(key_pub: &[u8; 32], data: &[u8]) -> [u8; 16] {
    let k = hash(&[b"mac1----", key_pub]);
    let m = hmac(&k, &[data]);
    m[..16].try_into().unwrap()
}

/// Short public id derived from a static key (used by supernodes).
pub fn key_id(public: &[u8; 32]) -> [u8; 16] {
    hash(&[b"peerova-id-", public])[..16].try_into().unwrap()
}

#[derive(Clone)]
pub struct Keypair {
    secret: StaticSecret,
    pub public: [u8; 32],
}

impl Keypair {
    pub fn generate() -> Self {
        Self::from_secret(random_bytes())
    }
    pub fn from_secret(b: [u8; 32]) -> Self {
        let secret = StaticSecret::from(b);
        let public = PublicKey::from(&secret).to_bytes();
        Keypair { secret, public }
    }
    pub fn secret_bytes(&self) -> [u8; 32] {
        self.secret.to_bytes()
    }
    pub fn dh(&self, other: &[u8; 32]) -> [u8; 32] {
        self.secret.diffie_hellman(&PublicKey::from(*other)).to_bytes()
    }
}

#[derive(Clone)]
struct Symmetric {
    ck: [u8; 32],
    h: [u8; 32],
    k: Option<[u8; 32]>,
    n: u64,
}

impl Symmetric {
    fn new() -> Self {
        // PROTOCOL is 33 bytes > HASHLEN, so h = HASH(protocol_name).
        let h = hash(&[PROTOCOL]);
        let mut s = Symmetric { ck: h, h, k: None, n: 0 };
        s.mix_hash(PROLOGUE);
        s
    }
    fn mix_hash(&mut self, d: &[u8]) {
        self.h = hash(&[&self.h, d]);
    }
    fn mix_key(&mut self, ikm: &[u8]) {
        let (ck, k) = hkdf2(&self.ck, ikm);
        self.ck = ck;
        self.k = Some(k);
        self.n = 0;
    }
    fn encrypt_and_hash(&mut self, pt: &[u8]) -> Vec<u8> {
        let c = match self.k {
            Some(k) => {
                let c = seal(&k, self.n, &self.h, pt);
                self.n += 1;
                c
            }
            None => pt.to_vec(),
        };
        self.mix_hash(&c);
        c
    }
    fn decrypt_and_hash(&mut self, ct: &[u8]) -> Option<Vec<u8>> {
        let p = match self.k {
            Some(k) => {
                let p = open(&k, self.n, &self.h, ct)?;
                self.n += 1;
                p
            }
            None => ct.to_vec(),
        };
        self.mix_hash(ct);
        Some(p)
    }
    fn split(&self) -> ([u8; 32], [u8; 32]) {
        hkdf2(&self.ck, &[])
    }
}

/// Client side, waiting for the response.
pub struct Initiator {
    sym: Symmetric,
    e: Keypair,
    s: Keypair,
    pub sender_idx: u32,
    pub packet: Vec<u8>,
}

/// Build handshake initiation. `payload` (encrypted, authenticated) carries
/// timestamp + access token.
pub fn initiate(s: &Keypair, rs: &[u8; 32], sender_idx: u32, payload: &[u8]) -> Initiator {
    let mut sym = Symmetric::new();
    sym.mix_hash(rs);
    let e = Keypair::generate();
    let mut msg = vec![T_INIT];
    msg.extend_from_slice(&sender_idx.to_le_bytes());
    msg.extend_from_slice(&e.public);
    sym.mix_hash(&e.public);
    sym.mix_key(&e.dh(rs)); // es
    let enc_s = sym.encrypt_and_hash(&s.public);
    msg.extend_from_slice(&enc_s);
    sym.mix_key(&s.dh(rs)); // ss
    let enc_p = sym.encrypt_and_hash(payload);
    msg.extend_from_slice(&enc_p);
    let m = mac1(rs, &msg);
    msg.extend_from_slice(&m);
    Initiator { sym, e, s: s.clone(), sender_idx, packet: msg }
}

impl Initiator {
    /// Process the response. Returns (session keys, responder index).
    /// Non-consuming: duplicate/forged responses don't destroy the state.
    pub fn finish(&self, resp: &[u8]) -> Option<Session> {
        let mut sym = self.sym.clone();
        if resp.len() != RESP_LEN || resp[0] != T_RESP {
            return None;
        }
        let (body, mac) = resp.split_at(RESP_LEN - 16);
        if !ct_eq(mac, &mac1(&self.s.public, body)) {
            return None;
        }
        let responder_idx = u32::from_le_bytes(resp[1..5].try_into().ok()?);
        let receiver = u32::from_le_bytes(resp[5..9].try_into().ok()?);
        if receiver != self.sender_idx {
            return None;
        }
        let re: [u8; 32] = resp[9..41].try_into().ok()?;
        sym.mix_hash(&re);
        sym.mix_key(&self.e.dh(&re)); // ee
        sym.mix_key(&self.s.dh(&re)); // se
        sym.decrypt_and_hash(&resp[41..41 + TAG])?;
        let (k1, k2) = sym.split();
        Some(Session::new(k1, k2, self.sender_idx, responder_idx))
    }

    /// Process a T_REJECT answering this initiation: the server's (authenticated) reason.
    pub fn finish_reject(&self, msg: &[u8]) -> Option<Vec<u8>> {
        let mut sym = self.sym.clone();
        if msg.len() < RESP_LEN || msg.len() > RESP_LEN + REJECT_MAX || msg[0] != T_REJECT {
            return None;
        }
        let (body, mac) = msg.split_at(msg.len() - 16);
        if !ct_eq(mac, &mac1(&self.s.public, body)) {
            return None;
        }
        if u32::from_le_bytes(msg[5..9].try_into().ok()?) != self.sender_idx {
            return None;
        }
        let re: [u8; 32] = msg[9..41].try_into().ok()?;
        sym.mix_hash(&re);
        sym.mix_key(&self.e.dh(&re)); // ee
        sym.mix_key(&self.s.dh(&re)); // se
        sym.decrypt_and_hash(&body[41..])
    }
}

/// Server side after reading a valid initiation; decide (auth, license) then `accept`.
pub struct Responder {
    sym: Symmetric,
    re: [u8; 32],
    pub initiator_static: [u8; 32],
    pub initiator_idx: u32,
    pub payload: Vec<u8>,
}

/// Cheap pre-check: only mac1, no DH. Lets the server drop scans/garbage for free.
pub fn check_init_mac(s_pub: &[u8; 32], msg: &[u8]) -> bool {
    msg.len() >= INIT_MIN && msg[0] == T_INIT && {
        let (body, mac) = msg.split_at(msg.len() - 16);
        ct_eq(mac, &mac1(s_pub, body))
    }
}

pub fn read_init(s: &Keypair, msg: &[u8]) -> Option<Responder> {
    if !check_init_mac(&s.public, msg) {
        return None;
    }
    let body = &msg[..msg.len() - 16];
    let mut sym = Symmetric::new();
    sym.mix_hash(&s.public);
    let initiator_idx = u32::from_le_bytes(body[1..5].try_into().ok()?);
    let re: [u8; 32] = body[5..37].try_into().ok()?;
    sym.mix_hash(&re);
    sym.mix_key(&s.dh(&re)); // es
    let rs: [u8; 32] = sym.decrypt_and_hash(&body[37..37 + 32 + TAG])?.try_into().ok()?;
    sym.mix_key(&s.dh(&rs)); // ss
    let payload = sym.decrypt_and_hash(&body[37 + 32 + TAG..])?;
    Some(Responder { sym, re, initiator_static: rs, initiator_idx, payload })
}

impl Responder {
    pub fn accept(mut self, my_idx: u32) -> (Vec<u8>, Session) {
        let e = Keypair::generate();
        let mut msg = vec![T_RESP];
        msg.extend_from_slice(&my_idx.to_le_bytes());
        msg.extend_from_slice(&self.initiator_idx.to_le_bytes());
        msg.extend_from_slice(&e.public);
        self.sym.mix_hash(&e.public);
        self.sym.mix_key(&e.dh(&self.re)); // ee
        self.sym.mix_key(&e.dh(&self.initiator_static)); // se
        let enc = self.sym.encrypt_and_hash(&[]);
        msg.extend_from_slice(&enc);
        let m = mac1(&self.initiator_static, &msg);
        msg.extend_from_slice(&m);
        let (k1, k2) = self.sym.split();
        (msg, Session::new(k2, k1, my_idx, self.initiator_idx))
    }

    /// Refuse this (valid) initiation with an encrypted, authenticated `reason` (T_REJECT).
    pub fn reject(&self, reason: &[u8]) -> Vec<u8> {
        let mut sym = self.sym.clone();
        let reason = &reason[..reason.len().min(REJECT_MAX)];
        let e = Keypair::generate();
        let mut msg = vec![T_REJECT];
        msg.extend_from_slice(&0u32.to_le_bytes());
        msg.extend_from_slice(&self.initiator_idx.to_le_bytes());
        msg.extend_from_slice(&e.public);
        sym.mix_hash(&e.public);
        sym.mix_key(&e.dh(&self.re)); // ee
        sym.mix_key(&e.dh(&self.initiator_static)); // se
        msg.extend_from_slice(&sym.encrypt_and_hash(reason));
        let m = mac1(&self.initiator_static, &msg);
        msg.extend_from_slice(&m);
        msg
    }
}

/// Sliding anti-replay window (1024 packets).
#[derive(Default)]
struct Replay {
    max: u64,
    bits: [u64; 16],
    any: bool,
}

impl Replay {
    const SIZE: u64 = 1024;
    fn bit(&self, d: u64) -> bool {
        self.bits[(d / 64) as usize] >> (d % 64) & 1 == 1
    }
    fn set(&mut self, d: u64) {
        self.bits[(d / 64) as usize] |= 1 << (d % 64);
    }
    fn check(&self, n: u64) -> bool {
        if !self.any || n > self.max {
            return true;
        }
        let d = self.max - n;
        d < Self::SIZE && !self.bit(d)
    }
    fn commit(&mut self, n: u64) {
        if !self.any {
            *self = Replay { max: n, bits: [0; 16], any: true };
            self.set(0);
            return;
        }
        if n > self.max {
            let shift = n - self.max;
            if shift >= Self::SIZE {
                self.bits = [0; 16];
            } else {
                let (w, b) = ((shift / 64) as usize, (shift % 64) as u32);
                let old = self.bits;
                for i in (0..16).rev() {
                    let hi = if i >= w { old[i - w] << b } else { 0 };
                    let lo = if b > 0 && i > w { old[i - w - 1] >> (64 - b) } else { 0 };
                    self.bits[i] = hi | lo;
                }
            }
            self.max = n;
            self.set(0);
        } else {
            self.set(self.max - n);
        }
    }
}

/// Established transport keys. Packet: T_DATA | receiver_idx u32 | counter u64 | ciphertext.
pub struct Session {
    send: ChaCha20Poly1305,
    recv: ChaCha20Poly1305,
    pub local_idx: u32,
    pub remote_idx: u32,
    replay: Replay,
}

impl Session {
    fn new(send: [u8; 32], recv: [u8; 32], local_idx: u32, remote_idx: u32) -> Self {
        Session {
            send: ChaCha20Poly1305::new(Key::from_slice(&send)),
            recv: ChaCha20Poly1305::new(Key::from_slice(&recv)),
            local_idx,
            remote_idx,
            replay: Replay::default(),
        }
    }

    pub fn seal(&self, counter: u64, pt: &[u8]) -> Vec<u8> {
        let mut hdr = Vec::with_capacity(DATA_HDR + pt.len() + TAG);
        hdr.push(T_DATA);
        hdr.extend_from_slice(&self.remote_idx.to_le_bytes());
        hdr.extend_from_slice(&counter.to_le_bytes());
        let ct = self
            .send
            .encrypt(Nonce::from_slice(&nonce(counter)), Payload { msg: pt, aad: &hdr })
            .expect("chacha encrypt");
        hdr.extend_from_slice(&ct);
        hdr
    }

    /// Receiver index of a data packet (to find the session).
    pub fn packet_index(pkt: &[u8]) -> Option<u32> {
        (pkt.len() >= DATA_HDR + TAG && pkt[0] == T_DATA).then(|| u32::from_le_bytes(pkt[1..5].try_into().unwrap()))
    }

    pub fn open(&mut self, pkt: &[u8]) -> Option<(u64, Vec<u8>)> {
        if pkt.len() < DATA_HDR + TAG || pkt[0] != T_DATA {
            return None;
        }
        let counter = u64::from_le_bytes(pkt[5..13].try_into().ok()?);
        if !self.replay.check(counter) {
            return None;
        }
        let pt = self
            .recv
            .decrypt(Nonce::from_slice(&nonce(counter)), Payload { msg: &pkt[DATA_HDR..], aad: &pkt[..DATA_HDR] })
            .ok()?;
        self.replay.commit(counter);
        Some((counter, pt))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn handshake_and_transport() {
        let server = Keypair::generate();
        let client = Keypair::generate();
        let init = initiate(&client, &server.public, 11, b"token");
        assert!(check_init_mac(&server.public, &init.packet));
        let r = read_init(&server, &init.packet).expect("server reads init");
        assert_eq!(r.initiator_static, client.public);
        assert_eq!(r.payload, b"token");
        let (resp, mut s_sess) = r.accept(22);
        let mut c_sess = init.finish(&resp).expect("client finishes");
        assert_eq!(c_sess.remote_idx, 22);
        assert_eq!(Session::packet_index(&c_sess.seal(0, b"x")), Some(22));

        let p = c_sess.seal(5, b"hello server");
        assert_eq!(s_sess.open(&p).unwrap(), (5, b"hello server".to_vec()));
        assert!(s_sess.open(&p).is_none(), "replay must be rejected");
        let q = s_sess.seal(0, b"hello client");
        assert_eq!(c_sess.open(&q).unwrap().1, b"hello client");
    }

    #[test]
    fn authenticated_rejection() {
        let server = Keypair::generate();
        let client = Keypair::generate();
        let init = initiate(&client, &server.public, 11, b"t");
        let r = read_init(&server, &init.packet).unwrap();
        let rej = r.reject(b"\x01full");
        assert_eq!(init.finish_reject(&rej).as_deref(), Some(&b"\x01full"[..]));
        assert!(init.finish(&rej).is_none(), "a rejection is never a session");
        // Forged / tampered rejections are ignored.
        let mut bad = rej.clone();
        bad[45] ^= 1;
        assert!(init.finish_reject(&bad).is_none());
        let other = initiate(&client, &server.public, 12, b"t");
        assert!(other.finish_reject(&rej).is_none(), "bound to the initiation");
        let fake = read_init(&server, &initiate(&Keypair::generate(), &server.public, 11, b"t").packet).unwrap().reject(b"x");
        assert!(init.finish_reject(&fake).is_none(), "made for another client key");
    }

    #[test]
    fn wrong_server_key_is_dark() {
        let server = Keypair::generate();
        let other = Keypair::generate();
        let client = Keypair::generate();
        let init = initiate(&client, &other.public, 1, b"");
        assert!(!check_init_mac(&server.public, &init.packet));
        assert!(read_init(&server, &init.packet).is_none());
    }

    #[test]
    fn tampering_detected() {
        let server = Keypair::generate();
        let client = Keypair::generate();
        let init = initiate(&client, &server.public, 1, b"abc");
        let mut bad = init.packet.clone();
        bad[50] ^= 1;
        assert!(read_init(&server, &bad).is_none());
        let (resp, s_sess) = read_init(&server, &init.packet).unwrap().accept(2);
        let mut c = init.finish(&resp).unwrap();
        let mut p = s_sess.seal(1, b"data");
        let last = p.len() - 1;
        p[last] ^= 0x80;
        assert!(c.open(&p).is_none());
    }

    #[test]
    fn replay_window() {
        let mut r = Replay::default();
        for n in [5u64, 3, 10, 7, 2000, 1500, 999] {
            assert!(r.check(n), "{n}");
            r.commit(n);
            assert!(!r.check(n), "{n} dup");
        }
        assert!(!r.check(900), "too old");
        assert!(r.check(1999));
        r.commit(70_000);
        assert!(!r.check(2000));
        assert!(r.check(69_999));
    }
}
