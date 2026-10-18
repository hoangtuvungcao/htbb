package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.render.screen.Font;
import game.render.screen.GameScr;

public class ChatPopup {
    public static Bitmap imgArrow;
    public static Bitmap imgPopup;
    public int arrowType;
    String[] chats;
    public int h;
    public int isAr;
    public int timeOut;
    public int w;
    public int xc;
    public int yc;

    public static void getImg() {
        imgPopup = FilePack.getImg("c");
        imgArrow = FilePack.getImg("ar");
    }

    public ChatPopup() {
    }

    public ChatPopup(int i, String str, int i2) {
        prepareData(i, str);
        this.isAr = i2;
    }

    public void setPos(int i, int i2) {
        this.xc = i;
        this.yc = i2;
    }

    public boolean setOut() {
        int i = this.timeOut;
        if (i > 0) {
            this.timeOut = i - 1;
        }
        return this.timeOut == 0;
    }

    public void prepareData(int i, String str) {
        String[] strArrSplitFontBStrInLine = Font.arialFontBlack.splitFontBStrInLine(str, 100);
        this.chats = strArrSplitFontBStrInLine;
        GameScr.timeDelayTask = strArrSplitFontBStrInLine.length * 8;
        this.h = (this.chats.length * 14) + 8;
        this.w = 30;
        for (int i2 = 0; i2 < this.chats.length; i2++) {
            int width = Font.arialFontBlack.getWidth(this.chats[i2]) + 5;
            if (width > this.w) {
                this.w = width;
            }
        }
        this.timeOut = i;
    }

    public void paintAnimal(Graphics graphics) {
        int i = this.xc;
        int i2 = this.w;
        int i3 = i - (i2 / 2);
        int i4 = this.yc;
        int i5 = this.h;
        paintRoundRect(graphics, i3, (i4 - i5) - 4, i2, i5, -1, 0);
        if (this.isAr == 1) {
            graphics.drawImage(imgArrow, this.xc, this.yc - 5, Graphics.TOP | Graphics.HCENTER);
        }
        int i6 = this.yc - this.h;
        for (int i7 = 0; i7 < this.chats.length; i7++) {
            Font.arialFontBlack.drawString(graphics, this.chats[i7], this.xc, i6, 2);
            i6 += 14;
        }
    }

    public static void paintRoundRect(Graphics graphics, int i, int i2, int i3, int i4, int i5, int i6) {
        graphics.drawRegion(imgPopup, 0, 0, 8, 8, 0, i, i2, 0);
        int i7 = i + i3;
        int i8 = i7 - 8;
        graphics.drawRegion(imgPopup, 0, 8, 8, 8, 0, i8, i2, 0);
        int i9 = i2 + i4;
        int i10 = i9 - 8;
        graphics.drawRegion(imgPopup, 0, 24, 8, 8, 0, i, i10, 0);
        graphics.drawRegion(imgPopup, 0, 16, 8, 8, 0, i8, i10, 0);
        graphics.setColor(i5);
        int i11 = i + 8;
        int i12 = i3 - 16;
        graphics.fillRect(i11, i2, i12, 8);
        graphics.fillRect(i11, i10, i12, 7);
        int i13 = i2 + 8;
        int i14 = i4 - 16;
        graphics.fillRect(i, i13, i3, i14);
        graphics.setColor(i6);
        graphics.fillRect(i11, i2, i12, 1);
        graphics.fillRect(i11, i9 - 1, i12, 1);
        graphics.fillRect(i, i13, 1, i14);
        graphics.fillRect(i7 - 1, i13, 1, i14);
    }
}
