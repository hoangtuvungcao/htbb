package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;

public class Explosion extends Actor {
    int f = 0;
    int p1;

    public Explosion(short x, short y) {
        this.catagory = (byte) 126;
        this.x = x;
        this.y = y;
        this.p1 = 0;
    }

    @Override
    public boolean isPaint() {
        if (this.x < GameScr.cmx || this.x > GameScr.cmx + Canvas.w || this.y < GameScr.cmy || this.y > GameScr.cmy + Canvas.h + 30) {
            return false;
        }
        return true;
    }

    @Override
    public void paint(Graphics g) {
        if (!isPaint()) {
            return;
        }
        g.drawRegion(Res.imgExplosion, 0, this.f * 24, 24, 24, 0, this.x - 12, this.y - 24, 0);
    }

    @Override
    public void setPosTo(short x, short y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void update() {
        this.p1++;
        if (this.p1 > 8) {
            this.p1 = 0;
            this.wantDestroy = true;
        }
        this.f = this.p1 >> 1;
    }
}
