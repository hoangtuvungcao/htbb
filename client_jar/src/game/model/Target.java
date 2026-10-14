package game.model;

import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

public class Target {
    public short x;
    public short y;
    public byte index = 8;

    public void update() {
        if (Canvas.gameTick % 2 == 0) {
            this.index = (byte) (this.index - 1);
            if (this.index < 0) {
                this.index = (byte) 8;
            }
        }
    }

    public void paint(Graphics g) {
        if (this.x == -1 || Canvas.gameScr.mainChar.state != 1 || this.y == -1) {
            return;
        }
        g.setColor(16777215);
        g.drawArc(((this.x * 16) - 3) + 16, (this.y * 16) - 3, 6, 6, 0, 360);
        g.drawArc(((this.x * 16) - this.index) + 16, (this.y * 16) - this.index, this.index + this.index, this.index + this.index, 0, 360);
        g.drawArc(((this.x * 16) - (this.index + 2)) + 16, (this.y * 16) - (this.index + 2), this.index + 2 + this.index + 2, this.index + 2 + this.index + 2, 0, 360);
    }
}
