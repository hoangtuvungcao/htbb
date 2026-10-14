package game.model.skill;

import game.core.j2me.Graphics;
import game.model.Cout;
import game.model.EffBack;
import game.model.Effect;
import game.model.EffectManager;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;

public class Skill_Da_Roi extends Effect {
    private short angHuong;
    private short angLimit;
    private short angle;
    private short dir;
    private short distant;
    private int hpLost;
    public Vector list;
    private short typeSkill;
    int xCu;
    private short xFrom;
    private short xGoc;
    private short xTo;
    int yCu;
    private short yFrom;
    private short yGoc;
    private short yTo;
    private short v = 10;
    int[] index = {0, 2, 4};

    public Skill_Da_Roi(int i, int i2, int i3, int i4, int i5) {
        int iAbs;
        this.hpLost = i5;
        if (i < i3) {
            this.dir = (short) 1;
        } else {
            this.dir = (short) -1;
        }
        this.xTo = (short) i;
        this.yTo = (short) i2;
        short s = (short) i3;
        this.xFrom = s;
        this.x = s;
        short s2 = (short) i4;
        this.yFrom = s2;
        this.y = s2;
        short sAngle = (short) Util.angle(i - i3, -(i2 - i4));
        this.angle = sAngle;
        this.angHuong = sAngle;
        int iDistance = Util.distance(i, i2, i3, i4);
        int i6 = iDistance / 2;
        int iCos = (Util.cos(Util.fixangle(this.angle)) * i6) >> 10;
        int i7 = (-(i6 * Util.sin(Util.fixangle(this.angle)))) >> 10;
        int iFixangle = Util.fixangle(this.angle + (this.dir * 90));
        int i8 = iDistance / 3;
        short s3 = this.angHuong;
        if (s3 < 180) {
            iAbs = Util.abs(s3 - 90) / 2;
        } else {
            iAbs = Util.abs(s3 - 270) / 2;
        }
        int i9 = i8 + (45 - iAbs);
        int iCos2 = (Util.cos(Util.fixangle(iFixangle)) * i9) >> 10;
        int i10 = (-(i9 * Util.sin(Util.fixangle(iFixangle)))) >> 10;
        short s4 = (short) (iCos + i3 + iCos2);
        this.xGoc = s4;
        short s5 = (short) (i7 + i4 + i10);
        this.yGoc = s5;
        this.distant = (short) Util.distance(s4, s5, i3, i4);
        this.angle = (short) Util.angle(i3 - this.xGoc, -(i4 - this.yGoc));
        this.angLimit = (short) Util.angle(i - this.xGoc, -(i2 - this.yGoc));
    }

    public void setTypeSkill(byte b) {
        this.typeSkill = b;
    }

    @Override
    public void update() {
        try {
            short sFixangle = (short) Util.fixangle(this.angle + (this.v * this.dir));
            this.angle = sFixangle;
            int iCos = (this.distant * Util.cos(Util.fixangle(sFixangle))) >> 10;
            int i = (-(this.distant * Util.sin(Util.fixangle(this.angle)))) >> 10;
            this.xCu = this.x;
            this.yCu = this.y;
            if (this.type != 1) {
                this.x = this.xGoc + iCos;
            }
            this.y = this.yGoc + i;
            int iDistance = Util.distance(this.xCu, this.yCu, this.x, this.y);
            if (iDistance > 10) {
                this.v = (short) (10 - ((iDistance % 10) / 2));
            }
            if (Util.abs(this.angle - this.angLimit) < this.v) {
                if (this.type != 1) {
                    if (this.hpLost != 0) {
                        GCanvas.gameScr.startFlyText("-" + this.hpLost, 0, this.xTo, this.yTo - 45, 1, -2);
                    } else {
                        GCanvas.gameScr.startFlyText("MISS", 0, this.xTo, this.yTo - 45, 1, -2);
                    }
                }
                EffectManager.addHiEffect(this.xTo, this.yTo - 10, 12);
                if (this.type != 1) {
                    EffectManager.lowEffects.addElement(new EffBack(this.xTo, this.yTo, 28, 3, 32, 31, false, false));
                } else {
                    EffectManager.hiEffects.addElement(new EffBack(this.xTo, this.yTo, 28, 3, 32, 31, false, true));
                    EffectManager.addHiEffect(this.xTo, this.yTo, 48);
                }
                EffectManager.hiEffects.removeElement(this);
                if (this.type == 1) {
                    EffectManager.lowEffects.addElement(new Skill_Cau_Lua(this.xTo, this.yTo, this.list));
                }
            }
        } catch (Exception unused) {
            Cout.println("update skill da roi");
        }
    }

    @Override
    public void paint(Graphics graphics) {
        try {
            int iDistance = Util.distance(this.xFrom, this.yFrom, this.x, this.y);
            int i = 10;
            graphics.drawImage(Res.imgShadow, this.xFrom + ((Util.cos(Util.fixangle(this.angHuong)) * iDistance) >> 10), this.yFrom + ((-(iDistance * Util.sin(Util.fixangle(this.angHuong)))) >> 10), 3);
            if (this.type == 1) {
                graphics.drawImage(Res.imgEffNew, this.x, this.y, 3);
                return;
            }
            int i2 = 0;
            while (i2 < 3) {
                Res.paintImgEff(graphics, 4, 0, this.index[i2] * 14, 14, 14, (this.x + Res.rnd(i)) - 5, (this.y + Res.rnd(i)) - 10, 3);
                int[] iArr = this.index;
                int i3 = iArr[i2] + 1;
                iArr[i2] = i3;
                if (i3 >= 5) {
                    iArr[i2] = 0;
                }
                i2++;
                i = 10;
            }
        } catch (Exception unused) {
            Cout.println("paint skill da roi");
        }
    }
}
