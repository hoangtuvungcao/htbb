package game.model.skill.complete;

import game.model.Char;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.Canvas;

public class Skill_Eevee_Type2 extends SkillAnimate {
    int pmonster;

    public Skill_Eevee_Type2() {
        super(-1);
        this.pmonster = 0;
    }

    @Override
    public void updateSkill(Monster c) {
        Canvas.gameScr.startNewMagicBeam(3, c, c.attackTarget, c.x + SkillAnimate.splashDuaX[c.dir], c.y + SkillAnimate.splashDuaY[c.dir], c.targethplost, (byte) c.eff);
    }

    @Override
    public void updateSkill(Char c) {
        super.updateSkill(c);
        if (c.state == 0) {
            return;
        }
        updateSkillDua(c);
        if (c.p1 == 3) {
            Canvas.gameScr.startNewMagicBeam(3, c, c.attackTarget, c.x + SkillAnimate.splashDuaX[c.dir], c.y + SkillAnimate.splashDuaY[c.dir], c.attkPower, c.attkEffect);
        }
        c.p1 = (short) (c.p1 + 1);
    }
}
