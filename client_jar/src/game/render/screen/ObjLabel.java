package game.render.screen;

import javax.microedition.lcdui.Graphics;

public class ObjLabel extends ObjGame {
    String label = "";
    int align;

    public ObjLabel(String label, int x, int y, int align) {
        this.align = 0;
        this.x = x;
        this.y = y;
        this.align = align;
    }

    @Override
    public void paint(Graphics g) {
        Font.normalFont[0].drawString(g, this.label, this.x, this.y, this.align);
    }
}
