package game.model;

import game.render.Canvas;
import game.render.Res;
import javax.microedition.lcdui.Graphics;

public class EffectLeg extends EffBack {
    public EffectLeg(int x, int y, int type, int limit, int w, int h, boolean isEffVong, boolean isEffUpDown) {
        super(x, y, type, limit, w, h, isEffVong, isEffUpDown);
        this.x = x;
        this.y = y;
        ((EffBack) this).type = type;
    }

    @Override
    public void paint(Graphics g) {
        if (Canvas.gameTick % 2 == 0) {
            g.drawRegion(Res.getWayPoint(), 0, ((EffBack) this).type * 24, 39, 24, 0, this.x, this.y, 3);
        }
    }
}
