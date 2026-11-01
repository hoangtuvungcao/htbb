package dev.peerova;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Peerova: direct peer-to-peer networking, end-to-end encrypted. JNI binding over the peerova C ABI
 * ({@code peerova.h}). Works on Java 8+ and Android (API 21+).
 *
 * <p>A {@link Server} starts listening and produces a <em>ticket</em> (and optionally a 6-character
 * <em>connect code</em>); a {@link Client} connects with either one. Both sides receive {@link Event}s by
 * polling and exchange messages with {@code send}.
 *
 * <pre>
 * try (Peerova.Server srv = Peerova.Server.start("{\"port\":0}");
 *      Peerova.Client cli = Peerova.Client.connect(srv.ticket())) {
 *     Peerova.Event ev = cli.poll(10000);                       // CONNECTED (or CONNECT_FAILED)
 *     cli.send(ev.peer, "hi".getBytes(UTF_8), 0, true);          // reliable, channel 0
 * }
 * </pre>
 *
 * <h2>Threading</h2>
 * Each native node has an internal lock, so {@code send}, {@code poll}, {@code disconnect} and
 * {@code peerCount} may be called from any thread. Run at most one poll loop per node. Never call
 * {@link Node#close()} while another thread is still inside {@code poll}/{@code send} on the same node:
 * stop the poll thread first, then close.
 *
 * <h2>Errors</h2>
 * Failures throw {@link PeerovaException} whose message is the native library's error text
 * ({@code peerova_last_error}). Using a closed node throws {@link IllegalStateException}.
 *
 * <h2>Native libraries</h2>
 * Two libraries are needed: {@code libpeerova_jni} (this glue) and {@code libpeerova} (the core). They are
 * loaded with {@link System#loadLibrary(String)} (so from {@code java.library.path}, or the APK on Android)
 * unless the system properties {@code peerova.jni.path} / {@code peerova.lib.path} give absolute file paths.
 */
public final class Peerova {
    /** Event type: a peer connected (server: a client joined; client: connected to the server). */
    public static final int CONNECTED = 1;
    /** Event type: a peer disconnected; {@link Event#text()} is the reason. */
    public static final int DISCONNECTED = 2;
    /** Event type: a message arrived; see {@link Event#data}, {@link Event#channel}, {@link Event#reliable}. */
    public static final int MESSAGE = 3;
    /** Event type (client only): the connection attempt failed; {@link Event#text()} is the reason. */
    public static final int CONNECT_FAILED = 4;

    /** Largest payload, in bytes, accepted by an unreliable send. Larger unreliable sends are rejected. */
    public static final int MAX_UNRELIABLE_PAYLOAD = 1180;

    private static final Charset UTF8 = Charset.forName("UTF-8");

    static {
        String lib = System.getProperty("peerova.lib.path");
        String jni = System.getProperty("peerova.jni.path");
        // Load libpeerova first: old Android linkers do not resolve dependencies from the app's lib dir.
        // (Not needed when libpeerova_jni was linked statically with libpeerova.a, see HTBB p2p/build.)
        if (lib != null) {
            try {
                System.load(lib);
            } catch (UnsatisfiedLinkError ignored) {
                // fine if libpeerova_jni is self-contained or finds libpeerova via rpath / PATH
            }
        }
        if (jni != null) {
            System.load(jni);
        } else {
            try {
                System.loadLibrary("peerova_jni");
            } catch (UnsatisfiedLinkError e) {
                // Desktop fat jar: native lib packaged as a resource /peerova-native/<os>-<arch>/<file>.
                if (!NativeLoader.loadFromResources()) throw e;
            }
        }
    }

    /** Forces the native library to load (throws UnsatisfiedLinkError if it cannot). */
    public static void ensureLoaded() {
        // class initialisation above does the work
    }

    private Peerova() {}

    // ---- native methods (implemented in peerova_jni.c) ----
    static native long nServerStart(byte[] json);
    static native long nClientConnect(byte[] ticket, byte[] json);
    static native byte[] nTicket(long h);
    static native byte[] nRoomCode(long h);
    static native int nSend(long h, long peer, int channel, byte[] data, int off, int len, boolean reliable);
    static native int nSendDirect(long h, long peer, int channel, ByteBuffer direct, int off, int len, boolean reliable);
    static native Event nPoll(long h, int timeoutMs);
    static native void nDisconnect(long h, long peer);
    static native long nPeerCount(long h);
    static native void nFree(long h);
    static native byte[] nLastError();
    static native byte[] nVersion();

    /** String to UTF-8 bytes (the C side appends the NUL); null stays null. */
    private static byte[] utf8(String s) { return s == null ? null : s.getBytes(UTF8); }
    private static String str(byte[] b) { return b == null ? null : new String(b, UTF8); }

    /** @return the native library version string. */
    public static String version() { return str(nVersion()); }

    /** Last native error text for the calling thread, never empty. */
    static String lastError() { String e = str(nLastError()); return e == null || e.isEmpty() ? "unknown peerova error" : e; }

    /** Thrown when a native call fails. The message is the native error text. */
    public static class PeerovaException extends RuntimeException {
        /** @param msg error text (usually from the native library) */
        public PeerovaException(String msg) { super(msg); }
    }

    /**
     * One network event returned by {@link Node#poll()}. All fields are plain Java values: {@link #data} is a
     * copy of the native buffer and may be kept as long as needed.
     */
    public static final class Event {
        /** {@link #CONNECTED}, {@link #DISCONNECTED}, {@link #MESSAGE} or {@link #CONNECT_FAILED}. */
        public final int type;
        /** Peer (connection) id, an unsigned 32-bit value widened to long. Pass it to send/disconnect. */
        public final long peer;
        /** Channel 0..255 the message arrived on ({@link #MESSAGE} only). */
        public final int channel;
        /** True if the message was sent reliably ({@link #MESSAGE} only). */
        public final boolean reliable;
        /** Message payload, or the UTF-8 reason for DISCONNECTED / CONNECT_FAILED. Never null. */
        public final byte[] data;

        Event(int type, long peer, int channel, boolean reliable, byte[] data) {
            this.type = type; this.peer = peer; this.channel = channel; this.reliable = reliable;
            this.data = data == null ? new byte[0] : data;
        }

        /** @return {@link #data} decoded as UTF-8 (message text or failure reason). */
        public String text() { return new String(data, UTF8); }

        @Override public String toString() {
            String[] n = {"?", "CONNECTED", "DISCONNECTED", "MESSAGE", "CONNECT_FAILED"};
            return (type >= 1 && type <= 4 ? n[type] : String.valueOf(type)) + " peer=" + peer + " ch=" + channel
                    + " reliable=" + reliable + " len=" + data.length;
        }
    }

    /**
     * Common base of {@link Server} and {@link Client}: owns one native node. Close it (try-with-resources)
     * when done; afterwards every method except {@link #isClosed()} / {@link #close()} throws
     * {@link IllegalStateException}.
     */
    public abstract static class Node implements AutoCloseable {
        private volatile long h;

        Node(long handle) {
            if (handle == 0) throw new PeerovaException(lastError());
            h = handle;
        }

        long handle() {
            long x = h;
            if (x == 0) throw new IllegalStateException("peerova node is closed");
            return x;
        }

        /**
         * Returns the next pending event without blocking.
         * @return the event, or null when none is pending
         */
        public Event poll() { return nPoll(handle(), 0); }

        /**
         * Waits up to {@code timeoutMs} for an event. Blocks the calling thread: on Android never call it
         * with a timeout on the main thread.
         * @param timeoutMs maximum wait in milliseconds ({@code <= 0} = non-blocking)
         * @return the event, or null on timeout
         */
        public Event poll(int timeoutMs) { return nPoll(handle(), timeoutMs); }

        /**
         * Sends the whole array. See {@link #send(long, byte[], int, int, int, boolean)}.
         * @param peer     peer id from a CONNECTED/MESSAGE event
         * @param data     payload
         * @param channel  0..255
         * @param reliable true = ordered and retransmitted; false = datagram, max {@link #MAX_UNRELIABLE_PAYLOAD} bytes
         * @throws PeerovaException on failure (unknown peer, oversized unreliable payload, ...)
         */
        public void send(long peer, byte[] data, int channel, boolean reliable) {
            send(peer, data, 0, data.length, channel, reliable);
        }

        /**
         * Sends {@code len} bytes of {@code data} starting at {@code off}.
         * <ul>
         * <li>{@code reliable=true}: delivered, in order per channel; may be large. Use for chat, inventory,
         *     RPCs, file chunks.</li>
         * <li>{@code reliable=false}: one datagram, lowest latency, may be lost or reordered, at most
         *     {@link #MAX_UNRELIABLE_PAYLOAD} bytes. Use for positions, inputs, voice-like realtime state.</li>
         * </ul>
         * Channels are independent: a stalled message on one channel never blocks another.
         * @throws PeerovaException on failure; message from the native library
         * @throws IndexOutOfBoundsException if off/len are outside {@code data}
         * @throws IllegalArgumentException if channel is not 0..255
         */
        public void send(long peer, byte[] data, int off, int len, int channel, boolean reliable) {
            if (!trySend(peer, data, off, len, channel, reliable)) throw new PeerovaException(lastError());
        }

        /** Sends {@code text} as UTF-8. Same rules and exceptions as {@link #send(long, byte[], int, boolean)}. */
        public void send(long peer, String text, int channel, boolean reliable) {
            send(peer, text.getBytes(UTF8), channel, reliable);
        }

        /**
         * Like {@link #send(long, byte[], int, int, int, boolean)} but reports native failures by returning
         * false instead of throwing (argument errors still throw).
         * @return true if the message was queued
         */
        public boolean trySend(long peer, byte[] data, int off, int len, int channel, boolean reliable) {
            if (off < 0 || len < 0 || off + len > data.length) throw new IndexOutOfBoundsException();
            if (channel < 0 || channel > 255) throw new IllegalArgumentException("channel must be 0..255");
            return nSend(handle(), peer, channel, data, off, len, reliable) == 0;
        }

        /**
         * Sends the bytes between {@code position} and {@code limit} of a ByteBuffer (position is not
         * changed). Direct buffers are sent without copying; heap buffers are copied first.
         * @throws PeerovaException on failure
         */
        public void send(long peer, ByteBuffer direct, int channel, boolean reliable) {
            if (!direct.isDirect()) {
                byte[] b = new byte[direct.remaining()];
                direct.duplicate().get(b);
                send(peer, b, channel, reliable);
                return;
            }
            if (channel < 0 || channel > 255) throw new IllegalArgumentException("channel must be 0..255");
            if (nSendDirect(handle(), peer, channel, direct, direct.position(), direct.remaining(), reliable) != 0)
                throw new PeerovaException(lastError());
        }

        /** Closes the connection to {@code peer}; a DISCONNECTED event follows. Unknown ids are ignored. */
        public void disconnect(long peer) { nDisconnect(handle(), peer); }

        /** @return number of currently connected peers (client: 0 or 1) */
        public long peerCount() { return nPeerCount(handle()); }

        /** @return true after {@link #close()} */
        public boolean isClosed() { return h == 0; }

        /**
         * Disconnects all peers and frees the native node. Idempotent. Must not run while another thread is
         * inside poll/send on this node.
         */
        @Override public synchronized void close() {
            long x = h;
            h = 0;
            if (x != 0) nFree(x);
        }
    }

    /** A server node: accepts clients that connect with its ticket or connect code. */
    public static final class Server extends Node {
        private final String ticket;

        private Server(long h) {
            super(h);
            String t = str(nTicket(h));
            ticket = t == null ? "" : t;
        }

        /**
         * Starts a server. Blocks until the ticket is ready (about 3 s at most), so on Android call it from a
         * background thread.
         * @param configJson JSON config or null for defaults, e.g.
         *        {@code {"port":47000,"key_file":"server.key","license":"PVA-...","token":"secret"}}.
         *        Keys: port, key_file, license, max_ccu, token, upnp, private, app, share
         *        (see the README).
         * @throws PeerovaException if the server cannot start (port in use, bad config, invalid license, ...)
         */
        public static Server start(String configJson) { return new Server(nServerStart(utf8(configJson))); }

        /**
         * @return the ticket ({@code "pvt_..."}) clients pass to {@link Client#connect(String)}. It is stable
         *         for the same {@code key_file}. Safe to share with players/users.
         */
        public String ticket() { return ticket; }

        /**
         * The 6-character connect code (e.g. {@code "K7Q2XD"}) assigned by the Peerova website, a short
         * alternative to the ticket. Only available when the server was started with {@code "app"} and
         * {@code "share"} in its config, and only after the website has registered it, which happens shortly
         * after start: poll this method until it returns non-null.
         * @return the connect code, or null if not (yet) available
         */
        public String roomCode() { return str(nRoomCode(handle())); }
    }

    /** A client node: one connection to one server. */
    public static final class Client extends Node {
        private Client(long h) { super(h); }

        /**
         * Starts connecting and returns immediately when given a ticket. With a 6-character connect code it first
         * looks the code up on the Peerova website (an HTTPS request on the calling thread, so not on the Android
         * main thread). Then wait for {@link #CONNECTED} (success) or
         * {@link #CONNECT_FAILED} (with reason) via {@link #poll(int)}. Connections are automatic: direct
         * first, with an automatic encrypted fallback route when networks block direct connections.
         * @param ticket     the server's ticket ({@code "pvt_..."}) or its 6-character connect code
         * @param configJson JSON config or null, e.g. {@code {"token":"secret","timeout_ms":20000}}
         * @throws IllegalArgumentException if ticket is null/empty
         * @throws PeerovaException if the ticket is malformed, the connect code is unknown/unreachable, or the
         *         node cannot be created
         */
        public static Client connect(String ticket, String configJson) {
            if (ticket == null || ticket.isEmpty()) throw new IllegalArgumentException("ticket is empty");
            return new Client(nClientConnect(utf8(ticket), utf8(configJson)));
        }

        /** Same as {@code connect(ticket, null)}. */
        public static Client connect(String ticket) { return connect(ticket, null); }
    }
}
