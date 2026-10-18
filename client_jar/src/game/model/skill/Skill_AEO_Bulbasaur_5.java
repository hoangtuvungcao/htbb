package game.model.skill;

import game.model.EffBack;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import javax.microedition.lcdui.Graphics;

public class Skill_AEO_Bulbasaur_5 extends Effect {
    private LiveActor c;
    private boolean isEff;
    private int angle;
    private int dis;
    private int countDown;
    private int v;
    private int xGoc;
    private int yGoc;
    private int[] index;
    public String[] textAttack;
    private int type;
    private static byte[][] indexImg = {new byte[]{15, 28}, new byte[]{4, 4}, new byte[]{15, 28}};
    private static byte[][] wImg = {new byte[]{17, 36}, new byte[]{32, 31}, new byte[]{14, 14}, new byte[]{17, 36}, new byte[]{17, 36}};
    private static byte[] indexLimit1 = {3, 5, 3};
    private boolean isNo = false;
    public boolean isDoubleDregon = false;

    public Skill_AEO_Bulbasaur_5(int x0, int y0, LiveActor c, int t, boolean isEff) {
        this.angle = 0;
        this.type = 0;
        this.isEff = isEff;
        this.type = t;
        this.x = c.x;
        this.y = c.y - 10;
        this.xGoc = x0;
        this.yGoc = y0 - 40;
        this.c = c;
        this.angle = Util.angle((c.x - 10) - this.xGoc, -(c.y - this.yGoc));
        this.dis = Util.distance(this.xGoc, this.yGoc, c.x - 10, c.y);
        this.index = new int[3];
        for (int i = 0; i < 3; i++) {
            this.index[i] = Res.rnd(6);
        }
        this.textAttack = new String[2];
    }

    @Override
    public void update() {
        EffBack eff;
        this.angle = Util.angle(this.x - this.xGoc, -(this.y - this.yGoc));
        this.dis = Util.distance(this.xGoc, this.yGoc, this.x, this.y);
        this.countDown += 6;
        this.v++;
        if (!this.isNo) {
            this.x = this.c.x;
            this.y = this.c.y - 10;
        }
        if (this.countDown > this.dis && !this.isNo) {
            EffectManager.addHiEffect(this.c.x, this.c.y - 10, 12);
            EffectManager.hiEffects.removeElement(this);
            if (this.c.catagory == 1) {
                ((Monster) this.c).isTarget = false;
            }
            this.c.jump();
            this.isNo = true;
            if (this.textAttack[0] != null) {
                Canvas.gameScr.startFlyText(this.textAttack[0], 0, this.x, this.y - 15, 1, -2);
            }
            if (this.textAttack[1] != null) {
                Canvas.gameScr.startFlyText(this.textAttack[1], 0, this.x, this.y - 15, 2, -2);
            }
            if (this.isEff) {
                if (this.type == 1) {
                    eff = new EffBack(this.x, this.y, 4, 5, 14, 14, false, false);
                } else {
                    eff = new EffBack(this.x, this.y, 28, 3, 32, 31, true, false);
                    if (this.type == 2) {
                        Skill_AEO_Eevee_Bulbasaur_5 skill = new Skill_AEO_Eevee_Bulbasaur_5(this.c.x + SkillAnimate.splashDuaX[this.c.dir], this.c.y + SkillAnimate.splashDuaY[this.c.dir], this.c, this.c, (byte) 5);
                        skill.isDoubleDragon = this.isDoubleDregon;
                        EffectManager.hiEffects.addElement(skill);
                    }
                }
                EffectManager.lowEffects.addElement(eff);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        int xa = (this.countDown * Util.cos(Util.fixangle(this.angle))) >> 10;
        int ya = (-(this.countDown * Util.sin(Util.fixangle(this.angle)))) >> 10;
        g.drawImage(Res.imgShadow, this.xGoc + xa, this.yGoc + ya, 3);
        for (int i = 0; i < 3; i++) {
            Res.paintImgEff(g, indexImg[this.type][0], 0, (this.index[i] / 2) * wImg[this.type * 2][1], wImg[this.type * 2][0], wImg[this.type * 2][1], ((this.xGoc + xa) + Res.rnd(10)) - 5, ((this.yGoc + ya) + Res.rnd(10)) - 10, 3);
            int[] iArr = this.index;
            int i2 = i;
            iArr[i2] = iArr[i2] + 1;
            if (this.index[i] >= indexLimit1[this.type]) {
                this.index[i] = 0;
            }
        }
    }
}
