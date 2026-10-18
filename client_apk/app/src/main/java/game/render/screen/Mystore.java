package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Command;
import game.model.IAction;
import game.model.Myseed;
import game.render.GCanvas;
import game.render.Res;
import java.util.Vector;

public class Mystore extends Screen {
    public static int cmdy;
    public static int cmdyText;
    public static int cmtoY;
    public static int cmtoYText;
    public static int cmvy;
    public static int cmvyText;
    public static int cmy;
    public static int cmyLim;
    public static int cmyLimText;
    public static int cmyText;
    public static Mystore me;
    int hShop;
    int index;
    int indexText;
    boolean isvieW;
    int sum;
    int wShop;
    public Vector mySeed = new Vector();
    int xShow = 0;
    int yShow = 0;
    int wcell = 0;
    int hcell = 0;
    String[] arr = {""};

    public static Mystore gI() {
        Mystore mystore = me;
        if (mystore != null) {
            return mystore;
        }
        Mystore mystore2 = new Mystore();
        me = mystore2;
        return mystore2;
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
        init();
    }

    void loadMyseed(int i) {
        int i2 = (GCanvas.h / 2) - (this.hShop / 2);
        int i3 = (GCanvas.w / 2) - (this.wShop / 2);
        for (int i4 = 0; i4 < i; i4++) {
            this.mySeed.addElement(new Myseed(((i4 % 7) * 18) + i3 + 10, ((i4 / 7) * 18) + i2 + 39, 0, "hạt giống cao cấp, thời gian trồng 20 giời,  rg g rg rgsrg  rgr gr r gr rr grrs gsr cho ra đồ cao cấp, pham cao minh trung,pham cao trung minh,ff, ff esf, ef se,fsef,sef ,es,f sef,es,fes ad a aae e rfe eea faef eff eaf s  faefsw  a ar fae erfear aer ar arf ea feaf" + i4));
        }
        this.sum = i;
    }

    @Override
    public void init() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                GCanvas.gameScr.switchToMe();
                Mystore.this.reset();
            }
        });
        this.center = new Command("", new IAction() {
            @Override
            public void perform() {
                Mystore mystore = Mystore.this;
                mystore.isvieW = !mystore.isvieW;
                if (Mystore.this.isvieW) {
                    Mystore.this.loadCmrText();
                }
            }
        });
        this.left = new Command("Chọn", new IAction() {
            @Override
            public void perform() {
            }
        });
        this.isvieW = false;
        this.hShop = 150;
        this.wShop = 150;
        loadMyseed(70);
        loadCmr();
    }

    public void reset() {
        this.index = 0;
        this.indexText = 0;
        this.isvieW = false;
        cmyText = 0;
        cmtoYText = 0;
        cmtoY = 0;
        cmy = 0;
    }

    public void loadCmr() {
        int i = (((this.sum / 7) * 18) - ((this.hShop + 38) / 2)) + 4;
        cmyLim = i;
        if (cmtoY < 0) {
            cmtoY = 0;
        }
        if (cmtoY > i) {
            cmtoY = i;
        }
        cmtoY = 0;
        cmy = 0;
    }

    public void loadCmrText() {
        this.indexText = 0;
        int length = (this.arr.length * 20) - this.hcell;
        cmyLimText = length;
        if (length < 0) {
            cmyLimText = 0;
        }
        if (cmtoYText < 0) {
            cmtoYText = 0;
        }
        int i = cmtoYText;
        int i2 = cmyLimText;
        if (i > i2) {
            cmtoYText = i2;
        }
        cmtoYText = 0;
        cmyText = 0;
    }

    public void updateCameraTexet() {
        if (this.isvieW) {
            int i = cmyText;
            int i2 = cmtoYText;
            if (i != i2) {
                int i3 = (i2 - i) << 2;
                cmvyText = i3;
                int i4 = cmdyText + i3;
                int i5 = i + (i4 >> 4);
                cmyText = i5;
                cmdyText = i4 & 15;
                if (i5 < 0) {
                    cmyText = 0;
                }
                int i6 = cmyText;
                int i7 = cmyLimText;
                if (i6 > i7) {
                    cmyText = i7;
                }
            }
            cmtoYText = (this.indexText * 18) - 20;
        }
    }

    public void updateCamera() {
        int i = cmy;
        int i2 = cmtoY;
        if (i != i2) {
            int i3 = (i2 - i) << 2;
            cmvy = i3;
            int i4 = cmdy + i3;
            int i5 = i + (i4 >> 4);
            cmy = i5;
            cmdy = i4 & 15;
            if (i5 < 0) {
                cmy = 0;
            }
            int i6 = cmy;
            int i7 = cmyLim;
            if (i6 > i7) {
                cmy = i7;
            }
        }
        cmtoY = ((this.index / 7) * 18) - 20;
    }

    @Override
    public void paint(Graphics graphics) {
        GCanvas.gameScr.paint(graphics);
        graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
        int i = (GCanvas.h / 2) - (this.hShop / 2);
        int i2 = GCanvas.w / 2;
        int i3 = this.wShop;
        int i4 = i2 - (i3 / 2);
        Res.paintDlgDragonFull(graphics, i4, i, i3, this.hShop);
        for (int i5 = 0; i5 < 3; i5++) {
            graphics.setColor(Res.color[i5]);
            graphics.fillRect(i4 + 3, (i + 30) - i5, this.wShop - 6, 1);
        }
        Font.normalFont[0].drawString(graphics, "Kho giống", GCanvas.w / 2, i + 8, 2);
        int i6 = i4 + 4;
        int i7 = i + 39;
        graphics.setClip(i6, i7, this.wShop - 6, this.hShop - 57);
        graphics.ClipRec(i6, i7, this.wShop - 6, this.hShop - 57);
        graphics.translate(0, -cmy);
        int size = this.mySeed.size();
        for (int i8 = 0; i8 < size; i8++) {
            Myseed myseed = (Myseed) this.mySeed.elementAt(i8);
            if (this.index == i8) {
                myseed.isfocus = true;
                this.xShow = myseed.x - (myseed.wcell / 2);
                this.yShow = myseed.y;
                this.wcell = myseed.wcell;
                this.hcell = myseed.hcell;
                this.arr = myseed.arrInfo;
            } else {
                myseed.isfocus = false;
            }
            myseed.paint(graphics);
        }
        graphics.setColor(-5592372);
        int i9 = this.index;
        graphics.drawRect(i4 + ((i9 % 7) * 18) + 10, i + ((i9 / 7) * 18) + 39, 18, 18);
        paintShowInfo(graphics);
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        super.paint(graphics);
    }

    public void paintShowInfo(Graphics graphics) {
        if (this.isvieW) {
            if (this.xShow < 0) {
                this.xShow = 10;
            }
            if (this.xShow + this.wcell > GCanvas.w) {
                this.xShow = (GCanvas.w - this.wcell) - 10;
            }
            if (this.yShow + 20 + cmy + this.hcell > GCanvas.h) {
                this.yShow += ((-this.hcell) / 2) - 22;
            }
            graphics.translate(this.xShow, this.yShow);
            graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
            graphics.setColor(-16751019);
            graphics.fillRect(4, 24, this.wcell, this.hcell);
            graphics.setColor(-188);
            graphics.drawRect(4, 24, this.wcell, this.hcell);
            graphics.setClip(0, 24, GCanvas.w, this.hcell);
            graphics.ClipRec(0, 24, GCanvas.w, this.hcell);
            graphics.translate(0, -cmyText);
            for (int i = 0; i < this.arr.length; i++) {
                Font.normalFont[0].drawString(graphics, this.arr[i], 10, (i * 18) + 30, 0);
            }
            Graphics.resetTransAndroid(graphics);
            graphics.restoreCanvas();
        }
    }

    @Override
    public void update() {
        GCanvas.gameScr.update();
        if (this.isvieW) {
            if (GCanvas.keyPressed[2]) {
                GCanvas.keyPressed[2] = false;
                if (cmyText > 0) {
                    this.indexText--;
                }
            } else if (GCanvas.keyPressed[8]) {
                GCanvas.keyPressed[8] = false;
                if (cmyText < cmyLimText) {
                    this.indexText++;
                }
            } else if (GCanvas.keyPressed[6]) {
                GCanvas.keyPressed[6] = false;
                this.isvieW = false;
                cmtoYText = 0;
                cmyText = 0;
            } else if (GCanvas.keyPressed[4]) {
                GCanvas.keyPressed[4] = false;
                this.isvieW = false;
                cmtoYText = 0;
                cmyText = 0;
            }
        } else if (GCanvas.keyPressed[6]) {
            GCanvas.keyPressed[6] = false;
            this.isvieW = false;
            int i = this.index + 1;
            this.index = i;
            if (i > this.sum - 1) {
                this.index = 0;
            }
        } else if (GCanvas.keyPressed[4]) {
            GCanvas.keyPressed[4] = false;
            this.isvieW = false;
            int i2 = this.index - 1;
            this.index = i2;
            if (i2 < 0) {
                this.index = this.sum - 1;
            }
        } else if (GCanvas.keyPressed[2]) {
            GCanvas.keyPressed[2] = false;
            int i3 = this.index;
            if (i3 / 7 > 0) {
                this.index = i3 - 7;
            }
        } else if (GCanvas.keyPressed[8]) {
            GCanvas.keyPressed[8] = false;
            int i4 = this.index;
            if (i4 + 7 < this.sum) {
                this.index = i4 + 7;
            }
        }
        updateCamera();
        updateCameraTexet();
        super.update();
    }
}
