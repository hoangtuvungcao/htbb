package game.model.skill;

import game.model.AttackResult;
import game.model.Char;
import game.model.EffectManager;
import game.model.Monster;
import game.render.Canvas;
import java.util.Random;
import java.util.Vector;

public abstract class SkillAnimate {
    int x;
    int y;
    int attackPower;
    public int dam;
    public static int[] nDra = {3, 3, 3, 3, 4, 4, 5, 6, 7, 8, 8};
    protected static final byte[] splashKiemX = {-2, 2, 20, -20};
    protected static final byte[] splashKiemY = {-30, -30, -30, -30};
    protected static final byte[] splashDuaX = {-2, 2, -8, 8};
    protected static final byte[] splashDuaY = {-10, -30, -10, -10};
    protected static final byte[] splashCungX = {-2, 2, -14, 14};
    protected static final byte[] splashCungY = {-2, -28, -10, -10};
    protected static final byte[] splashBuaX = {-10, 10, 10, -10};
    protected static final byte[] splashBuaY = {-30, -26, -30, -30};
    protected static final byte[] rotateSplashDaoX = {-30, -15, 0, 15, 30, 15, 0, -15};
    protected static final byte[] rotateSplashDaoY = {0, 13, 20, 13, 0, -13, -20, -13};

    public void updateSkill(Char c) {
        if (c.attackTarget == null) {
            c.state = (byte) 0;
            c.p3 = (short) 0;
            c.p2 = (short) 0;
            c.p1 = (short) 0;
            c.weapon_frame = (short) -1;
        }
        c.frame = (byte) 4;
    }

    public void updateSkill(Monster c) {
    }

    public void setMonster(Vector mst) {
    }

    public void setActorter(Vector mst) {
    }

    public int getX2() {
        return 1;
    }

    public void setLvSkill(int lv) {
    }

    public SkillAnimate(int skillWeapon) {
    }

    public void updateStateBua(Char c, int idSkill, int ID) {
        if (c.p1 == 20) {
            c.state = (byte) 0;
            c.p3 = (short) 0;
            c.p2 = (short) 0;
            c.p1 = (short) 0;
            c.weapon_frame = (short) 0;
            return;
        }
        if (c.p1 == 17 || c.p1 == 16 || c.p1 == 18 || c.p1 == 19) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 7;
            return;
        }
        if (c.p1 == 15 || c.p1 == 14) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 6;
            return;
        }
        if (c.p1 == 13 || c.p1 == 12) {
            c.frame = (byte) 4;
            c.weapon_frame = (short) 5;
            return;
        }
        c.frame = (byte) 4;
        c.weapon_frame = (short) 4;
        if (c.p1 < 6) {
            EffectManager.addLowEffect(c.x - (20 - c.p1), c.y - 4, 4);
            EffectManager.addLowEffect(c.x + (20 - c.p1), c.y - 4, 4);
            EffectManager.addLowEffect(c.x, (c.y - 4) + (20 - c.p1), 4);
            if (ID == 1) {
                Canvas.gameScr.timerung = 20;
                EffectManager.addLowEffect(c.attackTarget.x - (20 - c.p1), c.attackTarget.y - 4, 4);
                EffectManager.addLowEffect(c.attackTarget.x + (20 - c.p1), c.attackTarget.y - 4, 4);
                EffectManager.addLowEffect(c.attackTarget.x, (c.attackTarget.y - 4) - (20 - c.p1), 4);
                EffectManager.addLowEffect(c.attackTarget.x, (c.attackTarget.y - 4) + (20 - c.p1), 4);
            }
        }
        if (c.p1 % 3 == 0) {
            EffectManager.addHiEffect(c.x + splashBuaX[c.dir], c.y + splashBuaY[c.dir], 11);
        }
    }

    public void updateSkillBua_2_3(Char c, boolean is3, int nDragon, int ID, int lvSkill) {
        updateStateBua(c, 4, ID);
        if (c.p1 == 13) {
            Random r = new Random(System.currentTimeMillis());
            int rdx = 0;
            int rdy = 0;
            if (is3) {
                rdx = r.nextInt() % 20;
                rdy = rdx;
            }
            if ((c.dir == 0 || c.dir == 1) && is3) {
                rdy = 0;
            } else {
                rdx = 0;
            }
            if (c.attackTarget != null) {
                if (c.attackTarget.catagory == 1) {
                    if (c.attkEffect == 0) {
                        EffectManager.addHiEffect(c.attackTarget.x + rdx, (c.attackTarget.y - 10) + rdy, 11);
                        ((Monster) c.attackTarget).jump();
                    } else if (c.attkEffect == 2) {
                        EffectManager.addHiEffect(c.attackTarget.x + rdx, (c.attackTarget.y - 10) + rdy, 12);
                        ((Monster) c.attackTarget).doublejump();
                    }
                }
                c.attackTarget.realHPSyncTime = 2;
            }
            if (ID == 0) {
                EffectManager.addLowEffect((c.attackTarget.x - 10) + rdx, (c.attackTarget.y - 25) + rdy, 14);
                EffectManager.addLowEffect(c.attackTarget.x + 10 + rdx, (c.attackTarget.y - 25) + rdy, 14);
                EffectManager.addLowEffect(c.attackTarget.x + rdx, ((c.attackTarget.y - 25) - 10) + rdy, 14);
            }
            if (c.attkPower != 0 && c.attkPower != 2000000) {
                Canvas.gameScr.startFlyText(new StringBuffer("-").append(c.attkPower / nDragon).toString(), 0, c.attackTarget.x + 0, c.attackTarget.y - 15, -1, -2);
            }
            if (c.attkEffect != 0 && c.attkEffect < AttackResult.EFF_NAME.length) {
                Canvas.gameScr.startFlyText(AttackResult.EFF_NAME[c.attkEffect], 0, c.attackTarget.x + 0, c.attackTarget.y - 15, 2, -2);
            }
        }
    }

    public void updateSkillCung_2_3(Char c) {
        if (c.p1 == 15) {
            c.state = (byte) 0;
            c.p3 = (short) 0;
            c.p2 = (short) 0;
            c.p1 = (short) 0;
            c.weapon_frame = (short) 0;
            return;
        }
        if (c.p1 == 14 || c.p1 == 13) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 7;
            return;
        }
        if (c.p1 == 12 || c.p1 == 11) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 6;
            return;
        }
        if (c.p1 == 10 || c.p1 == 9) {
            c.frame = (byte) 4;
            c.weapon_frame = (short) 5;
            return;
        }
        if (c.p1 % 5 == 0) {
            EffectManager.addHiEffect(c.x + splashCungX[c.dir], c.y + splashCungY[c.dir], 16);
        }
        if (c.p1 % 7 == 0) {
            EffectManager.addLowEffect(((c.x + splashCungX[c.dir]) + 30) - c.p1, c.y + splashCungY[c.dir], 17);
            EffectManager.addLowEffect(((c.x + splashCungX[c.dir]) - 30) + c.p1, c.y + splashCungY[c.dir], 17);
            EffectManager.addLowEffect(c.x + splashCungX[c.dir], ((c.y + splashCungY[c.dir]) + 30) - c.p1, 17);
            EffectManager.addLowEffect(c.x + splashCungX[c.dir], ((c.y + splashCungY[c.dir]) - 30) + c.p1, 17);
        }
        c.frame = (byte) 4;
        c.weapon_frame = (short) 4;
    }

    public void updateSkillDao(Char c) {
        if (c.p1 == 16) {
            c.state = (byte) 0;
            c.p3 = (short) 0;
            c.p2 = (short) 0;
            c.p1 = (short) 0;
            c.weapon_frame = (short) 0;
            return;
        }
        if (c.p1 >= 14 && c.p1 < 16) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 7;
            return;
        }
        if (c.p1 == 13 || c.p1 == 12) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 6;
            return;
        }
        if (c.p1 == 11 || c.p1 == 10) {
            c.frame = (byte) 4;
            c.weapon_frame = (short) 5;
            return;
        }
        if (c.p1 < 4) {
            EffectManager.addHiEffect(c.x + rotateSplashDaoX[c.p1], (c.y - 10) + rotateSplashDaoY[c.p1], 15);
        } else if (c.p1 < 8) {
            EffectManager.addLowEffect(c.x + rotateSplashDaoX[c.p1], (c.y - 10) + rotateSplashDaoY[c.p1], 15);
        }
        c.frame = (byte) 4;
        c.weapon_frame = (short) 4;
    }

    public void updateSkillDua(Char c) {
        if (c.p1 == 8) {
            c.state = (byte) 0;
            c.p3 = (short) 0;
            c.p2 = (short) 0;
            c.p1 = (short) 0;
            c.weapon_frame = (short) 0;
            return;
        }
        if (c.p1 >= 6 && c.p1 < 8) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 7;
            return;
        }
        if (c.p1 == 5 || c.p1 == 4) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 6;
        } else if (c.p1 == 3 || c.p1 == 2) {
            c.frame = (byte) 4;
            c.weapon_frame = (short) 5;
        } else {
            c.frame = (byte) 4;
            c.weapon_frame = (short) 4;
        }
    }

    public void updateSkillKiem(Char c) {
        if (c.p1 == 8) {
            c.state = (byte) 0;
            c.p3 = (short) 0;
            c.p2 = (short) 0;
            c.p1 = (short) 0;
            c.weapon_frame = (short) 0;
            return;
        }
        if (c.p1 == 7 || c.p1 == 6) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 7;
            return;
        }
        if (c.p1 == 5 || c.p1 == 4) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 6;
        } else if (c.p1 == 3 || c.p1 == 2) {
            c.frame = (byte) 4;
            c.weapon_frame = (short) 5;
        } else {
            c.frame = (byte) 4;
            c.weapon_frame = (short) 4;
        }
    }
}
