package game.pokemon;

import game.model.Char;
import game.model.IAction;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.screen.Screen;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * XEM TRANG BỊ NGƯỜI KHÁC kiểu NRO (0.9.7, chỉ xem) - thay cửa sổ cũ WindowInfoScr ở máy cảm ứng: tấm gỗ, sân khấu có
 * Pokémon của người đó giữa hai cột ô vuông trang bị (đúng ô như màn Nhân vật), bên phải danh sách món đang mặc. Chạm ô /
 * hàng = THẺ VẬT PHẨM chỉ xem (TheVatPham.moXem: chỉ số so với đồ của mình). Kiểu THU: đồ của thú cưỡi người đó.
 */
public final class ManXemTrangBi extends Screen {
    static ManXemTrangBi me;
    Char ch;
    boolean thu;
    final Cuon ds = new Cuon();

    public static ManXemTrangBi gI() {
        if (me == null) {
            me = new ManXemTrangBi();
        }
        return me;
    }

    /** GameScr.onViewCharInfo (thu = false) / onViewAnimalInfo (thu = true). */
    public static void mo(Char ch, boolean thu) {
        if (ch == null) {
            return;
        }
        ManXemTrangBi m = gI();
        m.ch = ch;
        m.thu = thu;
        m.ds.den(0);
        m.left = null;
        m.center = null;
        m.right = null;
        Canvas.menu.showMenu = false;
        m.switchToMe();
        AmThanh.phat(AmThanh.MO);
    }

    /** Màn đang xem (kiểm thử). */
    public static boolean dangXem() {
        return me != null && Canvas.currentScreen == me;
    }

    void dong() {
        AmThanh.phat(AmThanh.XAC_NHAN);
        Canvas.gameScr.switchToMe();
    }

    Vector dsMon() {
        Vector v = this.thu ? this.ch.wearingAnimal : this.ch.wearingItems;
        return v == null ? new Vector() : v;
    }

    /** Món người đó mặc ở ô i (như ManNhanVat.dangMac). */
    ItemInInventory monO(int i) {
        Vector v = dsMon();
        for (int k = 0; k < v.size(); k++) {
            ItemInInventory it = (ItemInInventory) v.elementAt(k);
            ItemTemplate t = ManNhanVat.mau(it);
            if (t == null || !ManNhanVat.hopLoai(i, t.type)) {
                continue;
            }
            if (t.type == 8 && (i == 5) != (it.viTriVe == 1)) {
                continue;
            }
            return it;
        }
        return null;
    }

    /** Thẻ chỉ xem; xin chỉ số món của NGƯỜI ĐÓ (chủ = ch.ID) nếu chưa có. */
    void xem(ItemInInventory it) {
        if (it == null) {
            return;
        }
        if (!it.isEnoughData && it.allAttribute.size() == 0) {
            it.isEnoughData = true;
            try {
                GameService.gI().requestItemInfo(it.itemID, this.ch.ID);
            } catch (Throwable e) {
                // chưa nối máy chủ
            }
        }
        TheVatPham.moXem(it, this.thu ? "Đồ thú của " + this.ch.name : "Đang mặc: " + this.ch.name);
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
        if (this.ch == null) {
            return;
        }
        String ten = this.ch.name == null ? "?" : this.ch.name;
        int[] c = Bang.khung(g, (this.thu ? "Đồ thú: " : "Trang bị: ") + ten, null, 0, null, new IAction() {
            public void perform() {
                ManXemTrangBi.this.dong();
            }
        });
        int x = c[0], y = c[1], w = c[2], h = c[3];
        int k = Bang.cach();
        int lw = Math.min(w * 46 / 100, Math.max(200, w / 2));
        if (this.thu) {
            veLuoiThu(g, x, y, lw, h);
        } else {
            veSanKhau(g, x, y, lw, h);
        }
        veDanhSach(g, x + lw + k, y, w - lw - k, h);
    }

    /** Sân khấu: Pokémon người đó giữa hai cột ô vuông (6 + 6, như màn Nhân vật khi thấp). */
    void veSanKhau(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        int hTen = ChuDe.cao(ChuDe.NHAN) + 4;
        String cap = "Cấp " + this.ch.level;
        ChuDe.chu(g, ChuDe.cat(cap, ChuDe.NHAN, w), x + w / 2, y, ChuDe.NHAN, ChuDe.CHU, 2);
        int yTren = y + hTen;
        int hc = h - hTen;
        // 6 hàng x 1 cột mỗi bên; thấp quá (ô < cỡ chạm) thì 3 hàng x 2 cột mỗi bên (như màn Nhân vật)
        int s = Math.min((hc - 5 * k) / 6, Bang.co(30));
        boolean gon = s < VungCham.MIN;
        if (gon) {
            s = Math.max(VungCham.MIN, Math.min((hc - 2 * k) / 3, Bang.co(30)));
        }
        int b = s + k;
        int soHang = gon ? 3 : 6;
        int coCot = gon ? 2 * b : s + 2;
        ManNhanVat.nenSanKhau(g, x + coCot, yTren, w - 2 * coCot, hc);
        int y0 = yTren + Math.max(0, (hc - (soHang * b - k)) / 2);
        for (int i = 0; i < 6; i++) {
            int c = gon ? i / 3 : 0, r = gon ? i % 3 : i;
            veO(g, i, x + c * b, y0 + r * b, s);
            veO(g, 6 + i, x + w - s - c * b, y0 + r * b, s);
        }
        int giuaL = x + coCot + 4, giuaR = x + w - coCot - 4;
        float z = Math.max(1.5f, Math.min(4.2f, Math.min((giuaR - giuaL) / 40f, hc / 40f)));
        ManNhanVat.banRongBe = (giuaR - giuaL) / 2 - 2;
        ManNhanVat.veNguoiChoi(g, this.ch, (giuaL + giuaR) / 2, yTren + hc - 6 - (int) (3 * z), z);
        ManNhanVat.banRongBe = 1000;
    }

    /** Ô vuông trang bị i (ô 11 = thú: chỉ hình, không món). */
    void veO(Graphics g, int i, int x, int y, int s) {
        final ItemInInventory it = i == ManNhanVat.O_THU ? null : monO(i);
        if (it != null) {
            LuoiTui.veO(g, it, x, y, s, false, new IAction() {
                public void perform() {
                    ManXemTrangBi.this.xem(it);
                }
            }, "xem-o-" + i);
            return;
        }
        Bang.oDo(g, x, y, s, false, false, null, null);
        int ic = s * 11 / 20;
        BieuTuong.veBong(g, ManNhanVat.HINH_O[i], x + (s - ic) / 2, y + (s - ic) / 2, Math.max(ic, 16), 0x8A6A4A, ChuDe.NEN_PHU);
    }

    /** Đồ thú: lưới ô vuông (thú không có ô cố định). */
    void veLuoiThu(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        ManNhanVat.nenSanKhau(g, x, y, w, h);
        Vector v = dsMon();
        int s = Math.max(VungCham.MIN, Bang.co(30));
        int cot = Math.max(1, (w - 8 + k) / (s + k));
        int n = Math.max(cot * 2, ((v.size() + cot - 1) / cot) * cot);
        int x0 = x + (w - (cot * (s + k) - k)) / 2;
        for (int i = 0; i < n; i++) {
            int xx = x0 + (i % cot) * (s + k), yy = y + 6 + (i / cot) * (s + k);
            if (yy + s > y + h - 4) {
                break;
            }
            if (i < v.size()) {
                final ItemInInventory it = (ItemInInventory) v.elementAt(i);
                LuoiTui.veO(g, it, xx, yy, s, false, new IAction() {
                    public void perform() {
                        ManXemTrangBi.this.xem(it);
                    }
                }, "xem-thu-" + i);
            } else {
                Bang.oDo(g, xx, yy, s, false, false, null, null);
            }
        }
    }

    /** Bên phải: danh sách món đang mặc (ảnh + tên màu độ hiếm + ô / loại); chạm = thẻ chỉ xem. */
    void veDanhSach(Graphics g, int x, int y, int w, int h) {
        Bang.the(g, x, y, w, h);
        final Vector v = dsMon();
        if (v.size() == 0) {
            Bang.trong(g, x + 4, y + 4, w - 8, h - 8, this.thu ? "Thú chưa mang đồ" : "Chưa mặc trang bị", null);
            return;
        }
        int rh = caoHangMon();
        Bang.danhSach(g, this.ds, x + 4, y + 4, w - 8, h - 8, v.size(), rh, new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                final ItemInInventory it = (ItemInInventory) v.elementAt(i);
                Bang.hang(g, x, y, w, h, i, false, new IAction() {
                    public void perform() {
                        ManXemTrangBi.this.xem(it);
                    }
                }, "xem-hang-" + i);
                veHangMon(g, it, x, y, w, h, null);
            }
        });
    }

    /** Một hàng món: ô đầu (icon + viền độ hiếm), tên (+N), dòng phụ (mặc định "Cấp N - loại"). */
    static void veHangMon(Graphics g, ItemInInventory it, int x, int y, int w, int h, String phu) {
        int s = ManNhanVat.oDau(g, x, y, h);
        ItemTemplate t = ManNhanVat.mau(it);
        int hiem = KieuO.mauHiem(it.colorName);
        if (hiem >= 0) {
            KieuO.khungHiem(g, x + 3, y + 3, s, hiem);
        }
        if (t != null) {
            Bang.icon(g, t.idIcon, x + 3 + s / 2, y + 3 + s / 2, s >= 26 ? s / 22f : 1f);
        }
        if (it.lock == 1) {
            LuoiTui.oKhoaNho(g, x + 3 + s - 9, y + 5);
        }
        String ten = t == null ? "?" : t.name;
        game.render.screen.Font f = game.render.screen.Font.blackFont, f2 = game.render.screen.Font.arialFont;
        if (phu == null) {
            String loai = t == null ? null : BangTrangBi.tenLoai(t.type);
            phu = "Cấp " + ManNhanVat.capCan(it, t) + (loai != null ? " - " + loai : "");
            ten = ChuDe.boCap(ten);   // dòng phụ đã ghi cấp: tên bỏ " · Cấp N"
        }
        ten = ten + (it.plusTemplate > 0 ? " +" + it.plusTemplate : "");
        GiaoDien.tenHaiDong(g, f, f2, ten, phu, x + s + 9, y + 2, w - s - 14, y + 3 + f.getHeight(), w - s - 14);
    }

    /** Cao hàng món (đủ 2 dòng chữ, >= cỡ chạm). */
    static int caoHangMon() {
        return Math.max(Math.max(VungCham.MIN + 2, Bang.caoHang() * 5 / 6),
                2 + game.render.screen.Font.blackFont.getHeight() + 1 + game.render.screen.Font.arialFont.getHeight() + 3);
    }
}
