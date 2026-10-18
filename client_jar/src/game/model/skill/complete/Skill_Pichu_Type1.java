package game.model.skill.complete;

import game.model.Char;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.Canvas;

public class Skill_Pichu_Type1 extends SkillAnimate {
    public Skill_Pichu_Type1() {
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
        if (c.p1 == 15) {
            c.state = (byte) 0;
            c.p3 = (short) 0;
            c.p2 = (short) 0;
            c.p1 = (short) 0;
            c.weapon_frame = (short) 0;
        } else if (c.p1 == 12 || c.p1 == 11) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 7;
        } else if (c.p1 == 10 || c.p1 == 9) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 6;
        } else if (c.p1 == 8 || c.p1 == 7) {
            c.frame = (byte) 4;
            c.weapon_frame = (short) 5;
        } else {
            if (c.p1 == 1) {
                EffectManager.addHiEffect(c.x + SkillAnimate.splashDuaX[c.dir], c.y + SkillAnimate.splashDuaY[c.dir], 16);
            }
            c.frame = (byte) 4;
            c.weapon_frame = (short) 4;
        }
        if (c.p1 == 5) {
            Canvas.gameScr.startNewArrow(0, c, c.attackTarget, c.x, c.y - 15, c.attkPower, c.attkEffect, 1);
        }
        c.p1 = (short) (c.p1 + 1);
    }
}
