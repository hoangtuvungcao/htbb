package game.model;

import effect.DynamicEffect;
import game.model.skill.ThienThach_Skill;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class BossZapdos extends Monster {
    private byte frame;
    private byte transChan;
    private byte f;
    private byte skillID;
    Vector exp = new Vector();
    Vector listAttack = new Vector();
    private byte[] FRAME = {4, 6, 5};
    private byte maxCount = 10;
    byte totalExp = 0;
    long timeGetImage = System.currentTimeMillis();

    @Override
    public void paint(Graphics g) {
        // pokemon: template da la Pokemon (lenh -91) hoac do vat PMD: ve bang PokeBody, bo renderer boss cu
        if (paintPokemon(g)) {
            return;
        }
        if (!this.isFreeze) {
            if (Res.monsterTemplates[this.monster_type].imageObj != null) {
                g.drawImage(Res.imgShadow, this.x, this.y + 15, 3);
                if (this.dynamicEffBottom != null) {
                    for (int ii = 0; ii < this.dynamicEffBottom.size(); ii++) {
                        ((DynamicEffect) this.dynamicEffBottom.elementAt(ii)).paint(g, this.x, this.y);
                    }
                }
                int aa = this.frame / 5;
                int bb = this.y - (aa * 3);
                if (aa > 2) {
                    aa = 2;
                }
                Res.monsterTemplates[this.monster_type].imageObj.paint(g, this.x, bb, 0, 0, 1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(g, this.x, bb, this.f + 1, this.transChan, 1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(g, this.x, bb, this.FRAME[aa], 0, 1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(g, this.x, bb, this.FRAME[aa], 2, -1, 1);
                if (this.state == 3) {
                    Res.monsterTemplates[this.monster_type].imageObj.paint(g, this.x, bb, 3, 0, 1, 1);
                }
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
            paintExp(g);
        }
        super.paintEffSkill(g);
    }

    @Override
    public void jump() {
    }

    private void changeChan(int index) {
        if (this.z > 20) {
            this.f = (byte) index;
            this.z = 0;
        }
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
        updateDynamicBuff();
        this.z++;
        this.frame = (byte) (this.frame + 1);
        if (this.frame >= this.maxCount) {
            this.frame = (byte) 0;
        }
        this.transChan = (byte) 0;
        updateExp();
        switch (this.state) {
            case 0:
                this.maxCount = (byte) 10;
                changeChan(1);
                break;
            case 2:
                this.maxCount = (byte) 10;
                changeChan(0);
                if (this.frame / 5 == 1 && this.f == 0) {
                    this.transChan = (byte) 2;
                }
                move();
                break;
            case 3:
                this.maxCount = (byte) 20;
                changeChan(0);
                this.p1 = (short) (this.p1 + 1);
                if (this.frame / 5 > 2 && this.p1 > 20) {
                    this.p1 = (short) 0;
                    this.state = (byte) 2;
                    if (this.skillID == 1) {
                        byte[] cou = {-1, 1};
                        for (int i = 0; i < 30; i++) {
                            ArrowTarget a = new ArrowTarget((byte) 6);
                            int aa = Res.rnd(2);
                            a.set(0, this.x + (cou[aa] * 25), this.y - ((this.frame / 5) * 3), 0, (byte) 0, (this.x + (Res.rnd(200) * cou[aa])) - (10 * cou[aa]), this.y + 20 + Res.rnd(150), (byte) (Res.rnd(10) + 15), false);
                            GameScr.arrowsUp.addElement(a);
                        }
                        for (int i2 = 0; i2 < this.listAttack.size(); i2++) {
                            LiveActor live = (LiveActor) this.listAttack.elementAt(i2);
                            ArrowTarget a2 = new ArrowTarget((byte) 6);
                            a2.set(0, this.x + (cou[Res.rnd(2)] * 25), this.y - ((this.frame / 5) * 3), live.damAttack, (byte) 0, live.x, live.y, (byte) 20, true);
                            GameScr.arrowsUp.addElement(a2);
                        }
                    } else {
                        for (int i3 = 0; i3 < this.listAttack.size(); i3++) {
                            LiveActor live2 = (LiveActor) this.listAttack.elementAt(i3);
                            ThienThach_Skill rain = new ThienThach_Skill(live2.x, live2.y, live2.damAttack);
                            EffectManager.hiEffects.addElement(rain);
                        }
                    }
                }
                break;
            case 5:
                changeChan(1);
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
        Res.monsterTemplates[this.monster_type].w = (byte) 50;
        this.timeGetImage = System.currentTimeMillis() + 10000;
        this.height = (short) 100;
    }
}
