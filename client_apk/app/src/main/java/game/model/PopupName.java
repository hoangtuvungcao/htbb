package game.model;

import game.core.j2me.Graphics;
import game.render.Res;
import game.render.screen.Font;

public class PopupName extends Tree {
    public static FrameImage imgArrow;
    public byte iPrivate;
    public String name;
    public byte num;

    @Override
    public void update() {
    }

    public PopupName(String str, int i, int i2) {
        super(i, i2, 0);
        this.iPrivate = (byte) 0;
        this.x = (short) i;
        this.y = (short) i2;
        this.name = str;
        this.num = (byte) Res.rnd(8);
    }

    @Override
    public void paint(Graphics graphics) {
        byte b = (byte) (this.num + 1);
        this.num = b;
        if (b >= 8) {
            this.num = (byte) 0;
        }
        graphics.drawImage(Res.imgShadow, this.x, this.y, 3);
        FrameImage frameImage = imgArrow;
        if (frameImage != null) {
            frameImage.drawFrame(0, this.x, (this.y - 10) + (this.num / 2), 0, Graphics.BOTTOM | Graphics.HCENTER, graphics);
        }
        Font.arialFont11.drawString(graphics, this.name, this.x, (this.y - 32) + (this.num / 2), 2);
    }
}
