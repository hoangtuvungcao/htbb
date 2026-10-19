package htbb.pc;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import javax.microedition.lcdui.Graphics;

/**
 * Thẻ trạng thái góc trái trên của màn chơi (bản PC). 0.9.7: vẽ bằng thẻ chung kiểu NRO game.pokemon.TheNguoiChoi (bảng
 * gỗ tối, chân dung trong ô khung vàng, thanh HP đỏ / PP xanh / EXP vàng pixel, hàng ví xu + lượng). Trước 0.9.7 -
 * cùng bố cục với thẻ của bản APK (client_apk/.../hd/Hud.veThe):
 * chân dung Pokémon trong vòng vàng -> cam, huy hiệu Lv, chip hệ, tên, % EXP, thanh HP có nhãn (xanh -> vàng -> đỏ)
 * + số, thanh PP + số, EXP mảnh; dưới thẻ các viên tin (PK, đếm ngược, giờ vàng) như jar. port-j2me.py thay
 * GameScr.paintMainCharInfo bằng hàm này khi bật Khung HD; huy hiệu "12+" dời xuống dưới thẻ.
 */
public final class TheTrangThai {
    static final int L = 3, T = 3, RONG = game.pokemon.TheNguoiChoi.RONG, CAO = game.pokemon.TheNguoiChoi.CAO;
    /** đáy vùng thẻ + viên tin (toạ độ giao diện) - huy hiệu 12+ / trạng thái auto đặt dưới */
    static volatile int day = T + CAO + 4;

    private TheTrangThai() {
    }

    private static final Font[] FONT = new Font[2 * 256];

    /** Font cỡ co (nhớ sẵn - deriveFont mỗi khung rất tốn). */
    private static Font f(boolean dam, float co) {
        int k = Math.max(0, Math.min(255, Math.round(co * 10))) * 2 + (dam ? 1 : 0);
        Font r = FONT[k];
        if (r == null) {
            r = PhongChu.co(dam, co);
            FONT[k] = r;
        }
        return r;
    }

    private static void chu(Graphics2D g, String s, float x, float y, Font f, int mau, boolean vien, int canh) {
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        float w = fm.stringWidth(s);
        if (canh == 2) x -= w / 2;
        else if (canh == 1) x -= w;
        if (vien) {
            g.setColor(new Color(0, 0, 0, 170));
            float d = 0.55f;
            g.drawString(s, x - d, y);
            g.drawString(s, x + d, y);
            g.drawString(s, x, y - d);
            g.drawString(s, x, y + d);
        }
        g.setColor(new Color(mau, true));
        g.drawString(s, x, y);
    }

    private static String cat(Graphics2D g, String s, Font f, float w) {
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(s) <= w) return s;
        int n = s.length();
        while (n > 1 && fm.stringWidth(s.substring(0, n) + "..") > w) n--;
        return s.substring(0, n) + "..";
    }

    /** true = đã vẽ (bỏ HUD jar). */
    public static boolean ve(Graphics g, game.model.MainChar mc) {
        if (!GiaoDienHD.bat || mc == null || g.heSo() <= 1) return false;
        game.render.Canvas.resetTrans(g);
        game.pokemon.TheNguoiChoi.ve(g, mc, L, T);
        // viên tin dưới thẻ
        int y = T + CAO + 4;
        int n = 0;
        if (mc.killer > 0) {
            vienTin(g, L, y + n * 14, "PK " + (int) mc.killer, game.pokemon.ChuDe.DO);
            n++;
        }
        if (mc.delay >= 0) {
            int[] tg = mc.getTime();
            vienTin(g, L, y + n * 14, tg[0] + " : " + tg[1], game.pokemon.ChuDe.XANH);
            n++;
        }
        String tm = mc.getGoldTime();
        if (tm != null && tm.length() > 0) {
            vienTin(g, L, y + n * 14, tm + " " + game.model.Char.goldTime, game.pokemon.ChuDe.VANG);
            n++;
        }
        day = y + n * 14 + 2;
        // 0.9.5: viên Tự động (game.pokemon.VienTrangThai) dưới các viên tin; chữ "+N EXP" dưới góc phải thẻ
        game.pokemon.VienTrangThai.datHd(L, y + n * 14, L + RONG - 4, T + CAO + 2);
        if (game.pokemon.VienTrangThai.coAuto) day += 14;   // huy hiệu 12+ xuống dưới viên Tự động
        return true;
    }

    /** "gt/max" nhớ chuỗi cũ (không tạo chuỗi mới mỗi khung khi số không đổi). */
    static final class So {
        int a = -1, b = -1;
        String s = "";
    }

    static final So hpSo = new So(), ppSo = new So();

    static String so(So c, int a, int b) {
        if (a != c.a || b != c.b) {
            c.a = a;
            c.b = b;
            c.s = a + "/" + b;
        }
        return c.s;
    }

    /** 0.9.7: bảng tối chỉ vàng + ô màu + chữ giấy da (cùng kiểu viên Tự động game.pokemon.VienTrangThai). */
    static void vienTin(Graphics g, int x, int y, String s, int mau) {
        int vai = game.pokemon.ChuDe.NHAN_NHO;
        int w = game.pokemon.ChuDe.rong(s, vai) + 18;
        if (!game.pokemon.KhungNro.ve(g, game.pokemon.KhungNroBang.TOOLTIP, x, y, w, 12)) {
            game.pokemon.GiaoDien.khoi(g, x, y, w, 12, 2, 0x2B1A10);
        }
        game.pokemon.GiaoDien.khoiPixel(g, x + 4, y + 3, 6, 6, mau);
        game.pokemon.ChuDe.chuGiua(g, s, x + 13, y, 12, vai, 0xF6E6C0, 0);
    }


    /** Vị trí y huy hiệu 12+ trong màn chơi (dưới thẻ). */
    public static int y12() {
        return GiaoDienHD.bat ? day : 40;
    }
}
