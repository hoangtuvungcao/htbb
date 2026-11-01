/**
 * peerova — direct peer-to-peer networking (end-to-end encrypted) for Node.js >= 18,
 * Electron (main process) and Bun.
 *
 * Zero native dependencies: each node spawns the `peerova stdio` sidecar (the `peerova`
 * command-line tool) and exchanges JSON lines with it over stdin/stdout
 * (protocol: ../stdio/PROTOCOL.md). Everything is asynchronous and event-driven; nothing
 * here blocks the event loop.
 *
 * ```js
 * import { Peerova } from "peerova";
 * const srv = await Peerova.server({ port: 47000 });
 * srv.on("message", (peer, data, { channel, reliable }) => srv.send(peer, data));
 * const cli = await Peerova.client(srv.ticket);   // a ticket or a 6-character connect code
 * cli.on("connected", (peer) => cli.send(peer, "hello"));
 * ```
 *
 * Errors: methods throw {@link PeerovaError}; asynchronous sidecar errors are emitted as
 * "error" events.
 *
 * @module peerova
 */
import { spawn } from "node:child_process";
import { EventEmitter } from "node:events";
import { existsSync } from "node:fs";
import { dirname, join } from "node:path";
import { createInterface } from "node:readline";
import { fileURLToPath } from "node:url";

/** Largest payload accepted by `send(..., { reliable: false })`, in bytes. */
export const MAX_UNRELIABLE_PAYLOAD = 1180;

/** Error thrown (or emitted as "error") by this package. `message` is the SDK's error text. */
export class PeerovaError extends Error {
  /** @param {string} message */
  constructor(message) {
    super(message);
    this.name = "PeerovaError";
  }
}

const here = dirname(fileURLToPath(import.meta.url));
const exe = process.platform === "win32" ? "peerova.exe" : "peerova";
const platformDir = `${{ win32: "windows", darwin: "macos" }[process.platform] ?? process.platform}-${
  { x64: "x86_64", arm64: "aarch64" }[process.arch] ?? process.arch
}`;

/**
 * Locate the `peerova` command-line tool used as the sidecar.
 *
 * Order: `explicit` → `$PEEROVA_BIN` → `bin/peerova[.exe]` inside this package → next to this
 * file → the SDK's `lib/<platform>` folder → a development build → `peerova` on `PATH`.
 *
 * @param {string} [explicit] Path to use as-is (from `options.binary`).
 * @returns {string} A path or a bare command name (resolved by the OS when spawning).
 */
export function findBinary(explicit) {
  if (explicit) return explicit;
  if (process.env.PEEROVA_BIN) return process.env.PEEROVA_BIN;
  for (const p of [
    join(here, "bin", exe),
    join(here, exe),
    join(here, "..", "..", "lib", platformDir, exe),
    join(here, "..", "..", "target", "release", exe),
  ]) {
    if (existsSync(p)) return p;
  }
  return exe;
}

const toBuffer = (data) =>
  typeof data === "string" ? Buffer.from(data, "utf8") : Buffer.isBuffer(data) ? data : Buffer.from(data.buffer, data.byteOffset, data.byteLength);

/**
 * One Peerova node (server or client), backed by its own sidecar process.
 * Create it with {@link Peerova.server} or {@link Peerova.client}.
 *
 * Events:
 *  - "ready" (ticket?: string) — the sidecar is up (emitted before the factory promise resolves).
 *  - "connected" (peer: number) — server: a client joined; client: we joined the server.
 *  - "message" (peer: number, data: Buffer, info: { channel: number, reliable: boolean })
 *  - "disconnected" (peer: number, reason: string)
 *  - "connectFailed" (reason: string) — client only: the connection attempt failed (e.g. timeout).
 *  - "error" (err: PeerovaError) — non-fatal sidecar errors (e.g. send to a peer that has left).
 *     Unlike a plain EventEmitter, an unhandled "error" is NOT thrown (it is dropped).
 *  - "close" (code: number | null) — the sidecar exited; the node is unusable afterwards.
 *
 * All listeners run on the Node.js event loop; no extra threads are involved.
 */
export class Peerova extends EventEmitter {
  /** @type {string | null} Server ticket (`pvt_...`), to share with clients. `null` on clients. */
  ticket = null;
  /** @type {Set<number>} Currently connected peer ids (kept up to date from events). */
  peers = new Set();
  #proc;
  #closed = false;
  #exited;
  /** Last "error" message from the sidecar; used as the rejection text if it exits before ready. */
  #startError = null;

  /**
   * @private Use {@link Peerova.server} / {@link Peerova.client}.
   * @param {string[]} args Sidecar arguments after `stdio`.
   * @param {{ binary?: string, stderr?: "ignore" | "inherit" | "pipe" }} [options]
   */
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

  /**
   * Start a server.
   *
   * The sidecar cannot report the 6-character connect code; when you need one, read it with
   * an in-process binding or from the `peerova` command-line tool. Clients of this package can
   * still connect *with* a code (see {@link Peerova.client}).
   *
   * @param {object} [config] Server config (`port`, `key_file`, `license`, `max_ccu`, `token`,
   *   `upnp`, `private`, `app`, `share`), passed to the SDK as JSON.
   * @param {{ binary?: string, stderr?: "ignore" | "inherit" | "pipe" }} [options] Sidecar options.
   * @returns {Promise<Peerova>} Resolves once the ticket is ready (<= ~3 s).
   * @throws {PeerovaError} (rejection) the sidecar cannot be started or the server fails to start.
   */
  static server(config = {}, options = {}) {
    return new Peerova(["server", JSON.stringify(config)], options).#ready();
  }

  /**
   * Connect to a server.
   *
   * Connections are automatic: direct first, with an automatic encrypted fallback route when
   * networks block direct connections.
   *
   * @param {string} ticket The server's ticket (`pvt_...`) or its 6-character connect code.
   * @param {object} [config] Client config: `token`, `timeout_ms` (default 20000).
   * @param {{ binary?: string, stderr?: "ignore" | "inherit" | "pipe" }} [options] Sidecar options.
   * @returns {Promise<Peerova>} Resolves when the sidecar is up; then wait for "connected" or
   *   "connectFailed".
   * @throws {PeerovaError} `ticket` is empty (thrown synchronously), or (rejection) the ticket is
   *   malformed / the sidecar cannot be started.
   */
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
   * Send a message to a peer.
   *
   * @param {number} peer Peer id from a "connected" / "message" event.
   * @param {string | Uint8Array} data Payload; strings are sent as UTF-8.
   * @param {{ channel?: number, reliable?: boolean }} [options]
   *   `channel` 0..255 (default 0; channels never block each other).
   *   `reliable` (default true): ordered and retransmitted per channel, any size.
   *   `reliable: false`: lowest-latency datagram, may be lost or reordered, at most 1180 bytes.
   * @returns {boolean} `false` if the pipe to the sidecar is congested — `await node.drain()`
   *   before sending much more. The message is still queued.
   * @throws {PeerovaError} channel out of range, unreliable payload too large, or node closed.
   *   Failures detected by the SDK (e.g. the peer has left) arrive as "error" events.
   */
  send(peer, data, { channel = 0, reliable = true } = {}) {
    const buf = toBuffer(data);
    if (!Number.isInteger(channel) || channel < 0 || channel > 255) throw new PeerovaError("channel must be 0..255");
    if (!reliable && buf.length > MAX_UNRELIABLE_PAYLOAD) throw new PeerovaError(`unreliable payload max ${MAX_UNRELIABLE_PAYLOAD} bytes`);
    return this.#write({ cmd: "send", peer, channel, reliable, data: buf.toString("base64url") });
  }

  /**
   * Backpressure helper: resolves when the pipe to the sidecar has drained.
   * @returns {Promise<void>}
   */
  drain() {
    return this.#proc.stdin.writableNeedDrain ? new Promise((r) => this.#proc.stdin.once("drain", r)) : Promise.resolve();
  }

  /**
   * Close the connection to one peer (server: kick a client; client: leave the server).
   * @param {number} peer
   * @throws {PeerovaError} the node is closed.
   */
  disconnect(peer) {
    this.#write({ cmd: "disconnect", peer });
  }

  /** @returns {number} Number of currently connected peers. */
  get peerCount() {
    return this.peers.size;
  }

  /** @returns {boolean} `true` once the sidecar has exited. */
  get closed() {
    return this.#closed;
  }

  /**
   * Stop the node: disconnects all peers and ends the sidecar. Safe to call more than once.
   * @param {number} [timeoutMs=2000] Kill the sidecar if it has not exited by then.
   * @returns {Promise<void>} Resolves when the sidecar has exited.
   */
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
