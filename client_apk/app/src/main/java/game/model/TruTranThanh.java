package game.model;

import android.graphics.Bitmap;
import effect.DynamicEffect;
import game.core.j2me.Graphics;
import game.model.skill.Skill_TruTranThanh;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Random;
import java.util.Vector;

public class TruTranThanh extends Monster {
    public static boolean isChopSang;
    int dem;
    int lap;
    int typeID;
    Vector exp = new Vector();
    byte[] ATT_FRAME = {0, 0, 1, 1, 1, 1, 1, 1};
    byte[] STAND_FRAME = {0, 0, 0, 1, 1, 1};
    byte totalExp = 0;
    Vector listTarget = new Vector();

    @Override
    public boolean allwayPaint() {
        return true;
    }

    @Override
    public void jump() {
    }

    public TruTranThanh(int i) {
        this.typeID = i;
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
                graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, this.frame * Res.monsterTemplates[this.monster_type].h, Res.monsterTemplates[this.monster_type].w, Res.monsterTemplates[this.monster_type].h, 0, this.x - (Res.monsterTemplates[this.monster_type].w / 2), this.y - Res.monsterTemplates[this.monster_type].h, 0);
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
            Bitmap bitmap = Res.monsterTemplates[this.monster_type].image;
            byte b = Res.monsterTemplates[this.monster_type].h;
            graphics.drawRegion(bitmap, 0, 0, Res.monsterTemplates[this.monster_type].w, 40, 0, i - Res.monsterTemplates[this.monster_type].xCenter, i2 - 20, 0);
            graphics.drawRegion(Res.imgNguHanh, this.nh[this.he] * 16, 0, 16, 16, 0, i - 15, i2 + 3, Graphics.TOP | Graphics.LEFT);
        } catch (Exception unused) {
            Cout.println(" loi tai paintCorner monter");
        }
    }

    @Override
    public void update() {
        updateExp();
        updateDynamicBuff();
        byte b = this.state;
        if (b == 0) {
            int i = this.typeID;
            if (i == 0) {
                this.frame = (byte) 0;
                this.listTarget.removeAllElements();
            } else if (i == 1 && isChopSang) {
                int i2 = this.lap + 1;
                this.lap = i2;
                byte[] bArr = this.STAND_FRAME;
                if (i2 > bArr.length - 1) {
                    this.lap = 0;
                }
                this.frame = bArr[this.lap];
            } else {
                this.frame = (byte) 0;
            }
        } else if (b == 2) {
            this.frame = (byte) 0;
        } else if (b != 3) {
            if (b == 4) {
                this.frame = (byte) 0;
            } else if (b == 5) {
                this.frame = (byte) 0;
                createExp();
            }
        } else if (this.typeID == 0) {
            this.p1 = (short) (this.p1 + 1);
            if (this.p1 == this.ATT_FRAME.length - 5) {
                EffectManager.addHiEffect(this.x - 3, this.y - 30, 24);
                for (int i3 = 0; i3 < 10; i3++) {
                    EffectManager.hiEffects.addElement(new Skill_TruTranThanh(this.x - 5, this.y - 70, this, this.listTarget));
                }
            }
            if (this.p1 >= this.ATT_FRAME.length - 3 && this.p1 <= this.ATT_FRAME.length - 1) {
                EffectManager.addHiEffect(this.x - 5, this.y - 65, 3);
            }
            if (this.p1 == this.ATT_FRAME.length - 1 && this.listTarget.size() > 0) {
                for (int i4 = 0; i4 < this.listTarget.size(); i4++) {
                    LiveActor liveActor = (LiveActor) this.listTarget.elementAt(i4);
                    GCanvas.gameScr.startWeaponsLazer(this, liveActor, 0, -65, 0, liveActor.damAttack, 2);
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
        updateEffSkill();
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
    public void startAttack(Vector vector, byte b) {
        this.state = (byte) 3;
        this.listTarget = vector;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
    }

    @Override
    public void setInfo(MonsterInfo monsterInfo) {
        super.setInfo(monsterInfo);
    }
}
