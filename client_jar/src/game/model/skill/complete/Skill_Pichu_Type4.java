package game.model.skill.complete;

import game.model.Actor;
import game.model.Char;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;

public class Skill_Pichu_Type4 extends SkillAnimate {
    Vector mst;

    public Skill_Pichu_Type4(int skillWeapon) {
        super(skillWeapon);
        this.mst = new Vector();
    }

    @Override
    public void setMonster(Vector mst) {
        this.mst = mst;
    }

    @Override
    public void setActorter(Vector mst) {
        this.mst = mst;
    }

    @Override
    public void updateSkill(Char c) {
        super.updateSkill(c);
        if (c.state == 0) {
            return;
        }
        updateSkillCung_2_3(c);
        if (c.p1 == 10) {
            if (this.mst.size() > 0) {
                for (int i = 0; i < this.mst.size(); i++) {
                    Actor ac = (Actor) this.mst.elementAt(i);
                    Canvas.gameScr.startNewArrow(0, c, (LiveActor) ac, c.x, c.y - 15, c.attkPower, c.attkEffect, 1);
                }
            } else {
                Canvas.gameScr.startNewArrow(0, c, c.attackTarget, c.x, c.y - 15, c.attkPower, c.attkEffect, 1);
            }
        }
        if (c.p1 >= 13) {
            if (this.mst.size() > 0) {
                for (int i2 = 0; i2 < this.mst.size(); i2++) {
                    Actor ac2 = (Actor) this.mst.elementAt(i2);
                    attack(c, ac2);
                }
            } else {
                attack(c, c.attackTarget);
            }
        }
        c.p1 = (short) (c.p1 + 1);
    }

    private void attack(Char c, Actor ac) {
        int xx = ac.x;
        int yy = ac.y - 20;
        int aa = Res.rnd(3) + 1;
        for (int a = 0; a < aa; a++) {
            int bb = Res.rnd(50) + 50;
            int xa = (bb * Util.cos(a * 30)) >> 10;
            int ya = (-(bb * Util.sin(a * 30))) >> 10;
            Canvas.gameScr.startNewArrow(0, c, (LiveActor) ac, xx + xa, yy + ya, c.attkPower, c.attkEffect, Res.rnd(2));
        }
    }

    @Override
    public void updateSkill(Monster c) {
    }
}
