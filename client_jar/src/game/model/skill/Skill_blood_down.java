package game.model.skill;

import game.model.Effect;
import game.model.EffectManager;
import game.render.Canvas;
import game.render.Res;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Skill_blood_down extends Effect {
    byte delay;
    byte countPop;
    byte f;
    int dame;
    int count = 120;
    byte frame = 0;
    byte g = 5;
    byte[] arrframe = {0, 1, 2, 3};

    public Skill_blood_down(int x, int y, int dame) {
        this.delay = (byte) 10;
        this.x = x;
        this.y = y;
        this.delay = (byte) Canvas.random(5, 20);
        Effect eff = new Effect(x, y, 34);
        eff.loop = (byte) 3;
        EffectManager.addLowEffect(eff);
        this.dame = dame;
    }

    @Override
    public void update() {
        this.f = (byte) (this.f + 1);
        if (this.f > this.arrframe.length - 1) {
            this.f = (byte) 0;
        }
        this.frame = this.arrframe[this.f];
        if (this.delay > 0) {
            this.delay = (byte) (this.delay - 1);
        }
        if (this.delay <= 0 && this.count > 0) {
            this.count -= this.g;
            this.g = (byte) (this.g + 8);
            if (this.count < 0) {
                this.count = 0;
                EffectManager.hiEffects.removeElement(this);
                EffectManager.lowEffects.addElement(this);
            }
        }
        if (this.count == 0 && Canvas.gameTick % 2 == 1) {
            EffectManager.addLowEffect(this.x, this.y, 62);
            EffectManager.lowEffects.removeElement(this);
            Canvas.gameScr.startFlyText(new StringBuffer("- ").append(this.dame).toString(), 0, this.x, this.y - 25, 1, -2);
        }
    }

    @Override
    public void paint(Graphics g) {
        Image img = Res.getImageEffect(61);
        if (img != null) {
            g.drawRegion(img, 0, this.frame * Effect.HEIGHT[61], Effect.WIDTH[61], Effect.HEIGHT[61], 0, this.x, this.y - this.count, 3);
        }
    }
}
