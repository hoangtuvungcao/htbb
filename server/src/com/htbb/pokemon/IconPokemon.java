package com.htbb.pokemon;

/**
 * Dai anh icon Pokemon: id anh 14000+n = map/icon_pokemon/n.png (sinh boi
 * tools/pokemon/sinh_icon_pokemon.py - nguon DUY NHAT cua danh sach icon).
 *
 * Hai cach mot icon PMD toi tay nguoi choi:
 *  - Cot DB (data_item.xstart): sql/pokemon_icon_vat_pham.sql dat thang 14000+n.
 *  - Dai anh KHONG co cot DB (client tu cong 5500/6500/7500/8000/8200 vao chi so 1 byte):
 *    {@link #doiAnh} doi chi so cu -> n theo bang sinh IconPokemonBang, DataGame.sendByteImg
 *    goi khi bat che do Pokemon. Tat che do thi van ra hinh goc cu.
 *
 * Dai rieng vi nIconItem = 866 bi khoa (test KiemSkinLinhThu) va icon cu dung chung giua
 * nhieu vat pham. Truoc 14000 la dai skin linh thu (13000..13005), sendByteImg khong co dai
 * nao khac tu 12100 tro len (da grep 17/10/2026).
 */
public final class IconPokemon {

    public static final int ID_DAU = 14000;

    /** Toi da so icon trong dai (14000..14999); idImg la short nen con xa gioi han. */
    public static final int SO_TOI_DA = 1000;

    public static final String THU_MUC = "map/icon_pokemon/";

    private IconPokemon() {
    }

    public static boolean thuocDai(final int idImg) {
        return idImg >= ID_DAU && idImg < ID_DAU + SO_TOI_DA;
    }

    /**
     * Ham THUAN: id anh cu thuoc dai thuoc/ngoc/thu/vat pham nhiem vu/trai cay -> so thu tu n
     * cua icon PMD thay the, -1 neu khong doi (id ngoai cac dai do hoac o chua co icon).
     */
    public static int doiAnh(final int idImg) {
        int n = tra(IconPokemonBang.THUOC, IconPokemonBang.THUOC_DAU, idImg);
        if (n < 0) {
            n = tra(IconPokemonBang.NGOC, IconPokemonBang.NGOC_DAU, idImg);
        }
        if (n < 0) {
            n = tra(IconPokemonBang.THU, IconPokemonBang.THU_DAU, idImg);
        }
        if (n < 0) {
            n = tra(IconPokemonBang.NHIEM_VU, IconPokemonBang.NHIEM_VU_DAU, idImg);
        }
        if (n < 0) {
            n = tra(IconPokemonBang.TRAI, IconPokemonBang.TRAI_DAU, idImg);
        }
        return n;
    }

    private static int tra(final short[] bang, final int dau, final int idImg) {
        final int k = idImg - dau;
        return k >= 0 && k < bang.length ? bang[k] : -1;
    }
}
