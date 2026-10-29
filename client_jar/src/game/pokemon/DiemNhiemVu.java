package game.pokemon;

import game.model.Quest;
import game.model.Tilemap;
import game.render.Canvas;
import game.render.screen.GameScr;
import game.render.screen.HudRong;
import javax.microedition.lcdui.Graphics;

/**
 * ĐIỂM NHIỆM VỤ ★ (0.9.23, báo lỗi người chơi 29/10/2026: "làm sao để tới cái ô 53 19") - bước "Đứng tại điểm đánh
 * dấu..." / "Đến ..." chỉ ghi toạ độ ô, người chơi không thấy toạ độ của mình nên không tìm được. Máy chủ gửi điểm đích
 * (đuôi thứ hai gói -64, GameScr.docDiemNhiemVu -> Quest.dich*); lớp này:
 * <ul>
 *   <li>dấu sao vàng nhấp nháy + vòng vùng hoàn thành ngay trên ô đích (lớp thế giới, SinhThai.veTheGioi);</li>
 *   <li>chấm ★ trên bản đồ nhỏ (SinhThai.veMini) và bản đồ to (HudRong.veBanDoTo);</li>
 *   <li>mũi tên vàng quanh nhân vật chỉ hướng khi ★ ngoài màn - cả J2ME không cảm ứng cũng thấy;</li>
 *   <li>{@link #diToi}: tìm đường tới ô đi được gần tâm nhất trong vùng (nút "Đi tới dấu sao" ở sổ nhiệm vụ / thẻ mục
 *       tiêu, phím 5 / OK ở sổ nhiệm vụ cho máy phím).</li>
 * </ul>
 * Không gói tin mới: chỉ đọc đuôi -64. Kiểm thử: client_pc/test/KiemDiemNhiemVu.
 */
public final class DiemNhiemVu {

    /** Kiểm thử: {x, y} (toạ độ màn) dấu sao / mũi tên vừa vẽ, null = không vẽ. */
    public static int[] saoVuaVe, muiTenVuaVe;

    private DiemNhiemVu() {
    }

    /** Nhiệm vụ đang làm có điểm đích (ưu tiên chính, rồi phụ); null = không có. */
    public static Quest nhiemVu() {
        Quest q = GameScr.mainQuest;
        if (coDich(q)) {
            return q;
        }
        q = GameScr.subQuest;
        return coDich(q) ? q : null;
    }

    public static boolean coDich(Quest q) {
        return q != null && q.dichMap >= 0 && q.stateQuest == 2;
    }

    /** Điểm đích nằm trên bản đồ đang đứng (và không phải "vào bản đồ là xong"). */
    public static boolean oDay(Quest q) {
        return coDich(q) && !q.dichCaMap && q.dichMap == GameScr.mapNap;
    }

    /** Số ô (Chebyshev) từ nhân vật tới mép vùng đích; -1 = không ở bản đồ đích. */
    public static int conO(Quest q) {
        GameScr gs = Canvas.gameScr;
        if (!oDay(q) || gs == null || gs.mainChar == null) {
            return -1;
        }
        int ox = gs.mainChar.x >> 4, oy = gs.mainChar.y >> 4;
        int dx = Math.max(0, Math.abs(ox - q.dichX) - q.dichRx);
        int dy = Math.max(0, Math.abs(oy - q.dichY) - q.dichRy);
        return Math.max(dx, dy);
    }

    /** Chữ ngắn cho thẻ / nút: "Còn 12 ô tới ★" / "Đã tới ★" / "Tới Pallet". */
    public static String chuNgan(Quest q) {
        if (!coDich(q)) {
            return "";
        }
        if (!oDay(q)) {
            return q.dichCaMap && q.dichMap == GameScr.mapNap ? "Đã tới nơi" : "Tới " + q.dichNoi;
        }
        int n = conO(q);
        return n <= 0 ? "Đã tới điểm đánh dấu" : "Còn " + n + " ô tới dấu sao";
    }

    /** Tìm đường tới ô đi được trong vùng đích gần tâm nhất. false = không ở bản đồ đích / không có ô đi được. */
    public static boolean diToi(Quest q) {
        GameScr gs = Canvas.gameScr;
        if (!oDay(q) || gs == null) {
            return false;
        }
        int rMax = Math.max(q.dichRx, q.dichRy);
        for (int r = 0; r <= rMax; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    if (Math.max(Math.abs(dx), Math.abs(dy)) != r || Math.abs(dx) > q.dichRx || Math.abs(dy) > q.dichRy) {
                        continue;
                    }
                    int tx = q.dichX + dx, ty = q.dichY + dy;
                    if (tx < 0 || ty < 0 || tx >= Tilemap.w || ty >= Tilemap.h || Tilemap.tileTypeAtPixel(tx * 16 + 8, ty * 16 + 8, 2)) {
                        continue;
                    }
                    if (Canvas.currentScreen != gs) {
                        gs.switchToMe();
                    }
                    HudRong.diToi(gs, tx, ty);
                    return true;
                }
            }
        }
        return false;
    }

    /** Màu ★ nhấp nháy (vàng / vàng nhạt). */
    static int mau() {
        return Canvas.gameTick % 10 < 6 ? GiaoDien.VANG : GiaoDien.VANG_NHAT;
    }

    /** Ngôi sao 5 cánh đặc (hai tam giác + tâm) tâm (cx, cy), bán kính r. */
    static void sao(Graphics g, int cx, int cy, int r, int mau) {
        int r2 = r * 2 / 5;
        g.setColor(mau);
        g.fillTriangle(cx, cy - r, cx - r * 59 / 100, cy + r * 81 / 100, cx + r * 95 / 100, cy - r * 31 / 100);
        g.fillTriangle(cx, cy - r, cx + r * 59 / 100, cy + r * 81 / 100, cx - r * 95 / 100, cy - r * 31 / 100);
        g.fillTriangle(cx - r * 95 / 100, cy - r * 31 / 100, cx + r * 95 / 100, cy - r * 31 / 100, cx, cy + r2);
    }

    /** Bản đồ nhỏ (gốc = ô, 1 px / ô): ★ nhỏ có viền. */
    public static void veMini(Graphics g) {
        Quest q = nhiemVu();
        if (!oDay(q)) {
            return;
        }
        sao(g, q.dichX, q.dichY, 5, GiaoDien.VIEN);
        sao(g, q.dichX, q.dichY, 4, mau());
    }

    /** Bản đồ to (ảnh ở x0, y0, k px / ô). */
    public static void veTo(Graphics g, int x0, int y0, int k) {
        Quest q = nhiemVu();
        if (!oDay(q)) {
            return;
        }
        int cx = x0 + q.dichX * k + k / 2, cy = y0 + q.dichY * k + k / 2;
        g.setColor(GiaoDien.VANG);
        g.drawRect(x0 + (q.dichX - q.dichRx) * k, y0 + (q.dichY - q.dichRy) * k, (2 * q.dichRx + 1) * k - 1, (2 * q.dichRy + 1) * k - 1);
        sao(g, cx, cy, 8, GiaoDien.VIEN);
        sao(g, cx, cy, 6, mau());
    }

    /**
     * Lớp thế giới (đã translate -cmx, -cmy): vòng vùng hoàn thành + ★ nhảy trên ô đích; ngoài màn thì mũi tên vàng
     * quanh nhân vật (toạ độ màn).
     */
    public static void veTheGioi(Graphics g) {
        saoVuaVe = null;
        muiTenVuaVe = null;
        Quest q = nhiemVu();
        GameScr gs = Canvas.gameScr;
        if (!oDay(q) || gs == null || gs.mainChar == null) {
            return;
        }
        int tx = q.dichX * 16 + 8, ty = q.dichY * 16 + 8;
        int sx = tx - GameScr.cmx, sy = ty - GameScr.cmy;
        if (sx >= -40 && sx < Canvas.w + 40 && sy >= -40 && sy < Canvas.h + 40) {
            // vòng vùng (elip dẹt trên đất) + ★ nhảy lên xuống
            int rw = q.dichRx * 16 + 8, rh = q.dichRy * 8 + 4;
            g.setColor(GiaoDien.VIEN);
            g.drawArc(tx - rw - 1, ty - rh - 1, 2 * rw + 2, 2 * rh + 2, 0, 360);
            g.setColor(mau());
            g.drawArc(tx - rw, ty - rh, 2 * rw, 2 * rh, 0, 360);
            int nhay = (Canvas.gameTick >> 1) % 8;
            nhay = nhay < 4 ? nhay : 8 - nhay;
            int cy = ty - 22 - nhay;
            sao(g, tx, cy, 10, GiaoDien.VIEN);
            sao(g, tx, cy, 8, mau());
            g.setColor(GiaoDien.VIEN);
            g.fillRect(tx - 1, cy + 6, 3, 14 + nhay);
            saoVuaVe = new int[]{sx, sy};
        }
        if (sx >= 8 && sx < Canvas.w - 8 && sy >= 30 && sy < Canvas.h - 62) {
            return;
        }
        // mũi tên quanh nhân vật (bán kính ~44 px, phía dấu sao) - như mũi tên chỉ NPC của TheNhiemVu: luôn gần giữa
        // màn nên không bị cụm nút HUD ở mép màn che (mũi tên mép màn kiểu SinhThai nằm dưới nút chiêu / AUTO)
        int cx = gs.mainChar.x - GameScr.cmx, cyc = gs.mainChar.y - GameScr.cmy - 14;
        int dx = tx - gs.mainChar.x, dy = ty - gs.mainChar.y;
        int d0 = Math.max(1, (int) Math.sqrt((double) dx * dx + (double) dy * dy));
        int ban = 44;
        int px = cx + dx * ban / d0, py = cyc + dy * ban / d0;
        g.translate(GameScr.cmx, GameScr.cmy);   // về toạ độ màn
        GiaoDien.khoi(g, px - 9, py - 9, 18, 18, 9, GiaoDien.VIEN);
        GiaoDien.khoi(g, px - 8, py - 8, 16, 16, 8, mau());
        int d = Math.max(1, (int) Math.sqrt((double) dx * dx + (double) dy * dy));
        int ux = dx * 6 / d, uy = dy * 6 / d, nx = -dy * 4 / d, ny = dx * 4 / d;
        g.setColor(GiaoDien.VIEN);
        g.fillTriangle(px + ux, py + uy, px - ux / 2 + nx, py - uy / 2 + ny, px - ux / 2 - nx, py - uy / 2 - ny);
        g.translate(-GameScr.cmx, -GameScr.cmy);
        muiTenVuaVe = new int[]{px, py};
    }
}
