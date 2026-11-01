import XCTest
@testable import Peerova

final class PeerovaTests: XCTestCase {
    func testRoundtrip() throws {
        let key = FileManager.default.temporaryDirectory
            .appendingPathComponent("peerova-swift-\(UUID().uuidString).key").path
        let server = try PeerovaServer(config: ["port": 0, "upnp": false, "key_file": key])
        defer { server.close(); try? FileManager.default.removeItem(atPath: key) }
        XCTAssertTrue(server.ticket.hasPrefix("pvt_"), server.ticket)
        XCTAssertNil(server.roomCode)   // no "app"/"share" in the config → no connect code
        let client = try PeerovaClient(ticket: server.ticket)
        defer { client.close() }

        guard case .connected(let peer)? = client.poll(timeout: 10) else {
            return XCTFail("client did not connect")
        }
        let n = 100
        for i in 0..<n { try client.send("msg \(i)", to: peer, channel: 1, reliable: true) }
        try client.send("realtime", to: peer, channel: 2, reliable: false)

        var got: [String] = []
        var serverPeer: UInt32?
        var gotUnreliable = false
        let deadline = Date().addingTimeInterval(10)
        while (got.count < n || serverPeer == nil) && Date() < deadline {
            switch server.poll(timeout: 0.2) {
            case .connected(let p)?: serverPeer = p
            case .message(_, let ch, let reliable, let data)?:
                if reliable { XCTAssertEqual(ch, 1); got.append(String(decoding: data, as: UTF8.self)) }
                else if String(decoding: data, as: UTF8.self) == "realtime" { gotUnreliable = true }
            default: break
            }
        }
        XCTAssertEqual(got, (0..<n).map { "msg \($0)" })
        // The datagram may land after the last reliable message (on loopback it is never lost).
        let d1 = Date().addingTimeInterval(2)
        while !gotUnreliable && Date() < d1 {
            if case .message(_, 2, false, let data)? = server.poll(timeout: 0.2),
               String(decoding: data, as: UTF8.self) == "realtime" { gotUnreliable = true }
        }
        XCTAssertTrue(gotUnreliable, "client->server unreliable datagram not received")
        XCTAssertThrowsError(try client.send(Data(count: 2000), to: peer, reliable: false))

        let sp = try XCTUnwrap(serverPeer)
        let binary = Data([0x70, 0x6f, 0x6e, 0x67, 0x00, 0xff])
        try server.send(binary, to: sp)
        try server.send("srv-realtime", to: sp, channel: 3, reliable: false)
        var reply: Data?
        var backUnreliable = false
        let d2 = Date().addingTimeInterval(5)
        while (reply == nil || !backUnreliable) && Date() < d2 {
            switch client.poll(timeout: 0.2) {
            case .message(_, _, true, let data)?: reply = data
            case .message(_, 3, false, let data)?:
                if String(decoding: data, as: UTF8.self) == "srv-realtime" { backUnreliable = true }
            default: break
            }
        }
        XCTAssertEqual(reply, binary)
        XCTAssertTrue(backUnreliable, "server->client unreliable datagram not received")
        XCTAssertEqual(server.peerCount, 1)

        // client leaves -> server sees .disconnected
        client.close()
        XCTAssertThrowsError(try client.send("late", to: peer))
        var gone = false
        let d3 = Date().addingTimeInterval(10)
        while !gone && Date() < d3 {
            if case .disconnected(let p, _)? = server.poll(timeout: 0.2), p == sp { gone = true }
        }
        XCTAssertTrue(gone, "server did not see .disconnected")
        print("swift PASS: peerova \(peerovaVersion), \(n) reliable msgs in order, unreliable both ways, binary reply ok, disconnect ok")
    }

    /// Same flow through the async `events()` stream.
    func testAsyncEventsStream() async throws {
        let key = FileManager.default.temporaryDirectory
            .appendingPathComponent("peerova-swift-\(UUID().uuidString).key").path
        let server = try await PeerovaServer.start(config: ["port": 0, "upnp": false, "key_file": key])
        defer { server.close(); try? FileManager.default.removeItem(atPath: key) }
        let client = try PeerovaClient(ticket: server.ticket, config: ["timeout_ms": 10000])
        defer { client.close() }
        var cliPeer: UInt32?
        for await ev in client.events() {
            if case .connected(let p) = ev { cliPeer = p; break }
            if case .connectFailed(let r) = ev { return XCTFail("connect failed: \(r)") }
        }
        let peer = try XCTUnwrap(cliPeer)
        for i in 0..<60 { try client.send("m\(i)", to: peer, channel: 1) }
        var got: [String] = []
        for await ev in server.events() {
            if case .message(_, 1, true, let data) = ev {
                got.append(String(decoding: data, as: UTF8.self))
                if got.count == 60 { break }
            }
        }
        XCTAssertEqual(got, (0..<60).map { "m\($0)" })
    }

    func testBadTicketThrows() {
        XCTAssertThrowsError(try PeerovaClient(ticket: "not-a-ticket"))
    }
}
