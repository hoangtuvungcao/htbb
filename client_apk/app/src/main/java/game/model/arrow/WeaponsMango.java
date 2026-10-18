package game.model.arrow;

import game.core.j2me.Graphics;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.GCanvas;
import game.render.screen.GameScr;

public class WeaponsMango extends IArrow {
    public int angle;
    public int angleTo;
    public int count;
    boolean isLR;
    public int limCount;
    public int vangle;
    public int x;
    public int y;

    @Override
    public void onArrowTouchTarget() {
    }

    @Override
    public void paint(Graphics graphics) {
    }

    @Override
    public void set(int i, int i2, int i3, int i4, byte b, LiveActor liveActor, LiveActor liveActor2) {
    }

    @Override
    public void setAngle(int i) {
    }

    public WeaponsMango(LiveActor liveActor, LiveActor liveActor2, int i) {
        this.x = liveActor.x;
        this.y = liveActor.y;
        this.vangle = i;
        int iAbs = GCanvas.abs(GCanvas.r.nextInt(2));
        this.limCount = GCanvas.abs(GCanvas.r.nextInt(3)) + 1;
        boolean z = iAbs == 0;
        this.isLR = z;
        int i2 = !z ? 360 : 0;
        this.angleTo = i2;
        this.angle = i2;
        if (liveActor2 != null) {
            int i3 = liveActor2.damAttack;
            liveActor2.jump();
            EffectManager.addHiEffect(liveActor2.x, (liveActor2.y + liveActor2.dy) - 10, 11);
            GCanvas.gameScr.startFlyText("-" + i3, 0, liveActor2.x, (liveActor2.y + liveActor2.dy) - 15, 1, -2);
        }
    }

    @Override
    public void update() {
        if (this.isLR) {
            int i = this.angleTo;
            int i2 = i + 30;
            this.angleTo = i2;
            if (i2 > 360) {
                this.angleTo = i - 330;
            }
            int i3 = this.angle;
            if (i3 != this.angleTo) {
                int i4 = i3 + this.vangle;
                this.angle = i4;
                if (i4 > 360) {
                    this.angle = i4 - 360;
                    this.count++;
                }
            }
        } else {
            int i5 = this.angleTo;
            int i6 = i5 - 30;
            this.angleTo = i6;
            if (i6 < 0) {
                this.angleTo = i5 + 330;
            }
            int i7 = this.angle;
            if (i7 != this.angleTo) {
                int i8 = i7 - this.vangle;
                this.angle = i8;
                if (i8 < 0) {
                    this.angle = i8 + 360;
                    this.count++;
                }
            }
        }
        if (this.count >= this.limCount) {
            GameScr gameScr = GCanvas.gameScr;
            if (GameScr.arrowsUp.contains(this)) {
                GameScr gameScr2 = GCanvas.gameScr;
                GameScr.arrowsUp.removeElement(this);
            }
            this.count = 0;
        }
        if (this.angle % 30 == 0) {
            GCanvas.gameScr.startWeapsAngleBoss(this.angle, 10, null, null, 1, false, 0, 0, this.x, this.y, true);
        }
    }
}
