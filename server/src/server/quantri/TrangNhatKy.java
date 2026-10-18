package server.quantri;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import static server.quantri.GiaoDien.s;

/**
 * Trang 8 - Nhat ky: theo doi truc tiep tep log cua com.htbb.log.Nhat (may_chu-yyyyMMdd.log, dinh dang
 * "MM-dd HH:mm:ss.SSS L [LOAI] noi dung") - loc muc / loai / chu, tam dung; kiem toan bang dieu khien; hoat dong.
 * Doc tep TANG DAN (chi phan moi) tren luong nen, toi da 1 lan / giay khi trang dang hien.
 */
public final class TrangNhatKy extends Trang {

    static final int GIU_DONG = 4000;
    static final Pattern DONG = Pattern.compile("^\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d{3} ([EWID]) (?:\\[([A-Z0-9_]+)\\] )?.*");

    private static final ArrayDeque<String> HOAT_DONG = new ArrayDeque<String>();

    private final JComboBox<Object> chonTep = new JComboBox<Object>();
    private final JCheckBox cE = hop("Lỗi", true), cW = hop("Cảnh báo", true), cI = hop("Thông tin", true), cD = hop("Gỡ lỗi", false);
    private final JComboBox<String> loai = new JComboBox<String>(new String[]{"Mọi loại"});
    private final ThanhPhan.ONhap tim = new ThanhPhan.ONhap("", 16).goiY("Tìm trong nhật ký…");
    private final JCheckBox tamDung = hop("Tạm dừng", false);
    private final DefaultListModel<String> mh = new DefaultListModel<String>();
    private final JList<String> ds = new JList<String>(mh);
    private final JLabel tt = ThanhPhan.nhanPhu("");
    private final ArrayDeque<String> dem = new ArrayDeque<String>();
    private final TreeSet<String> dsLoai = new TreeSet<String>();
    private File tep;
    private long viTri = -1;
    private volatile boolean dangDoc;

    private final DefaultListModel<String> mhKt = new DefaultListModel<String>();
    private final DefaultListModel<String> mhHd = new DefaultListModel<String>();
    private final PhanDoan phan = new PhanDoan();

    public TrangNhatKy() {
        super("Nhật ký", "Nhật ký máy chủ trực tiếp, kiểm toán thao tác của bảng điều khiển và hoạt động gần đây.");
        ds.setFont(GiaoDien.mono(12));
        ds.setCellRenderer(new ODong());
        ds.setFixedCellHeight(s(20));
        for (final JCheckBox c : new JCheckBox[]{cE, cW, cI, cD}) {
            c.addActionListener(e -> locLai());
        }
        loai.addActionListener(e -> locLai());
        tim.addActionListener(e -> locLai());
        tim.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(final javax.swing.event.DocumentEvent e) {
                locLai();
            }

            @Override
            public void removeUpdate(final javax.swing.event.DocumentEvent e) {
                locLai();
            }

            @Override
            public void changedUpdate(final javax.swing.event.DocumentEvent e) {
                locLai();
            }
        });
        chonTep.addActionListener(e -> {
            final Object o = chonTep.getSelectedItem();
            if (o instanceof TepHien && !((TepHien) o).f.equals(tep)) {
                moTep(((TepHien) o).f);
            }
        });
        chonTep.setRenderer(new DefaultListCellRenderer());
        chonTep.setPreferredSize(new Dimension(s(220), s(32)));
        loai.setPreferredSize(new Dimension(s(150), s(32)));
        final JPanel loc = ThanhPhan.trongSuot(new FlowLayout(FlowLayout.LEFT, s(8), 0));
        loc.add(chonTep);
        loc.add(cE);
        loc.add(cW);
        loc.add(cI);
        loc.add(cD);
        loc.add(loai);
        loc.add(tim);
        loc.add(tamDung);
        final JPanel thanLog = ThanhPhan.trongSuot(new BorderLayout(0, s(8)));
        thanLog.add(loc, BorderLayout.NORTH);
        final JScrollPane sp = ThanhPhan.cuon(ds);
        sp.setPreferredSize(new Dimension(s(800), s(470)));
        thanLog.add(sp, BorderLayout.CENTER);
        thanLog.add(tt, BorderLayout.SOUTH);

        final JList<String> lKt = new JList<String>(mhKt);
        lKt.setFont(GiaoDien.mono(12));
        lKt.setCellRenderer(new ODong());
        final JScrollPane spKt = ThanhPhan.cuon(lKt);
        spKt.setPreferredSize(new Dimension(s(800), s(470)));
        final JPanel thanKt = ThanhPhan.trongSuot(new BorderLayout(0, s(8)));
        thanKt.add(spKt, BorderLayout.CENTER);
        thanKt.add(ThanhPhan.doanVan("Ghi đồng thời: " + KiemToanQuanTri.tep().getName() + " (thư mục làm việc) và kiem_toan-yyyyMMdd.log"
                + " của nhật ký chung (loại ADMIN).", true), BorderLayout.SOUTH);
        final JList<String> lHd = new JList<String>(mhHd);
        lHd.setFont(GiaoDien.mono(12));
        lHd.setCellRenderer(new ODong());
        final JScrollPane spHd = ThanhPhan.cuon(lHd);
        spHd.setPreferredSize(new Dimension(s(800), s(470)));

        phan.them("Máy chủ (trực tiếp)", thanLog).them("Kiểm toán bảng điều khiển", thanKt).them("Hoạt động gần đây", spHd);
        phan.khiDoi(i -> napPhu());
        final ThanhPhan.The the = new ThanhPhan.The(null);
        the.noiDung(phan);
        thanh.add(the, BorderLayout.NORTH);
        KiemToanQuanTri.nghe(() -> SwingUtilities.invokeLater(() -> {
            if (dangHien()) {
                napPhu();
            }
        }));
    }

    static JCheckBox hop(final String t, final boolean b) {
        final JCheckBox c = new JCheckBox(t, b);
        c.setOpaque(false);
        c.setFont(GiaoDien.font(12.5f));
        c.setForeground(GiaoDien.chu);
        return c;
    }

    @Override
    public String ten() {
        return "Nhật ký";
    }

    @Override
    public String icon() {
        return "Paper_News";
    }

    /** Ghi mot dong hoat dong (AdminPanel.addLog, ButtonEditor cu). */
    public static void hoatDong(final String dong) {
        synchronized (HOAT_DONG) {
            HOAT_DONG.addLast(DinhDang.gio(System.currentTimeMillis()) + " I [PANEL] " + KiemToanQuanTri.sach(dong));
            while (HOAT_DONG.size() > 500) {
                HOAT_DONG.removeFirst();
            }
        }
    }

    void napPhu() {
        mhKt.clear();
        for (final String s0 : KiemToanQuanTri.ganDay()) {
            mhKt.addElement(s0);
        }
        mhHd.clear();
        synchronized (HOAT_DONG) {
            for (final String s0 : HOAT_DONG) {
                mhHd.addElement(s0);
            }
        }
    }

    @Override
    public void khiHien() {
        napPhu();
        HopThoai.doc(TrangVanHanh::timTepNhatKy, l -> {
            final DefaultComboBoxModel<Object> m = new DefaultComboBoxModel<Object>();
            TepHien chonMacDinh = null;
            for (final File f : l) {
                final TepHien t = new TepHien(f);
                m.addElement(t);
                if (chonMacDinh == null && f.getName().startsWith("may_chu-")) {
                    chonMacDinh = t;
                }
            }
            if (tep != null) {
                for (int i = 0; i < m.getSize(); i++) {
                    if (((TepHien) m.getElementAt(i)).f.equals(tep)) {
                        chonMacDinh = (TepHien) m.getElementAt(i);
                    }
                }
            }
            chonTep.setModel(m);
            if (chonMacDinh == null && m.getSize() > 0) {
                chonMacDinh = (TepHien) m.getElementAt(0);
            }
            if (chonMacDinh != null) {
                chonTep.setSelectedItem(chonMacDinh);
                if (!chonMacDinh.f.equals(tep)) {
                    moTep(chonMacDinh.f);
                }
            } else {
                tt.setText("Chưa có tệp nhật ký - hãy bật ghi nhật ký ra tệp trong cấu hình.");
            }
        });
    }

    static final class TepHien {
        final File f;

        TepHien(final File f) {
            this.f = f;
        }

        @Override
        public String toString() {
            return f.getName();
        }
    }

    /** Mo mot tep (tu trang Van hanh hoac hop chon). */
    public void moTep(final File f) {
        tep = f;
        viTri = -1;
        dem.clear();
        mh.clear();
        dsLoai.clear();
        phan.chon(0);
        docMoi();
    }

    @Override
    public void nhip(final DuLieuTrucTiep.Mau m) {
        if (!tamDung.isSelected() && tep != null && phan.dangChon() == 0) {
            docMoi();
        }
    }

    void docMoi() {
        if (dangDoc || tep == null) {
            return;
        }
        dangDoc = true;
        final File f = tep;
        final long tu = viTri;
        HopThoai.doc(() -> doc(f, tu), kq -> {
            dangDoc = false;
            if (kq == null || f != tep) {
                return;
            }
            viTri = (Long) kq[0];
            @SuppressWarnings("unchecked")
            final List<String> moi = (List<String>) kq[1];
            if ((Boolean) kq[2]) {
                dem.clear();
                mh.clear();
            }
            final boolean cuoi = ds.getLastVisibleIndex() >= mh.getSize() - 2;
            boolean themLoai = false;
            for (final String d : moi) {
                dem.addLast(d);
                final Matcher mm = DONG.matcher(d);
                if (mm.matches() && mm.group(2) != null && dsLoai.add(mm.group(2))) {
                    themLoai = true;
                }
                if (hop(d)) {
                    mh.addElement(d);
                }
            }
            while (dem.size() > GIU_DONG) {
                dem.removeFirst();
            }
            while (mh.getSize() > GIU_DONG) {
                mh.remove(0);
            }
            if (themLoai) {
                final Object giu = loai.getSelectedItem();
                final DefaultComboBoxModel<String> lm = new DefaultComboBoxModel<String>();
                lm.addElement("Mọi loại");
                for (final String l : dsLoai) {
                    lm.addElement(l);
                }
                loai.setModel(lm);
                loai.setSelectedItem(giu);
            }
            if (cuoi && mh.getSize() > 0 && !moi.isEmpty()) {
                ds.ensureIndexIsVisible(mh.getSize() - 1);
            }
            tt.setText(f.getAbsolutePath() + " · " + DinhDang.so(f.length() / 1024) + " KB · hiển thị " + mh.getSize() + " / " + dem.size()
                    + " dòng gần nhất");
        });
    }

    /** {viTri moi, dong moi, da dat lai}. Doc toi da 256 KB moi lan; lan dau 128 KB cuoi. */
    static Object[] doc(final File f, final long tu) {
        try (RandomAccessFile r = new RandomAccessFile(f, "r")) {
            final long dai = r.length();
            long bd = tu;
            boolean datLai = false;
            if (bd < 0 || bd > dai) {
                bd = Math.max(0, dai - 128 * 1024);
                datLai = true;
            }
            final int n = (int) Math.min(256 * 1024, dai - bd);
            final byte[] b = new byte[n];
            r.seek(bd);
            r.readFully(b);
            int cat = n;
            while (cat > 0 && b[cat - 1] != '\n') {
                cat--; // chi lay dong tron ven
            }
            final String s0 = new String(b, 0, cat, StandardCharsets.UTF_8);
            final List<String> l = new ArrayList<String>();
            final String[] dong = s0.split("\n");
            for (int i = 0; i < dong.length; i++) {
                if (datLai && i == 0 && bd > 0) {
                    continue; // dong dau co the bi cat giua
                }
                if (!dong[i].isEmpty()) {
                    l.add(dong[i].length() > 600 ? dong[i].substring(0, 600) + "…" : dong[i]);
                }
            }
            return new Object[]{bd + cat, l, datLai};
        } catch (final Exception e) {
            return null;
        }
    }

    boolean hop(final String d) {
        final Matcher m = DONG.matcher(d);
        char muc = 'I';
        String l = null;
        if (m.matches()) {
            muc = m.group(1).charAt(0);
            l = m.group(2);
        }
        if (muc == 'E' && !cE.isSelected() || muc == 'W' && !cW.isSelected() || muc == 'I' && !cI.isSelected()
                || muc == 'D' && !cD.isSelected()) {
            return false;
        }
        final Object lc = loai.getSelectedItem();
        if (lc != null && !"Mọi loại".equals(lc) && !lc.equals(l)) {
            return false;
        }
        final String t = tim.getText().trim().toLowerCase(Locale.ROOT);
        return t.isEmpty() || d.toLowerCase(Locale.ROOT).contains(t);
    }

    void locLai() {
        mh.clear();
        for (final String d : dem) {
            if (hop(d)) {
                mh.addElement(d);
            }
        }
        if (mh.getSize() > 0) {
            ds.ensureIndexIsVisible(mh.getSize() - 1);
        }
    }

    /** To mau dong theo muc. */
    static final class ODong extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(final JList<?> l, final Object v, final int i, final boolean chon, final boolean f) {
            super.getListCellRendererComponent(l, v, i, chon, false);
            final String d = String.valueOf(v);
            final Matcher m = DONG.matcher(d);
            char muc = 'I';
            if (m.matches()) {
                muc = m.group(1).charAt(0);
            } else if (d.contains("[") && d.length() > 20) {
                muc = d.contains(" E ") ? 'E' : d.contains(" W ") ? 'W' : 'I';
            }
            setFont(GiaoDien.mono(12));
            setBorder(new EmptyBorder(0, s(8), 0, s(8)));
            final Color c = muc == 'E' ? GiaoDien.DO : muc == 'W' ? (GiaoDien.toi ? GiaoDien.VANG : new Color(0xB07800))
                    : muc == 'D' ? GiaoDien.chuMo : GiaoDien.chu;
            setForeground(c);
            setBackground(chon ? GiaoDien.chonDong : muc == 'E' ? GiaoDien.trong(GiaoDien.DO, GiaoDien.toi ? 40 : 18)
                    : i % 2 == 0 ? GiaoDien.nenThe : GiaoDien.dongLe);
            return this;
        }
    }
}
