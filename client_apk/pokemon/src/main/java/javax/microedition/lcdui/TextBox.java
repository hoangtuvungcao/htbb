package javax.microedition.lcdui;

/** TextBox = hộp nhập chữ của Android (bàn phím hệ thống, gõ tiếng Việt được) - xem NenAndroid.hienNhap. */
public class TextBox extends Screen {
    private volatile String text;
    private int maxSize;
    private int constraints;

    public TextBox(String title, String text, int maxSize, int constraints) {
        setTitle(title);
        this.maxSize = maxSize <= 0 ? 500 : maxSize;
        this.constraints = constraints;
        setString(text);
    }

    public String getString() {
        return text == null ? "" : text;
    }

    public void setString(String s) {
        if (s == null) s = "";
        if (s.length() > maxSize) s = s.substring(0, maxSize);
        text = s;
    }

    public int getChars(char[] d) {
        String s = getString();
        s.getChars(0, s.length(), d, 0);
        return s.length();
    }

    public void setChars(char[] d, int off, int len) {
        setString(d == null ? "" : new String(d, off, len));
    }

    public void insert(String s, int pos) {
        String t = getString();
        pos = Math.max(0, Math.min(pos, t.length()));
        setString(t.substring(0, pos) + s + t.substring(pos));
    }

    public void delete(int off, int len) {
        String t = getString();
        setString(t.substring(0, off) + t.substring(off + len));
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int setMaxSize(int m) {
        maxSize = m <= 0 ? 1 : m;
        setString(getString());
        return maxSize;
    }

    public int size() {
        return getString().length();
    }

    public int getCaretPosition() {
        return size();
    }

    public void setConstraints(int c) {
        constraints = c;
    }

    public int getConstraints() {
        return constraints;
    }

    public void setInitialInputMode(String m) {
    }
}
