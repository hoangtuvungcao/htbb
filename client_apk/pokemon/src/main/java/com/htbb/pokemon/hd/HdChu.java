package com.htbb.pokemon.hd;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import java.util.HashMap;
import java.util.IdentityHashMap;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.NenAndroid;

/**
 * Chữ HD cho bản APK: mọi chữ game vẽ qua font ẢNH của bản jar (game.render.screen.Font.drawString /
 * getWidthNguyenVan) được port-j2me.py nối sang đây -> vẽ bằng font vector Be Vietnam Pro (SIL OFL 1.1,
 * assets/fonts, đủ tiếng Việt: không còn chữ hoa có dấu ghép / thay chữ). Chữ là vector nên nét ở độ
 * phân giải thật của máy (canvas đã scale hệ số).
 *
 * Kiểu chữ (màu thân, viền, cỡ, đậm) suy ra từ chính ảnh font của bản jar: lấy màu nhiều nhất làm thân,
 * màu tương phản sát mép trong suốt làm viền -> mỗi font màu (normalFont[0..5], borderFont, smallFontColor...)
 * giữ đúng màu như jar. Bề rộng chữ có bộ nhớ đệm (không đo lại mỗi khung).
 */
public final class HdChu {
    /** Bật chữ HD (port-j2me.py chỉ nối khi build APK; bản jar không có lớp này). */
    public static boolean bat = true;

    private static Typeface tfVua;
    private static Typeface tfDam;
    private static Typeface tfDamHon;
    /** 0.9.6: phông hiển thị tròn đậm (Baloo 2, SIL OFL) cho tiêu đề - dam 3 */
    private static Typeface tfTieuDe;
    private static boolean daNapFont;
    private static final IdentityHashMap<Object, Kieu> KIEU = new IdentityHashMap<Object, Kieu>();
    private static final Paint BONG = new Paint(Paint.ANTI_ALIAS_FLAG);

    private HdChu() {
    }

    static final class Kieu {
        final Paint than = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        Paint vien;
        Paint bong;
        /** bản navy cho chữ sáng trên bảng sáng (Font.nenSang của jar) */
        Paint toi;
        /** bản trắng cho chữ tối trên nền tối */
        Paint trang;
        /** 1 = màu thân sáng */
        int sang;
        float baseline;
        final HashMap<String, Integer> rong = new HashMap<String, Integer>();
    }

    static synchronized void napFont() {
        if (daNapFont) return;
        daNapFont = true;
        try {
            android.content.res.AssetManager am = NenAndroid.ungDung.getAssets();
            tfVua = Typeface.createFromAsset(am, "fonts/BeVietnamPro-SemiBold.ttf");
            tfDam = Typeface.createFromAsset(am, "fonts/BeVietnamPro-Bold.ttf");
            tfDamHon = Typeface.createFromAsset(am, "fonts/BeVietnamPro-ExtraBold.ttf");
            try {
                tfTieuDe = new Typeface.Builder(am, "fonts/Baloo2-wght.ttf").setFontVariationSettings("'wght' 700").build();
            } catch (Throwable e2) {
                tfTieuDe = null;
            }
            if (tfTieuDe == null) tfTieuDe = tfDamHon;
        } catch (Throwable e) {
            Log.e(NenAndroid.TAG, "khong nap duoc font HD", e);
            tfVua = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
            tfDam = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
            tfDamHon = tfDam;
            tfTieuDe = tfDam;
        }
    }

    /** Typeface dùng cho HUD / bảng HD (0 = vừa, 1 = đậm, 2 = rất đậm, 3 = tiêu đề tròn Baloo 2). */
    public static Typeface typeface(int dam) {
        napFont();
        return dam >= 3 ? tfTieuDe : dam == 2 ? tfDamHon : dam == 1 ? tfDam : tfVua;
    }

    private static int doSang(int c) {
        return ((c >> 16 & 255) * 299 + (c >> 8 & 255) * 587 + (c & 255) * 114) / 1000;
    }

    /** Suy kiểu chữ từ ảnh font jar (dải ký tự dọc, mỗi ký tự cao charH). */
    private static Kieu taoKieu(Image img, int charH) {
        napFont();
        Kieu k = new Kieu();
        int mauThan = 0xFFFFFF;
        int mauVien = -1;
        try {
            if (img != null) {
                Bitmap b = img.getBitmap();
                int w = b.getWidth();
                int h = Math.min(b.getHeight(), charH * 60);
                int[] p = new int[w * h];
                b.getPixels(p, 0, w, 0, 0, w, h);
                HashMap<Integer, int[]> dem = new HashMap<Integer, int[]>();  // màu -> {số điểm, số điểm sát trong suốt}
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int c = p[y * w + x];
                        if ((c >>> 24) < 200) continue;
                        int q = c & 0xF8F8F8;
                        int[] d = dem.get(q);
                        if (d == null) {
                            d = new int[2];
                            dem.put(q, d);
                        }
                        d[0]++;
                        boolean mep = x == 0 || y == 0 || x == w - 1 || y == h - 1
                                || (p[y * w + x - 1] >>> 24) < 100 || (p[y * w + x + 1] >>> 24) < 100
                                || (p[(y - 1) * w + x] >>> 24) < 100 || (p[(y + 1) * w + x] >>> 24) < 100;
                        if (mep) d[1]++;
                    }
                }
                int c1 = -1;
                int n1 = 0;
                for (java.util.Map.Entry<Integer, int[]> e : dem.entrySet()) {
                    if (e.getValue()[0] > n1) {
                        n1 = e.getValue()[0];
                        c1 = e.getKey();
                    }
                }
                int c2 = -1;
                int n2 = 0;
                if (c1 >= 0) {
                    for (java.util.Map.Entry<Integer, int[]> e : dem.entrySet()) {
                        int c = e.getKey();
                        int n = e.getValue()[0];
                        if (c != c1 && n > n2 && Math.abs(doSang(c) - doSang(c1)) > 70 && n * 100 >= n1 * 12) {
                            n2 = n;
                            c2 = c;
                        }
                    }
                }
                if (c1 >= 0) {
                    mauThan = c1 | 0x070707;
                    if (c2 >= 0) {
                        int[] d1 = dem.get(c1);
                        int[] d2 = dem.get(c2);
                        // màu sát mép trong suốt nhiều hơn = viền
                        float t1 = d1[1] / (float) d1[0];
                        float t2 = d2[1] / (float) d2[0];
                        if (t1 > t2 + 0.05f) {
                            mauThan = c2 | 0x070707;
                            mauVien = c1;
                        } else {
                            mauVien = c2;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            Log.w(NenAndroid.TAG, "khong doc duoc anh font", e);
        }
        float co;
        Typeface tf;
        if (charH <= 6) {
            co = 6.6f;
            tf = tfDam;
        } else if (charH <= 9) {
            co = 8.2f;
            tf = tfDam;
        } else if (charH <= 12) {
            co = 9.2f;
            tf = tfDam;
        } else if (charH <= 14) {
            co = 10.6f;
            tf = tfVua;
        } else if (charH <= 16) {
            co = 10.2f;
            tf = tfDam;
        } else {
            co = charH * 0.74f;
            tf = tfDamHon;
        }
        k.than.setTypeface(tf);
        k.than.setTextSize(co);
        k.than.setColor(0xFF000000 | mauThan);
        k.than.setHinting(Paint.HINTING_OFF);
        if (mauVien >= 0) k.than.setLetterSpacing(-0.02f);
        if (charH <= 9) k.than.setLetterSpacing(-0.03f);
        float cap = co * 0.70f;
        k.baseline = (charH + cap) / 2f + (charH >= 13 ? 0.6f : 0.2f);
        if (mauVien >= 0) {
            k.vien = new Paint(k.than);
            k.vien.setStyle(Paint.Style.STROKE);
            k.vien.setStrokeJoin(Paint.Join.ROUND);
            k.vien.setStrokeWidth(charH <= 9 ? 1.3f : 1.8f);
            k.vien.setColor(0xFF000000 | mauVien);
        } else if (doSang(mauThan) > 150) {
            // chữ sáng không viền (trên bảng tối): bóng đổ mềm cho dễ đọc trên nền bản đồ
            k.bong = new Paint(k.than);
            k.bong.setColor(0x99000000);
        }
        k.sang = doSang(mauThan) > 150 ? 1 : 0;
        if (doSang(mauThan) > 150) {
            k.toi = new Paint(k.than);
            // vàng / cam giữ sắc (đậm lại), trắng / xám -> navy
            int r = mauThan >> 16 & 255, gg = mauThan >> 8 & 255, b = mauThan & 255;
            boolean mau = Math.max(r, Math.max(gg, b)) - Math.min(r, Math.min(gg, b)) > 60;
            k.toi.setColor(mau ? 0xFF000000 | ((r * 55 / 100) << 16) | ((gg * 45 / 100) << 8) | (b * 35 / 100) : 0xFF1E2A44);
        }
        return k;
    }

    private static synchronized Kieu kieu(Object font, Image img, int charH) {
        Kieu k = KIEU.get(font);
        if (k == null) {
            k = taoKieu(img, charH);
            KIEU.put(font, k);
        }
        return k;
    }

    /** Thay Font.drawString của jar (chữ đã đổi sang từ ngữ Pokémon như bản jar). */
    public static void ve(Object font, Image img, int charH, Graphics g, String st, int x, int y, int align) {
        if (st == null || g == null) return;
        st = game.pokemon.ChuPokemon.D(st);
        if (st.length() == 0) return;
        Kieu k = kieu(font, img, charH);
        int w = rongKieu(k, st);
        BONG.setTypeface(k.than.getTypeface());
        BONG.setTextSize(k.than.getTextSize());
        BONG.setLetterSpacing(k.than.getLetterSpacing());
        BONG.setColor(0x80000000);
        float x1 = align == 1 ? x - w : align == 2 ? x - (w >> 1) : x;
        x1 += g.getTranslateX();
        float by = y + g.getTranslateY() + k.baseline;
        Canvas c = g.androidCanvas();
        // tương phản tự động: nền dưới tâm chữ (HdVe ghi các khối đã tô); không biết thì theo Font.nenSang của jar
        int nen = HdVe.sangTai(g.manX(x1 - g.getTranslateX() + w / 2f), g.manY(by - g.getTranslateY() - k.than.getTextSize() * 0.35f));
        boolean dungToi = nen >= 0 ? nen > 165 : game.render.screen.Font.nenSang;
        if (nen >= 0 && nen < 130 && k.sang == 0 && k.vien == null) {
            // chữ tối trên nền tối -> trắng có bóng
            if (k.trang == null) {
                k.trang = new Paint(k.than);
                k.trang.setColor(0xFFFFFFFF);
            }
            c.drawText(st, x1 + 0.35f, by + 0.7f, BONG);
            c.drawText(st, x1, by, k.trang);
            return;
        }
        if (dungToi && k.toi != null) {
            if (HdKiem.bat) HdKiem.chu(g, x1 - g.getTranslateX(), by - g.getTranslateY() - k.than.getTextSize() * 0.78f, w, k.than.getTextSize() * 0.98f, st);
            c.drawText(st, x1, by, k.toi);
            return;
        }
        if (HdKiem.bat) HdKiem.chu(g, x1 - g.getTranslateX(), by - g.getTranslateY() - k.than.getTextSize() * 0.78f, w, k.than.getTextSize() * 0.98f, st);
        if (k.bong != null) c.drawText(st, x1 + 0.35f, by + 0.7f, k.bong);
        if (k.vien != null) c.drawText(st, x1, by, k.vien);
        c.drawText(st, x1, by, k.than);
    }

    /** Thay Font.getWidthNguyenVan (đo đúng chữ đưa vào, có bộ nhớ đệm). */
    public static int rong(Object font, Image img, int charH, String st) {
        if (st == null) return 0;
        return rongKieu(kieu(font, img, charH), st);
    }

    private static synchronized int rongKieu(Kieu k, String st) {
        Integer r = k.rong.get(st);
        if (r != null) return r;
        if (k.rong.size() > 2048) k.rong.clear();
        int v = (int) Math.ceil(k.than.measureText(st) + (k.vien != null ? 1 : 0));
        k.rong.put(st, v);
        return v;
    }
}
