package game.model.skill;

import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;

public class Skill_Cau_Lua extends Effect {
    int[] angle = {0, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330};
    Vector list;
    Vector total;

    public Skill_Cau_Lua(int i, int i2, Vector vector) {
        this.total = new Vector();
        this.total = new Vector();
        new Vector();
        this.list = vector;
        for (int i3 = 0; i3 < this.angle.length; i3++) {
            this.total.addElement(new Pos(i, i2, this.angle[i3]));
        }
    }

    @Override
    public void update() {
        for (int i = 0; i < this.total.size(); i++) {
            Pos pos = (Pos) this.total.elementAt(i);
            if (pos != null) {
                pos.update();
                if (pos.isRemove) {
                    this.total.removeElement(pos);
                }
            }
        }
        if (this.total.size() == 0) {
            this.wantDetroy = true;
            for (int i2 = 0; i2 < this.list.size(); i2++) {
                LiveActor liveActor = (LiveActor) this.list.elementAt(i2);
                if (liveActor != null) {
                    int i3 = liveActor.damAttack;
                    liveActor.doublejump();
                    if (i3 > 0) {
                        GCanvas.gameScr.startFlyText("-" + i3, 0, liveActor.x, (liveActor.y + liveActor.dy) - 15, 1, -2);
                    } else {
                        GCanvas.gameScr.startFlyText("MISS", 0, liveActor.x, (liveActor.y + liveActor.dy) - 15, 1, -2);
                    }
                }
            }
        }
    }

    class Pos {
        boolean isRemove;
        int x;
        int xTo;
        int y;
        int yTo;

        public Pos(int i, int i2, int i3) {
            this.x = i;
            this.y = i2;
            this.xTo = i + ((Util.cos(i3) * 100) / 1024);
            this.yTo = i2 + ((Util.sin(i3) * 100) / 1024);
        }

        public void update() {
            int i = this.x;
            int i2 = this.xTo;
            if (i != i2) {
                if (((i2 - i) >> 1) == 0) {
                    this.x = i2;
                } else {
                    this.x = i + ((i2 - i) >> 1);
                }
            }
            int i3 = this.y;
            int i4 = this.yTo;
            if (i3 != i4) {
                if (((i4 - i3) >> 1) == 0) {
                    this.y = i4;
                } else {
                    this.y = i3 + ((i4 - i3) >> 1);
                }
            }
            int i5 = this.x;
            if (i5 == i2 && this.y == i4) {
                this.isRemove = true;
            }
            if (!this.isRemove) {
                EffectManager.addHiEffect(i5, this.y, 15);
                EffectManager.addHiEffect(this.x, this.y, 49);
            } else {
                for (int i6 = 0; i6 < 5; i6++) {
                    EffectManager.addHiEffect((this.x + Res.rnd(10)) - 5, (this.y + Res.rnd(10)) - 10, 28);
                }
            }
        }
    }
}
