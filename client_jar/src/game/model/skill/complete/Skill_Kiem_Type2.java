package game.model.skill.complete;

import game.model.Char;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.Canvas;

public class Skill_Kiem_Type2 extends SkillAnimate {
    public Skill_Kiem_Type2() {
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
        if (c.p1 == 16) {
            c.state = (byte) 0;
            c.p3 = (short) 0;
            c.p2 = (short) 0;
            c.p1 = (short) 0;
            c.weapon_frame = (short) 0;
        } else if (c.p1 >= 14 && c.p1 < 16) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 7;
        } else if (c.p1 == 13 || c.p1 == 12) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 6;
        } else if (c.p1 == 11 || c.p1 == 10) {
            c.frame = (byte) 4;
            c.weapon_frame = (short) 5;
        } else {
            if (c.p1 % 2 == 0) {
                EffectManager.addHiEffect(c.x + SkillAnimate.splashKiemX[c.dir], c.y + SkillAnimate.splashKiemY[c.dir], 3);
            }
            c.frame = (byte) 4;
            c.weapon_frame = (short) 4;
        }
        if (c.p1 == 9) {
            Canvas.gameScr.startNewMagicBeam(1, c, c.attackTarget, c.x + SkillAnimate.splashKiemX[c.dir], c.y + SkillAnimate.splashKiemY[c.dir], c.attkPower, c.attkEffect);
        }
        c.p1 = (short) (c.p1 + 1);
    }
}
