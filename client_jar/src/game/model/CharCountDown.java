package game.model;

import game.render.screen.Font;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;

public class CharCountDown {
    private long maxTime;
    private int w = 60;
    public boolean WantDestroy;
    public String tile;
    long timesv;
    int timeper;
    int per;

    public CharCountDown(long time, String tile) {
        this.tile = "";
        this.tile = tile;
        this.maxTime = time;
        this.timeper = (int) (this.maxTime - System.currentTimeMillis());
    }

    public void paint(Graphics g, int x, int y) {
        long cur = this.maxTime - System.currentTimeMillis();
        int numpercen = (int) (60 - ((cur * ((long) this.w)) / ((long) this.timeper)));
        g.drawRegion(GameScr.imghealth[1], 0, 9, 62, 9, 0, x, y, 0);
        g.drawRegion(GameScr.imghealth[0], 0, 7, numpercen, 7, 0, x, y + 1, 0);
        Font.arialFont.drawString(g, this.tile, x + (this.w / 2) + 1, ((y - 10) + 1) - 3, 2);
        Font.arialFontW.drawString(g, this.tile, x + (this.w / 2), (y - 10) - 3, 2);
        Font.arialFontW.drawString(g, new StringBuffer(String.valueOf(this.per)).append("%").toString(), x + (this.w / 2), y, 2);
    }

    public void update() {
        long cur = this.maxTime - System.currentTimeMillis();
        this.per = (int) (100 - ((cur * 100) / ((long) this.timeper)));
        if (this.maxTime - System.currentTimeMillis() <= 0) {
            this.WantDestroy = true;
        }
    }

    public static String converSecon2minutes(int time) {
        int sec = time % 60;
        int min = (time / 60) % 60;
        if (min <= 0) {
            return new StringBuffer(String.valueOf(sec)).toString();
        }
        return new StringBuffer(String.valueOf(min)).append(":").append(sec).toString();
    }
}
