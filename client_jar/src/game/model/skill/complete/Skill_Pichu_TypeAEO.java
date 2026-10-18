package game.model.skill.complete;

import game.model.Char;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.model.skill.Skill_AEO_Pichu_5;
import game.render.Canvas;
import java.util.Vector;

public class Skill_Pichu_TypeAEO extends SkillAnimate {
    boolean isAEO;
    private byte idSkill;
    private byte imgIndex;
    Vector mst;
    boolean isMonster;

    public Skill_Pichu_TypeAEO(byte idSkill2, boolean is, byte imgIndex) {
        super(0);
        this.isAEO = false;
        this.mst = new Vector();
        this.isMonster = false;
        this.isAEO = is;
        this.idSkill = idSkill2;
        this.imgIndex = imgIndex;
    }

    @Override
    public void setMonster(Vector mst) {
        this.mst = mst;
        this.isMonster = true;
    }

    @Override
    public void setActorter(Vector mst) {
        this.mst = mst;
        this.isMonster = false;
    }

    @Override
    public void updateSkill(Char c) {
        super.updateSkill(c);
        if (c.state == 0) {
            return;
        }
        updateSkillCung_2_3(c);
        if (c.p1 == 10) {
            for (int i = 0; i < this.mst.size(); i++) {
                LiveActor lactor = (LiveActor) this.mst.elementAt(i);
                Canvas.gameScr.startNewArrow(0, c, lactor, c.x, c.y - 15, c.attkPower, c.attkEffect, this.imgIndex);
            }
            this.isMonster = false;
        }
        if (this.isAEO && c.p1 == 14) {
            if (this.mst.size() > 0) {
                Skill_AEO_Pichu_5 skill = new Skill_AEO_Pichu_5(this.mst, c);
                EffectManager.hiEffects.addElement(skill);
            } else {
                Vector lis = new Vector();
                lis.addElement(c.attackTarget);
                Skill_AEO_Pichu_5 skill2 = new Skill_AEO_Pichu_5(lis, c);
                EffectManager.hiEffects.addElement(skill2);
            }
        }
        c.p1 = (short) (c.p1 + 1);
    }

    @Override
    public void updateSkill(Monster c) {
        if (this.mst != null && this.mst.size() > 0) {
            for (int i = 0; i < this.mst.size(); i++) {
                LiveActor lactor = (LiveActor) this.mst.elementAt(i);
                Canvas.gameScr.startNewArrow(0, c, lactor, c.x, c.y - 15, 0, (byte) 0, this.imgIndex);
            }
            this.isMonster = false;
        } else {
            Canvas.gameScr.startNewArrow(0, c, c.attackTarget, c.x, c.y - 15, 0, (byte) 0, this.imgIndex);
        }
        c.p1 = (short) (c.p1 + 1);
    }
}
