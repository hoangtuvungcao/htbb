import com.htbb.pokemon.ChenhCapNhiemVu;
import com.htbb.pokemon.ThongSoLauDai;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * CAN BANG CAP 15-45 (bot-cap1-40 26/10/2026: tu cap 20 guc lien tuc, #29 ket, boss Gym cho 2 cap). Quyet dinh chu game:
 * <ol>
 *   <li>"Tự cân chỉnh": duong chi so quai (data_monster_lau_dai, khoi sua_can_bang_cap.sql) khop suc nguoi choi do cua
 *       hang +0..+3 - khong gai giua hai cap ke nhau; gia do cua hang (data_item) <= nhip thiet ke theo gio thu nhap.</li>
 *   <li>Nhiem vu kieu 4 "chenh khong qua 5 cap" luon lam duoc o moi cap chuoi chinh dat toi (ChenhCapNhiemVu).</li>
 *   <li>EXP thang Gym: tran mot cap, cao hon boss nhan it di (real.ExpLauDai.expThangGym).</li>
 * </ol>
 */
public final class KiemCanBangCap {

    private KiemCanBangCap() {
    }

    public static void chay() {
        Kiem.nhom("Can bang cap 15-45: EXP thang Gym theo cap (tran 1 cap, cao hon boss giam)");
        try {
            expGym();
        } catch (final Exception e) {
            Kiem.bang("expGym khong nem loi", "", String.valueOf(e));
        }
        Kiem.nhom("Can bang cap 15-45: nhiem vu 'chenh khong qua 5 cap' luon lam duoc (#29)");
        try {
            chenhCap();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.bang("chenhCap khong nem loi", "", String.valueOf(e));
        }
        Kiem.nhom("Can bang cap 15-45: duong chi so quai khop suc nguoi choi do cua hang, gia do theo gio thu nhap");
        try {
            KiemCanBangCapQuai.chay();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.bang("duong quai khong nem loi", "", String.valueOf(e));
        }
        Kiem.nhom("Can bang theo dong / he (lan 2): quai chu dong, Gym khac he <= x1,25 moi dong, khoi sua_can_bang_lop.sql");
        try {
            KiemCanBangCapLop.chay();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.bang("KiemCanBangCapLop khong nem loi", "", String.valueOf(e));
        }
    }

    // ------------------------------------------------------------------ Gym

    static void expGym() {
        Kiem.bang("Gym dung cap: du mot cap", Long.valueOf(8100L), Long.valueOf(real.ExpLauDai.expThangGym(12, 12, 8100L)));
        Kiem.bang("Gym: thap hon boss van chi mot cap (khong thuong them)", Long.valueOf(5210L),
                Long.valueOf(real.ExpLauDai.expThangGym(10, 12, 5210L)));
        Kiem.bang("Gym Pewter (12) nguoi cap 21: 50% mot cap", Long.valueOf(46150L), Long.valueOf(real.ExpLauDai.expThangGym(21, 12, 92300L)));
        Kiem.bang("Gym Pewter (12) nguoi cap 27: 5% mot cap (truoc: 1.000.000 = 27 -> 29)", Long.valueOf(15700L),
                Long.valueOf(real.ExpLauDai.expThangGym(27, 12, 314000L)));
        Kiem.bang("Gym cap cao: khong qua 1.000.000 cu", Long.valueOf(1000000L),
                Long.valueOf(real.ExpLauDai.expThangGym(60, 60, 609573273L)));
        final long[] len = ThongSoLauDai.EXP_LEN_CAP;
        Kiem.dung("bang EXP dai han da nap (" + len.length + ")", len.length > 90);
        final List<String> vuot = new ArrayList<String>();
        final List<String> tang = new ArrayList<String>();
        for (final Map<String, String> g : DayDu.dong("data_noi_dung_gym")) {
            final int cap = Integer.parseInt(g.get("cap"));
            long truoc = Long.MAX_VALUE;
            for (int L = 1; L < Math.min(100, len.length); L++) {
                final long x = real.ExpLauDai.expThangGym(L, cap, len[L]);
                if (x > len[L] || x > real.ExpLauDai.EXP_BOSS_CU) {
                    vuot.add(g.get("stt") + "@" + L);
                }
                // phan tram mot cap khong tang khi nguoi choi vuot cap boss
                final long pm = len[L] <= 0 ? 0 : x * 1000L / Math.min(real.ExpLauDai.EXP_BOSS_CU, len[L]);
                if (L > cap && pm > truoc) {
                    tang.add(g.get("stt") + "@" + L);
                }
                if (L >= cap) {
                    truoc = pm;
                }
            }
        }
        Kiem.bang("moi Gym, moi cap 1..99: EXP thang <= mot cap va <= 1.000.000", "[]", vuot.toString());
        Kiem.bang("moi Gym: nguoi cang vuot cap boss nhan ti le cang it", "[]", tang.toString());
        final String m = DayDu.doc("src/real/Monster.java");
        Kiem.dung("Monster.onDropItem: EXP ket lieu boss qua ExpLauDai.expHaBoss (khong con 1.000.000 phang)",
                m.contains("ExpLauDai.expHaBoss(p, this)") && !m.contains("addXpCharEvent(p, 1000000L, false, \"Monster1\")"));
        Kiem.dung("BossLauDai.datGym ghi Monster.gymStt (con sau khiChet)", DayDu.doc("src/real/BossLauDai.java").contains("m.gymStt = stt;"));
    }

    // ------------------------------------------------------------------ #29

    /** Cap quai thuong (isuse 1) cao nhat trong luoi sinh server/map/map<id>. */
    static Map<Integer, Integer> capBanDo(final Map<Integer, Integer> capQuai) throws Exception {
        final Map<Integer, Integer> ra = new TreeMap<Integer, Integer>();
        for (final Map<String, String> d : DayDu.dong("data_ban_do_mo_khoa")) {
            if (!"1".equals(d.get("cho_phep_tu_dong"))) {
                continue;
            }
            final int id = Integer.parseInt(d.get("map_id"));
            final File f = new File("map/map" + id);
            if (!f.isFile()) {
                continue;
            }
            final byte[] b = Files.readAllBytes(f.toPath());
            final int w = b[0] & 0xff, h = b[1] & 0xff;
            int max = 0;
            for (int i = 0; i < w * h && 2 + i < b.length; i++) {
                int v = b[2 + i] & 0xff;
                if (v == 0 || v == 255) {
                    continue;
                }
                if (id == 17 && v > 81) {
                    v += 3;
                }
                final Integer c = capQuai.get(Integer.valueOf(v));
                if (c != null) {
                    max = Math.max(max, c.intValue());
                }
            }
            if (max > 0) {
                ra.put(Integer.valueOf(id), Integer.valueOf(max));
            }
        }
        return ra;
    }

    static int so(final String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (final Exception e) {
            return 0;
        }
    }

    static void chenhCap() throws Exception {
        // luat thuan
        Kiem.dung("chenh 5: tinh", ChenhCapNhiemVu.tinh(20, 15, 5, 0, 0));
        Kiem.dung("chenh 6, khong o bai cao nhat: khong tinh", !ChenhCapNhiemVu.tinh(21, 15, 5, 20, 27));
        Kiem.dung("chenh 6, o bai cao nhat da mo: tinh", ChenhCapNhiemVu.tinh(21, 15, 5, 20, 20));
        Kiem.dung("ban do khong phai bai luyen (0): khong tinh", !ChenhCapNhiemVu.tinh(30, 10, 5, 0, 0));

        final Map<Integer, Integer> capQuai = new HashMap<Integer, Integer>();
        for (final Map<String, String> d : DayDu.dong("data_monster")) {
            if ("1".equals(d.get("isuse"))) {
                capQuai.put(Integer.valueOf(so(d.get("id"))), Integer.valueOf(so(d.get("level"))));
            }
        }
        final Map<Integer, Integer> capMap = capBanDo(capQuai);
        Kiem.dung("bai luyen co quai (" + capMap.size() + ")", capMap.size() >= 20);
        // cua ai: so thu tu k -> cap toi thieu de lam xong ca cua ai k
        final Map<Integer, Integer> capCuaAi = new TreeMap<Integer, Integer>();
        for (final Map<String, String> d : DayDu.dong("data_quest_lau_dai")) {
            final String ma = d.get("ma");
            if (ma != null && ma.matches("CA-\\d+\\.\\d+")) {
                final int k = Integer.parseInt(ma.substring(3, ma.indexOf('.')));
                final Integer c = capCuaAi.get(Integer.valueOf(k));
                capCuaAi.put(Integer.valueOf(k), Integer.valueOf(Math.max(c == null ? 0 : c.intValue(), so(d.get("cap_toi_thieu")))));
            }
        }
        final Map<Integer, String[]> luat = new HashMap<Integer, String[]>();
        for (final Map<String, String> d : DayDu.dong("data_ban_do_mo_khoa")) {
            luat.put(Integer.valueOf(so(d.get("map_id"))), new String[]{d.get("cap_toi_thieu"), d.get("ma_nhiem_vu")});
        }
        // cap cac buoc kieu 4 cua chuoi chinh
        final List<Integer> capBuoc = new ArrayList<Integer>();
        for (final Map<String, String> q : DayDu.dong("data_quest_moi")) {
            if ("4".equals(q.get("type")) && "0".equals(q.get("mainsub"))) {
                capBuoc.add(Integer.valueOf(so(q.get("lv"))));
            }
        }
        Kiem.dung("chuoi chinh co buoc kieu 4 (" + capBuoc.size() + ")", capBuoc.size() >= 5);
        final List<String> ket = new ArrayList<String>();
        int ketCu = 0, tong = 0;
        for (final int capQ : capBuoc) {
            for (int L = capQ; L <= 89; L++) {
                for (int k = 0; k <= 12; k++) {
                    final Integer ck = capCuaAi.get(Integer.valueOf(k));
                    if (k > 0 && (ck == null || ck.intValue() > L)) {
                        break; // cua ai k chua lam duoc o cap L
                    }
                    tong++;
                    int caoNhat = 0;
                    boolean cu = false;
                    for (final Map.Entry<Integer, Integer> e : capMap.entrySet()) {
                        final String[] l = luat.get(e.getKey());
                        final int capMo = so(l[0]);
                        final String ma = l[1] == null ? "" : l[1].trim();
                        boolean mo = capMo <= L;
                        if (mo && !ma.isEmpty()) {
                            mo = ma.matches("CA-\\d+\\.\\d+") && Integer.parseInt(ma.substring(3, ma.indexOf('.'))) <= k;
                        }
                        if (!mo) {
                            continue;
                        }
                        caoNhat = Math.max(caoNhat, e.getValue().intValue());
                        cu |= L - e.getValue().intValue() <= 5; // luat cu: phai co quai >= L-5 (quai cap cao nhat cua ban do)
                    }
                    if (caoNhat == 0 || !ChenhCapNhiemVu.tinh(L, caoNhat, 5, caoNhat, caoNhat)) {
                        ket.add("buoc " + capQ + " cap " + L + " cua ai " + k);
                    }
                    if (!cu) {
                        ketCu++;
                    }
                }
            }
        }
        Kiem.dung("so trang thai (buoc kieu 4 x cap x cua ai da xong) da xet: " + tong, tong > 500);
        Kiem.bang("moi cap chuoi chinh dat toi, moi tien do cua ai: co quai tinh duoc (bai cao nhat da mo)", "[]",
                ket.size() > 8 ? ket.subList(0, 8) + "... (" + ket.size() + ")" : ket.toString());
        Kiem.dung("luat cu (chi |chenh| <= 5) ket o " + ketCu + " trang thai (vd cap 21, xong CA-03) - luat moi gom ca", ketCu > 0);
        // may chu dung luat moi o ca hai cho dem
        Kiem.dung("Char.checkFinishQuest kieu 4 qua ChenhCapNhiemVuGame.tinh",
                DayDu.doc("src/real/Char.java").contains("ChenhCapNhiemVuGame.tinh(this, lv, info.deltaLv)"));
        Kiem.dung("NhiemVuEngine giet_lech_cap qua ChenhCapNhiemVuGame.tinh",
                DayDu.doc("src/real/NhiemVuEngine.java").contains("ChenhCapNhiemVuGame.tinh(p, mt.level, 5)"));
        Kiem.dung("MapNapTaiNguyen.loadMap ghi cap quai ban do", DayDu.doc("src/real/MapNapTaiNguyen.java").contains("ChenhCapNhiemVuGame.ghiQuai(map.mapId"));
        Kiem.bang("chu muc tieu bang Nhiem vu ghi ro bai cao nhat",
                "Hạ 30 Pokémon hoang dã chênh không quá 5 cấp (hoặc ở bãi luyện cao nhất bạn đã mở)",
                com.htbb.pokemon.MucTieuNhiemVu.muc(com.htbb.pokemon.MucTieuNhiemVu.GIET_CAP,
                        new com.htbb.pokemon.MucTieuNhiemVu.Muc[]{new com.htbb.pokemon.MucTieuNhiemVu.Muc("", 30, null, "")}, 5, 8, ""));
        // khoi SQL chu: ap len phan A trong bo nho
        final String khoi = DayDu.phanNangCap("sua_chenh_cap_nhiem_vu.sql");
        final Map<String, List<Map<String, String>>> bang = new HashMap<String, List<Map<String, String>>>();
        final List<Map<String, String>> qm = KiemMucTieuNhiemVu.sao(KiemMucTieuNhiemVu.dongThuong("data_quest_moi"));
        final List<Map<String, String>> nd = KiemMucTieuNhiemVu.sao(KiemMucTieuNhiemVu.dongThuong("data_nhiem_vu_noi_dung"));
        bang.put("data_quest_moi", qm);
        bang.put("data_nhiem_vu_noi_dung", nd);
        bang.put("data_quest_lau_dai", KiemMucTieuNhiemVu.sao(KiemMucTieuNhiemVu.dongThuong("data_quest_lau_dai")));
        // cac khoi phan B truoc (cung dang UPDATE theo gia tri cu) da sua chu cac buoc nay: ap theo thu tu tep
        KiemMucTieuNhiemVu.ap(DayDu.phanNangCap(KiemMucTieuNhiemVu.KHOI), bang);
        final List<Integer> lan1 = KiemMucTieuNhiemVu.ap(khoi, bang);
        int sai = 0;
        for (final Integer n : lan1) {
            sai += n.intValue() == 1 ? 0 : 1;
        }
        Kiem.dung("khoi sua_chenh_cap_nhiem_vu.sql co lenh (" + lan1.size() + ")", lan1.size() >= 28);
        Kiem.bang("khoi chu: moi lenh khop dung mot dong (gia tri cu dung)", Integer.valueOf(0), Integer.valueOf(sai));
        int lai = 0;
        for (final Integer n : KiemMucTieuNhiemVu.ap(khoi, bang)) {
            lai += n.intValue();
        }
        Kiem.bang("khoi chu: ap lan hai khong doi gi", Integer.valueOf(0), Integer.valueOf(lai));
        final List<String> conCu = new ArrayList<String>();
        for (final Map<String, String> q : qm) {
            if ("4".equals(q.get("type")) && !q.get("scontent").contains(ChenhCapNhiemVu.DUOI_CHU.trim())) {
                conCu.add(q.get("id"));
            }
        }
        Kiem.bang("sau khoi: moi nhiem vu kieu 4 ghi '(hoặc ở bãi luyện cao nhất bạn đã mở)', khong con ' ở <ban do>'", "[]", conCu.toString());
    }
}
