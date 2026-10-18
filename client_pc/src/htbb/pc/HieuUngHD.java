package htbb.pc;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.NenPc;

/**
 * Hiệu ứng HD của bản PC ("Hiệu ứng: cao"), vẽ ở độ phân giải thật, theo THỜI GIAN THẬT (luồng trình bày 60 Hz):
 * - va chạm theo hệ (HieuUngDanh.noHe): quầng sáng cộng màu hệ + vòng sóng lan + tia lửa;
 * - lên cấp / tiến hoá (lenCap / tienHoa): cột sáng, vòng đôi, tia xoay, sao lấp lánh;
 * - Pokémon hoang gục (quaiChet): làn khói sáng tan;
 * - chuyển màn mượt (mờ dần khi đổi màn), viền tối nhẹ (vignette) trong màn chơi;
 * - thời tiết HD: sương mù trôi (dải mờ liền), nắng gắt (quầng mặt trời + tia nắng dải màu) - thay bản pixel.
 * Toạ độ thế giới -> màn: (x + dịch camera của khung game cuối) * k + lề. port-j2me.py nối các hook.
 */
public final class HieuUngHD {
    private HieuUngHD() {
    }

    static final int VA_CHAM = 0, BUNG = 1, BUNG_TIEN_HOA = 2, TAN = 3;

    static final class HU {
        int loai;
        float x, y;      // toạ độ thế giới
        int mau;
        int manh;
        long t0;
        int ms;
        float[] goc;     // hướng tia
    }

    private static final ArrayList<HU> DS = new ArrayList<HU>();
    /** dịch camera (logic) của lớp hiệu ứng thế giới trong khung game vừa vẽ */
    private static volatile int camX, camY;
    private static volatile long khungCam = -10;
    private static volatile boolean coTheGioi;
    private static volatile int camXChot, camYChot;
    private static Object manCu;
    private static volatile long tChuyenMan;
    private static BufferedImage vignette;

    private static boolean bat() {
        return CaiDat.hieuUngCao;
    }

    // ------------------------------------------------------------ hook từ code game (luồng game / sự kiện)
    /** HieuUngDanh.ve(g): ghi dịch camera thế giới. */
    public static void camera(Graphics g) {
        camX = g.getTranslateX();
        camY = g.getTranslateY();
        khungCam = NenPc.soKhung;
    }

    /** Cuối mỗi khung game (NenPc): chốt camera + phát hiện đổi màn. */
    public static void chotKhung() {
        GiaoDienHD.chotKhung();
        KiemChong.chotKhung();
        coTheGioi = khungCam == NenPc.soKhung;
        camXChot = camX;
        camYChot = camY;
        Object man = game.render.Canvas.currentScreen;
        if (man != manCu) {
            if (manCu != null) tChuyenMan = System.currentTimeMillis();
            manCu = man;
            if (!(man instanceof game.render.screen.GameScr)) {
                synchronized (DS) {
                    DS.clear();
                }
            }
        }
    }

    private static void them(int loai, int x, int y, int mau, int manh, int ms) {
        if (!bat()) return;
        HU h = new HU();
        h.loai = loai;
        h.x = x;
        h.y = y;
        h.mau = mau;
        h.manh = manh;
        h.t0 = System.currentTimeMillis();
        h.ms = ms;
        int n = loai == VA_CHAM ? 7 + manh * 3 : 14;
        h.goc = new float[n];
        for (int i = 0; i < n; i++) h.goc[i] = (float) (Math.PI * 2 * i / n + Math.random() * 0.5);
        synchronized (DS) {
            if (DS.size() > 80) DS.remove(0);
            DS.add(h);
        }
    }

    public static void vaCham(int he, int x, int y, int manh, int mau) {
        them(VA_CHAM, x, y, mau, Math.max(0, Math.min(2, manh)), manh > 0 ? 520 : 420);
    }

    public static void bung(int x, int y, boolean tienHoa) {
        them(tienHoa ? BUNG_TIEN_HOA : BUNG, x, y, tienHoa ? 0xB8F0FF : 0xFFE36A, 2, tienHoa ? 2200 : 1500);
    }

    public static void tan(int x, int y) {
        them(TAN, x, y, 0xFFFFFF, 0, 700);
    }

    // ------------------------------------------------------------ vẽ (luồng trình bày)
    private static Color mau(int rgb, float a) {
        return new Color(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, Math.max(0, Math.min(255, (int) (a * 255))));
    }

    private static float sang(float t) {
        return 1 - (1 - t) * (1 - t);
    }

    public static void ve(Graphics2D g, int W, int H) {
        if (!bat()) return;
        long now = System.currentTimeMillis();
        int k = NenPc.heSo;
        Kinh.min(g);
        Composite cu = g.getComposite();
        try {
            boolean theGioi = coTheGioi && game.render.Canvas.currentScreen == game.render.Canvas.gameScr;
            if (theGioi) {
                veVignette(g, W, H);
                ArrayList<HU> ds;
                synchronized (DS) {
                    for (int i = DS.size() - 1; i >= 0; i--) {
                        if (now - DS.get(i).t0 > DS.get(i).ms) DS.remove(i);
                    }
                    ds = new ArrayList<HU>(DS);
                }
                java.awt.Shape clip = g.getClip();
                g.clipRect(NenPc.lechX, NenPc.lechY, NenPc.rong * k, NenPc.cao * k);
                for (HU h : ds) {
                    float t = (now - h.t0) / (float) h.ms;
                    float sx = NenPc.lechX + (h.x + camXChot) * k;
                    float sy = NenPc.lechY + (h.y + camYChot) * k;
                    switch (h.loai) {
                        case VA_CHAM: veVaCham(g, h, sx, sy - 10 * k, t, k); break;
                        case TAN: veTan(g, h, sx, sy - 8 * k, t, k); break;
                        default: veBung(g, h, sx, sy, t, k); break;
                    }
                }
                g.setClip(clip);
            }
            // chuyển màn: mờ dần từ xanh đêm
            long dt = now - tChuyenMan;
            if (dt >= 0 && dt < 380) {
                float a = 1 - dt / 380f;
                a = a * a;
                g.setComposite(AlphaComposite.SrcOver);
                g.setColor(new Color(8, 14, 32, (int) (a * 235)));
                g.fillRect(0, 0, W, H);
            }
        } finally {
            g.setComposite(cu);
        }
    }

    private static void veVignette(Graphics2D g, int W, int H) {
        BufferedImage v = vignette;
        if (v == null) {
            v = new BufferedImage(256, 144, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gv = v.createGraphics();
            gv.setPaint(new RadialGradientPaint(new Point2D.Float(128, 72), 150, new float[]{0f, 0.62f, 1f},
                    new Color[]{new Color(0, 0, 0, 0), new Color(6, 10, 30, 0), new Color(6, 10, 30, 110)},
                    MultipleGradientPaint.CycleMethod.NO_CYCLE));
            gv.fillRect(0, 0, 256, 144);
            gv.dispose();
            vignette = v;
        }
        g.drawImage(v, NenPc.lechX, NenPc.lechY, NenPc.rong * NenPc.heSo, NenPc.cao * NenPc.heSo, null);
    }

    /** Quầng sáng tròn (giả "cộng sáng": tâm trắng -> màu -> trong suốt). */
    private static void quang(Graphics2D g, float cx, float cy, float r, int rgb, float a) {
        if (r < 1 || a <= 0) return;
        g.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), r, new float[]{0f, 0.35f, 1f},
                new Color[]{mau(0xFFFFFF, a), mau(rgb, a * 0.7f), mau(rgb, 0)}));
        g.fill(new Ellipse2D.Float(cx - r, cy - r, 2 * r, 2 * r));
    }

    private static void veVaCham(Graphics2D g, HU h, float x, float y, float t, int k) {
        float e = sang(t);
        float r0 = (10 + h.manh * 5) * k;
        quang(g, x, y, r0 * (0.6f + 0.6f * e), h.mau, 0.85f * (1 - t));
        // vòng sóng
        g.setStroke(new BasicStroke(Math.max(1f, (2.2f - 1.6f * t) * k)));
        g.setColor(mau(h.mau, 0.9f * (1 - t)));
        float rr = r0 * (0.4f + 1.5f * e);
        g.draw(new Ellipse2D.Float(x - rr, y - rr * 0.75f, 2 * rr, 1.5f * rr));
        // tia
        g.setStroke(new BasicStroke(Math.max(1f, 1.4f * k * (1 - t)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (float a : h.goc) {
            float d0 = r0 * (0.3f + 1.1f * e), d1 = d0 + r0 * 0.5f * (1 - t);
            float c = (float) Math.cos(a), s = (float) Math.sin(a);
            g.setColor(mau(0xFFFFFF, 0.9f * (1 - t)));
            g.draw(new Line2D.Float(x + c * d0, y + s * d0 * 0.8f, x + c * d1, y + s * d1 * 0.8f));
        }
    }

    private static void veTan(Graphics2D g, HU h, float x, float y, float t, int k) {
        float e = sang(t);
        for (int i = 0; i < 5; i++) {
            float a = h.goc[i * 2];
            float d = 10 * k * e;
            quang(g, x + (float) Math.cos(a) * d, y + (float) Math.sin(a) * d * 0.5f - 6 * k * e, (5 + 5 * e) * k, 0xE8EEF8, 0.55f * (1 - t));
        }
    }

    private static void veBung(Graphics2D g, HU h, float x, float y, float t, int k) {
        boolean th = h.loai == BUNG_TIEN_HOA;
        float e = sang(Math.min(1, t * 1.6f));
        float mo = t < 0.75f ? 1 : (1 - t) / 0.25f;
        int mauChinh = th ? 0x9FE8FF : 0xFFD54A;
        // cột sáng
        float wc = (th ? 22 : 16) * k * (0.4f + 0.6f * e);
        float hc = (th ? 120 : 90) * k * e;
        g.setPaint(new GradientPaint(0, y - hc, mau(mauChinh, 0), 0, y, mau(0xFFFFFF, 0.55f * mo)));
        g.fill(new java.awt.geom.RoundRectangle2D.Float(x - wc / 2, y - hc, wc, hc, wc, wc));
        quang(g, x, y - 14 * k, (th ? 46 : 34) * k * (0.5f + 0.5f * e), mauChinh, 0.75f * mo);
        // tia xoay
        long now = System.currentTimeMillis();
        float quay = (now - h.t0) / 900f;
        g.setStroke(new BasicStroke(1.6f * k, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < h.goc.length; i++) {
            float a = h.goc[i] + quay;
            float d0 = 14 * k * e, d1 = d0 + (th ? 34 : 24) * k * e;
            g.setColor(mau(i % 2 == 0 ? 0xFFFFFF : mauChinh, 0.8f * mo));
            float c = (float) Math.cos(a), s = (float) Math.sin(a);
            g.draw(new Line2D.Float(x + c * d0, y - 14 * k + s * d0, x + c * d1, y - 14 * k + s * d1));
        }
        // vòng đôi
        for (int v = 0; v < 2; v++) {
            float tv = Math.max(0, Math.min(1, t * 1.4f - v * 0.25f));
            float rr = (12 + 40 * sang(tv)) * k;
            g.setStroke(new BasicStroke(Math.max(1f, 2.4f * k * (1 - tv))));
            g.setColor(mau(v == 0 ? 0xFFFFFF : mauChinh, 0.85f * (1 - tv)));
            g.draw(new Ellipse2D.Float(x - rr, y - rr * 0.45f, 2 * rr, rr * 0.9f));
        }
        // sao lấp lánh
        for (int i = 0; i < 8; i++) {
            float a = h.goc[i] * 3.1f;
            float d = (18 + 30 * ((i * 37 % 10) / 10f)) * k * e;
            float sx = x + (float) Math.cos(a) * d, sy = y - 20 * k - (float) Math.abs(Math.sin(a)) * d - 20 * k * t;
            float nh = (float) (0.5 + 0.5 * Math.sin((now - h.t0) / 90.0 + i));
            sao(g, sx, sy, (2 + 2 * nh) * k, mau(0xFFFFFF, mo * (0.4f + 0.6f * nh)));
        }
    }

    private static void sao(Graphics2D g, float x, float y, float r, Color c) {
        java.awt.geom.Path2D.Float p = new java.awt.geom.Path2D.Float();
        p.moveTo(x, y - r);
        p.quadTo(x, y, x + r, y);
        p.quadTo(x, y, x, y + r);
        p.quadTo(x, y, x - r, y);
        p.quadTo(x, y, x, y - r);
        g.setColor(c);
        g.fill(p);
    }

    // ------------------------------------------------------------ thời tiết HD (vẽ trong khung game)
    /** SinhThai.veSuongMu: sương mù HD - phủ xám xanh + dải sương liền trôi chậm theo thời gian thật. */
    public static boolean suongMu(Graphics g, int doDam) {
        if (!bat() || doDam <= 0) return false;
        Graphics2D g2 = g.g2d();
        int w = game.render.Canvas.w, h = game.render.Canvas.h;
        float d = doDam / 256f;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(214, 222, 232, (int) (110 * d)), 0, h, new Color(190, 200, 214, (int) (70 * d))));
        g2.fillRect(0, 0, w, h);
        double t = System.currentTimeMillis() / 1000.0;
        for (int i = 0; i < 5; i++) {
            float bw = w * 0.9f, bh = 18 + i * 6;
            float x = (float) (((i * 97 + t * (6 + i * 3)) % (w + bw)) - bw);
            float y = h * (i * 2 + 1) / 10f + (float) Math.sin(t * 0.4 + i) * 6;
            g2.setPaint(new RadialGradientPaint(new Point2D.Float(x + bw / 2, y), bw / 2, new float[]{0f, 1f},
                    new Color[]{new Color(240, 245, 250, (int) (90 * d)), new Color(240, 245, 250, 0)}));
            java.awt.geom.AffineTransform at = g2.getTransform();
            g2.translate(x + bw / 2, y);
            g2.scale(1, bh / bw);
            g2.translate(-(x + bw / 2), -y);
            g2.fill(new Ellipse2D.Float(x, y - bw / 2, bw, bw));
            g2.setTransform(at);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(g.getColor());
        return true;
    }

    /** SinhThai.veNangGat: nắng gắt HD - quầng mặt trời mềm, tia nắng dải màu, phủ ấm nhịp thở. */
    public static boolean nangGat(Graphics g, int doDam) {
        if (!bat() || doDam <= 0) return false;
        Graphics2D g2 = g.g2d();
        int w = game.render.Canvas.w, h = game.render.Canvas.h;
        float d = doDam / 256f;
        double t = System.currentTimeMillis() / 1000.0;
        float nhip = (float) (0.5 + 0.5 * Math.sin(t * 1.3));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(w, 0, new Color(255, 214, 90, (int) ((40 + 20 * nhip) * d)), w * 0.3f, h, new Color(255, 214, 90, 0)));
        g2.fillRect(0, 0, w, h);
        for (int i = 0; i < 5; i++) {
            float x0 = w - 20 - i * w / 6f;
            java.awt.geom.Path2D.Float p = new java.awt.geom.Path2D.Float();
            p.moveTo(x0, -4);
            p.lineTo(x0 + 10 + i * 3, -4);
            p.lineTo(x0 - h * 0.55f + 22 + i * 6, h);
            p.lineTo(x0 - h * 0.55f, h);
            p.closePath();
            g2.setPaint(new GradientPaint(x0, 0, new Color(255, 246, 190, (int) ((50 + 30 * nhip) * d)), x0 - h * 0.5f, h, new Color(255, 246, 190, 0)));
            g2.fill(p);
        }
        g2.setPaint(new RadialGradientPaint(new Point2D.Float(w - 6, 6), 46, new float[]{0f, 0.3f, 1f},
                new Color[]{new Color(255, 255, 235, (int) (240 * d)), new Color(255, 236, 140, (int) (170 * d)), new Color(255, 220, 120, 0)}));
        g2.fill(new Ellipse2D.Float(w - 52, -40, 92, 92));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(g.getColor());
        return true;
    }
}
