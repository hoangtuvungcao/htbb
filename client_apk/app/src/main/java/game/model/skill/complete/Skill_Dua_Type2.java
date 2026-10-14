package game.model.skill.complete;

import game.model.Char;
import game.model.Cout;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;

public class Skill_Dua_Type2 extends SkillAnimate {
    int pmonster;

    public Skill_Dua_Type2() {
        super(-1);
        this.pmonster = 0;
    }

    @Override
    public void updateSkill(Monster monster) {
        GCanvas.gameScr.startNewMagicBeam(3, monster, monster.attackTarget, monster.x + splashDuaX[monster.dir], monster.y + splashDuaY[monster.dir], monster.targethplost, (byte) monster.eff);
    }

    @Override
    public void updateSkill(Char r11) {
        try {
            super.updateSkill(r11);
            if (r11.state == 0) {
                return;
            }
            try {
                updateSkillDua(r11);
            } catch (Exception unused) {
            }
            try {
                if (r11.p1 == 3) {
                    GCanvas.gameScr.startNewMagicBeam(3, r11, r11.attackTarget, r11.x + splashDuaX[r11.dir], r11.y + splashDuaY[r11.dir], r11.attkPower, r11.attkEffect);
                }
            } catch (Exception unused2) {
            }
            r11.p1 = (short) (r11.p1 + 1);
        } catch (Exception unused3) {
            Cout.println("Loi skill dua type2");
        }
    }
}
