package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.core.j2me.Image;
import game.networklogic.GameService;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class TreeInfo {
    public int dx;
    public int dy;
    public int endx;
    public int endy;
    public int h;
    public int id;
    public Bitmap imgTree;
    public int startx;
    public int starty;
    long timeGetImage;
    public int w;

    public void onImage(byte[] bArr, int i) {
        try {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArr));
            int i2 = dataInputStream.readShort();
            byte[] bArr2 = new byte[i2];
            dataInputStream.read(bArr2, 0, i2);
            this.imgTree = Image.createImage(bArr2, 0, i2);
            int i3 = dataInputStream.readShort();
            byte[] bArr3 = new byte[i3];
            dataInputStream.read(bArr3, 0, i3);
            DataInputStream dataInputStream2 = new DataInputStream(new ByteArrayInputStream(bArr3));
            this.dx = dataInputStream2.readByte();
            this.dy = dataInputStream2.readByte();
            this.startx = dataInputStream2.readUnsignedByte();
            this.starty = dataInputStream2.readUnsignedByte();
            this.endx = dataInputStream2.readUnsignedByte();
            this.endy = dataInputStream2.readUnsignedByte();
            this.w = this.imgTree.getWidth();
            this.h = this.imgTree.getHeight();
            dataInputStream.close();
            dataInputStream2.close();
        } catch (Exception e) {
            Cout.println("ERROR TREE INFO");
            System.out.println("LOI LOAD CAY TRONG TREEINFO " + i);
            this.imgTree = null;
            e.printStackTrace();
        }
    }

    public Bitmap getImage() {
        if (System.currentTimeMillis() - this.timeGetImage > 180000 && this.imgTree == null) {
            GameService.gI().getTreeImage(this.id, 0, 0);
        }
        this.timeGetImage = System.currentTimeMillis();
        return this.imgTree;
    }

    public int getHeight() {
        Bitmap bitmap = this.imgTree;
        if (bitmap != null) {
            return bitmap.getHeight();
        }
        return 0;
    }

    public int getWidth() {
        Bitmap bitmap = this.imgTree;
        if (bitmap != null) {
            return bitmap.getWidth();
        }
        return 0;
    }

    public void paint(Graphics graphics, int i, int i2) {
        if (getImage() != null) {
            graphics.drawImage(this.imgTree, i, i2, 0);
        }
    }

    public void loadData() {
        try {
            this.imgTree = Image.createImage("/t6.png");
            DataInputStream dataInputStream = new DataInputStream("".getClass().getResourceAsStream("t6"));
            this.dx = dataInputStream.readUnsignedByte();
            int unsignedByte = dataInputStream.readUnsignedByte();
            this.dy = unsignedByte;
            int i = this.dx;
            if (i > 127) {
                this.dx = i - 256;
            }
            if (unsignedByte > 127) {
                this.dy = unsignedByte - 256;
            }
            this.startx = dataInputStream.readUnsignedByte();
            this.starty = dataInputStream.readUnsignedByte();
            this.endx = dataInputStream.readUnsignedByte();
            this.endy = dataInputStream.readUnsignedByte();
            this.w = this.imgTree.getWidth();
            this.h = this.imgTree.getHeight();
        } catch (Exception unused) {
        }
    }
}
