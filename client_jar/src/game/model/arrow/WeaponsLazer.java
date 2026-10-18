package game.model.arrow;

import game.model.EffectManager;
import game.model.LiveActor;
import game.render.Canvas;
import game.render.Util;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;

public class WeaponsLazer extends IArrow {
    int angle0;
    int lim;
    int x;
    int y;
    int xkc;
    int ykc;
    int power;
    int time;
    int timeStarpaint;
    public int xlim;
    public int ylim;
    int[] color;
    int idColor;

    public WeaponsLazer(LiveActor from, LiveActor to, int xadd, int yadd, int power, int idColor) {
        this.timeStarpaint = -1;
        this.color = new int[]{16741818, 8388469, 7710463};
        this.idColor = 0;
        this.x = from.x + xadd;
        this.y = from.y + yadd;
        this.xlim = to.x;
        this.ylim = to.y - 28;
        this.xkc = this.xlim - this.x;
        this.ykc = this.ylim - this.y;
        this.angle0 = Util.angle(this.xkc, this.ykc);
        this.lim = 6;
        this.power = power;
        this.idColor = idColor;
        to.jump();
        EffectManager.addHiEffect(to.x, (to.y + to.dy) - 10, 11);
        Canvas.gameScr.startFlyText(new StringBuffer("-").append(power).toString(), 0, to.x + 0, (to.y + to.dy) - 15, 1, -2);
    }

    @Override
    public void onArrowTouchTarget() {
    }

    public WeaponsLazer(int x, int y, int xTo, int yTo, int time, int lim) {
        this.timeStarpaint = -1;
        this.color = new int[]{16741818, 8388469, 7710463};
        this.idColor = 0;
        this.x = x;
        this.y = y;
        this.xlim = xTo;
        this.ylim = yTo;
        this.xkc = this.xlim - x;
        this.ykc = this.ylim - y;
        this.angle0 = Util.angle(this.xkc, this.ykc);
        this.lim = lim;
        this.time = time;
    }

    public WeaponsLazer(int x, int y, int xTo, int yTo, int time, int lim, int timepaint) {
        this.timeStarpaint = -1;
        this.color = new int[]{16741818, 8388469, 7710463};
        this.idColor = 0;
        this.x = x;
        this.y = y;
        this.xlim = xTo;
        this.ylim = yTo;
        this.xkc = this.xlim - x;
        this.ykc = this.ylim - y;
        this.angle0 = Util.angle(this.xkc, this.ykc);
        this.lim = lim;
        this.time = time;
        this.timeStarpaint = timepaint;
    }

    public void update2() {
        if (this.timeStarpaint >= 0) {
            this.timeStarpaint--;
        }
        if (this.time >= 0) {
            this.time--;
        }
        if (this.time <= 0) {
            this.lim--;
        }
        if (this.lim <= 0) {
            this.wantDestroy = true;
        }
    }

    @Override
    public void paint(Graphics g) {
        if (this.timeStarpaint < 0) {
            if ((this.angle0 > 60 && this.angle0 < 120) || (this.angle0 > 240 && this.angle0 < 300)) {
                g.setColor(this.color[this.idColor]);
                for (int i = 0; i < this.lim; i++) {
                    g.drawLine(this.x + i, this.y, this.x + i + this.xkc, this.y + this.ykc);
                    g.drawLine(this.x - i, this.y, (this.x - i) + this.xkc, this.y + this.ykc);
                }
                g.setColor(16777215);
                for (int i2 = 0; i2 < this.lim / 2; i2++) {
                    g.drawLine(this.x + i2, this.y, this.x + i2 + this.xkc, this.y + this.ykc);
                    g.drawLine(this.x - i2, this.y, (this.x - i2) + this.xkc, this.y + this.ykc);
                }
                return;
            }
            g.setColor(this.color[this.idColor]);
            for (int i3 = 0; i3 < this.lim; i3++) {
                g.drawLine(this.x, this.y + i3, this.x + this.xkc, this.y + this.ykc + i3);
                g.drawLine(this.x, this.y - i3, this.x + this.xkc, (this.y + this.ykc) - i3);
            }
            g.setColor(16777215);
            for (int i4 = 0; i4 < this.lim / 2; i4++) {
                g.drawLine(this.x, this.y + i4, this.x + this.xkc, this.y + this.ykc + i4);
                g.drawLine(this.x, this.y - i4, this.x + this.xkc, (this.y + this.ykc) - i4);
            }
        }
    }

    @Override
    public void set(int type, int x, int y, int power, byte effect2, LiveActor owner, LiveActor target) {
    }

    @Override
    public void setAngle(int angle) {
    }

    @Override
    public void update() {
        this.lim--;
        if (this.lim <= 0) {
            if (Canvas.gameScr.arrowsDown.contains(this)) {
                Canvas.gameScr.arrowsDown.removeElement(this);
            }
            if (GameScr.arrowsUp.contains(this)) {
                GameScr.arrowsUp.removeElement(this);
            }
        }
    }
}
