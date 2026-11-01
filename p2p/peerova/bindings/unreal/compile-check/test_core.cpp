// Non-Unreal test of PeerovaCore.h: 100 reliable messages in order + 1 unreliable + a reply.
#include "PeerovaCore.h"

#include <chrono>
#include <cstdio>
#include <cstdlib>
#include <string>

#define CHECK(c, ...) do { if (!(c)) { std::fprintf(stderr, "FAIL %s:%d: ", __FILE__, __LINE__); std::fprintf(stderr, __VA_ARGS__); std::fprintf(stderr, "\n"); return 1; } } while (0)

int main(int argc, char** argv) {
    using clock = std::chrono::steady_clock;
    const std::string key = argc > 1 ? argv[1] : "/tmp/peerova-unreal-core.key";
    auto srv = peerova::Node::server(R"({"port":0,"upnp":false,"key_file":")" + key + "\"}");
    CHECK(srv, "server: %s", srv.error.c_str());
    CHECK(srv.ticket().rfind("pvt_", 0) == 0, "ticket %s", srv.ticket().c_str());

    auto bad = peerova::Node::client("garbage");
    CHECK(!bad, "bad ticket accepted");
    std::printf("bad ticket rejected: %s\n", bad.error.c_str());

    auto cli = peerova::Node::client(srv.ticket());
    CHECK(cli, "client: %s", cli.error.c_str());
    peerova::Event ev;
    CHECK(cli.poll(ev, 10000) && ev.type == peerova::EventType::Connected, "client not connected (type %d)", (int)ev.type);
    const uint32_t peer = ev.peer;

    const int N = 100;
    auto t0 = clock::now();
    for (int i = 0; i < N; i++) CHECK(cli.send(peer, "msg " + std::to_string(i), 1, true), "send: %s", cli.error.c_str());
    CHECK(cli.send(peer, std::string_view("realtime"), 2, false), "unreliable send: %s", cli.error.c_str());

    int got = 0;
    bool unreliable = false;
    uint32_t sp = 0;
    bool have_sp = false;
    auto deadline = clock::now() + std::chrono::seconds(10);
    while ((got < N || !have_sp) && clock::now() < deadline) {
        if (!srv.poll(ev, 200)) continue;
        if (ev.type == peerova::EventType::Connected) { sp = ev.peer; have_sp = true; }
        else if (ev.type == peerova::EventType::Message) {
            if (ev.reliable) {
                CHECK(ev.channel == 1, "channel %d", ev.channel);
                CHECK(ev.text() == "msg " + std::to_string(got), "out of order at %d: %s", got, ev.text().c_str());
                got++;
            } else if (ev.text() == "realtime") unreliable = true;
        }
    }
    CHECK(got == N && have_sp, "got %d/%d", got, N);
    // the datagram may trail the stream; drain briefly
    for (int i = 0; i < 10 && !unreliable; i++)
        if (srv.poll(ev, 50) && ev.type == peerova::EventType::Message && !ev.reliable) unreliable = ev.text() == "realtime";

    CHECK(srv.send(sp, std::string_view("pong")), "reply: %s", srv.error.c_str());
    bool reply = false;
    deadline = clock::now() + std::chrono::seconds(5);
    while (!reply && clock::now() < deadline)
        if (cli.poll(ev, 200) && ev.type == peerova::EventType::Message) reply = ev.text() == "pong";
    CHECK(reply, "no reply");
    CHECK(srv.peer_count() == 1, "peer_count %u", srv.peer_count());

    peerova::Node moved = std::move(cli);  // move semantics
    CHECK(!cli && moved, "move");
    moved.reset();
    double ms = std::chrono::duration<double, std::milli>(clock::now() - t0).count();
    std::printf("unreal core OK: peerova %s, %d reliable msgs in order, unreliable=%s, reply ok, %.1f ms\n",
                peerova::version().c_str(), N, unreliable ? "received" : "lost(allowed)", ms);
    return 0;
}
