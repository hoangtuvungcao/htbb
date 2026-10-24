import io.KhoGoi;
import io.Message;
import io.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Set;

/**
 * Kho goi phia client (0.9.15, io.KhoGoi): client cu / khong bao bit nhan DUNG byte nhu truoc; client 915 co kho va
 * khai dung (lenh, bam, do dai) nhan goi -117 nho thay goi goc; goi da doi -> -117 "luu" + goi goc nguyen ven.
 */
public final class KiemKhoGoi {
    private KiemKhoGoi() {
    }

    static Message goi(int lenh, int n, int hat) throws Exception {
        Message m = new Message(lenh);
        java.util.Random r = new java.util.Random(hat);
        byte[] b = new byte[n];
        r.nextBytes(b);
        m.dos.write(b);
        return m;
    }

    static byte[] khai(Object[][] muc) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeByte(muc.length);
        for (Object[] x : muc) {
            o.writeByte((Integer) x[0]);
            o.writeLong((Long) x[1]);
            o.writeInt((Integer) x[2]);
        }
        return bo.toByteArray();
    }

    static long bamDuLieu(Message m) {
        byte[] k = m.toByteArray();
        int dau = k[0] == -128 ? 6 : 3;
        return KhoGoi.bam(k, dau, k.length - dau);
    }

    static int daiDuLieu(Message m) {
        byte[] k = m.toByteArray();
        return k.length - (k[0] == -128 ? 6 : 3);
    }

    public static void chay() {
        Kiem.nhom("Kho goi client (0.9.15, io.KhoGoi)");
        try {
            Kiem.bang("bam FNV-1a 64 chuan ('a')", Long.valueOf(0xaf63dc4c8601ec8cL), Long.valueOf(KhoGoi.bam(new byte[]{'a'}, 0, 1)));
            Session s = new Session(null, null, true);
            Message m26 = goi(26, 150000, 1);
            Message kem = new Message(-91);
            kem.dos.writeByte(7);
            m26.kemTheo = kem;
            byte[] goc = m26.toByteArray();

            // client cu: khong doi gi
            s.banClient = 914;
            s.khaNang = 3;
            Kiem.dung("ban 914: goi giu nguyen (cung doi tuong)", KhoGoi.thayThe(s, m26) == m26);
            s.banClient = 915;
            Kiem.dung("ban 915 khong co kho (khoGoi null): giu nguyen", KhoGoi.thayThe(s, m26) == m26);
            s.khoGoi = new java.util.HashSet<String>();
            Kiem.dung("ban 915 khong bao bit 4: giu nguyen", KhoGoi.thayThe(s, m26) == m26);
            s.banClient = 914;
            s.khaNang = 7;
            Kiem.dung("bit 4 nhung ban 914: giu nguyen", KhoGoi.thayThe(s, m26) == m26);
            s.banClient = 915;
            Message nho = goi(25, 100, 2);
            Kiem.dung("goi nho < nguong: giu nguyen", KhoGoi.thayThe(s, nho) == nho);
            Message khac = goi(27, 5000, 3);
            Kiem.dung("lenh ngoai danh sach (27): giu nguyen", KhoGoi.thayThe(s, khac) == khac);

            // 915 co kho, chua co goi: -117 kieu 0 + goi goc
            Message t = KhoGoi.thayThe(s, m26);
            Kiem.bang("chua co: lenh -117", Integer.valueOf(-117), Integer.valueOf(t.command));
            DataInputStream d = new DataInputStream(new ByteArrayInputStream(t.toByteArray(), 3, t.toByteArray().length - 3));
            Kiem.bang("chua co: kieu 0 (luu)", Integer.valueOf(0), Integer.valueOf(d.readByte()));
            Kiem.bang("chua co: dung lenh", Integer.valueOf(26), Integer.valueOf(d.readByte()));
            long h = d.readLong();
            int dai = d.readInt();
            Kiem.bang("chua co: bam dung du lieu", Long.valueOf(bamDuLieu(m26)), Long.valueOf(h));
            Kiem.bang("chua co: do dai du lieu", Integer.valueOf(150000), Integer.valueOf(dai));
            Kiem.dung("chua co: goi goc kem ngay sau, nguyen byte", t.kemTheo == m26 && java.util.Arrays.equals(goc, m26.toByteArray()) && m26.kemTheo == kem);

            // client khai dung -> chi goi -117 kieu 1, goi kem van di
            byte[] kb = khai(new Object[][]{{26, h, dai}, {25, 123L, 9}});
            Set<String> ds = KhoGoi.docKhai(new DataInputStream(new ByteArrayInputStream(kb)));
            Kiem.bang("doc khai: 2 muc", Integer.valueOf(2), Integer.valueOf(ds.size()));
            s.khoGoi = ds;
            Message u = KhoGoi.thayThe(s, m26);
            byte[] ub = u.toByteArray();
            Kiem.dung("da co: goi -117 kieu 1 thay goi goc (" + ub.length + " B thay " + goc.length + " B)", u.command == -117 && ub[3] == 1 && ub.length < 32);
            Kiem.dung("da co: goi kem -91 van gui", u.kemTheo == kem);
            Kiem.dung("da co: goi goc khong bi sua", m26.kemTheo == kem && java.util.Arrays.equals(goc, m26.toByteArray()));

            // goi da doi (cung lenh, khac du lieu) -> luu lai
            Message moi = goi(26, 150000, 99);
            Message v = KhoGoi.thayThe(s, moi);
            Kiem.dung("goi doi: -117 kieu 0 + goi moi", v.command == -117 && v.toByteArray()[3] == 0 && v.kemTheo == moi);
            Message dai1 = goi(26, 150001, 1);
            Kiem.dung("khac do dai: luu lai", KhoGoi.thayThe(s, dai1).toByteArray()[3] == 0);

            // tat bang sv.khoGoi=0
            KhoGoi.BAT = false;
            Kiem.dung("sv.khoGoi=0: giu nguyen", KhoGoi.thayThe(s, m26) == m26);
            KhoGoi.BAT = true;

            // khai cut ngang / lenh la / rong
            Kiem.bang("khai cut ngang: giu muc da doc", Integer.valueOf(1),
                    Integer.valueOf(KhoGoi.docKhai(new DataInputStream(new ByteArrayInputStream(java.util.Arrays.copyOf(kb, 1 + 13 + 5)))).size()));
            Kiem.bang("khai lenh ngoai danh sach: bo", Integer.valueOf(0),
                    Integer.valueOf(KhoGoi.docKhai(new DataInputStream(new ByteArrayInputStream(khai(new Object[][]{{27, 1L, 9}})))).size()));
            Kiem.bang("khai rong", Integer.valueOf(0), Integer.valueOf(KhoGoi.docKhai(new DataInputStream(new ByteArrayInputStream(new byte[0]))).size()));

            // dang nhap: chi doc khai khi ban >= 915 va bit 4
            String lh = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/real/cmd/LoginHandler.java")), "UTF-8");
            Kiem.dung("LoginHandler doc khai chi khi ban >= 915 + bit kho", lh.contains("banClient >= io.KhoGoi.BAN_TOI_THIEU && (session.khaNang & io.KhoGoi.KHA_NANG_KHO) != 0"));
            String sv = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/io/Session.java")), "UTF-8");
            Kiem.dung("Session.sendMessage thay goi qua KhoGoi", sv.contains("m = KhoGoi.thayThe(this, m);"));
            String cl = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("../client_jar/src/network/KhoGoi.java")), "UTF-8");
            Kiem.dung("client cung LENH -117, bit 4, cung danh sach lenh", cl.contains("LENH = -117") && cl.contains("KHA_NANG_KHO = 4")
                    && cl.contains("LOAI = {25, 26, 1, -108, 105}"));

            // qua socket that: ban 914 nhan dung byte nhu khi tat han kho goi
            byte[] a = quaSocket(914, 3, null, true);
            byte[] b = quaSocket(914, 3, null, false);
            Kiem.dung("socket ban 914: byte giong het khi tat kho (" + a.length + " B)", java.util.Arrays.equals(a, b) && a.length > 150000);
            byte[] c = quaSocket(915, 7, ds, true);
            Kiem.dung("socket ban 915 da co goi: chi vai chuc byte (" + c.length + " B)", c.length < 64);
        } catch (Throwable e) {
            e.printStackTrace();
            Kiem.dung("KiemKhoGoi loi: " + e, false);
        }
    }

    /** Session that tren loopback (khong ma hoa), gui goi 26 + goi kem; tra moi byte client nhan. */
    static byte[] quaSocket(int ban, int khaNang, Set<String> kho, boolean bat) throws Exception {
        KhoGoi.BAT = bat;
        java.net.ServerSocket ss = new java.net.ServerSocket(0, 5, java.net.InetAddress.getByName("127.0.0.1"));
        java.net.Socket kh = new java.net.Socket("127.0.0.1", ss.getLocalPort());
        java.net.Socket svs = ss.accept();
        ss.close();
        Session se = new Session(svs, null, true);
        se.banClient = ban;
        se.khaNang = khaNang & ~1;   // khong nen: so byte goc
        se.khoGoi = kho;
        se.start();
        for (int i = 0; i < 200 && se.getSender() == null; i++) {
            Thread.sleep(10);
        }
        Message m = goi(26, 150000, 1);
        Message kem = new Message(-91);
        kem.dos.writeByte(7);
        m.kemTheo = kem;
        se.sendMessage(m);
        Thread.sleep(400);
        kh.setSoTimeout(500);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] t = new byte[65536];
        try {
            java.io.InputStream in = kh.getInputStream();
            int n;
            while ((n = in.read(t)) > 0) {
                bo.write(t, 0, n);
            }
        } catch (java.net.SocketTimeoutException e) {
            // het du lieu
        }
        se.disconnect();
        kh.close();
        KhoGoi.BAT = true;
        return bo.toByteArray();
    }
}
