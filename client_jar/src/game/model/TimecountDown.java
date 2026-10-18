package game.model;

import game.render.screen.Font;
import game.render.screen.GameData;
import javax.microedition.lcdui.Graphics;

public class TimecountDown {
    long mysecond;
    public String tile;
    public boolean wantdestroy;
    public short id;
    public short idIcon;
    public byte typeCount;

    public TimecountDown(short id, long second, String tile) {
        this.mysecond = System.currentTimeMillis() + (second * 1000);
        this.tile = tile;
        this.id = id;
        this.idIcon = (short) -1;
    }

    public TimecountDown(short id, short idIcon, long second, String tile, byte type) {
        this.mysecond = System.currentTimeMillis() + (second * 1000);
        this.tile = tile;
        this.id = id;
        this.idIcon = idIcon;
        this.typeCount = type;
        if (this.typeCount == 0) {
            this.mysecond = second;
        }
    }

    public void paint(Graphics g, int x, int y) {
        if (this.idIcon == -1) {
            if (this.typeCount == 1) {
                int time = (int) ((this.mysecond - System.currentTimeMillis()) / 1000);
                if (time > 0) {
                    Font.arialFont.drawString(g, new StringBuffer(String.valueOf(this.tile)).append(" : ").append(converSecon2hours(time)).toString(), x - 5, y + 1, 1);
                    Font.arialFontW.drawString(g, new StringBuffer(String.valueOf(this.tile)).append(" : ").append(converSecon2hours(time)).toString(), x - 4, y, 1);
                    return;
                }
                return;
            }
            Font.arialFont.drawString(g, this.tile, x + 1, y + 1, 1);
            Font.arialFontW.drawString(g, this.tile, x, y, 1);
            return;
        }
        ImageIcon img = GameData.getImgIcon(this.idIcon);
        if (img != null && img.img != null) {
            if (this.typeCount == 0) {
                int ws = Font.arialFont.getWidth(new StringBuffer(String.valueOf(this.tile)).append(" : ").toString());
                g.drawImage(img.img, (x - ws) - (img.img.getWidth() * 2), y + (img.img.getHeight() / 4), 0);
                Font.arialFont.drawString(g, this.tile, ((x - ws) - (img.img.getWidth() * 2)) + 1 + img.img.getWidth(), y + 1 + (img.img.getHeight() / 4), 0);
                Font.arialFontW.drawString(g, this.tile, ((x - ws) - (img.img.getWidth() * 2)) + img.img.getWidth(), y + (img.img.getHeight() / 4), 0);
                return;
            }
            if (this.typeCount == 1) {
                int time2 = (int) ((this.mysecond - System.currentTimeMillis()) / 1000);
                int ws2 = Font.arialFont.getWidth(new StringBuffer(String.valueOf(converSecon2hours(time2))).append(":").toString());
                g.drawImage(img.img, (x - ws2) - (img.img.getWidth() * 2), y + (img.img.getHeight() / 4), 0);
                Font.arialFont.drawString(g, new StringBuffer(" : ").append(converSecon2hours(time2)).toString(), ((x - ws2) - (img.img.getWidth() * 2)) + 1 + img.img.getWidth(), y + 1 + (img.img.getHeight() / 4), 0);
                Font.arialFontW.drawString(g, new StringBuffer(" : ").append(converSecon2hours(time2)).toString(), ((x - ws2) - (img.img.getWidth() * 2)) + img.img.getWidth(), y + (img.img.getHeight() / 4), 0);
            }
        }
    }

    public void setsecond(long second) {
        this.mysecond = System.currentTimeMillis() + (second * 1000);
    }

    public void update() {
        if (this.typeCount == 1 && System.currentTimeMillis() - this.mysecond >= 0) {
            this.wantdestroy = true;
        }
    }

    public static String converSecon2hours(int totalSeconds) {
        int seconds = totalSeconds % 60;
        int totalMinutes = totalSeconds / 60;
        int minutes = totalMinutes % 60;
        int hours = totalMinutes / 60;
        if (hours > 0) {
            return new StringBuffer(String.valueOf(hours)).append(":").append(minutes).toString();
        }
        if (minutes > 0) {
            return new StringBuffer(String.valueOf(minutes)).append(":").append(seconds).toString();
        }
        if (seconds < 0) {
            return new StringBuffer("0:").append(seconds).toString();
        }
        return new StringBuffer(String.valueOf(seconds)).toString();
    }
}
