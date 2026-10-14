package game.model.arrow;

import game.model.EffectManager;
import game.model.LiveActor;
import game.render.Canvas;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;

public class WeaponsMango extends IArrow {
    public int x;
    public int y;
    public int angleTo;
    public int angle;
    public int count;
    public int vangle;
    public int limCount;
    boolean isLR;

    public WeaponsMango(LiveActor acFr, LiveActor acTo, int vangle) {
        this.x = acFr.x;
        this.y = acFr.y;
        this.vangle = vangle;
        int rd = Canvas.abs(Canvas.r.nextInt(2));
        this.limCount = Canvas.abs(Canvas.r.nextInt(3)) + 1;
        this.isLR = rd == 0;
        int i = this.isLR ? 0 : 360;
        this.angleTo = i;
        this.angle = i;
        if (acTo != null) {
            int hpL = acTo.damAttack;
            acTo.jump();
            EffectManager.addHiEffect(acTo.x, (acTo.y + acTo.dy) - 10, 11);
            Canvas.gameScr.startFlyText(new StringBuffer("-").append(hpL).toString(), 0, acTo.x + 0, (acTo.y + acTo.dy) - 15, 1, -2);
        }
    }

    @Override
    public void onArrowTouchTarget() {
    }

    @Override
    public void paint(Graphics g) {
    }

    @Override
    public void set(int type, int x, int y, int power, byte effect2, LiveActor owner, LiveActor target) {
    }

    @Override
    public void setAngle(int angle) {
    }

    @Override
    public void update() {
        if (this.isLR) {
            this.angleTo += 30;
            if (this.angleTo > 360) {
                this.angleTo -= 360;
            }
            if (this.angle != this.angleTo) {
                this.angle += this.vangle;
                if (this.angle > 360) {
                    this.angle -= 360;
                    this.count++;
                }
            }
        } else {
            this.angleTo -= 30;
            if (this.angleTo < 0) {
                this.angleTo += 360;
            }
            if (this.angle != this.angleTo) {
                this.angle -= this.vangle;
                if (this.angle < 0) {
                    this.angle += 360;
                    this.count++;
                }
            }
        }
        if (this.count >= this.limCount) {
            if (GameScr.arrowsUp.contains(this)) {
                GameScr.arrowsUp.removeElement(this);
            }
            this.count = 0;
        }
        if (this.angle % 30 == 0) {
            Canvas.gameScr.startWeapsAngleBoss(this.angle, 10, null, null, 1, false, 0, 0, this.x, this.y, true);
        }
    }
}
