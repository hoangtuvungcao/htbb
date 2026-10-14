package game.model;

import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.screen.Font;
import game.render.screen.GameScr;

public class MsgInfo {
    public String[] arr;
    public String message;
    long timeRemove = -1;
    int w;
    int x;

    public MsgInfo(String str) {
        this.message = str;
        this.arr = Font.arialFont11.splitFontBStrInLine(str, GCanvas.w - 100);
        this.w = Font.arialFont11.getWidth(this.arr[0]);
        this.x = GCanvas.w + (this.w / 2);
    }

    public void paint(Graphics graphics) {
        for (int i = 0; i < this.arr.length; i++) {
            if (GCanvas.gameScr.listMSGServer != null && GCanvas.gameScr.listMSGServer.size() > 0 && GCanvas.gameScr.mainChar.receiveQuest && GCanvas.gameScr.isPaint) {
                Font.arialFont11.drawString(graphics, this.arr[i], this.x, (GCanvas.hh - 29) + (i * 13), 2);
            } else {
                Font.arialFont11.drawString(graphics, this.arr[i], this.x, (GCanvas.hh - 53) + (i * 13), 2);
            }
        }
    }

    public void update() {
        if (this.x != GCanvas.w / 2) {
            int i = GCanvas.w / 2;
            int i2 = this.x;
            if (((i - i2) >> 1) == 0) {
                this.x = GCanvas.w / 2;
            } else {
                this.x = i2 + (((GCanvas.w / 2) - this.x) >> 1);
            }
        }
        if (this.x == GCanvas.w / 2) {
            if (this.timeRemove == -1) {
                this.timeRemove = (System.currentTimeMillis() / 1000) + 6;
            }
            if (this.timeRemove - (System.currentTimeMillis() / 1000) <= 0) {
                GameScr.msgWorld.removeElement(this);
            }
        }
    }
}
