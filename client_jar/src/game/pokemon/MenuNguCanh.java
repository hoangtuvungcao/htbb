package game.pokemon;

import game.model.Actor;
import game.model.Command;
import game.model.IAction;
import game.render.Canvas;
import game.render.screen.Dialog;
import game.render.screen.Font;
import game.render.screen.GameScr;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * MENU NGỮ CẢNH (0.9.3): chạm NPC / người chơi / ô đất... -> cột NÚT TO có biểu tượng cạnh mục tiêu (hoặc giữa đáy
 * màn), tối đa 6 nút mỗi trang ("Thêm" sang trang sau), thay danh sách chữ + phím mềm "Chọn / Đóng" của J2ME.
 *
 * Không sửa từng chỗ gọi: GameScr.update gọi tiepQuan() mỗi nhịp - menu chữ (Canvas.menu) vừa mở trên MÀN CHƠI ở
 * máy cảm ứng / chuột thì lấy danh sách lệnh của nó, tắt nó, mở bảng này (lệnh con mở menu chữ lại được tiếp quản
 * như vậy). Máy J2ME chỉ có phím vẫn dùng menu chữ cũ. Phím: lên / xuống chọn, OK làm, Back / Esc đóng.
 */
public final class MenuNguCanh extends Dialog {
    static MenuNguCanh me;
    public static final int TOI_DA = 6;

    Vector lenh;
    int trang;
    int chon;
    String tieuDe;
    int mx = -1, my = -1;

    public static MenuNguCanh gI() {
        if (me == null) {
            me = new MenuNguCanh();
        }
        return me;
    }

    /** Máy có con trỏ (APK / PC) dùng menu nút to. */
    public static boolean bat() {
        return Canvas.instance != null && Canvas.instance.hasPointerEvents();
    }

    /** GameScr.update: tiếp quản menu chữ vừa mở trên màn chơi. */
    public static void tiepQuan() {
        if (!bat() || Canvas.menu == null || !Canvas.menu.showMenu || Canvas.currentScreen != Canvas.gameScr) {
            return;
        }
        if (Canvas.currentDialog != null && Canvas.currentDialog != me) {
            return;
        }
        Vector v = Canvas.menu.menuItems;
        if (v == null || v.size() == 0) {
            return;
        }
        Canvas.menu.showMenu = false;
        mo(v);
    }

    public static void mo(Vector lenh) {
        mo(lenh, true);
    }

    /**
     * 0.9.5: bỏ mục TRÙNG (cùng một việc) - màn cũ đôi khi thêm hai lần cùng một lệnh. Giữ mục đầu. (Cùng chữ mà
     * khác việc - vd "Nói chuyện" của hai nhiệm vụ - vẫn giữ cả hai.)
     */
    public static Vector boTrung(Vector lenh) {
        if (lenh == null) {
            return null;
        }
        Vector r = new Vector();
        for (int i = 0; i < lenh.size(); i++) {
            Object o = lenh.elementAt(i);
            boolean trung = false;
            if (o instanceof game.model.Command) {
                game.model.Command c = (game.model.Command) o;
                for (int k = 0; k < r.size() && !trung; k++) {
                    Object q = r.elementAt(k);
                    if (q instanceof game.model.Command) {
                        game.model.Command d = (game.model.Command) q;
                        trung = c.action != null && c.action == d.action;
                    }
                }
            }
            if (!trung) {
                r.addElement(o);
            }
        }
        return r;
    }

    /** ganMucTieu = đặt cạnh mục tiêu đang chọn trên bản đồ (false: giữa đáy màn - mở từ bảng). */
    public static void mo(Vector lenh, boolean ganMucTieu) {
        MenuNguCanh m = gI();
        m.lenh = boTrung(lenh);
        m.trang = 0;
        m.chon = 0;
        m.left = null;
        m.center = null;
        m.right = null;
        GameScr gs = Canvas.gameScr;
        Actor a = gs == null || !ganMucTieu || Canvas.currentScreen != gs ? null : gs.focusedActor;
        m.tieuDe = null;
        m.mx = -1;
        if (a != null) {
            try {
                m.tieuDe = a.getDisplayName();
            } catch (Throwable e) {
                m.tieuDe = null;
            }
            // toạ độ màn của mục tiêu: thế giới -> màn theo tỉ lệ vùng nhìn (camera cố định phóng 2 ở APK / PC)
            float z = GameScr.gsw > 0 ? Canvas.w / (float) GameScr.gsw : 1f;
            m.mx = (int) ((a.x - GameScr.cmx) * z);
            m.my = (int) ((a.y - GameScr.cmy) * z);
        }
        m.show();
        AmThanh.phat(AmThanh.MO);
    }

    void dong() {
        if (Canvas.currentDialog == this) {
            Canvas.currentDialog = null;
        }
    }

    void lam(int i) {
        if (this.lenh == null || i < 0 || i >= this.lenh.size()) {
            return;
        }
        Command c = (Command) this.lenh.elementAt(i);
        dong();
        Canvas.clearKeyPressed();   // 0.9.4: phím chọn không lọt xuống màn chơi (bỏ qua lời thoại đầu)
        AmThanh.phat(AmThanh.XAC_NHAN);
        if (c.action != null) {
            c.action.perform();
        }
    }

    int soTrang() {
        int n = this.lenh == null ? 0 : this.lenh.size();
        return n <= TOI_DA ? 1 : (n + TOI_DA - 2) / (TOI_DA - 1);
    }

    /** Lệnh trên trang hiện tại: chỉ số trong lenh, -1 = nút "Thêm". */
    int[] dsTrang() {
        int n = this.lenh.size();
        if (n <= TOI_DA) {
            int[] r = new int[n];
            for (int i = 0; i < n; i++) {
                r[i] = i;
            }
            return r;
        }
        int moi = TOI_DA - 1;
        int dau = this.trang * moi;
        int so = Math.min(moi, n - dau);
        int[] r = new int[so + 1];
        for (int i = 0; i < so; i++) {
            r[i] = dau + i;
        }
        r[so] = -1;
        return r;
    }

    public void paint(Graphics g) {
        if (this.lenh == null) {
            dong();
            return;
        }
        Canvas.resetTrans(g);
        // phủ nhẹ: chạm ra ngoài = đóng
        KieuO.phu(g, 0, 0, Canvas.w, Canvas.h, 0x50060B1A);
        VungCham.them(g, 0, 0, Canvas.w, Canvas.h, new IAction() {
            public void perform() {
                MenuNguCanh.this.dong();
            }
        }, "nen-ngoai");
        int[] ds = dsTrang();
        int k = Bang.cach();
        int nh = Bang.caoHang();
        int w = 0;
        for (int i = 0; i < ds.length; i++) {
            String s = ds[i] < 0 ? "Thêm" : chu(ds[i]);
            w = Math.max(w, Font.borderFont.getWidth(s));
        }
        w = Math.min(Canvas.w - 16 - Bang.anTrai - Bang.anPhai, Math.max(150, w + nh + 34));
        int hTieu = this.tieuDe != null && this.tieuDe.length() > 0 ? Font.borderFont.getHeight() + 8 : 0;
        int h = hTieu + ds.length * (nh + k) - k;
        int x, y;
        if (this.mx >= 0 && this.mx < Canvas.w && this.my >= 0 && this.my < Canvas.h) {
            // cạnh mục tiêu: bên phải nếu đủ chỗ, không thì bên trái; dọc canh theo mục tiêu
            x = this.mx + 30 + w <= Canvas.w - 8 - Bang.anPhai ? this.mx + 30 : this.mx - 30 - w;
            y = this.my - h / 2 - 10;
        } else {
            x = Canvas.hw - w / 2;
            y = Canvas.h - 8 - h;
        }
        x = Math.max(8 + Bang.anTrai, Math.min(Canvas.w - 8 - Bang.anPhai - w, x));
        y = Math.max(8, Math.min(Canvas.h - 8 - h, y));
        if (hTieu > 0) {
            String t = GiaoDien.catChu(Font.borderFont, this.tieuDe, w - 12);
            GiaoDien.vien(g, x, y, w, hTieu - 2, GiaoDien.DO, t);
        }
        int yy = y + hTieu;
        for (int i = 0; i < ds.length; i++) {
            final int li = ds[i];
            nutLenh(g, x, yy, w, nh, li < 0 ? "Thêm" : chu(li), i == this.chon, new IAction() {
                public void perform() {
                    if (li < 0) {
                        MenuNguCanh.this.trang = (MenuNguCanh.this.trang + 1) % MenuNguCanh.this.soTrang();
                        MenuNguCanh.this.chon = 0;
                    } else {
                        MenuNguCanh.this.lam(li);
                    }
                }
            }, i);
            yy += nh + k;
        }
    }

    String chu(int i) {
        Command c = (Command) this.lenh.elementAt(i);
        return c.caption == null ? "" : ChuPokemon.D(c.caption);
    }

    static void nutLenh(Graphics g, int x, int y, int w, int h, String chu, boolean chon, Object viec, int i) {
        GiaoDien.khoi(g, x, y + 1, w, h, 10, 0x0A0F2C);
        GiaoDien.khoi(g, x, y, w, h, 10, chon ? GiaoDien.VANG : GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, x + 2, y + 2, w - 4, h - 4, 8, 0x3B66B8, 0x1F3A7A);
        int s = h - 10;
        byte[] hinh = GiaoDien.hinhMuc(chu);
        if (hinh != null) {
            KieuO.oChieu(g, x + 5, y + 5, s, s, hinh[1], hinh[0], false);
        } else {
            GiaoDien.pokeBall(g, x + 5 + s / 2, y + h / 2, s / 2 - 1, 0);
        }
        Font.borderFont.drawString(g, GiaoDien.catChu(Font.borderFont, chu, w - s - 18), x + s + 12, y + (h - Font.borderFont.getHeight()) / 2, 0);
        VungCham.them(g, x, y, w, h, viec, "lenh-" + i);
    }

    public void keyPress(int keyCode) {
        int[] ds = this.lenh == null ? new int[0] : dsTrang();
        if (keyCode == -1 || keyCode == 50) {
            this.chon = (this.chon + ds.length - 1) % Math.max(1, ds.length);
        } else if (keyCode == -2 || keyCode == 56) {
            this.chon = (this.chon + 1) % Math.max(1, ds.length);
        } else if (keyCode == -5 || keyCode == 10 || keyCode == 13 || keyCode == 53) {
            if (this.chon < ds.length) {
                if (ds[this.chon] < 0) {
                    this.trang = (this.trang + 1) % soTrang();
                    this.chon = 0;
                } else {
                    lam(ds[this.chon]);
                }
            }
        } else if (keyCode == -7 || keyCode == -6 || keyCode == 27) {
            dong();
        }
    }

    public void update() {
    }
}
