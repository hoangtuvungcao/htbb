package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.model.ItemLuckyDraw;
import game.model.Scroll;
import game.model.ScrollResult;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class ItemQsScreen extends Screen {
    /** 31/10/2026: đang chạy update không chạm thô (ManCu - vùng chạm kiểu chung đã xử lý) */
    boolean boc_update;

    public static ItemQsScreen me;
    public Screen last;
    public static int xBg;
    public static int yBg;
    public static int w;
    public static int h;
    public static int index;
    public static int nH;
    public static byte totalItemSelect = 0;
    public static Vector allItemLucky = new Vector();
    public static int nC = 8;
    public static short total = 32;
    public Scroll mScroll = new Scroll();
    private int sizeCell = 20;

    public static ItemQsScreen gI() {
        if (me != null) {
            return me;
        }
        ItemQsScreen itemQsScreen = new ItemQsScreen();
        me = itemQsScreen;
        return itemQsScreen;
    }

    public void resetData() {
        this.mScroll.clear();
        totalItemSelect = (byte) 0;
        allItemLucky = new Vector();
        total = (short) 32;
        index = 0;
    }

    @Override
    public void paint(Graphics g) {
        if (this.last != null) {
            this.last.paint(g);
        }
        Canvas.resetTrans(g);
        if (game.pokemon.ManCu.bat()) {
            // 31/10/2026: tấm chung - lưới phần thưởng (ô cỡ chạm, chạm = chọn), nút Chi tiết / Quay số
            this.anNutLenh = true;
            Command ct = this.center == null ? null : new Command("Chi tiết", this.center.action);
            int[] c = game.pokemon.ManCu.tam(g, this, "Vòng quay may mắn", game.pokemon.ManCu.viec(this.right), new Command[]{ct, this.left});
            int n = 0;
            while (n < allItemLucky.size() && ((ItemLuckyDraw) allItemLucky.elementAt(n)).isSelect == 1) {
                n++;
            }
            game.pokemon.ChuDe.chu(g, game.pokemon.ChuDe.cat("Chọn một phần thưởng rồi bấm Quay số.", game.pokemon.ChuDe.PHU, c[2] - 8), c[0] + c[2] / 2, c[1],
                    game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU_PHU, 2);
            int hg = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU) + game.pokemon.Bang.cach();
            game.pokemon.ManCu.luoi(g, new int[]{c[0], c[1] + hg, c[2], c[3] - hg}, n, index, new game.pokemon.ManCu.O() {
                public String chu(int i) {
                    return null;
                }

                public void cham(int i) {
                    ItemQsScreen.index = i;
                }

                public void ve(Graphics g, int i, int x, int y, int s) {
                    ((ItemLuckyDraw) allItemLucky.elementAt(i)).paint(g, x + s / 2, y + s / 2);
                }
            }, game.pokemon.Bang.coO());
            return;
        }
        Res.paintDlgDragonFull(g, xBg, yBg, w, h);
        int aa = total;
        this.mScroll.setStyle(aa / nC, this.sizeCell, xBg + 5, yBg + 5, w, h - 5, true, 8);
        this.mScroll.setClip(g, xBg + 5, yBg + 5, w, h - 3);
        int r = index / nC;
        int c = index - (r * nC);
        g.setColor(10595790);
        g.fillRect(xBg + (c * this.sizeCell) + 5, yBg + (r * this.sizeCell) + 5, this.sizeCell, this.sizeCell);
        byte xx = 0;
        byte yy = 0;
        for (int i = 0; i < allItemLucky.size(); i++) {
            ItemLuckyDraw item = (ItemLuckyDraw) allItemLucky.elementAt(i);
            if (item.isSelect != 1) {
                break;
            }
            item.paint(g, xBg + (xx * this.sizeCell) + (this.sizeCell / 2) + 5, yBg + (yy * this.sizeCell) + (this.sizeCell / 2) + 5);
            xx = (byte) (xx + 1);
            if (xx >= nC) {
                xx = 0;
                yy = (byte) (yy + 1);
            }
        }
        for (int i2 = 0; i2 < nH; i2++) {
            for (int j = 0; j < nC; j++) {
                g.setColor(8684162);
                g.drawRect(xBg + (j * this.sizeCell) + 5, yBg + (i2 * this.sizeCell) + 5, this.sizeCell, this.sizeCell);
            }
        }
        super.paint(g);
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
                if (ItemQsScreen.index < ItemQsScreen.allItemLucky.size() && ItemQsScreen.index >= 0) {
                    ItemLuckyDraw item = (ItemLuckyDraw) ItemQsScreen.allItemLucky.elementAt(ItemQsScreen.index);
                    if (item.isSelect == 1) {
                        DialLuckyScr.gI().switchToMe();
                        DialLuckyScr.gI().lastScr = ItemQsScreen.this.last;
                        DialLuckyScr.gI().degreeKim = 90;
                        DialLuckyScr.gI().isStart = false;
                        return;
                    }
                    Canvas.startOKDlg("Vui lòng chọn 1 vật phẩm để quay số.");
                }
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
        if (index < allItemLucky.size() && index >= 0) {
            ItemLuckyDraw item = (ItemLuckyDraw) allItemLucky.elementAt(index);
            if (item.isSelect == 1) {
                Canvas.startOKDlg(item.getDecript());
            }
        }
    }

    public ItemQsScreen() {
        xBg = ((Canvas.w / 2) - 80) - 10;
        yBg = (Canvas.hh - 40) - 10;
        w = 170;
        h = 90;
    }

    @Override
    public void update() {
        if (game.pokemon.ManCu.bat() && !this.boc_update && (Canvas.isPointerClick || Canvas.isPointerDown || Canvas.isPointerRelease)) {
            this.boc_update = true;
            try {
                game.pokemon.ManCu.phimKhongCham(new Runnable() {
                    public void run() {
                        ItemQsScreen.this.update();
                    }
                });
            } finally {
                this.boc_update = false;
            }
            return;
        }
        if (this.last != null) {
            this.last.update();
        }
        if (Canvas.currentDialog == null) {
            ScrollResult r = this.mScroll.updateKey();
            if (r.isDowning || r.isFinish) {
                index = r.selected;
            }
            nH = total / nC;
            this.mScroll.updatecm();
        }
        super.update();
    }

    @Override
    public void updateKey() {
        if (Canvas.keyPressed[2]) {
            Canvas.keyPressed[2] = false;
            if (index >= 0 && index < nC) {
                index = 0;
            } else if (index - nC >= 0) {
                index -= nC;
            }
            this.mScroll.moveTo((index / nC) * this.sizeCell);
        } else if (Canvas.keyPressed[8]) {
            Canvas.keyPressed[8] = false;
            if (index + nC <= total - 1) {
                index += nC;
            }
            this.mScroll.moveTo((index / nC) * this.sizeCell);
        } else if (Canvas.keyPressed[4]) {
            Canvas.keyPressed[4] = false;
            index--;
            if (index < 0) {
                index = total - 1;
            }
            this.mScroll.moveTo((index / nC) * this.sizeCell);
        } else if (Canvas.keyPressed[6]) {
            Canvas.keyPressed[6] = false;
            index++;
            if (index > total) {
                index = 0;
            }
            this.mScroll.moveTo((index / nC) * this.sizeCell);
        }
        super.updateKey();
    }
}
