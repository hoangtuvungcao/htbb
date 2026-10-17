package com.htbb.pokemon;

/**
 * Pet di theo (data.Pet) -> Pokemon di theo, tra theo ID ITEM PET (template cua Pet.itemPet,
 * data_item type 25/26/27). Gui xuong client bang lenh -94 POKE_PET (GoiPokemon.ghiPet).
 *
 * Nguon: cac dong loai=pet_item cua docs/pokemon/bang_thu.csv (sinh boi
 * tools/pokemon/sinh_bang_thu.py). test/KiemPetPokemon doi chieu TUNG dong CSV voi bang nay.
 *
 * "Cap" cua pet nuoi (Cu/Rong/Yeu tinh/Doi/Dai bang cap 1..5) KHONG phai mot truong rieng:
 * moi cap la mot item template khac (Pet.capPet() = atb[0] cua template, nang cap
 * Pet.doUpdrage thay itemPet bang template idItemUpLevel). Vi vay tien hoa theo cap
 * (vd Zubat cap 1-2, Golbat 3-4, Crobat 5) da nam san trong id item - khong can suy ra.
 * Pet.itemPet.level (trang thai 0..2, Pet.doTienHoa) chi doi hieu ung, khong doi loai.
 *
 * KHONG nham voi id hinh pet (Pet.idImg / PetTemplate, anh map/pet/) - bang nay theo id item.
 * Lop THUAN.
 */
public final class PetPokemon {

    /** {idItem, dex, shiny} - xep tang theo idItem. */
    static final int[][] BANG = {
        {681, 77, 0},   // Ponyta lua
        {682, 78, 0},   // Rapidash lua
        {683, 78, 1},   // Rapidash lua Shiny
        {684, 163, 0},  // Trung Hoothoot
        {685, 147, 0},  // Trung Dratini
        {686, 173, 0},  // Trung Cleffa
        {687, 41, 0},   // Trung Zubat
        {688, 627, 0},  // Trung Rufflet
        {689, 163, 0},  // Hoothoot cap 1
        {690, 163, 0},  // Hoothoot cap 2
        {691, 164, 0},  // Noctowl cap 3
        {692, 164, 0},  // Noctowl cap 4
        {693, 164, 1},  // Noctowl Shiny cap 5
        {694, 147, 0},  // Dratini cap 1
        {695, 147, 0},  // Dratini cap 2
        {696, 148, 0},  // Dragonair cap 3
        {697, 148, 0},  // Dragonair cap 4
        {698, 149, 0},  // Dragonite cap 5
        {699, 173, 0},  // Cleffa cap 1
        {700, 35, 0},   // Clefairy cap 2
        {701, 35, 0},   // Clefairy cap 3
        {702, 36, 0},   // Clefable cap 4
        {703, 36, 1},   // Clefable Shiny cap 5
        {704, 41, 0},   // Zubat cap 1
        {705, 41, 0},   // Zubat cap 2
        {706, 42, 0},   // Golbat cap 3
        {707, 42, 0},   // Golbat cap 4
        {708, 169, 0},  // Crobat cap 5
        {709, 627, 0},  // Rufflet cap 1
        {710, 627, 0},  // Rufflet cap 2
        {711, 628, 0},  // Braviary cap 3
        {712, 628, 0},  // Braviary cap 4
        {713, 628, 1},  // Braviary Shiny cap 5
        {727, 417, 0},  // Pachirisu
        {728, 225, 0},  // Delibird
        {790, 100, 1},  // Voltorb vang
        {802, 403, 1},  // Shinx vang
        {803, 667, 0},  // Litleo
        {804, 58, 0},   // Growlithe
        {832, 585, 0},  // Deerling
        {873, 399, 0},  // Bidoof
    };

    private PetPokemon() {
    }

    public static int soDong() {
        return BANG.length;
    }

    private static int[] dong(final int idItem) {
        for (final int[] d : BANG) {
            if (d[0] == idItem) {
                return d;
            }
        }
        return null;
    }

    /** Dex Pokemon cho item pet {@code idItem}, 0 neu khong co trong bang. */
    public static int dex(final int idItem) {
        final int[] d = dong(idItem);
        return d != null ? d[1] : 0;
    }

    public static boolean shiny(final int idItem) {
        final int[] d = dong(idItem);
        return d != null && d[2] != 0;
    }
}
