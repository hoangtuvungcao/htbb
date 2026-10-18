package game.model;

import effect.DynamicEffect;
import game.model.skill.Skill_Tia_Thuy;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class BossGyarados extends Monster {
    private static int[] trans = {0, 2};
    Vector exp = new Vector();
    byte totalExp = 0;

    @Override
    public void paint(Graphics g) {
        // pokemon: template da la Pokemon (lenh -91) hoac do vat PMD: ve bang PokeBody, bo renderer boss cu
        if (paintPokemon(g)) {
            return;
        }
        if (this.z != 0) {
            this.frame = (byte) (this.frame + 1);
            if (this.frame > 1) {
                this.frame = (byte) 0;
            }
        }
        if (Res.monsterTemplates[this.monster_type] != null) {
            byte b = Monster.REAL_FRAME[Res.monsterTemplates[this.monster_type].moveType][this.dir][this.frame];
            g.drawImage(Res.imgShadow, this.x, this.y, 3);
            if (this.dynamicEffBottom != null) {
                for (int ii = 0; ii < this.dynamicEffBottom.size(); ii++) {
                    ((DynamicEffect) this.dynamicEffBottom.elementAt(ii)).paint(g, this.x, this.y);
                }
            }
            g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, (b % 4) * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h, trans[b / 4], (this.x - Res.monsterTemplates[this.monster_type].xCenter) + this.vangx, (this.y - Res.monsterTemplates[this.monster_type].yCenter) + this.z + this.vangy, 0);
            if (this.dynamicEffTop != null) {
                for (int ii2 = 0; ii2 < this.dynamicEffTop.size(); ii2++) {
                    ((DynamicEffect) this.dynamicEffTop.elementAt(ii2)).paint(g, this.x, this.y);
                }
            }
        }
        paintExp(g);
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
            Explode ex = (Explode) this.exp.elementAt(i);
            if (ex != null) {
                ex.paint(g);
            }
        }
    }

    private void updateExp() {
        for (int i = 0; i < this.exp.size(); i++) {
            Explode ex = (Explode) this.exp.elementAt(i);
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

    @Override
    public void startDeadFly(int dx, int dy) {
        this.state = (byte) 5;
    }

    private void createExp() {
        if (this.totalExp < 15) {
            this.totalExp = (byte) (this.totalExp + 1);
            Random r = new Random(System.currentTimeMillis());
            this.exp.addElement(new Explode((this.x - 12) + GameScr.abs(r.nextInt() % 25), (this.y - 30) + GameScr.abs(r.nextInt() % 35), 0));
        }
    }

    @Override
    public void update() {
        Skill_Tia_Thuy skill;
        updateExp();
        updateDynamicBuff();
        if (this.z < 0) {
            this.z += this.dz;
            this.dz++;
            if (this.z > 0) {
                this.z = 0;
            }
        }
        this.vangx += this.dvangx;
        this.vangy += this.dvangy;
        if (this.dvangx > 0) {
            this.dvangx--;
        }
        if (this.dvangx < 0) {
            this.dvangx++;
        }
        if (this.dvangy > 0) {
            this.dvangy--;
        }
        if (this.dvangy < 0) {
            this.dvangy++;
        }
        if (this.dvangx == 0 && this.dvangy == 0) {
            if (this.vangx > 1) {
                this.vangx >>= 1;
            } else {
                this.vangx = 0;
            }
            if (this.vangy > 1) {
                this.vangy >>= 1;
            } else {
                this.vangy = 0;
            }
        }
        if (this.realHPSyncTime > 0) {
            this.realHPSyncTime--;
            if (this.realHPSyncTime == 0) {
                if (this.realHP < 0) {
                    this.realHP = 0;
                }
                if (this.hp > this.realHP || this.realHP == 0) {
                    this.hp = this.realHP;
                }
                if (this.hp == 0) {
                    this.state = (byte) 4;
                }
            }
        }
        switch (this.state) {
            case 0:
                this.frame = (byte) 0;
                break;
            case 2:
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 > 6) {
                    this.p1 = (short) 0;
                }
                if (this.p1 > 2) {
                    this.frame = (byte) 1;
                } else {
                    this.frame = (byte) 0;
                }
                move();
                break;
            case 3:
                this.frame = (byte) 2;
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 % 6 == 3) {
                    if (this.targethplost != 0) {
                        Canvas.gameScr.startFlyText(new StringBuffer("-").append(this.targethplost).toString(), 0, this.attackTarget.x + 0, (this.attackTarget.y + this.attackTarget.dy) - 15, 1, -2);
                    } else {
                        Canvas.gameScr.startFlyText("MISS", 0, this.attackTarget.x + 0, (this.attackTarget.y + this.attackTarget.dy) - 15, 1, -2);
                    }
                    if (Monster.REAL_FRAME[Res.monsterTemplates[this.monster_type].moveType][this.dir][this.frame] < 4) {
                        skill = new Skill_Tia_Thuy(this.attackTarget, this.x - 20, this.y - 30);
                    } else {
                        skill = new Skill_Tia_Thuy(this.attackTarget, this.x + 20, this.y - 30);
                    }
                    EffectManager.hiEffects.addElement(skill);
                }
                if (this.p1 > 10) {
                    this.p1 = (short) 0;
                    this.state = (byte) 2;
                }
                break;
            case 5:
                this.frame = (byte) 3;
                this.p1 = (short) (this.p1 + 1);
                this.x = (short) (this.x + this.p2);
                this.y = (short) (this.y + this.p3);
                this.p2 = (short) (this.p2 >> 1);
                this.p3 = (short) (this.p3 >> 1);
                createExp();
                break;
        }
    }

    public void startAttack(LiveActor live, int dam, byte skillID) {
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.attackTarget = live;
        this.targethplost = dam;
    }
}
