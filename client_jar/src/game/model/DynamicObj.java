package game.model;

import game.render.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class DynamicObj extends Actor {
    public Image img;
    public byte w;
    public byte h;
    public byte nFrame;
    public byte id;

    public DynamicObj() {
        this.catagory = (byte) 100;
    }

    @Override
    public void paint(Graphics g) {
        if (this.img != null) {
            g.drawRegion(this.img, 0, this.id * this.height, this.img.getWidth(), this.height, 0, this.x + 8, this.y + 8, 33);
        }
    }

    @Override
    public void setPosTo(short x, short y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void paintName(Graphics g) {
    }

    @Override
    public void update() {
        if (Canvas.gameTick % 3 == 0) {
            this.id = (byte) ((this.id + 1) % this.nFrame);
        }
    }
}
