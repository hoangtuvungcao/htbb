package game.model;

import game.networklogic.GameService;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class TreeInfo {
    public int dx;
    public int dy;
    public int startx;
    public int starty;
    public int endx;
    public int endy;
    public int w;
    public int h;
    public Image imgTree;
    public short id;
    long timeGetImage;

    public void onImage(byte[] arr, int index) {
        try {
            DataInputStream data = new DataInputStream(new ByteArrayInputStream(arr));
            byte[] img = new byte[data.readShort()];
            data.read(img, 0, img.length);
            this.imgTree = Image.createImage(img, 0, img.length);
            byte[] dtt = new byte[data.readShort()];
            data.read(dtt, 0, dtt.length);
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dtt));
            this.dx = dis.readByte();
            this.dy = dis.readByte();
            this.startx = dis.readUnsignedByte();
            this.starty = dis.readUnsignedByte();
            this.endx = dis.readUnsignedByte();
            this.endy = dis.readUnsignedByte();
            this.w = this.imgTree.getWidth();
            this.h = this.imgTree.getHeight();
            data.close();
            dis.close();
        } catch (Exception e) {
            Cout.println("ERROR TREE INFO");
            System.out.println(new StringBuffer("LOI LOAD CAY TRONG TREEINFO ").append(index).toString());
            this.imgTree = null;
            e.printStackTrace();
        }
    }

    public Image getImage() {
        if (System.currentTimeMillis() - this.timeGetImage > 180000 && this.imgTree == null) {
            GameService.gI().getTreeImage(this.id, 0, 0);
        }
        this.timeGetImage = System.currentTimeMillis();
        return this.imgTree;
    }

    public int getHeight() {
        if (this.imgTree != null) {
            return this.imgTree.getHeight();
        }
        return 0;
    }

    public int getWidth() {
        if (this.imgTree != null) {
            return this.imgTree.getWidth();
        }
        return 0;
    }

    public void paint(Graphics g, int x, int y) {
        if (getImage() != null) {
            g.drawImage(this.imgTree, x, y, 0);
        }
    }

    public void loadData() {
        try {
            this.imgTree = Image.createImage("/t6.png");
            InputStream is = "".getClass().getResourceAsStream("t6");
            DataInputStream dis = new DataInputStream(is);
            this.dx = dis.readUnsignedByte();
            this.dy = dis.readUnsignedByte();
            if (this.dx > 127) {
                this.dx -= 256;
            }
            if (this.dy > 127) {
                this.dy -= 256;
            }
            this.startx = dis.readUnsignedByte();
            this.starty = dis.readUnsignedByte();
            this.endx = dis.readUnsignedByte();
            this.endy = dis.readUnsignedByte();
            this.w = this.imgTree.getWidth();
            this.h = this.imgTree.getHeight();
        } catch (Exception e) {
        }
    }
}
