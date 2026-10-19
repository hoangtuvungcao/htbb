package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import game.render.screen.CauGiaoDien;
import game.render.screen.Font;
import game.render.screen.Screen;
import javax.microedition.lcdui.Graphics;

/**
 * MENU CHÍNH (0.9.3 lưới ô to; 0.9.6 vòng cung; 0.9.7 kiểu NRO: nút KHỐI GỖ icon pixel, nhãn chữ trắng viền): Poké Ball
 * giữa đáy mở vòng cung nút to + hàng nút gỗ phụ.
 * Mỗi việc thường dùng cách HUD đúng HAI chạm: nút Menu -> nút. Viên "Tiện ích & PK" mở menu chữ cũ cho việc hiếm.
 * Đóng: Poké Ball (X) giữa đáy, phím Back / Esc, hoặc chạm ra ngoài.
 */
public final class ManMenu extends Screen {
    public static ManMenu me;

    /**
     * 0.9.5: MỘT lối vào cho mỗi tính năng. Hành trang, Nhiệm vụ, Bản đồ có nút riêng trên HUD (không lặp ở đây);
     * Kỹ năng là tab của màn Pokémon (bỏ ô riêng); ô cuối mở menu chữ cũ cho việc hiếm (Tiện ích, PK, Đội nhóm...).
     */
    static final String[] TEN = {"Pokémon", "Pokédex", "Đội/Bạn bè", "Sự kiện", "Hệ sinh thái", "Cửa hàng", "Thư/Quà",
        "Xếp hạng", "Cài đặt", "Tiện ích & PK", "Tự động đánh"};
    /** màu ô (nền nhạt + biểu tượng màu) */
    static final int[] MAU = {ChuDe.DO, ChuDe.TIM, ChuDe.LA, ChuDe.HONG, 0x6DAA2C, ChuDe.TEAL, ChuDe.XANH, 0xE09B00, ChuDe.XAM,
        0x5C6BC0, ChuDe.CHINH_TOI};
    static final int[] HINH = {BieuTuong.POKEBALL, BieuTuong.DEX, BieuTuong.DOI, BieuTuong.SU_KIEN, BieuTuong.SINH_THAI,
        BieuTuong.CUA_HANG, BieuTuong.THU, BieuTuong.XEP_HANG, BieuTuong.CAI_DAT, BieuTuong.TIEN_ICH, BieuTuong.TU_DONG};
    static final int I_POKEMON = 0, I_DEX = 1, I_DOI = 2, I_SU_KIEN = 3, I_SINH_THAI = 4, I_CUA_HANG = 5, I_THU = 6,
        I_XEP_HANG = 7, I_CAI_DAT = 8, I_KHAC = 9, I_TU_DONG = 10;

    /** Chấm đỏ ô i (dữ liệu client có): nhiệm vụ mới / đủ để trả, thư chưa đọc, điểm kỹ năng chưa cộng. */
    public static boolean cham(int i) {
        try {
            switch (i) {
                case I_THU:
                    return ChamMoi.menuThu();
                case I_POKEMON:
                    return ChamMoi.menuPokemon();   // 0.9.6: nguồn chung ChamMoi (điểm kỹ năng / tiềm năng, chiêu mới)
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
            case I_THU: {
                // 0.9.6: Thư/Quà = thẻ 2 hàng: Hộp thư (số thư chưa đọc) + Nhập giftcode (một chỗ dễ tìm)
                ve();
                boolean chamThu = ChamMoi.menuThu();
                java.util.Vector v = new java.util.Vector();
                int n = game.render.screen.MessageScr.nMSG;
                v.addElement(new game.model.Command(n > 0 ? "Hộp thư (" + n + " mới)" : "Hộp thư", new IAction() {
                    public void perform() {
                        game.render.screen.MessageScr.gI().switchToMe();
                    }
                }));
                v.addElement(new game.model.Command("Nhập giftcode", new IAction() {
                    public void perform() {
                        HanhDong.moGiftcode();
                    }
                }));
                MenuNguCanh.mo(v, "Thư / Quà");
                MenuNguCanh.gI().cham = new boolean[]{chamThu, false};
                break;
            }
            case I_XEP_HANG:
                ManXepHang.mo();
                break;
            case I_CAI_DAT:
                ve();
                CauGiaoDien.moCaiDat();
                break;
            case I_TU_DONG:
                // 0.9.6: bảng cài đặt Tự động đánh (cùng bảng của nút bánh răng cạnh viên AUTO trên HUD)
                ve();
                game.render.screen.CauGiaoDien.moTuDong();
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

    /**
     * 0.9.6 MENU KIỂU POKÉMON GO: thế giới mờ 55 %, Poké Ball giữa đáy (cùng chỗ nút Menu của HUD) thành nút ĐÓNG
     * (đĩa trắng X), 5 nút TRÒN to xếp vòng cung trên nó (việc dùng nhiều nhất) + hàng 5 viên phụ phía trên (việc ít
     * dùng). Không còn bảng trắng to với ô nhỏ trôi nổi.
     */
    static final int[] CHINH = {I_POKEMON, I_DEX, I_CUA_HANG, I_DOI, I_SU_KIEN};
    static final int[] PHU = {I_TU_DONG, I_SINH_THAI, I_THU, I_XEP_HANG, I_CAI_DAT, I_KHAC};
    static final int[] GOC = {166, 128, 90, 52, 14};

    static int rNut() {
        return Math.max(VungCham.MIN / 2 + 1, ChuDe.dt() ? 25 : 21);
    }

    /** Tâm Poké Ball / nút đóng (= nút Menu giữa đáy HUD). */
    static int yBong() {
        return Canvas.h - 17 - 5;
    }

    /** Bán kính vòng cung (theo cao màn: đủ chỗ hàng phụ phía trên). */
    static int banKinhCung() {
        int r = rNut();
        int k = Math.max(ChuDe.KHE, Bang.cach()), hv = caoVienPhu();
        int[] wv = new int[PHU.length];
        for (int q = 0; q < PHU.length; q++) {
            wv[q] = ChuDe.rong(TEN[PHU[q]], ChuDe.NHAN) + 34;
        }
        int nh = soHangPhu(wv, k, Bang.phai() - Bang.trai() - 8);
        int conCho = yBong() - Bang.tren() - (hv + 14) - (nh - 1) * (hv + k) - r - 6;
        return Math.max(2 * r + 30, Math.min(ChuDe.dt() ? 150 : 132, conCho));
    }

    static int soHangPhu(int[] wv, int k, int wMax) {
        int n = 1, tong = 0;
        for (int q = 0; q < wv.length; q++) {
            if (q > 0 && tong + k + wv[q] > wMax) {
                n++;
                tong = wv[q];
            } else {
                tong += wv[q] + (q > 0 ? k : 0);
            }
        }
        return n;
    }

    static int caoVienPhu() {
        return Bang.co(ChuDe.dt() ? 26 : 22);
    }

    public void paint(Graphics g) {
        try {
            game.render.screen.GameScr.veNen(g);   // 0.9.5: chỉ lớp thế giới (HUD sau bảng không vẽ)
        } catch (Throwable e) {
            // bỏ qua
        }
        Canvas.resetTrans(g);
        Bang.khungMoTam();
        int t = Bang.tuoiTam();
        KieuO.phu(g, 0, 0, Canvas.w, Canvas.h, (0x8C * (60 + t * 40 / 140) / 100) << 24 | 0x120804);
        // chạm ra ngoài = đóng (vùng phủ cả màn, đăng ký TRƯỚC: nút vẽ sau thắng)
        IAction dong = new IAction() {
            public void perform() {
                ManMenu.ve();
            }
        };
        VungCham.them(g, 0, 0, Canvas.w, Canvas.h, dong, "nen-ngoai");
        int cx = Canvas.hw, cy = yBong();
        int r = rNut(), R = banKinhCung();
        // hiệu ứng mở: vòng cung bung ra từ Poké Ball (140 ms)
        int Rv = R * (70 + t * 30 / 140) / 100;
        for (int q = 0; q < CHINH.length; q++) {
            final int i = CHINH[q];
            int a = GOC[q];
            int x = cx + cos(a) * Rv / 1024, y = cy - sin(a) * Rv / 1024;
            nutTo(g, x, y, r, i, new IAction() {
                public void perform() {
                    ManMenu.lam(i);
                }
            });
        }
        // viên phụ phía trên vòng cung (xuống dòng khi không vừa bề ngang, mỗi hàng canh giữa)
        int hv = caoVienPhu();
        int k = Math.max(ChuDe.KHE, Bang.cach());
        int wMax = Bang.phai() - Bang.trai() - 8;
        int[] wv = new int[PHU.length];
        for (int q = 0; q < PHU.length; q++) {
            wv[q] = ChuDe.rong(TEN[PHU[q]], ChuDe.NHAN) + 34;
        }
        int soHang = soHangPhu(wv, k, wMax);
        int yv = Math.max(Bang.tren() + 4, cy - R - r - 14 - hv - (soHang - 1) * (hv + k));
        int q0 = 0;
        while (q0 < PHU.length) {
            int tong = 0, q1 = q0;
            while (q1 < PHU.length && (q1 == q0 || tong + k + wv[q1] <= wMax)) {
                tong += wv[q1] + (q1 > q0 ? k : 0);
                q1++;
            }
            int xv = cx - tong / 2;
            for (int q = q0; q < q1; q++) {
                final int i = PHU[q];
                vienPhu(g, xv, yv, wv[q], hv, i, new IAction() {
                    public void perform() {
                        ManMenu.lam(i);
                    }
                });
                xv += wv[q] + k;
            }
            yv += hv + k;
            q0 = q1;
        }
        // Poké Ball giữa đáy -> nút đóng (đĩa trắng, X tối)
        int sb = Math.max(VungCham.MIN, 38);
        Bang.nutDong(g, cx - sb / 2, cy - sb / 2, sb, dong);
    }

    /** Nút tròn to vòng cung: đĩa trắng + icon màu, nhãn đậm dưới; chấm đỏ khi có mới. */
    static void nutTo(Graphics g, int x, int y, int r, int i, Object viec) {
        Bang.nutTron(g, x, y, r, HINH[i], MAU[i], false, null, viec, "menu-" + TEN[i]);
        // nhãn: chữ trắng viền nâu dưới nút (đọc rõ trên thế giới tối) - kiểu NRO
        String ten = TEN[i];
        int ht = ChuDe.cao(ChuDe.SO) + 2;
        ChuDe.chuVienGiua(g, ten, x, y + r + 5, ht, ChuDe.SO, 0xFFFFFF, ChuDe.VIEN, 2);
        if (cham(i)) {
            ChuDe.cham(g, x + r * 7 / 10, y - r * 7 / 10);
        }
    }

    /** Nút phụ (việc ít dùng) - 0.9.7: khối gỗ nổi, icon pixel trái + nhãn trắng viền; nhấn = lõm. */
    static void vienPhu(Graphics g, int x, int y, int w, int h, int i, Object viec) {
        boolean an = Bang.dangAn(g, x, y, w, h);
        if (!an) {
            KhungNro.bong(g, x, y, w, h);
        }
        if (!KhungNro.ve(g, an ? KhungNroBang.NUT_GO_NHAN : KhungNroBang.NUT_GO, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 3, ChuDe.VIEN);
            GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, 2, 0x7A3E18);
        }
        int dy = an ? 1 : 0;
        int s = h - 6;
        if (!KhungNro.iconGiua(g, HINH[i], x + 4 + 8, y + h / 2 + dy, 1, -1)) {
            BieuTuong.ve(g, HINH[i], x + 3, y + 3, s, MAU[i], ChuDe.NEN);
        }
        ChuDe.chuVienGiua(g, TEN[i], x + 24, y + dy, h, ChuDe.NHAN, 0xF6E6C0, ChuDe.VIEN, 0);
        if (cham(i)) {
            ChuDe.cham(g, x + w - 5, y + 4);
        }
        VungCham.them(g, x, y, w, h, viec, "menu-" + TEN[i]);
    }


    static int sin(int goc) {
        return (int) (Math.sin(Math.toRadians(goc)) * 1024);
    }

    static int cos(int goc) {
        return (int) (Math.cos(Math.toRadians(goc)) * 1024);
    }
}
