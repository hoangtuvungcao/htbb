package game.model;

import game.model.arrow.SubArrow;
import game.render.Res;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;

public class Effect {
    public static final byte TYPE_ICE = 10;
    public static final byte TYPE_LIGHT = 11;
    public static final byte TYPE_POISON = 12;
    public static final byte[][] FRAME = {new byte[2], new byte[]{17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}, new byte[]{0, 1, 2, 3, 4, 5, 6, 7}, new byte[1], new byte[]{0, 1, 2, 3, 4, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4}, new byte[]{0, 1}, new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, new byte[]{0, 1, 1}, new byte[]{5, 5, 5, 0, 1, 2, 3, 4, 5, 6, 7, 8}, new byte[1], new byte[]{0, 1}, new byte[]{0, 1}, new byte[]{0, 0, 1, 1}, new byte[]{0, 1, 2}, new byte[]{0, 0, 0, 1, 1, 1, 2, 2, 2}, new byte[]{0, 0, 1, 1, 2, 2}, new byte[]{0, 0, 1, 1, 2, 2, 3, 3}, new byte[]{0, 1, 2}, new byte[]{0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2, 3, 4}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}, new byte[]{0, 0, 0, 1, 1, 1, 2, 2, 2}, new byte[1], new byte[]{0, 0, 1, 1, 2, 2, 3, 3}, new byte[]{0, 0, 1, 1, 2, 2, 3, 3}, new byte[]{0, 0, 0, 1, 1, 1}, new byte[]{0, 0, 1, 1, 2, 2, 3, 3, 4, 4}, new byte[]{0, 1, 2, 3, 4}, new byte[]{0, 1, 2, 3, 4}, new byte[]{0, 1, 2, 3, 4}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2, 3}, new byte[]{0, 0, 0, 1, 1, 1, 2, 2, 2}, new byte[]{0, 1, 2}, new byte[]{0, 0, 1, 1, 2, 2, 3, 3}, new byte[]{0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2}, new byte[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0}, new byte[]{0, 1, 2, 0, 1, 2, 0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[1], new byte[1], new byte[]{0, 1, 2, 3, 4, 5}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2, 3}, new byte[1]};
    public static final byte[] WIDTH = {52, 11, 12, 41, 14, 32, 12, 32, 10, 32, 16, 32, 32, 58, 64, 17, 31, 22, 32, 25, 25, 25, 21, 25, 24, 21, 21, 21, 32, 11, 40, 0, 12, 14, 24, 24, 25, 24, 27, 27, 39, 27, 40, 32, 42, 45, 32, 42, 32, 16, 35, 38, 53, 88, 55, 50, 40, 42, 32, 50, 10, 44, 50, 14, 7, 40};
    public static final byte[] HEIGHT = {18, 11, 12, 38, 14, 32, 12, 32, 10, 32, 100, 32, 32, 38, 46, 36, 32, 22, 32, 14, 17, 15, 34, 27, 35, 34, 34, 34, 31, 11, 31, 0, 13, 14, 15, 22, 16, 23, 27, 27, 25, 28, 76, 28, 41, 46, 32, 41, 32, 16, 36, 49, 28, 60, 55, 51, 76, 41, 32, 26, 10, 44, 50, 15, 7, 41};
    public int x;
    public int y;
    public int w;
    public int h;
    public short type;
    int f;
    public boolean wantDetroy;
    public LiveActor myChar;
    public byte loop;
    public byte yadd;
    public SubEff subEff;
    public SubArrow subArr;

    public static byte getWidth(int id) {
        return WIDTH[id];
    }

    public static byte getHeight(int id) {
        return HEIGHT[id];
    }

    public Effect(int s) {
        this.loop = (byte) 0;
        this.subEff = null;
        this.subArr = null;
    }

    public Effect() {
        this.loop = (byte) 0;
        this.subEff = null;
        this.subArr = null;
    }

    public Effect(int x, int y, int type) {
        this.loop = (byte) 0;
        this.subEff = null;
        this.subArr = null;
        this.x = x;
        this.y = y;
        this.type = (short) type;
        this.w = WIDTH[type];
        this.h = HEIGHT[type];
        this.f = -1;
    }

    public void paint(Graphics g) {
        if (this.type < 0) {
            return;
        }
        if (this.f == -1) {
            this.f = 0;
        }
        Res.paintImgEff(g, this.type, 0, FRAME[this.type][this.f] * this.h, this.w, this.h, this.x, this.y, 3);
    }

    public void update() {
        if (this.myChar != null) {
            this.x = this.myChar.x;
            this.y = this.myChar.y + this.yadd;
        }
        this.f++;
        if (this.f >= FRAME[this.type].length) {
            this.f = 0;
            if (this.loop == 0) {
                this.wantDetroy = true;
                if (this.subEff != null) {
                    Effect ef = GameScr.creatEff(this.subEff.ac, this.subEff.ac.x, this.subEff.ac.y, this.subEff.idEff);
                    EffectManager.hiEffects.addElement(ef);
                    ef.subArr = new SubArrow();
                    ef.subArr.from = this.subEff.ac;
                    ef.subArr.to = this.subEff.target;
                    ef.subArr.power = this.subEff.power;
                    ef.subArr.hp = this.subEff.hp;
                    ef.subArr.imgIndex = this.subEff.indeximg;
                    ef.subArr.ideff = this.subEff.eff;
                    this.subEff = null;
                }
                if (this.subArr != null) {
                    for (int i = 0; i < this.subArr.to.size(); i++) {
                        LiveActor ac = (LiveActor) this.subArr.to.elementAt(i);
                        int dam = Integer.parseInt((String) this.subArr.power.elementAt(i));
                        GameScr.createNewSkill(this.subArr.from, ac, dam, (byte) this.subArr.ideff, this.subArr.imgIndex);
                        ac.hp = Integer.parseInt((String) this.subArr.hp.elementAt(i));
                    }
                    this.subArr = null;
                    return;
                }
                return;
            }
            this.loop = (byte) (this.loop - 1);
        }
    }
}
