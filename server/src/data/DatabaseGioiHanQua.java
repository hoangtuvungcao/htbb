package data;

import com.htbb.pokemon.GioiHanQua;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * CSDL cho gioi han qua (li xi) va ky quy thach dau cuoc xu. Bang cung co trong sql/sua_vong_quay_2.sql (idempotent);
 * may chu tu tao neu thieu.
 * <ul>
 *   <li>tob_gioi_han_qua: o dem (ma, khoa) - so trong ngay + tong ca dot ({@link GioiHanQua.KhoDem}).</li>
 *   <li>tob_thach_dau_ky_quy: cuoc dang giu cua tran dang danh (khoi dong lai giua tran -> hoan du).</li>
 * </ul>
 * Loi CSDL: {@link #KHO}.thu tra false (DONG khi loi - khong phat qua khi khong dem duoc).
 */
public final class DatabaseGioiHanQua {

    private static volatile boolean daTaoBang = false;

    private DatabaseGioiHanQua() {
    }

    public static final String[] SQL_TAO_BANG = {
        "CREATE TABLE IF NOT EXISTS tob_gioi_han_qua ("
            + "ma VARCHAR(40) NOT NULL, khoa VARCHAR(80) NOT NULL, ngay VARCHAR(8) NOT NULL DEFAULT '',"
            + "so_ngay BIGINT NOT NULL DEFAULT 0, tong BIGINT NOT NULL DEFAULT 0, cap_nhat DATETIME NULL,"
            + "PRIMARY KEY (ma, khoa)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_thach_dau_ky_quy ("
            + "id_arena INT NOT NULL, loai TINYINT NOT NULL DEFAULT 3,"
            + "ten0 VARCHAR(40) NOT NULL DEFAULT '', id0 INT NOT NULL DEFAULT 0,"
            + "ten1 VARCHAR(40) NOT NULL DEFAULT '', id1 INT NOT NULL DEFAULT 0,"
            + "cuoc BIGINT NOT NULL DEFAULT 0, luc DATETIME NOT NULL,"
            + "PRIMARY KEY (id_arena)) ENGINE=InnoDB DEFAULT CHARSET=utf8"
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
        } catch (final Exception e) {
            System.out.println("[GIOI HAN QUA] khong tao duoc bang: " + e);
            return false;
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (final Exception ignored) {
            }
            tra(conn);
        }
    }

    /** Kho dem tren CSDL (moi phep thu la mot cau UPDATE co dieu kien - nguyen tu giua nhieu luong / may). */
    public static final GioiHanQua.KhoDem KHO = new GioiHanQua.KhoDem() {
        @Override
        public boolean thu(final String ma, final String khoa, final long them, final long ngayToiDa, final long tongToiDa,
                final String ngay) {
            if (them <= 0L || !taoBang()) {
                return false;
            }
            capNhat("INSERT IGNORE INTO tob_gioi_han_qua (ma,khoa,ngay,so_ngay,tong,cap_nhat) VALUES (?,?,?,0,0,NOW())",
                    cat(ma, 40), cat(khoa, 80), ngay);
            return capNhat("UPDATE tob_gioi_han_qua SET so_ngay=IF(ngay=?,so_ngay,0)+?, tong=tong+?, ngay=?, cap_nhat=NOW()"
                    + " WHERE ma=? AND khoa=? AND IF(ngay=?,so_ngay,0)+?<=? AND tong+?<=?",
                    ngay, Long.valueOf(them), Long.valueOf(them), ngay, cat(ma, 40), cat(khoa, 80), ngay, Long.valueOf(them),
                    Long.valueOf(ngayToiDa), Long.valueOf(them), Long.valueOf(tongToiDa)) == 1;
        }

        @Override
        public long tong(final String ma, final String khoa) {
            Connection conn = null;
            PreparedStatement pre = null;
            ResultSet rs = null;
            try {
                conn = Database.instance.getConnection();
                pre = conn.prepareStatement("SELECT tong FROM tob_gioi_han_qua WHERE ma=? AND khoa=?");
                pre.setString(1, cat(ma, 40));
                pre.setString(2, cat(khoa, 80));
                rs = pre.executeQuery();
                return rs.next() ? rs.getLong(1) : 0L;
            } catch (final Exception e) {
                return 0L;
            } finally {
                dong(rs, pre, conn);
            }
        }

        @Override
        public void hoan(final String ma, final String khoa, final long bot, final String ngay) {
            capNhat("UPDATE tob_gioi_han_qua SET so_ngay=IF(ngay=?,GREATEST(0,so_ngay-?),so_ngay), tong=GREATEST(0,tong-?)"
                    + " WHERE ma=? AND khoa=?", ngay, Long.valueOf(bot), Long.valueOf(bot), cat(ma, 40), cat(khoa, 80));
        }
    };

    // ------------------------------------------------------------------ ma li xi

    /** Chot ma li xi NGUYEN TU (chi 1 nguoi thang khi hai nguoi go cung ma). true = ma hop le va da chot. */
    public static boolean chotMaLiXi(final String code, final String charnhan) {
        if (code == null) {
            return false;
        }
        final String c = code.replaceAll("[^a-zA-Z0-9_-]", "");
        if (c.isEmpty()) {
            return false;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection1();
            pre = conn.prepareStatement("UPDATE tob_code_lixi SET nhan=1, charnhan=?, timeNhan=NOW() WHERE code=? AND nhan=0");
            pre.setString(1, cat(charnhan, 60));
            pre.setString(2, c);
            return pre.executeUpdate() == 1;
        } catch (final Exception e) {
            System.out.println("[LI XI] chotMaLiXi: " + e);
            return false;
        } finally {
            if (pre != null) {
                try {
                    pre.close();
                } catch (final Exception ignored) {
                }
            }
            if (conn != null) {
                try {
                    Database.connPool1.free(conn);
                } catch (final Exception ignored) {
                }
            }
        }
    }

    // ------------------------------------------------------------------ ky quy thach dau

    public static void ghiKyQuy(final int idArena, final int loai, final String ten0, final int id0, final String ten1,
            final int id1, final long cuoc) {
        capNhat("INSERT INTO tob_thach_dau_ky_quy (id_arena,loai,ten0,id0,ten1,id1,cuoc,luc) VALUES (?,?,?,?,?,?,?,NOW())"
                + " ON DUPLICATE KEY UPDATE loai=VALUES(loai), ten0=VALUES(ten0), id0=VALUES(id0), ten1=VALUES(ten1),"
                + " id1=VALUES(id1), cuoc=VALUES(cuoc), luc=VALUES(luc)",
                Integer.valueOf(idArena), Integer.valueOf(loai), cat(ten0, 40), Integer.valueOf(id0), cat(ten1, 40),
                Integer.valueOf(id1), Long.valueOf(cuoc));
    }

    public static void xoaKyQuy(final int idArena) {
        capNhat("DELETE FROM tob_thach_dau_ky_quy WHERE id_arena=?", Integer.valueOf(idArena));
    }

    /** Moi ky quy con lai (tran dang danh luc may tat): {id, ten0, ten1, cuoc}. */
    public static List<Object[]> napKyQuy() {
        final List<Object[]> ds = new ArrayList<Object[]>();
        if (!taoBang()) {
            return ds;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT id_arena,ten0,ten1,cuoc FROM tob_thach_dau_ky_quy");
            rs = pre.executeQuery();
            while (rs.next()) {
                ds.add(new Object[]{Integer.valueOf(rs.getInt(1)), rs.getString(2), rs.getString(3), Long.valueOf(rs.getLong(4))});
            }
        } catch (final Exception e) {
            System.out.println("[THACH DAU] napKyQuy: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return ds;
    }

    // ------------------------------------------------------------------ noi bo

    private static int capNhat(final String sql, final Object... thamSo) {
        if (!taoBang()) {
            return -1;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(sql);
            for (int i = 0; i < thamSo.length; i++) {
                pre.setObject(i + 1, thamSo[i]);
            }
            return pre.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[GIOI HAN QUA] " + e + " | " + sql);
            return -1;
        } finally {
            dong(null, pre, conn);
        }
    }

    static String cat(final String s, final int n) {
        if (s == null) {
            return "";
        }
        return s.length() > n ? s.substring(0, n) : s;
    }

    private static void tra(final Connection conn) {
        if (conn != null) {
            try {
                Database.connPool.free(conn);
            } catch (final Exception ignored) {
            }
        }
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
        tra(conn);
    }
}
