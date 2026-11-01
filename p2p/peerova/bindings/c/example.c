/* gcc example.c -L../../target/release -lpeerova -o example   (peerova.h is vendored next to this file) */
#include <stdio.h>
#include <string.h>
#include "peerova.h"

int main(void) {
    peerova_node* srv = peerova_server_start("{\"port\":0,\"upnp\":false,\"key_file\":\"c-example.key\"}");
    if (!srv) { printf("server error: %s\n", peerova_last_error()); return 1; }
    char ticket[1024];
    peerova_ticket(srv, ticket, sizeof ticket);
    printf("peerova %s ticket %.24s...\n", peerova_version(), ticket);
    peerova_node* cli = peerova_client_connect(ticket, NULL);
    peerova_event ev; uint32_t peer = 0;
    while (peerova_poll_wait(cli, &ev, 10000)) if (ev.type == PEEROVA_EVENT_CONNECTED) { peer = ev.peer; break; }
    if (!peer) { printf("connect failed\n"); return 1; }
    const char* msg = "hello from C";
    peerova_send(cli, peer, 0, (const uint8_t*)msg, (uint32_t)strlen(msg), 1);
    while (peerova_poll_wait(srv, &ev, 5000)) {
        if (ev.type == PEEROVA_EVENT_MESSAGE) {
            printf("server got [ch %u, reliable %u]: %.*s\n", ev.channel, ev.reliable, (int)ev.len, (const char*)ev.data);
            peerova_free(cli); peerova_free(srv); remove("c-example.key");
            return 0;
        }
    }
    printf("no message\n"); return 1;
}
