package com.htbb.pokemon.hd;

import android.graphics.Canvas;
import android.graphics.Paint;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.NenAndroid;

/**
 * Cửa sổ cỡ cố định của jar (Hành trang / Trang bị / Kỹ năng / cửa hàng... 145x144 px, Menu 170x150, hộp thoại xác
 * nhận) quá nhỏ trên màn điện thoại: bản APK phóng cả cửa sổ thành thẻ lớn giữa khung 640x360, phủ mờ nền game phía
 * sau, trượt vào khi mở. Cùng dữ liệu, cùng ô, cùng thao tác (chạm đổi ngược qua doiX / doiY); thanh phím mềm vẫn vẽ
 * đúng chỗ (không phóng). Chữ / khung là vector nên phóng vẫn nét.
 *
 * Hai LỚP độc lập (HdPhongToan.Lop): 0 = màn (currentScreen), 1 = hộp thoại (currentDialog) - Canvas.paint gọi ket()
 * sau mỗi lớp (port-j2me.py). Menu (Canvas.menu) vẽ sau cùng KHÔNG phóng -> khi menu mở chạm không đổi.
 */
public final class HdPhong {
    private static final HdPhongToan.Lop[] LOP = {new HdPhongToan.Lop(), new HdPhongToan.Lop()};
    /** số lần ket() trong khung đang vẽ (= lớp đang vẽ) - Hud.khungMoi đặt lại */
    private static int soKet;
    private static float z = 1f, dx, dy;
    private static boolean dang;
    private static int lopDang;
    private static final Paint P = new Paint();
    private static float moiX, moiY, moiW, moiH;

    private HdPhong() {
    }

    /** Đầu mỗi khung game (Hud.khungMoi). */
    static void khungMoi() {
        soKet = 0;
        dang = false;
    }

    static void ghiTheGoc(float x, float y, float w, float h) {
        if (!dang) return;
        if (w * h > moiW * moiH) {
            moiX = x;
            moiY = y;
            moiW = w;
            moiH = h;
        }
    }

    /** Cao thanh phím mềm (không phóng, chạm không đổi). */
    static int caoThanhPhim() {
        return 2 * game.render.screen.Screen.deltaY + 2;
    }

    /**
     * Bắt đầu phóng cửa sổ (x, y, w, h: khung cửa sổ theo toạ độ màn jar) - gọi ngay sau khi màn vẽ nền
     * game phía sau. man = màn / hộp thoại đang vẽ (hoạt hình mở khi đổi).
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
        long now = System.currentTimeMillis();
        int lop = Math.min(soKet, LOP.length - 1);
        HdPhongToan.Lop L = LOP[lop];
        L.vao(man, now);
        float fx, fy, fw, fh;
        if (L.tdW > 0) {
            // dùng khung thẻ đo được ở khung trước của CHÍNH lớp này (cửa sổ jar có thể to hơn ước lượng)
            fx = L.tdX;
            fy = L.tdY;
            fw = (float) Math.ceil(L.tdW);
            fh = (float) Math.ceil(L.tdH);
        } else {
            fx = x + g.getTranslateX();
            fy = y + g.getTranslateY();
            fw = w;
            fh = h;
        }
        moiW = 0;
        moiH = 0;
        int W = game.render.Canvas.w, H = game.render.Canvas.h;
        int band = caoThanhPhim();
        int k = Math.max(1, NenAndroid.heSo);
        float zz = HdPhongToan.heSo(W, H, band, fy, fw, fh, zMax, k, neoTren);
        float e = L.doMo(now);
        // phủ mờ nền game (vẽ ở toạ độ màn, chưa phóng)
        if (!neoTren) {
            Canvas c = g.androidCanvas();
            P.setColor(((int) (0x88 * e) << 24) | 0x050B18);
            c.drawRect(-g.getTranslateX() - 10, -g.getTranslateY() - 10, -g.getTranslateX() + W + 10, -g.getTranslateY() + H + 10, P);
        }
        if (zz <= 1.01f) {
            return;
        }
        float[] bd = HdPhongToan.bienDoi(W, H, band, fx, fy, fw, fh, zz, k, e, neoTren);
        z = bd[0];
        dx = bd[1];
        dy = bd[2];
        dang = true;
        lopDang = lop;
        g.datPhongMan(z, dx, dy);
    }

    /** Hết một lớp (Canvas.paint gọi sau currentScreen.paint và sau currentDialog.paint): bỏ phóng, ghi cho chạm. */
    public static void ket(Graphics g) {
        int lop = Math.min(soKet, LOP.length - 1);
        HdPhongToan.Lop L = LOP[lop];
        if (dang && lopDang == lop) {
            if (moiW > 0) {
                L.tdX = moiX;
                L.tdY = moiY;
                L.tdW = moiW;
                L.tdH = moiH;
            }
            g.datPhongMan(1f, 0, 0);
            L.z = z;
            L.dx = dx;
            L.dy = dy;
        } else {
            L.boPhong();
        }
        dang = false;
        z = 1f;
        dx = 0;
        dy = 0;
        if (lop == 0 && game.render.Canvas.currentDialog == null) LOP[1].boPhong();
        soKet++;
    }

    public static boolean dangPhong() {
        return LOP[0].z != 1f || LOP[1].z != 1f;
    }

    /** Hoạt hình mở đang chạy (vẽ lại mượt). */
    public static boolean dangDong() {
        long now = System.currentTimeMillis();
        return LOP[0].dangDong(now) || LOP[1].dangDong(now);
    }

    /** Lớp đang nhận chạm: menu mở = không lớp nào (menu vẽ không phóng), hộp thoại, rồi màn. */
    private static HdPhongToan.Lop lopCham() {
        if (game.render.Canvas.menu != null && game.render.Canvas.menu.showMenu) return null;
        return game.render.Canvas.currentDialog != null ? LOP[1] : LOP[0];
    }

    /** Đổi điểm chạm (logic) -> toạ độ jar trong cửa sổ đã phóng (thanh phím mềm giữ nguyên). */
    public static int doiX(int x, int y) {
        HdPhongToan.Lop L = lopCham();
        if (L == null) return x;
        return HdPhongToan.doi(x, L.z, L.dx, y, game.render.Canvas.h, caoThanhPhim());
    }

    public static int doiY(int x, int y) {
        HdPhongToan.Lop L = lopCham();
        if (L == null) return y;
        return HdPhongToan.doi(y, L.z, L.dy, y, game.render.Canvas.h, caoThanhPhim());
    }
}
