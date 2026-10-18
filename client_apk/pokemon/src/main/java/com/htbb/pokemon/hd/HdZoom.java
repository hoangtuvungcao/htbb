package com.htbb.pokemon.hd;

import game.model.Tilemap;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.NenAndroid;

/**
 * Bản đồ nhỏ hơn màn (nhà, hang, map mở đầu...): bản jar vẽ bản đồ 1x giữa khoảng đen. Bản APK phóng phần
 * THẾ GIỚI của GameScr.paint (nền, nhân vật, hiệu ứng, thời tiết) cho kín màn: hệ số làm tròn để
 * hệ số * NenAndroid.heSo là số nguyên (pixel art không méo), camera theo nhân vật, kẹp trong mép bản đồ
 * -> không còn khoảng đen, hạt / thời tiết nằm gọn trong bản đồ. HUD + bảng vẽ sau (không phóng).
 * Chạm vào thế giới đổi ngược qua wx / wy (GameScr.updatePoint / findRoad).
 */
public final class HdZoom {
    private static float z = 1f, dx, dy;
    private static float camX = Float.NaN, camY;
    private static int mapW, mapH;

    private HdZoom() {
    }

    /** Gọi ngay sau g.translate(-cmx, -cmy) của GameScr.paint. */
    public static void batDau(Graphics g, GameScr gs) {
        z = 1f;
        dx = 0;
        dy = 0;
        if (!Hud.BAT || gs == null || gs.mainChar == null) return;
        int mw = Tilemap.w * 16, mh = Tilemap.h * 16;
        int W = game.render.Canvas.w, H = game.render.Canvas.h;
        int k = Math.max(1, NenAndroid.heSo);
        // thu phóng thế giới kiểu game di động: thấy ~16 ô chiều cao (nhân vật to, dễ chạm); map nhỏ hơn màn
        // thì phóng cho kín. Hệ số * heSo nguyên -> pixel art không méo. Camera theo nhân vật, kẹp mép map.
        float muon = Hud.zoomTheGioi ? Math.max(1f, (float) Math.floor(H / (16f * 16f) * k) / k) : 1f;
        float r = mw > 0 && mh > 0 ? Math.max(W / (float) mw, H / (float) mh) : 1f;
        float zz = Math.max(muon, (float) Math.ceil(r * k - 0.01f) / k);
        if (mw <= 0 || mh <= 0 || zz <= 1.001f) {
            camX = Float.NaN;
            return;
        }
        float vw = W / zz, vh = H / zz;
        float tx = gs.mainChar.x - vw / 2, ty = gs.mainChar.y - 16 - vh / 2;
        tx = Math.max(0, Math.min(mw - vw, tx));
        ty = Math.max(0, Math.min(mh - vh, ty));
        if (Float.isNaN(camX) || mw != mapW || mh != mapH) {
            camX = tx;
            camY = ty;
            mapW = mw;
            mapH = mh;
        } else {
            camX += (tx - camX) * 0.22f;
            camY += (ty - camY) * 0.22f;
        }
        // điểm ảnh thật nguyên: làm tròn dịch theo 1/heSo
        z = zz;
        dx = Math.round(zz * (GameScr.cmx - camX) * k) / (float) k;
        dy = Math.round(zz * (GameScr.cmy - camY) * k) / (float) k;
        g.datBienDoi(z, dx, dy);
    }

    /** Hết phần thế giới (trước khung thông tin / HUD): vẽ thời tiết HD trong khung thế giới rồi bỏ phóng. */
    public static void ket(Graphics g) {
        try {
            HdHieuUng.veThoiTiet(g);
        } catch (Throwable e) {
            // bỏ qua
        }
        if (z != 1f || dx != 0 || dy != 0) g.datBienDoi(1f, 0, 0);
    }

    /** Vùng thế giới đang thấy (px thế giới): trái, trên, rộng, cao - khung camera trên bản đồ nhỏ. */
    static float[] vungThay() {
        int W = game.render.Canvas.w, H = game.render.Canvas.h;
        if (z <= 1.001f || Float.isNaN(camX)) return new float[]{GameScr.cmx, GameScr.cmy, W, H};
        return new float[]{camX, camY, W / z, H / z};
    }

    /** Tạm bỏ phóng thế giới để vẽ phủ cả màn (đêm, thời tiết); khoiPhuc trả lại. */
    static void boTam(Graphics g) {
        if (z != 1f || dx != 0 || dy != 0) g.datBienDoi(1f, 0, 0);
    }

    static void khoiPhuc(Graphics g) {
        if (z != 1f || dx != 0 || dy != 0) g.datBienDoi(z, dx, dy);
    }

    /** Toạ độ màn của điểm thế giới (lớp phủ: nổ lên cấp quanh nhân vật). */
    static float manX(int wx) {
        return (wx - GameScr.cmx) * z + dx;
    }

    static float manY(int wy) {
        return (wy - GameScr.cmy) * z + dy;
    }

    /** Toạ độ thế giới của điểm chạm (thay cmx + Canvas.px). */
    public static int wx(int cmx, int px) {
        return cmx + (int) ((px - dx) / z);
    }

    public static int wy(int cmy, int py) {
        return cmy + (int) ((py - dy) / z);
    }
}
