import com.htbb.pokemon.ChiSoDong;
import com.htbb.pokemon.ChieuPokemon;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.HuanLuyen;
import com.htbb.pokemon.LoaiPokemon;
import com.htbb.pokemon.NangCapLauDai;
import com.htbb.pokemon.ThongSoLauDai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * TTK QUAI THUONG / GYM THEO SUC NGUOI CHOI DO CUA HANG (chu game 31/10/2026 "fix" sau can bang dong: "ha quai cung cap qua
 * cham, 13-40 giay o cap 30, 24-100 giay o cap 72 voi vu khi +0").
 * <p>
 * Do lai tren stack rieng (bot tools/pokemon/kiem_tai/KichBanDoSuc, cot moi {@code ttk_tran} = giay DANH THUAN moi con, tu
 * don dau toi khi quai guc, khong tinh di tim / di bo / hoi sinh): con so 13-40 / 24-100 giay la cot {@code giay_moi_con} (CA
 * di tim quai, di bo, uong thuoc, guc - bot dung sau 120 don khong ha duoc), KHONG phai TTK. TTK that voi do cua hang +0:
 * 2,6-4 giay o cap 20-40, 5-7 giay o cap 50-85 (bang o docs/thiet-ke-game.md muc 2.4) = dung muc thiet ke 4 / 5 / 6 giay.
 * Duong chi so quai (data_monster_lau_dai sau khoi sua_can_bang_cap / sua_can_bang_lop + quai Hoenn 139..174) vi vay GIU
 * NGUYEN; test nay khoa no bang mo hinh TTK da doi chieu voi may chu:
 * <ul>
 *   <li>tan cong nguoi choi = (ChiSoDong.tanCong + vu khi bac cua hang x (1 + % nang cap) + huy hieu + vong co) x 1,125 (do
 *       mua co +0..25% ngau nhien, Map.rand10) x (1 + % giai doan tien hoa) x (1 + diem huan luyen 1/3 vao Tan cong) - lech
 *       so may chu tra (goi chi so) &lt;= 3% o cap 20-50 (do 31/10/2026);</li>
 *   <li>TTK = HP quai / (K x max(5% tan cong, tan cong - thu quai)), K = sat thuong moi giay / don co ban cua vong xoay chieu
 *       do that: 2,5-3,6 (Charmander, cham nhat) .. 4,7-6,2 (Eevee / Bulbasaur). Mo hinh dung K CHAM = 3,0 cho tran tren
 *       (khong dong nao cham hon) va K NHANH = 6,0 cho tran duoi (khong dong nao "mot don").</li>
 * </ul>
 * Kiem: moi dong x cap 10..100 x vu khi +0 / +6 / +10 (quai thuong dung cap, trung tinh he); Gym 16 truong Gym (cua so
 * thiet ke 90-120 giay danh mot minh dung cap); quai tinh anh HP x4; khac he cuc doan tren buoc giet bat buoc (bao chu game).
 */
public final class KiemTtkQuai {

    private KiemTtkQuai() {
    }

    /**
     * He so xoay chieu do that theo dong (sat thuong moi giay / don co ban, da bo he so he): trung vi cac lan do 31/10/2026
     * (stack rieng, KichBanDoSuc cot ttk_tran), Charmander Bulbasaur Eevee Squirtle Pichu.
     */
    static final double[] K_DONG = {3.0, 4.5, 5.0, 4.0, 4.0};
    /** Do cua hang co +0..25% ngau nhien (Map.rand10): ky vong +12,5%. */
    static final double NGAU_NHIEN_DO = 1.125;
    static final String[] TEN = {"Charmander", "Bulbasaur", "Eevee", "Squirtle", "Pichu"};

    /** TTK thiet ke quai thuong (docs muc 2.3): 4 / 5 / 6 giay o cap 1-30 / 31-60 / 61-100. */
    static double mucTieu(final int cap) {
        return cap <= 30 ? 4.0 : cap <= 60 ? 5.0 : 6.0;
    }

    public static void chay() {
        Kiem.nhom("TTK quai thuong / Gym theo suc nguoi choi do cua hang +0 / +6 / +10 (chu game 31/10/2026, do lai giay danh thuan)");
        try {
            final Bac bac = Bac.nap();
            Kiem.dung("bang do cua hang: vu khi 15 bac, huy hieu / vong co toi cap 79 (" + bac.vuKhi.size() + "/" + bac.huyHieu.size()
                    + "/" + bac.vongCo.size() + ")", bac.vuKhi.size() >= 15 && bac.huyHieu.size() >= 15 && bac.vongCo.size() >= 15);
            moHinhNguoi(bac);
            final TreeMap<Integer, double[]> quai = quaiThuong();
            Kiem.dung("duong quai thuong cap 1..100 (" + quai.size() + " cap)", quai.size() >= 60 && quai.firstKey() <= 10
                    && quai.lastKey() >= 100);
            ttkQuai(bac, quai);
            gym(bac);
            tinhAnh();
            khacHe();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("KiemTtkQuai khong nem loi: " + e, false);
        }
    }

    // ------------------------------------------------------------------ suc nguoi choi

    /** Bac do cua hang: cap mon -> chi so tan cong (atb0). */
    static final class Bac {
        final TreeMap<Integer, Double> vuKhi = new TreeMap<Integer, Double>();
        final TreeMap<Integer, Integer> huyHieu = new TreeMap<Integer, Integer>();
        final TreeMap<Integer, Integer> vongCo = new TreeMap<Integer, Integer>();

        static Bac nap() {
            final Bac b = new Bac();
            // vu khi tiem (Machamp 2 / 28: 79..113 cap 1-31, 174..213 cap 36-71) - trung binh 5 dong (ChiSoDong.lechVuKhi dua
            // moi dong ve trung binh)
            final Map<Integer, double[]> vk = new TreeMap<Integer, double[]>();
            final int[] giap = gop(real.NpcTinhBanDo.dsBan(3, 0), real.NpcTinhBanDo.dsBan(27, 201));
            final int[] ren = gop(real.NpcTinhBanDo.dsBan(2, 0), real.NpcTinhBanDo.dsBan(28, 201));
            for (final Map<String, String> d : DayDu.dong("data_item")) {
                final int id = so(d.get("id")), type = so(d.get("type")), cap = so(d.get("level")), a0 = so(d.get("atb0"));
                if (type >= 3 && type <= 7 && trong(ren, id) && (id <= 113 || id >= 174) && a0 > 0) {
                    double[] s = vk.get(Integer.valueOf(cap));
                    if (s == null) {
                        vk.put(Integer.valueOf(cap), s = new double[2]);
                    }
                    s[0] += a0;
                    s[1]++;
                } else if ((type == 8 || type == 9) && so(d.get("clazz")) == -1 && so(d.get("price")) >= 500 && trong(giap, id) && a0 > 0) {
                    final TreeMap<Integer, Integer> m = type == 8 ? b.huyHieu : b.vongCo;
                    final Integer cu = m.get(Integer.valueOf(cap));
                    m.put(Integer.valueOf(cap), Integer.valueOf(Math.max(a0, cu == null ? 0 : cu.intValue())));
                }
            }
            for (final Map.Entry<Integer, double[]> e : vk.entrySet()) {
                if (e.getValue()[1] >= 5) {
                    b.vuKhi.put(e.getKey(), Double.valueOf(e.getValue()[0] / e.getValue()[1]));
                }
            }
            return b;
        }

        static <T> double sau(final TreeMap<Integer, T> m, final int cap) {
            final Map.Entry<Integer, T> e = m.floorEntry(Integer.valueOf(cap));
            return e == null ? 0 : ((Number) e.getValue()).doubleValue();
        }
    }

    static int[] gop(final int[] a, final int[] b) {
        final int[] r = new int[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    static boolean trong(final int[] doan, final int id) {
        for (int k = 0; k + 1 < doan.length; k += 2) {
            if (id >= doan[k] && id <= doan[k + 1]) {
                return true;
            }
        }
        return false;
    }

    static int so(final String s) {
        try {
            return s == null || s.isEmpty() ? 0 : Integer.parseInt(s.trim());
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    /** % chi so mon o +p (NangCapLauDai, bang data_nang_cap_lau_dai; 3%/bac toi +10 neu chua nap). */
    static int phanTramNangCap(final int p) {
        return ThongSoLauDai.CHI_SO_NC_PHAN_TRAM.length >= 15 ? NangCapLauDai.tongChiSoPhanTram(p)
                : Math.min(p, 10) * 3 + Math.max(0, p - 10) * 5;
    }

    /** Phan nghin Tan cong tu diem huan luyen: 2 diem / cap, 1/3 vao Tan cong (40 diem dau 3 phan nghin, sau 2; tran 80). */
    static int huanLuyen(final int cap) {
        final HuanLuyen.Dong tc = new HuanLuyen.Dong(1, HuanLuyen.TAN_CONG, "Tấn công", "", 80, 40, 3, 2, 1);
        return HuanLuyen.phanNghin(tc, (2 * (cap - 1) + 2) / 3);
    }

    /** Tan cong nguoi choi dong {@code dong} cap {@code cap}, vu khi bac cua hang +p, huy hieu + vong co bac cua hang. */
    static double tanCong(final Bac bac, final int dong, final int cap, final int p) {
        final double doMua = Bac.sau(bac.vuKhi, cap) * (100 + phanTramNangCap(p)) / 100.0 + Bac.sau(bac.huyHieu, cap)
                + Bac.sau(bac.vongCo, cap);
        final int gd = LoaiPokemon.phanTramGiaiDoan(LoaiPokemon.dangTheoCap(LoaiPokemon.KHOI_DAU_THEO_CLASS[dong], cap));
        return (ChiSoDong.tanCong(dong, cap) + doMua * NGAU_NHIEN_DO) * (1 + gd / 100.0) * (1 + huanLuyen(cap) / 1000.0);
    }

    static void moHinhNguoi(final Bac bac) {
        // so may chu tra (goi chi so, do suc 31/10/2026 stack rieng, do cua hang +0, diem huan luyen chia deu): cap 30
        // Charmander 531 / Bulbasaur 490 / Eevee 480, cap 40 Charmander 750 / Bulbasaur 693, cap 50 Charmander 942
        final int[][] doThat = {{30, 0, 531}, {30, 1, 490}, {30, 2, 480}, {40, 0, 750}, {40, 1, 693}, {50, 0, 942}};
        final StringBuilder lech = new StringBuilder();
        for (final int[] d : doThat) {
            final double m = tanCong(bac, d[1], d[0], 0);
            if (Math.abs(m / d[2] - 1) > 0.04) {
                lech.append(' ').append(TEN[d[1]]).append('@').append(d[0]).append(String.format(Locale.ROOT, "=%.0f/%d", m, d[2]));
            }
        }
        Kiem.dung("mo hinh tan cong nguoi choi do cua hang +0 khop so may chu tra (do suc) +-4%" + lech, lech.length() == 0);
        Kiem.dung("+6 vu khi = +18% chi so vu khi (NangCapLauDai)", phanTramNangCap(6) == 18 && phanTramNangCap(10) == 30);
        final double t0 = tanCong(bac, 2, 72, 0), t6 = tanCong(bac, 2, 72, 6);
        Kiem.dung(String.format(Locale.ROOT, "cap 72: +6 chi them %.1f%% tan cong (vu khi la ~1/3 tan cong)", (t6 / t0 - 1) * 100),
                t6 > t0 && t6 / t0 < 1.10);
    }

    // ------------------------------------------------------------------ quai

    /** cap -> {hp, tan cong, thu} quai thuong (isuse 1) chay that: data_monster_lau_dai sau 2 khoi can bang + quai Hoenn 139..174. */
    static TreeMap<Integer, double[]> quaiThuong() {
        final Map<String, List<Map<String, String>>> bang = trangThaiCuoi();
        final Map<String, Map<String, String>> mon = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> d : bang.get("data_monster")) {
            mon.put(d.get("id"), d);
        }
        final TreeMap<Integer, double[]> ra = new TreeMap<Integer, double[]>();
        for (final Map<String, String> d : bang.get("data_monster_lau_dai")) {
            final Map<String, String> m = mon.get(d.get("id"));
            if (m != null && "1".equals(m.get("isuse"))) {
                them(ra, so(d.get("cap")), so(d.get("maxhp")), so(d.get("attack")), so(d.get("defent")));
            }
        }
        for (final Map<String, String> m : bang.get("data_monster")) {
            final int id = so(m.get("id"));
            if (id >= 139 && id <= 174 && "1".equals(m.get("isuse"))) {
                them(ra, so(m.get("level")), so(m.get("maxhp")), so(m.get("attack")), so(m.get("defent")));
            }
        }
        return ra;
    }

    /** Hai quai cung cap: giu con cung (HP / thu lon hon) - tran tren TTK. */
    static void them(final TreeMap<Integer, double[]> ra, final int cap, final int hp, final int atk, final int thu) {
        final double[] cu = ra.get(Integer.valueOf(cap));
        if (cu == null || hp > cu[0] || hp == cu[0] && thu > cu[2]) {
            ra.put(Integer.valueOf(cap), new double[]{hp, atk, thu});
        }
    }

    /** Bang quai / Gym SAU cac khoi phan B dang chay (sua_can_bang_cap, sua_can_bang_lop). */
    static Map<String, List<Map<String, String>>> cuoi;

    static synchronized Map<String, List<Map<String, String>>> trangThaiCuoi() {
        if (cuoi != null) {
            return cuoi;
        }
        final Map<String, List<Map<String, String>>> bang = new HashMap<String, List<Map<String, String>>>();
        for (final String t : new String[]{"data_monster", "data_monster_lau_dai", "data_noi_dung_gym", "data_npc"}) {
            bang.put(t, KiemMucTieuNhiemVu.sao(KiemMucTieuNhiemVu.dongThuong(t)));
        }
        for (final String khoi : new String[]{"sua_can_bang_cap.sql", "sua_can_bang_lop.sql"}) {
            KiemMucTieuNhiemVu.ap(DayDu.phanNangCap(khoi), bang);
        }
        cuoi = bang;
        return bang;
    }

    /** Quai thuong dung cap: con cap cao nhat &lt;= cap nguoi choi (nhu bot do suc chon). */
    static double[] dungCap(final TreeMap<Integer, double[]> quai, final int cap) {
        final Map.Entry<Integer, double[]> e = quai.floorEntry(Integer.valueOf(cap));
        return e == null ? null : e.getValue();
    }

    static double ttk(final double hp, final double thu, final double atk, final double k) {
        return hp / (k * Math.max(0.05 * atk, atk - thu));
    }

    static void ttkQuai(final Bac bac, final TreeMap<Integer, double[]> quai) {
        final List<String> cham = new ArrayList<String>(), nhanh = new ArrayList<String>(), motDon = new ArrayList<String>();
        final List<String> nangCap = new ArrayList<String>(), gai = new ArrayList<String>();
        final StringBuilder bangIn = new StringBuilder();
        double t0Max = 0, t0Min = 99;
        for (int cap = 10; cap <= 100; cap++) {
            final double[] q = dungCap(quai, cap);
            final double mt = mucTieu(cap);
            final StringBuilder dong = new StringBuilder();
            for (int d = 0; d < 5; d++) {
                final double a0 = tanCong(bac, d, cap, 0), a6 = tanCong(bac, d, cap, 6), a10 = tanCong(bac, d, cap, 10);
                final double t0 = ttk(q[0], q[2], a0, K_DONG[d]), t6 = ttk(q[0], q[2], a6, K_DONG[d]), t10 = ttk(q[0], q[2], a10, K_DONG[d]);
                t0Max = Math.max(t0Max, t0);
                t0Min = Math.min(t0Min, t0);
                if (t0 > 1.5 * mt) {
                    cham.add(TEN[d] + "@" + cap + String.format(Locale.ROOT, "=%.1f", t0));
                }
                if (t0 < 0.4 * mt) {
                    nhanh.add(TEN[d] + "@" + cap + String.format(Locale.ROOT, "=%.1f", t0));
                }
                // tran duoi: vong xoay nhanh nhat do duoc (K x 1,5) van >= 1,2 giay (>= 2 don thuong) o +10
                if (t10 / 1.5 < 1.2) {
                    motDon.add(TEN[d] + "@" + cap + String.format(Locale.ROOT, "=%.1f", t10 / 1.5));
                }
                if (t6 > 0.95 * t0 || t10 > 0.92 * t0) {
                    nangCap.add(TEN[d] + "@" + cap);
                }
                if (cap % 10 == 0) {
                    dong.append(String.format(Locale.ROOT, " %4.1f/%4.1f/%4.1f", t0, t6, t10));
                }
            }
            if (cap % 10 == 0) {
                bangIn.append(String.format(Locale.ROOT, "   cap %3d (quai HP %6.0f thu %4.0f, muc tieu %.0f s):%s%n", cap, q[0], q[2], mt, dong));
            }
        }
        // khong gai giua hai cap quai ke nhau (nguoi choi dung cap moi con, dong chuan Eevee): cham hon <= 20%
        Map.Entry<Integer, double[]> truoc = null;
        for (final Map.Entry<Integer, double[]> e : quai.tailMap(Integer.valueOf(10), true).entrySet()) {
            if (truoc != null) {
                final int c0 = truoc.getKey().intValue(), c1 = e.getKey().intValue();
                final double r = ttk(e.getValue()[0], e.getValue()[2], tanCong(bac, 2, c1, 0), K_DONG[2])
                        / ttk(truoc.getValue()[0], truoc.getValue()[2], tanCong(bac, 2, c0, 0), K_DONG[2]);
                if (r > 1.20) {
                    gai.add(c0 + "->" + c1 + String.format(Locale.ROOT, " x%.2f", r));
                }
            }
            truoc = e;
        }
        System.out.print("   TTK mo hinh (giay danh thuan, +0 / +6 / +10; Charmander Bulbasaur Eevee Squirtle Pichu):\n" + bangIn);
        Kiem.bang("+0, moi dong, cap 10..100: TTK <= 1,5 x muc tieu 4 / 5 / 6 giay (khong dong nao ha quai qua cham)", "[]", cham.toString());
        Kiem.bang("+0, moi dong, cap 10..100: TTK >= 0,4 x muc tieu (quai khong qua mong)", "[]", nhanh.toString());
        Kiem.bang("+10, moi dong, cap 10..100: TTK (vong xoay nhanh nhat) >= 1,2 giay (nang cap khong bien quai thanh mot don)", "[]",
                motDon.toString());
        Kiem.bang("+6 nhanh hon +0 >= 5%, +10 >= 8% (nang cap co y nghia) moi dong moi cap", "[]", nangCap.toString());
        Kiem.bang("duong TTK khong gai: quai cap ke tiep (nguoi choi dung cap) cham hon <= 20%", "[]", gai.toString());
        Kiem.dung(String.format(Locale.ROOT, "TTK +0 moi dong trong %.1f..%.1f giay (thiet ke 4 / 5 / 6; '13-40 / 24-100 giay' la giay_moi_con co di tim / di bo)",
                t0Min, t0Max), t0Max <= 9.0 && t0Min >= 1.6);
    }

    // ------------------------------------------------------------------ Gym, tinh anh

    static void gym(final Bac bac) {
        final List<Map<String, String>> gym = trangThaiCuoi().get("data_noi_dung_gym");
        final List<String> ngoai = new ArrayList<String>();
        final StringBuilder in = new StringBuilder();
        for (final Map<String, String> g : gym) {
            final int cap = so(g.get("cap"));
            final double hp = so(g.get("hp")), thu = so(g.get("phong_thu"));
            double lo = 1e9, hi = 0;
            for (int d = 0; d < 5; d++) {
                final double a = tanCong(bac, d, cap, 0);
                lo = Math.min(lo, ttk(hp, thu, a, K_DONG[d]));
                hi = Math.max(hi, ttk(hp, thu, a, K_DONG[d]));
            }
            in.append(String.format(Locale.ROOT, " %s:%.0f-%.0f", g.get("stt"), lo, hi));
            // cua so thiet ke 90-120 giay (do +6) -> do +0 cua hang, trung tinh he: dong nhanh nhat >= 45 giay, cham nhat <= 180
            if (lo < 45 || hi > 180) {
                ngoai.add(g.get("stt") + ":" + Math.round(lo) + "-" + Math.round(hi));
            }
        }
        System.out.println("   Gym stt:giay (+0, dong nhanh nhat .. cham nhat, trung tinh he):" + in);
        Kiem.dung("du 16 Gym", gym.size() == 16);
        Kiem.bang("Gym: TTK +0 dung cap trong 45..180 giay (khong bi tam thuong hoa, khong qua 10 phut)", "[]", ngoai.toString());
    }

    /**
     * Quai tinh anh (NoiDungLauDai.khiHoiSinh: maxhp = hp, attack = tan_cong cua data_noi_dung_quai_tinh_anh; thu = template):
     * khoi sua_quai_tinh_anh.sql dua moi dong ve hp = 4 x, tan_cong = 1,3 x quai thuong DANG CHAY (sau sua_can_bang_cap /
     * sua_can_bang_lop), phong_thu = thu template.
     */
    static void tinhAnh() {
        final Map<String, List<Map<String, String>>> bang = new HashMap<String, List<Map<String, String>>>();
        final List<Map<String, String>> goc = KiemMucTieuNhiemVu.dongThuong("data_noi_dung_quai_tinh_anh");
        final List<Map<String, String>> ta = KiemMucTieuNhiemVu.sao(goc);
        bang.put("data_noi_dung_quai_tinh_anh", ta);
        final String khoi = DayDu.phanNangCap("sua_quai_tinh_anh.sql");
        final List<Integer> lan1 = KiemMucTieuNhiemVu.ap(khoi, bang);
        int sai = 0;
        for (final Integer n : lan1) {
            sai += n.intValue() == 1 ? 0 : 1;
        }
        Kiem.dung("khoi sua_quai_tinh_anh.sql co lenh (" + lan1.size() + ")", lan1.size() >= 40);
        Kiem.bang("khoi quai tinh anh: moi lenh khop dung mot dong (3 gia tri cu dung)", Integer.valueOf(0), Integer.valueOf(sai));
        int lai = 0;
        for (final Integer n : KiemMucTieuNhiemVu.ap(khoi, bang)) {
            lai += n.intValue();
        }
        Kiem.bang("khoi quai tinh anh: ap lan hai khong doi gi", Integer.valueOf(0), Integer.valueOf(lai));
        final Map<String, Map<String, String>> thuong = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> d : trangThaiCuoi().get("data_monster_lau_dai")) {
            thuong.put(d.get("id"), d);
        }
        final List<String> lechTruoc = new ArrayList<String>(), lech = new ArrayList<String>();
        int n = 0;
        for (int i = 0; i < ta.size(); i++) {
            final Map<String, String> q = thuong.get(ta.get(i).get("id_goc"));
            if (q == null) {
                continue;
            }
            n++;
            final int hp = so(q.get("maxhp")), atk = so(q.get("attack")), thu = so(q.get("defent"));
            final String moi = so(ta.get(i).get("hp")) + "/" + so(ta.get(i).get("tan_cong")) + "/" + so(ta.get(i).get("phong_thu"));
            final String mong = 4 * hp + "/" + (atk * 13 + 5) / 10 + "/" + thu;
            if (!mong.equals(moi)) {
                lech.add(ta.get(i).get("id_goc") + ":" + moi + "!=" + mong);
            }
            if (so(goc.get(i).get("tan_cong")) > (atk * 13 + 5) / 10 * 1.05) {
                lechTruoc.add(ta.get(i).get("id_goc"));
            }
        }
        Kiem.dung("quai tinh anh co dong (" + n + ")", n >= 60);
        Kiem.dung("truoc khoi: tinh anh cap 13-59 danh manh hon x1,3 quai thuong dang chay (" + lechTruoc.size() + " dong, Arbok 1117 / 627)",
                lechTruoc.size() >= 20 && lechTruoc.contains("23"));
        Kiem.bang("sau khoi: moi quai tinh anh = 4 x HP, 1,3 x tan cong, thu template cua quai thuong dang chay", "[]", lech.toString());
    }

    // ------------------------------------------------------------------ khac he cuc doan

    /**
     * Buoc giet bat buoc (chinh / cua ai): moi dong, he so DPS (vong xoay chieu, trong so % / hoi chieu, STAB) vao he quai va he
     * so don quai vao minh. In cac buoc ma sat thuong NHAN moi con (= vao / ra) >= x2,5 so voi trung tinh - bao chu game.
     */
    static void khacHe() {
        final List<Map<String, String>> chieu = KiemMucTieuNhiemVu.dongThuong("data_noi_dung_chieu");
        final Map<String, Map<String, String>> mon = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> m : DayDu.dong("data_monster")) {
            mon.put(m.get("id"), m);
        }
        final List<String> xau = new ArrayList<String>();
        boolean pichuDat = false;
        int buoc = 0;
        for (final Map<String, String> q : KiemMucTieuNhiemVu.dongThuong("data_quest_lau_dai")) {
            final String loai = q.get("loai"), kieu = q.get("kieu_muc_tieu");
            if (!("chinh".equals(loai) || "cua_ai".equals(loai)) || !("giet".equals(kieu) || "giet_tinh_anh".equals(kieu))) {
                continue;
            }
            final Map<String, String> m = mon.get(q.get("muc_tieu_id"));
            if (m == null) {
                continue;
            }
            buoc++;
            final int cap = so(q.get("cap_toi_thieu")), h1 = so(m.get("he")), h2 = m.get("he2") == null || m.get("he2").isEmpty() ? -1 : so(m.get("he2"));
            for (int d = 0; d < 5; d++) {
                final int dex = LoaiPokemon.dangTheoCap(LoaiPokemon.KHOI_DAU_THEO_CLASS[d], cap);
                final int p1 = LoaiPokemon.he1(dex), p2 = LoaiPokemon.he2(dex);
                double w = 0, wf = 0;
                for (final Map<String, String> c : chieu) {
                    final String k = c.get("kieu");
                    if (so(c.get("dong")) != d || so(c.get("cap_mo")) > cap
                            || !("thuong".equals(k) || "tan_cong".equals(k) || "nhieu_don".equals(k) || "toi_thuong".equals(k) || "khong_che".equals(k))) {
                        continue;
                    }
                    final int hc = ChieuPokemon.heChieu(d, so(c.get("so_hieu")));
                    final double hoi = Double.parseDouble(c.get("hoi_chieu_giay"));
                    if (hc < 0 || hoi <= 0) {
                        continue;
                    }
                    final double ww = so(c.get("phan_tram_cap10")) / hoi * ChieuPokemon.heSoStab(hc, p1, p2) / 100.0;
                    w += ww;
                    wf += ww * HePokemon.heSoTrongGame(hc, h1, h2) / 100.0;
                }
                final double ra = w == 0 ? 1 : wf / w, vao = HePokemon.heSoTrongGame(h1, p1, p2) / 100.0;
                if (vao / ra >= 2.5) {
                    xau.add(q.get("ma") + "(" + cap + " " + m.get("name") + ")/" + TEN[d] + String.format(Locale.ROOT, " ra x%.2f vao x%.1f", ra, vao));
                    pichuDat |= d == 4 && ra <= 0.31;
                }
            }
        }
        System.out.println("   Buoc giet bat buoc nhan >= x2,5 sat thuong moi con so voi trung tinh (bao chu game): " + xau);
        Kiem.dung("co buoc giet bat buoc chinh / cua ai (" + buoc + ")", buoc >= 50);
        // Pichu chi co chieu Dien (o 0..8, ke ca don thuong) -> quai Dat (x0,3) la buoc kho nhat game; Nhanh nhen (Sieu linh) la buff
        Kiem.dung("rui ro da biet (bao chu game): Pichu chi co chieu Dien, quai Dat x0,3 (Golem / Rhydon cua ai CA-09.1 / CA-10.1)", pichuDat);
    }
}
