// peerova — direct P2P networking for Node.js ≥18 / Electron (main process) / Bun.
// Zero native deps: drives the `peerova stdio` sidecar (JSON lines over stdin/stdout).
// Protocol: ../stdio/PROTOCOL.md
//
//   import { Peerova } from "peerova";
//   const srv = await Peerova.server({ port: 47000 });
//   srv.on("message", (peer, data, { channel, reliable }) => srv.send(peer, data));
//   const cli = await Peerova.client(srv.ticket);
//   cli.on("connected", (peer) => cli.send(peer, "hello"));
import { spawn } from "node:child_process";
import { EventEmitter } from "node:events";
import { existsSync } from "node:fs";
import { dirname, join } from "node:path";
import { createInterface } from "node:readline";
import { fileURLToPath } from "node:url";

/** Max payload of an unreliable message (bytes). */
export const MAX_UNRELIABLE_PAYLOAD = 1180;

export class PeerovaError extends Error {
  constructor(message) {
    super(message);
    this.name = "PeerovaError";
  }
}

const here = dirname(fileURLToPath(import.meta.url));
const exe = process.platform === "win32" ? "peerova.exe" : "peerova";

/**
 * Locate the `peerova` CLI: options.binary → $PEEROVA_BIN → next to this file (bin/) →
 * repo build (../../target/release) → "peerova" on PATH.
 */
export function findBinary(explicit) {
  if (explicit) return explicit;
  if (process.env.PEEROVA_BIN) return process.env.PEEROVA_BIN;
  for (const p of [join(here, "bin", exe), join(here, exe), join(here, "..", "..", "target", "release", exe)]) {
    if (existsSync(p)) return p;
  }
  return exe;
}

const toBuffer = (data) =>
  typeof data === "string" ? Buffer.from(data, "utf8") : Buffer.isBuffer(data) ? data : Buffer.from(data.buffer, data.byteOffset, data.byteLength);

/**
 * One peerova node (server or client) backed by a sidecar process.
 *
 * Events:
 *  - "ready" (ticket?: string)
 *  - "connected" (peer: number)
 *  - "message" (peer: number, data: Buffer, info: { channel: number, reliable: boolean })
 *  - "disconnected" (peer: number, reason: string)
 *  - "connectFailed" (reason: string)
 *  - "error" (err: PeerovaError) — non-fatal sidecar errors (e.g. send to a gone peer).
 *     Unlike plain EventEmitter, an unhandled "error" is NOT thrown (it is dropped).
 *  - "close" (code: number | null)
 */
export class Peerova extends EventEmitter {
  /** Server ticket (pvt_...) — null for clients. */
  ticket = null;
  /** Currently connected peer ids. */
  peers = new Set();
  #proc;
  #closed = false;
  #exited;

  /** @private use Peerova.server / Peerova.client */
  constructor(args, options = {}) {
    super();
    this.#proc = spawn(findBinary(options.binary), ["stdio", ...args], {
      stdio: ["pipe", "pipe", options.stderr ?? "ignore"],
      windowsHide: true,
    });
    this.#exited = new Promise((resolve) => this.#proc.once("close", resolve));
    this.#proc.stdin.on("error", () => {}); // EPIPE if the sidecar died; surfaced via "close"
    const rl = createInterface({ input: this.#proc.stdout, crlfDelay: Infinity });
    rl.on("line", (line) => this.#onLine(line));
    this.#proc.once("close", (code) => {
      this.#closed = true;
      this.peers.clear();
      this.emit("close", code);
    });
  }

  /** Start a server. Resolves once the ticket is ready (≤ ~3 s). */
  static server(config = {}, options = {}) {
    return new Peerova(["server", JSON.stringify(config)], options).#ready();
  }

  /** Connect to a server ticket. Resolves when the sidecar is up; wait for "connected" / "connectFailed". */
  static client(ticket, config = {}, options = {}) {
    if (typeof ticket !== "string" || !ticket) throw new PeerovaError("ticket is required");
    return new Peerova(["client", ticket, JSON.stringify(config)], options).#ready();
  }

  #ready() {
    return new Promise((resolve, reject) => {
      const onReady = () => {
        cleanup();
        resolve(this);
      };
      const onClose = (code) => {
        cleanup();
        reject(new PeerovaError(this.#startError ?? `peerova sidecar exited (code ${code}) before ready`));
      };
      const onSpawnError = (e) => {
        cleanup();
        reject(new PeerovaError(`cannot start peerova sidecar: ${e.message} (set PEEROVA_BIN)`));
      };
      const cleanup = () => {
        this.off("ready", onReady);
        this.off("close", onClose);
        this.#proc.off("error", onSpawnError);
      };
      this.once("ready", onReady);
      this.once("close", onClose);
      this.#proc.once("error", onSpawnError);
    });
  }

  #startError = null;

  #onLine(line) {
    let m;
    try {
      m = JSON.parse(line);
    } catch {
      return;
    }
    switch (m.event) {
      case "ready":
        this.ticket = m.ticket ?? null;
        this.emit("ready", this.ticket ?? undefined);
        break;
      case "connected":
        this.peers.add(m.peer);
        this.emit("connected", m.peer);
        break;
      case "message":
        this.emit("message", m.peer, Buffer.from(m.data ?? "", "base64url"), { channel: m.channel, reliable: !!m.reliable });
        break;
      case "disconnected":
        this.peers.delete(m.peer);
        this.emit("disconnected", m.peer, m.reason ?? "");
        break;
      case "connect_failed":
        this.emit("connectFailed", m.reason ?? "");
        break;
      case "error":
        this.#startError = m.message;
        if (this.listenerCount("error") > 0) this.emit("error", new PeerovaError(m.message));
        break;
    }
  }

  #write(obj) {
    if (this.#closed) throw new PeerovaError("node is closed");
    // Writes are queued in order by the stream; returns false when the pipe buffer is full.
    return this.#proc.stdin.write(JSON.stringify(obj) + "\n");
  }

  /**
   * Send bytes (Buffer/Uint8Array) or a UTF-8 string to a peer.
   * reliable (default true): ordered + retransmitted per channel. reliable=false: datagram ≤ 1180 bytes.
   * Returns false if the pipe is congested (await `node.drain()` before sending lots more).
   */
  send(peer, data, { channel = 0, reliable = true } = {}) {
    const buf = toBuffer(data);
    if (!Number.isInteger(channel) || channel < 0 || channel > 255) throw new PeerovaError("channel must be 0..255");
    if (!reliable && buf.length > MAX_UNRELIABLE_PAYLOAD) throw new PeerovaError(`unreliable payload max ${MAX_UNRELIABLE_PAYLOAD} bytes`);
    return this.#write({ cmd: "send", peer, channel, reliable, data: buf.toString("base64url") });
  }

  /** Resolves when the sidecar's stdin pipe has drained (backpressure helper). */
  drain() {
    return this.#proc.stdin.writableNeedDrain ? new Promise((r) => this.#proc.stdin.once("drain", r)) : Promise.resolve();
  }

  disconnect(peer) {
    this.#write({ cmd: "disconnect", peer });
  }

  get peerCount() {
    return this.peers.size;
  }

  get closed() {
    return this.#closed;
  }

  /** Stop the node. Resolves when the sidecar has exited (kills it after `timeoutMs`). */
  async close(timeoutMs = 2000) {
    if (!this.#closed) {
      try {
        this.#proc.stdin.end(JSON.stringify({ cmd: "quit" }) + "\n");
      } catch {}
      const t = setTimeout(() => this.#proc.kill(), timeoutMs);
      await this.#exited;
      clearTimeout(t);
    }
  }
}

// `await using node = await Peerova.server(...)` where explicit resource management exists.
if (typeof Symbol.asyncDispose === "symbol") {
  Peerova.prototype[Symbol.asyncDispose] = function () {
    return this.close();
  };
}

export default Peerova;
