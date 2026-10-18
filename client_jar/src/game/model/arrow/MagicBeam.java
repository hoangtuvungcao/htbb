package game.model.arrow;

import game.model.AttackResult;
import game.model.Char;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.MagicLogic;
import game.model.Monster;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.Eff_da_vang;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class MagicBeam extends IArrow {
    public int power;
    public int type;

    public byte effect;
    public int hpLost;
    public int frame;
    public int w;
    public int h;
    public int x;
    public int y;
    LiveActor owner;
    public static Image imgFire;
    public static final int[] HEADINDEX = {-1, -1, -1, 4, 3, 4, 6, -1, 7, 16, 19};
    byte fra;
    MagicLogic arrow = new MagicLogic();
    private byte[][] frameFly = {new byte[]{0, 0, 1, 1}, new byte[]{2, 2, 3, 3}, new byte[]{4, 4, 5, 5}};
    byte framelocxuay = 0;

    @Override
    public void setAngle(int angle) {
        this.arrow.setAngle(angle);
    }

    @Override
    public void set(int type, int x, int y, int power, byte effect2, LiveActor owner, LiveActor target) {
        this.arrow.set(type, x, y, owner.dir, target);
        this.type = type;
        this.owner = owner;
        this.effect = effect2;
        if (type == 20) {
        }
        this.hpLost = power;
        this.power = power;
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
                Eff_da_vang eff = new Eff_da_vang(this.x, this.y + 25);
                EffectManager.addLowEffect(eff);
                Eff_da_vang eff2 = new Eff_da_vang(this.x, this.y + 25);
                EffectManager.addLowEffect(eff2);
                this.framelocxuay = (byte) (this.framelocxuay + 1);
                if (this.framelocxuay > 3) {
                    this.framelocxuay = (byte) 0;
                }
                break;
            case 10:
                this.frame++;
                if (this.frame > this.frameFly[this.arrow.headArrowFrame].length - 1) {
                    this.frame = 0;
                }
                this.fra = this.frameFly[this.arrow.headArrowFrame][this.frame];
                break;
        }
    }

    @Override
    public void onArrowTouchTarget() {
        short x0 = this.arrow.target.x;
        short y0 = this.arrow.target.y;
        switch (this.type) {
            case 0:
                Canvas.gameScr.startExplosionAt(x0, y0);
                break;
            case 1:
                EffectManager.addHiEffect(x0, y0 - 10, 3);
                EffectManager.addLowEffect(x0, y0 - 25, 11);
                EffectManager.addHiEffect(x0, y0 - 15, 11);
                EffectManager.addHiEffect(x0 - 10, y0 - 20, 11);
                EffectManager.addHiEffect(x0 + 10, y0 - 20, 11);
                break;
            case 2:
                EffectManager.addHiEffect(x0, y0 - 10, 5);
                break;
            case 3:
                EffectManager.addHiEffect(x0, y0 - 10, 7);
                break;
            case 4:
                EffectManager.addLowEffect(x0, y0 - 25, 15);
                EffectManager.addHiEffect(x0, y0 - 15, 15);
                EffectManager.addHiEffect(x0 - 10, y0 - 20, 15);
                EffectManager.addHiEffect(x0 + 10, y0 - 20, 15);
                break;
            case 5:
            case 7:
                EffectManager.addHiEffect(x0, y0 - 10, 30);
                break;
            case 8:
                EffectManager.addHiEffect(x0, y0 - 10, 3);
                EffectManager.addLowEffect(x0, y0 - 25, 11);
                EffectManager.addHiEffect(x0, y0 - 15, 9);
                EffectManager.addHiEffect(x0 - 10, y0 - 20, 11);
                EffectManager.addHiEffect(x0 + 10, y0 - 20, 9);
                break;
            case 9:
                EffectManager.addHiEffect(x0, y0 - 15, 50);
                break;
        }
        if (this.effect != 0 && this.effect < AttackResult.EFF_NAME.length) {
            Canvas.gameScr.startFlyText(AttackResult.EFF_NAME[this.effect], 0, x0, y0 - 25, 1, -2);
        }
        if (this.power != 2000000) {
            if (this.power != 0) {
                if (this.type < 20) {
                    Canvas.gameScr.startFlyText(new StringBuffer("-").append(this.power).toString(), 0, x0, y0 - 15, 1, -2);
                } else {
                    Canvas.gameScr.startFlyText(new StringBuffer("-").append(this.hpLost).toString(), 0, x0, y0 - 15, 1, -2);
                }
            } else {
                Canvas.gameScr.startFlyText("MISS", 0, x0, y0 - 15, 1, -2);
            }
        }
        this.arrow.target.realHPSyncTime = 2;
        if (this.arrow.target.catagory == 1) {
            if (this.effect == 0) {
                ((Monster) this.arrow.target).jump();
                EffectManager.hiEffects.addElement(new Effect(x0, y0 - 10, 11));
            } else if (this.effect == 2) {
                ((Monster) this.arrow.target).doublejump();
                EffectManager.hiEffects.addElement(new Effect(x0, y0 - 10, 12));
            }
        } else if (this.arrow.target.catagory == 0) {
            if (this.effect == 0) {
                ((Char) this.arrow.target).jump();
                EffectManager.hiEffects.addElement(new Effect(x0, y0 - 10, 11));
            } else if (this.effect == 2) {
                ((Char) this.arrow.target).doublejump();
                EffectManager.hiEffects.addElement(new Effect(x0, y0 - 10, 12));
            }
        }
        this.wantDestroy = true;
    }

    @Override
    public void paint(Graphics g) {
        int imgIndex;
        Image img;
        if (this.type < 20) {
            if (HEADINDEX[this.type] != -1 && (img = Res.getImgArrow((imgIndex = HEADINDEX[this.type]))) != null) {
                if (this.type != 10 && this.type != 9) {
                    g.drawRegion(img, 0, this.arrow.headArrowFrame * Arrow.ARROWSIZE[1][imgIndex], Arrow.ARROWSIZE[0][imgIndex], Arrow.ARROWSIZE[1][imgIndex], this.arrow.headTransform, this.x, this.y, 3);
                    return;
                } else if (this.type == 10) {
                    g.drawRegion(img, 0, this.fra * Arrow.ARROWSIZE[1][imgIndex], Arrow.ARROWSIZE[0][imgIndex], Arrow.ARROWSIZE[1][imgIndex], this.arrow.headTransform, this.x, this.y, 3);
                    return;
                } else {
                    if (this.type == 9) {
                        g.drawRegion(img, 0, this.framelocxuay * Arrow.ARROWSIZE[1][imgIndex], Arrow.ARROWSIZE[0][imgIndex], Arrow.ARROWSIZE[1][imgIndex], 0, this.x, this.y, 3);
                        return;
                    }
                    return;
                }
            }
            return;
        }
        g.drawImage(imgFire, this.x, this.y, 3);
    }
}
