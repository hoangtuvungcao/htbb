package com.kpah.pokemon;

/**
 * Dai anh icon vat pham Pokemon: id anh 14000+n = map/icon_pokemon/n.png
 * (sinh boi tools/pokemon/sinh_icon_pokemon.py, cot data_item.xstart tro vao day qua
 * sql/pokemon_icon_vat_pham.sql). Dai rieng vi nIconItem = 866 bi khoa (test KiemSkinLinhThu)
 * va icon cu dung chung giua nhieu vat pham. Truoc 14000 la dai skin linh thu (13000+).
 */
public final class IconPokemon {

    public static final int ID_DAU = 14000;

    /** Toi da so icon trong dai (14000..14099). */
    public static final int SO_TOI_DA = 100;

    public static final String THU_MUC = "map/icon_pokemon/";

    private IconPokemon() {
    }

    public static boolean thuocDai(final int idImg) {
        return idImg >= ID_DAU && idImg < ID_DAU + SO_TOI_DA;
    }
}
