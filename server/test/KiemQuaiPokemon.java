import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

/**
 * Moi template quai / boss server co the gui (data_monster trong sql/htbb.sql, ca 2 loai:
 * Pokemon dex > 0 qua lenh -91, va do vat dex = 0) phai ve duoc bang hinh Pokemon o CA client jar
 * va Unity, khong bao gio roi xuong duong ve kieu cu (anh -47 map/m, khung /boss/, Boss_Dragon):
 *  - dex > 0: sprite dong goi san client_jar/res/pk/<dex>[s].dl va Unity poke/<dex>[s]_dl.bytes
 *    (shiny thieu thi dung ban thuong - PokeBody.lay); ban nhe res_nhe/pk co bong.dl (loai thieu ve
 *    bong den, khong ve hinh cu);
 *  - dex = 0: nam trong VatThePokemon.QUAI cua jar va Unity, co vt_<id>.png (jar, ban nhe) / .bytes (Unity);
 *  - boss the gioi (du_lieu/noi_dung_boss_the_gioi.csv) tro vao template Pokemon;
 *  - than thu (POKE_DEX_THAN_THU, idThanThu 0..2 = Hoa Ky Lan cap 1..3) co sprite;
 *  - client khong con xin anh quai cu (-47) cho template Pokemon / do vat; moi lop boss ve
 *    paintPokemon truoc;
 *  - anh quai / boss kiem hiep cu da xoa (map/m, map/boss, res/boss) hoac thay tai cho bang anh
 *    Pokemon cung kich thuoc (map/monster, map/thanthu, res/rong - tools/pokemon/sinh_anh_quai_cu.py).
 */
public final class KiemQuaiPokemon {

    private static final String JAR = "../client_jar/res/pk/";
    private static final String NHE = "../client_jar/res_nhe/pk/";
    private static final String UNITY = "../client_pc_ios/Assets/Resources/poke/";
    private static final String JAR_SRC = "../client_jar/src/game/";
    private static final String CS = "../client_pc_ios/Assets/Scripts/Assembly-CSharp/";

    private KiemQuaiPokemon() {
    }

    public static void chay() {
        Kiem.nhom("QuaiPokemon - moi quai/boss ve bang Pokemon o jar + Unity, het anh quai cu");
        try {
            kiemTemplate();
            kiemThanThu();
            kiemMaClient();
            kiemAnhCu();
        } catch (final Exception e) {
            Kiem.dung("doc du lieu quai: " + e, false);
        }
    }

    private static String doc(final String p) throws java.io.IOException {
        return new String(Files.readAllBytes(new File(p).toPath()), StandardCharsets.UTF_8);
    }

    private static boolean co(final String p) {
        return new File(p).isFile();
    }

    private static String khoa(final int dex) {
        return String.format("%04d", Integer.valueOf(dex));
    }

    private static boolean spriteJar(final String thuMuc, final int dex, final boolean shiny) {
        return (shiny && co(thuMuc + khoa(dex) + "s.dl")) || co(thuMuc + khoa(dex) + ".dl");
    }

    private static boolean spriteUnity(final int dex, final boolean shiny) {
        return (shiny && co(UNITY + khoa(dex) + "s_dl.bytes")) || co(UNITY + khoa(dex) + "_dl.bytes");
    }

    private static Set<Integer> idVatThe(final String ma) {
        final Set<Integer> kq = new HashSet<>();
        final int a = ma.indexOf("QUAI =");
        final int b = ma.indexOf("};", a);
        final Matcher m = Pattern.compile("\\{\\s*(\\d+)\\s*,\\s*\\d+\\s*,\\s*\\d+\\s*\\}").matcher(ma.substring(a, b));
        while (m.find()) {
            kq.add(Integer.valueOf(m.group(1)));
        }
        return kq;
    }

    private static void kiemTemplate() throws java.io.IOException {
        final String db = DayDu.sql();
        final int a = db.indexOf("INSERT INTO `data_monster` VALUES");
        Kiem.dung("htbb.sql co data_monster", a >= 0);
        if (a < 0) {
            return;
        }
        final String khoi = db.substring(a, db.indexOf(";\n", a));
        // (id,'ten',...cot...,dex,shiny,he2) - dex/shiny/he2 la ba cot cuoi (sql/them_cot_pokemon.sql)
        final Matcher m = Pattern.compile("\\((\\d+),'((?:[^'\\\\]|\\\\.)*)',([^)]*)\\)").matcher(khoi);
        final Set<Integer> vtJar = idVatThe(doc(JAR_SRC + "pokemon/VatThePokemon.java"));
        final Set<Integer> vtCs = idVatThe(doc(CS + "VatThePokemon.cs"));
        final Set<Integer> pokemon = new HashSet<>();
        final List<String> loi = new ArrayList<>();
        int n = 0;
        int soPoke = 0;
        int soVat = 0;
        while (m.find()) {
            n++;
            final int id = Integer.parseInt(m.group(1));
            final String[] c = m.group(3).split(",");
            final int dex = Integer.parseInt(c[c.length - 3].trim());
            final boolean shiny = Integer.parseInt(c[c.length - 2].trim()) != 0;
            if (dex > 0) {
                soPoke++;
                pokemon.add(Integer.valueOf(id));
                if (!spriteJar(JAR, dex, shiny)) {
                    loi.add(id + " " + m.group(2) + ": jar thieu " + khoa(dex));
                }
                if (!spriteUnity(dex, shiny)) {
                    loi.add(id + " " + m.group(2) + ": Unity thieu " + khoa(dex));
                }
            } else {
                soVat++;
                final Integer k = Integer.valueOf(id);
                if (!vtJar.contains(k) || !vtCs.contains(k)) {
                    loi.add(id + " " + m.group(2) + ": dex 0 ma khong co trong VatThePokemon.QUAI (jar/Unity)");
                }
                if (!co(JAR + "vt_" + id + ".png") || !co(NHE + "vt_" + id + ".png") || !co(UNITY + "vt_" + id + ".bytes")) {
                    loi.add(id + " " + m.group(2) + ": thieu anh vt_" + id);
                }
            }
        }
        Kiem.dung("data_monster co template (" + n + ": " + soPoke + " Pokemon, " + soVat + " do vat)", n > 150);
        Kiem.bang("moi template ve duoc bang Pokemon / do vat PMD o jar + Unity", "[]", loi.toString());
        Kiem.dung("ban nhe co bong.dl (loai khong dong goi ve bong den, khong ve hinh cu)", co(NHE + "bong.dl"));
        Kiem.dung("ban day du co bong.dl", co(JAR + "bong.dl"));

        // boss the gioi: cot template_quai (nam 1) va template_nam2
        final List<String> dong = new ArrayList<>(); // bang thiet ke chi con trong CSDL day du (data_noi_dung_boss_the_gioi)
        for (final java.util.Map<String, String> r : DayDu.bangThietKe("noi_dung_boss_the_gioi.csv")) {
            if (dong.isEmpty()) {
                dong.add(String.join(",", r.keySet()));
            }
            dong.add(String.join(",", r.values()));
        }
        final List<String> dau = java.util.Arrays.asList(dong.get(0).split(","));
        final int c1 = dau.indexOf("template_quai");
        final int c2 = dau.indexOf("template_nam2");
        final List<String> bossLoi = new ArrayList<>();
        for (int i = 1; i < dong.size(); i++) {
            final String[] c = dong.get(i).split(",", -1);
            for (final int cot : new int[]{c1, c2}) {
                if (cot >= 0 && cot < c.length && !c[cot].isEmpty()
                        && !pokemon.contains(Integer.valueOf(c[cot].trim()))) {
                    bossLoi.add("tuan " + c[0] + " template " + c[cot]);
                }
            }
        }
        Kiem.dung("bang boss the gioi co cot template", c1 >= 0 && c2 >= 0);
        Kiem.bang("moi boss the gioi tro vao template Pokemon", "[]", bossLoi.toString());
    }

    private static void kiemThanThu() throws java.io.IOException {
        for (final String p : new String[]{JAR_SRC + "model/PokemonHuyenThoai.java", CS + "PokemonHuyenThoai.cs"}) {
            final String ma = doc(p);
            final Matcher m = Pattern.compile("POKE_DEX_THAN_THU\\s*=\\s*(?:new short\\[\\]\\s*)?\\{([^}]*)\\}").matcher(ma);
            Kiem.dung(p + " co POKE_DEX_THAN_THU", m.find());
            final String[] d = m.group(1).split(",");
            Kiem.dung("than thu du 3 cap (idThanThu 0..2)", d.length >= 3);
            for (final String s : d) {
                final int dex = Integer.parseInt(s.trim());
                Kiem.dung("than thu dex " + dex + " co sprite jar + Unity", spriteJar(JAR, dex, false) && spriteUnity(dex, false));
            }
        }
    }

    private static void kiemMaClient() throws java.io.IOException {
        Kiem.dung("jar: khong xin anh quai cu (-47) cho Pokemon / do vat",
                doc(JAR_SRC + "model/MonsterTemplate.java").contains("if (laPokemon(this.type) || VatThePokemon.laVatThe(this.type)) {"));
        Kiem.dung("Unity: khong xin anh quai cu (-47) cho Pokemon / do vat",
                doc(CS + "MonsterTemplate.cs").contains("if (laPokemon(type) || VatThePokemon.laVatThe(type))"));
        final String[] lop = {"BossAegislash", "Boss_Dragon", "BossDusknoir", "BossGengar", "BossGyarados", "BossRayquaza",
            "BossSalamence", "BossSerperior", "BossZapdos", "MonsterNew", "CongGym", "TruNangLuong", "Monster"};
        for (final String l : lop) {
            Kiem.dung("jar " + l + ".paint ve Pokemon truoc", doc(JAR_SRC + "model/" + l + ".java").contains("if (paintPokemon(g)) {"));
            Kiem.dung("Unity " + l + ".paint ve Pokemon truoc", doc(CS + l + ".cs").contains("paintPokemon(g)"));
        }
        Kiem.dung("jar QuaiYamask.paint ve Pokemon truoc", doc(JAR_SRC + "model/monster/QuaiYamask.java").contains("if (paintPokemon(g)) {"));
        Kiem.dung("Unity QuaiYamask.paint ve Pokemon truoc", doc(CS + "QuaiYamask.cs").contains("paintPokemon(g)"));
    }

    private static int soTep(final String thuMuc) {
        final File[] f = new File(thuMuc).listFiles();
        int n = 0;
        if (f != null) {
            for (final File x : f) {
                if (!x.getName().startsWith(".")) {
                    n++;
                }
            }
        }
        return n;
    }

    private static void kiemAnhCu() throws Exception {
        // Thu muc DataGame doc phai con (KiemAnhHieuUng) nhung khong con anh/khung quai cu nao.
        Kiem.dung("map/m con (DataGame doc)", new File("map/m").isDirectory());
        Kiem.bang("map/m: het anh quai cu (-47)", Integer.valueOf(0), Integer.valueOf(soTep("map/m")));
        Kiem.dung("map/boss con (DataGame doc)", new File("map/boss").isDirectory());
        Kiem.bang("map/boss: het khung boss cu", Integer.valueOf(0), Integer.valueOf(soTep("map/boss")));
        Kiem.sai("jar khong con res/boss", new File("../client_jar/res/boss").exists());
        Kiem.sai("Unity khong con res/boss", new File("../client_pc_ios/Assets/Resources/res/boss").exists());

        // Thay tai cho: dung kich thuoc ma loader / bang khung cu doi.
        final int[][] bienHinh = {{60, 960}, {42, 41}, {42, 41}, {55, 976}, {22, 380}, {64, 1024}, {35, 496}, {64, 1024}, {64, 1024}, {70, 2052}};
        for (int i = 0; i < bienHinh.length; i++) {
            final java.awt.image.BufferedImage im = ImageIO.read(new File("map/monster/" + i + ".png"));
            Kiem.dung("map/monster/" + i + ".png giu kich thuoc " + bienHinh[i][0] + "x" + bienHinh[i][1],
                    im != null && im.getWidth() == bienHinh[i][0] && im.getHeight() == bienHinh[i][1]);
        }
        Kiem.bang("map/monster chi con 10 anh DataGame nap (0..9)", Integer.valueOf(10), Integer.valueOf(soTep("map/monster")));
        final int[][] thanThu = {{132, 192}, {176, 182}, {195, 244}};
        for (int i = 0; i < thanThu.length; i++) {
            final java.awt.image.BufferedImage im = ImageIO.read(new File("map/thanthu/" + i + ".png"));
            Kiem.dung("map/thanthu/" + i + ".png giu kich thuoc", im != null && im.getWidth() == thanThu[i][0] && im.getHeight() == thanThu[i][1]);
            Kiem.dung("map/thanthu/" + i + " (khung, gui luc dang nhap) con", co("map/thanthu/" + i));
        }
        final String rj = "../client_jar/res/rong/";
        final String ru = "../client_pc_ios/Assets/Resources/res/rong/";
        final java.awt.image.BufferedImage big = ImageIO.read(new File(rj + "Big0.png"));
        Kiem.dung("rong/Big0.png giu kich thuoc 114x68 (dragonData)", big != null && big.getWidth() == 114 && big.getHeight() == 68);
        Kiem.dung("Unity Big0.bytes = jar Big0.png",
                java.util.Arrays.equals(Files.readAllBytes(new File(rj + "Big0.png").toPath()), Files.readAllBytes(new File(ru + "Big0.bytes").toPath())));
        Kiem.dung("Unity shadow.bytes = jar shadow.png",
                java.util.Arrays.equals(Files.readAllBytes(new File(rj + "shadow.png").toPath()), Files.readAllBytes(new File(ru + "shadow.bytes").toPath())));
        Kiem.dung("rong/dragonData con (Boss_Dragon nap luc khoi dong)", co(rj + "dragonData") && co(ru + "dragonData.bytes"));
    }
}
