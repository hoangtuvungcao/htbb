package game.model.skill;

import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.arrow.Arrow;
import game.render.Canvas;
import game.render.Res;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Skill_TruRong extends Effect {
    private int[] xw;
    private int[] yw;
    public int dx;
    public int dy;
    public int pos;
    public int time;
    public int lim;
    public int height = 0;
    public int xto;
    public int yto;
    public int imgIndex;
    byte frame;
    byte frame1;
    int TimeStarAtack;
    boolean isShot;
    short id;
    LiveActor target;

    public Skill_TruRong(int x, int y, int xto, int yto, LiveActor target, int timeAtt) {
        this.target = null;
        this.target = target;
        this.x = x;
        this.y = y + 30;
        this.xto = xto;
        this.yto = yto;
        this.imgIndex = 15;
        this.lim = y - 60;
        this.TimeStarAtack = timeAtt;
        setspeed();
        this.id = (short) 55;
    }

    public void setspeed() {
        this.dx = this.xto - this.x;
        this.dy = (this.yto - 0) - this.lim;
        int nPosition = (Math.abs(this.dx) + Math.abs(this.dy)) / 20;
        if (nPosition < 2) {
            nPosition = 2;
        }
        this.xw = new int[nPosition];
        this.yw = new int[nPosition];
        for (int i = 0; i < nPosition; i++) {
            this.xw[i] = this.x + ((i * this.dx) / nPosition);
            this.yw[i] = this.lim + ((i * this.dy) / nPosition);
        }
    }

    @Override
    public void paint(Graphics g) {
        Image img;
        Image img2;
        if (this.TimeStarAtack < 0) {
            if (this.y > this.lim && (img2 = Res.getImageEffect(this.id)) != null) {
                g.drawRegion(img2, 0, (this.frame1 * img2.getHeight()) / 2, img2.getWidth(), img2.getHeight() / 2, 0, this.x, this.y, 3);
            }
            if (this.isShot && (img = Res.getImgArrow(this.imgIndex)) != null) {
                g.drawRegion(img, 0, this.frame * Arrow.ARROWSIZE[1][this.imgIndex], Arrow.ARROWSIZE[0][this.imgIndex], Arrow.ARROWSIZE[1][this.imgIndex], 0, this.xw[this.pos], this.yw[this.pos], 3);
            }
        }
    }

    @Override
    public void update() {
        this.frame = (byte) ((this.frame + 1) % 3);
        this.frame1 = (byte) ((this.frame1 + 1) % 2);
        if (this.TimeStarAtack >= 0) {
            this.TimeStarAtack--;
        }
        if (this.TimeStarAtack < 0) {
            if (this.y > this.lim) {
                this.y -= 10;
            }
            if (this.y <= this.lim && this.time <= 6) {
                this.time++;
            }
            if (this.time == 2) {
                EffectManager.addHiEffect(this.x, this.y, 54);
            }
            if (this.time >= 6 && !this.isShot) {
                this.isShot = true;
            }
            if (this.time > 6 && this.pos < this.xw.length) {
                this.pos++;
            }
            if (this.pos >= this.xw.length) {
                this.pos = this.xw.length - 1;
                this.xw[this.pos] = this.xto;
                this.yw[this.pos] = this.yto;
                EffectManager.addLowEffect(this.xto, this.yto, 53);
                this.wantDetroy = true;
                if (this.target != null && this.target.damAttack != 0 && this.target.damAttack != 2000000) {
                    Canvas.gameScr.startFlyText(new StringBuffer("-").append(this.target.damAttack).toString(), 0, this.target.x + 0, (this.target.y + this.target.dy) - 15, 1, -2);
                }
            }
        }
    }
}
