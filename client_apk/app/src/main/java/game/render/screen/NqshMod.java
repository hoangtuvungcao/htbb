package game.render.screen;

import com.htbb.client.GameMidlet;
import game.networklogic.GameService;
import game.render.GCanvas;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Vector;
import network.Message;

/**
 * Mod NQSH-H2 cho APK KPAH_277 gốc của TeaMobi (chèn bằng smali, xem patch.py):
 * - ServerListScr.loadSv  -> apDungServer(): server nqsh-h2.top:30774 + tự tải danh sách từ LINK (1 lần / lần mở game)
 * - ServerListScr.doUpdateServer -> capNhat(): đọc "Tên|host|port" như TaiDanhSach của bản jar
 * - GameService.login     -> loginGuard(): xác thực -88 trước (KiemServer của bản jar) + mã máy
 * - MessageHandler.onMessage -> goiGuard(): nhận gói -88
 */
public final class NqshMod {
    private static final String LINK = "https://nqsh-h2.top/robots.txt";
    private static final int LENH = -88;
    private static final long CHO_MS = 30000L;

    /** Khóa công khai (e = 65537), 32 số 32-bit, số thấp trước - giống KiemServer.N của bản jar. */
    private static final int[] N = {0xe753c265, 0x2b76f6a4, 0x15a19590, 0xecf19eea, 0x67d5e2e9, 0xb66ba946, 0xfc4bc516, 0xc850b2ee, 0x2820ecf2, 0xf820c771, 0x8cfa9f69, 0xbffac9b0, 0x301d384f, 0xfd55af57, 0xcc91852b, 0xa211e18b, 0xc35948b6, 0xbe73bbd2, 0xf1105817, 0x5a8b5de1, 0xd5b57bc0, 0xdaedb6ef, 0x5dba856, 0x37b5e20e, 0x62c91a56, 0x64dd8242, 0x582aa78d, 0x4519d24d, 0x9d3c0b84, 0x9ea53550, 0xf821af79, 0x8ca875ab};
    /** Giống AutoGuard.MAU_NEN của bản jar. */
    private static final int[] MAU_NEN = {0x69436c, 0x45957f, 0xd11674, 0xb24394, 0xf69a31, 0x8c9b09, 0xbcdeeb, 0x8f48ec};
    private static final byte[] MAGIC = {-115, 50, 41, -64, -25, 78, 23, -22};
    private static final SecureRandom NGAU_NHIEN = new SecureRandom();

    // volatile: ghi từ luồng mạng (nhan), luồng hẹn giờ 30 s và luồng game
    private static volatile boolean dangCho;
    private static volatile boolean choPhep;
    private static volatile int lan;
    private static String host;
    private static byte[] nonce;
    private static byte[] nonceS;
    private static String tk;
    private static String mk;
    private static String ver;
    /** CHẨN ĐOÁN: các bước xác thực đã qua, hiện trong hộp thoại khi lỗi. */
    private static StringBuffer buoc = new StringBuffer();

    private static void ghi(String s) {
        buoc.append(buoc.length() == 0 ? "" : " > ").append(s);
        System.out.println("NQSH: " + s);
    }

    private NqshMod() {
    }

    // ---------------------------------------------------------------- danh sách server

    public static void apDungServer() {
        ServerListScr.nameServer = new String[]{"NQSH-H2"};
        ServerListScr.address = new String[]{"nqsh-h2.top"};
        ServerListScr.port = new short[]{30774};
        ServerListScr.index_server = new byte[]{0};
        ServerListScr.linkGetHost = LINK;
        taiDanhSachNgam();
        NqshCapNhat.batDau();
    }

    private static boolean daTaiNgam;

    /**
     * Mỗi lần mở game tự tải danh sách server một lần (như bấm "Cập nhật" nhưng ngầm): đổi IP / cổng
     * chỉ cần sửa robots.txt trên web. Lỗi mạng thì giữ danh sách đang có (đã lưu RMS "ipnqsh").
     */
    private static synchronized void taiDanhSachNgam() {
        if (daTaiNgam) {
            return;
        }
        daTaiNgam = true;
        new Thread() {
            public void run() {
                try {
                    boolean ok = apDung(GameMidlet.connectHTTP(LINK), false);
                    System.out.println("NQSH: danh sach server tu dong " + (ok ? "OK " + ServerListScr.address[0] + ":" + ServerListScr.port[0] : "loi"));
                } catch (Throwable t) {
                    System.out.println("NQSH: danh sach server tu dong loi " + t);
                }
            }
        }.start();
    }

    // ---------------------------------------------------------------- tab "Auto" đầu tiên của MenuWindows
    // "Tab ảo": chỉ thêm vào mảng tên tab (vẽ + bấm), số tab bên trong (focusTabChinh) giữ nguyên nên mọi
    // tab gốc vẫn mở đúng nội dung. patch.py: tabName nối "Auto" ở đầu, paintTab cộng 1 khi tô tab đang
    // chọn, updateTab qua tabAuto() trước khi gán tdx.

    public static final String TAB_AUTO = "Auto";

    /** Sau khi MenuWindows tạo tabName: thêm "Auto" ở đầu. */
    public static String[] themTabAuto(String[] goc) {
        String[] moi = new String[goc.length + 1];
        moi[0] = TAB_AUTO;
        System.arraycopy(goc, 0, moi, 1, goc.length);
        return moi;
    }

    /**
     * Bấm thanh tab (ô thứ o, tính cả tab Auto): ô 0 = Auto -> đóng MenuWindows, mở menu mod, trả -1 (bỏ
     * phần còn lại); ô khác trả o - 1 = số ô của tab gốc, game xử lý như cũ.
     */
    public static int tabAuto(int o) {
        if (o != 0) {
            return o - 1;
        }
        GCanvas.gameScr.switchToMe();
        PauseMenu.moMenuMod();
        return -1;
    }

    /** Thay thân ServerListScr.doUpdateServer (nút "Cập nhật"). */
    public static void capNhat() {
        GCanvas.startWaitDlg();
        String s = GameMidlet.connectHTTP(LINK);
        if (!apDung(s, true)) {
            GCanvas.startOKDlg("Không thể kết nối, xin kiểm tra lại GPRS/3G/Wifi.");
            return;
        }
        GCanvas.endDlg();
    }

    /** Mỗi dòng "Tên|host|port", dòng trống / # bỏ qua. veLai = vẽ lại màn chọn server (chỉ khi đang ở màn đó). */
    private static boolean apDung(String s, boolean veLai) {
        if (s == null) {
            return false;
        }
        Vector ds = new Vector();
        int i = 0;
        while (i < s.length()) {
            int j = s.indexOf('\n', i);
            if (j < 0) {
                j = s.length();
            }
            String dong = s.substring(i, j).trim();
            i = j + 1;
            if (dong.length() == 0 || dong.charAt(0) == '#') {
                continue;
            }
            int a = dong.indexOf('|');
            int b = a < 0 ? -1 : dong.indexOf('|', a + 1);
            if (b < 0) {
                continue;
            }
            try {
                String ten = dong.substring(0, a).trim();
                String h = dong.substring(a + 1, b).trim();
                short p = Short.parseShort(dong.substring(b + 1).trim());
                if (ten.length() > 0 && h.length() > 0) {
                    ds.addElement(new Object[]{ten, h, Short.valueOf(p)});
                }
            } catch (Exception e) {
                // dòng hỏng thì bỏ
            }
        }
        int n = ds.size();
        if (n == 0) {
            return false;
        }
        String[] ten = new String[n];
        String[] dc = new String[n];
        short[] cong = new short[n];
        for (int k = 0; k < n; k++) {
            Object[] o = (Object[]) ds.elementAt(k);
            ten[k] = (String) o[0];
            dc[k] = (String) o[1];
            cong[k] = ((Short) o[2]).shortValue();
        }
        ServerListScr.nameServer = ten;
        ServerListScr.address = dc;
        ServerListScr.port = cong;
        ServerListScr.index_server = new byte[n];
        // danh sách ngắn lại mà đang chọn server cuối -> address[index] ngoài mảng, mọi lần đăng nhập đều lỗi
        if (ServerListScr.index >= n) {
            ServerListScr.index = 0;
        }
        ServerListScr.saveIP();
        if (veLai) {
            ServerListScr.gI().init();
        }
        return true;
    }

    // ---------------------------------------------------------------- link web

    private static final String WEB = "https://nqsh-h2.top";

    /**
     * Link server gửi xuống (trang chủ GameMidlet.url, link tải game qua platformRequest) mà
     * trỏ thẳng IP thì đổi sang tên miền, giữ nguyên đường dẫn. Link khác để nguyên.
     */
    public static String suaLink(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        int a = t.indexOf("://");
        int b = a < 0 ? 0 : a + 3;
        int c = b;
        while (c < t.length() && "/?#".indexOf(t.charAt(c)) < 0) {
            c++;
        }
        String host = t.substring(b, c);
        int hai = host.lastIndexOf(':');
        if (hai >= 0) {
            host = host.substring(0, hai);
        }
        if (host.length() == 0 || !host.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {
            return s;
        }
        String duong = t.substring(c);
        return WEB + (duong.length() == 0 ? "/" : duong);
    }

    // ---------------------------------------------------------------- đăng nhập + xác thực -88

    /** Đầu GameService.login: true = đã xử lý (đang xác thực, hoặc đã tự gửi gói đăng nhập). */
    public static boolean loginGuard(String u, String p, String v) {
        // nhớ tài khoản cho "tự đăng nhập lại" (TienIch) / đăng nhập lại khi nuôi pet - như AutoGuard.loginGuard của jar
        if (u != null && u.length() > 0 && p != null && p.length() > 0) {
            AutoGuard.tkDangNhap = u;
            AutoGuard.mkDangNhap = p;
        }
        try {
            if (chanDangNhap(u, p, v)) {
                return true;
            }
        } catch (Throwable t) {
            ghi("loi login " + t);
            return true;
        }
        boolean ok = guiDangNhap(u, p, v);
        ghi(ok ? "da gui dang nhap" : "loi gui dang nhap");
        return ok;
    }

    /** Đầu MessageHandler.onMessage: true = gói -88 đã xử lý ở đây. */
    public static boolean goiGuard(Message m) {
        try {
            // Vận may: nuốt gói mở màn rương (-62) như AutoGuard.goiGuard của jar
            if (AutoVanMay.anGoi(m.command)) {
                return true;
            }
            return nhan(m);
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean chanDangNhap(String u, String p, String v) {
        if (choPhep) {
            choPhep = false;
            return false;
        }
        buoc.setLength(0);
        ghi("login");
        tk = u;
        mk = p;
        ver = v;
        dangCho = true;
        final int n = ++lan;
        try {
            host = ServerListScr.address[ServerListScr.index];
            nonce = new byte[16];
            NGAU_NHIEN.nextBytes(nonce);
            Message m = new Message(LENH);
            m.writer().writeByte(1);
            m.writer().writeUTF(host);
            m.writer().write(nonce);
            GameService.gI().session.sendMessage(m);
            m.cleanup();
            ghi("gui88 " + host);
        } catch (Throwable t) {
            ghi("loi gui88 " + t);
            thatBai(n);
            return true;
        }
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(CHO_MS);
                } catch (InterruptedException e) {
                    // bỏ qua
                }
                if (n == lan && dangCho) {
                    ghi("het gio 30s");
                }
                thatBai(n);
            }
        }.start();
        return true;
    }

    private static boolean nhan(Message m) {
        if (m.command != LENH) {
            return false;
        }
        ghi("nhan88 " + (dangCho ? "" : "(khong cho)"));
        if (!dangCho) {
            return true;
        }
        int n = lan;
        boolean ok = false;
        try {
            byte[] chuKy = new byte[128];
            m.reader().readFully(chuKy);
            nonceS = new byte[16];
            m.reader().readFully(nonceS);
            ok = kiem(chuKy);
        } catch (Throwable t) {
            ghi("loi doc88 " + t);
        }
        ghi(ok ? "chu ky dung" : "chu ky SAI");
        if (!ok) {
            thatBai(n);
            return true;
        }
        try {
            Message m2 = new Message(LENH);
            m2.writer().writeByte(2);
            m2.writer().write(chungMinh(hostBytes(), nonce, nonceS));
            GameService.gI().session.sendMessage(m2);
            m2.cleanup();
            ghi("gui chung minh");
        } catch (Throwable t) {
            ghi("loi chung minh " + t);
            thatBai(n);
            return true;
        }
        dangCho = false;
        choPhep = true;
        GameService.gI().login(tk, mk, ver);
        return true;
    }

    private static void thatBai(int n) {
        if (n != lan || !dangCho) {
            return;
        }
        dangCho = false;
        GCanvas.gameScr.onConnectFail();
        GCanvas.startOKDlg("Không thể kết nối");
    }

    /** Như GameService.login gốc nhưng thêm MÃ MÁY ở trường cuối (server: LoginHandler -> CamTheoMay). */
    private static boolean guiDangNhap(String u, String p, String v) {
        try {
            Message m = new Message((byte) 1);
            m.writer().writeUTF(u);
            m.writer().writeUTF(p);
            m.writer().writeUTF(v);
            m.writer().writeUTF("nokia/1/1");
            m.writer().writeUTF(GameMidlet.client_Provider);
            m.writer().writeUTF(GameMidlet.provider);
            m.writer().writeUTF(GameMidlet.agent);
            m.writer().writeByte(GameMidlet.VERSION);
            m.writer().writeShort(GCanvas.w);
            m.writer().writeByte(GCanvas.isLoadTileServer ? -1 : 0);
            m.writer().writeByte(GameService.gI().getInDexServer(ServerListScr.index));
            m.writer().writeUTF(maMay());
            GameService.gI().session.sendMessage(m);
            m.cleanup();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Mã máy: chuỗi ngẫu nhiên sinh một lần, lưu RMS "kpahmay" (chỉ chữ/số, server lọc như vậy). */
    static String maMay() {
        String s = null;
        try {
            s = RMS.loadRMSString("kpahmay");
        } catch (Throwable t) {
            // chưa có
        }
        if (s != null && s.length() >= 16) {
            return s;
        }
        String chu = "0123456789abcdefghijklmnopqrstuvwxyz";
        StringBuffer sb = new StringBuffer("andr");
        for (int i = 0; i < 24; i++) {
            sb.append(chu.charAt(NGAU_NHIEN.nextInt(chu.length())));
        }
        s = sb.toString();
        try {
            RMS.saveRMSString("kpahmay", s);
        } catch (Throwable t) {
            // không lưu được thì lần sau sinh mã khác
        }
        return s;
    }

    private static byte[] hostBytes() {
        try {
            return host.getBytes("UTF-8");
        } catch (Exception e) {
            return host.getBytes();
        }
    }

    /** N dạng 128 byte, byte cao trước. */
    private static byte[] nBytes() {
        byte[] b = new byte[128];
        for (int i = 0; i < 128; i++) {
            int j = 127 - i;
            b[i] = (byte) (N[j >> 2] >>> ((j & 3) << 3));
        }
        return b;
    }

    private static boolean kiem(byte[] chuKy) {
        BigInteger n = new BigInteger(1, nBytes());
        BigInteger s = new BigInteger(1, chuKy);
        if (s.compareTo(n) >= 0) {
            return false;
        }
        return s.modPow(BigInteger.valueOf(65537L), n).equals(new BigInteger(1, dongGoi()));
    }

    /** 00 01 FF..FF 00 MAGIC host nC nS - phải giống hệt XacThucClient.dongGoi() bên server. */
    private static byte[] dongGoi() {
        byte[] h = hostBytes();
        byte[] kq = new byte[128];
        int o = 128 - MAGIC.length - h.length - nonce.length - nonceS.length;
        kq[1] = 1;
        for (int i = 2; i < o - 1; i++) {
            kq[i] = (byte) 0xFF;
        }
        System.arraycopy(MAGIC, 0, kq, o, MAGIC.length);
        o += MAGIC.length;
        System.arraycopy(h, 0, kq, o, h.length);
        o += h.length;
        System.arraycopy(nonce, 0, kq, o, nonce.length);
        System.arraycopy(nonceS, 0, kq, o + nonce.length, nonceS.length);
        return kq;
    }

    /** k = SHA-256(N 128 byte | MAU_NEN); trả SHA-256(k | nS | nC | host | k). */
    public static byte[] chungMinh(byte[] h, byte[] nC, byte[] nS) throws Exception {
        byte[] goc = new byte[128 + MAU_NEN.length * 4];
        System.arraycopy(nBytes(), 0, goc, 0, 128);
        for (int i = 0; i < MAU_NEN.length; i++) {
            int v = MAU_NEN[i];
            goc[128 + i * 4] = (byte) (v >>> 24);
            goc[129 + i * 4] = (byte) (v >>> 16);
            goc[130 + i * 4] = (byte) (v >>> 8);
            goc[131 + i * 4] = (byte) v;
        }
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] k = md.digest(goc);
        md.reset();
        md.update(k);
        md.update(nS);
        md.update(nC);
        md.update(h);
        md.update(k);
        return md.digest();
    }
}
