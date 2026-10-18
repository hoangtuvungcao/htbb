package game.model.arrow;

import game.core.j2me.Graphics;
import game.model.EffectLeg;
import game.model.EffectManager;
import game.model.EffectTarget;
import game.model.LiveActor;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;

public class WeaponsFire extends IArrow {
    int angle;
    int dx;
    int dy;
    EffectLeg effleg;
    int frame;
    int frameEff;
    int himg;
    boolean isStartEff;
    LiveActor live_To;
    int saveDir;
    int transform;
    int type;
    int va;
    int vx;
    int vy;
    int x;
    int xTo;
    int y;
    int yTo;

    @Override
    public void onArrowTouchTarget() {
    }

    @Override
    public void set(int i, int i2, int i3, int i4, byte b, LiveActor liveActor, LiveActor liveActor2) {
    }

    @Override
    public void setAngle(int i) {
    }

    public WeaponsFire(LiveActor liveActor, LiveActor liveActor2, int i) {
        this.type = i;
        this.live_To = liveActor2;
        this.x = liveActor.x;
        this.y = liveActor.y - 54;
        this.xTo = liveActor2.x;
        this.yTo = liveActor2.y;
        int iAngle = Util.angle(this.x - liveActor2.x, (liveActor2.y - (liveActor2.height >> 1)) - this.y);
        this.angle = iAngle;
        this.va = 24;
        this.vx = (24 * Util.cos(iAngle)) >> 10;
        this.vy = (this.va * Util.sin(this.angle)) >> 10;
        this.himg = liveActor2.height;
        if (i < 6) {
            this.effleg = new EffectLeg(this.xTo, this.yTo, i == 3 ? 1 : 0, 0, 0, 0, false, false);
            EffectManager.lowEffects.addElement(this.effleg);
        }
        this.y = liveActor.y - 31;
        if (liveActor.dir == 2) {
            this.x = liveActor.x - 30;
        } else if (liveActor.dir == 3) {
            this.x = liveActor.x + 30;
        }
        int iFindDirIndexFromAngle = Arrow.findDirIndexFromAngle(Util.angle(liveActor2.x - this.x, -((liveActor2.y + (liveActor2.height >> 1)) - this.y)));
        this.frame = Arrow.FRAME[iFindDirIndexFromAngle];
        this.transform = Arrow.TRANSFORM[iFindDirIndexFromAngle];
    }

    @Override
    public void paint(Graphics graphics) {
        int i;
        if (!this.isStartEff) {
            if (Res.getImgArrow(this.type) == null || (i = this.type) == 6 || i == 7) {
                return;
            }
            graphics.drawRegion(Res.imgArrow[this.type], 0, this.frame * Arrow.ARROWSIZE[1][this.type], Arrow.ARROWSIZE[0][this.type], Arrow.ARROWSIZE[1][this.type], this.transform, this.x, this.y, 3);
            return;
        }
        if (Res.imgEffect[38] != null) {
            graphics.drawRegion(Res.imgEffect[38], 0, this.frameEff * 27, 27, 27, 0, this.xTo, this.yTo, 33);
        }
        if (GCanvas.gameTick % 2 == 0) {
            int i2 = this.frameEff + 1;
            this.frameEff = i2;
            if (i2 > 4) {
                if (GCanvas.gameScr.arrowsDown.contains(this)) {
                    GCanvas.gameScr.arrowsDown.removeElement(this);
                }
                GameScr gameScr = GCanvas.gameScr;
                if (GameScr.arrowsUp.contains(this)) {
                    GameScr gameScr2 = GCanvas.gameScr;
                    GameScr.arrowsUp.removeElement(this);
                }
                this.isStartEff = false;
                this.frameEff = 0;
            }
        }
    }

    @Override
    public void update() {
        LiveActor liveActor = this.live_To;
        if (liveActor != null) {
            this.xTo = liveActor.x;
            this.yTo = this.live_To.y;
        }
        int i = this.xTo - this.x;
        this.dx = i;
        int i2 = (this.yTo - (this.himg >> 1)) - this.y;
        this.dy = i2;
        int iAngle = Util.angle(i, i2);
        this.angle = iAngle;
        this.vx = (this.va * Util.cos(iAngle)) >> 10;
        int iSin = (this.va * Util.sin(this.angle)) >> 10;
        this.vy = iSin;
        int i3 = this.x + this.vx;
        this.x = i3;
        int i4 = this.y + iSin;
        this.y = i4;
        if (!this.isStartEff) {
            int i5 = this.type;
            if (i5 < 6) {
                EffectManager.addHiEffect(i3, i4, i5 == 3 ? 8 : 6);
            } else if (i5 == 6) {
                EffectManager.addHiEffect(i3, i4, 46);
            }
        }
        int i6 = this.x;
        int i7 = i6 - 20;
        int i8 = i6 + 20;
        int i9 = this.xTo;
        int i10 = this.himg;
        int i11 = i9 - (i10 / 2);
        int i12 = i9 + (i10 / 2);
        int i13 = this.y;
        int i14 = i13 - 20;
        int i15 = i13 + 20;
        int i16 = this.yTo;
        if (Res.isDestroy(i7, i8, i11, i12, i14, i15, i16 - (i10 / 2), i16 + (i10 / 2))) {
            if (!this.isStartEff) {
                int i17 = this.type;
                if (i17 == 3) {
                    creatMyEff(this.live_To, 0, 0, 18);
                    creatMyEff(this.live_To, 1, 0, 24);
                    creatMyEff(this.live_To, 2, 0, 28);
                    creatMyEff(this.live_To, 3, 0, 32);
                } else if (i17 < 6) {
                    EffectManager.addHiEffect(this.xTo, this.yTo - 10, 30);
                    if (GCanvas.gameScr.arrowsDown.contains(this)) {
                        GCanvas.gameScr.arrowsDown.removeElement(this);
                    }
                    GameScr gameScr = GCanvas.gameScr;
                    if (GameScr.arrowsUp.contains(this)) {
                        GameScr gameScr2 = GCanvas.gameScr;
                        GameScr.arrowsUp.removeElement(this);
                    }
                    this.live_To.doublejump();
                    creatMyEff(this.live_To, 0, 1, 40);
                    creatMyEff(this.live_To, 1, 1, 40);
                    creatMyEff(this.live_To, 2, 1, 40);
                    creatMyEff(this.live_To, 3, 1, 40);
                } else if (i17 == 6) {
                    if (GCanvas.gameScr.arrowsDown.contains(this)) {
                        GCanvas.gameScr.arrowsDown.removeElement(this);
                    }
                    GameScr gameScr3 = GCanvas.gameScr;
                    if (GameScr.arrowsUp.contains(this)) {
                        GameScr gameScr4 = GCanvas.gameScr;
                        GameScr.arrowsUp.removeElement(this);
                    }
                    this.live_To.doublejump();
                }
            }
            this.isStartEff = true;
        }
    }

    public void creatMyEff(LiveActor liveActor, int i, int i2, int i3) {
        EffectManager.hiEffects.addElement(new EffectTarget(liveActor, i, i2, i3));
    }
}
