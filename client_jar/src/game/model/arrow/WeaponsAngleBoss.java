package game.model.arrow;

import game.model.Cout;
import game.model.EffectManager;
import game.model.LiveActor;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class WeaponsAngleBoss extends IArrow {
    public int type;
    public int x;
    public int y;
    public int angle;
    public int typesize;
    public int power;
    public int xbg;
    public int ybg;
    public int upPower;
    public int angleTo;
    public int[] FLIP = {0, 2, 1, 3, 7, 4};
    int index;

    public WeaponsAngleBoss(int angle, int power, LiveActor acFr, LiveActor acTo, int type, int addw, int addh, int xbg, int ybg, boolean lastActo) {
        this.angle = angle;
        this.power = power;
        if (acFr != null) {
            this.xbg = acFr.x - addw;
            this.ybg = acFr.y - addh;
        } else {
            this.xbg = xbg;
            this.ybg = ybg;
        }
        this.type = type;
        this.typesize = this.typesize;
        if (lastActo && acTo != null) {
            acTo.jump();
            EffectManager.addHiEffect(acTo.x, (acTo.y + acTo.dy) - 10, 11);
            Canvas.gameScr.startFlyText(new StringBuffer("-").append(acTo.damAttack).toString(), 0, acTo.x + 0, (acTo.y + acTo.dy) - 15, 1, -2);
        }
        this.angleTo = angle;
    }

    public int getFlip(int angle) {
        switch (angle) {
            case 0:
            case 300:
            case 330:
                return 0;
            case 30:
            case 60:
                return 2;
            case 90:
                return 5;
            case 120:
            case 150:
                return 3;
            case 180:
            case 210:
            case 240:
                return 1;
            case 270:
                return 4;
            default:
                return 0;
        }
    }

    @Override
    public void onArrowTouchTarget() {
    }

    public int getIdFrame(int angle) {
        switch (angle) {
            case 30:
            case 150:
            case 210:
            case 330:
                return 1;
            case 60:
            case 120:
            case 240:
            case 300:
                return 2;
            default:
                return 0;
        }
    }

    @Override
    public void paint(Graphics g) {
        Image img;
        try {
            if (this.type != 0 && (img = Res.getImageEffect(34)) != null) {
                g.drawRegion(img, 0, this.index * 15, 24, 15, 0, this.x, this.y, 3);
            }
        } catch (Exception e) {
            Cout.println("loi vu khi xoia");
        }
    }

    @Override
    public void set(int type, int x, int y, int power, byte effect2, LiveActor owner, LiveActor target) {
    }

    @Override
    public void setAngle(int angle) {
    }

    @Override
    public void update() {
        this.x = this.xbg + ((Util.cos(this.angle) * this.power) / 1024);
        this.y = this.ybg + ((Util.sin(this.angle) * this.power) / 1024);
        if (this.type == 0) {
            this.angleTo += 30;
            if (this.angleTo > 360) {
                this.angleTo -= 360;
            }
            if (this.angle != this.angleTo) {
                this.angle += 10;
                if (this.angle > 360) {
                    this.angle -= 360;
                }
            }
            if (this.upPower < 15) {
                this.upPower += 2;
            }
            EffectManager.addHiEffect(this.x, this.y, 33);
        } else {
            if (this.upPower < 24) {
                this.upPower += 8;
            }
            if (Canvas.gameTick % 4 == 0) {
                if (this.index < 1) {
                    this.index++;
                } else {
                    this.index = 0;
                }
            }
        }
        this.power += this.upPower;
        if (this.x < GameScr.cmx - (Canvas.w / 2) || this.x > GameScr.cmx + Canvas.w + (Canvas.w / 2) || this.y < GameScr.cmy - (Canvas.h / 2) || this.y > GameScr.cmy + Canvas.h + (Canvas.h / 2)) {
            if (Canvas.gameScr.arrowsDown.contains(this)) {
                Canvas.gameScr.arrowsDown.removeElement(this);
            }
            if (GameScr.arrowsUp.contains(this)) {
                GameScr.arrowsUp.removeElement(this);
            }
        }
    }
}
