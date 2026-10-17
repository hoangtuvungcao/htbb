package javax.microedition.lcdui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.midlet.MIDlet;

/** javax.microedition.lcdui.Image trên android.graphics.Bitmap (ARGB_8888, không co giãn theo mật độ). */
public class Image {
    final Bitmap bitmap;
    private final boolean mutable;

    private static final BitmapFactory.Options OPT = new BitmapFactory.Options();

    static {
        OPT.inScaled = false;
        OPT.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }

    private Image(Bitmap b, boolean mutable) {
        this.bitmap = b;
        this.mutable = mutable;
    }

    /** Bitmap Android bên dưới (cho lớp nền Android). */
    public Bitmap getBitmap() {
        return bitmap;
    }

    public static Image createImage(int w, int h) {
        if (w <= 0 || h <= 0) throw new IllegalArgumentException();
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        b.eraseColor(0xFFFFFFFF); // MIDP: ảnh mới tạo màu trắng
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
        Bitmap b = BitmapFactory.decodeByteArray(d, 0, d.length, OPT);
        if (b == null) throw new IOException("Anh hong");
        return new Image(b, false);
    }

    public static Image createImage(byte[] data, int off, int len) {
        Bitmap b = BitmapFactory.decodeByteArray(data, off, len, OPT);
        if (b == null) throw new IllegalArgumentException("Anh hong");
        return new Image(b, false);
    }

    public static Image createImage(Image src) {
        if (!src.mutable) return src;
        return new Image(src.bitmap.copy(Bitmap.Config.ARGB_8888, false), false);
    }

    public static Image createImage(Image src, int x, int y, int w, int h, int transform) {
        if (w <= 0 || h <= 0) throw new IllegalArgumentException();
        boolean doi = transform >= 4;
        int dw = doi ? h : w;
        int dh = doi ? w : h;
        Bitmap b = Bitmap.createBitmap(dw, dh, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas c = new android.graphics.Canvas(b);
        Graphics g = new Graphics(c, dw, dh);
        g.drawRegion(src, x, y, w, h, transform, 0, 0, Graphics.TOP | Graphics.LEFT);
        return new Image(b, false);
    }

    public static Image createRGBImage(int[] rgb, int w, int h, boolean processAlpha) {
        if (w <= 0 || h <= 0) throw new IllegalArgumentException();
        int[] d = rgb;
        if (!processAlpha) {
            d = new int[w * h];
            for (int i = 0; i < d.length; i++) d[i] = rgb[i] | 0xFF000000;
        }
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        b.setPixels(d, 0, w, 0, 0, w, h);
        return new Image(b, false);
    }

    public Graphics getGraphics() {
        if (!mutable) throw new IllegalStateException("Anh khong sua duoc");
        return new Graphics(new android.graphics.Canvas(bitmap), bitmap.getWidth(), bitmap.getHeight());
    }

    public int getWidth() {
        return bitmap.getWidth();
    }

    public int getHeight() {
        return bitmap.getHeight();
    }

    public boolean isMutable() {
        return mutable;
    }

    public void getRGB(int[] rgb, int offset, int scanlength, int x, int y, int w, int h) {
        bitmap.getPixels(rgb, offset, scanlength, x, y, w, h);
    }
}
