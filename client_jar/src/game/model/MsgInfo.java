package game.model;

import game.render.Canvas;
import game.render.screen.Font;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;

public class MsgInfo {
    public String message;
    public String[] arr;
    int x;
    int w;
    long timeRemove = -1;

    public MsgInfo(String message) {
        this.message = message;
        this.arr = Font.arialFontW.splitFontBStrInLine(message, Canvas.w - 8);
        this.w = Font.arialFontW.getWidth(this.arr[0]);
        this.x = Canvas.w + (this.w / 2);
    }

    public void paint(Graphics g) {
        for (int i = 0; i < this.arr.length; i++) {
            Font.arialFontW.drawString(g, this.arr[i], this.x, 1 + (i * 13) + (Canvas.gameScr.tk == null ? 0 : 18), 2);
        }
    }

    public void update() {
        if (this.x != Canvas.w / 2) {
            if ((((Canvas.w / 2) - this.x) >> 1) == 0) {
                this.x = Canvas.w / 2;
            } else {
                this.x += ((Canvas.w / 2) - this.x) >> 1;
            }
        }
        if (this.x == Canvas.w / 2) {
            if (this.timeRemove == -1) {
                this.timeRemove = (System.currentTimeMillis() / 1000) + 6;
            }
            if (this.timeRemove - (System.currentTimeMillis() / 1000) <= 0) {
                GameScr.msgWorld.removeElement(this);
            }
        }
    }
}
