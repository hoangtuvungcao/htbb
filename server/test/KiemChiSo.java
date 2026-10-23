import com.htbb.pokemon.ChiSoTrangBi;
import com.htbb.pokemon.GioiHanChiSo;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.NganSachTrangBi;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chi so trang bi + chong lam phat (docs/thiet-ke-game.md): tran / giam dan, san sat thuong, PvP,
 * ngan sach mon, bang 198 dong chi so (data_attribute trong sql/htbb.sql). Mo phong TTK cu (MoPhongChiSo) da luu
 * tru cung bo sinh (tools/luu_tru_sinh_du_lieu/pokemon/).
 */
public final class KiemChiSo {

    private KiemChiSo() {
    }

    public static void chay() {
        Kiem.nhom("Chi so trang bi - tran, giam dan, PvP (com.htbb.pokemon.GioiHanChiSo)");
        kiemGiamDan();
        kiemTran();
        Kiem.nhom("Chi so trang bi - ngan sach mon (com.htbb.pokemon.NganSachTrangBi)");
        kiemNganSach();
        Kiem.nhom("Chi so trang bi - bang chi so 198 dong (com.htbb.pokemon.ChiSoTrangBi)");
        kiemBang();
        Kiem.nhom("Chi so trang bi - bang data_attribute trong CSDL + code goi");
        kiemSqlVaCode();
    }

    // ------------------------------------------------------------------ giam dan
    static void kiemGiamDan() {
        Kiem.bang("duoi goi giu nguyen", 200, GioiHanChiSo.giamDan(200, 250, 400));
        Kiem.bang("dung goi", 250, GioiHanChiSo.giamDan(250, 250, 400));
        Kiem.bang("am -> 0", 0, GioiHanChiSo.giamDan(-5, 250, 400));
        boolean donDieu = true;
        boolean duoiTran = true;
        int truoc = -1;
        for (int x = 0; x <= 100000; x += 37) {
            final int v = GioiHanChiSo.giamDan(x, 250, 400);
            donDieu &= v >= truoc;
            duoiTran &= v < 400;
            truoc = v;
        }
        Kiem.dung("giam dan don dieu khong giam", donDieu);
        Kiem.dung("giam dan khong bao gio cham tran (x toi 100000)", duoiTran);
        Kiem.dung("tren goi tang cham hon (them 150 -> them < 150)", GioiHanChiSo.giamDan(400, 250, 400) - 250 < 150);
        Kiem.bang("tren goi: 250 + 150*150/300 = 325", 325, GioiHanChiSo.giamDan(400, 250, 400));
    }

    // ------------------------------------------------------------------ tran
    static void kiemTran() {
        // chi mang
        Kiem.dung("chi mang PvE < 40% du cong bao nhieu", GioiHanChiSo.chiMang(5000, false) < 400);
        Kiem.dung("chi mang PvP < 30%", GioiHanChiSo.chiMang(5000, true) < 300);
        Kiem.bang("chi mang tho: 30 diem /300 = 100 phan nghin + bao kich 50", 150L, GioiHanChiSo.chiMangTho(30, 50));
        Kiem.dung("giu chi mang PvP: ti le that = tran PvP", Math.abs(GioiHanChiSo.chiMang(800, false)
                * GioiHanChiSo.giuChiMangPvp(800) / 1000 - GioiHanChiSo.chiMang(800, true)) <= 1);
        Kiem.bang("chi mang it (10%) PvP giu nguyen", 1000, GioiHanChiSo.giuChiMangPvp(100));
        // ST chi mang
        Kiem.bang("he so chi mang nen x1,5", 150, GioiHanChiSo.heSoChiMangPhanTram(0));
        Kiem.dung("he so chi mang < x2,0 (= +100% so voi don thuong)", GioiHanChiSo.heSoChiMangPhanTram(100000) < 200);
        // ne, hut, giam, khang
        Kiem.dung("ne PvE < 30%", GioiHanChiSo.neTranh(5000, false) < 300);
        Kiem.dung("ne PvP < 20%", GioiHanChiSo.neTranh(5000, true) < 200);
        Kiem.dung("hut HP PvE < 10%", GioiHanChiSo.hut(5000, false) < 100);
        Kiem.dung("hut HP PvP < 5%", GioiHanChiSo.hut(5000, true) < 50);
        Kiem.dung("giam ST PvE < 50%", GioiHanChiSo.giamSatThuong(5000, false) < 500);
        Kiem.dung("giam ST PvP < 40%", GioiHanChiSo.giamSatThuong(5000, true) < 400);
        Kiem.dung("khang mot he < 60%", GioiHanChiSo.khangHe(5000) < 600);
        Kiem.bang("phan don PvE tran 15%", 150, GioiHanChiSo.phanDon(900, false));
        Kiem.bang("phan don PvP tran 10%", 100, GioiHanChiSo.phanDon(900, true));
        Kiem.bang("toc danh: giam hoi chieu toi da 33% (= +50% toc danh)", 33, GioiHanChiSo.giamHoiChieuPhanTram(90));
        Kiem.bang("toc danh: 20% giu nguyen", 20, GioiHanChiSo.giamHoiChieuPhanTram(20));
        Kiem.bang("toc chay them toi da 30% cua 5 = 1", 1, GioiHanChiSo.tocChayThemToiDa(5));
        Kiem.bang("ST len boss tran 20%", 200, GioiHanChiSo.tran(999, GioiHanChiSo.ST_BOSS_TRAN));
        Kiem.bang("EXP trang bi tran 30%", 30, GioiHanChiSo.expTrangBi(95));
        Kiem.bang("roi do tran 20%", 20, GioiHanChiSo.roiDo(60));
        Kiem.bang("roi xu tran 10%", 10, GioiHanChiSo.roiXu(60));
        Kiem.bang("thuong bo +12% -> 5%", 5, GioiHanChiSo.thuongBo(12));
        Kiem.bang("thuong bo +1% giu", 1, GioiHanChiSo.thuongBo(1));
        Kiem.bang("hoi nhip tran 1% HP toi da", 100, GioiHanChiSo.hoiNhip(5000, 10000));
        Kiem.bang("hoi nhip nho giu nguyen", 30, GioiHanChiSo.hoiNhip(30, 10000));
        Kiem.bang("tong % cong them tran +50%", 1500, GioiHanChiSo.kepPhanTramCong(3000, 1000));
        Kiem.bang("tong % duoi tran giu", 1200, GioiHanChiSo.kepPhanTramCong(1200, 1000));
        // % HP muc tieu
        Kiem.bang("ST theo % HP: thuong tran 1%", 10000L, GioiHanChiSo.stTheoHp(1000000, 60, false, 999999));
        Kiem.bang("ST theo % HP: boss tran 0,2%", 2000L, GioiHanChiSo.stTheoHp(1000000, 60, true, 999999));
        Kiem.bang("ST theo % HP: khong qua don goc", 500L, GioiHanChiSo.stTheoHp(1000000, 60, false, 500));
        // hut mot don
        Kiem.bang("hut mot don: 10% cua 1000", 100L, GioiHanChiSo.hutMotDon(1000, 100, false, 1000000));
        Kiem.bang("hut mot don: thu thach x10%", 10L, GioiHanChiSo.hutMotDon(1000, 100, true, 1000000));
        Kiem.bang("hut mot don: <= 0,2% HP toi da", 20L, GioiHanChiSo.hutMotDon(100000, 100, false, 10000));
        Kiem.bang("PvP hut tinh tren sat thuong that x0,30", 300L, GioiHanChiSo.satThuongThatPvp(1000));
        // trang thai
        Kiem.dung("gay trang thai PvE < 15%", GioiHanChiSo.gayTrangThai(5000, false) < 150);
        Kiem.dung("gay trang thai PvP = nua PvE", GioiHanChiSo.gayTrangThai(5000, true) == GioiHanChiSo.gayTrangThai(5000, false) / 2);
        Kiem.dung("khang trang thai < 50%", GioiHanChiSo.khangTrangThai(5000) < 500);
        // san sat thuong
        int conLai = 1000;
        for (int i = 0; i < 10; i++) {
            final int g = GioiHanChiSo.giamDuocPhep(conLai, 900);
            conLai = conLai * (1000 - g) / 1000;
        }
        Kiem.dung("san sat thuong: 10 lop giam 90% van con >= 40%", conLai >= GioiHanChiSo.SAN_SAT_THUONG - 1);
        Kiem.bang("san sat thuong: lop dau 90% -> chi duoc 60%", 600, GioiHanChiSo.giamDuocPhep(1000, 900));
        Kiem.bang("san sat thuong: lop nho giu nguyen", 200, GioiHanChiSo.giamDuocPhep(1000, 200));
        Kiem.bang("san sat thuong: da o san -> 0", 0, GioiHanChiSo.giamDuocPhep(400, 500));
    }

    // ------------------------------------------------------------------ ngan sach
    static void kiemNganSach() {
        Kiem.dung("o 0..12, 19 co ngan sach", NganSachTrangBi.coNganSach(0) && NganSachTrangBi.coNganSach(12)
                && NganSachTrangBi.coNganSach(19));
        Kiem.sai("do thu cuoi (14..18) khong ngan sach", NganSachTrangBi.coNganSach(15));
        Kiem.bang("chuan do cam tay Charmander cap 21 = 110", 110.0, NganSachTrangBi.chuan(3, 0, 21));
        Kiem.bang("chuan noi suy cap 16 = 90", 90.0, NganSachTrangBi.chuan(3, 0, 16));
        Kiem.bang("tran mon +0 = ceil(110 x 1,3) + 2", 145, NganSachTrangBi.tranMon(3, 0, 21, 0));
        Kiem.bang("tran mon +15 = ceil(110 x 1,3 x 1,55) + 2", (int) Math.ceil(110 * 1.3 * 1.55) + 2, NganSachTrangBi.tranMon(3, 0, 21, 15));
        Kiem.bang("dong ngoai o (thu tren do cam tay cap 21)", 6, NganSachTrangBi.tranMon(3, 1, 21, 0));
        Kiem.bang("do tim cu 193 tan cong cap 21 bi kep ve 145", 145, NganSachTrangBi.kep(3, 0, 21, 0, 21, 193));
        Kiem.bang("trong ngan sach giu nguyen", 110, NganSachTrangBi.kep(3, 0, 21, 0, 21, 110));
        Kiem.bang("bac theo cap 25 (do cam tay) = 2", 2, NganSachTrangBi.bac(3, 25));
        Kiem.dung("do bac cu: tran theo bac hien tai", NganSachTrangBi.tranBacCu(3, 0, 31, 45) < Integer.MAX_VALUE);
        Kiem.bang("do cung bac: khong tran bac cu", Integer.MAX_VALUE, NganSachTrangBi.tranBacCu(3, 0, 41, 45));
        boolean bacCu = true;
        for (final int t : NganSachTrangBi.cacO()) {
            for (int b = 1; b <= 10; b++) {
                bacCu &= NganSachTrangBi.tiLeBacCuSauKep(t, b) <= 1.30 + 1e-9;
            }
        }
        Kiem.dung("moi o, moi bac: do bac cu +15 sau kep <= 130% do bac moi +0", bacCu);
        final List<String> loi = NganSachTrangBi.kiemMau(3, 21, new int[]{193, 10, 10, 13, 10, 10, 0, 0, 0, 0});
        Kiem.dung("kiem mau bat duoc do vuot (id 214)", loi.size() >= 5);
        Kiem.dung("thoi trang (atb9=1) bo qua", NganSachTrangBi.kiemMau(0, 1, new int[]{10, 10, 3000, 3000, 19, 19, 19, 19, 19, 1}).isEmpty());
        Kiem.dung("khung che tao (moi atb <= 1) bo qua", NganSachTrangBi.kiemMau(0, 40, new int[]{0, 1, 1, 1, 1, 1, 1, 1, 1, 0}).isEmpty());
        boolean donDieu = true;
        for (final int t : NganSachTrangBi.cacO()) {
            final int idx = NganSachTrangBi.chiSoChinh(t);
            for (int b = 1; b <= 10; b++) {
                donDieu &= NganSachTrangBi.giaTriMoc(t, idx, b) >= NganSachTrangBi.giaTriMoc(t, idx, b - 1);
            }
        }
        Kiem.dung("chuan chi so chinh tang dan theo bac o moi o", donDieu);
    }

    // ------------------------------------------------------------------ bang
    static void kiemBang() {
        Kiem.bang("198 dong", 198, ChiSoTrangBi.BANG.length);
        final Set<Integer> id = new HashSet<Integer>();
        boolean thuTu = true;
        int truoc = -1;
        boolean tenOk = true;
        boolean tenNgan = true;
        boolean khongGhiChu = true;
        for (final ChiSoTrangBi.Dong d : ChiSoTrangBi.BANG) {
            id.add(d.id);
            thuTu &= d.id > truoc;
            truoc = d.id;
            tenOk &= d.ten != null && !d.ten.trim().isEmpty();
            tenNgan &= d.ten.length() <= 45 && d.moTa.length() <= 100;
            // mo ta nguoi choi doc: co dau, khong ghi chu noi bo kieu "max 3%", "otheratt", "skin thu cuoi:"
            khongGhiChu &= !d.moTa.matches("(?i).*(max \\d|otheratt|skin thu cuoi|tang % xuat hien|ko tinh).*");
        }
        boolean du = true;
        for (int i = 0; i <= 186; i++) {
            du &= id.contains(i);
        }
        for (int i = 200; i <= 210; i++) {
            du &= id.contains(i);
        }
        Kiem.dung("du id 0..186 va 200..210, khong them id (client tra ten theo vi tri)", du && id.size() == 198);
        Kiem.dung("dung thu tu id", thuTu);
        Kiem.dung("moi dong co ten", tenOk);
        Kiem.dung("ten <= 45, mo ta <= 100 ky tu (cot data_attribute)", tenNgan);
        Kiem.dung("mo ta khong con ghi chu noi bo", khongGhiChu);
        Kiem.dung("khong con ten 'chưa dùng' / 'chỉ số 7..9'", !coTen("chưa dùng") && !coTen("chỉ số 7") && !coTen("chỉ số 9"));
        Kiem.dung("lan (66..74, 85..92) ngung + an", ChiSoTrangBi.an(66) && ChiSoTrangBi.an(70) && ChiSoTrangBi.an(85)
                && ChiSoTrangBi.an(89) && ChiSoTrangBi.an(92));
        Kiem.dung("bien nguoi tuyet (75) ngung", ChiSoTrangBi.ngung(75));
        Kiem.dung("ma hinh thoi trang (7..9) noi bo + an", ChiSoTrangBi.an(7) && ChiSoTrangBi.an(9) && !ChiSoTrangBi.ngung(7));
        Kiem.sai("tan cong (0) khong an", ChiSoTrangBi.an(0));
        Kiem.bang("trang thai cu 14 -> Gay ngu", "Gây ngủ", ChiSoTrangBi.lay(14).ten);
        Kiem.bang("trang thai cu 18 -> Gay bong", "Gây bỏng", ChiSoTrangBi.lay(18).ten);
        Kiem.bang("trang thai luu phan nghin -> ispercent 2", 2, ChiSoTrangBi.lay(15).phanTram);
        Kiem.bang("HP moi diem +1.000 (khong con 'Tang HP: 2' gay hieu nham)", "HP (mỗi điểm +1.000)", ChiSoTrangBi.lay(33).ten);
        // 5 he cu -> he Pokemon
        final int[] g = new int[187];
        g[120] = 100; // Khang Lua 10% (phan nghin)
        g[155] = 5;   // Khang Lua 5% (phan tram)
        g[186] = 30;  // khang moi he 3%
        Kiem.bang("khang he Lua = 100 + 50 + 30", 180L, ChiSoTrangBi.khangHeTho(g, HePokemon.LUA));
        Kiem.bang("khang he Bang chi co khang moi he", 30L, ChiSoTrangBi.khangHeTho(g, HePokemon.BANG));
        g[ChiSoTrangBi.ID_KHANG_HE_PHIEN_DA] = ChiSoTrangBi.maDongPhienDa(HePokemon.BANG, 50);
        Kiem.bang("phien da he Bang (co che gon cho 13 he con lai)", 80L, ChiSoTrangBi.khangHeTho(g, HePokemon.BANG));
        Kiem.bang("ma dong phien da: he", (int) HePokemon.TIEN, ChiSoTrangBi.heCuaDongPhienDa(ChiSoTrangBi.maDongPhienDa(HePokemon.TIEN, 7)));
        Kiem.bang("ma dong phien da: phan nghin", 7, ChiSoTrangBi.phanNghinDongPhienDa(ChiSoTrangBi.maDongPhienDa(HePokemon.TIEN, 7)));
        g[166] = 8; // ST he Lua 8%
        Kiem.bang("ST he Lua 8% = 80 phan nghin", 80L, ChiSoTrangBi.stHeTho(g, HePokemon.LUA));
        boolean du5 = true;
        for (final int he : new int[]{HePokemon.LUA, HePokemon.NUOC, HePokemon.CO, HePokemon.THEP, HePokemon.DAT}) {
            boolean a = false;
            boolean b = false;
            for (final int[] x : ChiSoTrangBi.KHANG_HE_PHAN_NGHIN) {
                a |= x[1] == he;
            }
            for (final int[] x : ChiSoTrangBi.KHANG_HE_PHAN_TRAM) {
                b |= x[1] == he;
            }
            du5 &= a && b;
        }
        Kiem.dung("5 he cu (Lua/Nuoc/Co/Thep/Dat) anh xa du o ca hai kieu luu", du5);
    }

    static boolean coTen(final String s) {
        for (final ChiSoTrangBi.Dong d : ChiSoTrangBi.BANG) {
            if (d.ten.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ SQL / code
    static void kiemSqlVaCode() {
        int dem = 0;
        boolean khop = true;
        for (final ChiSoTrangBi.Dong d : ChiSoTrangBi.BANG) {
            final java.util.Map<String, String> r = DayDu.tim("data_attribute", "id", String.valueOf(d.id));
            if (r != null && d.ten.equals(r.get("name")) && String.valueOf(d.phanTram).equals(r.get("ispercent"))) {
                dem++;
            } else {
                khop = false;
                System.out.println("   data_attribute " + d.id + " lech: " + r);
            }
        }
        Kiem.bang("CSDL co dong data_attribute cho moi dong chi so (khop ChiSoTrangBi: ten, ispercent)", ChiSoTrangBi.BANG.length, dem);
        Kiem.dung("CSDL khop ten / don vi", khop);
        Kiem.bang("data_attribute dung 198 dong (client tra theo vi tri)", 198, DayDu.soDong("data_attribute"));
        Kiem.dung("tai lieu thiet-ke-game.md co GioiHanChiSo", doc("../docs/thiet-ke-game.md").contains("GioiHanChiSo"));
        // code goi dung cho
        final String ch = doc("src/real/Char.java");
        Kiem.dung("Char.havecrit qua gioi han", ch.contains("ChiSoTrangBiGame.quayChiMang(this)"));
        Kiem.dung("Char.tkNhanChiMang qua gioi han", ch.contains("ChiSoTrangBiGame.heSoChiMang(this)"));
        Kiem.dung("Char.checkHapthuSatThuong qua san sat thuong", ch.contains("ChiSoTrangBiGame.tranGiam(pn, actor instanceof Char)"));
        Kiem.dung("Char.calculateAttrib cap nhat bang thuoc tinh", ch.contains("ChiSoTrangBiGame.capNhat(this)"));
        final String cs = doc("src/real/CharChiSo.java");
        Kiem.dung("CharChiSo.applyThanKhiOnHit qua ban co tran", cs.contains("ChiSoTrangBiGame.trenDon(self, actor, finalDame)"));
        Kiem.dung("CharChiSo kep tong % tan cong", cs.contains("ChiSoTrangBiGame.kepPhanTram(self.attack, gocTc)"));
        Kiem.dung("CharChiSo kep chi so mon", cs.contains("ChiSoTrangBiGame.atb(self, item, 0)"));
        Kiem.dung("CharChiSo truyen do he vao SatThuongLauDai", cs.contains("ChiSoTrangBiGame.doHePhanTram(self, heDung)"));
        for (final String f : new String[]{"Region", "MapDanhNhau", "MapLienDau", "RegionVantienTran"}) {
            Kiem.dung("PvP " + f + " qua tran chi mang PvP", doc("src/real/" + f + ".java").contains("ChiSoTrangBiGame.giuChiMangPvp(p, c)"));
        }
        Kiem.dung("lan ngung khi bat", doc("src/real/CharTrangBi.java").contains("if (ChiSoTrangBiGame.bat()) {\n            return; // \"lan\""));
        Kiem.dung("EXP tu trang bi kep tran", doc("src/real/ExpLauDai.java").contains("ChiSoTrangBiGame.expTrangBi(tb)"));
        Kiem.dung("tooltip loc dong ngung", doc("src/real/Item.java").contains("ChiSoTrangBiGame.locHienThi(this, allAtb"));
        Kiem.dung("khoa cau hinh chi_so.* dang ky", DayDu.danhMucJson().contains("\"chi_so.san_sat_thuong\""));
    }

    static String doc(final String p) {
        try {
            return new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
