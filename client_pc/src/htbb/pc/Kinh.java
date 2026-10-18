package htbb.pc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Bộ vẽ giao diện HD "kính" của bản PC (Java2D khử răng cưa, toạ độ điểm ảnh thật): bảng kính mờ, nút, phím,
 * thanh trượt. Bảng màu theo thương hiệu (GiaoDien của bản jar / web HTBB): đỏ Poké #E3350D, vàng #FFCB05,
 * xanh #3D7DCA, navy #003A70, viền tối #1E2A44.
 */
public final class Kinh {
    public static final Color DO = new Color(0xE3350D);
    public static final Color DO_SANG = new Color(0xF4613A);
    public static final Color VANG = new Color(0xFFCB05);
    public static final Color XANH = new Color(0x3D7DCA);
    public static final Color XANH_SANG = new Color(0x6FA6EA);
    public static final Color NAVY = new Color(0x003A70);
    public static final Color VIEN = new Color(0x1E2A44);
    public static final Color CHU = new Color(0xF5F8FC);
    public static final Color CHU_MO = new Color(0xB4C3DA);

    private Kinh() {
    }

    public static void min(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    public static Color mo(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    /** Bảng kính: thân xanh đêm trong mờ dải dọc, viền sáng mảnh, ánh sáng mép trên, bóng đổ mềm. */
    public static void bang(Graphics2D g, float x, float y, float w, float h, float r, int alpha) {
        // bóng đổ mềm (3 lớp)
        for (int i = 3; i >= 1; i--) {
            g.setColor(new Color(0, 0, 0, 28 * alpha / 255));
            g.fill(new RoundRectangle2D.Float(x - i + 1, y + i * 2 - 1, w + 2 * i - 2, h + 2 * i - 2, r + i * 2, r + i * 2));
        }
        RoundRectangle2D.Float rr = new RoundRectangle2D.Float(x, y, w, h, r, r);
        g.setPaint(new GradientPaint(0, y, new Color(26, 48, 92, 222 * alpha / 255), 0, y + h, new Color(10, 20, 44, 236 * alpha / 255)));
        g.fill(rr);
        // ánh kính mép trên
        java.awt.Shape cu = g.getClip();
        g.clip(rr);
        g.setPaint(new GradientPaint(0, y, new Color(255, 255, 255, 46 * alpha / 255), 0, y + Math.min(h, 40), new Color(255, 255, 255, 0)));
        g.fillRect((int) x, (int) y, (int) w + 1, (int) Math.min(h, 40) + 1);
        g.setClip(cu);
        g.setStroke(new BasicStroke(1.2f));
        g.setColor(new Color(160, 200, 255, 110 * alpha / 255));
        g.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, r, r));
        g.setColor(new Color(0, 0, 0, 120 * alpha / 255));
        g.draw(new RoundRectangle2D.Float(x - 0.5f, y - 0.5f, w + 1, h + 1, r + 1, r + 1));
    }

    /** Nút: chinh = nút chính (đỏ Poké), sang = chuột đang trỏ, chon = đang bật. */
    public static void nut(Graphics2D g, float x, float y, float w, float h, String chu, Font f, boolean chinh, boolean sang, boolean chon) {
        float r = Math.min(h, 14);
        Color tren, duoi;
        if (chinh) {
            tren = sang ? new Color(0xFF7A50) : DO_SANG;
            duoi = sang ? new Color(0xC8300E) : new Color(0xA5240A);
        } else if (chon) {
            tren = sang ? new Color(0x8CC0FF) : XANH_SANG;
            duoi = sang ? new Color(0x3A78C8) : new Color(0x2B5EA8);
        } else {
            tren = sang ? new Color(255, 255, 255, 70) : new Color(255, 255, 255, 34);
            duoi = sang ? new Color(255, 255, 255, 40) : new Color(255, 255, 255, 14);
        }
        RoundRectangle2D.Float rr = new RoundRectangle2D.Float(x, y, w, h, r, r);
        g.setPaint(new GradientPaint(0, y, tren, 0, y + h, duoi));
        g.fill(rr);
        g.setStroke(new BasicStroke(1f));
        g.setColor(chon || chinh ? new Color(255, 255, 255, 150) : new Color(255, 255, 255, sang ? 120 : 60));
        g.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, r, r));
        if (chu != null) {
            g.setFont(f);
            chuGiua(g, chu, x + w / 2, y + h / 2, CHU, chinh || chon);
        }
    }

    /** Phím (keycap) nhỏ: nền sáng, chữ tối. Trả bề rộng. */
    public static float phim(Graphics2D g, float x, float y, float h, String chu, Font f) {
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        float w = Math.max(h, fm.stringWidth(chu) + h * 0.6f);
        RoundRectangle2D.Float rr = new RoundRectangle2D.Float(x, y, w, h, h * 0.45f, h * 0.45f);
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(new RoundRectangle2D.Float(x, y + 1.5f, w, h, h * 0.45f, h * 0.45f));
        g.setPaint(new GradientPaint(0, y, new Color(0xFDFEFF), 0, y + h, new Color(0xC9D6EA)));
        g.fill(rr);
        g.setColor(new Color(0x1E2A44));
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, h * 0.45f, h * 0.45f));
        float by = y + (h - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent();
        g.drawString(chu, x + (w - fm.stringWidth(chu)) / 2f, by);
        return w;
    }

    /** Chữ căn giữa tại (cx, cy), bóng mảnh cho dễ đọc trên nền sáng/tối. */
    public static void chuGiua(Graphics2D g, String s, float cx, float cy, Color mau, boolean bong) {
        FontMetrics fm = g.getFontMetrics();
        float x = cx - fm.stringWidth(s) / 2f;
        float y = cy - (fm.getAscent() + fm.getDescent()) / 2f + fm.getAscent();
        if (bong) {
            g.setColor(new Color(0, 0, 0, 110));
            g.drawString(s, x, y + 1.2f);
        }
        g.setColor(mau);
        g.drawString(s, x, y);
    }

    /** Chữ trái, đường chân y. */
    public static void chu(Graphics2D g, String s, float x, float y, Color mau) {
        g.setColor(new Color(0, 0, 0, 100));
        g.drawString(s, x, y + 1.1f);
        g.setColor(mau);
        g.drawString(s, x, y);
    }

    /** Thanh trượt 0..100. */
    public static void thanhTruot(Graphics2D g, float x, float y, float w, float h, int gt, boolean sang) {
        float r = h;
        g.setColor(new Color(0, 0, 0, 120));
        g.fill(new RoundRectangle2D.Float(x, y, w, h, r, r));
        float d = w * gt / 100f;
        g.setPaint(new GradientPaint(x, 0, XANH, x + w, 0, VANG));
        g.fill(new RoundRectangle2D.Float(x, y, Math.max(h, d), h, r, r));
        float nr = h * 1.9f;
        g.setColor(sang ? Color.WHITE : new Color(0xEAF1FB));
        g.fill(new java.awt.geom.Ellipse2D.Float(x + d - nr / 2, y + h / 2 - nr / 2, nr, nr));
        g.setColor(VIEN);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new java.awt.geom.Ellipse2D.Float(x + d - nr / 2, y + h / 2 - nr / 2, nr, nr));
    }

    /** Công tắc bật/tắt. */
    public static void congTac(Graphics2D g, float x, float y, float h, boolean bat, boolean sang) {
        float w = h * 1.9f;
        g.setColor(bat ? new Color(0x48D058) : new Color(255, 255, 255, sang ? 70 : 45));
        g.fill(new RoundRectangle2D.Float(x, y, w, h, h, h));
        g.setColor(new Color(255, 255, 255, 120));
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, h, h));
        float nr = h - 4;
        g.setColor(Color.WHITE);
        g.fill(new java.awt.geom.Ellipse2D.Float(bat ? x + w - nr - 2 : x + 2, y + 2, nr, nr));
    }
}
