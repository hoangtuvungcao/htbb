package game.model;

import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

public class Scroll {
    public int cmtoX;
    public int cmtoY;
    public int cmx;
    public int cmy;
    public int cmvx;
    public int cmvy;
    public int cmdx;
    public int cmdy;
    public int xPos;
    public int yPos;
    public int width;
    public int height;
    public int cmxLim;
    public int cmyLim;
    public static Scroll me;
    private int pointerDownTime;
    private int pointerDownFirstX;
    public boolean pointerIsDowning;
    public boolean isDownWhenRunning;
    private int cmRun;
    public int selectedItem;
    public int ITEM_SIZE;
    public int nITEM;
    public int ITEM_PER_LINE;
    public boolean FocusNew;
    public int[] pointerDownLastX = new int[3];
    public boolean styleUPDOWN = true;

    public void clear() {
        this.cmtoX = 0;
        this.cmtoY = 0;
        this.cmx = 0;
        this.cmy = 0;
        this.cmvx = 0;
        this.cmvy = 0;
        this.cmdx = 0;
        this.cmdy = 0;
        this.cmxLim = 0;
        this.cmyLim = 0;
        this.width = 0;
        this.height = 0;
    }

    public void setClip(Graphics g, int x, int y, int w, int h) {
        g.setClip(x, y, w, h - 1);
        g.translate(-g.getTranslateX(), -g.getTranslateY());
        g.translate(-this.cmx, -this.cmy);
    }

    public void setClip(Graphics g) {
        g.setClip(this.xPos, this.yPos, this.width, this.height - 1);
        g.translate(-g.getTranslateX(), -g.getTranslateY());
        g.translate(-this.cmx, -this.cmy);
    }

    public ScrollResult updateKey() {
        if (this.styleUPDOWN) {
            return updateKeyScrollUpDown();
        }
        return updateKeyScrollLeftRight();
    }

    private ScrollResult updateKeyScrollUpDown() {
        int s;
        int xTT = this.xPos;
        int yTT = this.yPos;
        int wTT = this.width;
        int hTT = this.height;
        if (Canvas.isPointerDownNj) {
            if (!this.pointerIsDowning && Canvas.isPointerNj(xTT, yTT, wTT, hTT)) {
                for (int i = 0; i < this.pointerDownLastX.length; i++) {
                    this.pointerDownLastX[0] = Canvas.py;
                }
                this.pointerDownFirstX = Canvas.py;
                this.pointerIsDowning = true;
                this.selectedItem = -1;
                this.isDownWhenRunning = this.cmRun != 0;
                this.cmRun = 0;
            } else if (this.pointerIsDowning) {
                this.pointerDownTime++;
                if (this.pointerDownTime > 5 && this.pointerDownFirstX == Canvas.py && !this.isDownWhenRunning) {
                    this.pointerDownFirstX = -1000;
                    if (this.ITEM_PER_LINE > 1) {
                        int selectedRow = ((this.cmtoY + Canvas.py) - yTT) / this.ITEM_SIZE;
                        int selectedColumn = ((this.cmtoX + Canvas.px) - xTT) / this.ITEM_SIZE;
                        this.selectedItem = (selectedRow * this.ITEM_PER_LINE) + selectedColumn;
                    } else {
                        this.selectedItem = ((this.cmtoY + Canvas.py) - yTT) / this.ITEM_SIZE;
                    }
                }
                int dx = Canvas.py - this.pointerDownLastX[0];
                if (dx != 0 && this.selectedItem != -1) {
                    this.selectedItem = -1;
                }
                for (int i2 = this.pointerDownLastX.length - 1; i2 > 0; i2--) {
                    this.pointerDownLastX[i2] = this.pointerDownLastX[i2 - 1];
                }
                this.pointerDownLastX[0] = Canvas.py;
                this.cmtoY -= dx;
                if (this.cmtoY < 0) {
                    this.cmtoY = 0;
                }
                if (this.cmtoY > this.cmyLim) {
                    this.cmtoY = this.cmyLim;
                }
                if (this.cmy < 0 || this.cmy > this.cmyLim) {
                    dx /= 2;
                }
                this.cmy -= dx;
            }
        }
        boolean isFinish = false;
        if (Canvas.isPointerJustRelease && this.pointerIsDowning) {
            int dx2 = Canvas.py - this.pointerDownLastX[0];
            Canvas.isPointerJustRelease = false;
            if (Math.abs(dx2) < 20 && Math.abs(Canvas.py - this.pointerDownFirstX) < 20 && !this.isDownWhenRunning) {
                this.cmRun = 0;
                this.cmtoY = this.cmy;
                this.pointerDownFirstX = -1000;
                if (this.ITEM_PER_LINE > 1) {
                    int selectedRow2 = ((this.cmtoY + Canvas.py) - yTT) / this.ITEM_SIZE;
                    int selectedColumn2 = ((this.cmtoX + Canvas.px) - xTT) / this.ITEM_SIZE;
                    this.selectedItem = (selectedRow2 * this.ITEM_PER_LINE) + selectedColumn2;
                } else {
                    this.selectedItem = ((this.cmtoY + Canvas.py) - yTT) / this.ITEM_SIZE;
                }
                this.pointerDownTime = 0;
                isFinish = true;
            } else if (this.selectedItem != -1 && this.pointerDownTime > 5) {
                this.pointerDownTime = 0;
                isFinish = true;
            } else if (this.selectedItem == -1 && !this.isDownWhenRunning) {
                if (this.cmy < 0) {
                    this.cmtoY = 0;
                } else if (this.cmy > this.cmyLim) {
                    this.cmtoY = this.cmyLim;
                } else {
                    int s2 = (Canvas.py - this.pointerDownLastX[0]) + (this.pointerDownLastX[0] - this.pointerDownLastX[1]) + (this.pointerDownLastX[1] - this.pointerDownLastX[2]);
                    if (s2 > 10) {
                        s = 10;
                    } else if (s2 < -10) {
                        s = -10;
                    } else {
                        s = 0;
                    }
                    this.cmRun = (-s) * 100;
                }
            }
            this.pointerIsDowning = false;
            this.pointerDownTime = 0;
            Canvas.isPointerJustRelease = false;
        }
        ScrollResult r = new ScrollResult();
        r.selected = this.selectedItem;
        r.isFinish = isFinish;
        r.isDowning = this.pointerIsDowning;
        return r;
    }

    private ScrollResult updateKeyScrollLeftRight() {
        int s;
        int xTT = this.xPos;
        int yTT = this.yPos;
        int wTT = this.width;
        int hTT = this.height;
        if (Canvas.isPointerDownNj) {
            if (!this.pointerIsDowning && Canvas.isPointerNj(xTT, yTT, wTT, hTT)) {
                for (int i = 0; i < this.pointerDownLastX.length; i++) {
                    this.pointerDownLastX[0] = Canvas.px;
                }
                this.pointerDownFirstX = Canvas.px;
                this.pointerIsDowning = true;
                this.selectedItem = -1;
                this.isDownWhenRunning = this.cmRun != 0;
                this.cmRun = 0;
            } else if (this.pointerIsDowning) {
                this.pointerDownTime++;
                if (this.pointerDownTime > 5 && this.pointerDownFirstX == Canvas.px && !this.isDownWhenRunning) {
                    this.pointerDownFirstX = -1000;
                    this.selectedItem = ((this.cmtoX + Canvas.px) - xTT) / this.ITEM_SIZE;
                }
                int dx = Canvas.px - this.pointerDownLastX[0];
                if (dx != 0 && this.selectedItem != -1) {
                    this.selectedItem = -1;
                }
                for (int i2 = this.pointerDownLastX.length - 1; i2 > 0; i2--) {
                    this.pointerDownLastX[i2] = this.pointerDownLastX[i2 - 1];
                }
                this.pointerDownLastX[0] = Canvas.px;
                this.cmtoX -= dx;
                if (this.cmtoX < 0) {
                    this.cmtoX = 0;
                }
                if (this.cmtoX > this.cmxLim) {
                    this.cmtoX = this.cmxLim;
                }
                if (this.cmx < 0 || this.cmx > this.cmxLim) {
                    dx /= 2;
                }
                this.cmx -= dx;
            }
        }
        boolean isFinish = false;
        if (Canvas.isPointerJustRelease && this.pointerIsDowning) {
            int dx2 = Canvas.px - this.pointerDownLastX[0];
            Canvas.isPointerJustRelease = false;
            if (Math.abs(dx2) < 20 && Math.abs(Canvas.px - this.pointerDownFirstX) < 20 && !this.isDownWhenRunning) {
                this.cmRun = 0;
                this.cmtoX = this.cmx;
                this.pointerDownFirstX = -1000;
                this.selectedItem = ((this.cmtoX + Canvas.px) - xTT) / this.ITEM_SIZE;
                this.pointerDownTime = 0;
                isFinish = true;
            } else if (this.selectedItem != -1 && this.pointerDownTime > 5) {
                this.pointerDownTime = 0;
                isFinish = true;
            } else if (this.selectedItem == -1 && !this.isDownWhenRunning) {
                if (this.cmx < 0) {
                    this.cmtoX = 0;
                } else if (this.cmx > this.cmxLim) {
                    this.cmtoX = this.cmxLim;
                } else {
                    int s2 = (Canvas.px - this.pointerDownLastX[0]) + (this.pointerDownLastX[0] - this.pointerDownLastX[1]) + (this.pointerDownLastX[1] - this.pointerDownLastX[2]);
                    if (s2 > 10) {
                        s = 10;
                    } else if (s2 < -10) {
                        s = -10;
                    } else {
                        s = 0;
                    }
                    this.cmRun = (-s) * 100;
                }
            }
            this.pointerIsDowning = false;
            this.pointerDownTime = 0;
            Canvas.isPointerJustRelease = false;
        }
        ScrollResult r = new ScrollResult();
        r.selected = this.selectedItem;
        r.isFinish = isFinish;
        r.isDowning = this.pointerIsDowning;
        return r;
    }

    public void updatecm() {
        int xTT = this.xPos;
        int yTT = this.yPos;
        int wTT = this.width;
        int hTT = this.height;
        if (Canvas.isPointerNj(xTT, yTT, wTT, hTT) && Canvas.canTouch() && !this.FocusNew) {
            this.FocusNew = true;
        }
        if (this.cmRun != 0 && !this.pointerIsDowning) {
            if (this.styleUPDOWN) {
                this.cmtoY += this.cmRun / 100;
                if (this.cmtoY < 0) {
                    this.cmtoY = 0;
                } else if (this.cmtoY > this.cmyLim) {
                    this.cmtoY = this.cmyLim;
                } else {
                    this.cmy = this.cmtoY;
                }
            } else {
                this.cmtoX += this.cmRun / 100;
                if (this.cmtoX < 0) {
                    this.cmtoX = 0;
                } else if (this.cmtoX > this.cmxLim) {
                    this.cmtoX = this.cmxLim;
                } else {
                    this.cmx = this.cmtoX;
                }
            }
            this.cmRun = (this.cmRun * 9) / 10;
            if (this.cmRun < 100 && this.cmRun > -100) {
                this.cmRun = 0;
            }
        }
        if (this.cmx != this.cmtoX && !this.pointerIsDowning) {
            this.cmvx = (this.cmtoX - this.cmx) << 2;
            this.cmdx += this.cmvx;
            this.cmx += this.cmdx >> 4;
            this.cmdx &= 15;
        }
        if (this.cmy != this.cmtoY && !this.pointerIsDowning) {
            this.cmvy = (this.cmtoY - this.cmy) << 2;
            this.cmdy += this.cmvy;
            this.cmy += this.cmdy >> 4;
            this.cmdy &= 15;
        }
    }

    public void setStyle(int nItem, int ITEM_SIZE, int xPos, int yPos, int width, int height, boolean styleUPDOWN, int ITEM_PER_LINE) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.ITEM_SIZE = ITEM_SIZE;
        this.nITEM = nItem;
        this.width = width;
        this.height = height;
        this.styleUPDOWN = styleUPDOWN;
        this.ITEM_PER_LINE = ITEM_PER_LINE;
        if (styleUPDOWN) {
            this.cmyLim = (nItem * ITEM_SIZE) - height;
        } else {
            this.cmxLim = (nItem * ITEM_SIZE) - width;
        }
        if (this.cmyLim < 0) {
            this.cmyLim = 0;
        }
        if (this.cmxLim < 0) {
            this.cmxLim = 0;
        }
    }

    public void moveTo(int to) {
        if (this.styleUPDOWN) {
            this.cmtoY = to - ((this.height - this.ITEM_SIZE) / 2);
            if (this.cmtoY < 0) {
                this.cmtoY = 0;
            }
            if (this.cmtoY > this.cmyLim) {
                this.cmtoY = this.cmyLim;
                return;
            }
            return;
        }
        this.cmtoX = to - ((this.width - this.ITEM_SIZE) / 2);
        if (this.cmtoX < 0) {
            this.cmtoX = 0;
        }
        if (this.cmtoX > this.cmxLim) {
            this.cmtoX = this.cmxLim;
        }
    }

    public static Scroll gI() {
        if (me == null) {
            me = new Scroll();
        }
        return me;
    }
}
