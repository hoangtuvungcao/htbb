/* peerova — direct P2P networking SDK (no relay, end-to-end encrypted).
 * C ABI, stable across languages. Thread-safe per node (internal mutex).
 * Link: libpeerova.so / peerova.dll / libpeerova.dylib (or the static lib).
 */
#ifndef PEEROVA_H
#define PEEROVA_H
#include <stdint.h>
#ifdef __cplusplus
extern "C" {
#endif

typedef struct peerova_node peerova_node;

enum {
    PEEROVA_EVENT_CONNECTED      = 1, /* peer connected (server: a client; client: the server) */
    PEEROVA_EVENT_DISCONNECTED   = 2,
    PEEROVA_EVENT_MESSAGE        = 3, /* data/len = payload, channel set */
    PEEROVA_EVENT_CONNECT_FAILED = 4, /* data/len = UTF-8 reason */
};

typedef struct {
    int32_t        type;
    uint32_t       peer;     /* peer id (connection id) */
    uint8_t        channel;  /* 0..255 */
    uint8_t        reliable; /* 1 if it arrived on a reliable channel */
    const uint8_t* data;     /* valid until the next peerova_poll / peerova_free on this node */
    uint32_t       len;
} peerova_event;

/* Server. config_json (may be NULL):
 * {"port":47000,"key_file":"server.key","license":"PVA-...","license_server":"https://...",
 *  "authority_pubkey":"<b64>","supernodes":["host:port"],"supernode_mode":false,
 *  "private":false,"token":"secret","max_ccu":100,"upnp":true,
 *  "tcp_fallback":true,"auto_supernode":true,"rendezvous":true | "https://your.site"}
 * Blocks until the ticket is ready (≤ ~3 s). NULL on error (see peerova_last_error). */
peerova_node* peerova_server_start(const char* config_json);

/* Client. config_json (may be NULL): {"token":"secret","timeout_ms":20000,"supernodes":[...],"force_tcp":false}
 * Returns immediately; wait for PEEROVA_EVENT_CONNECTED / CONNECT_FAILED via peerova_poll. */
peerova_node* peerova_client_connect(const char* ticket, const char* config_json);

/* Copies the NUL-terminated ticket into buf. Returns required size incl. NUL (0 if none). */
int32_t peerova_ticket(peerova_node* node, char* buf, int32_t buf_len);

/* 6-character connect code (e.g. "K7Q2XD") once the website assigned it — server started with
 * {"app":"prj_x","share":{"name":"...","public":true,"max":16,"mode":"...","region":"VN"}}
 * (any service: web/HTTPS, SSH, database, game…; "lobby" is accepted as an alias).
 * Returns required size incl. NUL, or 0 if not (yet) available. Clients may pass the code instead
 * of a ticket to peerova_client_connect (resolved via the website; config "site" overrides it). */
int32_t peerova_room_code(peerova_node* node, char* buf, int32_t buf_len);

/* reliable=1: ordered, retransmitted (per channel, no head-of-line blocking across channels).
 * reliable=0: unreliable datagram, lowest latency, max 1180 bytes. Returns 0 on success. */
int32_t peerova_send(peerova_node* node, uint32_t peer, uint8_t channel,
                  const uint8_t* data, uint32_t len, int32_t reliable);

/* Non-blocking. Returns 1 and fills *out if an event is available, else 0. */
int32_t peerova_poll(peerova_node* node, peerova_event* out);

/* Like peerova_poll but waits up to timeout_ms for an event. */
int32_t peerova_poll_wait(peerova_node* node, peerova_event* out, uint32_t timeout_ms);

void peerova_disconnect(peerova_node* node, uint32_t peer);

/* Number of connected peers. */
uint32_t peerova_peer_count(peerova_node* node);

void peerova_free(peerova_node* node);

/* ---- Port tunnel (in-process `peerova server --expose` / `peerova client --forward`) ----
 * For apps that already speak TCP: the server exposes local services, the client gets a local
 * 127.0.0.1 port per forward rule; every TCP connection is an independent reliable stream.
 * States: */
enum {
    PEEROVA_TUNNEL_CONNECTING = 0,
    PEEROVA_TUNNEL_UP         = 1, /* server: ticket ready; client: connected + local listeners bound */
    PEEROVA_TUNNEL_DOWN       = 2, /* final: failed / disconnected / stopped (see peerova_tunnel_error) */
};
typedef struct peerova_tunnel peerova_tunnel;

/* Server. config_json = peerova_server_start config + "expose":["tcp:19129","udp:7777=10.0.0.5:7777",...].
 * Blocks until the ticket is ready (<= ~5 s). NULL on error (peerova_last_error). */
peerova_tunnel* peerova_tunnel_server(const char* config_json);
/* Client. config_json = peerova_client_connect config + "forward":["tcp:LOCAL=REMOTE",...] (LOCAL 0 = pick
 * a free port), optional "bind":"127.0.0.1". Returns immediately; wait for UP with peerova_tunnel_wait. */
peerova_tunnel* peerova_tunnel_client(const char* ticket, const char* config_json);
int32_t peerova_tunnel_state(peerova_tunnel* t);
/* Waits up to timeout_ms while CONNECTING; returns the state. */
int32_t peerova_tunnel_wait(peerova_tunnel* t, uint32_t timeout_ms);
/* Client: local port of forward rule #index (known right after peerova_tunnel_client). 0 if none. */
int32_t peerova_tunnel_local_port(peerova_tunnel* t, int32_t index);
/* String getters: copy NUL-terminated text into buf, return required size incl. NUL (0 = none). */
int32_t peerova_tunnel_ticket(peerova_tunnel* t, char* buf, int32_t buf_len);
int32_t peerova_tunnel_error(peerova_tunnel* t, char* buf, int32_t buf_len);
/* Pops the oldest log line only when buf is large enough (call with NULL first to get the size). */
int32_t peerova_tunnel_next_log(peerova_tunnel* t, char* buf, int32_t buf_len);
/* Server: "ip:port" of the remote peer owning the loopback TCP connection whose source port (as seen by
 * the exposed service, e.g. Socket.getPort()) is src_port; waits up to wait_ms for the mapping. */
int32_t peerova_tunnel_peer_of(peerova_tunnel* t, uint16_t src_port, uint32_t wait_ms, char* buf, int32_t buf_len);
uint32_t peerova_tunnel_peer_count(peerova_tunnel* t);
void peerova_tunnel_free(peerova_tunnel* t);

/* Last error message for this thread (UTF-8, static until next call). */
const char* peerova_last_error(void);
const char* peerova_version(void);

#ifdef __cplusplus
}
#endif
#endif
