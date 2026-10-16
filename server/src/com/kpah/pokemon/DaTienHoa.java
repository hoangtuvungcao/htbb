package com.kpah.pokemon;

/**
 * 8 vat pham da tien hoa cho nhanh Eevee (viec I2, KE_HOACH_CHUYEN_DOI.md).
 *
 * Id item 960..967 trong data_item (sql/pokemon_vat_pham_eevee.sql). Truoc khi chon
 * da grep src/, sql/ va docs/danh_muc_item.md: id lon nhat dang dung la 954, khong
 * cho nao trong src/ nhac toi 955..967. Doi id thi sua CA file SQL do.
 *
 * Lop THUAN, kiem thu o test/KiemPokemonServer.java.
 */
public final class DaTienHoa {

    public static final int ID_DAU = 960;

    /** ID_ITEM[i] tien hoa Eevee thanh DEX[i]. */
    public static final int[] ID_ITEM = {960, 961, 962, 963, 964, 965, 966, 967};

    public static final int[] DEX = {134, 135, 136, 196, 197, 470, 471, 700};

    /** Ten hien thi (khop cot name trong sql/pokemon_vat_pham_eevee.sql). */
    public static final String[] TEN = {
        "Đá Nước", "Đá Sấm", "Đá Lửa", "Đá Mặt Trời",
        "Đá Mặt Trăng", "Đá Lá", "Đá Băng", "Ruy Băng Tiên"
    };

    public static final String THONG_BAO_KHONG_DUNG_DUOC = "Chỉ Eevee từ cấp 25 mới dùng được";

    private DaTienHoa() {
    }

    public static boolean laDa(final int idItem) {
        return dexTheoDa(idItem) != LoaiPokemon.KHONG;
    }

    /** Loai Eevee tien hoa thanh khi dung da {@code idItem}, KHONG neu khong phai da. */
    public static int dexTheoDa(final int idItem) {
        for (int i = 0; i < ID_ITEM.length; i++) {
            if (ID_ITEM[i] == idItem) {
                return DEX[i];
            }
        }
        return LoaiPokemon.KHONG;
    }

    /** Nhan vat dang la {@code dex} o cap {@code capDo} co duoc dung da khong. */
    public static boolean duocDung(final int dex, final int capDo) {
        return dex == LoaiPokemon.EEVEE && capDo >= LoaiPokemon.CAP_NHANH_EEVEE;
    }

    /**
     * Ket qua dung da: loai moi, hoac KHONG neu khong phai da / khong du dieu kien
     * (khi do KHONG duoc tru vat pham).
     */
    public static int ketQuaDung(final int dex, final int capDo, final int idItem) {
        if (!duocDung(dex, capDo)) {
            return LoaiPokemon.KHONG;
        }
        return dexTheoDa(idItem);
    }
}
