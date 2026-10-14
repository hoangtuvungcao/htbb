package game.model.skill;

import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;

public class Skill_Cau_Lua extends Effect {
    Vector total;
    Vector list;
    int[] angle = {0, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330};

    public Skill_Cau_Lua(int xC, int yC, Vector list) {
        this.total = new Vector();
        this.total = new Vector();
        this.list = new Vector();
        this.list = list;
        for (int i = 0; i < this.angle.length; i++) {
            this.total.addElement(new Pos(xC, yC, this.angle[i]));
        }
    }

    @Override
    public void update() {
        for (int i = 0; i < this.total.size(); i++) {
            Pos p = (Pos) this.total.elementAt(i);
            if (p != null) {
                p.update();
                if (p.isRemove) {
                    this.total.removeElement(p);
                }
            }
        }
        if (this.total.size() == 0) {
            this.wantDetroy = true;
            for (int i2 = 0; i2 < this.list.size(); i2++) {
                LiveActor l_To = (LiveActor) this.list.elementAt(i2);
                if (l_To != null) {
                    int hpL = l_To.damAttack;
                    l_To.doublejump();
                    if (hpL > 0) {
                        Canvas.gameScr.startFlyText(new StringBuffer("-").append(hpL).toString(), 0, l_To.x + 0, (l_To.y + l_To.dy) - 15, 1, -2);
                    } else {
                        Canvas.gameScr.startFlyText("MISS", 0, l_To.x + 0, (l_To.y + l_To.dy) - 15, 1, -2);
                    }
                }
            }
        }
    }

    class Pos {
        int x;
        int y;
        int xTo;
        int yTo;
        boolean isRemove;

        public Pos(int x, int y, int angle) {
            this.x = x;
            this.y = y;
            this.xTo = x + ((Util.cos(angle) * 100) / 1024);
            this.yTo = y + ((Util.sin(angle) * 100) / 1024);
        }

        public void update() {
            if (this.x != this.xTo) {
                if (((this.xTo - this.x) >> 1) == 0) {
                    this.x = this.xTo;
                } else {
                    this.x += (this.xTo - this.x) >> 1;
                }
            }
            if (this.y != this.yTo) {
                if (((this.yTo - this.y) >> 1) == 0) {
                    this.y = this.yTo;
                } else {
                    this.y += (this.yTo - this.y) >> 1;
                }
            }
            if (this.x == this.xTo && this.y == this.yTo) {
                this.isRemove = true;
            }
            if (this.isRemove) {
                for (int i = 0; i < 5; i++) {
                    EffectManager.addHiEffect((this.x + Res.rnd(10)) - 5, (this.y + Res.rnd(10)) - 10, 28);
                }
                return;
            }
            EffectManager.addHiEffect(this.x, this.y, 15);
            EffectManager.addHiEffect(this.x, this.y, 49);
        }
    }
}
