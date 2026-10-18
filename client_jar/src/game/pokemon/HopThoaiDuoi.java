package game.pokemon;

import game.render.Canvas;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Hộp thoại NPC kiểu Pokémon GBA/DS: khung trắng viền kép ở ĐÁY màn, thẻ tên người nói, chữ hiện
 * dần (máy chữ), tam giác ▼ nhấp nháy khi hết dòng. Dùng cho lời thoại nhiệm vụ / nói chuyện NPC
 * (GameScr.addChat khi đang trò chuyện) thay bong bóng trên đầu; phím / chạm vẫn do GameScr xử lý.
 */
public final class HopThoaiDuoi {
    private static String ten;
    private static String[] dong;
    private static int tuTick;
    private static boolean bat;
    private static long hetLuc;

    private HopThoaiDuoi() {
    }

    /** Lời mới của người nói (ten có thể rỗng). */
    public static void dat(String nguoiNoi, String loi, int tickSong) {
        ten = nguoiNoi;
        // ChatPopup đếm tick (~25/giây); giữ tối thiểu 4 giây, tối đa 25 giây
        long ms = tickSong * 40L;
        hetLuc = System.currentTimeMillis() + (ms < 4000L ? 4000L : (ms > 25000L ? 25000L : ms));
        int w = rongLong();
        dong = Font.arialFont.splitFontBStrInLine(loi == null ? "" : loi, w);
        tuTick = Canvas.gameTick;
        bat = true;
    }

    public static void tat() {
        bat = false;
        dong = null;
    }

    public static boolean dangBat() {
        if (bat && System.currentTimeMillis() > hetLuc) {
            tat();
        }
        return bat && dong != null;
    }

    private static int rongLong() {
        return Canvas.w - 24;
    }

    /** Số dòng hiện một lúc (3 trên máy cao, 2 trên máy thấp 240 px). */
    private static int soDong() {
        int toiDa = Canvas.h >= 260 ? 4 : 3;
        int n = dong == null ? 1 : dong.length;
        return n < 1 ? 1 : (n > toiDa ? toiDa : n);
    }

    public static void ve(Graphics g) {
        if (!dangBat()) {
            return;
        }
        Canvas.resetTrans(g);
        int n = soDong();
        int lh = 14;
        int h = n * lh + 14;
        int y = Canvas.h - game.render.screen.Screen.deltaY - h - 2;
        int x = 3;
        int w = Canvas.w - 6;
        GiaoDien.hop(g, x, y, w, h);
        // thẻ tên người nói (trên mép trái hộp)
        if (ten != null && ten.length() > 0) {
            String t = GiaoDien.catChu(Font.borderFont, ten, w / 2);
            int wt = Font.borderFont.getWidth(t) + 14;
            GiaoDien.khoi(g, x + 6, y - 11, wt, 15, 6, GiaoDien.VIEN);
            GiaoDien.khoiDoc(g, x + 7, y - 10, wt - 2, 13, 5, GiaoDien.DO_SANG, GiaoDien.DO_TOI);
            Font.borderFont.drawString(g, t, x + 6 + wt / 2, y - 11, Font.CENTER);
        }
        // máy chữ: 2 ký tự / khung hình
        int tick = Canvas.gameTick - tuTick;
        if (tick < 0) {
            tick += 10001;
        }
        int conLai = tick * 2;
        boolean xong = true;
        int yy = y + 7;
        for (int i = 0; i < dong.length && i < n; i++) {
            String s = dong[i];
            if (conLai < s.length()) {
                s = s.substring(0, conLai < 0 ? 0 : conLai);
                xong = false;
            }
            conLai -= dong[i].length();
            Font.arialFont.drawString(g, s, x + 10, yy, Font.LEFT);
            yy += lh;
        }
        if (xong) {
            GiaoDien.muiXuong(g, x + w - 12, y + h - 9);
        }
    }
}
