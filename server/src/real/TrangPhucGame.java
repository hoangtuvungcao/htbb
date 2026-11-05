package real;

import com.htbb.pokemon.DanhHieuLogic;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.LichNam;
import com.htbb.pokemon.NapTienDb;
import com.htbb.pokemon.TrangPhucLogic;
import data.Database;
import data.DatabaseBoNgoc;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TU DO TRANG TRI - phan GAME (logic thuan com.htbb.pokemon.TrangPhucLogic). Danh muc data_trang_phuc_o / data_trang_phuc
 * (sql/htbb.sql (khoi them_do_trang_phuc.sql)) nap cung BoNgocGame.nap. So huu dung chung nap_so_huu (Tu do trang tri cua cua hang luong),
 * mon dang mac = dang_dung (moi o mot mon). Dong chi so chon + bo dem quai su kien: tob_trang_phuc.
 *
 * <p>Nguon: mien_phi (cap), su_kien (ha quai trong cua so LichNam), thanh_tuu (danh hieu dang co), cua_hang (luong, qua
 * NapTienGame.mua: so cai, thu hoi, gioi han), che_tao (nguyen lieu phu 1034..1037 + xu theo gio thu nhap = ho hut),
 * nhiem_vu (NhiemVuEngine.thuongKhac "trang_tri:&lt;ma&gt;").
 *
 * <p>Goi -116 (TrangPhucLogic.LENH): chi gui cho client &gt;= 908 (BoNgocGame.BAN_CLIENT). Hinh tren than: duoi goi -102
 * (NgoaiHinh, NgoaiHinhGame.tinh). Chi so: CharChiSo (HP, phong thu) + Char.getAttack (tan cong).
 */
public final class TrangPhucGame {

    public static final String K_BAT = "trang_phuc.bat";

    /** Mot mon trong danh muc. */
    static final class Mon {
        String ma;
        int oIdx;
        int so;
        String ten;
        int doHiem;
        int icon;
        String nguon;
        String thamSo;
        int giaLuong;
        String moTa;
        TrangPhucLogic.DieuKien dk;
    }

    /** Danh muc da nap. */
    static final class DuLieu {
        final LinkedHashMap<String, Mon> mon = new LinkedHashMap<String, Mon>();
        final int[] nganSach = TrangPhucLogic.TRAN_O.clone();
        final String[][] dong = TrangPhucLogic.DONG_MAC_DINH.clone();
        final String[] tenO = TrangPhucLogic.TEN_O.clone();
    }

    private static volatile DuLieu DL = new DuLieu();

    /** Trang thai mot nhan vat. */
    static final class TT {
        final String[] dangMac = {"", "", "", ""};
        final String[] dong = new String[TrangPhucLogic.O.length];
        final long[] doiLuc = new long[TrangPhucLogic.O.length];
        final HashMap<String, Long> soHuu = new HashMap<String, Long>();   // ma -> id nap_so_huu
        final ConcurrentHashMap<String, Long> dem = new ConcurrentHashMap<String, Long>();
        final Set<String> ban = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    }

    private static final ConcurrentHashMap<Integer, TT> DS = new ConcurrentHashMap<Integer, TT>();
    private static final ConcurrentHashMap<Integer, Long> LAN = new ConcurrentHashMap<Integer, Long>();
    static final long KHOANG_MS = 400L;

    private TrangPhucGame() {
    }

    public static boolean bat() {
        return LauDai.so(K_BAT, 1) != 0;
    }

    public static final String[] TAO_BANG = {
        "CREATE TABLE IF NOT EXISTS data_trang_phuc_o (o VARCHAR(16) NOT NULL, ten VARCHAR(32) NOT NULL DEFAULT '',"
            + " phan_nghin INT NOT NULL DEFAULT 0, dong VARCHAR(80) NOT NULL DEFAULT '', thu_tu INT NOT NULL DEFAULT 0,"
            + " PRIMARY KEY (o)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS data_trang_phuc (ma VARCHAR(40) NOT NULL, o VARCHAR(16) NOT NULL, so TINYINT UNSIGNED NOT NULL,"
            + " ten VARCHAR(64) NOT NULL DEFAULT '', do_hiem TINYINT NOT NULL DEFAULT 0, icon INT NOT NULL DEFAULT 0,"
            + " manh VARCHAR(32) NOT NULL DEFAULT '', nguon VARCHAR(16) NOT NULL DEFAULT '', tham_so VARCHAR(96) NOT NULL DEFAULT '',"
            + " gia_luong INT NOT NULL DEFAULT 0, mo_ta VARCHAR(255) NOT NULL DEFAULT '', thu_tu INT NOT NULL DEFAULT 0,"
            + " bat TINYINT NOT NULL DEFAULT 1, PRIMARY KEY (ma), UNIQUE KEY uq_dtp_o_so (o, so)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_trang_phuc (char_id INT NOT NULL, khoa VARCHAR(48) NOT NULL,"
            + " gia_tri VARCHAR(48) NOT NULL DEFAULT '', luc BIGINT NOT NULL DEFAULT 0, PRIMARY KEY (char_id, khoa)) DEFAULT CHARSET=utf8",
    };

    /** BoNgocGame.nap goi. Bang loi: giu du lieu cu. */
    public static synchronized String nap() {
        final List<Object[]> o = DatabaseBoNgoc.docBang("SELECT o, ten, phan_nghin, dong FROM data_trang_phuc_o", 4);
        final List<Object[]> m = DatabaseBoNgoc.docBang("SELECT ma, o, so, ten, do_hiem, icon, nguon, tham_so, gia_luong, mo_ta"
                + " FROM data_trang_phuc WHERE bat = 1 ORDER BY thu_tu, ma", 10);
        if (o == null || m == null) {
            return "[TRANG PHUC] giữ dữ liệu cũ (bảng lỗi)";
        }
        final DuLieu d = new DuLieu();
        for (final Object[] r : o) {
            final int i = TrangPhucLogic.oIdx(DatabaseBoNgoc.chu(r[0]));
            if (i < 0) {
                continue;
            }
            d.tenO[i] = DatabaseBoNgoc.chu(r[1]);
            d.nganSach[i] = TrangPhucLogic.nganSach(i, DatabaseBoNgoc.so(r[2]));
            final List<String> dong = new ArrayList<String>();
            for (final String x : DatabaseBoNgoc.chu(r[3]).split(",")) {
                final int k = x.indexOf(':');
                final String ma = (k > 0 ? x.substring(0, k) : x).trim();
                if (java.util.Arrays.asList(TrangPhucLogic.MA_CHI_SO).contains(ma)) {
                    dong.add(ma);
                }
            }
            if (!dong.isEmpty()) {
                d.dong[i] = dong.toArray(new String[0]);
            }
        }
        for (final Object[] r : m) {
            final Mon x = new Mon();
            x.ma = DatabaseBoNgoc.chu(r[0]);
            x.oIdx = TrangPhucLogic.oIdx(DatabaseBoNgoc.chu(r[1]));
            if (x.oIdx < 0 || x.ma.isEmpty()) {
                continue;
            }
            x.so = DatabaseBoNgoc.so(r[2]);
            x.ten = DatabaseBoNgoc.chu(r[3]);
            x.doHiem = DatabaseBoNgoc.so(r[4]);
            x.icon = DatabaseBoNgoc.so(r[5]);
            x.nguon = DatabaseBoNgoc.chu(r[6]);
            x.thamSo = DatabaseBoNgoc.chu(r[7]);
            x.giaLuong = DatabaseBoNgoc.so(r[8]);
            x.moTa = DatabaseBoNgoc.chu(r[9]);
            x.dk = TrangPhucLogic.doc(x.nguon, x.thamSo);
            d.mon.put(x.ma, x);
        }
        DL = d;
        suKienDangCo = null;
        return "[TRANG PHUC] " + d.mon.size() + " món trang trí";
    }

    /** Ma nay la mon tu do trang tri moi (data_trang_phuc). */
    public static boolean laMa(final String ma) {
        return ma != null && DL.mon.containsKey(ma);
    }

    /** So mon tu do KHONG thuoc danh muc trang phuc (de tran o Tu do cua cua hang khong tinh trang phuc). */
    public static int demNgoaiDanhMuc(final List<NapTienDb.SoHuu> ds) {
        int n = 0;
        for (final NapTienDb.SoHuu s : ds) {
            if (!laMa(s.ma)) {
                n++;
            }
        }
        return n;
    }

    // ================================================================== trang thai nhan vat

    static TT tt(final Char p) {
        if (p == null || p.charDBID <= 0) {
            return null;
        }
        return DS.get(Integer.valueOf(p.charDBID));
    }

    /** CharNapDuLieu sau DanhHieuGame.napChoChar. */
    public static void napChoChar(final Char p) {
        if (p == null || p.charDBID <= 0 || p.isBot != -1) {
            return;
        }
        final TT t = new TT();
        docSoHuu(p, t);
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT khoa, gia_tri, luc FROM tob_trang_phuc WHERE char_id = ?");
            pre.setInt(1, p.charDBID);
            rs = pre.executeQuery();
            while (rs.next()) {
                final String k = rs.getString(1);
                final String v = rs.getString(2);
                if (k.startsWith("dong:")) {
                    final int i = TrangPhucLogic.oIdx(k.substring(5));
                    if (i >= 0) {
                        t.dong[i] = v;
                        t.doiLuc[i] = rs.getLong(3);
                    }
                } else if (k.startsWith("sk:")) {
                    try {
                        t.dem.put(k, Long.valueOf(Long.parseLong(v)));
                    } catch (final NumberFormatException ignored) {
                    }
                }
            }
        } catch (final Exception e) {
            System.out.println("[TRANG PHUC] LOI nap " + p.charDBID + ": " + e);
        } finally {
            dong(rs, pre, conn);
        }
        DS.put(Integer.valueOf(p.charDBID), t);
        traoMienPhi(p);
    }

    static void docSoHuu(final Char p, final TT t) {
        try {
            final List<NapTienDb.SoHuu> ds = NapTienDb.soHuu(p.charDBID, System.currentTimeMillis());
            synchronized (t) {
                t.soHuu.clear();
                java.util.Arrays.fill(t.dangMac, "");
                for (final NapTienDb.SoHuu s : ds) {
                    final Mon m = DL.mon.get(s.ma);
                    if (m == null) {
                        continue;
                    }
                    t.soHuu.put(s.ma, Long.valueOf(s.id));
                    if (s.dangDung && t.dangMac[m.oIdx].isEmpty()) {
                        t.dangMac[m.oIdx] = s.ma;
                    }
                }
            }
        } catch (final Exception e) {
            System.out.println("[TRANG PHUC] LOI doc tu do " + p.charDBID + ": " + e);
        }
    }

    /** Database.saveChar / saveCharAuto: ghi bo dem da doi. */
    public static void luu(final Char p) {
        final TT t = tt(p);
        if (t == null || t.ban.isEmpty()) {
            return;
        }
        final List<String> ds = new ArrayList<String>(t.ban);
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("REPLACE INTO tob_trang_phuc (char_id, khoa, gia_tri, luc) VALUES (?, ?, ?, ?)");
            for (final String k : ds) {
                String v;
                long luc = System.currentTimeMillis();
                if (k.startsWith("dong:")) {
                    final int i = TrangPhucLogic.oIdx(k.substring(5));
                    if (i < 0 || t.dong[i] == null) {
                        continue;
                    }
                    v = t.dong[i];
                    luc = t.doiLuc[i];
                } else {
                    final Long n = t.dem.get(k);
                    if (n == null) {
                        continue;
                    }
                    v = String.valueOf(n);
                }
                pre.setInt(1, p.charDBID);
                pre.setString(2, k.length() > 48 ? k.substring(0, 48) : k);
                pre.setString(3, v);
                pre.setLong(4, luc);
                pre.executeUpdate();
                t.ban.remove(k);
            }
        } catch (final Exception e) {
            System.out.println("[TRANG PHUC] LOI luu " + p.charDBID + ": " + e);
        } finally {
            dong(null, pre, conn);
        }
    }

    static void dong(final ResultSet rs, final PreparedStatement pre, final Connection conn) {
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
                conn.close();
            }
        } catch (final Exception ignored) {
        }
    }

    // ================================================================== chi so

    /** Phan nghin danh hieu dang deo (chi de tinh tran tong trang tri). */
    static int phanNghinDanhHieu(final Char p) {
        try {
            final DanhHieuLogic.Mau m = DanhHieuGame.dangDeo(p);
            return m == null || !DanhHieuGame.bat() ? 0 : DanhHieuLogic.hieuLuc(m.phanNghin, DanhHieuGame.tranO(),
                    DanhHieuGame.tranTrangTri(), 0);
        } catch (final Exception e) {
            return 0;
        }
    }

    static int[] hieuLuc(final Char p, final TT t) {
        final boolean[] mac = new boolean[TrangPhucLogic.O.length];
        for (int i = 0; i < mac.length; i++) {
            mac[i] = !t.dangMac[i].isEmpty();
        }
        return TrangPhucLogic.hieuLuc(DL.nganSach, mac, phanNghinDanhHieu(p), DanhHieuGame.tranTrangTri());
    }

    static String dongChon(final TT t, final int i) {
        final String[] ds = DL.dong[i];
        if (t.dong[i] != null) {
            for (final String d : ds) {
                if (d.equals(t.dong[i])) {
                    return d;
                }
            }
        }
        return ds.length > 0 ? ds[0] : "hp";
    }

    /**
     * Phan nghin dong {@code chiSo} ("hp", "tan_cong", "phong_thu", "thu_dac_biet") tu cac o trang tri dang mac. 0 khi tat
     * (trang_phuc.bat / chi_so.bat / sv.lau_dai), bot, chua nap.
     */
    public static int phanNghin(final Char p, final String chiSo) {
        try {
            if (p == null || p.isBot != -1 || !bat() || !ChiSoTrangBiGame.bat()) {
                return 0;
            }
            final TT t = tt(p);
            if (t == null) {
                return 0;
            }
            final String[] chon = new String[TrangPhucLogic.O.length];
            for (int i = 0; i < chon.length; i++) {
                chon[i] = dongChon(t, i);
            }
            return TrangPhucLogic.phanNghin(chiSo, chon, hieuLuc(p, t));
        } catch (final Exception e) {
            return 0;
        }
    }

    /** So hieu (data_trang_phuc.so) mon dang mac o {@code oIdx} cho goi -102; 0 = khong. */
    public static int soDangMac(final Char p, final int oIdx) {
        final TT t = tt(p);
        if (t == null || !bat() || oIdx < 0 || oIdx >= TrangPhucLogic.O.length) {
            return 0;
        }
        final Mon m = DL.mon.get(t.dangMac[oIdx]);
        return m == null ? 0 : Math.max(0, Math.min(255, m.so));
    }

    // ================================================================== nguon

    /** Cache ma LichNam dang dien ra (lam moi moi phut). */
    private static volatile Set<String> suKienDangCo;
    private static volatile long suKienLuc;

    static Set<String> suKienDangCo() {
        final long bay = System.currentTimeMillis();
        Set<String> s = suKienDangCo;
        if (s == null || bay - suKienLuc > 60000L) {
            s = new HashSet<String>();
            for (final Mon m : DL.mon.values()) {
                for (final String l : m.dk.lich) {
                    if (!s.contains(l) && LichNam.dangDienRa(l, bay)) {
                        s.add(l);
                    }
                }
            }
            suKienDangCo = s;
            suKienLuc = bay;
        }
        return s;
    }

    /** Map.capNhatDiemGietQuai (quai +-10 cap): cong bo dem quai su kien cho mon su kien dang mo ma chua co. */
    public static void khiHaQuai(final Char p) {
        try {
            if (p == null || p.isBot != -1 || !bat()) {
                return;
            }
            final TT t = tt(p);
            if (t == null) {
                return;
            }
            final Set<String> dang = suKienDangCo();
            if (dang.isEmpty()) {
                return;
            }
            final int nam = LichNam.namCua(System.currentTimeMillis());
            for (final Mon m : DL.mon.values()) {
                if (!TrangPhucLogic.SU_KIEN.equals(m.nguon) || t.soHuu.containsKey(m.ma)) {
                    continue;
                }
                boolean mo = false;
                for (final String l : m.dk.lich) {
                    mo |= dang.contains(l);
                }
                if (!mo) {
                    continue;
                }
                final String k = TrangPhucLogic.khoaSuKien(m.ma, nam);
                final Long cu = t.dem.get(k);
                final long v = (cu == null ? 0L : cu.longValue()) + 1L;
                if (cu == null || cu.longValue() < m.dk.quai) {
                    t.dem.put(k, Long.valueOf(v));
                    t.ban.add(k);
                }
            }
        } catch (final Exception ignored) {
        }
    }

    static int cap(final Char p) {
        return p.lvDetail != null ? p.lvDetail.lv : 1;
    }

    static long demSuKien(final TT t, final Mon m) {
        final Long v = t.dem.get(TrangPhucLogic.khoaSuKien(m.ma, LichNam.namCua(System.currentTimeMillis())));
        return v == null ? 0L : v.longValue();
    }

    static boolean coDanhHieu(final Char p, final String ma) {
        try {
            final DanhHieuLogic.Mau m = DanhHieuGame.mauTheoMa(ma);
            return m != null && DanhHieuGame.dangCo(p, m.id) != null;
        } catch (final Exception e) {
            return false;
        }
    }

    /** Nhan ngay duoc khong (mien phi / su kien / thanh tuu). */
    static boolean nhanDuoc(final Char p, final TT t, final Mon m) {
        if (t.soHuu.containsKey(m.ma)) {
            return false;
        }
        switch (m.nguon) {
            case TrangPhucLogic.MIEN_PHI:
                return cap(p) >= m.dk.cap;
            case TrangPhucLogic.SU_KIEN:
                return m.dk.quai > 0 && demSuKien(t, m) >= m.dk.quai;
            case TrangPhucLogic.THANH_TUU:
                return !m.dk.danhHieu.isEmpty() && coDanhHieu(p, m.dk.danhHieu);
            default:
                return false;
        }
    }

    static String tenSuKien(final Mon m) {
        final StringBuilder b = new StringBuilder();
        for (final String l : m.dk.lich) {
            final LichNam.Muc x = LichNam.muc(l);
            if (x != null) {
                b.append(b.length() > 0 ? " / " : "").append(x.ten);
            }
        }
        return b.toString();
    }

    static String chuNguon(final Mon m) {
        switch (m.nguon) {
            case TrangPhucLogic.MIEN_PHI:
                return "Miễn phí từ cấp " + m.dk.cap;
            case TrangPhucLogic.SU_KIEN:
                return "Sự kiện " + tenSuKien(m);
            case TrangPhucLogic.THANH_TUU: {
                final DanhHieuLogic.Mau d = DanhHieuGame.mauTheoMa(m.dk.danhHieu);
                return "Thành tựu: " + (d != null ? d.ten : m.dk.danhHieu);
            }
            case TrangPhucLogic.CUA_HANG:
                return "Cửa hàng lượng (chỉ đổi hình)";
            case TrangPhucLogic.CHE_TAO:
                return "Chế tạo ở Machamp";
            case TrangPhucLogic.NHIEM_VU:
                return "Phần thưởng nhiệm vụ";
            default:
                return m.nguon;
        }
    }

    static String tienDo(final Char p, final TT t, final Mon m) {
        if (t.soHuu.containsKey(m.ma)) {
            return "Đã có";
        }
        switch (m.nguon) {
            case TrangPhucLogic.MIEN_PHI:
                return cap(p) >= m.dk.cap ? "Nhận ngay" : "Cần cấp " + m.dk.cap;
            case TrangPhucLogic.SU_KIEN: {
                final boolean mo = !Collections.disjoint(suKienDangCo(), m.dk.lich);
                final long n = Math.min(demSuKien(t, m), m.dk.quai);
                return (mo ? "" : "Chưa tới sự kiện · ") + "Hạ " + n + "/" + m.dk.quai + " Pokémon trong sự kiện";
            }
            case TrangPhucLogic.THANH_TUU: {
                final DanhHieuLogic.Mau d = DanhHieuGame.mauTheoMa(m.dk.danhHieu);
                return coDanhHieu(p, m.dk.danhHieu) ? "Nhận ngay" : "Cần danh hiệu " + (d != null ? d.ten : m.dk.danhHieu);
            }
            case TrangPhucLogic.CUA_HANG:
                return m.giaLuong + " lượng";
            case TrangPhucLogic.CHE_TAO:
                return TrangPhucLogic.chuCheTao(m.dk);
            default:
                return "";
        }
    }

    /** Trao mon (nhiem vu / admin / nhan). false neu khong co mon / da co / loi CSDL. */
    public static boolean trao(final Char p, final String ma, final String lyDo) {
        final Mon m = DL.mon.get(ma);
        final TT t = tt(p);
        if (m == null || t == null) {
            return false;
        }
        synchronized (t) {
            if (t.soHuu.containsKey(ma)) {
                return false;
            }
            try {
                final long id = NapTienDb.themSoHuu(p.charDBID, ma, lyDo, false, 0L);
                t.soHuu.put(ma, Long.valueOf(id));
            } catch (final Exception e) {
                System.out.println("[TRANG PHUC] LOI trao " + ma + " cho " + p.charDBID + ": " + e);
                return false;
            }
        }
        try {
            Database.instance.saveOrtherLog("", p.charname, "trang phuc trao " + ma + " (" + lyDo + ")", "trangphuc");
        } catch (final Exception ignored) {
        }
        com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.HIEM, "Trang trí mới: " + m.ten,
                DL.tenO[m.oIdx] + " · Hồ sơ → Ngoại hình", com.htbb.thongbao.ThongBao.HINH_ICON, m.icon);
        return true;
    }

    /** Dang nhap: trao mon mien phi du cap (moi o co mot mon som, khong can bam). */
    static void traoMienPhi(final Char p) {
        final TT t = tt(p);
        if (t == null || !bat()) {
            return;
        }
        for (final Mon m : DL.mon.values()) {
            if (TrangPhucLogic.MIEN_PHI.equals(m.nguon) && nhanDuoc(p, t, m)) {
                trao(p, m.ma, TrangPhucLogic.MIEN_PHI);
            }
        }
    }

    // ================================================================== mac / thao / dong

    /** Mac {@code ma} vao o cua no ({@code ma} rong = thao o {@code oIdx}). Tra thong bao loi hoac null. */
    public static String mac(final Char p, final int oIdx, final String ma) {
        final TT t = tt(p);
        if (t == null) {
            return "Tủ đồ chưa sẵn sàng, thử lại sau.";
        }
        final Mon m = ma == null || ma.isEmpty() ? null : DL.mon.get(ma);
        final int o = m != null ? m.oIdx : oIdx;
        if (o < 0 || o >= TrangPhucLogic.O.length) {
            return "Ô không hợp lệ.";
        }
        if (m != null && !t.soHuu.containsKey(ma)) {
            return "Bạn chưa có món này.";
        }
        try {
            synchronized (t) {
                final String cu = t.dangMac[o];
                if (!cu.isEmpty() && t.soHuu.containsKey(cu)) {
                    NapTienDb.datDung(t.soHuu.get(cu).longValue(), p.charDBID, false);
                }
                // phong khi CSDL co nhieu mon cung o dang dung: tat het truoc
                for (final java.util.Map.Entry<String, Long> e : t.soHuu.entrySet()) {
                    final Mon x = DL.mon.get(e.getKey());
                    if (x != null && x.oIdx == o && !e.getKey().equals(ma) && !e.getKey().equals(cu)) {
                        NapTienDb.datDung(e.getValue().longValue(), p.charDBID, false);
                    }
                }
                if (m != null && !NapTienDb.datDung(t.soHuu.get(ma).longValue(), p.charDBID, true)) {
                    t.dangMac[o] = "";
                    return "Món đang rao bán ở chợ, không mặc được.";
                }
                t.dangMac[o] = m != null ? ma : "";
            }
        } catch (final Exception e) {
            return "Tủ đồ tạm thời không ghi được, thử lại sau.";
        }
        sauKhiDoi(p);
        return null;
    }

    /** Sau khi doi mon dang mac / dong: tinh lai chi so + gui lai -102 cho minh va nguoi gan. */
    static void sauKhiDoi(final Char p) {
        try {
            p.calculateAttrib();
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
        } catch (final Exception ignored) {
        }
        NgoaiHinhGame.guiNeuDoi(p);
    }

    /** NapTienGame menu Tu do "Dung / Bo dung" voi mon trong danh muc: di qua {@link #mac}. */
    public static String macTuMenu(final Char p, final String ma, final boolean dung) {
        final Mon m = DL.mon.get(ma);
        if (m == null) {
            return "Không có món này.";
        }
        if (tt(p) == null) {
            napChoChar(p);
        } else {
            docSoHuu(p, tt(p));
        }
        final String loi = mac(p, m.oIdx, dung ? ma : "");
        return loi != null ? loi : (dung ? "Đang dùng " : "Đã bỏ dùng ") + m.ten + ".";
    }

    static String doiDong(final Char p, final int o, final int dongIdx) {
        final TT t = tt(p);
        if (t == null || o < 0 || o >= TrangPhucLogic.O.length) {
            return "Ô không hợp lệ.";
        }
        final String[] ds = DL.dong[o];
        if (dongIdx < 0 || dongIdx >= ds.length) {
            return "Dòng chỉ số không hợp lệ.";
        }
        if (ds[dongIdx].equals(dongChon(t, o))) {
            return null;
        }
        final long bay = System.currentTimeMillis();
        if (t.doiLuc[o] > 0 && bay - t.doiLuc[o] < TrangPhucLogic.KHOANG_DOI_DONG_MS) {
            return "Mỗi ô đổi dòng chỉ số 1 lần / ngày (còn " + giayCon(t, o) / 3600 + " giờ).";
        }
        t.dong[o] = ds[dongIdx];
        t.doiLuc[o] = bay;
        t.ban.add("dong:" + TrangPhucLogic.O[o]);
        sauKhiDoi(p);
        return null;
    }

    static int giayCon(final TT t, final int o) {
        final long con = t.doiLuc[o] + TrangPhucLogic.KHOANG_DOI_DONG_MS - System.currentTimeMillis();
        return t.doiLuc[o] <= 0 || con <= 0 ? 0 : (int) (con / 1000L);
    }

    static String nhan(final Char p, final String ma) {
        final TT t = tt(p);
        final Mon m = DL.mon.get(ma);
        if (t == null || m == null) {
            return "Không có món này.";
        }
        if (t.soHuu.containsKey(ma)) {
            return "Bạn đã có " + m.ten + ".";
        }
        if (!nhanDuoc(p, t, m)) {
            return "Chưa đủ điều kiện: " + tienDo(p, t, m);
        }
        return trao(p, ma, m.nguon) ? null : "Tủ đồ tạm thời không ghi được, thử lại sau.";
    }

    static String cheTao(final Char p, final String ma) {
        final TT t = tt(p);
        final Mon m = DL.mon.get(ma);
        if (t == null || m == null || !TrangPhucLogic.CHE_TAO.equals(m.nguon)) {
            return "Món này không chế được.";
        }
        if (t.soHuu.containsKey(ma)) {
            return "Bạn đã có " + m.ten + ".";
        }
        final long xu = TrangPhucLogic.xuCheTao(m.dk, LauDai.xuMoiGio(cap(p)));
        final String[] thieu = {null};
        synchronized (p) {
            final boolean du = p.truNeuDu(() -> {
                if (t.soHuu.containsKey(ma)) { // 05/11/2026: kiem lai TRONG khoa - hai goi che cung luc tru hai lan
                    thieu[0] = "Bạn đã có " + m.ten + ".";
                    return false;
                }
                for (final int[] x : m.dk.nguyenLieu) {
                    if (VatPhamDungLauDai.demItem(p, x[0]) < x[1]) {
                        thieu[0] = "Thiếu " + TrangPhucLogic.tenNguyenLieu(x[0]) + " (cần " + x[1] + ").";
                        return false;
                    }
                }
                if (p.xu < xu) {
                    thieu[0] = "Thiếu xu (cần " + xu + " xu).";
                    return false;
                }
                return true;
            }, () -> {
                for (final int[] x : m.dk.nguyenLieu) {
                    VatPhamDungLauDai.truItem(p, x[0], x[1]);
                }
                if (xu > 0) {
                    p.subXu(xu, true, "che trang phuc " + ma);
                }
            });
            if (!du) {
                return thieu[0] == null ? "Không đủ nguyên liệu." : thieu[0];
            }
            // trao trong cung khoa p (goi che thu hai cho toi khi mon da vao tu do -> thay "Bạn đã có").
            if (!trao(p, ma, TrangPhucLogic.CHE_TAO)) {
                if (xu > 0) {
                    p.addXu(xu, "hoan che trang phuc " + ma, Char.XU_HOAN); // loi ghi tu do: tra xu ngay
                }
                return "Lỗi ghi tủ đồ, đã hoàn " + xu + " xu; hãy báo quản trị để hoàn nguyên liệu.";
            }
        }
        try {
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
        } catch (final Exception ignored) {
        }
        return null;
    }

    static String mua(final Char p, final String ma) {
        final TT t = tt(p);
        final Mon m = DL.mon.get(ma);
        if (t == null || m == null || !TrangPhucLogic.CUA_HANG.equals(m.nguon)) {
            return "Món này không bán.";
        }
        if (t.soHuu.containsKey(ma)) {
            return "Bạn đã có " + m.ten + ".";
        }
        final String kq = com.htbb.pokemon.NapTienGame.muaTheoMa(p, ma);
        docSoHuu(p, t);
        return t.soHuu.containsKey(ma) ? null : kq;
    }

    // ================================================================== goi -116

    static boolean coClient(final Char p) {
        return BoNgocGame.coClient(p);
    }

    /** MapXuLyLenh case -116. */
    public static void xuLy(final Char p, final Message msg) {
        if (p == null || p.charDBID <= 0 || !coClient(p)) {
            return;
        }
        try {
            final int kieu = msg.dis.readUnsignedByte();
            final Integer k = Integer.valueOf(p.charDBID);
            final long bay = System.currentTimeMillis();
            final Long lan = LAN.get(k);
            if (kieu != TrangPhucLogic.C_XIN && lan != null && bay - lan.longValue() < KHOANG_MS) {
                return;
            }
            LAN.put(k, Long.valueOf(bay));
            if (!bat()) {
                ketQua(p, false, "Tủ đồ trang trí đang tạm khoá.");
                return;
            }
            if (tt(p) == null) {
                napChoChar(p);
            }
            String loi;
            String ok;
            switch (kieu) {
                case TrangPhucLogic.C_XIN:
                    traoMienPhi(p);
                    guiTuDo(p);
                    return;
                case TrangPhucLogic.C_MAC: {
                    final int o = msg.dis.readUnsignedByte();
                    final String ma = msg.dis.readUTF();
                    loi = mac(p, o, ma);
                    ok = ma.isEmpty() ? "Đã tháo." : "Đã mặc " + ten(ma) + ".";
                    break;
                }
                case TrangPhucLogic.C_DONG: {
                    final int o = msg.dis.readUnsignedByte();
                    final int d = msg.dis.readUnsignedByte();
                    loi = doiDong(p, o, d);
                    ok = "Đã đổi dòng chỉ số.";
                    break;
                }
                case TrangPhucLogic.C_NHAN: {
                    final String ma = msg.dis.readUTF();
                    loi = nhan(p, ma);
                    ok = "Đã nhận " + ten(ma) + ".";
                    break;
                }
                case TrangPhucLogic.C_CHE: {
                    final String ma = msg.dis.readUTF();
                    loi = cheTao(p, ma);
                    ok = "Đã chế " + ten(ma) + ".";
                    break;
                }
                case TrangPhucLogic.C_MUA: {
                    final String ma = msg.dis.readUTF();
                    loi = mua(p, ma);
                    ok = "Đã mua " + ten(ma) + ".";
                    break;
                }
                default:
                    return;
            }
            ketQua(p, loi == null, loi == null ? ok : loi);
            guiTuDo(p);
        } catch (final Exception e) {
            // goi hong: bo qua
        }
    }

    /** Ten mon tu do; null = khong co trong danh muc. */
    public static String ten(final String ma) {
        final Mon m = DL.mon.get(ma);
        return m == null ? null : m.ten;
    }

    /** Client co man Tu do (Nhan vat &gt; Tu do, goi -116): mac / thao / doi dong chi so CHI o do (mot loi vao). */
    public static boolean coManTuDo(final Char p) {
        return bat() && coClient(p);
    }

    /**
     * Tong phan nghin TRANG TRI dang tinh (danh hieu dang deo + cac o tu do) - DatTenPetGame dung lam "trang tri khac"
     * de dong the ten pet chi lay phan con lai cua tran tong trang_tri.tran_tong_phan_nghin (5%, thiet_ke_can_bang 4.5).
     */
    public static int tongPhanNghin(final Char p) {
        try {
            if (p == null || p.isBot != -1 || !ChiSoTrangBiGame.bat()) {
                return 0;
            }
            int tong = phanNghinDanhHieu(p);
            final TT t = tt(p);
            if (t != null && bat()) {
                for (final int v : hieuLuc(p, t)) {
                    tong += v;
                }
            }
            return tong;
        } catch (final Exception e) {
            return 0;
        }
    }

    static Message moi(final int kieu) throws java.io.IOException {
        final Message m = new Message(TrangPhucLogic.LENH);
        m.dos.writeByte(kieu);
        return m;
    }

    static void ketQua(final Char p, final boolean ok, final String chu) {
        try {
            final Message m = moi(TrangPhucLogic.S_KET_QUA);
            m.dos.writeByte(ok ? 1 : 0);
            m.dos.writeUTF(chu == null ? "" : chu);
            p.sendMessage(m);
        } catch (final Exception ignored) {
        }
    }

    /** S-&gt;C kieu 0: toan bo tu do. */
    public static void guiTuDo(final Char p) {
        final TT t = tt(p);
        if (t == null || !coClient(p)) {
            return;
        }
        try {
            final int[] hl = hieuLuc(p, t);
            final List<TrangPhucLogic.OTuDo> oDs = new ArrayList<TrangPhucLogic.OTuDo>();
            int tong = phanNghinDanhHieu(p);
            final int dh = tong;
            for (int i = 0; i < TrangPhucLogic.O.length; i++) {
                final TrangPhucLogic.OTuDo o = new TrangPhucLogic.OTuDo();
                o.ma = TrangPhucLogic.O[i];
                o.ten = DL.tenO[i];
                o.hieuLuc = hl[i];
                o.goc = DL.nganSach[i];
                o.dong = DL.dong[i];
                o.dongChon = Math.max(0, java.util.Arrays.asList(o.dong).indexOf(dongChon(t, i)));
                o.dangMac = t.dangMac[i];
                o.giayConDoi = giayCon(t, i);
                oDs.add(o);
                tong += hl[i];
            }
            final List<TrangPhucLogic.MonTuDo> mon = new ArrayList<TrangPhucLogic.MonTuDo>();
            for (final Mon m : DL.mon.values()) {
                final TrangPhucLogic.MonTuDo x = new TrangPhucLogic.MonTuDo();
                x.ma = m.ma;
                x.oIdx = m.oIdx;
                x.so = m.so;
                x.ten = m.ten;
                x.doHiem = m.doHiem;
                x.icon = m.icon;
                final boolean co = t.soHuu.containsKey(m.ma);
                x.trangThai = m.ma.equals(t.dangMac[m.oIdx]) ? TrangPhucLogic.TT_DANG_MAC : co ? TrangPhucLogic.TT_DA_CO
                        : nhanDuoc(p, t, m) ? TrangPhucLogic.TT_NHAN_DUOC : TrangPhucLogic.TT_CHUA_CO;
                x.nguon = chuNguon(m);
                x.tienDo = tienDo(p, t, m);
                x.hanhDong = co ? TrangPhucLogic.HD_KHONG : x.trangThai == TrangPhucLogic.TT_NHAN_DUOC ? TrangPhucLogic.HD_NHAN
                        : TrangPhucLogic.CHE_TAO.equals(m.nguon) ? TrangPhucLogic.HD_CHE
                        : TrangPhucLogic.CUA_HANG.equals(m.nguon) ? TrangPhucLogic.HD_MUA : TrangPhucLogic.HD_KHONG;
                x.moTa = m.moTa;
                mon.add(x);
            }
            tong += DatTenPetGame.hieuLucDangTheo(p); // the ten pet dang di theo: cung tran tong trang tri
            final Message m = moi(TrangPhucLogic.S_TU_DO);
            TrangPhucLogic.ghiTuDo(m.dos, oDs, tong, DanhHieuGame.tranTrangTri(), dh, mon);
            p.sendMessage(m);
        } catch (final Exception e) {
            System.out.println("[TRANG PHUC] LOI gui tu do: " + e);
        }
    }
}
