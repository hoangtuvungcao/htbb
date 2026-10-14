package game.render.screen;

import game.model.Effect;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Eff_da_vang extends Effect {
    public int vx;
    public int vy;
    public int t;
    public int y_changspeed;
    public int f;
    public int tt;
    public int frame;
    public int loai;
    public int id;
    int[] arry = {-10, 12, 0};
    public int[] time = {0, 1};

    public Eff_da_vang(int x, int y) {
        this.x = x;
        this.y = y;
        this.vx = Canvas.random(-1, 3);
        this.t = Canvas.random(5, 10);
        this.y_changspeed = y + this.arry[Math.abs(Canvas.r.nextInt() % this.arry.length)];
        this.tt = this.time[Util.abs(Canvas.r.nextInt() % this.time.length)];
    }

    @Override
    public void paint(Graphics g) {
        Image img = Res.getImageEffect(60);
        if (img != null) {
            g.drawImage(img, this.x, this.y, 3);
        }
    }

    @Override
    public void update() {
        if (this.tt >= 0) {
            this.tt--;
        }
        if (this.tt < 0) {
            this.x += this.vx;
            this.y += this.vy;
            if (this.vy < 10) {
                this.vy += 2;
            }
            if (this.y >= this.y_changspeed) {
                this.vy = -this.t;
                this.t /= 3;
            }
            if (this.t <= 2) {
                this.vx = 0;
                this.wantDetroy = true;
            }
        }
    }
}
