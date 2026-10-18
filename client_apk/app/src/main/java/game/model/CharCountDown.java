package game.model;

import game.core.j2me.Graphics;
import game.render.screen.Font;
import game.render.screen.GameScr;

public class CharCountDown {
    public boolean WantDestroy;
    private long maxTime;
    int per;
    public String tile;
    int timeper;
    long timesv;
    private int w = 60;

    public CharCountDown(long j, String str) {
        this.tile = str;
        this.maxTime = j;
        this.timeper = (int) (j - System.currentTimeMillis());
    }

    public void paint(Graphics graphics, int i, int i2) {
        int iCurrentTimeMillis = (int) (60 - (((this.maxTime - System.currentTimeMillis()) * ((long) this.w)) / ((long) this.timeper)));
        graphics.drawRegion(GameScr.imghealth[1], 0, 9, 62, 9, 0, i, i2, 0);
        graphics.drawRegion(GameScr.imghealth[0], 0, 7, iCurrentTimeMillis, 7, 0, i, i2 + 1, 0);
        Font.arialFontBlack.drawString(graphics, this.tile, i + (this.w / 2) + 1, i2 - 12, 2);
        Font.arialFont11.drawString(graphics, this.tile, i + (this.w / 2), i2 - 13, 2);
        Font.normalFont[0].drawString(graphics, "" + this.per + "%", i + (this.w / 2), i2, 2);
    }

    public void update() {
        this.per = (int) (100 - (((this.maxTime - System.currentTimeMillis()) * 100) / ((long) this.timeper)));
        if (this.maxTime - System.currentTimeMillis() <= 0) {
            this.WantDestroy = true;
        }
    }

    public static String converSecon2minutes(int i) {
        int i2 = i % 60;
        int i3 = (i / 60) % 60;
        if (i3 <= 0) {
            return i2 + "";
        }
        return i3 + ":" + i2;
    }
}
