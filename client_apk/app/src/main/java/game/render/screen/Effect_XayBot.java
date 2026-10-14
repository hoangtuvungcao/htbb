package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Effect;
import game.render.GCanvas;

public class Effect_XayBot extends Effect {
    byte count;
    byte frame;
    byte id;
    public int idimg;
    byte loai;
    public int time;
    public int tung;
    public int va;
    byte vx;
    public int yto;
    byte[] arr_frame = {0, 1, 2, 3};
    byte[] arr = {-2, -1, 0, 1, 2};
    byte[] arr1 = {2, 10, 6};

    public Effect_XayBot(int i, int i2, int i3, int i4, int i5) {
        this.tung = 1;
        this.x = i;
        this.y = i2;
        this.time = i3;
        this.yto = i4;
        this.id = (byte) 54;
        this.type = (short) -1;
        this.loai = (byte) i5;
        if (i5 == -1) {
            this.tung = GCanvas.random(5, 15);
            this.vx = this.arr[Math.abs(GCanvas.r.nextInt() % this.arr.length)];
        }
    }

    @Override
    public void paint(Graphics graphics) {
        graphics.setColor(-1509383);
        graphics.fillRect(this.x, this.y, 2, 2);
    }

    @Override
    public void update() {
        int i = this.time;
        if (i >= 0) {
            this.time = i - 1;
        }
        this.frame = this.arr_frame[Math.abs(GCanvas.r.nextInt() % (this.arr_frame.length - 1))];
        if (this.time < 0) {
            this.y += this.va;
        }
        this.x += this.vx;
        int i2 = this.va;
        if (i2 < 10) {
            this.va = i2 + 1;
        }
        if (this.tung != 0) {
            this.x += this.arr[Math.abs(GCanvas.r.nextInt() % this.arr.length)];
            this.va = -this.tung;
            this.tung = 0;
        }
        if (this.y >= this.yto) {
            this.wantDetroy = true;
        }
    }
}
