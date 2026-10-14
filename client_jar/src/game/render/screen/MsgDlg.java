package game.render.screen;

import game.model.Command;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class MsgDlg extends Dialog {
    protected String[] info;
    long time;
    public byte size;
    private int w;
    private int h;
    private int ySt;
    private int y;
    public Vector listDelay;
    private int selected;
    private int dis;
    private int disLim;
    private int wText;
    public static int cmtoY;
    public static int cmy;
    public static int cmdy;
    public static int cmvy;
    public static int cmyLim;
    public String[] arr;
    public boolean isUseArr;
    public int hArr;
    private static final int[] translate = {0, 7, 3, 6};
    public static int lastSe = 0;
    public int state = 0;
    public boolean isIcon = true;
    public boolean isShowDelay = false;
    int dir = 1;
    int x2 = 0;

    public void setInfo(String info, Command left, Command center, Command right) {
        this.info = Font.arialFontW.splitFontBStrInLine(info, 100);
        this.left = left;
        this.center = center;
        this.right = right;
        this.isShowDelay = false;
        init();
        this.selected = lastSe;
        this.isUseArr = false;
    }

    public void setInfo(String[] info2, Command left, Command center, Command right) {
        this.info = info2;
        this.left = left;
        this.center = center;
        this.right = right;
        this.isShowDelay = false;
        init();
        this.selected = lastSe;
        this.isUseArr = false;
    }

    public void setInfoArr(String info, String[] arr, Command left, Command center, Command right) {
        this.info = Font.arialFontW.splitFontBStrInLine(info, Canvas.w - 60);
        this.left = left;
        this.center = center;
        this.right = right;
        this.isShowDelay = false;
        this.arr = arr;
        init();
        this.selected = lastSe;
        this.hArr = (arr.length * 20) + (20 * this.info.length);
        this.isUseArr = true;
    }

    public void init() {
        this.w = Canvas.w - 40;
        this.h = 20;
        this.dis = 90;
        this.y = Canvas.hh - 42;
        this.ySt = 15;
        if (this.isIcon) {
            this.dis -= 50;
            this.ySt += 50;
        }
        this.disLim = this.dis;
        if (this.info.length * 13 < this.dis) {
            this.ySt += (this.dis / 2) - ((this.info.length * 13) / 2);
            this.disLim -= this.dis - (this.info.length * 13);
        }
        cmyLim = (this.info.length * 13) - this.dis;
        if (cmyLim < 0) {
            cmyLim = 0;
        }
        lastSe = (this.dis / 2) / 13;
    }

    public void paintArr(Graphics g) {
        Canvas.resetTrans(g);
        int ybg0 = (Canvas.hh - (this.hArr / 2)) - 20;
        if (ybg0 <= 10) {
            ybg0 = 10;
        }
        Res.paintDlgDragonFull(g, 20, ybg0, Canvas.w - 40, this.hArr + 20);
        for (int i = 0; i < this.arr.length; i++) {
            Font.arialFontW.drawString(g, this.arr[i], Canvas.hw, (i * 20) + 10 + ybg0, 2);
        }
        int ybd = 10 + 32;
        for (int i2 = 0; i2 < this.info.length; i2++) {
            Font.normalFont[0].drawString(g, this.info[i2], Canvas.hw, ((((i2 * 20) + ((this.arr.length - 1) * 20)) + ybd) + ybg0) - 4, 2);
        }
        g.translate(0, this.y);
    }

    @Override
    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        g.translate(0, this.y);
        if (this.isUseArr) {
            paintArr(g);
        } else {
            Res.paintDlgDragonFull(g, Canvas.hw - 70, 12, 140, 100);
            if (this.isIcon) {
                g.drawRegion(Res.imgWaitIcon, 0, 0, 25, 25, translate[this.state], Canvas.hw, 47, 3);
            }
            g.translate(0, this.ySt);
            g.setClip(0, 0, Canvas.w, this.disLim);
            g.translate(0, -cmy);
            for (int i = 0; i < this.info.length; i++) {
                Font.arialFontW.drawString(g, this.info[i], Canvas.hw, i * 13, 2);
            }
        }
        super.paint(g);
    }

    @Override
    public void update() {
        this.state++;
        if (this.state > 3) {
            this.state = 0;
        }
        boolean changeFocus = false;
        if (Canvas.keyHold[2]) {
            this.selected--;
            if (this.selected < lastSe) {
                this.selected = lastSe;
            }
            changeFocus = true;
        } else if (Canvas.keyHold[8]) {
            changeFocus = true;
            if (cmy < cmyLim) {
                this.selected++;
            }
            if (this.selected > this.info.length - lastSe) {
                this.selected = this.info.length - lastSe;
            }
        }
        if (changeFocus) {
            cmtoY = (this.selected * 13) - (this.dis / 2);
            if (cmtoY < 0) {
                cmtoY = 0;
            }
            if (cmtoY > cmyLim) {
                cmtoY = cmyLim;
            }
        }
        if (cmy != cmtoY) {
            cmvy = (cmtoY - cmy) << 2;
            cmdy += cmvy;
            cmy += cmdy >> 4;
            cmdy &= 15;
        }
        super.update();
    }
}
