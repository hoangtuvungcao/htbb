import real.GiaoDichThuoc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * GIAO DICH: KHONG RUT DO SAU KHI DOI PHUONG DA KHOA (bot bat_thuong / bt_gd.doi_sau_khi_khoa, 25/10/2026).
 *
 * LOI. MapMuaBan case 2 (bo / rut thuoc, goi 66/2) chi kiem player.finishTrade cua ben dang doi. Nguoi choi that tren
 * may thu: A dua 10 thuoc 1, C dua 10 thuoc 4; C khoa (66/4) + xac nhan (66/5); A rut thuoc ra (66/2/2), bo lai 1,
 * khoa + xac nhan -> giao dich hoan tat: C mat 10 thuoc 4, chi nhan 1 thuoc 1.
 *
 * SUA. GiaoDichThuoc.rutDoDuoc(minhKhoa, doiPhuongKhoa, doiPhuongXacNhan): chi rut khi CA HAI chua khoa; MapMuaBan
 * goi no trong nhanh rut (kieu 2) va bao "Đối phương đã khoá giao dịch...". Bo THEM sau khi doi phuong khoa van duoc
 * (chi co loi cho doi phuong).
 */
public final class KiemGiaoDichRutDo {

    private KiemGiaoDichRutDo() {
    }

    public static void chay() {
        Kiem.nhom("Giao dich: khong rut do sau khi doi phuong da khoa / xac nhan");
        Kiem.dung("hai ben chua khoa -> rut duoc", GiaoDichThuoc.rutDoDuoc(false, false, false));
        Kiem.sai("doi phuong da khoa -> KHONG rut", GiaoDichThuoc.rutDoDuoc(false, true, false));
        Kiem.sai("doi phuong da khoa + xac nhan -> KHONG rut", GiaoDichThuoc.rutDoDuoc(false, true, true));
        Kiem.sai("doi phuong da xac nhan -> KHONG rut", GiaoDichThuoc.rutDoDuoc(false, false, true));
        Kiem.sai("minh da khoa -> KHONG rut", GiaoDichThuoc.rutDoDuoc(true, false, false));

        final String mb = doc("src/real/MapMuaBan.java");
        final int rut = mb.indexOf("Không thể lấy vật phẩm ra.");
        final int kiem = mb.indexOf("GiaoDichThuoc.rutDoDuoc(player.finishTrade, dp.finishTrade, dp.lockTrade)");
        final int doc = mb.indexOf("player.tPotions[idpotion] = 0;");
        Kiem.dung("MapMuaBan nhanh rut goi GiaoDichThuoc.rutDoDuoc voi trang thai khoa cua doi phuong", kiem > 0);
        Kiem.dung("kiem rutDoDuoc nam TRUOC khi xoa thuoc khoi ban (tPotions = 0)", kiem > rut && kiem < doc);
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
