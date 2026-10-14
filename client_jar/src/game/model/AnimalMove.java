package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.GameData;
import javax.microedition.lcdui.Graphics;

public class AnimalMove extends Char {
    public short status;
    public short dir;
    public short xTo;
    public short yTo;
    public short sumFrame;
    public short addY;
    public short idSameMyOwnerID;
    public short vMax;
    public short type;
    public short imgID;
    public short count;
    public short idFrame;
    public int totalTime;
    public int timeMagic;
    public long timeStart;
    public String infoPet;
    byte fLip;
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
                    byte n = 0;
                    for (int k = 1; k < this.sumFrame * 3; k++) {
                        this.arrFrameStand[this.type][i][k] = n;
                        if (k % 3 == 0) {
                            n = (byte) (n + 1);
                        }
                    }
                }
            }
            this.arrFrameMove[this.type] = this.arrFrameStand[this.type];
        }
    }

    @Override
    public void setPosTo(short x, short y) {
        this.xTo = x;
        this.yTo = y;
    }

    @Override
    public void update() {
        switch (this.status) {
            case 0:
                this.count = (short) (this.count + 1);
                if (this.count > this.arrFrameStand[this.type][this.dir].length - 1) {
                    this.count = (short) 0;
                }
                this.idFrame = this.arrFrameStand[this.type][this.dir][this.count];
                if (isOutRange()) {
                    this.status = (short) 1;
                }
                this.vMax = (short) 0;
                break;
            case 1:
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
                } else if (this.myOwner.dir == 2 || this.myOwner.dir == 1) {
                    this.xTo = (short) (this.myOwner.x + (this.myOwner.width / 2) + (this.type == 2 ? 5 : 14));
                } else {
                    this.xTo = (short) ((this.myOwner.x - (this.myOwner.width / 2)) - (this.type == 2 ? 5 : 10));
                }
                boolean isMatchX = false;
                boolean isMatchY = false;
                int dx1 = Canvas.abs(this.x - this.xTo);
                int dy1 = Canvas.abs(this.y - this.yTo);
                if (dx1 <= this.v + this.vMax) {
                    this.x = this.xTo;
                    isMatchX = true;
                }
                if (dy1 <= this.v + this.vMax) {
                    this.y = this.yTo;
                    isMatchY = true;
                }
                if (isMatchX && isMatchY && (this.myOwner.state == 0 || this.myOwner.state == 2)) {
                    if (this.myOwner.ID != Canvas.gameScr.mainChar.ID || Canvas.gameScr.mainChar.posTransRoad == null) {
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
                    if (this.dir == 0 && this.y >= this.yTo) {
                        this.y = this.yTo;
                        this.vMax = (short) 0;
                    }
                    this.count = (short) (this.count + 1);
                    if (this.count > this.arrFrameMove[this.type][this.dir].length - 1) {
                        this.count = (short) 0;
                    }
                    this.idFrame = this.arrFrameMove[this.type][this.dir][this.count];
                }
                break;
            case 3:
                if (this.timeMagic > 0) {
                    this.timeMagic--;
                }
                if (this.timeMagic <= 0) {
                    Canvas.gameScr.actors.removeElement(this);
                }
                if (this.timeMagic % 4 == 0) {
                    EffectManager.addHiEffect(this.x, (this.y + this.addY) - (this.hImg / 2), this.type != 1 ? 11 : 23);
                }
                break;
        }
        if (this.type != 1 && Canvas.gameTick % 2 == 0) {
            this.index = (byte) (this.index + 1);
            if (this.index > this.arr.length - 1) {
                this.index = (byte) 0;
            }
        }
        if (this.myOwner.isDie && Canvas.gameScr.actors.contains(this)) {
            Canvas.gameScr.actors.removeElement(this);
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
        if (this.myOwner == null) {
            return false;
        }
        int limRange = 64;
        int dx = 70;
        int dy = 70;
        if (this.type == 2) {
            limRange = 32;
            dy = 30;
            dx = 30;
        }
        if (this.myOwner.dir == 0) {
            limRange = 48;
        }
        if (Util.distance(this.x, this.y, this.myOwner.x, this.myOwner.y) <= limRange) {
            if ((Math.abs(this.x - this.myOwner.x) > dx && this.dir != 0 && this.dir != 1) || Math.abs(this.y - this.myOwner.y) > dy) {
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public void paint(Graphics g) {
        ImageIcon img = GameData.getImgIcon((short) (this.imgID + 5200));
        if (this.myOwner != null && img != null && !img.isLoad) {
            if (this.hImg == 1) {
                this.hImg = (short) (img.img.getHeight() / this.sumFrame);
            }
            if (this.wImg == 1) {
                this.wImg = (short) img.img.getWidth();
            }
            if (this.timeMagic % 2 == 0) {
                g.drawRegion(img.img, 0, this.hImg * this.idFrame, this.wImg, this.hImg, this.fLip, this.x, this.y + this.addY + this.arr[this.index] + ((this.myOwner.useHorse != -1 || this.type == 1) ? 0 : 3), 33);
                g.drawImage(Res.imgShadowPet, this.x, this.y + ((this.myOwner.useHorse != -1 || this.type == 1) ? (short) 0 : (short) 3), 3);
            }
        }
    }
}
