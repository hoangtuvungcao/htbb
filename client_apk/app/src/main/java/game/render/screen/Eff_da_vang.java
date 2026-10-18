package game.render.screen;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.Effect;
import game.render.GCanvas;
import game.render.Res;

public class Eff_da_vang extends Effect {
    public int f;
    public int frame;
    public int id;
    public Bitmap img;
    public int loai;
    public int t;
    public int tt;
    public int vx;
    public int vy;
    public int y_changspeed;
    int[] arry = {-10, 12, 0};
    public int[] time = {0, 1};

    public Eff_da_vang(int i, int i2) {
        this.x = i;
        this.y = i2;
        this.vx = GCanvas.random(-1, 3);
        this.t = GCanvas.random(5, 10);
        this.y_changspeed = i2 + this.arry[Math.abs(GCanvas.r.nextInt() % this.arry.length)];
        this.tt = this.time[Math.abs(GCanvas.r.nextInt() % this.time.length)];
        this.img = Res.imgEffect[60];
    }

    @Override
    public void paint(Graphics graphics) {
        graphics.drawImage(this.img, this.x, this.y, 3);
    }

    @Override
    public void update() {
        int i = this.tt;
        if (i >= 0) {
            this.tt = i - 1;
        }
        if (this.tt < 0) {
            this.x += this.vx;
            this.y += this.vy;
            int i2 = this.vy;
            if (i2 < 10) {
                this.vy = i2 + 2;
            }
            if (this.y >= this.y_changspeed) {
                int i3 = this.t;
                this.vy = -i3;
                this.t = i3 / 3;
            }
            if (this.t <= 2) {
                this.vx = 0;
                this.wantDetroy = true;
            }
        }
    }
}
