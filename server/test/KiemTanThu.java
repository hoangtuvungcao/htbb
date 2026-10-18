import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tan thu (nhan vat cap 1, khong GM) phai co quai de danh ngay canh lang va danh duoc.
 *
 * Bao loi 18-19/10/2026 (client Android/PC tren server that): tai khoan cap 1 "khong thay Pokemon hoang
 * nao". Ket qua dieu tra (BotChucNang / BotXuyenSuot, docs/kien-truc.md):
 *   - SERVER co quai: moi lang (0/70/80 va ban sao) noi thang toi map 1 va 2 (luoi map/map<N>,
 *     quai cap 1..5), bot khong GM di bo ra cong va thay 8-18 quai trong tam goi 7.
 *   - Client J2ME: goi 4 (vi tri actor) bi loi dich nguoc (break trong try truoc onActorMove) -> khong ve
 *     quai; da sua o client.
 *   - Nhan vat moi khong DANH duoc (chieu 0 cap 0 - mo qua nhiem vu cu so 8 khong con trong chuoi moi):
 *     CharNapDuLieu mo chieu 0 khi bat sv.lau_dai.
 * Bai nay canh ca hai phia du lieu + ma: (1) map ke lang trong Map.mapChange co luoi quai cap thap,
 * (2) luoi do co quai cap <= 3, (3) CharNapDuLieu mo chieu 0 khi bat co.
 */
public final class KiemTanThu {

    public static void chay() {
        Kiem.nhom("Tan thu: quai canh lang + danh duoc tu cap 1");
        try {
            final String map = new String(Files.readAllBytes(new File("src/real/Map.java").toPath()), StandardCharsets.UTF_8);
            final Map<Integer, Integer> cap = capQuai();
            Kiem.dung("doc duoc cap quai (du_lieu/bang_quai.csv)", cap.size() > 50);
            for (final int lang : new int[]{0, 70, 80}) {
                final Matcher m = Pattern.compile("mapChange\\.put\\(\\(short\\) " + lang + ", new short\\[\\]\\{([\\d, ]+)\\}\\)").matcher(map);
                Kiem.dung("lang " + lang + " co trong Map.mapChange", m.find());
                int coQuai = 0;
                final StringBuilder sb = new StringBuilder();
                for (final String s : m.group(1).split(",")) {
                    final int ke = Integer.parseInt(s.trim());
                    if (ke == lang) {
                        continue;
                    }
                    final int[] q = luoi(ke, cap);
                    sb.append(ke).append(":").append(q[0]).append("/min").append(q[1]).append(' ');
                    if (q[0] >= 30 && q[1] <= 3) {
                        coQuai++;
                    }
                }
                Kiem.dung("lang " + lang + ": it nhat 1 map ke co >= 30 quai cap <= 3 (" + sb.toString().trim() + ")", coQuai >= 1);
            }
            final String nap = new String(Files.readAllBytes(new File("src/real/CharNapDuLieu.java").toPath()), StandardCharsets.UTF_8);
            Kiem.dung("CharNapDuLieu mo chieu 0 khi bat sv.lau_dai (nhan vat moi danh duoc quai)",
                    nap.contains("(self.lvDetail.lv >= 3 || com.htbb.pokemon.LauDai.bat()) && self.skill[0] == 0"));
        } catch (final Exception e) {
            Kiem.dung("doc du lieu tan thu: " + e, false);
        }
    }

    /** {so quai, cap thap nhat} cua luoi map/map&lt;load&gt; (hoac map/map&lt;load&gt;). */
    static int[] luoi(final int load, final Map<Integer, Integer> cap) throws Exception {
        File f = new File("map/map" + load);
        if (!f.isFile()) {
            f = new File("map/map" + load);
        }
        if (!f.isFile()) {
            return new int[]{0, 999};
        }
        final byte[] b = Files.readAllBytes(f.toPath());
        final int w = b[0] & 255, h = b[1] & 255;
        int n = 0, min = 999;
        for (int i = 0; i < w * h && 2 + i < b.length; i++) {
            final int v = b[2 + i] & 255;
            if (v > 0 && v != 255) {
                n++;
                final Integer c = cap.get(v);
                if (c != null) {
                    min = Math.min(min, c);
                }
            }
        }
        return new int[]{n, min};
    }

    static Map<Integer, Integer> capQuai() throws Exception {
        final Map<Integer, Integer> m = new HashMap<Integer, Integer>();
        final List<String> l = Files.readAllLines(new File("../du_lieu/bang_quai.csv").toPath(), StandardCharsets.UTF_8);
        for (int i = 1; i < l.size(); i++) {
            final String[] f = l.get(i).split(",");
            try {
                m.put(Integer.parseInt(f[0].trim()), Integer.parseInt(f[2].trim()));
            } catch (final Exception ignored) {
            }
        }
        return m;
    }
}
