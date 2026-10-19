package game.render.screen;

import game.networklogic.GameService;
import game.pokemon.GiaoDien;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * Màn chuyển bản đồ (chờ server gửi bản đồ mới). Kiểu Pokémon cùng màn tải chính (Canvas: nền banner + Poké Ball
 * quay): nền banner (thiếu ảnh -> trời + đồi vẽ bằng code), thẻ trắng mờ giữa dưới có Poké Ball lắc / quay,
 * dòng "Đang tải bản đồ" + dấu chấm chạy, thanh tiến trình (ước lượng theo thời gian, chạy chậm dần tới ~95%;
 * xong thì server chuyển màn). Chỉ lệnh Graphics MIDP 2 (fillRect / fillArc), số nguyên.
 * 30 giây chưa có bản đồ: gửi lại yêu cầu đổi bản đồ (như cũ).
 */
public class ChangScr extends Screen {
    private static ChangScr me;
    int lastTime;
    private int map = -1;
    private int xMap;
    private int yMap;
    /** mốc bắt đầu chờ (ms) - cho thanh tiến trình */
    private long batDau;

    public static ChangScr gI() {
        if (me != null) {
            return me;
        }
        ChangScr changScr = new ChangScr();
        me = changScr;
        return changScr;
    }

    public void setMap(int map, int xMap, int yMap) {
        this.map = map;
        this.xMap = xMap;
        this.yMap = yMap;
    }

    @Override
    public void update() {
        if (this.map > -1 && (System.currentTimeMillis() / 1000) - ((long) this.lastTime) > 30) {
            this.lastTime = (int) (System.currentTimeMillis() / 1000);
            GameService.gI().changeMap(this.map, this.xMap, this.yMap);
        }
    }

    /** Tiến trình ước lượng 0..1000: nhanh lúc đầu, chậm dần, không quá 950 (phần nghìn). */
    private int tienTrinh() {
        long t = System.currentTimeMillis() - this.batDau;
        if (t < 0) {
            t = 0;
        }
        // 950 * t / (t + 1200): ~0.5 s -> 28%, 1.2 s -> 47%, 3 s -> 68%, 10 s -> 85%
        return (int) (950L * t / (t + 1200L));
    }

    @Override
    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        int w = Canvas.w;
        int h = Canvas.h;
        g.setClip(0, 0, w, h);
        boolean coBanner = false;
        try {
            coBanner = game.pokemon.Banner.ve(g, -1, false);
        } catch (Throwable e) {
            // thiếu ảnh banner
        }
        Canvas.resetTrans(g);
        if (!coBanner) {
            GiaoDien.nenTroi(g, w, h);
        }
        // thẻ chờ: Poké Ball trái, chữ + thanh phải
        int cw = w - 24;
        if (cw > 220) {
            cw = 220;
        }
        int ch = 46;
        int cx = (w - cw) / 2;
        int cy = h * 2 / 3 - ch / 2;
        if (cy + ch > h - 8) {
            cy = h - 8 - ch;
        }
        GiaoDien.theMo(g, cx, cy, cw, ch);
        int r = 12;
        int bx = cx + 10 + r;
        int by = cy + ch / 2;
        GiaoDien.dangTai(g, bx, by, r);
        int tx = bx + r + 10;
        int tw = cx + cw - 12 - tx;
        String chu = "Đang tải bản đồ";
        int cham = (int) ((System.currentTimeMillis() / 400) % 4);
        for (int i = 0; i < cham; i++) {
            chu = chu + ".";
        }
        try {
            Font f = Font.arialFont != null ? Font.arialFont : Font.normalFont[0];
            f.drawString(g, chu, tx, cy + 8, 0);
        } catch (Throwable e) {
            // font chưa nạp
        }
        // thanh tiến trình kiểu Pokémon: viền tối, nền xám, phần chạy vàng -> cam + ánh trắng
        int hy = cy + ch - 15;
        int hh = 7;
        GiaoDien.khoi(g, tx, hy, tw, hh, 3, GiaoDien.VIEN);
        GiaoDien.khoi(g, tx + 1, hy + 1, tw - 2, hh - 2, 2, 0x3A4258);
        int dai = (tw - 2) * tienTrinh() / 1000;
        if (dai < 3) {
            dai = 3;
        }
        GiaoDien.khoiDoc(g, tx + 1, hy + 1, dai, hh - 2, 2, GiaoDien.VANG_NHAT, GiaoDien.CAM);
        g.setColor(0xFFF7B0);
        if (dai > 4) {
            g.fillRect(tx + 3, hy + 2, dai - 4, 1);
        }
        // vệt sáng chạy trên phần đã đầy
        int vt = (Canvas.gameTick * 3) % (dai + 12) - 6;
        if (vt > 0 && vt < dai - 2) {
            g.setColor(0xFFFFFF);
            g.fillRect(tx + 1 + vt, hy + 1, 2, hh - 2);
        }
    }

    @Override
    public void switchToMe() {
        this.lastTime = (int) (System.currentTimeMillis() / 1000);
        this.batDau = System.currentTimeMillis();
        super.switchToMe();
    }
}
