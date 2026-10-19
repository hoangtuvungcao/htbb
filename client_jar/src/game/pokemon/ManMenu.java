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

    static final String[] TEN = {"Pokémon", "Hành trang", "Kỹ năng", "Nhiệm vụ", "Pokédex", "Đội/Bạn bè", "Bản đồ", "Sự kiện",
        "Hệ sinh thái", "Cửa hàng", "Thư/Quà", "Xếp hạng", "Cài đặt", "Khác"};
    /** màu ô */
    static final int[] MAU = {0xE8312B, 0xF08030, 0x7038F8, 0xE8B800, 0xD8342C, 0x6890F0, 0x48A858, 0xF85888,
        0x78C850, 0x30B0B0, 0x5D93FF, 0xC97A10, 0x8A94A6, 0x5E6A80};
    /** hình ảnh mặc định (KieuO.HINH_*) cho ô không vẽ tay */
    static final byte[] HINH = {KieuO.HINH_SAO, KieuO.HINH_KHIEN, KieuO.HINH_SET, KieuO.HINH_TIM, KieuO.HINH_SAO,
        KieuO.HINH_VONG, KieuO.HINH_LOC, KieuO.HINH_SAO, KieuO.HINH_LA, KieuO.HINH_BOM, KieuO.HINH_BONG, KieuO.HINH_TANG,
        KieuO.HINH_TANG, KieuO.HINH_CONG};
    static final int I_POKEMON = 0, I_TUI = 1, I_KY_NANG = 2, I_NHIEM_VU = 3, I_DEX = 4, I_DOI = 5, I_BAN_DO = 6, I_SU_KIEN = 7,
        I_SINH_THAI = 8, I_CUA_HANG = 9, I_THU = 10, I_XEP_HANG = 11, I_CAI_DAT = 12, I_KHAC = 13;

    /** Chấm đỏ ô i (dữ liệu client có): nhiệm vụ mới / đủ để trả, thư chưa đọc, điểm kỹ năng chưa cộng. */
    public static boolean cham(int i) {
        try {
            switch (i) {
                case I_NHIEM_VU:
                    return ManNhiemVu.coMoi();
                case I_THU:
                    return game.render.screen.MessageScr.nMSG > 0;
                case I_KY_NANG:
                    return Canvas.gameScr.mainChar.skillPointLeft > 0;
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
            case I_TUI:
                ManNhanVat.moHanhTrang();
                break;
            case I_KY_NANG:
                ManNhanVat.mo(ManNhanVat.TAB_KY_NANG);
                break;
            case I_NHIEM_VU:
                ManNhiemVu.mo();
                break;
            case I_DEX:
                ManPokedex.mo();
                break;
            case I_DOI:
                ManDoi.mo(0);
                break;
            case I_BAN_DO:
                ve();
                HanhDong.moBanDoLon();
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

    public void paint(Graphics g) {
        try {
            Canvas.gameScr.paint(g);
        } catch (Throwable e) {
            // bỏ qua
        }
        Bang.phuNen(g);
        // chạm ra ngoài lưới = đóng (vùng phủ cả màn, đăng ký TRƯỚC: ô vẽ sau thắng)
        VungCham.them(g, 0, 0, Canvas.w, Canvas.h, new IAction() {
            public void perform() {
                ManMenu.ve();
            }
        }, "nen-ngoai");
        int[] c = Bang.khung(g, "Menu", null, 0, null, new IAction() {
            public void perform() {
                ManMenu.ve();
            }
        });
        int x = c[0], y = c[1], w = c[2], h = c[3];
        int k = Bang.cach();
        int n = TEN.length;
        // lưới: 4-5 cột (màn rộng PC tới 7), ô vuông >= 64 dp, vừa chiều cao
        int cotMax = Canvas.w > 640 || !MenuNguCanh.bat() ? 7 : 5;
        int cot = 4;
        int s = 0;
        for (int cc = 4; cc <= cotMax; cc++) {
            int hang = (n + cc - 1) / cc;
            int sw = (w - k * (cc + 1)) / cc;
            int sh = (h - k * (hang + 1)) / hang;
            int ss = Math.min(sw, sh);
            if (ss > s) {
                s = ss;
                cot = cc;
            }
        }
        s = Math.min(s, 104);
        int hang = (n + cot - 1) / cot;
        int wLuoi = cot * s + (cot - 1) * k, hLuoi = hang * s + (hang - 1) * k;
        int x0 = x + (w - wLuoi) / 2, y0 = y + (h - hLuoi) / 2;
        for (int i = 0; i < n; i++) {
            final int m = i;
            int xi = x0 + (i % cot) * (s + k), yi = y0 + (i / cot) * (s + k);
            o(g, xi, yi, s, i, new IAction() {
                public void perform() {
                    ManMenu.lam(m);
                }
            });
        }
    }

    /** Biểu tượng nét trắng trong vòng màu (cỡ theo r). */
    static void veIcon(Graphics g, int i, int cx, int cy, int r) {
        int u = r * 11 / 20;
        int vien = GiaoDien.pha(MAU[i], 0x000000, 1, 2);
        switch (i) {
            case I_POKEMON:   // Pokémon: Poké Ball
                GiaoDien.pokeBall(g, cx, cy, u + 2, 0);
                return;
            case I_TUI: {   // hành trang: ba lô
                g.setColor(GiaoDien.TRANG);
                g.fillRoundRect(cx - u, cy - u + 4, 2 * u, 2 * u - 2, 8, 8);
                g.drawArc(cx - u / 2, cy - u - 2, u, u + 2, 0, 180);
                g.setColor(vien);
                g.fillRect(cx - u + 3, cy, 2 * u - 6, 2);
                g.fillRect(cx - 2, cy - 2, 4, 6);
                return;
            }
            case I_NHIEM_VU: {   // nhiệm vụ: cuộn giấy + chấm than
                g.setColor(GiaoDien.TRANG);
                g.fillRect(cx - u + 2, cy - u, 2 * u - 4, 2 * u);
                g.fillRoundRect(cx - u - 1, cy - u - 3, 2 * u + 2, 6, 4, 4);
                g.fillRoundRect(cx - u - 1, cy + u - 3, 2 * u + 2, 6, 4, 4);
                g.setColor(MAU[i]);
                g.fillRect(cx - 2, cy - u + 4, 4, u);
                g.fillRect(cx - 2, cy + u / 2 + 1, 4, 4);
                return;
            }
            case I_BAN_DO: {   // bản đồ: tờ gấp 3 + ghim
                g.setColor(GiaoDien.TRANG);
                g.fillTriangle(cx - u, cy - u + 3, cx - u / 3, cy - u, cx - u / 3, cy + u - 3);
                g.fillTriangle(cx - u, cy - u + 3, cx - u, cy + u, cx - u / 3, cy + u - 3);
                g.fillTriangle(cx - u / 3, cy - u, cx + u / 3, cy - u + 3, cx + u / 3, cy + u);
                g.fillTriangle(cx - u / 3, cy - u, cx - u / 3, cy + u - 3, cx + u / 3, cy + u);
                g.fillTriangle(cx + u / 3, cy - u + 3, cx + u, cy - u, cx + u, cy + u - 3);
                g.fillTriangle(cx + u / 3, cy - u + 3, cx + u / 3, cy + u, cx + u, cy + u - 3);
                g.setColor(GiaoDien.DO);
                g.fillArc(cx - 4, cy - 6, 8, 8, 0, 360);
                return;
            }
            case I_DOI: {   // đội / bạn bè: ba người
                g.setColor(GiaoDien.TRANG);
                int a = u * 2 / 3;
                g.fillArc(cx - u, cy - u + 1, a, a, 0, 360);
                g.fillArc(cx - u - a / 3, cy, a + 2 * a / 3, u, 0, 180);
                g.fillArc(cx + u - a, cy - u + 1, a, a, 0, 360);
                g.fillArc(cx + u - a - a / 3, cy, a + 2 * a / 3, u, 0, 180);
                g.fillArc(cx - a / 2, cy - u - 2, a, a, 0, 360);
                g.fillArc(cx - a / 2 - a / 3, cy - 1, a + 2 * a / 3, u + 2, 0, 180);
                return;
            }
            case I_THU: {   // thư / quà: phong bì
                g.setColor(GiaoDien.TRANG);
                g.fillRect(cx - u, cy - u * 2 / 3, 2 * u, u * 4 / 3);
                g.setColor(vien);
                g.drawLine(cx - u, cy - u * 2 / 3, cx, cy + 2);
                g.drawLine(cx + u - 1, cy - u * 2 / 3, cx, cy + 2);
                return;
            }
            case I_DEX: {   // Pokédex: máy đỏ + màn tròn xanh
                g.setColor(GiaoDien.TRANG);
                g.fillRoundRect(cx - u * 3 / 4, cy - u, u * 3 / 2, 2 * u, 6, 6);
                g.setColor(0x5D93FF);
                g.fillArc(cx - u / 2, cy - u + 3, u, u, 0, 360);
                g.setColor(vien);
                g.fillRect(cx - u / 2, cy + u / 3, u, 2);
                g.fillRect(cx - u / 2, cy + u / 3 + 4, u * 2 / 3, 2);
                return;
            }
            case I_CUA_HANG: {   // cửa hàng: mái hiên sọc + quầy
                g.setColor(GiaoDien.TRANG);
                g.fillRect(cx - u, cy - u / 3, 2 * u, u + u / 3);
                for (int k = 0; k < 4; k++) {
                    g.setColor(k % 2 == 0 ? GiaoDien.TRANG : GiaoDien.DO);
                    g.fillRect(cx - u + k * u / 2, cy - u, u / 2 + 1, u * 2 / 3);
                }
                g.setColor(vien);
                g.fillRect(cx - u / 4, cy + u / 4, u / 2, u * 3 / 4);
                return;
            }
            case I_XEP_HANG: {   // xếp hạng: cúp
                g.setColor(GiaoDien.TRANG);
                g.fillArc(cx - u * 3 / 4, cy - u, u * 3 / 2, u * 3 / 2, 180, 180);
                g.fillRect(cx - u * 3 / 4, cy - u, u * 3 / 2, u * 3 / 4);
                g.fillRect(cx - 2, cy + u / 3, 5, u / 2);
                g.fillRect(cx - u / 2, cy + u * 3 / 4, u, 3);
                return;
            }
            case I_CAI_DAT: {   // cài đặt: bánh răng
                g.setColor(GiaoDien.TRANG);
                for (int k = 0; k < 8; k++) {
                    double t = Math.PI * k / 4;
                    int dx = (int) (Math.cos(t) * u), dy = (int) (Math.sin(t) * u);
                    g.fillArc(cx + dx - 3, cy + dy - 3, 7, 7, 0, 360);
                }
                g.fillArc(cx - u + 2, cy - u + 2, 2 * u - 4, 2 * u - 4, 0, 360);
                g.setColor(MAU[i]);
                g.fillArc(cx - u / 3, cy - u / 3, 2 * (u / 3), 2 * (u / 3), 0, 360);
                return;
            }
            case I_KHAC: {   // khác: ba chấm
                g.setColor(GiaoDien.TRANG);
                for (int k = -1; k <= 1; k++) {
                    g.fillArc(cx + k * (u * 2 / 3) - 3, cy - 3, 7, 7, 0, 360);
                }
                return;
            }
            default: {
                javax.microedition.lcdui.Image a = KieuO.anhHinh(HINH[i], r >= 16 ? 2 : 1);
                if (a != null) {
                    g.drawImage(a, cx, cy, Graphics.HCENTER | Graphics.VCENTER);
                }
            }
        }
    }

    static void o(Graphics g, int x, int y, int s, int i, Object viec) {
        GiaoDien.khoi(g, x, y + 2, s, s, 10, 0x0A0F2C);
        GiaoDien.khoi(g, x, y, s, s, 10, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, x + 1, y + 1, s - 2, s - 2, 9, Bang.NEN_THE, Bang.NEN_THE_DUOI);
        int fh = Font.blackFont.getHeight();
        int r = Math.max(10, (s - fh - 14) / 2);
        int cx = x + s / 2, cy = y + 6 + r;
        g.setColor(GiaoDien.VIEN);
        g.fillArc(cx - r - 1, cy - r - 1, 2 * r + 2, 2 * r + 2, 0, 360);
        g.setColor(MAU[i]);
        g.fillArc(cx - r, cy - r, 2 * r, 2 * r, 0, 360);
        g.setColor(GiaoDien.pha(MAU[i], 0xFFFFFF, 1, 2));
        g.fillArc(cx - r + 3, cy - r + 2, 2 * r - 6, r, 0, 180);
        veIcon(g, i, cx, cy, r);
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, TEN[i], s - 4), cx, y + s - fh - 4, Font.CENTER);
        if (cham(i)) {
            // chấm đỏ: có mục mới
            int rd = Math.max(5, s / 12);
            g.setColor(0xFFFFFF);
            g.fillArc(x + s - 2 * rd - 6, y + 4, 2 * rd + 2, 2 * rd + 2, 0, 360);
            g.setColor(GiaoDien.DO);
            g.fillArc(x + s - 2 * rd - 5, y + 5, 2 * rd, 2 * rd, 0, 360);
        }
        VungCham.them(g, x, y, s, s, viec, "menu-" + TEN[i]);
    }
}
