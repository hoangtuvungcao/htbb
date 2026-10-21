package data;

import com.htbb.pokemon.LichNam;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Lich su kien nam trong CSDL (19/10/2026). Bang tob_su_kien_lich: moi lan dien ra cua moi su kien (ma, nam, lan),
 * gio mo / dong theo lich (lich_mo / lich_dong) va gio hieu luc (mo_luc / dong_luc) - khac lich khi admin ghi de
 * MOT nam o /admin/sukien:
 *   ghi_de = 0 theo lich, 1 = TAT nam nay, 2 = doi gio (bat som / doi ngay: mo_luc / dong_luc do admin dat).
 *
 * Moi nhip LichSuKien (15 giay) {@link #dongBo} dat tob_su_kien.bat = "co lan dien ra nao chua NOW()" cho moi ma co
 * lich dang tu dong (tob_su_kien.tu_dong = 1). So sanh bang NOW() cua CSDL (cung dong ho voi web); gio ghi vao bang
 * la gio Viet Nam (LichNam.MUI_GIO) - CSDL tren VPS chay gio he thong Asia/Ho_Chi_Minh.
 *
 * Web doc bang nay: /api/su-kien (trang chu, cam nang), tin tu dong (web/server/plugins/tin-su-kien.ts),
 * /admin/sukien (ghi de). SQL tay: tools/luu_tru_sinh_du_lieu/sql_nguon/them_lich_su_kien_nam.sql.
 */
public final class DatabaseLichNam {

    public static final String BANG = "tob_su_kien_lich";
    /** sua_boi khi lich nam doi tob_su_kien.bat (ASCII). */
    public static final String NGUOI_DAT = "lich nam";

    public static final String SQL_TAO_BANG = "CREATE TABLE IF NOT EXISTS " + BANG + " ("
            + "ma VARCHAR(40) NOT NULL,"
            + "nam SMALLINT NOT NULL,"
            + "lan TINYINT NOT NULL DEFAULT 0,"
            + "ten VARCHAR(100) NOT NULL DEFAULT '',"
            + "nhom VARCHAR(40) NOT NULL DEFAULT '',"
            + "ngay_le VARCHAR(60) NOT NULL DEFAULT '',"
            + "mo_ta VARCHAR(500) NOT NULL DEFAULT '',"
            + "phan_thuong VARCHAR(300) NOT NULL DEFAULT '',"
            + "lich_mo DATETIME NOT NULL,"
            + "lich_dong DATETIME NOT NULL,"
            + "mo_luc DATETIME NOT NULL,"
            + "dong_luc DATETIME NOT NULL,"
            + "ghi_de TINYINT NOT NULL DEFAULT 0,"
            + "sua_boi VARCHAR(50) NULL,"
            + "sua_luc DATETIME NULL,"
            + "cap_nhat DATETIME NULL,"
            + "PRIMARY KEY (ma, nam, lan), KEY k_mo (mo_luc), KEY k_dong (dong_luc)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    /** Dieu kien SQL tren tob_su_kien: dong KHONG do lich nam lo (de lich hen tay cu chi chay tren cac dong nay). */
    public static final String KHONG_THEO_LICH = "NOT (tu_dong = 1 AND ma IN (" + dsMaSql() + "))";

    private static volatile boolean daTaoBang = false;

    private DatabaseLichNam() {
    }

    static String dsMaSql() {
        final StringBuilder b = new StringBuilder();
        for (final String ma : LichNam.dsMa()) {
            // ma la hang so trong ma nguon (chu thuong, so, ':'), khong co dau nhay
            b.append(b.length() == 0 ? "" : ",").append('\'').append(ma.replace("'", "")).append('\'');
        }
        return b.toString();
    }

    static String cat(final String s, final int n) {
        return s == null ? "" : (s.length() <= n ? s : s.substring(0, n));
    }

    static String gio(final long ms) {
        final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        f.setTimeZone(LichNam.MUI_GIO);
        return f.format(new Date(ms));
    }

    public static boolean taoBang() {
        if (daTaoBang) {
            return true;
        }
        if (!DatabaseSuKien.taoBang()) {
            return false;
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
            System.out.println("[LICH NAM] khong tao duoc bang " + BANG + ": " + e);
            return false;
        } finally {
            dong(null, pre, conn);
        }
    }

    /**
     * Ghi cac lan dien ra (cac nam [tuNam, denNam]). Dong admin da ghi de (ghi_de <> 0) giu mo_luc / dong_luc cua
     * admin, chi cap nhat lich_mo / lich_dong de trang quan tri thay lich goc. Dong ghi_de = 0 khong con trong lich
     * (doi dinh nghia) bi xoa.
     */
    public static int ghiLich(final int tuNam, final int denNam) {
        if (!taoBang()) {
            return 0;
        }
        final List<LichNam.CuaSo> ds = LichNam.tatCa(tuNam, denNam);
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT NOW()");
            rs = pre.executeQuery();
            final java.sql.Timestamp batDau = rs.next() ? rs.getTimestamp(1) : null;
            rs.close();
            rs = null;
            pre.close();
            pre = conn.prepareStatement("INSERT INTO " + BANG
                    + " (ma, nam, lan, ten, nhom, ngay_le, mo_ta, phan_thuong, lich_mo, lich_dong, mo_luc, dong_luc, ghi_de, cap_nhat)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,0,NOW())"
                    + " ON DUPLICATE KEY UPDATE ten = VALUES(ten), nhom = VALUES(nhom), ngay_le = VALUES(ngay_le),"
                    + " mo_ta = VALUES(mo_ta), phan_thuong = VALUES(phan_thuong),"
                    + " lich_mo = VALUES(lich_mo), lich_dong = VALUES(lich_dong),"
                    + " mo_luc = IF(ghi_de = 0, VALUES(mo_luc), mo_luc), dong_luc = IF(ghi_de = 0, VALUES(dong_luc), dong_luc),"
                    + " cap_nhat = NOW()");
            for (final LichNam.CuaSo c : ds) {
                int i = 1;
                pre.setString(i++, c.muc.ma);
                pre.setInt(i++, c.nam);
                pre.setInt(i++, c.lan);
                pre.setString(i++, cat(c.ten, 100));
                pre.setString(i++, c.muc.nhom);
                pre.setString(i++, c.muc.ngayLe);
                pre.setString(i++, cat(c.moTa, 500));
                pre.setString(i++, cat(c.phanThuong, 300));
                pre.setString(i++, gio(c.mo));
                pre.setString(i++, gio(c.dong));
                pre.setString(i++, gio(c.mo));
                pre.setString(i, gio(c.dong));
                pre.addBatch();
            }
            pre.executeBatch();
            pre.close();
            pre = null;
            if (batDau != null) {
                pre = conn.prepareStatement("DELETE FROM " + BANG + " WHERE nam BETWEEN ? AND ? AND ghi_de = 0"
                        + " AND (cap_nhat IS NULL OR cap_nhat < ?)");
                pre.setInt(1, tuNam);
                pre.setInt(2, denNam);
                pre.setTimestamp(3, batDau);
                pre.executeUpdate();
            }
            return ds.size();
        } catch (final Exception e) {
            System.out.println("[LICH NAM] khong ghi duoc lich: " + e);
            return 0;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /**
     * Dat tob_su_kien.bat theo lich cho moi ma dang tu dong. Dong chua co trong tob_su_kien thi them (bat = 0) truoc.
     * @return so dong vua doi bat.
     */
    public static int dongBo(final Set<String> dsMa) {
        if (!taoBang() || dsMa.isEmpty()) {
            return 0;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        int doi = 0;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("INSERT IGNORE INTO " + DatabaseSuKien.BANG + " (ma, bat, dang_bat, tu_dong) VALUES (?, 0, 0, 1)");
            for (final String ma : dsMa) {
                pre.setString(1, ma);
                pre.addBatch();
            }
            pre.executeBatch();
            pre.close();
            final String trong = "EXISTS (SELECT 1 FROM " + BANG + " l WHERE l.ma = s.ma AND l.ghi_de <> 1"
                    + " AND l.mo_luc <= NOW() AND l.dong_luc > NOW())";
            pre = conn.prepareStatement("UPDATE " + DatabaseSuKien.BANG + " s SET s.bat = IF(" + trong + ", 1, 0),"
                    + " s.sua_boi = ?, s.sua_luc = NOW()"
                    + " WHERE s.tu_dong = 1 AND s.ma IN (" + dsMaSql() + ") AND s.bat <> IF(" + trong + ", 1, 0)");
            pre.setString(1, NGUOI_DAT);
            doi = pre.executeUpdate();
            pre.close();
            // Moc hen tay cu (mo_luc / dong_luc tren tob_su_kien, do lich 52 tuan cu dat) khong con y nghia voi ma tu dong.
            pre = conn.prepareStatement("UPDATE " + DatabaseSuKien.BANG + " SET mo_luc = NULL, dong_luc = NULL"
                    + " WHERE tu_dong = 1 AND ma IN (" + dsMaSql() + ") AND (mo_luc IS NOT NULL OR dong_luc IS NOT NULL)");
            pre.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[LICH NAM] khong dong bo duoc tob_su_kien: " + e);
        } finally {
            dong(null, pre, conn);
        }
        return doi;
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
