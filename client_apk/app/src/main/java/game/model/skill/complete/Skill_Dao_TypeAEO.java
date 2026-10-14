package game.model.skill.complete;

import game.model.AttackResult;
import game.model.Char;
import game.model.Cout;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.model.skill.Skill_AEO_DUA_DAO_5;
import game.model.skill.Skill_AEO_Dao_5;
import game.render.GCanvas;
import java.util.Vector;

public class Skill_Dao_TypeAEO extends SkillAnimate {
    boolean isMonster;
    private boolean isSkill5;
    Vector mst;

    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Dao_TypeAEO(boolean z) {
        super(-1);
        this.isMonster = false;
        this.isSkill5 = z;
    }

    @Override
    public void setMonster(Vector vector) {
        this.mst = vector;
    }

    @Override
    public void setActorter(Vector vector) {
        this.mst = vector;
        this.isMonster = false;
    }

    @Override
    public void updateSkill(Char r15) {
        Vector vector;
        try {
            r15.frame = (byte) 4;
            if (r15.p1 == 16) {
                r15.state = (byte) 0;
                r15.p3 = (short) 0;
                r15.p2 = (short) 0;
                r15.p1 = (short) 0;
                r15.weapon_frame = (short) 0;
            } else if (r15.p1 >= 14 && r15.p1 < 16) {
                r15.frame = (byte) 5;
                r15.weapon_frame = (short) 7;
            } else if (r15.p1 == 13 || r15.p1 == 12) {
                r15.frame = (byte) 5;
                r15.weapon_frame = (short) 6;
            } else if (r15.p1 == 11 || r15.p1 == 10) {
                r15.frame = (byte) 4;
                r15.weapon_frame = (short) 5;
            } else {
                r15.frame = (byte) 4;
                r15.weapon_frame = (short) 4;
            }
            if (r15.p1 == 9 && (vector = this.mst) != null) {
                try {
                    if (!this.isSkill5) {
                        if (r15.attkPower == 0) {
                            GCanvas.gameScr.startFlyText("MISS", 0, r15.attackTarget.x, r15.attackTarget.y - 15, 1, -2);
                        }
                        int size = this.mst.size();
                        for (int i = 0; i < size; i++) {
                            LiveActor liveActor = (LiveActor) this.mst.elementAt(i);
                            if (r15.dir <= SkillAnimate.splashDuaX.length - 1 && r15.dir <= SkillAnimate.splashDuaY.length - 1) {
                                Skill_AEO_DUA_DAO_5 skill_AEO_DUA_DAO_5 = new Skill_AEO_DUA_DAO_5(SkillAnimate.splashDuaX[r15.dir] + r15.x, SkillAnimate.splashDuaY[r15.dir] + r15.y, r15, liveActor, (byte) 3);
                                if (r15.attkPower != 0 && r15.attkPower != 2000000) {
                                    skill_AEO_DUA_DAO_5.textAttack[0] = "-" + r15.attkPower;
                                }
                                if (r15.attkEffect != 0 && r15.attkEffect <= AttackResult.EFF_NAME.length - 1) {
                                    skill_AEO_DUA_DAO_5.textAttack[1] = AttackResult.EFF_NAME[r15.attkEffect];
                                }
                                if (liveActor.catagory == 1) {
                                    ((Monster) liveActor).isTarget = true;
                                }
                                EffectManager.hiEffects.addElement(skill_AEO_DUA_DAO_5);
                            }
                        }
                    } else {
                        int size2 = vector.size();
                        for (int i2 = 0; i2 < size2; i2++) {
                            LiveActor liveActor2 = (LiveActor) this.mst.elementAt(i2);
                            Skill_AEO_Dao_5 skill_AEO_Dao_5 = new Skill_AEO_Dao_5(r15.x, r15.y + 30, liveActor2, 0, true);
                            if (r15.attkPower != 0 && r15.attkPower != 2000000) {
                                skill_AEO_Dao_5.textAttack[0] = "-" + r15.attkPower;
                            }
                            if (r15.attkEffect != 0) {
                                skill_AEO_Dao_5.textAttack[1] = AttackResult.EFF_NAME[r15.attkEffect];
                            }
                            if (liveActor2.catagory == 1) {
                                ((Monster) liveActor2).isTarget = true;
                            }
                            EffectManager.hiEffects.addElement(skill_AEO_Dao_5);
                        }
                    }
                } catch (Exception unused) {
                }
            }
            r15.p1 = (short) (r15.p1 + 1);
        } catch (Exception unused2) {
            Cout.println("Loi dao type AEO");
        }
    }
}
