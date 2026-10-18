package htbb.pc;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.NenPc;

/**
 * Thu phóng THẾ GIỚI riêng với giao diện (bản PC). Giao diện (HUD, menu, hộp thoại) vẫn ở độ phân giải logic
 * NenPc (vd 640x360 x2); lớp thế giới (ô bản đồ, nhân vật, hiệu ứng, thời tiết, đêm) phóng m điểm ảnh thật / px
 * thế giới, m NGUYÊN (pixel art sắc nét), chọn để thấy ~20 x 11 ô (ô = 16 px):
 *   1280x720 -> m 4 (320x180 px thế giới), 1366x768 -> 4, 1920x1080 -> 6, 2560x1440 -> 8, 2560x1080 -> 6 (21:9).
 * Con lăn chuột / phím + - trong màn chơi đổi m (giới hạn [k, 3k]), lưu trong cài đặt (zoom_bu).
 *
 * Camera của game dùng sẵn bề rộng "gsw/gsh" (vùng nhìn thế giới) tách khỏi Canvas.w/h: port-j2me.py đặt
 * gsw = Zoom.gsw(), bọc phần vẽ thế giới của GameScr.paint bằng vaoTheGioi/raTheGioi, đổi toạ độ chạm
 * (Canvas.px -> thế giới). Bản đồ nhỏ hơn vùng nhìn: game tự căn giữa; lề ngoài bản đồ vẽ nền "bản đồ mờ"
 * (chính bản đồ thu nhỏ + làm mờ + tối dần) thay cho màu đen.
 */
public final class Zoom {
    /** chênh lệch người chơi chỉnh so với mức tự động (lưu cai_dat) */
    public static volatile int bu;

    private Zoom() {
    }

    /** Phóng thế giới tự động theo cửa sổ (điểm ảnh thật / px thế giới). */
    static int mTuDong() {
        int H = NenPc.thatH, W = NenPc.thatW;
        int m = Math.round(Math.min(H / 180f, W / 300f));
        return Math.max(NenPc.heSo, m);
    }

    /** Phóng thế giới hiện tại (nguyên). */
    public static int m() {
        int k = NenPc.heSo;
        int m = mTuDong() + bu;
        return Math.max(k, Math.min(k * 3, m));
    }

    /** z = m / k (tỉ lệ thế giới so với giao diện). */
    public static float z() {
        return m() / (float) NenPc.heSo;
    }

    /** Bề rộng vùng nhìn thế giới (px thế giới) - GameScr.loadCamera. */
    public static int gsw(int canvasW) {
        return (int) Math.ceil(canvasW / z());
    }

    public static int gsh(int canvasH) {
        return (int) Math.ceil(canvasH / z());
    }

    /** Toạ độ chạm (giao diện) -> thế giới. */
    public static int theGioiX(int px) {
        return (int) (px / z());
    }

    public static int theGioiY(int py) {
        return (int) (py / z());
    }

    // ------------------------------------------------------------ vẽ
    private static volatile boolean trongTheGioi;
    /** khung game cuối có bản đồ nhỏ hơn vùng nhìn (lề cần nền) */
    static volatile boolean coLe;

    /** GameScr.paint: bắt đầu vẽ thế giới (trước translate camera). */
    public static void vaoTheGioi(Graphics g) {
        float z = z();
        g.vaoTheGioi(z, Math.max(1, game.render.screen.GameScr.gsw), Math.max(1, game.render.screen.GameScr.gsh));
        trongTheGioi = true;
        coLe = game.render.screen.GameScr.lockcmx || game.render.screen.GameScr.lockcmy;
    }

    /** GameScr.paint: hết phần thế giới (trước HUD). */
    public static void raTheGioi(Graphics g) {
        if (!trongTheGioi) return;
        trongTheGioi = false;
        ManHinh mh = ManHinh.me;
        if (coLe && mh != null && g.heSo() > 1) {
            try {
                phuLe(g.g2dThat(), mh.demSau());   // lề quanh bản đồ nhỏ, TRƯỚC HUD
            } catch (Throwable e) {
                NhatKy.loi("nen le", e);
            }
        }
        g.boTheGioi();
    }

    /** Cắt Graphics (toạ độ giao diện) theo khung bản đồ trên màn khi bản đồ nhỏ hơn vùng nhìn. */
    public static void catVungTheGioi(Graphics g) {
        if (!coLe || game.render.Canvas.currentScreen != game.render.Canvas.gameScr) return;
        float z = z();
        int x0 = (int) Math.floor(-game.render.screen.GameScr.cmx * z);
        int y0 = (int) Math.floor(-game.render.screen.GameScr.cmy * z);
        int w = (int) Math.ceil(game.model.Tilemap.pxw * z);
        int h = (int) Math.ceil(game.model.Tilemap.pxh * z);
        g.clipRect(x0, y0, w, h);
    }

    /** Nền phía sau bản đồ (thay fillRect đen): toạ độ thế giới đã dịch camera. */
    public static void nen(Graphics g, int x, int y, int w, int h) {
        g.setColor(0x0A1426);
        g.fillRect(x, y, w, h);
    }

    private static BufferedImage nho;

    /**
     * Sau khi game vẽ khung (luồng sự kiện): lề ngoài bản đồ nhỏ = bản đồ thu nhỏ làm mờ phóng ra + phủ tối dần
     * (nền "theo chủ đề" của chính bản đồ, không bao giờ là màu đen phẳng).
     */
    public static void phuLe(Graphics2D g2, Image dem) {
        if (!coLe || dem == null || game.render.Canvas.currentScreen != game.render.Canvas.gameScr) return;
        int m = m();
        int lx = NenPc.lechX, ly = NenPc.lechY;
        // khung bản đồ trên màn thật
        int mx0 = lx + (-game.render.screen.GameScr.cmx) * m;
        int my0 = ly + (-game.render.screen.GameScr.cmy) * m;
        int mw = game.model.Tilemap.pxw * m;
        int mh = game.model.Tilemap.pxh * m;
        int W = NenPc.thatW, H = NenPc.thatH;
        int x0 = Math.max(0, mx0), y0 = Math.max(0, my0);
        int x1 = Math.min(W, mx0 + mw), y1 = Math.min(H, my0 + mh);
        if (x1 - x0 < 8 || y1 - y0 < 8) return;
        if (x0 <= lx && y0 <= ly && x1 >= W - lx && y1 >= H - ly) return; // bản đồ phủ kín
        if (nho == null) nho = new BufferedImage(48, 27, BufferedImage.TYPE_INT_RGB);
        Graphics2D gn = nho.createGraphics();
        gn.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gn.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gn.drawImage(dem, 0, 0, 48, 27, x0, y0, x1, y1, null);
        gn.dispose();
        java.awt.Shape cu = g2.getClip();
        java.awt.geom.Area le = new java.awt.geom.Area(new java.awt.Rectangle(0, 0, W, H));
        le.subtract(new java.awt.geom.Area(new java.awt.Rectangle(x0, y0, x1 - x0, y1 - y0)));
        g2.setClip(le);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(nho, -W / 8, -H / 8, W + W / 4, H + H / 4, null);
        g2.setPaint(new GradientPaint(0, 0, new Color(8, 14, 32, 150), 0, H, new Color(8, 14, 32, 205)));
        g2.fillRect(0, 0, W, H);
        // viền mềm quanh bản đồ (bóng đổ)
        for (int i = 1; i <= 6; i++) {
            g2.setColor(new Color(0, 0, 0, 60 - i * 9));
            g2.drawRect(x0 - i, y0 - i, x1 - x0 + 2 * i - 1, y1 - y0 + 2 * i - 1);
        }
        g2.setClip(cu);
    }

    /** Đổi phóng thế giới (+1 / -1). */
    public static void doi(int d) {
        int truoc = m();
        bu += d;
        int sau = m();
        bu = sau - mTuDong();
        if (sau != truoc) {
            CaiDat.zoomBu = bu;
            CaiDat.luu();
            NenPc.baoDoiCo();
            LopPhu.bao("Thu phóng bản đồ: ×" + sau + " (" + (NenPc.thatW / sau / 16) + "×" + (NenPc.thatH / sau / 16) + " ô)");
        }
    }
}
