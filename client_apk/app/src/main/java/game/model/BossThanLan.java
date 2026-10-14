package game.model;

import effect.DynamicEffect;
import game.core.j2me.Graphics;
import game.model.skill.Skill_AEO_Dao_5;
import game.model.skill.Skill_Da_Roi;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;

public class BossThanLan extends Monster {
    private byte count;
    int countLeft;
    int countRight;
    int dis;
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

    public void createExplode1() {
    }

    @Override
    public void jump() {
    }

    @Override
    public void paint(Graphics graphics) {
        if (this.totalExp < 15) {
            if (Res.monsterTemplates[this.monster_type].imageObj != null) {
                if (this.dynamicEffBottom != null) {
                    for (int i = 0; i < this.dynamicEffBottom.size(); i++) {
                        ((DynamicEffect) this.dynamicEffBottom.elementAt(i)).paint(graphics, this.x, this.y);
                    }
                }
                Res.monsterTemplates[this.monster_type].imageObj.paint(graphics, this.x, this.y, 0, 0, 1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(graphics, this.x, this.y, 0, 2, -1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(graphics, this.x, this.y + this.count, this.indexDau, 0, 1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(graphics, this.x, this.y + this.count, this.indexDau, 2, -1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(graphics, this.x, this.y, 1, 0, 1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(graphics, this.x, this.y, 1, 0, -1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(graphics, this.x, this.y, this.indexDapLeft, 0, 1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(graphics, this.x, this.y, this.indexDapRight, 2, -1, 1);
                if (this.dynamicEffTop != null) {
                    for (int i2 = 0; i2 < this.dynamicEffTop.size(); i2++) {
                        ((DynamicEffect) this.dynamicEffTop.elementAt(i2)).paint(graphics, this.x, this.y);
                    }
                }
            } else if (System.currentTimeMillis() > this.timeGetImage) {
                this.timeGetImage = System.currentTimeMillis() + 10000;
                Res.monsterTemplates[this.monster_type].isLoad = false;
                Res.monsterTemplates[this.monster_type].loadImage();
            }
        }
        paintExp(graphics);
    }

    @Override
    public void jumpVang(Actor actor) {
        super.jumpVang(actor);
    }

    private void paintExp(Graphics graphics) {
        for (int i = 0; i < this.exp.size(); i++) {
            Exp exp = (Exp) this.exp.elementAt(i);
            if (exp != null) {
                exp.paint(graphics);
            }
        }
    }

    private void updateExp() {
        for (int i = 0; i < this.exp.size(); i++) {
            Exp exp = (Exp) this.exp.elementAt(i);
            if (exp != null) {
                exp.update();
                if (exp.destroy) {
                    this.exp.removeElement(exp);
                    if (this.exp.size() == 0) {
                        this.wantDestroy = true;
                    }
                }
            }
        }
    }

    private void createExp() {
        byte b = this.totalExp;
        if (b < 15) {
            byte b2 = (byte) (b + 1);
            this.totalExp = b2;
            if (b2 == 15) {
                createExplode1();
            }
            Random random = new Random(System.currentTimeMillis());
            this.exp.addElement(new Explode((this.x - 12) + GameScr.abs(random.nextInt() % 25), (this.y - 30) + GameScr.abs(random.nextInt() % 35), 0));
        }
    }

    @Override
    public void startDeadFly(int i, int i2) {
        this.state = (byte) 5;
    }

    @Override
    public void paintCorner(Graphics graphics, int i, int i2) {
        if (this.maxhp == 0 || Res.monsterTemplates[this.monster_type].imageObj == null) {
            return;
        }
        Res.monsterTemplates[this.monster_type].imageObj.paint(graphics, i, i2, 4, 0, 1, 1);
    }

    @Override
    public void update() {
        updateDynamicBuff();
        this.monster_type = (short) 39;
        if (this.totalExp < 15 && Res.monsterTemplates[this.monster_type].imageObj != null && GCanvas.gameTick % 2 == 1) {
            byte b = (byte) (this.count + this.dirdau);
            this.count = b;
            if (Util.abs(b) > 1) {
                this.dirdau = (byte) (this.dirdau * (-1));
            }
        }
        updateExp();
        if (this.dlTime != 0) {
            this.z += this.a;
            int i = this.a + 1;
            this.a = i;
            if (i > 1) {
                this.dlTime = 0L;
                this.a = -1;
            }
        } else if (GCanvas.gameTick % 10 == 0) {
            this.dlTime = 1L;
        }
        byte b2 = this.state;
        if (b2 == 0) {
            this.indexDau = (byte) 4;
            this.frame = (byte) 0;
        } else if (b2 == 5) {
            this.indexDau = (byte) 4;
            this.frame = (byte) 2;
            this.p1 = (short) (this.p1 + 1);
            this.x = (short) (this.x + this.p2);
            this.y = (short) (this.y + this.p3);
            this.p2 = (short) (this.p2 >> 1);
            this.p3 = (short) (this.p3 >> 1);
            createExp();
        } else if (b2 == 2) {
            this.indexDau = (byte) 4;
            this.p1 = (short) (this.p1 + 1);
            if (this.p1 > 6) {
                this.p1 = (short) 0;
            }
            this.frame = (byte) 0;
        } else if (b2 == 3) {
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
        }
        int i2 = this.AEO;
        if (i2 != -1) {
            int i3 = i2 + 1;
            this.AEO = i3;
            if (i3 < 8) {
                int i4 = i3 % 2;
            }
            if (i3 >= 8) {
                this.AEO = -1;
            }
        }
        int i5 = this.countRight;
        if (i5 > 0) {
            int i6 = i5 - 1;
            this.countRight = i6;
            if (i6 == 0) {
                this.indexDapRight = 2;
                setFire(-1);
            }
        }
        int i7 = this.countLeft;
        if (i7 > 0) {
            int i8 = i7 - 1;
            this.countLeft = i8;
            if (i8 == 0) {
                this.indexDapLeft = 2;
                setFire(1);
            }
        }
    }

    private void setFire(int i) {
        int size = this.listAttack.size();
        for (int i2 = 0; i2 < size; i2++) {
            LiveActor liveActor = (LiveActor) this.listAttack.elementAt(i2);
            Skill_AEO_Dao_5 skill_AEO_Dao_5 = new Skill_AEO_Dao_5((i * 40) + this.x, this.y + 30, liveActor, 1, true);
            if (liveActor.damAttack != 0) {
                skill_AEO_Dao_5.textAttack[0] = "-" + liveActor.damAttack;
            }
            EffectManager.addHiEffect(this.x + (i * 50), this.y - 30, 14);
            EffectManager.hiEffects.addElement(skill_AEO_Dao_5);
        }
    }

    @Override
    public void startAttack(Vector vector, byte b) {
        this.listAttack.removeAllElements();
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.AEO = 0;
        Res.rnd(2);
        this.skillID = (byte) 0;
        this.frame = (byte) 1;
        int size = vector.size();
        for (int i = 0; i < size; i++) {
            LiveActor liveActor = (LiveActor) vector.elementAt(i);
            EffectManager.hiEffects.addElement(new Skill_Da_Roi(liveActor.x, liveActor.y, this.x, this.y, liveActor.damAttack));
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
