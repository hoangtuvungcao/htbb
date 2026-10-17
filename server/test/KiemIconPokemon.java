import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.htbb.pokemon.IconPokemon;

/**
 * Kiem thu icon Pokemon (tools/pokemon/sinh_icon_pokemon.py -> map/icon_pokemon/, SQL, bang
 * IconPokemonBang). Moi icon nguoi choi nhin thay phai ra mot file PMD co that khi bat che do:
 *  - data_item.xstart cua CA 924 dong trong sql/htbb_day_du.sql (CSDL sau migration),
 *  - o anh thuoc cua data_potion.idImage / data_shop.idImg (client cong 5500),
 *  - o anh ngoc cua moi GemTemplate (6500), icon thu cuoi/pet (7500), vat pham nhiem vu (8000),
 *    trai cay tob_fruit.id1/id2 + 13 (8200).
 * Them: so dong SQL dung bang dau file, cac dai id khong chong nhau, ham doiAnh thuan.
 */
public final class KiemIconPokemon {

    private static int soFile;

    public static void chay() {
        Kiem.nhom("IconPokemon - icon PMD cho moi vat pham/thuoc/ngoc/thu/trai cay");
        soFile = 0;
        while (new File(IconPokemon.THU_MUC + soFile + ".png").isFile()) {
            soFile++;
        }
        Kiem.dung("co icon PMD (" + soFile + " file lien tuc)", soFile > 22);
        Kiem.dung("so icon <= SO_TOI_DA", soFile <= IconPokemon.SO_TOI_DA);
        Kiem.dung("khong co file le sau o cuoi", !new File(IconPokemon.THU_MUC + (soFile + 1) + ".png").isFile());

        // Dai id khong chong nhau (CODING_CONVENTION muc 1).
        Kiem.sai("dai 14000+ khong chong skin linh thu", IconPokemon.thuocDai(real.SkinLinhThu.ID_ANH_BAN_DO + real.SkinLinhThu.SO_SKIN - 1));
        Kiem.sai("dai 14000+ khong chong than thu 12000+", IconPokemon.thuocDai(12099));
        Kiem.dung("14999 thuoc dai", IconPokemon.thuocDai(14999));
        Kiem.dung("dai cuoi van la short", IconPokemon.ID_DAU + IconPokemon.SO_TOI_DA - 1 <= Short.MAX_VALUE);

        // Ham doiAnh thuan.
        Kiem.bang("5499 (pet) khong doi", Integer.valueOf(-1), Integer.valueOf(IconPokemon.doiAnh(5499)));
        Kiem.bang("8214 (cay trai) khong doi", Integer.valueOf(-1), Integer.valueOf(IconPokemon.doiAnh(8214)));
        Kiem.bang("8500 (co vat) khong doi", Integer.valueOf(-1), Integer.valueOf(IconPokemon.doiAnh(8500)));
        Kiem.bang("14014 khong doi", Integer.valueOf(-1), Integer.valueOf(IconPokemon.doiAnh(14014)));
        Kiem.bang("700 (icon vat pham) khong doi", Integer.valueOf(-1), Integer.valueOf(IconPokemon.doiAnh(700)));
        int ngoaiFile = 0;
        for (int id = 5500; id < 8500; id++) {
            final int n = IconPokemon.doiAnh(id);
            if (n >= soFile) {
                ngoaiFile++;
            }
        }
        Kiem.bang("moi n trong bang doi tro vao file co that", Integer.valueOf(0), Integer.valueOf(ngoaiFile));

        try {
            final String dg = doc("src/data/DataGame.java");
            Kiem.dung("sendByteImg chi doi anh khi bat che do Pokemon",
                    dg.contains("com.htbb.pokemon.CheDoPokemon.bat() ? com.htbb.pokemon.IconPokemon.doiAnh(idImg) : -1"));
            Kiem.dung("nIconItem van la 866", dg.contains("nIconItem = 866;"));

            // So dong SQL sinh ra.
            final List<String> sql = Files.readAllLines(new File("sql/pokemon_icon_vat_pham.sql").toPath(), StandardCharsets.UTF_8);
            int dong = 0;
            int khai = -1;
            final Map<String, Integer> tachThuoc = new HashMap<String, Integer>();
            for (final String d : sql) {
                final Matcher m = Pattern.compile("^-- SO_DONG: (\\d+)").matcher(d);
                if (m.find()) {
                    khai = Integer.parseInt(m.group(1));
                }
                if (d.startsWith("UPDATE ")) {
                    dong++;
                }
                final Matcher t = Pattern.compile("^UPDATE (data_potion|data_shop) SET \\w+=(\\d+) WHERE id=(\\d+);").matcher(d);
                if (t.find()) {
                    tachThuoc.put(t.group(1) + ":" + t.group(3), Integer.valueOf(t.group(2)));
                }
            }
            Kiem.bang("so dong UPDATE = SO_DONG dau file", Integer.valueOf(khai), Integer.valueOf(dong));
            Kiem.bang("so dong bang_icon.csv = so file icon", Integer.valueOf(soFile),
                    Integer.valueOf(Files.readAllLines(new File("docs/pokemon/bang_icon.csv").toPath(), StandardCharsets.UTF_8).size() - 1));

            // CSDL day du sau migration.
            final String db = doc("sql/htbb_day_du.sql");
            final List<List<String>> item = hang(db, "data_item");
            Kiem.bang("htbb_day_du co 924 data_item", Integer.valueOf(924), Integer.valueOf(item.size()));
            final List<String> loiItem = new ArrayList<String>();
            for (final List<String> r : item) {
                final int xstart = Integer.parseInt(r.get(20));
                if (!IconPokemon.thuocDai(xstart) || xstart - IconPokemon.ID_DAU >= soFile) {
                    loiItem.add(r.get(0) + "=" + xstart);
                }
            }
            Kiem.bang("moi data_item.xstart la icon PMD co file", "[]", loiItem.toString());

            final Map<Integer, Set<String>> dungO = new HashMap<Integer, Set<String>>();
            final List<String> loiThuoc = new ArrayList<String>();
            for (final String[] bang : new String[][]{{"data_potion", "1"}, {"data_shop", "1"}}) {
                for (final List<String> r : hang(db, bang[0])) {
                    final int k = Integer.parseInt(r.get(Integer.parseInt(bang[1])));
                    if (!coIcon(5500 + k)) {
                        loiThuoc.add(bang[0] + ":" + r.get(0) + "->" + k);
                    }
                    Set<String> s = dungO.get(Integer.valueOf(k));
                    if (s == null) {
                        s = new HashSet<String>();
                        dungO.put(Integer.valueOf(k), s);
                    }
                    s.add(bang[0] + ":" + r.get(0));
                }
            }
            Kiem.bang("moi o anh thuoc (data_potion + data_shop) co icon PMD", "[]", loiThuoc.toString());
            // O tach phai la o rieng: chi cac dong duoc tach dung no.
            final List<String> loiTach = new ArrayList<String>();
            for (final Map.Entry<String, Integer> e : tachThuoc.entrySet()) {
                for (final String ai : dungO.get(e.getValue())) {
                    if (!tachThuoc.containsKey(ai) || !tachThuoc.get(ai).equals(e.getValue())) {
                        loiTach.add(e.getValue() + " dung chung voi " + ai);
                    }
                }
            }
            Kiem.bang("o anh thuoc tach rieng khong ai khac dung", "[]", loiTach.toString());

            final List<String> loiTrai = new ArrayList<String>();
            for (final List<String> r : hang(db, "tob_fruit")) {
                for (final String cot : new String[]{"id1", "id2"}) {
                    final int k = Integer.parseInt(r.get(cotFruit(db, cot)));
                    if (!coIcon(8200 + k)) {
                        loiTrai.add(r.get(0) + "." + cot + "=" + k);
                    }
                }
            }
            Kiem.dung("trai heo (13) co icon", coIcon(8213));
            Kiem.bang("moi trai cay tob_fruit co icon PMD", "[]", loiTrai.toString());
        } catch (final Exception e) {
            Kiem.dung("doc SQL/nguon: " + e, false);
        }

        // Ngoc: moi GemTemplate (doc nguon, khong nap Map vi khoi static goi CSDL).
        try {
            final Matcher m = Pattern.compile("new GemTemplate\\(\\d+,\\s*(\\d+),").matcher(doc("src/real/MapNapTaiNguyen.java"));
            final List<String> loi = new ArrayList<String>();
            int dem = 0;
            while (m.find()) {
                dem++;
                if (!coIcon(6500 + Integer.parseInt(m.group(1)))) {
                    loi.add(m.group(1));
                }
            }
            Kiem.dung("doc duoc GemTemplate (" + dem + ")", dem > 100);
            Kiem.bang("moi anh ngoc co icon PMD", "[]", loi.toString());
        } catch (final Exception e) {
            Kiem.dung("doc MapNapTaiNguyen: " + e, false);
        }

        final List<String> loiThu = new ArrayList<String>();
        for (final byte k : data.Animal.idImgIcon) {
            if (!coIcon(7500 + k)) {
                loiThu.add(String.valueOf(k));
            }
        }
        for (int k = 30; k <= 47; k++) { // Pet.getIdIconPaint (35..44) + PetTemplate moi (30..34, 46, 47)
            if (k != 45 && !coIcon(7500 + k)) {
                loiThu.add("pet " + k);
            }
        }
        Kiem.bang("icon thu cuoi + pet co icon PMD", "[]", loiThu.toString());
        final List<String> loiNv = new ArrayList<String>();
        for (final byte k : data.ItemQuest.ICON_IMAGE) {
            if (!coIcon(8000 + k)) {
                loiNv.add(String.valueOf(k));
            }
        }
        Kiem.bang("vat pham nhiem vu co icon PMD", "[]", loiNv.toString());
    }

    private static boolean coIcon(final int idImg) {
        final int n = IconPokemon.doiAnh(idImg);
        return n >= 0 && n < soFile;
    }

    private static String doc(final String f) throws Exception {
        return new String(Files.readAllBytes(new File(f).toPath()), StandardCharsets.UTF_8);
    }

    /** Vi tri cot trong CREATE TABLE `tob_fruit`. */
    private static int cotFruit(final String db, final String cot) {
        final int a = db.indexOf("CREATE TABLE `tob_fruit`");
        final String than = db.substring(a, db.indexOf(") ENGINE", a));
        int i = -1;
        for (final String d : than.split("\n")) {
            final String t = d.trim();
            if (t.startsWith("`")) {
                i++;
                if (t.startsWith("`" + cot + "`")) {
                    return i;
                }
            }
        }
        throw new IllegalStateException("khong co cot " + cot);
    }

    /** Cac dong cua INSERT INTO `bang` VALUES (...),(...); kieu mysqldump. */
    private static List<List<String>> hang(final String db, final String bang) {
        final List<List<String>> ra = new ArrayList<List<String>>();
        final String dau = "INSERT INTO `" + bang + "` VALUES ";
        int p = db.indexOf(dau);
        while (p >= 0) {
            int i = p + dau.length();
            List<String> r = null;
            StringBuilder o = new StringBuilder();
            boolean chuoi = false;
            for (; i < db.length(); i++) {
                final char c = db.charAt(i);
                if (chuoi) {
                    if (c == '\\') {
                        o.append(db.charAt(++i));
                    } else if (c == '\'') {
                        chuoi = false;
                    } else {
                        o.append(c);
                    }
                } else if (c == '\'') {
                    chuoi = true;
                } else if (c == '(') {
                    r = new ArrayList<String>();
                    o = new StringBuilder();
                } else if (c == ',' && r != null) {
                    r.add(o.toString());
                    o = new StringBuilder();
                } else if (c == ')') {
                    r.add(o.toString());
                    ra.add(r);
                    r = null;
                } else if (c == ';' && r == null) {
                    break;
                } else if (r != null) {
                    o.append(c);
                }
            }
            p = db.indexOf(dau, i);
        }
        return ra;
    }
}
