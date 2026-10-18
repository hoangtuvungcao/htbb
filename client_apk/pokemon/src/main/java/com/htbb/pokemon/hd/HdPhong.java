package com.htbb.pokemon.hd;

import android.graphics.Canvas;
import android.graphics.Paint;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.NenAndroid;

/**
 * Cửa sổ cỡ cố định của jar (Hành trang / Trang bị / Kỹ năng... 145x144 px, Menu 170x150) quá nhỏ trên màn
 * điện thoại ngang: bản APK phóng cả cửa sổ thành thẻ lớn giữa màn (hệ số * heSo nguyên), phủ mờ nền
 * game phía sau, trượt vào khi mở. Cùng dữ liệu, cùng ô, cùng thao tác (chạm đổi ngược qua doiCham);
 * thanh phím mềm vẫn vẽ đúng chỗ (không phóng). Chữ / khung là vector nên phóng vẫn nét.
 */
public final class HdPhong {
    private static float z = 1f, dx, dy;
    private static boolean dang;
    /** giá trị khung trước (cho chạm) */
    private static volatile float zT = 1f, dxT, dyT;
    private static Object manTruoc;
    private static long lucMo;
    private static final Paint P = new Paint();
    /** thẻ to nhất vẽ trong lúc phóng ở khung trước (toạ độ màn trước phóng) - đo thật thay ước lượng */
    private static float tdX, tdY, tdW, tdH;
    private static float moiX, moiY, moiW, moiH;
    private static Object tdMan;

    static void ghiTheGoc(float x, float y, float w, float h) {
        if (!dang) return;
        if (w * h > moiW * moiH) {
            moiX = x;
            moiY = y;
            moiW = w;
            moiH = h;
        }
    }

    private HdPhong() {
    }

    /** Cao thanh phím mềm (không phóng, chạm không đổi). */
    static int caoThanhPhim() {
        return 2 * game.render.screen.Screen.deltaY + 2;
    }

    /**
     * Bắt đầu phóng cửa sổ (x, y, w, h: khung cửa sổ theo toạ độ màn jar) - gọi ngay sau khi màn vẽ nền
     * game phía sau. man = màn đang vẽ (hoạt hình mở khi đổi màn).
     */
    public static void batDau(Graphics g, Object man, int x, int y, int w, int h, float zMax) {
        batDau(g, man, x, y, w, h, zMax, false);
    }

    /**
     * neoTren = giữ mép trên thẻ tại chỗ (màn trước khi vào game: logo phía trên không bị che), phóng xuống
     * dưới; không phủ mờ nền (banner).
     */
    public static void batDau(Graphics g, Object man, int x, int y, int w, int h, float zMax, boolean neoTren) {
        if (!Hud.BAT || w <= 0 || h <= 0 || dang) return;
        if (tdMan == man && tdW > 0) {
            // dùng khung thẻ đo được ở khung trước (cửa sổ jar có thể to hơn ước lượng)
            x = (int) tdX;
            y = (int) tdY;
            w = (int) Math.ceil(tdW);
            h = (int) Math.ceil(tdH);
        } else {
            x += g.getTranslateX();
            y += g.getTranslateY();
        }
        moiW = 0;
        moiH = 0;
        int W = game.render.Canvas.w, H = game.render.Canvas.h;
        int band = caoThanhPhim();
        float top = y;
        float zz = neoTren ? Math.min((W - 24f) / w, (H - band - 6f - top) / h)
                : Math.min((W - 24f) / w, (H - band - 14f) / h);
        zz = Math.min(zz, zMax);
        int k = Math.max(1, NenAndroid.heSo);
        zz = (float) Math.floor(zz * k) / k;
        long now = System.currentTimeMillis();
        if (man != manTruoc) {
            manTruoc = man;
            lucMo = now;
        }
        // phủ mờ nền game (vẽ ở toạ độ màn, chưa phóng)
        Canvas c = g.androidCanvas();
        float t = Math.min(1f, (now - lucMo) / 180f);
        float e = 1 - (1 - t) * (1 - t) * (1 - t);
        if (!neoTren) {
            P.setColor(((int) (0x88 * e) << 24) | 0x050B18);
            c.drawRect(-g.getTranslateX() - 10, -g.getTranslateY() - 10, -g.getTranslateX() + W + 10, -g.getTranslateY() + H + 10, P);
        }
        if (zz <= 1.01f) {
            return;
        }
        // trượt lên + phóng dần khi mở
        float zHien = zz * (0.92f + 0.08f * e);
        float cx = x + w / 2f, cy = y + h / 2f;
        float tx = W / 2f, ty = (H - band) / 2f + 2 + (1 - e) * 14;
        z = zHien;
        dx = Math.round((tx - zHien * cx) * k) / (float) k;
        dy = neoTren ? Math.round((top - zHien * top) * k) / (float) k
                : Math.round((ty - zHien * cy) * k) / (float) k;
        dang = true;
        g.datPhongMan(z, dx, dy);
    }

    /** Hết màn (gọi sau currentScreen.paint): bỏ phóng, ghi lại cho đổi toạ độ chạm. */
    public static void ket(Graphics g) {
        if (dang) {
            if (moiW > 0) {
                tdX = moiX;
                tdY = moiY;
                tdW = moiW;
                tdH = moiH;
                tdMan = manTruoc;
            }
            g.datPhongMan(1f, 0, 0);
            zT = z;
            dxT = dx;
            dyT = dy;
            dang = false;
            z = 1f;
            dx = 0;
            dy = 0;
        } else {
            zT = 1f;
            dxT = 0;
            dyT = 0;
            manTruoc = null;
        }
    }

    public static boolean dangPhong() {
        return zT != 1f;
    }

    /** Hoạt hình mở đang chạy (vẽ lại mượt). */
    public static boolean dangDong() {
        return manTruoc != null && System.currentTimeMillis() - lucMo < 220;
    }

    /** Đổi điểm chạm màn thật -> toạ độ jar trong cửa sổ đã phóng (thanh phím mềm giữ nguyên). */
    public static int doiX(int x, int y) {
        if (zT == 1f || y >= game.render.Canvas.h - caoThanhPhim()) return x;
        return (int) ((x - dxT) / zT);
    }

    public static int doiY(int x, int y) {
        if (zT == 1f || y >= game.render.Canvas.h - caoThanhPhim()) return y;
        return (int) ((y - dyT) / zT);
    }
}
