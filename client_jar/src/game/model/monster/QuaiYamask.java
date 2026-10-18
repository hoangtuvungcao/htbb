package game.model.monster;

import effect.DynamicEffect;
import game.model.LiveActor;
import game.model.Monster;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class QuaiYamask extends Monster {
    private int nhay;
    private int angle;
    private int indexDoc;
    private boolean isFire;
    public static byte[] clip = {0, 1, 2, 2};
    private int vNhay = 0;
    private int dirNhay = 1;
    private int timeNhay = 0;
    private boolean isNhay = false;

    public QuaiYamask() {
        this.z = 0;
    }

    @Override
    public void paint(Graphics g) {
        // pokemon: template da la Pokemon (lenh -91) hoac do vat PMD: ve bang PokeBody, bo renderer boss cu
        if (paintPokemon(g)) {
            return;
        }
        if (!this.isFreeze) {
            g.drawImage(Res.imgShadow, this.x, this.y, 3);
            if (this.dynamicEffBottom != null) {
                for (int ii = 0; ii < this.dynamicEffBottom.size(); ii++) {
                    ((DynamicEffect) this.dynamicEffBottom.elementAt(ii)).paint(g, this.x, this.y);
                }
            }
            if (Res.monsterTemplates[this.monster_type].image != null) {
                g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, clip[this.dir] * 31, 20, 31, this.dir == 3 ? 2 : 0, this.x + this.vangx, (this.y - this.nhay) + this.z + this.vangy + this.dy, 33);
                if (this.isFire) {
                    int xa = ((10 * (this.indexDoc / 3)) * Util.cos(this.angle)) >> 10;
                    int ya = (-((10 * (this.indexDoc / 3)) * Util.sin(this.angle))) >> 10;
                    int xDoc = this.x + xa;
                    int ydoc = (this.y + ya) - 25;
                    g.drawRegion(Res.loadImgEff(33), 0, ((11 - this.indexDoc) / 3) * 14, 14, 14, 0, xDoc, ydoc, 3);
                }
                if (this.dynamicEffTop != null) {
                    for (int ii2 = 0; ii2 < this.dynamicEffTop.size(); ii2++) {
                        ((DynamicEffect) this.dynamicEffTop.elementAt(ii2)).paint(g, this.x, this.y);
                    }
                }
            }
        }
        super.paintEffSkill(g);
    }

    @Override
    public void update() {
        super.update();
        if (this.timeNhay == 0) {
            this.nhay += this.vNhay;
            this.vNhay += this.dirNhay * 2;
            if (Util.abs(this.vNhay) > 4) {
                this.dirNhay *= -1;
            }
            if (this.vNhay == 0 && this.dirNhay == 1) {
                this.timeNhay = 8;
            }
        }
        if (this.timeNhay > 0 && !this.isNhay) {
            this.timeNhay--;
        }
        if (this.x != this.xTo || this.y != this.yTo) {
            this.isNhay = false;
        } else {
            this.isNhay = true;
        }
        if (!this.isFreeze && this.isFire) {
            this.indexDoc++;
            if (this.indexDoc >= 12) {
                this.isFire = false;
                this.indexDoc = 0;
            }
        }
    }

    @Override
    public void move() {
        if (FuntioncanMove() && this.timeNhay == 0) {
            super.move();
        }
    }

    @Override
    public void startAttack(Vector list, byte skillID) {
        if (this.isFire) {
            return;
        }
        LiveActor live = (LiveActor) list.elementAt(0);
        this.isFire = true;
        this.indexDoc = 0;
        this.angle = Util.angle(live.x - this.x, -(live.y - this.y));
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                LiveActor lv = (LiveActor) list.elementAt(i);
                if (lv != null) {
                    int hpL = lv.hp;
                    lv.doublejump();
                    Canvas.gameScr.startFlyText(new StringBuffer("-").append(hpL).toString(), 0, lv.x + 0, (lv.y + lv.dy) - 15, 1, -2);
                }
            }
        }
    }
}
