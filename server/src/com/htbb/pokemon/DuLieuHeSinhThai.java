package com.htbb.pokemon;

import data.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSDL cua he sinh thai (du_lieu/sql_nguon/pokemon_he_sinh_thai.sql). Moi SQL dung PreparedStatement, cu phap
 * portable (MariaDB + NovaDB). Moi thao tac "tranh mua" (mua tin ban, giao don, nhan hop thu) dung
 * UPDATE/DELETE co dieu kien roi kiem so dong - chi mot ben thang.
 *
 *   tob_hst_nhan_vat (id_char, du_lieu, cap_nhat) : HoSoSinhThai.chuoi()
 *   tob_hst_cho      : tin rao ban (trang_thai 0 mo, 1 da ban, 2 da huy, 3 het han)
 *   tob_hst_don      : don dat hang (0 mo, 1 du hang, 2 huy, 3 het han)
 *   tob_hst_hop_thu  : xu / hang gui cho nguoi dang offline (nhan qua menu)
 */
public final class DuLieuHeSinhThai {

    private DuLieuHeSinhThai() {
    }

    static void dong(final AutoCloseable... a) {
        for (final AutoCloseable c : a) {
            if (c != null) {
                try {
                    c.close();
                } catch (final Exception ignored) {
                }
            }
        }
    }

    static Connection ketNoi() throws Exception {
        return Database.instance.getConnection();
    }

    /** Tao bang nguoi choi neu thieu (bang data_hst_* chi tu tep SQL). */
    public static void taoBang() {
        final String[] ddl = {
            "CREATE TABLE IF NOT EXISTS tob_hst_nhan_vat (id_char INT NOT NULL, du_lieu TEXT NOT NULL,"
                + " cap_nhat BIGINT NOT NULL, PRIMARY KEY (id_char)) DEFAULT CHARSET=utf8",
            "CREATE TABLE IF NOT EXISTS tob_hst_cho (id INT NOT NULL AUTO_INCREMENT, id_ban INT NOT NULL,"
                + " ten_ban VARCHAR(64) NOT NULL, khoa_vat INT NOT NULL, so_luong INT NOT NULL, gia_moi BIGINT NOT NULL,"
                + " du_lieu VARCHAR(64) NOT NULL, het_han BIGINT NOT NULL, trang_thai INT NOT NULL, PRIMARY KEY (id))"
                + " DEFAULT CHARSET=utf8",
            "CREATE TABLE IF NOT EXISTS tob_hst_don (id INT NOT NULL AUTO_INCREMENT, id_mua INT NOT NULL,"
                + " ten_mua VARCHAR(64) NOT NULL, ma_vat INT NOT NULL, so_can INT NOT NULL, so_da_giao INT NOT NULL,"
                + " gia_moi BIGINT NOT NULL, het_han BIGINT NOT NULL, trang_thai INT NOT NULL, PRIMARY KEY (id))"
                + " DEFAULT CHARSET=utf8",
            "CREATE TABLE IF NOT EXISTS tob_hst_hop_thu (id INT NOT NULL AUTO_INCREMENT, id_char INT NOT NULL,"
                + " xu BIGINT NOT NULL, khoa_vat INT NOT NULL, so_luong INT NOT NULL, du_lieu VARCHAR(64) NOT NULL,"
                + " ghi_chu VARCHAR(128) NOT NULL, tao_luc BIGINT NOT NULL, PRIMARY KEY (id)) DEFAULT CHARSET=utf8",
        };
        Connection c = null;
        try {
            c = ketNoi();
            for (final String s : ddl) {
                final PreparedStatement p = c.prepareStatement(s);
                try {
                    p.executeUpdate();
                } finally {
                    dong(p);
                }
            }
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] LOI tao bang: " + e);
        } finally {
            dong(c);
        }
    }

    // ------------------------------------------------------------------ du lieu the gioi

    /** map_id -> vung. Rong neu thieu bang. */
    public static Map<Integer, Integer> napVung() {
        final Map<Integer, Integer> m = new HashMap<Integer, Integer>();
        docBang("SELECT map_id, vung FROM data_hst_vung", (rs) -> m.put(rs.getInt(1), rs.getInt(2)));
        return m;
    }

    /** Dong data_hst_thay_quai: {id_goc, id_thay, trong_so, hiem, thoi_tiet, ngay_dem, mat_na_thang}. */
    public static List<int[]> napThayQuai() {
        final List<int[]> r = new ArrayList<int[]>();
        docBang("SELECT id_goc, id_thay, trong_so, hiem, thoi_tiet, ngay_dem, mat_na_thang FROM data_hst_thay_quai"
                + " ORDER BY id_goc, id_thay", (rs) -> r.add(new int[]{rs.getInt(1), rs.getInt(2), rs.getInt(3),
                    rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7)}));
        return r;
    }

    public static List<int[]> napBungPhat() {
        final List<int[]> r = new ArrayList<int[]>();
        docBang("SELECT map_id, id_quai FROM data_hst_bung_phat ORDER BY map_id, id_quai",
                (rs) -> r.add(new int[]{rs.getInt(1), rs.getInt(2)}));
        return r;
    }

    /** vung -> dex quai cua vung (data_hst_vung_loai). */
    public static Map<Integer, java.util.Set<Integer>> napVungLoai() {
        final Map<Integer, java.util.Set<Integer>> m = new HashMap<Integer, java.util.Set<Integer>>();
        docBang("SELECT vung, dex FROM data_hst_vung_loai ORDER BY vung, dex",
                (rs) -> m.computeIfAbsent(rs.getInt(1), (k) -> new java.util.TreeSet<Integer>()).add(rs.getInt(2)));
        return m;
    }

    interface DocDong {
        void doc(ResultSet rs) throws Exception;
    }

    static void docBang(final String sql, final DocDong f) {
        Connection c = null;
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            c = ketNoi();
            p = c.prepareStatement(sql);
            rs = p.executeQuery();
            while (rs.next()) {
                f.doc(rs);
            }
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] khong doc duoc (" + sql.substring(0, Math.min(60, sql.length())) + "): " + e);
        } finally {
            dong(rs, p, c);
        }
    }

    // ------------------------------------------------------------------ ho so

    /** Chuoi ho so; "" = chua co; null = loi CSDL (noi goi KHONG duoc ghi de). */
    public static String napHoSo(final int idChar) {
        Connection c = null;
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            c = ketNoi();
            p = c.prepareStatement("SELECT du_lieu FROM tob_hst_nhan_vat WHERE id_char = ?");
            p.setInt(1, idChar);
            rs = p.executeQuery();
            return rs.next() ? rs.getString(1) : "";
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] LOI nap ho so " + idChar + ": " + e);
            return null;
        } finally {
            dong(rs, p, c);
        }
    }

    public static boolean luuHoSo(final int idChar, final String duLieu) {
        Connection c = null;
        PreparedStatement p = null;
        try {
            c = ketNoi();
            p = c.prepareStatement("UPDATE tob_hst_nhan_vat SET du_lieu = ?, cap_nhat = ? WHERE id_char = ?");
            p.setString(1, duLieu);
            p.setLong(2, com.htbb.pokemon.DongHo.bay());
            p.setInt(3, idChar);
            if (p.executeUpdate() == 0) {
                dong(p);
                p = c.prepareStatement("INSERT INTO tob_hst_nhan_vat (id_char, du_lieu, cap_nhat) VALUES (?, ?, ?)");
                p.setInt(1, idChar);
                p.setString(2, duLieu);
                p.setLong(3, com.htbb.pokemon.DongHo.bay());
                p.executeUpdate();
            }
            return true;
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] LOI luu ho so " + idChar + ": " + e);
            return false;
        } finally {
            dong(p, c);
        }
    }

    // ------------------------------------------------------------------ cho

    /** Mot tin ban: {id, id_ban, khoa_vat, so_luong, gia_moi, het_han}, ten_ban, du_lieu. */
    public static final class TinBan {
        public int id, idBan, khoaVat, soLuong;
        public long giaMoi, hetHan;
        public String tenBan, duLieu;
    }

    public static int themTinBan(final int idBan, final String tenBan, final int khoaVat, final int so, final long giaMoi,
            final String duLieu, final long hetHan) {
        Connection c = null;
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            c = ketNoi();
            p = c.prepareStatement("INSERT INTO tob_hst_cho (id_ban, ten_ban, khoa_vat, so_luong, gia_moi, du_lieu, het_han,"
                    + " trang_thai) VALUES (?, ?, ?, ?, ?, ?, ?, 0)", Statement.RETURN_GENERATED_KEYS);
            p.setInt(1, idBan);
            p.setString(2, tenBan);
            p.setInt(3, khoaVat);
            p.setInt(4, so);
            p.setLong(5, giaMoi);
            p.setString(6, duLieu == null ? "" : duLieu);
            p.setLong(7, hetHan);
            p.executeUpdate();
            rs = p.getGeneratedKeys();
            return rs != null && rs.next() ? rs.getInt(1) : 0;
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] LOI them tin ban: " + e);
            return -1;
        } finally {
            dong(rs, p, c);
        }
    }

    /** Tin dang mo; idBan > 0 = chi cua nguoi do; loai >= 0 = loc theo loai vat lieu. */
    public static List<TinBan> dsTinBan(final int idBan, final int loai, final int toiDa) {
        final List<TinBan> r = new ArrayList<TinBan>();
        final long now = com.htbb.pokemon.DongHo.bay();
        docBang("SELECT id, id_ban, ten_ban, khoa_vat, so_luong, gia_moi, du_lieu, het_han FROM tob_hst_cho"
                + " WHERE trang_thai = 0 ORDER BY id DESC", (rs) -> {
                    if (r.size() >= toiDa) {
                        return;
                    }
                    final TinBan t = new TinBan();
                    t.id = rs.getInt(1);
                    t.idBan = rs.getInt(2);
                    t.tenBan = rs.getString(3);
                    t.khoaVat = rs.getInt(4);
                    t.soLuong = rs.getInt(5);
                    t.giaMoi = rs.getLong(6);
                    t.duLieu = rs.getString(7);
                    t.hetHan = rs.getLong(8);
                    if (t.hetHan < now || idBan > 0 && t.idBan != idBan
                            || loai >= 0 && VatLieuSinhThai.loai(VatLieuSinhThai.maCuaKhoa(t.khoaVat)) != loai) {
                        return;
                    }
                    r.add(t);
                });
        return r;
    }

    public static TinBan layTinBan(final int id) {
        final TinBan[] r = new TinBan[1];
        Connection c = null;
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            c = ketNoi();
            p = c.prepareStatement("SELECT id, id_ban, ten_ban, khoa_vat, so_luong, gia_moi, du_lieu, het_han FROM tob_hst_cho"
                    + " WHERE id = ? AND trang_thai = 0");
            p.setInt(1, id);
            rs = p.executeQuery();
            if (rs.next()) {
                final TinBan t = new TinBan();
                t.id = rs.getInt(1);
                t.idBan = rs.getInt(2);
                t.tenBan = rs.getString(3);
                t.khoaVat = rs.getInt(4);
                t.soLuong = rs.getInt(5);
                t.giaMoi = rs.getLong(6);
                t.duLieu = rs.getString(7);
                t.hetHan = rs.getLong(8);
                r[0] = t;
            }
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] LOI lay tin ban: " + e);
        } finally {
            dong(rs, p, c);
        }
        return r[0];
    }

    /** Doi trang thai tin 0 -> moi (chi 1 ben thang). {@code idBan} > 0: chi nguoi ban do. */
    public static boolean chotTinBan(final int id, final int trangThaiMoi, final int idBan) {
        return capNhat("UPDATE tob_hst_cho SET trang_thai = ? WHERE id = ? AND trang_thai = 0"
                + (idBan > 0 ? " AND id_ban = ?" : ""), trangThaiMoi, id, idBan) == 1;
    }

    static int capNhat(final String sql, final long... ts) {
        Connection c = null;
        PreparedStatement p = null;
        try {
            c = ketNoi();
            p = c.prepareStatement(sql);
            final int n = sql.length() - sql.replace("?", "").length();
            for (int i = 0; i < n && i < ts.length; i++) {
                p.setLong(i + 1, ts[i]);
            }
            return p.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] LOI cap nhat: " + e);
            return -1;
        } finally {
            dong(p, c);
        }
    }

    /** Tin ban da het han con mo (de tra hang ve hop thu). */
    public static List<TinBan> tinHetHan() {
        final List<TinBan> r = new ArrayList<TinBan>();
        final long now = com.htbb.pokemon.DongHo.bay();
        docBang("SELECT id, id_ban, ten_ban, khoa_vat, so_luong, gia_moi, du_lieu, het_han FROM tob_hst_cho"
                + " WHERE trang_thai = 0", (rs) -> {
                    if (rs.getLong(8) >= now) {
                        return;
                    }
                    final TinBan t = new TinBan();
                    t.id = rs.getInt(1);
                    t.idBan = rs.getInt(2);
                    t.tenBan = rs.getString(3);
                    t.khoaVat = rs.getInt(4);
                    t.soLuong = rs.getInt(5);
                    t.giaMoi = rs.getLong(6);
                    t.duLieu = rs.getString(7);
                    t.hetHan = rs.getLong(8);
                    r.add(t);
                });
        return r;
    }

    // ------------------------------------------------------------------ don dat hang

    public static final class Don {
        public int id, idMua, maVat, soCan, soDaGiao;
        public long giaMoi, hetHan;
        public String tenMua;
    }

    public static int themDon(final int idMua, final String tenMua, final int ma, final int so, final long gia, final long hetHan) {
        Connection c = null;
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            c = ketNoi();
            p = c.prepareStatement("INSERT INTO tob_hst_don (id_mua, ten_mua, ma_vat, so_can, so_da_giao, gia_moi, het_han,"
                    + " trang_thai) VALUES (?, ?, ?, ?, 0, ?, ?, 0)", Statement.RETURN_GENERATED_KEYS);
            p.setInt(1, idMua);
            p.setString(2, tenMua);
            p.setInt(3, ma);
            p.setInt(4, so);
            p.setLong(5, gia);
            p.setLong(6, hetHan);
            p.executeUpdate();
            rs = p.getGeneratedKeys();
            return rs != null && rs.next() ? rs.getInt(1) : 0;
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] LOI them don: " + e);
            return -1;
        } finally {
            dong(rs, p, c);
        }
    }

    public static List<Don> dsDon(final int idMua, final int toiDa, final boolean caHetHan) {
        final List<Don> r = new ArrayList<Don>();
        final long now = com.htbb.pokemon.DongHo.bay();
        docBang("SELECT id, id_mua, ten_mua, ma_vat, so_can, so_da_giao, gia_moi, het_han FROM tob_hst_don"
                + " WHERE trang_thai = 0 ORDER BY id DESC", (rs) -> {
                    if (r.size() >= toiDa) {
                        return;
                    }
                    final Don d = new Don();
                    d.id = rs.getInt(1);
                    d.idMua = rs.getInt(2);
                    d.tenMua = rs.getString(3);
                    d.maVat = rs.getInt(4);
                    d.soCan = rs.getInt(5);
                    d.soDaGiao = rs.getInt(6);
                    d.giaMoi = rs.getLong(7);
                    d.hetHan = rs.getLong(8);
                    if (!caHetHan && d.hetHan < now || caHetHan && d.hetHan >= now || idMua > 0 && d.idMua != idMua) {
                        return;
                    }
                    r.add(d);
                });
        return r;
    }

    /** Giao {@code so} cai cho don (chi khi con mo, chua qua so can). Tra true neu ghi duoc. */
    public static boolean giaoDon(final int id, final int so) {
        final int n = capNhat("UPDATE tob_hst_don SET so_da_giao = so_da_giao + ? WHERE id = ? AND trang_thai = 0"
                + " AND so_da_giao + ? <= so_can", so, id, so);
        if (n == 1) {
            capNhat("UPDATE tob_hst_don SET trang_thai = 1 WHERE id = ? AND so_da_giao >= so_can", id);
        }
        return n == 1;
    }

    /** Dong don (huy / het han): tra ve so da giao luc dong, -1 neu khong dong duoc. */
    public static int dongDon(final int id, final int trangThai, final int idMua) {
        final Don[] d = new Don[1];
        docBang("SELECT so_da_giao, id_mua FROM tob_hst_don WHERE id = " + id + " AND trang_thai = 0", (rs) -> {
            d[0] = new Don();
            d[0].soDaGiao = rs.getInt(1);
            d[0].idMua = rs.getInt(2);
        });
        if (d[0] == null || idMua > 0 && d[0].idMua != idMua) {
            return -1;
        }
        final int n = capNhat("UPDATE tob_hst_don SET trang_thai = ? WHERE id = ? AND trang_thai = 0 AND so_da_giao = ?",
                trangThai, id, d[0].soDaGiao);
        return n == 1 ? d[0].soDaGiao : -1;
    }

    // ------------------------------------------------------------------ hop thu

    public static boolean guiHopThu(final int idChar, final long xu, final int khoaVat, final int so, final String duLieu,
            final String ghiChu) {
        Connection c = null;
        PreparedStatement p = null;
        try {
            c = ketNoi();
            p = c.prepareStatement("INSERT INTO tob_hst_hop_thu (id_char, xu, khoa_vat, so_luong, du_lieu, ghi_chu, tao_luc)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?)");
            p.setInt(1, idChar);
            p.setLong(2, xu);
            p.setInt(3, khoaVat);
            p.setInt(4, so);
            p.setString(5, duLieu == null ? "" : duLieu);
            p.setString(6, ghiChu == null ? "" : (ghiChu.length() > 120 ? ghiChu.substring(0, 120) : ghiChu));
            p.setLong(7, com.htbb.pokemon.DongHo.bay());
            return p.executeUpdate() == 1;
        } catch (final Exception e) {
            System.out.println("[HE SINH THAI] LOI gui hop thu " + idChar + ": " + e);
            return false;
        } finally {
            dong(p, c);
        }
    }

    /** Mot thu: {id, xu, khoa_vat, so_luong}, du_lieu, ghi_chu. */
    public static final class Thu {
        public int id, khoaVat, so;
        public long xu;
        public String duLieu, ghiChu;
    }

    public static List<Thu> dsHopThu(final int idChar) {
        final List<Thu> r = new ArrayList<Thu>();
        docBang("SELECT id, xu, khoa_vat, so_luong, du_lieu, ghi_chu FROM tob_hst_hop_thu WHERE id_char = " + idChar
                + " ORDER BY id", (rs) -> {
                    final Thu t = new Thu();
                    t.id = rs.getInt(1);
                    t.xu = rs.getLong(2);
                    t.khoaVat = rs.getInt(3);
                    t.so = rs.getInt(4);
                    t.duLieu = rs.getString(5);
                    t.ghiChu = rs.getString(6);
                    r.add(t);
                });
        return r;
    }

    /** Xoa thu truoc khi phat (chi lan xoa thanh cong moi duoc phat). */
    public static boolean xoaThu(final int id, final int idChar) {
        return capNhat("DELETE FROM tob_hst_hop_thu WHERE id = ? AND id_char = ?", id, idChar) == 1;
    }
}
