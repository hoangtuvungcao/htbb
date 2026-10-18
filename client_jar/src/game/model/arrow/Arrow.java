package game.model.arrow;

import game.model.AttackResult;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.Skill_Eff_end;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Arrow extends IArrow {
    public Effect end;
    public int power;

    public byte effect;
    public byte typeEffEnd = -1;
    private LiveActor target;
    private int[] xw;
    private int[] yw;
    int frame;
    int transform;
    int pos;
    private int imgIndex;
    private int d;
    byte frameRun;
    public static final int[] ARROWINDEX = {0, 15, 37, 52, 75, 105, 127, 142, 165, 195, 217, 232, 255, 285, 307, 322, 345, 370};
    public static final byte[][] ARROWSIZE = {new byte[]{16, 24, 36, 24, 24, 36, 17, 28, 28, 73, 32, 14, 21, 56, 56, 42, 46, 24, 29, 32}, new byte[]{16, 24, 36, 24, 24, 36, 17, 28, 28, 73, 32, 13, 16, 54, 54, 41, 50, 24, 28, 32}};
    public static final int[] TRANSFORM = {0, 0, 0, 7, 6, 6, 6, 2, 2, 3, 3, 4, 5, 5, 5, 1};
    public static final byte[] FRAME = {0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 1, 0};

    public Arrow(int imgIndex) {
        this.imgIndex = imgIndex;
    }

    @Override
    public void setAngle(int angle) {
    }

    @Override
    public void onArrowTouchTarget() {
        if (this.power != 0 && this.power != 2000000) {
            Canvas.gameScr.startFlyText(new StringBuffer("-").append(this.power).toString(), 0, this.target.x + 0, (this.target.y + this.target.dy) - 15, 1, -2);
        }
        if (this.effect != 0 && this.effect < AttackResult.EFF_NAME.length) {
            Canvas.gameScr.startFlyText(AttackResult.EFF_NAME[this.effect], 0, this.target.x + 0, (this.target.y + this.target.dy) - 15, 2, -2);
        }
        this.target.realHPSyncTime = 2;
        if (this.target.catagory == 1) {
            if (this.effect == 0) {
                ((Monster) this.target).jump();
                EffectManager.addHiEffect(this.target.x, (this.target.y + this.target.dy) - 10, 11);
            } else if (this.effect == 2) {
                ((Monster) this.target).doublejump();
                EffectManager.addHiEffect(this.target.x, (this.target.y + this.target.dy) - 10, 12);
            }
        }
        if (this.effect != 1) {
            if (this.imgIndex == 1) {
                EffectManager.addHiEffect(this.target.x, (this.target.y + this.target.dy) - 10, 9);
            }
            if (this.imgIndex == 2) {
                EffectManager.addHiEffect(this.target.x - 10, this.target.y + this.target.dy, 16);
                EffectManager.addHiEffect(this.target.x + 10, this.target.y + this.target.dy, 16);
                EffectManager.addHiEffect(this.target.x, (this.target.y + this.target.dy) - 10, 16);
                EffectManager.addHiEffect(this.target.x, (this.target.y + this.target.dy) - 10, 16);
            }
            if (this.imgIndex == 5) {
                EffectManager.addHiEffect(this.target.x, (this.target.y + this.target.dy) - 10, 30);
            }
            if (this.imgIndex == 7) {
                Skill_Eff_end skill = new Skill_Eff_end(this.target.x, this.target.y + this.target.dy, this.transform);
                EffectManager.hiEffects.addElement(skill);
                EffectManager.lowEffects.addElement(skill);
            }
            if (this.imgIndex == 8) {
                EffectManager.addHiEffect(this.target.x, (this.target.y + this.target.dy) - 10, 11);
            }
            if (this.imgIndex == 13) {
                EffectManager.addHiEffect(this.target.x, (this.target.y + this.target.dy) - 10, 50);
            }
            if (this.imgIndex == 18) {
                EffectManager.addHiEffect(this.target.x, (this.target.y + this.target.dy) - 10, 50);
            }
            if (this.typeEffEnd != -1) {
                Effect ef = new Effect(this.target.x, this.target.y - (this.target.height / 2), this.typeEffEnd);
                ef.loop = this.typeEffEnd == 30 ? (byte) 0 : (byte) 1;
                EffectManager.hiEffects.addElement(ef);
            }
        }
        this.wantDestroy = true;
    }

    @Override
    public void paint(Graphics g) {
        Image img = Res.getImgArrow(this.imgIndex);
        if (img != null) {
            if (this.imgIndex == 18 || this.imgIndex == 16) {
                g.drawRegion(img, 0, this.frameRun * ARROWSIZE[1][this.imgIndex], ARROWSIZE[0][this.imgIndex], ARROWSIZE[1][this.imgIndex], 0, this.xw[this.pos], this.yw[this.pos], 3);
            } else {
                g.drawRegion(img, 0, this.frame * ARROWSIZE[1][this.imgIndex], ARROWSIZE[0][this.imgIndex], ARROWSIZE[1][this.imgIndex], this.transform, this.xw[this.pos], this.yw[this.pos], 3);
            }
        }
    }

    @Override
    public void set(int type, int x, int y, int power, byte effect2, LiveActor owner, LiveActor target) {
        this.effect = effect2;
        this.target = target;
        this.power = power;
        int dx = target.x - x;
        int dy = (target.y + target.dy) - y;
        if (type == 7) {
            dx = (target.x + 10) - x;
            dy = (target.y + target.dy) - y;
        } else if (type == 8) {
            dx = (target.x - 10) - x;
            dy = ((target.y - 10) + target.dy) - y;
        }
        int nPosition = (GameScr.abs(dx) + GameScr.abs(dy)) / 20;
        if (nPosition < 2) {
            nPosition = 2;
        }
        this.xw = new int[nPosition];
        this.yw = new int[nPosition];
        for (int i = 1; i < nPosition; i++) {
            this.xw[i] = x + ((i * dx) / nPosition);
            this.yw[i] = y + ((i * dy) / nPosition);
        }
        this.d = findDirIndexFromAngle(Util.angle(dx, -dy));
        this.frame = FRAME[this.d];
        this.transform = TRANSFORM[this.d];
    }

    public static int findDirIndexFromAngle(int angle) {
        for (int i = 0; i < ARROWINDEX.length - 1; i++) {
            if (angle >= ARROWINDEX[i] && angle <= ARROWINDEX[i + 1]) {
                if (i >= 16) {
                    return 0;
                }
                return i;
            }
        }
        return 0;
    }

    @Override
    public void update() {
        if (this.imgIndex == 18) {
            this.frameRun = (byte) ((this.frameRun + 1) % 4);
        }
        this.pos++;
        if (this.pos >= this.xw.length) {
            this.pos = this.xw.length;
        }
        if (this.pos == this.xw.length) {
            onArrowTouchTarget();
        }
    }
}
