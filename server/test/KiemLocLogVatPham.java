import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Bo loc allow-list cho tob_log_all_item (data/LocLogVatPham): chi loai trong GIU moi ghi.
 *
 * VI SAO CAN. Ngay 02/09/2026 bang co 5,79 trieu dong, hon 5,6 trieu la rac. Bo loc chi la
 * mot Set va mot cau if, nhung co bon cach hong am tham ma compiler khong bat:
 *   1. Viet lop loc roi QUEN goi o mot trong bay ham ghi cua Database (da xay ra voi
 *      saveLogMatVatPham, xem KiemLogMatVatPham) - log van ghi nhu cu, khong ai biet.
 *   2. Ai do doi chuoi aclog trong ma nguon (vd "matdo" -> "mat_do") - bo loc van compile,
 *      nhung loai QUAN TRONG do lap tuc bi bo qua vi khong con khop GIU.
 *   3. vps/don-log.sh chep lai danh sach de xoa dong cu tren production; hai ban lech nhau la
 *      loai dang giu bi xoa mat, hoac rac khong bao gio duoc don.
 *   4. Trang chan doan web (HTBB-Web server/utils/chanDoan.ts; truoc la web/admin/chandoan.php
 *      cua website PHP da bo) dem mot so loai loi/gian lan; loai nao o do ma khong nam trong
 *      GIU thi trang chan doan mu, luon bao "0 loi".
 */
public final class KiemLocLogVatPham {

    private static final String F_DATABASE = "src/data/Database.java";
    private static final String F_DON_LOG = "../vps/don-log.sh";
    /** HTBB-Web (thay web/admin/chandoan.php da bo - docs/kien-truc.md). */
    private static final String F_CHAN_DOAN = "server/utils/chanDoan.ts";

    public static void chay() {
        Kiem.nhom("LocLogVatPham - chi ghi loai trong danh sach GIU vao tob_log_all_item");

        // Bat buoc giu: mat do, gian lan, tai san tang bat thuong.
        for (final String s : new String[]{"matdo", "trade", "sitemu", "delI", "timeout", "XOAPET",
                "loiinvent", "hackc", "hackR", "hackbitem", "hackchonchar", "ffbug", "hklbug", "hcm",
                "naptien", "nhanxu", "logxu", "autosave", "lenlv", "lenlevel", "putGiftCode",
                "caitaocaitrang", "hoahinhpet", "truxu", "truthieu"}) {
            Kiem.sai("van ghi '" + s + "'", data.LocLogVatPham.boQua(s));
        }
        // Rac da tat - nhung loai tung chiem hang trieu dong.
        for (final String s : new String[]{"luckGOLD", "luckSILV", "joinLucky", "hop", "vulan",
                "gvang", "gbac", "sitems", "bossdie", "delSP", "nhansam", "expdon", "petan",
                "fruit", "hocskill", "delP", "themtt", "newatt", "tao", "die"}) {
            Kiem.dung("bo qua '" + s + "'", data.LocLogVatPham.boQua(s));
        }
        Kiem.dung("null bi bo qua va khong nem loi", data.LocLogVatPham.boQua(null));
        Kiem.dung("chuoi rong bi bo qua", data.LocLogVatPham.boQua(""));
        Kiem.dung("loai moi chua khai bao MAC DINH bi bo qua (allow-list)",
                data.LocLogVatPham.boQua("loai_moi_chua_ai_khai_bao"));
        Kiem.dung("phan biet hoa/thuong: 'MATDO' khong phai 'matdo'", data.LocLogVatPham.boQua("MATDO"));

        kiemDatabaseCoGoi();
        kiemChuoiConTonTai();
        kiemKhopDonLog();
        kiemKhopChanDoan();
    }

    /** (1) Ca bay ham ghi trong Database phai hoi bo loc, va hoi TRUOC khi day INSERT vao hang doi. */
    private static void kiemDatabaseCoGoi() {
        final String db = doc(Paths.get(F_DATABASE));
        Kiem.dung("doc duoc " + F_DATABASE, !db.isEmpty());
        if (db.isEmpty()) {
            return;
        }
        final String[][] ham = {
            {"public void saveOrtherLog(",       "LocLogVatPham.boQua(action)"},
            {"public void saveLogItemSellShop(", "LocLogVatPham.boQua(\"sitems\")"},
            {"public void saveLogLearnSkill(",   "LocLogVatPham.boQua(\"hocskill\")"},
            {"public void saveLogNapMoney(",     "LocLogVatPham.boQua(\"naptien\")"},
            {"public void saveLogTradeItem(",    "LocLogVatPham.boQua(\"trade\")"},
            {"public void saveLogSellItem(",     "LocLogVatPham.boQua(\"sitemu\")"},
            {"public void saveLogMatVatPham(",   "LocLogVatPham.boQua(\"matdo\")"},
        };
        for (final String[] h : ham) {
            final String than = thanHam(db, h[0]);
            Kiem.dung(h[0] + " co mat", !than.isEmpty());
            Kiem.dung(h[0] + " hoi " + h[1], than.contains(h[1]));
            Kiem.dung(h[0] + " hoi bo loc TRUOC Logdata.query.add",
                    than.indexOf("LocLogVatPham.boQua") >= 0
                            && than.indexOf("LocLogVatPham.boQua") < than.indexOf("Logdata.query.add"));
        }
        // Khong con cau INSERT nao vao bang ma nam ngoai bay ham tren.
        Kiem.bang("Database co dung 7 cau insert vao tob_log_all_item (moi cau mot ham da gac)",
                Integer.valueOf(7), Integer.valueOf(dem(db, "insert into tob_log_all_item")));
        final String saveOrther = thanHam(db, "public void saveOrtherLog(");
        Kiem.dung("saveOrtherLog: bo loc dung TRUOC UserLogger.addLog (tat la tat ca hai noi)",
                saveOrther.indexOf("LocLogVatPham.boQua") < saveOrther.indexOf("UserLogger.addLog"));
    }

    /** (2) Moi ten trong GIU phai con la aclog that trong src/ - doi ten la loai do mat log. */
    private static void kiemChuoiConTonTai() {
        final String toanBo = docToanBoSrc();
        Kiem.dung("doc duoc ma nguon src/ (tren 1 MB)", toanBo.length() > 1000000);
        if (toanBo.isEmpty()) {
            return;
        }
        for (final String s : new TreeSet<String>(data.LocLogVatPham.GIU)) {
            // Loai ghi qua saveOrtherLog la "abc"; loai ghi bang cau SQL chuyen biet la 'abc'.
            Kiem.dung("'" + s + "' con la aclog trong ma nguon",
                    toanBo.contains("\"" + s + "\"") || toanBo.contains("'" + s + "'"));
        }
    }

    /** (3) vps/don-log.sh chep lai danh sach (bien LOAI_GIU) - phai khop tung phan tu. */
    private static void kiemKhopDonLog() {
        final String sh = doc(Paths.get(F_DON_LOG));
        Kiem.dung("doc duoc " + F_DON_LOG, !sh.isEmpty());
        final Matcher m = Pattern.compile("^LOAI_GIU=\"([^\"]*)\"", Pattern.MULTILINE).matcher(sh);
        final boolean co = m.find();
        Kiem.dung("don-log.sh co bien LOAI_GIU", co);
        if (!co) {
            return;
        }
        final Set<String> trongSh = new TreeSet<String>();
        for (final String s : m.group(1).trim().split(" +")) {
            if (!s.isEmpty()) {
                trongSh.add(s);
            }
        }
        Kiem.bang("LOAI_GIU trong don-log.sh khop y GIU trong Java",
                new TreeSet<String>(data.LocLogVatPham.GIU).toString(), trongSh.toString());
        Kiem.dung("don-log.sh xoa dong NGOAI danh sach giu, theo lo (LIMIT) vi MyISAM khoa ca bang",
                sh.contains("WHERE aclog NOT IN ($DS) LIMIT $LO"));
    }

    /** (4) Moi loai ma trang chan doan web dem deu phai nam trong GIU, khong thi trang do mu. */
    private static void kiemKhopChanDoan() {
        if (WebHtbb.boQua("trang chan doan HTBB-Web")) {
            return;
        }
        final String php = WebHtbb.doc(F_CHAN_DOAN);
        Kiem.dung("doc duoc " + WebHtbb.ten(F_CHAN_DOAN), !php.isEmpty());
        final int bd = php.indexOf("CHAN_DOAN_LOAI_LOI: Readonly<Record<string, string>> = Object.freeze({");
        Kiem.dung("chanDoan.ts co bang CHAN_DOAN_LOAI_LOI", bd >= 0);
        if (bd < 0) {
            return;
        }
        final int kt = php.indexOf("\n})", bd);
        final String khoi = php.substring(bd, kt < 0 ? php.length() : kt);
        final Matcher m = Pattern.compile("(?m)^\\s*([A-Za-z0-9_]+):\\s*'").matcher(khoi);
        int so = 0;
        while (m.find()) {
            so++;
            Kiem.dung("chanDoan.ts dem '" + m.group(1) + "' -> phai nam trong GIU",
                    data.LocLogVatPham.GIU.contains(m.group(1)));
        }
        Kiem.dung("doc duoc danh sach loai loi cua chanDoan.ts (tren 5 loai)", so > 5);
        // Cau SQL ghi cung loai (vd "WHERE aclog = 'loiinvent'") cung phai nam trong GIU.
        final Matcher m2 = Pattern.compile("aclog\\s*=\\s*'([A-Za-z0-9_]+)'").matcher(php);
        while (m2.find()) {
            Kiem.dung("chanDoan.ts truy van aclog = '" + m2.group(1) + "' -> phai nam trong GIU",
                    data.LocLogVatPham.GIU.contains(m2.group(1)));
        }
    }

    /** Lay than mot method: tu chu ky toi dau dong ket thuc "    }" cung cap. */
    private static String thanHam(final String ma, final String chuKy) {
        final int bd = ma.indexOf(chuKy);
        if (bd < 0) {
            return "";
        }
        final int kt = ma.indexOf("\n    }\n", bd);
        return kt < 0 ? ma.substring(bd) : ma.substring(bd, kt);
    }

    private static int dem(final String s, final String mau) {
        int n = 0;
        for (int i = s.indexOf(mau); i >= 0; i = s.indexOf(mau, i + mau.length())) {
            n++;
        }
        return n;
    }

    private static String docToanBoSrc() {
        final StringBuilder sb = new StringBuilder();
        try (Stream<Path> ds = Files.walk(Paths.get("src"))) {
            ds.filter(p -> p.toString().endsWith(".java")).forEach(p -> sb.append(doc(p)).append('\n'));
        } catch (final IOException e) {
            return "";
        }
        return sb.toString();
    }

    private static String doc(final Path p) {
        try {
            return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemLocLogVatPham() {
    }
}
