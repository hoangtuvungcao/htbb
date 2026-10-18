package server.quantri;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import static server.quantri.GiaoDien.s;

/** Thanh phan tu ve theo phong cach Pokemon: nut, the, the chi so, huy hieu, cong tac, o nhap. */
public final class ThanhPhan {

    private ThanhPhan() {
    }

    // ================================================================== NUT

    public enum Kieu { DO, XANH, VANG, LA, MO, NGUY, TIM }

    /** Nut bo tron tu ve, co hieu ung di chuot / bam. */
    public static class Nut extends JButton {
        private Kieu kieu;
        private boolean tren;
        private boolean nhan;
        private boolean nho;

        public Nut(final String chu, final Kieu kieu) {
            this(chu, kieu, null);
        }

        public Nut(final String chu, final Kieu kieu, final Icon icon) {
            super(chu, icon);
            this.kieu = kieu;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(GiaoDien.dam(12.5f));
            setIconTextGap(s(7));
            setMargin(new Insets(s(7), s(14), s(7), s(14)));
            setBorder(new EmptyBorder(s(7), s(14), s(7), s(14)));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(final MouseEvent e) {
                    tren = true;
                    repaint();
                }

                @Override
                public void mouseExited(final MouseEvent e) {
                    tren = false;
                    nhan = false;
                    repaint();
                }

                @Override
                public void mousePressed(final MouseEvent e) {
                    nhan = true;
                    repaint();
                }

                @Override
                public void mouseReleased(final MouseEvent e) {
                    nhan = false;
                    repaint();
                }
            });
        }

        public Nut nho() {
            nho = true;
            setFont(GiaoDien.dam(11.5f));
            setBorder(new EmptyBorder(s(4), s(10), s(4), s(10)));
            return this;
        }

        public void datKieu(final Kieu k) {
            this.kieu = k;
            repaint();
        }

        public Kieu kieu() {
            return kieu;
        }

        Color mauNen() {
            switch (kieu) {
                case DO:
                    return GiaoDien.DO;
                case XANH:
                    return GiaoDien.XANH_DUONG;
                case VANG:
                    return GiaoDien.VANG;
                case LA:
                    return GiaoDien.XANH_LA_DAM;
                case TIM:
                    return GiaoDien.TIM;
                case NGUY:
                    return GiaoDien.DO_DAM;
                default:
                    return GiaoDien.nenThe2;
            }
        }

        @Override
        public Dimension getPreferredSize() {
            final Dimension d = super.getPreferredSize();
            d.height = Math.max(d.height, s(nho ? 26 : 34));
            return d;
        }

        @Override
        protected void paintComponent(final Graphics g0) {
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            final int w = getWidth(), h = getHeight();
            final float bk = s(nho ? 8 : 10);
            Color c = mauNen();
            if (!isEnabled()) {
                c = GiaoDien.pha(c, GiaoDien.nen, 0.55f);
            } else if (nhan) {
                c = GiaoDien.pha(c, Color.BLACK, 0.18f);
            } else if (tren) {
                c = kieu == Kieu.MO ? GiaoDien.nenHover : GiaoDien.pha(c, Color.WHITE, 0.12f);
            }
            // bong nho duoi nut (noi khoi)
            if (kieu != Kieu.MO && isEnabled() && !nhan) {
                g.setColor(GiaoDien.trong(GiaoDien.pha(c, Color.BLACK, 0.4f), 60));
                g.fill(new RoundRectangle2D.Float(0, s(2), w, h - s(2), bk, bk));
            }
            g.setPaint(new GradientPaint(0, 0, GiaoDien.pha(c, Color.WHITE, kieu == Kieu.MO ? 0f : 0.08f), 0, h,
                    GiaoDien.pha(c, Color.BLACK, kieu == Kieu.MO ? 0f : 0.06f)));
            g.fill(new RoundRectangle2D.Float(0, nhan ? s(1) : 0, w, h - s(2), bk, bk));
            if (kieu == Kieu.MO || kieu == Kieu.NGUY) {
                g.setColor(kieu == Kieu.NGUY ? GiaoDien.pha(c, Color.WHITE, 0.25f) : GiaoDien.vienDam);
                g.setStroke(new BasicStroke(GiaoDien.sf(1f)));
                g.draw(new RoundRectangle2D.Float(0.5f, (nhan ? s(1) : 0) + 0.5f, w - 1, h - s(2) - 1, bk, bk));
            }
            g.dispose();
            setForeground(kieu == Kieu.MO ? GiaoDien.chu : GiaoDien.chuTren(mauNen()));
            super.paintComponent(g0);
        }
    }

    // ================================================================== THE (card)

    /** The bo tron co bong, tieu de + icon + phan ben phai tuy chon. */
    public static class The extends JPanel {
        private final JLabel nhanTieuDe;
        private final JPanel dau;
        private Color nhan;

        public The(final String tieuDe) {
            this(tieuDe, null);
        }

        public The(final String tieuDe, final Icon icon) {
            super(new BorderLayout(0, s(8)));
            setOpaque(false);
            setBorder(new EmptyBorder(s(14), s(16), s(14), s(16)));
            dau = new JPanel(new BorderLayout(s(8), 0));
            dau.setOpaque(false);
            nhanTieuDe = new JLabel(tieuDe, icon, SwingConstants.LEADING);
            nhanTieuDe.setFont(GiaoDien.dam(14));
            nhanTieuDe.setIconTextGap(s(8));
            dau.add(nhanTieuDe, BorderLayout.WEST);
            if (tieuDe != null) {
                super.add(dau, BorderLayout.NORTH);
            }
        }

        /** Gach mau tren dinh the (nhan nhom). */
        public The nhan(final Color c) {
            this.nhan = c;
            return this;
        }

        public void datTieuDe(final String t) {
            nhanTieuDe.setText(t);
        }

        public void datBenPhai(final Component c) {
            dau.add(c, BorderLayout.EAST);
        }

        public void noiDung(final Component c) {
            super.add(c, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(final Graphics g0) {
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            final int w = getWidth(), h = getHeight();
            final float bk = s(16);
            for (int i = 3; i >= 1; i--) {
                g.setColor(GiaoDien.trong(GiaoDien.bong, GiaoDien.bong.getAlpha() / i));
                g.fill(new RoundRectangle2D.Float(s(1) - i, s(2) + i - 1, w - s(2) + 2 * i, h - s(3), bk + i, bk + i));
            }
            g.setColor(GiaoDien.nenThe);
            g.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - s(3), bk, bk));
            g.setColor(GiaoDien.vien);
            g.setStroke(new BasicStroke(1f));
            g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 2, h - s(3) - 1, bk, bk));
            if (nhan != null) {
                g.setClip(new RoundRectangle2D.Float(0, 0, w - 1, h - s(3), bk, bk));
                g.setColor(nhan);
                g.fillRect(0, 0, w, s(4));
            }
            g.dispose();
            nhanTieuDe.setForeground(GiaoDien.chu);
            super.paintComponent(g0);
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    // ================================================================== THE CHI SO

    /** The so lieu song: nhan, so lon, dong phu, mau trang thai, duong nho (sparkline). Tu ve, khong con. */
    public static class TheChiSo extends JComponent {
        private final String tieuDe;
        private final Color mau;
        private final Image icon;
        private String giaTri = "—";
        private String phu = "";
        private int trangThai = GiaoDien.TRUNG_TINH;
        private BieuDo.Chuoi chuoi;

        public TheChiSo(final String tieuDe, final Color mau, final Image icon) {
            this.tieuDe = tieuDe;
            this.mau = mau;
            this.icon = icon;
            setPreferredSize(new Dimension(s(200), s(122)));
            setMinimumSize(new Dimension(s(150), s(118)));
        }

        public TheChiSo duong(final BieuDo.Chuoi c) {
            this.chuoi = c;
            return this;
        }

        /** Cap nhat - chi repaint khi doi (khong tao rac moi giay). */
        public void capNhat(final String gt, final String p, final int tt) {
            if (gt.equals(giaTri) && p.equals(phu) && tt == trangThai && chuoi == null) {
                return;
            }
            giaTri = gt;
            phu = p;
            trangThai = tt;
            repaint();
        }

        public String giaTri() {
            return giaTri;
        }

        @Override
        protected void paintComponent(final Graphics g0) {
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            final int w = getWidth(), h = getHeight();
            final float bk = s(16);
            for (int i = 3; i >= 1; i--) {
                g.setColor(GiaoDien.trong(GiaoDien.bong, GiaoDien.bong.getAlpha() / i));
                g.fill(new RoundRectangle2D.Float(s(1) - i, s(2) + i - 1, w - s(2) + 2 * i, h - s(3), bk + i, bk + i));
            }
            final RoundRectangle2D.Float khung = new RoundRectangle2D.Float(0, 0, w - 1, h - s(3), bk, bk);
            g.setColor(GiaoDien.nenThe);
            g.fill(khung);
            // sparkline o day the
            if (chuoi != null && chuoi.soDiem() > 1) {
                final java.awt.Shape cu = g.getClip();
                g.clip(khung);
                BieuDo.veDuongNho(g, chuoi, mau, 0, h - s(30), w - 1, s(27));
                g.setClip(cu);
            }
            g.setColor(GiaoDien.vien);
            g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 2, h - s(3) - 1, bk, bk));
            // vong tron icon
            final int r = s(30);
            final int x0 = s(14), y0 = s(13);
            g.setColor(GiaoDien.trong(mau, GiaoDien.toi ? 60 : 36));
            g.fill(new Ellipse2D.Float(x0, y0, r, r));
            if (icon != null) {
                final int is = s(22);
                g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                        java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(icon, x0 + (r - is) / 2, y0 + (r - is) / 2, is, is, null);
            }
            g.setFont(GiaoDien.dam(11.5f));
            g.setColor(GiaoDien.chuPhu);
            g.drawString(tieuDe.toUpperCase(), x0 + r + s(9), y0 + s(19));
            // cham trang thai
            final Color ct = GiaoDien.mauTrangThai(trangThai);
            g.setColor(GiaoDien.trong(ct, 60));
            g.fill(new Ellipse2D.Float(w - s(24), y0 + s(8), s(12), s(12)));
            g.setColor(ct);
            g.fill(new Ellipse2D.Float(w - s(21), y0 + s(11), s(6), s(6)));
            // so lon
            g.setFont(GiaoDien.dam(24));
            g.setColor(GiaoDien.chu);
            g.drawString(catChu(g, giaTri, w - s(28)), x0, y0 + r + s(30));
            g.setFont(GiaoDien.font(11.5f));
            g.setColor(GiaoDien.chuPhu);
            g.drawString(catChu(g, phu, w - s(28)), x0, y0 + r + s(48));
            g.dispose();
        }
    }

    static String catChu(final Graphics2D g, final String s, final int rong) {
        if (s == null) {
            return "";
        }
        final FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(s) <= rong) {
            return s;
        }
        String t = s;
        while (t.length() > 1 && fm.stringWidth(t + "…") > rong) {
            t = t.substring(0, t.length() - 1);
        }
        return t + "…";
    }

    // ================================================================== HUY HIEU

    /** Nhan vien thuoc (pill) co mau. */
    public static class HuyHieu extends JLabel {
        private Color mau;

        public HuyHieu(final String chu, final Color mau) {
            super(chu);
            this.mau = mau;
            setFont(GiaoDien.dam(11));
            setBorder(new EmptyBorder(s(3), s(9), s(3), s(9)));
            setHorizontalAlignment(CENTER);
        }

        public void dat(final String chu, final Color m) {
            if (!chu.equals(getText()) || !m.equals(mau)) {
                setText(chu);
                mau = m;
                repaint();
            }
        }

        @Override
        protected void paintComponent(final Graphics g0) {
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            g.setColor(GiaoDien.trong(mau, GiaoDien.toi ? 70 : 40));
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            g.dispose();
            setForeground(GiaoDien.toi ? GiaoDien.pha(mau, Color.WHITE, 0.45f) : GiaoDien.pha(mau, Color.BLACK, 0.25f));
            super.paintComponent(g0);
        }
    }

    // ================================================================== CONG TAC

    /** Cong tac bat/tat kieu iOS. Bam -> goi nguoiNghe (khong tu doi trang thai: nguoi goi xac nhan roi dat). */
    public static class CongTac extends JComponent {
        private boolean bat;
        private ActionListener nghe;

        public CongTac(final boolean bat) {
            this.bat = bat;
            setPreferredSize(new Dimension(s(44), s(24)));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (isEnabled() && nghe != null) {
                        nghe.actionPerformed(new java.awt.event.ActionEvent(CongTac.this, 0, "doi"));
                    }
                }
            });
        }

        public void khiBam(final ActionListener l) {
            nghe = l;
        }

        public boolean bat() {
            return bat;
        }

        public void dat(final boolean b) {
            if (b != bat) {
                bat = b;
                repaint();
            }
        }

        @Override
        protected void paintComponent(final Graphics g0) {
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            final int w = s(44), h = s(24);
            final int y = (getHeight() - h) / 2;
            g.setColor(bat ? GiaoDien.XANH_LA : GiaoDien.vienDam);
            if (!isEnabled()) {
                g.setColor(GiaoDien.pha(g.getColor(), GiaoDien.nen, 0.5f));
            }
            g.fill(new RoundRectangle2D.Float(0, y, w, h, h, h));
            g.setColor(Color.WHITE);
            final int d = h - s(6);
            g.fill(new Ellipse2D.Float(bat ? w - d - s(3) : s(3), y + s(3), d, d));
            g.dispose();
        }
    }

    // ================================================================== O NHAP

    /** O nhap bo tron co chu goi y (placeholder). */
    public static class ONhap extends JTextField {
        private String goiY = "";

        public ONhap(final String text, final int cot) {
            super(text, cot);
            setFont(GiaoDien.font(13));
            setBorder(new VienTron(s(9), true));
            setOpaque(false);
        }

        public ONhap goiY(final String g) {
            goiY = g;
            return this;
        }

        @Override
        protected void paintComponent(final Graphics g0) {
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            g.setColor(isEditable() ? GiaoDien.nenO : GiaoDien.nenThe2);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, s(9), s(9)));
            g.dispose();
            setForeground(GiaoDien.chu);
            setCaretColor(GiaoDien.chu);
            super.paintComponent(g0);
            if (getText().isEmpty() && goiY != null && !goiY.isEmpty()) {
                final Graphics2D g2 = (Graphics2D) g0.create();
                GiaoDien.khuRang(g2);
                g2.setColor(GiaoDien.chuMo);
                g2.setFont(getFont());
                final Insets in = getInsets();
                g2.drawString(goiY, in.left, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - s(2));
                g2.dispose();
            }
        }

        @Override
        public Dimension getPreferredSize() {
            final Dimension d = super.getPreferredSize();
            d.height = Math.max(d.height, s(34));
            return d;
        }
    }

    /** Vien bo tron (dung cho o nhap, vung chu). */
    public static class VienTron extends AbstractBorder {
        private final int bk;
        private final boolean dem;

        public VienTron(final int bk, final boolean dem) {
            this.bk = bk;
            this.dem = dem;
        }

        @Override
        public void paintBorder(final Component c, final Graphics g0, final int x, final int y, final int w, final int h) {
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            g.setColor(c.hasFocus() ? GiaoDien.XANH_DUONG : GiaoDien.vienDam);
            g.setStroke(new BasicStroke(c.hasFocus() ? GiaoDien.sf(1.6f) : 1f));
            g.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1.5f, h - 1.5f, bk, bk));
            g.dispose();
        }

        @Override
        public Insets getBorderInsets(final Component c) {
            return dem ? new Insets(s(6), s(10), s(6), s(10)) : new Insets(2, 2, 2, 2);
        }

        @Override
        public Insets getBorderInsets(final Component c, final Insets in) {
            final Insets i = getBorderInsets(c);
            in.top = i.top;
            in.left = i.left;
            in.bottom = i.bottom;
            in.right = i.right;
            return in;
        }
    }

    // ================================================================== BANG

    /** Lam dep JTable: dong cao, so le, khong ke luoi, tieu de tu ve. */
    public static void kieuBang(final JTable t) {
        t.setRowHeight(s(30));
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setFont(GiaoDien.font(12.5f));
        t.setBackground(GiaoDien.nenThe);
        t.setForeground(GiaoDien.chu);
        t.setSelectionBackground(GiaoDien.chonDong);
        t.setSelectionForeground(GiaoDien.chu);
        t.setBorder(null);
        final JTableHeader h = t.getTableHeader();
        if (h != null) {
            h.setReorderingAllowed(false);
            h.setPreferredSize(new Dimension(10, s(32)));
            h.setDefaultRenderer(new TieuDeBang());
        }
        t.setDefaultRenderer(Object.class, new OBang());
        t.setDefaultRenderer(Number.class, new OBang());
        t.setDefaultRenderer(Integer.class, new OBang());
        t.setDefaultRenderer(Long.class, new OBang());
    }

    /** O bang mac dinh: so le, can phai cho so, dinh dang so co dau cham. */
    public static class OBang extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(final JTable t, final Object v, final boolean chon, final boolean focus,
                final int row, final int col) {
            Object hien = v;
            if (v instanceof Long || v instanceof Integer) {
                hien = DinhDang.so(((Number) v).longValue());
            }
            super.getTableCellRendererComponent(t, hien, chon, false, row, col);
            setBorder(new EmptyBorder(0, s(10), 0, s(10)));
            setFont(GiaoDien.font(12.5f));
            setHorizontalAlignment(v instanceof Number || laCotSo(t, col) ? RIGHT : LEFT);
            // chu bi cat -> hien day du khi re chuot
            final String chu = getText();
            if (chu != null && !chu.isEmpty() && getFontMetrics(getFont()).stringWidth(chu) > t.getColumnModel().getColumn(col).getWidth() - s(20)) {
                setToolTipText(chu);
            } else {
                setToolTipText(null);
            }
            if (chon) {
                setBackground(GiaoDien.chonDong);
            } else {
                setBackground(row % 2 == 0 ? GiaoDien.nenThe : GiaoDien.dongLe);
            }
            setForeground(GiaoDien.chu);
            return this;
        }
    }

    /** Cot so (can phai ca tieu de lan o): theo lop cot cua mo hinh, hoac gia tri dong dau la so. */
    static boolean laCotSo(final JTable t, final int cotHien) {
        try {
            final int m = t.convertColumnIndexToModel(cotHien);
            final Class<?> c = t.getModel().getColumnClass(m);
            if (Number.class.isAssignableFrom(c)) {
                return true;
            }
            if (c != Object.class || t.getModel().getRowCount() == 0) {
                return false;
            }
            return t.getModel().getValueAt(0, m) instanceof Number;
        } catch (final Throwable e) {
            return false;
        }
    }

    /** JTable cua bang dieu khien: hien cau "khong co du lieu" khi rong. */
    public static class Bang extends JTable {
        private String rong = "Không có dữ liệu.";

        public Bang(final javax.swing.table.TableModel m) {
            super(m);
            kieuBang(this);
        }

        /** Tong be rong uu tien cac cot: hep hon -> cuon ngang thay vi ep cot (chu bi cat). */
        @Override
        public boolean getScrollableTracksViewportWidth() {
            if (getAutoResizeMode() == AUTO_RESIZE_OFF || !(getParent() instanceof javax.swing.JViewport)) {
                return super.getScrollableTracksViewportWidth();
            }
            int tong = 0;
            for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
                tong += getColumnModel().getColumn(i).getPreferredWidth();
            }
            return getParent().getWidth() >= tong;
        }

        @Override
        public Dimension getPreferredSize() {
            final Dimension d = super.getPreferredSize();
            if (getParent() instanceof javax.swing.JViewport && !getScrollableTracksViewportWidth()) {
                int tong = 0;
                for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
                    tong += getColumnModel().getColumn(i).getPreferredWidth();
                }
                d.width = tong;
            }
            return d;
        }

        @Override
        public void doLayout() {
            if (getParent() instanceof javax.swing.JViewport && !getScrollableTracksViewportWidth()) {
                // hep: giu nguyen be rong uu tien tung cot
                for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
                    final javax.swing.table.TableColumn c = getColumnModel().getColumn(i);
                    c.setWidth(c.getPreferredWidth());
                }
                return;
            }
            super.doLayout();
        }

        public Bang rong(final String s0) {
            rong = s0;
            return this;
        }

        @Override
        protected void paintComponent(final Graphics g0) {
            super.paintComponent(g0);
            if (getRowCount() == 0 && rong != null) {
                final Graphics2D g = (Graphics2D) g0.create();
                GiaoDien.khuRang(g);
                g.setFont(GiaoDien.font(12.5f));
                g.setColor(GiaoDien.chuMo);
                final int w = g.getFontMetrics().stringWidth(rong);
                final java.awt.Rectangle r = getVisibleRect();
                g.drawString(rong, r.x + Math.max(s(10), (r.width - w) / 2), r.y + Math.max(s(30), r.height / 2));
                g.dispose();
            }
        }
    }

    static class TieuDeBang extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(final JTable t, final Object v, final boolean chon, final boolean focus,
                final int row, final int col) {
            super.getTableCellRendererComponent(t, v, false, false, row, col);
            setFont(GiaoDien.dam(11.5f));
            setForeground(GiaoDien.chuPhu);
            setBackground(GiaoDien.nenThe2);
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, GiaoDien.vien),
                    new EmptyBorder(0, s(10), 0, s(10))));
            String chu = v == null ? "" : v.toString();
            if (t.getRowSorter() != null && !t.getRowSorter().getSortKeys().isEmpty()
                    && t.convertColumnIndexToModel(col) == t.getRowSorter().getSortKeys().get(0).getColumn()) {
                chu += t.getRowSorter().getSortKeys().get(0).getSortOrder() == javax.swing.SortOrder.ASCENDING ? " ↑" : " ↓";
            }
            setText(chu.toUpperCase());
            setHorizontalAlignment(laCotSo(t, col) ? RIGHT : LEFT);
            return this;
        }
    }

    /** Cuon co vien mong, thanh cuon tron. */
    public static JScrollPane cuon(final Component c) {
        final JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(GiaoDien.nenThe);
        sp.setOpaque(false);
        sp.getVerticalScrollBar().setUI(new ThanhCuon());
        sp.getHorizontalScrollBar().setUI(new ThanhCuon());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(s(10), 10));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(10, s(10)));
        sp.getVerticalScrollBar().setUnitIncrement(s(18));
        sp.getVerticalScrollBar().setOpaque(false);
        sp.getHorizontalScrollBar().setOpaque(false);
        sp.setCorner(JScrollPane.UPPER_RIGHT_CORNER, null);
        return sp;
    }

    static class ThanhCuon extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = GiaoDien.vienDam;
        }

        @Override
        protected JButton createDecreaseButton(final int o) {
            return nutRong();
        }

        @Override
        protected JButton createIncreaseButton(final int o) {
            return nutRong();
        }

        private JButton nutRong() {
            final JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintTrack(final Graphics g, final JComponent c, final java.awt.Rectangle r) {
        }

        @Override
        protected void paintThumb(final Graphics g0, final JComponent c, final java.awt.Rectangle r) {
            if (r.isEmpty()) {
                return;
            }
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            g.setColor(isThumbRollover() ? GiaoDien.chuMo : GiaoDien.vienDam);
            final int p = s(2);
            g.fill(new RoundRectangle2D.Float(r.x + p, r.y + p, r.width - 2 * p, r.height - 2 * p, s(8), s(8)));
            g.dispose();
        }
    }

    // ================================================================== hop chon

    /** Giao dien hop chon phang: nen o nhap, vien tron, mui ten ve tay (khong kieu Metal). */
    public static class UIHopChon extends javax.swing.plaf.basic.BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            final JButton b = new JButton() {
                @Override
                protected void paintComponent(final Graphics g0) {
                    final Graphics2D g = (Graphics2D) g0.create();
                    GiaoDien.khuRang(g);
                    final int w = getWidth(), h = getHeight();
                    final java.awt.geom.Path2D.Float p = new java.awt.geom.Path2D.Float();
                    p.moveTo(w / 2f - s(4), h / 2f - s(2));
                    p.lineTo(w / 2f + s(4), h / 2f - s(2));
                    p.lineTo(w / 2f, h / 2f + s(3));
                    p.closePath();
                    g.setColor(GiaoDien.chuPhu);
                    g.fill(p);
                    g.dispose();
                }
            };
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setOpaque(false);
            return b;
        }

        @Override
        public void paintCurrentValueBackground(final Graphics g, final java.awt.Rectangle r, final boolean coFocus) {
            g.setColor(comboBox.isEnabled() ? GiaoDien.nenO : GiaoDien.nenThe2);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        public void paintCurrentValue(final Graphics g, final java.awt.Rectangle r, final boolean coFocus) {
            super.paintCurrentValue(g, r, false);
        }
    }

    /** Ap kieu hop chon (goi mot lan khi tao hoac khi to lai cay). */
    public static void kieuHopChon(final javax.swing.JComboBox<?> c) {
        if (!(c.getUI() instanceof UIHopChon)) {
            c.setUI(new UIHopChon());
        }
        c.setBorder(new VienTron(s(8), false));
        c.setBackground(GiaoDien.nenO);
        c.setForeground(GiaoDien.chu);
        c.setFont(GiaoDien.font(13));
        c.setOpaque(false);
        final javax.swing.ListCellRenderer<? super Object> goc = new javax.swing.DefaultListCellRenderer();
        @SuppressWarnings("unchecked")
        final javax.swing.JComboBox<Object> co = (javax.swing.JComboBox<Object>) c;
        co.setRenderer(new javax.swing.ListCellRenderer<Object>() {
            @Override
            public Component getListCellRendererComponent(final javax.swing.JList<? extends Object> l, final Object v, final int i,
                    final boolean chon, final boolean f) {
                final JLabel x = (JLabel) goc.getListCellRendererComponent(l, v, i, chon, false);
                x.setBorder(new EmptyBorder(s(4), s(8), s(4), s(8)));
                x.setFont(GiaoDien.font(13));
                x.setBackground(chon && i >= 0 ? GiaoDien.chonDong : GiaoDien.nenO);
                x.setForeground(GiaoDien.chu);
                return x;
            }
        });
        if (c.getPreferredSize().height < s(32)) {
            c.setPreferredSize(new Dimension(Math.max(c.getPreferredSize().width + s(10), s(90)), s(32)));
        }
    }

    // ================================================================== tien ich

    public static JLabel nhan(final String chu) {
        final JLabel l = new JLabel(chu);
        l.setFont(GiaoDien.font(13));
        l.setForeground(GiaoDien.chu);
        return l;
    }

    /** Doan van tu xuong dong (khong sua duoc, trong suot) - dung trong danh sach doc. */
    public static JTextArea doanVan(final String chu, final boolean phu) {
        final JTextArea a = new JTextArea() {
            @Override
            public void scrollRectToVisible(final java.awt.Rectangle r) {
                // doan van chi de doc: khong bao gio keo trang cuon toi con tro
            }
        };
        ((javax.swing.text.DefaultCaret) a.getCaret()).setUpdatePolicy(javax.swing.text.DefaultCaret.NEVER_UPDATE);
        a.setText(chu);
        a.setColumns(10);
        a.setEditable(false);
        a.setFocusable(false);
        a.setOpaque(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(null);
        ((javax.swing.text.DefaultCaret) a.getCaret()).setUpdatePolicy(javax.swing.text.DefaultCaret.NEVER_UPDATE);
        a.setFont(GiaoDien.font(phu ? 12 : 13));
        a.setForeground(phu ? GiaoDien.chuPhu : GiaoDien.chu);
        a.putClientProperty("giuMau", Boolean.TRUE);
        return a;
    }

    public static JLabel nhanPhu(final String chu) {
        final JLabel l = new JLabel(chu);
        l.setFont(GiaoDien.font(12));
        l.setForeground(GiaoDien.chuPhu);
        l.putClientProperty("mauChu", "phu");
        return l;
    }

    public static JLabel tieuDe(final String chu, final float co) {
        final JLabel l = new JLabel(chu);
        l.setFont(GiaoDien.dam(co));
        l.setForeground(GiaoDien.chu);
        return l;
    }

    /** FlowLayout biet xuong dong: chieu cao uu tien tinh theo be rong that cua cha (khong mat nut khi hep). */
    public static class FlowXuongDong extends FlowLayout {
        public FlowXuongDong(final int canh, final int ngang, final int doc) {
            super(canh, ngang, doc);
        }

        @Override
        public Dimension preferredLayoutSize(final java.awt.Container p) {
            return kich(p, true);
        }

        @Override
        public Dimension minimumLayoutSize(final java.awt.Container p) {
            final Dimension d = kich(p, false);
            d.width -= getHgap() + 1;
            return d;
        }

        private Dimension kich(final java.awt.Container p, final boolean uuTien) {
            synchronized (p.getTreeLock()) {
                // Be rong san co = be rong cua CHA (khong phai cua chinh no: thanh phan dat o EAST cua BorderLayout
                // duoc cap dung be rong uu tien, dung no se tu xuong dong mai).
                int rong = 0;
                java.awt.Container cha = p.getParent();
                while (cha != null && rong == 0) {
                    rong = cha.getWidth() - cha.getInsets().left - cha.getInsets().right;
                    cha = cha.getParent();
                }
                if (rong <= 0) {
                    rong = Integer.MAX_VALUE;
                }
                final Insets in = p.getInsets();
                final int toiDa = rong - (in.left + in.right + getHgap() * 2);
                final Dimension kq = new Dimension(0, 0);
                int w = 0, h = 0;
                for (int i = 0; i < p.getComponentCount(); i++) {
                    final Component c = p.getComponent(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    final Dimension d = uuTien ? c.getPreferredSize() : c.getMinimumSize();
                    if (w + d.width > toiDa && w > 0) {
                        kq.width = Math.max(kq.width, w);
                        kq.height += h + getVgap();
                        w = 0;
                        h = 0;
                    }
                    if (w != 0) {
                        w += getHgap();
                    }
                    w += d.width;
                    h = Math.max(h, d.height);
                }
                kq.width = Math.max(kq.width, w);
                kq.height += h;
                kq.width += in.left + in.right + getHgap() * 2;
                kq.height += in.top + in.bottom + getVgap() * 2;
                return kq;
            }
        }
    }

    public static JPanel hang(final int canh, final Component... cs) {
        final JPanel p = new JPanel(new FlowXuongDong(canh, s(8), s(3)));
        p.setOpaque(false);
        for (final Component c : cs) {
            p.add(c);
        }
        return p;
    }

    public static JPanel trongSuot(final java.awt.LayoutManager lm) {
        final JPanel p = new JPanel(lm);
        p.setOpaque(false);
        return p;
    }

    public static JTextArea vungChu(final int dong, final int cot) {
        final JTextArea a = new JTextArea(dong, cot);
        a.setFont(GiaoDien.font(13));
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBackground(GiaoDien.nenO);
        a.setForeground(GiaoDien.chu);
        a.setCaretColor(GiaoDien.chu);
        a.setBorder(new EmptyBorder(s(8), s(10), s(8), s(10)));
        return a;
    }

    /** Icon tu anh pixel (phong to lang gieng gan nhat, giu net pixel art). */
    public static ImageIcon iconPixel(final BufferedImage anh, final int co) {
        if (anh == null) {
            return null;
        }
        return new ImageIcon(AnhPokemon.phongTo(anh, co, co));
    }

    /** Nut nho chi co icon (dung tren thanh tieu de). */
    public static AbstractButton nutIcon(final Icon icon, final String goiY) {
        final Nut b = new Nut("", Kieu.MO, icon);
        b.setToolTipText(goiY);
        b.setBorder(new EmptyBorder(s(5), s(7), s(5), s(7)));
        return b;
    }

    /** Ve chu co vien (logo kieu Pokemon: chu vang vien xanh). */
    public static void veChuVien(final Graphics2D g, final String chu, final Font f, final float x, final float y,
            final Color ruot, final Color vienMau, final float day) {
        final java.awt.font.TextLayout tl = new java.awt.font.TextLayout(chu, f, g.getFontRenderContext());
        final java.awt.Shape hinh = tl.getOutline(java.awt.geom.AffineTransform.getTranslateInstance(x, y));
        g.setStroke(new BasicStroke(day, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(vienMau);
        g.draw(hinh);
        g.setColor(ruot);
        g.fill(hinh);
    }

    /** Lam mo mot thanh phan (dung cho trang thai cho). */
    static void veMo(final Graphics2D g, final float a) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
    }

    /** Hien ngang mot thanh phan rong khi khong co du lieu. */
    public static JComponent trongRong(final String chu) {
        final JLabel l = new JLabel(chu, SwingConstants.CENTER);
        l.setFont(GiaoDien.font(13));
        l.setForeground(GiaoDien.chuMo);
        l.putClientProperty("mauChu", "mo");
        return l;
    }

    static TableCellRenderer oHuyHieu() {
        return new DefaultTableCellRenderer() {
            private final HuyHieu h = new HuyHieu("", GiaoDien.XANH_LA);

            @Override
            public Component getTableCellRendererComponent(final JTable t, final Object v, final boolean chon, final boolean f,
                    final int row, final int col) {
                final String s0 = v == null ? "" : v.toString();
                Color m = GiaoDien.XANH_DUONG;
                final String sl = s0.toLowerCase();
                if (sl.startsWith("bật") || sl.startsWith("đang") || sl.startsWith("tốt") || sl.startsWith("xanh")
                        || sl.startsWith("ok") || sl.startsWith("còn") || sl.startsWith("đã")) {
                    m = GiaoDien.XANH_LA;
                } else if (sl.startsWith("tắt") || sl.startsWith("hết") || sl.startsWith("đỏ") || sl.startsWith("lỗi")
                        || sl.startsWith("khoá") || sl.startsWith("khóa") || sl.startsWith("huỷ") || sl.startsWith("hủy")) {
                    m = GiaoDien.DO;
                } else if (sl.startsWith("chờ") || sl.startsWith("vàng") || sl.startsWith("cần")) {
                    m = GiaoDien.VANG_DAM;
                }
                h.dat(s0, m);
                if (h.getParent() != p) {
                    p.add(h);
                }
                p.setBackground(chon ? GiaoDien.chonDong : row % 2 == 0 ? GiaoDien.nenThe : GiaoDien.dongLe);
                return p;
            }

            private final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, s(8), s(5)));
        };
    }
}
