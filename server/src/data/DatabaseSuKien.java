package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import real.SuKien;

/**
 * Bang `tob_su_kien` - cong bat/tat cua tung su kien.
 *
 * CHIEU GHI CHIA DOI RO RANG, de hai ben khong dam len nhau:
 *
 *   Trang quan tri (web/admin/sukien.php) ghi:  bat, sua_boi, sua_luc, mo_luc, dong_luc
 *   May chu game ghi:                           ten, nhom, mo_ta, dang_bat, may_chu_luc
 *
 * ghiAnhChup() KHONG BAO GIO ghi vao cot `bat`. Nho vay khoi dong lai may chu khong lam
 * mat thiet lap ai vua dat, va trang web khong can biet trong game co nhung su kien nao -
 * danh sach do do may chu tu dang len moi lan khoi dong. Them mot su kien moi vao
 * real/SuKien la no tu hien tren trang quan tri, khong phai sua PHP.
 *
 * NGOAI LE DUY NHAT: chayLichDenHan() co ghi `bat`, vi do chinh la viec cua lich hen -
 * toi gio thi bat/tat thay nguoi. Khi lam vay no ghi luon sua_boi/sua_luc y het nhu trang
 * web, nen moi thu phia sau (dem so dong cho khoi dong lai, nhan "cho khoi dong lai" tren
 * web) van chay dung ma khong phai biet thay doi den tu dau.
 *
 * `may_chu_luc` so voi `sua_luc` la cach biet mot thiet lap da co hieu luc chua: sua sau
 * lan khoi dong gan nhat = chua vao game. Ca trang web lan real/LichSuKien deu dung dung
 * mot phep so sanh nay, dung tach ra thanh hai luat khac nhau.
 */
public final class DatabaseSuKien {

    public static final String BANG = "tob_su_kien";

    private DatabaseSuKien() {
    }

    // ============================ TAO BANG ============================

    /**
     * Tao bang neu chua co. Tra ve false khi khong tao duoc - luc do may chu van chay
     * binh thuong voi gia tri mac dinh, chi la khong bat/tat duoc tu web.
     */
    /**
     * Da tao bang xong roi thi thoi.
     *
     * Can cai chot nay vi chayLichDenHan() goi taoBang() va no chay 15 giay mot lan trong
     * suot doi may chu; khong co chot thi ba cau CREATE/SHOW COLUMNS lap lai mai mai chi
     * de biet mot dieu khong bao gio doi.
     */
    private static volatile boolean daTaoBang;

    public static boolean taoBang() {
        if (daTaoBang) {
            return true;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + BANG + " ("
                            + "ma VARCHAR(40) NOT NULL,"
                            + "bat TINYINT NOT NULL DEFAULT 0,"
                            + "ten VARCHAR(100) NULL,"
                            + "nhom VARCHAR(40) NULL,"
                            + "mo_ta VARCHAR(500) NULL,"
                            + "dang_bat TINYINT NOT NULL DEFAULT 0,"
                            + "sua_boi VARCHAR(50) NULL,"
                            + "sua_luc DATETIME NULL,"
                            + "may_chu_luc DATETIME NULL,"
                            + "mo_luc DATETIME NULL,"
                            + "dong_luc DATETIME NULL,"
                            + "PRIMARY KEY (ma)"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            pre.executeUpdate();
            themCotThieu(conn);
            daTaoBang = true;
            return true;
        } catch (final Exception e) {
            System.out.println("[SUKIEN] khong tao duoc bang " + BANG + ": " + e);
            return false;
        } finally {
            dong(null, pre, conn);
        }
    }

    /**
     * Hai cot lich (`mo_luc`, `dong_luc`) them vao ngay 30/08/2026, SAU khi nhieu may chu
     * da co bang nay roi.
     *
     * CREATE TABLE IF NOT EXISTS o tren khong giup gi cho nhung bang da ton tai: bang cu
     * van thieu hai cot, moi cau SELECT cua trang lich se nem loi "Unknown column" va
     * trang su kien hong hoan toan. Nen phai ALTER rieng. Kiem tra bang SHOW COLUMNS chu
     * khong dung "ADD COLUMN IF NOT EXISTS" vi cu phap do MySQL 5.x khong co (chi MariaDB
     * moi co), ma khong biet may chu nao chay ban nao.
     */
    private static void themCotThieu(final Connection conn) {
        for (final String[] cot : new String[][]{
                {"mo_luc", "DATETIME NULL"},
                {"dong_luc", "DATETIME NULL"}}) {
            PreparedStatement pre = null;
            ResultSet rs = null;
            try {
                pre = conn.prepareStatement("SHOW COLUMNS FROM " + BANG + " LIKE ?");
                pre.setString(1, cot[0]);
                rs = pre.executeQuery();
                final boolean daCo = rs.next();
                rs.close();
                rs = null;
                pre.close();
                pre = null;
                if (!daCo) {
                    pre = conn.prepareStatement(
                            "ALTER TABLE " + BANG + " ADD COLUMN " + cot[0] + " " + cot[1]);
                    pre.executeUpdate();
                    System.out.println("[SUKIEN] da them cot " + cot[0] + " vao " + BANG);
                }
            } catch (final Exception e) {
                System.out.println("[SUKIEN] khong them duoc cot " + cot[0] + ": " + e);
            } finally {
                dong(rs, pre, null);
            }
        }
    }

    // ============================ DOC ============================

    /**
     * Doc cot `bat` cua moi dong roi nap vao SuKien.
     *
     * Dong chua co trong bang thi khong nap gi ca - SuKien.macDinh() lo phan do. Nho
     * vay them su kien moi vao ma nguon khong can migrate CSDL truoc.
     */
    public static void nap() {
        if (!taoBang()) {
            return;
        }
        final Map<String, Boolean> ra = new HashMap<String, Boolean>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT ma, bat FROM " + BANG);
            rs = pre.executeQuery();
            while (rs.next()) {
                ra.put(rs.getString("ma"), Boolean.valueOf(rs.getInt("bat") == 1));
            }
            SuKien.dat(ra);
        } catch (final Exception e) {
            System.out.println("[SUKIEN] khong doc duoc bang " + BANG + ": " + e);
        } finally {
            dong(rs, pre, conn);
        }
    }

    // ============================ GHI ANH CHUP ============================

    /**
     * Dang danh muc su kien va trang thai THUC TE luc nay len bang, de trang quan tri
     * co cai ma hien.
     *
     * Dong da co: chi cap nhat phan may chu so huu, GIU NGUYEN cot `bat` cua nguoi dat.
     * Dong chua co: them moi voi bat = trang thai mac dinh dang dung, nen lan dau vao
     * trang quan tri thay dung nhung gi game dang chay chu khong phai mot bang toan tat.
     */
    public static void ghiAnhChup() {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(
                    "INSERT INTO " + BANG
                            + " (ma, bat, ten, nhom, mo_ta, dang_bat, may_chu_luc)"
                            + " VALUES (?,?,?,?,?,?,NOW())"
                            + " ON DUPLICATE KEY UPDATE"
                            + "   ten = VALUES(ten), nhom = VALUES(nhom), mo_ta = VALUES(mo_ta),"
                            + "   dang_bat = VALUES(dang_bat), may_chu_luc = NOW()");
            for (final SuKien.Muc m : SuKien.danhMuc()) {
                final boolean dangBat = SuKien.bat(m.ma);
                pre.setString(1, m.ma);
                pre.setInt(2, dangBat ? 1 : 0);
                pre.setString(3, m.ten);
                pre.setString(4, m.nhom);
                pre.setString(5, m.moTa);
                pre.setInt(6, dangBat ? 1 : 0);
                pre.addBatch();
            }
            pre.executeBatch();
        } catch (final Exception e) {
            System.out.println("[SUKIEN] khong ghi duoc anh chup: " + e);
        } finally {
            dong(null, pre, conn);
        }
    }

    // ============================ LICH HEN ============================

    /** Ai dat gia tri `bat` khi lich hen chay. Giu ASCII cho khoi phu thuoc bang ma ket noi. */
    public static final String NGUOI_DAT_LICH = "lich hen";

    /**
     * Chay moi lich hen da toi gio: `mo_luc` <= bay gio thi bat, `dong_luc` <= bay gio thi tat.
     * Moc nao da chay xong thi bi XOA, nen moi moc chi chay dung mot lan.
     *
     * SO SANH THOI GIAN BANG NOW() CUA CSDL chu khong phai dong ho cua JVM: hai cai co the
     * lech mui gio (JVM chay voi TZ khac MySQL) va luc do lich se chay som/muon vai tieng ma
     * khong ai hieu tai sao. Trang web cung ghi moc bang gio CSDL, nen ca hai dau cung mot dong ho.
     *
     * MO CHAY TRUOC, DONG CHAY SAU. Neu ca hai moc cung qua han thi ket qua la TAT - dung
     * cho truong hop binh thuong (dong_luc sau mo_luc, may chu tat may ngay roi bat lai giua
     * chung). Truong hop dong_luc <= mo_luc la vo nghia va bi trang web chan tu luc nhap.
     *
     * @return so dong that su DOI gia tri `bat` (khong tinh dong chi bi xoa moc).
     */
    public static int chayLichDenHan() {
        if (!taoBang()) {
            return 0;
        }
        return chayMotVe("mo_luc", 1) + chayMotVe("dong_luc", 0);
    }

    private static int chayMotVe(final String cot, final int giaTri) {
        Connection conn = null;
        PreparedStatement pre = null;
        int doi = 0;
        try {
            conn = Database.instance.getConnection();
            // Buoc 1: dong nao dang khac gia tri can dat -> doi that, co dau vet nguoi sua.
            pre = conn.prepareStatement(
                    "UPDATE " + BANG + " SET bat = ?, sua_boi = ?, sua_luc = NOW(), " + cot + " = NULL"
                            + " WHERE " + cot + " IS NOT NULL AND " + cot + " <= NOW() AND bat <> ?");
            pre.setInt(1, giaTri);
            pre.setString(2, NGUOI_DAT_LICH);
            pre.setInt(3, giaTri);
            doi = pre.executeUpdate();
            pre.close();
            pre = null;

            // Buoc 2: dong da o dung trang thai roi -> chi xoa moc, KHONG dong vao sua_luc.
            // Neu gop hai buoc lam mot thi moi lich hen "bat" cua su kien dang bat san se
            // day sua_luc len moi hon may_chu_luc, va may chu se tuong co thay doi cho khoi
            // dong lai -> khoi dong lai vo co.
            pre = conn.prepareStatement(
                    "UPDATE " + BANG + " SET " + cot + " = NULL"
                            + " WHERE " + cot + " IS NOT NULL AND " + cot + " <= NOW()");
            pre.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[SUKIEN] khong chay duoc lich " + cot + ": " + e);
        } finally {
            dong(null, pre, conn);
        }
        return doi;
    }

    /**
     * Dem so su kien dang CHO KHOI DONG LAI: thiet lap khac trang thai may chu dang chay,
     * va thay doi do xay ra SAU lan khoi dong gan nhat.
     *
     * Ve `sua_luc > may_chu_luc`: day khong phai lam mau. Sau moi lan khoi dong dang hoang,
     * ghiAnhChup() dat dang_bat = bat nen phep tru nay luon bang 0. Con neu ghiAnhChup()
     * that bai (CSDL loi giua chung), dang_bat se ket lai o gia tri cu va `bat <> dang_bat`
     * thanh dung MAI MAI - khong co dieu kien thoi gian nay thi may chu se tu khoi dong lai
     * vo han, va sau 5 lan trong 300 giay thi systemd bo cuoc, server nam luon.
     */
    public static int soDongChoKhoiDongLai() {
        return dem("bat <> dang_bat AND sua_luc IS NOT NULL"
                + " AND (may_chu_luc IS NULL OR sua_luc > may_chu_luc)");
    }

    /**
     * Dem so dong LECH MA KHONG PHAI DO AI SUA - dau hieu anh chup luc khoi dong bi hong.
     * Chi de ghi log canh bao; tuyet doi khong lay so nay lam co khoi dong lai (xem tren).
     */
    public static int soDongAnhChupHong() {
        return dem("bat <> dang_bat AND (sua_luc IS NULL"
                + " OR (may_chu_luc IS NOT NULL AND sua_luc <= may_chu_luc))");
    }

    private static int dem(final String dieuKien) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT COUNT(*) FROM " + BANG + " WHERE " + dieuKien);
            rs = pre.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (final Exception e) {
            System.out.println("[SUKIEN] khong dem duoc dong cho khoi dong lai: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return 0;
    }

    // ==================== CO KHOI DONG LAI (5h_systems) ====================

    /**
     * Bat co khoi dong lai bang cach dat 5h_systems.status = 0 o dong command='restart'.
     *
     * KHONG tu tat may o day. Duong tat da co san va da duoc dung ky: BaoTriDaily doc co
     * nay moi 5 giay roi chay ca chuoi luu du lieu (clan, nhan vat, cho ban, vantieu, log,
     * top lien tram) truoc khi System.exit(0). Viet mot duong tat thu hai la co hai luong
     * cung luu mot luc - hong du lieu kieu kho lan nhat.
     *
     * Bat lai may la viec cua systemd: kpah-game.service dat Restart=always, RestartSec=10.
     * Tren may Windows thi BaoTriDaily tu mo lai run.bat.
     *
     * Database.baotriCMD() se dat status ve 1 ngay khi doc duoc, nen co nay tu tat.
     */
    public static boolean yeuCauKhoiDongLai() {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("UPDATE 5h_systems SET status = 0 WHERE command = 'restart'");
            final int n = pre.executeUpdate();
            pre.close();
            pre = null;
            if (n == 0) {
                // Bang 5h_systems co san tu ban dump goc nen day gan nhu khong bao gio chay.
                // Van lam vi thieu dong nay thi co khong bao gio bat duoc, ma khong ai thay loi.
                pre = conn.prepareStatement(
                        "INSERT INTO 5h_systems (command, status) VALUES ('restart', 0)");
                pre.executeUpdate();
                System.out.println("[SUKIEN] 5h_systems chua co dong 'restart' - da them.");
            }
            return true;
        } catch (final Exception e) {
            System.out.println("[SUKIEN] khong bat duoc co khoi dong lai: " + e);
            return false;
        } finally {
            dong(null, pre, conn);
        }
    }

    // ============================ TIEN ICH ============================

    private static void dong(final ResultSet rs, final PreparedStatement pre, final Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (final Exception ignored) {
            }
        }
        if (pre != null) {
            try {
                pre.close();
            } catch (final Exception ignored) {
            }
        }
        if (conn != null) {
            try {
                Database.connPool.free(conn);
            } catch (final Exception ignored) {
            }
        }
    }
}
