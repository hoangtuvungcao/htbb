package game.model.skill;

import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.arrow.Arrow;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import javax.microedition.lcdui.Graphics;

public class Skill_Boss_Halloween extends Effect {
    public int dx;
    public int dy;
    public int angle;
    public int pos;
    public int frame;
    public int xTo;
    public int yTo;
    public int idimg;
    public short va;
    public short himg;
    public short vx;
    public short vy;
    LiveActor target;
    boolean isMatchX;
    boolean isMatchY;
    int dx1;
    int dy1;
    byte[] framearray = {0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3};
    byte nframe;

    public Skill_Boss_Halloween(int x, int y, LiveActor target) {
        this.x = x;
        this.y = y;
        this.target = target;
        this.xTo = target.x;
        this.yTo = target.y;
        this.idimg = 17;
        this.va = (short) 12;
        this.yTo += 5;
        this.himg = (short) 1;
    }

    @Override
    public void paint(Graphics g) {
        if (Res.getImgArrow(this.idimg) != null) {
            g.drawRegion(Res.getImgArrow(this.idimg), 0, this.frame * Arrow.ARROWSIZE[1][this.idimg], Arrow.ARROWSIZE[0][this.idimg], Arrow.ARROWSIZE[1][this.idimg], 0, this.x, this.y, 3);
        }
    }

    @Override
    public void update() {
        this.nframe = (byte) (this.nframe + 1);
        if (this.nframe > this.framearray.length - 1) {
            this.nframe = (byte) 0;
        }
        this.frame = this.framearray[this.nframe];
        if (this.target != null) {
            this.xTo = this.target.x;
            this.yTo = this.target.y;
        }
        this.dx = (short) (this.xTo - this.x);
        this.dy = (short) ((this.yTo - (this.himg >> 1)) - this.y);
        this.angle = Util.angle(this.dx, this.dy);
        this.vx = (short) ((this.va * Util.cos(this.angle)) >> 10);
        this.vy = (short) ((this.va * Util.sin(this.angle)) >> 10);
        this.x += this.vx;
        this.y += this.vy;
        this.dx1 = Util.abs(this.x - this.xTo);
        this.dy1 = Util.abs(this.y - this.yTo);
        if (this.dx1 <= this.vx) {
            this.x = (short) this.xTo;
            this.isMatchX = true;
        }
        if (this.dy1 < this.vy) {
            this.y = (short) this.yTo;
            this.isMatchY = true;
        }
        if ((this.isMatchX && this.isMatchY) || Res.getDistance(this.x, this.y, this.xTo, this.yTo) <= 10) {
            this.x = this.xTo;
            this.y = this.yTo;
            EffectManager.addHiEffect(this.x, this.y, 48);
            this.wantDetroy = true;
            Canvas.gameScr.startFlyText(new StringBuffer("-").append(this.target.damAttack).toString(), 0, this.target.x + 0, (this.target.y + this.target.dy) - 15, 1, -2);
            this.target.jump();
        }
        if (this.x != this.xTo && this.y != this.yTo && Canvas.gameTick % 2 == 0) {
            EffectManager.addLowEffect(this.x, this.y - 5, 15);
        }
    }
}
