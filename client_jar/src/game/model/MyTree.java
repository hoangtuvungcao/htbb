package game.model;

import game.render.screen.GameData;
import javax.microedition.lcdui.Graphics;

public class MyTree extends Actor {
    public int himg;
    public byte idImg;
    public short idTree;

    public MyTree(int id, int x, int y, int idImg) {
        this.himg = 1;
        this.idImg = (byte) 0;
        this.idTree = (short) 0;
        this.idTree = (short) id;
        this.x = (short) x;
        this.y = (short) y;
        this.idImg = (byte) idImg;
        this.catagory = (byte) 11;
        this.himg = 1;
    }

    @Override
    public void paint(Graphics g) {
        ImageIcon img;
        if (this.idTree > -1 && (img = GameData.getImgIcon((short) (this.idImg + 3200))) != null && !img.isLoad) {
            if (this.himg == 1) {
                this.himg = img.img.getHeight();
            }
            g.drawImage(img.img, this.x, this.y, 33);
        }
    }

    @Override
    public void setPosTo(short x, short y) {
    }
}
