package game.model;

import game.core.j2me.Graphics;
import game.render.Res;
import game.render.screen.GameScr;

public class Tree extends Actor {
    public int type;

    @Override
    public void setPosTo(short s, short s2) {
    }

    public Tree(int i, int i2, int i3) {
        this.x = (short) i;
        this.y = (short) i2;
        this.type = i3;
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
    public void paint(Graphics graphics) {
        TreeInfo tree = Res.getTree(this.type);
        int i = (this.x << 4) + tree.dx;
        int i2 = (this.y << 4) + tree.dy;
        if (i >= GameScr.cmx - tree.w && i2 >= GameScr.cmy - tree.h && i <= GameScr.cmx + GameScr.gsw && i2 <= GameScr.cmy + GameScr.gsh) {
            tree.paint(graphics, i, i2);
        }
    }

    @Override
    public int getX() {
        return super.getX();
    }

    @Override
    public int getY() {
        return (this.y << 4) + Res.getDyTree(this.type) + Res.getHeightTree(this.type);
    }
}
