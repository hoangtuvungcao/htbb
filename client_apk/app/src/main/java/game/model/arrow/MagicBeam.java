package game.model.arrow;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.AttackResult;
import game.model.Char;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.MagicLogic;
import game.model.Monster;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.Eff_da_vang;

public class MagicBeam extends IArrow {
    public static final int[] HEADINDEX = {-1, -1, -1, 4, 3, 4, 6, -1, 7, 16, 19};
    public static Bitmap imgFire;

    public byte effect;
    byte fra;
    public int frame;
    public int h;
    public int hpLost;
    LiveActor owner;
    public int power;
    public int type;
    public int w;
    public int x;
    public int y;
    byte framelocxuay = 0;
    MagicLogic arrow = new MagicLogic();
    private byte[][] frameFly = {new byte[]{0, 0, 1, 1}, new byte[]{2, 2, 3, 3}, new byte[]{4, 4, 5, 5}};

    @Override
    public void setAngle(int i) {
        this.arrow.setAngle(i);
    }

    @Override
    public void set(int i, int i2, int i3, int i4, byte b, LiveActor liveActor, LiveActor liveActor2) {
        this.arrow.set(i, i2, i3, liveActor.dir, liveActor2);
        this.type = i;
        this.owner = liveActor;
        this.effect = b;
        this.hpLost = i4;
        this.power = i4;
    }

    @Override
    public void update() {
        this.arrow.updateAngle();
        this.x = this.arrow.x;
        this.y = this.arrow.y;
        if (this.arrow.isEnd) {
            onArrowTouchTarget();
        }
        switch (this.type) {
            case 0:
                EffectManager.addHiEffect(this.x, this.y, 1);
                break;
            case 1:
                EffectManager.addHiEffect(this.x, this.y, 2);
                break;
            case 2:
                EffectManager.addHiEffect(this.x, this.y, 4);
                break;
            case 3:
                EffectManager.addHiEffect(this.x, this.y, 6);
                EffectManager.addHiEffect(this.x, this.y, 7);
                break;
            case 4:
                EffectManager.addHiEffect(this.x, this.y, 8);
                break;
            case 5:
            case 7:
                EffectManager.addHiEffect(this.x, this.y, 29);
                break;
            case 9:
                EffectManager.addLowEffect(this.x, this.y + 25, 59);
                EffectManager.addLowEffect(new Eff_da_vang(this.x, this.y + 25));
                EffectManager.addLowEffect(new Eff_da_vang(this.x, this.y + 25));
                byte b = (byte) (this.framelocxuay + 1);
                this.framelocxuay = b;
                if (b > 3) {
                    this.framelocxuay = (byte) 0;
                }
                break;
            case 10:
                int i = this.frame + 1;
                this.frame = i;
                if (i > this.frameFly[this.arrow.headArrowFrame].length - 1) {
                    this.frame = 0;
                }
                this.fra = this.frameFly[this.arrow.headArrowFrame][this.frame];
                break;
        }
    }

    @Override
    public void onArrowTouchTarget() {
        short s = this.arrow.target.x;
        short s2 = this.arrow.target.y;
        switch (this.type) {
            case 0:
                GCanvas.gameScr.startExplosionAt(s, s2);
                break;
            case 1:
                EffectManager.addHiEffect(s, s2 - 10, 3);
                EffectManager.addLowEffect(s, s2 - 25, 11);
                EffectManager.addHiEffect(s, s2 - 15, 11);
                int i = s2 - 20;
                EffectManager.addHiEffect(s - 10, i, 11);
                EffectManager.addHiEffect(s + 10, i, 11);
                break;
            case 2:
                EffectManager.addHiEffect(s, s2 - 10, 5);
                break;
            case 3:
                EffectManager.addHiEffect(s, s2 - 10, 7);
                break;
            case 4:
                EffectManager.addLowEffect(s, s2 - 25, 15);
                EffectManager.addHiEffect(s, s2 - 15, 15);
                int i2 = s2 - 20;
                EffectManager.addHiEffect(s - 10, i2, 15);
                EffectManager.addHiEffect(s + 10, i2, 15);
                break;
            case 5:
            case 7:
                EffectManager.addHiEffect(s, s2 - 10, 30);
                break;
            case 9:
                EffectManager.addHiEffect(s, s2 - 15, 50);
                break;
            case 10:
                EffectManager.addHiEffect(s, s2 - 15, 58);
                break;
        }
        byte b = this.effect;
        if (b != 0 && b < AttackResult.EFF_NAME.length) {
            GCanvas.gameScr.startFlyText(AttackResult.EFF_NAME[this.effect], 0, s, s2 - 25, 1, -2);
        }
        int i3 = this.power;
        if (i3 != 2000000) {
            if (i3 != 0) {
                if (this.type < 20) {
                    GCanvas.gameScr.startFlyText("-" + this.power, 0, s, s2 - 15, 1, -2);
                } else {
                    GCanvas.gameScr.startFlyText("-" + this.hpLost, 0, s, s2 - 15, 1, -2);
                }
            } else {
                GCanvas.gameScr.startFlyText("MISS", 0, s, s2 - 15, 1, -2);
            }
        }
        this.arrow.target.realHPSyncTime = 2;
        if (this.arrow.target.catagory == 1) {
            byte b2 = this.effect;
            if (b2 == 0) {
                ((Monster) this.arrow.target).jump();
                EffectManager.hiEffects.addElement(new Effect(s, s2 - 10, 11));
            } else if (b2 == 2) {
                ((Monster) this.arrow.target).doublejump();
                EffectManager.hiEffects.addElement(new Effect(s, s2 - 10, 12));
            }
        } else if (this.arrow.target.catagory == 0) {
            byte b3 = this.effect;
            if (b3 == 0) {
                ((Char) this.arrow.target).jump();
                EffectManager.hiEffects.addElement(new Effect(s, s2 - 10, 11));
            } else if (b3 == 2) {
                ((Char) this.arrow.target).doublejump();
                EffectManager.hiEffects.addElement(new Effect(s, s2 - 10, 12));
            }
        }
        this.wantDestroy = true;
    }

    @Override
    public void paint(Graphics graphics) {
        int i = this.type;
        if (i < 20) {
            int i2 = HEADINDEX[i];
            if (i2 == -1 || Res.getImgArrow(i2) == null) {
                return;
            }
            int i3 = this.type;
            if (i3 != 10 && i3 != 9) {
                graphics.drawRegion(Res.imgArrow[i2], 0, this.arrow.headArrowFrame * Arrow.ARROWSIZE[1][i2], Arrow.ARROWSIZE[0][i2], Arrow.ARROWSIZE[1][i2], this.arrow.headTransform, this.x, this.y, 3);
                return;
            } else if (i3 == 10) {
                graphics.drawRegion(Res.imgArrow[i2], 0, this.fra * Arrow.ARROWSIZE[1][i2], Arrow.ARROWSIZE[0][i2], Arrow.ARROWSIZE[1][i2], this.arrow.headTransform, this.x, this.y, 3);
                return;
            } else {
                if (i3 == 9) {
                    graphics.drawRegion(Res.imgArrow[i2], 0, this.framelocxuay * Arrow.ARROWSIZE[1][i2], Arrow.ARROWSIZE[0][i2], Arrow.ARROWSIZE[1][i2], 0, this.x, this.y, 3);
                    return;
                }
                return;
            }
        }
        graphics.drawImage(imgFire, this.x, this.y, 3);
    }
}
