package game.model.skill.complete;

import game.model.Char;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.Canvas;

public class Skill_Pichu_Type3 extends SkillAnimate {
    int lvSkill;
    int ID;
    byte nDragon;
    int loop;

    public Skill_Pichu_Type3(int id) {
        super(0);
        this.nDragon = (byte) 1;
        this.loop = 0;
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
        int yadd;
        int xadd;
        super.updateSkill(c);
        if (c.state == 0) {
            return;
        }
        if (c.p1 == 15) {
            this.loop = 0;
        }
        updateSkillCung_2_3(c);
        if (c.p1 == 10) {
            if (this.ID == 0) {
                Canvas.gameScr.startNewArrow(0, c, c.attackTarget, c.x, c.y - 15, c.attkPower != 2000000 ? c.attkPower / this.nDragon : c.attkPower, c.attkEffect, 2);
                this.loop++;
                if (this.loop < this.nDragon) {
                    c.p1 = (short) 7;
                }
            } else {
                if (c.dir == 3) {
                    yadd = -22;
                    xadd = 10;
                } else {
                    yadd = -22;
                    xadd = -10;
                }
                Canvas.gameScr.startWeaponsLazer(c, c.attackTarget, xadd, yadd, c.dir, c.attkPower != 2000000 ? c.attkPower / this.nDragon : c.attkPower, 1);
                EffectManager.addHiEffect(c.attackTarget.x, c.attackTarget.y - 15, 32);
                EffectManager.addHiEffect(c.attackTarget.x, c.attackTarget.y - 10, 9);
            }
        }
        c.p1 = (short) (c.p1 + 1);
    }

    @Override
    public void updateSkill(Monster c) {
    }
}
