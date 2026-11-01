/**
 * peerova — direct peer-to-peer networking (end-to-end encrypted) for Deno >= 1.38, via `Deno.dlopen`.
 *
 * Permissions: `--allow-ffi`, plus `--allow-env --allow-read` for the library lookup.
 *
 * ```ts
 * import { EventType, PeerovaClient, PeerovaServer } from "./mod.ts";
 * using srv = PeerovaServer.start({ port: 0 });
 * using cli = PeerovaClient.connect(srv.ticket);    // a ticket or a 6-character connect code
 * const ev = await cli.pollAsync(10_000);           // CONNECTED (does not block the event loop)
 * if (ev?.type === EventType.Connected) cli.send(ev.peer, "hello");
 * ```
 *
 * Threading: a node is internally synchronised. `send` / `disconnect` / `peerCount` may be called
 * while a `pollAsync` is pending (it waits on a native worker thread). Only one `pollAsync` may be
 * pending per node, and `close()` must not be called while one is pending.
 *
 * Errors: failing calls throw {@link PeerovaError}; its message is the native library's error text.
 *
 * Native library lookup: `$PEEROVA_LIB` (full path) → next to this file → the SDK's `lib/<platform>`
 * folder → a development build → the system loader search path.
 *
 * @module
 */

/** Event types returned by {@link PeerovaServer.poll} / {@link PeerovaClient.poll}. */
export enum EventType {
  /** A peer connected (server: a new client; client: the server accepted us). */
  Connected = 1,
  /** A peer left. `data` holds the UTF-8 reason. */
  Disconnected = 2,
  /** A message arrived. `data`, `channel` and `reliable` are set. */
  Message = 3,
  /** Client only: the connection attempt failed. `data` holds the UTF-8 reason. */
  ConnectFailed = 4,
}

/** One event. */
export interface PeerovaEvent {
  /** What happened. */
  type: EventType;
  /** Peer id (connection id). On a client this is the server's id. */
  peer: number;
  /** Channel 0..255 (Message only). */
  channel: number;
  /** True if the message was sent reliably (Message only). */
  reliable: boolean;
  /** Copied out of native memory — safe to keep. For Disconnected/ConnectFailed: UTF-8 reason. */
  data: Uint8Array;
  /** `data` decoded as UTF-8. */
  readonly text: string;
}

/** Options for {@link PeerovaServer.send} / {@link PeerovaClient.send}. */
export interface SendOptions {
  /** 0..255, default 0. Channels never block each other. */
  channel?: number;
  /**
   * Default true: ordered and retransmitted per channel, any size.
   * false: lowest-latency datagram, may be lost or reordered, at most 1180 bytes.
   */
  reliable?: boolean;
}

/** Settings for publishing a 6-character connect code (requires `ServerConfig.app`). */
export interface ShareConfig {
  name?: string;
  public?: boolean;
  max?: number;
  mode?: string;
  region?: string;
}

/** Server config, passed to the native library as JSON. */
export interface ServerConfig {
  /** Listening port. Default 47000; 0 = random free port. */
  port?: number;
  /** Server identity file (default "server.key"). Same key ⇒ same ticket. Keep it secret. */
  key_file?: string;
  /** License key ("PVA-..."). Without it: dev mode, at most 3 clients. */
  license?: string;
  /** Maximum concurrent clients. */
  max_ccu?: number;
  /** Shared secret clients must present. */
  token?: string;
  /** Try to open the port on the router (default true). */
  upnp?: boolean;
  /** The ticket does not contain the server's IP addresses. */
  private?: boolean;
  /** Project id from the Peerova website; needed for a connect code. */
  app?: string;
  /** Publish a 6-character connect code. */
  share?: ShareConfig;
  [key: string]: unknown;
}

/** Client config, passed to the native library as JSON. */
export interface ClientConfig {
  /** Must match the server's `token`. */
  token?: string;
  /** Give up (→ ConnectFailed) if not connected within this time. Default 20000. */
  timeout_ms?: number;
  [key: string]: unknown;
}

/** Largest payload accepted by `send(..., { reliable: false })`, in bytes. */
export const MAX_UNRELIABLE_PAYLOAD = 1180;

/** Error thrown by this module. `message` is the native library's error text. */
export class PeerovaError extends Error {
  override name = "PeerovaError";
}

const SYMBOLS = {
  peerova_server_start: { parameters: ["buffer"], result: "pointer" },
  peerova_server_start_async: { name: "peerova_server_start", parameters: ["buffer"], result: "pointer", nonblocking: true },
  peerova_client_connect: { parameters: ["buffer", "buffer"], result: "pointer" },
  peerova_ticket: { parameters: ["pointer", "buffer", "i32"], result: "i32" },
  peerova_room_code: { parameters: ["pointer", "buffer", "i32"], result: "i32" },
  peerova_send: { parameters: ["pointer", "u32", "u8", "buffer", "u32", "i32"], result: "i32" },
  peerova_poll: { parameters: ["pointer", "buffer"], result: "i32" },
  peerova_poll_wait: { parameters: ["pointer", "buffer", "u32"], result: "i32" },
  peerova_poll_wait_async: { name: "peerova_poll_wait", parameters: ["pointer", "buffer", "u32"], result: "i32", nonblocking: true },
  peerova_disconnect: { parameters: ["pointer", "u32"], result: "void" },
  peerova_peer_count: { parameters: ["pointer"], result: "u32" },
  peerova_free: { parameters: ["pointer"], result: "void" },
  peerova_last_error: { parameters: [], result: "pointer" },
  peerova_version: { parameters: [], result: "pointer" },
} as const;

function libName(): string {
  switch (Deno.build.os) {
    case "windows":
      return "peerova.dll";
    case "darwin":
      return "libpeerova.dylib";
    default:
      return "libpeerova.so";
  }
}

function platformDir(): string {
  const os = Deno.build.os === "darwin" ? "macos" : Deno.build.os;
  return `${os}-${Deno.build.arch}`; // e.g. linux-x86_64, windows-x86_64
}

function openLib() {
  const candidates: string[] = [];
  try {
    const env = Deno.env.get("PEEROVA_LIB");
    if (env) candidates.push(env);
  } catch { /* no --allow-env */ }
  if (new URL(import.meta.url).protocol === "file:") {
    const local = (rel: string) => new URL(rel, import.meta.url).pathname.replace(/^\/([A-Za-z]:)/, "$1");
    candidates.push(local(`./${libName()}`));
    candidates.push(local(`../../lib/${platformDir()}/${libName()}`));
    candidates.push(local(`../../target/release/${libName()}`));
  }
  for (const p of candidates) {
    try {
      Deno.statSync(p);
    } catch {
      continue;
    }
    return Deno.dlopen(p, SYMBOLS);
  }
  return Deno.dlopen(libName(), SYMBOLS); // system search path
}

const lib = openLib().symbols;
const enc = new TextEncoder();
const dec = new TextDecoder();
const cstr = (s: string) => enc.encode(s + "\0");

function lastError(): string {
  const p = lib.peerova_last_error();
  return p ? Deno.UnsafePointerView.getCString(p) : "unknown error";
}

/** Version string of the loaded native library. */
export function version(): string {
  const p = lib.peerova_version();
  return p ? Deno.UnsafePointerView.getCString(p) : "";
}

// peerova_event layout (64-bit): i32 type@0, u32 peer@4, u8 channel@8, u8 reliable@9, ptr data@16, u32 len@24; size 32.
const EV_SIZE = 32;

function decodeEvent(buf: Uint8Array): PeerovaEvent {
  const v = new DataView(buf.buffer, buf.byteOffset, buf.byteLength);
  const len = v.getUint32(24, true);
  const ptr = Deno.UnsafePointer.create(v.getBigUint64(16, true));
  const data = new Uint8Array(len);
  if (len && ptr) Deno.UnsafePointerView.copyInto(ptr, data); // copy: native data is only valid until the next poll
  return {
    type: v.getInt32(0, true) as EventType,
    peer: v.getUint32(4, true),
    channel: v.getUint8(8),
    reliable: v.getUint8(9) !== 0,
    data,
    get text() {
      return dec.decode(this.data);
    },
  };
}

/** Call a size-then-fill native getter (peerova_ticket / peerova_room_code). Empty string = none. */
function readString(fn: (h: Deno.PointerObject, buf: Uint8Array | null, len: number) => number, h: Deno.PointerObject): string {
  const n = fn(h, null, 0);
  if (n <= 0) return "";
  const buf = new Uint8Array(n);
  fn(h, buf, n);
  return dec.decode(buf.subarray(0, n - 1));
}

/** Shared implementation of {@link PeerovaServer} and {@link PeerovaClient}. */
abstract class Node implements Disposable {
  #h: Deno.PointerValue;
  #ev = new Uint8Array(EV_SIZE);
  #busy = false;

  protected constructor(h: Deno.PointerValue) {
    if (!h) throw new PeerovaError(lastError());
    this.#h = h;
  }

  /** @throws {PeerovaError} the node is closed. */
  protected get handle(): Deno.PointerObject {
    if (!this.#h) throw new PeerovaError("node is closed");
    return this.#h;
  }

  /**
   * Next event, or `null` if none.
   * `timeoutMs = 0` (default) returns immediately; `> 0` blocks the JS thread up to that long —
   * prefer {@link pollAsync} for waiting. Call it from a timer to drain events game-loop style.
   */
  poll(timeoutMs = 0): PeerovaEvent | null {
    const got = timeoutMs > 0 ? lib.peerova_poll_wait(this.handle, this.#ev, timeoutMs) : lib.peerova_poll(this.handle, this.#ev);
    return got ? decodeEvent(this.#ev) : null;
  }

  /**
   * Waits up to `timeoutMs` for the next event on a native worker thread; the event loop stays free.
   * Resolves to `null` on timeout.
   * @throws {PeerovaError} another `pollAsync` is already pending on this node, or the node is closed.
   */
  async pollAsync(timeoutMs: number): Promise<PeerovaEvent | null> {
    if (this.#busy) throw new PeerovaError("pollAsync already pending on this node");
    this.#busy = true;
    const buf = new Uint8Array(EV_SIZE);
    try {
      const got = await lib.peerova_poll_wait_async(this.handle, buf, timeoutMs);
      return got ? decodeEvent(buf) : null;
    } finally {
      this.#busy = false;
    }
  }

  /** Yields events (via {@link pollAsync}) until `idleMs` passes without one. */
  async *events(idleMs = 1000): AsyncGenerator<PeerovaEvent> {
    for (;;) {
      const ev = await this.pollAsync(idleMs);
      if (!ev) return;
      yield ev;
    }
  }

  /**
   * Send a message to a peer. Strings are sent as UTF-8.
   * @throws {PeerovaError} channel outside 0..255, unreliable payload over 1180 bytes, peer gone,
   *   or node closed.
   */
  send(peer: number, data: Uint8Array | string, { channel = 0, reliable = true }: SendOptions = {}): void {
    const bytes = typeof data === "string" ? enc.encode(data) : data;
    if (!Number.isInteger(channel) || channel < 0 || channel > 255) throw new PeerovaError("channel must be 0..255");
    if (!reliable && bytes.length > MAX_UNRELIABLE_PAYLOAD) throw new PeerovaError(`unreliable max ${MAX_UNRELIABLE_PAYLOAD} bytes`);
    if (lib.peerova_send(this.handle, peer, channel, bytes, bytes.length, reliable ? 1 : 0) !== 0) {
      throw new PeerovaError(lastError());
    }
  }

  /** Close the connection to one peer. Unknown ids are ignored. */
  disconnect(peer: number): void {
    lib.peerova_disconnect(this.handle, peer);
  }

  /** Number of currently connected peers. */
  get peerCount(): number {
    return lib.peerova_peer_count(this.handle);
  }

  /** True after {@link close}. */
  get closed(): boolean {
    return !this.#h;
  }

  /**
   * Disconnect everyone and free the node. Safe to call twice.
   * @throws {PeerovaError} a `pollAsync` is still pending (await it first).
   */
  close(): void {
    if (this.#busy) throw new PeerovaError("cannot close while pollAsync is pending");
    if (this.#h) {
      lib.peerova_free(this.#h);
      this.#h = null;
    }
  }

  /** `using` support: calls {@link close}. */
  [Symbol.dispose](): void {
    this.close();
  }
}

/** A server node: accepts client connections. */
export class PeerovaServer extends Node {
  /** The server ticket (`pvt_...`). Give it to clients. */
  readonly ticket: string;

  private constructor(h: Deno.PointerValue) {
    super(h);
    this.ticket = readString(lib.peerova_ticket, this.handle);
  }

  /**
   * The 6-character connect code (e.g. "K7Q2XD"), or `null`.
   * Only available when started with `app` + `share`, and only once the website has assigned it
   * (shortly after start) — read it again later if it is `null`. Clients may pass it instead of the ticket.
   */
  get roomCode(): string | null {
    return readString(lib.peerova_room_code, this.handle) || null;
  }

  /**
   * Start a server. Blocks the JS thread until the ticket is ready (<= ~3 s).
   * @throws {PeerovaError} the server could not start (port in use, invalid config, ...).
   */
  static start(config: ServerConfig = {}): PeerovaServer {
    return new PeerovaServer(lib.peerova_server_start(cstr(JSON.stringify(config))));
  }

  /**
   * Same as {@link start} but waits on a native worker thread (the event loop stays free).
   * @throws {PeerovaError} on failure. The detailed error text is not available here (it is kept per
   *   thread); call {@link start} to see it.
   */
  static async startAsync(config: ServerConfig = {}): Promise<PeerovaServer> {
    const h = await lib.peerova_server_start_async(cstr(JSON.stringify(config)));
    if (!h) throw new PeerovaError("peerova_server_start failed (run start() for the error text)");
    return new PeerovaServer(h);
  }
}

/** A client node: connects to one server. */
export class PeerovaClient extends Node {
  private constructor(h: Deno.PointerValue) {
    super(h);
  }

  /**
   * Connect to a server by ticket (`pvt_...`) or 6-character connect code. Returns immediately;
   * wait for {@link EventType.Connected} or {@link EventType.ConnectFailed}. Connections are
   * automatic: direct first, with an automatic encrypted fallback route when networks block
   * direct connections.
   * @throws {PeerovaError} malformed ticket or invalid config.
   */
  static connect(ticket: string, config: ClientConfig = {}): PeerovaClient {
    return new PeerovaClient(lib.peerova_client_connect(cstr(ticket), cstr(JSON.stringify(config))));
  }
}
