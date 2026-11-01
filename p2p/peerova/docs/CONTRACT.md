# Contract: web (license authority) <-> core (peerova agent / SDK)

## 1. License key (what customers paste)
`PVA-XXXXX-XXXXX-XXXXX-XXXXX` (Crockford base32, uppercase, 20 random chars).
Web stores SHA-256(key) only.

## 2. Lease token (signed, verified OFFLINE by the core)
```
pvl1.<B64URL(payload_json_utf8)>.<B64URL(ed25519_signature_64_bytes)>
```
* base64url, no padding.
* Signature = Ed25519(authority_private_key, ASCII bytes of `pvl1.<B64URL(payload)>`).
* payload is a FLAT JSON object, keys exactly (no nesting, numbers are integers):
```json
{"v":1,"lid":"lic_xxx","pid":"prj_xxx","plan":"indie","srv":3,"ccu":200,"mid":"<machine id hex>","iat":1761955200,"exp":1762560000}
```
* Core accepts a lease iff signature valid AND `mid` == local machine id AND now < exp.
* Web issues exp = now + 7 days (offline grace). Core renews every 30 min (`renew_after` hint).

## 3. HTTP API (JSON)
* `GET  /api/v1/pubkey` -> `{"alg":"Ed25519","key":"<b64url raw 32-byte public key>"}`
* `POST /api/v1/lease`  body `{"key":"PVA-...","mid":"<hex>","hostname":"...","version":"0.2.0"}`
  * 200 `{"lease":"pvl1....","exp":<unix>,"renew_after":<unix>}`
  * 403 `{"error":"invalid_key|expired|revoked|server_limit"}`
  * server_limit: distinct `mid` seen in last 8 days for this license > plan.srv
* `POST /api/v1/telemetry` body `{"lease":"pvl1...","ccu":12,"connects":{"ipv6":3,"mapped":1,"punched":5,"lan":0},"failures":1}` -> 204

## 4. Plans (defaults, editable in web config)
| plan | srv | ccu | price/month |
|---|---|---|---|
| dev (no key) | 1 | 3 | free, enforced by core with no lease |
| trial (14 days, auto on signup) | 1 | 50 | free |
| indie | 3 | 200 | 290.000 VND / 12 USD |
| studio | 10 | 2000 | 1.490.000 VND / 59 USD |
| enterprise | custom | custom | contact; may self-host the authority |

## 5. CLI (for docs)
```
peerova doctor
peerova keygen                                   # server identity key (server.key)
peerova server  --port 47000 --license PVA-... --expose tcp:25565 --expose udp:19132 --expose tcp:443
peerova client  <TICKET> --forward tcp:25565 --forward udp:19132 --forward tcp:8443=443
```
`--forward PROTO:LOCAL[=REMOTE]` listens on 127.0.0.1:LOCAL and tunnels to the server's exposed REMOTE port.
TCP covers HTTP, HTTPS (end-to-end, not decrypted), WebSocket, SSH, gRPC...; UDP covers game traffic, QUIC, voice.

## 6. SDK C ABI (all languages bind this)
```c
peerova_node* peerova_server_start(const char* config_json); // {"port":47000,"key_file":"server.key","license":"PEEROVA-..","supernodes":[]}
peerova_node* peerova_client_connect(const char* ticket, const char* config_json);
int  peerova_ticket(peerova_node*, char* buf, int buf_len);
int  peerova_send(peerova_node*, uint32_t peer, uint8_t channel, const uint8_t* data, uint32_t len, int reliable);
int  peerova_poll(peerova_node*, peerova_event* out);   // 1 = got event, 0 = none
void peerova_free(peerova_node*);
// events: 1 CONNECTED(peer)  2 DISCONNECTED(peer)  3 MESSAGE(peer, channel, data, len)
```
Bindings: C/C++ (Unreal), C# (Unity, Godot .NET), Python, Java/Kotlin, Go (cgo), anything with a C FFI.

## 7. Amendment v2 (anti-crack via network enforcement + auto supernodes)
* `POST /api/v1/lease` body MAY include `"spk":"<b64url 32-byte server static public key>"` and
  `"supernode":"<ip:port>"` (server is reachable and offers itself as a supernode).
  * If `spk` is present, the lease payload MUST include `"spk":"<same value>"` (string). Core verifies
    `spk` == its own key; supernodes verify it against the registering server. Leases without `spk`
    remain valid (backwards compatible).
  * If `supernode` is present and the license is valid, record it (license id, addr, last_seen).
* `GET /api/v1/supernodes` -> `{"supernodes":["ip:port", ...]}`: up to 32 addresses seen in the last
  30 minutes from valid, non-revoked, non-trial licenses, most recent first (cache 60 s).
  Supernodes never carry user data; they only reflect addresses and introduce peers.
* Supernodes only accept registrations carrying a lease signed by the authority (signature + exp + spk),
  so cracked/unlicensed builds cannot use the network. Licensed users are never throttled.

## 8. Amendment v3: website as rendezvous (no VPS, no supernode, no relay)
The website only exchanges addresses at connection start; data always flows peer-to-peer.
The observed public IP is `CF-Connecting-IP` on Cloudflare, or the socket peer address on the Node adapter
(`X-Forwarded-For` only when `TRUST_PROXY=1`). `sid` = b64url of the 32-byte server static public key (it is in the ticket).

* `POST /api/v1/rdv/poll` (server, every ~2 s; also acts as announce/refresh)
  body `{"sid":"<b64url32>","port":<local udp port>,"lan":["10.1.0.2:47000"],"v6":["[2001:db8::1]:47000"],"tcp":true}`
  -> 200 `{"ip":"<server public ip as seen>","poll":2,"clients":[{"session":"<hex u64>","ip":"<client public ip>","port":<client local udp port>,"lan":[...],"v6":[...]}]}`
  * Upserts the server entry (sid → ip, port, lan, v6, tcp, ts); entries expire after 30 s without a poll.
  * Returns AND removes pending client requests for that sid (each request is delivered once).
* `POST /api/v1/rdv/connect` (client)
  body `{"sid":"<b64url32>","session":"<hex u64>","port":<local udp port>,"lan":[...],"v6":[...]}`
  -> 200 `{"ip":"<client public ip as seen>","server":{"ip":"...","port":47000,"lan":[...],"v6":[...],"tcp":true,"age":<seconds since last poll>}}`
  -> 404 `{"error":"offline"}` if no fresh server entry.
  * Queues the request for the server (max 64 pending per sid, expire after 30 s).
* Validation & abuse limits: sid must decode to 32 bytes; ports 1..65535; lan/v6 at most 8 entries each, valid `ip:port` /
  `[v6]:port` strings; per-IP rate limit (poll: 60/min, connect: 30/min) → 429 `{"error":"rate_limited"}`.
  No auth (anyone holding the ticket may look up the server address — same trust level as the ticket itself).
* Storage: D1 tables `rdv_servers(sid PK, ip, port, lan, v6, tcp, ts)` and `rdv_requests(id, sid, session, ip, port, lan, v6, ts)`;
  the Node adapter's memory/sqlite engines must support them. Cache-Control: no-store.

## 9. Amendment v4: lobbies, room codes, live server directory (metadata only, no relay)
`app` = public application id = the project id shown in the dashboard (e.g. `prj_xxx`). Lobby data is
public metadata; the service data path is still peer-to-peer.

* `POST /api/v1/lobby/upsert` (server; called at start and then every ≤15 s as heartbeat)
  body `{"app":"prj_x","ticket":"pvt_...","name":"Phòng của Trọng","public":true,"players":3,"max":16,
         "mode":"survival","region":"VN","version":"1.2","tags":["pvp"],"secret":"<owner secret or empty on first call>"}`
  -> 200 `{"code":"K7Q2XD","secret":"<owner secret>","ttl":45}`
  * First call (no/unknown secret) creates a lobby with a fresh 6-char room code (alphabet `ABCDEFGHJKLMNPQRSTUVWXYZ23456789`,
    unique among live lobbies) and a random 32-byte b64url owner secret. Later calls with the secret update it (same code).
  * The lobby disappears 45 s after the last heartbeat. `DELETE`-equivalent: `POST /api/v1/lobby/close {"code","secret"}` → 204.
  * The ticket must start with `pvt_` and be ≤ 2048 chars; name ≤ 64, mode/region/version ≤ 32, ≤ 8 tags of ≤ 16 chars,
    players/max 0..100000. The `app` must exist (project id); unknown → 404 `{"error":"unknown_app"}`.
  * Per-IP limit 30/min → 429.
* `GET /api/v1/lobby/resolve?code=K7Q2XD` -> 200 `{"ticket":"pvt_...","name":"...","players":3,"max":16,"app":"prj_x"}` or 404 `{"error":"not_found"}`.
  Case-insensitive; per-IP limit 60/min (anti-enumeration: 404 timing constant, no hints).
* `GET /api/v1/lobby/list?app=prj_x[&mode=..&region=..&q=..&limit=50]` -> 200 `{"lobbies":[{"code","name","players","max","mode","region","version","tags","age"}]}`
  Only `public:true` lobbies; ticket NOT included (join via resolve). Sorted by players desc then newest. Cache 5 s.
* `GET /api/v1/lobby/stats?app=prj_x` -> `{"lobbies":N,"players":M}` (for app UIs: "M người đang dùng").
* Dashboard (owner of the project): live table of the project's lobbies/servers (code, name, players/max, public, last heartbeat,
  version, region) + total players chart; admin overview KPI: live lobbies and players.

## 10. Amendment v5: no public information disclosure
* `GET /api/v1/supernodes` now REQUIRES `Authorization: Lease <pvl1 token>`; the lease must verify (signature, not expired,
  license active and not trial). Otherwise 401 `{"error":"unauthorized"}` with no hint. Rate limit 30/min per license.
  (Supernode IPs are customers' server addresses — never public.)
* `GET /api/v1/stats/public` returns only coarse, rounded marketing aggregates (no server/supernode counts, no per-path
  numbers) — or `{}` until a minimum sample size is reached.
* `GET /api/v1/health` → `{"ok":true}` only. No version, time, build or stack info anywhere in responses; errors are
  generic codes (`{"error":"internal"}`), details only in server logs.

## 11. Amendment v6: network-side license enforcement for rendezvous & connect codes
* `POST /api/v1/rdv/poll` and `POST /api/v1/lobby/upsert` accept `Authorization: Lease <pvl1 token>`.
  * Valid lease (signature, not expired, license active; trial counts as licensed) whose `spk` (if present) equals the
    request's `sid` → normal service. For lobby: `players`/`max` are capped at the lease's `ccu`.
  * No/invalid lease → **dev mode**: rdv poll delivers at most 3 client requests per minute per sid (extra requests are
    dropped silently, client sees offline); lobby upsert allowed only with `public:false` and `max ≤ 3`, and its code
    resolves at most 10 times per hour. Response field `"mode":"dev"|"licensed"` (no other hints).
* Rationale: a cracked server cannot forge leases, so it only gets dev-mode service from our network.

## 12. Amendment v7: automatic last-resort relay (fallback only)
Used only when every direct path (LAN, IPv6, public IP, UPnP/NAT-PMP, hole punching, direct TCP) fails.
Payload stays end-to-end encrypted (Noise); the relay forwards opaque packets and never sees plaintext.

* Endpoint: `GET /api/v1/relay/<room>?role=s|c&lane=N` with `Upgrade: websocket` (RFC 6455).
  * `<room>` = 32 lowercase hex chars (128-bit random, chosen per session and exchanged via the rendezvous).
  * `role` = `s` (server) or `c` (client); `lane` = 0..3 (parallel sockets; default 0). Same room + lane pairs `s` with `c`.
    A message on a lane whose partner lane is missing goes to any open lane of the partner.
  * Errors (no upgrade): bad path 404 `not_found`, bad role/lane 400 `bad_request`, missing `Upgrade: websocket` 426
    `websocket_required`. A new socket for the same role+lane replaces the old one.
* Framing: every WebSocket **binary** message carries one or more frames `len u16 BE | packet` (concatenated).
  Text messages are ignored. Max message size 64 KiB (larger → connection closed).
* Tiers: the server side (`role=s`) may send `Authorization: Lease <pvl1 token>`; a valid lease (signature, not expired,
  license active; trial counts) makes the room **licensed**, otherwise **dev**. Client sockets cannot raise the tier.
  * dev: at most 50 MiB forwarded and 10 minutes per room, then the room is closed (close code 4002 quota / 4003 time).
  * licensed: no byte cap; 24 h per room.
  * A room whose partner does not arrive within 30 s is closed (4001). When one side is gone, the other is closed.
  * Messages sent before the partner connects are dropped (the engine retransmits).
* Discovery: when the operator sets `RELAY_URL` (an `https://` base URL), `POST /api/v1/rdv/poll` and
  `POST /api/v1/rdv/connect` include `"relay": "<base url>"`; clients then connect to `<relay>/api/v1/relay/<room>…`.
  Without the field, clients use the same site as the API. The relay is a **fallback only**: clients must keep
  trying/preferring direct paths and switch back when a direct path becomes available.
* Long-poll (same amendment): `POST /api/v1/rdv/poll` accepts optional `"wait": N` (0..25 s). With no pending client
  requests the server re-checks every ~500 ms until one arrives or N seconds pass. Omitting `wait` keeps the old behaviour.
