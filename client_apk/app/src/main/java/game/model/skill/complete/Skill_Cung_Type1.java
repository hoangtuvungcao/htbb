package game.model.skill.complete;

import game.model.Char;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;

public class Skill_Cung_Type1 extends SkillAnimate {
    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Cung_Type1() {
        super(0);
    }

    @Override
    public void updateSkill(Char r13) {
        if (r13 == null) {
            return;
        }
        super.updateSkill(r13);
        if (r13.state == 0) {
            return;
        }
        if (r13.p1 == 15) {
            r13.state = (byte) 0;
            r13.p3 = (short) 0;
            r13.p2 = (short) 0;
            r13.p1 = (short) 0;
            r13.weapon_frame = (short) 0;
        } else if (r13.p1 == 12 || r13.p1 == 11) {
            r13.frame = (byte) 5;
            r13.weapon_frame = (short) 7;
        } else if (r13.p1 == 10 || r13.p1 == 9) {
            r13.frame = (byte) 5;
            r13.weapon_frame = (short) 6;
        } else if (r13.p1 == 8 || r13.p1 == 7) {
            r13.frame = (byte) 4;
            r13.weapon_frame = (short) 5;
        } else {
            try {
                if (r13.p1 == 1) {
                    EffectManager.addHiEffect(r13.x + splashDuaX[r13.dir], r13.y + splashDuaY[r13.dir], 16);
                }
                r13.frame = (byte) 4;
                r13.weapon_frame = (short) 4;
            } catch (Exception unused) {
            }
        }
        if (r13.p1 == 5 && r13.attackTarget != null) {
            GCanvas.gameScr.startNewArrow(0, r13, r13.attackTarget, r13.x, r13.y - 15, r13.attkPower, r13.attkEffect, 1);
        }
        r13.p1 = (short) (r13.p1 + 1);
    }
}
