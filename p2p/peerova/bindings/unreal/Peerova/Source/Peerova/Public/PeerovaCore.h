// PeerovaCore.h — header-only C++17 RAII wrapper over the Peerova C API (peerova.h).
// Engine-agnostic: no Unreal types, usable from any C++ project (UPeerovaSubsystem is built on it).
//
//   auto srv = peerova::Node::server(R"({"port":0})");     // blocks ≤ ~3 s
//   auto cli = peerova::Node::client(srv.ticket());        // ticket or 6-character connect code
//   peerova::Event ev;
//   if (cli.poll(ev, 10000) && ev.type == peerova::EventType::Connected) cli.send(ev.peer, "hi");
//
// Error handling: factories return an invalid Node (operator bool == false) and set
// `error` from peerova_last_error(); send() returns false and sets `error`. No exceptions are
// thrown, so it builds with exceptions disabled (Unreal default).
//
// Threading: the native node has an internal mutex, so calls are safe from any thread, but a
// waiting poll (timeout_ms > 0) holds that mutex. Never destroy/reset a Node while another
// thread is inside poll()/send() on it.
#pragma once

#include <cstdint>
#include <string>
#include <string_view>
#include <utility>
#include <vector>

#include "peerova.h"

namespace peerova {

/// Kind of an Event.
enum class EventType : int32_t {
    None          = 0,                             ///< no event (default-constructed Event)
    Connected     = PEEROVA_EVENT_CONNECTED,       ///< server: a client connected; client: connected to the server
    Disconnected  = PEEROVA_EVENT_DISCONNECTED,    ///< the peer is gone; data = UTF-8 reason
    Message       = PEEROVA_EVENT_MESSAGE,         ///< payload in data, on `channel`, `reliable` set
    ConnectFailed = PEEROVA_EVENT_CONNECT_FAILED,  ///< client only: could not connect; data = UTF-8 reason
};

/// Largest payload (bytes) accepted by an unreliable send.
constexpr uint32_t kMaxUnreliablePayload = 1180;

/// One event filled by Node::poll(). `data` is an owned copy (the native buffer is only valid
/// until the next poll). For Disconnected / ConnectFailed it holds the UTF-8 reason.
struct Event {
    EventType type = EventType::None;
    uint32_t peer = 0;          ///< connection id (server: the client; client: the server)
    uint8_t channel = 0;        ///< 0..255 (Message only)
    bool reliable = false;      ///< true if sent reliably (Message only)
    std::vector<uint8_t> data;  ///< payload (Message) or UTF-8 reason (Disconnected / ConnectFailed)

    /// `data` as a std::string (UTF-8 text, or raw bytes).
    std::string text() const { return std::string(data.begin(), data.end()); }
};

/// Last native error message for the calling thread (empty if none).
inline std::string last_error() {
    const char* e = peerova_last_error();
    return e ? std::string(e) : std::string();
}

/// Native library version, e.g. "0.1.0".
inline std::string version() {
    const char* v = peerova_version();
    return v ? std::string(v) : std::string();
}

/// Owns one native node (server or client). Move-only; the node is freed by reset() or the destructor.
/// Thread-safe per the C API (internal mutex), but do not destroy/reset it while another thread is
/// inside poll()/send() on it.
class Node {
public:
    /// An empty (invalid) node.
    Node() = default;
    /// Takes ownership of a raw handle from peerova_server_start / peerova_client_connect (may be null).
    explicit Node(peerova_node* h) noexcept : h_(h) {}
    Node(const Node&) = delete;
    Node& operator=(const Node&) = delete;
    Node(Node&& o) noexcept : error(std::move(o.error)), h_(std::exchange(o.h_, nullptr)), ticket_(std::move(o.ticket_)) {}
    Node& operator=(Node&& o) noexcept {
        if (this != &o) {
            reset();
            h_ = std::exchange(o.h_, nullptr);
            ticket_ = std::move(o.ticket_);
            error = std::move(o.error);
        }
        return *this;
    }
    ~Node() { reset(); }

    /// Starts a server. Blocks until the ticket is ready (≤ ~3 s) — not on a game/UI thread.
    ///
    /// config_json (may be empty): {"port":47000, "key_file":"server.key", "license":"PVA-...",
    /// "max_ccu":N, "token":"secret", "upnp":true, "private":false} and, for a 6-character connect
    /// code, "app":"<project id from the Peerova website>" + "share":{"name","public","max","mode","region"}.
    /// Defaults: port 47000 (0 = random), key_file "server.key" (same key ⇒ same ticket), no license =
    /// dev mode (max 3 clients). On failure the returned Node is invalid and `error` says why.
    static Node server(const std::string& config_json = std::string()) {
        Node n(peerova_server_start(config_json.empty() ? nullptr : config_json.c_str()));
        if (!n) {
            n.error = last_error();
            return n;
        }
        int32_t need = peerova_ticket(n.h_, nullptr, 0);
        if (need > 0) {
            std::vector<char> buf(static_cast<size_t>(need));
            peerova_ticket(n.h_, buf.data(), need);
            n.ticket_.assign(buf.data());
        }
        return n;
    }

    /// Connects to a server. `ticket` is the server's ticket ("pvt_...") or its 6-character connect
    /// code (a code is first looked up on the Peerova website, so the call then blocks for that request).
    /// config_json (may be empty): {"token":"secret","timeout_ms":20000}.
    /// Returns immediately otherwise; poll for Connected / ConnectFailed. On failure (malformed ticket,
    /// unknown code, bad config) the returned Node is invalid and `error` says why.
    static Node client(const std::string& ticket, const std::string& config_json = std::string()) {
        Node n(peerova_client_connect(ticket.c_str(), config_json.empty() ? nullptr : config_json.c_str()));
        if (!n) n.error = last_error();
        return n;
    }

    /// True when this Node owns a native node.
    explicit operator bool() const noexcept { return h_ != nullptr; }
    /// The raw native handle (still owned by this Node), for direct peerova_* calls.
    peerova_node* handle() const noexcept { return h_; }

    /// Server ticket ("pvt_..."), empty for clients. Share it with clients.
    const std::string& ticket() const noexcept { return ticket_; }

    /// The server's 6-character connect code (e.g. "K7Q2XD"), or empty. Only set when the server was
    /// started with "app" + "share", and only once the website has assigned it (shortly after start) —
    /// call again later while empty. Clients can pass it to client() instead of the ticket.
    std::string room_code() const {
        if (!h_) return std::string();
        int32_t need = peerova_room_code(h_, nullptr, 0);
        if (need <= 0) return std::string();
        std::vector<char> buf(static_cast<size_t>(need));
        if (peerova_room_code(h_, buf.data(), need) <= 0) return std::string();
        return std::string(buf.data());
    }

    /// Gets the next event. Waits up to timeout_ms (0 = non-blocking). Returns true and fills `out`
    /// if an event arrived, false otherwise (or when the node is invalid).
    /// Note: a waiting poll holds the node mutex; use 0 from a game tick and loop until false.
    bool poll(Event& out, uint32_t timeout_ms = 0) {
        if (!h_) return false;
        peerova_event ev{};
        int32_t got = timeout_ms ? peerova_poll_wait(h_, &ev, timeout_ms) : peerova_poll(h_, &ev);
        if (!got) return false;
        out.type = static_cast<EventType>(ev.type);
        out.peer = ev.peer;
        out.channel = ev.channel;
        out.reliable = ev.reliable != 0;
        if (ev.len && ev.data) out.data.assign(ev.data, ev.data + ev.len);
        else out.data.clear();
        return true;
    }

    /// Sends `len` bytes to `peer` on `channel` (0..255; channels never block each other).
    /// reliable = true: ordered + retransmitted per channel, any size (chat, inventory, RPC, file chunks).
    /// reliable = false: datagram, may be lost or reordered, max kMaxUnreliablePayload bytes
    /// (positions, inputs, voice-like real-time state).
    /// Returns false and sets `error` on failure (invalid node, unknown peer, datagram too large).
    bool send(uint32_t peer, const void* data, uint32_t len, uint8_t channel = 0, bool reliable = true) {
        if (!h_) {
            error = "node is closed";
            return false;
        }
        if (peerova_send(h_, peer, channel, static_cast<const uint8_t*>(data), len, reliable ? 1 : 0) != 0) {
            error = last_error();
            return false;
        }
        return true;
    }
    /// send() for a byte vector.
    bool send(uint32_t peer, const std::vector<uint8_t>& data, uint8_t channel = 0, bool reliable = true) {
        return send(peer, data.data(), static_cast<uint32_t>(data.size()), channel, reliable);
    }
    /// send() for text (sent as its raw bytes, normally UTF-8).
    bool send(uint32_t peer, std::string_view text, uint8_t channel = 0, bool reliable = true) {
        return send(peer, text.data(), static_cast<uint32_t>(text.size()), channel, reliable);
    }

    /// Closes the connection to `peer`; both sides then get Disconnected. No-op on an invalid node.
    void disconnect(uint32_t peer) {
        if (h_) peerova_disconnect(h_, peer);
    }

    /// Number of connected peers (0 for an invalid node).
    uint32_t peer_count() const { return h_ ? peerova_peer_count(h_) : 0; }

    /// Disconnects all peers and frees the native node. Idempotent.
    void reset() noexcept {
        if (h_) peerova_free(std::exchange(h_, nullptr));
        ticket_.clear();
    }

    /// Last error recorded by this wrapper (factory failure or send failure); text from peerova_last_error.
    std::string error;

private:
    peerova_node* h_ = nullptr;
    std::string ticket_;
};

}  // namespace peerova
