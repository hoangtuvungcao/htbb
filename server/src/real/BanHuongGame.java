package real;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.BanHuong;
import data.Database;
import data.NghiVanGianLan;
import io.Message;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * NGAM TAY TRONG PvP (0.9.8) - phan GAME cua com.htbb.pokemon.BanHuong.
 *
 * <p>Luat (chu game 22/10/2026): trong moi tran giua nguoi choi (co PK bat / ten do, vua danh hoac bi nguoi choi danh
 * trong {@link #K_GIAY} giay, thach dau 204/205, dau xep hang 202, loi dai 227, chien truong, giai Lien minh, chiem
 * Gym / chien thanh 201, + {@link #K_MAP} va ngu canh dang ky them qua {@link #dangKyNguCanh}):
 * <ul>
 * <li>Tu dong danh bi tu choi / dung ngay (real.TuDongGame goi {@link #lyDoCamTuDong}) - khong phu thuoc
 * tu_dong.tat_khi_pvp.</li>
 * <li>Danh KHOA MUC TIEU vao nguoi choi (lenh 6) bi tu choi voi client &gt;= 908; client cu (&lt;= 907) khong ngam duoc:
 * lenh 6 cua no doi thanh phat ban theo HUONG NHAN VAT DANG QUAY (4 phia, khong khoa muc tieu) + nhac cap nhat 0.9.8.</li>
 * <li>Sat thuong PvP chi con tu phat ban theo huong (lenh {@link BanHuong#LENH}): may chu mo phong duong dan (dan / xuyen /
 * non / vung, tam / be rong / toc do theo data_chieu_ban), tuong chan, bu tre 150 ms, roi gay sat thuong QUA DUONG DANH
 * CU (Map.doAttackPlayer / doAttackMonster: moi luat PK, to doi, bang, chenh cap, giap, chi mang... giu nguyen).</li>
 * <li>Buff / chieu ban than: duong cu (lenh 51), tuc thi.</li>
 * <li>PvE: khong doi, tru khi nguoi choi bat "Ngam thu cong" (client gui lenh ban ca khi danh quai).</li>
 * </ul>
 *
 * <p>Diem goi: MapXuLyLenh.processMessage dau tien ({@link #xuLy}), CharCapNhat.doPlayerMove ({@link #ghiViTri}),
 * CharVongLap.update ({@link #nhip}), TuDongGame.xuLy / kiemDangChay / kiemLenh ({@link #lyDoCamTuDong}),
 * NoiDungLauDai.sauKhiNapTemplate ({@link #nap}).
 */
public final class BanHuongGame {

    private BanHuongGame() {
    }

    public static final String K_BAT = "pvp.ngam_tay";
    public static final String K_GIAY = "pvp.ngam_tay.giay_giao_chien";
    public static final String K_BU_TRE = "pvp.ngam_tay.bu_tre_ms";
    public static final String K_MAP = "pvp.ngam_tay.map";

    public static final int GIAY_MAC_DINH = 10;
    public static final int[] MAP_MAC_DINH = {202, 204, 205, 227};

    /** Lenh noi bo cua lan gay sat thuong (khong bao gio la lenh client: lenh client la byte). */
    static final int LENH_DANH_NGUOI = 6;
    static final int LENH_DANH_QUAI = 9;

    public static final String BAO_KEO = "Trận đấu giữa người chơi: không tự nhắm. Kéo chiêu để ngắm, thả tay để bắn.";
    public static final String BAO_CAP_NHAT = "Bản 0.9.8 mới kéo chiêu ngắm được trong PvP. Bản cũ chỉ bắn thẳng theo hướng Pokémon đang quay - hãy cập nhật.";

    // ---- chi so (trang quan tri / log) ----
    public static final AtomicLong SO_PHAT = new AtomicLong();
    public static final AtomicLong SO_TRUNG = new AtomicLong();
    public static final AtomicLong SO_TRUOT = new AtomicLong();
    public static final AtomicLong SO_KHOA_MUC_TIEU_CHAN = new AtomicLong();
    public static final AtomicLong SO_CLIENT_CU = new AtomicLong();
    public static final AtomicLong SO_TU_CHOI = new AtomicLong();

    // ================================================================== cau hinh

    public static boolean bat() {
        return !"0".equals(CauHinhTrungTam.chuoi(K_BAT).trim());
    }

    static long giayGiaoChienMs() {
        return Math.max(0L, Math.min(600L, CauHinhTrungTam.so(K_GIAY, GIAY_MAC_DINH))) * 1000L;
    }

    static long buTre() {
        return Math.max(0L, Math.min(BanHuong.BU_TRE_TOI_DA_MS, CauHinhTrungTam.so(K_BU_TRE, BanHuong.BU_TRE_MS)));
    }

    static boolean trongDsMap(final int id) {
        final String s = CauHinhTrungTam.chuoi(K_MAP);
        if (s == null || s.trim().isEmpty()) {
            for (final int m : MAP_MAC_DINH) {
                if (m == id) {
                    return true;
                }
            }
            return false;
        }
        for (final String x : s.split(",")) {
            try {
                if (Integer.parseInt(x.trim()) == id) {
                    return true;
                }
            } catch (final NumberFormatException e) {
                // bo
            }
        }
        return false;
    }

    // ================================================================== thong so chieu (data_chieu_ban)

    /** lop * 100 + chieu -&gt; thong so (tu CSDL). Rong = dung mac dinh BanHuong.macDinh. */
    static volatile java.util.Map<Integer, BanHuong.ThongSo> BANG = Collections.emptyMap();

    public static void datBang(final java.util.Map<Integer, BanHuong.ThongSo> b) {
        BANG = b == null ? Collections.<Integer, BanHuong.ThongSo>emptyMap()
                : Collections.unmodifiableMap(new HashMap<Integer, BanHuong.ThongSo>(b));
    }

    static int tamGoc(final int lop, final int chieu) {
        try {
            return CharManager.SKILL_RANGE[lop][chieu];
        } catch (final Exception e) {
            return 64;
        }
    }

    public static BanHuong.ThongSo thongSo(final int lop, final int chieu) {
        final BanHuong.ThongSo t = BANG.get(Integer.valueOf(lop * 100 + chieu));
        final int tg = tamGoc(lop, chieu);
        if (t == null) {
            return BanHuong.macDinh(lop, chieu, tg);
        }
        // tam ban khong vuot tam cu (duong danh cu kiem hop vuong canh SKILL_RANGE): khong thi trung ma khong gay sat thuong
        return t.tam <= tg ? t : new BanHuong.ThongSo(t.kieu, tg, t.rong, t.tocDo, t.goc, t.toiDa);
    }

    static final String SQL_TAO = "CREATE TABLE IF NOT EXISTS `data_chieu_ban` ("
            + "`lop` TINYINT NOT NULL, `chieu` TINYINT NOT NULL, `kieu` VARCHAR(10) NOT NULL DEFAULT 'dan', "
            + "`tam` SMALLINT NOT NULL DEFAULT 0, `be_rong` SMALLINT NOT NULL DEFAULT 20, `toc_do` SMALLINT NOT NULL DEFAULT 0, "
            + "`goc` SMALLINT NOT NULL DEFAULT 60, `toi_da` TINYINT NOT NULL DEFAULT 1, `ghi_chu` VARCHAR(120) NOT NULL DEFAULT '', "
            + "PRIMARY KEY (`lop`, `chieu`)) ENGINE=InnoDB DEFAULT CHARSET=utf8";

    /** Ten kieu trong CSDL -&gt; ma. Sai = dan. */
    public static int kieuTuTen(final String s) {
        if (s == null) {
            return BanHuong.K_DAN;
        }
        final String k = s.trim().toLowerCase();
        for (int i = 0; i < BanHuong.TEN_KIEU.length; i++) {
            if (BanHuong.TEN_KIEU[i].equals(k)) {
                return i;
            }
        }
        if ("duong_thang".equals(k) || "dan_bay".equals(k)) {
            return BanHuong.K_DAN;
        }
        if ("tia".equals(k)) {
            return BanHuong.K_XUYEN;
        }
        if ("non".equals(k) || "hinh_non".equals(k)) {
            return BanHuong.K_NON;
        }
        if ("vung_tron".equals(k)) {
            return BanHuong.K_VUNG;
        }
        return BanHuong.K_DAN;
    }

    /** NoiDungLauDai.sauKhiNapTemplate: nap data_chieu_ban (tao bang neu thieu; tam 0 = tam goc cua chieu). */
    public static void nap() {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            st = conn.createStatement();
            st.execute(SQL_TAO);
            rs = st.executeQuery("SELECT lop, chieu, kieu, tam, be_rong, toc_do, goc, toi_da FROM data_chieu_ban");
            final java.util.Map<Integer, BanHuong.ThongSo> b = new HashMap<Integer, BanHuong.ThongSo>();
            while (rs.next()) {
                final int lop = rs.getInt(1), chieu = rs.getInt(2);
                final int tam = rs.getInt(4);
                b.put(Integer.valueOf(lop * 100 + chieu), new BanHuong.ThongSo(kieuTuTen(rs.getString(3)),
                        tam <= 0 ? tamGoc(lop, chieu) : tam, rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8)));
            }
            datBang(b);
            System.out.println("[NGAM TAY] " + b.size() + " dong data_chieu_ban (con lai dung mac dinh)");
        } catch (final Exception e) {
            System.out.println("[NGAM TAY] LOI nap data_chieu_ban (dung mac dinh): " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (final Exception ignored) {
            }
            try {
                if (st != null) {
                    st.close();
                }
            } catch (final Exception ignored) {
            }
            try {
                if (conn != null) {
                    Database.connPool.free(conn);
                }
            } catch (final Exception ignored) {
            }
        }
    }

    // ================================================================== trang thai tung nhan vat

    static final class TrangThai {
        final BanHuong.LichSu ls = new BanHuong.LichSu();
        final BanHuong.ChongSpam cs = new BanHuong.ChongSpam();
        volatile long pvpLuc;
        volatile int cheDoDaGui = -1;
        volatile int lopDaGui = -1;
        volatile long baoKeoLuc;
        volatile long baoCuLuc;
        volatile long nghiVanLuc;
        volatile boolean ngamTayPve;
    }

    private static final WeakHashMap<Char, TrangThai> TT = new WeakHashMap<Char, TrangThai>();

    static TrangThai tt(final Char p) {
        synchronized (TT) {
            TrangThai t = TT.get(p);
            if (t == null) {
                t = new TrangThai();
                TT.put(p, t);
            }
            return t;
        }
    }

    static TrangThai ttNeuCo(final Char p) {
        synchronized (TT) {
            return TT.get(p);
        }
    }

    public static BanHuong.LichSu lichSu(final Char p) {
        return tt(p).ls;
    }

    /** CharCapNhat.doPlayerMove sau khi nhan vi tri moi. */
    public static void ghiViTri(final Char p, final long bay) {
        if (p == null || p.isBot != -1) {
            return;
        }
        try {
            tt(p).ls.ghi(bay, p.x, p.y);
        } catch (final Exception e) {
            // khong lam hong di chuyen
        }
    }

    /** Hai nguoi choi vua giao chien (trung don ban / co gang khoa muc tieu). */
    public static void ghiGiaoChien(final Char a, final Char b, final long bay) {
        if (a != null && a.isBot == -1) {
            tt(a).pvpLuc = bay;
        }
        if (b != null && b.isBot == -1) {
            tt(b).pvpLuc = bay;
        }
    }

    // ================================================================== ngu canh PvP

    private static final CopyOnWriteArrayList<Predicate<Char>> NGU_CANH_THEM = new CopyOnWriteArrayList<Predicate<Char>>();

    /** Nhanh khac (vd cup tuan) dang ky them ngu canh PvP: tra true = dang trong tran giua nguoi choi. */
    public static void dangKyNguCanh(final Predicate<Char> ham) {
        if (ham != null) {
            NGU_CANH_THEM.addIfAbsent(ham);
        }
    }

    static boolean laMapPvp(final Map m, final Char p) {
        if (m == null) {
            return false;
        }
        if (trongDsMap(m.mapId) || (m.mapIDLoadMap != m.mapId && trongDsMap(m.mapIDLoadMap))) {
            return true;
        }
        if (m instanceof MapLoiDai || m instanceof MapLienDau || m instanceof MapChienTruongMoba || m instanceof ArenaMap) {
            return true;
        }
        try {
            if (m.isMapLoiDai() || m.isMapChienTruongMoba()) {
                return true;
            }
        } catch (final Exception ignored) {
        }
        // chien thanh / chiem Gym: Cao nguyen 201 luc dang chien
        final int nuoc = p == null ? -1 : p.inCountry;
        return m.mapId == Map.idMapTown && nuoc >= 0 && nuoc < Map.nwar.length && (Map.nwar[nuoc] || Map.getTown[nuoc]);
    }

    /** Ly do dang o ngu canh PvP (null = khong). */
    public static String lyDoPvp(final Char p, final long bay) {
        if (p == null || !bat()) {
            return null;
        }
        if (p.isKiller || p.pk != 0) {
            return "đang bật PK";
        }
        final TrangThai t = ttNeuCo(p);
        if (t != null && t.pvpLuc > 0L && bay - t.pvpLuc <= giayGiaoChienMs()) {
            return "vừa giao chiến với người chơi khác";
        }
        if (laMapPvp(p.map, p)) {
            return "đang ở sàn đấu / trận giữa người chơi";
        }
        for (final Predicate<Char> h : NGU_CANH_THEM) {
            try {
                if (h.test(p)) {
                    return "đang trong trận giữa người chơi";
                }
            } catch (final Exception ignored) {
            }
        }
        return null;
    }

    public static boolean trongPvp(final Char p) {
        return lyDoPvp(p, System.currentTimeMillis()) != null;
    }

    /** TuDongGame: cau tu choi / dung Tu dong danh trong PvP (null = cho). */
    public static String lyDoCamTuDong(final Char p) {
        final String ly = lyDoPvp(p, System.currentTimeMillis());
        return ly == null ? null : "Tự động đánh không dùng trong trận giữa người chơi (" + ly + "). Hãy tự điều khiển, kéo chiêu để ngắm.";
    }

    static boolean clientMoi(final Char p) {
        try {
            return p.getSession() != null && p.getSession().banClient >= BanHuong.BAN_CLIENT;
        } catch (final Exception e) {
            return false;
        }
    }

    // ================================================================== goi tin

    static void bao(final Char p, final String s) {
        try {
            p.sendMessage(MessageCreator.createMsgChat(p.id, s));
        } catch (final Exception ignored) {
        }
    }

    static void guiCheDo(final Char p, final int pvp, final String ly) {
        try {
            final Message m = new Message(BanHuong.LENH);
            m.dos.writeByte(BanHuong.S_CHE_DO);
            m.dos.writeByte(pvp);
            m.dos.writeUTF(ly == null ? "" : ly);
            p.sendMessage(m);
        } catch (final Exception ignored) {
        }
    }

    /** Bang thong so chieu cua lop {@code lop} (de client ve vach ngam dung hinh). */
    static Message goiBang(final int lop) throws java.io.IOException {
        final Message m = new Message(BanHuong.LENH);
        m.dos.writeByte(BanHuong.S_BANG);
        final int n = Math.min(16, CharManager.SKILL_RANGE != null && lop >= 0 && lop < CharManager.SKILL_RANGE.length
                ? CharManager.SKILL_RANGE[lop].length : 16);
        m.dos.writeByte(n);
        for (int s = 0; s < n; s++) {
            final BanHuong.ThongSo t = thongSo(lop, s);
            m.dos.writeByte(s);
            m.dos.writeByte(t.kieu);
            m.dos.writeShort(t.tam);
            m.dos.writeShort(t.rong);
            m.dos.writeShort(t.tocDo);
            m.dos.writeByte(Math.min(127, t.goc / 3)); // goc / 3 (vua byte): client nhan lai x3
            m.dos.writeByte(t.toiDa);
        }
        return m;
    }

    static Message goiDan(final Char p, final int chieu, final int ox, final int oy, final double hx, final double hy, final int tamThat,
            final BanHuong.ThongSo ts) throws java.io.IOException {
        final Message m = new Message(BanHuong.LENH);
        m.dos.writeByte(BanHuong.S_DAN);
        m.dos.writeShort(p.id);
        m.dos.writeByte(chieu);
        m.dos.writeShort(ox);
        m.dos.writeShort(oy);
        m.dos.writeShort((int) Math.round(hx * 1000));
        m.dos.writeShort((int) Math.round(hy * 1000));
        m.dos.writeShort(tamThat);
        m.dos.writeByte(ts.kieu);
        m.dos.writeShort(ts.rong);
        m.dos.writeShort(ts.tocDo);
        m.dos.writeByte(Math.min(127, ts.goc / 3));
        return m;
    }

    static Vector<Char> nguoiCung(final Map map, final Char p) {
        try {
            final Vector<Char> v = map.getAllPlayer(p.inCountry, p.region);
            return v == null ? new Vector<Char>() : v;
        } catch (final Exception e) {
            return new Vector<Char>();
        }
    }

    /** Gui hieu ung dan bay cho nguoi xung quanh co client &gt;= 908 (nguoi ban tu ve tren may minh). */
    static void phatHieuUng(final Map map, final Char p, final Message m) {
        for (final Char o : new ArrayList<Char>(nguoiCung(map, p))) {
            if (o == null || o == p || o.isBot != -1 || !clientMoi(o)) {
                continue;
            }
            if (Math.abs(o.x - p.x) > 400 || Math.abs(o.y - p.y) > 300) {
                continue;
            }
            o.sendMessage(m);
        }
    }

    // ================================================================== nhip (CharVongLap.update ~300 ms)

    public static void nhip(final Char p) {
        if (p == null || p.isBot != -1 || p.getSession() == null) {
            return;
        }
        try {
            final long bay = System.currentTimeMillis();
            final String ly = lyDoPvp(p, bay);
            final int cheDo = ly != null ? 1 : 0;
            final TrangThai t = tt(p);
            if (clientMoi(p)) {
                if (t.lopDaGui != p.charClass) {
                    p.sendMessage(goiBang(p.charClass));
                    t.lopDaGui = p.charClass;
                }
                if (cheDo != t.cheDoDaGui) {
                    guiCheDo(p, cheDo, cheDo == 1 ? BAO_KEO + " (" + ly + ")" : "");
                }
            } else if (cheDo == 1 && t.cheDoDaGui != 1 && bay - t.baoCuLuc > 60000L) {
                t.baoCuLuc = bay;
                bao(p, BAO_CAP_NHAT);
            }
            t.cheDoDaGui = cheDo;
            if (cheDo == 1 && TuDongGame.dangTuDong(p)) {
                TuDongGame.dungViPvp(p);
            }
        } catch (final Exception e) {
            // khong lam hong vong nhan vat
        }
    }

    // ================================================================== lenh

    /**
     * MapXuLyLenh.processMessage, TRUOC moi xu ly khac. @return true = da xu ly (bo qua switch cu).
     */
    public static boolean xuLy(final Map map, final Char p, final Message message) {
        if (p == null || message == null) {
            return false;
        }
        final int lenh = message.command;
        if (lenh != BanHuong.LENH && lenh != LENH_DANH_NGUOI) {
            return false;
        }
        if (lenh == BanHuong.LENH) {
            if (!bat() || !clientMoi(p) || message.dis == null) {
                return true;
            }
            try {
                final DataInputStream dis = message.dis;
                final int loai = dis.readByte();
                if (loai == BanHuong.C_BAN) {
                    final int chieu = dis.readByte();
                    final int ox = dis.readShort(), oy = dis.readShort();
                    final int dx = dis.readShort(), dy = dis.readShort();
                    ban(map, p, chieu, ox, oy, dx, dy, true);
                } else if (loai == BanHuong.C_NGAM_TAY) {
                    tt(p).ngamTayPve = dis.readByte() != 0;
                }
            } catch (final Exception e) {
                // goi ngan / hong: bo
            }
            return true;
        }
        // lenh 6 = danh KHOA MUC TIEU vao nguoi choi
        if (!bat() || message.dis == null) {
            return false;
        }
        final long bay = System.currentTimeMillis();
        try {
            final DataInputStream dis = message.dis;
            final short idMt = dis.readShort();
            final int chieu = dis.readByte();
            final Char c = map.getChar(idMt);
            final TrangThai t = tt(p);
            if (clientMoi(p)) {
                SO_KHOA_MUC_TIEU_CHAN.incrementAndGet();
                ghiGiaoChien(p, null, bay); // y dinh danh nguoi: vao che do ngam ngay
                if (bay - t.baoKeoLuc > 3000L) {
                    t.baoKeoLuc = bay;
                    bao(p, BAO_KEO);
                    guiCheDo(p, 1, BAO_KEO);
                    t.cheDoDaGui = 1;
                }
                return true;
            }
            // client cu: ban thang theo huong nhan vat dang quay (client quay ve phia muc tieu, 4 phia), khong khoa
            SO_CLIENT_CU.incrementAndGet();
            if (bay - t.baoCuLuc > 60000L) {
                t.baoCuLuc = bay;
                bao(p, BAO_CAP_NHAT);
            }
            final int[] h;
            if (c != null) {
                h = BanHuong.huongBonPhia(p.x, p.y, c.x, c.y);
            } else {
                h = new int[]{t.ls.huongX * 1000, t.ls.huongY * 1000};
            }
            ban(map, p, chieu, p.x, p.y, h[0], h[1], false);
        } catch (final Exception e) {
            // goi hong
        }
        return true;
    }

    static boolean khongChe(final Char p) {
        try {
            return p.isHoangSo() || p.isHoangLoan() || p.cannotAttackWhenBienhinh() || p.freeze() || p.checkLamthinh()
                    || p.checkRuNgu() || p.checkChoang() || (com.htbb.pokemon.LauDai.bat() && TrangThaiGame.khongDanhDuoc(p));
        } catch (final Exception e) {
            return false;
        }
    }

    static int capChieu(final Char p, final int chieu) {
        if (p.skill == null || chieu < 0 || chieu >= p.skill.length) {
            return 0;
        }
        final int them = p.addMoreLevelSkill != null && chieu < p.addMoreLevelSkill.length ? p.addMoreLevelSkill[chieu] : 0;
        int lv = p.skill[chieu] + them;
        if (lv <= 0) {
            lv = them;
        }
        return lv;
    }

    static int bang3(final short[][][] a, final int i, final int j, final int k, final int macDinh) {
        try {
            return a[i][j][Math.min(k, a[i][j].length - 1)];
        } catch (final Exception e) {
            return macDinh;
        }
    }

    /** Lich lan gay sat thuong khi dan toi cuoi duong bay (mo phong lai tu lich su vi tri). */
    private static volatile ScheduledExecutorService HEN;

    static ScheduledExecutorService hen() {
        ScheduledExecutorService h = HEN;
        if (h == null) {
            synchronized (BanHuongGame.class) {
                h = HEN;
                if (h == null) {
                    final ScheduledThreadPoolExecutor e = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
                        @Override
                        public Thread newThread(final Runnable r) {
                            final Thread th = new Thread(r, "ngam-tay");
                            th.setDaemon(true);
                            return th;
                        }
                    });
                    e.setRemoveOnCancelPolicy(true);
                    HEN = h = e;
                }
            }
        }
        return h;
    }

    /** Ghi nghi van (hack.txt); test thay de khong ghi tep. */
    static volatile java.util.function.BiConsumer<Char, String> NGHI_VAN = new java.util.function.BiConsumer<Char, String>() {
        @Override
        public void accept(final Char p, final String s) {
            NghiVanGianLan.ghiChung(p, "NGAM_TAY_SPAM", s);
        }
    };

    /** Test: giai quyet ngay thay vi hen gio (dan bay van mo phong theo thoi gian tu lich su vi tri). */
    static volatile boolean GIAI_NGAY = false;

    /** Mot phat ban da qua kiem tra: thong so + goc + huong + thoi diem. */
    static final class Phat {
        Map map;
        Char p;
        int chieu;
        int cap;
        int mpCan;
        BanHuong.ThongSo ts;
        double ox;
        double oy;
        double hx;
        double hy;
        long t0;
        int tamThat;
        int soTrung;
        final List<Integer> daTrung = new ArrayList<Integer>();
    }

    /**
     * Mot phat ban. {@code clientMoi}: client &gt;= 908 tu ngam (goc client gui duoc xet); false = client cu (goc = vi tri
     * may chu, huong 4 phia).
     *
     * @return phat da chap nhan (null = tu choi)
     */
    static Phat ban(final Map map, final Char p, final int chieu, final int ox, final int oy, final int dx, final int dy,
            final boolean tuNgam) {
        final long bay = System.currentTimeMillis();
        final TrangThai t = tt(p);
        final int lop = p.charClass;
        final int cap = capChieu(p, chieu);
        final long hoi = (long) bang3(CharManager.SKILL_COOLDOWN, lop, chieu, cap, 10) * 100L;
        final int mpCan = bang3(CharManager.SKILL_MP, lop, chieu, cap, 0);
        final long daQua = p.timeLastUseSkills != null && chieu >= 0 && chieu < p.timeLastUseSkills.length
                ? bay - p.timeLastUseSkills[chieu] : 0L;
        final int mpCo = p.mp + (p.percentBuff != null && p.percentBuff.length > 1 ? p.percentBuff[1] : 0);
        String loi = BanHuong.kiemBan(chieu, p.skill == null ? 0 : p.skill.length, cap, BanHuong.laBuff(lop, chieu), daQua, hoi,
                mpCo, mpCan, p.hp > 0, khongChe(p), dx, dy);
        if (loi == null && !t.cs.choPhat(bay)) {
            loi = "qua_nhanh";
        }
        if (loi == null && (p.myCountry == -1 || p.map != map)) {
            loi = "ngoai_ban_do";
        }
        if (loi != null) {
            SO_TU_CHOI.incrementAndGet();
            if (t.cs.tuChoi(bay) && bay - t.nghiVanLuc > 60000L) {
                t.nghiVanLuc = bay;
                NGHI_VAN.accept(p, "goi ban bi tu choi " + t.cs.soTuChoi() + " lan / " + (BanHuong.SPAM_MS / 1000) + " giay ("
                        + loi + ")");
            }
            return null;
        }
        final BanHuong.ThongSo ts = thongSo(lop, chieu);
        if (ts.kieu == BanHuong.K_TU_THAN) {
            return null;
        }
        final double[] h = BanHuong.chuanHoa(dx, dy);
        // goc: client moi gui goc; chi dung khi sat vi tri may chu (hien tai hoac trong cua so bu tre)
        int gx = p.x, gy = p.y;
        if (tuNgam) {
            final int[] cu = t.ls.luc(bay - buTre(), p.x, p.y);
            if (BanHuong.gocHopLe(ox, oy, p.x, p.y) || BanHuong.gocHopLe(ox, oy, cu[0], cu[1])) {
                gx = ox;
                gy = oy;
            }
        }
        // tru hoi chieu + PP NGAY luc ban (truot cung mat), lan gay sat thuong hoan lai phan duong cu tru them
        p.timeLastUseSkills[chieu] = bay;
        p.mp -= mpCan;
        if (p.mp < 0) {
            p.mp = 0;
        }
        SO_PHAT.incrementAndGet();
        final Phat ph = new Phat();
        ph.map = map;
        ph.p = p;
        ph.chieu = chieu;
        ph.cap = cap;
        ph.mpCan = mpCan;
        ph.ts = ts;
        ph.ox = gx;
        ph.oy = gy;
        ph.hx = h[0];
        ph.hy = h[1];
        ph.t0 = bay;
        ph.tamThat = BanHuong.tamThat(tuong(map), gx, gy, h[0], h[1], ts.tam);
        try {
            phatHieuUng(map, p, goiDan(p, chieu, gx, gy, h[0], h[1], ph.tamThat, ts));
        } catch (final Exception ignored) {
        }
        final long msBay = ts.msBay(ph.tamThat);
        if (msBay <= 0L || GIAI_NGAY) {
            giaiQuyet(ph);
        } else {
            hen().schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        giaiQuyet(ph);
                    } catch (final Throwable e) {
                        // khong de luong hen chet
                    }
                }
            }, msBay + 10L, TimeUnit.MILLISECONDS);
        }
        return ph;
    }

    static BanHuong.Tuong tuong(final Map map) {
        if (map == null || map.type == null || map.w <= 0) {
            return null;
        }
        return new BanHuong.Tuong() {
            @Override
            public boolean chan(final int x, final int y) {
                return !map.canMove(x, y);
            }
        };
    }

    static Hashtable<Short, Monster> quaiCung(final Map map, final Char p) {
        try {
            return map.nRegion > 0
                    ? ((Region) map.ALL_REGION.get(p.region)).monsters.get(Math.max(0, Math.min(p.inCountry, 1)))
                    : map.monsters.get(p.inCountry);
        } catch (final Exception e) {
            return null;
        }
    }

    /** Ung vien quanh duong dan (loc thoi: gay sat thuong hay khong do duong danh cu quyet). */
    static List<BanHuong.MucTieu> ungVien(final Phat ph) {
        final List<BanHuong.MucTieu> ds = new ArrayList<BanHuong.MucTieu>();
        final int r = ph.ts.tam + ph.ts.rong + 64;
        for (final Char c : new ArrayList<Char>(nguoiCung(ph.map, ph.p))) {
            if (c == null || c == ph.p || c.hp <= 0 || ph.daTrung.contains(Integer.valueOf(c.id))) {
                continue;
            }
            if (Math.abs(c.x - ph.ox) > r || Math.abs(c.y - ph.oy) > r) {
                continue;
            }
            final TrangThai t = ttNeuCo(c);
            ds.add(new BanHuong.MucTieu(c.id, 0, c.x, c.y, t == null ? null : t.ls));
        }
        final Hashtable<Short, Monster> h = quaiCung(ph.map, ph.p);
        if (h != null) {
            for (final Monster mt : new ArrayList<Monster>(h.values())) {
                if (mt == null || mt.isDead || mt.hp <= 0 || ph.daTrung.contains(Integer.valueOf(-1 - mt.id))) {
                    continue;
                }
                if (Math.abs(mt.x - ph.ox) > r || Math.abs(mt.y - ph.oy) > r) {
                    continue;
                }
                ds.add(new BanHuong.MucTieu(-1 - mt.id, 1, mt.x, mt.y, null));
            }
        }
        return ds;
    }

    /** Ket qua giai quyet (test doc). */
    static final class KetQuaGiai {
        final List<Integer> trung = new ArrayList<Integer>();
        final List<Integer> boQua = new ArrayList<Integer>();
    }

    /** Dan toi cuoi duong: mo phong lai tu lich su vi tri, gay sat thuong qua duong danh cu. */
    static KetQuaGiai giaiQuyet(final Phat ph) {
        final KetQuaGiai kq = new KetQuaGiai();
        final Char p = ph.p;
        if (p == null || p.map != ph.map || p.hp <= 0 || p.exit) {
            return kq;
        }
        final BanHuong.Tuong tg = tuong(ph.map);
        final long bu = buTre();
        final boolean dan = ph.ts.kieu == BanHuong.K_DAN;
        // dan: xet lan luot nguoi dau tien tren duong; khong gay duoc sat thuong (to doi, vung an toan...) thi dan bay
        // xuyen qua nguoi do toi nguoi ke tiep (toi da 6 lan). Kieu khac: moi muc tieu tren duong theo thu tu, dem du toiDa.
        final BanHuong.ThongSo tsTat = new BanHuong.ThongSo(ph.ts.kieu == BanHuong.K_DAN ? BanHuong.K_XUYEN : ph.ts.kieu,
                ph.ts.tam, ph.ts.rong, ph.ts.tocDo, ph.ts.goc, 20);
        final BanHuong.KetQua mp = BanHuong.moPhong(tsTat, ph.ox, ph.oy, ph.hx, ph.hy, ph.t0, bu, ungVien(ph), tg);
        int lan = 0;
        for (final BanHuong.Trung tr : mp.trung) {
            if (ph.soTrung >= ph.ts.toiDa || (dan && lan >= 6)) {
                break;
            }
            lan++;
            final boolean ok = gaySatThuong(ph, tr);
            ph.daTrung.add(Integer.valueOf(tr.mt.id));
            if (ok) {
                ph.soTrung++;
                kq.trung.add(Integer.valueOf(tr.mt.id));
            } else {
                kq.boQua.add(Integer.valueOf(tr.mt.id));
            }
        }
        if (ph.soTrung > 0) {
            SO_TRUNG.incrementAndGet();
        } else {
            SO_TRUOT.incrementAndGet();
        }
        return kq;
    }

    static Message goiDanh(final int lenh, final int id, final int chieu) throws java.io.IOException {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream(4);
        final DataOutputStream d = new DataOutputStream(bo);
        d.writeShort(id);
        d.writeByte(chieu);
        d.flush();
        return new Message(lenh, bo.toByteArray());
    }

    /** Luong danh cu cho mot muc tieu. @return true = duong cu chap nhan (qua luat, trong tam) - tinh la trung. */
    static boolean gaySatThuong(final Phat ph, final BanHuong.Trung tr) {
        final Char p = ph.p;
        final int chieu = ph.chieu;
        if (p.timeLastUseSkills == null || chieu < 0 || chieu >= p.timeLastUseSkills.length) {
            return false;
        }
        final long luuMoc = p.timeLastUseSkills[chieu];
        final int luuMp = p.mp;
        boolean danh = false;
        try {
            if (tr.mt.loai == 0) {
                final Char c = ph.map.getChar((short) tr.mt.id);
                if (c == null || c == p || c.hp <= 0) {
                    return false;
                }
                final int tam = CharManager.getSkillRange((byte) chieu, p.charClass);
                // bu tre: duong cu kiem tam bang vi tri HIEN TAI; dan trung o vi tri da lui thoi gian -> dat tam vi tri
                // do cho lan kiem roi tra lai (chi tra khi chua ai doi trong luc do)
                final int cx = c.x, cy = c.y, px = p.x, py = p.y;
                boolean doiC = false, doiP = false;
                if (!Map.inRangeSkill(p, c, tam)) {
                    c.x = tr.x;
                    c.y = tr.y;
                    doiC = true;
                }
                if (!Map.inRangeSkill(p, c, tam)) {
                    p.x = (int) ph.ox;
                    p.y = (int) ph.oy;
                    doiP = true;
                }
                final int gx = (int) ph.ox, gy = (int) ph.oy;
                try {
                    p.timeLastUseSkills[chieu] = 0L;
                    p.mp = luuMp + ph.mpCan;
                    ph.map.doAttackPlayer(p, goiDanh(LENH_DANH_NGUOI, c.id, chieu));
                    danh = p.timeLastUseSkills[chieu] != 0L && Map.inRangeSkill(p, c, tam);
                } finally {
                    if (doiC && c.x == tr.x && c.y == tr.y) {
                        c.x = cx;
                        c.y = cy;
                    }
                    if (doiP && p.x == gx && p.y == gy) {
                        p.x = px;
                        p.y = py;
                    }
                }
                if (danh) {
                    ghiGiaoChien(p, c, System.currentTimeMillis());
                }
            } else {
                final short id = (short) (-1 - tr.mt.id);
                p.timeLastUseSkills[chieu] = 0L;
                p.mp = luuMp + ph.mpCan;
                ph.map.doAttackMonster(p, goiDanh(LENH_DANH_QUAI, id, chieu));
                danh = p.timeLastUseSkills[chieu] != 0L;
            }
        } catch (final Exception e) {
            danh = false;
        } finally {
            p.mp = luuMp;
            p.timeLastUseSkills[chieu] = luuMoc;
        }
        return danh;
    }
}
