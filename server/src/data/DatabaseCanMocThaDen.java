package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Trung Thu 2026 - "can moc tha den": mot bo dem DUY NHAT, DUNG CHUNG TOAN SERVER, dem moi
 * lan THA DEN THANH CONG (khong tinh dot den mien phi). KHONG reset theo su kien bat/tat -
 * nguoi dung xac nhan 16/09/2026 bo dem nay chi cong don, khong phan biet lang/quoc gia.
 *
 * VI SAO BANG RIENG, KHONG DUNG cau_hinh: cau_hinh (server.CauHinh) danh cho gia tri ADMIN
 * TU CHINH va duoc napLai() moi 10s - dung no cho mot con so ma SERVER TU TANG o moi lan
 * tha den (co the hang tram lan/gio) se lam nham vai tro bang do (gia tri ADMIN dat) voi
 * bo dem runtime, va cau_hinh khong co san co che UPDATE ... SET x = x + 1 an toan.
 *
 * Hai bang, tu tao qua CREATE TABLE IF NOT EXISTS (giong DatabaseSuKien.taoBang()):
 *   - tob_canmoc_thadeon        : 1 dong duy nhat (id=1), giu tong so lan tha den.
 *   - tob_canmoc_thadeon_nhan   : moi dong la (idchar, moc) da nhan - chan nhan hai lan.
 */
public final class DatabaseCanMocThaDen {

    private static final String BANG_DEM = "tob_canmoc_thadeon";
    private static final String BANG_NHAN = "tob_canmoc_thadeon_nhan";
    private static final String BANG_CA_NHAN = "tob_canmoc_thadeon_canhan";

    /** Cac moc ca nhan co qua - dung chung cho ca kiem tra "da qua moc" lan menu nhan qua. */
    public static final int[] CAC_MOC = {300, 3000};

    /** Moc tong server kich hoat x2 exp toan server trong ngay (xem Char.isTrungThuX2ExpHomNay()). */
    public static final long MOC_TOAN_SERVER_X2_EXP = 10000L;

    private static volatile boolean daTaoBang;

    private static boolean taoBang() {
        if (daTaoBang) {
            return true;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + BANG_DEM + " ("
                            + "id TINYINT NOT NULL,"
                            + "tong_tha_den BIGINT NOT NULL DEFAULT 0,"
                            + "ngay_dat_10000 DATE NULL,"
                            + "PRIMARY KEY (id)"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            pre.executeUpdate();
            pre.close();
            pre = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + BANG_NHAN + " ("
                            + "idchar INT NOT NULL,"
                            + "moc INT NOT NULL,"
                            + "thoi_gian DATETIME NOT NULL,"
                            + "PRIMARY KEY (idchar, moc)"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            pre.executeUpdate();
            pre.close();
            pre = conn.prepareStatement(
                    "INSERT IGNORE INTO " + BANG_DEM + " (id, tong_tha_den) VALUES (1, 0)");
            pre.executeUpdate();
            pre.close();
            pre = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + BANG_CA_NHAN + " ("
                            + "idchar INT NOT NULL,"
                            + "so_lan INT NOT NULL DEFAULT 0,"
                            + "cap_nhat_luc DATETIME NOT NULL,"
                            + "PRIMARY KEY (idchar)"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            pre.executeUpdate();
            daTaoBang = true;
            return true;
        } catch (final Exception e) {
            System.out.println("[CANMOC_THADEN] khong tao duoc bang: " + e);
            return false;
        } finally {
            dong(null, pre, conn);
        }
    }

    /**
     * Goi moi khi MOT lan tha den THAT SU thanh cong (da tru xu/luong/nen). Tang bo dem
     * chung 1 don vi va tra ve gia tri MOI (dung de biet co vua vuot moc nao khong).
     */
    public static long tangVaLayTong() {
        if (!taoBang()) {
            return -1L;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("UPDATE " + BANG_DEM + " SET tong_tha_den = tong_tha_den + 1 WHERE id = 1");
            pre.executeUpdate();
            pre.close();
            pre = conn.prepareStatement("SELECT tong_tha_den FROM " + BANG_DEM + " WHERE id = 1");
            rs = pre.executeQuery();
            if (rs.next()) {
                final long tong = rs.getLong(1);
                danhDauDat10000NeuDu(conn, tong);
                return tong;
            }
            return -1L;
        } catch (final Exception e) {
            System.out.println("[CANMOC_THADEN] loi tang bo dem: " + e);
            return -1L;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /**
     * Tang so lan THA DEN THANH CONG cua RIENG mot nhan vat (moi 25/09/2026, theo yeu cau
     * "top tha den dang sai thu tu" - Bang xep hang cu dung tob_top_event, sap theo TONG
     * LUONG TIEU CA SU KIEN (dinh cac khoan doi pet/long den rong/... khong lien quan tha
     * den, va tha den TRA XU khong duoc tinh vi tob_top_event chi cong diem qua subLuong()).
     * Bang rieng nay dem DUNG so lan tha den, khong phan biet tra bang luong hay xu - goi tu
     * RealController.xuLyCanMocThaDen() cung luc voi bo dem toan server, cho ca hai duong tra phi.
     */
    public static void tangCaNhan(final int idchar) {
        if (!taoBang()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "INSERT INTO " + BANG_CA_NHAN + " (idchar, so_lan, cap_nhat_luc) VALUES (?, 1, NOW())"
                            + " ON DUPLICATE KEY UPDATE so_lan = so_lan + 1, cap_nhat_luc = NOW()");
            pre.setInt(1, idchar);
            pre.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[CANMOC_THADEN] loi tang ca nhan: " + e);
        } finally {
            dong(null, pre, conn);
        }
    }

    /**
     * So lan tha den cua RIENG mot nhan vat (moi 25/09/2026, theo yeu cau nguoi dung "chi
     * chinh nhan vat do moi biet duoc so diem") - dung o menu NPC Hang Nga, chi hien cho
     * chinh nguoi choi dang mo menu (xem RequestNPCInfoHandler case -49), khong lo cho ai
     * khac xem duoc so lieu nay.
     */
    public static int layCaNhan(final int idchar) {
        if (!taoBang()) {
            return 0;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT so_lan FROM " + BANG_CA_NHAN + " WHERE idchar = ?");
            pre.setInt(1, idchar);
            rs = pre.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (final Exception e) {
            System.out.println("[CANMOC_THADEN] loi lay so lan ca nhan: " + e);
            return 0;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /** Mot dong trong top ca nhan - ten hien tai (doc lai tu tob_char) + so lan tha den. */
    public static final class TopCaNhan {
        public final String charname;
        public final int soLan;

        public TopCaNhan(final String charname, final int soLan) {
            this.charname = charname;
            this.soLan = soLan;
        }
    }

    /**
     * Top nhan vat tha den nhieu nhat (dem THAT, khong phan biet tra luong/xu), sap giam dan.
     *
     * Sua 25/09/2026 (yeu cau nguoi dung, "ai tha truoc thi dung truoc khi bang so"): them tieu
     * chi phu cap_nhat_luc ASC. cap_nhat_luc duoc UPDATE = NOW() o MOI lan tang (tangCaNhan()),
     * nen no chinh la thoi diem nhan vat DAT so_lan hien tai (vi so_lan chi tang, khong bao gio
     * giam) - khong can them cot rieng de luu "luc dat moc X".
     */
    public static java.util.List<TopCaNhan> layTopCaNhan(final int gioiHan) {
        final java.util.List<TopCaNhan> ket = new java.util.ArrayList<>();
        if (!taoBang()) {
            return ket;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "SELECT c.charname, t.so_lan FROM " + BANG_CA_NHAN + " t"
                            + " JOIN tob_char c ON c.id = t.idchar"
                            + " ORDER BY t.so_lan DESC, t.cap_nhat_luc ASC LIMIT ?");
            pre.setInt(1, gioiHan);
            rs = pre.executeQuery();
            while (rs.next()) {
                ket.add(new TopCaNhan(rs.getString(1), rs.getInt(2)));
            }
        } catch (final Exception e) {
            System.out.println("[CANMOC_THADEN] loi lay top ca nhan: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return ket;
    }

    public static long layTong() {
        if (!taoBang()) {
            return 0L;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT tong_tha_den FROM " + BANG_DEM + " WHERE id = 1");
            rs = pre.executeQuery();
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (final Exception e) {
            System.out.println("[CANMOC_THADEN] loi doc bo dem: " + e);
            return 0L;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /**
     * Khi tong vua cham moc 10.000 LAN DAU TIEN, ghi lai NGAY HOM DO (ngay_dat_10000) de
     * Char.isTrungThuX2ExpHomNay() biet "hom nay" co phai dung ngay dat moc khong - moc nay
     * chi ap dung x2 exp toan server DUY NHAT trong ngay dat duoc, khong phai moi ngay sau do.
     */
    private static void danhDauDat10000NeuDu(final Connection conn, final long tongMoi) {
        if (tongMoi < MOC_TOAN_SERVER_X2_EXP) {
            return;
        }
        PreparedStatement pre = null;
        try {
            pre = conn.prepareStatement(
                    "UPDATE " + BANG_DEM + " SET ngay_dat_10000 = CURDATE() WHERE id = 1 AND ngay_dat_10000 IS NULL");
            pre.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[CANMOC_THADEN] loi ghi ngay dat 10000: " + e);
        } finally {
            dong(null, pre, null);
        }
    }

    private static volatile boolean capNhatHomNayLaNgayDat10000 = false;
    private static volatile long lanCapNhatCuoiHomNayLaNgayDat10000 = 0L;

    /**
     * True neu HOM NAY dung la ngay server vua cham moc 10.000 (chi dung 1 ngay duy nhat).
     * Sua 19/09/2026: ham nay duoc goi tu Monster.apDungGiamExpTheoCapNV() - TREN MOI LAN
     * cong exp giet quai cua MOI nguoi choi (rat thuong xuyen) - ban cu truy DB THAT moi
     * lan goi se lam qua tai DB duoi tai nang. Them cache bo nho, lam moi toi da 1 phut/lan.
     */
    public static boolean homNayLaNgayDat10000() {
        final long bayGio = System.currentTimeMillis();
        if (bayGio - lanCapNhatCuoiHomNayLaNgayDat10000 < 60000L) {
            return capNhatHomNayLaNgayDat10000;
        }
        if (!taoBang()) {
            return capNhatHomNayLaNgayDat10000;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "SELECT 1 FROM " + BANG_DEM + " WHERE id = 1 AND ngay_dat_10000 = CURDATE()");
            rs = pre.executeQuery();
            capNhatHomNayLaNgayDat10000 = rs.next();
            lanCapNhatCuoiHomNayLaNgayDat10000 = bayGio;
            return capNhatHomNayLaNgayDat10000;
        } catch (final Exception e) {
            System.out.println("[CANMOC_THADEN] loi kiem tra ngay dat 10000: " + e);
            return capNhatHomNayLaNgayDat10000;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /** Nhan vat nay da nhan qua o moc nay chua (chan nhan hai lan, vinh vien). */
    public static boolean daNhan(final int idchar, final int moc) {
        if (!taoBang()) {
            return true; // an toan: khong doc duoc DB thi coi nhu da nhan, tranh phat nham
        }
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT 1 FROM " + BANG_NHAN + " WHERE idchar = ? AND moc = ?");
            pre.setInt(1, idchar);
            pre.setInt(2, moc);
            rs = pre.executeQuery();
            return rs.next();
        } catch (final Exception e) {
            System.out.println("[CANMOC_THADEN] loi kiem tra da nhan: " + e);
            return true;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /** Ghi nhan da phat qua moc nay cho nhan vat - goi NGAY SAU KHI da trao qua thanh cong. */
    public static void danhDauDaNhan(final int idchar, final int moc) {
        if (!taoBang()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "INSERT IGNORE INTO " + BANG_NHAN + " (idchar, moc, thoi_gian) VALUES (?, ?, NOW())");
            pre.setInt(1, idchar);
            pre.setInt(2, moc);
            pre.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[CANMOC_THADEN] loi ghi da nhan: " + e);
        } finally {
            dong(null, pre, conn);
        }
    }

    private static void dong(final ResultSet rs, final PreparedStatement pre, final Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (final Exception ignored) {
        }
        try {
            if (pre != null) {
                pre.close();
            }
        } catch (final Exception ignored) {
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (final Exception ignored) {
        }
    }

    private DatabaseCanMocThaDen() {
    }
}
