package game.model.skill.complete;

import game.model.Actor;
import game.model.Char;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.model.skill.ThienThach_Skill;
import java.util.Vector;

public class Skill_Bulbasaur_Type4 extends SkillAnimate {
    Vector mst;

    public Skill_Bulbasaur_Type4() {
        super(-1);
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
            int i = c.p1 % 2;
            c.frame = (byte) 4;
            c.weapon_frame = (short) 4;
        }
        if (c.p1 == 9) {
            if (this.mst.size() > 0) {
                for (int i2 = 0; i2 < this.mst.size(); i2++) {
                    Actor ac = (Actor) this.mst.elementAt(i2);
                    ThienThach_Skill tts = new ThienThach_Skill(ac.x, ac.y, c.attkPower);
                    EffectManager.hiEffects.addElement(tts);
                }
            } else {
                ThienThach_Skill tts2 = new ThienThach_Skill(c.attackTarget.x, c.attackTarget.y, c.attkPower);
                EffectManager.hiEffects.addElement(tts2);
            }
        }
        c.p1 = (short) (c.p1 + 1);
    }

    @Override
    public void updateSkill(Monster c) {
    }
}
