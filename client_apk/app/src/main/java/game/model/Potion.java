package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;

public class Potion {
    public long delay;
    public short id;
    public short index;
    public boolean isTrade;
    public String name;
    public String name2;
    public int numTrade;
    public int number;

    public void paint(Graphics graphics, Bitmap bitmap, int i, int i2, int i3) {
        graphics.drawImage(bitmap, i, i2, i3);
    }
}
