/* How to run (Linux; see README for Windows):
 *   cc chat_positions.c -I.. -L../../../target/release -lpeerova -Wl,-rpath,'$ORIGIN/../../../target/release' -o chat_positions
 *   ./chat_positions server [port]                 # prints "TICKET <ticket>" (and "CODE <code>" if configured)
 *   ./chat_positions client <ticket|code> [secs]   # default 10 seconds, 0 = until Ctrl+C
 *
 * Tiny chat + position sync over Peerova.
 *   channel 0, reliable   : chat lines (UTF-8). The client sends one per second.
 *   channel 1, unreliable : positions, 20 times per second, 12 bytes = [u32 player][f32 x][f32 y] little-endian.
 * The server forwards chat and positions to the other clients (writing the sender's peer id into "player";
 * the server's own player id is 0). Set PEEROVA_EXAMPLE_EXIT_WHEN_EMPTY=1 to make the server exit once its
 * last client has left. Optional: PEEROVA_APP=prj_... gives the server a 6-character connect code.
 */
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "peerova.h"

#define CHAT_CHANNEL     0
#define POSITION_CHANNEL 1
#define SEND_INTERVAL_MS 50 /* 20 Hz */
#define MAX_PEERS        64

static volatile sig_atomic_t g_quit = 0;
static void on_sigint(int sig) { (void)sig; g_quit = 1; }

static double now_ms(void) {
    struct timespec ts;
    timespec_get(&ts, TIME_UTC);
    return ts.tv_sec * 1000.0 + ts.tv_nsec / 1e6;
}

/* Little-endian encoding, independent of the host byte order. */
static void put_u32(uint8_t* p, uint32_t v) { p[0] = (uint8_t)v; p[1] = (uint8_t)(v >> 8); p[2] = (uint8_t)(v >> 16); p[3] = (uint8_t)(v >> 24); }
static uint32_t get_u32(const uint8_t* p) { return p[0] | (uint32_t)p[1] << 8 | (uint32_t)p[2] << 16 | (uint32_t)p[3] << 24; }
static void put_f32(uint8_t* p, float f) { uint32_t v; memcpy(&v, &f, 4); put_u32(p, v); }
static float get_f32(const uint8_t* p) { uint32_t v = get_u32(p); float f; memcpy(&f, &v, 4); return f; }

static void encode_position(uint8_t out[12], uint32_t player, float x, float y) {
    put_u32(out, player); put_f32(out + 4, x); put_f32(out + 8, y);
}

/* ------------------------------------------------------------------ server */

static uint32_t g_peers[MAX_PEERS];
static int g_peer_count = 0;

static void peer_add(uint32_t p) { if (g_peer_count < MAX_PEERS) g_peers[g_peer_count++] = p; }
static void peer_remove(uint32_t p) {
    for (int i = 0; i < g_peer_count; i++) if (g_peers[i] == p) { g_peers[i] = g_peers[--g_peer_count]; return; }
}
/* Send to every client except `except` (0 = nobody excluded; peer ids are never 0 in practice). */
static void forward_to_others(peerova_node* srv, uint32_t except, uint8_t ch, const uint8_t* data, uint32_t len, int reliable) {
    for (int i = 0; i < g_peer_count; i++)
        if (g_peers[i] != except) peerova_send(srv, g_peers[i], ch, data, len, reliable);
}

static int run_server(int port) {
    char cfg[512];
    const char* app = getenv("PEEROVA_APP");
    /* The key file is the server identity (same key = same ticket). Keep it secret, never commit it. */
    if (app && *app)
        snprintf(cfg, sizeof cfg, "{\"port\":%d,\"key_file\":\"example-server.key\",\"app\":\"%s\","
                 "\"share\":{\"name\":\"C example\",\"public\":false,\"max\":8}}", port, app);
    else
        snprintf(cfg, sizeof cfg, "{\"port\":%d,\"key_file\":\"example-server.key\"}", port);

    peerova_node* srv = peerova_server_start(cfg);
    if (!srv) { fprintf(stderr, "server start failed: %s\n", peerova_last_error()); return 1; }

    char ticket[2048];
    if (peerova_ticket(srv, ticket, sizeof ticket) <= 0) { fprintf(stderr, "no ticket: %s\n", peerova_last_error()); peerova_free(srv); return 1; }
    printf("TICKET %s\n", ticket);
    printf("peerova %s; waiting for clients (Ctrl+C to stop)\n", peerova_version());

    int exit_when_empty = getenv("PEEROVA_EXAMPLE_EXIT_WHEN_EMPTY") && strcmp(getenv("PEEROVA_EXAMPLE_EXIT_WHEN_EMPTY"), "1") == 0;
    int had_clients = 0, code_printed = 0;
    unsigned long positions = 0;
    double t0 = now_ms(), next_send = 0, next_report = 1000;
    peerova_event ev;

    while (!g_quit) {
        /* Drain all events; the first poll waits up to 10 ms so the loop does not spin. */
        uint32_t wait = 10;
        while (peerova_poll_wait(srv, &ev, wait)) {
            wait = 0;
            if (ev.type == PEEROVA_EVENT_CONNECTED) {
                peer_add(ev.peer); had_clients = 1;
                printf("peer %u connected (%u online)\n", ev.peer, peerova_peer_count(srv));
            } else if (ev.type == PEEROVA_EVENT_DISCONNECTED) {
                peer_remove(ev.peer);
                printf("peer %u left\n", ev.peer);
            } else if (ev.type == PEEROVA_EVENT_MESSAGE && ev.channel == CHAT_CHANNEL) {
                char line[1200];
                int n = snprintf(line, sizeof line, "[peer %u] %.*s", ev.peer, (int)ev.len, (const char*)ev.data);
                if (n > (int)sizeof line - 1) n = (int)sizeof line - 1;
                printf("%s\n", line);
                forward_to_others(srv, ev.peer, CHAT_CHANNEL, (const uint8_t*)line, (uint32_t)n, 1);
            } else if (ev.type == PEEROVA_EVENT_MESSAGE && ev.channel == POSITION_CHANNEL && ev.len == 12) {
                uint8_t out[12];
                positions++;
                /* ev.data is only valid until the next poll: re-encode into our own buffer. */
                encode_position(out, ev.peer, get_f32(ev.data + 4), get_f32(ev.data + 8));
                forward_to_others(srv, ev.peer, POSITION_CHANNEL, out, 12, 0);
            }
        }

        double t = now_ms() - t0;
        if (t >= next_send) {
            uint8_t pos[12];
            next_send = t + SEND_INTERVAL_MS;
            encode_position(pos, 0, (float)((long)t % 10000) / 1000.0f, 0.0f); /* walk 0..10 on x, repeat */
            forward_to_others(srv, 0, POSITION_CHANNEL, pos, 12, 0); /* unreliable: a lost update is replaced by the next */
        }
        if (t >= next_report) {
            next_report = t + 1000;
            if (!code_printed) {
                char code[16];
                if (peerova_room_code(srv, code, sizeof code) > 0) { printf("CODE %s\n", code); code_printed = 1; }
            }
            if (g_peer_count) printf("%lu position updates received so far\n", positions);
        }
        if (exit_when_empty && had_clients && g_peer_count == 0) break;
    }
    printf("server: shutting down\n");
    peerova_free(srv);
    return 0;
}

/* ------------------------------------------------------------------ client */

static int run_client(const char* ticket, int seconds) {
    peerova_node* cli = peerova_client_connect(ticket, "{\"timeout_ms\":20000}");
    if (!cli) { fprintf(stderr, "connect failed: %s\n", peerova_last_error()); return 1; }

    uint32_t server = 0;
    unsigned long positions = 0, chats = 0;
    int chat_sent = 0, rc = 0;
    double t0 = now_ms(), connected_at = 0, next_send = 0, next_chat = 0;
    peerova_event ev;

    while (!g_quit) {
        uint32_t wait = 10;
        while (peerova_poll_wait(cli, &ev, wait)) {
            wait = 0;
            if (ev.type == PEEROVA_EVENT_CONNECTED) {
                server = ev.peer; connected_at = now_ms();
                printf("connected in %.0f ms\n", connected_at - t0);
            } else if (ev.type == PEEROVA_EVENT_CONNECT_FAILED) {
                printf("connect failed: %.*s\n", (int)ev.len, (const char*)ev.data);
                peerova_free(cli);
                return 1;
            } else if (ev.type == PEEROVA_EVENT_DISCONNECTED) {
                printf("server closed the connection\n");
                g_quit = 1;
            } else if (ev.type == PEEROVA_EVENT_MESSAGE && ev.channel == CHAT_CHANNEL) {
                chats++;
                printf("%.*s\n", (int)ev.len, (const char*)ev.data);
            } else if (ev.type == PEEROVA_EVENT_MESSAGE && ev.channel == POSITION_CHANNEL && ev.len == 12) {
                positions++;
            }
        }
        if (!server || g_quit) continue;

        double t = now_ms();
        if (t >= next_send) {
            uint8_t pos[12];
            next_send = t + SEND_INTERVAL_MS;
            encode_position(pos, 0, 0.0f, (float)((long)(t - connected_at) % 10000) / 1000.0f); /* walk 0..10 on y */
            peerova_send(cli, server, POSITION_CHANNEL, pos, 12, 0);
        }
        if (t >= next_chat) {
            char line[64];
            int n = snprintf(line, sizeof line, "hello #%d from C", ++chat_sent);
            next_chat = t + 1000;
            if (peerova_send(cli, server, CHAT_CHANNEL, (const uint8_t*)line, (uint32_t)n, 1) != 0)
                fprintf(stderr, "chat send failed: %s\n", peerova_last_error());
        }
        if (seconds > 0 && t - connected_at >= seconds * 1000.0) break;
    }

    printf("client: received %lu position updates, %lu chat lines; disconnecting\n", positions, chats);
    if (server) peerova_disconnect(cli, server);
    if (!server || positions == 0) rc = 1;
    peerova_free(cli); /* never free while another thread is polling this node */
    return rc;
}

int main(int argc, char** argv) {
    setvbuf(stdout, NULL, _IONBF, 0);
    signal(SIGINT, on_sigint);
    if (argc >= 2 && strcmp(argv[1], "server") == 0) return run_server(argc >= 3 ? atoi(argv[2]) : 0);
    if (argc >= 3 && strcmp(argv[1], "client") == 0) return run_client(argv[2], argc >= 4 ? atoi(argv[3]) : 10);
    fprintf(stderr, "usage: %s server [port] | client <ticket|code> [seconds]\n", argv[0]);
    return 2;
}
