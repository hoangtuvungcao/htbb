package javax.microedition.lcdui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.microedition.midlet.MIDlet;

/**
 * javax.microedition.lcdui.Image trên java.awt.image.BufferedImage (bản PC).
 * Ảnh PNG: INT_ARGB_PRE (Java2D vẽ nhanh nhất, được tăng tốc phần cứng); ảnh sửa được: INT_RGB trắng như MIDP.
 */
public class Image {
    final BufferedImage anh;
    private final boolean mutable;

    static {
        ImageIO.setUseCache(false);
    }

    private Image(BufferedImage b, boolean mutable) {
        this.anh = b;
        this.mutable = mutable;
    }

    /** Ảnh Java2D bên dưới (cho lớp HD). */
    public BufferedImage awt() {
        return anh;
    }

    /** Bọc ảnh Java2D có sẵn (lớp HD: logo, banner). */
    public static Image tuAwt(BufferedImage b) {
        return new Image(chuan(b), false);
    }

    private static BufferedImage chuan(BufferedImage b) {
        if (b.getType() == BufferedImage.TYPE_INT_ARGB_PRE) return b;
        BufferedImage c = new BufferedImage(b.getWidth(), b.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = c.createGraphics();
        g.setComposite(java.awt.AlphaComposite.Src);
        g.drawImage(b, 0, 0, null);
        g.dispose();
        return c;
    }

    public static Image createImage(int w, int h) {
        if (w <= 0 || h <= 0) throw new IllegalArgumentException();
        BufferedImage b = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = b.createGraphics();
        g.setColor(java.awt.Color.WHITE); // MIDP: ảnh mới tạo màu trắng
        g.fillRect(0, 0, w, h);
        g.dispose();
        return new Image(b, true);
    }

    public static Image createImage(String name) throws IOException {
        if (name == null) throw new NullPointerException();
        InputStream in = MIDlet.moTaiNguyen(name);
        if (in == null) throw new IOException("Khong thay anh " + name);
        try {
            return createImage(in);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // bỏ qua
            }
        }
    }

    public static Image createImage(InputStream in) throws IOException {
        if (in == null) throw new NullPointerException();
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
        byte[] d = bo.toByteArray();
        return giaiMa(d, 0, d.length);
    }

    private static Image giaiMa(byte[] d, int off, int len) throws IOException {
        BufferedImage b = ImageIO.read(new ByteArrayInputStream(d, off, len));
        if (b == null) throw new IOException("Anh hong");
        return new Image(chuan(b), false);
    }

    public static Image createImage(byte[] data, int off, int len) {
        try {
            return giaiMa(data, off, len);
        } catch (IOException e) {
            throw new IllegalArgumentException("Anh hong");
        }
    }

    public static Image createImage(Image src) {
        if (!src.mutable) return src;
        return new Image(chuan(src.anh), false);
    }

    public static Image createImage(Image src, int x, int y, int w, int h, int transform) {
        NghiemMidp.vung("createImage(Image..)", src, x, y, w, h);
        if (w <= 0 || h <= 0) throw new IllegalArgumentException();
        boolean doi = transform >= 4;
        int dw = doi ? h : w;
        int dh = doi ? w : h;
        BufferedImage b = new BufferedImage(dw, dh, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2 = b.createGraphics();
        Graphics g = new Graphics(g2, dw, dh, 1);
        g.drawRegion(src, x, y, w, h, transform, 0, 0, Graphics.TOP | Graphics.LEFT);
        g2.dispose();
        return new Image(b, false);
    }

    public static Image createRGBImage(int[] rgb, int w, int h, boolean processAlpha) {
        NghiemMidp.mang("createRGBImage", rgb, 0, w, w, h);
        if (w <= 0 || h <= 0) throw new IllegalArgumentException();
        BufferedImage b = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        if (processAlpha) {
            b.setRGB(0, 0, w, h, rgb, 0, w);
        } else {
            int[] d = new int[w * h];
            for (int i = 0; i < d.length; i++) d[i] = rgb[i] | 0xFF000000;
            b.setRGB(0, 0, w, h, d, 0, w);
        }
        return new Image(chuan(b), false);
    }

    // ---- 0.9.5: bản phóng sẵn (láng giềng gần nhất) theo hệ số nguyên của bộ đệm: vẽ = chép thẳng, không phép biến đổi
    // (Java2D vẽ ảnh qua phép phóng mỗi lần: chậm + cấp phát AffineTransform / double[] mỗi lệnh vẽ) ----
    /** bản phóng theo hệ số k (chỉ số k, 2..8) - một ảnh có thể vẽ ở nhiều hệ số (icon 1x / 1,5x) */
    private BufferedImage[] phong;
    private long phongPx;
    long dungLuc;
    /** tổng điểm ảnh các bản phóng đang giữ; trần ~16 M điểm ảnh (~64 MB) */
    static long tongPhong;
    static final long TRAN_PHONG = 16L * 1024 * 1024;
    static final java.util.ArrayList<Image> DS_PHONG = new java.util.ArrayList<Image>();

    /** Bản phóng k lần (null = không giữ được: ảnh sửa được / quá to / hết trần). Gọi trên luồng vẽ. */
    BufferedImage phong(int k, long now) {
        if (mutable || k < 2 || k > 8) return null;
        if (phong != null && phong[k] != null) {
            dungLuc = now;
            return phong[k];
        }
        long px = (long) anh.getWidth() * anh.getHeight() * k * k;
        if (px > 2L * 1024 * 1024) return null;
        if (tongPhong + px > TRAN_PHONG && !donPhong(now, px)) return null;
        BufferedImage b = new BufferedImage(anh.getWidth() * k, anh.getHeight() * k, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = b.createGraphics();
        g.setComposite(java.awt.AlphaComposite.Src);
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(anh, 0, 0, b.getWidth(), b.getHeight(), null);
        g.dispose();
        if (phong == null) {
            phong = new BufferedImage[9];
            DS_PHONG.add(this);
        }
        phong[k] = b;
        phongPx += px;
        dungLuc = now;
        tongPhong += px;
        return b;
    }

    /** Bỏ bản phóng không dùng > 5 giây cho tới khi đủ chỗ (không xoá cái đang dùng: tránh phóng lại liên tục). */
    private static boolean donPhong(long now, long can) {
        for (int i = DS_PHONG.size() - 1; i >= 0 && tongPhong + can > TRAN_PHONG; i--) {
            Image m = DS_PHONG.get(i);
            if (now - m.dungLuc > 5000) {
                tongPhong -= m.phongPx;
                m.phong = null;
                m.phongPx = 0;
                DS_PHONG.remove(i);
            }
        }
        return tongPhong + can <= TRAN_PHONG;
    }

    public Graphics getGraphics() {
        if (!mutable) throw new IllegalStateException("Anh khong sua duoc");
        return new Graphics(anh.createGraphics(), anh.getWidth(), anh.getHeight(), 1);
    }

    public int getWidth() {
        return anh.getWidth();
    }

    public int getHeight() {
        return anh.getHeight();
    }

    public boolean isMutable() {
        return mutable;
    }

    public void getRGB(int[] rgb, int offset, int scanlength, int x, int y, int w, int h) {
        NghiemMidp.vung("getRGB", this, x, y, w, h);
        NghiemMidp.mang("getRGB", rgb, offset, scanlength, w, h);
        anh.getRGB(x, y, w, h, rgb, offset, scanlength);
    }
}
