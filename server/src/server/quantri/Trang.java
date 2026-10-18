package server.quantri;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;

import static server.quantri.GiaoDien.s;

/** Mot trang cua bang dieu khien: tieu de + noi dung cuon. Lop con chi cap nhat khi dang hien. */
public abstract class Trang extends JPanel {

    protected final JPanel thanh;
    private final JLabel tieuDe;
    private final JLabel moTa;
    protected final JPanel benPhai;
    private boolean dangHien;

    protected Trang(final String ten, final String moTaTrang) {
        super(new BorderLayout());
        setOpaque(false);
        final JPanel dau = new JPanel(new BorderLayout(s(12), 0));
        dau.setOpaque(false);
        dau.setBorder(new EmptyBorder(s(18), s(24), s(8), s(24)));
        final JPanel chu = new JPanel(new BorderLayout(0, s(2)));
        chu.setOpaque(false);
        tieuDe = ThanhPhan.tieuDe(ten, 22);
        moTa = ThanhPhan.nhanPhu(moTaTrang);
        chu.add(tieuDe, BorderLayout.NORTH);
        chu.add(moTa, BorderLayout.CENTER);
        dau.add(chu, BorderLayout.CENTER);
        benPhai = ThanhPhan.hang(java.awt.FlowLayout.RIGHT);
        dau.add(benPhai, BorderLayout.EAST);
        add(dau, BorderLayout.NORTH);
        thanh = new PanelCuon(new BorderLayout());
        thanh.setOpaque(false);
        thanh.setBorder(new EmptyBorder(s(6), s(24), s(24), s(24)));
        final JScrollPane sp = ThanhPhan.cuon(thanh);
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);
        add(sp, BorderLayout.CENTER);
        // Doan van tu xuong dong tinh chieu cao theo be rong hien tai: doi kich thuoc -> bo cuc lai mot lan.
        thanh.addComponentListener(new java.awt.event.ComponentAdapter() {
            private int rongCu = -1;

            @Override
            public void componentResized(final java.awt.event.ComponentEvent e) {
                if (thanh.getWidth() != rongCu) {
                    rongCu = thanh.getWidth();
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        thanh.revalidate();
                        thanh.repaint();
                    });
                }
            }
        });
    }

    /** Dua khung cuon ve dau trang (khi chuyen trang). */
    public void lenDau() {
        final java.awt.Container v = thanh.getParent();
        if (v instanceof javax.swing.JViewport) {
            ((javax.swing.JViewport) v).setViewPosition(new java.awt.Point(0, 0));
        }
    }

    /** Ten ngan (thanh ben). */
    public abstract String ten();

    /** Ten icon vat pham PMD cho thanh ben. */
    public abstract String icon();

    /** Goi khi trang duoc chon (EDT) - tai du lieu nang (ngoai EDT qua HopThoai.doc). */
    public void khiHien() {
    }

    /** Moi giay (EDT) KHI trang dang hien. */
    public void nhip(final DuLieuTrucTiep.Mau m) {
    }

    /** Bang dieu khien danh dau trang dang hien (chi trang hien moi nhan nhip). */
    public static void datHien(final Trang t, final boolean h) {
        t.dangHien = h;
    }

    public final boolean dangHien() {
        return dangHien;
    }

    /** Noi dung cuon doc nhung khop chieu ngang khung nhin. */
    static class PanelCuon extends JPanel implements Scrollable {
        PanelCuon(final LayoutManager lm) {
            super(lm);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(final Rectangle r, final int o, final int d) {
            return s(20);
        }

        @Override
        public int getScrollableBlockIncrement(final Rectangle r, final int o, final int d) {
            return r.height - s(40);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() != null && getParent().getHeight() > getPreferredSize().height;
        }
    }

    /** Danh sach doc (moi dong cao theo noi dung, khong keo gian nhu GridLayout). */
    public static JPanel danhSach() {
        final JPanel p = new JPanel(new CotLayout(s(8)));
        p.setOpaque(false);
        return p;
    }

    /** Cot doc cac khoi, cach deu. */
    public static JPanel cot(final Component... cs) {
        final JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new CotLayout(s(16)));
        for (final Component c : cs) {
            p.add(c);
        }
        return p;
    }

    /** Luoi tu chia cot theo be rong (the toi thieu rongToiThieu). */
    public static JPanel luoi(final int rongToiThieu, final Component... cs) {
        final JPanel p = new JPanel(new LuoiTuDong(rongToiThieu, s(14)));
        p.setOpaque(false);
        for (final Component c : cs) {
            p.add(c);
        }
        return p;
    }

    /** Hang ngang chia ti le (vd 3:2). */
    public static JPanel tiLe(final double[] phan, final Component... cs) {
        final JPanel p = new JPanel(new TiLeLayout(phan, s(16)));
        p.setOpaque(false);
        for (final Component c : cs) {
            p.add(c);
        }
        return p;
    }

    // ================================================================== bo cuc

    /** Xep doc, moi con keo het chieu ngang, cao theo preferred. */
    static final class CotLayout implements LayoutManager {
        private final int khe;

        CotLayout(final int khe) {
            this.khe = khe;
        }

        @Override
        public void addLayoutComponent(final String n, final Component c) {
        }

        @Override
        public void removeLayoutComponent(final Component c) {
        }

        @Override
        public Dimension preferredLayoutSize(final Container p) {
            final Insets in = p.getInsets();
            int h = 0, w = 0, n = 0;
            for (final Component c : p.getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                final Dimension d = c.getPreferredSize();
                h += d.height;
                w = Math.max(w, d.width);
                n++;
            }
            return new Dimension(w + in.left + in.right, h + Math.max(0, n - 1) * khe + in.top + in.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(final Container p) {
            return new Dimension(s(200), preferredLayoutSize(p).height);
        }

        @Override
        public void layoutContainer(final Container p) {
            final Insets in = p.getInsets();
            int y = in.top;
            final int w = p.getWidth() - in.left - in.right;
            for (final Component c : p.getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                final int h = c.getPreferredSize().height;
                c.setBounds(in.left, y, w, h);
                y += h + khe;
            }
        }
    }

    /** Luoi: so cot = be rong / rongToiThieu; cac o cung chieu cao trong mot hang. */
    static final class LuoiTuDong implements LayoutManager {
        private final int rongMin, khe;

        LuoiTuDong(final int rongMin, final int khe) {
            this.rongMin = rongMin;
            this.khe = khe;
        }

        int soCot(final int w, final int n) {
            final int toiDa = Math.max(1, Math.min(n, (w + khe) / (rongMin + khe)));
            // uu tien so cot chia het so the (khong de "lo" o hang cuoi), lui toi da 2 cot
            for (int c = toiDa; c >= Math.max(1, toiDa - 2); c--) {
                if (n % c == 0) {
                    return c;
                }
            }
            return toiDa;
        }

        @Override
        public void addLayoutComponent(final String n, final Component c) {
        }

        @Override
        public void removeLayoutComponent(final Component c) {
        }

        @Override
        public Dimension preferredLayoutSize(final Container p) {
            final int n = p.getComponentCount();
            int w = p.getWidth();
            if (w <= 0) {
                w = rongMin * Math.min(n, 4) + khe * 3;
            }
            final int cot = soCot(w, n);
            int h = 0;
            for (int i = 0; i < n; i += cot) {
                int hh = 0;
                for (int j = i; j < Math.min(n, i + cot); j++) {
                    hh = Math.max(hh, p.getComponent(j).getPreferredSize().height);
                }
                h += hh + (i > 0 ? khe : 0);
            }
            final Insets in = p.getInsets();
            return new Dimension(Math.min(w, rongMin * cot + khe * (cot - 1)) + in.left + in.right, h + in.top + in.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(final Container p) {
            return new Dimension(rongMin, s(60));
        }

        @Override
        public void layoutContainer(final Container p) {
            final Insets in = p.getInsets();
            final int n = p.getComponentCount();
            final int w = p.getWidth() - in.left - in.right;
            final int cot = soCot(w, n);
            final int rong = (w - khe * (cot - 1)) / cot;
            int y = in.top;
            for (int i = 0; i < n; i += cot) {
                int hh = 0;
                for (int j = i; j < Math.min(n, i + cot); j++) {
                    hh = Math.max(hh, p.getComponent(j).getPreferredSize().height);
                }
                for (int j = i; j < Math.min(n, i + cot); j++) {
                    p.getComponent(j).setBounds(in.left + (j - i) * (rong + khe), y, rong, hh);
                }
                y += hh + khe;
            }
        }
    }

    /** Hang ngang theo ti le; hep qua (&lt; 760) thi xep doc. */
    static final class TiLeLayout implements LayoutManager {
        private final double[] phan;
        private final int khe;

        TiLeLayout(final double[] phan, final int khe) {
            this.phan = phan;
            this.khe = khe;
        }

        boolean doc(final Container p) {
            return p.getWidth() > 0 && p.getWidth() < s(760);
        }

        @Override
        public void addLayoutComponent(final String n, final Component c) {
        }

        @Override
        public void removeLayoutComponent(final Component c) {
        }

        @Override
        public Dimension preferredLayoutSize(final Container p) {
            int h = 0, tong = 0, w = 0;
            for (final Component c : p.getComponents()) {
                final Dimension d = c.getPreferredSize();
                h = Math.max(h, d.height);
                tong += d.height + khe;
                w += d.width + khe;
            }
            final Insets in = p.getInsets();
            return doc(p) ? new Dimension(w, tong - khe + in.top + in.bottom) : new Dimension(w, h + in.top + in.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(final Container p) {
            return new Dimension(s(200), s(80));
        }

        @Override
        public void layoutContainer(final Container p) {
            final Insets in = p.getInsets();
            final int w = p.getWidth() - in.left - in.right;
            final int n = p.getComponentCount();
            if (doc(p)) {
                int y = in.top;
                for (final Component c : p.getComponents()) {
                    final int h = c.getPreferredSize().height;
                    c.setBounds(in.left, y, w, h);
                    y += h + khe;
                }
                return;
            }
            double tong = 0;
            for (int i = 0; i < n; i++) {
                tong += i < phan.length ? phan[i] : 1;
            }
            final int con = w - khe * (n - 1);
            int x = in.left;
            final int h = p.getHeight() - in.top - in.bottom;
            for (int i = 0; i < n; i++) {
                final int cw = i == n - 1 ? in.left + w - x : (int) Math.round(con * (i < phan.length ? phan[i] : 1) / tong);
                p.getComponent(i).setBounds(x, in.top, cw, h);
                x += cw + khe;
            }
        }
    }

    /** Dat chieu cao uu tien cho mot thanh phan (vd bang trong the). */
    public static <T extends JComponent> T cao(final T c, final int h) {
        c.setPreferredSize(new Dimension(Math.max(s(100), c.getPreferredSize().width), s(h)));
        return c;
    }
}
