package game.model.skill;

import game.model.Char;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class Skill_TruTranThanh extends Effect {
    Vector lisAttack;
    LiveActor from;
    int[] color = new int[0];
    int w;
    int xF;
    int yF;
    int vx;
    int dem;
    boolean isEnd;

    public Skill_TruTranThanh(int x, int y, LiveActor From, Vector list) {
        this.lisAttack = new Vector();
        this.h = 2;
        this.w = 2;
        this.x = x;
        this.y = y;
        for (int i = 0; i < 20; i++) {
            int a0 = (byte) (20 - Res.rnd(40));
            int b0 = (byte) (20 - Res.rnd(40));
            this.xF = x + a0;
            this.yF = y + b0;
        }
        this.from = From;
        this.lisAttack = list;
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
        if (Math.abs(this.x - this.xF) <= 5 && Math.abs(this.y - this.yF) <= 5) {
            this.isEnd = true;
            this.vx = 0;
            EffectManager.hiEffects.removeElement(this);
        }
    }

    @Override
    public void paint(Graphics g) {
        for (int i = 0; i < 5; i++) {
            if (!this.isEnd) {
                g.drawRegion(Char.imgLight, 0, 7, 7, 7, 0, this.xF, this.yF, 0);
                g.drawRegion(Char.imgLight, 0, 14, 7, 7, 0, (this.xF + 7) - Res.rnd(15), (this.yF + 7) - Res.rnd(15), 0);
            }
        }
    }
}
