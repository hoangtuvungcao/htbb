package com.htbb.pokemon.hd;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import java.util.HashMap;
import javax.microedition.lcdui.Graphics;

/**
 * Bộ vẽ giao diện HD cho bản APK: thay các hàm vẽ của game.pokemon.GiaoDien / KieuO (bản jar tô từng hàng
 * điểm ảnh, không có alpha) bằng hình vector khử răng cưa ở độ phân giải thật: bo góc mượt, dải màu, kính
 * mờ (alpha), bóng đổ mềm, viền sáng. port-j2me.py chèn "if (HdVe.xxx(...)) return;" vào đầu mỗi hàm
 * GiaoDien -> mọi bảng, hộp thoại, tab, thanh HP, viên chữ... của mọi màn đổi kiểu mà không đổi bố cục,
 * dữ liệu hay thao tác. Chỉ chạy trên luồng vẽ (một luồng) nên dùng chung Paint tĩnh.
 */
public final class HdVe {
    // bảng màu (cùng GiaoDien: đỏ Poké, vàng Pikachu, xanh, navy) + màu kính
    static final int VIEN = 0xFF1E2A44;
    static final int DO = 0xFFE3350D;
    static final int DO_SANG = 0xFFF4613A;
    static final int DO_TOI = 0xFFA5240A;
    static final int VANG = 0xFFFFCB05;
    static final int XANH = 0xFF3D7DCA;
    static final int XANH_SANG = 0xFF6FA6EA;
    static final int KINH_TREN = 0xDE1A305C;   // token chung với pc-java Kinh.bang
    static final int KINH_DUOI = 0xEC0A142C;

    private static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint PV = new Paint(Paint.ANTI_ALIAS_FLAG);
    static final RectF R = new RectF();
    private static final Path PATH = new Path();
    private static final HashMap<Long, LinearGradient> DAI = new HashMap<Long, LinearGradient>();

    static {
        PV.setStyle(Paint.Style.STROKE);
    }

    private HdVe() {
    }

    // ------------------------------------------------------------------ nền dưới chữ (tương phản tự động)
    /** các khối đã tô trong khung (toạ độ màn) + độ sáng -> HdChu chọn chữ sáng / tối cho dễ đọc */
    static final float[] NEN = new float[256 * 5];
    static int soNen;

    static void khungMoi() {
        soNen = 0;
    }

    static int doSang(int c) {
        return ((c >> 16 & 255) * 299 + (c >> 8 & 255) * 587 + (c & 255) * 114) / 1000;
    }

    static void ghiNen(Graphics g, float x, float y, float w, float h, int sang) {
        if (w < 6 || h < 6) return;
        float l = g.manX(x), t = g.manY(y), r = g.manX(x + w), b = g.manY(y + h);
        if (soNen >= 256) {
            System.arraycopy(NEN, 5 * 64, NEN, 0, 5 * 192);
            soNen = 192;
        }
        int i = soNen * 5;
        NEN[i] = l;
        NEN[i + 1] = t;
        NEN[i + 2] = r;
        NEN[i + 3] = b;
        NEN[i + 4] = sang;
        soNen++;
    }

    /** Độ sáng nền tại điểm màn (x, y): khối vẽ sau cùng chứa điểm; -1 = không biết. */
    static int sangTai(float x, float y) {
        for (int i = soNen - 1; i >= 0; i--) {
            int k = i * 5;
            if (x >= NEN[k] && x <= NEN[k + 2] && y >= NEN[k + 1] && y <= NEN[k + 3]) return (int) NEN[k + 4];
        }
        return -1;
    }

    // ------------------------------------------------------------------ tiện ích

    static Canvas cv(Graphics g) {
        return g.androidCanvas();
    }

    /** Dải màu dọc argb tren -> duoi trên đoạn [y0, y1] (bộ nhớ đệm theo màu + vị trí). */
    static Shader doc(int tren, int duoi, float y0, float y1) {
        long k = ((long) tren * 31 + duoi) * 1000003L + Float.floatToIntBits(y0) * 7919L + Float.floatToIntBits(y1);
        LinearGradient s = DAI.get(k);
        if (s == null) {
            if (DAI.size() > 256) DAI.clear();
            s = new LinearGradient(0, y0, 0, y1, tren, duoi, Shader.TileMode.CLAMP);
            DAI.put(k, s);
        }
        return s;
    }

    static void tron(Canvas c, float l, float t, float r, float b, float rad, int argb) {
        P.setShader(null);
        P.setColor(argb);
        R.set(l, t, r, b);
        c.drawRoundRect(R, rad, rad, P);
    }

    static void tronDoc(Canvas c, float l, float t, float r, float b, float rad, int tren, int duoi) {
        P.setShader(doc(tren, duoi, t, b));
        P.setColor(0xFF000000);
        R.set(l, t, r, b);
        c.drawRoundRect(R, rad, rad, P);
        P.setShader(null);
    }

    static void vienTron(Canvas c, float l, float t, float r, float b, float rad, float day, int argb) {
        PV.setColor(argb);
        PV.setStrokeWidth(day);
        R.set(l + day / 2, t + day / 2, r - day / 2, b - day / 2);
        c.drawRoundRect(R, rad, rad, PV);
    }

    /** Bóng đổ mềm (3 lớp alpha thấp, rẻ hơn blur). */
    static void bong(Canvas c, float l, float t, float r, float b, float rad, float doSau) {
        for (int i = 3; i >= 1; i--) {
            float e = i * 0.9f;
            tron(c, l - e * 0.5f, t - e * 0.2f + doSau, r + e * 0.5f, b + e + doSau, rad + e, 0x1A000000);
        }
    }

    static int argb(int rgb) {
        return 0xFF000000 | rgb;
    }

    /** Pha màu rgb a -> b theo t (0..1). */
    static int pha(int a, int b, float t) {
        int ra = a >> 16 & 255, ga = a >> 8 & 255, ba = a & 255;
        int r = (int) (ra + ((b >> 16 & 255) - ra) * t);
        int gg = (int) (ga + ((b >> 8 & 255) - ga) * t);
        int bb = (int) (ba + ((b & 255) - ba) * t);
        return (a & 0xFF000000) | r << 16 | gg << 8 | bb;
    }

    // ------------------------------------------------------------------ thay GiaoDien

    public static boolean khoi(Graphics g, int x, int y, int w, int h, int r, int mau) {
        if (w <= 0 || h <= 0) return true;
        if (mau != 0x0B1222 && mau != 0x0A0F2C) ghiNen(g, x, y, w, h, doSang(mau));
        Canvas c = cv(g);
        float l = x + g.getTranslateX();
        float t = y + g.getTranslateY();
        float rad = Math.min(r, Math.min(w, h) / 2f);
        if (mau == 0x0A0F2C || (mau == 0x0B1222 && w * h >= 700)) {
            // "bóng đổ" kiểu jar (khối tối lệch 1-2 px) -> bóng mềm
            bong(c, l, t - 1, l + w - 1, t + h - 2, rad, 0);
            return true;
        }
        tron(c, l, t, l + w, t + h, rad, argb(mau));
        return true;
    }

    public static boolean khoiDoc(Graphics g, int x, int y, int w, int h, int r, int tren, int duoi) {
        if (w <= 0 || h <= 0) return true;
        ghiNen(g, x, y, w, h, (doSang(tren) + doSang(duoi)) / 2);
        Canvas c = cv(g);
        float l = x + g.getTranslateX();
        float t = y + g.getTranslateY();
        float rad = Math.min(r, Math.min(w, h) / 2f);
        tronDoc(c, l, t, l + w, t + h, rad, argb(tren), argb(duoi));   // 0.9.5: phẳng (bỏ ánh kính)
        return true;
    }

    /** Hộp thoại sáng (chữ tối): thẻ trắng ngà, bo 8, viền mảnh, bóng mềm. */
    public static boolean hop(Graphics g, int x, int y, int w, int h) {
        moClip(g);
        ghiNen(g, x, y, w, h, 240);
        HdNut.ghiThe(g, x, y, w, h);
        Canvas c = cv(g);
        float l = x + g.getTranslateX();
        float t = y + g.getTranslateY();
        theSang(c, l, t, l + w, t + h, 9);
        game.render.screen.Font.nenSang = true;
        return true;
    }

    /** Thẻ sáng 0.9.5 (game.pokemon.ChuDe): bóng mềm, thân trắng, viền tối mảnh. */
    static void theSang(Canvas c, float l, float t, float r, float b, float rad) {
        bong(c, l, t, r, b, rad, 1.5f);
        tronDoc(c, l, t, r, b, rad, argb(game.pokemon.ChuDe.NEN), argb(game.pokemon.ChuDe.NEN_DUOI));
        vienTron(c, l, t, r, b, rad, 0.8f, 0xC0000000 | game.pokemon.ChuDe.VIEN);
    }

    /**
     * Bảng chính (cùng hướng với jar mới: thẻ SÁNG kiểu Pokémon Sword/Shield): thân trắng ngà -> xanh băng,
     * viền navy mảnh + viền trong xanh, sọc chéo rất nhạt, bóng mềm. Bật Font.nenSang như jar -> chữ sáng
     * của game tự đổi sang bản navy (HdChu).
     */
    public static boolean bang(Graphics g, int x, int y, int w, int h) {
        moClip(g);
        HdNut.ghiThe(g, x, y, w, h);
        Canvas c = cv(g);
        float l = x + g.getTranslateX();
        float t = y + g.getTranslateY();
        theSang(c, l, t, l + w, t + h, 9);
        ghiNen(g, x, y, w, h, 240);
        game.render.screen.Font.nenSang = true;
        return true;
    }

    static void bangSang(Canvas c, float l, float t, float r, float b, float rad) {
        bong(c, l, t, r, b, rad, 2f);
        tronDoc(c, l, t, r, b, rad, 0xFF2F6CB8, 0xFF1E4C8E);
        tronDoc(c, l + 2, t + 2, r - 2, b - 2, rad - 1.5f, 0xFFFFFFFF, 0xFFEAF1FA);
        socCheo(c, l + 2, t + 2, r - 2, b - 2, rad - 1.5f, 0x0A1E4C8E, 11f);
        vienTron(c, l, t, r, b, rad, 0.9f, 0xFF0F1B33);
        vienTron(c, l + 2, t + 2, r - 2, b - 2, rad - 1.5f, 0.6f, 0x501E4C8E);
    }

    /** Bảng tối (jar: bangToi - thẻ / khung con nền đậm, chữ trắng): kính navy. */
    public static boolean bangToi(Graphics g, int x, int y, int w, int h) {
        moClip(g);
        HdNut.ghiThe(g, x, y, w, h);
        Canvas c = cv(g);
        float l = x + g.getTranslateX();
        float t = y + g.getTranslateY();
        // 0.9.5: thẻ nhấn XANH tươi (chữ trắng đọc rõ) thay kính navy tối
        bong(c, l, t, l + w, t + h, 9, 1.5f);
        tronDoc(c, l, t, l + w, t + h, 9, pha(argb(game.pokemon.ChuDe.XANH), 0xFFFFFFFF, 0.2f), argb(game.pokemon.ChuDe.XANH_TOI));
        vienTron(c, l, t, l + w, t + h, 9, 0.8f, 0xC0000000 | game.pokemon.ChuDe.VIEN);
        game.render.screen.Font.nenSang = false;
        ghiNen(g, x, y, w, h, 90);
        return true;
    }

    /** Sọc chéo mảnh kiểu thẻ Pokémon (Sword/Shield), cắt theo khối bo góc. */
    static void socCheo(Canvas c, float l, float t, float r, float b, float rad, int argb, float buoc) {
        int sv = c.save();
        PATH.reset();
        R.set(l, t, r, b);
        PATH.addRoundRect(R, rad, rad, Path.Direction.CW);
        c.clipPath(PATH);
        PV.setColor(argb);
        PV.setStrokeWidth(buoc * 0.35f);
        float h = b - t;
        for (float x = l - h; x < r; x += buoc) c.drawLine(x, b, x + h, t, PV);
        c.restoreToCount(sv);
    }

    static void bangKinh(Canvas c, float l, float t, float r, float b, float rad) {
        bong(c, l, t, r, b, rad, 1.5f);
        tronDoc(c, l, t, r, b, rad, KINH_TREN, KINH_DUOI);
        socCheo(c, l, t, r, b, rad, 0x0CFFFFFF, 10f);
        tronDoc(c, l + 1, t + 1, r - 1, t + Math.min(26, (b - t) * 0.4f), rad - 1, 0x26FFFFFF, 0x00FFFFFF);
        vienTron(c, l, t, r, b, rad, 1f, 0xFF0A1428);
        vienTron(c, l + 1, t + 1, r - 1, b - 1, rad - 1, 0.6f, 0x50A9C8FF);
    }

    public static boolean theMo(Graphics g, int x, int y, int w, int h) {
        moClip(g);
        ghiNen(g, x, y, w, h, 235);
        phongManTruocGame(g, x, y, w, h);
        HdNut.ghiThe(g, x, y, w, h);
        Canvas c = cv(g);
        float l = x + g.getTranslateX();
        float t = y + g.getTranslateY();
        bong(c, l, t, l + w, t + h, 10, 1.5f);
        tronDoc(c, l, t, l + w, t + h, 10, 0xEBFFFFFF, 0xE0E6EEF9);
        vienTron(c, l, t, l + w, t + h, 10, 1f, 0x901E2A44);
        vienTron(c, l + 1, t + 1, l + w - 1, t + h - 1, 9, 0.6f, 0xFFFFFFFF);
        return true;
    }

    public static boolean bangMo(Graphics g, int x, int y, int w, int h) {
        moClip(g);
        // 0.9.5: thẻ trắng mờ (chữ tối) - cùng kiểu theMo
        ghiNen(g, x, y, w, h, 238);
        game.render.screen.Font.nenSang = true;
        phongManTruocGame(g, x, y, w, h);
        HdNut.ghiThe(g, x, y, w, h);
        Canvas c = cv(g);
        float l = x + g.getTranslateX();
        float t = y + g.getTranslateY();
        bong(c, l, t, l + w, t + h, 10, 1.5f);
        tronDoc(c, l, t, l + w, t + h, 10, 0xF2FFFFFF, 0xEAF3F6FB);
        vienTron(c, l, t, l + w, t + h, 10, 0.8f, 0xA0000000 | game.pokemon.ChuDe.VIEN);
        return true;
    }

    /** Dải tiêu đề (0.9.5): jar GiaoDien.dai vẽ bằng khoiDoc / pokeBall / chữ giao diện (đều HD). */
    public static boolean dai(Graphics g, int x, int y, int w, int h, String tieuDe) {
        return false;
    }

    public static boolean vienTab(Graphics g, int x, int y, int w, int h, boolean doiTab) {
        ghiNen(g, x, y, w, h, 100);
        Canvas c = cv(g);
        float l = x + g.getTranslateX();
        float t = y + g.getTranslateY();
        float rad = h / 2f;
        bong(c, l, t, l + w, t + h, rad, 1f);
        int m = argb(doiTab ? game.pokemon.ChuDe.XANH : game.pokemon.ChuDe.DO);
        tronDoc(c, l, t, l + w, t + h, rad, pha(m, 0xFFFFFFFF, 0.15f), m);
        return true;
    }

    /** Ô đang chọn 0.9.5: vàng nhạt viền cam (cả nền sáng lẫn thẻ nhấn). */
    public static boolean chon(Graphics g, int x, int y, int w, int h) {
        return chonSang(g, x, y, w, h);
    }

    public static boolean chonSang(Graphics g, int x, int y, int w, int h) {
        ghiNen(g, x, y, w, h, 220);
        Canvas c = cv(g);
        float l = x + g.getTranslateX();
        float t = y + g.getTranslateY();
        float rad = Math.min(6, h / 2f);
        tronDoc(c, l, t, l + w, t + h, rad, 0xFFFFFBEA, argb(game.pokemon.ChuDe.VANG_NHAT));
        vienTron(c, l, t, l + w, t + h, rad, 1.1f, argb(game.pokemon.ChuDe.CAM));
        return true;
    }

    /** Thanh HP / PP / EXP: viên bo tròn, nền tối mờ, thân dải màu + ánh. */
    public static boolean thanh(Graphics g, int x, int y, int w, int h, long gt, long max, int loai) {
        if (w < 4 || h < 3) return true;
        if (gt < 0) gt = 0;
        if (max > 0 && gt > max) gt = max;
        Canvas c = cv(g);
        float l = x + g.getTranslateX();
        float t = y + g.getTranslateY();
        veThanh(c, l, t, w, h, max <= 0 ? 0f : gt / (float) max, mauThanh(loai, gt, max));
        return true;
    }

    static int mauThanh(int loai, long gt, long max) {
        if (loai == 0) {
            long p = max <= 0 ? 100 : gt * 100 / max;
            return argb(p > 50 ? game.pokemon.ChuDe.HP_XANH : p > 20 ? game.pokemon.ChuDe.HP_VANG : game.pokemon.ChuDe.HP_DO);
        }
        return argb(loai == 1 ? game.pokemon.ChuDe.PP : game.pokemon.ChuDe.EXP);
    }

    static void veThanh(Canvas c, float l, float t, float w, float h, float tiLe, int mau) {
        float rad = h / 2f;
        tron(c, l, t, l + w, t + h, rad, 0xE0000000 | game.pokemon.ChuDe.RANH);
        float dai = (w - 1.2f) * Math.max(0f, Math.min(1f, tiLe));
        if (dai > 0) {
            if (dai < h - 1.2f) dai = Math.min(w - 1.2f, h - 1.2f);
            tronDoc(c, l + 0.6f, t + 0.6f, l + 0.6f + dai, t + h - 0.6f, rad - 0.6f, pha(mau, 0xFFFFFFFF, 0.35f), pha(mau, 0xFF000000, 0.12f));
            if (h >= 4) tron(c, l + 1.5f, t + 1f, l + 0.6f + dai - 1f, t + Math.max(1.6f, h * 0.32f), h * 0.2f, 0x50FFFFFF);
        }
        vienTron(c, l, t, l + w, t + h, rad, 0.6f, 0x50000000 | game.pokemon.ChuDe.VIEN);
    }

    /** Viên (pill) màu: dải màu + viền + chữ (chữ HD). */
    public static boolean vien(Graphics g, int x, int y, int w, int h, int mau, String chu) {
        ghiNen(g, x, y, w, h, Math.min(120, doSang(mau)));
        Canvas c = cv(g);
        float l = x + g.getTranslateX();
        float t = y + g.getTranslateY();
        float rad = h / 2f;
        bong(c, l, t, l + w, t + h, rad, 0.8f);
        tronDoc(c, l, t, l + w, t + h, rad, pha(argb(mau), 0xFFFFFFFF, 0.3f), pha(argb(mau), 0xFF000000, 0.15f));
        tronDoc(c, l + 1, t + 0.6f, l + w - 1, t + h * 0.5f, rad, 0x38FFFFFF, 0x00FFFFFF);
        vienTron(c, l, t, l + w, t + h, rad, 0.8f, 0xC0101830);
        game.render.screen.Font f = game.render.screen.Font.borderFont;
        if (chu != null && f != null) {
            f.drawString(g, chu, x + w / 2, y + (h - f.getHeight()) / 2 + 1, game.render.screen.Font.CENTER);
        }
        return true;
    }

    public static boolean muiTab(Graphics g, int x, int yTam, boolean trai) {
        Canvas c = cv(g);
        float cx = x + g.getTranslateX();
        float cy = yTam + g.getTranslateY();
        float d = trai ? 1 : -1;
        // nút tròn nhỏ có mũi tên
        float mx = cx + 4.5f * d;
        tron(c, mx - 7, cy - 7, mx + 7, cy + 7, 7, 0xE01E2A44);
        tronDoc(c, mx - 6, cy - 6, mx + 6, cy + 6, 6, 0xFFFFE066, 0xFFF0A800);
        PATH.reset();
        PATH.moveTo(mx - 2.2f * d, cy);
        PATH.lineTo(mx + 2f * d, cy - 3.4f);
        PATH.lineTo(mx + 2f * d, cy + 3.4f);
        PATH.close();
        P.setShader(null);
        P.setColor(VIEN);
        c.drawPath(PATH, P);
        return true;
    }

    /** Poké Ball vector (dải màu đỏ, ánh sáng, đai, nút giữa). */
    public static boolean pokeBall(Graphics g, int cx, int cy, int r, int goc) {
        if (r < 3) r = 3;
        Canvas c = cv(g);
        float x = cx + g.getTranslateX() + 0.5f;
        float y = cy + g.getTranslateY() + 0.5f;
        veBong(c, x, y, r, goc);
        return true;
    }

    static void veBong(Canvas c, float x, float y, float r, int goc) {
        P.setShader(null);
        P.setColor(VIEN);
        c.drawCircle(x, y, r + 0.9f, P);
        int sv = c.save();
        c.rotate(-goc, x, y);
        R.set(x - r, y - r, x + r, y + r);
        P.setShader(doc(0xFFFF6A47, 0xFFC8200A, y - r, y));
        c.drawArc(R, 180, 180, true, P);
        P.setShader(doc(0xFFFFFFFF, 0xFFD5DCE6, y, y + r));
        c.drawArc(R, 0, 180, true, P);
        P.setShader(null);
        P.setColor(0x70FFFFFF);
        c.drawCircle(x - r * 0.38f, y - r * 0.45f, r * 0.22f, P);
        P.setColor(VIEN);
        float dai = Math.max(1.1f, r * 0.2f);
        c.drawRect(x - r, y - dai / 2, x + r, y + dai / 2, P);
        float rb = Math.max(1.8f, r * 0.36f);
        c.drawCircle(x, y, rb + 0.8f, P);
        P.setColor(0xFFFFFFFF);
        c.drawCircle(x, y, rb, P);
        PV.setColor(0xFFB4BDCA);
        PV.setStrokeWidth(0.5f);
        c.drawCircle(x, y, rb * 0.6f, PV);
        c.restoreToCount(sv);
    }

    // ------------------------------------------------------------------ banner máy tính bảng
    private static android.graphics.Bitmap bitmapTroi;
    private static int mauTroi = 0xFF12163D;

    /** Kéo dài 2 hàng trên cùng của cảnh banner lên phần trống phía trên (trời liền mạch) + vài sao. */
    public static void troiBanner(Graphics g, javax.microedition.lcdui.Image nen, int ox, int oy, int w) {
        if (nen == null || oy <= 0) return;
        Canvas c = cv(g);
        android.graphics.Bitmap b = nen.getBitmap();
        float tx = g.getTranslateX(), ty = g.getTranslateY();
        if (b != bitmapTroi) {
            // màu trung bình mép trên cảnh -> dải trời mượt (không kéo sọc chi tiết)
            bitmapTroi = b;
            int[] px = new int[b.getWidth() * 3];
            b.getPixels(px, 0, b.getWidth(), 0, 0, b.getWidth(), 3);
            long r = 0, gg = 0, bb = 0;
            for (int v : px) {
                r += v >> 16 & 255;
                gg += v >> 8 & 255;
                bb += v & 255;
            }
            int n = px.length;
            mauTroi = 0xFF000000 | (int) (r / n) << 16 | (int) (gg / n) << 8 | (int) (bb / n);
        }
        P.setShader(doc(pha(mauTroi, 0xFF060A24, 0.55f), mauTroi, ty, ty + oy + 1));
        P.setColor(0xFF000000);
        c.drawRect(tx, ty, tx + w, ty + oy + 1, P);
        P.setShader(null);
        // sao lấp lánh
        long now = System.currentTimeMillis();
        for (int i = 0; i < 26; i++) {
            float x = (i * 97 % Math.max(1, w)), y = (i * 53 % Math.max(1, oy));
            float a = 0.5f + 0.5f * (float) Math.sin(now / 600.0 + i * 1.3);
            P.setColor(((int) (a * 200)) << 24 | 0xFFF6D8);
            c.drawCircle(tx + x, ty + y, i % 5 == 0 ? 1.1f : 0.6f, P);
        }
    }

    // ------------------------------------------------------------------ thanh phím mềm

    /** Ảnh thanh phím mềm (Res.createTab -> imgCmdBar): dải tối mờ dần lên trên thay thanh navy viền đỏ. */
    public static boolean thanhPhim(Graphics g, int w, int h) {
        game.render.screen.Font.nenSang = false;
        Canvas c = cv(g);
        c.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        // không còn thanh phím mềm: chỉ một dải tối rất nhẹ cho ô chat / tin chạy chữ đọc rõ
        P.setShader(doc(0x000A142C, 0x900A142C, 0, h));
        P.setColor(0xFF000000);
        c.drawRect(0, 0, w, h, P);
        P.setShader(null);
        return true;
    }

    private static final Paint PT = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private static final Paint PTV = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

    /** Chữ phím mềm (Main.paint) -> nút viên kính ở đáy màn; canh 0 trái, 2 giữa, 1 phải. */
    public static void nutMem(Graphics g, String chu, int canh) {
        // lệnh phím mềm nay là nút theo ngữ cảnh (HdNut, vẽ trên lớp phủ): không vẽ chữ ở đáy màn nữa
        if (Hud.BAT) return;
        if (chu == null || chu.length() == 0) return;
        chu = game.pokemon.ChuPokemon.D(chu);
        boolean phong = g.coPhongMan();
        if (phong) g.tamTatPhong(true);
        Canvas c = cv(g);
        int W = game.render.Canvas.w, H = game.render.Canvas.h;
        PT.setTypeface(HdChu.typeface(1));
        PT.setTextSize(9.5f);
        float tw = PT.measureText(chu);
        float w = Math.max(44, tw + 22), h = 20;
        float x0;
        int l = javax.microedition.lcdui.NenAndroid.anToanTrai, r = javax.microedition.lcdui.NenAndroid.anToanPhai;
        if (canh == 0) x0 = Math.max(4, l + 2);
        else if (canh == 1) x0 = W - Math.max(4, r + 2) - w;
        else x0 = (W - w) / 2f;
        float y0 = H - h - 3 - javax.microedition.lcdui.NenAndroid.anToanDuoi;
        float tx = g.getTranslateX(), ty = g.getTranslateY();
        x0 -= tx;
        y0 -= ty;
        x0 += 0;
        float L = x0 + tx, T = y0 + ty;
        boolean chinh = canh == 2;
        bong(c, L, T, L + w, T + h, h / 2, 1f);
        if (chinh) tronDoc(c, L, T, L + w, T + h, h / 2, DO_SANG, 0xFFB8260A);
        else tronDoc(c, L, T, L + w, T + h, h / 2, 0xE0284C82, 0xE8122446);
        tronDoc(c, L + 1, T + 0.6f, L + w - 1, T + h * 0.5f, h / 2, 0x38FFFFFF, 0x00FFFFFF);
        vienTron(c, L, T, L + w, T + h, h / 2, 0.8f, chinh ? 0xC0FFE0D0 : 0x80A9C8FF);
        PT.setColor(0xFFFFFFFF);
        PT.setTextAlign(Paint.Align.CENTER);
        c.drawText(chu, L + w / 2, T + h / 2 + 3.4f, PT);
        if (phong) g.tamTatPhong(false);
    }

    /** Vùng chạm phím mềm rộng hơn cho ngón tay (thay Canvas.collisionCmdBar 50 px). */
    public static int vungPhimMem() {
        if (!HdNut.dungPhimMemJar()) return -1;   // vùng phím mềm ẩn: chỉ còn ô chat dùng
        int W = game.render.Canvas.w, H = game.render.Canvas.h;
        int hb = 2 * game.render.screen.Screen.deltaY + 8;
        int wz = Math.max(50, W * 22 / 100);
        if (game.render.Canvas.isPointer(0, H - hb, wz, hb)) return 0;
        if (game.render.Canvas.isPointer(W / 2 - wz / 2, H - hb, wz, hb)) return 1;
        if (game.render.Canvas.isPointer(W - wz, H - hb, wz, hb)) return 2;
        return -1;
    }

    // ------------------------------------------------------------------ thay KieuO

    /** Phủ màu alpha (bản jar tạo ảnh RGB theo cỡ ô) -> tô thẳng màu ARGB. */
    public static boolean phuCat(Graphics g, int x, int y, int w, int hDu, int h, int argb) {
        if (w <= 0 || h <= 0 || hDu <= 0) return true;
        if (h > hDu) h = hDu;
        Canvas c = cv(g);
        float l = x + g.getTranslateX();
        float t = y + g.getTranslateY();
        P.setShader(null);
        P.setColor(argb);
        c.drawRect(l, t, l + w, t + h, P);
        return true;
    }

    // ------------------------------------------------------------------

    /** Màn trước khi vào game (máy chủ, đăng nhập, đăng ký, chọn / tạo): thẻ to hơn, giữ logo phía trên. */
    static void phongManTruocGame(Graphics g, int x, int y, int w, int h) {
        Object m = game.render.Canvas.currentScreen;
        if (m == null || m == game.render.Canvas.gameScr || g.coPhongMan()) return;
        // màn HD mới (bảng chung game.pokemon.Bang) tự bố cục theo cả màn: không phóng như thẻ màn trước game
        if (m instanceof game.pokemon.ManNhanVat || m instanceof game.pokemon.ManMenu || m instanceof game.pokemon.ManBang
                || m instanceof game.pokemon.ManCuaHang || m instanceof game.pokemon.ManNhiemVu || m instanceof game.pokemon.ManGiaoDich
                || m instanceof game.pokemon.ManDoi) return;
        HdPhong.batDau(g, m, x, y, w, h, 1.6f, true);
    }

    static void moClip(Graphics g) {
        game.pokemon.GiaoDien.moClip(g);
    }
}
