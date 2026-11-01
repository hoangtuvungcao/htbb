// How to run (from bindings/nodejs; Electron is not a dependency of the package, install it in your app):
//   npx electron examples/electron                # opens a window and shows the round-trip as it happens
//   npx electron examples/electron --selftest     # hidden window, exits with code 0 (PASS) or 1 (FAIL)
// The sidecar binary is found like in Node (PEEROVA_BIN, bin/, the SDK's lib/<platform>/, target/release).
// On a Linux machine without a display add --ozone-platform=headless (or run under xvfb-run).
//
// Pattern shown here: Peerova runs in the Electron MAIN process (it spawns the `peerova stdio` sidecar);
// the renderer only receives events over IPC through a sandboxed preload (contextIsolation, no nodeIntegration).
// Round-trip: server + client in this process, 100 reliable messages in order + unreliable both ways,
// binary reply, client close -> server sees "disconnected". Every step is forwarded to the window.
import { app, BrowserWindow } from "electron";
import { once } from "node:events";
import { mkdtempSync } from "node:fs";
import { tmpdir } from "node:os";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { Peerova } from "../../index.js";

const here = dirname(fileURLToPath(import.meta.url));
const selftest = process.argv.includes("--selftest");
const N = 100;

const withTimeout = (p, ms, what) =>
  Promise.race([p, new Promise((_, rej) => setTimeout(() => rej(new Error(`timeout: ${what}`)), ms).unref())]);

/** All nodes opened so far, closed on quit even after a failure. */
const nodes = [];

async function roundTrip(log) {
  const key = join(mkdtempSync(join(tmpdir(), "peerova-electron-")), "server.key");
  const srv = await Peerova.server({ port: 0, upnp: false, key_file: key });
  nodes.push(srv);
  log(`server started, ticket ${srv.ticket.slice(0, 12)}...`);

  const got = [];
  let unreliable = 0;
  const allIn = new Promise((resolve) => {
    srv.on("message", (peer, data, { channel, reliable }) => {
      if (reliable && channel === 1) got.push(data.toString());
      else if (!reliable && channel === 2 && data.toString() === "realtime") unreliable++;
      if (got.length === N && unreliable === 1) resolve(peer);
    });
  });

  const cli = await Peerova.client(srv.ticket, { timeout_ms: 10000 });
  nodes.push(cli);
  const [peer] = await withTimeout(once(cli, "connected"), 10000, "client connected");
  log(`client connected (server peer id ${peer})`);
  for (let i = 0; i < N; i++) cli.send(peer, `msg ${i}`, { channel: 1, reliable: true });
  cli.send(peer, "realtime", { channel: 2, reliable: false });

  const srvPeer = await withTimeout(allIn, 5000, "client -> server messages");
  for (let i = 0; i < N; i++) if (got[i] !== `msg ${i}`) throw new Error(`out of order at ${i}: ${got[i]}`);
  log(`server: ${N} reliable messages in order + 1 unreliable`);

  const binary = Buffer.from([0x70, 0x6f, 0x6e, 0x67, 0x00, 0xff]);
  let gotBinary = false;
  let gotUnreliable = false;
  const back = new Promise((resolve) => {
    cli.on("message", (_p, data, { channel, reliable }) => {
      if (reliable && channel === 0 && data.equals(binary)) gotBinary = true;
      if (!reliable && channel === 3 && data.toString() === "srv-realtime") gotUnreliable = true;
      if (gotBinary && gotUnreliable) resolve();
    });
  });
  srv.send(srvPeer, binary, { channel: 0, reliable: true });
  srv.send(srvPeer, "srv-realtime", { channel: 3, reliable: false });
  await withTimeout(back, 5000, "server -> client reply");
  log("client: binary reliable reply + unreliable datagram");

  const gone = once(srv, "disconnected");
  await cli.close();
  const [leftPeer] = await withTimeout(gone, 10000, "server sees disconnect");
  if (leftPeer !== srvPeer) throw new Error(`disconnected peer ${leftPeer} != ${srvPeer}`);
  log("client closed -> server got 'disconnected'");
  await srv.close();
}

async function main() {
  const win = new BrowserWindow({
    show: !selftest,
    width: 720,
    height: 420,
    webPreferences: { preload: join(here, "preload.cjs"), contextIsolation: true, sandbox: true },
  });
  await win.loadFile(join(here, "index.html"));
  let sent = 0;
  const log = (line) => {
    console.log(line);
    win.webContents.send("peerova:log", line);
    sent++;
  };

  let code = 0;
  try {
    await roundTrip(log);
    // The renderer must have received every line over IPC.
    const shown = await win.webContents.executeJavaScript("document.querySelectorAll('#log li').length");
    if (shown !== sent) throw new Error(`renderer shows ${shown} of ${sent} lines`);
    console.log(`electron PASS: Electron ${process.versions.electron} (node ${process.versions.node}), ` +
      `${N} reliable msgs in order, unreliable both ways, binary reply ok, disconnect ok, renderer got ${shown} IPC events`);
  } catch (e) {
    code = 1;
    console.error(`electron FAIL: ${e?.stack ?? e}`);
    log(`FAIL: ${e?.message ?? e}`);
  }
  await Promise.allSettled(nodes.map((n) => n.close()));
  if (selftest) app.exit(code);
}

app.on("window-all-closed", () => app.quit());
// Close every node (stops its sidecar) when the app quits.
app.on("before-quit", () => nodes.forEach((n) => n.close()));
app.whenReady().then(main);
