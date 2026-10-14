package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.screen.GameScr;

public class ImageMapBig {
    public int himg;
    public int id;
    public Bitmap img;
    public boolean isLoadOK;
    public boolean isShow;
    public String path;
    public int timeRemove;
    public int wimg;
    public int x;
    public int y;

    public boolean isPaint(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        return i <= i4 && i2 >= i3 && i5 <= i8 && i6 >= i7;
    }

    public void update() {
        int i = this.x;
        int i2 = i - 10;
        int i3 = i + this.wimg + 10;
        GameScr gameScr = GCanvas.gameScr;
        int i4 = GameScr.cmx;
        GameScr gameScr2 = GCanvas.gameScr;
        int i5 = GameScr.cmx + GCanvas.w;
        int i6 = this.y;
        int i7 = i6 - 10;
        int i8 = i6 + this.himg + 10;
        GameScr gameScr3 = GCanvas.gameScr;
        int i9 = GameScr.cmy;
        GameScr gameScr4 = GCanvas.gameScr;
        if (isPaint(i2, i3, i4, i5, i7, i8, i9, GameScr.cmy + GCanvas.h)) {
            this.isShow = true;
            this.timeRemove = (int) (System.currentTimeMillis() / 1000);
        } else {
            this.isShow = false;
        }
        if (!this.isLoadOK || (System.currentTimeMillis() / 1000) - ((long) this.timeRemove) <= 360) {
            return;
        }
        this.img = null;
        this.isLoadOK = false;
    }

    public void paint(Graphics graphics) {
        if (this.isShow) {
            Bitmap bitmap = this.img;
            if (bitmap == null) {
                this.img = Tilemap.loadImageMap(this);
            } else {
                graphics.drawImage(bitmap, this.x, this.y);
            }
        }
    }
}
