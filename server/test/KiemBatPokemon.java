import com.htbb.pokemon.BatPokemon;
import com.htbb.pokemon.HopPc;
import com.htbb.pokemon.Pokedex;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * THU PHUC POKEMON (docs/thiet-ke-game.md 7.7): ti le bat (RNG co hat giong), dieu kien (boss / Gym / huyen thoai khong
 * bat duoc, HP, lech cap), tran bong, bo chay, Keo loai theo ho, Hop PC (o, mo rong, luu / nap), Pokedex "Da bat", du
 * lieu sql/them_bo_ngoc_che_tao_bat.sql doi chieu du_lieu/bang_quai.csv + kiem_ke_sprite.csv, khoa cau hinh bat.*.
 */
public final class KiemBatPokemon {

    private KiemBatPokemon() {
    }

    public static void chay() {
        Kiem.nhom("Thu phuc: dieu kien nem bong (boss / chua yeu / qua manh / su kien)");
        kiemDieuKien();
        Kiem.nhom("Thu phuc: ti le theo HP, cap, do hiem, alpha, bong (tran), thuong truot; RNG hat giong");
        kiemTiLe();
        Kiem.nhom("Hop PC: o 30 -> 100, them / tha / di theo, keo loai, bong, bo dem ngay, luu / nap");
        kiemHop();
        Kiem.nhom("Pokedex: Da bat (so_bat, shiny) luu cung so_ha");
        kiemPokedex();
        Kiem.nhom("Du lieu data_bat_loai / data_bat_bong + khoa bat.*");
        kiemDuLieu();
    }

    static BatPokemon.Loai thuong(final int dex) {
        return new BatPokemon.Loai(dex, "X", 0, 0, 0, 0, 0, true, false);
    }

    static void kiemDieuKien() {
        final BatPokemon.Loai l = thuong(19);
        Kiem.bang("boss / Gym / boss the gioi: khong bat duoc", Integer.valueOf(BatPokemon.KHONG_BAT_DUOC),
                Integer.valueOf(BatPokemon.kiem(l, true, 1, 100, 10, 10, 25, false)));
        Kiem.bang("loai khong co trong bang: khong phai quai thuong", Integer.valueOf(BatPokemon.KHONG_PHAI_QUAI),
                Integer.valueOf(BatPokemon.kiem(null, false, 1, 100, 10, 10, 25, false)));
        Kiem.bang("loai bat_duoc = 0 (huyen thoai / chi tien hoa)", Integer.valueOf(BatPokemon.KHONG_BAT_DUOC),
                Integer.valueOf(BatPokemon.kiem(new BatPokemon.Loai(94, "Gengar", 0, 0, 0, 0, 0, false, false), false, 1, 100, 10, 10, 25, false)));
        Kiem.bang("HP 26% > nguong 25%: chua yeu", Integer.valueOf(BatPokemon.CHUA_YEU),
                Integer.valueOf(BatPokemon.kiem(l, false, 26, 100, 10, 10, 25, false)));
        Kiem.bang("HP 25%: nem duoc", Integer.valueOf(BatPokemon.OK), Integer.valueOf(BatPokemon.kiem(l, false, 25, 100, 10, 10, 25, false)));
        Kiem.bang("quai da chet (hp 0): chua yeu (khong nem vao xac)", Integer.valueOf(BatPokemon.CHUA_YEU),
                Integer.valueOf(BatPokemon.kiem(l, false, 0, 100, 10, 10, 25, false)));
        Kiem.bang("quai hon 11 cap: qua manh", Integer.valueOf(BatPokemon.QUA_MANH),
                Integer.valueOf(BatPokemon.kiem(l, false, 1, 100, 31, 20, 25, false)));
        Kiem.bang("quai hon 10 cap: duoc", Integer.valueOf(BatPokemon.OK), Integer.valueOf(BatPokemon.kiem(l, false, 1, 100, 30, 20, 25, false)));
        final BatPokemon.Loai sk = new BatPokemon.Loai(25, "Pikachu", 2, 0, 0, 0, 0, true, true);
        Kiem.bang("loai chi su kien, su kien tat", Integer.valueOf(BatPokemon.CHI_SU_KIEN),
                Integer.valueOf(BatPokemon.kiem(sk, false, 1, 100, 10, 10, 25, false)));
        Kiem.bang("loai chi su kien, su kien bat", Integer.valueOf(BatPokemon.OK), Integer.valueOf(BatPokemon.kiem(sk, false, 1, 100, 10, 10, 25, true)));
        Kiem.dung("chu thong bao co dau, co nguong", BatPokemon.chuKiem(BatPokemon.CHUA_YEU, 25).contains("dưới 25%"));
    }

    static void kiemTiLe() {
        Kiem.bang("he so HP: 25% -> 833, 100% -> 333, 1 HP -> 999", "833,333,999", BatPokemon.heSoHp(25, 100) + ","
                + BatPokemon.heSoHp(100, 100) + "," + BatPokemon.heSoHp(1, 1000));
        Kiem.bang("he so cap: thap hon / +5 / +10 / +11", "1000,800,500,0", BatPokemon.heSoCap(5, 10) + "," + BatPokemon.heSoCap(15, 10)
                + "," + BatPokemon.heSoCap(20, 10) + "," + BatPokemon.heSoCap(21, 10));
        final List<BatPokemon.Bong> b = BatPokemon.bongMacDinh();
        final BatPokemon.Bong poke = b.get(0);
        final BatPokemon.Bong great = b.get(1);
        final BatPokemon.Bong ultra = b.get(2);
        final BatPokemon.Loai l = thuong(19);
        // 0.9.8 trao doi: goc 55 / 25 / 9 / 3%, nguong 35% HP (he so 0,766), bo chay sau 4 lan truot
        Kiem.bang("goc theo bac hiem 550/250/90/30, nguong 35%, bo chay 4", "550,250,90,30,35,4", BatPokemon.GOC_THEO_DO_HIEM[0] + ","
                + BatPokemon.GOC_THEO_DO_HIEM[1] + "," + BatPokemon.GOC_THEO_DO_HIEM[2] + "," + BatPokemon.GOC_THEO_DO_HIEM[3] + ","
                + BatPokemon.NGUONG_HP_PHAN_TRAM + "," + BatPokemon.SO_LAN_BO_CHAY);
        Kiem.bang("he so HP o nguong 35%", Integer.valueOf(766), Integer.valueOf(BatPokemon.heSoHp(35, 100)));
        // loai thuong 550 x 0,766 = 421 phan nghin o 35% HP, cung cap, Poke Ball
        Kiem.bang("Poke Ball, loai thuong, 35% HP, cung cap = 42,1%", Integer.valueOf(421),
                Integer.valueOf(BatPokemon.tiLe(l, 35, 100, 10, 10, false, poke, 0)));
        Kiem.bang("Great Ball x1,5 = 63,1%", Integer.valueOf(631), Integer.valueOf(BatPokemon.tiLe(l, 35, 100, 10, 10, false, great, 0)));
        Kiem.bang("Ultra Ball x2 = 84,2%", Integer.valueOf(842), Integer.valueOf(BatPokemon.tiLe(l, 35, 100, 10, 10, false, ultra, 0)));
        Kiem.bang("loai thuong 25% HP: Poke 45,8% / Ultra kep 90%", "458,900", BatPokemon.tiLe(l, 25, 100, 10, 10, false, poke, 0) + ","
                + BatPokemon.tiLe(l, 25, 100, 10, 10, false, ultra, 0));
        final BatPokemon.Loai itGap = new BatPokemon.Loai(128, "Tauros", 1, 0, 0, 0, 0, true, false);
        final BatPokemon.Loai hiem = new BatPokemon.Loai(123, "Scyther", 2, 0, 0, 0, 0, true, false);
        final BatPokemon.Loai ratHiem = new BatPokemon.Loai(371, "Bagon", 3, 0, 0, 0, 0, true, false);
        Kiem.bang("It gap 35% HP: Poke 19,1% / Great 28,6% / Ultra 38,2%", "191,286,382", BatPokemon.tiLe(itGap, 35, 100, 10, 10, false, poke, 0)
                + "," + BatPokemon.tiLe(itGap, 35, 100, 10, 10, false, great, 0) + "," + BatPokemon.tiLe(itGap, 35, 100, 10, 10, false, ultra, 0));
        Kiem.bang("Hiem 35% HP: Poke 6,8% / Great 10,2% / Ultra 13,6% (van HIEM)", "68,102,136", BatPokemon.tiLe(hiem, 35, 100, 10, 10, false, poke, 0)
                + "," + BatPokemon.tiLe(hiem, 35, 100, 10, 10, false, great, 0) + "," + BatPokemon.tiLe(hiem, 35, 100, 10, 10, false, ultra, 0));
        Kiem.bang("Rat hiem 35% HP: Poke 2,2% / Ultra 4,4%", "22,44", BatPokemon.tiLe(ratHiem, 35, 100, 10, 10, false, poke, 0) + ","
                + BatPokemon.tiLe(ratHiem, 35, 100, 10, 10, false, ultra, 0));
        Kiem.bang("Rat hiem 1 HP + Ultra: chi 5,8%", Integer.valueOf(58), Integer.valueOf(BatPokemon.tiLe(ratHiem, 1, 1000, 10, 10, false, ultra, 0)));
        Kiem.bang("thuong truot tuong doi: Hiem +1,7% / lan (khong thanh de)", "85,102,119", BatPokemon.tiLe(hiem, 35, 100, 10, 10, false, poke, 1)
                + "," + BatPokemon.tiLe(hiem, 35, 100, 10, 10, false, poke, 2) + "," + BatPokemon.tiLe(hiem, 35, 100, 10, 10, false, poke, 3));
        Kiem.bang("ten bac hiem", "Thường,Ít gặp,Hiếm,Rất hiếm,Rất hiếm", BatPokemon.tenDoHiem(0) + "," + BatPokemon.tenDoHiem(1) + ","
                + BatPokemon.tenDoHiem(2) + "," + BatPokemon.tenDoHiem(3) + "," + BatPokemon.tenDoHiem(9));
        Kiem.bang("1 HP + Ultra: kep tran Ultra 90%", Integer.valueOf(900), Integer.valueOf(BatPokemon.tiLe(l, 1, 1000, 10, 10, false, ultra, 2)));
        final BatPokemon.Loai de = new BatPokemon.Loai(19, "X", 0, 1000, 0, 0, 0, true, false);
        Kiem.bang("loai goc 100%, 1 HP + Poke: kep tran Poke 60%", Integer.valueOf(600), Integer.valueOf(BatPokemon.tiLe(de, 1, 1000, 10, 10, false, poke, 2)));
        final BatPokemon.Loai hiem2 = new BatPokemon.Loai(376, "Metagross", 3, 0, 0, 0, 0, true, false);
        Kiem.bang("rat hiem (3%) + 25% HP + Poke = 2,4%", Integer.valueOf(24), Integer.valueOf(BatPokemon.tiLe(hiem2, 25, 100, 10, 10, false, poke, 0)));
        Kiem.bang("alpha x25%", Integer.valueOf(114), Integer.valueOf(BatPokemon.tiLe(l, 25, 100, 10, 10, true, poke, 0)));
        Kiem.bang("quai hon 6 cap x50%", Integer.valueOf(229), Integer.valueOf(BatPokemon.tiLe(l, 25, 100, 16, 10, false, poke, 0)));
        Kiem.bang("san 1%: rat hiem + alpha + lech cap", Integer.valueOf(BatPokemon.SAN_PHAN_NGHIN),
                Integer.valueOf(BatPokemon.tiLe(hiem2, 25, 100, 20, 10, true, poke, 0)));
        Kiem.bang("thuong truot loai thuong: +5% moi lan, toi da 3 lan", "471,521,571,571", BatPokemon.tiLe(l, 35, 100, 10, 10, false, poke, 1) + ","
                + BatPokemon.tiLe(l, 35, 100, 10, 10, false, poke, 2) + "," + BatPokemon.tiLe(l, 35, 100, 10, 10, false, poke, 3) + ","
                + BatPokemon.tiLe(l, 35, 100, 10, 10, false, poke, 5));
        Kiem.bang("qua manh: 0", Integer.valueOf(0), Integer.valueOf(BatPokemon.tiLe(l, 1, 100, 40, 10, false, ultra, 0)));
        // bong hong / CSDL sai: khong bong nao chac chan bat duoc
        final BatPokemon.Bong sai = new BatPokemon.Bong("x", "x", 9, 9999, 5000, 1, 1);
        Kiem.bang("bong cau hinh sai: he so <= 2,5, tran <= 95%", "250,950", sai.heSoPhanTram + "," + sai.tranPhanNghin);
        // RNG hat giong: tan suat thanh cong ~ ti le
        final Random r = new Random(20261020L);
        final int tl = BatPokemon.tiLe(l, 35, 100, 10, 10, false, poke, 0);
        int duoc = 0;
        final int n = 200000;
        for (int i = 0; i < n; i++) {
            if (BatPokemon.thanhCong(r.nextInt(1000), tl)) {
                duoc++;
            }
        }
        Kiem.dung("200.000 lan nem (hat giong): " + duoc * 100.0 / n + "% ~ " + tl / 10.0 + "%", Math.abs(duoc - (long) n * tl / 1000) < n / 200);
        // ky vong (hat giong 5) o nguong 35% HP bang Poke Ball, toi da 4 lan (lan 4 truot thi bo chay), theo bac hiem
        final int[][] khoang = {{90000, 97000}, {65000, 76000}, {27000, 38000}, {8000, 15000}};
        final BatPokemon.Loai[] bac = {l, itGap, hiem, ratHiem};
        for (int k = 0; k < bac.length; k++) {
            final Random r2 = new Random(5L);
            int batDuoc = 0;
            int bong = 0;
            for (int i = 0; i < 100000; i++) {
                for (int lan = 0; lan < BatPokemon.SO_LAN_BO_CHAY; lan++) {
                    bong++;
                    if (BatPokemon.thanhCong(r2.nextInt(1000), BatPokemon.tiLe(bac[k], 35, 100, 10, 10, false, poke, lan))) {
                        batDuoc++;
                        break;
                    }
                }
            }
            Kiem.dung(BatPokemon.tenDoHiem(k) + ", Poke Ball 35% HP: bat duoc ~" + batDuoc / 1000 + "% truoc khi bo chay, ~"
                    + Math.round(bong * 10.0 / Math.max(1, batDuoc)) / 10.0 + " bong / con", batDuoc > khoang[k][0] && batDuoc < khoang[k][1]);
        }
        Kiem.bang("lac: bat duoc 3, truot sat 2, truot xa 0", "3,2,0", BatPokemon.soLanLac(10, 374, true) + ","
                + BatPokemon.soLanLac(400, 374, false) + "," + BatPokemon.soLanLac(900, 374, false));
        Kiem.dung("bo chay o lan truot thu 4", !BatPokemon.boChay(3, BatPokemon.SO_LAN_BO_CHAY) && BatPokemon.boChay(4, BatPokemon.SO_LAN_BO_CHAY));
        Kiem.bang("keo khi bat: 3 (shiny 6), tha +1", "3,6,1", BatPokemon.keoKhiBat(false) + "," + BatPokemon.keoKhiBat(true) + ","
                + BatPokemon.KEO_KHI_THA);
        Kiem.bang("gia bong: 2% / 5% / 12% gio thu nhap", "200,500,1200", BatPokemon.giaBong(poke, 10000) + ","
                + BatPokemon.giaBong(great, 10000) + "," + BatPokemon.giaBong(ultra, 10000));
        final Map<Integer, BatPokemon.Loai> bang = new HashMap<Integer, BatPokemon.Loai>();
        bang.put(Integer.valueOf(255), new BatPokemon.Loai(255, "Torchic", 0, 0, 256, 16, 25, true, false));
        bang.put(Integer.valueOf(256), new BatPokemon.Loai(256, "Combusken", 0, 0, 257, 36, 50, true, false));
        bang.put(Integer.valueOf(257), new BatPokemon.Loai(257, "Blaziken", 0, 0, 0, 0, 0, true, false));
        Kiem.bang("ho tien hoa: Blaziken / Combusken -> keo Torchic", "255,255,255,19", BatPokemon.goc(257, bang) + ","
                + BatPokemon.goc(256, bang) + "," + BatPokemon.goc(255, bang) + "," + BatPokemon.goc(19, bang));
        bang.put(Integer.valueOf(1), new BatPokemon.Loai(1, "A", 0, 0, 2, 1, 1, true, false));
        bang.put(Integer.valueOf(2), new BatPokemon.Loai(2, "B", 0, 0, 1, 1, 1, true, false));
        Kiem.dung("du lieu vong (1 -> 2 -> 1) khong treo", BatPokemon.goc(1, bang) > 0);
        Kiem.bang("keo luyen: Lv.9 -> 1, Lv.10 -> 2, Lv.55 -> 6", "1,2,6", BatPokemon.keoLuyen(9) + "," + BatPokemon.keoLuyen(10) + ","
                + BatPokemon.keoLuyen(55));
        Kiem.bang("cap toi da con = cap nguoi choi, <= 100", "37,100", BatPokemon.capToiDa(37) + "," + BatPokemon.capToiDa(140));
    }

    static void kiemHop() {
        final HopPc h = new HopPc();
        Kiem.bang("suc chua mac dinh 30", Integer.valueOf(30), Integer.valueOf(h.sucChua()));
        for (int i = 0; i < 30; i++) {
            h.them(19, 5, false, 20261020, "poke");
        }
        Kiem.dung("30 con: day, them nua bi tu choi", h.day() && h.them(19, 5, false, 20261020, "poke") == null);
        final long g0 = h.giaMoRong(10000);
        h.moRong();
        Kiem.bang("mo rong +10", Integer.valueOf(40), Integer.valueOf(h.sucChua()));
        Kiem.bang("gia mo rong: 2 gio, roi x1,5", "20000,30000", g0 + "," + h.giaMoRong(10000));
        for (int i = 0; i < 20; i++) {
            h.moRong();
        }
        Kiem.bang("tran 100 o", Integer.valueOf(100), Integer.valueOf(h.sucChua()));
        Kiem.dung("het mo rong", !h.moRongDuoc());
        final HopPc.Con c = h.danhSach().get(3);
        h.datTheo(c.id);
        Kiem.bang("di theo", Integer.valueOf(c.id), Integer.valueOf(h.conDangTheo().id));
        Kiem.dung("tha con dang theo -> thoi theo", h.tha(c.id) != null && h.conDangTheo() == null && h.soCon() == 29);
        Kiem.dung("tha con khong co -> null", h.tha(9999) == null);
        h.themKeo(255, 10);
        Kiem.dung("tru keo du / thieu", h.truKeo(255, 4) && !h.truKeo(255, 7) && h.keo(255) == 6);
        h.themBong("poke", 5);
        Kiem.dung("bong: tru tung qua, het thi tu choi", h.truBong("poke") && h.bong("poke") == 4);
        h.doiNgay(20261020);
        h.batNgay = 12;
        h.nemNgay = 40;
        h.doiNgay(20261020);
        Kiem.bang("cung ngay giu bo dem", "12,40", h.batNgay + "," + h.nemNgay);
        h.doiNgay(20261021);
        Kiem.bang("doi ngay dat lai bo dem", "0,0", h.batNgay + "," + h.nemNgay);
        h.tongBat = 31;
        final HopPc.Con s = h.them(257, 40, true, 20261021, "ultra");
        h.datTheo(s.id);
        final HopPc h2 = HopPc.giaiMa(h.maHoa());
        Kiem.bang("luu / nap: so con, o, keo, bong, theo, shiny, tong bat", h.soCon() + "," + h.sucChua() + ",6,4," + s.id + ",true,31",
                h2.soCon() + "," + h2.sucChua() + "," + h2.keo(255) + "," + h2.bong("poke") + "," + h2.dangTheo + ","
                        + h2.lay(s.id).shiny + "," + h2.tongBat);
        Kiem.dung("con moi sau khi nap khong trung id", h2.them(19, 1, false, 1, "poke").id > s.id);
        Kiem.dung("chuoi hong -> hop rong, khong nem loi", HopPc.giaiMa("c1=abc;k=;;mr=99999").soCon() == 0
                && HopPc.giaiMa("mr=99999").sucChua() == 100);
        Kiem.dung("ma bong la loc ky tu (khong chen ';' / '=' vao chuoi luu)", new HopPc.Con(1, 1, 1, false, 1, "po;k=e").bong.equals("poke"));
    }

    static void kiemPokedex() {
        final Pokedex d = new Pokedex();
        d.napDong(19, 50, 1000L);
        d.napDong(255, 0, 1000L, 2, true);
        d.layDongBan();
        Kiem.dung("nap so_bat / bat_shiny", d.soBat(255) == 2 && d.batShiny(255) && !d.daBat(19));
        Kiem.dung("lan dau bat loai: true; lan sau: false", d.ghiBat(19, false, 2000L) && !d.ghiBat(19, true, 2001L));
        Kiem.dung("bat loai chua gap -> ghi luon da gap", d.ghiBat(400, false, 2002L) && d.daGap(400));
        Kiem.bang("so loai da bat", Integer.valueOf(3), Integer.valueOf(d.soLoaiDaBat()));
        Kiem.bang("so ha khong doi khi bat", Integer.valueOf(50), Integer.valueOf(d.soHa(19)));
        final List<int[]> tt = d.trangThai();
        final StringBuilder sb = new StringBuilder();
        for (final int[] x : tt) {
            sb.append(x[0]).append(':').append(x[1]).append(' ');
        }
        Kiem.bang("co Pokedex: bit0 ha, bit1 bat, bit2 shiny (theo dex)", "19:7 255:6 400:2 ", sb.toString());
        final List<long[]> ban = d.layDongBan();
        boolean co = false;
        for (final long[] x : ban) {
            if (x[0] == 19 && x.length == 5 && x[3] == 2 && x[4] == 1) {
                co = true;
            }
        }
        Kiem.dung("dong ban mang {dex, so_ha, lan_dau, so_bat, bat_shiny}", co && ban.size() == 2);
    }

    static void kiemDuLieu() {
        final String sql = KiemBoNgocCheTao.doc("../sql/them_bo_ngoc_che_tao_bat.sql");
        final Pattern p = Pattern.compile("\\((\\d+),'((?:[^']|'')*)',(\\d),(\\d+),(\\d+),(\\d+),(\\d+),(\\d),(\\d)\\)");
        final Matcher m = p.matcher(sql);
        final Map<Integer, int[]> loai = new HashMap<Integer, int[]>();
        while (m.find()) {
            loai.put(Integer.valueOf(m.group(1)), new int[]{Integer.parseInt(m.group(3)), Integer.parseInt(m.group(5)),
                Integer.parseInt(m.group(6)), Integer.parseInt(m.group(7)), Integer.parseInt(m.group(8))});
        }
        Kiem.dung("data_bat_loai co >= 100 loai (" + loai.size() + ")", loai.size() >= 100);
        final Set<Integer> quai = new HashSet<Integer>();
        final Set<Integer> boss = new HashSet<Integer>();
        for (final String[] r : KiemBoNgocCheTao.csv("../du_lieu/bang_quai.csv")) {
            if (r.length > 5) {
                if ("quai".equals(r[4])) {
                    quai.add(Integer.valueOf(r[5]));
                } else if ("boss".equals(r[4])) {
                    boss.add(Integer.valueOf(r[5]));
                }
            }
        }
        int thieu = 0;
        for (final Integer d : quai) {
            if (!loai.containsKey(d) || loai.get(d)[4] != 1) {
                thieu++;
            }
        }
        Kiem.bang("moi loai quai thuong (bang_quai.csv) deu bat duoc", Integer.valueOf(0), Integer.valueOf(thieu));
        int bossBat = 0;
        for (final Integer d : boss) {
            if (!quai.contains(d) && loai.containsKey(d) && loai.get(d)[4] != 0) {
                bossBat++;
            }
        }
        Kiem.bang("loai chi la boss / huyen thoai: khong bat duoc ngoai tu nhien", Integer.valueOf(0), Integer.valueOf(bossBat));
        Kiem.dung("Rayquaza / Lugia / Kyogre / Groudon / Zapdos khong co dong bat duoc", !coBat(loai, 384) && !coBat(loai, 249)
                && !coBat(loai, 382) && !coBat(loai, 383) && !coBat(loai, 145));
        final Set<Integer> sprite = new HashSet<Integer>();
        for (final String[] r : KiemBoNgocCheTao.csv("../du_lieu/kiem_ke_sprite.csv")) {
            if (r.length > 3 && "OK".equals(r[3]) && "0".equals(r[1])) {
                sprite.add(Integer.valueOf(r[0]));
            }
        }
        int hong = 0;
        for (final Map.Entry<Integer, int[]> e : loai.entrySet()) {
            final int sau = e.getValue()[1];
            if (sau > 0 && (!loai.containsKey(Integer.valueOf(sau)) || !sprite.contains(Integer.valueOf(sau))
                    || e.getValue()[2] <= 0 || e.getValue()[3] <= 0)) {
                hong++;
            }
        }
        Kiem.bang("tien hoa trong Hop PC: loai sau co dong + co hinh, cap + keo > 0", Integer.valueOf(0), Integer.valueOf(hong));
        int khongHinh = 0;
        for (final Integer d : loai.keySet()) {
            if (!sprite.contains(d)) {
                khongHinh++;
            }
        }
        Kiem.bang("moi loai co hinh (ban dong hanh di theo ve duoc)", Integer.valueOf(0), Integer.valueOf(khongHinh));
        Kiem.dung("3 bong Poke / Great / Ultra, tran 60 / 75 / 90%", sql.contains("('poke', 'Poké Ball', 1, 100, 600, 20, 1, 1)")
                && sql.contains("('great', 'Great Ball', 2, 150, 750, 50, 20, 1)") && sql.contains("('ultra', 'Ultra Ball', 3, 200, 900, 120, 40, 1)"));
        final String dm = KiemBoNgocCheTao.doc("../tools/cauhinh/danh_muc.json");
        final String[][] khoa = {
            {"bat.bat", "1"}, {"bat.nguong_hp_phan_tram", String.valueOf(BatPokemon.NGUONG_HP_PHAN_TRAM)}, {"bat.tran_ngay", "30"},
            {"bat.tran_nem_ngay", "150"}, {"bat.cach_ms", "1500"}, {"bat.so_lan_bo_chay", String.valueOf(BatPokemon.SO_LAN_BO_CHAY)},
            {"bat.su_kien", "0"}, {"bat.dong_hanh", "1"},
        };
        for (final String[] k : khoa) {
            Kiem.dung("danh muc cau hinh: " + k[0] + " mac dinh " + k[1],
                    dm.matches("(?s).*\"khoa\": \"" + k[0].replace(".", "\\.") + "\"[^}]*\"macDinh\": \"" + k[1] + "\".*"));
        }
    }

    static boolean coBat(final Map<Integer, int[]> loai, final int dex) {
        final int[] l = loai.get(Integer.valueOf(dex));
        return l != null && l[4] == 1;
    }
}
