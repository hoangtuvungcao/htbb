package javax.microedition.lcdui;

import java.awt.image.BufferedImage;
import java.io.InputStream;

/** Stub MIDP Image tren AWT - chi cho cong cu kiem ve map (KiemVeMap), khong dong goi vao jar. */
public class Image {
    public final BufferedImage bi;

    Image(final BufferedImage bi) {
        this.bi = bi;
    }

    public static Image createImage(final String ten) throws java.io.IOException {
        final InputStream is = Image.class.getResourceAsStream(ten);
        if (is == null) {
            throw new java.io.IOException("thieu " + ten);
        }
        try {
            return docAnh(javax.imageio.ImageIO.read(is));
        } finally {
            is.close();
        }
    }

    public static Image createImage(final byte[] d, final int o, final int n) {
        try {
            return docAnh(javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(d, o, n)));
        } catch (final java.io.IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static Image docAnh(final BufferedImage a) {
        final BufferedImage r = new BufferedImage(a.getWidth(), a.getHeight(), BufferedImage.TYPE_INT_ARGB);
        r.createGraphics().drawImage(a, 0, 0, null);
        return new Image(r);
    }

    public static Image createImage(final int w, final int h) {
        final BufferedImage r = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final java.awt.Graphics2D g = r.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, w, h);
        return new Image(r);
    }

    public static Image createRGBImage(final int[] px, final int w, final int h, final boolean alpha) {
        final BufferedImage r = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < w * h; i++) {
            r.setRGB(i % w, i / w, alpha ? px[i] : (px[i] | 0xFF000000));
        }
        return new Image(r);
    }

    public int getWidth() {
        return bi.getWidth();
    }

    public int getHeight() {
        return bi.getHeight();
    }

    public void getRGB(final int[] d, final int off, final int scan, final int x, final int y, final int w, final int h) {
        if (x < 0 || y < 0 || x + w > bi.getWidth() || y + h > bi.getHeight()) {
            throw new IllegalArgumentException("getRGB ngoai anh");
        }
        bi.getRGB(x, y, w, h, d, off, scan);
    }

    public Graphics getGraphics() {
        return new Graphics(bi);
    }
}
