import com.htbb.dulieu.BangSql;
import com.htbb.dulieu.DocDumpSql;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Doc CSDL DAY DU sql/htbb_day_du.sql cho test - NGUON DUY NHAT cua du lieu game (khong con bo sinh / tep le,
 * xem sql/README.md). Test kiem thang du lieu trong tep nay (so dong, dau, tham chieu) thay vi chay lai bo sinh.
 * Tep nang cap sql/nang_cap.sql cung doc qua day. Thu muc lam viec: server/.
 */
final class DayDu {

    static final String TEP = "../sql/htbb_day_du.sql";
    static final String TEP_NANG_CAP = "../sql/nang_cap.sql";

    private static String sql;
    private static DocDumpSql dump;
    private static final Map<String, BangSql> BANG = new LinkedHashMap<String, BangSql>();

    private DayDu() {
    }

    /** Toan van htbb_day_du.sql ("" neu khong doc duoc). */
    static synchronized String sql() {
        if (sql == null) {
            sql = doc(TEP);
        }
        return sql;
    }

    /** Toan van sql/nang_cap.sql. */
    /**
     * Mot phan cua sql/nang_cap.sql gop tu tep SQL le cu (mot tep SQL): tu dong "-- >>> {@code tep}" toi dong "-- >>> "
     * ke tiep (hoac het tep). "" neu khong co. Lenh danh muc cau hinh cua tep cu nam o phan 2 (khong o day).
     */
    static String phanNangCap(final String tep) {
        final String s = nangCap();
        final int a = s.indexOf("-- >>> " + tep + "\n");
        if (a < 0) {
            return "";
        }
        final int b = s.indexOf("\n-- >>> ", a + 1);
        return b < 0 ? s.substring(a) : s.substring(a, b + 1);
    }

    static String nangCap() {
        return doc(TEP_NANG_CAP);
    }

    static synchronized DocDumpSql dump() {
        if (dump == null) {
            dump = DocDumpSql.tuChuoi(sql());
        }
        return dump;
    }

    static boolean coBang(final String ten) {
        return dump().coBang(ten);
    }

    /** Moi cot, moi dong cua bang (null neu bang khong co). */
    static synchronized BangSql bang(final String ten) {
        if (!BANG.containsKey(ten)) {
            BangSql b = null;
            try {
                final String[] cot = dump().cotCua(ten);
                if (cot != null) {
                    b = dump().doc(ten, cot);
                }
            } catch (final Exception e) {
                b = null;
            }
            BANG.put(ten, b);
        }
        return BANG.get(ten);
    }

    static int soDong(final String ten) {
        final BangSql b = bang(ten);
        return b == null ? -1 : b.soDong();
    }

    /** Cac dong cua bang dang ban do cot -> gia tri (NULL -> null). */
    static List<Map<String, String>> dong(final String ten) {
        final List<Map<String, String>> ra = new ArrayList<Map<String, String>>();
        final BangSql b = bang(ten);
        if (b == null) {
            return ra;
        }
        final String[] cot = b.cot();
        for (int r = 0; r < b.soDong(); r++) {
            final Map<String, String> m = new LinkedHashMap<String, String>();
            for (final String c : cot) {
                m.put(c, b.chuoi(r, c));
            }
            ra.add(m);
        }
        return ra;
    }

    /** Nhu dong() nhung NULL -> "" (tien cho test doc kieu bang tinh). */
    static List<Map<String, String>> dongChu(final String ten) {
        final List<Map<String, String>> ra = dong(ten);
        for (final Map<String, String> m : ra) {
            for (final Map.Entry<String, String> e : m.entrySet()) {
                if (e.getValue() == null) {
                    e.setValue("");
                }
            }
        }
        return ra;
    }

    /**
     * Bang thiet ke tung la bang tinh trong du_lieu/ (nay chi con trong CSDL; ban CSV cu da luu tru o
     * tools/luu_tru_sinh_du_lieu/du_lieu/): noi_dung_X.csv -> data_noi_dung_X, su_kien_nam.csv -> data_su_kien_nam,
     * nhiem_vu_moi.csv -> data_quest_lau_dai + data_nhiem_vu_noi_dung (cung khoa ma), noi_dung_item_moi.csv -> data_item
     * id 1001..1199. Cot va gia tri giong het ban CSV (NULL -> "").
     */
    static List<Map<String, String>> bangThietKe(final String tenCsv) {
        final String t = tenCsv.replaceAll("^.*/", "");
        if ("nhiem_vu_moi.csv".equals(t)) {
            final Map<String, Map<String, String>> nd = new LinkedHashMap<String, Map<String, String>>();
            for (final Map<String, String> m : dongChu("data_nhiem_vu_noi_dung")) {
                nd.put(m.get("ma"), m);
            }
            final List<Map<String, String>> ra = dongChu("data_quest_lau_dai");
            for (final Map<String, String> m : ra) {
                final Map<String, String> x = nd.get(m.get("ma"));
                if (x != null) {
                    for (final Map.Entry<String, String> e : x.entrySet()) {
                        if (!m.containsKey(e.getKey())) {
                            m.put(e.getKey(), e.getValue());
                        }
                    }
                }
            }
            return thuTuTruoc(ra);
        }
        if ("noi_dung_item_moi.csv".equals(t)) {
            final List<Map<String, String>> ra = new ArrayList<Map<String, String>>();
            for (final Map<String, String> m : dongChu("data_item")) {
                final int id = Integer.parseInt(m.get("id"));
                if (id >= 1001 && id <= 1199) {
                    ra.add(m);
                }
            }
            return ra;
        }
        if ("su_kien_nam.csv".equals(t)) {
            return dongChu("data_su_kien_nam");
        }
        if (t.startsWith("noi_dung_") && t.endsWith(".csv") && coBang("data_" + t.substring(0, t.length() - 4))) {
            return dongChu("data_" + t.substring(0, t.length() - 4));
        }
        throw new IllegalArgumentException("khong co bang CSDL cho " + tenCsv);
    }

    /**
     * Danh muc cau hinh trung tam (cau_hinh_nhom + cau_hinh_danh_muc trong CSDL) viet lai dang chu giong danh_muc.json cu
     * (nay da luu tru) de test tim khoa: moi nhom {@code { "ma": "..", "ten": "..", "moTa": ".." }}, moi khoa
     * {@code { "khoa": "..", "nhom": "..", "lop": "..", "ten": "..", "kieu": "..", "macDinh": ".." }} (lop chi khi khac chinh).
     */
    static String danhMucJson() {
        final StringBuilder b = new StringBuilder("{\n  \"nhom\": [\n");
        for (final Map<String, String> n : dongChu("cau_hinh_nhom")) {
            b.append("    { \"ma\": \"").append(n.get("ma")).append("\", \"ten\": \"").append(n.get("ten"))
                    .append("\", \"moTa\": \"").append(n.get("mo_ta")).append("\" },\n");
        }
        b.append("  ],\n  \"khoa\": [\n");
        for (final Map<String, String> k : dongChu("cau_hinh_danh_muc")) {
            b.append("    { \"khoa\": \"").append(k.get("khoa")).append("\", \"nhom\": \"").append(k.get("nhom")).append('"');
            if (!"chinh".equals(k.get("lop")) && !k.get("lop").isEmpty()) {
                b.append(", \"lop\": \"").append(k.get("lop")).append('"');
            }
            b.append(", \"ten\": \"").append(k.get("ten")).append("\", \"kieu\": \"").append(k.get("kieu"))
                    .append("\", \"macDinh\": \"").append(k.get("mac_dinh")).append("\", \"apDung\": \"").append(k.get("ap_dung"))
                    .append("\" },\n");
        }
        return b.append("  ]\n}\n").toString();
    }

    /**
     * Sap nhiem vu theo thu tu thiet ke: dong 'truoc' dung truoc dong phu thuoc (sap topo, cung muc thi id_so nho truoc).
     * Bang CSDL xuat theo khoa chinh 'ma' nen mat thu tu cua bang tinh cu. Dong vuong vong / tro toi ma khong co thi xep
     * cuoi (test se bao loi).
     */
    static List<Map<String, String>> thuTuTruoc(final List<Map<String, String>> ds) {
        final Map<String, Map<String, String>> theoMa = new LinkedHashMap<String, Map<String, String>>();
        for (final Map<String, String> m : ds) {
            theoMa.put(m.get("ma"), m);
        }
        final Map<String, List<Map<String, String>>> con = new java.util.HashMap<String, List<Map<String, String>>>();
        final java.util.PriorityQueue<Map<String, String>> san = new java.util.PriorityQueue<Map<String, String>>(
                (x, y) -> Long.compare(Long.parseLong(x.get("id_so")), Long.parseLong(y.get("id_so"))));
        for (final Map<String, String> m : ds) {
            final String t = m.get("truoc");
            if (t == null || t.isEmpty() || !theoMa.containsKey(t)) {
                san.add(m);
            } else {
                con.computeIfAbsent(t, k -> new ArrayList<Map<String, String>>()).add(m);
            }
        }
        final List<Map<String, String>> ra = new ArrayList<Map<String, String>>();
        final java.util.Set<String> da = new java.util.HashSet<String>();
        while (!san.isEmpty()) {
            final Map<String, String> m = san.poll();
            ra.add(m);
            da.add(m.get("ma"));
            final List<Map<String, String>> c = con.get(m.get("ma"));
            if (c != null) {
                san.addAll(c);
            }
        }
        for (final Map<String, String> m : ds) {
            if (!da.contains(m.get("ma"))) {
                ra.add(m);
            }
        }
        return ra;
    }

    /** Dong dau tien co cot khoa = gia tri (null neu khong co). */
    static Map<String, String> tim(final String ten, final String cot, final String giaTri) {
        for (final Map<String, String> m : dong(ten)) {
            if (giaTri.equals(m.get(cot))) {
                return m;
            }
        }
        return null;
    }

    /** Gia tri cot 'lay' cua dong co cot 'khoa' = giaTri. */
    static String giaTri(final String ten, final String khoa, final String giaTri, final String lay) {
        final Map<String, String> m = tim(ten, khoa, giaTri);
        return m == null ? null : m.get(lay);
    }

    /** Khoi CREATE TABLE `ten` (...) cua ban day du ("" neu khong co). */
    static String ddl(final String ten) {
        final Matcher m = Pattern.compile("(?s)CREATE TABLE `" + Pattern.quote(ten) + "` \\(.*?\\n\\)[^;]*;").matcher(sql());
        return m.find() ? m.group() : "";
    }

    /** Cot cua bang (rong neu khong co). */
    static List<String> cot(final String ten) {
        final List<String> ra = new ArrayList<String>();
        final String[] c = dump().cotCua(ten);
        if (c != null) {
            for (final String x : c) {
                ra.add(x);
            }
        }
        return ra;
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
