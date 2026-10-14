package game.model;

import effect.DynamicEffect;
import game.core.j2me.Graphics;
import game.model.skill.ThienThach_Skill;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;

public class Boss_Eagle extends Monster {
    int aa;
    int bb;
    private byte f;
    private byte frame;
    private byte skillID;
    private byte transChan;
    Vector exp = new Vector();
    Vector listAttack = new Vector();
    private byte[] FRAME = {4, 6, 5};
    private byte maxCount = 10;
    byte totalExp = 0;
    long timeGetImage = System.currentTimeMillis();

    @Override
    public void jump() {
    }

    @Override
    public void paint(Graphics graphics) {
        if (!this.isFreeze) {
            if (Res.monsterTemplates[this.monster_type].imageObj != null) {
                graphics.drawImage(Res.imgShadow, this.x, this.y + 15, 3);
                if (this.dynamicEffBottom != null) {
                    for (int i = 0; i < this.dynamicEffBottom.size(); i++) {
                        ((DynamicEffect) this.dynamicEffBottom.elementAt(i)).paint(graphics, this.x, this.y);
                    }
                }
                Res.monsterTemplates[this.monster_type].imageObj.paint(graphics, this.x, this.bb, 0, 0, 1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(graphics, this.x, this.bb, this.f + 1, this.transChan, 1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(graphics, this.x, this.bb, this.FRAME[this.aa], 0, 1, 1);
                Res.monsterTemplates[this.monster_type].imageObj.paint(graphics, this.x, this.bb, this.FRAME[this.aa], 2, -1, 1);
                if (this.state == 3) {
                    Res.monsterTemplates[this.monster_type].imageObj.paint(graphics, this.x, this.bb, 3, 0, 1, 1);
                }
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
            paintExp(graphics);
        }
        super.paintEffSkill(graphics);
    }

    private void changeChan(int i) {
        if (this.z > 20) {
            this.f = (byte) i;
            this.z = 0;
        }
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
        updateDynamicBuff();
        if (!this.isFreeze && Res.monsterTemplates[this.monster_type].imageObj != null) {
            this.aa = this.frame / 5;
            short s = this.y;
            int i = this.aa;
            this.bb = s - (i * 3);
            if (i > 2) {
                this.aa = 2;
            }
        }
        this.z++;
        byte b = (byte) (this.frame + 1);
        this.frame = b;
        int i2 = 0;
        if (b >= this.maxCount) {
            this.frame = (byte) 0;
        }
        this.transChan = (byte) 0;
        updateExp();
        byte b2 = this.state;
        if (b2 == 0) {
            this.maxCount = (byte) 10;
            changeChan(1);
        } else if (b2 == 5) {
            changeChan(1);
            createExp();
        } else if (b2 == 2) {
            this.maxCount = (byte) 10;
            changeChan(0);
            if (this.frame / 5 == 1 && this.f == 0) {
                this.transChan = (byte) 2;
            }
            move();
        } else if (b2 == 3) {
            this.maxCount = (byte) 20;
            changeChan(0);
            this.p1 = (short) (this.p1 + 1);
            if (this.frame / 5 > 2 && this.p1 > 20) {
                this.p1 = (short) 0;
                this.state = (byte) 2;
                if (this.skillID == 1) {
                    byte[] bArr = {-1, 1};
                    for (int i3 = 0; i3 < 30; i3++) {
                        ArrowTarget arrowTarget = new ArrowTarget((byte) 6);
                        int iRnd = Res.rnd(2);
                        int i4 = this.x + (bArr[iRnd] * 25);
                        int i5 = this.y - ((this.frame / 5) * 3);
                        short s2 = this.x;
                        int iRnd2 = Res.rnd(200);
                        byte b3 = bArr[iRnd];
                        arrowTarget.set(0, i4, i5, 0, (byte) 0, (s2 + (iRnd2 * b3)) - (b3 * 10), this.y + 20 + Res.rnd(150), (byte) (Res.rnd(10) + 15), false);
                        GameScr gameScr = GCanvas.gameScr;
                        GameScr.arrowsUp.addElement(arrowTarget);
                    }
                    int size = this.listAttack.size();
                    while (i2 < size) {
                        LiveActor liveActor = (LiveActor) this.listAttack.elementAt(i2);
                        ArrowTarget arrowTarget2 = new ArrowTarget((byte) 6);
                        arrowTarget2.set(0, this.x + (bArr[Res.rnd(2)] * 25), this.y - ((this.frame / 5) * 3), liveActor.damAttack, (byte) 0, liveActor.x, liveActor.y, (byte) 20, true);
                        GameScr gameScr2 = GCanvas.gameScr;
                        GameScr.arrowsUp.addElement(arrowTarget2);
                        i2++;
                    }
                } else {
                    int size2 = this.listAttack.size();
                    while (i2 < size2) {
                        LiveActor liveActor2 = (LiveActor) this.listAttack.elementAt(i2);
                        EffectManager.hiEffects.addElement(new ThienThach_Skill(liveActor2.x, liveActor2.y, liveActor2.damAttack));
                        i2++;
                    }
                }
            }
        }
        updateEffSkill();
    }

    @Override
    public void startAttack(Vector vector, byte b) {
        this.listAttack = vector;
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.skillID = b;
    }

    @Override
    public void setInfo(MonsterInfo monsterInfo) {
        super.setInfo(monsterInfo);
        Res.monsterTemplates[this.monster_type].w = (byte) 50;
        this.timeGetImage = System.currentTimeMillis() + 10000;
        this.height = (short) 100;
    }
}
