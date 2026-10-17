package game.pokemon;

import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Ten game va thuong hieu - ban J2ME cua ClientUnity ThuongHieu.cs (viec G7.E1/E2, muc 15).
 * Ban server: KPAH src/com/kpah/pokemon/ThuongHieu.java. Doi ten game: sua ca ba noi.
 * Man tai / chon may chu / dang nhap: ve tieu de Pokemon (ten game + 5 chan dung khoi dau)
 * thay cho logo KPAH.
 */
public final class ThuongHieu {
    public static final String TEN_GAME = "Huyền Thoại Bảo Bối";
    /** Ten game viet hoa cho bigFont (font nay chi co chu hoa, ke ca chu hoa co dau). */
    public static final String TEN_GAME_HOA = "HUYỀN THOẠI BẢO BỐI";
    public static final String TEN_VIET_TAT = "HTBB";
    public static final String TEN_GAME_CU = "Khí Phách Anh Hùng";
    public static final String GIOI_THIEU = "Game Pokémon fan phi thương mại";
    public static final String GIOI_THIEU_2 = "Pokémon (c) Nintendo / Game Freak";
    public static final int SO_KHOI_DAU = 5;

    private ThuongHieu() {
    }

    public static String tenGame() {
        return PokeBody.CHE_DO_POKEMON ? TEN_GAME : TEN_GAME_CU;
    }

    /**
     * Ve tieu de Pokemon, tam ngang xTam, dong ten game o yTam, hang chan dung ngay duoi.
     * rongToiDa: be ngang man hinh (de thu nho hang chan dung tren may 128/176 px).
     * Tra ve false (de ve logo cu) khi tat che do Pokemon hoac chua co gi de ve.
     */
    public static boolean veTieuDe(Graphics g, int xTam, int yTam, int rongToiDa) {
        return veTieuDe(g, xTam, yTam, rongToiDa, 100000);
    }

    /** Nhu tren; dong gioi thieu nao vuot qua yDay (dinh khung ben duoi) thi bo. */
    public static boolean veTieuDe(Graphics g, int xTam, int yTam, int rongToiDa, int yDay) {
        if (!PokeBody.CHE_DO_POKEMON) {
            return false;
        }
        boolean daVe = false;
        int khoang = 38;
        int canh = 32;
        if (rongToiDa > 0 && khoang * SO_KHOI_DAU > rongToiDa - 6) {
            khoang = (rongToiDa - 6) / SO_KHOI_DAU;
            canh = khoang - 4;
        }
        int x0 = xTam - (SO_KHOI_DAU - 1) * khoang / 2;
        int yChanDung = yTam + 16 + canh / 2;
        for (int i = 0; i < SO_KHOI_DAU; i++) {
            int dex = LoaiPokemonClient.khoiDau(i);
            if (dex > 0 && PokeBody.paintChanDung(g, dex, false, x0 + i * khoang, yChanDung, canh)) {
                daVe = true;
            }
        }
        try {
            Font chu = Font.bigFont;
            String ten = TEN_GAME_HOA;
            if (chu == null || rongToiDa > 0 && chu.getWidth(ten) > rongToiDa - 4) {
                chu = Font.borderFont != null ? Font.borderFont : Font.normalFont[0];
                ten = TEN_GAME;
            }
            if (chu != null) {
                chu.drawString(g, ten, xTam, yTam - 8, Font.CENTER);
                daVe = true;
            }
            if (Font.normalFont[0] != null) {
                int y = yChanDung + canh / 2 + 2;
                if (y + 13 <= yDay) {
                    Font.normalFont[0].drawString(g, GIOI_THIEU, xTam, y, Font.CENTER);
                }
                if (y + 27 <= yDay) {
                    Font.normalFont[0].drawString(g, GIOI_THIEU_2, xTam, y + 14, Font.CENTER);
                }
            }
        } catch (Throwable e) {
            // font chua nap (man tai dau tien): chi co chan dung cung du
        }
        return daVe;
    }
}
