/**
 * peerova — direct P2P networking (no relay, E2E encrypted) for Deno ≥1.38 via Deno.dlopen.
 * Needs `--allow-ffi` (+ `--allow-env --allow-read` for library lookup).
 *
 * ```ts
 * import { PeerovaServer, PeerovaClient, EventType } from "./mod.ts";
 * using srv = PeerovaServer.start({ port: 0, upnp: false });
 * using cli = PeerovaClient.connect(srv.ticket);
 * const ev = await cli.pollAsync(10_000);           // CONNECTED (does not block the event loop)
 * if (ev?.type === EventType.Connected) cli.send(ev.peer, "hello");
 * ```
 *
 * Native lib lookup: $PEEROVA_LIB → ../../target/release/<lib> (repo checkout) → system loader.
 */

export enum EventType {
  Connected = 1,
  Disconnected = 2,
  Message = 3,
  ConnectFailed = 4,
}

export interface PeerovaEvent {
  type: EventType;
  peer: number;
  channel: number;
  reliable: boolean;
  /** Copied out of native memory — safe to keep. For Disconnected/ConnectFailed: UTF-8 reason. */
  data: Uint8Array;
  /** `data` decoded as UTF-8. */
  readonly text: string;
}

export interface SendOptions {
  channel?: number;
  reliable?: boolean;
}

export const MAX_UNRELIABLE_PAYLOAD = 1180;

export class PeerovaError extends Error {
  override name = "PeerovaError";
}

const SYMBOLS = {
  peerova_server_start: { parameters: ["buffer"], result: "pointer" },
  peerova_server_start_async: { name: "peerova_server_start", parameters: ["buffer"], result: "pointer", nonblocking: true },
  peerova_client_connect: { parameters: ["buffer", "buffer"], result: "pointer" },
  peerova_ticket: { parameters: ["pointer", "buffer", "i32"], result: "i32" },
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

function openLib() {
  const candidates: string[] = [];
  try {
    const env = Deno.env.get("PEEROVA_LIB");
    if (env) candidates.push(env);
  } catch { /* no --allow-env */ }
  if (new URL(import.meta.url).protocol === "file:") {
    candidates.push(new URL(`../../target/release/${libName()}`, import.meta.url).pathname.replace(/^\/([A-Za-z]:)/, "$1"));
    candidates.push(new URL(`./${libName()}`, import.meta.url).pathname.replace(/^\/([A-Za-z]:)/, "$1"));
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

abstract class Node implements Disposable {
  #h: Deno.PointerValue;
  #ev = new Uint8Array(EV_SIZE);
  #busy = false;

  protected constructor(h: Deno.PointerValue) {
    if (!h) throw new PeerovaError(lastError());
    this.#h = h;
  }

  protected get handle(): Deno.PointerObject {
    if (!this.#h) throw new PeerovaError("node is closed");
    return this.#h;
  }

  /** Non-blocking (timeoutMs = 0) or blocking up to timeoutMs. Blocks the JS thread — prefer pollAsync for waits. */
  poll(timeoutMs = 0): PeerovaEvent | null {
    const got = timeoutMs > 0 ? lib.peerova_poll_wait(this.handle, this.#ev, timeoutMs) : lib.peerova_poll(this.handle, this.#ev);
    return got ? decodeEvent(this.#ev) : null;
  }

  /** Waits up to timeoutMs on a native worker thread (event loop stays free). One pending call per node. */
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

  /** Yields events until `idleMs` passes without one. */
  async *events(idleMs = 1000): AsyncGenerator<PeerovaEvent> {
    for (;;) {
      const ev = await this.pollAsync(idleMs);
      if (!ev) return;
      yield ev;
    }
  }

  send(peer: number, data: Uint8Array | string, { channel = 0, reliable = true }: SendOptions = {}): void {
    const bytes = typeof data === "string" ? enc.encode(data) : data;
    if (channel < 0 || channel > 255) throw new PeerovaError("channel must be 0..255");
    if (!reliable && bytes.length > MAX_UNRELIABLE_PAYLOAD) throw new PeerovaError(`unreliable max ${MAX_UNRELIABLE_PAYLOAD} bytes`);
    if (lib.peerova_send(this.handle, peer, channel, bytes, bytes.length, reliable ? 1 : 0) !== 0) {
      throw new PeerovaError(lastError());
    }
  }

  disconnect(peer: number): void {
    lib.peerova_disconnect(this.handle, peer);
  }

  get peerCount(): number {
    return lib.peerova_peer_count(this.handle);
  }

  /** Frees the node. Do not call while a pollAsync is pending. */
  close(): void {
    if (this.#busy) throw new PeerovaError("cannot close while pollAsync is pending");
    if (this.#h) {
      lib.peerova_free(this.#h);
      this.#h = null;
    }
  }

  [Symbol.dispose](): void {
    this.close();
  }
}

function readTicket(h: Deno.PointerObject): string {
  const n = lib.peerova_ticket(h, null, 0);
  if (n <= 0) return "";
  const buf = new Uint8Array(n);
  lib.peerova_ticket(h, buf, n);
  return dec.decode(buf.subarray(0, n - 1));
}

export class PeerovaServer extends Node {
  readonly ticket: string;

  private constructor(h: Deno.PointerValue) {
    super(h);
    this.ticket = readTicket(this.handle);
  }

  /** Blocks up to ~3 s until the ticket is ready. */
  static start(config: Record<string, unknown> = {}): PeerovaServer {
    return new PeerovaServer(lib.peerova_server_start(cstr(JSON.stringify(config))));
  }

  /** Same as start() but on a native worker thread. (Error text may be lost — last_error is per thread.) */
  static async startAsync(config: Record<string, unknown> = {}): Promise<PeerovaServer> {
    const h = await lib.peerova_server_start_async(cstr(JSON.stringify(config)));
    if (!h) throw new PeerovaError("peerova_server_start failed (run start() for the error text)");
    return new PeerovaServer(h);
  }
}

export class PeerovaClient extends Node {
  private constructor(h: Deno.PointerValue) {
    super(h);
  }

  /** Returns immediately; wait for EventType.Connected / ConnectFailed. */
  static connect(ticket: string, config: Record<string, unknown> = {}): PeerovaClient {
    return new PeerovaClient(lib.peerova_client_connect(cstr(ticket), cstr(JSON.stringify(config))));
  }
}
