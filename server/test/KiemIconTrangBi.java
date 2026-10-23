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
