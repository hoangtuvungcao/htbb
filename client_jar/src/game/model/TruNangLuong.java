package game.model;

import effect.DynamicEffect;
import game.model.skill.Skill_TruTranThanh;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class TruNangLuong extends Monster {
    int typeID;
    public static boolean isChopSang;
    public static Image img;
    int dem;
    int lap;
    Vector exp = new Vector();
    byte[] ATT_FRAME = {0, 0, 1, 1, 1, 1, 1, 1};
    byte[] STAND_FRAME = {0, 0, 0, 1, 1, 1};
    byte totalExp = 0;
    Vector listTarget = new Vector();

    public TruNangLuong(int TYPE) {
        this.typeID = TYPE;
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
                g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, this.frame * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h, 0, this.x - (Res.monsterTemplates[this.monster_type].w / 2), this.y - Res.monsterTemplates[this.monster_type].h, 0);
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
                g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, 0 * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, 40, 0, xCorner - Res.monsterTemplates[this.monster_type].xCenter, yCorner - 20, 0);
                g.drawRegion(Res.imgNguHanh, this.nh[this.he] * 16, 0, 16, 16, 0, xCorner - 15, yCorner + 3, 20);
            }
        } catch (Exception e) {
            Cout.println(" loi tai paintCorner monter");
        }
    }

    @Override
    public void update() {
        updateDynamicBuff();
        updateExp();
        switch (this.state) {
            case 0:
                if (this.typeID == 0) {
                    this.frame = (byte) 0;
                    this.listTarget.removeAllElements();
                } else if (this.typeID == 1 && isChopSang) {
                    this.lap++;
                    if (this.lap > this.STAND_FRAME.length - 1) {
                        this.lap = 0;
                    }
                    this.frame = this.STAND_FRAME[this.lap];
                } else {
                    this.frame = (byte) 0;
                }
                break;
            case 2:
                this.frame = (byte) 0;
                break;
            case 3:
                if (this.typeID == 0) {
                    this.p1 = (short) (this.p1 + 1);
                    if (this.p1 == this.ATT_FRAME.length - 5) {
                        EffectManager.addHiEffect(this.x - 3, this.y - 30, 24);
                        for (int i = 0; i < 10; i++) {
                            Skill_TruTranThanh s = new Skill_TruTranThanh(this.x - 5, this.y - 70, this, this.listTarget);
                            EffectManager.hiEffects.addElement(s);
                        }
                    }
                    if (this.p1 >= this.ATT_FRAME.length - 3 && this.p1 <= this.ATT_FRAME.length - 1) {
                        EffectManager.addHiEffect(this.x - 5, this.y - 65, 3);
                    }
                    if (this.p1 == this.ATT_FRAME.length - 1 && this.listTarget.size() > 0) {
                        for (int i2 = 0; i2 < this.listTarget.size(); i2++) {
                            LiveActor ac = (LiveActor) this.listTarget.elementAt(i2);
                            Canvas.gameScr.startWeaponsLazer(this, ac, 0, -65, 0, ac.damAttack, 2);
                        }
                    }
                    if (this.p1 > this.ATT_FRAME.length - 1) {
                        this.p1 = (short) 0;
                        this.state = (byte) 0;
                    }
                    this.frame = this.ATT_FRAME[this.p1];
                } else {
                    this.frame = (byte) 0;
                }
                break;
            case 4:
                this.frame = (byte) 0;
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
            ex.paint(g);
        }
    }

    private void updateExp() {
        for (int i = 0; i < this.exp.size(); i++) {
            Explode ex = (Explode) this.exp.elementAt(i);
            ex.update();
            if (ex.destroy) {
                this.exp.removeElement(ex);
                if (this.exp.size() == 0) {
                    this.wantDestroy = true;
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
        this.state = (byte) 3;
        this.listTarget = list;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
    }

    @Override
    public void setInfo(MonsterInfo monsterInfo) {
        super.setInfo(monsterInfo);
    }

    @Override
    public boolean allwayPaint() {
        return true;
    }
}
