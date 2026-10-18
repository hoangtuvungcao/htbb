package game.model.skill.complete;

import game.model.Char;
import game.model.skill.SkillAnimate;

public class Skill_Squirtle_Type2 extends SkillAnimate {
    public Skill_Squirtle_Type2() {
        super(0);
    }

    @Override
    public void updateSkill(Char c) {
        super.updateSkill(c);
        if (c.state == 0) {
            return;
        }
        updateSkillBua_2_3(c, false, 1, 0, 0);
        c.p1 = (short) (c.p1 + 1);
    }
}
