package game.model;

import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.Dialog;
import game.render.screen.Font;
import game.render.screen.Screen;

public class ViewTextDlg extends Dialog {
    public static int cmdy;
    public static int cmtoY;
    public static int cmvy;
    public static int cmy;
    public static int cmyLim;
    public static int lastSe;
    private static ViewTextDlg me;
    public static int selected;
    private String[] list;
    private String title;
    private int x = 50;
    private int y = 20;
    private int w = GCanvas.w - 100;
    private int h = (GCanvas.h - 40) - Screen.deltaY;

    public static ViewTextDlg gI() {
        ViewTextDlg viewTextDlg = me;
        if (viewTextDlg != null) {
            return viewTextDlg;
        }
        ViewTextDlg viewTextDlg2 = new ViewTextDlg();
        me = viewTextDlg2;
        return viewTextDlg2;
    }

    public ViewTextDlg() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                GCanvas.currentDialog = null;
            }
        });
        lastSe = ((this.h - 30) / 2) / 14;
    }

    public void setInfo(String str, String str2) {
        String[] strArrSplitFontBStrInLine = Font.arialFont11.splitFontBStrInLine(str, this.w - 15);
        this.list = strArrSplitFontBStrInLine;
        this.title = str2;
        int length = ((strArrSplitFontBStrInLine.length * 14) - (this.h - 25)) + 15;
        cmyLim = length;
        if (length < 0) {
            cmyLim = 0;
        }
        cmtoY = 0;
        cmy = 0;
        selected = lastSe;
        show();
    }

    @Override
    public void paint(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        Res.paintDlgFull(graphics, this.x, this.y, this.w, this.h);
        Font.bigFont1.drawString(graphics, this.title, this.x + (this.w / 2), this.y + 2, 2);
        graphics.translate(this.x, this.y + 25);
        graphics.setClip(0, 0, this.w, this.h - 30);
        graphics.ClipRec(0, 0, this.w, this.h - 30);
        graphics.translate(0, -cmy);
        int i = cmy / 14;
        int length = (this.h / 14) + i + 1;
        String[] strArr = this.list;
        if (length >= strArr.length) {
            length = strArr.length;
        }
        while (i < length) {
            Font.arialFont11.drawString(graphics, this.list[i], 7, (i * 14) + 5, 0);
            i++;
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        super.paint(graphics);
    }

    @Override
    public void update() {
        int i;
        int i2;
        if (GCanvas.keyHold[2]) {
            int i3 = selected - 1;
            selected = i3;
            int i4 = lastSe;
            if (i3 < i4) {
                selected = i4;
            }
        } else {
            if (GCanvas.keyHold[8]) {
                if (cmy < cmyLim) {
                    selected++;
                }
                int i5 = selected;
                String[] strArr = this.list;
                int length = strArr.length;
                int i6 = lastSe;
                if (i5 > (length - i6) + 1) {
                    selected = (strArr.length - i6) + 1;
                }
            }
            i = cmy;
            i2 = cmtoY;
            if (i != i2) {
                int i7 = (i2 - i) << 2;
                cmvy = i7;
                int i8 = cmdy + i7;
                cmy = i + (i8 >> 4);
                cmdy = i8 & 15;
            }
            super.updateKey();
        }
        int i9 = (selected * 14) - ((this.h - 25) / 2);
        cmtoY = i9;
        if (i9 < 0) {
            cmtoY = 0;
        }
        int i10 = cmtoY;
        int i11 = cmyLim;
        if (i10 > i11) {
            cmtoY = i11;
        }
        i = cmy;
        i2 = cmtoY;
        if (i != i2) {
            int i12 = (i2 - i) << 2;
            cmvy = i12;
            int i13 = cmdy + i12;
            cmy = i + (i13 >> 4);
            cmdy = i13 & 15;
        }
        super.updateKey();
    }
}
