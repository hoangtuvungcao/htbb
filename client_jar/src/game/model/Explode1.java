package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Explode1 extends Exp {
    private static final byte[] frame = {0, 1, 2, 1, 0, 1, 2, 1};
    int degree;
    Image img;
    int xt;
    int yt;
    int f = 0;
    int d = 0;
    int delay = 10;
    int delta = 10;

    public Explode1(Image img, int x, int y, int degree) {
        this.img = img;
        this.x = x;
        this.y = y;
        this.degree = degree;
    }

    @Override
    public void paint(Graphics g) {
        if (this.img != null) {
            g.drawImage(this.img, this.x + this.xt, this.y + this.yt, 20);
            Res.paintImgEff(g, 15, 0, frame[this.f] * Effect.HEIGHT[15], Effect.WIDTH[15], Effect.HEIGHT[15], this.x + this.xt + (this.img.getWidth() / 2), this.y + this.yt + (this.img.getHeight() / 2), 33);
            Res.paintImgEff(g, 15, 0, frame[this.f] * Effect.HEIGHT[15], Effect.WIDTH[15], Effect.HEIGHT[15], this.x + this.xt + (this.img.getWidth() / 2) + 5, this.y + this.yt + (this.img.getHeight() / 2), 33);
            Res.paintImgEff(g, 15, 0, frame[this.f] * Effect.HEIGHT[15], Effect.WIDTH[15], Effect.HEIGHT[15], ((this.x + this.xt) + (this.img.getWidth() / 2)) - 5, this.y + this.yt + (this.img.getHeight() / 2), 33);
            Res.paintImgEff(g, 15, 0, frame[this.f] * Effect.HEIGHT[15], Effect.WIDTH[15], Effect.HEIGHT[15], this.x + this.xt + (this.img.getWidth() / 2) + 5, this.y + this.yt + (this.img.getHeight() / 2) + 5, 33);
            Res.paintImgEff(g, 15, 0, frame[this.f] * Effect.HEIGHT[15], Effect.WIDTH[15], Effect.HEIGHT[15], ((this.x + this.xt) + (this.img.getWidth() / 2)) - 5, this.y + this.yt + (this.img.getHeight() / 2) + 5, 33);
        }
    }

    @Override
    public void update() {
        if (this.d < 100) {
            this.d += this.delta;
            this.xt = (Util.cos(this.degree) * this.d) / 1024;
            this.yt = (Util.sin(this.degree) * this.d) / 1024;
            this.delta += 10;
        } else {
            this.destroy = true;
        }
        if (Canvas.gameTick % 2 == 0) {
            this.f = (this.f + 1) % frame.length;
        }
    }
}
