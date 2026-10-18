package game.model.skill;

import game.core.j2me.Graphics;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.Res;
import game.render.Util;

public class Skill_Tia_Thuy extends Effect {
    private int angle;
    private int index;
    LiveActor to;

    public Skill_Tia_Thuy(LiveActor liveActor, int i, int i2) {
        this.to = liveActor;
        this.x = i;
        this.y = i2;
    }

    @Override
    public void update() {
        try {
            int iAngle = Util.angle(this.to.x - this.x, -(this.to.y - this.y));
            this.angle = iAngle;
            int iCos = (Util.cos(Util.fixangle(iAngle)) * 10) >> 10;
            int i = (-(Util.sin(Util.fixangle(this.angle)) * 10)) >> 10;
            this.x += iCos;
            this.y += i;
            if (Util.distance(this.x, this.y, this.to.x, this.to.y) <= 20) {
                EffectManager.addHiEffect(this.to.x, this.to.y - 10, 30);
                EffectManager.hiEffects.removeElement(this);
            }
        } catch (Exception unused) {
        }
    }

    @Override
    public void paint(Graphics graphics) {
        try {
            Res.paintImgEff(graphics, 19, 0, (this.index % 3) * 14, 25, 14, this.x, this.y, 3);
            this.index++;
        } catch (Exception unused) {
        }
    }
}
