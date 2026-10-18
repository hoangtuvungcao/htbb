package game.model.skill;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.GCanvas;
import game.render.Res;

public class Skill_Eff_end extends Effect {
    int angle;
    int delay;
    int vy;
    int x;
    int y;
    int yTo;

    public Skill_Eff_end(int i, int i2, int i3) {
        this.x = GCanvas.random(i - 10, i + 10);
        this.y = GCanvas.random(i2 - 10, i2 + 10);
        this.angle = i3;
        this.type = (short) 0;
    }

    public Skill_Eff_end(int i, int i2, LiveActor liveActor) {
        this.x = i;
        this.y = i2;
        this.yTo = liveActor.y;
        this.type = (short) 1;
    }

    @Override
    public void update() {
        if (this.type == 0) {
            int i = this.delay + 1;
            this.delay = i;
            if (i > 50) {
                EffectManager.hiEffects.removeElement(this);
                EffectManager.lowEffects.removeElement(this);
                this.delay = 0;
                return;
            }
            return;
        }
        int i2 = this.y;
        int i3 = this.vy;
        int i4 = i2 + i3;
        this.y = i4;
        this.vy = i3 + 2;
        if (i4 > this.yTo) {
            this.vy = 0;
            EffectManager.hiEffects.removeElement(this);
        }
    }

    @Override
    public void paint(Graphics graphics) {
        if (this.type == 0) {
            if (this.delay <= 0 || Res.getImgArrow(7) == null) {
                return;
            }
            graphics.drawRegion(Res.imgArrow[7], 0, 38, 20, 18, this.angle, this.x, this.y, 3);
            return;
        }
        Bitmap bitmapLoadImgEff = Res.loadImgEff(5);
        if (bitmapLoadImgEff != null) {
            graphics.drawRegion(bitmapLoadImgEff, 0, 0, 32, 32, 0, this.x, this.y, 3);
            graphics.drawRegion(bitmapLoadImgEff, 0, 32, 32, 32, 0, this.x, this.y, 3);
            graphics.drawRegion(bitmapLoadImgEff, 0, 0, 32, 32, 0, this.x + 2, this.y + 2, 3);
            graphics.drawRegion(bitmapLoadImgEff, 0, 32, 32, 32, 0, this.x + 2, this.y + 2, 3);
        }
    }
}
