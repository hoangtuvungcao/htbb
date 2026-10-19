import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Doi chieu du_lieu/danh_muc_item.md voi bang data_item trong du_lieu/sql_nguon/gamedb7_clean.sql.
 *
 * Doc lai NGUON (SQL) bang mot cach RIENG (khong goi lai code cua SinhDanhMucItem)
 * roi so id+ten voi tep da sinh - phong sinh thieu/du/sai id ma khong ai nhin ra
 * giua ~900 dong.
 */
public final class KiemDanhMucItem {

    private static final String NGUON = "../du_lieu/sql_nguon/gamedb7_clean.sql";
    private static final String DICH = "../du_lieu/danh_muc_item.md";

    // Bat dau moi tuple data_item: "(<id>, '<ten co the co dau nhay thoat \' hoac ''>'"
    private static final Pattern MOC_SQL = Pattern.compile(
            "\\((\\d+),\\s*'((?:[^'\\\\]|\\\\.|'')*)'");
    private static final Pattern DONG_MD = Pattern.compile(
            "^\\|\\s*(\\d+)\\s*\\|\\s*(.*?)\\s*\\|\\s*\\d+\\s*\\|\\s*\\d+\\s*\\|\\s*[^|]*\\|\\s*[^|]*\\|$");

    private KiemDanhMucItem() {
    }

    public static void chay() {
        Kiem.nhom("Danh muc item - tep sinh phai khop bang data_item trong SQL");

        final String sql = doc(NGUON);
        Kiem.dung("doc duoc " + NGUON, !sql.isEmpty());
        final String md = doc(DICH);
        Kiem.dung("doc duoc " + DICH, !md.isEmpty());
        if (sql.isEmpty() || md.isEmpty()) {
            return;
        }

        // Chi lay phan trong cac khoi "INSERT INTO `data_item` ... VALUES ... ;"
        // tranh vo tinh khop nham tuple cua bang khac co dang tuong tu.
        final StringBuilder khoiDataItem = new StringBuilder();
        final Matcher mKhoi = Pattern.compile(
                "INSERT INTO `data_item`[^V]*VALUES\\s*([\\s\\S]*?);").matcher(sql);
        while (mKhoi.find()) {
            khoiDataItem.append(mKhoi.group(1)).append('\n');
        }
        Kiem.dung("tim thay it nhat 1 khoi INSERT INTO `data_item`", khoiDataItem.length() > 0);

        final Map<Integer, String> tuSql = new LinkedHashMap<Integer, String>();
        final Matcher mSql = MOC_SQL.matcher(khoiDataItem);
        while (mSql.find()) {
            final int id = Integer.parseInt(mSql.group(1));
            // trim(): SinhDanhMucItem chuan hoa khoang trang thua trong ten luc sinh
            // (vd id 115 "Nhan lam ngoc ") - trim o day cho khop, xem chu thich o do.
            final String ten = boDauNhay(mSql.group(2)).trim();
            tuSql.put(Integer.valueOf(id), ten);
        }

        final Map<Integer, String> tuMd = new LinkedHashMap<Integer, String>();
        for (final String dong : md.split("\r?\n")) {
            final Matcher mMd = DONG_MD.matcher(dong);
            if (mMd.matches()) {
                tuMd.put(Integer.valueOf(Integer.parseInt(mMd.group(1))), unescMd(mMd.group(2)));
            }
        }

        Kiem.bang("so vat pham trong tep sinh khop so tuple trong SQL",
                Integer.valueOf(tuSql.size()), Integer.valueOf(tuMd.size()));

        int lech = 0;
        String viDuLech = "";
        for (final Map.Entry<Integer, String> e : tuSql.entrySet()) {
            final String tenMd = tuMd.get(e.getKey());
            if (tenMd == null) {
                lech++;
                if (viDuLech.isEmpty()) {
                    viDuLech = "id " + e.getKey() + " co trong SQL nhung khong co trong tep sinh";
                }
                continue;
            }
            if (!tenMd.equals(e.getValue())) {
                lech++;
                if (viDuLech.isEmpty()) {
                    viDuLech = "id " + e.getKey() + ": SQL=\"" + e.getValue() + "\" tep=\"" + tenMd + "\"";
                }
            }
        }
        Kiem.bang("khong id nao lech ten" + (viDuLech.isEmpty() ? "" : " (" + viDuLech + ")"),
                Integer.valueOf(0), Integer.valueOf(lech));

        // Vai moc da doi chieu tay, phong ca hai cach doc (SQL va MD) cung sai giong
        // nhau (vi du cung bo sot mot loai dau nhay dac biet) ma phep so o tren
        // khong bat ra duoc.
        Kiem.bang("id 1 = 'Áo bà ba'", "Áo bà ba", tuMd.get(Integer.valueOf(1)));
        Kiem.bang("id 872 = 'Mặt nạ ông địa' (doi chieu ghi chu icon 800-806 bi chan)",
                "Mặt nạ ông địa", tuMd.get(Integer.valueOf(872)));
        Kiem.bang("id 904 = 'Cải Trang Sa Tăng'", "Cải Trang Sa Tăng", tuMd.get(Integer.valueOf(904)));

        // Moc o VUNG MOI. Ba moc tren deu <= 904, tuc dung o cuoi ban dump cu; neu ban
        // dump lai bi cat ngang o do thi ba moc kia van dat. Hai moc duoi neo lai phan
        // duoi cua bang: 910 (Phi phong vo hinh) va 954 (id lon nhat hien co).
        Kiem.bang("id 910 = 'Phi phong vô hình'", "Phi phong vô hình", tuMd.get(Integer.valueOf(910)));
        Kiem.bang("id 954 = 'Skin Chiến Lang (hệ Thổ)' (id lon nhat)",
                "Skin Chiến Lang (hệ Thổ)", tuMd.get(Integer.valueOf(954)));

        // Ten co ky tu xuong dong (mysqldump ghi thanh "\n" hai ky tu) phai qua duoc ca
        // hai duong doc ma khong lam vo dong bang markdown.
        final String ten888 = tuMd.get(Integer.valueOf(888));
        Kiem.dung("id 888 doc duoc va giu nguyen \\n dang ky tu (khong vo dong bang)",
                ten888 != null && ten888.startsWith("Áo noel nam 2024\\n"));

        Kiem.dung("tep sinh co ghi ro DUNG SUA TAY", md.contains("sinh ra"));
        Kiem.dung("tep sinh co ghi cach sinh lai", md.contains("SinhDanhMucItem"));
    }

    private static String boDauNhay(final String s) {
        return s.replace("\\'", "'").replace("''", "'");
    }

    private static String unescMd(final String s) {
        return s.replace("\\|", "|");
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
