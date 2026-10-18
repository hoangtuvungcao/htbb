package server.quantri;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.JComponent;

import static server.quantri.GiaoDien.s;

/** Bieu do ve bang Java2D: duong (nhieu chuoi), cot, lich 52 tuan. Khong thu vien ngoai. */
public final class BieuDo {

    private BieuDo() {
    }

    /** Nguon so lieu doc truc tiep (khong chep mang) - vd vong dem trong DuLieuTrucTiep. */
    public interface Chuoi {
        int soDiem();

        double giaTri(int i);
    }

    /** Vong dem co dinh kich thuoc - ghi khong cap phat. Chi mot luong ghi. */
    public static final class VongDem implements Chuoi {
        private final double[] a;
        private int dau;
        private int n;

        public VongDem(final int sucChua) {
            a = new double[sucChua];
        }

        public synchronized void them(final double v) {
            a[(dau + n) % a.length] = v;
            if (n < a.length) {
                n++;
            } else {
                dau = (dau + 1) % a.length;
            }
        }

        @Override
        public synchronized int soDiem() {
            return n;
        }

        @Override
        public synchronized double giaTri(final int i) {
            return a[(dau + i) % a.length];
        }

        public synchronized double cuoi() {
            return n == 0 ? 0 : a[(dau + n - 1) % a.length];
        }

        public synchronized double lonNhat() {
            double m = 0;
            for (int i = 0; i < n; i++) {
                m = Math.max(m, a[(dau + i) % a.length]);
            }
            return m;
        }
    }

    /** Chuoi tu mang co dinh. */
    public static Chuoi tuMang(final double[] v) {
        return new Chuoi() {
            @Override
            public int soDiem() {
                return v.length;
            }

            @Override
            public double giaTri(final int i) {
                return v[i];
            }
        };
    }

    // ================================================================== duong nho trong the

    static void veDuongNho(final Graphics2D g, final Chuoi c, final Color mau, final float x, final float y,
            final float w, final float h) {
        final int n = c.soDiem();
        if (n < 2) {
            return;
        }
        double max = 0, min = Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            max = Math.max(max, c.giaTri(i));
            min = Math.min(min, c.giaTri(i));
        }
        if (max - min < 1e-9) {
            max = min + 1;
        }
        min = Math.min(min, max - (max - min) * 1.6); // de khoang trong o day cho de nhin
        final GeneralPath p = new GeneralPath();
        for (int i = 0; i < n; i++) {
            final float px = x + w * i / (n - 1);
            final float py = (float) (y + h - (c.giaTri(i) - min) / (max - min) * h);
            if (i == 0) {
                p.moveTo(px, py);
            } else {
                p.lineTo(px, py);
            }
        }
        final GeneralPath vung = new GeneralPath(p);
        vung.lineTo(x + w, y + h);
        vung.lineTo(x, y + h);
        vung.closePath();
        g.setPaint(new GradientPaint(0, y, GiaoDien.trong(mau, 70), 0, y + h, GiaoDien.trong(mau, 0)));
        g.fill(vung);
        g.setColor(GiaoDien.trong(mau, 200));
        g.setStroke(new BasicStroke(GiaoDien.sf(1.6f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(p);
    }

    // ================================================================== bieu do duong

    /** Bieu do duong nhieu chuoi, luoi + nhan truc, ro chuot hien gia tri. */
    public static class Duong extends JComponent {
        private final List<Chuoi> chuoi = new ArrayList<Chuoi>();
        private final List<Color> mau = new ArrayList<Color>();
        private final List<String> ten = new ArrayList<String>();
        private String donVi = "";
        private String nhanTrai = "30 phút trước";
        private String nhanPhai = "bây giờ";
        private int chuot = -1;
        private double tranCoDinh = -1;
        private int dungLuong = -1;

        public Duong() {
            setPreferredSize(new Dimension(s(420), s(190)));
            final MouseAdapter m = new MouseAdapter() {
                @Override
                public void mouseMoved(final MouseEvent e) {
                    chuot = e.getX();
                    repaint();
                }

                @Override
                public void mouseExited(final MouseEvent e) {
                    chuot = -1;
                    repaint();
                }
            };
            addMouseListener(m);
            addMouseMotionListener(m);
        }

        public Duong them(final String t, final Chuoi c, final Color m) {
            ten.add(t);
            chuoi.add(c);
            mau.add(m);
            return this;
        }

        /** So diem toi da cua truc X (vd 180 diem = 30 phut): diem moi luon nam ben phai, chua du thi de trong ben trai. */
        public Duong dungLuong(final int n) {
            dungLuong = n;
            return this;
        }

        public Duong donVi(final String d) {
            donVi = d;
            return this;
        }

        public Duong nhanTruc(final String trai, final String phai) {
            nhanTrai = trai;
            nhanPhai = phai;
            return this;
        }

        /** Tran truc Y co dinh (vd 100 cho %), -1 = tu tinh. */
        public Duong tran(final double t) {
            tranCoDinh = t;
            return this;
        }

        public void xoaChuoi() {
            chuoi.clear();
            mau.clear();
            ten.clear();
        }

        @Override
        protected void paintComponent(final Graphics g0) {
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            final int w = getWidth(), h = getHeight();
            final int trai = s(44), phai = s(10), tren = s(22), duoi = s(22);
            final int cw = w - trai - phai, ch = h - tren - duoi;
            if (cw < 20 || ch < 20) {
                g.dispose();
                return;
            }
            int n = 0;
            double max = 0;
            for (final Chuoi c : chuoi) {
                n = Math.max(n, c.soDiem());
                for (int i = 0; i < c.soDiem(); i++) {
                    max = Math.max(max, c.giaTri(i));
                }
            }
            if (tranCoDinh > 0) {
                max = Math.max(max, tranCoDinh);
            }
            final boolean coDiem = n >= 1;
            if (dungLuong > 1 && coDiem) {
                n = Math.max(n, dungLuong);
            }
            max = tronLen(max <= 0 ? 1 : max * 1.1);
            // chu giai
            g.setFont(GiaoDien.dam(11));
            int lx = trai;
            for (int k = 0; k < chuoi.size(); k++) {
                g.setColor(mau.get(k));
                g.fill(new RoundRectangle2D.Float(lx, s(6), s(10), s(10), s(4), s(4)));
                g.setColor(GiaoDien.chuPhu);
                g.drawString(ten.get(k), lx + s(15), s(15));
                lx += s(28) + g.getFontMetrics().stringWidth(ten.get(k));
            }
            // luoi
            g.setFont(GiaoDien.font(10.5f));
            final FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i <= 4; i++) {
                final float y = tren + ch * i / 4f;
                g.setColor(GiaoDien.vien);
                g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{s(3), s(4)}, 0));
                g.draw(new Line2D.Float(trai, y, trai + cw, y));
                g.setColor(GiaoDien.chuMo);
                final String nh = DinhDang.gon(max * (4 - i) / 4);
                g.drawString(nh, trai - s(6) - fm.stringWidth(nh), y + fm.getAscent() / 2f - 1);
            }
            g.setColor(GiaoDien.chuMo);
            g.drawString(nhanTrai, trai, h - s(5));
            g.drawString(nhanPhai, trai + cw - fm.stringWidth(nhanPhai), h - s(5));
            if (!coDiem) {
                g.setFont(GiaoDien.font(12));
                g.setColor(GiaoDien.chuMo);
                final String c = "Đang thu thập số liệu…";
                g.drawString(c, trai + (cw - g.getFontMetrics().stringWidth(c)) / 2, tren + ch / 2);
                g.dispose();
                return;
            }
            for (int k = 0; k < chuoi.size(); k++) {
                final Chuoi c = chuoi.get(k);
                final int m = c.soDiem();
                if (m < 1) {
                    continue;
                }
                final GeneralPath p = new GeneralPath();
                final float buoc = n > 1 ? cw / (float) (n - 1) : 0;
                final float x0 = n > 1 ? trai + cw - buoc * (m - 1) : trai + cw / 2f;
                for (int i = 0; i < m; i++) {
                    final float x = x0 + buoc * i;
                    final float y = (float) (tren + ch - c.giaTri(i) / max * ch);
                    if (i == 0) {
                        p.moveTo(x, y);
                    } else {
                        p.lineTo(x, y);
                    }
                }
                final GeneralPath vung = new GeneralPath(p);
                vung.lineTo(trai + cw, tren + ch);
                vung.lineTo(x0, tren + ch);
                vung.closePath();
                final Color mc = mau.get(k);
                if (m >= 2) {
                    g.setPaint(new GradientPaint(0, tren, GiaoDien.trong(mc, k == 0 ? 80 : 40), 0, tren + ch, GiaoDien.trong(mc, 0)));
                    g.fill(vung);
                }
                g.setColor(mc);
                g.setStroke(new BasicStroke(GiaoDien.sf(2f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(p);
                // it diem: hien tung diem (khong de bieu do trong / mot net thang)
                if (m <= 40) {
                    for (int i = 0; i < m; i++) {
                        final float x = x0 + buoc * i;
                        final float y = (float) (tren + ch - c.giaTri(i) / max * ch);
                        g.setColor(GiaoDien.nenThe);
                        g.fill(new Ellipse2D.Float(x - s(4), y - s(4), s(8), s(8)));
                        g.setColor(mc);
                        g.fill(new Ellipse2D.Float(x - GiaoDien.sf(2.5f), y - GiaoDien.sf(2.5f), s(5), s(5)));
                    }
                }
                final float yc = (float) (tren + ch - c.giaTri(m - 1) / max * ch);
                final float xc = x0 + buoc * (m - 1);
                g.setColor(GiaoDien.nenThe);
                g.fill(new Ellipse2D.Float(xc - s(5), yc - s(5), s(10), s(10)));
                g.setColor(mc);
                g.fill(new Ellipse2D.Float(xc - s(3), yc - s(3), s(6), s(6)));
            }
            // ro chuot
            if (chuot >= trai && chuot <= trai + cw) {
                final int i = n > 1 ? Math.round((chuot - trai) / (float) cw * (n - 1)) : 0;
                final float x = n > 1 ? trai + cw * i / (float) (n - 1) : trai + cw / 2f;
                g.setColor(GiaoDien.vienDam);
                g.setStroke(new BasicStroke(1f));
                g.draw(new Line2D.Float(x, tren, x, tren + ch));
                final StringBuilder b = new StringBuilder();
                for (int k = 0; k < chuoi.size(); k++) {
                    final Chuoi c = chuoi.get(k);
                    final int j = i - (n - c.soDiem());
                    if (j >= 0 && j < c.soDiem()) {
                        if (b.length() > 0) {
                            b.append("   ");
                        }
                        b.append(ten.get(k)).append(": ").append(DinhDang.gon(c.giaTri(j))).append(donVi);
                    }
                }
                final String tt = b.toString();
                g.setFont(GiaoDien.dam(11));
                final int tw = g.getFontMetrics().stringWidth(tt) + s(16);
                final float tx = Math.min(Math.max(trai, x - tw / 2f), trai + cw - tw);
                g.setColor(GiaoDien.toi ? new Color(0x2A3248) : new Color(0x1D2333));
                g.fill(new RoundRectangle2D.Float(tx, tren - s(2), tw, s(22), s(8), s(8)));
                g.setColor(Color.WHITE);
                g.drawString(tt, tx + s(8), tren + s(13));
            }
            g.dispose();
        }
    }

    /** Lam tron len so "dep": 1, 2, 2.5, 5 x 10^k. */
    public static double tronLen(final double v) {
        if (v <= 0) {
            return 1;
        }
        final double mu = Math.pow(10, Math.floor(Math.log10(v)));
        final double r = v / mu;
        final double dep = r <= 1 ? 1 : r <= 2 ? 2 : r <= 2.5 ? 2.5 : r <= 5 ? 5 : 10;
        return dep * mu;
    }

    // ================================================================== bieu do cot

    /** Cot ngang: nhan + gia tri, mau rieng tung cot (nguon vao xanh, ho hut do). */
    public static class Cot extends JComponent {
        private String[] nhan = new String[0];
        private double[] gt = new double[0];
        private Color[] mau = new Color[0];

        public Cot() {
            setPreferredSize(new Dimension(s(380), s(170)));
        }

        public void dat(final String[] n, final double[] v, final Color[] m) {
            nhan = n;
            gt = v;
            mau = m;
            repaint();
        }

        @Override
        protected void paintComponent(final Graphics g0) {
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            final int w = getWidth(), h = getHeight();
            if (nhan.length == 0) {
                g.setFont(GiaoDien.font(12));
                g.setColor(GiaoDien.chuMo);
                final String c = "Chưa có số liệu";
                g.drawString(c, (w - g.getFontMetrics().stringWidth(c)) / 2, h / 2);
                g.dispose();
                return;
            }
            double max = 0;
            for (final double v : gt) {
                max = Math.max(max, Math.abs(v));
            }
            if (max <= 0) {
                max = 1;
            }
            g.setFont(GiaoDien.font(11.5f));
            int rongNhan = 0;
            for (final String s0 : nhan) {
                rongNhan = Math.max(rongNhan, g.getFontMetrics().stringWidth(s0));
            }
            rongNhan += s(10);
            final int hang = Math.min(s(54), (h - s(6)) / nhan.length);
            final int rongCot = w - rongNhan - s(90);
            for (int i = 0; i < nhan.length; i++) {
                final int y = s(3) + i * hang;
                g.setColor(GiaoDien.chuPhu);
                g.setFont(GiaoDien.font(11.5f));
                g.drawString(nhan[i], 0, y + hang / 2 + s(4));
                g.setColor(GiaoDien.nenThe2);
                g.fill(new RoundRectangle2D.Float(rongNhan, y + hang * 0.22f, rongCot, hang * 0.56f, s(10), s(10)));
                final float d = (float) (Math.abs(gt[i]) / max * rongCot);
                g.setPaint(new GradientPaint(rongNhan, 0, GiaoDien.pha(mau[i], Color.WHITE, 0.15f), rongNhan + d, 0, mau[i]));
                g.fill(new RoundRectangle2D.Float(rongNhan, y + hang * 0.22f, Math.max(s(6), d), hang * 0.56f, s(10), s(10)));
                g.setColor(GiaoDien.chu);
                g.setFont(GiaoDien.dam(11.5f));
                g.drawString(DinhDang.gon(gt[i]), rongNhan + rongCot + s(8), y + hang / 2 + s(4));
            }
            g.dispose();
        }
    }

    // ================================================================== lich 52 tuan

    /** Luoi 53 cot x 7 dong (kieu lich dong gop): moi o = mot ngay, dam = so su kien trong ngay. */
    public static class Lich extends JComponent {
        /** ngay trong nam (0..365) -> so viec. */
        private final int[] dem = new int[367];
        private final String[] ghiChu = new String[367];
        private int nam = Calendar.getInstance().get(Calendar.YEAR);
        private int chuot = -1;

        public Lich() {
            setPreferredSize(new Dimension(s(760), s(150)));
            final MouseAdapter m = new MouseAdapter() {
                @Override
                public void mouseMoved(final MouseEvent e) {
                    chuot = oTai(e.getX(), e.getY());
                    setToolTipText(chuot >= 0 ? moTa(chuot) : null);
                    repaint();
                }

                @Override
                public void mouseExited(final MouseEvent e) {
                    chuot = -1;
                    repaint();
                }
            };
            addMouseListener(m);
            addMouseMotionListener(m);
        }

        public synchronized void xoa() {
            java.util.Arrays.fill(dem, 0);
            java.util.Arrays.fill(ghiChu, null);
        }

        /** Them mot moc vao ngay (ms). Ngoai nam hien tai -> bo qua. */
        public synchronized void them(final long ms, final String chu) {
            final Calendar c = Calendar.getInstance();
            c.setTimeInMillis(ms);
            if (c.get(Calendar.YEAR) != nam) {
                return;
            }
            final int d = c.get(Calendar.DAY_OF_YEAR);
            dem[d]++;
            ghiChu[d] = ghiChu[d] == null ? chu : ghiChu[d] + " · " + chu;
        }

        synchronized String moTa(final int ngayNam) {
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, nam);
            c.set(Calendar.DAY_OF_YEAR, ngayNam);
            final String d = new java.text.SimpleDateFormat("EEEE dd/MM", new java.util.Locale("vi")).format(c.getTime());
            return ghiChu[ngayNam] == null ? d : d + ": " + ghiChu[ngayNam];
        }

        int[] hinh() {
            final int nhanTrai = s(30);
            final int o = Math.max(s(8), Math.min(s(22), (getWidth() - nhanTrai - s(4)) / 53 - s(3)));
            return new int[]{nhanTrai, s(20), o, s(3)};

        }

        int oTai(final int x, final int y) {
            final int[] h = hinh();
            final int buoc = h[2] + h[3];
            final int cot = (x - h[0]) / buoc, dong = (y - h[1]) / buoc;
            if (x < h[0] || y < h[1] || cot > 52 || dong > 6) {
                return -1;
            }
            final Calendar c = Calendar.getInstance();
            c.set(nam, Calendar.JANUARY, 1);
            final int lech = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7; // thu Hai = 0
            final int d = cot * 7 + dong - lech + 1;
            return d >= 1 && d <= c.getActualMaximum(Calendar.DAY_OF_YEAR) ? d : -1;
        }

        @Override
        protected synchronized void paintComponent(final Graphics g0) {
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            final int[] h = hinh();
            final int o = h[2], k = h[3], buoc = o + k;
            final Calendar c = Calendar.getInstance();
            final int homNay = c.get(Calendar.YEAR) == nam ? c.get(Calendar.DAY_OF_YEAR) : -1;
            c.set(nam, Calendar.JANUARY, 1);
            final int lech = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            final int soNgay = c.getActualMaximum(Calendar.DAY_OF_YEAR);
            g.setFont(GiaoDien.font(10));
            final String[] thu = {"T2", "", "T4", "", "T6", "", "CN"};
            g.setColor(GiaoDien.chuMo);
            for (int i = 0; i < 7; i++) {
                g.drawString(thu[i], 0, h[1] + i * buoc + o - s(1));
            }
            int thangCu = -1;
            for (int d = 1; d <= soNgay; d++) {
                final int vt = d - 1 + lech;
                final int cot = vt / 7, dong = vt % 7;
                final int x = h[0] + cot * buoc, y = h[1] + dong * buoc;
                c.set(Calendar.DAY_OF_YEAR, d);
                final int thang = c.get(Calendar.MONTH);
                if (thang != thangCu && dong == 0) {
                    thangCu = thang;
                    g.setColor(GiaoDien.chuMo);
                    g.drawString("Th" + (thang + 1), x, h[1] - s(6));
                }
                final int n = dem[d];
                Color m;
                if (n <= 0) {
                    m = GiaoDien.nenThe2;
                } else {
                    m = GiaoDien.pha(GiaoDien.XANH_LA_NHAT, GiaoDien.XANH_LA_DAM, Math.min(1f, (n - 1) / 3f));
                }
                if (d < homNay) {
                    m = GiaoDien.pha(m, GiaoDien.nenThe2, n <= 0 ? 0.6f : 0.55f);
                }
                g.setColor(m);
                g.fill(new RoundRectangle2D.Float(x, y, o, o, s(4), s(4)));
                if (d == homNay) {
                    g.setColor(GiaoDien.DO);
                    g.setStroke(new BasicStroke(GiaoDien.sf(2f)));
                    g.draw(new RoundRectangle2D.Float(x - 1, y - 1, o + 2, o + 2, s(5), s(5)));
                } else if (d == chuot) {
                    g.setColor(GiaoDien.XANH_DUONG);
                    g.setStroke(new BasicStroke(GiaoDien.sf(1.5f)));
                    g.draw(new RoundRectangle2D.Float(x - 1, y - 1, o + 2, o + 2, s(5), s(5)));
                }
            }
            g.dispose();
        }
    }
}
