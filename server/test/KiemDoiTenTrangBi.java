import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * De xuat QA #8 (23/10/2026, chu game duyet): 110 nhom trang bi (data_item type 0..11) trung ten chi khac cap -> ten
 * "&lt;Ten&gt; · Cap N" (N = data_item.level = cap yeu cau tren the vat pham). Khoi phan B doi_ten_trang_bi_cap.sql ap len
 * phan A trong bo nho: phu du moi nhom, ten moi dung mau, chay lai khong doi (dieu kien ten cu), giu ten admin da sua,
 * con trung ten chi khi cung cap (bien the hinh / mau), dong bo cot ten cua bang noi dung.
 */
public final class KiemDoiTenTrangBi {

    private KiemDoiTenTrangBi() {
    }

    static final String KHOI = "doi_ten_trang_bi_cap.sql";
    static final Pattern LENH = Pattern.compile("UPDATE `data_item` SET `name` = CONCAT\\(`name`, ' · Cấp ', `level`\\) WHERE `name` = '([^']+)'"
            + " AND `id` IN \\(([0-9,]+)\\);");

    public static void chay() {
        Kiem.nhom("Ten trang bi theo cap (doi_ten_trang_bi_cap.sql)");
        final String b = DayDu.phanNangCap(KHOI);
        Kiem.dung("phan B co khoi " + KHOI, !b.isEmpty());
        // phan A: id -> {ten, type, level}
        final Map<Integer, String[]> a = new LinkedHashMap<Integer, String[]>();
        for (final Map<String, String> r : DayDu.dong("data_item")) {
            a.put(Integer.valueOf(r.get("id")), new String[]{r.get("name"), r.get("type"), r.get("level")});
        }
        Kiem.dung("doc duoc data_item phan A (" + a.size() + ")", a.size() > 1000);
        // nhom trung ten type <= 11, level > 0 (840 = mau rong level 0, bo qua)
        final Map<String, List<Integer>> nhom = new TreeMap<String, List<Integer>>();
        final Map<String, Integer> dem = new HashMap<String, Integer>();
        for (final Map.Entry<Integer, String[]> e : a.entrySet()) {
            if (Integer.parseInt(e.getValue()[1]) <= 11) {
                final String t = e.getValue()[0];
                dem.put(t, Integer.valueOf(dem.containsKey(t) ? dem.get(t).intValue() + 1 : 1));
            }
        }
        for (final Map.Entry<Integer, String[]> e : a.entrySet()) {
            final String[] v = e.getValue();
            if (Integer.parseInt(v[1]) <= 11 && dem.get(v[0]).intValue() > 1 && Integer.parseInt(v[2]) > 0) {
                if (!nhom.containsKey(v[0])) {
                    nhom.put(v[0], new ArrayList<Integer>());
                }
                nhom.get(v[0]).add(e.getKey());
            }
        }
        Kiem.bang("110 nhom trung ten", Integer.valueOf(110), Integer.valueOf(nhom.size()));
        // lenh trong khoi
        final Map<String, List<Integer>> lenh = new TreeMap<String, List<Integer>>();
        final Matcher m = LENH.matcher(b);
        while (m.find()) {
            final List<Integer> ids = new ArrayList<Integer>();
            for (final String s : m.group(2).split(",")) {
                ids.add(Integer.valueOf(s));
            }
            java.util.Collections.sort(ids);
            lenh.put(m.group(1), ids);
        }
        final Map<String, List<Integer>> mong = new TreeMap<String, List<Integer>>();
        for (final Map.Entry<String, List<Integer>> e : nhom.entrySet()) {
            final List<Integer> ids = new ArrayList<Integer>(e.getValue());
            java.util.Collections.sort(ids);
            mong.put(e.getKey(), ids);
        }
        Kiem.bang("moi nhom mot lenh, du id (ten cu + danh sach id)", mong.toString(), lenh.toString());
        Kiem.bang("so lenh data_item trong khoi = 110", Integer.valueOf(110), Integer.valueOf(b.split("UPDATE `data_item`", -1).length - 1));
        // ap len phan A trong bo nho
        final Map<Integer, String> sau = new HashMap<Integer, String>();
        for (final Map.Entry<Integer, String[]> e : a.entrySet()) {
            sau.put(e.getKey(), e.getValue()[0]);
        }
        final int lan1 = ap(lenh, a, sau);
        final int lan2 = ap(lenh, a, sau);
        Kiem.bang("lan 1 doi 558 dong", Integer.valueOf(558), Integer.valueOf(lan1));
        Kiem.bang("chay lai: khong doi dong nao", Integer.valueOf(0), Integer.valueOf(lan2));
        // ten admin da sua: khong khop ten cu -> giu nguyen
        final Map<Integer, String> sua = new HashMap<Integer, String>();
        for (final Map.Entry<Integer, String[]> e : a.entrySet()) {
            sua.put(e.getKey(), e.getValue()[0]);
        }
        sua.put(Integer.valueOf(63), "Băng đô bạc của admin");
        ap(lenh, a, sua);
        Kiem.bang("ten admin da sua (63) giu nguyen", "Băng đô bạc của admin", sua.get(Integer.valueOf(63)));
        Kiem.bang("cung nhom van doi (316)", "Băng đô bạc · Cấp 20", sua.get(Integer.valueOf(316)));
        // sau doi: con trung ten chi khi cung cap; ten dung mau, vua 35 ky tu
        final Map<String, java.util.Set<String>> capTheoTen = new HashMap<String, java.util.Set<String>>();
        final List<String> sai = new ArrayList<String>();
        int dai = 0;
        for (final Map.Entry<Integer, String[]> e : a.entrySet()) {
            final String[] v = e.getValue();
            if (Integer.parseInt(v[1]) > 11) {
                continue;
            }
            final String t = sau.get(e.getKey());
            if (!capTheoTen.containsKey(t)) {
                capTheoTen.put(t, new java.util.HashSet<String>());
            }
            capTheoTen.get(t).add(v[2]);
            if (!t.equals(v[0])) {
                dai = Math.max(dai, t.length());
                if (!t.equals(v[0] + " · Cấp " + v[2])) {
                    sai.add(e.getKey() + " " + t);
                }
            }
        }
        Kiem.bang("ten moi = ten cu + ' · Cấp ' + level", "[]", sai.toString());
        Kiem.dung("ten moi dai nhat <= 35 ky tu (" + dai + ")", dai > 0 && dai <= 35);
        final List<String> conTrung = new ArrayList<String>();
        for (final Map.Entry<String, java.util.Set<String>> e : capTheoTen.entrySet()) {
            if (e.getValue().size() > 1) {
                conTrung.add(e.getKey() + " " + e.getValue());
            }
        }
        Kiem.bang("khong con hai mon cung ten khac cap", "[]", conTrung.toString());
        Kiem.bang("840 (mau rong level 0) khong doi", "Khăn quàng sơ cấp", sau.get(Integer.valueOf(840)));
        Kiem.bang("vi du 664", "Găng đấm huyền thoại · Cấp 85", sau.get(Integer.valueOf(664)));
        // dong bo bang noi dung (theo id, chi dong con ten cu)
        for (final String bang : new String[]{"data_noi_dung_cua_hang", "data_noi_dung_roi", "data_noi_dung_che_tao", "data_noi_dung_trang_bi"}) {
            Kiem.dung("dong bo ten " + bang + " theo ten cu + cap", b.contains("UPDATE `" + bang + "` c JOIN `data_item` i ON ")
                    && b.indexOf("i.`name` = CONCAT(c.`ten`, ' · Cấp ', i.`level`)", b.indexOf("UPDATE `" + bang + "`")) > 0);
        }
        Kiem.dung("phan A data_item khong doi (chu thuong nhiem vu luu san khop ten phan A)", a.get(Integer.valueOf(63))[0].equals("Băng đô bạc"));
    }

    /** Ap cac lenh: WHERE name = ten cu AND id IN (...) -&gt; name + ' · Cấp ' + level. @return so dong doi. */
    static int ap(final Map<String, List<Integer>> lenh, final Map<Integer, String[]> a, final Map<Integer, String> ten) {
        int n = 0;
        for (final Map.Entry<String, List<Integer>> e : lenh.entrySet()) {
            for (final Integer id : e.getValue()) {
                if (e.getKey().equals(ten.get(id))) {
                    ten.put(id, ten.get(id) + " · Cấp " + a.get(id)[2]);
                    n++;
                }
            }
        }
        return n;
    }
}
