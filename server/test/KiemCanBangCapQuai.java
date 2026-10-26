import com.htbb.pokemon.ThongSoLauDai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Duong chi so quai (data_monster_lau_dai sau khoi sua_can_bang_cap.sql) so voi suc nguoi choi do CUA HANG +0 cham mot bac
 * vu khi (nguoi choi binh thuong, bot-cap1-40 26/10/2026) + gia do cua hang (data_item sau sua_gia_do_cua_hang.sql).
 *
 * Suc nguoi choi tham chieu (do that bang bot tools/pokemon/kiem_tai/KichBanDoSuc, vu khi cua hang dung bac, khong giap):
 * tan cong ~ 30 + 12L (cap 15/20/25/30/35: 196/270/327/384/442), thu ~ 21 + L, HP ~ 4240 + 213 (L - 15). Cham mot bac vu khi
 * (moi bac 5 cap, +20 tan cong) = tan cong - 20.
 * <ul>
 *   <li>A(L) = HP quai / (tan cong nguoi choi - thu quai): so "don co ban" de ha quai (ti le voi thoi gian ha quai);</li>
 *   <li>B(L) = HP nguoi choi / don quai trung binh ((tan cong quai - thu nguoi) x 0,76): so don quai de ha nguoi choi.</li>
 * </ul>
 * Khong gai: giua hai cap quai ke nhau A tang &lt;= 15%, B giam &lt;= 13%; nguoi cham mot bac ha quai cap 20..45 trong A &lt;= 28.
 */
public final class KiemCanBangCapQuai {

    private KiemCanBangCapQuai() {
    }

    static int atkNguoi(final int L) {
        return 30 + 12 * L - 20;
    }

    static int thuNguoi(final int L) {
        return 21 + L;
    }

    static int hpNguoi(final int L) {
        return 4240 + 213 * (L - 15);
    }

    static double a(final int L, final int hp, final int thu) {
        return hp / (double) Math.max(1, atkNguoi(L) - thu);
    }

    static double b(final int L, final int atk) {
        return hpNguoi(L) / Math.max(1.0, (atk - thuNguoi(L)) * 0.76);
    }

    /** cap -> {hp, atk, def} cua quai thuong (isuse 1) dau tien o cap do, cap 12..60. */
    static TreeMap<Integer, int[]> duong(final List<Map<String, String>> ld) {
        final Map<String, String> dung = new HashMap<String, String>();
        for (final Map<String, String> d : DayDu.dong("data_monster")) {
            dung.put(d.get("id"), d.get("isuse"));
        }
        final TreeMap<Integer, int[]> ra = new TreeMap<Integer, int[]>();
        for (final Map<String, String> d : ld) {
            final int L = Integer.parseInt(d.get("cap"));
            if (L < 12 || L > 60 || !"1".equals(dung.get(d.get("id"))) || ra.containsKey(Integer.valueOf(L))) {
                continue;
            }
            ra.put(Integer.valueOf(L), new int[]{Integer.parseInt(d.get("maxhp")), Integer.parseInt(d.get("attack")),
                Integer.parseInt(d.get("defent"))});
        }
        return ra;
    }

    static List<String> gai(final TreeMap<Integer, int[]> d) {
        final List<String> loi = new ArrayList<String>();
        Map.Entry<Integer, int[]> truoc = null;
        for (final Map.Entry<Integer, int[]> e : d.entrySet()) {
            final int L = e.getKey().intValue();
            final int[] q = e.getValue();
            if (truoc != null) {
                final int L0 = truoc.getKey().intValue();
                final int[] q0 = truoc.getValue();
                final double ra = a(L, q[0], q[2]) / a(L0, q0[0], q0[2]);
                final double rb = b(L, q[1]) / b(L0, q0[1]);
                if (ra > 1.15) {
                    loi.add("A " + L0 + "->" + L + " x" + String.format("%.2f", ra));
                }
                if (rb < 0.87) {
                    loi.add("B " + L0 + "->" + L + " x" + String.format("%.2f", rb));
                }
                if (q[2] < q0[2] || q[0] < q0[0] || q[1] < q0[1]) {
                    loi.add("giam " + L0 + "->" + L);
                }
            }
            truoc = e;
        }
        return loi;
    }

    public static void chay() {
        // ---- quai
        final List<Map<String, String>> goc = KiemMucTieuNhiemVu.dongThuong("data_monster_lau_dai");
        final List<Map<String, String>> moi = KiemMucTieuNhiemVu.sao(goc);
        final String khoi = DayDu.phanNangCap("sua_can_bang_cap.sql");
        final Map<String, List<Map<String, String>>> bang = new HashMap<String, List<Map<String, String>>>();
        bang.put("data_monster_lau_dai", moi);
        final List<Integer> lan1 = KiemMucTieuNhiemVu.ap(khoi, bang);
        int sai = 0;
        for (final Integer n : lan1) {
            sai += n.intValue() == 1 ? 0 : 1;
        }
        Kiem.dung("khoi sua_can_bang_cap.sql co lenh (" + lan1.size() + ")", lan1.size() >= 30);
        Kiem.bang("khoi quai: moi lenh khop dung mot dong (3 gia tri cu dung)", Integer.valueOf(0), Integer.valueOf(sai));
        int lai = 0;
        for (final Integer n : KiemMucTieuNhiemVu.ap(khoi, bang)) {
            lai += n.intValue();
        }
        Kiem.bang("khoi quai: ap lan hai khong doi gi", Integer.valueOf(0), Integer.valueOf(lai));
        final TreeMap<Integer, int[]> truoc = duong(goc);
        final TreeMap<Integer, int[]> sau = duong(moi);
        Kiem.dung("duong quai cap 12..60 du diem (" + sau.size() + ")", sau.size() >= 35);
        final List<String> gaiCu = gai(truoc);
        Kiem.dung("bang cu co gai (cap 30 -> 31: " + gaiCu + ")", gaiCu.toString().contains("30->31"));
        Kiem.bang("sau chinh: khong gai giua hai cap ke nhau (A +<=15%, B -<=13%, chi so khong giam)", "[]", gai(sau).toString());
        final List<String> cham = new ArrayList<String>();
        final List<String> chet = new ArrayList<String>();
        for (final Map.Entry<Integer, int[]> e : sau.entrySet()) {
            final int L = e.getKey().intValue();
            final int[] q = e.getValue();
            if (L >= 20 && L <= 45 && a(L, q[0], q[2]) > 28) {
                cham.add(L + ":" + String.format("%.1f", a(L, q[0], q[2])));
            }
            if (L <= 45 && b(L, q[1]) < 10) {
                chet.add(L + ":" + String.format("%.1f", b(L, q[1])));
            }
        }
        Kiem.bang("nguoi cham mot bac vu khi ha quai cap 20..45 trong <= 28 don co ban", "[]", cham.toString());
        Kiem.bang("quai cap 12..45 can >= 10 don de ha nguoi choi do cua hang", "[]", chet.toString());
        final int[] g20 = truoc.get(Integer.valueOf(20));
        Kiem.dung("bang cu: Growlithe 20 qua cham voi nguoi choi do cham bac (A > 28)", g20 != null && a(20, g20[0], g20[2]) > 28);
        // quai cap >= 60 / boss giu nguyen
        int doiNgoai = 0;
        for (int i = 0; i < goc.size(); i++) {
            if (Integer.parseInt(goc.get(i).get("cap")) >= 60 && !goc.get(i).equals(moi.get(i))) {
                doiNgoai++;
            }
        }
        Kiem.bang("quai cap >= 60 giu nguyen", Integer.valueOf(0), Integer.valueOf(doiNgoai));

        // ---- gia do cua hang
        final List<Map<String, String>> item = KiemMucTieuNhiemVu.sao(KiemMucTieuNhiemVu.dongThuong("data_item"));
        final Map<String, List<Map<String, String>>> bi = new HashMap<String, List<Map<String, String>>>();
        bi.put("data_item", item);
        final String kg = DayDu.phanNangCap("sua_gia_do_cua_hang.sql");
        final List<Integer> g1 = KiemMucTieuNhiemVu.ap(kg, bi);
        int sg = 0;
        for (final Integer n : g1) {
            sg += n.intValue() == 1 ? 0 : 1;
        }
        Kiem.dung("khoi sua_gia_do_cua_hang.sql co lenh (" + g1.size() + ")", g1.size() >= 50);
        Kiem.bang("khoi gia: moi lenh khop dung mot dong", Integer.valueOf(0), Integer.valueOf(sg));
        int gl = 0;
        for (final Integer n : KiemMucTieuNhiemVu.ap(kg, bi)) {
            gl += n.intValue();
        }
        Kiem.bang("khoi gia: ap lan hai khong doi gi", Integer.valueOf(0), Integer.valueOf(gl));
        final List<String> dat = new ArrayList<String>();
        final Map<Integer, TreeMap<Integer, Integer>> theoLoai = new HashMap<Integer, TreeMap<Integer, Integer>>();
        for (final Map<String, String> d : item) {
            final int id = Integer.parseInt(d.get("id"));
            final int t = Integer.parseInt(d.get("type"));
            final int L = Integer.parseInt(d.get("level"));
            final long gia = Long.parseLong(d.get("price"));
            if (id < 3 || id > 213 || gia < 500 || t > 12 || L < 1 || L > 99) {
                continue;
            }
            final double he = t >= 3 && t <= 7 ? 2.0 : 1.5;
            if (gia > he * ThongSoLauDai.xuMoiGio(L) + 100) {
                dat.add(id + "@" + L + ":" + gia);
            }
            final TreeMap<Integer, Integer> m = theoLoai.computeIfAbsent(Integer.valueOf(t * 10 + (t >= 3 && t <= 7 ? 0 : id % 2)),
                    k -> new TreeMap<Integer, Integer>());
            final Integer c = m.get(Integer.valueOf(L));
            m.put(Integer.valueOf(L), Integer.valueOf(Math.max(c == null ? 0 : c.intValue(), (int) gia)));
        }
        Kiem.bang("do cua hang: vu khi <= 2 gio, giap / phu kien <= 1,5 gio thu nhap o cap mon", "[]", dat.toString());
        final List<String> nguoc = new ArrayList<String>();
        for (final Map.Entry<Integer, TreeMap<Integer, Integer>> e : theoLoai.entrySet()) {
            int truocGia = 0;
            for (final Map.Entry<Integer, Integer> x : e.getValue().entrySet()) {
                if (x.getValue().intValue() < truocGia) {
                    nguoc.add(e.getKey() + "@" + x.getKey());
                }
                truocGia = x.getValue().intValue();
            }
        }
        Kiem.bang("gia do cua hang khong giam khi cap mon tang (cung loai)", "[]", nguoc.toString());
        long tien = 0;
        for (final int id : new int[]{82, 83, 84, 85, 174}) {
            for (final Map<String, String> d : item) {
                if (Integer.parseInt(d.get("id")) == id) {
                    tien += Long.parseLong(d.get("price"));
                }
            }
        }
        Kiem.dung("vu khi bac 16..36 (5 mon) tong <= 150.000 xu (truoc 325.000) - " + tien, tien <= 150000);
    }
}
