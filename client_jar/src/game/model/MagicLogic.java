package game.model;

import game.model.arrow.Arrow;
import game.render.Util;

public class MagicLogic {
    public int angle;
    public int vx;
    public int vy;
    public short va;
    public int x;
    public int y;
    public int z;
    public LiveActor target;
    int life;
    public int headArrowFrame;
    public int headTransform;
    public static final short[] SPEED = {1, 60, 8, 1, 1, 16, 60, 18, 60, 1, 1};
    public int index = 0;
    public boolean isSpeedUp = false;
    public boolean isEnd = false;

    public void setAngle(int angle) {
        this.angle = angle;
        this.vx = (this.va * Util.cos(angle)) >> 10;
        this.vy = (this.va * Util.sin(angle)) >> 10;
    }

    public void set(int type, int x, int y, int dir, LiveActor target) {
        this.x = x;
        this.y = y;
        this.target = target;
        switch (dir) {
            case 0:
                this.angle = 90;
                break;
            case 1:
                this.angle = 270;
                break;
            case 2:
                this.angle = 180;
                break;
            case 3:
                this.angle = 0;
                break;
        }
        if (type == 20) {
            type = 2;
        }
        this.va = (short) (256 * SPEED[type]);
        this.z = 0;
        this.life = 0;
        this.vx = (this.va * Util.cos(this.angle)) >> 10;
        this.vy = (this.va * Util.sin(this.angle)) >> 10;
    }

    public void updateAngle() {
        if (this.target == null) {
            this.isEnd = true;
            return;
        }
        int dx = this.target.x - this.x;
        int dy = (this.target.y - (this.target.height >> 1)) - this.y;
        this.life++;
        if ((Util.abs(dx) < 16 && Util.abs(dy) < 16) || this.life > 60) {
            this.isEnd = true;
            return;
        }
        int a = Util.angle(dx, dy);
        if (Math.abs(a - this.angle) < 90 || (dx * dx) + (dy * dy) > 4096) {
            if (Math.abs(a - this.angle) < 15) {
                this.angle = a;
            } else if ((a - this.angle >= 0 && a - this.angle < 180) || a - this.angle < -180) {
                this.angle = Util.fixangle(this.angle + 15);
            } else {
                this.angle = Util.fixangle(this.angle - 15);
            }
        }
        if (!this.isSpeedUp && this.va < 8192) {
            this.va = (short) (this.va + 1024);
        }
        this.vx = (this.va * Util.cos(this.angle)) >> 10;
        this.vy = (this.va * Util.sin(this.angle)) >> 10;
        int dx2 = dx + this.vx;
        int deltaX = dx2 >> 10;
        this.x += deltaX;
        int i = dx2 & 1023;
        int dy2 = dy + this.vy;
        int deltaY = dy2 >> 10;
        this.y += deltaY;
        int i2 = dy2 & 1023;
        this.index = Arrow.findDirIndexFromAngle(Util.angle(deltaX, -deltaY));
        this.headArrowFrame = Arrow.FRAME[this.index];
        this.headTransform = Arrow.TRANSFORM[this.index];
    }
}
