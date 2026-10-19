package real;

import data.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * VAT PHAM SU KIEN (0.9.7): bang data_su_kien_vat_pham (ma_su_kien = ma SuKien / lich nam, item_id = id vat pham
 * dang potion trong potions[]) liet ke vat pham THAT ma code roi / thuong cua tung su kien dua vao tui (Monster roi
 * quai, NPC su kien, giao hang...). Dung cho kieu muc tieu nhiem vu "su_kien_vat_pham" (DiemMocNhiemVu): chi tinh vat
 * pham thuoc mot su kien DANG BAT. Nap luc khoi dong (NoiDungLauDai); kiem dong bo voi code roi: test/KiemPvpPk.
 */
public final class SuKienVatPham {

    private SuKienVatPham() {
    }

    /** item_id -&gt; cac ma su kien co vat pham do. */
    static volatile HashMap<Integer, Set<String>> BANG = new HashMap<Integer, Set<String>>();

    /** Dat bang (nap CSDL / kiem thu): {ma su kien, item id}. */
    public static void datBang(final java.util.List<Object[]> ds) {
        final HashMap<Integer, Set<String>> m = new HashMap<Integer, Set<String>>();
        for (final Object[] d : ds) {
            m.computeIfAbsent(Integer.valueOf(((Number) d[1]).intValue()), k -> new HashSet<String>()).add(String.valueOf(d[0]));
        }
        BANG = m;
    }

    public static int soDong() {
        int n = 0;
        for (final Entry<Integer, Set<String>> e : BANG.entrySet()) {
            n += e.getValue().size();
        }
        return n;
    }

    public static Set<String> suKienCua(final int itemId) {
        final Set<String> s = BANG.get(Integer.valueOf(itemId));
        return s == null ? Collections.<String>emptySet() : s;
    }

    /** Ma su kien dang bat co vat pham nay (null = khong phai vat pham cua su kien nao dang bat). */
    public static String suKienDangBat(final int itemId, final java.util.function.Predicate<String> bat) {
        for (final String ma : suKienCua(itemId)) {
            if (bat.test(ma)) {
                return ma;
            }
        }
        return null;
    }

    static boolean dangBat(final String ma) {
        try {
            return SuKien.bat(ma);
        } catch (final Exception e) {
            return false;
        }
    }

    public static void taoBang() {
        data.DatabaseLauDai.ghiCong("CREATE TABLE IF NOT EXISTS data_su_kien_vat_pham (ma_su_kien VARCHAR(32) NOT NULL,"
                + " item_id INT NOT NULL, ten VARCHAR(64) NOT NULL DEFAULT '', nguon VARCHAR(128) NOT NULL DEFAULT '',"
                + " PRIMARY KEY (ma_su_kien, item_id)) DEFAULT CHARSET=utf8");
    }

    public static void nap() {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT ma_su_kien, item_id FROM data_su_kien_vat_pham");
            rs = pre.executeQuery();
            final java.util.List<Object[]> ds = new java.util.ArrayList<Object[]>();
            while (rs.next()) {
                ds.add(new Object[]{rs.getString(1), Integer.valueOf(rs.getInt(2))});
            }
            datBang(ds);
            System.out.println("[SU KIEN VAT PHAM] " + ds.size() + " dong");
        } catch (final Exception e) {
            System.out.println("[SU KIEN VAT PHAM] LOI nap: " + e);
        } finally {
            XepHangPvpGame.dong(rs, pre, conn);
        }
    }
}
