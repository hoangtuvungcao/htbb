package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import game.render.screen.Screen;
import javax.microedition.lcdui.Graphics;

/**
 * Khung chung cho màn dạng bảng (0.9.3): thế giới chạy phía sau (cú chạm không lọt xuống), nền phủ mờ, Bang.khung
 * (tiêu đề + tab + nút đóng), phím trái / phải đổi tab, lên / xuống cuộn, Back / Esc = lui() (mặc định: về màn chơi).
 */
public abstract class ManBang extends Screen {
    protected int tab;
    protected final Cuon ds = new Cuon();

    protected abstract String tieuDe();

    /** null = không tab */
    protected String[] tabs() {
        return null;
    }

    protected abstract void veNoiDung(Graphics g, int x, int y, int w, int h);

    protected void chonTab(int t) {
        this.tab = t;
        this.ds.den(0);
    }

    /** Back / Esc: lùi một bước; mặc định đóng. */
    protected void lui() {
        dong();
    }

    public void dong() {
        AmThanh.phat(AmThanh.XAC_NHAN);
        Canvas.gameScr.switchToMe();
    }

    protected void moBang() {
        this.left = null;
        this.center = null;
        this.right = null;
        Canvas.menu.showMenu = false;
        Canvas.endDlg();
        this.ds.den(0);
        switchToMe();
        AmThanh.phat(AmThanh.MO);
    }

    public void update() {
        boolean c = Canvas.isPointerClick, d = Canvas.isPointerDown;
        Canvas.isPointerClick = false;
        Canvas.isPointerDown = false;
        try {
            Canvas.gameScr.update();
        } catch (Throwable e) {
            // bỏ qua
        }
        Canvas.isPointerClick = c;
        Canvas.isPointerDown = d;
        if (Canvas.currentScreen != this || Canvas.currentDialog != null) {
            return;
        }
        String[] t = tabs();
        if (t != null && t.length > 1) {
            if (Canvas.isKeyPressed(4)) {
                chonTab((this.tab + t.length - 1) % t.length);
            } else if (Canvas.isKeyPressed(6)) {
                chonTab((this.tab + 1) % t.length);
            }
        }
        if (Canvas.isKeyPressed(2)) {
            this.ds.them(-(Bang.caoHang() + Bang.cach()));
        } else if (Canvas.isKeyPressed(8)) {
            this.ds.them(Bang.caoHang() + Bang.cach());
        }
    }

    public void updateKey() {
    }

    public boolean keyPress(int keyCode) {
        if (Canvas.currentDialog != null || Canvas.menu.showMenu) {
            return false;
        }
        if (keyCode == -7 || keyCode == 27) {
            lui();
            return true;
        }
        return false;
    }

    public void paint(Graphics g) {
        try {
            game.render.screen.GameScr.veNen(g);   // 0.9.5: chỉ lớp thế giới (HUD sau bảng không vẽ)
        } catch (Throwable e) {
            // bỏ qua
        }
        Bang.phuNen(g);
        String[] t = tabs();
        Object[] vt = null;
        if (t != null) {
            vt = new Object[t.length];
            for (int i = 0; i < t.length; i++) {
                final int ti = i;
                vt[i] = new IAction() {
                    public void perform() {
                        ManBang.this.chonTab(ti);
                    }
                };
            }
        }
        int[] c = Bang.khung(g, tieuDe(), t, this.tab, vt, new IAction() {
            public void perform() {
                ManBang.this.dong();
            }
        });
        veNoiDung(g, c[0], c[1], c[2], c[3]);
        Canvas.resetTrans(g);
    }
}
