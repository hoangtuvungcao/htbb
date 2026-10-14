package game.model.skill.complete;

import game.model.Char;
import game.model.EffectManager;
import game.model.skill.SkillAnimate;
import game.model.skill.Skill_Eff_end;

public class Skill_Bua_Type3 extends SkillAnimate {
    int lvSkill;
    int ID;
    byte nDragon;
    private int loop;

    public Skill_Bua_Type3(int id) {
        super(0);
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
        if (c.p1 == 20) {
            this.loop = 0;
        }
        updateSkillBua_2_3(c, true, this.nDragon, this.ID, this.lvSkill);
        if (c.p1 == 13) {
            if (c.attackTarget != null && this.ID == 0) {
                this.loop++;
                if (this.loop < this.nDragon) {
                    c.p1 = (short) 10;
                }
            }
            if (this.ID == 1) {
                Skill_Eff_end skill = new Skill_Eff_end(c.attackTarget.x, c.attackTarget.y - 200, c.attackTarget);
                EffectManager.hiEffects.addElement(skill);
            }
        }
        c.p1 = (short) (c.p1 + 1);
    }
}
