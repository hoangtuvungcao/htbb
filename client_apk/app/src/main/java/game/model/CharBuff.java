package game.model;

import game.render.GCanvas;

public class CharBuff extends Effect {
    int count;
    public long tick;
    public int timeLife;
    int timeReceive;

    public CharBuff(int i, int i2, int i3) {
        super(i, i2, i3);
        this.timeLife = -1;
        this.tick = 0L;
    }

    public void setTimeLive(int i) {
        this.timeLife = i;
        this.count = 0;
        long jCurrentTimeMillis = System.currentTimeMillis();
        int i2 = this.timeLife;
        this.tick = jCurrentTimeMillis + ((long) (i2 * 1000));
        if (i2 <= 0) {
            this.wantDetroy = true;
        }
    }

    public boolean isCenter() {
        return this.type == 20 || this.type == 22 || this.type == 23 || this.type == 24 || this.type == 25 || this.type == 27;
    }

    @Override
    public void update() {
        if (GCanvas.gameTick % 2 == 0) {
            this.f = (this.f + 1) % FRAME[this.type].length;
        }
        if (System.currentTimeMillis() > this.tick) {
            this.wantDetroy = true;
        }
    }

    public void setXY(int i, int i2) {
        this.x = i;
        this.y = i2;
    }
}
