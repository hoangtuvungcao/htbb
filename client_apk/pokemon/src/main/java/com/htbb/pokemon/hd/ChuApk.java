package com.htbb.pokemon.hd;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import java.util.HashMap;
import javax.microedition.lcdui.Graphics;

/**
 * Chữ giao diện + biểu tượng vector 0.9.5 (game.pokemon.ChuUI) của bản APK: Be Vietnam Pro (vừa / đậm / rất đậm) đúng
 * cỡ px logic, khử răng cưa; biểu tượng BieuTuong vẽ bằng Path Android. Một luồng vẽ: Paint / Path dùng chung.
 */
public final class ChuApk extends game.pokemon.ChuUI {
    static final ChuApk I = new ChuApk();
    private final Paint chu = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint to = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint net = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path duong = new Path();
    private final RectF rf = new RectF();
    @SuppressWarnings("unchecked")
    private final HashMap<String, Integer>[] rong = new HashMap[4 * 512];

    private ChuApk() {
        net.setStyle(Paint.Style.STROKE);
        net.setStrokeCap(Paint.Cap.ROUND);
        net.setStrokeJoin(Paint.Join.ROUND);
    }

    static void bat() {
        game.pokemon.ChuUI.bo = I;
    }

    private void dat(float co, int dam) {
        chu.setTypeface(HdChu.typeface(dam));
        chu.setTextSize(co * 1.08f);
    }

    public int cao(float co) {
        return (int) Math.ceil(co * 1.42f);
    }

    public int rong(String s, float co, int dam) {
        int k = Math.max(1, Math.min(511, Math.round(co * 10))) * 4 + Math.max(0, Math.min(3, dam));
        HashMap<String, Integer> m = rong[k];
        if (m == null) {
            m = new HashMap<String, Integer>();
            rong[k] = m;
        }
        Integer v = m.get(s);
        if (v != null) return v.intValue();
        dat(co, dam);
        int r = (int) Math.ceil(chu.measureText(s));
        if (m.size() > 3000) m.clear();
        m.put(s, Integer.valueOf(r));
        return r;
    }

    public void ve(Graphics g, String s, int x, int y, float co, int dam, int mau, int canh) {
        dat(co, dam);
        chu.setColor(0xFF000000 | mau);
        chu.setTextAlign(canh == 0 ? Paint.Align.LEFT : canh == 1 ? Paint.Align.RIGHT : Paint.Align.CENTER);
        float by = y + g.getTranslateY() + cao(co) * 0.5f + co * 0.38f;
        g.androidCanvas().drawText(s, x + g.getTranslateX(), by, chu);
    }

    /** 0.9.7: nền mảnh pixel (KhungNro) -> danh sách nền để chữ game cũ tự tương phản. */
    public void nen(Graphics g, int x, int y, int w, int h, int sang) {
        HdVe.ghiNen(g, x, y, w, h, sang);
    }

    public boolean net(Graphics g, int cx, int cy, int k, int mau, int kieu) {
        Canvas c = g.androidCanvas();
        net.setColor(0xFF000000 | mau);
        net.setStrokeWidth(Math.max(1f, k * 0.35f));
        float x = cx + g.getTranslateX() + 0.5f, y = cy + g.getTranslateY() + 0.5f;
        if (kieu == 0) {
            c.drawLine(x - k, y - k, x + k, y + k, net);
            c.drawLine(x + k, y - k, x - k, y + k, net);
        } else if (kieu == 1) {
            c.drawLine(x + k * 0.5f, y - k, x - k * 0.5f, y, net);
            c.drawLine(x - k * 0.5f, y, x + k * 0.5f, y + k, net);
        } else {
            c.drawLine(x - k, y, x + k, y, net);
            c.drawLine(x, y - k, x, y + k, net);
        }
        return true;
    }

    // ---- 0.9.5: ảnh PNG giao diện (game.pokemon.AnhUI, res/ui) - tô màu + thu phóng một lần, nhớ theo cỡ thật ----
    private int[] tamIcon, tamNut;
    private int tamIconRong, tamNutRong;
    private boolean daNap;
    private final HashMap<Long, AnhNho> anhUI = new HashMap<Long, AnhNho>();
    private final Paint loc = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private final Rect rs = new Rect(), rd = new Rect();

    static final class AnhNho {
        long khoa2;
        Bitmap img;
        AnhNho sau;
    }

    private static int[] doc(String ten, int[] rong) {
        try {
            Bitmap b = javax.microedition.lcdui.Image.createImage(ten).getBitmap();
            int w = b.getWidth(), h = b.getHeight();
            int[] p = new int[w * h];
            b.getPixels(p, 0, w, 0, 0, w, h);
            rong[0] = w;
            return p;
        } catch (Throwable e) {
            return null;
        }
    }

    private void nap() {
        if (daNap) return;
        daNap = true;
        int[] r = new int[1];
        tamIcon = doc(game.pokemon.AnhUI.TAM_ICON, r);
        tamIconRong = r[0];
        tamNut = doc(game.pokemon.AnhUI.TAM_NUT, r);
        tamNutRong = r[0];
    }

    /** Ô o (O x O) của tấm, đã tô màu; null = tấm thiếu / ô ngoài tấm. */
    private static Bitmap o(int[] tam, int rong, int o, int mau, int vien) {
        int n = game.pokemon.AnhUI.O, cot = rong / n;
        if (tam == null || cot <= 0) return null;
        int ox = (o % cot) * n, oy = (o / cot) * n;
        if (oy + n > tam.length / rong) return null;
        int[] p = new int[n * n];
        for (int y = 0; y < n; y++) System.arraycopy(tam, (oy + y) * rong + ox, p, y * n, n);
        game.pokemon.AnhUI.to(p, p.length, mau, vien);
        return Bitmap.createBitmap(p, n, n, Bitmap.Config.ARGB_8888);
    }

    /** Thu phóng lọc (thu nhiều bước một nửa để không răng cưa). */
    private static Bitmap thu(Bitmap b, int w, int h) {
        while (b.getWidth() / 2 >= w && b.getHeight() / 2 >= h) b = Bitmap.createScaledBitmap(b, b.getWidth() / 2, b.getHeight() / 2, true);
        return b.getWidth() == w && b.getHeight() == h ? b : Bitmap.createScaledBitmap(b, w, h, true);
    }

    private Bitmap nho(long k1, long k2) {
        for (AnhNho a = anhUI.get(k1); a != null; a = a.sau) if (a.khoa2 == k2) return a.img;
        return null;
    }

    private void giu(long k1, long k2, Bitmap img) {
        if (anhUI.size() > 600) anhUI.clear();
        AnhNho a = new AnhNho();
        a.khoa2 = k2;
        a.img = img;
        a.sau = anhUI.get(k1);
        anhUI.put(k1, a);
    }

    private void veAnh(Graphics g, Bitmap b, int x, int y, int w, int h) {
        float ox = x + g.getTranslateX(), oy = y + g.getTranslateY();
        rf.set(ox, oy, ox + w, oy + h);
        g.androidCanvas().drawBitmap(b, null, rf, loc);
    }

    /** Icon PNG ô o cạnh s (logic) tại (x, y). false = không có ảnh. */
    private boolean veIcon(Graphics g, int o, int x, int y, int s, int chinh, int nen) {
        nap();
        if (tamIcon == null) return false;
        int px = Math.max(4, Math.min(1023, Math.round(s * javax.microedition.lcdui.NenAndroid.tiLe)));
        int mau = game.pokemon.AnhUI.mau(o, chinh), vien = game.pokemon.AnhUI.vien(o, chinh, nen);
        long k1 = (long) o << 40 | (long) px << 24 | mau, k2 = vien;
        Bitmap img = nho(k1, k2);
        if (img == null) {
            Bitmap b = o(tamIcon, tamIconRong, o, mau, vien);
            if (b == null) return false;
            img = thu(b, px, px);
            giu(k1, k2, img);
        }
        veAnh(g, img, x, y, s, s);
        return true;
    }

    /** Da nút 9-slice kiểu k, cỡ w x h (logic), màu chinh, viền vien. */
    private boolean veNut(Graphics g, int k, int x, int y, int w, int h, int chinh, int vien) {
        nap();
        if (tamNut == null || w <= 0 || h <= 0) return false;
        float tl = javax.microedition.lcdui.NenAndroid.tiLe;
        int pw = Math.max(2, Math.min(4095, Math.round(w * tl))), ph = Math.max(2, Math.min(4095, Math.round(h * tl)));
        long k1 = 1L << 62 | (long) k << 48 | (long) pw << 24 | chinh, k2 = (long) ph << 24 | vien;
        Bitmap img = nho(k1, k2);
        if (img == null) {
            Bitmap b = o(tamNut, tamNutRong, k, chinh, vien);
            if (b == null) return false;
            if (k >= game.pokemon.AnhUI.NUT_TRON) {
                img = thu(b, pw, ph);
            } else {
                int n = game.pokemon.AnhUI.O, c = game.pokemon.AnhUI.GOC_NUT;
                int d = Math.max(2, Math.min(Math.round(10 * tl), Math.min(pw, ph) / 2));
                Bitmap bg = d * 2 <= c ? thu(b, n * d / c, n * d / c) : b;
                int sn = bg.getWidth(), sc = sn * c / n;
                img = Bitmap.createBitmap(pw, ph, Bitmap.Config.ARGB_8888);
                Canvas c2 = new Canvas(img);
                int[] dx = {0, d, pw - d, pw}, dy = {0, d, ph - d, ph}, sx = {0, sc, sn - sc, sn};
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (dx[j + 1] > dx[j] && dy[i + 1] > dy[i]) {
                            rs.set(sx[j], sx[i], sx[j + 1], sx[i + 1]);
                            rd.set(dx[j], dy[i], dx[j + 1], dy[i + 1]);
                            c2.drawBitmap(bg, rs, rd, loc);
                        }
                    }
                }
            }
            giu(k1, k2, img);
        }
        veAnh(g, img, x, y, w, h);
        return true;
    }

    // ---- icon chiêu (res/ui/chieu_*.png, đã tô màu sẵn): chọn tấm nhỏ nhất >= cỡ thật, thu phóng một lần ----
    private final int[][] tamChieu = new int[6][];
    private final int[] tamChieuRong = new int[6];
    private final boolean[] daNapChieu = new boolean[6];

    public boolean chieu(Graphics g, int o, boolean tron, int x, int y, int s, boolean khoa) {
        if (o < 0 || s <= 0) return false;
        int px = Math.max(4, Math.min(1023, Math.round(s * javax.microedition.lcdui.NenAndroid.tiLe)));
        int[] co = game.pokemon.AnhUI.CO_CHIEU;
        int t = co.length - 1;
        for (int i = 0; i < co.length; i++) if (co[i] >= px) { t = i; break; }
        // 0.9.6 icon pixel-art: cỡ thật là bội số nguyên của một tấm -> phóng LÁNG GIỀNG GẦN NHẤT (mép sắc)
        boolean ganNhat = false;
        for (int i = co.length - 1; i >= 0; i--) if (px >= co[i] && px % co[i] == 0) { t = i; ganNhat = true; break; }
        long k1 = 3L << 61 | (long) o << 40 | (long) px << 8 | (tron ? 2 : 0) | (khoa ? 1 : 0), k2 = 0;
        Bitmap img = nho(k1, k2);
        if (img == null) {
            int[] tam = null;
            int n = 0, rong = 0;
            for (int i = t; i >= 0 && tam == null; i--) {
                int j = (tron ? 3 : 0) + i;
                if (!daNapChieu[j]) {
                    daNapChieu[j] = true;
                    int[] r = new int[1];
                    tamChieu[j] = doc(game.pokemon.AnhUI.tamChieu(tron, co[i]), r);
                    tamChieuRong[j] = r[0];
                }
                tam = tamChieu[j];
                rong = tamChieuRong[j];
                n = co[i];
            }
            if (tam == null) return false;
            int cot = rong / n, ox = (o % cot) * n, oy = (o / cot) * n;
            if (cot <= 0 || oy + n > tam.length / rong) return false;
            int[] p = new int[n * n];
            for (int yy = 0; yy < n; yy++) System.arraycopy(tam, (oy + yy) * rong + ox, p, yy * n, n);
            boolean rong0 = true;
            for (int i = 0; i < p.length && rong0; i++) if ((p[i] >>> 24) != 0) rong0 = false;
            if (rong0) return false;
            if (khoa) {
                for (int i = 0; i < p.length; i++) {
                    int c = p[i], a = c >>> 24;
                    int l = (((c >> 16) & 255) * 3 + ((c >> 8) & 255) * 6 + (c & 255)) / 10;
                    l = 120 + l * 2 / 5;
                    p[i] = (a * 3 / 4) << 24 | l << 16 | l << 8 | l;
                }
            }
            Bitmap bm = Bitmap.createBitmap(p, n, n, Bitmap.Config.ARGB_8888);
            // 0.9.6 icon pixel-art: bội số nguyên -> phóng láng giềng gần nhất (filter = false), mép sắc
            img = ganNhat ? (px == n ? bm : Bitmap.createScaledBitmap(bm, px, px, false)) : thu(bm, px, px);
            giu(k1, k2, img);
        }
        veAnh(g, img, x, y, s, s);
        return true;
    }

    public boolean hinh(Graphics g, float[] h, int x, int y, int s, int chinh, int nen) {
        int o = game.pokemon.AnhUI.o(h);
        if (o >= 0 && veIcon(g, o, x, y, s, chinh, nen)) return true;
        int kn = game.pokemon.AnhUI.nut(h);
        if (kn >= 0) return veNut(g, kn, x, y, s >>> 16, s & 0xFFFF, chinh, nen);
        if (o == 0) return false;
        Canvas c = g.androidCanvas();
        float ox = x + g.getTranslateX(), oy = y + g.getTranslateY(), k = s / 100f;
        int i = 0;
        int mau = 0xFF000000 | chinh;
        while (i < h.length) {
            int lenh = (int) h[i++];
            switch (lenh) {
                case game.pokemon.BieuTuong.MAU:
                    mau = 0xFF000000 | mauLenh((int) h[i++], chinh, nen);
                    to.setColor(mau);
                    net.setColor(mau);
                    break;
                case game.pokemon.BieuTuong.TRON:
                    to.setColor(mau);
                    c.drawCircle(ox + h[i] * k, oy + h[i + 1] * k, h[i + 2] * k, to);
                    i += 3;
                    break;
                case game.pokemon.BieuTuong.CHU_NHAT:
                    to.setColor(mau);
                    rf.set(ox + h[i] * k, oy + h[i + 1] * k, ox + (h[i] + h[i + 2]) * k, oy + (h[i + 1] + h[i + 3]) * k);
                    c.drawRoundRect(rf, h[i + 4] * k, h[i + 4] * k, to);
                    i += 5;
                    break;
                case game.pokemon.BieuTuong.NET:
                    net.setColor(mau);
                    net.setStrokeWidth(h[i + 4] * k);
                    c.drawLine(ox + h[i] * k, oy + h[i + 1] * k, ox + h[i + 2] * k, oy + h[i + 3] * k, net);
                    i += 5;
                    break;
                case game.pokemon.BieuTuong.VONG:
                    net.setColor(mau);
                    net.setStrokeWidth(h[i + 3] * k);
                    c.drawCircle(ox + h[i] * k, oy + h[i + 1] * k, h[i + 2] * k, net);
                    i += 4;
                    break;
                case game.pokemon.BieuTuong.CUNG: {
                    float r = h[i + 2];
                    rf.set(ox + (h[i] - r) * k, oy + (h[i + 1] - r) * k, ox + (h[i] + r) * k, oy + (h[i + 1] + r) * k);
                    // J2ME / Java2D: góc ngược chiều kim đồng hồ; Android: cùng chiều -> đổi dấu
                    if (h[i + 5] <= 0) {
                        to.setColor(mau);
                        c.drawArc(rf, -h[i + 3], -h[i + 4], true, to);
                    } else {
                        net.setColor(mau);
                        net.setStrokeWidth(h[i + 5] * k);
                        c.drawArc(rf, -h[i + 3], -h[i + 4], false, net);
                    }
                    i += 6;
                    break;
                }
                case game.pokemon.BieuTuong.DA_GIAC:
                case game.pokemon.BieuTuong.DUONG: {
                    int n = (int) h[i++];
                    float w = lenh == game.pokemon.BieuTuong.DUONG ? h[i++] : 0;
                    duong.reset();
                    for (int q = 0; q < n; q++) {
                        float px = ox + h[i + 2 * q] * k, py = oy + h[i + 2 * q + 1] * k;
                        if (q == 0) duong.moveTo(px, py);
                        else duong.lineTo(px, py);
                    }
                    if (lenh == game.pokemon.BieuTuong.DA_GIAC) {
                        duong.close();
                        to.setColor(mau);
                        c.drawPath(duong, to);
                    } else {
                        net.setColor(mau);
                        net.setStrokeWidth(w * k);
                        c.drawPath(duong, net);
                    }
                    i += 2 * n;
                    break;
                }
                default:
                    i = h.length;
                    break;
            }
        }
        return true;
    }

    static int mauLenh(int m, int chinh, int nen) {
        switch (m) {
            case game.pokemon.BieuTuong.M_TRANG:
                return 0xFFFFFF;
            case game.pokemon.BieuTuong.M_TOI:
                return game.pokemon.GiaoDien.pha(chinh, 0, 1, 4);
            case game.pokemon.BieuTuong.M_NHAT:
                return game.pokemon.GiaoDien.pha(chinh, 0xFFFFFF, 2, 5);
            case game.pokemon.BieuTuong.M_NEN:
                return nen;
            default:
                return chinh;
        }
    }
}
