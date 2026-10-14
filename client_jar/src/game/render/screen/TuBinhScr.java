package game.render.screen;

import game.model.Command;
import game.model.Cout;
import game.model.IAction;
import game.model.ImageIcon;
import game.model.Scroll;
import game.model.ScrollResult;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import network.Message;

public class TuBinhScr extends Screen {
    public static TuBinhScr me;
    public static int wCell;
    public static int hCell;
    public static int xDr;
    public static int yDr;
    public int index;
    public int xSh;
    public int ySh;
    public int wSh;
    public int hSh;
    public int indexTab;
    public int saveIndexTab;
    public Screen last;
    public boolean isShowText;
    Command cmdClose;
    Command cmdErr;
    Command cmdView;
    public static int hImg = 80;
    public static int wImg = 40;
    public static Vector allTuBinh = new Vector();
    public Scroll mScroll = new Scroll();
    public Scroll mScrollTab = new Scroll();
    public Scroll mScrollShowText = new Scroll();
    public int wTab = 80;
    public String[] arrShowText = {""};
    public String[] arraySubTile = {"Tab 1", "Tab 2", "Tab 3", "Tab 4"};
    public String tile = "Cổ vật";

    public static TuBinhScr gI() {
        if (me != null) {
            return me;
        }
        TuBinhScr tuBinhScr = new TuBinhScr();
        me = tuBinhScr;
        return tuBinhScr;
    }

    public void setData(Message msg) {
        allTuBinh = new Vector();
        int xBegin = xDr + 6;
        int yBegin = yDr + 55;
        try {
            int i = msg.reader().readByte();
            this.arraySubTile = new String[i];
            for (int i2 = 0; i2 < i; i2++) {
                this.arraySubTile[i2] = msg.reader().readUTF();
                byte size = msg.reader().readByte();
                Vector a = new Vector();
                for (int j = 0; j < size; j++) {
                    ObjTuBinh obj = new ObjTuBinh();
                    obj.name = msg.reader().readUTF();
                    obj.infoDetail = new StringBuffer(String.valueOf(obj.name)).append("|").append(msg.reader().readUTF()).toString();
                    String[] info = Util.split(msg.reader().readUTF(), "|");
                    obj.arrayInfo = new String[info.length + 1];
                    obj.arrayInfo[0] = obj.name;
                    for (int k = 0; k < info.length; k++) {
                        obj.arrayInfo[k + 1] = info[k];
                    }
                    obj.idImg = msg.reader().readByte();
                    obj.x = xBegin;
                    obj.y = yBegin + (j * (hImg + 3));
                    a.addElement(obj);
                }
                allTuBinh.addElement(a);
            }
        } catch (Exception e) {
        }
    }

    public void resetData() {
        this.mScroll.clear();
        this.mScrollTab.clear();
        this.mScrollShowText.clear();
        allTuBinh = new Vector();
        this.index = Canvas.isSmartPhone ? -1 : 0;
        this.indexTab = 0;
    }

    public TuBinhScr() {
        this.index = -1;
        wCell = Canvas.w - 10;
        hCell = (Canvas.h - Screen.ITEM_HEIGHT) - 10;
        if (wCell > 176) {
            wCell = 176;
        }
        if (hCell > 220) {
            hCell = 220;
        }
        if (Canvas.w >= 320 && Canvas.isSmartPhone) {
            wCell = 300;
        }
        xDr = (Canvas.w / 2) - (wCell / 2);
        yDr = ((Canvas.h - Screen.ITEM_HEIGHT) / 2) - (hCell / 2);
        if (yDr < 5) {
            yDr = 5;
        }
        if (xDr < 5) {
            xDr = 5;
        }
        this.wSh = wCell;
        if (Canvas.isSmartPhone && wCell == 300) {
            this.xSh = xDr + (wCell / 2);
            this.ySh = yDr + 55;
            this.wSh = (wCell / 2) - 8;
            this.hSh = hCell - 54;
        }
        Cout.println(new StringBuffer("H cell = ").append(hCell).append(" Hcanvas = ").append(Canvas.h).toString());
        this.cmdClose = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                if (TuBinhScr.this.isShowText) {
                    TuBinhScr.this.isShowText = false;
                    TuBinhScr.this.center = TuBinhScr.this.cmdView;
                    TuBinhScr.this.mScrollShowText.clear();
                    TuBinhScr.this.arrShowText = new String[]{""};
                    return;
                }
                TuBinhScr.this.last.switchToMe();
            }
        });
        this.cmdView = new Command(Canvas.isSmartPhone ? "" : "Xem", new IAction() {
            @Override
            public void perform() {
                TuBinhScr.this.doView();
            }
        });
        this.cmdErr = new Command("", new IAction() {
            @Override
            public void perform() {
            }
        });
        this.right = this.cmdClose;
        this.center = this.cmdView;
        if (!Canvas.isSmartPhone) {
            this.index = 0;
        }
        setData(null);
    }

    protected void doView() {
        if (allTuBinh.size() == 0 || this.saveIndexTab <= -1) {
            return;
        }
        Vector allTuBinh2 = (Vector) allTuBinh.elementAt(this.saveIndexTab);
        if (this.index < 0 || this.index > allTuBinh2.size() - 1) {
            return;
        }
        ObjTuBinh obj = (ObjTuBinh) allTuBinh2.elementAt(this.index);
        this.arrShowText = Util.split(obj.infoDetail, "|");
        if (!Canvas.isSmartPhone) {
            if (this.arrShowText.length == 1) {
                this.hSh = 35;
                this.ySh = (Canvas.h / 2) - (Font.normalFont[0].getHeight() / 2);
            } else {
                this.hSh = (this.arrShowText.length * (Screen.ITEM_HEIGHT - 3)) + 5;
                this.ySh = (Canvas.h / 2) - ((this.arrShowText.length * (Screen.ITEM_HEIGHT - 3)) / 2);
            }
            if (this.ySh < yDr) {
                this.ySh = yDr;
            }
            if (this.hSh > 150) {
                this.hSh = 150;
            }
            this.xSh = xDr;
        }
        this.isShowText = true;
        if (!Canvas.isSmartPhone) {
            this.center = this.cmdErr;
        }
    }

    public void paintCell(Graphics g) {
        g.setColor(25695);
        g.fillRect(this.xSh, this.ySh + (Canvas.isSmartPhone ? 0 : 10), this.wSh, this.hSh - 10);
        g.setColor(16774720);
        g.drawRect(this.xSh, this.ySh + (Canvas.isSmartPhone ? 0 : 10), this.wSh, this.hSh - 10);
    }

    public void paintShowText(Graphics g) {
        Canvas.resetTrans(g);
        if (wCell >= 300) {
            paintCell(g);
            if (!this.isShowText) {
                return;
            }
        } else if (!this.isShowText) {
            return;
        } else {
            paintCell(g);
        }
        this.mScrollShowText.setStyle(this.arrShowText.length, Screen.ITEM_HEIGHT, this.xSh, this.ySh, this.wSh - 2, this.hSh - 10, true, 0);
        this.mScrollShowText.setClip(g, this.xSh, this.ySh + (Canvas.isSmartPhone ? 2 : 10), this.wSh - 2, this.hSh - 15);
        for (int i = 0; i < this.arrShowText.length; i++) {
            Font.normalFont[0].drawString(g, this.arrShowText[i], this.xSh + 4, this.ySh + (i * (Screen.ITEM_HEIGHT - 3)) + (Canvas.isSmartPhone ? 6 : 14), 0);
        }
    }

    @Override
    public void paint(Graphics g) {
        if (this.last != null) {
            this.last.paint(g);
        }
        int w0 = (Canvas.isSmartPhone && wCell == 300) ? wCell / 2 : wCell;
        Res.paintDlgFull(g, xDr, yDr, wCell, hCell, 25, this.tile, false, 0);
        Canvas.resetTrans(g);
        this.mScrollTab.setStyle(this.arraySubTile.length, this.wTab + 2, xDr, yDr + 32, wCell - 2, 25, false, 0);
        this.mScrollTab.setClip(g, xDr + 2, yDr + 25, wCell - 4, 25);
        for (int i = 0; i < this.arraySubTile.length; i++) {
            if (this.indexTab == i) {
                g.setColor(6513507);
                g.fillRect(xDr + (this.wTab * i), yDr + 26, this.wTab, 25);
            }
            Font.normalFont[0].drawString(g, this.arraySubTile[i], xDr + (this.wTab * i) + (this.wTab / 2), yDr + 32, 2);
            g.setColor(14527502);
            g.fillRect(xDr + (this.wTab * i), yDr + 25, 1, 25);
        }
        g.fillRect(xDr + (this.wTab * this.arraySubTile.length), yDr + 25, 1, 25);
        Canvas.resetTrans(g);
        if (allTuBinh.size() > 0) {
            if (this.saveIndexTab != this.indexTab && this.indexTab > -1 && this.indexTab < allTuBinh.size()) {
                this.saveIndexTab = this.indexTab;
            }
            Vector allTuBinh2 = (Vector) allTuBinh.elementAt(this.saveIndexTab);
            this.mScroll.setStyle(allTuBinh2.size(), hImg + 2, xDr, yDr + 55, w0 - 2, hCell - 65, true, 0);
            this.mScroll.setClip(g, xDr, yDr + 52, w0 - 2, hCell - 55);
            for (int i2 = 0; i2 < allTuBinh2.size(); i2++) {
                ObjTuBinh obj = (ObjTuBinh) allTuBinh2.elementAt(i2);
                if (obj != null) {
                    if (this.index == i2) {
                        g.setColor(6513507);
                        g.fillRect(xDr + 4, obj.y, w0, hImg);
                    }
                    obj.paint(g);
                }
            }
        }
        paintShowText(g);
        super.paint(g);
    }

    @Override
    public void update() {
        if (this.last != null) {
            this.last.update();
        }
        if (Canvas.currentDialog == null) {
            if (Canvas.isSmartPhone) {
                ScrollResult r = this.mScroll.updateKey();
                if (r.isDowning || r.isFinish) {
                    if (this.isShowText) {
                        this.right.action.perform();
                    }
                    this.index = r.selected;
                    if (!this.mScroll.pointerIsDowning && !this.isShowText && this.center != null) {
                        this.center.action.perform();
                    }
                }
                this.mScroll.updatecm();
                ScrollResult r0 = this.mScrollTab.updateKey();
                if (r0.isDowning || r0.isFinish) {
                    this.indexTab = r0.selected;
                    this.mScrollTab.moveTo(this.indexTab * (this.wTab + 2));
                    if (this.right != null && this.isShowText) {
                        this.right.action.perform();
                    }
                }
                this.mScrollTab.updatecm();
                this.mScrollShowText.updateKey();
                this.mScrollShowText.updatecm();
            } else if (!this.isShowText) {
                ScrollResult r2 = this.mScroll.updateKey();
                if (r2.isDowning || r2.isFinish) {
                    this.index = r2.selected;
                }
                this.mScroll.updatecm();
                ScrollResult r1 = this.mScrollTab.updateKey();
                if (r1.isDowning || r1.isFinish) {
                    this.indexTab = r1.selected;
                    this.mScrollTab.moveTo(this.indexTab * (this.wTab + 2));
                }
                this.mScrollTab.updatecm();
            } else {
                this.mScrollShowText.updateKey();
                this.mScrollShowText.updatecm();
            }
        }
        super.update();
    }

    @Override
    public void updateKey() {
        if (this.isShowText) {
            if (Canvas.keyPressed[2]) {
                Canvas.keyPressed[2] = false;
                this.mScrollShowText.cmtoY -= 50;
                if (this.mScrollShowText.cmtoY < 0) {
                    this.mScrollShowText.cmtoY = 0;
                }
            } else if (Canvas.keyPressed[8]) {
                Canvas.keyPressed[8] = false;
                this.mScrollShowText.cmtoY += 50;
                if (this.mScrollShowText.cmtoY > this.mScrollShowText.cmyLim) {
                    this.mScrollShowText.cmtoY = this.mScrollShowText.cmyLim;
                }
            }
        } else {
            if (this.indexTab > -1 && this.indexTab < allTuBinh.size()) {
                Vector allTuBinh2 = (Vector) allTuBinh.elementAt(this.indexTab);
                if (Canvas.keyPressed[2]) {
                    Canvas.keyPressed[2] = false;
                    this.index--;
                    if (this.index < 0) {
                        this.index = allTuBinh2.size() - 1;
                    }
                    this.mScroll.moveTo(this.index * (hImg + 2));
                } else if (Canvas.keyPressed[8]) {
                    Canvas.keyPressed[8] = false;
                    this.index++;
                    if (this.index > allTuBinh2.size() - 1) {
                        this.index = 0;
                    }
                    this.mScroll.moveTo(this.index * (hImg + 2));
                }
            }
            if (Canvas.keyPressed[4]) {
                Canvas.keyPressed[4] = false;
                this.indexTab--;
                if (this.indexTab < 0) {
                    this.indexTab = this.arraySubTile.length - 1;
                }
                this.mScrollTab.moveTo(this.indexTab * (this.wTab + 2));
            } else if (Canvas.keyPressed[6]) {
                Canvas.keyPressed[6] = false;
                this.indexTab++;
                if (this.indexTab > this.arraySubTile.length - 1) {
                    this.indexTab = 0;
                }
                this.mScrollTab.moveTo(this.indexTab * (this.wTab + 2));
            }
        }
        super.updateKey();
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
    }

    @Override
    public void switchToMe(Screen lastSCR) {
        this.last = lastSCR;
        super.switchToMe(lastSCR);
    }

    public class ObjTuBinh {
        public int x;
        public int y;
        public short idImg;
        public String name = "";
        public String[] arrayInfo = {""};
        public String infoDetail = "";

        public ObjTuBinh() {
        }

        public void paint(Graphics g) {
            int w0 = 0;
            g.setColor(8023616);
            g.fillRect(this.x, this.y + TuBinhScr.hImg + 1, TuBinhScr.wCell, 1);
            ImageIcon img = GameData.getImgIcon((short) (this.idImg + 8500));
            if (img != null && img.img != null) {
                w0 = img.img.getWidth() + 2;
            }
            GameData.paintIconTuBinh(g, (short) (this.idImg + 8500), this.x, this.y, 0);
            for (int i = 0; i < this.arrayInfo.length; i++) {
                Font.normalFont[0].drawString(g, this.arrayInfo[i], this.x + w0, this.y + (i * (Screen.ITEM_HEIGHT - 3)), 0);
            }
        }
    }
}
