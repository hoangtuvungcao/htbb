package game.model;

import game.core.j2me.Graphics;
import game.render.screen.Font;
import game.render.screen.GameData;

public class TimecountDown {
    public short id;
    public short idIcon;
    long mysecond;
    public String tile;
    public byte typeCount;
    public boolean wantdestroy;

    public TimecountDown(short s, long j, String str) {
        this.mysecond = System.currentTimeMillis() + (j * 1000);
        this.tile = str;
        this.id = s;
        this.idIcon = (short) -1;
    }

    public TimecountDown(short s, short s2, long j, String str, byte b) {
        this.mysecond = System.currentTimeMillis() + (1000 * j);
        this.tile = str;
        this.id = s;
        this.idIcon = s2;
        this.typeCount = b;
        if (b == 0) {
            this.mysecond = j;
        }
    }

    public void paint(Graphics graphics, int i, int i2) {
        short s = this.idIcon;
        if (s == -1) {
            if (this.typeCount == 1) {
                int iCurrentTimeMillis = (int) ((this.mysecond - System.currentTimeMillis()) / 1000);
                if (iCurrentTimeMillis > 0) {
                    Font.arialFontBlack.drawString(graphics, this.tile + " : " + converSecon2hours(iCurrentTimeMillis), i - 5, i2 + 1, 1);
                    Font.arialFont11.drawString(graphics, this.tile + " : " + converSecon2hours(iCurrentTimeMillis), i - 4, i2, 1);
                    return;
                }
                return;
            }
            Font.arialFontBlack.drawString(graphics, this.tile, i + 1, i2 + 1, 1);
            Font.arialFont11.drawString(graphics, this.tile, i, i2, 1);
            return;
        }
        ImageIcon imgIcon = GameData.getImgIcon(s);
        if (imgIcon == null || imgIcon.img == null) {
            return;
        }
        byte b = this.typeCount;
        if (b == 0) {
            int width = i - Font.arialFontBlack.getWidth(this.tile + " : ");
            graphics.drawImage(imgIcon.img, width - (imgIcon.img.getWidth() * 2), i2 + (imgIcon.img.getHeight() / 4), 0);
            Font.arialFontBlack.drawString(graphics, this.tile, imgIcon.img.getWidth() + (width - (imgIcon.img.getWidth() * 2)) + 1, (imgIcon.img.getHeight() / 4) + i2 + 1, 0);
            Font.arialFont11.drawString(graphics, this.tile, (width - (imgIcon.img.getWidth() * 2)) + imgIcon.img.getWidth(), i2 + (imgIcon.img.getHeight() / 4), 0);
            return;
        }
        if (b == 1) {
            int iCurrentTimeMillis2 = (int) ((this.mysecond - System.currentTimeMillis()) / 1000);
            int width2 = i - Font.arialFontBlack.getWidth(converSecon2hours(iCurrentTimeMillis2) + ":");
            graphics.drawImage(imgIcon.img, width2 - (imgIcon.img.getWidth() * 2), i2 + (imgIcon.img.getHeight() / 4), 0);
            Font.arialFontBlack.drawString(graphics, " : " + converSecon2hours(iCurrentTimeMillis2), imgIcon.img.getWidth() + (width2 - (imgIcon.img.getWidth() * 2)) + 1, (imgIcon.img.getHeight() / 4) + i2 + 1, 0);
            Font.arialFont11.drawString(graphics, " : " + converSecon2hours(iCurrentTimeMillis2), (width2 - (imgIcon.img.getWidth() * 2)) + imgIcon.img.getWidth(), i2 + (imgIcon.img.getHeight() / 4), 0);
        }
    }

    public void setsecond(long j) {
        this.mysecond = System.currentTimeMillis() + (j * 1000);
    }

    public void update() {
        if (this.typeCount != 1 || System.currentTimeMillis() - this.mysecond < 0) {
            return;
        }
        this.wantdestroy = true;
    }

    public static String converSecon2hours(int i) {
        int i2 = i % 60;
        int i3 = i / 60;
        int i4 = i3 % 60;
        int i5 = i3 / 60;
        if (i5 > 0) {
            return i5 + ":" + i4;
        }
        if (i4 > 0) {
            return i4 + ":" + i2;
        }
        if (i2 < 0) {
            return "0:" + i2;
        }
        return i2 + "";
    }
}
