package game.model;

import game.render.Res;
import javax.microedition.lcdui.Graphics;

public class EffBack extends Effect {
    short[] xE;
    short[] yE;
    byte[] index;
    byte[] count;
    int type;
    int limit;
    int w0;
    int h0;
    private boolean isEffVong;
    private boolean isEffUpDown;
    int aa = 0;

    public EffBack(int x, int y, int type, int limit, int w, int h, boolean isEffVong, boolean isEffUpDown) {
        this.isEffVong = isEffVong;
        this.w0 = w;
        this.h0 = h;
        this.limit = limit;
        this.type = type;
        this.x = x;
        this.y = y;
        this.xE = new short[6];
        this.yE = new short[6];
        this.index = new byte[6];
        this.count = new byte[6];
        for (int i = 0; i < 6; i++) {
            this.xE[i] = (short) (((x - 10) + Res.rnd(20)) - 10);
            this.yE[i] = (short) (y + Res.rnd(20));
            this.index[i] = (byte) Res.rnd(6);
        }
        this.isEffUpDown = isEffUpDown;
    }

    @Override
    public void update() {
        for (int i = 0; i < this.xE.length; i++) {
            byte[] bArr = this.index;
            int i2 = i;
            bArr[i2] = (byte) (bArr[i2] + 1);
            if (this.index[i] >= this.limit * 3) {
                this.xE[i] = (short) ((this.x + Res.rnd(30)) - 15);
                this.yE[i] = (short) (this.y + Res.rnd(14) + 3);
                this.index[i] = (byte) Res.rnd(6);
                byte[] bArr2 = this.count;
                int i3 = i;
                bArr2[i3] = (byte) (bArr2[i3] + 1);
                if (this.count[i] > 3) {
                    if (!this.isEffUpDown) {
                        EffectManager.lowEffects.removeElement(this);
                    } else {
                        EffectManager.hiEffects.removeElement(this);
                    }
                }
            }
        }
        this.aa++;
        if (this.aa >= 6) {
            this.aa = 0;
        }
    }

    @Override
    public void paint(Graphics g) {
        for (int i = 0; i < 6; i++) {
            Res.paintImgEff(g, this.type, 0, (this.index[i] / 3) * this.h0, this.w0, this.h0, this.xE[i], this.yE[i], 33);
        }
        if (this.isEffVong) {
            Res.paintImgEff(g, 13, 0, (this.aa / 2) * 38, 58, 38, this.x, this.y - 3, 3);
        }
    }
}
