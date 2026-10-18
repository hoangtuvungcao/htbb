package game.model.skill.complete;

import game.model.Char;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.Canvas;

public class Skill_Eevee_Type1 extends SkillAnimate {
    public Skill_Eevee_Type1() {
        super(-1);
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
        if (c.p1 == 18) {
            c.state = (byte) 0;
            c.p3 = (short) 0;
            c.p2 = (short) 0;
            c.p1 = (short) 0;
            c.weapon_frame = (short) 0;
        } else if (c.p1 >= 6 && c.p1 < 18) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 7;
        } else if (c.p1 == 5 || c.p1 == 4) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 6;
        } else if (c.p1 == 3 || c.p1 == 2) {
            c.frame = (byte) 4;
            c.weapon_frame = (short) 5;
        } else {
            c.frame = (byte) 4;
            c.weapon_frame = (short) 4;
        }
        if (c.p1 == 3) {
            Canvas.gameScr.startNewMagicBeam(0, c, c.attackTarget, c.x + SkillAnimate.splashDuaX[c.dir], c.y + SkillAnimate.splashDuaY[c.dir], c.attkPower, c.attkEffect);
        }
        c.p1 = (short) (c.p1 + 1);
    }
}
