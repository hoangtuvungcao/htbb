package game.model;

import game.render.GCanvas;
import game.render.screen.GameScr;

public abstract class Dropable extends Actor {
    static short DROP_POWER = 6;
    public static final byte STATE_DROP = 1;
    public static final byte STATE_FLY = 2;
    public static final byte STATE_STAND = 0;
    short deltaH;
    short g;
    public boolean isSendGet = false;
    public int itemClass;
    public short item_template_id;
    int state;
    long timeStart;
    short xTo;
    short yTo;

    @Override
    public boolean isDropItem() {
        return true;
    }

    @Override
    public boolean isItemCanGet() {
        return true;
    }

    @Override
    public void setType(short s) {
        this.item_template_id = s;
        this.timeStart = System.currentTimeMillis();
    }

    public void startFlyTo(short s, short s2) {
        this.xTo = s;
        this.yTo = s2;
        this.state = 2;
        this.deltaH = (short) 0;
        this.g = DROP_POWER;
    }

    public void startDropFrom(short s, short s2, short s3, short s4) {
        this.x = s;
        this.y = s2;
        this.xTo = s3;
        this.yTo = s4;
        this.state = 1;
        this.deltaH = (short) 0;
        this.g = DROP_POWER;
        this.timeStart = System.currentTimeMillis();
    }

    @Override
    public void setPosTo(short s, short s2) {
        this.x = s;
        this.y = (short) ((s2 - 4) + (GCanvas.r.nextInt() % 8));
    }

    @Override
    public void update() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        if (this.catagory == 3 || this.catagory == 6) {
            if (jCurrentTimeMillis - this.timeStart > 25000) {
                this.wantDestroy = true;
            }
        } else if (this.catagory == 4) {
            if (jCurrentTimeMillis - this.timeStart > (this.item_template_id >= 10 ? 60000 : 15000)) {
                this.wantDestroy = true;
            }
        }
        int i = this.state;
        if (i == 1 || i == 2) {
            this.x = (short) (this.x + ((short) ((this.xTo - this.x) >> 2)));
            this.y = (short) (this.y + ((short) ((this.yTo - this.y) >> 2)));
            short s = this.g;
            if (s >= (-DROP_POWER)) {
                this.deltaH = (short) (this.deltaH + s);
                this.g = (short) (s - 1);
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
}
