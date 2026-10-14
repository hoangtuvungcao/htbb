package game.model.skill;

import game.core.j2me.Graphics;
import game.model.Cout;
import game.model.EffBack;
import game.model.Effect;
import game.model.EffectManager;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;

public class ThienThach_Skill extends Effect {
    private int dame;
    private byte index;
    private int v;
    private int xTo;
    private int yTo;

    public ThienThach_Skill(int i, int i2, int i3) {
        this.xTo = i;
        this.yTo = i2;
        this.x = i - 100;
        this.y = i2 - 200;
        this.v = 1;
        this.dame = i3;
    }

    @Override
    public void update() {
        try {
            this.v++;
            EffectManager.addHiEffect(this.x, this.y, 32);
            int iAngle = Util.angle(this.xTo - this.x, -(this.yTo - this.y));
            int iCos = (this.v * Util.cos(iAngle)) >> 10;
            int i = (-(this.v * Util.sin(iAngle))) >> 10;
            this.x += iCos;
            this.y += i;
            if (Util.distance(this.x, this.y, this.xTo, this.yTo) <= this.v) {
                if (this.dame != 2000000) {
                    GCanvas.gameScr.startFlyText("-" + this.dame, 0, this.x, this.y - 15, 1, -2);
                }
                EffectManager.hiEffects.removeElement(this);
                EffectManager.lowEffects.addElement(new EffBack(this.xTo, this.yTo, 28, 3, 32, 31, false, false));
            }
        } catch (Exception unused) {
        }
    }

    @Override
    public void paint(Graphics graphics) {
        try {
            Res.paintImgEff(graphics, 31, 0, (this.index / 3) * 20, 20, 20, this.x, this.y, 3);
            byte b = (byte) (this.index + 1);
            this.index = b;
            if (b >= 6) {
                this.index = (byte) 0;
            }
        } catch (Exception unused) {
            Cout.println("skill thien thach");
        }
    }
}
