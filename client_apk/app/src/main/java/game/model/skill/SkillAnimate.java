package game.model.skill;

import game.model.AttackResult;
import game.model.Char;
import game.model.EffectManager;
import game.model.Monster;
import game.render.GCanvas;
import java.util.Random;
import java.util.Vector;

public abstract class SkillAnimate {
    int attackPower;
    public int dam;
    int x;
    int y;
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

    public int getX2() {
        return 1;
    }

    public void setActorter(Vector vector) {
    }

    public void setLvSkill(int i) {
    }

    public void setMonster(Vector vector) {
    }

    public void updateSkill(Monster monster) {
    }

    public void updateSkill(Char r2) {
        if (r2.attackTarget == null) {
            r2.state = (byte) 0;
            r2.p3 = (short) 0;
            r2.p2 = (short) 0;
            r2.p1 = (short) 0;
            r2.weapon_frame = (short) -1;
        }
        r2.frame = (byte) 4;
    }

    public SkillAnimate(int i) {
    }

    public void updateStateBua(Char r6, int i, int i2) {
        try {
            if (r6.p1 == 20) {
                r6.state = (byte) 0;
                r6.p3 = (short) 0;
                r6.p2 = (short) 0;
                r6.p1 = (short) 0;
                r6.weapon_frame = (short) 0;
                return;
            }
            if (r6.p1 != 17 && r6.p1 != 16 && r6.p1 != 18 && r6.p1 != 19) {
                if (r6.p1 != 15 && r6.p1 != 14) {
                    if (r6.p1 != 13 && r6.p1 != 12) {
                        r6.frame = (byte) 4;
                        r6.weapon_frame = (short) 4;
                        if (r6.p1 < 6) {
                            EffectManager.addLowEffect(r6.x - (20 - r6.p1), r6.y - 4, 4);
                            EffectManager.addLowEffect(r6.x + (20 - r6.p1), r6.y - 4, 4);
                            EffectManager.addLowEffect(r6.x, (r6.y - 4) + (20 - r6.p1), 4);
                            if (i2 == 1) {
                                GCanvas.gameScr.timerung = 20;
                                EffectManager.addLowEffect(r6.attackTarget.x - (20 - r6.p1), r6.attackTarget.y - 4, 4);
                                EffectManager.addLowEffect(r6.attackTarget.x + (20 - r6.p1), r6.attackTarget.y - 4, 4);
                                EffectManager.addLowEffect(r6.attackTarget.x, (r6.attackTarget.y - 4) - (20 - r6.p1), 4);
                                EffectManager.addLowEffect(r6.attackTarget.x, (r6.attackTarget.y - 4) + (20 - r6.p1), 4);
                            }
                        }
                        if (r6.p1 % 3 == 0) {
                            EffectManager.addHiEffect(r6.x + splashBuaX[r6.dir], r6.y + splashBuaY[r6.dir], 11);
                            return;
                        }
                        return;
                    }
                    r6.frame = (byte) 4;
                    r6.weapon_frame = (short) 5;
                    return;
                }
                r6.frame = (byte) 5;
                r6.weapon_frame = (short) 6;
                return;
            }
            r6.frame = (byte) 5;
            r6.weapon_frame = (short) 7;
        } catch (Exception unused) {
        }
    }

    public void updateSkillBua_2_3(Char r8, boolean z, int i, int i2, int i3) {
        try {
            updateStateBua(r8, 4, i2);
            if (r8.p1 == 13) {
                int i4 = 0;
                int iNextInt = z ? new Random(System.currentTimeMillis()).nextInt() % 20 : 0;
                int i5 = iNextInt;
                if ((r8.dir == 0 || r8.dir == 1) && z) {
                    i5 = 0;
                    i4 = iNextInt;
                }
                if (r8.attackTarget != null) {
                    if (r8.attackTarget.catagory == 1) {
                        try {
                            if (r8.attkEffect == 0) {
                                EffectManager.addHiEffect(r8.attackTarget.x + i4, (r8.attackTarget.y - 10) + i5, 11);
                                ((Monster) r8.attackTarget).jump();
                            } else if (r8.attkEffect == 2) {
                                EffectManager.addHiEffect(r8.attackTarget.x + i4, (r8.attackTarget.y - 10) + i5, 12);
                                ((Monster) r8.attackTarget).doublejump();
                            }
                        } catch (Exception unused) {
                        }
                    }
                    r8.attackTarget.realHPSyncTime = 2;
                }
                if (i2 == 0) {
                    try {
                        EffectManager.addLowEffect((r8.attackTarget.x - 10) + i4, (r8.attackTarget.y - 25) + i5, 14);
                        EffectManager.addLowEffect(r8.attackTarget.x + 10 + i4, (r8.attackTarget.y - 25) + i5, 14);
                        EffectManager.addLowEffect(r8.attackTarget.x + i4, (r8.attackTarget.y - 35) + i5, 14);
                    } catch (Exception unused2) {
                    }
                }
                if (r8.attkPower != 0 && r8.attkPower != 2000000) {
                    GCanvas.gameScr.startFlyText("-" + (r8.attkPower / i), 0, r8.attackTarget.x, r8.attackTarget.y - 15, -1, -2);
                }
                if (r8.attkEffect == 0 || r8.attkEffect >= AttackResult.EFF_NAME.length) {
                    return;
                }
                GCanvas.gameScr.startFlyText(AttackResult.EFF_NAME[r8.attkEffect], 0, r8.attackTarget.x, r8.attackTarget.y - 15, 2, -2);
            }
        } catch (Exception unused3) {
        }
    }

    public void updateSkillCung_2_3(Char r8) {
        try {
            if (r8.p1 == 15) {
                r8.state = (byte) 0;
                r8.p3 = (short) 0;
                r8.p2 = (short) 0;
                r8.p1 = (short) 0;
                r8.weapon_frame = (short) 0;
                return;
            }
            if (r8.p1 != 14 && r8.p1 != 13) {
                if (r8.p1 != 12 && r8.p1 != 11) {
                    if (r8.p1 != 10 && r8.p1 != 9) {
                        if (r8.p1 % 5 == 0) {
                            EffectManager.addHiEffect(r8.x + splashCungX[r8.dir], r8.y + splashCungY[r8.dir], 16);
                        }
                        if (r8.p1 % 7 == 0) {
                            short s = r8.x;
                            byte[] bArr = splashCungX;
                            int i = ((s + bArr[r8.dir]) + 30) - r8.p1;
                            short s2 = r8.y;
                            byte[] bArr2 = splashCungY;
                            EffectManager.addLowEffect(i, s2 + bArr2[r8.dir], 17);
                            EffectManager.addLowEffect(((r8.x + bArr[r8.dir]) - 30) + r8.p1, r8.y + bArr2[r8.dir], 17);
                            EffectManager.addLowEffect(r8.x + bArr[r8.dir], ((r8.y + bArr2[r8.dir]) + 30) - r8.p1, 17);
                            EffectManager.addLowEffect(r8.x + bArr[r8.dir], ((r8.y + bArr2[r8.dir]) - 30) + r8.p1, 17);
                        }
                        r8.frame = (byte) 4;
                        r8.weapon_frame = (short) 4;
                        return;
                    }
                    r8.frame = (byte) 4;
                    r8.weapon_frame = (short) 5;
                    return;
                }
                r8.frame = (byte) 5;
                r8.weapon_frame = (short) 6;
                return;
            }
            r8.frame = (byte) 5;
            r8.weapon_frame = (short) 7;
        } catch (Exception unused) {
        }
    }

    public void updateSkillDao(Char r7) {
        try {
            if (r7.p1 == 16) {
                r7.state = (byte) 0;
                r7.p3 = (short) 0;
                r7.p2 = (short) 0;
                r7.p1 = (short) 0;
                r7.weapon_frame = (short) 0;
                return;
            }
            if (r7.p1 >= 14 && r7.p1 < 16) {
                r7.frame = (byte) 5;
                r7.weapon_frame = (short) 7;
                return;
            }
            if (r7.p1 != 13 && r7.p1 != 12) {
                if (r7.p1 != 11 && r7.p1 != 10) {
                    if (r7.p1 < 4) {
                        EffectManager.addHiEffect(r7.x + rotateSplashDaoX[r7.p1], (r7.y - 10) + rotateSplashDaoY[r7.p1], 15);
                    } else if (r7.p1 < 8) {
                        EffectManager.addLowEffect(r7.x + rotateSplashDaoX[r7.p1], (r7.y - 10) + rotateSplashDaoY[r7.p1], 15);
                    }
                    r7.frame = (byte) 4;
                    r7.weapon_frame = (short) 4;
                    return;
                }
                r7.frame = (byte) 4;
                r7.weapon_frame = (short) 5;
                return;
            }
            r7.frame = (byte) 5;
            r7.weapon_frame = (short) 6;
        } catch (Exception unused) {
        }
    }

    public void updateSkillDua(Char r5) {
        if (r5.p1 == 8) {
            r5.state = (byte) 0;
            r5.p3 = (short) 0;
            r5.p2 = (short) 0;
            r5.p1 = (short) 0;
            r5.weapon_frame = (short) 0;
            return;
        }
        if (r5.p1 >= 6 && r5.p1 < 8) {
            r5.frame = (byte) 5;
            r5.weapon_frame = (short) 7;
            return;
        }
        if (r5.p1 == 5 || r5.p1 == 4) {
            r5.frame = (byte) 5;
            r5.weapon_frame = (short) 6;
        } else if (r5.p1 == 3 || r5.p1 == 2) {
            r5.frame = (byte) 4;
            r5.weapon_frame = (short) 5;
        } else {
            r5.frame = (byte) 4;
            r5.weapon_frame = (short) 4;
        }
    }

    public void updateSkillKiem(Char r5) {
        if (r5.p1 == 8) {
            r5.state = (byte) 0;
            r5.p3 = (short) 0;
            r5.p2 = (short) 0;
            r5.p1 = (short) 0;
            r5.weapon_frame = (short) 0;
            return;
        }
        if (r5.p1 == 7 || r5.p1 == 6) {
            r5.frame = (byte) 5;
            r5.weapon_frame = (short) 7;
            return;
        }
        if (r5.p1 == 5 || r5.p1 == 4) {
            r5.frame = (byte) 5;
            r5.weapon_frame = (short) 6;
        } else if (r5.p1 == 3 || r5.p1 == 2) {
            r5.frame = (byte) 4;
            r5.weapon_frame = (short) 5;
        } else {
            r5.frame = (byte) 4;
            r5.weapon_frame = (short) 4;
        }
    }
}
