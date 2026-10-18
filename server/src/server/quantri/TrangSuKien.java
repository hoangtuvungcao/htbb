package server.quantri;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import static server.quantri.GiaoDien.s;

/** Trang 3 - Thong bao & Su kien: thong bao (xem truoc, hen gio), tob_su_kien + lich 52 tuan, su kien the gioi, boss. */
public final class TrangSuKien extends Trang {

    private final JTextArea noiDung = ThanhPhan.vungChu(3, 40);
    private final ButtonGroup kieu = new ButtonGroup();
    private final XemTruoc xemTruoc = new XemTruoc();
    private final JSpinner sauPhut = new JSpinner(new SpinnerNumberModel(0, 0, 1440, 1));
    private final DefaultListModel<String> dsHen = new DefaultListModel<String>();
    private final List<Timer> henGio = new ArrayList<Timer>();

    private final DefaultTableModel moHinhSk = new DefaultTableModel(new Object[]{"Tên", "Nhóm", "Cài đặt", "Đang chạy",
        "Hẹn mở", "Hẹn đóng", "Mã"}, 0) {
        @Override
        public boolean isCellEditable(final int r, final int c) {
            return false;
        }
    };
    private final JTable bangSk = new ThanhPhan.Bang(moHinhSk).rong("Chưa có sự kiện nào.");
    private final BieuDo.Lich lich = new BieuDo.Lich();
    private final DefaultTableModel moHinhBoss = new DefaultTableModel(new Object[]{"#", "Boss", "Trạng thái", "Hiện tại", "Hồi sinh sau"}, 0) {
        @Override
        public boolean isCellEditable(final int r, final int c) {
            return false;
        }
    };
    private final JTable bangBoss = new ThanhPhan.Bang(moHinhBoss).rong("Máy chủ chưa nạp boss thế giới.");
    private final JPanel trangThaiTg = danhSach();
    private long lanDoc;

    public TrangSuKien() {
        super("Thông báo & Sự kiện", "Gửi thông báo toàn máy chủ, bật/hẹn giờ sự kiện, kích hoạt sự kiện thế giới.");
        // ---- thong bao
        final ThanhPhan.The theTb = new ThanhPhan.The("Thông báo toàn máy chủ", TrangTongQuan.icon("Paper_News")).nhan(GiaoDien.DO);
        final JPanel radio = TrangNguoiChoi.nhomRadio(kieu, ThaoTacMayChu.KIEU_THONG_BAO);
        java.util.Collections.list(kieu.getElements()).forEach(b -> b.addActionListener(e -> xemTruoc.repaint()));
        noiDung.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                xemTruoc.repaint();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                xemTruoc.repaint();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                xemTruoc.repaint();
            }
        });
        final ThanhPhan.Nut gui = new ThanhPhan.Nut("Gửi thông báo", ThanhPhan.Kieu.DO, TrangTongQuan.icon("Note_Gold"));
        gui.addActionListener(e -> gui());
        final JList<String> lHen = new JList<String>(dsHen);
        lHen.setFont(GiaoDien.font(12));
        lHen.setVisibleRowCount(3);
        final ThanhPhan.Nut huyHen = new ThanhPhan.Nut("Huỷ hẹn", ThanhPhan.Kieu.MO).nho();
        huyHen.addActionListener(e -> {
            final int i = lHen.getSelectedIndex();
            if (i >= 0) {
                henGio.get(i).stop();
                henGio.remove(i);
                KiemToanQuanTri.ghi("THONG_BAO_HUY_HEN", "", dsHen.get(i));
                dsHen.remove(i);
            }
        });
        final JPanel trai = ThanhPhan.trongSuot(new BorderLayout(0, s(8)));
        noiDung.setRows(4);
        final JScrollPane spNd = ThanhPhan.cuon(noiDung);
        spNd.setBorder(new ThanhPhan.VienTron(s(10), false));
        spNd.setPreferredSize(new Dimension(s(400), s(96)));
        trai.add(spNd, BorderLayout.NORTH);
        trai.add(radio, BorderLayout.CENTER);
        trai.add(ThanhPhan.hang(FlowLayout.LEFT, ThanhPhan.nhan("Gửi sau"), sauPhut, ThanhPhan.nhanPhu("phút (0 = ngay)"), gui),
                BorderLayout.SOUTH);
        final JPanel phai = ThanhPhan.trongSuot(new BorderLayout(0, s(8)));
        phai.add(xemTruoc, BorderLayout.NORTH);
        final JPanel hen = ThanhPhan.trongSuot(new BorderLayout(s(6), 0));
        final JScrollPane spHen = ThanhPhan.cuon(lHen);
        spHen.setPreferredSize(new Dimension(s(200), s(56)));
        spHen.setBorder(new ThanhPhan.VienTron(s(10), false));
        hen.add(spHen, BorderLayout.CENTER);
        final JPanel oHuy = new JPanel(new java.awt.GridBagLayout());
        oHuy.setOpaque(false);
        oHuy.add(huyHen);
        hen.add(oHuy, BorderLayout.EAST);
        hen.add(ThanhPhan.doanVan("Thông báo đã hẹn giờ (giữ trong lúc bảng điều khiển còn mở):", true), BorderLayout.NORTH);
        phai.add(hen, BorderLayout.CENTER);
        theTb.noiDung(tiLe(new double[]{1.1, 1}, trai, phai));

        // ---- su kien tob_su_kien
        final ThanhPhan.The theSk = new ThanhPhan.The("Sự kiện trong game", TrangTongQuan.icon("Crown_Yellow")).nhan(GiaoDien.VANG);
        ThanhPhan.kieuBang(bangSk);
        bangSk.setAutoCreateRowSorter(true);
        final int[] rongSk = {170, 90, 70, 86, 124, 124, 90};
        for (int i = 0; i < rongSk.length; i++) {
            bangSk.getColumnModel().getColumn(i).setPreferredWidth(s(rongSk[i]));
        }
        bangSk.getColumnModel().getColumn(2).setCellRenderer(ThanhPhan.oHuyHieu());
        bangSk.getColumnModel().getColumn(3).setCellRenderer(ThanhPhan.oHuyHieu());
        final ThanhPhan.Nut bat = new ThanhPhan.Nut("Bật", ThanhPhan.Kieu.LA).nho();
        bat.addActionListener(e -> datSk(true));
        final ThanhPhan.Nut tat = new ThanhPhan.Nut("Tắt", ThanhPhan.Kieu.DO).nho();
        tat.addActionListener(e -> datSk(false));
        final ThanhPhan.Nut hen2 = new ThanhPhan.Nut("Hẹn giờ mở/đóng…", ThanhPhan.Kieu.XANH).nho();
        hen2.addActionListener(e -> henSk());
        final ThanhPhan.Nut tai = new ThanhPhan.Nut("Tải lại", ThanhPhan.Kieu.MO).nho();
        tai.addActionListener(e -> docCham());
        final JPanel thanSk = ThanhPhan.trongSuot(new BorderLayout(0, s(8)));
        final JScrollPane spSk = ThanhPhan.cuon(bangSk);
        spSk.setPreferredSize(new Dimension(s(500), s(230)));
        thanSk.add(spSk, BorderLayout.CENTER);
        final JPanel duoiSk = ThanhPhan.trongSuot(new BorderLayout(0, s(4)));
        duoiSk.add(ThanhPhan.hang(FlowLayout.LEFT, bat, tat, hen2, tai), BorderLayout.NORTH);
        duoiSk.add(ThanhPhan.doanVan("Đổi cài đặt có hiệu lực ở lần khởi động lại kế tiếp (máy chủ tự gom các thay đổi rồi khởi động lại nếu được bật).", true),
                BorderLayout.SOUTH);
        thanSk.add(duoiSk, BorderLayout.SOUTH);
        theSk.noiDung(thanSk);

        // ---- lich
        final ThanhPhan.The theLich = new ThanhPhan.The("Lịch 52 tuần · " + Calendar.getInstance().get(Calendar.YEAR),
                TrangTongQuan.icon("Note_Gold")).nhan(GiaoDien.XANH_LA);
        final JPanel thanLich = ThanhPhan.trongSuot(new BorderLayout(0, s(6)));
        thanLich.add(lich, BorderLayout.CENTER);
        thanLich.add(ThanhPhan.doanVan("Ô xanh = có hẹn mở/đóng sự kiện, giải đấu tuần (thứ Bảy), chiếm Gym. Viền đỏ = hôm nay."
                + " Rê chuột để xem.", true), BorderLayout.SOUTH);
        theLich.noiDung(thanLich);

        // ---- su kien the gioi
        final ThanhPhan.The theTg = new ThanhPhan.The("Sự kiện thế giới (hệ sinh thái)", TrangTongQuan.icon("Berry_Oran"))
                .nhan(GiaoDien.XANH_LA_DAM);
        final JPanel nutTg = ThanhPhan.trongSuot(new GridLayout(0, 2, s(8), s(8)));
        nutTg.add(nutTg("Đố vui \"Ai là Pokémon?\"", "dovui", ThanhPhan.Kieu.XANH));
        nutTg.add(nutTg("Mở đua Pokémon", "dua", ThanhPhan.Kieu.LA));
        nutTg.add(nutTg("Bùng phát loài hiếm", "bungphat", ThanhPhan.Kieu.VANG));
        nutTg.add(nutTg("Công bố giải đấu tuần", "giaidau", ThanhPhan.Kieu.TIM));
        final JComboBox<String> vung = new JComboBox<String>(tenVung());
        final JComboBox<String> thoiTiet = new JComboBox<String>(tenThoiTiet());
        final ThanhPhan.Nut datTt = new ThanhPhan.Nut("Đặt thời tiết", ThanhPhan.Kieu.XANH).nho();
        datTt.addActionListener(e -> HopThoai.chay(this, "Đặt thời tiết " + vung.getSelectedItem() + " = " + thoiTiet.getSelectedItem()
                + " (đến hết hôm nay)?", () -> sk("thoitiet", vung.getSelectedIndex(), thoiTiet.getSelectedIndex()), k -> docCham()));
        final ThanhPhan.Nut xoaTt = new ThanhPhan.Nut("Bỏ đặt tay", ThanhPhan.Kieu.MO).nho();
        xoaTt.addActionListener(e -> HopThoai.chay(this, "Bỏ mọi thời tiết đặt tay?", () -> sk("xoathoitiet", 0, 0), k -> docCham()));
        final JPanel thanTg = ThanhPhan.trongSuot(new BorderLayout(0, s(10)));
        thanTg.add(nutTg, BorderLayout.NORTH);
        thanTg.add(ThanhPhan.hang(FlowLayout.LEFT, vung, thoiTiet, datTt, xoaTt), BorderLayout.CENTER);
        thanTg.add(trangThaiTg, BorderLayout.SOUTH);
        theTg.noiDung(thanTg);

        // ---- boss
        final ThanhPhan.The theBoss = new ThanhPhan.The("Boss thế giới", TrangTongQuan.icon("Claw_Gray")).nhan(GiaoDien.DO_DAM);
        ThanhPhan.kieuBang(bangBoss);
        bangBoss.getColumnModel().getColumn(2).setCellRenderer(ThanhPhan.oHuyHieu());
        bangBoss.getColumnModel().getColumn(0).setMaxWidth(s(40));
        final ThanhPhan.Nut moB = new ThanhPhan.Nut("Gọi boss (30 giây)", ThanhPhan.Kieu.DO).nho();
        moB.addActionListener(e -> boss(true));
        final ThanhPhan.Nut tatB = new ThanhPhan.Nut("Tắt boss", ThanhPhan.Kieu.MO).nho();
        tatB.addActionListener(e -> boss(false));
        final JPanel thanB = ThanhPhan.trongSuot(new BorderLayout(0, s(8)));
        final JScrollPane spB = ThanhPhan.cuon(bangBoss);
        spB.setPreferredSize(new Dimension(s(400), s(190)));
        thanB.add(spB, BorderLayout.CENTER);
        thanB.add(ThanhPhan.hang(FlowLayout.LEFT, moB, tatB), BorderLayout.SOUTH);
        theBoss.noiDung(thanB);

        thanh.add(cot(theTb, tiLe(new double[]{1.4, 1}, theSk, theTg), theLich, theBoss), BorderLayout.NORTH);
    }

    static String[] tenVung() {
        try {
            return com.htbb.pokemon.QuanTriSinhThai.tenVung();
        } catch (final Throwable t) {
            return new String[]{"?"};
        }
    }

    static String[] tenThoiTiet() {
        try {
            return com.htbb.pokemon.QuanTriSinhThai.tenThoiTiet();
        } catch (final Throwable t) {
            return new String[]{"?"};
        }
    }

    static String sk(final String loai, final int a, final int b) {
        String kq;
        try {
            kq = com.htbb.pokemon.QuanTriSinhThai.lenh(loai, a, b);
        } catch (final Throwable t) {
            kq = "Lỗi: " + t;
        }
        KiemToanQuanTri.ghi("SU_KIEN_THE_GIOI", loai, a + "/" + b + " -> " + kq);
        return kq;
    }

    ThanhPhan.Nut nutTg(final String chu, final String loai, final ThanhPhan.Kieu k) {
        final ThanhPhan.Nut b = new ThanhPhan.Nut(chu, k);
        b.addActionListener(e -> HopThoai.chay(this, "Kích hoạt \"" + chu + "\" ngay bây giờ? Người chơi toàn máy chủ sẽ nhận thông báo.",
                () -> sk(loai, 0, 0), x -> docCham()));
        return b;
    }

    @Override
    public String ten() {
        return "Sự kiện";
    }

    @Override
    public String icon() {
        return "Crown_Yellow";
    }

    @Override
    public void khiHien() {
        docCham();
    }

    @Override
    public void nhip(final DuLieuTrucTiep.Mau m) {
        if (System.currentTimeMillis() - lanDoc > 10000L) {
            docCham();
        }
    }

    int kieuChon() {
        return kieu.getSelection() == null ? 0 : Integer.parseInt(kieu.getSelection().getActionCommand());
    }

    void gui() {
        final String nd = noiDung.getText().trim();
        if (nd.isEmpty()) {
            HopThoai.baoNhanh.accept("Nhập nội dung thông báo trước.");
            return;
        }
        final int k = kieuChon();
        final int phut = (Integer) sauPhut.getValue();
        if (phut <= 0) {
            HopThoai.chay(this, "Gửi \"" + ThaoTacMayChu.KIEU_THONG_BAO[k] + "\" tới toàn bộ người chơi?\n\n" + nd,
                    () -> ThaoTacMayChu.thongBao(k, nd), x -> noiDung.setText(""));
            return;
        }
        if (!HopThoai.xacNhan(this, "Hẹn giờ thông báo", "Gửi sau " + phut + " phút (lúc " + new SimpleDateFormat("HH:mm").format(
                new Date(System.currentTimeMillis() + phut * 60000L)) + "):\n\n" + nd, "Hẹn giờ")) {
            return;
        }
        final String nhan = new SimpleDateFormat("HH:mm").format(new Date(System.currentTimeMillis() + phut * 60000L)) + " · " + nd;
        final Timer t = new Timer(phut * 60000, null);
        t.setRepeats(false);
        t.addActionListener(e -> {
            final int i = henGio.indexOf(t);
            if (i >= 0) {
                henGio.remove(i);
                dsHen.remove(i);
            }
            HopThoai.chay(this, null, () -> ThaoTacMayChu.thongBao(k, nd), null);
        });
        henGio.add(t);
        dsHen.addElement(nhan);
        t.start();
        KiemToanQuanTri.ghi("THONG_BAO_HEN", ThaoTacMayChu.KIEU_THONG_BAO[k], phut + " phut: " + nd);
        noiDung.setText("");
        HopThoai.baoNhanh.accept("Đã hẹn thông báo lúc " + nhan.substring(0, 5) + " (giữ khi bảng điều khiển còn mở).");
    }

    String maChon() {
        final int r = bangSk.getSelectedRow();
        if (r < 0) {
            HopThoai.baoNhanh.accept("Chọn một sự kiện trong bảng.");
            return null;
        }
        return String.valueOf(bangSk.getValueAt(r, 6));
    }

    void datSk(final boolean bat) {
        final String ma = maChon();
        if (ma != null) {
            HopThoai.chay(this, (bat ? "Bật" : "Tắt") + " sự kiện " + ma + "? Áp dụng khi máy chủ khởi động lại.",
                    () -> ThaoTacMayChu.datSuKien(ma, bat), k -> docCham());
        }
    }

    void henSk() {
        final String ma = maChon();
        if (ma == null) {
            return;
        }
        final int r = bangSk.getSelectedRow();
        final ThanhPhan.ONhap mo = new ThanhPhan.ONhap(rong(bangSk.getValueAt(r, 4)), 18).goiY("yyyy-MM-dd HH:mm");
        final ThanhPhan.ONhap dong = new ThanhPhan.ONhap(rong(bangSk.getValueAt(r, 5)), 18).goiY("yyyy-MM-dd HH:mm");
        if (!HopThoai.bieuMau(this, "Hẹn giờ sự kiện " + ma, "Để trống = không hẹn. Giờ so theo đồng hồ CSDL (NOW()).",
                HopThoai.luoi("Mở lúc", mo, "Đóng lúc", dong), "Lưu hẹn")) {
            return;
        }
        HopThoai.chay(this, null, () -> ThaoTacMayChu.henSuKien(ma, mo.getText(), dong.getText()), k -> docCham());
    }

    static String rong(final Object o) {
        return o == null || "—".equals(o) ? "" : String.valueOf(o).replaceAll(":00(\\.0)?$", "");
    }

    void boss(final boolean mo) {
        final int r = bangBoss.getSelectedRow();
        if (r < 0) {
            HopThoai.baoNhanh.accept("Chọn một boss.");
            return;
        }
        final int i = ((Number) bangBoss.getValueAt(r, 0)).intValue() - 1;
        HopThoai.chay(this, (mo ? "Gọi" : "Tắt") + " boss \"" + bangBoss.getValueAt(r, 1) + "\"?", () -> ThaoTacMayChu.moBoss(i, mo),
                k -> docCham());
    }

    void docCham() {
        lanDoc = System.currentTimeMillis();
        HopThoai.doc(() -> {
            final Object[] kq = new Object[4];
            kq[0] = ThaoTacMayChu.dsSuKien();
            kq[1] = ThaoTacMayChu.dsBoss();
            List<String> st;
            try {
                st = com.htbb.pokemon.QuanTriSinhThai.trangThai();
            } catch (final Throwable t) {
                st = new ArrayList<String>();
            }
            kq[2] = st;
            kq[3] = ThaoTacMayChu.docServerIni().getProperty("sv.ngayChiemThanh", "");
            return kq;
        }, this::hien);
    }

    @SuppressWarnings("unchecked")
    void hien(final Object[] kq) {
        final List<Object[]> sk = (List<Object[]>) kq[0];
        final int giu = bangSk.getSelectedRow();
        moHinhSk.setRowCount(0);
        lich.xoa();
        if (ThaoTacMayChu.loiTruyVan(sk) == null) {
            final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 1; i < sk.size(); i++) {
                final Object[] r = sk.get(i);
                moHinhSk.addRow(new Object[]{r[1] == null ? r[0] : r[1], r[2], so(r[3]) == 1 ? "Bật" : "Tắt", so(r[4]) == 1 ? "Đang chạy" : "Tắt",
                    gio(r[7]), gio(r[8]), r[0]});
                for (final int k : new int[]{7, 8}) {
                    if (r[k] != null) {
                        try {
                            lich.them(f.parse(String.valueOf(r[k]).replaceAll("\\.0$", "")).getTime(), (k == 7 ? "mở " : "đóng ") + r[1]);
                        } catch (final Exception ignored) {
                        }
                    }
                }
            }
        }
        // giai dau tuan thu Bay + lich chiem thanh (sv.ngayChiemThanh = thu trong tuan, 1 = CN .. 7 = T7)
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_YEAR, 1);
        final int nam = c.get(Calendar.YEAR);
        int ngayCt = -1;
        try {
            ngayCt = Integer.parseInt(String.valueOf(kq[3]).trim());
        } catch (final Exception ignored) {
        }
        while (c.get(Calendar.YEAR) == nam) {
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                lich.them(c.getTimeInMillis(), "giải đấu tuần 20:00");
            }
            if (ngayCt >= 1 && ngayCt <= 7 && c.get(Calendar.DAY_OF_WEEK) == ngayCt) {
                lich.them(c.getTimeInMillis(), "chiếm Gym");
            }
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        lich.repaint();
        if (giu >= 0 && giu < bangSk.getRowCount()) {
            bangSk.setRowSelectionInterval(giu, giu);
        }
        final int giuB = bangBoss.getSelectedRow();
        moHinhBoss.setRowCount(0);
        for (final Object[] r : (List<Object[]>) kq[1]) {
            moHinhBoss.addRow(r);
        }
        if (giuB >= 0 && giuB < bangBoss.getRowCount()) {
            bangBoss.setRowSelectionInterval(giuB, giuB);
        }
        trangThaiTg.removeAll();
        for (final String s0 : (List<String>) kq[2]) {
            trangThaiTg.add(ThanhPhan.doanVan(s0, true));
        }
        GiaoDien.toCay(trangThaiTg);
        revalidate();
        repaint();
    }

    /** "2026-11-16 00:00:00.0" -> "2026-11-16 00:00"; null -> "—". */
    static String gio(final Object o) {
        if (o == null) {
            return "—";
        }
        final String s0 = String.valueOf(o);
        return s0.length() >= 16 ? s0.substring(0, 16) : s0;
    }

    static int so(final Object o) {
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }

    /** Xem truoc thong bao kieu trong game (khung toi, chu vang / hop thoai). */
    final class XemTruoc extends JComponent {
        XemTruoc() {
            setPreferredSize(new Dimension(s(320), s(118)));
        }

        @Override
        protected void paintComponent(final Graphics g0) {
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            final int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, new Color(0x5DBB63), 0, h, new Color(0x2E7D32)));
            g.fill(new RoundRectangle2D.Float(0, 0, w, h, s(14), s(14)));
            // co nho trang tri
            g.setColor(new Color(255, 255, 255, 30));
            for (int i = 0; i < 9; i++) {
                g.fillOval((i * 53) % w, h - s(18) - (i % 3) * s(10), s(18), s(8));
            }
            final String nd = noiDung.getText().trim().isEmpty() ? "Nội dung thông báo hiện ở đây…" : noiDung.getText().trim();
            final int k = kieuChon();
            g.setFont(GiaoDien.dam(12));
            if (k == 0) {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, s(10), w, s(24));
                g.setColor(GiaoDien.VANG);
                g.drawString(ThanhPhan.catChu(g, nd, w - s(20)), s(10), s(27));
            } else {
                final int bw = Math.min(w - s(40), s(260)), bh = s(64);
                final int x = (w - bw) / 2, y = k == 1 ? (h - bh) / 2 : h - bh - s(8);
                g.setColor(new Color(0xF7F3E3));
                g.fill(new RoundRectangle2D.Float(x, y, bw, bh, s(10), s(10)));
                g.setColor(new Color(0x3B4CCA));
                g.setStroke(new java.awt.BasicStroke(GiaoDien.sf(2.5f)));
                g.draw(new RoundRectangle2D.Float(x, y, bw, bh, s(10), s(10)));
                g.setColor(new Color(0x1D2333));
                g.setFont(GiaoDien.font(11.5f));
                final List<String> dong = boDong(g, nd, bw - s(20));
                for (int i = 0; i < Math.min(3, dong.size()); i++) {
                    g.drawString(dong.get(i), x + s(10), y + s(20) + i * s(15));
                }
            }
            g.setFont(GiaoDien.dam(10).deriveFont(Font.BOLD));
            g.setColor(new Color(255, 255, 255, 200));
            g.drawString("Xem trước".toUpperCase(), w - s(72), h - s(8));
            g.dispose();
        }
    }

    static List<String> boDong(final Graphics2D g, final String s0, final int rong) {
        final List<String> l = new ArrayList<String>();
        String dong = "";
        for (final String tu : s0.split("\\s+")) {
            final String thu = dong.isEmpty() ? tu : dong + " " + tu;
            if (g.getFontMetrics().stringWidth(thu) > rong && !dong.isEmpty()) {
                l.add(dong);
                dong = tu;
            } else {
                dong = thu;
            }
        }
        if (!dong.isEmpty()) {
            l.add(dong);
        }
        return l;
    }
}
