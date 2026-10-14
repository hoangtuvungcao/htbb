package game.model.skill;

import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.Res;
import game.render.Util;
import javax.microedition.lcdui.Graphics;

public class Skill_Tia_Thuy extends Effect {
    LiveActor to;
    private int angle;
    private int index;

    public Skill_Tia_Thuy(LiveActor to, int xFrom, int yFrom) {
        this.to = to;
        this.x = xFrom;
        this.y = yFrom;
    }

    @Override
    public void update() {
        this.angle = Util.angle(this.to.x - this.x, -(this.to.y - this.y));
        int xa = (10 * Util.cos(Util.fixangle(this.angle))) >> 10;
        int ya = (-(10 * Util.sin(Util.fixangle(this.angle)))) >> 10;
        this.x += xa;
        this.y += ya;
        if (Util.distance(this.x, this.y, this.to.x, this.to.y) <= 20) {
            EffectManager.addHiEffect(this.to.x, this.to.y - 10, 30);
            EffectManager.hiEffects.removeElement(this);
        }
    }

    @Override
    public void paint(Graphics g) {
        Res.paintImgEff(g, 19, 0, (this.index % 3) * 14, 25, 14, this.x, this.y, 3);
        this.index++;
    }
}
