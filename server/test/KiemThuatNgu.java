import com.htbb.pokemon.ChuPokemon;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thuat ngu chuan (docs/thiet-ke-game.md): KHONG con thuat ngu kiem hiep cu (ChuPokemon.TU_CAM) trong
 * chu nguoi choi / admin thay - quet THO (truoc tu dien luc chay), phai 0.
 *
 * VI SAO: tu 18/10/2026 ma nguon da mang thuat ngu moi (tools/pokemon/ap_thuat_ngu.py, ap_thuat_ngu_web.py; du lieu
 * trong CSDL da doi san). Tu dien luc chay chi con la luoi an toan; chuoi moi viet them ma dung tu cu
 * (vd "chiếm thành", "phi phong", "bang hội") se do o day thay vi lot ra nguoi choi / trang admin.
 *
 * Quet:
 *   - chuoi "..." trong src/**.java (bo chu thich, dong log, bang tu dien ChuPokemon/TenMapPokemon);
 *   - client: J2ME, Android (.java), PC/iOS Unity client_pc_ios/Assets (.cs);
 *   - web canh HTBB/ (../HTBB-Web: .vue/.ts/.js/.mjs/.sql) (web PHP cu web/ da bo - docs/kien-truc.md);
 *   - chuoi '...' trong sql/htbb_day_du.sql va sql/nang_cap.sql (du lieu game).
 * Khong co thu muc client/web (vd may chu chi co repo game) thi bo qua phan do, in ra.
 * "bang" trong chu KHONG dau (bảng/băng/khoa 'bang') khong tinh - xem docs/thiet-ke-game.md.
 */
public final class KiemThuatNgu {

    private static final Set<String> BO_QUA_TEN = new HashSet<>(Arrays.asList(
            "ChuPokemon.java", "ChuPokemon.cs", "ChuPokemonDuLieu.cs", "TenMapPokemon.java", "TenMapPokemon.cs"));
    private static final Pattern LOG = Pattern.compile(
            "System\\.(out|err)|printStackTrace|saveOrtherLog|saveLogThongKe|Debug\\.Log|Logdata|LOGGER|logger\\.|Log\\.[dewiv]\\(");
    private static final Set<String> BO_THU_MUC = new HashSet<>(Arrays.asList(
            "build", "out", "bin", "obj", "node_modules", ".nuxt", ".output", "dist", ".data", ".git"));

    private static final List<String> LOI = new ArrayList<>();
    private static int soChuoi;

    public static void chay() {
        Kiem.nhom("Thuat ngu chuan (docs/thiet-ke-game.md): khong con tu kiem hiep THO trong chu nguoi choi/admin");
        Kiem.dung("bo bat duoc tu cu (tu kiem tra)", !conTu("Giờ chiếm thành").isEmpty()
                && !conTu("Phi phong lông thú").isEmpty() && !conTu("Bang hội của bạn").isEmpty()
                && !conTu("Thành liên đấu 1").isEmpty() && !conTu("cưỡi linh thú").isEmpty());
        Kiem.dung("ngoai le khong bi bat (thành phố, tìm kiếm, Múa kiếm, BANG DIEU KHIEN)",
                conTu("Hoàn thành nhiệm vụ ở thành phố, tìm kiếm Múa kiếm").isEmpty()
                        && conTu("BANG DIEU KHIEN - so lieu").isEmpty());

        quet("src", "server Java", ".java", false);
        quet("../client_jar/src", "client J2ME", ".java", true);
        quet("../client_jar/shared/mod", "client J2ME (mod dung chung)", ".java", true);
        quet("../client_apk/pokemon/src", "client Android", ".java", true);
        quet("../client_pc/src", "client PC Java", ".java", true);
        quet("../client_pc_ios/Assets/Scripts", "client PC", ".cs", true);
        for (final String g : new String[]{"app", "server", "shared", "scripts"}) {
            quet("../web/" + g, "web/" + g, "web", true);
        }
        quetSql();

        for (final String l : LOI) {
            System.out.println("        " + l);
        }
        Kiem.dung("da quet duoc chuoi (" + soChuoi + ")", soChuoi > 1000);
        Kiem.bang("tu cu con lai trong chu nguoi choi/admin thay (THO, khong qua tu dien)",
                Integer.valueOf(0), Integer.valueOf(LOI.size()));
    }

    /** Dau hieu o dau tep cua lop dich nguoc tu lib/htbb-core.jar (P3, xem docs/giay-phep.md). */
    static final String DAU_PHUC_HOI = "PHUC HOI TU LOI CU";

    /**
     * Lop phuc hoi tu jar loi cu: chuoi trong do giu NGUYEN nhu bytecode cu (P3 khong doi hanh vi,
     * ChuPokemon.doi van dich luc chay nhu truoc). Lam sach chuoi thuoc P4/P5, nen cac phep quet chu
     * hien cho nguoi choi bo qua chung. Chi xet 10 dong dau: mot chuoi trong than lop khong lot qua.
     */
    static boolean laLopPhucHoi(final List<String> dong) {
        for (int i = 0; i < Math.min(10, dong.size()); i++) {
            if (dong.get(i).contains(DAU_PHUC_HOI)) {
                return true;
            }
        }
        return false;
    }

    /** Tu cam con trong chuoi (bo "bang" cua chu khong dau). */
    static List<String> conTu(final String s0) {
        final String s = Normalizer.normalize(s0, Normalizer.Form.NFC);
        final List<String> ra = new ArrayList<>(ChuPokemon.tuCamTrong(s));
        if (!s.matches("(?s).*[^\\x00-\\x7f].*") && !"Bang".equals(s.trim()) && !"BANG".equals(s.trim())) {
            ra.removeIf(g -> g.equalsIgnoreCase("bang"));
        }
        return ra;
    }

    private static void xet(final String tep, final int dong, final String chu) {
        soChuoi++;
        final List<String> t = conTu(chu);
        if (!t.isEmpty()) {
            LOI.add(tep + ":" + dong + " " + t + " " + chu.replace("\n", "|").substring(0, Math.min(140, chu.length())));
        }
    }

    private static void quet(final String goc, final String ten, final String duoi, final boolean tuyChon) {
        final File g = new File(goc);
        if (!g.isDirectory()) {
            Kiem.dung("co thu muc " + goc, tuyChon);
            System.out.println("   (bo qua " + ten + ": khong co " + goc + ")");
            return;
        }
        final List<File> ds = new ArrayList<>();
        liet(g, ds);
        for (final File f : ds) {
            final String n = f.getName();
            if (BO_QUA_TEN.contains(n)) {
                continue;
            }
            final boolean web = "web".equals(duoi);
            if (web ? !(n.endsWith(".vue") || n.endsWith(".ts") || n.endsWith(".js") || n.endsWith(".mjs")
                    || n.endsWith(".sql")) : !n.endsWith(duoi)) {
                continue;
            }
            final String s = doc(f);
            final String rel = f.getPath();
            // Lop phuc hoi tu lib/htbb-core.jar (P3) giu nguyen chuoi cu - xem laLopPhucHoi.
            if (n.endsWith(".java") && laLopPhucHoi(Arrays.asList(s.split("\n", 11)))) {
                continue;
            }
            if (n.endsWith(".java") || n.endsWith(".cs")) {
                chuoiJava(rel, s);
            } else if (n.endsWith(".sql")) {
                chuoiSql(rel, s, false);
            } else if (n.endsWith(".vue")) {
                final Matcher m = Pattern.compile("<script\\b[^>]*>(.*?)</script>", Pattern.DOTALL).matcher(s);
                while (m.find()) {
                    js(rel, s, m.start(1), m.end(1));
                }
                final Matcher t = Pattern.compile("(?ms)^<template\\b[^>]*>(.*)^</template>").matcher(s);
                if (t.find()) {
                    template(rel, s, t.start(1), t.end(1));
                }
            } else if (n.endsWith(".php")) {
                int i = 0;
                while (i < s.length()) {
                    final int j = s.indexOf("<?php", i);
                    if (j < 0) {
                        template(rel, s, i, s.length());
                        break;
                    }
                    template(rel, s, i, j);
                    int k = s.indexOf("?>", j);
                    k = k < 0 ? s.length() : k;
                    js(rel, s, j + 5, k);
                    i = k + 2;
                }
            } else {
                js(rel, s, 0, s.length());
            }
        }
    }

    private static void liet(final File d, final List<File> ra) {
        final File[] con = d.listFiles();
        if (con == null) {
            return;
        }
        Arrays.sort(con);
        for (final File f : con) {
            if (f.isDirectory()) {
                if (!BO_THU_MUC.contains(f.getName())) {
                    liet(f, ra);
                }
            } else {
                ra.add(f);
            }
        }
    }

    private static String doc(final File f) {
        try {
            final String s = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            // Ma .java hay viet chu Viet bang escape unicode: giai truoc khi quet, khong thi lot tu cam.
            return f.getName().endsWith(".java") ? giaiEscape(s) : s;
        } catch (final Exception e) {
            return "";
        }
    }

    /** Giai escape unicode kieu Java (dau gach nguoc + u + 4 so hex). */
    static String giaiEscape(final String s) {
        final StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == '\\' && i + 5 < s.length() && s.charAt(i + 1) == 'u') {
                try {
                    b.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16));
                    i += 5;
                    continue;
                } catch (final NumberFormatException e) {
                    // khong phai escape
                }
            }
            b.append(c);
        }
        return b.toString();
    }

    private static int dong(final String s, final int i) {
        int n = 1;
        for (int k = 0; k < i && k < s.length(); k++) {
            if (s.charAt(k) == '\n') {
                n++;
            }
        }
        return n;
    }

    private static String boThoat(final String s) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                final char n = s.charAt(++i);
                sb.append(n == 'n' ? '\n' : n == 't' ? '\t' : n == 'r' ? '\r' : n);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean giongChu(final String t) {
        if (t.isEmpty()) {
            return false;
        }
        for (int i = 0; i < t.length(); i++) {
            if (t.charAt(i) > 127) {
                return true;
            }
        }
        return Character.isUpperCase(t.charAt(0));
    }

    /** Chuoi "..." trong Java/C# (bo chu thich, dong log). */
    private static void chuoiJava(final String tep, final String s0) {
        final String s = KiemChuPokemon.boChuThich(s0);
        final Pattern lit = Pattern.compile("\"((?:[^\"\\\\\\n]|\\\\.)*)\"");
        int so = 0;
        for (final String d : s.split("\n", -1)) {
            so++;
            if (LOG.matcher(d).find()) {
                continue;
            }
            final Matcher m = lit.matcher(d);
            while (m.find()) {
                final String t = boThoat(m.group(1));
                if (giongChu(t)) {
                    xet(tep, so, t);
                }
            }
        }
    }

    /** Chuoi '...' trong SQL (bo -- chu thich). chiSet: chi xet gia tri SET (pokemon_thuat_ngu.sql). */
    private static void chuoiSql(final String tep, final String s, final boolean chiSet) {
        int i = 0;
        final int n = s.length();
        while (i < n) {
            if (s.startsWith("--", i) && (i == 0 || s.charAt(i - 1) == '\n' || s.charAt(i - 1) == ' ')) {
                final int j = s.indexOf('\n', i);
                i = j < 0 ? n : j;
                continue;
            }
            if (s.charAt(i) == '\'') {
                int j = i + 1;
                final StringBuilder sb = new StringBuilder();
                while (j < n) {
                    final char c = s.charAt(j);
                    if (c == '\\' && j + 1 < n) {
                        final char x = s.charAt(j + 1);
                        sb.append(x == 'n' ? '\n' : x == 't' ? '\t' : x == 'r' ? '\r' : x);
                        j += 2;
                        continue;
                    }
                    if (c == '\'') {
                        if (j + 1 < n && s.charAt(j + 1) == '\'') {
                            sb.append('\'');
                            j += 2;
                            continue;
                        }
                        break;
                    }
                    sb.append(c);
                    j++;
                }
                boolean xetDuoc = true;
                if (chiSet) {
                    final int dau = s.lastIndexOf('\n', i) + 1;
                    final String truoc = s.substring(dau, i);
                    xetDuoc = truoc.startsWith("UPDATE ") && !truoc.contains(" WHERE ");
                }
                if (xetDuoc) {
                    xet(tep, dong(s, i), sb.toString());
                }
                i = j + 1;
                continue;
            }
            i++;
        }
    }

    private static void quetSql() {
        // Du lieu game nam trong MOT tep day du (va tep nang cap) - quet chu nguoi choi thay o hai tep do.
        int n = 0;
        for (final File f : new File[]{new File("../sql/htbb_day_du.sql"), new File("../sql/nang_cap.sql")}) {
            if (f.isFile()) {
                n++;
                chuoiSql(f.getPath(), doc(f), false);
            }
        }
        Kiem.bang("co sql/htbb_day_du.sql va sql/nang_cap.sql", 2, n);
    }

    // ---------------- web (ban Java don gian cua tools/pokemon/ap_thuat_ngu_web.py) ----------------

    private static void js(final String tep, final String s, int i, final int n) {
        while (i < n) {
            final char c = s.charAt(i);
            if (s.startsWith("//", i)) {
                final int j = s.indexOf('\n', i);
                i = j < 0 || j > n ? n : j;
                continue;
            }
            if (s.startsWith("/*", i)) {
                final int j = s.indexOf("*/", i + 2);
                i = j < 0 ? n : j + 2;
                continue;
            }
            if (c == '\'' || c == '"') {
                int j = i + 1;
                while (j < n && s.charAt(j) != c && s.charAt(j) != '\n') {
                    j += s.charAt(j) == '\\' ? 2 : 1;
                }
                xet(tep, dong(s, i), boThoat(s.substring(i + 1, Math.min(j, n))));
                i = j + 1;
                continue;
            }
            if (c == '`') {
                i = mau(tep, s, i + 1, n);
                continue;
            }
            if (c == '/' && coTheRegex(s, i)) {
                int j = i + 1;
                boolean lop = false;
                while (j < n && s.charAt(j) != '\n') {
                    final char x = s.charAt(j);
                    if (x == '\\') {
                        j += 2;
                        continue;
                    }
                    if (x == '[') {
                        lop = true;
                    } else if (x == ']') {
                        lop = false;
                    } else if (x == '/' && !lop) {
                        break;
                    }
                    j++;
                }
                i = j + 1;
                continue;
            }
            i++;
        }
    }

    private static boolean coTheRegex(final String s, final int i) {
        int k = i - 1;
        while (k >= 0 && (s.charAt(k) == ' ' || s.charAt(k) == '\t')) {
            k--;
        }
        return k < 0 || "(,=:[!&|?{};\n".indexOf(s.charAt(k)) >= 0 || s.substring(Math.max(0, k - 5), k + 1).endsWith("return");
    }

    private static int mau(final String tep, final String s, int i, final int n) {
        int dau = i;
        while (i < n) {
            final char c = s.charAt(i);
            if (c == '\\') {
                i += 2;
                continue;
            }
            if (c == '`') {
                xet(tep, dong(s, dau), boThoat(s.substring(dau, i)));
                return i + 1;
            }
            if (s.startsWith("${", i)) {
                xet(tep, dong(s, dau), boThoat(s.substring(dau, i)));
                int sau = 1;
                int j = i + 2;
                final int k0 = j;
                while (j < n && sau > 0) {
                    final char x = s.charAt(j);
                    if (x == '{') {
                        sau++;
                    } else if (x == '}') {
                        sau--;
                    } else if (x == '\'' || x == '"' || x == '`') {
                        j++;
                        while (j < n && s.charAt(j) != x) {
                            j += s.charAt(j) == '\\' ? 2 : 1;
                        }
                    }
                    j++;
                }
                js(tep, s, k0, j - 1);
                i = j;
                dau = i;
                continue;
            }
            i++;
        }
        return i;
    }

    private static void template(final String tep, final String s, int i, final int n) {
        final Pattern scriptStyle = Pattern.compile("(?i)<(script|style)\\b[^>]*>");
        final Pattern tenThuocTinh = Pattern.compile("([:@#\\w.-]+)\\s*=\\s*$");
        while (i < n) {
            if (s.startsWith("<!--", i)) {
                final int j = s.indexOf("-->", i);
                i = j < 0 ? n : j + 3;
                continue;
            }
            final Matcher ss = scriptStyle.matcher(s).region(i, Math.min(n, i + 200));
            if (ss.lookingAt()) {
                int e = s.toLowerCase().indexOf("</" + ss.group(1).toLowerCase() + ">", i);
                e = e < 0 || e > n ? n : e;
                if ("script".equalsIgnoreCase(ss.group(1))) {
                    js(tep, s, ss.end(), e);
                }
                i = e + ss.group(1).length() + 3;
                continue;
            }
            if (s.charAt(i) == '<') {
                int j = i + 1;
                while (j < n && s.charAt(j) != '>') {
                    final char q = s.charAt(j);
                    if (q == '"' || q == '\'') {
                        int k = s.indexOf(q, j + 1);
                        k = k < 0 ? n : k;
                        final Matcher m = tenThuocTinh.matcher(s.substring(i, j));
                        final String ten = m.find() ? m.group(1) : "";
                        if (ten.startsWith(":") || ten.startsWith("@") || ten.startsWith("v-") || ten.startsWith("#")) {
                            js(tep, s, j + 1, k);
                        } else {
                            xet(tep, dong(s, j), s.substring(j + 1, k));
                        }
                        j = k + 1;
                        continue;
                    }
                    j++;
                }
                i = j + 1;
                continue;
            }
            int j = s.indexOf('<', i);
            j = j < 0 || j > n ? n : j;
            int k = i;
            while (k < j) {
                final int m = s.indexOf("{{", k);
                if (m < 0 || m >= j) {
                    xet(tep, dong(s, k), s.substring(k, j));
                    break;
                }
                xet(tep, dong(s, k), s.substring(k, m));
                int e = s.indexOf("}}", m + 2);
                e = e < 0 || e > j ? j : e;
                js(tep, s, m + 2, e);
                k = e + 2;
            }
            i = j;
        }
    }

    private KiemThuatNgu() {
    }
}
