package game.model;

import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.Font;

public class Ground extends Actor {
    byte addyMytree;
    String clockInfo = "";
    byte colorInfo;
    byte count;
    long currentTime;
    public short idTree;
    public boolean isBuyOk;
    public int minutes;
    public MyTree myTree;
    public int timeLive;

    @Override
    public void setPosTo(short s, short s2) {
    }

    public Ground(int i, int i2, int i3, int i4, String str, String str2, int i5, int i6, boolean z, byte b, byte b2) {
        this.idTree = (short) 0;
        this.timeLive = 0;
        this.currentTime = 0L;
        this.ID = (short) i;
        this.idTree = (short) i2;
        this.x = (short) i3;
        this.y = (short) i4;
        this.catagory = (byte) 10;
        this.infoGround = str;
        this.timeLive = i6 / 60;
        this.minutes = i6 % 60;
        this.myTree = null;
        this.isBuyOk = z;
        this.currentTime = System.currentTimeMillis();
        this.addyMytree = b;
        this.colorInfo = b2;
        if (i2 > -1) {
            this.myTree = new MyTree(i2, i3 + 16, i4 + 16 + b, i5);
            GCanvas.gameScr.actors.addElement(this.myTree);
        }
    }

    public void setInfo(int i, int i2, int i3, int i4, String str, String str2, int i5, int i6, boolean z, byte b, byte b2) {
        this.ID = (short) i;
        short s = (short) i2;
        this.idTree = s;
        this.x = (short) i3;
        this.y = (short) i4;
        this.catagory = (byte) 10;
        this.infoGround = str;
        this.timeLive = i6 / 60;
        this.minutes = i6 % 60;
        this.isBuyOk = z;
        this.currentTime = System.currentTimeMillis();
        this.addyMytree = b;
        this.colorInfo = b2;
        if (i2 > -1) {
            MyTree myTree = this.myTree;
            if (myTree != null) {
                myTree.himg = 1;
                this.myTree.idTree = s;
                this.myTree.idImg = (byte) i5;
                return;
            }
            this.myTree = new MyTree(i2, i3 + 16, i4 + 16 + b, i5);
            GCanvas.gameScr.actors.addElement(this.myTree);
            return;
        }
        if (this.myTree != null) {
            GCanvas.gameScr.actors.removeElement(this.myTree);
            this.myTree = null;
        }
    }

    @Override
    public void paint(Graphics graphics) {
        if (Res.imgGround != null) {
            if (this.isBuyOk) {
                if (this.myTree == null) {
                    graphics.drawImage(Res.imgGround[0], this.x, this.y, 0);
                } else {
                    graphics.drawImage(Res.imgGround[2], this.x, this.y, 0);
                }
            } else {
                graphics.drawImage(Res.imgGround[1], this.x, this.y, 0);
            }
        }
        if (this.isFocus) {
            if (this.timeLive > -1 && this.myTree != null) {
                int width = Font.normalFont[this.colorInfo].getWidth(this.clockInfo);
                graphics.setColor(-15183304);
                int i = width / 2;
                int i2 = ((this.x + 16) - i) - 3;
                short s = this.y;
                MyTree myTree = this.myTree;
                int i3 = width + 5;
                graphics.fillRect(i2, s - (myTree == null ? 8 : myTree.himg + 15), i3, 14);
                graphics.setColor(-4608);
                int i4 = ((this.x + 16) - i) - 3;
                short s2 = this.y;
                MyTree myTree2 = this.myTree;
                graphics.drawRect(i4, s2 - (myTree2 == null ? 8 : myTree2.himg + 15), i3, 14);
                Font font = Font.normalFont[this.colorInfo];
                String str = this.clockInfo;
                int i5 = this.x + 16;
                short s3 = this.y;
                MyTree myTree3 = this.myTree;
                font.drawString(graphics, str, i5, s3 - (myTree3 == null ? 8 : myTree3.himg + 15), 2);
            }
            if (PopupName.imgArrow != null) {
                FrameImage frameImage = PopupName.imgArrow;
                int i6 = this.x + 16;
                short s4 = this.y;
                MyTree myTree4 = this.myTree;
                frameImage.drawFrame(0, i6, (s4 - (myTree4 == null ? 8 : myTree4.himg)) + (GCanvas.gameTick % 8), 0, 33, graphics);
            }
        }
    }

    @Override
    public void update() {
        byte bCurrentTimeMillis = (byte) (((long) this.minutes) - ((System.currentTimeMillis() - this.currentTime) / 1000));
        this.count = bCurrentTimeMillis;
        if (bCurrentTimeMillis <= 0) {
            this.timeLive--;
            this.currentTime = System.currentTimeMillis();
            this.minutes = 59;
        }
        if (this.isFocus && this.timeLive > -1 && this.myTree != null) {
            this.clockInfo = (this.timeLive < 10 ? new StringBuilder("0").append(this.timeLive) : new StringBuilder().append(this.timeLive).append("")).toString() + " : " + (this.count < 10 ? new StringBuilder("0").append((int) this.count) : new StringBuilder().append((int) this.count).append("")).toString();
        }
        super.update();
    }
}
