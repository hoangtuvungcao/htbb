package game.render.screen;

import game.model.Effect;
import game.model.arrow.Arrow;
import game.render.Res;
import game.render.Util;
import javax.microedition.lcdui.Graphics;

public class EffectDapDoTim extends Effect {
    public int angle;
    public int xto;
    public int yto;
    public int pos = 0;
    public int thongsolathinh = 0;
    private int[] xw;
    private int[] yw;
    public int dx;
    public int dy;
    public int idimg;
    public int time;
    byte frame;

    public EffectDapDoTim(int x, int y, int xto, int yto) {
        this.yto = 0;
        this.x = x;
        this.y = y;
        this.xto = xto;
        this.yto = yto;
        setSpeed();
        this.time = 10;
    }

    private void setSpeed() {
        this.dx = this.xto - this.x;
        this.dy = (this.yto - 0) - this.y;
        this.angle = Util.angle(this.dx, this.dy);
        int nPosition = (Math.abs(this.dx) + Math.abs(this.dy)) / 20;
        if (nPosition < 2) {
            nPosition = 2;
        }
        this.xw = new int[nPosition];
        this.yw = new int[nPosition];
        for (int i = 0; i < nPosition; i++) {
            this.xw[i] = this.x + ((i * this.dx) / nPosition);
            this.yw[i] = this.y + ((i * this.dy) / nPosition);
        }
    }

    @Override
    public void paint(Graphics g) {
        if (Res.getImgArrow(10) != null) {
            g.drawRegion(Res.getImgArrow(10), 0, this.frame * Arrow.ARROWSIZE[1][10], Arrow.ARROWSIZE[0][10], Arrow.ARROWSIZE[1][10], 0, this.xw[this.pos], this.yw[this.pos], 3);
        }
    }

    @Override
    public void update() {
        if (this.time >= 0) {
            this.time--;
        }
        this.frame = (byte) (this.frame + 1);
        if (this.frame >= 3) {
            this.frame = (byte) 0;
        }
        if (this.time < 0) {
            if (this.pos < this.xw.length) {
                this.pos++;
            }
            if (this.pos >= this.xw.length) {
                this.pos = this.xw.length - 1;
                this.xw[this.pos] = this.xto;
                this.yw[this.pos] = this.yto;
                this.wantDetroy = true;
            }
        }
    }
}
