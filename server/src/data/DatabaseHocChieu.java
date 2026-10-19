package data;

import com.htbb.pokemon.HuanLuyen;
import com.htbb.pokemon.Learnset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSDL cua hoc chieu theo loai + Diem huan luyen (real.HocChieuGame). Bang + du lieu o sql/hoc_chieu_huan_luyen.sql
 * (idempotent); may chu chi tu tao BANG neu thieu (khong co du lieu learnset -&gt; giu luat cu).
 * Moi ham nuot loi (in ra): khong duoc lam chet luong dang nhap / ban do.
 */
public final class DatabaseHocChieu {

    private static volatile boolean daTaoBang = false;

    private DatabaseHocChieu() {
    }

    public static final String[] SQL_TAO_BANG = {
        "CREATE TABLE IF NOT EXISTS data_learnset (loai SMALLINT NOT NULL, o TINYINT NOT NULL, dong TINYINT NOT NULL,"
            + " ten VARCHAR(48) NOT NULL, he TINYINT NOT NULL, kieu VARCHAR(16) NOT NULL, nguon VARCHAR(8) NOT NULL DEFAULT 'cap',"
            + " cap_hoc TINYINT NOT NULL, gia_xu INT NOT NULL DEFAULT 0, cap_cap_chieu VARCHAR(64) NOT NULL,"
            + " sua_boi VARCHAR(50) NULL, sua_luc DATETIME NULL, PRIMARY KEY (loai, o), KEY k_dong (dong))"
            + " ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS data_huan_luyen (id TINYINT NOT NULL, ma VARCHAR(16) NOT NULL, ten VARCHAR(32) NOT NULL,"
            + " mo_ta VARCHAR(120) NOT NULL DEFAULT '', tran_diem SMALLINT NOT NULL, nguong_giam SMALLINT NOT NULL,"
            + " phan_nghin_truoc SMALLINT NOT NULL, phan_nghin_sau SMALLINT NOT NULL, thu_tu TINYINT NOT NULL DEFAULT 0,"
            + " sua_boi VARCHAR(50) NULL, sua_luc DATETIME NULL, PRIMARY KEY (id)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS data_tang_truong_dong (dong TINYINT NOT NULL, ten VARCHAR(24) NOT NULL,"
            + " suc_manh TINYINT NOT NULL, kheo_leo TINYINT NOT NULL, tinh_than TINYINT NOT NULL, suc_khoe TINYINT NOT NULL,"
            + " may_man TINYINT NOT NULL DEFAULT 0, ghi_chu VARCHAR(160) NOT NULL DEFAULT '', sua_boi VARCHAR(50) NULL,"
            + " sua_luc DATETIME NULL, PRIMARY KEY (dong)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_huan_luyen (char_id INT NOT NULL, phan_bo VARCHAR(48) NOT NULL DEFAULT '',"
            + " lan_tay INT NOT NULL DEFAULT 0, tay_luc BIGINT NOT NULL DEFAULT 0, tong_xu_tay BIGINT NOT NULL DEFAULT 0,"
            + " cap_nhat DATETIME NULL, PRIMARY KEY (char_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_chuyen_doi_can_bang (char_id INT NOT NULL, buoc VARCHAR(24) NOT NULL,"
            + " luc DATETIME NOT NULL, chi_tiet TEXT NULL, xu_hoan BIGINT NOT NULL DEFAULT 0, da_hoan TINYINT NOT NULL DEFAULT 0,"
            + " da_bao TINYINT NOT NULL DEFAULT 0, PRIMARY KEY (char_id, buoc)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
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
            System.out.println("[HOC CHIEU] khong tao duoc bang: " + e);
            return false;
        } finally {
            dongSt(st);
            tra(conn);
        }
    }

    // ------------------------------------------------------------------ du lieu thiet ke

    public static List<Learnset.Dong> napLearnset() {
        final List<Learnset.Dong> ds = new ArrayList<Learnset.Dong>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT loai, o, dong, ten, he, kieu, nguon, cap_hoc, gia_xu, cap_cap_chieu FROM data_learnset");
            rs = pre.executeQuery();
            while (rs.next()) {
                final int[] moc = Learnset.moc(rs.getString(10));
                if (moc == null) {
                    System.out.println("[HOC CHIEU] bo dong learnset hong: loai " + rs.getInt(1) + " o " + rs.getInt(2));
                    continue;
                }
                ds.add(new Learnset.Dong(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getString(4), rs.getInt(5),
                        rs.getString(6), Learnset.nguon(rs.getString(7)), rs.getInt(8), rs.getInt(9), moc));
            }
        } catch (final Exception e) {
            System.out.println("[HOC CHIEU] LOI nap data_learnset: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return ds;
    }

    public static List<HuanLuyen.Dong> napDongHuanLuyen() {
        final List<HuanLuyen.Dong> ds = new ArrayList<HuanLuyen.Dong>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT id, ma, ten, mo_ta, tran_diem, nguong_giam, phan_nghin_truoc, phan_nghin_sau, thu_tu"
                    + " FROM data_huan_luyen");
            rs = pre.executeQuery();
            while (rs.next()) {
                ds.add(new HuanLuyen.Dong(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5),
                        rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getInt(9)));
            }
        } catch (final Exception e) {
            System.out.println("[HOC CHIEU] LOI nap data_huan_luyen: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return ds;
    }

    public static Map<Integer, int[]> napTangTruong() {
        final Map<Integer, int[]> m = new HashMap<Integer, int[]>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT dong, suc_manh, kheo_leo, tinh_than, suc_khoe, may_man FROM data_tang_truong_dong");
            rs = pre.executeQuery();
            while (rs.next()) {
                m.put(rs.getInt(1), new int[]{rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6)});
            }
        } catch (final Exception e) {
            System.out.println("[HOC CHIEU] LOI nap data_tang_truong_dong: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return m;
    }

    // ------------------------------------------------------------------ trang thai nguoi choi

    /** {phan_bo, lan_tay, tay_luc, tong_xu_tay}; mang rong (length 0) = chua co dong; null = LOI CSDL. */
    public static Object[] napTrangThai(final int charId) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT phan_bo, lan_tay, tay_luc, tong_xu_tay FROM tob_huan_luyen WHERE char_id=?");
            pre.setInt(1, charId);
            rs = pre.executeQuery();
            if (!rs.next()) {
                return new Object[0];
            }
            return new Object[]{rs.getString(1), rs.getInt(2), rs.getLong(3), rs.getLong(4)};
        } catch (final Exception e) {
            System.out.println("[HOC CHIEU] LOI nap tob_huan_luyen " + charId + ": " + e);
            return null;
        } finally {
            dong(rs, pre, conn);
        }
    }

    public static boolean luuTrangThai(final int charId, final String phanBo, final int lanTay, final long tayLuc,
            final long tongXuTay) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("INSERT INTO tob_huan_luyen (char_id, phan_bo, lan_tay, tay_luc, tong_xu_tay, cap_nhat)"
                    + " VALUES (?,?,?,?,?,NOW()) ON DUPLICATE KEY UPDATE phan_bo=VALUES(phan_bo), lan_tay=VALUES(lan_tay),"
                    + " tay_luc=VALUES(tay_luc), tong_xu_tay=VALUES(tong_xu_tay), cap_nhat=VALUES(cap_nhat)");
            pre.setInt(1, charId);
            pre.setString(2, phanBo == null ? "" : phanBo);
            pre.setInt(3, lanTay);
            pre.setLong(4, tayLuc);
            pre.setLong(5, tongXuTay);
            pre.executeUpdate();
            return true;
        } catch (final Exception e) {
            System.out.println("[HOC CHIEU] LOI luu tob_huan_luyen " + charId + ": " + e);
            return false;
        } finally {
            dong(null, pre, conn);
        }
    }

    /** Co dau chuyen doi (char, buoc) chua: 1 co, 0 chua, -1 loi CSDL. */
    public static int coBuoc(final int charId, final String buoc) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT 1 FROM tob_chuyen_doi_can_bang WHERE char_id=? AND buoc=?");
            pre.setInt(1, charId);
            pre.setString(2, buoc);
            rs = pre.executeQuery();
            return rs.next() ? 1 : 0;
        } catch (final Exception e) {
            System.out.println("[HOC CHIEU] LOI doc tob_chuyen_doi_can_bang " + charId + ": " + e);
            return -1;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /**
     * CHUYEN DOI DIEM TIEM NANG -&gt; DIEM HUAN LUYEN, MOT giao dich: dau 'huan_luyen' + trang thai. Tra 1 = vua chuyen,
     * 0 = da chuyen tu truoc (khong ghi gi), -1 = loi (khong ghi gi; nhan vat giu luat cu phien nay).
     */
    public static int chuyenDoiHuanLuyen(final int charId, final String phanBo, final String chiTiet, final boolean canBao) {
        Connection conn = null;
        PreparedStatement pre = null;
        boolean tuDong = true;
        try {
            conn = Database.instance.getConnection();
            tuDong = conn.getAutoCommit();
            conn.setAutoCommit(false);
            pre = conn.prepareStatement("INSERT IGNORE INTO tob_chuyen_doi_can_bang (char_id, buoc, luc, chi_tiet, xu_hoan, da_hoan, da_bao)"
                    + " VALUES (?,?,NOW(),?,0,1,?)");
            pre.setInt(1, charId);
            pre.setString(2, "huan_luyen");
            pre.setString(3, catChu(chiTiet, 4000));
            pre.setInt(4, canBao ? 0 : 1);
            final int n = pre.executeUpdate();
            pre.close();
            pre = null;
            if (n == 0) {
                conn.rollback();
                return 0;
            }
            pre = conn.prepareStatement("INSERT INTO tob_huan_luyen (char_id, phan_bo, lan_tay, tay_luc, tong_xu_tay, cap_nhat)"
                    + " VALUES (?,?,0,0,0,NOW()) ON DUPLICATE KEY UPDATE phan_bo=VALUES(phan_bo), lan_tay=0, tay_luc=0,"
                    + " cap_nhat=VALUES(cap_nhat)");
            pre.setInt(1, charId);
            pre.setString(2, phanBo == null ? "" : phanBo);
            pre.executeUpdate();
            conn.commit();
            return 1;
        } catch (final Exception e) {
            System.out.println("[HOC CHIEU] LOI chuyen doi huan luyen " + charId + ": " + e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (final Exception ignored) {
            }
            return -1;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(tuDong);
                }
            } catch (final Exception ignored) {
            }
            dong(null, pre, conn);
        }
    }

    /** Them dau (char, buoc) neu chua co. Tra 1 = vua them, 0 = da co, -1 = loi. */
    public static int themBuoc(final int charId, final String buoc, final String chiTiet, final long xuHoan) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("INSERT IGNORE INTO tob_chuyen_doi_can_bang (char_id, buoc, luc, chi_tiet, xu_hoan, da_hoan)"
                    + " VALUES (?,?,NOW(),?,?,?)");
            pre.setInt(1, charId);
            pre.setString(2, buoc);
            pre.setString(3, catChu(chiTiet, 4000));
            pre.setLong(4, Math.max(0L, xuHoan));
            pre.setInt(5, xuHoan > 0L ? 0 : 1);
            return pre.executeUpdate() > 0 ? 1 : 0;
        } catch (final Exception e) {
            System.out.println("[HOC CHIEU] LOI ghi tob_chuyen_doi_can_bang " + charId + " " + buoc + ": " + e);
            return -1;
        } finally {
            dong(null, pre, conn);
        }
    }

    /** Dong chua hoan xu hoac chua bao: {buoc, chi_tiet, xu_hoan, da_hoan, da_bao}. */
    public static List<Object[]> dsChoXuLy(final int charId) {
        final List<Object[]> ds = new ArrayList<Object[]>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT buoc, chi_tiet, xu_hoan, da_hoan, da_bao FROM tob_chuyen_doi_can_bang"
                    + " WHERE char_id=? AND (da_hoan=0 OR da_bao=0) ORDER BY buoc");
            pre.setInt(1, charId);
            rs = pre.executeQuery();
            while (rs.next()) {
                ds.add(new Object[]{rs.getString(1), rs.getString(2), rs.getLong(3), rs.getInt(4), rs.getInt(5)});
            }
        } catch (final Exception e) {
            System.out.println("[HOC CHIEU] LOI doc cho xu ly " + charId + ": " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return ds;
    }

    /** Danh dau da hoan (cot = "da_hoan") / da bao (cot = "da_bao"). */
    public static boolean danhDau(final int charId, final String buoc, final String cot) {
        if (!"da_hoan".equals(cot) && !"da_bao".equals(cot)) {
            return false;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("UPDATE tob_chuyen_doi_can_bang SET " + cot + "=1 WHERE char_id=? AND buoc=?");
            pre.setInt(1, charId);
            pre.setString(2, buoc);
            return pre.executeUpdate() > 0;
        } catch (final Exception e) {
            System.out.println("[HOC CHIEU] LOI danh dau " + cot + " " + charId + ": " + e);
            return false;
        } finally {
            dong(null, pre, conn);
        }
    }

    // ------------------------------------------------------------------ tien ich

    static String catChu(final String s, final int n) {
        if (s == null) {
            return "";
        }
        return s.length() > n ? s.substring(0, n) : s;
    }

    private static void dongSt(final Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (final Exception ignored) {
            }
        }
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
