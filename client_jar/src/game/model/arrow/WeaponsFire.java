package game.model.arrow;

import game.model.EffectLeg;
import game.model.EffectManager;
import game.model.EffectTarget;
import game.model.LiveActor;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;

public class WeaponsFire extends IArrow {
    int va = 24;
    int vx;
    int vy;
    int angle;
    int x;
    int y;
    int type;
    int dx;
    int dy;
    int xTo;
    int yTo;
    int himg;
    LiveActor live_To;
    int frame;
    int transform;
    EffectLeg effleg;
    int frameEff;
    int saveDir;
    boolean isStartEff;

    public WeaponsFire(LiveActor live_From, LiveActor live_To, int type) {
        this.type = type;
        this.live_To = live_To;
        this.x = live_From.x;
        this.y = live_From.y - 54;
        this.xTo = live_To.x;
        this.yTo = live_To.y;
        this.angle = Util.angle(this.x - live_To.x, (live_To.y - (live_To.height >> 1)) - this.y);
        this.vx = (this.va * Util.cos(this.angle)) >> 10;
        this.vy = (this.va * Util.sin(this.angle)) >> 10;
        this.himg = live_To.height;
        if (type < 6) {
            this.effleg = new EffectLeg(this.xTo, this.yTo, type == 3 ? 1 : 0, 0, 0, 0, false, false);
            EffectManager.lowEffects.addElement(this.effleg);
        }
        this.y = live_From.y - 31;
        if (live_From.dir == 2) {
            this.x = live_From.x - 30;
        } else if (live_From.dir == 3) {
            this.x = live_From.x + 30;
        }
        int d = Arrow.findDirIndexFromAngle(Util.angle(live_To.x - this.x, -((live_To.y + (live_To.height >> 1)) - this.y)));
        this.frame = Arrow.FRAME[d];
        this.transform = Arrow.TRANSFORM[d];
    }

    @Override
    public void onArrowTouchTarget() {
    }

    @Override
    public void paint(Graphics g) {
        if (!this.isStartEff) {
            if (Res.getImgArrow(this.type) != null && this.type != 6 && this.type != 7) {
                g.drawRegion(Res.getImgArrow(this.type), 0, this.frame * Arrow.ARROWSIZE[1][this.type], Arrow.ARROWSIZE[0][this.type], Arrow.ARROWSIZE[1][this.type], this.transform, this.x, this.y, 3);
                return;
            }
            return;
        }
        if (Res.getImageEffect(38) != null) {
            g.drawRegion(Res.getImageEffect(38), 0, this.frameEff * 27, 27, 27, 0, this.xTo, this.yTo, 33);
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
        if (this.live_To != null) {
            this.xTo = this.live_To.x;
            this.yTo = this.live_To.y;
        }
        this.dx = this.xTo - this.x;
        this.dy = (this.yTo - (this.himg >> 1)) - this.y;
        this.angle = Util.angle(this.dx, this.dy);
        this.vx = (this.va * Util.cos(this.angle)) >> 10;
        this.vy = (this.va * Util.sin(this.angle)) >> 10;
        this.x += this.vx;
        this.y += this.vy;
        if (!this.isStartEff) {
            if (this.type < 6) {
                EffectManager.addHiEffect(this.x, this.y, this.type == 3 ? 8 : 6);
            } else if (this.type == 6) {
                EffectManager.addHiEffect(this.x, this.y, 46);
            }
        }
        if (Res.isDestroy(this.x - 20, this.x + 20, this.xTo - (this.himg / 2), this.xTo + (this.himg / 2), this.y - 20, this.y + 20, this.yTo - (this.himg / 2), this.yTo + (this.himg / 2))) {
            if (!this.isStartEff) {
                if (this.type == 3) {
                    creatMyEff(this.live_To, 0, 0, 18);
                    creatMyEff(this.live_To, 1, 0, 24);
                    creatMyEff(this.live_To, 2, 0, 28);
                    creatMyEff(this.live_To, 3, 0, 32);
                } else if (this.type < 6) {
                    EffectManager.addHiEffect(this.xTo, this.yTo - 10, 30);
                    if (Canvas.gameScr.arrowsDown.contains(this)) {
                        Canvas.gameScr.arrowsDown.removeElement(this);
                    }
                    if (GameScr.arrowsUp.contains(this)) {
                        GameScr.arrowsUp.removeElement(this);
                    }
                    this.live_To.doublejump();
                    creatMyEff(this.live_To, 0, 1, 40);
                    creatMyEff(this.live_To, 1, 1, 40);
                    creatMyEff(this.live_To, 2, 1, 40);
                    creatMyEff(this.live_To, 3, 1, 40);
                } else if (this.type == 6) {
                    if (Canvas.gameScr.arrowsDown.contains(this)) {
                        Canvas.gameScr.arrowsDown.removeElement(this);
                    }
                    if (GameScr.arrowsUp.contains(this)) {
                        GameScr.arrowsUp.removeElement(this);
                    }
                    this.live_To.doublejump();
                }
            }
            this.isStartEff = true;
        }
        if (this.isStartEff && Canvas.gameTick % 2 == 0) {
            this.frameEff++;
            if (this.frameEff > 4) {
                if (Canvas.gameScr.arrowsDown.contains(this)) {
                    Canvas.gameScr.arrowsDown.removeElement(this);
                }
                if (GameScr.arrowsUp.contains(this)) {
                    GameScr.arrowsUp.removeElement(this);
                }
                this.isStartEff = false;
                this.frameEff = 0;
            }
        }
    }

    public void creatMyEff(LiveActor l_To, int dir, int type, int va) {
        EffectTarget ef = new EffectTarget(l_To, dir, type, va);
        EffectManager.hiEffects.addElement(ef);
    }
}
