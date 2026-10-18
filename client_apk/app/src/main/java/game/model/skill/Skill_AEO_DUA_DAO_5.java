package game.model.skill;

import game.core.j2me.Graphics;
import game.model.Cout;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.MagicLogic;
import game.model.Monster;
import game.model.Position;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;

public class Skill_AEO_DUA_DAO_5 extends Effect {
    boolean isDoubleDragon;
    private MagicLogic magic;
    public String[] textAttack;
    private LiveActor to;
    private int xDau_1;
    private int xDau_2;
    private int xDau_3;
    private int xDau_4;
    private int xlast;
    private int yDau_1;
    private int yDau_2;
    private int yDau_3;
    private int yDau_4;
    private int yLast;
    private int countDau = 7;
    private int dir = -1;
    private Vector listPos = new Vector();

    public Skill_AEO_DUA_DAO_5(int i, int i2, LiveActor liveActor, LiveActor liveActor2, byte b) {
        this.type = b;
        this.to = liveActor2;
        MagicLogic magicLogic = new MagicLogic();
        this.magic = magicLogic;
        this.xlast = i;
        this.yLast = i2;
        if (b == 5) {
            this.xlast = liveActor2.x;
            this.yLast = liveActor2.y;
            liveActor2.x = (short) this.xlast;
            liveActor2.y = (short) (this.yLast - 200);
            this.to = liveActor2;
            this.magic.set(b, this.xlast, this.yLast, liveActor2.dir, liveActor2);
            this.magic.setAngle(Util.fixangle(Util.angle(this.xlast - this.x, -(this.yLast - this.y)) - 45));
        } else {
            magicLogic.set(b, i, i2, liveActor.dir, liveActor2);
            this.magic.setAngle(Util.fixangle(Util.angle(i - this.x, -(i2 - this.y)) - 180));
        }
        this.textAttack = new String[2];
    }

    @Override
    public void update() {
        try {
            MagicLogic magicLogic = this.magic;
            if (magicLogic != null) {
                magicLogic.updateAngle();
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
                        GCanvas.gameScr.startFlyText(this.textAttack[0], 0, this.x, this.y - 15, 1, -2);
                    }
                    if (this.textAttack[1] != null) {
                        GCanvas.gameScr.startFlyText(this.textAttack[1], 0, this.x, this.y - 15, 2, -2);
                        return;
                    }
                    return;
                }
                this.x = this.magic.x;
                this.y = this.magic.y;
                int iAngle = Util.angle(this.xlast - this.x, -(this.yLast - this.y));
                int iFixangle = Util.fixangle(iAngle + 90);
                int iCos = (this.countDau * Util.cos(iFixangle)) >> 10;
                int i = (-(this.countDau * Util.sin(iFixangle))) >> 10;
                this.xDau_1 = this.x + iCos;
                this.yDau_1 = this.y + i;
                int iFixangle2 = Util.fixangle(iAngle - 90);
                int iCos2 = (this.countDau * Util.cos(iFixangle2)) >> 10;
                int i2 = (-(this.countDau * Util.sin(iFixangle2))) >> 10;
                this.xDau_2 = this.x + iCos2;
                this.yDau_2 = this.y + i2;
                if (this.type == 5 && this.isDoubleDragon) {
                    int iFixangle3 = Util.fixangle(iAngle + 135);
                    int iCos3 = (this.countDau * Util.cos(iFixangle3)) >> 10;
                    int i3 = (-(this.countDau * Util.sin(iFixangle3))) >> 10;
                    this.xDau_3 = this.x + iCos3 + 10;
                    this.yDau_3 = (this.y + i3) - 10;
                    int iFixangle4 = Util.fixangle(iAngle - 135);
                    int iCos4 = (this.countDau * Util.cos(iFixangle4)) >> 10;
                    int i4 = (-(this.countDau * Util.sin(iFixangle4))) >> 10;
                    this.xDau_4 = this.x + iCos4 + 10;
                    this.yDau_4 = (this.y + i4) - 10;
                }
                int i5 = this.countDau + (this.dir * 3);
                this.countDau = i5;
                if (Util.abs(i5) > 9) {
                    this.dir *= -1;
                }
                this.listPos.addElement(new Position(this.xDau_1, this.yDau_1, 0));
                this.listPos.addElement(new Position(this.xDau_2, this.yDau_2, 0));
                if (this.type == 5 && this.isDoubleDragon) {
                    this.listPos.addElement(new Position(this.xDau_3, this.yDau_3, 0));
                    this.listPos.addElement(new Position(this.xDau_4, this.yDau_4, 0));
                }
                this.xlast = this.x;
                this.yLast = this.y;
            }
        } catch (Exception unused) {
            Cout.println("update skill AEO dua dao 5");
        }
    }

    @Override
    public void paint(Graphics graphics) {
        try {
            int i = 0;
            if (this.type == 4) {
                int size = this.listPos.size();
                while (i < size) {
                    Position position = (Position) this.listPos.elementAt(i);
                    Res.paintImgEff(graphics, 29, 0, (17 - position.follow) * 11, 11, 11, position.x, position.y, 3);
                    position.follow = (byte) (position.follow + 1);
                    if (position.follow >= 18) {
                        this.listPos.removeElement(position);
                    }
                    i++;
                }
            } else {
                int size2 = this.listPos.size();
                while (i < size2) {
                    Position position2 = (Position) this.listPos.elementAt(i);
                    Res.paintImgEff(graphics, 8, 0, position2.follow * 10, 10, 10, position2.x, position2.y, 3);
                    position2.follow = (byte) (position2.follow + 1);
                    if (position2.follow >= 10) {
                        this.listPos.removeElement(position2);
                    }
                    i++;
                }
            }
            if (Res.getImgArrow(this.type) == null || this.magic.isEnd) {
                return;
            }
            if (this.type == 5) {
                graphics.drawRegion(Res.imgArrow[3], 0, this.magic.headArrowFrame * 24, 24, 24, this.magic.headTransform, this.xDau_1, this.yDau_1, 3);
                graphics.drawRegion(Res.imgArrow[3], 0, this.magic.headArrowFrame * 24, 24, 24, this.magic.headTransform, this.xDau_2, this.yDau_2, 3);
                if (this.isDoubleDragon) {
                    graphics.drawRegion(Res.imgArrow[3], 0, this.magic.headArrowFrame * 24, 24, 24, this.magic.headTransform, this.xDau_3, this.yDau_3, 3);
                    graphics.drawRegion(Res.imgArrow[3], 0, this.magic.headArrowFrame * 24, 24, 24, this.magic.headTransform, this.xDau_4, this.yDau_4, 3);
                    return;
                }
                return;
            }
            graphics.drawRegion(Res.imgArrow[this.type], 0, this.magic.headArrowFrame * 24, 24, 24, this.magic.headTransform, this.xDau_1, this.yDau_1, 3);
            graphics.drawRegion(Res.imgArrow[this.type], 0, this.magic.headArrowFrame * 24, 24, 24, this.magic.headTransform, this.xDau_2, this.yDau_2, 3);
        } catch (Exception unused) {
            Cout.println("paint aeo dua dao 5");
        }
    }
}
