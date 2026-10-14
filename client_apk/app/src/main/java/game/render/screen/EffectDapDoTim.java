package game.render.screen;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.Effect;
import game.model.arrow.Arrow;
import game.render.Res;
import game.render.Util;

public class EffectDapDoTim extends Effect {
    public int angle;
    public int dx;
    public int dy;
    byte frame;
    public int idimg;
    public int pos = 0;
    public int thongsolathinh = 0;
    public int time;
    public int xto;
    private int[] xw;
    public int yto;
    private int[] yw;

    public EffectDapDoTim(int i, int i2, int i3, int i4) {
        this.yto = 0;
        this.x = i;
        this.y = i2;
        this.xto = i3;
        this.yto = i4;
        setSpeed();
        this.time = 10;
    }

    private void setSpeed() {
        this.dx = this.xto - this.x;
        int i = this.yto - this.y;
        this.dy = i;
        this.angle = Util.angle(this.dx, i);
        int iAbs = (Math.abs(this.dx) + Math.abs(this.dy)) / 20;
        if (iAbs < 2) {
            iAbs = 2;
        }
        this.xw = new int[iAbs];
        this.yw = new int[iAbs];
        for (int i2 = 0; i2 < iAbs; i2++) {
            this.xw[i2] = this.x + ((this.dx * i2) / iAbs);
            this.yw[i2] = this.y + ((this.dy * i2) / iAbs);
        }
    }

    @Override
    public void paint(Graphics graphics) {
        Bitmap imgArrow = Res.getImgArrow(10);
        int i = this.frame * Arrow.ARROWSIZE[1][10];
        byte b = Arrow.ARROWSIZE[0][10];
        byte b2 = Arrow.ARROWSIZE[1][10];
        int[] iArr = this.xw;
        int i2 = this.pos;
        graphics.drawRegion(imgArrow, 0, i, b, b2, 0, iArr[i2], this.yw[i2], Graphics.VCENTER | Graphics.HCENTER);
    }

    @Override
    public void update() {
        int i = this.time;
        if (i >= 0) {
            this.time = i - 1;
        }
        byte b = (byte) (this.frame + 1);
        this.frame = b;
        if (b >= 3) {
            this.frame = (byte) 0;
        }
        if (this.time < 0) {
            int i2 = this.pos;
            int[] iArr = this.xw;
            if (i2 < iArr.length) {
                this.pos = i2 + 1;
            }
            if (this.pos >= iArr.length) {
                int length = iArr.length - 1;
                this.pos = length;
                iArr[length] = this.xto;
                this.yw[length] = this.yto;
                this.wantDetroy = true;
            }
        }
    }
}
