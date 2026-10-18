package javax.microedition.lcdui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * javax.microedition.lcdui.Graphics (MIDP 2.0) vẽ lên android.graphics.Canvas.
 * Toạ độ dịch (translate) và vùng cắt (clip) giữ riêng như J2ME: setClip THAY vùng cắt (Android chỉ cắt
 * nhỏ dần nên mỗi lần đổi clip thì restore về trạng thái gốc rồi clipRect lại). Không khử răng cưa, không
 * lọc ảnh: ảnh pixel giữ nét như máy J2ME.
 */
public class Graphics {
    public static final int HCENTER = 1;
    public static final int VCENTER = 2;
    public static final int LEFT = 4;
    public static final int RIGHT = 8;
    public static final int TOP = 16;
    public static final int BOTTOM = 32;
    public static final int BASELINE = 64;
    public static final int SOLID = 0;
    public static final int DOTTED = 1;

    private final Canvas canvas;
    private final int baseSave;
    private final int surfaceW;
    private final int surfaceH;
    private final Paint fill = new Paint();
    private final Paint stroke = new Paint();
    private final Paint bmpPaint = new Paint();
    private final Rect src = new Rect();
    private final Rect dst = new Rect();
    private final RectF rf = new RectF();
    private final Matrix matrix = new Matrix();
    private final float[] mv = new float[9];
    private final Path path = new Path();
    private int tx;
    private int ty;
    private int clipX;
    private int clipY;
    private int clipW;
    private int clipH;
    private int color = 0xFF000000;
    private int strokeStyle = SOLID;
    private Font font = Font.getDefaultFont();

    Graphics(Canvas canvas, int w, int h) {
        this.canvas = canvas;
        this.surfaceW = w;
        this.surfaceH = h;
        this.baseSave = canvas.save();
        fill.setAntiAlias(false);
        fill.setStyle(Paint.Style.FILL);
        stroke.setAntiAlias(false);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(0);
        bmpPaint.setFilterBitmap(false);
        bmpPaint.setAntiAlias(false);
        bmpPaint.setDither(false);
        reset();
    }

    /** Đưa về trạng thái đầu (mỗi khung hình trước Canvas.paint). */
    void reset() {
        tx = 0;
        ty = 0;
        clipX = 0;
        clipY = 0;
        clipW = surfaceW;
        clipH = surfaceH;
        canvas.restoreToCount(baseSave);
        canvas.save();
        canvas.clipRect(0, 0, surfaceW, surfaceH);
        setColor(0);
        strokeStyle = SOLID;
        font = Font.getDefaultFont();
    }

    Canvas getAndroidCanvas() {
        return canvas;
    }

    // ---------- toạ độ / vùng cắt ----------
    public void translate(int x, int y) {
        tx += x;
        ty += y;
    }

    public int getTranslateX() {
        return tx;
    }

    public int getTranslateY() {
        return ty;
    }

    public int getClipX() {
        return clipX - tx;
    }

    public int getClipY() {
        return clipY - ty;
    }

    public int getClipWidth() {
        return clipW;
    }

    public int getClipHeight() {
        return clipH;
    }

    public void setClip(int x, int y, int w, int h) {
        int x0 = x + tx;
        int y0 = y + ty;
        int x1 = x0 + Math.max(0, w);
        int y1 = y0 + Math.max(0, h);
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        if (x1 > surfaceW) x1 = surfaceW;
        if (y1 > surfaceH) y1 = surfaceH;
        apClip(x0, y0, x1, y1);
    }

    public void clipRect(int x, int y, int w, int h) {
        int x0 = Math.max(clipX, x + tx);
        int y0 = Math.max(clipY, y + ty);
        int x1 = Math.min(clipX + clipW, x + tx + Math.max(0, w));
        int y1 = Math.min(clipY + clipH, y + ty + Math.max(0, h));
        apClip(x0, y0, x1, y1);
    }

    private void apClip(int x0, int y0, int x1, int y1) {
        if (x1 < x0) x1 = x0;
        if (y1 < y0) y1 = y0;
        clipX = x0;
        clipY = y0;
        clipW = x1 - x0;
        clipH = y1 - y0;
        canvas.restoreToCount(baseSave);
        canvas.save();
        canvas.clipRect(x0, y0, x1, y1);
    }

    // ---------- màu ----------
    public void setColor(int rgb) {
        color = 0xFF000000 | rgb;
        fill.setColor(color);
        stroke.setColor(color);
    }

    public void setColor(int r, int g, int b) {
        setColor(((r & 255) << 16) | ((g & 255) << 8) | (b & 255));
    }

    public void setGrayScale(int v) {
        setColor(v, v, v);
    }

    public int getColor() {
        return color & 0xFFFFFF;
    }

    public int getRedComponent() {
        return (color >> 16) & 255;
    }

    public int getGreenComponent() {
        return (color >> 8) & 255;
    }

    public int getBlueComponent() {
        return color & 255;
    }

    public int getGrayScale() {
        return (getRedComponent() + getGreenComponent() + getBlueComponent()) / 3;
    }

    public int getDisplayColor(int c) {
        return c & 0xFFFFFF;
    }

    public void setStrokeStyle(int s) {
        strokeStyle = s;
    }

    public int getStrokeStyle() {
        return strokeStyle;
    }

    public void setFont(Font f) {
        font = f == null ? Font.getDefaultFont() : f;
    }

    public Font getFont() {
        return font;
    }

    // ---------- hình ----------
    public void fillRect(int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) return;
        x += tx;
        y += ty;
        canvas.drawRect(x, y, x + w, y + h, fill);
    }

    public void drawRect(int x, int y, int w, int h) {
        if (w < 0 || h < 0) return;
        x += tx;
        y += ty;
        // J2ME: viền phủ (w+1) x (h+1) điểm ảnh
        canvas.drawRect(x, y, x + w + 1, y + 1, fill);
        canvas.drawRect(x, y + h, x + w + 1, y + h + 1, fill);
        canvas.drawRect(x, y, x + 1, y + h + 1, fill);
        canvas.drawRect(x + w, y, x + w + 1, y + h + 1, fill);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        x1 += tx;
        y1 += ty;
        x2 += tx;
        y2 += ty;
        if (y1 == y2) {
            int a = Math.min(x1, x2);
            int b = Math.max(x1, x2);
            canvas.drawRect(a, y1, b + 1, y1 + 1, fill);
            return;
        }
        if (x1 == x2) {
            int a = Math.min(y1, y2);
            int b = Math.max(y1, y2);
            canvas.drawRect(x1, a, x1 + 1, b + 1, fill);
            return;
        }
        canvas.drawLine(x1 + 0.5f, y1 + 0.5f, x2 + 0.5f, y2 + 0.5f, stroke);
        canvas.drawRect(x2, y2, x2 + 1, y2 + 1, fill);
    }

    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        path.reset();
        path.moveTo(x1 + tx, y1 + ty);
        path.lineTo(x2 + tx, y2 + ty);
        path.lineTo(x3 + tx, y3 + ty);
        path.close();
        canvas.drawPath(path, fill);
    }

    public void fillArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        if (w <= 0 || h <= 0) return;
        rf.set(x + tx, y + ty, x + tx + w, y + ty + h);
        if (arcAngle >= 360 || arcAngle <= -360) {
            canvas.drawOval(rf, fill);
        } else {
            canvas.drawArc(rf, -startAngle, -arcAngle, true, fill);
        }
    }

    public void drawArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        if (w < 0 || h < 0) return;
        rf.set(x + tx + 0.5f, y + ty + 0.5f, x + tx + w + 0.5f, y + ty + h + 0.5f);
        if (arcAngle >= 360 || arcAngle <= -360) {
            canvas.drawOval(rf, stroke);
        } else {
            canvas.drawArc(rf, -startAngle, -arcAngle, false, stroke);
        }
    }

    public void fillRoundRect(int x, int y, int w, int h, int arcW, int arcH) {
        if (w <= 0 || h <= 0) return;
        rf.set(x + tx, y + ty, x + tx + w, y + ty + h);
        canvas.drawRoundRect(rf, arcW / 2f, arcH / 2f, fill);
    }

    public void drawRoundRect(int x, int y, int w, int h, int arcW, int arcH) {
        if (w < 0 || h < 0) return;
        rf.set(x + tx + 0.5f, y + ty + 0.5f, x + tx + w + 0.5f, y + ty + h + 0.5f);
        canvas.drawRoundRect(rf, arcW / 2f, arcH / 2f, stroke);
    }

    // ---------- chữ (font hệ thống - game dùng font ảnh riêng, đây chỉ để đủ API) ----------
    public void drawString(String s, int x, int y, int anchor) {
        if (s == null) throw new NullPointerException();
        Paint p = font.paint();
        p.setColor(color);
        int w = font.stringWidth(s);
        if ((anchor & HCENTER) != 0) x -= w / 2;
        else if ((anchor & RIGHT) != 0) x -= w;
        float by;
        if ((anchor & BOTTOM) != 0) by = y + ty - font.descent();
        else if ((anchor & BASELINE) != 0) by = y + ty;
        else if ((anchor & VCENTER) != 0) by = y + ty + font.getBaselinePosition() - font.getHeight() / 2f;
        else by = y + ty + font.getBaselinePosition();
        canvas.drawText(s, x + tx, by, p);
    }

    public void drawSubstring(String s, int off, int len, int x, int y, int anchor) {
        drawString(s.substring(off, off + len), x, y, anchor);
    }

    public void drawChar(char c, int x, int y, int anchor) {
        drawString(String.valueOf(c), x, y, anchor);
    }

    public void drawChars(char[] d, int off, int len, int x, int y, int anchor) {
        drawString(new String(d, off, len), x, y, anchor);
    }

    // ---------- ảnh ----------
    public void drawImage(Image img, int x, int y, int anchor) {
        Bitmap b = img.bitmap;
        int w = b.getWidth();
        int h = b.getHeight();
        if ((anchor & HCENTER) != 0) x -= w / 2;
        else if ((anchor & RIGHT) != 0) x -= w;
        if ((anchor & VCENTER) != 0) y -= h / 2;
        else if ((anchor & BOTTOM) != 0) y -= h;
        canvas.drawBitmap(b, x + tx, y + ty, bmpPaint);
    }

    public void drawRegion(Image img, int xs, int ys, int w, int h, int transform, int xd, int yd, int anchor) {
        if (w <= 0 || h <= 0) return;
        Bitmap b = img.bitmap;
        boolean doi = transform >= 4; // ROT90/ROT270/MIRROR_ROT90/MIRROR_ROT270: đổi rộng <-> cao
        int dw = doi ? h : w;
        int dh = doi ? w : h;
        if ((anchor & HCENTER) != 0) xd -= dw / 2;
        else if ((anchor & RIGHT) != 0) xd -= dw;
        if ((anchor & VCENTER) != 0) yd -= dh / 2;
        else if ((anchor & BOTTOM) != 0) yd -= dh;
        xd += tx;
        yd += ty;
        src.set(xs, ys, xs + w, ys + h);
        if (transform == 0) {
            dst.set(xd, yd, xd + w, yd + h);
            canvas.drawBitmap(b, src, dst, bmpPaint);
            return;
        }
        // X = a*u + b*v + c, Y = d*u + e*v + f  (u,v: toạ độ trong vùng nguồn; X,Y: trong vùng đích)
        float a, bb, c, d, e, f;
        switch (transform) {
            case Sprite_TRANS.MIRROR_ROT180: a = 1; bb = 0; c = 0; d = 0; e = -1; f = h; break;
            case Sprite_TRANS.MIRROR: a = -1; bb = 0; c = w; d = 0; e = 1; f = 0; break;
            case Sprite_TRANS.ROT180: a = -1; bb = 0; c = w; d = 0; e = -1; f = h; break;
            case Sprite_TRANS.MIRROR_ROT270: a = 0; bb = 1; c = 0; d = 1; e = 0; f = 0; break;
            case Sprite_TRANS.ROT90: a = 0; bb = -1; c = h; d = 1; e = 0; f = 0; break;
            case Sprite_TRANS.ROT270: a = 0; bb = 1; c = 0; d = -1; e = 0; f = w; break;
            case Sprite_TRANS.MIRROR_ROT90: a = 0; bb = -1; c = h; d = -1; e = 0; f = w; break;
            default: throw new IllegalArgumentException("transform " + transform);
        }
        mv[0] = a; mv[1] = bb; mv[2] = c + xd;
        mv[3] = d; mv[4] = e; mv[5] = f + yd;
        mv[6] = 0; mv[7] = 0; mv[8] = 1;
        matrix.setValues(mv);
        int sc = canvas.save();
        canvas.concat(matrix);
        dst.set(0, 0, w, h);
        canvas.drawBitmap(b, src, dst, bmpPaint);
        canvas.restoreToCount(sc);
    }

    public void drawRGB(int[] rgb, int offset, int scanlength, int x, int y, int w, int h, boolean processAlpha) {
        if (w <= 0 || h <= 0) return;
        int[] buf = new int[w * h];
        for (int j = 0; j < h; j++) {
            int s = offset + j * scanlength;
            int o = j * w;
            for (int i = 0; i < w; i++) {
                int p = rgb[s + i];
                buf[o + i] = processAlpha ? p : (p | 0xFF000000);
            }
        }
        Bitmap tmp = Bitmap.createBitmap(buf, w, h, Bitmap.Config.ARGB_8888);
        canvas.drawBitmap(tmp, x + tx, y + ty, bmpPaint);
        tmp.recycle();
    }

    public void copyArea(int xs, int ys, int w, int h, int xd, int yd, int anchor) {
        // hiếm dùng trong game: bỏ qua (không có ảnh nguồn của màn hình)
    }

    /** Hằng số biến đổi của javax.microedition.lcdui.game.Sprite (cùng giá trị). */
    static final class Sprite_TRANS {
        static final int NONE = 0;
        static final int MIRROR_ROT180 = 1;
        static final int MIRROR = 2;
        static final int ROT180 = 3;
        static final int MIRROR_ROT270 = 4;
        static final int ROT90 = 5;
        static final int ROT270 = 6;
        static final int MIRROR_ROT90 = 7;
    }
}
