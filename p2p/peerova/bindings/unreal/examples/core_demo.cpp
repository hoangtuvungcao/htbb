// How to run (Linux, no Unreal needed), from the bindings/unreal folder:
//   g++ -std=c++17 -IPeerova/Source/ThirdParty/PeerovaLibrary/include -IPeerova/Source/Peerova/Public
//       examples/core_demo.cpp -L../../lib/linux-x86_64 -lpeerova -Wl,-rpath,'$ORIGIN' -o core_demo
//   cp ../../lib/linux-x86_64/libpeerova.so .
//   ./core_demo server [seconds]              prints the ticket
//   ./core_demo client <TICKET|CODE> [seconds]
//
// Engine-agnostic position-sync + chat demo on PeerovaCore.h — the same calls UPeerovaSubsystem makes:
//   server : forwards chat lines to the other clients, prints the latest position of each client every second.
//   client : sends its position 20 times per second on channel 1 (unreliable) and a chat line on
//            channel 0 (reliable) once per second.
//   seconds: run time (default 10). Then the node is closed cleanly.
// Server identity key: $PEEROVA_KEY_FILE, else ./server.key (keep it secret, do not commit it).
#include "PeerovaCore.h"

#include <chrono>
#include <cmath>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <map>
#include <set>
#include <string>
#include <thread>

namespace {
constexpr uint8_t kChat = 0;  // reliable, ordered
constexpr uint8_t kPos = 1;   // unreliable, latest value wins

struct Pos { float x = 0, y = 0; int updates = 0; };

std::vector<uint8_t> encode_pos(float x, float y) {
    std::vector<uint8_t> b(8);
    std::memcpy(b.data(), &x, 4);  // little-endian on all supported platforms
    std::memcpy(b.data() + 4, &y, 4);
    return b;
}
}  // namespace

int main(int argc, char** argv) {
    using clock = std::chrono::steady_clock;
    const bool is_server = argc >= 2 && std::strcmp(argv[1], "server") == 0;
    const bool is_client = argc >= 3 && std::strcmp(argv[1], "client") == 0;
    if (!is_server && !is_client) {
        std::fprintf(stderr, "usage: %s server [seconds] | client <TICKET|CODE> [seconds]\n", argv[0]);
        return 2;
    }
    const int sec_arg = is_server ? 2 : 3;
    const double seconds = argc > sec_arg ? std::atof(argv[sec_arg]) : 10.0;

    peerova::Node node;
    if (is_server) {
        const char* key = std::getenv("PEEROVA_KEY_FILE");
        node = peerova::Node::server(std::string(R"({"port":0,"key_file":")") + (key ? key : "server.key") + "\"}");
        if (!node) { std::fprintf(stderr, "server failed: %s\n", node.error.c_str()); return 1; }
        std::printf("ticket: %s\n", node.ticket().c_str());
    } else {
        node = peerova::Node::client(argv[2], R"({"timeout_ms":20000})");
        if (!node) { std::fprintf(stderr, "client failed: %s\n", node.error.c_str()); return 1; }
        std::printf("connecting...\n");
    }
    std::fflush(stdout);

    std::set<uint32_t> peers;
    std::map<uint32_t, Pos> positions;
    const auto end = clock::now() + std::chrono::duration_cast<clock::duration>(std::chrono::duration<double>(seconds));
    auto next_pos = clock::now();
    auto next_tick = clock::now() + std::chrono::seconds(1);
    float t = 0;
    int rc = 0, chat_no = 0;
    bool running = true;

    while (running && clock::now() < end) {
        // 1. "Tick": drain every pending event without blocking, like UPeerovaSubsystem::Tick.
        peerova::Event ev;
        while (running && node.poll(ev, 0)) {
            switch (ev.type) {
            case peerova::EventType::Connected:
                peers.insert(ev.peer);
                std::printf("* peer %u connected\n", ev.peer);
                break;
            case peerova::EventType::Disconnected:
                peers.erase(ev.peer);
                positions.erase(ev.peer);
                std::printf("* peer %u left: %s\n", ev.peer, ev.text().c_str());
                if (is_client) running = false;
                break;
            case peerova::EventType::ConnectFailed:
                std::printf("* connect failed: %s\n", ev.text().c_str());
                rc = 1;
                running = false;
                break;
            case peerova::EventType::Message:
                if (ev.channel == kPos && !ev.reliable && ev.data.size() == 8) {
                    Pos& p = positions[ev.peer];
                    std::memcpy(&p.x, ev.data.data(), 4);
                    std::memcpy(&p.y, ev.data.data() + 4, 4);
                    p.updates++;
                } else if (ev.channel == kChat) {
                    std::printf("<%u> %s\n", ev.peer, ev.text().c_str());
                    if (is_server)
                        for (uint32_t other : peers)
                            if (other != ev.peer)
                                node.send(other, "<" + std::to_string(ev.peer) + "> " + ev.text(), kChat, true);
                }
                break;
            default:
                break;
            }
        }
        const auto now = clock::now();
        // 2. Client: positions at 20 Hz, unreliable.
        if (is_client && now >= next_pos) {
            next_pos += std::chrono::milliseconds(50);
            t += 0.05f;
            const auto pos = encode_pos(10 * std::cos(t), 10 * std::sin(t));
            for (uint32_t p : peers)
                if (!node.send(p, pos, kPos, false)) std::printf("send failed: %s\n", node.error.c_str());
        }
        // 3. Once per second: client chats (reliable), server prints positions.
        if (now >= next_tick) {
            next_tick += std::chrono::seconds(1);
            if (is_client)
                for (uint32_t p : peers) node.send(p, "chat line " + std::to_string(++chat_no), kChat, true);
            if (is_server)
                for (const auto& [peer, p] : positions)
                    std::printf("  pos %u: (%.2f, %.2f), %d updates\n", peer, p.x, p.y, p.updates);
        }
        std::fflush(stdout);
        std::this_thread::sleep_for(std::chrono::milliseconds(5));  // stand-in for the engine's frame time
    }
    node.reset();  // clean shutdown: disconnects peers, frees the native node
    std::printf("bye\n");
    return rc;
}
