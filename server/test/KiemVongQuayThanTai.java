import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Vong quay Than Tai - kiem tra viec them Canh (898-902) vao
 * CharSuKien.loadAllItemLucky() khong lam vo phan thuong phu (idgem/ps) dua vao
 * Char.groupLucky + Char.addMore.
 *
 * QUET MA NGUON, KHONG GOI loadAllItemLucky() THAT: ham do doc
 * Map.itemTemplates.get(5) - du lieu chi duoc nap day du khi server that khoi
 * dong (TeamServer nap tu file/CSDL), KHONG co trong bo test doc lap nay. Da
 * thu goi that va thay ArrayIndexOutOfBoundsException ngay o dong dau tien
 * (Map.itemTemplates.get(5) rong) - dung dung ly do khong Kiem* nao khac tung
 * dong toi ham nay. Nen thay vi chay code, test doc lai chinh van ban nguon va
 * kiem tra bang toan hoc thuan tuy (giong KiemLichSuKien/KiemBangCapDo).
 */
public final class KiemVongQuayThanTai {

    private static final String TEP_CHAR_SU_KIEN = "src/real/CharSuKien.java";
    private static final String TEP_CHAR = "src/real/Char.java";

    private KiemVongQuayThanTai() {
    }

    public static void chay() {
        Kiem.nhom("Vong quay Than Tai - them Canh (898-902), addMore phai bu dung");

        final String suKien = doc(TEP_CHAR_SU_KIEN);
        final String charJava = doc(TEP_CHAR);
        Kiem.dung("doc duoc " + TEP_CHAR_SU_KIEN, !suKien.isEmpty());
        Kiem.dung("doc duoc " + TEP_CHAR, !charJava.isEmpty());
        if (suKien.isEmpty() || charJava.isEmpty()) {
            return;
        }

        final int dauHam = suKien.indexOf("static void loadAllItemLucky()");
        final int cuoiHam = suKien.indexOf("static void doFinishLuckyDraw(");
        Kiem.dung("tim thay than ham loadAllItemLucky()", dauHam >= 0 && cuoiHam > dauHam);
        if (dauHam < 0 || cuoiHam <= dauHam) {
            return;
        }
        final String than = suKien.substring(dauHam, cuoiHam);

        // 1) Canh (898-902) phai co mat, KHONG con bi comment, va nam DUNG vi tri:
        // sau khi da them xong 619 (bo "choi"), truoc khi them gem id 11.
        final int viTriSau619 = than.lastIndexOf("item2.id = 619;");
        final int viTriGem11 = than.indexOf("item2.id = 11;");
        Kiem.dung("tim thay moc id 619 va gem id 11 de xac dinh vi tri chen Canh",
                viTriSau619 >= 0 && viTriGem11 > viTriSau619);
        final String vungCanh = (viTriSau619 >= 0 && viTriGem11 > viTriSau619)
                ? than.substring(viTriSau619, viTriGem11) : "";
        for (final int id : new int[]{898, 899, 900, 901, 902}) {
            final boolean coDong = Pattern.compile("(?m)^\\s*item2\\.id = " + id + ";\\s*$").matcher(vungCanh).find();
            Kiem.dung("Canh id " + id + " co mat, dung vi tri (giua 619 va gem 11), khong bi comment", coDong);
        }
        Kiem.sai("khong con TODO cu ve Canh trong loadAllItemLucky()", than.contains("TODO"));

        // 2) N = tong so vat pham THUC SU duoc them vao allItemLuckyDraw truoc khi
        // toi vong lap ngoc khoa (idgem) - gom vong lap ao giap (584..592, cong don
        // theo bien dem "i") CONG voi tung dong ".add(item2)" rieng le (choi + Canh
        // + 1 ngoc + 3 thu). groupLucky[2] = {15..41} duoc thiet ke voi gia dinh vi
        // tri dau tien cua vong lap idgem la "15 + Char.addMore" - tuc addMore PHAI
        // luon bang N-15, bat ky sau nay co them/bot bao nhieu vat pham o day.
        final int viTriIdgem = than.indexOf("final short[] idgem");
        Kiem.dung("tim thay khai bao mang idgem", viTriIdgem > 0);
        final String truocIdgem = than.substring(0, viTriIdgem);

        final Matcher mVongLap = Pattern.compile("for \\(int i = (\\d+); i <= (\\d+); \\+\\+i\\)").matcher(truocIdgem);
        Kiem.dung("tim thay vong lap ao giap (584..592)", mVongLap.find());
        final int demAoGiap = mVongLap.groupCount() >= 2
                ? Integer.parseInt(mVongLap.group(2)) - Integer.parseInt(mVongLap.group(1)) + 1 : 0;
        final int demRieng = demSoLan(truocIdgem, Pattern.compile("Char\\.allItemLuckyDraw\\.add\\(item2\\);"));
        final int tongN = demAoGiap + demRieng;

        final Matcher mAddMore = Pattern.compile("Char\\.addMore = (\\d+);").matcher(charJava);
        Kiem.dung("tim thay Char.addMore trong " + TEP_CHAR, mAddMore.find());
        final int addMore = mAddMore.groupCount() >= 1 ? Integer.parseInt(mAddMore.group(1)) : -1;
        Kiem.bang("Char.addMore = N - 15 (N=" + tongN + " vat pham truoc vong lap idgem, gom "
                        + demAoGiap + " ao giap + " + demRieng + " rieng le)",
                Integer.valueOf(tongN - 15), Integer.valueOf(addMore));

        // 3) idgem/ps phai dai dung nhu groupLucky[] gia dinh: nhom {15..41} (27
        // phan tu, dung do dai idgem) va ba nhom con lai gop thanh {42..52} lien
        // tuc (11 phan tu, dung do dai ps) - neu ai do them/bot phan tu trong idgem
        // hoac ps ma khong sua groupLucky[], hai kiem tra duoi day se do.
        final Matcher mIdgem = Pattern.compile("final short\\[\\] idgem = \\{([^}]*)\\};").matcher(than);
        final Matcher mPs = Pattern.compile("final byte\\[\\] ps = \\{([^}]*)\\};").matcher(than);
        Kiem.dung("tim thay mang idgem", mIdgem.find());
        Kiem.dung("tim thay mang ps", mPs.find());
        final int idgemLen = mIdgem.groupCount() >= 1 ? demPhanTu(mIdgem.group(1)) : 0;
        final int psLen = mPs.groupCount() >= 1 ? demPhanTu(mPs.group(1)) : 0;
        Kiem.bang("idgem co 27 phan tu (ngoc khoa)", Integer.valueOf(27), Integer.valueOf(idgemLen));
        Kiem.bang("ps co 11 phan tu (thuoc/xu)", Integer.valueOf(11), Integer.valueOf(psLen));

        final Matcher mGroup = Pattern.compile(
                "Char\\.groupLucky = new byte\\[\\]\\[\\]\\{\\{([^}]*)\\}, \\{([^}]*)\\}, \\{([^}]*)\\}, \\{([^}]*)\\}\\};"
        ).matcher(charJava);
        Kiem.dung("tim thay Char.groupLucky", mGroup.find());
        if (mGroup.groupCount() >= 4) {
            final TreeSet<Integer> psGop = new TreeSet<Integer>();
            for (final int nhom : new int[]{0, 1, 3}) {
                for (final String s : mGroup.group(nhom + 1).split(",")) {
                    psGop.add(Integer.valueOf(s.trim()));
                }
            }
            Kiem.bang("nhom ngoc (index 2 cua groupLucky) co dung " + idgemLen + " phan tu, bat dau tu 15",
                    "15.." + (15 + idgemLen - 1), minMax(mGroup.group(3)));
            Kiem.bang("ba nhom con lai gop lien tuc thanh " + psLen + " phan tu, bat dau tu " + (15 + idgemLen),
                    "[" + (15 + idgemLen) + ".." + (15 + idgemLen + psLen - 1) + "], co " + psLen + " phan tu",
                    "[" + psGop.first() + ".." + psGop.last() + "], co " + psGop.size() + " phan tu");
        }
    }

    private static int demPhanTu(final String csv) {
        return csv.trim().isEmpty() ? 0 : csv.split(",").length;
    }

    private static String minMax(final String csv) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (final String s : csv.split(",")) {
            final int v = Integer.parseInt(s.trim());
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        return min + ".." + max;
    }

    private static int demSoLan(final String s, final Pattern p) {
        final Matcher m = p.matcher(s);
        int dem = 0;
        while (m.find()) {
            dem++;
        }
        return dem;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
