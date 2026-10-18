package game.core.j2me;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import com.htbb.client.GameMidlet;
import game.render.GCanvas;
import java.util.Vector;

public class FontSys {
    public static final int ADDMONEY = 6;
    public static final int CENTER = 2;
    public static final int FATAL = 3;
    public static final int FATAL_ME = 8;
    public static final int GREEN = 2;
    public static final int LEFT = 0;
    public static final int MISS = 4;
    public static final int MISS_ME = 7;
    public static final int ORANGE = 5;
    public static final int RED = 0;
    public static final int RIGHT = 1;
    public static final int YELLOW = 1;
    public static FontSys arialFont11;
    public static FontSys arialFontBlack;
    public static FontSys big;
    public static FontSys bigFont1;
    public static FontSys gI = new FontSys(0);
    public static FontSys[] normalFont = new FontSys[6];
    public static FontSys normalFontColor1;
    public static FontSys smallFont1;
    public static FontSys smallFontBLUE;
    public static FontSys smallFontGREEN;
    public static FontSys smallFontRED;
    public static FontSys smallFontShow;
    public static FontSys smallFontWHITE;
    public static FontSys verySmallFont;
    public static FontSys verySmallFont_Red;
    public int height;
    public int id;
    public int idTemp;
    public Paint p;
    public int wight;
    public Boolean isPaintDoubleFont = false;
    public Rect bounds = new Rect();

    public void setHeight(int i) {
    }

    public void setPaint(int i) {
    }

    public static void iniFontsyn() {
        try {
            arialFont11 = new FontSys(7);
            arialFontBlack = new FontSys(8);
            bigFont1 = new FontSys(9);
            smallFont1 = new FontSys(10);
            normalFontColor1 = new FontSys(11);
            verySmallFont = new FontSys(12);
            smallFontWHITE = new FontSys(13);
            smallFontGREEN = new FontSys(14);
            smallFontBLUE = new FontSys(15);
            smallFontRED = new FontSys(16);
            verySmallFont_Red = new FontSys(17);
            smallFontShow = new FontSys(18);
            int i = 0;
            while (true) {
                FontSys[] fontSysArr = normalFont;
                if (i >= fontSysArr.length) {
                    return;
                }
                if (i == 5) {
                    fontSysArr[i] = new FontSys(100);
                } else {
                    fontSysArr[i] = new FontSys(i + 1);
                }
                i++;
            }
        } catch (Exception unused) {
        }
    }

    public FontSys(int i) {
        String str;
        Paint paint = new Paint();
        this.p = paint;
        this.id = i;
        if (i != 0 && i != 14) {
            str = "fonts/cool.ttf";
        } else {
            str = "fonts/big.ttf";
        }
        paint.setTypeface(Typeface.createFromAsset(GameMidlet.asset, str));
        initFontSys();
        this.p.getTextBounds("A", 0, 1, this.bounds);
        this.height = this.bounds.height();
        this.wight = (int) this.p.measureText("O");
    }

    public void initFontSys() {
        float f = GameMidlet.isChaCha ? 2.0f : 0.0f;
        float f2 = 12.0f + f;
        int i = this.id;
        if ((i >= 1 && i <= 6) || i == 100) {
            f2 = 11.0f + f;
        }
        if (i == 7) {
            f2 = 10.0f + f;
        }
        if ((i >= 10 && i <= 16) || i == 8) {
            f2 = f + 9.5f;
        }
        if (i == 9) {
            f2 = 14.0f + f;
        }
        if (i == 12 && GCanvas.screenlevel == 1) {
            this.p.setFakeBoldText(true);
        }
        int i2 = this.id;
        if (i2 == 9 || (i2 >= 14 && i2 <= 17)) {
            this.p.setFakeBoldText(true);
        }
        int i3 = this.id;
        if (i3 == 17) {
            f2 = f + 9.5f;
        }
        if (i3 == 18) {
            f2 = 7.5f + f;
        }
        this.p.setTextSize(f2);
    }

    public static void init() {
        big = new FontSys(0);
    }

    public void setFont(int i) {
        gI = new FontSys(i);
    }

    public static FontSys gI() {
        return gI;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth(String str) {
        return (int) this.p.measureText(str);
    }

    public void drawString(Graphics graphics, String str, int i, int i2, int i3) {
        setColor(-10747904);
        int i4 = i + 1;
        _drawString(graphics, str, i4, i2, i3);
        int i5 = i - 1;
        _drawString(graphics, str, i5, i2, i3);
        int i6 = i2 - 1;
        _drawString(graphics, str, i, i6, i3);
        int i7 = i2 + 1;
        _drawString(graphics, str, i, i7, i3);
        _drawString(graphics, str, i4, i7, i3);
        _drawString(graphics, str, i4, i6, i3);
        _drawString(graphics, str, i5, i6, i3);
        _drawString(graphics, str, i5, i7, i3);
        setColor(-65536);
        _drawString(graphics, str, i, i2, i3);
    }

    public void _drawString(Graphics graphics, String str, int i, int i2, int i3) {
        str.trim();
        graphics.drawString(str, i, i2 + 9, i3, this.p);
    }

    public void drawString(Graphics graphics, String str, int i, int i2, int i3, int i4) {
        str.trim();
        setColor(-16777216);
        graphics.drawString(str, i, i2 + 10, i3, this.p);
        setColor(-65536);
        graphics.drawString(str, i, i2 + 9, i3, this.p);
    }

    public void setColor(int i) {
        this.p.setColor(i);
    }

    public Vector splitFontVector(String str, int i) {
        Vector vector = new Vector();
        String str2 = "";
        int length = 0;
        while (length < str.length()) {
            if (str.charAt(length) == '\n') {
                vector.addElement(str2);
                str2 = "";
            } else {
                str2 = str2 + str.charAt(length);
                if (getWidth(str2) > i) {
                    int length2 = str2.length() - 1;
                    while (length2 >= 0 && str2.charAt(length2) != ' ') {
                        length2--;
                    }
                    if (length2 < 0) {
                        length2 = str2.length() - 1;
                    }
                    vector.addElement(str2.substring(0, length2));
                    length = (length - (str2.length() - length2)) + 1;
                    str2 = "";
                }
                if (length == str.length() - 1 && !str2.trim().equals("")) {
                    vector.addElement(str2);
                }
            }
            length++;
        }
        return vector;
    }

    public String splitFirst(String str) {
        String str2 = "";
        boolean z = false;
        for (int i = 0; i < str.length(); i++) {
            if (!z) {
                String strSubstring = str.substring(i, str.length());
                if (compare(strSubstring, " ")) {
                    str2 = str2 + str.charAt(i) + "-";
                } else {
                    str2 = str2 + strSubstring;
                }
                z = true;
            } else if (str.charAt(i) == ' ') {
                z = false;
            }
        }
        return str2;
    }

    public String[] splitFontArray(String str, int i) {
        Vector vectorSplitFontVector = splitFontVector(str, i);
        String[] strArr = new String[vectorSplitFontVector.size()];
        for (int i2 = 0; i2 < vectorSplitFontVector.size(); i2++) {
            strArr[i2] = vectorSplitFontVector.elementAt(i2).toString();
        }
        return strArr;
    }

    public boolean compare(String str, String str2) {
        for (int i = 0; i < str.length(); i++) {
            if (String.valueOf(str.charAt(i)).equals(str2)) {
                return true;
            }
        }
        return false;
    }
}
