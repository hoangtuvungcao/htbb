package game.model.skill;

import game.model.Char;
import game.model.Effect;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import javax.microedition.lcdui.Graphics;

public class AEOSkillLua extends Effect {
    int idSkill;
    public int degree;
    short d;
    private static byte[][] frame = {new byte[]{0, 0, 1, 1, 2, 2}, new byte[]{1, 1, 2, 2, 2}};
    private static byte[] typeEff = {15, 15};
    long time;
    int delay;
    Char attacker;
    int f;
    int startX;
    int startY;

    public AEOSkillLua(int x, int y, int type) {
        super(x, y, type);
        this.idSkill = 0;
        this.degree = 0;
        this.d = (short) 20;
        this.f = 0;
    }

    public AEOSkillLua(int degree) {
        super(degree);
        this.idSkill = 0;
        this.degree = 0;
        this.d = (short) 20;
        this.f = 0;
        this.degree = degree;
    }

    public AEOSkillLua(int x, int y, byte idSkill) {
        super(x);
        this.idSkill = 0;
        this.degree = 0;
        this.d = (short) 20;
        this.f = 0;
        this.x = x;
        this.y = y;
        this.idSkill = idSkill;
    }

    @Override
    public void paint(Graphics g) {
        switch (this.idSkill) {
            case 0:
                Res.paintImgEff(g, typeEff[this.idSkill], 0, frame[this.idSkill][this.f] * Effect.HEIGHT[typeEff[this.idSkill]], Effect.WIDTH[typeEff[this.idSkill]], Effect.HEIGHT[typeEff[this.idSkill]], this.x + this.startX, this.y + this.startY, 33);
                Res.paintImgEff(g, typeEff[this.idSkill], 0, frame[this.idSkill][this.f] * Effect.HEIGHT[typeEff[this.idSkill]], Effect.WIDTH[typeEff[this.idSkill]], Effect.HEIGHT[typeEff[this.idSkill]], (this.x - 3) + this.startX, this.y + this.startY, 33);
                break;
            case 1:
                Res.paintImgEff(g, typeEff[this.idSkill], 0, frame[this.idSkill][this.f] * Effect.HEIGHT[typeEff[this.idSkill]], Effect.WIDTH[typeEff[this.idSkill]], Effect.HEIGHT[typeEff[this.idSkill]], this.x, this.y, 33);
                break;
        }
    }

    public void setPos(int x, int y) {
        this.startX = x;
        this.startY = y;
    }

    public void setCharAttack(Char c) {
        this.attacker = c;
    }

    @Override
    public void update() {
        switch (this.idSkill) {
            case 0:
                if (Canvas.gameTick % 2 == 0) {
                    this.f = (this.f + 1) % frame[this.idSkill].length;
                }
                if (this.degree > 360) {
                    this.degree -= 360;
                }
                if (this.d < 80) {
                    this.d = (short) (this.d + 10);
                } else {
                    this.d = (short) 20;
                }
                if (this.degree == 0) {
                    this.x = this.d;
                } else if (this.degree == 180) {
                    this.x = (short) (-this.d);
                } else {
                    this.x = (short) ((Util.cos(this.degree) * this.d) / 1024);
                }
                if (this.degree == 90) {
                    this.y = this.d;
                } else if (this.degree != 270) {
                    this.y = (short) ((Util.sin(this.degree) * this.d) / 1024);
                } else {
                    this.y = (short) (-this.d);
                }
                break;
            case 1:
                if (Canvas.gameTick % 2 == 0) {
                    this.f = (this.f + 1) % frame[this.idSkill].length;
                }
                if (this.delay <= 0) {
                    this.wantDetroy = true;
                } else if (System.currentTimeMillis() - this.time >= 1000) {
                    this.time = System.currentTimeMillis();
                    this.delay--;
                }
                break;
        }
    }

    public void setTime(int delay) {
        this.time = System.currentTimeMillis();
        this.delay = delay;
    }
}
