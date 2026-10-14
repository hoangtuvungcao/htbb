package game.render.screen;

import android.content.res.Resources;
import game.core.j2me.FontSys;
import game.core.j2me.Graphics;
import game.render.GCanvas;
import java.util.Vector;

public class Font {
    public static final int ARIALFONT11 = 3;
    public static final int ARIALFONTBLACK = 4;
    public static final int BIGFONT = 5;
    public static final int CENTER = 2;
    public static final int LEFT = 0;
    public static final int NORMALFONTCOLOR = 7;
    public static final int RIGHT = 1;
    public static final int SMALLFONT = 6;
    public static final int SMALLFONTBLUE = 11;
    public static final int SMALLFONTGREEN = 10;
    public static final int SMALLFONTRED = 12;
    public static final int SMALLFONTWHITE = 9;
    public static final int SMALLFONTWHITESHOW = 15;
    public static final int VERYSMALLFONT = 8;
    public static final int VERYSMALLFONT_RED = 13;
    public static Font arialFont11 = null;
    public static Font arialFontBlack = null;
    public static Font bigFont1 = null;
    public static Font[] normalFont = new Font[6];
    public static Font normalFontColor1 = null;
    public static Font smallFont1 = null;
    public static Font smallFontBLUE = null;
    public static Font smallFontGREEN = null;
    public static Font smallFontRED = null;
    public static Font smallFontWHITE = null;
    public static Font smallFontWHITEShow = null;
    public static Font smallFontYellow = null;
    public static final int smallFontYellowID = 14;
    public static Font verySmallFont;
    public static Font verySmallFont_Red;
    public FontBitMap fBitMap;
    public int id;
    public FontSys mFontSys;
    private int pos;
    private String stMap2;
    public int y0;

    public void input() {
        arialFont11 = new Font(3, GCanvas.res);
        arialFontBlack = new Font(4, GCanvas.res);
        bigFont1 = new Font(5, GCanvas.res);
        smallFont1 = new Font(6, GCanvas.res);
        normalFontColor1 = new Font(7, GCanvas.res);
        verySmallFont = new Font(8, GCanvas.res);
        verySmallFont_Red = new Font(13, GCanvas.res);
        smallFontWHITE = new Font(9, GCanvas.res);
        smallFontGREEN = new Font(10, GCanvas.res);
        smallFontBLUE = new Font(11, GCanvas.res);
        smallFontRED = new Font(12, GCanvas.res);
        smallFontYellow = new Font(14, GCanvas.res);
        smallFontWHITEShow = new Font(15, GCanvas.res);
        int i = 0;
        while (true) {
            Font[] fontArr = normalFont;
            if (i >= fontArr.length) {
                return;
            }
            if (i == 5) {
                fontArr[i] = new Font(100);
            } else {
                fontArr[i] = new Font(i);
            }
            i++;
        }
    }

    public Font(int i) {
        this.id = i;
        this.mFontSys = FontSys.normalFont[i == 100 ? 5 : i];
        if (GCanvas.screenlevel == 1) {
            this.fBitMap = FontBitMap.normalFont[i == 100 ? 5 : i];
        }
    }

    public Font(int i, Resources resources) {
        switch (i) {
            case 3:
                if (GCanvas.screenlevel == 1) {
                    this.fBitMap = FontBitMap.borderFont;
                    this.y0 = -2;
                }
                this.mFontSys = FontSys.arialFont11;
                break;
            case 4:
                if (GCanvas.screenlevel == 1) {
                    this.fBitMap = FontBitMap.arialFont;
                }
                this.mFontSys = FontSys.arialFontBlack;
                break;
            case 5:
                if (GCanvas.screenlevel == 1) {
                    this.fBitMap = FontBitMap.bigFont;
                    this.y0 -= 5;
                }
                this.mFontSys = FontSys.bigFont1;
                break;
            case 6:
                if (GCanvas.screenlevel == 1) {
                    this.fBitMap = FontBitMap.smallFont;
                }
                this.mFontSys = FontSys.smallFont1;
                break;
            case 7:
                if (GCanvas.screenlevel == 1) {
                    this.fBitMap = FontBitMap.borderFont;
                }
                this.mFontSys = FontSys.normalFontColor1;
                break;
            case 8:
                if (GCanvas.screenlevel == 1) {
                    this.fBitMap = FontBitMap.smallFont;
                }
                this.mFontSys = FontSys.verySmallFont;
                break;
            case 9:
                if (GCanvas.screenlevel == 1) {
                    this.fBitMap = FontBitMap.smallFontColor[0];
                }
                this.mFontSys = FontSys.smallFontWHITE;
                break;
            case 10:
                if (GCanvas.screenlevel == 1) {
                    this.fBitMap = FontBitMap.smallFontColor[2];
                }
                this.mFontSys = FontSys.smallFontGREEN;
                break;
            case 11:
                if (GCanvas.screenlevel == 1) {
                    this.fBitMap = FontBitMap.smallFontColor[3];
                }
                this.mFontSys = FontSys.smallFontBLUE;
                break;
            case 12:
                if (GCanvas.screenlevel == 1) {
                    this.fBitMap = FontBitMap.smallFontColor[4];
                }
                this.mFontSys = FontSys.smallFontRED;
                break;
            case 13:
                if (GCanvas.screenlevel == 1) {
                    this.fBitMap = FontBitMap.smallFontColor[4];
                }
                this.mFontSys = FontSys.verySmallFont_Red;
                break;
            case 14:
                if (GCanvas.screenlevel == 1) {
                    this.fBitMap = FontBitMap.smallFontColor[1];
                }
                this.mFontSys = FontSys.smallFontWHITE;
                break;
            case 15:
                this.mFontSys = FontSys.smallFontShow;
                break;
        }
    }

    public static String[] splitString(String str, String str2) {
        int length = str2.length();
        int i = 0;
        int iIndexOf = str.indexOf(str2, 0);
        if (GCanvas.screenlevel == 1) {
            return FontBitMap.splitString(str, str2);
        }
        int i2 = 0;
        while (iIndexOf != -1) {
            iIndexOf = str.indexOf(str2, iIndexOf + length);
            i2++;
        }
        String[] strArr = new String[i2 + 1];
        int iIndexOf2 = str.indexOf(str2);
        int i3 = 0;
        while (iIndexOf2 != -1) {
            strArr[i] = str.substring(i3, iIndexOf2);
            i3 = iIndexOf2 + length;
            iIndexOf2 = str.indexOf(str2, i3);
            i++;
        }
        strArr[i] = str.substring(i3, str.length());
        return strArr;
    }

    public void drawString(String str, int i, int i2, int i3, Graphics graphics) {
        drawString(graphics, str, i, i2, i3);
    }

    public void drawString(Graphics graphics, String str, int i, int i2, int i3) {
        FontBitMap fontBitMap = this.fBitMap;
        if (fontBitMap != null) {
            fontBitMap.drawString(graphics, str, i, i2 + this.y0, i3);
            return;
        }
        Font[] fontArr = normalFont;
        if (this == fontArr[0]) {
            this.mFontSys.setColor(-16777216);
            graphics.drawString(str, i, i2 + 12, i3, this.mFontSys.p);
            this.mFontSys.setColor(-1);
            graphics.drawString(str, i, i2 + 11, i3, this.mFontSys.p);
            return;
        }
        if (this == fontArr[1]) {
            this.mFontSys.setColor(-16777216);
            graphics.drawString(str, i, i2 + 12, i3, this.mFontSys.p);
            this.mFontSys.setColor(-16711936);
            graphics.drawString(str, i, i2 + 11, i3, this.mFontSys.p);
            return;
        }
        if (this == fontArr[2]) {
            this.mFontSys.setColor(-16777216);
            graphics.drawString(str, i, i2 + 12, i3, this.mFontSys.p);
            this.mFontSys.setColor(-65536);
            graphics.drawString(str, i, i2 + 11, i3, this.mFontSys.p);
            return;
        }
        if (this == fontArr[3]) {
            this.mFontSys.setColor(-16777216);
            graphics.drawString(str, i, i2 + 12, i3, this.mFontSys.p);
            this.mFontSys.setColor(-16776961);
            graphics.drawString(str, i, i2 + 11, i3, this.mFontSys.p);
            return;
        }
        if (this == fontArr[4]) {
            this.mFontSys.setColor(-16777216);
            graphics.drawString(str, i, i2 + 12, i3, this.mFontSys.p);
            this.mFontSys.setColor(-6681155);
            graphics.drawString(str, i, i2 + 11, i3, this.mFontSys.p);
            return;
        }
        if (this == fontArr[5]) {
            this.mFontSys.setColor(-6868);
            graphics.drawString(str, i, i2 + 12, i3, this.mFontSys.p);
            return;
        }
        if (this == normalFontColor1) {
            if (str.length() == 0) {
                return;
            }
            this.mFontSys.setColor(-16777216);
            graphics.drawString(str, i, i2 + 11, i3, this.mFontSys.p);
            this.mFontSys.setColor(-1);
            graphics.drawString(str, i, i2 + 10, i3, this.mFontSys.p);
            return;
        }
        if (this == arialFont11) {
            this.mFontSys.setColor(-16777216);
            graphics.drawString(str, i, i2 + 11, i3, this.mFontSys.p);
            this.mFontSys.setColor(-1);
            graphics.drawString(str, i, i2 + 10, i3, this.mFontSys.p);
            return;
        }
        if (this == arialFontBlack) {
            this.mFontSys.setColor(-16777216);
            graphics.drawString(str, i, i2 + 10, i3, this.mFontSys.p);
            return;
        }
        if (this == bigFont1) {
            this.mFontSys.setColor(-16777216);
            graphics.drawString(str, i, i2 + 18, i3, this.mFontSys.p);
            this.mFontSys.setColor(-1);
            graphics.drawString(str, i, i2 + 17, i3, this.mFontSys.p);
            return;
        }
        if (this == verySmallFont) {
            this.mFontSys.setColor(-16777216);
            graphics.drawString(str, i, i2 + 6, i3, this.mFontSys.p);
            this.mFontSys.setColor(-256);
            graphics.drawString(str, i, i2 + 5, i3, this.mFontSys.p);
            return;
        }
        if (this == smallFont1) {
            this.mFontSys.setColor(-16777216);
            graphics.drawString(str, i, i2 + 6, i3, this.mFontSys.p);
            this.mFontSys.setColor(-256);
            graphics.drawString(str, i, i2 + 5, i3, this.mFontSys.p);
            return;
        }
        if (this == smallFontWHITE) {
            this.mFontSys.setColor(-16777216);
            graphics.drawString(str, i, i2 + 6, i3, this.mFontSys.p);
            this.mFontSys.setColor(-1);
            graphics.drawString(str, i, i2 + 5, i3, this.mFontSys.p);
            return;
        }
        if (this == smallFontRED) {
            this.mFontSys.setColor(-16777216);
            int i4 = i + 2;
            graphics.drawString(str, i4, i2 + 8, i3, this.mFontSys.p);
            this.mFontSys.setColor(-65536);
            graphics.drawString(str, i4, i2 + 7, i3, this.mFontSys.p);
            return;
        }
        if (this == smallFontBLUE) {
            this.mFontSys.setColor(-16777216);
            int i5 = i + 2;
            graphics.drawString(str, i5, i2 + 8, i3, this.mFontSys.p);
            this.mFontSys.setColor(-16776961);
            graphics.drawString(str, i5, i2 + 7, i3, this.mFontSys.p);
            return;
        }
        if (this == smallFontGREEN) {
            this.mFontSys.setColor(-16777216);
            int i6 = i + 2;
            graphics.drawString(str, i6, i2 + 8, i3, this.mFontSys.p);
            this.mFontSys.setColor(-16711936);
            graphics.drawString(str, i6, i2 + 7, i3, this.mFontSys.p);
            return;
        }
        if (this == verySmallFont_Red) {
            this.mFontSys.setColor(-16777216);
            int i7 = i + 1;
            graphics.drawString(str, i7, i2 + 8, i3, this.mFontSys.p);
            this.mFontSys.setColor(-65536);
            graphics.drawString(str, i7, i2 + 7, i3, this.mFontSys.p);
            return;
        }
        if (this == smallFontWHITEShow) {
            this.mFontSys.setColor(-1);
            graphics.drawStringCell(str, i, i2 + 4, i3, this.mFontSys.p, -530680, -16762575);
        }
    }

    public int getWidth(String str) {
        FontBitMap fontBitMap;
        if (GCanvas.screenlevel == 1 && (fontBitMap = this.fBitMap) != null) {
            return fontBitMap.getWidth(str);
        }
        return this.mFontSys.getWidth(str);
    }

    public String[] splitFontBStrInLine(String str, int i) {
        FontBitMap fontBitMap;
        Vector vector = new Vector();
        if (GCanvas.screenlevel == 1 && (fontBitMap = this.fBitMap) != null) {
            return fontBitMap.splitFontBStrInLine(str, i);
        }
        int length = str.length();
        if (length <= 1) {
            return new String[]{str};
        }
        String str2 = "";
        int i2 = 0;
        int i3 = 0;
        while (true) {
            if (getWidth(str2) < i) {
                str2 = str2 + str.charAt(i2);
                i2++;
                if (str.charAt(i2) != '\n') {
                    int i4 = length - 1;
                    if (i2 >= i4) {
                        i2 = i4;
                    } else {
                        continue;
                    }
                }
            }
            int i5 = length - 1;
            if (i2 != i5 && str.charAt(i2 + 1) != ' ') {
                int i6 = i2;
                while (true) {
                    int i7 = i6 + 1;
                    if (str.charAt(i7) == '\n' || ((str.charAt(i7) == ' ' && str.charAt(i6) != ' ') || i6 == i3)) {
                        break;
                    }
                    i6--;
                }
                if (i6 != i3) {
                    i2 = i6;
                }
            }
            int i8 = i2 + 1;
            vector.addElement(str.substring(i3, i8));
            if (i2 == i5) {
                break;
            }
            i3 = i8;
            while (i3 != i5 && str.charAt(i3) == ' ') {
                i3++;
            }
            if (i3 == i5) {
                break;
            }
            str2 = "";
            i2 = i3;
        }
        int size = vector.size();
        String[] strArr = new String[size];
        for (int i9 = 0; i9 < size; i9++) {
            strArr[i9] = ((String) vector.elementAt(i9)).trim();
        }
        return strArr;
    }

    public int getHeight() {
        FontBitMap fontBitMap;
        if (GCanvas.screenlevel == 1 && (fontBitMap = this.fBitMap) != null) {
            return fontBitMap.getHeight();
        }
        return this.mFontSys.getHeight();
    }
}
