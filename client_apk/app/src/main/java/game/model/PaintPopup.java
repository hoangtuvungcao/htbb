package game.model;

import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.Screen;
import java.util.Vector;

public class PaintPopup extends Screen {
    public static int cmdy;
    public static int cmtoY;
    public static int cmvy;
    public static int cmy;
    public static int cmyLim;
    private static PaintPopup me;
    public Vector cmd;
    private int h;
    private Screen lastScr;
    private int numW;
    private int pb;
    public int selected;
    private String title;
    private boolean trans;
    private int w;
    private int wOne;
    private int x;
    private int x0;
    private int y;
    private int y0;

    public static PaintPopup gI() {
        PaintPopup paintPopup = me;
        if (paintPopup != null) {
            return paintPopup;
        }
        PaintPopup paintPopup2 = new PaintPopup();
        me = paintPopup2;
        return paintPopup2;
    }

    @Override
    public void switchToMe() {
        this.lastScr = GCanvas.currentScreen;
        super.switchToMe();
    }

    public PaintPopup() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                PaintPopup.this.cmd.removeAllElements();
                PaintPopup.this.title = null;
                PaintPopup.this.lastScr = null;
                GCanvas.gameScr.switchToMe();
            }
        });
        this.center = new Command("Chọn", new IAction() {
            @Override
            public void perform() {
                ((Command) PaintPopup.this.cmd.elementAt(PaintPopup.this.selected)).action.perform();
            }
        });
    }

    public void setPos(int i, int i2, int i3, int i4, int i5) {
        this.x = i;
        this.y = i2;
        this.w = i3;
        this.h = i4;
        this.numW = i5;
        int i6 = (i3 - 6) / i5;
        this.wOne = i6;
        int i7 = ((i3 - 6) - (i6 * i5)) / 2;
        this.y0 = i7;
        this.x0 = i7;
    }

    public void setInfo(Vector vector, String str) {
        this.title = str;
        this.cmd = vector;
        int size = (((vector.size() / this.numW) + (vector.size() % this.numW != 0 ? 1 : 0)) * this.wOne) - (this.h - 32);
        cmyLim = size;
        if (size < 0) {
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
    }

    @Override
    public void updateKey() {
        touchCatch(false);
        super.updateKey();
    }

    private void touchCatch(boolean z) {
        if (GCanvas.isPointer(this.x, this.y, this.w, this.h)) {
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
                    if (i3 < 0) {
                        cmtoY = 0;
                    }
                    int i4 = cmtoY;
                    int i5 = cmyLim;
                    if (i4 > i5) {
                        cmtoY = i5;
                    }
                }
                int i6 = ((cmtoY + GCanvas.py) - (this.y + this.numW)) / this.wOne;
                int i7 = (i6 * this.numW) + ((GCanvas.px - this.x) / this.wOne);
                this.selected = i7;
                if (i7 < 0) {
                    this.selected = 0;
                }
                if (this.selected >= this.cmd.size()) {
                    this.selected = this.cmd.size() - 1;
                }
            }
            if (GCanvas.isPointerClick) {
                GCanvas.isPointerClick = false;
                this.trans = false;
                if (Math.abs(i) < 10 && Math.abs(i2) < 10 && this.center != null) {
                    this.center.action.perform();
                }
            }
            int i8 = cmy;
            int i9 = cmtoY;
            if (i8 != i9) {
                int i10 = (i9 - i8) << 2;
                cmvy = i10;
                int i11 = cmdy + i10;
                cmy = i8 + (i11 >> 4);
                cmdy = i11 & 15;
            }
        }
    }

    @Override
    public void paint(Graphics graphics) {
        GCanvas.gameScr.paint(graphics);
        GCanvas.resetTrans(graphics);
        Res.paintDlgFull(graphics, this.x, this.y, this.w, this.h);
        Font.normalFont[0].drawString(graphics, this.title, this.x + (this.w / 2), this.y + 10, 2);
        graphics.translate(this.x + 3 + this.x0, this.y + 26 + this.y0);
        graphics.setClip(0, 0, this.w - (this.x0 * 2), (this.h - 30) - this.y0);
        graphics.ClipRec(0, 0, this.w - (this.x0 * 2), (this.h - 30) - this.y0);
        graphics.translate(0, -cmy);
        int i = this.selected;
        int i2 = this.numW;
        int i3 = this.wOne;
        Res.paintSelected(graphics, (i % i2) * i3, (i / i2) * i3, i3, i3);
        int i4 = cmy;
        int i5 = this.wOne;
        int i6 = this.numW;
        int i7 = (i4 / i5) * i6;
        int size = ((((this.h - 31) / i5) + 2) * i6) + i7;
        if (size > this.cmd.size()) {
            size = this.cmd.size();
        }
        while (i7 < size) {
            Command command = (Command) this.cmd.elementAt(i7);
            graphics.setColor(Res.color[0]);
            int i8 = this.numW;
            int i9 = this.wOne;
            graphics.drawRect((i7 % i8) * i9, (i7 / i8) * i9, i9, i9);
            int i10 = this.numW;
            int i11 = this.wOne;
            command.paint(graphics, ((i7 % i10) * i11) + (i11 / 2), ((i7 / i10) * i11) + (i11 / 2));
            i7++;
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        super.paint(graphics);
    }
}
