// Peerova — direct P2P networking (no relay, end-to-end encrypted). Swift wrapper over the C ABI.
//
//     let server = try PeerovaServer(config: ["port": 0])      // blocks ≤ ~3 s — not on the main thread
//     let client = try PeerovaClient(ticket: server.ticket)
//     if case .connected(let peer)? = client.poll(timeout: 10) {
//         try client.send("hello", to: peer)
//     }
//
// Threading: each node has an internal mutex, so calls are safe from any thread, but
// `poll(timeout:)` holds that mutex while it waits — keep timeouts short if another
// thread sends on the same node. Do not call `close()` while another thread is inside
// `poll`/`send` on the same node (the `events()` stream is stopped by `close()` itself).
import CPeerova
import Foundation

/// An event from `poll`. Payloads are copied out of the native buffer immediately.
public enum PeerovaEvent: Equatable {
    /// Server: a client connected. Client: connected to the server.
    case connected(peer: UInt32)
    case disconnected(peer: UInt32, reason: String)
    case message(peer: UInt32, channel: UInt8, reliable: Bool, data: Data)
    /// Client only: the connection attempt failed.
    case connectFailed(reason: String)
}

public struct PeerovaError: Error, CustomStringConvertible {
    public let message: String
    public var description: String { message }
    static func last(_ fallback: String) -> PeerovaError {
        if let p = peerova_last_error() {
            let s = String(cString: p)
            if !s.isEmpty { return PeerovaError(message: s) }
        }
        return PeerovaError(message: fallback)
    }
}

/// Library version, e.g. "0.1.0".
public var peerovaVersion: String {
    guard let p = peerova_version() else { return "" }
    return String(cString: p)
}

/// Largest payload accepted by `send(..., reliable: false)`.
public let peerovaMaxUnreliablePayload = 1180

/// Common base of `PeerovaServer` and `PeerovaClient`.
public class PeerovaNode: @unchecked Sendable {
    private var handle: OpaquePointer?
    private let stateLock = NSLock()
    private var streamStop: (() -> Void)?

    fileprivate init(handle: OpaquePointer?, what: String) throws {
        guard let h = handle else { throw PeerovaError.last("\(what) failed") }
        self.handle = h
    }

    deinit { close() }

    fileprivate var raw: OpaquePointer? { handle }

    /// Number of currently connected peers.
    public var peerCount: Int {
        guard let h = handle else { return 0 }
        return Int(peerova_peer_count(h))
    }

    /// Returns the next event, waiting up to `timeout` seconds (0 = non-blocking).
    public func poll(timeout: TimeInterval = 0) -> PeerovaEvent? {
        guard let h = handle else { return nil }
        var ev = peerova_event()
        let got: Int32
        if timeout > 0 {
            let ms = UInt32(min(timeout * 1000, Double(UInt32.max)).rounded())
            got = peerova_poll_wait(h, &ev, ms)
        } else {
            got = peerova_poll(h, &ev)
        }
        guard got != 0 else { return nil }
        // event.data is only valid until the next poll — copy now.
        let data: Data
        if ev.len > 0, let p = ev.data {
            data = Data(bytes: p, count: Int(ev.len))
        } else {
            data = Data()
        }
        let text = { String(decoding: data, as: UTF8.self) }
        switch Int(ev.type) {
        case Int(PEEROVA_EVENT_CONNECTED): return .connected(peer: ev.peer)
        case Int(PEEROVA_EVENT_DISCONNECTED): return .disconnected(peer: ev.peer, reason: text())
        case Int(PEEROVA_EVENT_MESSAGE):
            return .message(peer: ev.peer, channel: ev.channel, reliable: ev.reliable != 0, data: data)
        case Int(PEEROVA_EVENT_CONNECT_FAILED): return .connectFailed(reason: text())
        default: return nil
        }
    }

    /// Sends bytes. `reliable`: ordered + retransmitted per channel; otherwise an unreliable
    /// datagram (≤ `peerovaMaxUnreliablePayload` bytes).
    public func send(_ data: Data, to peer: UInt32, channel: UInt8 = 0, reliable: Bool = true) throws {
        guard let h = handle else { throw PeerovaError(message: "node is closed") }
        let rc: Int32 = data.withUnsafeBytes { (buf: UnsafeRawBufferPointer) -> Int32 in
            let p = buf.bindMemory(to: UInt8.self).baseAddress
            return peerova_send(h, peer, channel, p, UInt32(buf.count), reliable ? 1 : 0)
        }
        if rc != 0 { throw PeerovaError.last("send failed") }
    }

    /// Sends a UTF-8 string.
    public func send(_ text: String, to peer: UInt32, channel: UInt8 = 0, reliable: Bool = true) throws {
        try send(Data(text.utf8), to: peer, channel: channel, reliable: reliable)
    }

    public func disconnect(_ peer: UInt32) {
        if let h = handle { peerova_disconnect(h, peer) }
    }

    /// Async stream of events, fed by a dedicated polling thread. Finishes on `close()`
    /// or when the consuming task is cancelled. Only one stream per node at a time.
    public func events(pollInterval: TimeInterval = 0.01) -> AsyncStream<PeerovaEvent> {
        AsyncStream { continuation in
            let stopped = ManagedFlag()
            let done = DispatchSemaphore(value: 0)
            let thread = Thread { [weak self] in
                defer { continuation.finish(); done.signal() }
                while !stopped.value {
                    guard let self = self, self.handle != nil else { return }
                    if let ev = self.poll(timeout: pollInterval) { continuation.yield(ev) }
                }
            }
            thread.name = "peerova-events"
            continuation.onTermination = { _ in stopped.set() }
            stateLock.lock()
            let previous = streamStop
            // If the last reference is dropped on the polling thread itself, deinit runs
            // there — never wait for ourselves.
            streamStop = { stopped.set(); if Thread.current !== thread { done.wait() } }
            stateLock.unlock()
            previous?()
            thread.start()
        }
    }

    /// Frees the native node (also done by `deinit`). Idempotent.
    public func close() {
        stateLock.lock()
        let stop = streamStop
        streamStop = nil
        stateLock.unlock()
        stop?()
        stateLock.lock()
        let h = handle
        handle = nil
        stateLock.unlock()
        if let h = h { peerova_free(h) }
    }
}

/// Hosts a session; clients join with `ticket`.
public final class PeerovaServer: PeerovaNode {
    /// Shareable connection ticket ("pvt_...").
    public let ticket: String

    /// `configJSON` — see peerova.h, e.g. `{"port":47000,"upnp":true,"license":"PVA-..."}`.
    /// Blocks up to ~3 s while the ticket is prepared.
    public init(configJSON: String?) throws {
        let h: OpaquePointer? = configJSON.map { $0.withCString { peerova_server_start($0) } } ?? peerova_server_start(nil)
        let n = h.map { peerova_ticket($0, nil, 0) } ?? 0
        var buf = [CChar](repeating: 0, count: Int(max(n, 1)))
        if let h = h, n > 0 { _ = peerova_ticket(h, &buf, n) }
        ticket = String(cString: buf)
        try super.init(handle: h, what: "peerova_server_start")
    }

    /// Dictionary config, serialized with JSONSerialization.
    public convenience init(config: [String: Any] = [:]) throws {
        let d = try JSONSerialization.data(withJSONObject: config)
        try self.init(configJSON: String(decoding: d, as: UTF8.self))
    }

    /// Starts the server off the calling thread (use this from the main actor / UI code).
    public static func start(config: [String: Any] = [:]) async throws -> PeerovaServer {
        try await withCheckedThrowingContinuation { cont in
            DispatchQueue.global(qos: .userInitiated).async {
                cont.resume(with: Result { try PeerovaServer(config: config) })
            }
        }
    }
}

/// Connects to a server. Returns immediately; wait for `.connected` / `.connectFailed`.
public final class PeerovaClient: PeerovaNode {
    /// `configJSON` e.g. `{"token":"secret","timeout_ms":20000}` (may be nil).
    public init(ticket: String, configJSON: String? = nil) throws {
        let h: OpaquePointer? = ticket.withCString { t in
            if let c = configJSON { return c.withCString { peerova_client_connect(t, $0) } }
            return peerova_client_connect(t, nil)
        }
        try super.init(handle: h, what: "peerova_client_connect")
    }

    public convenience init(ticket: String, config: [String: Any]) throws {
        let d = try JSONSerialization.data(withJSONObject: config)
        try self.init(ticket: ticket, configJSON: String(decoding: d, as: UTF8.self))
    }
}

/// Tiny thread-safe boolean.
private final class ManagedFlag: @unchecked Sendable {
    private let lock = NSLock()
    private var v = false
    var value: Bool { lock.lock(); defer { lock.unlock() }; return v }
    func set() { lock.lock(); v = true; lock.unlock() }
}
