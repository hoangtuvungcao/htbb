package com.htbb.pokemon;

import data.Database;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * CSDL cua doi luong + san luong <-> xu ({@link DoiLuong}). Bang (cung DDL voi sql/htbb.sql phan A + B, server tu tao
 * neu thieu):
 * <ul>
 *   <li>{@code tob_doi_tien_ngay}: dem tran ngay theo TAI KHOAN (loai doi_luong_xu / san_luong), tang co dieu kien trong
 *       MOT cau UPDATE nen hai luong / hai may chu khong vuot tran.</li>
 *   <li>{@code tob_doi_luong_log}: moi lan doi (khoa / xu): ai, bao nhieu, ti gia, nguon (npc / web), lenh web (UNIQUE -
 *       mot lenh web khong bao gio ap dung hai lan).</li>
 *   <li>{@code tob_ti_gia_luong_xu}: ti gia he thong moi ngay (INSERT IGNORE - dong dau tien cua ngay thang, dong bang).</li>
 *   <li>{@code tob_san_luong_lenh} / {@code tob_san_luong_khop} / {@code tob_san_luong_tra}: so lenh, lich su khop, hang
 *       doi tra tien (moi khoan CONG vao nhan vat - xu ban duoc, luong mua duoc, hoan ky quy - di qua bang tra, giao khi
 *       nhan vat online: chot trang_thai 0 -> 1 co dieu kien roi moi cong RAM).</li>
 * </ul>
 */
public final class DoiLuongDb {

    public static final String[] DDL = {
        "CREATE TABLE IF NOT EXISTS `tob_doi_tien_ngay` (`user_id` INT NOT NULL, `loai` VARCHAR(20) NOT NULL,"
            + " `ngay` DATE NOT NULL, `da_dung` BIGINT NOT NULL DEFAULT 0, PRIMARY KEY (`user_id`, `loai`, `ngay`))"
            + " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
        "CREATE TABLE IF NOT EXISTS `tob_doi_luong_log` (`id` BIGINT NOT NULL AUTO_INCREMENT, `user_id` INT NOT NULL,"
            + " `char_id` INT NOT NULL, `charname` VARCHAR(50) NOT NULL DEFAULT '', `loai` VARCHAR(20) NOT NULL,"
            + " `so_luong` INT NOT NULL, `nhan` BIGINT NOT NULL, `ti_gia` BIGINT NOT NULL DEFAULT 0,"
            + " `nguon` VARCHAR(10) NOT NULL DEFAULT 'npc', `lenh_id` BIGINT NULL, `luc` DATETIME NOT NULL,"
            + " PRIMARY KEY (`id`), UNIQUE KEY `uq_lenh` (`lenh_id`), KEY `idx_user_luc` (`user_id`, `luc`))"
            + " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
        "CREATE TABLE IF NOT EXISTS `tob_ti_gia_luong_xu` (`ngay` DATE NOT NULL, `cap_trung_vi` INT NOT NULL,"
            + " `so_nguoi` INT NOT NULL DEFAULT 0, `xu_moi_gio` BIGINT NOT NULL, `phan_tram` INT NOT NULL,"
            + " `ti_gia` BIGINT NOT NULL, `tao_luc` DATETIME NOT NULL, PRIMARY KEY (`ngay`))"
            + " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
        "CREATE TABLE IF NOT EXISTS `tob_san_luong_lenh` (`id` BIGINT NOT NULL AUTO_INCREMENT, `loai` TINYINT NOT NULL"
            + " COMMENT '1 ban 2 mua', `char_id` INT NOT NULL, `user_id` INT NOT NULL, `charname` VARCHAR(50) NOT NULL DEFAULT '',"
            + " `gia` BIGINT NOT NULL, `so_luong` INT NOT NULL, `con_lai` INT NOT NULL, `xu_ky_quy` BIGINT NOT NULL DEFAULT 0,"
            + " `trang_thai` TINYINT NOT NULL DEFAULT 0 COMMENT '0 mo 1 xong 2 huy 3 het han', `ngay_tao` DATE NOT NULL,"
            + " `tao_luc` DATETIME NOT NULL, `het_han` DATETIME NOT NULL, `xong_luc` DATETIME NULL, PRIMARY KEY (`id`),"
            + " KEY `idx_so` (`trang_thai`, `loai`, `gia`), KEY `idx_char` (`char_id`, `trang_thai`))"
            + " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
        "CREATE TABLE IF NOT EXISTS `tob_san_luong_khop` (`id` BIGINT NOT NULL AUTO_INCREMENT, `lenh_ban` BIGINT NOT NULL,"
            + " `lenh_mua` BIGINT NOT NULL, `char_ban` INT NOT NULL, `char_mua` INT NOT NULL, `so_luong` INT NOT NULL,"
            + " `gia` BIGINT NOT NULL, `xu_ban_nhan` BIGINT NOT NULL, `phi_xu` BIGINT NOT NULL, `luc` DATETIME NOT NULL,"
            + " PRIMARY KEY (`id`), KEY `idx_luc` (`luc`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
        "CREATE TABLE IF NOT EXISTS `tob_san_luong_tra` (`id` BIGINT NOT NULL AUTO_INCREMENT, `char_id` INT NOT NULL,"
            + " `xu` BIGINT NOT NULL DEFAULT 0, `luong` INT NOT NULL DEFAULT 0, `ly_do` VARCHAR(60) NOT NULL DEFAULT '',"
            + " `trang_thai` TINYINT NOT NULL DEFAULT 0, `tao_luc` DATETIME NOT NULL, `tra_luc` DATETIME NULL,"
            + " PRIMARY KEY (`id`), KEY `idx_cho` (`trang_thai`, `char_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
    };

    public static final int LOAI_BAN = 1;
    public static final int LOAI_MUA = 2;
    public static final int MO = 0;
    public static final int XONG = 1;
    public static final int HUY = 2;
    public static final int HET_HAN = 3;

    private static volatile boolean daTao = false;

    private DoiLuongDb() {
    }

    /** Mot cong viec trong giao dich. */
    public interface Viec<T> {
        T lam(Connection conn) throws Exception;
    }

    public static Connection ketNoi() throws Exception {
        return Database.instance.getConnection();
    }

    public static void tra(final Connection conn) {
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (final Exception ignored) {
        }
    }

    static void dong(final AutoCloseable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (final Exception ignored) {
        }
    }

    /** Chay {@code v} trong MOT giao dich (loi -> rollback, nem lai). */
    public static <T> T giaoDich(final Viec<T> v) throws Exception {
        final Connection conn = ketNoi();
        boolean tuDong = true;
        try {
            tuDong = conn.getAutoCommit();
            conn.setAutoCommit(false);
            final T kq = v.lam(conn);
            conn.commit();
            return kq;
        } catch (final Exception e) {
            try {
                conn.rollback();
            } catch (final Exception ignored) {
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(tuDong);
            } catch (final Exception ignored) {
            }
            tra(conn);
        }
    }

    public static void taoBang() {
        if (daTao) {
            return;
        }
        Connection conn = null;
        Statement st = null;
        try {
            conn = ketNoi();
            st = conn.createStatement();
            for (final String s : DDL) {
                st.execute(s);
            }
            daTao = true;
        } catch (final Exception e) {
            System.out.println("[DOI LUONG] LOI tao bang: " + e);
        } finally {
            dong(st);
            tra(conn);
        }
    }

    static int capNhat(final Connection conn, final String sql, final Object... ts) throws Exception {
        final PreparedStatement ps = conn.prepareStatement(sql);
        try {
            for (int i = 0; i < ts.length; i++) {
                ps.setObject(i + 1, ts[i]);
            }
            return ps.executeUpdate();
        } finally {
            dong(ps);
        }
    }

    static long chen(final Connection conn, final String sql, final Object... ts) throws Exception {
        final PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = null;
        try {
            for (int i = 0; i < ts.length; i++) {
                ps.setObject(i + 1, ts[i]);
            }
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getLong(1) : 0L;
        } finally {
            dong(rs);
            dong(ps);
        }
    }

    static List<Object[]> doc(final Connection conn, final String sql, final Object... ts) throws Exception {
        final PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = null;
        final List<Object[]> ra = new ArrayList<Object[]>();
        try {
            for (int i = 0; i < ts.length; i++) {
                ps.setObject(i + 1, ts[i]);
            }
            rs = ps.executeQuery();
            final int n = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                final Object[] r = new Object[n];
                for (int i = 0; i < n; i++) {
                    r[i] = rs.getObject(i + 1);
                }
                ra.add(r);
            }
            return ra;
        } finally {
            dong(rs);
            dong(ps);
        }
    }

    /** Doc ngoai giao dich (ket noi rieng). */
    public static List<Object[]> doc(final String sql, final Object... ts) throws Exception {
        final Connection conn = ketNoi();
        try {
            return doc(conn, sql, ts);
        } finally {
            tra(conn);
        }
    }

    public static int capNhat(final String sql, final Object... ts) throws Exception {
        final Connection conn = ketNoi();
        try {
            return capNhat(conn, sql, ts);
        } finally {
            tra(conn);
        }
    }

    static long so(final Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    // ------------------------------------------------------------------ tran ngay

    /** Da dung bao nhieu hom nay (loai, tai khoan). */
    public static long daDung(final int userId, final String loai, final LocalDate ngay) throws Exception {
        final List<Object[]> r = doc("SELECT da_dung FROM tob_doi_tien_ngay WHERE user_id = ? AND loai = ? AND ngay = ?",
                userId, loai, Date.valueOf(ngay));
        return r.isEmpty() ? 0L : so(r.get(0)[0]);
    }

    /** Giu cho {@code so} trong tran {@code tran}: true neu giu duoc (MOT cau UPDATE co dieu kien - an toan song song). */
    public static boolean giuTran(final int userId, final String loai, final LocalDate ngay, final long so, final long tran)
            throws Exception {
        if (so <= 0L || so > tran) {
            return false;
        }
        final Connection conn = ketNoi();
        try {
            capNhat(conn, "INSERT IGNORE INTO tob_doi_tien_ngay (user_id, loai, ngay, da_dung) VALUES (?, ?, ?, 0)",
                    userId, loai, Date.valueOf(ngay));
            return capNhat(conn, "UPDATE tob_doi_tien_ngay SET da_dung = da_dung + ? WHERE user_id = ? AND loai = ?"
                    + " AND ngay = ? AND da_dung + ? <= ?", so, userId, loai, Date.valueOf(ngay), so, tran) == 1;
        } finally {
            tra(conn);
        }
    }

    /** Tra lai phan da giu (giao dich hong / huy lenh cung ngay). */
    public static void traTran(final int userId, final String loai, final LocalDate ngay, final long so) {
        if (so <= 0L) {
            return;
        }
        try {
            capNhat("UPDATE tob_doi_tien_ngay SET da_dung = GREATEST(0, da_dung - ?) WHERE user_id = ? AND loai = ? AND ngay = ?",
                    so, userId, loai, Date.valueOf(ngay));
        } catch (final Exception e) {
            System.out.println("[DOI LUONG] LOI tra tran " + loai + " user " + userId + ": " + e);
        }
    }

    // ------------------------------------------------------------------ nhat ky doi

    /** Ghi mot lan doi. lenhId > 0: UNIQUE - lan thu hai cung lenh nem loi. */
    public static void ghiDoi(final int userId, final int charId, final String charname, final String loai, final int soLuong,
            final long nhan, final long tiGia, final String nguon, final long lenhId) throws Exception {
        capNhat("INSERT INTO tob_doi_luong_log (user_id, char_id, charname, loai, so_luong, nhan, ti_gia, nguon, lenh_id, luc)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", userId, charId, charname == null ? "" : charname, loai, soLuong,
                nhan, tiGia, nguon, lenhId > 0L ? Long.valueOf(lenhId) : null, new Timestamp(System.currentTimeMillis()));
    }

    // ------------------------------------------------------------------ ti gia

    /** {cap_trung_vi, so_nguoi, xu_moi_gio, phan_tram, ti_gia} cua ngay, null neu chua co. */
    public static long[] tiGiaNgay(final LocalDate ngay) throws Exception {
        final List<Object[]> r = doc("SELECT cap_trung_vi, so_nguoi, xu_moi_gio, phan_tram, ti_gia FROM tob_ti_gia_luong_xu"
                + " WHERE ngay = ?", Date.valueOf(ngay));
        if (r.isEmpty()) {
            return null;
        }
        final Object[] d = r.get(0);
        return new long[]{so(d[0]), so(d[1]), so(d[2]), so(d[3]), so(d[4])};
    }

    /** Cap (lastLv) nhan vat THAT (khong bot, khong khoa, chua xoa) hoat dong 7 ngay. */
    public static int[] capHoatDong(final long bayGioMs) throws Exception {
        final Timestamp tu = new Timestamp(bayGioMs - 7L * 86400000L);
        final String tk = real.cmd.DangKyTrongGame.csdlTaiKhoan;
        List<Object[]> r;
        try {
            r = doc("SELECT c.lastLv FROM tob_char c WHERE c.lastLog >= ? AND c.del = 1 AND c.ban = 0"
                    + " AND c.userId NOT IN (SELECT id FROM `" + tk + "`.team_user WHERE la_bot = 1)", tu);
        } catch (final Exception thieuCot) {
            // CSDL chua co cot la_bot = chua co bot
            r = doc("SELECT c.lastLv FROM tob_char c WHERE c.lastLog >= ? AND c.del = 1 AND c.ban = 0", tu);
        }
        final int[] cap = new int[r.size()];
        for (int i = 0; i < cap.length; i++) {
            cap[i] = (int) so(r.get(i)[0]);
        }
        return cap;
    }

    /** Ghi ti gia ngay (INSERT IGNORE: da co dong thi giu nguyen - dong bang ca ngay). */
    public static void ghiTiGia(final LocalDate ngay, final int capTrungVi, final int soNguoi, final long xuMoiGio,
            final long phanTram, final long tiGia) throws Exception {
        capNhat("INSERT IGNORE INTO tob_ti_gia_luong_xu (ngay, cap_trung_vi, so_nguoi, xu_moi_gio, phan_tram, ti_gia, tao_luc)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)", Date.valueOf(ngay), capTrungVi, soNguoi, xuMoiGio, phanTram, tiGia,
                new Timestamp(System.currentTimeMillis()));
    }

    // ------------------------------------------------------------------ san: doc

    /** Lenh mo cua nhan vat: {id, loai, gia, so_luong, con_lai, het_han}. */
    public static List<Object[]> lenhMoCua(final int charId) throws Exception {
        return doc("SELECT id, loai, gia, so_luong, con_lai, het_han FROM tob_san_luong_lenh WHERE char_id = ? AND trang_thai = 0"
                + " ORDER BY id", charId);
    }

    /** So lenh: {gia, tong con_lai} nhom theo gia, ban tang dan / mua giam dan, toi da n muc. */
    public static List<Object[]> soLenh(final int loai, final int n) throws Exception {
        return doc("SELECT gia, SUM(con_lai) FROM tob_san_luong_lenh WHERE trang_thai = 0 AND loai = ? GROUP BY gia ORDER BY gia "
                + (loai == LOAI_BAN ? "ASC" : "DESC") + " LIMIT " + Math.max(1, Math.min(20, n)), loai);
    }

    /** {tong xu, tong luong, so lan} khop trong 24 gio qua. */
    public static long[] khop24h() throws Exception {
        final List<Object[]> r = doc("SELECT COALESCE(SUM(gia * so_luong), 0), COALESCE(SUM(so_luong), 0), COUNT(*)"
                + " FROM tob_san_luong_khop WHERE luc >= ?", new Timestamp(System.currentTimeMillis() - 86400000L));
        return r.isEmpty() ? new long[3] : new long[]{so(r.get(0)[0]), so(r.get(0)[1]), so(r.get(0)[2])};
    }

    // ------------------------------------------------------------------ san: tra tien

    static void themTra(final Connection conn, final int charId, final long xu, final long luong, final String lyDo)
            throws Exception {
        if (xu <= 0L && luong <= 0L) {
            return;
        }
        capNhat(conn, "INSERT INTO tob_san_luong_tra (char_id, xu, luong, ly_do, trang_thai, tao_luc) VALUES (?, ?, ?, ?, 0, ?)",
                charId, xu, luong, lyDo, new Timestamp(System.currentTimeMillis()));
    }

    /** Cac khoan cho tra cua nhan vat: {id, xu, luong, ly_do}. */
    public static List<Object[]> traCho(final int charId) throws Exception {
        return doc("SELECT id, xu, luong, ly_do FROM tob_san_luong_tra WHERE char_id = ? AND trang_thai = 0 ORDER BY id LIMIT 50",
                charId);
    }

    /** Nhan vat co khoan cho tra (cho vong quet). */
    public static List<Integer> nguoiChoTra() throws Exception {
        final List<Integer> ra = new ArrayList<Integer>();
        for (final Object[] r : doc("SELECT DISTINCT char_id FROM tob_san_luong_tra WHERE trang_thai = 0 LIMIT 500")) {
            ra.add((int) so(r[0]));
        }
        return ra;
    }

    /** Chot mot khoan tra 0 -> 1: true = luong goi duoc quyen cong (chi MOT lan). */
    public static boolean chotTra(final long id) throws Exception {
        return capNhat("UPDATE tob_san_luong_tra SET trang_thai = 1, tra_luc = ? WHERE id = ? AND trang_thai = 0",
                new Timestamp(System.currentTimeMillis()), id) == 1;
    }

    /** Bo chot (cong RAM loi) de lan sau thu lai. */
    public static void boChotTra(final long id) {
        try {
            capNhat("UPDATE tob_san_luong_tra SET trang_thai = 0, tra_luc = NULL WHERE id = ? AND trang_thai = 1", id);
        } catch (final Exception e) {
            System.out.println("[SAN LUONG] LOI bo chot tra #" + id + ": " + e);
        }
    }
}
