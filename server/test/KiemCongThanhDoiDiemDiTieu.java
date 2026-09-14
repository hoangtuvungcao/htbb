import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Yeu cau nguoi dung 13/09/2026 - mot lan gui gom 3 mang gameplay khong lien quan:
 *
 * 1) Cong thanh:
 *    - Bo tran cung "10 muc tieu" trong CharTrangBi.checkNewEffectItem (dang lam mat
 *      attacker va cat bot Set Lan khi sl[1]=15 vuot qua 10).
 *    - Chan CHEO ca 4 loai lan (Bang/Set/Doc/Lua): dang dinh lan nao thi KHONG the dinh
 *      them lan khac cho toi khi het (LiveActor.hetHanLan, moc thoi gian dung chung).
 *    - Nang nguong vao thanh cong thanh tu <50 len <70 (4 vi tri: MapXuLyLenh, MapLienDau
 *      doChangeMap, Char.kickOutTown, MapTown.playerGiveCard) - KHONG dung toi
 *      MapLienDau.playerGiveCard (gate rieng, khong lien quan, cho "Thanh Lien Dau").
 *
 * 2) NPC doi diem (Map.TEN_DOI_THUONG/GIA_DOI_THUONG + MapQuaTang.traoVatPhamDoiThuong):
 *    - Bo han Phuong Hoang Lua, Giap Rong, Ho ve lenh 4x.
 *    - Dau Bi Ngo/Non Phu Thuy 800->2000, VKTT/Nguyet Linh Truong 1200->1500, HVL 5x/6x
 *      2000->800, Skin linh thu tu vinh vien sang 7 ngay (gia giu 2000).
 *    - Canh/Choi: gop 4 muc rieng le thanh 1 muc NGAU NHIEN he+chi so, TRU tier cao nhat
 *      (Item.createOptionCanhNgauNhien/createAttChoiNgauNhien).
 *    - Ty le canh/choi VINH VIEN trong Lucky Draw: 0.1% -> 0.0001% (ca hai, choi truoc
 *      day khong co nhanh vinh vien, nay them moi).
 *
 * 3) Di tieu:
 *    - Toi da 5 tieu/ngay: 3 mien phi (luot_van_tieu, khong doi) + 2 luot MUA/ngay
 *      (buy_luot_van_tieu 3+vip -> 2 co dinh) + chan cung o diem DUNG ve (doUsePotion
 *      case 82, luot_van_tieu>=5 thi tu choi va hoan lai ve - vi kho ve (potions[82])
 *      dung CHUNG index voi nguyen lieu "tre" cua su kien Trung Thu nen KHONG duoc void
 *      thang o day).
 *    - cuop_tieu: 3 -> 5/ngay.
 *    - Ha ty le tieu Tim (doInitMonter) xuong 0.001%, Vang xuong 0.01%; dong bo ha luon
 *      bang doi tieu tra phi (doChangeTieu) de khong lam vo nghia viec ha ty le goc.
 *    - Them Hoan My vao nhanh cap luong/luong khoa (truoc chi Vang+Tim).
 *    - Cong thuc exp MOI (% cua exp can len cap) cho Hoan My/Vang/Tim tu lv50, giam dan
 *      theo moc cap (50-59: 5/7/9%, 60-69: 3.5/5.5/7.5%, 70+: 0.5/2.5/4.5%) - duoi lv50
 *      va Trang/Xanh/Do giu nguyen cong thuc cu.
 *    - Tu lv70: bo luong/luong khoa, thay bang bot xanh lam. Cap nhat cung ngay: Hoan
 *      My/Vang/Tim doi tu random 1-3 sang muc CO DINH rieng (Hoan My 10/Vang 20/Tim 30,
 *      Trang/Xanh/Do van giu random 1-3 cu); bi cuop chi con MOT NUA (chia cho div).
 *    - Diem doi thuong (diemDoiThuong): chi Hoan My/Vang/Tim moi duoc, khong con ap dung
 *      cho ca 6 mau nhu truoc. Cap nhat cung ngay: moi mau mot muc rieng (Hoan My 15/Vang
 *      30/Tim 50, truoc do ca 3 mau dung chung 50), bi cuop chi con MOT NUA.
 */
public final class KiemCongThanhDoiDiemDiTieu {

    private KiemCongThanhDoiDiemDiTieu() {
    }

    public static void chay() {
        Kiem.nhom("Cong thanh: lan chong lan + gioi han muc tieu + cap 70 vao thanh");
        kiemLanChongLan();
        kiemCapDoVaoThanh();

        Kiem.nhom("NPC doi diem: bo/doi gia + canh+choi ngau nhien + ty le vinh vien");
        kiemNpcDoiDiem();
        kiemTyLeVinhVienLuckyDraw();

        Kiem.nhom("Di tieu: gioi han ngay + ty le mau + cong thuc exp moi");
        kiemGioiHanDiTieuMoiNgay();
        kiemTyLeMauTieu();
        kiemCongThucExpVaThuongLv70();
    }

    private static void kiemLanChongLan() {
        final String ma = doc("src/real/CharTrangBi.java");
        Kiem.dung("doc duoc src/real/CharTrangBi.java", !ma.isEmpty());
        if (ma.isEmpty()) {
            return;
        }
        // Chi bat dung dong CODE cu (co khoang trang la "10 )" va chu thich tieng Viet cua no),
        // khong bat vao chu thich giai thich moi co nhac lai chuoi "if (i == 10) break;".
        Kiem.sai("KHONG con tran cung 'if (i == 10 ) break;' (tung cat bot Set Lan sl[1]=15)",
                ma.contains("if (i == 10 ) break;") || ma.contains("Chỉ tính 10 mục tiêu"));
        Kiem.dung("dieu kien resist co them chan CHEO qua hetHanLan",
                ma.contains("|| actor.hetHanLan > System.currentTimeMillis()"));

        // Dem so lan dat hetHanLan khi mot lan THAT SU ap dung (Doc/Bang/Set-heo/Set-thuong/Lua = 5).
        final String GAN = "hetHanLan = System.currentTimeMillis() + ";
        int soLan = 0;
        int i = ma.indexOf(GAN);
        while (i >= 0) {
            soLan++;
            i = ma.indexOf(GAN, i + GAN.length());
        }
        Kiem.bang("hetHanLan duoc dat dung 5 cho (Doc, Bang, Set x2 nhanh, Lua)",
                Integer.valueOf(5), Integer.valueOf(soLan));
        Kiem.dung("Doc Lan dat hetHanLan +5000ms (5 giay, khop totalTime=5 tick x tDelay=1s)",
                ma.contains("actor.hetHanLan = System.currentTimeMillis() + 5000L;"));
        Kiem.dung("Bang Lan dat hetHanLan +3000ms (3 giay, khop hien thi khamEff)",
                ma.contains("actor.hetHanLan = System.currentTimeMillis() + 3000L;"));
        Kiem.dung("Set Lan (nhanh 'else') dat hetHanLan theo dung bien msChoang (co the 3-6s)",
                ma.contains("actor.hetHanLan = System.currentTimeMillis() + msChoang;"));
        Kiem.dung("Lua Lan dat hetHanLan +5000ms (khop FIRE_BURN, hieu ung dai nhat)",
                ma.contains("c.hetHanLan = System.currentTimeMillis() + 5000L;"));

        final String liveActor = doc("src/real/LiveActor.java");
        Kiem.dung("doc duoc src/real/LiveActor.java", !liveActor.isEmpty());
        Kiem.dung("LiveActor khai bao truong hetHanLan (long, khoi tao 0L)",
                liveActor.contains("public long hetHanLan = 0L;"));
    }

    private static void kiemCapDoVaoThanh() {
        final String xuLyLenh = doc("src/real/MapXuLyLenh.java");
        Kiem.dung("MapXuLyLenh: nguong vao thanh la <70 (cong thanh)",
                xuLyLenh.contains("player.lvDetail.lv < 70 && Map.getTown[player.inCountry] && toMapID == Map.idMapTown"));
        Kiem.dung("MapXuLyLenh: thong bao da doi thanh '70'",
                xuLyLenh.contains("Bạn phải đạt cấp độ 70 trở lên mới có thể vào thành trong thời gian này."));

        final String lienDau = doc("src/real/MapLienDau.java");
        Kiem.dung("MapLienDau.doChangeMap: nguong vao thanh (cong thanh that) la <70",
                lienDau.contains("player.lvDetail.lv < 70 && getTown[player.inCountry] && toMapID == idMapTown"));
        // Gate rieng, khong lien quan (playerGiveCard cua "Thanh Lien Dau" - tinh nang PvE
        // khac, chi trung ten) van phai con nguyen <50, KHONG duoc dong theo.
        Kiem.dung("MapLienDau.playerGiveCard (Thanh Lien Dau, KHONG lien quan) van con nguyen <50",
                lienDau.contains("if (player.lvDetail.lv < 50) {")
                        && lienDau.contains("Bạn phải đạt cấp độ 50 trở lên mới có thể giao thẻ."));

        final String charJava = doc("src/real/Char.java");
        Kiem.dung("Char.kickOutTown: nguong la <70",
                charJava.contains("(Map.getTown[this.inCountry] && this.myCountry != this.inCountry) || (Map.getTown[this.inCountry] && this.lvDetail.lv < 70))"));

        final String mapTown = doc("src/real/MapTown.java");
        Kiem.dung("MapTown.playerGiveCard: nguong la <70",
                mapTown.contains("if (player.lvDetail.lv < 70) {"));
        Kiem.dung("MapTown: thong bao da doi thanh '70'",
                mapTown.contains("Bạn phải đạt cấp độ 70 trở lên mới có thể giao thẻ."));
    }

    private static void kiemNpcDoiDiem() {
        final String mapJava = doc("src/real/Map.java");
        Kiem.dung("doc duoc src/real/Map.java", !mapJava.isEmpty());
        final int viTriMang = mapJava.indexOf("static final String[] TEN_DOI_THUONG = {");
        Kiem.dung("tim thay mang TEN_DOI_THUONG", viTriMang >= 0);
        if (viTriMang < 0) {
            return;
        }
        final int ketThucMang = mapJava.indexOf("static final int[] GIA_DOI_THUONG", viTriMang);
        final String thanMang = mapJava.substring(viTriMang, ketThucMang > viTriMang ? ketThucMang : mapJava.length());

        Kiem.sai("da bo Phuong Hoang Lua khoi doi diem", thanMang.contains("Phượng Hoàng Lửa"));
        Kiem.sai("da bo Giap Rong khoi doi diem", thanMang.contains("Giáp Rồng"));
        Kiem.sai("da bo Ho ve lenh 4x khoi doi diem", thanMang.contains("Hộ vệ lệnh 4x"));
        Kiem.dung("con giu Ho ve lenh 5x", thanMang.contains("Hộ vệ lệnh 5x random lv1-3, 7 ngày"));
        Kiem.dung("con giu Ho ve lenh 6x", thanMang.contains("Hộ vệ lệnh 6x random lv1-3, 7 ngày"));
        Kiem.dung("Canh gop thanh 1 muc ngau nhien", thanMang.contains("Cánh ngẫu nhiên (hệ + chỉ số random) 7 ngày"));
        Kiem.dung("Choi gop thanh 1 muc ngau nhien", thanMang.contains("Chổi ngẫu nhiên (hệ + chỉ số random) 7 ngày"));
        Kiem.dung("Skin linh thu doi sang 7 ngay (khong con vinh vien)",
                thanMang.contains("Skin linh thú Phúc Thú 7 ngày") && !thanMang.contains("\"Skin linh thú Phúc Thú\","));

        Kiem.dung("gia VKTT/Hoa thach/Nghe Thuong/Xich Cuoc/NLT dung: 1500,800,1200,1500,1500",
                mapJava.contains("500, 500, 1500, 800, 1200, 1500, 1500,"));
        Kiem.dung("gia Dau Bi Ngo/Non Phu Thuy da tang len 2000 (2 dong '2000, 2000,' lien tiep)",
                mapJava.contains("2000, 2000,\n            2000, 2000, 2000, 2000, 2000,\n            800, 800,"));
        // Doi chieu so luong ten <-> gia: dem so dau ngoac kep (moi ten 1 cap) va so phan
        // tu trong GIA_DOI_THUONG (dem dau phay + 1).
        int soTen = 0;
        int p = thanMang.indexOf('"');
        while (p >= 0) {
            final int q = thanMang.indexOf('"', p + 1);
            if (q < 0) {
                break;
            }
            soTen++;
            p = thanMang.indexOf('"', q + 1);
        }
        Kiem.bang("con dung 22 ten vat pham doi diem (28 truoc khi gop Canh/Choi, tru 3 bo + 6 gop = 22)",
                Integer.valueOf(22), Integer.valueOf(soTen));

        final String itemJava = doc("src/real/Item.java");
        Kiem.dung("Item co ham createAttChoiNgauNhien(boolean)", itemJava.contains("public void createAttChoiNgauNhien(final boolean loaiTruTierCaoNhat)"));
        Kiem.dung("Item co ham createOptionCanhNgauNhien(int, boolean)", itemJava.contains("public void createOptionCanhNgauNhien(final int id, final boolean loaiTruTierCaoNhat)"));

        final String quaTang = doc("src/real/MapQuaTang.java");
        Kiem.dung("doc duoc src/real/MapQuaTang.java", !quaTang.isEmpty());
        Kiem.dung("switch doi diem: case 7 la Canh ngau nhien (goi createOptionCanhNgauNhien)",
                quaTang.contains("case 7: { // Canh ngau nhien") && quaTang.contains("newItem.createOptionCanhNgauNhien(idTemplate - 898, true);"));
        Kiem.dung("switch doi diem: case 12 la Choi ngau nhien (goi createAttChoiNgauNhien)",
                quaTang.contains("case 12: { // Choi ngau nhien") && quaTang.contains("newItem.createAttChoiNgauNhien(true);"));
        Kiem.dung("switch doi diem: khong con case Phuong Hoang Lua rieng (case 4 cu)",
                !quaTang.contains("case 4: // Phuong Hoang Lua"));
        Kiem.dung("switch doi diem: Skin linh thu (case 15-19) truyen PHUT_7_NGAY thay vi 0",
                quaTang.contains("GiftCode.AddItem(player, SkinLinhThu.ID_ITEM[idx - 15], 0, 0, PHUT_7_NGAY);"));
        Kiem.dung("switch doi diem: HVL 5x/6x la case 20/21", quaTang.contains("case 20: // Ho ve lenh 5x") && quaTang.contains("case 21: // Ho ve lenh 6x"));
    }

    private static void kiemTyLeVinhVienLuckyDraw() {
        final String luckyDraw = doc("src/data/ItemLuckyDraw.java");
        Kiem.dung("doc duoc src/data/ItemLuckyDraw.java", !luckyDraw.isEmpty());
        // Dem so lan xuat hien nguong "pc1 < 50" tren thang nextInt(1000000) - phai co
        // CA HAI nhanh (Canh va Choi moi them) dung chung cong thuc 0.0001%.
        final String NGUONG = "if (pc1 < 50) {";
        int soLan = 0;
        int i = luckyDraw.indexOf(NGUONG);
        while (i >= 0) {
            soLan++;
            i = luckyDraw.indexOf(NGUONG, i + NGUONG.length());
        }
        Kiem.bang("ca Canh va Choi cung dung nguong vinh vien 'pc1<50' tren thang 1 trieu (0.0001%)",
                Integer.valueOf(2), Integer.valueOf(soLan));
        Kiem.bang("dung 2 cho (Canh + Choi) doi sang thang nextInt(1000000)",
                Integer.valueOf(2), Integer.valueOf(demSoLan(luckyDraw, "int pc1 = Map.r.nextInt(1000000);")));
        // Nhanh phi phong World Cup (id 616) van dung nextInt(100) - KHONG lien quan, giu nguyen.
        Kiem.bang("chi con 1 cho dung nextInt(100) cho pc1 (nhanh phi phong 616, ngoai pham vi)",
                Integer.valueOf(1), Integer.valueOf(demSoLan(luckyDraw, "int pc1 = Map.r.nextInt(100);")));
    }

    private static void kiemGioiHanDiTieuMoiNgay() {
        final String khoiTao = doc("src/real/CharKhoiTao.java");
        Kiem.dung("CharKhoiTao: buy_luot_van_tieu khoi tao = 2", khoiTao.contains("self.buy_luot_van_tieu = 2;"));
        Kiem.dung("CharKhoiTao: cuop_tieu khoi tao = 5", khoiTao.contains("self.cuop_tieu = 5;"));

        final String capNhat = doc("src/real/CharCapNhat.java");
        Kiem.dung("CharCapNhat: buy_luot_van_tieu reset ngay = 2 co dinh (khong con 3+vip)",
                capNhat.contains("self.buy_luot_van_tieu = 2;"));
        Kiem.sai("CharCapNhat: KHONG con cong them vip vao buy_luot_van_tieu",
                capNhat.contains("self.buy_luot_van_tieu = (byte) (3 + self.vip);"));
        Kiem.dung("CharCapNhat: cuop_tieu reset ngay = 5", capNhat.contains("self.cuop_tieu = 5;"));

        final String napDuLieu = doc("src/real/CharNapDuLieu.java");
        Kiem.dung("CharNapDuLieu: buy_luot_van_tieu reset ngay = 2 co dinh",
                napDuLieu.contains("self.buy_luot_van_tieu = 2;"));
        Kiem.dung("CharNapDuLieu: cuop_tieu reset ngay = 5", napDuLieu.contains("self.cuop_tieu = 5;"));

        final String usePotion = doc("src/real/doUsePotion.java");
        Kiem.dung("doc duoc src/real/doUsePotion.java", !usePotion.isEmpty());
        Kiem.dung("case 82 (dung the van tieu) chan tran 5 tieu/ngay",
                usePotion.contains("if (player.luot_van_tieu < 5) {")
                        && usePotion.contains("++player.luot_van_tieu;"));
        Kiem.dung("case 82: khi da du 5, hoan lai ve (potions[potionType]++) thay vi mat khong",
                usePotion.contains("Một ngày chỉ được đi tối đa 5 tiêu, không thể dùng thêm vé."));
    }

    private static void kiemTyLeMauTieu() {
        final String charJava = doc("src/real/Char.java");
        Kiem.dung("doc duoc src/real/Char.java", !charJava.isEmpty());

        // Char.java co nhieu cho khac dung nextInt(100000) tu truoc - chi dem dung dong roll mau tieu.
        Kiem.bang("co dung 2 lan 'final int ran = Map.r.nextInt(100000);' (doInitMonter + doChangeTieu)",
                Integer.valueOf(2), Integer.valueOf(demSoLan(charJava, "final int ran = Map.r.nextInt(100000);")));
        // 14/09/2026: 5 nguong doInitMonter chuyen tu literal hardcode sang truong static
        // Char.TIEU_NGUONG_MAU de quan ly duoc qua SQL (server.CauHinh) - xem
        // docs/cau-hinh-server-ini.md muc 5c. Gia tri GIU NGUYEN, chi doi CHO O.
        Kiem.dung("Char khai bao TIEU_NGUONG_MAU giu nguyen 5 nguong cu (60189,89389,99689,99989,99999)",
                charJava.contains("public static int[] TIEU_NGUONG_MAU = {60189, 89389, 99689, 99989, 99999};"));
        Kiem.dung("doInitMonter: doc nguong tu Char.TIEU_NGUONG_MAU (khong con hardcode) - Vang la nguong[4]",
                charJava.contains("ran < nguongMau[4]) {") && charJava.contains("indexMons = 4; // Vang"));
        Kiem.dung("doInitMonter: Tim la nhanh else (ran >= nguongMau[4], gia tri con lai duy nhat)",
                charJava.contains("// else: Tim (indexMons=5) - ran >= nguongMau[4]"));

        Kiem.dung("doChangeTieu: xu reroll Vang con 0.05% (nguong 99995, index=4)",
                charJava.contains("ran < 99995) {") && charJava.contains("index = 4; // Vang 0.05%"));
        Kiem.dung("doChangeTieu: xu reroll Tim con 0.005%", charJava.contains("// else: Tim - 0.005%"));
        Kiem.dung("doChangeTieu: luong reroll Vang con 0.1% (nguong 99990, index=4)",
                charJava.contains("ran < 99990) {") && charJava.contains("index = 4; // Vang 0.1%"));
        Kiem.dung("doChangeTieu: luong reroll Tim con 0.01%", charJava.contains("// else: Tim - 0.01%"));
    }

    private static void kiemCongThucExpVaThuongLv70() {
        final String suKien = doc("src/real/CharSuKien.java");
        Kiem.dung("doc duoc src/real/CharSuKien.java", !suKien.isEmpty());
        if (suKien.isEmpty()) {
            return;
        }
        final String charJava2 = doc("src/real/Char.java");
        Kiem.dung("doc duoc src/real/Char.java", !charJava2.isEmpty());

        // 14/09/2026: PERMILLE_TIEU_CAO_CAP, pcExpAdd va "* 2" (TangExpVanTieu) chuyen tu
        // bien LOCAL trong CharSuKien.doResponseTieu() sang truong static
        // Char.TIEU_PERMILLE_CAO_CAP / TIEU_PC_EXP_THEO_MAU / TIEU_HE_SO_EXP de quan ly
        // duoc qua SQL (server.CauHinh) - xem docs/cau-hinh-server-ini.md muc 5c. Gia tri
        // GIU NGUYEN, chi doi CHO O: mang 2 chieu {{50,70,90},{35,55,75},{5,25,45}} gop
        // phang thanh 1 chieu {50,70,90,35,55,75,5,25,45}, chi so tra = khung*3 + (mau-3).
        Kiem.dung("Char khai bao TIEU_PERMILLE_CAO_CAP gop phang dung 9 gia tri cu (5.0/7.0/9.0, 3.5/5.5/7.5, 0.5/2.5/4.5%)",
                charJava2.contains("public static int[] TIEU_PERMILLE_CAO_CAP = {50, 70, 90, 35, 55, 75, 5, 25, 45};"));
        Kiem.dung("Char khai bao TIEU_PC_EXP_THEO_MAU giu nguyen 7 gia tri cu",
                charJava2.contains("public static int[] TIEU_PC_EXP_THEO_MAU = {50, 100, 150, 250, 250, 500, 0};"));
        Kiem.dung("Char khai bao TIEU_HE_SO_EXP = 200 (giu nguyen '* 2' TangExpVanTieu cu, dang % de quan ly qua SQL)",
                charJava2.contains("public static int TIEU_HE_SO_EXP = 200;"));

        Kiem.dung("CharSuKien doc bang tu Char.TIEU_PERMILLE_CAO_CAP (khong con khai bao rieng)",
                suKien.contains("final int[] permilleCaoCap = Char.TIEU_PERMILLE_CAO_CAP;"));
        Kiem.dung("CharSuKien tra chi so gop phang dung cong thuc khung*3 + (mau-3)",
                suKien.contains("final int permille = permilleCaoCap[bracket * 3 + (colortieu - 3)];"));
        Kiem.dung("CharSuKien doc pcExpAdd tu Char.TIEU_PC_EXP_THEO_MAU (khong con khai bao rieng)",
                suKien.contains("final int[] pcExpAdd = Char.TIEU_PC_EXP_THEO_MAU;"));
        Kiem.dung("duoi lv50 HOAC mau Trang/Xanh/Do van dung cong thuc EXP_NV_HANG_NGAY cu (nhan Char.TIEU_HE_SO_EXP/100 thay '* 2')",
                suKien.contains("if (lvOpen < 50 || colortieu < 3) {")
                        && suKien.contains("exp = Char.EXP_NV_HANG_NGAY[indexExp][idexp[colortieu]] * Char.TIEU_HE_SO_EXP / 100L;"));
        Kiem.dung("tu lv50 (Hoan My/Vang/Tim): exp = % cua LevelDetail.expRequire[lv]",
                suKien.contains("exp = LevelDetail.expRequire[lvExp] * permille / 1000L;"));

        Kiem.dung("luongadd them Hoan My (idx3, truoc day chi Vang/Tim)",
                suKien.contains("final int[] luongadd = {0, 0, 0, 100 + Map.r.nextInt(11), 100 + Map.r.nextInt(11), 100 + Map.r.nextInt(11), 0};"));
        Kiem.dung("tu lv70: khoa nhanh cap luong/luong khoa lai (lvOpen < 70)",
                suKien.contains("if (luongadd[colortieu] > 0 && lvOpen < 70) {"));
        Kiem.dung("tu lv70: cap bot xanh lam thay the (GemTemplate.BOT_XANH)",
                suKien.contains("self.doAddGemItem(GemTemplate.BOT_XANH, slBotXanh, true);"));
        Kiem.dung("bot xanh lam: Hoan My/Vang/Tim dung muc co dinh rieng (10/20/30), bi cuop chia div",
                suKien.contains("final int[] BOT_XANH_THEO_MAU = {10, 20, 30}; // [Hoan My, Vang, Tim]")
                        && suKien.contains("slBotXanh = Math.max(1, BOT_XANH_THEO_MAU[colortieu - 3] / div);"));
        Kiem.dung("bot xanh lam: Trang/Xanh/Do van giu random 1-3 cu",
                suKien.contains("slBotXanh = Math.max(1, (Map.r.nextInt(3) + 1) / div);"));

        Kiem.dung("diem doi thuong: chi ap dung tu Hoan My tro len (colortieu >= 3)",
                suKien.contains("if (colortieu >= 3) {") && suKien.contains("final int[] DIEM_DOI_THUONG = {15, 30, 50}; // [Hoan My, Vang, Tim]"));
        Kiem.dung("diem doi thuong: moi mau mot muc rieng, bi cuop chi con mot nua (chia cho div)",
                suKien.contains("final int diem = DIEM_DOI_THUONG[colortieu - 3] / div;"));
    }

    private static int demSoLan(final String noiDung, final String mau) {
        int soLan = 0;
        int i = noiDung.indexOf(mau);
        while (i >= 0) {
            soLan++;
            i = noiDung.indexOf(mau, i + mau.length());
        }
        return soLan;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
