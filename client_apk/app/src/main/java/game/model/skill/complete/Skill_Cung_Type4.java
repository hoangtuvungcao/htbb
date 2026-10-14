package game.model.skill.complete;

import game.model.Actor;
import game.model.Char;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;

public class Skill_Cung_Type4 extends SkillAnimate {
    Vector mst;

    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Cung_Type4(int i) {
        super(i);
        this.mst = new Vector();
    }

    @Override
    public void setMonster(Vector vector) {
        this.mst = vector;
    }

    @Override
    public void setActorter(Vector vector) {
        this.mst = vector;
    }

    @Override
    public void updateSkill(Char r14) {
        if (r14 == null) {
            return;
        }
        super.updateSkill(r14);
        if (r14.state == 0) {
            return;
        }
        updateSkillCung_2_3(r14);
        if (r14.p1 == 10) {
            if (this.mst.size() > 0) {
                try {
                    int size = this.mst.size();
                    for (int i = 0; i < size; i++) {
                        GCanvas.gameScr.startNewArrow(0, r14, (LiveActor) ((Actor) this.mst.elementAt(i)), r14.x, r14.y - 15, r14.attkPower, r14.attkEffect, 1);
                    }
                } catch (Exception unused) {
                }
            } else if (r14.attackTarget != null) {
                GCanvas.gameScr.startNewArrow(0, r14, r14.attackTarget, r14.x, r14.y - 15, r14.attkPower, r14.attkEffect, 1);
            }
        }
        if (r14.p1 >= 13) {
            if (this.mst.size() > 0) {
                for (int i2 = 0; i2 < this.mst.size(); i2++) {
                    Actor actor = (Actor) this.mst.elementAt(i2);
                    if (actor != null) {
                        attack(r14, actor);
                    }
                }
            } else if (r14.attackTarget != null) {
                attack(r14, r14.attackTarget);
            }
        }
        r14.p1 = (short) (r14.p1 + 1);
    }

    private void attack(Char r16, Actor actor) {
        short s = actor.x;
        int i = actor.y - 20;
        int iRnd = Res.rnd(3) + 1;
        for (int i2 = 0; i2 < iRnd; i2++) {
            int iRnd2 = Res.rnd(50) + 50;
            int i3 = i2 * 30;
            GCanvas.gameScr.startNewArrow(0, r16, (LiveActor) actor, s + ((Util.cos(i3) * iRnd2) >> 10), i + ((-(iRnd2 * Util.sin(i3))) >> 10), r16.attkPower, r16.attkEffect, Res.rnd(2));
        }
    }
}
