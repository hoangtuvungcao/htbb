package server.quantri;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * So lieu kinh te TRUC TIEP cho trang Kinh te (bang tob_kinh_te_ngay chi duoc LauDaiJob chot moi dem):
 *   - {@link #chup()}: tong xu / luong / luong khoa / nguoi hoat dong tinh NGAY tu tob_char (theo lan luu gan
 *     nhat cua nhan vat) + xu vao / ra tu dau ngay (bo dem cua LauDai, doc khong dat lai);
 *   - {@link #ghiNeuCan()}: moi gio mot dong vao tob_kinh_te_gio (giu 30 ngay) - bang dieu khien goi moi 10 phut;
 *   - {@link #chuoiNgay(List)}: chuoi 30 ngay gop tu chot ngay (neu co) + mau theo gio + anh chup hien tai.
 * Chi doc tong (SUM) - mot truy van nhe, chay ngoai EDT.
 */
public final class KinhTeTrucTiep {

    public static final String BANG = "tob_kinh_te_gio";
    static final long MOT_GIO = 3600000L;

    /** Mot anh chup. */
    public static final class Anh {
        public final long luc;
        public final long tongXu, tongLuong, tongLuongKhoa, xuVao, xuRa;
        public final int nguoiHoatDong, online;

        Anh(final long luc, final long xu, final long luong, final long lk, final int nguoi, final int online, final long vao,
                final long ra) {
            this.luc = luc;
            this.tongXu = xu;
            this.tongLuong = luong;
            this.tongLuongKhoa = lk;
            this.nguoiHoatDong = nguoi;
            this.online = online;
            this.xuVao = vao;
            this.xuRa = ra;
        }
    }

    private static volatile long lanGhi;
    private static volatile Anh moiNhat;

    private KinhTeTrucTiep() {
    }

    public static Anh moiNhat() {
        return moiNhat;
    }

    /** Xu vao / ra tu lan chot ngay gan nhat (bo dem rieng cua LauDai - doc qua phan chieu, KHONG dat lai). */
    static long[] xuVaoRa() {
        final long[] kq = new long[2];
        try {
            final Class<?> c = com.htbb.pokemon.LauDai.class;
            final String[] ten = {"XU_VAO", "XU_RA"};
            for (int i = 0; i < 2; i++) {
                final java.lang.reflect.Field f = c.getDeclaredField(ten[i]);
                f.setAccessible(true);
                kq[i] = ((java.util.concurrent.atomic.AtomicLong) f.get(null)).get();
            }
        } catch (final Throwable ignored) {
        }
        return kq;
    }

    /** Tinh anh chup hien tai (null neu khong doc duoc CSDL). */
    public static Anh chup() {
        final List<Object[]> r = ThaoTacMayChu.truyVan("SELECT COALESCE(SUM(gold + kho_xu),0), COALESCE(SUM(luong + kho_luong),0),"
                + " COALESCE(SUM(luonglock + kho_luong_khoa),0), COALESCE(SUM(CASE WHEN lastLog >= DATE_SUB(NOW(), INTERVAL 7 DAY)"
                + " THEN 1 ELSE 0 END),0) FROM tob_char WHERE del=1");
        if (ThaoTacMayChu.loiTruyVan(r) != null || r.size() < 2) {
            return null;
        }
        final Object[] d = r.get(1);
        final long[] vr = xuVaoRa();
        final Anh a = new Anh(System.currentTimeMillis(), so(d[0]), so(d[1]), so(d[2]), (int) so(d[3]),
                DuLieuTrucTiep.demOnline(), vr[0], vr[1]);
        moiNhat = a;
        return a;
    }

    static long so(final Object o) {
        return o instanceof Number ? ((Number) o).longValue() : 0L;
    }

    static void taoBang(final Connection c) throws Exception {
        try (Statement st = c.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS " + BANG + " (thoi_gian DATETIME NOT NULL, so_nguoi INT NOT NULL DEFAULT 0,"
                    + " online INT NOT NULL DEFAULT 0, tong_xu BIGINT NOT NULL DEFAULT 0, tong_luong BIGINT NOT NULL DEFAULT 0,"
                    + " tong_luong_khoa BIGINT NOT NULL DEFAULT 0, xu_vao BIGINT NOT NULL DEFAULT 0, xu_ra BIGINT NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (thoi_gian)) DEFAULT CHARSET=utf8");
        }
    }

    /** Chup va ghi mot mau neu mau truoc cu hon 55 phut. Tra ve anh chup (co the null). */
    public static Anh ghiNeuCan() {
        final Anh a = chup();
        if (a == null || System.currentTimeMillis() - lanGhi < 55L * 60000L) {
            return a;
        }
        Connection conn = null;
        try {
            conn = data.Database.instance.getConnection();
            taoBang(conn);
            final List<Object[]> cuoi = new ArrayList<Object[]>();
            ThaoTacMayChu.truyVan(conn, cuoi, "SELECT MAX(thoi_gian) FROM " + BANG);
            if (cuoi.size() > 1 && cuoi.get(1)[0] instanceof Date
                    && System.currentTimeMillis() - ((Date) cuoi.get(1)[0]).getTime() < 55L * 60000L) {
                lanGhi = ((Date) cuoi.get(1)[0]).getTime();
                return a;
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO " + BANG + " (thoi_gian, so_nguoi, online, tong_xu, tong_luong,"
                    + " tong_luong_khoa, xu_vao, xu_ra) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(a.luc)));
                ps.setInt(2, a.nguoiHoatDong);
                ps.setInt(3, a.online);
                ps.setLong(4, a.tongXu);
                ps.setLong(5, a.tongLuong);
                ps.setLong(6, a.tongLuongKhoa);
                ps.setLong(7, a.xuVao);
                ps.setLong(8, a.xuRa);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + BANG + " WHERE thoi_gian < DATE_SUB(NOW(), INTERVAL 30 DAY)")) {
                ps.executeUpdate();
            }
            lanGhi = a.luc;
        } catch (final Exception ignored) {
        } finally {
            ThaoTacMayChu.tra(conn);
        }
        return a;
    }

    /** Mau theo gio (cu -> moi), 30 ngay: {thoi_gian ms, tong_xu, xu_vao, xu_ra}. */
    public static List<long[]> mauGio() {
        final List<long[]> l = new ArrayList<long[]>();
        Connection conn = null;
        try {
            conn = data.Database.instance.getConnection();
            taoBang(conn);
            final List<Object[]> r = new ArrayList<Object[]>();
            ThaoTacMayChu.truyVan(conn, r, "SELECT thoi_gian, tong_xu, xu_vao, xu_ra FROM " + BANG + " ORDER BY thoi_gian");
            for (int i = 1; i < r.size(); i++) {
                final Object[] d = r.get(i);
                l.add(new long[]{d[0] instanceof Date ? ((Date) d[0]).getTime() : 0L, so(d[1]), so(d[2]), so(d[3])});
            }
        } catch (final Exception ignored) {
        } finally {
            ThaoTacMayChu.tra(conn);
        }
        return l;
    }

    /**
     * Gop chuoi theo NGAY (yyyy-MM-dd -> {tong_xu, xu_vao, xu_ra}), cu -> moi:
     * chot ngay (tob_kinh_te_ngay) uu tien; ngay chua chot dung mau gio cuoi cung cua ngay; hom nay = anh chup hien tai.
     *
     * @param ngay cac dong {ngay(String/Date), tong_xu, xu_vao, xu_ra} tu tob_kinh_te_ngay
     */
    public static LinkedHashMap<String, long[]> chuoiNgay(final List<Object[]> ngay, final List<long[]> gio, final Anh hienTai) {
        final java.util.TreeMap<String, long[]> m = new java.util.TreeMap<String, long[]>();
        final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        if (gio != null) {
            for (final long[] g : gio) {
                m.put(f.format(new Date(g[0])), new long[]{g[1], g[2], g[3]});
            }
        }
        if (ngay != null) {
            for (final Object[] d : ngay) {
                final String k = d[0] instanceof Date ? f.format((Date) d[0]) : String.valueOf(d[0]).substring(0, Math.min(10,
                        String.valueOf(d[0]).length()));
                m.put(k, new long[]{so(d[1]), so(d[2]), so(d[3])});
            }
        }
        if (hienTai != null) {
            // tong xu: so do hien tai; xu vao/ra: giu so cua ngay (neu co) - bo dem song chi tinh tu lan chot / khoi dong
            // gan nhat, dua vao chuoi ngay se tao "vuc" gia o cuoi bieu do.
            final String k = f.format(new Date(hienTai.luc));
            final long[] cu = m.get(k);
            m.put(k, cu == null ? new long[]{hienTai.tongXu, hienTai.xuVao, hienTai.xuRa} : new long[]{hienTai.tongXu, cu[1], cu[2]});
        }
        final LinkedHashMap<String, long[]> kq = new LinkedHashMap<String, long[]>();
        final String tu = f.format(new Date(System.currentTimeMillis() - 30L * 86400000L));
        for (final Map.Entry<String, long[]> e : m.entrySet()) {
            if (e.getKey().compareTo(tu) >= 0) {
                kq.put(e.getKey(), e.getValue());
            }
        }
        return kq;
    }
}
