import com.htbb.pokemon.DinhDangBanDo;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
 * 0.9.8 (ban-098, chu game duyet): buoc dia diem toi duoc + "Địa điểm" / NPC cua chuoi chinh dung voi ban do that.
 *
 * <ul>
 *   <li>Moi dong data_nhiem_vu_khu_vuc deu bat (khong con buoc "ban do khong toi duoc"), khong o Hang Darkrai 111 (chi mua
 *       he); ban do khu xa (100 / 118 / 41 / 42) co loi Arcanine gac cong (real.NpcDichVu.LOI_DI), Arcanine dung tren cMap
 *       41 / 42 / 118 (de ve lang) va data_npc loai 4 ghi dung ban do.</li>
 *   <li>Khoa ban do (khoa-map): ban do cua moi buoc dia diem mo (cap_toi_thieu) o cap cua buoc.</li>
 *   <li>Chuoi chinh (data_quest_moi mainsub 0): NPC cua buoc co tren ban do "Địa điểm" hoac o lang khoi dau hoac o noi ghi
 *       ro "NPC:|X (Y)|"; buoc noi chuyen / mang / giao "X ở Y" thi X co tren Y; tran Gym ghi "(đấu ở <Địa điểm>)".</li>
 * </ul>
 * Doc sql/htbb.sql (DayDu), server/cMap.
 */
final class KiemDiaDiemNpc {

    private KiemDiaDiemNpc() {
    }

    static final Set<Integer> LANG = new HashSet<Integer>(java.util.Arrays.asList(0, 70, 80));

    public static void chay() {
        Kiem.nhom("Buoc dia diem toi duoc (Arcanine gac cong) + Dia diem / NPC chuoi chinh dung ban do");
        try {
            chayThat();
        } catch (final Exception e) {
            e.printStackTrace(System.out);
            Kiem.dung("KiemDiaDiemNpc chay het: " + e, false);
        }
    }

    static Set<Integer> so(final String ds) {
        final Set<Integer> s = new TreeSet<Integer>();
        for (final String x : ds.split(",")) {
            if (x.trim().matches("\\d+")) {
                s.add(Integer.valueOf(x.trim()));
            }
        }
        return s;
    }

    static void chayThat() throws Exception {
        final Map<String, Set<Integer>> theoTen = new HashMap<String, Set<Integer>>();
        for (final Map<String, String> d : DayDu.dong("data_map_ten")) {
            Set<Integer> s = theoTen.get(d.get("ten").trim());
            if (s == null) {
                theoTen.put(d.get("ten").trim(), s = new TreeSet<Integer>());
            }
            s.add(Integer.valueOf(d.get("map_id")));
        }
        theoTen.put("Làng khởi đầu", LANG);
        theoTen.put("làng", LANG);
        final Map<String, Set<Integer>> npcO = new HashMap<String, Set<Integer>>(); // ten Pokemon NPC -> ban do
        Set<Integer> arcanine = null;
        for (final Map<String, String> d : DayDu.dong("data_npc")) {
            Set<Integer> s = npcO.get(d.get("pokemon"));
            if (s == null) {
                npcO.put(d.get("pokemon"), s = new TreeSet<Integer>());
            }
            s.addAll(so(d.get("ban_do")));
            if ("4".equals(d.get("type"))) {
                arcanine = so(d.get("ban_do"));
            }
        }
        final Map<Integer, Integer> capMo = new HashMap<Integer, Integer>();
        for (final Map<String, String> d : DayDu.dong("data_ban_do_mo_khoa")) {
            capMo.put(Integer.valueOf(d.get("map_id")), Integer.valueOf(d.get("cap_toi_thieu")));
        }
        final Map<String, Integer> capBuoc = new HashMap<String, Integer>();
        for (final Map<String, String> d : DayDu.dong("data_quest_lau_dai")) {
            capBuoc.put(d.get("ma"), Integer.valueOf(d.get("cap_toi_thieu")));
        }

        // ---- Arcanine gac cong
        final Set<Integer> khuXa = new TreeSet<Integer>();
        for (final int[] d : real.NpcDichVuKiem.loiDi()) {
            khuXa.add(Integer.valueOf(d[0]));
        }
        Kiem.bang("Arcanine dan toi 4 khu xa", "[41, 42, 100, 118]", khuXa.toString());
        Kiem.dung("data_npc loai 4 (Arcanine) o lang + 41 / 42 / 118", arcanine != null && arcanine.containsAll(LANG)
                && arcanine.containsAll(java.util.Arrays.asList(41, 42, 118)));
        for (final int m : new int[]{41, 42, 118}) {
            final DinhDangBanDo b = DinhDangBanDo.doc(Files.readAllBytes(Paths.get("cMap", String.valueOf(m))));
            boolean co = false;
            for (final DinhDangBanDo.Diem n : b.npc) {
                co |= n.v == 4;
            }
            Kiem.dung("cMap/" + m + " co Arcanine (ve lang)", co);
        }
        Kiem.dung("Arcanine: menu o lang = 4 loi, o khu xa = 3 loi khac + Về làng", real.NpcDichVuKiem.soLoi(0) == 4
                && real.NpcDichVuKiem.soLoi(42) == 3 && real.NpcDichVuKiem.laKhuXa(42) && !real.NpcDichVuKiem.laKhuXa(0));
        final String nd = new String(Files.readAllBytes(Paths.get("src/real/NpcDichVu.java")), StandardCharsets.UTF_8);
        Kiem.dung("Arcanine: qua KhoaBanDoGame.chan truoc khi dua di (cap + cua ai)", nd.contains("if (KhoaBanDoGame.chan(p, d[0])) {"));
        Kiem.dung("goi 23 loai 4 -> NpcDichVu", new String(Files.readAllBytes(Paths.get("src/real/cmd/RequestNPCInfoHandler.java")),
                StandardCharsets.UTF_8).contains("npcType == real.NpcDichVu.NPC_ARCANINE"));

        // ---- buoc dia diem: bat, khong ban do theo mua, mo dung cap
        final List<String> tat = new ArrayList<String>();
        final List<String> khoa = new ArrayList<String>();
        int n = 0;
        for (final Map<String, String> d : DayDu.dong("data_nhiem_vu_khu_vuc")) {
            n++;
            final int map = Integer.parseInt(d.get("map_id"));
            if (!"1".equals(d.get("bat")) || map == 111) {
                tat.add(d.get("ma") + "@" + map);
            }
            final Integer cap = capBuoc.get(d.get("ma"));
            final Integer mo = capMo.get(map);
            if (cap != null && mo != null && mo.intValue() > cap.intValue()) {
                khoa.add(d.get("ma") + " (cap " + cap + ") @" + map + " mo tu cap " + mo);
            }
        }
        Kiem.dung("co buoc dia diem (" + n + ")", n > 100);
        Kiem.bang("moi buoc dia diem bat, khong o Hang Darkrai (mua he)", "[]", tat.toString());
        Kiem.bang("ban do cua buoc dia diem mo o cap cua buoc (khoa-map)", "[]", khoa.toString());

        // ---- chuoi chinh: Dia diem / NPC
        final Pattern pDd = Pattern.compile("Địa điểm:\\|([^|]*)\\|");
        final Pattern pNpc = Pattern.compile("NPC:\\|([^|]*)\\|");
        final Pattern pNoi = Pattern.compile("^(?:Nói chuyện với|Mang .+? tới|Giao .+? cho) (\\S+) ở (.+)$");
        final Pattern pGym = Pattern.compile("^Thắng trưởng Gym .+?: \\S+(?: \\(đấu ở (.+)\\))?$");
        final List<String> loiNpc = new ArrayList<String>();
        final List<String> loiNoi = new ArrayList<String>();
        final List<String> loiGym = new ArrayList<String>();
        int soBuoc = 0;
        final List<String> loiNpcPhu = new ArrayList<String>();
        final List<String> loiNoiPhu = new ArrayList<String>();
        int soBuocPhu = 0;
        for (final Map<String, String> q : DayDu.dong("data_quest_moi")) {
            final boolean phu = "1".equals(q.get("mainsub"));
            if (!"0".equals(q.get("mainsub")) && !phu) {
                continue;
            }
            final String inf = q.get("infogift") == null ? "" : q.get("infogift");
            final Matcher md = pDd.matcher(inf);
            final Matcher mn = pNpc.matcher(inf);
            if (!md.find() || !mn.find()) {
                continue;
            }
            final String dd = md.group(1).trim();
            final String npcChu = mn.group(1).trim();
            if (npcChu.contains("→")) {
                continue; // huong dan tan thu: "Xatu → Blaziken" (ca hai o lang, KiemHuongDanTanThu)
            }
            if (phu) {
                soBuocPhu++;
            } else {
                soBuoc++;
            }
            final Set<Integer> mapDd = theoTen.get(dd);
            final int o = npcChu.indexOf(" (");
            final String ten = o > 0 ? npcChu.substring(0, o) : npcChu;
            final Set<Integer> noiNpc = npcO.get(ten);
            boolean ok = noiNpc != null && (giao(noiNpc, mapDd) || giao(noiNpc, LANG));
            if (!ok && noiNpc != null && o > 0) {
                ok = npcChu.endsWith(")") && giao(noiNpc, theoTen.get(npcChu.substring(o + 2, npcChu.length() - 1).trim()));
            }
            if (!ok) {
                (phu ? loiNpcPhu : loiNpc).add(q.get("id") + " NPC " + npcChu + " @ " + dd);
            }
            final String sc = q.get("scontent") == null ? "" : q.get("scontent").trim();
            final Matcher m3 = pNoi.matcher(sc);
            if (m3.find()) {
                final Set<Integer> x = npcO.get(m3.group(1));
                if (x == null || !giao(x, theoTen.get(m3.group(2).trim()))) {
                    (phu ? loiNoiPhu : loiNoi).add(q.get("id") + " " + sc);
                }
            }
            final Matcher mg = pGym.matcher(sc);
            if (mg.find() && !sc.startsWith("Thắng trưởng Gym " + dd + ":") && (mg.group(1) == null || !mg.group(1).trim().equals(dd))) {
                loiGym.add(q.get("id") + " " + sc + " @ " + dd);
            }
        }
        Kiem.dung("chuoi chinh: co buoc de kiem (" + soBuoc + ")", soBuoc > 300);
        Kiem.bang("chuoi chinh: NPC cua buoc co tren ban do Dia diem / o lang / o noi ghi ro", "[]", loiNpc.toString());
        Kiem.bang("chuoi chinh: 'Noi chuyen / mang / giao X o Y' - X co tren Y", "[]", loiNoi.toString());
        Kiem.bang("chuoi chinh: tran Gym ghi dung noi dau (= Dia diem)", "[]", loiGym.toString());
        // dong bo 0.9.8: chuoi phu cung luat (truoc: 173 buoc "Nói chuyện với X ở Pallet / Viridian..." ma X chi o lang)
        Kiem.dung("chuoi phu: co buoc de kiem (" + soBuocPhu + ")", soBuocPhu > 300);
        Kiem.bang("chuoi phu: NPC cua buoc co tren ban do Dia diem / o lang / o noi ghi ro", "[]", loiNpcPhu.toString());
        Kiem.bang("chuoi phu: 'Noi chuyen / mang / giao X o Y' - X co tren Y", "[]", loiNoiPhu.toString());
    }

    static boolean giao(final Set<Integer> a, final Set<Integer> b) {
        if (a == null || b == null) {
            return false;
        }
        for (final Integer x : a) {
            if (b.contains(x)) {
                return true;
            }
        }
        return false;
    }
}
