package game.model;

import effect.DynamicEffect;
import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;

public class Boss_Xuong extends Monster {
    private byte skillID;
    Vector exp = new Vector();
    Vector listAttack = new Vector();
    byte[][] MOVE_FRAME = {new byte[]{10, 10, 10, 10, 11, 11, 11, 11}, new byte[]{19, 19, 19, 19, 20, 20, 20, 20}, new byte[]{1, 1, 1, 1, 2, 2, 2, 2}, new byte[]{1, 1, 1, 1, 2, 2, 2, 2}};
    byte[][][] ATT_FRAME = {new byte[][]{new byte[]{12, 12, 12, 12, 12, 12, 12, 12, 15, 15, 16, 16, 16, 16, 16, 16}, new byte[]{21, 21, 21, 21, 21, 21, 21, 21, 24, 24, 25, 25, 25, 25, 25, 25}, new byte[]{3, 3, 3, 3, 3, 3, 3, 3, 6, 6, 7, 7, 7, 7, 7, 7}, new byte[]{3, 3, 3, 3, 3, 3, 3, 3, 6, 6, 7, 7, 7, 7, 7, 7}}, new byte[][]{new byte[]{12, 12, 12, 12, 12, 12, 12, 12, 13, 13, 14, 14, 14, 14, 14, 14}, new byte[]{21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 23, 23, 23, 23, 23, 23}, new byte[]{3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 5, 5, 5, 5, 5, 5}, new byte[]{3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 5, 5, 5, 5, 5, 5}}};
    byte[][] INJURE_FRAME = {new byte[]{17, 17, 17}, new byte[]{26, 26, 26}, new byte[]{8, 8, 8}, new byte[]{8, 8, 8}};
    byte[][] STAND_FRAME = {new byte[]{9, 9, 9, 9, 9, 10, 10, 10, 10, 10}, new byte[]{18, 18, 18, 18, 18, 19, 19, 19, 19, 19}, new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1}};
    byte totalExp = 0;

    @Override
    public void jump() {
    }

    public Boss_Xuong() {
        this.xTo = this.x;
        this.yTo = this.y;
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
                graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, this.frame * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h, this.dir == 3 ? 2 : 0, this.x, this.y + 10, 33);
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
            graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, 0, Res.monsterTemplates[this.monster_type].w, 50, 0, i - Res.monsterTemplates[this.monster_type].xCenter, i2 - 30, 0);
            graphics.drawRegion(Res.imgNguHanh, this.nh[this.he] * 16, 0, 16, 16, 0, i - 15, i2 + 3, Graphics.TOP | Graphics.LEFT);
        } catch (Exception unused) {
            Cout.println(" loi tai paaintCorner monter");
        }
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
                int size = this.listAttack.size();
                for (int i = 0; i < size; i++) {
                    LiveActor liveActor = (LiveActor) this.listAttack.elementAt(i);
                    GameScr gameScr = GCanvas.gameScr;
                    byte b3 = this.skillID;
                    if (b3 == 2) {
                        b3 = 0;
                    }
                    gameScr.creatWeaponFire(this, liveActor, b3 + 3);
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
    }
}
