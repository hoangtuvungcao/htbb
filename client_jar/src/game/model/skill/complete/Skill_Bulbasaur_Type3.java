package game.model.skill.complete;

import game.model.AttackResult;
import game.model.Char;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.model.skill.Skill_AEO_Bulbasaur_5;
import game.render.Canvas;

public class Skill_Bulbasaur_Type3 extends SkillAnimate {
    int lvSkill;
    int ID;
    byte nDragon;
    byte loop;
    private static byte[] x2 = {0, 20, 20, 0, 0, 20, 20, 20, 20};
    private static byte[] y2 = {0, 0, 0, 20, 20, 20, 20, 20, 20};
    public static short[][] goc = {new short[]{90, 315, 225, 270, 315, 225, 90, 315, 225, 315, 225, 225}, new short[]{270, 135, 45, 90, 135, 45, 270, 135, 45, 135, 45, 45}, new short[]{180, 45, 315, 180, 45, 315, 180, 45, 315, 45, 315, 315}, new short[]{0, 135, 225, 0, 135, 225, 0, 135, 225, 135, 225, 225}};

    public Skill_Bulbasaur_Type3(int id) {
        super(-1);
        this.nDragon = (byte) 1;
        this.ID = id;
    }

    @Override
    public void setLvSkill(int lv) {
        this.lvSkill = lv;
    }

    @Override
    public int getX2() {
        this.nDragon = (byte) SkillAnimate.nDra[this.lvSkill];
        return SkillAnimate.nDra[this.lvSkill];
    }

    @Override
    public void updateSkill(Char c) {
        super.updateSkill(c);
        if (c.state == 0) {
            return;
        }
        if (c.p1 == 16) {
            this.loop = (byte) 0;
        }
        updateSkillDao(c);
        if (c.p1 == 9) {
            try {
                if (this.ID == 0) {
                    Canvas.gameScr.startNewMagicBeam(4, c, c.attackTarget, c.x + SkillAnimate.splashDuaX[c.dir] + x2[this.loop], c.y + SkillAnimate.splashDuaY[c.dir] + y2[this.loop], c.attkPower != 2000000 ? c.attkPower / this.nDragon : c.attkPower, c.attkEffect, goc[c.dir][this.loop]);
                    this.loop = (byte) (this.loop + 1);
                    if (this.loop < this.nDragon) {
                        c.p1 = (short) 8;
                    }
                } else {
                    Skill_AEO_Bulbasaur_5 skill = new Skill_AEO_Bulbasaur_5(c.x, c.y + 30, c.attackTarget, 2, true);
                    if (this.lvSkill > 0 && this.lvSkill < 7) {
                        skill.isDoubleDregon = false;
                    } else {
                        skill.isDoubleDregon = true;
                    }
                    if (c.attkPower != 0 && c.attkPower != 2000000) {
                        skill.textAttack[0] = new StringBuffer("-").append(c.attkPower).toString();
                    }
                    if (c.attkEffect != 0 && c.attkEffect < AttackResult.EFF_NAME.length) {
                        skill.textAttack[1] = AttackResult.EFF_NAME[c.attkEffect];
                    }
                    if (c.attackTarget.catagory == 1) {
                        ((Monster) c.attackTarget).isTarget = true;
                    }
                    EffectManager.hiEffects.addElement(skill);
                }
            } catch (Exception e) {
            }
        }
        c.p1 = (short) (c.p1 + 1);
    }

    @Override
    public void updateSkill(Monster c) {
    }
}
