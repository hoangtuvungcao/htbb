package game.model.skill;

import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.Canvas;
import game.render.Res;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Skill_Eff_end extends Effect {
    int x;
    int y;
    int angle;
    int delay;
    int yTo;
    int vy;

    public Skill_Eff_end(int x, int y, int transForm) {
        this.x = Canvas.random(x - 10, x + 10);
        this.y = Canvas.random(y - 10, y + 10);
        this.angle = transForm;
        this.type = (short) 0;
    }

    public Skill_Eff_end(int xF, int yF, LiveActor to) {
        this.x = xF;
        this.y = yF;
        this.yTo = to.y;
        this.type = (short) 1;
    }

    @Override
    public void update() {
        if (this.type == 0) {
            this.delay++;
            if (this.delay > 50) {
                EffectManager.hiEffects.removeElement(this);
                EffectManager.lowEffects.removeElement(this);
                this.delay = 0;
                return;
            }
            return;
        }
        this.y += this.vy;
        this.vy += 2;
        if (this.y > this.yTo) {
            this.vy = 0;
            EffectManager.hiEffects.removeElement(this);
        }
    }

    @Override
    public void paint(Graphics g) {
        Image img;
        if (this.type == 0) {
            if (this.delay > 0 && (img = Res.getImgArrow(7)) != null) {
                g.drawRegion(img, 0, 38, 20, 18, this.angle, this.x, this.y, 3);
                return;
            }
            return;
        }
        Image effImg = Res.loadImgEff(5);
        if (effImg != null) {
            g.drawRegion(effImg, 0, 0, 32, 32, 0, this.x, this.y, 3);
            g.drawRegion(effImg, 0, 32, 32, 32, 0, this.x, this.y, 3);
            g.drawRegion(effImg, 0, 0, 32, 32, 0, this.x + 2, this.y + 2, 3);
            g.drawRegion(effImg, 0, 32, 32, 32, 0, this.x + 2, this.y + 2, 3);
        }
    }
}
