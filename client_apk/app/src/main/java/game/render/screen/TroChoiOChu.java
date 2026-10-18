package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Command;
import game.model.IAction;
import game.render.GCanvas;
import game.render.Res;

public class TroChoiOChu extends Screen {
    public static int cmdy;
    public static int cmtoY;
    public static int cmvy;
    public static int cmy;
    public static int cmyLim;
    public static int maxOChu;
    public static TroChoiOChu me;
    public byte[] arrNum;
    int hSmall;
    int height;
    private boolean isClick;
    int numH;
    int numW;
    int pb;
    int selected;
    private boolean trans;
    int wSmall;
    int width;
    int x;
    int y;

    public static TroChoiOChu gI() {
        TroChoiOChu troChoiOChu = me;
        if (troChoiOChu != null) {
            return troChoiOChu;
        }
        TroChoiOChu troChoiOChu2 = new TroChoiOChu();
        me = troChoiOChu2;
        return troChoiOChu2;
    }

    public TroChoiOChu() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                GCanvas.gameScr.switchToMe();
            }
        });
        this.center = new Command("", new IAction() {
            @Override
            public void perform() {
            }
        });
    }

    @Override
    public void switchToMe() {
        init();
        super.switchToMe();
    }

    @Override
    public void init() {
        this.width = GCanvas.w - 60;
        this.height = GCanvas.h - 80;
        this.hSmall = 24;
        this.wSmall = 24;
        this.numW = 10;
        this.numH = 10;
        maxOChu = this.arrNum.length;
        this.selected = 0;
        this.isClick = false;
        setLim();
    }

    private void setLim() {
        int i = this.hSmall;
        int i2 = (this.numH * i) - (i * 6);
        cmyLim = i2;
        if (i2 < 0) {
            cmyLim = 0;
        }
    }

    @Override
    public void update() {
        GCanvas.gameScr.update();
        int i = cmy;
        int i2 = cmtoY;
        if (i != i2) {
            int i3 = (i2 - i) << 2;
            cmvy = i3;
            int i4 = cmdy + i3;
            cmy = i + (i4 >> 4);
            cmdy = i4 & 15;
        }
        if (Math.abs(i2 - cmy) < 15 && cmy < 0) {
            cmtoY = 0;
        }
        if (Math.abs(cmtoY - cmy) < 10) {
            int i5 = cmy;
            int i6 = cmyLim;
            if (i5 > i6) {
                cmtoY = i6;
            }
        }
        super.update();
    }

    @Override
    public void updateKey() {
        if (GCanvas.isPointer(GCanvas.hw - 120, GCanvas.hh - 72, this.numW * this.wSmall, this.hSmall * 6) && !GCanvas.menu.showMenu) {
            int i = GCanvas.pyLast - GCanvas.py;
            int i2 = GCanvas.pxLast - GCanvas.px;
            if (GCanvas.isPointerDown) {
                if (!this.trans) {
                    this.pb = cmy;
                    this.trans = true;
                }
                if (Math.abs(i) > 10) {
                    int i3 = this.pb + (GCanvas.pyLast - GCanvas.py);
                    cmtoY = i3;
                    if (i3 < -50) {
                        cmtoY = -50;
                    }
                    int i4 = cmtoY;
                    int i5 = cmyLim;
                    if (i4 > i5 + 50) {
                        cmtoY = i5 + 50;
                    }
                }
            }
            if (GCanvas.isPointerClick) {
                GCanvas.isPointerClick = false;
                this.isClick = true;
                this.trans = false;
                int i6 = ((cmtoY + GCanvas.py) - (GCanvas.hh - 72)) / this.hSmall;
                int i7 = (GCanvas.px - (GCanvas.hw - 120)) / this.wSmall;
                if (Math.abs(i) < 10 && Math.abs(i2) < 10) {
                    int i8 = this.selected;
                    int i9 = this.numW;
                    if (i8 == (i6 * i9) + i7) {
                        GCanvas.startYesNoDlg("Bạn có muốn mua số " + ((int) this.arrNum[this.selected]) + " này không?", new IAction() {
                            @Override
                            public void perform() {
                                GCanvas.gameScr.gameService.lottery(TroChoiOChu.this.arrNum[TroChoiOChu.this.selected]);
                                GCanvas.endDlg();
                            }
                        });
                    } else {
                        int i10 = (i6 * i9) + i7;
                        this.selected = i10;
                        int i11 = maxOChu;
                        if (i10 > i11 - 1) {
                            this.selected = i11 - 1;
                        }
                    }
                }
            }
        }
        super.updateKey();
    }

    @Override
    public void paint(Graphics graphics) {
        StringBuilder sb;
        byte b;
        GCanvas.gameScr.paint(graphics);
        Res.paintDlgDragonFull(graphics, GCanvas.hw - 130, GCanvas.hh - 82, 260, 164);
        graphics.translate(GCanvas.hw - 120, GCanvas.hh - 72);
        graphics.setClip(0, 0, (this.numW * this.wSmall) + 1, (this.hSmall * 6) + 1);
        graphics.ClipRec(0, 0, (this.numW * this.wSmall) + 1, (this.hSmall * 6) + 1);
        graphics.translate(0, -cmy);
        int i = cmy / this.hSmall;
        if (i < 0) {
            i = 0;
        }
        while (i < this.numH) {
            for (int i2 = 0; i2 < this.numW; i2++) {
                paintGrid(graphics, this.wSmall * i2, this.hSmall * i);
            }
            i++;
        }
        if (!GCanvas.isPointerDown && this.isClick && Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
            graphics.setColor(-6181426);
            int i3 = this.selected;
            int i4 = this.numW;
            int i5 = this.wSmall;
            int i6 = ((i3 % i4) * i5) + 2;
            int i7 = i3 / i4;
            int i8 = this.hSmall;
            graphics.fillRect(i6, (i7 * i8) + 2, i5 - 3, i8 - 3);
        }
        for (int i9 = 0; i9 < this.arrNum.length; i9++) {
            Font font = Font.normalFont[1];
            if (this.arrNum[i9] <= 9) {
                sb = new StringBuilder("0");
                b = this.arrNum[i9];
            } else {
                sb = new StringBuilder("");
                b = this.arrNum[i9];
            }
            String string = sb.append((int) b).toString();
            int i10 = this.numW;
            font.drawString(graphics, string, ((i9 % i10) * this.wSmall) + 13, ((i9 / i10) * this.hSmall) + 6, 2);
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        super.paint(graphics);
    }

    private void paintGrid(Graphics graphics, int i, int i2) {
        graphics.setColor(-10263709);
        graphics.drawRect(i, i2, this.wSmall, this.hSmall);
        graphics.setColor(-8093054);
        graphics.drawRect(i + 1, i2 + 1, this.wSmall - 2, this.hSmall - 2);
    }
}
