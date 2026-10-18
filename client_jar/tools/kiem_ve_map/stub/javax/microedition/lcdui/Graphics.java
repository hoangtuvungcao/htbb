package javax.microedition.lcdui;

import java.awt.image.BufferedImage;

/** Stub MIDP Graphics tren AWT (neo, drawRegion khong bien doi/lat ngang) - chi cho KiemVeMap. */
public class Graphics {
    public static final int HCENTER = 1, VCENTER = 2, LEFT = 4, RIGHT = 8, TOP = 16, BOTTOM = 32, BASELINE = 64;
    final java.awt.Graphics2D g;
    final BufferedImage dich;
    int tx, ty;

    public Graphics(final BufferedImage dich) {
        this.dich = dich;
        this.g = dich.createGraphics();
    }

    public void translate(final int x, final int y) {
        tx += x;
        ty += y;
    }

    public void setColor(final int c) {
        g.setColor(new java.awt.Color(c));
    }

    public void setColor(final int r, final int gg, final int b) {
        g.setColor(new java.awt.Color(r, gg, b));
    }

    public void fillRect(final int x, final int y, final int w, final int h) {
        g.fillRect(x + tx, y + ty, w, h);
    }

    public void drawRect(final int x, final int y, final int w, final int h) {
        g.drawRect(x + tx, y + ty, w, h);
    }

    static int neoX(final int x, final int w, final int a) {
        return (a & HCENTER) != 0 ? x - w / 2 : (a & RIGHT) != 0 ? x - w : x;
    }

    static int neoY(final int y, final int h, final int a) {
        return (a & VCENTER) != 0 ? y - h / 2 : (a & BOTTOM) != 0 ? y - h : y;
    }

    public void drawImage(final Image img, final int x, final int y, final int a) {
        g.drawImage(img.bi, neoX(x, img.getWidth(), a) + tx, neoY(y, img.getHeight(), a) + ty, null);
    }

    /** Giong MIDP: vung nguon ra ngoai anh thi nem IllegalArgumentException. transform 0 = nguyen, 2 = lat ngang. */
    public void drawRegion(final Image img, final int sx, final int sy, final int w, final int h, final int tr, final int x, final int y,
                           final int a) {
        if (sx < 0 || sy < 0 || w <= 0 || h <= 0 || sx + w > img.getWidth() || sy + h > img.getHeight()) {
            throw new IllegalArgumentException("drawRegion ngoai anh " + sx + "," + sy + " " + w + "x" + h + " / " + img.getWidth() + "x"
                    + img.getHeight());
        }
        final int dx = neoX(x, w, a) + tx, dy = neoY(y, h, a) + ty;
        if (tr == 2) {
            g.drawImage(img.bi, dx + w, dy, dx, dy + h, sx, sy, sx + w, sy + h, null);
        } else {
            g.drawImage(img.bi, dx, dy, dx + w, dy + h, sx, sy, sx + w, sy + h, null);
        }
    }
}
