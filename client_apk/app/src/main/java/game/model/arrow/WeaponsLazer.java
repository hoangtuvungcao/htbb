package game.model.arrow;

import game.core.j2me.Graphics;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.GCanvas;
import game.render.Util;
import game.render.screen.GameScr;

public class WeaponsLazer extends IArrow {
    int angle0;
    int[] color;
    int idColor;
    int lim;
    int power;
    int time;
    int timeStarpaint;
    int x;
    int xkc;
    public int xlim;
    int y;
    int ykc;
    public int ylim;

    @Override
    public void onArrowTouchTarget() {
    }

    @Override
    public void set(int i, int i2, int i3, int i4, byte b, LiveActor liveActor, LiveActor liveActor2) {
    }

    @Override
    public void setAngle(int i) {
    }

    public WeaponsLazer(LiveActor liveActor, LiveActor liveActor2, int i, int i2, int i3, int i4) {
        this.timeStarpaint = -1;
        this.color = new int[]{-35398, -8388747, -9066753};
        this.idColor = 0;
        this.x = liveActor.x + i;
        this.y = liveActor.y + i2;
        this.xlim = liveActor2.x;
        int i5 = liveActor2.y - 28;
        this.ylim = i5;
        int i6 = this.xlim - this.x;
        this.xkc = i6;
        int i7 = i5 - this.y;
        this.ykc = i7;
        this.angle0 = Util.angle(i6, i7);
        this.lim = 6;
        this.power = i3;
        this.idColor = i4;
        liveActor2.jump();
        EffectManager.addHiEffect(liveActor2.x, (liveActor2.y + liveActor2.dy) - 10, 11);
        GCanvas.gameScr.startFlyText("-" + i3, 0, liveActor2.x, (liveActor2.y + liveActor2.dy) - 15, 1, -2);
    }

    public WeaponsLazer(int i, int i2, int i3, int i4, int i5, int i6) {
        this.timeStarpaint = -1;
        this.color = new int[]{-35398, -8388747, -9066753};
        this.idColor = 0;
        this.x = i;
        this.y = i2;
        this.xlim = i3;
        this.ylim = i4;
        int i7 = i3 - i;
        this.xkc = i7;
        int i8 = i4 - i2;
        this.ykc = i8;
        this.angle0 = Util.angle(i7, i8);
        this.lim = i6;
        this.time = i5;
    }

    public WeaponsLazer(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        this.timeStarpaint = -1;
        this.color = new int[]{-35398, -8388747, -9066753};
        this.idColor = 0;
        this.x = i;
        this.y = i2;
        this.xlim = i3;
        this.ylim = i4;
        int i8 = i3 - i;
        this.xkc = i8;
        int i9 = i4 - i2;
        this.ykc = i9;
        this.angle0 = Util.angle(i8, i9);
        this.lim = i6;
        this.time = i5;
        this.timeStarpaint = i7;
    }

    public void update2() {
        int i = this.timeStarpaint;
        if (i >= 0) {
            this.timeStarpaint = i - 1;
        }
        int i2 = this.time;
        if (i2 >= 0) {
            this.time = i2 - 1;
        }
        if (this.time <= 0) {
            this.lim--;
        }
        if (this.lim <= 0) {
            this.wantDestroy = true;
        }
    }

    @Override
    public void paint(Graphics graphics) {
        if (this.timeStarpaint < 0) {
            int i = this.angle0;
            int i2 = 0;
            if ((i > 60 && i < 120) || (i > 240 && i < 300)) {
                graphics.setColor(this.color[this.idColor]);
                for (int i3 = 0; i3 < this.lim; i3++) {
                    int i4 = this.x;
                    int i5 = this.y;
                    graphics.drawLine(i4 + i3, i5, i4 + i3 + this.xkc, this.ykc + i5);
                    int i6 = this.x;
                    int i7 = this.y;
                    graphics.drawLine(i6 - i3, i7, (i6 - i3) + this.xkc, this.ykc + i7);
                }
                graphics.setColor(-1);
                while (i2 < this.lim / 2) {
                    int i8 = this.x;
                    int i9 = this.y;
                    graphics.drawLine(i8 + i2, i9, i8 + i2 + this.xkc, this.ykc + i9);
                    int i10 = this.x;
                    int i11 = this.y;
                    graphics.drawLine(i10 - i2, i11, (i10 - i2) + this.xkc, this.ykc + i11);
                    i2++;
                }
                return;
            }
            graphics.setColor(this.color[this.idColor]);
            for (int i12 = 0; i12 < this.lim; i12++) {
                int i13 = this.x;
                int i14 = this.y;
                graphics.drawLine(i13, i14 + i12, this.xkc + i13, i14 + this.ykc + i12);
                int i15 = this.x;
                int i16 = this.y;
                graphics.drawLine(i15, i16 - i12, this.xkc + i15, (i16 + this.ykc) - i12);
            }
            graphics.setColor(-1);
            while (i2 < this.lim / 2) {
                int i17 = this.x;
                int i18 = this.y;
                graphics.drawLine(i17, i18 + i2, this.xkc + i17, i18 + this.ykc + i2);
                int i19 = this.x;
                int i20 = this.y;
                graphics.drawLine(i19, i20 - i2, this.xkc + i19, (i20 + this.ykc) - i2);
                i2++;
            }
        }
    }

    @Override
    public void update() {
        int i = this.lim - 1;
        this.lim = i;
        if (i <= 0) {
            if (GCanvas.gameScr.arrowsDown.contains(this)) {
                GCanvas.gameScr.arrowsDown.removeElement(this);
            }
            GameScr gameScr = GCanvas.gameScr;
            if (GameScr.arrowsUp.contains(this)) {
                GameScr gameScr2 = GCanvas.gameScr;
                GameScr.arrowsUp.removeElement(this);
            }
        }
    }
}
