package game.model;

import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;

public class EffectLeg extends EffBack {
    public EffectLeg(int i, int i2, int i3, int i4, int i5, int i6, boolean z, boolean z2) {
        super(i, i2, i3, i4, i5, i6, z, z2);
        this.x = i;
        this.y = i2;
        this.type = i3;
    }

    @Override
    public void paint(Graphics graphics) {
        if (GCanvas.gameTick % 2 == 0) {
            graphics.drawRegion(Res.getWayPoint(), 0, this.type * 24, 39, 24, 0, this.x, this.y, 3);
        }
    }
}
