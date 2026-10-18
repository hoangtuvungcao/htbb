package game.model;

import effect.DynamicEffect;
import game.model.skill.Skill_AEO_Dao_5;
import game.model.skill.Skill_Da_Roi;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class BossThanLan extends Monster {
    int dis;
    int countLeft;
    int countRight;
    private byte count;
    private byte skillID;
    Vector exp = new Vector();
    Vector listAttack = new Vector();
    int AEO = -1;
    int indexDapLeft = 2;
    int indexDapRight = 2;
    private byte indexDau = 4;
    private byte dirdau = 1;
    byte totalExp = 0;
    int a = -1;
    long dlTime = 1;
    long timeGetImage = System.currentTimeMillis();

    @Override
    public void paint(Graphics g) {
        // pokemon: template da la Pokemon (lenh -91) hoac do vat PMD: ve bang PokeBody, bo renderer boss cu
        if (paintPokemon(g)) {
            return;
        }
        this.monster_type = (short) 39;
        if (this.totalExp < 15) {
            if (Res.monsterTemplates[this.monster_type].imageObj != null) {
                if (Canvas.gameTick % 2 == 1) {
                    this.count = (byte) (this.count + this.dirdau);
                    if (Util.abs(this.count) > 1) {
                        this.dirdau = (byte) (this.dirdau * (-1));
                    }
                }
                if (this.dynamicEffBottom != null) {
                    for (int ii = 0; ii < this.dynamicEffBottom.size(); ii++) {
                        ((DynamicEffect) this.dynamicEffBottom.elementAt(ii)).paint(g, this.x, this.y);
                    }
                }
                Res.monsterTemplates[this.monster_type].imageObj.paint(g, this.x, this.y, 0, 0, 1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(g, this.x, this.y, 0, 2, -1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(g, this.x, this.y + this.count, this.indexDau, 0, 1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(g, this.x, this.y + this.count, this.indexDau, 2, -1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(g, this.x, this.y, 1, 0, 1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(g, this.x, this.y, 1, 0, -1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(g, this.x, this.y, this.indexDapLeft, 0, 1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(g, this.x, this.y, this.indexDapRight, 2, -1, 1);
                if (this.dynamicEffTop != null) {
                    for (int ii2 = 0; ii2 < this.dynamicEffTop.size(); ii2++) {
                        ((DynamicEffect) this.dynamicEffTop.elementAt(ii2)).paint(g, this.x, this.y);
                    }
                }
            } else if (System.currentTimeMillis() > this.timeGetImage) {
                this.timeGetImage = System.currentTimeMillis() + 10000;
                Res.monsterTemplates[this.monster_type].isLoad = false;
                Res.monsterTemplates[this.monster_type].loadImage();
            }
        }
        paintExp(g);
    }

    public void createExplode1() {
    }

    @Override
    public void jump() {
    }

    @Override
    public void jumpVang(Actor causeBy) {
        super.jumpVang(causeBy);
    }

    private void paintExp(Graphics g) {
        int size = this.exp.size();
        for (int i = 0; i < size; i++) {
            Exp ex = (Exp) this.exp.elementAt(i);
            if (ex != null) {
                ex.paint(g);
            }
        }
    }

    private void updateExp() {
        for (int i = 0; i < this.exp.size(); i++) {
            Exp ex = (Exp) this.exp.elementAt(i);
            if (ex != null) {
                ex.update();
                if (ex.destroy) {
                    this.exp.removeElement(ex);
                    if (this.exp.size() == 0) {
                        this.wantDestroy = true;
                    }
                }
            }
        }
    }

    private void createExp() {
        if (this.totalExp < 15) {
            this.totalExp = (byte) (this.totalExp + 1);
            if (this.totalExp == 15) {
                createExplode1();
            }
            Random r = new Random(System.currentTimeMillis());
            this.exp.addElement(new Explode((this.x - 12) + GameScr.abs(r.nextInt() % 25), (this.y - 30) + GameScr.abs(r.nextInt() % 35), 0));
        }
    }

    @Override
    public void startDeadFly(int dx, int dy) {
        this.state = (byte) 5;
    }

    @Override
    public void paintCorner(Graphics g, int xCorner, int yCorner) {
        if (this.maxhp != 0 && Res.monsterTemplates[this.monster_type].imageObj != null) {
            Res.monsterTemplates[this.monster_type].imageObj.paint(g, xCorner, yCorner, 4, 0, 1, 1);
        }
    }

    @Override
    public void update() {
        updateDynamicBuff();
        this.monster_type = (short) 39;
        updateExp();
        if (this.dlTime != 0) {
            this.z += this.a;
            this.a++;
            if (this.a > 1) {
                this.dlTime = 0L;
                this.a = -1;
            }
        } else if (Canvas.gameTick % 10 == 0) {
            this.dlTime = 1L;
        }
        switch (this.state) {
            case 0:
                this.indexDau = (byte) 4;
                this.frame = (byte) 0;
                break;
            case 2:
                this.indexDau = (byte) 4;
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 > 6) {
                    this.p1 = (short) 0;
                }
                this.frame = (byte) 0;
                break;
            case 3:
                if (this.skillID == 1) {
                    this.indexDau = (byte) 4;
                } else {
                    this.indexDau = (byte) 5;
                }
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 > 6) {
                    if (this.p1 > 15) {
                        this.p1 = (short) 0;
                        this.state = (byte) 2;
                    }
                } else if (this.p1 == 2 && this.currentSkillAnimate != null) {
                    this.currentSkillAnimate.updateSkill(this);
                }
                break;
            case 5:
                this.indexDau = (byte) 4;
                this.frame = (byte) 2;
                this.p1 = (short) (this.p1 + 1);
                this.x = (short) (this.x + this.p2);
                this.y = (short) (this.y + this.p3);
                this.p2 = (short) (this.p2 >> 1);
                this.p3 = (short) (this.p3 >> 1);
                createExp();
                break;
        }
        if (this.AEO != -1) {
            this.AEO++;
            if (this.AEO < 8) {
                int i = this.AEO % 2;
            }
            if (this.AEO >= 8) {
                this.AEO = -1;
            }
        }
        if (this.countRight > 0) {
            this.countRight--;
            if (this.countRight == 0) {
                this.indexDapRight = 2;
                setFire(-1);
            }
        }
        if (this.countLeft > 0) {
            this.countLeft--;
            if (this.countLeft == 0) {
                this.indexDapLeft = 2;
                setFire(1);
            }
        }
    }

    private void setFire(int i) {
        for (int a = 0; a < this.listAttack.size(); a++) {
            LiveActor live = (LiveActor) this.listAttack.elementAt(a);
            Skill_AEO_Dao_5 skill = new Skill_AEO_Dao_5(this.x + (i * 40), this.y + 30, live, 1, true);
            if (live.damAttack != 0) {
                skill.textAttack[0] = new StringBuffer("-").append(live.damAttack).toString();
            }
            EffectManager.addHiEffect(this.x + (i * 50), this.y - 30, 14);
            EffectManager.hiEffects.addElement(skill);
        }
    }

    @Override
    public void startAttack(Vector list, byte skillID) {
        this.listAttack.removeAllElements();
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.AEO = 0;
        int aa = Res.rnd(2);
        this.skillID = (byte) 0;
        if (this.skillID == 1) {
            if (aa == 0 && this.indexDapLeft != 3) {
                this.indexDapLeft = 3;
                this.countLeft = 10;
                this.listAttack = list;
                return;
            } else {
                this.indexDapRight = 3;
                this.countRight = 10;
                this.listAttack = list;
                return;
            }
        }
        this.frame = (byte) 1;
        for (int i = 0; i < list.size(); i++) {
            LiveActor live = (LiveActor) list.elementAt(i);
            Skill_Da_Roi skill = new Skill_Da_Roi(live.x, live.y, this.x, this.y, live.damAttack);
            EffectManager.hiEffects.addElement(skill);
        }
    }

    @Override
    public void setInfo(MonsterInfo monsterInfo) {
        super.setInfo(monsterInfo);
        Res.monsterTemplates[this.monster_type].w = (byte) 50;
        this.timeGetImage = System.currentTimeMillis() + 10000;
        this.height = (short) 100;
    }
}
