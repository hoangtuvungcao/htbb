package game.model.skill.complete;

import game.model.Char;
import game.model.EffectManager;
import game.model.skill.SkillAnimate;
import game.model.skill.Skill_Eff_end;

public class Skill_Bua_Type3 extends SkillAnimate {
    int ID;
    private int loop;
    int lvSkill;
    byte nDragon;

    public Skill_Bua_Type3(int i) {
        super(0);
        this.nDragon = (byte) 1;
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
    public void updateSkill(Char r8) {
        if (r8 == null) {
            return;
        }
        super.updateSkill(r8);
        if (r8.state == 0) {
            return;
        }
        if (r8.p1 == 20) {
            this.loop = 0;
        }
        try {
            updateSkillBua_2_3(r8, true, this.nDragon, this.ID, this.lvSkill);
        } catch (Exception unused) {
        }
        if (r8.p1 == 13) {
            if (r8.attackTarget != null && this.ID == 0) {
                int i = this.loop + 1;
                this.loop = i;
                if (i < this.nDragon) {
                    r8.p1 = (short) 10;
                }
            }
            if (this.ID == 1) {
                EffectManager.hiEffects.addElement(new Skill_Eff_end(r8.attackTarget.x, r8.attackTarget.y - 200, r8.attackTarget));
            }
        }
        r8.p1 = (short) (r8.p1 + 1);
    }
}
