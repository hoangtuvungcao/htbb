package com.htbb.pokemon;

import java.io.File;

/**
 * G7.A2: vat trang tri ban do ban Pokemon (sinh boi tools/pokemon/DoiTileMap.java) nam o
 * map/pokemon_trang_tri/ voi CUNG duong dan con nhu ban goc:
 *   map/tree/&lt;i&gt;       -> map/pokemon_trang_tri/tree/&lt;i&gt;   (cay/nha ban do, lenh -48, cung dinh dang)
 *   map/cay/&lt;j&gt;.png   -> map/pokemon_trang_tri/cay/&lt;j&gt;.png (cay trong, anh 3200+j)
 *   map/&lt;ten&gt;.png     -> map/pokemon_trang_tri/&lt;ten&gt;.png (anh dip le, Map.imgDynamic)
 * KHONG nham voi map/pokemon_tile/ (tile nen, xem DataGame.THU_MUC_TILE_POKEMON).
 */
public final class TrangTriPokemon {

    public static final String THU_MUC = "map/pokemon_trang_tri/";

    private TrangTriPokemon() {
    }

    /** Duong anh nen doc: ban Pokemon khi bat che do va tep ton tai, khong thi duong goc. */
    public static String duong(final String goc) {
        if (!CheDoPokemon.bat() || goc == null || !goc.startsWith("map/")) {
            return goc;
        }
        final String moi = THU_MUC + goc.substring("map/".length());
        return new File(moi).isFile() ? moi : goc;
    }
}
