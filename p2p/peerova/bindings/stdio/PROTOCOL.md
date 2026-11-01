# peerova stdio sidecar protocol (v1)

`peerova stdio` runs one peerova node (server or client) as a child process and talks
**newline-delimited JSON** (one UTF-8 object per line) on stdin/stdout. Any language that can spawn a
process and read/write lines gets the full SDK with no native bindings: Node.js, Electron, Bun, Deno,
PHP, Ruby, Perl, Erlang/Elixir, Lua 5.x, Tcl, R, shell, ...

```
peerova stdio server ['<json config>']
peerova stdio client <TICKET> ['<json config>']
```

* The config JSON is the same object accepted by `peerova_server_start` / `peerova_client_connect`
  (see [`crates/peerova-ffi/include/peerova.h`](../../crates/peerova-ffi/include/peerova.h)). Example server config:
  `{"port":0,"upnp":false,"key_file":"/var/lib/myapp/server.key"}`. Without a `license` the server accepts 3 clients.
* `<TICKET>` may also be `@path/to/file` containing the ticket (same rule as the other CLI commands).
* stdout carries **only** protocol lines; diagnostics go to stderr (ignore or log it).
* If startup fails (bad config, bad ticket, port in use) the process prints the error on stderr and exits
  with a non-zero status **before** emitting `ready`.

## Output (sidecar → you)

| Line | Meaning |
|---|---|
| `{"event":"ready","ticket":"pvt_..."}` | Server is listening; share `ticket` with clients. Always the first line. |
| `{"event":"ready"}` | Client started (no ticket); now wait for `connected` or `connect_failed`. |
| `{"event":"connected","peer":N}` | A peer connected. Server: a client (one per client). Client: the server. |
| `{"event":"message","peer":N,"channel":C,"reliable":true,"data":"<b64url>"}` | Payload from peer `N` on channel `C` (0..255). |
| `{"event":"disconnected","peer":N,"reason":"..."}` | Peer `N` is gone. |
| `{"event":"connect_failed","reason":"..."}` | Client could not reach the server (timeout, wrong token, server full...). |
| `{"event":"error","message":"..."}` | A command was rejected (unparsable JSON, unknown peer, unreliable payload > 1180 bytes...). Non-fatal. |

`peer` is a `u32` connection id; on a server it identifies the client, on a client it identifies the server.

## Input (you → sidecar)

| Line | Meaning |
|---|---|
| `{"cmd":"send","peer":N,"channel":C,"reliable":true,"data":"<b64url>"}` | Send bytes. `channel` default 0, `reliable` default `true`. |
| `{"cmd":"disconnect","peer":N}` | Close the connection to peer `N`. |
| `{"cmd":"quit"}` | Shut the node down and exit 0. Closing stdin does the same. |

Blank lines are ignored. Commands are processed in order.

## Data encoding

`data` is **base64url without padding** (RFC 4648 §5: alphabet `A-Z a-z 0-9 - _`, no `=`).
Decoders should accept missing padding (add `=` until the length is a multiple of 4 if your library insists).
Payloads are arbitrary bytes; UTF-8 text is just a common case.

| Language | encode / decode |
|---|---|
| Node ≥16 / Bun | `buf.toString("base64url")` / `Buffer.from(s, "base64url")` |
| Python | `base64.urlsafe_b64encode(b).rstrip(b"=")` / `base64.urlsafe_b64decode(s + "=" * (-len(s) % 4))` |
| PHP | `rtrim(strtr(base64_encode($b), '+/', '-_'), '=')` / `base64_decode(strtr($s, '-_', '+/'))` |
| Ruby | `Base64.urlsafe_encode64(b, padding: false)` / `Base64.urlsafe_decode64(s)` |
| Go | `base64.RawURLEncoding` |
| shell | `base64 \| tr -d '\n=' \| tr '+/' '-_'` / see `pv_b64d` in `peerova.sh` |

## Semantics

* **reliable=true**: ordered and retransmitted *per channel*; different channels never block each other.
* **reliable=false**: datagram, lowest latency, may be lost or reordered; max **1180 bytes** of payload.
* The sidecar polls the node every ≤5 ms, so the added latency is a few ms plus a JSON/base64 round trip.
  For tight game loops with thousands of messages/s per process prefer a native FFI binding.
* Backpressure: the sidecar reads stdin as fast as it can; if you write faster than the network drains,
  memory grows inside the sidecar. Pace bulk transfers (e.g. wait for an application-level ack).
* One process = one node. Run several processes for several nodes.

## Lifecycle

1. Spawn with stdin/stdout as pipes (stderr: inherit, log, or discard).
2. Read the first line; it must be `ready` (server: keep `ticket`). EOF before that = startup failure → read stderr / exit code.
3. Loop: read event lines; write command lines.
4. Stop: write `{"cmd":"quit"}` (or close stdin) and wait for exit; kill after a grace period.

## Example session

```
$ peerova stdio server '{"port":0,"upnp":false}'
{"event":"ready","ticket":"pvt_AmQpvhpHt8oz..."}
{"event":"connected","peer":2633379007}
{"event":"message","peer":2633379007,"channel":0,"reliable":true,"data":"aGVsbG8"}
{"cmd":"send","peer":2633379007,"channel":0,"reliable":true,"data":"aGkgYmFjaw"}          <- typed by you
{"event":"disconnected","peer":2633379007,"reason":"..."}
{"cmd":"quit"}
```

## Reference clients in this repo

| Language | File |
|---|---|
| bash (≥4.1, jq optional) | [`peerova.sh`](peerova.sh) library, [`chat.sh`](chat.sh), test [`test_stdio.sh`](test_stdio.sh) |
| Node.js / Electron / Bun | [`../nodejs/index.js`](../nodejs/index.js) |
| PHP (no ext/ffi) | [`../php/src/PeerovaStdio.php`](../php/src/PeerovaStdio.php) |
| Lua 5.1–5.4 | [`../lua/peerova_stdio.lua`](../lua/peerova_stdio.lua) |
