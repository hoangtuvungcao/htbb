package game.model;

import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.GameScr;

public class Explosion extends Actor {
    int f = 0;
    int p1;

    public Explosion(short s, short s2) {
        this.catagory = (byte) 126;
        this.x = s;
        this.y = s2;
        this.p1 = 0;
    }

    @Override
    public boolean isPaint() {
        return this.x >= GameScr.cmx && this.x <= GameScr.cmx + GCanvas.w && this.y >= GameScr.cmy && this.y <= (GameScr.cmy + GCanvas.h) + 30;
    }

    @Override
    public void paint(Graphics graphics) {
        if (isPaint()) {
            graphics.drawRegion(Res.imgExplosion, 0, this.f * 24, 24, 24, 0, this.x - 12, this.y - 24, 0);
        }
    }

    @Override
    public void setPosTo(short s, short s2) {
        this.x = s;
        this.y = s2;
    }

    @Override
    public void update() {
        int i = this.p1 + 1;
        this.p1 = i;
        if (i > 8) {
            this.p1 = 0;
            this.wantDestroy = true;
        }
        this.f = this.p1 >> 1;
    }
}
