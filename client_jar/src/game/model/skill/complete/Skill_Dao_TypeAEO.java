package game.model.skill.complete;

import game.model.AttackResult;
import game.model.Char;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.model.skill.Skill_AEO_DUA_DAO_5;
import game.model.skill.Skill_AEO_Dao_5;
import game.render.Canvas;
import java.util.Vector;

public class Skill_Dao_TypeAEO extends SkillAnimate {
    Vector mst;
    private boolean isSkill5;
    boolean isMonster;

    public Skill_Dao_TypeAEO(boolean isSkill5) {
        super(-1);
        this.isMonster = false;
        this.isSkill5 = isSkill5;
    }

    @Override
    public void setMonster(Vector mst) {
        this.mst = mst;
    }

    @Override
    public void setActorter(Vector mst) {
        this.mst = mst;
        this.isMonster = false;
    }

    @Override
    public void updateSkill(Char c) {
        c.frame = (byte) 4;
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
            c.frame = (byte) 4;
            c.weapon_frame = (short) 4;
        }
        if (c.p1 == 9 && this.mst != null) {
            if (!this.isSkill5) {
                if (c.attkPower == 0) {
                    Canvas.gameScr.startFlyText("MISS", 0, c.attackTarget.x + 0, c.attackTarget.y - 15, 1, -2);
                }
                for (int i = 0; i < this.mst.size(); i++) {
                    LiveActor mt = (LiveActor) this.mst.elementAt(i);
                    Skill_AEO_DUA_DAO_5 skill = new Skill_AEO_DUA_DAO_5(c.x + SkillAnimate.splashDuaX[c.dir], c.y + SkillAnimate.splashDuaY[c.dir], c, mt, (byte) 3);
                    if (c.attkPower != 0 && c.attkPower != 2000000) {
                        skill.textAttack[0] = new StringBuffer("-").append(c.attkPower).toString();
                    }
                    if (c.attkEffect != 0 && c.attkEffect < AttackResult.EFF_NAME.length) {
                        skill.textAttack[1] = AttackResult.EFF_NAME[c.attkEffect];
                    }
                    if (mt.catagory == 1) {
                        ((Monster) mt).isTarget = true;
                    }
                    EffectManager.hiEffects.addElement(skill);
                }
            } else {
                for (int i2 = 0; i2 < this.mst.size(); i2++) {
                    LiveActor lactor = (LiveActor) this.mst.elementAt(i2);
                    Skill_AEO_Dao_5 skill2 = new Skill_AEO_Dao_5(c.x, c.y + 30, lactor, 0, true);
                    if (c.attkPower != 0 && c.attkPower != 2000000) {
                        skill2.textAttack[0] = new StringBuffer("-").append(c.attkPower).toString();
                    }
                    if (c.attkEffect != 0) {
                        skill2.textAttack[1] = AttackResult.EFF_NAME[c.attkEffect];
                    }
                    if (lactor.catagory == 1) {
                        ((Monster) lactor).isTarget = true;
                    }
                    EffectManager.hiEffects.addElement(skill2);
                }
            }
        }
        c.p1 = (short) (c.p1 + 1);
    }

    @Override
    public void updateSkill(Monster c) {
    }
}
