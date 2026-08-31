import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Doi chieu web/admin/inc/danh_muc_danh_hieu.php voi data.DanhHieu.getName() VA
 * voi mang availableTitles ghi cung trong src/data/DatabaseGiftCode.java.
 *
 * Hai dieu phai dung dong thoi:
 *   1. Ten trong tep sinh phai khop CHINH XAC getName() - lech ten la nguoi tao
 *      ma thay mot cai, nguoi choi nhan mot cai khac (hoac danh hieu rong/"TEST").
 *   2. Co "ngauNhien" phai khop CHINH XAC mang availableTitles that trong
 *      DatabaseGiftCode.java - sai co nay la bo chon hua "chon duoc ngau nhien"
 *      cho mot danh hieu ma nhanh xu ly DH khong bao gio random ra duoc.
 */
public final class KiemDanhMucDanhHieu {

    private static final String TEP = "web/admin/inc/danh_muc_danh_hieu.php";
    private static final String TEP_GOC = "src/data/DatabaseGiftCode.java";

    private static final Pattern DONG = Pattern.compile(
            "^\\s*(\\d+)\\s*=>\\s*\\['ten'\\s*=>\\s*'(.*)'\\s*,\\s*'ngauNhien'\\s*=>\\s*(true|false)\\s*\\],\\s*$");

    private KiemDanhMucDanhHieu() {
    }

    public static void chay() {
        Kiem.nhom("Danh muc danh hieu - tep sinh phai khop getName() va mang ngau nhien that");

        final String php = doc(TEP);
        Kiem.dung("doc duoc " + TEP, !php.isEmpty());
        final String goc = doc(TEP_GOC);
        Kiem.dung("doc duoc " + TEP_GOC, !goc.isEmpty());
        if (php.isEmpty() || goc.isEmpty()) {
            return;
        }

        final Map<Integer, String[]> tuTep = new LinkedHashMap<Integer, String[]>();
        for (final String dong : php.split("\r?\n")) {
            final Matcher m = DONG.matcher(dong);
            if (m.matches()) {
                tuTep.put(Integer.valueOf(Integer.parseInt(m.group(1))),
                        new String[]{boThoat(m.group(2)), m.group(3)});
            }
        }
        Kiem.dung("doc duoc dong du lieu tu tep sinh", tuTep.size() > 30);

        // ---- 1. Ten phai khop data.DanhHieu.getName() ----
        int lechTen = 0;
        int coTenRongHoacTest = 0;
        String viDuLech = "";
        for (final Map.Entry<Integer, String[]> e : tuTep.entrySet()) {
            final String tenThat = data.DanhHieu.getName(e.getKey().intValue());
            if (tenThat == null || tenThat.isEmpty() || "TEST".equals(tenThat)) {
                coTenRongHoacTest++;
                continue;
            }
            if (!tenThat.equals(e.getValue()[0])) {
                lechTen++;
                if (viDuLech.isEmpty()) {
                    viDuLech = "id " + e.getKey() + ": that='" + tenThat + "' tep='" + e.getValue()[0] + "'";
                }
            }
        }
        Kiem.bang("khong id nao co ten rong hoac \"TEST\" lot vao tep",
                Integer.valueOf(0), Integer.valueOf(coTenRongHoacTest));
        Kiem.bang("khong ten nao lech voi getName()" + (viDuLech.isEmpty() ? "" : " (" + viDuLech + ")"),
                Integer.valueOf(0), Integer.valueOf(lechTen));

        // ---- 2. Co ngauNhien phai khop mang availableTitles THAT trong DatabaseGiftCode ----
        final Set<Integer> ngauNhienThat = docMangNgauNhienThat(goc);
        Kiem.dung("doc duoc mang availableTitles that (khong rong)", !ngauNhienThat.isEmpty());
        Kiem.bang("mang availableTitles that co dung 34 id",
                Integer.valueOf(34), Integer.valueOf(ngauNhienThat.size()));

        int lechCo = 0;
        String viDuLechCo = "";
        int soNgauNhienTrongTep = 0;
        for (final Map.Entry<Integer, String[]> e : tuTep.entrySet()) {
            final boolean tepNoiNgauNhien = "true".equals(e.getValue()[1]);
            final boolean thatSuNgauNhien = ngauNhienThat.contains(e.getKey());
            if (tepNoiNgauNhien) {
                soNgauNhienTrongTep++;
            }
            if (tepNoiNgauNhien != thatSuNgauNhien) {
                lechCo++;
                if (viDuLechCo.isEmpty()) {
                    viDuLechCo = "id " + e.getKey() + ": tep noi ngauNhien=" + tepNoiNgauNhien
                            + " nhung mang that " + (thatSuNgauNhien ? "CO" : "KHONG CO") + " id nay";
                }
            }
        }
        Kiem.bang("so id danh dau ngauNhien=true trong tep khop 34",
                Integer.valueOf(34), Integer.valueOf(soNgauNhienTrongTep));
        Kiem.bang("khong co \"ngauNhien\" nao lech voi mang that" + (viDuLechCo.isEmpty() ? "" : " (" + viDuLechCo + ")"),
                Integer.valueOf(0), Integer.valueOf(lechCo));

        // ---- Vai id cu the, doi chieu tay de chac chan khong sai ca hai ben cung mot kieu ----
        Kiem.bang("id 8767 (Anh hung) la ngau nhien",
                Boolean.TRUE, Boolean.valueOf("true".equals(tuTep.containsKey(Integer.valueOf(8767))
                        ? tuTep.get(Integer.valueOf(8767))[1] : "khong-co")));
        Kiem.dung("id 8983 (Chien Than Bat Diet 2) TON TAI nhung KHONG phai ngau nhien",
                tuTep.containsKey(Integer.valueOf(8983))
                        && "false".equals(tuTep.get(Integer.valueOf(8983))[1]));
        Kiem.sai("id 8999 (\"TEST\") khong duoc co trong tep", tuTep.containsKey(Integer.valueOf(8999)));
        for (int id = 8780; id <= 8784; id++) {
            Kiem.sai("id " + id + " (ten rong) khong duoc co trong tep",
                    tuTep.containsKey(Integer.valueOf(id)));
        }

        Kiem.dung("tep sinh co ghi ro DUNG SUA TAY", php.contains("DUNG SUA TAY"));
        Kiem.dung("tep sinh co ghi cach sinh lai", php.contains("SinhDanhMucDanhHieu"));
    }

    /**
     * Doc mang int[] availableTitles THAT trong nhanh xu ly "DH" cua
     * DatabaseGiftCode.java bang cach tim khoi so ngay sau khai bao ten bien do,
     * roi tach so.
     */
    private static Set<Integer> docMangNgauNhienThat(final String goc) {
        final Set<Integer> ra = new HashSet<Integer>();
        final int viTri = goc.indexOf("availableTitles = {");
        if (viTri < 0) {
            return ra;
        }
        final int dong = goc.indexOf('{', viTri);
        final int dongCuoi = goc.indexOf('}', dong);
        if (dong < 0 || dongCuoi < 0) {
            return ra;
        }
        final String than = goc.substring(dong + 1, dongCuoi);
        for (final String phan : than.split(",")) {
            final String s = phan.trim();
            if (!s.isEmpty()) {
                ra.add(Integer.valueOf(Integer.parseInt(s)));
            }
        }
        return ra;
    }

    private static String boThoat(final String s) {
        return s.replace("\\'", "'").replace("\\\\", "\\");
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
