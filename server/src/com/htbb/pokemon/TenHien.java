package com.htbb.pokemon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * TEN NGUOI CHOI THAY doc tu SQL (P6): ten NPC tinh (bang {@code data_npc}) va ten ban do (bang {@code data_map_ten}),
 * CSDL la NGUON (sua o web /admin/ten-hien); du lieu dau: migration sql/pokemon_ten_hien.sql = khoi cung ten trong
 * sql/htbb_day_du.sql (test/KiemTenHien kiem: co dau, khong tu cam, du moi NPC / ban do co trong game).
 * Khong con bang ten trong Java: thieu bang -> bao loi TO moi lan nap (server van chay, ten ban do giu ten goc cua
 * Map.namMap - chay sql/pokemon_ten_hien.sql roi doi toi 30 giay).
 *
 * Bo nho dem: hai Map bat bien thay nguyen khoi khi nap lai (doc khong khoa). {@link #batNapLaiTuDong} chay luong nen
 * moi {@link #CHU_KY_MS} so "chu ky" (COUNT, MAX(sua_luc)) cua hai bang - trang web /admin/ten-hien ghi sua_luc = NOW()
 * nen ten moi co hieu luc trong ~30 giay, KHONG can khoi dong lai server (nguoi choi thay o lan vao map ke tiep).
 */
public final class TenHien {

    public static final String BANG_NPC = "data_npc";
    public static final String BANG_MAP = "data_map_ten";
    public static final long CHU_KY_MS = 30000L;

    private static volatile Map<Integer, String> tenMap = Collections.emptyMap();
    private static volatile Map<Integer, String> tenNpc = Collections.emptyMap();
    private static volatile String chuKy = "";
    private static volatile boolean daNapDuoc = false;
    private static Thread luong;

    private TenHien() {
    }

    /** Ten ban do co dong rieng trong data_map_ten, "" neu khong co. */
    public static String tenMap(final int id) {
        final String t = tenMap.get(Integer.valueOf(id));
        return t == null ? "" : t;
    }

    /** Ten NPC tinh theo type, null neu khong co. */
    public static String tenNpc(final int type) {
        return tenNpc.get(Integer.valueOf(type));
    }

    public static boolean daNap() {
        return daNapDuoc;
    }

    public static Map<Integer, String> bangMap() {
        return tenMap;
    }

    public static Map<Integer, String> bangNpc() {
        return tenNpc;
    }

    /** Test / cong cu: dat bang tu ngoai (vd doc tu sql/pokemon_ten_hien.sql). */
    public static void datBang(final Map<Integer, String> map, final Map<Integer, String> npc) {
        tenMap = Collections.unmodifiableMap(new HashMap<Integer, String>(map));
        tenNpc = Collections.unmodifiableMap(new HashMap<Integer, String>(npc));
        daNapDuoc = !map.isEmpty() && !npc.isEmpty();
    }

    /** Doc hai bang tu CSDL. false = thieu bang / loi (da in loi). */
    public static synchronized boolean nap() {
        final Map<Integer, String> m = docBang("SELECT map_id, ten FROM " + BANG_MAP);
        final Map<Integer, String> n = docBang("SELECT type, ten FROM " + BANG_NPC);
        if (m == null || n == null || m.isEmpty() || n.isEmpty()) {
            System.out.println("############################################################");
            System.out.println("[TEN HIEN] LOI: thieu / rong bang " + (m == null || m.isEmpty() ? BANG_MAP + " " : "")
                    + (n == null || n.isEmpty() ? BANG_NPC : "") + " -> ten ban do / NPC chua lay duoc tu SQL.");
            System.out.println("[TEN HIEN] Chay: mysql htbb_db < sql/pokemon_ten_hien.sql (server tu nap lai sau <= 30 giay).");
            System.out.println("############################################################");
            daNapDuoc = false;
            return false;
        }
        datBang(m, n);
        chuKy = docChuKy();
        System.out.println("[TEN HIEN] da nap " + m.size() + " ten ban do, " + n.size() + " ten NPC tu SQL");
        return true;
    }

    /**
     * Bat luong nen: moi {@link #CHU_KY_MS} so chu ky hai bang, doi (hoac lan truoc nap hong) thi nap lai roi goi
     * {@code sauKhiNap} (ap lai ten vao bang cua Map). Goi lai lan 2 khong tao luong moi.
     */
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
                        if (!daNapDuoc || !ky.equals(chuKy)) {
                            if (nap() && sauKhiNap != null) {
                                sauKhiNap.run();
                            }
                        }
                    } catch (final InterruptedException e) {
                        return;
                    } catch (final Throwable t) {
                        System.out.println("[TEN HIEN] loi nap lai: " + t);
                    }
                }
            }
        }, "TenHien-napLai");
        luong.setDaemon(true);
        luong.start();
    }

    private static String docChuKy() {
        final StringBuilder sb = new StringBuilder();
        for (final String b : new String[]{BANG_MAP, BANG_NPC}) {
            Connection c = null;
            PreparedStatement p = null;
            ResultSet rs = null;
            try {
                c = data.Database.instance.getConnection();
                p = c.prepareStatement("SELECT COUNT(*), MAX(sua_luc) FROM " + b);
                rs = p.executeQuery();
                if (rs.next()) {
                    sb.append(rs.getLong(1)).append('/').append(rs.getString(2)).append(';');
                }
            } catch (final Exception e) {
                sb.append("loi;");
            } finally {
                dong(rs, p, c);
            }
        }
        return sb.toString();
    }

    private static Map<Integer, String> docBang(final String sql) {
        Connection c = null;
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            c = data.Database.instance.getConnection();
            p = c.prepareStatement(sql);
            rs = p.executeQuery();
            final Map<Integer, String> kq = new HashMap<Integer, String>();
            while (rs.next()) {
                final String t = rs.getString(2);
                if (t != null && !t.trim().isEmpty()) {
                    kq.put(Integer.valueOf(rs.getInt(1)), t.trim());
                }
            }
            return kq;
        } catch (final Exception e) {
            System.out.println("[TEN HIEN] khong doc duoc (" + sql + "): " + e);
            return null;
        } finally {
            dong(rs, p, c);
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
