package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Command;
import game.model.IAction;
import game.model.Main;
import game.render.GCanvas;
import java.util.Vector;

public class Menu extends Main {
    public static int cmdy;
    public static int cmtoY;
    public static int cmvy;
    public static int cmy;
    public static int cmyLim;
    public static int xc;
    public boolean canSelecet;
    int cdyb;
    private int countShowText;
    int ctoyBag;
    int cvyb;
    private int dxTrant;
    private int dyTrant;
    private int hToucht;
    public boolean isBegin;
    public boolean isClose;
    public boolean isPaintFc;
    public int menuH;
    public int menuTemY;
    public int menuW;
    public int menuX;
    public int menuY;
    private int pyLastt;
    public int selected;
    public boolean showMenu;
    long timeDelayt;
    private int timeHold;
    long timeOpent;
    public float vYT;
    private int wToucht;
    private int xBegint;
    private int yBegint;
    public Vector menuItems = new Vector();
    int cyBag = 0;
    int cylim = 0;
    public boolean transYShowText = false;
    long timePointYt = 0;
    int pa = 0;

    public Menu() {
        this.right = new Command("", new IAction() {
            @Override
            public void perform() {
                Menu.this.showMenu = false;
                GCanvas.clearKeyHold();
                GCanvas.clearKeyPressed();
                GCanvas.isPointerDown = false;
            }
        });
        this.center = new Command("", new IAction() {
            @Override
            public void perform() {
                Menu.this.showMenu = false;
                if (Menu.this.selected != -1) {
                    if (GameScr.iTaskAuto == 2) {
                        Menu.this.selected = 0;
                    }
                    if (Math.abs(GCanvas.px - GCanvas.pxLast) >= 10 || Math.abs(GCanvas.py - GCanvas.pyLast) >= 10) {
                        return;
                    }
                    Command command = (Command) Menu.this.menuItems.elementAt(Menu.this.selected);
                    if (Menu.this.isPaintFc) {
                        command.action.perform();
                    }
                }
            }
        });
        this.left = new Command("", new IAction() {
            @Override
            public void perform() {
            }
        });
    }

    public void setIndex() {
        if (this.menuItems != null) {
            int iNextInt = GameScr.r.nextInt(this.menuItems.size());
            this.selected = iNextInt;
            int i = (iNextInt * Screen.ITEM_HEIGHT) - (Screen.ITEM_HEIGHT * 2);
            this.ctoyBag = i;
            if (i < 0) {
                this.ctoyBag = 0;
            }
            int i2 = this.ctoyBag;
            int i3 = this.cylim;
            if (i2 > i3) {
                this.ctoyBag = i3;
            }
        }
    }

    public void startAt(Vector vector, int i) {
        if (vector.size() <= 0) {
            return;
        }
        GCanvas.isPointerRelease = false;
        GCanvas.isPointerDrange = false;
        GCanvas.isPointerDownItem = false;
        GCanvas.isPointerDown = false;
        GCanvas.isPointerClick = false;
        this.selected = GameScr.iTaskAuto == 2 ? 0 : -1;
        this.isPaintFc = false;
        this.menuItems = vector;
        this.menuW = 0;
        this.menuH = 0;
        int size = vector.size();
        for (int i2 = 0; i2 < size; i2++) {
            int width = Font.normalFont[0].getWidth(((Command) vector.elementAt(i2)).caption);
            if (width > this.menuW) {
                this.menuW = width;
            }
            this.menuH += Main.ITEM_HEIGHT;
        }
        int i3 = this.menuW + 60;
        this.menuW = i3;
        if (i3 < 100) {
            this.menuW = 100;
        }
        this.menuH += 5;
        this.menuY = ((GCanvas.h - Main.hTab) - this.menuH) - 3;
        if (vector.size() > 4) {
            this.menuY = ((GCanvas.h - (Main.ITEM_HEIGHT * 4)) - Main.hTab) - 60;
            this.menuH = (Main.ITEM_HEIGHT * 4) + 50;
        }
        this.showMenu = true;
        this.cylim = (this.menuItems.size() * Main.ITEM_HEIGHT) - (Main.ITEM_HEIGHT * 4);
        if (vector.size() > 4) {
            this.cylim = (this.menuItems.size() * Main.ITEM_HEIGHT) - (Main.ITEM_HEIGHT * 5);
        }
        if (this.cylim < 0) {
            this.cylim = 0;
        }
        this.menuW += 30;
        this.cyBag = 0;
        this.ctoyBag = 0;
        xc = 50;
        this.menuTemY = this.menuY;
        if (i == 0) {
            this.menuX = 5;
        } else if (i == 1) {
            this.menuX = (GCanvas.w - this.menuW) - 10;
        } else {
            this.menuX = (GCanvas.w >> 1) - (this.menuW >> 1);
        }
        this.menuH -= 3;
        if (GCanvas.gameScr.posNpcRequest != null && GameScr.iTaskAuto == 2) {
            this.isPaintFc = true;
        }
        GCanvas.clearKeyHold();
        GCanvas.clearKeyPressed();
        GCanvas.endDlg();
    }

    private void setKey(int i) {
        this.isPaintFc = true;
        int i2 = this.selected + i;
        this.selected = i2;
        if (i2 < 0) {
            this.selected = this.menuItems.size() - 1;
        }
        if (this.selected > this.menuItems.size() - 1) {
            this.selected = 0;
        }
        int i3 = (this.selected * Main.ITEM_HEIGHT) - (Main.ITEM_HEIGHT * 2);
        this.ctoyBag = i3;
        if (i3 < 0) {
            this.ctoyBag = 0;
        }
        int i4 = this.ctoyBag;
        int i5 = this.cylim;
        if (i4 > i5) {
            this.ctoyBag = i5;
        }
    }

    public void updateMenuKey() {
        if (GCanvas.isKeyPressed(2)) {
            setKey(-1);
        } else if (GCanvas.isKeyPressed(8)) {
            setKey(1);
        }
        this.xBegint = this.menuX;
        this.yBegint = this.menuY;
        this.wToucht = this.menuW;
        this.hToucht = this.menuH;
        updateKeyShowText();
        super.updateKey();
    }

    public boolean checkClose() {
        if (GCanvas.py > GCanvas.h - 40) {
            return false;
        }
        return GCanvas.px < this.xBegint || GCanvas.px > this.xBegint + this.wToucht || GCanvas.py < this.yBegint || GCanvas.py > this.yBegint + this.hToucht;
    }

    public void updateKeyShowText() {
        this.countShowText++;
        long j = this.timeOpent;
        if (j > 0) {
            long j2 = j - 1;
            this.timeOpent = j2;
            boolean z = this.isClose;
            if (!z) {
                this.isPaintFc = true;
            }
            if (j2 == 0 && this.canSelecet) {
                if (!z) {
                    if (this.selected != -1 && this.center != null) {
                        this.center.action.perform();
                    }
                } else {
                    if (this.right != null) {
                        this.right.action.perform();
                    }
                    this.isClose = false;
                }
                this.canSelecet = false;
                return;
            }
            return;
        }
        if (GCanvas.isPointerDownItem) {
            if (GCanvas.isPointer(this.xBegint, this.yBegint, this.wToucht, this.hToucht)) {
                this.pyLastt = GCanvas.pyLast;
                GCanvas.isPointerDownItem = false;
                this.timeDelayt = this.countShowText;
                this.pa = this.cyBag;
                this.transYShowText = true;
                this.vYT = 0.0f;
            }
            if (checkClose()) {
                this.isClose = true;
                this.isPaintFc = false;
                this.transYShowText = false;
            }
        }
        if (this.transYShowText) {
            long j3 = ((long) this.countShowText) - this.timeDelayt;
            int i = this.pyLastt - GCanvas.py;
            if (!this.isClose) {
                this.pyLastt = GCanvas.py;
                if (GCanvas.isPointerDown) {
                    if (this.countShowText % 2 == 0) {
                        this.dyTrant = GCanvas.py;
                        this.timePointYt = this.countShowText;
                    }
                    this.vYT = 0.0f;
                    int i2 = this.ctoyBag;
                    if (i2 <= 0 || i2 >= this.cylim) {
                        if (GCanvas.beginMoveCmrY()) {
                            this.ctoyBag = this.pa + (GCanvas.dy() / 2);
                        }
                    } else if (GCanvas.beginMoveCmrY()) {
                        int i3 = this.pa + i;
                        this.ctoyBag = i3;
                        this.pa = i3;
                    }
                    this.cyBag = this.ctoyBag;
                    if (GCanvas.canPutKey()) {
                        int i4 = this.timeHold + 1;
                        this.timeHold = i4;
                        if (i4 >= 5) {
                            this.timeHold = 5;
                            this.isPaintFc = true;
                            checkSelect();
                        }
                    } else if (GCanvas.canNotPutKey()) {
                        this.timeHold = 0;
                        this.isPaintFc = false;
                    }
                }
                if (GCanvas.isPointerRelease) {
                    this.transYShowText = false;
                    int i5 = (int) (((long) this.countShowText) - this.timePointYt);
                    float f = this.dyTrant - GCanvas.py;
                    int i6 = GCanvas.px;
                    if (GCanvas.beginMoveCmrY()) {
                        this.vYT = (f / i5) * 10.0f;
                    }
                    this.timePointYt = -1L;
                    if (!GCanvas.canPutKey() || this.timeHold == 5) {
                        if (this.timeHold >= 5) {
                            this.timeHold = 0;
                            this.timeOpent = 5L;
                            this.canSelecet = true;
                            checkSelect();
                            this.isClose = false;
                        }
                    } else if (j3 <= 4) {
                        this.timeHold = 0;
                        this.timeOpent = 5L;
                        this.canSelecet = true;
                        checkSelect();
                        this.isClose = false;
                    }
                }
            }
        }
        if (this.isClose && GCanvas.isPointerRelease) {
            if (GCanvas.canPutKey()) {
                this.timeHold = 0;
                this.timeOpent = 5L;
                this.canSelecet = true;
                this.isPaintFc = false;
            } else {
                this.isClose = false;
            }
        }
        moveCameraShowText();
    }

    public void checkSelect() {
        if (GCanvas.px >= this.xBegint && GCanvas.px <= this.xBegint + this.wToucht && GCanvas.py >= this.yBegint && GCanvas.py <= this.yBegint + this.hToucht) {
            int size = ((this.ctoyBag + GCanvas.py) - (this.menuTemY + 2)) / Main.ITEM_HEIGHT;
            if (size < 0) {
                size = 0;
            }
            if (size > this.menuItems.size() - 1) {
                size = this.menuItems.size() - 1;
            }
            this.selected = size;
            return;
        }
        this.selected = -1;
    }

    public void moveCameraShowText() {
        float f = this.vYT;
        if (f != 0.0f) {
            int i = this.cyBag;
            if (i < 0 || i > this.cylim) {
                if (f > 500.0f) {
                    this.vYT = 500.0f;
                } else if (f < -500.0f) {
                    this.vYT = -500.0f;
                }
                float f2 = this.vYT;
                float f3 = f2 - (f2 / 5.0f);
                this.vYT = f3;
                if (Math.abs((int) (f3 / 10.0f)) <= 10) {
                    this.vYT = 0.0f;
                }
            }
            int i2 = this.cyBag;
            float f4 = this.vYT;
            int i3 = i2 + ((int) (f4 / 15.0f));
            this.cyBag = i3;
            this.ctoyBag = i3;
            this.vYT = f4 - (f4 / 20.0f);
        } else {
            int i4 = this.cyBag;
            if (i4 < 0) {
                this.ctoyBag = 0;
            } else {
                int i5 = this.cylim;
                if (i4 > i5) {
                    this.ctoyBag = i5;
                }
            }
        }
        int i6 = this.cyBag;
        int i7 = this.ctoyBag;
        if (i6 != i7) {
            int i8 = (i7 - i6) << 2;
            this.cvyb = i8;
            int i9 = this.cdyb + i8;
            this.cyBag = i6 + (i9 >> 4);
            this.cdyb = i9 & 15;
        }
    }

    public void paintMenu(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        graphics.setColor(-4868683);
        int i = this.menuItems.size() <= 3 ? 10 : 0;
        graphics.fillRect(this.menuX - 2, this.menuY - 2, this.menuW + 4, this.menuH + 5 + i);
        graphics.setColor(-14595766);
        graphics.fillRect(this.menuX, this.menuTemY, this.menuW, this.menuH + i);
        graphics.setClip(this.menuX - 2, this.menuY, this.menuW + 5, this.menuH + i);
        graphics.ClipRec(this.menuX - 2, this.menuY, this.menuW + 5, this.menuH + i);
        graphics.translate(this.menuX + 2, this.menuTemY);
        for (int i2 = 0; i2 < this.menuItems.size(); i2++) {
            Font font = Font.normalFont[0];
            int i3 = this.selected;
            if (i3 != -1 && i2 == i3 && this.isPaintFc) {
                graphics.setColor(-5282048);
                graphics.fillRect(-2, (((Main.ITEM_HEIGHT * i2) + 10) - this.cyBag) - 4, this.menuW, Main.ITEM_HEIGHT);
                font = Font.normalFont[0];
            }
            font.drawString(graphics, ((Command) this.menuItems.elementAt(i2)).caption, 5, (((Main.ITEM_HEIGHT * i2) + 10) - this.cyBag) + 3, 0);
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        super.paint(graphics);
    }

    public void updateMenu() {
        int i = this.menuTemY;
        int i2 = this.menuY;
        if (i > i2) {
            int i3 = (i - i2) >> 1;
            this.menuTemY = i - (i3 >= 1 ? i3 : 1);
        }
        this.menuTemY = i2;
    }
}
