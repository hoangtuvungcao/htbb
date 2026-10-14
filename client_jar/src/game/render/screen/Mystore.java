package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.model.Myseed;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class Mystore extends Screen {
    public static Mystore me;
    int hShop;
    int wShop;
    int index;
    int sum;
    int indexText;
    public static int cmtoY;
    public static int cmy;
    public static int cmdy;
    public static int cmvy;
    public static int cmyLim;
    public static int cmtoYText;
    public static int cmyText;
    public static int cmdyText;
    public static int cmvyText;
    public static int cmyLimText;
    boolean isvieW;
    public Vector mySeed = new Vector();
    int xShow = 0;
    int yShow = 0;
    int wcell = 0;
    int hcell = 0;
    String[] arr = {""};

    public static Mystore gI() {
        if (me != null) {
            return me;
        }
        Mystore mystore = new Mystore();
        me = mystore;
        return mystore;
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
        init();
    }

    void loadMyseed(int sum0) {
        int ybegin = (Canvas.h / 2) - (this.hShop / 2);
        int xbegin = (Canvas.w / 2) - (this.wShop / 2);
        for (int i = 0; i < sum0; i++) {
            Myseed obj = new Myseed(xbegin + ((i % 7) * 18) + 10, ybegin + ((i / 7) * 18) + 39, 0, new StringBuffer(String.valueOf("hạt giống cao cấp, thời gian trồng 20 giời,  rg g rg rgsrg  rgr gr r gr rr grrs gsr cho ra đồ cao cấp, pham cao minh trung,pham cao trung minh,ff, ff esf, ef se,fsef,sef ,es,f sef,es,fes ad a aae e rfe eea faef eff eaf s  faefsw  a ar fae erfear aer ar arf ea feaf")).append(i).toString());
            this.mySeed.addElement(obj);
        }
        this.sum = sum0;
    }

    @Override
    public void init() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                Canvas.gameScr.switchToMe();
                Mystore.this.reset();
            }
        });
        this.center = new Command("", new IAction() {
            @Override
            public void perform() {
                Mystore.this.isvieW = !Mystore.this.isvieW;
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
        cmyLim = (((this.sum / 7) * 18) - ((this.hShop + 38) / 2)) + 4;
        if (cmtoY < 0) {
            cmtoY = 0;
        }
        if (cmtoY > cmyLim) {
            cmtoY = cmyLim;
        }
        cmtoY = 0;
        cmy = 0;
    }

    public void loadCmrText() {
        this.indexText = 0;
        cmyLimText = (this.arr.length * 20) - this.hcell;
        if (cmyLimText < 0) {
            cmyLimText = 0;
        }
        if (cmtoYText < 0) {
            cmtoYText = 0;
        }
        if (cmtoYText > cmyLimText) {
            cmtoYText = cmyLimText;
        }
        cmtoYText = 0;
        cmyText = 0;
    }

    public void updateCameraTexet() {
        if (this.isvieW) {
            if (cmyText != cmtoYText) {
                cmvyText = (cmtoYText - cmyText) << 2;
                cmdyText += cmvyText;
                cmyText += cmdyText >> 4;
                cmdyText &= 15;
                if (cmyText < 0) {
                    cmyText = 0;
                }
                if (cmyText > cmyLimText) {
                    cmyText = cmyLimText;
                }
            }
            cmtoYText = (this.indexText * 18) - 20;
        }
    }

    public void updateCamera() {
        if (cmy != cmtoY) {
            cmvy = (cmtoY - cmy) << 2;
            cmdy += cmvy;
            cmy += cmdy >> 4;
            cmdy &= 15;
            if (cmy < 0) {
                cmy = 0;
            }
            if (cmy > cmyLim) {
                cmy = cmyLim;
            }
        }
        cmtoY = ((this.index / 7) * 18) - 20;
    }

    @Override
    public void paint(Graphics g) {
        Canvas.gameScr.paint(g);
        g.translate(-g.getTranslateX(), -g.getTranslateY());
        int ybegin = (Canvas.h / 2) - (this.hShop / 2);
        int xbegin = (Canvas.w / 2) - (this.wShop / 2);
        Res.paintDlgDragonFull(g, xbegin, ybegin, this.wShop, this.hShop);
        for (int i = 0; i < 3; i++) {
            g.setColor(Res.color[i]);
            g.fillRect(xbegin + 3, (ybegin + 30) - i, this.wShop - 6, 1);
        }
        Font.normalFont[0].drawString(g, "Kho giống", Canvas.w / 2, ybegin + 8, 2);
        g.setClip(xbegin + 4, ybegin + 39, this.wShop - 6, this.hShop - 57);
        g.translate(0, -cmy);
        for (int i2 = 0; i2 < this.mySeed.size(); i2++) {
            Myseed obj = (Myseed) this.mySeed.elementAt(i2);
            if (this.index == i2) {
                obj.isfocus = true;
                this.xShow = obj.x - (obj.wcell / 2);
                this.yShow = obj.y;
                this.wcell = obj.wcell;
                this.hcell = obj.hcell;
                this.arr = obj.arrInfo;
            } else {
                obj.isfocus = false;
            }
            obj.paint(g);
        }
        g.setColor(11184844);
        g.drawRect(xbegin + ((this.index % 7) * 18) + 10, ybegin + ((this.index / 7) * 18) + 39, 18, 18);
        paintShowInfo(g);
        super.paint(g);
    }

    public void paintShowInfo(Graphics g) {
        if (this.isvieW) {
            if (this.xShow < 0) {
                this.xShow = 10;
            }
            if (this.xShow + this.wcell > Canvas.w) {
                this.xShow = (Canvas.w - this.wcell) - 10;
            }
            if (this.yShow + 20 + cmy + this.hcell > Canvas.h) {
                this.yShow += ((-this.hcell) / 2) - 22;
            }
            g.translate(this.xShow, this.yShow);
            g.setClip(0, 0, Canvas.w, Canvas.h);
            g.setColor(26197);
            g.fillRect(4, 24 + 0, this.wcell, this.hcell);
            g.setColor(16777028);
            g.drawRect(4, 24 + 0, this.wcell, this.hcell);
            g.setClip(0, 24 + 0, Canvas.w, this.hcell);
            g.translate(0, -cmyText);
            for (int i = 0; i < this.arr.length; i++) {
                Font.normalFont[0].drawString(g, this.arr[i], 10, 4 + (i * 18) + 26, 0);
            }
        }
    }

    @Override
    public void update() {
        Canvas.gameScr.update();
        if (this.isvieW) {
            if (Canvas.keyPressed[2]) {
                Canvas.keyPressed[2] = false;
                if (cmyText > 0) {
                    this.indexText--;
                }
            } else if (Canvas.keyPressed[8]) {
                Canvas.keyPressed[8] = false;
                if (cmyText < cmyLimText) {
                    this.indexText++;
                }
            } else if (Canvas.keyPressed[6]) {
                Canvas.keyPressed[6] = false;
                this.isvieW = false;
                cmtoYText = 0;
                cmyText = 0;
            } else if (Canvas.keyPressed[4]) {
                Canvas.keyPressed[4] = false;
                this.isvieW = false;
                cmtoYText = 0;
                cmyText = 0;
            }
        } else if (Canvas.keyPressed[6]) {
            Canvas.keyPressed[6] = false;
            this.isvieW = false;
            this.index++;
            if (this.index > this.sum - 1) {
                this.index = 0;
            }
        } else if (Canvas.keyPressed[4]) {
            Canvas.keyPressed[4] = false;
            this.isvieW = false;
            this.index--;
            if (this.index < 0) {
                this.index = this.sum - 1;
            }
        } else if (Canvas.keyPressed[2]) {
            Canvas.keyPressed[2] = false;
            if (this.index / 7 > 0) {
                this.index -= 7;
            }
        } else if (Canvas.keyPressed[8]) {
            Canvas.keyPressed[8] = false;
            if (this.index + 7 < this.sum) {
                this.index += 7;
            }
        }
        updateCamera();
        updateCameraTexet();
        super.update();
    }
}
