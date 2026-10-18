package game.model;

import game.core.j2me.Graphics;
import game.model.arrow.Arrow;
import game.model.arrow.IArrow;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;

public class ArrowTarget extends IArrow {
    public short angle;
    public short angleShadow;
    private byte d;
    private byte imgIndex;
    public boolean isTarget;
    int power;
    private byte timeDelay = 0;
    private byte v;
    public short x;
    public short xShadow;
    public short xTo;
    public short y;
    public short yShadow;
    public short yTo;

    @Override
    public void onArrowTouchTarget() {
    }

    @Override
    public void set(int i, int i2, int i3, int i4, byte b, LiveActor liveActor, LiveActor liveActor2) {
    }

    @Override
    public void setAngle(int i) {
    }

    public ArrowTarget(byte b) {
        this.imgIndex = b;
    }

    @Override
    public void update() {
        int iCos = (this.v * Util.cos(this.angle)) >> 10;
        int i = (-(this.v * Util.sin(this.angle))) >> 10;
        this.x = (short) (this.x + iCos);
        this.y = (short) (this.y + i);
        int iCos2 = (this.v * Util.cos(this.angleShadow)) >> 10;
        int i2 = (-(this.v * Util.sin(this.angleShadow))) >> 10;
        this.xShadow = (short) (this.xShadow + iCos2);
        this.yShadow = (short) (this.yShadow + i2);
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
                GCanvas.gameScr.arrowsDown.addElement(this);
                GCanvas.gameScr.startExplosionAt(this.x, (short) (this.y + 10));
            } else {
                if (this.power != 0) {
                    GCanvas.gameScr.startFlyText("-" + this.power, 0, this.x, this.y - 15, 1, -2);
                }
                EffectManager.addHiEffect(this.x, this.y - 20, 12);
            }
            GameScr gameScr = GCanvas.gameScr;
            GameScr.arrowsUp.removeElement(this);
        } else if (Util.distance(this.xTo, this.yTo, this.x, this.y) > 1000) {
            GameScr gameScr2 = GCanvas.gameScr;
            GameScr.arrowsUp.removeElement(this);
        }
        byte b = this.timeDelay;
        if (b > 0) {
            byte b2 = (byte) (b - 1);
            this.timeDelay = b2;
            if (b2 == 0) {
                GCanvas.gameScr.arrowsDown.removeElement(this);
            }
        }
    }

    @Override
    public void paint(Graphics graphics) {
        if (Res.getImgArrow(this.imgIndex) != null) {
            graphics.drawImage(Res.imgShadow, this.xShadow, this.yShadow, Graphics.TOP | Graphics.HCENTER);
            graphics.drawRegion(Res.imgArrow[this.imgIndex], 0, Arrow.FRAME[this.d] * Arrow.ARROWSIZE[1][this.imgIndex], Arrow.ARROWSIZE[0][this.imgIndex], Arrow.ARROWSIZE[1][this.imgIndex], Arrow.TRANSFORM[this.d], this.x, this.y, 3);
        }
    }

    public void set(int i, int i2, int i3, int i4, byte b, int i5, int i6, byte b2, boolean z) {
        this.power = i4;
        this.isTarget = z;
        this.v = b2;
        int i7 = i5 - i2;
        short s = (short) i2;
        this.xShadow = s;
        this.x = s;
        this.y = (short) i3;
        this.xTo = (short) i5;
        this.yTo = (short) i6;
        this.yShadow = (short) (i3 + 30);
        int i8 = -(i6 - i3);
        this.d = (byte) Arrow.findDirIndexFromAngle(Util.angle(i7, i8));
        this.angle = (short) Util.angle(i7, i8);
        this.angleShadow = (short) Util.angle(i5 - this.xShadow, -(i6 - this.yShadow));
    }
}
