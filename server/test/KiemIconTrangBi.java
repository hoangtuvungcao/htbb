import com.htbb.pokemon.IconDoMoi;
import com.htbb.pokemon.IconPokemon;
import com.htbb.pokemon.IconTrangBi;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Icon trang bi dung do vat (chu game 24/10/2026: Bang do hien tui nau, Gang dam hien binh thuoc...): moi mau trang bi
 * (data_item type 0..12, 19, ten bat dau bang danh tu cua o) chi dung icon thuoc TAP DUYET cua o minh = icon mon goc
 * (IconTrangBi.GOC) + icon bo do ho / vung cung o (du_lieu/do_moi_trang_bi.csv). Ap khoi phan B doi_icon_trang_bi.sql
 * len phan A trong bo nho; icon co PNG; chay lai khong doi.
 */
public final class KiemIconTrangBi {

    private KiemIconTrangBi() {
    }

    static final String KHOI = "doi_icon_trang_bi.sql";
    static final Pattern LENH = Pattern.compile("UPDATE `data_item` SET `xstart` = (\\d+) WHERE `type` = (\\d+) AND `xstart` = (\\d+)"
            + " AND `name` LIKE '([^'%]+) %';");

    public static void chay() {
        Kiem.nhom("Icon trang bi theo o (doi_icon_trang_bi.sql, IconTrangBi)");
        final String b = DayDu.phanNangCap(KHOI);
        Kiem.dung("phan B co khoi " + KHOI, !b.isEmpty());
        // tap duyet: icon mon goc + icon do ho / vung cung o
        final Map<Integer, Set<Integer>> duyet = new HashMap<Integer, Set<Integer>>();
        for (final int t : IconTrangBi.O) {
            final Set<Integer> s = new HashSet<Integer>();
            s.add(Integer.valueOf(IconTrangBi.goc(t)));
            duyet.put(Integer.valueOf(t), s);
        }
        final Map<Integer, Set<Integer>> hoTheoO = new HashMap<Integer, Set<Integer>>();
        try {
            final List<String> dong = Files.readAllLines(Paths.get("../du_lieu/do_moi_trang_bi.csv"), StandardCharsets.UTF_8);
            final List<String> cot = java.util.Arrays.asList(dong.get(0).split(","));
            final int cType = cot.indexOf("type"), cIcon = cot.indexOf("icon");
            for (int i = 1; i < dong.size(); i++) {
                final String[] r = dong.get(i).split(",");
                final Integer t = Integer.valueOf(r[cType].trim());
                if (!hoTheoO.containsKey(t)) {
                    hoTheoO.put(t, new HashSet<Integer>());
                }
                hoTheoO.get(t).add(Integer.valueOf(r[cIcon].trim()));
                if (duyet.containsKey(t)) {
                    duyet.get(t).add(Integer.valueOf(r[cIcon].trim()));
                }
            }
        } catch (final Exception e) {
            Kiem.dung("doc du_lieu/do_moi_trang_bi.csv: " + e, false);
        }
        // icon mon goc moi (khong phai PMD cu) phai lay tu bo do cung o
        final List<String> gocLa = new ArrayList<String>();
        for (int i = 0; i < IconTrangBi.O.length; i++) {
            final int t = IconTrangBi.O[i];
            final int g = IconTrangBi.GOC[i];
            if (g != IconTrangBi.GOC_CU[i] && (hoTheoO.get(Integer.valueOf(t)) == null || !hoTheoO.get(Integer.valueOf(t)).contains(Integer.valueOf(g)))) {
                gocLa.add(t + "=" + g);
            }
        }
        Kiem.bang("icon mon goc moi lay tu bo do ho / vung cung o", "[]", gocLa.toString());
        // moi icon duyet co PNG
        final List<String> thieu = new ArrayList<String>();
        for (final Set<Integer> s : duyet.values()) {
            for (final Integer x : s) {
                if (!coPng(x.intValue())) {
                    thieu.add(String.valueOf(x));
                }
            }
        }
        Kiem.bang("moi icon duyet co PNG (map/icon_pokemon, map/icon_do_moi)", "[]", thieu.toString());
        // lenh khoi: dung bang IconTrangBi (cu -> moi, danh tu)
        final List<String[]> lenh = new ArrayList<String[]>();
        final Matcher m = LENH.matcher(b);
        while (m.find()) {
            lenh.add(new String[]{m.group(1), m.group(2), m.group(3), m.group(4)});
        }
        int doi = 0;
        final List<String> lenhSai = new ArrayList<String>();
        for (int i = 0; i < IconTrangBi.O.length; i++) {
            if (IconTrangBi.GOC[i] != IconTrangBi.GOC_CU[i]) {
                doi++;
            }
        }
        for (final String[] l : lenh) {
            final int i = IconTrangBi.viTri(Integer.parseInt(l[1]));
            if (i < 0 || IconTrangBi.GOC[i] != Integer.parseInt(l[0]) || IconTrangBi.GOC_CU[i] != Integer.parseInt(l[2])
                    || !IconTrangBi.DANH_TU[i].equals(l[3])) {
                lenhSai.add(l[1] + ":" + l[2] + "->" + l[0] + " " + l[3]);
            }
        }
        Kiem.bang("so lenh = so o doi icon (9)", Integer.valueOf(doi), Integer.valueOf(lenh.size()));
        Kiem.bang("lenh khop IconTrangBi", "[]", lenhSai.toString());
        // ap len phan A
        final List<Map<String, String>> dong = DayDu.dong("data_item");
        final Map<String, Integer> icon = new HashMap<String, Integer>();
        for (final Map<String, String> r : dong) {
            icon.put(r.get("id"), Integer.valueOf(r.get("xstart")));
        }
        final int lan1 = ap(lenh, dong, icon);
        final int lan2 = ap(lenh, dong, icon);
        Kiem.dung("lan 1 doi icon mon goc (" + lan1 + " mau)", lan1 > 400);
        Kiem.bang("chay lai: khong doi", Integer.valueOf(0), Integer.valueOf(lan2));
        final List<String> sai = new ArrayList<String>();
        int soMau = 0;
        for (final Map<String, String> r : dong) {
            final int t = Integer.parseInt(r.get("type"));
            if (!IconTrangBi.tenCuaO(t, r.get("name"))) {
                continue;
            }
            soMau++;
            final Integer x = icon.get(r.get("id"));
            if (!duyet.get(Integer.valueOf(t)).contains(x)) {
                sai.add(r.get("id") + " " + r.get("name") + " type " + t + " icon " + x);
            }
        }
        Kiem.dung("co mau trang bi de kiem (" + soMau + ")", soMau > 1000);
        Kiem.bang("moi trang bi chi dung icon thuoc tap duyet cua o", "[]", sai.size() > 10 ? sai.subList(0, 10) + " ... " + sai.size() : sai.toString());
        Kiem.bang("vi du: Gang dam so cap (150) -> gang", Integer.valueOf(15541), icon.get("150"));
        Kiem.bang("vi du: Bang do bac (63) -> bang do", Integer.valueOf(15010), icon.get("63"));
        Kiem.bang("vi du: Khan quang so cap (1) giu", Integer.valueOf(14000), icon.get("1"));
        kiemTrangPhuc(dong, icon);
    }

    static final String KHOI_TRANG_PHUC = "doi_icon_trang_phuc.sql";
    static final Pattern LENH_TP = Pattern.compile("UPDATE `data_item` SET `xstart` = (\\d+) WHERE `id` (?:= (\\d+)|IN \\(([\\d,]+)\\))"
            + " AND `type` = (\\d+) AND `xstart` = (\\d+);");
    /** Icon them moi cho trang phuc (tools/pokemon/them_icon_trang_phuc.py): map/icon_pokemon/503..526. */
    static final int MOI_DAU = 503, MOI_CUOI = 526;

    /**
     * Trang phuc / mat na / non / vu khi / skin trong o trang bi (ten KHONG bat dau bang danh tu cua o, 89 mon): khoi
     * doi_icon_trang_phuc.sql dua moi mon khoi icon goc cua o (khan do, soi day, hat giong...) sang icon PMD co PNG.
     */
    static void kiemTrangPhuc(final List<Map<String, String>> dong, final Map<String, Integer> icon) {
        final String b = DayDu.phanNangCap(KHOI_TRANG_PHUC);
        Kiem.dung("phan B co khoi " + KHOI_TRANG_PHUC, !b.isEmpty());
        final Map<String, Map<String, String>> theoId = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> r : dong) {
            theoId.put(r.get("id"), r);
        }
        // lenh: id thuoc o trang bi, icon cu = icon goc cu cua o, icon moi co PNG
        final List<int[]> lenh = new ArrayList<int[]>();
        final List<String> lenhSai = new ArrayList<String>();
        final Set<Integer> moiDung = new HashSet<Integer>();
        final Matcher m = LENH_TP.matcher(b);
        while (m.find()) {
            final int moi = Integer.parseInt(m.group(1)), t = Integer.parseInt(m.group(4)), cu = Integer.parseInt(m.group(5));
            final String ds = m.group(2) != null ? m.group(2) : m.group(3);
            final int i = IconTrangBi.viTri(t);
            if (i < 0 || IconTrangBi.GOC_CU[i] != cu || !coPng(moi) || !IconPokemon.thuocDai(moi)) {
                lenhSai.add(m.group());
            }
            moiDung.add(Integer.valueOf(moi));
            for (final String id : ds.split(",")) {
                final Map<String, String> r = theoId.get(id);
                if (r == null || !r.get("type").equals(String.valueOf(t)) || IconTrangBi.tenCuaO(t, r.get("name"))) {
                    lenhSai.add("id " + id + " khong phai trang phuc o " + t);
                }
                lenh.add(new int[]{Integer.parseInt(id), t, cu, moi});
            }
        }
        Kiem.bang("lenh trang phuc: o trang bi, icon cu = goc cu cua o, icon moi co PNG", "[]", lenhSai.toString());
        Kiem.bang("so mon trang phuc doi icon", Integer.valueOf(89), Integer.valueOf(lenh.size()));
        final int lan1 = apId(lenh, icon), lan2 = apId(lenh, icon);
        Kiem.bang("lan 1 doi 89 mon", Integer.valueOf(89), Integer.valueOf(lan1));
        Kiem.bang("chay lai: khong doi", Integer.valueOf(0), Integer.valueOf(lan2));
        // moi mon trong o trang bi ten khac danh tu (tru "Ruy Băng ..." = ruy bang) khong con icon goc cua o
        final List<String> conGoc = new ArrayList<String>();
        for (final Map<String, String> r : dong) {
            final int t = Integer.parseInt(r.get("type"));
            final int i = IconTrangBi.viTri(t);
            final String ten = r.get("name");
            if (i < 0 || IconTrangBi.tenCuaO(t, ten)
                    || ten.toLowerCase(java.util.Locale.ROOT).startsWith(IconTrangBi.DANH_TU[i].toLowerCase(java.util.Locale.ROOT) + " ")) {
                continue;
            }
            final int x = icon.get(r.get("id")).intValue();
            if (x == IconTrangBi.GOC_CU[i] || x == IconTrangBi.GOC[i] || !coPng(x)) {
                conGoc.add(r.get("id") + " " + ten + " icon " + x);
            }
        }
        Kiem.bang("trang phuc / mat na / skin khong con icon goc cua o", "[]", conGoc.toString());
        // icon them moi: lien tuc, 16x16 (vat pham RawAsset) hoac 20x20 (chan dung), co dong bang_icon.csv + ghi cong, deu duoc dung
        final List<String> moiSai = new ArrayList<String>();
        String csv = "", ghiCong = "";
        try {
            csv = new String(Files.readAllBytes(Paths.get("../du_lieu/bang_icon.csv")), StandardCharsets.UTF_8);
            ghiCong = new String(Files.readAllBytes(Paths.get(IconPokemon.THU_MUC + "GHI_CONG.txt")), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            moiSai.add("doc: " + e);
        }
        for (int n = MOI_DAU; n <= MOI_CUOI; n++) {
            final int[] kt = kichThuoc(new File(IconPokemon.THU_MUC + n + ".png"));
            if (kt == null || kt[0] != kt[1] || (kt[0] != 16 && kt[0] != 20)) {
                moiSai.add(n + " kich thuoc");
            }
            if (!csv.contains("\n" + n + "," + (IconPokemon.ID_DAU + n) + ",")) {
                moiSai.add(n + " thieu dong bang_icon.csv");
            }
            if (!ghiCong.contains("\n" + n + ".png\t")) {
                moiSai.add(n + " thieu ghi cong");
            }
            if (!moiDung.contains(Integer.valueOf(IconPokemon.ID_DAU + n))) {
                moiSai.add(n + " khong mon nao dung");
            }
        }
        Kiem.bang("icon moi 503..526: PNG 16/20, bang_icon.csv, GHI_CONG.txt, co mon dung", "[]", moiSai.toString());
        Kiem.bang("vi du: Ao Lunala (264) -> chan dung Lunala nhu o cua hang", Integer.valueOf(14211), icon.get("264"));
        Kiem.bang("vi du: Mat na Gengar (749) -> chan dung Gengar", Integer.valueOf(14519), icon.get("749"));
        Kiem.bang("vi du: Luoi liem Scyther (892) khong con hat giong", Integer.valueOf(14523), icon.get("892"));
        Kiem.bang("vi du: Skin Copperajah (950) -> chan dung Copperajah", Integer.valueOf(14090), icon.get("950"));
        Kiem.bang("vi du: Ruy Băng Hoenn (750) giu no hong", Integer.valueOf(14013), icon.get("750"));
    }

    /** Ap lenh theo id + o + icon cu. @return so dong doi. */
    static int apId(final List<int[]> lenh, final Map<String, Integer> icon) {
        int n = 0;
        for (final int[] l : lenh) {
            final String id = String.valueOf(l[0]);
            if (icon.containsKey(id) && icon.get(id).intValue() == l[2]) {
                icon.put(id, Integer.valueOf(l[3]));
                n++;
            }
        }
        return n;
    }

    /** {rong, cao} doc tu IHDR, null neu khong phai PNG. */
    static int[] kichThuoc(final File f) {
        try {
            final byte[] d = Files.readAllBytes(f.toPath());
            if (d.length < 24 || d[0] != (byte) 0x89 || d[1] != 'P' || d[2] != 'N' || d[3] != 'G') {
                return null;
            }
            final java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(d, 16, 8);
            return new int[]{bb.getInt(), bb.getInt()};
        } catch (final Exception e) {
            return null;
        }
    }

    static boolean coPng(final int x) {
        if (IconDoMoi.thuocDai(x)) {
            return new File(IconDoMoi.THU_MUC + (x - IconDoMoi.ID_DAU) + ".png").isFile();
        }
        if (IconPokemon.thuocDai(x)) {
            return new File(IconPokemon.THU_MUC + (x - IconPokemon.ID_DAU) + ".png").isFile();
        }
        return false;
    }

    /** Ap lenh: type = T AND xstart = cu AND name bat dau "danh tu " -&gt; moi. @return so dong doi. */
    static int ap(final List<String[]> lenh, final List<Map<String, String>> dong, final Map<String, Integer> icon) {
        int n = 0;
        for (final String[] l : lenh) {
            for (final Map<String, String> r : dong) {
                if (r.get("type").equals(l[1]) && icon.get(r.get("id")).intValue() == Integer.parseInt(l[2])
                        && r.get("name").startsWith(l[3] + " ")) {
                    icon.put(r.get("id"), Integer.valueOf(l[0]));
                    n++;
                }
            }
        }
        return n;
    }
}
