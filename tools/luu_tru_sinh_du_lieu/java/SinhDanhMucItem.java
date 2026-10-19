import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sinh du_lieu/danh_muc_item.md tu bang data_item trong du_lieu/sql_nguon/gamedb7_clean.sql
 * (BAN MOI, khong phai htbb_db.sql da cu - xem README.md).
 *
 * VI SAO SINH CHU KHONG GO TAY: ~900 vat pham, go tay chac chan sot/sai id.
 * File nay chi de DEV TRA CUU id<->ten khi doc/viet code (giftcode, shop, che
 * tao...) - khong phai nguon du lieu that (nguon that van la DB dang chay qua
 * data_item, file nay chi la ban chup tu SQL dump).
 *
 * Cach dung: java -cp "server/build/classes;server/lib/mysql-connector-java-5.1.49.jar" SinhDanhMucItem
 *
 * Chay lai khi du_lieu/sql_nguon/gamedb7_clean.sql doi (them/sua vat pham). test/KiemDanhMucItem.java
 * doi chieu so luong + vai id moc voi chinh file SQL, phong sinh thieu/sinh sai.
 */
public final class SinhDanhMucItem {

    private static final String NGUON = "du_lieu/sql_nguon/gamedb7_clean.sql";
    private static final String DICH = "du_lieu/danh_muc_item.md";

    private static final Pattern INSERT = Pattern.compile(
            "INSERT INTO `data_item`[^V]*VALUES\\s*([\\s\\S]*?);");

    public static void main(final String[] thamSo) throws Exception {
        final String sql = new String(Files.readAllBytes(Paths.get(NGUON)), StandardCharsets.UTF_8);

        final List<String[]> hang = new ArrayList<String[]>();
        final Matcher mInsert = INSERT.matcher(sql);
        while (mInsert.find()) {
            for (final String tuple : tachTuple(mInsert.group(1))) {
                hang.add(tachTruong(tuple));
            }
        }

        if (hang.isEmpty()) {
            System.out.println("[LOI] khong doc duoc dong nao tu " + NGUON + " - khong sinh gi.");
            System.exit(1);
        }

        // Thu tu cot trong data_item: id,name,type,style,he,gender,level,durable,
        // atb0..atb9(10 cot),price,clazz,xstart,ystart,colortype,nloan,idUpLevel,
        // namein,ideff,pricecu - chi lay vai cot huu ich cho tra cuu nhanh.
        final int[] id = new int[hang.size()];
        final String[] ten = new String[hang.size()];
        final int[] type = new int[hang.size()];
        final int[] level = new int[hang.size()];
        final String[] gia = new String[hang.size()];
        final String[] icon = new String[hang.size()];
        for (int i = 0; i < hang.size(); i++) {
            final String[] f = hang.get(i);
            id[i] = Integer.parseInt(boDauNhay(f[0]));
            // trim(): mot vai ten trong data_item co khoang trang thua o dau (vd id
            // 115 "Nhan lam ngoc ") - khong y nghia gi cho muc dich tra cuu, va bang
            // markdown khong the giu duoc no dang tin cay (khong phan biet duoc voi
            // dau cach ngan cot khi doc lai) nen chuan hoa luon luc sinh.
            ten[i] = boDauNhay(f[1]).trim();
            type[i] = Integer.parseInt(boDauNhay(f[2]));
            level[i] = Integer.parseInt(boDauNhay(f[6]));
            gia[i] = boDauNhay(f[18]);
            icon[i] = boDauNhay(f[20]);
        }

        // Sap theo id tang dan cho de tra - du lieu nguon von da theo thu tu nay
        // nhung sap lai cho chac, phong INSERT sau nay khong con theo thu tu.
        final Integer[] thuTu = new Integer[id.length];
        for (int i = 0; i < thuTu.length; i++) thuTu[i] = i;
        java.util.Arrays.sort(thuTu, (a, b) -> Integer.compare(id[a], id[b]));

        final StringBuilder s = new StringBuilder();
        s.append("# Danh mục vật phẩm (data_item)\n\n");
        s.append("File này được **sinh ra**, đừng sửa tay.\n");
        s.append("  Nguồn  : ").append(NGUON).append(", bảng `data_item`\n");
        s.append("  Sinh lại: java -cp \"build/classes;");
        s.append("server/lib/mysql-connector-java-5.1.49.jar\" SinhDanhMucItem\n\n");
        s.append("Mục đích: tra nhanh **id ↔ tên vật phẩm** khi đọc/viết code (giftcode, shop,\n");
        s.append("chế tạo, danh sách vật phẩm sự kiện...) thay vì tự query DB hay đoán từ magic\n");
        s.append("number rải rác trong `Item.java`/`ModelWearing.java`. `type` là cột thô từ DB\n");
        s.append("(xem cách dùng trong `Item.java` nếu cần biết ý nghĩa từng giá trị — không lặp\n");
        s.append("lại ở đây để tránh lệch khi type có thêm giá trị mới). `icon` = cột `xstart`\n");
        s.append("(id ảnh, xem `map/icon/<icon>.png`); dải 800-806 BỊ CHẶN (xem `map/other/`,\n");
        s.append("và mục 2 trong ghi chú icon của dự án) — đừng gán vật phẩm mới vào dải đó.\n\n");
        s.append("test/KiemDanhMucItem.java đối chiếu số lượng + vài id mốc với chính file SQL.\n\n");
        s.append("Tổng: ").append(hang.size()).append(" vật phẩm (id ")
                .append(id[thuTu[0]]).append("-").append(id[thuTu[thuTu.length - 1]]).append(").\n\n");
        s.append("| ID | Tên | type | Cấp | Giá | icon |\n");
        s.append("|---:|---|---:|---:|---:|---:|\n");
        for (final int i : thuTu) {
            s.append("| ").append(id[i]).append(" | ").append(escMd(ten[i])).append(" | ")
                    .append(type[i]).append(" | ").append(level[i]).append(" | ")
                    .append(gia[i]).append(" | ").append(icon[i]).append(" |\n");
        }

        try (Writer w = new OutputStreamWriter(new FileOutputStream(DICH), StandardCharsets.UTF_8)) {
            w.write(s.toString());
        }
        System.out.println("Da sinh " + DICH + " voi " + hang.size() + " vat pham.");
        System.exit(0);
    }

    /** Tach mot khoi "VALUES (...),(...),..." thanh danh sach noi dung ben trong tung cap ngoac, ton trong dau nhay. */
    private static List<String> tachTuple(final String khoi) {
        final List<String> ra = new ArrayList<String>();
        int depth = 0;
        boolean inStr = false;
        final StringBuilder cur = new StringBuilder();
        for (int i = 0; i < khoi.length(); i++) {
            final char c = khoi.charAt(i);
            if (inStr) {
                cur.append(c);
                if (c == '\'' && (i == 0 || khoi.charAt(i - 1) != '\\')) {
                    inStr = false;
                }
                continue;
            }
            if (c == '\'') {
                inStr = true;
                cur.append(c);
                continue;
            }
            if (c == '(') {
                depth++;
                if (depth == 1) {
                    cur.setLength(0);
                    continue;
                }
            }
            if (c == ')') {
                depth--;
                if (depth == 0) {
                    ra.add(cur.toString());
                    continue;
                }
            }
            if (depth >= 1) {
                cur.append(c);
            }
        }
        return ra;
    }

    /** Tach mot tuple theo dau phay o CAP NGOAI CUNG (khong nam trong dau nhay). */
    private static String[] tachTruong(final String tuple) {
        final List<String> ra = new ArrayList<String>();
        boolean inStr = false;
        final StringBuilder cur = new StringBuilder();
        for (int i = 0; i < tuple.length(); i++) {
            final char c = tuple.charAt(i);
            if (inStr) {
                cur.append(c);
                if (c == '\'' && (i == 0 || tuple.charAt(i - 1) != '\\')) {
                    inStr = false;
                }
                continue;
            }
            if (c == '\'') {
                inStr = true;
                cur.append(c);
            } else if (c == ',') {
                ra.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        ra.add(cur.toString().trim());
        return ra.toArray(new String[0]);
    }

    private static String boDauNhay(final String s) {
        String t = s.trim();
        if (t.equals("NULL")) {
            return "";
        }
        if (t.length() >= 2 && t.charAt(0) == '\'' && t.charAt(t.length() - 1) == '\'') {
            t = t.substring(1, t.length() - 1).replace("\\'", "'").replace("''", "'");
        }
        return t;
    }

    private static String escMd(final String s) {
        return s.replace("|", "\\|");
    }

    private SinhDanhMucItem() {
    }
}
