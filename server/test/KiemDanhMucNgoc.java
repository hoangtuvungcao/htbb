import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import real.GemTemplate;

/**
 * Doi chieu web/admin/inc/danh_muc_ngoc.php voi mang real.Map.gemTemplate.
 *
 * VI SAO CAN BO KIEM NAY. Ten 270 vien ngoc khong co trong CSDL - chung ghi cung trong
 * src/real/MapNapTaiNguyen.java, va mot nua lay tu hang so data.Text.*. Trang quan tri
 * khong tra ten ngoc bang mot cau SELECT duoc, nen ban ten duoc SINH ra mot lan bang
 * tools/SinhDanhMucNgoc.java.
 *
 * Nghia la co HAI ban cua cung mot su that. Sua ngoc trong Java ma quen chay lai bo sinh
 * thi trang quan tri van chay binh thuong, van hien danh sach dep de - chi la hien TEN CU.
 * Nguoi tao giftcode chon "Tơ lụa cấp 6" va phat cho nguoi choi mot mon khac han. Khong
 * co gi bao loi, khong co gi do; chi co nguoi choi thac mac.
 *
 * Bo kiem nay lam cho viec quen do thanh mot phep kiem do, ngay tai may nguoi sua.
 */
public final class KiemDanhMucNgoc {

    private static final String TEP = "web/admin/inc/danh_muc_ngoc.php";

    /** 108 => ['ten' => 'Tơ lụa cấp 6', 'ban' => false, 'nhom' => 'NLCC6'], */
    private static final Pattern DONG = Pattern.compile(
            "^\\s*(\\d+)\\s*=>\\s*\\['ten'\\s*=>\\s*'(.*)'\\s*,\\s*'ban'\\s*=>\\s*(true|false)\\s*,"
                    + "\\s*'nhom'\\s*=>\\s*'([^']*)'\\s*\\],\\s*$");

    private KiemDanhMucNgoc() {
    }

    public static void chay() {
        Kiem.nhom("Danh muc ngoc - tep sinh phai khop mang Java");

        final String php = doc(TEP);
        Kiem.dung("doc duoc " + TEP, !php.isEmpty());
        if (php.isEmpty()) {
            return;
        }

        // ---- Doc tep sinh ----
        final Map<Integer, String[]> tuTep = new LinkedHashMap<Integer, String[]>();
        for (final String dong : php.split("\r?\n")) {
            final Matcher m = DONG.matcher(dong);
            if (m.matches()) {
                tuTep.put(Integer.valueOf(Integer.parseInt(m.group(1))),
                        new String[]{boThoat(m.group(2)), m.group(3), m.group(4)});
            }
        }
        Kiem.dung("doc duoc dong du lieu tu tep sinh", tuTep.size() > 200);

        // ---- Nap mang Java ----
        real.Map.loadGemTemplate();
        final GemTemplate[] ds = real.Map.gemTemplate;
        Kiem.dung("nap duoc Map.gemTemplate", ds != null && ds.length > 0);
        if (ds == null || ds.length == 0) {
            return;
        }

        Kiem.bang("so vien trong tep khop so vien trong Java",
                Integer.valueOf(ds.length), Integer.valueOf(tuTep.size()));

        // ---- Nhom nguyen lieu theo ID_MATERIAL_LOW / HIGHT ----
        final Map<Integer, String> nhomJava = new HashMap<Integer, String>();
        gomNhom(nhomJava, GemTemplate.ID_MATERIAL_LOW, "NLSC");
        gomNhom(nhomJava, GemTemplate.ID_MATERIAL_HIGHT, "NLCC");
        Kiem.bang("Java co 60 vien nguyen lieu (12 nhom x 5 he)",
                Integer.valueOf(60), Integer.valueOf(nhomJava.size()));

        // ---- Doi chieu tung vien ----
        int lechTen = 0;
        int lechBan = 0;
        int lechNhom = 0;
        int thieu = 0;
        String viDuLech = "";
        for (int i = 0; i < ds.length; i++) {
            final GemTemplate g = ds[i];
            if (g == null) {
                continue;
            }
            final String[] t = tuTep.get(Integer.valueOf(i));
            if (t == null) {
                thieu++;
                continue;
            }
            if (!gopMotDong(g.name).equals(t[0])) {
                lechTen++;
                if (viDuLech.isEmpty()) {
                    viDuLech = "id " + i + ": Java='" + gopMotDong(g.name) + "' tep='" + t[0] + "'";
                }
            }
            final boolean banJava = g.sell == real.Map.SELL;
            if (banJava != "true".equals(t[1])) {
                lechBan++;
            }
            final String nJava = nhomJava.containsKey(Integer.valueOf(i))
                    ? nhomJava.get(Integer.valueOf(i)) : "";
            if (!nJava.equals(t[2])) {
                lechNhom++;
            }
        }

        Kiem.bang("khong vien nao thieu trong tep sinh", Integer.valueOf(0), Integer.valueOf(thieu));
        Kiem.bang("khong ten nao lech" + (viDuLech.isEmpty() ? "" : " (" + viDuLech + ")"),
                Integer.valueOf(0), Integer.valueOf(lechTen));
        Kiem.bang("khong co ban/khong-ban nao lech", Integer.valueOf(0), Integer.valueOf(lechBan));
        Kiem.bang("khong nhom nguyen lieu nao lech", Integer.valueOf(0), Integer.valueOf(lechNhom));

        // ---- Hai nhom nguoi van hanh goi ten thuong xuyen nhat ----
        // NLSC6 / NLCC6 la ly do bo chon nay ton tai. Ghi cung id o day de doi mang ben
        // Java ma quen sinh lai thi do ngay, chu khong am tham lech.
        Kiem.bang("NLSC cap 6 dung id 73,80,87,94,101",
                "73,80,87,94,101", idCuaNhom(nhomJava, "NLSC6"));
        Kiem.bang("NLCC cap 6 dung id 108,115,122,129,136",
                "108,115,122,129,136", idCuaNhom(nhomJava, "NLCC6"));

        // ---- Bo sinh phai con dung, va tep phai ghi ro la tep sinh ----
        final String tool = doc("tools/SinhDanhMucNgoc.java");
        Kiem.dung("con bo sinh tools/SinhDanhMucNgoc.java", !tool.isEmpty());
        Kiem.dung("tep sinh co ghi ro DUNG SUA TAY", php.contains("DUNG SUA TAY"));
        Kiem.dung("tep sinh co ghi cach sinh lai", php.contains("SinhDanhMucNgoc"));
    }

    /** Gop xuong dong y het bo sinh, de so sanh cong bang. */
    private static String gopMotDong(final String ten) {
        String s = ten == null ? "" : ten;
        s = s.replace("\r", "").replace("\n", " · ").trim();
        while (s.contains("  ")) {
            s = s.replace("  ", " ");
        }
        return s.isEmpty() ? "(khong ten)" : s;
    }

    /** Bo thoat cua chuoi PHP nhay don. */
    private static String boThoat(final String s) {
        return s.replace("\\'", "'").replace("\\\\", "\\");
    }

    private static void gomNhom(final Map<Integer, String> ra, final short[][] mang, final String tienTo) {
        if (mang == null) {
            return;
        }
        for (int cap = 0; cap < mang.length; cap++) {
            if (mang[cap] == null) {
                continue;
            }
            for (final short id : mang[cap]) {
                ra.put(Integer.valueOf(id), tienTo + (cap + 1));
            }
        }
    }

    private static String idCuaNhom(final Map<Integer, String> nhom, final String ten) {
        final java.util.List<Integer> ds = new java.util.ArrayList<Integer>();
        for (final Map.Entry<Integer, String> e : nhom.entrySet()) {
            if (ten.equals(e.getValue())) {
                ds.add(e.getKey());
            }
        }
        java.util.Collections.sort(ds);
        final StringBuilder s = new StringBuilder();
        for (final Integer i : ds) {
            if (s.length() > 0) {
                s.append(',');
            }
            s.append(i);
        }
        return s.toString();
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
