/**
 * peerova — direct peer-to-peer networking (end-to-end encrypted) for Bun >= 1.0, via `bun:ffi`.
 *
 * ```ts
 * import { EventType, PeerovaClient, PeerovaServer } from "./peerova.ts";
 * const srv = PeerovaServer.start({ port: 0 });
 * const cli = PeerovaClient.connect(srv.ticket);   // a ticket or a 6-character connect code
 * setInterval(() => {                              // game-loop style: never blocks
 *   for (let ev; (ev = cli.poll()); ) {
 *     if (ev.type === EventType.Connected) cli.send(ev.peer, "hello");
 *   }
 * }, 16);
 * ```
 *
 * `bun:ffi` calls are synchronous: `poll()` with timeout 0 returns immediately (call it from a timer),
 * while `poll(timeoutMs > 0)` blocks the JS thread — use that only in a Worker or a script that has
 * nothing else to do. Prefer zero native dependencies? The Node package (../nodejs, sidecar) also
 * runs on Bun.
 *
 * Threading: create and use each node in one JS thread (the main thread or one Worker); node
 * objects cannot be shared between Workers. To avoid blocking the main thread, run the node and a
 * blocking `poll(timeoutMs)` loop inside a Worker and exchange messages with `postMessage`.
 *
 * Errors: failing calls throw {@link PeerovaError}; its message is the native library's error text.
 *
 * Native library lookup: `$PEEROVA_LIB` (full path) → next to this file → the SDK's `lib/<platform>`
 * folder → a development build → the system loader search path.
 *
 * @module
 */
import { CString, dlopen, FFIType, type Pointer, suffix, toArrayBuffer } from "bun:ffi";
import { existsSync } from "node:fs";
import { join } from "node:path";

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

const libFile = process.platform === "win32" ? "peerova.dll" : `libpeerova.${suffix}`;
const platformDir = `${{ win32: "windows", darwin: "macos" }[process.platform as string] ?? process.platform}-${
  ({ x64: "x86_64", arm64: "aarch64" } as Record<string, string>)[process.arch] ?? process.arch
}`;

function libPath(): string {
  for (const p of [
    process.env.PEEROVA_LIB,
    join(import.meta.dir, libFile),
    join(import.meta.dir, "..", "..", "lib", platformDir, libFile),
    join(import.meta.dir, "..", "..", "target", "release", libFile),
  ]) {
    if (p && existsSync(p)) return p;
  }
  return libFile;
}

const { symbols: lib } = dlopen(libPath(), {
  peerova_server_start: { args: [FFIType.ptr], returns: FFIType.ptr },
  peerova_client_connect: { args: [FFIType.ptr, FFIType.ptr], returns: FFIType.ptr },
  peerova_ticket: { args: [FFIType.ptr, FFIType.ptr, FFIType.i32], returns: FFIType.i32 },
  peerova_room_code: { args: [FFIType.ptr, FFIType.ptr, FFIType.i32], returns: FFIType.i32 },
  peerova_send: { args: [FFIType.ptr, FFIType.u32, FFIType.u8, FFIType.ptr, FFIType.u32, FFIType.i32], returns: FFIType.i32 },
  peerova_poll: { args: [FFIType.ptr, FFIType.ptr], returns: FFIType.i32 },
  peerova_poll_wait: { args: [FFIType.ptr, FFIType.ptr, FFIType.u32], returns: FFIType.i32 },
  peerova_disconnect: { args: [FFIType.ptr, FFIType.u32], returns: FFIType.void },
  peerova_peer_count: { args: [FFIType.ptr], returns: FFIType.u32 },
  peerova_free: { args: [FFIType.ptr], returns: FFIType.void },
  peerova_last_error: { args: [], returns: FFIType.ptr },
  peerova_version: { args: [], returns: FFIType.ptr },
});

const enc = new TextEncoder();
const dec = new TextDecoder();
// TypedArrays are passed directly (bun:ffi pins them for the duration of the call) — never keep raw ptr() numbers around.
const cstr = (s: string) => enc.encode(s + "\0");

function lastError(): string {
  const p = lib.peerova_last_error();
  return p ? new CString(p).toString() : "unknown error";
}

/** Version string of the loaded native library. */
export function version(): string {
  const p = lib.peerova_version();
  return p ? new CString(p).toString() : "";
}

/** Call a size-then-fill native getter (peerova_ticket / peerova_room_code). Empty string = none. */
function readString(fn: typeof lib.peerova_ticket, h: Pointer): string {
  const n = fn(h, null, 0);
  if (n <= 0) return "";
  const buf = new Uint8Array(n);
  fn(h, buf, n);
  return dec.decode(buf.subarray(0, n - 1));
}

// peerova_event layout (64-bit): i32 type@0, u32 peer@4, u8 channel@8, u8 reliable@9, ptr data@16, u32 len@24; size 32.
const EV_SIZE = 32;

/** Shared implementation of {@link PeerovaServer} and {@link PeerovaClient}. */
abstract class Node {
  #h: Pointer | null;
  #ev = new Uint8Array(EV_SIZE);
  #view = new DataView(this.#ev.buffer);

  protected constructor(h: Pointer | null) {
    if (!h) throw new PeerovaError(lastError());
    this.#h = h;
  }

  /** @throws {PeerovaError} the node is closed. */
  protected get handle(): Pointer {
    if (!this.#h) throw new PeerovaError("node is closed");
    return this.#h;
  }

  /**
   * Next event, or `null` if none.
   * `timeoutMs = 0` (default): returns immediately — call it from a timer until it returns `null`.
   * `> 0`: blocks the JS thread up to `timeoutMs`.
   */
  poll(timeoutMs = 0): PeerovaEvent | null {
    const got = timeoutMs > 0 ? lib.peerova_poll_wait(this.handle, this.#ev, timeoutMs) : lib.peerova_poll(this.handle, this.#ev);
    if (!got) return null;
    const v = this.#view;
    const len = v.getUint32(24, true);
    const dataPtr = Number(v.getBigUint64(16, true)) as Pointer;
    // copy now: native data is only valid until the next poll on this node
    const data = len && dataPtr ? new Uint8Array(toArrayBuffer(dataPtr, 0, len)).slice() : new Uint8Array(0);
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

  /** Yields events until `idleMs` passes without one. Blocks the JS thread while waiting. */
  *events(idleMs = 1000): Generator<PeerovaEvent> {
    for (let ev; (ev = this.poll(idleMs)); ) yield ev;
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
    // ptr() of an empty array is invalid; send a 1-byte buffer with len 0 instead
    const buf = bytes.length ? bytes : new Uint8Array(1);
    if (lib.peerova_send(this.handle, peer, channel, buf, bytes.length, reliable ? 1 : 0) !== 0) {
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

  /** Disconnect everyone and free the node. Safe to call twice. */
  close(): void {
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

  private constructor(h: Pointer | null) {
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
}

/** A client node: connects to one server. */
export class PeerovaClient extends Node {
  private constructor(h: Pointer | null) {
    super(h);
  }

  /**
   * Connect to a server by ticket (`pvt_...`) or 6-character connect code. Returns immediately;
   * poll for {@link EventType.Connected} or {@link EventType.ConnectFailed}. Connections are
   * automatic: direct first, with an automatic encrypted fallback route when networks block
   * direct connections.
   * @throws {PeerovaError} malformed ticket or invalid config.
   */
  static connect(ticket: string, config: ClientConfig = {}): PeerovaClient {
    return new PeerovaClient(lib.peerova_client_connect(cstr(ticket), cstr(JSON.stringify(config))));
  }
}
