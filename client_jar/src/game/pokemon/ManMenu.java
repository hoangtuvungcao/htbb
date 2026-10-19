package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import game.render.screen.CauGiaoDien;
import game.render.screen.Font;
import game.render.screen.Screen;
import javax.microedition.lcdui.Graphics;

/**
 * MENU CHÍNH dạng lưới ô to (0.9.3): thay chuỗi menu chữ "Menu -> Bản thân -> ..." (menu chỉ liệt kê menu con).
 * Mỗi việc thường dùng cách HUD đúng HAI chạm: nút Menu -> ô. Ô "Khác" mở menu chữ cũ cho các việc hiếm.
 * Đóng: nút X góc phải trên, phím Back / Esc, hoặc chạm ra ngoài lưới.
 */
public final class ManMenu extends Screen {
    public static ManMenu me;

    /**
     * 0.9.5: MỘT lối vào cho mỗi tính năng. Hành trang, Nhiệm vụ, Bản đồ có nút riêng trên HUD (không lặp ở đây);
     * Kỹ năng là tab của màn Pokémon (bỏ ô riêng); ô cuối mở menu chữ cũ cho việc hiếm (Tiện ích, PK, Đội nhóm...).
     */
    static final String[] TEN = {"Pokémon", "Pokédex", "Đội/Bạn bè", "Sự kiện", "Hệ sinh thái", "Cửa hàng", "Thư/Quà",
        "Xếp hạng", "Cài đặt", "Tiện ích & PK"};
    /** màu ô (nền nhạt + biểu tượng màu) */
    static final int[] MAU = {ChuDe.DO, ChuDe.TIM, ChuDe.LA, ChuDe.HONG, 0x6DAA2C, ChuDe.TEAL, ChuDe.XANH, 0xE09B00, ChuDe.XAM,
        0x5C6BC0};
    static final int[] HINH = {BieuTuong.POKEBALL, BieuTuong.DEX, BieuTuong.DOI, BieuTuong.SU_KIEN, BieuTuong.SINH_THAI,
        BieuTuong.CUA_HANG, BieuTuong.THU, BieuTuong.XEP_HANG, BieuTuong.CAI_DAT, BieuTuong.TIEN_ICH};
    static final int I_POKEMON = 0, I_DEX = 1, I_DOI = 2, I_SU_KIEN = 3, I_SINH_THAI = 4, I_CUA_HANG = 5, I_THU = 6,
        I_XEP_HANG = 7, I_CAI_DAT = 8, I_KHAC = 9;

    /** Chấm đỏ ô i (dữ liệu client có): nhiệm vụ mới / đủ để trả, thư chưa đọc, điểm kỹ năng chưa cộng. */
    public static boolean cham(int i) {
        try {
            switch (i) {
                case I_THU:
                    return game.render.screen.MessageScr.nMSG > 0;
                case I_POKEMON:
                    return Canvas.gameScr.mainChar.skillPointLeft > 0 || Canvas.gameScr.mainChar.basePointLeft > 0;
                default:
                    return false;
            }
        } catch (Throwable e) {
            return false;
        }
    }

    /** Có ô nào chấm đỏ (chấm trên nút Menu của HUD). */
    public static boolean coChamDo() {
        for (int i = 0; i < TEN.length; i++) {
            if (cham(i)) {
                return true;
            }
        }
        return false;
    }

    public static ManMenu gI() {
        if (me == null) {
            me = new ManMenu();
        }
        return me;
    }

    public static void mo() {
        if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null) {
            return;
        }
        ManMenu m = gI();
        m.left = null;
        m.center = null;
        m.right = null;
        Canvas.menu.showMenu = false;
        m.switchToMe();
        AmThanh.phat(AmThanh.MO);
    }

    static void ve() {
        Canvas.gameScr.switchToMe();
    }

    /** Làm việc ô i: mở thẳng tính năng (tính năng nhiều phần mở bằng thanh tab, không qua menu con). */
    static void lam(int i) {
        AmThanh.phat(AmThanh.XAC_NHAN);
        switch (i) {
            case I_POKEMON:
                ManNhanVat.mo(ManNhanVat.TAB_TRANG_BI);
                break;
            case I_DEX:
                ManPokedex.mo();
                break;
            case I_DOI:
                ManDoi.mo(0);
                break;
            case I_SU_KIEN:
                // sự kiện do máy chủ điều khiển: gói -76 (-10000) trả menu sự kiện -> menu nút to (MenuNguCanh)
                ve();
                try {
                    game.networklogic.GameService.gI().dosendKhacMenu();
                } catch (Throwable e) {
                    // chưa nối máy chủ
                }
                break;
            case I_SINH_THAI:
                ve();
                HanhDong.moSinhThai();
                break;
            case I_CUA_HANG: {
                int n = 0;
                for (int k = 0; k < game.render.Res.shopTemplate.size(); k++) {
                    game.model.GemTemp gt = (game.model.GemTemp) game.render.Res.shopTemplate.elementAt(k);
                    n = Math.max(n, gt.shopType + 1);
                }
                byte[] cu = new byte[Math.max(1, n)];
                for (int k = 0; k < cu.length; k++) {
                    cu[k] = 20;
                }
                ManCuaHang.mo(ManCuaHang.DAC_BIET, cu);
                break;
            }
            case I_THU:
                ve();
                game.render.screen.MessageScr.gI().switchToMe();
                break;
            case I_XEP_HANG:
                ManXepHang.mo();
                break;
            case I_CAI_DAT:
                ve();
                CauGiaoDien.moCaiDat();
                break;
            default:
                ve();
                HanhDong.moMenuChinh();
                break;
        }
    }

    /** Đích của ô i (kiểm "không hai lối vào cùng một việc" - KiemTrungLap). */
    public static String dich(int i) {
        return i >= 0 && i < TEN.length ? "menu:" + i : null;
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
            return false;   // hộp thoại / menu trên màn này tự nhận phím
        }
        if (keyCode == -7 || keyCode == 27 || keyCode == -6) {
            ve();
            return true;
        }
        return false;
    }

    /** Khung bảng menu {x, y, w, h}: PC = thẻ gọn giữa màn; máy cảm ứng = bảng phải ~65 % bề ngang, cao trọn vùng an toàn. */
    static int[] khungBang() {
        int n = TEN.length;
        boolean dt = ChuDe.dt();
        int k = Math.max(ChuDe.KHE, Bang.cach());   // khe >= khoảng cách chạm tối thiểu
        if (dt) {
            int w = Math.max(300, (Bang.phai() - Bang.trai()) * 65 / 100);
            return new int[]{Bang.phai() - w, Bang.tren(), w, Bang.duoi() - Bang.tren()};
        }
        int cot = 5, hang = (n + cot - 1) / cot;
        int w = cot * O_W + (cot - 1) * k + 2 * ChuDe.DEM + 8;
        int h = ChuDe.caoDau() + 1 + ChuDe.DEM + hang * oCao() + (hang - 1) * k + ChuDe.DEM + 6;
        return new int[]{(Canvas.w - w) / 2, (Canvas.h - h) / 2, w, h};
    }

    /** bề ngang / cạnh biểu tượng ô (PC); máy cảm ứng tính theo bảng */
    static final int O_W = 74;

    static int oCao() {
        return 8 + coIcon() + 6 + 2 * ChuDe.cao(ChuDe.NHAN) + 4;
    }

    static int coIcon() {
        return ChuDe.dt() ? 30 : 26;
    }

    public void paint(Graphics g) {
        try {
            game.render.screen.GameScr.veNen(g);   // 0.9.5: chỉ lớp thế giới (HUD sau bảng không vẽ)
        } catch (Throwable e) {
            // bỏ qua
        }
        Bang.phuNen(g);
        // chạm ra ngoài bảng = đóng (vùng phủ cả màn, đăng ký TRƯỚC: ô vẽ sau thắng)
        IAction dong = new IAction() {
            public void perform() {
                ManMenu.ve();
            }
        };
        VungCham.them(g, 0, 0, Canvas.w, Canvas.h, dong, "nen-ngoai");
        int[] kb = khungBang();
        int[] c = Bang.khung(g, kb[0], kb[1], kb[2], kb[3], "Menu", null, 0, null, dong);
        int x = c[0], y = c[1], w = c[2], h = c[3];
        int k = Math.max(ChuDe.KHE, Bang.cach());   // khe >= khoảng cách chạm tối thiểu
        int n = TEN.length;
        int cot, tw, th = oCao();
        if (ChuDe.dt()) {
            cot = w >= 5 * 70 ? 5 : 4;
            tw = (w - (cot - 1) * k) / cot;
            th = Math.max(th, Math.min(tw, (h - k) / ((n + cot - 1) / cot)));
        } else {
            cot = 5;
            tw = O_W;
        }
        int hang = (n + cot - 1) / cot;
        int hLuoi = hang * th + (hang - 1) * k;
        int y0 = y + Math.max(0, (h - hLuoi) / 2);
        for (int i = 0; i < n; i++) {
            final int m = i;
            int r = i / cot;
            int trongHang = Math.min(cot, n - r * cot);   // hàng cuối thiếu ô: canh giữa
            int wHang = trongHang * tw + (trongHang - 1) * k;
            int x0 = x + (w - wHang) / 2;
            o(g, x0 + (i % cot) * (tw + k), y0 + r * (th + k), tw, th, i, new IAction() {
                public void perform() {
                    ManMenu.lam(m);
                }
            });
        }
    }

    /** Ô phẳng: biểu tượng trong ô vuông bo góc màu nhạt + nhãn dưới (tối đa 2 dòng), chấm đỏ nhỏ khi có mới. */
    static void o(Graphics g, int x, int y, int w, int h, int i, Object viec) {
        boolean an = Canvas.isPointerDown && Canvas.px >= x && Canvas.px < x + w && Canvas.py >= y && Canvas.py < y + h;
        if (an) {
            GiaoDien.khoi(g, x, y, w, h, ChuDe.BO_O, ChuDe.NEN_PHU);
        }
        int s = coIcon() + 10;
        int xi = x + (w - s) / 2, yi = y + 6;
        int mau = MAU[i];
        GiaoDien.khoi(g, xi, yi, s, s, ChuDe.BO_O + 2, GiaoDien.pha(mau, 0xFFFFFF, 6, 7));
        int ic = coIcon() - 4;
        BieuTuong.ve(g, HINH[i], xi + (s - ic) / 2, yi + (s - ic) / 2, ic, mau, GiaoDien.pha(mau, 0xFFFFFF, 6, 7));
        String[] d = ChuDe.ngat(TEN[i], ChuDe.NHAN, w - 4, 2);
        int lh = ChuDe.cao(ChuDe.NHAN);
        int yt = yi + s + 4;
        for (int q = 0; q < d.length; q++) {
            ChuDe.chu(g, d[q], x + w / 2, yt + q * lh, ChuDe.NHAN, ChuDe.CHU, 2);
        }
        if (cham(i)) {
            ChuDe.cham(g, xi + s - 2, yi + 2);
        }
        VungCham.them(g, x, y, w, h, viec, "menu-" + TEN[i]);
    }
}
