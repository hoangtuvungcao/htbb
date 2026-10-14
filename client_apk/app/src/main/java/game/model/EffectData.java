package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.render.Res;
import network.Message;

public class EffectData {
    public short ID;
    public byte[] arrFrame;
    public Frame[] frame;
    public Bitmap img;
    public ImageInfo[] imgImfo;

    public void setInfo(Message message) {
        try {
            byte[] bArr = new byte[message.reader().readShort()];
            message.reader().read(bArr);
            this.img = Res.createImgByByteArray(bArr);
            int i = message.reader().readByte();
            this.imgImfo = new ImageInfo[i];
            for (int i2 = 0; i2 < i; i2++) {
                this.imgImfo[i2] = new ImageInfo();
                this.imgImfo[i2].ID = message.reader().readByte();
                this.imgImfo[i2].x0 = message.reader().readByte();
                this.imgImfo[i2].y0 = message.reader().readByte();
                this.imgImfo[i2].w = message.reader().readByte();
                this.imgImfo[i2].h = message.reader().readByte();
            }
            int i3 = message.reader().readByte();
            this.frame = new Frame[i3];
            for (int i4 = 0; i4 < i3; i4++) {
                this.frame[i4] = new Frame();
                int i5 = message.reader().readByte();
                this.frame[i4].dx = new byte[i5];
                this.frame[i4].dy = new byte[i5];
                this.frame[i4].idImg = new byte[i5];
                for (int i6 = 0; i6 < i5; i6++) {
                    this.frame[i4].dx[i6] = message.reader().readByte();
                    this.frame[i4].dy[i6] = message.reader().readByte();
                    this.frame[i4].idImg[i6] = message.reader().readByte();
                }
            }
            int i7 = message.reader().readByte();
            this.arrFrame = new byte[i7];
            for (int i8 = 0; i8 < i7; i8++) {
                this.arrFrame[i8] = message.reader().readByte();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ImageInfo getImgInfo(byte b) {
        int i = 0;
        while (true) {
            ImageInfo[] imageInfoArr = this.imgImfo;
            if (i >= imageInfoArr.length) {
                return null;
            }
            if (imageInfoArr[i].ID == b) {
                return this.imgImfo[i];
            }
            i++;
        }
    }

    public void paint(Graphics graphics, int i, int i2, int i3) {
        Frame frame = this.frame[this.arrFrame[i3]];
        for (int i4 = 0; i4 < frame.dx.length; i4++) {
            ImageInfo imgInfo = getImgInfo(frame.idImg[i4]);
            graphics.drawRegion(this.img, imgInfo.x0, imgInfo.y0, imgInfo.w, imgInfo.h, 0, i + frame.dx[i4], i2 + frame.dy[i4], 0);
        }
    }
}
