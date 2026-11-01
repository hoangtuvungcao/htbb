// How to run (two terminals, from bindings/deno):
//   deno run --allow-ffi --allow-env --allow-read --allow-write examples/chat.ts server [--port 47000] [--key server.key]
//   deno run --allow-ffi --allow-env --allow-read examples/chat.ts client <ticket-or-code> [--seconds N]
//
// Tiny chat + position-sync demo. Positions are sent unreliable at ~20 Hz (channel 1), chat lines
// reliable (channel 0); the server forwards both to every other client. Events are received with
// pollAsync(), so the event loop is never blocked.
import { EventType, PeerovaClient, PeerovaError, type PeerovaEvent, PeerovaServer } from "../mod.ts";

const CH_CHAT = 0;
const CH_POS = 1;

const [mode, ...rest] = Deno.args;
const flag = (name: string, dflt: string): string => {
  const i = rest.indexOf(`--${name}`);
  return i >= 0 && i + 1 < rest.length ? rest[i + 1] : dflt;
};

// position packet: [peer u32][x f32][y f32], little-endian (12 bytes, far below 1180)
function packPos(peer: number, x: number, y: number): Uint8Array {
  const b = new Uint8Array(12);
  const v = new DataView(b.buffer);
  v.setUint32(0, peer >>> 0, true);
  v.setFloat32(4, x, true);
  v.setFloat32(8, y, true);
  return b;
}

/** Resolves on Ctrl+C (and SIGTERM where the OS supports it). */
function onSignal(): Promise<void> {
  return new Promise((resolve) => {
    Deno.addSignalListener("SIGINT", () => resolve());
    if (Deno.build.os !== "windows") Deno.addSignalListener("SIGTERM", () => resolve());
  });
}

/**
 * Polls `node` until `stop.done` is set. The short timeout lets the loop notice `stop` quickly;
 * awaiting the returned promise guarantees no pollAsync is pending, so close() is safe afterwards.
 */
async function pollLoop(node: PeerovaServer | PeerovaClient, stop: { done: boolean }, handle: (ev: PeerovaEvent) => void) {
  while (!stop.done) {
    const ev = await node.pollAsync(100);
    if (ev) handle(ev);
  }
}

async function runServer() {
  const srv = await PeerovaServer.startAsync({ port: Number(flag("port", "47000")), key_file: flag("key", "server.key") });
  console.log("TICKET", srv.ticket);
  const peers = new Set<number>();
  const stop = { done: false };

  const loop = pollLoop(srv, stop, (ev) => {
    if (ev.type === EventType.Connected) {
      peers.add(ev.peer);
      console.log(`[server] peer ${ev.peer} connected (${srv.peerCount} online)`);
    } else if (ev.type === EventType.Disconnected) {
      peers.delete(ev.peer);
      console.log(`[server] peer ${ev.peer} left: ${ev.text}`);
    } else if (ev.type === EventType.Message) {
      let out: Uint8Array | string;
      let reliable: boolean;
      if (ev.channel === CH_CHAT) {
        console.log(`[server] chat from ${ev.peer}: ${ev.text}`);
        out = `${ev.peer}: ${ev.text}`;
        reliable = true;
      } else {
        const v = new DataView(ev.data.buffer, ev.data.byteOffset, ev.data.byteLength);
        out = packPos(ev.peer, v.getFloat32(4, true), v.getFloat32(8, true));
        reliable = false;
      }
      for (const p of peers) {
        if (p === ev.peer) continue;
        try {
          srv.send(p, out, { channel: ev.channel, reliable });
        } catch (e) {
          if (!(e instanceof PeerovaError)) throw e; // peer left in the meantime
        }
      }
    }
  });

  await onSignal();
  stop.done = true;
  await loop; // no pollAsync pending any more
  srv.close();
  console.log("[server] closed");
  Deno.exit(0);
}

async function runClient(ticket: string | undefined) {
  if (!ticket) throw new PeerovaError("usage: chat.ts client <ticket-or-code>");
  const cli = PeerovaClient.connect(ticket, { timeout_ms: 10000 });
  const stop = { done: false };
  let positions = 0;
  let timer: number | undefined;
  let finished!: () => void;
  const ended = new Promise<void>((r) => (finished = r));

  const loop = pollLoop(cli, stop, (ev) => {
    switch (ev.type) {
      case EventType.Connected: {
        const server = ev.peer;
        console.log(`[client] connected (server peer ${server})`);
        const t0 = performance.now();
        timer = setInterval(() => {
          const t = (performance.now() - t0) / 1000;
          cli.send(server, packPos(0, Math.cos(t), Math.sin(t)), { channel: CH_POS, reliable: false });
        }, 50); // ~20 Hz
        void readLines((line) => cli.send(server, line, { channel: CH_CHAT, reliable: true }));
        break;
      }
      case EventType.ConnectFailed:
        console.log(`[client] connect failed: ${ev.text}`);
        finished();
        break;
      case EventType.Disconnected:
        console.log(`[client] disconnected: ${ev.text}`);
        finished();
        break;
      case EventType.Message:
        if (ev.channel === CH_CHAT) console.log(`[chat] ${ev.text}`);
        else positions++;
    }
  });

  const seconds = Number(flag("seconds", "0"));
  if (seconds > 0) setTimeout(finished, seconds * 1000);
  void onSignal().then(finished);

  await ended;
  clearInterval(timer);
  stop.done = true;
  await loop; // no pollAsync pending any more
  cli.close();
  console.log(`[client] closed (received ${positions} position updates)`);
  Deno.exit(0);
}

/** Calls `onLine` for every non-empty line typed on stdin. */
async function readLines(onLine: (line: string) => void) {
  const decoder = new TextDecoder();
  let pending = "";
  for await (const chunk of Deno.stdin.readable) {
    pending += decoder.decode(chunk, { stream: true });
    let nl: number;
    while ((nl = pending.indexOf("\n")) >= 0) {
      const line = pending.slice(0, nl).trim();
      pending = pending.slice(nl + 1);
      if (line) onLine(line);
    }
  }
}

if (mode === "server") await runServer();
else if (mode === "client") await runClient(rest[0]);
else {
  console.error("usage: chat.ts server [--port N] [--key FILE] | client <ticket-or-code> [--seconds N]");
  Deno.exit(2);
}
