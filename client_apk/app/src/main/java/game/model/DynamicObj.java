package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.render.GCanvas;

public class DynamicObj extends Actor {
    public byte h;
    public byte id;
    public Bitmap img;
    public byte nFrame;
    public byte w;

    @Override
    public void paintName(Graphics graphics) {
    }

    public DynamicObj() {
        this.catagory = (byte) 100;
    }

    @Override
    public void paint(Graphics graphics) {
        Bitmap bitmap = this.img;
        if (bitmap != null) {
            graphics.drawRegion(bitmap, 0, this.id * this.height, this.img.getWidth(), this.height, 0, this.x + 8, this.y + 8, Graphics.BOTTOM | Graphics.HCENTER);
        }
    }

    @Override
    public void setPosTo(short s, short s2) {
        this.x = s;
        this.y = s2;
    }

    @Override
    public void update() {
        if (GCanvas.gameTick % 3 == 0) {
            this.id = (byte) ((this.id + 1) % this.nFrame);
        }
    }
}
