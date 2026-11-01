# peerova for Node.js / Electron / Bun

Direct P2P networking (no relay, end-to-end encrypted) with **zero native dependencies**: the package
spawns the `peerova stdio` sidecar (the `peerova` CLI binary) and talks JSON lines to it
([protocol](../stdio/PROTOCOL.md)). ESM, ships `index.d.ts`. Node ≥18.

```js
import { Peerova } from "peerova";

const srv = await Peerova.server({ port: 47000 });          // resolves when the ticket is ready
console.log(srv.ticket);
srv.on("message", (peer, data, { channel, reliable }) => srv.send(peer, data, { channel })); // echo

const cli = await Peerova.client(srv.ticket);
cli.on("connected", (peer) => cli.send(peer, "hello", { channel: 0, reliable: true }));
cli.on("message", (peer, data) => console.log(data.toString()));
cli.on("connectFailed", (reason) => console.error(reason));
// ... later
await cli.close(); await srv.close();
```

API: `Peerova.server(config?, options?)`, `Peerova.client(ticket, config?, options?)` → `Promise<Peerova>`;
`.send(peer, data: string|Uint8Array, { channel = 0, reliable = true })` (returns `false` when the pipe is
congested — `await node.drain()`), `.disconnect(peer)`, `.peers` / `.peerCount`, `.ticket`, `.close()`.
Events: `ready`, `connected(peer)`, `message(peer, Buffer, {channel, reliable})`, `disconnected(peer, reason)`,
`connectFailed(reason)`, `error(PeerovaError)` (non-fatal; not thrown when unhandled), `close(code)`.
`config` is passed verbatim to the SDK (see `ServerConfig` / `ClientConfig` in `index.d.ts`).

## Finding the binary

`options.binary` → `$PEEROVA_BIN` → `./bin/peerova[.exe]` inside the package → `../../target/release/peerova`
(repo checkout) → `peerova` on `PATH`. To ship it, copy the CLI for each target into `bin/`
(`cargo build --release -p peerova-cli` → `target/release/peerova`; Windows: `--target x86_64-pc-windows-gnu`
→ `peerova.exe`).

## Electron

Main process (or a `utilityProcess`) only — not the renderer. The binary cannot run from inside `app.asar`:
unpack it (electron-builder: `"asarUnpack": ["**/node_modules/peerova/bin/**"]`) or put it in
`extraResources` and pass `{ binary: path.join(process.resourcesPath, "peerova") }`. Forward events to the
renderer with IPC.

## Bun

The package uses only `node:child_process` / `node:readline` / `Buffer`, which Bun implements; for in-process
native FFI on Bun see [`../bun`](../bun). (Not run under Bun here — Bun is not installed on the build machine.)

## Test

```sh
node test/test.mjs                                   # 200 reliable msgs in order + 1 unreliable + binary reply + disconnect
tsc -p test/tsconfig.json                            # type-check index.d.ts (needs @types/node)
```
