package game.model.skill.complete;

import game.model.Actor;
import game.model.AttackResult;
import game.model.Char;
import game.model.Cout;
import game.model.EffectManager;
import game.model.Lightning;
import game.model.LiveActor;
import game.model.Monster;
import game.model.Position;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;
import java.util.Vector;

public class Skill_Kiem_TypeAEO extends SkillAnimate {
    boolean isMonster;
    private Lightning light;
    Vector mst;
    int type;
    int xPos;
    int yPos;

    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Kiem_TypeAEO(int i) {
        super(-1);
        this.mst = new Vector();
        this.isMonster = false;
        this.type = i;
    }

    @Override
    public void setMonster(Vector vector) {
        add(vector);
    }

    private void addEff() {
        if (this.light == null) {
            this.light = new Lightning();
        }
        this.light.reset();
        int size = this.mst.size();
        for (int i = 0; i < size; i++) {
            Actor actor = (Actor) this.mst.elementAt(i);
            this.light.listPos.addElement(new Position(actor.x, actor.y));
        }
        if (this.light.listPos.size() > 0) {
            Lightning lightning = this.light;
            lightning.setInfo(lightning.listPos, new Position(this.xPos, this.yPos - 40), this.type == 4);
            EffectManager.hiEffects.addElement(this.light);
        }
    }

    @Override
    public void setActorter(Vector vector) {
        add(vector);
        this.isMonster = false;
    }

    private void add(Vector vector) {
        this.mst.removeAllElements();
        int size = vector.size();
        for (int i = 0; i < size; i++) {
            Actor actorFindActorByID = GCanvas.gameScr.findActorByID(((LiveActor) vector.elementAt(i)).ID);
            if (actorFindActorByID != null) {
                this.mst.addElement(actorFindActorByID);
            }
        }
    }

    @Override
    public void updateSkill(Char r14) {
        try {
            r14.frame = (byte) 4;
            if (r14.p1 == 16) {
                r14.state = (byte) 0;
                r14.p3 = (short) 0;
                r14.p2 = (short) 0;
                r14.p1 = (short) 0;
                r14.weapon_frame = (short) 0;
            } else if (r14.p1 >= 14 && r14.p1 < 16) {
                r14.frame = (byte) 5;
                r14.weapon_frame = (short) 7;
            } else if (r14.p1 == 13 || r14.p1 == 12) {
                r14.frame = (byte) 5;
                r14.weapon_frame = (short) 6;
            } else if (r14.p1 == 11 || r14.p1 == 10) {
                r14.frame = (byte) 4;
                r14.weapon_frame = (short) 5;
            } else {
                try {
                    if (r14.p1 % 2 == 0) {
                        this.xPos = r14.x;
                        this.yPos = r14.y;
                        EffectManager.addHiEffect(r14.x, r14.y - 40, 3);
                    }
                    r14.frame = (byte) 4;
                    r14.weapon_frame = (short) 4;
                } catch (Exception unused) {
                }
            }
            try {
                if (r14.p1 == 9) {
                    addEff();
                    int size = this.mst.size();
                    for (int i = 0; i < size; i++) {
                        try {
                            if (!((Actor) this.mst.elementAt(i)).isNPC()) {
                                LiveActor liveActor = (LiveActor) this.mst.elementAt(i);
                                if (r14.attkPower != 0 && r14.attkPower != 2000000) {
                                    GCanvas.gameScr.startFlyText("-" + r14.attkPower, 0, liveActor.x, liveActor.y - 15, 1, -2);
                                }
                                if (r14.attkEffect != 0 && r14.attkEffect < AttackResult.EFF_NAME.length) {
                                    GCanvas.gameScr.startFlyText(AttackResult.EFF_NAME[r14.attkEffect], 0, liveActor.x, liveActor.y - 15, 2, -2);
                                }
                                liveActor.jump();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    this.isMonster = false;
                }
            } catch (Exception unused2) {
            }
            r14.p1 = (short) (r14.p1 + 1);
        } catch (Exception unused3) {
            Cout.println("skill kiem AEO");
        }
    }
}
