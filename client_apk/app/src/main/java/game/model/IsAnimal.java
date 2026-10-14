package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;

public class IsAnimal {
    public int dir;
    public int idFrame;
    public byte idMonster = 0;
    public int p1;
    public int status;
    public int type;
    public static byte[][][] frameRun = {new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}};
    public static byte[][][] frameStand = {new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}};
    public static byte[][][] frameAt = {new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}, new byte[][]{new byte[]{0}, new byte[]{0}, new byte[]{0}, new byte[]{0}}};
    public static Bitmap[] img = new Bitmap[10];
    public static int[] wimg = new int[10];
    public static int[] hImg = new int[10];
    public static int[] addx = new int[10];
    public static int[] addy = new int[10];

    public void paint(Graphics graphics, int i, int i2) {
        Bitmap[] bitmapArr = img;
        byte b = this.idMonster;
        Bitmap bitmap = bitmapArr[b];
        if (bitmap == null) {
            return;
        }
        int i3 = this.idFrame;
        int i4 = hImg[b];
        graphics.drawRegion(bitmap, 0, i3 * i4, wimg[b], i4, 0, i + addx[b], i2 + addy[b], 33);
    }

    public void update() {
        if (this.idMonster < 0) {
            this.idMonster = (byte) 0;
        }
        try {
            int i = this.status;
            if (i == 0) {
                int i2 = this.p1 + 1;
                this.p1 = i2;
                byte[] bArr = frameStand[this.idMonster][this.dir];
                if (i2 > bArr.length - 1) {
                    this.p1 = 0;
                }
                this.idFrame = bArr[this.p1];
                return;
            }
            if (i == 1) {
                int i3 = this.p1 + 1;
                this.p1 = i3;
                byte[] bArr2 = frameRun[this.idMonster][this.dir];
                if (i3 > bArr2.length - 1) {
                    this.p1 = 0;
                }
                this.idFrame = bArr2[this.p1];
                return;
            }
            if (i != 2) {
                return;
            }
            int i4 = this.p1 + 1;
            this.p1 = i4;
            byte[] bArr3 = frameAt[this.idMonster][this.dir];
            if (i4 > bArr3.length - 1) {
                this.p1 = bArr3.length - 1;
            }
            this.idFrame = bArr3[this.p1];
        } catch (Exception unused) {
            System.out.println(((int) this.idMonster) + " >> " + this.p1 + " >> " + this.dir);
        }
    }

    public int getHeight() {
        return hImg[this.idMonster];
    }
}
