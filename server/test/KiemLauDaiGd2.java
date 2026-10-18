import com.htbb.pokemon.CapDoLauDai;
import com.htbb.pokemon.ChiSoKinhTe;
import com.htbb.pokemon.CuonHut;
import com.htbb.pokemon.GiaiDoanBoss;
import com.htbb.pokemon.GoiLauDai;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.KinhTeLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangCapLauDai;
import com.htbb.pokemon.NangLuong;
import com.htbb.pokemon.NhiemVuLauDai;
import com.htbb.pokemon.SatThuongLauDai;
import com.htbb.pokemon.SuKienLauDai;
import com.htbb.pokemon.ThangPvp;
import com.htbb.pokemon.ThongSoLauDai;
import com.htbb.pokemon.TrangThaiPokemon;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Giai doan 2 cua thiet ke dai han (docs/thiet-ke-game.md): ham thuan cua tung
 * buoc + cac sua loi buoc 1. Co sv.lau_dai TAT trong moi test cu (mac dinh) - test nay tu bat/tat
 * va tra lai trang thai cu.
 */
public final class KiemLauDaiGd2 {

    private KiemLauDaiGd2() {
    }

    public static void chay() {
        final boolean cu = LauDai.bat();
        try {
            buoc0();
            buoc1();
            buoc2();
            buoc3();
            buoc4();
            buoc5();
            buoc6();
            buoc7();
            yeuCauThuong();
            gd2b();
        } finally {
            LauDai.dat(cu);
            LauDai.apDungCauHinh(new Properties());
        }
    }

    // ------------------------------------------------------------------ buoc 0

    static void buoc0() {
        Kiem.nhom("GD2 buoc 0 - co sv.lau_dai, LauDai, bang kinh te");
        Kiem.sai("thieu dong = tat", LauDai.docGiaTri(null));
        Kiem.dung("\"1\" = bat", LauDai.docGiaTri(" 1 "));
        Kiem.sai("\"0\" = tat", LauDai.docGiaTri("0"));
        Kiem.sai("mac dinh tat khi chay ngoai server", LauDai.bat());
        final String tmpl = doc("../vps/server.ini.tmpl");
        Kiem.dung("ban mau co sv.lau_dai=1 (production)", tmpl.contains("\nsv.lau_dai=1\n"));
        Kiem.dung("ban mau co sv.ngay_khai_truong", tmpl.contains("\nsv.ngay_khai_truong="));
        final String sv = doc("src/server/TeamServer.java");
        Kiem.dung("TeamServer doc sv.lau_dai", sv.contains("p.getProperty(\"sv.lau_dai\")"));
        Kiem.dung("TeamServer doc sv.ngay_khai_truong", sv.contains("p.getProperty(\"sv.ngay_khai_truong\")"));
        Kiem.dung("CauHinh.napLai chuyen cau_hinh cho LauDai", doc("src/server/CauHinh.java").contains("LauDai.apDungCauHinh(p)"));

        final Properties p = new Properties();
        p.setProperty("thue.cho", "8");
        p.setProperty("sv.tileXu", "100");
        p.setProperty("roi.xu.phan_tram", "abc");
        LauDai.apDungCauHinh(p);
        Kiem.bang("doc de thue.cho tu cau_hinh", Integer.valueOf(8), Integer.valueOf(LauDai.so("thue.cho", 6)));
        Kiem.bang("khoa sv.* khong lot vao LauDai", Integer.valueOf(-1), Integer.valueOf(LauDai.so("sv.tileXu", -1)));
        Kiem.bang("gia tri hong -> mac dinh", Integer.valueOf(30), Integer.valueOf(LauDai.so("roi.xu.phan_tram", 30)));
        LauDai.apDungCauHinh(new Properties());

        final long kt = LauDai.docNgay("2026-10-01");
        Kiem.dung("doc duoc ngay khai truong", kt > 0L);
        Kiem.bang("ngay sai dinh dang = 0", Long.valueOf(0L), Long.valueOf(LauDai.docNgay("01/10/2026")));
        Kiem.bang("10 ngay sau khai truong", Integer.valueOf(10), Integer.valueOf(LauDai.ngayTuKhaiTruong(kt, kt + 10L * 86400000L + 5000L)));
        Kiem.bang("chua dat ngay = ngay 0", Integer.valueOf(0), Integer.valueOf(LauDai.ngayTuKhaiTruong(0L, System.currentTimeMillis())));
        LauDai.datTranTheoMua(true);
        Kiem.bang("mua S1 ngay 59", Integer.valueOf(0), Integer.valueOf(LauDai.mua(59)));
        Kiem.bang("mua S4 ngay 240", Integer.valueOf(3), Integer.valueOf(LauDai.mua(240)));
        LauDai.datTranTheoMua(false);
        Kiem.bang("tran theo mua tat (mac dinh): mua cuoi -> tinh thong du 50", Integer.valueOf(5), Integer.valueOf(LauDai.mua(0)));

        LauDai.dat(false);
        LauDai.ghiXuVao(100L);
        Kiem.bang("tat co: khong dem xu vao", Long.valueOf(0L), Long.valueOf(LauDai.layVaDatLaiXu()[0]));
        LauDai.dat(true);
        LauDai.ghiXuVao(100L);
        LauDai.ghiXuRa(40L);
        final long[] vr = LauDai.layVaDatLaiXu();
        Kiem.bang("bat co: dem xu vao", Long.valueOf(100L), Long.valueOf(vr[0]));
        Kiem.bang("bat co: dem xu ra", Long.valueOf(40L), Long.valueOf(vr[1]));
        Kiem.bang("lay xong thi dat lai 0", Long.valueOf(0L), Long.valueOf(LauDai.layVaDatLaiXu()[0]));
        LauDai.dat(false);

        final ChiSoKinhTe c = ChiSoKinhTe.tinh(new int[]{10, 30, 50, 30}, new long[]{0, 1000, 1000000, 3000},
                new long[]{1, 2, 3, 4}, new long[]{5, 5, 5, 5});
        Kiem.bang("trung vi cap", Integer.valueOf(30), Integer.valueOf(c.capTrungVi));
        Kiem.bang("chi so gia = xuMoiGio(30)", Long.valueOf(ThongSoLauDai.xuMoiGio(30)), Long.valueOf(c.chiSoGia));
        Kiem.bang("tong luong", Long.valueOf(10L), Long.valueOf(c.tongLuong));
        Kiem.dung("top 1% (1 nguoi) giu > 99% xu", c.phanTramTop1X100 > 9900);
        Kiem.bang("khong ai hoat dong: 0 nguoi", Integer.valueOf(0), Integer.valueOf(ChiSoKinhTe.tinh(new int[0], new long[0], new long[0], new long[0]).soNguoi));

        final String sql = doc("../sql/them_bang_lau_dai.sql");
        for (final String b : new String[]{"tob_kinh_te_ngay", "tob_char_lau_dai", "tob_nhiem_vu_lau_dai", "tob_mua_tuan",
            "tob_boss_dong_gop", "tob_ham_nguc", "tob_pvp_mua", "tob_su_pham", "tob_char_xp_cu", "tob_pokedex_shiny",
            "tob_giftcode", "tob_giftcode_log", "tob_qua_online", "tob_chiem_thanh_tuan"}) {
            Kiem.dung("sql/them_bang_lau_dai.sql tao " + b, sql.contains("CREATE TABLE IF NOT EXISTS `" + b + "`"));
            Kiem.dung("DatabaseLauDai cung tu tao " + b, doc("src/data/DatabaseLauDai.java").contains("CREATE TABLE IF NOT EXISTS " + b + " "));
        }
        for (final String f : new String[]{"../sql/them_bang_lau_dai.sql", "../sql/pokemon_lau_dai.sql", "../sql/vo_hieu_giftcode_tt.sql"}) {
            final String s = doc(f).toUpperCase();
            Kiem.sai(f + " khong dung CREATE TABLE ... LIKE", s.matches("(?s).*CREATE TABLE[^;]*\\sLIKE\\s.*"));
            Kiem.sai(f + " khong dung TIMESTAMPDIFF", s.contains("TIMESTAMPDIFF"));
        }
        final String xuat = doc("../tools/pokemon/xuat_sql_day_du.sh");
        Kiem.dung("xuat_sql_day_du.sh gom pokemon_lau_dai", xuat.contains("pokemon_lau_dai"));
        Kiem.dung("xuat_sql_day_du.sh gom them_bang_lau_dai", xuat.contains("them_bang_lau_dai"));
        Kiem.dung("xuat_sql_day_du.sh gom vo_hieu_giftcode_tt", xuat.contains("vo_hieu_giftcode_tt"));
        Kiem.dung("BaoTriDaily goi job kinh te dem", doc("src/real/BaoTriDaily.java").contains("LauDaiJob"));

        final HoSoLauDai h = new HoSoLauDai();
        h.ttDiem = 7;
        h.veChuong = 0;
        h.pvpElo = 1234;
        h.datTruot(55, 3);
        h.shinyDem = 1999;
        final HoSoLauDai h2 = HoSoLauDai.giaiMa(h.maHoa());
        Kiem.bang("ho so: tinh thong", Integer.valueOf(7), Integer.valueOf(h2.ttDiem));
        Kiem.bang("ho so: ve 0 van la 0", Integer.valueOf(0), Integer.valueOf(h2.veChuong));
        Kiem.bang("ho so: elo", Integer.valueOf(1234), Integer.valueOf(h2.pvpElo));
        Kiem.bang("ho so: pity mon 55", Integer.valueOf(3), Integer.valueOf(h2.truot(55)));
        Kiem.bang("ho so: shiny dem", Integer.valueOf(1999), Integer.valueOf(h2.shinyDem));
        Kiem.bang("ho so moi: 4 ve ban dau", Integer.valueOf(4), Integer.valueOf(HoSoLauDai.giaiMa("").veChuong));
        Kiem.bang("ho so hong khong nem loi", Integer.valueOf(0), Integer.valueOf(HoSoLauDai.giaiMa("td=x;;=;zz").ttDiem));
        Kiem.dung("ho so vua cot du_lieu varchar(2000) du 40 mon pity", hoSoDai() <= 2000);
    }

    static int hoSoDai() {
        final HoSoLauDai h = new HoSoLauDai();
        for (int i = 0; i < 60; i++) {
            h.datTruot(30000 + i, 14);
        }
        h.ttQuy = Long.MAX_VALUE;
        return h.maHoa().length();
    }

    // ------------------------------------------------------------------ buoc 1

    static void buoc1() {
        Kiem.nhom("GD2 buoc 1 - sua loi doc lap voi thiet ke");
        // 1.1 Map.random khong con reseed theo ms
        int trung = 0;
        for (int i = 0; i < 2000; i++) {
            if (real.Map.random(1000000) == real.Map.random(1000000)) {
                trung++;
            }
        }
        Kiem.dung("Map.random: hai lan goi lien tiep khong con trung so (" + trung + "/2000)", trung < 5);
        boolean nemLoi = false;
        try {
            real.Map.random(0);
        } catch (final IllegalArgumentException e) {
            nemLoi = true;
        }
        Kiem.dung("Map.random(0) van nem loi nhu cu", nemLoi);
        final String map = doc("src/real/Map.java") + doc("src/real/Char.java") + doc("src/real/CharCapNhat.java");
        Kiem.bang("khong con new Random(currentTimeMillis) trong ham goi lap", Integer.valueOf(1),
                Integer.valueOf(dem(map, "new Random(System.currentTimeMillis());")));

        // 1.2 tru tien sau khi kiem
        final String ep = doc("src/real/MapEpNgoc.java");
        final int iHaveGem = ep.indexOf("if (!haveGem(map, player)) {\n                            try {\n                                player.sendMessage(MessageCreator.createServerAlertMessage(\"Chưa có luyện kim dược\"");
        final int iTru = ep.indexOf("player.truXuNeuDu((long) prizeImbue, true, \"map 72\")");
        Kiem.dung("doFinishImbue: kiem luyen kim duoc TRUOC khi tru xu", iHaveGem > 0 && iTru > iHaveGem);
        Kiem.sai("doFinishImbue: khong con subXu thang 5000 xu truoc khi kiem", ep.contains("player.subXu((long) prizeImbue, true, \"map 72\");"));

        // 1.3 pet bac 5
        try {
            final java.lang.reflect.Field f = Class.forName("data.Pet").getDeclaredField("MATERIAL_UPDGRADE");
            f.setAccessible(true);
            final java.lang.reflect.Field g = Class.forName("data.Pet").getDeclaredField("PC_UPDGRADE_OK");
            g.setAccessible(true);
            Kiem.bang("Pet.MATERIAL_UPDGRADE dai bang PC_UPDGRADE_OK", Integer.valueOf(((int[]) g.get(null)).length),
                    Integer.valueOf(((int[]) f.get(null)).length));
            Kiem.bang("Pet bac 5 can 14 nguyen lieu", Integer.valueOf(14), Integer.valueOf(((int[]) f.get(null))[5]));
        } catch (final Throwable e) {
            Kiem.dung("doc Pet.MATERIAL_UPDGRADE: " + e, false);
        }

        // 1.4 ep ngoc "<"
        final Method epm = phuongThuc("real.MapEpNgoc", "epNgocThanhCong", int.class, int.class);
        int ok3 = 0;
        int ok4 = 0;
        for (int r = 0; r < 100; r++) {
            ok3 += goiB(epm, r, 3) ? 1 : 0;
            ok4 += goiB(epm, r, 4) ? 1 : 0;
        }
        Kiem.bang("ep 3 vien: dung 10/100 roll", Integer.valueOf(10), Integer.valueOf(ok3));
        Kiem.bang("ep 4 vien: dung 20/100 roll", Integer.valueOf(20), Integer.valueOf(ok4));
        Kiem.dung("ep 5 vien: luon thanh cong", goiB(epm, 99, 5));
        Kiem.sai("ep 6 vien: tu choi", goiB(epm, 0, 6));

        // 1.5 x2 vang
        final Method xu = phuongThuc("real.Monster", "tinhXuRoi", int.class, int.class, int.class, int.class);
        Kiem.bang("x2 vang loai 2: gap doi", Integer.valueOf(200), goiI(xu, 100, 2, 1, 1));
        Kiem.bang("x2 vang loai 1: +50%", Integer.valueOf(150), goiI(xu, 100, 1, 1, 1));
        Kiem.bang("khong ve: giu nguyen", Integer.valueOf(100), goiI(xu, 100, 0, 1, 1));
        Kiem.bang("doubleALL 2 + ve x2: x4", Integer.valueOf(400), goiI(xu, 100, 2, 2, 1));

        // 1.6 to doi
        int conEp = 0;
        for (final String f : new String[]{"Region", "RegionVantienTran", "MapTown", "MapLienDau", "MapDanhNhau", "MapClan", "CharCopy", "CharTrangBi"}) {
            final String s = doc("src/real/" + f + ".java");
            conEp += dem(s, "nUser = 5;") + dem(s, "nUserx = 5;") + dem(s, "exp50 = 5;") + dem(s, "xpReceive = 5;");
        }
        Kiem.bang("to doi: khong ban sao nao con ep so nguoi = 5", Integer.valueOf(0), Integer.valueOf(conEp));

        // 1.7 lua x2
        Kiem.sai("attackDam khong cong getValueLua(2) lan hai", doc("src/real/CharChiSo.java").contains("att += self.getValueLua(2) * att / 100;"));
        Kiem.dung("Char.getAttack van cong getValueLua(2) mot lan", doc("src/real/Char.java").contains("super.getAttack() + this.getValueLua(2) * this.attack / 100"));

        // 1.8 wModel null
        final String cs = doc("src/real/CharChiSo.java");
        Kiem.dung("boss Son Tinh: kiem wModel null", cs.contains("(self.wModel == null || !self.wModel.isThuyTinh())"));
        Kiem.dung("boss Thuy Tinh: kiem wModel null", cs.contains("self.wModel == null || !self.wModel.isSonTinh()"));

        // 1.9 bang chieu c1
        try {
            final short[][][] d = real.CharManager.SKILL_DAM_PERCENT;
            Kiem.bang("Bulbasaur (c1) chieu 6 = Charmander (c0) chieu 6", java.util.Arrays.toString(d[0][6]), java.util.Arrays.toString(d[1][6]));
            Kiem.bang("Bulbasaur (c1) chieu 8 = c0 chieu 8", java.util.Arrays.toString(d[0][8]), java.util.Arrays.toString(d[1][8]));
            Kiem.bang("c1 cung so dong voi c3/c4", Integer.valueOf(d[3].length), Integer.valueOf(d[1].length));
        } catch (final Throwable e) {
            Kiem.dung("doc CharManager.SKILL_DAM_PERCENT: " + e, false);
        }

        // 1.10 giftcode tt
        final String gc = doc("../sql/vo_hieu_giftcode_tt.sql");
        Kiem.dung("sql vo hieu giftcode tt", gc.contains("UPDATE giftcode SET limit_use = 0 WHERE giftcode = 'tt'"));
        Kiem.dung("limit_use = 0 nghia la het luot (DatabaseGiftCode)", doc("src/data/DatabaseGiftCode.java").contains("if (limit == 0) {"));
    }

    // ------------------------------------------------------------------ buoc 2

    static void buoc2() {
        Kiem.nhom("GD2 buoc 2 - kinh te (co sv.lau_dai)");
        Kiem.dung("Monster: DropLuong tat khi bat co", doc("src/real/Monster.java").contains("boolean DropLuong = !com.htbb.pokemon.LauDai.bat();"));
        Kiem.bang("roi xu 30% (phan trieu)", Integer.valueOf(300000), Integer.valueOf(KinhTeLauDai.tiLeRoiXuPhanTrieu()));
        // ky vong moi quai = XU_MOI_QUAI
        final int lan = KinhTeLauDai.xuMotLanRoi(30, 100, 100);
        Kiem.bang("ky vong xu / quai cap 30 = XU_MOI_QUAI[30]", Integer.valueOf(ThongSoLauDai.XU_MOI_QUAI[30]),
                Integer.valueOf((int) Math.round(lan * 0.30)));
        Kiem.bang("lech cap 20% -> xu 20%", Integer.valueOf(lan / 5), Integer.valueOf(KinhTeLauDai.xuMotLanRoi(30, 20, 100)));
        Kiem.bang("het nang luong -> 0 xu", Integer.valueOf(0), Integer.valueOf(KinhTeLauDai.xuMotLanRoi(30, 100, 0)));
        Kiem.bang("da roi 1,5% khi lech <= 5", Integer.valueOf(15000), Integer.valueOf(KinhTeLauDai.tiLeRoiDaPhanTrieu(5, 100)));
        Kiem.bang("da khong roi khi lech > 5", Integer.valueOf(0), Integer.valueOf(KinhTeLauDai.tiLeRoiDaPhanTrieu(-6, 100)));

        // nang luong
        final HoSoLauDai h = new HoSoLauDai();
        final long t0 = ngay(2026, 10, 17, 8);
        int heSo = 0;
        for (int i = 0; i <= 179; i++) {
            heSo = NangLuong.ghiNhan(h, t0 + i * 60000L, 180, 360);
        }
        Kiem.bang("179 phut dau: 100%", Integer.valueOf(100), Integer.valueOf(heSo));
        for (int i = 180; i <= 200; i++) {
            heSo = NangLuong.ghiNhan(h, t0 + i * 60000L, 180, 360);
        }
        Kiem.bang("phut 200: 20% (bac met 18/10/2026)", Integer.valueOf(20), Integer.valueOf(heSo));
        for (int i = 201; i <= 400; i++) {
            heSo = NangLuong.ghiNhan(h, t0 + i * 60000L, 180, 360);
        }
        Kiem.bang("phut 400: 5%", Integer.valueOf(5), Integer.valueOf(heSo));
        for (int i = 401; i <= 610; i++) {
            heSo = NangLuong.ghiNhan(h, t0 + i * 60000L, 180, 360);
        }
        Kiem.bang("sau 10 gio: 0%", Integer.valueOf(0), Integer.valueOf(heSo));
        final HoSoLauDai nghi = new HoSoLauDai();
        NangLuong.ghiNhan(nghi, ngay(2026, 10, 1, 8), 180, 360);
        NangLuong.ghiNhan(nghi, ngay(2026, 10, 5, 8), 180, 360);
        Kiem.bang("nghi vai ngay: 6 gio 100%", Integer.valueOf(360), Integer.valueOf(nghi.nlPhut100));
        final HoSoLauDai treo = new HoSoLauDai();
        NangLuong.ghiNhan(treo, ngay(2026, 10, 1, 8), 180, 360);
        treo.nlMsDung = 180L * 60000L;
        NangLuong.ghiNhan(treo, ngay(2026, 10, 2, 8), 180, 360);
        Kiem.bang("hom qua dung het: chi 180 phut", Integer.valueOf(180), Integer.valueOf(treo.nlPhut100));
        Kiem.bang("khoang nghi > 60s khong tinh", Long.valueOf(60000L), Long.valueOf(thuKhoang()));

        // nang cap
        Kiem.bang("bac do cap 1 = so cap", Integer.valueOf(0), Integer.valueOf(NangCapLauDai.bacTheoCap(1)));
        Kiem.bang("bac do cap 55 = kim cuong (5)", Integer.valueOf(5), Integer.valueOf(NangCapLauDai.bacTheoCap(55)));
        Kiem.bang("gia +15 bac 6", Long.valueOf(124580L), Long.valueOf(NangCapLauDai.chiPhi(60, 15)));
        Kiem.dung("gia tang theo bac (+10: bac 8 > bac 2)", NangCapLauDai.chiPhi(80, 10) > NangCapLauDai.chiPhi(20, 10));
        Kiem.bang("pity +15 sau 3 lan truot = 22%", Integer.valueOf(22), Integer.valueOf(NangCapLauDai.tiLe(15, 3)));
        Kiem.bang("+1 luon 100%", Integer.valueOf(100), Integer.valueOf(NangCapLauDai.tiLe(1, 0)));
        Kiem.dung("pity khong qua 100%", NangCapLauDai.tiLe(3, 50) == 100);
        Kiem.bang("truot +5 giu cap", Integer.valueOf(4), Integer.valueOf(NangCapLauDai.capSauTruot(4, 5, false)));
        Kiem.bang("truot +7 khong bua: -1", Integer.valueOf(5), Integer.valueOf(NangCapLauDai.capSauTruot(6, 7, false)));
        Kiem.bang("truot +7 co bua: giu", Integer.valueOf(6), Integer.valueOf(NangCapLauDai.capSauTruot(6, 7, true)));
        Kiem.bang("bua +11 = 25 luong khoa", Integer.valueOf(25), Integer.valueOf(NangCapLauDai.bua(11)));
        Kiem.bang("da +11 = 4", Integer.valueOf(4), Integer.valueOf(NangCapLauDai.da(11)));
        Kiem.bang("tong chi so +10 = 30%", Integer.valueOf(30), Integer.valueOf(NangCapLauDai.tongChiSoPhanTram(10)));
        Kiem.bang("tong chi so +15 = 55%", Integer.valueOf(55), Integer.valueOf(NangCapLauDai.tongChiSoPhanTram(15)));
        Kiem.sai("roll 10 voi ti le 10% la truot ('<')", NangCapLauDai.thanhCong(10, 10));
        // ky vong so lan len +15 khop bang thiet ke (4,82)
        Kiem.dung("ky vong so lan +15 voi pity ~ 4,8", Math.abs(kyVong(15) - 4.82) < 0.15);
        final String ep = doc("src/real/MapEpNgoc.java");
        final String auto = doc("src/real/CharCapNhat.java");
        Kiem.dung("nang cap tay goi NangCapLauDaiGame", ep.contains("NangCapLauDaiGame.thu("));
        Kiem.dung("nang cap tu dong goi NangCapLauDaiGame", auto.contains("NangCapLauDaiGame.thu("));
        Kiem.dung("NangCapLauDaiGame kiem-va-tru trong truNeuDu", doc("src/real/NangCapLauDaiGame.java").contains("p.truNeuDu("));

        // thue, phi
        LauDai.apDungCauHinh(new Properties());
        Kiem.bang("thue cho 6%", Long.valueOf(6000L), Long.valueOf(KinhTeLauDai.thueCho(100000L)));
        Kiem.bang("phi dang ban 1%", Long.valueOf(1000L), Long.valueOf(KinhTeLauDai.phiDangBan(100000L)));
        Kiem.bang("phi dang ban toi thieu 1", Long.valueOf(1L), Long.valueOf(KinhTeLauDai.phiDangBan(10L)));
        Kiem.bang("chuyen luong 5% lam tron len", Long.valueOf(6L), Long.valueOf(KinhTeLauDai.phiChuyenLuong(101L)));
        Kiem.bang("dich chuyen = 3% gio thu nhap", Long.valueOf(ThongSoLauDai.xuMoiGio(50) * 3 / 100), Long.valueOf(KinhTeLauDai.phiDichChuyen(50)));
        Kiem.bang("hoi sinh = 5% gio thu nhap", Long.valueOf(ThongSoLauDai.xuMoiGio(50) * 5 / 100), Long.valueOf(KinhTeLauDai.phiHoiSinh(50)));
        Kiem.bang("ban NPC = 5% gia", Long.valueOf(500L), Long.valueOf(KinhTeLauDai.giaBanNpc(10000L)));
        Kiem.bang("phan ra = 2 da + 10%", "[2, 1000]", java.util.Arrays.toString(KinhTeLauDai.phanRa(10000L)));
        Kiem.dung("phan ra (10%) > ban NPC (5%)", KinhTeLauDai.phanRa(10000L)[1] > KinhTeLauDai.giaBanNpc(10000L));
        Kiem.bang("tran chuyen xu = 5 gio", Long.valueOf(ThongSoLauDai.xuMoiGio(40) * 5), Long.valueOf(KinhTeLauDai.tranChuyenXuNgay(40)));
        Kiem.bang("sua do: chan tren 8% gio x so mon", Long.valueOf(ThongSoLauDai.xuMoiGio(30) * 8 / 100 * 2),
                Long.valueOf(KinhTeLauDai.phiSuaDo(10000000L, 30, 2)));
        Kiem.bang("sua do re hon tran: giu gia cu", Long.valueOf(10L), Long.valueOf(KinhTeLauDai.phiSuaDo(10L, 30, 2)));
        Kiem.bang("ngoc he cap 1 = 1 gio", Long.valueOf(ThongSoLauDai.xuMoiGio(40)), Long.valueOf(KinhTeLauDai.giaNgocHe(40, 1)));
        Kiem.dung("ngoc he cap 2 = x1,45", Math.abs(KinhTeLauDai.giaNgocHe(40, 2) - ThongSoLauDai.xuMoiGio(40) * 1.45) < 2);
        Kiem.bang("luong top 1 tuan", Integer.valueOf(50), Integer.valueOf(KinhTeLauDai.luongXepHangTuan(1)));
        Kiem.bang("luong top 500 tuan", Integer.valueOf(3), Integer.valueOf(KinhTeLauDai.luongXepHangTuan(500)));
        Kiem.bang("ngoai top 1000: 0", Integer.valueOf(0), Integer.valueOf(KinhTeLauDai.luongXepHangTuan(1001)));
        final String nv = doc("src/real/MapXuLyLenh.java");
        Kiem.dung("qua cap 35: bat co thi khong cho 10 trieu xu", nv.contains("if (com.htbb.pokemon.LauDai.bat()) {\n            thuongLenCap35LauDai(p);"));
        Kiem.dung("thue cho moi o OfflineMap", doc("src/real/OfflineMap.java").contains("KinhTeLauDai.thueCho("));
        Kiem.dung("phi chuyen luong 5%", doc("src/real/CharChuyenLuong.java").contains("KinhTeLauDai.phiChuyenLuong("));
    }

    static long thuKhoang() {
        final HoSoLauDai h = new HoSoLauDai();
        final long t = ngay(2026, 10, 17, 9);
        NangLuong.ghiNhan(h, t, 180, 360);
        NangLuong.ghiNhan(h, t + 3600000L, 180, 360);
        return h.nlMsDung;
    }

    static double kyVong(final int k) {
        double conLai = 1.0;
        double ky = 0.0;
        for (int truot = 0; truot < 100 && conLai > 1e-9; truot++) {
            final double p = NangCapLauDai.tiLe(k, truot) / 100.0;
            ky += conLai;
            conLai *= 1.0 - p;
        }
        return ky;
    }

    // ------------------------------------------------------------------ buoc 3

    static void buoc3() {
        Kiem.nhom("GD2 buoc 3 - bang EXP, tran cap, tinh thong, ham EXP duy nhat");
        final long[] main = CapDoLauDai.expMain(91);
        final long[] req = CapDoLauDai.expRequire(91);
        Kiem.bang("bang moi 91 cap", Integer.valueOf(91), Integer.valueOf(main.length));
        boolean tang = true;
        for (int i = 1; i < main.length; i++) {
            tang &= main[i] > main[i - 1];
        }
        Kiem.dung("bang moi tang don dieu", tang);
        Kiem.bang("EXP cap 1 -> 2", Long.valueOf(215L), Long.valueOf(main[1]));
        Kiem.bang("expRequire[50] = EXP_LEN_CAP[50]", Long.valueOf(ThongSoLauDai.EXP_LEN_CAP[50]), Long.valueOf(req[50]));
        Kiem.dung("expRequire[91] > 0 (khong chia 0)", req[91] > 0L);
        Kiem.bang("cap tu exp 0 = 1", Integer.valueOf(1), Integer.valueOf(CapDoLauDai.capTuExp(0L, main)));
        Kiem.bang("cap tu exp = main[49] la 50", Integer.valueOf(50), Integer.valueOf(CapDoLauDai.capTuExp(main[49], main)));

        // chuyen doi nhan vat cu: giu cap, giu %
        final long[] mainCu = real.LevelDetail.expMain;
        final long[] reqCu = real.LevelDetail.expRequire;
        for (final int L : new int[]{1, 10, 35, 60, 85, 90}) {
            final long cu = mainCu[L - 1] + reqCu[L] / 2;
            final long moi = CapDoLauDai.chuyenExp(cu, mainCu, reqCu, main, req);
            Kiem.bang("chuyen exp: cap " + L + " giu cap", Integer.valueOf(L), Integer.valueOf(CapDoLauDai.capTuExp(moi, main)));
            final long pc = (moi - main[L - 1]) * 1000L / req[L];
            Kiem.dung("chuyen exp: cap " + L + " giu ~50% trong cap (" + pc + "/1000)", Math.abs(pc - 500) <= 5);
        }
        Kiem.bang("chuyen exp: cap toi da cu (91) -> cap 91", Integer.valueOf(91),
                Integer.valueOf(CapDoLauDai.capTuExp(CapDoLauDai.chuyenExp(mainCu[90] + 5L, mainCu, reqCu, main, req), main)));
        Kiem.bang("chuyen exp 0 = 0", Long.valueOf(0L), Long.valueOf(CapDoLauDai.chuyenExp(0L, mainCu, reqCu, main, req)));

        // tran
        LauDai.datTranTheoMua(true);
        Kiem.bang("tran ngay 0 = 60", Integer.valueOf(60), Integer.valueOf(CapDoLauDai.tranCap(0, 91)));
        Kiem.bang("tran ngay 400 kep 91", Integer.valueOf(91), Integer.valueOf(CapDoLauDai.tranCap(400, 91)));
        LauDai.datTranTheoMua(false);
        Kiem.bang("tran theo mua tat: ngay 0 = cap cao nhat bang", Integer.valueOf(100), Integer.valueOf(CapDoLauDai.tranCap(0, 100)));
        final long tranExp = main[60] - 1L;
        long[] kq = CapDoLauDai.catTheoTran(tranExp - 10L, 100L, 60, main);
        Kiem.bang("cat theo tran: exp dung o tran", Long.valueOf(tranExp), Long.valueOf(kq[0]));
        Kiem.bang("cat theo tran: thua 90", Long.valueOf(90L), Long.valueOf(kq[1]));
        Kiem.bang("cat theo tran: cap van 60", Integer.valueOf(60), Integer.valueOf(CapDoLauDai.capTuExp(kq[0], main)));
        kq = CapDoLauDai.catTheoTran(main[84], 1000L, 60, main);
        Kiem.bang("nhan vat cu tren tran: giu exp", Long.valueOf(main[84]), Long.valueOf(kq[0]));
        Kiem.bang("nhan vat cu tren tran: moi exp la thua", Long.valueOf(1000L), Long.valueOf(kq[1]));
        kq = CapDoLauDai.catTheoTran(100L, 50L, 60, main);
        Kiem.bang("duoi tran: cong du", Long.valueOf(150L), Long.valueOf(kq[0]));

        // tinh thong
        final long buoc = CapDoLauDai.expMoiDiemTinhThong(60);
        Kiem.bang("1 diem tinh thong = 50% EXP len cap 59", Long.valueOf(ThongSoLauDai.EXP_LEN_CAP[59] / 2), Long.valueOf(buoc));
        long[] tt = CapDoLauDai.congTinhThong(0, 0L, buoc * 3 + 5, 60, 0);
        Kiem.bang("3,x buoc -> 3 diem", Long.valueOf(3L), Long.valueOf(tt[0]));
        Kiem.bang("du lai 5", Long.valueOf(5L), Long.valueOf(tt[1]));
        tt = CapDoLauDai.congTinhThong(0, 0L, buoc * 100, 60, 0);
        Kiem.bang("mua S1 toi da 10 diem", Long.valueOf(10L), Long.valueOf(tt[0]));
        Kiem.bang("tran toan bo 50", Integer.valueOf(50), Integer.valueOf(CapDoLauDai.tinhThongToiDa(9)));
        Kiem.bang("50 diem = +10% (100 phan nghin)", Integer.valueOf(100), Integer.valueOf(CapDoLauDai.tinhThongPhanNghin(50)));

        // EXP ha quai
        Kiem.bang("lech 0 = 100%", Integer.valueOf(100), Integer.valueOf(CapDoLauDai.heSoLechCap(0)));
        Kiem.bang("lech 5 = 80%", Integer.valueOf(80), Integer.valueOf(CapDoLauDai.heSoLechCap(5)));
        Kiem.bang("lech 8 = 50%", Integer.valueOf(50), Integer.valueOf(CapDoLauDai.heSoLechCap(8)));
        Kiem.bang("lech 12 = 20%", Integer.valueOf(20), Integer.valueOf(CapDoLauDai.heSoLechCap(12)));
        Kiem.bang("lech 15 = 5%", Integer.valueOf(5), Integer.valueOf(CapDoLauDai.heSoLechCap(15)));
        Kiem.bang("quai cao hon 3 = 115%", Integer.valueOf(115), Integer.valueOf(CapDoLauDai.heSoLechCap(-3)));
        Kiem.bang("quai cao hon 7 = 125%", Integer.valueOf(125), Integer.valueOf(CapDoLauDai.heSoLechCap(-7)));
        Kiem.bang("quai cao hon 9 = 0", Integer.valueOf(0), Integer.valueOf(CapDoLauDai.heSoLechCap(-9)));
        Kiem.bang("to doi 5 nguoi = +40%", Integer.valueOf(140), Integer.valueOf(CapDoLauDai.heSoToDoi(5)));
        Kiem.bang("to doi 9 nguoi kep 5", Integer.valueOf(140), Integer.valueOf(CapDoLauDai.heSoToDoi(9)));
        Kiem.bang("buff tran +100%", Integer.valueOf(200), Integer.valueOf(CapDoLauDai.expHaQuai(100, 100, 350, 100, 100, 100)));
        Kiem.bang("nang luong 50%", Integer.valueOf(50), Integer.valueOf(CapDoLauDai.expHaQuai(100, 100, 0, 50, 100, 100)));
        Kiem.bang("nang luong 0 = 0 exp", Integer.valueOf(0), Integer.valueOf(CapDoLauDai.expHaQuai(100, 100, 0, 0, 100, 100)));
        Kiem.bang("bat kip x1,5 khi cap < trung vi - 5", Integer.valueOf(150), Integer.valueOf(CapDoLauDai.heSoBatKip(40, 50, true)));
        Kiem.bang("bat kip: khong du dieu kien", Integer.valueOf(100), Integer.valueOf(CapDoLauDai.heSoBatKip(40, 50, false)));
        // gio de len cap ~ h(L): 300 quai/gio cung cap
        final double gio = (double) ThongSoLauDai.EXP_LEN_CAP[50] / ThongSoLauDai.expMoiGio(50);
        Kiem.dung("cap 50: ~3,6 gio hieu dung/cap (" + gio + ")", gio > 3.0 && gio < 4.2);

        // 7 ban sao -> 1 ham
        int re = 0;
        int goi = 0;
        for (final String f : new String[]{"Region", "RegionVantienTran", "MapTown", "MapLienDau", "MapDanhNhau", "MapClan", "CharCopy", "CharTrangBi"}) {
            final String s = doc("src/real/" + f + ".java");
            re += dem(s, "ExpLauDai.traoHaQuai(");
            goi += dem(s, "calculatorXpParty(");
        }
        Kiem.bang("moi cho goi calculatorXpParty deu re nhanh ExpLauDai.traoHaQuai", Integer.valueOf(goi), Integer.valueOf(re));
        Kiem.bang("21 ban sao cong EXP ha quai", Integer.valueOf(21), Integer.valueOf(re));
        final String xl = doc("src/real/MapXuLyLenh.java");
        Kiem.bang("ca hai ham len cap cat theo tran (addXPForChar + addXpCharEvent)", Integer.valueOf(2), Integer.valueOf(dem(xl, "ExpLauDai.congTheoTran(")));
        Kiem.dung("TeamServer doi bang EXP luc khoi dong", doc("src/server/TeamServer.java").contains("ExpLauDai.apDungBang()"));
        Kiem.dung("TeamServer chuyen EXP tu dong o ca hai trang thai co", doc("src/server/TeamServer.java").contains("real.ChuyenDoiLauDai.khiKhoiDong();"));
        chuKyBatTat(mainCu, reqCu, main, req);
    }

    /**
     * Yeu cau 18/10/2026: bat -> tat -> bat -> tat tren nhan vat mau (moi cap, nhieu % trong cap,
     * co "choi them" giua cac lan): cap KHONG BAO GIO tut, % trong cap giu trong dung sai.
     */
    static void chuKyBatTat(final long[] mainCu, final long[] reqCu, final long[] main, final long[] req) {
        int tut = 0;
        int lechPc = 0;
        int mau = 0;
        final java.util.Random r = new java.util.Random(20261018L);
        for (int L = 1; L <= 91; L += 3) {
            for (final int pc : new int[]{0, 137, 500, 999}) {
                long xp = L >= mainCu.length ? mainCu[mainCu.length - 1] : mainCu[L - 1] + reqCu[L] * pc / 1000L;
                final int capGoc = CapDoLauDai.capTuExp(xp, mainCu);
                int capTruoc = capGoc;
                boolean moi = false;
                for (int vong = 0; vong < 4; vong++) {
                    final long[] mA = moi ? main : mainCu;
                    final long[] rA = moi ? req : reqCu;
                    final long[] mB = moi ? mainCu : main;
                    final long[] rB = moi ? reqCu : req;
                    final int c = CapDoLauDai.capTuExp(xp, mA);
                    final long pcA = c < mA.length ? (xp - mA[c - 1]) * 1000L / rA[c] : 0L;
                    xp = CapDoLauDai.chuyenExp(xp, mA, rA, mB, rB);
                    moi = !moi;
                    final int c2 = CapDoLauDai.capTuExp(xp, mB);
                    final long pcB = c2 < mB.length ? (xp - mB[c2 - 1]) * 1000L / rB[c2] : 0L;
                    if (c2 < capTruoc) {
                        tut++;
                    }
                    if (c2 == c && c < 91 && Math.abs(pcB - pcA) > 5) {
                        lechPc++;
                    }
                    capTruoc = c2;
                    // choi them mot chut o bang hien tai (khong qua cap sau)
                    final long them = c2 < mB.length ? rB[c2] * r.nextInt(50) / 1000L : 0L;
                    xp += them;
                    capTruoc = CapDoLauDai.capTuExp(xp, mB);
                    mau++;
                }
            }
        }
        Kiem.bang("bat/tat lien tuc: cap khong bao gio tut (" + mau + " lan chuyen)", Integer.valueOf(0), Integer.valueOf(tut));
        Kiem.bang("bat/tat lien tuc: % trong cap lech <= 0,5%", Integer.valueOf(0), Integer.valueOf(lechPc));
        final String cd = doc("src/real/ChuyenDoiLauDai.java");
        Kiem.dung("chuyen doi: nhat ky TRUOC, UPDATE co dieu kien sau", cd.indexOf("ghiNhatKyChuyen(") < cd.indexOf("apChuyen("));
        Kiem.dung("chuyen doi: lam tiep lan bi ngat (lau_dai.dang_chuyen)", cd.contains("K_DANG"));
        Kiem.dung("ngay khai truong tu ghi vao cau_hinh khi thieu", cd.contains("lau_dai.ngay_khai_truong"));
        Kiem.dung("tat co: nhiem vu chinh tra ve cho cu", doc("src/real/CharNapDuLieu.java").contains("NhiemVuLauDaiGame.khiNapTatCo(self)"));
        Kiem.dung("tat co: go lich su kien da hen", cd.contains("goLichSuKien()"));
    }

    // ------------------------------------------------------------------ buoc 4

    static void buoc4() {
        Kiem.nhom("GD2 buoc 4 - nhiem vu: cap, ve tuan, loc danh sach");
        Kiem.dung("hop cap: lv 20, cap 25", NhiemVuLauDai.hopCap(20, 25));
        Kiem.sai("hop cap: lv 20, cap 30", NhiemVuLauDai.hopCap(20, 30));
        Kiem.sai("hop cap: lv 20, cap 19", NhiemVuLauDai.hopCap(20, 19));
        final int[] lv = new int[300];
        for (int i = 0; i < lv.length; i++) {
            lv[i] = 20;
        }
        final List<Integer> chon = NhiemVuLauDai.locPhu(lv, 25, NhiemVuLauDai.PHU_TOI_DA);
        Kiem.bang("300 nhiem vu cung dai -> chi gui 20", Integer.valueOf(20), Integer.valueOf(chon.size()));
        Kiem.dung("20 <= 127 (goi ghi so luong bang 1 byte)", NhiemVuLauDai.PHU_TOI_DA + 2 <= NhiemVuLauDai.GOI_TOI_DA);
        int[] v = NhiemVuLauDai.capNhatVe(1, 100, 101);
        Kiem.bang("sang tuan moi +2 ve", Integer.valueOf(3), Integer.valueOf(v[0]));
        v = NhiemVuLauDai.capNhatVe(3, 100, 110);
        Kiem.bang("tich toi da 4 ve", Integer.valueOf(4), Integer.valueOf(v[0]));
        v = NhiemVuLauDai.capNhatVe(1, 100, 100);
        Kiem.bang("cung tuan khong cong", Integer.valueOf(1), Integer.valueOf(v[0]));
        Kiem.dung("thieu cap -> tu choi", NhiemVuLauDai.kiemChinh(30, 20, 0, 5, false, 4) != null);
        Kiem.dung("chua toi ngay mo -> tu choi", NhiemVuLauDai.kiemChinh(1, 20, 60, 5, false, 4) != null);
        Kiem.dung("het ve -> tu choi", NhiemVuLauDai.kiemChinh(1, 20, 0, 5, true, 0) != null);
        Kiem.bang("du dieu kien", null, NhiemVuLauDai.kiemChinh(1, 20, 0, 5, true, 1));
        final long thuHai5h = ngay(2026, 10, 19, 5) + 60000L; // 19/10/2026 la thu Hai
        Kiem.bang("tuan doi luc thu Hai 05:00", Integer.valueOf(NhiemVuLauDai.tuan(thuHai5h - 120000L) + 1),
                Integer.valueOf(NhiemVuLauDai.tuan(thuHai5h)));
        Kiem.bang("trong tuan khong doi", Integer.valueOf(NhiemVuLauDai.tuan(thuHai5h)),
                Integer.valueOf(NhiemVuLauDai.tuan(thuHai5h + 3L * 86400000L)));
        final int[] lvc = {1, 1, 3, 3, 10, 10};
        final boolean[] dau = {true, false, true, false, true, false};
        Kiem.bang("nhan vat cu cap 5 -> dau chuong lv 3", Integer.valueOf(2), Integer.valueOf(NhiemVuLauDai.chiSoChinhChoCap(lvc, dau, 5)));
        Kiem.bang("nhan vat cu cap 50 -> chuong cuoi <= cap", Integer.valueOf(4), Integer.valueOf(NhiemVuLauDai.chiSoChinhChoCap(lvc, dau, 50)));
        final String c = doc("src/real/Char.java");
        Kiem.dung("getAllQuestCanReceive loc theo co", c.contains("NhiemVuLauDaiGame.locDanhSach("));
        Kiem.dung("OnQuest kiem cap/ve truoc khi nhan nhiem vu chinh", c.contains("NhiemVuLauDaiGame.kiemNhanChinh("));
        Kiem.dung("loader doc data_quest_moi + ORDER BY id khi bat co", doc("src/data/DatabaseXepHang.java").contains("data_quest_moi"));
    }

    // ------------------------------------------------------------------ buoc 5

    static void buoc5() {
        Kiem.nhom("GD2 buoc 5 - su kien: ma moi + lich 52 tuan");
        final String csv = doc("../du_lieu/su_kien_nam.csv");
        final List<String[]> dong = docCsv(csv);
        Kiem.bang("CSV 52 tuan", Integer.valueOf(52), Integer.valueOf(dong.size()));
        Kiem.bang("bang Java 52 tuan", Integer.valueOf(52), Integer.valueOf(SuKienLauDai.LICH.length));
        for (int i = 0; i < Math.min(52, dong.size()); i++) {
            final String[] d = dong.get(i);
            final Object[] j = SuKienLauDai.LICH[i];
            Kiem.bang("tuan " + d[0] + " khop CSV", d[0] + "|" + d[6] + "|" + d[7], j[0] + "|" + j[1] + "|" + j[2]);
        }
        for (final String ma : SuKienLauDai.MA_MOI) {
            Kiem.dung("ma " + ma + " co trong SuKien.DANH_MUC", real.SuKien.timMuc(ma) != null);
        }
        for (final Object[] d : SuKienLauDai.LICH) {
            for (final String ma : ((String) d[2]).split(",")) {
                Kiem.dung("ma lich " + ma + " (tuan " + d[0] + ") co trong danh muc", real.SuKien.timMuc(ma) != null);
            }
        }
        final long now = ngay(2026, 10, 17, 12);
        final Map<String, long[]> lan = SuKienLauDai.lanKeTiep(now);
        Kiem.dung("lan ke tiep cua halloween mo sau bay gio", lan.get("halloween")[0] > now);
        Kiem.dung("lan ke tiep cua tuan he da hen", lan.containsKey("moi:tuanhe"));
        for (final Map.Entry<String, long[]> e : lan.entrySet()) {
            Kiem.dung("lich " + e.getKey() + ": dong sau mo", e.getValue()[1] > e.getValue()[0]);
        }
        Kiem.bang("tuan 43/2026 mo thu Hai 19/10 05:00", Long.valueOf(ngay(2026, 10, 19, 5)), Long.valueOf(SuKienLauDai.moLuc(2026, 43)));
        Kiem.bang("tuan he 18 = Lua", Integer.valueOf(HePokemon.LUA), Integer.valueOf(SuKienLauDai.heTuan(18)));
        Kiem.bang("shiny tuan shiny x10", Integer.valueOf(10), Integer.valueOf(SuKienLauDai.nhanShiny(true, false)));
        Kiem.bang("shiny ngay cong dong x5", Integer.valueOf(5), Integer.valueOf(SuKienLauDai.nhanShiny(false, true)));
        Kiem.dung("giftcode: bat co thi kiem 2/thang o noi xu ly", doc("src/data/DatabaseGiftCode.java").contains("GiftcodeLauDai.choPhep("));
    }

    // ------------------------------------------------------------------ buoc 6

    static void buoc6() {
        Kiem.nhom("GD2 buoc 6 - chien dau, boss, trang thai, goi -95..-99");
        Kiem.bang("co ban = atk - def", Long.valueOf(700L), Long.valueOf(SatThuongLauDai.coBan(1000, 300)));
        Kiem.bang("san 5% atk khi thu cao", Long.valueOf(50L), Long.valueOf(SatThuongLauDai.coBan(1000, 5000)));
        int[] r = SatThuongLauDai.tinh(1000, 0, 100, HePokemon.LUA, HePokemon.LUA, -1, HePokemon.CO, -1, 0, false, 100);
        Kiem.bang("lua -> co (khac x1,5 trong game) va STAB 1,2 (sau thu)", Integer.valueOf(1800), Integer.valueOf(r[0]));
        Kiem.dung("co khac + STAB", (r[1] & SatThuongLauDai.CO_KHAC) != 0 && (r[1] & SatThuongLauDai.CO_STAB) != 0);
        r = SatThuongLauDai.tinh(1000, 500, 100, HePokemon.LUA, HePokemon.NUOC, -1, HePokemon.CO, -1, 0, false, 100);
        Kiem.bang("he ap SAU thu: (1000-500) x 1,5 = 750", Integer.valueOf(750), Integer.valueOf(r[0]));
        r = SatThuongLauDai.tinh(1000, 0, 100, -1, 0, -1, 0, -1, 0, true, 100);
        Kiem.bang("chi mang x1,5", Integer.valueOf(1500), Integer.valueOf(r[0]));
        Kiem.bang("PvP x0,3", Integer.valueOf(300), Integer.valueOf(SatThuongLauDai.pvp(1000, 100000)));
        Kiem.bang("PvP tran 8% HP", Integer.valueOf(800), Integer.valueOf(SatThuongLauDai.pvp(100000, 10000)));
        Kiem.bang("do he tran 20%", Integer.valueOf(20), Integer.valueOf(SatThuongLauDai.doHePhanTram(15, true)));

        // trang thai
        final TrangThaiPokemon t = TrangThaiPokemon.moi();
        Kiem.dung("ap bong", t.ap(TrangThaiPokemon.BONG, 6000, 0L));
        Kiem.bang("bong: chua toi nhip", Integer.valueOf(0), Integer.valueOf(t.nhip(1000L, 1000)));
        Kiem.bang("bong 3% / 2 giay", Integer.valueOf(30), Integer.valueOf(t.nhip(2000L, 1000)));
        Kiem.bang("bong -10% tan cong", Integer.valueOf(90), Integer.valueOf(t.tanCongPhanTram(2500L)));
        Kiem.bang("het han sau 6 giay", Integer.valueOf(100), Integer.valueOf(t.tanCongPhanTram(6001L)));
        final TrangThaiPokemon d = TrangThaiPokemon.moi();
        d.ap(TrangThaiPokemon.DOC, 8000, 0L);
        d.ap(TrangThaiPokemon.DOC, 8000, 100L);
        d.ap(TrangThaiPokemon.DOC, 8000, 200L);
        d.ap(TrangThaiPokemon.DOC, 8000, 300L);
        Kiem.bang("doc chong toi da 3", Integer.valueOf(3), Integer.valueOf(d.chong));
        Kiem.bang("doc 3 chong = 6%", Integer.valueOf(60), Integer.valueOf(d.nhip(2000L, 1000)));
        final TrangThaiPokemon n = TrangThaiPokemon.moi();
        n.ap(TrangThaiPokemon.NGU, 3000, 0L);
        Kiem.dung("ngu: khoa hanh dong", n.khoa(100L));
        n.trungDon(200L);
        Kiem.sai("trung don thi tinh", n.khoa(300L));
        Kiem.sai("mien ngu 10 giay sau", n.ap(TrangThaiPokemon.NGU, 3000, 5000L));
        Kiem.dung("he Lua mien bong", TrangThaiPokemon.mien(TrangThaiPokemon.BONG, HePokemon.LUA, -1));
        Kiem.dung("he Thep mien doc", TrangThaiPokemon.mien(TrangThaiPokemon.DOC, HePokemon.THEP, -1));
        Kiem.bang("PvP x0,5", Integer.valueOf(3000), Integer.valueOf(TrangThaiPokemon.thoiGian(TrangThaiPokemon.BONG, true, false)));
        Kiem.bang("boss 30%", Integer.valueOf(1800), Integer.valueOf(TrangThaiPokemon.thoiGian(TrangThaiPokemon.BONG, false, true)));
        Kiem.bang("boss khong ngu", Integer.valueOf(0), Integer.valueOf(TrangThaiPokemon.thoiGian(TrangThaiPokemon.NGU, false, true)));

        // boss
        final GiaiDoanBoss b = new GiaiDoanBoss(0L, true);
        Kiem.bang("71% = pha 1", Integer.valueOf(1), Integer.valueOf(GiaiDoanBoss.phaTheoHp(71, 100)));
        Kiem.bang("70% = pha 2", Integer.valueOf(2), Integer.valueOf(GiaiDoanBoss.phaTheoHp(70, 100)));
        Kiem.bang("39% = pha 3", Integer.valueOf(3), Integer.valueOf(GiaiDoanBoss.phaTheoHp(39, 100)));
        Kiem.bang("vao pha 2: vao pha + goi de", "[0, 2]", java.util.Arrays.toString(b.capNhat(60, 100, 1000L)));
        Kiem.bang("vao pha 3: + no", "[0, 2, 1]", java.util.Arrays.toString(b.capNhat(30, 100, 2000L)));
        Kiem.bang("toc do pha 3 +30%", Integer.valueOf(130), Integer.valueOf(b.heSoTocDo()));
        Kiem.bang("chua no theo gio", Integer.valueOf(100), Integer.valueOf(b.heSoSatThuong()));
        Kiem.bang("15 phut: no x3", "[1]", java.util.Arrays.toString(b.capNhat(30, 100, 15L * 60000L)));
        Kiem.bang("sau no sat thuong x3", Integer.valueOf(300), Integer.valueOf(b.heSoSatThuong()));
        Kiem.bang("de theo so nguoi: 10 nguoi -> 6", Integer.valueOf(6), Integer.valueOf(GiaiDoanBoss.soDe(10)));
        Kiem.bang("de 1 nguoi -> 2", Integer.valueOf(2), Integer.valueOf(GiaiDoanBoss.soDe(1)));
        Kiem.dung("vung tron trung", GiaiDoanBoss.trongVung(0, 0, 0, 50, 0, 0, 30, 30));
        Kiem.sai("vung tron truot", GiaiDoanBoss.trongVung(0, 0, 0, 50, 0, 0, 40, 40));
        Kiem.dung("vung non 90 do huong 0", GiaiDoanBoss.trongVung(1, 0, 0, 100, 90, 0, 50, 10));
        Kiem.sai("vung non: sau lung", GiaiDoanBoss.trongVung(1, 0, 0, 100, 90, 0, -50, 0));
        Kiem.dung("vung chu nhat", GiaiDoanBoss.trongVung(2, 0, 0, 100, 20, 90, 0, 50));
        Kiem.sai("vanh: trong long an toan", GiaiDoanBoss.trongVung(3, 0, 0, 100, 50, 0, 10, 10));
        final int[] pn = GiaiDoanBoss.phanNghin(new long[]{750, 250, 0});
        Kiem.bang("dong gop phan nghin", "[750, 250, 0]", java.util.Arrays.toString(pn));
        Kiem.bang("top 10 = hop vang", Integer.valueOf(2), Integer.valueOf(GiaiDoanBoss.bacThuong(3, 100)));
        Kiem.bang(">= 0,5% = hop tham gia", Integer.valueOf(1), Integer.valueOf(GiaiDoanBoss.bacThuong(50, 5)));
        Kiem.bang("< 0,5% = khong", Integer.valueOf(0), Integer.valueOf(GiaiDoanBoss.bacThuong(50, 4)));

        // goi
        Kiem.bang("-95 dai 13 byte (2+1+2+1+4+1+1+1)", Integer.valueOf(13), Integer.valueOf(doDai(0)));
        Kiem.bang("-96 dai 18 byte", Integer.valueOf(18), Integer.valueOf(doDai(1)));
        Kiem.bang("-97 dai 7 byte", Integer.valueOf(7), Integer.valueOf(doDai(2)));
        Kiem.bang("-98 dai 10 byte", Integer.valueOf(10), Integer.valueOf(doDai(3)));
        Kiem.bang("-99 (2 nguoi) dai 1+8+2", Integer.valueOf(11), Integer.valueOf(doDai(4)));
        final String src = docThuMuc("src");
        for (final int lenh : new int[]{-95, -96, -97, -98, -99}) {
            Kiem.bang("Message(" + lenh + ") chi tao o GoiLauDaiGame", Integer.valueOf(0),
                    Integer.valueOf(dem(src.replace(doc("src/real/GoiLauDaiGame.java"), ""), "new Message(" + lenh + ")")));
        }
        final String tl = doc("../docs/thiet-ke-game.md");
        Kiem.dung("thiet-ke-game.md ghi ro -95..-99 da chot", tl.contains("-95") && tl.contains("POKE_DONG_GOP"));
    }

    static int doDai(final int loai) {
        try {
            final ByteArrayOutputStream b = new ByteArrayOutputStream();
            final DataOutputStream d = new DataOutputStream(b);
            switch (loai) {
                case 0:
                    GoiLauDai.ghiDon(d, 1, 0, 2, 1, 500, 5, 1, 0);
                    break;
                case 1:
                    GoiLauDai.ghiCanhBao(d, 1, 2, 3, 0, 10, 20, 30, 0, 0, 2000);
                    break;
                case 2:
                    GoiLauDai.ghiTrangThai(d, 1, 0, 1, 1, 6000);
                    break;
                case 3:
                    GoiLauDai.ghiBoss(d, 1, 2, 0, 12345, 600);
                    break;
                default:
                    GoiLauDai.ghiDongGop(d, new int[]{1, 2}, new int[]{600, 400}, 2, 600);
                    break;
            }
            d.flush();
            return b.size();
        } catch (final Exception e) {
            return -1;
        }
    }

    // ------------------------------------------------------------------ buoc 7

    static void buoc7() {
        Kiem.nhom("GD2 buoc 7 - cuon hut: PvP mua, shiny, Ham nguc, su pham, dang nhap");
        Kiem.bang("Elo ngang nhau: thang +16", Integer.valueOf(1016), Integer.valueOf(ThangPvp.eloMoi(1000, 1000, 1)));
        Kiem.bang("Elo ngang nhau: thua -16", Integer.valueOf(984), Integer.valueOf(ThangPvp.eloMoi(1000, 1000, 0)));
        Kiem.dung("thang nguoi manh hon duoc nhieu hon", ThangPvp.eloMoi(1000, 1400, 1) - 1000 > 16);
        Kiem.bang("bac Master tu 1600", Integer.valueOf(3), Integer.valueOf(ThangPvp.bac(1650)));
        Kiem.bang("reset mem giu 50%", Integer.valueOf(1300), Integer.valueOf(ThangPvp.resetMem(1600)));
        Kiem.dung("ghep cung dai 10 cap", ThangPvp.cungDai(41, 50));
        Kiem.sai("khac dai", ThangPvp.cungDai(40, 41));
        Kiem.bang("mua PvP 13 tuan", Integer.valueOf(1), Integer.valueOf(ThangPvp.mua(91)));
        Kiem.dung("shiny pity lan 2000", CuonHut.shiny(1999, 4000, 1, 4096, 2000));
        Kiem.sai("shiny binh thuong roll 1", CuonHut.shiny(10, 1, 1, 4096, 2000));
        Kiem.dung("shiny roll 0", CuonHut.shiny(10, 0, 1, 4096, 2000));
        Kiem.dung("tuan shiny x10: roll 9 trung", CuonHut.shiny(10, 9, 10, 4096, 2000));
        final int[] quai = {5, 7, 9, 11};
        Kiem.bang("ham nguc tat dinh theo ngay", java.util.Arrays.toString(CuonHut.tangHamNguc(20261017, 3, quai)),
                java.util.Arrays.toString(CuonHut.tangHamNguc(20261017, 3, quai)));
        Kiem.dung("ham nguc khac ngay khac ban do", !java.util.Arrays.equals(tang(20261017), tang(20261018)));
        Kiem.bang("tang 5 co thuong", Integer.valueOf(1), Integer.valueOf(CuonHut.tangHamNguc(20261017, 5, quai)[2]));
        Kiem.dung("thay cap 30 kem tro cap 10", CuonHut.ghepSuPham(30, 10));
        Kiem.sai("thay cap 29 khong duoc", CuonHut.ghepSuPham(29, 5));
        Kiem.bang("moc su pham 20", Integer.valueOf(20), Integer.valueOf(CuonHut.mocSuPham(19, 21)));
        int[] dn = CuonHut.dangNhap(0, 20261016, 20261017);
        Kiem.bang("dang nhap ngay 1: 3 luong khoa", Integer.valueOf(3), Integer.valueOf(dn[1]));
        dn = CuonHut.dangNhap(dn[0], 20261017, 20261017);
        Kiem.bang("cung ngay khong nhan lai", Integer.valueOf(0), Integer.valueOf(dn[1]));
        dn = CuonHut.dangNhap(6, 20261001, 20261017);
        Kiem.bang("ngay 7: 15, nghi khong mat chuoi", Integer.valueOf(15), Integer.valueOf(dn[1]));
        Kiem.bang("sau ngay 7 lap lai", Integer.valueOf(0), Integer.valueOf(dn[0]));
        Kiem.dung("NPC -90 co muc So tay huan luyen", doc("src/real/cmd/RequestNPCInfoHandler.java").contains("SoTayLauDai.TEN_MUC"));
    }

    // ------------------------------------------------------------------ yeu cau 17/10/2026

    static void yeuCauThuong() {
        Kiem.nhom("GD2 bo sung - giftcode an toan, qua hoat dong, chiem thanh, kiem toan");
        // giftcode
        final String gc = doc("src/data/DatabaseGiftCode.java");
        Kiem.sai("khong con khoi ma cua sau if (false)", gc.contains("if (false) {"));
        Kiem.sai("khong con ma xuluong", gc.contains("\"xuluong\""));
        Kiem.sai("khong con ma petmax", gc.contains("\"petmax\""));
        Kiem.sai("khong con ma test 100 trieu xu", gc.contains("addXu(100000000L, \"testcode\")"));
        Kiem.sai("tra giftcode khong con noi chuoi SQL", gc.contains("giftcode='\" + text"));
        Kiem.dung("bat co -> he giftcode an toan", gc.contains("real.GiftcodeAnToan.nhap(player, text)"));
        Kiem.bang("ma chu hoa -> chu thuong", "tet2027", com.htbb.pokemon.GiftcodeAnToanLogic.chuanHoaMa(" TET2027 "));
        Kiem.bang("ma co ky tu la -> khong hop le", null, com.htbb.pokemon.GiftcodeAnToanLogic.chuanHoaMa("a'b;--"));
        Kiem.bang("GEM luon ep khoa", "GEM:8:2:-1,LUONGKHOA:5", com.htbb.pokemon.GiftcodeAnToanLogic.chuanHoaThuong("gem:8:2:0,luongkhoa:5"));
        Kiem.bang("cam xu", null, com.htbb.pokemon.GiftcodeAnToanLogic.chuanHoaThuong("XU:100000"));
        Kiem.bang("cam luong mo", null, com.htbb.pokemon.GiftcodeAnToanLogic.chuanHoaThuong("LUONG:10"));
        Kiem.bang("cam DB/PET", null, com.htbb.pokemon.GiftcodeAnToanLogic.chuanHoaThuong("PET:1:1:1"));
        Kiem.bang("gia tri ITEM x5", Integer.valueOf(15), Integer.valueOf(com.htbb.pokemon.GiftcodeAnToanLogic.giaTri("ITEM:5:2,LUONGKHOA:5")));
        Kiem.dung("vuot tran gia tri bi tu choi", com.htbb.pokemon.GiftcodeAnToanLogic.kiem("LUONGKHOA:31", 30) != null);
        Kiem.bang("trong tran: hop le", null, com.htbb.pokemon.GiftcodeAnToanLogic.kiem("LUONGKHOA:10,GEM:8:5", 30));
        final String at = doc("src/real/GiftcodeAnToan.java");
        Kiem.dung("danh dau (log) TRUOC khi tru luot va phat", at.indexOf("ghiLogGiftcode(") < at.indexOf("truLuotGiftcode(")
                && at.indexOf("truLuotGiftcode(") < at.indexOf("applyItemString("));
        Kiem.dung("chong do ma: khoa sau nhieu lan sai", at.contains("ghiSai(p.userID)"));
        Kiem.dung("ngan sach thang toan may chu", at.contains("tongGiftcodeThang("));
        // qua hoat dong
        Kiem.bang("dung yen > 60s khong tinh", Integer.valueOf(0), Integer.valueOf(com.htbb.pokemon.QuaHoatDongLogic.congGiay(1000L, 70000L)));
        Kiem.bang("hanh dong cach 30s: +30s", Integer.valueOf(30), Integer.valueOf(com.htbb.pokemon.QuaHoatDongLogic.congGiay(1000L, 31000L)));
        Kiem.bang("15 phut -> moc 0", Integer.valueOf(0), Integer.valueOf(com.htbb.pokemon.QuaHoatDongLogic.mocNhanDuoc(15 * 60, 0)));
        Kiem.bang("da nhan moc 0, 40 phut -> moc 1", Integer.valueOf(1), Integer.valueOf(com.htbb.pokemon.QuaHoatDongLogic.mocNhanDuoc(40 * 60, 1)));
        Kiem.bang("chua du 15 phut", Integer.valueOf(-1), Integer.valueOf(com.htbb.pokemon.QuaHoatDongLogic.mocNhanDuoc(14 * 60, 0)));
        Kiem.bang("chuoi: nghi 1 ngay van giu (an han)", "[4, 6, 0]", java.util.Arrays.toString(com.htbb.pokemon.QuaHoatDongLogic.chuoi(3, 2)));
        Kiem.bang("chuoi: nghi 2 ngay -> ve ngay 1", "[1, 3, 1]", java.util.Arrays.toString(com.htbb.pokemon.QuaHoatDongLogic.chuoi(3, 3)));
        Kiem.bang("chuoi: cung ngay khong nhan", "[3, 0, 0]", java.util.Arrays.toString(com.htbb.pokemon.QuaHoatDongLogic.chuoi(3, 0)));
        int lk = 0;
        for (final int x : com.htbb.pokemon.QuaHoatDongLogic.MOC_LUONG_KHOA) {
            lk += x;
        }
        Kiem.dung("qua hoat dong <= 5 luong khoa / ngay / tai khoan", lk <= 5);
        Kiem.dung("thuong moc deu khoa", com.htbb.pokemon.QuaHoatDongLogic.thuongMoc(3, 8, 100).endsWith(":-1"));
        Kiem.dung("qua hoat dong theo TAI KHOAN (userID)", doc("src/real/QuaHoatDong.java").contains("p.userID"));
        Kiem.dung("RealController case 100 khong con 'dang cap nhat' khi bat co", doc("src/real/RealController.java").contains("QuaHoatDong.nhan(player, idOptionMenu)"));
        // chiem thanh
        Kiem.sai("AFK (0 dong gop) khong nhan", com.htbb.pokemon.ChiemGymLogic.duDongGop(0, 100000));
        Kiem.sai("duoi 1% nguoi cao nhat khong nhan", com.htbb.pokemon.ChiemGymLogic.duDongGop(5000, 1000000));
        Kiem.dung("du dong gop", com.htbb.pokemon.ChiemGymLogic.duDongGop(50000, 1000000));
        Kiem.bang("bang thang top 3: 25 (tran)", Integer.valueOf(25), Integer.valueOf(com.htbb.pokemon.ChiemGymLogic.luongKhoa(true, true, 3)));
        Kiem.bang("bang thua hang 50: 5", Integer.valueOf(5), Integer.valueOf(com.htbb.pokemon.ChiemGymLogic.luongKhoa(false, true, 50)));
        Kiem.bang("khong du dong gop: 0", Integer.valueOf(0), Integer.valueOf(com.htbb.pokemon.ChiemGymLogic.luongKhoa(true, false, 1)));
        Kiem.bang("chia 20% quy / nguoi", Long.valueOf(2000L), Long.valueOf(com.htbb.pokemon.ChiemGymLogic.phanQuy(100000L, 10, 999999L)));
        Kiem.bang("chia quy tran 1 gio thu nhap", Long.valueOf(500L), Long.valueOf(com.htbb.pokemon.ChiemGymLogic.phanQuy(100000L, 10, 500L)));
        final String ct = doc("src/real/ChiemGymLauDai.java");
        Kiem.dung("quy bang bi tru (chia lai, khong in tien)", ct.contains("clan.addMoney2Clan(-xu)"));
        Kiem.sai("khong con luong mo trong thuong chiem thanh moi", ct.contains("addLuong("));
        Kiem.dung("NpcReceiveCard goi thuong moi", doc("src/real/NpcReceiveCard.java").contains("ChiemGymLauDai.batDau("));
        // kiem toan bo sung
        Kiem.sai("A1: khong con cong thang lockLuong += 1000", doc("src/real/CharCapNhat.java").contains("self.lockLuong += 1000;"));
        Kiem.sai("A6: doi keo khong cong thang lockLuong", doc("src/real/cmd/RequestNPCInfoHandler.java").contains("p.lockLuong += luongLock;"));
        Kiem.dung("D2: thue doi chung khoang 1..20", doc("src/real/Map.java").contains("THUE_DOI_TOI_DA = 20")
                && doc("src/real/MapNhapText.java").contains("Map.THUE_DOI_TOI_DA"));
    }

    static int[] tang(final int ngay) {
        final int[] kq = new int[10];
        for (int i = 0; i < 10; i++) {
            kq[i] = CuonHut.tangHamNguc(ngay, i + 1, new int[]{1, 2, 3, 4, 5, 6, 7, 8})[0];
        }
        return kq;
    }

    // ------------------------------------------------------------------ tien ich

    static long ngay(final int y, final int m, final int d, final int h) {
        final java.util.Calendar c = java.util.Calendar.getInstance();
        c.clear();
        c.set(y, m - 1, d, h, 0, 0);
        return c.getTimeInMillis();
    }

    static Method phuongThuc(final String lop, final String ten, final Class<?>... ts) {
        try {
            final Method m = Class.forName(lop).getDeclaredMethod(ten, ts);
            m.setAccessible(true);
            return m;
        } catch (final Throwable e) {
            return null;
        }
    }

    static boolean goiB(final Method m, final Object... a) {
        try {
            return ((Boolean) m.invoke(null, a)).booleanValue();
        } catch (final Throwable e) {
            return false;
        }
    }

    static Integer goiI(final Method m, final Object... a) {
        try {
            return (Integer) m.invoke(null, a);
        } catch (final Throwable e) {
            return Integer.valueOf(Integer.MIN_VALUE);
        }
    }

    static int dem(final String s, final String x) {
        int n = 0;
        int i = 0;
        while ((i = s.indexOf(x, i)) >= 0) {
            n++;
            i += x.length();
        }
        return n;
    }

    // ------------------------------------------------------------------ GD2b

    static void gd2b() {
        Kiem.nhom("GD2b - engine nhiem vu, vat pham dung, noi dung, chuyen doi tu dong");
        final com.htbb.pokemon.NhiemVuMau m = new com.htbb.pokemon.NhiemVuMau();
        m.ma = "P-01.2";
        m.cap = 10;
        m.truoc = "P-01.1";
        Kiem.dung("chuoi chinh khong do engine moi quan ly", !com.htbb.pokemon.NhiemVuMau.engineQuanLy("chinh")
                && !com.htbb.pokemon.NhiemVuMau.engineQuanLy("huong_dan") && com.htbb.pokemon.NhiemVuMau.engineQuanLy("phu"));
        final java.util.Set<String> xong = new java.util.HashSet<String>();
        Kiem.dung("thieu cap -> tu choi", m.kiemNhan(5, 0, xong, false) != null);
        Kiem.dung("chua xong nhiem vu truoc -> tu choi", m.kiemNhan(20, 0, xong, false) != null);
        xong.add("P-01.1");
        Kiem.bang("du dieu kien -> nhan duoc", null, m.kiemNhan(20, 0, xong, false));
        m.ngayMo = 30;
        Kiem.dung("ngay_mo chua toi -> tu choi", m.kiemNhan(20, 10, xong, false) != null);
        m.ngayMo = 0;
        m.an = true;
        Kiem.dung("nhiem vu an chua mo -> tu choi", m.kiemNhan(20, 0, xong, false) != null);
        Kiem.bang("nhiem vu an da mo -> nhan duoc", null, m.kiemNhan(20, 0, xong, true));
        Kiem.dung("an 'ban đêm' chi mo ban dem", com.htbb.pokemon.NhiemVuMau.moAn("Ban đêm", 22, null)
                && !com.htbb.pokemon.NhiemVuMau.moAn("Ban đêm", 12, null));
        Kiem.dung("an dieu kien khong hieu -> giu an", !com.htbb.pokemon.NhiemVuMau.moAn("khi trang tron", 22, null));
        Kiem.dung("phu chiem khe, ngay/tuan/thanh tuu khong", m.chiemKhe());
        final com.htbb.pokemon.NhiemVuMau tv = new com.htbb.pokemon.NhiemVuMau();
        tv.ma = "TV-07.2";
        Kiem.dung("TV- la tuan, khoa ca chuoi", tv.laTuan() && "TV-07".equals(tv.khoaTuan()) && !tv.chiemKhe());
        final com.htbb.pokemon.NhiemVuMau td = new com.htbb.pokemon.NhiemVuMau();
        td.ma = "TD-03";
        Kiem.dung("TD- dung chung gioi han tuan", td.laTuan() && "TD-03".equals(td.khoaTuan()));
        final com.htbb.pokemon.NhiemVuMau sk = new com.htbb.pokemon.NhiemVuMau();
        sk.ma = "SK-12.3";
        Kiem.bang("tuan su kien SK-12.3", Integer.valueOf(12), Integer.valueOf(sk.tuanSuKien()));
        Kiem.bang("khe phu 3, rut 5, thuong 3, tuan 2", "3,5,3,2", com.htbb.pokemon.NhiemVuMau.KHE_PHU + "," + com.htbb.pokemon.NhiemVuMau.NGAY_RUT
                + "," + com.htbb.pokemon.NhiemVuMau.NGAY_THUONG + "," + com.htbb.pokemon.NhiemVuMau.TUAN_TOI_DA);
        final List<String> ds = new java.util.ArrayList<String>();
        for (int i = 0; i < 12; i++) {
            ds.add("N-" + i);
        }
        final List<String> r1 = com.htbb.pokemon.NhiemVuMau.rutNgay(ds, 12345L, 5);
        Kiem.bang("rut ngay 5 ma", Integer.valueOf(5), Integer.valueOf(r1.size()));
        Kiem.bang("rut ngay tat dinh theo hat giong", r1, com.htbb.pokemon.NhiemVuMau.rutNgay(ds, 12345L, 5));
        Kiem.bang("rut ngay khong trung", Integer.valueOf(5), Integer.valueOf(new java.util.HashSet<String>(r1).size()));
        final java.util.Collection<String> dsCu = new java.util.ArrayList<String>(ds);
        com.htbb.pokemon.NhiemVuMau.rutNgay(ds, 1L, 5);
        Kiem.bang("rut ngay khong sua danh sach goc", dsCu, ds);
        Kiem.bang("bac ngay theo cap", "1,2,3,4,5", com.htbb.pokemon.NhiemVuMau.bacNgay(1) + "," + com.htbb.pokemon.NhiemVuMau.bacNgay(20) + ","
                + com.htbb.pokemon.NhiemVuMau.bacNgay(45) + "," + com.htbb.pokemon.NhiemVuMau.bacNgay(79) + "," + com.htbb.pokemon.NhiemVuMau.bacNgay(95));
        m.kieu = "giet";
        m.mucTieuId = "12|15";
        Kiem.dung("khop su kien dung id", m.khop("giet", "15") && !m.khop("giet", "16") && !m.khop("thu_thap", "15"));
        m.mucTieuId = "";
        Kiem.dung("muc tieu rong = bat ky", m.khop("giet", "99"));
        Kiem.bang("hoi thoai {da}/{can}", "Đã 3/10\nBạn: ok", com.htbb.pokemon.NhiemVuMau.hoiThoai("Đã {da}/{can}|Bạn: ok", 3, 10));
        m.dapAn = "Pikachu|Pi ka chu";
        Kiem.dung("dap an cau do bo dau + hoa thuong", m.dungDapAn("  PIKACHU ") && m.dungDapAn("pi-ka-chu") && !m.dungDapAn("raichu"));
        Kiem.bang("chuan hoa bo dau tieng Viet", "duong di", com.htbb.pokemon.NhiemVuMau.chuanHoa("Đường đi!"));
        final List<String[]> lc = com.htbb.pokemon.NhiemVuMau.luaChon("A:Giúp đỡ=>danh_hieu:Người tốt|B:Bỏ qua");
        Kiem.dung("lua chon 2 nhanh, nhanh A co danh hieu", lc.size() == 2 && "Giúp đỡ".equals(lc.get(0)[0])
                && "danh_hieu:Người tốt".equals(lc.get(0)[1]) && "".equals(lc.get(1)[1]));
        Kiem.bang("doc dieu kien k=v;k=v", "5", com.htbb.pokemon.NhiemVuMau.docDieuKien("ban_kinh_o=5;x=1").get("ban_kinh_o"));
        m.mapId = 7;
        m.toaX = 10;
        m.toaY = 10;
        Kiem.dung("trong toa do (ban kinh 3)", m.trongToaDo(7, 12, 9) && !m.trongToaDo(7, 15, 10) && !m.trongToaDo(8, 10, 10));
        // engine + noi
        final String ev = doc("src/real/NhiemVuEngine.java");
        Kiem.dung("engine: tu choi khi day khe phu", ev.contains("KHE_PHU"));
        Kiem.dung("engine: gioi han tuan chung", ev.contains("TUAN_TOI_DA"));
        Kiem.dung("engine: gioi han thuong ngay", ev.contains("NGAY_THUONG"));
        Kiem.dung("chat: kiem dap an TRUOC khi phat thanh", doc("src/real/MapChat.java").contains("NhiemVuEngine.khiChat"));
        Kiem.dung("ha quai: engine nhan su kien", doc("src/real/RoiLauDai.java").contains("NhiemVuEngine.khiHaQuai"));
        Kiem.dung("nap noi dung goi engine + ItemQuest", doc("src/real/NoiDungLauDai.java").contains("NhiemVuEngine.nap()")
                && doc("src/real/NoiDungLauDai.java").contains("moRongVatPhamNhiemVu()"));
        final String dl = doc("src/data/DatabaseLauDai.java");
        Kiem.dung("nap mau khong JOIN", dl.contains("napMauNhiemVu") && !dl.toUpperCase().contains(" JOIN "));
        // vat pham dung + noi dung
        final String vp = doc("../sql/pokemon_vat_pham_lau_dai.sql");
        Kiem.dung("type 28 + bang hieu ung", vp.contains("data_vat_pham_lau_dai") && vp.contains("Bùa bảo hộ"));
        Kiem.dung("map dung vat pham goi VatPhamDungLauDai", docThuMuc("src/real").contains("VatPhamDungLauDai.dung("));
        Kiem.dung("keo EXP 10-12 bi chan khi bat co", docThuMuc("src/real").contains("keo EXP"));
        final String nc = doc("src/real/NangCapLauDaiGame.java");
        Kiem.dung("nang cap +11..+15 can nguyen lieu phu", nc.contains("NL_PHU"));
        Kiem.dung("gym 1 dau 1 + danh dau truoc phat", doc("src/real/GymLauDai.java").indexOf("h.ban = true") < doc("src/real/GymLauDai.java").indexOf("vuKhiHoanMy(p,"));
        Kiem.dung("chieu moi nap vao CharManager", doc("src/real/ChieuLauDai.java").contains("SKILL_DAM_PERCENT"));
        // chuyen doi tu dong
        final String cd = doc("src/real/ChuyenDoiLauDai.java");
        Kiem.dung("chuyen doi chay ca 2 trang thai co", doc("src/server/TeamServer.java").contains("ChuyenDoiLauDai.khiKhoiDong()")
                || docThuMuc("src").contains("ChuyenDoiLauDai.khiKhoiDong()"));
        Kiem.dung("nhat ky ghi TRUOC cap nhat co dieu kien", cd.indexOf("ghiNhatKyChuyen") >= 0
                && cd.indexOf("ghiNhatKyChuyen") < cd.indexOf("apChuyen(") && dl.contains("AND xp = ?"));
        Kiem.dung("ngay khai truong tu ghi neu thieu", cd.contains("lau_dai.ngay_khai_truong"));
        Kiem.dung("nearMons: khong get ngoai bien", doc("src/real/CharCapNhat.java").contains("j < self.nearMons.size()) {"));
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    static String docThuMuc(final String goc) {
        final StringBuilder b = new StringBuilder();
        try {
            Files.walk(Paths.get(goc)).filter(p -> p.toString().endsWith(".java")).forEach(p -> b.append(doc(p.toString())));
        } catch (final Exception e) {
            // bo qua
        }
        return b.toString();
    }

    /** CSV toi gian co ngoac kep. */
    static List<String[]> docCsv(final String s) {
        final List<String[]> kq = new java.util.ArrayList<String[]>();
        final String[] dong = s.split("\n");
        for (int i = 1; i < dong.length; i++) {
            if (dong[i].trim().isEmpty()) {
                continue;
            }
            final List<String> o = new java.util.ArrayList<String>();
            final StringBuilder b = new StringBuilder();
            boolean trong = false;
            for (final char c : dong[i].toCharArray()) {
                if (c == '"') {
                    trong = !trong;
                } else if (c == ',' && !trong) {
                    o.add(b.toString());
                    b.setLength(0);
                } else {
                    b.append(c);
                }
            }
            o.add(b.toString().trim());
            kq.add(o.toArray(new String[0]));
        }
        return kq;
    }
}
