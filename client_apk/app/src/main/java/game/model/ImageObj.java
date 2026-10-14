package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.core.j2me.Image;
import game.render.Res;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class ImageObj {
    public short[] dx;
    public short[] dy;
    public byte[] h;
    private Bitmap[] img;
    public byte[] index;
    public byte[] w;
    public byte[] x0;
    public byte[] y0;

    public ImageObj() {
        this.img = new Bitmap[1];
    }

    public void paint(Graphics graphics, int i, int i2, int i3, int i4, int i5, int i6) {
        graphics.drawRegion(this.img[0], this.x0[i3], this.y0[i3], this.w[i3], this.h[i3], i4, i + (this.dx[i3] * i5), i2 + (this.dy[i3] * i6), 3);
    }

    public ImageObj(int i, int i2) {
        this.img = new Bitmap[1];
        try {
            byte[] bArrLoadFile = FilePack.instance.loadFile(i2 + "_h");
            byte[] bArrLoadFile2 = FilePack.instance.loadFile("data");
            Bitmap[] bitmapArr = this.img;
            Bitmap bitmapCreateImgByHeader = Res.createImgByHeader(bArrLoadFile, bArrLoadFile2);
            bitmapArr[0] = bitmapCreateImgByHeader;
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(FilePack.instance.loadFile("pos")));
            int i3 = dataInputStream.readByte();
            this.index = new byte[i3];
            this.x0 = new byte[i3];
            this.y0 = new byte[i3];
            this.w = new byte[i3];
            this.h = new byte[i3];
            this.dx = new short[i3];
            this.dy = new short[i3];
            for (int i4 = 0; i4 < i3; i4++) {
                this.index[i4] = dataInputStream.readByte();
                this.x0[i4] = dataInputStream.readByte();
                this.y0[i4] = dataInputStream.readByte();
                this.w[i4] = dataInputStream.readByte();
                this.h[i4] = dataInputStream.readByte();
                this.dx[i4] = dataInputStream.readByte();
                this.dy[i4] = dataInputStream.readByte();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ImageObj(byte[] bArr) {
        this.img = new Bitmap[1];
        try {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArr));
            this.img = new Bitmap[dataInputStream.readByte()];
            for (int i = 0; i < this.img.length; i++) {
                int i2 = dataInputStream.readShort();
                byte[] bArr2 = new byte[i2];
                dataInputStream.read(bArr2);
                this.img[i] = Image.createImage(bArr2, 0, i2);
            }
        } catch (Exception unused) {
        }
    }

    public Bitmap getImage(int i) {
        Bitmap[] bitmapArr = this.img;
        if (bitmapArr == null) {
            return null;
        }
        return bitmapArr[i];
    }

    public void initImage(int i) {
        this.img = new Bitmap[i];
    }
}
