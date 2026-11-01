// Type-level check of index.d.ts: tsc --noEmit -p test/tsconfig.json (see README)
import Peerova, { Peerova as Named, PeerovaError, MAX_UNRELIABLE_PAYLOAD, type ServerConfig } from "peerova";

async function main(): Promise<void> {
  const cfg: ServerConfig = { port: 0, upnp: false };
  const srv: Peerova = await Peerova.server(cfg);
  const t: string | null = srv.ticket;
  srv.on("message", (peer: number, data: Buffer, info) => {
    const ch: number = info.channel;
    srv.send(peer, data, { channel: ch, reliable: info.reliable });
  });
  srv.on("disconnected", (peer, reason: string) => void peer);
  const cli = await Named.client(t ?? "", { timeout_ms: 5000 }, { binary: "/usr/local/bin/peerova" });
  cli.on("connected", (peer) => cli.send(peer, new Uint8Array(MAX_UNRELIABLE_PAYLOAD), { reliable: false }));
  cli.on("connectFailed", (reason) => console.error(new PeerovaError(reason)));
  await cli.drain();
  await Promise.all([cli.close(), srv.close(500)]);
}
void main;
