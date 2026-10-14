package game.model;

import android.graphics.Bitmap;
import effect.DynamicEffect;
import game.core.j2me.Graphics;
import game.model.arrow.Arrow;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.io.IOException;
import java.io.InputStream;

public class Boss_Dragon extends Monster {
    public static Bitmap img;
    public static ImageObj imgObj;
    public static Bitmap imgShadow;
    MagicLogic arrow;
    int index;
    int indexRear;
    LiveActor live;
    LiveActor target;
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
    byte[] tranIndex = {0, 0, 0, 2, 2, 2, 0, 0};
    byte[] IndexRear = {9, 6, 9, 6, 9, 6, 9, 6};
    byte[] tranRear = {4, 3, 3, 1, 7, 0, 0, 2};

    public void init() {
        imgObj = new ImageObj();
        try {
            FilePack.init("/other");
            imgShadow = FilePack.getImg("rong_shadow");
            img = FilePack.getImg("rong_Big0");
            FilePack.reset();
            InputStream resourceAsStream = "".getClass().getResourceAsStream("/rong/dragonData");
            int i = (byte) resourceAsStream.read();
            imgObj.index = new byte[i];
            imgObj.x0 = new byte[i];
            imgObj.y0 = new byte[i];
            imgObj.w = new byte[i];
            imgObj.h = new byte[i];
            for (int i2 = 0; i2 < i; i2++) {
                imgObj.index[i2] = (byte) resourceAsStream.read();
                imgObj.x0[i2] = (byte) resourceAsStream.read();
                imgObj.y0[i2] = (byte) resourceAsStream.read();
                imgObj.w[i2] = (byte) resourceAsStream.read();
                imgObj.h[i2] = (byte) resourceAsStream.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boss_Dragon() {
        int i = 0;
        MainChar mainChar = GCanvas.gameScr.mainChar;
        this.target = mainChar;
        this.x = (short) (mainChar.x - 80);
        this.y = (short) (this.target.y - 80);
        while (true) {
            short[] sArr = this.xBody;
            if (i < sArr.length) {
                sArr[i] = this.x;
                this.yBody[i] = this.y;
                i++;
            } else {
                this.live = new LiveActor() {
                    @Override
                    public void setPosTo(short s, short s2) {
                    }
                };
                MagicLogic magicLogic = new MagicLogic();
                this.arrow = magicLogic;
                magicLogic.isSpeedUp = true;
                this.arrow.setAngle(20);
                this.arrow.set(7, this.x, this.y, 7, this.target);
                return;
            }
        }
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
        short[] sArr = this.xBody;
        int i = sArr[9] - sArr[11];
        short[] sArr2 = this.yBody;
        this.indexRear = Arrow.findDirIndexFromAngle(Util.angle(i, -(sArr2[9] - sArr2[11]))) / 2;
        if (GCanvas.gameTick % 2 == 0) {
            for (int length = this.xBody.length - 1; length > 0; length--) {
                short[] sArr3 = this.xBody;
                int i2 = length - 1;
                sArr3[length] = sArr3[i2];
                short[] sArr4 = this.yBody;
                sArr4[length] = sArr4[i2];
            }
            this.xBody[0] = this.x;
            this.yBody[0] = this.y;
        }
        if (GCanvas.gameTick % 20 == 10) {
            int iDistance = Util.distance(this.target.x, this.target.y, this.xBody[2], this.yBody[2] - 50);
            int iDistance2 = Util.distance(this.target.x, this.target.y, this.x + this.xDuHead[this.index], (this.y + this.yDuHead[this.index]) - 50);
            int iAngle = Util.angle(this.target.x - this.xBody[2], -(this.target.y - (this.yBody[2] - 50)));
            int iAngle2 = Util.angle(this.target.x - (this.x + this.xDuHead[this.index]), -(this.target.y - ((this.y + this.yDuHead[this.index]) - 50)));
            if (iDistance <= iDistance2 || Util.abs(iAngle - iAngle2) >= 50) {
                return;
            }
            int iAngle3 = Util.angle(this.xBody[2] - (this.x + this.xDuHead[this.index]), -((this.yBody[2] - 50) - ((this.y + this.yDuHead[this.index]) - 50)));
            GCanvas.gameScr.startNewMagicBeam(4, this, this.target, this.x + this.xDuHead[this.index] + ((Util.cos(iAngle3) * (-10)) >> 10), ((this.y + this.yDuHead[this.index]) - 50) + ((-(Util.sin(iAngle3) * (-10))) >> 10), 0, (byte) 0, iAngle3);
        }
    }

    @Override
    public void paint(Graphics graphics) {
        graphics.drawImage(imgShadow, this.xBody[11], this.yBody[11], 3);
        int i = 0;
        while (true) {
            short[] sArr = this.xBody;
            if (i >= sArr.length - 1) {
                break;
            }
            graphics.drawImage(imgShadow, sArr[i], this.yBody[i], 3);
            i++;
        }
        graphics.drawImage(imgShadow, this.x, this.y, 3);
        if (this.dynamicEffBottom != null) {
            for (int i2 = 0; i2 < this.dynamicEffBottom.size(); i2++) {
                ((DynamicEffect) this.dynamicEffBottom.elementAt(i2)).paint(graphics, this.x, this.y);
            }
        }
        Bitmap bitmap = img;
        byte b = imgObj.x0[this.IndexRear[this.indexRear]];
        byte b2 = imgObj.y0[this.IndexRear[this.indexRear]];
        byte b3 = imgObj.w[this.IndexRear[this.indexRear]];
        byte[] bArr = imgObj.h;
        byte[] bArr2 = this.IndexRear;
        int i3 = this.indexRear;
        graphics.drawRegion(bitmap, b, b2, b3, bArr[bArr2[i3]], this.tranRear[i3], this.xDuRear[i3] + this.xBody[11], (this.yBody[11] + this.yDuRear[i3]) - 50, 3);
        int i4 = this.index;
        if (i4 == 1 || i4 == 2 || i4 == 3) {
            paintHead(graphics);
            paintBody(graphics);
        } else {
            paintBody(graphics);
            paintHead(graphics);
        }
        if (this.dynamicEffTop != null) {
            for (int i5 = 0; i5 < this.dynamicEffTop.size(); i5++) {
                ((DynamicEffect) this.dynamicEffTop.elementAt(i5)).paint(graphics, this.x, this.y);
            }
        }
    }

    public void paintBody(Graphics graphics) {
        for (int i = 0; i < this.xBody.length - 1; i++) {
            graphics.drawRegion(img, imgObj.x0[5], imgObj.y0[5], imgObj.w[5], imgObj.h[5], 0, this.xBody[i], this.yBody[i] - 50, 3);
        }
    }

    public void paintHead(Graphics graphics) {
        Bitmap bitmap = img;
        byte b = imgObj.x0[this.arrIndex[this.index]];
        byte b2 = imgObj.y0[this.arrIndex[this.index]];
        byte b3 = imgObj.w[this.arrIndex[this.index]];
        byte[] bArr = imgObj.h;
        byte[] bArr2 = this.arrIndex;
        int i = this.index;
        graphics.drawRegion(bitmap, b, b2, b3, bArr[bArr2[i]], this.tranIndex[i], this.xDuHead[this.index] + this.x, (this.y + this.yDuHead[this.index]) - 50, 3);
    }
}
