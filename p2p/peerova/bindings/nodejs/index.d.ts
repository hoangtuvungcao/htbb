/// <reference types="node" />
/**
 * peerova — direct peer-to-peer networking (end-to-end encrypted) for Node.js >= 18,
 * Electron (main process) and Bun. Each node drives a `peerova stdio` sidecar process.
 *
 * @module peerova
 */
import { EventEmitter } from "node:events";

/** Largest payload accepted by `send(..., { reliable: false })`, in bytes. */
export declare const MAX_UNRELIABLE_PAYLOAD: 1180;

/** Stable SDK error codes (same values as `PEEROVA_E_*` in peerova.h). */
export declare const ErrorCode: Readonly<{
  E_OK: 0;
  E_INVALID_ARG: 1;
  E_TOO_LARGE: 2;
  E_UNKNOWN_PEER: 3;
  E_CLOSED: 4;
  E_CODE_NOT_FOUND: 5;
  E_NETWORK: 6;
  E_TIMEOUT: 7;
  E_LICENSE: 8;
  E_CCU_LIMIT: 9;
  E_ACCESS_DENIED: 10;
  E_INVALID_TICKET: 11;
  E_IO: 12;
  E_INTERNAL: 99;
}>;

/** Name of an error code, e.g. `"E_TOO_LARGE"`. */
export type ErrorCodeName = keyof typeof ErrorCode;

/**
 * Error thrown (or emitted as "error") by this package. `message` is English text
 * `"E_NAME: description"`; `code` is the stable error code name.
 */
export declare class PeerovaError extends Error {
  constructor(message: string, code?: ErrorCodeName);
  /** Stable error code name, e.g. `"E_UNKNOWN_PEER"`. */
  readonly code: ErrorCodeName;
}

/** Settings for publishing a 6-character connect code (requires `ServerConfig.app`). */
export interface ShareConfig {
  /** Display name shown on the Peerova website. */
  name?: string;
  /** List the server publicly on the website. */
  public?: boolean;
  /** Maximum players / users advertised. */
  max?: number;
  /** Free-form mode label (e.g. a game mode). */
  mode?: string;
  /** Region label, e.g. "VN". */
  region?: string;
}

/** Server config, passed to the SDK as JSON. */
export interface ServerConfig {
  /** Listening port. Default 47000; 0 = random free port. */
  port?: number;
  /** Server identity file (default "server.key"). Same key ⇒ same ticket. Keep it secret. */
  key_file?: string;
  /** License key ("PVA-..."). Without it the server runs in dev mode (at most 3 clients). */
  license?: string;
  /** Maximum concurrent clients. */
  max_ccu?: number;
  /** Shared secret clients must present (`ClientConfig.token`). */
  token?: string;
  /** Try to open the port on the router (default true). */
  upnp?: boolean;
  /** The ticket does not contain the server's IP addresses. */
  private?: boolean;
  /** Hide the server IP: players never learn its real address (DDoS protection); slightly higher latency. */
  hide_ip?: boolean;
  /** Project id from the Peerova website; needed for a connect code. */
  app?: string;
  /** Publish a 6-character connect code for this server. */
  share?: ShareConfig;
  [key: string]: unknown;
}

/** Client config, passed to the SDK as JSON. */
export interface ClientConfig {
  /** Must match the server's `token`. */
  token?: string;
  /** Give up (→ "connectFailed") if not connected within this time. Default 20000. */
  timeout_ms?: number;
  /**
   * Client identity key file (created if missing), e.g. `<appdata>/<game>/peerova-client.key`. Keeping it keeps
   * the player's identity and stable address on the server across runs. Default: a new random key per run.
   */
  key_file?: string;
  [key: string]: unknown;
}

/** How the sidecar process is started. */
export interface SpawnOptions {
  /**
   * Path to the `peerova` command-line tool. Default: $PEEROVA_BIN, then `bin/peerova[.exe]` in
   * this package, then the SDK's `lib/<platform>` folder, then `peerova` on PATH.
   */
  binary?: string;
  /** What to do with the sidecar's stderr (default "ignore"). */
  stderr?: "ignore" | "inherit" | "pipe";
}

/** Options for {@link Peerova.send}. */
export interface SendOptions {
  /** 0..255, default 0. Channels never block each other. */
  channel?: number;
  /**
   * Default true: ordered and retransmitted per channel, any size.
   * false: lowest-latency datagram, may be lost or reordered, at most 1180 bytes.
   */
  reliable?: boolean;
}

/** Extra data delivered with a "message" event. */
export interface MessageInfo {
  /** Channel 0..255 the message arrived on. */
  channel: number;
  /** True if it was sent reliably. */
  reliable: boolean;
}

/** Event names and listener arguments of a {@link Peerova} node. */
export interface PeerovaEvents {
  /** The sidecar is up (fires before the factory promise resolves). Server: the ticket. */
  ready: [ticket?: string];
  /** Server: a client joined. Client: we joined the server. */
  connected: [peer: number];
  /** A message arrived. */
  message: [peer: number, data: Buffer, info: MessageInfo];
  /** A peer left. */
  disconnected: [peer: number, reason: string, code: ErrorCodeName];
  /** Client only: the connection attempt failed (e.g. code `"E_TIMEOUT"`). `reason` is English. */
  connectFailed: [reason: string, code: ErrorCodeName];
  /** Non-fatal sidecar error (e.g. send to a peer that has left). Not thrown when unhandled. */
  error: [err: PeerovaError];
  /** The sidecar exited; the node is unusable afterwards. */
  close: [code: number | null];
}

/** Locate the `peerova` sidecar binary (see {@link SpawnOptions.binary}). */
export declare function findBinary(explicit?: string): string;

/**
 * One Peerova node (server or client) backed by its own sidecar process.
 * Listeners run on the event loop; nothing blocks.
 */
export declare class Peerova extends EventEmitter {
  /**
   * Start a server; resolves when the ticket is ready (<= ~3 s).
   * The sidecar does not report the 6-character connect code.
   * @throws {PeerovaError} (rejection) the sidecar or the server cannot start.
   */
  static server(config?: ServerConfig, options?: SpawnOptions): Promise<Peerova>;
  /**
   * Connect to a server by ticket (`pvt_...`) or 6-character connect code. Resolves when the
   * sidecar is up; then wait for "connected" or "connectFailed".
   * @throws {PeerovaError} empty ticket (sync), or (rejection) malformed ticket / sidecar cannot start.
   */
  static client(ticket: string, config?: ClientConfig, options?: SpawnOptions): Promise<Peerova>;

  /** Server ticket (`pvt_...`) to share with clients; `null` on clients. */
  readonly ticket: string | null;
  /** Currently connected peer ids. */
  readonly peers: Set<number>;
  /** Number of currently connected peers. */
  readonly peerCount: number;
  /** True once the sidecar has exited. */
  readonly closed: boolean;

  /**
   * Send a message (strings are UTF-8). Returns false when the pipe to the sidecar is congested —
   * `await drain()` before sending much more.
   * @throws {PeerovaError} channel out of range, unreliable payload > 1180 bytes, or node closed.
   */
  send(peer: number, data: string | Uint8Array, options?: SendOptions): boolean;
  /** Resolves when the pipe to the sidecar has drained. */
  drain(): Promise<void>;
  /** Close the connection to one peer. @throws {PeerovaError} node closed. */
  disconnect(peer: number): void;
  /** Stop the node; resolves when the sidecar has exited (killed after `timeoutMs`, default 2000). */
  close(timeoutMs?: number): Promise<void>;

  on<K extends keyof PeerovaEvents>(event: K, listener: (...args: PeerovaEvents[K]) => void): this;
  once<K extends keyof PeerovaEvents>(event: K, listener: (...args: PeerovaEvents[K]) => void): this;
  off<K extends keyof PeerovaEvents>(event: K, listener: (...args: PeerovaEvents[K]) => void): this;
  emit<K extends keyof PeerovaEvents>(event: K, ...args: PeerovaEvents[K]): boolean;
}

export default Peerova;
