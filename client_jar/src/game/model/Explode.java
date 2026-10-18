package game.model;

import game.render.Res;
import javax.microedition.lcdui.Graphics;

public class Explode extends Exp {
    int f;
    int p;
    private static byte[] eff = {15};
    private static final byte[][] frame = {new byte[]{0, 1, 2, 1, 0, 1, 2, 0}};
    byte type;

    public Explode(int x, int y, int typeEff) {
        this.x = x;
        this.y = y;
        this.type = (byte) typeEff;
    }

    @Override
    public void paint(Graphics g) {
        Res.paintImgEff(g, eff[this.type], 0, frame[this.type][this.f] * Effect.HEIGHT[eff[this.type]], Effect.WIDTH[eff[this.type]], Effect.HEIGHT[eff[this.type]], this.x, this.y, 33);
    }

    @Override
    public void update() {
        this.p++;
        if (this.p % 2 == 0) {
            this.f++;
            if (this.f >= frame[this.type].length) {
                this.f = 0;
                this.destroy = true;
            }
        }
    }
}
