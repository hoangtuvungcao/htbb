package com.htbb.pokemon;

/**
 * Thu cuoi (data.Animal, so hieu 0..20 = Animal.idImg) -> Pokemon thu cuoi.
 *
 * Nguon: du_lieu/bang_thu.csv (sinh boi tools/pokemon/sinh_bang_thu.py). Sua bang
 * o script roi cap nhat mang nay - test/KiemPokemon doi chieu hai noi voi nhau.
 * Lop THUAN. Gui xuong client bang lenh -93 POKE_CUOI (GoiPokemon).
 */
public final class ThuPokemon {

    /** DEX[idImg] - index = hang so trong data.Animal (BACH_MA=0 .. SU_TU_BANG=20). */
    static final int[] DEX = {
        78,   // 0 BACH_MA -> Rapidash
        59,   // 1 HO -> Arcanine
        128,  // 2 TRAU -> Tauros
        262,  // 3 SOI -> Mightyena
        18,   // 4 HAC -> Pidgeot
        146,  // 5 PHUONG_HOANG -> Moltres
        428,  // 6 HAC_THO -> Lopunny (shiny)
        234,  // 7 TUAN_LOC -> Stantler
        144,  // 8 PHUONG_HOANG_BANG -> Articuno
        831,  // 9 PORO -> Wooloo
        245,  // 10 LAN -> Suicune
        221,  // 11 HEO -> Piloswine
        897,  // 12 NGUA_XUONG -> Spectrier
        149,  // 13 RONG_LON -> Dragonite
        250,  // 14 PHUONG_HOANG_7MAU -> Ho-Oh
        357,  // 15 PHUONG_HOANG_MOC -> Tropius
        227,  // 16 PHUONG_HOANG_KIM -> Skarmory
        330,  // 17 PHUONG_HOANG_THO -> Flygon
        473,  // 18 HEO_BANG -> Mamoswine
        244,  // 19 SU_TU -> Entei
        896,  // 20 SU_TU_BANG -> Glastrier
    };

    static final boolean[] SHINY = new boolean[DEX.length];

    static {
        SHINY[6] = true;
    }

    private ThuPokemon() {
    }

    public static int soLoai() {
        return DEX.length;
    }

    /** Dex Pokemon cho loai thu cuoi, 0 neu ngoai bang. */
    public static int dex(final int idImgAnimal) {
        return idImgAnimal >= 0 && idImgAnimal < DEX.length ? DEX[idImgAnimal] : 0;
    }

    public static boolean shiny(final int idImgAnimal) {
        return idImgAnimal >= 0 && idImgAnimal < SHINY.length && SHINY[idImgAnimal];
    }
}
