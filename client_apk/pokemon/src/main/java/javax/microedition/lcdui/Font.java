package javax.microedition.lcdui;

import android.graphics.Paint;
import android.graphics.Typeface;

/** javax.microedition.lcdui.Font tối thiểu (game vẽ chữ bằng font ảnh riêng; lớp này chỉ để đủ API). */
public final class Font {
    public static final int FACE_SYSTEM = 0;
    public static final int FACE_MONOSPACE = 32;
    public static final int FACE_PROPORTIONAL = 64;
    public static final int STYLE_PLAIN = 0;
    public static final int STYLE_BOLD = 1;
    public static final int STYLE_ITALIC = 2;
    public static final int STYLE_UNDERLINED = 4;
    public static final int SIZE_SMALL = 8;
    public static final int SIZE_MEDIUM = 0;
    public static final int SIZE_LARGE = 16;
    public static final int FONT_STATIC_TEXT = 0;
    public static final int FONT_INPUT_TEXT = 1;

    private static Font macDinh;
    private final int face;
    private final int style;
    private final int size;
    private final Paint p = new Paint();

    private Font(int face, int style, int size) {
        this.face = face;
        this.style = style;
        this.size = size;
        p.setAntiAlias(true);
        int ts = size == SIZE_SMALL ? 10 : size == SIZE_LARGE ? 15 : 12;
        p.setTextSize(ts);
        int st = Typeface.NORMAL;
        if ((style & STYLE_BOLD) != 0) st |= Typeface.BOLD;
        if ((style & STYLE_ITALIC) != 0) st |= Typeface.ITALIC;
        p.setTypeface(Typeface.create(face == FACE_MONOSPACE ? Typeface.MONOSPACE : Typeface.SANS_SERIF, st));
        p.setUnderlineText((style & STYLE_UNDERLINED) != 0);
    }

    public static Font getDefaultFont() {
        if (macDinh == null) macDinh = new Font(FACE_SYSTEM, STYLE_PLAIN, SIZE_MEDIUM);
        return macDinh;
    }

    public static Font getFont(int specifier) {
        return getDefaultFont();
    }

    public static Font getFont(int face, int style, int size) {
        return new Font(face, style, size);
    }

    Paint paint() {
        return p;
    }

    float descent() {
        return p.descent();
    }

    public int getFace() {
        return face;
    }

    public int getStyle() {
        return style;
    }

    public int getSize() {
        return size;
    }

    public boolean isPlain() {
        return style == 0;
    }

    public boolean isBold() {
        return (style & STYLE_BOLD) != 0;
    }

    public boolean isItalic() {
        return (style & STYLE_ITALIC) != 0;
    }

    public boolean isUnderlined() {
        return (style & STYLE_UNDERLINED) != 0;
    }

    public int getHeight() {
        return (int) Math.ceil(p.descent() - p.ascent());
    }

    public int getBaselinePosition() {
        return (int) Math.ceil(-p.ascent());
    }

    public int charWidth(char c) {
        return (int) Math.ceil(p.measureText(String.valueOf(c)));
    }

    public int charsWidth(char[] d, int off, int len) {
        return (int) Math.ceil(p.measureText(d, off, len));
    }

    public int stringWidth(String s) {
        return (int) Math.ceil(p.measureText(s));
    }

    public int substringWidth(String s, int off, int len) {
        return (int) Math.ceil(p.measureText(s, off, off + len));
    }
}
