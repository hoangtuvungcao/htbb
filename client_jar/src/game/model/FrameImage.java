package game.model;

import game.render.Res;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class FrameImage {
    public short frameWidth;
    public short frameHeight;
    public short nFrame;
    public Image imgFrame;

    public FrameImage(Image img, int width, int height) {
        this.imgFrame = img;
        this.frameWidth = (short) width;
        this.frameHeight = (short) height;
        this.nFrame = (short) (img.getHeight() / height);
    }

    public FrameImage(int id, int frame) {
        this.imgFrame = Res.getImageEffect(id);
        this.frameWidth = Effect.getWidth(id);
        this.frameHeight = Effect.getHeight(id);
        this.nFrame = (short) frame;
    }

    public static FrameImage init(String path, int w, int h) {
        return new FrameImage(FilePack.getImg(path), w, h);
    }

    public void drawFrame(int idx, int x, int y, int trans, int orthor, Graphics g) {
        if (this.imgFrame != null) {
            g.drawRegion(this.imgFrame, 0, idx * this.frameHeight, this.frameWidth, this.frameHeight, trans, x, y, orthor);
        }
    }
}
