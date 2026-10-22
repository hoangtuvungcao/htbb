package game.render.screen;

import game.model.Actor;
import game.model.ChatInfo;
import game.model.LiveActor;
import game.model.MainChar;
import game.model.PopupName;
import game.model.Position;
import game.model.QuickSlot;
import game.model.Tilemap;
import game.pokemon.GiaoDien;
import game.pokemon.HanhDong;
import game.pokemon.HopThoaiDuoi;
import game.pokemon.KieuO;
import game.pokemon.PokeChieu;
import game.render.Canvas;
import game.render.Res;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * HUD màn rộng (màn ngang >= 400 px có chuột / cảm ứng: bản PC 640x360 logic, máy tính bảng J2ME) - bố cục kiểu
 * MMO nhập vai, giao diện Pokémon của game:
 *  - trái trên: thẻ trạng thái (GameScr.paintMainCharInfo / thẻ HD của PC) - chạm = hồ sơ nhân vật;
 *  - giữa trên: khung mục tiêu (GameScr.paintFocusActorInfo dời vào giữa) + thông báo nhặt đồ;
 *  - phải trên: hàng nút KHỐI GỖ icon pixel (hành trang, nhiệm vụ, bản đồ - 0.9.7 kiểu NRO), bản đồ nhỏ khung gỗ ngay dưới;
 *  - GIỮA ĐÁY: nút Poké Ball to (khối đỏ viền vàng) = Menu;
 *  - phải dưới: nút đánh lớn (phím 5) + 4 nút chiêu tròn (phím 1/3/7/9: màu hệ, quạt hồi chiêu, PP), đổi bộ ô (phím 0),
 *    2 nút thuốc HP / PP, công tắc Tự động (TuDong.batTat), nút ngữ cảnh (câu cá cạnh hồ);
 *  - trái dưới: nút chat + 3 dòng chat mới nhất mờ dần.
 * Không phần nào chồng nhau hay chồng hộp thoại đáy (HopThoaiDuoi: phần dưới HUD ẩn khi hộp thoại mở) - kiểm bằng
 * {@link #kiemChong()}. Bản APK có HUD HD riêng (com.htbb.pokemon.hd.Hud) -> port vá bat() = false.
 */
public final class HudRong {
    private HudRong() {
    }

    /** Tắt hẳn (bản APK vá; máy bấm phím). */
    public static boolean tat;

    public static boolean bat() {
        return !tat && Canvas.w >= 400 && Canvas.h >= 240 && Canvas.instance != null && Canvas.instance.hasPointerEvents();
    }

    // ------------------------------------------------------------------ bố cục (toạ độ logic)

    /**
     * 0.9.5 (một lối vào mỗi tính năng): HUD chỉ giữ lối tắt dùng nhiều - Hành trang (cam), Nhiệm vụ (vàng), Bản đồ (teal)
     * - và nút Menu (Poké Ball, sát mép phải); mọi việc khác ở lưới Menu (ManMenu), không lặp lại ở đây.
     */
    static final int N_TUI = 0, N_NV = 1, N_BAN_DO = 2, N_MENU = 3;
    static final int SO_NUT_TREN = 4;
    static final int R_TREN = 12, BUOC_TREN = 30, Y_TREN = 15;
    static final int[] MAU_TREN = {game.pokemon.ChuDe.NUT_TUI, game.pokemon.ChuDe.NUT_NV, game.pokemon.ChuDe.NUT_BAN_DO, game.pokemon.ChuDe.DO};
    static final int[] HINH_TREN = {game.pokemon.BieuTuong.TUI, game.pokemon.BieuTuong.NHIEM_VU, game.pokemon.BieuTuong.BAN_DO, game.pokemon.BieuTuong.POKEBALL};
    static final String[] TEN_TREN = {"Hành trang", "Nhiệm vụ", "Bản đồ", "Menu"};
    /** đỉnh đầu thẻ bản đồ nhỏ / đỉnh ảnh bản đồ nhỏ */
    static final int Y_DAU_MM = 29, Y_MM = 45;
    static final int MM_W = 132, MM_H = 82;

    /** Nút Menu (Poké Ball) giữa đáy: bán kính. */
    static final int R_MENU = 17;

    /** Tâm x nút i: 0..2 hàng phải trên (sát mép phải), N_MENU = giữa đáy. */
    static int xNutTren(int i) {
        if (i == N_MENU) {
            return Canvas.hw;
        }
        return Canvas.w - 16 - (SO_NUT_TREN - 2 - i) * BUOC_TREN;
    }

    /**
     * 0.9.8 (game.pokemon.ThoiTiet): tâm x của ô kế bên trái hàng nút trên (cùng bước / bán kính, chừa đèn Tự động
     * ở xNutTren(0) - R_TREN - 7) - đĩa thời tiết đặt ở đây, đổi bố cục hàng nút thì đĩa theo.
     */
    public static int xTruocHangTren() {
        return xNutTren(0) - BUOC_TREN - 6;
    }

    public static int yHangTren() {
        return Y_TREN;
    }

    public static int rHangTren() {
        return R_TREN;
    }

    static int yNut(int i) {
        return i == N_MENU ? Canvas.h - R_MENU - 5 : Y_TREN;
    }

    static int rNut(int i) {
        return i == N_MENU ? R_MENU : R_TREN;
    }

    /** Thẻ trạng thái (vùng chạm). */
    static int rongThe() {
        return 170;
    }

    static int caoThe() {
        return 50;
    }

    /** Thẻ trạng thái (rộng / cao) cho lớp khác đặt dưới nó (game.pokemon.TheNhiemVu). */
    public static int rongTheTrangThai() {
        return rongThe();
    }

    public static int caoTheTrangThai() {
        return caoThe();
    }

    // cụm chiến đấu
    static final int R_DANH = 27, R_CHIEU = 16, KC_CHIEU = 64, R_THUOC = 12, R_NGU_CANH = 14, R_DOI_BO = 9;
    static final int[] GOC_CHIEU = {180, 145, 110, 75};
    static final int[] PHIM_CHIEU = {1, 3, 7, 9};
    /** cos / sin * 1024 của GOC_CHIEU */
    static final int[] COS = {-1024, -839, -350, 265};
    static final int[] SIN = {0, 587, 962, 989};

    static int xDanh() {
        return Canvas.w - 40;
    }

    static int yDanh() {
        return Canvas.h - 40;
    }

    static int xChieu(int i) {
        return xDanh() + COS[i] * KC_CHIEU / 1024;
    }

    static int yChieu(int i) {
        return yDanh() - SIN[i] * KC_CHIEU / 1024;
    }

    static int xThuoc() {
        return xDanh() - 100;
    }

    static int yThuoc(boolean hp) {
        return hp ? yDanh() + 12 : yDanh() - 18;
    }

    static int xNguCanh() {
        return xDanh() - 84;
    }

    static int yNguCanh() {
        return yDanh() - 76;
    }

    /**
     * Viên đổi bộ chiêu "1/2" (vẽ R_DOI_BO) dưới cụm chiêu, giữa ô chiêu 0 và nút đánh. Vùng CHẠM riêng R_CHAM_DOI_BO
     * (> vẽ: ngón tay / chuột không phải nhắm đúng viên nhỏ), không chạm vùng chạm nút đánh / chiêu 0 (KiemChamHud).
     */
    static final int R_CHAM_DOI_BO = 13;

    static int xDoiBo() {
        return xDanh() - 40;
    }

    static int yDoiBo() {
        return yDanh() + 26;
    }

    /** Công tắc Tự động: viên trên cụm chiêu, sát mép phải. */
    static final int W_TU_DONG = 46, H_TU_DONG = 18;

    static int xTuDong() {
        return Canvas.w - W_TU_DONG - 4;
    }

    static int yTuDong() {
        return yChieu(3) - R_CHIEU - H_TU_DONG - 6;
    }

    /** 0.9.6: nút bánh răng nhỏ cạnh viên AUTO = bảng cài đặt Tự động đánh (chạm ngắn viên = bật / tắt). */
    static final int R_TD_CAI = 9;

    static int xTdCai() {
        return xTuDong() - R_TD_CAI - 8;
    }

    static int yTdCai() {
        return yTuDong() + H_TU_DONG / 2;
    }

    /** lúc bắt đầu giữ viên AUTO (nhấn giữ >= 0,45 giây = mở cài đặt thay vì bật / tắt) */
    static long giuTdLuc;

    // chat
    static final int R_CHAT = 12;

    static int xChat() {
        return 16;
    }

    static int yChat() {
        return Canvas.h - 16;
    }

    static final int W_CHAT = 210, DONG_CHAT = 3, LH_CHAT = 13;

    /** Phần dưới HUD (cụm chiến đấu, chat) đang hiện? Ẩn khi hộp thoại đáy / ô chat / menu mở. */
    static boolean hienDuoi(GameScr gs) {
        return !gs.chatMode && Canvas.currentDialog == null && !Canvas.menu.showMenu && !HopThoaiDuoi.dangBat() && gs.hideGUI != 2;
    }

    // ------------------------------------------------------------------ bản đồ nhỏ (vị trí cho GameScr.setPosMiniMap)

    /** {x, y, w, h} bản đồ nhỏ theo cỡ ảnh bản đồ (1 px / ô). */
    static int[] oMiniMap(int wAnh, int hAnh) {
        int w = wAnh < MM_W ? wAnh : MM_W;
        int h = hAnh < MM_H ? hAnh : MM_H;
        if (w < 40) {
            w = 40;
        }
        if (h < 30) {
            h = 30;
        }
        return new int[]{Canvas.w - w - 4, Y_MM, w, h};
    }

    // ------------------------------------------------------------------ vẽ

    private static final String[] SO = new String[200];

    static String so(int n) {
        if (n >= 0 && n < SO.length) {
            String s = SO[n];
            if (s == null) {
                s = String.valueOf(n);
                SO[n] = s;
            }
            return s;
        }
        return String.valueOf(n);
    }

    static boolean banDoTo;

    /** Đang mở bản đồ to (lớp khác ẩn thẻ HUD phụ). */
    public static boolean dangMoBanDoTo() {
        return banDoTo || BangTuDong.mo;
    }

    /** Gọi từ GameScr.paint (lớp giao diện), sau thanh ô / bản đồ nhỏ. */
    public static void ve(Graphics g, GameScr gs) {
        if (!bat() || gs.mainChar == null) {
            return;
        }
        Canvas.resetTrans(g);
        if (gs.hideGUI != 2) {
            veNutTren(g);
        }
        HanhDong.veBao(g, 40);
        if (hienDuoi(gs)) {
            veChat(g, gs);
            try {
                veCum(g, gs);
            } catch (Throwable e) {
                // chưa có dữ liệu chiêu / ô nhanh (vừa vào game): bỏ khung này
            }
        }
        if (banDoTo) {
            veBanDoTo(g, gs);
        }
        Canvas.resetTrans(g);
        if (!banDoTo) {
            dangKyCham(gs);
        }
    }

    /**
     * Đăng ký mọi nút HUD vào VungCham (TAM: chạm trong vùng đã nới cỡ tối thiểu kéo về tâm nút rồi HudRong.cham xử lý
     * như cũ) - cùng một nơi đo cỡ chạm / khoảng cách (kiểm thử ChupTrongGame).
     */
    static void dangKyCham(GameScr gs) {
        Object t = game.pokemon.VungCham.TAM;
        if (Canvas.currentScreen != gs || BangTuDong.mo) {
            return;   // màn chơi đang làm nền cho bảng khác / tấm Tự động: nút HUD phía sau không nhận chạm
        }
        BoNut b = BO_VE;
        lapNut(gs, b);
        for (int i = 0; i < b.n; i++) {
            if (b.r[i] > 0) {
                game.pokemon.VungCham.themTron(b.x[i], b.y[i], b.rVe[i], t, b.ten[i]);
            } else {
                game.pokemon.VungCham.themTuyetDoi(b.x[i], b.y[i], b.w[i], b.h[i], t, b.ten[i]);
            }
        }
    }

    // ------------------------------------------------------------------ bảng nút chạm (MỘT nguồn: trung / VungCham / kiểm thử)

    /** Nút HUD đang hiện khung này: mã (lam), tâm / góc, bán kính vẽ + bán kính chạm (tròn) hoặc hộp w x h (+ lề). */
    static final class BoNut {
        int n;
        final int[] ma = new int[24], x = new int[24], y = new int[24], rVe = new int[24], r = new int[24], w = new int[24], h = new int[24], le = new int[24];
        final String[] ten = new String[24];

        void tron(int m, String t, int cx, int cy, int rv, int rc) {
            int i = n++;
            ma[i] = m;
            ten[i] = t;
            x[i] = cx;
            y[i] = cy;
            rVe[i] = rv;
            r[i] = rc;
        }

        void hop(int m, String t, int x0, int y0, int w0, int h0, int le0) {
            int i = n++;
            ma[i] = m;
            ten[i] = t;
            x[i] = x0;
            y[i] = y0;
            w[i] = w0;
            h[i] = h0;
            le[i] = le0;
            r[i] = 0;
        }
    }

    /** bộ của luồng vẽ (dangKyCham) / của trung (luồng game, chuột PC) - không dùng chung giữa hai luồng */
    private static final BoNut BO_VE = new BoNut(), BO_CHAM = new BoNut();

    /** Lập bảng nút đang hiện (cùng điều kiện hiện với veNutTren / veCum). */
    static void lapNut(GameScr gs, BoNut b) {
        b.n = 0;
        if (gs.hideGUI != 2) {
            for (int i = 0; i < SO_NUT_TREN; i++) {
                if (i == N_MENU && !hienDuoi(gs)) {
                    continue;
                }
                b.tron(i, TEN_VUNG_TREN[i], xNutTren(i), yNut(i), rNut(i), rNut(i) + 2);
            }
            b.hop(10, "hud-the", 0, 0, rongThe(), caoThe(), 0);
            // 0.9.5: bản đồ nhỏ không còn là nút (trùng nút Bản đồ)
        }
        if (!hienDuoi(gs)) {
            return;
        }
        QuickSlot[] bo = MainChar.quickSlot == null ? null : MainChar.quickSlot[GameScr.indexTabSlot];
        for (int i = 0; i < 4; i++) {
            if (game.pokemon.ThanhNhanh.hienO(bo, gs.SLOTINDEXOFKEY[PHIM_CHIEU[i]])) {
                b.tron(20 + i, TEN_CHIEU[i], xChieu(i), yChieu(i), R_CHIEU, R_CHIEU + 2);
            }
        }
        b.tron(24, "hud-danh", xDanh(), yDanh(), R_DANH, R_DANH + 3);
        if (game.pokemon.ThanhNhanh.coBo2()) {
            b.tron(25, "hud-doi-bo", xDoiBo(), yDoiBo(), R_CHAM_DOI_BO, R_CHAM_DOI_BO);
        }
        b.tron(26, "hud-thuoc-hp", xThuoc(), yThuoc(true), R_THUOC, R_THUOC + 2);
        b.tron(27, "hud-thuoc-pp", xThuoc(), yThuoc(false), R_THUOC, R_THUOC + 2);
        if (HanhDong.loaiNguCanh() == 3) {
            b.tron(28, "hud-ngu-canh", xNguCanh(), yNguCanh(), R_NGU_CANH, R_NGU_CANH + 2);
        }
        b.hop(29, "hud-tu-dong", xTuDong(), yTuDong(), W_TU_DONG, H_TU_DONG, 2);
        b.tron(31, "hud-tu-dong-cai-dat", xTdCai(), yTdCai(), R_TD_CAI, R_TD_CAI + 2);
        b.tron(30, "hud-chat", xChat(), yChat(), R_CHAT, R_CHAT + 3);
    }

    /**
     * Kiểm thử (client_pc/test/KiemChamHud): nút HUD đang hiện {tên, mã, int[]{x, y, r}} (tròn, r = bán kính chạm của
     * trung) hoặc {tên, mã, int[]{x, y, w, h}} (hộp, đã cộng lề chạm).
     */
    public static Object[][] cacNutCham(GameScr gs) {
        BoNut b = new BoNut();
        lapNut(gs, b);
        Object[][] v = new Object[b.n][];
        for (int i = 0; i < b.n; i++) {
            int[] hinh = b.r[i] > 0 ? new int[]{b.x[i], b.y[i], b.r[i]}
                    : new int[]{b.x[i] - b.le[i], b.y[i] - b.le[i], b.w[i] + 2 * b.le[i], b.h[i] + 2 * b.le[i]};
            v[i] = new Object[]{b.ten[i], new Integer(b.ma[i]), hinh};
        }
        return v;
    }

    static final String[] TEN_CHIEU = {"hud-chieu0", "hud-chieu1", "hud-chieu2", "hud-chieu3"};
    static final String[] TEN_VUNG_TREN = {"hud-tren-0", "hud-tren-1", "hud-tren-2", "hud-tren-3"};

    static boolean tro(int x, int y, int r) {
        // chuột / ngón đang đè (hiệu ứng nhấn)
        if (!Canvas.isPointerDown) {
            return false;
        }
        int dx = Canvas.px - x, dy = Canvas.py - y;
        return dx * dx + dy * dy <= r * r;
    }

    /** Nút HUD (0.9.7 kiểu NRO): khối vuông gỗ (thân sáng = gỗ, thân màu = khối màu gần nhất), sáng = viền vàng. */
    static void nen(Graphics g, int x, int y, int r, int tren, int duoi, boolean sang) {
        int m = GiaoDien.doSang(duoi) > 200 ? game.pokemon.KhungNroBang.NUT_GO : game.pokemon.Bang.manhMau(duoi);
        game.pokemon.Bang.nutVuong(g, x, y, r, m, sang, false);
    }


    /** Nút tròn một màu (dải từ nhạt -> màu). */
    static void nenMau(Graphics g, int x, int y, int r, int mau, boolean sang) {
        nen(g, x, y, r, GiaoDien.pha(mau, 0xFFFFFF, 1, 4), mau, sang);
    }

    static void veNutTren(Graphics g) {
        boolean tuDong = TuDong.dang;
        GameScr gs = Canvas.gameScr;
        for (int i = 0; i < SO_NUT_TREN; i++) {
            int x = xNutTren(i), y = yNut(i), r0 = rNut(i);
            if (i == N_MENU) {
                if (gs == null || !hienDuoi(gs)) {
                    continue;   // hộp thoại đáy / chat mở: Poké Ball ẩn cùng phần dưới HUD
                }
                // 0.9.7: Poké Ball pixel to trên khối đỏ viền vàng (nút Menu)
                boolean an = tro(x, y, r0 + 2);
                game.pokemon.Bang.nutVuong(g, x, y, r0, game.pokemon.KhungNroBang.NUT_DO, true, an);
                if (!game.pokemon.KhungNro.iconGiua(g, game.pokemon.BieuTuong.POKEBALL, x, y + (an ? 1 : 0), 2, -1)) {
                    GiaoDien.pokeBall(g, x, y, r0 - 1, 0);
                }
                continue;
            }
            // 0.9.7: khối gỗ + icon pixel đủ màu
            boolean an = tro(x, y, r0 + 2);
            game.pokemon.Bang.nutVuong(g, x, y, r0, game.pokemon.KhungNroBang.NUT_GO, false, an);
            int s = r0 * 2 - 4;
            game.pokemon.BieuTuong.ve(g, HINH_TREN[i], x - s / 2, y - s / 2 + (an ? 1 : 0), s, MAU_TREN[i], 0xFFFFFF);
        }
        // chấm đỏ: Menu (mục mới trong lưới), Nhiệm vụ (nhiệm vụ mới / đủ để trả)
        if (game.pokemon.ManMenu.coChamDo() && gs != null && hienDuoi(gs)) {
            game.pokemon.ChuDe.cham(g, xNutTren(N_MENU) + R_MENU - 4, yNut(N_MENU) - R_MENU + 4);
        }
        if (game.pokemon.ChamMoi.hudNhiemVu()) {
            game.pokemon.ChuDe.cham(g, xNutTren(N_NV) + R_TREN - 3, Y_TREN - R_TREN + 3);
        }
        if (tuDong) {
            // đèn nhỏ cạnh hàng nút khi Tự động đang chạy
            g.setColor(game.pokemon.ChuDe.CHINH);
            g.fillArc(xNutTren(0) - R_TREN - 7, Y_TREN - 3, 6, 6, 0, 360);
        }
    }

    static void chamDo(Graphics g, int x, int y) {
        game.pokemon.ChuDe.cham(g, x, y);
    }

    /** Biểu tượng vẽ bằng nét (không ảnh, không chữ). */
    static void veIcon(Graphics g, int n, int x, int y, int r) {
        // 0.9.7: icon PNG (/nro/icon.png) - chat, nói chuyện, nhặt (hộp đồ), câu cá
        int id = n == 20 || n == 21 ? game.pokemon.BieuTuong.CHAT : n == 22 ? game.pokemon.BieuTuong.HOP_DO
                : n == 23 ? game.pokemon.BieuTuong.CAN_CAU : -1;
        if (id >= 0) {
            int s = Math.max(16, (2 * r * 4 / 5 - 4) / 16 * 16);
            game.pokemon.BieuTuong.ve(g, id, x - s / 2, y - s / 2, s, 0xFFFFFF, 0);
        }
    }

    /**
     * Nút câu (0.9.6.1): đĩa trắng như mọi nút tròn HUD + icon cần câu (BieuTuong.CAN_CAU, ảnh tạm). Trạng thái:
     * chờ thả = bình thường; đang chờ cá = đĩa ấn chìm + nhãn "Chờ"; cá cắn = viền vàng nhấp nháy + nhãn "Kéo!";
     * đã kéo = mờ. Nhấn co 1 px như các nút khác.
     */
    static void veNutCau(Graphics g, int x, int y, int r0) {
        boolean can = game.pokemon.SinhThai.dangCan() && !game.pokemon.SinhThai.daKeo();
        boolean cho = game.pokemon.SinhThai.dangCau() && !can;
        int r = r0 - (tro(x, y, r0 + 2) ? 1 : 0);
        boolean sang = can && (Canvas.gameTick & 4) == 0;
        nen(g, x, y, r, cho ? game.pokemon.ChuDe.NEN_DUOI : 0xFFFFFF, cho ? game.pokemon.ChuDe.NEN_PHU_TOI : game.pokemon.ChuDe.NEN_DUOI, sang);
        int s = r * 6 / 5;
        game.pokemon.BieuTuong.ve(g, game.pokemon.BieuTuong.CAN_CAU, x - s / 2, y - s / 2, s,
                cho ? game.pokemon.ChuDe.CHU_PHU : game.pokemon.ChuDe.CHINH, 0xFFFFFF);
        String nhan = can ? "Kéo!" : (cho ? "Chờ" : null);
        if (nhan != null) {
            game.pokemon.ChuDe.chuGiua(g, nhan, x, y + r - 2, 12, game.pokemon.ChuDe.NHAN_NHO,
                    can ? game.pokemon.ChuDe.DO : game.pokemon.ChuDe.CHU_PHU, 2);
        }
    }

    static final int[] COS8 = {1024, 724, 0, -724, -1024, -724, 0, 724};
    static final int[] SIN8 = {0, 724, 1024, 724, 0, -724, -1024, -724};

    static void veCum(Graphics g, GameScr gs) {
        MainChar mc = gs.mainChar;
        if (MainChar.quickSlot == null) {
            mc.loadQuickSlot();
        }
        game.pokemon.ThanhNhanh.kiem(mc);
        QuickSlot[] bo = MainChar.quickSlot == null ? null : MainChar.quickSlot[GameScr.indexTabSlot];
        long now = System.currentTimeMillis();
        // 4 chiêu
        for (int i = 0; i < 4; i++) {
            if (!game.pokemon.ThanhNhanh.hienO(bo, gs.SLOTINDEXOFKEY[PHIM_CHIEU[i]])) {
                continue;   // 0.9.5: ô trống mà không còn chiêu nào để gán: không hiện "+"
            }
            QuickSlot qs = bo == null ? null : bo[gs.SLOTINDEXOFKEY[PHIM_CHIEU[i]]];
            int x = xChieu(i), y = yChieu(i);
            veO(g, qs, mc, x, y, R_CHIEU - (tro(x, y, R_CHIEU) ? 1 : 0), now);
        }
        // nút đánh
        int xd = xDanh(), yd = yDanh();
        int rd = R_DANH - (tro(xd, yd, R_DANH) ? 2 : 0);
        int nc = HanhDong.loaiNguCanh();
        if (nc == 1 || nc == 2) {
            nenMau(g, xd, yd, rd * 4 / 5, game.pokemon.ChuDe.LA, true);
            veIcon(g, nc == 1 ? 21 : 22, xd, yd, rd);
        } else {
            QuickSlot qs = bo == null ? null : bo[gs.SLOTINDEXOFKEY[5]];
            veO(g, qs, mc, xd, yd, rd, now);
            if (gs.focusedActor != null && gs.focusedActor.isMonster()) {
                g.setColor(GiaoDien.DO);
                int ra = rd * 4 / 5 + 4;
                g.drawRect(xd - ra, yd - ra, 2 * ra - 1, 2 * ra - 1);
            }
        }
        // đổi bộ chiêu (phím 0): chỉ hiện khi có bộ 2 (học quá 4 chiêu / đã gán bộ 2); nhãn = số bộ đang dùng
        if (game.pokemon.ThanhNhanh.coBo2()) {
            int xb = xDoiBo(), yb = yDoiBo();
            nen(g, xb, yb, R_DOI_BO, 0xFFFFFF, game.pokemon.ChuDe.NEN_PHU_TOI, false);
            game.pokemon.ChuDe.chuGiua(g, GameScr.indexTabSlot == 0 ? "1/2" : "2/2", xb, yb - R_DOI_BO, 2 * R_DOI_BO,
                    game.pokemon.ChuDe.SO, game.pokemon.ChuDe.CHU, 2);
        }
        // thuốc
        veThuoc(g, mc, true, now);
        veThuoc(g, mc, false, now);
        // ngữ cảnh: câu cá
        if (nc == 3) {
            veNutCau(g, xNguCanh(), yNguCanh(), R_NGU_CANH);
        }
        // tự động: công tắc viên (tắt = trắng, núm xám trái; bật = xanh lá sáng, núm trắng phải, chữ đậm)
        boolean td = TuDong.dang;
        boolean cho = !td && TuDong.choBat;   // đã xin máy chủ, chờ trả lời
        int xt = xTuDong(), yt = yTuDong();
        int hp = H_TU_DONG;
        // 0.9.7: bảng gỗ "AUTO" (bật = khối xanh lá viền vàng, chờ máy chủ = cam), đèn vuông bên trái
        int mAuto = td ? game.pokemon.KhungNroBang.NUT_LA : cho ? game.pokemon.KhungNroBang.NUT_CAM : game.pokemon.KhungNroBang.NUT_GO;
        if (td) {
            g.setColor(game.pokemon.ChuDe.VANG);
            g.fillRect(xt - 2, yt - 1, W_TU_DONG + 4, hp + 2);
            g.fillRect(xt - 1, yt - 2, W_TU_DONG + 2, hp + 4);
        } else {
            game.pokemon.KhungNro.bong(g, xt, yt, W_TU_DONG, hp);
        }
        if (!game.pokemon.KhungNro.ve(g, mAuto, xt, yt, W_TU_DONG, hp)) {
            GiaoDien.khoi(g, xt, yt, W_TU_DONG, hp, 3, td ? game.pokemon.ChuDe.LA : game.pokemon.ChuDe.KE);
        }
        int rn = hp / 2 - 4;
        int xn = xt + 4 + rn;
        g.setColor(game.pokemon.ChuDe.VIEN);
        g.fillRect(xn - rn - 1, yt + hp / 2 - rn - 1, 2 * rn + 2, 2 * rn + 2);
        g.setColor(td ? 0xB4F08A : cho ? game.pokemon.ChuDe.VANG : 0x4A3424);
        g.fillRect(xn - rn, yt + hp / 2 - rn, 2 * rn, 2 * rn);
        game.pokemon.ChuDe.chuVienGiua(g, "AUTO", xt + 2 * rn + 6 + (W_TU_DONG - 2 * rn - 8) / 2, yt, hp,
                game.pokemon.ChuDe.SO, td ? 0xFFFFFF : 0xF6E6C0, game.pokemon.ChuDe.VIEN, 2);
        // bản đồ không cho Tự động đánh (gói -108): viên tối + ổ khoá (icon PNG /nro/icon.png) thay núm; chạm = báo lý do
        if (!td && game.pokemon.KhoaMap.tuDongKhoa(gs.map)) {
            g.setColor(game.pokemon.ChuDe.VIEN);
            g.fillRect(xn - rn - 1, yt + hp / 2 - rn - 1, 2 * rn + 2, 2 * rn + 2);
            if (!game.pokemon.KhungNro.iconGiua(g, game.pokemon.BieuTuong.KHOA, xn, yt + hp / 2, 1, -1)) {
                game.pokemon.BieuTuong.ve(g, game.pokemon.BieuTuong.KHOA, xn - rn, yt + hp / 2 - rn, 2 * rn, game.pokemon.ChuDe.XAM,
                        game.pokemon.ChuDe.VIEN);
            }
        }
        // bánh răng cài đặt Tự động (đĩa trắng nhỏ)
        int xc = xTdCai(), yc2 = yTdCai(), rc = R_TD_CAI - (tro(xc, yc2, R_TD_CAI + 2) ? 1 : 0);
        nen(g, xc, yc2, rc, 0xFFFFFF, game.pokemon.ChuDe.NEN_DUOI, false);
        int sc2 = rc * 7 / 5;
        game.pokemon.BieuTuong.ve(g, game.pokemon.BieuTuong.CAI_DAT, xc - sc2 / 2, yc2 - sc2 / 2, sc2, game.pokemon.ChuDe.CHU_PHU, 0xFFFFFF);
        // nhấn giữ viên AUTO: vòng tiến độ (0,45 s) rồi mở cài đặt
        if (Canvas.isPointerDown && trongO(Canvas.px, Canvas.py, xt - 2, yt - 2, W_TU_DONG + 4, hp + 4)) {
            if (giuTdLuc == 0) {
                giuTdLuc = now;
            }
            int p = (int) Math.min(360, (now - giuTdLuc) * 360 / 450);
            g.setColor(game.pokemon.ChuDe.CHINH);
            g.drawArc(xc - rc - 2, yc2 - rc - 2, 2 * rc + 4, 2 * rc + 4, 90, -p);
        } else if (!Canvas.isPointerClick) {
            giuTdLuc = 0;
        }
        // tên chiêu vừa dùng
        veTenChieu(g, gs, bo, now);
        veGoiYChieu(g, gs, bo);
    }

    /** Một ô chiêu / thuốc tròn tâm (x, y) bán kính r. */
    static void veO(Graphics g, QuickSlot qs, MainChar mc, int x, int y, int r, long now) {
        // 0.9.7: khung VUÔNG phủ góc nhiều hơn đĩa tròn cùng bán kính -> nửa cạnh 4/5 r (+ viền 2) để các ô chiêu / nút
        // đánh / thuốc cách nhau >= 6 px logic như vòng tròn cũ (vùng chạm giữ nguyên bán kính r)
        r = r * 4 / 5;
        int loai = qs == null ? -1 : qs.quickslotType;
        if (loai == QuickSlot.TYPE_SKILL) {
            int lop = mc.clazz;
            int sk = qs.skillType;
            int he = PokeChieu.he(lop, sk);
            int mau = KieuO.mauHe(he);
            // 0.9.5: icon PNG riêng từng chiêu (huy hiệu tròn màu hệ, tools/pokemon/sinh_icon_chieu.py) + tên viết tắt
            // phía dưới; J2ME / thiếu ảnh -> đĩa màu hệ + biểu tượng hệ
            int oc = game.pokemon.AnhUI.oChieu(lop, sk);
            // 0.9.7: ô VUÔNG hộp gỗ (như NRO) + viền màu hệ + icon chiêu bo góc bên trong
            game.pokemon.KhungNro.bong(g, x - r - 2, y - r - 2, 2 * r + 4, 2 * r + 4);
            if (!game.pokemon.KhungNro.ve(g, game.pokemon.KhungNroBang.O, x - r - 2, y - r - 2, 2 * r + 4, 2 * r + 4)) {
                GiaoDien.khoi(g, x - r - 2, y - r - 2, 2 * r + 4, 2 * r + 4, 3, game.pokemon.ChuDe.VIEN);
            }
            g.setColor(mau);
            g.drawRect(x - r + 1, y - r + 1, 2 * r - 3, 2 * r - 3);
            boolean anh = oc >= 0 && game.pokemon.ChuUI.bo != null
                    && game.pokemon.ChuUI.bo.chieu(g, oc, false, x - r + 3, y - r + 3, 2 * r - 6, false);
            if (!anh) {
                nen(g, x, y, r, GiaoDien.pha(mau, GiaoDien.TRANG, 1, 4), mau, false);
                int ic = r * 9 / 10;
                game.pokemon.BieuTuong.ve(g, game.pokemon.BieuTuong.heIcon(he), x - ic / 2, y - r * 3 / 4, ic, 0xFFFFFF, mau);
            }
            String vt = vietTat(lop, sk);
            if (vt != null) {
                // 0.9.6: huy hiệu hệ Thường là đĩa kem -> nhãn tối cho dễ đọc
                // tên viết tắt sát đáy BÊN TRONG ô (đo theo font đang dùng - font ảnh J2ME cao hơn)
                int vaiVt = r >= 24 ? game.pokemon.ChuDe.SO : game.pokemon.ChuDe.PHU;
                int yVt = Math.min(anh ? y + r * 2 / 5 : y + r / 4 - 1, y + r - game.pokemon.ChuDe.cao(vaiVt));
                if (r < 24) {
                    // 0.9.8: huy hiệu PP (r < 24) nằm giữa đáy ô - tên đặt NGAY TRÊN huy hiệu, không đè chữ
                    yVt = Math.min(yVt, y + r - Font.smallFont.getHeight() / 2 - 2 - game.pokemon.ChuDe.cao(vaiVt));
                }
                game.pokemon.ChuDe.chuVien(g, game.pokemon.ChuDe.cat(vt, vaiVt, 2 * r - 2), x, yVt, vaiVt, 0xFFFFFF, game.pokemon.ChuDe.VIEN, 2);
            }
            long da = now - mc.timeLastUseSkills[sk];
            long tong = mc.coolDownSkill[sk];
            int cap = game.model.Char.skillLevelLearnt != null && sk < game.model.Char.skillLevelLearnt.length ? game.model.Char.skillLevelLearnt[sk] : 0;
            int pp = 0;
            try {
                pp = game.model.skill.SkillManager.getSkillMP(sk, cap, lop);
            } catch (Throwable e) {
                pp = 0;
            }
            if (da < tong && tong > 0) {
                // 0.9.7: hồi chiêu = màn tối rút dần từ trên xuống (ô vuông pixel)
                int hh = (int) ((2 * r - 2) * (tong - da) / tong);
                KieuO.phu(g, x - r + 1, y + r - 1 - hh, 2 * r - 2, hh, 0xB0100804);
                g.setColor(GiaoDien.VANG);
                g.fillRect(x - r + 1, y + r - 1 - hh, 2 * r - 2, 1);
                Font.borderFont.drawString(g, so((int) ((tong - da + 999) / 1000)), x, y - Font.borderFont.getHeight() / 2, Font.CENTER);
            } else if (pp > 0 && mc.mp < pp) {
                g.setColor(GiaoDien.MP_XANH);
                g.drawRect(x - r - 1, y - r - 1, 2 * r + 1, 2 * r + 1);
            }
            if (pp > 0 && r < 24) {
                String s = so(pp);
                int w = Font.smallFont.getWidth(s) + 4;
                int hh = Font.smallFont.getHeight();
                GiaoDien.khoi(g, x - w / 2, y + r - hh / 2 - 1, w, hh, 3, mc.mp < pp ? game.pokemon.ChuDe.DO_TOI : game.pokemon.ChuDe.XANH_TOI);
                Font.smallFont.drawString(g, s, x, y + r - hh / 2 - 1, Font.CENTER);
            }
            return;
        }
        if (loai == QuickSlot.TYPE_POTION) {
            nen(g, x, y, r, 0xFFFFFF, game.pokemon.ChuDe.XANH_NHAT, false);
            try {
                Res.paintPotion(g, MainChar.listPotion[qs.potionType].index, x, y, 3);
                KieuO.soLuong(g, x + r, y + r, KieuO.soGon(mc.potions[qs.potionType]));
            } catch (Throwable e) {
                // chưa có bảng thuốc
            }
            return;
        }
        // ô trống thật sự: dấu + mờ (chạm = bảng gán chiêu / thuốc)
        // 0.9.7: ô hộp gỗ trống + icon PNG "+" (mở rộng / gán)
        game.pokemon.Bang.oDo(g, x - r, y - r, 2 * r, false, false, null, null);
        game.pokemon.BieuTuong.ve(g, game.pokemon.BieuTuong.MO_RONG, x - 8, y - 8, 16, 0, 0);
    }

    private static final String[][] VIET_TAT = new String[8][16];

    /** Tên chiêu viết tắt (từ đầu, tối đa 6 ký tự) - nhớ sẵn. */
    static String vietTat(int lop, int sk) {
        if (lop < 0 || lop >= VIET_TAT.length || sk < 0 || sk >= VIET_TAT[lop].length) {
            return null;
        }
        String v = VIET_TAT[lop][sk];
        if (v == null) {
            String t = PokeChieu.ten(lop, sk);
            if (t == null) {
                return null;
            }
            int sp = t.indexOf(' ');
            v = sp > 0 ? t.substring(0, sp) : t;
            if (v.length() > 6) {
                v = v.substring(0, 5) + ".";
            }
            VIET_TAT[lop][sk] = v;
        }
        return v;
    }

    // ------------------------------------------------------------------ nhấn giữ / di chuột: thông tin chiêu
    static int giuO = -1;
    static long giuLuc;
    static boolean daGiu;

    /** Nhấn giữ nút chiêu > 0,45 giây: thẻ tên, hệ, PP, hồi chiêu (thả ra không đánh). */
    static void veGoiYChieu(Graphics g, GameScr gs, QuickSlot[] bo) {
        int dang = -1;
        if (Canvas.isPointerDown && !game.pokemon.NgamTay.dangGiu()) {
            for (int i = 0; i < 5; i++) {
                int x = i < 4 ? xChieu(i) : xDanh(), y = i < 4 ? yChieu(i) : yDanh(), r = i < 4 ? R_CHIEU : R_DANH;
                if (trongTron(Canvas.px, Canvas.py, x, y, r)) {
                    dang = i;
                }
            }
        }
        long now = System.currentTimeMillis();
        if (dang != giuO) {
            giuO = dang;
            giuLuc = now;
            if (dang < 0) {
                return;
            }
        }
        if (dang < 0 || now - giuLuc < 450 || bo == null) {
            return;
        }
        QuickSlot qs = bo[gs.SLOTINDEXOFKEY[dang < 4 ? PHIM_CHIEU[dang] : 5]];
        if (qs == null || qs.quickslotType != QuickSlot.TYPE_SKILL) {
            return;
        }
        daGiu = true;
        int lop = gs.mainChar.clazz, sk = qs.skillType;
        String ten = PokeChieu.ten(lop, sk);
        int he = PokeChieu.he(lop, sk);
        int cap = game.model.Char.skillLevelLearnt != null && sk < game.model.Char.skillLevelLearnt.length ? game.model.Char.skillLevelLearnt[sk] : 0;
        int pp = 0;
        try {
            pp = game.model.skill.SkillManager.getSkillMP(sk, cap, lop);
        } catch (Throwable e) {
            pp = 0;
        }
        long hoi = gs.mainChar.coolDownSkill == null || sk >= gs.mainChar.coolDownSkill.length ? 0 : gs.mainChar.coolDownSkill[sk];
        StringBuffer b2 = new StringBuffer("Hệ ").append(KieuO.tenHe(he)).append(" · Cấp ").append(cap);
        if (pp > 0) {
            b2.append(" · PP ").append(pp);
        }
        if (hoi >= 100) {
            b2.append(" · hồi ").append(hoi / 100 / 10.0).append(" s");
        }
        if (sk == 0 && pp <= 0) {
            b2.append(" · đòn cơ bản");
        }
        String d2 = b2.toString();
        String d3 = PokeChieu.moTa(lop, sk);
        int lh = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU);
        int h = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.NHAN) + 2 * lh + 12;
        int ic = h - 14;   // icon chiêu bên trái
        int w = Math.max(game.pokemon.ChuDe.rong(ten, game.pokemon.ChuDe.NHAN), game.pokemon.ChuDe.rong(d2, game.pokemon.ChuDe.PHU));
        w = Math.min(Canvas.w - 20, Math.max(w, Math.min(220, game.pokemon.ChuDe.rong(d3, game.pokemon.ChuDe.PHU))) + 22 + ic);
        int x = Math.max(4, Math.min(Canvas.w - w - 4, xChieu(1) - w / 2));
        int y = Math.max(4, yChieu(2) - R_CHIEU - h - 6);
        game.pokemon.ChuDe.the(g, x, y, w, h, 7);
        GiaoDien.khoi(g, x + 1, y + 1, 4, h - 2, 2, KieuO.mauHe(he));
        KieuO.oChieuSk(g, x + 10, y + 7, ic, ic, lop, sk, false);
        int xc = x + 16 + ic;
        game.pokemon.ChuDe.chu(g, ten, xc, y + 5, game.pokemon.ChuDe.NHAN, game.pokemon.ChuDe.CHU, 0);
        int yy = y + 5 + game.pokemon.ChuDe.cao(game.pokemon.ChuDe.NHAN);
        game.pokemon.ChuDe.chu(g, d2, xc, yy, game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU_PHU, 0);
        game.pokemon.ChuDe.chu(g, game.pokemon.ChuDe.cat(d3, game.pokemon.ChuDe.PHU, w - 22 - ic), xc, yy + lh, game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU_PHU, 0);
    }

    static final int[] THUOC_HP = {1, 2, 3, 21, 22, 93, 94};
    static final int[] THUOC_PP = {4, 5, 6, 23, 24, 95, 96};

    static int loaiThuoc(MainChar mc, boolean hp) {
        int[] ds = hp ? THUOC_HP : THUOC_PP;
        if (mc.potions == null) {
            return -1;
        }
        // 0.9.5: dùng bình MẠNH NHẤT đang có (danh sách xếp yếu -> mạnh)
        for (int i = ds.length - 1; i >= 0; i--) {
            if (ds[i] < mc.potions.length && mc.potions[ds[i]] > 0) {
                return ds[i];
            }
        }
        return -1;
    }

    /** Nút thuốc HP (đỏ) / PP (xanh): bình thuốc trắng trên thân màu, nhãn HP / PP, số lượng; hết thuốc = xám. */
    static void veThuoc(Graphics g, MainChar mc, boolean hp, long now) {
        int x = xThuoc(), y = yThuoc(hp);
        int r = (R_THUOC - (tro(x, y, R_THUOC) ? 1 : 0)) * 4 / 5;   // 0.9.7: khung vuông nhỏ hơn đĩa (cách ô chiêu)
        int loai = loaiThuoc(mc, hp);
        int mau = loai < 0 ? 0xB9C1CF : (hp ? game.pokemon.ChuDe.DO : game.pokemon.ChuDe.PP);
        if (loai < 0) {
            nenMau(g, x, y, r, mau, false);
            int s = r + 2;
            game.pokemon.BieuTuong.ve(g, game.pokemon.BieuTuong.THUOC, x - s / 2 - 3, y - s / 2, s, 0xFFFFFF, mau);
            game.pokemon.ChuDe.chuGiua(g, hp ? "HP" : "PP", x + r / 2 + 1, y - 5, 10, game.pokemon.ChuDe.NHAN_NHO, 0xFFFFFF, 2);
            return;
        }
        // ảnh thật của bình đang dùng trên nền trắng, vành màu HP (đỏ) / PP (xanh), nhãn nhỏ góc trái
        // 0.9.7: ô hộp gỗ + bình thật, nhãn HP / PP khối màu góc trái
        game.pokemon.Bang.nutVuong(g, x, y, r, game.pokemon.KhungNroBang.NUT_GO, false, false);
        game.pokemon.ManNhanVat.veThuocHud(g, loai, x, y, 2 * r);
        // nhãn HP / PP: khối pixel cỡ theo chữ (font ảnh J2ME cao hơn)
        int wNh = Math.max(14, game.pokemon.ChuDe.rong("HP", game.pokemon.ChuDe.PHU) + 4), hNh = Math.max(9, game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU));
        GiaoDien.khoiPixel(g, x - r - 2, y - r - 2, wNh, hNh, mau);
        game.pokemon.ChuDe.chuGiua(g, hp ? "HP" : "PP", x - r - 2 + wNh / 2, y - r - 2, hNh, game.pokemon.ChuDe.PHU, 0xFFFFFF, 2);
        try {
            long da = now - mc.potionLastTimeUse[loai];
            long tong = MainChar.listPotion[loai].delay;
            if (da < tong && tong > 0) {
                int hh = (int) ((2 * r - 2) * (tong - da) / tong);
                KieuO.phu(g, x - r + 1, y + r - 1 - hh, 2 * r - 2, hh, 0x90100804);
            }
            KieuO.soLuong(g, x + r + 2, y + r, KieuO.soGon(mc.potions[loai]));
        } catch (Throwable e) {
            // chưa có bảng thuốc
        }
    }

    static void veTenChieu(Graphics g, GameScr gs, QuickSlot[] bo, long now) {
        if (bo == null) {
            return;
        }
        int kMoi = -1;
        long tMoi = 0;
        for (int k = 0; k < bo.length; k++) {
            QuickSlot qs = bo[k];
            if (qs != null && qs.quickslotType == QuickSlot.TYPE_SKILL) {
                long t = gs.mainChar.timeLastUseSkills[qs.skillType];
                if (t > tMoi && now - t < 1300) {
                    tMoi = t;
                    kMoi = k;
                }
            }
        }
        if (kMoi < 0) {
            return;
        }
        QuickSlot qs = bo[kMoi];
        int lop = gs.mainChar.clazz;
        String ten = PokeChieu.ten(lop, qs.skillType);
        if (ten == null) {
            return;
        }
        // 0.9.6: viên trắng + chấm màu hệ, trên nút Poké Ball
        int wt = Math.min(118, game.pokemon.ChuDe.rong(ten, game.pokemon.ChuDe.NHAN) + 22);
        int xt = Canvas.hw - wt / 2;
        int yt = yTenChieu();
        int mau = KieuO.mauHe(PokeChieu.he(lop, qs.skillType));
        // 0.9.7: bảng tối chỉ vàng + ô màu hệ
        if (!game.pokemon.KhungNro.ve(g, game.pokemon.KhungNroBang.TOOLTIP, xt, yt, wt, 16)) {
            GiaoDien.khoi(g, xt, yt, wt, 16, 3, 0x2B1A10);
        }
        GiaoDien.khoiPixel(g, xt + 4, yt + 4, 8, 8, mau);
        game.pokemon.ChuDe.chuGiua(g, game.pokemon.ChuDe.cat(ten, game.pokemon.ChuDe.NHAN, wt - 20), xt + 15, yt, 16, game.pokemon.ChuDe.NHAN, 0xF6E6C0, 0);
    }

    /** Đỉnh viên tên chiêu vừa dùng (trên nút Poké Ball giữa đáy). */
    static int yTenChieu() {
        return yNut(N_MENU) - R_MENU - 24;
    }

    // ------------------------------------------------------------------ chat thu gọn (trái dưới)

    private static final Object[] DONG = new Object[DONG_CHAT];
    private static final long[] DONG_LUC = new long[DONG_CHAT];

    static long lucCua(Object o, long now) {
        for (int i = 0; i < DONG_CHAT; i++) {
            if (DONG[i] == o) {
                return DONG_LUC[i];
            }
        }
        // dòng mới: thay dòng cũ nhất
        int cu = 0;
        for (int i = 1; i < DONG_CHAT; i++) {
            if (DONG_LUC[i] < DONG_LUC[cu]) {
                cu = i;
            }
        }
        DONG[cu] = o;
        DONG_LUC[cu] = now;
        return now;
    }

    static void veChat(Graphics g, GameScr gs) {
        int x = xChat(), y = yChat();
        int rc = R_CHAT - (tro(x, y, R_CHAT) ? 1 : 0);
        nen(g, x, y, rc, 0xFFFFFF, game.pokemon.ChuDe.NEN_PHU, false);
        int sc = rc * 6 / 5;
        game.pokemon.BieuTuong.ve(g, game.pokemon.BieuTuong.CHAT, x - sc / 2, y - sc / 2, sc, game.pokemon.ChuDe.XANH, 0xFFFFFF);
        if (game.pokemon.ChamMoi.hudChat()) {
            game.pokemon.ChuDe.cham(g, x + rc - 2, y - rc + 2);   // 0.9.8: tin riêng / bang hội / đội chưa đọc
        }
        java.util.Vector ls = gs.chatHistory;
        int n = ls == null ? 0 : ls.size();
        if (n == 0) {
            return;
        }
        long now = System.currentTimeMillis();
        int dong = n < DONG_CHAT ? n : DONG_CHAT;
        int x0 = x + R_CHAT + 6;
        int yDay = Canvas.h - 4;
        Font f = Font.arialFontW;
        for (int i = 0; i < dong; i++) {
            Object o = ls.elementAt(n - 1 - i);
            if (!(o instanceof ChatInfo)) {
                continue;
            }
            long tuoi = now - lucCua(o, now);
            if (tuoi > 9000) {
                continue;   // mờ hẳn sau 9 giây (mở ô chat để xem lại)
            }
            ChatInfo ci = (ChatInfo) o;
            String s = ci.name == null || ci.name.length() == 0 ? ci.content : new StringBuffer(ci.name).append(": ").append(ci.content).toString();
            s = GiaoDien.catChu(f, s, W_CHAT - 8);
            int yy = yDay - (i + 1) * LH_CHAT;
            int w = f.getWidth(s) + 8;
            KieuO.phu(g, x0, yy, w, LH_CHAT - 1, tuoi < 6000 ? 0x80101828 : 0x40101828);
            f.drawString(g, s, x0 + 4, yy, Font.LEFT);
        }
    }

    // ------------------------------------------------------------------ vòng chọn dưới chân mục tiêu (lớp thế giới)

    public static void veVongChon(Graphics g, Actor a) {
        if (a == null || a.catagory == 10 || a.catagory == 11 || a.catagory == 12 || a.catagory == 100) {
            return;
        }
        int w = a.width < 20 ? 20 : a.width;
        if (w > 48) {
            w = 48;
        }
        int nhip = (Canvas.gameTick >> 1) & 3;
        int rw = w / 2 + 3 + (nhip == 3 ? 1 : nhip);
        int rh = rw / 2;
        int mau = a.isMonster() ? GiaoDien.DO : a.isNPC() || a.catagory == 2 ? GiaoDien.LA : a.isItemCanGet() ? GiaoDien.VANG : GiaoDien.XANH_SANG;
        g.setColor(GiaoDien.VIEN);
        g.drawArc(a.x - rw - 1, a.y - rh - 1, 2 * rw + 2, 2 * rh + 2, 0, 360);
        g.setColor(mau);
        g.drawArc(a.x - rw, a.y - rh, 2 * rw, 2 * rh, 0, 360);
        g.drawArc(a.x - rw + 1, a.y - rh, 2 * rw - 2, 2 * rh, 0, 360);
    }

    // ------------------------------------------------------------------ bản đồ nhỏ + bản đồ cả map

    /** GameScr.paintMiniMap (màn rộng): đầu thẻ tên map, ảnh bản đồ, chấm người / NPC / cổng, vùng + toạ độ dưới. */
    static void veMiniMap(Graphics g, GameScr gs, Position pos) {
        Canvas.resetTrans(g);
        if (pos == null || gs.hideGUI == 2) {
            return;
        }
        int x0 = pos.x, y0 = pos.y, w = GameScr.wMiniMap, h = GameScr.hMiniMap;
        // đầu thẻ: tên map (cắt vừa bề ngang)
        String ten = GameScr.nameMap;
        if (ten != null && ten.length() > 0) {
            ten = GiaoDien.catChu(Font.borderFont, ten, w + 20);
            int wt = Font.borderFont.getWidth(ten) + 12;
            // 0.9.7: biển tên map = bảng tối chỉ vàng, chữ giấy da
            if (!game.pokemon.KhungNro.ve(g, game.pokemon.KhungNroBang.TOOLTIP, Canvas.w - 4 - wt, Y_DAU_MM, wt, 15)) {
                GiaoDien.khoi(g, Canvas.w - 4 - wt, Y_DAU_MM, wt, 15, 3, 0x2B1A10);
            }
            game.pokemon.ChuDe.chuGiua(g, ten, Canvas.w - 4 - wt / 2, Y_DAU_MM, 15, game.pokemon.ChuDe.NHAN_NHO, 0xF6E6C0, 2);
        }
        // khung gỗ quanh bản đồ nhỏ
        game.pokemon.KhungNro.bong(g, x0 - 4, y0 - 4, w + 8, h + 8);
        if (!game.pokemon.KhungNro.ve(g, game.pokemon.KhungNroBang.NUT_GO, x0 - 4, y0 - 4, w + 8, h + 8)) {
            GiaoDien.khoi(g, x0 - 3, y0 - 3, w + 6, h + 6, 3, 0x7A3E18);
        }
        g.setClip(x0, y0, w, h);
        g.translate(x0 - GameScr.cmxMini, y0 - GameScr.cmyMini);
        g.setColor(0x0E1A30);
        g.fillRect(GameScr.cmxMini, GameScr.cmyMini, w, h);
        if (Tilemap.imgMap != null) {
            g.drawImage(Tilemap.imgMap, 0, 0, 0);
        }
        try {
            gs.paintPosNPCQuest(g);
        } catch (Throwable e) {
            // bỏ qua
        }
        game.pokemon.SinhThai.veMini(g);
        // người khác (xanh lá), nhóm (đỏ cam), NPC (vàng), quái gần (đỏ)
        int n = gs.actors.size();
        for (int i = 0; i < n; i++) {
            Actor a = (Actor) gs.actors.elementAt(i);
            if (a == gs.mainChar) {
                continue;
            }
            int ax = a.x >> 4, ay = a.y >> 4;
            if (a.catagory == 2 || a.isNPC()) {
                g.setColor(GiaoDien.VANG);
                g.fillRect(ax - 1, ay - 1, 3, 3);
            } else if (a.catagory == 0) {
                g.setColor(GiaoDien.HP_XANH);
                g.fillRect(ax, ay, 2, 2);
            } else if (a.isMonster() && ((LiveActor) a).hp > 0) {
                g.setColor(GiaoDien.DO);
                g.fillRect(ax, ay, 1, 1);
            }
        }
        g.setColor(16317005);
        int sp = Tilemap.pointPop.size();
        for (int i = 0; i < sp; i++) {
            PopupName p = (PopupName) Tilemap.pointPop.elementAt(i);
            g.fillRect((p.x >> 4) - 1, (p.y >> 4) - 1, 3, 3);
        }
        int mx = gs.mainChar.x >> 4, my = gs.mainChar.y >> 4;
        g.setColor(GiaoDien.TRANG);
        g.fillRect(mx - 2, my - 2, 5, 5);
        g.setColor(0x2050FF);
        g.fillRect(mx - 1, my - 1, 3, 3);
        Canvas.resetTrans(g);
        // 0.9.5: bỏ dòng "vùng + toạ độ" dưới bản đồ nhỏ (trông như chữ gỡ lỗi) - tên map đã ở viên trên
    }

    private static int tdX = -1, tdY = -1;
    private static String tdChu = "";

    static String toaDo(int x, int y) {
        if (x != tdX || y != tdY) {
            tdX = x;
            tdY = y;
            tdChu = new StringBuffer(8).append(x).append('.').append(y).toString();
        }
        return tdChu;
    }

    /** Bản đồ nhỏ có vẽ (map nhỏ hơn màn thì GameScr.paintMiniMap bỏ). */
    static boolean coMiniMap() {
        return Tilemap.imgMap != null && !(Tilemap.w < Canvas.w / 16 && Tilemap.h < Canvas.h / 16);
    }

    /** Đáy vùng bản đồ nhỏ + chữ dưới (icon buff / đếm giờ đặt dưới). */
    static int dayMiniMap(GameScr gs) {
        return Y_MM + GameScr.hMiniMap + 6;
    }

    private static Image anhTo;
    private static int anhToLv = -1, anhToK;
    private static Image anhToGoc;

    /** Hệ số phóng ảnh bản đồ cả map (1 px / ô -> k px / ô). */
    static int heSoTo() {
        Image m = Tilemap.imgMap;
        if (m == null) {
            return 1;
        }
        int k = Math.min((Canvas.w - 60) / Math.max(1, m.getWidth()), (Canvas.h - 70) / Math.max(1, m.getHeight()));
        return k < 1 ? 1 : (k > 6 ? 6 : k);
    }

    static Image anhBanDoTo() {
        Image m = Tilemap.imgMap;
        if (m == null) {
            return null;
        }
        int k = heSoTo();
        if (anhTo != null && anhToLv == Tilemap.lv && anhToK == k && anhToGoc == m) {
            return anhTo;
        }
        try {
            int w = m.getWidth(), h = m.getHeight();
            int[] goc = new int[w * h];
            m.getRGB(goc, 0, w, 0, 0, w, h);
            int W = w * k, H = h * k;
            int[] px = new int[W * H];
            for (int y = 0; y < H; y++) {
                int hang = (y / k) * w;
                for (int x = 0; x < W; x++) {
                    px[y * W + x] = goc[hang + x / k] | 0xFF000000;
                }
            }
            anhTo = Image.createRGBImage(px, W, H, false);
            anhToLv = Tilemap.lv;
            anhToK = k;
            anhToGoc = m;
        } catch (Throwable e) {
            anhTo = null;
        }
        return anhTo;
    }

    static int[] khungBanDoTo() {
        Image m = Tilemap.imgMap;
        int k = heSoTo();
        int w = (m == null ? 100 : m.getWidth()) * k, h = (m == null ? 60 : m.getHeight()) * k;
        return new int[]{(Canvas.w - w) / 2, (Canvas.h - h) / 2 + 8, w, h, k};
    }

    static void veBanDoTo(Graphics g, GameScr gs) {
        Image a = anhBanDoTo();
        if (a == null) {
            banDoTo = false;
            return;
        }
        int[] kh = khungBanDoTo();
        int x0 = kh[0], y0 = kh[1], w = kh[2], h = kh[3], k = kh[4];
        GiaoDien.bang(g, x0 - 8, y0 - 26, w + 16, h + 34);
        String ten = GameScr.nameMap == null ? "" : GameScr.nameMap;
        Font.borderFont.drawString(g, ten, Canvas.hw, y0 - 22, Font.CENTER);
        // nút đóng (x) góc phải
        int xx = x0 + w - 4, yx = y0 - 16;
        nen(g, xx, yx, 8, 0xFF6A4D, 0x9E1520, false);
        g.setColor(GiaoDien.TRANG);
        g.drawLine(xx - 3, yx - 3, xx + 3, yx + 3);
        g.drawLine(xx - 3, yx + 3, xx + 3, yx - 3);
        g.drawImage(a, x0, y0, 0);
        g.setClip(x0, y0, w, h);
        int n = gs.actors.size();
        for (int i = 0; i < n; i++) {
            Actor ac = (Actor) gs.actors.elementAt(i);
            if (ac == gs.mainChar) {
                continue;
            }
            int ax = x0 + (ac.x * k >> 4), ay = y0 + (ac.y * k >> 4);
            if (ac.catagory == 2 || ac.isNPC()) {
                g.setColor(GiaoDien.VIEN);
                g.fillRect(ax - 3, ay - 3, 7, 7);
                g.setColor(GiaoDien.VANG);
                g.fillRect(ax - 2, ay - 2, 5, 5);
            } else if (ac.catagory == 0) {
                g.setColor(GiaoDien.HP_XANH);
                g.fillRect(ax - 1, ay - 1, 3, 3);
            } else if (ac.isMonster() && ((LiveActor) ac).hp > 0) {
                g.setColor(GiaoDien.DO);
                g.fillRect(ax - 1, ay - 1, 2, 2);
            }
        }
        int sp = Tilemap.pointPop.size();
        for (int i = 0; i < sp; i++) {
            PopupName p = (PopupName) Tilemap.pointPop.elementAt(i);
            int px = x0 + (p.x * k >> 4), py = y0 + (p.y * k >> 4);
            g.setColor(GiaoDien.VIEN);
            g.fillRect(px - 3, py - 3, 7, 7);
            g.setColor(16317005);
            g.fillRect(px - 2, py - 2, 5, 5);
        }
        int mx = x0 + (gs.mainChar.x * k >> 4), my = y0 + (gs.mainChar.y * k >> 4);
        g.setColor(GiaoDien.TRANG);
        g.fillArc(mx - 4, my - 4, 9, 9, 0, 360);
        g.setColor(0x2050FF);
        g.fillArc(mx - 3, my - 3, 7, 7, 0, 360);
        Canvas.resetTrans(g);
    }

    // ------------------------------------------------------------------ chạm

    static boolean trongTron(int px, int py, int x, int y, int r) {
        int dx = px - x, dy = py - y;
        return dx * dx + dy * dy <= r * r;
    }

    static boolean trongO(int px, int py, int x, int y, int w, int h) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    /**
     * Mã thứ trúng tại (x, y) toạ độ logic: 0..7 nút trên, 10 thẻ trạng thái, 11 bản đồ nhỏ, 20..23 chiêu, 24 đánh,
     * 25 đổi bộ, 26 / 27 thuốc HP / PP, 28 ngữ cảnh, 29 tự động, 31 cài đặt tự động, 30 chat, 40 bản đồ to (trong ảnh), 41 bản đồ to (đóng);
     * -1 = không trúng.
     */
    static int trung(GameScr gs, int x, int y) {
        if (banDoTo) {
            int[] kh = khungBanDoTo();
            if (trongTron(x, y, kh[0] + kh[2] - 4, kh[1] - 16, 11)) {
                return 41;
            }
            if (trongO(x, y, kh[0], kh[1], kh[2], kh[3])) {
                return 40;
            }
            return 41;   // ngoài bảng: đóng
        }
        BoNut b = BO_CHAM;
        synchronized (b) {
            lapNut(gs, b);
            for (int i = 0; i < b.n; i++) {
                if (b.r[i] > 0 ? trongTron(x, y, b.x[i], b.y[i], b.r[i])
                        : trongO(x, y, b.x[i] - b.le[i], b.y[i] - b.le[i], b.w[i] + 2 * b.le[i], b.h[i] + 2 * b.le[i])) {
                    return b.ma[i];
                }
            }
        }
        return -1;
    }

    /** 0.9.8 ngắm tay: nút chiêu / đánh tại (x, y) -> {phím, tâm x, tâm y, bán kính}; null = không phải. */
    public static int[] oChieuTai(GameScr gs, int x, int y) {
        if (banDoTo) {
            return null;
        }
        int ma = trung(gs, x, y);
        if (ma >= 20 && ma <= 23) {
            return new int[]{PHIM_CHIEU[ma - 20], xChieu(ma - 20), yChieu(ma - 20), R_CHIEU};
        }
        if (ma == 24 && HanhDong.loaiNguCanh() != 1) {
            return new int[]{5, xDanh(), yDanh(), R_DANH};
        }
        return null;
    }

    /** 0.9.8 ngắm tay: thả tay sau khi ngắm - bỏ cờ "nhấn giữ xem thông tin chiêu" (khỏi nuốt cú chạm sau). */
    public static void boGiu() {
        daGiu = false;
        giuO = -1;
    }

    /** PC: con trỏ bàn tay (game.pokemon.HanhDong.laVungBam). */
    public static boolean trungNut(int x, int y) {
        GameScr gs = Canvas.gameScr;
        return bat() && gs != null && trung(gs, x, y) >= 0;
    }

    /** GameScr.updatePoint (màn rộng): true = cú chạm thuộc HUD (đã xử lý, không đi / chọn trong thế giới). */
    static boolean cham(GameScr gs) {
        if (!Canvas.isPointerClick) {
            return banDoTo;   // bảng bản đồ to mở: nuốt cả nhấn giữ
        }
        int ma = trung(gs, Canvas.px, Canvas.py);
        if (ma < 0) {
            return false;
        }
        if (daGiu && ma >= 20 && ma <= 24) {
            // vừa nhấn giữ xem thông tin chiêu: thả ra không đánh
            daGiu = false;
            Canvas.isPointerClick = false;
            Canvas.isPointerDown = false;
            return true;
        }
        daGiu = false;
        Canvas.isPointerClick = false;
        Canvas.isPointerDown = false;
        lam(gs, ma, Canvas.px, Canvas.py);
        return true;
    }

    static void lam(GameScr gs, int ma, int x, int y) {
        switch (ma) {
            case N_MENU:
                HanhDong.moMenu();   // 0.9.3: menu lưới ô to (2 chạm tới mọi việc), ô "Khác" = menu chữ cũ
                break;
            case N_TUI:
                game.pokemon.ManNhanVat.moHanhTrang();
                break;
            case N_NV:
                game.pokemon.ManNhiemVu.mo();
                break;
            case N_BAN_DO:
                // 0.9.5: một lối vào bản đồ: bản đồ cả map của HUD (chạm điểm = đi tới); bản đồ nhỏ chỉ để xem
                banDoTo = anhBanDoTo() != null;
                if (!banDoTo) {
                    HanhDong.moBanDoLon();
                }
                break;
            case 10:
                HanhDong.moHoSo();
                break;
            case 11:
                banDoTo = anhBanDoTo() != null;
                break;
            case 20:
            case 21:
            case 22:
            case 23:
                danh(gs, PHIM_CHIEU[ma - 20]);
                break;
            case 24:
                if (HanhDong.loaiNguCanh() == 1 && HanhDong.nguCanh()) {
                    break;
                }
                danh(gs, 5);
                break;
            case 25:
                GameScr.doiBoChieu();   // viên "1/2": đổi bộ ô ngay (trước: keyPressed[0] không ai đọc -> chạm không ăn)
                break;
            case 26:
            case 27: {
                int loai = loaiThuoc(gs.mainChar, ma == 26);
                if (loai >= 0) {
                    gs.doUsePotion(loai);
                }
                break;
            }
            case 28:
                HanhDong.cauCa();
                break;
            case 29:
                if (giuTdLuc > 0 && System.currentTimeMillis() - giuTdLuc >= 450) {
                    BangTuDong.mo();   // nhấn giữ viên AUTO = cài đặt
                } else {
                    TuDong.batTat();
                }
                giuTdLuc = 0;
                break;
            case 31:
                BangTuDong.mo();
                break;
            case 30:
                game.pokemon.ManChat.mo();   // 0.9.8: bảng chat nhiều kênh, mở lại tab dùng lần cuối
                break;
            case 40: {
                int[] kh = khungBanDoTo();
                int tx = (x - kh[0]) / kh[4], ty = (y - kh[1]) / kh[4];
                banDoTo = false;
                diToi(gs, tx, ty);
                break;
            }
            case 41:
                banDoTo = false;
                break;
            default:
                break;
        }
    }

    /** Cài đặt: bản jar = Menu nhanh (âm thanh, nút); bản PC vá thành bảng Cài đặt của cửa sổ. */
    static void moCaiDat() {
        MenuNhanh.moBang();   // jar: Menu nhanh
    }

    /** Nút đánh / chiêu: chưa có mục tiêu thì tự chọn quái gần nhất (như bấm phím chiêu trên bàn phím có mục tiêu). */
    static void danh(GameScr gs, int phim) {
        if (MainChar.quickSlot != null && game.pokemon.ChonChieuNhanh.chamO(gs.SLOTINDEXOFKEY[phim], false)) {
            return;   // ô trống: bảng gán chiêu / thuốc
        }
        if (gs.focusedActor == null) {
            HanhDong.tuChonQuai(gs);
        }
        Canvas.keyPressed[phim] = true;
    }

    /** Đi tới ô (tx, ty) từ bản đồ to: lưới tìm đường cả map (cmxMini / cmyMini = 0 khi tìm). */
    static void diToi(GameScr gs, int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= Tilemap.w || ty >= Tilemap.h) {
            return;
        }
        int px = tx * 16 + 8, py = ty * 16 + 8;
        if (Tilemap.tileTypeAtPixel(px, py, 2)) {
            return;   // ô chặn
        }
        int cx = GameScr.cmxMini, cy = GameScr.cmyMini;
        GameScr.cmxMini = 0;
        GameScr.cmyMini = 0;
        try {
            HanhDong.boChon();
            gs.findRoad2(px, py);
            HanhDong.chamDat(px, py);
        } finally {
            GameScr.cmxMini = cx;
            GameScr.cmyMini = cy;
        }
    }

    // ------------------------------------------------------------------ kiểm chồng (bố cục 640x360 và màn đang chạy)

    /** Hình chữ nhật mọi phần HUD (toạ độ logic): {tên, x, y, w, h}. */
    public static Object[][] cacVung(GameScr gs) {
        java.util.Vector v = new java.util.Vector();
        v.addElement(new Object[]{"the", new int[]{0, 0, rongThe(), caoThe()}});
        v.addElement(new Object[]{"muc_tieu", new int[]{(Canvas.w - 140) / 2, 0, 140, 34}});
        int xt = xNutTren(0) - R_TREN - 1;
        v.addElement(new Object[]{"nut_tren", new int[]{xt, Y_TREN - R_TREN - 1, Canvas.w - xt, 2 * R_TREN + 3}});
        v.addElement(new Object[]{"menu", tron(xNutTren(N_MENU), yNut(N_MENU), R_MENU + 2)});
        Position p = gs == null ? null : gs.posMiniMapHud();
        int wm = GameScr.wMiniMap > 0 ? GameScr.wMiniMap : MM_W, hm = GameScr.hMiniMap > 0 ? GameScr.hMiniMap : MM_H;
        int xm = p != null ? p.x : Canvas.w - wm - 4;
        v.addElement(new Object[]{"ban_do_nho", new int[]{xm - 2, Y_DAU_MM, wm + 4, Y_MM + hm + 14 - Y_DAU_MM}});
        for (int i = 0; i < 4; i++) {
            v.addElement(new Object[]{"chieu" + i, tron(xChieu(i), yChieu(i), R_CHIEU + 3)});
        }
        v.addElement(new Object[]{"danh", tron(xDanh(), yDanh(), R_DANH + 3)});
        v.addElement(new Object[]{"doi_bo", tron(xDoiBo(), yDoiBo(), R_DOI_BO + 1)});
        v.addElement(new Object[]{"thuoc_hp", tron(xThuoc(), yThuoc(true), R_THUOC + 1)});
        v.addElement(new Object[]{"thuoc_pp", tron(xThuoc(), yThuoc(false), R_THUOC + 1)});
        v.addElement(new Object[]{"ngu_canh", tron(xNguCanh(), yNguCanh(), R_NGU_CANH + 1)});
        v.addElement(new Object[]{"tu_dong", new int[]{xTuDong(), yTuDong(), W_TU_DONG, H_TU_DONG}});
        v.addElement(new Object[]{"tu_dong_cai_dat", tron(xTdCai(), yTdCai(), R_TD_CAI + 1)});
        v.addElement(new Object[]{"chat", tron(xChat(), yChat(), R_CHAT + 1)});
        v.addElement(new Object[]{"dong_chat", new int[]{xChat() + R_CHAT + 6, Canvas.h - 4 - DONG_CHAT * LH_CHAT, W_CHAT, DONG_CHAT * LH_CHAT}});
        v.addElement(new Object[]{"ten_chieu", new int[]{Canvas.hw - 60, yTenChieu(), 120, 16}});
        Object[][] r = new Object[v.size()][];
        for (int i = 0; i < r.length; i++) {
            r[i] = (Object[]) v.elementAt(i);
        }
        return r;
    }

    static int[] tron(int x, int y, int r) {
        return new int[]{x - r, y - r, 2 * r, 2 * r, x, y, r};
    }

    /**
     * Kiểm chồng: cặp phần HUD giao nhau (hình tròn so theo tâm / bán kính để không báo nhầm góc vuông) và phần ra
     * ngoài màn. "" = sạch. Hộp thoại đáy: phần dưới HUD ẩn khi hộp mở (hienDuoi) nên không xét.
     */
    public static String kiemChong(GameScr gs) {
        Object[][] v = cacVung(gs);
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < v.length; i++) {
            int[] a = (int[]) v[i][1];
            if (a[0] < 0 || a[1] < 0 || a[0] + a[2] > Canvas.w || a[1] + a[3] > Canvas.h) {
                b.append("NGOAI ").append(v[i][0]).append('\n');
            }
            for (int j = i + 1; j < v.length; j++) {
                int[] c = (int[]) v[j][1];
                boolean giao;
                if (a.length > 4 && c.length > 4) {   // hai hình tròn: so tâm / bán kính
                    int dx = a[4] - c[4], dy = a[5] - c[5];
                    int rr = a[6] + c[6];
                    giao = dx * dx + dy * dy < rr * rr;
                } else {
                    giao = a[0] < c[0] + c[2] && c[0] < a[0] + a[2] && a[1] < c[1] + c[3] && c[1] < a[1] + a[3];
                }
                if (giao) {
                    b.append("CHONG ").append(v[i][0]).append(" x ").append(v[j][0]).append('\n');
                }
            }
        }
        return b.toString();
    }
}
