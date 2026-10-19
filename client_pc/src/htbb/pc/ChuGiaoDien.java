package htbb.pc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.microedition.lcdui.Graphics;

/**
 * Chữ giao diện 0.9.5 (game.pokemon.ChuUI) của bản PC: Noto Sans thường / đậm đúng cỡ (px logic, có phần lẻ), khử răng
 * cưa ở độ phân giải thật. Bề ngang và ẢNH chữ đã dựng được nhớ (vẽ lại = một lần chép ảnh, không dựng chữ / không cấp
 * phát mỗi khung).
 */
public final class ChuGiaoDien extends game.pokemon.ChuUI {
    static final ChuGiaoDien I = new ChuGiaoDien();
    private static final FontRenderContext FRC = new FontRenderContext(new AffineTransform(), true, true);

    /** chỉ số kiểu = round(co*10)*3 + dam */
    private final Font[] font = new Font[3 * 512];
    @SuppressWarnings("unchecked")
    private final HashMap<String, Integer>[] rong = new HashMap[3 * 512];
    @SuppressWarnings("unchecked")
    private final HashMap<String, Anh>[] anh = new HashMap[3 * 512];
    private long soDiemAnh;
    static final long TOI_DA_DIEM_ANH = 6000000L;

    static final class Anh {
        int mau;
        double tiLe;
        BufferedImage img;
        float lechX;
        Anh sau;
    }

    private ChuGiaoDien() {
    }

    static void bat() {
        game.pokemon.ChuUI.bo = I;
    }

    private static int chiSo(float co, int dam) {
        int c = Math.max(1, Math.min(511, Math.round(co * 10)));
        return c * 3 + Math.max(0, Math.min(2, dam));
    }

    private Font f(int k) {
        Font r = font[k];
        if (r == null) {
            r = PhongChu.thuong(k % 3 != 0).deriveFont((k / 3) / 10f);
            font[k] = r;
        }
        return r;
    }

    public int cao(float co) {
        return (int) Math.ceil(co * 1.42f);
    }

    public synchronized int rong(String s, float co, int dam) {
        int k = chiSo(co, dam);
        HashMap<String, Integer> m = rong[k];
        if (m == null) {
            m = new HashMap<String, Integer>();
            rong[k] = m;
        }
        Integer v = m.get(s);
        if (v != null) return v.intValue();
        int r = (int) Math.ceil(f(k).getStringBounds(ChuHD.thayKyHieu(s), FRC).getWidth());
        if (m.size() > 3000) m.clear();
        m.put(s, Integer.valueOf(r));
        return r;
    }

    public void ve(Graphics g, String s, int x, int y, float co, int dam, int mau, int canh) {
        java.util.Set<String> gh = ChuHD.ghi;
        if (gh != null && s != null) gh.add(s);
        int k = chiSo(co, dam);
        int w = rong(s, co, dam);
        float x1 = canh == 0 ? x : canh == 1 ? x - w : x - w / 2f;
        double tl = g.tiLeThat();
        Anh a = lay(k, s, mau & 0xFFFFFF, tl);
        // đường chân chữ ~ 0.78 * cao hộp: ảnh đặt ở góc (x1 - 1, y - 1) logic
        g.veAnhThat(a.img, x1 - a.lechX, y - 1);
        if (KiemChong.bat) {
            Graphics2D g2 = g.g2d();
            java.awt.Rectangle r = g2.getTransform().createTransformedShape(
                    new java.awt.geom.Rectangle2D.Float(x1 + g.getTranslateX(), y + g.getTranslateY() + 1, w, cao(co) - 2)).getBounds();
            java.awt.Shape cl = g2.getClip();
            KiemChong.ghi(s, r, cl == null ? null : g2.getTransform().createTransformedShape(cl).getBounds(), g.tiLeTheGioi() != 1f);
        }
    }

    private synchronized Anh lay(int k, String s, int mau, double tl) {
        HashMap<String, Anh> m = anh[k];
        if (m == null) {
            m = new HashMap<String, Anh>();
            anh[k] = m;
        }
        Anh dau = m.get(s);
        for (Anh a = dau; a != null; a = a.sau) {
            if (a.mau == mau && a.tiLe == tl) return a;
        }
        Anh a = dung(k, s, mau, tl);
        a.sau = dau;
        if (soDiemAnh > TOI_DA_DIEM_ANH) {
            for (int i = 0; i < anh.length; i++) if (anh[i] != null) anh[i].clear();
            soDiemAnh = 0;
            a.sau = null;
        }
        soDiemAnh += (long) a.img.getWidth() * a.img.getHeight();
        m.put(s, a);
        return a;
    }

    private Anh dung(int k, String s, int mau, double tl) {
        float co = (k / 3) / 10f;
        String t = ChuHD.thayKyHieu(s);
        Font fo = f(k);
        float w = (float) fo.getStringBounds(t, FRC).getWidth();
        int iw = (int) Math.ceil((w + 2) * tl) + 2;
        int ih = (int) Math.ceil((cao(co) + 2) * tl) + 2;
        BufferedImage b = new BufferedImage(Math.max(1, iw), Math.max(1, ih), BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = b.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.scale(tl, tl);
        g.setFont(fo);
        g.setColor(new Color(mau));
        // đường chân chữ: căn giữa chữ hoa trong hộp dòng (cao = 1.42 co)
        float chan = 1 + cao(co) * 0.5f + co * 0.36f;
        g.drawString(t, 1f, chan);
        g.dispose();
        Anh a = new Anh();
        a.mau = mau;
        a.tiLe = tl;
        a.img = b;
        a.lechX = 1f;
        return a;
    }

    private static final BasicStroke[] NET = new BasicStroke[16];

    /** Dấu X / mũi tên / dấu cộng vector khử răng cưa. */
    public boolean net(Graphics g, int cx, int cy, int k, int mau, int kieu) {
        if (!GiaoDienHD.bat) return false;
        Graphics2D g2 = g.g2d();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int i = Math.max(1, Math.min(15, Math.round(k * 0.35f * 2)));
        BasicStroke st = NET[i];
        if (st == null) {
            st = new BasicStroke(i / 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            NET[i] = st;
        }
        java.awt.Stroke cu = g2.getStroke();
        g2.setStroke(st);
        g2.setColor(GiaoDienHD.c(0xFF000000 | mau));
        float x = cx + g.getTranslateX() + 0.5f, y = cy + g.getTranslateY() + 0.5f;
        if (kieu == 0) {
            g2.draw(new Line2D.Float(x - k, y - k, x + k, y + k));
            g2.draw(new Line2D.Float(x + k, y - k, x - k, y + k));
        } else if (kieu == 1) {
            g2.draw(new Line2D.Float(x + k * 0.5f, y - k, x - k * 0.5f, y));
            g2.draw(new Line2D.Float(x - k * 0.5f, y, x + k * 0.5f, y + k));
        } else {
            g2.draw(new Line2D.Float(x - k, y, x + k, y));
            g2.draw(new Line2D.Float(x, y - k, x, y + k));
        }
        g2.setStroke(cu);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(g.getColor());
        return true;
    }

    private final java.awt.geom.Path2D.Float duong = new java.awt.geom.Path2D.Float();
    private final java.awt.geom.Ellipse2D.Float elip = new java.awt.geom.Ellipse2D.Float();
    private final java.awt.geom.RoundRectangle2D.Float hcn = new java.awt.geom.RoundRectangle2D.Float();
    private final java.awt.geom.Arc2D.Float cung = new java.awt.geom.Arc2D.Float();
    private final Line2D.Float doan = new Line2D.Float();
    private final int[] mauKhoa = new int[128];
    private final Color[] mauGt = new Color[128];
    private int mauSo;
    private final BasicStroke[] netDem = new BasicStroke[2048];

    /** Màu RGB -> Color (bảng nhỏ, không cấp phát khi đã có). */
    private Color mau(int rgb) {
        for (int i = 0; i < mauSo; i++) if (mauKhoa[i] == rgb) return mauGt[i];
        int i = mauSo < mauKhoa.length ? mauSo++ : (rgb & 127);
        mauKhoa[i] = rgb;
        mauGt[i] = new Color(rgb);
        return mauGt[i];
    }

    private BasicStroke net(float w) {
        int k = Math.max(1, Math.min(2047, Math.round(w * 20)));
        BasicStroke b = netDem[k];
        if (b == null) {
            b = new BasicStroke(k / 20f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            netDem[k] = b;
        }
        return b;
    }

    // ---- 0.9.5: ảnh PNG giao diện (game.pokemon.AnhUI, res/ui) - tô màu + thu phóng một lần, nhớ theo cỡ thật ----
    private int[] tamIcon, tamNut;
    private int tamIconRong, tamNutRong;
    private boolean daNap;
    private final HashMap<Long, AnhNho> anhUI = new HashMap<Long, AnhNho>();

    static final class AnhNho {
        long khoa2;
        BufferedImage img;
        AnhNho sau;
    }

    private static int[] doc(String ten, int[] rong) {
        try {
            BufferedImage b = javax.microedition.lcdui.Image.createImage(ten).awt();
            rong[0] = b.getWidth();
            return b.getRGB(0, 0, b.getWidth(), b.getHeight(), null, 0, b.getWidth());
        } catch (Exception e) {
            return null;
        }
    }

    private void nap() {
        if (daNap) return;
        daNap = true;
        int[] r = new int[1];
        tamIcon = doc(game.pokemon.AnhUI.TAM_ICON, r);
        tamIconRong = r[0];
        tamNut = doc(game.pokemon.AnhUI.TAM_NUT, r);
        tamNutRong = r[0];
    }

    /** Ô o (O x O) của tấm, đã tô màu, ảnh ARGB tiền nhân; null = tấm thiếu / ô ngoài tấm. */
    private static BufferedImage o(int[] tam, int rong, int o, int mau, int vien) {
        int n = game.pokemon.AnhUI.O, cot = rong / n;
        if (tam == null || cot <= 0) return null;
        int ox = (o % cot) * n, oy = (o / cot) * n;
        if (oy + n > tam.length / rong) return null;
        int[] p = new int[n * n];
        for (int y = 0; y < n; y++) System.arraycopy(tam, (oy + y) * rong + ox, p, y * n, n);
        game.pokemon.AnhUI.to(p, p.length, mau, vien);
        BufferedImage b = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        b.setRGB(0, 0, n, n, p, 0, n);
        return b;
    }

    /** Thu phóng chất lượng cao (thu nhiều bước một nửa để không răng cưa). */
    private static BufferedImage thu(BufferedImage b, int w, int h) {
        while (b.getWidth() / 2 >= w && b.getHeight() / 2 >= h) b = co(b, b.getWidth() / 2, b.getHeight() / 2);
        return b.getWidth() == w && b.getHeight() == h ? b : co(b, w, h);
    }

    private static BufferedImage co(BufferedImage b, int w, int h) {
        BufferedImage r = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = r.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(b, 0, 0, w, h, null);
        g.dispose();
        return r;
    }

    private BufferedImage nho(long k1, long k2) {
        for (AnhNho a = anhUI.get(k1); a != null; a = a.sau) if (a.khoa2 == k2) return a.img;
        return null;
    }

    private void giu(long k1, long k2, BufferedImage img) {
        if (anhUI.size() > 600) anhUI.clear();
        AnhNho a = new AnhNho();
        a.khoa2 = k2;
        a.img = img;
        a.sau = anhUI.get(k1);
        anhUI.put(k1, a);
    }

    /** Icon PNG ô o cạnh s (logic) tại (x, y). false = không có ảnh. */
    private boolean veIcon(Graphics g, int o, int x, int y, int s, int chinh, int nen) {
        nap();
        if (tamIcon == null) return false;
        int px = Math.max(4, Math.min(1023, (int) Math.round(s * g.tiLeThat())));
        int mau = game.pokemon.AnhUI.mau(o, chinh), vien = game.pokemon.AnhUI.vien(o, chinh, nen);
        long k1 = (long) o << 40 | (long) px << 24 | mau, k2 = vien;
        BufferedImage img = nho(k1, k2);
        if (img == null) {
            BufferedImage b = o(tamIcon, tamIconRong, o, mau, vien);
            if (b == null) return false;
            img = thu(b, px, px);
            giu(k1, k2, img);
        }
        g.veAnhThat(img, x, y);
        return true;
    }

    /** Da nút 9-slice kiểu k, cỡ w x h (logic), màu chinh, viền vien. */
    private boolean veNut(Graphics g, int k, int x, int y, int w, int h, int chinh, int vien) {
        nap();
        if (tamNut == null || w <= 0 || h <= 0) return false;
        double tl = g.tiLeThat();
        int pw = Math.max(2, Math.min(4095, (int) Math.round(w * tl))), ph = Math.max(2, Math.min(4095, (int) Math.round(h * tl)));
        long k1 = 1L << 62 | (long) k << 48 | (long) pw << 24 | chinh, k2 = (long) ph << 24 | vien;
        BufferedImage img = nho(k1, k2);
        if (img == null) {
            BufferedImage b = o(tamNut, tamNutRong, k, chinh, vien);
            if (b == null) return false;
            if (k >= game.pokemon.AnhUI.NUT_TRON) {
                img = thu(b, pw, ph);
            } else {
                int n = game.pokemon.AnhUI.O, c = game.pokemon.AnhUI.GOC_NUT;
                int d = Math.max(2, Math.min((int) Math.round(10 * tl), Math.min(pw, ph) / 2));
                BufferedImage bg = d * 2 <= c ? thu(b, n * d / c, n * d / c) : b;
                int sn = bg.getWidth(), sc = sn * c / n;
                img = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_ARGB_PRE);
                Graphics2D g2 = img.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                int[] dx = {0, d, pw - d, pw}, dy = {0, d, ph - d, ph}, sx = {0, sc, sn - sc, sn};
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (dx[j + 1] > dx[j] && dy[i + 1] > dy[i]) {
                            g2.drawImage(bg, dx[j], dy[i], dx[j + 1], dy[i + 1], sx[j], sx[i], sx[j + 1], sx[i + 1], null);
                        }
                    }
                }
                g2.dispose();
            }
            giu(k1, k2, img);
        }
        g.veAnhThat(img, x, y);
        return true;
    }

    // ---- icon chiêu (res/ui/chieu_*.png, đã tô màu sẵn): chọn tấm nhỏ nhất >= cỡ thật, thu phóng một lần ----
    private final int[][] tamChieu = new int[6][];
    private final int[] tamChieuRong = new int[6];
    private final boolean[] daNapChieu = new boolean[6];

    public synchronized boolean chieu(Graphics g, int o, boolean tron, int x, int y, int s, boolean khoa) {
        if (!GiaoDienHD.bat || o < 0 || s <= 0) return false;
        int px = Math.max(4, Math.min(1023, (int) Math.round(s * g.tiLeThat())));
        int[] co = game.pokemon.AnhUI.CO_CHIEU;
        int t = co.length - 1;
        for (int i = 0; i < co.length; i++) if (co[i] >= px) { t = i; break; }
        long k1 = 3L << 61 | (long) o << 40 | (long) px << 8 | (tron ? 2 : 0) | (khoa ? 1 : 0), k2 = 0;
        BufferedImage img = nho(k1, k2);
        if (img == null) {
            int[] tam = null;
            int n = 0, rong = 0;
            for (int i = t; i >= 0 && tam == null; i--) {   // thiếu tấm lớn -> dùng tấm nhỏ hơn
                int j = (tron ? 3 : 0) + i;
                if (!daNapChieu[j]) {
                    daNapChieu[j] = true;
                    int[] r = new int[1];
                    tamChieu[j] = doc(game.pokemon.AnhUI.tamChieu(tron, co[i]), r);
                    tamChieuRong[j] = r[0];
                }
                tam = tamChieu[j];
                rong = tamChieuRong[j];
                n = co[i];
            }
            if (tam == null) return false;
            int cot = rong / n, ox = (o % cot) * n, oy = (o / cot) * n;
            if (cot <= 0 || oy + n > tam.length / rong) return false;
            int[] p = new int[n * n];
            for (int yy = 0; yy < n; yy++) System.arraycopy(tam, (oy + yy) * rong + ox, p, yy * n, n);
            boolean rong0 = true;
            for (int i = 0; i < p.length && rong0; i++) if ((p[i] >>> 24) != 0) rong0 = false;
            if (rong0) return false;
            if (khoa) {   // chưa học: xám, mờ
                for (int i = 0; i < p.length; i++) {
                    int c = p[i], a = c >>> 24;
                    int l = (((c >> 16) & 255) * 3 + ((c >> 8) & 255) * 6 + (c & 255)) / 10;
                    l = 120 + l * 2 / 5;
                    p[i] = (a * 3 / 4) << 24 | l << 16 | l << 8 | l;
                }
            }
            BufferedImage b = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
            b.setRGB(0, 0, n, n, p, 0, n);
            img = thu(b, px, px);
            giu(k1, k2, img);
        }
        g.veAnhThat(img, x, y);
        return true;
    }

    /** Biểu tượng vector (game.pokemon.BieuTuong) khử răng cưa ở độ phân giải thật. */
    public synchronized boolean hinh(Graphics g, float[] h, int x, int y, int s, int chinh, int nen) {
        if (!GiaoDienHD.bat) return false;
        int o = game.pokemon.AnhUI.o(h);
        if (o >= 0 && veIcon(g, o, x, y, s, chinh, nen)) return true;
        int kn = game.pokemon.AnhUI.nut(h);
        if (kn >= 0) return veNut(g, kn, x, y, s >>> 16, s & 0xFFFF, chinh, nen);
        if (o == 0) return false;
        Graphics2D g2 = g.g2d();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        java.awt.Stroke cu = g2.getStroke();
        float ox = x + g.getTranslateX(), oy = y + g.getTranslateY(), k = s / 100f;
        int i = 0;
        while (i < h.length) {
            int lenh = (int) h[i++];
            switch (lenh) {
                case game.pokemon.BieuTuong.MAU:
                    g2.setColor(mau(mauLenh((int) h[i++], chinh, nen)));
                    break;
                case game.pokemon.BieuTuong.TRON:
                    elip.setFrame(ox + (h[i] - h[i + 2]) * k, oy + (h[i + 1] - h[i + 2]) * k, 2 * h[i + 2] * k, 2 * h[i + 2] * k);
                    g2.fill(elip);
                    i += 3;
                    break;
                case game.pokemon.BieuTuong.CHU_NHAT:
                    hcn.setRoundRect(ox + h[i] * k, oy + h[i + 1] * k, h[i + 2] * k, h[i + 3] * k, 2 * h[i + 4] * k, 2 * h[i + 4] * k);
                    g2.fill(hcn);
                    i += 5;
                    break;
                case game.pokemon.BieuTuong.NET:
                    g2.setStroke(net(h[i + 4] * k));
                    doan.setLine(ox + h[i] * k, oy + h[i + 1] * k, ox + h[i + 2] * k, oy + h[i + 3] * k);
                    g2.draw(doan);
                    i += 5;
                    break;
                case game.pokemon.BieuTuong.VONG:
                    g2.setStroke(net(h[i + 3] * k));
                    elip.setFrame(ox + (h[i] - h[i + 2]) * k, oy + (h[i + 1] - h[i + 2]) * k, 2 * h[i + 2] * k, 2 * h[i + 2] * k);
                    g2.draw(elip);
                    i += 4;
                    break;
                case game.pokemon.BieuTuong.CUNG: {
                    float r = h[i + 2];
                    if (h[i + 5] <= 0) {
                        cung.setArc(ox + (h[i] - r) * k, oy + (h[i + 1] - r) * k, 2 * r * k, 2 * r * k, h[i + 3], h[i + 4], java.awt.geom.Arc2D.PIE);
                        g2.fill(cung);
                    } else {
                        g2.setStroke(net(h[i + 5] * k));
                        cung.setArc(ox + (h[i] - r) * k, oy + (h[i + 1] - r) * k, 2 * r * k, 2 * r * k, h[i + 3], h[i + 4], java.awt.geom.Arc2D.OPEN);
                        g2.draw(cung);
                    }
                    i += 6;
                    break;
                }
                case game.pokemon.BieuTuong.DA_GIAC:
                case game.pokemon.BieuTuong.DUONG: {
                    int n = (int) h[i++];
                    float w = lenh == game.pokemon.BieuTuong.DUONG ? h[i++] : 0;
                    duong.reset();
                    for (int q = 0; q < n; q++) {
                        float px = ox + h[i + 2 * q] * k, py = oy + h[i + 2 * q + 1] * k;
                        if (q == 0) duong.moveTo(px, py);
                        else duong.lineTo(px, py);
                    }
                    if (lenh == game.pokemon.BieuTuong.DA_GIAC) {
                        duong.closePath();
                        g2.fill(duong);
                    } else {
                        g2.setStroke(net(w * k));
                        g2.draw(duong);
                    }
                    i += 2 * n;
                    break;
                }
                default:
                    i = h.length;
                    break;
            }
        }
        g2.setStroke(cu);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(g.getColor());
        return true;
    }

    static int mauLenh(int m, int chinh, int nen) {
        switch (m) {
            case game.pokemon.BieuTuong.M_TRANG:
                return 0xFFFFFF;
            case game.pokemon.BieuTuong.M_TOI:
                return game.pokemon.GiaoDien.pha(chinh, 0, 1, 4);
            case game.pokemon.BieuTuong.M_NHAT:
                return game.pokemon.GiaoDien.pha(chinh, 0xFFFFFF, 2, 5);
            case game.pokemon.BieuTuong.M_NEN:
                return nen;
            default:
                return chinh;
        }
    }

    public void khungMoi() {
        GiaoDienHD.khungMoi();
    }
}
