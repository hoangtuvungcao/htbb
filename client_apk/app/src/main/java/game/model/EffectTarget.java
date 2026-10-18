package game.model;

import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;

public class EffectTarget extends Effect {
    int angle0;
    int dir;
    int dx;
    int dy;
    int flip;
    int himg;
    LiveActor l_To;
    int type;
    int va;
    int vx;
    int vy;
    int xTo;
    int yTo;

    public EffectTarget(LiveActor liveActor, int i, int i2, int i3) {
        this.l_To = liveActor;
        this.dir = i;
        if (i == 0) {
            this.x = liveActor.x - (i2 == 0 ? (short) 100 : (short) 20);
            this.y = (liveActor.y - 100) - (i2 == 0 ? (short) 0 : (short) 100);
            this.xTo = liveActor.x + (i2 != 0 ? (short) -20 : (short) 0);
        } else if (i == 1) {
            this.x = liveActor.x + (i2 == 0 ? (short) 100 : (short) -10);
            this.y = (liveActor.y - 100) - (i2 == 0 ? (short) 0 : (short) 100);
            this.xTo = liveActor.x + (i2 != 0 ? (short) -10 : (short) 0);
        } else if (i == 2) {
            this.x = liveActor.x + (i2 == 0 ? (short) 100 : (short) 10);
            this.y = liveActor.y + (i2 != 0 ? (short) -200 : (short) 100);
            this.xTo = liveActor.x + (i2 != 0 ? (short) 10 : (short) 0);
        } else {
            this.x = liveActor.x - (i2 == 0 ? (short) 100 : (short) 20);
            this.y = liveActor.y + (i2 != 0 ? (short) -200 : (short) 100);
            this.xTo = liveActor.x + (i2 == 0 ? (short) 0 : (short) 20);
        }
        this.yTo = liveActor.y;
        this.type = i2;
        int iAngle = Util.angle(this.x - liveActor.x, (liveActor.y - (liveActor.height >> 1)) - this.y);
        this.angle0 = iAngle;
        this.va = i3;
        this.vx = (Util.cos(iAngle) * i3) >> 10;
        this.vy = (i3 * Util.sin(this.angle0)) >> 10;
        this.himg = liveActor.height;
    }

    @Override
    public void update() {
        LiveActor liveActor = this.l_To;
        if (liveActor != null) {
            int i = this.dir;
            if (i == 0) {
                this.xTo = liveActor.x + (this.type == 0 ? (short) -4 : (short) -20);
                this.yTo = this.l_To.y;
            } else if (i == 1) {
                this.xTo = liveActor.x + (this.type == 0 ? (short) 8 : (short) -10);
                this.yTo = this.l_To.y;
            } else if (i == 2) {
                this.xTo = liveActor.x + (this.type == 0 ? (short) 4 : (short) 10);
                this.yTo = this.l_To.y;
            } else {
                this.xTo = liveActor.x + (this.type == 0 ? (short) -8 : (short) 20);
                this.yTo = this.l_To.y;
            }
        }
        this.dx = this.xTo - this.x;
        int i2 = (this.yTo - (this.himg >> 1)) - this.y;
        this.dy = i2;
        int iAngle = Util.angle(this.dx, i2);
        this.angle0 = iAngle;
        this.vx = (this.va * Util.cos(iAngle)) >> 10;
        this.vy = (this.va * Util.sin(this.angle0)) >> 10;
        this.x += this.vx;
        this.y += this.vy;
        EffectManager.addLowEffect(this.x, this.y, this.type == 0 ? 32 : 29);
        int i3 = this.x - 20;
        int i4 = this.x + 20;
        int i5 = this.xTo;
        int i6 = this.himg;
        int i7 = i5 - (i6 / 2);
        int i8 = i5 + (i6 / 2);
        int i9 = this.y - 20;
        int i10 = this.y + 20;
        int i11 = this.yTo;
        int i12 = this.himg;
        if (Res.isDestroy(i3, i4, i7, i8, i9, i10, i11 - (i12 / 2), i11 + (i12 / 2))) {
            EffectManager.addHiEffect(this.xTo, this.yTo - 10, this.type == 0 ? 38 : 30);
            EffectManager.hiEffects.removeElement(this);
            LiveActor liveActor2 = this.l_To;
            if (liveActor2 != null) {
                int i13 = liveActor2.damAttack;
                this.l_To.doublejump();
                GCanvas.gameScr.startFlyText("-" + i13, 0, this.l_To.x, (this.l_To.y + this.l_To.dy) - 15, 1, -2);
            }
        }
        if (this.type != 1) {
            int i14 = this.dir;
            if (i14 == 0) {
                this.flip = 0;
                return;
            }
            if (i14 == 1) {
                this.flip = 2;
            } else if (i14 == 3) {
                this.flip = 1;
            } else {
                this.flip = 7;
            }
        }
    }

    @Override
    public void paint(Graphics graphics) {
        if (this.type == 1) {
            if (Res.getImgArrow(4) != null) {
                graphics.drawRegion(Res.imgArrow[4], 0, 0, 24, 24, 5, this.x, this.y, 3);
            }
        } else if (Res.imgEffect[31] != null) {
            graphics.drawRegion(Res.imgEffect[31], 0, 0, 20, 20, this.flip, this.x, this.y, 3);
        }
    }
}
