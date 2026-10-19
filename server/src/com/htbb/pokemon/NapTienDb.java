package com.htbb.pokemon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Truy cap CSDL cua nap tien (bang nap_* trong tools/luu_tru_sinh_du_lieu/sql_nguon/pokemon_nap_tien.sql). CHI PreparedStatement.
 * Moi buoc doi trang thai la mot cau UPDATE co dieu kien tren trang thai cu ("cong" chi
 * thanh cong cho DUNG MOT luong goi) - khong dua vao khoa hang, khong transaction: chay y
 * het tren MariaDB va NovaDB. So cai co UNIQUE(ma_tham_chieu) lam chot cuoi.
 *
 * Moc thoi gian do Java ghi (gio may chu, +07 tren VPS) - NOW() cua NovaDB la UTC.
 */
public final class NapTienDb {

    private NapTienDb() {
    }

    /** Ket noi: mac dinh lay tu pool cua game; test thay bang ket noi rieng. */
    public interface Nguon {
        Connection lay() throws SQLException;

        void tra(Connection c);
    }

    static volatile Nguon nguon = new Nguon() {
        @Override
        public Connection lay() throws SQLException {
            return data.Database.instance.getConnection();
        }

        @Override
        public void tra(final Connection c) {
            try {
                if (c != null) {
                    data.Database.connPool.free(c);
                }
            } catch (final Exception ignored) {
            }
        }
    };

    /** Chi test goi. */
    public static void datNguon(final Nguon n) {
        nguon = n;
    }

    // ------------------------------------------------------------------ tien ich

    public static String bayGio() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static String homNay() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    interface Viec<T> {
        T lam(Connection c) throws SQLException;
    }

    static <T> T chay(final Viec<T> v) throws SQLException {
        final Connection c = nguon.lay();
        try {
            return v.lam(c);
        } finally {
            nguon.tra(c);
        }
    }

    static int capNhat(final String sql, final Object... thamSo) throws SQLException {
        return chay(new Viec<Integer>() {
            @Override
            public Integer lam(final Connection c) throws SQLException {
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    dat(ps, thamSo);
                    return ps.executeUpdate();
                }
            }
        });
    }

    static void dat(final PreparedStatement ps, final Object... thamSo) throws SQLException {
        for (int i = 0; i < thamSo.length; i++) {
            final Object o = thamSo[i];
            if (o instanceof Integer) {
                ps.setInt(i + 1, (Integer) o);
            } else if (o instanceof Long) {
                ps.setLong(i + 1, (Long) o);
            } else {
                ps.setString(i + 1, o == null ? null : String.valueOf(o));
            }
        }
    }

    static List<Object[]> doc(final String sql, final int soCot, final Object... thamSo) throws SQLException {
        return chay(new Viec<List<Object[]>>() {
            @Override
            public List<Object[]> lam(final Connection c) throws SQLException {
                final List<Object[]> ra = new ArrayList<Object[]>();
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    dat(ps, thamSo);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            final Object[] d = new Object[soCot];
                            for (int i = 0; i < soCot; i++) {
                                d[i] = rs.getObject(i + 1);
                            }
                            ra.add(d);
                        }
                    }
                }
                return ra;
            }
        });
    }

    static long so(final Object o) {
        if (o == null) {
            return 0L;
        }
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(o).trim());
        } catch (final NumberFormatException e) {
            return 0L;
        }
    }

    static String chu(final Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    /** Loi trung khoa (MariaDB 1062 / NovaDB "UNIQUE constraint failed"). */
    static boolean laTrungKhoa(final SQLException e) {
        final String m = String.valueOf(e.getMessage());
        return e.getErrorCode() == 1062 || m.contains("Duplicate entry") || m.contains("UNIQUE constraint");
    }

    // ------------------------------------------------------------------ bang

    private static volatile long kiemLuc = 0L;
    private static volatile boolean coBangNho = false;

    /** Co du bang chua (nho 60 giay). */
    public static boolean coBang() {
        final long t = System.currentTimeMillis();
        if (t - kiemLuc < 60000L) {
            return coBangNho;
        }
        boolean co;
        try {
            doc("SELECT 1 FROM nap_don LIMIT 1", 1);
            doc("SELECT 1 FROM nap_ve_mua LIMIT 1", 1);
            doc("SELECT 1 FROM nap_mua_dem LIMIT 1", 1);
            co = true;
        } catch (final SQLException e) {
            co = false;
        }
        coBangNho = co;
        kiemLuc = t;
        return co;
    }

    // ------------------------------------------------------------------ don

    /** Mot don da thanh toan cho server cong. */
    public static final class Don {
        public long id;
        public String maDon, maGoi, loai;
        public int userId, charId, luong, soTien;
    }

    public static List<Don> donChoCong(final int toiDa) throws SQLException {
        final List<Don> ra = new ArrayList<Don>();
        for (final Object[] d : doc("SELECT id, ma_don, user_id, char_id, ma_goi, loai, luong, so_tien FROM nap_don"
                + " WHERE trang_thai = ? ORDER BY id LIMIT " + Math.max(1, toiDa), 8, NapTienLogic.DA_THANH_TOAN)) {
            ra.add(don(d));
        }
        return ra;
    }

    public static List<Don> donChoThuHoi(final int toiDa) throws SQLException {
        final List<Don> ra = new ArrayList<Don>();
        for (final Object[] d : doc("SELECT id, ma_don, user_id, char_cong, ma_goi, loai, luong, so_tien FROM nap_don"
                + " WHERE trang_thai = ? ORDER BY id LIMIT " + Math.max(1, toiDa), 8, NapTienLogic.CHO_THU_HOI)) {
            ra.add(don(d));
        }
        return ra;
    }

    static Don don(final Object[] d) {
        final Don x = new Don();
        x.id = so(d[0]);
        x.maDon = chu(d[1]);
        x.userId = (int) so(d[2]);
        x.charId = (int) so(d[3]);
        x.maGoi = chu(d[4]);
        x.loai = chu(d[5]);
        x.luong = (int) so(d[6]);
        x.soTien = (int) so(d[7]);
        return x;
    }

    /** CHOT IDEMPOTENT cua viec cong: da_thanh_toan -> da_cong. true = DUNG lan nay doi. */
    public static boolean danhDauDaCong(final long id, final int charId) throws SQLException {
        return capNhat("UPDATE nap_don SET trang_thai = ?, cong_luc = ?, char_cong = ? WHERE id = ? AND trang_thai = ?",
                NapTienLogic.DA_CONG, bayGio(), charId, id, NapTienLogic.DA_THANH_TOAN) == 1;
    }

    /** Hoan tac chot cong (chi khi chua dong gi vao nhan vat - vd the thang bi tu choi). */
    public static boolean hoanTacCong(final long id, final String lyDo) throws SQLException {
        return capNhat("UPDATE nap_don SET trang_thai = ?, cong_luc = NULL, char_cong = 0, ly_do = ? WHERE id = ? AND trang_thai = ?",
                NapTienLogic.DA_THANH_TOAN, cat(lyDo, 250), id, NapTienLogic.DA_CONG) == 1;
    }

    public static void danhDauDaLuu(final long id) throws SQLException {
        capNhat("UPDATE nap_don SET da_luu_nv = 1 WHERE id = ?", id);
    }

    /** CHOT IDEMPOTENT cua thu hoi: cho_thu_hoi -> da_thu_hoi. */
    public static boolean danhDauDaThuHoi(final long id) throws SQLException {
        return capNhat("UPDATE nap_don SET trang_thai = ? WHERE id = ? AND trang_thai = ?",
                NapTienLogic.DA_THU_HOI, id, NapTienLogic.CHO_THU_HOI) == 1;
    }

    // ------------------------------------------------------------------ so cai / nhat ky

    /** Ghi so cai. false = ma_tham_chieu da co (da ghi truoc do) - KHONG phai loi. */
    public static boolean ghiSoCai(final int userId, final int charId, final String loai, final long soLuong,
            final int soTien, final String maDon, final String thamChieu, final String ghiChu) throws SQLException {
        try {
            capNhat("INSERT INTO nap_so_cai (user_id, char_id, loai, so_luong, so_tien, ma_don, ma_tham_chieu, ghi_chu, tao_luc)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", userId, charId, loai, soLuong, soTien, cat(maDon, 16),
                    cat(thamChieu, 80), cat(ghiChu, 250), bayGio());
            return true;
        } catch (final SQLException e) {
            if (laTrungKhoa(e)) {
                return false;
            }
            throw e;
        }
    }

    public static void ghiNhatKy(final String hanhDong, final String maDon, final int userId, final String chiTiet) {
        try {
            capNhat("INSERT INTO nap_nhat_ky (tac_nhan, hanh_dong, ma_don, user_id, chi_tiet, ip, tao_luc) VALUES (?, ?, ?, ?, ?, '', ?)",
                    "server", cat(hanhDong, 40), cat(maDon, 16), userId, cat(chiTiet, 500), bayGio());
        } catch (final SQLException e) {
            System.out.println("NAP TIEN: loi ghi nhat ky - " + e.getMessage());
        }
    }

    static String cat(final String s, final int n) {
        if (s == null) {
            return "";
        }
        return s.length() <= n ? s : s.substring(0, n);
    }

    // ------------------------------------------------------------------ vi / no

    static void damBaoVi(final int userId) throws SQLException {
        capNhat("INSERT IGNORE INTO nap_vi (user_id, cap_nhat_luc) VALUES (?, ?)", userId, bayGio());
    }

    public static long noLuong(final int userId) throws SQLException {
        final List<Object[]> r = doc("SELECT no_luong FROM nap_vi WHERE user_id = ?", 1, userId);
        return r.isEmpty() ? 0L : so(r.get(0)[0]);
    }

    /** Tru no co dieu kien (khong bao gio am). */
    public static boolean truNo(final int userId, final long soLuong) throws SQLException {
        if (soLuong <= 0L) {
            return true;
        }
        return capNhat("UPDATE nap_vi SET no_luong = no_luong - ?, cap_nhat_luc = ? WHERE user_id = ? AND no_luong >= ?",
                soLuong, bayGio(), userId, soLuong) == 1;
    }

    public static void congNo(final int userId, final long soLuong) throws SQLException {
        if (soLuong <= 0L) {
            return;
        }
        damBaoVi(userId);
        capNhat("UPDATE nap_vi SET no_luong = no_luong + ?, cap_nhat_luc = ? WHERE user_id = ?", soLuong, bayGio(), userId);
    }

    /** Thuong lan nap dau: true DUNG MOT lan / tai khoan. */
    public static boolean nhanThuongDau(final int userId) throws SQLException {
        damBaoVi(userId);
        return capNhat("UPDATE nap_vi SET thuong_dau = 1 WHERE user_id = ? AND thuong_dau = 0", userId) == 1;
    }

    /** user_id -> no (chi tai khoan dang no). */
    public static Map<Integer, Long> taiKhoanDangNo() throws SQLException {
        final Map<Integer, Long> ra = new HashMap<Integer, Long>();
        for (final Object[] d : doc("SELECT user_id, no_luong FROM nap_vi WHERE no_luong > 0", 2)) {
            ra.put((int) so(d[0]), so(d[1]));
        }
        return ra;
    }

    // ------------------------------------------------------------------ the thang

    /** {het_han_ms, ngay_nhan_cuoi} - khong co dong = {0, ""}. */
    public static Object[] theThang(final int charId) throws SQLException {
        final List<Object[]> r = doc("SELECT het_han_ms, ngay_nhan_cuoi FROM nap_the_thang WHERE char_id = ?", 2, charId);
        return r.isEmpty() ? new Object[]{0L, ""} : new Object[]{so(r.get(0)[0]), chu(r.get(0)[1])};
    }

    /** Doi han the co dieu kien (han cu phai dung nhu da doc - chong hai don cung luc). */
    public static boolean doiHanThe(final int charId, final int userId, final long hanCu, final long hanMoi) throws SQLException {
        capNhat("INSERT IGNORE INTO nap_the_thang (char_id, user_id, het_han_ms, cap_nhat_luc) VALUES (?, ?, 0, ?)",
                charId, userId, bayGio());
        return capNhat("UPDATE nap_the_thang SET het_han_ms = ?, user_id = ?, cap_nhat_luc = ? WHERE char_id = ? AND het_han_ms = ?",
                hanMoi, userId, bayGio(), charId, hanCu) == 1;
    }

    /** Chot nhan luong ngay: true DUNG MOT lan / nhan vat / ngay, chi khi con han. */
    public static boolean nhanNgay(final int charId, final String homNay, final long bayGioMs) throws SQLException {
        return capNhat("UPDATE nap_the_thang SET ngay_nhan_cuoi = ?, so_ngay_da_nhan = so_ngay_da_nhan + 1, cap_nhat_luc = ?"
                + " WHERE char_id = ? AND ngay_nhan_cuoi <> ? AND het_han_ms > ?", homNay, bayGio(), charId, homNay, bayGioMs) == 1;
    }

    // ------------------------------------------------------------------ ve mua

    /** {cao_cap, diem, diem_ngay, ngay, bac_free, bac_cao_cap}; tao dong neu chua co. */
    public static Object[] veMua(final int charId, final int mua) throws SQLException {
        capNhat("INSERT IGNORE INTO nap_ve_mua (char_id, mua) VALUES (?, ?)", charId, mua);
        final List<Object[]> r = doc("SELECT cao_cap, diem, diem_ngay, ngay, bac_free, bac_cao_cap FROM nap_ve_mua"
                + " WHERE char_id = ? AND mua = ?", 6, charId, mua);
        if (r.isEmpty()) {
            return new Object[]{0, 0, 0, "", 0, 0};
        }
        final Object[] d = r.get(0);
        return new Object[]{(int) so(d[0]), (int) so(d[1]), (int) so(d[2]), chu(d[3]), (int) so(d[4]), (int) so(d[5])};
    }

    /** Cong diem co tran ngay. Lac quan: doc -> tinh -> UPDATE co dieu kien, thu lai 3 lan. @return diem thuc cong. */
    public static int congDiem(final int charId, final int mua, final int them, final String homNay, final int tranNgay)
            throws SQLException {
        for (int lan = 0; lan < 3; lan++) {
            final Object[] v = veMua(charId, mua);
            final int diem = (Integer) v[1];
            final int diemNgayCu = (Integer) v[2];
            final String ngay = (String) v[3];
            final int diemNgay = homNay.equals(ngay) ? diemNgayCu : 0;
            final int duoc = NapTienLogic.diemDuocCong(diemNgay, them, tranNgay);
            if (duoc <= 0) {
                return 0;
            }
            if (capNhat("UPDATE nap_ve_mua SET diem = ?, diem_ngay = ?, ngay = ? WHERE char_id = ? AND mua = ? AND diem = ? AND ngay = ?",
                    diem + duoc, diemNgay + duoc, homNay, charId, mua, diem, ngay) == 1) {
                return duoc;
            }
        }
        return 0;
    }

    public static boolean datCaoCap(final int charId, final int mua, final int caoCap) throws SQLException {
        veMua(charId, mua);
        return capNhat("UPDATE nap_ve_mua SET cao_cap = ? WHERE char_id = ? AND mua = ? AND cao_cap <> ?",
                caoCap, charId, mua, caoCap) == 1;
    }

    /** Chot nhan bac: cot = bac_free | bac_cao_cap, tu cu len moi. */
    public static boolean nhanBac(final int charId, final int mua, final boolean caoCap, final int cu, final int moi)
            throws SQLException {
        final String cot = caoCap ? "bac_cao_cap" : "bac_free";
        return capNhat("UPDATE nap_ve_mua SET " + cot + " = ? WHERE char_id = ? AND mua = ? AND " + cot + " = ?",
                moi, charId, mua, cu) == 1;
    }

    // ------------------------------------------------------------------ cua hang

    private static volatile List<NapTienLogic.Mon> cuaHangNho = null;
    private static volatile long cuaHangLuc = 0L;

    /** Danh muc (nho 60 giay). Dong sai luat bi bo qua (kiemMon). Bang rong -> MON_MAC_DINH. */
    public static List<NapTienLogic.Mon> cuaHang() throws SQLException {
        final long t = System.currentTimeMillis();
        final List<NapTienLogic.Mon> nho = cuaHangNho;
        if (nho != null && t - cuaHangLuc < 60000L) {
            return nho;
        }
        final List<NapTienLogic.Mon> ra = new ArrayList<NapTienLogic.Mon>();
        for (final Object[] d : doc("SELECT ma, ten, nhom, trao, gia_luong, han_ngay, gioi_han, nhom_xoay, giao_dich_duoc,"
                + " co_chi_so FROM nap_cua_hang WHERE dang_ban = 1 ORDER BY thu_tu, ma", 10)) {
            final NapTienLogic.Mon m = new NapTienLogic.Mon(chu(d[0]), chu(d[1]), chu(d[2]), chu(d[3]), (int) so(d[4]),
                    (int) so(d[5]), (int) so(d[6]), (int) so(d[7]), so(d[8]) == 1, so(d[9]) != 0);
            final String loi = NapTienLogic.kiemMon(m);
            if (loi == null) {
                ra.add(m);
            } else {
                System.out.println("NAP TIEN: bo qua mon cua hang '" + m.ma + "': " + loi);
            }
        }
        cuaHangNho = ra;
        cuaHangLuc = t;
        return ra;
    }

    /** {so_lan, lan_cuoi_ms}. */
    public static long[] demMua(final int charId, final String ma) throws SQLException {
        final List<Object[]> r = doc("SELECT so_lan, lan_cuoi_ms FROM nap_mua_dem WHERE char_id = ? AND ma = ?", 2, charId, ma);
        return r.isEmpty() ? new long[]{0L, 0L} : new long[]{so(r.get(0)[0]), so(r.get(0)[1])};
    }

    /** Tang dem mua co dieu kien (so_lan cu phai dung) - chong vuot gioi han khi bam nhanh. */
    public static boolean ghiMua(final int charId, final String ma, final long soLanCu, final long bayGioMs) throws SQLException {
        capNhat("INSERT IGNORE INTO nap_mua_dem (char_id, ma, so_lan, lan_cuoi_ms) VALUES (?, ?, 0, 0)", charId, ma);
        return capNhat("UPDATE nap_mua_dem SET so_lan = so_lan + 1, lan_cuoi_ms = ? WHERE char_id = ? AND ma = ? AND so_lan = ?",
                bayGioMs, charId, ma, soLanCu) == 1;
    }

    /** Tra lai dem khi trao that bai sau khi da ghi. */
    public static void hoanMua(final int charId, final String ma) throws SQLException {
        capNhat("UPDATE nap_mua_dem SET so_lan = so_lan - 1 WHERE char_id = ? AND ma = ? AND so_lan > 0", charId, ma);
    }

    // ------------------------------------------------------------------ so huu

    public static final class SoHuu {
        public long id;
        public String ma, nguon;
        public boolean giaoDichDuoc, dangDung, dangRao;
        public long hetHanMs;
    }

    public static long themSoHuu(final int charId, final String ma, final String nguon, final boolean giaoDichDuoc,
            final long hetHanMs) throws SQLException {
        return chay(new Viec<Long>() {
            @Override
            public Long lam(final Connection c) throws SQLException {
                final String now = bayGio();
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO nap_so_huu (char_id, ma, nguon, giao_dich_duoc,"
                        + " het_han_ms, tao_luc) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    dat(ps, charId, cat(ma, 40), cat(nguon, 16), giaoDichDuoc ? 1 : 0, hetHanMs, now);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs != null && rs.next()) {
                            final long id = rs.getLong(1);
                            if (id > 0L) {
                                return id;
                            }
                        }
                    } catch (final SQLException ignored) {
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT MAX(id) FROM nap_so_huu WHERE char_id = ? AND ma = ?")) {
                    dat(ps, charId, ma);
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next() ? rs.getLong(1) : 0L;
                    }
                }
            }
        });
    }

    /** Mon con han cua nhan vat (moi nhat truoc). */
    public static List<SoHuu> soHuu(final int charId, final long bayGioMs) throws SQLException {
        final List<SoHuu> ra = new ArrayList<SoHuu>();
        for (final Object[] d : doc("SELECT id, ma, nguon, giao_dich_duoc, dang_dung, dang_rao, het_han_ms FROM nap_so_huu"
                + " WHERE char_id = ? AND (het_han_ms = 0 OR het_han_ms > ?) ORDER BY id DESC", 7, charId, bayGioMs)) {
            final SoHuu s = new SoHuu();
            s.id = so(d[0]);
            s.ma = chu(d[1]);
            s.nguon = chu(d[2]);
            s.giaoDichDuoc = so(d[3]) == 1;
            s.dangDung = so(d[4]) == 1;
            s.dangRao = so(d[5]) == 1;
            s.hetHanMs = so(d[6]);
            ra.add(s);
        }
        return ra;
    }

    public static boolean datDung(final long id, final int charId, final boolean dung) throws SQLException {
        return capNhat("UPDATE nap_so_huu SET dang_dung = ? WHERE id = ? AND char_id = ? AND dang_rao = 0",
                dung ? 1 : 0, id, charId) == 1;
    }

    // ------------------------------------------------------------------ cho trang tri

    public static final class Rao {
        public long id, soHuuId, gia;
        public String ma, tenNguoiBan;
        public int nguoiBan;
    }

    /** Gia 20 giao dich gan nhat cua mot ma (tham chieu bien gia). */
    public static List<Long> giaGanDay(final String ma) throws SQLException {
        final List<Long> ra = new ArrayList<Long>();
        for (final Object[] d : doc("SELECT gia_xu FROM nap_cho WHERE ma = ? AND trang_thai = 'da_ban' ORDER BY id DESC LIMIT "
                + NapTienLogic.SO_GIAO_DICH_THAM_CHIEU, 1, ma)) {
            ra.add(so(d[0]));
        }
        return ra;
    }

    public static int soDangRao(final int charId) throws SQLException {
        final List<Object[]> r = doc("SELECT COUNT(*) FROM nap_cho WHERE nguoi_ban = ? AND trang_thai = 'dang_ban'", 1, charId);
        return r.isEmpty() ? 0 : (int) so(r.get(0)[0]);
    }

    /** Rao ban: chot nap_so_huu.dang_rao 0 -> 1 roi them dong cho. false = mon khong rao duoc. */
    public static boolean rao(final long soHuuId, final int charId, final String tenNguoiBan, final String ma, final long gia,
            final long phi) throws SQLException {
        if (capNhat("UPDATE nap_so_huu SET dang_rao = 1, dang_dung = 0 WHERE id = ? AND char_id = ? AND dang_rao = 0"
                + " AND giao_dich_duoc = 1 AND het_han_ms = 0", soHuuId, charId) != 1) {
            return false;
        }
        try {
            capNhat("INSERT INTO nap_cho (so_huu_id, ma, nguoi_ban, ten_nguoi_ban, gia_xu, phi_dang, tao_luc)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?)", soHuuId, ma, charId, cat(tenNguoiBan, 50), gia, phi, bayGio());
            return true;
        } catch (final SQLException e) {
            capNhat("UPDATE nap_so_huu SET dang_rao = 0 WHERE id = ?", soHuuId);
            throw e;
        }
    }

    public static boolean huyRao(final long soHuuId, final int charId) throws SQLException {
        if (capNhat("UPDATE nap_cho SET trang_thai = 'huy' WHERE so_huu_id = ? AND nguoi_ban = ? AND trang_thai = 'dang_ban'",
                soHuuId, charId) < 1) {
            return false;
        }
        capNhat("UPDATE nap_so_huu SET dang_rao = 0 WHERE id = ? AND char_id = ?", soHuuId, charId);
        return true;
    }

    /** Ma dang co nguoi rao: {ma, so dong, gia thap nhat}. */
    public static List<Object[]> maDangRao() throws SQLException {
        final List<Object[]> ra = new ArrayList<Object[]>();
        for (final Object[] d : doc("SELECT ma, COUNT(*), MIN(gia_xu) FROM nap_cho WHERE trang_thai = 'dang_ban'"
                + " GROUP BY ma ORDER BY ma LIMIT 40", 3)) {
            ra.add(new Object[]{chu(d[0]), so(d[1]), so(d[2])});
        }
        return ra;
    }

    public static List<Rao> raoTheoMa(final String ma) throws SQLException {
        final List<Rao> ra = new ArrayList<Rao>();
        for (final Object[] d : doc("SELECT id, so_huu_id, gia_xu, ma, ten_nguoi_ban, nguoi_ban FROM nap_cho"
                + " WHERE ma = ? AND trang_thai = 'dang_ban' ORDER BY gia_xu, id LIMIT 15", 6, ma)) {
            final Rao r = new Rao();
            r.id = so(d[0]);
            r.soHuuId = so(d[1]);
            r.gia = so(d[2]);
            r.ma = chu(d[3]);
            r.tenNguoiBan = chu(d[4]);
            r.nguoiBan = (int) so(d[5]);
            ra.add(r);
        }
        return ra;
    }

    /** CHOT mua: dang_ban -> da_ban (khong tu mua cua minh). true = nguoi nay mua duoc. */
    public static boolean chotMua(final long raoId, final int nguoiMua, final long thue) throws SQLException {
        return capNhat("UPDATE nap_cho SET trang_thai = 'da_ban', nguoi_mua = ?, thue = ?, ban_luc = ? WHERE id = ?"
                + " AND trang_thai = 'dang_ban' AND nguoi_ban <> ?", nguoiMua, thue, bayGio(), raoId, nguoiMua) == 1;
    }

    public static void chuyenSoHuu(final long soHuuId, final int nguoiMua) throws SQLException {
        capNhat("UPDATE nap_so_huu SET char_id = ?, dang_rao = 0, dang_dung = 0, nguon = 'cho' WHERE id = ?", nguoiMua, soHuuId);
    }

    /** {id, gia, thue} cac lan ban chua tra xu cho nguoi ban. */
    public static List<long[]> chuaTra(final int nguoiBan) throws SQLException {
        final List<long[]> ra = new ArrayList<long[]>();
        for (final Object[] d : doc("SELECT id, gia_xu, thue FROM nap_cho WHERE nguoi_ban = ? AND trang_thai = 'da_ban'"
                + " AND da_tra = 0", 3, nguoiBan)) {
            ra.add(new long[]{so(d[0]), so(d[1]), so(d[2])});
        }
        return ra;
    }

    /** nguoi_ban co tien cho tra (de vong quet khong hoi tung nhan vat). */
    public static List<Integer> nguoiBanChoTra() throws SQLException {
        final List<Integer> ra = new ArrayList<Integer>();
        for (final Object[] d : doc("SELECT DISTINCT nguoi_ban FROM nap_cho WHERE trang_thai = 'da_ban' AND da_tra = 0 LIMIT 500", 1)) {
            ra.add((int) so(d[0]));
        }
        return ra;
    }

    public static boolean chotTra(final long raoId) throws SQLException {
        return capNhat("UPDATE nap_cho SET da_tra = 1 WHERE id = ? AND da_tra = 0", raoId) == 1;
    }

    // ------------------------------------------------------------------ cau hinh / trang thai

    public static void ghiCauHinh(final String khoa, final String giaTri) {
        try {
            capNhat("INSERT IGNORE INTO nap_cau_hinh (khoa, gia_tri, cap_nhat_luc) VALUES (?, ?, ?)", khoa, giaTri, bayGio());
            capNhat("UPDATE nap_cau_hinh SET gia_tri = ?, cap_nhat_luc = ? WHERE khoa = ?", cat(giaTri, 255), bayGio(), khoa);
        } catch (final SQLException e) {
            System.out.println("NAP TIEN: loi ghi cau hinh " + khoa + " - " + e.getMessage());
        }
    }
}
