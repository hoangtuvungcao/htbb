/// <reference types="node" />
import { EventEmitter } from "node:events";

/** Max payload of an unreliable message (bytes). */
export declare const MAX_UNRELIABLE_PAYLOAD: 1180;

export declare class PeerovaError extends Error {}

/** Server config, passed verbatim to the native SDK (see crates/peerova-ffi/include/peerova.h). */
export interface ServerConfig {
  port?: number;
  key_file?: string;
  license?: string;
  license_server?: string;
  authority_pubkey?: string;
  supernodes?: string[];
  supernode_mode?: boolean;
  private?: boolean;
  token?: string;
  max_ccu?: number;
  upnp?: boolean;
  tcp_fallback?: boolean;
  auto_supernode?: boolean;
  rendezvous?: boolean | string;
  [key: string]: unknown;
}

export interface ClientConfig {
  token?: string;
  timeout_ms?: number;
  supernodes?: string[];
  force_tcp?: boolean;
  rendezvous?: string;
  [key: string]: unknown;
}

export interface SpawnOptions {
  /** Path to the `peerova` CLI. Default: $PEEROVA_BIN, ./bin/peerova, ../../target/release/peerova, PATH. */
  binary?: string;
  /** What to do with the sidecar's stderr (default "ignore"). */
  stderr?: "ignore" | "inherit" | "pipe";
}

export interface SendOptions {
  /** 0..255, default 0 */
  channel?: number;
  /** default true. false = unreliable datagram (≤ 1180 bytes, may be lost/reordered). */
  reliable?: boolean;
}

export interface MessageInfo {
  channel: number;
  reliable: boolean;
}

export interface PeerovaEvents {
  ready: [ticket?: string];
  connected: [peer: number];
  message: [peer: number, data: Buffer, info: MessageInfo];
  disconnected: [peer: number, reason: string];
  connectFailed: [reason: string];
  error: [err: PeerovaError];
  close: [code: number | null];
}

export declare function findBinary(explicit?: string): string;

export declare class Peerova extends EventEmitter {
  /** Start a server; resolves when the ticket is ready (≤ ~3 s). */
  static server(config?: ServerConfig, options?: SpawnOptions): Promise<Peerova>;
  /** Connect to a ticket; wait for "connected" or "connectFailed". */
  static client(ticket: string, config?: ClientConfig, options?: SpawnOptions): Promise<Peerova>;

  /** Server ticket (pvt_...), null for clients. */
  readonly ticket: string | null;
  readonly peers: Set<number>;
  readonly peerCount: number;
  readonly closed: boolean;

  /** Returns false when the pipe is congested — `await drain()` before sending more. */
  send(peer: number, data: string | Uint8Array, options?: SendOptions): boolean;
  drain(): Promise<void>;
  disconnect(peer: number): void;
  close(timeoutMs?: number): Promise<void>;

  on<K extends keyof PeerovaEvents>(event: K, listener: (...args: PeerovaEvents[K]) => void): this;
  once<K extends keyof PeerovaEvents>(event: K, listener: (...args: PeerovaEvents[K]) => void): this;
  off<K extends keyof PeerovaEvents>(event: K, listener: (...args: PeerovaEvents[K]) => void): this;
  emit<K extends keyof PeerovaEvents>(event: K, ...args: PeerovaEvents[K]): boolean;
}

export default Peerova;
