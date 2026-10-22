package com.htbb.pokemon;

/**
 * Dai anh icon DO HO MOI + TRANG TRI (0.9.8): id anh 15000+n = map/icon_do_moi/n.png (sinh boi
 * tools/do_trang_phuc/sinh.py - tu ve, CC0). data_item.xstart cua mon ho va data_trang_phuc.icon tro thang vao dai nay.
 * Dai rieng sau dai icon PMD (14000..14999, IconPokemon); sendByteImg khong co dai nao khac tu 13006 tro len
 * (da grep 22/10/2026).
 */
public final class IconDoMoi {

    public static final int ID_DAU = 15000;
    public static final int SO_TOI_DA = 1000;
    public static final String THU_MUC = "map/icon_do_moi/";

    private IconDoMoi() {
    }

    public static boolean thuocDai(final int idImg) {
        return idImg >= ID_DAU && idImg < ID_DAU + SO_TOI_DA;
    }
}
