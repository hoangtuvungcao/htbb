package game.model.skill;

import game.model.LiveActor;
import game.render.Res;
import game.render.Util;

public class TouchTarget {
    public LiveActor to;
    public int x;
    public int y;
    public int angleStart;
    public int angCur;
    public int v;
    public int disOld;
    public int angleRouch;
    public boolean isEnd = false;
    public boolean isDoiHuong = false;
    public int direct = Res.rnd(1, -1);

    public TouchTarget(int xFrom, int yFrom, LiveActor t, int hardAngle, int angleRouch, int v) {
        this.angleRouch = angleRouch;
        this.to = t;
        this.x = xFrom;
        this.y = yFrom;
        this.angleStart = Util.fixangle(Util.angle(t.x - this.x, -(t.y - this.y)));
        this.angCur = Util.fixangle(this.angleStart - (hardAngle * this.direct));
        this.v = v;
        this.disOld = Util.distance(this.x, this.y, this.to.x, this.to.y);
    }

    public void update() {
        if (this.to == null) {
            this.isEnd = true;
            return;
        }
        int xa = (this.v * Util.cos(this.angCur)) >> 10;
        int ya = (-(this.v * Util.sin(this.angCur))) >> 10;
        this.x += xa;
        this.y += ya;
        int angDau = Util.fixangle(this.angCur + (this.angleRouch * this.direct));
        int ang = Util.angle(this.to.x - this.x, -(this.to.y - this.y));
        if (Util.abs(this.angCur - ang) <= this.angleRouch * 3) {
            this.angCur = ang;
            if (Util.distance(this.x, this.y, this.to.x, this.to.y) <= this.v * 2) {
                this.isEnd = true;
                return;
            }
            return;
        }
        this.angCur = angDau;
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
