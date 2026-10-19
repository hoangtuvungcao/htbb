package com.htbb.dulieu;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Doc bang du lieu tu TEP .sql trong repo (khong can CSDL). Lop THUAN, dung cho test, cong cu mo phong
 * (tools/pokemon/MoPhongKinhTe.java) va doi chieu "tep sinh == dump day du".
 *
 * Hieu dung hai dang tep cua repo:
 *   - mysqldump (sql/htbb_day_du.sql): {@code CREATE TABLE `t` (...)} roi
 *     {@code INSERT INTO `t` VALUES (..),(..);}, chuoi thoat bang dau gach nguoc;
 *   - tep sinh (du_lieu/sql_nguon/pokemon_*.sql): co hoac khong co danh sach cot sau ten bang, chuoi thoat bang
 *     {@code ''} hoac gach nguoc.
 * Lenh khac (SET, LOCK, ALTER, UPDATE...) va chu thich ({@code --}, {@code #}, {@code /* *}{@code /}) bi bo
 * qua. {@code DROP TABLE} / {@code CREATE TABLE} (khong IF NOT EXISTS) / {@code DELETE FROM `t`} (khong WHERE)
 * xoa dong da gom - trang thai cuoi cung giong het khi nap tep vao CSDL trong. UPDATE KHONG duoc mo phong:
 * bang nao bi UPDATE sau INSERT thi khong doc tu tep sinh duoc (doc tu dump day du).
 */
public final class DocDumpSql implements NguonDuLieu {

    private final String moTa;
    private final Map<String, String[]> cotBang = new HashMap<String, String[]>();
    private final Map<String, List<String[]>> dongBang = new LinkedHashMap<String, List<String[]>>();

    private DocDumpSql(final String moTa) {
        this.moTa = moTa;
    }

    /** Doc mot (hoac nhieu, theo thu tu nap) tep .sql. */
    public static DocDumpSql tuTep(final String... duong) throws java.io.IOException {
        final StringBuilder mt = new StringBuilder("tep");
        for (final String p : duong) {
            mt.append(' ').append(p);
        }
        final DocDumpSql d = new DocDumpSql(mt.toString());
        for (final String p : duong) {
            d.nap(new String(Files.readAllBytes(new File(p).toPath()), StandardCharsets.UTF_8));
        }
        return d;
    }

    public static DocDumpSql tuChuoi(final String sql) {
        final DocDumpSql d = new DocDumpSql("chuoi SQL");
        d.nap(sql);
        return d;
    }

    @Override
    public String moTa() {
        return moTa;
    }

    /** Bang co trong tep (da CREATE hoac co INSERT) khong. */
    public boolean coBang(final String bang) {
        return cotBang.containsKey(bang.toLowerCase()) || dongBang.containsKey(bang.toLowerCase());
    }

    /** Cot cua bang theo CREATE TABLE cuoi cung trong tep (chu thuong); null neu khong co. */
    public String[] cotCua(final String bang) {
        final String[] c = cotBang.get(bang.toLowerCase());
        return c == null ? null : c.clone();
    }

    @Override
    public BangSql doc(final String bang, final String[] cot) throws Exception {
        final String k = bang.toLowerCase();
        final String[] cotTep = cotBang.get(k);
        final List<String[]> dong = dongBang.get(k);
        if (cotTep == null && dong == null) {
            throw new IllegalStateException("bang " + bang + " khong co trong " + moTa);
        }
        if (cotTep == null) {
            throw new IllegalStateException("bang " + bang + ": khong thay CREATE TABLE trong " + moTa);
        }
        final int[] vt = new int[cot.length];
        for (int i = 0; i < cot.length; i++) {
            vt[i] = -1;
            for (int j = 0; j < cotTep.length; j++) {
                if (cotTep[j].equalsIgnoreCase(cot[i])) {
                    vt[i] = j;
                }
            }
            if (vt[i] < 0) {
                throw new IllegalStateException("bang " + bang + ": thieu cot " + cot[i] + " trong " + moTa);
            }
        }
        final List<String[]> ra = new ArrayList<String[]>();
        if (dong != null) {
            for (final String[] d : dong) {
                final String[] r = new String[cot.length];
                for (int i = 0; i < cot.length; i++) {
                    r[i] = vt[i] < d.length ? d[vt[i]] : null;
                }
                ra.add(r);
            }
        }
        return new BangSql(bang, cot, ra);
    }

    // =================================================================== tach lenh

    private void nap(final String s) {
        final int n = s.length();
        final StringBuilder lenh = new StringBuilder();
        int i = 0;
        while (i < n) {
            final char c = s.charAt(i);
            if (c == '\'' || c == '"' || c == '`') {
                final int j = hetChuoi(s, i);
                lenh.append(s, i, j);
                i = j;
            } else if (c == '-' && i + 1 < n && s.charAt(i + 1) == '-' && (i + 2 >= n || Character.isWhitespace(s.charAt(i + 2)))) {
                i = hetDong(s, i);
            } else if (c == '#') {
                i = hetDong(s, i);
            } else if (c == '/' && i + 1 < n && s.charAt(i + 1) == '*') {
                final int j = s.indexOf("*/", i + 2);
                i = j < 0 ? n : j + 2;
                lenh.append(' ');
            } else if (c == ';') {
                xuLy(lenh.toString().trim());
                lenh.setLength(0);
                i++;
            } else {
                lenh.append(c);
                i++;
            }
        }
        xuLy(lenh.toString().trim());
    }

    private static int hetDong(final String s, final int i) {
        final int j = s.indexOf('\n', i);
        return j < 0 ? s.length() : j + 1;
    }

    /** Vi tri ngay sau dau dong cua chuoi bat dau o {@code i}. */
    private static int hetChuoi(final String s, final int i) {
        final char q = s.charAt(i);
        int j = i + 1;
        while (j < s.length()) {
            final char c = s.charAt(j);
            if (c == '\\' && q != '`') {
                j += 2;
                continue;
            }
            if (c == q) {
                if (j + 1 < s.length() && s.charAt(j + 1) == q) {
                    j += 2;
                    continue;
                }
                return j + 1;
            }
            j++;
        }
        throw new IllegalArgumentException("chuoi khong dong tu vi tri " + i);
    }

    private void xuLy(final String l) {
        if (l.isEmpty()) {
            return;
        }
        final String h = l.length() > 40 ? l.substring(0, 40).toUpperCase() : l.toUpperCase();
        if (h.startsWith("DROP TABLE")) {
            final String t = tenSau(l, "DROP TABLE");
            if (t != null) {
                cotBang.remove(t);
                dongBang.remove(t);
            }
        } else if (h.startsWith("CREATE TABLE")) {
            final boolean neuChuaCo = h.startsWith("CREATE TABLE IF NOT EXISTS");
            final String t = tenSau(l, neuChuaCo ? "CREATE TABLE IF NOT EXISTS" : "CREATE TABLE");
            if (t == null || (neuChuaCo && cotBang.containsKey(t))) {
                return;
            }
            final int mo = l.indexOf('(');
            final int dong = mo < 0 ? -1 : ngoacDong(l, mo);
            if (dong < 0) {
                return;
            }
            final List<String> cot = new ArrayList<String>();
            for (final String phan : tachPhay(l.substring(mo + 1, dong))) {
                final String p = phan.trim();
                if (p.startsWith("`")) {
                    cot.add(p.substring(1, p.indexOf('`', 1)).toLowerCase());
                } else if (!p.isEmpty()) {
                    final String tu = p.split("\\s+")[0];
                    if (tu.matches("[A-Za-z0-9_]+") && !tu.toUpperCase().matches(
                            "PRIMARY|KEY|UNIQUE|INDEX|CONSTRAINT|FULLTEXT|SPATIAL|FOREIGN|CHECK")) {
                        cot.add(tu.toLowerCase());
                    }
                }
            }
            cotBang.put(t, cot.toArray(new String[0]));
            dongBang.put(t, new ArrayList<String[]>());
        } else if (h.startsWith("DELETE FROM")) {
            final String t = tenSau(l, "DELETE FROM");
            if (t != null && !l.toUpperCase().contains(" WHERE ")) {
                final List<String[]> d = dongBang.get(t);
                if (d != null) {
                    d.clear();
                }
            }
        } else if (h.startsWith("INSERT ")) {
            chen(l);
        }
    }

    /** Ten bang (chu thuong) ngay sau tien to, co hoac khong co dau `. */
    private static String tenSau(final String l, final String tienTo) {
        String r = l.substring(tienTo.length()).trim();
        if (r.toUpperCase().startsWith("IF EXISTS")) {
            r = r.substring(9).trim();
        }
        if (r.isEmpty()) {
            return null;
        }
        if (r.charAt(0) == '`') {
            final int j = r.indexOf('`', 1);
            return j < 0 ? null : r.substring(1, j).toLowerCase();
        }
        int j = 0;
        while (j < r.length() && (Character.isLetterOrDigit(r.charAt(j)) || r.charAt(j) == '_' || r.charAt(j) == '.')) {
            j++;
        }
        return j == 0 ? null : r.substring(0, j).toLowerCase();
    }

    private void chen(final String l) {
        final String hoa = l.toUpperCase();
        final int into = hoa.indexOf("INTO");
        if (into < 0) {
            return;
        }
        final String t = tenSau(l.substring(into), "INTO");
        if (t == null) {
            return;
        }
        final int values = hoa.indexOf("VALUES", into);
        if (values < 0) {
            return; // INSERT ... SELECT: khong mo phong
        }
        final String dau = l.substring(into, values);
        final int moCot = dau.indexOf('(');
        String[] cotLenh = null;
        if (moCot >= 0) {
            final List<String> c = new ArrayList<String>();
            for (final String x : tachPhay(dau.substring(moCot + 1, dau.lastIndexOf(')')))) {
                c.add(x.trim().replace("`", "").toLowerCase());
            }
            cotLenh = c.toArray(new String[0]);
        }
        final String[] cotTep = cotBang.get(t);
        List<String[]> dich = dongBang.get(t);
        if (dich == null) {
            dich = new ArrayList<String[]>();
            dongBang.put(t, dich);
        }
        final int dongLenh = hoa.indexOf(" ON DUPLICATE KEY", values);
        final String than = l.substring(values + 6, dongLenh < 0 ? l.length() : dongLenh);
        for (final String[] gt : tachBo(than)) {
            if (cotLenh == null || cotTep == null) {
                dich.add(gt);
                continue;
            }
            final String[] r = new String[cotTep.length];
            for (int i = 0; i < cotLenh.length && i < gt.length; i++) {
                for (int j = 0; j < cotTep.length; j++) {
                    if (cotTep[j].equals(cotLenh[i])) {
                        r[j] = gt[i];
                    }
                }
            }
            dich.add(r);
        }
    }

    /** Vi tri dau ')' khop voi '(' o {@code mo} (bo qua ngoac trong chuoi); -1 neu khong co. */
    static int ngoacDong(final String s, final int mo) {
        int sau = 0;
        int i = mo;
        while (i < s.length()) {
            final char c = s.charAt(i);
            if (c == '\'' || c == '"' || c == '`') {
                i = hetChuoi(s, i);
                continue;
            }
            if (c == '(') {
                sau++;
            } else if (c == ')' && --sau == 0) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /** Tach theo dau phay o muc ngoai cung (bo qua phay trong ngoac / chuoi). */
    static List<String> tachPhay(final String s) {
        final List<String> ra = new ArrayList<String>();
        int sau = 0;
        int bat = 0;
        int i = 0;
        while (i < s.length()) {
            final char c = s.charAt(i);
            if (c == '\'' || c == '"' || c == '`') {
                i = hetChuoi(s, i);
                continue;
            }
            if (c == '(') {
                sau++;
            } else if (c == ')') {
                sau--;
            } else if (c == ',' && sau == 0) {
                ra.add(s.substring(bat, i));
                bat = i + 1;
            }
            i++;
        }
        ra.add(s.substring(bat));
        return ra;
    }

    /** "(1,'a'),(2,NULL)" -> [["1","a"],["2",null]]. */
    static List<String[]> tachBo(final String s) {
        final List<String[]> ra = new ArrayList<String[]>();
        int i = 0;
        final int n = s.length();
        while (i < n) {
            final char c = s.charAt(i);
            if (c != '(') {
                i++;
                continue;
            }
            i++;
            final List<String> gt = new ArrayList<String>();
            while (i < n) {
                while (i < n && Character.isWhitespace(s.charAt(i))) {
                    i++;
                }
                if (i < n && s.charAt(i) == ')') {
                    i++;
                    break;
                }
                if (s.charAt(i) == '\'' || s.charAt(i) == '"') {
                    final int j = hetChuoi(s, i);
                    gt.add(giaiChuoi(s.substring(i + 1, j - 1), s.charAt(i)));
                    i = j;
                } else {
                    int j = i;
                    int sau = 0;
                    while (j < n && !((s.charAt(j) == ',' || s.charAt(j) == ')') && sau == 0)) {
                        if (s.charAt(j) == '(') {
                            sau++;
                        } else if (s.charAt(j) == ')') {
                            sau--;
                        }
                        j++;
                    }
                    final String tho = s.substring(i, j).trim();
                    gt.add("NULL".equalsIgnoreCase(tho) ? null : tho);
                    i = j;
                }
                while (i < n && Character.isWhitespace(s.charAt(i))) {
                    i++;
                }
                if (i < n && s.charAt(i) == ',') {
                    i++;
                }
            }
            ra.add(gt.toArray(new String[0]));
        }
        return ra;
    }

    /** Bo thoat chuoi SQL (gach nguoc kieu MySQL va dau nhay doi). */
    static String giaiChuoi(final String s, final char q) {
        if (s.indexOf('\\') < 0 && s.indexOf(q) < 0) {
            return s;
        }
        final StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                final char d = s.charAt(++i);
                switch (d) {
                    case 'n':
                        b.append('\n');
                        break;
                    case 'r':
                        b.append('\r');
                        break;
                    case 't':
                        b.append('\t');
                        break;
                    case '0':
                        b.append('\0');
                        break;
                    case 'Z':
                        b.append('\032');
                        break;
                    case 'b':
                        b.append('\b');
                        break;
                    default:
                        b.append(d);
                }
            } else if (c == q && i + 1 < s.length() && s.charAt(i + 1) == q) {
                b.append(q);
                i++;
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }
}
