package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Command;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;

public class HelpScr extends Screen {
    private static HelpScr me;
    public int cmdy;
    public int cmtoY;
    public int cmvy;
    public int cmy;
    int[] font;
    private int pa;
    private boolean trans;
    public int cmyLim = 10000;
    String[] stInfo = null;
    private short y = 20;
    private short x = 20;
    private short w = (short) (GCanvas.w - (this.x * 2));
    private short h = (short) ((GCanvas.h - (this.y * 2)) - deltaY);

    public static HelpScr gI() {
        HelpScr helpScr = me;
        if (helpScr != null) {
            return helpScr;
        }
        HelpScr helpScr2 = new HelpScr();
        me = helpScr2;
        return helpScr2;
    }

    public HelpScr() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                GCanvas.gameScr.switchToMe();
                HelpScr unused = HelpScr.me = null;
                HelpScr.this.stInfo = null;
            }
        });
        GameService.gI().requestString(0);
        GCanvas.startWaitDlg();
    }

    public void setInfo(String str) {
        String[] strArrSplitFontBStrInLine = Font.normalFont[0].splitFontBStrInLine(str, GCanvas.w - 30);
        this.stInfo = strArrSplitFontBStrInLine;
        this.font = new int[strArrSplitFontBStrInLine.length];
        int i = 0;
        int i2 = 0;
        while (true) {
            String[] strArr = this.stInfo;
            if (i < strArr.length) {
                if (strArr[i].trim().startsWith("x0")) {
                    String[] strArr2 = this.stInfo;
                    strArr2[i] = strArr2[i].substring(3);
                    i2 = 0;
                } else if (this.stInfo[i].trim().startsWith("x1")) {
                    String[] strArr3 = this.stInfo;
                    strArr3[i] = strArr3[i].substring(3);
                    i2 = 1;
                }
                this.font[i] = i2;
                i++;
            } else {
                this.cmyLim = (strArr.length * 13) - (this.h - 35);
                GCanvas.endDlg();
                return;
            }
        }
    }

    @Override
    public boolean keyPress(int i) {
        return super.keyPress(i);
    }

    @Override
    public void paint(Graphics graphics) {
        GCanvas.gameScr.paint(graphics);
        graphics.setColor(0);
        graphics.setAlpha(180);
        graphics.fillRect(0, 0, GCanvas.w, GCanvas.h);
        graphics.translate(this.x, this.y);
        Res.paintDlgFull(graphics, 0, 0, this.w, this.h);
        Font font = Font.normalFont[0];
        if (GCanvas.screenlevel == 1) {
            font = Font.bigFont1;
        }
        font.drawString(graphics, "HƯỚNG DẪN", this.w / 2, (GCanvas.screenlevel == 1 ? 4 : 0) + 6, 2);
        graphics.translate(0, 30);
        int translateX = graphics.getTranslateX();
        int translateY = graphics.getTranslateY();
        Graphics.resetTransAndroid(graphics);
        graphics.saveCanvas();
        graphics.translateAndroid(translateX, translateY);
        graphics.setClip(0, -2, this.w - 4, this.h - 32);
        graphics.ClipRec2(0, -2, this.w - 4, this.h - 32);
        graphics.translate(0, -this.cmy);
        String[] strArr = this.stInfo;
        if (strArr != null) {
            int i = this.cmy / 13;
            int length = ((this.h - 35) / 13) + i + 2;
            if (length > strArr.length) {
                length = strArr.length;
            }
            while (i < length) {
                if (i < 0) {
                    i = 0;
                }
                (this.font[i] == 0 ? Font.arialFont11 : Font.normalFont[0]).drawString(graphics, this.stInfo[i], 7, i * 13, 0);
                i++;
            }
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        super.paint(graphics);
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
    }

    @Override
    public void update() {
        boolean z;
        moveCameraY();
        if (GCanvas.keyHold[8]) {
            this.cmtoY += 20;
            GCanvas.clearKeyHold();
            z = true;
        } else {
            z = false;
        }
        if (GCanvas.keyHold[2]) {
            this.cmtoY -= 20;
            GCanvas.clearKeyHold();
            z = true;
        }
        if (z) {
            int i = this.cmtoY;
            int i2 = this.cmyLim;
            if (i > i2) {
                this.cmtoY = i2;
            }
            if (this.cmtoY < 0) {
                this.cmtoY = 0;
            }
        }
        if (GCanvas.isPointerDown && GCanvas.isPointer(this.x, this.y, this.w, this.h)) {
            if (!this.trans) {
                this.pa = this.cmy;
                this.trans = true;
            }
            int i3 = this.pa + (GCanvas.pyLast - GCanvas.py);
            this.cmtoY = i3;
            if (i3 < (-GCanvas.pyLast)) {
                this.cmtoY = -GCanvas.pyLast;
            }
            if (this.cmtoY > this.cmyLim + (GCanvas.pyLast / 2)) {
                this.cmtoY = this.cmyLim + (GCanvas.pyLast / 2);
            }
        }
        if (GCanvas.isPointerClick) {
            this.trans = false;
        }
        super.update();
    }

    public void moveCameraY() {
        int i = this.cmy;
        int i2 = this.cmtoY;
        if (i != i2) {
            int i3 = (i2 - i) << 2;
            this.cmvy = i3;
            int i4 = this.cmdy + i3;
            this.cmy = i + (i4 >> 4);
            this.cmdy = i4 & 15;
        }
        if (Math.abs(i2 - this.cmy) < 15 && this.cmy < 0) {
            this.cmtoY = 0;
        }
        if (Math.abs(this.cmtoY - this.cmy) < 10) {
            int i5 = this.cmy;
            int i6 = this.cmyLim;
            if (i5 > i6) {
                this.cmtoY = i6;
            }
        }
    }
}
