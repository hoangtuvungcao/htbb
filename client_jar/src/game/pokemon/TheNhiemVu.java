package game.pokemon;

import game.model.Actor;
import game.model.IAction;
import game.model.ItemQuest;
import game.model.Quest;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * THẺ MỤC TIÊU (0.9.8, nhánh huong-dan): thẻ nhỏ góc trái (dưới thẻ trạng thái HUD rộng) cho Pokémon cấp thấp - nhiệm
 * vụ chính hiện tại, việc cần làm ngay (nhận ở / tiến độ x/y / trả ở NPC nào) và nút "Đi tới &lt;NPC&gt;" (tìm đường rồi
 * nói chuyện, HanhDong.noiVoi). NPC ở bản đồ này mà nằm ngoài màn thì có mũi tên quanh nhân vật chỉ về NPC.
 *
 * Không có gói tin mới: mọi thứ lấy từ danh sách nhiệm vụ -64 (GameScr.mainQuest / allQuestCanReceive) - chữ (tên, mô
 * tả, tên quái / vật phẩm, tên NPC) là chữ máy chủ gửi (CSDL). Client 0.9.7 vẫn có dấu nhiệm vụ trên đầu NPC, chấm bản
 * đồ nhỏ và nút "Đi tới" trong sổ nhiệm vụ; thẻ này chỉ là lối tắt. Chạm thân thẻ = mở sổ nhiệm vụ (ManNhiemVu).
 * Kiểm thử: client_pc/test/KiemTheNhiemVu (11 cỡ màn).
 */
public final class TheNhiemVu {

    /** Hiện thẻ tới cấp này (cùng ngưỡng "tân thủ" của máy chủ HuongDanTanThu.CAP_XEM_LAI). */
    public static final int CAP_HIEN = 15;
    /** Kiểm thử: khung thẻ / nút / mũi tên vừa vẽ ({x, y, w, h}; mũi tên {cx, cy, r, dx, dy}), null = không vẽ. */
    public static int[] vuaVe, nutVuaVe, muiTenVuaVe;
    /** Kiểm thử: NPC ngoài màn nhưng mũi tên trùng chỗ thẻ (nhân vật sát góc trái trên) nên không vẽ. */
    public static boolean muiTenBiThe;
    /** Kiểm thử: dòng chữ vừa vẽ (tiêu đề, việc cần làm, nút). */
    public static String[] chuVuaVe;

    private TheNhiemVu() {
    }

    /** Nhiệm vụ chính đang theo: đủ để trả / đang làm / có thể nhận (null = không có). */
    static Quest nhiemVu() {
        Quest q = GameScr.mainQuest;
        if (q != null && q.stateQuest != 3) {
            return q;
        }
        Vector v = GameScr.allQuestCanReceive;
        for (int i = 0; v != null && i < v.size(); i++) {
            Quest c = (Quest) v.elementAt(i);
            if (c.main_sub == 0 && c.stateQuest != 3) {
                return c;
            }
        }
        return null;
    }

    static boolean dangLam(Quest q) {
        return q == GameScr.mainQuest;
    }

    /** NPC cần gặp lúc này: đang làm (đủ / chưa đủ) = NPC trả, chưa nhận = NPC giao. */
    static int npcCanGap(Quest q) {
        return dangLam(q) ? q.idNpcResponse : q.idNpcReceive;
    }

    /** Dòng việc cần làm ngay (chữ máy chủ: tên NPC, tên quái / vật phẩm, mô tả ngắn). */
    static String viecCanLam(Quest q) {
        String ten = ManNhiemVu.tenNpc(npcCanGap(q));
        ten = ten == null ? "NPC" : ten;
        if (!dangLam(q)) {
            return "Nhận ở " + ten;
        }
        if (ManNhiemVu.xong(q)) {
            return "Xong! Trả ở " + ten;
        }
        try {
            if (q.type == Quest.TYPE_KILL_MONS && q.idMonsKill != null && q.idMonsKill.length > 0) {
                return Res.monsterTemplates[q.idMonsKill[0]].name + " " + Math.min(q.allMonsKilled[0], q.totalMonskill[0]) + "/" + q.totalMonskill[0];
            }
            if (q.type == Quest.TYPE_GET_ITEM && q.idItemGet != null && q.idItemGet.length > 0) {
                return ItemQuest.NAME_ITEM[q.idItemGet[0]] + " " + Math.min(q.allItemGet[0], q.totalItemGet[0]) + "/" + q.totalItemGet[0];
            }
        } catch (Throwable e) {
            // thiếu dữ liệu quái / vật phẩm: dùng mô tả
        }
        return q.decript != null && q.decript.length() > 0 ? q.decript : "Trả ở " + ten;
    }

    /** Thẻ có hiện không (HUD rộng, Pokémon cấp thấp, không hộp thoại / menu / chat / hội thoại NPC). */
    public static boolean hien(GameScr gs) {
        return gs != null && gs.mainChar != null && game.render.screen.HudRong.bat() && Canvas.currentScreen == gs
                && !game.render.screen.HudRong.dangMoBanDoTo() && !HopXacNhan.dangMo() && gs.hideGUI != 2 && Canvas.currentDialog == null && !Canvas.menu.showMenu && !gs.chatMode
                && !gs.dangTroChuyen() && gs.mainChar.level > 0 && gs.mainChar.level <= CAP_HIEN && nhiemVu() != null;
    }

    /** GameScr.paint (sau HudRong.ve): thẻ + mũi tên chỉ hướng NPC. */
    public static void ve(Graphics g, GameScr gs) {
        vuaVe = null;
        nutVuaVe = null;
        muiTenVuaVe = null;
        muiTenBiThe = false;
        chuVuaVe = null;
        if (!hien(gs)) {
            return;
        }
        try {
            Canvas.resetTrans(g);
            final Quest q = nhiemVu();
            final Actor a = ManNhiemVu.npc(npcCanGap(q));
            veThe(g, q, a);
            if (a != null) {
                veMuiTen(g, a);
            }
        } catch (Throwable e) {
            // không làm hỏng khung hình game
        }
    }

    static void veThe(Graphics g, final Quest q, final Actor a) {
        int k = Bang.cach();
        int x = Bang.trai();
        // dưới thẻ trạng thái và nhãn độ tuổi "12+" (GameScr.paint12plus ghi đáy nhãn; PC HD: thẻ trạng thái lớn hơn)
        int y = Math.max(Math.max(Bang.tren(), 0) + game.render.screen.HudRong.caoTheTrangThai(), GameScr.day12plus + 1) + k;
        int w = Math.min(game.render.screen.HudRong.rongTheTrangThai(), Canvas.w / 3);
        int hTd = ChuDe.cao(ChuDe.NHAN);
        int hViec = ChuDe.cao(ChuDe.PHU);
        int hNut = Bang.co(24);
        int s = Math.max(14, hTd + 2);
        int hNoi = Math.max(s, hTd) + 2 + hViec;
        int hThan = Bang.co(hNoi + 8);   // thân thẻ (vùng chạm mở sổ nhiệm vụ) >= cỡ chạm tối thiểu
        int h = hThan + k + hNut + 6;
        Bang.the(g, x, y, w, h);
        int y0 = y + (hThan - hNoi) / 2;
        ManNhiemVu.veIcon(g, q, x + 6, y0, s);
        String tieuDe = ChuDe.cat(q.name, ChuDe.NHAN, w - s - 18);
        ChuDe.chu(g, tieuDe, x + s + 12, y0 + (s - hTd) / 2, ChuDe.NHAN, ChuDe.CHU, 0);
        String viec = ChuDe.cat(viecCanLam(q), ChuDe.PHU, w - 12);
        int yViec = y0 + Math.max(s, hTd) + 2;
        ChuDe.chu(g, viec, x + 6, yViec, ChuDe.PHU, ManNhiemVu.xong(q) && dangLam(q) ? ChuDe.CHINH_TOI : ChuDe.CHU_PHU, 0);
        // chạm thân thẻ (trên nút) = mở sổ nhiệm vụ
        VungCham.them(g, x, y, w, hThan, new IAction() {
            public void perform() {
                ManNhiemVu.mo();
            }
        }, "nv-the");
        String tn = ManNhiemVu.tenNpc(npcCanGap(q));
        tn = tn == null ? "NPC" : tn;
        String nhan = a != null ? "Đi tới " + tn : tn + " ở bản đồ khác";
        int yNut = y + h - 6 - hNut;
        Bang.nut(g, x + 6, yNut, w - 12, hNut, nhan, a != null ? Bang.XAC_NHAN : Bang.TAT, a == null ? null : new IAction() {
            public void perform() {
                try {
                    GameScr gs = Canvas.gameScr;
                    if (gs != null && a != null) {
                        HanhDong.noiVoi(gs, a);
                    }
                } catch (Throwable e) {
                    // không tìm được đường
                }
            }
        }, "nv-di-toi");
        vuaVe = new int[]{x, y, w, h};
        nutVuaVe = new int[]{x + 6, yNut, w - 12, hNut};
        chuVuaVe = new String[]{tieuDe, viec, nhan};
    }

    /**
     * Mũi tên chỉ hướng quanh nhân vật (bán kính ~40 px, phía NPC) khi NPC ở bản đồ này nhưng ngoài vùng nhìn - không
     * bao giờ đè thẻ / cụm nút HUD ở mép màn (nhân vật luôn gần giữa màn).
     */
    static void veMuiTen(Graphics g, Actor a) {
        GameScr gs = Canvas.gameScr;
        int sx = a.x - GameScr.cmx;
        int sy = a.y - GameScr.cmy;
        if (sx >= 0 && sx < Canvas.w && sy >= 0 && sy < Canvas.h) {
            return;   // NPC trong màn: dấu nhiệm vụ trên đầu NPC đã đủ
        }
        int cx = gs.mainChar.x - GameScr.cmx;
        int cy = gs.mainChar.y - GameScr.cmy - 14;
        int dx = a.x - gs.mainChar.x, dy = a.y - gs.mainChar.y;
        int d = Math.max(1, (int) Math.sqrt((double) dx * dx + (double) dy * dy));
        int ban = 40;
        int px = cx + dx * ban / d, py = cy + dy * ban / d;
        int r = 8;
        int[] t = vuaVe;
        if (t != null && px + r > t[0] && px - r < t[0] + t[2] && py + r > t[1] && py - r < t[1] + t[3]) {
            muiTenBiThe = true;   // nhân vật sát góc trái trên: thẻ đã ghi NPC + nút Đi tới, không vẽ đè lên thẻ
            return;
        }
        if (game.render.screen.HudRong.bat()) {
            // 0.9.8: nhân vật sát mép (camera chạm biên bản đồ) - mũi tên không đè nút HUD; thẻ đã có nút Đi tới
            Object[][] v = game.render.screen.HudRong.cacVung(gs);
            for (int i = 0; v != null && i < v.length; i++) {
                int[] o = (int[]) v[i][1];
                if (px + r > o[0] && px - r < o[0] + o[2] && py + r > o[1] && py - r < o[1] + o[3]) {
                    muiTenBiThe = true;
                    return;
                }
            }
        }
        GiaoDien.khoi(g, px - r - 1, py - r - 1, 2 * r + 2, 2 * r + 2, r + 1, GiaoDien.VIEN);
        GiaoDien.khoi(g, px - r, py - r, 2 * r, 2 * r, r, ChuDe.CHINH);
        // tam giác trắng hướng (dx, dy): đỉnh ở trước, đáy vuông góc
        int tx = dx * 6 / d, ty = dy * 6 / d, nx = -dy * 4 / d, ny = dx * 4 / d;
        g.setColor(0xFFFFFF);
        g.fillTriangle(px + tx, py + ty, px - tx / 2 + nx, py - ty / 2 + ny, px - tx / 2 - nx, py - ty / 2 - ny);
        muiTenVuaVe = new int[]{px, py, r, dx, dy};
    }
}
