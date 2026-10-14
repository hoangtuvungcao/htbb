package game.model;

public class NameAttributeItem {
    public byte ispercent;
    public byte colorPaint;
    public short id;
    public String name;

    public NameAttributeItem(short id, String name, byte ispercent, byte colorPaint) {
        this.ispercent = (byte) 0;
        this.colorPaint = (byte) 0;
        this.name = "";
        this.id = id;
        this.name = name;
        this.colorPaint = colorPaint;
        this.ispercent = ispercent;
    }

    public byte getColorPaint(boolean kick) {
        byte color = this.colorPaint;
        if (color == 1 && kick) {
            color = 2;
        }
        return color;
    }
}
