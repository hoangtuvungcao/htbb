package game.pokemon;

import game.model.Char;
import game.model.IAction;
import game.model.ItemInInventory;
import game.render.Canvas;
import game.render.screen.Screen;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * KHO ĐỒ kiểu NRO (0.9.7) - thay cửa sổ cũ WindowInfoScr tab 22 ở máy cảm ứng: tấm gỗ hai cột "Hành trang" | "Kho đồ",
 * chạm món = THẺ VẬT PHẨM (thông tin + nút "Gửi vào kho" / "Lấy ra"). Gửi / lấy như cửa sổ cũ (putItem2Bag /
 * getItemFromBag, máy chủ trả từng món); đóng = finishPutItem2Bag.
 */
public final class ManKhoDo extends Screen {
    static ManKhoDo me;
    final Cuon dsTui = new Cuon(), dsKho = new Cuon();

    public static ManKhoDo gI() {
        if (me == null) {
            me = new ManKhoDo();
        }
        return me;
    }

    /** GameScr.onNPCKeepItem (Char.itembag đã nạp). */
    public static void mo() {
        ManKhoDo m = gI();
        if (Canvas.currentScreen == m) {
            return;
        }
        m.dsTui.den(0);
        m.dsKho.den(0);
        m.left = null;
        m.center = null;
        m.right = null;
        Canvas.menu.showMenu = false;
        Canvas.endDlg();
        m.switchToMe();
        AmThanh.phat(AmThanh.MO);
    }

    void dong() {
        try {
            Canvas.gameScr.gameService.finishPutItem2Bag();
        } catch (Throwable e) {
            // chưa nối máy chủ
        }
        AmThanh.phat(AmThanh.XAC_NHAN);
        Canvas.gameScr.switchToMe();
    }

    static Vector tui() {
        Vector v = new Vector();
        for (int i = 0; i < Char.inventory.size(); i++) {
            if (Char.inventory.elementAt(i) instanceof ItemInInventory) {
                v.addElement(Char.inventory.elementAt(i));
            }
        }
        return v;
    }

    static Vector kho() {
        return Char.itembag == null ? new Vector() : Char.itembag;
    }

    /** Thẻ món: gui = từ hành trang vào kho. */
    void moThe(final ItemInInventory it, final boolean gui) {
        if (TheVatPham.dangMo() || Canvas.currentDialog != null) {
            return;
        }
        TheVatPham.moMua(it, 0, TienTe.XU, 1, gui ? "Gửi vào kho" : "Lấy ra", new TheVatPham.ViecMua() {
            public String khongMua(int so) {
                if (gui && it.isSelling) {
                    return "Vật phẩm đang được bán.";
                }
                if (!gui && Canvas.gameScr.mainChar.FullInventory() >= TrangThaiNguoiChoi.sucChua(Canvas.gameScr.mainChar)) {
                    return "Hành trang đã đầy.";
                }
                return null;
            }

            public void mua(int so) {
                if (gui) {
                    Canvas.gameScr.gameService.putItem2Bag(it.itemID);
                } else {
                    Canvas.gameScr.gameService.getItemFromBag(it.itemID);
                }
            }
        }, null);
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
    }

    public void updateKey() {
    }

    public boolean keyPress(int keyCode) {
        if (Canvas.currentDialog != null || Canvas.menu.showMenu) {
            return false;
        }
        if (keyCode == -7 || keyCode == 27) {
            dong();
            return true;
        }
        return false;
    }

    public void paint(Graphics g) {
        try {
            game.render.screen.GameScr.veNen(g);
        } catch (Throwable e) {
            // bỏ qua
        }
        Bang.phuNen(g);
        int[] c = Bang.khung(g, "Kho đồ", null, 0, null, new IAction() {
            public void perform() {
                ManKhoDo.this.dong();
            }
        });
        int x = c[0], y = c[1], w = c[2], h = c[3];
        int k = Bang.cach();
        int wc = (w - k) / 2;
        cot(g, x, y, wc, h, "Hành trang", tui(), true, this.dsTui);
        cot(g, x + wc + k, y, w - wc - k, h, "Kho đồ", kho(), false, this.dsKho);
    }

    /** Một cột: nhãn + số món, danh sách hàng (chạm = thẻ). */
    void cot(Graphics g, int x, int y, int w, int h, String ten, final Vector v, final boolean gui, Cuon cuon) {
        Bang.the(g, x, y, w, h);
        int hn = ChuDe.cao(ChuDe.NHAN) + 6;
        String nhan = ten + " (" + v.size() + ")";
        ChuDe.chu(g, ChuDe.cat(nhan, ChuDe.NHAN, w - 12), x + 8, y + 4, ChuDe.NHAN, ChuDe.CHU, 0);
        int yl = y + 4 + hn;
        if (v.size() == 0) {
            Bang.trong(g, x + 4, yl, w - 8, y + h - 4 - yl, gui ? "Hành trang trống" : "Kho trống", null);
            return;
        }
        Bang.danhSach(g, cuon, x + 4, yl, w - 8, y + h - 4 - yl, v.size(), ManXemTrangBi.caoHangMon(), new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                final ItemInInventory it = (ItemInInventory) v.elementAt(i);
                Bang.hang(g, x, y, w, h, i, TheVatPham.monDangXem() == it, new IAction() {
                    public void perform() {
                        ManKhoDo.this.moThe(it, gui);
                    }
                }, (gui ? "kho-tui-" : "kho-kho-") + i);
                ManXemTrangBi.veHangMon(g, it, x, y, w, h, null);
            }
        });
    }
}
