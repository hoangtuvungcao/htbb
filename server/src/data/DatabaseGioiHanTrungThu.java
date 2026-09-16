package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Trung Thu 2026: gioi han doi "Tiên đan" toi da 5 LAN/nhan vat - GIOI HAN TRON DOI (khong
 * reset theo ngay), nen can bang rieng thay vi dung cac bo dem reset-ngay da co san tren Char
 * (vd nBinhTra/nHopMut). Tu tao bang qua CREATE TABLE IF NOT EXISTS, giong DatabaseSuKien.
 */
public final class DatabaseGioiHanTrungThu {

    private static final String BANG = "tob_trungthu_doi_tiendan";
    private static volatile boolean daTaoBang;

    private static boolean taoBang() {
        if (daTaoBang) {
            return true;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + BANG + " ("
                            + "idchar INT NOT NULL,"
                            + "so_lan INT NOT NULL DEFAULT 0,"
                            + "PRIMARY KEY (idchar)"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            pre.executeUpdate();
            daTaoBang = true;
            return true;
        } catch (final Exception e) {
            System.out.println("[TRUNGTHU_GIOIHAN] khong tao duoc bang: " + e);
            return false;
        } finally {
            dong(null, pre, conn);
        }
    }

    public static int laySoLanDoiTienDan(final int idchar) {
        if (!taoBang()) {
            return 0;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT so_lan FROM " + BANG + " WHERE idchar = ?");
            pre.setInt(1, idchar);
            rs = pre.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (final Exception e) {
            System.out.println("[TRUNGTHU_GIOIHAN] loi doc so lan: " + e);
            return 0;
        } finally {
            dong(rs, pre, conn);
        }
    }

    public static void tangSoLanDoiTienDan(final int idchar) {
        if (!taoBang()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "INSERT INTO " + BANG + " (idchar, so_lan) VALUES (?, 1)"
                            + " ON DUPLICATE KEY UPDATE so_lan = so_lan + 1");
            pre.setInt(1, idchar);
            pre.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[TRUNGTHU_GIOIHAN] loi tang so lan: " + e);
        } finally {
            dong(null, pre, conn);
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
                conn.close();
            }
        } catch (final Exception ignored) {
        }
    }

    private DatabaseGioiHanTrungThu() {
    }
}
