// bun test   (from bindings/bun; needs target/release/libpeerova.so or PEEROVA_LIB)
import { expect, test } from "bun:test";
import { mkdtempSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { EventType, PeerovaClient, PeerovaError, PeerovaServer, version } from "./peerova.ts";

test("roundtrip: 200 reliable in order + unreliable + reply", () => {
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
  let reply = cli.poll(5000);
  while (reply && reply.type !== EventType.Message) reply = cli.poll(5000);
  expect(Array.from(reply!.data)).toEqual([0x70, 0x6f, 0x6e, 0x67, 0, 0xff]);
  console.log(`bun PASS: peerova ${version()}, ${N} reliable msgs in order, unreliable=${unreliable}, reply ok, peers=${srv.peerCount}`);
});
