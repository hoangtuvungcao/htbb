package game.model;

import game.model.arrow.Arrow;
import game.model.arrow.IArrow;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class ArrowTarget extends IArrow {
    private byte imgIndex;
    private byte d;
    private byte v;
    private byte timeDelay = 0;
    public short x;
    public short y;
    public short xShadow;
    public short yShadow;
    public short xTo;
    public short yTo;
    public short angle;
    public short angleShadow;
    public boolean isTarget;
    int power;

    public ArrowTarget(byte type) {
        this.imgIndex = type;
    }

    @Override
    public void update() {
        int xa = (this.v * Util.cos(this.angle)) >> 10;
        int ya = (-(this.v * Util.sin(this.angle))) >> 10;
        this.x = (short) (this.x + xa);
        this.y = (short) (this.y + ya);
        int xb = (this.v * Util.cos(this.angleShadow)) >> 10;
        int yb = (-(this.v * Util.sin(this.angleShadow))) >> 10;
        this.xShadow = (short) (this.xShadow + xb);
        this.yShadow = (short) (this.yShadow + yb);
        if (this.timeDelay == 0 && Util.distance(this.xTo, this.yTo, this.x, this.y) <= this.v * 2) {
            short s = this.xTo;
            this.xShadow = s;
            this.x = s;
            short s2 = this.yTo;
            this.yShadow = s2;
            this.y = s2;
            this.v = (byte) 0;
            if (!this.isTarget) {
                this.timeDelay = (byte) (Res.rnd(30) + 20);
                Canvas.gameScr.arrowsDown.addElement(this);
                Canvas.gameScr.startExplosionAt(this.x, (short) (this.y + 10));
            } else {
                if (this.power != 0) {
                    Canvas.gameScr.startFlyText(new StringBuffer("-").append(this.power).toString(), 0, this.x, this.y - 15, 1, -2);
                }
                EffectManager.addHiEffect(this.x, this.y - 20, 12);
            }
            GameScr.arrowsUp.removeElement(this);
        } else if (Util.distance(this.xTo, this.yTo, this.x, this.y) > 1000) {
            GameScr.arrowsUp.removeElement(this);
        }
        if (this.timeDelay > 0) {
            this.timeDelay = (byte) (this.timeDelay - 1);
            if (this.timeDelay == 0) {
                Canvas.gameScr.arrowsDown.removeElement(this);
            }
        }
    }

    @Override
    public void onArrowTouchTarget() {
    }

    @Override
    public void paint(Graphics g) {
        if (Res.getImgArrow(this.imgIndex) != null) {
            g.drawImage(Res.imgShadow, this.xShadow, this.yShadow, 17);
            Image img = Res.getImgArrow(this.imgIndex);
            if (img != null) {
                g.drawRegion(img, 0, Arrow.FRAME[this.d] * Arrow.ARROWSIZE[1][this.imgIndex], Arrow.ARROWSIZE[0][this.imgIndex], Arrow.ARROWSIZE[1][this.imgIndex], Arrow.TRANSFORM[this.d], this.x, this.y, 3);
            }
        }
    }

    public void set(int type, int x, int y, int power, byte effect2, int xTo, int yTo, byte v, boolean isTarget) {
        this.power = power;
        this.isTarget = isTarget;
        this.v = v;
        int dx = xTo - x;
        int dy = yTo - y;
        short s = (short) x;
        this.xShadow = s;
        this.x = s;
        this.y = (short) y;
        this.xTo = (short) xTo;
        this.yTo = (short) yTo;
        this.yShadow = (short) (y + 30);
        this.d = (byte) Arrow.findDirIndexFromAngle(Util.angle(dx, -dy));
        this.angle = (short) Util.angle(xTo - x, -(yTo - y));
        this.angleShadow = (short) Util.angle(xTo - this.xShadow, -(yTo - this.yShadow));
    }

    @Override
    public void set(int type, int x, int y, int power, byte effect2, LiveActor owner, LiveActor target) {
    }

    @Override
    public void setAngle(int angle) {
    }
}
