package effect;

import game.core.j2me.Graphics;
import game.core.j2me.Image;

public class SmallImage {
    public short h;
    public short id;
    public short w;
    public short x;
    public short y;

    public void paint(Graphics graphics, Image image, int i, int i2) {
    }

    public SmallImage(int i, int i2, int i3, int i4, int i5) {
        this.id = (short) i;
        this.x = (short) i2;
        this.y = (short) i3;
        this.w = (short) i4;
        this.h = (short) i5;
    }
}
