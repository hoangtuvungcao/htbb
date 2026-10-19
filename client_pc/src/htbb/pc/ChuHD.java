package htbb.pc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Chữ HD: font ẢNH của game (game.render.screen.Font, ảnh pixel 8..20 px) vẽ bằng font THẬT Noto Sans
 * (đủ tiếng Việt) ở độ phân giải thật của cửa sổ, khử răng cưa. port-j2me.py nối Font.drawString /
 * getWidthNguyenVan vào đây nên ngắt dòng, căn giữa... của game khớp bề rộng chữ mới.
 *
 * Kiểu chữ (màu, viền, cỡ, đường chân chữ) TỰ suy từ chính ảnh font: điểm ảnh trong lòng nét = màu chữ,
 * điểm mép = màu viền (font có viền), mép dưới phổ biến của các chữ = đường chân chữ. Nhờ vậy mỗi font của game
 * (trắng viền, vàng, đen, nhỏ màu...) giữ đúng màu/cảm giác cũ mà nét chữ mịn.
 */
public final class ChuHD {
    /** bật chữ HD (Cài đặt "Chữ HD"; --chu-anh = tắt) */
    public static volatile boolean bat = true;

    private static final FontRenderContext FRC = new FontRenderContext(new AffineTransform(), true, true);
    private static final IdentityHashMap<Object, Kieu> KIEU = new IdentityHashMap<Object, Kieu>();

    private ChuHD() {
    }

    static final class Kieu {
        Font font;
        float chan;          // khoảng từ đỉnh dòng tới đường chân chữ (px logic)
        Color mau;
        Color vien;          // null = không viền
        float doVien;
        boolean dung;        // false = để font ảnh vẽ
        final HashMap<String, Integer> rong = new HashMap<String, Integer>();
    }

    private static synchronized Kieu kieu(Object f, Image anh, int cao) {
        Kieu k = KIEU.get(f);
        if (k == null) {
            k = phanTich(anh, cao);
            KIEU.put(f, k);
        }
        return k;
    }

    private static int choiSang(int c) {
        return ((c >> 16 & 255) * 299 + (c >> 8 & 255) * 587 + (c & 255) * 114) / 1000;
    }

    private static int mode(HashMap<Integer, int[]> dem) {
        int best = 0;
        int n = -1;
        for (java.util.Map.Entry<Integer, int[]> e : dem.entrySet()) {
            if (e.getValue()[0] > n) {
                n = e.getValue()[0];
                best = e.getKey();
            }
        }
        return best;
    }

    private static Kieu phanTich(Image anh, int cao) {
        Kieu k = new Kieu();
        if (anh == null || cao <= 0) {
            return k;
        }
        BufferedImage b = anh.awt();
        int w = b.getWidth();
        int h = b.getHeight();
        int[] p = new int[w * h];
        b.getRGB(0, 0, w, h, p, 0, w);
        HashMap<Integer, int[]> trong = new HashMap<Integer, int[]>();
        HashMap<Integer, int[]> mep = new HashMap<Integer, int[]>();
        long sr = 0, sg = 0, sb = 0;
        int nTrong = 0, nDuc = 0;
        int soChu = h / cao;
        int[] day = new int[soChu];
        int[] dinh = new int[soChu];
        int nChu = 0;
        for (int c = 0; c < soChu; c++) {
            int top = -1, bot = -1;
            for (int y = c * cao; y < (c + 1) * cao; y++) {
                for (int x = 0; x < w; x++) {
                    int v = p[y * w + x];
                    if ((v >>> 24) < 160) continue;
                    nDuc++;
                    int ry = y - c * cao;
                    if (top < 0) top = ry;
                    bot = ry;
                    boolean laMep = x == 0 || x == w - 1 || ry == 0 || ry == cao - 1
                            || (p[y * w + x - 1] >>> 24) < 160 || (p[y * w + x + 1] >>> 24) < 160
                            || (p[(y - 1) * w + x] >>> 24) < 160 || (p[(y + 1) * w + x] >>> 24) < 160;
                    Integer key = v & 0xFFFFFF;
                    HashMap<Integer, int[]> m = laMep ? mep : trong;
                    int[] d = m.get(key);
                    if (d == null) m.put(key, d = new int[1]);
                    d[0]++;
                    if (!laMep) {
                        nTrong++;
                        sr += v >> 16 & 255;
                        sg += v >> 8 & 255;
                        sb += v & 255;
                    }
                }
            }
            if (top >= 0) {
                dinh[nChu] = top;
                day[nChu] = bot;
                nChu++;
            }
        }
        if (nDuc == 0 || nChu == 0) {
            return k;
        }
        int[] d2 = Arrays.copyOf(day, nChu);
        int[] t2 = Arrays.copyOf(dinh, nChu);
        Arrays.sort(d2);
        Arrays.sort(t2);
        int chan = d2[nChu / 2] + 1;                 // đường chân chữ (đa số chữ đứng trên đó)
        int dinhCao = t2[Math.max(0, nChu / 10)];    // đỉnh chữ cao (hoa có dấu)
        boolean coVien = nTrong * 100 / nDuc >= 12 && !mep.isEmpty();
        int mau;
        if (coVien) {
            mau = (int) (sr / nTrong) << 16 | (int) (sg / nTrong) << 8 | (int) (sb / nTrong);
            int v = mode(mep);
            if (Math.abs(choiSang(v) - choiSang(mau)) < 40) {
                coVien = false; // "viền" cùng màu chữ = nét dày
            } else {
                k.vien = new Color(v);
            }
        } else {
            HashMap<Integer, int[]> tat = new HashMap<Integer, int[]>(mep);
            for (java.util.Map.Entry<Integer, int[]> e : trong.entrySet()) {
                int[] d = tat.get(e.getKey());
                if (d == null) tat.put(e.getKey(), d = new int[1]);
                d[0] += e.getValue()[0];
            }
            mau = mode(tat);
        }
        k.mau = new Color(mau);
        int cuaNet = Math.max(4, chan - dinhCao);       // cao từ đỉnh chữ hoa có dấu tới chân
        float em = cuaNet / 1.12f;
        if (coVien) em -= 1.2f;
        em = Math.max(6f, Math.min(em, cao * 1.05f));
        k.font = PhongChu.thuong(coVien || cao >= 15).deriveFont(em);
        k.chan = chan - (coVien ? 1.1f : 0.6f);
        k.doVien = 0.7f;
        k.dung = true;
        NhatKy.ghi("ChuHD: font " + cao + "px " + b.getWidth() + "x" + h + " mau=" + Integer.toHexString(mau)
                + (k.vien != null ? " vien=" + Integer.toHexString(k.vien.getRGB() & 0xFFFFFF) : "")
                + " em=" + em + " chan=" + k.chan);
        return k;
    }

    /**
     * Ký hiệu phông Noto Sans đi kèm KHÔNG có (→ − ≥ ≤ ★ ✓ ▼ ♪ ●... - font ảnh của game vẽ được, server gửi trong
     * thông báo vd "Mốc: (x,y) → ...", "(−5% EXP)") -> thay bằng chữ có trong phông, tránh ô vuông "tofu".
     */
    static String thayKyHieu(String s) {
        int n = s.length();
        int i = 0;
        while (i < n && s.charAt(i) < 0x2190) i++;
        if (i == n) return s;
        StringBuilder b = new StringBuilder(n + 4);
        b.append(s, 0, i);
        for (; i < n; i++) {
            char c = s.charAt(i);
            if (c < 0x2190) {
                b.append(c);
                continue;
            }
            switch (c) {
                case '\u2192': case '\u27A4': case '\u279C': case '\u25B6': case '\u25BA': b.append("->"); break;   // → ➤ ➜ ▶ ►
                case '\u2190': case '\u25C0': case '\u25C4': b.append("<-"); break;                              // ← ◀ ◄
                case '\u2191': case '\u25B2': b.append('^'); break;                                               // ↑ ▲
                case '\u2193': case '\u25BC': b.append('v'); break;                                               // ↓ ▼
                case '\u2212': b.append('-'); break;                                                               // −
                case '\u2265': b.append(">="); break;
                case '\u2264': b.append("<="); break;
                case '\u2605': case '\u2606': case '\u2B50': b.append('*'); break;                                // ★ ☆ ⭐
                case '\u2713': case '\u2714': b.append("(v)"); break;                                             // ✓ ✔
                case '\u2715': case '\u2716': case '\u2717': case '\u2718': b.append('\u00D7'); break;           // ✕ ✖ ✗ ✘ -> ×
                case '\u25CF': case '\u25CB': case '\u25A0': case '\u25A1': case '\u25C6': b.append('\u2022'); break; // ● ○ ■ □ ◆ -> •
                case '\u2665': case '\u2764': b.append("<3"); break;
                case '\u266A': case '\u266B': b.append('~'); break;                                               // ♪ ♫
                default: b.append(c); break;
            }
        }
        return b.toString();
    }

    /** Bề rộng chữ HD (px logic), -1 = để font ảnh đo. */
    public static int rong(Object f, Image anh, int cao, String s) {
        Kieu k = kieu(f, anh, cao);
        if (!k.dung) return -1;
        synchronized (k) {
            Integer v = k.rong.get(s);
            if (v != null) return v.intValue();
            int r = (int) Math.ceil(k.font.getStringBounds(thayKyHieu(s), FRC).getWidth() + (k.vien != null ? 1 : 0));
            if (k.rong.size() > 4000) k.rong.clear();
            k.rong.put(s, Integer.valueOf(r));
            return r;
        }
    }

    private static final float[][] HUONG = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-0.7f, -0.7f}, {0.7f, -0.7f}, {-0.7f, 0.7f}, {0.7f, 0.7f}};

    /** Ảnh chữ đã dựng (độ phân giải thật) - vẽ lại chỉ là chép ảnh. */
    static final class Anh {
        int bien;          // 0 thường, 1 tối (chữ sáng trên nền sáng), 2 trắng (chữ tối trên nền tối)
        double tiLe;
        BufferedImage img;
        Anh sau;
    }

    private static final HashMap<Object, HashMap<String, Anh>> ANH = new HashMap<Object, HashMap<String, Anh>>();
    private static long soDiemAnh;
    private static final Color CHU_TOI = new Color(game.pokemon.ChuDe.CHU);

    private static synchronized Anh layAnh(Kieu k, String s, int bien, double tl, int w, int cao) {
        HashMap<String, Anh> m = ANH.get(k);
        if (m == null) {
            m = new HashMap<String, Anh>();
            ANH.put(k, m);
        }
        Anh dau = m.get(s);
        for (Anh a = dau; a != null; a = a.sau) {
            if (a.bien == bien && a.tiLe == tl) return a;
        }
        Anh a = new Anh();
        a.bien = bien;
        a.tiLe = tl;
        a.img = dung(k, s, bien, tl, w, cao);
        a.sau = dau;
        if (soDiemAnh > 8000000L) {
            for (HashMap<String, Anh> x : ANH.values()) x.clear();
            soDiemAnh = 0;
            a.sau = null;
        }
        soDiemAnh += (long) a.img.getWidth() * a.img.getHeight();
        m.put(s, a);
        return a;
    }

    /** Dựng ảnh chữ: góc ảnh = (x - 1, y - 1) logic; chân chữ tại 1 + k.chan. */
    private static BufferedImage dung(Kieu k, String s, int bien, double tl, int w, int cao) {
        String t = thayKyHieu(s);
        int iw = (int) Math.ceil((w + 3) * tl) + 1, ih = (int) Math.ceil((cao + 3) * tl) + 1;
        BufferedImage b = new BufferedImage(Math.max(1, iw), Math.max(1, ih), BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = b.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.scale(tl, tl);
        g.setFont(k.font);
        float bx = 1 + (k.vien != null ? 0.5f : 0f), by = 1 + k.chan;
        if (k.vien != null && bien == 0) {
            g.setColor(k.vien);
            float d = k.doVien;
            for (int i = 0; i < HUONG.length; i++) g.drawString(t, bx + HUONG[i][0] * d, by + HUONG[i][1] * d);
        } else if (bien == 2) {
            g.setColor(new Color(0, 0, 0, 110));
            g.drawString(t, bx + 0.35f, by + 0.6f);
        }
        g.setColor(bien == 1 ? CHU_TOI : bien == 2 ? Color.WHITE : k.mau);
        g.drawString(t, bx, by);
        g.dispose();
        return b;
    }

    /** Kiểm thử (ChupTrongGame): ghi mọi chuỗi đã vẽ để soát chữ thô / từ cấm; null = tắt. */
    public static volatile java.util.Set<String> ghi;

    /** Vẽ chữ HD (align: 0 trái, 1 phải, 2 giữa như game). false = để font ảnh vẽ. */
    public static boolean ve(Object f, Image anh, int cao, Graphics g, String s, int x, int y, int align) {
        java.util.Set<String> gh = ghi;
        if (gh != null && s != null) gh.add(s);
        Kieu k = kieu(f, anh, cao);
        if (!k.dung) return false;
        if (s.length() == 0) return true;
        int w = rong(f, anh, cao, s);
        int x1 = align == 0 ? x : align == 1 ? x - w : x - (w >> 1);
        // tương phản tự động (như APK HdChu): nền dưới tâm chữ do GiaoDienHD ghi; không biết thì theo Font.nenSang
        double tl = g.tiLeThat();
        int bien = 0;
        int lum = choiSang(k.mau.getRGB());
        if (g.tiLeTheGioi() == 1f) {
            int nen = GiaoDienHD.soNen == 0 ? -1 : GiaoDienHD.sangTai((float) ((x1 + g.getTranslateX() + w / 2f) * tl),
                    (float) ((y + g.getTranslateY() + cao / 2f) * tl));
            boolean nenSang = nen >= 0 ? nen > 165 : game.render.screen.Font.nenSang;
            if (nenSang && lum > 150) bien = 1;
            else if (nen >= 0 && nen < 130 && lum < 90 && k.vien == null) bien = 2;
        }
        Anh a = layAnh(k, s, bien, tl, w, cao);
        g.veAnhThat(a.img, x1 - 1, y - 1);
        if (KiemChong.bat) {
            Graphics2D g2 = g.g2d();
            java.awt.Rectangle r = g2.getTransform().createTransformedShape(
                    new java.awt.geom.Rectangle2D.Float(x1 + g.getTranslateX(), y + g.getTranslateY() + 1, w, cao - 2)).getBounds();
            java.awt.Shape cl = g2.getClip();
            KiemChong.ghi(s, r, cl == null ? null : g2.getTransform().createTransformedShape(cl).getBounds(), g.tiLeTheGioi() != 1f);
        }
        return true;
    }
}
