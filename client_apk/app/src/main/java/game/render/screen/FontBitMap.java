package game.render.screen;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.core.j2me.Image;
import game.model.FilePack;
import game.render.Res;
import java.util.Vector;

public class FontBitMap {
    public static final int CENTER = 2;
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static FontBitMap arialFont;
    public static FontBitMap arialFontW;
    public static FontBitMap bigFont;
    public static FontBitMap blackFont;
    public static FontBitMap borderFont;
    public static FontBitMap normalFontColor;
    public static FontBitMap smallFont;
    public static FontBitMap smallFontYellow;
    private int charHeight;
    private String charList;
    private int charSpace;
    public byte[] charWidth;
    private Bitmap imgFont;
    public static FontBitMap[] smallFontColor = new FontBitMap[5];
    public static FontBitMap[] normalFont = new FontBitMap[6];

    public static void init() {
        FilePack.initAsset("font.sh");
        for (int i = 0; i < 5; i++) {
            normalFont[i] = new FontBitMap(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~ĂÁ", new byte[]{4, 6, 5, 6, 6, 7, 6, 6, 6, 6, 6, 3, 3, 3, 4, 5, 4, 4, 6, 5, 8, 8, 6, 6, 10, 6, 7, 5, 7, 6, 4, 7, 7, 3, 4, 6, 3, 9, 7, 7, 7, 7, 5, 5, 4, 7, 6, 9, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 3, 3, 3, 5, 3, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 7, 7, 7, 7, 8, 7, 7, 7, 7, 7, 6, 6, 7, 7, 3, 5, 7, 6, 10, 8, 7, 7, 7, 6, 7, 7, 7, 7, 9, 7, 7, 8, 8, 6, 8, 7}, 13, "fb", i, 0);
        }
        normalFont[5] = new FontBitMap(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~ĂÁ", new byte[]{4, 6, 5, 6, 6, 7, 6, 6, 6, 6, 6, 3, 3, 3, 4, 5, 4, 4, 6, 5, 8, 8, 6, 6, 10, 6, 7, 5, 7, 6, 4, 7, 7, 3, 4, 6, 3, 9, 7, 7, 7, 7, 5, 5, 4, 7, 6, 9, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 3, 3, 3, 5, 3, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 7, 7, 7, 7, 8, 7, 7, 7, 7, 7, 6, 6, 7, 7, 3, 5, 7, 6, 10, 8, 7, 7, 7, 6, 7, 7, 7, 7, 9, 7, 7, 8, 8, 6, 8, 7}, 13, "font/fbv.png", 0, (byte) 0);
        normalFontColor = new FontBitMap(" 0123456789.,:!?()+*~#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ$", new byte[]{3, 7, 6, 7, 7, 8, 7, 7, 7, 7, 7, 4, 4, 4, 4, 6, 4, 4, 7, 6, 9, 9, 6, 7, 10, 7, 7, 6, 7, 7, 5, 7, 7, 4, 5, 7, 4, 10, 7, 7, 7, 7, 6, 6, 5, 7, 7, 9, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 7, 7, 8, 7, 8, 7, 7, 7, 7, 7, 7, 7, 7, 8, 7, 8, 7, 7, 4, 4, 4, 6, 4, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 7, 7, 7, 7, 8, 8, 7, 7, 7, 7, 6, 6, 7, 7, 4, 5, 8, 6, 9, 8, 7, 7, 7, 7, 7, 8, 7, 7, 9, 8, 8, 8, 8, 7}, 14, "fb11", 0, -1);
        borderFont = new FontBitMap(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~Á", new byte[]{4, 6, 5, 6, 6, 7, 6, 6, 6, 6, 6, 3, 3, 3, 4, 5, 4, 4, 6, 5, 8, 8, 6, 6, 10, 6, 7, 5, 7, 6, 4, 7, 7, 3, 4, 6, 3, 9, 7, 7, 7, 7, 5, 5, 4, 7, 6, 9, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 3, 3, 3, 5, 3, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 7, 7, 7, 7, 8, 7, 7, 7, 7, 7, 6, 6, 7, 7, 3, 5, 7, 6, 10, 8, 7, 7, 7, 6, 7, 7, 7, 7, 9, 7, 7, 8, 8, 6, 7}, 15, "fb1", 0, 0);
        bigFont = new FontBitMap(" 0123456789.,:!?()-'/ABCDEFGHIJKLMNOPQRSTUVWXYZÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴĐ", new byte[]{4, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 4, 4, 4, 4, 8, 6, 6, 6, 3, 7, 10, 10, 10, 10, 8, 8, 10, 10, 5, 8, 9, 8, 13, 11, 10, 10, 10, 10, 10, 9, 10, 10, 13, 11, 11, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 5, 5, 5, 5, 5, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10}, 20, "fcg14", 0, 0);
        blackFont = new FontBitMap(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~", new byte[]{4, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 2, 2, 6, 4, 3, 6, 5, 6, 7, 3, 3, 10, 6, 6, 5, 6, 6, 4, 6, 6, 2, 2, 6, 2, 10, 6, 6, 6, 6, 4, 6, 3, 6, 5, 9, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 3, 2, 3, 4, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 8, 8, 8, 8, 8, 8, 5, 5, 5, 5, 5, 7, 7, 7, 8, 8, 7, 6, 8, 8, 2, 5, 8, 7, 8, 8, 8, 7, 8, 8, 7, 7, 8, 7, 9, 7, 7, 7, 8, 5}, 14, "f", 0, 0);
        arialFont = new FontBitMap(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~", new byte[]{4, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 2, 2, 6, 4, 4, 6, 5, 6, 7, 4, 4, 10, 6, 6, 6, 6, 6, 4, 6, 6, 2, 2, 5, 2, 8, 6, 6, 6, 6, 4, 6, 3, 6, 6, 10, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 3, 2, 3, 4, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 8, 8, 8, 8, 8, 8, 6, 6, 6, 6, 6, 7, 8, 7, 7, 7, 6, 6, 8, 7, 2, 5, 7, 6, 8, 7, 8, 6, 8, 7, 7, 6, 7, 8, 11, 7, 8, 7, 7, 6}, 13, "arial11", 0, 0);
        smallFontYellow = new FontBitMap("0123456789-+abcdefghijklmnopqrstuvwxyz ", new byte[]{10, 6, 10, 10, 10, 10, 11, 10, 10, 9, 8, 8, 11, 10, 10, 10, 9, 9, 10, 10, 6, 8, 11, 9, 11, 11, 11, 10, 11, 10, 10, 11, 11, 11, 11, 11, 11, 10, 5}, 11, "small_f", 0, 0);
        for (int i2 = 0; i2 < 5; i2++) {
            if (i2 != 4) {
                smallFontColor[i2] = new FontBitMap("0123456789+-%$:abcdefghijklmnopqrstuvwxyz", new byte[]{5, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 5, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 6, 5, 5, 5, 5, 5, 5, 5, 5, 7, 5, 5, 5}, 8, "fs", i2, -1);
            } else {
                smallFontColor[i2] = new FontBitMap("0123456789+-%$:abcdefghijklmnopqrstuvwxyz@", new byte[]{5, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 5, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 6, 5, 5, 5, 5, 5, 5, 5, 5, 7, 5, 5, 5, 7}, 8, "font/fs4_red.png", -1, true);
            }
        }
        smallFont = new FontBitMap("0123456789+-./abcdefghijklmnopqrstuvwxyz:@ ", new byte[]{4, 3, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 1, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 1}, 5, "font/fss_yellow.png", 1, true);
        arialFontW = new FontBitMap(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~", new byte[]{4, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 2, 2, 6, 4, 4, 6, 5, 6, 7, 4, 4, 10, 6, 6, 6, 6, 6, 4, 6, 6, 2, 2, 5, 2, 8, 6, 6, 6, 6, 4, 6, 3, 6, 6, 10, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 3, 2, 3, 4, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 8, 8, 8, 8, 8, 8, 6, 6, 6, 6, 6, 7, 8, 7, 7, 7, 6, 6, 8, 7, 2, 5, 7, 6, 8, 7, 8, 6, 8, 7, 7, 6, 7, 8, 11, 7, 8, 7, 7, 6}, 13, "arialWhite", 0, 0);
        FilePack.reset();
    }

    public FontBitMap(String str, byte[] bArr, int i, String str2, int i2, int i3) {
        this.charSpace = i3;
        this.charList = str;
        this.charWidth = bArr;
        this.charHeight = i;
        try {
            this.imgFont = Res.createImgByHeader(FilePack.instance.loadFile(str2 + i2 + "_h"), FilePack.instance.loadFile(str2 + "_data"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FontBitMap(String str, byte[] bArr, int i, String str2, int i2) {
        this.charSpace = i2;
        this.charList = str;
        this.charWidth = bArr;
        this.charHeight = i;
        try {
            this.imgFont = Image.createImage(str2);
        } catch (Exception unused) {
        }
    }

    public FontBitMap(String str, byte[] bArr, int i, String str2, int i2, byte b) {
        this.charSpace = i2;
        this.charList = str;
        this.charWidth = bArr;
        this.charHeight = i;
        try {
            this.imgFont = Image.createImageAsset(str2);
        } catch (Exception unused) {
        }
    }

    public FontBitMap(String str, byte[] bArr, int i, String str2, int i2, boolean z) {
        this.charSpace = i2;
        this.charList = str;
        this.charWidth = bArr;
        this.charHeight = i;
        try {
            this.imgFont = Image.loadImageFromAsset(str2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void drawString(Graphics graphics, String str, int i, int i2, int i3) {
        int width;
        int i4;
        int length = str.length();
        if (i3 == 0) {
            i4 = i;
        } else {
            if (i3 == 1) {
                width = getWidth(str);
            } else {
                width = getWidth(str) >> 1;
            }
            i4 = i - width;
        }
        for (int i5 = 0; i5 < length; i5++) {
            int iIndexOf = this.charList.indexOf(str.charAt(i5));
            int i6 = iIndexOf == -1 ? 0 : iIndexOf;
            if (i6 > -1) {
                Bitmap bitmap = this.imgFont;
                graphics.drawRegion(bitmap, 0, i6 * this.charHeight, bitmap.getWidth(), this.charHeight, 0, i4, i2, 20);
            }
            i4 += this.charWidth[i6] + this.charSpace;
        }
    }

    public int getWidth(String str) {
        int i = 0;
        for (int i2 = 0; i2 < str.length(); i2++) {
            int iIndexOf = this.charList.indexOf(str.charAt(i2));
            if (iIndexOf == -1) {
                iIndexOf = 0;
            }
            i += this.charWidth[iIndexOf] + this.charSpace;
        }
        return i;
    }

    public String[] splitFontBStrInLine(String str, int i) {
        Vector vector = new Vector();
        int length = str.length();
        if (length <= 1) {
            return new String[]{str};
        }
        str.replace('\t', ' ');
        int i2 = 0;
        int i3 = 0;
        String str2 = "";
        while (true) {
            if (getWidth(str2) <= i) {
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
            } else if (i2 > 0) {
                i2--;
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
        String[] strArr = new String[vector.size()];
        for (int i9 = 0; i9 < vector.size(); i9++) {
            strArr[i9] = (String) vector.elementAt(i9);
            while (strArr[i9].length() > 0 && (strArr[i9].charAt(0) == '\n' || strArr[i9].charAt(0) == '\r')) {
                strArr[i9] = strArr[i9].substring(1);
            }
            while (strArr[i9].length() > 0) {
                String str3 = strArr[i9];
                if (str3.charAt(str3.length() - 1) != '\n') {
                    String str4 = strArr[i9];
                    if (str4.charAt(str4.length() - 1) != '\r') {
                        break;
                    }
                }
                String str5 = strArr[i9];
                strArr[i9] = str5.substring(0, str5.length() - 1);
            }
        }
        return strArr;
    }

    public static String[] splitString(String str, String str2) {
        int length = str2.length();
        int i = 0;
        int iIndexOf = str.indexOf(str2, 0);
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

    public static String replace(String str, String str2, String str3) {
        StringBuffer stringBuffer = new StringBuffer();
        int iIndexOf = str.indexOf(str2);
        int length = str2.length();
        int i = 0;
        while (iIndexOf != -1) {
            stringBuffer.append(str.substring(i, iIndexOf)).append(str3);
            i = iIndexOf + length;
            iIndexOf = str.indexOf(str2, i);
        }
        stringBuffer.append(str.substring(i, str.length()));
        return stringBuffer.toString();
    }

    public int getHeight() {
        return this.charHeight;
    }
}
