package game.model;

import effect.DynamicEffect;
import game.core.j2me.Graphics;
import game.model.skill.Skill_Tia_Thuy;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;

public class BossTL extends Monster {
    private static int[] trans = {0, 2};
    Vector exp = new Vector();
    byte totalExp = 0;

    @Override
    public void jump() {
    }

    @Override
    public void paint(Graphics graphics) {
        if (Res.monsterTemplates[this.monster_type] != null) {
            byte b = REAL_FRAME[Res.monsterTemplates[this.monster_type].moveType][this.dir][this.frame];
            graphics.drawImage(Res.imgShadow, this.x, this.y, 3);
            if (this.dynamicEffBottom != null) {
                for (int i = 0; i < this.dynamicEffBottom.size(); i++) {
                    ((DynamicEffect) this.dynamicEffBottom.elementAt(i)).paint(graphics, this.x, this.y);
                }
            }
            graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, (b % 4) * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h, trans[b / 4], (this.x - Res.monsterTemplates[this.monster_type].xCenter) + this.vangx, (this.y - Res.monsterTemplates[this.monster_type].yCenter) + this.z + this.vangy, 0);
            if (this.dynamicEffTop != null) {
                for (int i2 = 0; i2 < this.dynamicEffTop.size(); i2++) {
                    ((DynamicEffect) this.dynamicEffTop.elementAt(i2)).paint(graphics, this.x, this.y);
                }
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
            Explode explode = (Explode) this.exp.elementAt(i);
            if (explode != null) {
                explode.paint(graphics);
            }
        }
    }

    private void updateExp() {
        for (int i = 0; i < this.exp.size(); i++) {
            Explode explode = (Explode) this.exp.elementAt(i);
            if (explode != null) {
                explode.update();
                if (explode.destroy) {
                    this.exp.removeElement(explode);
                    if (this.exp.size() == 0) {
                        this.wantDestroy = true;
                    }
                }
            }
        }
    }

    @Override
    public void startDeadFly(int i, int i2) {
        this.state = (byte) 5;
    }

    private void createExp() {
        byte b = this.totalExp;
        if (b < 15) {
            this.totalExp = (byte) (b + 1);
            Random random = new Random(System.currentTimeMillis());
            this.exp.addElement(new Explode((this.x - 12) + GameScr.abs(random.nextInt() % 25), (this.y - 30) + GameScr.abs(random.nextInt() % 35), 0));
        }
    }

    @Override
    public void update() {
        Skill_Tia_Thuy skill_Tia_Thuy;
        updateDynamicBuff();
        updateExp();
        if (this.z < 0) {
            this.z += this.dz;
            this.dz++;
            if (this.z > 0) {
                this.z = 0;
            }
        }
        if (this.z != 0) {
            this.frame = (byte) (this.frame + 1);
            if (this.frame > 1) {
                this.frame = (byte) 0;
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
        byte b = this.state;
        if (b == 0) {
            this.frame = (byte) 0;
            return;
        }
        if (b == 5) {
            this.frame = (byte) 3;
            this.p1 = (short) (this.p1 + 1);
            this.x = (short) (this.x + this.p2);
            this.y = (short) (this.y + this.p3);
            this.p2 = (short) (this.p2 >> 1);
            this.p3 = (short) (this.p3 >> 1);
            createExp();
            return;
        }
        if (b == 2) {
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
            return;
        }
        if (b != 3) {
            return;
        }
        this.frame = (byte) 2;
        this.p1 = (short) (this.p1 + 1);
        if (this.p1 % 6 == 3) {
            if (this.targethplost != 0) {
                GCanvas.gameScr.startFlyText("-" + this.targethplost, 0, this.attackTarget.x, (this.attackTarget.y + this.attackTarget.dy) - 15, 1, -2);
            } else {
                GCanvas.gameScr.startFlyText("MISS", 0, this.attackTarget.x, (this.attackTarget.y + this.attackTarget.dy) - 15, 1, -2);
            }
            if (REAL_FRAME[Res.monsterTemplates[this.monster_type].moveType][this.dir][this.frame] < 4) {
                skill_Tia_Thuy = new Skill_Tia_Thuy(this.attackTarget, this.x - 20, this.y - 30);
            } else {
                skill_Tia_Thuy = new Skill_Tia_Thuy(this.attackTarget, this.x + 20, this.y - 30);
            }
            EffectManager.hiEffects.addElement(skill_Tia_Thuy);
        }
        if (this.p1 > 10) {
            this.p1 = (short) 0;
            this.state = (byte) 2;
        }
    }

    public void startAttack(LiveActor liveActor, int i, byte b) {
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.attackTarget = liveActor;
        this.targethplost = i;
    }
}
