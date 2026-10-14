package game.model;

import game.render.Res;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class ImageObj {
    private Image[] img;
    public byte[] index;
    public byte[] x0;
    public byte[] y0;
    public byte[] w;
    public byte[] h;
    public short[] dx;
    public short[] dy;

    public ImageObj() {
        this.img = new Image[1];
    }

    public void paint(Graphics g, int x, int y, int index, int trans, int dirX, int dirY) {
        g.drawRegion(this.img[0], this.x0[index], this.y0[index], this.w[index], this.h[index], trans, x + (this.dx[index] * dirX), y + (this.dy[index] * dirY), 3);
    }

    public ImageObj(int type, int palate) {
        this.img = new Image[1];
        try {
            byte[] header = FilePack.instance.loadFile(new StringBuffer(String.valueOf(palate)).append("_h").toString());
            byte[] data = FilePack.instance.loadFile("data");
            this.img[0] = Res.createImgByHeader(header, data);
            byte[] pos = FilePack.instance.loadFile("pos");
            ByteArrayInputStream bi = new ByteArrayInputStream(pos);
            DataInputStream dis = new DataInputStream(bi);
            int size = dis.readByte();
            this.index = new byte[size];
            this.x0 = new byte[size];
            this.y0 = new byte[size];
            this.w = new byte[size];
            this.h = new byte[size];
            this.dx = new short[size];
            this.dy = new short[size];
            for (int i = 0; i < size; i++) {
                this.index[i] = dis.readByte();
                this.x0[i] = dis.readByte();
                this.y0[i] = dis.readByte();
                this.w[i] = dis.readByte();
                this.h[i] = dis.readByte();
                this.dx[i] = dis.readByte();
                this.dy[i] = dis.readByte();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ImageObj(byte[] array) {
        this.img = new Image[1];
        try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(array));
            this.img = new Image[dis.readByte()];
            for (int i = 0; i < this.img.length; i++) {
                int len = dis.readShort();
                byte[] bimg = new byte[len];
                dis.read(bimg);
                this.img[i] = Image.createImage(bimg, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Image getImage(int indexImg) {
        if (this.img == null) {
            return null;
        }
        return this.img[indexImg];
    }

    public void initImage(int nImg) {
        this.img = new Image[nImg];
    }
}
