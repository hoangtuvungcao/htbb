package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameData;

public class AnimalMove extends Char {
    public short addY;
    public short count;
    public short dir;
    byte fLip;
    public short idFrame;
    public short idSameMyOwnerID;
    public short imgID;
    public String infoPet;
    public short status;
    public short sumFrame;
    public int timeMagic;
    public long timeStart;
    public int totalTime;
    public short type;
    public short vMax;
    public short xTo;
    public short yTo;
    public short wImg = 1;
    public short hImg = 1;
    public Char myOwner = null;
    public short v = 5;
    byte index = 0;
    byte[] arr = {-1, 0, 1, 0};
    byte[][][] arrFrameMove = {new byte[][]{new byte[]{0, 0, 0, 1, 1, 1, 2, 2, 2, 1, 1, 1}, new byte[]{3, 3, 3, 4, 4, 4, 5, 5, 5, 4, 4, 4}, new byte[]{6, 6, 6, 7, 7, 7, 8, 8, 8, 7, 7, 7}, new byte[]{6, 6, 6, 7, 7, 7, 8, 8, 8, 7, 7, 7}}, new byte[][]{new byte[]{0, 0, 1, 1, 2, 2, 1, 1, 3, 3}, new byte[]{5, 5, 6, 6, 5, 5, 7, 7}, new byte[]{9, 9, 10, 10, 9, 9, 11, 11}, new byte[]{9, 9, 10, 10, 9, 9, 11, 11}}, new byte[][]{new byte[]{0, 0, 0, 0, 1, 1, 1, 1}, new byte[]{0, 0, 0, 0, 1, 1, 1, 1}, new byte[]{0, 0, 0, 0, 1, 1, 1, 1}, new byte[]{0, 0, 0, 0, 1, 1, 1, 1}}, new byte[][]{new byte[]{0, 0, 1, 1}, new byte[]{2, 2, 3, 3}, new byte[]{4, 4, 5, 5}, new byte[]{4, 4, 5, 5}}, new byte[][]{new byte[]{0, 0, 1, 1}, new byte[]{2, 2, 3, 3}, new byte[]{4, 4, 5, 5}, new byte[]{4, 4, 5, 5}}, new byte[][]{new byte[]{0, 0, 1, 1, 2, 2, 1, 1, 3, 3}, new byte[]{5, 5, 6, 6, 5, 5, 7, 7}, new byte[]{9, 9, 10, 10, 9, 9, 11, 11}, new byte[]{9, 9, 10, 10, 9, 9, 11, 11}}, new byte[][]{new byte[]{0, 0, 1, 1, 2, 2, 1, 1, 3, 3}, new byte[]{5, 5, 6, 6, 5, 5, 7, 7}, new byte[]{9, 9, 10, 10, 9, 9, 11, 11}, new byte[]{9, 9, 10, 10, 9, 9, 11, 11}}};
    byte[][][] arrFrameStand = {new byte[][]{new byte[]{0, 0, 0, 1, 1, 1, 2, 2, 2, 1, 1, 1}, new byte[]{3, 3, 3, 4, 4, 4, 5, 5, 5, 4, 4, 4}, new byte[]{6, 6, 6, 7, 7, 7, 8, 8, 8, 7, 7, 7}, new byte[]{6, 6, 6, 7, 7, 7, 8, 8, 8, 7, 7, 7}}, new byte[][]{new byte[]{0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1}, new byte[]{4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5}, new byte[]{8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9}, new byte[]{8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9}}, new byte[][]{new byte[]{0, 0, 0, 0, 1, 1, 1, 1}, new byte[]{0, 0, 0, 0, 1, 1, 1, 1}, new byte[]{0, 0, 0, 0, 1, 1, 1, 1}, new byte[]{0, 0, 0, 0, 1, 1, 1, 1}}, new byte[][]{new byte[]{0, 0, 1, 1}, new byte[]{2, 2, 3, 3}, new byte[]{4, 4, 5, 5}, new byte[]{4, 4, 5, 5}}, new byte[][]{new byte[]{0, 0, 1, 1}, new byte[]{2, 2, 3, 3}, new byte[]{4, 4, 5, 5}, new byte[]{4, 4, 5, 5}}, new byte[][]{new byte[]{0, 0, 1, 1, 2, 2, 1, 1, 3, 3}, new byte[]{5, 5, 6, 6, 5, 5, 7, 7}, new byte[]{9, 9, 10, 10, 9, 9, 11, 11}, new byte[]{9, 9, 10, 10, 9, 9, 11, 11}}, new byte[][]{new byte[]{0, 0, 1, 1, 2, 2, 1, 1, 3, 3}, new byte[]{5, 5, 6, 6, 5, 5, 7, 7}, new byte[]{9, 9, 10, 10, 9, 9, 11, 11}, new byte[]{9, 9, 10, 10, 9, 9, 11, 11}}};

    public void setFrameType4() {
        if (this.type == 4) {
            for (int i = 0; i < this.arrFrameStand[this.type].length; i++) {
                for (int j = 0; j < this.arrFrameStand[this.type][i].length; j++) {
                    this.arrFrameStand[this.type][i] = new byte[this.sumFrame * 3];
                    this.arrFrameStand[this.type][i][0] = 0;
                    byte frame = 0;
                    for (int k = 1; k < this.sumFrame * 3; k++) {
                        this.arrFrameStand[this.type][i][k] = frame;
                        if (k % 3 == 0) {
                            frame = (byte) (frame + 1);
                        }
                    }
                }
            }
            this.arrFrameMove[this.type] = this.arrFrameStand[this.type];
        }
    }

    @Override
    public void setPosTo(short s, short s2) {
        this.xTo = s;
        this.yTo = s2;
    }

    @Override
    public void update() {
        boolean z;
        boolean z2;
        short s = this.status;
        if (s == 0) {
            short s2 = (short) (this.count + 1);
            this.count = s2;
            byte[] bArr = this.arrFrameStand[this.type][this.dir];
            if (s2 > bArr.length - 1) {
                this.count = (short) 0;
            }
            this.idFrame = bArr[this.count];
            if (isOutRange()) {
                this.status = (short) 1;
            }
            this.vMax = (short) 0;
        } else if (s == 1) {
            this.yTo = this.myOwner.y;
            if (this.type == 1) {
                if (this.myOwner.dir == 2) {
                    this.xTo = (short) (this.myOwner.x + (this.myOwner.width / 2) + 16);
                } else if (this.myOwner.dir == 3) {
                    this.xTo = (short) ((this.myOwner.x - (this.myOwner.width / 2)) - 10);
                } else {
                    this.xTo = this.myOwner.x;
                    if (this.myOwner.dir == 1) {
                        this.yTo = (short) (this.myOwner.y + this.myOwner.height);
                    } else {
                        this.yTo = (short) ((this.myOwner.y - this.myOwner.height) - 10);
                    }
                }
            } else {
                if (this.myOwner.dir == 2 || this.myOwner.dir == 1) {
                    this.xTo = (short) (this.myOwner.x + (this.myOwner.width / 2) + (this.type != 2 ? 14 : 5));
                } else {
                    this.xTo = (short) ((this.myOwner.x - (this.myOwner.width / 2)) - (this.type == 2 ? 5 : 10));
                }
            }
            int iAbs = GCanvas.abs(this.x - this.xTo);
            int iAbs2 = GCanvas.abs(this.y - this.yTo);
            if (iAbs <= this.v + this.vMax) {
                this.x = this.xTo;
                z = true;
            } else {
                z = false;
            }
            if (iAbs2 <= this.v + this.vMax) {
                this.y = this.yTo;
                z2 = true;
            } else {
                z2 = false;
            }
            if (z && z2 && (this.myOwner.state == 0 || this.myOwner.state == 2)) {
                if (this.myOwner.ID != GCanvas.gameScr.mainChar.ID || GCanvas.gameScr.mainChar.posTransRoad == null) {
                    this.status = (short) 0;
                }
            } else {
                if (this.x < this.xTo) {
                    this.x = (short) (this.x + this.v + this.vMax);
                    this.dir = (short) 3;
                } else if (this.x > this.xTo) {
                    this.x = (short) (this.x - (this.v + this.vMax));
                    this.dir = (short) 2;
                } else if (this.y > this.yTo) {
                    this.y = (short) (this.y - (this.v + this.vMax));
                    this.dir = (short) 1;
                } else if (this.y < this.yTo) {
                    this.y = (short) (this.y + this.v + this.vMax);
                    this.dir = (short) 0;
                } else {
                    this.dir = this.myOwner.dir;
                }
                if (isOutRange()) {
                    this.vMax = (byte) (this.v / 2);
                }
                if (this.dir == 0) {
                    short s3 = this.y;
                    short s4 = this.yTo;
                    if (s3 >= s4) {
                        this.y = s4;
                        this.vMax = (short) 0;
                    }
                }
                short s5 = (short) (this.count + 1);
                this.count = s5;
                byte[] bArr2 = this.arrFrameMove[this.type][this.dir];
                if (s5 > bArr2.length - 1) {
                    this.count = (short) 0;
                }
                this.idFrame = bArr2[this.count];
            }
        } else if (s == 3) {
            int i = this.timeMagic;
            if (i > 0) {
                this.timeMagic = i - 1;
            }
            if (this.timeMagic <= 0) {
                GCanvas.gameScr.actors.removeElement(this);
            }
            if (this.timeMagic % 4 == 0) {
                EffectManager.addHiEffect(this.x, (this.y + this.addY) - (this.hImg / 2), this.type != 1 ? 11 : 23);
            }
        }
        if (this.type != 1 && GCanvas.gameTick % 2 == 0) {
            byte b = (byte) (this.index + 1);
            this.index = b;
            if (b > this.arr.length - 1) {
                this.index = (byte) 0;
            }
        }
        if (this.myOwner.isDie && GCanvas.gameScr.actors.contains(this)) {
            GCanvas.gameScr.actors.removeElement(this);
        }
        if (((long) this.totalTime) - ((System.currentTimeMillis() - this.timeStart) / 60000) <= 0 && this.status != 3) {
            this.status = (short) 3;
            this.timeMagic = 20;
        }
        if (this.dir == 2) {
            this.fLip = (byte) 2;
        } else {
            this.fLip = (byte) 0;
        }
        super.update();
    }

    public boolean isOutRange() {
        int i;
        int i2;
        short s;
        Char r0 = this.myOwner;
        if (r0 == null) {
            return false;
        }
        if (this.type == 2) {
            i2 = 32;
            i = 30;
        } else {
            i = 70;
            i2 = 64;
        }
        int i3 = i;
        if (r0.dir == 0) {
            i2 = 48;
        }
        return Util.distance(this.x, this.y, this.myOwner.x, this.myOwner.y) > i2 || !(Math.abs(this.x - this.myOwner.x) <= i || (s = this.dir) == 0 || s == 1) || Math.abs(this.y - this.myOwner.y) > i3;
    }

    @Override
    public void paint(Graphics graphics) {
        ImageIcon imgIcon = GameData.getImgIcon((short) (this.imgID + 5200));
        if (this.myOwner == null || imgIcon == null || imgIcon.isLoad) {
            return;
        }
        if (this.hImg == 1) {
            this.hImg = (short) (imgIcon.img.getHeight() / this.sumFrame);
        }
        if (this.wImg == 1) {
            this.wImg = (short) imgIcon.img.getWidth();
        }
        if (this.timeMagic % 2 == 0) {
            Bitmap bitmap = imgIcon.img;
            short s = this.hImg;
            short s2 = 0;
            graphics.drawRegion(bitmap, 0, s * this.idFrame, this.wImg, s, this.fLip, this.x, this.y + this.addY + this.arr[this.index] + ((this.myOwner.useHorse != -1 || this.type == 1) ? 0 : 3), Graphics.BOTTOM | Graphics.HCENTER);
            Bitmap bitmap2 = Res.imgSD;
            short s3 = this.x;
            short s4 = this.y;
            if (this.myOwner.useHorse == -1 && this.type != 1) {
                s2 = 3;
            }
            graphics.drawImage(bitmap2, s3, s4 + s2, 3);
        }
    }
}
