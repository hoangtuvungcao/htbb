package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.Dialog;
import game.render.screen.Font;
import game.render.screen.Screen;
import javax.microedition.lcdui.Graphics;

public class ViewTextDlg extends Dialog {
    private static ViewTextDlg me;
    private String[] list;
    private String title;
    private int x = 50;
    private int y = 20;
    private int w = Canvas.w - 100;
    private int h = (Canvas.h - 40) - Screen.deltaY;
    public static int cmtoY;
    public static int cmy;
    public static int cmdy;
    public static int cmvy;
    public static int cmyLim;
    public static int selected = 0;
    public static int lastSe = 0;

    public static ViewTextDlg gI() {
        if (me != null) {
            return me;
        }
        ViewTextDlg viewTextDlg = new ViewTextDlg();
        me = viewTextDlg;
        return viewTextDlg;
    }

    public ViewTextDlg() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                Canvas.currentDialog = null;
            }
        });
        lastSe = ((this.h - 30) / 2) / 14;
    }

    public void setInfo(String list, String title) {
        this.list = Font.arialFontW.splitFontBStrInLine(list, this.w - 15);
        this.title = title;
        cmyLim = ((this.list.length * 14) - (this.h - 25)) + 15;
        if (cmyLim < 0) {
            cmyLim = 0;
        }
        cmtoY = 0;
        cmy = 0;
        selected = lastSe;
        show();
    }

    @Override
    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        Res.paintDlgFull(g, this.x, this.y, this.w, this.h);
        Font.bigFont.drawString(g, this.title, this.x + (this.w / 2), this.y + 2, 2);
        g.translate(this.x, this.y + 25);
        g.setClip(0, 0, this.w, this.h - 30);
        g.translate(0, -cmy);
        int aa = cmy / 14;
        int bb = aa + (this.h / 14) + 1;
        if (bb >= this.list.length) {
            bb = this.list.length;
        }
        for (int i = aa; i < bb; i++) {
            Font.arialFontW.drawString(g, this.list[i], 7, 5 + (i * 14), 0);
        }
        super.paint(g);
    }

    @Override
    public void update() {
        boolean changeFocus = false;
        if (Canvas.keyHold[2]) {
            selected--;
            if (selected < lastSe) {
                selected = lastSe;
            }
            changeFocus = true;
        } else if (Canvas.keyHold[8]) {
            changeFocus = true;
            if (cmy < cmyLim) {
                selected++;
            }
            if (selected > (this.list.length - lastSe) + 1) {
                selected = (this.list.length - lastSe) + 1;
            }
        }
        if (changeFocus) {
            cmtoY = (selected * 14) - ((this.h - 25) / 2);
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
        super.updateKey();
    }
}
