package game.render.screen;

import game.model.Effect;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

public class Effect_XayBot extends Effect {
    byte count;
    byte id;
    byte vx;
    byte loai;
    public int idimg;
    public int time;
    public int yto;
    public int tung;
    public int va;
    byte[] arr_frame = {0, 1, 2, 3};
    byte[] arr = {-2, -1, 0, 1, 2};
    byte[] arr1 = {2, 10, 6};

    public Effect_XayBot(int x, int y, int time, int yto, int loai) {
        this.tung = 1;
        this.x = x;
        this.y = y;
        this.time = time;
        this.yto = yto;
        this.id = (byte) 54;
        this.type = (short) -1;
        this.loai = (byte) loai;
        if (loai == -1) {
            this.tung = Canvas.random(5, 15);
            this.vx = this.arr[Math.abs(Canvas.r.nextInt() % this.arr.length)];
        }
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(15267833);
        g.fillRect(this.x, this.y, 2, 2);
    }

    @Override
    public void update() {
        if (this.time >= 0) {
            this.time--;
        }
        if (this.time < 0) {
            this.y += this.va;
        }
        this.x += this.vx;
        if (this.va < 10) {
            this.va++;
        }
        if (this.tung != 0) {
            this.x += this.arr[Math.abs(Canvas.r.nextInt() % this.arr.length)];
            this.va = -this.tung;
            this.tung = 0;
        }
        if (this.y >= this.yto) {
            this.wantDetroy = true;
        }
    }
}
