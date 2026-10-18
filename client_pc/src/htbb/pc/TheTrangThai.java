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
 * Thẻ trạng thái HD góc trái trên của màn chơi (bản PC) - cùng bố cục với thẻ của bản APK (client_apk/.../hd/Hud.veThe):
 * chân dung Pokémon trong vòng vàng -> cam, huy hiệu Lv, chip hệ, tên, % EXP, thanh HP có nhãn (xanh -> vàng -> đỏ)
 * + số, thanh PP + số, EXP mảnh; dưới thẻ các viên tin (PK, đếm ngược, giờ vàng) như jar. port-j2me.py thay
 * GameScr.paintMainCharInfo bằng hàm này khi bật Khung HD; huy hiệu "12+" dời xuống dưới thẻ.
 */
public final class TheTrangThai {
    static final int L = 3, T = 3, RONG = 150, CAO = 46;
    /** đáy vùng thẻ + viên tin (toạ độ giao diện) - huy hiệu 12+ / trạng thái auto đặt dưới */
    static volatile int day = T + CAO + 4;

    private TheTrangThai() {
    }

    private static Font f(boolean dam, float co) {
        return PhongChu.thuong(dam).deriveFont(co);
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
        Graphics2D g2 = g.g2d();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float l = L, t = T, r = L + RONG, b = T + CAO;
        GiaoDienHD.bong(g2, l, t, r, b, 12, 1.5f);
        GiaoDienHD.tronDoc(g2, l, t, r, b, 12, 0xE0213F6E, 0xE80E1D38);
        GiaoDienHD.tronDoc(g2, l + 1, t + 1, r - 1, t + 16, 11, 0x22FFFFFF, 0x00FFFFFF);
        // sọc chéo kiểu Pokémon mờ ở mép phải thẻ
        java.awt.Shape cu = g2.getClip();
        g2.clip(GiaoDienHD.rr(l, t, r, b, 12));
        g2.setColor(new Color(255, 255, 255, 14));
        g2.setStroke(new java.awt.BasicStroke(3f));
        for (float sx = r - 46; sx < r + 10; sx += 7) g2.draw(new java.awt.geom.Line2D.Float(sx, b, sx + CAO, t));
        g2.setClip(cu);
        GiaoDienHD.vienTron(g2, l, t, r, b, 12, 0.8f, 0x60A9C8FF);
        // chân dung trong vòng vàng -> cam
        float ax = l + 23, ay = t + 22, ar = 19;
        g2.setPaint(new GradientPaint(0, ay - ar, new Color(0xFFD23A), 0, ay + ar, new Color(0xFF7A1A)));
        g2.fill(new Ellipse2D.Float(ax - ar, ay - ar, 2 * ar, 2 * ar));
        g2.setColor(new Color(0x15284A));
        g2.fill(new Ellipse2D.Float(ax - ar + 2.2f, ay - ar + 2.2f, 2 * ar - 4.4f, 2 * ar - 4.4f));
        g2.setPaint(new GradientPaint(0, ay - ar, new Color(0x3D6FB0), 0, ay + ar, new Color(0x1A3462)));
        g2.fill(new Ellipse2D.Float(ax - ar + 3, ay - ar + 3, 2 * ar - 6, 2 * ar - 6));
        java.awt.Shape cu2 = g2.getClip();
        g2.clip(new Ellipse2D.Float(ax - ar + 3, ay - ar + 3, 2 * ar - 6, 2 * ar - 6));
        try {
            boolean ok = mc.pokeDex > 0 && game.pokemon.PokeBody.paintChanDung(g, mc.pokeDex, mc.pokeShiny, (int) ax, (int) ay + 1, 30);
            if (!ok) game.render.Res.getCharPartInfo(2, mc.currentHead).paintStatic(g, (short) ax, (short) (ay + 1), 0, mc.frame);
        } catch (Throwable e) {
            // không có ảnh
        }
        g2 = g.g2d();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(cu2);
        // huy hiệu Lv (ruy băng vàng)
        String lv = "Lv " + (int) mc.level;
        Font fLv = f(true, 7.5f);
        g2.setFont(fLv);
        float wl = g2.getFontMetrics().stringWidth(lv) + 8;
        GiaoDienHD.tronDoc(g2, ax - wl / 2, ay + ar - 7, ax + wl / 2, ay + ar + 3, 5, 0xFFFFD23A, 0xFFFF8A1A);
        GiaoDienHD.vienTron(g2, ax - wl / 2, ay + ar - 7, ax + wl / 2, ay + ar + 3, 5, 0.7f, 0xFF15284A);
        chu(g2, lv, ax, ay + ar + 0.9f, fLv, 0xFF15284A, false, 2);
        if ((mc.basePointLeft > 0 || mc.skillPointLeft > 0) && System.currentTimeMillis() % 1000 < 650) {
            g2.setColor(new Color(mc.basePointLeft > 0 ? 0xFF7A1A : 0x40C8F8));
            g2.fill(new Ellipse2D.Float(ax + ar * 0.72f - 3.6f, ay - ar * 0.72f - 3.6f, 7.2f, 7.2f));
        }
        // tên + chip hệ + % EXP
        float x0 = l + 47, wB = r - x0 - 7;
        String pc = mc.getPercent();
        Font fPc = f(false, 7f);
        g2.setFont(fPc);
        float wPc = pc == null ? 0 : g2.getFontMetrics().stringWidth(pc);
        String he = null;
        int mauHe = 0;
        try {
            int dex = mc.pokeDex;
            if (dex > 0) {
                int h1 = game.pokemon.HePoke.he1(dex);
                he = game.pokemon.KieuO.tenHe(h1);
                mauHe = game.pokemon.KieuO.mauHe(h1);
            }
        } catch (Throwable e) {
            he = null;
        }
        Font fHe = f(true, 6.2f);
        float wHe = 0;
        if (he != null && he.length() > 0) {
            g2.setFont(fHe);
            wHe = g2.getFontMetrics().stringWidth(he) + 7;
        }
        Font fTen = f(true, 9f);
        String ten = cat(g2, mc.name == null ? "" : mc.name, fTen, wB - wPc - wHe - 6);
        chu(g2, ten, x0, t + 11.5f, fTen, 0xFFFFFFFF, false, 0);
        g2.setFont(fTen);
        float xHe = x0 + g2.getFontMetrics().stringWidth(ten) + 4;
        if (wHe > 0) {
            GiaoDienHD.tronDoc(g2, xHe, t + 4.5f, xHe + wHe, t + 12.5f, 4, GiaoDienHD.pha(0xFF000000 | mauHe, 0xFFFFFFFF, 0.25f), 0xFF000000 | mauHe);
            chu(g2, he, xHe + wHe / 2, t + 10.9f, fHe, 0xFFFFFFFF, true, 2);
        }
        if (pc != null) chu(g2, pc, r - 7, t + 11.5f, fPc, 0xFF9FE3FF, false, 1);
        // HP (nhãn + thanh + số)
        Font fNhan = f(true, 6f);
        float wN = 12;
        chu(g2, "HP", x0, t + 22, fNhan, 0xFFFFD23A, true, 0);
        GiaoDienHD.veThanh(g2, x0 + wN, t + 15, wB - wN, 9, mc.maxhp <= 0 ? 0 : mc.hp / (float) mc.maxhp, GiaoDienHD.mauThanh(0, mc.hp, mc.maxhp));
        chu(g2, Math.max(0, mc.hp) + "/" + mc.maxhp, x0 + wN + (wB - wN) / 2, t + 22.2f, f(true, 6.4f), 0xFFFFFFFF, true, 2);
        chu(g2, "PP", x0, t + 31.6f, fNhan, 0xFF9FD0FF, true, 0);
        GiaoDienHD.veThanh(g2, x0 + wN, t + 26, wB - wN, 7, mc.maxmp <= 0 ? 0 : mc.mp / (float) mc.maxmp, 0xFF3FA0F0);
        chu(g2, Math.max(0, mc.mp) + "/" + mc.maxmp, x0 + wN + (wB - wN) / 2, t + 31.6f, f(true, 5.6f), 0xFFFFFFFF, true, 2);
        GiaoDienHD.veThanh(g2, x0 + wN, t + 36, wB - wN, 4, mc.lvpercent / 1000f, 0xFF40C8F8);
        chu(g2, "EXP", x0, t + 40, f(true, 4.6f), 0xFF9FE3FF, false, 0);
        // viên tin dưới thẻ
        float y = b + 4;
        int n = 0;
        if (mc.killer > 0) {
            vienTin(g2, l, y + n * 14, "PK " + (int) mc.killer, 0xFFE3350D);
            n++;
        }
        if (mc.delay >= 0) {
            int[] tg = mc.getTime();
            vienTin(g2, l, y + n * 14, tg[0] + " : " + tg[1], 0xFF3D7DCA);
            n++;
        }
        String tm = mc.getGoldTime();
        if (tm != null && tm.length() > 0) {
            vienTin(g2, l, y + n * 14, tm + " " + game.model.Char.goldTime, 0xFFE8A800);
            n++;
        }
        day = (int) (y + n * 14) + 2;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(g.getColor());
        return true;
    }

    static void vienTin(Graphics2D g, float x, float y, String s, int mau) {
        Font f = f(true, 7.5f);
        g.setFont(f);
        float w = g.getFontMetrics().stringWidth(s) + 12;
        GiaoDienHD.tronDoc(g, x, y, x + w, y + 12, 6, GiaoDienHD.pha(mau, 0xFFFFFFFF, 0.2f), GiaoDienHD.pha(mau, 0xFF000000, 0.25f));
        GiaoDienHD.vienTron(g, x, y, x + w, y + 12, 6, 0.6f, 0x80000000);
        chu(g, s, x + 6, y + 8.8f, f, 0xFFFFFFFF, false, 0);
    }

    /** Vị trí y huy hiệu 12+ trong màn chơi (dưới thẻ). */
    public static int y12() {
        return GiaoDienHD.bat ? day : 40;
    }
}
