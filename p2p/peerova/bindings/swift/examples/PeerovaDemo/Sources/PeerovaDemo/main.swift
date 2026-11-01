// How to run:  swift run PeerovaDemo server [seconds]   then   swift run PeerovaDemo client <TICKET|CODE> [seconds]
//
// Position-sync + chat demo:
//   server : prints its ticket, forwards chat lines to the other clients, prints the latest position of each client.
//   client : sends its position 20 times per second on channel 1 (unreliable) and every line you type
//            as chat on channel 0 (reliable).
//   seconds: optional run time; without it the demo runs until end of input (Ctrl+D).
// Server identity key: $PEEROVA_KEY_FILE, else ./server.key (keep it secret, do not commit it).
import Foundation
import Peerova

let chatChannel: UInt8 = 0   // reliable, ordered
let posChannel: UInt8 = 1    // unreliable, latest value wins (max 1180 bytes per message)

let args = CommandLine.arguments
func usage() -> Never {
    print("usage: PeerovaDemo server [seconds] | client <TICKET|CODE> [seconds]")
    exit(2)
}
guard args.count >= 2, args[1] == "server" || args[1] == "client" else { usage() }
let isServer = args[1] == "server"
if !isServer && args.count < 3 { usage() }
let secondsArg = args.count > (isServer ? 2 : 3) ? Double(args[isServer ? 2 : 3]) : nil
let deadline = secondsArg.map { Date().addingTimeInterval($0) }

/// Lines typed on stdin, filled by a background thread and drained by the main loop.
final class LineQueue: @unchecked Sendable {
    private let lock = NSLock()
    private var lines: [String] = []
    private var closed = false
    func push(_ s: String) { lock.lock(); lines.append(s); lock.unlock() }
    func close() { lock.lock(); closed = true; lock.unlock() }
    func drain() -> (lines: [String], closed: Bool) {
        lock.lock(); defer { lock.unlock() }
        let out = lines
        lines.removeAll()
        return (out, closed)
    }
}
let input = LineQueue()
if deadline == nil || isatty(STDIN_FILENO) != 0 {
    Thread.detachNewThread {
        while let line = readLine() { input.push(line) }
        input.close()
    }
}

func encodePosition(_ x: Float, _ y: Float) -> Data {
    var le = [x.bitPattern.littleEndian, y.bitPattern.littleEndian]
    return Data(bytes: &le, count: 8)
}
func decodePosition(_ d: Data) -> (Float, Float)? {
    guard d.count == 8 else { return nil }
    let b = [UInt8](d)
    func f(_ o: Int) -> Float {
        Float(bitPattern: UInt32(b[o]) | UInt32(b[o + 1]) << 8 | UInt32(b[o + 2]) << 16 | UInt32(b[o + 3]) << 24)
    }
    return (f(0), f(4))
}

do {
    let node: PeerovaNode
    if isServer {
        let key = ProcessInfo.processInfo.environment["PEEROVA_KEY_FILE"] ?? "server.key"
        let server = try PeerovaServer(config: ["port": 0, "key_file": key])   // blocks ≤ ~3 s
        print("ticket: \(server.ticket)")
        node = server
    } else {
        node = try PeerovaClient(ticket: args[2], config: ["timeout_ms": 20000])
        print("connecting...")
    }
    defer { node.close() }

    var peers = Set<UInt32>()
    var positions: [UInt32: (x: Float, y: Float, count: Int)] = [:]
    var nextPos = Date()
    var nextReport = Date().addingTimeInterval(1)
    var t: Float = 0
    var failed = false

    loop: while deadline.map({ Date() < $0 }) ?? true {
        // 1. Drain all pending events (short wait keeps the loop at ~100 Hz without spinning).
        var wait: TimeInterval = 0.01
        while let ev = node.poll(timeout: wait) {
            wait = 0
            switch ev {
            case .connected(let peer):
                peers.insert(peer)
                print("* peer \(peer) connected")
                if !isServer { try node.send("hello from swift", to: peer, channel: chatChannel) }
            case .disconnected(let peer, let reason):
                peers.remove(peer)
                positions[peer] = nil
                print("* peer \(peer) left: \(reason)")
                if !isServer { break loop }
            case .connectFailed(let reason):
                print("* connect failed: \(reason)")
                failed = true
                break loop
            case .message(let peer, let channel, let reliable, let data):
                if channel == posChannel && !reliable, case let (x, y)? = decodePosition(data) {
                    positions[peer] = (x, y, (positions[peer]?.count ?? 0) + 1)
                } else if channel == chatChannel {
                    let text = String(decoding: data, as: UTF8.self)
                    print("<\(peer)> \(text)")
                    if isServer {
                        for other in peers where other != peer {
                            try node.send("<\(peer)> \(text)", to: other, channel: chatChannel)
                        }
                    }
                }
            }
        }
        // 2. Chat lines typed by the user → reliable.
        let (lines, closed) = input.drain()
        for line in lines { for p in peers { try node.send(line, to: p, channel: chatChannel) } }
        if closed { break }
        // 3. Client: 20 Hz position updates → unreliable.
        let now = Date()
        if !isServer && now >= nextPos {
            nextPos = nextPos.addingTimeInterval(0.05)
            t += 0.05
            let pos = encodePosition(10 * cos(t), 10 * sin(t))
            for p in peers { try node.send(pos, to: p, channel: posChannel, reliable: false) }
        }
        // 4. Server: print positions once per second.
        if isServer && now >= nextReport {
            nextReport = nextReport.addingTimeInterval(1)
            for (peer, p) in positions.sorted(by: { $0.key < $1.key }) {
                print(String(format: "  pos %u: (%.2f, %.2f), %ld updates", peer, p.x, p.y, p.count))
            }
        }
    }
    node.close()   // clean shutdown: disconnects peers, frees the native node
    print("bye")
    if failed { exit(1) }
} catch {
    print("error: \(error)")
    exit(1)
}
