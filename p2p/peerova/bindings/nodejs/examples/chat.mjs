// How to run (two terminals, from bindings/nodejs):
//   node examples/chat.mjs server [--port 47000] [--key server.key]    # prints: TICKET pvt_...
//   node examples/chat.mjs client <ticket-or-code> [--seconds N]        # type lines to chat
//
// Tiny chat + position-sync demo. Positions are sent unreliable at ~20 Hz (channel 1),
// chat lines reliable (channel 0). The server forwards both to every other client.
import { createInterface } from "node:readline";
import { Peerova, PeerovaError } from "../index.js";

const CH_CHAT = 0;
const CH_POS = 1;

const [mode, ...rest] = process.argv.slice(2);
const flag = (name, dflt) => {
  const i = rest.indexOf(`--${name}`);
  return i >= 0 && i + 1 < rest.length ? rest[i + 1] : dflt;
};

// position packet: [peer u32][x f32][y f32], little-endian (12 bytes, far below 1180)
const packPos = (peer, x, y) => {
  const b = Buffer.alloc(12);
  b.writeUInt32LE(peer >>> 0, 0);
  b.writeFloatLE(x, 4);
  b.writeFloatLE(y, 8);
  return b;
};

async function runServer() {
  const srv = await Peerova.server({ port: Number(flag("port", 47000)), key_file: flag("key", "server.key") });
  console.log("TICKET", srv.ticket);

  srv.on("connected", (peer) => console.log(`[server] peer ${peer} connected (${srv.peerCount} online)`));
  srv.on("disconnected", (peer, reason) => console.log(`[server] peer ${peer} left: ${reason}`));
  srv.on("error", (err) => console.log(`[server] ${err.message}`)); // e.g. forwarded to a peer that just left
  srv.on("message", (from, data, { channel }) => {
    let out;
    let reliable;
    if (channel === CH_CHAT) {
      console.log(`[server] chat from ${from}: ${data}`);
      out = `${from}: ${data}`;
      reliable = true;
    } else {
      out = packPos(from, data.readFloatLE(4), data.readFloatLE(8));
      reliable = false;
    }
    for (const p of srv.peers) if (p !== from) srv.send(p, out, { channel, reliable });
  });

  const stop = async () => {
    await srv.close();
    console.log("[server] closed");
    process.exit(0);
  };
  process.once("SIGINT", stop);
  process.once("SIGTERM", stop);
}

async function runClient(ticket) {
  if (!ticket) throw new PeerovaError("usage: chat.mjs client <ticket-or-code>");
  const cli = await Peerova.client(ticket, { timeout_ms: 10000 });
  let positions = 0;
  let timer = null;
  let rl = null;
  let stopping = false;

  const stop = async () => {
    if (stopping) return;
    stopping = true;
    clearInterval(timer);
    rl?.close();
    await cli.close();
    console.log(`[client] closed (received ${positions} position updates)`);
    process.exit(0);
  };

  cli.on("connectFailed", (reason) => {
    console.log(`[client] connect failed: ${reason}`);
    void stop();
  });
  cli.on("disconnected", (_peer, reason) => {
    console.log(`[client] disconnected: ${reason}`);
    void stop();
  });
  cli.on("message", (_peer, data, { channel }) => {
    if (channel === CH_CHAT) console.log(`[chat] ${data}`);
    else positions++;
  });
  cli.on("connected", (server) => {
    console.log(`[client] connected (server peer ${server})`);
    const t0 = performance.now();
    timer = setInterval(() => {
      const t = (performance.now() - t0) / 1000;
      cli.send(server, packPos(0, Math.cos(t), Math.sin(t)), { channel: CH_POS, reliable: false });
    }, 50); // ~20 Hz
    rl = createInterface({ input: process.stdin });
    rl.on("line", (line) => {
      if (line.trim()) cli.send(server, line.trim(), { channel: CH_CHAT, reliable: true });
    });
  });

  const seconds = Number(flag("seconds", 0));
  if (seconds > 0) setTimeout(stop, seconds * 1000);
  process.once("SIGINT", stop);
  process.once("SIGTERM", stop);
}

if (mode === "server") await runServer();
else if (mode === "client") await runClient(rest[0]);
else {
  console.error("usage: node examples/chat.mjs server [--port N] [--key FILE] | client <ticket-or-code> [--seconds N]");
  process.exit(2);
}
