package game.model;

import game.render.Res;
import javax.microedition.lcdui.Graphics;

public class TileTop {
    public short x;
    public short y;
    public short index = 0;

    public void paint(Graphics g) {
        g.drawRegion(Res.imgTile, 0, this.index << 4, 16, 16, 0, this.x << 4, this.y << 4, 0);
    }

    public void paintSmall(Graphics g) {
        g.drawRegion(Res.imgtileSmall, 0, this.index, 1, 1, 0, this.x, this.y, 0);
    }
}
