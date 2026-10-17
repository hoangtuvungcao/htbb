package game.model;

import effect.DynamicEffect;
import game.model.skill.Skill_Bua_4;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Boss_TuongQuan extends Monster {
    public static Image image;
    Vector exp = new Vector();
    Vector listAttack = new Vector();
    byte[][] MOVE_FRAME = {new byte[]{11, 11, 11, 11, 12, 12, 12, 12}, new byte[]{19, 19, 19, 19, 20, 20, 20, 20}, new byte[]{3, 3, 3, 3, 4, 4, 4, 4}, new byte[]{3, 3, 3, 3, 4, 4, 4, 4}};
    byte[][][] ATT_FRAME = {new byte[][]{new byte[]{13, 13, 13, 13, 13, 13, 13, 13, 14, 14, 15, 15, 15, 15, 15, 15}, new byte[]{21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 23, 23, 23, 23, 23, 23}, new byte[]{5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 7, 7, 7, 7, 7, 7}, new byte[]{5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 7, 7, 7, 7, 7, 7}}, new byte[][]{new byte[]{13, 13, 13, 13, 13, 13, 13, 13, 14, 14, 15, 15, 15, 15, 15, 15}, new byte[]{21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 23, 23, 23, 23, 23, 23}, new byte[]{5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 7, 7, 7, 7, 7, 7}, new byte[]{5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 7, 7, 7, 7, 7, 7}}};
    byte[][] INJURE_FRAME = {new byte[]{10, 10, 10}, new byte[]{18, 18, 18}, new byte[]{2, 2, 2}, new byte[]{2, 2, 2}};
    byte[][] STAND_FRAME = {new byte[]{8, 8, 8, 8, 8, 9, 9, 9, 9, 9}, new byte[]{16, 16, 16, 16, 16, 17, 17, 17, 17, 17}, new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1}};
    byte totalExp = 0;
    private byte skillID = 0;

    static {
        try {
            image = Image.createImage("/m/bosshothanh.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g) {
        // pokemon: template da la Pokemon (lenh -91) hoac do vat PMD: ve bang PokeBody, bo renderer boss cu
        if (paintPokemon(g)) {
            return;
        }
        if (!this.isFreeze) {
            if (Res.monsterTemplates[this.monster_type] != null && Res.monsterTemplates[this.monster_type].image != null) {
                if (this.dynamicEffBottom != null) {
                    for (int ii = 0; ii < this.dynamicEffBottom.size(); ii++) {
                        ((DynamicEffect) this.dynamicEffBottom.elementAt(ii)).paint(g, this.x, this.y);
                    }
                }
                g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, this.frame * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h, this.dir == 3 ? 2 : 0, this.x, this.y - 20, 3);
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
                g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, 8 * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, 35, 0, xCorner - Res.monsterTemplates[this.monster_type].xCenter, yCorner - 25, 0);
                g.drawRegion(Res.imgNguHanh, this.nh[this.he] * 16, 0, 16, 16, 0, xCorner - 15, yCorner + 3, 20);
            }
        } catch (Exception e) {
            Cout.println(" loi tai paaintCorner monter");
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
                if (this.p1 == this.ATT_FRAME[this.skillID == 2 ? (byte) 0 : this.skillID][this.dir].length - 7) {
                    if (this.skillID == 0) {
                        for (int i = 0; i < this.listAttack.size(); i++) {
                            LiveActor acto = (LiveActor) this.listAttack.elementAt(i);
                            Canvas.gameScr.startNewArrow(7, this, acto, this.x - 20, this.y - 50, acto.damAttack, (byte) 2, 8);
                            Canvas.gameScr.startNewArrow(7, this, acto, this.x, this.y - 30, acto.damAttack, (byte) 2, 8);
                            Canvas.gameScr.startNewArrow(7, this, acto, this.x - 20, this.y - 10, acto.damAttack, (byte) 2, 8);
                            acto.doublejump();
                        }
                    } else if (this.skillID == 1) {
                        Canvas.gameScr.timerung = 20;
                        EffectManager.lowEffects.addElement(new Skill_Bua_4(this.attackTarget, this.x, this.y, true));
                        if (this.attackTarget.damAttack != 0 && this.attackTarget.damAttack != 2000000) {
                            Canvas.gameScr.startFlyText(new StringBuffer("-").append(this.attackTarget.damAttack).toString(), 0, this.attackTarget.x + 0, this.attackTarget.y - 15, -1, -2);
                        }
                    }
                }
                if (this.p1 > this.ATT_FRAME[this.skillID == 2 ? (byte) 0 : this.skillID][this.dir].length - 1) {
                    this.p1 = (short) 0;
                    this.state = (byte) 2;
                } else {
                    this.frame = this.ATT_FRAME[this.skillID == 2 ? (byte) 0 : this.skillID][this.dir][this.p1];
                }
                break;
            case 4:
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 > this.INJURE_FRAME[this.dir].length - 1) {
                    this.p1 = (short) 0;
                }
                this.frame = this.INJURE_FRAME[this.dir][this.p1];
                break;
            case 5:
                this.frame = (byte) 0;
                createExp();
                break;
        }
        updateEffSkill();
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
    public void startAttack(Vector list, byte skillID) {
        this.listAttack = list;
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.skillID = skillID;
        if (skillID == 1) {
            this.attackTarget = (LiveActor) list.elementAt(0);
        }
    }

    @Override
    public void setInfo(MonsterInfo monsterInfo) {
        super.setInfo(monsterInfo);
    }
}
