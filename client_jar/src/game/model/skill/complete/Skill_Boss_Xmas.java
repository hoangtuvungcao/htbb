package game.model.skill.complete;

import game.model.Char;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.Canvas;
import java.util.Vector;

public class Skill_Boss_Xmas extends SkillAnimate {
    private boolean isSkill5;
    Vector mst;

    public Skill_Boss_Xmas(boolean isSkill5) {
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
    }

    @Override
    public void updateSkill(Monster c) {
        if (c.p1 == 3 && !this.isSkill5) {
            for (int i = 0; i < this.mst.size(); i++) {
                LiveActor mt = (LiveActor) this.mst.elementAt(i);
                Canvas.gameScr.startNewMagicBeam(7, c, mt, c.x + SkillAnimate.splashDuaX[c.dir], c.y + SkillAnimate.splashDuaY[c.dir], mt.damAttack, (byte) 0);
            }
        }
        c.p1 = (short) (c.p1 + 1);
    }
}
