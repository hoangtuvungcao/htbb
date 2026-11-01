package dev.peerova;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * peerova — direct P2P networking (no relay, E2E encrypted). JNI binding over the C ABI (peerova.h).
 * Java 8+ and Android compatible.
 *
 * <pre>
 * try (Peerova.Server srv = Peerova.Server.start("{\"port\":0}");
 *      Peerova.Client cli = Peerova.Client.connect(srv.ticket())) {
 *     Peerova.Event ev = cli.poll(10000);           // CONNECTED
 *     cli.send(ev.peer, "hi".getBytes(UTF_8), 0, true);
 * }
 * </pre>
 *
 * Threading: each native node has an internal mutex, so send/poll may be called from any thread.
 * Do not call close() while another thread is inside poll()/send() on the same node.
 *
 * Native libs: libpeerova_jni (this glue) + libpeerova. Loaded with System.loadLibrary unless the system
 * properties {@code peerova.jni.path} / {@code peerova.lib.path} give absolute paths.
 */
public final class Peerova {
    public static final int CONNECTED = 1, DISCONNECTED = 2, MESSAGE = 3, CONNECT_FAILED = 4;
    /** Max payload for unreliable sends. */
    public static final int MAX_UNRELIABLE_PAYLOAD = 1180;

    private static final Charset UTF8 = Charset.forName("UTF-8");

    static {
        String lib = System.getProperty("peerova.lib.path");
        String jni = System.getProperty("peerova.jni.path");
        // Load libpeerova first: old Android linkers don't resolve dependencies from the app's lib dir.
        // (Not needed when libpeerova_jni was linked statically with libpeerova.a, see p2p/build of HTBB.)
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

    // ---- native ----
    static native long nServerStart(byte[] json);
    static native long nClientConnect(byte[] ticket, byte[] json);
    static native byte[] nTicket(long h);
    static native int nSend(long h, long peer, int channel, byte[] data, int off, int len, boolean reliable);
    static native int nSendDirect(long h, long peer, int channel, ByteBuffer direct, int off, int len, boolean reliable);
    static native Event nPoll(long h, int timeoutMs);
    static native void nDisconnect(long h, long peer);
    static native long nPeerCount(long h);
    static native void nFree(long h);
    static native byte[] nLastError();
    static native byte[] nVersion();

    private static byte[] z(String s) { return s == null ? null : s.getBytes(UTF8); } // C side adds the NUL
    private static String str(byte[] b) { return b == null ? null : new String(b, UTF8); }

    public static String version() { return str(nVersion()); }

    static String lastError() { String e = str(nLastError()); return e == null || e.isEmpty() ? "unknown peerova error" : e; }

    public static class PeerovaException extends RuntimeException {
        public PeerovaException(String msg) { super(msg); }
    }

    /** A network event. {@code data} is a Java copy, safe to keep. */
    public static final class Event {
        public final int type;
        /** Peer id (unsigned 32-bit, widened to long). */
        public final long peer;
        public final int channel;
        public final boolean reliable;
        public final byte[] data;

        Event(int type, long peer, int channel, boolean reliable, byte[] data) {
            this.type = type; this.peer = peer; this.channel = channel; this.reliable = reliable;
            this.data = data == null ? new byte[0] : data;
        }

        public String text() { return new String(data, UTF8); }

        @Override public String toString() {
            String[] n = {"?", "CONNECTED", "DISCONNECTED", "MESSAGE", "CONNECT_FAILED"};
            return (type >= 1 && type <= 4 ? n[type] : String.valueOf(type)) + " peer=" + peer + " ch=" + channel
                    + " reliable=" + reliable + " len=" + data.length;
        }
    }

    /** Common base of Server and Client. */
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

        /** Non-blocking poll; null when no event is pending. */
        public Event poll() { return nPoll(handle(), 0); }

        /** Waits up to timeoutMs for an event; null on timeout. */
        public Event poll(int timeoutMs) { return nPoll(handle(), timeoutMs); }

        /** reliable=true: ordered + retransmitted per channel. reliable=false: datagram, max 1180 bytes. */
        public void send(long peer, byte[] data, int channel, boolean reliable) {
            send(peer, data, 0, data.length, channel, reliable);
        }

        public void send(long peer, byte[] data, int off, int len, int channel, boolean reliable) {
            if (!trySend(peer, data, off, len, channel, reliable)) throw new PeerovaException(lastError());
        }

        public void send(long peer, String text, int channel, boolean reliable) {
            send(peer, text.getBytes(UTF8), channel, reliable);
        }

        /** Like send but returns false instead of throwing. */
        public boolean trySend(long peer, byte[] data, int off, int len, int channel, boolean reliable) {
            if (off < 0 || len < 0 || off + len > data.length) throw new IndexOutOfBoundsException();
            if (channel < 0 || channel > 255) throw new IllegalArgumentException("channel must be 0..255");
            return nSend(handle(), peer, channel, data, off, len, reliable) == 0;
        }

        /** Zero-copy send from a direct ByteBuffer (position..limit). */
        public void send(long peer, ByteBuffer direct, int channel, boolean reliable) {
            if (!direct.isDirect()) {
                byte[] b = new byte[direct.remaining()];
                direct.duplicate().get(b);
                send(peer, b, channel, reliable);
                return;
            }
            if (nSendDirect(handle(), peer, channel, direct, direct.position(), direct.remaining(), reliable) != 0)
                throw new PeerovaException(lastError());
        }

        public void disconnect(long peer) { nDisconnect(handle(), peer); }

        public long peerCount() { return nPeerCount(handle()); }

        public boolean isClosed() { return h == 0; }

        @Override public synchronized void close() {
            long x = h;
            h = 0;
            if (x != 0) nFree(x);
        }
    }

    public static final class Server extends Node {
        private final String ticket;

        private Server(long h) {
            super(h);
            String t = str(nTicket(h));
            ticket = t == null ? "" : t;
        }

        /** Starts a server; blocks until the ticket is ready (about 3 s max). configJson may be null. */
        public static Server start(String configJson) { return new Server(nServerStart(z(configJson))); }

        public String ticket() { return ticket; }
    }

    public static final class Client extends Node {
        private Client(long h) { super(h); }

        /** Returns immediately; wait for CONNECTED / CONNECT_FAILED via poll. configJson may be null. */
        public static Client connect(String ticket, String configJson) {
            if (ticket == null || ticket.isEmpty()) throw new IllegalArgumentException("ticket is empty");
            return new Client(nClientConnect(z(ticket), z(configJson)));
        }

        public static Client connect(String ticket) { return connect(ticket, null); }
    }
}
