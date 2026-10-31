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
     * 0.9.9: viên "Tự động đánh" luôn hiện (0.9.6.1 - 0.9.8 ẩn khi HUD màn rộng có bánh răng: người chơi không tìm thấy).
     */
    static final String[] TEN = {"Pokémon", "Pokédex", "Đội/Bạn bè", "Sự kiện", "Hệ sinh thái", "Cửa hàng", "Thư/Quà",
        "Xếp hạng", "Cài đặt", "Tiện ích & PK", "Tự động đánh", "Nhiệm vụ của tôi", "Sổ tay huấn luyện", "Bản đồ vùng"};
    /** màu ô (nền nhạt + biểu tượng màu) */
    static final int[] MAU = {ChuDe.DO, ChuDe.TIM, ChuDe.LA, ChuDe.HONG, 0x6DAA2C, ChuDe.CAM, ChuDe.XANH, 0xE09B00, ChuDe.XAM,
        0x5C6BC0, ChuDe.CHINH_TOI, ChuDe.CAM, ChuDe.LA, ChuDe.NUT_BAN_DO};
    static final int[] HINH = {BieuTuong.POKEBALL, BieuTuong.DEX, BieuTuong.DOI, BieuTuong.SU_KIEN, BieuTuong.SINH_THAI,
        BieuTuong.CUA_HANG, BieuTuong.THU, BieuTuong.XEP_HANG, BieuTuong.CAI_DAT, BieuTuong.TIEN_ICH, BieuTuong.TU_DONG,
        BieuTuong.NHIEM_VU, BieuTuong.TIN, BieuTuong.BAN_DO};
    static final int I_POKEMON = 0, I_DEX = 1, I_DOI = 2, I_SU_KIEN = 3, I_SINH_THAI = 4, I_CUA_HANG = 5, I_THU = 6,
        I_XEP_HANG = 7, I_CAI_DAT = 8, I_KHAC = 9, I_TU_DONG = 10,
        // 0.9.16 (chủ dự án: "mỗi lần về làng có về trực tiếp được đâu, đi mất thời gian lắm"): Nhiệm vụ của tôi + Sổ tay
        // huấn luyện mở ở mọi nơi (trước chỉ qua Quầy đổi thưởng trong làng / nút trong sổ Nhiệm vụ)
        I_NV_CUA_TOI = 11, I_SO_TAY = 12,
        // 30/10/2026 (người chơi: "thêm cái mở bản đồ vùng để xem cho tiện"): bản đồ vùng Kanto / Johto / Hoenn - trước chỉ
        // ở Cài đặt › Bản đồ lớn của menu chữ, không ai tìm thấy. Nút "Bản đồ" HUD PC = bản đồ CẢ MAP đang đứng (khác việc).
        I_BAN_DO_VUNG = 13;

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
                // 0.9.17: thẻ "Sự kiện" (mùa / ngày cộng đồng + hoạt động ngày / tuần). Trước: -76 / -10000 = menu "Khác"
                // của game gốc (Mở Túi Đồ, Mở Kho, Shop Đặc Biệt...) với tiêu đề = tên quái đang nhắm.
                ve();
                game.render.screen.GameScr.moSuKien();
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
                        if (MenuNguCanh.bat()) {
                            // 31/10/2026 (soi giao diện toàn bộ): màn thư cũ (MessageScr) vẽ trống ở máy cảm ứng (nhãn tab
                            // trắng, không vùng chạm). Thư riêng / tin đội đã chép sang bảng chat (KenhChat) - mở tab đó.
                            game.render.screen.MessageScr.nMSG = 0;
                            ManChat.moTab(KenhChat.RIENG);
                        } else {
                            game.render.screen.MessageScr.gI().switchToMe();
                        }
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
            case I_NV_CUA_TOI:
                ve();
                game.render.screen.GameScr.moNhiemVuCuaToi();   // -527 / 0 / 8 như nút trong sổ Nhiệm vụ (0.9.15)
                break;
            case I_SO_TAY:
                ve();
                game.render.screen.GameScr.moSoTay();
                break;
            case I_BAN_DO_VUNG:
                ve();
                ManBanDoVung.mo();
                break;
            case I_CAI_DAT:
                ve();
                CauGiaoDien.moCaiDat();
                break;
            case I_TU_DONG:
                // bảng cài đặt Tự động đánh (HUD APK: chỉ có nhấn giữ nút Tự động -> cần lối vào nhìn thấy được ở đây)
                ve();
                game.render.screen.CauGiaoDien.moTuDong();
                break;
            default:
                ve();
                // menu chữ cũ chỉ còn việc hiếm: ẩn mục đã có lối vào ở Menu này / HUD (PauseMenu.an)
                game.render.screen.PauseMenu.moTuMenu = true;
                HanhDong.moMenuChinh();
                game.render.screen.PauseMenu.moTuMenu = false;
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
    static final int[] PHU = {I_NV_CUA_TOI, I_SO_TAY, I_BAN_DO_VUNG, I_TU_DONG, I_SINH_THAI, I_THU, I_XEP_HANG, I_CAI_DAT, I_KHAC};
    static final int[] PHU_HUD_RONG = {I_SINH_THAI, I_THU, I_XEP_HANG, I_CAI_DAT, I_KHAC};

    /**
     * 0.9.6.1 một lối vào mỗi việc: HUD màn rộng (PC) có viên AUTO + nút bánh răng cài đặt Tự động đánh ngay trên màn
     * chơi -> bỏ viên "Tự động đánh" trùng ở đây. HUD APK (Hud.java) chỉ có nhấn giữ -> giữ viên.
     */
    public static int[] phu() {
        // 0.9.9: chủ game - "Tự động đánh" LUÔN có ở Menu (0.9.8 bỏ trên HUD màn rộng -> người chơi PC / J2ME báo mất
        // menu AUTO); viên AUTO + bánh răng HUD vẫn giữ (lối tắt). PHU_HUD_RONG giữ cho kiểm thử cũ.
        // 30/10/2026: màn thấp (J2ME cảm ứng 320x240) - viên "Bản đồ vùng" thêm một hàng làm hàng viên đè vòng cung: bỏ viên
        // này (lối vào còn ở Tiện ích & PK › Cài đặt › Bản đồ vùng - PauseMenu.an hiện lại mục đó, phím *)
        if (conCho(PHU) < toiThieuCung() && conCho(PHU_GON) > conCho(PHU)) {
            return PHU_GON;
        }
        return PHU;
    }

    static final int[] PHU_GON = {I_NV_CUA_TOI, I_SO_TAY, I_TU_DONG, I_SINH_THAI, I_THU, I_XEP_HANG, I_CAI_DAT, I_KHAC};

    /** Menu này có viên "Bản đồ vùng" (màn thấp bỏ - PauseMenu không ẩn mục Cài đặt › Bản đồ vùng). */
    public static boolean coBanDoVung() {
        int[] p = phu();
        for (int i = 0; i < p.length; i++) {
            if (p[i] == I_BAN_DO_VUNG) {
                return true;
            }
        }
        return false;
    }

    /** Chỗ dọc còn cho vòng cung khi hàng viên phụ là phu. */
    static int conCho(int[] phu) {
        int r = rNut();
        int k = Math.max(ChuDe.KHE, Bang.cach()), hv = caoVienPhu();
        int[] wv = new int[phu.length];
        for (int q = 0; q < phu.length; q++) {
            wv[q] = ChuDe.rongVien(TEN[phu[q]], ChuDe.NHAN, 0xF6E6C0) + 34;
        }
        int nh = soHangPhu(wv, k, Bang.phai() - Bang.trai() - 8);
        return yBong() - Bang.tren() - (hv + 14) - (nh - 1) * (hv + k) - r - 6;
    }

    /** Bán kính vòng cung tối thiểu (nhãn dưới nút không chạm nút kế bên). */
    static int toiThieuCung() {
        int r = rNut();
        // 0.9.7: đủ xa để nhãn dưới nút (vuông NRO) không chạm nút kế bên trên cung (sin128 - sin166 = 0,546)
        return Math.max(2 * r + 30, (2 * r + ChuDe.cao(ChuDe.SO) + 12) * 1000 / 546);
    }
    static final int[] GOC = {166, 128, 90, 52, 14};

    static int rNut() {
        // 0.9.22: màn nhỏ (J2ME cảm ứng 240x320 / 320x240 - mặc định J2ME Loader): nút cung nhỏ hơn để vòng cung + hàng
        // viên phụ vừa màn (trước: nút 166 / 14 độ tràn mép trái / phải 240x320, nút giữa đè hàng viên phụ 320x240)
        int r = Math.min(Canvas.w, Canvas.h) < 300 ? 17 : ChuDe.dt() ? 25 : 21;
        return Math.max(VungCham.MIN / 2 + 1, r);
    }

    /**
     * 0.9.22: bán kính NGANG của vòng cung - nút ngoài cùng (góc 166 / 14) và nhãn dưới nó trọn trong màn (màn hẹp
     * 240x320: cung dẹt ngang; khoảng dọc giữa các nút giữ nguyên nên nhãn không chạm nút kế bên).
     */
    static int banKinhNgang(int R, int r) {
        int bien = r;
        int[] ngoai = {CHINH[0], CHINH[CHINH.length - 1]};
        for (int q = 0; q < ngoai.length; q++) {
            bien = Math.max(bien, (ChuDe.rong(TEN[ngoai[q]], ChuDe.SO) + 4) / 2);
        }
        int rx = (Canvas.hw - bien - 2) * 1024 / Math.max(1, cos(GOC[GOC.length - 1]));
        return Math.max(r, Math.min(R, rx));
    }

    /** Tâm Poké Ball / nút đóng (= nút Menu giữa đáy HUD). */
    static int yBong() {
        // 0.9.21: nút đóng cỡ chạm lớn (APK mật độ cao: MIN > 38) không tràn mép dưới màn
        return Canvas.h - Math.max(17 + 5, Math.max(VungCham.MIN, 38) / 2 + 3);
    }

    /** Bán kính vòng cung (theo cao màn: đủ chỗ hàng phụ phía trên). */
    static int banKinhCung() {
        return Math.max(toiThieuCung(), Math.min(ChuDe.dt() ? 150 : 132, conCho(phu())));
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
                // 0.9.22: đang bung (140 ms đầu, nút còn trượt ra) chạm trượt khỏi ô KHÔNG đóng Menu (trước: cú chạm vào ô
                // đúng lúc khung đổi rơi xuống nền = đóng Menu, KiemChamHud chập chờn khi máy tải nặng)
                if (Bang.tuoiTam() >= 140) {
                    ManMenu.ve();
                }
            }
        };
        VungCham.them(g, 0, 0, Canvas.w, Canvas.h, dong, "nen-ngoai");
        int cx = Canvas.hw, cy = yBong();
        int r = rNut(), R = banKinhCung(), Rn = banKinhNgang(R, r);
        // hiệu ứng mở: vòng cung bung ra từ Poké Ball (140 ms)
        int Rv = R * (70 + t * 30 / 140) / 100, Rnv = Rn * (70 + t * 30 / 140) / 100;
        for (int q = 0; q < CHINH.length; q++) {
            final int i = CHINH[q];
            int a = GOC[q];
            int x = cx + cos(a) * Rnv / 1024, y = cy - sin(a) * Rv / 1024;
            nutTo(g, x, y, r, i, t >= 140, new IAction() {
                public void perform() {
                    ManMenu.lam(i);
                }
            });
        }
        // viên phụ phía trên vòng cung (xuống dòng khi không vừa bề ngang, mỗi hàng canh giữa)
        int hv = caoVienPhu();
        int k = Math.max(ChuDe.KHE, Bang.cach());
        int wMax = Bang.phai() - Bang.trai() - 8;
        int[] phu = phu();
        int[] wv = new int[phu.length];
        for (int q = 0; q < phu.length; q++) {
            wv[q] = ChuDe.rongVien(TEN[phu[q]], ChuDe.NHAN, 0xF6E6C0) + 34;
        }
        int soHang = soHangPhu(wv, k, wMax);
        int yv = Math.max(Bang.tren() + 4, cy - R - r - 14 - hv - (soHang - 1) * (hv + k));
        int q0 = 0;
        while (q0 < phu.length) {
            int tong = 0, q1 = q0;
            while (q1 < phu.length && (q1 == q0 || tong + k + wv[q1] <= wMax)) {
                tong += wv[q1] + (q1 > q0 ? k : 0);
                q1++;
            }
            int xv = cx - tong / 2;
            for (int q = q0; q < q1; q++) {
                final int i = phu[q];
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
    static void nutTo(Graphics g, int x, int y, int r, int i, boolean nhan, Object viec) {
        Bang.nutTron(g, x, y, r, HINH[i], MAU[i], false, null, viec, "menu-" + TEN[i]);
        // nhãn: chữ trắng viền nâu dưới nút (đọc rõ trên thế giới tối) - kiểu NRO; 0.9.7: chỉ khi vòng đã bung hết
        // (đang bung 70 -> 100% bán kính thì nhãn đè nút kế bên)
        String ten = TEN[i];
        int ht = ChuDe.cao(ChuDe.SO) + 2;
        if (nhan) {
            ChuDe.chuVienGiua(g, ten, x, y + r + 5, ht, ChuDe.SO, 0xFFFFFF, ChuDe.VIEN, 2);
        }
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
