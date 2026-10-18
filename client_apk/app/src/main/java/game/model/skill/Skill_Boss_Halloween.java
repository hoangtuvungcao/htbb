package game.model.skill;

import game.core.j2me.Graphics;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.arrow.Arrow;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;

public class Skill_Boss_Halloween extends Effect {
    public int angle;
    public int dx;
    int dx1;
    public int dy;
    int dy1;
    public int frame = 3;
    byte[] framearray = {0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3};
    public short himg;
    public int idimg;
    boolean isMatchX;
    boolean isMatchY;
    byte nframe;
    public int pos;
    LiveActor target;
    public short va;
    public short vx;
    public short vy;
    public int xTo;
    public int yTo;

    public Skill_Boss_Halloween(int i, int i2, LiveActor liveActor) {
        this.x = i;
        this.y = i2;
        this.target = liveActor;
        this.xTo = liveActor.x;
        short s = liveActor.y;
        this.idimg = 17;
        this.va = (short) 12;
        this.yTo = s + 5;
        this.himg = (short) 1;
    }

    @Override
    public void paint(Graphics graphics) {
        if (Res.getImgArrow(this.idimg) != null) {
            graphics.drawRegion(Res.getImgArrow(this.idimg), 0, this.frame * Arrow.ARROWSIZE[1][this.idimg], Arrow.ARROWSIZE[0][this.idimg], Arrow.ARROWSIZE[1][this.idimg], 0, this.x, this.y, 3);
        }
    }

    @Override
    public void update() {
        byte b = (byte) (this.nframe + 1);
        this.nframe = b;
        byte[] bArr = this.framearray;
        if (b > bArr.length - 1) {
            this.nframe = (byte) 0;
        }
        this.frame = bArr[this.nframe];
        LiveActor liveActor = this.target;
        if (liveActor != null) {
            this.xTo = liveActor.x;
            this.yTo = this.target.y;
        }
        this.dx = (short) (this.xTo - this.x);
        short s = (short) ((this.yTo - (this.himg >> 1)) - this.y);
        this.dy = s;
        int iAngle = Util.angle(this.dx, s);
        this.angle = iAngle;
        this.vx = (short) ((this.va * Util.cos(iAngle)) >> 10);
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
            this.target.jump();
            GCanvas.gameScr.startFlyText("-" + this.target.damAttack, 0, this.target.x, (this.target.y + this.target.dy) - 15, 1, -2);
        }
        if (this.x == this.xTo || this.y == this.yTo || GCanvas.gameTick % 2 != 0) {
            return;
        }
        EffectManager.addLowEffect(this.x, this.y - 5, 15);
    }
}
