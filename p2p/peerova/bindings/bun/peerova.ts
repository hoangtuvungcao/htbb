/**
 * peerova — direct P2P networking (no relay, E2E encrypted) for Bun ≥1.0 via bun:ffi.
 *
 * ```ts
 * import { PeerovaServer, PeerovaClient, EventType } from "./peerova.ts";
 * const srv = PeerovaServer.start({ port: 0, upnp: false });
 * const cli = PeerovaClient.connect(srv.ticket);
 * const ev = cli.poll(10_000);                     // blocks the JS thread up to 10 s
 * if (ev?.type === EventType.Connected) cli.send(ev.peer, "hello");
 * setInterval(() => { for (let e; (e = srv.poll()); ) handle(e); }, 16);   // game-loop style, non-blocking
 * ```
 * bun:ffi calls are synchronous: use poll() (timeout 0) from a timer, or run poll(timeout) in a Worker.
 * Prefer zero-native-deps? The Node package (../nodejs, stdio sidecar) also runs on Bun.
 *
 * Native lib lookup: $PEEROVA_LIB → ./<lib> → ../../target/release/<lib> → system loader.
 */
import { CString, dlopen, FFIType, suffix, toArrayBuffer, type Pointer } from "bun:ffi";
import { existsSync } from "node:fs";
import { join } from "node:path";

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

const libFile = process.platform === "win32" ? "peerova.dll" : `libpeerova.${suffix}`;
function libPath(): string {
  for (const p of [process.env.PEEROVA_LIB, join(import.meta.dir, libFile), join(import.meta.dir, "..", "..", "target", "release", libFile)]) {
    if (p && existsSync(p)) return p;
  }
  return libFile;
}

const { symbols: lib } = dlopen(libPath(), {
  peerova_server_start: { args: [FFIType.ptr], returns: FFIType.ptr },
  peerova_client_connect: { args: [FFIType.ptr, FFIType.ptr], returns: FFIType.ptr },
  peerova_ticket: { args: [FFIType.ptr, FFIType.ptr, FFIType.i32], returns: FFIType.i32 },
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

export function version(): string {
  const p = lib.peerova_version();
  return p ? new CString(p).toString() : "";
}

// peerova_event layout (64-bit): i32 type@0, u32 peer@4, u8 channel@8, u8 reliable@9, ptr data@16, u32 len@24; size 32.
const EV_SIZE = 32;

abstract class Node {
  #h: Pointer | null;
  #ev = new Uint8Array(EV_SIZE);
  #view = new DataView(this.#ev.buffer);

  protected constructor(h: Pointer | null) {
    if (!h) throw new PeerovaError(lastError());
    this.#h = h;
  }

  protected get handle(): Pointer {
    if (!this.#h) throw new PeerovaError("node is closed");
    return this.#h;
  }

  /** timeoutMs = 0: non-blocking. > 0: blocks the JS thread up to timeoutMs. */
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

  /** Yields events until `idleMs` passes without one (blocking). */
  *events(idleMs = 1000): Generator<PeerovaEvent> {
    for (let ev; (ev = this.poll(idleMs)); ) yield ev;
  }

  send(peer: number, data: Uint8Array | string, { channel = 0, reliable = true }: SendOptions = {}): void {
    const bytes = typeof data === "string" ? enc.encode(data) : data;
    if (channel < 0 || channel > 255) throw new PeerovaError("channel must be 0..255");
    if (!reliable && bytes.length > MAX_UNRELIABLE_PAYLOAD) throw new PeerovaError(`unreliable max ${MAX_UNRELIABLE_PAYLOAD} bytes`);
    // ptr() of an empty array is invalid; send a 1-byte buffer with len 0 instead
    const buf = bytes.length ? bytes : new Uint8Array(1);
    if (lib.peerova_send(this.handle, peer, channel, buf, bytes.length, reliable ? 1 : 0) !== 0) {
      throw new PeerovaError(lastError());
    }
  }

  disconnect(peer: number): void {
    lib.peerova_disconnect(this.handle, peer);
  }

  get peerCount(): number {
    return lib.peerova_peer_count(this.handle);
  }

  close(): void {
    if (this.#h) {
      lib.peerova_free(this.#h);
      this.#h = null;
    }
  }

  [Symbol.dispose](): void {
    this.close();
  }
}

export class PeerovaServer extends Node {
  readonly ticket: string;

  private constructor(h: Pointer | null) {
    super(h);
    const n = lib.peerova_ticket(this.handle, null, 0);
    const buf = new Uint8Array(Math.max(n, 1));
    if (n > 0) lib.peerova_ticket(this.handle, buf, n);
    this.ticket = n > 0 ? dec.decode(buf.subarray(0, n - 1)) : "";
  }

  /** Blocks up to ~3 s until the ticket is ready. */
  static start(config: Record<string, unknown> = {}): PeerovaServer {
    return new PeerovaServer(lib.peerova_server_start(cstr(JSON.stringify(config))));
  }
}

export class PeerovaClient extends Node {
  private constructor(h: Pointer | null) {
    super(h);
  }

  /** Returns immediately; wait for EventType.Connected / ConnectFailed. */
  static connect(ticket: string, config: Record<string, unknown> = {}): PeerovaClient {
    return new PeerovaClient(lib.peerova_client_connect(cstr(ticket), cstr(JSON.stringify(config))));
  }
}
