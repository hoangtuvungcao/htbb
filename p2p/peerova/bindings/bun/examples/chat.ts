// How to run (two terminals, from bindings/bun):
//   bun examples/chat.ts server [--port 47000] [--key server.key]     # prints: TICKET pvt_...
//   bun examples/chat.ts client <ticket-or-code> [--seconds N]         # type lines to chat
//
// Tiny chat + position-sync demo. Positions are sent unreliable at ~20 Hz (channel 1), chat lines
// reliable (channel 0); the server forwards both to every other client. Events are drained with
// non-blocking poll() from a timer, so the JS thread never blocks.
import { EventType, PeerovaClient, PeerovaError, type PeerovaEvent, PeerovaServer } from "../peerova.ts";

const CH_CHAT = 0;
const CH_POS = 1;

const [mode, ...rest] = process.argv.slice(2);
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

/** Drains all pending events every 10 ms. Returns the timer; clear it before close(). */
function pump(node: PeerovaServer | PeerovaClient, handle: (ev: PeerovaEvent) => void) {
  return setInterval(() => {
    for (let ev; !node.closed && (ev = node.poll()); ) handle(ev);
  }, 10);
}

function onSignal(stop: () => void) {
  process.once("SIGINT", stop);
  process.once("SIGTERM", stop);
}

function runServer() {
  const srv = PeerovaServer.start({ port: Number(flag("port", "47000")), key_file: flag("key", "server.key") });
  console.log("TICKET", srv.ticket);
  const peers = new Set<number>();

  const timer = pump(srv, (ev) => {
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

  onSignal(() => {
    clearInterval(timer);
    srv.close();
    console.log("[server] closed");
    process.exit(0);
  });
}

function runClient(ticket: string | undefined) {
  if (!ticket) throw new PeerovaError("usage: chat.ts client <ticket-or-code>");
  const cli = PeerovaClient.connect(ticket, { timeout_ms: 10000 });
  let positions = 0;
  let posTimer: ReturnType<typeof setInterval> | undefined;
  let stopped = false;

  const stop = () => {
    if (stopped) return;
    stopped = true;
    clearInterval(posTimer);
    clearInterval(pollTimer);
    cli.close();
    console.log(`[client] closed (received ${positions} position updates)`);
    process.exit(0);
  };

  const pollTimer = pump(cli, (ev) => {
    switch (ev.type) {
      case EventType.Connected: {
        const server = ev.peer;
        console.log(`[client] connected (server peer ${server})`);
        const t0 = performance.now();
        posTimer = setInterval(() => {
          const t = (performance.now() - t0) / 1000;
          cli.send(server, packPos(0, Math.cos(t), Math.sin(t)), { channel: CH_POS, reliable: false });
        }, 50); // ~20 Hz
        void (async () => {
          for await (const line of console) { // Bun: async iterator over stdin lines
            if (stopped) return;
            if (line.trim()) cli.send(server, line.trim(), { channel: CH_CHAT, reliable: true });
          }
        })();
        break;
      }
      case EventType.ConnectFailed:
        console.log(`[client] connect failed: ${ev.text}`);
        stop();
        break;
      case EventType.Disconnected:
        console.log(`[client] disconnected: ${ev.text}`);
        stop();
        break;
      case EventType.Message:
        if (ev.channel === CH_CHAT) console.log(`[chat] ${ev.text}`);
        else positions++;
    }
  });

  const seconds = Number(flag("seconds", "0"));
  if (seconds > 0) setTimeout(stop, seconds * 1000);
  onSignal(stop);
}

if (mode === "server") runServer();
else if (mode === "client") runClient(rest[0]);
else {
  console.error("usage: bun examples/chat.ts server [--port N] [--key FILE] | client <ticket-or-code> [--seconds N]");
  process.exit(2);
}
