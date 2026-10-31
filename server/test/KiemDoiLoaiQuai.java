import com.htbb.pokemon.HeSinhThaiGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
 */
public final class KiemDoiLoaiQuai {

    private KiemDoiLoaiQuai() {
    }

    public static void chay() {
        Kiem.nhom("Quai doi loai khi hoi sinh (he sinh thai / quai hiem / bung phat) giu cap + chi so cua bai (do suc 31/10/2026)");
        try {
            duLieu();
            ham();
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

    static void nguon() {
        final String hst = KiemTtkQuai.doc("src/com/htbb/pokemon/HeSinhThaiGame.java");
        Kiem.dung("HeSinhThaiGame.khiHoiSinh doi loai qua doiLoai(m, t, mau goc)",
                hst.contains("doiLoai(m, t, real.Map.monsterTemplates.get(Integer.valueOf(goc)));"));
        Kiem.dung("NoiDungLauDai.khiHoiSinh: HP / tan cong / quai tinh anh theo mauChiSo",
                KiemTtkQuai.doc("src/real/NoiDungLauDai.java").contains("final MonsterTemplate t = com.htbb.pokemon.HeSinhThaiGame.mauChiSo(m);"));
        final String mo = KiemTtkQuai.doc("src/real/Monster.java");
        Kiem.dung("Monster: EXP khi hoi sinh + xu roi theo mauChiSo", mo.contains("Monster.rcvXpSauGiam(com.htbb.pokemon.HeSinhThaiGame.mauChiSo(this))")
                && mo.contains("com.htbb.pokemon.HeSinhThaiGame.mauChiSo(this).rcvGold / tileDrop"));
    }
}
