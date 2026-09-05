import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Cai Tao Cai Trang (real/CaiTaoCaiTrang) - luat viet lai ngay 02/09/2026 theo mo ta cua
 * nguoi yeu cau. Phan luat la ham thuan nen goi THAT; phan noi vao Map/RealController/
 * CharChiSo can Char that nen quet van ban nguon, giong cach KiemTinhLuyenThanBinh da lam.
 *
 * Nhung cai de hong am tham ma test nay canh:
 *   - hai buoc (chon o type == 5, xac nhan o idpopup == 125) loc danh sach khac nhau ->
 *     client gui chi so cua danh sach nay, server tru diem cho mon o danh sach kia
 *   - tru diem SAU khi quay -> that bai khong mat diem, trai mo ta
 *   - tra bang luong hay diem nap cho nhom 1-2 (hanh vi cu) thay vi diem xai
 *   - doi ten ca chuoi -> mat cac dong mo ta hieu ung nam trong cot name
 *   - Tinh Hoa cong ca MP / tan cong (hanh vi cu) thay vi CHI HP
 *   - bo dang mac doi CaiTao ma khong tinh lai chi so / khong gui lai hinh
 */
public final class KiemCaiTaoCaiTrang {

    private KiemCaiTaoCaiTrang() {
    }

    public static void chay() {
        Kiem.nhom("Cai Tao Cai Trang - nhom, gia, ti le, ten hien thi");

        kiemNhomVaGia();
        kiemQuayPhanTram();
        kiemTenHienThi();
        kiemMap();
        kiemRealController();
        kiemCharChiSo();
        kiemModelWearingVaLog();
    }

    private static void kiemNhomVaGia() {
        final int[] nhom1 = {797, 798, 799, 800, 801};
        final int[] nhom2 = {723, 724, 725, 726};
        final int[] nhom3 = {756, 757};
        for (final int id : nhom1) {
            Kiem.bang(id + " thuoc nhom 1", Integer.valueOf(1), Integer.valueOf(real.CaiTaoCaiTrang.nhom(id)));
            Kiem.bang(id + " ton 500 diem", Integer.valueOf(500), Integer.valueOf(real.CaiTaoCaiTrang.diemCan(id)));
            Kiem.sai(id + " tra bang diem XAI (khong phai diem nap)", real.CaiTaoCaiTrang.dungDiemNap(id));
        }
        for (final int id : nhom2) {
            Kiem.bang(id + " thuoc nhom 2", Integer.valueOf(2), Integer.valueOf(real.CaiTaoCaiTrang.nhom(id)));
            Kiem.bang(id + " ton 300 diem", Integer.valueOf(300), Integer.valueOf(real.CaiTaoCaiTrang.diemCan(id)));
            Kiem.sai(id + " tra bang diem XAI", real.CaiTaoCaiTrang.dungDiemNap(id));
        }
        for (final int id : nhom3) {
            Kiem.bang(id + " thuoc nhom 3", Integer.valueOf(3), Integer.valueOf(real.CaiTaoCaiTrang.nhom(id)));
            Kiem.bang(id + " ton 500 diem", Integer.valueOf(500), Integer.valueOf(real.CaiTaoCaiTrang.diemCan(id)));
            Kiem.dung(id + " tra bang diem NAP", real.CaiTaoCaiTrang.dungDiemNap(id));
        }
        Kiem.bang("ten diem nhom 1", "điểm xài", real.CaiTaoCaiTrang.tenDiem(797));
        Kiem.bang("ten diem nhom 3", "điểm nạp", real.CaiTaoCaiTrang.tenDiem(756));

        // Khong cai tao duoc: do thuong, "Son Tinh"/"Thuy Tinh" goc (736/737), ao dai (754/755),
        // Ao Lac long quan CU (265).
        for (final int id : new int[]{1, 265, 736, 737, 754, 755, 0, -1}) {
            Kiem.bang(id + " khong thuoc nhom nao", Integer.valueOf(0), Integer.valueOf(real.CaiTaoCaiTrang.nhom(id)));
            Kiem.bang(id + " diemCan = -1", Integer.valueOf(-1), Integer.valueOf(real.CaiTaoCaiTrang.diemCan(id)));
        }

        // caiTaoDuoc: phai vinh vien VA thuoc nhom.
        final real.Item vinhVien = new real.Item();
        vinhVien.tempateID = 799;
        vinhVien.minuteExist = -1;
        Kiem.dung("Xich Cuoc vinh vien -> cai tao duoc", real.CaiTaoCaiTrang.caiTaoDuoc(vinhVien));
        final real.Item coHan = new real.Item();
        coHan.tempateID = 799;
        coHan.minuteExist = 10080;
        Kiem.sai("Xich Cuoc CO HAN -> khong cai tao duoc", real.CaiTaoCaiTrang.caiTaoDuoc(coHan));
        final real.Item khacNhom = new real.Item();
        khacNhom.tempateID = 736;
        khacNhom.minuteExist = -1;
        Kiem.sai("Son Tinh goc (736) vinh vien -> van khong cai tao duoc", real.CaiTaoCaiTrang.caiTaoDuoc(khacNhom));
        Kiem.sai("null -> khong cai tao duoc, khong nem loi", real.CaiTaoCaiTrang.caiTaoDuoc(null));
        Kiem.bang("ti le thanh cong 70%", Integer.valueOf(70), Integer.valueOf(real.CaiTaoCaiTrang.TI_LE_THANH_CONG));
    }

    private static void kiemQuayPhanTram() {
        final Random r = new Random(20260902L);
        int thap = 0;
        int cao = 0;
        boolean ngoaiBien = false;
        final int lan = 20000;
        for (int i = 0; i < lan; i++) {
            final int n = real.CaiTaoCaiTrang.quayPhanTramTinhHoa(r);
            if (n < 1 || n > 10) {
                ngoaiBien = true;
            }
            if (n <= 3) {
                thap++;
            }
            if (n >= 9) {
                cao++;
            }
        }
        Kiem.sai("quay 20000 lan: luon trong 1..10", ngoaiBien);
        Kiem.dung("khoang 40% ra 1-3 (do duoc 35-45%)", thap > lan * 35 / 100 && thap < lan * 45 / 100);
        Kiem.dung("khoang 10% ra 9-10 (do duoc 7-13%)", cao > lan * 7 / 100 && cao < lan * 13 / 100);
    }

    private static void kiemTenHienThi() {
        // Anh xa ten theo gioi tinh cua chinh mon do (giu tu ban truoc).
        Kiem.bang("797 Nghe Thuong (nu)", "Mị Nương Công Chúa", real.Item.tenTrangPhucSauTinhHoa(797));
        Kiem.bang("798 Ngoc Nu (nu)", "Mị Nương Công Chúa", real.Item.tenTrangPhucSauTinhHoa(798));
        Kiem.bang("799 Xich Cuoc (nam)", "Giáp Sơn Tinh Vương", real.Item.tenTrangPhucSauTinhHoa(799));
        Kiem.bang("800 Kim Dong (nam)", "Giáp Sơn Tinh Vương", real.Item.tenTrangPhucSauTinhHoa(800));
        Kiem.bang("801 Loi Cong (nam)", "Giáp Sơn Tinh Vương", real.Item.tenTrangPhucSauTinhHoa(801));
        Kiem.bang("723 Hac Lang (nam)", "Giáp Sơn Tinh Vương", real.Item.tenTrangPhucSauTinhHoa(723));
        Kiem.bang("724 Than Nu (nu)", "Mị Nương Công Chúa", real.Item.tenTrangPhucSauTinhHoa(724));
        Kiem.bang("725 Bach Mi (nam)", "Giáp Sơn Tinh Vương", real.Item.tenTrangPhucSauTinhHoa(725));
        Kiem.bang("726 Thuy Vu Hong Sam (nu)", "Mị Nương Công Chúa", real.Item.tenTrangPhucSauTinhHoa(726));
        Kiem.bang("756 Lac Long Quan (nam)", "Giáp Sơn Tinh Vương", real.Item.tenTrangPhucSauTinhHoa(756));
        Kiem.bang("757 Au Co (nu)", "Mị Nương Công Chúa", real.Item.tenTrangPhucSauTinhHoa(757));
        Kiem.dung("id 1 -> null", real.Item.tenTrangPhucSauTinhHoa(1) == null);

        // Ten hien thi: doi DONG DAU, giu mo ta phia sau. Cot name that cua 799 co dang nay.
        final String goc = "Áo Xích Cước\n- Tăng 50% băng ,sét ,độc ,lửa lan\n- Quyền năng ma thuật:\n. Tỷ lệ xh: 5%";
        Kiem.bang("CaiTao = 0 -> giu nguyen ten goc", goc, real.Item.tenHienThiSauCaiTao(goc, 799, 0));
        Kiem.bang("CaiTao = 3 -> doi ngay tu 1% (khong doi toi 10 nua), giu mo ta",
                "Giáp Sơn Tinh Vương\n- Tăng 50% băng ,sét ,độc ,lửa lan\n- Quyền năng ma thuật:\n. Tỷ lệ xh: 5%",
                real.Item.tenHienThiSauCaiTao(goc, 799, 3));
        Kiem.bang("CaiTao = 10 -> nhu 3, cung ten do", real.Item.tenHienThiSauCaiTao(goc, 799, 3),
                real.Item.tenHienThiSauCaiTao(goc, 799, 10));
        Kiem.bang("ten khong co mo ta -> chi con ten moi", "Mị Nương Công Chúa",
                real.Item.tenHienThiSauCaiTao("Áo Nghê Thường", 797, 5));
        Kiem.bang("id ngoai nhom -> giu nguyen du CaiTao > 0", "Sơn Tinh\nTăng 50%",
                real.Item.tenHienThiSauCaiTao("Sơn Tinh\nTăng 50%", 736, 5));
        Kiem.dung("tenGoc null -> null, khong nem loi", real.Item.tenHienThiSauCaiTao(null, 799, 5) == null);
        Kiem.bang("tenNgan lay dong dau", "Áo Xích Cước", real.CaiTaoCaiTrang.tenNgan(goc));
        Kiem.bang("tenNgan chuoi mot dong", "Áo Nghê Thường", real.CaiTaoCaiTrang.tenNgan("Áo Nghê Thường "));
        Kiem.bang("tenNgan null -> rong", "", real.CaiTaoCaiTrang.tenNgan(null));
    }

    private static void kiemMap() {
        final String map = doc("src/real/Map.java");
        Kiem.dung("doc duoc src/real/Map.java", !map.isEmpty());
        if (map.isEmpty()) {
            return;
        }
        // Duong "tinh hoa bao dam" da bo hoan toan.
        Kiem.sai("khong con soLuongTinhHoaCanThiet", map.contains("soLuongTinhHoaCanThiet"));
        Kiem.sai("khong con showTrangPhucTinhHoaList", map.contains("showTrangPhucTinhHoaList"));
        Kiem.sai("khong con nhanh type == 8", map.contains("(type == 8)"));
        Kiem.sai("khong con nhanh idpopup == 126", map.contains("idpopup == 126"));
        // validItemIDs con dung cho Kho Manh / Bo Suu Tap (848-871, 903-904) - chi kiem mang RIENG cua cai tao.
        Kiem.sai("khong con mang highTierItems / normalTierItems (luat da gom ve CaiTaoCaiTrang)",
                map.contains("highTierItems") || map.contains("normalTierItems"));
        Kiem.sai("khong con danh sach 11 id cai tao viet tay trong Map",
                map.contains("{797, 798, 799, 800, 801, 723, 724, 725, 726,756,757}")
                || map.contains("{797, 798, 799, 800, 801, 723, 724, 725, 726, 756, 757}"));

        Kiem.dung("showCaiTaoMaterialList dung CaiTaoCaiTrang.locTrangPhuc",
                thanHam(map, "public void showCaiTaoMaterialList(").contains("CaiTaoCaiTrang.locTrangPhuc(player)"));

        final String type5 = doan(map, "else if (type == 5)", "else if (type == 6)");
        Kiem.dung("tim thay nhanh type == 5", !type5.isEmpty());
        Kiem.dung("type == 5 dung CaiTaoCaiTrang.locTrangPhuc", type5.contains("CaiTaoCaiTrang.locTrangPhuc(player)"));
        Kiem.dung("type == 5 kiem diem bang diemHienCo (dung loai diem theo nhom)", type5.contains("CaiTaoCaiTrang.diemHienCo("));
        Kiem.sai("type == 5 KHONG con doi luong", type5.contains("getLuong()") || type5.contains("lượng"));
        Kiem.sai("type == 5 KHONG con doc diemNapVip truc tiep", type5.contains("diemNapVip"));
        Kiem.dung("type == 5 mo popup 125", type5.contains("createMsgPopUp(shopIndex, 125,"));
        Kiem.sai("type == 5 chua tru diem, chua doi CaiTao", type5.contains("truDiem(") || type5.contains(".CaiTao ="));

        final String popup = doan(map, "if (idpopup == 125)", "else if (idpopup == 124)");
        Kiem.dung("tim thay nhanh idpopup == 125", !popup.isEmpty());
        Kiem.dung("popup 125 dung CaiTaoCaiTrang.locTrangPhuc (cung ham voi type == 5)",
                popup.contains("CaiTaoCaiTrang.locTrangPhuc(player)"));
        Kiem.dung("popup 125 chan idActor ngoai bien va ghi nghi van", popup.contains("\"haccaitaocaitrang\""));
        Kiem.dung("popup 125 kiem lai diem truoc khi tru", popup.contains("CaiTaoCaiTrang.diemHienCo("));
        final int viTriTru = popup.indexOf("CaiTaoCaiTrang.truDiem(");
        final int viTriQuay = popup.indexOf("TI_LE_THANH_CONG");
        Kiem.dung("popup 125 tru diem qua truDiem", viTriTru >= 0);
        Kiem.dung("popup 125 tru diem TRUOC khi quay (that bai cung mat diem)", viTriTru >= 0 && viTriQuay > viTriTru);
        Kiem.dung("popup 125 quay % bang quayPhanTramTinhHoa", popup.contains("CaiTaoCaiTrang.quayPhanTramTinhHoa("));
        Kiem.sai("popup 125 KHONG con subLuong / doi luong", popup.contains("subLuong(") || popup.contains("getLuong()"));
        Kiem.sai("popup 125 KHONG con doc/tru diemNapVip truc tiep", popup.contains("diemNapVip"));
        Kiem.dung("popup 125: bo dang mac thi tinh lai chi so", popup.contains("laBoDangMac(") && popup.contains("player.calculateAttrib();"));
        Kiem.dung("popup 125: bo dang mac thi gui lai hinh cho chinh minh va nguoi xung quanh",
                popup.contains("createMainCharInfoMessage(player)")
                && popup.contains("sendToNearPlayer(MessageCreator.createCharWearingMessage(player, player))"));
        Kiem.dung("popup 125 ghi log 'caitaocaitrang' (nam trong LocLogVatPham.GIU)", popup.contains("\"caitaocaitrang\""));
        Kiem.dung("log ghi ca % cu va ket qua", popup.contains("caiTaoCu") && popup.contains("ketQua"));
    }

    private static void kiemRealController() {
        final String rc = doc("src/real/RealController.java");
        Kiem.dung("doc duoc src/real/RealController.java", !rc.isEmpty());
        if (rc.isEmpty()) {
            return;
        }
        Kiem.sai("khong con submenu 'Tinh hoa bảo đảm'", rc.contains("Tinh hoa bảo đảm"));
        Kiem.sai("khong con doi 500 luong o menu", rc.contains("Bạn cần 500 lượng để cải tạo cải trang"));
        Kiem.sai("khong con showTrangPhucTinhHoaList", rc.contains("showTrangPhucTinhHoaList"));
        Kiem.sai("khong con mang validItemIDs (luat da gom ve CaiTaoCaiTrang)", rc.contains("validItemIDs"));
        final String case10 = doan(rc, "// cải tạo cải trang\n                        case 10:", "case 11:");
        Kiem.dung("case 10 mo thang danh sach qua player.map.showCaiTaoMaterialList",
                case10.contains("player.map.showCaiTaoMaterialList(player);"));

        final String menuNpc = doc("src/real/cmd/RequestNPCInfoHandler.java");
        Kiem.dung("menu NPC -8 van co muc \"Cải Tạo Cải Trang\"", menuNpc.contains("\"Cải Tạo Cải Trang\""));
        Kiem.sai("menu NPC -8 khong co muc rieng \"Tinh Hoa Trang Phục\"", menuNpc.contains("\"Tinh Hoa Trang Phục\""));
    }

    private static void kiemCharChiSo() {
        final String cs = doc("src/real/CharChiSo.java");
        Kiem.dung("doc duoc src/real/CharChiSo.java", !cs.isEmpty());
        if (cs.isEmpty()) {
            return;
        }
        final int soCho = dem(cs, "isCaiTaoMiNuong() || self.wModel.isCaiTaoSonTinh()");
        Kiem.bang("Tinh Hoa chi con MOT cho cong chi so (HP), khong con cho cong tan cong",
                Integer.valueOf(1), Integer.valueOf(soCho));
        Kiem.dung("cong vao maxhp", cs.contains("self.maxhp += (int) (self.maxhp * (percentCaiTao / 100.0));"));
        Kiem.sai("KHONG con cong MP", cs.contains("mpBonus"));
        Kiem.sai("KHONG con cong tan cong", cs.contains("dameBonus"));
        Kiem.sai("KHONG con println 'Cải tạo cải trang' moi lan tinh chi so", cs.contains("println(\"Cải tạo cải trang"));
    }

    private static void kiemModelWearingVaLog() {
        final String mw = doc("src/data/ModelWearing.java");
        Kiem.dung("doc duoc src/data/ModelWearing.java", !mw.isEmpty());
        // Hinh doi ngay khi co % (CaiTao > 0) - khop voi viec ten cung doi ngay tu 1%.
        Kiem.bang("isCaiTaoSonTinh/isCaiTaoMiNuong deu xet CaiTao > 0 (hai cho)",
                Integer.valueOf(2), Integer.valueOf(dem(mw, "this.itemModel.CaiTao > 0")));

        Kiem.sai("'caitaocaitrang' duoc GIU trong LocLogVatPham (khong bi loc)",
                data.LocLogVatPham.boQua("caitaocaitrang"));
    }

    /** Doan van ban tu chuoi bat dau toi chuoi ket thuc (khong gom chuoi ket thuc), hoac rong. */
    private static String doan(final String ma, final String tu, final String den) {
        final int bd = ma.indexOf(tu);
        if (bd < 0) {
            return "";
        }
        final int kt = ma.indexOf(den, bd);
        return kt < 0 ? ma.substring(bd) : ma.substring(bd, kt);
    }

    /** Than mot method: tu chu ky toi dau dong ket thuc "    }" cung cap. */
    private static String thanHam(final String ma, final String chuKy) {
        final int bd = ma.indexOf(chuKy);
        if (bd < 0) {
            return "";
        }
        final int kt = ma.indexOf("\n    }\n", bd);
        return kt < 0 ? ma.substring(bd) : ma.substring(bd, kt);
    }

    private static int dem(final String s, final String mau) {
        int n = 0;
        for (int i = s.indexOf(mau); i >= 0; i = s.indexOf(mau, i + mau.length())) {
            n++;
        }
        return n;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
