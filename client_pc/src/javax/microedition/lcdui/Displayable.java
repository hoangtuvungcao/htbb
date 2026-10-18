package javax.microedition.lcdui;

import java.util.Vector;

public abstract class Displayable {
    final Vector lenh = new Vector();
    CommandListener ngheLenh;
    private String title;

    Displayable() {
    }

    public void addCommand(Command c) {
        if (c != null && !lenh.contains(c)) lenh.addElement(c);
    }

    public void removeCommand(Command c) {
        lenh.removeElement(c);
    }

    public void setCommandListener(CommandListener l) {
        ngheLenh = l;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String s) {
        title = s;
    }

    public Ticker getTicker() {
        return null;
    }

    public void setTicker(Ticker t) {
    }

    public boolean isShown() {
        return Display.hienTai == this;
    }

    public int getWidth() {
        return NenPc.rong;
    }

    public int getHeight() {
        return NenPc.cao;
    }

    protected void sizeChanged(int w, int h) {
    }

    /** Gọi lệnh (trên luồng sự kiện). */
    void goiLenh(Command c) {
        CommandListener l = ngheLenh;
        if (l != null) l.commandAction(c, this);
    }
}
