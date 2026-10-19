package game.pokemon;
/**
 * BẢNG MẢNH giao diện NRO (0.9.7) - SINH bởi tools/giao_dien_nro/sinh_khung_nro.py, KHÔNG sửa tay.
 * Mỗi mảnh 9 số: x, y, rộng, cao trong /nro/khung.png, lề 9-slice trái, trên, phải, dưới, kiểu (0 lát, 1 hình
 * cố định); GIUA = màu thân (RGB, -1 = lát ảnh) để tô giữa bằng fillRect.
 */
public final class KhungNroBang {
    private KhungNroBang() {
    }

    public static final String TAM_KHUNG = "/nro/khung.png", TAM_ICON = "/nro/icon.png";
    public static final int SO_ICON = 62, COT_ICON = 8;

    public static final int TAM = 0;
    public static final int TAM_TOI = 1;
    public static final int HOP = 2;
    public static final int THE = 3;
    public static final int THE_TOI = 4;
    public static final int HANG = 5;
    public static final int HANG_CHON = 6;
    public static final int HANG_NHAN = 7;
    public static final int TOOLTIP = 8;
    public static final int BIEN = 9;
    public static final int NUT_CAM = 10;
    public static final int NUT_CAM_NHAN = 11;
    public static final int NUT_XANH = 12;
    public static final int NUT_XANH_NHAN = 13;
    public static final int NUT_LA = 14;
    public static final int NUT_LA_NHAN = 15;
    public static final int NUT_DO = 16;
    public static final int NUT_DO_NHAN = 17;
    public static final int NUT_GIAY = 18;
    public static final int NUT_GIAY_NHAN = 19;
    public static final int NUT_DA = 20;
    public static final int NUT_DA_NHAN = 21;
    public static final int NUT_GO = 22;
    public static final int NUT_GO_NHAN = 23;
    public static final int TAB = 24;
    public static final int TAB_CHON = 25;
    public static final int O = 26;
    public static final int O_CHON = 27;
    public static final int O_TRONG = 28;
    public static final int O_KHOA = 29;
    public static final int O_SANG = 30;
    public static final int RANH = 31;
    public static final int DAY_HP = 32;
    public static final int DAY_PP = 33;
    public static final int DAY_EXP = 34;
    public static final int DAY_LA = 35;
    public static final int CUON = 36;
    public static final int CUON_NUM = 37;
    public static final int DONG = 38;
    public static final int LUI = 39;
    public static final int CHAM = 40;
    public static final int DINH = 41;
    public static final int NUM_TRUOT = 42;
    public static final int CONG_TAC_TAT = 43;
    public static final int CONG_TAC_BAT = 44;
    public static final int MUI_PHAI = 45;

    public static final short[] MANH = {
        0, 0, 40, 40, 12, 12, 12, 12, 0,   // TAM
        41, 0, 40, 40, 12, 12, 12, 12, 0,   // TAM_TOI
        82, 0, 32, 32, 8, 8, 8, 8, 0,   // HOP
        115, 0, 16, 16, 4, 4, 4, 4, 0,   // THE
        132, 0, 16, 16, 4, 4, 4, 4, 0,   // THE_TOI
        149, 0, 16, 16, 4, 4, 4, 4, 0,   // HANG
        166, 0, 16, 16, 4, 4, 4, 4, 0,   // HANG_CHON
        0, 41, 16, 16, 4, 4, 4, 4, 0,   // HANG_NHAN
        17, 41, 14, 14, 3, 3, 3, 3, 0,   // TOOLTIP
        32, 41, 28, 18, 10, 6, 10, 6, 0,   // BIEN
        61, 41, 16, 16, 5, 5, 5, 5, 0,   // NUT_CAM
        78, 41, 16, 16, 5, 5, 5, 5, 0,   // NUT_CAM_NHAN
        95, 41, 16, 16, 5, 5, 5, 5, 0,   // NUT_XANH
        112, 41, 16, 16, 5, 5, 5, 5, 0,   // NUT_XANH_NHAN
        129, 41, 16, 16, 5, 5, 5, 5, 0,   // NUT_LA
        146, 41, 16, 16, 5, 5, 5, 5, 0,   // NUT_LA_NHAN
        163, 41, 16, 16, 5, 5, 5, 5, 0,   // NUT_DO
        0, 60, 16, 16, 5, 5, 5, 5, 0,   // NUT_DO_NHAN
        17, 60, 16, 16, 5, 5, 5, 5, 0,   // NUT_GIAY
        34, 60, 16, 16, 5, 5, 5, 5, 0,   // NUT_GIAY_NHAN
        51, 60, 16, 16, 5, 5, 5, 5, 0,   // NUT_DA
        68, 60, 16, 16, 5, 5, 5, 5, 0,   // NUT_DA_NHAN
        85, 60, 16, 16, 5, 5, 5, 5, 0,   // NUT_GO
        102, 60, 16, 16, 5, 5, 5, 5, 0,   // NUT_GO_NHAN
        119, 60, 16, 16, 5, 5, 5, 5, 0,   // TAB
        136, 60, 16, 16, 5, 5, 5, 5, 0,   // TAB_CHON
        153, 60, 16, 16, 5, 5, 5, 5, 0,   // O
        170, 60, 16, 16, 5, 5, 5, 5, 0,   // O_CHON
        0, 77, 16, 16, 5, 5, 5, 5, 0,   // O_TRONG
        17, 77, 16, 16, 5, 5, 5, 5, 0,   // O_KHOA
        34, 77, 12, 12, 3, 3, 3, 3, 0,   // O_SANG
        47, 77, 8, 8, 2, 2, 2, 2, 0,   // RANH
        56, 77, 4, 5, 0, 2, 0, 2, 0,   // DAY_HP
        61, 77, 4, 5, 0, 2, 0, 2, 0,   // DAY_PP
        66, 77, 4, 5, 0, 2, 0, 2, 0,   // DAY_EXP
        71, 77, 4, 5, 0, 2, 0, 2, 0,   // DAY_LA
        76, 77, 8, 8, 2, 2, 2, 2, 0,   // CUON
        85, 77, 8, 8, 2, 2, 2, 2, 0,   // CUON_NUM
        94, 77, 16, 16, 0, 0, 0, 0, 1,   // DONG
        111, 77, 16, 16, 0, 0, 0, 0, 1,   // LUI
        128, 77, 7, 7, 0, 0, 0, 0, 1,   // CHAM
        136, 77, 6, 6, 0, 0, 0, 0, 1,   // DINH
        143, 77, 10, 14, 0, 0, 0, 0, 1,   // NUM_TRUOT
        154, 77, 26, 14, 0, 0, 0, 0, 1,   // CONG_TAC_TAT
        0, 94, 26, 14, 0, 0, 0, 0, 1,   // CONG_TAC_BAT
        27, 94, 6, 9, 0, 0, 0, 0, 1,   // MUI_PHAI
    };

    public static final int[] GIUA = {
        0xF2E0B4, 0x2B1A10, 0xF2E0B4, 0xE9D2A0, 0x2A1C12, 0xF2E0B4, 0xFBE9B0, 0xE7CF9C, 0x2B1A10, 0xC83220, 0xE8861E, 0xB85A10, 0x2F66C4, 0x1E4296, 0x36962E, 0x1E6A22, 0xC83220, 0x8E1A0E, 0xEAD3A0, 0xDCC08A, 0x8A8A92, 0x5A5A62, 0x7A3E18, 0x4E260E, 0x8E4A1E, 0xF2E0B4, 0x2E1E14, 0x3A2616, 0x24170E, 0x4A4A52, 0xE7CF9C, 0x24160C, 0xE0301E, 0x3A78D8, 0xF2C21A, 0x4AB03A, 0x3A2414, 0xF2C54E, -1, -1, -1, -1, -1, -1, -1, -1
    };
}
