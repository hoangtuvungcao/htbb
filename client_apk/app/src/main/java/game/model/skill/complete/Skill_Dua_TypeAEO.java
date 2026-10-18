package game.model.skill.complete;

import game.model.AttackResult;
import game.model.Char;
import game.model.Cout;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.model.skill.Skill_AEO_DUA_DAO_5;
import game.render.GCanvas;
import java.util.Vector;

public class Skill_Dua_TypeAEO extends SkillAnimate {
    private boolean isSkill5;
    Vector mst;

    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Dua_TypeAEO(boolean z) {
        super(-1);
        this.mst = new Vector();
        this.isSkill5 = z;
    }

    @Override
    public void setMonster(Vector vector) {
        this.mst = vector;
    }

    @Override
    public void updateSkill(Char r14) {
        try {
            r14.frame = (byte) 4;
            if (r14.p1 == 18) {
                r14.state = (byte) 0;
                r14.p3 = (short) 0;
                r14.p2 = (short) 0;
                r14.p1 = (short) 0;
                r14.weapon_frame = (short) 0;
            } else if (r14.p1 >= 6 && r14.p1 < 18) {
                r14.frame = (byte) 5;
                r14.weapon_frame = (short) 7;
            } else if (r14.p1 == 5 || r14.p1 == 4) {
                r14.frame = (byte) 5;
                r14.weapon_frame = (short) 6;
            } else if (r14.p1 == 3 || r14.p1 == 2) {
                r14.frame = (byte) 4;
                r14.weapon_frame = (short) 5;
            } else {
                r14.frame = (byte) 4;
                r14.weapon_frame = (short) 4;
            }
            if (r14.p1 == 3) {
                try {
                    if (!this.isSkill5) {
                        int size = this.mst.size();
                        for (int i = 0; i < size; i++) {
                            LiveActor liveActor = (LiveActor) this.mst.elementAt(i);
                            if (r14.dir <= splashDuaX.length - 1 && r14.dir <= splashDuaY.length - 1) {
                                GCanvas.gameScr.startNewMagicBeam(5, r14, liveActor, r14.x + splashDuaX[r14.dir], r14.y + splashDuaY[r14.dir], r14.attkPower, r14.attkEffect);
                            }
                        }
                    } else {
                        int size2 = this.mst.size();
                        for (int i2 = 0; i2 < size2; i2++) {
                            LiveActor liveActor2 = (LiveActor) this.mst.elementAt(i2);
                            if (r14.dir <= splashDuaX.length - 1 && r14.dir <= splashDuaY.length - 1) {
                                Skill_AEO_DUA_DAO_5 skill_AEO_DUA_DAO_5 = new Skill_AEO_DUA_DAO_5(splashDuaX[r14.dir] + r14.x, splashDuaY[r14.dir] + r14.y, r14, liveActor2, (byte) 4);
                                if (r14.attkPower != 0 && r14.attkPower != 2000000) {
                                    skill_AEO_DUA_DAO_5.textAttack[0] = "-" + r14.attkPower;
                                }
                                if (r14.attkEffect != 0 && r14.attkEffect <= AttackResult.EFF_NAME.length - 1) {
                                    skill_AEO_DUA_DAO_5.textAttack[1] = AttackResult.EFF_NAME[r14.attkEffect];
                                }
                                if (liveActor2.catagory == 1) {
                                    ((Monster) liveActor2).isTarget = true;
                                }
                                EffectManager.hiEffects.addElement(skill_AEO_DUA_DAO_5);
                            }
                        }
                    }
                } catch (Exception unused) {
                }
            }
            r14.p1 = (short) (r14.p1 + 1);
        } catch (Exception unused2) {
            Cout.println("Loi skill type dua AEO");
        }
    }
}
