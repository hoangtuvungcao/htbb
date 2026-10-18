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

    public Skill_Bua_Type4(int skillWeapon) {
        super(skillWeapon);
        this.mst = new Vector();
    }

    @Override
    public void setMonster(Vector mst) {
        this.mst = mst;
    }

    @Override
    public void updateSkill(Char c) {
        super.updateSkill(c);
        if (c.state == 0) {
            return;
        }
        updateStateBua(c, 35, 0);
        if (c.p1 == 13) {
            if (this.mst.size() > 0) {
                for (int i = 0; i < this.mst.size(); i++) {
                    Actor ac = (Actor) this.mst.elementAt(i);
                    EffectManager.lowEffects.addElement(new Skill_Bua_4((LiveActor) ac, c.x, c.y, false));
                }
            } else {
                EffectManager.lowEffects.addElement(new Skill_Bua_4(c.attackTarget, c.x, c.y, false));
            }
        }
        c.p1 = (short) (c.p1 + 1);
    }
}
