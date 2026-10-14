package game.model.skill;

import game.core.j2me.Graphics;
import game.model.Char;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.Res;
import java.util.Vector;

public class Skill_TruTranThanh extends Effect {
    int[] color = new int[0];
    int dem;
    LiveActor from;
    boolean isEnd;
    Vector lisAttack;
    int vx;
    int w;
    int xF;
    int yF;

    public Skill_TruTranThanh(int i, int i2, LiveActor liveActor, Vector vector) {
        this.lisAttack = new Vector();
        this.h = 2;
        this.w = 2;
        this.x = i;
        this.y = i2;
        for (int i3 = 0; i3 < 20; i3++) {
            byte bRnd = (byte) (20 - Res.rnd(40));
            byte bRnd2 = (byte) (20 - Res.rnd(40));
            this.xF = bRnd + i;
            this.yF = bRnd2 + i2;
        }
        this.from = liveActor;
        this.lisAttack = vector;
    }

    @Override
    public void update() {
        if (this.xF < this.x) {
            this.xF += this.vx;
        }
        if (this.xF >= this.x) {
            this.xF -= this.vx;
        }
        if (this.yF < this.y) {
            this.yF += this.vx;
        }
        if (this.yF > this.y) {
            this.yF -= this.vx;
        }
        this.vx++;
        if (Math.abs(this.x - this.xF) > 5 || Math.abs(this.y - this.yF) > 5) {
            return;
        }
        this.isEnd = true;
        this.vx = 0;
        EffectManager.hiEffects.removeElement(this);
    }

    @Override
    public void paint(Graphics graphics) {
        for (int i = 0; i < 5; i++) {
            if (!this.isEnd) {
                graphics.drawRegion(Char.imgLight, 0, 7, 7, 7, 0, this.xF, this.yF, 0);
                graphics.drawRegion(Char.imgLight, 0, 14, 7, 7, 0, (this.xF + 7) - Res.rnd(15), (this.yF + 7) - Res.rnd(15), 0);
            }
        }
    }
}
