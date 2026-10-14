package game.model.skill;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.arrow.Arrow;
import game.render.GCanvas;
import game.render.Res;

public class Skill_TruRong extends Effect {
    int TimeStarAtack;
    public int dx;
    public int dy;
    byte frame;
    byte frame1;
    public int height = 0;
    public int imgIndex;
    boolean isShot;
    public int lim;
    public int pos;
    LiveActor target;
    public int time;
    public int xto;
    private int[] xw;
    public int yto;
    private int[] yw;

    public Skill_TruRong(int i, int i2, int i3, int i4, LiveActor liveActor, int i5) {
        this.target = liveActor;
        this.x = i;
        this.y = i2 + 30;
        this.xto = i3;
        this.yto = i4;
        this.imgIndex = 15;
        this.lim = i2 - 60;
        this.TimeStarAtack = i5;
        setspeed();
    }

    public void setspeed() {
        int i = this.xto - this.x;
        this.dx = i;
        this.dy = this.yto - this.lim;
        int iAbs = (Math.abs(i) + Math.abs(this.dy)) / 20;
        if (iAbs < 2) {
            iAbs = 2;
        }
        this.xw = new int[iAbs];
        this.yw = new int[iAbs];
        for (int i2 = 0; i2 < iAbs; i2++) {
            this.xw[i2] = this.x + ((this.dx * i2) / iAbs);
            this.yw[i2] = this.lim + ((this.dy * i2) / iAbs);
        }
    }

    @Override
    public void paint(Graphics graphics) {
        if (this.TimeStarAtack < 0) {
            if (this.y > this.lim) {
                graphics.drawRegion(Res.getImgeffect(55), 0, (this.frame1 * Res.getImgeffect(55).getHeight()) / 2, Res.getImgeffect(55).getWidth(), Res.getImgeffect(55).getHeight() / 2, 0, this.x, this.y, Graphics.VCENTER | Graphics.HCENTER);
            }
            if (!this.isShot || Res.getImgArrow(this.imgIndex) == null) {
                return;
            }
            Bitmap bitmap = Res.imgArrow[this.imgIndex];
            int i = this.frame * Arrow.ARROWSIZE[1][this.imgIndex];
            byte b = Arrow.ARROWSIZE[0][this.imgIndex];
            byte b2 = Arrow.ARROWSIZE[1][this.imgIndex];
            int[] iArr = this.xw;
            int i2 = this.pos;
            graphics.drawRegion(bitmap, 0, i, b, b2, 0, iArr[i2], this.yw[i2], 3);
        }
    }

    @Override
    public void update() {
        int i;
        int i2;
        this.frame = (byte) ((this.frame + 1) % 3);
        this.frame1 = (byte) ((this.frame1 + 1) % 2);
        int i3 = this.TimeStarAtack;
        if (i3 >= 0) {
            this.TimeStarAtack = i3 - 1;
        }
        if (this.TimeStarAtack < 0) {
            if (this.y > this.lim) {
                this.y -= 10;
            }
            if (this.y <= this.lim && (i2 = this.time) <= 6) {
                this.time = i2 + 1;
            }
            if (this.time == 2) {
                EffectManager.addHiEffect(this.x, this.y, 54);
            }
            int i4 = this.time;
            if (i4 >= 6 && !this.isShot) {
                this.isShot = true;
            }
            if (i4 > 6 && (i = this.pos) < this.xw.length) {
                this.pos = i + 1;
            }
            int i5 = this.pos;
            int[] iArr = this.xw;
            if (i5 >= iArr.length) {
                int length = iArr.length - 1;
                this.pos = length;
                int i6 = this.xto;
                iArr[length] = i6;
                int[] iArr2 = this.yw;
                int i7 = this.yto;
                iArr2[length] = i7;
                EffectManager.addLowEffect(i6, i7, 53);
                this.wantDetroy = true;
                LiveActor liveActor = this.target;
                if (liveActor == null || liveActor.damAttack == 0 || this.target.damAttack == 2000000) {
                    return;
                }
                GCanvas.gameScr.startFlyText("-" + this.target.damAttack, 0, this.target.x, (this.target.y + this.target.dy) - 15, 1, -2);
            }
        }
    }
}
