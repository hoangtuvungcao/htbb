package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;

public class Explode1 extends Exp {
    private static final byte[] frame = {0, 1, 2, 1, 0, 1, 2, 1};
    int degree;
    Bitmap img;
    int xt;
    int yt;
    int f = 0;
    int d = 0;
    int delay = 10;
    int delta = 10;

    public Explode1(Bitmap bitmap, int i, int i2, int i3) {
        this.img = bitmap;
        this.x = i;
        this.y = i2;
        this.degree = i3;
    }

    @Override
    public void paint(Graphics graphics) {
        Bitmap bitmap = this.img;
        if (bitmap != null) {
            graphics.drawImage(bitmap, this.x + this.xt, this.y + this.yt, 20);
            byte[] bArr = frame;
            Res.paintImgEff(graphics, 15, 0, bArr[this.f] * Effect.HEIGHT[15], Effect.WIDTH[15], Effect.HEIGHT[15], this.x + this.xt + (this.img.getWidth() / 2), this.y + this.yt + (this.img.getHeight() / 2), Graphics.BOTTOM | Graphics.HCENTER);
            Res.paintImgEff(graphics, 15, 0, bArr[this.f] * Effect.HEIGHT[15], Effect.WIDTH[15], Effect.HEIGHT[15], this.x + this.xt + (this.img.getWidth() / 2) + 5, this.y + this.yt + (this.img.getHeight() / 2), Graphics.BOTTOM | Graphics.HCENTER);
            Res.paintImgEff(graphics, 15, 0, bArr[this.f] * Effect.HEIGHT[15], Effect.WIDTH[15], Effect.HEIGHT[15], ((this.x + this.xt) + (this.img.getWidth() / 2)) - 5, this.y + this.yt + (this.img.getHeight() / 2), Graphics.BOTTOM | Graphics.HCENTER);
            Res.paintImgEff(graphics, 15, 0, bArr[this.f] * Effect.HEIGHT[15], Effect.WIDTH[15], Effect.HEIGHT[15], this.x + this.xt + (this.img.getWidth() / 2) + 5, this.y + this.yt + (this.img.getHeight() / 2) + 5, Graphics.BOTTOM | Graphics.HCENTER);
            Res.paintImgEff(graphics, 15, 0, bArr[this.f] * Effect.HEIGHT[15], Effect.WIDTH[15], Effect.HEIGHT[15], ((this.x + this.xt) + (this.img.getWidth() / 2)) - 5, this.y + this.yt + (this.img.getHeight() / 2) + 5, Graphics.BOTTOM | Graphics.HCENTER);
        }
    }

    @Override
    public void update() {
        int i = this.d;
        if (i < 100) {
            this.d = i + this.delta;
            this.xt = (Util.cos(this.degree) * this.d) / 1024;
            this.yt = (Util.sin(this.degree) * this.d) / 1024;
            this.delta += 10;
        } else {
            this.destroy = true;
        }
        if (GCanvas.gameTick % 2 == 0) {
            this.f = (this.f + 1) % frame.length;
        }
    }
}
