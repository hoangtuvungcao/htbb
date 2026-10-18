package game.model.skill;

import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import javax.microedition.lcdui.Graphics;

public class Skill_Bua_4 extends Effect {
    LiveActor live;
    private boolean isSkillBoss;
    private boolean isExpl = false;
    int count = 0;

    public Skill_Bua_4(LiveActor live, int x, int y, boolean isBoss) {
        this.x = x;
        this.y = y;
        this.live = live;
        this.isSkillBoss = isBoss;
    }

    @Override
    public void update() {
        if (!this.isExpl) {
            int angle1 = Util.angle(this.live.x - this.x, -(this.live.y - this.y));
            int xa = (10 * Util.cos(angle1)) >> 10;
            int ya = (-(10 * Util.sin(angle1))) >> 10;
            this.x += xa;
            this.y += ya;
            EffectManager.addLowEffect(this.x, this.y, this.isSkillBoss ? 35 : 4);
            if (Util.distance(this.x, this.y, this.live.x, this.live.y) <= 10) {
                this.isExpl = true;
                return;
            }
            return;
        }
        if (Canvas.gameTick % 2 == 0) {
            for (int i = 0; i < 5; i++) {
                int a0 = (byte) (20 - Res.rnd(40));
                int b0 = (byte) (10 - Res.rnd(20));
                EffectManager.addLowEffect(this.live.x + a0, this.live.y + b0, 35);
            }
            this.count++;
            if (this.count > 3) {
                EffectManager.lowEffects.removeElement(this);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
    }
}
