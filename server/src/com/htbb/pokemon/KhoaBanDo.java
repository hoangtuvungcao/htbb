package com.htbb.pokemon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * KHOA BAN DO (tien trinh kieu NRO / NSO): luat vao tung ban do goc doc tu bang {@code data_ban_do_mo_khoa} (CSDL la
 * nguon, sql/htbb.sql phan 7). Mot dong / ban do goc (Map.mapIDLoadMap):
 * {@code cap_toi_thieu}, {@code ma_nhiem_vu} (+ {@code ten_nhiem_vu} de bao), {@code cho_phep_tu_dong} +
 * {@code ly_do_tu_dong}. Khong co dong = mo, cho tu dong.
 *
 * Luat mo: (cap >= cap_toi_thieu va (ma rong hoac da xong ma)) HOAC cap_toi_thieu <= moc mien tru cua nhan vat
 * (tob_ban_do_mo: nhan vat cu duoc mien tru theo cap / ban do luc dang nhap dau - khong bao gio bi bo roi).
 * Lop thuan (khong Char) de test; real.KhoaBanDoGame noi vao game. Nap lai moi {@link #CHU_KY_MS} theo chu ky
 * (COUNT, MAX(sua_luc)) nhu TenHien.
 */
public final class KhoaBanDo {

    public static final String BANG = "data_ban_do_mo_khoa";
    public static final String BANG_MIEN = "tob_ban_do_mo";
    public static final long CHU_KY_MS = 30000L;
    /** Co goi -108: ban do dang khoa voi nguoi choi / khong cho tu dong. */
    public static final int CO_KHOA = 1, CO_KHONG_TU_DONG = 2;

    /** Mot luat. Bat bien. */
    public static final class Luat {
        public final int mapId;
        public final int cap;
        public final String ma;
        public final String tenNhiemVu;
        public final boolean tuDong;
        public final String lyDoTuDong;

        public Luat(final int mapId, final int cap, final String ma, final String tenNhiemVu, final boolean tuDong,
                final String lyDoTuDong) {
            this.mapId = mapId;
            this.cap = Math.max(0, cap);
            this.ma = ma == null ? "" : ma.trim();
            this.tenNhiemVu = tenNhiemVu == null ? "" : tenNhiemVu.trim();
            this.tuDong = tuDong;
            this.lyDoTuDong = lyDoTuDong == null ? "" : lyDoTuDong.trim();
        }

        /** Luat co khoa vao khong (cap > 1 hoac can nhiem vu). */
        public boolean coKhoa() {
            return cap > 1 || !ma.isEmpty();
        }
    }

    private static volatile Map<Integer, Luat> LUAT = Collections.emptyMap();
    private static volatile String chuKy = "";
    private static volatile boolean daNap = false;
    private static Thread luong;

    private KhoaBanDo() {
    }

    public static Luat luat(final int mapGoc) {
        return LUAT.get(Integer.valueOf(mapGoc));
    }

    public static Map<Integer, Luat> bang() {
        return LUAT;
    }

    public static boolean daNap() {
        return daNap;
    }

    /** Test / cong cu: dat bang tu ngoai. */
    public static void datBang(final Map<Integer, Luat> m) {
        LUAT = Collections.unmodifiableMap(new HashMap<Integer, Luat>(m));
        daNap = true;
    }

    // ------------------------------------------------------------------ luat

    /** Mo voi nhan vat? {@code daXong}: da xong ma_nhiem_vu (bo qua neu ma rong). */
    public static boolean mo(final Luat l, final int cap, final boolean daXong, final int mocMien) {
        if (l == null || !l.coKhoa()) {
            return true;
        }
        if (l.cap <= mocMien) {
            return true;
        }
        return cap >= l.cap && (l.ma.isEmpty() || daXong);
    }

    /**
     * Cau bao khi bi chan, vd "Cần hoàn thành nhiệm vụ «Cửa ải: Đường sang Johto» và đạt cấp 27 để vào New Bark."
     * Chi noi dieu kien con thieu. null neu mo.
     */
    public static String lyDo(final Luat l, final int cap, final boolean daXong, final int mocMien, final String tenMap) {
        if (mo(l, cap, daXong, mocMien)) {
            return null;
        }
        final boolean thieuNv = !l.ma.isEmpty() && !daXong;
        final boolean thieuCap = cap < l.cap;
        final String ten = tenMap == null || tenMap.isEmpty() ? "bản đồ này" : tenMap;
        final String nv = "nhiệm vụ «" + (l.tenNhiemVu.isEmpty() ? l.ma : l.tenNhiemVu) + "»";
        final StringBuilder sb = new StringBuilder("Cần ");
        if (thieuNv) {
            sb.append("hoàn thành ").append(nv);
        }
        if (thieuNv && thieuCap) {
            sb.append(" và ");
        }
        if (thieuCap) {
            sb.append("đạt cấp ").append(l.cap);
        }
        sb.append(" để vào ").append(ten).append('.');
        if (thieuNv && l.ma.startsWith("CA-")) {
            sb.append(" Mở Sổ tay huấn luyện → Thử thách cửa ải.");
        }
        return sb.toString();
    }

    /** Cau bao khi ban do khong cho tu dong. */
    public static String lyDoTuDong(final Luat l) {
        if (l == null || l.tuDong) {
            return null;
        }
        return "Bản đồ này không cho phép tự động đánh" + (l.lyDoTuDong.isEmpty() ? "." : ": " + l.lyDoTuDong);
    }

    /** Moc mien tru lan dau: max(cap, cap toi thieu cua ban do dang luu). */
    public static int mocMienDau(final int cap, final Luat banDoLuu) {
        return Math.max(Math.max(1, cap), banDoLuu == null ? 0 : banDoLuu.cap);
    }

    // ------------------------------------------------------------------ nap tu CSDL

    public static synchronized boolean nap() {
        Connection c = null;
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            c = data.Database.instance.getConnection();
            p = c.prepareStatement("SELECT map_id, cap_toi_thieu, ma_nhiem_vu, ten_nhiem_vu, cho_phep_tu_dong, ly_do_tu_dong FROM "
                    + BANG);
            rs = p.executeQuery();
            final Map<Integer, Luat> m = new HashMap<Integer, Luat>();
            while (rs.next()) {
                final int id = rs.getInt(1);
                m.put(Integer.valueOf(id), new Luat(id, rs.getInt(2), rs.getString(3), rs.getString(4), rs.getInt(5) != 0,
                        rs.getString(6)));
            }
            LUAT = Collections.unmodifiableMap(m);
            daNap = true;
            System.out.println("[KHOA BAN DO] da nap " + m.size() + " luat ban do tu " + BANG);
        } catch (final Exception e) {
            System.out.println("############################################################");
            System.out.println("[KHOA BAN DO] LOI: khong doc duoc bang " + BANG + " (" + e + ") -> moi ban do MO.");
            System.out.println("[KHOA BAN DO] Chay: sudo bash /opt/htbb/vps/nang_cap_csdl.sh (nap sql/htbb.sql).");
            System.out.println("############################################################");
            daNap = false;
            return false;
        } finally {
            dong(rs, p, c);
        }
        chuKy = docChuKy();
        return true;
    }

    /** Luong nen nap lai khi bang doi; goi {@code sauKhiNap} sau moi lan nap lai thanh cong. */
    public static synchronized void batNapLaiTuDong(final Runnable sauKhiNap) {
        if (luong != null) {
            return;
        }
        luong = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(CHU_KY_MS);
                        final String ky = docChuKy();
                        if (!daNap || !ky.equals(chuKy)) {
                            if (nap() && sauKhiNap != null) {
                                sauKhiNap.run();
                            }
                        }
                    } catch (final InterruptedException e) {
                        return;
                    } catch (final Throwable t) {
                        System.out.println("[KHOA BAN DO] loi nap lai: " + t);
                    }
                }
            }
        }, "KhoaBanDo-napLai");
        luong.setDaemon(true);
        luong.start();
    }

    private static String docChuKy() {
        Connection c = null;
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            c = data.Database.instance.getConnection();
            p = c.prepareStatement("SELECT COUNT(*), MAX(sua_luc) FROM " + BANG);
            rs = p.executeQuery();
            return rs.next() ? rs.getLong(1) + "/" + rs.getString(2) : "";
        } catch (final Exception e) {
            return "loi";
        } finally {
            dong(rs, p, c);
        }
    }

    /** Moc mien tru da luu; -1 = chua co dong (lan dau). */
    public static int docMien(final int idChar) {
        Connection c = null;
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            c = data.Database.instance.getConnection();
            p = c.prepareStatement("SELECT moc_mien FROM " + BANG_MIEN + " WHERE id_char = ?");
            p.setInt(1, idChar);
            rs = p.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (final Exception e) {
            System.out.println("[KHOA BAN DO] doc mien tru " + idChar + ": " + e);
            return -2; // loi CSDL: coi nhu chua biet (KhoaBanDoGame mien tru tam theo cap, khong ghi)
        } finally {
            dong(rs, p, c);
        }
    }

    /** Ghi / nang moc mien tru (chi tang: GREATEST). */
    public static void ghiMien(final int idChar, final int moc, final String nguon) {
        Connection c = null;
        PreparedStatement p = null;
        try {
            c = data.Database.instance.getConnection();
            p = c.prepareStatement("INSERT INTO " + BANG_MIEN + " (id_char, moc_mien, nguon, luc) VALUES (?, ?, ?, NOW())"
                    + " ON DUPLICATE KEY UPDATE moc_mien = GREATEST(moc_mien, VALUES(moc_mien)), nguon = VALUES(nguon), luc = NOW()");
            p.setInt(1, idChar);
            p.setInt(2, moc);
            p.setString(3, nguon == null ? "" : nguon.length() > 16 ? nguon.substring(0, 16) : nguon);
            p.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[KHOA BAN DO] ghi mien tru " + idChar + ": " + e);
        } finally {
            dong(null, p, c);
        }
    }

    /** Server tu tao bang mien tru (htbb.sql cung tao). */
    public static void taoBang() {
        Connection c = null;
        PreparedStatement p = null;
        try {
            c = data.Database.instance.getConnection();
            p = c.prepareStatement("CREATE TABLE IF NOT EXISTS " + BANG_MIEN + " (id_char INT NOT NULL, moc_mien INT NOT NULL DEFAULT 0,"
                    + " nguon VARCHAR(16) NOT NULL DEFAULT '', luc DATETIME NULL DEFAULT NULL, PRIMARY KEY (id_char)) DEFAULT CHARSET=utf8");
            p.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[KHOA BAN DO] tao bang " + BANG_MIEN + ": " + e);
        } finally {
            dong(null, p, c);
        }
    }

    private static void dong(final ResultSet rs, final PreparedStatement p, final Connection c) {
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
                data.Database.connPool.free(c);
            }
        } catch (final Exception ignored) {
        }
    }
}
