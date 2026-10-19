package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Vector;
import real.Char;
import real.CharManager;
import real.Map;
import server.TeamServer;

/**
 * GHI LAI VI TRI (MAP + TOA DO) CUA MOI NGUOI CHOI DANG ONLINE.
 *
 * Truoc day muon biet ai dang o map nao chi co cach doc RAM cua tien trinh Java
 * (CharManager.instance.vChars) - web PHP la tien trinh RIENG, khong doc duoc RAM do,
 * chi noi chuyen qua CSDL. Lop nay lay MAU TOAN BO nguoi choi online moi CHU_KY_MS,
 * ghi de len bang tob_vi_tri_online de trang quan tri doc va hien "ai dang o dau".
 *
 * LA SNAPSHOT, KHONG PHAI LOG LICH SU: moi chu ky XOA HET roi ghi lai tu dau, nen bang
 * nay LUON chi chua dung nhung nguoi ONLINE tai thoi diem lay mau gan nhat - khong can
 * don dong cu rieng, khong co dong "ma" (nguoi da logout) ton dong lai.
 *
 * DEM NHAN VAT THAT BANG getSession() != null (giong GhiSoOnline.demNhanVat) de loai bot
 * va linh thue (charHire) - ca hai deu co isBot=-1 giong nguoi choi that nhung session null.
 */
public final class GhiViTriOnline implements Runnable {

    public static final String TEN_BANG = "tob_vi_tri_online";

    /** Lay mau moi 30 giay - du moi de admin xem, khong qua nang CSDL o CCU lon. */
    public static final long CHU_KY_MS = 30L * 1000L;

    /** Cot client (web /admin/online: Tai khoan, Client). */
    static final String[] COT_THEM = {
        "`tai_khoan` VARCHAR(45) NOT NULL DEFAULT ''",
        "`client` VARCHAR(10) NOT NULL DEFAULT ''",
        "`chinh_chu` TINYINT NOT NULL DEFAULT 0",
        "`client_ban` VARCHAR(20) NOT NULL DEFAULT ''",
        "`platf` VARCHAR(100) NOT NULL DEFAULT ''",
    };

    private static volatile boolean daChay = false;
    private static boolean daBaoLoi = false;

    /** Goi mot lan luc khoi dong server. Goi lai khong tao them thread. */
    public static synchronized void batDau() {
        if (daChay) {
            return;
        }
        daChay = true;
        final Thread t = new Thread(new GhiViTriOnline());
        t.setName("GHI VI TRI ONLINE");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void run() {
        taoBang();
        GhiClient.taoBangLucKhoiDong();
        while (TeamServer.running) {
            try {
                ghiMauViTri();
            } catch (final Exception e) {
                baoLoiMotLan(e);
            }
            try {
                Thread.sleep(CHU_KY_MS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    static void taoBang() {
        Connection conn = null;
        Statement st = null;
        try {
            conn = Database.instance.getConnection();
            st = conn.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS `" + TEN_BANG + "` ("
                    + "`charname` VARCHAR(45) NOT NULL,"
                    + "`map_id` INT NOT NULL DEFAULT 0,"
                    + "`map_ten` VARCHAR(100) NOT NULL DEFAULT '',"
                    + "`khu` INT NOT NULL DEFAULT 0,"
                    + "`x` INT NOT NULL DEFAULT 0,"
                    + "`y` INT NOT NULL DEFAULT 0,"
                    + "`cap_nhat_luc` DATETIME NOT NULL,"
                    + "`tai_khoan` VARCHAR(45) NOT NULL DEFAULT '',"
                    + "`client` VARCHAR(10) NOT NULL DEFAULT '',"
                    + "`chinh_chu` TINYINT NOT NULL DEFAULT 0,"
                    + "`client_ban` VARCHAR(20) NOT NULL DEFAULT '',"
                    + "`platf` VARCHAR(100) NOT NULL DEFAULT '',"
                    + "PRIMARY KEY (`charname`)"
                    + ") DEFAULT CHARSET=utf8mb4");
            // Bang tao tu ban cu (truoc 19/10/2026) thieu 5 cot client: them tung cot, cot da co thi bo qua.
            for (final String cot : COT_THEM) {
                try {
                    st.execute("ALTER TABLE `" + TEN_BANG + "` ADD COLUMN " + cot);
                } catch (final Exception daCo) {
                }
            }
        } catch (final Exception e) {
            baoLoiMotLan(e);
        } finally {
            dong(st);
            tra(conn);
        }
    }

    /**
     * Xoa het roi ghi lai toan bo snapshot trong MOT lan mo ket noi. Duyet lui va bat
     * ngoai le tung vong giong GhiSoOnline.demNhanVat - vChars la Vector dung chung, co
     * the ngan di ngay giua luc dang duyet.
     */
    static void ghiMauViTri() {
        Connection conn = null;
        Statement stXoa = null;
        PreparedStatement ps = null;
        try {
            conn = Database.instance.getConnection();
            stXoa = conn.createStatement();
            stXoa.execute("DELETE FROM `" + TEN_BANG + "`");

            ps = conn.prepareStatement("INSERT INTO `" + TEN_BANG
                    + "` (charname, map_id, map_ten, khu, x, y, cap_nhat_luc, tai_khoan, client, chinh_chu, client_ban, platf)"
                    + " VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?)");
            final Vector<Char> ds = CharManager.instance.vChars;
            int soDong = 0;
            for (int i = ds.size() - 1; i >= 0; --i) {
                try {
                    final Char c = ds.get(i);
                    if (c == null || c.getSession() == null) {
                        continue;
                    }
                    ps.setString(1, c.charname);
                    ps.setInt(2, c.mapID);
                    ps.setString(3, Map.getNameMap(c.mapID));
                    ps.setInt(4, c.region);
                    ps.setInt(5, c.x);
                    ps.setInt(6, c.y);
                    final io.Session ss = c.getSession();
                    final String tk = ss.username == null ? "" : ss.username;
                    final GhiClient.ThongTin tt = GhiClient.ganNhat(tk);
                    ps.setString(7, GhiClient.cat(tk, 45));
                    ps.setString(8, tt == null ? "" : tt.loai);
                    ps.setInt(9, tt != null && tt.chinhChu ? 1 : 0);
                    ps.setString(10, tt == null || tt.ban < 0 ? "" : ("v" + tt.ban));
                    ps.setString(11, tt == null ? "" : tt.platf);
                    ps.addBatch();
                    ++soDong;
                } catch (final Exception ignored) {
                }
            }
            if (soDong > 0) {
                ps.executeBatch();
            }
        } catch (final Exception e) {
            baoLoiMotLan(e);
        } finally {
            dong(ps);
            dong(stXoa);
            tra(conn);
        }
    }

    /** Chi in mot lan: thread nay chay mai, loi lap lai se ngap log. */
    private static synchronized void baoLoiMotLan(final Exception e) {
        if (daBaoLoi) {
            return;
        }
        daBaoLoi = true;
        System.out.println("GHI VI TRI ONLINE: loi (chi bao mot lan) - " + e);
    }

    private static void dong(final Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (final Exception ignored) {
        }
    }

    private static void tra(final Connection conn) {
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (final Exception ignored) {
        }
    }

    private GhiViTriOnline() {
    }
}
