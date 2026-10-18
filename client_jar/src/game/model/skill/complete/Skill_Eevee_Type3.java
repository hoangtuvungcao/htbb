package game.model.skill.complete;

import game.model.Char;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.Canvas;
import game.render.Res;
import javax.microedition.lcdui.Graphics;

public class Skill_Eevee_Type3 extends SkillAnimate {
    int pmonster;
    int lvSkill;
    int ID;
    byte loop;
    byte lap;
    byte nDragon;

    public Skill_Eevee_Type3(int id) {
        super(-1);
        this.pmonster = 0;
        this.nDragon = (byte) 1;
        this.ID = id;
    }

    @Override
    public void setLvSkill(int lv) {
        this.lvSkill = lv;
    }

    @Override
    public int getX2() {
        this.nDragon = (byte) SkillAnimate.nDra[this.lvSkill];
        return SkillAnimate.nDra[this.lvSkill];
    }

    @Override
    public void updateSkill(Char c) {
        super.updateSkill(c);
        if (c.state == 0) {
            return;
        }
        if (c.p1 == 8) {
            this.loop = (byte) 0;
            this.lap = (byte) 0;
        }
        updateSkillDua(c);
        if (c.p1 == 3 && this.ID == 0) {
            short[][] goc = {new short[]{90, 315, 225, 270, 315, 225, 90, 315, 225, 315, 225}, new short[]{270, 135, 45, 90, 135, 45, 270, 135, 45, 135, 45}, new short[]{180, 45, 315, 180, 45, 315, 180, 45, 315, 45, 315}, new short[]{0, 135, 225, 0, 135, 225, 0, 135, 225, 135, 225}};
            if (c.dir <= SkillAnimate.splashDuaX.length - 1 && c.dir <= SkillAnimate.splashDuaY.length - 1 && c.dir <= goc.length - 1 && this.loop <= goc[c.dir].length - 1) {
                Canvas.gameScr.startNewMagicBeam(3, c, c.attackTarget, c.x + SkillAnimate.splashDuaX[c.dir], c.y + SkillAnimate.splashDuaY[c.dir], c.attkPower != 2000000 ? c.attkPower / this.nDragon : c.attkPower, c.attkEffect, goc[c.dir][this.loop]);
            }
            this.loop = (byte) (this.loop + 1);
            if (this.loop < this.nDragon) {
                c.p1 = (short) 0;
            }
        }
        if (this.lap % 2 == 0 && this.ID == 1) {
            int sl = 1;
            if (this.lvSkill < 7 && this.lvSkill > 3) {
                sl = 2;
            } else if (this.lvSkill >= 7) {
                sl = 4;
            }
            for (int i = 0; i < sl; i++) {
                Eff_DauRong_Down skill = new Eff_DauRong_Down(c.attackTarget.x, c.attackTarget.y - 200, c.attackTarget);
                if (c.attkPower != 2000000) {
                    skill.power = c.attkPower / this.nDragon;
                } else {
                    skill.power = c.attkPower;
                }
                EffectManager.hiEffects.addElement(skill);
                EffectManager.addHiEffect(c.x, c.y - 15, 23);
            }
        }
        c.p1 = (short) (c.p1 + 1);
        if (this.ID == 1) {
            this.lap = (byte) (this.lap + 1);
        }
    }

    @Override
    public void updateSkill(Monster c) {
    }

    class Eff_DauRong_Down extends Effect {
        int x;
        int y;
        int xTo;
        int yTo;
        int power;
        public LiveActor to;
        int vy = 5;
        int[] wImg = {0, 0, 24, 24};

        public Eff_DauRong_Down(int xF, int yF, LiveActor to) {
            this.x = Canvas.random(xF - 15, xF + 15);
            this.y = Canvas.random(yF - 20, yF + 20);
            this.to = to;
        }

        @Override
        public void update() {
            this.y += this.vy;
            this.vy += 2;
            if (this.y > this.to.y - 15) {
                EffectManager.hiEffects.removeElement(this);
                EffectManager.addHiEffect(this.x, this.y - 5, 30);
            }
            if (this.power != 0) {
                Canvas.gameScr.startFlyText(new StringBuffer("-").append(this.power).toString(), 0, this.x, this.y - 15, 1, -2);
            } else {
                Canvas.gameScr.startFlyText("MISS", 0, this.x, this.y - 15, 1, -2);
            }
        }

        @Override
        public void paint(Graphics g) {
            if (Res.getImgArrow(4) != null) {
                g.drawRegion(Res.getImgArrow(4), this.wImg[0], this.wImg[1], this.wImg[2], this.wImg[3], 5, this.x, this.y, 3);
            }
        }
    }
}
