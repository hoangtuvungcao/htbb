/* peerova — direct P2P networking SDK (end-to-end encrypted).
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
    PEEROVA_EVENT_DISCONNECTED   = 2, /* data/len = UTF-8 reason, "E_NAME: English text" */
    PEEROVA_EVENT_MESSAGE        = 3, /* data/len = payload, channel set */
    PEEROVA_EVENT_CONNECT_FAILED = 4, /* data/len = UTF-8 reason, "E_NAME: English text" */
};

/* Error codes (stable; new codes may be added). peerova_last_error_code() returns one of these;
 * peerova_last_error() and CONNECT_FAILED / DISCONNECTED payloads carry the same name as a prefix:
 * "E_TOO_LARGE: unreliable messages are limited to 1180 bytes". */
enum {
    PEEROVA_E_OK             = 0,  /* no error */
    PEEROVA_E_INVALID_ARG    = 1,  /* NULL/invalid argument, malformed config JSON, bad code format */
    PEEROVA_E_TOO_LARGE      = 2,  /* unreliable payload > 1180 bytes */
    PEEROVA_E_UNKNOWN_PEER   = 3,  /* peer id not connected (left or never existed) */
    PEEROVA_E_CLOSED         = 4,  /* connection closed (DISCONNECTED reasons) */
    PEEROVA_E_CODE_NOT_FOUND = 5,  /* 6-character connect code unknown or expired */
    PEEROVA_E_NETWORK        = 6,  /* socket / port in use / website or license server unreachable */
    PEEROVA_E_TIMEOUT        = 7,  /* connect timed out, server not ready, peer stopped responding */
    PEEROVA_E_LICENSE        = 8,  /* license / lease invalid, expired or rejected */
    PEEROVA_E_CCU_LIMIT      = 9,  /* server's concurrent-client limit reached */
    PEEROVA_E_ACCESS_DENIED  = 10, /* access token wrong / expired / revoked */
    PEEROVA_E_INVALID_TICKET = 11, /* ticket malformed, corrupted or unsupported version */
    PEEROVA_E_IO             = 12, /* server key file unreadable / unwritable / corrupted */
    PEEROVA_E_INTERNAL       = 99, /* internal or unclassified error */
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
 * {"port":47000,"key_file":"server.key","license":"PVA-...","token":"secret",
 *  "max_ccu":100,"upnp":true,"hide_ip":false}
 * "hide_ip": true hides the server IP: players never learn its real address (DDoS protection); tickets carry
 * no address and clients connect only through the automatic encrypted route — slightly higher latency.
 * Blocks until the ticket is ready (≤ ~3 s). NULL on error (see peerova_last_error). */
peerova_node* peerova_server_start(const char* config_json);

/* Client. ticket = a ticket (pvt_...) or a 6-character connect code ("K7Q2XD").
 * config_json (may be NULL): {"token":"secret","timeout_ms":20000,"key_file":"<appdata>/<game>/peerova-client.key"}
 * "key_file" (optional): the client's identity key, created if missing; keep it to keep the same identity
 * across runs (servers map each player to a stable loopback address 127.a.b.c from the token subject, else
 * from this key, and may ban by that address). Without it a new random key is used per run. Store it in the
 * user's app-data folder. Address bans only hold while the player keeps the file: use accounts/tokens for
 * robust identity.
 * Connects directly when possible; otherwise an automatic encrypted fallback route is used
 * and the connection upgrades to direct in the background as soon as it can.
 * With a ticket it returns immediately; with a connect code it first looks the code up
 * (a short blocking call, ≤ a few seconds) and returns NULL if the code is unknown or
 * expired (see peerova_last_error). Then wait for PEEROVA_EVENT_CONNECTED /
 * CONNECT_FAILED via peerova_poll. */
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

/* Closes one connection; a DISCONNECTED event follows (data/len = "E_NAME: English reason"). */
void peerova_disconnect(peerova_node* node, uint32_t peer);

/* Number of connected peers. */
uint32_t peerova_peer_count(peerova_node* node);

void peerova_free(peerova_node* node);

/* Last error for this thread, set when a call reports failure (NULL node / -1 return).
 * peerova_server_start, peerova_client_connect and peerova_send reset it to E_OK / "" on entry.
 * peerova_last_error: English UTF-8 text "E_NAME: message", valid until the next call on this thread.
 * peerova_last_error_code: the matching PEEROVA_E_* value. */
const char* peerova_last_error(void);
int32_t peerova_last_error_code(void);
const char* peerova_version(void);

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
uint32_t peerova_tunnel_peer_count(peerova_tunnel* t);
void peerova_tunnel_free(peerova_tunnel* t);

#ifdef __cplusplus
}
#endif
#endif
