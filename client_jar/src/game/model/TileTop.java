package game.model;

import game.render.Res;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class TileTop {
    public short x;
    public short y;
    public short index = 0;

    /**
     * 0.9.22 (Nokia E72 / S60): máy thật ném IllegalArgumentException khi vùng nguồn drawRegion vượt ảnh (giả lập cắt im
     * lặng) - chỉ số ô ngoài bộ tile (dữ liệu bản đồ lệch bộ tile) thì bỏ ô đó, không ném.
     */
    public void paint(Graphics g) {
        Image t = Res.imgTile;
        if (t != null && this.index >= 0 && (this.index << 4) + 16 <= t.getHeight()) {
            g.drawRegion(t, 0, this.index << 4, 16, 16, 0, this.x << 4, this.y << 4, 0);
        }
    }

    public void paintSmall(Graphics g) {
        Image t = Res.imgtileSmall;
        if (t != null && this.index >= 0 && this.index < t.getHeight()) {
            g.drawRegion(t, 0, this.index, 1, 1, 0, this.x, this.y, 0);
        }
    }
}
