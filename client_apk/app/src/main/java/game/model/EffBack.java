package game.model;

import game.core.j2me.Graphics;
import game.render.Res;

public class EffBack extends Effect {
    int aa = 0;
    byte[] count;
    int h0;
    byte[] index;
    private boolean isEffUpDown;
    private boolean isEffVong;
    int limit;
    int type;
    int w0;
    short[] xE;
    short[] yE;

    public EffBack(int i, int i2, int i3, int i4, int i5, int i6, boolean z, boolean z2) {
        this.isEffVong = z;
        this.w0 = i5;
        this.h0 = i6;
        this.limit = i4;
        this.type = i3;
        this.x = i;
        this.y = i2;
        this.xE = new short[6];
        this.yE = new short[6];
        this.index = new byte[6];
        this.count = new byte[6];
        for (int i7 = 0; i7 < 6; i7++) {
            this.xE[i7] = (short) (((i - 10) + Res.rnd(20)) - 10);
            this.yE[i7] = (short) (Res.rnd(20) + i2);
            this.index[i7] = (byte) Res.rnd(6);
        }
        this.isEffUpDown = z2;
    }

    @Override
    public void update() {
        int i = this.aa + 1;
        this.aa = i;
        int i2 = 0;
        if (i >= 6) {
            this.aa = 0;
        }
        while (true) {
            short[] sArr = this.xE;
            if (i2 >= sArr.length) {
                return;
            }
            byte[] bArr = this.index;
            byte b = (byte) (bArr[i2] + 1);
            bArr[i2] = b;
            if (b >= this.limit * 3) {
                sArr[i2] = (short) ((this.x + Res.rnd(30)) - 15);
                this.yE[i2] = (short) (this.y + Res.rnd(14) + 3);
                this.index[i2] = (byte) Res.rnd(6);
                byte[] bArr2 = this.count;
                byte b2 = (byte) (bArr2[i2] + 1);
                bArr2[i2] = b2;
                if (b2 > 3) {
                    if (!this.isEffUpDown) {
                        EffectManager.lowEffects.removeElement(this);
                    } else {
                        EffectManager.hiEffects.removeElement(this);
                    }
                }
            }
            i2++;
        }
    }

    @Override
    public void paint(Graphics graphics) {
        for (int i = 0; i < 6; i++) {
            int i2 = this.type;
            int i3 = this.index[i] / 3;
            int i4 = this.h0;
            Res.paintImgEff(graphics, i2, 0, i3 * i4, this.w0, i4, this.xE[i], this.yE[i], Graphics.BOTTOM | Graphics.HCENTER);
        }
        if (this.isEffVong) {
            Res.paintImgEff(graphics, 13, 0, (this.aa / 2) * 38, 58, 38, this.x, this.y - 3, 3);
        }
    }
}
