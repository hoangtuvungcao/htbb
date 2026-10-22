package game.render.screen;

import game.model.Char;
import game.pokemon.GiaoDien;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * Menu nhanh (bản jar / APK): MỘT Poké Ball nhỏ sát mép trái; chạm vào (hoặc phím # -> "Menu nhanh") mở bảng
 * các nút dạng viên thuốc: xanh lá = đang bật, xám = tắt, xanh dương = lệnh. Mục đầu "Tự động đánh" mở bảng
 * Tự động đánh chính thức (BangTuDong, máy chủ điều khiển); còn lại là cài đặt nút và âm thanh. Các nút mod cũ
 * (Luyện cấp, Giao hàng, Tự PK, Mã PK, Nhảy vùng, Bật cờ PK, Đổi Pet, Dừng hết) đã bỏ.
 * Đóng bằng nút x, chạm ra ngoài, phím mềm phải, # hoặc *. "Tự ẩn nút": Poké Ball ẩn lúc Pokémon đang đánh.
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

    private static final int TU_DONG = 0;
    // pokemon 0.9.7: hiệu ứng thời tiết Tắt / Thấp / Đầy đủ, màu theo mùa, xem thẻ thời tiết (game.pokemon.ThoiTiet)
    private static final int THOI_TIET = 1;
    private static final int MAU_MUA = 2;
    private static final int XEM_THOI_TIET = 3;
    private static final int AN = 8;
    // pokemon: cài đặt âm thanh (game.model.Sound, lưu RecordStore "htbbAm")
    private static final int NHAC = 9;
    private static final int HIEU_UNG = 10;
    private static final int AM_LUONG = 11;
    /** 0.9.6: Tài khoản -> nhập giftcode (cùng hộp nhập của NPC quà tặng) */
    private static final int GIFTCODE = 12;
    /** 0.9.8: ngắm tay cả khi đánh Pokémon hoang dã (PvP luôn ngắm tay - game.pokemon.NgamTay) */
    private static final int NGAM_TAY = 13;
    /** 0.9.8: thứ tự dùng bóng của nút Poké Ball (game.pokemon.BatPokemon.uuTien) */
    private static final int BONG = 14;

    private static final String[] TEN = {"Tự động đánh", "Thời tiết", "Màu theo mùa", "Xem thời tiết", "", "", "", "", "Tự ẩn nút", "Nhạc nền", "Âm thanh", "Âm lượng", "Nhập giftcode", "Ngắm thủ công", "Thứ tự bóng"};

    /** Tên mục (âm lượng kèm %). */
    private static String ten(int m) {
        if (m == AM_LUONG) {
            return new StringBuffer("Loa ").append(game.model.Sound.me.amLuong).append('%').toString();
        }
        if (m == THOI_TIET) {
            game.pokemon.ThoiTiet.napCaiDat();
            return new StringBuffer("Thời tiết: ").append(game.pokemon.ThoiTiet.TEN_CHAT_LUONG[game.pokemon.ThoiTiet.chatLuong]).toString();
        }
        if (m == BONG) {
            return new StringBuffer("Bóng: ").append(game.pokemon.BatPokemon.tenThuTu()).toString();
        }
        return TEN[m];
    }

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

    private static final int[] DS = {TU_DONG, AN, NGAM_TAY, BONG, NHAC, HIEU_UNG, AM_LUONG, THOI_TIET, MAU_MUA, XEM_THOI_TIET, GIFTCODE};

    private static int[] dsMuc() {
        return DS;
    }

    /** 1 = đang bật, 0 = tắt, -1 = lệnh, -2 = lệnh dừng (đỏ). */
    private static int trangThai(int m) {
        switch (m) {
            case TU_DONG:
                return TuDong.dang ? 1 : 0;
            case AN:
                return anKhiDanh ? 1 : 0;
            case NGAM_TAY:
                return game.pokemon.NgamTay.thuCong() ? 1 : 0;
            case NHAC:
                game.model.Sound.me.napCaiDat();
                return game.model.Sound.me.batNhac ? 1 : 0;
            case HIEU_UNG:
                game.model.Sound.me.napCaiDat();
                return game.model.Sound.me.batHieuUng ? 1 : 0;
            case THOI_TIET:
                game.pokemon.ThoiTiet.napCaiDat();
                return game.pokemon.ThoiTiet.chatLuong > game.pokemon.ThoiTiet.TAT ? 1 : 0;
            case MAU_MUA:
                game.pokemon.ThoiTiet.napCaiDat();
                return game.pokemon.ThoiTiet.mauMuaBat ? 1 : 0;
            default:
                return -1;
        }
    }

    private static void lam(int m) {
        game.pokemon.AmThanh.phat(game.pokemon.AmThanh.XAC_NHAN);
        switch (m) {
            case NHAC:
                game.model.Sound.me.datNhac(!game.model.Sound.me.batNhac);
                break;
            case HIEU_UNG:
                game.model.Sound.me.datHieuUng(!game.model.Sound.me.batHieuUng);
                game.pokemon.AmThanh.phat(game.pokemon.AmThanh.CLICK);
                break;
            case AM_LUONG: {
                // 25 -> 50 -> 75 -> 100 -> 25
                int a = game.model.Sound.me.amLuong;
                game.model.Sound.me.datAmLuong(a >= 100 ? 25 : (a / 25 + 1) * 25);
                game.pokemon.AmThanh.phat(game.pokemon.AmThanh.CLICK);
                break;
            }
            case TU_DONG:
                BangTuDong.mo();
                break;
            case THOI_TIET:
                game.pokemon.ThoiTiet.doiChatLuong();
                break;
            case MAU_MUA:
                game.pokemon.ThoiTiet.doiMauMua();
                break;
            case XEM_THOI_TIET:
                mo = false;
                game.pokemon.ThoiTiet.moTheGoiY();
                break;   // (trước thiếu break: Xem thời tiết đổi luôn Ngắm thủ công)
            case BONG:
                game.pokemon.BatPokemon.doiThuTu();
                game.pokemon.AmThanh.phat(game.pokemon.AmThanh.CLICK);
                break;
            case NGAM_TAY:
                game.pokemon.NgamTay.datThuCong(!game.pokemon.NgamTay.thuCong());
                game.pokemon.AmThanh.phat(game.pokemon.AmThanh.CLICK);
                break;
            case GIFTCODE:
                mo = false;
                game.pokemon.HanhDong.moGiftcode();
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

    /** Tự động đánh đang chạy (chấm xanh trên Poké Ball). */
    private static boolean dangChay() {
        return TuDong.dang;
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

    static boolean trongBong(int x, int y) {
        int r = rBong() + 5;
        return trong(x, y, xBong() - r, yBong() - r, 2 * r, 2 * r);
    }

    // ------------------------------------------------------------------ vẽ

    static boolean ve(Graphics g) {
        nap();
        if (!dungManChoi()) {
            mo = false;
            BangTuDong.mo = false;
            return true;
        }
        if (Canvas.currentDialog != null || Canvas.menu.showMenu || Canvas.gameScr.chatMode) {
            return true;
        }
        Canvas.resetTrans(g);
        if (BangTuDong.mo) {
            BangTuDong.ve(g);
            return true;
        }
        if (!mo) {
            if (!Nen.coNutUp() || anBong() || HudRong.bat()) {   // màn rộng: nút trên của HudRong thay Poké Ball mép trái
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
                f.drawString(g, ten(m), x + wO / 2, yChu, Font.CENTER);
            } else {
                f.drawString(g, ten(m), xChu, yChu, Font.LEFT);
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
            BangTuDong.mo = false;
            return true;
        }
        if (Canvas.currentDialog != null || Canvas.menu.showMenu || Canvas.gameScr.chatMode) {
            return true;
        }
        if (BangTuDong.mo) {
            BangTuDong.tick();
            return true;
        }
        if (!mo) {
            if (!Nen.coNutUp() || anBong() || HudRong.bat()) {   // màn rộng: nút trên của HudRong thay Poké Ball mép trái
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
                game.pokemon.AmThanh.phat(game.pokemon.AmThanh.MO);
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
        game.pokemon.AmThanh.phat(game.pokemon.AmThanh.MO);
    }

    static boolean phim(int keyCode) {
        if (BangTuDong.mo) {
            if (!dungManChoi() || Canvas.currentDialog != null || Canvas.menu.showMenu) {
                BangTuDong.mo = false;
                return false;
            }
            return BangTuDong.phim(keyCode);
        }
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
