package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Command;
import game.model.Cout;
import game.model.IAction;
import game.model.ImageIcon;
import game.model.Scroll;
import game.model.ScrollResult;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;
import network.Message;

public class TuBinhScr extends Screen {
    public static Vector allTuBinh = new Vector();
    public static int hCell = 0;
    public static int hImg = 80;
    public static TuBinhScr me = null;
    public static int wCell = 0;
    public static int wImg = 40;
    public static int xDr;
    public static int yDr;
    Command cmdClose;
    Command cmdErr;
    Command cmdView;
    public int hSh;
    public int index;
    public int indexTab;
    public boolean isShowText;
    public Screen last;
    public int saveIndexTab;
    public int wSh;
    public int xSh;
    public int ySh;
    public Scroll mScroll = new Scroll();
    public Scroll mScrollTab = new Scroll();
    public Scroll mScrollShowText = new Scroll();
    public int wTab = 80;
    public String[] arrShowText = {""};
    public String[] arraySubTile = {"Tab 1", "Tab 2", "Tab 3", "Tab 4"};
    public String tile = "Cổ vật";

    public static TuBinhScr gI() {
        TuBinhScr tuBinhScr = me;
        if (tuBinhScr != null) {
            return tuBinhScr;
        }
        TuBinhScr tuBinhScr2 = new TuBinhScr();
        me = tuBinhScr2;
        return tuBinhScr2;
    }

    public void setData(Message message) {
        allTuBinh = new Vector();
        int i = xDr + 6;
        int i2 = yDr + 55;
        try {
            int i3 = message.reader().readByte();
            this.arraySubTile = new String[i3];
            for (int i4 = 0; i4 < i3; i4++) {
                this.arraySubTile[i4] = message.reader().readUTF();
                byte b = message.reader().readByte();
                Vector vector = new Vector();
                for (int i5 = 0; i5 < b; i5++) {
                    ObjTuBinh objTuBinh = new ObjTuBinh();
                    objTuBinh.name = message.reader().readUTF();
                    objTuBinh.infoDetail = objTuBinh.name + "|" + message.reader().readUTF();
                    String[] strArrSplit = Util.split(message.reader().readUTF(), "|");
                    objTuBinh.arrayInfo = new String[strArrSplit.length + 1];
                    objTuBinh.arrayInfo[0] = objTuBinh.name;
                    int i6 = 0;
                    while (i6 < strArrSplit.length) {
                        int i7 = i6 + 1;
                        objTuBinh.arrayInfo[i7] = strArrSplit[i6];
                        i6 = i7;
                    }
                    objTuBinh.idImg = message.reader().readByte();
                    objTuBinh.x = i;
                    objTuBinh.y = ((hImg + 3) * i5) + i2;
                    vector.addElement(objTuBinh);
                }
                allTuBinh.addElement(vector);
            }
        } catch (Exception unused) {
        }
    }

    public void resetData() {
        this.mScroll.clear();
        this.mScrollTab.clear();
        this.mScrollShowText.clear();
        allTuBinh = new Vector();
        this.index = GCanvas.isSmartPhone ? -1 : 0;
        this.indexTab = 0;
    }

    public TuBinhScr() {
        int i;
        this.index = -1;
        wCell = GCanvas.w - 10;
        int i2 = (GCanvas.h - ITEM_HEIGHT) - 10;
        hCell = i2;
        if (wCell > 176) {
            wCell = 176;
        }
        if (i2 > 200) {
            hCell = 200;
        }
        if (GCanvas.w >= 320 && GCanvas.isSmartPhone) {
            wCell = 300;
        }
        xDr = (GCanvas.w / 2) - (wCell / 2);
        int i3 = (((GCanvas.h - ITEM_HEIGHT) / 2) - (hCell / 2)) - 5;
        yDr = i3;
        if (i3 < 5) {
            yDr = 5;
        }
        if (xDr < 5) {
            xDr = 5;
        }
        this.wSh = wCell;
        if (GCanvas.isSmartPhone && (i = wCell) == 300) {
            this.xSh = xDr + (i / 2);
            this.ySh = yDr + 55;
            this.wSh = (i / 2) - 8;
            this.hSh = hCell - 54;
        }
        Cout.println("H cell = " + hCell + " Hcanvas = " + GCanvas.h);
        this.cmdClose = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                if (TuBinhScr.this.isShowText) {
                    TuBinhScr.this.isShowText = false;
                    TuBinhScr tuBinhScr = TuBinhScr.this;
                    tuBinhScr.center = tuBinhScr.cmdView;
                    TuBinhScr.this.mScrollShowText.clear();
                    TuBinhScr.this.arrShowText = new String[]{""};
                    return;
                }
                TuBinhScr.this.last.switchToMe();
            }
        });
        this.cmdView = new Command(GCanvas.isSmartPhone ? "" : "Xem", new IAction() {
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
        if (!GCanvas.isSmartPhone) {
            this.index = 0;
        }
        setData(null);
    }

    protected void doView() {
        int i;
        if (allTuBinh.size() == 0 || (i = this.saveIndexTab) <= -1) {
            return;
        }
        Vector vector = (Vector) allTuBinh.elementAt(i);
        int i2 = this.index;
        if (i2 < 0 || i2 > vector.size() - 1) {
            return;
        }
        this.arrShowText = Util.split(((ObjTuBinh) vector.elementAt(this.index)).infoDetail, "|");
        if (!GCanvas.isSmartPhone) {
            String[] strArr = this.arrShowText;
            if (strArr.length == 1) {
                this.hSh = 35;
                this.ySh = (GCanvas.h / 2) - (Font.normalFont[0].getHeight() / 2);
            } else {
                this.hSh = (strArr.length * (ITEM_HEIGHT - 3)) + 5;
                this.ySh = (GCanvas.h / 2) - ((this.arrShowText.length * (ITEM_HEIGHT - 3)) / 2);
            }
            int i3 = this.ySh;
            int i4 = yDr;
            if (i3 < i4) {
                this.ySh = i4;
            }
            if (this.hSh > 150) {
                this.hSh = 150;
            }
            this.xSh = xDr;
        }
        this.isShowText = true;
        if (GCanvas.isSmartPhone) {
            return;
        }
        this.center = this.cmdErr;
    }

    public void paintCell(Graphics graphics) {
        graphics.setColor(-16751521);
        graphics.fillRect(this.xSh, this.ySh + (GCanvas.isSmartPhone ? 0 : 10), this.wSh, this.hSh - 10);
        graphics.setColor(-2496);
        graphics.drawRect(this.xSh, this.ySh + (GCanvas.isSmartPhone ? 0 : 10), this.wSh, this.hSh - 10);
    }

    public void paintShowText(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        if (wCell >= 300) {
            paintCell(graphics);
            if (!this.isShowText) {
                return;
            }
        } else if (!this.isShowText) {
            return;
        } else {
            paintCell(graphics);
        }
        this.mScrollShowText.setStyle(this.arrShowText.length, ITEM_HEIGHT, this.xSh, this.ySh, this.wSh - 2, this.hSh - 10, true, 0);
        this.mScrollShowText.setClip(graphics, this.xSh, this.ySh + (GCanvas.isSmartPhone ? 2 : 10), this.wSh - 2, this.hSh - 15);
        for (int i = 0; i < this.arrShowText.length; i++) {
            Font.normalFont[0].drawString(graphics, this.arrShowText[i], this.xSh + 4, this.ySh + ((ITEM_HEIGHT - 3) * i) + (GCanvas.isSmartPhone ? 6 : 14), 0);
        }
    }

    @Override
    public void paint(Graphics graphics) {
        int i;
        Screen screen = this.last;
        if (screen != null) {
            screen.paint(graphics);
        }
        graphics.setColor(0);
        graphics.setAlpha(180);
        graphics.fillRect(0, 0, GCanvas.w, GCanvas.h);
        int i2 = (GCanvas.isSmartPhone && (i = wCell) == 300) ? i / 2 : wCell;
        Res.paintDlgFull(graphics, xDr, yDr, wCell, hCell, 25, this.tile, false, 0);
        GCanvas.resetTrans(graphics);
        graphics.ClipRec(xDr + 2, yDr + 25, wCell - 4, 25);
        this.mScrollTab.setStyle(this.arraySubTile.length, this.wTab + 2, xDr, yDr + 22, wCell - 2, 25, false, 0);
        this.mScrollTab.setClip(graphics, xDr + 2, yDr + 25, wCell - 4, 25);
        for (int i3 = 0; i3 < this.arraySubTile.length; i3++) {
            if (this.indexTab == i3) {
                graphics.setColor(-10263709);
                int i4 = xDr;
                int i5 = this.wTab;
                graphics.fillRect(i4 + (i5 * i3), yDr + 26, i5, 25);
            }
            Font font = Font.normalFont[0];
            String str = this.arraySubTile[i3];
            int i6 = xDr;
            int i7 = this.wTab;
            font.drawString(graphics, str, (i7 / 2) + i6 + (i7 * i3), yDr + 32, 2);
            graphics.setColor(-2249714);
            graphics.fillRect(xDr + (this.wTab * i3), yDr + 25, 1, 25);
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        graphics.fillRect(xDr + (this.wTab * this.arraySubTile.length), yDr + 25, 1, 25);
        GCanvas.resetTrans(graphics);
        if (allTuBinh.size() > 0) {
            int i8 = i2 - 2;
            graphics.ClipRec(xDr, yDr + 52, i8, hCell - 55);
            int i9 = this.saveIndexTab;
            int i10 = this.indexTab;
            if (i9 != i10 && i10 > -1 && i10 < allTuBinh.size()) {
                this.saveIndexTab = this.indexTab;
            }
            Vector vector = (Vector) allTuBinh.elementAt(this.saveIndexTab);
            this.mScroll.setStyle(vector.size(), hImg + 2, xDr, yDr + 55, i8, hCell - 65, true, 0);
            this.mScroll.setClip(graphics, xDr, yDr + 52, i8, hCell - 55);
            for (int i11 = 0; i11 < vector.size(); i11++) {
                ObjTuBinh objTuBinh = (ObjTuBinh) vector.elementAt(i11);
                if (objTuBinh != null) {
                    if (this.index == i11) {
                        graphics.setColor(-10263709);
                        graphics.fillRect(xDr + 4, objTuBinh.y, i2, hImg);
                    }
                    objTuBinh.paint(graphics);
                }
            }
            Graphics.resetTransAndroid(graphics);
            graphics.restoreCanvas();
        }
        paintShowText(graphics);
        super.paint(graphics);
    }

    @Override
    public void update() {
        Screen screen = this.last;
        if (screen != null) {
            screen.update();
        }
        if (GCanvas.currentDialog == null) {
            if (GCanvas.isSmartPhone) {
                ScrollResult scrollResultUpdateKey = this.mScroll.updateKey();
                if (scrollResultUpdateKey.isDowning || scrollResultUpdateKey.isFinish) {
                    if (this.isShowText) {
                        this.right.action.perform();
                    }
                    int i = scrollResultUpdateKey.selected;
                    this.index = i;
                    this.mScroll.moveTo(i * (hImg + 2));
                    if (!this.mScroll.pointerIsDowning && !this.isShowText && this.center != null) {
                        this.center.action.perform();
                    }
                }
                this.mScroll.updatecm();
                ScrollResult scrollResultUpdateKey2 = this.mScrollTab.updateKey();
                if (scrollResultUpdateKey2.isDowning || scrollResultUpdateKey2.isFinish) {
                    int i2 = scrollResultUpdateKey2.selected;
                    this.indexTab = i2;
                    this.mScrollTab.moveTo(i2 * (this.wTab + 2));
                    if (this.right != null && this.isShowText) {
                        this.right.action.perform();
                    }
                }
                this.mScrollTab.updatecm();
                this.mScrollShowText.updateKey();
                this.mScrollShowText.updatecm();
            } else if (!this.isShowText) {
                ScrollResult scrollResultUpdateKey3 = this.mScroll.updateKey();
                if (scrollResultUpdateKey3.isDowning || scrollResultUpdateKey3.isFinish) {
                    this.index = scrollResultUpdateKey3.selected;
                }
                this.mScroll.updatecm();
                ScrollResult scrollResultUpdateKey4 = this.mScrollTab.updateKey();
                if (scrollResultUpdateKey4.isDowning || scrollResultUpdateKey4.isFinish) {
                    int i3 = scrollResultUpdateKey4.selected;
                    this.indexTab = i3;
                    this.mScrollTab.moveTo(i3 * (this.wTab + 2));
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
            if (GCanvas.keyPressed[2]) {
                GCanvas.keyPressed[2] = false;
                this.mScrollShowText.cmtoY -= 50;
                if (this.mScrollShowText.cmtoY < 0) {
                    this.mScrollShowText.cmtoY = 0;
                }
            } else if (GCanvas.keyPressed[8]) {
                GCanvas.keyPressed[8] = false;
                this.mScrollShowText.cmtoY += 50;
                if (this.mScrollShowText.cmtoY > this.mScrollShowText.cmyLim) {
                    Scroll scroll = this.mScrollShowText;
                    scroll.cmtoY = scroll.cmyLim;
                }
            }
        } else {
            int i = this.indexTab;
            if (i > -1 && i < allTuBinh.size()) {
                Vector vector = (Vector) allTuBinh.elementAt(this.indexTab);
                if (GCanvas.keyPressed[2]) {
                    GCanvas.keyPressed[2] = false;
                    int i2 = this.index - 1;
                    this.index = i2;
                    if (i2 < 0) {
                        this.index = vector.size() - 1;
                    }
                    this.mScroll.moveTo(this.index * (hImg + 2));
                } else if (GCanvas.keyPressed[8]) {
                    GCanvas.keyPressed[8] = false;
                    int i3 = this.index + 1;
                    this.index = i3;
                    if (i3 > vector.size() - 1) {
                        this.index = 0;
                    }
                    this.mScroll.moveTo(this.index * (hImg + 2));
                }
            }
            if (GCanvas.keyPressed[4]) {
                GCanvas.keyPressed[4] = false;
                int i4 = this.indexTab - 1;
                this.indexTab = i4;
                if (i4 < 0) {
                    this.indexTab = this.arraySubTile.length - 1;
                }
                this.mScrollTab.moveTo(this.indexTab * (this.wTab + 2));
            } else if (GCanvas.keyPressed[6]) {
                GCanvas.keyPressed[6] = false;
                int i5 = this.indexTab + 1;
                this.indexTab = i5;
                if (i5 > this.arraySubTile.length - 1) {
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
    public void switchToMe(Screen screen) {
        this.last = screen;
        super.switchToMe(screen);
    }

    public class ObjTuBinh {
        public short idImg;
        public int x;
        public int y;
        public String name = "";
        public String[] arrayInfo = {""};
        public String infoDetail = "";

        public ObjTuBinh() {
        }

        public void paint(Graphics graphics) {
            graphics.setColor(-8753600);
            graphics.fillRect(this.x, this.y + TuBinhScr.hImg + 1, TuBinhScr.wCell, 1);
            ImageIcon imgIcon = GameData.getImgIcon((short) (this.idImg + 8500));
            int width = (imgIcon == null || imgIcon.img == null) ? 0 : imgIcon.img.getWidth() + 2;
            GameData.paintIconTuBinh(graphics, (short) (this.idImg + 8500), this.x, this.y, 0);
            for (int i = 0; i < this.arrayInfo.length; i++) {
                Font.normalFont[0].drawString(graphics, this.arrayInfo[i], this.x + width, this.y + ((Screen.ITEM_HEIGHT - 3) * i), 0);
            }
        }
    }
}
