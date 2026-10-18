package game.model;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Potion {
    public int number;
    public int numTrade;
    public long delay;
    public short id;
    public short index;
    public boolean isTrade;
    public String name;
    public String name2;

    public void paint(Graphics g, Image img, int x, int y, int align) {
        g.drawImage(img, x, y, align);
    }
}
