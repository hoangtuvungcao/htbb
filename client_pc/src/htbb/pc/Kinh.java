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
 * Bộ vẽ giao diện HD của lớp phủ bản PC (Cài đặt, hộp nhập, thanh lệnh - toạ độ điểm ảnh thật). 0.9.7 (kiểu NRO): bảng,
 * nút, phím, thanh trượt, công tắc vẽ bằng CÙNG tấm mảnh pixel của jar (/nro/khung.png, game.pokemon.KhungNroBang)
 * phóng hệ số nguyên láng giềng gần nhất ({@link #ve9}) - cùng giao diện gỗ / giấy da với bảng trong game. Bảng màu theo thương hiệu (GiaoDien của bản jar / web HTBB): đỏ Poké #E3350D, vàng #FFCB05,
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
    /** 0.9.6 (kiểu Pokémon GO, cùng ChuDe): chữ tối trên mặt trắng, màu chính teal */
    public static final Color CHU = new Color(game.pokemon.ChuDe.CHU);
    public static final Color CHU_MO = new Color(game.pokemon.ChuDe.CHU_PHU);
    public static final Color CHINH = new Color(game.pokemon.ChuDe.CHINH);
    public static final Color CHINH_SANG = new Color(game.pokemon.ChuDe.CHINH_SANG);
    public static final Color CHINH_TOI = new Color(game.pokemon.ChuDe.CHINH_TOI);
    public static final Color KE = new Color(game.pokemon.ChuDe.KE);

    private Kinh() {
    }

    // ------------------------------------------------------------------ 0.9.7: mảnh pixel NRO (9-slice) ở toạ độ thật
    private static java.awt.image.BufferedImage tam;
    private static boolean daNap;

    static java.awt.image.BufferedImage tam() {
        if (!daNap) {
            daNap = true;
            try {
                java.io.InputStream in = Kinh.class.getResourceAsStream(game.pokemon.KhungNroBang.TAM_KHUNG);
                if (in != null) tam = javax.imageio.ImageIO.read(in);
            } catch (Throwable e) {
                tam = null;
            }
        }
        return tam;
    }

    /** Hệ số pixel (px thật / px ảnh) cho tỉ lệ lớp phủ u (1280x720 = 1): 1 px logic jar = 2u px thật. */
    public static int heSo(float u) {
        return Math.max(1, Math.round(2 * u));
    }

    private static void vung(Graphics2D g, java.awt.image.BufferedImage a, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
        if (sw <= 0 || sh <= 0 || dw <= 0 || dh <= 0) return;
        g.drawImage(a, dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy + sh, null);
    }

    private static void lat(Graphics2D g, java.awt.image.BufferedImage a, int sx, int sy, int sw, int sh, int x, int y, int w, int h, int k) {
        if (sw <= 0 || sh <= 0) return;
        for (int yy = 0; yy < h; yy += sh * k) {
            int hh = Math.min(sh * k, h - yy);
            for (int xx = 0; xx < w; xx += sw * k) {
                int ww = Math.min(sw * k, w - xx);
                vung(g, a, sx, sy, (ww + k - 1) / k, (hh + k - 1) / k, x + xx, y + yy, ww, hh);
            }
        }
    }

    /** Mảnh manh (KhungNroBang) phủ (x, y, w, h) px thật, phóng k (láng giềng gần nhất). false = thiếu ảnh. */
    public static boolean ve9(Graphics2D g, int manh, float fx, float fy, float fw, float fh, int k) {
        java.awt.image.BufferedImage a = tam();
        if (a == null) return false;
        Object cu = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        short[] m = game.pokemon.KhungNroBang.MANH;
        int i = manh * 9;
        int sx = m[i], sy = m[i + 1], sw = m[i + 2], sh = m[i + 3];
        int x = Math.round(fx), y = Math.round(fy), w = Math.round(fw), h = Math.round(fh);
        if (m[i + 8] == 1) {
            vung(g, a, sx, sy, sw, sh, x, y, sw * k, sh * k);
        } else {
            int l = m[i + 4], t = m[i + 5], r = m[i + 6], b = m[i + 7];
            int L = Math.min(l * k, w / 2), T = Math.min(t * k, h / 2), R = Math.min(r * k, w - L), B = Math.min(b * k, h - T);
            int cw = sw - l - r, ch = sh - t - b;
            vung(g, a, sx, sy, L / k, T / k, x, y, L, T);
            vung(g, a, sx + sw - R / k, sy, R / k, T / k, x + w - R, y, R, T);
            vung(g, a, sx, sy + sh - B / k, L / k, B / k, x, y + h - B, L, B);
            vung(g, a, sx + sw - R / k, sy + sh - B / k, R / k, B / k, x + w - R, y + h - B, R, B);
            lat(g, a, sx + l, sy, cw, t, x + L, y, w - L - R, T, k);
            lat(g, a, sx + l, sy + sh - b, cw, b, x + L, y + h - B, w - L - R, B, k);
            lat(g, a, sx, sy + t, l, ch, x, y + T, L, h - T - B, k);
            lat(g, a, sx + sw - r, sy + t, r, ch, x + w - R, y + T, R, h - T - B, k);
            int giua = game.pokemon.KhungNroBang.GIUA[manh];
            if (giua >= 0) {
                g.setColor(new Color(giua));
                g.fillRect(x + L, y + T, w - L - R, h - T - B);
            } else {
                lat(g, a, sx + l, sy + t, cw, ch, x + L, y + T, w - L - R, h - T - B, k);
            }
        }
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, cu == null ? RenderingHints.VALUE_INTERPOLATION_BILINEAR : cu);
        return true;
    }

    /** Bóng cứng (nâu đen) dưới-phải khối, dày k. */
    public static void bongCung(Graphics2D g, float x, float y, float w, float h, int k) {
        g.setColor(new Color(game.pokemon.ChuDe.BONG));
        g.fillRect(Math.round(x) + 2 * k, Math.round(y + h), Math.round(w) - 2 * k, 2 * k);
        g.fillRect(Math.round(x + w), Math.round(y) + 2 * k, 2 * k, Math.round(h));
    }

    /** Hệ số mặc định theo cỡ khung game hiện tại. */
    static int k() {
        return Math.max(1, NenPcHeSo());
    }

    private static int NenPcHeSo() {
        try {
            return javax.microedition.lcdui.NenPc.heSo;
        } catch (Throwable e) {
            return 2;
        }
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

    /** Tấm (0.9.6 kiểu GO): mặt sáng dải teal rất nhạt -> trắng, bo lớn, bóng mềm, không viền tối. */
    public static void bang(Graphics2D g, float x, float y, float w, float h, float r, int alpha) {
        // 0.9.7: tấm khung gỗ viền vàng góc Poké Ball + giấy da (mảnh TAM), bóng cứng
        int k = k();
        bongCung(g, x, y, w, h, k);
        if (ve9(g, game.pokemon.KhungNroBang.TAM, x, y, w, h, k)) return;
        g.setColor(new Color(game.pokemon.ChuDe.NEN));
        g.fill(new RoundRectangle2D.Float(x, y, w, h, 6, 6));
    }

    /** Nút viên: chinh = teal (chính), chon = teal đang chọn, thường = trắng viền nhạt chữ tối; sang = chuột trỏ. */
    public static void nut(Graphics2D g, float x, float y, float w, float h, String chu, Font f, boolean chinh, boolean sang, boolean chon) {
        // 0.9.7: nút khối pixel - chính / đang chọn = cam, thường = giấy da; chuột trỏ = lõm
        int k = k();
        int m = chinh || chon ? game.pokemon.KhungNroBang.NUT_CAM : game.pokemon.KhungNroBang.NUT_GIAY;
        if (!sang) bongCung(g, x, y, w, h, k);
        if (!ve9(g, sang ? m + 1 : m, x, y, w, h, k)) {
            g.setColor(chinh || chon ? CHINH : new Color(game.pokemon.ChuDe.NEN));
            g.fill(new RoundRectangle2D.Float(x, y, w, h, 6, 6));
        }
        if (chu != null) {
            g.setFont(f);
            chuGiua(g, chu, x + w / 2, y + h / 2 + (sang ? k : 0), chinh || chon ? Color.WHITE : CHU, chinh || chon);
        }
    }

    /** Phím (keycap) nhỏ: nền sáng, chữ tối. Trả bề rộng. */
    public static float phim(Graphics2D g, float x, float y, float h, String chu, Font f) {
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        float w = Math.max(h, fm.stringWidth(chu) + h * 0.6f);
        // 0.9.7: phím = nút khối giấy da nhỏ
        if (!ve9(g, game.pokemon.KhungNroBang.NUT_GIAY, x, y, w, h, k())) {
            g.setColor(new Color(game.pokemon.ChuDe.NEN));
            g.fill(new RoundRectangle2D.Float(x, y, w, h, 4, 4));
        }
        g.setColor(CHU);
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
        if (mau.getRed() + mau.getGreen() + mau.getBlue() > 600) {
            g.setColor(new Color(0, 0, 0, 100));   // chữ sáng: bóng mảnh
            g.drawString(s, x, y + 1.1f);
        }
        g.setColor(mau);
        g.drawString(s, x, y);
    }

    /** Thanh trượt 0..100 (rãnh xám xanh, phần đầy teal, núm trắng viền teal). */
    public static void thanhTruot(Graphics2D g, float x, float y, float w, float h, int gt, boolean sang) {
        // 0.9.7: rãnh tối lõm + dải vàng + núm đồng (pixel)
        int k = k();
        float hr = Math.max(h, 7 * k);
        float yr = y + h / 2 - hr / 2;
        float d = w * gt / 100f;
        if (ve9(g, game.pokemon.KhungNroBang.RANH, x, yr, w, hr, k)) {
            if (d > 0) ve9(g, game.pokemon.KhungNroBang.DAY_EXP, x + k, yr + k, Math.max(2 * k, d - k), hr - 2 * k, k);
            int nw = 10 * k, nh = 14 * k;
            ve9(g, game.pokemon.KhungNroBang.NUM_TRUOT, x + d - nw / 2f, y + h / 2 - nh / 2f, nw, nh, k);
            return;
        }
        g.setColor(CHINH);
        g.fillRect(Math.round(x), Math.round(y), Math.round(d), Math.round(h));
    }

    /** Công tắc bật/tắt (bật = teal, núm trắng). */
    public static void congTac(Graphics2D g, float x, float y, float h, boolean bat, boolean sang) {
        // 0.9.7: công tắc pixel (tắt: lỗ tối núm xám; bật: xanh lá dấu tích núm vàng)
        int k = Math.max(1, Math.round(h / 14f));
        float ww = 26 * k;
        if (ve9(g, bat ? game.pokemon.KhungNroBang.CONG_TAC_BAT : game.pokemon.KhungNroBang.CONG_TAC_TAT, x + (h * 1.9f - ww) / 2, y + (h - 14 * k) / 2, ww, 14 * k, k)) {
            return;
        }
        g.setColor(bat ? new Color(game.pokemon.ChuDe.LA) : new Color(0x2E1E14));
        g.fillRect(Math.round(x), Math.round(y), Math.round(h * 1.9f), Math.round(h));
    }

    // ------------------------------------------------------------------ biểu tượng vẽ bằng hình (phông đi kèm
    // Noto Sans / Baloo 2 KHÔNG có ✕ ⚙ ▶ ♪ ✓ ← → ... -> mọi ký hiệu giao diện vẽ bằng nét, không dùng ký tự)
    public static final int BT_MAN_HINH = 0, BT_LOA = 1, BT_BAN_PHIM = 2, BT_SET = 3, BT_THONG_TIN = 4, BT_DONG = 5,
            BT_CHUOT = 6, BT_TAI_KHOAN = 7;

    // ------------------------------------------------------------------ 0.9.7: icon = ảnh PNG (/nro/icon.png)
    private static java.awt.image.BufferedImage tamIcon;
    private static boolean daNapIcon;

    /** Vẽ icon pixel id (game.pokemon.BieuTuong) tâm (cx, cy), cạnh ~s px thật (phóng nguyên, láng giềng gần nhất). */
    public static boolean icon(Graphics2D g, int id, float cx, float cy, float s) {
        if (!daNapIcon) {
            daNapIcon = true;
            try {
                java.io.InputStream in = Kinh.class.getResourceAsStream(game.pokemon.KhungNroBang.TAM_ICON);
                if (in != null) tamIcon = javax.imageio.ImageIO.read(in);
            } catch (Throwable e) {
                tamIcon = null;
            }
        }
        if (tamIcon == null || id < 0 || id >= game.pokemon.KhungNroBang.SO_ICON) return false;
        int k = Math.max(1, Math.round(s / 16f));
        int c = 16 * k;
        int sx = (id % game.pokemon.KhungNroBang.COT_ICON) * 16, sy = (id / game.pokemon.KhungNroBang.COT_ICON) * 16;
        Object cu = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int x = Math.round(cx - c / 2f), y = Math.round(cy - c / 2f);
        g.drawImage(tamIcon, x, y, x + c, y + c, sx, sy, sx + 16, sy + 16, null);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, cu == null ? RenderingHints.VALUE_INTERPOLATION_BILINEAR : cu);
        return true;
    }

    private static final int[] BT_ICON = {game.pokemon.BieuTuong.MAN_HINH, game.pokemon.BieuTuong.LOA, game.pokemon.BieuTuong.BAN_PHIM,
        game.pokemon.BieuTuong.PHIM, game.pokemon.BieuTuong.TIN, game.pokemon.BieuTuong.DONG, game.pokemon.BieuTuong.CHUOT_TRAI,
        game.pokemon.BieuTuong.TAI_KHOAN};

    /** Biểu tượng loai tâm (cx, cy), cạnh s (0.9.7: ảnh PNG; vẽ nét chỉ khi thiếu ảnh), màu c. */
    public static void bieuTuong(Graphics2D g, int loai, float cx, float cy, float s, Color c) {
        if (loai >= 0 && loai < BT_ICON.length && icon(g, BT_ICON[loai], cx, cy, s)) return;
        game.pokemon.VeTay.ghi("Kinh.bieuTuong vector");
        java.awt.Stroke cu = g.getStroke();
        float n = Math.max(1.2f, s * 0.11f);
        g.setColor(c);
        g.setStroke(new BasicStroke(n, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (loai) {
            case BT_MAN_HINH: {
                g.draw(new RoundRectangle2D.Float(cx - s * 0.48f, cy - s * 0.4f, s * 0.96f, s * 0.6f, s * 0.14f, s * 0.14f));
                g.draw(new java.awt.geom.Line2D.Float(cx, cy + s * 0.22f, cx, cy + s * 0.36f));
                g.draw(new java.awt.geom.Line2D.Float(cx - s * 0.22f, cy + s * 0.38f, cx + s * 0.22f, cy + s * 0.38f));
                break;
            }
            case BT_LOA: {
                java.awt.geom.Path2D.Float p = new java.awt.geom.Path2D.Float();
                p.moveTo(cx - s * 0.46f, cy - s * 0.14f);
                p.lineTo(cx - s * 0.24f, cy - s * 0.14f);
                p.lineTo(cx - s * 0.02f, cy - s * 0.36f);
                p.lineTo(cx - s * 0.02f, cy + s * 0.36f);
                p.lineTo(cx - s * 0.24f, cy + s * 0.14f);
                p.lineTo(cx - s * 0.46f, cy + s * 0.14f);
                p.closePath();
                g.fill(p);
                g.draw(p);
                g.draw(new java.awt.geom.Arc2D.Float(cx - s * 0.2f, cy - s * 0.2f, s * 0.4f, s * 0.4f, -50, 100, java.awt.geom.Arc2D.OPEN));
                g.draw(new java.awt.geom.Arc2D.Float(cx - s * 0.38f, cy - s * 0.38f, s * 0.76f, s * 0.76f, -50, 100, java.awt.geom.Arc2D.OPEN));
                break;
            }
            case BT_BAN_PHIM: {
                g.draw(new RoundRectangle2D.Float(cx - s * 0.5f, cy - s * 0.32f, s, s * 0.64f, s * 0.16f, s * 0.16f));
                float k = s * 0.1f;
                for (int hang = 0; hang < 2; hang++) {
                    for (int i = 0; i < 4; i++) {
                        float x = cx - s * 0.33f + i * s * 0.22f - k / 2;
                        float y = cy - s * 0.17f + hang * s * 0.17f - k / 2;
                        g.fill(new java.awt.geom.Rectangle2D.Float(x, y, k, k));
                    }
                }
                g.draw(new java.awt.geom.Line2D.Float(cx - s * 0.2f, cy + s * 0.17f, cx + s * 0.2f, cy + s * 0.17f));
                break;
            }
            case BT_SET: {
                java.awt.geom.Path2D.Float p = new java.awt.geom.Path2D.Float();
                p.moveTo(cx + s * 0.1f, cy - s * 0.46f);
                p.lineTo(cx - s * 0.26f, cy + s * 0.06f);
                p.lineTo(cx - s * 0.02f, cy + s * 0.06f);
                p.lineTo(cx - s * 0.1f, cy + s * 0.46f);
                p.lineTo(cx + s * 0.26f, cy - s * 0.06f);
                p.lineTo(cx + s * 0.02f, cy - s * 0.06f);
                p.closePath();
                g.setStroke(new BasicStroke(n * 0.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.fill(p);
                g.draw(p);
                break;
            }
            case BT_TAI_KHOAN: {
                // người: đầu tròn + vai cung
                g.draw(new java.awt.geom.Ellipse2D.Float(cx - s * 0.17f, cy - s * 0.42f, s * 0.34f, s * 0.34f));
                g.draw(new java.awt.geom.Arc2D.Float(cx - s * 0.36f, cy - s * 0.02f, s * 0.72f, s * 0.8f, 0, 180, java.awt.geom.Arc2D.OPEN));
                break;
            }
            case BT_THONG_TIN: {
                g.draw(new java.awt.geom.Ellipse2D.Float(cx - s * 0.44f, cy - s * 0.44f, s * 0.88f, s * 0.88f));
                float d = n * 1.25f;
                g.fill(new java.awt.geom.Ellipse2D.Float(cx - d / 2, cy - s * 0.24f - d / 2, d, d));
                g.draw(new java.awt.geom.Line2D.Float(cx, cy - s * 0.04f, cx, cy + s * 0.24f));
                break;
            }
            case BT_DONG: {
                float r = s * 0.3f;
                g.draw(new java.awt.geom.Line2D.Float(cx - r, cy - r, cx + r, cy + r));
                g.draw(new java.awt.geom.Line2D.Float(cx - r, cy + r, cx + r, cy - r));
                break;
            }
            default:
                break;
        }
        g.setStroke(cu);
    }

    /**
     * Chuột nhỏ (trang Phím tắt): nut 0 = nút trái sáng, 1 = nút phải sáng, 2 = con lăn sáng. Tâm (cx, cy), cao s.
     */
    public static void chuot(Graphics2D g, float cx, float cy, float s, int nut) {
        if (icon(g, nut == 0 ? game.pokemon.BieuTuong.CHUOT_TRAI : nut == 1 ? game.pokemon.BieuTuong.CHUOT_PHAI : game.pokemon.BieuTuong.CHUOT_LAN, cx, cy, s)) return;
        game.pokemon.VeTay.ghi("Kinh.chuot vector");
        float w = s * 0.66f, h = s;
        float x = cx - w / 2, y = cy - h / 2;
        RoundRectangle2D.Float than = new RoundRectangle2D.Float(x, y, w, h, w * 0.9f, w * 0.9f);
        g.setPaint(new GradientPaint(0, y, new Color(0xFDFEFF), 0, y + h, new Color(0xC9D6EA)));
        g.fill(than);
        java.awt.Shape cu = g.getClip();
        g.clip(than);
        g.setColor(VANG);
        if (nut == 0) g.fill(new java.awt.geom.Rectangle2D.Float(x, y, w / 2, h * 0.42f));
        else if (nut == 1) g.fill(new java.awt.geom.Rectangle2D.Float(cx, y, w / 2, h * 0.42f));
        g.setClip(cu);
        g.setColor(VIEN);
        g.setStroke(new BasicStroke(Math.max(1f, s * 0.07f)));
        g.draw(than);
        g.draw(new java.awt.geom.Line2D.Float(x, y + h * 0.42f, x + w, y + h * 0.42f));
        g.draw(new java.awt.geom.Line2D.Float(cx, y, cx, y + h * 0.42f));
        float lw = w * 0.2f;
        RoundRectangle2D.Float lan = new RoundRectangle2D.Float(cx - lw / 2, y + h * 0.1f, lw, h * 0.24f, lw, lw);
        g.setColor(nut == 2 ? DO : VIEN);
        g.fill(lan);
    }

    /** Poké Ball tâm (cx, cy) bán kính r (tiêu đề bảng Cài đặt). */
    public static void pokeBall(Graphics2D g, float cx, float cy, float r) {
        if (icon(g, game.pokemon.BieuTuong.POKEBALL, cx, cy, 2 * r)) return;
        game.pokemon.VeTay.ghi("Kinh.pokeBall vector");
        java.awt.geom.Ellipse2D.Float tron = new java.awt.geom.Ellipse2D.Float(cx - r, cy - r, 2 * r, 2 * r);
        g.setColor(Color.WHITE);
        g.fill(tron);
        g.setPaint(new GradientPaint(0, cy - r, DO_SANG, 0, cy, DO));
        g.fill(new java.awt.geom.Arc2D.Float(cx - r, cy - r, 2 * r, 2 * r, 0, 180, java.awt.geom.Arc2D.CHORD));
        g.setColor(VIEN);
        g.setStroke(new BasicStroke(r * 0.16f));
        g.draw(new java.awt.geom.Line2D.Float(cx - r, cy, cx + r, cy));
        g.draw(tron);
        float rb = r * 0.34f;
        g.fill(new java.awt.geom.Ellipse2D.Float(cx - rb, cy - rb, 2 * rb, 2 * rb));
        rb = r * 0.2f;
        g.setColor(Color.WHITE);
        g.fill(new java.awt.geom.Ellipse2D.Float(cx - rb, cy - rb, 2 * rb, 2 * rb));
        g.setColor(new Color(255, 255, 255, 150));
        float rh = r * 0.22f;
        g.fill(new java.awt.geom.Ellipse2D.Float(cx - r * 0.55f - rh / 2, cy - r * 0.6f - rh / 2, rh, rh));
    }
}
