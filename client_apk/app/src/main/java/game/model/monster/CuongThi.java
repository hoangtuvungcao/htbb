package game.model.monster;

import effect.DynamicEffect;
import game.core.j2me.Graphics;
import game.model.LiveActor;
import game.model.Monster;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;

public class CuongThi extends Monster {
    public static byte[] clip = {0, 1, 2, 2};
    private int angle;
    private int indexDoc;
    private boolean isFire;
    private int nhay;
    private int vNhay = 0;
    private int dirNhay = 1;
    private int timeNhay = 0;
    private boolean isNhay = false;

    public CuongThi() {
        this.z = 0;
    }

    @Override
    public void paint(Graphics graphics) {
        if (!this.isFreeze) {
            graphics.drawImage(Res.imgShadow, this.x, this.y, 3);
            if (this.dynamicEffBottom != null) {
                for (int i = 0; i < this.dynamicEffBottom.size(); i++) {
                    ((DynamicEffect) this.dynamicEffBottom.elementAt(i)).paint(graphics, this.x, this.y);
                }
            }
            if (Res.monsterTemplates[this.monster_type].image != null) {
                graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, clip[this.dir] * 31, 20, 31, this.dir == 3 ? 2 : 0, this.x + this.vangx, (this.y - this.nhay) + this.z + this.vangy + this.dy, Graphics.BOTTOM | Graphics.HCENTER);
            }
            if (this.isFire) {
                graphics.drawRegion(Res.loadImgEff(33), 0, ((11 - this.indexDoc) / 3) * 14, 14, 14, 0, this.x + ((((this.indexDoc / 3) * 10) * Util.cos(this.angle)) >> 10), (this.y + ((-(((this.indexDoc / 3) * 10) * Util.sin(this.angle))) >> 10)) - 25, 3);
            }
            if (this.dynamicEffTop != null) {
                for (int i2 = 0; i2 < this.dynamicEffTop.size(); i2++) {
                    ((DynamicEffect) this.dynamicEffTop.elementAt(i2)).paint(graphics, this.x, this.y);
                }
            }
        }
        super.paintEffSkill(graphics);
    }

    @Override
    public void update() {
        super.update();
        if (this.timeNhay == 0) {
            int i = this.nhay;
            int i2 = this.vNhay;
            this.nhay = i + i2;
            int i3 = i2 + (this.dirNhay * 2);
            this.vNhay = i3;
            if (Util.abs(i3) > 4) {
                this.dirNhay *= -1;
            }
            if (this.vNhay == 0 && this.dirNhay == 1) {
                this.timeNhay = 8;
            }
        }
        int i4 = this.timeNhay;
        if (i4 > 0 && !this.isNhay) {
            this.timeNhay = i4 - 1;
        }
        if (this.x != this.xTo || this.y != this.yTo) {
            this.isNhay = false;
        } else {
            this.isNhay = true;
        }
        if (this.isFire) {
            int i5 = this.indexDoc + 1;
            this.indexDoc = i5;
            if (i5 >= 12) {
                this.isFire = false;
                this.indexDoc = 0;
            }
        }
    }

    @Override
    public void move() {
        if (canMove() && this.timeNhay == 0) {
            super.move();
        }
    }

    @Override
    public void startAttack(Vector vector, byte b) {
        if (this.isFire) {
            return;
        }
        LiveActor liveActor = (LiveActor) vector.elementAt(0);
        this.isFire = true;
        this.indexDoc = 0;
        this.angle = Util.angle(liveActor.x - this.x, -(liveActor.y - this.y));
        if (vector == null || vector.size() <= 0) {
            return;
        }
        int size = vector.size();
        for (int i = 0; i < size; i++) {
            LiveActor liveActor2 = (LiveActor) vector.elementAt(i);
            if (liveActor2 != null) {
                int i2 = liveActor2.hp;
                liveActor2.doublejump();
                GCanvas.gameScr.startFlyText("-" + i2, 0, liveActor2.x, (liveActor2.y + liveActor2.dy) - 15, 1, -2);
            }
        }
    }
}
