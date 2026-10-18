package game.model;

public class NameAttributeItem {
    public byte colorPaint;
    public short id;
    public byte ispercent;
    public String name;

    public NameAttributeItem(short s, String str, byte b, byte b2) {
        this.id = s;
        this.name = str;
        this.colorPaint = b2;
        this.ispercent = b;
    }

    public byte getColorPaint(boolean z) {
        byte b = this.colorPaint;
        if (b == 1 && z) {
            return (byte) 2;
        }
        return b;
    }
}
