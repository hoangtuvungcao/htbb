package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.model.Scroll;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import network.Message;

public class Tree2014 extends Screen {
    /** 31/10/2026: đang chạy updateKey không chạm thô (ManCu - vùng chạm kiểu chung đã xử lý) */
    boolean boc_updateKey;

    /** 31/10/2026: đang chạy update không chạm thô (ManCu - vùng chạm kiểu chung đã xử lý) */
    boolean boc_update;

    public static Tree2014 me;
    public Screen last;
    int[] xTrai;
    int[] yTrai;
    private int index;
    private int xSh;
    private int ySh;
    private int wSh;
    private int hSh;
    private int wCell;
    private int hCell;
    private int xDr;
    private int yDr;
    Command cmdView;
    Command cmdTangToc;
    Command cmdThuHoach;
    Command cmdClose;
    Command cmdMenu;
    private boolean isShowText;
    private Command cmdErr;
    public String name = "";
    private int wTree = 76;
    public Scroll mScrollShowText = new Scroll();
    public Vector allTree = new Vector();
    public String[] arrShowText = {""};
    public String str = null;

    public static Tree2014 gI() {
        if (me != null) {
            return me;
        }
        Tree2014 tree2014 = new Tree2014();
        me = tree2014;
        return tree2014;
    }

    public Tree2014() {
        resetData();
        this.cmdClose = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                if (Tree2014.this.isShowText && !Canvas.isSmartPhone) {
                    Tree2014.this.isShowText = false;
                    Tree2014.this.center = Tree2014.this.cmdView;
                    Tree2014.this.left = Tree2014.this.cmdMenu;
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
        this.cmdView = new Command(Canvas.isSmartPhone ? "" : "Xem", new IAction() {
            @Override
            public void perform() {
                Tree2014.this.doView();
            }
        });
        this.cmdMenu = new Command("Menu", new IAction() {
            @Override
            public void perform() {
                Vector mn = new Vector();
                if (Tree2014.this.allTree.size() > 0) {
                    ObjTree obj = (ObjTree) Tree2014.this.allTree.elementAt(Tree2014.this.index);
                    if (obj.status == 0) {
                        mn.addElement(Tree2014.this.cmdTangToc);
                    } else if (obj.status == 2 || obj.status == 3) {
                        mn.addElement(Tree2014.this.cmdThuHoach);
                    }
                    Canvas.menu.startAt(mn, 0);
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
        if (!Canvas.isSmartPhone || Canvas.w < 308) {
            // pokemon: màn hẹp (176/240/320) - bố cục một cột, không tràn mép
            this.wCell = Math.min(180, Canvas.w - 8);
        }
        if (this.hCell > Canvas.h - 40) {
            this.hCell = Canvas.h - 40;
        }
        this.xDr = (Canvas.w / 2) - (this.wCell / 2);
        this.yDr = (Canvas.h / 2) - (this.hCell / 2);
        this.wSh = 140;
        if (Canvas.isSmartPhone && this.wCell == 300) {
            this.xSh = this.xDr + (this.wCell / 2);
            this.ySh = this.yDr + 25;
            this.wSh = (this.wCell / 2) - 6;
            this.hSh = this.hCell - 36;
        }
    }

    protected void doView() {
        if (this.index < 0 || this.index > this.allTree.size() - 1) {
            return;
        }
        GameService.gI().sendInfoFruit(this.index, 1);
    }

    public void setShowText(String info) {
        int w0 = Canvas.isSmartPhone ? this.wSh - 16 : 120;
        String[] tempt = Util.split(info, "|");
        int total = 0;
        int cout = 0;
        for (String str : tempt) {
            total += Font.normalFont[0].splitFontBStrInLine(str, w0).length;
        }
        String[] result = new String[total];
        for (String str2 : tempt) {
            String[] t = Font.normalFont[0].splitFontBStrInLine(str2, w0);
            for (String str3 : t) {
                result[cout] = str3;
                cout++;
            }
        }
        this.arrShowText = result;
        if (!Canvas.isSmartPhone) {
            if (this.arrShowText.length == 1) {
                this.hSh = 35;
                this.ySh = (Canvas.h / 2) - (Font.normalFont[0].getHeight() / 2);
            } else {
                this.hSh = (this.arrShowText.length * (Screen.ITEM_HEIGHT - 3)) + 5;
                this.ySh = (Canvas.h / 2) - ((this.arrShowText.length * (Screen.ITEM_HEIGHT - 3)) / 2);
            }
            if (this.ySh < 5) {
                this.ySh = 5;
            }
            if (this.hSh > 150) {
                this.hSh = 150;
            }
            this.xSh = (Canvas.w / 2) - 75;
        }
        this.isShowText = true;
        if (!Canvas.isSmartPhone) {
            this.center = this.cmdErr;
            this.left = this.cmdErr;
        }
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
        int x0 = (Canvas.w / 2) - 75;
        int y0 = (Canvas.h / 2) - 75;
        this.xTrai[0] = 10;
        this.yTrai[0] = 72;
        this.xTrai[1] = 43;
        this.yTrai[1] = 62;
        this.xTrai[2] = 20;
        this.yTrai[2] = 28;
        this.xTrai[3] = 52;
        this.yTrai[3] = 17;
        this.xTrai[4] = 70;
        this.yTrai[4] = 56;
        this.xTrai[5] = 82;
        this.yTrai[5] = 7;
        this.xTrai[6] = 106;
        this.yTrai[6] = 22;
        this.xTrai[7] = 112;
        this.yTrai[7] = 64;
        int y1 = y0 + 12;
        if (!Canvas.isSmartPhone) {
            this.wTree = 0;
        }
        for (int i = 0; i < this.xTrai.length; i++) {
            int[] iArr = this.xTrai;
            int i2 = i;
            iArr[i2] = iArr[i2] + (x0 - this.wTree);
            int[] iArr2 = this.yTrai;
            int i3 = i;
            iArr2[i3] = iArr2[i3] + y1;
        }
        this.mScrollShowText.clear();
        this.allTree = new Vector();
        this.index = 0;
        this.arrShowText = new String[]{""};
    }

    public void setInfoTree(Message msg) {
        try {
            byte type = msg.reader().readByte();
            if (type == 0) {
                resetData();
                this.allTree = new Vector();
                this.name = msg.reader().readUTF();
                byte size = msg.reader().readByte();
                for (int i = 0; i < size; i++) {
                    String nameFruit = msg.reader().readUTF();
                    byte idIconImage = msg.reader().readByte();
                    byte status = msg.reader().readByte();
                    ObjTree obj = new ObjTree();
                    obj.idIconImg = idIconImage;
                    obj.name = nameFruit;
                    obj.status = status;
                    obj.x = this.xTrai[i];
                    obj.y = this.yTrai[i];
                    this.allTree.addElement(obj);
                }
                return;
            }
            if (type == 1) {
                byte index = msg.reader().readByte();
                ObjTree obj2 = (ObjTree) this.allTree.elementAt(index);
                obj2.info = msg.reader().readUTF();
                setShowText(obj2.info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void paintCell(Graphics g) {
        g.setColor(game.pokemon.GiaoDien.mauKhungCon());
        g.fillRect(this.xSh - 5, this.ySh + 10, this.wSh, this.hSh - 10);
        g.setColor(Font.nenSang ? game.pokemon.GiaoDien.XANH : game.pokemon.GiaoDien.VANG);
        g.drawRect(this.xSh - 5, this.ySh + 10, this.wSh, this.hSh - 10);
        if (!this.isShowText && this.str != null && Canvas.isSmartPhone) {
            String[] r = Util.split(this.str, "|");
            for (int i = 0; i < r.length; i++) {
                Font.normalFont[0].drawString(g, r[i], this.xSh, this.ySh + (i * 14) + 14, 0);
            }
        }
    }

    public void paintShowText(Graphics g) {
        Canvas.resetTrans(g);
        if (this.wCell >= 300) {
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
        this.mScrollShowText.setClip(g, this.xSh - 3, this.ySh + 10, this.wSh - 2, this.hSh - 15);
        for (int i = 0; i < this.arrShowText.length; i++) {
            Font.normalFont[0].drawString(g, this.arrShowText[i], this.xSh, this.ySh + (i * (Screen.ITEM_HEIGHT - 3)) + 14, 0);
        }
    }

    public void paintNameTree(Graphics g, String clockInfo, int x, int y) {
        String[] r = Util.split(clockInfo, "|");
        int w = Font.normalFont[0].getWidth(r[0]);
        int w1 = Font.normalFont[0].getWidth(r[1]);
        int w2 = w < w1 ? w1 : w;
        g.setColor(game.pokemon.GiaoDien.mauKhungCon());
        g.fillRect(((x + 16) - (w2 / 2)) - 3, y - 16, w2 + 5, 30);
        g.setColor(Font.nenSang ? game.pokemon.GiaoDien.XANH : game.pokemon.GiaoDien.VANG);
        g.drawRect(((x + 16) - (w2 / 2)) - 3, y - 16, w2 + 5, 30);
        Font.normalFont[0].drawString(g, r[0], x + 16, y - 15, 2);
        Font.normalFont[0].drawString(g, r[1], x + 16, y, 2);
    }

    static String trangThai(int st) {
        return st == 0 ? "Xanh - tưới để lớn nhanh" : st == 1 ? "Héo" : st == 2 ? "Chín - thu hoạch được" : "Thu hoạch được";
    }

    /** 31/10/2026: máy có con trỏ - tấm chung: danh sách quả (chạm = chọn), chi tiết, nút Xem / Tăng tốc / Thu hoạch. */
    void veCham(Graphics g) {
        this.anNutLenh = true;
        ObjTree sel = this.index >= 0 && this.index < this.allTree.size() ? (ObjTree) this.allTree.elementAt(this.index) : null;
        Command xem = sel == null ? null : new Command("Xem chi tiết", this.cmdView.action);
        Command viec = sel == null ? null : sel.status == 0 ? this.cmdTangToc : sel.status == 2 || sel.status == 3 ? this.cmdThuHoach : null;
        int[] c = game.pokemon.ManCu.tam(g, this, this.name == null || this.name.length() == 0 ? "Cây thần" : this.name,
                game.pokemon.ManCu.viec(this.cmdClose), new Command[]{xem, viec});
        game.pokemon.ManCu.tiepQuanMenu(this.name);
        int[][] v = game.pokemon.ManCu.chia(c, 55);
        final Vector l = this.allTree;
        game.pokemon.ManCu.danhSach(g, v[0], l.size(), this.index, new game.pokemon.ManCu.Dong() {
            public String chinh(int i) {
                return ((ObjTree) l.elementAt(i)).name;
            }

            public String phu(int i) {
                return trangThai(((ObjTree) l.elementAt(i)).status);
            }

            public void cham(int i) {
                Tree2014.this.index = i;
                Tree2014.this.isShowText = false;
            }
        }, "Cây chưa ra quả");
        String[] d;
        int[] k;
        if (this.isShowText && this.arrShowText != null) {
            d = this.arrShowText;
            k = new int[d.length];
            if (d.length > 0) {
                k[0] = 1;
            }
        } else if (sel != null) {
            d = new String[]{sel.name, "Trạng thái: " + trangThai(sel.status), "Chạm \"Xem chi tiết\" để xem công dụng."};
            k = new int[]{1, 0, 0};
        } else {
            d = new String[]{"Chọn một quả ở danh sách."};
            k = new int[]{0};
        }
        game.pokemon.ManCu.vanBanPhu(g, v[1], d, k);
    }

    public void paint(Graphics g) {
        if (this.last != null) {
            this.last.paint(g);
        }
        if (game.pokemon.ManCu.bat()) {
            veCham(g);
            return;
        }
        Res.paintDlgFull(g, this.xDr, this.yDr, this.wCell, this.hCell, 0, this.name == null || this.name.length() == 0 ? "Cây thần" : this.name, false, 0);
        int w0 = Canvas.isSmartPhone && this.wCell == 300 ? this.wTree : this.wCell / 2;
        GameData.paintIconTuBinh(g, (short) 8214, this.xDr + w0, (Canvas.h / 2) + 12, 3);
        this.str = null;
        int xx = 0;
        int yy = 0;
        for (int i = 0; i < this.allTree.size(); i++) {
            ObjTree obj = (ObjTree) this.allTree.elementAt(i);
            if (obj != null) {
                if (this.index == i) {
                    g.setColor(game.pokemon.GiaoDien.mauKhungCon());
                    g.fillRect(obj.x, obj.y, 16, 30);
                    g.setColor(Font.nenSang ? game.pokemon.GiaoDien.XANH : game.pokemon.GiaoDien.VANG);
                    g.drawRect(obj.x, obj.y, 16, 30);
                    xx = obj.x;
                    yy = obj.y;
                    String stt = "";
                    if (obj.status == 1) {
                        stt = "héo";
                    }
                    if (obj.status == 0) {
                        stt = "xanh";
                    }
                    if (obj.status == 2) {
                        stt = "chín";
                    }
                    this.str = new StringBuffer(String.valueOf(obj.name)).append("|Trạng thái: ").append(stt).toString();
                }
                obj.paint(g);
            }
        }
        paintShowText(g);
        if (this.str != null && !Canvas.isSmartPhone && !this.isShowText) {
            paintNameTree(g, this.str, xx, yy);
        }
        super.paint(g);
    }

    @Override
    public void update() {
        if (game.pokemon.ManCu.bat() && !this.boc_update && (Canvas.isPointerClick || Canvas.isPointerDown || Canvas.isPointerRelease)) {
            this.boc_update = true;
            try {
                game.pokemon.ManCu.phimKhongCham(new Runnable() {
                    public void run() {
                        Tree2014.this.update();
                    }
                });
            } finally {
                this.boc_update = false;
            }
            return;
        }
        if (this.last != null) {
            this.last.update();
        }
        if (Canvas.currentDialog == null) {
            this.mScrollShowText.updateKey();
            this.mScrollShowText.updatecm();
        }
        super.update();
    }

    public void updateKeyShowText() {
        if (Canvas.keyPressed[2]) {
            Canvas.keyPressed[2] = false;
            this.mScrollShowText.cmtoY -= 50;
            if (this.mScrollShowText.cmtoY < 0) {
                this.mScrollShowText.cmtoY = 0;
                return;
            }
            return;
        }
        if (Canvas.keyPressed[8]) {
            Canvas.keyPressed[8] = false;
            this.mScrollShowText.cmtoY += 50;
            if (this.mScrollShowText.cmtoY > this.mScrollShowText.cmyLim) {
                this.mScrollShowText.cmtoY = this.mScrollShowText.cmyLim;
            }
        }
    }

    @Override
    public void updateKey() {
        if (game.pokemon.ManCu.bat() && !this.boc_updateKey && (Canvas.isPointerClick || Canvas.isPointerDown || Canvas.isPointerRelease)) {
            this.boc_updateKey = true;
            try {
                game.pokemon.ManCu.phimKhongCham(new Runnable() {
                    public void run() {
                        Tree2014.this.updateKey();
                    }
                });
            } finally {
                this.boc_updateKey = false;
            }
            return;
        }
        if (this.isShowText && !Canvas.isSmartPhone) {
            updateKeyShowText();
        } else {
            if (Canvas.keyPressed[4]) {
                Canvas.keyPressed[4] = false;
                this.index--;
                if (this.index < 0) {
                    this.index = this.allTree.size() - 1;
                }
                if (Canvas.isSmartPhone) {
                    this.isShowText = false;
                    this.mScrollShowText.clear();
                }
            }
            if (Canvas.keyPressed[6]) {
                Canvas.keyPressed[6] = false;
                this.index++;
                if (this.index > this.allTree.size() - 1) {
                    this.index = 0;
                }
                if (Canvas.isSmartPhone) {
                    this.isShowText = false;
                    this.mScrollShowText.clear();
                }
            }
            if (Canvas.isSmartPhone) {
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
    public void switchToMe(Screen lastSCR) {
        this.last = lastSCR;
        super.switchToMe(lastSCR);
    }

    public void pointer() {
        if ((Canvas.isSmartPhone || !this.isShowText) && Canvas.isPointerClick) {
            for (int i = 0; i < this.xTrai.length; i++) {
                if (Canvas.px >= this.xTrai[i] && Canvas.px <= this.xTrai[i] + 15 && Canvas.py >= this.yTrai[i] && Canvas.py <= this.yTrai[i] + 30) {
                    Canvas.isPointerClick = false;
                    this.isShowText = false;
                    this.mScrollShowText.clear();
                    this.index = i;
                    if (!this.isShowText && this.center != null) {
                        this.center.action.perform();
                        return;
                    }
                    return;
                }
            }
        }
    }

    public class ObjTree {
        public int x;
        public int y;
        public int idIconImg;
        public byte status = 0;
        public String name = "";
        public String info = "";

        public ObjTree() {
        }

        public void paint(Graphics g) {
            GameData.paintIconTuBinh(g, (short) (this.idIconImg + 8200), this.x, this.y, 0);
        }
    }
}
