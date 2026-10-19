package data;

import com.htbb.pokemon.VongQuayLogic;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * CSDL cua vong quay gop hu (real.VongQuay). Bang cung co trong sql/sua_vong_quay.sql (idempotent); may chu tu
 * tao neu thieu.
 * <ul>
 *   <li>tob_vong_quay_cuoc: cuoc dang cho cua luot hien tai (nap lai khi khoi dong - khong mat tien khi restart).</li>
 *   <li>tob_vong_quay_cho_nhan: tien thang / hoan cho nguoi dang OFFLINE, cong vao luc ho dang nhap.</li>
 *   <li>tob_vong_quay_ngay: luot + tong cuoc moi nhan vat moi ngay, thiet bi / IP (tran ngay, chong clone).</li>
 *   <li>tob_vong_quay_lich_su: moi luot da xong (hu, phi, nguoi thang) - kiem toan "khong in tien".</li>
 * </ul>
 * Moi ham nuot loi (in ra) - vong quay khong duoc lam chet luong ban do.
 */
public final class DatabaseVongQuay {

    private static volatile boolean daTaoBang = false;

    private DatabaseVongQuay() {
    }

    public static final String[] SQL_TAO_BANG = {
        "CREATE TABLE IF NOT EXISTS tob_vong_quay_cuoc ("
            + "loai TINYINT NOT NULL, charname VARCHAR(40) NOT NULL, char_id INT NOT NULL DEFAULT 0,"
            + "so BIGINT NOT NULL DEFAULT 0, may VARCHAR(64) NOT NULL DEFAULT '', ip VARCHAR(64) NOT NULL DEFAULT '',"
            + "luc DATETIME NOT NULL, PRIMARY KEY (loai, charname)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_vong_quay_cho_nhan ("
            + "charname VARCHAR(40) NOT NULL, xu BIGINT NOT NULL DEFAULT 0, luong BIGINT NOT NULL DEFAULT 0,"
            + "ly_do VARCHAR(255) NOT NULL DEFAULT '', cap_nhat DATETIME NOT NULL,"
            + "PRIMARY KEY (charname)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_vong_quay_ngay ("
            + "ngay DATE NOT NULL, loai TINYINT NOT NULL, charname VARCHAR(40) NOT NULL,"
            + "may VARCHAR(64) NOT NULL DEFAULT '', ip VARCHAR(64) NOT NULL DEFAULT '',"
            + "luot INT NOT NULL DEFAULT 0, tong BIGINT NOT NULL DEFAULT 0,"
            + "PRIMARY KEY (ngay, loai, charname)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_vong_quay_lich_su ("
            + "id BIGINT NOT NULL AUTO_INCREMENT, luc DATETIME NOT NULL, loai TINYINT NOT NULL,"
            + "so_nguoi INT NOT NULL DEFAULT 0, hu BIGINT NOT NULL DEFAULT 0, phi BIGINT NOT NULL DEFAULT 0,"
            + "tra BIGINT NOT NULL DEFAULT 0, hoan TINYINT NOT NULL DEFAULT 0,"
            + "nguoi_thang VARCHAR(40) NOT NULL DEFAULT '', chi_tiet VARCHAR(1000) NOT NULL DEFAULT '',"
            + "PRIMARY KEY (id), KEY k_luc (luc)) ENGINE=InnoDB DEFAULT CHARSET=utf8"
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
            System.out.println("[VONG QUAY] khong tao duoc bang: " + e);
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

    // ------------------------------------------------------------------ cuoc dang cho

    public static List<VongQuayLogic.NguoiCuoc> napCuoc(final int loai) {
        final List<VongQuayLogic.NguoiCuoc> ds = new ArrayList<VongQuayLogic.NguoiCuoc>();
        if (!taoBang()) {
            return ds;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT charname,char_id,so,may,ip FROM tob_vong_quay_cuoc WHERE loai=? AND so>0 ORDER BY luc");
            pre.setInt(1, loai);
            rs = pre.executeQuery();
            while (rs.next()) {
                ds.add(new VongQuayLogic.NguoiCuoc(rs.getString(1), rs.getInt(2), rs.getLong(3), rs.getString(4), rs.getString(5)));
            }
        } catch (final Exception e) {
            System.out.println("[VONG QUAY] napCuoc: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return ds;
    }

    public static void ghiCuoc(final int loai, final VongQuayLogic.NguoiCuoc n) {
        capNhat("INSERT INTO tob_vong_quay_cuoc (loai,charname,char_id,so,may,ip,luc) VALUES (?,?,?,?,?,?,NOW())"
                + " ON DUPLICATE KEY UPDATE so=VALUES(so), char_id=VALUES(char_id), luc=VALUES(luc)",
                Integer.valueOf(loai), n.khoa, Integer.valueOf(n.charId), Long.valueOf(n.so), cat(n.may, 64), cat(n.ip, 64));
    }

    public static void xoaCuoc(final int loai) {
        capNhat("DELETE FROM tob_vong_quay_cuoc WHERE loai=?", Integer.valueOf(loai));
    }

    // ------------------------------------------------------------------ cho nhan (offline)

    public static void congChoNhan(final String charname, final long xu, final long luong, final String lyDo) {
        capNhat("INSERT INTO tob_vong_quay_cho_nhan (charname,xu,luong,ly_do,cap_nhat) VALUES (?,?,?,?,NOW())"
                + " ON DUPLICATE KEY UPDATE xu=xu+VALUES(xu), luong=luong+VALUES(luong), ly_do=VALUES(ly_do), cap_nhat=VALUES(cap_nhat)",
                charname.toLowerCase(), Long.valueOf(xu), Long.valueOf(luong), cat(lyDo, 255));
    }

    /** Lay va XOA phan cho nhan (nguyen tu: chi tra khi DELETE dung dong vua doc). {xu, luong} hoac null. */
    public static long[] nhanChoNhan(final String charname) {
        if (!taoBang()) {
            return null;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT xu,luong FROM tob_vong_quay_cho_nhan WHERE charname=?");
            pre.setString(1, charname.toLowerCase());
            rs = pre.executeQuery();
            if (!rs.next()) {
                return null;
            }
            final long xu = rs.getLong(1);
            final long luong = rs.getLong(2);
            rs.close();
            rs = null;
            pre.close();
            pre = conn.prepareStatement("DELETE FROM tob_vong_quay_cho_nhan WHERE charname=? AND xu=? AND luong=?");
            pre.setString(1, charname.toLowerCase());
            pre.setLong(2, xu);
            pre.setLong(3, luong);
            if (pre.executeUpdate() != 1) {
                return null;
            }
            return new long[]{xu, luong};
        } catch (final Exception e) {
            System.out.println("[VONG QUAY] nhanChoNhan: " + e);
            return null;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /**
     * Cuoc dang cho cua he CU (nin_xoso / nin_xoso_low = xu, nin_xoso_luong = luong) -> hoan qua cho_nhan, roi
     * dua ve 0. Idempotent (sql/sua_vong_quay.sql lam cung viec; chay sau thi khong con dong nao).
     */
    public static void chuyenCuocCu() {
        if (!taoBang()) {
            return;
        }
        chuyenBangCu("nin_xoso", false);
        chuyenBangCu("nin_xoso_low", false);
        chuyenBangCu("nin_xoso_luong", true);
    }

    private static void chuyenBangCu(final String bang, final boolean laLuong) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        final List<Object[]> ds = new ArrayList<Object[]>();
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT charname,money FROM " + bang + " WHERE money>0");
            rs = pre.executeQuery();
            while (rs.next()) {
                ds.add(new Object[]{rs.getString(1), Long.valueOf(rs.getLong(2))});
            }
            rs.close();
            rs = null;
            pre.close();
            pre = null;
            for (final Object[] d : ds) {
                final String ten = (String) d[0];
                final long so = ((Long) d[1]).longValue();
                pre = conn.prepareStatement("UPDATE " + bang + " SET money=0 WHERE charname=? AND money=?");
                pre.setString(1, ten);
                pre.setLong(2, so);
                final int n = pre.executeUpdate();
                pre.close();
                pre = null;
                if (n == 1) {
                    congChoNhan(ten, laLuong ? 0L : so, laLuong ? so : 0L, "hoan cuoc vong quay cu (" + bang + ")");
                    System.out.println("[VONG QUAY] hoan cuoc cu " + bang + ": " + ten + " " + so);
                }
            }
        } catch (final Exception e) {
            // bang cu co the khong co (CSDL moi) - bo qua.
        } finally {
            dong(rs, pre, conn);
        }
    }

    // ------------------------------------------------------------------ so ngay

    /** Cong {@code dLuot}/{@code dTong} (co the am khi hoan) cho dong hom nay. */
    public static void ghiNgay(final int loai, final String charname, final String may, final String ip, final int dLuot,
            final long dTong) {
        capNhat("INSERT INTO tob_vong_quay_ngay (ngay,loai,charname,may,ip,luot,tong) VALUES (CURDATE(),?,?,?,?,GREATEST(0,?),GREATEST(0,?))"
                + " ON DUPLICATE KEY UPDATE luot=GREATEST(0,CAST(luot AS SIGNED)+?), tong=GREATEST(0,tong+?),"
                + " may=IF(VALUES(may)='',may,VALUES(may)), ip=IF(VALUES(ip)='',ip,VALUES(ip))",
                Integer.valueOf(loai), charname.toLowerCase(), cat(may, 64), cat(ip, 64), Integer.valueOf(dLuot),
                Long.valueOf(dTong), Integer.valueOf(dLuot), Long.valueOf(dTong));
    }

    /** Nap so hom nay vao {@code so}. */
    public static void napNgay(final VongQuayLogic.SoNgay so, final String ngay) {
        if (!taoBang()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT loai,charname,may,ip,luot,tong FROM tob_vong_quay_ngay WHERE ngay=CURDATE()");
            rs = pre.executeQuery();
            while (rs.next()) {
                so.nap(ngay, rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5), rs.getLong(6));
            }
        } catch (final Exception e) {
            System.out.println("[VONG QUAY] napNgay: " + e);
        } finally {
            dong(rs, pre, conn);
        }
    }

    // ------------------------------------------------------------------ lich su

    public static void ghiLichSu(final int loai, final VongQuayLogic.KetQua kq) {
        final StringBuilder b = new StringBuilder();
        for (final VongQuayLogic.NguoiCuoc n : kq.nguoi) {
            if (b.length() > 900) {
                b.append("...");
                break;
            }
            b.append(b.length() == 0 ? "" : ",").append(n.khoa).append(':').append(n.so);
        }
        capNhat("INSERT INTO tob_vong_quay_lich_su (luc,loai,so_nguoi,hu,phi,tra,hoan,nguoi_thang,chi_tiet) VALUES (NOW(),?,?,?,?,?,?,?,?)",
                Integer.valueOf(loai), Integer.valueOf(kq.nguoi.size()), Long.valueOf(kq.hu), Long.valueOf(kq.phi),
                Long.valueOf(kq.tongTra()), Integer.valueOf(kq.hoan ? 1 : 0),
                kq.nguoiThang == null ? "" : kq.nguoiThang.khoa, cat(b.toString(), 1000));
    }

    // ------------------------------------------------------------------ noi bo

    private static void capNhat(final String sql, final Object... thamSo) {
        if (!taoBang()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(sql);
            for (int i = 0; i < thamSo.length; i++) {
                pre.setObject(i + 1, thamSo[i]);
            }
            pre.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[VONG QUAY] " + e + " | " + sql);
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
