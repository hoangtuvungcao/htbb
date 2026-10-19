package com.htbb.pokemon;

/**
 * SINH RA boi tools/pokemon/sinh_giftcode_an_toan.py - KHONG SUA TAY. Ban Java cua
 * HTBB-Web/shared/data/giftcode-an-toan.json (danh sach vat pham giftcode an toan duoc phat). Dung o
 * GiftcodeAnToanLogic.kiemVatPham; test/KiemItemMoCoi.java so SHA-256 voi file JSON.
 */
public final class GiftcodeAnToanDuLieu {

    public static final String SHA256_JSON = "6a2becdbce027792b4a05fd88bb0134aca0f1cb7ed8f0fed7c76c39375ca0221";
    /** Dai id [tu, den] tieu hao / nguyen lieu / tin vat phat duoc. */
    public static final int[][] VAT_PHAM = {{714, 714}, {960, 967}, {1034, 1061}, {1201, 1260}};
    /**
     * Trang bi catalog (id_item cua data_noi_dung_trang_bi): phat duoc khi cap mon <= cap nguoi nhan. NAP TU SQL
     * (com.htbb.dulieu.DuLieuSql, tang dan) - rong cho toi khi nap; JSON giu ban du phong cho web.
     */
    public static volatile int[] TRANG_BI = new int[0];
    public static final int NGOC_TOI_DA = 268;
    /** Ngoc ban bang luong / da nghi huu: khong phat. */
    public static final int[] NGOC_CAM = {106, 107, 113, 114, 120, 121, 127, 128, 134, 135, 149, 150, 151, 152, 153, 154, 175, 176, 177, 227, 228, 234, 269};

    private GiftcodeAnToanDuLieu() {
    }
}
