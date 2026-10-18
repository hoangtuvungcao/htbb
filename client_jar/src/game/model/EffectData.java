package game.model;

import game.render.Res;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import network.Message;

public class EffectData {
    public byte[] arrFrame;
    public ImageInfo[] imgImfo;
    public Image img;
    public Frame[] frame;
    public short ID;

    public void setInfo(Message msg) {
        try {
            byte[] dataImg = new byte[msg.reader().readShort()];
            msg.reader().read(dataImg);
            this.img = Res.createImgByByteArray(dataImg);
            int i = msg.reader().readByte();
            this.imgImfo = new ImageInfo[i];
            for (int i2 = 0; i2 < i; i2++) {
                this.imgImfo[i2] = new ImageInfo();
                this.imgImfo[i2].ID = msg.reader().readByte();
                this.imgImfo[i2].x0 = msg.reader().readByte();
                this.imgImfo[i2].y0 = msg.reader().readByte();
                this.imgImfo[i2].w = msg.reader().readByte();
                this.imgImfo[i2].h = msg.reader().readByte();
            }
            int i3 = msg.reader().readByte();
            this.frame = new Frame[i3];
            for (int i4 = 0; i4 < i3; i4++) {
                this.frame[i4] = new Frame();
                int i5 = msg.reader().readByte();
                this.frame[i4].dx = new byte[i5];
                this.frame[i4].dy = new byte[i5];
                this.frame[i4].idImg = new byte[i5];
                for (int a = 0; a < i5; a++) {
                    this.frame[i4].dx[a] = msg.reader().readByte();
                    this.frame[i4].dy[a] = msg.reader().readByte();
                    this.frame[i4].idImg[a] = msg.reader().readByte();
                }
            }
            int i6 = msg.reader().readByte();
            this.arrFrame = new byte[i6];
            for (int i7 = 0; i7 < i6; i7++) {
                this.arrFrame[i7] = msg.reader().readByte();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ImageInfo getImgInfo(byte id) {
        for (int i = 0; i < this.imgImfo.length; i++) {
            if (this.imgImfo[i].ID == id) {
                return this.imgImfo[i];
            }
        }
        return null;
    }

    public void paint(Graphics g, int x, int y, int index) {
        Frame fr = this.frame[this.arrFrame[index]];
        for (int i = 0; i < fr.dx.length; i++) {
            ImageInfo im = getImgInfo(fr.idImg[i]);
            g.drawRegion(this.img, im.x0, im.y0, im.w, im.h, 0, x + fr.dx[i], y + fr.dy[i], 0);
        }
    }
}
