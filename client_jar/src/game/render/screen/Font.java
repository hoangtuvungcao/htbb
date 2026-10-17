package game.render.screen;

import game.model.FilePack;
import game.pokemon.ChuPokemon;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Font {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int CENTER = 2;
    private Image imgFont;
    private String charList;
    public byte[] charWidth;
    private int charHeight;
    private int charSpace;
    public static Font normalFontColor;
    public static Font borderFont;
    public static Font bigFont;
    public static Font blackFont;
    public static Font arialFont;
    public static Font smallFontYellow;
    public static Font smallFont;
    public static Font arialFontW;
    public static Font[] smallFontColor = new Font[5];
    public static Font[] normalFont = new Font[6];

    public static void init() {
        FilePack.init("/font.sh");
        for (int i = 0; i < 5; i++) {
            normalFont[i] = new Font(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~ĂÁ", new byte[]{4, 6, 5, 6, 6, 7, 6, 6, 6, 6, 6, 3, 3, 3, 4, 5, 4, 4, 6, 5, 8, 8, 6, 6, 10, 6, 7, 5, 7, 6, 4, 7, 7, 3, 4, 6, 3, 9, 7, 7, 7, 7, 5, 5, 4, 7, 6, 9, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 3, 3, 3, 5, 3, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 7, 7, 7, 7, 8, 7, 7, 7, 7, 7, 6, 6, 7, 7, 3, 5, 7, 6, 10, 8, 7, 7, 7, 6, 7, 7, 7, 7, 9, 7, 7, 8, 8, 6, 8, 7}, 13, "fb", i, 0);
        }
        normalFontColor = new Font(" 0123456789.,:!?()+*~#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ$", new byte[]{3, 7, 6, 7, 7, 8, 7, 7, 7, 7, 7, 4, 4, 4, 4, 6, 4, 4, 7, 6, 9, 9, 6, 7, 10, 7, 7, 6, 7, 7, 5, 7, 7, 4, 5, 7, 4, 10, 7, 7, 7, 7, 6, 6, 5, 7, 7, 9, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 7, 7, 8, 7, 8, 7, 7, 7, 7, 7, 7, 7, 7, 8, 7, 8, 7, 7, 4, 4, 4, 6, 4, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 7, 7, 7, 7, 8, 8, 7, 7, 7, 7, 6, 6, 7, 7, 4, 5, 8, 6, 9, 8, 7, 7, 7, 7, 7, 8, 7, 7, 9, 8, 8, 8, 8, 7}, 14, "fb11", 0, -1);
        borderFont = new Font(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~Á", new byte[]{4, 6, 5, 6, 6, 7, 6, 6, 6, 6, 6, 3, 3, 3, 4, 5, 4, 4, 6, 5, 8, 8, 6, 6, 10, 6, 7, 5, 7, 6, 4, 7, 7, 3, 4, 6, 3, 9, 7, 7, 7, 7, 5, 5, 4, 7, 6, 9, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 3, 3, 3, 5, 3, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 7, 7, 7, 7, 8, 7, 7, 7, 7, 7, 6, 6, 7, 7, 3, 5, 7, 6, 10, 8, 7, 7, 7, 6, 7, 7, 7, 7, 9, 7, 7, 8, 8, 6, 7}, 15, "fb1", 0, 0);
        bigFont = new Font(" 0123456789.,:!?()-'/ABCDEFGHIJKLMNOPQRSTUVWXYZÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴĐ", new byte[]{4, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 4, 4, 4, 4, 8, 6, 6, 6, 3, 7, 10, 10, 10, 10, 8, 8, 10, 10, 5, 8, 9, 8, 13, 11, 10, 10, 10, 10, 10, 9, 10, 10, 13, 11, 11, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 5, 5, 5, 5, 5, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10}, 20, "fcg14", 0, 0);
        blackFont = new Font(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~", new byte[]{4, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 2, 2, 6, 4, 3, 6, 5, 6, 7, 3, 3, 10, 6, 6, 5, 6, 6, 4, 6, 6, 2, 2, 6, 2, 10, 6, 6, 6, 6, 4, 6, 3, 6, 5, 9, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 3, 2, 3, 4, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 8, 8, 8, 8, 8, 8, 5, 5, 5, 5, 5, 7, 7, 7, 8, 8, 7, 6, 8, 8, 2, 5, 8, 7, 8, 8, 8, 7, 8, 8, 7, 7, 8, 7, 9, 7, 7, 7, 8, 5}, 14, "f", 0, 0);
        arialFont = new Font(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~@", new byte[]{4, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 2, 2, 6, 4, 4, 6, 5, 6, 7, 4, 4, 10, 6, 6, 6, 6, 6, 4, 6, 6, 2, 2, 5, 2, 8, 6, 6, 6, 6, 4, 6, 3, 6, 6, 10, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 3, 2, 3, 4, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 8, 8, 8, 8, 8, 8, 6, 6, 6, 6, 6, 7, 8, 7, 7, 7, 6, 6, 8, 7, 2, 5, 7, 6, 8, 7, 8, 6, 8, 7, 7, 6, 7, 8, 11, 7, 8, 7, 7, 6, 9}, 13, "/arial11.png", 0);
        smallFontYellow = new Font("0123456789-+abcdefghijklmnopqrstuvwxyz ", new byte[]{10, 6, 10, 10, 10, 10, 11, 10, 10, 9, 8, 8, 11, 10, 10, 10, 9, 9, 10, 10, 6, 8, 11, 9, 11, 11, 11, 10, 11, 10, 10, 11, 11, 11, 11, 11, 11, 10, 5}, 11, "small_f", 0, 0);
        for (int i2 = 0; i2 < 5; i2++) {
            if (i2 != 4) {
                smallFontColor[i2] = new Font("0123456789+-%$:abcdefghijklmnopqrstuvwxyz", new byte[]{5, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 5, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 6, 5, 5, 5, 5, 5, 5, 5, 5, 7, 5, 5, 5}, 8, "fs", i2, -1);
            } else {
                smallFontColor[i2] = new Font("0123456789+-%$:abcdefghijklmnopqrstuvwxyz@", new byte[]{5, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 5, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 6, 5, 5, 5, 5, 5, 5, 5, 5, 7, 5, 5, 5, 7}, 8, "/font/fs4_red.png", -1);
            }
        }
        normalFont[5] = new Font(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~ĂÁ", new byte[]{4, 6, 5, 6, 6, 7, 6, 6, 6, 6, 6, 3, 3, 3, 4, 5, 4, 4, 6, 5, 8, 8, 6, 6, 10, 6, 7, 5, 7, 6, 4, 7, 7, 3, 4, 6, 3, 9, 7, 7, 7, 7, 5, 5, 4, 7, 6, 9, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 3, 3, 3, 5, 3, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 7, 7, 7, 7, 8, 7, 7, 7, 7, 7, 6, 6, 7, 7, 3, 5, 7, 6, 10, 8, 7, 7, 7, 6, 7, 7, 7, 7, 9, 7, 7, 8, 8, 6, 8, 7}, 13, "/font/fbv.png", 0);
        smallFont = new Font("0123456789+-./abcdefghijklmnopqrstuvwxyz:@ ", new byte[]{4, 3, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 1, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 1}, 5, "/font/fss_yellow.png", 1);
        arialFontW = new Font(" 0123456789.,:!?()+*$#/-%abcdefghijklmnopqrstuvwxyzáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđABCDEFGHIJKLMNOPQRSTUVWXYZĐ~", new byte[]{4, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 2, 2, 6, 4, 4, 6, 5, 6, 7, 4, 4, 10, 6, 6, 6, 6, 6, 4, 6, 6, 2, 2, 5, 2, 8, 6, 6, 6, 6, 4, 6, 3, 6, 6, 10, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 3, 2, 3, 4, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 8, 8, 8, 8, 8, 8, 6, 6, 6, 6, 6, 7, 8, 7, 7, 7, 6, 6, 8, 7, 2, 5, 7, 6, 8, 7, 8, 6, 8, 7, 7, 6, 7, 8, 11, 7, 8, 7, 7, 6}, 13, "arialWhite", 0, 0);
        FilePack.reset();
    }

    public Font(String charList, byte[] charWidth, int charHeight, String fontFile, int type, int charSpace) {
        this.charSpace = charSpace;
        this.charList = charList;
        this.charWidth = charWidth;
        this.charHeight = charHeight;
        try {
            byte[] header = FilePack.instance.loadFile(new StringBuffer(String.valueOf(fontFile)).append(type).append("_h").toString());
            byte[] data = FilePack.instance.loadFile(new StringBuffer(String.valueOf(fontFile)).append("_data").toString());
            this.imgFont = Res.createImgByHeader(header, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Font(String charList, byte[] charWidth, int charHeight, String fontFile, int charSpace) {
        this.charSpace = charSpace;
        this.charList = charList;
        this.charWidth = charWidth;
        this.charHeight = charHeight;
        try {
            this.imgFont = Image.createImage(fontFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void drawString(Graphics g, String st, int x, int y, int align) {
        // pokemon (G7.C3): moi chu ve ra man hinh doi sang the gioi Pokemon o day (co bo nho dem)
        st = ChuPokemon.D(st);
        int x1;
        int len = st.length();
        if (align == 0) {
            x1 = x;
        } else if (align == 1) {
            x1 = x - getWidthNguyenVan(st);
        } else {
            x1 = x - (getWidthNguyenVan(st) >> 1);
        }
        for (int i = 0; i < len; i++) {
            int pos = this.charList.indexOf(st.charAt(i));
            if (pos == -1) {
                pos = 0;
            }
            if (pos > -1) {
                g.drawRegion(this.imgFont, 0, pos * this.charHeight, this.imgFont.getWidth(), this.charHeight, 0, x1, y, 20);
            }
            x1 += this.charWidth[pos] + this.charSpace;
        }
    }

    /** Do rong theo chu SAU khi doi (khop chu drawString ve ra). */
    public int getWidth(String st) {
        return getWidthNguyenVan(ChuPokemon.D(st));
    }

    /** Do rong DUNG chu dua vao, khong qua ChuPokemon (TField, ngat dong da doi san). */
    public int getWidthNguyenVan(String st) {
        int len = 0;
        for (int i = 0; i < st.length(); i++) {
            int pos = this.charList.indexOf(st.charAt(i));
            if (pos == -1) {
                pos = 0;
            }
            len += this.charWidth[pos] + this.charSpace;
        }
        return len;
    }

    public String[] splitFontBStrInLine(String src, int lineWidth) {
        // pokemon: ngat dong theo chu SAU khi doi (chu doi co the dai/ngan hon)
        src = ChuPokemon.D(src);
        Vector list = new Vector();
        int srclen = src.length();
        if (srclen <= 1) {
            return new String[]{src};
        }
        String tem = "";
        int start = 0;
        int end = 0;
        src.replace('\t', ' ');
        while (true) {
            if (getWidthNguyenVan(tem) > lineWidth) {
                if (end > 0) {
                    end--;
                }
            } else {
                tem = new StringBuffer(String.valueOf(tem)).append(src.charAt(end)).toString();
                end++;
                if (src.charAt(end) != '\n') {
                    if (end >= srclen - 1) {
                        end = srclen - 1;
                    } else {
                        continue;
                    }
                }
            }
            if (end != srclen - 1 && src.charAt(end + 1) != ' ') {
                int endAnyway = end;
                while (src.charAt(end + 1) != '\n' && ((src.charAt(end + 1) != ' ' || src.charAt(end) == ' ') && end != start)) {
                    end--;
                }
                if (end == start) {
                    end = endAnyway;
                }
            }
            list.addElement(src.substring(start, end + 1));
            if (end == srclen - 1) {
                break;
            }
            start = end + 1;
            while (start != srclen - 1 && src.charAt(start) == ' ') {
                start++;
            }
            if (start == srclen - 1) {
                break;
            }
            end = start;
            tem = "";
        }
        String[] strs = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            strs[i] = (String) list.elementAt(i);
            while (strs[i].length() > 0 && (strs[i].charAt(0) == '\n' || strs[i].charAt(0) == '\r')) {
                strs[i] = strs[i].substring(1);
            }
            while (strs[i].length() > 0 && (strs[i].charAt(strs[i].length() - 1) == '\n' || strs[i].charAt(strs[i].length() - 1) == '\r')) {
                strs[i] = strs[i].substring(0, strs[i].length() - 1);
            }
        }
        return strs;
    }

    public static String[] splitString(String _text, String _searchStr) {
        int count = 0;
        int searchStringLength = _searchStr.length();
        int aa = _text.indexOf(_searchStr, 0);
        while (aa != -1) {
            int pos = aa + searchStringLength;
            aa = _text.indexOf(_searchStr, pos);
            count++;
        }
        String[] sb = new String[count + 1];
        int searchStringPos = _text.indexOf(_searchStr);
        int startPos = 0;
        int index = 0;
        while (searchStringPos != -1) {
            sb[index] = _text.substring(startPos, searchStringPos);
            startPos = searchStringPos + searchStringLength;
            searchStringPos = _text.indexOf(_searchStr, startPos);
            index++;
        }
        sb[index] = _text.substring(startPos, _text.length());
        return sb;
    }

    public static String replace(String _text, String _searchStr, String _replacementStr) {
        StringBuffer sb = new StringBuffer();
        int searchStringPos = _text.indexOf(_searchStr);
        int startPos = 0;
        int searchStringLength = _searchStr.length();
        while (searchStringPos != -1) {
            sb.append(_text.substring(startPos, searchStringPos)).append(_replacementStr);
            startPos = searchStringPos + searchStringLength;
            searchStringPos = _text.indexOf(_searchStr, startPos);
        }
        sb.append(_text.substring(startPos, _text.length()));
        return sb.toString();
    }

    public int getHeight() {
        return this.charHeight;
    }
}
