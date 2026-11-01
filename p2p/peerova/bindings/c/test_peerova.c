/* Round-trip test for the C ABI.
 *
 *   cc test_peerova.c -I. -L../../target/release -lpeerova -Wl,-rpath,'$ORIGIN/../../target/release' -o test_peerova
 *   ./test_peerova                 # server + client in one process
 *   ./test_peerova server [port]   # print "TICKET <ticket>", echo every message back, exit when the client leaves
 *   ./test_peerova client <ticket> # connect to a remote server started with "server"
 *
 * Client side: send N reliable messages on channel 0 + 1 unreliable on channel 1, expect the
 * server to echo each back (same channel/reliability), verify order and content, disconnect.
 * Exit code 0 = PASS.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "peerova.h"

#define N_RELIABLE 50

static int fail(const char* what) {
    printf("FAIL: %s (last_error: %s)\n", what, peerova_last_error());
    return 1;
}

static double now_ms(void) {
    struct timespec ts; clock_gettime(CLOCK_MONOTONIC, &ts);
    return ts.tv_sec * 1000.0 + ts.tv_nsec / 1e6;
}

/* Server side of one step: echo any message back to its sender. Returns event type handled. */
static int serve_one(peerova_node* srv, uint32_t timeout_ms, int* disconnected) {
    peerova_event ev;
    if (!peerova_poll_wait(srv, &ev, timeout_ms)) return 0;
    if (ev.type == PEEROVA_EVENT_MESSAGE) {
        /* copy before send: data is only valid until the next poll, but be safe anyway */
        uint8_t buf[2048]; uint32_t n = ev.len < sizeof buf ? ev.len : (uint32_t)sizeof buf;
        memcpy(buf, ev.data, n);
        if (peerova_send(srv, ev.peer, ev.channel, buf, n, ev.reliable) != 0)
            printf("server: echo send failed: %s\n", peerova_last_error());
    } else if (ev.type == PEEROVA_EVENT_DISCONNECTED) {
        *disconnected = 1;
    }
    return ev.type;
}

/* Client side. If srv != NULL it is pumped in the same thread (in-process mode). */
static int run_client(const char* ticket, peerova_node* srv) {
    int srv_disc = 0;
    peerova_node* cli = peerova_client_connect(ticket, "{\"timeout_ms\":20000}");
    if (!cli) return fail("client_connect");
    peerova_event ev; uint32_t peer = 0; double t0 = now_ms();
    while (!peer && now_ms() - t0 < 25000) {
        if (srv) serve_one(srv, 0, &srv_disc);
        if (peerova_poll_wait(cli, &ev, srv ? 10 : 500)) {
            if (ev.type == PEEROVA_EVENT_CONNECTED) peer = ev.peer;
            else if (ev.type == PEEROVA_EVENT_CONNECT_FAILED) {
                printf("connect failed: %.*s\n", (int)ev.len, (const char*)ev.data);
                peerova_free(cli); return 1;
            }
        }
    }
    if (!peer) { peerova_free(cli); return fail("timeout waiting for CONNECTED"); }
    printf("client connected (peer %u) in %.0f ms\n", peer, now_ms() - t0);

    char msg[64];
    for (int i = 0; i < N_RELIABLE; i++) {
        int n = snprintf(msg, sizeof msg, "rel-%d", i);
        if (peerova_send(cli, peer, 0, (const uint8_t*)msg, (uint32_t)n, 1) != 0) { peerova_free(cli); return fail("send reliable"); }
    }
    const char* un = "unreliable-ping";
    if (peerova_send(cli, peer, 1, (const uint8_t*)un, (uint32_t)strlen(un), 0) != 0) { peerova_free(cli); return fail("send unreliable"); }

    int next = 0, got_unrel = 0, bad = 0; t0 = now_ms();
    while ((next < N_RELIABLE || !got_unrel) && now_ms() - t0 < 15000) {
        if (srv) serve_one(srv, 0, &srv_disc);
        if (!peerova_poll_wait(cli, &ev, srv ? 5 : 200)) {
            /* unreliable may be lost: resend it while waiting */
            if (!got_unrel && next == N_RELIABLE) peerova_send(cli, peer, 1, (const uint8_t*)un, (uint32_t)strlen(un), 0);
            continue;
        }
        if (ev.type != PEEROVA_EVENT_MESSAGE) { printf("client: unexpected event %d\n", ev.type); continue; }
        if (ev.reliable) {
            int n = snprintf(msg, sizeof msg, "rel-%d", next);
            if (ev.channel != 0 || ev.len != (uint32_t)n || memcmp(ev.data, msg, n) != 0) {
                printf("client: out-of-order/corrupt reliable echo: %.*s (want %s)\n", (int)ev.len, (const char*)ev.data, msg);
                bad = 1; break;
            }
            next++;
        } else {
            if (ev.channel != 1 || ev.len != strlen(un) || memcmp(ev.data, un, ev.len) != 0) { printf("client: bad unreliable echo\n"); bad = 1; break; }
            got_unrel = 1;
        }
    }
    if (bad || next < N_RELIABLE || !got_unrel) {
        printf("FAIL: reliable %d/%d, unreliable %d\n", next, N_RELIABLE, got_unrel);
        peerova_free(cli); return 1;
    }
    printf("client: %d reliable echoes in order + unreliable echo OK (%.1f ms)\n", N_RELIABLE, now_ms() - t0);

    peerova_disconnect(cli, peer);
    if (srv) {
        t0 = now_ms();
        while (!srv_disc && now_ms() - t0 < 5000) serve_one(srv, 50, &srv_disc);
        if (!srv_disc) { peerova_free(cli); return fail("server did not see DISCONNECTED"); }
        printf("server saw DISCONNECTED\n");
    }
    peerova_free(cli);
    return 0;
}

static peerova_node* start_server(int port) {
    char cfg[160];
    snprintf(cfg, sizeof cfg, "{\"port\":%d,\"upnp\":false,\"key_file\":\"c-test-%d.key\"}", port, port);
    return peerova_server_start(cfg);
}

int main(int argc, char** argv) {
    setvbuf(stdout, NULL, _IONBF, 0);
    printf("peerova %s\n", peerova_version());
    if (argc >= 3 && strcmp(argv[1], "client") == 0) {
        int rc = run_client(argv[2], NULL);
        printf(rc ? "C client FAIL\n" : "C client PASS\n");
        return rc;
    }
    if (argc >= 2 && strcmp(argv[1], "server") == 0) {
        int port = argc >= 3 ? atoi(argv[2]) : 0;
        peerova_node* srv = start_server(port);
        if (!srv) return fail("server_start");
        char ticket[2048];
        if (peerova_ticket(srv, ticket, sizeof ticket) <= 0) return fail("ticket");
        printf("TICKET %s\n", ticket);
        int disc = 0, connected = 0; double t0 = now_ms();
        while (!disc && now_ms() - t0 < 300000) if (serve_one(srv, 500, &disc) == PEEROVA_EVENT_CONNECTED && !connected) { connected = 1; printf("server: client connected\n"); }
        printf(disc ? "server: client disconnected, exiting\n" : "server: timeout\n");
        peerova_free(srv);
        return disc ? 0 : 1;
    }
    peerova_node* srv = start_server(0);
    if (!srv) return fail("server_start");
    char ticket[2048];
    if (peerova_ticket(srv, ticket, sizeof ticket) <= 0) return fail("ticket");
    int rc = run_client(ticket, srv);
    peerova_free(srv);
    remove("c-test-0.key");
    printf(rc ? "C FAIL\n" : "C PASS\n");
    return rc;
}
