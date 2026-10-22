package data;

import com.htbb.pokemon.ChoPokemon;
import com.htbb.pokemon.HopPc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * CSDL CHO POKEMON (sql/them_trade_pokemon.sql): tob_cho_pokemon = tin ban + KY QUY con Pokemon (cot du_lieu),
 * tob_log_cho_pokemon = nhat ky moi lan ban (gia, thue, nghi van). Moi thao tac doi chu la MOT giao dich: tin + Hop PC
 * (tob_hop_pc) + nhat ky cung commit hoac cung rollback. Mua = CAS trang_thai 0 -&gt; 1 (UPDATE ... WHERE trang_thai = 0
 * AND het_han &gt; ?): hai nguoi mua cung luc, dung mot UPDATE tra 1 dong.
 */
public final class DatabaseChoPokemon implements ChoPokemon.Kho {

    public static final DatabaseChoPokemon KHO = new DatabaseChoPokemon();
    public static final String BANG = "tob_cho_pokemon";
    public static final String BANG_LOG = "tob_log_cho_pokemon";

    public static final String[] TAO_BANG = {
        "CREATE TABLE IF NOT EXISTS tob_cho_pokemon (id BIGINT NOT NULL AUTO_INCREMENT, id_ban INT NOT NULL, ten_ban VARCHAR(64) NOT NULL DEFAULT '',"
            + " user_ban INT NOT NULL DEFAULT 0, may_ban VARCHAR(64) NOT NULL DEFAULT '', ip_ban VARCHAR(64) NOT NULL DEFAULT '',"
            + " du_lieu VARCHAR(200) NOT NULL DEFAULT '', dex SMALLINT NOT NULL DEFAULT 0, cap TINYINT NOT NULL DEFAULT 1,"
            + " shiny TINYINT NOT NULL DEFAULT 0, do_hiem TINYINT NOT NULL DEFAULT 0, gia BIGINT NOT NULL DEFAULT 0, phi_dang BIGINT NOT NULL DEFAULT 0,"
            + " dang_luc BIGINT NOT NULL DEFAULT 0, het_han BIGINT NOT NULL DEFAULT 0, trang_thai TINYINT NOT NULL DEFAULT 0,"
            + " id_mua INT NOT NULL DEFAULT 0, ten_mua VARCHAR(64) NOT NULL DEFAULT '', mua_luc BIGINT NOT NULL DEFAULT 0,"
            + " thue BIGINT NOT NULL DEFAULT 0, PRIMARY KEY (id), KEY trang_thai (trang_thai, het_han), KEY id_ban (id_ban, trang_thai))"
            + " DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_log_cho_pokemon (id BIGINT NOT NULL AUTO_INCREMENT, luc DATETIME NOT NULL, id_tin BIGINT NOT NULL,"
            + " id_ban INT NOT NULL, ten_ban VARCHAR(64) NOT NULL DEFAULT '', id_mua INT NOT NULL, ten_mua VARCHAR(64) NOT NULL DEFAULT '',"
            + " con VARCHAR(200) NOT NULL DEFAULT '', gia BIGINT NOT NULL DEFAULT 0, thue BIGINT NOT NULL DEFAULT 0,"
            + " sat_tran TINYINT NOT NULL DEFAULT 0, nghi VARCHAR(255) NOT NULL DEFAULT '', PRIMARY KEY (id), KEY id_ban (id_ban, luc),"
            + " KEY id_mua (id_mua, luc)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS data_cho_pokemon_gia (do_hiem TINYINT NOT NULL, ten VARCHAR(32) NOT NULL DEFAULT '',"
            + " san_phan_nghin_gio INT NOT NULL DEFAULT 100, tran_phan_nghin_gio INT NOT NULL DEFAULT 1000, PRIMARY KEY (do_hiem))"
            + " DEFAULT CHARSET=utf8",
    };

    /** Nghi van / sat tran cua lan mua dang ghi (game dat truoc khi goi mua; chi luong chot dung). */
    public static final ThreadLocal<String[]> NGHI = new ThreadLocal<String[]>();

    private DatabaseChoPokemon() {
    }

    public static void taoBang() {
        for (final String sql : TAO_BANG) {
            Connection conn = null;
            PreparedStatement pre = null;
            try {
                conn = Database.instance.getConnection();
                pre = conn.prepareStatement(sql);
                pre.executeUpdate();
            } catch (final Exception e) {
                System.out.println("[CHO POKEMON] LOI tao bang: " + e);
            } finally {
                dong(null, pre, conn);
            }
        }
    }

    static void ghiHop(final Connection conn, final int idChar, final String hop, final Timestamp bay) throws Exception {
        final PreparedStatement p = conn.prepareStatement("INSERT INTO " + DatabaseBoNgoc.BANG_HOP_PC + " (id_char, du_lieu, sua_luc)"
                + " VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE du_lieu = VALUES(du_lieu), sua_luc = VALUES(sua_luc)");
        try {
            p.setInt(1, idChar);
            p.setString(2, hop);
            p.setTimestamp(3, bay);
            p.executeUpdate();
        } finally {
            p.close();
        }
    }

    interface Viec {
        Object lam(Connection conn) throws Exception;
    }

    /** Chay {@code v} trong giao dich; v tra null / nem loi = rollback. */
    static Object giaoDich(final Viec v) {
        Connection conn = null;
        boolean tuDong = true;
        try {
            conn = Database.instance.getConnection();
            tuDong = conn.getAutoCommit();
            conn.setAutoCommit(false);
            final Object kq = v.lam(conn);
            if (kq == null) {
                conn.rollback();
                return null;
            }
            conn.commit();
            return kq;
        } catch (final Exception e) {
            System.out.println("[CHO POKEMON] LOI giao dich, rollback: " + e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (final Exception ignored) {
            }
            return null;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(tuDong);
                }
            } catch (final Exception ignored) {
            }
            dong(null, null, conn);
        }
    }

    @Override
    public long dang(final ChoPokemon.Tin t, final String hopBanMoi) {
        final Object kq = giaoDich(conn -> {
            final PreparedStatement p = conn.prepareStatement("INSERT INTO " + BANG + " (id_ban, ten_ban, user_ban, may_ban, ip_ban, du_lieu,"
                    + " dex, cap, shiny, do_hiem, gia, phi_dang, dang_luc, het_han, trang_thai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
                    Statement.RETURN_GENERATED_KEYS);
            long id = 0L;
            try {
                p.setInt(1, t.idBan);
                p.setString(2, t.tenBan);
                p.setInt(3, t.userBan);
                p.setString(4, t.mayBan);
                p.setString(5, t.ipBan);
                p.setString(6, HopPc.maHoaCon(t.con));
                p.setInt(7, t.con.dex);
                p.setInt(8, t.con.cap);
                p.setInt(9, t.con.shiny ? 1 : 0);
                p.setInt(10, t.doHiem);
                p.setLong(11, t.gia);
                p.setLong(12, t.phiDang);
                p.setLong(13, t.dangLuc);
                p.setLong(14, t.hetHan);
                p.executeUpdate();
                final ResultSet rs = p.getGeneratedKeys();
                if (rs.next()) {
                    id = rs.getLong(1);
                }
                rs.close();
            } finally {
                p.close();
            }
            if (id <= 0L) {
                return null;
            }
            ghiHop(conn, t.idBan, hopBanMoi, new Timestamp(System.currentTimeMillis()));
            return Long.valueOf(id);
        });
        return kq == null ? 0L : ((Long) kq).longValue();
    }

    @Override
    public boolean mua(final ChoPokemon.Tin t, final long bayGiay, final int idMua, final String tenMua, final String hopMuaMoi, final long thue) {
        final String[] nghi = NGHI.get();
        return giaoDich(conn -> {
            final PreparedStatement p = conn.prepareStatement("UPDATE " + BANG + " SET trang_thai = 1, id_mua = ?, ten_mua = ?, mua_luc = ?,"
                    + " thue = ? WHERE id = ? AND trang_thai = 0 AND het_han > ?");
            int n;
            try {
                p.setInt(1, idMua);
                p.setString(2, tenMua);
                p.setLong(3, bayGiay);
                p.setLong(4, thue);
                p.setLong(5, t.id);
                p.setLong(6, bayGiay);
                n = p.executeUpdate();
            } finally {
                p.close();
            }
            if (n != 1) {
                return null; // nguoi khac vua mua / het han: rollback
            }
            final Timestamp bay = new Timestamp(System.currentTimeMillis());
            ghiHop(conn, idMua, hopMuaMoi, bay);
            final PreparedStatement l = conn.prepareStatement("INSERT INTO " + BANG_LOG + " (luc, id_tin, id_ban, ten_ban, id_mua, ten_mua, con,"
                    + " gia, thue, sat_tran, nghi) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            try {
                l.setTimestamp(1, bay);
                l.setLong(2, t.id);
                l.setInt(3, t.idBan);
                l.setString(4, t.tenBan);
                l.setInt(5, idMua);
                l.setString(6, tenMua);
                l.setString(7, HopPc.maHoaCon(t.con));
                l.setLong(8, t.gia);
                l.setLong(9, thue);
                l.setInt(10, nghi != null && "1".equals(nghi[0]) ? 1 : 0);
                l.setString(11, nghi != null ? nghi[1] : "");
                l.executeUpdate();
            } finally {
                l.close();
            }
            return Boolean.TRUE;
        }) != null;
    }

    @Override
    public boolean thuHoi(final ChoPokemon.Tin t, final String hopBanMoi) {
        return giaoDich(conn -> {
            final PreparedStatement p = conn.prepareStatement("UPDATE " + BANG + " SET trang_thai = 3 WHERE id = ? AND id_ban = ? AND trang_thai = 0");
            int n;
            try {
                p.setLong(1, t.id);
                p.setInt(2, t.idBan);
                n = p.executeUpdate();
            } finally {
                p.close();
            }
            if (n != 1) {
                return null;
            }
            ghiHop(conn, t.idBan, hopBanMoi, new Timestamp(System.currentTimeMillis()));
            return Boolean.TRUE;
        }) != null;
    }

    /** Nhan tien MOT tin da ban (CAS 1 -&gt; 2): true = nguoi goi cong tien. */
    public static boolean nhanTien(final long id, final int idBan) {
        return giaoDich(conn -> {
            final PreparedStatement p = conn.prepareStatement("UPDATE " + BANG + " SET trang_thai = 2 WHERE id = ? AND id_ban = ? AND trang_thai = 1");
            try {
                p.setLong(1, id);
                p.setInt(2, idBan);
                return p.executeUpdate() == 1 ? Boolean.TRUE : null;
            } finally {
                p.close();
            }
        }) != null;
    }

    static final String COT = "id, id_ban, ten_ban, user_ban, may_ban, ip_ban, du_lieu, do_hiem, gia, phi_dang, dang_luc, het_han, trang_thai,"
            + " id_mua, ten_mua, thue";

    static ChoPokemon.Tin doc(final ResultSet rs) throws Exception {
        final ChoPokemon.Tin t = new ChoPokemon.Tin();
        t.id = rs.getLong(1);
        t.idBan = rs.getInt(2);
        t.tenBan = rs.getString(3);
        t.userBan = rs.getInt(4);
        t.mayBan = rs.getString(5);
        t.ipBan = rs.getString(6);
        t.con = HopPc.giaiMaCon(rs.getString(7));
        t.doHiem = rs.getInt(8);
        t.gia = rs.getLong(9);
        t.phiDang = rs.getLong(10);
        t.dangLuc = rs.getLong(11);
        t.hetHan = rs.getLong(12);
        t.trangThai = rs.getInt(13);
        t.idMua = rs.getInt(14);
        t.tenMua = rs.getString(15);
        t.thue = rs.getLong(16);
        return t;
    }

    /** Doc tin theo dieu kien (tham so long / int / String). */
    public static List<ChoPokemon.Tin> tim(final String where, final Object... ts) {
        final List<ChoPokemon.Tin> v = new ArrayList<ChoPokemon.Tin>();
        Connection conn = null;
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            p = conn.prepareStatement("SELECT " + COT + " FROM " + BANG + " WHERE " + where);
            for (int i = 0; i < ts.length; i++) {
                p.setObject(i + 1, ts[i]);
            }
            rs = p.executeQuery();
            while (rs.next()) {
                final ChoPokemon.Tin t = doc(rs);
                if (t.con != null) {
                    v.add(t);
                }
            }
        } catch (final Exception e) {
            System.out.println("[CHO POKEMON] LOI doc tin: " + e);
            return null;
        } finally {
            dong(rs, p, conn);
        }
        return v;
    }

    /** So lan mua-ban cung cap (hai chieu) / so lan nguoi ban dat gia sat tran - 7 ngay qua. {cungCap, satTran}. */
    public static int[] demNghi(final int idBan, final int idMua) {
        final int[] kq = new int[2];
        Connection conn = null;
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            p = conn.prepareStatement("SELECT SUM(CASE WHEN (id_ban = ? AND id_mua = ?) OR (id_ban = ? AND id_mua = ?) THEN 1 ELSE 0 END),"
                    + " SUM(CASE WHEN id_ban = ? AND sat_tran = 1 THEN 1 ELSE 0 END) FROM " + BANG_LOG + " WHERE luc > ? AND (id_ban = ? OR id_mua = ?)");
            p.setInt(1, idBan);
            p.setInt(2, idMua);
            p.setInt(3, idMua);
            p.setInt(4, idBan);
            p.setInt(5, idBan);
            p.setTimestamp(6, new Timestamp(System.currentTimeMillis() - 7L * 86400000L));
            p.setInt(7, idBan);
            p.setInt(8, idBan);
            rs = p.executeQuery();
            if (rs.next()) {
                kq[0] = rs.getInt(1);
                kq[1] = rs.getInt(2);
            }
        } catch (final Exception e) {
            System.out.println("[CHO POKEMON] LOI dem nghi van: " + e);
        } finally {
            dong(rs, p, conn);
        }
        return kq;
    }

    /** Khung gia data_cho_pokemon_gia: {doHiem, san, tran}. */
    public static List<long[]> docKhung() {
        final List<long[]> v = new ArrayList<long[]>();
        final List<Object[]> b = DatabaseBoNgoc.docBang("SELECT do_hiem, san_phan_nghin_gio, tran_phan_nghin_gio FROM data_cho_pokemon_gia", 3);
        if (b != null) {
            for (final Object[] r : b) {
                v.add(new long[]{DatabaseBoNgoc.so(r[0]), DatabaseBoNgoc.so(r[1]), DatabaseBoNgoc.so(r[2])});
            }
        }
        return v;
    }

    static void dong(final ResultSet rs, final PreparedStatement p, final Connection c) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (final Exception ignored) {
        }
        try {
            if (p != null) {
                p.close();
            }
        } catch (final Exception ignored) {
        }
        try {
            if (c != null) {
                c.close();
            }
        } catch (final Exception ignored) {
        }
    }
}
