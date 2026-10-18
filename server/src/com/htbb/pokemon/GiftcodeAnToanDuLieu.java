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
    /** Trang bi catalog (noi_dung_trang_bi.csv): phat duoc khi cap mon <= cap nguoi nhan. */
    public static final int[] TRANG_BI = {1, 7, 19, 23, 27, 33, 37, 41, 45, 49, 53, 59, 63, 67, 71, 75, 79, 81, 83, 85, 86, 88, 90, 92, 93, 95, 97, 99, 100, 102, 104, 106, 107, 109, 111, 113, 114, 116, 118, 120, 122, 124, 126, 128, 130, 132, 134, 136, 138, 140, 142, 144, 146, 148, 150, 152, 154, 156, 158, 160, 163, 164, 166, 168, 170, 172, 175, 177, 179, 181, 183, 185, 187, 189, 191, 193, 195, 197, 199, 201, 203, 205, 207, 209, 211, 213, 268, 272, 467, 471, 475, 479, 483, 487, 491, 493, 495, 497, 499, 501, 503, 505, 569, 571, 584, 585, 587, 589, 591, 593, 599, 601, 603, 605, 607, 641, 646, 649, 652, 656, 660, 664, 668, 1001, 1002, 1003, 1004, 1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018, 1019, 1021, 1022, 1023, 1024, 1025, 1026, 1027, 1028, 1029, 1030, 1031, 1032, 1033};
    public static final int NGOC_TOI_DA = 268;
    /** Ngoc ban bang luong / da nghi huu: khong phat. */
    public static final int[] NGOC_CAM = {106, 107, 113, 114, 120, 121, 127, 128, 134, 135, 149, 150, 151, 152, 153, 154, 175, 176, 177, 227, 228, 234, 269};

    private GiftcodeAnToanDuLieu() {
    }
}
