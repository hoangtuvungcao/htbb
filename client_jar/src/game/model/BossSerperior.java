package game.model;

import effect.DynamicEffect;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class BossSerperior extends Monster {
    Vector exp = new Vector();
    Vector listAttack = new Vector();
    byte[][] MOVE_FRAME = {new byte[]{1, 1, 1, 1, 2, 2, 2, 2}, new byte[]{5, 5, 5, 5, 6, 6, 6, 6}, new byte[]{1, 1, 1, 1, 2, 2, 2, 2}, new byte[]{1, 1, 1, 1, 2, 2, 2, 2}};
    byte[][] ATT_FRAME = {new byte[]{3, 3, 3, 3, 3, 3}, new byte[]{7, 7, 7, 7, 7, 7}, new byte[]{3, 3, 3, 3, 3, 3}, new byte[]{3, 3, 3, 3, 3, 3}};
    byte[][] STAND_FRAME = {new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, new byte[]{5, 5, 5, 5, 5, 6, 6, 6, 6, 6}, new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1}};
    byte totalExp = 0;
    private byte skillID;

    @Override
    public void paint(Graphics g) {
        // pokemon: template da la Pokemon (lenh -91) hoac do vat PMD: ve bang PokeBody, bo renderer boss cu
        if (paintPokemon(g)) {
            return;
        }
        if (!this.isFreeze) {
            if (Res.monsterTemplates[this.monster_type] != null && Res.monsterTemplates[this.monster_type].image != null) {
                g.drawImage(Res.imgShadow, this.x, this.y - 5, 3);
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
                g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, 0, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h / 2, 0, xCorner - Res.monsterTemplates[this.monster_type].xCenter, yCorner - 20, 0);
                g.drawRegion(Res.imgNguHanh, this.nh[this.he] * 16, 0, 16, 16, 0, xCorner - 15, yCorner + 3, 20);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Cout.println(" loi tai paaintCorner monter");
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
        updateExp();
        updateDynamicBuff();
        switch (this.state) {
            case 0:
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 > this.STAND_FRAME[this.dir].length - 1) {
                    this.p1 = (short) 0;
                }
                this.frame = this.STAND_FRAME[this.dir][this.p1];
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
                    EffectManager.addLowEffect(this.x - 20, this.y - 14, 30);
                    EffectManager.addLowEffect(this.x + 20, this.y - 14, 30);
                    EffectManager.addLowEffect(this.x - 20, this.y - 4, 30);
                    EffectManager.addLowEffect(this.x + 20, this.y - 4, 30);
                    EffectManager.addLowEffect(this.x, this.y + 4, 30);
                }
                if (this.p1 > this.ATT_FRAME[this.dir].length - 1) {
                    this.p1 = (short) 0;
                    if (this.skillID == 0) {
                        for (int i = 0; i < this.listAttack.size(); i++) {
                            LiveActor acto = (LiveActor) this.listAttack.elementAt(i);
                            Canvas.gameScr.SkillofBoss_Snake(this, acto, 4, 52);
                        }
                    } else if (this.skillID != 1) {
                        for (int i2 = 0; i2 < this.listAttack.size(); i2++) {
                            LiveActor acTo = (LiveActor) this.listAttack.elementAt(i2);
                            if (acTo != null) {
                                int hpL = acTo.damAttack;
                                acTo.doublejump();
                                Canvas.gameScr.startFlyText(new StringBuffer("-").append(hpL).toString(), 0, acTo.x + 0, (acTo.y + acTo.dy) - 15, 1, -2);
                                for (int j = 0; j < 15; j++) {
                                    EffectManager.addHiEffect((acTo.x - 12) + GameScr.abs(LiveActor.r.nextInt() % 25), (acTo.y - 30) + GameScr.abs(LiveActor.r.nextInt() % 35), 33);
                                }
                            }
                        }
                    }
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
                this.frame = (byte) 3;
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
