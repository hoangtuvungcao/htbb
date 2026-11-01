// Peerova — direct P2P networking (end-to-end encrypted). Swift wrapper over the Peerova C API.
//
//     let server = try PeerovaServer(config: ["port": 0])      // blocks ≤ ~3 s — not on the main thread
//     let client = try PeerovaClient(ticket: server.ticket)    // ticket or 6-character connect code
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

/// An event returned by ``PeerovaNode/poll(timeout:)`` or delivered by ``PeerovaNode/events(pollInterval:)``.
///
/// Payloads are copied out of the native buffer immediately, so an event stays valid forever.
public enum PeerovaEvent: Equatable {
    /// A peer connected. Server: a client connected (one event per client). Client: connected to the server.
    /// `peer` is the connection id used by `send(_:to:channel:reliable:)`.
    case connected(peer: UInt32)
    /// The peer is gone (closed, timed out or disconnected by you). `reason` is human-readable text.
    case disconnected(peer: UInt32, reason: String)
    /// A payload from `peer` on `channel` (0...255). `reliable` tells which kind of send produced it.
    case message(peer: UInt32, channel: UInt8, reliable: Bool, data: Data)
    /// Client only: the connection attempt failed (timeout, wrong token, server full, ...).
    /// The node stays allocated until you call ``PeerovaNode/close()``.
    case connectFailed(reason: String)
}

/// Error thrown by Peerova calls. `message` is the library's error text (from `peerova_last_error`).
public struct PeerovaError: Error, CustomStringConvertible {
    /// Human-readable error text.
    public let message: String
    /// Same as ``message``.
    public var description: String { message }

    /// Builds an error from the calling thread's last native error, or `fallback` when there is none.
    static func last(_ fallback: String) -> PeerovaError {
        if let p = peerova_last_error() {
            let s = String(cString: p)
            if !s.isEmpty { return PeerovaError(message: s) }
        }
        return PeerovaError(message: fallback)
    }
}

/// Native library version, e.g. `"0.1.0"`.
public var peerovaVersion: String {
    guard let p = peerova_version() else { return "" }
    return String(cString: p)
}

/// Largest payload (in bytes) accepted by `send(..., reliable: false)`.
public let peerovaMaxUnreliablePayload = 1180

/// Reads a NUL-terminated string from a size-then-fill native getter
/// (`peerova_ticket` / `peerova_room_code`). Returns nil when the getter reports 0.
private func readNativeString(_ h: OpaquePointer,
                              _ getter: (OpaquePointer?, UnsafeMutablePointer<CChar>?, Int32) -> Int32) -> String? {
    let n = getter(h, nil, 0)
    guard n > 0 else { return nil }
    var buf = [CChar](repeating: 0, count: Int(n))
    guard getter(h, &buf, n) > 0 else { return nil }
    return String(cString: buf)
}

/// Common base of ``PeerovaServer`` and ``PeerovaClient``: polling, sending, disconnecting, closing.
///
/// The native node is freed by ``close()`` or when the object is deallocated.
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

    /// Number of currently connected peers (0 after ``close()``).
    public var peerCount: Int {
        guard let h = handle else { return 0 }
        return Int(peerova_peer_count(h))
    }

    /// Returns the next event, or `nil` when none arrived.
    ///
    /// - Parameter timeout: seconds to wait for an event; `0` (default) returns immediately.
    ///   Call it with `0` once per frame in a game loop until it returns `nil`.
    /// - Returns: the event, or `nil` (also `nil` after ``close()``).
    /// - Note: while waiting, the node's internal mutex is held, so `send` from other threads waits too.
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
        // ev.data is only valid until the next poll — copy now.
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

    /// Sends bytes to a connected peer.
    ///
    /// - Parameters:
    ///   - data: the payload.
    ///   - peer: connection id from ``PeerovaEvent/connected(peer:)``.
    ///   - channel: 0...255 (default 0). Different channels never block each other.
    ///   - reliable: `true` (default): ordered and retransmitted per channel, any size — chat, inventory, RPC,
    ///     file chunks. `false`: unreliable datagram, lowest latency, may be lost or reordered, at most
    ///     ``peerovaMaxUnreliablePayload`` bytes — positions, inputs, voice-like real-time state.
    /// - Throws: ``PeerovaError`` when the node is closed, the peer is unknown, or an unreliable payload
    ///   is larger than 1180 bytes.
    public func send(_ data: Data, to peer: UInt32, channel: UInt8 = 0, reliable: Bool = true) throws {
        guard let h = handle else { throw PeerovaError(message: "node is closed") }
        let rc: Int32 = data.withUnsafeBytes { (buf: UnsafeRawBufferPointer) -> Int32 in
            let p = buf.bindMemory(to: UInt8.self).baseAddress
            return peerova_send(h, peer, channel, p, UInt32(buf.count), reliable ? 1 : 0)
        }
        if rc != 0 { throw PeerovaError.last("send failed") }
    }

    /// Sends a string as UTF-8. Same parameters and errors as `send(_:to:channel:reliable:)` (Data).
    public func send(_ text: String, to peer: UInt32, channel: UInt8 = 0, reliable: Bool = true) throws {
        try send(Data(text.utf8), to: peer, channel: channel, reliable: reliable)
    }

    /// Closes the connection to `peer`. Both sides then get ``PeerovaEvent/disconnected(peer:reason:)``.
    /// Does nothing for an unknown peer or a closed node.
    public func disconnect(_ peer: UInt32) {
        if let h = handle { peerova_disconnect(h, peer) }
    }

    /// Async stream of events, fed by a dedicated polling thread.
    ///
    /// The stream finishes on ``close()`` or when the consuming task is cancelled. Only one stream per
    /// node at a time: calling `events()` again stops the previous stream. Do not mix it with ``poll(timeout:)``.
    ///
    /// - Parameter pollInterval: how long each native wait lasts, in seconds (default 10 ms). Shorter values
    ///   let concurrent `send` calls through sooner.
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

    /// Disconnects every peer and frees the native node (also done on deallocation). Idempotent.
    ///
    /// Stops the ``events(pollInterval:)`` stream first. Never call it while another thread is inside
    /// ``poll(timeout:)`` or `send` on this node.
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

/// A server node: accepts client connections. Give clients its ``ticket`` (or its ``roomCode``).
public final class PeerovaServer: PeerovaNode {
    /// The shareable connection ticket (`"pvt_..."`). Same `key_file` ⇒ same ticket.
    public let ticket: String

    /// Starts a server from a JSON config string.
    ///
    /// Supported keys: `port` (default 47000; 0 = random free port), `key_file` (default `"server.key"`,
    /// the server identity — keep it secret), `license` (`"PVA-..."`; without it the server runs in dev mode,
    /// max 3 clients), `max_ccu`, `token` (shared secret clients must present), `upnp` (default true),
    /// `private`, and for a 6-character connect code `app` (project id from the Peerova website) plus
    /// `share` (`{"name","public","max","mode","region"}`).
    ///
    /// Blocks up to ~3 s while the ticket is prepared — call it off the main thread (or use ``start(config:)``).
    /// - Parameter configJSON: the config, or `nil` for defaults.
    /// - Throws: ``PeerovaError`` (port in use, invalid config or license, ...).
    public init(configJSON: String?) throws {
        let h: OpaquePointer? = configJSON.map { $0.withCString { peerova_server_start($0) } } ?? peerova_server_start(nil)
        let err = h == nil ? PeerovaError.last("peerova_server_start failed") : nil
        ticket = h.flatMap { readNativeString($0, peerova_ticket) } ?? ""
        if let err = err { throw err }
        try super.init(handle: h, what: "peerova_server_start")
    }

    /// Starts a server from a dictionary config (serialized with `JSONSerialization`).
    /// Same keys, blocking and errors as ``init(configJSON:)``.
    public convenience init(config: [String: Any] = [:]) throws {
        let d = try JSONSerialization.data(withJSONObject: config)
        try self.init(configJSON: String(decoding: d, as: UTF8.self))
    }

    /// Starts the server on a background queue (use this from the main actor / UI code).
    /// - Throws: ``PeerovaError`` like ``init(configJSON:)``.
    public static func start(config: [String: Any] = [:]) async throws -> PeerovaServer {
        try await withCheckedThrowingContinuation { cont in
            DispatchQueue.global(qos: .userInitiated).async {
                cont.resume(with: Result { try PeerovaServer(config: config) })
            }
        }
    }

    /// The 6-character **connect code** (e.g. `"K7Q2XD"`), or `nil`.
    ///
    /// Only available when the server was started with `app` and `share`, and only once the Peerova website
    /// has assigned it (shortly after start) — read it again later while it is `nil`. Clients can pass the
    /// code to ``PeerovaClient/init(ticket:configJSON:)`` instead of the ticket.
    public var roomCode: String? {
        guard let h = raw else { return nil }
        return readNativeString(h, peerova_room_code)
    }
}

/// A client node: connects to one server.
///
/// The initializer returns immediately; then wait for ``PeerovaEvent/connected(peer:)`` or
/// ``PeerovaEvent/connectFailed(reason:)``. Connections are automatic: direct first, with an automatic
/// encrypted fallback route when networks block direct connections.
public final class PeerovaClient: PeerovaNode {
    /// Connects to a server.
    ///
    /// - Parameters:
    ///   - ticket: the server's ticket (`"pvt_..."`) or its 6-character connect code. A code is looked up
    ///     on the Peerova website first, so the call then blocks for that network request — call it off the
    ///     main thread.
    ///   - configJSON: `{"token":"secret","timeout_ms":20000}` or `nil`. `token` must match the server's;
    ///     `timeout_ms` (default 20000) is when ``PeerovaEvent/connectFailed(reason:)`` fires.
    /// - Throws: ``PeerovaError`` for a malformed ticket, unknown code or invalid config.
    public init(ticket: String, configJSON: String? = nil) throws {
        let h: OpaquePointer? = ticket.withCString { t in
            if let c = configJSON { return c.withCString { peerova_client_connect(t, $0) } }
            return peerova_client_connect(t, nil)
        }
        try super.init(handle: h, what: "peerova_client_connect")
    }

    /// Connects with a dictionary config (`token`, `timeout_ms`). Same behaviour as ``init(ticket:configJSON:)``.
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
