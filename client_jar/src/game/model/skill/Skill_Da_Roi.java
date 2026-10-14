package game.model.skill;

import game.model.EffBack;
import game.model.Effect;
import game.model.EffectManager;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class Skill_Da_Roi extends Effect {
    private short angle;
    private short xGoc;
    private short yGoc;
    private short distant;
    private short angLimit;
    private short xFrom;
    private short yFrom;
    private short xTo;
    private short yTo;
    private short angHuong;
    private short dir;
    private short typeSkill;
    private int hpLost;
    public Vector list;
    int xCu;
    int yCu;
    private short v = 10;
    int[] index = {0, 2, 4};

    public Skill_Da_Roi(int xTo, int yTo, int x, int y, int hpLost) {
        int an;
        this.hpLost = hpLost;
        if (xTo < x) {
            this.dir = (short) 1;
        } else {
            this.dir = (short) -1;
        }
        this.xTo = (short) xTo;
        this.yTo = (short) yTo;
        short s = (short) x;
        this.xFrom = s;
        this.x = s;
        short s2 = (short) y;
        this.yFrom = s2;
        this.y = s2;
        this.angle = (short) Util.angle(xTo - x, -(yTo - y));
        this.angHuong = this.angle;
        int dis = Util.distance(xTo, yTo, x, y);
        int xa = ((dis / 2) * Util.cos(Util.fixangle(this.angle))) >> 10;
        int ya = (-((dis / 2) * Util.sin(Util.fixangle(this.angle)))) >> 10;
        int ang = Util.fixangle(this.angle + (90 * this.dir));
        int dd = dis / 3;
        if (this.angHuong < 180) {
            an = Util.abs(this.angHuong - 90) / 2;
        } else {
            an = Util.abs(this.angHuong - 270) / 2;
        }
        int dd2 = dd + (45 - an);
        int xb = (dd2 * Util.cos(Util.fixangle(ang))) >> 10;
        int yb = (-(dd2 * Util.sin(Util.fixangle(ang)))) >> 10;
        this.xGoc = (short) (x + xa + xb);
        this.yGoc = (short) (y + ya + yb);
        this.distant = (short) Util.distance(this.xGoc, this.yGoc, x, y);
        this.angle = (short) Util.angle(x - this.xGoc, -(y - this.yGoc));
        this.angLimit = (short) Util.angle(xTo - this.xGoc, -(yTo - this.yGoc));
    }

    public void setTypeSkill(byte type) {
        this.typeSkill = type;
    }

    @Override
    public void update() {
        this.angle = (short) Util.fixangle(this.angle + (this.v * this.dir));
        int xa = (this.distant * Util.cos(Util.fixangle(this.angle))) >> 10;
        int ya = (-(this.distant * Util.sin(Util.fixangle(this.angle)))) >> 10;
        this.xCu = this.x;
        this.yCu = this.y;
        if (this.type != 1) {
            this.x = this.xGoc + xa;
        }
        this.y = this.yGoc + ya;
        int dis = Util.distance(this.xCu, this.yCu, this.x, this.y);
        if (dis > 10) {
            this.v = (short) (10 - ((dis % 10) / 2));
        }
        if (Util.abs(this.angle - this.angLimit) < this.v) {
            if (this.type != 1) {
                if (this.hpLost != 0) {
                    Canvas.gameScr.startFlyText(new StringBuffer("-").append(this.hpLost).toString(), 0, this.xTo + 0, this.yTo - 45, 1, -2);
                } else {
                    Canvas.gameScr.startFlyText("MISS", 0, this.xTo + 0, this.yTo - 45, 1, -2);
                }
            }
            EffectManager.addHiEffect(this.xTo, this.yTo - 10, 12);
            if (this.type != 1) {
                EffBack e = new EffBack(this.xTo, this.yTo, 28, 3, 32, 31, false, false);
                EffectManager.lowEffects.addElement(e);
            } else {
                EffBack e2 = new EffBack(this.xTo, this.yTo, 28, 3, 32, 31, false, true);
                EffectManager.hiEffects.addElement(e2);
                EffectManager.addHiEffect(this.xTo, this.yTo, 48);
            }
            EffectManager.hiEffects.removeElement(this);
            if (this.type == 1) {
                EffectManager.lowEffects.addElement(new Skill_Cau_Lua(this.xTo, this.yTo, this.list));
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        int dis = Util.distance(this.xFrom, this.yFrom, this.x, this.y);
        int xa = (dis * Util.cos(Util.fixangle(this.angHuong))) >> 10;
        int ya = (-(dis * Util.sin(Util.fixangle(this.angHuong)))) >> 10;
        g.drawImage(Res.imgShadow, this.xFrom + xa, this.yFrom + ya, 3);
        if (this.type == 1) {
            g.drawImage(Res.imgEffNew, this.x, this.y, 3);
            return;
        }
        for (int i = 0; i < 3; i++) {
            Res.paintImgEff(g, 4, 0, this.index[i] * 14, 14, 14, (this.x + Res.rnd(10)) - 5, (this.y + Res.rnd(10)) - 10, 3);
            int[] iArr = this.index;
            int i2 = i;
            iArr[i2] = iArr[i2] + 1;
            if (this.index[i] >= 5) {
                this.index[i] = 0;
            }
        }
    }
}
