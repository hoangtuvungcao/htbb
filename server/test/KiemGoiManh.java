import io.GoiManh;
import io.Message;
import io.Session;
import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Kiem io.GoiManh (0.9.7.1, client J2ME ket man tai tren gia lap iOS): chia goi lon thanh manh -123 cho client tu
 * bao KHA_NANG_MANH.
 *  - chia(): moi manh < 32768 byte, gom lai dung tung byte (cung thuat toan network.Session_ME.nhanManh), khung nho /
 *    -40 / goi nen khong chia, sv.manhGoi=0 tat;
 *  - QUA SOCKET THAT (io.Session + Sender, ma hoa XOR bat): goi 26 ~159 KB gui CUNG LUC voi 4 luong goi nho - cac
 *    manh nam LIEN TIEP (khong goi nao chen giua), gom lai dung; client khong bao bit (0.9.7) nhan nguyen mot khung
 *    nhu cu; client nhan tung mau 1..97 byte (doc socket tung phan) van dung;
 *  - client: Session_ME bao bit 2, gom manh khong ghep mang lon (LuongManh), bat ca OutOfMemoryError o luong mang.
 */
public final class KiemGoiManh {
    private KiemGoiManh() {
    }

    /** Gom cac khung (cmd, du lieu) nhu client: tra ve danh sach goi {cmd, du lieu} da gom manh + so lan chen giua. */
    static final class Gom {
        final List<Object[]> goi = new ArrayList<Object[]>();
        int chenGiua, manh;
        private int con = -1, lenh;
        private java.io.ByteArrayOutputStream dl;

        void them(int cmd, byte[] d) {
            if (cmd == GoiManh.LENH_MANH) {
                manh++;
                if (d[0] == 0) {
                    lenh = d[1];
                    int tong = ((d[2] & 255) << 24) | ((d[3] & 255) << 16) | ((d[4] & 255) << 8) | (d[5] & 255);
                    dl = new java.io.ByteArrayOutputStream();
                    dl.write(d, 6, d.length - 6);
                    con = tong - (d.length - 6);
                } else {
                    dl.write(d, 1, d.length - 1);
                    con -= d.length - 1;
                }
                if (con == 0) {
                    goi.add(new Object[]{Integer.valueOf(lenh), dl.toByteArray()});
                    con = -1;
                }
                return;
            }
            if (con > 0) {
                chenGiua++;
            }
            goi.add(new Object[]{Integer.valueOf(cmd), d});
        }
    }

    static void gomTuKhung(Gom g, byte[] k) {
        Object[] o = KiemGoiNen.docKhung(k);
        g.them(((Integer) o[0]).intValue(), (byte[]) o[1]);
    }

    static byte[] ngauNhien(int n, long hat) {
        byte[] d = new byte[n];
        new Random(hat).nextBytes(d);
        return d;
    }

    public static void chay() {
        Kiem.nhom("io.GoiManh - chia goi lon thanh manh cho client J2ME");
        try {
            final byte[] d26 = ngauNhien(159000, 26);
            final byte[] goc = KiemGoiNen.khung(26, d26);
            final byte[][] m = GoiManh.chia(goc);
            Kiem.dung("goi 26 159 KB duoc chia", m != null && m.length > 1);
            boolean nho = true;
            for (int i = 0; m != null && i < m.length; i++) {
                nho &= m[i].length <= 3 + GoiManh.CO_MANH && m[i][0] == GoiManh.LENH_MANH && m[i].length < 32768;
            }
            Kiem.dung("moi manh la lenh -123, <= " + GoiManh.CO_MANH + " B du lieu, khung < 32768", nho);
            Gom g = new Gom();
            for (int i = 0; i < m.length; i++) {
                gomTuKhung(g, m[i]);
            }
            Kiem.bang("gom duoc dung 1 goi", Integer.valueOf(1), Integer.valueOf(g.goi.size()));
            Kiem.bang("goi gom la lenh 26", Integer.valueOf(26), g.goi.get(0)[0]);
            Kiem.dung("du lieu gom dung tung byte", Arrays.equals(d26, (byte[]) g.goi.get(0)[1]));
            // bien: dung bang nguong, nguong+1 byte, du lieu dung vua mot manh dau
            int[] co = {GoiManh.NGUONG, GoiManh.NGUONG + 1, GoiManh.CO_MANH - 6, GoiManh.CO_MANH - 5, 3 * (GoiManh.CO_MANH - 1) + GoiManh.CO_MANH - 6, 40000};
            boolean bienOk = true;
            for (int c : co) {
                byte[] d = ngauNhien(c, c);
                byte[][] mm = GoiManh.chia(KiemGoiNen.khung(25, d));
                if (c < GoiManh.NGUONG) {
                    bienOk &= mm == null;
                    continue;
                }
                Gom gg = new Gom();
                for (byte[] x : mm) {
                    gomTuKhung(gg, x);
                }
                bienOk &= gg.goi.size() == 1 && Arrays.equals(d, (byte[]) gg.goi.get(0)[1]);
            }
            Kiem.dung("bien do dai (nguong, vua manh dau, > 32767 tien to -128) gom dung", bienOk);
            Kiem.dung("goi < nguong khong chia", GoiManh.chia(KiemGoiNen.khung(4, new byte[1000])) == null);
            Kiem.dung("goi -40 (khoa) khong chia", GoiManh.chia(KiemGoiNen.khung(-40, new byte[40000])) == null);
            Kiem.dung("goi nen -127 khong chia", GoiManh.chia(KiemGoiNen.khung(-127, new byte[40000])) == null);
            GoiManh.BAT = false;
            Kiem.dung("sv.manhGoi=0 -> khong chia", GoiManh.chia(goc) == null);
            GoiManh.BAT = true;

            // ---- qua socket that: Session + Sender, XOR bat, nhieu luong gui cung luc
            Object[] kq = quaSocket(GoiManh.KHA_NANG_MANH, d26, 97);
            Gom gs = (Gom) kq[0];
            Kiem.dung("socket: co manh -123 (client bao bit 2)", gs.manh > 1);
            Kiem.bang("socket: khong goi nao chen giua cac manh", Integer.valueOf(0), Integer.valueOf(gs.chenGiua));
            Kiem.dung("socket: goi 26 gom dung tung byte (XOR + doc tung mau <= 97 B)", Boolean.TRUE.equals(kq[1]));
            Kiem.bang("socket: du 4 x 200 goi nho", Integer.valueOf(800), kq[2]);
            Object[] cu = quaSocket(0, d26, 4096);
            Gom gc = (Gom) cu[0];
            Kiem.bang("socket client 0.9.7 (khong bit): khong manh nao", Integer.valueOf(0), Integer.valueOf(gc.manh));
            Kiem.dung("socket client 0.9.7: goi 26 nguyen khung dung", Boolean.TRUE.equals(cu[1]));

            // ---- theo doi dang nhap -> vao ban do (dong nhat ky [VAO_GAME])
            Session se = new Session(null, null, true);
            se.username = "kiem";
            se.lucDangNhap = System.currentTimeMillis() - 120000L;
            java.lang.reflect.Method mt = Session.class.getDeclaredMethod("moTaVaoGame", long.class, String.class);
            mt.setAccessible(true);
            String s = (String) mt.invoke(se, Long.valueOf(System.currentTimeMillis()), "CHUA vao duoc ban do");
            Kiem.dung("mo ta vao game: buoc + thoi gian + byte + platf", s.contains("chua chon / tao Pokemon") && s.contains("sau 120 s")
                    && s.contains("da gui 0 KB") && s.contains("platf="));

            // ---- client
            final String sm = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("../client_jar/src/network/Session_ME.java")), "UTF-8");
            Kiem.dung("client: lenh -123 + bit kha nang 2 luon bao", sm.contains("GOI_MANH = -123") && sm.contains("KHA_NANG_MANH = 2")
                    && sm.contains("| KHA_NANG_MANH"));
            Kiem.dung("client: gom manh qua LuongManh (khong ghep mang lon)", sm.contains("new LuongManh(ds, dau)")
                    && new java.io.File("../client_jar/src/network/LuongManh.java").isFile());
            Kiem.dung("client: luong mang bat ca Error (OutOfMemoryError)", sm.contains("} catch (Throwable e2) {")
                    && sm.contains("catch (OutOfMemoryError e)"));
            final String sv = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/io/Session.java")), "UTF-8");
            Kiem.dung("server: xep manh duoi khoa Sender (lien tiep)", sv.contains("synchronized (this) {\n                            this.xepKhung(data);"));
        } catch (Throwable e) {
            e.printStackTrace();
            Kiem.dung("KiemGoiManh loi: " + e, false);
        }
    }

    /**
     * Mot Session that tren socket loopback, key XOR bat. Luong chinh gui goi 26, 4 luong gui 200 goi nho moi luong
     * cung luc. Client doc tung mau <= mau byte, giai XOR, gom. @return {Gom, goi 26 dung?, so goi nho}
     */
    static Object[] quaSocket(int khaNang, final byte[] d26, final int mau) throws Exception {
        ServerSocket ss = new ServerSocket(0, 5, InetAddress.getByName("127.0.0.1"));
        Socket kh = new Socket("127.0.0.1", ss.getLocalPort());
        Socket sv = ss.accept();
        ss.close();
        final Session se = new Session(sv, null, true);
        final byte[] khoa = {7, 91, 33, 120, 5, 66, 18};
        se.setKey(khoa);
        se.khaNang = khaNang;
        se.start();
        for (int i = 0; i < 200 && se.getSender() == null; i++) {
            Thread.sleep(10);
        }
        Thread[] ls = new Thread[4];
        for (int t = 0; t < ls.length; t++) {
            final int tt = t;
            ls[t] = new Thread(new Runnable() {
                public void run() {
                    for (int i = 0; i < 200; i++) {
                        Message m = new Message(4);
                        try {
                            m.dos.writeShort(tt);
                            m.dos.writeShort(i);
                        } catch (Exception e) {
                        }
                        se.sendMessage(m);
                        if (i % 50 == 0) {
                            Thread.yield();
                        }
                    }
                }
            });
        }
        for (Thread t : ls) {
            t.start();
        }
        Message lon = new Message(26);
        lon.dos.write(d26);
        se.sendMessage(lon);
        for (Thread t : ls) {
            t.join();
        }
        // client: doc + giai XOR tung byte (nhu Session_ME.readKey / server writeKey: cung khoa, cung vi tri)
        kh.setSoTimeout(10000);
        final java.io.InputStream raw = kh.getInputStream();
        DataInputStream in = new DataInputStream(new java.io.InputStream() {
            public int read() throws java.io.IOException {
                return raw.read();
            }

            public int read(byte[] b, int o, int n) throws java.io.IOException {
                return raw.read(b, o, Math.min(n, mau));
            }
        });
        int[] vt = {0};
        Gom g = new Gom();
        int nho = 0;
        boolean dung26 = false;
        long het = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < het && (nho < 800 || !dung26)) {
            int c = giai(in.readByte(), khoa, vt);
            int n;
            if (c == -128) {
                c = giai(in.readByte(), khoa, vt);
                n = 0;
                for (int k = 0; k < 4; k++) {
                    n = (n << 8) | (giai(in.readByte(), khoa, vt) & 255);
                }
            } else {
                n = ((giai(in.readByte(), khoa, vt) & 255) << 8) | (giai(in.readByte(), khoa, vt) & 255);
            }
            byte[] d = new byte[n];
            in.readFully(d);
            for (int k = 0; k < n; k++) {
                d[k] = (byte) giai(d[k], khoa, vt);
            }
            int truoc = g.goi.size();
            g.them(c, d);
            for (int k = truoc; k < g.goi.size(); k++) {
                Object[] o = g.goi.get(k);
                int lenh = ((Integer) o[0]).intValue();
                if (lenh == 4) {
                    nho++;
                } else if (lenh == 26) {
                    dung26 = Arrays.equals(d26, (byte[]) o[1]);
                }
            }
        }
        se.disconnect(0);
        kh.close();
        return new Object[]{g, Boolean.valueOf(dung26), Integer.valueOf(nho)};
    }

    static int giai(byte b, byte[] khoa, int[] vt) {
        int r = (byte) ((khoa[vt[0]] & 255) ^ (b & 255));
        vt[0] = (vt[0] + 1) % khoa.length;
        return r;
    }
}
