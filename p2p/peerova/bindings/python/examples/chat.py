#!/usr/bin/env python3
"""Tiny chat + position-sync demo.

How to run (two terminals, from bindings/python):
    python3 examples/chat.py server [--key server.key]            # prints TICKET pvt_...
    python3 examples/chat.py client <ticket-or-code> [--seconds N] # type lines to chat

* The server uses the plain-thread pattern: one background thread polls, the
  main thread waits for Ctrl+C.
* The client uses the asyncio pattern: ``poll(timeout)`` runs in a worker thread
  via ``loop.run_in_executor`` so the event loop is never blocked.
* Positions are sent unreliable at ~20 Hz (channel 1); chat lines are reliable
  (channel 0). The server forwards both to every other client.
"""
import argparse
import asyncio
import math
import os
import signal
import struct
import sys
import threading
import time

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
import peerova  # noqa: E402

CH_CHAT, CH_POS = 0, 1
POS = struct.Struct("<Iff")  # sender peer id, x, y  (12 bytes, well under 1180)


# ---------------------------------------------------------------- server (thread pattern)
def run_server(args) -> None:
    srv = peerova.Server(port=args.port, key_file=args.key)
    print("TICKET", srv.ticket, flush=True)
    stop = threading.Event()

    def poll_loop() -> None:
        peers = set()
        while not stop.is_set():
            ev = srv.poll(0.1)  # short timeout so `stop` is noticed quickly
            if ev is None:
                continue
            if ev.type == peerova.CONNECTED:
                peers.add(ev.peer)
                print(f"[server] peer {ev.peer} connected ({srv.peer_count} online)", flush=True)
            elif ev.type == peerova.DISCONNECTED:
                peers.discard(ev.peer)
                print(f"[server] peer {ev.peer} left: {ev.text}", flush=True)
            elif ev.type == peerova.MESSAGE:
                if ev.channel == CH_CHAT:
                    print(f"[server] chat from {ev.peer}: {ev.text}", flush=True)
                    out, reliable = f"{ev.peer}: {ev.text}".encode(), True
                else:
                    _, x, y = POS.unpack(ev.data)
                    out, reliable = POS.pack(ev.peer, x, y), False
                for p in list(peers):
                    if p != ev.peer:
                        try:
                            srv.send(p, out, channel=ev.channel, reliable=reliable)
                        except peerova.PeerovaError:
                            pass  # peer left in the meantime

    # Ctrl+C / SIGTERM only set the flag; shutdown happens below in order.
    for sig in (signal.SIGINT, signal.SIGTERM):
        signal.signal(sig, lambda *_: stop.set())
    t = threading.Thread(target=poll_loop, daemon=True)
    t.start()
    while not stop.wait(0.5):
        pass
    t.join()      # the poller must be finished before close()
    srv.close()
    print("[server] closed", flush=True)


# ---------------------------------------------------------------- client (asyncio pattern)
async def run_client(args) -> None:
    loop = asyncio.get_running_loop()
    cli = peerova.Client(args.ticket, timeout_ms=10000)
    connected = loop.create_future()
    done = asyncio.Event()
    stats = {"pos": 0}

    async def poll_loop() -> None:
        while not done.is_set():
            # Blocking wait in a worker thread; at most one poll in flight.
            ev = await loop.run_in_executor(None, cli.poll, 0.1)
            if ev is None:
                continue
            if ev.type == peerova.CONNECTED:
                print(f"[client] connected (server peer {ev.peer})", flush=True)
                connected.set_result(ev.peer)
            elif ev.type == peerova.CONNECT_FAILED:
                print(f"[client] connect failed: {ev.text}", flush=True)
                if not connected.done():
                    connected.set_exception(peerova.PeerovaError(ev.text))
                done.set()
            elif ev.type == peerova.DISCONNECTED:
                print(f"[client] disconnected: {ev.text}", flush=True)
                done.set()
            elif ev.type == peerova.MESSAGE:
                if ev.channel == CH_CHAT:
                    print(f"[chat] {ev.text}", flush=True)
                else:
                    stats["pos"] += 1

    async def send_positions(server: int) -> None:
        t0 = time.monotonic()
        while not done.is_set():
            t = time.monotonic() - t0
            cli.send(server, POS.pack(0, math.cos(t), math.sin(t)), channel=CH_POS, reliable=False)
            await asyncio.sleep(0.05)  # ~20 Hz

    async def send_chat(server: int) -> None:
        # stdin is read by a daemon thread so a blocked readline() never delays shutdown.
        lines: asyncio.Queue = asyncio.Queue()

        def reader() -> None:
            for line in sys.stdin:
                loop.call_soon_threadsafe(lines.put_nowait, line)

        threading.Thread(target=reader, daemon=True).start()
        while True:
            line = (await lines.get()).strip()
            if line:
                cli.send(server, line, channel=CH_CHAT, reliable=True)

    poller = asyncio.create_task(poll_loop())
    try:
        server = await asyncio.wait_for(connected, 15)
        tasks = [asyncio.create_task(send_positions(server)), asyncio.create_task(send_chat(server))]
        try:
            await asyncio.wait_for(done.wait(), args.seconds or None)
        except asyncio.TimeoutError:
            pass
        for t in tasks:
            t.cancel()
    finally:
        done.set()
        await poller          # wait for the in-flight poll to return before close()
        cli.close()
        print(f"[client] closed (received {stats['pos']} position updates)", flush=True)


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="mode", required=True)
    s = sub.add_parser("server")
    s.add_argument("--port", type=int, default=47000)
    s.add_argument("--key", default="server.key", help="identity file (keep it secret)")
    c = sub.add_parser("client")
    c.add_argument("ticket", help="server ticket (pvt_...) or 6-character connect code")
    c.add_argument("--seconds", type=float, default=0, help="quit after N seconds (0 = until Ctrl+C)")
    args = ap.parse_args()
    if args.mode == "server":
        run_server(args)
    else:
        try:
            asyncio.run(run_client(args))
        except KeyboardInterrupt:
            pass


if __name__ == "__main__":
    main()
