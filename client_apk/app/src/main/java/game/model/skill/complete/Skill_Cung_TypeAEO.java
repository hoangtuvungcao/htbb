package game.model.skill.complete;

import game.model.Char;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.model.skill.Skill_AEO_CUNG_5;
import game.render.GCanvas;
import java.util.Vector;

public class Skill_Cung_TypeAEO extends SkillAnimate {
    private byte idSkill;
    private byte imgIndex;
    boolean isAEO;
    boolean isMonster;
    Vector mst;

    public Skill_Cung_TypeAEO(byte b, boolean z, byte b2) {
        super(0);
        this.isAEO = false;
        this.mst = new Vector();
        this.isMonster = false;
        this.isAEO = z;
        this.idSkill = b;
        this.imgIndex = b2;
    }

    @Override
    public void setMonster(Vector vector) {
        this.mst = vector;
        this.isMonster = true;
    }

    @Override
    public void setActorter(Vector vector) {
        this.mst = vector;
        this.isMonster = false;
    }

    @Override
    public void updateSkill(Char r13) {
        if (r13 == null) {
            return;
        }
        super.updateSkill(r13);
        if (r13.state == 0) {
            return;
        }
        updateSkillCung_2_3(r13);
        if (r13.p1 == 10) {
            for (int i = 0; i < this.mst.size(); i++) {
                LiveActor liveActor = (LiveActor) this.mst.elementAt(i);
                if (liveActor != null) {
                    GCanvas.gameScr.startNewArrow(0, r13, liveActor, r13.x, r13.y - 15, r13.attkPower, r13.attkEffect, this.imgIndex);
                }
            }
            this.isMonster = false;
        }
        if (this.isAEO && r13.p1 == 14) {
            if (this.mst.size() > 0) {
                EffectManager.hiEffects.addElement(new Skill_AEO_CUNG_5(this.mst, r13));
            } else {
                Vector vector = new Vector();
                if (r13.attackTarget != null) {
                    vector.addElement(r13.attackTarget);
                    EffectManager.hiEffects.addElement(new Skill_AEO_CUNG_5(vector, r13));
                }
            }
        }
        r13.p1 = (short) (r13.p1 + 1);
    }

    @Override
    public void updateSkill(Monster monster) {
        if (monster == null) {
            return;
        }
        Vector vector = this.mst;
        if (vector != null && vector.size() > 0) {
            for (int i = 0; i < this.mst.size(); i++) {
                LiveActor liveActor = (LiveActor) this.mst.elementAt(i);
                if (liveActor != null) {
                    GCanvas.gameScr.startNewArrow(0, monster, liveActor, monster.x, monster.y - 15, 0, (byte) 0, this.imgIndex);
                }
            }
            this.isMonster = false;
        } else if (monster.attackTarget != null) {
            GCanvas.gameScr.startNewArrow(0, monster, monster.attackTarget, monster.x, monster.y - 15, 0, (byte) 0, this.imgIndex);
        }
        monster.p1 = (short) (monster.p1 + 1);
    }
}
