package game.model.skill.complete;

import game.model.Char;
import game.model.Cout;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.Canvas;

public class Skill_Charmander_Type3 extends SkillAnimate {
    private static byte[] x2 = {0, 20, 20, 0, 0, 20, 20, 20, 20, 20, 20, 20, 20};
    private static byte[] y2 = {0, 0, 0, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20};
    private static short[][][] angle = {new short[][]{new short[]{90, 315, 225}, new short[]{90, 315, 225}, new short[]{90, 315, 225}, new short[]{90, 315, 225, 270}, new short[]{90, 315, 225, 270}, new short[]{90, 315, 225, 270, 45}, new short[]{90, 315, 225, 270, 45, 135}, new short[]{90, 315, 225, 270, 45, 135, 180}, new short[]{90, 315, 225, 270, 45, 135, 0}, new short[]{90, 315, 225, 270, 45, 135, 0}, new short[]{90, 315, 225, 270, 45, 135, 0}}, new short[][]{new short[]{270, 135, 45}, new short[]{270, 135, 45}, new short[]{270, 135, 45}, new short[]{270, 135, 45, 90}, new short[]{270, 135, 45, 90}, new short[]{270, 135, 45, 90, 315}, new short[]{270, 135, 45, 90, 315, 235}, new short[]{270, 135, 45, 90, 315, 235, 180}, new short[]{270, 135, 45, 90, 315, 235, 0}, new short[]{90, 315, 225, 270, 45, 135, 0}, new short[]{90, 315, 225, 270, 45, 135, 0}}, new short[][]{new short[]{180, 45, 315}, new short[]{180, 45, 315}, new short[]{180, 45, 315}, new short[]{180, 45, 315, 0}, new short[]{180, 45, 315, 0}, new short[]{180, 45, 315, 0, 135}, new short[]{180, 45, 315, 0, 135, 225}, new short[]{180, 45, 315, 0, 135, 225, 270}, new short[]{180, 45, 315, 0, 135, 225, 270, 90}, new short[]{90, 315, 225, 270, 45, 135, 0}, new short[]{90, 315, 225, 270, 45, 135, 0}}, new short[][]{new short[]{0, 135, 225}, new short[]{0, 135, 225}, new short[]{0, 135, 225}, new short[]{0, 135, 225, 180}, new short[]{0, 135, 225, 180}, new short[]{0, 135, 225, 180, 45}, new short[]{0, 135, 225, 180, 45, 90}, new short[]{0, 135, 225, 180, 45, 90, 270}, new short[]{0, 135, 225, 180, 45, 90, 270, 315}, new short[]{90, 315, 225, 270, 45, 135, 0}, new short[]{90, 315, 225, 270, 45, 135, 0}}};
    int lvSkill;
    int ID;
    byte nDragon;
    int loop;

    public Skill_Charmander_Type3(int id) {
        super(-1);
        this.nDragon = (byte) 1;
        this.loop = 0;
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
        try {
            if (c.state == 0) {
                return;
            }
            if (c.p1 == 16) {
                this.loop = 0;
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
                    if (this.ID == 1) {
                        EffectManager.addHiEffect(c.x, c.y - 15, 24);
                    }
                }
                c.frame = (byte) 4;
                c.weapon_frame = (short) 4;
            }
            if (c.p1 == 9 && this.lvSkill > 0) {
                if (this.ID == 0) {
                    for (int i = 0; i < angle[c.dir][this.lvSkill - 1].length; i++) {
                        Canvas.gameScr.startNewMagicBeam(1, c, c.attackTarget, c.x + SkillAnimate.splashKiemX[c.dir] + x2[i], c.y + SkillAnimate.splashKiemY[c.dir] + y2[i], c.attkPower != 2000000 ? c.attkPower / this.nDragon : c.attkPower, c.attkEffect, angle[c.dir][this.lvSkill - 1][i]);
                    }
                } else if (this.ID == 1) {
                    if (this.loop <= (this.lvSkill - 1) / 2) {
                        Canvas.gameScr.startNewArrow(7, c, c.attackTarget, c.x + SkillAnimate.splashKiemX[c.dir], c.y - 25, c.attkPower != 2000000 ? c.attkPower / this.nDragon : c.attkPower, c.attkEffect, 7);
                        Canvas.gameScr.startNewArrow(7, c, c.attackTarget, c.x + SkillAnimate.splashKiemX[c.dir] + 10, c.y - 15, c.attkPower != 2000000 ? c.attkPower / this.nDragon : c.attkPower, c.attkEffect, 7);
                        Canvas.gameScr.startNewArrow(8, c, c.attackTarget, (c.x + SkillAnimate.splashKiemX[c.dir]) - 10, c.y - 55, c.attkPower != 2000000 ? c.attkPower / this.nDragon : c.attkPower, c.attkEffect, 7);
                        Canvas.gameScr.startNewArrow(1, c, c.attackTarget, c.x + SkillAnimate.splashKiemX[c.dir] + 20, c.y - 35, c.attkPower != 2000000 ? c.attkPower / this.nDragon : c.attkPower, c.attkEffect, 7);
                        Canvas.gameScr.startNewArrow(1, c, c.attackTarget, (c.x + SkillAnimate.splashKiemX[c.dir]) - 20, c.y - 45, c.attkPower != 2000000 ? c.attkPower / this.nDragon : c.attkPower, c.attkEffect, 7);
                    }
                    this.loop++;
                    if (this.loop < this.nDragon) {
                        c.p1 = (short) 7;
                    }
                }
            }
            c.p1 = (short) (c.p1 + 1);
        } catch (Exception e) {
            Cout.println("error kiem");
        }
    }

    @Override
    public void updateSkill(Monster c) {
    }
}
