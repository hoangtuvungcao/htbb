package server.quantri;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import static server.quantri.GiaoDien.s;

/**
 * Hop thoai theo giao dien Pokemon (xac nhan, bieu mau, ket qua) + chay viec NGOAI EDT.
 *
 * {@link #chay}: moi thao tac co the cham (CSDL, luu nhan vat, gui goi tin hang loat) chay trong
 * SwingWorker - giao dien khong dong bang, luong game khong bi luong Swing giu.
 */
public final class HopThoai {

    private HopThoai() {
    }

    /** Hien hop thoai co noi dung tuy y + cac nut; tra ve chi so nut da bam, -1 = dong. */
    public static int hien(final Component cha, final String tieuDe, final String moTa, final JComponent noiDung,
            final boolean nguy, final String... nut) {
        final Window w = cha == null ? null : SwingUtilities.getWindowAncestor(cha);
        final JDialog d = new JDialog(w, tieuDe, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        final int[] chon = {-1};
        final JPanel goc = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(final Graphics g0) {
                final Graphics2D g = (Graphics2D) g0.create();
                GiaoDien.khuRang(g);
                g.setColor(GiaoDien.nenThe);
                g.fillRect(0, 0, getWidth(), getHeight());
                final Color m = nguy ? GiaoDien.DO : GiaoDien.XANH_DUONG;
                g.setPaint(new GradientPaint(0, 0, m, getWidth(), 0, GiaoDien.pha(m, Color.BLACK, 0.25f)));
                g.fillRect(0, 0, getWidth(), s(56));
                g.setColor(new Color(0x22252E));
                g.fillRect(0, s(56), getWidth(), s(3));
                AnhPokemon.vePokeBall(g, getWidth() - s(70), -s(22), s(96), true);
                g.dispose();
            }
        };
        final JPanel dau = new JPanel(new BorderLayout(s(10), 0));
        dau.setOpaque(false);
        dau.setBorder(new EmptyBorder(s(12), s(18), s(12), s(18)));
        dau.setPreferredSize(new Dimension(s(420), s(59)));
        final JLabel t = new JLabel(tieuDe, ThanhPhan.iconPixel(AnhPokemon.pokeBall(32), s(28)), JLabel.LEADING);
        t.setFont(GiaoDien.dam(16));
        t.setForeground(Color.WHITE);
        t.setIconTextGap(s(10));
        t.putClientProperty("giuMau", Boolean.TRUE);
        dau.putClientProperty("giuMau", Boolean.TRUE);
        dau.add(t, BorderLayout.WEST);
        goc.add(dau, BorderLayout.NORTH);
        final JPanel than = new JPanel(new BorderLayout(0, s(10)));
        than.setOpaque(false);
        than.setBorder(new EmptyBorder(s(16), s(20), s(8), s(20)));
        if (moTa != null && !moTa.isEmpty()) {
            final JTextArea mt = new JTextArea(moTa);
            mt.setEditable(false);
            mt.setOpaque(false);
            mt.setLineWrap(true);
            mt.setWrapStyleWord(true);
            mt.setFont(GiaoDien.font(13));
            mt.setForeground(GiaoDien.chu);
            mt.setBorder(null);
            mt.setColumns(36);
            mt.setFocusable(false);
            mt.putClientProperty("giuMau", Boolean.TRUE);
            than.add(mt, BorderLayout.NORTH);
        }
        if (noiDung != null) {
            than.add(noiDung, BorderLayout.CENTER);
        }
        goc.add(than, BorderLayout.CENTER);
        final JPanel hangNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, s(8), 0));
        hangNut.setOpaque(false);
        hangNut.setBorder(new EmptyBorder(s(6), s(16), s(16), s(16)));
        for (int i = nut.length - 1; i >= 0; i--) {
            final int k = i;
            final ThanhPhan.Kieu kieu = i == 0 ? (nguy ? ThanhPhan.Kieu.DO : ThanhPhan.Kieu.XANH) : ThanhPhan.Kieu.MO;
            final ThanhPhan.Nut b = new ThanhPhan.Nut(nut[i], kieu);
            b.addActionListener(e -> {
                chon[0] = k;
                d.dispose();
            });
            hangNut.add(b);
            if (i == 0) {
                d.getRootPane().setDefaultButton(b);
            }
        }
        goc.add(hangNut, BorderLayout.SOUTH);
        d.getRootPane().registerKeyboardAction(e -> d.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        GiaoDien.toCay(than);
        goc.setBorder(javax.swing.BorderFactory.createLineBorder(GiaoDien.toi ? new Color(0x3C4664) : new Color(0x9AA3B8)));
        d.setContentPane(goc);
        d.pack();
        d.setMinimumSize(new Dimension(s(420), d.getHeight()));
        d.setLocationRelativeTo(w);
        d.setVisible(true);
        return chon[0];
    }

    public static boolean xacNhan(final Component cha, final String tieuDe, final String noiDung, final String dongY) {
        return hien(cha, tieuDe, noiDung, null, true, dongY, "Huỷ") == 0;
    }

    public static boolean bieuMau(final Component cha, final String tieuDe, final String moTa, final JComponent noiDung,
            final String dongY) {
        return hien(cha, tieuDe, moTa, noiDung, false, dongY, "Huỷ") == 0;
    }

    public static void ketQua(final Component cha, final String tieuDe, final String noiDung) {
        hien(cha, tieuDe, noiDung, null, false, "Đóng");
    }

    /** Hien bang/van ban dai trong hop thoai cuon. */
    public static void xemNoiDung(final Component cha, final String tieuDe, final JComponent noiDung) {
        noiDung.setPreferredSize(noiDung.getPreferredSize().width < s(300)
                ? new Dimension(s(640), s(380)) : noiDung.getPreferredSize());
        hien(cha, tieuDe, null, noiDung, false, "Đóng");
    }

    // ================================================================== chay ngoai EDT

    /** Noi bao ket qua (thanh trang thai / thong bao nho) - AdminPanel dat. */
    public static volatile Consumer<String> baoNhanh = s -> { };

    /**
     * Chay viec tren luong nen. xacNhan != null -> hoi truoc (hop thoai do). Xong -> goi xong tren EDT
     * va bao nhanh ket qua.
     */
    public static void chay(final Component cha, final String xacNhan, final Supplier<String> viec, final Consumer<String> xong) {
        if (xacNhan != null && !xacNhan(cha, "Xác nhận thao tác", xacNhan, "Thực hiện")) {
            return;
        }
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    return viec.get();
                } catch (final Throwable t) {
                    return "Lỗi: " + t;
                }
            }

            @Override
            protected void done() {
                String kq;
                try {
                    kq = get();
                } catch (final Exception e) {
                    kq = "Lỗi: " + e;
                }
                try {
                    baoNhanh.accept(kq);
                } catch (final Throwable ignored) {
                }
                if (xong != null) {
                    xong.accept(kq);
                }
            }
        }.execute();
    }

    /** Doc du lieu ngoai EDT roi dua ket qua ve EDT. */
    public static <T> void doc(final Supplier<T> viec, final Consumer<T> xong) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return viec.get();
            }

            @Override
            protected void done() {
                try {
                    xong.accept(get());
                } catch (final Exception ignored) {
                }
            }
        }.execute();
    }

    /** Luoi nhan + o (bieu mau 2 cot). */
    public static JPanel luoi(final Object... nhanVaO) {
        final JPanel p = new JPanel(new java.awt.GridBagLayout());
        p.setOpaque(false);
        final java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.insets = new java.awt.Insets(s(5), s(4), s(5), s(4));
        c.anchor = java.awt.GridBagConstraints.WEST;
        for (int i = 0; i + 1 < nhanVaO.length; i += 2) {
            c.gridy = i / 2;
            c.gridx = 0;
            c.weightx = 0;
            c.fill = java.awt.GridBagConstraints.NONE;
            final Object n = nhanVaO[i];
            p.add(n instanceof Component ? (Component) n : ThanhPhan.nhan(String.valueOf(n)), c);
            c.gridx = 1;
            c.weightx = 1;
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;
            p.add((Component) nhanVaO[i + 1], c);
        }
        p.setBorder(BorderFactory.createEmptyBorder());
        return p;
    }
}
