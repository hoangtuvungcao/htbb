# Peerova stdio sidecar protocol (v1)

`peerova stdio` runs one Peerova node (server or client) as a child process and talks
**newline-delimited JSON** (one UTF-8 object per line) on stdin/stdout. Any language that can spawn a
process and read/write lines gets the full SDK with no native binding: Node.js, Electron, PHP, Ruby,
Perl, Erlang/Elixir, Lua 5.x, Tcl, R, shell, ...

```
peerova stdio server ['<json config>']
peerova stdio client <TICKET | CODE | @file> ['<json config>']
```

* The config JSON is the same object accepted by `peerova_server_start` / `peerova_client_connect`
  (see `include/peerova.h` and [`../DEVELOPER-GUIDE.md`](../DEVELOPER-GUIDE.md)). Supported keys:

  | Side | Key | Meaning |
  |---|---|---|
  | server | `port` | UDP port, default `47000`; `0` = random free port |
  | server | `key_file` | identity key file, default `"server.key"` (created if missing). Same key ⇒ same ticket |
  | server | `license` | `PVA-...` license key. Without it the server runs in dev mode (max 3 clients) |
  | server | `max_ccu` | maximum concurrent clients |
  | server | `token` | shared secret every client must present |
  | server | `upnp` | ask the router for a port mapping, default `true` |
  | server | `private` | the ticket contains no IP addresses, so sharing it does not reveal the server's address, default `false` |
  | server | `app`, `share` | get a 6-character connect code: `app` = project id from the Peerova website, `share` = `{"name","public","max","mode","region"}` |
  | client | `token` | must match the server's `token` |
  | server | `hide_ip` | hide the server IP: players never learn its real address (DDoS protection); slightly higher latency |
  | client | `timeout_ms` | give up after this long, default `20000` |
  | client | `key_file` | client identity key (created if missing), e.g. `<appdata>/<game>/peerova-client.key`; keeps the player's identity and stable address on the server across runs |

  Example server config: `{"port":0,"upnp":false,"key_file":"/var/lib/myapp/server.key"}`.
* The client argument is a ticket (`pvt_...`), a **6-character connect code** (e.g. `K7Q2XD`, resolved
  through the Peerova website), or `@path/to/file` containing the ticket.
* The sidecar does **not** print the server's connect code. Read it in the Peerova website dashboard, or use
  a native binding (`peerova_room_code`) if your server needs it programmatically.
* stdout carries **only** protocol lines; diagnostics go to stderr (log or discard it).
* Connections are automatic: direct first, with an automatic encrypted fallback route when networks block
  direct connections. Traffic is end-to-end encrypted.

## Exit status

| Status | When |
|---|---|
| `0` | after `{"cmd":"quit"}` or when stdin is closed |
| `1` | startup failed (bad config JSON, invalid ticket/code, port in use, ...). One `{"event":"error","message":"E_NAME: ...","code":"E_NAME"}` line is written first (a human-readable reason also goes to stderr), and **no** `ready` line is written |

## Output (sidecar → you)

| Line | Meaning |
|---|---|
| `{"event":"ready","ticket":"pvt_..."}` | Server is listening; share `ticket` with clients. The first line on success. |
| `{"event":"ready"}` | Client started (no ticket); now wait for `connected` or `connect_failed`. |
| `{"event":"connected","peer":N}` | A peer connected. Server: a client (one event per client). Client: the server. |
| `{"event":"message","peer":N,"channel":C,"reliable":true,"data":"<b64url>"}` | Payload from peer `N` on channel `C` (0..255). |
| `{"event":"disconnected","peer":N,"reason":"...","code":"E_CLOSED"}` | Peer `N` is gone. |
| `{"event":"connect_failed","reason":"...","code":"E_TIMEOUT"}` | Client could not reach the server (timeout, wrong token, server full...). The node stays alive; send `quit`. |
| `{"event":"error","message":"...","code":"E_UNKNOWN_PEER"}` | A command was rejected (unparsable JSON, unknown `cmd`, unknown peer, unreliable payload > 1180 bytes...). Non-fatal. Also written once before exit status `1` when startup fails. |

* `peer` is an unsigned 32-bit connection id; on a server it identifies the client, on a client it identifies
  the server. Store it as an integer type that holds values up to 4294967295.
* `reason` and `message` are English text `"E_NAME: description"`; `code` is the stable error code name
  (table: [Error codes](../DEVELOPER-GUIDE.md#error-codes)). Branch on `code`; log the text.
* Unknown fields may be added in later versions: ignore fields you don't know.

## Input (you → sidecar)

| Line | Meaning |
|---|---|
| `{"cmd":"send","peer":N,"channel":C,"reliable":true,"data":"<b64url>"}` | Send bytes. `channel` default 0, `reliable` default `true`. |
| `{"cmd":"disconnect","peer":N}` | Close the connection to peer `N`. |
| `{"cmd":"quit"}` | Shut the node down and exit 0. Closing stdin does the same. |

Blank lines are ignored. Commands are processed in order. A successful `send` produces no output.

## Data encoding

`data` is **base64url without padding** (RFC 4648 §5: alphabet `A-Z a-z 0-9 - _`, no `=`).
Decoders should accept missing padding (add `=` until the length is a multiple of 4 if your library insists).
Payloads are arbitrary bytes; UTF-8 text is just a common case.

| Language | encode / decode |
|---|---|
| Node ≥16 | `buf.toString("base64url")` / `Buffer.from(s, "base64url")` |
| Python | `base64.urlsafe_b64encode(b).rstrip(b"=")` / `base64.urlsafe_b64decode(s + "=" * (-len(s) % 4))` |
| PHP | `rtrim(strtr(base64_encode($b), '+/', '-_'), '=')` / `base64_decode(strtr($s, '-_', '+/'))` |
| Perl | see `b64e` / `b64d` in [`examples/demo.pl`](examples/demo.pl) (`MIME::Base64` + `tr{+/}{-_}`) |
| Ruby | `Base64.urlsafe_encode64(b, padding: false)` / `Base64.urlsafe_decode64(s)` |
| Go | `base64.RawURLEncoding` |
| shell | `base64 \| tr -d '\n=' \| tr '+/' '-_'` / see `pv_b64d` in [`peerova.sh`](peerova.sh) |

## Semantics

* **reliable=true**: ordered and retransmitted *per channel*; different channels never block each other.
  Messages can be large. Use for chat, inventory, RPC, file chunks.
* **reliable=false**: datagram, lowest latency, may be lost or reordered; max **1180 bytes** of payload.
  Use for gameplay inputs, positions, voice-like real-time state where only the latest value matters.
* The sidecar polls the node every ≤5 ms, so the added latency is a few ms plus a JSON/base64 round trip.
  For tight game loops with thousands of messages per second per process, prefer a native FFI binding.
* Backpressure: the sidecar reads stdin as fast as it can; if you write faster than the network drains,
  memory grows inside the sidecar. Pace bulk transfers (e.g. wait for an application-level ack).
* One process = one node. Run several processes for several nodes.

## Lifecycle

1. Spawn with stdin/stdout as pipes (stderr: inherit, log, or discard).
2. Read lines until `ready` (server: keep `ticket`). An `error` line followed by EOF before `ready` = startup
   failure: its `code` tells why (exit status `1`).
3. Loop: read event lines; write command lines. Read stdout continuously (a full pipe stalls the sidecar).
4. Stop: write `{"cmd":"quit"}` (or close stdin) and wait for exit; kill it after a grace period (e.g. 3 s).

## Example session

```
$ peerova stdio server '{"port":0,"upnp":false}'
{"event":"ready","ticket":"pvt_AmQpvhpHt8oz..."}
{"event":"connected","peer":2633379007}
{"event":"message","peer":2633379007,"channel":0,"reliable":true,"data":"aGVsbG8"}
{"cmd":"send","peer":2633379007,"channel":0,"reliable":true,"data":"aGkgYmFjaw"}          <- typed by you
{"event":"disconnected","peer":2633379007,"reason":"E_CLOSED: the peer closed the connection","code":"E_CLOSED"}
{"cmd":"quit"}
```

## Reference clients

| Language | File |
|---|---|
| bash (≥4.1, jq optional) | [`peerova.sh`](peerova.sh) library, [`chat.sh`](chat.sh), test [`test_stdio.sh`](test_stdio.sh) |
| Perl 5 (core modules only) | [`examples/demo.pl`](examples/demo.pl) |
| Node.js / Electron | [`../nodejs/index.js`](../nodejs/index.js) |
| PHP (no ext/ffi) | [`../php/src/PeerovaStdio.php`](../php/src/PeerovaStdio.php) |
| Lua 5.1–5.4 | [`../lua/peerova_stdio.lua`](../lua/peerova_stdio.lua) |
