package com.htbb.pokemon.hd;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import game.model.Command;
import game.model.Main;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.NenAndroid;

/**
 * Nút theo ngữ cảnh thay THANH PHÍM MỀM kiểu J2ME ("Chọn / Đóng / Menu / Xoá" ở đáy màn): 3 lệnh
 * left / center / right của màn / hộp thoại / menu đang hiện được vẽ gắn vào THẺ trên cùng của khung
 * (thẻ = bảng / hộp thoại vẽ sau cùng, ghi lại bởi HdVe):
 * - lệnh đóng (Đóng / Thoát / Huỷ...) -> nút X tròn ở góc phải trên thẻ;
 * - "Menu" -> nút ≡ góc trái trên thẻ;
 * - lệnh chính (center, không có thì left) -> nút đỏ góc phải dưới thẻ; lệnh phụ -> nút kính góc trái dưới.
 * Không có thẻ: thanh trên (≡ trái, X phải) + nút chính phải dưới. Bấm nút = phím mềm J2ME tương ứng
 * (-6 trái, -5 giữa, -7 phải) -> đúng hành động của jar. Vùng chạm phím mềm ẩn ở đáy bị tắt (trừ ô chat).
 */
public final class HdNut {
    private HdNut() {
    }

    // thẻ trên cùng của khung đang vẽ (toạ độ màn logic)
    private static float gl, gt, gr, gb;
    private static boolean coThe;

    /** HdVe gọi khi vẽ bảng / hộp thoại: thẻ vẽ sau cùng trong khung = thẻ trên cùng. */
    static void ghiThe(Graphics g, float x, float y, float w, float h) {
        if (!g.laHd() || w < 60 || h < 34) return;
        gl = g.manX(x);
        gt = g.manY(y);
        gr = g.manX(x + w);
        gb = g.manY(y + h);
        coThe = true;
        HdKiem.the(gl, gt, gr, gb);
        HdPhong.ghiTheGoc(x + g.getTranslateX(), y + g.getTranslateY(), w, h);
    }

    static void khungMoi() {
        coThe = false;
    }

    // ------------------------------------------------------------------ bố cục đã chốt (đọc ở lớp phủ)
    static final int TRAI = 0, GIUA = 1, PHAI = 2;
    /** kiểu: 0 = viên phụ, 1 = viên chính (đỏ), 2 = X đóng, 3 = ≡ menu */
    static final int[] KIEU = new int[3];
    static final String[] CHU = new String[3];
    static final boolean[] CO = new boolean[3];
    static final RectF[] O = {new RectF(), new RectF(), new RectF()};
    static final long[] NHAN = new long[3];
    static volatile boolean hien;
    static final int[] PHIM = {-6, -5, -7};
    static final String[] DONG = {"Đóng", "Thoát", "Huỷ", "Hủy", "Quay lại", "Trở về", "Trở lại", "Back", "Cancel", "Bỏ qua", "Thôi"};

    static final Paint PT = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);
    static final Paint PV = new Paint(Paint.ANTI_ALIAS_FLAG);

    static {
        PV.setStyle(Paint.Style.STROKE);
        PV.setStrokeCap(Paint.Cap.ROUND);
    }

    static boolean laDong(String s) {
        for (int i = 0; i < DONG.length; i++) if (DONG[i].equalsIgnoreCase(s)) return true;
        return false;
    }

    static Main chu() {
        if (game.render.Canvas.currentDialog != null) return game.render.Canvas.currentDialog;
        if (game.render.Canvas.menu != null && game.render.Canvas.menu.showMenu) return game.render.Canvas.menu;
        return game.render.Canvas.currentScreen;
    }

    /** Ô chat của màn chơi dùng vùng phím mềm riêng (Đóng / gửi / Xoá). */
    public static boolean dungPhimMemJar() {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        return gs != null && game.render.Canvas.currentScreen == gs && gs.chatMode && game.render.Canvas.currentDialog == null
                && !game.render.Canvas.menu.showMenu;
    }

    static void khungXong() {
        boolean h = Hud.BAT && !Hud.anPhimMem() && !dungPhimMemJar() && !game.render.Canvas.isLoad;
        Main m = h ? chu() : null;
        if (m == game.render.Canvas.gameScr) m = null;   // màn chơi: lệnh đã có trên HUD
        if (m == null) {
            hien = false;
            return;
        }
        Command[] cm = {m.left, m.center, m.right};
        String[] c = new String[3];
        int so = 0;
        for (int i = 0; i < 3; i++) {
            c[i] = cm[i] != null && cm[i].action != null && cm[i].caption != null && cm[i].caption.trim().length() > 0
                    ? game.pokemon.ChuPokemon.D(cm[i].caption.trim()) : null;
            if (c[i] != null) so++;
        }
        if (so == 0) {
            hien = false;
            return;
        }
        Hud.tinhKhung();
        int W = Hud.W, H = Hud.H;
        float L = Hud.L, T = Hud.T, R = Hud.R, B = Hud.B;
        boolean the = coThe && gr - gl < W - 8;
        float l = the ? gl : L, t = the ? gt : T, r = the ? gr : R, b = the ? gb : B;
        PT.setTypeface(HdChu.typeface(1));
        PT.setTextSize(11f);
        for (int i = 0; i < 3; i++) CO[i] = false;
        // lệnh chính: center, không có thì left (trừ "Menu")
        int chinh = c[GIUA] != null ? GIUA : (c[TRAI] != null && !"Menu".equals(c[TRAI]) ? TRAI : -1);
        float hN = 28, rr = 13.5f;
        boolean ngoaiPhai = false;
        float xCot = 0, yCot = 0;
        // hàng nút dưới thẻ (không che nội dung); không đủ chỗ thì nằm trong mép dưới thẻ
        float yN = the ? b + 6 : B - hN;
        float xPhai = the ? r : R, xTrai = the ? l : L;
        if (yN + hN > B + 3) {
            // không đủ chỗ dưới thẻ: đặt cột bên phải thẻ (ngoài thẻ) nếu đủ, không thì sát mép dưới trong thẻ
            float wCan = 0;
            for (int i = 0; i < 3; i++) if (c[i] != null && !(i == PHAI && laDong(c[i])) && !(i == TRAI && "Menu".equals(c[i]))) wCan = Math.max(wCan, PT.measureText(c[i]) + 30);
            wCan = Math.max(70, wCan);
            if (the && r + 8 + wCan <= R) {
                ngoaiPhai = true;
                xCot = r + 8;
                yCot = b - hN;
            } else {
                yN = the ? b - hN - 4 : B - hN;
            }
        }
        if (chinh >= 0) {
            float w = Math.max(70, PT.measureText(c[chinh]) + 30);
            if (ngoaiPhai) {
                O[chinh].set(xCot, yCot, xCot + w, yCot + hN);
                yCot -= hN + 8;
            } else {
                O[chinh].set(xPhai - w, yN, xPhai, yN + hN);
                xPhai -= w + 8;
            }
            KIEU[chinh] = 1;
            CHU[chinh] = c[chinh];
            CO[chinh] = true;
        }
        // phải: đóng -> X bên phải thẻ (ngoài thẻ nếu đủ chỗ); khác -> viên phụ cạnh nút chính
        if (c[PHAI] != null) {
            if (laDong(c[PHAI])) {
                float cx = r + rr + 5, cy = t + rr;
                if (!the || cx + rr > R) {
                    cx = Math.min(R - rr, r - rr - 2);
                    cy = Math.max(T + rr, t - rr * 0.4f);
                }
                O[PHAI].set(cx - rr, cy - rr, cx + rr, cy + rr);
                KIEU[PHAI] = 2;
            } else {
                float w = Math.max(56, PT.measureText(c[PHAI]) + 24);
                if (ngoaiPhai) {
                    O[PHAI].set(xCot, yCot, xCot + w, yCot + hN);
                    yCot -= hN + 8;
                } else {
                    O[PHAI].set(xPhai - w, yN, xPhai, yN + hN);
                    xPhai -= w + 8;
                }
                KIEU[PHAI] = 0;
            }
            CHU[PHAI] = c[PHAI];
            CO[PHAI] = true;
        }
        if (c[TRAI] != null && chinh != TRAI) {
            if ("Menu".equals(c[TRAI])) {
                float cx = l - rr - 5, cy = t + rr;
                if (!the || cx - rr < L) {
                    cx = Math.max(L + rr, l + rr + 2);
                    cy = Math.max(T + rr, t - rr * 0.4f);
                }
                O[TRAI].set(cx - rr, cy - rr, cx + rr, cy + rr);
                KIEU[TRAI] = 3;
            } else {
                float w = Math.max(56, PT.measureText(c[TRAI]) + 24);
                if (ngoaiPhai) {
                    O[TRAI].set(xCot, yCot, xCot + w, yCot + hN);
                    yCot -= hN + 8;
                } else {
                    float x0 = Math.min(xTrai, xPhai - w);
                    O[TRAI].set(x0, yN, x0 + w, yN + hN);
                }
                KIEU[TRAI] = 0;
            }
            CHU[TRAI] = c[TRAI];
            CO[TRAI] = true;
        }
        hien = true;
    }

    static void ve(Canvas c) {
        if (!hien) return;
        long now = System.currentTimeMillis();
        PT.setTypeface(HdChu.typeface(1));
        for (int i = 0; i < 3; i++) {
            if (!CO[i]) continue;
            RectF o = O[i];
            float k = now - NHAN[i] < 120 ? 0.94f : 1f;
            float cx = o.centerX(), cy = o.centerY();
            float hw = o.width() / 2 * k, hh = o.height() / 2 * k;
            float l = cx - hw, t = cy - hh, r = cx + hw, b = cy + hh;
            switch (KIEU[i]) {
                case 2:
                case 3: {
                    float rr = hw;
                    HdVe.bong(c, l, t, r, b, rr, 1f);
                    if (KIEU[i] == 2) HdVe.tronDoc(c, l, t, r, b, rr, 0xFFFFFFFF, 0xFFDDE5F0);
                    else HdVe.tronDoc(c, l, t, r, b, rr, 0xF0284C82, 0xF8122446);
                    HdVe.vienTron(c, l, t, r, b, rr, 1f, KIEU[i] == 2 ? 0xFF1E2A44 : 0xC0A9C8FF);
                    PV.setStrokeWidth(2f * k);
                    if (KIEU[i] == 2) {
                        PV.setColor(0xFFE3350D);
                        float d = rr * 0.38f;
                        c.drawLine(cx - d, cy - d, cx + d, cy + d, PV);
                        c.drawLine(cx - d, cy + d, cx + d, cy - d, PV);
                    } else {
                        PV.setColor(0xFFFFFFFF);
                        float d = rr * 0.42f;
                        for (int j = -1; j <= 1; j++) c.drawLine(cx - d, cy + j * d * 0.62f, cx + d, cy + j * d * 0.62f, PV);
                    }
                    break;
                }
                default: {
                    float rr = (b - t) / 2;
                    HdVe.bong(c, l, t, r, b, rr, 1.2f);
                    if (KIEU[i] == 1) {
                        HdVe.tronDoc(c, l, t, r, b, rr, 0xFFF4613A, 0xFFC2290A);
                        // sọc chéo kiểu thẻ Pokémon
                        int sv = c.save();
                        HdVe.R.set(l, t, r, b);
                        android.graphics.Path p = new android.graphics.Path();
                        p.addRoundRect(HdVe.R, rr, rr, android.graphics.Path.Direction.CW);
                        c.clipPath(p);
                        P.setColor(0x18FFFFFF);
                        P.setStrokeWidth(3f);
                        for (float x = l - (b - t); x < r; x += 9) c.drawLine(x, b, x + (b - t), t, P);
                        c.restoreToCount(sv);
                    } else {
                        HdVe.tronDoc(c, l, t, r, b, rr, 0xF0FFFFFF, 0xF0DCE6F4);
                    }
                    HdVe.tronDoc(c, l + 1, t + 0.6f, r - 1, t + (b - t) * 0.5f, rr, 0x40FFFFFF, 0x00FFFFFF);
                    HdVe.vienTron(c, l, t, r, b, rr, 1f, KIEU[i] == 1 ? 0xFF7A1505 : 0xFF1E2A44);
                    PT.setTextSize(11f * k);
                    PT.setTextAlign(Paint.Align.CENTER);
                    PT.setColor(KIEU[i] == 1 ? 0xFFFFFFFF : 0xFF1E2A44);
                    String s = CHU[i];
                    if (KIEU[i] == 1) {
                        // ▶ con trỏ kiểu Pokémon trước lệnh chính
                        float tw = PT.measureText(s);
                        float ax = cx - tw / 2 - 7, ay = cy;
                        android.graphics.Path p = new android.graphics.Path();
                        p.moveTo(ax - 2.5f, ay - 3.5f);
                        p.lineTo(ax + 3f, ay);
                        p.lineTo(ax - 2.5f, ay + 3.5f);
                        p.close();
                        P.setColor(0xFFFFE066);
                        c.drawPath(p, P);
                        c.drawText(s, cx + 3, cy + 3.9f * k, PT);
                    } else {
                        c.drawText(s, cx, cy + 3.9f * k, PT);
                    }
                    break;
                }
            }
            HdKiem.ghi(1, l, t, r, b, CHU[i]);
        }
    }

    static int nutGiu = -1;

    /** Chạm (luồng sự kiện): true = trúng nút. */
    static boolean cham(int kieu, float x, float y) {
        if (kieu == 0) {
            if (!hien) return false;
            for (int i = 0; i < 3; i++) {
                if (!CO[i]) continue;
                RectF o = O[i];
                if (x >= o.left - 8 && x <= o.right + 8 && y >= o.top - 8 && y <= o.bottom + 8) {
                    nutGiu = i;
                    NHAN[i] = System.currentTimeMillis();
                    if (Hud.rung) NenAndroid.rungNhe();
                    return true;
                }
            }
            return false;
        }
        if (nutGiu < 0) return false;
        if (kieu == 2) {
            int i = nutGiu;
            nutGiu = -1;
            RectF o = O[i];
            if (x >= o.left - 12 && x <= o.right + 12 && y >= o.top - 12 && y <= o.bottom + 12) {
                NenAndroid.phimNgay(0, PHIM[i]);
                NenAndroid.phimNgay(2, PHIM[i]);
            }
        }
        return true;
    }
}
