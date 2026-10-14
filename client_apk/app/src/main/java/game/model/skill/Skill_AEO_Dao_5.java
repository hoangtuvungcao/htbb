package game.model.skill;

import game.core.j2me.Graphics;
import game.model.Cout;
import game.model.EffBack;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;

public class Skill_AEO_Dao_5 extends Effect {
    private int angle;
    private LiveActor c;
    private int countDown;
    private int dis;
    private int[] index;
    private boolean isEff;
    public String[] textAttack;
    private int type;
    private int v;
    private int xGoc;
    private int yGoc;
    private static byte[][] indexImg = {new byte[]{15, 28}, new byte[]{4, 4}, new byte[]{15, 28}};
    private static byte[][] wImg = {new byte[]{17, 36}, new byte[]{32, 31}, new byte[]{14, 14}, new byte[]{17, 36}, new byte[]{17, 36}};
    private static byte[] indexLimit1 = {3, 5, 3};
    private boolean isNo = false;
    public boolean isDoubleDregon = false;

    public Skill_AEO_Dao_5(int i, int i2, LiveActor liveActor, int i3, boolean z) {
        this.angle = 0;
        this.isEff = z;
        this.type = i3;
        this.x = liveActor.x;
        this.y = liveActor.y - 10;
        this.xGoc = i;
        this.yGoc = i2 - 40;
        this.c = liveActor;
        this.angle = Util.angle((liveActor.x - 10) - this.xGoc, -(liveActor.y - this.yGoc));
        this.dis = Util.distance(this.xGoc, this.yGoc, liveActor.x - 10, liveActor.y);
        this.index = new int[3];
        for (int i4 = 0; i4 < 3; i4++) {
            this.index[i4] = Res.rnd(6);
        }
        this.textAttack = new String[2];
    }

    @Override
    public void update() {
        EffBack effBack;
        try {
            this.angle = Util.angle(this.x - this.xGoc, -(this.y - this.yGoc));
            this.dis = Util.distance(this.xGoc, this.yGoc, this.x, this.y);
            this.countDown += 6;
            this.v++;
            if (!this.isNo) {
                this.x = this.c.x;
                this.y = this.c.y - 10;
            }
            if (this.countDown <= this.dis || this.isNo) {
                return;
            }
            EffectManager.addHiEffect(this.c.x, this.c.y - 10, 12);
            EffectManager.hiEffects.removeElement(this);
            if (this.c.catagory == 1) {
                ((Monster) this.c).isTarget = false;
            }
            this.c.jump();
            this.isNo = true;
            if (this.textAttack[0] != null) {
                GCanvas.gameScr.startFlyText(this.textAttack[0], 0, this.x, this.y - 15, 1, -2);
            }
            if (this.textAttack[1] != null) {
                GCanvas.gameScr.startFlyText(this.textAttack[1], 0, this.x, this.y - 15, 2, -2);
            }
            if (this.isEff) {
                if (this.type == 1) {
                    effBack = new EffBack(this.x, this.y, 4, 5, 14, 14, false, false);
                } else {
                    effBack = new EffBack(this.x, this.y, 28, 3, 32, 31, true, false);
                    if (this.type == 2) {
                        int i = this.c.x + SkillAnimate.splashDuaX[this.c.dir];
                        int i2 = this.c.y + SkillAnimate.splashDuaY[this.c.dir];
                        LiveActor liveActor = this.c;
                        Skill_AEO_DUA_DAO_5 skill_AEO_DUA_DAO_5 = new Skill_AEO_DUA_DAO_5(i, i2, liveActor, liveActor, (byte) 5);
                        skill_AEO_DUA_DAO_5.isDoubleDragon = this.isDoubleDregon;
                        EffectManager.hiEffects.addElement(skill_AEO_DUA_DAO_5);
                    }
                }
                EffectManager.lowEffects.addElement(effBack);
            }
        } catch (Exception unused) {
            Cout.println("update AEO dao 5");
        }
    }

    @Override
    public void paint(Graphics graphics) {
        try {
            int iCos = (this.countDown * Util.cos(Util.fixangle(this.angle))) >> 10;
            int i = (-(this.countDown * Util.sin(Util.fixangle(this.angle)))) >> 10;
            graphics.drawImage(Res.imgShadow, this.xGoc + iCos, this.yGoc + i, 3);
            for (int i2 = 0; i2 < 3; i2++) {
                byte[][] bArr = indexImg;
                int i3 = this.type;
                byte b = bArr[i3][0];
                int i4 = this.index[i2] / 2;
                byte[][] bArr2 = wImg;
                Res.paintImgEff(graphics, b, 0, bArr2[i3 * 2][1] * i4, bArr2[i3 * 2][0], bArr2[i3 * 2][1], ((this.xGoc + iCos) + Res.rnd(10)) - 5, ((this.yGoc + i) + Res.rnd(10)) - 10, 3);
                int[] iArr = this.index;
                int i5 = iArr[i2] + 1;
                iArr[i2] = i5;
                if (i5 >= indexLimit1[this.type]) {
                    iArr[i2] = 0;
                }
            }
        } catch (Exception unused) {
            Cout.println("skill AEO dao 5");
        }
    }
}
