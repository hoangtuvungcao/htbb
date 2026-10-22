import com.htbb.pokemon.PhaKhoaLogic;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import real.Char;
import real.Item;
import real.PhaKhoaGame;

/**
 * [khoa-khi-mac] Do da mac bi khoa (chu game 0.9.6.1) + pha khoa o Machamp tho ren (ho hut xu).
 *
 * Phan 1: co khoa tren Item (khoa do mac / khoa vi ly do khac, giao dich, ban NPC). Phan 2: chay THAT duong mac (goi 29,
 * DungVatPham -> MapMuaBan.exchangeItem) tren nhan vat ngoai server (dung lai dung cu cua KiemDungVatPham). Phan 3: gia
 * pha khoa (PhaKhoaLogic) + pha khoa that (PhaKhoaGame.phaKhoa: tru xu, tru da, tu choi mon khong hop le, tran 3 lan,
 * mac lai khoa lai). Phan 4: chuoi luu (getDBInfo) doc lai + chuoi cu. Phan 5: quet ma nguon - moi duong dua do cho
 * nguoi khac chan do khoa, ban NPC cho do khoa do mac, luong khoa khong chuyen, migration luc nap, SQL.
 * Khong can DB / mang.
 */
public final class KiemKhoaKhiMac {

    private KiemKhoaKhiMac() {
    }

    static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    static String doan(final String nguon, final String dau, final int dai) {
        final int i = nguon.indexOf(dau);
        return i < 0 ? "" : nguon.substring(i, Math.min(nguon.length(), i + dai));
    }

    static int dem(final String s, final String mau) {
        int n = 0;
        for (int i = s.indexOf(mau); i >= 0; i = s.indexOf(mau, i + 1)) {
            n++;
        }
        return n;
    }

    public static void chay() {
        Kiem.nhom("Khoa khi mac + pha khoa trang bi (Machamp tho ren, ho hut xu)");
        coKhoa();
        giaPhaKhoa();
        try {
            KiemDungVatPham.chuanBi();
            KiemDungVatPham.mau(PhaKhoaLogic.ID_DA_PHA_KHOA, 28, "Đá phá khoá", 1);
            macVaPhaKhoa();
            chuoiLuu();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            PhaKhoaLogic.macDinh();
            com.htbb.pokemon.LauDai.dat(false);
            real.Map.gemTemplate = KiemDungVatPham.gemCu;
            KiemDungVatPham.traLai();
        }
        quetDuong();
        quetSql();
    }

    // ------------------------------------------------------------------ 1. co khoa

    static void coKhoa() {
        final Item a = new Item();
        Kiem.dung("mon moi: chua khoa, giao dich duoc", a.lock == 0 && a.giaoDichDuoc());
        Kiem.dung("mac lan dau: khoa", a.khoaKhiMac());
        Kiem.dung("mac lan dau: lock 1 + co khoa do mac", a.lock == 1 && a.daMac == 1 && a.laKhoaDoMac());
        Kiem.sai("mac lan 2: khong doi gi (idempotent)", a.khoaKhiMac());
        Kiem.sai("do da mac: khong giao dich duoc", a.giaoDichDuoc());
        Kiem.dung("do da mac: ban NPC duoc ca khi cam ban do khoa", a.banNpcDuoc(false));
        Kiem.dung("mo ta: Đã khoá (đã mặc)", a.moTaKhoa().startsWith("Đã khoá (đã mặc)") && a.moTaKhoa().contains("0/3"));

        final Item b = new Item();
        b.lock = 1; // mua bang luong khoa / qua nhiem vu
        Kiem.sai("do khoa vi ly do khac roi mac: khong thanh khoa do mac", b.khoaKhiMac());
        Kiem.dung("do khoa vi ly do khac: daMac 0", b.daMac == 0 && !b.laKhoaDoMac());
        Kiem.sai("do khoa vi ly do khac: khong ban NPC khi cam ban do khoa", b.banNpcDuoc(false));
        Kiem.dung("do khoa vi ly do khac: ban NPC khi cau hinh cho phep", b.banNpcDuoc(true));
        Kiem.bang("mo ta do khoa khac", "Đã khoá", b.moTaKhoa());

        a.khoaVinhVien(); // vd nang cap bang nguyen lieu khoa sau khi da mac
        Kiem.dung("khoa vinh vien tren do da mac: xoa co khoa do mac", a.lock == 1 && a.daMac == 0);
        Kiem.bang("mo ta mon chua mac", "Khoá khi mặc", new Item().moTaKhoa());
    }

    // ------------------------------------------------------------------ 2. gia pha khoa

    static void giaPhaKhoa() {
        PhaKhoaLogic.macDinh();
        Kiem.bang("toi da 3 lan", 3, PhaKhoaLogic.lanToiDa());
        // cap 50 = 33.925 xu / gio; Quy (3) = 200%, +10 = x2, lan 1 x1
        Kiem.bang("cap 50 Quy +10 lan 1", 135700L, PhaKhoaLogic.giaXu(50, 3, 10, 0));
        Kiem.bang("cap 50 Quy +10 lan 2 (x2)", 271400L, PhaKhoaLogic.giaXu(50, 3, 10, 1));
        Kiem.bang("cap 50 Quy +10 lan 3 (x4)", 542800L, PhaKhoaLogic.giaXu(50, 3, 10, 2));
        Kiem.bang("lan 4: het", -1L, PhaKhoaLogic.giaXu(50, 3, 10, 3));
        Kiem.bang("cap 90 Su thi +15 lan 1", 1470757L, PhaKhoaLogic.giaXu(90, 4, 15, 0));
        Kiem.bang("cap 1 Thuong +0 lan 1 (nua gio)", 1795L, PhaKhoaLogic.giaXu(1, 0, 0, 0));
        Kiem.dung("bac cao dat hon bac thap", PhaKhoaLogic.giaXu(60, 6, 5, 0) > PhaKhoaLogic.giaXu(60, 4, 5, 0)
                && PhaKhoaLogic.giaXu(60, 4, 5, 0) > PhaKhoaLogic.giaXu(60, 3, 5, 0)
                && PhaKhoaLogic.giaXu(60, 3, 5, 0) > PhaKhoaLogic.giaXu(60, 7, 5, 0)
                && PhaKhoaLogic.giaXu(60, 7, 5, 0) > PhaKhoaLogic.giaXu(60, 0, 5, 0));
        Kiem.dung("+N dat hon +0", PhaKhoaLogic.giaXu(60, 3, 12, 0) > PhaKhoaLogic.giaXu(60, 3, 0, 0));
        Kiem.dung("cap cao dat hon cap thap", PhaKhoaLogic.giaXu(80, 3, 0, 0) > PhaKhoaLogic.giaXu(40, 3, 0, 0));
        Kiem.bang("cap ngoai bang: kep ve 100", PhaKhoaLogic.giaXu(100, 3, 0, 0), PhaKhoaLogic.giaXu(500, 3, 0, 0));
        Kiem.bang("mau la: dung dong dau (Thuong)", PhaKhoaLogic.giaXu(30, 0, 0, 0), PhaKhoaLogic.giaXu(30, 99, 0, 0));
        Kiem.bang("da lan 1", 1, PhaKhoaLogic.soDa(0));
        Kiem.bang("da lan 2", 2, PhaKhoaLogic.soDa(1));
        Kiem.bang("da lan 3", 3, PhaKhoaLogic.soDa(2));
        Kiem.bang("da lan 4: het", -1, PhaKhoaLogic.soDa(3));
        // Bang tu CSDL de len mac dinh
        PhaKhoaLogic.napBang(new int[][]{{3, 100, 0}}, new int[][]{{2, 300, 5}, {1, 150, 2}});
        Kiem.bang("bang CSDL: so lan = so dong", 2, PhaKhoaLogic.lanToiDa());
        Kiem.bang("bang CSDL: sap theo lan, lan 1 = 150%, 2 da", 2, PhaKhoaLogic.soDa(0));
        Kiem.bang("bang CSDL: gia", 33925L * 100 * 100 * 150 / 1000000L, PhaKhoaLogic.giaXu(50, 3, 10, 0));
        PhaKhoaLogic.napBang(new int[0][], null);
        Kiem.bang("bang rong: giu bang dang dung", 2, PhaKhoaLogic.lanToiDa());
        PhaKhoaLogic.macDinh();
        // Dieu kien
        Kiem.dung("chua khoa: tu choi", PhaKhoaLogic.lyDoKhongDuoc(0, 0, 0, false) != null);
        Kiem.dung("khoa vi ly do khac: tu choi", PhaKhoaLogic.lyDoKhongDuoc(1, 0, 0, false).contains("lý do khác"));
        Kiem.dung("dang ban: tu choi", PhaKhoaLogic.lyDoKhongDuoc(1, 1, 0, true) != null);
        Kiem.dung("da pha 3 lan: tu choi", PhaKhoaLogic.lyDoKhongDuoc(1, 1, 3, false).contains("tối đa 3"));
        Kiem.bang("khoa do mac, 2 lan: duoc", null, PhaKhoaLogic.lyDoKhongDuoc(1, 1, 2, false));
        Kiem.bang("dinh dang xu", "1.470.757", PhaKhoaLogic.dinhDang(1470757L));
    }

    // ------------------------------------------------------------------ 3. mac that + pha khoa that

    static int demDa(final Char p) {
        return real.VatPhamDungLauDai.demItem(p, PhaKhoaLogic.ID_DA_PHA_KHOA);
    }

    static boolean dangMac(final Char p, final Item v) {
        for (final Item it : p.getArrayCharWearing()) {
            if (it == v) {
                return true;
            }
        }
        return false;
    }

    static void macVaPhaKhoa() throws Exception {
        final Char p = KiemDungVatPham.nhanVat();
        final Item v = KiemDungVatPham.cho(p, 126);
        Kiem.dung("truoc khi mac: mo", v.lock == 0);
        KiemDungVatPham.dung(p, v.id);
        Kiem.dung("mac that (goi 29): vao o trang bi", dangMac(p, v));
        Kiem.dung("mac that: khoa + khoa do mac", v.lock == 1 && v.daMac == 1);
        KiemDungVatPham.dung(p, v.id);
        Kiem.dung("thao ra: van khoa trong tui", !dangMac(p, v) && v.lock == 1 && p.iItems.contains(v));

        final Item khac = KiemDungVatPham.cho(p, 503);
        khac.lock = 1; // khoa vi ly do khac
        Kiem.dung("pha khoa do khoa vi ly do khac: tu choi",
                PhaKhoaGame.phaKhoa(p, khac.id, 1L, 1).contains("lý do khác") && khac.lock == 1);
        final Item mo = KiemDungVatPham.cho(p, 503);
        Kiem.dung("pha khoa do chua khoa: tu choi", PhaKhoaGame.phaKhoa(p, mo.id, 1L, 1).contains("chưa khoá"));

        final long gia = PhaKhoaLogic.giaXu(v.level, v.colorName, v.plus, v.soLanPhaKhoa);
        Kiem.dung("gia lan 1 > 0", gia > 0);
        String kq = PhaKhoaGame.phaKhoa(p, v.id, gia, 1);
        Kiem.dung("khong co da: tu choi, giu khoa (" + kq + ")", kq.contains("Đá phá khoá") && v.lock == 1);
        KiemDungVatPham.cho(p, PhaKhoaLogic.ID_DA_PHA_KHOA);
        KiemDungVatPham.cho(p, PhaKhoaLogic.ID_DA_PHA_KHOA);
        final long xuTruoc0 = p.getxu();
        kq = PhaKhoaGame.phaKhoa(p, v.id, gia, 1);
        if (p.getxu() >= gia) {
            Kiem.dung("(nhan vat thu da co du xu tu truoc - bo qua kiem thieu xu)", true);
        } else {
            Kiem.dung("thieu xu: tu choi (" + kq + ")", kq.startsWith("Không đủ") && v.lock == 1);
            Kiem.bang("thieu xu: khong mat da", 2, demDa(p));
            Kiem.bang("thieu xu: khong mat xu", xuTruoc0, p.getxu());
        }
        p.addXu(gia * 10, "kiem pha khoa");
        Kiem.dung("gia da hoi sai: tu choi", PhaKhoaGame.phaKhoa(p, v.id, gia + 1, 1).contains("thay đổi") && v.lock == 1);
        final long xuTruoc = p.getxu();
        kq = PhaKhoaGame.phaKhoa(p, v.id, gia, 1);
        Kiem.dung("pha khoa lan 1: thanh cong (" + kq + ")", kq.startsWith("Đã phá khoá"));
        Kiem.bang("pha khoa: tru dung gia (xu dot)", xuTruoc - gia, p.getxu());
        Kiem.bang("pha khoa: tru 1 da", 1, demDa(p));
        Kiem.dung("pha khoa: mo khoa, giao dich duoc", v.lock == 0 && v.daMac == 0 && v.giaoDichDuoc());
        Kiem.bang("pha khoa: dem 1", 1, (int) v.soLanPhaKhoa);

        KiemDungVatPham.dung(p, v.id);
        Kiem.dung("mac lai: khoa lai (khoa do mac)", dangMac(p, v) && v.lock == 1 && v.daMac == 1);
        KiemDungVatPham.dung(p, v.id);
        Kiem.bang("mac lai: dem giu 1", 1, (int) v.soLanPhaKhoa);
        Kiem.dung("lan 2 dat hon lan 1", PhaKhoaLogic.giaXu(v.level, v.colorName, v.plus, 1) > gia);
        Kiem.dung("lan 2 can 2 da, con 1: tu choi",
                PhaKhoaGame.phaKhoa(p, v.id, PhaKhoaLogic.giaXu(v.level, v.colorName, v.plus, 1), 2).contains("Đá phá khoá")
                        && v.lock == 1);

        v.soLanPhaKhoa = 3;
        Kiem.dung("da pha 3 lan: tu choi", PhaKhoaGame.phaKhoa(p, v.id, 1L, 1).contains("tối đa") && v.lock == 1);
        v.isSelling = true;
        v.soLanPhaKhoa = 1;
        Kiem.dung("dang bay ban: tu choi", PhaKhoaGame.phaKhoa(p, v.id, 1L, 1).contains("bày bán"));
        v.isSelling = false;

        // Skin / trang tri khong khoa khi mac: macVao bo qua mon da khoa / null, khong nem loi
        PhaKhoaGame.macVao(null, null);
        final Item da = new Item();
        da.lock = 1;
        PhaKhoaGame.macVao(p, da);
        Kiem.dung("macVao mon da khoa: giu khoa khac", da.daMac == 0);
    }

    // ------------------------------------------------------------------ 4. chuoi luu

    static void chuoiLuu() {
        final Item a = new Item();
        a.tempateID = 79;
        a.classTemplate = -1;
        a.clazz = 0;
        a.level = 1;
        a.khoaKhiMac();
        a.soLanPhaKhoa = 2;
        final String s = a.getDBInfo();
        final String[] f = s.split(",", -1);
        Kiem.bang("getDBInfo: 36 truong (them 2 truong cuoi)", 36, f.length);
        Kiem.bang("truong 34 = daMac", "1", f[34]);
        Kiem.bang("truong 35 = so lan pha khoa", "2", f[35]);
        final Item b = new Item();
        b.dbInfo = s;
        b.initInfoFromDB();
        Kiem.dung("doc lai: lock 1, daMac 1, dem 2", b.lock == 1 && b.daMac == 1 && b.soLanPhaKhoa == 2);

        // Chuoi cu: 34 truong (truoc 0.9.6.1)
        final StringBuilder cu = new StringBuilder();
        for (int i = 0; i < 34; i++) {
            cu.append(i == 0 ? "" : ",").append(f[i]);
        }
        final Item c = new Item();
        c.dbInfo = cu.toString();
        c.initInfoFromDB();
        Kiem.dung("chuoi cu: lock giu, daMac 0, dem 0", c.lock == 1 && c.daMac == 0 && c.soLanPhaKhoa == 0);
        Kiem.bang("chuoi cu: cac truong cu doc dung (level)", 1, (int) c.level);

        // Chuoi hong: daMac 1 ma lock 0 -> daMac bi bo (khong pha khoa / ban NPC sai)
        f[14] = "0";
        final Item d = new Item();
        d.dbInfo = String.join(",", f);
        d.initInfoFromDB();
        Kiem.dung("lock 0 + daMac 1 trong chuoi: daMac ve 0", d.lock == 0 && d.daMac == 0 && d.soLanPhaKhoa == 2);
        Kiem.bang("cloneItem (mua o gian hang): giu dem pha khoa", 2, (int) a.cloneItem().soLanPhaKhoa);
    }

    // ------------------------------------------------------------------ 5. quet ma nguon

    static void quetDuong() {
        final String mb = doc("src/real/MapMuaBan.java");
        Kiem.bang("mac do nhan vat + nhan: goi PhaKhoaGame.macVao (2 duong)", 2, dem(mb, "PhaKhoaGame.macVao(player, iItem)"));
        final String map = doc("src/real/Map.java");
        final String thu = doan(map, "void exchangeItemAnimal(", 2500);
        Kiem.dung("mac trang bi thu: khoa (tru skin)", thu.contains("PhaKhoaGame.macVao(player, iItem)")
                && thu.contains("SkinThuCuoi.laItemSkin(iItem.getTemplate().id)"));
        Kiem.dung("mac than khi: khoa", map.contains("player.itemThanKhi = item;\n                        PhaKhoaGame.macVao(player, item);"));
        final String nap = doc("src/real/CharTrangBi.java");
        Kiem.bang("migration luc nap (do nhan vat + trang bi thu dang mac)", 2, dem(nap, "item.khoaKhiMac()"));
        Kiem.bang("migration duong nap cu (CharNapDuLieu)", 2, dem(doc("src/real/CharNapDuLieu.java"), "item.khoaKhiMac()"));

        // Duong dua do cho nguoi khac
        final String gd = doan(mb, "private static void doTradeDuoiKhoa(", 12000);
        Kiem.dung("giao dich: tu choi moi vat pham trang bi (kieu 0)",
                gd.contains("if (type == 0) {") && gd.contains("Không thể trao đổi vật phẩm này."));
        final String ch = doc("src/real/Char.java");
        Kiem.dung("giao dich: chot cuoi chan do khoa", doan(ch, "public boolean doAddItemTrad2Inventory()", 1200).contains("giaoDichDuoc()"));
        final String off = doc("src/real/OfflineMap.java");
        Kiem.dung("gian hang / ky gui: chan do khoa (lock 1) truoc moi nhanh", off.contains("if (((Item)item).lock == 1) {")
                && dem(off, "((Item)item).lock == 1") >= 3);
        final String datRa = doan(map, "void doGiveItemToGround(", 1500);
        Kiem.dung("bo ra dat: do khoa khong ra dat - chi Huy (0.9.8, BanDoKhoa)", datRa.contains("final boolean huyKhoa = item.lock != 0;")
                && datRa.contains("BanDoKhoa.huyDuoc("));
        Kiem.sai("khong co duong thu / tang vat pham giua nguoi choi (mail)",
                ch.contains("guiThuVatPham") || map.contains("guiThuVatPham"));
        // Ban NPC
        final String ban = doan(off, "public void doSellItem(", 3500);
        Kiem.dung("ban NPC: do khoa do mac van ban duoc, do khoa khac theo cau hinh",
                ban.contains("!item.laKhoaDoMac() && item.lock == 1 && !com.htbb.pokemon.ChongClone.banNpcDoKhoa()"));
        // Khoa vi ly do khac tren mon da co: xoa co khoa do mac
        final String[][] khoaKhac = {
            {"src/real/MapCheTao.java", "khoa thuoc tinh"},
            {"src/real/NangCapLauDaiGame.java", "nang cap nguyen lieu khoa"},
            {"src/real/CharCapNhat.java", "ep ngoc khoa"},
            {"src/real/MapEpNgoc.java", "ep ngoc khoa"},
            {"src/BaoVatHuyenThoai/ThoRenBiAn.java", "than khi nguyen lieu khoa"},
            {"src/data/ItemChangeColor.java", "doi mau"},
            {"src/real/Map.java", "tinh luyen vu khi thoi trang"},
        };
        for (final String[] k : khoaKhac) {
            Kiem.dung(k[1] + ": khoaVinhVien (khong pha khoa duoc)", doc(k[0]).contains("khoaVinhVien()"));
        }
        Kiem.bang("OfflineMap khoa thuoc tinh (2 duong): khoaVinhVien", 2, dem(off, "item.khoaVinhVien();"));
        // Luong khoa
        final String pk = doc("src/real/PhaKhoaGame.java");
        Kiem.sai("pha khoa: khong dong luong / luong khoa", pk.contains("Luong") || pk.contains("lockLuong"));
        Kiem.dung("pha khoa: tru xu kiem-va-tru mot buoc (dot)", pk.contains("p.truXuNeuDu(gia, true,"));
        Kiem.dung("pha khoa: ghi thong ke chi tieu xu nhu ho hut khac", pk.contains("saveLogThongKeChiTieu(\"Pha khoa trang bi_xu\""));
        Kiem.sai("giao dich truc tiep khong cong luong khoa", mb.contains("addLuongLock"));
        // NPC
        final String npc = doc("src/real/cmd/RequestNPCInfoHandler.java");
        Kiem.dung("Machamp (NPC 2): muc Phá khoá trang bị",
                npc.contains("{\"Mua đồ cầm tay\", \"Mua trang bị thú\", real.PhaKhoaGame.TEN_MUC}"));
        final String dm = doc("src/real/MapDoMenu.java");
        Kiem.dung("MapDoMenu: chon muc 3 Machamp + NPC ao pha khoa",
                dm.contains("idOptionMenu == PhaKhoaGame.VI_TRI_MUC_THO_REN") && dm.contains("idNpc == PhaKhoaGame.NPC_ID"));
        Kiem.dung("xac nhan hien dung gia (xu + da)", pk.contains("PhaKhoaLogic.dinhDang(c.gia) + \" xu + \" + c.soDa + \" Đá phá khoá\""));
    }

    static void quetSql() {
        final String sql = DayDu.phanNangCap("sua_096_1_logic.sql");
        final int a = sql.indexOf("-- [khoa-khi-mac]");
        final int b = sql.indexOf("-- [/khoa-khi-mac]");
        Kiem.dung("sua_096_1_logic.sql: khoi [khoa-khi-mac] ... [/khoa-khi-mac]", a > 0 && b > a);
        final String k = a > 0 && b > a ? sql.substring(a, b) : "";
        Kiem.dung("SQL: item Da pha khoa " + PhaKhoaLogic.ID_DA_PHA_KHOA,
                k.contains("(" + PhaKhoaLogic.ID_DA_PHA_KHOA + ",'Đá phá khoá',28,"));
        Kiem.dung("SQL idempotent (IF NOT EXISTS / IGNORE / ON DUPLICATE)", k.contains("CREATE TABLE IF NOT EXISTS `data_pha_khoa_bac`")
                && k.contains("CREATE TABLE IF NOT EXISTS `data_pha_khoa_lan`") && k.contains("ON DUPLICATE KEY UPDATE")
                && !k.contains("INSERT INTO `data_pha_khoa") && !k.contains("DROP TABLE"));
        Kiem.dung("SQL: bang lan khop mac dinh", k.contains("(1,100,1),(2,200,2),(3,400,3)"));
        Kiem.dung("SQL: bang bac khop mac dinh (Quy 200%, Su thi 300%, Huyen thoai 500%)",
                k.contains("(3,'Quý',200,10)") && k.contains("(4,'Sử thi',300,10)") && k.contains("(6,'Huyền thoại',500,10)"));
        Kiem.dung("SQL: nguon da = hop dot kich boss", k.contains("'mo_hop','item:1051','item:1295'") && k.contains("'mo_hop','item:1050','item:1295'"));
        Kiem.dung("SQL ghi migration lam o server luc nap", k.contains("setItemWearing") && k.contains("idempotent"));
        final String dump = doc("../sql/htbb_day_du.sql");
        Kiem.dung("htbb_day_du.sql: co item 1295 + 2 bang + dong roi",
                dump.contains("(1295,'Đá phá khoá',28,") && dump.contains("CREATE TABLE `data_pha_khoa_bac`")
                        && dump.contains("CREATE TABLE `data_pha_khoa_lan`") && dump.contains("(9501,'mo_hop','item:1051','item:1295'"));
        final int ch = dump.indexOf("INSERT INTO `data_cua_hang_lau_dai` VALUES");
        final String cuaHang = ch < 0 ? "" : dump.substring(ch, dump.indexOf('\n', ch));
        Kiem.sai("Da pha khoa khong ban o cua hang (luong / luong khoa)", cuaHang.contains("ITEM:1295"));
        Kiem.sai("Da pha khoa khong co trong ma shop luong", doc("src/real/doBuySpecialItem.java").contains("1295"));
    }
}
