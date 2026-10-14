package game.model.skill.complete;

import game.model.Char;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;

public class Skill_Cung_Type2 extends SkillAnimate {
    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Cung_Type2() {
        super(0);
    }

    @Override
    public void updateSkill(Char r11) {
        if (r11 == null) {
            return;
        }
        super.updateSkill(r11);
        if (r11.state == 0) {
            return;
        }
        updateSkillCung_2_3(r11);
        if (r11.p1 == 10 && r11.attackTarget != null) {
            GCanvas.gameScr.startNewArrow(0, r11, r11.attackTarget, r11.x, r11.y - 15, r11.attkPower, r11.attkEffect, 2);
        }
        r11.p1 = (short) (r11.p1 + 1);
    }
}
