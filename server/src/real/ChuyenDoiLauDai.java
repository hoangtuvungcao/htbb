package real;

import com.htbb.pokemon.CapDoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.SuKienLauDai;
import data.Database;
import data.DatabaseLauDai;
import java.util.List;
import java.util.Map.Entry;

/**
 * Bat / tat sv.lau_dai TU DONG, khong buoc tay (yeu cau 18/10/2026). Goi MOT lan luc khoi dong o
 * CA HAI trang thai co (TeamServer), sau khi CSDL san sang, truoc khi nhan nguoi choi.
 *
 *  1. Ngay khai truong: server.ini sv.ngay_khai_truong hop le thi dung; khong thi lay
 *     cau_hinh lau_dai.ngay_khai_truong; chua co thi ghi HOM NAY vao do (lan bat dau tien).
 *  2. Thang EXP cua tob_char.xp (cau_hinh lau_dai.thang_exp = moi | cu). Co bat ma dang "cu" thi
 *     doi sang bang moi; co tat ma dang "moi" thi doi NGUOC ve bang cu - tu GIA TRI HIEN TAI (giu cap,
 *     giu % trong cap), khong khoi phuc ban cu da cu. Moi lan doi co so lan (lau_dai.lan_chuyen) va
 *     nhat ky tob_char_xp_chuyen(lan, id, huong, xp_truoc, xp_sau, lv): ghi nhat ky TRUOC, roi
 *     UPDATE ... WHERE xp = xp_truoc (co dieu kien). Ngat giua chung: lan khoi dong sau doc lai
 *     lau_dai.dang_chuyen va lam tiep dung lan do, dong da ghi thi chi ap lai (khong doi 2 lan).
 *  3. Tat co: go lich su kien do lich 52 tuan hen (chi dong trung dung moc da hen). Nhiem vu chinh
 *     tra ve vi tri cu khi nap nhan vat (NhiemVuLauDaiGame.khiNapTatCo).
 * tob_char la MyISAM (khong co giao dich): moi dong la mot UPDATE nguyen tu co dieu kien.
 */
public final class ChuyenDoiLauDai {

    public static final String K_THANG = "lau_dai.thang_exp";
    public static final String K_DANG = "lau_dai.dang_chuyen";
    public static final String K_LAN = "lau_dai.lan_chuyen";
    public static final String K_NGAY = "lau_dai.ngay_khai_truong";
    public static final String MOI = "moi";
    public static final String CU = "cu";

    private ChuyenDoiLauDai() {
    }

    public static void khiKhoiDong() {
        try {
            DatabaseLauDai.taoBangChuyenDoi();
            if (LauDai.bat()) {
                ngayKhaiTruong();
            }
            chuyenExp();
            if (!LauDai.bat()) {
                goLichSuKien();
                NhiemVuLauDaiGame.coHoSoCu = DatabaseLauDai.coHoSo();
            }
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI chuyen doi luc khoi dong: " + e);
        }
    }

    static void ngayKhaiTruong() {
        if (LauDai.ngayKhaiTruongMs() > 0L) {
            System.out.println("[LAU DAI] ngay khai truong (server.ini): " + ngay(LauDai.ngayKhaiTruongMs()));
            return;
        }
        String s = DatabaseLauDai.docKhoa(K_NGAY);
        if (s == null || LauDai.docNgay(s) <= 0L) {
            s = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            DatabaseLauDai.ghiKhoa(K_NGAY, s);
            System.out.println("[LAU DAI] chua co ngay khai truong -> ghi hom nay " + s + " vao cau_hinh " + K_NGAY);
        } else {
            System.out.println("[LAU DAI] ngay khai truong (cau_hinh): " + s);
        }
        LauDai.datNgayKhaiTruong(s);
    }

    static String ngay(final long ms) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(ms));
    }

    /** Thang EXP hien tai cua CSDL. Ban cu (truoc 18/10) chi co dong moc tob_char_xp_cu id 0. */
    static String thangHienTai() {
        final String k = DatabaseLauDai.docKhoa(K_THANG);
        if (MOI.equals(k) || CU.equals(k)) {
            return k;
        }
        return DatabaseLauDai.daChuyenExpXong() == 1 ? MOI : CU;
    }

    static void chuyenExp() {
        final String dang = DatabaseLauDai.docKhoa(K_DANG);
        final String dich = LauDai.bat() ? MOI : CU;
        int lan;
        String huong;
        if (dang != null && dang.indexOf(':') > 0) {
            lan = Integer.parseInt(dang.substring(0, dang.indexOf(':')));
            huong = dang.substring(dang.indexOf(':') + 1);
            System.out.println("[LAU DAI] lam tiep lan chuyen EXP " + lan + " (" + huong + ") bi ngat truoc do");
        } else {
            final String hien = thangHienTai();
            if (hien.equals(dich)) {
                System.out.println("[LAU DAI] EXP nhan vat dang o bang " + hien + " - khong can chuyen");
                if (DatabaseLauDai.docKhoa(K_THANG) == null) {
                    DatabaseLauDai.ghiKhoa(K_THANG, hien);
                }
                return;
            }
            final String l = DatabaseLauDai.docKhoa(K_LAN);
            lan = (l == null || l.isEmpty() ? 0 : Integer.parseInt(l.trim())) + 1;
            huong = dich;
            DatabaseLauDai.ghiKhoa(K_DANG, lan + ":" + huong);
        }
        // Bang moi 1..100 (cap > 91 chi co khi da qua ngay 365). Doi NGUOC ve bang cu: cap > 91 ve 91
        // (bang cu dung o 91) - xem KE_HOACH_LAU_DAI_GD2.md "Bat / tat co".
        final long[] mainMoi = CapDoLauDai.expMain(CapDoLauDai.CAP_BANG);
        final long[] reqMoi = CapDoLauDai.expRequire(CapDoLauDai.CAP_BANG);
        final long[] mainCu = LevelDetail.expMainCu != null ? LevelDetail.expMainCu : LevelDetail.expMain;
        final long[] reqCu = LevelDetail.expRequireCu != null ? LevelDetail.expRequireCu : LevelDetail.expRequire;
        final boolean sangMoi = MOI.equals(huong);
        final java.util.Map<Long, long[]> daGhi = DatabaseLauDai.napNhatKyChuyen(lan);
        final List<long[]> ds = DatabaseLauDai.napXpTatCa();
        if (ds == null || daGhi == null) {
            System.out.println("[LAU DAI] khong doc duoc tob_char / nhat ky - KHONG chuyen, giu lau_dai.dang_chuyen de lam tiep");
            return;
        }
        int n = 0;
        for (final long[] d : ds) {
            final long id = d[0];
            long[] g = daGhi.get(Long.valueOf(id));
            if (g == null) {
                final long sau = sangMoi ? CapDoLauDai.chuyenExp(d[1], mainCu, reqCu, mainMoi, reqMoi)
                        : CapDoLauDai.chuyenExp(d[1], mainMoi, reqMoi, mainCu, reqCu);
                if (!DatabaseLauDai.ghiNhatKyChuyen(lan, id, huong, d[1], sau, d[2])) {
                    continue; // loi CSDL: bo qua dong nay, lan sau lam tiep
                }
                g = new long[]{d[1], sau};
            }
            if (DatabaseLauDai.apChuyen(id, g[0], g[1])) {
                n++;
            }
        }
        DatabaseLauDai.ghiKhoa(K_THANG, huong);
        DatabaseLauDai.ghiKhoa(K_LAN, String.valueOf(lan));
        DatabaseLauDai.ghiKhoa(K_DANG, "");
        System.out.println("[LAU DAI] chuyen EXP lan " + lan + " sang bang " + huong + ": " + n + "/" + ds.size()
                + " nhan vat (nhat ky tob_char_xp_chuyen)");
        try {
            Database.instance.saveOrtherLog("", "server", "chuyen exp lau dai lan " + lan + " -> " + huong + ": " + n, "laudai");
        } catch (final Exception ignored) {
        }
    }

    /** Tat co: bo cac moc lich 52 tuan da hen (chi dong co moc trung khop dung lan da hen). */
    static void goLichSuKien() {
        int n = 0;
        for (final Entry<String, long[]> e : SuKienLauDai.lanKeTiep(System.currentTimeMillis()).entrySet()) {
            if (DatabaseLauDai.goHenSuKien(e.getKey(), e.getValue()[0], e.getValue()[1])) {
                n++;
            }
        }
        if (n > 0) {
            System.out.println("[LAU DAI] tat co: go " + n + " lich su kien do lich 52 tuan hen");
        }
    }
}
