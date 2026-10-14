package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import javax.microedition.lcdui.Graphics;

public class EffectTarget extends Effect {
    int xTo;
    int yTo;
    int va;
    int vx;
    int vy;
    int angle0;
    int dir;
    int type;
    LiveActor l_To;
    int dx;
    int dy;
    int himg;
    int flip;

    public EffectTarget(LiveActor l_To, int dir, int type, int va) {
        this.l_To = l_To;
        this.dir = dir;
        if (dir == 0) {
            this.x = l_To.x - (type == 0 ? (short) 100 : (short) 20);
            this.y = (l_To.y - 100) - (type == 0 ? 0 : 100);
            this.xTo = l_To.x + (type == 0 ? (short) 0 : (short) -20);
        } else if (dir == 1) {
            this.x = l_To.x + (type == 0 ? (short) 100 : (short) -10);
            this.y = (l_To.y - 100) - (type == 0 ? 0 : 100);
            this.xTo = l_To.x + (type == 0 ? (short) 0 : (short) -10);
        } else if (dir == 2) {
            this.x = l_To.x + (type == 0 ? (short) 100 : (short) 10);
            this.y = l_To.y + (type == 0 ? (short) 100 : (short) -200);
            this.xTo = l_To.x + (type == 0 ? (short) 0 : (short) 10);
        } else {
            this.x = l_To.x - (type == 0 ? (short) 100 : (short) 20);
            this.y = l_To.y + (type == 0 ? (short) 100 : (short) -200);
            this.xTo = l_To.x + (type == 0 ? (short) 0 : (short) 20);
        }
        this.yTo = l_To.y;
        this.type = type;
        this.angle0 = Util.angle(this.x - l_To.x, (l_To.y - (l_To.height >> 1)) - this.y);
        this.va = va;
        this.vx = (va * Util.cos(this.angle0)) >> 10;
        this.vy = (va * Util.sin(this.angle0)) >> 10;
        this.himg = l_To.height;
    }

    @Override
    public void update() {
        if (this.l_To != null) {
            if (this.dir == 0) {
                this.xTo = this.l_To.x + (this.type == 0 ? (short) -4 : (short) -20);
                this.yTo = this.l_To.y - (this.type == 15 ? (short) 0 : (short) 0);
            } else if (this.dir == 1) {
                this.xTo = this.l_To.x + (this.type == 0 ? (short) 8 : (short) -10);
                this.yTo = this.l_To.y - (this.type == 10 ? (short) 0 : (short) 0);
            } else if (this.dir == 2) {
                this.xTo = this.l_To.x + (this.type == 0 ? (short) 4 : (short) 10);
                this.yTo = this.l_To.y + (this.type == 15 ? (short) 0 : (short) 0);
            } else {
                this.xTo = this.l_To.x + (this.type == 0 ? (short) -8 : (short) 20);
                this.yTo = this.l_To.y + (this.type == 20 ? (short) 0 : (short) 0);
            }
        }
        this.dx = this.xTo - this.x;
        this.dy = (this.yTo - (this.himg >> 1)) - this.y;
        this.angle0 = Util.angle(this.dx, this.dy);
        this.vx = (this.va * Util.cos(this.angle0)) >> 10;
        this.vy = (this.va * Util.sin(this.angle0)) >> 10;
        this.x += this.vx;
        this.y += this.vy;
        EffectManager.addLowEffect(this.x, this.y, this.type == 0 ? 32 : 29);
        if (Res.isDestroy(this.x - 20, this.x + 20, this.xTo - (this.himg / 2), this.xTo + (this.himg / 2), this.y - 20, this.y + 20, this.yTo - (this.himg / 2), this.yTo + (this.himg / 2))) {
            EffectManager.addHiEffect(this.xTo, this.yTo - 10, this.type == 0 ? 38 : 30);
            EffectManager.hiEffects.removeElement(this);
            if (this.l_To != null) {
                int hpL = this.l_To.damAttack;
                this.l_To.doublejump();
                Canvas.gameScr.startFlyText(new StringBuffer("-").append(hpL).toString(), 0, this.l_To.x + 0, (this.l_To.y + this.l_To.dy) - 15, 1, -2);
            }
        }
        if (this.type != 1) {
            if (this.dir == 0) {
                this.flip = 0;
                return;
            }
            if (this.dir == 1) {
                this.flip = 2;
            } else if (this.dir == 3) {
                this.flip = 1;
            } else {
                this.flip = 7;
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        if (this.type == 1) {
            if (Res.getImgArrow(4) != null) {
                g.drawRegion(Res.getImgArrow(4), 0, 0, 24, 24, 5, this.x, this.y, 3);
            }
        } else if (Res.getImageEffect(31) != null) {
            g.drawRegion(Res.getImageEffect(31), 0, 0, 20, 20, this.flip, this.x, this.y, 3);
        }
    }
}
