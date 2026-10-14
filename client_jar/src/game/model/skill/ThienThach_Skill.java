package game.model.skill;

import game.model.EffBack;
import game.model.Effect;
import game.model.EffectManager;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import javax.microedition.lcdui.Graphics;

public class ThienThach_Skill extends Effect {
    private int xTo;
    private int yTo;
    private int v;
    private int dame;
    private byte index;

    public ThienThach_Skill(int x, int y, int dam) {
        this.xTo = x;
        this.yTo = y;
        this.x = x - 100;
        this.y = y - 200;
        this.v = 1;
        this.dame = dam;
    }

    @Override
    public void update() {
        this.v++;
        EffectManager.addHiEffect(this.x, this.y, 32);
        int angle1 = Util.angle(this.xTo - this.x, -(this.yTo - this.y));
        int xa = (this.v * Util.cos(angle1)) >> 10;
        int ya = (-(this.v * Util.sin(angle1))) >> 10;
        this.x += xa;
        this.y += ya;
        if (Util.distance(this.x, this.y, this.xTo, this.yTo) <= this.v) {
            if (this.dame != 2000000) {
                Canvas.gameScr.startFlyText(new StringBuffer("-").append(this.dame).toString(), 0, this.x, this.y - 15, 1, -2);
            }
            EffectManager.hiEffects.removeElement(this);
            EffBack eff = new EffBack(this.xTo, this.yTo, 28, 3, 32, 31, false, false);
            EffectManager.lowEffects.addElement(eff);
        }
    }

    @Override
    public void paint(Graphics g) {
        Res.paintImgEff(g, 31, 0, (this.index / 3) * 20, 20, 20, this.x, this.y, 3);
        this.index = (byte) (this.index + 1);
        if (this.index >= 6) {
            this.index = (byte) 0;
        }
    }
}
