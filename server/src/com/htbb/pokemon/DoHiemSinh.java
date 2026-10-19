package com.htbb.pokemon;

/**
 * SINH TU tools/cauhinh/do_hiem.json boi tools/cauhinh/sinh_do_hiem.py - KHONG SUA TAY.
 * Do hiem chung (6 bac) cho vat pham, danh hieu, phu kien ngoai hinh; ten bac trang bi. Mau RGB cung nguon voi
 * client (game.pokemon.DoHiemSinh) va web (shared/utils/doHiem.gen.ts).
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

    public static final String[] MA = {"thuong", "hiem", "quy", "su_thi", "huyen_thoai", "trang_tri"};
    public static final String[] TEN = {"Thường", "Hiếm", "Quý", "Sử thi", "Huyền thoại", "Trang trí"};
    /** Mau chu / vien tren nen sang (0xRRGGBB). */
    public static final int[] MAU = {0x5F6B7A, 0x23803A, 0x2366C0, 0x7B45D6, 0xB95A0E, 0x8A6A00};
    /** Vien chay / lap lanh. */
    public static final boolean[] VIEN_DONG = {false, false, false, false, true, false};

    /** Item.colorName -> bac (ngoai bang = THUONG). */
    static final int[] BAC_VAT_PHAM = {0, 3, 2, 1, 3, 5, 4, 1};
    /** data_danh_hieu.do_hiem -> bac. */
    static final int[] BAC_DANH_HIEU = {0, 1, 3, 4};

    /** Ten 11 bac trang bi (NgoaiHinh). */
    public static final String[] TEN_BAC_TRANG_BI = {"Sơ cấp", "Đồng", "Bạc", "Vàng", "Bạch kim", "Kim cương", "Chuyên gia", "Tinh anh", "Huyền thoại", "Thần thoại", "Tối thượng"};
    public static final int[] MAU_BAC_TRANG_BI = {0x9AA0A8, 0xC88048, 0xD0D8E0, 0xF8D030, 0xE4F4FF, 0x78D8F8, 0x52C866, 0xA45CE8, 0xF08A28, 0xF84868, 0xFFFFFF};

    /** Bac cua vat pham theo colorName. */
    public static int bacVatPham(final int colorName) {
        return colorName >= 0 && colorName < BAC_VAT_PHAM.length ? BAC_VAT_PHAM[colorName] : THUONG;
    }

    /** Bac cua do hiem danh hieu (0..3). */
    public static int bacDanhHieu(final int doHiem) {
        return doHiem >= 0 && doHiem < BAC_DANH_HIEU.length ? BAC_DANH_HIEU[doHiem] : THUONG;
    }

    public static String ten(final int bac) {
        return bac >= 0 && bac < TEN.length ? TEN[bac] : TEN[THUONG];
    }

    /** Ten bac cua tung colorName (thu tu giu nguyen). */
    public static String[] tenVatPham(final int[] colorName) {
        final String[] r = new String[colorName.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = ten(bacVatPham(colorName[i]));
        }
        return r;
    }
}
