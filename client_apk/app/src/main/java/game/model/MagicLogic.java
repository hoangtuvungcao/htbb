package game.model;

import game.model.arrow.Arrow;
import game.render.Util;

public class MagicLogic {
    public static final short[] SPEED = {1, 60, 8, 1, 1, 16, 60, 18, 60, 1, 1};
    public int angle;
    public int headArrowFrame;
    public int headTransform;
    int life;
    public LiveActor target;
    public short va;
    public int vx;
    public int vy;
    public int x;
    public int y;
    public int z;
    public int index = 0;
    public boolean isSpeedUp = false;
    public boolean isEnd = false;

    public void setAngle(int i) {
        this.angle = i;
        this.vx = (this.va * Util.cos(i)) >> 10;
        this.vy = (this.va * Util.sin(i)) >> 10;
    }

    public void set(int i, int i2, int i3, int i4, LiveActor liveActor) {
        this.x = i2;
        this.y = i3;
        this.target = liveActor;
        if (i4 == 0) {
            this.angle = 90;
        } else if (i4 == 1) {
            this.angle = 270;
        } else if (i4 == 2) {
            this.angle = 180;
        } else if (i4 == 3) {
            this.angle = 0;
        }
        if (i == 20) {
            i = 2;
        }
        short s = (short) (SPEED[i] * 256);
        this.va = s;
        this.z = 0;
        this.life = 0;
        this.vx = (s * Util.cos(this.angle)) >> 10;
        this.vy = (this.va * Util.sin(this.angle)) >> 10;
    }

    public void updateAngle() {
        short s;
        LiveActor liveActor = this.target;
        if (liveActor == null) {
            this.isEnd = true;
            return;
        }
        int i = liveActor.x - this.x;
        int i2 = (this.target.y - (this.target.height >> 1)) - this.y;
        this.life++;
        if ((Util.abs(i) < 16 && Util.abs(i2) < 16) || this.life > 60) {
            this.isEnd = true;
            return;
        }
        int iAngle = Util.angle(i, i2);
        if (Math.abs(iAngle - this.angle) < 90 || (i * i) + (i2 * i2) > 4096) {
            if (Math.abs(iAngle - this.angle) < 15) {
                this.angle = iAngle;
            } else {
                int i3 = this.angle;
                if ((iAngle - i3 >= 0 && iAngle - i3 < 180) || iAngle - i3 < -180) {
                    this.angle = Util.fixangle(i3 + 15);
                } else {
                    this.angle = Util.fixangle(i3 - 15);
                }
            }
        }
        if (!this.isSpeedUp && (s = this.va) < 8192) {
            this.va = (short) (s + 1024);
        }
        this.vx = (this.va * Util.cos(this.angle)) >> 10;
        int iSin = (this.va * Util.sin(this.angle)) >> 10;
        this.vy = iSin;
        int i4 = (i + this.vx) >> 10;
        this.x += i4;
        int i5 = (i2 + iSin) >> 10;
        this.y += i5;
        this.index = Arrow.findDirIndexFromAngle(Util.angle(i4, -i5));
        this.headArrowFrame = Arrow.FRAME[this.index];
        this.headTransform = Arrow.TRANSFORM[this.index];
    }
}
