package game.model.skill;

import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.MagicLogic;
import game.model.Monster;
import game.model.Position;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Skill_AEO_DUA_DAO_5 extends Effect {
    private MagicLogic magic;
    private int xDau_1;
    private int yDau_1;
    private int xDau_2;
    private int yDau_2;
    private int xlast;
    private int yLast;
    private int xDau_3;
    private int yDau_3;
    private int xDau_4;
    private int yDau_4;
    public String[] textAttack;
    private LiveActor to;
    boolean isDoubleDragon;
    private int countDau = 7;
    private int dir = -1;
    private Vector listPos = new Vector();

    public Skill_AEO_DUA_DAO_5(int xFrom, int yFrom, LiveActor from, LiveActor to, byte type) {
        this.type = type;
        this.to = to;
        this.magic = new MagicLogic();
        this.xlast = xFrom;
        this.yLast = yFrom;
        if (type != 5) {
            this.magic.set(type, xFrom, yFrom, from.dir, to);
            int angle1 = Util.angle(xFrom - this.x, -(yFrom - this.y));
            this.magic.setAngle(Util.fixangle(angle1 - 180));
        } else {
            this.xlast = to.x;
            this.yLast = to.y;
            to.x = (short) this.xlast;
            to.y = (short) (this.yLast - 200);
            this.to = to;
            this.magic.set(type, this.xlast, this.yLast, to.dir, to);
            int angle2 = Util.angle(this.xlast - this.x, -(this.yLast - this.y));
            this.magic.setAngle(Util.fixangle(angle2 - 45));
        }
        this.textAttack = new String[2];
    }

    @Override
    public void update() {
        if (this.magic != null) {
            this.magic.updateAngle();
            if (this.magic.isEnd) {
                if (this.type != 5) {
                    EffectManager.addLowEffect(this.magic.x, this.magic.y, this.type == 4 ? 30 : 28);
                }
                EffectManager.hiEffects.removeElement(this);
                if (this.to.catagory == 1) {
                    ((Monster) this.to).isTarget = false;
                }
                this.to.jump();
                if (this.textAttack[0] != null) {
                    Canvas.gameScr.startFlyText(this.textAttack[0], 0, this.x, this.y - 15, 1, -2);
                }
                if (this.textAttack[1] != null) {
                    Canvas.gameScr.startFlyText(this.textAttack[1], 0, this.x, this.y - 15, 2, -2);
                    return;
                }
                return;
            }
            this.x = this.magic.x;
            this.y = this.magic.y;
            int angM = Util.angle(this.xlast - this.x, -(this.yLast - this.y));
            int ang = Util.fixangle(angM + 90);
            int xa = (this.countDau * Util.cos(ang)) >> 10;
            int ya = (-(this.countDau * Util.sin(ang))) >> 10;
            this.xDau_1 = this.x + xa;
            this.yDau_1 = this.y + ya;
            int ang1 = Util.fixangle(angM - 90);
            int xa1 = (this.countDau * Util.cos(ang1)) >> 10;
            int ya1 = (-(this.countDau * Util.sin(ang1))) >> 10;
            this.xDau_2 = this.x + xa1;
            this.yDau_2 = this.y + ya1;
            if (this.type == 5 && this.isDoubleDragon) {
                int ang2 = Util.fixangle(angM + 135);
                int xa2 = (this.countDau * Util.cos(ang2)) >> 10;
                int ya2 = (-(this.countDau * Util.sin(ang2))) >> 10;
                this.xDau_3 = this.x + xa2 + 10;
                this.yDau_3 = (this.y + ya2) - 10;
                int ang3 = Util.fixangle(angM - 135);
                int xa3 = (this.countDau * Util.cos(ang3)) >> 10;
                int ya3 = (-(this.countDau * Util.sin(ang3))) >> 10;
                this.xDau_4 = this.x + xa3 + 10;
                this.yDau_4 = (this.y + ya3) - 10;
            }
            this.countDau += this.dir * 3;
            if (Util.abs(this.countDau) > 9) {
                this.dir *= -1;
            }
            Position p = new Position(this.xDau_1, this.yDau_1, 0);
            this.listPos.addElement(p);
            Position p1 = new Position(this.xDau_2, this.yDau_2, 0);
            this.listPos.addElement(p1);
            if (this.type == 5 && this.isDoubleDragon) {
                Position p2 = new Position(this.xDau_3, this.yDau_3, 0);
                this.listPos.addElement(p2);
                Position p3 = new Position(this.xDau_4, this.yDau_4, 0);
                this.listPos.addElement(p3);
            }
            this.xlast = this.x;
            this.yLast = this.y;
        }
    }

    @Override
    public void paint(Graphics g) {
        if (this.type == 4) {
            for (int i = 0; i < this.listPos.size(); i++) {
                Position p = (Position) this.listPos.elementAt(i);
                Res.paintImgEff(g, 29, 0, (17 - p.follow) * 11, 11, 11, p.x, p.y, 3);
                p.follow = (byte) (p.follow + 1);
                if (p.follow >= 18) {
                    this.listPos.removeElement(p);
                }
            }
        } else {
            for (int i2 = 0; i2 < this.listPos.size(); i2++) {
                Position p2 = (Position) this.listPos.elementAt(i2);
                Res.paintImgEff(g, 8, 0, p2.follow * 10, 10, 10, p2.x, p2.y, 3);
                p2.follow = (byte) (p2.follow + 1);
                if (p2.follow >= 10) {
                    this.listPos.removeElement(p2);
                }
            }
        }
        if (Res.getImgArrow(this.type) != null && !this.magic.isEnd) {
            if (this.type == 5) {
                Image img = Res.getImgArrow(3);
                if (img != null) {
                    g.drawRegion(img, 0, this.magic.headArrowFrame * 24, 24, 24, this.magic.headTransform, this.xDau_1, this.yDau_1, 3);
                    g.drawRegion(img, 0, this.magic.headArrowFrame * 24, 24, 24, this.magic.headTransform, this.xDau_2, this.yDau_2, 3);
                    if (this.isDoubleDragon) {
                        g.drawRegion(img, 0, this.magic.headArrowFrame * 24, 24, 24, this.magic.headTransform, this.xDau_3, this.yDau_3, 3);
                        g.drawRegion(img, 0, this.magic.headArrowFrame * 24, 24, 24, this.magic.headTransform, this.xDau_4, this.yDau_4, 3);
                        return;
                    }
                    return;
                }
                return;
            }
            Image img2 = Res.getImgArrow(this.type);
            if (img2 != null) {
                g.drawRegion(img2, 0, this.magic.headArrowFrame * 24, 24, 24, this.magic.headTransform, this.xDau_1, this.yDau_1, 3);
                g.drawRegion(img2, 0, this.magic.headArrowFrame * 24, 24, 24, this.magic.headTransform, this.xDau_2, this.yDau_2, 3);
            }
        }
    }
}
