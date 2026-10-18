package game.model;

import game.core.j2me.Graphics;
import game.render.Res;

public class TileTop {
    public short index = 0;
    public short x;
    public short y;

    public void paint(Graphics graphics) {
        graphics.drawRegion(Res.imgTile, 0, this.index << 4, 16, 16, 0, this.x << 4, this.y << 4, 0);
    }

    public void paintSmall(Graphics graphics) {
        graphics.drawRegion(Res.imgtileSmall, 0, this.index, 1, 1, 0, this.x, this.y, 0);
    }
}
