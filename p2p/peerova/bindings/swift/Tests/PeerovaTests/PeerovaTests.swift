import XCTest
@testable import Peerova

final class PeerovaTests: XCTestCase {
    func testRoundtrip() throws {
        let key = FileManager.default.temporaryDirectory
            .appendingPathComponent("peerova-swift-\(UUID().uuidString).key").path
        let server = try PeerovaServer(config: ["port": 0, "upnp": false, "key_file": key])
        defer { server.close(); try? FileManager.default.removeItem(atPath: key) }
        XCTAssertTrue(server.ticket.hasPrefix("pvt_"), server.ticket)
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
        // The unreliable datagram may legitimately be lost; on loopback it normally arrives.
        if !gotUnreliable { print("note: unreliable datagram not observed (allowed)") }

        let sp = try XCTUnwrap(serverPeer)
        try server.send("pong", to: sp)
        var reply: Data?
        let d2 = Date().addingTimeInterval(5)
        while reply == nil && Date() < d2 {
            if case .message(_, _, true, let data)? = client.poll(timeout: 0.2) { reply = data }
        }
        XCTAssertEqual(reply, Data("pong".utf8))
        XCTAssertEqual(server.peerCount, 1)
        print("swift OK: peerova \(peerovaVersion), \(n) reliable msgs in order, unreliable=\(gotUnreliable)")
    }

    func testBadTicketThrows() {
        XCTAssertThrowsError(try PeerovaClient(ticket: "not-a-ticket"))
    }
}
