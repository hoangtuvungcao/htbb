package com.htbb.pokemon.hd;

import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.KhungCoDinh;

/**
 * CAMERA CỐ ĐỊNH (quyết định chủ game - chống lợi thế màn to, giống bản PC): giao diện logic 640x360, thế giới
 * phóng KhungCoDinh.THE_GIOI_Z = 2 / NHIN_THEM (0.9.7: NHIN_THEM 1,15 -> vùng nhìn ~368x207 px thế giới, ~23 x 13
 * ô) trên mọi máy. Không thu phóng (bỏ tuỳ chọn "Phóng" và phóng theo cỡ bản đồ của bản cũ).
 *
 * port-j2me.py (vá BẮT BUỘC): GameScr.loadCamera đặt gsw / gsh = gsw(Canvas.w) / gsh(Canvas.h) để camera của game
 * theo vùng nhìn (NHIN_W x NHIN_H); GameScr.paint gọi batDau sau g.translate(-cmx, -cmy) và ket trước HUD; toạ độ chạm
 * thế giới cmx + Canvas.px -> wx(cmx, Canvas.px). Bản đồ nhỏ hơn vùng nhìn: game tự canh giữa, lề vẽ nền().
 */
public final class HdZoom {
    /** phóng thế giới so với giao diện logic (cố định) */
    static final float Z = KhungCoDinh.THE_GIOI_Z;
    private static boolean dang;

    private HdZoom() {
    }

    /** Vùng nhìn thế giới (px thế giới) - GameScr.loadCamera (gsw). */
    public static int gsw(int canvasW) {
        return KhungCoDinh.nhinW(canvasW);
    }

    public static int gsh(int canvasH) {
        return KhungCoDinh.nhinH(canvasH);
    }

    /** Gọi ngay sau g.translate(-cmx, -cmy) của GameScr.paint: lớp thế giới phóng Z. */
    public static void batDau(Graphics g, GameScr gs) {
        dang = true;
        g.datBienDoi(Z, 0, 0);
    }

    /** Nền sau bản đồ (thay fillRect đen của jar): navy theo chủ đề - lề quanh bản đồ nhỏ hơn vùng nhìn. */
    public static void nen(Graphics g, int x, int y, int w, int h) {
        g.setColor(0x0A1426);
        g.fillRect(x, y, w, h);
    }

    /** Hết phần thế giới (trước khung thông tin / HUD): vẽ thời tiết HD trong khung thế giới rồi bỏ phóng. */
    public static void ket(Graphics g) {
        try {
            HdHieuUng.veThoiTiet(g);
        } catch (Throwable e) {
            // bỏ qua
        }
        if (dang) g.datBienDoi(1f, 0, 0);
        dang = false;
    }

    /** Vùng thế giới đang thấy (px thế giới): trái, trên, rộng, cao - khung camera trên bản đồ nhỏ. */
    static float[] vungThay() {
        return new float[]{GameScr.cmx, GameScr.cmy, GameScr.gsw, GameScr.gsh};
    }

    /** Tạm bỏ phóng thế giới để vẽ phủ cả màn (đêm, thời tiết); khoiPhuc trả lại. */
    static void boTam(Graphics g) {
        if (dang) g.datBienDoi(1f, 0, 0);
    }

    static void khoiPhuc(Graphics g) {
        if (dang) g.datBienDoi(Z, 0, 0);
    }

    /** Toạ độ màn (logic) của điểm thế giới (lớp phủ: nổ lên cấp quanh nhân vật). */
    static float manX(int wx) {
        return (wx - GameScr.cmx) * Z;
    }

    static float manY(int wy) {
        return (wy - GameScr.cmy) * Z;
    }

    /** Toạ độ thế giới của điểm chạm (thay cmx + Canvas.px). */
    public static int wx(int cmx, int px) {
        return cmx + KhungCoDinh.theGioi(px);
    }

    public static int wy(int cmy, int py) {
        return cmy + KhungCoDinh.theGioi(py);
    }
}
