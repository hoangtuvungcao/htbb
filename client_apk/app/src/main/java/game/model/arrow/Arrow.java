package game.model.arrow;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.AttackResult;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.Skill_Eff_end;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;

public class Arrow extends IArrow {

    public byte effect;
    public Effect end;
    int frame;
    byte frameRun;
    private int imgIndex;
    int pos;
    public int power;
    private LiveActor target;
    int transform;
    public byte typeEffEnd = -1;
    private int[] xw;
    private int[] yw;
    public static final int[] ARROWINDEX = {0, 15, 37, 52, 75, 105, 127, 142, 165, 195, 217, 232, 255, 285, 307, 322, 345, 370};
    public static final byte[][] ARROWSIZE = {new byte[]{16, 24, 36, 24, 24, 36, 17, 28, 28, 73, 32, 14, 21, 56, 56, 42, 46, 24, 29, 32}, new byte[]{16, 24, 36, 24, 24, 36, 17, 28, 28, 73, 32, 13, 16, 54, 54, 41, 50, 24, 28, 32}};
    public static final int[] TRANSFORM = {0, 0, 0, 7, 6, 6, 6, 2, 2, 3, 3, 4, 5, 5, 5, 1};
    public static final byte[] FRAME = {0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 1, 0};

    @Override
    public void setAngle(int i) {
    }

    public Arrow(int i) {
        this.imgIndex = i;
    }

    @Override
    public void onArrowTouchTarget() {
        int i = this.power;
        if (i != 0 && i != 2000000) {
            GCanvas.gameScr.startFlyText("-" + this.power, 0, this.target.x, (this.target.y + this.target.dy) - 15, 1, -2);
        }
        byte b = this.effect;
        if (b != 0 && b < AttackResult.EFF_NAME.length) {
            GCanvas.gameScr.startFlyText(AttackResult.EFF_NAME[this.effect], 0, this.target.x, (this.target.y + this.target.dy) - 15, 2, -2);
        }
        this.target.realHPSyncTime = 2;
        if (this.target.catagory == 1) {
            byte b2 = this.effect;
            if (b2 == 0) {
                ((Monster) this.target).jump();
                EffectManager.addHiEffect(this.target.x, (this.target.y + this.target.dy) - 10, 11);
            } else if (b2 == 2) {
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
                Skill_Eff_end skill_Eff_end = new Skill_Eff_end(this.target.x, this.target.y + this.target.dy, this.transform);
                EffectManager.hiEffects.addElement(skill_Eff_end);
                EffectManager.lowEffects.addElement(skill_Eff_end);
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
                Effect effect2 = new Effect(this.target.x, this.target.y - (this.target.height / 2), this.typeEffEnd);
                effect2.loop = this.typeEffEnd == 30 ? (byte) 0 : (byte) 1;
                EffectManager.hiEffects.addElement(effect2);
            }
        }
        this.wantDestroy = true;
    }

    @Override
    public void paint(Graphics graphics) {
        if (Res.getImgArrow(this.imgIndex) != null) {
            if (this.imgIndex == 18) {
                Bitmap[] bitmapArr = Res.imgArrow;
                int i = this.imgIndex;
                Bitmap bitmap = bitmapArr[i];
                byte b = this.frameRun;
                byte[][] bArr = ARROWSIZE;
                byte b2 = bArr[1][i];
                int i2 = b * b2;
                byte b3 = bArr[0][i];
                int[] iArr = this.xw;
                int i3 = this.pos;
                graphics.drawRegion(bitmap, 0, i2, b3, b2, 0, iArr[i3], this.yw[i3], 3);
                return;
            }
            Bitmap[] bitmapArr2 = Res.imgArrow;
            int i4 = this.imgIndex;
            Bitmap bitmap2 = bitmapArr2[i4];
            int i5 = this.frame;
            byte[][] bArr2 = ARROWSIZE;
            byte b4 = bArr2[1][i4];
            int i6 = i5 * b4;
            byte b5 = bArr2[0][i4];
            int i7 = this.transform;
            int[] iArr2 = this.xw;
            int i8 = this.pos;
            graphics.drawRegion(bitmap2, 0, i6, b5, b4, i7, iArr2[i8], this.yw[i8], 3);
        }
    }

    @Override
    public void set(int type, int x, int y, int power, byte effect, LiveActor owner, LiveActor target) {
        this.effect = effect;
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
            this.xw[i] = ((i * dx) / nPosition) + x;
            this.yw[i] = ((i * dy) / nPosition) + y;
        }
        int d = findDirIndexFromAngle(Util.angle(dx, -dy));
        this.frame = FRAME[d];
        this.transform = TRANSFORM[d];
    }

    public static int findDirIndexFromAngle(int i) {
        int i2 = 0;
        while (true) {
            int[] iArr = ARROWINDEX;
            if (i2 >= iArr.length - 1) {
                return 0;
            }
            if (i >= iArr[i2] && i <= iArr[i2 + 1]) {
                if (i2 >= 16) {
                    return 0;
                }
                return i2;
            }
            i2++;
        }
    }

    @Override
    public void update() {
        if (this.imgIndex == 18) {
            this.frameRun = (byte) ((this.frameRun + 1) % 4);
        }
        int i = this.pos + 1;
        this.pos = i;
        int[] iArr = this.xw;
        if (i >= iArr.length) {
            this.pos = iArr.length;
        }
        if (this.pos == iArr.length) {
            onArrowTouchTarget();
        }
    }
}
