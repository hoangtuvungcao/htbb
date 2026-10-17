package game.render.screen;

import game.model.Char;
import game.pokemon.GiaoDien;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * Menu nhanh của mod (bản jar): thay cột nút tròn to (Luyện / Giao / PK / Mã PK / Nhảy vùng / Bật PK /
 * Dừng hết / Đổi pet) từng nằm đè giữa - phải màn chơi. Giờ chỉ còn MỘT Poké Ball nhỏ sát mép trái;
 * chạm vào (hoặc phím # -> "Menu nhanh") mở bảng các nút dạng viên thuốc: xanh lá = đang bật,
 * xám = tắt, xanh dương = lệnh làm ngay, đỏ = dừng hết. Đóng bằng nút x, chạm ra ngoài, phím mềm
 * phải, # hoặc *. Tùy chọn "Tự ẩn nút": Poké Ball ẩn trong lúc nhân vật đang đánh.
 * Gọi qua Nen.menuNhanhVe / menuNhanhTick / menuNhanhPhim từ AutoGuard (code mod dùng chung).
 */
final class MenuNhanh {
    static boolean mo;
    private static int chon;
    private static boolean anKhiDanh;
    private static boolean daNap;
    private static long lucDanh;
    /** đang điều khiển bằng phím (hiện viền vàng ô đang chọn cả trên máy cảm ứng) */
    private static boolean dungPhim;

    private static final int LUYEN = 0;
    private static final int GIAO = 1;
    private static final int TU_PK = 2;
    private static final int MA_PK = 3;
    private static final int NHAY = 4;
    private static final int CO_PK = 5;
    private static final int DOI_PET = 6;
    private static final int DUNG = 7;
    private static final int AN = 8;

    private static final String[] TEN = {"Luyện cấp", "Giao hàng", "Tự PK", "Mã PK", "Nhảy vùng", "Bật cờ PK", "Đổi Pet", "Dừng hết", "Tự ẩn nút"};

    private MenuNhanh() {
    }

    private static void nap() {
        if (daNap) {
            return;
        }
        daNap = true;
        try {
            anKhiDanh = RMS.loadRMSInt("htbbAnNut") == 1;
        } catch (Throwable e) {
            // mặc định hiện
        }
    }

    /** Các mục đang hiện (nút phím PK theo cài đặt "Nút phím PK: Hiện/Ẩn" của AutoPk). */
    private static int[] dsMuc() {
        if (AutoPk.hienNut) {
            return new int[]{LUYEN, GIAO, TU_PK, MA_PK, NHAY, CO_PK, DOI_PET, DUNG, AN};
        }
        return new int[]{LUYEN, GIAO, TU_PK, DUNG, AN};
    }

    /** 1 = đang bật, 0 = tắt, -1 = lệnh, -2 = lệnh dừng (đỏ). */
    private static int trangThai(int m) {
        switch (m) {
            case LUYEN:
                return AutoUp.dang ? 1 : 0;
            case GIAO:
                return AutoVanTieu.dang ? 1 : 0;
            case TU_PK:
                return AutoPk.danh() ? 1 : 0;
            case MA_PK:
                return AutoPk.maPk ? 1 : 0;
            case AN:
                return anKhiDanh ? 1 : 0;
            case DUNG:
                return -2;
            default:
                return -1;
        }
    }

    private static void lam(int m) {
        switch (m) {
            case LUYEN:
                AutoUp.batTat();
                break;
            case GIAO:
                AutoVanTieu.batTat();
                break;
            case TU_PK:
                AutoPk.batTat();
                break;
            case MA_PK:
                AutoPk.nutMaPk();
                break;
            case NHAY:
                mo = false;
                AutoPk.nutNhay();
                break;
            case CO_PK:
                AutoPk.nutDs();
                break;
            case DOI_PET:
                AutoPk.nutDoiPet();
                break;
            case DUNG:
                AutoPk.nutDung();
                break;
            case AN:
                anKhiDanh = !anKhiDanh;
                try {
                    RMS.saveRMSInt("htbbAnNut", anKhiDanh ? 1 : 0);
                } catch (Throwable e) {
                    // không lưu được thì chỉ giữ trong phiên
                }
                break;
        }
    }

    /** Có auto nào đang chạy (chấm xanh trên Poké Ball). */
    private static boolean dangChay() {
        return AutoUp.dang || AutoVanTieu.dang || AutoPk.dang();
    }

    private static boolean dungManChoi() {
        return Canvas.currentScreen == Canvas.gameScr && Canvas.gameScr != null;
    }

    // ------------------------------------------------------------------ vị trí (tính theo cỡ màn)

    private static int rBong() {
        return Canvas.w >= 300 ? 11 : 9;
    }

    private static int xBong() {
        return rBong() + 3;
    }

    /** Giữa mép trái, hơi thấp hơn tâm (dưới khung HP, trên tên map góc trái dưới). */
    private static int yBong() {
        return Canvas.h * 9 / 20;
    }

    private static boolean anBong() {
        if (!anKhiDanh || Canvas.gameScr == null) {
            return false;
        }
        Char c = Canvas.gameScr.mainChar;
        long now = System.currentTimeMillis();
        if (c != null && c.state == Char.A_ATTACK) {
            lucDanh = now;
        }
        return now - lucDanh < 3000L;
    }

    private static int cot() {
        return Canvas.w >= 150 ? 2 : 1;
    }

    private static int rongO() {
        int c = cot();
        int w = (Canvas.w - 20 - (c - 1) * 4) / c;
        return w > 96 ? 96 : w;
    }

    private static int caoO() {
        return Font.blackFont.getHeight() + 5;
    }

    private static int rongBang() {
        return cot() * rongO() + (cot() - 1) * 4 + 12;
    }

    private static int caoBang(int n) {
        int hang = (n + cot() - 1) / cot();
        return 26 + hang * (caoO() + 3) + 4;
    }

    private static int xBang() {
        return (Canvas.w - rongBang()) / 2;
    }

    private static int yBang(int n) {
        int y = (Canvas.h - Screen.deltaY - caoBang(n)) / 2;
        return y < 2 ? 2 : y;
    }

    private static int xO(int i) {
        return xBang() + 6 + (i % cot()) * (rongO() + 4);
    }

    private static int yO(int i, int n) {
        return yBang(n) + 26 + (i / cot()) * (caoO() + 3);
    }

    private static boolean trong(int x, int y, int x0, int y0, int w, int h) {
        return x >= x0 && x < x0 + w && y >= y0 && y < y0 + h;
    }

    private static boolean trongBong(int x, int y) {
        int r = rBong() + 5;
        return trong(x, y, xBong() - r, yBong() - r, 2 * r, 2 * r);
    }

    // ------------------------------------------------------------------ vẽ

    static boolean ve(Graphics g) {
        nap();
        if (!dungManChoi()) {
            mo = false;
            return true;
        }
        if (Canvas.currentDialog != null || Canvas.menu.showMenu || Canvas.gameScr.chatMode) {
            return true;
        }
        Canvas.resetTrans(g);
        if (!mo) {
            if (!Nen.coNutUp() || anBong()) {
                return true;
            }
            int r = rBong();
            GiaoDien.pokeBall(g, xBong(), yBong(), r, 0);
            if (dangChay() && Canvas.gameTick % 16 < 12) {
                g.setColor(GiaoDien.VIEN);
                g.fillArc(xBong() + r - 6, yBong() - r - 1, 8, 8, 0, 360);
                g.setColor(0x7CFC6C);
                g.fillArc(xBong() + r - 5, yBong() - r, 6, 6, 0, 360);
            }
            return true;
        }
        int[] ds = dsMuc();
        int n = ds.length;
        if (chon >= n) {
            chon = n - 1;
        }
        int x0 = xBang();
        int y0 = yBang(n);
        int w = rongBang();
        GiaoDien.bang(g, x0, y0, w, caoBang(n));
        GiaoDien.dai(g, x0 + 4, y0 + 4, w - 8, 17, "Menu nhanh");
        // nút đóng x
        int xd = x0 + w - 20;
        GiaoDien.khoi(g, xd, y0 + 6, 13, 13, 6, GiaoDien.VIEN);
        GiaoDien.khoi(g, xd + 1, y0 + 7, 11, 11, 5, GiaoDien.TRANG);
        g.setColor(GiaoDien.DO);
        g.drawLine(xd + 4, y0 + 10, xd + 8, y0 + 14);
        g.drawLine(xd + 8, y0 + 10, xd + 4, y0 + 14);
        g.drawLine(xd + 5, y0 + 10, xd + 9, y0 + 14);
        g.drawLine(xd + 9, y0 + 10, xd + 5, y0 + 14);
        int wO = rongO();
        int hO = caoO();
        for (int i = 0; i < n; i++) {
            int m = ds[i];
            int tt = trangThai(m);
            int x = xO(i);
            int y = yO(i, n);
            if (i == chon && (dungPhim || !Nen.coNutUp())) {
                GiaoDien.khoi(g, x - 2, y - 2, wO + 4, hO + 4, hO / 2 + 2, GiaoDien.VANG);
            }
            int mau = tt == 1 ? GiaoDien.LA_TOI : (tt == 0 ? 0xD8DEE8 : (tt == -2 ? GiaoDien.DO : GiaoDien.XANH));
            int mau2 = tt == 1 ? GiaoDien.LA : (tt == 0 ? GiaoDien.TRANG : (tt == -2 ? GiaoDien.DO_SANG : GiaoDien.XANH_SANG));
            GiaoDien.khoi(g, x, y, wO, hO, hO / 2, GiaoDien.VIEN);
            GiaoDien.khoiDoc(g, x + 1, y + 1, wO - 2, hO - 2, hO / 2 - 1, mau2, mau);
            int xChu = x + 8;
            if (tt >= 0) {
                // đèn trạng thái: xanh sáng = bật, xám = tắt
                g.setColor(GiaoDien.VIEN);
                g.fillArc(x + 4, y + hO / 2 - 4, 8, 8, 0, 360);
                g.setColor(tt == 1 ? 0x9CFF8C : 0x8A94A6);
                g.fillArc(x + 5, y + hO / 2 - 3, 6, 6, 0, 360);
                xChu = x + 15;
            }
            Font f = tt == 0 ? Font.blackFont : Font.borderFont;
            int yChu = y + (hO - f.getHeight()) / 2 + 1;
            if (tt < 0) {
                f.drawString(g, TEN[m], x + wO / 2, yChu, Font.CENTER);
            } else {
                f.drawString(g, TEN[m], xChu, yChu, Font.LEFT);
            }
        }
        // phím mềm riêng của bảng: trái = Chọn, phải = Đóng
        Canvas.resetTrans(g);
        g.drawImage(game.render.Res.imgCmdBar, 0, Canvas.h + 3, Graphics.BOTTOM | Graphics.LEFT);
        if (dungPhim || !Nen.coNutUp()) {
            Font.borderFont.drawString(g, "Chọn", 5, Canvas.h - Screen.deltaY, Font.LEFT);
        }
        Font.borderFont.drawString(g, "Đóng", Canvas.w - 5, Canvas.h - Screen.deltaY, Font.RIGHT);
        return true;
    }

    // ------------------------------------------------------------------ chạm

    static boolean tick() {
        nap();
        if (!dungManChoi()) {
            mo = false;
            return true;
        }
        if (Canvas.currentDialog != null || Canvas.menu.showMenu || Canvas.gameScr.chatMode) {
            return true;
        }
        if (!mo) {
            if (!Nen.coNutUp() || anBong()) {
                return true;
            }
            if (Canvas.isPointerDown && trongBong(Canvas.pxLast, Canvas.pyLast)) {
                Canvas.isPointerDown = false;
            }
            if (Canvas.isPointerClick && trongBong(Canvas.px, Canvas.py) && trongBong(Canvas.pxLast, Canvas.pyLast)) {
                Canvas.isPointerClick = false;
                Canvas.isPointerDown = false;
                mo = true;
                chon = 0;
            }
            return true;
        }
        // bảng đang mở: nuốt mọi cú chạm (nhân vật không chạy theo)
        boolean click = Canvas.isPointerClick;
        if (click || Canvas.isPointerDown) {
            dungPhim = false;
        }
        Canvas.isPointerDown = false;
        Canvas.isPointerClick = false;
        if (!click) {
            return true;
        }
        int[] ds = dsMuc();
        int n = ds.length;
        int x0 = xBang();
        int y0 = yBang(n);
        int w = rongBang();
        if (!trong(Canvas.px, Canvas.py, x0, y0, w, caoBang(n))) {
            mo = false;
            return true;
        }
        if (trong(Canvas.px, Canvas.py, x0 + w - 24, y0, 24, 24)) {
            mo = false;
            return true;
        }
        for (int i = 0; i < n; i++) {
            int x = xO(i);
            int y = yO(i, n);
            if (trong(Canvas.px, Canvas.py, x, y, rongO(), caoO()) && trong(Canvas.pxLast, Canvas.pyLast, x - 4, y - 4, rongO() + 8, caoO() + 8)) {
                chon = i;
                lam(ds[i]);
                return true;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ phím (máy bấm phím)

    /** Mở từ menu phím # của màn chơi. */
    static void moBang() {
        nap();
        mo = true;
        chon = 0;
        dungPhim = true;
    }

    static boolean phim(int keyCode) {
        if (!mo) {
            return false;
        }
        if (!dungManChoi() || Canvas.currentDialog != null || Canvas.menu.showMenu) {
            mo = false;
            return false;
        }
        int[] ds = dsMuc();
        int n = ds.length;
        int c = cot();
        dungPhim = true;
        if (keyCode == -7 || keyCode == -22 || keyCode == 35 || keyCode == 42) {
            mo = false;
            return true;
        }
        if (keyCode == -5 || keyCode == 53 || keyCode == 10 || keyCode == -6 || keyCode == -21) {
            lam(ds[chon]);
            return true;
        }
        int ga = 0;
        try {
            ga = Canvas.instance.getGameAction(keyCode);
        } catch (Throwable e) {
            // phím không có hành động game
        }
        if (keyCode == 50 || ga == javax.microedition.lcdui.Canvas.UP) {
            chon = (chon - c + n) % n;
        } else if (keyCode == 56 || ga == javax.microedition.lcdui.Canvas.DOWN) {
            chon = (chon + c) % n;
        } else if (keyCode == 52 || ga == javax.microedition.lcdui.Canvas.LEFT) {
            chon = (chon - 1 + n) % n;
        } else if (keyCode == 54 || ga == javax.microedition.lcdui.Canvas.RIGHT) {
            chon = (chon + 1) % n;
        } else if (ga == javax.microedition.lcdui.Canvas.FIRE) {
            lam(ds[chon]);
        }
        return true;
    }
}
