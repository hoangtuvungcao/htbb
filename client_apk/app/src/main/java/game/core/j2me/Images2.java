package game.core.j2me;

import android.graphics.Bitmap;

public class Images2 {
    Bitmap bitmap;

    public static Images2 createImage(int i, int i2) {
        return new Images2(Bitmap.createBitmap(i, i2, Bitmap.Config.RGB_565));
    }

    public Images2(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Graphics getGraphics() {
        return new Graphics(this);
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public int getWidth() {
        Bitmap bitmap = this.bitmap;
        if (bitmap == null) {
            return 0;
        }
        return bitmap.getWidth();
    }

    public int getHeight() {
        Bitmap bitmap = this.bitmap;
        if (bitmap == null) {
            return 0;
        }
        return bitmap.getHeight();
    }
}
