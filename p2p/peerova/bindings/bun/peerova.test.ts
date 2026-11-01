// bun test   (from bindings/bun; needs the native library — see README — or PEEROVA_LIB)
import { expect, test } from "bun:test";
import { mkdtempSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { EventType, PeerovaClient, PeerovaError, PeerovaServer, version } from "./peerova.ts";

test("roundtrip: 200 reliable in order + unreliable both ways + reply + disconnect", () => {
  const N = 200;
  const key = join(mkdtempSync(join(tmpdir(), "peerova-bun-")), "bun.key");
  using srv = PeerovaServer.start({ port: 0, upnp: false, key_file: key });
  expect(srv.ticket.startsWith("pvt_")).toBe(true);
  using cli = PeerovaClient.connect(srv.ticket);

  const ev = cli.poll(10_000);
  expect(ev?.type).toBe(EventType.Connected);
  const peer = ev!.peer;
  for (let i = 0; i < N; i++) cli.send(peer, `msg ${i}`, { channel: 1, reliable: true });
  cli.send(peer, "realtime", { channel: 2, reliable: false });
  expect(() => cli.send(peer, new Uint8Array(2000), { reliable: false })).toThrow(PeerovaError);

  const got: string[] = [];
  let sp = -1;
  let unreliable = 0;
  for (const e of srv.events(3000)) {
    if (e.type === EventType.Connected) sp = e.peer;
    else if (e.type === EventType.Message && e.reliable) {
      expect(e.channel).toBe(1);
      got.push(e.text);
      if (got.length === N) break;
    } else if (e.type === EventType.Message) unreliable++;
  }
  expect(got).toEqual(Array.from({ length: N }, (_, i) => `msg ${i}`));

  // the datagram may land after the last reliable message (on loopback it is never lost)
  while (unreliable === 0) {
    const e = srv.poll(1000);
    if (!e) break;
    if (e.type === EventType.Message && !e.reliable && e.text === "realtime") unreliable++;
  }
  expect(unreliable).toBe(1);
  srv.send(sp, new Uint8Array([0x70, 0x6f, 0x6e, 0x67, 0, 0xff]));
  srv.send(sp, "srv-realtime", { channel: 3, reliable: false });
  let binOk = false;
  let backUnreliable = false;
  for (let i = 0; i < 20 && !(binOk && backUnreliable); i++) {
    const r = cli.poll(1000);
    if (!r || r.type !== EventType.Message) continue;
    if (r.reliable) {
      expect(Array.from(r.data)).toEqual([0x70, 0x6f, 0x6e, 0x67, 0, 0xff]);
      binOk = true;
    } else if (r.channel === 3 && r.text === "srv-realtime") backUnreliable = true;
  }
  expect(binOk).toBe(true);
  expect(backUnreliable).toBe(true);
  const peers = srv.peerCount;

  // client leaves -> server sees DISCONNECTED
  cli.close();
  let gone = false;
  for (let i = 0; i < 10 && !gone; i++) {
    const e = srv.poll(1000);
    if (e?.type === EventType.Disconnected && e.peer === sp) gone = true;
  }
  expect(gone).toBe(true);
  console.log(`bun PASS: peerova ${version()}, ${N} reliable msgs in order, unreliable both ways, reply ok, peers=${peers}, disconnect ok`);
});
