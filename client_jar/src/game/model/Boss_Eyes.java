package game.model;

import effect.DynamicEffect;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class Boss_Eyes extends Monster {
    Vector exp = new Vector();
    Vector listAttack = new Vector();
    byte[][] MOVE_FRAME = {new byte[]{0, 0, 0, 1, 1, 1}, new byte[]{4, 4, 4, 5, 5, 5}, new byte[]{0, 0, 0, 1, 1, 1}, new byte[]{0, 0, 0, 1, 1, 1}};
    byte[][] ATT_FRAME = {new byte[]{2, 2, 2, 2, 2, 2}, new byte[]{6, 6, 6, 6, 6, 6}, new byte[]{2, 2, 2, 2, 2, 2}, new byte[]{2, 2, 2, 2, 2, 2}};
    byte totalExp = 0;
    private byte skillID;

    @Override
    public void paint(Graphics g) {
        if (!this.isFreeze) {
            if (Res.monsterTemplates[this.monster_type] != null && Res.monsterTemplates[this.monster_type].image != null) {
                if (this.dynamicEffBottom != null) {
                    for (int ii = 0; ii < this.dynamicEffBottom.size(); ii++) {
                        ((DynamicEffect) this.dynamicEffBottom.elementAt(ii)).paint(g, this.x, this.y);
                    }
                }
                g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, this.frame * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h, this.dir == 3 ? 2 : 0, this.x, this.y, 33);
                if (this.dynamicEffTop != null) {
                    for (int ii2 = 0; ii2 < this.dynamicEffTop.size(); ii2++) {
                        ((DynamicEffect) this.dynamicEffTop.elementAt(ii2)).paint(g, this.x, this.y);
                    }
                }
            }
            paintExp(g);
        }
        super.paintEffSkill(g);
    }

    @Override
    public void paintCorner(Graphics g, int xCorner, int yCorner) {
        try {
            if (Res.monsterTemplates[this.monster_type] != null && Res.monsterTemplates[this.monster_type].image != null) {
                g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, this.frame * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h, 0, xCorner - Res.monsterTemplates[this.monster_type].xCenter, yCorner - 20, 0);
                g.drawRegion(Res.imgNguHanh, this.nh[this.he] * 16, 0, 16, 16, 0, xCorner - 15, yCorner + 3, 20);
            }
        } catch (Exception e) {
        }
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
        int yadd;
        int i;
        int yadd2;
        int xadd;
        updateExp();
        updateDynamicBuff();
        switch (this.state) {
            case 0:
                if (this.dir != 1) {
                    this.frame = (byte) 0;
                } else {
                    this.frame = (byte) 4;
                }
                break;
            case 2:
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 > this.MOVE_FRAME[this.dir].length - 1) {
                    this.p1 = (short) 0;
                }
                this.frame = this.MOVE_FRAME[this.dir][this.p1];
                if (!this.isFreeze) {
                    move();
                }
                break;
            case 3:
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 % 3 == 0) {
                    EffectManager.addLowEffect(this.x - 20, this.y - 14, 4);
                    EffectManager.addLowEffect(this.x + 20, this.y - 14, 4);
                    EffectManager.addLowEffect(this.x - 20, this.y - 4, 4);
                    EffectManager.addLowEffect(this.x + 20, this.y - 4, 4);
                    EffectManager.addLowEffect(this.x, this.y + 4, 4);
                }
                if (this.skillID == 0 && this.p1 == 1) {
                    if (this.dir == 3) {
                        yadd2 = -22;
                        xadd = 10;
                    } else {
                        yadd2 = -22;
                        xadd = -10;
                    }
                    if (this.dir != 1) {
                        EffectManager.addHiEffect(this.x + xadd, this.y + yadd2, 37);
                    }
                }
                if (this.p1 > this.ATT_FRAME[this.dir].length - 1) {
                    if (this.skillID == 0) {
                        for (int i2 = 0; i2 < this.listAttack.size(); i2++) {
                            LiveActor acto = (LiveActor) this.listAttack.elementAt(i2);
                            if (this.dir == 3) {
                                yadd = -22;
                                i = 10;
                            } else {
                                yadd = -22;
                                i = -10;
                            }
                            int xadd2 = i;
                            EffectManager.addHiEffect(this.x + xadd2, this.y + yadd, 11);
                            Canvas.gameScr.startWeaponsLazer(this, acto, xadd2, yadd, this.dir, acto.damAttack, 0);
                        }
                    } else if (this.skillID == 1) {
                        for (int i3 = 0; i3 < this.listAttack.size(); i3++) {
                            Canvas.gameScr.startWeapsMango(this, (LiveActor) this.listAttack.elementAt(i3), 30);
                        }
                    } else {
                        for (int i4 = 0; i4 < this.listAttack.size(); i4++) {
                            LiveActor acTo = (LiveActor) this.listAttack.elementAt(i4);
                            if (acTo != null) {
                                int hpL = acTo.damAttack;
                                acTo.doublejump();
                                Canvas.gameScr.startFlyText(new StringBuffer("-").append(hpL).toString(), 0, acTo.x + 0, (acTo.y + acTo.dy) - 15, 1, -2);
                                for (int j = 0; j < 10; j++) {
                                    EffectManager.addHiEffect((acTo.x - 12) + GameScr.abs(LiveActor.r.nextInt() % 25), (acTo.y - 30) + GameScr.abs(LiveActor.r.nextInt() % 35), 28);
                                }
                            }
                        }
                    }
                    this.p1 = (short) 0;
                    this.state = (byte) 2;
                }
                this.frame = this.ATT_FRAME[this.dir][this.p1];
                break;
            case 4:
                if (this.dir != 1) {
                    this.frame = (byte) 3;
                } else {
                    this.frame = (byte) 7;
                }
                break;
            case 5:
                this.frame = (byte) 0;
                createExp();
                break;
        }
        updateEffSkill();
    }

    @Override
    public void startAttack(Vector list, byte skillID) {
        this.listAttack = list;
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.skillID = skillID;
    }

    @Override
    public void setInfo(MonsterInfo monsterInfo) {
        super.setInfo(monsterInfo);
    }
}
