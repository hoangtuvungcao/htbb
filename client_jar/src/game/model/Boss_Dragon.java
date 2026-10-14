package game.model;

import effect.DynamicEffect;
import game.model.arrow.Arrow;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Boss_Dragon extends Monster {
    public static ImageObj imgObj = new ImageObj();
    public static Image img;
    public static Image imgShadow;
    MagicLogic arrow;
    LiveActor target;
    LiveActor live;
    int index;
    int indexRear;
    byte[] tranIndex;
    byte[] IndexRear;
    byte[] tranRear;
    public short[] xBody = new short[12];
    public short[] yBody = new short[12];
    Position posGo = new Position();
    byte[] xDuHead = {5, 3, -1, -8, -8, 0, 0, 2};
    byte[] yDuHead = {-5, -8, -10, -7, -2, 0, 0, -3};
    int indexRan = 0;
    int count = 0;
    byte[] xDuRear = {-12, -6, 0, 10, 10, 7, 0, -8};
    byte[] yDuRear = {4, 8, 12, 8, -2, -8, -10, -4};
    byte[] arrIndex = {2, 3, 4, 3, 2, 1, 0, 1};

    static {
        try {
            imgShadow = Image.createImage("/rong/shadow.png");
            img = Image.createImage("/rong/Big0.png");
            InputStream ip = "".getClass().getResourceAsStream("/rong/dragonData");
            int i = (byte) ip.read();
            imgObj.index = new byte[i];
            imgObj.x0 = new byte[i];
            imgObj.y0 = new byte[i];
            imgObj.w = new byte[i];
            imgObj.h = new byte[i];
            for (int i2 = 0; i2 < i; i2++) {
                imgObj.index[i2] = (byte) ip.read();
                imgObj.x0[i2] = (byte) ip.read();
                imgObj.y0[i2] = (byte) ip.read();
                imgObj.w[i2] = (byte) ip.read();
                imgObj.h[i2] = (byte) ip.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boss_Dragon() {
        byte[] bArr = new byte[8];
        bArr[3] = 2;
        bArr[4] = 2;
        bArr[5] = 2;
        this.tranIndex = bArr;
        this.IndexRear = new byte[]{9, 6, 9, 6, 9, 6, 9, 6};
        this.tranRear = new byte[]{4, 3, 3, 1, 7, 0, 0, 2};
        this.target = Canvas.gameScr.mainChar;
        this.x = (short) (this.target.x - 80);
        this.y = (short) (this.target.y - 80);
        for (int i = 0; i < this.xBody.length; i++) {
            this.xBody[i] = this.x;
            this.yBody[i] = this.y;
        }
        this.live = new LiveActor() {
            @Override
            public void setPosTo(short x, short y) {
            }
        };
        this.arrow = new MagicLogic();
        this.arrow.isSpeedUp = true;
        this.arrow.setAngle(20);
        this.arrow.set(7, this.x, this.y, 7, this.target);
    }

    @Override
    public void update() {
        updateDynamicBuff();
        this.arrow.updateAngle();
        if (this.arrow.isEnd) {
            this.arrow.isEnd = false;
            this.arrow.life = 0;
            this.live.x = (short) ((this.target.x - 80) + Res.rnd(160));
            this.live.y = (short) ((this.target.y - 80) + Res.rnd(160));
            this.arrow.target = this.live;
        }
        this.x = (short) this.arrow.x;
        this.y = (short) this.arrow.y;
        this.index = this.arrow.index / 2;
        this.indexRear = Arrow.findDirIndexFromAngle(Util.angle(this.xBody[9] - this.xBody[11], -(this.yBody[9] - this.yBody[11]))) / 2;
        if (Canvas.gameTick % 2 == 0) {
            for (int i = this.xBody.length - 1; i > 0; i--) {
                this.xBody[i] = this.xBody[i - 1];
                this.yBody[i] = this.yBody[i - 1];
            }
            this.xBody[0] = this.x;
            this.yBody[0] = this.y;
        }
        if (Canvas.gameTick % 20 == 10) {
            int dis0 = Util.distance(this.target.x, this.target.y, this.xBody[2], this.yBody[2] - 50);
            int dis1 = Util.distance(this.target.x, this.target.y, this.x + this.xDuHead[this.index], (this.y + this.yDuHead[this.index]) - 50);
            int ang0 = Util.angle(this.target.x - this.xBody[2], -(this.target.y - (this.yBody[2] - 50)));
            int ang1 = Util.angle(this.target.x - (this.x + this.xDuHead[this.index]), -(this.target.y - ((this.y + this.yDuHead[this.index]) - 50)));
            if (dis0 > dis1 && Util.abs(ang0 - ang1) < 50) {
                int angl = Util.angle(this.xBody[2] - (this.x + this.xDuHead[this.index]), -((this.yBody[2] - 50) - ((this.y + this.yDuHead[this.index]) - 50)));
                int xa = ((-10) * Util.cos(angl)) >> 10;
                int ya = (-((-10) * Util.sin(angl))) >> 10;
                Canvas.gameScr.startNewMagicBeam(4, this, this.target, this.x + this.xDuHead[this.index] + xa, ((this.y + this.yDuHead[this.index]) - 50) + ya, 0, (byte) 0, angl);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(imgShadow, this.xBody[11], this.yBody[11], 3);
        for (int i = 0; i < this.xBody.length - 1; i++) {
            g.drawImage(imgShadow, this.xBody[i], this.yBody[i], 3);
        }
        g.drawImage(imgShadow, this.x, this.y, 3);
        if (this.dynamicEffBottom != null) {
            for (int ii = 0; ii < this.dynamicEffBottom.size(); ii++) {
                ((DynamicEffect) this.dynamicEffBottom.elementAt(ii)).paint(g, this.x, this.y);
            }
        }
        g.drawRegion(img, imgObj.x0[this.IndexRear[this.indexRear]], imgObj.y0[this.IndexRear[this.indexRear]], imgObj.w[this.IndexRear[this.indexRear]], imgObj.h[this.IndexRear[this.indexRear]], this.tranRear[this.indexRear], this.xBody[11] + this.xDuRear[this.indexRear], (this.yBody[11] + this.yDuRear[this.indexRear]) - 50, 3);
        if (this.index == 1 || this.index == 2 || this.index == 3) {
            paintHead(g);
            paintBody(g);
        } else {
            paintBody(g);
            paintHead(g);
        }
        if (this.dynamicEffTop != null) {
            for (int ii2 = 0; ii2 < this.dynamicEffTop.size(); ii2++) {
                ((DynamicEffect) this.dynamicEffTop.elementAt(ii2)).paint(g, this.x, this.y);
            }
        }
    }

    public void paintBody(Graphics g) {
        for (int i = 0; i < this.xBody.length - 1; i++) {
            g.drawRegion(img, imgObj.x0[5], imgObj.y0[5], imgObj.w[5], imgObj.h[5], 0, this.xBody[i], this.yBody[i] - 50, 3);
        }
    }

    public void paintHead(Graphics g) {
        g.drawRegion(img, imgObj.x0[this.arrIndex[this.index]], imgObj.y0[this.arrIndex[this.index]], imgObj.w[this.arrIndex[this.index]], imgObj.h[this.arrIndex[this.index]], this.tranIndex[this.index], this.x + this.xDuHead[this.index], (this.y + this.yDuHead[this.index]) - 50, 3);
    }
}
