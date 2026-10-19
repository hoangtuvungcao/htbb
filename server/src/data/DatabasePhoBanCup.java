package data;

import com.htbb.pokemon.CupLogic;
import com.htbb.pokemon.PhoBanLogic;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * CSDL Pho ban Ham nguc + Cup PvP tuan (0.9.7). Bang du lieu (data_*) va bang ghi (tob_*) deu co trong
 * sql/them_pho_ban_cup.sql (idempotent); may chu tu tao bang tob_* neu thieu. Moi ham nuot loi (in ra): CSDL loi
 * thi pho ban / cup dung du lieu mac dinh trong PhoBanLogic / CupLogic, khong lam chet luong ban do.
 *
 * <ul>
 *   <li>data_pho_ban_ngay / data_pho_ban_tang - chu de theo thu, tang (doc moi 60 giay qua real.PhoBanGame).</li>
 *   <li>data_cup_loai / data_cup_lich - loai cup + lich xoay 16 tuan.</li>
 *   <li>tob_pho_ban_luot (ngay, char_id) - luot da vao trong ngay (gioi han luot ngay song qua khoi dong lai).</li>
 *   <li>tob_pho_ban_lich_su - moi lan vao (doi, chu de, tang, ket qua, giay) cho admin / bang xep hang.</li>
 *   <li>tob_cup_dang_ky / tob_cup_tran - dang ky + ket qua tung tran (cup chay tiep sau khoi dong lai giua vong).</li>
 *   <li>tob_cup_thuong (tuan, char_id) PRIMARY KEY - ghi TRUOC khi phat: khong bao gio phat hai lan.</li>
 * </ul>
 */
public final class DatabasePhoBanCup {

    private static volatile boolean daTaoBang = false;

    private DatabasePhoBanCup() {
    }

    public static final String[] SQL_TAO_BANG = {
        "CREATE TABLE IF NOT EXISTS tob_pho_ban_luot (ngay INT NOT NULL, char_id INT NOT NULL,"
            + " luot INT NOT NULL DEFAULT 0, cap_nhat DATETIME NULL,"
            + " PRIMARY KEY (ngay, char_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_pho_ban_lich_su (id BIGINT NOT NULL AUTO_INCREMENT, luc DATETIME NOT NULL,"
            + " ngay INT NOT NULL, chu_de VARCHAR(32) NOT NULL DEFAULT '', cap INT NOT NULL DEFAULT 0,"
            + " so_nguoi TINYINT NOT NULL DEFAULT 0, thanh_vien VARCHAR(255) NOT NULL DEFAULT '',"
            + " tang TINYINT NOT NULL DEFAULT 0, ket_qua TINYINT NOT NULL DEFAULT 0, giay INT NOT NULL DEFAULT 0,"
            + " PRIMARY KEY (id), KEY k_ngay (ngay)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_cup_dang_ky (tuan INT NOT NULL, char_id INT NOT NULL, doi_id INT NOT NULL,"
            + " ma_loai VARCHAR(16) NOT NULL DEFAULT '', ten VARCHAR(40) NOT NULL DEFAULT '', cap INT NOT NULL DEFAULT 0,"
            + " may VARCHAR(64) NOT NULL DEFAULT '', ip VARCHAR(64) NOT NULL DEFAULT '', user_id INT NOT NULL DEFAULT 0,"
            + " thu_tu INT NOT NULL DEFAULT 0, diem INT NOT NULL DEFAULT 0, thang INT NOT NULL DEFAULT 0,"
            + " hoa INT NOT NULL DEFAULT 0, thua INT NOT NULL DEFAULT 0, mien TINYINT NOT NULL DEFAULT 0,"
            + " hang INT NOT NULL DEFAULT 0, luc DATETIME NULL,"
            + " PRIMARY KEY (tuan, char_id), KEY k_doi (tuan, doi_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_cup_tran (tuan INT NOT NULL, vong TINYINT NOT NULL, doi_a INT NOT NULL,"
            + " doi_b INT NOT NULL, ket_qua TINYINT NOT NULL DEFAULT 0, giay INT NOT NULL DEFAULT 0, luc DATETIME NULL,"
            + " PRIMARY KEY (tuan, vong, doi_a)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_cup_thuong (tuan INT NOT NULL, char_id INT NOT NULL, hang INT NOT NULL DEFAULT 0,"
            + " luong_khoa INT NOT NULL DEFAULT 0, danh_hieu INT NOT NULL DEFAULT -1, phut INT NOT NULL DEFAULT 0,"
            + " da_nhan TINYINT NOT NULL DEFAULT 0, luc DATETIME NOT NULL, nhan_luc DATETIME NULL,"
            + " PRIMARY KEY (tuan, char_id), KEY k_cho (char_id, da_nhan)) ENGINE=InnoDB DEFAULT CHARSET=utf8"
    };

    public static boolean taoBang() {
        if (daTaoBang) {
            return true;
        }
        Connection conn = null;
        Statement st = null;
        try {
            conn = Database.instance.getConnection();
            st = conn.createStatement();
            for (final String s : SQL_TAO_BANG) {
                st.execute(s);
            }
            daTaoBang = true;
            return true;
        } catch (final Throwable e) {
            System.out.println("[PHO BAN / CUP] khong tao duoc bang: " + e);
            return false;
        } finally {
            dong(null, st, conn);
        }
    }

    // ================================================================== du lieu

    static int so(final Object o) {
        return DatabaseLauDai.so(o);
    }

    static int[] dsSo(final Object o) {
        final List<Integer> l = new ArrayList<Integer>();
        if (o != null) {
            for (final String s : String.valueOf(o).split("[,;| ]+")) {
                try {
                    if (!s.trim().isEmpty()) {
                        l.add(Integer.valueOf(Integer.parseInt(s.trim())));
                    }
                } catch (final NumberFormatException ignored) {
                }
            }
        }
        final int[] a = new int[l.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = l.get(i).intValue();
        }
        return a;
    }

    public static List<PhoBanLogic.ChuDe> napChuDe() {
        final List<PhoBanLogic.ChuDe> ds = new ArrayList<PhoBanLogic.ChuDe>();
        for (final Object[] d : DatabaseLauDai.docBang("SELECT thu, ma, ten, he, mau_quai, mau_boss, mo_ta FROM data_pho_ban_ngay"
                + " WHERE bat = 1 ORDER BY thu", 7)) {
            ds.add(new PhoBanLogic.ChuDe(so(d[0]), String.valueOf(d[1]), String.valueOf(d[2]), so(d[3]), dsSo(d[4]), so(d[5]),
                    d[6] == null ? "" : String.valueOf(d[6])));
        }
        return ds;
    }

    public static List<PhoBanLogic.Tang> napTang() {
        final List<PhoBanLogic.Tang> ds = new ArrayList<PhoBanLogic.Tang>();
        for (final Object[] d : DatabaseLauDai.docBang("SELECT tang, so_quai, hp_phan_nghin, tc_phan_nghin, thu_phan_nghin, la_boss"
                + " FROM data_pho_ban_tang ORDER BY tang", 6)) {
            ds.add(new PhoBanLogic.Tang(so(d[0]), so(d[1]), so(d[2]), so(d[3]), so(d[4]), so(d[5]) == 1));
        }
        return ds;
    }

    public static List<CupLogic.Loai> napLoaiCup() {
        final List<CupLogic.Loai> ds = new ArrayList<CupLogic.Loai>();
        for (final Object[] d : DatabaseLauDai.docBang("SELECT ma, ten, mo_ta, cap_toi_da, he, so_nguoi, khong_trang_bi,"
                + " phan_nghin_min, phan_nghin_max FROM data_cup_loai WHERE bat = 1 ORDER BY thu_tu, ma", 9)) {
            ds.add(new CupLogic.Loai(String.valueOf(d[0]), String.valueOf(d[1]), d[2] == null ? "" : String.valueOf(d[2]),
                    so(d[3]), so(d[4]), so(d[5]), so(d[6]) == 1, so(d[7]), so(d[8])));
        }
        return ds;
    }

    public static List<String> napLichCup() {
        final List<String> ds = new ArrayList<String>();
        for (final Object[] d : DatabaseLauDai.docBang("SELECT ma_loai FROM data_cup_lich ORDER BY vi_tri", 1)) {
            ds.add(String.valueOf(d[0]));
        }
        return ds;
    }

    /** Lan cup tuan bi admin TAT nam nay o /admin/sukien (tob_su_kien_lich.ghi_de = 1). */
    public static boolean tuanBiTat(final int namIso, final int tuanIso) {
        for (final Object[] d : DatabaseLauDai.docBang("SELECT ghi_de FROM " + DatabaseLichNam.BANG + " WHERE ma = '"
                + CupLogic.MA_LICH.replace("'", "") + "' AND nam = " + namIso + " AND lan = " + tuanIso, 1)) {
            return so(d[0]) == 1;
        }
        return false;
    }

    // ================================================================== pho ban

    public static int luotDaDung(final int ngay, final int charId) {
        if (!taoBang()) {
            return 0;
        }
        for (final Object[] d : DatabaseLauDai.docBang("SELECT luot FROM tob_pho_ban_luot WHERE ngay = " + ngay
                + " AND char_id = " + charId, 1)) {
            return so(d[0]);
        }
        return 0;
    }

    public static void congLuot(final int ngay, final int charId) {
        capNhat("INSERT INTO tob_pho_ban_luot (ngay, char_id, luot, cap_nhat) VALUES (?, ?, 1, NOW())"
                + " ON DUPLICATE KEY UPDATE luot = luot + 1, cap_nhat = NOW()", Integer.valueOf(ngay), Integer.valueOf(charId));
    }

    public static void ghiLichSu(final int ngay, final String chuDe, final int cap, final int soNguoi, final String thanhVien,
            final int tang, final int ketQua, final int giay) {
        capNhat("INSERT INTO tob_pho_ban_lich_su (luc, ngay, chu_de, cap, so_nguoi, thanh_vien, tang, ket_qua, giay)"
                + " VALUES (NOW(), ?, ?, ?, ?, ?, ?, ?, ?)", Integer.valueOf(ngay), cat(chuDe, 32), Integer.valueOf(cap),
                Integer.valueOf(soNguoi), cat(thanhVien, 255), Integer.valueOf(tang), Integer.valueOf(ketQua), Integer.valueOf(giay));
    }

    // ================================================================== cup

    /** Mot dong dang ky (tob_cup_dang_ky). */
    public static final class DangKy {
        public int charId, doiId, cap, userId, thuTu, diem, thang, hoa, thua, hang;
        public boolean mien;
        public String ten = "", may = "", ip = "", maLoai = "";
    }

    public static List<DangKy> napDangKy(final int tuan) {
        final List<DangKy> ds = new ArrayList<DangKy>();
        if (!taoBang()) {
            return ds;
        }
        for (final Object[] d : DatabaseLauDai.docBang("SELECT char_id, doi_id, ma_loai, ten, cap, may, ip, user_id, thu_tu, diem,"
                + " thang, hoa, thua, mien, hang FROM tob_cup_dang_ky WHERE tuan = " + tuan + " ORDER BY thu_tu, char_id", 15)) {
            final DangKy k = new DangKy();
            k.charId = so(d[0]);
            k.doiId = so(d[1]);
            k.maLoai = String.valueOf(d[2]);
            k.ten = String.valueOf(d[3]);
            k.cap = so(d[4]);
            k.may = String.valueOf(d[5]);
            k.ip = String.valueOf(d[6]);
            k.userId = so(d[7]);
            k.thuTu = so(d[8]);
            k.diem = so(d[9]);
            k.thang = so(d[10]);
            k.hoa = so(d[11]);
            k.thua = so(d[12]);
            k.mien = so(d[13]) == 1;
            k.hang = so(d[14]);
            ds.add(k);
        }
        return ds;
    }

    /** @return true neu vua them (INSERT IGNORE: dang ky lai khong doi thu tu). */
    public static boolean ghiDangKy(final int tuan, final DangKy k) {
        return capNhat("INSERT IGNORE INTO tob_cup_dang_ky (tuan, char_id, doi_id, ma_loai, ten, cap, may, ip, user_id, thu_tu, luc)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())", Integer.valueOf(tuan), Integer.valueOf(k.charId),
                Integer.valueOf(k.doiId), cat(k.maLoai, 16), cat(k.ten, 40), Integer.valueOf(k.cap), cat(k.may, 64), cat(k.ip, 64),
                Integer.valueOf(k.userId), Integer.valueOf(k.thuTu)) > 0;
    }

    public static void huyDangKy(final int tuan, final int doiId) {
        capNhat("DELETE FROM tob_cup_dang_ky WHERE tuan = ? AND doi_id = ?", Integer.valueOf(tuan), Integer.valueOf(doiId));
    }

    public static void ghiDiem(final int tuan, final int doiId, final int diem, final int thang, final int hoa, final int thua,
            final boolean mien, final int hang) {
        capNhat("UPDATE tob_cup_dang_ky SET diem = ?, thang = ?, hoa = ?, thua = ?, mien = ?, hang = ? WHERE tuan = ? AND doi_id = ?",
                Integer.valueOf(diem), Integer.valueOf(thang), Integer.valueOf(hoa), Integer.valueOf(thua),
                Integer.valueOf(mien ? 1 : 0), Integer.valueOf(hang), Integer.valueOf(tuan), Integer.valueOf(doiId));
    }

    /** {vong, doiA, doiB, ketQua}. */
    public static List<int[]> napTran(final int tuan) {
        final List<int[]> ds = new ArrayList<int[]>();
        if (!taoBang()) {
            return ds;
        }
        for (final Object[] d : DatabaseLauDai.docBang("SELECT vong, doi_a, doi_b, ket_qua FROM tob_cup_tran WHERE tuan = " + tuan
                + " ORDER BY vong, doi_a", 4)) {
            ds.add(new int[]{so(d[0]), so(d[1]), so(d[2]), so(d[3])});
        }
        return ds;
    }

    public static void ghiTran(final int tuan, final int vong, final int doiA, final int doiB, final int ketQua, final int giay) {
        capNhat("INSERT INTO tob_cup_tran (tuan, vong, doi_a, doi_b, ket_qua, giay, luc) VALUES (?, ?, ?, ?, ?, ?, NOW())"
                + " ON DUPLICATE KEY UPDATE doi_b = VALUES(doi_b), ket_qua = VALUES(ket_qua), giay = VALUES(giay), luc = VALUES(luc)",
                Integer.valueOf(tuan), Integer.valueOf(vong), Integer.valueOf(doiA), Integer.valueOf(doiB), Integer.valueOf(ketQua),
                Integer.valueOf(giay));
    }

    /**
     * Ghi phan thuong cup cua (tuan, nhan vat) - INSERT IGNORE (chot lai / hai luong cung chot: chi mot dong).
     * @return 1 = vua ghi, 0 = da co, -1 = CSDL loi.
     */
    public static int ghiThuong(final int tuan, final int charId, final int hang, final int luongKhoa, final int danhHieu,
            final int phut) {
        if (!taoBang()) {
            return -1;
        }
        final int n = capNhat("INSERT IGNORE INTO tob_cup_thuong (tuan, char_id, hang, luong_khoa, danh_hieu, phut, da_nhan, luc)"
                + " VALUES (?, ?, ?, ?, ?, ?, 0, NOW())", Integer.valueOf(tuan), Integer.valueOf(charId), Integer.valueOf(hang),
                Integer.valueOf(luongKhoa), Integer.valueOf(danhHieu), Integer.valueOf(phut));
        return n < 0 ? -1 : (n > 0 ? 1 : 0);
    }

    /** Thuong cup chua nhan cua nhan vat: {tuan, hang, luong_khoa, danh_hieu, phut}. */
    public static List<int[]> thuongChoNhan(final int charId) {
        final List<int[]> ds = new ArrayList<int[]>();
        if (!taoBang()) {
            return ds;
        }
        for (final Object[] d : DatabaseLauDai.docBang("SELECT tuan, hang, luong_khoa, danh_hieu, phut FROM tob_cup_thuong"
                + " WHERE char_id = " + charId + " AND da_nhan = 0 ORDER BY tuan", 5)) {
            ds.add(new int[]{so(d[0]), so(d[1]), so(d[2]), so(d[3]), so(d[4])});
        }
        return ds;
    }

    /**
     * Danh dau da phat (UPDATE ... WHERE da_nhan = 0 - nguyen tu). @return 1 = vua danh dau (duoc phat), 0 = da phat
     * truoc do (KHONG phat lai), -1 = CSDL loi (KHONG phat: an toan hon phat trung).
     */
    public static int danhDauDaNhan(final int tuan, final int charId) {
        final int n = capNhat("UPDATE tob_cup_thuong SET da_nhan = 1, nhan_luc = NOW() WHERE tuan = ? AND char_id = ? AND da_nhan = 0",
                Integer.valueOf(tuan), Integer.valueOf(charId));
        return n < 0 ? -1 : (n > 0 ? 1 : 0);
    }

    // ================================================================== tien ich

    static String cat(final String s, final int n) {
        if (s == null) {
            return "";
        }
        return s.length() <= n ? s : s.substring(0, n);
    }

    /** @return so dong doi, -1 neu loi. */
    static int capNhat(final String sql, final Object... thamSo) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(sql);
            for (int i = 0; i < thamSo.length; i++) {
                pre.setObject(i + 1, thamSo[i]);
            }
            return pre.executeUpdate();
        } catch (final Throwable e) {
            System.out.println("[PHO BAN / CUP] " + sql.substring(0, Math.min(60, sql.length())) + ": " + e);
            return -1;
        } finally {
            dong(null, pre, conn);
        }
    }

    static void dong(final ResultSet rs, final Statement st, final Connection conn) {
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
