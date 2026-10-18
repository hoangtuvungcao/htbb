package server.quantri;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import static server.quantri.GiaoDien.s;

/** The chon dang "phan doan" (thay JTabbedPane mac dinh): hang nut vien thuoc + CardLayout. */
public final class PhanDoan extends JPanel {

    private final JPanel hang;
    private final JPanel than = ThanhPhan.trongSuot(new CardLayout());
    private final List<ThanhPhan.Nut> nut = new ArrayList<ThanhPhan.Nut>();
    private int chon = -1;
    private IntConsumer khiDoi = i -> { };

    public PhanDoan() {
        super(new BorderLayout(0, s(10)));
        setOpaque(false);
        hang = new JPanel(new FlowLayout(FlowLayout.LEFT, s(3), s(3))) {
            @Override
            protected void paintComponent(final Graphics g0) {
                final Graphics2D g = (Graphics2D) g0.create();
                GiaoDien.khuRang(g);
                int w = 0;
                for (final Component c : getComponents()) {
                    w = Math.max(w, c.getX() + c.getWidth());
                }
                g.setColor(GiaoDien.nenThe2);
                g.fill(new RoundRectangle2D.Float(0, 0, w + s(3), getHeight(), s(12), s(12)));
                g.dispose();
            }
        };
        hang.setOpaque(false);
        hang.setBorder(new EmptyBorder(0, 0, 0, 0));
        final JPanel boc = ThanhPhan.trongSuot(new BorderLayout());
        boc.add(hang, BorderLayout.WEST);
        add(boc, BorderLayout.NORTH);
        add(than, BorderLayout.CENTER);
    }

    public PhanDoan them(final String ten, final Component c) {
        final int i = nut.size();
        final ThanhPhan.Nut b = new ThanhPhan.Nut(ten, ThanhPhan.Kieu.MO).nho();
        b.addActionListener(e -> chon(i));
        nut.add(b);
        hang.add(b);
        than.add(c, String.valueOf(i));
        if (chon < 0) {
            chon(0);
        }
        return this;
    }

    public void khiDoi(final IntConsumer c) {
        khiDoi = c;
    }

    public int dangChon() {
        return chon;
    }

    public void chon(final int i) {
        chon = i;
        for (int k = 0; k < nut.size(); k++) {
            nut.get(k).datKieu(k == i ? ThanhPhan.Kieu.XANH : ThanhPhan.Kieu.MO);
        }
        ((CardLayout) than.getLayout()).show(than, String.valueOf(i));
        khiDoi.accept(i);
    }
}
