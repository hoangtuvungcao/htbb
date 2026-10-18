package game.model;

import game.core.j2me.Graphics;
import game.model.arrow.SubArrow;
import game.render.Res;
import game.render.screen.GameScr;

public class Effect {
    public static final byte[][] FRAME;
    public static final byte TYPE_ICE = 10;
    public static final byte TYPE_LIGHT = 11;
    public static final byte TYPE_POISON = 12;
    int f;
    public int h;
    public byte loop;
    public LiveActor myChar;
    public SubArrow subArr;
    public SubEff subEff;
    public short type;
    public int w;
    public boolean wantDetroy;
    public int x;
    public int y;
    public byte yadd;
    public static final byte[] WIDTH = {52, 11, 12, 41, 14, 32, 12, 32, 10, 32, 16, 32, 32, 58, 64, 17, 31, 22, 32, 25, 25, 25, 21, 25, 24, 21, 21, 21, 32, 11, 40, 0, 12, 14, 24, 24, 25, 24, 27, 27, 39, 27, 40, 32, 42, 45, 32, 42, 32, 16, 35, 38, 53, 88, 55, 50, 40, 42, 32, 50, 10, 44, 50};
    public static final byte[] HEIGHT = {18, 11, 12, 38, 14, 32, 12, 32, 10, 32, 100, 32, 32, 38, 46, 36, 32, 22, 32, 14, 17, 15, 34, 27, 35, 34, 34, 34, 31, 11, 31, 0, 13, 14, 15, 22, 16, 23, 27, 27, 25, 28, 76, 28, 41, 46, 32, 41, 32, 16, 36, 49, 28, 60, 55, 51, 76, 41, 32, 26, 10, 44, 50};

    static {
        byte[] bArr = new byte[2];
        
        bArr[0] = 0;
        bArr[1] = 1;
        byte[] bArr2 = new byte[3];
        
        bArr2[0] = 0;
        bArr2[1] = 1;
        bArr2[2] = 2;
        FRAME = new byte[][]{new byte[]{0, 0}, new byte[]{17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}, new byte[]{0, 1, 2, 3, 4, 5, 6, 7}, new byte[]{0}, new byte[]{0, 1, 2, 3, 4, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4}, new byte[]{0, 1}, new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, new byte[]{0, 1, 1}, new byte[]{5, 5, 5, 0, 1, 2, 3, 4, 5, 6, 7, 8}, new byte[]{0}, bArr, new byte[]{0, 1}, new byte[]{0, 0, 1, 1}, bArr2, new byte[]{0, 0, 0, 1, 1, 1, 2, 2, 2}, new byte[]{0, 0, 1, 1, 2, 2}, new byte[]{0, 0, 1, 1, 2, 2, 3, 3}, new byte[]{0, 1, 2}, new byte[]{0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2, 3, 4}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}, new byte[]{0, 0, 0, 1, 1, 1, 2, 2, 2}, new byte[]{0}, new byte[]{0, 0, 1, 1, 2, 2, 3, 3}, new byte[]{0, 0, 1, 1, 2, 2, 3, 3}, new byte[]{0, 0, 0, 1, 1, 1}, new byte[]{0, 0, 1, 1, 2, 2, 3, 3, 4, 4}, new byte[]{0, 1, 2, 3, 4}, new byte[]{0, 1, 2, 3, 4}, new byte[]{0, 1, 2, 3, 4}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2, 3}, new byte[]{0, 0, 0, 1, 1, 1, 2, 2, 2}, new byte[]{0, 1, 2}, new byte[]{0, 0, 1, 1, 2, 2, 3, 3}, new byte[]{0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2}, new byte[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0}, new byte[]{0, 1, 2, 0, 1, 2, 0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0, 1}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2, 3}, new byte[]{0, 1, 2}, new byte[]{0, 1, 2}, new byte[]{0}, new byte[]{0}, new byte[]{0, 1, 2, 3, 4, 5}};
    }

    public static byte getWidth(int i) {
        return WIDTH[i];
    }

    public static byte getHeight(int i) {
        return HEIGHT[i];
    }

    public Effect(int i) {
        this.loop = (byte) 0;
        this.subEff = null;
        this.subArr = null;
    }

    public Effect() {
        this.loop = (byte) 0;
        this.subEff = null;
        this.subArr = null;
    }

    public Effect(int i, int i2, int i3) {
        this.loop = (byte) 0;
        this.subEff = null;
        this.subArr = null;
        this.x = i;
        this.y = i2;
        this.type = (short) i3;
        this.w = WIDTH[i3];
        this.h = HEIGHT[i3];
        this.f = -1;
    }

    public void paint(Graphics graphics) {
        short s = this.type;
        if (s < 0) {
            return;
        }
        if (this.f == -1) {
            this.f = 0;
        }
        byte b = FRAME[s][this.f];
        int i = this.h;
        Res.paintImgEff(graphics, s, 0, b * i, this.w, i, this.x, this.y, Graphics.VCENTER | Graphics.HCENTER);
    }

    public void update() {
        LiveActor liveActor = this.myChar;
        if (liveActor != null) {
            this.x = liveActor.x;
            this.y = this.myChar.y + this.yadd;
        }
        int i = this.f + 1;
        this.f = i;
        if (i >= FRAME[this.type].length) {
            this.f = 0;
            byte b = this.loop;
            if (b == 0) {
                this.wantDetroy = true;
                SubEff subEff = this.subEff;
                if (subEff != null) {
                    Effect effectCreatEff = GameScr.creatEff(subEff.ac, this.subEff.ac.x, this.subEff.ac.y, this.subEff.idEff);
                    EffectManager.hiEffects.addElement(effectCreatEff);
                    SubArrow subArrow = new SubArrow();
                    effectCreatEff.subArr = subArrow;
                    subArrow.from = this.subEff.ac;
                    effectCreatEff.subArr.to = this.subEff.target;
                    effectCreatEff.subArr.power = this.subEff.power;
                    effectCreatEff.subArr.hp = this.subEff.hp;
                    effectCreatEff.subArr.imgIndex = this.subEff.indeximg;
                    effectCreatEff.subArr.ideff = this.subEff.eff;
                    this.subEff = null;
                }
                if (this.subArr != null) {
                    for (int i2 = 0; i2 < this.subArr.to.size(); i2++) {
                        LiveActor liveActor2 = (LiveActor) this.subArr.to.elementAt(i2);
                        GameScr.createNewSkill(this.subArr.from, liveActor2, Integer.parseInt((String) this.subArr.power.elementAt(i2)), (byte) this.subArr.ideff, this.subArr.imgIndex);
                        liveActor2.hp = Integer.parseInt((String) this.subArr.hp.elementAt(i2));
                    }
                    this.subArr = null;
                    return;
                }
                return;
            }
            this.loop = (byte) (b - 1);
        }
    }
}
