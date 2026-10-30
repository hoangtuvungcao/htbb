import com.htbb.pokemon.ChuDongQuai;
import com.htbb.pokemon.GiaiDoanBoss;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.LoaiPokemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * CAN BANG THEO DONG / HE (lan 2 cua "Tự cân chỉnh", 29/10/2026). Lan 1 chi do bang hai bot dong Lua; lan 2 do suc ca 5 dong
 * (class 0..4 = Charmander / Bulbasaur / Eevee / Squirtle / Pichu) o Gym va bai cua ai bang bot KichBanDoSuc, choi 5 dong
 * cap 1-40 bang KichBanCap140.
 * <ol>
 *   <li>Quai chu dong (com.htbb.pokemon.ChuDongQuai): quai xam (thap hon >= 4 cap) khong tu lao vao, toi da 2 quai chua
 *       bi danh cung danh mot nguoi, quai bi danh luon danh tra.</li>
 *   <li>Truong Gym: khac he vao nguoi choi tran x1,25 (GiaiDoanBoss.giamKhacHeGym) - MOI dong x MOI Gym (he theo loai cua
 *       dong o cap Gym) <= x1,25; truoc co cap (Gyarados -> dong Lua, Luxray -> dong Nuoc...) x1,5. Chieu nguoi choi vao
 *       Gym it nhat x0,7 hieu dung (GiaiDoanBoss.hpGymTheoHe: dong Dien gap Golem / Seismitoad he Dat x0,3).</li>
 *   <li>Khoi sua_can_bang_lop.sql: Gym Cerulean nhe hon (van >= Pewter), HP quai cap 61-69 noi suy (het buoc +24%).</li>
 * </ol>
 */
public final class KiemCanBangCapLop {

    private KiemCanBangCapLop() {
    }

    public static void chay() {
        chuDong();
        gymHe();
        baoHiemRoi();
        try {
            arcanine();
        } catch (final Exception e) {
            Kiem.bang("Arcanine khong nem loi", "", String.valueOf(e));
        }
        try {
            nguon();
        } catch (final Exception e) {
            Kiem.bang("doc nguon Monster / BossLauDai", "", String.valueOf(e));
        }
        try {
            khoi();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.bang("khoi sua_can_bang_lop.sql khong nem loi", "", String.valueOf(e));
        }
    }

    static void chuDong() {
        final int x = ChuDongQuai.XAM_CAP_MAC_DINH, v = ChuDongQuai.TOI_DA_VAY_MAC_DINH;
        Kiem.bang("mac dinh quai.xam_cap / quai.toi_da_vay", "4/2", x + "/" + v);
        // Combusken cap 18 / Butterfree cap 18 voi nguoi cap 24-26 (guc tren duong qua Celadon / Saffron / Fuchsia)
        Kiem.dung("quai thap hon 4 cap la quai xam (16 vs 20)", ChuDongQuai.xam(16, 20, x));
        Kiem.dung("quai thap hon 3 cap khong xam (17 vs 20)", !ChuDongQuai.xam(17, 20, x));
        Kiem.dung("quai cao hon khong xam", !ChuDongQuai.xam(27, 24, x));
        Kiem.dung("nguong 0 = tat luat xam", !ChuDongQuai.xam(1, 99, 0));
        Kiem.dung("quai xam chua bi danh: khong tu lao vao", !ChuDongQuai.duocDanh(18, 26, false, 0, x, v));
        Kiem.dung("quai xam BI DANH thi danh tra", ChuDongQuai.duocDanh(18, 26, true, 9, x, v));
        Kiem.dung("ngang cap, 1 quai khac dang danh: con thu 2 duoc danh", ChuDongQuai.duocDanh(26, 26, false, 1, x, v));
        Kiem.dung("ngang cap, 2 quai khac dang danh: con thu 3 cho", !ChuDongQuai.duocDanh(26, 26, false, 2, x, v));
        Kiem.dung("quai bi danh khong tinh tran vay", ChuDongQuai.duocDanh(26, 26, true, 5, x, v));
        Kiem.dung("toi_da_vay 0 = tat", ChuDongQuai.duocDanh(26, 26, false, 50, x, 0));
    }

    static void gymHe() {
        final int t = GiaiDoanBoss.GYM_HE_TOI_DA;
        Kiem.bang("gym.he_toi_da mac dinh 125, gym.so_de 1", "125/1", t + "/" + GiaiDoanBoss.GYM_SO_DE);
        Kiem.bang("khac x1,5 -> x1,25", Integer.valueOf(833), Integer.valueOf(GiaiDoanBoss.giamKhacHeGym(1000, 150, t)));
        Kiem.bang("khac x2 -> x1,25", Integer.valueOf(625), Integer.valueOf(GiaiDoanBoss.giamKhacHeGym(1000, 200, t)));
        Kiem.bang("trung tinh giu", Integer.valueOf(1000), Integer.valueOf(GiaiDoanBoss.giamKhacHeGym(1000, 100, t)));
        Kiem.bang("yeu x0,7 giu", Integer.valueOf(1000), Integer.valueOf(GiaiDoanBoss.giamKhacHeGym(1000, 70, t)));
        Kiem.bang("tran 0 = tat", Integer.valueOf(1000), Integer.valueOf(GiaiDoanBoss.giamKhacHeGym(1000, 150, 0)));
        Kiem.bang("toi thieu 1", Integer.valueOf(1), Integer.valueOf(GiaiDoanBoss.giamKhacHeGym(1, 200, t)));
        // moi Gym x moi dong (loai cua dong o cap Gym): he so khac he sau tran <= 125; truoc tran co cap > 125
        final Map<String, int[]> he = new HashMap<String, int[]>();
        for (final Map<String, String> d : DayDu.dong("data_monster")) {
            final String h2 = d.get("he2");
            he.put(d.get("id"), new int[]{Integer.parseInt(d.get("he")), h2 == null || h2.isEmpty() ? -1 : Integer.parseInt(h2)});
        }
        final List<String> qua = new ArrayList<String>();
        final List<String> truoc = new ArrayList<String>();
        int cap = 0;
        for (final Map<String, String> g : KiemMucTieuNhiemVu.dongThuong("data_noi_dung_gym")) {
            final int[] hb = he.get(g.get("template_quai"));
            if (hb == null) {
                qua.add("gym " + g.get("stt") + " khong co template " + g.get("template_quai"));
                continue;
            }
            final int capGym = Integer.parseInt(g.get("cap"));
            for (int lop = 0; lop < LoaiPokemon.KHOI_DAU_THEO_CLASS.length; lop++) {
                final int dex = LoaiPokemon.dangTheoCap(LoaiPokemon.KHOI_DAU_THEO_CLASS[lop], capGym);
                final int hs = HePokemon.heSoTrongGame(hb[0], LoaiPokemon.he1(dex), LoaiPokemon.he2(dex));
                cap++;
                if (hs > t) {
                    truoc.add(g.get("stt") + "/" + lop + ":" + hs);
                }
                final int sau = GiaiDoanBoss.giamKhacHeGym(1000, hs, t);
                if (sau > 1000 * t / 100) {
                    qua.add(g.get("stt") + "/" + lop + ":" + sau);
                }
            }
        }
        Kiem.dung("du 16 Gym x 5 dong (" + cap + ")", cap >= 80);
        // chieu NGUOI CHOI vao Gym: dong khong co chieu danh nao qua x0,7 -> HP boss x tot / 70 (GiaiDoanBoss.hpGymTheoHe)
        final int san = GiaiDoanBoss.GYM_HE_RA_TOI_THIEU;
        Kiem.bang("hpGymTheoHe: x0,3 -> 30/70 HP", Integer.valueOf(30000), Integer.valueOf(GiaiDoanBoss.hpGymTheoHe(70000, 30, san)));
        Kiem.bang("hpGymTheoHe: >= san giu", Integer.valueOf(70000), Integer.valueOf(GiaiDoanBoss.hpGymTheoHe(70000, 70, san)));
        Kiem.bang("hpGymTheoHe: khong biet (0) giu", Integer.valueOf(70000), Integer.valueOf(GiaiDoanBoss.hpGymTheoHe(70000, 0, san)));
        Kiem.bang("hpGymTheoHe: san 0 = tat", Integer.valueOf(70000), Integer.valueOf(GiaiDoanBoss.hpGymTheoHe(70000, 30, 0)));
        final List<String> chan = new ArrayList<String>();
        final List<String> conChan = new ArrayList<String>();
        for (final Map<String, String> g : KiemMucTieuNhiemVu.dongThuong("data_noi_dung_gym")) {
            final int[] hb = he.get(g.get("template_quai"));
            if (hb == null) {
                continue;
            }
            for (int lop = 0; lop < LoaiPokemon.KHOI_DAU_THEO_CLASS.length; lop++) {
                int tot = 0;
                for (int k = 0; k < 4; k++) {
                    final int hc = com.htbb.pokemon.ChieuPokemon.heChieu(lop, k);
                    if (hc >= 0) {
                        tot = Math.max(tot, HePokemon.heSoTrongGame(hc, hb[0], hb[1]));
                    }
                }
                if (tot < san) {
                    chan.add(g.get("stt") + "/" + lop + ":" + tot);
                }
                final int hp = 100000;
                final double hieuDung = 100.0 * tot * hp / Math.max(1, GiaiDoanBoss.hpGymTheoHe(hp, tot, san));
                if (hieuDung < san - 0.5) {
                    conChan.add(g.get("stt") + "/" + lop + ":" + String.format("%.0f", hieuDung));
                }
            }
        }
        Kiem.dung("truoc: dong Dien gan mien Gym he Dat (Pewter Golem, Seismitoad) (" + chan + ")", chan.contains("1/4:30") && chan.contains("5/4:30"));
        Kiem.bang("sau: moi Gym x moi dong danh hieu dung >= x0,7", "[]", conChan.toString());
        Kiem.dung("truoc tran co cap khac he > x1,25 (" + truoc + ")", truoc.contains("2/0:150") && truoc.contains("3/3:150"));
        Kiem.bang("sau tran: moi Gym x moi dong <= x1,25", "[]", qua.toString());
    }

    /**
     * Arcanine gac cong (real.NpcDichVu.LOI_DI): o xuat hien nam TRONG ban do dich (cMap rong x cao). Choi 5 dong 30/10/2026:
     * moi dong ket o #36 'Bảo tàng thiên thạch' - Arcanine dua toi o (88, 168) cua Bao tang 11 x 16 (truyen pixel).
     */
    static void arcanine() throws Exception {
        final List<String> ngoai = new ArrayList<String>();
        int n = 0;
        for (final int[] d : real.NpcDichVuKiem.loiDi()) {
            final byte[] b = java.nio.file.Files.readAllBytes(new java.io.File("cMap/" + d[0]).toPath());
            final int w = b[0] & 255, h = b[1] & 255;
            final int[] o = real.NpcDichVuKiem.oDen(d);
            n++;
            if (o[0] < 0 || o[1] < 0 || o[0] >= w || o[1] >= h) {
                ngoai.add(d[0] + ":" + o[0] + "," + o[1] + " (ban do " + w + "x" + h + ")");
            }
        }
        Kiem.dung("Arcanine: du 4 loi di (" + n + ")", n >= 4);
        Kiem.bang("Arcanine: o xuat hien trong ban do dich", "[]", ngoai.toString());
        final String s = new String(java.nio.file.Files.readAllBytes(new java.io.File("src/real/NpcDichVu.java").toPath()), "UTF-8");
        Kiem.dung("Arcanine: move2Map nhan O (khong con d * 16 + 8)", !s.contains("d[1] * 16 + 8") && s.contains("p.map.move2Map(p, o[0], o[1], d[0], p.inCountry)"));
    }

    /** Bao hiem xui vat nhiem vu kieu 2 (BaoHiemRoiNhiemVu). */
    static void baoHiemRoi() {
        final int n = com.htbb.pokemon.BaoHiemRoiNhiemVu.nguong(com.htbb.pokemon.BaoHiemRoiNhiemVu.TI_LE_GOC);
        Kiem.bang("nguong o ti le goc 15% = 2 x 6,7 con", Integer.valueOf(13), Integer.valueOf(n));
        Kiem.bang("nguong ti le 100% / 0", "1/1", com.htbb.pokemon.BaoHiemRoiNhiemVu.nguong(1000000) + "/" + com.htbb.pokemon.BaoHiemRoiNhiemVu.nguong(0));
        final com.htbb.pokemon.BaoHiemRoiNhiemVu b = new com.htbb.pokemon.BaoHiemRoiNhiemVu();
        final String k = com.htbb.pokemon.BaoHiemRoiNhiemVu.khoa(0, 23, 5);
        int lan = 0;
        while (!b.roi(k, false, n)) {
            lan++;
        }
        Kiem.bang("truot lien tiep: lan thu 13 chac chan roi", Integer.valueOf(12), Integer.valueOf(lan));
        Kiem.bang("roi -> dat lai dem", Integer.valueOf(0), Integer.valueOf(b.dangTruot(k)));
        b.roi(k, false, n);
        b.roi(k, false, n);
        Kiem.dung("trung ngau nhien thi roi ngay va dat lai", b.roi(k, true, n) && b.dangTruot(k) == 0);
        b.roi(k, false, n);
        Kiem.bang("dem rieng theo nhiem vu + vat", Integer.valueOf(0), Integer.valueOf(b.dangTruot(com.htbb.pokemon.BaoHiemRoiNhiemVu.khoa(0, 23, 6))));
        // mo phong 20.000 vat o 15%: khong vat nao can > 13 con; trung binh <= ky vong khong bao hiem (6,7)
        final java.util.Random r = new java.util.Random(29102026L);
        final com.htbb.pokemon.BaoHiemRoiNhiemVu m = new com.htbb.pokemon.BaoHiemRoiNhiemVu();
        int maxCon = 0;
        long tong = 0;
        for (int v = 0; v < 20000; v++) {
            int con = 0;
            boolean roi = false;
            while (!roi) {
                con++;
                roi = m.roi("x", r.nextInt(1000000) < com.htbb.pokemon.BaoHiemRoiNhiemVu.TI_LE_GOC, n);
            }
            maxCon = Math.max(maxCon, con);
            tong += con;
        }
        Kiem.dung("mo phong: nhieu nhat 13 con / vat (" + maxCon + "), trung binh " + String.format("%.2f", tong / 20000.0) + " <= 6,7",
                maxCon <= 13 && tong / 20000.0 <= 6.7);
    }

    /** Noi day trong may chu (khong chay duoc khong co ban do): kiem cho goi tren nguon. */
    static void nguon() throws Exception {
        final String m = new String(java.nio.file.Files.readAllBytes(new java.io.File("src/real/Monster.java").toPath()), "UTF-8");
        final String b = new String(java.nio.file.Files.readAllBytes(new java.io.File("src/real/BossLauDai.java").toPath()), "UTF-8");
        final int attack = m.indexOf("public void attack()");
        final String danh = attack < 0 ? "" : m.substring(attack, Math.min(m.length(), attack + 3000));
        Kiem.dung("Monster.attack: boss Gym / de nham muc tieu -> quay ve nguoi thach dau (BossLauDai.chuGym)",
                danh.contains("BossLauDai.chiDanhChu(this, this.target)") && danh.contains("BossLauDai.chuGym(this)"));
        Kiem.dung("Monster.attack: luat quai chu dong truoc khi ra don (chuDongDuoc)", danh.contains("this.chuDongDuoc(this.target, now)"));
        Kiem.dung("chuDongDuoc chi ap quai hoang da thuong (lop Monster, khong boss)", m.contains("this.getClass() != Monster.class || this.isBoss"));
        Kiem.dung("Monster.update cua boss: chieu lon no dung luc het gio niem (BossLauDai.nhipNiem)", m.contains("BossLauDai.nhipNiem(this)"));
        Kiem.dung("monsterDanh: khong con xet vung o don ke tiep (noNiem + niemTrung)", b.contains("noNiem(t, bay)") && b.contains("t.niemTrung = false"));
        Kiem.dung("Monster: vat nhiem vu kieu 2 qua bao hiem xui (checkDropItemQuestBaoHiem, moi lan ha dung quai)",
                m.contains("p.checkDropItemQuestBaoHiem(this.idTemplate, Map.randomMillion() < tiLeNv") && !m.contains("p.checkDropItemQuest(this.idTemplate)"));
        Kiem.dung("Gym goi de theo gym.so_de", b.contains("LauDai.so(\"gym.so_de\", GiaiDoanBoss.GYM_SO_DE)"));
        final String g = new String(java.nio.file.Files.readAllBytes(new java.io.File("src/real/GymLauDai.java").toPath()), "UTF-8");
        Kiem.dung("thach dau Gym: HP boss theo he chieu nguoi thach dau (hpGymTheoHe + heTotNhat)",
                g.contains("GiaiDoanBoss.hpGymTheoHe(so(g[6]), heTotNhat(p, t)") && g.contains("BossLauDai.moGym(p, stt, t, so(g[3]), hp,"));
    }

    static void khoi() {
        final List<Map<String, String>> gymGoc = KiemMucTieuNhiemVu.sao(KiemMucTieuNhiemVu.dongThuong("data_noi_dung_gym"));
        final List<Map<String, String>> quai = KiemMucTieuNhiemVu.sao(KiemMucTieuNhiemVu.dongThuong("data_monster_lau_dai"));
        final List<Map<String, String>> mau = KiemMucTieuNhiemVu.sao(KiemMucTieuNhiemVu.dongThuong("data_monster"));
        final Map<String, List<Map<String, String>>> bang = new HashMap<String, List<Map<String, String>>>();
        bang.put("data_noi_dung_gym", gymGoc);
        bang.put("data_monster_lau_dai", quai);
        bang.put("data_monster", mau);
        KiemMucTieuNhiemVu.ap(DayDu.phanNangCap("sua_can_bang_cap.sql"), bang); // lan 1 truoc (gia tri cu cua khoi nay)
        final TreeMap<Integer, Integer> hpTruoc = hpTheoCap(quai);
        final String khoi = DayDu.phanNangCap("sua_can_bang_lop.sql");
        final List<Integer> lan1 = KiemMucTieuNhiemVu.ap(khoi, bang);
        int sai = 0;
        for (final Integer n : lan1) {
            sai += n.intValue() == 1 ? 0 : 1;
        }
        Kiem.bang("khoi sua_can_bang_lop.sql: 17 lenh, moi lenh khop dung mot dong", "17/0", lan1.size() + "/" + sai);
        int lai = 0;
        for (final Integer n : KiemMucTieuNhiemVu.ap(khoi, bang)) {
            lai += n.intValue();
        }
        Kiem.bang("khoi sua_can_bang_lop.sql: ap lan hai khong doi gi", Integer.valueOf(0), Integer.valueOf(lai));
        // Gym: Cerulean nhe hon nhung tan cong / HP van tang theo stt (Pewter 657 / 49100 <= Cerulean <= Vermilion)
        final List<String> nguoc = new ArrayList<String>();
        int tc0 = 0, hp0 = 0;
        for (final Map<String, String> g : gymGoc) {
            final int tc = Integer.parseInt(g.get("tan_cong")), hp = Integer.parseInt(g.get("hp"));
            if (tc < tc0 || hp < hp0) {
                nguoc.add(g.get("stt"));
            }
            tc0 = tc;
            hp0 = hp;
            if ("2".equals(g.get("stt"))) {
                Kiem.bang("Gym Cerulean hp / tan cong", "52000/660", hp + "/" + tc);
            }
        }
        Kiem.bang("Gym: tan cong / HP khong giam theo stt", "[]", nguoc.toString());
        // HP quai thuong: khong buoc > 12% giua hai cap ke nhau co quai (cap 40..90); truoc co buoc 60 -> 62
        final TreeMap<Integer, Integer> hpSau = hpTheoCap(quai);
        Kiem.dung("truoc: buoc HP 60 -> 62 > 20% (" + hpTruoc.get(Integer.valueOf(60)) + " -> " + hpTruoc.get(Integer.valueOf(62)) + ")",
                buoc(hpTruoc, 40, 90).contains("60->62"));
        Kiem.bang("sau: HP quai cap 40..90 khong buoc > 12% giua hai cap ke nhau", "[]", buoc(hpSau, 40, 90).toString());
        Kiem.bang("cap 60 / 70 giu nguyen (moc noi suy)", "10080/14030", hpSau.get(Integer.valueOf(60)) + "/" + hpSau.get(Integer.valueOf(70)));
        // Cinnabar: Croconaw / Croagunk ve cap 24 / 25, chi so + thuong = quai cung cap; duong lan 1 van khong gai
        final Map<String, String> lv = new HashMap<String, String>();
        for (final Map<String, String> d : mau) {
            lv.put(d.get("id"), d.get("level"));
        }
        Kiem.bang("Croconaw / Croagunk cap (data_monster)", "24/25", lv.get("29") + "/" + lv.get("30"));
        Kiem.bang("Crawdaunt / Farfetch'd cap (CA-06.1 cap 33 / CA-07.1 cap 37)", "34/37", lv.get("35") + "/" + lv.get("45"));
        // muc tieu cua ai giet: quai muc tieu khong cao hon cap toi thieu cua buoc qua 2 cap (cong thuc cu +70 / cap chenh)
        final Map<String, String> capTen = new HashMap<String, String>();
        for (final Map<String, String> d : mau) {
            capTen.put(d.get("name"), d.get("level"));
        }
        final List<String> qua = new ArrayList<String>();
        for (final Map<String, String> q : KiemMucTieuNhiemVu.dongThuong("data_quest_lau_dai")) {
            if (!"cua_ai".equals(q.get("loai")) || !"giet".equals(q.get("kieu_muc_tieu"))) {
                continue;
            }
            final String c = capTen.get(q.get("muc_tieu_ten"));
            final int caps = Integer.parseInt(q.get("cap_toi_thieu"));
            if (c != null && caps < 40 && Integer.parseInt(c) > caps + 2) {
                qua.add(q.get("ma") + ":" + q.get("muc_tieu_ten") + " " + c + ">" + caps);
            }
        }
        Kiem.bang("cua ai giet cap < 40: quai muc tieu <= cap buoc + 2", "[]", qua.toString());
        final Map<String, Map<String, String>> ld = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> d : quai) {
            ld.put(d.get("id"), d);
        }
        final String[] k = {"cap", "maxhp", "attack", "defent", "rcvxp", "rcvgold"};
        final StringBuilder c24 = new StringBuilder(), a24 = new StringBuilder();
        for (final String c : k) {
            c24.append(ld.get("29").get(c)).append('/');
            a24.append(ld.get("18").get(c)).append('/');
        }
        Kiem.bang("Croconaw = quai cap 24 (Ariados): chi so + EXP + xu", a24.toString(), c24.toString());
        Kiem.dung("Croagunk cap 25 nam giua cap 24 va cap 26 cu (hp " + ld.get("30").get("maxhp") + ", xu " + ld.get("30").get("rcvgold") + ")",
                Integer.parseInt(ld.get("30").get("maxhp")) > 3420 && Integer.parseInt(ld.get("30").get("maxhp")) < 3680
                        && Integer.parseInt(ld.get("30").get("rcvgold")) < 120 && Integer.parseInt(ld.get("30").get("rcvxp")) < 1453);
        Kiem.bang("duong quai cap 12..60 sau ca hai khoi: khong gai (A +<=15%, B -<=13%, khong giam)", "[]",
                KiemCanBangCapQuai.gai(KiemCanBangCapQuai.duong(quai)).toString());
    }

    /** cap -> HP quai thuong (isuse 1) dau tien o cap do. */
    static TreeMap<Integer, Integer> hpTheoCap(final List<Map<String, String>> ld) {
        final Map<String, String> dung = new HashMap<String, String>();
        for (final Map<String, String> d : DayDu.dong("data_monster")) {
            dung.put(d.get("id"), d.get("isuse"));
        }
        final TreeMap<Integer, Integer> ra = new TreeMap<Integer, Integer>();
        for (final Map<String, String> d : ld) {
            final Integer L = Integer.valueOf(d.get("cap"));
            if ("1".equals(dung.get(d.get("id"))) && !ra.containsKey(L)) {
                ra.put(L, Integer.valueOf(d.get("maxhp")));
            }
        }
        return ra;
    }

    static List<String> buoc(final TreeMap<Integer, Integer> hp, final int tu, final int den) {
        final List<String> ra = new ArrayList<String>();
        Map.Entry<Integer, Integer> truoc = null;
        for (final Map.Entry<Integer, Integer> e : hp.subMap(Integer.valueOf(tu), true, Integer.valueOf(den), true).entrySet()) {
            if (truoc != null && e.getValue().intValue() > truoc.getValue().intValue() * 1.12) {
                ra.add(truoc.getKey() + "->" + e.getKey());
            }
            truoc = e;
        }
        return ra;
    }
}
