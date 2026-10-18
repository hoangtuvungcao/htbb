package game.model.skill.complete;

import game.model.Char;
import game.model.Cout;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;

public class Skill_Kiem_Type2 extends SkillAnimate {
    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Kiem_Type2() {
        super(-1);
    }

    @Override
    public void updateSkill(Char r11) {
        try {
            super.updateSkill(r11);
            if (r11.state == 0) {
                return;
            }
            if (r11.p1 == 16) {
                r11.state = (byte) 0;
                r11.p3 = (short) 0;
                r11.p2 = (short) 0;
                r11.p1 = (short) 0;
                r11.weapon_frame = (short) 0;
            } else if (r11.p1 >= 14 && r11.p1 < 16) {
                r11.frame = (byte) 5;
                r11.weapon_frame = (short) 7;
            } else if (r11.p1 == 13 || r11.p1 == 12) {
                r11.frame = (byte) 5;
                r11.weapon_frame = (short) 6;
            } else if (r11.p1 == 11 || r11.p1 == 10) {
                r11.frame = (byte) 4;
                r11.weapon_frame = (short) 5;
            } else {
                try {
                    if (r11.p1 % 2 == 0) {
                        EffectManager.addHiEffect(r11.x + splashKiemX[r11.dir], r11.y - 80, 10);
                        EffectManager.addHiEffect(r11.x + splashKiemX[r11.dir], r11.y + splashKiemY[r11.dir], 3);
                    }
                    r11.frame = (byte) 4;
                    r11.weapon_frame = (short) 4;
                } catch (Exception unused) {
                }
            }
            try {
                if (r11.p1 == 9) {
                    GCanvas.gameScr.startNewMagicBeam(1, r11, r11.attackTarget, r11.x + splashKiemX[r11.dir], r11.y + splashKiemY[r11.dir], r11.attkPower, r11.attkEffect);
                }
            } catch (Exception unused2) {
            }
            r11.p1 = (short) (r11.p1 + 1);
        } catch (Exception unused3) {
            Cout.println("Loi skill kiem type2");
        }
    }
}
