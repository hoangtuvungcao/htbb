package game.render.screen;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.Command;
import game.model.Cout;
import game.model.FilePack;
import game.model.FrameImage;
import game.model.IAction;
import game.model.ItemLuckyDraw;
import game.model.Point;
import game.model.Position;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Util;
import java.util.Random;
import java.util.Vector;

public class DialLuckyScr extends Screen {
    private static DialLuckyScr me;
    private Command cmdClose;
    private Command cmdContinue;
    private Command cmdDial;
    private Command cmdErr;
    private Command cmdReChose;
    private Command cmdWait;
    private int cout;
    public int degree;
    public int degreeKim;
    public int g;
    private int hBorder;
    private Bitmap imgCau_back;
    private Bitmap imgDauHoi;
    private Bitmap imgDo;
    private FrameImage imgFireWork;
    private boolean[] isFireWork;
    private boolean isPaint;
    public boolean isStart;
    private boolean isTurn;
    public Screen lastScr;
    private Vector listFireWork;
    public int num;
    public int part;
    private Position posCenter;
    public int radius;
    public int selectedNumber;
    static int[] posPaintX = {0, 0, 0};
    static int[] posPaintY = {0, 0, 0};
    public static byte[] arrayIndex = null;
    private boolean isable = false;
    private Vector listGift = new Vector();
    private long timePaint = 0;
    Random r = new Random(System.currentTimeMillis());

    public static class Gift {
        public String expire = "";
        public short idPart;
        public int luong;
        public byte type;
        public int x;
        public int xp;
        public int xu;
        public int y;
    }

    int abs(int i) {
        return i < 0 ? -i : i;
    }

    public static DialLuckyScr gI() {
        DialLuckyScr dialLuckyScr = me;
        if (dialLuckyScr != null) {
            return dialLuckyScr;
        }
        DialLuckyScr dialLuckyScr2 = new DialLuckyScr();
        me = dialLuckyScr2;
        return dialLuckyScr2;
    }

    @Override
    public void switchToMe() {
        GCanvas.keyHold[5] = false;
        super.switchToMe();
        doContinue();
        this.left = this.cmdErr;
    }

    public DialLuckyScr() {
        FilePack.init("/qs");
        this.imgDo = FilePack.getImg("8Chars");
        this.imgDauHoi = FilePack.getImg("q");
        this.imgFireWork = FrameImage.init("st", 11, 11);
        this.imgCau_back = FilePack.getImg("wood21");
        FilePack.reset();
        if (GCanvas.w < 200) {
            this.radius = 80;
        } else {
            this.radius = 90;
        }
        this.posCenter = new Position(GCanvas.w, GCanvas.hh + 1);
        this.part = 30;
        this.num = 360 / 30;
        this.cmdDial = new Command("Bắt đầu", new IAction() {
            @Override
            public void perform() {
                DialLuckyScr.this.isable = true;
            }
        });
        this.cmdWait = new Command("Xin chờ", new IAction() {
            @Override
            public void perform() {
            }
        });
        this.cmdClose = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                DialLuckyScr.this.lastScr.switchToMe();
                DialLuckyScr.this.doContinue();
                DialLuckyScr.this.isStart = false;
            }
        });
        this.cmdErr = new Command("", new IAction() {
            @Override
            public void perform() {
            }
        });
        this.cmdReChose = new Command("Chọn lại", new IAction() {
            @Override
            public void perform() {
                DialLuckyScr.this.doContinue();
                ItemQsScreen.gI().switchToMe();
                DialLuckyScr.this.isStart = false;
            }
        });
        this.cmdContinue = new Command("Tiếp tục", new IAction() {
            @Override
            public void perform() {
                DialLuckyScr.this.doContinue();
                DialLuckyScr.this.isStart = false;
                DialLuckyScr dialLuckyScr = DialLuckyScr.this;
                dialLuckyScr.center = dialLuckyScr.cmdDial;
                DialLuckyScr dialLuckyScr2 = DialLuckyScr.this;
                dialLuckyScr2.left = dialLuckyScr2.cmdErr;
                DialLuckyScr dialLuckyScr3 = DialLuckyScr.this;
                dialLuckyScr3.right = dialLuckyScr3.cmdClose;
            }
        });
        this.center = this.cmdDial;
        this.right = this.cmdClose;
        this.degreeKim = 90;
        this.isFireWork = new boolean[3];
        this.listFireWork = new Vector();
        this.hBorder = Font.normalFont[0].getHeight();
    }

    protected void doContinue() {
        this.isPaint = false;
        this.isTurn = false;
        this.center = this.cmdDial;
        this.right = this.cmdClose;
        this.timePaint = 0L;
        this.cout = -1;
        for (int i = 0; i < 3; i++) {
            this.isFireWork[i] = false;
        }
        this.listFireWork.removeAllElements();
        this.listGift.removeAllElements();
        Cout.println("reset Quay so -----");
    }

    @Override
    public void update() {
        Screen screen = this.lastScr;
        if (screen != null) {
            screen.update();
        }
        int i = this.g;
        if (i > 0) {
            int i2 = this.degree - i;
            this.degree = i2;
            if (i2 < 0) {
                this.degree = i2 + 7200;
            }
            if (i < 10) {
                if ((this.degree / 20) % 30 == 0) {
                    this.g = 0;
                }
            } else {
                this.g = i - 1;
            }
        } else if (this.isTurn) {
            stop();
        }
        if (this.cout == 2 && this.listFireWork.size() == 0) {
            getResult();
            this.cout = 3;
        }
        if (this.isPaint) {
            if (this.center == this.cmdWait) {
                int i3 = 0;
                int i4 = 0;
                while (true) {
                    boolean[] zArr = this.isFireWork;
                    if (i3 >= zArr.length) {
                        break;
                    }
                    if (zArr[i3]) {
                        i4++;
                    }
                    i3++;
                }
                if (i4 == 3) {
                    this.center = this.cmdClose;
                    this.left = this.cmdContinue;
                    this.right = this.cmdReChose;
                }
            }
            for (int i5 = 0; i5 < this.listFireWork.size(); i5++) {
                Point point = (Point) this.listFireWork.elementAt(i5);
                point.x += point.g;
                if (point.g > 1 || point.g < -1) {
                    point.g -= point.g / abs(point.g);
                }
                point.y += point.h;
                point.h++;
                point.color++;
                if (point.color > 20) {
                    this.listFireWork.removeElement(point);
                }
            }
            if (arrayIndex != null) {
                for (int i6 = 0; i6 < arrayIndex.length; i6++) {
                    if (!this.isFireWork[i6] && (System.currentTimeMillis() / 100) - this.timePaint > (i6 + 1) * 5) {
                        this.isFireWork[i6] = true;
                        addFire(posPaintX[i6], posPaintY[i6]);
                    }
                }
            }
        }
    }

    private void addFire(int i, int i2) {
        for (int i3 = 0; i3 < 10; i3++) {
            int i4 = i3 % 2 == 0 ? -1 : 1;
            Point point = new Point(i, i2);
            point.color = 0;
            point.g = i4 * (this.r.nextInt(80) / 10);
            point.h = (-this.r.nextInt(70)) / 10;
            this.listFireWork.addElement(point);
        }
    }

    private void stop() {
        int i;
        byte[] bArr = arrayIndex;
        posPaintX = new int[bArr.length];
        posPaintY = new int[bArr.length];
        int i2 = 0;
        this.isTurn = false;
        this.isPaint = true;
        this.isable = false;
        this.timePaint = System.currentTimeMillis() / 100;
        while (i2 < arrayIndex.length) {
            if (i2 == 0) {
                i = 150;
            } else {
                i = i2 == 1 ? 180 : 210;
            }
            int iFixangle = Util.fixangle(i);
            int iCos = (this.radius * Util.cos(iFixangle)) >> 10;
            int i3 = (-(this.radius * Util.sin(iFixangle))) >> 10;
            posPaintX[i2] = this.posCenter.x + iCos;
            posPaintY[i2] = this.posCenter.y + i3;
            i2++;
        }
    }

    @Override
    public void updateKey() {
        if (!this.isPaint) {
            if (GCanvas.collisionCmdBar() == 1) {
                if (GCanvas.isPointerDown) {
                    this.isable = true;
                    GCanvas.keyHold[5] = true;
                }
                if (GCanvas.isPointerRelease) {
                    GCanvas.keyReleased[5] = true;
                }
            }
            if (GCanvas.keyHold[5] && !this.isTurn && this.isable) {
                int i = this.degreeKim;
                if (i < 270) {
                    this.degreeKim = i + 3;
                }
            } else {
                int i2 = this.degreeKim;
                if (i2 > 90 && this.isStart) {
                    this.degreeKim = i2 - 3;
                }
            }
            if (GCanvas.keyReleased[5]) {
                if (this.degreeKim > 90) {
                    start();
                }
                GCanvas.keyReleased[5] = false;
            }
        }
        super.updateKey();
    }

    private void start() {
        if (this.isTurn || !this.isable) {
            return;
        }
        this.selectedNumber = this.degreeKim;
        GameService.gI().onStartLuckyDraw(ItemQsScreen.index);
        this.center = this.cmdWait;
    }

    public void onStart() {
        this.center = this.cmdWait;
        this.g = this.selectedNumber + 10;
        this.isTurn = true;
        this.right = this.cmdErr;
        this.isStart = true;
        GCanvas.endDlg();
    }

    @Override
    public void paint(Graphics graphics) {
        int i;
        int i2;
        ItemLuckyDraw itemLuckyDraw;
        Screen screen = this.lastScr;
        if (screen != null) {
            screen.paint(graphics);
        }
        GCanvas.resetTrans(graphics);
        int i3 = this.degree / 20;
        int i4 = 0;
        int i5 = 0;
        while (true) {
            i = 278;
            i2 = 82;
            if (i5 >= this.num) {
                break;
            }
            int i6 = (this.part * i5) + i3;
            if (i6 > 360) {
                i6 -= 360;
            }
            if (i6 >= 82 && i6 <= 278) {
                int iFixangle = Util.fixangle(i6);
                graphics.drawImage(this.imgCau_back, this.posCenter.x + ((this.radius * Util.cos(iFixangle)) >> 10), this.posCenter.y + ((-(this.radius * Util.sin(iFixangle))) >> 10), 3);
            }
            i5++;
        }
        if (this.isPaint) {
            paintGift(graphics);
        }
        int i7 = 0;
        while (i4 < this.num) {
            int i8 = (this.part * i4) + i3;
            if (i8 > 360) {
                i8 -= 360;
            }
            if (i8 >= i2 && i8 <= i) {
                int iFixangle2 = Util.fixangle(i8);
                int iCos = (this.radius * Util.cos(iFixangle2)) >> 10;
                int i9 = (-(this.radius * Util.sin(iFixangle2))) >> 10;
                long jCurrentTimeMillis = (System.currentTimeMillis() / 100) - this.timePaint;
                if (!this.isPaint || i8 < 150 || i8 > 210) {
                    graphics.drawImage(this.imgDauHoi, this.posCenter.x + iCos, this.posCenter.y + i9, 3);
                } else {
                    int i10 = i7 + 1;
                    int i11 = i10 * 5;
                    if (jCurrentTimeMillis > i11 || jCurrentTimeMillis <= i11 - 5) {
                        i7 = i10;
                    } else {
                        graphics.drawImage(this.imgDauHoi, this.posCenter.x + iCos, this.posCenter.y + i9, 3);
                    }
                }
            }
            i4++;
            i = 278;
            i2 = 82;
        }
        graphics.setColor(-7764089);
        graphics.fillRect(this.posCenter.x - 18, this.posCenter.y - 18, 18, 36);
        Bitmap bitmap = this.imgDo;
        graphics.drawRegion(bitmap, 0, 0, bitmap.getWidth(), this.imgDo.getHeight(), 0, this.posCenter.x, this.posCenter.y, Graphics.BOTTOM | Graphics.RIGHT);
        Bitmap bitmap2 = this.imgDo;
        graphics.drawRegion(bitmap2, 0, 0, bitmap2.getWidth(), this.imgDo.getHeight(), 1, this.posCenter.x, this.posCenter.y, Graphics.TOP | Graphics.RIGHT);
        if (ItemQsScreen.index >= 0 && ItemQsScreen.index < ItemQsScreen.allItemLucky.size() && (itemLuckyDraw = (ItemLuckyDraw) ItemQsScreen.allItemLucky.elementAt(ItemQsScreen.index)) != null) {
            itemLuckyDraw.paint(graphics, (this.posCenter.x - this.imgDo.getWidth()) + 28, this.posCenter.y);
        }
        paintKim(graphics);
        if (this.isPaint || this.g > 0) {
            paintFireWork(graphics);
        }
        super.paint(graphics);
    }

    private void paintFireWork(Graphics graphics) {
        for (int i = 0; i < this.listFireWork.size(); i++) {
            Point point = (Point) this.listFireWork.elementAt(i);
            int i2 = point.color / 5;
            if (i2 < 4) {
                this.imgFireWork.drawFrame(i2, point.x, point.y, 0, 3, graphics);
            }
            int i3 = this.cout + 1;
            this.cout = i3;
            if (i3 >= 2) {
                this.cout = 2;
            }
        }
    }

    private void paintKim(Graphics graphics) {
        int iFixangle = Util.fixangle(this.degreeKim);
        int iCos = (((this.radius / 3) + 2) * Util.cos(iFixangle)) >> 10;
        int i = (-(((this.radius / 3) + 2) * Util.sin(iFixangle))) >> 10;
        int i2 = this.degreeKim;
        int i3 = i2 + 90;
        if (i3 > 360) {
            i3 = i2 - 270;
        }
        int iFixangle2 = Util.fixangle(i3);
        int iCos2 = (Util.cos(iFixangle2) * 6) >> 10;
        int i4 = (-(Util.sin(iFixangle2) * 6)) >> 10;
        int i5 = this.degreeKim;
        int i6 = i5 - 90;
        if (i6 < 0) {
            i6 = i5 + 270;
        }
        int iFixangle3 = Util.fixangle(i6);
        int iCos3 = (Util.cos(iFixangle3) * 6) >> 10;
        int i7 = (-(Util.sin(iFixangle3) * 6)) >> 10;
        graphics.setColor(-2841812);
        graphics.fillTriangle(this.posCenter.x + iCos, this.posCenter.y + i, this.posCenter.x + iCos2, this.posCenter.y + i4, this.posCenter.x + iCos3, this.posCenter.y + i7);
        graphics.fillRoundRect(this.posCenter.x - 6, this.posCenter.y - 6, 12, 12, 12, 12);
    }

    public void getResult() {
        String str = "Bạn nhận được: ";
        if (arrayIndex != null) {
            int i = 0;
            while (i < arrayIndex.length) {
                int i2 = i + 1;
                if ((System.currentTimeMillis() / 100) - this.timePaint > i2 * 5) {
                    str = str + "\n" + ((ItemLuckyDraw) ItemQsScreen.allItemLucky.elementAt(arrayIndex[i])).name;
                }
                i = i2;
            }
        }
        GCanvas.startOKDlg(str);
    }

    private void paintGift(Graphics graphics) {
        if (arrayIndex != null) {
            int i = 0;
            while (i < arrayIndex.length) {
                int i2 = i + 1;
                if ((System.currentTimeMillis() / 100) - this.timePaint > i2 * 5) {
                    ((ItemLuckyDraw) ItemQsScreen.allItemLucky.elementAt(arrayIndex[i])).paint(graphics, posPaintX[i], posPaintY[i]);
                }
                i = i2;
            }
        }
    }
}
