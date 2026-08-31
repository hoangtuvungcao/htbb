import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Sua loi: bam "Tinh Luyện Thần Binh" o menu NPC -8 nhay THANG vao o chon
 * nguyen lieu, khong cho xem vu khi than binh dang mac (chi so tinh luyen
 * hien tai) truoc. Fix them mot buoc trung gian: menu gui shop chi co 1 item
 * (vu khi dang mac, type 7) de nguoi choi xem, bam "Tiep tuc" moi mo danh
 * sach nguyen lieu that (Map.doBuyItemShopNew, nhanh type == 7 goi
 * showTinhLuyenMaterialList()).
 *
 * Nhan tien sua mot cho LECH du lieu phat hien trong luc sua: nhanh type == 6
 * (dung de dung popup xac nhan "Ban co muon dung X de tinh luyen Y") tu loc
 * nguyen lieu rieng bang mang validMaterialIDs cung, KHONG co truong hop moc
 * 99% (locNguyenLieuTinhLuyen doi nguyen lieu phai CAO HON trang bi), va hien
 * gia SAI (500 luong, khong tru xu) so voi luc THUC THI that (idpopup 124:
 * 1 luong + 3 trieu xu) - khien shopIndex nguoi choi gui ve co the tro sang
 * MON KHAC trong danh sach dung de thuc thi. Da doi sang dung chung
 * locNguyenLieuTinhLuyen() va cung gia voi luc thuc thi.
 *
 * RealController/Map qua nang de dung truc tiep trong test (can Char/Map
 * that voi CSDL) nen quet van ban nguon de xac nhan cau truc sua dung, giong
 * cach cac Kiem* khac lam.
 */
public final class KiemTinhLuyenThanBinh {

    private static final String TEP_REAL_CONTROLLER = "src/real/RealController.java";
    private static final String TEP_MAP = "src/real/Map.java";

    private KiemTinhLuyenThanBinh() {
    }

    public static void chay() {
        Kiem.nhom("Tinh Luyện Thần Binh - phai xem vu khi truoc khi chon nguyen lieu");

        kiemRealController();
        kiemMap();
    }

    private static void kiemRealController() {
        final String noiDung = doc(TEP_REAL_CONTROLLER);
        Kiem.dung("doc duoc " + TEP_REAL_CONTROLLER, !noiDung.isEmpty());
        if (noiDung.isEmpty()) {
            return;
        }

        final int dauCase8 = noiDung.indexOf("case 8:");
        final int dauCase9 = noiDung.indexOf("case 9:");
        Kiem.dung("tim thay case 8 (Tinh Luyện Thần Binh) va case 9 sau do", dauCase8 >= 0 && dauCase9 > dauCase8);
        if (dauCase8 < 0 || dauCase9 <= dauCase8) {
            return;
        }
        final String than = noiDung.substring(dauCase8, dauCase9);

        Kiem.dung("van giu kiem tra CheckNguyenLieuTL truoc khi hien gi ca",
                than.contains("player.CheckNguyenLieuTL()"));
        Kiem.dung("gui shop 'Vũ Khí Thần Binh' voi type 7 (buoc xem truoc)",
                than.contains("\"Vũ Khí Thần Binh\", 7"));
        Kiem.dung("shop nay chi chua DUNG vu khi dang mac (player.itemVukhiThoiTrang), khong phai danh sach nguyen lieu",
                than.contains("vuKhiDangMac.addElement(player.itemVukhiThoiTrang)"));
        Kiem.dung("KHONG con tu xay danh sach nguyen lieu rieng o day (da chuyen sang Map, nhanh type == 7)",
                !than.contains("validMaterialIDs"));
        Kiem.dung("KHONG con goi thang shop 'Nguyên liệu' voi type 6 tu day nua",
                !than.contains("\"Chọn\", \"Nguyên liệu\", 6"));
    }

    private static void kiemMap() {
        final String noiDung = doc(TEP_MAP);
        Kiem.dung("doc duoc " + TEP_MAP, !noiDung.isEmpty());
        if (noiDung.isEmpty()) {
            return;
        }

        final int dauType6 = noiDung.indexOf("else if (type == 6)");
        final int dauType7 = noiDung.indexOf("else if (type == 7)");
        final int dauType10000 = noiDung.indexOf("else if (type == -10000)");
        Kiem.dung("tim thay nhanh type == 6, type == 7 (moi), va type == -10000 sau do, dung thu tu",
                dauType6 >= 0 && dauType7 > dauType6 && dauType10000 > dauType7);
        if (dauType6 < 0 || dauType7 <= dauType6 || dauType10000 <= dauType7) {
            return;
        }

        final String thanType6 = noiDung.substring(dauType6, dauType7);
        Kiem.dung("nhanh type == 6 dung chung locNguyenLieuTinhLuyen() thay vi tu loc rieng",
                thanType6.contains("locNguyenLieuTinhLuyen(player)"));
        Kiem.dung("nhanh type == 6 KHONG con tu khai bao mang validMaterialIDs rieng (nguon lech voi luc thuc thi)",
                !thanType6.contains("int[] validMaterialIDs ="));
        Kiem.dung("nhanh type == 6 kiem tra dung 1 luong (khop luc thuc thi idpopup 124)",
                thanType6.contains("getLuong() < 1"));
        Kiem.dung("nhanh type == 6 co kiem tra 3.000.000 xu (truoc day thieu hoan toan)",
                thanType6.contains("getxu() < 3000000"));

        final String thanType7 = noiDung.substring(dauType7, dauType10000);
        Kiem.dung("nhanh type == 7 (buoc xem vu khi) mo tiep danh sach nguyen lieu that",
                thanType7.contains("showTinhLuyenMaterialList(player)"));
        Kiem.dung("nhanh type == 7 kiem tra lai checkVuKhiThanBinh() (phong trang bi da doi giua luc mo 2 shop)",
                thanType7.contains("player.checkVuKhiThanBinh()"));
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
