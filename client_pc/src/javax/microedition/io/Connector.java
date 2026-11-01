package javax.microedition.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

/** Connector: socket:// -> java.net.Socket, http(s):// -> HttpURLConnection; sms:// không hỗ trợ. */
public class Connector {
    public static final int READ = 1;
    public static final int WRITE = 2;
    public static final int READ_WRITE = 3;

    private Connector() {
    }

    public static Connection open(String name) throws IOException {
        return open(name, READ_WRITE, false);
    }

    public static Connection open(String name, int mode) throws IOException {
        return open(name, mode, false);
    }

    public static Connection open(String name, int mode, boolean timeouts) throws IOException {
        if (name == null) throw new IllegalArgumentException();
        int sc = name.indexOf(';');
        String url = sc >= 0 ? name.substring(0, sc) : name; // bỏ ";deviceside=true" ... của BlackBerry
        if (url.startsWith("socket://")) {
            String hp = url.substring(9);
            int c = hp.lastIndexOf(':');
            if (c <= 0) throw new IllegalArgumentException(name);
            String host = hp.substring(0, c);
            int port = Integer.parseInt(hp.substring(c + 1));
            // Nhanh p2p: ban build co ticket Peerova -> moi ket noi game di qua tunnel P2P trong tien trinh
            // (127.0.0.1:<cong tu chon> -> cong game cua may chu). Loi P2P KHONG lui ve TCP thang.
            if (htbb.p2p.KetNoiP2P.bat()) {
                network.Session_ME.lyDoLoiKetNoi = null;
                try {
                    port = htbb.p2p.KetNoiP2P.congCucBo(port);
                    host = "127.0.0.1";
                } catch (IOException e) {
                    network.Session_ME.lyDoLoiKetNoi = htbb.p2p.KetNoiP2P.loiCuoi;
                    throw e;
                }
            }
            Socket s = new Socket();
            s.connect(new InetSocketAddress(host, port), 15000);
            s.setTcpNoDelay(true);
            s.setKeepAlive(true);
            return new KetNoiSocket(s);
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return new KetNoiHttp(name);
        }
        throw new ConnectionNotFoundException(name);
    }

    public static InputStream openInputStream(String name) throws IOException {
        return ((InputConnection) open(name, READ)).openInputStream();
    }

    public static DataInputStream openDataInputStream(String name) throws IOException {
        return new DataInputStream(openInputStream(name));
    }

    public static OutputStream openOutputStream(String name) throws IOException {
        return ((OutputConnection) open(name, WRITE)).openOutputStream();
    }

    public static DataOutputStream openDataOutputStream(String name) throws IOException {
        return new DataOutputStream(openOutputStream(name));
    }

    static final class KetNoiSocket implements SocketConnection {
        private final Socket s;

        KetNoiSocket(Socket s) {
            this.s = s;
        }

        public void setSocketOption(byte o, int v) throws IOException {
            switch (o) {
                case DELAY: s.setTcpNoDelay(v == 0); break;
                case LINGER: s.setSoLinger(v > 0, v); break;
                case KEEPALIVE: s.setKeepAlive(v != 0); break;
                case RCVBUF: s.setReceiveBufferSize(v); break;
                case SNDBUF: s.setSendBufferSize(v); break;
                default: throw new IllegalArgumentException();
            }
        }

        public int getSocketOption(byte o) throws IOException {
            switch (o) {
                case DELAY: return s.getTcpNoDelay() ? 0 : 1;
                case LINGER: return s.getSoLinger();
                case KEEPALIVE: return s.getKeepAlive() ? 1 : 0;
                case RCVBUF: return s.getReceiveBufferSize();
                case SNDBUF: return s.getSendBufferSize();
                default: throw new IllegalArgumentException();
            }
        }

        public String getLocalAddress() {
            return s.getLocalAddress().getHostAddress();
        }

        public int getLocalPort() {
            return s.getLocalPort();
        }

        public String getAddress() {
            return s.getInetAddress().getHostAddress();
        }

        public int getPort() {
            return s.getPort();
        }

        // Kiem lag 10/2026: Session_ME doc dau goi tung byte (readByte) - tren luong socket tho moi byte la mot lan
        // goi he thong (Android: kem BlockGuard). Dem 8 KB: cung byte, it lan doc hon (~4 -> ~1 lan / goi luc dong).
        private InputStream vao;

        private synchronized InputStream vao() throws IOException {
            if (vao == null) {
                vao = new java.io.BufferedInputStream(s.getInputStream(), 8192);
            }
            return vao;
        }

        public InputStream openInputStream() throws IOException {
            return vao();
        }

        public DataInputStream openDataInputStream() throws IOException {
            return new DataInputStream(vao());
        }

        public OutputStream openOutputStream() throws IOException {
            return s.getOutputStream();
        }

        public DataOutputStream openDataOutputStream() throws IOException {
            return new DataOutputStream(s.getOutputStream());
        }

        public void close() throws IOException {
            s.close();
        }
    }

    static final class KetNoiHttp implements HttpConnection {
        private final String url;
        private final HttpURLConnection c;

        KetNoiHttp(String url) throws IOException {
            this.url = url;
            this.c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(15000);
            c.setReadTimeout(20000);
        }

        public String getURL() {
            return url;
        }

        public String getProtocol() {
            return c.getURL().getProtocol();
        }

        public String getHost() {
            return c.getURL().getHost();
        }

        public String getFile() {
            return c.getURL().getPath();
        }

        public String getRef() {
            return c.getURL().getRef();
        }

        public String getQuery() {
            return c.getURL().getQuery();
        }

        public int getPort() {
            int p = c.getURL().getPort();
            return p < 0 ? c.getURL().getDefaultPort() : p;
        }

        public String getRequestMethod() {
            return c.getRequestMethod();
        }

        public void setRequestMethod(String m) throws IOException {
            c.setRequestMethod(m);
            if (POST.equals(m)) c.setDoOutput(true);
        }

        public String getRequestProperty(String k) {
            return c.getRequestProperty(k);
        }

        public void setRequestProperty(String k, String v) {
            c.setRequestProperty(k, v);
        }

        public int getResponseCode() throws IOException {
            return c.getResponseCode();
        }

        public String getResponseMessage() throws IOException {
            return c.getResponseMessage();
        }

        public long getExpiration() {
            return c.getExpiration();
        }

        public long getDate() {
            return c.getDate();
        }

        public long getLastModified() {
            return c.getLastModified();
        }

        public String getHeaderField(String n) {
            return c.getHeaderField(n);
        }

        public int getHeaderFieldInt(String n, int d) {
            return c.getHeaderFieldInt(n, d);
        }

        public long getHeaderFieldDate(String n, long d) {
            return c.getHeaderFieldDate(n, d);
        }

        public String getHeaderField(int n) {
            return c.getHeaderField(n);
        }

        public String getHeaderFieldKey(int n) {
            return c.getHeaderFieldKey(n);
        }

        public String getType() {
            return c.getContentType();
        }

        public String getEncoding() {
            return c.getContentEncoding();
        }

        public long getLength() {
            return c.getContentLength();
        }

        public InputStream openInputStream() throws IOException {
            return new DocDu(c.getInputStream());
        }

        public DataInputStream openDataInputStream() throws IOException {
            return new DataInputStream(openInputStream());
        }

        public OutputStream openOutputStream() throws IOException {
            c.setDoOutput(true);
            return c.getOutputStream();
        }

        public DataOutputStream openDataOutputStream() throws IOException {
            return new DataOutputStream(openOutputStream());
        }

        public void close() {
            c.disconnect();
        }
    }

    /** read(byte[]) đọc đủ (code J2ME hay gọi read một lần với mảng bằng getLength()). */
    static final class DocDu extends java.io.FilterInputStream {
        DocDu(InputStream in) {
            super(in);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int tong = 0;
            while (tong < len) {
                int n = in.read(b, off + tong, len - tong);
                if (n < 0) return tong == 0 ? -1 : tong;
                tong += n;
            }
            return tong;
        }
    }
}
