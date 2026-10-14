package game.model.skill.complete;

import game.model.Char;
import game.model.Cout;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;

public class Skill_Kiem_Type3 extends SkillAnimate {
    int ID;
    int loop;
    int lvSkill;
    byte nDragon;
    private static byte[] x2 = {0, 20, 20, 0, 0, 20, 20, 20, 20, 20, 20, 20, 20};
    private static byte[] y2 = {0, 0, 0, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20};
    private static short[][][] angle = {new short[][]{new short[]{90, 315, 225}, new short[]{90, 315, 225}, new short[]{90, 315, 225}, new short[]{90, 315, 225, 270}, new short[]{90, 315, 225, 270}, new short[]{90, 315, 225, 270, 45}, new short[]{90, 315, 225, 270, 45, 135}, new short[]{90, 315, 225, 270, 45, 135, 180}, new short[]{90, 315, 225, 270, 45, 135, 0}, new short[]{90, 315, 225, 270, 45, 135, 0}, new short[]{90, 315, 225, 270, 45, 135, 0}}, new short[][]{new short[]{270, 135, 45}, new short[]{270, 135, 45}, new short[]{270, 135, 45}, new short[]{270, 135, 45, 90}, new short[]{270, 135, 45, 90}, new short[]{270, 135, 45, 90, 315}, new short[]{270, 135, 45, 90, 315, 235}, new short[]{270, 135, 45, 90, 315, 235, 180}, new short[]{270, 135, 45, 90, 315, 235, 0}, new short[]{90, 315, 225, 270, 45, 135, 0}, new short[]{90, 315, 225, 270, 45, 135, 0}}, new short[][]{new short[]{180, 45, 315}, new short[]{180, 45, 315}, new short[]{180, 45, 315}, new short[]{180, 45, 315, 0}, new short[]{180, 45, 315, 0}, new short[]{180, 45, 315, 0, 135}, new short[]{180, 45, 315, 0, 135, 225}, new short[]{180, 45, 315, 0, 135, 225, 270}, new short[]{180, 45, 315, 0, 135, 225, 270, 90}, new short[]{90, 315, 225, 270, 45, 135, 0}, new short[]{90, 315, 225, 270, 45, 135, 0}}, new short[][]{new short[]{0, 135, 225}, new short[]{0, 135, 225}, new short[]{0, 135, 225}, new short[]{0, 135, 225, 180}, new short[]{0, 135, 225, 180}, new short[]{0, 135, 225, 180, 45}, new short[]{0, 135, 225, 180, 45, 90}, new short[]{0, 135, 225, 180, 45, 90, 270}, new short[]{0, 135, 225, 180, 45, 90, 270, 315}, new short[]{90, 315, 225, 270, 45, 135, 0}, new short[]{90, 315, 225, 270, 45, 135, 0}}};

    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Kiem_Type3(int i) {
        super(-1);
        this.nDragon = (byte) 1;
        this.loop = 0;
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
    public void updateSkill(Char r16) {
        int i;
        super.updateSkill(r16);
        try {
            if (r16.state == 0) {
                return;
            }
            if (r16.p1 == 16) {
                this.loop = 0;
            }
            if (r16.p1 == 16) {
                r16.state = (byte) 0;
                r16.p3 = (short) 0;
                r16.p2 = (short) 0;
                r16.p1 = (short) 0;
                r16.weapon_frame = (short) -1;
            } else if (r16.p1 >= 14 && r16.p1 < 16) {
                r16.frame = (byte) 5;
                r16.weapon_frame = (short) 7;
            } else if (r16.p1 == 13 || r16.p1 == 12) {
                r16.frame = (byte) 5;
                r16.weapon_frame = (short) 6;
            } else if (r16.p1 == 11 || r16.p1 == 10) {
                r16.frame = (byte) 4;
                r16.weapon_frame = (short) 5;
            } else {
                try {
                    if (r16.p1 % 2 == 0) {
                        EffectManager.addHiEffect(r16.x + splashKiemX[r16.dir], r16.y + splashKiemY[r16.dir], 3);
                        if (this.ID == 1) {
                            EffectManager.addHiEffect(r16.x, r16.y - 15, 24);
                        }
                    }
                    r16.frame = (byte) 4;
                    r16.weapon_frame = (short) 4;
                } catch (Exception unused) {
                }
            }
            try {
                if (r16.p1 == 9 && (i = this.lvSkill) > 0) {
                    int i2 = this.ID;
                    if (i2 == 0) {
                        for (int i3 = 0; i3 < angle[r16.dir][this.lvSkill - 1].length; i3++) {
                            GCanvas.gameScr.startNewMagicBeam(1, r16, r16.attackTarget, r16.x + splashKiemX[r16.dir] + x2[i3], r16.y + splashKiemY[r16.dir] + y2[i3], r16.attkPower != 2000000 ? r16.attkPower / this.nDragon : r16.attkPower, r16.attkEffect, angle[r16.dir][this.lvSkill - 1][i3]);
                        }
                    } else if (i2 == 1) {
                        if (this.loop <= (i - 1) / 2) {
                            GCanvas.gameScr.startNewArrow(7, r16, r16.attackTarget, r16.x + splashKiemX[r16.dir], r16.y - 25, r16.attkPower != 2000000 ? r16.attkPower / this.nDragon : r16.attkPower, r16.attkEffect, 7);
                            GCanvas.gameScr.startNewArrow(7, r16, r16.attackTarget, r16.x + splashKiemX[r16.dir] + 10, r16.y - 15, r16.attkPower != 2000000 ? r16.attkPower / this.nDragon : r16.attkPower, r16.attkEffect, 7);
                            GCanvas.gameScr.startNewArrow(8, r16, r16.attackTarget, (r16.x + splashKiemX[r16.dir]) - 10, r16.y - 55, r16.attkPower != 2000000 ? r16.attkPower / this.nDragon : r16.attkPower, r16.attkEffect, 7);
                            GCanvas.gameScr.startNewArrow(1, r16, r16.attackTarget, r16.x + splashKiemX[r16.dir] + 20, r16.y - 35, r16.attkPower != 2000000 ? r16.attkPower / this.nDragon : r16.attkPower, r16.attkEffect, 7);
                            GCanvas.gameScr.startNewArrow(1, r16, r16.attackTarget, (r16.x + splashKiemX[r16.dir]) - 20, r16.y - 45, r16.attkPower != 2000000 ? r16.attkPower / this.nDragon : r16.attkPower, r16.attkEffect, 7);
                        }
                        int i4 = this.loop + 1;
                        this.loop = i4;
                        if (i4 < this.nDragon) {
                            r16.p1 = (short) 7;
                        }
                    }
                }
            } catch (Exception unused2) {
            }
            r16.p1 = (short) (r16.p1 + 1);
        } catch (Exception unused3) {
            Cout.println("error kiem");
        }
    }
}
