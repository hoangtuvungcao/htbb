package game.model;

import game.render.Res;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;

public class Tree extends Actor {
    public int type;

    public Tree(int x, int y, int type) {
        this.x = (short) x;
        this.y = (short) y;
        this.type = type;
        this.catagory = (byte) 13;
    }

    public void removeImage() {
        Res.removeTreeImage(this.type);
    }

    public int getH() {
        return Res.getHeightTree(this.type);
    }

    public int getW() {
        return Res.getwidthTree(this.type);
    }

    @Override
    public void paint(Graphics g) {
        TreeInfo tree = Res.getTree(this.type);
        int xx = (this.x << 4) + tree.dx;
        int yy = (this.y << 4) + tree.dy;
        if (xx < GameScr.cmx - tree.w || yy < GameScr.cmy - tree.h || xx > GameScr.cmx + GameScr.gsw || yy > GameScr.cmy + GameScr.gsh) {
            return;
        }
        tree.paint(g, xx, yy);
    }

    @Override
    public int getX() {
        return super.getX();
    }

    @Override
    public int getY() {
        int yy = (this.y << 4) + Res.getDyTree(this.type) + Res.getHeightTree(this.type);
        return yy;
    }

    @Override
    public void setPosTo(short x, short y) {
    }
}
