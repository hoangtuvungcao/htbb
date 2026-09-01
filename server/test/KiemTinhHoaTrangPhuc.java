import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Tinh Hoa Trang Phuc (them 01/09/2026): duong bao dam de dat CaiTao=10 tren
 * mot trong 11 bo thoi trang vinh vien co san (thay cho may ngau nhien cua
 * "Cai Tao Cai Trang" da co tu truoc), tieu binh hoa hinh thu cuoi (item/gem
 * 268) theo nhom - nhom cang hiem cang can nhieu binh:
 *   nhom 1 (10 binh): Nghe Thuong(797) Ngoc Nu(798) Xich Cuoc(799) Kim Dong(800) Loi Cong(801)
 *   nhom 2 (15 binh): Bach Mi(725) Thuy Vu Hong Sam(726) Hac Lang(723) Than Nu(724)
 *   nhom 3 (20 binh): Lac Long Quan(756) Au Co(757)
 * Khi CaiTao dat 10, Item.getName() tu doi ten thanh Giap Son Tinh Vuong (mon
 * gioi tinh nam trong data_item) hoac Mi Nuong Cong Chua (mon gioi tinh nu) -
 * KHONG tao vat pham moi, chi doi ten hien thi cua chinh bo do (theo xac nhan
 * cua nguoi dung).
 *
 * real.Item.tenTrangPhucSauTinhHoa()/getName() la ham thuan (khong dong CSDL)
 * nen goi THAT duoc truc tiep. Phan con lai (Map.locTrangPhucTinhHoa/
 * soLuongTinhHoaCanThiet la package-private trong package real, khong goi
 * duoc tu goi mac dinh cua test; RealController.case 14 can Char that) thi
 * quet van ban nguon, giong cach KiemTinhLuyenThanBinh da lam.
 */
public final class KiemTinhHoaTrangPhuc {

    private KiemTinhHoaTrangPhuc() {
    }

    public static void chay() {
        Kiem.nhom("Tinh Hoa Trang Phuc - doi ten khi CaiTao dat toi da");

        kiemTenTrangPhucSauTinhHoa();
        kiemGetName();
        kiemVanBanNguonMap();
        kiemVanBanNguonRealController();
        kiemVanBanNguonRequestNPCInfoHandler();
    }

    private static void kiemTenTrangPhucSauTinhHoa() {
        // Nhom 1: 797,798 la nu (Mi Nuong), 799,800,801 la nam (Son Tinh)
        Kiem.bang("797 Nghe Thuong (nu) -> Mi Nuong Cong Chua",
                "Mị Nương Công Chúa", real.Item.tenTrangPhucSauTinhHoa(797));
        Kiem.bang("798 Ngoc Nu (nu) -> Mi Nuong Cong Chua",
                "Mị Nương Công Chúa", real.Item.tenTrangPhucSauTinhHoa(798));
        Kiem.bang("799 Xich Cuoc (nam) -> Giap Son Tinh Vuong",
                "Giáp Sơn Tinh Vương", real.Item.tenTrangPhucSauTinhHoa(799));
        Kiem.bang("800 Kim Dong (nam) -> Giap Son Tinh Vuong",
                "Giáp Sơn Tinh Vương", real.Item.tenTrangPhucSauTinhHoa(800));
        Kiem.bang("801 Loi Cong (nam) -> Giap Son Tinh Vuong",
                "Giáp Sơn Tinh Vương", real.Item.tenTrangPhucSauTinhHoa(801));

        // Nhom 2: 723 Hac Lang (nam), 724 Than Nu (nu), 725 Bach Mi (nam), 726 Thuy Vu Hong Sam (nu)
        Kiem.bang("723 Hac Lang (nam) -> Giap Son Tinh Vuong",
                "Giáp Sơn Tinh Vương", real.Item.tenTrangPhucSauTinhHoa(723));
        Kiem.bang("724 Than Nu (nu) -> Mi Nuong Cong Chua",
                "Mị Nương Công Chúa", real.Item.tenTrangPhucSauTinhHoa(724));
        Kiem.bang("725 Bach Mi (nam) -> Giap Son Tinh Vuong",
                "Giáp Sơn Tinh Vương", real.Item.tenTrangPhucSauTinhHoa(725));
        Kiem.bang("726 Thuy Vu Hong Sam (nu) -> Mi Nuong Cong Chua",
                "Mị Nương Công Chúa", real.Item.tenTrangPhucSauTinhHoa(726));

        // Nhom 3: 756 Lac Long Quan (nam), 757 Au Co (nu)
        Kiem.bang("756 Lac Long Quan (nam) -> Giap Son Tinh Vuong",
                "Giáp Sơn Tinh Vương", real.Item.tenTrangPhucSauTinhHoa(756));
        Kiem.bang("757 Au Co (nu) -> Mi Nuong Cong Chua",
                "Mị Nương Công Chúa", real.Item.tenTrangPhucSauTinhHoa(757));

        // Id khong thuoc nhom nao -> null (khong lien quan tinh hoa trang phuc)
        Kiem.dung("id 1 (khong lien quan) -> tra ve null", real.Item.tenTrangPhucSauTinhHoa(1) == null);
        Kiem.dung("id 265 (Ao Lac long quan CU, khac 756) -> tra ve null - chi 756/757 moi hop le",
                real.Item.tenTrangPhucSauTinhHoa(265) == null);
    }

    private static void kiemGetName() {
        // getName() cua Item moi tao (khong co template, khong dung Map.itemTemplates)
        // van goi duoc AN TOAN mien la CaiTao>=10 va id hop le - nhanh nay tra ve
        // SOM, truoc khi cham toi getTemplate() (can Map.itemTemplates, KHONG nap
        // duoc ngoai server that).
        final real.Item aoNgheThuong = new real.Item();
        aoNgheThuong.tempateID = 797;
        aoNgheThuong.CaiTao = 10;
        Kiem.bang("getName(): Nghe Thuong CaiTao=10 -> Mi Nuong Cong Chua",
                "Mị Nương Công Chúa", aoNgheThuong.getName());

        final real.Item aoXichCuoc = new real.Item();
        aoXichCuoc.tempateID = 799;
        aoXichCuoc.CaiTao = 10;
        Kiem.bang("getName(): Xich Cuoc CaiTao=10 -> Giap Son Tinh Vuong",
                "Giáp Sơn Tinh Vương", aoXichCuoc.getName());

        // CaiTao=11 (vuot moc, phong khi co the xay ra) van tinh la da toi da (>=10).
        final real.Item vuotMoc = new real.Item();
        vuotMoc.tempateID = 756;
        vuotMoc.CaiTao = 11;
        Kiem.bang("getName(): CaiTao=11 (vuot moc) van tra ten tinh hoa (dung >=10, khong phai ==10)",
                "Giáp Sơn Tinh Vương", vuotMoc.getName());
    }

    private static void kiemVanBanNguonMap() {
        final String map = doc("src/real/Map.java");
        Kiem.dung("doc duoc src/real/Map.java", !map.isEmpty());
        if (map.isEmpty()) {
            return;
        }
        Kiem.dung("co ham soLuongTinhHoaCanThiet", map.contains("int soLuongTinhHoaCanThiet("));
        Kiem.dung("nhom 1 (5 mon) can 10 binh",
                map.contains("case 797: case 798: case 799: case 800: case 801:\n                return 10;"));
        Kiem.dung("nhom 2 (4 mon) can 15 binh",
                map.contains("case 723: case 724: case 725: case 726:\n                return 15;"));
        Kiem.dung("nhom 3 (2 mon) can 20 binh",
                map.contains("case 756: case 757:\n                return 20;"));

        Kiem.dung("co ham loc locTrangPhucTinhHoa", map.contains("Vector<Item> locTrangPhucTinhHoa(Char player)"));
        Kiem.dung("loc chi lay do vinh vien (minuteExist <= 0)",
                map.contains("item.minuteExist <= 0 && item.CaiTao < 10 && soLuongTinhHoaCanThiet(item.tempateID) > 0"));

        Kiem.dung("co showTrangPhucTinhHoaList gui shop type 8",
                map.contains("\"Chọn\", \"Tinh Hoa Trang Phục\", 8)"));

        final int dauType8 = map.indexOf("else if (type == 8)");
        final int dauType10000 = map.indexOf("else if (type == -10000)");
        Kiem.dung("tim thay nhanh type == 8 truoc type == -10000",
                dauType8 >= 0 && dauType10000 > dauType8);
        if (dauType8 >= 0 && dauType10000 > dauType8) {
            final String thanType8 = map.substring(dauType8, dauType10000);
            Kiem.dung("nhanh type == 8 dung chung locTrangPhucTinhHoa (khong tu loc rieng)",
                    thanType8.contains("locTrangPhucTinhHoa(player)"));
            Kiem.dung("nhanh type == 8 kiem tra du binh hoa hinh (gem 268) truoc khi mo popup",
                    thanType8.contains("player.listGemitem[268] < soLuongCan && player.listGemitemLock[268] < soLuongCan"));
            Kiem.dung("nhanh type == 8 mo popup xac nhan idpopup 126",
                    thanType8.contains("createMsgPopUp(shopIndex, 126,"));
            Kiem.dung("nhanh type == 8 KHONG tru binh hay dat CaiTao (chi xac nhan, chua thuc thi)",
                    !thanType8.contains("delGem(268") && !thanType8.contains("CaiTao = 10"));
        }

        final int dauPopup126 = map.indexOf("else if (idpopup == 126)");
        Kiem.dung("tim thay nhanh idpopup == 126", dauPopup126 >= 0);
        if (dauPopup126 >= 0) {
            final int cuoiPopup126 = map.indexOf("\n            if(idpopup == 123){", dauPopup126);
            Kiem.dung("tim thay diem ket thuc nhanh idpopup 126 (truoc idpopup 123)", cuoiPopup126 > dauPopup126);
            final String thanPopup126 = cuoiPopup126 > dauPopup126 ? map.substring(dauPopup126, cuoiPopup126) : map.substring(dauPopup126);

            Kiem.dung("idpopup 126 chan bien idActor (phong hack, giong idpopup 124)",
                    thanPopup126.contains("data.NghiVanGianLan.ghiVatPham(player, \"hactinhhoatrangphuc\""));
            Kiem.dung("idpopup 126 dung chung locTrangPhucTinhHoa (khong tu loc rieng)",
                    thanPopup126.contains("locTrangPhucTinhHoa(player)"));
            Kiem.dung("idpopup 126 kiem tra lai du binh truoc khi tru (phong trang thai doi giua luc mo popup va bam xac nhan)",
                    thanPopup126.contains("player.listGemitem[268] < soLuongCan && player.listGemitemLock[268] < soLuongCan"));
            Kiem.dung("idpopup 126 uu tien tru binh KHOA truoc (dung mau hinh nhu Hoa Hinh Pet: listGemitemLock >= soLuongCan)",
                    thanPopup126.contains("if (player.listGemitemLock[268] >= soLuongCan) {")
                    && thanPopup126.contains("player.delGem(268, soLuongCan, true);")
                    && thanPopup126.contains("player.delGem(268, soLuongCan, false);"));
            Kiem.dung("idpopup 126 dat CaiTao = 10 (chac chan toi da, khong ngau nhien)",
                    thanPopup126.contains("selectedItem.CaiTao = 10;"));
            Kiem.dung("idpopup 126 co luu log rieng (tinhhoatrangphuc), khong lan voi log tinh luyen/cai tao khac",
                    thanPopup126.contains("\"tinhhoatrangphuc\""));
        }
    }

    private static void kiemVanBanNguonRealController() {
        final String rc = doc("src/real/RealController.java");
        Kiem.dung("doc duoc src/real/RealController.java", !rc.isEmpty());
        if (rc.isEmpty()) {
            return;
        }
        final int dauCase14 = rc.indexOf("case 14:");
        // "case 4:" xuat hien nhieu lan trong file switch khong lien quan khac -
        // phai tim ban NGAY SAU case 14 (case "Sap ra mat" ke tiep trong CUNG mot
        // switch), khong duoc dung indexOf tu dau file se vo tinh khop case 4 o
        // mot switch hoan toan khac truoc do.
        final int dauCase4 = dauCase14 >= 0 ? rc.indexOf("case 4:", dauCase14) : -1;
        Kiem.dung("tim thay case 14 (Tinh Hoa Trang Phuc) truoc case 4 (Sap ra mat) ke tiep",
                dauCase14 >= 0 && dauCase4 > dauCase14);
        if (dauCase14 >= 0 && dauCase4 > dauCase14) {
            final String thanCase14 = rc.substring(dauCase14, dauCase4);
            Kiem.dung("case 14 goi Map.showTrangPhucTinhHoaList qua player.map",
                    thanCase14.contains("player.map.showTrangPhucTinhHoaList(player);"));
        }
    }

    private static void kiemVanBanNguonRequestNPCInfoHandler() {
        final String menuNpc = doc("src/real/cmd/RequestNPCInfoHandler.java");
        Kiem.dung("doc duoc RequestNPCInfoHandler.java", !menuNpc.isEmpty());
        Kiem.dung("menu NPC -8 co muc \"Tinh Hoa Trang Phục\" (sau \"Cải Tạo Cải Trang\" da co san)",
                menuNpc.contains("\"Cải Tạo Cải Trang\",\"Bộ Sưu Tập\"")
                && menuNpc.contains("\"Tinh Hoa Trang Phục\"}"));
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
