package game.model.skill.complete;

import game.model.AttackResult;
import game.model.Char;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.model.skill.Skill_AEO_Dao_5;
import game.render.GCanvas;

public class Skill_Dao_Type3 extends SkillAnimate {
    int ID;
    byte loop;
    int lvSkill;
    byte nDragon;
    private static byte[] x2 = {0, 20, 20, 0, 0, 20, 20, 20, 20};
    private static byte[] y2 = {0, 0, 0, 20, 20, 20, 20, 20, 20};
    public static short[][] goc = {new short[]{90, 315, 225, 270, 315, 225, 90, 315, 225, 315, 225, 225}, new short[]{270, 135, 45, 90, 135, 45, 270, 135, 45, 135, 45, 45}, new short[]{180, 45, 315, 180, 45, 315, 180, 45, 315, 45, 315, 315}, new short[]{0, 135, 225, 0, 135, 225, 0, 135, 225, 135, 225, 225}};

    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Dao_Type3(int i) {
        super(-1);
        this.nDragon = (byte) 1;
        this.ID = i;
    }

    @Override
    public void setLvSkill(int i) {
        this.lvSkill = i;
    }

    @Override
    public int getX2() {
        this.nDragon = (byte) nDra[this.lvSkill];
        return nDra[this.lvSkill];
    }

    @Override
    public void updateSkill(Char r15) {
        if (r15 == null) {
            return;
        }
        super.updateSkill(r15);
        if (r15.state == 0) {
            return;
        }
        if (r15.p1 == 16) {
            this.loop = (byte) 0;
        }
        updateSkillDao(r15);
        if (r15.p1 == 9) {
            try {
                if (this.ID == 0) {
                    GCanvas.gameScr.startNewMagicBeam(4, r15, r15.attackTarget, r15.x + splashDuaX[r15.dir] + x2[this.loop], r15.y + splashDuaY[r15.dir] + y2[this.loop], r15.attkPower != 2000000 ? r15.attkPower / this.nDragon : r15.attkPower, r15.attkEffect, goc[r15.dir][this.loop]);
                    byte b = (byte) (this.loop + 1);
                    this.loop = b;
                    if (b < this.nDragon) {
                        r15.p1 = (short) 8;
                    }
                } else {
                    Skill_AEO_Dao_5 skill_AEO_Dao_5 = new Skill_AEO_Dao_5(r15.x, r15.y + 30, r15.attackTarget, 2, true);
                    int i = this.lvSkill;
                    if (i > 0 && i < 7) {
                        skill_AEO_Dao_5.isDoubleDregon = false;
                    } else {
                        skill_AEO_Dao_5.isDoubleDregon = true;
                    }
                    if (r15.attkPower != 0 && r15.attkPower != 2000000) {
                        skill_AEO_Dao_5.textAttack[0] = "-" + r15.attkPower;
                    }
                    if (r15.attkEffect != 0 && r15.attkEffect < AttackResult.EFF_NAME.length) {
                        skill_AEO_Dao_5.textAttack[1] = AttackResult.EFF_NAME[r15.attkEffect];
                    }
                    if (r15.attackTarget.catagory == 1) {
                        ((Monster) r15.attackTarget).isTarget = true;
                    }
                    EffectManager.hiEffects.addElement(skill_AEO_Dao_5);
                }
            } catch (Exception unused) {
            }
        }
        r15.p1 = (short) (r15.p1 + 1);
    }
}
