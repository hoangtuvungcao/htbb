package javax.microedition.lcdui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * javax.microedition.lcdui.Graphics (MIDP 2.0) vẽ bằng Java2D (bản PC).
 *
 * Màn hình: Graphics2D của bộ đệm cỡ THẬT của cửa sổ, biến đổi gốc = dịch (lề) + phóng HỆ SỐ NGUYÊN k
 * (toạ độ game vẫn là độ phân giải logic, vd 640x360). Ảnh pixel phóng láng giềng gần nhất -> sắc nét;
 * chữ thật / khung HD (htbb.pc.ChuHD, GiaoDienHD) vẽ khử răng cưa ở độ phân giải thật qua g2d().
 * Ảnh sửa được (Image.createImage(w, h)): k = 1.
 * Toạ độ dịch (translate) + vùng cắt (clip) giữ riêng như J2ME: setClip THAY vùng cắt.
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

    private final Graphics2D g;
    private AffineTransform goc;
    private final AffineTransform gocUI;
    private int surfaceW;
    private int surfaceH;
    private final int surfaceWUI;
    private final int surfaceHUI;
    private final int heSo;
    /** tỉ lệ thế giới / giao diện (htbb.pc.Zoom), 1 = giao diện */
    private float tiLe = 1f;
    private int tx;
    private int ty;
    private int clipX;
    private int clipY;
    private int clipW;
    private int clipH;
    private int color = 0xFF000000;
    private Color mauAwt = Color.BLACK;
    private int strokeStyle = SOLID;
    private Font font = Font.getDefaultFont();
    private final AffineTransform tam = new AffineTransform();

    /**
     * @param g2  Graphics2D đích (đã đặt biến đổi gốc: lề + phóng k)
     * @param w   rộng logic
     * @param h   cao logic
     * @param k   hệ số phóng (1 = ảnh)
     */
    public Graphics(Graphics2D g2, int w, int h, int k) {
        this.g = g2;
        this.surfaceW = w;
        this.surfaceH = h;
        this.heSo = k;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        this.goc = g.getTransform();
        this.gocUI = goc;
        this.surfaceWUI = w;
        this.surfaceHUI = h;
        reset();
    }

    /** Đưa về trạng thái đầu. */
    public void reset() {
        tx = 0;
        ty = 0;
        apClip(0, 0, surfaceW, surfaceH);
        setColor(0);
        strokeStyle = SOLID;
        font = Font.getDefaultFont();
    }

    // ---------- cho lớp HD (không thuộc MIDP) ----------

    /** Graphics2D bên dưới, biến đổi = gốc (toạ độ logic, CHƯA cộng translate của J2ME). */
    public Graphics2D g2d() {
        g.setTransform(goc);
        return g;
    }

    /**
     * Vào lớp THẾ GIỚI (htbb.pc.Zoom): toạ độ game nhân thêm z (= phóng thế giới / phóng giao diện), vùng logic
     * vw x vh; translate về 0. Ra: boTheGioi().
     */
    public void vaoTheGioi(float z, int vw, int vh) {
        tiLe = z;
        AffineTransform t = new AffineTransform(gocUI);
        t.scale(z, z);
        goc = t;
        surfaceW = vw;
        surfaceH = vh;
        tx = 0;
        ty = 0;
        apClip(0, 0, vw, vh);
    }

    public void boTheGioi() {
        tiLe = 1f;
        goc = gocUI;
        surfaceW = surfaceWUI;
        surfaceH = surfaceHUI;
        tx = 0;
        ty = 0;
        apClip(0, 0, surfaceW, surfaceH);
    }

    /**
     * Graphics2D ở toạ độ ĐIỂM ẢNH THẬT của bộ đệm (bỏ biến đổi + vùng cắt) - cho htbb.pc.Zoom vẽ nền lề.
     * Lệnh MIDP kế tiếp (setClip / boTheGioi...) tự đặt lại biến đổi.
     */
    public Graphics2D g2dThat() {
        g.setTransform(new AffineTransform());
        g.setClip(null);
        return g;
    }

    /** Đang vẽ lớp thế giới phóng to (tỉ lệ > 1). */
    public float tiLeTheGioi() {
        return tiLe;
    }

    /** Hệ số phóng logic -> điểm ảnh thật (1 với ảnh). */
    public int heSo() {
        return heSo;
    }

    public void huy() {
        g.dispose();
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
        apClip(x0, y0, x1 - x0, y1 - y0);
    }

    public void clipRect(int x, int y, int w, int h) {
        int x0 = Math.max(clipX, x + tx);
        int y0 = Math.max(clipY, y + ty);
        int x1 = Math.min(clipX + clipW, x + tx + Math.max(0, w));
        int y1 = Math.min(clipY + clipH, y + ty + Math.max(0, h));
        apClip(x0, y0, x1 - x0, y1 - y0);
    }

    private void apClip(int x, int y, int w, int h) {
        if (w < 0) w = 0;
        if (h < 0) h = 0;
        clipX = x;
        clipY = y;
        clipW = w;
        clipH = h;
        g.setTransform(goc);
        g.setClip(x, y, w, h);
    }

    /** Vùng cắt hiện tại theo toạ độ logic tuyệt đối (cho lớp HD). */
    public int clipTuyetDoiX() {
        return clipX;
    }

    public int clipTuyetDoiY() {
        return clipY;
    }

    // ---------- màu ----------
    public void setColor(int rgb) {
        int c = 0xFF000000 | rgb;
        if (c != color) {
            color = c;
            mauAwt = new Color(c, false);
        }
        g.setColor(mauAwt);
    }

    public void setColor(int r, int gg, int b) {
        setColor(((r & 255) << 16) | ((gg & 255) << 8) | (b & 255));
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
        g.setColor(mauAwt);
        g.fillRect(x + tx, y + ty, w, h);
    }

    public void drawRect(int x, int y, int w, int h) {
        if (w < 0 || h < 0) return;
        x += tx;
        y += ty;
        g.setColor(mauAwt);
        // J2ME: viền phủ (w+1) x (h+1) điểm ảnh
        g.fillRect(x, y, w + 1, 1);
        g.fillRect(x, y + h, w + 1, 1);
        g.fillRect(x, y, 1, h + 1);
        g.fillRect(x + w, y, 1, h + 1);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        x1 += tx;
        y1 += ty;
        x2 += tx;
        y2 += ty;
        g.setColor(mauAwt);
        if (y1 == y2) {
            int a = Math.min(x1, x2);
            g.fillRect(a, y1, Math.abs(x2 - x1) + 1, 1);
            return;
        }
        if (x1 == x2) {
            int a = Math.min(y1, y2);
            g.fillRect(x1, a, 1, Math.abs(y2 - y1) + 1);
            return;
        }
        // đường chéo: Bresenham theo điểm ảnh logic (giữ nét pixel)
        int dx = Math.abs(x2 - x1);
        int dy = -Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx + dy;
        int n = 0;
        while (n++ < 4096) {
            g.fillRect(x1, y1, 1, 1);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x1 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private void apMin(boolean bat) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                bat ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        g.setColor(mauAwt);
        int[] xs = {x1 + tx, x2 + tx, x3 + tx};
        int[] ys = {y1 + ty, y2 + ty, y3 + ty};
        g.fillPolygon(xs, ys, 3);
    }

    public void fillArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        if (w <= 0 || h <= 0) return;
        g.setColor(mauAwt);
        apMin(heSo > 1);
        Shape s = arcAngle >= 360 || arcAngle <= -360
                ? new Ellipse2D.Float(x + tx, y + ty, w, h)
                : new Arc2D.Float(x + tx, y + ty, w, h, startAngle, arcAngle, Arc2D.PIE);
        g.fill(s);
        apMin(false);
    }

    public void drawArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        if (w < 0 || h < 0) return;
        g.setColor(mauAwt);
        apMin(heSo > 1);
        Shape s = arcAngle >= 360 || arcAngle <= -360
                ? new Ellipse2D.Float(x + tx + 0.5f, y + ty + 0.5f, w, h)
                : new Arc2D.Float(x + tx + 0.5f, y + ty + 0.5f, w, h, startAngle, arcAngle, Arc2D.OPEN);
        g.draw(s);
        apMin(false);
    }

    public void fillRoundRect(int x, int y, int w, int h, int arcW, int arcH) {
        if (w <= 0 || h <= 0) return;
        g.setColor(mauAwt);
        apMin(heSo > 1);
        g.fill(new RoundRectangle2D.Float(x + tx, y + ty, w, h, arcW, arcH));
        apMin(false);
    }

    public void drawRoundRect(int x, int y, int w, int h, int arcW, int arcH) {
        if (w < 0 || h < 0) return;
        g.setColor(mauAwt);
        apMin(heSo > 1);
        g.draw(new RoundRectangle2D.Float(x + tx + 0.5f, y + ty + 0.5f, w, h, arcW, arcH));
        apMin(false);
    }

    // ---------- chữ (lcdui.Font = font thật Noto Sans; game chủ yếu dùng font ảnh riêng -> htbb.pc.ChuHD) ----------
    public void drawString(String s, int x, int y, int anchor) {
        if (s == null) throw new NullPointerException();
        java.awt.Font f = font.awt();
        g.setFont(f);
        g.setColor(mauAwt);
        int w = font.stringWidth(s);
        if ((anchor & HCENTER) != 0) x -= w / 2;
        else if ((anchor & RIGHT) != 0) x -= w;
        float by;
        if ((anchor & BOTTOM) != 0) by = y + ty - font.descent();
        else if ((anchor & BASELINE) != 0) by = y + ty;
        else if ((anchor & VCENTER) != 0) by = y + ty + font.getBaselinePosition() - font.getHeight() / 2f;
        else by = y + ty + font.getBaselinePosition();
        g.drawString(s, (float) (x + tx), by);
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
        if (img == null) throw new NullPointerException();
        BufferedImage b = img.anh;
        int w = b.getWidth();
        int h = b.getHeight();
        if ((anchor & HCENTER) != 0) x -= w / 2;
        else if ((anchor & RIGHT) != 0) x -= w;
        if ((anchor & VCENTER) != 0) y -= h / 2;
        else if ((anchor & BOTTOM) != 0) y -= h;
        if (veThang(img, 0, 0, w, h, x + tx, y + ty, false)) return;
        g.drawImage(b, x + tx, y + ty, null);
    }

    /** thời điểm khung (cho bộ nhớ đệm ảnh phóng) */
    private long luc = System.currentTimeMillis();

    /**
     * 0.9.5: vẽ vùng ảnh bằng BẢN PHÓNG SẴN khi phép biến đổi gốc là phóng nguyên k (không xoay): chép điểm ảnh 1:1 ở
     * toạ độ thật. false = để đường Java2D thường vẽ.
     */
    private boolean veThang(Image img, int xs, int ys, int w, int h, int xd, int yd, boolean lat) {
        double sx = goc.getScaleX();
        int k = (int) Math.round(sx);
        if (k < 2 || Math.abs(sx - k) > 1e-6 || Math.abs(goc.getScaleY() - sx) > 1e-6 || goc.getShearX() != 0 || goc.getShearY() != 0) {
            return false;
        }
        double ox = goc.getTranslateX(), oy = goc.getTranslateY();
        if (Math.abs(ox - Math.rint(ox)) > 1e-6 || Math.abs(oy - Math.rint(oy)) > 1e-6) return false;
        BufferedImage p = img.phong(k, luc);
        if (p == null) return false;
        int dx = (int) Math.rint(ox) + xd * k, dy = (int) Math.rint(oy) + yd * k;
        g.setTransform(DON_VI);
        if (lat) {
            g.drawImage(p, dx + w * k, dy, dx, dy + h * k, xs * k, ys * k, (xs + w) * k, (ys + h) * k, null);
        } else {
            g.drawImage(p, dx, dy, dx + w * k, dy + h * k, xs * k, ys * k, (xs + w) * k, (ys + h) * k, null);
        }
        g.setTransform(goc);
        return true;
    }

    public void drawRegion(Image img, int xs, int ys, int w, int h, int transform, int xd, int yd, int anchor) {
        if (img == null) throw new NullPointerException();
        if (w <= 0 || h <= 0) return;
        BufferedImage b = img.anh;
        boolean doi = transform >= 4; // ROT90/ROT270/MIRROR_ROT90/MIRROR_ROT270: đổi rộng <-> cao
        int dw = doi ? h : w;
        int dh = doi ? w : h;
        if ((anchor & HCENTER) != 0) xd -= dw / 2;
        else if ((anchor & RIGHT) != 0) xd -= dw;
        if ((anchor & VCENTER) != 0) yd -= dh / 2;
        else if ((anchor & BOTTOM) != 0) yd -= dh;
        xd += tx;
        yd += ty;
        switch (transform) {
            case 0:
                if (veThang(img, xs, ys, w, h, xd, yd, false)) return;
                g.drawImage(b, xd, yd, xd + w, yd + h, xs, ys, xs + w, ys + h, null);
                return;
            case 2: // MIRROR
                if (veThang(img, xs, ys, w, h, xd, yd, true)) return;
                g.drawImage(b, xd + w, yd, xd, yd + h, xs, ys, xs + w, ys + h, null);
                return;
            case 1: // MIRROR_ROT180 = lật dọc
                g.drawImage(b, xd, yd + h, xd + w, yd, xs, ys, xs + w, ys + h, null);
                return;
            case 3: // ROT180
                g.drawImage(b, xd + w, yd + h, xd, yd, xs, ys, xs + w, ys + h, null);
                return;
            default:
                break;
        }
        // X = a*u + bb*v + c, Y = d*u + e*v + f  (u,v: toạ độ trong vùng nguồn; X,Y: trong vùng đích)
        double a, bb, c, d, e, f;
        switch (transform) {
            case 4: a = 0; bb = 1; c = 0; d = 1; e = 0; f = 0; break;          // MIRROR_ROT270
            case 5: a = 0; bb = -1; c = h; d = 1; e = 0; f = 0; break;         // ROT90
            case 6: a = 0; bb = 1; c = 0; d = -1; e = 0; f = w; break;         // ROT270
            case 7: a = 0; bb = -1; c = h; d = -1; e = 0; f = w; break;        // MIRROR_ROT90
            default: throw new IllegalArgumentException("transform " + transform);
        }
        AffineTransform cu = g.getTransform();
        tam.setTransform(a, d, bb, e, c + xd, f + yd);
        g.transform(tam);
        g.drawImage(b, 0, 0, w, h, xs, ys, xs + w, ys + h, null);
        g.setTransform(cu);
    }

    public void drawRGB(int[] rgb, int offset, int scanlength, int x, int y, int w, int h, boolean processAlpha) {
        if (w <= 0 || h <= 0) return;
        BufferedImage tmp = new BufferedImage(w, h, processAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        tmp.setRGB(0, 0, w, h, rgb, offset, scanlength);
        g.drawImage(tmp, x + tx, y + ty, null);
    }

    public void copyArea(int xs, int ys, int w, int h, int xd, int yd, int anchor) {
        // hiếm dùng trong game: bỏ qua
    }

    private static final AffineTransform DON_VI = new AffineTransform();

    /** Hệ số phóng thật hiện tại (logic -> điểm ảnh bộ đệm) - lớp HD. */
    public double tiLeThat() {
        return goc.getScaleX();
    }

    /**
     * 0.9.5 (bộ nhớ đệm chữ HD): vẽ ảnh ĐÃ Ở ĐỘ PHÂN GIẢI THẬT (1 điểm ảnh ảnh = 1 điểm ảnh bộ đệm), góc trên trái tại
     * toạ độ logic (lx, ly) + translate, làm tròn tới điểm ảnh thật. Không cấp phát.
     */
    public void veAnhThat(BufferedImage anh, double lx, double ly) {
        double s = goc.getScaleX();
        int dx = (int) Math.round(goc.getTranslateX() + (lx + tx) * s);
        int dy = (int) Math.round(goc.getTranslateY() + (ly + ty) * goc.getScaleY());
        g.setTransform(DON_VI);
        g.drawImage(anh, dx, dy, null);
        g.setTransform(goc);
    }

    /** Vẽ ảnh mờ (alpha 0..255) - cho lớp HD. */
    public void veAnhMo(Image img, int x, int y, int alpha) {
        Composite cu = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, Math.min(255, alpha)) / 255f));
        g.drawImage(img.anh, x + tx, y + ty, null);
        g.setComposite(cu);
    }
}
