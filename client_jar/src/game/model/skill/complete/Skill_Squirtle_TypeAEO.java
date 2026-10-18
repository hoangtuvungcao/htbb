package game.model.skill.complete;

import game.model.AttackResult;
import game.model.Char;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.model.skill.Skill_AEO_Bulbasaur_5;
import game.render.Canvas;
import java.util.Random;
import java.util.Vector;

public class Skill_Squirtle_TypeAEO extends SkillAnimate {
    private boolean isSkill5;
    Vector mst;

    public Skill_Squirtle_TypeAEO(boolean isSkill5) {
        super(0);
        this.isSkill5 = false;
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
        updateStateBua(c, 4, 0);
        if (c.p1 == 13) {
            Random r = new Random(System.currentTimeMillis());
            int rdx = r.nextInt() % 20;
            int rdy = rdx;
            if (c.dir == 0 || c.dir == 1) {
                rdy = 0;
            } else {
                rdx = 0;
            }
            if (c.attackTarget != null) {
                if (c.attackTarget.catagory == 1) {
                    if (c.attkEffect == 0) {
                        EffectManager.addHiEffect(c.attackTarget.x + rdx, (c.attackTarget.y - 10) + rdy, 11);
                        c.attackTarget.jump();
                    } else if (c.attkEffect == 2) {
                        EffectManager.addHiEffect(c.attackTarget.x + rdx, (c.attackTarget.y - 10) + rdy, 12);
                        c.attackTarget.doublejump();
                    }
                }
                c.attackTarget.realHPSyncTime = 2;
            }
            if (!this.isSkill5) {
                for (int i = 0; i < this.mst.size(); i++) {
                    LiveActor mt = (LiveActor) this.mst.elementAt(i);
                    EffectManager.addLowEffect((mt.x - 10) + rdx, (mt.y - 25) + rdy, 14);
                    EffectManager.addLowEffect(mt.x + 10 + rdx, (mt.y - 25) + rdy, 14);
                    EffectManager.addLowEffect(mt.x + rdx, ((mt.y - 25) - 10) + rdy, 14);
                    if (c.attkPower != 0 && c.attkPower != 2000000) {
                        Canvas.gameScr.startFlyText(new StringBuffer("-").append(c.attkPower).toString(), 0, mt.x + 0, mt.y - 15, -1, -2);
                    }
                    if (c.attkEffect != 0 && c.attkEffect < AttackResult.EFF_NAME.length) {
                        Canvas.gameScr.startFlyText(AttackResult.EFF_NAME[c.attkEffect], 0, mt.x + 0, mt.y - 15, 2, -2);
                    }
                }
            } else {
                for (int i2 = 0; i2 < this.mst.size(); i2++) {
                    Skill_AEO_Bulbasaur_5 skill = new Skill_AEO_Bulbasaur_5(c.x, c.y + 37, (LiveActor) this.mst.elementAt(i2), 1, true);
                    if (c.attkPower != 0 && c.attkPower != 2000000) {
                        skill.textAttack[0] = new StringBuffer("-").append(c.attkPower).toString();
                    }
                    if (c.attkEffect != 0) {
                        skill.textAttack[1] = AttackResult.EFF_NAME[c.attkEffect];
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
