package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GHI LAI TAI KHOAN NAO VAO GAME BANG CLIENT NAO (bang tob_client_dang_nhap).
 *
 * Trang /admin/online cua web ("So tai khoan da vao game theo client", "Client ngoai")
 * doc bang nay. Truoc 19/10/2026 lop nay CHUA TUNG co trong may chu (web viet cho mot ban
 * cu cua repo HTBB) nen bang khong bao gio duoc tao va trang luon hien 0 / "chua co bang".
 *
 * MOT DONG MOI (tai khoan, loai client): so lan, lan dau, lan cuoi, platf, phien ban, so ban jar,
 * chinh chu. Goi o CA HAI duong dang nhap thanh cong cua LoginHandler (duong login server
 * processLoginOk la duong may that dung; duong ket noi CSDL truc tiep it dung).
 *
 * PHAN LOAI CLIENT: byte firmware khong dung duoc - ca jar, PC lan Android (cung mot ma
 * client_jar) deu gui 0. Phan theo platf (microedition.platform) ma launcher dat:
 *   "HTBB-PC/..."  -> pc       (client_pc/htbb/pc/Chay.java)
 *   "Android/..."  -> android  (client_apk HtbbActivity)
 *   con lai        -> jar      (dien thoai J2ME / gia lap)
 * CHINH CHU = client HTBB chinh thuc: gui ma may (AutoGuard.maMay) VA so ban (PhienBan.BAN).
 * Client goc TeaMobi / client la khong gui hai truong do.
 *
 * Moi loi bi nuot (bao mot lan): day chi la ghi chep, khong duoc lam hong dang nhap.
 */
public final class GhiClient {

    public static final String TEN_BANG = "tob_client_dang_nhap";

    /** Thong tin client cua lan dang nhap gan nhat theo tai khoan (GhiViTriOnline doc). */
    public static final class ThongTin {
        public final String loai;
        public final boolean chinhChu;
        public final int ban;
        public final String platf;

        ThongTin(final String loai, final boolean chinhChu, final int ban, final String platf) {
            this.loai = loai;
            this.chinhChu = chinhChu;
            this.ban = ban;
            this.platf = platf;
        }
    }

    private static final ConcurrentHashMap<String, ThongTin> GAN_NHAT = new ConcurrentHashMap<String, ThongTin>();
    /**
     * platf doc o goi dang nhap, cho toi luc login server tra ket qua (processLoginOk chay o
     * luong khac, khong con bien cuc bo). Khoa yeu: phien bi bo thi tu roi khoi bang.
     */
    private static final java.util.Map<io.Session, String> PLATF_CHO =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<io.Session, String>());
    private static volatile boolean daTaoBang = false;
    private static volatile boolean daBaoLoi = false;

    private GhiClient() {
    }

    /** pc / android / jar theo platf (xem chu thich lop). */
    public static String loaiClient(final String platf) {
        final String p = platf == null ? "" : platf.trim();
        if (p.startsWith("HTBB-PC")) {
            return "pc";
        }
        if (p.startsWith("Android")) {
            return "android";
        }
        return "jar";
    }

    /** Client HTBB chinh thuc: co ma may va co so ban. */
    public static boolean laChinhChu(final String maMay, final int banClient) {
        return maMay != null && !maMay.isEmpty() && banClient >= 0;
    }

    /** Cat chuoi client gui len ve do dai cot. */
    public static String cat(final String s, final int toiDa) {
        if (s == null) {
            return "";
        }
        final String t = s.replace('\n', ' ').replace('\r', ' ').trim();
        return t.length() > toiDa ? t.substring(0, toiDa) : t;
    }

    /** Thong tin client lan dang nhap gan nhat cua tai khoan (null neu chua dang nhap tu khi chay). */
    public static ThongTin ganNhat(final String taiKhoan) {
        return taiKhoan == null ? null : GAN_NHAT.get(taiKhoan.toLowerCase());
    }

    /** Nho platf cua phien (goi luc doc goi dang nhap). */
    public static void nhoPlatf(final io.Session s, final String platf) {
        if (s != null) {
            PLATF_CHO.put(s, cat(platf, 100));
        }
    }

    /** Ghi nhan dang nhap thanh cong cua phien (tai khoan = s.username). */
    public static void ghiNhan(final io.Session s) {
        if (s == null) {
            return;
        }
        final String platf = PLATF_CHO.remove(s);
        ghiNhan(s.username, platf, s.version, s.banClient, s.IMEI);
    }

    /**
     * Ghi nhan mot lan dang nhap thanh cong. Goi SAU khi mat khau dung va khong bi chan.
     */
    public static void ghiNhan(final String taiKhoan, final String platf, final String phienBan,
                               final int banClient, final String maMay) {
        if (taiKhoan == null || taiKhoan.isEmpty()) {
            return;
        }
        final String tk = cat(taiKhoan.toLowerCase(), 45);
        final String loai = loaiClient(platf);
        final boolean chinhChu = laChinhChu(maMay, banClient);
        GAN_NHAT.put(tk, new ThongTin(loai, chinhChu, banClient, cat(platf, 100)));
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            taoBang(conn);
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO `" + TEN_BANG + "`"
                    + " (tai_khoan, loai, platf, phien_ban, ban_jar, chinh_chu, so_lan, lan_dau, lan_cuoi)"
                    + " VALUES (?, ?, ?, ?, ?, ?, 1, NOW(), NOW())"
                    + " ON DUPLICATE KEY UPDATE platf = VALUES(platf), phien_ban = VALUES(phien_ban),"
                    + " ban_jar = VALUES(ban_jar), chinh_chu = VALUES(chinh_chu), so_lan = so_lan + 1, lan_cuoi = NOW()")) {
                ps.setString(1, tk);
                ps.setString(2, loai);
                ps.setString(3, cat(platf, 100));
                ps.setString(4, cat(phienBan, 20));
                ps.setInt(5, Math.max(banClient, 0));
                ps.setInt(6, chinhChu ? 1 : 0);
                ps.executeUpdate();
            }
        } catch (final Exception e) {
            baoLoiMotLan(e);
        } finally {
            try {
                if (conn != null) {
                    Database.connPool.free(conn);
                }
            } catch (final Exception ignored) {
            }
        }
    }

    /** Cung DDL voi du_lieu/sql_nguon/them_bang_tob_client_dang_nhap.sql. */
    static final String DDL = "CREATE TABLE IF NOT EXISTS `" + TEN_BANG + "` ("
            + "`tai_khoan` VARCHAR(45) NOT NULL,"
            + "`loai` VARCHAR(10) NOT NULL DEFAULT 'jar',"
            + "`platf` VARCHAR(100) NOT NULL DEFAULT '',"
            + "`phien_ban` VARCHAR(20) NOT NULL DEFAULT '',"
            + "`ban_jar` INT NOT NULL DEFAULT 0,"
            + "`chinh_chu` TINYINT NOT NULL DEFAULT 0,"
            + "`so_lan` INT NOT NULL DEFAULT 0,"
            + "`lan_dau` DATETIME NOT NULL,"
            + "`lan_cuoi` DATETIME NOT NULL,"
            + "PRIMARY KEY (`tai_khoan`, `loai`),"
            + "KEY `idx_tcdn_lan_cuoi` (`lan_cuoi`)"
            + ") DEFAULT CHARSET=utf8mb4";

    /** Tao bang ngay luc khoi dong (web het bao "chua co bang" truoc lan dang nhap dau). */
    public static void taoBangLucKhoiDong() {
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            taoBang(conn);
        } catch (final Exception e) {
            baoLoiMotLan(e);
        } finally {
            try {
                if (conn != null) {
                    Database.connPool.free(conn);
                }
            } catch (final Exception ignored) {
            }
        }
    }

    private static void taoBang(final Connection conn) throws Exception {
        if (daTaoBang) {
            return;
        }
        try (Statement st = conn.createStatement()) {
            st.execute(DDL);
        }
        daTaoBang = true;
    }

    private static void baoLoiMotLan(final Exception e) {
        if (daBaoLoi) {
            return;
        }
        daBaoLoi = true;
        System.out.println("GHI CLIENT: loi (chi bao mot lan) - " + e);
    }
}
