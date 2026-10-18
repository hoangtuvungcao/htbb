package com.htbb.pokemon.hd;

import android.util.Log;
import javax.microedition.lcdui.Graphics;

/**
 * Kiểm tra chồng lấn (chỉ khi bật: adb shell setprop debug.htbb.kiem 1): mỗi ~1,5 s ghi ra logcat (thẻ
 * HTBBKIEM) một dòng JSON gồm màn đang hiện, cỡ màn, vùng an toàn và hình chữ nhật (toạ độ màn logic, đã
 * cắt theo vùng clip) của mọi chữ (HdChu), nút theo ngữ cảnh (HdNut) và nút HUD. Công cụ:
 * client_apk/pokemon/tools/kiem_chong_lan.py đọc logcat và báo chữ đè chữ, nút đè chữ / nút, ra ngoài màn.
 */
public final class HdKiem {
    private HdKiem() {
    }

    static boolean bat;
    private static long lucDoc;
    private static long lucGhi;
    private static final StringBuilder KHUNG = new StringBuilder(8192);
    private static final StringBuilder PHU = new StringBuilder(4096);
    private static String khungXong = "";

    static void kiemBat() {
        long now = System.currentTimeMillis();
        if (now - lucDoc < 2000) return;
        lucDoc = now;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            String v = (String) c.getMethod("get", String.class, String.class).invoke(null, "debug.htbb.kiem", "0");
            bat = "1".equals(v);
        } catch (Throwable e) {
            bat = false;
        }
    }

    static void khungMoi() {
        kiemBat();
        if (bat) KHUNG.setLength(0);
    }

    static void khungXong() {
        if (bat) khungXong = KHUNG.toString();
    }

    /** Chữ của game (HdChu) - vùng đã cắt theo clip của Graphics. */
    static void chu(Graphics g, float x, float y, float w, float h, String s) {
        if (!bat || !g.laHd()) return;
        float l = g.manX(x), t = g.manY(y), r = g.manX(x + w), b = g.manY(y + h);
        float cl = g.manX(g.getClipX()), ct = g.manY(g.getClipY());
        float cr = g.manX(g.getClipX() + g.getClipWidth()), cb = g.manY(g.getClipY() + g.getClipHeight());
        l = Math.max(l, cl);
        t = Math.max(t, ct);
        r = Math.min(r, cr);
        b = Math.min(b, cb);
        if (r - l < 1 || b - t < 1) return;
        them(KHUNG, 0, l, t, r, b, s);
    }

    /** Thẻ (bảng / hộp thoại) theo thứ tự vẽ: chữ vẽ TRƯỚC bị thẻ che thì không tính chồng. */
    static void the(float l, float t, float r, float b) {
        if (!bat) return;
        them(KHUNG, 3, l, t, r, b, "the");
    }

    /** Lớp phủ: 1 = nút theo ngữ cảnh, 2 = nút / thẻ HUD. */
    static void ghi(int loai, float l, float t, float r, float b, String s) {
        if (!bat) return;
        them(PHU, loai, l, t, r, b, s);
    }

    private static void them(StringBuilder sb, int loai, float l, float t, float r, float b, String s) {
        if (sb.length() > 60000) return;
        if (sb.length() > 0) sb.append(',');
        sb.append('[').append(loai).append(',').append((int) l).append(',').append((int) t).append(',')
                .append((int) Math.ceil(r)).append(',').append((int) Math.ceil(b)).append(",\"");
        if (s != null) {
            for (int i = 0; i < s.length() && i < 40; i++) {
                char ch = s.charAt(i);
                if (ch == '"' || ch == '\\') sb.append('\\');
                if (ch >= ' ') sb.append(ch);
            }
        }
        sb.append("\"]");
    }

    static void lopPhuMoi() {
        if (bat) PHU.setLength(0);
    }

    /** Sau khi vẽ lớp phủ: định kỳ ghi một dòng. */
    static void lopPhuXong(int W, int H) {
        if (!bat) return;
        long now = System.currentTimeMillis();
        if (now - lucGhi < 1500) return;
        lucGhi = now;
        String man = game.render.Canvas.currentScreen == null ? "?" : game.render.Canvas.currentScreen.getClass().getSimpleName();
        if (game.render.Canvas.currentDialog != null) man += "+" + game.render.Canvas.currentDialog.getClass().getSimpleName();
        if (game.render.Canvas.menu != null && game.render.Canvas.menu.showMenu) man += "+Menu";
        StringBuilder sb = new StringBuilder(KHUNG.length() + PHU.length() + 200);
        sb.append("{\"man\":\"").append(man).append("\",\"W\":").append(W).append(",\"H\":").append(H)
                .append(",\"an\":[").append(javax.microedition.lcdui.NenAndroid.anToanTrai).append(',')
                .append(javax.microedition.lcdui.NenAndroid.anToanTren).append(',')
                .append(javax.microedition.lcdui.NenAndroid.anToanPhai).append(',')
                .append(javax.microedition.lcdui.NenAndroid.anToanDuoi).append("],\"o\":[")
                .append(khungXong);
        if (khungXong.length() > 0 && PHU.length() > 0) sb.append(',');
        sb.append(PHU).append("]}");
        String s = sb.toString();
        // logcat cắt dòng ~4000 ký tự: chia mảnh, công cụ ghép lại
        int n = (s.length() + 3499) / 3500;
        for (int i = 0; i < n; i++) {
            Log.i("HTBBKIEM", (i + 1) + "/" + n + " " + s.substring(i * 3500, Math.min(s.length(), (i + 1) * 3500)));
        }
    }
}
