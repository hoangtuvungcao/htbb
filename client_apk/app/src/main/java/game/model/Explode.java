package game.model;

import game.core.j2me.Graphics;
import game.render.Res;

public class Explode extends Exp {
    private static byte[] eff = {15};
    private static final byte[][] frame = {new byte[]{0, 1, 2, 1, 0, 1, 2, 0}};
    int f;
    int p;
    byte type;

    public Explode(int i, int i2, int i3) {
        this.x = i;
        this.y = i2;
        this.type = (byte) i3;
    }

    @Override
    public void paint(Graphics graphics) {
        byte[] bArr = eff;
        byte b = this.type;
        Res.paintImgEff(graphics, bArr[b], 0, frame[b][this.f] * Effect.HEIGHT[eff[this.type]], Effect.WIDTH[eff[this.type]], Effect.HEIGHT[eff[this.type]], this.x, this.y, Graphics.BOTTOM | Graphics.HCENTER);
    }

    @Override
    public void update() {
        int i = this.p + 1;
        this.p = i;
        if (i % 2 == 0) {
            int i2 = this.f + 1;
            this.f = i2;
            if (i2 >= frame[this.type].length) {
                this.f = 0;
                this.destroy = true;
            }
        }
    }
}
