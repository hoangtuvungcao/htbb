import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * QUAI NHIEM VU TOI DUOC (30/10/2026, bot phu kin d250 ket o C39.6 "Hạ 5 Whiscash ở Tàu S.S. Anne"): moi quai ma nhiem vu
 * chinh / phu / cua ai bat ha (idMonskill, cua ai kieu giet) phai sinh o it nhat mot ban do nguoi choi toi duoc tu lang
 * bang duong mien phi / mua ve (cong, Arcanine, Lapras, Pidgeot - KiemSuaTuyen.toiDuoc). Truoc day Tau S.S. Anne (22 + ban
 * sao 741..744) khong co cong / NPC nao dan vao: Whiscash / Kingler chi sinh o do -> CA-11.1 (mo Duong Victory), 4 buoc
 * chinh + 6 phu khong lam duoc. Sua: Arcanine "Đến Tàu S.S. Anne" (khoa ban do cap 65 + CA-10.3) + Arcanine trong cMap/22.
 */
public final class KiemQuaiNhiemVuToiDuoc {

    private KiemQuaiNhiemVuToiDuoc() {
    }

    /**
     * Quai o ban do toi duoc qua cong ma mo hinh tinh KiemSuaTuyen.toiDuoc khong thay (nhom locationMap noi them luc chay -
     * real.VungHoenn): Galvantula 118 o Rung Co 26 (cong 15 -> 26, goi 105 that; bot doan d290 toi va ha duoc 30/10/2026).
     */
    static final Set<Integer> QUAI_CONG_LUC_CHAY = new HashSet<Integer>(java.util.Arrays.asList(118));

    /** Ma mau quai sinh tren luoi map/map{load} (MapKhoiTao: load 17 ma > 81 cong 3). */
    static Set<Integer> quaiCua(final int load) throws Exception {
        final Set<Integer> s = new HashSet<Integer>();
        final File f = new File("map/map" + load);
        if (!f.isFile()) {
            return s;
        }
        final byte[] b = Files.readAllBytes(f.toPath());
        final int w = b[0] & 255, h = b[1] & 255;
        for (int i = 0; i < w * h && 2 + i < b.length; i++) {
            final int v = b[2 + i] & 255;
            if (v > 0 && v != 255) {
                s.add(Integer.valueOf(load == 17 && v > 81 ? v + 3 : v));
            }
        }
        return s;
    }

    public static void chay() {
        Kiem.nhom("Quai nhiem vu chinh / phu / cua ai sinh o ban do toi duoc (Tau S.S. Anne qua Arcanine)");
        try {
            final String du = DayDu.sql();
            final String rc = KiemSuaTuyen.doc("src/real/RealController.java");
            final Map<Integer, Integer> load = new HashMap<Integer, Integer>();
            final Matcher m = Pattern.compile("mapList\\.put\\(new Integer\\((\\d+)\\), (?:m = )?new (\\w+)\\((\\d+), ([-\\d]+), ([-\\d]+), ([-\\d]+)").matcher(rc);
            while (m.find()) {
                load.put(Integer.valueOf(m.group(1)), Integer.valueOf(m.group(6)));
            }
            final Set<Integer> toi = new HashSet<Integer>(KiemSuaTuyen.toiDuoc(du));
            toi.addAll(KiemSuaTuyen.LANG);
            Kiem.dung("Tau S.S. Anne (22) toi duoc tu lang", toi.contains(Integer.valueOf(22)));
            Kiem.dung("Dong co phia tay (21) toi duoc tu lang", toi.contains(Integer.valueOf(21)));
            final Set<Integer> quaiToi = new HashSet<Integer>();
            for (final Integer mp : toi) {
                final Integer l = load.get(mp);
                quaiToi.addAll(quaiCua(l == null ? mp.intValue() : l.intValue()));
            }
            final Set<String> thieu = new TreeSet<String>();
            int soQuai = 0;
            for (final List<String> r : KiemSuaTuyen.dong(du, "data_quest_moi")) {
                final String ms = r.get(5).trim();
                if (!"0".equals(ms) && !"1".equals(ms)) {
                    continue;
                }
                for (final String x : r.get(10).split(",")) {
                    final String t = x.trim();
                    if (t.isEmpty() || "32000".equals(t) || "-1".equals(t)) {
                        continue;
                    }
                    soQuai++;
                    if (!quaiToi.contains(Integer.valueOf(t)) && !QUAI_CONG_LUC_CHAY.contains(Integer.valueOf(t))) {
                        thieu.add((ms.equals("0") ? "chinh " : "phu ") + r.get(0).trim() + " '" + r.get(1).trim() + "' quai " + t);
                    }
                }
            }
            for (final List<String> r : KiemSuaTuyen.dong(du, "data_quest_lau_dai")) {
                if (r.size() > 20 && "cua_ai".equals(r.get(2).trim())) {
                    // cua ai giet: muc tieu mau quai (cot muc_tieu_id) - doi chieu theo ten cot qua chuoi 'giet'
                    final int k = r.indexOf("giet");
                    if (k > 0 && k + 1 < r.size()) {
                        final String t = r.get(k + 1).trim();
                        if (t.matches("\\d+")) {
                            soQuai++;
                            if (!quaiToi.contains(Integer.valueOf(t))) {
                                thieu.add("cua ai " + r.get(0).trim() + " quai " + t);
                            }
                        }
                    }
                }
            }
            Kiem.dung("da xet du quai nhiem vu (" + soQuai + ")", soQuai > 200);
            Kiem.bang("moi quai nhiem vu sinh o ban do toi duoc", "[]", thieu.toString());
        } catch (final Exception e) {
            Kiem.dung("khong ngoai le: " + e, false);
        }
    }
}
