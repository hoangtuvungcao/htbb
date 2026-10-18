package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Command;
import game.render.GCanvas;
import game.render.Res;
import java.util.Vector;

public class MsgDlg extends Dialog {
    public static int cmdy;
    public static int cmtoY;
    public static int cmvy;
    public static int cmy;
    public static int cmyLim;
    public String[] arr;
    private int dis;
    private int disLim;
    private int h;
    public int hArr;
    protected String[] info;
    public boolean isUseArr;
    public Vector listDelay;
    private int selected;
    public byte size;
    long time;
    private int w;
    private int wText;
    private int y;
    private int ySt;
    private static final int[] translate = {0, 7, 3, 6};
    public static int lastSe = 0;
    public int state = 0;
    public boolean isIcon = true;
    public boolean isShowDelay = false;
    int dir = 1;
    int x2 = 0;

    public void setInfo(String str, Command command, Command command2, Command command3) {
        this.info = Font.arialFont11.splitFontBStrInLine(str, 100);
        this.left = command;
        this.center = command2;
        this.right = command3;
        this.isShowDelay = false;
        init();
        this.selected = lastSe;
        this.isUseArr = false;
    }

    public void setInfo(String[] strArr, Command command, Command command2, Command command3) {
        this.info = strArr;
        this.left = command;
        this.center = command2;
        this.right = command3;
        this.isShowDelay = false;
        init();
        this.selected = lastSe;
        this.isUseArr = false;
    }

    public void setInfoArr(String str, String[] strArr, Command command, Command command2, Command command3) {
        this.info = Font.arialFont11.splitFontBStrInLine(str, 100);
        this.left = command;
        this.center = command2;
        this.right = command3;
        this.isShowDelay = false;
        this.arr = strArr;
        init();
        this.selected = lastSe;
        this.hArr = (strArr.length * Font.arialFont11.getHeight()) + 40 + (Font.arialFont11.getHeight() * this.info.length);
        this.isUseArr = true;
    }

    public void init() {
        this.w = GCanvas.w - 40;
        this.h = 20;
        this.dis = 90;
        this.y = GCanvas.hh - 50;
        this.ySt = 15;
        if (this.isIcon) {
            this.dis -= 50;
            this.ySt = 15 + 50;
        }
        int i = this.dis;
        this.disLim = i;
        String[] strArr = this.info;
        if (strArr.length * 13 < i) {
            this.ySt += (i / 2) - ((strArr.length * 13) / 2);
            this.disLim = i - (i - (strArr.length * 13));
        }
        int length = (strArr.length * 13) - i;
        cmyLim = length;
        if (length < 0) {
            cmyLim = 0;
        }
        lastSe = (i / 2) / 13;
    }

    public void paintArr(Graphics graphics) {
        Res.paintDlgDragonFull(graphics, GCanvas.hw - 80, 12, 160, this.hArr);
        for (int i = 0; i < this.arr.length; i++) {
            Font.arialFont11.drawString(graphics, this.arr[i], GCanvas.hw, (i * 13) + 22, 2);
        }
        for (int i2 = 0; i2 < this.info.length; i2++) {
            Font.normalFont[1].drawString(graphics, this.info[i2], GCanvas.hw, (i2 * 13) + ((this.arr.length - 1) * 13) + 40, 2);
        }
    }

    @Override
    public void paint(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        if (this.isUseArr) {
            graphics.translate(0, this.y - 50);
            paintArr(graphics);
        } else {
            if (this.isIcon) {
                graphics.setColor(-16777216);
                graphics.setAlpha(180);
                graphics.fillRect(0, 0, GCanvas.w, GCanvas.h);
                graphics.translate(0, this.y);
                graphics.drawRegion(Res.imgWaitIcon, 0, 0, 25, 25, translate[this.state], GCanvas.hw, 47, 3);
            } else {
                graphics.translate(0, this.y);
                Res.paintDlgDragonFull(graphics, GCanvas.hw - 70, 12, 140, 100);
            }
            graphics.translate(0, this.ySt);
            graphics.setClip(0, 0, GCanvas.w, this.disLim);
            graphics.translate(0, -cmy);
            for (int i = 0; i < this.info.length; i++) {
                Font.arialFont11.drawString(graphics, this.info[i], GCanvas.hw, i * 13, 2);
            }
        }
        super.paint(graphics);
    }

    @Override
    public void update() {
        int i;
        int i2;
        int i3 = this.state + 1;
        this.state = i3;
        if (i3 > 3) {
            this.state = 0;
        }
        if (GCanvas.keyHold[2]) {
            int i4 = this.selected - 1;
            this.selected = i4;
            int i5 = lastSe;
            if (i4 < i5) {
                this.selected = i5;
            }
        } else {
            if (GCanvas.keyHold[8]) {
                if (cmy < cmyLim) {
                    this.selected++;
                }
                int i6 = this.selected;
                String[] strArr = this.info;
                int length = strArr.length;
                int i7 = lastSe;
                if (i6 > length - i7) {
                    this.selected = strArr.length - i7;
                }
            }
            i = cmy;
            i2 = cmtoY;
            if (i != i2) {
                int i8 = (i2 - i) << 2;
                cmvy = i8;
                int i9 = cmdy + i8;
                cmy = i + (i9 >> 4);
                cmdy = i9 & 15;
            }
            super.update();
        }
        int i10 = (this.selected * 13) - (this.dis / 2);
        cmtoY = i10;
        if (i10 < 0) {
            cmtoY = 0;
        }
        int i11 = cmtoY;
        int i12 = cmyLim;
        if (i11 > i12) {
            cmtoY = i12;
        }
        i = cmy;
        i2 = cmtoY;
        if (i != i2) {
            int i13 = (i2 - i) << 2;
            cmvy = i13;
            int i14 = cmdy + i13;
            cmy = i + (i14 >> 4);
            cmdy = i14 & 15;
        }
        super.update();
    }
}
