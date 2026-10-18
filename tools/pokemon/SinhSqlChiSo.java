import com.htbb.pokemon.ChiSoTrangBi;
import com.htbb.pokemon.NganSachTrangBi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * SINH sql/pokemon_chi_so.sql + du_lieu/chi_so_trang_bi.csv (docs/thiet-ke-game.md).
 *
 * NGUON: com.htbb.pokemon.ChiSoTrangBi (ten / don vi / mo ta 198 dong chi so) + com.htbb.pokemon.NganSachTrangBi
 * (ngan sach o / cap) + bang data_item trong sql/htbb_day_du.sql (chi lay type + level cua dong, KHONG lay chi so,
 * nen sinh lai sau khi dump da chua migration nay van ra DUNG file cu: idempotent theo quy tac, khong theo anh chup).
 * Ten cu (cot ten_cu cua CSV) doc tu ban seed sql/gamedb7_clean.sql.
 *
 * Chay o HTBB/ (can build/src hoac thu muc lop tuy y chua com.htbb.pokemon):
 *   javac -encoding UTF-8 -cp &lt;lop src&gt; -d &lt;ra&gt; tools/pokemon/SinhSqlChiSo.java
 *   java -cp &lt;ra&gt;:&lt;lop src&gt; SinhSqlChiSo           (ghi file)
 *   java -cp ... SinhSqlChiSo --kiem                      (ma thoat 1 neu file trong repo lech)
 */
public final class SinhSqlChiSo {

    static final String DUMP = "sql/htbb_day_du.sql";
    static final String SEED = "sql/gamedb7_clean.sql";
    static final String RA_SQL = "sql/pokemon_chi_so.sql";
    static final String RA_CSV = "du_lieu/chi_so_trang_bi.csv";

    public static void main(final String[] args) throws Exception {
        final boolean kiem = args.length > 0 && "--kiem".equals(args[0]);
        final String sql = sinhSql(docDataItem(doc(DUMP)));
        final String csv = sinhCsv(tenCu(doc(SEED)));
        if (kiem) {
            final boolean ok = sql.equals(doc(RA_SQL)) && csv.equals(doc(RA_CSV));
            System.out.println(ok ? "DONG BO" : "LECH: chay lai tools/pokemon/SinhSqlChiSo");
            System.exit(ok ? 0 : 1);
        }
        ghi(RA_SQL, sql);
        ghi(RA_CSV, csv);
        System.out.println("Da ghi " + RA_SQL + " (" + sql.length() + " byte) va " + RA_CSV);
    }

    // ------------------------------------------------------------------ SQL

    /** Dong data_item: {id, type, level, atb0..9}. */
    public static String sinhSql(final List<int[]> items) {
        final StringBuilder b = new StringBuilder();
        b.append("-- MO HINH CHI SO POKEMON + NGAN SACH TRANG BI (docs/thiet-ke-game.md).\n");
        b.append("-- SINH BOI tools/pokemon/SinhSqlChiSo.java tu com.htbb.pokemon.ChiSoTrangBi + NganSachTrangBi - KHONG SUA TAY.\n");
        b.append("-- IDEMPOTENT: chay lai bao nhieu lan cung ra cung ket qua. Chi THEM/DOI nghia, khong xoa dong nao.\n");
        b.append("-- Phan 1: data_attribute - ten nguoi choi doc, don vi (ispercent), mau, mo ta; xoa namein (tieng Indonesia).\n");
        b.append("--          GIU NGUYEN ID va so dong (client tra ten theo vi tri dong, test/KiemBangThuocTinh).\n");
        b.append("-- Phan 2: data_item - KEP (LEAST, chi giam, khong bao gio tang) chi so atb0..6 cua trang bi nhan vat\n");
        b.append("--          (type 0..12, 19; khong dong thoi trang atb9<>0, khong khung che tao moi atb<=1) ve tran mon\n");
        b.append("--          o +0 = chuan bac x 1,30 + 2 (NganSachTrangBi.tranMon). Theo QUY TAC (type, level) nen an toan\n");
        b.append("--          khi chay lai tren dump da chay. Do cua nguoi choi (tob_char) KHONG doi: server kep luc tinh\n");
        b.append("--          (real.ChiSoTrangBiGame.atb, chi khi sv.lau_dai=1).\n\n");
        b.append("-- ===== Phan 1: data_attribute =====\n");
        for (final ChiSoTrangBi.Dong d : ChiSoTrangBi.BANG) {
            b.append("UPDATE data_attribute SET name='").append(q(d.ten)).append("', ispercent=").append(d.phanTram)
                    .append(", idcolor=").append(d.mau).append(", decript='").append(q(d.moTa))
                    .append("', namein='' WHERE id=").append(d.id).append(";\n");
        }
        b.append("\n-- ===== Phan 2: data_item - kep ngan sach =====\n");
        final Map<String, int[]> nhom = new TreeMap<String, int[]>();
        for (final int[] it : items) {
            final int type = it[1];
            final int level = it[2];
            if (!NganSachTrangBi.coNganSach(type)) {
                continue;
            }
            nhom.put(String.format("%02d_%03d", type, Math.max(0, level)), new int[]{type, level});
        }
        for (final int[] tl : nhom.values()) {
            final StringBuilder set = new StringBuilder();
            for (int i = 0; i < NganSachTrangBi.SO_CHI_SO; i++) {
                final int t = NganSachTrangBi.tranMon(tl[0], i, tl[1], 0);
                if (set.length() > 0) {
                    set.append(", ");
                }
                set.append("atb").append(i).append("=LEAST(atb").append(i).append(',').append(t).append(')');
            }
            b.append("UPDATE data_item SET ").append(set).append(" WHERE type=").append(tl[0]).append(" AND level=")
                    .append(tl[1]).append(" AND atb9=0 AND NOT (atb0<=1 AND atb1<=1 AND atb2<=1 AND atb3<=1 AND atb4<=1 AND atb5<=1);\n");
        }
        return b.toString();
    }

    static String q(final String s) {
        return s.replace("\\", "\\\\").replace("'", "''");
    }

    // ------------------------------------------------------------------ CSV

    public static String sinhCsv(final Map<Integer, String> cu) {
        final StringBuilder b = new StringBuilder();
        b.append("id,ten,ten_cu,don_vi,trang_thai,nhom,mo_ta,ap_dung\n");
        final String[] dv = {"so", "phan_tram", "phan_nghin"};
        for (final ChiSoTrangBi.Dong d : ChiSoTrangBi.BANG) {
            b.append(d.id).append(',').append(c(d.ten)).append(',').append(c(cu.containsKey(d.id) ? cu.get(d.id) : ""))
                    .append(',').append(dv[Math.max(0, Math.min(2, d.phanTram))]).append(',').append(d.trangThai).append(',')
                    .append(d.nhom).append(',').append(c(d.moTa)).append(',').append(c(d.apDung)).append('\n');
        }
        return b.toString();
    }

    static String c(final String s) {
        if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }

    // ------------------------------------------------------------------ doc dump

    public static String doc(final String p) {
        try {
            return new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    static void ghi(final String p, final String s) throws Exception {
        new File(p).getParentFile().mkdirs();
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(p), StandardCharsets.UTF_8))) {
            w.print(s);
        }
    }

    /** Tach cac tuple cua "INSERT INTO `bang` VALUES (...),(...);" thanh danh sach truong (chuoi tho, da bo nhay). */
    public static List<List<String>> tuple(final String sql, final String bang) {
        final List<List<String>> ra = new ArrayList<List<String>>();
        final String moc = "INSERT INTO `" + bang + "`";
        int p = 0;
        while ((p = sql.indexOf(moc, p)) >= 0) {
            p += moc.length();
            final int v = sql.indexOf("VALUES", p);
            if (v < 0) {
                break;
            }
            p = v + 6;
            List<String> cur = null;
            StringBuilder f = null;
            boolean trongNhay = false;
            boolean coNhay = false;
            for (; p < sql.length(); p++) {
                final char ch = sql.charAt(p);
                if (trongNhay) {
                    if (ch == '\\' && p + 1 < sql.length()) {
                        f.append(sql.charAt(++p));
                    } else if (ch == '\'' && p + 1 < sql.length() && sql.charAt(p + 1) == '\'') {
                        f.append('\'');
                        p++;
                    } else if (ch == '\'') {
                        trongNhay = false;
                    } else {
                        f.append(ch);
                    }
                    continue;
                }
                if (cur == null) {
                    if (ch == '(') {
                        cur = new ArrayList<String>();
                        f = new StringBuilder();
                        coNhay = false;
                    } else if (ch == ';') {
                        break;
                    }
                    continue;
                }
                if (ch == '\'') {
                    trongNhay = true;
                    coNhay = true;
                } else if (ch == ',') {
                    cur.add(coNhay ? f.toString() : f.toString().trim());
                    f = new StringBuilder();
                    coNhay = false;
                } else if (ch == ')') {
                    cur.add(coNhay ? f.toString() : f.toString().trim());
                    ra.add(cur);
                    cur = null;
                } else {
                    f.append(ch);
                }
            }
        }
        return ra;
    }

    /** data_item: {id, type, level, atb0..atb9} (cot 0, 2, 6, 8..17). */
    public static List<int[]> docDataItem(final String sql) {
        final List<int[]> ra = new ArrayList<int[]>();
        for (final List<String> t : tuple(sql, "data_item")) {
            if (t.size() < 18) {
                continue;
            }
            try {
                final int[] r = new int[13];
                r[0] = Integer.parseInt(t.get(0));
                r[1] = Integer.parseInt(t.get(2));
                r[2] = Integer.parseInt(t.get(6));
                for (int i = 0; i < 10; i++) {
                    r[3 + i] = Integer.parseInt(t.get(8 + i));
                }
                ra.add(r);
            } catch (final NumberFormatException e) {
                // bo dong hong
            }
        }
        return ra;
    }

    static Map<Integer, String> tenCu(final String seed) {
        final Map<Integer, String> m = new TreeMap<Integer, String>();
        for (final List<String> t : tuple(seed, "data_attribute")) {
            try {
                m.put(Integer.parseInt(t.get(0)), t.get(1));
            } catch (final Exception e) {
                // bo
            }
        }
        return m;
    }
}
