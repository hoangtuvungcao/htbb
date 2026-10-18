package game.model;

import game.render.Canvas;
import game.render.screen.GameScr;

public abstract class Dropable extends Actor {
    public static final byte STATE_STAND = 0;
    public static final byte STATE_DROP = 1;
    public static final byte STATE_FLY = 2;
    public short item_template_id;
    public boolean isSendGet = false;
    int state;
    short xTo;
    short yTo;
    short deltaH;
    short g;
    static short DROP_POWER = 6;
    public int itemClass;
    long timeStart;

    @Override
    public void setType(short template_id) {
        this.item_template_id = template_id;
        this.timeStart = System.currentTimeMillis();
    }

    public void startFlyTo(short xTo, short yTo) {
        this.xTo = xTo;
        this.yTo = yTo;
        this.state = 2;
        this.deltaH = (short) 0;
        this.g = DROP_POWER;
    }

    public void startDropFrom(short xFrom, short yFrom, short xTo, short yTo) {
        this.x = xFrom;
        this.y = yFrom;
        this.xTo = xTo;
        this.yTo = yTo;
        this.state = 1;
        this.deltaH = (short) 0;
        this.g = DROP_POWER;
        this.timeStart = System.currentTimeMillis();
    }

    @Override
    public void setPosTo(short x, short y) {
        this.x = x;
        this.y = (short) ((y - 4) + (Canvas.r.nextInt() % 8));
    }

    @Override
    public void update() {
        long now = System.currentTimeMillis();
        if (this.catagory == 3 || this.catagory == 6) {
            if (now - this.timeStart > 25000) {
                this.wantDestroy = true;
            }
        } else if (this.catagory == 4) {
            int time = this.item_template_id >= 10 ? 60000 : 15000;
            if (now - this.timeStart > time) {
                this.wantDestroy = true;
            }
        }
        if (this.state == 1 || this.state == 2) {
            this.x = (short) (this.x + ((short) ((this.xTo - this.x) >> 2)));
            this.y = (short) (this.y + ((short) ((this.yTo - this.y) >> 2)));
            if (this.g >= (-DROP_POWER)) {
                this.deltaH = (short) (this.deltaH + this.g);
                this.g = (short) (this.g - 1);
            }
            if ((GameScr.abs(this.x - this.xTo) < 4 || GameScr.abs(this.y - this.yTo) < 4) && this.deltaH <= 1) {
                this.x = this.xTo;
                this.y = this.yTo;
                this.deltaH = (short) 0;
                this.g = (short) 0;
                if (this.state == 2) {
                    this.wantDestroy = true;
                }
                this.state = 0;
            }
        }
    }

    @Override
    public boolean isItemCanGet() {
        return true;
    }

    @Override
    public boolean isDropItem() {
        return true;
    }
}
