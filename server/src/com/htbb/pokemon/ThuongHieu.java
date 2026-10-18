package com.htbb.pokemon;

/**
 * Ten game - MOT cho duy nhat phia server (viec G7.E1 trong docs/kien-truc.md
 * muc 15). Ban client: client_pc_ios/Assets/Scripts/Assembly-CSharp/ThuongHieu.cs (TEN_GAME phai giong het,
 * test/KiemHinhPokemon so hai ban). Web (HTBB-Web): app/app.config.ts htbb.tenSite + server/utils/config.ts site.name phai cung ten.
 */
public final class ThuongHieu {

    /** Ten game. Doi ten game: sua o day VA o ThuongHieu.cs. */
    public static final String TEN_GAME = "Huyền Thoại Bảo Bối";

    /** Ten viet tat. */
    public static final String TEN_VIET_TAT = "HTBB";

    private ThuongHieu() {
    }

    public static String tenGame() {
        return TEN_GAME;
    }
}
