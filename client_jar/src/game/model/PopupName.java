package game.model;

import game.render.Res;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;

public class PopupName extends Tree {
    public String name;
    public byte num;
    public byte iPrivate;
    public static FrameImage imgArrow;

    public PopupName(String name, int x, int y) {
        super(x, y, 0);
        this.iPrivate = (byte) 0;
        this.x = (short) x;
        this.y = (short) y;
        this.name = name;
        this.num = (byte) Res.rnd(8);
    }

    @Override
    public void update() {
    }

    @Override
    public void paint(Graphics g) {
        this.num = (byte) (this.num + 1);
        if (this.num >= 8) {
            this.num = (byte) 0;
        }
        g.drawImage(Res.imgShadow, this.x, this.y, 3);
        if (imgArrow != null) {
            imgArrow.drawFrame(0, this.x, (this.y - 10) + (this.num / 2), 0, 33, g);
        }
        Font.normalFontColor.drawString(g, this.name, this.x, (this.y - 32) + (this.num / 2), 2);
    }
}
