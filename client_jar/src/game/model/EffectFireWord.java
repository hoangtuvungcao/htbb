package game.model;

import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class EffectFireWord extends Effect {
    public static final byte END_PHAO_HOA = 0;
    public FrameImage fraImgEff;
    public int type;
    public int f;
    int fRemove;
    Vector VecEffEnd = new Vector();

    public EffectFireWord(int type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        switch (type) {
            case 0:
                this.fraImgEff = new FrameImage(64, 4);
                int tphao = Res.random(7, 12);
                this.fRemove = 12;
                for (int i = 0; i < tphao; i++) {
                    Point p = new Point();
                    p.x = x + Res.random_Am_0(4);
                    p.y = y + Res.random_Am_0(5);
                    p.frame = Res.random(this.fraImgEff.nFrame);
                    p.vy = -Res.random(5, 9);
                    p.vx = Res.random(0, 3);
                    if (i % 2 == 0) {
                        p.vx = -p.vx;
                    }
                    this.VecEffEnd.addElement(p);
                }
                break;
        }
    }

    @Override
    public void paint(Graphics g) {
        switch (this.type) {
            case 0:
                for (int i = 0; i < this.VecEffEnd.size(); i++) {
                    Point p = (Point) this.VecEffEnd.elementAt(i);
                    if (this.f < (this.fRemove / 3) * 2) {
                        if (this.fraImgEff != null) {
                            this.fraImgEff.drawFrame(p.frame, p.x, p.y, 0, 3, g);
                        }
                    } else if (this.fraImgEff != null) {
                        this.fraImgEff.drawFrame(p.frame == 0 ? 3 : p.frame, p.x, p.y, 0, 3, g);
                    }
                }
                break;
        }
    }

    @Override
    public void update() {
        this.f++;
        switch (this.type) {
            case 0:
                for (int i = 0; i < this.VecEffEnd.size(); i++) {
                    Point p = (Point) this.VecEffEnd.elementAt(i);
                    p.update();
                    p.vy++;
                }
                if (this.f >= this.fRemove) {
                    this.wantDetroy = true;
                }
                break;
        }
    }
}
