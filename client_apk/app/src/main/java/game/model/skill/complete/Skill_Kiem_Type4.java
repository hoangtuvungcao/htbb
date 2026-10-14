package game.model.skill.complete;

import game.model.Actor;
import game.model.Char;
import game.model.Cout;
import game.model.EffectManager;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.model.skill.Skill_Kiem_Down;
import java.util.Vector;

public class Skill_Kiem_Type4 extends SkillAnimate {
    Vector mst;

    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Kiem_Type4() {
        super(-1);
        this.mst = new Vector();
    }

    @Override
    public void setMonster(Vector vector) {
        this.mst = vector;
    }

    @Override
    public void updateSkill(Char r7) {
        try {
            super.updateSkill(r7);
            if (r7.state == 0) {
                return;
            }
            if (r7.p1 == 16) {
                r7.state = (byte) 0;
                r7.p3 = (short) 0;
                r7.p2 = (short) 0;
                r7.p1 = (short) 0;
                r7.weapon_frame = (short) 0;
            } else if (r7.p1 >= 14 && r7.p1 < 16) {
                r7.frame = (byte) 5;
                r7.weapon_frame = (short) 7;
            } else if (r7.p1 == 13 || r7.p1 == 12) {
                r7.frame = (byte) 5;
                r7.weapon_frame = (short) 6;
            } else if (r7.p1 == 11 || r7.p1 == 10) {
                r7.frame = (byte) 4;
                r7.weapon_frame = (short) 5;
            } else {
                int i = r7.p1 % 2;
                r7.frame = (byte) 4;
                r7.weapon_frame = (short) 4;
            }
            if (r7.p1 == 9) {
                if (this.mst.size() > 0) {
                    int size = this.mst.size();
                    for (int i2 = 0; i2 < size; i2++) {
                        Actor actor = (Actor) this.mst.elementAt(i2);
                        try {
                            EffectManager.hiEffects.addElement(new Skill_Kiem_Down(actor.x, actor.y));
                        } catch (Exception unused) {
                            Cout.println("Loi skill kiem down 4");
                        }
                    }
                } else {
                    try {
                        EffectManager.hiEffects.addElement(new Skill_Kiem_Down(r7.attackTarget.x, r7.attackTarget.y));
                    } catch (Exception unused2) {
                        Cout.println("Loi skill kiem down");
                    }
                }
            }
            r7.p1 = (short) (r7.p1 + 1);
        } catch (Exception unused3) {
            Cout.println("Loi skill kiem down");
        }
    }
}
