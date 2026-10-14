package game.model.skill.complete;

import game.model.Char;
import game.model.Cout;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;

public class Skill_Dua_Type1 extends SkillAnimate {
    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Dua_Type1() {
        super(-1);
    }

    @Override
    public void updateSkill(Char r11) {
        try {
            super.updateSkill(r11);
            if (r11.state == 0) {
                return;
            }
            if (r11.p1 == 18) {
                r11.state = (byte) 0;
                r11.p3 = (short) 0;
                r11.p2 = (short) 0;
                r11.p1 = (short) 0;
                r11.weapon_frame = (short) 0;
            } else if (r11.p1 >= 6 && r11.p1 < 18) {
                r11.frame = (byte) 5;
                r11.weapon_frame = (short) 7;
            } else if (r11.p1 == 5 || r11.p1 == 4) {
                r11.frame = (byte) 5;
                r11.weapon_frame = (short) 6;
            } else if (r11.p1 == 3 || r11.p1 == 2) {
                r11.frame = (byte) 4;
                r11.weapon_frame = (short) 5;
            } else {
                r11.frame = (byte) 4;
                r11.weapon_frame = (short) 4;
            }
            try {
                if (r11.p1 == 3) {
                    GCanvas.gameScr.startNewMagicBeam(0, r11, r11.attackTarget, r11.x + splashDuaX[r11.dir], r11.y + splashDuaY[r11.dir], r11.attkPower, r11.attkEffect);
                }
            } catch (Exception unused) {
            }
            r11.p1 = (short) (r11.p1 + 1);
        } catch (Exception unused2) {
            Cout.println("Loi skill dua type 1");
        }
    }
}
