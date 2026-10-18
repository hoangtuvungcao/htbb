package game.model;

import game.core.j2me.Graphics;
import game.render.screen.GameData;

public class MyTree extends Actor {
    public int himg;
    public byte idImg;
    public short idTree;

    @Override
    public void setPosTo(short s, short s2) {
    }

    public MyTree(int i, int i2, int i3, int i4) {
        this.himg = 1;
        this.idImg = (byte) 0;
        this.idTree = (short) i;
        this.x = (short) i2;
        this.y = (short) i3;
        this.idImg = (byte) i4;
        this.catagory = (byte) 11;
        this.himg = 1;
    }

    @Override
    public void paint(Graphics graphics) {
        ImageIcon imgIcon;
        if (this.idTree <= -1 || (imgIcon = GameData.getImgIcon((short) (this.idImg + 3200))) == null || imgIcon.isLoad) {
            return;
        }
        if (this.himg == 1) {
            this.himg = imgIcon.img.getHeight();
        }
        graphics.drawImage(imgIcon.img, this.x, this.y, 33);
    }
}
