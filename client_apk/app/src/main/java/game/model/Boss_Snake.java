package game.model;

import effect.DynamicEffect;
import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;

public class Boss_Snake extends Monster {
    private byte skillID;
    Vector exp = new Vector();
    Vector listAttack = new Vector();
    byte[][] MOVE_FRAME = {new byte[]{1, 1, 1, 1, 2, 2, 2, 2}, new byte[]{5, 5, 5, 5, 6, 6, 6, 6}, new byte[]{1, 1, 1, 1, 2, 2, 2, 2}, new byte[]{1, 1, 1, 1, 2, 2, 2, 2}};
    byte[][] ATT_FRAME = {new byte[]{3, 3, 3, 3, 3, 3}, new byte[]{7, 7, 7, 7, 7, 7}, new byte[]{3, 3, 3, 3, 3, 3}, new byte[]{3, 3, 3, 3, 3, 3}};
    byte[][] STAND_FRAME = {new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, new byte[]{5, 5, 5, 5, 5, 6, 6, 6, 6, 6}, new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1}};
    byte totalExp = 0;

    @Override
    public void jump() {
    }

    @Override
    public void paint(Graphics graphics) {
        if (!this.isFreeze) {
            if (Res.monsterTemplates[this.monster_type] != null && Res.monsterTemplates[this.monster_type].image != null) {
                graphics.drawImage(Res.imgShadow, this.x, this.y - 5, 3);
                if (this.dynamicEffBottom != null) {
                    for (int i = 0; i < this.dynamicEffBottom.size(); i++) {
                        ((DynamicEffect) this.dynamicEffBottom.elementAt(i)).paint(graphics, this.x, this.y);
                    }
                }
                graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, this.frame * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h, this.dir == 3 ? 2 : 0, this.x, this.y, 33);
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
            graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, 0, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h / 2, 0, i - Res.monsterTemplates[this.monster_type].xCenter, i2 - 20, 0);
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
            if (this.p1 % 3 == 0) {
                EffectManager.addLowEffect(this.x - 20, this.y - 14, 30);
                EffectManager.addLowEffect(this.x + 20, this.y - 14, 30);
                EffectManager.addLowEffect(this.x - 20, this.y - 4, 30);
                EffectManager.addLowEffect(this.x + 20, this.y - 4, 30);
                EffectManager.addLowEffect(this.x, this.y + 4, 30);
            }
            if (this.p1 > this.ATT_FRAME[this.dir].length - 1) {
                this.p1 = (short) 0;
                byte b2 = this.skillID;
                if (b2 == 0) {
                    int size = this.listAttack.size();
                    for (int i = 0; i < size; i++) {
                        GCanvas.gameScr.SkillofBoss_Snake(this, (LiveActor) this.listAttack.elementAt(i), 4, 52);
                    }
                } else if (b2 != 1) {
                    int size2 = this.listAttack.size();
                    for (int i2 = 0; i2 < size2; i2++) {
                        LiveActor liveActor = (LiveActor) this.listAttack.elementAt(i2);
                        if (liveActor != null) {
                            int i3 = liveActor.damAttack;
                            liveActor.doublejump();
                            GCanvas.gameScr.startFlyText("-" + i3, 0, liveActor.x, (liveActor.y + liveActor.dy) - 15, 1, -2);
                            for (int i4 = 0; i4 < 15; i4++) {
                                EffectManager.addHiEffect((liveActor.x - 12) + GameScr.abs(r.nextInt() % 25), (liveActor.y - 30) + GameScr.abs(r.nextInt() % 35), 33);
                            }
                        }
                    }
                }
                this.state = (byte) 2;
            }
            this.frame = this.ATT_FRAME[this.dir][this.p1];
        } else if (b != 4) {
            if (b == 5) {
                this.frame = (byte) 3;
                createExp();
            }
        } else if (this.dir != 1) {
            this.frame = (byte) 3;
        } else {
            this.frame = (byte) 7;
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
