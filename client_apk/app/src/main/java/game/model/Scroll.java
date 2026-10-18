package game.model;

import game.core.j2me.Graphics;
import game.render.GCanvas;

public class Scroll {
    public static Scroll me;
    public boolean FocusNew;
    public int ITEM_PER_LINE;
    public int ITEM_SIZE;
    private int cmRun;
    public int cmdx;
    public int cmdy;
    public int cmtoX;
    public int cmtoY;
    public int cmvx;
    public int cmvy;
    public int cmx;
    public int cmxLim;
    public int cmy;
    public int cmyLim;
    public int height;
    public boolean isDownWhenRunning;
    public int nITEM;
    private int pointerDownFirstX;
    private int pointerDownTime;
    public boolean pointerIsDowning;
    public int selectedItem;
    public int width;
    public int xPos;
    public int yPos;
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

    public void setClip(Graphics graphics, int i, int i2, int i3, int i4) {
        graphics.setClip(i, i2, i3, i4 - 1);
        graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
        graphics.translate(-this.cmx, -this.cmy);
    }

    public void setClip(Graphics graphics) {
        graphics.setClip(this.xPos, this.yPos, this.width, this.height - 1);
        graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
        graphics.translate(-this.cmx, -this.cmy);
    }

    public ScrollResult updateKey() {
        if (this.styleUPDOWN) {
            return updateKeyScrollUpDown();
        }
        return updateKeyScrollLeftRight();
    }

    private ScrollResult updateKeyScrollUpDown() {
        int i = this.xPos;
        int i2 = this.yPos;
        int i3 = this.width;
        int i4 = this.height;
        boolean z = true;
        boolean z2 = false;
        if (GCanvas.isPointerDownNj) {
            if (!this.pointerIsDowning && GCanvas.isPointerNj(i, i2, i3, i4)) {
                int i5 = 0;
                while (true) {
                    int[] iArr = this.pointerDownLastX;
                    if (i5 >= iArr.length) {
                        break;
                    }
                    iArr[0] = GCanvas.py;
                    i5++;
                }
                this.pointerDownFirstX = GCanvas.py;
                this.pointerIsDowning = true;
                this.selectedItem = -1;
                this.isDownWhenRunning = this.cmRun != 0;
                this.cmRun = 0;
            } else if (this.pointerIsDowning) {
                int i6 = this.pointerDownTime + 1;
                this.pointerDownTime = i6;
                if (i6 > 5 && this.pointerDownFirstX == GCanvas.py && !this.isDownWhenRunning) {
                    this.pointerDownFirstX = -1000;
                    if (this.ITEM_PER_LINE > 1) {
                        this.selectedItem = ((((this.cmtoY + GCanvas.py) - i2) / this.ITEM_SIZE) * this.ITEM_PER_LINE) + (((this.cmtoX + GCanvas.px) - i) / this.ITEM_SIZE);
                    } else {
                        this.selectedItem = ((this.cmtoY + GCanvas.py) - i2) / this.ITEM_SIZE;
                    }
                }
                int i7 = GCanvas.py;
                int[] iArr2 = this.pointerDownLastX;
                int i8 = i7 - iArr2[0];
                if (i8 != 0 && this.selectedItem != -1) {
                    this.selectedItem = -1;
                }
                for (int length = iArr2.length - 1; length > 0; length--) {
                    int[] iArr3 = this.pointerDownLastX;
                    iArr3[length] = iArr3[length - 1];
                }
                this.pointerDownLastX[0] = GCanvas.py;
                int i9 = this.cmtoY - i8;
                this.cmtoY = i9;
                if (i9 < 0) {
                    this.cmtoY = 0;
                }
                int i10 = this.cmtoY;
                int i11 = this.cmyLim;
                if (i10 > i11) {
                    this.cmtoY = i11;
                }
                int i12 = this.cmy;
                if (i12 < 0 || i12 > i11) {
                    i8 /= 2;
                }
                this.cmy = i12 - i8;
            }
        }
        if (GCanvas.isPointerJustRelease && this.pointerIsDowning) {
            int i13 = GCanvas.py - this.pointerDownLastX[0];
            GCanvas.isPointerJustRelease = false;
            if (Math.abs(i13) < 20 && Math.abs(GCanvas.py - this.pointerDownFirstX) < 20 && !this.isDownWhenRunning) {
                this.cmRun = 0;
                int i14 = this.cmy;
                this.cmtoY = i14;
                this.pointerDownFirstX = -1000;
                if (this.ITEM_PER_LINE > 1) {
                    this.selectedItem = ((((i14 + GCanvas.py) - i2) / this.ITEM_SIZE) * this.ITEM_PER_LINE) + (((this.cmtoX + GCanvas.px) - i) / this.ITEM_SIZE);
                } else {
                    this.selectedItem = ((i14 + GCanvas.py) - i2) / this.ITEM_SIZE;
                }
                this.pointerDownTime = 0;
            } else {
                int i15 = this.selectedItem;
                if (i15 != -1 && this.pointerDownTime > 5) {
                    this.pointerDownTime = 0;
                } else {
                    if (i15 == -1 && !this.isDownWhenRunning) {
                        int i16 = this.cmy;
                        if (i16 < 0) {
                            this.cmtoY = 0;
                        } else {
                            int i17 = this.cmyLim;
                            if (i16 > i17) {
                                this.cmtoY = i17;
                            } else {
                                int i18 = GCanvas.py;
                                int[] iArr4 = this.pointerDownLastX;
                                int i19 = iArr4[0];
                                int i20 = iArr4[1];
                                int i21 = (i18 - i19) + (i19 - i20) + (i20 - iArr4[2]);
                                int i22 = 10;
                                if (i21 <= 10) {
                                    i22 = -10;
                                    if (i21 >= -10) {
                                        i22 = 0;
                                    }
                                }
                                this.cmRun = (-i22) * 100;
                            }
                        }
                    }
                    z = false;
                }
            }
            this.pointerIsDowning = false;
            this.pointerDownTime = 0;
            GCanvas.isPointerJustRelease = false;
            z2 = z;
        }
        ScrollResult scrollResult = new ScrollResult();
        scrollResult.selected = this.selectedItem;
        scrollResult.isFinish = z2;
        scrollResult.isDowning = this.pointerIsDowning;
        return scrollResult;
    }

    private ScrollResult updateKeyScrollLeftRight() {
        int i = this.xPos;
        int i2 = this.yPos;
        int i3 = this.width;
        int i4 = this.height;
        boolean z = true;
        boolean z2 = false;
        if (GCanvas.isPointerDownNj) {
            if (!this.pointerIsDowning && GCanvas.isPointerNj(i, i2, i3, i4)) {
                int i5 = 0;
                while (true) {
                    int[] iArr = this.pointerDownLastX;
                    if (i5 >= iArr.length) {
                        break;
                    }
                    iArr[0] = GCanvas.px;
                    i5++;
                }
                this.pointerDownFirstX = GCanvas.px;
                this.pointerIsDowning = true;
                this.selectedItem = -1;
                this.isDownWhenRunning = this.cmRun != 0;
                this.cmRun = 0;
            } else if (this.pointerIsDowning) {
                int i6 = this.pointerDownTime + 1;
                this.pointerDownTime = i6;
                if (i6 > 5 && this.pointerDownFirstX == GCanvas.px && !this.isDownWhenRunning) {
                    this.pointerDownFirstX = -1000;
                    this.selectedItem = ((this.cmtoX + GCanvas.px) - i) / this.ITEM_SIZE;
                }
                int i7 = GCanvas.px;
                int[] iArr2 = this.pointerDownLastX;
                int i8 = i7 - iArr2[0];
                if (i8 != 0 && this.selectedItem != -1) {
                    this.selectedItem = -1;
                }
                for (int length = iArr2.length - 1; length > 0; length--) {
                    int[] iArr3 = this.pointerDownLastX;
                    iArr3[length] = iArr3[length - 1];
                }
                this.pointerDownLastX[0] = GCanvas.px;
                int i9 = this.cmtoX - i8;
                this.cmtoX = i9;
                if (i9 < 0) {
                    this.cmtoX = 0;
                }
                int i10 = this.cmtoX;
                int i11 = this.cmxLim;
                if (i10 > i11) {
                    this.cmtoX = i11;
                }
                int i12 = this.cmx;
                if (i12 < 0 || i12 > i11) {
                    i8 /= 2;
                }
                this.cmx = i12 - i8;
            }
        }
        if (GCanvas.isPointerJustRelease && this.pointerIsDowning) {
            int i13 = GCanvas.px - this.pointerDownLastX[0];
            GCanvas.isPointerJustRelease = false;
            if (Math.abs(i13) < 20 && Math.abs(GCanvas.px - this.pointerDownFirstX) < 20 && !this.isDownWhenRunning) {
                this.cmRun = 0;
                int i14 = this.cmx;
                this.cmtoX = i14;
                this.pointerDownFirstX = -1000;
                this.selectedItem = ((i14 + GCanvas.px) - i) / this.ITEM_SIZE;
                this.pointerDownTime = 0;
            } else {
                int i15 = this.selectedItem;
                if (i15 != -1 && this.pointerDownTime > 5) {
                    this.pointerDownTime = 0;
                } else {
                    if (i15 == -1 && !this.isDownWhenRunning) {
                        int i16 = this.cmx;
                        if (i16 < 0) {
                            this.cmtoX = 0;
                        } else {
                            int i17 = this.cmxLim;
                            if (i16 > i17) {
                                this.cmtoX = i17;
                            } else {
                                int i18 = GCanvas.px;
                                int[] iArr4 = this.pointerDownLastX;
                                int i19 = iArr4[0];
                                int i20 = iArr4[1];
                                int i21 = (i18 - i19) + (i19 - i20) + (i20 - iArr4[2]);
                                int i22 = 10;
                                if (i21 <= 10) {
                                    i22 = -10;
                                    if (i21 >= -10) {
                                        i22 = 0;
                                    }
                                }
                                this.cmRun = (-i22) * 100;
                            }
                        }
                    }
                    z = false;
                }
            }
            this.pointerIsDowning = false;
            this.pointerDownTime = 0;
            GCanvas.isPointerJustRelease = false;
            z2 = z;
        }
        ScrollResult scrollResult = new ScrollResult();
        scrollResult.selected = this.selectedItem;
        scrollResult.isFinish = z2;
        scrollResult.isDowning = this.pointerIsDowning;
        return scrollResult;
    }

    public void updatecm() {
        if (GCanvas.isPointerNj(this.xPos, this.yPos, this.width, this.height) && GCanvas.canTouch() && !this.FocusNew) {
            this.FocusNew = true;
        }
        int i = this.cmRun;
        if (i != 0 && !this.pointerIsDowning) {
            if (this.styleUPDOWN) {
                int i2 = this.cmtoY + (i / 100);
                this.cmtoY = i2;
                if (i2 < 0) {
                    this.cmtoY = 0;
                } else {
                    int i3 = this.cmyLim;
                    if (i2 > i3) {
                        this.cmtoY = i3;
                    } else {
                        this.cmy = i2;
                    }
                }
            } else {
                int i4 = this.cmtoX + (i / 100);
                this.cmtoX = i4;
                if (i4 < 0) {
                    this.cmtoX = 0;
                } else {
                    int i5 = this.cmxLim;
                    if (i4 > i5) {
                        this.cmtoX = i5;
                    } else {
                        this.cmx = i4;
                    }
                }
            }
            int i6 = (i * 9) / 10;
            this.cmRun = i6;
            if (i6 < 100 && i6 > -100) {
                this.cmRun = 0;
            }
        }
        int i7 = this.cmx;
        int i8 = this.cmtoX;
        if (i7 != i8 && !this.pointerIsDowning) {
            int i9 = (i8 - i7) << 2;
            this.cmvx = i9;
            int i10 = this.cmdx + i9;
            this.cmx = i7 + (i10 >> 4);
            this.cmdx = i10 & 15;
        }
        int i11 = this.cmy;
        int i12 = this.cmtoY;
        if (i11 == i12 || this.pointerIsDowning) {
            return;
        }
        int i13 = (i12 - i11) << 2;
        this.cmvy = i13;
        int i14 = this.cmdy + i13;
        this.cmy = i11 + (i14 >> 4);
        this.cmdy = i14 & 15;
    }

    public void setStyle(int i, int i2, int i3, int i4, int i5, int i6, boolean z, int i7) {
        this.xPos = i3;
        this.yPos = i4;
        this.ITEM_SIZE = i2;
        this.nITEM = i;
        this.width = i5;
        this.height = i6;
        this.styleUPDOWN = z;
        this.ITEM_PER_LINE = i7;
        if (z) {
            this.cmyLim = (i * i2) - i6;
        } else {
            this.cmxLim = (i * i2) - i5;
        }
        if (this.cmyLim < 0) {
            this.cmyLim = 0;
        }
        if (this.cmxLim < 0) {
            this.cmxLim = 0;
        }
    }

    public void moveTo(int i) {
        if (this.styleUPDOWN) {
            int i2 = i - ((this.height - this.ITEM_SIZE) / 2);
            this.cmtoY = i2;
            if (i2 < 0) {
                this.cmtoY = 0;
            }
            int i3 = this.cmtoY;
            int i4 = this.cmyLim;
            if (i3 > i4) {
                this.cmtoY = i4;
                return;
            }
            return;
        }
        int i5 = i - ((this.width - this.ITEM_SIZE) / 2);
        this.cmtoX = i5;
        if (i5 < 0) {
            this.cmtoX = 0;
        }
        int i6 = this.cmtoX;
        int i7 = this.cmxLim;
        if (i6 > i7) {
            this.cmtoX = i7;
        }
    }

    public static Scroll gI() {
        if (me == null) {
            me = new Scroll();
        }
        return me;
    }
}
