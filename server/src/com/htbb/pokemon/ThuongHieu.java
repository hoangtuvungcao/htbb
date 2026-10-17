package com.htbb.pokemon;

/**
 * Ten game - MOT cho duy nhat phia server (viec G7.E1 trong docs/pokemon/KE_HOACH_CHUYEN_DOI.md
 * muc 15). Ban client: ClientUnity/Assets/Scripts/Game/ThuongHieu.cs (TEN_GAME phai giong het,
 * test/KiemHinhPokemon so hai ban). Web (HTBB-Web): app/app.config.ts htbb.tenSite + server/utils/config.ts site.name phai cung ten.
 *
 * Lop THUAN: chi doc co CheDoPokemon.
 */
public final class ThuongHieu {

    /** Ten game khi bat che do Pokemon. Doi ten game: sua o day VA o ThuongHieu.cs. */
    public static final String TEN_GAME = "Huyền Thoại Bảo Bối";

    /** Ten viet tat. */
    public static final String TEN_VIET_TAT = "HTBB";

    /** Ten game cu (tat che do Pokemon). */
    public static final String TEN_GAME_CU = "Khí Phách Anh Hùng";

    private ThuongHieu() {
    }

    /** Ten game theo co che do Pokemon. */
    public static String tenGame() {
        return tenGame(CheDoPokemon.bat());
    }

    public static String tenGame(final boolean cheDoPokemon) {
        return cheDoPokemon ? TEN_GAME : TEN_GAME_CU;
    }
}
