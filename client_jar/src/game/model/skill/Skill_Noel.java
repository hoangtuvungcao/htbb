package game.model.skill;

import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.Canvas;
import game.render.Util;

public class Skill_Noel extends Effect {
    private int[] xw;
    private int[] yw;
    public int dx;
    public int dy;
    public int angle;
    public int pos;
    public int frame = 0;
    public int height = 0;
    public int xto;
    public int yto;
    LiveActor target;

    public Skill_Noel(int x, int y, int xto, int yto, LiveActor target) {
        this.target = null;
        this.target = target;
        this.x = x;
        this.y = y;
        this.xto = xto;
        this.yto = yto;
        setspeed();
    }

    public void setspeed() {
        this.dx = this.xto - this.x;
        this.dy = (this.yto - 0) - this.y;
        this.angle = Util.angle(this.dx, this.dy);
        int nPosition = (Math.abs(this.dx) + Math.abs(this.dy)) / 30;
        if (nPosition < 2) {
            nPosition = 2;
        }
        this.xw = new int[nPosition];
        this.yw = new int[nPosition];
        for (int i = 0; i < nPosition; i++) {
            this.xw[i] = this.x + ((i * this.dx) / nPosition);
            this.yw[i] = this.y + ((i * this.dy) / nPosition);
        }
    }

    public void createEfect(int x, int y, int type) {
        Effect u = new Effect(x, y, type);
        EffectManager.addLowEffect(u);
    }

    @Override
    public void update() {
        if (this.pos >= 1) {
            createEfect(this.xw[this.pos] + 3, this.yw[this.pos] + 15, 52);
            createEfect(this.xw[this.pos], this.yw[this.pos], 51);
        }
        if (this.pos < this.xw.length) {
            this.pos++;
        }
        if (this.pos >= this.xw.length) {
            this.pos = this.xw.length - 1;
            this.xw[this.pos] = this.xto;
            this.yw[this.pos] = this.yto;
            createEfect(this.xw[this.pos] + 3, this.yw[this.pos] + 15, 52);
            createEfect(this.xw[this.pos], this.yw[this.pos], 51);
            this.wantDetroy = true;
            if (this.target != null && this.target.damAttack != 0 && this.target.damAttack != 2000000) {
                Canvas.gameScr.startFlyText(new StringBuffer("-").append(this.target.damAttack).toString(), 0, this.target.x + 0, (this.target.y + this.target.dy) - 15, 1, -2);
            }
        }
    }
}
