package game.model.skill.complete;

import game.model.Char;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.Canvas;

public class Skill_Pichu_Type2 extends SkillAnimate {
    public Skill_Pichu_Type2() {
        super(0);
    }

    @Override
    public void updateSkill(Monster c) {
    }

    @Override
    public void updateSkill(Char c) {
        super.updateSkill(c);
        if (c.state == 0) {
            return;
        }
        updateSkillCung_2_3(c);
        if (c.p1 == 10) {
            Canvas.gameScr.startNewArrow(0, c, c.attackTarget, c.x, c.y - 15, c.attkPower, c.attkEffect, 2);
        }
        c.p1 = (short) (c.p1 + 1);
    }
}
