package data;

import com.htbb.pokemon.Pokedex;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * Luu Pokedex (viec G7.D2) - xem sql/them_bang_pokedex.sql.
 *
 *   tob_pokedex        (id_char, dex, so_ha, lan_dau)  : moi loai da gap / da ha
 *   tob_pokedex_thuong (id_char, moc, thoi_gian)       : moc thuong da nhan (chan nhan 2 lan)
 *
 * Tao bang (CREATE TABLE IF NOT EXISTS) luc khoi dong - goi tu
 * Database.themCotPokemon(). Ghi theo lo: real.PokedexPokemon gom trong bo nho, Database.saveChar
 * / saveCharAuto goi {@link #luu} chi ghi cac dong da doi. Moi SQL dung PreparedStatement.
 */
public final class DatabasePokedex {

    public static final String BANG = "tob_pokedex";
    public static final String BANG_THUONG = "tob_pokedex_thuong";

    private DatabasePokedex() {
    }

    public static void taoBang() {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + BANG + " ("
                    + "id_char INT NOT NULL,"
                    + "dex SMALLINT NOT NULL,"
                    + "so_ha INT NOT NULL DEFAULT 0,"
                    + "lan_dau DATETIME NOT NULL,"
                    + "PRIMARY KEY (id_char, dex)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            pre.executeUpdate();
            pre.close();
            pre = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + BANG_THUONG + " ("
                    + "id_char INT NOT NULL,"
                    + "moc INT NOT NULL,"
                    + "thoi_gian DATETIME NOT NULL,"
                    + "PRIMARY KEY (id_char, moc)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            pre.executeUpdate();
            System.out.println("[POKEMON] bang " + BANG + ", " + BANG_THUONG + " san sang.");
        } catch (Exception e) {
            System.out.println("[POKEMON] LOI tao bang pokedex: " + e);
        } finally {
            dong(null, pre, conn);
        }
    }

    /** Nap Pokedex cua nhan vat; null neu loi CSDL (noi goi KHONG duoc ghi de bang rong). */
    public static Pokedex nap(final int idChar) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        final Pokedex p = new Pokedex();
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT dex, so_ha, UNIX_TIMESTAMP(lan_dau) FROM " + BANG + " WHERE id_char = ?");
            pre.setInt(1, idChar);
            rs = pre.executeQuery();
            while (rs.next()) {
                p.napDong(rs.getInt(1), rs.getInt(2), rs.getLong(3));
            }
            rs.close();
            pre.close();
            pre = conn.prepareStatement("SELECT moc FROM " + BANG_THUONG + " WHERE id_char = ?");
            pre.setInt(1, idChar);
            rs = pre.executeQuery();
            while (rs.next()) {
                p.napMoc(rs.getInt(1));
            }
            return p;
        } catch (Exception e) {
            System.out.println("[POKEMON] LOI nap pokedex " + idChar + ": " + e);
            return null;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /**
     * Ghi cac dong da doi (so_ha tuyet doi, GREATEST: hai luong luu chong nhau khong lam
     * so_ha lui lai). Loi thi tra lai danh dau ban de lan luu sau ghi tiep.
     */
    public static void luu(final int idChar, final Pokedex p) {
        if (p == null || !p.coBan()) {
            return;
        }
        final List<long[]> ds = p.layDongBan();
        if (ds.isEmpty()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("INSERT INTO " + BANG + " (id_char, dex, so_ha, lan_dau)"
                    + " VALUES (?, ?, ?, FROM_UNIXTIME(?)) ON DUPLICATE KEY UPDATE so_ha = GREATEST(so_ha, VALUES(so_ha))");
            for (final long[] d : ds) {
                pre.setInt(1, idChar);
                pre.setInt(2, (int) d[0]);
                pre.setInt(3, (int) d[1]);
                pre.setLong(4, d[2]);
                pre.addBatch();
            }
            pre.executeBatch();
        } catch (Exception e) {
            p.traLaiBan(ds);
            System.out.println("[POKEMON] LOI luu pokedex " + idChar + ": " + e);
        } finally {
            dong(null, pre, conn);
        }
    }

    /**
     * Danh dau DA NHAN moc (CODING_CONVENTION muc 5: danh dau truoc, phat thuong sau).
     * true chi khi cau INSERT thuc su them dong (chua nhan lan nao).
     */
    public static boolean danhDauMoc(final int idChar, final int moc) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("INSERT IGNORE INTO " + BANG_THUONG + " (id_char, moc, thoi_gian) VALUES (?, ?, NOW())");
            pre.setInt(1, idChar);
            pre.setInt(2, moc);
            return pre.executeUpdate() == 1;
        } catch (Exception e) {
            System.out.println("[POKEMON] LOI danh dau moc pokedex " + idChar + "/" + moc + ": " + e);
            return false;
        } finally {
            dong(null, pre, conn);
        }
    }

    private static void dong(final ResultSet rs, final PreparedStatement pre, final Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (pre != null) {
                pre.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (Exception ignored) {
        }
    }
}
