package real;

import com.htbb.pokemon.DanhHieuLogic;
import com.htbb.pokemon.DanhHieuLogic.Mau;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.ThangPvp;
import data.Database;
import data.DanhHieu;
import io.Message;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DANH HIEU (0.9.7) - phan GAME: nap mau tu data_danh_hieu, bo dem tob_danh_hieu_dem, mo khoa tu su kien
 * (NhiemVuEngine.suKien / datMuc goi vao day) + danh gia song moi phut (CharVongLap), deo / thao, nhan tren dau
 * (-110 G_NHAN) cho minh va nguoi gan, bang danh hieu (-110 G_DANH_SACH) cho client, chi so cua DUY NHAT danh hieu
 * dang deo (Char.haveNeTranh, ChiSoTrangBiGame, CharChiSo) co tran danh_hieu.tran_phan_nghin.
 *
 * <p>So huu + han dung: kho cu Char.allDanhHieu / tob_danh_hieu (Char.addDanhHieu). Logic thuan: DanhHieuLogic.
 */
public final class DanhHieuGame {

    private DanhHieuGame() {
    }

    // ================================================================== mau (data_danh_hieu)

    static volatile LinkedHashMap<Integer, Mau> MAU = new LinkedHashMap<Integer, Mau>();
    static volatile HashMap<String, List<Mau>> THEO_KIEU = new HashMap<String, List<Mau>>();
    static volatile List<Mau> SONG = Collections.emptyList();
    static volatile HashMap<String, Mau> THEO_TEN_NV = new HashMap<String, Mau>();
    static volatile HashMap<String, Mau> THEO_MA = new HashMap<String, Mau>();
    static volatile HashMap<Integer, Integer> THU_TU = new HashMap<Integer, Integer>();
    static volatile String[] TEN_DO_HIEM = {"Thường", "Hiếm", "Sử thi", "Huyền thoại"};

    /** Dat mau (nap tu CSDL hoac kiem thu). */
    public static void datMau(final List<Mau> ds, final String[] tenDoHiem) {
        final LinkedHashMap<Integer, Mau> m = new LinkedHashMap<Integer, Mau>();
        final HashMap<String, List<Mau>> k = new HashMap<String, List<Mau>>();
        final List<Mau> song = new ArrayList<Mau>();
        final HashMap<String, Mau> nv = new HashMap<String, Mau>();
        final HashMap<Integer, Integer> tt = new HashMap<Integer, Integer>();
        final HashMap<String, Mau> theoMa = new HashMap<String, Mau>();
        for (final Mau x : ds) {
            theoMa.put(x.ma, x);
            m.put(Integer.valueOf(x.id), x);
            tt.put(Integer.valueOf(x.id), Integer.valueOf(x.thuTu));
            final String kieu = x.kieuSuKien();
            if (!kieu.isEmpty()) {
                k.computeIfAbsent(kieu, z -> new ArrayList<Mau>()).add(x);
            } else if (!"trao".equals(x.dieuKien)) {
                song.add(x);
            }
            if ("nhiem_vu".equals(x.dieuKien)) {
                nv.put(x.thamSo, x);
            }
        }
        MAU = m;
        THEO_KIEU = k;
        SONG = song;
        THEO_TEN_NV = nv;
        THEO_MA = theoMa;
        THU_TU = tt;
        if (tenDoHiem != null && tenDoHiem.length > 0) {
            TEN_DO_HIEM = tenDoHiem;
        }
    }

    public static Mau mau(final int id) {
        return MAU.get(Integer.valueOf(id));
    }

    /** Mau theo ma (data_danh_hieu.ma), null neu khong co / dang tat. */
    public static Mau mauTheoMa(final String ma) {
        return ma == null ? null : THEO_MA.get(ma);
    }

    public static int soMau() {
        return MAU.size();
    }

    /** Ten hien cua danh hieu id (data_danh_hieu); null neu khong co mau (data.DanhHieu dung ten cu). */
    public static String ten(final int id) {
        final Mau m = mau(id);
        return m == null || m.ten.isEmpty() ? null : m.ten;
    }

    static boolean bat() {
        return LauDai.so(DanhHieuLogic.K_BAT, 1) != 0;
    }

    static int tranO() {
        return DanhHieuLogic.tranO(LauDai.so(DanhHieuLogic.K_TRAN, DanhHieuLogic.TRAN_MAC_DINH));
    }

    static int tranTrangTri() {
        return Math.max(0, Math.min(DanhHieuLogic.TRAN_TRANG_TRI_MAC_DINH,
                LauDai.so(DanhHieuLogic.K_TRAN_TRANG_TRI, DanhHieuLogic.TRAN_TRANG_TRI_MAC_DINH)));
    }

    // ================================================================== CSDL

    static final String[] TAO_BANG = {
        "CREATE TABLE IF NOT EXISTS data_danh_hieu_do_hiem (id TINYINT NOT NULL, ten VARCHAR(32) NOT NULL DEFAULT '',"
            + " mau VARCHAR(7) NOT NULL DEFAULT '', thu_tu INT NOT NULL DEFAULT 0, PRIMARY KEY (id)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS data_danh_hieu (id INT NOT NULL, ma VARCHAR(48) NOT NULL, ten VARCHAR(64) NOT NULL DEFAULT '',"
            + " mo_ta VARCHAR(255) NOT NULL DEFAULT '', goi_y VARCHAR(255) NOT NULL DEFAULT '', do_hiem TINYINT NOT NULL DEFAULT 0,"
            + " hinh VARCHAR(16) NOT NULL DEFAULT '', chi_so VARCHAR(24) NOT NULL DEFAULT '', phan_nghin INT NOT NULL DEFAULT 0,"
            + " dieu_kien VARCHAR(40) NOT NULL DEFAULT 'trao', tham_so VARCHAR(64) NOT NULL DEFAULT '', so_luong INT NOT NULL DEFAULT 1,"
            + " an TINYINT NOT NULL DEFAULT 0, thoi_han_phut INT NOT NULL DEFAULT 0, nguon VARCHAR(16) NOT NULL DEFAULT '',"
            + " thu_tu INT NOT NULL DEFAULT 0, bat TINYINT NOT NULL DEFAULT 1, PRIMARY KEY (id), UNIQUE KEY uq_ddh_ma (ma))"
            + " DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_danh_hieu_dem (id_char INT NOT NULL, khoa VARCHAR(80) NOT NULL, gia_tri BIGINT NOT NULL DEFAULT 0,"
            + " tap TEXT NULL, PRIMARY KEY (id_char, khoa)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_danh_hieu_log (id BIGINT NOT NULL AUTO_INCREMENT, id_char INT NOT NULL, id_dh INT NOT NULL,"
            + " hanh_dong VARCHAR(16) NOT NULL DEFAULT '', luc DATETIME NOT NULL, PRIMARY KEY (id), KEY idx_dhl_char (id_char))"
            + " DEFAULT CHARSET=utf8",
    };

    /** TeamServer luc khoi dong: bang co the chua co (chua chay sql/them_danh_hieu.sql) - tao rong, khong seed. */
    public static void taoBang() {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            for (final String sql : TAO_BANG) {
                pre = conn.prepareStatement(sql);
                pre.executeUpdate();
                pre.close();
                pre = null;
            }
        } catch (final Exception e) {
            System.out.println("[DANH HIEU] LOI tao bang: " + e);
        } finally {
            dong(null, pre, conn);
        }
    }

    /** NoiDungLauDai.sauKhiNapTemplate: nap lai mau. */
    public static void nap() {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            final List<String> dh = new ArrayList<String>();
            pre = conn.prepareStatement("SELECT id, ten FROM data_danh_hieu_do_hiem ORDER BY id");
            rs = pre.executeQuery();
            while (rs.next()) {
                final int id = rs.getInt(1);
                while (dh.size() < id) {
                    dh.add("");
                }
                if (id >= 0 && id < 16) {
                    dh.add(rs.getString(2));
                }
            }
            rs.close();
            pre.close();
            final List<Mau> ds = new ArrayList<Mau>();
            pre = conn.prepareStatement("SELECT id, ma, ten, mo_ta, goi_y, do_hiem, hinh, chi_so, phan_nghin, dieu_kien, tham_so,"
                    + " so_luong, an, thoi_han_phut, nguon, thu_tu FROM data_danh_hieu WHERE bat = 1 ORDER BY thu_tu, id");
            rs = pre.executeQuery();
            while (rs.next()) {
                final int id = rs.getInt(1);
                if (id <= 0 || id > Short.MAX_VALUE) {
                    continue;
                }
                ds.add(new Mau(id, rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getInt(6),
                        rs.getString(7), rs.getString(8), rs.getInt(9), rs.getString(10), rs.getString(11), rs.getLong(12),
                        rs.getInt(13), rs.getInt(14), rs.getString(15), rs.getInt(16)));
            }
            datMau(ds, dh.isEmpty() ? null : dh.toArray(new String[0]));
            System.out.println("[DANH HIEU] " + ds.size() + " mau danh hieu, " + THEO_KIEU.size() + " kieu su kien, "
                    + SONG.size() + " mau song");
        } catch (final Exception e) {
            System.out.println("[DANH HIEU] LOI nap mau: " + e);
        } finally {
            dong(rs, pre, conn);
        }
    }

    private static void dong(final ResultSet rs, final PreparedStatement pre, final Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (final Exception ignored) {
        }
        try {
            if (pre != null) {
                pre.close();
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

    // ================================================================== trang thai nhan vat (bo dem)

    /** Bo dem + tap cua mot nhan vat. */
    static final class TT {
        final ConcurrentHashMap<String, Long> dem = new ConcurrentHashMap<String, Long>();
        final ConcurrentHashMap<String, Set<String>> tap = new ConcurrentHashMap<String, Set<String>>();
        final Set<String> ban = ConcurrentHashMap.newKeySet();
        volatile boolean daGuiDau;
        volatile long nhipCuoi;

        synchronized Set<String> tap(final String k) {
            Set<String> s = tap.get(k);
            if (s == null) {
                s = Collections.synchronizedSet(new java.util.LinkedHashSet<String>());
                tap.put(k, s);
            }
            return s;
        }
    }

    private static final ConcurrentHashMap<Integer, TT> DS = new ConcurrentHashMap<Integer, TT>();

    static TT tt(final Char p) {
        if (p == null || p.charDBID <= 0) {
            return null;
        }
        return DS.computeIfAbsent(Integer.valueOf(p.charDBID), k -> new TT());
    }

    /** CharNapDuLieu sau getCharDanhHieu: nap bo dem (thay ban nho cu neu co). */
    public static void napChoChar(final Char p) {
        if (p == null || p.charDBID <= 0 || p.isBot != -1) {
            return;
        }
        final TT t = new TT();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT khoa, gia_tri, tap FROM tob_danh_hieu_dem WHERE id_char = ?");
            pre.setInt(1, p.charDBID);
            rs = pre.executeQuery();
            while (rs.next()) {
                final String k = rs.getString(1);
                t.dem.put(k, Long.valueOf(rs.getLong(2)));
                final String tap = rs.getString(3);
                if (tap != null && !tap.isEmpty()) {
                    t.tap(k).addAll(DanhHieuLogic.docTap(tap));
                }
            }
        } catch (final Exception e) {
            System.out.println("[DANH HIEU] LOI nap bo dem " + p.charDBID + ": " + e);
        } finally {
            dong(rs, pre, conn);
        }
        DS.put(Integer.valueOf(p.charDBID), t);
    }

    /** Database.saveChar / saveCharAuto: ghi cac khoa da doi. */
    public static void luu(final Char p) {
        final TT t = p == null || p.charDBID <= 0 ? null : DS.get(Integer.valueOf(p.charDBID));
        if (t == null || t.ban.isEmpty()) {
            return;
        }
        final List<String> ds = new ArrayList<String>(t.ban);
        t.ban.removeAll(ds);
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("INSERT INTO tob_danh_hieu_dem (id_char, khoa, gia_tri, tap) VALUES (?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri), tap = VALUES(tap)");
            for (final String k : ds) {
                final Long v = t.dem.get(k);
                final Set<String> s = t.tap.get(k);
                pre.setInt(1, p.charDBID);
                pre.setString(2, k.length() <= 80 ? k : k.substring(0, 80));
                pre.setLong(3, v == null ? (s == null ? 0L : s.size()) : v.longValue());
                String tap = null;
                if (s != null) {
                    synchronized (s) {
                        tap = DanhHieuLogic.ghiTap(s);
                    }
                }
                pre.setString(4, tap);
                pre.addBatch();
            }
            pre.executeBatch();
        } catch (final Exception e) {
            t.ban.addAll(ds);
            System.out.println("[DANH HIEU] LOI luu bo dem " + p.charDBID + ": " + e);
        } finally {
            dong(null, pre, conn);
        }
    }

    static void ghiLog(final Char p, final int id, final String hd) {
        if (Database.instance == null || Database.connPool == null) {
            return;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("INSERT INTO tob_danh_hieu_log (id_char, id_dh, hanh_dong, luc) VALUES (?, ?, ?, NOW())");
            pre.setInt(1, p.charDBID);
            pre.setInt(2, id);
            pre.setString(3, hd);
            pre.executeUpdate();
        } catch (final Exception ignored) {
        } finally {
            dong(null, pre, conn);
        }
    }

    // ================================================================== ngu canh song

    private static volatile int tongLoai = -1;
    private static volatile long topLuc;
    private static volatile HashMap<Integer, Integer> TOP = new HashMap<Integer, Integer>();
    static final long TOP_MOI_MS = 10L * 60L * 1000L;

    static int tongLoai() {
        int t = tongLoai;
        if (t <= 0) {
            try {
                t = com.htbb.pokemon.Pokedex.tongLoai(PokedexPokemon.dexTemplate());
            } catch (final Exception e) {
                t = 0;
            }
            if (t > 0) {
                tongLoai = t;
            }
        }
        return t;
    }

    /** Hang mua PvP hien tai (bang tob_pvp_mua), nho 10 phut. */
    static int hangPvp(final Char p) {
        final long bay = System.currentTimeMillis();
        if (bay - topLuc > TOP_MOI_MS) {
            topLuc = bay;
            final HashMap<Integer, Integer> m = new HashMap<Integer, Integer>();
            Connection conn = null;
            PreparedStatement pre = null;
            ResultSet rs = null;
            try {
                conn = Database.instance.getConnection();
                pre = conn.prepareStatement("SELECT id_char FROM tob_pvp_mua WHERE mua = ? AND (thang + thua) > 0"
                        + " ORDER BY elo DESC, thang DESC LIMIT 100");
                pre.setInt(1, ThangPvp.mua(LauDai.ngayHienTai()));
                rs = pre.executeQuery();
                int h = 0;
                while (rs.next()) {
                    m.put(Integer.valueOf(rs.getInt(1)), Integer.valueOf(++h));
                }
                TOP = m;
            } catch (final Exception e) {
                // giu bang cu
            } finally {
                dong(rs, pre, conn);
            }
        }
        final Integer h = TOP.get(Integer.valueOf(p.charDBID));
        return h == null ? 0 : h.intValue();
    }

    /** Ngu canh that cua nhan vat. */
    static DanhHieuLogic.NguCanh nguCanh(final Char p, final TT t) {
        return new DanhHieuLogic.NguCanh() {
            public int cap() {
                return p.lvDetail == null ? 0 : p.lvDetail.lv;
            }

            public int pokedexPhanTram() {
                final int tong = tongLoai();
                return tong <= 0 ? 0 : pokedexLoai() * 100 / tong;
            }

            public int pokedexLoai() {
                try {
                    final com.htbb.pokemon.Pokedex d = PokedexPokemon.lay(p);
                    return d == null ? 0 : d.soLoaiDaHa();
                } catch (final Exception e) {
                    return 0;
                }
            }

            public boolean xongNhiemVu(final String ma) {
                return NhiemVuEngine.daXongMa(p, ma);
            }

            public int pvpBac() {
                final HoSoLauDai h = LauDaiNhanVat.lay(p);
                if (h == null || h.pvpMua != ThangPvp.mua(LauDai.ngayHienTai()) || h.pvpThang + h.pvpThua <= 0) {
                    return -1;
                }
                return ThangPvp.bac(h.pvpElo);
            }

            public int hangPvp() {
                return DanhHieuGame.hangPvp(p);
            }

            public boolean suKienBat(final String ma) {
                return SuKien.bat(ma);
            }

            public boolean sinhThai(final String ten) {
                return com.htbb.pokemon.HeSinhThaiGame.coDanhHieu(p, ten);
            }

            public boolean nhiemVu(final String ten) {
                return NhiemVuEngine.coDanhHieuTen(p, ten);
            }

            public int gymHuyHieu() {
                final HoSoLauDai h = LauDaiNhanVat.lay(p);
                return h == null ? 0 : Integer.bitCount(h.gymHuyHieu);
            }

            public long dem(final String khoa) {
                final Long v = t.dem.get(khoa);
                return v == null ? 0L : v.longValue();
            }

            public int tap(final String khoa) {
                final Set<String> s = t.tap.get(khoa);
                return s == null ? 0 : s.size();
            }
        };
    }

    // ================================================================== so huu

    static DanhHieu dangCo(final Char p, final int id) {
        if (p.allDanhHieu == null) {
            return null;
        }
        final long bay = System.currentTimeMillis();
        for (int i = 0; i < p.allDanhHieu.size(); i++) {
            final DanhHieu d = p.allDanhHieu.get(i);
            if (d != null && d.ideff == id && !DanhHieuLogic.hetHan(d.timeExpire, bay)) {
                return d;
            }
        }
        return null;
    }

    /** Mau dang deo (con han) hoac null. */
    public static Mau dangDeo(final Char p) {
        if (p == null || p.idEffDanhHieu < 0) {
            return null;
        }
        final DanhHieu c = p.currentDanhHieu;
        if (c == null || c.ideff != p.idEffDanhHieu || c.isExpire()) {
            return null;
        }
        return mau(p.idEffDanhHieu);
    }

    /** Phan nghin dong {@code ma} cua danh hieu DANG DEO (0 neu tat / khong deo / khac dong). */
    public static int phanNghin(final Char p, final String ma) {
        try {
            if (p == null || p.isBot != -1 || !bat() || !ChiSoTrangBiGame.bat()) {
                return 0;
            }
            return DanhHieuLogic.phanNghin(dangDeo(p), ma, tranO(), tranTrangTri(), 0);
        } catch (final Exception e) {
            return 0;
        }
    }

    // ================================================================== su kien (diem moc)

    /** NhiemVuEngine.suKien: cong bo dem cho moi mau nghe kieu nay. */
    public static void suKien(final Char p, final String kieu, final String thamSo, final int n) {
        capNhat(p, kieu, thamSo, n, false);
    }

    /** NhiemVuEngine.datMuc: bo dem = max(bo dem, gia tri). */
    public static void datMuc(final Char p, final String kieu, final int giaTri) {
        capNhat(p, kieu, "", giaTri, true);
        if ("thanh_tuu_cap".equals(kieu)) {
            // len cap (SoTayLauDai.khiLenCap): mo ngay danh hieu theo cap thay vi doi nhip phut
            try {
                if (p != null && p.isBot == -1 && p.charDBID > 0 && bat()) {
                    final List<Mau> cap = new ArrayList<Mau>();
                    for (final Mau m : SONG) {
                        if ("cap".equals(m.dieuKien)) {
                            cap.add(m);
                        }
                    }
                    kiemTra(p, tt(p), cap);
                }
            } catch (final Exception e) {
                System.out.println("DanhHieuGame.datMuc cap: " + e);
            }
        }
    }

    static void capNhat(final Char p, final String kieu, final String thamSo, final int n, final boolean muc) {
        try {
            if (p == null || p.isBot != -1 || p.charDBID <= 0 || kieu == null || n <= 0) {
                return;
            }
            final List<Mau> ds = THEO_KIEU.get(kieu);
            if (ds == null || ds.isEmpty() || !bat()) {
                return;
            }
            final TT t = tt(p);
            final String ts = thamSo == null ? "" : thamSo;
            final Set<String> daCong = new HashSet<String>();
            for (final Mau m : ds) {
                final String k = m.khoaDem();
                if (daCong.contains(k)) {
                    continue;
                }
                if (m.laTap()) {
                    if (ts.isEmpty() || !laSo(ts)) {
                        continue; // tap chi dem tham so so (id ban do, so thu tu Gym)
                    }
                    daCong.add(k);
                    if (t.tap(k).add(ts)) {
                        t.ban.add(k);
                    }
                    continue;
                }
                if (!m.thamSo.isEmpty() && !m.thamSo.equals(ts)) {
                    continue;
                }
                daCong.add(k);
                final Long cu = t.dem.get(k);
                final long c = cu == null ? 0L : cu.longValue();
                final long moi = muc ? Math.max(c, n) : Math.min(Long.MAX_VALUE / 2, c + n);
                if (moi != c) {
                    t.dem.put(k, Long.valueOf(moi));
                    t.ban.add(k);
                }
            }
            kiemTra(p, t, ds);
        } catch (final Exception e) {
            System.out.println("DanhHieuGame.capNhat " + kieu + ": " + e);
        }
    }

    static boolean laSo(final String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return !s.isEmpty();
    }

    /** Mo khoa cac mau du dieu kien trong ds (chua co, hoac co han can gia han). */
    static void kiemTra(final Char p, final TT t, final List<Mau> ds) {
        final DanhHieuLogic.NguCanh c = nguCanh(p, t);
        final long bay = System.currentTimeMillis();
        for (final Mau m : ds) {
            final DanhHieu co = dangCo(p, m.id);
            if (co != null && !DanhHieuLogic.canGiaHan(co.timeExpire, m.thoiHanPhut, bay)) {
                continue;
            }
            if (DanhHieuLogic.du(m, c)) {
                trao(p, m, "tu_dong");
            }
        }
    }

    /**
     * Vao ban do (MapXuLyLenh sau doi map / vao game): lan dau trong phien gui nhan + so moi, phat thuong mua PvP cu
     * (khong doi nhip phut ~90 giay).
     */
    public static void khiVaoBanDo(final Char p) {
        try {
            if (p == null || p.isBot != -1 || p.charDBID <= 0) {
                return;
            }
            final TT t = tt(p);
            if (t.daGuiDau) {
                return;
            }
            t.daGuiDau = true;
            if (bat() && !MAU.isEmpty()) {
                kiemTra(p, t, SONG);
                guiNhan(p);
                guiSoMoi(p, t);
            }
            XepHangPvpGame.thuongMuaCu(p); // thuong cac mua PvP da ket thuc chua phat (mot lan / mua)
            DiemMocNhiemVu.apChoCho(p); // ban hang tren cho luc offline: cong tien do nhiem vu cho_ban
        } catch (final Exception e) {
            System.out.println("DanhHieuGame.khiVaoBanDo: " + e);
        }
    }

    /** Moi phut (CharVongLap): danh gia mau song + lan dau gui nhan / so moi. */
    public static void nhip(final Char p) {
        try {
            if (p == null || p.isBot != -1 || p.charDBID <= 0) {
                return;
            }
            if (!bat() || MAU.isEmpty()) {
                final TT t0 = tt(p);
                if (!t0.daGuiDau) {
                    t0.daGuiDau = true;
                    XepHangPvpGame.thuongMuaCu(p);
                }
                return;
            }
            final TT t = tt(p);
            kiemTra(p, t, SONG);
            if (!t.daGuiDau) {
                t.daGuiDau = true;
                guiNhan(p);
                guiSoMoi(p, t);
                XepHangPvpGame.thuongMuaCu(p); // thuong cac mua PvP da ket thuc chua phat (mot lan / mua)
            }
        } catch (final Exception e) {
            System.out.println("DanhHieuGame.nhip: " + e);
        }
    }

    /** NhiemVuEngine.thuongKhac "danh_hieu:<ten>": mo ngay danh hieu nhiem vu cung ten. false = khong co mau. */
    public static boolean khiNhanTheoTen(final Char p, final String ten) {
        final Mau m = THEO_TEN_NV.get(ten);
        if (m == null || p == null || !bat()) {
            return false;
        }
        if (dangCo(p, m.id) == null) {
            trao(p, m, "nhiem_vu");
        }
        return true;
    }

    /**
     * Trao danh hieu (tu dong, nhiem vu, admin): Char.addDanhHieu (luu tob_danh_hieu, han theo thoi_han_phut) +
     * danh dau moi / da mo + nhat ky + thong bao. Lan dau thi bao "Danh hiệu mới".
     */
    public static boolean trao(final Char p, final Mau m, final String lyDo) {
        if (p == null || m == null || p.allDanhHieu == null) {
            return false;
        }
        final int deoTruoc = p.idEffDanhHieu;
        // moi = truoc do KHONG dang co (chua tung co, hoac da het han roi nay dat lai); dang co thi chi gia han
        final boolean moi = dangCo(p, m.id) == null;
        try {
            p.addDanhHieu(m.id, m.thoiHanPhut);
        } catch (final NullPointerException e) {
            // may kiem thu khong co CSDL: Char.addDanhHieu da them vao kho truoc khi ghi tob_danh_hieu
        }
        final TT t = tt(p);
        if (!moi) {
            return false; // gia han im lang
        }
        if (t != null) {
            t.tap(DanhHieuLogic.TAP_MOI).add(String.valueOf(m.id));
            t.tap(DanhHieuLogic.TAP_DA_MO).add(String.valueOf(m.id));
            t.ban.add(DanhHieuLogic.TAP_MOI);
            t.ban.add(DanhHieuLogic.TAP_DA_MO);
        }
        ghiLog(p, m.id, lyDo);
        final String hiem = m.doHiem < TEN_DO_HIEM.length ? TEN_DO_HIEM[m.doHiem] : "";
        // thong bao HIEM (the vang, com.htbb.thongbao.ThongBao): dong 1 ten, dong 2 do hiem / han / noi xem
        com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.HIEM, "Danh hiệu mới: " + m.ten,
                (hiem.isEmpty() ? "" : hiem + " · ") + (m.coHan() ? "còn " + DanhHieuLogic.chuConLai(m.thoiHanPhut) + " · " : "")
                        + "Hồ sơ → Danh hiệu", com.htbb.thongbao.ThongBao.HINH_KHONG, 0);
        if (deoTruoc != p.idEffDanhHieu) {
            sauKhiDoi(p); // chua deo gi: Char.addDanhHieu tu deo danh hieu dau tien
        }
        if (t != null) {
            guiSoMoi(p, t);
        }
        return true;
    }

    // ================================================================== deo / thao

    /** Deo danh hieu id (phai dang co, con han). false = khong co. */
    public static boolean deo(final Char p, final int id) {
        if (p == null || p.allDanhHieu == null) {
            return false;
        }
        DanhHieu d = dangCo(p, id);
        if (d == null) {
            final Mau m = mau(id);
            // danh hieu sinh thai / nhiem vu da co o kho rieng nhung chua dong bo: trao ngay roi deo
            if (m != null && ("sinh_thai".equals(m.dieuKien) || "nhiem_vu".equals(m.dieuKien))
                    && DanhHieuLogic.du(m, nguCanh(p, tt(p)))) {
                trao(p, m, "dong_bo");
                d = dangCo(p, id);
            }
            if (d == null) {
                return false;
            }
        }
        p.idEffDanhHieu = id;
        p.currentDanhHieu = d;
        sauKhiDoi(p);
        return true;
    }

    public static void thao(final Char p) {
        if (p == null || p.idEffDanhHieu < 0) {
            return;
        }
        p.idEffDanhHieu = -1;
        p.currentDanhHieu = null;
        sauKhiDoi(p);
    }

    /** Doi danh hieu dang deo: tinh lai chi so, gui thong tin + nhan cho minh va nguoi gan. */
    static void sauKhiDoi(final Char p) {
        try {
            p.calculateAttrib();
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
        } catch (final Exception ignored) {
        }
        guiNhan(p);
    }

    // ================================================================== goi tin

    static boolean moi(final Char p) {
        return p != null && p.session != null && p.session.banClient >= DanhHieuLogic.BAN_CLIENT;
    }

    /** -110 G_NHAN cua ch (danh hieu dang deo co mau) - null neu khong co gi de gui. */
    public static Message taoNhan(final Char ch) {
        if (ch == null || MAU.isEmpty()) {
            return null;
        }
        try {
            final Mau m = dangDeo(ch);
            final Message msg = new Message(DanhHieuLogic.LENH);
            DanhHieuLogic.ghiNhan(msg.dos, ch.id, m == null ? -1 : m.id, m == null ? "" : m.ten, m == null ? 0 : m.doHiem);
            return msg;
        } catch (final Exception e) {
            return null;
        }
    }

    /** Noi -110 G_NHAN vao cuoi chuoi kemTheo (TienHoaPokemon.taoPokeThan: -90 -> ... -> -102 -> -110). */
    public static Message noiDuoi(final Message duoi, final Char ch) {
        final Message n = taoNhan(ch);
        if (n == null) {
            return duoi;
        }
        if (duoi == null) {
            return n;
        }
        Message cuoi = duoi;
        while (cuoi.kemTheo != null) {
            cuoi = cuoi.kemTheo;
        }
        cuoi.kemTheo = n;
        return duoi;
    }

    /** Nhan cua p cho chinh p va nguoi gan (client >= 906). */
    public static void guiNhan(final Char p) {
        if (p == null || p.isBot != -1) {
            return;
        }
        final Message m = taoNhan(p);
        if (m == null) {
            return;
        }
        if (moi(p)) {
            p.sendMessage(m);
        }
        try {
            for (int j = p.nearChars.size() - 1; j >= 0; --j) {
                final Char o = p.map == null ? null : p.map.getPlayerByID(p.nearChars.get(j));
                if (o != null && o.id != p.id && o.isBot == -1 && moi(o) && o.session != null) {
                    o.session.sendMessage(m);
                }
            }
        } catch (final Exception ignored) {
        }
    }

    static void guiSoMoi(final Char p, final TT t) {
        if (!moi(p)) {
            return;
        }
        try {
            final Message m = new Message(DanhHieuLogic.LENH);
            DanhHieuLogic.ghiSoMoi(m.dos, soMoi(p, t));
            p.sendMessage(m);
        } catch (final Exception ignored) {
        }
    }

    /** So danh hieu moi chua xem (con dang co). */
    static int soMoi(final Char p, final TT t) {
        final Set<String> s = t == null ? null : t.tap.get(DanhHieuLogic.TAP_MOI);
        if (s == null) {
            return 0;
        }
        int n = 0;
        synchronized (s) {
            for (final String x : s) {
                try {
                    if (dangCo(p, Integer.parseInt(x)) != null) {
                        n++;
                    }
                } catch (final NumberFormatException ignored) {
                }
            }
        }
        return n;
    }

    /** Dung bang cho p (moi mau con bat, theo luat an / hien). */
    public static List<DanhHieuLogic.Dong> bang(final Char p) {
        final TT t = tt(p);
        final DanhHieuLogic.NguCanh c = nguCanh(p, t);
        final long bay = System.currentTimeMillis();
        final Set<String> daMo = t.tap(DanhHieuLogic.TAP_DA_MO);
        final Set<String> moiXem = t.tap(DanhHieuLogic.TAP_MOI);
        final int tran = tranO();
        final List<DanhHieuLogic.Dong> ds = new ArrayList<DanhHieuLogic.Dong>();
        final Mau deo = dangDeo(p);
        for (final Mau m : MAU.values()) {
            final DanhHieu co = dangCo(p, m.id);
            if (!DanhHieuLogic.coTrongBang(m, co != null)) {
                continue;
            }
            final String k = String.valueOf(m.id);
            ds.add(DanhHieuLogic.dong(m, c, co != null, deo != null && deo.id == m.id, daMo.contains(k),
                    co == null ? 0L : co.timeExpire, moiXem.contains(k), bay, tran));
        }
        DanhHieuLogic.sap(ds, THU_TU);
        return ds;
    }

    /** Gui bang (-110 G_DANH_SACH); mo = client mo bang ngay. */
    public static void guiBang(final Char p, final boolean mo) {
        if (!moi(p) || MAU.isEmpty()) {
            if (mo) {
                DungVatPham.baoNhe(p, "Danh hiệu cần bản game mới nhất.");
            }
            return;
        }
        try {
            if (bat()) {
                kiemTra(p, tt(p), SONG); // mo bang = danh gia trang thai song ngay (khong doi nhip phut)
            }
            final Mau deo = dangDeo(p);
            final Message m = new Message(DanhHieuLogic.LENH);
            DanhHieuLogic.ghiDanhSach(m.dos, mo, deo == null ? -1 : deo.id, TEN_DO_HIEM, bang(p));
            p.sendMessage(m);
        } catch (final Exception e) {
            System.out.println("DanhHieuGame.guiBang: " + e);
        }
    }

    /** Menu cu (Suu tam sinh thai -> Danh hieu): client moi + co mau thi mo bang chung thay menu rieng. */
    public static boolean moBangNeuDuoc(final Char p) {
        if (!moi(p) || MAU.isEmpty()) {
            return false;
        }
        guiBang(p, true);
        return true;
    }

    /** Lenh -111 tu client: byte loai (+ short id khi deo). */
    public static void xuLy(final Char p, final Message msg) {
        if (p == null || p.charDBID <= 0) {
            return;
        }
        try {
            final byte loai = msg.dis.readByte();
            switch (loai) {
                case DanhHieuLogic.K_XIN:
                    guiBang(p, false);
                    break;
                case DanhHieuLogic.K_DEO: {
                    final int id = msg.dis.readShort();
                    if (!deo(p, id)) {
                        com.htbb.thongbao.ThongBao.loi(p, "Bạn chưa có danh hiệu này hoặc đã hết hạn.", null);
                    } else {
                        final Mau m = mau(id);
                        com.htbb.thongbao.ThongBao.ketQua(p, "Đang đeo danh hiệu " + (m == null ? DanhHieu.getName(id) : m.ten), null);
                    }
                    guiBang(p, false);
                    break;
                }
                case DanhHieuLogic.K_THAO:
                    thao(p);
                    com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.TIN, "Đã tháo danh hiệu", null,
                            com.htbb.thongbao.ThongBao.HINH_KHONG, 0);
                    guiBang(p, false);
                    break;
                case DanhHieuLogic.K_DA_XEM: {
                    final TT t = tt(p);
                    final Set<String> s = t.tap(DanhHieuLogic.TAP_MOI);
                    if (!s.isEmpty()) {
                        s.clear();
                        t.ban.add(DanhHieuLogic.TAP_MOI);
                    }
                    guiSoMoi(p, t);
                    break;
                }
                default:
                    break;
            }
        } catch (final Exception e) {
            System.out.println("DanhHieuGame.xuLy: " + e);
        }
    }

    // ================================================================== kiem thu

    /** Kiem thu: dat bo dem cho nhan vat gia (khong CSDL). */
    public static void datDem(final Char p, final String khoa, final long v) {
        final TT t = tt(p);
        t.dem.put(khoa, Long.valueOf(v));
    }

    public static void xoaTrangThai(final Char p) {
        if (p != null) {
            DS.remove(Integer.valueOf(p.charDBID));
        }
    }

    /** So khoa dang cho ghi (kiem thu). */
    public static int soBan(final Char p) {
        final TT t = p == null ? null : DS.get(Integer.valueOf(p.charDBID));
        return t == null ? 0 : t.ban.size();
    }

    public static Set<Entry<String, Long>> dem(final Char p) {
        return tt(p).dem.entrySet();
    }

    /** So danh hieu moi chua xem (kiem thu / HUD). */
    public static int soMoi(final Char p) {
        return soMoi(p, tt(p));
    }
}
