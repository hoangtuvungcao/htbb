package game.model;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class IsAnimal {
    public static byte[][][] frameRun = {new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}};
    public static byte[][][] frameStand = {new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}};
    public static byte[][][] frameAt = {new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}, new byte[][]{new byte[1], new byte[1], new byte[1], new byte[1]}};
    public static Image[] img = new Image[10];
    public static int[] wimg = new int[10];
    public static int[] hImg = new int[10];
    public static int[] addx = new int[10];
    public static int[] addy = new int[10];
    public byte idMonster = 0;
    public int status;
    public int idFrame;
    public int p1;
    public int dir;
    public int type;

    public void paint(Graphics g, int xx, int yy) {
        if (img[this.idMonster] == null) {
            return;
        }
        g.drawRegion(img[this.idMonster], 0, this.idFrame * hImg[this.idMonster], wimg[this.idMonster], hImg[this.idMonster], 0, xx + addx[this.idMonster], yy + addy[this.idMonster], 33);
    }

    public void update() {
        try {
            switch (this.status) {
                case 0:
                    this.p1++;
                    if (this.p1 > frameStand[this.idMonster][this.dir].length - 1) {
                        this.p1 = 0;
                    }
                    this.idFrame = frameStand[this.idMonster][this.dir][this.p1];
                    return;
                case 1:
                    this.p1++;
                    if (this.p1 > frameRun[this.idMonster][this.dir].length - 1) {
                        this.p1 = 0;
                    }
                    this.idFrame = frameRun[this.idMonster][this.dir][this.p1];
                    return;
                case 2:
                    this.p1++;
                    if (this.p1 > frameAt[this.idMonster][this.dir].length - 1) {
                        this.p1 = frameAt[this.idMonster][this.dir].length - 1;
                    }
                    this.idFrame = frameAt[this.idMonster][this.dir][this.p1];
                    break;
            }
        } catch (Exception e) {
            System.out.println(new StringBuffer(String.valueOf((int) this.idMonster)).append(" >> ").append(this.p1).append(" >> ").append(this.dir).toString());
        }
    }

    public int getHeight() {
        return hImg[this.idMonster];
    }
}
