package game.model.skill.complete;

import game.model.Char;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;

public class Skill_Cung_Type3 extends SkillAnimate {
    int ID;
    int loop;
    int lvSkill;
    byte nDragon;

    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Cung_Type3(int i) {
        super(0);
        this.nDragon = (byte) 1;
        this.loop = 0;
        this.ID = i;
    }

    @Override
    public void setLvSkill(int i) {
        this.lvSkill = i;
    }

    @Override
    public int getX2() {
        this.nDragon = (byte) nDra[this.lvSkill];
        return nDra[this.lvSkill];
    }

    @Override
    public void updateSkill(Char r14) {
        if (r14 == null) {
            return;
        }
        super.updateSkill(r14);
        if (r14.state == 0) {
            return;
        }
        if (r14.p1 == 15) {
            this.loop = 0;
        }
        updateSkillCung_2_3(r14);
        if (r14.attackTarget != null && r14.p1 == 10) {
            if (this.ID == 0) {
                GCanvas.gameScr.startNewArrow(0, r14, r14.attackTarget, r14.x, r14.y - 15, r14.attkPower != 2000000 ? r14.attkPower / this.nDragon : r14.attkPower, r14.attkEffect, 2);
                int i = this.loop + 1;
                this.loop = i;
                if (i < this.nDragon) {
                    r14.p1 = (short) 7;
                }
            } else {
                GCanvas.gameScr.startWeaponsLazer(r14, r14.attackTarget, r14.dir == 3 ? 10 : -10, -22, r14.dir, r14.attkPower != 2000000 ? r14.attkPower / this.nDragon : r14.attkPower, 1);
                EffectManager.addHiEffect(r14.attackTarget.x, r14.attackTarget.y - 15, 32);
                EffectManager.addHiEffect(r14.attackTarget.x, r14.attackTarget.y - 10, 9);
            }
        }
        r14.p1 = (short) (r14.p1 + 1);
    }
}
