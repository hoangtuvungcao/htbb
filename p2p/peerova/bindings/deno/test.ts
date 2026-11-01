// deno test --allow-ffi --allow-env --allow-read --allow-write test.ts
import { EventType, PeerovaClient, PeerovaError, PeerovaServer, version } from "./mod.ts";

function assert(cond: unknown, msg: string): asserts cond {
  if (!cond) throw new Error(msg);
}

Deno.test("peerova roundtrip: 200 reliable in order + unreliable both ways + reply + disconnect", async () => {
  const N = 200;
  const dir = Deno.makeTempDirSync({ prefix: "peerova-deno-" });
  using srv = await PeerovaServer.startAsync({ port: 0, upnp: false, key_file: `${dir}/deno.key` });
  assert(srv.ticket.startsWith("pvt_"), "ticket");
  using cli = PeerovaClient.connect(srv.ticket);

  const ev = await cli.pollAsync(10_000);
  assert(ev?.type === EventType.Connected, `expected CONNECTED, got ${ev?.type}`);
  const peer = ev.peer;
  for (let i = 0; i < N; i++) cli.send(peer, `msg ${i}`, { channel: 1, reliable: true });
  cli.send(peer, "realtime", { channel: 2, reliable: false });
  let threw = false;
  try {
    cli.send(peer, new Uint8Array(2000), { reliable: false });
  } catch (e) {
    threw = e instanceof PeerovaError;
  }
  assert(threw, "oversized unreliable must throw");

  const got: string[] = [];
  let sp = -1;
  let unreliable = 0;
  for await (const e of srv.events(3000)) {
    if (e.type === EventType.Connected) sp = e.peer;
    else if (e.type === EventType.Message && e.reliable) {
      assert(e.channel === 1, "channel");
      got.push(e.text);
      if (got.length === N) break;
    } else if (e.type === EventType.Message) unreliable++;
  }
  assert(got.length === N && got.every((m, i) => m === `msg ${i}`), `order: got ${got.length}`);

  // the datagram may land after the last reliable message (on loopback it is never lost)
  while (unreliable === 0) {
    const e = await srv.pollAsync(1000);
    if (!e) break;
    if (e.type === EventType.Message && !e.reliable && e.text === "realtime") unreliable++;
  }
  assert(unreliable === 1, "unreliable datagram not received");
  srv.send(sp, new Uint8Array([0x70, 0x6f, 0x6e, 0x67, 0, 0xff]));
  srv.send(sp, "srv-realtime", { channel: 3, reliable: false });
  let binOk = false, backUnreliable = false;
  for (let i = 0; i < 20 && !(binOk && backUnreliable); i++) {
    const r = await cli.pollAsync(1000);
    if (!r || r.type !== EventType.Message) continue;
    if (r.reliable && r.data.length === 6 && r.data[5] === 0xff) binOk = true;
    else if (!r.reliable && r.channel === 3 && r.text === "srv-realtime") backUnreliable = true;
  }
  assert(binOk, "binary reply");
  assert(backUnreliable, "server->client unreliable not received");
  const peers = srv.peerCount;

  // client leaves -> server sees DISCONNECTED
  cli.close();
  let gone = false;
  for (let i = 0; i < 10 && !gone; i++) {
    const e = await srv.pollAsync(1000);
    if (e?.type === EventType.Disconnected && e.peer === sp) gone = true;
  }
  assert(gone, "server did not see DISCONNECTED");
  console.log(`deno PASS: peerova ${version()}, ${N} reliable msgs in order, unreliable both ways, reply ok, peers=${peers}, disconnect ok`);
});
