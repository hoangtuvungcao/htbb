package game.model.skill;

import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.GCanvas;
import game.render.Util;

public class Skill_Noel extends Effect {
    public int angle;
    public int dx;
    public int dy;
    public int frame = 0;
    public int height = 0;
    public int pos;
    LiveActor target;
    public int xto;
    private int[] xw;
    public int yto;
    private int[] yw;

    public Skill_Noel(int i, int i2, int i3, int i4, LiveActor liveActor) {
        this.target = liveActor;
        this.x = i;
        this.y = i2;
        this.xto = i3;
        this.yto = i4;
        setspeed();
    }

    public void setspeed() {
        this.dx = this.xto - this.x;
        int i = this.yto - this.y;
        this.dy = i;
        this.angle = Util.angle(this.dx, i);
        int iAbs = (Math.abs(this.dx) + Math.abs(this.dy)) / 30;
        if (iAbs < 2) {
            iAbs = 2;
        }
        this.xw = new int[iAbs];
        this.yw = new int[iAbs];
        for (int i2 = 0; i2 < iAbs; i2++) {
            this.xw[i2] = this.x + ((this.dx * i2) / iAbs);
            this.yw[i2] = this.y + ((this.dy * i2) / iAbs);
        }
    }

    public void createEfect(int i, int i2, int i3) {
        EffectManager.addLowEffect(new Effect(i, i2, i3));
    }

    @Override
    public void update() {
        int i = this.pos;
        if (i >= 1) {
            createEfect(this.xw[i] + 3, this.yw[i] + 15, 52);
            int[] iArr = this.xw;
            int i2 = this.pos;
            createEfect(iArr[i2], this.yw[i2], 51);
        }
        int i3 = this.pos;
        int[] iArr2 = this.xw;
        if (i3 < iArr2.length) {
            this.pos = i3 + 1;
        }
        if (this.pos >= iArr2.length) {
            int length = iArr2.length - 1;
            this.pos = length;
            iArr2[length] = this.xto;
            int[] iArr3 = this.yw;
            int i4 = this.yto;
            iArr3[length] = i4;
            createEfect(iArr2[length] + 3, i4 + 15, 52);
            int[] iArr4 = this.xw;
            int i5 = this.pos;
            createEfect(iArr4[i5], this.yw[i5], 51);
            this.wantDetroy = true;
            LiveActor liveActor = this.target;
            if (liveActor == null || liveActor.damAttack == 0 || this.target.damAttack == 2000000) {
                return;
            }
            GCanvas.gameScr.startFlyText("-" + this.target.damAttack, 0, this.target.x, (this.target.y + this.target.dy) - 15, 1, -2);
        }
    }
}
