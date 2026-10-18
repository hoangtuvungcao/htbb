package game.model.arrow;

import game.core.j2me.Graphics;
import game.model.Cout;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;

public class WeaponsAngleBoss extends IArrow {
    public int[] FLIP = {0, 2, 1, 3, 7, 4};
    public int angle;
    public int angleTo;
    int index;
    public int power;
    public int type;
    public int typesize;
    public int upPower;
    public int x;
    public int xbg;
    public int y;
    public int ybg;

    public int getFlip(int i) {
        if (i == 30 || i == 60) {
            return 2;
        }
        if (i == 90) {
            return 5;
        }
        if (i == 120 || i == 150) {
            return 3;
        }
        if (i == 180 || i == 210 || i == 240) {
            return 1;
        }
        return i != 270 ? 0 : 4;
    }

    public int getIdFrame(int i) {
        if (i == 30) {
            return 1;
        }
        if (i == 60 || i == 120) {
            return 2;
        }
        if (i == 150 || i == 210) {
            return 1;
        }
        if (i == 240 || i == 300) {
            return 2;
        }
        return i != 330 ? 0 : 1;
    }

    @Override
    public void onArrowTouchTarget() {
    }

    @Override
    public void set(int i, int i2, int i3, int i4, byte b, LiveActor liveActor, LiveActor liveActor2) {
    }

    @Override
    public void setAngle(int i) {
    }

    public WeaponsAngleBoss(int i, int i2, LiveActor liveActor, LiveActor liveActor2, int i3, int i4, int i5, int i6, int i7, boolean z) {
        this.angle = i;
        this.power = i2;
        if (liveActor != null) {
            this.xbg = liveActor.x - i4;
            this.ybg = liveActor.y - i5;
        } else {
            this.xbg = i6;
            this.ybg = i7;
        }
        this.type = i3;
        this.typesize = this.typesize;
        if (z && liveActor2 != null) {
            liveActor2.jump();
            EffectManager.addHiEffect(liveActor2.x, (liveActor2.y + liveActor2.dy) - 10, 11);
            GCanvas.gameScr.startFlyText("-" + liveActor2.damAttack, 0, liveActor2.x, (liveActor2.y + liveActor2.dy) - 15, 1, -2);
        }
        this.angleTo = i;
    }

    @Override
    public void paint(Graphics graphics) {
        try {
            if (this.type != 0) {
                if (Res.imgEffect[34] == null) {
                    Res.load.loadImgEffect(34);
                } else {
                    graphics.drawRegion(Res.imgEffect[34], 0, this.index * 15, 24, 15, 0, this.x, this.y, 3);
                }
            }
        } catch (Exception e) {
            Cout.println("loi vu khi xoia " + e.toString());
        }
    }

    @Override
    public void update() {
        this.x = this.xbg + ((Util.cos(this.angle) * this.power) / 1024);
        int iSin = this.ybg + ((Util.sin(this.angle) * this.power) / 1024);
        this.y = iSin;
        if (this.type == 0) {
            int i = this.angleTo;
            int i2 = i + 30;
            this.angleTo = i2;
            if (i2 > 360) {
                this.angleTo = i - 330;
            }
            int i3 = this.angle;
            if (i3 != this.angleTo) {
                int i4 = i3 + 10;
                this.angle = i4;
                if (i4 > 360) {
                    this.angle = i3 - 350;
                }
            }
            int i5 = this.upPower;
            if (i5 < 15) {
                this.upPower = i5 + 2;
            }
            EffectManager.addHiEffect(this.x, iSin, 33);
        } else {
            int i6 = this.upPower;
            if (i6 < 24) {
                this.upPower = i6 + 8;
            }
            if (GCanvas.gameTick % 4 == 0) {
                int i7 = this.index;
                if (i7 < 1) {
                    this.index = i7 + 1;
                } else {
                    this.index = 0;
                }
            }
        }
        this.power += this.upPower;
        int i8 = this.x;
        GameScr gameScr = GCanvas.gameScr;
        if (i8 >= GameScr.cmx - (GCanvas.w / 2)) {
            int i9 = this.x;
            GameScr gameScr2 = GCanvas.gameScr;
            if (i9 <= GameScr.cmx + GCanvas.w + (GCanvas.w / 2)) {
                int i10 = this.y;
                GameScr gameScr3 = GCanvas.gameScr;
                if (i10 >= GameScr.cmy - (GCanvas.h / 2)) {
                    int i11 = this.y;
                    GameScr gameScr4 = GCanvas.gameScr;
                    if (i11 <= GameScr.cmy + GCanvas.h + (GCanvas.h / 2)) {
                        return;
                    }
                }
            }
        }
        if (GCanvas.gameScr.arrowsDown.contains(this)) {
            GCanvas.gameScr.arrowsDown.removeElement(this);
        }
        GameScr gameScr5 = GCanvas.gameScr;
        if (GameScr.arrowsUp.contains(this)) {
            GameScr gameScr6 = GCanvas.gameScr;
            GameScr.arrowsUp.removeElement(this);
        }
    }
}
