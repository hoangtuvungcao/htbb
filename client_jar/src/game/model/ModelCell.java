package game.model;

import game.render.Res;
import javax.microedition.lcdui.Graphics;

public class ModelCell {
    public int x;
    public int y;
    public int xTo;
    public int yTo;
    public int id;
    public int saveX;
    public int saveY;
    public int index = 0;

    public void paint(Graphics g) {
        g.setColor(15852810);
        g.fillRect(this.x - 16, this.y - 16, 32, 32);
        g.setColor(34949);
        g.fillRect(this.x - 14, this.y - 14, 28, 28);
        g.drawImage(Res.imgBox[this.index], this.x, this.y, 3);
    }

    public void update() {
        if (this.x != this.xTo) {
            if (((this.xTo - this.x) >> 1) == 0) {
                this.x = this.xTo;
            } else {
                this.x += (this.xTo - this.x) >> 1;
            }
        }
        if (this.y != this.yTo) {
            if (((this.yTo - this.y) >> 1) == 0) {
                this.y = this.yTo;
            } else {
                this.y += (this.yTo - this.y) >> 1;
            }
        }
    }
}
