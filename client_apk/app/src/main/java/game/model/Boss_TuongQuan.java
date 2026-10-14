package game.model;

import effect.DynamicEffect;
import game.core.j2me.Graphics;
import game.model.skill.Skill_Bua_4;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;

public class Boss_TuongQuan extends Monster {
    Vector exp = new Vector();
    Vector listAttack = new Vector();
    byte[][] MOVE_FRAME = {new byte[]{11, 11, 11, 11, 12, 12, 12, 12}, new byte[]{19, 19, 19, 19, 20, 20, 20, 20}, new byte[]{3, 3, 3, 3, 4, 4, 4, 4}, new byte[]{3, 3, 3, 3, 4, 4, 4, 4}};
    byte[][][] ATT_FRAME = {new byte[][]{new byte[]{13, 13, 13, 13, 13, 13, 13, 13, 14, 14, 15, 15, 15, 15, 15, 15}, new byte[]{21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 23, 23, 23, 23, 23, 23}, new byte[]{5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 7, 7, 7, 7, 7, 7}, new byte[]{5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 7, 7, 7, 7, 7, 7}}, new byte[][]{new byte[]{13, 13, 13, 13, 13, 13, 13, 13, 14, 14, 15, 15, 15, 15, 15, 15}, new byte[]{21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 23, 23, 23, 23, 23, 23}, new byte[]{5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 7, 7, 7, 7, 7, 7}, new byte[]{5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 7, 7, 7, 7, 7, 7}}};
    byte[][] INJURE_FRAME = {new byte[]{10, 10, 10}, new byte[]{18, 18, 18}, new byte[]{2, 2, 2}, new byte[]{2, 2, 2}};
    byte[][] STAND_FRAME = {new byte[]{8, 8, 8, 8, 8, 9, 9, 9, 9, 9}, new byte[]{16, 16, 16, 16, 16, 17, 17, 17, 17, 17}, new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1}};
    byte totalExp = 0;
    private byte skillID = 0;

    @Override
    public void jump() {
    }

    @Override
    public void paint(Graphics graphics) {
        if (!this.isFreeze) {
            if (Res.monsterTemplates[this.monster_type] != null && Res.monsterTemplates[this.monster_type].image != null) {
                if (this.dynamicEffBottom != null) {
                    for (int i = 0; i < this.dynamicEffBottom.size(); i++) {
                        ((DynamicEffect) this.dynamicEffBottom.elementAt(i)).paint(graphics, this.x, this.y);
                    }
                }
                graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, this.frame * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h, this.dir == 3 ? 2 : 0, this.x, this.y - 20, 3);
                if (this.dynamicEffTop != null) {
                    for (int i2 = 0; i2 < this.dynamicEffTop.size(); i2++) {
                        ((DynamicEffect) this.dynamicEffTop.elementAt(i2)).paint(graphics, this.x, this.y);
                    }
                }
            }
            paintExp(graphics);
        }
        super.paintEffSkill(graphics);
    }

    @Override
    public void paintCorner(Graphics graphics, int i, int i2) {
        try {
            if (Res.monsterTemplates[this.monster_type] == null || Res.monsterTemplates[this.monster_type].image == null) {
                return;
            }
            graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, Res.monsterTemplates[this.monster_type].h * 8, Res.monsterTemplates[this.monster_type].w, 35, 0, i - Res.monsterTemplates[this.monster_type].xCenter, i2 - 25, 0);
            graphics.drawRegion(Res.imgNguHanh, this.nh[this.he] * 16, 0, 16, 16, 0, i - 15, i2 + 3, Graphics.TOP | Graphics.LEFT);
        } catch (Exception unused) {
            Cout.println(" loi tai paaintCorner monter");
        }
    }

    @Override
    public void update() {
        updateExp();
        updateDynamicBuff();
        byte b = this.state;
        if (b == 0) {
            this.p1 = (short) (this.p1 + 1);
            if (this.p1 > this.STAND_FRAME[this.dir].length - 1) {
                this.p1 = (short) 0;
            }
            this.frame = this.STAND_FRAME[this.dir][this.p1];
        } else if (b == 2) {
            this.p1 = (short) (this.p1 + 1);
            if (this.p1 > this.MOVE_FRAME[this.dir].length - 1) {
                this.p1 = (short) 0;
            }
            this.frame = this.MOVE_FRAME[this.dir][this.p1];
            if (!this.isFreeze) {
                move();
            }
        } else if (b == 3) {
            this.p1 = (short) (this.p1 + 1);
            short s = this.p1;
            byte[][][] bArr = this.ATT_FRAME;
            byte b2 = this.skillID;
            if (b2 == 2) {
                b2 = 0;
            }
            if (s == bArr[b2][this.dir].length - 7) {
                byte b3 = this.skillID;
                if (b3 == 0) {
                    for (int i = 0; i < this.listAttack.size(); i++) {
                        LiveActor liveActor = (LiveActor) this.listAttack.elementAt(i);
                        GCanvas.gameScr.startNewArrow(7, this, liveActor, this.x - 20, this.y - 50, liveActor.damAttack, (byte) 2, 8);
                        GCanvas.gameScr.startNewArrow(7, this, liveActor, this.x, this.y - 30, liveActor.damAttack, (byte) 2, 8);
                        GCanvas.gameScr.startNewArrow(7, this, liveActor, this.x - 20, this.y - 10, liveActor.damAttack, (byte) 2, 8);
                    }
                } else if (b3 == 1) {
                    GCanvas.gameScr.timerung = 20;
                    EffectManager.lowEffects.addElement(new Skill_Bua_4(this.attackTarget, this.x, this.y, true));
                    if (this.attackTarget.damAttack != 0 && this.attackTarget.damAttack != 2000000) {
                        GCanvas.gameScr.startFlyText("-" + this.attackTarget.damAttack, 0, this.attackTarget.x, this.attackTarget.y - 15, -1, -2);
                    }
                }
            }
            short s2 = this.p1;
            byte[][][] bArr2 = this.ATT_FRAME;
            byte b4 = this.skillID;
            if (b4 == 2) {
                b4 = 0;
            }
            if (s2 > bArr2[b4][this.dir].length - 1) {
                this.p1 = (short) 0;
                this.state = (byte) 2;
            } else {
                byte[][][] bArr3 = this.ATT_FRAME;
                byte b5 = this.skillID;
                this.frame = bArr3[b5 != 2 ? b5 : (byte) 0][this.dir][this.p1];
            }
        } else if (b == 4) {
            this.p1 = (short) (this.p1 + 1);
            if (this.p1 > this.INJURE_FRAME[this.dir].length - 1) {
                this.p1 = (short) 0;
            }
            this.frame = this.INJURE_FRAME[this.dir][this.p1];
        } else if (b == 5) {
            this.frame = (byte) 0;
            createExp();
        }
        updateEffSkill();
    }

    @Override
    public void jumpVang(Actor actor) {
        super.jumpVang(actor);
    }

    private void paintExp(Graphics graphics) {
        int size = this.exp.size();
        for (int i = 0; i < size; i++) {
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
    public void startAttack(Vector vector, byte b) {
        this.listAttack = vector;
        this.state = (byte) 3;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
        this.skillID = b;
        if (b == 1) {
            this.attackTarget = (LiveActor) vector.elementAt(0);
        }
    }

    @Override
    public void setInfo(MonsterInfo monsterInfo) {
        super.setInfo(monsterInfo);
    }
}
