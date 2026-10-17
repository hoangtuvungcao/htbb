package com.htbb.pokemon;

import java.io.File;

/**
 * Ban do VE LAI cho che do Pokemon (tools/pokemon/VeMapMoi.java, docs/pokemon/DINH_DANG_MAP.md):
 *   cMap/&lt;id&gt;            -> cMap_pokemon/&lt;id&gt;          (bo cuc moi; cung kich thuoc, cong, NPC)
 *   cMap/&lt;bo&gt;.type       -> cMap_pokemon/&lt;bo&gt;.type     (kieu di duoc cua BO TILE MOI - DungBoTileMoi)
 *   map/map&lt;N&gt;           -> map/pokemon_spawn/map&lt;N&gt;   (luoi quai da doi cho - chi vai map bien/ho)
 * Ban moi dung bo tile moi (map/pokemon_tile, chi so khac han bo cu) nen ban do va .type PHAI di cung
 * nhau: KiemMapPokemon bat buoc moi cMap/&lt;id&gt; deu co ban moi.
 * Tat che do hoac thieu tep -> duong goc (ban do game goc giu nguyen).
 * Client nhan bo cuc qua goi 12 (Map.idMapSend2Client doc qua ham nay), nen ca Unity lan J2ME dung
 * ban moi ma khong can dong goi lai (tru map 0 luc khoi dong, xem DINH_DANG_MAP.md).
 * KHONG nham voi TrangTriPokemon (anh cay/trang tri) va map/pokemon_tile (anh tile).
 */
public final class BanDoPokemon {

    public static final String THU_MUC_CMAP = "cMap_pokemon/";
    public static final String THU_MUC_QUAI = "map/pokemon_spawn/";

    private BanDoPokemon() {
    }

    /** "cMap/12" -> "cMap_pokemon/12", "cMap/t.type" -> "cMap_pokemon/t.type" khi bat che do va tep ton tai. */
    public static String duong(final String goc) {
        if (!CheDoPokemon.bat() || goc == null || !goc.startsWith("cMap/")) {
            return goc;
        }
        final String ten = goc.substring("cMap/".length());
        if (!ten.matches("\\d+") && !ten.matches("t(_hang|_thanh)?\\.type")) {
            return goc;
        }
        final String moi = THU_MUC_CMAP + ten;
        return new File(moi).isFile() ? moi : goc;
    }

    /**
     * Map co bo cuc moi nhung KHONG nam trong danh sach gui client cua game goc (Map.java: client tu
     * doc ban dong goi). Chi gui them khi bat che do va co ban moi; tat che do -> rong (giu hanh vi cu).
     */
    public static short[] mapGuiThem() {
        if (!CheDoPokemon.bat()) {
            return new short[0];
        }
        final short[] ung = {0, 122};
        int n = 0;
        for (final short id : ung) {
            if (new File(THU_MUC_CMAP + id).isFile()) {
                n++;
            }
        }
        final short[] ra = new short[n];
        n = 0;
        for (final short id : ung) {
            if (new File(THU_MUC_CMAP + id).isFile()) {
                ra[n++] = id;
            }
        }
        return ra;
    }

    /** "map/map12" ->"map/pokemon_spawn/map12" khi bat che do va tep ton tai. */
    public static String duongQuai(final String goc) {
        if (!CheDoPokemon.bat() || goc == null || !goc.startsWith("map/map")) {
            return goc;
        }
        final String moi = THU_MUC_QUAI + goc.substring("map/".length());
        return new File(moi).isFile() ? moi : goc;
    }
}
