package game.render.screen;

import game.core.j2me.Graphics;

public class ObjLabel extends ObjGame {
    int align;
    String label = "";

    public ObjLabel(String str, int i, int i2, int i3) {
        this.align = 0;
        this.x = i;
        this.y = i2;
        this.align = i3;
    }

    @Override
    public void paint(Graphics graphics) {
        Font.normalFont[0].drawString(graphics, this.label, this.x, this.y, this.align);
    }
}
