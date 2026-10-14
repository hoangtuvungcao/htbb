package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;

public class Ground extends Actor {
    public MyTree myTree;
    public short idTree;
    public int timeLive;
    public int minutes;
    long currentTime;
    byte count;
    byte addyMytree;
    byte colorInfo;
    public boolean isBuyOk;
    String clockInfo = "";

    public Ground(int idFarm, int id, int x, int y, String info, String name, int idImg, int timeLive, boolean isBuyOk, byte addyMytree, byte colorInfo) {
        this.idTree = (short) 0;
        this.timeLive = 0;
        this.currentTime = 0L;
        this.ID = (short) idFarm;
        this.idTree = (short) id;
        this.x = (short) x;
        this.y = (short) y;
        this.catagory = (byte) 10;
        this.infoGround = info;
        this.timeLive = timeLive / 60;
        this.minutes = timeLive % 60;
        this.myTree = null;
        this.isBuyOk = isBuyOk;
        this.currentTime = System.currentTimeMillis();
        this.addyMytree = addyMytree;
        this.colorInfo = colorInfo;
        if (id > -1) {
            this.myTree = new MyTree(id, x + 16, y + 16 + addyMytree, idImg);
            Canvas.gameScr.actors.addElement(this.myTree);
        }
    }

    public void setInfo(int idFarm, int id, int x, int y, String info, String name, int idImgTree, int timeLive, boolean isBuyOk, byte addyMytree, byte colorInfo) {
        this.ID = (short) idFarm;
        this.idTree = (short) id;
        this.x = (short) x;
        this.y = (short) y;
        this.catagory = (byte) 10;
        this.infoGround = info;
        this.timeLive = timeLive / 60;
        this.minutes = timeLive % 60;
        this.isBuyOk = isBuyOk;
        this.currentTime = System.currentTimeMillis();
        this.addyMytree = addyMytree;
        this.colorInfo = colorInfo;
        if (id > -1) {
            if (this.myTree != null) {
                this.myTree.himg = 1;
                this.myTree.idTree = (short) id;
                this.myTree.idImg = (byte) idImgTree;
                return;
            }
            this.myTree = new MyTree(id, x + 16, y + 16 + addyMytree, idImgTree);
            Canvas.gameScr.actors.addElement(this.myTree);
            return;
        }
        if (this.myTree != null) {
            Canvas.gameScr.actors.removeElement(this.myTree);
            this.myTree = null;
        }
    }

    @Override
    public void paint(Graphics g) {
        if (Res.imgGround != null) {
            if (this.isBuyOk) {
                if (this.myTree == null) {
                    g.drawImage(Res.imgGround[0], this.x, this.y, 0);
                } else {
                    g.drawImage(Res.imgGround[2], this.x, this.y, 0);
                }
            } else {
                g.drawImage(Res.imgGround[1], this.x, this.y, 0);
            }
        }
        if (this.isFocus) {
            if (this.timeLive > -1 && this.myTree != null) {
                int w = Font.normalFont[this.colorInfo].getWidth(this.clockInfo);
                g.setColor(1593912);
                g.fillRect(((this.x + 16) - (w / 2)) - 3, this.y - (this.myTree == null ? 8 : this.myTree.himg + 15), w + 5, 14);
                g.setColor(16772608);
                g.drawRect(((this.x + 16) - (w / 2)) - 3, this.y - (this.myTree == null ? 8 : this.myTree.himg + 15), w + 5, 14);
                Font.normalFont[this.colorInfo].drawString(g, this.clockInfo, this.x + 16, this.y - (this.myTree == null ? 8 : this.myTree.himg + 15), 2);
            }
            if (PopupName.imgArrow != null) {
                PopupName.imgArrow.drawFrame(0, this.x + 16, (this.y - (this.myTree == null ? 8 : this.myTree.himg)) + (Canvas.gameTick % 8), 0, 33, g);
            }
        }
    }

    @Override
    public void update() {
        this.count = (byte) (((long) this.minutes) - ((System.currentTimeMillis() - this.currentTime) / 1000));
        if (this.count <= 0) {
            this.timeLive--;
            this.currentTime = System.currentTimeMillis();
            this.minutes = 59;
        }
        if (this.isFocus && this.timeLive > -1 && this.myTree != null) {
            this.clockInfo = new StringBuffer(String.valueOf(this.timeLive < 10 ? new StringBuffer("0").append(this.timeLive).toString() : new StringBuffer(String.valueOf(this.timeLive)).toString())).append(" : ").append(this.count < 10 ? new StringBuffer("0").append((int) this.count).toString() : new StringBuffer(String.valueOf((int) this.count)).toString()).toString();
        }
        super.update();
    }

    @Override
    public void setPosTo(short x, short y) {
    }
}
