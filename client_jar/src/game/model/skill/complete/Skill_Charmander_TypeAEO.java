package game.model.skill.complete;

import game.model.Actor;
import game.model.AttackResult;
import game.model.Char;
import game.model.EffectManager;
import game.model.Lightning;
import game.model.LiveActor;
import game.model.Monster;
import game.model.Position;
import game.model.skill.SkillAnimate;
import game.render.Canvas;
import java.util.Vector;

public class Skill_Charmander_TypeAEO extends SkillAnimate {
    Vector mst;
    int xPos;
    int yPos;
    int type;
    private Lightning light;
    boolean isMonster;

    public Skill_Charmander_TypeAEO(int type) {
        super(-1);
        this.mst = new Vector();
        this.isMonster = false;
        this.type = type;
    }

    @Override
    public void setMonster(Vector mst) {
        add(mst);
    }

    private void addEff() {
        if (this.light == null) {
            this.light = new Lightning();
        }
        this.light.reset();
        for (int i = 0; i < this.mst.size(); i++) {
            Actor ms = (Actor) this.mst.elementAt(i);
            this.light.listPos.addElement(new Position(ms.x, ms.y));
        }
        if (this.light.listPos.size() > 0) {
            this.light.setInfo(this.light.listPos, new Position(this.xPos, this.yPos - 40), this.type == 4);
            EffectManager.hiEffects.addElement(this.light);
        }
    }

    @Override
    public void setActorter(Vector mst) {
        add(mst);
        this.isMonster = false;
    }

    private void add(Vector mst) {
        this.mst.removeAllElements();
        for (int i = 0; i < mst.size(); i++) {
            LiveActor live = (LiveActor) mst.elementAt(i);
            Actor ac = Canvas.gameScr.findActorByID(live.ID);
            if (ac != null) {
                this.mst.addElement(ac);
            }
        }
    }

    @Override
    public void updateSkill(Char c) {
        c.frame = (byte) 4;
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
            if (c.p1 % 2 == 0) {
                this.xPos = c.x;
                this.yPos = c.y;
                EffectManager.addHiEffect(c.x, c.y - 40, 3);
            }
            c.frame = (byte) 4;
            c.weapon_frame = (short) 4;
        }
        if (c.p1 == 9) {
            addEff();
            for (int i = 0; i < this.mst.size(); i++) {
                try {
                    if (!((Actor) this.mst.elementAt(i)).isNPC()) {
                        LiveActor lactor = (LiveActor) this.mst.elementAt(i);
                        if (c.attkPower != 0 && c.attkPower != 2000000) {
                            Canvas.gameScr.startFlyText(new StringBuffer("-").append(c.attkPower).toString(), 0, lactor.x + 0, lactor.y - 15, 1, -2);
                        }
                        if (c.attkEffect != 0 && c.attkEffect < AttackResult.EFF_NAME.length) {
                            Canvas.gameScr.startFlyText(AttackResult.EFF_NAME[c.attkEffect], 0, lactor.x + 0, lactor.y - 15, 2, -2);
                        }
                        lactor.jump();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.isMonster = false;
        }
        c.p1 = (short) (c.p1 + 1);
    }

    @Override
    public void updateSkill(Monster c) {
    }
}
