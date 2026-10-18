package game.model.skill.complete;

import game.model.AttackResult;
import game.model.Char;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.model.skill.Skill_AEO_Eevee_Bulbasaur_5;
import game.render.Canvas;
import java.util.Vector;

public class Skill_Eevee_TypeAEO extends SkillAnimate {
    private boolean isSkill5;
    Vector mst;

    public Skill_Eevee_TypeAEO(boolean isSkill5) {
        super(-1);
        this.mst = new Vector();
        this.isSkill5 = isSkill5;
    }

    @Override
    public void setMonster(Vector mst) {
        this.mst = mst;
    }

    @Override
    public void updateSkill(Char c) {
        c.frame = (byte) 4;
        if (c.p1 == 18) {
            c.state = (byte) 0;
            c.p3 = (short) 0;
            c.p2 = (short) 0;
            c.p1 = (short) 0;
            c.weapon_frame = (short) 0;
        } else if (c.p1 >= 6 && c.p1 < 18) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 7;
        } else if (c.p1 == 5 || c.p1 == 4) {
            c.frame = (byte) 5;
            c.weapon_frame = (short) 6;
        } else if (c.p1 == 3 || c.p1 == 2) {
            c.frame = (byte) 4;
            c.weapon_frame = (short) 5;
        } else {
            c.frame = (byte) 4;
            c.weapon_frame = (short) 4;
        }
        if (c.p1 == 3) {
            if (!this.isSkill5) {
                for (int i = 0; i < this.mst.size(); i++) {
                    Canvas.gameScr.startNewMagicBeam(5, c, (LiveActor) this.mst.elementAt(i), c.x + SkillAnimate.splashDuaX[c.dir], c.y + SkillAnimate.splashDuaY[c.dir], c.attkPower, c.attkEffect);
                }
            } else {
                for (int i2 = 0; i2 < this.mst.size(); i2++) {
                    LiveActor mt = (LiveActor) this.mst.elementAt(i2);
                    Skill_AEO_Eevee_Bulbasaur_5 skill = new Skill_AEO_Eevee_Bulbasaur_5(c.x + SkillAnimate.splashDuaX[c.dir], c.y + SkillAnimate.splashDuaY[c.dir], c, mt, (byte) 4);
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
            }
        }
        c.p1 = (short) (c.p1 + 1);
    }

    @Override
    public void updateSkill(Monster c) {
    }
}
