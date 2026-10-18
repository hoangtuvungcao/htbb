package game.model.skill.complete;

import game.model.Char;
import game.model.skill.SkillAnimate;

public class Skill_Bua_Type2 extends SkillAnimate {
    public Skill_Bua_Type2() {
        super(0);
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
        updateSkillBua_2_3(r8, false, 1, 0, 0);
        r8.p1 = (short) (r8.p1 + 1);
    }
}
