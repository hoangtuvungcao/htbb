//! Reliable multiplexed transport over the encrypted UDP session (a "mini QUIC").
//!
//! Pure state machine — no sockets, no clock reads — so it is fully testable
//! with a simulated lossy network. The engine feeds decrypted payloads in and
//! pulls plaintext payloads out (encrypting them with the packet number as nonce).
//!
//! * Packet numbers monotonic, never reused (also the AEAD counter).
//! * Range ACKs, RTT estimation (RFC 6298 style), packet/time-threshold loss
//!   detection, PTO probes, NewReno + delay-based (low-latency) congestion control:
//!   the window shrinks as soon as queueing delay builds up, so bulk transfers
//!   never inflate the ping of realtime game traffic sharing the link.
//! * Priority: datagrams > control > SDK channel streams > bulk tunnel streams.
//! * Bidirectional streams with per-stream flow control (TCP tunnelling, SDK
//!   reliable channels) + unreliable datagrams (UDP tunnelling, SDK unreliable).

use std::collections::{BTreeMap, HashMap, VecDeque};
use std::time::{Duration, Instant};

/// Max UDP payload we emit (safe for IPv4/IPv6 + PPPoE + mobile).
pub const MAX_UDP: usize = 1280;
/// Plaintext budget per packet (minus data header + AEAD tag).
pub const MAX_PAYLOAD: usize = MAX_UDP - crate::crypto::DATA_HDR - crate::crypto::TAG;
/// Largest datagram payload the app may send.
pub const MAX_DATAGRAM: usize = MAX_PAYLOAD - 3 - 40;

const RECV_WINDOW: u64 = 1 << 20;
const SEND_BUF: usize = 1 << 20;
const INIT_CWND: usize = 10 * MAX_PAYLOAD;
const MIN_CWND: usize = 2 * MAX_PAYLOAD;
const ACK_DELAY: Duration = Duration::from_millis(10);
const INIT_RTT: Duration = Duration::from_millis(250);
pub const IDLE_TIMEOUT: Duration = Duration::from_secs(30);
pub const KEEPALIVE: Duration = Duration::from_secs(8);
const MAX_ACK_RANGES: usize = 24;
const DEAD_AFTER_PTOS: u32 = 6;
const DEAD_MIN_SILENCE: Duration = Duration::from_secs(5);
/// Datagrams up to this size get redundancy (game inputs/state deltas, voice frames).
const REDUNDANT_MAX: usize = 300;
/// If no newer datagram follows, re-send the last small one once after this delay.
const REDUNDANT_TAIL: Duration = Duration::from_millis(15);

// ------------------------------------------------------------- RangeSet

/// Set of disjoint half-open ranges [start, end).
#[derive(Default, Clone, Debug)]
pub struct RangeSet {
    m: BTreeMap<u64, u64>,
}

impl RangeSet {
    pub fn insert(&mut self, mut s: u64, mut e: u64) {
        if s >= e {
            return;
        }
        let touching: Vec<u64> = self
            .m
            .range(..=e)
            .rev()
            .take_while(|(_, &end)| end >= s)
            .map(|(&k, _)| k)
            .collect();
        for k in touching {
            let end = self.m.remove(&k).unwrap();
            s = s.min(k);
            e = e.max(end);
        }
        self.m.insert(s, e);
    }

    pub fn remove(&mut self, s: u64, e: u64) {
        if s >= e {
            return;
        }
        let over: Vec<(u64, u64)> = self
            .m
            .range(..e)
            .rev()
            .take_while(|(_, &end)| end > s)
            .map(|(&k, &v)| (k, v))
            .collect();
        for (k, v) in over {
            self.m.remove(&k);
            if k < s {
                self.m.insert(k, s);
            }
            if v > e {
                self.m.insert(e, v);
            }
        }
    }

    pub fn contains(&self, x: u64) -> bool {
        self.m.range(..=x).next_back().is_some_and(|(_, &e)| x < e)
    }
    pub fn first(&self) -> Option<(u64, u64)> {
        self.m.iter().next().map(|(&s, &e)| (s, e))
    }
    pub fn is_empty(&self) -> bool {
        self.m.is_empty()
    }
    pub fn len(&self) -> usize {
        self.m.len()
    }
    pub fn iter(&self) -> impl DoubleEndedIterator<Item = (u64, u64)> + '_ {
        self.m.iter().map(|(&s, &e)| (s, e))
    }
    fn pop_smallest(&mut self) {
        if let Some(&k) = self.m.keys().next() {
            self.m.remove(&k);
        }
    }
}

// ------------------------------------------------------------- frames

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum Frame {
    Ping,
    /// Ranges [start, end) of received packet numbers, descending.
    Ack {
        delay_ms: u16,
        ranges: Vec<(u64, u64)>,
    },
    Stream {
        id: u32,
        off: u64,
        fin: bool,
        data: Vec<u8>,
    },
    MaxData {
        id: u32,
        max: u64,
    },
    Reset {
        id: u32,
    },
    /// Unreliable datagram; `seq` lets the receiver drop redundant copies.
    Datagram {
        seq: u32,
        data: Vec<u8>,
    },
    Close {
        code: u8,
    },
}

mod ft {
    pub const PING: u8 = 1;
    pub const ACK: u8 = 2;
    pub const STREAM: u8 = 3;
    pub const MAX_DATA: u8 = 4;
    pub const RESET: u8 = 5;
    pub const DATAGRAM: u8 = 6;
    pub const CLOSE: u8 = 7;
}

const STREAM_HDR: usize = 1 + 4 + 8 + 1 + 2;

impl Frame {
    pub fn encode(&self, out: &mut Vec<u8>) {
        match self {
            Frame::Ping => out.push(ft::PING),
            Frame::Ack { delay_ms, ranges } => {
                out.push(ft::ACK);
                out.extend_from_slice(&delay_ms.to_be_bytes());
                out.push(ranges.len() as u8);
                for (s, e) in ranges {
                    out.extend_from_slice(&s.to_be_bytes());
                    out.extend_from_slice(&((e - s) as u32).to_be_bytes());
                }
            }
            Frame::Stream { id, off, fin, data } => {
                out.push(ft::STREAM);
                out.extend_from_slice(&id.to_be_bytes());
                out.extend_from_slice(&off.to_be_bytes());
                out.push(*fin as u8);
                out.extend_from_slice(&(data.len() as u16).to_be_bytes());
                out.extend_from_slice(data);
            }
            Frame::MaxData { id, max } => {
                out.push(ft::MAX_DATA);
                out.extend_from_slice(&id.to_be_bytes());
                out.extend_from_slice(&max.to_be_bytes());
            }
            Frame::Reset { id } => {
                out.push(ft::RESET);
                out.extend_from_slice(&id.to_be_bytes());
            }
            Frame::Datagram { seq, data } => {
                out.push(ft::DATAGRAM);
                out.extend_from_slice(&seq.to_be_bytes());
                out.extend_from_slice(&(data.len() as u16).to_be_bytes());
                out.extend_from_slice(data);
            }
            Frame::Close { code } => out.extend_from_slice(&[ft::CLOSE, *code]),
        }
    }

    pub fn decode_all(mut b: &[u8]) -> Option<Vec<Frame>> {
        fn take<'a>(b: &mut &'a [u8], n: usize) -> Option<&'a [u8]> {
            if b.len() < n {
                return None;
            }
            let (h, t) = b.split_at(n);
            *b = t;
            Some(h)
        }
        let u16_ = |b: &mut &[u8]| take(b, 2).map(|x| u16::from_be_bytes(x.try_into().unwrap()));
        let u32_ = |b: &mut &[u8]| take(b, 4).map(|x| u32::from_be_bytes(x.try_into().unwrap()));
        let u64_ = |b: &mut &[u8]| take(b, 8).map(|x| u64::from_be_bytes(x.try_into().unwrap()));
        let mut out = vec![];
        while !b.is_empty() {
            let t = take(&mut b, 1)?[0];
            out.push(match t {
                ft::PING => Frame::Ping,
                ft::ACK => {
                    let delay_ms = u16_(&mut b)?;
                    let n = take(&mut b, 1)?[0] as usize;
                    let mut ranges = Vec::with_capacity(n);
                    for _ in 0..n {
                        let s = u64_(&mut b)?;
                        let len = u32_(&mut b)? as u64;
                        ranges.push((s, s.checked_add(len)?));
                    }
                    Frame::Ack { delay_ms, ranges }
                }
                ft::STREAM => {
                    let id = u32_(&mut b)?;
                    let off = u64_(&mut b)?;
                    let fin = take(&mut b, 1)?[0] != 0;
                    let len = u16_(&mut b)? as usize;
                    Frame::Stream {
                        id,
                        off,
                        fin,
                        data: take(&mut b, len)?.to_vec(),
                    }
                }
                ft::MAX_DATA => Frame::MaxData {
                    id: u32_(&mut b)?,
                    max: u64_(&mut b)?,
                },
                ft::RESET => Frame::Reset { id: u32_(&mut b)? },
                ft::DATAGRAM => {
                    let seq = u32_(&mut b)?;
                    let len = u16_(&mut b)? as usize;
                    Frame::Datagram {
                        seq,
                        data: take(&mut b, len)?.to_vec(),
                    }
                }
                ft::CLOSE => Frame::Close {
                    code: take(&mut b, 1)?[0],
                },
                _ => return None,
            });
        }
        Some(out)
    }
}

// ------------------------------------------------------------- streams

struct SendStream {
    buf: VecDeque<u8>,
    base: u64,
    next: u64,
    acked: RangeSet,
    lost: RangeSet,
    fin: Option<u64>,
    fin_pending: bool,
    fin_acked: bool,
    max_data: u64,
}

impl Default for SendStream {
    /// Must start with the peer's initial window, never 0 (or nothing is ever sent).
    fn default() -> Self {
        SendStream {
            buf: VecDeque::new(),
            base: 0,
            next: 0,
            acked: RangeSet::default(),
            lost: RangeSet::default(),
            fin: None,
            fin_pending: false,
            fin_acked: false,
            max_data: RECV_WINDOW,
        }
    }
}

impl SendStream {
    fn end(&self) -> u64 {
        self.base + self.buf.len() as u64
    }
    fn has_pending(&self) -> bool {
        !self.lost.is_empty()
            || (self.next < self.end() && self.next < self.max_data)
            || self.fin_pending
    }
    /// Next chunk to (re)send: (offset, data, fin).
    fn next_chunk(&mut self, max: usize) -> Option<(u64, Vec<u8>, bool)> {
        let (s, e) = if let Some((s, e)) = self.lost.first() {
            let s = s.max(self.base);
            let e = e.min(s + max as u64).min(self.end());
            self.lost.remove(0, e);
            if s >= e {
                return self.next_chunk(max);
            }
            (s, e)
        } else if self.next < self.end().min(self.max_data) {
            let s = self.next;
            let e = self.end().min(self.max_data).min(s + max as u64);
            self.next = e;
            (s, e)
        } else if self.fin_pending {
            let f = self.fin.unwrap();
            self.fin_pending = false;
            return Some((f, vec![], true));
        } else {
            return None;
        };
        let from = (s - self.base) as usize;
        let to = (e - self.base) as usize;
        let data: Vec<u8> = self.buf.range(from..to).copied().collect();
        let fin = self.fin == Some(e);
        if fin {
            self.fin_pending = false;
        }
        Some((s, data, fin))
    }
    fn on_ack(&mut self, off: u64, len: u64, fin: bool) -> usize {
        if fin {
            self.fin_acked = true;
        }
        self.acked.insert(off, off + len);
        self.lost.remove(off, off + len);
        let mut freed = 0;
        while let Some((s, e)) = self.acked.first() {
            if s > self.base {
                break;
            }
            self.acked.remove(s, e);
            if e > self.base {
                let n = (e.min(self.end()) - self.base) as usize;
                self.buf.drain(..n);
                self.base += n as u64;
                freed += n;
            }
        }
        freed
    }
    fn on_lost(&mut self, off: u64, len: u64, fin: bool) {
        let (s, e) = (off.max(self.base), off + len);
        if s < e {
            self.lost.insert(s, e);
            for (a, b) in self.acked.iter().collect::<Vec<_>>() {
                self.lost.remove(a, b);
            }
        }
        if fin && !self.fin_acked {
            self.fin_pending = true;
        }
    }
    fn done(&self) -> bool {
        self.fin_acked && self.buf.is_empty()
    }
}

struct RecvStream {
    read: u64,
    pending: BTreeMap<u64, Vec<u8>>,
    fin: Option<u64>,
    fin_delivered: bool,
    consumed: u64,
    advertised: u64,
}

impl Default for RecvStream {
    fn default() -> Self {
        RecvStream {
            read: 0,
            pending: BTreeMap::new(),
            fin: None,
            fin_delivered: false,
            consumed: 0,
            advertised: RECV_WINDOW,
        }
    }
}

impl RecvStream {
    fn on_data(&mut self, off: u64, mut data: Vec<u8>, fin: bool) -> (Vec<u8>, bool) {
        let end = off + data.len() as u64;
        if fin {
            self.fin = Some(end);
        }
        // Enforce flow control: ignore bytes beyond what we advertised.
        if end > self.advertised {
            data.truncate(self.advertised.saturating_sub(off) as usize);
        }
        if off + data.len() as u64 > self.read && !data.is_empty() {
            let keep = self.pending.get(&off).is_none_or(|d| d.len() < data.len());
            if keep {
                self.pending.insert(off, data);
            }
        }
        let mut out = vec![];
        while let Some((&k, _)) = self.pending.iter().next() {
            if k > self.read {
                break;
            }
            let d = self.pending.remove(&k).unwrap();
            let dend = k + d.len() as u64;
            if dend > self.read {
                out.extend_from_slice(&d[(self.read - k) as usize..]);
                self.read = dend;
            }
        }
        let fin_now = !self.fin_delivered && self.fin == Some(self.read);
        if fin_now {
            self.fin_delivered = true;
        }
        (out, fin_now)
    }
}

// ------------------------------------------------------------- conn

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ConnEvent {
    StreamOpened(u32),
    StreamData(u32, Vec<u8>),
    StreamFin(u32),
    StreamReset(u32),
    /// Bytes of a local stream acknowledged by the peer (frees send buffer).
    StreamAcked(u32, usize),
    Datagram(Vec<u8>),
    Closed(&'static str),
}

#[derive(Debug, Clone, Copy)]
enum Retx {
    Stream {
        id: u32,
        off: u64,
        len: u16,
        fin: bool,
    },
    MaxData {
        id: u32,
    },
    Reset {
        id: u32,
    },
    Close,
}

struct Sent {
    time: Instant,
    size: usize,
    eliciting: bool,
    frames: Vec<Retx>,
}

#[derive(Debug, Default, Clone, Copy)]
pub struct Stats {
    pub sent_packets: u64,
    pub recv_packets: u64,
    pub lost_packets: u64,
    pub sent_bytes: u64,
    pub recv_bytes: u64,
}

pub struct Conn {
    is_client: bool,
    next_pn: u64,
    sent: BTreeMap<u64, Sent>,
    largest_acked: Option<u64>,
    srtt: Option<Duration>,
    rttvar: Duration,
    min_rtt: Option<Duration>,
    latest_rtt: Duration,
    last_delay_cut: Option<Instant>,
    pto_count: u32,
    probes: u32,
    loss_time: Option<Instant>,
    cwnd: usize,
    ssthresh: usize,
    in_flight: usize,
    recovery_start: Option<Instant>,
    received: RangeSet,
    largest_recv: Option<(u64, Instant)>,
    ack_pending: bool,
    eliciting_unacked: u32,
    ack_deadline: Option<Instant>,
    send: HashMap<u32, SendStream>,
    recv: HashMap<u32, RecvStream>,
    order: VecDeque<u32>,
    finished: std::collections::HashSet<u32>,
    ctrl: VecDeque<Frame>,
    datagrams: VecDeque<(u32, Vec<u8>)>,
    dgram_seq: u32,
    /// Last small datagram sent: piggybacked into the next packet, or re-sent
    /// once after `REDUNDANT_TAIL` if nothing follows (anti-stutter redundancy).
    last_small: Option<(u32, Vec<u8>, Instant, bool)>,
    seen_dgrams: std::collections::HashSet<u32>,
    seen_order: VecDeque<u32>,
    events: VecDeque<ConnEvent>,
    next_stream: u32,
    last_recv: Instant,
    last_send: Instant,
    closed: bool,
    close_queued: bool,
    pub stats: Stats,
}

impl Conn {
    pub fn new(is_client: bool, now: Instant) -> Self {
        Conn {
            is_client,
            next_pn: 0,
            sent: BTreeMap::new(),
            largest_acked: None,
            srtt: None,
            rttvar: INIT_RTT / 2,
            min_rtt: None,
            latest_rtt: INIT_RTT,
            last_delay_cut: None,
            pto_count: 0,
            probes: 0,
            loss_time: None,
            cwnd: INIT_CWND,
            ssthresh: usize::MAX,
            in_flight: 0,
            recovery_start: None,
            received: RangeSet::default(),
            largest_recv: None,
            ack_pending: false,
            eliciting_unacked: 0,
            ack_deadline: None,
            send: HashMap::new(),
            recv: HashMap::new(),
            order: VecDeque::new(),
            finished: Default::default(),
            ctrl: VecDeque::new(),
            datagrams: VecDeque::new(),
            dgram_seq: 0,
            last_small: None,
            seen_dgrams: Default::default(),
            seen_order: VecDeque::new(),
            events: VecDeque::new(),
            next_stream: if is_client { 1 } else { 2 },
            last_recv: now,
            last_send: now,
            closed: false,
            close_queued: false,
            stats: Stats::default(),
        }
    }

    pub fn rtt(&self) -> Duration {
        self.srtt.unwrap_or(INIT_RTT)
    }
    pub fn cwnd(&self) -> usize {
        self.cwnd
    }
    pub fn is_closed(&self) -> bool {
        self.closed
    }

    // ---------------- app API

    /// Allocate a new locally-initiated stream id (client odd, server even).
    pub fn open_stream(&mut self) -> u32 {
        let id = self.next_stream;
        self.next_stream += 2;
        id
    }

    /// Queue bytes; returns how many were accepted (send buffer limit).
    pub fn stream_write(&mut self, id: u32, data: &[u8]) -> usize {
        if self.closed {
            return 0;
        }
        let s = self.send.entry(id).or_default();
        self.recv.entry(id).or_default();
        if s.fin.is_some() {
            return 0;
        }
        let n = data.len().min(SEND_BUF.saturating_sub(s.buf.len()));
        s.buf.extend(&data[..n]);
        self.schedule(id);
        n
    }

    /// SDK channel streams (high bit) jump ahead of bulk tunnel streams.
    fn schedule(&mut self, id: u32) {
        if !self.order.contains(&id) {
            if id & 0x8000_0000 != 0 {
                self.order.push_front(id);
            } else {
                self.order.push_back(id);
            }
        }
    }

    pub fn stream_send_buffered(&self, id: u32) -> usize {
        self.send.get(&id).map_or(0, |s| s.buf.len())
    }

    pub fn stream_finish(&mut self, id: u32) {
        let s = self.send.entry(id).or_default();
        if s.fin.is_none() {
            s.fin = Some(s.end());
            s.fin_pending = true;
            if !self.order.contains(&id) {
                self.order.push_back(id);
            }
        }
    }

    pub fn stream_reset(&mut self, id: u32) {
        self.finished.insert(id);
        self.send.remove(&id);
        self.recv.remove(&id);
        self.order.retain(|&x| x != id);
        self.ctrl.push_back(Frame::Reset { id });
    }

    /// App consumed `n` delivered bytes: grow the peer's window.
    pub fn stream_consumed(&mut self, id: u32, n: usize) {
        if let Some(r) = self.recv.get_mut(&id) {
            r.consumed += n as u64;
            if r.advertised - r.consumed < RECV_WINDOW / 2 {
                r.advertised = r.consumed + RECV_WINDOW;
                self.ctrl.push_back(Frame::MaxData {
                    id,
                    max: r.advertised,
                });
            }
        }
    }

    pub fn send_datagram(&mut self, d: Vec<u8>) -> bool {
        if d.len() > MAX_DATAGRAM || self.closed {
            return false;
        }
        if self.datagrams.len() > 1024 {
            self.datagrams.pop_front(); // stale realtime data is worthless
        }
        self.dgram_seq = self.dgram_seq.wrapping_add(1);
        self.datagrams.push_back((self.dgram_seq, d));
        true
    }

    pub fn close(&mut self) {
        if !self.close_queued {
            self.close_queued = true;
            self.ctrl.push_back(Frame::Close { code: 0 });
        }
    }

    /// Queue an ack-eliciting keepalive (also confirms a new path).
    pub fn ping(&mut self) {
        self.ctrl.push_back(Frame::Ping);
    }

    pub fn poll_event(&mut self) -> Option<ConnEvent> {
        self.events.pop_front()
    }

    // ---------------- input

    pub fn on_packet(&mut self, pn: u64, payload: &[u8], now: Instant) {
        if self.closed {
            return;
        }
        let Some(frames) = Frame::decode_all(payload) else {
            return;
        };
        self.last_recv = now;
        self.stats.recv_packets += 1;
        self.stats.recv_bytes += payload.len() as u64;
        if self.received.contains(pn) {
            return;
        }
        self.received.insert(pn, pn + 1);
        while self.received.len() > MAX_ACK_RANGES {
            self.received.pop_smallest();
        }
        let out_of_order = self.largest_recv.is_some_and(|(l, _)| pn < l);
        if self.largest_recv.is_none_or(|(l, _)| pn > l) {
            self.largest_recv = Some((pn, now));
        }
        let eliciting = frames.iter().any(|f| !matches!(f, Frame::Ack { .. }));
        if eliciting {
            self.ack_pending = true;
            self.eliciting_unacked += 1;
            if out_of_order {
                self.eliciting_unacked += 1;
            }
            self.ack_deadline.get_or_insert(now + ACK_DELAY);
        }
        for f in frames {
            match f {
                Frame::Ping => {}
                Frame::Ack { delay_ms, ranges } => self.on_ack(ranges, delay_ms, now),
                Frame::Stream { id, off, fin, data } => {
                    if !self.recv.contains_key(&id) {
                        if self.finished.contains(&id)
                            || (self.is_local(id) && !self.send.contains_key(&id))
                        {
                            continue; // stale frame for a stream we already finished
                        }
                        self.recv.insert(id, RecvStream::default());
                        self.send.entry(id).or_default();
                        if !self.is_local(id) {
                            self.events.push_back(ConnEvent::StreamOpened(id));
                        }
                    }
                    let r = self.recv.get_mut(&id).unwrap();
                    let (bytes, fin_now) = r.on_data(off, data, fin);
                    if !bytes.is_empty() {
                        self.events.push_back(ConnEvent::StreamData(id, bytes));
                    }
                    if fin_now {
                        self.events.push_back(ConnEvent::StreamFin(id));
                        self.gc(id);
                    }
                }
                Frame::MaxData { id, max } => {
                    if let Some(s) = self.send.get_mut(&id) {
                        s.max_data = s.max_data.max(max);
                        if s.has_pending() {
                            self.schedule(id);
                        }
                    }
                }
                Frame::Reset { id } => {
                    self.finished.insert(id);
                    let existed = self.send.remove(&id).is_some() | self.recv.remove(&id).is_some();
                    self.order.retain(|&x| x != id);
                    if existed {
                        self.events.push_back(ConnEvent::StreamReset(id));
                    }
                }
                Frame::Datagram { seq, data } => {
                    if self.seen_dgrams.insert(seq) {
                        self.seen_order.push_back(seq);
                        if self.seen_order.len() > 4096 {
                            if let Some(old) = self.seen_order.pop_front() {
                                self.seen_dgrams.remove(&old);
                            }
                        }
                        self.events.push_back(ConnEvent::Datagram(data));
                    }
                }
                Frame::Close { .. } => {
                    self.closed = true;
                    self.events.push_back(ConnEvent::Closed("peer closed"));
                }
            }
        }
    }

    fn is_local(&self, id: u32) -> bool {
        id & 0x8000_0000 == 0 && (id % 2 == 1) == self.is_client
    }

    fn gc(&mut self, id: u32) {
        let recv_done = self.recv.get(&id).is_none_or(|r| r.fin_delivered);
        let send_done = self.send.get(&id).is_none_or(|s| s.done());
        if recv_done && send_done {
            self.finished.insert(id);
            self.recv.remove(&id);
            self.send.remove(&id);
            self.order.retain(|&x| x != id);
        }
    }

    fn on_ack(&mut self, ranges: Vec<(u64, u64)>, delay_ms: u16, now: Instant) {
        let mut newly: Vec<(u64, Sent)> = vec![];
        for (s, e) in ranges {
            let pns: Vec<u64> = self.sent.range(s..e).map(|(&k, _)| k).collect();
            for pn in pns {
                newly.push((pn, self.sent.remove(&pn).unwrap()));
            }
        }
        if newly.is_empty() {
            return;
        }
        let largest = newly.iter().map(|(pn, _)| *pn).max().unwrap();
        if self.largest_acked.is_none_or(|l| largest > l) {
            self.largest_acked = Some(largest);
            if let Some((_, sent)) = newly.iter().find(|(pn, _)| *pn == largest) {
                if sent.eliciting {
                    let mut sample = now.saturating_duration_since(sent.time);
                    let delay = Duration::from_millis(delay_ms as u64);
                    if sample > delay + Duration::from_millis(1) {
                        sample -= delay;
                    }
                    self.update_rtt(sample);
                }
            }
        }
        let queueing = self.queue_building();
        if queueing
            && self
                .last_delay_cut
                .is_none_or(|t| now.duration_since(t) >= self.rtt())
        {
            // Delay signal: back off by 1/8 once per RTT and leave slow start.
            self.last_delay_cut = Some(now);
            self.cwnd = (self.cwnd - self.cwnd / 16).max(MIN_CWND);
            self.ssthresh = self.cwnd;
        }
        let mut acked_per_stream: HashMap<u32, usize> = HashMap::new();
        for (_, sent) in &newly {
            if sent.eliciting {
                self.in_flight = self.in_flight.saturating_sub(sent.size);
                // NewReno: no growth for packets sent before recovery started,
                // and no growth while the bottleneck queue is building.
                if !queueing && self.recovery_start.is_none_or(|r| sent.time > r) {
                    if self.cwnd < self.ssthresh {
                        self.cwnd += sent.size;
                    } else {
                        // Path not queueing: probe faster than Reno (≈4 MSS/RTT).
                        self.cwnd += 4 * MAX_PAYLOAD * sent.size / self.cwnd;
                    }
                }
            }
            for f in &sent.frames {
                if let Retx::Stream { id, off, len, fin } = *f {
                    if let Some(s) = self.send.get_mut(&id) {
                        let freed = s.on_ack(off, len as u64, fin);
                        if freed > 0 {
                            *acked_per_stream.entry(id).or_default() += freed;
                        }
                    }
                }
            }
        }
        for (id, n) in acked_per_stream {
            self.events.push_back(ConnEvent::StreamAcked(id, n));
        }
        let ids: Vec<u32> = self
            .send
            .iter()
            .filter(|(_, s)| s.done())
            .map(|(&id, _)| id)
            .collect();
        for id in ids {
            self.gc(id);
        }
        self.pto_count = 0;
        self.detect_loss(now);
    }

    /// True when RTT is clearly above the path's base RTT (a queue is forming).
    fn queue_building(&self) -> bool {
        let Some(min) = self.min_rtt else {
            return false;
        };
        let target = (min / 4)
            .max(Duration::from_millis(5))
            .min(Duration::from_millis(20));
        self.latest_rtt > min + target
    }

    pub fn min_rtt(&self) -> Option<Duration> {
        self.min_rtt
    }

    fn update_rtt(&mut self, sample: Duration) {
        self.latest_rtt = sample;
        self.min_rtt = Some(self.min_rtt.map_or(sample, |m| m.min(sample)));
        match self.srtt {
            None => {
                self.srtt = Some(sample);
                self.rttvar = sample / 2;
            }
            Some(srtt) => {
                let diff = srtt.abs_diff(sample);
                self.rttvar = (self.rttvar * 3 + diff) / 4;
                self.srtt = Some((srtt * 7 + sample) / 8);
            }
        }
    }

    fn loss_delay(&self) -> Duration {
        (self.rtt() * 9 / 8).max(Duration::from_millis(2))
    }

    fn detect_loss(&mut self, now: Instant) {
        self.loss_time = None;
        let Some(largest) = self.largest_acked else {
            return;
        };
        let delay = self.loss_delay();
        let mut lost = vec![];
        for (&pn, sent) in self.sent.range(..largest) {
            if largest - pn >= 3 || sent.time + delay <= now {
                lost.push(pn);
            } else {
                let t = sent.time + delay;
                self.loss_time = Some(self.loss_time.map_or(t, |x| x.min(t)));
            }
        }
        let mut congestion = false;
        for pn in lost {
            let sent = self.sent.remove(&pn).unwrap();
            self.stats.lost_packets += 1;
            if sent.eliciting {
                self.in_flight = self.in_flight.saturating_sub(sent.size);
                if self.recovery_start.is_none_or(|r| sent.time > r) {
                    congestion = true;
                }
            }
            self.requeue(&sent.frames);
        }
        if congestion {
            self.recovery_start = Some(now);
            self.cwnd = (self.cwnd / 2).max(MIN_CWND);
            self.ssthresh = self.cwnd;
        }
    }

    fn requeue(&mut self, frames: &[Retx]) {
        for f in frames {
            match *f {
                Retx::Stream { id, off, len, fin } => {
                    if let Some(s) = self.send.get_mut(&id) {
                        s.on_lost(off, len as u64, fin);
                        self.schedule(id);
                    }
                }
                Retx::MaxData { id } => {
                    if let Some(r) = self.recv.get(&id) {
                        self.ctrl.push_back(Frame::MaxData {
                            id,
                            max: r.advertised,
                        });
                    }
                }
                Retx::Reset { id } => self.ctrl.push_back(Frame::Reset { id }),
                Retx::Close => self.ctrl.push_back(Frame::Close { code: 0 }),
            }
        }
    }

    // ---------------- timers

    fn pto_deadline(&self) -> Option<Instant> {
        let last = self.sent.values().rev().find(|s| s.eliciting)?.time;
        let pto = self.rtt() + (self.rttvar * 4).max(Duration::from_millis(1)) + ACK_DELAY;
        Some(last + pto * 2u32.pow(self.pto_count.min(6)))
    }

    pub fn poll_timeout(&self) -> Option<Instant> {
        if self.closed {
            return None;
        }
        let tail = self
            .last_small
            .as_ref()
            .filter(|(_, _, _, r)| !r)
            .map(|(_, _, t, _)| *t + REDUNDANT_TAIL);
        [
            tail,
            self.ack_deadline,
            self.loss_time,
            self.pto_deadline(),
            Some(self.last_recv + IDLE_TIMEOUT),
            Some(self.last_send + KEEPALIVE),
        ]
        .into_iter()
        .flatten()
        .min()
    }

    pub fn on_timeout(&mut self, now: Instant) {
        if self.closed {
            return;
        }
        if now >= self.last_recv + IDLE_TIMEOUT {
            self.closed = true;
            self.events.push_back(ConnEvent::Closed("timeout"));
            return;
        }
        if self.loss_time.is_some_and(|t| now >= t) {
            self.detect_loss(now);
        }
        if self.pto_deadline().is_some_and(|t| now >= t) {
            self.pto_count += 1;
            self.probes = 2;
            // Retransmit the oldest outstanding data as the probe.
            if let Some((&pn, _)) = self.sent.iter().find(|(_, s)| s.eliciting) {
                let sent = self.sent.remove(&pn).unwrap();
                self.in_flight = self.in_flight.saturating_sub(sent.size);
                if sent.frames.is_empty() {
                    self.ctrl.push_back(Frame::Ping);
                }
                self.requeue(&sent.frames);
            }
            if self.pto_count >= 6 {
                self.cwnd = MIN_CWND;
            }
            // Peer silent through repeated probes (≈5–8 s): declare it dead so the
            // app can fail over to another instance instead of waiting 30 s.
            if self.pto_count >= DEAD_AFTER_PTOS
                && now.duration_since(self.last_recv) >= DEAD_MIN_SILENCE
            {
                self.closed = true;
                self.events
                    .push_back(ConnEvent::Closed("server không phản hồi (mất kết nối)"));
                return;
            }
        }
        if now >= self.last_send + KEEPALIVE {
            self.ctrl.push_back(Frame::Ping);
        }
    }

    // ---------------- output

    /// Next plaintext packet to encrypt & send: (packet number, payload).
    pub fn poll_transmit(&mut self, now: Instant) -> Option<(u64, Vec<u8>)> {
        if self.closed && !self.close_queued {
            return None;
        }
        let mut out = Vec::with_capacity(MAX_PAYLOAD);
        let mut frames: Vec<Retx> = vec![];
        let mut eliciting = false;

        let ack_due = self.ack_pending
            && (self.eliciting_unacked >= 2 || self.ack_deadline.is_some_and(|t| now >= t));
        let cwnd_ok = self.in_flight + MAX_PAYLOAD <= self.cwnd || self.probes > 0;
        let have_data = cwnd_ok
            && self
                .order
                .iter()
                .any(|id| self.send.get(id).is_some_and(|s| s.has_pending()));
        let tail_due = self.datagrams.is_empty()
            && self
                .last_small
                .as_ref()
                .is_some_and(|(_, _, t, resent)| !resent && now >= *t + REDUNDANT_TAIL);
        let want =
            ack_due || !self.ctrl.is_empty() || !self.datagrams.is_empty() || have_data || tail_due;
        if !want {
            return None;
        }

        if self.ack_pending {
            let ranges: Vec<(u64, u64)> = self.received.iter().rev().take(MAX_ACK_RANGES).collect();
            let delay = self.largest_recv.map_or(0, |(_, t)| {
                now.saturating_duration_since(t)
                    .as_millis()
                    .min(u16::MAX as u128) as u16
            });
            Frame::Ack {
                delay_ms: delay,
                ranges,
            }
            .encode(&mut out);
            self.ack_pending = false;
            self.eliciting_unacked = 0;
            self.ack_deadline = None;
        }

        while let Some(f) = self.ctrl.front() {
            let mut tmp = vec![];
            f.encode(&mut tmp);
            if out.len() + tmp.len() > MAX_PAYLOAD {
                break;
            }
            let f = self.ctrl.pop_front().unwrap();
            frames.extend(match f {
                Frame::MaxData { id, .. } => Some(Retx::MaxData { id }),
                Frame::Reset { id } => Some(Retx::Reset { id }),
                Frame::Close { .. } => Some(Retx::Close),
                _ => None,
            });
            if matches!(f, Frame::Close { .. }) {
                self.closed = true;
            }
            out.extend_from_slice(&tmp);
            eliciting = true;
        }

        while let Some((_, d)) = self.datagrams.front() {
            if out.len() + 7 + d.len() > MAX_PAYLOAD {
                break;
            }
            let (seq, data) = self.datagrams.pop_front().unwrap();
            // Redundancy: carry a copy of the previous small datagram, so one lost
            // packet costs ~1 tick of delay instead of a missing game update.
            if let Some((pseq, pdata, _, _)) = &self.last_small {
                if out.len() + 14 + data.len() + pdata.len() <= MAX_PAYLOAD {
                    Frame::Datagram {
                        seq: *pseq,
                        data: pdata.clone(),
                    }
                    .encode(&mut out);
                }
            }
            self.last_small =
                (data.len() <= REDUNDANT_MAX).then(|| (seq, data.clone(), now, false));
            Frame::Datagram { seq, data }.encode(&mut out);
            eliciting = true;
        }
        if tail_due {
            if let Some((seq, data, _, resent)) = &mut self.last_small {
                if out.len() + 7 + data.len() <= MAX_PAYLOAD {
                    Frame::Datagram {
                        seq: *seq,
                        data: data.clone(),
                    }
                    .encode(&mut out);
                    *resent = true;
                    eliciting = true;
                }
            }
        }

        if cwnd_ok {
            let mut rounds = self.order.len();
            while rounds > 0 && out.len() + STREAM_HDR + 1 < MAX_PAYLOAD {
                rounds -= 1;
                let Some(id) = self.order.pop_front() else {
                    break;
                };
                let Some(s) = self.send.get_mut(&id) else {
                    continue;
                };
                let room = MAX_PAYLOAD - out.len() - STREAM_HDR;
                if let Some((off, data, fin)) = s.next_chunk(room) {
                    let len = data.len() as u16;
                    Frame::Stream { id, off, fin, data }.encode(&mut out);
                    frames.push(Retx::Stream { id, off, len, fin });
                    eliciting = true;
                    rounds += 1; // stream may have more; keep filling
                }
                if s.has_pending() {
                    self.order.push_back(id);
                }
            }
        }

        if out.is_empty() {
            return None;
        }
        let pn = self.next_pn;
        self.next_pn += 1;
        if eliciting {
            self.in_flight += out.len();
            self.probes = self.probes.saturating_sub(1);
            self.last_send = now;
        }
        self.stats.sent_packets += 1;
        self.stats.sent_bytes += out.len() as u64;
        self.sent.insert(
            pn,
            Sent {
                time: now,
                size: out.len(),
                eliciting,
                frames,
            },
        );
        // ACK-only packets are never acked back; don't track them forever.
        if !eliciting {
            self.sent.remove(&pn);
        }
        Some((pn, out))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn rangeset_ops() {
        let mut r = RangeSet::default();
        r.insert(10, 20);
        r.insert(30, 40);
        r.insert(20, 30);
        assert_eq!(r.iter().collect::<Vec<_>>(), vec![(10, 40)]);
        r.remove(15, 25);
        assert_eq!(r.iter().collect::<Vec<_>>(), vec![(10, 15), (25, 40)]);
        assert!(r.contains(10) && !r.contains(15) && r.contains(39) && !r.contains(40));
        r.insert(0, 100);
        assert_eq!(r.len(), 1);
    }

    #[test]
    fn frames_roundtrip() {
        let fs = vec![
            Frame::Ping,
            Frame::Ack {
                delay_ms: 3,
                ranges: vec![(10, 12), (1, 5)],
            },
            Frame::Stream {
                id: 7,
                off: 99,
                fin: true,
                data: b"abc".to_vec(),
            },
            Frame::MaxData {
                id: 7,
                max: 1 << 30,
            },
            Frame::Reset { id: 3 },
            Frame::Datagram {
                seq: 9,
                data: b"udp".to_vec(),
            },
            Frame::Close { code: 0 },
        ];
        let mut b = vec![];
        for f in &fs {
            f.encode(&mut b);
        }
        assert_eq!(Frame::decode_all(&b).unwrap(), fs);
    }

    /// Deterministic PRNG for the network simulator.
    struct Lcg(u64);
    impl Lcg {
        fn next(&mut self) -> u64 {
            self.0 = self
                .0
                .wrapping_mul(6364136223846793005)
                .wrapping_add(1442695040888963407);
            self.0 >> 33
        }
    }

    struct InFlight {
        at: Instant,
        to_b: bool,
        pn: u64,
        data: Vec<u8>,
    }

    /// Simulate a lossy, reordering, delayed link; transfer data both ways.
    fn simulate(loss_pct: u64, size: usize, seed: u64, jitter_us: u64) -> (Duration, Stats) {
        let t0 = Instant::now();
        let mut now = t0;
        let mut a = Conn::new(true, now);
        let mut b = Conn::new(false, now);
        let mut rng = Lcg(seed);
        let payload: Vec<u8> = (0..size).map(|i| (i * 31 % 251) as u8).collect();
        let sid = a.open_stream();
        let mut written_a = 0;
        let mut b_got: Vec<u8> = vec![];
        let mut a_got: Vec<u8> = vec![];
        let mut b_fin = false;
        let mut a_fin = false;
        let mut b_replied = false;
        let mut dgrams = 0;
        let mut net: Vec<InFlight> = vec![];
        a.send_datagram(b"realtime".to_vec());
        for _ in 0..2_000_000 {
            if written_a < payload.len() {
                written_a += a.stream_write(sid, &payload[written_a..]);
                if written_a == payload.len() {
                    a.stream_finish(sid);
                }
            }
            if b_fin && !b_replied {
                assert_eq!(b.stream_write(sid, b"pong-reply"), 10);
                b.stream_finish(sid);
                b_replied = true;
            }
            for (conn, to_b) in [(&mut a, true), (&mut b, false)] {
                while let Some((pn, data)) = conn.poll_transmit(now) {
                    if rng.next() % 100 < loss_pct {
                        continue;
                    }
                    let jitter = Duration::from_micros(if jitter_us > 0 {
                        rng.next() % jitter_us
                    } else {
                        0
                    });
                    net.push(InFlight {
                        at: now + Duration::from_millis(20) + jitter,
                        to_b,
                        pn,
                        data,
                    });
                }
            }
            // Deliver everything due.
            net.sort_by_key(|p| p.at);
            let due: Vec<InFlight> = {
                let k = net.iter().take_while(|p| p.at <= now).count();
                net.drain(..k).collect()
            };
            let delivered_any = !due.is_empty();
            for p in due {
                if p.to_b {
                    b.on_packet(p.pn, &p.data, now)
                } else {
                    a.on_packet(p.pn, &p.data, now)
                }
            }
            while let Some(e) = b.poll_event() {
                match e {
                    ConnEvent::StreamData(id, d) => {
                        assert_eq!(id, sid);
                        b.stream_consumed(id, d.len());
                        b_got.extend(d);
                    }
                    ConnEvent::StreamFin(_) => b_fin = true,
                    ConnEvent::Datagram(_) => dgrams += 1,
                    _ => {}
                }
            }
            while let Some(e) = a.poll_event() {
                match e {
                    ConnEvent::StreamData(_, d) => a_got.extend(d),
                    ConnEvent::StreamFin(_) => a_fin = true,
                    _ => {}
                }
            }
            if a_fin && b_fin {
                assert_eq!(b_got, payload, "data corrupted");
                assert_eq!(a_got, b"pong-reply");
                assert!(dgrams <= 1);
                return (now - t0, a.stats);
            }
            if std::env::var("SIMDBG").is_ok() && (now - t0).as_millis() % 1000 < 1 {
                eprintln!(
                    "t={:?} cwnd={} inflight={} sent={} b_got={} rtt={:?} order={:?} pending={:?}",
                    now - t0,
                    a.cwnd,
                    a.in_flight,
                    a.sent.len(),
                    b_got.len(),
                    a.rtt(),
                    a.order,
                    a.send
                        .get(&sid)
                        .map(|s| (s.base, s.next, s.end(), s.max_data, s.lost.len()))
                );
            }
            if delivered_any {
                continue; // let both sides react (transmit) at the same instant
            }
            // Advance virtual time to the next interesting moment.
            let next = [
                a.poll_timeout(),
                b.poll_timeout(),
                net.iter().map(|p| p.at).min(),
            ]
            .into_iter()
            .flatten()
            .min()
            .unwrap_or(now + Duration::from_millis(1));
            now = next.max(now + Duration::from_micros(50));
            a.on_timeout(now);
            b.on_timeout(now);
            assert!(!a.is_closed() && !b.is_closed(), "connection died");
        }
        panic!("transfer did not finish");
    }

    /// Bulk upload through a 1 MB/s bottleneck (20 ms one-way) while a game
    /// sends a 50 ms heartbeat datagram. Measures the heartbeat's ping.
    fn ping_under_load() -> (f64, f64) {
        let t0 = Instant::now();
        let mut now = t0;
        let mut a = Conn::new(true, now);
        let mut b = Conn::new(false, now);
        let sid = a.open_stream();
        let bulk = vec![7u8; 16 << 20];
        let mut written = 0;
        let mut link_free = now; // a->b bottleneck queue
        let mut net: Vec<InFlight> = vec![];
        let mut next_ping = now;
        let mut pings: Vec<f64> = vec![];
        let mut b_got = 0usize;
        let prop = Duration::from_millis(20);
        let per_byte = Duration::from_nanos(1000); // 1 MB/s
        while now - t0 < Duration::from_secs(8) {
            if written < bulk.len() {
                written += a.stream_write(sid, &bulk[written..]);
            }
            if now >= next_ping {
                next_ping = now + Duration::from_millis(50);
                let ts = (now - t0).as_nanos() as u64;
                a.send_datagram(ts.to_be_bytes().to_vec());
            }
            while let Some((pn, data)) = a.poll_transmit(now) {
                let start = link_free.max(now);
                link_free = start + per_byte * (data.len() as u32 + 29);
                net.push(InFlight {
                    at: link_free + prop,
                    to_b: true,
                    pn,
                    data,
                });
            }
            while let Some((pn, data)) = b.poll_transmit(now) {
                net.push(InFlight {
                    at: now + prop,
                    to_b: false,
                    pn,
                    data,
                });
            }
            net.sort_by_key(|p| p.at);
            let k = net.iter().take_while(|p| p.at <= now).count();
            let due: Vec<InFlight> = net.drain(..k).collect();
            let delivered = !due.is_empty();
            for p in due {
                if p.to_b {
                    b.on_packet(p.pn, &p.data, now)
                } else {
                    a.on_packet(p.pn, &p.data, now)
                }
            }
            while let Some(e) = b.poll_event() {
                match e {
                    ConnEvent::StreamData(id, d) => {
                        b_got += d.len();
                        b.stream_consumed(id, d.len());
                    }
                    ConnEvent::Datagram(d) => {
                        b.send_datagram(d); // echo back
                    }
                    _ => {}
                }
            }
            while let Some(e) = a.poll_event() {
                if let ConnEvent::Datagram(d) = e {
                    let ts = u64::from_be_bytes(d[..8].try_into().unwrap());
                    let sent = t0 + Duration::from_nanos(ts);
                    if now - t0 > Duration::from_secs(2) {
                        pings.push((now - sent).as_secs_f64() * 1000.0);
                    }
                }
            }
            if delivered {
                continue;
            }
            let next = [
                a.poll_timeout(),
                b.poll_timeout(),
                net.iter().map(|p| p.at).min(),
                Some(next_ping),
            ]
            .into_iter()
            .flatten()
            .min()
            .unwrap();
            now = next.max(now + Duration::from_micros(50));
            a.on_timeout(now);
            b.on_timeout(now);
        }
        let goodput = b_got as f64 / 8.0 / 1e6; // MB/s over 8 s
        let avg = pings.iter().sum::<f64>() / pings.len().max(1) as f64;
        assert!(!pings.is_empty());
        (avg, goodput)
    }

    #[test]
    fn low_latency_under_bulk_load() {
        let (avg_ping, goodput) = ping_under_load();
        eprintln!("ping under load: avg {avg_ping:.1} ms (base 40 ms), goodput {goodput:.2} MB/s");
        assert!(avg_ping < 65.0, "queueing delay too high: {avg_ping:.1} ms");
        assert!(goodput > 0.5, "bandwidth wasted: {goodput:.2} MB/s");
    }

    /// 60 Hz game updates over a 10%-loss link: redundancy must recover most losses.
    #[test]
    fn game_datagrams_survive_loss_with_redundancy() {
        let t0 = Instant::now();
        let mut now = t0;
        let mut a = Conn::new(true, now);
        let mut b = Conn::new(false, now);
        let mut rng = Lcg(77);
        let mut got = std::collections::HashSet::new();
        let mut dup = 0;
        let mut net: Vec<InFlight> = vec![];
        let total = 600u32; // 10 s at 60 Hz
        let mut next_tick = now;
        let mut sent = 0u32;
        while now - t0 < Duration::from_secs(12) {
            if sent < total && now >= next_tick {
                a.send_datagram(sent.to_be_bytes().repeat(8));
                sent += 1;
                next_tick += Duration::from_micros(16_667);
            }
            for (conn, to_b) in [(&mut a, true), (&mut b, false)] {
                while let Some((pn, data)) = conn.poll_transmit(now) {
                    if rng.next() % 100 < 10 {
                        continue;
                    }
                    net.push(InFlight {
                        at: now + Duration::from_millis(20),
                        to_b,
                        pn,
                        data,
                    });
                }
            }
            net.sort_by_key(|p| p.at);
            let k = net.iter().take_while(|p| p.at <= now).count();
            for p in net.drain(..k).collect::<Vec<_>>() {
                if p.to_b {
                    b.on_packet(p.pn, &p.data, now)
                } else {
                    a.on_packet(p.pn, &p.data, now)
                }
            }
            while let Some(e) = b.poll_event() {
                if let ConnEvent::Datagram(d) = e {
                    if !got.insert(u32::from_be_bytes(d[..4].try_into().unwrap())) {
                        dup += 1;
                    }
                }
            }
            let next = [
                a.poll_timeout(),
                b.poll_timeout(),
                net.iter().map(|p| p.at).min(),
                Some(next_tick),
            ]
            .into_iter()
            .flatten()
            .min()
            .unwrap();
            now = next.max(now + Duration::from_micros(50));
            a.on_timeout(now);
            b.on_timeout(now);
        }
        let ratio = got.len() as f64 / total as f64;
        eprintln!("game datagrams at 10% packet loss: {:.1}% delivered (without redundancy ≈ 90%), duplicates surfaced: {dup}", ratio * 100.0);
        assert!(ratio >= 0.97, "only {:.1}% delivered", ratio * 100.0);
        assert_eq!(dup, 0, "duplicates must be filtered");
    }

    #[test]
    fn dead_peer_detected_fast() {
        let t0 = Instant::now();
        let mut now = t0;
        let mut a = Conn::new(true, now);
        // Establish an RTT, then the peer vanishes while we keep sending.
        let mut b = Conn::new(false, now);
        a.send_datagram(vec![1]);
        let sid = a.open_stream();
        a.stream_write(sid, b"hello");
        for _ in 0..3 {
            while let Some((pn, d)) = a.poll_transmit(now) {
                b.on_packet(pn, &d, now + Duration::from_millis(20));
            }
            now += Duration::from_millis(40);
            while let Some((pn, d)) = b.poll_transmit(now) {
                a.on_packet(pn, &d, now);
            }
        }
        a.stream_write(sid, &[7u8; 5000]); // unacked data, peer gone
        let died_at = loop {
            while a.poll_transmit(now).is_some() {}
            now = a.poll_timeout().unwrap();
            a.on_timeout(now);
            if a.is_closed() {
                break now - t0;
            }
            assert!(now - t0 < Duration::from_secs(20), "not detected");
        };
        eprintln!("dead peer detected after {died_at:?}");
        assert!(died_at < Duration::from_secs(12), "{died_at:?}");
    }

    #[test]
    fn transfer_reordering_link() {
        let (t, _) = simulate(0, 1 << 20, 3, 8000);
        assert!(t < Duration::from_secs(20), "too slow: {t:?}");
    }

    #[test]
    fn transfer_clean_link() {
        let (t, st) = simulate(0, 2 << 20, 1, 0);
        assert_eq!(st.lost_packets, 0);
        assert!(t < Duration::from_secs(10), "too slow: {t:?}");
    }

    #[test]
    fn transfer_with_10pct_loss() {
        let (_, st) = simulate(10, 1 << 20, 7, 8000);
        assert!(st.lost_packets > 0);
    }

    #[test]
    fn transfer_with_30pct_loss() {
        simulate(30, 200_000, 42, 8000);
    }
}
