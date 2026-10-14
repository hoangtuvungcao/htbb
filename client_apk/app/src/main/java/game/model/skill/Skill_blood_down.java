package game.model.skill;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.Effect;
import game.model.EffectManager;
import game.render.GCanvas;
import game.render.Res;

public class Skill_blood_down extends Effect {
    byte countPop;
    int dame;
    byte delay;
    byte f;
    int count = 120;
    byte frame = 0;
    byte g = 5;
    byte[] arrframe = {0, 1, 2, 3};

    public Skill_blood_down(int i, int i2, int i3) {
        this.delay = (byte) 10;
        this.x = i;
        this.y = i2;
        this.delay = (byte) GCanvas.random(5, 20);
        Effect effect2 = new Effect(i, i2, 34);
        effect2.loop = (byte) 3;
        EffectManager.addLowEffect(effect2);
        this.dame = i3;
    }

    @Override
    public void update() {
        int i;
        byte b = (byte) (this.f + 1);
        this.f = b;
        byte[] bArr = this.arrframe;
        if (b > bArr.length - 1) {
            this.f = (byte) 0;
        }
        this.frame = bArr[this.f];
        byte b2 = this.delay;
        if (b2 > 0) {
            this.delay = (byte) (b2 - 1);
        }
        if (this.delay <= 0 && (i = this.count) > 0) {
            byte b3 = this.g;
            int i2 = i - b3;
            this.count = i2;
            this.g = (byte) (b3 + 8);
            if (i2 < 0) {
                this.count = 0;
                EffectManager.hiEffects.removeElement(this);
                EffectManager.lowEffects.addElement(this);
            }
        }
        if (this.count == 0 && GCanvas.gameTick % 2 == 1) {
            EffectManager.addLowEffect(this.x, this.y, 62);
            EffectManager.lowEffects.removeElement(this);
            GCanvas.gameScr.startFlyText("- " + this.dame, 0, this.x, this.y - 25, 1, -2);
        }
    }

    @Override
    public void paint(Graphics graphics) {
        Bitmap imgeffect = Res.getImgeffect(61);
        if (imgeffect != null) {
            graphics.drawRegion(imgeffect, 0, this.frame * Effect.HEIGHT[61], Effect.WIDTH[61], Effect.HEIGHT[61], 0, this.x, this.y - this.count, Graphics.VCENTER | Graphics.HCENTER);
        }
    }
}
