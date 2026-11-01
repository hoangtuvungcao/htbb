// node test/test.mjs   (needs the peerova sidecar binary: see README "Where to put the binary", or set PEEROVA_BIN)
import assert from "node:assert/strict";
import { once } from "node:events";
import { mkdtempSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { Peerova, PeerovaError, ErrorCode } from "../index.js";

const N = 200;
const withTimeout = (p, ms, what) =>
  Promise.race([p, new Promise((_, rej) => setTimeout(() => rej(new Error(`timeout: ${what}`)), ms).unref())]);

const t0 = performance.now();
const key = join(mkdtempSync(join(tmpdir(), "peerova-node-")), "node.key");
const srv = await Peerova.server({ port: 0, upnp: false, key_file: key });
assert.match(srv.ticket, /^pvt_/);

const got = [];
let unreliable = 0;
let srvPeer = null;
const allReliable = new Promise((resolve) => {
  srv.on("connected", (p) => (srvPeer = p));
  srv.on("message", (peer, data, { channel, reliable }) => {
    if (reliable) {
      assert.equal(channel, 1);
      got.push(data.toString());
      if (got.length === N) resolve(peer);
    } else {
      assert.equal(channel, 2);
      assert.equal(data.toString(), "realtime");
      unreliable++;
    }
  });
});

const cli = await Peerova.client(srv.ticket, { timeout_ms: 10000 });
const [peer] = await withTimeout(once(cli, "connected"), 10000, "client connected");
for (let i = 0; i < N; i++) cli.send(peer, `msg ${i}`, { channel: 1, reliable: true });
cli.send(peer, "realtime", { channel: 2, reliable: false });
assert.throws(() => cli.send(peer, Buffer.alloc(2000), { reliable: false }), (e) => e instanceof PeerovaError && e.code === "E_TOO_LARGE");

const fromPeer = await withTimeout(allReliable, 5000, "reliable messages");
assert.deepEqual(got, Array.from({ length: N }, (_, i) => `msg ${i}`));
assert.equal(fromPeer, srvPeer);

// binary reply server -> client
const reply = Buffer.from([0x70, 0x6f, 0x6e, 0x67, 0x00, 0xff]);
const gotReply = once(cli, "message");
srv.send(fromPeer, reply, { channel: 0 });
const [rp, rdata, rinfo] = await withTimeout(gotReply, 5000, "reply");
assert.equal(rp, peer);
assert.deepEqual(rdata, reply);
assert.deepEqual(rinfo, { channel: 0, reliable: true });
// the datagram may land after the last reliable message — wait up to 1 s (on loopback it is never lost)
for (let i = 0; i < 20 && unreliable === 0; i++) await new Promise((r) => setTimeout(r, 50));
assert.equal(unreliable, 1, "unreliable datagram not received");
assert.equal(srv.peerCount, 1);

// disconnect is observed on the client
const disc = once(cli, "disconnected");
srv.disconnect(fromPeer);
await withTimeout(disc, 5000, "disconnected");

// bad ticket -> startup error
await assert.rejects(Peerova.client("pvt_garbage"), (e) => e instanceof PeerovaError && e.code === "E_INVALID_TICKET" && /^E_INVALID_TICKET: [\x20-\x7e]+$/.test(e.message));
assert.equal(ErrorCode.E_INVALID_TICKET, 11);

await cli.close();
await srv.close();
assert.ok(srv.closed && cli.closed);
console.log(
  `node PASS: ${N} reliable msgs in order, unreliable received=${unreliable}, binary reply ok, disconnect ok, ` +
    `${(performance.now() - t0).toFixed(0)} ms (node ${process.version})`,
);
