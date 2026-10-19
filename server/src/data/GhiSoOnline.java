package data;

import io.SessionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Vector;
import real.Char;
import real.CharManager;
import server.TeamServer;

/**
 * GHI LAI SO NGUOI ONLINE THEO THOI GIAN.
 *
 * Truoc day server chi biet so online TUC THOI (SessionManager.instance.size(), dung o
 * AdminPanel va o chan dang nhap), khong luu lai gi ca — nen khong tra loi duoc "toi qua
 * dong nhat bao nhieu nguoi", "sau ban cap nhat thi nguoi choi co bo di khong".
 * Lop nay lay mau dinh ky roi ghi xuong bang tob_log_online de trang Tong quan ve bieu do.
 *
 * HAI CON SO, CO Y GHI CA HAI:
 *   ket_noi  = so socket dang mo (SessionManager). Ke ca dang o man hinh dang nhap /
 *              chon nhan vat, chua vao game.
 *   nhan_vat = so nhan vat THAT dang trong game.
 * Chenh lech giua hai cot chinh la so nguoi ket noi duoc nhung chua vao duoc game — rat
 * hay khi chan doan (vi du sau khi doi mat khau, hoac khi dang nhap bi cham 5 giay).
 *
 * DEM NHAN VAT THAT BANG getSession() != null CHU KHONG BANG isBot:
 * CharManager.vChars chua ca bot. Ma linh thue (charHire) duoc tao bang
 * setInfoChar(ten + " lan su vu", -1, ...) — tham so thu hai chinh la isBot, tuc no
 * mang isBot = -1 Y HET nguoi choi that. Loc theo isBot se dem ca linh thue va nhan doi
 * so nguoi online. Bot deu duoc tao bang new Char(null) nen session cua chung la null.
 *
 * LUU LUONG: 5 phut mot mau = 288 dong/ngay, giu 30 ngay = ~8.600 dong. Khong dang ke
 * so voi tob_log_all_item. Van tu don de khong bao gio phinh (xem donDongCu).
 */
public final class GhiSoOnline implements Runnable {

    public static final String TEN_BANG = "tob_log_online";

    /** Lay mau moi 5 phut. */
    public static final long CHU_KY_MS = 5L * 60L * 1000L;

    /** Giu lai 30 ngay, dong bo voi chinh sach don log khac (vps/don-log.sh). */
    public static final int SO_NGAY_GIU = 30;

    /** Don dong cu moi 6 tieng, khong can thuong xuyen hon. */
    private static final long CHU_KY_DON_MS = 6L * 60L * 60L * 1000L;

    private static volatile boolean daChay = false;
    private static boolean daBaoLoi = false;

    /** Goi mot lan luc khoi dong server. Goi lai khong tao them thread. */
    public static synchronized void batDau() {
        if (daChay) {
            return;
        }
        daChay = true;
        final Thread t = new Thread(new GhiSoOnline());
        t.setName("GHI SO ONLINE");
        t.setDaemon(true);
        t.start();
    }

    /**
     * So nhan vat THAT dang trong game.
     *
     * Duyet lui va bat ngoai le tung vong: vChars la Vector dung chung, no co the ngan di
     * ngay giua luc dang duyet.
     */
    public static int demNhanVat() {
        int n = 0;
        try {
            final Vector<Char> ds = CharManager.instance.vChars;
            for (int i = ds.size() - 1; i >= 0; --i) {
                try {
                    final Char c = ds.get(i);
                    if (c != null && c.getSession() != null) {
                        ++n;
                    }
                } catch (final Exception ignored) {
                }
            }
        } catch (final Exception ignored) {
        }
        return n;
    }

    /** So socket dang mo, ke ca chua vao game. */
    public static int demKetNoi() {
        try {
            return SessionManager.instance.size();
        } catch (final Exception e) {
            return 0;
        }
    }

    @Override
    public void run() {
        taoBang();
        long lanDonTiep = System.currentTimeMillis();
        while (TeamServer.running) {
            try {
                ghiMotMau(demKetNoi(), demNhanVat());
                if (System.currentTimeMillis() >= lanDonTiep) {
                    lanDonTiep = System.currentTimeMillis() + CHU_KY_DON_MS;
                    donDongCu();
                }
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
            // Khong chi dinh ENGINE: de MySQL dung mac dinh cua may, khoi lech voi phan
            // con lai cua schema.
            st.execute("CREATE TABLE IF NOT EXISTS `" + TEN_BANG + "` ("
                    + "`id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
                    + "`thoi_gian` DATETIME NOT NULL,"
                    + "`ket_noi` INT NOT NULL DEFAULT 0,"
                    + "`nhan_vat` INT NOT NULL DEFAULT 0,"
                    + "PRIMARY KEY (`id`),"
                    + "KEY `idx_thoi_gian` (`thoi_gian`)"
                    + ") DEFAULT CHARSET=utf8");
            // So nhan vat theo loai client (web /admin/online). Bang cu thieu cot: them, da co thi bo qua.
            for (final String cot : new String[]{"pc", "android", "jar", "khac"}) {
                try {
                    st.execute("ALTER TABLE `" + TEN_BANG + "` ADD COLUMN `" + cot + "` INT NOT NULL DEFAULT 0");
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

    /** So nhan vat that theo loai client {pc, android, jar, khac} (GhiClient.ganNhat). */
    static int[] demTheoClient() {
        final int[] n = new int[4];
        try {
            final Vector<Char> ds = CharManager.instance.vChars;
            for (int i = ds.size() - 1; i >= 0; --i) {
                try {
                    final Char c = ds.get(i);
                    if (c == null || c.getSession() == null) {
                        continue;
                    }
                    final GhiClient.ThongTin tt = GhiClient.ganNhat(c.getSession().username);
                    final String l = tt == null ? "" : tt.loai;
                    n["pc".equals(l) ? 0 : "android".equals(l) ? 1 : "jar".equals(l) ? 2 : 3]++;
                } catch (final Exception ignored) {
                }
            }
        } catch (final Exception ignored) {
        }
        return n;
    }

    static void ghiMotMau(final int ketNoi, final int nhanVat) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Database.instance.getConnection();
            final int[] l = demTheoClient();
            ps = conn.prepareStatement("INSERT INTO `" + TEN_BANG
                    + "` (thoi_gian, ket_noi, nhan_vat, pc, android, jar, khac) VALUES (NOW(), ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, ketNoi);
            ps.setInt(2, nhanVat);
            for (int i = 0; i < 4; ++i) {
                ps.setInt(3 + i, l[i]);
            }
            ps.execute();
        } catch (final Exception e) {
            baoLoiMotLan(e);
        } finally {
            dong(ps);
            tra(conn);
        }
    }

    static void donDongCu() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Database.instance.getConnection();
            ps = conn.prepareStatement("DELETE FROM `" + TEN_BANG
                    + "` WHERE thoi_gian < DATE_SUB(NOW(), INTERVAL ? DAY)");
            ps.setInt(1, SO_NGAY_GIU);
            final int xoa = ps.executeUpdate();
            if (xoa > 0) {
                System.out.println("GHI SO ONLINE: da don " + xoa + " dong cu hon "
                        + SO_NGAY_GIU + " ngay.");
            }
        } catch (final Exception e) {
            baoLoiMotLan(e);
        } finally {
            dong(ps);
            tra(conn);
        }
    }

    /** Chi in mot lan: thread nay chay mai, loi lap lai se ngap log. */
    private static synchronized void baoLoiMotLan(final Exception e) {
        if (daBaoLoi) {
            return;
        }
        daBaoLoi = true;
        System.out.println("GHI SO ONLINE: loi (chi bao mot lan) - " + e);
    }

    private static void dong(final Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (final Exception ignored) {
        }
    }

    /**
     * Tra ket noi ve pool — DUNG MOT LAN.
     *
     * Khong bao gio goi free() hai lan cho mot lan lay: neu luong khac kip lay ket noi do
     * ra khoi pool o giua, lan free thu hai se day no vao lai TRONG KHI luong kia dang
     * dung, va hai luong se chay cau lenh chong nhau tren cung mot Connection.
     * free(null) cung khong duoc goi vi no van giam countConnection.
     */
    private static void tra(final Connection conn) {
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (final Exception ignored) {
        }
    }

    private GhiSoOnline() {
    }
}
