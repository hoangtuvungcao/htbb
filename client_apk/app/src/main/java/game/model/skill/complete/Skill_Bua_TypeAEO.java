package game.model.skill.complete;

import game.model.AttackResult;
import game.model.Char;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.model.skill.Skill_AEO_Dao_5;
import game.render.GCanvas;
import java.util.Random;
import java.util.Vector;

public class Skill_Bua_TypeAEO extends SkillAnimate {
    private boolean isSkill5;
    Vector mst;

    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Bua_TypeAEO(boolean z) {
        super(0);
        this.isSkill5 = false;
        this.mst = new Vector();
        this.isSkill5 = z;
    }

    @Override
    public void setMonster(Vector vector) {
        this.mst = vector;
    }

    @Override
    public void updateSkill(Char r20) {
        int i;
        if (r20 == null) {
            return;
        }
        r20.frame = (byte) 4;
        updateStateBua(r20, 4, 0);
        if (r20.p1 == 13) {
            int iNextInt = new Random(System.currentTimeMillis()).nextInt() % 20;
            if (r20.dir == 0 || r20.dir == 1) {
                i = 0;
            } else {
                i = iNextInt;
                iNextInt = 0;
            }
            if (r20.attackTarget != null) {
                if (r20.attackTarget.catagory == 1) {
                    if (r20.attkEffect == 0) {
                        EffectManager.addHiEffect(r20.attackTarget.x + iNextInt, (r20.attackTarget.y - 10) + i, 11);
                        r20.attackTarget.jump();
                    } else if (r20.attkEffect == 2) {
                        EffectManager.addHiEffect(r20.attackTarget.x + iNextInt, (r20.attackTarget.y - 10) + i, 12);
                        r20.attackTarget.doublejump();
                    }
                }
                r20.attackTarget.realHPSyncTime = 2;
            }
            if (!this.isSkill5) {
                int size = this.mst.size();
                for (int i2 = 0; i2 < size; i2++) {
                    LiveActor liveActor = (LiveActor) this.mst.elementAt(i2);
                    EffectManager.addLowEffect((liveActor.x - 10) + iNextInt, (liveActor.y - 25) + i, 14);
                    EffectManager.addLowEffect(liveActor.x + 10 + iNextInt, (liveActor.y - 25) + i, 14);
                    EffectManager.addLowEffect(liveActor.x + iNextInt, (liveActor.y - 35) + i, 14);
                    if (r20.attkPower != 0 && r20.attkPower != 2000000) {
                        GCanvas.gameScr.startFlyText("-" + r20.attkPower, 0, liveActor.x, liveActor.y - 15, -1, -2);
                    }
                    if (r20.attkEffect != 0 && r20.attkEffect < AttackResult.EFF_NAME.length && r20.attkEffect <= AttackResult.EFF_NAME.length - 1) {
                        GCanvas.gameScr.startFlyText(AttackResult.EFF_NAME[r20.attkEffect], 0, liveActor.x, liveActor.y - 15, 2, -2);
                    }
                }
            } else {
                int size2 = this.mst.size();
                for (int i3 = 0; i3 < size2; i3++) {
                    Skill_AEO_Dao_5 skill_AEO_Dao_5 = new Skill_AEO_Dao_5(r20.x, r20.y + 37, (LiveActor) this.mst.elementAt(i3), 1, true);
                    if (r20.attkPower != 0 && r20.attkPower != 2000000) {
                        skill_AEO_Dao_5.textAttack[0] = "-" + r20.attkPower;
                    }
                    if (r20.attkEffect != 0 && r20.attkEffect <= AttackResult.EFF_NAME.length - 1) {
                        skill_AEO_Dao_5.textAttack[1] = AttackResult.EFF_NAME[r20.attkEffect];
                    }
                    EffectManager.hiEffects.addElement(skill_AEO_Dao_5);
                }
            }
        }
        r20.p1 = (short) (r20.p1 + 1);
    }
}
