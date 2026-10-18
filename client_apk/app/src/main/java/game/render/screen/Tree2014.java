package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Command;
import game.model.IAction;
import game.model.Scroll;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;
import network.Message;

public class Tree2014 extends Screen {
    public static Tree2014 me;
    Command cmdClose;
    private Command cmdErr;
    Command cmdMenu;
    Command cmdTangToc;
    Command cmdThuHoach;
    Command cmdTuoi;
    Command cmdView;
    private int hCell;
    private int hSh;
    private int index;
    private boolean isShowText;
    public Screen last;
    private int wCell;
    private int wSh;
    private int xDr;
    private int xSh;
    int[] xTrai;
    private int yDr;
    private int ySh;
    int[] yTrai;
    public String name = "";
    private int wTree = 76;
    public Scroll mScrollShowText = new Scroll();
    public Vector allTree = new Vector();
    public String[] arrShowText = {""};
    public String str = null;

    public static Tree2014 gI() {
        Tree2014 tree2014 = me;
        if (tree2014 != null) {
            return tree2014;
        }
        Tree2014 tree2015 = new Tree2014();
        me = tree2015;
        return tree2015;
    }

    public Tree2014() {
        int i;
        resetData();
        this.cmdClose = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                if (Tree2014.this.isShowText && !GCanvas.isSmartPhone) {
                    Tree2014.this.isShowText = false;
                    Tree2014 tree2014 = Tree2014.this;
                    tree2014.center = tree2014.cmdView;
                    Tree2014 tree2015 = Tree2014.this;
                    tree2015.left = tree2015.cmdMenu;
                    Tree2014.this.mScrollShowText.clear();
                    return;
                }
                Tree2014.this.last.switchToMe();
            }
        });
        this.cmdThuHoach = new Command("Thu hoạch", new IAction() {
            @Override
            public void perform() {
                Tree2014.this.doThuhoach();
            }
        });
        this.cmdTangToc = new Command("Tăng tốc", new IAction() {
            @Override
            public void perform() {
                Tree2014.this.doTuoi();
            }
        });
        this.cmdView = new Command(GCanvas.isSmartPhone ? "" : "Xem", new IAction() {
            @Override
            public void perform() {
                Tree2014.this.doView();
            }
        });
        this.cmdMenu = new Command("Menu", new IAction() {
            @Override
            public void perform() {
                Vector vector = new Vector();
                if (Tree2014.this.allTree.size() > 0) {
                    ObjTree objTree = (ObjTree) Tree2014.this.allTree.elementAt(Tree2014.this.index);
                    if (objTree.status == 0) {
                        vector.addElement(Tree2014.this.cmdTangToc);
                    } else if (objTree.status == 2 || objTree.status == 3) {
                        vector.addElement(Tree2014.this.cmdThuHoach);
                    }
                    GCanvas.menu.startAt(vector, 0);
                }
            }
        });
        this.cmdErr = new Command("", new IAction() {
            @Override
            public void perform() {
            }
        });
        this.right = this.cmdClose;
        this.center = this.cmdView;
        this.left = this.cmdMenu;
        this.wCell = 300;
        this.hCell = 180;
        if (!GCanvas.isSmartPhone) {
            this.wCell = 180;
        }
        this.xDr = (GCanvas.w / 2) - (this.wCell / 2);
        this.yDr = ((GCanvas.h / 2) - (this.hCell / 2)) - 5;
        this.wSh = 140;
        if (GCanvas.isSmartPhone && (i = this.wCell) == 300) {
            this.xSh = this.xDr + (i / 2);
            this.ySh = this.yDr + 25;
            this.wSh = (i / 2) - 6;
            this.hSh = this.hCell - 36;
        }
    }

    protected void doView() {
        int i = this.index;
        if (i < 0 || i > this.allTree.size() - 1) {
            return;
        }
        GameService.gI().sendInfoFruit(this.index, 1);
    }

    public void setShowText(String str) {
        int i = GCanvas.isSmartPhone ? this.wSh - 30 : 120;
        String[] strArrSplit = Util.split(str, "|");
        int length = 0;
        for (String str2 : strArrSplit) {
            length += Font.normalFont[0].splitFontBStrInLine(str2, i).length;
        }
        String[] strArr = new String[length];
        int i2 = 0;
        for (String str3 : strArrSplit) {
            for (String str4 : Font.normalFont[0].splitFontBStrInLine(str3, i)) {
                strArr[i2] = str4;
                i2++;
            }
        }
        this.arrShowText = strArr;
        if (!GCanvas.isSmartPhone) {
            String[] strArr2 = this.arrShowText;
            if (strArr2.length == 1) {
                this.hSh = 35;
                this.ySh = (GCanvas.h / 2) - (Font.normalFont[0].getHeight() / 2);
            } else {
                this.hSh = strArr2.length * 24;
                this.ySh = (GCanvas.h / 2) - ((this.arrShowText.length * 24) / 2);
            }
            if (this.ySh < 5) {
                this.ySh = 5;
            }
            if (this.hSh > 150) {
                this.hSh = 150;
            }
            this.xSh = (GCanvas.w / 2) - 75;
        }
        this.isShowText = true;
        if (GCanvas.isSmartPhone) {
            return;
        }
        this.center = this.cmdErr;
        this.left = this.cmdErr;
    }

    protected void doTuoi() {
        GameService.gI().sendInfoFruit(this.index, 2);
    }

    protected void doThuhoach() {
        GameService.gI().sendInfoFruit(this.index, 3);
    }

    public void resetData() {
        this.xTrai = new int[8];
        this.yTrai = new int[8];
        int i = (GCanvas.w / 2) - 75;
        int i2 = GCanvas.h / 2;
        int[] iArr = this.xTrai;
        iArr[0] = 10;
        int[] iArr2 = this.yTrai;
        iArr2[0] = 72;
        iArr[1] = 43;
        iArr2[1] = 62;
        iArr[2] = 20;
        iArr2[2] = 28;
        iArr[3] = 52;
        iArr2[3] = 17;
        iArr[4] = 70;
        iArr2[4] = 56;
        iArr[5] = 82;
        iArr2[5] = 7;
        iArr[6] = 106;
        iArr2[6] = 22;
        iArr[7] = 112;
        iArr2[7] = 64;
        int i3 = i2 - 63;
        if (!GCanvas.isSmartPhone) {
            this.wTree = 0;
        }
        int i4 = 0;
        while (true) {
            int[] iArr3 = this.xTrai;
            if (i4 < iArr3.length) {
                iArr3[i4] = iArr3[i4] + (i - this.wTree);
                int[] iArr4 = this.yTrai;
                iArr4[i4] = iArr4[i4] + i3;
                i4++;
            } else {
                this.mScrollShowText.clear();
                this.allTree = new Vector();
                this.index = 0;
                this.arrShowText = new String[]{""};
                return;
            }
        }
    }

    public void setInfoTree(Message message) {
        try {
            byte b = message.reader().readByte();
            if (b != 0) {
                if (b == 1) {
                    ObjTree objTree = (ObjTree) this.allTree.elementAt(message.reader().readByte());
                    objTree.info = message.reader().readUTF();
                    setShowText(objTree.info);
                    return;
                }
                return;
            }
            resetData();
            this.allTree = new Vector();
            this.name = message.reader().readUTF();
            byte b2 = message.reader().readByte();
            for (int i = 0; i < b2; i++) {
                String utf = message.reader().readUTF();
                byte b3 = message.reader().readByte();
                byte b4 = message.reader().readByte();
                ObjTree objTree2 = new ObjTree();
                objTree2.idIconImg = b3;
                objTree2.name = utf;
                objTree2.status = b4;
                objTree2.x = this.xTrai[i];
                objTree2.y = this.yTrai[i];
                this.allTree.addElement(objTree2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void paintCell(Graphics graphics) {
        graphics.setColor(-16751521);
        graphics.fillRect(this.xSh - 5, this.ySh + 10, this.wSh, this.hSh - 10);
        graphics.setColor(-2496);
        graphics.drawRect(this.xSh - 5, this.ySh + 10, this.wSh, this.hSh - 10);
        if (this.isShowText || this.str == null || !GCanvas.isSmartPhone) {
            return;
        }
        String[] strArrSplit = Util.split(this.str, "|");
        for (int i = 0; i < strArrSplit.length; i++) {
            Font.normalFont[0].drawString(graphics, strArrSplit[i], this.xSh, this.ySh + (i * 14) + 14, 0);
        }
    }

    public void paintShowText(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        if (this.wCell >= 300) {
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
        this.mScrollShowText.setClip(graphics, this.xSh - 3, this.ySh + 10, this.wSh - 2, this.hSh - 15);
        for (int i = 0; i < this.arrShowText.length; i++) {
            Font.normalFont[0].drawString(graphics, this.arrShowText[i], this.xSh, this.ySh + ((ITEM_HEIGHT - 3) * i) + 14, 0);
        }
    }

    public void paintNameTree(Graphics graphics, String str, int i, int i2) {
        String[] strArrSplit = Util.split(str, "|");
        int width = Font.normalFont[0].getWidth(strArrSplit[0]);
        int width2 = Font.normalFont[0].getWidth(strArrSplit[1]);
        if (width < width2) {
            width = width2;
        }
        graphics.setColor(-15183304);
        int i3 = i + 16;
        int i4 = (i3 - (width / 2)) - 3;
        int i5 = i2 - 16;
        int i6 = width + 5;
        graphics.fillRect(i4, i5, i6, 30);
        graphics.setColor(-4608);
        graphics.drawRect(i4, i5, i6, 30);
        Font.normalFont[0].drawString(graphics, strArrSplit[0], i3, i2 - 15, 2);
        Font.normalFont[0].drawString(graphics, strArrSplit[1], i3, i2, 2);
    }

    @Override
    public void paint(Graphics graphics) {
        String str;
        Screen screen = this.last;
        if (screen != null) {
            screen.paint(graphics);
        }
        graphics.setColor(0);
        graphics.setAlpha(180);
        graphics.fillRect(0, 0, GCanvas.w, GCanvas.h);
        Res.paintDlgFull(graphics, this.xDr, this.yDr, this.wCell, this.hCell, 0, this.name, false, 0);
        GameData.paintIconTuBinh(graphics, (short) 8214, this.xDr + (GCanvas.isSmartPhone ? this.wTree : this.wCell / 2), (GCanvas.h / 2) + 12, Graphics.VCENTER | Graphics.HCENTER);
        this.str = null;
        int i = 0;
        int i2 = 0;
        for (int i3 = 0; i3 < this.allTree.size(); i3++) {
            ObjTree objTree = (ObjTree) this.allTree.elementAt(i3);
            if (objTree != null) {
                if (this.index == i3) {
                    graphics.setColor(-16751521);
                    graphics.fillRect(objTree.x, objTree.y, 16, 30);
                    graphics.setColor(-2496);
                    graphics.drawRect(objTree.x, objTree.y, 16, 30);
                    i = objTree.x;
                    i2 = objTree.y;
                    if (objTree.status != 1) {
                        str = "";
                    } else {
                        str = "héo";
                    }
                    if (objTree.status == 0) {
                        str = "xanh";
                    }
                    if (objTree.status == 2) {
                        str = "chín";
                    }
                    this.str = objTree.name + "|Trạng thái: " + str;
                }
                objTree.paint(graphics);
            }
        }
        paintShowText(graphics);
        if (this.str != null && !GCanvas.isSmartPhone && !this.isShowText) {
            paintNameTree(graphics, this.str, i, i2);
        }
        super.paint(graphics);
    }

    @Override
    public void update() {
        Screen screen = this.last;
        if (screen != null) {
            screen.update();
        }
        if (GCanvas.currentDialog == null) {
            this.mScrollShowText.updateKey();
            this.mScrollShowText.updatecm();
        }
        super.update();
    }

    public void updateKeyShowText() {
        if (GCanvas.keyPressed[2]) {
            GCanvas.keyPressed[2] = false;
            this.mScrollShowText.cmtoY -= 50;
            if (this.mScrollShowText.cmtoY < 0) {
                this.mScrollShowText.cmtoY = 0;
                return;
            }
            return;
        }
        if (GCanvas.keyPressed[8]) {
            GCanvas.keyPressed[8] = false;
            this.mScrollShowText.cmtoY += 50;
            if (this.mScrollShowText.cmtoY > this.mScrollShowText.cmyLim) {
                Scroll scroll = this.mScrollShowText;
                scroll.cmtoY = scroll.cmyLim;
            }
        }
    }

    @Override
    public void updateKey() {
        if (this.isShowText && !GCanvas.isSmartPhone) {
            updateKeyShowText();
        } else {
            if (GCanvas.keyPressed[4]) {
                GCanvas.keyPressed[4] = false;
                int i = this.index - 1;
                this.index = i;
                if (i < 0) {
                    this.index = this.allTree.size() - 1;
                }
                if (GCanvas.isSmartPhone) {
                    this.isShowText = false;
                    this.mScrollShowText.clear();
                }
            }
            if (GCanvas.keyPressed[6]) {
                GCanvas.keyPressed[6] = false;
                int i2 = this.index + 1;
                this.index = i2;
                if (i2 > this.allTree.size() - 1) {
                    this.index = 0;
                }
                if (GCanvas.isSmartPhone) {
                    this.isShowText = false;
                    this.mScrollShowText.clear();
                }
            }
            if (GCanvas.isSmartPhone) {
                updateKeyShowText();
            }
        }
        pointer();
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

    public void pointer() {
        if ((GCanvas.isSmartPhone || !this.isShowText) && GCanvas.isPointerClick) {
            for (int i = 0; i < this.xTrai.length; i++) {
                if (GCanvas.px >= this.xTrai[i] && GCanvas.px <= this.xTrai[i] + 15 && GCanvas.py >= this.yTrai[i] && GCanvas.py <= this.yTrai[i] + 30) {
                    GCanvas.isPointerClick = false;
                    this.isShowText = false;
                    this.mScrollShowText.clear();
                    this.index = i;
                    if (this.isShowText || this.center == null) {
                        return;
                    }
                    this.center.action.perform();
                    return;
                }
            }
        }
    }

    public class ObjTree {
        public int idIconImg;
        public int x;
        public int y;
        public byte status = 0;
        public String name = "";
        public String info = "";

        public ObjTree() {
        }

        public void paint(Graphics graphics) {
            GameData.paintIconTuBinh(graphics, (short) (this.idIconImg + 8200), this.x, this.y, 0);
        }
    }
}
