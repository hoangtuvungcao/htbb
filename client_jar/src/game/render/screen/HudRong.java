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
 *  - phải trên: hàng nút biểu tượng (menu Poké Ball, hành trang, Pokémon / chiêu, nhiệm vụ, nhóm, bản đồ, hệ sinh
 *    thái, cài đặt), bản đồ nhỏ ngay dưới (chạm = bản đồ cả map, chạm điểm trên đó = đi tới);
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

    static final int N_MENU = 0, N_TUI = 1, N_POKE = 2, N_NV = 3, N_NHOM = 4, N_BAN_DO = 5, N_SINH_THAI = 6, N_CAI_DAT = 7;
    static final int SO_NUT_TREN = 8;
    static final int R_TREN = 11, BUOC_TREN = 27, Y_TREN = 14;
    /** đỉnh đầu thẻ bản đồ nhỏ / đỉnh ảnh bản đồ nhỏ */
    static final int Y_DAU_MM = 29, Y_MM = 45;
    static final int MM_W = 132, MM_H = 82;

    static int xNutTren(int i) {
        return Canvas.w - 15 - (SO_NUT_TREN - 1 - i) * BUOC_TREN;
    }

    /** Thẻ trạng thái (vùng chạm). */
    static int rongThe() {
        return 158;
    }

    static int caoThe() {
        return 50;
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

    static int xDoiBo() {
        return xDanh() - 30;
    }

    static int yDoiBo() {
        return yDanh() + 30;
    }

    /** Công tắc Tự động: viên trên cụm chiêu, sát mép phải. */
    static final int W_TU_DONG = 46, H_TU_DONG = 18;

    static int xTuDong() {
        return Canvas.w - W_TU_DONG - 4;
    }

    static int yTuDong() {
        return yChieu(3) - R_CHIEU - H_TU_DONG - 6;
    }

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
        if (Canvas.currentScreen != gs) {
            return;   // màn chơi đang làm nền cho bảng khác: nút HUD phía sau không nhận chạm
        }
        if (gs.hideGUI != 2) {
            for (int i = 0; i < SO_NUT_TREN; i++) {
                game.pokemon.VungCham.themTuyetDoi(xNutTren(i) - R_TREN, Y_TREN - R_TREN, 2 * R_TREN + 1, 2 * R_TREN + 1, t, "hud-tren-" + i);
            }
            game.pokemon.VungCham.themTuyetDoi(0, 0, rongThe(), caoThe(), t, "hud-the");
        }
        if (!hienDuoi(gs)) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            game.pokemon.VungCham.themTuyetDoi(xChieu(i) - R_CHIEU, yChieu(i) - R_CHIEU, 2 * R_CHIEU + 1, 2 * R_CHIEU + 1, t, "hud-chieu" + i);
        }
        game.pokemon.VungCham.themTuyetDoi(xDanh() - R_DANH, yDanh() - R_DANH, 2 * R_DANH + 1, 2 * R_DANH + 1, t, "hud-danh");
        game.pokemon.VungCham.themTuyetDoi(xDoiBo() - R_DOI_BO, yDoiBo() - R_DOI_BO, 2 * R_DOI_BO + 1, 2 * R_DOI_BO + 1, t, "hud-doi-bo");
        game.pokemon.VungCham.themTuyetDoi(xThuoc() - R_THUOC, yThuoc(true) - R_THUOC, 2 * R_THUOC + 1, 2 * R_THUOC + 1, t, "hud-thuoc-hp");
        game.pokemon.VungCham.themTuyetDoi(xThuoc() - R_THUOC, yThuoc(false) - R_THUOC, 2 * R_THUOC + 1, 2 * R_THUOC + 1, t, "hud-thuoc-pp");
        if (HanhDong.loaiNguCanh() == 3) {
            game.pokemon.VungCham.themTuyetDoi(xNguCanh() - R_NGU_CANH, yNguCanh() - R_NGU_CANH, 2 * R_NGU_CANH + 1, 2 * R_NGU_CANH + 1, t, "hud-ngu-canh");
        }
        game.pokemon.VungCham.themTuyetDoi(xTuDong(), yTuDong(), W_TU_DONG, H_TU_DONG, t, "hud-tu-dong");
        game.pokemon.VungCham.themTuyetDoi(xChat() - R_CHAT, yChat() - R_CHAT, 2 * R_CHAT + 1, 2 * R_CHAT + 1, t, "hud-chat");
    }

    static boolean tro(int x, int y, int r) {
        // chuột / ngón đang đè (hiệu ứng nhấn)
        if (!Canvas.isPointerDown) {
            return false;
        }
        int dx = Canvas.px - x, dy = Canvas.py - y;
        return dx * dx + dy * dy <= r * r;
    }

    static void nen(Graphics g, int x, int y, int r, int tren, int duoi, boolean sang) {
        g.setColor(GiaoDien.VIEN);
        g.fillArc(x - r - 1, y - r - 1, 2 * r + 3, 2 * r + 3, 0, 360);
        g.setColor(duoi);
        g.fillArc(x - r, y - r, 2 * r + 1, 2 * r + 1, 0, 360);
        g.setColor(tren);
        g.fillArc(x - r + 1, y - r + 1, 2 * r - 1, 2 * r - 3, 20, 140);
        g.setColor(sang ? GiaoDien.VANG : 0x6F86AE);
        g.drawArc(x - r, y - r, 2 * r, 2 * r, 0, 360);
    }

    static void veNutTren(Graphics g) {
        boolean tuDong = TuDong.dang;
        for (int i = 0; i < SO_NUT_TREN; i++) {
            int x = xNutTren(i), y = Y_TREN;
            int r = R_TREN - (tro(x, y, R_TREN + 2) ? 1 : 0);
            if (i == N_MENU) {
                GiaoDien.pokeBall(g, x, y, r, 0);
                continue;
            }
            nen(g, x, y, r, 0x3D6FB0, 0x1A3462, false);
            veIcon(g, i, x, y, r);
        }
        // chấm đỏ: Menu (có mục mới), Nhiệm vụ (nhiệm vụ mới / đủ để trả)
        if (game.pokemon.ManMenu.coChamDo()) {
            chamDo(g, xNutTren(N_MENU) + R_TREN - 3, Y_TREN - R_TREN + 3);
        }
        if (game.pokemon.ManNhiemVu.coMoi()) {
            chamDo(g, xNutTren(N_NV) + R_TREN - 3, Y_TREN - R_TREN + 3);
        }
        if (tuDong) {
            // đèn nhỏ cạnh nút menu khi Tự động đang chạy
            g.setColor(GiaoDien.HP_XANH);
            g.fillArc(xNutTren(N_MENU) - R_TREN - 2, Y_TREN + R_TREN - 6, 6, 6, 0, 360);
        }
    }

    static void chamDo(Graphics g, int x, int y) {
        g.setColor(GiaoDien.TRANG);
        g.fillArc(x - 4, y - 4, 9, 9, 0, 360);
        g.setColor(GiaoDien.DO);
        g.fillArc(x - 3, y - 3, 7, 7, 0, 360);
    }

    /** Biểu tượng vẽ bằng nét (không ảnh, không chữ). */
    static void veIcon(Graphics g, int n, int x, int y, int r) {
        int u = r * 5 / 11;
        g.setColor(GiaoDien.TRANG);
        switch (n) {
            case N_TUI:   // ba lô
                g.fillRoundRect(x - u, y - u + 2, 2 * u + 1, 2 * u - 1, 4, 4);
                g.setColor(0x1A3462);
                g.drawLine(x - u + 2, y, x + u - 2, y);
                g.setColor(GiaoDien.TRANG);
                g.drawArc(x - u / 2, y - u - 1, u + 1, u, 0, 180);
                break;
            case N_POKE:   // Poké Ball nhỏ trong vòng
                GiaoDien.pokeBall(g, x, y, u, 0);
                break;
            case N_NV:   // cuộn giấy + dấu chấm than
                g.fillRect(x - u + 1, y - u, 2 * u - 1, 2 * u + 1);
                g.setColor(GiaoDien.CAM);
                g.fillRect(x - 1, y - u + 2, 2, u);
                g.fillRect(x - 1, y + u - 3, 2, 2);
                break;
            case N_NHOM:   // hai người
                g.fillArc(x - u - 1, y - u, u, u, 0, 360);
                g.fillArc(x + 1, y - u, u, u, 0, 360);
                g.fillArc(x - u - 3, y + 1, u + 4, u + 2, 0, 180);
                g.fillArc(x - 1, y + 1, u + 4, u + 2, 0, 180);
                g.fillRect(x - u - 3, y + u / 2 + 1, 2 * u + 6, u / 2 + 1);
                break;
            case N_BAN_DO:   // bản đồ gấp + ghim
                g.fillRect(x - u, y - u + 1, 2 * u + 1, 2 * u - 1);
                g.setColor(0x1A3462);
                g.drawLine(x - u / 3, y - u + 1, x - u / 3, y + u - 1);
                g.drawLine(x + u / 3, y - u + 1, x + u / 3, y + u - 1);
                g.setColor(GiaoDien.DO);
                g.fillArc(x - 2, y - 3, 5, 5, 0, 360);
                break;
            case N_SINH_THAI:   // chiếc lá
                g.setColor(GiaoDien.LA);
                g.fillArc(x - u, y - u, 2 * u + 1, 2 * u + 1, 45, 180);
                g.fillArc(x - u + 2, y - u + 2, 2 * u - 3, 2 * u - 3, 225, 180);
                g.setColor(GiaoDien.TRANG);
                g.drawLine(x - u + 1, y + u - 1, x + u - 1, y - u + 1);
                break;
            case N_CAI_DAT:   // bánh răng
                g.fillArc(x - u, y - u, 2 * u + 1, 2 * u + 1, 0, 360);
                for (int k = 0; k < 8; k++) {
                    int dx = COS8[k] * (u + 2) / 1024, dy = SIN8[k] * (u + 2) / 1024;
                    g.fillRect(x + dx - 1, y + dy - 1, 3, 3);
                }
                g.setColor(0x1A3462);
                g.fillArc(x - u / 2, y - u / 2, u + 1, u + 1, 0, 360);
                break;
            case 20:   // chat: bóng thoại
                g.fillRoundRect(x - u - 1, y - u + 1, 2 * u + 3, 2 * u - 2, 5, 5);
                g.fillTriangle(x - u + 1, y + u - 2, x - u + 1, y + u + 3, x - 1, y + u - 2);
                g.setColor(0x1A3462);
                g.fillRect(x - u + 2, y - 1, 2, 2);
                g.fillRect(x, y - 1, 2, 2);
                g.fillRect(x + u - 2, y - 1, 2, 2);
                break;
            case 21:   // nói chuyện (NPC): bóng thoại + 3 chấm
                veIcon(g, 20, x, y, r);
                break;
            case 22:   // nhặt: bàn tay
                g.fillRoundRect(x - u, y - 1, 2 * u + 1, u + 2, 4, 4);
                for (int k = 0; k < 4; k++) {
                    g.fillRect(x - u + k * (2 * u) / 4 + 1, y - u, 2, u);
                }
                break;
            case 23:   // câu cá: cần + phao
                g.setColor(0xC08040);
                g.drawLine(x - u, y + u, x + u - 1, y - u);
                g.drawLine(x - u + 1, y + u, x + u, y - u);
                g.setColor(GiaoDien.TRANG);
                g.drawLine(x + u, y - u, x + u, y + u - 2);
                g.setColor(GiaoDien.DO);
                g.fillArc(x + u - 2, y + u - 3, 5, 5, 0, 360);
                break;
            default:
                break;
        }
    }

    static final int[] COS8 = {1024, 724, 0, -724, -1024, -724, 0, 724};
    static final int[] SIN8 = {0, 724, 1024, 724, 0, -724, -1024, -724};

    static void veCum(Graphics g, GameScr gs) {
        MainChar mc = gs.mainChar;
        if (MainChar.quickSlot == null) {
            mc.loadQuickSlot();
        }
        QuickSlot[] bo = MainChar.quickSlot == null ? null : MainChar.quickSlot[GameScr.indexTabSlot];
        long now = System.currentTimeMillis();
        // 4 chiêu
        for (int i = 0; i < 4; i++) {
            QuickSlot qs = bo == null ? null : bo[gs.SLOTINDEXOFKEY[PHIM_CHIEU[i]]];
            int x = xChieu(i), y = yChieu(i);
            veO(g, qs, mc, x, y, R_CHIEU - (tro(x, y, R_CHIEU) ? 1 : 0), now);
        }
        // nút đánh
        int xd = xDanh(), yd = yDanh();
        int rd = R_DANH - (tro(xd, yd, R_DANH) ? 2 : 0);
        int nc = HanhDong.loaiNguCanh();
        if (nc == 1 || nc == 2) {
            nen(g, xd, yd, rd, 0x60D070, 0x2E8A3A, true);
            veIcon(g, nc == 1 ? 21 : 22, xd, yd, rd);
        } else {
            QuickSlot qs = bo == null ? null : bo[gs.SLOTINDEXOFKEY[5]];
            veO(g, qs, mc, xd, yd, rd, now);
            if (gs.focusedActor != null && gs.focusedActor.isMonster()) {
                g.setColor(GiaoDien.DO);
                g.drawArc(xd - rd - 2, yd - rd - 2, 2 * rd + 4, 2 * rd + 4, 0, 360);
            }
        }
        // đổi bộ ô (phím 0): 2 chấm
        int xb = xDoiBo(), yb = yDoiBo();
        nen(g, xb, yb, R_DOI_BO, 0x3D6FB0, 0x1A3462, false);
        for (int k = 0; k < 2; k++) {
            g.setColor(k == GameScr.indexTabSlot ? GiaoDien.VANG : 0x5E7AA8);
            g.fillArc(xb - 5 + k * 6, yb - 2, 4, 4, 0, 360);
        }
        // thuốc
        veThuoc(g, mc, true, now);
        veThuoc(g, mc, false, now);
        // ngữ cảnh: câu cá
        if (nc == 3) {
            int x = xNguCanh(), y = yNguCanh();
            nen(g, x, y, R_NGU_CANH, 0x50B8FF, 0x2060A0, true);
            veIcon(g, 23, x, y, R_NGU_CANH);
        }
        // tự động
        boolean td = TuDong.dang;
        boolean cho = !td && TuDong.choBat;   // đã xin máy chủ, chờ trả lời
        int xt = xTuDong(), yt = yTuDong();
        GiaoDien.khoi(g, xt, yt, W_TU_DONG, H_TU_DONG, 8, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, xt + 1, yt + 1, W_TU_DONG - 2, H_TU_DONG - 2, 7, td ? 0x60D070 : cho ? 0xF8C020 : 0x5E6A80, td ? 0x2E8A3A : cho ? 0xC97A10 : 0x2A3346);
        g.setColor(td ? GiaoDien.TRANG : 0x9AA6BA);
        g.fillArc(xt + 4, yt + 4, H_TU_DONG - 8, H_TU_DONG - 8, 0, 360);
        Font.smallFont.drawString(g, "AUTO", xt + W_TU_DONG / 2 + 5, yt + (H_TU_DONG - Font.smallFont.getHeight()) / 2 + 1, Font.CENTER);
        // tên chiêu vừa dùng
        veTenChieu(g, gs, bo, now);
    }

    /** Một ô chiêu / thuốc tròn tâm (x, y) bán kính r. */
    static void veO(Graphics g, QuickSlot qs, MainChar mc, int x, int y, int r, long now) {
        int loai = qs == null ? -1 : qs.quickslotType;
        if (loai == QuickSlot.TYPE_SKILL) {
            int lop = mc.clazz;
            int sk = qs.skillType;
            int mau = KieuO.mauHe(PokeChieu.he(lop, sk));
            nen(g, x, y, r, GiaoDien.pha(mau, GiaoDien.TRANG, 2, 5), GiaoDien.pha(mau, 0x000000, 1, 4), false);
            Image a = KieuO.anhHinh(PokeChieu.hinh(lop, sk), r >= 20 ? 2 : 1);
            if (a != null) {
                g.drawImage(a, x, y, Graphics.HCENTER | Graphics.VCENTER);
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
                // quạt hồi chiêu: phần còn lại tối, chạy theo chiều kim đồng hồ từ 12 giờ
                int goc = (int) (360 * (tong - da) / tong);
                g.setColor(0x202838);
                g.fillArc(x - r + 1, y - r + 1, 2 * r - 1, 2 * r - 1, 90, goc);
                g.setColor(GiaoDien.VANG);
                g.drawArc(x - r, y - r, 2 * r, 2 * r, 90, goc);
                Font.borderFont.drawString(g, so((int) ((tong - da + 999) / 1000)), x, y - Font.borderFont.getHeight() / 2, Font.CENTER);
            } else if (pp > 0 && mc.mp < pp) {
                g.setColor(GiaoDien.MP_XANH);
                g.drawArc(x - r - 1, y - r - 1, 2 * r + 2, 2 * r + 2, 0, 360);
            }
            if (pp > 0 && r < 24) {
                String s = so(pp);
                int w = Font.smallFont.getWidth(s) + 4;
                int hh = Font.smallFont.getHeight();
                GiaoDien.khoi(g, x - w / 2, y + r - hh / 2 - 1, w, hh, 3, mc.mp < pp ? 0x7A2030 : 0x1A3B6E);
                Font.smallFont.drawString(g, s, x, y + r - hh / 2 - 1, Font.CENTER);
            }
            return;
        }
        if (loai == QuickSlot.TYPE_POTION) {
            nen(g, x, y, r, 0x3A72B8, 0x1A3B6E, false);
            try {
                Res.paintPotion(g, MainChar.listPotion[qs.potionType].index, x, y, 3);
                KieuO.soLuong(g, x + r, y + r, KieuO.soGon(mc.potions[qs.potionType]));
            } catch (Throwable e) {
                // chưa có bảng thuốc
            }
            return;
        }
        // ô trống: dấu + (chạm = bảng gán chiêu / thuốc)
        nen(g, x, y, r, 0x2A3E6E, 0x0E1A30, false);
        g.setColor(0xA9C8FF);
        int u = r / 2;
        g.fillRect(x - u, y - 1, 2 * u + 1, 3);
        g.fillRect(x - 1, y - u, 3, 2 * u + 1);
    }

    static final int[] THUOC_HP = {1, 2, 3, 21, 22, 93, 94};
    static final int[] THUOC_PP = {4, 5, 6, 23, 24, 95, 96};

    static int loaiThuoc(MainChar mc, boolean hp) {
        int[] ds = hp ? THUOC_HP : THUOC_PP;
        if (mc.potions == null) {
            return -1;
        }
        for (int i = 0; i < ds.length; i++) {
            if (ds[i] < mc.potions.length && mc.potions[ds[i]] > 0) {
                return ds[i];
            }
        }
        return -1;
    }

    static void veThuoc(Graphics g, MainChar mc, boolean hp, long now) {
        int x = xThuoc(), y = yThuoc(hp);
        int r = R_THUOC - (tro(x, y, R_THUOC) ? 1 : 0);
        int loai = loaiThuoc(mc, hp);
        int mau = hp ? 0xE3350D : GiaoDien.MP_XANH;
        nen(g, x, y, r, loai < 0 ? 0x3A4256 : 0x3D6FB0, loai < 0 ? 0x1E2433 : 0x1A3462, false);
        g.setColor(mau);
        g.drawArc(x - r + 1, y - r + 1, 2 * r - 2, 2 * r - 2, 0, 360);
        if (loai < 0) {
            g.setColor(GiaoDien.pha(mau, 0x000000, 1, 2));
            g.fillRoundRect(x - 3, y - 4, 7, 9, 3, 3);
            return;
        }
        try {
            Res.paintPotion(g, MainChar.listPotion[loai].index, x, y, 3);
            long da = now - mc.potionLastTimeUse[loai];
            long tong = MainChar.listPotion[loai].delay;
            if (da < tong && tong > 0) {
                g.setColor(GiaoDien.VANG);
                g.drawArc(x - r, y - r, 2 * r, 2 * r, 90, (int) (360 * (tong - da) / tong));
            }
            KieuO.soLuong(g, x + r + 2, y + r, KieuO.soGon(mc.potions[loai]));
        } catch (Throwable e) {
            g.setColor(mau);
            g.fillRoundRect(x - 3, y - 4, 7, 9, 3, 3);
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
        Font f = Font.borderFont;
        int wt = f.getWidth(ten) + 14;
        int xt = Canvas.hw - wt / 2;
        int yt = Canvas.h - 36;
        int mau = KieuO.mauHe(PokeChieu.he(lop, qs.skillType));
        GiaoDien.khoi(g, xt, yt, wt, 16, 7, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, xt + 1, yt + 1, wt - 2, 14, 6, mau, GiaoDien.pha(mau, 0, 2, 5));
        f.drawString(g, ten, xt + wt / 2, yt + 1, Font.CENTER);
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
        nen(g, x, y, R_CHAT - (tro(x, y, R_CHAT) ? 1 : 0), 0x3D6FB0, 0x1A3462, false);
        veIcon(g, 20, x, y, R_CHAT);
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
            if (tuoi < 6000) {
                KieuO.phu(g, x0, yy, w, LH_CHAT - 1, 0x900E1D38);
            } else {
                KieuO.phu(g, x0, yy, w, LH_CHAT - 1, 0x500E1D38);
            }
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
            GiaoDien.vien(g, Canvas.w - 4 - wt, Y_DAU_MM, wt, 15, GiaoDien.XANH, ten);
        }
        g.setColor(GiaoDien.VIEN);
        g.fillRect(x0 - 2, y0 - 2, w + 4, h + 4);
        g.setColor(0x6F86AE);
        g.drawRect(x0 - 1, y0 - 1, w + 1, h + 1);
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
        // dưới bản đồ: vùng địa lý + toạ độ ô
        String vung = game.pokemon.VungMap.ten(Tilemap.lv);
        int yd = y0 + h + 3;
        if (vung != null) {
            Font.smallFont.drawString(g, vung, x0, yd, Font.LEFT);
        }
        Font.smallFont.drawString(g, toaDo(gs.mainChar.x >> 4, gs.mainChar.y >> 4), Canvas.w - 4, yd, Font.RIGHT);
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
        return Y_MM + GameScr.hMiniMap + 14;
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
     * 25 đổi bộ, 26 / 27 thuốc HP / PP, 28 ngữ cảnh, 29 tự động, 30 chat, 40 bản đồ to (trong ảnh), 41 bản đồ to (đóng);
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
        if (gs.hideGUI != 2) {
            for (int i = 0; i < SO_NUT_TREN; i++) {
                if (trongTron(x, y, xNutTren(i), Y_TREN, R_TREN + 2)) {
                    return i;
                }
            }
            if (trongO(x, y, 0, 0, rongThe(), caoThe())) {
                return 10;
            }
            Position p = gs.posMiniMapHud();
            if (p != null && coMiniMap() && trongO(x, y, p.x - 2, Y_DAU_MM, GameScr.wMiniMap + 4, p.y + GameScr.hMiniMap + 2 - Y_DAU_MM)) {
                return 11;
            }
        }
        if (!hienDuoi(gs)) {
            return -1;
        }
        for (int i = 0; i < 4; i++) {
            if (trongTron(x, y, xChieu(i), yChieu(i), R_CHIEU + 2)) {
                return 20 + i;
            }
        }
        if (trongTron(x, y, xDanh(), yDanh(), R_DANH + 3)) {
            return 24;
        }
        if (trongTron(x, y, xDoiBo(), yDoiBo(), R_DOI_BO + 2)) {
            return 25;
        }
        if (trongTron(x, y, xThuoc(), yThuoc(true), R_THUOC + 2)) {
            return 26;
        }
        if (trongTron(x, y, xThuoc(), yThuoc(false), R_THUOC + 2)) {
            return 27;
        }
        if (HanhDong.loaiNguCanh() == 3 && trongTron(x, y, xNguCanh(), yNguCanh(), R_NGU_CANH + 2)) {
            return 28;
        }
        if (trongO(x, y, xTuDong() - 2, yTuDong() - 2, W_TU_DONG + 4, H_TU_DONG + 4)) {
            return 29;
        }
        if (trongTron(x, y, xChat(), yChat(), R_CHAT + 3)) {
            return 30;
        }
        return -1;
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
            case N_POKE:
                game.pokemon.ManNhanVat.mo(game.pokemon.ManNhanVat.TAB_KY_NANG);
                break;
            case N_NV:
                game.pokemon.ManNhiemVu.mo();
                break;
            case N_NHOM:
                game.pokemon.ManDoi.mo(0);
                break;
            case N_BAN_DO:
                HanhDong.moBanDoLon();
                break;
            case N_SINH_THAI:
                HanhDong.moSinhThai();
                break;
            case N_CAI_DAT:
                moCaiDat();
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
                Canvas.keyPressed[0] = true;
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
                TuDong.batTat();
                break;
            case 30:
                GameScr.typechat = GameScr.CHAT_NORMAL;
                gs.chatMode = true;
                gs.tfChat.setIputType(0);
                gs.tfChat.isFocus = true;
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
        Position p = gs == null ? null : gs.posMiniMapHud();
        int wm = GameScr.wMiniMap > 0 ? GameScr.wMiniMap : MM_W, hm = GameScr.hMiniMap > 0 ? GameScr.hMiniMap : MM_H;
        int xm = p != null ? p.x : Canvas.w - wm - 4;
        v.addElement(new Object[]{"ban_do_nho", new int[]{xm - 2, Y_DAU_MM, wm + 4, Y_MM + hm + 14 - Y_DAU_MM}});
        for (int i = 0; i < 4; i++) {
            v.addElement(new Object[]{"chieu" + i, tron(xChieu(i), yChieu(i), R_CHIEU + 1)});
        }
        v.addElement(new Object[]{"danh", tron(xDanh(), yDanh(), R_DANH + 1)});
        v.addElement(new Object[]{"doi_bo", tron(xDoiBo(), yDoiBo(), R_DOI_BO + 1)});
        v.addElement(new Object[]{"thuoc_hp", tron(xThuoc(), yThuoc(true), R_THUOC + 1)});
        v.addElement(new Object[]{"thuoc_pp", tron(xThuoc(), yThuoc(false), R_THUOC + 1)});
        v.addElement(new Object[]{"ngu_canh", tron(xNguCanh(), yNguCanh(), R_NGU_CANH + 1)});
        v.addElement(new Object[]{"tu_dong", new int[]{xTuDong(), yTuDong(), W_TU_DONG, H_TU_DONG}});
        v.addElement(new Object[]{"chat", tron(xChat(), yChat(), R_CHAT + 1)});
        v.addElement(new Object[]{"dong_chat", new int[]{xChat() + R_CHAT + 6, Canvas.h - 4 - DONG_CHAT * LH_CHAT, W_CHAT, DONG_CHAT * LH_CHAT}});
        v.addElement(new Object[]{"ten_chieu", new int[]{Canvas.hw - 60, Canvas.h - 36, 120, 16}});
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
