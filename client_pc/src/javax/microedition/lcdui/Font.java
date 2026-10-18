package javax.microedition.lcdui;

import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

/**
 * javax.microedition.lcdui.Font = font thật (Noto Sans đi kèm, đủ tiếng Việt - htbb.pc.PhongChu), cỡ tính
 * theo điểm ảnh LOGIC; Graphics phóng k lần nên chữ nét ở độ phân giải thật.
 */
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

    private static final FontRenderContext FRC = new FontRenderContext(new AffineTransform(), true, true);
    private static Font macDinh;
    private final int face;
    private final int style;
    private final int size;
    private final java.awt.Font f;
    private final float ascent;
    private final float descent;

    private Font(int face, int style, int size) {
        this.face = face;
        this.style = style;
        this.size = size;
        float ts = size == SIZE_SMALL ? 10 : size == SIZE_LARGE ? 14 : 12;
        java.awt.Font goc = face == FACE_MONOSPACE ? new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12)
                : htbb.pc.PhongChu.thuong((style & STYLE_BOLD) != 0);
        int st = java.awt.Font.PLAIN;
        if ((style & STYLE_ITALIC) != 0) st |= java.awt.Font.ITALIC;
        this.f = goc.deriveFont(st, ts);
        java.awt.font.LineMetrics lm = f.getLineMetrics("Ẩg", FRC);
        ascent = lm.getAscent();
        descent = lm.getDescent();
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

    java.awt.Font awt() {
        return f;
    }

    float descent() {
        return descent;
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
        return (int) Math.ceil(ascent + descent);
    }

    public int getBaselinePosition() {
        return (int) Math.ceil(ascent);
    }

    public int charWidth(char c) {
        return stringWidth(String.valueOf(c));
    }

    public int charsWidth(char[] d, int off, int len) {
        return stringWidth(new String(d, off, len));
    }

    public int stringWidth(String s) {
        return (int) Math.ceil(f.getStringBounds(s, FRC).getWidth());
    }

    public int substringWidth(String s, int off, int len) {
        return stringWidth(s.substring(off, off + len));
    }
}
