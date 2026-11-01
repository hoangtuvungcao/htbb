package htbb.p2p;

/**
 * Client PC Java / Android: cong game di qua Peerova P2P (JNI, trong tien trinh) thay cho TCP thang.
 *
 * Bat khi ban build co TICKET (thuoc tinh "HTBB-P2P-Ticket" trong htbb-app.properties, hoac ghi de luc chay -
 * PC: --p2p-ticket / bien HTBB_P2P_TICKET / p2p_ticket= trong cai_dat.properties). Khi bat, MOI ket noi
 * socket://host:cong cua game di qua tunnel: Peerova nghe 127.0.0.1:&lt;cong tu chon&gt; va chuyen tung ket noi TCP
 * thanh mot stream P2P toi cong game {@code cong} cua may chu (host trong URL bi bo qua - ticket xac dinh may chu).
 *
 * KHONG lui ve TCP thang khi P2P loi (quyet dinh chu du an): nem IOException co cau bao ro rang; game hien
 * "mat ket noi" nhu moi loi mang khac. Client J2ME (khong nap duoc thu vien native) khong dung lop nay.
 *
 * Tep nay la NGUON CHUNG o p2p/java/htbb/p2p/ - chep vao client_pc/src va client_apk bang p2p/build/sync-java.sh.
 */
public final class KetNoiP2P {
    private KetNoiP2P() {
    }

    private static dev.peerova.PeerovaTunnel tunnel;
    private static int congXa = -1;
    /** Cau loi gan nhat (hien cho nguoi choi / nhat ky). */
    public static volatile String loiCuoi = "";
    /** Ghi nhat ky (null = System.out). */
    public static volatile java.io.PrintStream nhatKy;

    /** Thuoc tinh ung dung: PC = htbb-app.properties + ghi de dong lenh; Android = assets/htbb-app.properties. */
    static String cauHinh(final String k) {
        final String v = javax.microedition.midlet.MIDlet.thuocTinh(k);
        return v == null ? "" : v.trim();
    }

    public static String ticket() {
        return cauHinh("HTBB-P2P-Ticket");
    }

    /** Ban build / cau hinh nay dung P2P (co ticket). */
    public static boolean bat() {
        return ticket().length() > 0;
    }

    static void ghi(final String s) {
        final java.io.PrintStream p = nhatKy;
        (p != null ? p : System.out).println("[P2P] " + s);
    }

    private static void xaLog(final dev.peerova.PeerovaTunnel t) {
        String l;
        while ((l = t.nextLog()) != null) {
            ghi(l);
        }
    }

    /**
     * Cong 127.0.0.1 da noi P2P toi cong game {@code cong} cua may chu. Dung lai tunnel dang song; tunnel chet /
     * cong khac -> tao moi. Chan toi khi noi xong (toi da HTBB-P2P-Timeout ms, mac dinh 20000).
     */
    public static synchronized int congCucBo(final int cong) throws java.io.IOException {
        final String tk = ticket();
        if (tk.length() == 0) {
            throw new java.io.IOException("P2P: thieu ticket may chu");
        }
        if (tunnel != null && (congXa != cong || tunnel.state() == dev.peerova.PeerovaTunnel.DOWN)) {
            xaLog(tunnel);
            tunnel.close();
            tunnel = null;
        }
        if (tunnel == null) {
            final StringBuilder j = new StringBuilder("{\"forward\":[\"tcp:0=").append(cong).append("\"]");
            final String token = cauHinh("HTBB-P2P-Token");
            if (token.length() > 0) {
                j.append(",\"token\":\"").append(token.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
            }
            int to = 20000;
            try {
                if (cauHinh("HTBB-P2P-Timeout").length() > 0) to = Integer.parseInt(cauHinh("HTBB-P2P-Timeout"));
            } catch (NumberFormatException ignored) {
                // giu mac dinh
            }
            j.append(",\"timeout_ms\":").append(to).append('}');
            try {
                tunnel = dev.peerova.PeerovaTunnel.client(tk, j.toString());
            } catch (Throwable e) {
                loiCuoi = "Không khởi động được kết nối P2P: " + thongDiep(e);
                ghi(loiCuoi);
                throw new java.io.IOException(loiCuoi);
            }
            congXa = cong;
            ghi("dang noi P2P toi may chu (cong game " + cong + ") qua 127.0.0.1:" + tunnel.localPort(0));
        }
        final int st = tunnel.await(30000);
        xaLog(tunnel);
        if (st != dev.peerova.PeerovaTunnel.UP) {
            final String e = tunnel.error();
            loiCuoi = "Không kết nối P2P được tới máy chủ" + (e == null || e.length() == 0 ? "" : ": " + e);
            ghi(loiCuoi);
            tunnel.close();
            tunnel = null;
            throw new java.io.IOException(loiCuoi);
        }
        loiCuoi = "";
        return tunnel.localPort(0);
    }

    private static String thongDiep(final Throwable e) {
        if (e instanceof UnsatisfiedLinkError) {
            return "thiếu thư viện peerova_jni (" + e.getMessage() + ")";
        }
        return e.getMessage() == null ? e.toString() : e.getMessage();
    }

    public static synchronized void dong() {
        if (tunnel != null) {
            tunnel.close();
            tunnel = null;
        }
    }
}
