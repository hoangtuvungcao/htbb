package data;

import com.htbb.pokemon.ChongCloneLogic;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * CSDL cua chong clone (com.htbb.pokemon.ChongClone): bang nhat ky tob_log_chuyen_gia_tri (moi lan chuyen xu /
 * gia tri giua hai nguoi choi, ke ca lan bi chan) + doc ngay dang ky tai khoan va tong phut choi.
 *
 * Bang cung co trong tools/luu_tru_sinh_du_lieu/sql_nguon/them_bang_chong_clone.sql (chay tay duoc, idempotent); may chu tu tao neu thieu.
 * Web doc bang nay o /admin/kinh-te (muc "Nghi clone"): web/server/api/admin/kinh-te/nghi-clone.get.ts.
 */
public final class DatabaseChongClone {

    public static final String BANG = "tob_log_chuyen_gia_tri";
    private static volatile boolean daTaoBang = false;

    private DatabaseChongClone() {
    }

    public static final String SQL_TAO_BANG = "CREATE TABLE IF NOT EXISTS " + BANG + " ("
            + "id BIGINT NOT NULL AUTO_INCREMENT,"
            + "luc DATETIME NOT NULL,"
            + "kieu VARCHAR(20) NOT NULL,"
            + "gui_id INT NOT NULL DEFAULT 0, gui_ten VARCHAR(40) NOT NULL DEFAULT '', gui_tk INT NOT NULL DEFAULT 0,"
            + "gui_cap INT NOT NULL DEFAULT 0, gui_may VARCHAR(64) NOT NULL DEFAULT '', gui_ip VARCHAR(64) NOT NULL DEFAULT '',"
            + "nhan_id INT NOT NULL DEFAULT 0, nhan_ten VARCHAR(40) NOT NULL DEFAULT '', nhan_tk INT NOT NULL DEFAULT 0,"
            + "nhan_cap INT NOT NULL DEFAULT 0, nhan_may VARCHAR(64) NOT NULL DEFAULT '', nhan_ip VARCHAR(64) NOT NULL DEFAULT '',"
            + "xu BIGINT NOT NULL DEFAULT 0,"
            + "chi_tiet VARCHAR(255) NOT NULL DEFAULT '',"
            + "bi_chan TINYINT NOT NULL DEFAULT 0,"
            + "nghi TINYINT NOT NULL DEFAULT 0,"
            + "ly_do VARCHAR(255) NOT NULL DEFAULT '',"
            + "PRIMARY KEY (id), KEY k_nhan (nhan_id, luc), KEY k_luc (luc), KEY k_nghi (nghi, luc)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    static boolean taoBang() {
        if (daTaoBang) {
            return true;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(SQL_TAO_BANG);
            pre.executeUpdate();
            daTaoBang = true;
            return true;
        } catch (final Exception e) {
            System.out.println("[CHONG CLONE] khong tao duoc bang " + BANG + ": " + e);
            return false;
        } finally {
            dong(null, pre, conn);
        }
    }

    /** Ghi mot dong (goi tren luong nen cua ChongClone). */
    public static void ghi(final String kieu, final ChongCloneLogic.Ben gui, final ChongCloneLogic.Ben nhan, final long xu,
            final String chiTiet, final boolean biChan, final boolean nghi, final String lyDo) {
        if (!taoBang()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("INSERT INTO " + BANG + " (luc,kieu,gui_id,gui_ten,gui_tk,gui_cap,gui_may,gui_ip,"
                    + "nhan_id,nhan_ten,nhan_tk,nhan_cap,nhan_may,nhan_ip,xu,chi_tiet,bi_chan,nghi,ly_do)"
                    + " VALUES (NOW(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            int i = 1;
            pre.setString(i++, cat(kieu, 20));
            i = datBen(pre, i, gui);
            i = datBen(pre, i, nhan);
            pre.setLong(i++, xu);
            pre.setString(i++, cat(chiTiet, 255));
            pre.setInt(i++, biChan ? 1 : 0);
            pre.setInt(i++, nghi ? 1 : 0);
            pre.setString(i, cat(lyDo, 255));
            pre.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[CHONG CLONE] khong ghi duoc nhat ky: " + e);
        } finally {
            dong(null, pre, conn);
        }
    }

    private static int datBen(final PreparedStatement pre, int i, final ChongCloneLogic.Ben b) throws Exception {
        pre.setInt(i++, b == null ? 0 : b.charId);
        pre.setString(i++, b == null ? "" : cat(b.ten, 40));
        pre.setInt(i++, b == null ? 0 : b.userId);
        pre.setInt(i++, b == null ? 0 : b.cap);
        pre.setString(i++, b == null ? "" : b.may);
        pre.setString(i++, b == null ? "" : b.ip);
        return i;
    }

    /** Cac lan chuyen THANH CONG tu 0h hom nay (nap lai so ngay sau khi khoi dong): {gui, nhan}. */
    public static List<ChongCloneLogic.Ben[]> napHomNay() {
        final List<ChongCloneLogic.Ben[]> ra = new ArrayList<ChongCloneLogic.Ben[]>();
        if (!taoBang()) {
            return ra;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT gui_id,gui_ten,gui_tk,gui_cap,gui_may,gui_ip,nhan_id,nhan_ten,nhan_tk,nhan_cap,"
                    + "nhan_may,nhan_ip FROM " + BANG + " WHERE luc >= CURDATE() AND bi_chan = 0 ORDER BY id LIMIT 50000");
            rs = pre.executeQuery();
            while (rs.next()) {
                ra.add(new ChongCloneLogic.Ben[]{
                    new ChongCloneLogic.Ben(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4), rs.getString(5),
                            rs.getString(6), -1L, -1L),
                    new ChongCloneLogic.Ben(rs.getInt(7), rs.getString(8), rs.getInt(9), rs.getInt(10), rs.getString(11),
                            rs.getString(12), -1L, -1L)});
            }
        } catch (final Exception e) {
            System.out.println("[CHONG CLONE] khong nap duoc nhat ky hom nay: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return ra;
    }

    /**
     * 0.9.27 giao dich xu (real.GiaoDichXu): tong xu moi TAI KHOAN da dua qua giao dich 1-1 tu 0h hom nay (kieu
     * giao_dich, khong bi chan) - nap lai tran ngay sau khi khoi dong. Giao dich chi thuoc ghi xu = 0 nen khong lam lech.
     */
    public static java.util.Map<Integer, Long> xuGiaoDichHomNay() {
        final java.util.Map<Integer, Long> ra = new java.util.HashMap<Integer, Long>();
        if (!taoBang()) {
            return ra;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT gui_tk, SUM(xu) FROM " + BANG
                    + " WHERE luc >= CURDATE() AND kieu = 'giao_dich' AND bi_chan = 0 AND xu > 0 AND gui_tk > 0 GROUP BY gui_tk");
            rs = pre.executeQuery();
            while (rs.next()) {
                ra.put(Integer.valueOf(rs.getInt(1)), Long.valueOf(rs.getLong(2)));
            }
        } catch (final Exception e) {
            System.out.println("[CHONG CLONE] khong nap duoc tong xu giao dich hom nay: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return ra;
    }

    /** Ngay dang ky tai khoan (team_user.regdate, CSDL tai khoan), -1 neu khong doc duoc. */
    public static long ngayDangKy(final int userId, final String csdlTaiKhoan) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        String catalogCu = null;
        try {
            conn = Database.instance.getConnection();
            catalogCu = conn.getCatalog();
            // NovaDB: moi ket noi chi thay MOT CSDL -> doi catalog (nhu DangKyTrongGame), tra lai truoc khi free.
            if (csdlTaiKhoan != null && !csdlTaiKhoan.equals(catalogCu)) {
                conn.setCatalog(csdlTaiKhoan);
            }
            pre = conn.prepareStatement("SELECT regdate FROM team_user WHERE id = ? LIMIT 1");
            pre.setInt(1, userId);
            rs = pre.executeQuery();
            if (rs.next()) {
                final Timestamp t = rs.getTimestamp(1);
                return t == null ? -1L : t.getTime();
            }
            return -1L;
        } catch (final Exception e) {
            return -1L;
        } finally {
            try {
                if (conn != null && catalogCu != null && !catalogCu.equals(conn.getCatalog())) {
                    conn.setCatalog(catalogCu);
                }
            } catch (final Exception ignored) {
            }
            dong(rs, pre, conn);
        }
    }

    /** tob_char.totalTimePlay (phut da luu), -1 neu khong doc duoc. */
    public static long phutChoiDaLuu(final int charId) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT totalTimePlay FROM tob_char WHERE id = ? LIMIT 1");
            pre.setInt(1, charId);
            rs = pre.executeQuery();
            return rs.next() ? rs.getLong(1) : -1L;
        } catch (final Exception e) {
            return -1L;
        } finally {
            dong(rs, pre, conn);
        }
    }

    static String cat(final String s, final int n) {
        if (s == null) {
            return "";
        }
        return s.length() > n ? s.substring(0, n) : s;
    }

    private static void dong(final ResultSet rs, final PreparedStatement pre, final Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (final Exception ignored) {
            }
        }
        if (pre != null) {
            try {
                pre.close();
            } catch (final Exception ignored) {
            }
        }
        if (conn != null) {
            try {
                Database.connPool.free(conn);
            } catch (final Exception ignored) {
            }
        }
    }
}
