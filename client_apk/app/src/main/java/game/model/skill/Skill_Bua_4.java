package game.model.skill;

import game.core.j2me.Graphics;
import game.model.Cout;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;

public class Skill_Bua_4 extends Effect {
    private boolean isSkillBoss;
    LiveActor live;
    private boolean isExpl = false;
    int count = 0;

    @Override
    public void paint(Graphics graphics) {
    }

    public Skill_Bua_4(LiveActor liveActor, int i, int i2, boolean z) {
        this.x = i;
        this.y = i2;
        this.live = liveActor;
        this.isSkillBoss = z;
    }

    @Override
    public void update() {
        try {
            int i = 35;
            if (!this.isExpl) {
                int iAngle = Util.angle(this.live.x - this.x, -(this.live.y - this.y));
                int iCos = (Util.cos(iAngle) * 10) >> 10;
                int i2 = (-(Util.sin(iAngle) * 10)) >> 10;
                this.x += iCos;
                this.y += i2;
                int i3 = this.x;
                int i4 = this.y;
                if (!this.isSkillBoss) {
                    i = 4;
                }
                EffectManager.addLowEffect(i3, i4, i);
                if (Util.distance(this.x, this.y, this.live.x, this.live.y) <= 10) {
                    this.isExpl = true;
                }
            } else if (GCanvas.gameTick % 2 == 0) {
                for (int i5 = 0; i5 < 5; i5++) {
                    try {
                        EffectManager.addLowEffect(this.live.x + ((byte) (20 - Res.rnd(40))), this.live.y + ((byte) (10 - Res.rnd(20))), 35);
                    } catch (Exception unused) {
                        return;
                    }
                }
                int i6 = this.count + 1;
                this.count = i6;
                if (i6 > 3) {
                    EffectManager.lowEffects.removeElement(this);
                }
            }
        } catch (Exception unused2) {
            Cout.println("update skill bua 4");
        }
    }
}
