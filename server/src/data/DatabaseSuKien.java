package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import real.SuKien;

/**
 * Bang `tob_su_kien` - cong bat/tat cua tung su kien.
 *
 * CHIEU GHI CHIA DOI RO RANG, de hai ben khong dam len nhau:
 *
 *   Trang quan tri (web/admin/sukien.php) ghi:  bat, sua_boi, sua_luc
 *   May chu game ghi:                           ten, nhom, mo_ta, dang_bat, may_chu_luc
 *
 * May chu KHONG BAO GIO ghi vao cot `bat`. Nho vay khoi dong lai may chu khong lam mat
 * thiet lap ai vua dat, va trang web khong can biet trong game co nhung su kien nao -
 * danh sach do do may chu tu dang len moi lan khoi dong (ghiAnhChup). Them mot su kien
 * moi vao real/SuKien la no tu hien tren trang quan tri, khong phai sua PHP.
 *
 * `may_chu_luc` so voi `sua_luc` la cach trang web biet co can nhac "phai khoi dong lai"
 * hay khong: sua sau lan khoi dong gan nhat = thiet lap chua co hieu luc.
 */
public final class DatabaseSuKien {

    public static final String BANG = "tob_su_kien";

    private DatabaseSuKien() {
    }

    // ============================ TAO BANG ============================

    /**
     * Tao bang neu chua co. Tra ve false khi khong tao duoc - luc do may chu van chay
     * binh thuong voi gia tri mac dinh, chi la khong bat/tat duoc tu web.
     */
    public static boolean taoBang() {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + BANG + " ("
                            + "ma VARCHAR(40) NOT NULL,"
                            + "bat TINYINT NOT NULL DEFAULT 0,"
                            + "ten VARCHAR(100) NULL,"
                            + "nhom VARCHAR(40) NULL,"
                            + "mo_ta VARCHAR(500) NULL,"
                            + "dang_bat TINYINT NOT NULL DEFAULT 0,"
                            + "sua_boi VARCHAR(50) NULL,"
                            + "sua_luc DATETIME NULL,"
                            + "may_chu_luc DATETIME NULL,"
                            + "PRIMARY KEY (ma)"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            pre.executeUpdate();
            return true;
        } catch (final Exception e) {
            System.out.println("[SUKIEN] khong tao duoc bang " + BANG + ": " + e);
            return false;
        } finally {
            dong(null, pre, conn);
        }
    }

    // ============================ DOC ============================

    /**
     * Doc cot `bat` cua moi dong roi nap vao SuKien.
     *
     * Dong chua co trong bang thi khong nap gi ca - SuKien.macDinh() lo phan do. Nho
     * vay them su kien moi vao ma nguon khong can migrate CSDL truoc.
     */
    public static void nap() {
        if (!taoBang()) {
            return;
        }
        final Map<String, Boolean> ra = new HashMap<String, Boolean>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT ma, bat FROM " + BANG);
            rs = pre.executeQuery();
            while (rs.next()) {
                ra.put(rs.getString("ma"), Boolean.valueOf(rs.getInt("bat") == 1));
            }
            SuKien.dat(ra);
        } catch (final Exception e) {
            System.out.println("[SUKIEN] khong doc duoc bang " + BANG + ": " + e);
        } finally {
            dong(rs, pre, conn);
        }
    }

    // ============================ GHI ANH CHUP ============================

    /**
     * Dang danh muc su kien va trang thai THUC TE luc nay len bang, de trang quan tri
     * co cai ma hien.
     *
     * Dong da co: chi cap nhat phan may chu so huu, GIU NGUYEN cot `bat` cua nguoi dat.
     * Dong chua co: them moi voi bat = trang thai mac dinh dang dung, nen lan dau vao
     * trang quan tri thay dung nhung gi game dang chay chu khong phai mot bang toan tat.
     */
    public static void ghiAnhChup() {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "INSERT INTO " + BANG
                            + " (ma, bat, ten, nhom, mo_ta, dang_bat, may_chu_luc)"
                            + " VALUES (?,?,?,?,?,?,NOW())"
                            + " ON DUPLICATE KEY UPDATE"
                            + "   ten = VALUES(ten), nhom = VALUES(nhom), mo_ta = VALUES(mo_ta),"
                            + "   dang_bat = VALUES(dang_bat), may_chu_luc = NOW()");
            for (final SuKien.Muc m : SuKien.danhMuc()) {
                final boolean dangBat = SuKien.bat(m.ma);
                pre.setString(1, m.ma);
                pre.setInt(2, dangBat ? 1 : 0);
                pre.setString(3, m.ten);
                pre.setString(4, m.nhom);
                pre.setString(5, m.moTa);
                pre.setInt(6, dangBat ? 1 : 0);
                pre.addBatch();
            }
            pre.executeBatch();
        } catch (final Exception e) {
            System.out.println("[SUKIEN] khong ghi duoc anh chup: " + e);
        } finally {
            dong(null, pre, conn);
        }
    }

    // ============================ TIEN ICH ============================

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
