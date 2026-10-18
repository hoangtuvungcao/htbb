package com.htbb.pokemon;

import java.io.File;

/**
 * Ban do Pokemon (ve bang tools/pokemon/VeMapMoi.java, dinh dang: docs/kien-truc.md):
 *   cMap/&lt;id&gt;        bo cuc ban do (bo tile map/pokemon_tile)
 *   cMap/&lt;bo&gt;.type   kieu di duoc cua bo tile (t, t_hang, t_thanh)
 *   map/map&lt;N&gt;       luoi quai
 *   map/tree, map/cay  vat trang tri
 * Client nhan bo cuc qua goi 12 (Map.idMapSend2Client), nen ca Unity lan J2ME dung ban cua server
 * ma khong can dong goi lai (tru map 0 luc khoi dong, xem docs/kien-truc.md).
 */
public final class BanDoPokemon {

    public static final String THU_MUC_CMAP = "cMap/";

    private BanDoPokemon() {
    }

    /**
     * Map khong nam trong danh sach gui client cua game goc (Map.java: client tu doc ban dong goi)
     * nhung phai gui bo cuc Pokemon: chi nhung map co tep.
     */
    public static short[] mapGuiThem() {
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
}
