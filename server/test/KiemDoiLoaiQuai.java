import com.htbb.pokemon.DuongQuaiChuan;
import com.htbb.pokemon.HeSinhThaiGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import real.Monster;
import real.MonsterTemplate;

/**
 * QUAI DOI LOAI KHI HOI SINH GIU CHI SO CUA BAI (do suc 31/10/2026, stack rieng: o Goldenrod - ban do 18, cua ai CA-09.1 Golem
 * 51 - nguoi choi cap 49 Charmander / Pichu do +0 an don 3.000-8.000, guc sau 2-3 don).
 * <p>
 * Nguyen nhan: he sinh thai (com.htbb.pokemon.HeSinhThaiGame.khiHoiSinh: thay loai theo thoi tiet / gio data_hst_thay_quai,
 * quai hiem, bung phat data_hst_bung_phat, ngay cong dong) doi template cua quai khi hoi sinh va lay HET chi so cua template
 * moi; NoiDungLauDai.khiHoiSinh dat lai HP / tan cong theo template moi. Nhieu template thay the la quai cap cao con chi so
 * goc cu (khong co dong data_monster_lau_dai): Golem 51 / Breloom 52 -> Liepard 57 ("Liepard" bot thay), Krokorok 60,
 * Feraligatr 62 (HP 160.000-200.000, tan cong ~3.050, thu ~720), Hoenn cap 82-92 -> Ursaluna 90 (tan cong 75.000), Vileplume -> Liepard Shiny 82 (HP 1.000.000, tan cong 26.000), Kingler -> Trevenant chi so cu (tan cong 17.000)...
 * cao hon nguoi choi 9+ cap nen them luat chenh cap (LiveActor.attackDam: x2, nua thu).
 * <p>
 * Sua: loai / hinh / he theo template moi, nhung cap + HP + tan cong + thu + chinh xac / ne + EXP + xu theo mau GOC cua o
 * sinh (HeSinhThaiGame.doiLoai / mauChiSo; NoiDungLauDai va Monster dung mauChiSo).
 * <p>
 * Quai hiem / bung phat / ngay cong dong (chu game 31/10/2026): cap goc + 2, HP / tan cong / thu / EXP / xu theo duong quai
 * chuan o cap do (DuongQuaiChuan, noi suy data_monster_lau_dai + Hoenn) - khong theo chi so goc cu cua loai; thay theo thoi
 * tiet / gio giu dung cap goc. +2 khong bao gio kich luat chenh cap x2 (LiveActor.attackDam: chenh &gt;= 5).
 */
public final class KiemDoiLoaiQuai {

    private KiemDoiLoaiQuai() {
    }

    public static void chay() {
        Kiem.nhom("Quai doi loai khi hoi sinh (he sinh thai / quai hiem / bung phat) giu cap + chi so cua bai (do suc 31/10/2026)");
        try {
            duLieu();
            ham();
            congCap();
            nguon();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("KiemDoiLoaiQuai khong nem loi: " + e, false);
        }
    }

    /** Chi so chay that cua mot mau: data_monster_lau_dai (sau cac khoi phan B) neu co, khong thi data_monster. */
    static Map<String, int[]> chiSo() {
        final Map<String, List<Map<String, String>>> b = KiemTtkQuai.trangThaiCuoi();
        final Map<String, int[]> ra = new HashMap<String, int[]>();
        for (final Map<String, String> m : b.get("data_monster")) {
            ra.put(m.get("id"), new int[]{KiemTtkQuai.so(m.get("level")), KiemTtkQuai.so(m.get("maxhp")), KiemTtkQuai.so(m.get("attack"))});
        }
        for (final Map<String, String> m : b.get("data_monster_lau_dai")) {
            final int[] c = ra.get(m.get("id"));
            if (c != null) {
                c[1] = KiemTtkQuai.so(m.get("maxhp"));
                c[2] = KiemTtkQuai.so(m.get("attack"));
            }
        }
        return ra;
    }

    /** Bang thay loai: truoc khi sua, nhieu cap thay (goc -> moi) mang quai cao hon >= 3 cap / tan cong > x1,5 vao bai. */
    static void duLieu() {
        final Map<String, int[]> cs = chiSo();
        final List<String> lech = new ArrayList<String>();
        int n = 0;
        for (final Map<String, String> d : KiemMucTieuNhiemVu.dongThuong("data_hst_thay_quai")) {
            final int[] g = cs.get(d.get("id_goc")), t = cs.get(d.get("id_thay"));
            if (g == null || t == null) {
                continue;
            }
            n++;
            if (t[2] > 1.5 * g[2]) {
                lech.add(d.get("id_goc") + "(" + g[0] + ")->" + d.get("id_thay") + "(" + t[0] + String.format(Locale.ROOT, ", tc x%.1f)", t[2] / (double) g[2]));
            }
        }
        System.out.println("   Cap thay loai (data_hst_thay_quai) ma mau moi danh > x1,5 bai goc - truoc khi sua mang ca chi so vao bai (" + lech.size() + "): " + lech);
        Kiem.dung("bang thay loai co dong (" + n + ")", n >= 50);
        Kiem.dung("rui ro da biet: Golem 74 / Breloom 80 (Goldenrod, cua ai CA-09.1) -> Liepard 61 cap 57 / Krokorok 64 / Feraligatr 66 cap 60-62 (tan cong x2,7-2,8)",
                lech.toString().contains("74(51)->61") && lech.toString().contains("80(52)->61") && lech.toString().contains("74(51)->64")
                && lech.toString().contains("74(51)->66"));
        Kiem.dung("rui ro da biet: Hoenn cap 82-92 -> Ursaluna 135 (tan cong 75.000, x40)", lech.toString().contains("->135(90"));
    }

    static MonsterTemplate mau(final int id, final int cap, final int hp, final int tc, final int thu, final int he) {
        final MonsterTemplate t = new MonsterTemplate();
        t.id = id;
        t.name = "mau" + id;
        t.level = (byte) cap;
        t.maxhp = hp;
        t.attack = tc;
        t.defend = thu;
        t.accurate = 690;
        t.dodge = (short) (cap / 3);
        t.he = (byte) he;
        t.rcvXp = 100 * cap;
        t.rcvGold = cap;
        return t;
    }

    /** HeSinhThaiGame.doiLoai: loai / he moi, cap + chi so theo mau goc. */
    static void ham() {
        final MonsterTemplate golem = mau(74, 51, 8620, 1098, 326, 12);
        final MonsterTemplate krok = mau(64, 60, 183830, 3053, 720, 8);
        final Monster m = new Monster(null, golem, 0, 0, 0);
        HeSinhThaiGame.doiLoai(m, krok, golem);
        Kiem.bang("doi loai Golem 51 -> Krokorok 60: loai / he moi", "64/8", m.idTemplate + "/" + m.hePokemon);
        Kiem.bang("doi loai: cap / HP / tan cong / thu / EXP theo bai goc", "51/8620/1098/326/5100",
                m.level + "/" + m.maxhp + "/" + m.attack + "/" + m.defend_physic + "/" + m.maxxp);
        final Monster m2 = new Monster(null, golem, 0, 0, 0);
        HeSinhThaiGame.doiLoai(m2, krok, null);
        Kiem.bang("khong biet mau goc: chi so mau moi (nhu cu)", "60/183830", m2.level + "/" + m2.maxhp);
        final Monster m3 = new Monster(null, krok, 0, 0, 0);
        HeSinhThaiGame.doiLoai(m3, golem, golem);
        Kiem.bang("tra ve loai goc: chi so goc", "74/51/8620", m3.idTemplate + "/" + m3.level + "/" + m3.maxhp);
        Kiem.dung("mauChiSo(null) = null", HeSinhThaiGame.mauChiSo(null) == null);
    }

    /** Duong chuan tu du lieu that (nhu data.Database.loadMonsterTemplate): quai dai cap + Hoenn 139..174. */
    static TreeMap<Integer, int[]> duongThat() {
        final Map<String, List<Map<String, String>>> b = KiemTtkQuai.trangThaiCuoi();
        final TreeMap<Integer, int[]> d = new TreeMap<Integer, int[]>();
        final Map<String, Map<String, String>> mon = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> m : b.get("data_monster")) {
            mon.put(m.get("id"), m);
        }
        for (final Map<String, String> q : b.get("data_monster_lau_dai")) {
            final Map<String, String> m = mon.get(q.get("id"));
            DuongQuaiChuan.them(d, KiemTtkQuai.so(m == null ? q.get("cap") : m.get("level")), KiemTtkQuai.so(q.get("maxhp")),
                    KiemTtkQuai.so(q.get("attack")), KiemTtkQuai.so(q.get("defent")), KiemTtkQuai.so(q.get("rcvxp")),
                    KiemTtkQuai.so(q.get("rcvgold")));
        }
        for (final Map<String, String> m : b.get("data_monster")) {
            final int id = KiemTtkQuai.so(m.get("id"));
            if (id >= 139 && id <= 174 && "1".equals(m.get("isuse"))) {
                DuongQuaiChuan.them(d, KiemTtkQuai.so(m.get("level")), KiemTtkQuai.so(m.get("maxhp")), KiemTtkQuai.so(m.get("attack")),
                        KiemTtkQuai.so(m.get("defent")), KiemTtkQuai.so(m.get("rcvxp")), KiemTtkQuai.so(m.get("rcvgold")));
            }
        }
        return d;
    }

    /** Quai hiem / bung phat / ngay cong dong: cap goc + 2, chi so + thuong theo duong chuan. */
    static void congCap() {
        // duong gia: noi suy, hai dau
        final TreeMap<Integer, int[]> gia = new TreeMap<Integer, int[]>();
        DuongQuaiChuan.them(gia, 50, 8460, 1071, 314, 5940, 383);
        DuongQuaiChuan.them(gia, 54, 9110, 1184, 364, 7014, 463);
        DuongQuaiChuan.dat(gia);
        try {
            Kiem.bang("DuongQuaiChuan: noi suy cap 52 (HP lam tron 10)", "8790/1128/339/6477/423",
                    String.join("/", soChuoi(DuongQuaiChuan.taiCap(52))));
            Kiem.bang("DuongQuaiChuan: ngoai hai dau lay cap gan nhat", "8460/9110",
                    DuongQuaiChuan.taiCap(10)[0] + "/" + DuongQuaiChuan.taiCap(99)[0]);
            final MonsterTemplate golem = mau(74, 51, 8620, 1098, 326, 12);
            final MonsterTemplate krok = mau(64, 60, 183830, 3053, 720, 8);
            final MonsterTemplate p = HeSinhThaiGame.mauCongCap(krok, golem);
            Kiem.bang("mauCongCap: loai / he Krokorok, id mau goc, cap goc + 2 = 53", "mau64/8/74/53",
                    p.name + "/" + p.he + "/" + p.id + "/" + p.level);
            final int[] c53 = DuongQuaiChuan.taiCap(53);
            Kiem.bang("mauCongCap: HP / tan cong / thu / EXP / xu = duong chuan cap 53 (khong phai chi so cu Krokorok 183.830 / 3.053)",
                    c53[0] + "/" + c53[1] + "/" + c53[2] + "/" + c53[3] + "/" + c53[4],
                    p.maxhp + "/" + p.attack + "/" + p.defend + "/" + p.rcvXp + "/" + p.rcvGold);
            final Monster m = new Monster(null, golem, 0, 0, 0);
            HeSinhThaiGame.doiLoai(m, krok, golem);
            HeSinhThaiGame.giuChiSo(m, p);
            Kiem.bang("quai hiem tren bai Golem 51: cap 53, chi so duong chuan", "53/" + c53[0] + "/" + c53[1], m.level + "/" + m.maxhp + "/" + m.attack);
            // luat chenh cap (LiveActor.attackDam) tinh theo cap GOC: Breloom 52 bung phat len 54, nguoi choi cap 49 chenh 3 (khong
            // phai 5 -> x2). Do suc 31/10/2026: don 2.000-3.200 truoc khi sua.
            final MonsterTemplate breloom = mau(80, 52, 8780, 1127, 339, 4);
            final MonsterTemplate p54 = HeSinhThaiGame.mauCongCap(breloom, breloom);
            final Monster mb = new Monster(null, breloom, 0, 0, 0);
            HeSinhThaiGame.giuChiSo(mb, p54);
            HeSinhThaiGame.datCongCap(mb, p54);
            Kiem.bang("bung phat Breloom 52: cap hien 54, cap tinh chenh cap 52", "54/52", mb.level + "/" + HeSinhThaiGame.capChenh(mb));
            HeSinhThaiGame.datCongCap(mb, null);
            Kiem.bang("quai thuong: cap tinh chenh cap = cap hien tai", Integer.valueOf(54), Integer.valueOf(HeSinhThaiGame.capChenh(mb)));
            Kiem.dung("LiveActor.attackDam: luat chenh cap dung HeSinhThaiGame.capChenh",
                    KiemTtkQuai.doc("src/real/LiveActor.java").contains("int level = this instanceof Monster ? com.htbb.pokemon.HeSinhThaiGame.capChenh((Monster) this) : this.getLevel();"));
            DuongQuaiChuan.dat(null);
            Kiem.dung("duong chua nap: khong cong cap (null, giu chi so goc)", HeSinhThaiGame.mauCongCap(krok, golem) == null);
            Kiem.bang("cong cap = 2 (< 5: khong kich luat chenh cap x2)", Integer.valueOf(2), Integer.valueOf(HeSinhThaiGame.CONG_CAP_DAC_BIET));

            // du lieu that: moi o sinh co the ra quai hiem / bung phat -> +2 cap chi danh manh hon <= 15%, HP <= 20%
            final TreeMap<Integer, int[]> that = duongThat();
            DuongQuaiChuan.dat(that);
            Kiem.dung("duong chuan that cap 1..100 (" + that.size() + " cap)", that.size() >= 60 && that.firstKey() <= 1 && that.lastKey() >= 100);
            final List<String> manh = new ArrayList<String>();
            for (int cap = 10; cap <= 98; cap++) { // cap 1..9: HP quai rat nho (450..1.490), x1,2..1,6 khong dang ke
                final int[] a = DuongQuaiChuan.taiCap(cap), b = DuongQuaiChuan.taiCap(cap + 2);
                if (b[1] > 1.15 * a[1] || b[0] > 1.20 * a[0] || b[1] < a[1] || b[0] < a[0]) {
                    manh.add(cap + String.format(Locale.ROOT, ":tc x%.2f hp x%.2f", b[1] / (double) a[1], b[0] / (double) a[0]));
                }
            }
            Kiem.bang("cap goc 10..98 -> +2 tren duong that: tan cong x1..1,15, HP x1..1,2 (thuong nho, khong lech)", "[]", manh.toString());
        } finally {
            DuongQuaiChuan.dat(null);
        }
    }

    static String[] soChuoi(final int[] a) {
        final String[] r = new String[a.length];
        for (int i = 0; i < a.length; i++) {
            r[i] = String.valueOf(a[i]);
        }
        return r;
    }

    static void nguon() {
        final String hst = KiemTtkQuai.doc("src/com/htbb/pokemon/HeSinhThaiGame.java");
        Kiem.dung("HeSinhThaiGame.khiHoiSinh doi loai qua doiLoai(m, t, mau goc)",
                hst.contains("doiLoai(m, t, real.Map.monsterTemplates.get(Integer.valueOf(goc)));"));
        Kiem.dung("NoiDungLauDai.khiHoiSinh: HP / tan cong / quai tinh anh theo mauChiSo",
                KiemTtkQuai.doc("src/real/NoiDungLauDai.java").contains("final MonsterTemplate t = com.htbb.pokemon.HeSinhThaiGame.mauChiSo(m);"));
        Kiem.dung("cong cap chi cho quai hiem / bung phat / ngay cong dong (thay theo thoi tiet / gio: dacBiet 0, khong cong cap)",
                hst.contains("congCap = true;") && hst.contains("if (congCap || dacBiet != 0) {")
                && hst.contains("dacBiet = quaiHiem.contains(c) ? 1 : 0;"));
        Kiem.dung("mauChiSo uu tien mau cong cap", hst.contains("final MonsterTemplate pc = m == null ? null : PHU_CAP.get(m);"));
        Kiem.dung("Database.loadMonsterTemplate nap DuongQuaiChuan", KiemTtkQuai.doc("src/data/Database.java").contains("com.htbb.pokemon.DuongQuaiChuan.dat(duong);"));
        final String mo = KiemTtkQuai.doc("src/real/Monster.java");
        Kiem.dung("Monster: EXP khi hoi sinh + xu roi theo mauChiSo", mo.contains("Monster.rcvXpSauGiam(com.htbb.pokemon.HeSinhThaiGame.mauChiSo(this))")
                && mo.contains("com.htbb.pokemon.HeSinhThaiGame.mauChiSo(this).rcvGold / tileDrop"));
    }
}
