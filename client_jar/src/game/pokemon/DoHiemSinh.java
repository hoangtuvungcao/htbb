package game.pokemon;

/**
 * SINH TU tools/cauhinh/do_hiem.json boi tools/cauhinh/sinh_do_hiem.py - KHONG SUA TAY.
 * Bang mau do hiem chung (MOT nguon voi may chu va web). Ve qua token chu de ChuDe.mauHiem / nenHiem / sangHiem,
 * khung o do KieuO.mauHiem, phu kien PhuKien. J2ME: chi mang int / String.
 */
public final class DoHiemSinh {

    private DoHiemSinh() {
    }

    public static final int THUONG = 0;
    public static final int HIEM = 1;
    public static final int QUY = 2;
    public static final int SU_THI = 3;
    public static final int HUYEN_THOAI = 4;
    public static final int TRANG_TRI = 5;
    public static final int SO_BAC = 6;

    public static final String[] TEN = {"Thường", "Hiếm", "Quý", "Sử thi", "Huyền thoại", "Trang trí"};
    /** chu / vien tren nen sang */
    public static final int[] MAU = {0x5F6B7A, 0x23803A, 0x2366C0, 0x7B45D6, 0xB95A0E, 0x8A6A00};
    /** nen nhat cung tong */
    public static final int[] NHAT = {0xEEF1F5, 0xE2F5E5, 0xE3EEFC, 0xEEE8FC, 0xFFEEDB, 0xFFF5D0};
    /** chu tren nen toi / the gioi */
    public static final int[] SANG = {0xF2F4F7, 0x7EE08A, 0x8CC8FF, 0xCDB8FF, 0xFFC46B, 0xFFE070};
    public static final boolean[] VIEN_DONG = {false, false, false, false, true, false};
    /** bang mau phu kien [bac] = {vien, toi, goc, sang, diem} */
    public static final int[][] PHU_KIEN = {
        {0x3A4250, 0x9AA2B0, 0xD8DCE4, 0xFFFFFF, 0x8FA8C8},
        {0x123A1C, 0x2A8A3E, 0x52C866, 0xC2F2C8, 0xFFFFFF},
        {0x123060, 0x2A60B8, 0x48A0F8, 0xB8E0FF, 0xFFFFFF},
        {0x30104A, 0x6A2CA0, 0xA45CE8, 0xE0C0FF, 0xFFE070},
        {0x4A2006, 0xB45A10, 0xF08A28, 0xFFD8A0, 0xFFFFFF},
        {0x4A3208, 0xC89018, 0xFFD040, 0xFFF8C8, 0xFFFFFF},
    };
    /** Item.colorName -> bac (ngoai bang = THUONG) */
    public static final int[] BAC_VAT_PHAM = {0, 3, 2, 1, 3, 5, 4, 1};
    /** do hiem danh hieu 0..3 -> bac */
    public static final int[] BAC_DANH_HIEU = {0, 1, 3, 4};
    public static final String[] TEN_BAC_TRANG_BI = {"Sơ cấp", "Đồng", "Bạc", "Vàng", "Bạch kim", "Kim cương", "Chuyên gia", "Tinh anh", "Huyền thoại", "Thần thoại", "Tối thượng"};
    public static final int[] MAU_BAC_TRANG_BI = {0x9AA0A8, 0xC88048, 0xD0D8E0, 0xF8D030, 0xE4F4FF, 0x78D8F8, 0x52C866, 0xA45CE8, 0xF08A28, 0xF84868, 0xFFFFFF};

    public static int bacVatPham(int colorName) {
        return colorName >= 0 && colorName < BAC_VAT_PHAM.length ? BAC_VAT_PHAM[colorName] : THUONG;
    }

    public static int bacDanhHieu(int doHiem) {
        return doHiem >= 0 && doHiem < BAC_DANH_HIEU.length ? BAC_DANH_HIEU[doHiem] : THUONG;
    }

    static int kep(int bac) {
        return bac >= 0 && bac < SO_BAC ? bac : THUONG;
    }
}
