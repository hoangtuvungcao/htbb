package dev.peerova;

import java.nio.charset.Charset;

/**
 * In-process port tunnel (the CLI's {@code server --expose} / {@code client --forward}) for apps that
 * already speak TCP: no protocol rewrite, no sidecar process.
 *
 * <pre>
 * // server: expose the local game port
 * PeerovaTunnel srv = PeerovaTunnel.server("{\"port\":47000,\"key_file\":\"p2p/server.key\",\"expose\":[\"tcp:19129\"]}");
 * String ticket = srv.ticket();
 * // client: 127.0.0.1:&lt;free port&gt; -> server port 19129
 * PeerovaTunnel cli = PeerovaTunnel.client(ticket, "{\"forward\":[\"tcp:0=19129\"]}");
 * if (cli.await(20000) == PeerovaTunnel.UP) new java.net.Socket("127.0.0.1", cli.localPort(0));
 * </pre>
 */
public final class PeerovaTunnel implements AutoCloseable {
    public static final int CONNECTING = 0, UP = 1, DOWN = 2;
    private static final Charset UTF8 = Charset.forName("UTF-8");

    static {
        Peerova.version(); // loads the native library (dev.peerova.NativeLoader)
    }

    static native long nServer(byte[] json);
    static native long nClient(byte[] ticket, byte[] json);
    static native int nState(long h);
    static native int nWait(long h, int ms);
    static native int nLocalPort(long h, int i);
    static native byte[] nTicket(long h);
    static native byte[] nError(long h);
    static native byte[] nNextLog(long h);
    static native long nPeerCount(long h);
    static native void nFree(long h);

    private volatile long h;

    private PeerovaTunnel(long h) {
        if (h == 0) throw new Peerova.PeerovaException(Peerova.lastError());
        this.h = h;
    }

    private static byte[] z(String s) { return s == null ? null : s.getBytes(UTF8); }
    private static String str(byte[] b) { return b == null ? null : new String(b, UTF8); }

    /** Starts a tunnel server; blocks until the ticket is ready (about 5 s max). */
    public static PeerovaTunnel server(String configJson) { return new PeerovaTunnel(nServer(z(configJson))); }

    /** Starts connecting; returns immediately (local ports are already assigned). */
    public static PeerovaTunnel client(String ticket, String configJson) {
        if (ticket == null || ticket.trim().isEmpty()) throw new IllegalArgumentException("ticket is empty");
        return new PeerovaTunnel(nClient(z(ticket.trim()), z(configJson)));
    }

    private long handle() {
        long x = h;
        if (x == 0) throw new IllegalStateException("tunnel is closed");
        return x;
    }

    public int state() { long x = h; return x == 0 ? DOWN : nState(x); }

    /** Waits up to timeoutMs while CONNECTING; returns the state. */
    public int await(int timeoutMs) { return nWait(handle(), timeoutMs); }

    public int localPort(int index) { return nLocalPort(handle(), index); }

    public String ticket() { return str(nTicket(handle())); }

    /** Last error / disconnect reason, or null. */
    public String error() { long x = h; return x == 0 ? "tunnel closed" : str(nError(x)); }

    /** Oldest pending log line, or null. */
    public String nextLog() { long x = h; return x == 0 ? null : str(nNextLog(x)); }

    public long peerCount() { long x = h; return x == 0 ? 0 : nPeerCount(x); }

    public boolean isClosed() { return h == 0; }

    @Override public synchronized void close() {
        long x = h;
        h = 0;
        if (x != 0) nFree(x);
    }
}
