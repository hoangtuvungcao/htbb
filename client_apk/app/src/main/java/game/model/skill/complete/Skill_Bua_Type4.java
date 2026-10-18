package game.model.skill.complete;

import game.model.Actor;
import game.model.Char;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.skill.SkillAnimate;
import game.model.skill.Skill_Bua_4;
import java.util.Vector;

public class Skill_Bua_Type4 extends SkillAnimate {
    Vector mst;

    public Skill_Bua_Type4(int i) {
        super(i);
        this.mst = new Vector();
    }

    @Override
    public void setMonster(Vector vector) {
        this.mst = vector;
    }

    @Override
    public void updateSkill(Char r9) {
        if (r9 == null) {
            return;
        }
        super.updateSkill(r9);
        if (r9.state == 0) {
            return;
        }
        updateStateBua(r9, 35, 0);
        if (r9.p1 == 13) {
            if (this.mst.size() > 0) {
                int size = this.mst.size();
                for (int i = 0; i < size; i++) {
                    Actor actor = (Actor) this.mst.elementAt(i);
                    if (actor != null) {
                        EffectManager.lowEffects.addElement(new Skill_Bua_4((LiveActor) actor, r9.x, r9.y, false));
                    }
                }
            } else if (r9.attackTarget != null) {
                EffectManager.lowEffects.addElement(new Skill_Bua_4(r9.attackTarget, r9.x, r9.y, false));
            }
        }
        r9.p1 = (short) (r9.p1 + 1);
    }
}
