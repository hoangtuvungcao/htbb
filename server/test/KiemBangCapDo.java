import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Doi chieu HTBB-Web/shared/data/bang-cap-do.json (tools/SinhBangCapDo sinh; web doc qua
 * server/utils/capDo.ts) voi mang real.LevelDetail.expMain / expRequire. Truoc day la tep PHP
 * web/admin/inc/capdo.php - website PHP da bo (docs/pokemon/BO_WEB_PHP.md).
 *
 * Mang co 91 phan tu, nhieu so dai toi 14 chu so - loi go nham MOT chu so o day
 * khong ai nhin ra bang mat. Bo kiem nay doc lai tung so trong tep JSON va so
 * TUNG PHAN TU MOT voi mang that, khong chi so luong phan tu.
 */
public final class KiemBangCapDo {

    private static final String TEP = "shared/data/bang-cap-do.json";
    /** Bo sinh ghi MOI phan tu mang mot dong: "    12345," (phan tu cuoi khong co dau phay). */
    private static final Pattern DONG_SO = Pattern.compile("^\\s*(\\d+),?\\s*$");

    private KiemBangCapDo() {
    }

    public static void chay() {
        Kiem.nhom("Bang cap do - tep sinh phai khop tung so voi LevelDetail.expMain");

        if (WebHtbb.boQua("bang cap do")) {
            return;
        }
        final String php = WebHtbb.doc(TEP);
        Kiem.dung("doc duoc " + WebHtbb.ten(TEP), !php.isEmpty());
        if (php.isEmpty()) {
            return;
        }

        final long[] that = real.LevelDetail.expMain;
        Kiem.dung("doc duoc real.LevelDetail.expMain", that != null && that.length > 0);
        if (that == null) {
            return;
        }

        // expMain (chi so mang = cap - 1) va expRequire (chi so mang = CHINH cap do) nam
        // trong cung mot tep, cung dang "moi so mot dong" - phai tach rieng tung mang truoc
        // khi doi chieu, khong thi regex se gop nham ca hai bang lam mot.
        final int viTriMang1 = php.indexOf("\"expMain\"");
        final int viTriHam2 = php.indexOf("\"expRequire\"");
        Kiem.dung("tim thay mang expMain trong tep", viTriMang1 >= 0);
        final String phanExp = viTriHam2 > viTriMang1 && viTriMang1 >= 0
                ? php.substring(viTriMang1, viTriHam2) : (viTriMang1 >= 0 ? php.substring(viTriMang1) : "");
        doiChieu("expMain", phanExp, that, 1);

        if (viTriHam2 >= 0) {
            final long[] thatRequire = real.LevelDetail.expRequire;
            Kiem.dung("doc duoc real.LevelDetail.expRequire", thatRequire != null && thatRequire.length > that.length);
            if (thatRequire != null) {
                doiChieu("expRequire", php.substring(viTriHam2), thatRequire, 0);
            }
        } else {
            Kiem.dung("tim thay mang expRequire trong tep (cho chuc nang Buff level %)", false);
        }

        // Tran cap ghi trong tep phai THAP HON do dai mang mot don vi - tranh dung
        // toi bien co loi lech-mot trong chinh LevelDetail.getXpFromLevel() ben Java
        // (dieu kien "level < expMain.length" sai cho dung level = do dai mang).
        if (preg1(php, "\"capDoToiDa\":\\s*(\\d+)") != null) {
            final int tranCap = Integer.parseInt(preg1(php, "\"capDoToiDa\":\\s*(\\d+)"));
            Kiem.bang("CAPDO_TOI_DA = do dai mang - 1 (tranh loi lech-mot cua Java)",
                    Integer.valueOf(that.length - 1), Integer.valueOf(tranCap));
        } else {
            Kiem.dung("tim thay capDoToiDa trong tep", false);
        }

        // Doi chieu voi du lieu THAT dang chay: cap cao nhat tung ghi nhan tren VPS
        // la 90 (doi chieu 31/08/2026) - tran cap cho phat phai >= gia tri do, khong
        // thi mot so nhan vat dang co san se KHONG the "dat lai dung cap hien tai".
        ktDungCu("tran cap (" + (that.length - 1) + ") >= 90 (cap cao nhat da ghi nhan tren VPS)",
                that.length - 1 >= 90);

        Kiem.dung("tep sinh co ghi ro DUNG SUA TAY", php.contains("DUNG SUA TAY"));
        Kiem.dung("tep sinh co ghi cach sinh lai", php.contains("SinhBangCapDo"));
        Kiem.dung("tep sinh co ghi ro moi thu khac tu tinh lai tu lastLv, khong duoc dong tay",
                php.contains("SelectCharHandler"));
    }

    private static void ktDungCu(final String moTa, final boolean dung) {
        Kiem.dung(moTa, dung);
    }

    /**
     * Doi chieu mot bang "N, // cap M" trong PHP voi mot mang long that ben Java.
     *
     * @param capBanDau nhan cap mong doi cho phan tu chi so 0 (1 cho capDoRaExp -
     *                  chi so mang = cap-1; 0 cho capDoRaExpRequire - chi so mang =
     *                  chinh cap do).
     */
    private static void doiChieu(final String tenHam, final String phanTep, final long[] that, final int capBanDau) {
        final List<Long> tuTep = new ArrayList<Long>();
        for (final String dong : phanTep.split("\r?\n")) {
            final Matcher m = DONG_SO.matcher(dong);
            if (m.matches()) {
                tuTep.add(Long.valueOf(Long.parseLong(m.group(1))));
            }
        }

        Kiem.bang(tenHam + " so muc trong tep khop so phan tu mang Java",
                Integer.valueOf(that.length), Integer.valueOf(tuTep.size()));

        int lech = 0;
        String viDuLech = "";
        for (int i = 0; i < that.length && i < tuTep.size(); i++) {
            // JSON khong co chu thich "// cap M" nhu tep PHP cu: cap suy tu VI TRI (chi so
            // + capBanDau). Dong bi xoa/chen nham thi so muc lech (kiem o tren) va cac so sau
            // do lech tung cai mot - van bi bat.
            if (that[i] != tuTep.get(i).longValue()) {
                lech++;
                if (viDuLech.isEmpty()) {
                    viDuLech = "cap " + (i + capBanDau) + ": Java=" + that[i] + " tep=" + tuTep.get(i);
                }
            }
        }
        Kiem.bang(tenHam + " khong so nao lech" + (viDuLech.isEmpty() ? "" : " (" + viDuLech + ")"),
                Integer.valueOf(0), Integer.valueOf(lech));
    }

    private static String preg1(final String s, final String regex) {
        final Matcher m = Pattern.compile(regex).matcher(s);
        return m.find() ? m.group(1) : null;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
