package javax.microedition.lcdui;

import javax.microedition.midlet.MIDlet;

public class Display {
    public static final int LIST_ELEMENT = 1;
    public static final int CHOICE_GROUP_ELEMENT = 2;
    public static final int ALERT = 3;
    public static final int COLOR_BACKGROUND = 0;
    public static final int COLOR_FOREGROUND = 1;
    public static final int COLOR_HIGHLIGHTED_BACKGROUND = 2;
    public static final int COLOR_HIGHLIGHTED_FOREGROUND = 3;
    public static final int COLOR_BORDER = 4;
    public static final int COLOR_HIGHLIGHTED_BORDER = 5;

    private static final Display ME = new Display();
    static volatile Displayable hienTai;
    /** Canvas cuối cùng được hiện: vẫn vẽ phía sau khi hộp nhập chữ đang mở. */
    static volatile Canvas canvasCuoi;

    private Display() {
    }

    public static Display getDisplay(MIDlet m) {
        return ME;
    }

    public Displayable getCurrent() {
        return hienTai;
    }

    public void setCurrent(Displayable d) {
        if (d == null) return;
        final Displayable cu = hienTai;
        hienTai = d;
        if (d instanceof Canvas) {
            final Canvas c = (Canvas) d;
            canvasCuoi = c;
            NenAndroid.anNhap();
            if (cu != c) {
                NenAndroid.luong().them(new Runnable() {
                    public void run() {
                        if (cu instanceof Canvas) ((Canvas) cu).hideNotify();
                        c.showNotify();
                    }
                });
            }
            c.repaint();
        } else if (d instanceof TextBox) {
            NenAndroid.hienNhap((TextBox) d);
        }
    }

    public void setCurrent(Alert a, Displayable next) {
        setCurrent(next);
    }

    public void callSerially(Runnable r) {
        NenAndroid.luong().them(r);
    }

    public boolean vibrate(int ms) {
        return NenAndroid.rung(ms);
    }

    public boolean flashBacklight(int ms) {
        return false;
    }

    public boolean isColor() {
        return true;
    }

    public int numColors() {
        return 1 << 24;
    }

    public int numAlphaLevels() {
        return 256;
    }

    public int getColor(int which) {
        return which == COLOR_BACKGROUND ? 0xFFFFFF : 0;
    }

    public int getBestImageWidth(int t) {
        return 0;
    }

    public int getBestImageHeight(int t) {
        return 0;
    }
}
