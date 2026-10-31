import com.htbb.dangnhap.BeKetNoi;
import com.htbb.dangnhap.CauHinh;
import com.htbb.dangnhap.GiaoThuc;
import com.htbb.dangnhap.GioiHanKetNoi;
import com.htbb.dangnhap.MayChuDangNhap;
import com.htbb.dangnhap.NhatKy;
import com.htbb.dangnhap.XacThuc;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Login server HTBB (com.htbb.dangnhap, thay CheckLoginSocket.jar - docs/dang-nhap.md): giao thuc khop BYTE voi phia
 * game (io.Message / io.LogInController), ngu nghia ket qua giong ban cu, be ket noi, gioi han, han xu ly, tat em,
 * tep trien khai (jar, unit systemd, quay lui). Khong can CSDL: nguon tai khoan gia.
 */
public final class KiemDangNhapServer {

    private KiemDangNhapServer() {
    }

    public static void chay() {
        Kiem.nhom("Login server HTBB (com.htbb.dangnhap): giao thuc, xac thuc, be ket noi, may chu, trien khai");
        try {
            giaoThuc();
            xacThuc();
            beKetNoi();
            cauHinh();
            gioiHan();
            mayChu();
            trienKhai();
            khachGame();
        } catch (final Exception e) {
            Kiem.dung("khong nem ngoai le: " + e, false);
            e.printStackTrace(System.out);
        }
    }

    // ================================================================ giao thuc

    /** Khung yeu cau dung y nhu io.LogInController.OnMessageLogInComming tao bang io.Message. */
    static byte[] khungTuGame(final long phien, final String ten, final String mk) throws IOException {
        final io.Message m = new io.Message(1);
        m.dos.writeLong(phien);
        m.dos.writeUTF(ten);
        m.dos.writeUTF(mk);
        m.dos.writeByte(9);
        return m.toByteArray();
    }

    /** Doc tra loi y nhu phia game: io.Session.readMessage (lenh, u16, du lieu) + LogInController.ReceiveMessage. */
    static Object[] docNhuGame(final byte[] khung) throws IOException {
        final DataInputStream d = new DataInputStream(new ByteArrayInputStream(khung));
        final int lenh = d.readByte();
        final int dai = d.readUnsignedShort();
        final byte[] b = new byte[dai];
        d.readFully(b);
        final io.Message m = new io.Message(lenh, b);
        return new Object[]{lenh, m.dis.readLong(), m.dis.readInt(), m.dis.readBoolean(), m.dis.readByte(), d.available()};
    }

    static void giaoThuc() throws IOException {
        final byte[] k = khungTuGame(123456789012L, "hlvai00024", "matkhau9");
        final GiaoThuc.Khung kh = GiaoThuc.docKhung(new ByteArrayInputStream(k), 4096);
        Kiem.bang("khung game -> login: lenh 1", (byte) 1, kh.lenh);
        final GiaoThuc.YeuCau yc = GiaoThuc.docYeuCau(kh.duLieu);
        Kiem.bang("doc yeu cau: phien", 123456789012L, yc.phien);
        Kiem.bang("doc yeu cau: ten", "hlvai00024", yc.ten);
        Kiem.bang("doc yeu cau: mat khau", "matkhau9", yc.matKhau);
        Kiem.bang("doc yeu cau: ma game 9 (HTBB)", (byte) 9, yc.maGame);
        Kiem.dung("YeuCau.toString KHONG chua mat khau", !yc.toString().contains("matkhau9"));
        Kiem.dung("khungYeuCau (cong cu) trung byte voi io.Message cua game",
                java.util.Arrays.equals(k, GiaoThuc.khungYeuCau(123456789012L, "hlvai00024", "matkhau9", (byte) 9)));

        final byte[] tl = GiaoThuc.khungTraLoi(new GiaoThuc.TraLoi(-7L, 105, true, (byte) 9));
        Kiem.bang("tra loi = 3 + 14 byte (nhu ban cu)", 17, tl.length);
        final Object[] g = docNhuGame(tl);
        Kiem.bang("game doc tra loi: lenh", 1, g[0]);
        Kiem.bang("game doc tra loi: phien", -7L, g[1]);
        Kiem.bang("game doc tra loi: userId", 105, g[2]);
        Kiem.bang("game doc tra loi: vinaphone", true, g[3]);
        Kiem.bang("game doc tra loi: fromgame (bRegion)", (byte) 9, g[4]);
        Kiem.bang("game doc tra loi: khong thua byte", 0, g[5]);
        final Object[] g2 = docNhuGame(GiaoThuc.khungTraLoi(new GiaoThuc.TraLoi(Long.MAX_VALUE, -3, false, (byte) 0)));
        Kiem.bang("tra loi -3 doc lai dung", -3, g2[2]);
        final GiaoThuc.TraLoi t = GiaoThuc.docTraLoi(java.util.Arrays.copyOfRange(tl, 3, tl.length));
        Kiem.bang("docTraLoi (cong cu) khop", 105, t.ketQua);

        final byte[] viet = khungTuGame(1L, "tênviệt", "mậtkhẩu");
        final GiaoThuc.YeuCau yv = GiaoThuc.docYeuCau(GiaoThuc.docKhung(new ByteArrayInputStream(viet), 4096).duLieu);
        Kiem.bang("UTF co dau doc dung (writeUTF/readUTF)", "tênviệt|mậtkhẩu", yv.ten + "|" + yv.matKhau);

        Kiem.dung("luong rong -> null (dong sach)", GiaoThuc.docKhung(new ByteArrayInputStream(new byte[0]), 4096) == null);
        boolean eof = false;
        try {
            GiaoThuc.docKhung(new ByteArrayInputStream(java.util.Arrays.copyOf(k, k.length - 1)), 4096);
        } catch (final EOFException e) {
            eof = true;
        }
        Kiem.dung("khung cut giua chung -> EOFException", eof);
        boolean lon = false;
        try {
            GiaoThuc.docKhung(new ByteArrayInputStream(new byte[]{1, (byte) 0x20, 0}), 4096);
        } catch (final GiaoThuc.KhungQuaLon e) {
            lon = e.doDai == 8192;
        }
        Kiem.dung("khung 8192 byte > tran 4096 -> KhungQuaLon", lon);
        boolean thieu = false;
        try {
            GiaoThuc.docYeuCau(new byte[]{0, 0, 0, 0, 0, 0, 0, 5, 0});
        } catch (final IOException e) {
            thieu = true;
        }
        Kiem.dung("du lieu thieu truong -> IOException", thieu);
        Kiem.bang("docPhien doc 8 byte dau", 5L, GiaoThuc.docPhien(new byte[]{0, 0, 0, 0, 0, 0, 0, 5, 0}));
        Kiem.dung("docPhien < 8 byte -> null", GiaoThuc.docPhien(new byte[]{1, 2}) == null);
        Kiem.bang("dongKhung: u16 do dai big-endian", "1,1,0", "" + GiaoThuc.dongKhung((byte) 1, new byte[256])[0] + ","
                + GiaoThuc.dongKhung((byte) 1, new byte[256])[1] + "," + GiaoThuc.dongKhung((byte) 1, new byte[256])[2]);
    }

    // ================================================================ xac thuc

    /** Nguon gia: ten -> {mk, id, ban, provider, phone, fromgame}. */
    static class NguonGia implements XacThuc.Nguon {
        final Map<String, Object[]> tk = new ConcurrentHashMap<>();
        final Map<String, XacThuc.Token> token = new ConcurrentHashMap<>();
        final AtomicInteger goi = new AtomicInteger();
        volatile long cuoiHanChot;
        volatile SQLException nem;
        volatile RuntimeException nemRt;
        volatile CountDownLatch chan; // != null: moi lan goi cho latch nay
        volatile long ngu;

        void them(final String ten, final String mk, final int id, final int ban, final int provider, final String phone,
                final int fromgame) {
            this.tk.put(ten.toLowerCase(), new Object[]{mk, id, ban, provider, phone, (byte) fromgame});
        }

        private void truoc(final long hanChot) throws SQLException {
            this.goi.incrementAndGet();
            this.cuoiHanChot = hanChot;
            final CountDownLatch c = this.chan;
            if (c != null) {
                try {
                    c.await(10, TimeUnit.SECONDS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (this.ngu > 0) {
                try {
                    Thread.sleep(this.ngu);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (this.nem != null) {
                throw this.nem;
            }
            if (this.nemRt != null) {
                throw this.nemRt;
            }
        }

        @Override
        public XacThuc.TaiKhoan timTaiKhoan(final String ten, final String matKhau, final long hanChot) throws SQLException {
            truoc(hanChot);
            final Object[] r = this.tk.get(ten.toLowerCase()); // collation utf8_general_ci: khong phan biet hoa thuong
            if (r == null || !r[0].equals(matKhau)) {
                return null;
            }
            return new XacThuc.TaiKhoan((Integer) r[1], (Integer) r[2], (Integer) r[3], (String) r[4], (Byte) r[5]);
        }

        @Override
        public XacThuc.Token timToken(final String ten, final String tok, final long hanChot) throws SQLException {
            truoc(hanChot);
            return this.token.get(ten + "|" + tok);
        }
    }

    static void xacThuc() {
        final NguonGia n = new NguonGia();
        n.them("an", "123456", 105, 0, 0, "", 0);
        n.them("khoa1", "123456", 7, 1, 0, "", 0);
        n.them("khoa3", "123456", 8, 3, 0, "", 0);
        n.them("ban2", "123456", 9, 2, 0, "", 0);
        n.them("vina", "123456", 10, 0, 0, "84912345678", 0);
        n.them("vina1", "123456", 11, 0, 1, "84912345678", 0);
        n.them("vina5", "123456", 12, 0, 0, "84129000000", 0);
        n.them("tugame", "123456", 13, 0, 0, "", 9);
        final long[] gio = {1_000_000_000L};
        final XacThuc x = new XacThuc(n, () -> gio[0]);

        Kiem.bang("dung mat khau -> id", 105, x.kiem("an", "123456", (byte) 9).ma);
        Kiem.bang("ten khac hoa thuong (collation) -> id", 105, x.kiem("AN", "123456", (byte) 9).ma);
        Kiem.bang("sai mat khau -> -1", -1, x.kiem("an", "654321", (byte) 9).ma);
        Kiem.bang("khong co tai khoan -> -1", -1, x.kiem("khongco", "123456", (byte) 9).ma);
        Kiem.bang("ban=1 -> -2 (bi khoa)", -2, x.kiem("khoa1", "123456", (byte) 9).ma);
        Kiem.bang("ban=3 -> -5 (chua kich hoat email)", -5, x.kiem("khoa3", "123456", (byte) 9).ma);
        Kiem.bang("ban=2 (khac 1/3) -> van vao (id)", 9, x.kiem("ban2", "123456", (byte) 9).ma);
        Kiem.dung("ban=1 sai mat khau -> -1 (khong lo tai khoan bi khoa)", x.kiem("khoa1", "x", (byte) 9).ma == -1);
        Kiem.bang("vinaphone: provider 0 + dau 8491", true, x.kiem("vina", "123456", (byte) 9).vinaphone);
        Kiem.bang("vinaphone: provider 1 -> false", false, x.kiem("vina1", "123456", (byte) 9).vinaphone);
        Kiem.bang("vinaphone: dau 84129", true, x.kiem("vina5", "123456", (byte) 9).vinaphone);
        Kiem.bang("vinaphone: phone rong -> false", false, x.kiem("an", "123456", (byte) 9).vinaphone);
        Kiem.bang("fromgame tra nguyen cot", (byte) 9, x.kiem("tugame", "123456", (byte) 9).fromgame);
        Kiem.bang("laVinaphone(null) / ngan", false, XacThuc.laVinaphone(null) || XacThuc.laVinaphone("8491"));

        final int truoc = n.goi.get();
        Kiem.bang("ten co dau ' -> -3", -3, x.kiem("a'n", "123456", (byte) 9).ma);
        Kiem.bang("mat khau co dau ' -> -3", -3, x.kiem("an", "1' OR '1'='1", (byte) 9).ma);
        Kiem.bang("ma game 2 / 6 / 15 (game cu) -> -3", "-3-3-3", "" + x.kiem("an", "123456", (byte) 2).ma
                + x.kiem("an", "123456", (byte) 6).ma + x.kiem("an", "123456", (byte) 15).ma);
        Kiem.bang("cac truong hop tren khong cham CSDL", truoc, n.goi.get());
        Kiem.bang("ma game khac 9 (vd 1, 3, 20) van kiem team_user", 105, x.kiem("an", "123456", (byte) 1).ma);

        n.token.put("an|#tok", new XacThuc.Token(105, gio[0] - 3_600_000L));
        n.token.put("an|#cu", new XacThuc.Token(105, gio[0] - 3_601_000L));
        Kiem.bang("token '#' con han (3600 s) -> id", 105, x.kiem("an", "#tok", (byte) 9).ma);
        Kiem.bang("token qua 3600 s -> -1", -1, x.kiem("an", "#cu", (byte) 9).ma);
        Kiem.bang("token khong co -> -3 (giu ban cu)", -3, x.kiem("an", "#khong", (byte) 9).ma);

        n.nem = new SQLException("Table 'usertoken' doesn't exist", "42S02", 1146);
        final XacThuc.KetQua kl = x.kiem("an", "123456", (byte) 9);
        Kiem.bang("loi CSDL -> -3", -3, kl.ma);
        Kiem.dung("loi CSDL co ly do de ghi log", kl.loi != null && kl.loi.contains("SQLException"));
        n.nem = null;
        n.nemRt = new IllegalStateException("x");
        Kiem.bang("RuntimeException trong nguon -> -3 (khong lam chet luong)", -3, x.kiem("an", "123456", (byte) 9).ma);
        n.nemRt = null;
        x.kiem("an", "123456", (byte) 9, 4242L);
        Kiem.bang("han chot truyen xuong nguon", 4242L, n.cuoiHanChot);
        Kiem.bang("loi nhieu dong -> chi dong dau", "Communications link failure",
                new XacThuc(new NguonGia() {
                    @Override
                    public XacThuc.TaiKhoan timTaiKhoan(final String t, final String m, final long h) throws SQLException {
                        throw new SQLException("Communications link failure\n\nThe last packet...", "08S01");
                    }
                }).kiem("a", "b", (byte) 9).loi.replace("CSDL: SQLException: ", ""));
    }

    // ================================================================ be ket noi

    /** Connection gia: dem dong, isValid theo co. */
    static final class KetNoiGia implements InvocationHandler {
        static final AtomicInteger SO = new AtomicInteger();
        final int so = SO.incrementAndGet();
        volatile boolean dong;
        volatile boolean song = true;

        @Override
        public Object invoke(final Object p, final Method m, final Object[] a) {
            switch (m.getName()) {
                case "close":
                    this.dong = true;
                    return null;
                case "isClosed":
                    return this.dong;
                case "isValid":
                    return this.song && !this.dong;
                case "hashCode":
                    return System.identityHashCode(p);
                case "equals":
                    return p == a[0];
                case "toString":
                    return "KetNoiGia#" + this.so;
                default:
                    return null;
            }
        }

        static Connection tao(final List<KetNoiGia> ds) {
            final KetNoiGia h = new KetNoiGia();
            ds.add(h);
            return (Connection) Proxy.newProxyInstance(KiemDangNhapServer.class.getClassLoader(),
                    new Class<?>[]{Connection.class}, h);
        }
    }

    static void beKetNoi() throws Exception {
        final List<KetNoiGia> ds = Collections.synchronizedList(new ArrayList<KetNoiGia>());
        final BeKetNoi be = new BeKetNoi(() -> KetNoiGia.tao(ds), 2, 150, 0, 60_000);
        final BeKetNoi.Muon a = be.muon();
        final BeKetNoi.Muon b = be.muon();
        Kiem.bang("muon 2 = toi da, mo 2 ket noi", 2, be.dangMo());
        final long t0 = System.currentTimeMillis();
        boolean hetCho = false;
        try {
            be.muon();
        } catch (final SQLTimeoutException e) {
            hetCho = true;
        }
        final long cho = System.currentTimeMillis() - t0;
        Kiem.dung("muon thu 3 cho ~150 ms roi SQLTimeoutException (khong 'Limited Connection' ngay, khong treo)",
                hetCho && cho >= 120 && cho < 2000);
        Kiem.bang("dem het cho", 1L, be.soHetCho.get());
        be.tra(a, false);
        final BeKetNoi.Muon c = be.muon();
        Kiem.dung("tra roi muon lai -> dung lai ket noi cu (khong mo moi)", c.ketNoi == a.ketNoi && be.soMoMoi.get() == 2);
        be.tra(c, true);
        Kiem.dung("tra kem 'hong' -> dong ket noi", ((KetNoiGia) Proxy.getInvocationHandler(c.ketNoi)).dong);
        Kiem.bang("ket noi hong khong tinh dang mo", 1, be.dangMo());
        final BeKetNoi.Muon d = be.muon();
        Kiem.dung("sau ket noi hong -> mo ket noi moi", d.ketNoi != c.ketNoi && be.soMoMoi.get() == 3);
        be.tra(b, false);
        be.tra(d, false);
        Kiem.bang("tra het -> dang muon 0", 0, be.dangMuon());

        // ket noi ranh da chet (MariaDB wait_timeout) -> isValid false -> bo, mo moi (kiemSauRanhMs = 0: luon kiem)
        for (final KetNoiGia k : ds) {
            k.song = false;
        }
        final BeKetNoi.Muon e = be.muon();
        Kiem.dung("ket noi ranh da chet bi bo, mo moi", ((KetNoiGia) Proxy.getInvocationHandler(e.ketNoi)).song
                && be.soBoHong.get() >= 2);
        be.tra(e, false);

        final BeKetNoi be2 = new BeKetNoi(() -> KetNoiGia.tao(ds), 1, 100, 3_600_000, 1);
        final BeKetNoi.Muon f = be2.muon();
        be2.tra(f, false);
        Thread.sleep(5);
        final BeKetNoi.Muon g = be2.muon();
        Kiem.dung("ket noi qua tuoi toi da -> dong, mo moi", g.ketNoi != f.ketNoi
                && ((KetNoiGia) Proxy.getInvocationHandler(f.ketNoi)).dong);
        be2.tra(g, false);

        final BeKetNoi be3 = new BeKetNoi(() -> {
            throw new SQLException("Access denied", "28000", 1045);
        }, 1, 100, 0, 60_000);
        boolean nem = false;
        try {
            be3.muon();
        } catch (final SQLException x) {
            nem = true;
        }
        Kiem.dung("mo ket noi loi -> nem SQLException va TRA cho (khong ro cho)", nem && be3.dangMuon() == 0);
        Kiem.bang("muon voi han 0 ms khi day -> het cho ngay", true, hetChoNgay());
        be.close();
        boolean dong = false;
        try {
            be.muon();
        } catch (final SQLException x) {
            dong = true;
        }
        Kiem.dung("be da dong -> muon nem loi", dong);
    }

    static boolean hetChoNgay() throws SQLException {
        final List<KetNoiGia> ds = new ArrayList<>();
        final BeKetNoi be = new BeKetNoi(() -> KetNoiGia.tao(ds), 1, 5000, 0, 60_000);
        final BeKetNoi.Muon a = be.muon();
        final long t0 = System.currentTimeMillis();
        try {
            be.muon(0);
            return false;
        } catch (final SQLTimeoutException e) {
            return System.currentTimeMillis() - t0 < 1000;
        } finally {
            be.tra(a, false);
        }
    }

    // ================================================================ cau hinh, gioi han

    static CauHinh ch(final String... kv) {
        final Properties p = new Properties();
        p.setProperty("port", "0");
        p.setProperty("db.url", "jdbc:mysql://127.0.0.1:3306/account?characterEncoding=UTF-8");
        p.setProperty("db.user", "htbb");
        p.setProperty("db.password", "BiMatLam");
        for (int i = 0; i + 1 < kv.length; i += 2) {
            p.setProperty(kv[i], kv[i + 1]);
        }
        return CauHinh.doc(p);
    }

    static void cauHinh() {
        final CauHinh c = ch("port", "8023");
        Kiem.dung("4 khoa cu du -> khong loi", c.loi.isEmpty());
        Kiem.bang("cong", 8023, c.cong);
        Kiem.bang("mac dinh: 8 ket noi CSDL (= ban cu), 8 luong", "8/8", c.csdlToiDa + "/" + c.luong);
        Kiem.dung("han xu ly < 15 s cho cua game (io.LogInController)", c.hanXuLyMs < 15000);
        Kiem.dung("tom tat KHONG chua mat khau CSDL", !c.tomTat().contains("BiMatLam"));
        final CauHinh c2 = ch("db.url", "jdbc:mysql://h/account?user=x&password=lo&a=1");
        Kiem.dung("tom tat che password= trong db.url", !c2.tomTat().contains("=lo") && c2.tomTat().contains("password=***"));
        final Properties thieu = new Properties();
        thieu.setProperty("port", "abc");
        Kiem.bang("thieu db.* va port hong -> 4 loi", 4, CauHinh.doc(thieu).loi.size());
        final CauHinh k = ch("login.csdl.toiDa", "1000", "login.luong", "0", "login.hanXuLyMs", "x");
        Kiem.bang("kep toiDa <= 64, luong >= 1, so hong -> mac dinh", "64/1/13000", k.csdlToiDa + "/" + k.luong + "/" + k.hanXuLyMs);
        Kiem.bang("login.luong mac dinh = login.csdl.toiDa", 4, ch("login.csdl.toiDa", "4").luong);
        Kiem.bang("ipChoPhep tach dau phay / cach", "[127.0.0.1, 10.0.0.2]", ch("login.ipChoPhep", "127.0.0.1, 10.0.0.2").ipChoPhep.toString());
        Kiem.bang("logMoiLan=0 tat", false, ch("login.logMoiLan", "0").logMoiLan);
        Kiem.bang("NhatKy.sach: xuong dong gia mao bi thay", "a?b?c", NhatKy.sach("a\nb\rc"));
        Kiem.bang("NhatKy.ten: cat 40 ky tu", 41, NhatKy.ten(new String(new char[60]).replace('\0', 'x')).length());
    }

    static void gioiHan() {
        final GioiHanKetNoi g = new GioiHanKetNoi(Collections.<String>emptyList(), 3, 2, 0);
        Kiem.dung("ket noi 1, 2 tu cung IP -> nhan", g.nhan("1.1.1.1") == null && g.nhan("1.1.1.1") == null);
        Kiem.dung("ket noi 3 tu cung IP (moiIp=2) -> tu choi", g.nhan("1.1.1.1") != null);
        Kiem.dung("IP khac -> nhan", g.nhan("2.2.2.2") == null);
        Kiem.dung("tong 3 -> IP thu ba tu choi", g.nhan("3.3.3.3") != null);
        g.nha("1.1.1.1");
        Kiem.dung("nha mot -> nhan lai duoc", g.nhan("3.3.3.3") == null);
        Kiem.bang("dem ket noi", 3, g.soKetNoi());
        final GioiHanKetNoi ds = new GioiHanKetNoi(Collections.singletonList("127.0.0.1"), 10, 10, 0);
        Kiem.dung("ipChoPhep: IP ngoai danh sach bi tu choi", ds.nhan("10.0.0.9") != null && ds.nhan("127.0.0.1") == null);
        final GioiHanKetNoi x = new GioiHanKetNoi(Collections.<String>emptyList(), 10, 10, 5);
        int qua = 0;
        for (int i = 0; i < 10; i++) {
            if (x.choYeuCau("1.1.1.1", 1000L)) {
                qua++;
            }
        }
        Kiem.bang("xo token 5/s: dot 10 yeu cau cung luc -> qua 5", 5, qua);
        Kiem.dung("sau 1 giay nap lai", x.choYeuCau("1.1.1.1", 2000L));
        Kiem.dung("0 = tat gioi han yeu cau", g.choYeuCau("1.1.1.1", 0L));
    }

    // ================================================================ may chu that tren cong ngau nhien

    static final class Khach implements AutoCloseable {
        final Socket s;
        final InputStream vao;
        final OutputStream ra;

        Khach(final int cong) throws IOException {
            this.s = new Socket("127.0.0.1", cong);
            this.s.setSoTimeout(5000);
            this.vao = new java.io.BufferedInputStream(this.s.getInputStream());
            this.ra = this.s.getOutputStream();
        }

        void gui(final long phien, final String ten, final String mk) throws IOException {
            this.ra.write(khungTuGame(phien, ten, mk));
            this.ra.flush();
        }

        GiaoThuc.TraLoi doc() throws IOException {
            final GiaoThuc.Khung k = GiaoThuc.docKhung(this.vao, 4096);
            return k == null ? null : GiaoThuc.docTraLoi(k.duLieu);
        }

        @Override
        public void close() throws IOException {
            this.s.close();
        }
    }

    static MayChuDangNhap bat(final NguonGia n, final String... kv) throws IOException {
        final String[] them = new String[kv.length + 2];
        them[0] = "login.nhipGiay";
        them[1] = "0";
        System.arraycopy(kv, 0, them, 2, kv.length);
        final MayChuDangNhap m = new MayChuDangNhap(ch(them), new XacThuc(n), null);
        m.batDau();
        return m;
    }

    static void mayChu() throws Exception {
        final NguonGia n = new NguonGia();
        for (int i = 1; i <= 300; i++) {
            n.them("tk" + i, "123456", 1000 + i, 0, 0, "", 0);
        }
        n.them("khoa", "123456", 5, 1, 0, "", 0);
        // log may chu vao bo dem (khong lam roi output bo kiem)
        final java.io.ByteArrayOutputStream log = new java.io.ByteArrayOutputStream();
        final Method datRa = NhatKy.class.getDeclaredMethod("datRa", java.io.PrintStream.class);
        datRa.setAccessible(true);
        datRa.invoke(null, new java.io.PrintStream(log, true, "UTF-8"));

        MayChuDangNhap m = bat(n);
        try (Khach k = new Khach(m.cong())) {
            for (int i = 1; i <= 300; i++) {
                k.gui(i, "tk" + i, i % 10 == 0 ? "sai" : "123456");
            }
            final Map<Long, Integer> kq = new HashMap<>();
            for (int i = 0; i < 300; i++) {
                final GiaoThuc.TraLoi t = k.doc();
                kq.put(t.phien, t.ketQua);
            }
            int dung = 0;
            for (int i = 1; i <= 300; i++) {
                final Integer v = kq.get((long) i);
                if (v != null && v == (i % 10 == 0 ? -1 : 1000 + i)) {
                    dung++;
                }
            }
            Kiem.bang("300 yeu cau tren MOT ket noi (nhu game): moi phien nhan dung ket qua", 300, dung);
            // lenh la: bo qua, khong tra loi; ket noi van song
            k.ra.write(GiaoThuc.dongKhung((byte) 7, new byte[]{1, 2, 3}));
            k.gui(901, "khoa", "123456");
            final GiaoThuc.TraLoi t = k.doc();
            Kiem.dung("lenh la bi bo qua (khong tra loi), yeu cau sau van duoc tra: ban=1 -> -2",
                    t.phien == 901 && t.ketQua == -2);
            // du lieu hong nhung co phien -> -3 cho dung phien
            k.ra.write(GiaoThuc.dongKhung((byte) 1, new byte[]{0, 0, 0, 0, 0, 0, 3, 120, 0, 9}));
            k.ra.flush();
            final GiaoThuc.TraLoi h = k.doc();
            Kiem.dung("goi kiem hong (co phien) -> -3 cho phien do", h.phien == 888 && h.ketQua == -3);
            // khung qua lon -> dong ket noi
            k.ra.write(new byte[]{1, (byte) 0x40, 0});
            k.ra.flush();
            boolean dong;
            try {
                dong = k.doc() == null;
            } catch (final IOException e) {
                dong = !(e instanceof SocketTimeoutException);
            }
            Kiem.dung("khung 16384 byte > login.khungToiDa -> cat ket noi", dong);
        }
        final String nhip = m.dongNhip();
        Kiem.dung("[NHIP] dem 301 yeu cau (goi hong khong tinh), co do tre + hang doi + ket noi: " + nhip,
                nhip.contains("yc=301") && nhip.contains("khungHong=2") && nhip.contains("tre p50=") && nhip.contains("hang=0/"));
        m.dung(1000);

        // hang doi day -> -3 ngay (khong treo)
        final NguonGia cham = new NguonGia();
        cham.them("a", "1", 1, 0, 0, "", 0);
        cham.chan = new CountDownLatch(1);
        m = bat(cham, "login.luong", "1", "login.hangDoi.toiDa", "2");
        try (Khach k = new Khach(m.cong())) {
            for (int i = 1; i <= 6; i++) {
                k.gui(i, "a", "1");
            }
            final Set<Long> tuChoi = new HashSet<>();
            for (int i = 0; i < 3; i++) {
                final GiaoThuc.TraLoi t = k.doc();
                if (t.ketQua == -3) {
                    tuChoi.add(t.phien);
                }
            }
            Kiem.bang("luong 1 + hang 2 dang ban: 3 yeu cau thua -> -3 NGAY (backpressure)", "[4, 5, 6]",
                    new java.util.TreeSet<>(tuChoi).toString());
            cham.chan.countDown();
            final Set<Long> ok = new HashSet<>();
            for (int i = 0; i < 3; i++) {
                ok.add(k.doc().phien);
            }
            Kiem.bang("3 yeu cau da nhan van duoc xu ly xong", "[1, 2, 3]", new java.util.TreeSet<>(ok).toString());
            Kiem.bang("dem tu choi", 3L, m.thongKe().tongTuChoi.get());
        }
        m.dung(1000);

        // han xu ly: CSDL treo -> -3 dung han (truoc 15 s cua game), viec xong muon bi bo
        final NguonGia treo = new NguonGia();
        treo.them("a", "1", 1, 0, 0, "", 0);
        treo.ngu = 1500;
        m = bat(treo, "login.hanXuLyMs", "500", "login.luong", "1");
        try (Khach k = new Khach(m.cong())) {
            final long t0 = System.currentTimeMillis();
            k.gui(1, "a", "1");
            k.gui(2, "a", "1");
            final GiaoThuc.TraLoi a = k.doc();
            final GiaoThuc.TraLoi b = k.doc();
            final long tre = System.currentTimeMillis() - t0;
            Kiem.dung("CSDL cham 1.5 s, han 0.5 s: ca hai (dang truy van + con trong hang) nhan -3 sau ~0.5 s",
                    a.ketQua == -3 && b.ketQua == -3 && tre >= 400 && tre < 1400);
            Thread.sleep(2000);
            k.s.setSoTimeout(300);
            boolean thua = false;
            try {
                thua = k.doc() != null;
            } catch (final SocketTimeoutException e) {
                thua = false;
            }
            Kiem.sai("viec xong sau han KHONG gui tra loi thu hai cho cung phien", thua);
            Kiem.dung("yeu cau con trong hang qua han khong ton truy van", treo.goi.get() == 1);
        }
        m.dung(1000);

        // gioi han ket noi moi IP
        m = bat(n, "login.ketNoi.moiIp", "1");
        try (Khach k1 = new Khach(m.cong()); Khach k2 = new Khach(m.cong())) {
            boolean bi = false;
            try {
                k2.gui(1, "tk1", "123456");
                bi = k2.doc() == null;
            } catch (final IOException e) {
                bi = true;
            }
            k1.gui(2, "tk2", "123456");
            Kiem.dung("ket noi thu 2 tu 127.0.0.1 (moiIp=1) bi dong; ket noi 1 van chay", bi && k1.doc().ketQua == 1002);
        }
        m.dung(1000);

        // tat em: yeu cau dang xu ly van duoc tra loi, sau do cong dong
        final NguonGia tat = new NguonGia();
        tat.them("a", "1", 77, 0, 0, "", 0);
        tat.ngu = 400;
        m = bat(tat);
        final int cong = m.cong();
        try (Khach k = new Khach(cong)) {
            k.gui(5, "a", "1");
            Thread.sleep(100);
            final MayChuDangNhap mm = m;
            final Thread t = new Thread(() -> mm.dung(3000));
            t.start();
            final GiaoThuc.TraLoi r = k.doc();
            Kiem.dung("tat em: yeu cau dang chay van nhan ket qua that (77)", r != null && r.ketQua == 77);
            t.join(5000);
        }
        boolean congDong;
        try (Socket s = new Socket("127.0.0.1", cong)) {
            congDong = false;
        } catch (final IOException e) {
            congDong = true;
        }
        Kiem.dung("sau khi tat: cong khong con nghe", congDong);
        NhatKy.xa(3000L);
        final String l = new String(log.toByteArray(), StandardCharsets.UTF_8);
        Kiem.dung("log co dong [KHOI_DONG] / [DANG_NHAP] / [TAT] dang 'yyyy-MM-dd HH:mm:ss.SSS I [LOAI]'",
                l.matches("(?s).*\\d{4}-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d{3} I \\[KHOI_DONG\\].*")
                        && l.contains("I [DANG_NHAP] tk1 kq=1001") && l.contains("I [TAT] da tat"));
        Kiem.dung("log KHONG chua mat khau (yeu cau / CSDL)", !l.contains("123456") && !l.contains("BiMatLam"));
        datRa.invoke(null, new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out), true, "UTF-8"));
    }

    // ================================================================ trien khai

    static String doc(final String duong) throws IOException {
        return new String(Files.readAllBytes(new File(duong).toPath()), StandardCharsets.UTF_8);
    }

    static void trienKhai() throws IOException {
        for (final File f : new File("src/com/htbb/dangnhap").listFiles()) {
            Kiem.dung("ma nguon login server khong co chu NQSH: " + f.getName(), !doc(f.getPath()).toUpperCase().contains("NQSH"));
        }
        try (JarFile j = new JarFile("loginServer/htbb-login.jar")) {
            Kiem.bang("htbb-login.jar: Main-Class", "com.htbb.dangnhap.MayChuDangNhap",
                    j.getManifest().getMainAttributes().getValue("Main-Class"));
            Kiem.dung("htbb-login.jar dong goi kem Connector/J (chay doc lap nhu jar cu)",
                    j.getEntry("com/mysql/jdbc/Driver.class") != null && j.getEntry("META-INF/services/java.sql.Driver") != null);
            boolean du = true;
            for (final File f : new File("src/com/htbb/dangnhap").listFiles()) {
                final String lop = "com/htbb/dangnhap/" + f.getName().replace(".java", ".class");
                du &= j.getEntry(lop) != null;
            }
            Kiem.dung("htbb-login.jar co moi lop cua src/com/htbb/dangnhap (build lai sau khi sua: sh loginServer/build.sh)", du);
            boolean nqsh = false;
            for (final java.util.Enumeration<JarEntry> e = j.entries(); e.hasMoreElements(); ) {
                final JarEntry x = e.nextElement();
                if (x.getName().startsWith("com/htbb/")) {
                    try (InputStream i = j.getInputStream(x)) {
                        final byte[] b = new byte[(int) x.getSize()];
                        new DataInputStream(i).readFully(b);
                        nqsh |= new String(b, StandardCharsets.ISO_8859_1).toUpperCase().contains("NQSH");
                    }
                }
            }
            Kiem.sai("htbb-login.jar khong co chu NQSH", nqsh);
        }
        Kiem.dung("CheckLoginSocket.jar (ban cu) van con de quay lui", new File("loginServer/CheckLoginSocket.jar").isFile());
        for (final String u : new String[]{"../vps/htbb-login.service", "../vps/staging/htbb-staging-login.service"}) {
            final String s = doc(u);
            Kiem.dung(u + ": mac dinh htbb-login.jar", s.contains("Environment=HTBB_LOGIN_JAR=htbb-login.jar"));
            Kiem.dung(u + ": doc tep quay lui tuy chon (EnvironmentFile=-...)", s.matches("(?s).*EnvironmentFile=-/etc/htbb/[a-z-]+\\.env.*"));
            Kiem.dung(u + ": ExecStart chay ${HTBB_LOGIN_JAR}", s.matches("(?s).*\\nExecStart=[^\\n]*-jar \\$\\{HTBB_LOGIN_JAR\\}.*"));
            Kiem.dung(u + ": EnvironmentFile dat SAU Environment (tep quay lui ghi de mac dinh)",
                    s.indexOf("EnvironmentFile=-") > s.indexOf("Environment=HTBB_LOGIN_JAR"));
        }
    }

    // ================================================================ phia game: KhachDangNhap (io.LogInController)

    /** Ghi lai moi tra loi / het han cua khach. */
    static final class NgheGhi implements com.htbb.dangnhap.KhachDangNhap.Nghe {
        final ConcurrentHashMap<Long, AtomicInteger> soLan = new ConcurrentHashMap<>();
        final ConcurrentHashMap<Long, Integer> kq = new ConcurrentHashMap<>();
        final ConcurrentHashMap<Long, Long> luc = new ConcurrentHashMap<>();
        final Set<Long> hetHan = ConcurrentHashMap.newKeySet();

        @Override
        public void traLoi(final long phien, final int ketQua, final boolean v, final byte r) {
            this.soLan.computeIfAbsent(phien, x -> new AtomicInteger()).incrementAndGet();
            this.kq.put(phien, ketQua);
            this.luc.put(phien, System.currentTimeMillis());
        }

        @Override
        public void hetHan(final long phien, final long cho, final String t) {
            this.soLan.computeIfAbsent(phien, x -> new AtomicInteger()).incrementAndGet();
            this.hetHan.add(phien);
        }

        @Override
        public void suKien(final String chu) {
        }

        boolean moiPhienMotLan() {
            for (final AtomicInteger a : this.soLan.values()) {
                if (a.get() != 1) {
                    return false;
                }
            }
            return true;
        }
    }

    static void doiDu(final NgheGhi n, final int so, final long ms) throws InterruptedException {
        final long het = System.currentTimeMillis() + ms;
        while (n.soLan.size() < so && System.currentTimeMillis() < het) {
            Thread.sleep(20);
        }
    }

    static void khachGame() throws Exception {
        final java.io.ByteArrayOutputStream log = new java.io.ByteArrayOutputStream();
        final Method datRa = NhatKy.class.getDeclaredMethod("datRa", java.io.PrintStream.class);
        datRa.setAccessible(true);
        datRa.invoke(null, new java.io.PrintStream(log, true, "UTF-8"));
        try {
            final NguonGia ng = new NguonGia();
            for (int i = 1; i <= 200; i++) {
                ng.them("kh" + i, "1", 5000 + i, 0, 0, "", 0);
            }
            ng.ngu = 15; // CSDL hoi cham: luc tat dot ngot van con yeu cau trong hang chua tra loi
            MayChuDangNhap m = bat(ng, "login.luong", "1");
            final int cong = m.cong();
            final NgheGhi nghe = new NgheGhi();
            final com.htbb.dangnhap.KhachDangNhap k = new com.htbb.dangnhap.KhachDangNhap(
                    new com.htbb.dangnhap.KhachDangNhap.DiaChi() {
                        @Override
                        public String host() {
                            return "127.0.0.1";
                        }

                        @Override
                        public int cong() {
                            return cong;
                        }
                    }, nghe, 15000L);
            k.batDau();
            final long t0 = System.currentTimeMillis();
            final Map<Long, Long> guiLuc = new ConcurrentHashMap<>();
            for (int i = 1; i <= 200; i++) {
                guiLuc.put((long) i, System.currentTimeMillis());
                k.gui(i, "kh" + i, "1", (byte) 9);
                Thread.sleep(5);
                if (i == 100) {
                    m.dung(0); // login server "chet" giua dot: yeu cau trong hang + dang gui bi bo, ket noi dut
                    Thread.sleep(300);
                    m = bat(ng, "login.luong", "1", "port", String.valueOf(cong)); // bat lai cung cong
                }
            }
            doiDu(nghe, 200, 20000);
            long treMax = 0;
            int dung = 0;
            for (int i = 1; i <= 200; i++) {
                final Long l = nghe.luc.get((long) i);
                if (l != null) {
                    treMax = Math.max(treMax, l - guiLuc.get((long) i));
                }
                final Integer v = nghe.kq.get((long) i);
                if (v != null && v == 5000 + i) {
                    dung++;
                }
            }
            Kiem.bang("login server khoi dong lai giua dot 200 dang nhap: ca 200 nhan DUNG ket qua", 200, dung);
            Kiem.dung("khong yeu cau nao het han 15 s", nghe.hetHan.isEmpty());
            Kiem.dung("moi phien tra loi DUNG MOT lan (khong tra loi doi)", nghe.moiPhienMotLan() && nghe.soLan.size() == 200);
            Kiem.dung("tre lon nhat chi bang thoi gian noi lai (< 5 s, khong phai 15 s): " + treMax + " ms", treMax < 5000);
            Kiem.dung("da noi lai va gui lai yeu cau dang cho: noiLai=" + k.soNoiLai.get() + " guiLai=" + k.soGuiLai.get(),
                    k.soNoiLai.get() >= 2 && k.soGuiLai.get() > 0);
            Kiem.bang("khong con yeu cau treo", 0, k.soDangCho());

            // yeu cau dau tien SAU khi login khoi dong lai luc dang ranh (loi cu: ghi vao socket chet -> mat, cho 15 s)
            m.dung(0);
            Thread.sleep(300);
            m = bat(ng, "port", String.valueOf(cong));
            final long t1 = System.currentTimeMillis();
            k.gui(1001, "kh1", "1", (byte) 9);
            doiDu(nghe, 201, 20000);
            Kiem.dung("yeu cau dau tien sau khi login khoi dong lai (luc ranh) duoc tra loi nhanh: "
                    + (System.currentTimeMillis() - t1) + " ms", Integer.valueOf(5001).equals(nghe.kq.get(1001L))
                    && System.currentTimeMillis() - t1 < 5000);
            m.dung(0);
            k.dung();

            // login server CHET HAN: het han dung choMs, bat lai sau do thi KHONG tra loi them lan nua
            final NgheGhi n2 = new NgheGhi();
            final int cong2 = cong;
            final com.htbb.dangnhap.KhachDangNhap k2 = new com.htbb.dangnhap.KhachDangNhap(
                    new com.htbb.dangnhap.KhachDangNhap.DiaChi() {
                        @Override
                        public String host() {
                            return "127.0.0.1";
                        }

                        @Override
                        public int cong() {
                            return cong2;
                        }
                    }, n2, 1200L);
            k2.batDau();
            k2.gui(1, "kh1", "1", (byte) 9);
            k2.gui(2, "kh2", "1", (byte) 9);
            doiDu(n2, 2, 5000);
            Kiem.dung("login server khong len: het han sau choMs (1.2 s) -> bao nguoi choi thu lai", n2.hetHan.size() == 2);
            m = bat(ng, "port", String.valueOf(cong));
            Thread.sleep(2500);
            Kiem.dung("login len lai sau het han: yeu cau da het han KHONG gui lai, khong tra loi lan hai",
                    n2.moiPhienMotLan() && n2.kq.isEmpty());
            k2.dung();
            m.dung(0);
            Kiem.dung("thoi gian chay phan khach < 30 s", System.currentTimeMillis() - t0 < 30000);
        } finally {
            NhatKy.xa(2000L);
            datRa.invoke(null, new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out), true, "UTF-8"));
        }
    }
}
