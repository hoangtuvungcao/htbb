package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Command;
import game.model.IAction;
import game.model.ItemLuckyDraw;
import game.model.Scroll;
import game.model.ScrollResult;
import game.render.GCanvas;
import game.render.Res;
import java.util.Vector;

public class ItemQsScreen extends Screen {
    public static int h;
    public static int index;
    public static ItemQsScreen me;
    public static int nH;
    public static byte totalItemSelect;
    public static int w;
    public static int xBg;
    public static int yBg;
    public Screen last;
    public Scroll mScroll = new Scroll();
    private int sizeCell = 20;
    public static Vector allItemLucky = new Vector();
    public static int nC = 8;
    public static short total = 32;

    public static ItemQsScreen gI() {
        ItemQsScreen itemQsScreen = me;
        if (itemQsScreen != null) {
            return itemQsScreen;
        }
        ItemQsScreen itemQsScreen2 = new ItemQsScreen();
        me = itemQsScreen2;
        return itemQsScreen2;
    }

    public void resetData() {
        this.mScroll.clear();
        totalItemSelect = (byte) 0;
        allItemLucky = new Vector();
        total = (short) 32;
        index = 0;
    }

    @Override
    public void paint(Graphics graphics) {
        Screen screen = this.last;
        if (screen != null) {
            screen.paint(graphics);
        }
        GCanvas.resetTrans(graphics);
        Res.paintDlgDragonFull(graphics, xBg, yBg, w, h);
        this.mScroll.setStyle(total / nC, this.sizeCell, xBg + 5, yBg + 5, w, h - 5, true, 8);
        this.mScroll.setClip(graphics, xBg + 5, yBg + 5, w, h - 3);
        int i = index;
        int i2 = nC;
        int i3 = i / i2;
        int i4 = i - (i2 * i3);
        graphics.setColor(-6181426);
        int i5 = xBg;
        int i6 = this.sizeCell;
        graphics.fillRect(i5 + (i4 * i6) + 5, yBg + (i3 * i6) + 5, i6, i6);
        byte b = 0;
        byte b2 = 0;
        for (int i7 = 0; i7 < allItemLucky.size(); i7++) {
            ItemLuckyDraw itemLuckyDraw = (ItemLuckyDraw) allItemLucky.elementAt(i7);
            if (itemLuckyDraw.isSelect != 1) {
                break;
            }
            int i8 = xBg;
            int i9 = this.sizeCell;
            itemLuckyDraw.paint(graphics, i8 + (b * i9) + (i9 / 2) + 5, yBg + (b2 * i9) + (i9 / 2) + 5);
            b = (byte) (b + 1);
            if (b >= nC) {
                b2 = (byte) (b2 + 1);
                b = 0;
            }
        }
        for (int i10 = 0; i10 < nH; i10++) {
            for (int i11 = 0; i11 < nC; i11++) {
                graphics.setColor(-8093054);
                int i12 = xBg;
                int i13 = this.sizeCell;
                graphics.drawRect(i12 + (i11 * i13) + 5, yBg + (i10 * i13) + 5, i13, i13);
            }
        }
        super.paint(graphics);
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
        this.center = new Command("Chọn", new IAction() {
            @Override
            public void perform() {
                ItemQsScreen.this.doselect();
            }
        });
        this.left = new Command("Quay số", new IAction() {
            @Override
            public void perform() {
                if (ItemQsScreen.index >= ItemQsScreen.allItemLucky.size() || ItemQsScreen.index < 0) {
                    return;
                }
                if (((ItemLuckyDraw) ItemQsScreen.allItemLucky.elementAt(ItemQsScreen.index)).isSelect == 1) {
                    DialLuckyScr.gI().switchToMe();
                    DialLuckyScr.gI().lastScr = ItemQsScreen.this.last;
                    DialLuckyScr.gI().degreeKim = 90;
                    DialLuckyScr.gI().isStart = false;
                    return;
                }
                GCanvas.startOKDlg("Vui lòng chọn 1 vật phẩm để quay số.");
            }
        });
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                ItemQsScreen.this.resetData();
                ItemQsScreen.this.last.switchToMe();
            }
        });
        this.mScroll.clear();
    }

    protected void doselect() {
        int i;
        if (index >= allItemLucky.size() || (i = index) < 0) {
            return;
        }
        ItemLuckyDraw itemLuckyDraw = (ItemLuckyDraw) allItemLucky.elementAt(i);
        if (itemLuckyDraw.isSelect == 1) {
            GCanvas.startOKDlg(itemLuckyDraw.getDecript());
        }
    }

    public ItemQsScreen() {
        xBg = (GCanvas.w / 2) - 90;
        yBg = GCanvas.hh - 50;
        w = 170;
        h = 90;
    }

    @Override
    public void update() {
        Screen screen = this.last;
        if (screen != null) {
            screen.update();
        }
        if (GCanvas.currentDialog == null) {
            ScrollResult scrollResultUpdateKey = this.mScroll.updateKey();
            if (scrollResultUpdateKey.isDowning || scrollResultUpdateKey.isFinish) {
                index = scrollResultUpdateKey.selected;
            }
            nH = total / nC;
            this.mScroll.updatecm();
        }
        super.update();
    }

    @Override
    public void updateKey() {
        if (GCanvas.keyPressed[2]) {
            GCanvas.keyPressed[2] = false;
            int i = index;
            if (i >= 0 && i < nC) {
                index = 0;
            } else {
                int i2 = nC;
                if (i - i2 >= 0) {
                    index = i - i2;
                }
            }
            this.mScroll.moveTo((index / nC) * this.sizeCell);
        } else if (GCanvas.keyPressed[8]) {
            GCanvas.keyPressed[8] = false;
            int i3 = index;
            int i4 = nC;
            if (i3 + i4 <= total - 1) {
                index = i3 + i4;
            }
            this.mScroll.moveTo((index / i4) * this.sizeCell);
        } else if (GCanvas.keyPressed[4]) {
            GCanvas.keyPressed[4] = false;
            int i5 = index - 1;
            index = i5;
            if (i5 < 0) {
                index = total - 1;
            }
            this.mScroll.moveTo((index / nC) * this.sizeCell);
        } else if (GCanvas.keyPressed[6]) {
            int i6 = index + 1;
            index = i6;
            if (i6 > total) {
                index = 0;
            }
            this.mScroll.moveTo((index / nC) * this.sizeCell);
        }
        super.updateKey();
    }
}
