package game.model.skill.complete;

import game.model.Char;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;
import java.util.Vector;

public class Skill_Boss_Xmas extends SkillAnimate {
    private boolean isSkill5;
    Vector mst;

    @Override
    public void updateSkill(Char r1) {
    }

    public Skill_Boss_Xmas(boolean z) {
        super(-1);
        this.mst = new Vector();
        this.isSkill5 = z;
    }

    @Override
    public void setMonster(Vector vector) {
        this.mst = vector;
    }

    @Override
    public void updateSkill(Monster monster) {
        if (monster.p1 == 3 && !this.isSkill5) {
            int size = this.mst.size();
            for (int i = 0; i < size; i++) {
                LiveActor liveActor = (LiveActor) this.mst.elementAt(i);
                GCanvas.gameScr.startNewMagicBeam(7, monster, liveActor, monster.x + splashDuaX[monster.dir], monster.y + splashDuaY[monster.dir], liveActor.damAttack, (byte) 0);
            }
        }
        monster.p1 = (short) (monster.p1 + 1);
    }
}
