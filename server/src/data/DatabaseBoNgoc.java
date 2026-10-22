package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * CSDL cua bo trang bi / ngoc he / che tao / moc neo nang cap / thu phuc Pokemon (sql/nang_cap.sql phan 11).
 * Bang du lieu (data_*) chi DOC (real.BoNgocGame nap vao bo nho luc khoi dong, nap lai bang lenh admin "9" hoac khi
 * chu ky bang doi); bang nguoi choi (tob_ho_so_bo_ngoc, tob_hop_pc) doc khi can, ghi cung luc luu nhan vat
 * (Database.saveChar). Moi SQL dung PreparedStatement.
 */
public final class DatabaseBoNgoc {

    private DatabaseBoNgoc() {
    }

    /** Bang du lieu ma chu ky (CHECKSUM TABLE) quyet dinh nap lai. */
    public static final String[] BANG_DU_LIEU = {
        "data_noi_dung_bo", "data_noi_dung_ngoc_he", "data_noi_dung_che_tao", "data_ngoc_he_lo", "data_ngoc_he_khac",
        "data_che_tao_bac", "data_nang_cap_moc", "data_bat_bong", "data_bat_loai", "data_bat_chi_so_loai", "data_bat_chuyen_doi",
    };

    /** Bang may chu tu tao neu thieu (RONG: du lieu mau o sql/nang_cap.sql phan 11; thieu thi dung mac dinh). */
    static final String[] TAO_BANG = {
        "CREATE TABLE IF NOT EXISTS data_ngoc_he_lo (color_name TINYINT NOT NULL, ten_bac VARCHAR(32) NOT NULL DEFAULT '',"
            + " so_lo TINYINT NOT NULL DEFAULT 1, PRIMARY KEY (color_name)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS data_ngoc_he_khac (he TINYINT NOT NULL, he_khac TINYINT NOT NULL,"
            + " PRIMARY KEY (he)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS data_che_tao_bac (ma TINYINT NOT NULL, ten VARCHAR(32) NOT NULL DEFAULT '',"
            + " color_name TINYINT NOT NULL DEFAULT 0, phan_nghin INT NOT NULL DEFAULT 0, he_so_phan_tram SMALLINT NOT NULL DEFAULT 100,"
            + " so_lo TINYINT NOT NULL DEFAULT 1, PRIMARY KEY (ma)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS data_nang_cap_moc (moc TINYINT NOT NULL, PRIMARY KEY (moc)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS data_bat_bong (ma VARCHAR(16) NOT NULL, ten VARCHAR(32) NOT NULL DEFAULT '',"
            + " thu_tu TINYINT NOT NULL DEFAULT 0, he_so_phan_tram SMALLINT NOT NULL DEFAULT 100, tran_phan_nghin SMALLINT NOT NULL DEFAULT 600,"
            + " gia_phan_nghin_gio INT NOT NULL DEFAULT 20, cap_mo TINYINT NOT NULL DEFAULT 1, bat TINYINT NOT NULL DEFAULT 1,"
            + " PRIMARY KEY (ma)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS data_bat_loai (dex SMALLINT NOT NULL, ten VARCHAR(32) NOT NULL DEFAULT '',"
            + " do_hiem TINYINT NOT NULL DEFAULT 0, ti_le_phan_nghin SMALLINT NOT NULL DEFAULT 0, tien_hoa_thanh SMALLINT NOT NULL DEFAULT 0,"
            + " cap_tien_hoa TINYINT NOT NULL DEFAULT 0, keo_tien_hoa SMALLINT NOT NULL DEFAULT 0, bat_duoc TINYINT NOT NULL DEFAULT 1,"
            + " chi_su_kien TINYINT NOT NULL DEFAULT 0, PRIMARY KEY (dex)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_ho_so_bo_ngoc (id_char INT NOT NULL, du_lieu MEDIUMTEXT NULL, sua_luc DATETIME NULL,"
            + " PRIMARY KEY (id_char)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_hop_pc (id_char INT NOT NULL, du_lieu MEDIUMTEXT NULL, sua_luc DATETIME NULL,"
            + " PRIMARY KEY (id_char)) DEFAULT CHARSET=utf8",
        // 0.9.8 trao doi Pokemon (sql/nang_cap.sql phan 11)
        "ALTER TABLE data_bat_loai ADD COLUMN IF NOT EXISTS tien_hoa_trao_doi TINYINT NOT NULL DEFAULT 0",
        "CREATE TABLE IF NOT EXISTS data_bat_chi_so_loai (dex SMALLINT NOT NULL, hp SMALLINT NOT NULL DEFAULT 60,"
            + " tan_cong SMALLINT NOT NULL DEFAULT 60, phong_thu SMALLINT NOT NULL DEFAULT 60, tan_cong_db SMALLINT NOT NULL DEFAULT 60,"
            + " phong_thu_db SMALLINT NOT NULL DEFAULT 60, toc_do SMALLINT NOT NULL DEFAULT 60, PRIMARY KEY (dex)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS data_bat_chuyen_doi (do_hiem TINYINT NOT NULL, ten VARCHAR(32) NOT NULL DEFAULT '',"
            + " keo SMALLINT NOT NULL DEFAULT 1, xu_phan_nghin_gio SMALLINT NOT NULL DEFAULT 0, luong_khoa SMALLINT NOT NULL DEFAULT 0,"
            + " PRIMARY KEY (do_hiem)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_log_trao_doi_pokemon (id BIGINT NOT NULL AUTO_INCREMENT, luc DATETIME NOT NULL,"
            + " id_a INT NOT NULL, ten_a VARCHAR(64) NOT NULL DEFAULT '', id_b INT NOT NULL, ten_b VARCHAR(64) NOT NULL DEFAULT '',"
            + " a_dua VARCHAR(512) NOT NULL DEFAULT '', b_dua VARCHAR(512) NOT NULL DEFAULT '', phi_a BIGINT NOT NULL DEFAULT 0,"
            + " phi_b BIGINT NOT NULL DEFAULT 0, PRIMARY KEY (id), KEY id_a (id_a, luc), KEY id_b (id_b, luc)) DEFAULT CHARSET=utf8",
    };

    /** TeamServer luc khoi dong (sau ket noi CSDL): tao bang thieu + cot moi cua data_noi_dung_che_tao. */
    public static void taoBang() {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            for (final String sql : TAO_BANG) {
                try {
                    pre = conn.prepareStatement(sql);
                    pre.executeUpdate();
                } catch (final Exception e) {
                    System.out.println("[BO NGOC] LOI tao bang: " + e);
                } finally {
                    dongPre(pre);
                    pre = null;
                }
            }
            try {
                pre = conn.prepareStatement("ALTER TABLE data_noi_dung_che_tao ADD COLUMN IF NOT EXISTS loi_che_tao TINYINT NOT NULL DEFAULT 0,"
                        + " ADD COLUMN IF NOT EXISTS bat TINYINT NOT NULL DEFAULT 1");
                pre.executeUpdate();
            } catch (final Exception e) {
                System.out.println("[BO NGOC] chua them cot loi_che_tao/bat cho data_noi_dung_che_tao: " + e);
            } finally {
                dongPre(pre);
                pre = null;
            }
        } catch (final Exception e) {
            System.out.println("[BO NGOC] LOI ket noi tao bang: " + e);
        } finally {
            dong(null, pre, conn);
        }
        DatabaseChoPokemon.taoBang(); // [cho-pokemon] tin ban + ky quy + nhat ky + khung gia
    }

    // ------------------------------------------------------------------ doc bang du lieu

    /** Doc moi dong (null neu loi: noi goi GIU du lieu cu, khong thay bang rong). */
    public static List<Object[]> docBang(final String sql, final int soCot) {
        final List<Object[]> kq = new ArrayList<Object[]>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            while (rs.next()) {
                final Object[] d = new Object[soCot];
                for (int i = 0; i < soCot; i++) {
                    d[i] = rs.getObject(i + 1);
                }
                kq.add(d);
            }
            return kq;
        } catch (final Exception e) {
            System.out.println("[BO NGOC] chua doc duoc (" + sql.substring(0, Math.min(60, sql.length())) + "): " + e);
            return null;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /** Chu ky cac bang du lieu (CHECKSUM TABLE); "" neu loi. */
    public static String chuKy() {
        final StringBuilder sb = new StringBuilder();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("CHECKSUM TABLE " + String.join(", ", BANG_DU_LIEU));
            rs = pre.executeQuery();
            while (rs.next()) {
                sb.append(rs.getString(1)).append('=').append(rs.getString(2)).append(';');
            }
        } catch (final Exception e) {
            return "";
        } finally {
            dong(rs, pre, conn);
        }
        return sb.toString();
    }

    public static int so(final Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(o.toString().trim());
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    public static long soDai(final Object o) {
        if (o == null) {
            return 0L;
        }
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        try {
            return Long.parseLong(o.toString().trim());
        } catch (final NumberFormatException e) {
            return 0L;
        }
    }

    public static double soThuc(final Object o) {
        if (o == null) {
            return 0.0;
        }
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        try {
            return Double.parseDouble(o.toString().trim().replace(',', '.'));
        } catch (final NumberFormatException e) {
            return 0.0;
        }
    }

    public static String chu(final Object o) {
        return o == null ? "" : o.toString();
    }

    // ------------------------------------------------------------------ ho so nguoi choi

    public static final String BANG_TUI_NGOC = "tob_ho_so_bo_ngoc";
    public static final String BANG_HOP_PC = "tob_hop_pc";

    /** Chuoi ho so trong {@code bang}; "" neu chua co dong; null neu loi CSDL (noi goi KHONG duoc ghi de). */
    public static String napHoSo(final String bang, final int idChar) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT du_lieu FROM " + bang + " WHERE id_char = ?");
            pre.setInt(1, idChar);
            rs = pre.executeQuery();
            if (!rs.next()) {
                return "";
            }
            final String s = rs.getString(1);
            return s == null ? "" : s;
        } catch (final Exception e) {
            System.out.println("[BO NGOC] LOI nap " + bang + " " + idChar + ": " + e);
            return null;
        } finally {
            dong(rs, pre, conn);
        }
    }

    public static boolean luuHoSo(final String bang, final int idChar, final String duLieu) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("INSERT INTO " + bang + " (id_char, du_lieu, sua_luc) VALUES (?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE du_lieu = VALUES(du_lieu), sua_luc = VALUES(sua_luc)");
            pre.setInt(1, idChar);
            pre.setString(2, duLieu);
            pre.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            pre.executeUpdate();
            return true;
        } catch (final Exception e) {
            System.out.println("[BO NGOC] LOI luu " + bang + " " + idChar + ": " + e);
            return false;
        } finally {
            dong(null, pre, conn);
        }
    }

    /** Nhat ky trao doi Pokemon (sql/nang_cap.sql phan 11). */
    public static final String BANG_LOG_TRAO_DOI = "tob_log_trao_doi_pokemon";

    /**
     * TRAO DOI POKEMON: ghi CA HAI Hop PC + mot dong nhat ky trong MOT giao dich (autocommit tat). Loi bat ky =&gt;
     * rollback, tra false (hai hop trong CSDL giu ban cu, noi goi khong ap thay doi vao bo nho).
     */
    public static boolean luuTraoDoi(final int idA, final String tenA, final String hopA, final int idB, final String tenB,
            final String hopB, final String aDua, final String bDua, final long phiA, final long phiB) {
        Connection conn = null;
        PreparedStatement pre = null;
        boolean tuDong = true;
        try {
            conn = Database.instance.getConnection();
            tuDong = conn.getAutoCommit();
            conn.setAutoCommit(false);
            final Timestamp bay = new Timestamp(System.currentTimeMillis());
            pre = conn.prepareStatement("INSERT INTO " + BANG_HOP_PC + " (id_char, du_lieu, sua_luc) VALUES (?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE du_lieu = VALUES(du_lieu), sua_luc = VALUES(sua_luc)");
            pre.setInt(1, idA);
            pre.setString(2, hopA);
            pre.setTimestamp(3, bay);
            pre.addBatch();
            pre.setInt(1, idB);
            pre.setString(2, hopB);
            pre.setTimestamp(3, bay);
            pre.addBatch();
            pre.executeBatch();
            dongPre(pre);
            pre = conn.prepareStatement("INSERT INTO " + BANG_LOG_TRAO_DOI + " (luc, id_a, ten_a, id_b, ten_b, a_dua, b_dua, phi_a, phi_b)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            pre.setTimestamp(1, bay);
            pre.setInt(2, idA);
            pre.setString(3, tenA == null ? "" : tenA);
            pre.setInt(4, idB);
            pre.setString(5, tenB == null ? "" : tenB);
            pre.setString(6, aDua == null ? "" : aDua);
            pre.setString(7, bDua == null ? "" : bDua);
            pre.setLong(8, phiA);
            pre.setLong(9, phiB);
            pre.executeUpdate();
            conn.commit();
            return true;
        } catch (final Exception e) {
            System.out.println("[TRAO DOI POKEMON] LOI ghi " + idA + " <-> " + idB + ", rollback: " + e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (final Exception ignored) {
            }
            return false;
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

    private static void dongPre(final PreparedStatement pre) {
        try {
            if (pre != null) {
                pre.close();
            }
        } catch (final Exception ignored) {
        }
    }

    private static void dong(final ResultSet rs, final PreparedStatement pre, final Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (final Exception ignored) {
        }
        dongPre(pre);
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (final Exception ignored) {
        }
    }
}
