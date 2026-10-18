package game.model.skill.complete;

import game.model.Char;
import game.model.Cout;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;

public class Skill_Dao_Type2 extends SkillAnimate {
    public Skill_Dao_Type2() {
        super(-1);
    }

    @Override
    public void updateSkill(Monster monster) {
        GCanvas.gameScr.startNewMagicBeam(4, monster, monster.attackTarget, monster.x + splashDuaX[monster.dir], monster.y + splashDuaY[monster.dir], monster.targethplost, (byte) monster.eff);
    }

    @Override
    public void updateSkill(Char r11) {
        try {
            super.updateSkill(r11);
            if (r11.state == 0) {
                return;
            }
            updateSkillDao(r11);
            try {
                if (r11.p1 == 9) {
                    GCanvas.gameScr.startNewMagicBeam(4, r11, r11.attackTarget, r11.x + splashDuaX[r11.dir], r11.y + splashDuaY[r11.dir], r11.attkPower, r11.attkEffect);
                }
            } catch (Exception unused) {
            }
            r11.p1 = (short) (r11.p1 + 1);
        } catch (Exception unused2) {
            Cout.println("Loi dao type 2");
        }
    }
}
