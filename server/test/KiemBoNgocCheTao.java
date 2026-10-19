import com.htbb.pokemon.BoTrangBi;
import com.htbb.pokemon.CheTaoLogic;
import com.htbb.pokemon.GioiHanChiSo;
import com.htbb.pokemon.NangCapLauDai;
import com.htbb.pokemon.NgocHe;
import com.htbb.pokemon.ThongSoLauDai;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * BO TRANG BI / NGOC HE / CHE TAO / MOC NEO NANG CAP (nhanh bo-ngoc-bat, docs/thiet-ke-game.md 4.2-4.3): lop thuan
 * com.htbb.pokemon.BoTrangBi, NgocHe, CheTaoLogic, NangCapLauDai + du lieu (du_lieu/noi_dung_*.csv,
 * sql/them_bo_ngoc_che_tao_bat.sql) + khoa cau hinh trong tools/cauhinh/danh_muc.json. RNG co hat giong.
 */
public final class KiemBoNgocCheTao {

    private KiemBoNgocCheTao() {
    }

    static String doc(final String p) {
        try {
            return new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    /** CSV don gian (khong co dau phay trong o). */
    static List<String[]> csv(final String p) {
        final List<String[]> v = new ArrayList<String[]>();
        final String[] dong = doc(p).split("\r?\n");
        for (int i = 1; i < dong.length; i++) {
            if (!dong[i].trim().isEmpty()) {
                v.add(dong[i].split(","));
            }
        }
        return v;
    }

    public static void chay() {
        Kiem.nhom("Bo trang bi cung bac (data_noi_dung_bo): dem mon, thuong cao nhat, tran 5% / 3%");
        kiemBo();
        Kiem.nhom("Ngoc he: lo theo bac, ma hoa tren mon, loi + hai, tran 10% / mon, gia theo gio thu nhap, tui ngoc");
        kiemNgoc();
        Kiem.nhom("Che tao: cong thuc dung spec 4.4, ti le bac 60/30/9/1 (RNG hat giong), pity Su thi lan 20");
        kiemCheTao();
        Kiem.nhom("Nang cap: moc neo +7/+10/+13, pity tren nut, he so xu +11..+15");
        kiemMocNeo();
        Kiem.nhom("SQL them_bo_ngoc_che_tao_bat.sql + khoa cau hinh bo/ngoc_he/che_tao/nang_cap");
        kiemSqlCauHinh();
    }

    // ------------------------------------------------------------------ bo

    static BoTrangBi boTuCsv() {
        final List<BoTrangBi.Dong> ds = new ArrayList<BoTrangBi.Dong>();
        for (final String[] r : csv("../du_lieu/noi_dung_bo.csv")) {
            ds.add(new BoTrangBi.Dong(Integer.parseInt(r[0]), r[1], r[2], Integer.parseInt(r[3]), BoTrangBi.maChiSo(r[4]),
                    Integer.parseInt(r[5]), r[6]));
        }
        return new BoTrangBi(ds);
    }

    static void kiemBo() {
        final BoTrangBi bo = boTuCsv();
        Kiem.bang("33 dong bo (11 bac x 4 / 7 / 11 mon), moi dong co chi so biet", Integer.valueOf(33), Integer.valueOf(bo.soDong()));
        Kiem.bang("ma chi so: HP / Tấn công / Chí mạng / la", "0,1,2,-1", BoTrangBi.maChiSo("HP") + "," + BoTrangBi.maChiSo("Tấn công")
                + "," + BoTrangBi.maChiSo("Chí mạng") + "," + BoTrangBi.maChiSo("Né tránh"));
        final int[] dem3 = BoTrangBi.dem(new int[]{3, 3, 3, 5, -1, 99});
        Kiem.bang("dem mon theo bac (bo qua bac sai)", "3,1", dem3[3] + "," + dem3[5]);
        Kiem.bang("3 mon cung bac: chua kich", "0,0,0", s(bo.thuong(dem3, null)));
        final int[] dem4 = BoTrangBi.dem(new int[]{3, 3, 3, 3});
        Kiem.bang("4 mon: +5% HP (50 phan nghin)", "50,0,0", s(bo.thuong(dem4, null)));
        final int[] dem7 = BoTrangBi.dem(new int[]{2, 2, 2, 2, 2, 2, 2});
        Kiem.bang("7 mon: +5% HP, +5% tan cong", "50,50,0", s(bo.thuong(dem7, null)));
        final int[] dem11 = BoTrangBi.dem(new int[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10});
        Kiem.bang("11 mon: +5% HP, +5% tan cong, +3% chi mang", "50,50,30", s(bo.thuong(dem11, null)));
        final int[] hai = BoTrangBi.dem(new int[]{1, 1, 1, 1, 6, 6, 6, 6});
        Kiem.bang("hai bo 4 mon khac bac KHONG cong don (lay cao nhat)", "50,0,0", s(bo.thuong(hai, null)));
        Kiem.bang("tran nho hon (2% / 1%) kep thuong", "20,20,10", s(bo.thuong(dem11, new int[]{20, 20, 10})));
        Kiem.bang("tran 0 = tat", "0,0,0", s(bo.thuong(dem11, new int[]{0, 0, 0})));
        // bo cu (+10/+13/+15 du bo, toi 12%) + bo cung bac 5% -> chung tran 5%
        Kiem.bang("bo nang cap cu 12% + bo bac 5% -> tran chung 5%", Integer.valueOf(5),
                Integer.valueOf(GioiHanChiSo.thuongBo(12 + bo.thuong(dem11, null)[BoTrangBi.HP] / 10)));
        Kiem.bang("chi bo bac (khong co bo cu) = 5%", Integer.valueOf(5),
                Integer.valueOf(GioiHanChiSo.thuongBo(bo.thuong(dem4, null)[BoTrangBi.HP] / 10)));
        Kiem.bang("so mon du bo bac 0 = 11", Integer.valueOf(11), Integer.valueOf(bo.soMonDu(0)));
        Kiem.bang("chu thuong", "+3% chí mạng", BoTrangBi.chuThuong(bo.cuaBac(4).get(2)));
        Kiem.dung("moi bac 0..10 deu co bo", bo.cuaBac(0).size() == 3 && bo.cuaBac(10).size() == 3);
        // tran tong ~2,4x khong doi: phan tram bo khong vuot THUONG_BO_TRAN_PHAN_TRAM
        int max = 0;
        for (final BoTrangBi.Dong d : bo.dong()) {
            max = Math.max(max, d.phanTram);
        }
        Kiem.dung("moi dong bo <= tran thuong bo 5%", max <= GioiHanChiSo.THUONG_BO_TRAN_PHAN_TRAM);
    }

    static String s(final int[] a) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            b.append(i > 0 ? "," : "").append(a[i]);
        }
        return b.toString();
    }

    // ------------------------------------------------------------------ ngoc

    static void kiemNgoc() {
        final NgocHe.Kham k = new NgocHe.Kham();
        Kiem.dung("kham rong -> chuoi rong (khong them truong vao mon cu)", NgocHe.maHoa(k).isEmpty());
        k.o[0] = new NgocHe.O(1, 3, false);
        k.o[2] = new NgocHe.O(17, 10, true);
        final String m = NgocHe.maHoa(k);
        Kiem.bang("ma hoa", "nh0_103_-1_6710", m);
        Kiem.dung("ma hoa khong co dau phay / gach dung (chuoi luu mon ngan bang ',')", m.indexOf(',') < 0 && m.indexOf('|') < 0);
        final NgocHe.Kham g = NgocHe.giaiMa(m);
        Kiem.bang("giai ma lai dung", "1/3/false;-1;17/10/true",
                g.o[0].he + "/" + g.o[0].cap + "/" + g.o[0].mai + ";" + g.o[1].he + ";" + g.o[2].he + "/" + g.o[2].cap + "/" + g.o[2].mai);
        Kiem.dung("chuoi hong -> kham rong, khong nem loi", NgocHe.giaiMa("nhabc").rong() && NgocHe.giaiMa(null).rong()
                && NgocHe.giaiMa("xx").rong());
        Kiem.dung("he / cap ngoai khoang bi bo", NgocHe.giaiMa("nh0_1899_150").rong());
        final NgocHe.Kham lt = new NgocHe.Kham();
        lt.loThem = 3;
        Kiem.bang("lo them (Huyen thoai) duoc luu", Integer.valueOf(3), Integer.valueOf(NgocHe.giaiMa(NgocHe.maHoa(lt)).loThem));
        Kiem.bang("so lo: max(bac, them), <= 3", "1,3,3,0", NgocHe.soLo(1, 0) + "," + NgocHe.soLo(1, 3) + "," + NgocHe.soLo(5, 0)
                + "," + NgocHe.soLo(-1, 0));
        // tran 10% / mon: 3 lo cap 10 = 30% -> 10%
        final NgocHe.Kham ba = new NgocHe.Kham();
        for (int i = 0; i < 3; i++) {
            ba.o[i] = new NgocHe.O(i, 10, false);
        }
        Kiem.bang("3 lo cap 10 -> % chi so mon kep 10%", Integer.valueOf(10), Integer.valueOf(NgocHe.phanTramMon(ba, 3, null, 10)));
        Kiem.bang("chi tinh lo dung duoc (2 lo, tran 30)", Integer.valueOf(20), Integer.valueOf(NgocHe.phanTramMon(ba, 2, null, 30)));
        Kiem.bang("mot ngoc cap 3 -> +3%", Integer.valueOf(3), Integer.valueOf(NgocHe.phanTramMon(k, 1, null, 10)));
        final int[] st = new int[18];
        NgocHe.congStHe(k, 3, NgocHe.ST_HE_PHAN_VAN_MOI_CAP, st);
        Kiem.bang("ST he: cap 3 lua = 4,5 phan nghin (45 phan van), cap 10 tien = 15", "45,150", st[1] + "," + st[17]);
        final int[] khac = {6, 2, 3, 8, 1, 1, 10, 8, 2, 3, 15, 1, 2, 15, 5, 6, 1, 16};
        final int[] tru = new int[18];
        NgocHe.congKhangTru(k, 3, khac, NgocHe.KHANG_TRU_MOI_CAP, tru);
        Kiem.bang("hai: Ngoc Lua cap 3 -> -3 phan nghin khang Nuoc; Tien cap 10 da mai -> -5 khang Thep", "30,50", tru[2] + "," + tru[16]);
        // tran ST he chung 20% (ChiSoTrangBi.TRAN_DO_HE_PHAN_TRAM): 11 mon x 3 lo cap 10 cung he = 495 phan nghin -> 20%
        Kiem.bang("tran ST he chung 20% van chan 11 mon x 3 ngoc cap 10",
                Integer.valueOf(com.htbb.pokemon.ChiSoTrangBi.TRAN_DO_HE_PHAN_TRAM),
                Integer.valueOf((int) Math.min(com.htbb.pokemon.ChiSoTrangBi.TRAN_DO_HE_PHAN_TRAM, 11L * 3 * 150 / 10 / 10)));
        // gia theo bang du_lieu/noi_dung_ngoc_he.csv (gio_thu_nhap)
        final double[] gio = new double[11];
        for (final String[] r : csv("../du_lieu/noi_dung_ngoc_he.csv")) {
            if ("0".equals(r[0])) {
                gio[Integer.parseInt(r[2])] = Double.parseDouble(r[4]);
            }
        }
        Kiem.bang("gia khảm moi = 1 gio thu nhap", Long.valueOf(10000L), Long.valueOf(NgocHe.giaCap(1, gio, 10000L)));
        Kiem.bang("gia len cap 10 = 28,334 gio (1,45^9)", Long.valueOf(283340L), Long.valueOf(NgocHe.giaCap(10, gio, 10000L)));
        Kiem.bang("bang trong -> cong thuc 1,45^(n-1) (cap 2 = 1,45 gio)", Long.valueOf(14500L), Long.valueOf(NgocHe.giaCap(2, null, 10000L)));
        Kiem.bang("thao 0,5 gio, mai 0,3 gio", "5000,3000", NgocHe.giaGio(NgocHe.GIA_THAO_PHAN_NGHIN_GIO, 10000L) + ","
                + NgocHe.giaGio(NgocHe.GIA_MAI_PHAN_NGHIN_GIO, 10000L));
        double tong = 0;
        for (int c = 1; c <= 10; c++) {
            tong += gio[c];
        }
        Kiem.dung("mot lo cap 10 = ~88 gio thu nhap (ho hut cuoi game, spec 5.4)", tong > 85 && tong < 92);
        // tui ngoc
        final NgocHe.Tui t = new NgocHe.Tui();
        t.them(1, 3, false, 2);
        t.them(17, 10, true, 1);
        t.cheTaoPity = 7;
        t.cheTaoTong = 30;
        final NgocHe.Tui t2 = NgocHe.Tui.giaiMa(t.maHoa());
        Kiem.bang("tui ngoc luu / nap lai", "2,1,7,30", t2.so(1, 3, false) + "," + t2.so(17, 10, true) + "," + t2.cheTaoPity + "," + t2.cheTaoTong);
        Kiem.dung("lay 1 vien: co thi lay, het thi tu choi", t2.lay(17, 10, true) && !t2.lay(17, 10, true));
        Kiem.bang("tong vien", Integer.valueOf(2), Integer.valueOf(t2.tongVien()));
        Kiem.dung("ngoc khong hop le khong vao tui", NgocHe.Tui.giaiMa("g99999=5;cp=x").tongVien() == 0);
    }

    // ------------------------------------------------------------------ che tao

    static void kiemCheTao() {
        final List<String[]> ct = csv("../du_lieu/noi_dung_che_tao.csv");
        Kiem.bang("22 cong thuc (Vong co + Phien da he x 11 bac)", Integer.valueOf(22), Integer.valueOf(ct.size()));
        int sai = 0;
        for (final String[] r : ct) {
            final int bac = Integer.parseInt(r[2]);
            if (Integer.parseInt(r[4]) != CheTaoLogic.daTheoBac(bac) || Integer.parseInt(r[6]) != CheTaoLogic.nlPhuSoTheoBac(bac)
                    || !r[5].equals(CheTaoLogic.nlPhuTheoBac(bac))) {
                sai++;
            }
        }
        Kiem.bang("dau vao: (2 + bac) da, (1 + bac/3) nguyen lieu phu dung nhom bac", Integer.valueOf(0), Integer.valueOf(sai));
        Kiem.bang("xu = 50% gia mon bac (bac 0: 5385 -> 2692)", "2692", ct.get(0)[8]);
        final List<CheTaoLogic.BacHiem> bac = CheTaoLogic.bacMacDinh();
        int tong = 0;
        for (final CheTaoLogic.BacHiem b : bac) {
            tong += b.phanNghin;
        }
        Kiem.bang("tong ti le bac = 1000 phan nghin", Integer.valueOf(1000), Integer.valueOf(tong));
        // phan phoi (khong pity) 200.000 lan, RNG hat giong
        final Random r = new Random(20261019L);
        final int[] dem = new int[5];
        for (int i = 0; i < 200000; i++) {
            dem[CheTaoLogic.quay(bac, r.nextInt(1000), 0, 0).ma]++;
        }
        Kiem.dung("Hiem ~60% (" + dem[1] / 2000.0 + "%)", Math.abs(dem[1] - 120000) < 1500);
        Kiem.dung("Quy ~30% (" + dem[2] / 2000.0 + "%)", Math.abs(dem[2] - 60000) < 1200);
        Kiem.dung("Su thi ~9% (" + dem[3] / 2000.0 + "%)", Math.abs(dem[3] - 18000) < 700);
        Kiem.dung("Huyen thoai ~1% (" + dem[4] / 2000.0 + "%)", Math.abs(dem[4] - 2000) < 250);
        // pity: 19 lan khong ra Su thi -> lan 20 chac Su thi
        int pity = 0;
        boolean dung = true;
        for (int i = 0; i < 19; i++) {
            final CheTaoLogic.BacHiem b = CheTaoLogic.quay(bac, 0, pity, 20); // roll 0 = Hiem
            dung &= b.ma == 1;
            pity = CheTaoLogic.pitySau(pity, b);
        }
        Kiem.dung("19 lan dau roll Hiem van ra Hiem", dung);
        Kiem.bang("bo dem 19", Integer.valueOf(19), Integer.valueOf(pity));
        final CheTaoLogic.BacHiem b20 = CheTaoLogic.quay(bac, 0, pity, 20);
        Kiem.bang("lan thu 20 chac Su thi", "Sử thi", b20.ten);
        Kiem.bang("ra Su thi -> bo dem ve 0", Integer.valueOf(0), Integer.valueOf(CheTaoLogic.pitySau(pity, b20)));
        Kiem.bang("lan 20 ma quay ra Huyen thoai thi giu Huyen thoai", "Huyền thoại", CheTaoLogic.quay(bac, 999, 19, 20).ten);
        Kiem.bang("pity 0 = tat", "Hiếm", CheTaoLogic.quay(bac, 0, 500, 0).ten);
        // pity that: mo phong 100.000 nguoi che toi khi ra Su thi, khong ai qua 20 lan
        final Random r2 = new Random(7L);
        int lauNhat = 0;
        for (int n = 0; n < 100000; n++) {
            int p = 0;
            int lan = 0;
            while (true) {
                lan++;
                final CheTaoLogic.BacHiem b = CheTaoLogic.quay(bac, r2.nextInt(1000), p, 20);
                p = CheTaoLogic.pitySau(p, b);
                if (b.ma >= CheTaoLogic.MA_SU_THI) {
                    break;
                }
            }
            lauNhat = Math.max(lauNhat, lan);
        }
        Kiem.bang("100.000 nguoi: lau nhat 20 lan moi ra Su thi", Integer.valueOf(20), Integer.valueOf(lauNhat));
        Kiem.bang("he so bac: 100 -> 130 (tran NganSachTrangBi 130%)", "130,130,110,0",
                CheTaoLogic.nhanChiSo(100, 130) + "," + CheTaoLogic.nhanChiSo(100, 200) + "," + CheTaoLogic.nhanChiSo(100, 110)
                        + "," + CheTaoLogic.nhanChiSo(0, 130));
        final CheTaoLogic.CongThuc c = new CheTaoLogic.CongThuc(126, "Vòng cổ sơ cấp", 0, 1, 2, "Bụi sao", 1034, 1, 2692, 0, false, true);
        Kiem.dung("du dieu kien -> null", CheTaoLogic.thieu(c, 1, 2692, 2, 1, 0) == null);
        Kiem.bang("thieu xu", "Cần 2692 xu.", CheTaoLogic.thieu(c, 1, 2691, 2, 1, 0));
        Kiem.bang("thieu da", "Cần 2 Đá nâng cấp (có 1).", CheTaoLogic.thieu(c, 1, 9999, 1, 1, 0));
        Kiem.bang("thieu nguyen lieu phu", "Cần 1 Bụi sao (có 0).", CheTaoLogic.thieu(c, 1, 9999, 9, 0, 0));
        final CheTaoLogic.CongThuc cl = new CheTaoLogic.CongThuc(126, "x", 0, 10, 2, "Bụi sao", 1034, 1, 1, 1, false, true);
        Kiem.bang("thieu cap truoc", "Cần cấp 10.", CheTaoLogic.thieu(cl, 9, 99, 9, 9, 9));
        Kiem.bang("thieu loi che tao", "Cần 1 Lõi chế tạo (có 0).", CheTaoLogic.thieu(cl, 10, 99, 9, 9, 0));
        Kiem.bang("cong thuc tat", "Công thức đang tạm khoá.", CheTaoLogic.thieu(
                new CheTaoLogic.CongThuc(1, "x", 0, 1, 0, "", 0, 0, 0, 0, false, false), 99, 99, 9, 9, 9));
    }

    // ------------------------------------------------------------------ moc neo

    static void kiemMocNeo() {
        final int[] moc = NangCapLauDai.MOC_NEO_MAC_DINH;
        Kiem.bang("moc mac dinh +7 / +10 / +13", "7,10,13", s(moc));
        Kiem.bang("moc giu: +6 -> 0, +7 -> 7, +9 -> 7, +12 -> 10, +15 -> 13", "0,7,7,10,13", NangCapLauDai.mocGiu(6, moc) + ","
                + NangCapLauDai.mocGiu(7, moc) + "," + NangCapLauDai.mocGiu(9, moc) + "," + NangCapLauDai.mocGiu(12, moc) + ","
                + NangCapLauDai.mocGiu(15, moc));
        // truot (khong bua): luat cu -1 tu +7
        Kiem.bang("+6 truot len +7: chua dat moc -> +5", Integer.valueOf(5), Integer.valueOf(NangCapLauDai.capSauTruot(6, 7, false, moc)));
        Kiem.bang("+7 truot len +8: giu moc +7", Integer.valueOf(7), Integer.valueOf(NangCapLauDai.capSauTruot(7, 8, false, moc)));
        Kiem.bang("+9 truot len +10: -1 con +8 (tren moc 7)", Integer.valueOf(8), Integer.valueOf(NangCapLauDai.capSauTruot(9, 10, false, moc)));
        Kiem.bang("+10 truot len +11: giu +10", Integer.valueOf(10), Integer.valueOf(NangCapLauDai.capSauTruot(10, 11, false, moc)));
        Kiem.bang("+13 truot len +14: giu +13", Integer.valueOf(13), Integer.valueOf(NangCapLauDai.capSauTruot(13, 14, false, moc)));
        Kiem.bang("+14 truot len +15: con +13", Integer.valueOf(13), Integer.valueOf(NangCapLauDai.capSauTruot(14, 15, false, moc)));
        Kiem.bang("+3 truot len +4: giu cap nhu cu", Integer.valueOf(3), Integer.valueOf(NangCapLauDai.capSauTruot(3, 4, false, moc)));
        Kiem.bang("co bua: giu cap", Integer.valueOf(9), Integer.valueOf(NangCapLauDai.capSauTruot(9, 10, true, moc)));
        Kiem.bang("tat moc neo (mang rong) = luat cu", Integer.valueOf(6), Integer.valueOf(NangCapLauDai.capSauTruot(7, 8, false, new int[0])));
        // moi cap: moc neo khong bao gio lam cap SAU TRUOT thap hon luat cu, va khong bao gio cao hon cap hien tai
        boolean ok = true;
        for (int cu = 0; cu < 15; cu++) {
            final int a = NangCapLauDai.capSauTruot(cu, cu + 1, false);
            final int b = NangCapLauDai.capSauTruot(cu, cu + 1, false, moc);
            ok &= b >= a && b <= cu && b >= NangCapLauDai.mocGiu(cu, moc);
        }
        Kiem.dung("moi +k: cu <= neo <= hien tai, neo >= moc da dat", ok);
        // pity tren nut: "Ti le 38% (+13% pity)"
        Kiem.bang("+10: goc 38%, sau 1 lan truot +13% pity", "38,51,13", NangCapLauDai.tiLe(10, 0) + "," + NangCapLauDai.tiLe(10, 1)
                + "," + NangCapLauDai.pityCong(10, 1));
        Kiem.bang("pity khong vuot 100%", Integer.valueOf(100), Integer.valueOf(NangCapLauDai.tiLe(3, 50)));
        // he so xu buoc cao
        Kiem.bang("+10 khong doi gia", Long.valueOf(NangCapLauDai.chiPhi(50, 10)), Long.valueOf(NangCapLauDai.chiPhi(50, 10, 1060)));
        Kiem.bang("+11 x1,06", Long.valueOf(NangCapLauDai.chiPhi(50, 11) * 1060 / 1000), Long.valueOf(NangCapLauDai.chiPhi(50, 11, 1060)));
        Kiem.bang("he so tran x2", Long.valueOf(NangCapLauDai.chiPhi(50, 15) * 2), Long.valueOf(NangCapLauDai.chiPhi(50, 15, 9000)));
        // ky vong xu len +15 (mo phong, RNG hat giong; bac 5). Che do "bua tu +11" (nguoi choi dung bua luong khoa cho
        // buoc cao) la che do spec 4.4 uoc "moc neo giam ~6%, bu x1,06": kiem moc neo giam, he so dua ve +-5% luat cu.
        final double[] cu = kyVong(new int[0], 1000, 11, 20000, 1L);
        final double[] neo = kyVong(moc, 1000, 11, 20000, 1L);
        final double[] bu = kyVong(moc, 1060, 11, 20000, 1L);
        Kiem.dung("bua tu +11: moc neo giam xu (" + pt(neo[0], cu[0]) + " luat cu)", neo[0] < cu[0]);
        Kiem.dung("bua tu +11: he so x1,06 buoc cao dua xu ve +-5% luat cu (" + pt(bu[0], cu[0]) + ")",
                bu[0] >= cu[0] * 0.95 && bu[0] <= cu[0] * 1.05);
        final double[] buaCu = kyVong(new int[0], 1000, 7, 20000, 2L);
        final double[] buaNeo = kyVong(moc, 1060, 7, 20000, 2L);
        Kiem.dung("bua tu +7: moc neo tiet kiem bua luong khoa (" + Math.round(buaCu[2] * 10) / 10.0 + " -> "
                + Math.round(buaNeo[2] * 10) / 10.0 + " bua / mon), xu " + pt(buaNeo[0], buaCu[0]), buaNeo[2] < buaCu[2]
                && buaNeo[0] >= buaCu[0]);
        final double[] tran = kyVong(new int[0], 1000, 99, 2000, 3L);
        final double[] tranNeo = kyVong(moc, 1060, 99, 2000, 3L);
        Kiem.dung("khong bua: moc neo bo duoc 'roi vo tan' (luat cu " + Math.round(tran[1]) + " lan -> " + Math.round(tranNeo[1])
                + " lan / mon)", tranNeo[1] * 10 < tran[1]);
    }

    static String pt(final double a, final double b) {
        return Math.round(a * 1000 / b) / 10.0 + "%";
    }

    /** {xu, so lan thu, so bua da tieu} trung binh de nang +0 -> +15 o bac 5; bua dung tu buoc +{@code buaTu}. */
    static double[] kyVong(final int[] moc, final int heSo, final int buaTu, final int lan, final long hat) {
        final Random r = new Random(hat);
        double tong = 0;
        double thu = 0;
        double bua = 0;
        for (int n = 0; n < lan; n++) {
            int p = 0;
            int truot = 0;
            while (p < 15) {
                final int k = p + 1;
                tong += NangCapLauDai.chiPhi(50, k, heSo);
                thu++;
                if (NangCapLauDai.thanhCong(r.nextInt(100), NangCapLauDai.tiLe(k, truot))) {
                    p = k;
                    truot = 0;
                } else {
                    truot++;
                    final boolean coBua = k >= buaTu;
                    // nhu NangCapLauDaiGame: chi tieu bua khi that su se rot cap
                    if (coBua && NangCapLauDai.capSauTruot(p, k, false, moc) < p) {
                        bua++;
                    }
                    p = NangCapLauDai.capSauTruot(p, k, coBua, moc);
                }
            }
        }
        return new double[]{tong / lan, thu / lan, bua / lan};
    }

    // ------------------------------------------------------------------ SQL + cau hinh

    static void kiemSqlCauHinh() {
        final String sql = doc("../sql/them_bo_ngoc_che_tao_bat.sql");
        Kiem.dung("doc duoc sql/them_bo_ngoc_che_tao_bat.sql", !sql.isEmpty());
        for (final String b : new String[]{"data_ngoc_he_lo", "data_ngoc_he_khac", "data_che_tao_bac", "data_nang_cap_moc",
            "data_bat_bong", "data_bat_loai", "tob_ho_so_bo_ngoc", "tob_hop_pc"}) {
            Kiem.dung("CREATE TABLE IF NOT EXISTS " + b, sql.contains("CREATE TABLE IF NOT EXISTS `" + b + "`"));
        }
        Kiem.dung("khong DROP / TRUNCATE / DELETE (chay lai duoc, giu du lieu)", !sql.matches("(?is).*\\b(DROP|TRUNCATE|DELETE)\\b.*"));
        Kiem.dung("khong INSERT ... SELECT ... ON DUPLICATE KEY UPDATE", !sql.matches("(?is).*INSERT[^;]*SELECT[^;]*ON DUPLICATE.*"));
        int insert = 0;
        int dung = 0;
        for (final String cau : sql.split(";")) {
            if (cau.contains("INSERT INTO")) {
                insert++;
                if (cau.contains("ON DUPLICATE KEY UPDATE") && cau.contains("= VALUES(`") && !cau.matches("(?s).*`(\\w+)` = `\\1`.*")) {
                    dung++;
                }
            }
        }
        Kiem.bang("moi INSERT co ON DUPLICATE KEY UPDATE cot = VALUES(cot)", Integer.valueOf(insert), Integer.valueOf(dung));
        Kiem.dung("ADD COLUMN IF NOT EXISTS loi_che_tao / so_bat", sql.contains("ADD COLUMN IF NOT EXISTS `loi_che_tao`")
                && sql.contains("ADD COLUMN IF NOT EXISTS `so_bat`"));
        Kiem.dung("moc neo 7 / 10 / 13", sql.contains("VALUES (7), (10), (13)"));
        Kiem.dung("ti le bac 600 / 300 / 90 / 10", sql.contains("(1, 'Hiếm', 3, 600, 110, 1)") && sql.contains("(4, 'Huyền thoại', 1, 10, 130, 3)"));
        // khoa cau hinh: co trong danh muc, mac dinh khop ma
        final String dm = doc("../tools/cauhinh/danh_muc.json");
        final String[][] khoa = {
            {"bo.bat", "1"}, {"bo.tran_phan_tram", "5"}, {"bo.tran_chi_mang_phan_nghin", "30"}, {"ngoc_he.bat", "1"},
            {"ngoc_he.tran_phan_tram_mon", String.valueOf(NgocHe.TRAN_PHAN_TRAM_MON)},
            {"ngoc_he.st_he_phan_van_moi_cap", String.valueOf(NgocHe.ST_HE_PHAN_VAN_MOI_CAP)},
            {"ngoc_he.khang_tru_phan_nghin_moi_cap", String.valueOf(NgocHe.KHANG_TRU_MOI_CAP)},
            {"ngoc_he.gia_thao_phan_nghin_gio", String.valueOf(NgocHe.GIA_THAO_PHAN_NGHIN_GIO)},
            {"ngoc_he.gia_mai_phan_nghin_gio", String.valueOf(NgocHe.GIA_MAI_PHAN_NGHIN_GIO)},
            {"che_tao.bat", "1"}, {"che_tao.pity", String.valueOf(CheTaoLogic.PITY_MAC_DINH)}, {"nang_cap.moc_neo", "1"},
            {"nang_cap.he_so_xu_bac_cao", String.valueOf(NangCapLauDai.HE_SO_XU_BAC_CAO_MAC_DINH)},
        };
        for (final String[] k : khoa) {
            Kiem.dung("danh muc cau hinh: " + k[0] + " mac dinh " + k[1],
                    dm.contains("\"khoa\": \"" + k[0] + "\"") && dm.matches("(?s).*\"khoa\": \"" + k[0].replace(".", "\\.")
                            + "\"[^}]*\"macDinh\": \"" + k[1] + "\".*"));
        }
        Kiem.bang("tran thuong bo = GioiHanChiSo.THUONG_BO_TRAN_PHAN_TRAM", Integer.valueOf(5),
                Integer.valueOf(GioiHanChiSo.THUONG_BO_TRAN_PHAN_TRAM));
        Kiem.bang("cap ngoc toi da = ThongSoLauDai.NGOC_HE_CAP_TOI_DA", Integer.valueOf(ThongSoLauDai.NGOC_HE_CAP_TOI_DA),
                Integer.valueOf(NgocHe.CAP_TOI_DA));
    }
}
