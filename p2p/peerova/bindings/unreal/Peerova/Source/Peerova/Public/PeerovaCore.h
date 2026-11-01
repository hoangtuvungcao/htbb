// PeerovaCore.h — header-only C++17 RAII wrapper over the peerova C ABI (peerova.h).
// Engine-agnostic: no Unreal types, usable from any C++ project.
//
//   auto srv = peerova::Node::server(R"({"port":0})");     // blocks ≤ ~3 s
//   auto cli = peerova::Node::client(srv.ticket());
//   peerova::Event ev;
//   if (cli.poll(ev, 10000) && ev.type == peerova::EventType::Connected) cli.send(ev.peer, "hi");
//
// Error handling: factories return an invalid Node (operator bool == false) and set
// `error` from peerova_last_error(); send() returns false. No exceptions are thrown,
// so it builds with exceptions disabled (Unreal default).
#pragma once

#include <cstdint>
#include <string>
#include <string_view>
#include <utility>
#include <vector>

#include "peerova.h"

namespace peerova {

enum class EventType : int32_t {
    None          = 0,
    Connected     = PEEROVA_EVENT_CONNECTED,
    Disconnected  = PEEROVA_EVENT_DISCONNECTED,
    Message       = PEEROVA_EVENT_MESSAGE,
    ConnectFailed = PEEROVA_EVENT_CONNECT_FAILED,
};

/// Largest payload for unreliable sends.
constexpr uint32_t kMaxUnreliablePayload = 1180;

/// One event. `data` is an owned copy (the native buffer is only valid until the next poll).
/// For Disconnected / ConnectFailed it holds the UTF-8 reason.
struct Event {
    EventType type = EventType::None;
    uint32_t peer = 0;
    uint8_t channel = 0;
    bool reliable = false;
    std::vector<uint8_t> data;

    std::string text() const { return std::string(data.begin(), data.end()); }
};

inline std::string last_error() {
    const char* e = peerova_last_error();
    return e ? std::string(e) : std::string();
}

inline std::string version() {
    const char* v = peerova_version();
    return v ? std::string(v) : std::string();
}

/// Owns a peerova_node*. Move-only. Thread-safe per the C ABI (internal mutex), but do not
/// destroy/reset it while another thread is inside poll()/send() on it.
class Node {
public:
    Node() = default;
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

    /// Starts a server (blocks until the ticket is ready, ≤ ~3 s). config_json may be empty.
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

    /// Connects to a server. Returns immediately; poll for Connected / ConnectFailed.
    static Node client(const std::string& ticket, const std::string& config_json = std::string()) {
        Node n(peerova_client_connect(ticket.c_str(), config_json.empty() ? nullptr : config_json.c_str()));
        if (!n) n.error = last_error();
        return n;
    }

    explicit operator bool() const noexcept { return h_ != nullptr; }
    peerova_node* handle() const noexcept { return h_; }

    /// Server ticket ("pvt_..."), empty for clients.
    const std::string& ticket() const noexcept { return ticket_; }

    /// Waits up to timeout_ms (0 = non-blocking). Returns true and fills `out` if an event arrived.
    /// Note: a waiting poll holds the node mutex; use 0 from a game tick.
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
    bool send(uint32_t peer, const std::vector<uint8_t>& data, uint8_t channel = 0, bool reliable = true) {
        return send(peer, data.data(), static_cast<uint32_t>(data.size()), channel, reliable);
    }
    bool send(uint32_t peer, std::string_view text, uint8_t channel = 0, bool reliable = true) {
        return send(peer, text.data(), static_cast<uint32_t>(text.size()), channel, reliable);
    }

    void disconnect(uint32_t peer) {
        if (h_) peerova_disconnect(h_, peer);
    }

    uint32_t peer_count() const { return h_ ? peerova_peer_count(h_) : 0; }

    /// Frees the native node. Idempotent.
    void reset() noexcept {
        if (h_) peerova_free(std::exchange(h_, nullptr));
        ticket_.clear();
    }

    /// Last error recorded by this wrapper (factory failure or send failure).
    std::string error;

private:
    peerova_node* h_ = nullptr;
    std::string ticket_;
};

}  // namespace peerova
