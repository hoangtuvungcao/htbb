package game.model;

import game.render.Canvas;

public class CharBuff extends Effect {
    public int timeLife;
    public long tick;
    int timeReceive;
    int count;

    public CharBuff(int x, int y, int type) {
        super(x, y, type);
        this.timeLife = -1;
        this.tick = 0L;
        this.tick = 0L;
    }

    public void setTimeLive(int time) {
        this.timeLife = time;
        this.count = 0;
        this.tick = System.currentTimeMillis() + ((long) (this.timeLife * 1000));
        if (this.timeLife <= 0) {
            this.wantDetroy = true;
        }
    }

    public boolean isCenter() {
        return this.type == 20 || this.type == 22 || this.type == 23 || this.type == 24 || this.type == 25 || this.type == 27;
    }

    @Override
    public void update() {
        if (Canvas.gameTick % 2 == 0) {
            this.f = (this.f + 1) % Effect.FRAME[this.type].length;
        }
        if (System.currentTimeMillis() > this.tick) {
            this.wantDetroy = true;
        }
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
