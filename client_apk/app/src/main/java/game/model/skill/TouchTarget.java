package game.model.skill;

import game.model.LiveActor;
import game.render.Res;
import game.render.Util;

public class TouchTarget {
    public int angCur;
    public int angleRouch;
    public int angleStart;
    public int direct;
    public int disOld;
    public LiveActor to;
    public int v;
    public int x;
    public int y;
    public boolean isEnd = false;
    public boolean isDoiHuong = false;

    public TouchTarget(int i, int i2, LiveActor liveActor, int i3, int i4, int i5) {
        this.angleRouch = i4;
        this.to = liveActor;
        this.x = i;
        this.y = i2;
        this.angleStart = Util.fixangle(Util.angle(liveActor.x - this.x, -(liveActor.y - this.y)));
        int iRnd = Res.rnd(1, -1);
        this.direct = iRnd;
        this.angCur = Util.fixangle(this.angleStart - (i3 * iRnd));
        this.v = i5;
        this.disOld = Util.distance(this.x, this.y, this.to.x, this.to.y);
    }

    public void update() {
        if (this.to == null) {
            this.isEnd = true;
            return;
        }
        int iCos = (this.v * Util.cos(this.angCur)) >> 10;
        int i = (-(this.v * Util.sin(this.angCur))) >> 10;
        this.x += iCos;
        this.y += i;
        int iFixangle = Util.fixangle(this.angCur + (this.angleRouch * this.direct));
        int iAngle = Util.angle(this.to.x - this.x, -(this.to.y - this.y));
        if (Util.abs(this.angCur - iAngle) <= this.angleRouch * 3) {
            this.angCur = iAngle;
            if (Util.distance(this.x, this.y, this.to.x, this.to.y) <= this.v * 2) {
                this.isEnd = true;
                return;
            }
            return;
        }
        this.angCur = iFixangle;
        if (!this.isDoiHuong) {
            if (Util.distance(this.x, this.y, this.to.x, this.to.y) > this.disOld) {
                this.isDoiHuong = true;
                if (Res.rnd(4) != 1) {
                    this.direct *= -1;
                }
            }
        } else if (Util.distance(this.x, this.y, this.to.x, this.to.y) <= this.disOld) {
            this.isDoiHuong = false;
        }
        this.disOld = Util.distance(this.x, this.y, this.to.x, this.to.y);
    }
}
