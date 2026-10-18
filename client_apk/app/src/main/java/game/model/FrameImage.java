package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;

public class FrameImage {
    public int frameHeight;
    public int frameWidth;
    public Bitmap imgFrame;
    public int nFrame;
    private int[] pos;
    private int totalHeight;

    public FrameImage(Bitmap bitmap, int i, int i2) {
        this.imgFrame = bitmap;
        this.frameWidth = i;
        this.frameHeight = i2;
        int height = bitmap.getHeight();
        this.totalHeight = height;
        int i3 = height / i2;
        this.nFrame = i3;
        this.pos = new int[i3];
        for (int i4 = 0; i4 < this.nFrame; i4++) {
            this.pos[i4] = i4 * i2;
        }
    }

    public void drawFrame(int i, int i2, int i3, int i4, int i5, Graphics graphics) {
        Bitmap bitmap = this.imgFrame;
        if (bitmap == null || i < 0 || i >= this.nFrame) {
            return;
        }
        graphics.drawRegion(bitmap, 0, this.pos[i], this.frameWidth, this.frameHeight, i4, i2, i3, i5);
    }

    public void drawFrame(int i, int i2, int i3, int i4, Graphics graphics) {
        Bitmap bitmap = this.imgFrame;
        if (bitmap != null) {
            int i5 = this.frameHeight;
            graphics.drawRegion(bitmap, 0, i * i5, this.frameWidth, i5, i4, i2, i3, 0);
        }
    }

    public void drawFrameXY(int i, int i2, int i3, int i4, Graphics graphics) {
        Bitmap bitmap = this.imgFrame;
        if (bitmap == null || i < 0 || i >= this.nFrame) {
            return;
        }
        int i5 = this.frameWidth;
        int i6 = this.frameHeight;
        graphics.drawRegion(bitmap, i * i5, i2 * i6, i5, i6, 0, i3, i4, 0);
    }

    public void unload() {
        this.imgFrame = null;
        this.pos = null;
    }

    public static FrameImage init(String str, int i, int i2) {
        return new FrameImage(FilePack.getImg(str), i, i2);
    }
}
