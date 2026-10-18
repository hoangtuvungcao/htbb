package game.model.skill.complete;

import game.model.Char;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.Canvas;

public class Skill_Kiem_Type1 extends SkillAnimate {
    public Skill_Kiem_Type1() {
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
        updateSkillKiem(c);
        if (c.p1 == 3) {
            if (c.attackTarget != null) {
                if (c.attackTarget.catagory == 1) {
                    ((Monster) c.attackTarget).jump();
                }
                c.attackTarget.realHPSyncTime = 2;
            }
            EffectManager.addHiEffect(c.attackTarget.x, c.attackTarget.y - 10, 3);
            if (c.attkPower != 2000000) {
                Canvas.gameScr.startFlyText(new StringBuffer("-").append(c.attkPower).toString(), 0, c.attackTarget.x + 0, c.attackTarget.y - 15, -1, -2);
            }
        }
        c.p1 = (short) (c.p1 + 1);
    }
}
