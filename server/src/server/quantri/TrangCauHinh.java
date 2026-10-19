package server.quantri;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import static server.quantri.GiaoDien.s;

/** Trang 6 - Cau hinh: xem / sua khoa cau hinh (qua NguonCauHinh), huy hieu "can khoi dong lai", co tinh nang. */
public final class TrangCauHinh extends Trang {

    private final DefaultTableModel mh = new DefaultTableModel(new Object[]{"Nhóm", "Tên", "Giá trị", "Áp dụng", "Nguồn",
        "Sửa bởi", "Sửa lúc"}, 0) {
        @Override
        public boolean isCellEditable(final int r, final int c) {
            return false;
        }
    };
    private final JTable bang = new ThanhPhan.Bang(mh) {
        @Override
        public String getToolTipText(final java.awt.event.MouseEvent e) {
            final int r = rowAtPoint(e.getPoint());
            if (r < 0) {
                return null;
            }
            final NguonCauHinh.Muc m = ds.get(convertRowIndexToModel(r));
            return "<html><b>" + TrangTongQuan.esc(m.ten.isEmpty() ? m.khoa : m.ten) + "</b><br>" + TrangTongQuan.esc(m.moTa)
                    + "<br><span style='color:gray'>Khoá: " + TrangTongQuan.esc(m.khoa) + " · mặc định: " + TrangTongQuan.esc(m.macDinh)
                    + "</span></html>";
        }
    }.rong("Đang tải danh mục cấu hình…");
    private final TableRowSorter<DefaultTableModel> sx = new TableRowSorter<DefaultTableModel>(mh);
    private final ThanhPhan.ONhap tim = new ThanhPhan.ONhap("", 20).goiY("Lọc theo tên / nhóm…");
    private final JLabel cheDo = ThanhPhan.nhanPhu("");
    private final JPanel co = ThanhPhan.hang(java.awt.FlowLayout.LEFT);
    private final List<NguonCauHinh.Muc> ds = new ArrayList<NguonCauHinh.Muc>();

    public TrangCauHinh() {
        super("Cấu hình", "Khoá cấu hình chỉnh được lúc chạy - kiểm giá trị trước khi lưu, ghi kiểm toán, đánh dấu khoá cần khởi động lại.");
        ThanhPhan.kieuBang(bang);
        bang.setRowSorter(sx);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getColumnModel().getColumn(3).setCellRenderer(ThanhPhan.oHuyHieu());
        final int[] rong = {130, 260, 150, 150, 110, 130, 140};
        for (int i = 0; i < rong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(s(rong[i]));
        }
        bang.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(final java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    sua();
                }
            }
        });
        tim.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                loc();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                loc();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                loc();
            }
        });
        final ThanhPhan.Nut sua = new ThanhPhan.Nut("Sửa giá trị…", ThanhPhan.Kieu.XANH);
        sua.addActionListener(e -> sua());
        final ThanhPhan.Nut tai = new ThanhPhan.Nut("Tải lại", ThanhPhan.Kieu.MO);
        tai.addActionListener(e -> khiHien());
        final ThanhPhan.Nut nap = new ThanhPhan.Nut("Nạp lại ngay", ThanhPhan.Kieu.MO);
        nap.addActionListener(e -> HopThoai.chay(this, null, ThaoTacMayChu::napLaiCauHinh, k -> khiHien()));
        benPhai.add(nap);
        benPhai.add(tai);

        final ThanhPhan.The the = new ThanhPhan.The("Khoá cấu hình", TrangTongQuan.icon("Key_White")).nhan(GiaoDien.XANH_DUONG);
        final JPanel dau = ThanhPhan.trongSuot(new BorderLayout(s(8), 0));
        dau.add(tim, BorderLayout.CENTER);
        dau.add(ThanhPhan.hang(FlowLayout.RIGHT, cheDo, sua), BorderLayout.EAST);
        final JPanel than = ThanhPhan.trongSuot(new BorderLayout(0, s(10)));
        than.add(dau, BorderLayout.NORTH);
        final JScrollPane sp = ThanhPhan.cuon(bang);
        sp.setPreferredSize(new Dimension(s(800), s(430)));
        than.add(sp, BorderLayout.CENTER);
        than.add(ThanhPhan.doanVan("Nháy đúp một dòng để sửa. \"Nóng\" = máy chủ áp dụng trong vài giây; \"Cần khởi động lại\" = lưu"
                + " ngay nhưng chỉ có hiệu lực sau lần khởi động lại.", true), BorderLayout.SOUTH);
        the.noiDung(than);

        final ThanhPhan.The theCo = new ThanhPhan.The("Cờ tính năng", TrangTongQuan.icon("Crown_Yellow")).nhan(GiaoDien.VANG);
        final JPanel thanCo = ThanhPhan.trongSuot(new BorderLayout(0, s(8)));
        thanCo.add(co, BorderLayout.CENTER);
        thanCo.add(ThanhPhan.doanVan("Đọc một lần lúc khởi động. Đổi tính năng = sửa cấu hình tương ứng rồi khởi động lại"
                + " (trang Vận hành).", true), BorderLayout.SOUTH);
        theCo.noiDung(thanCo);
        thanh.add(cot(theCo, the), BorderLayout.NORTH);
    }

    @Override
    public String ten() {
        return "Cấu hình";
    }

    @Override
    public String icon() {
        return "Key_White";
    }

    void loc() {
        final String t = tim.getText().trim();
        sx.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(t)));
    }

    @Override
    public void khiHien() {
        HopThoai.doc(() -> new Object[]{NguonCauHinh.doc(), NguonCauHinh.cheDo(), ThaoTacMayChu.coTinhNang()}, kq -> hien(kq));
    }

    @SuppressWarnings("unchecked")
    void hien(final Object[] kq) {
        ds.clear();
        ds.addAll((List<NguonCauHinh.Muc>) kq[0]);
        cheDo.setText("Nguồn: " + kq[1]);
        mh.setRowCount(0);
        for (final NguonCauHinh.Muc m : ds) {
            final String ap = m.chiDoc ? "Chỉ đọc" : m.cho != null ? "Chờ khởi động lại" : m.apDungNgay ? "Nóng (≤10s)" : "Cần khởi động lại";
            mh.addRow(new Object[]{m.nhom, m.ten.isEmpty() ? m.khoa : m.ten, m.nhay ? "••••••" : m.giaTri
                + (m.cho != null ? "  → " + m.cho : ""), ap, tenNguon(m.nguon), "khoi-dong-lan-dau".equals(m.suaBoi) ? "Khởi tạo" : m.suaBoi,
                TrangSuKien.gio(m.suaLuc.isEmpty() ? null : m.suaLuc)});
        }
        co.removeAll();
        for (final Map.Entry<String, Boolean> e : ((Map<String, Boolean>) kq[2]).entrySet()) {
            final Boolean b = e.getValue();
            co.add(new ThanhPhan.HuyHieu(ThaoTacMayChu.tenCo(e.getKey()) + ": " + (b == null ? "chưa có" : b ? "bật" : "tắt"),
                    b == null ? GiaoDien.chuMo : b ? GiaoDien.XANH_LA : GiaoDien.DO));
        }
        GiaoDien.toCay(co);
        revalidate();
        repaint();
    }

    void sua() {
        final int r = bang.getSelectedRow();
        if (r < 0) {
            HopThoai.baoNhanh.accept("Chọn một khoá trong bảng.");
            return;
        }
        final NguonCauHinh.Muc m = ds.get(bang.convertRowIndexToModel(r));
        if (m.chiDoc) {
            HopThoai.ketQua(this, "Khoá chỉ đọc", m.khoa + " không sửa được ở đây (biến môi trường / bảng khác / nhạy cảm).");
            return;
        }
        final JComponent o;
        final ThanhPhan.ONhap chu = new ThanhPhan.ONhap(m.nhay ? "" : m.giaTri, 28);
        final JComboBox<String> chon = new JComboBox<String>(m.luaChon.length > 0 ? m.luaChon
                : "co".equals(m.kieu) ? new String[]{"0", "1"} : new String[0]);
        final boolean laChon = chon.getItemCount() > 0;
        if (laChon) {
            chon.setSelectedItem(m.giaTri);
            o = chon;
        } else {
            o = chu;
        }
        final JLabel kiem = ThanhPhan.nhanPhu(" ");
        final ThanhPhan.ONhap lyDo = new ThanhPhan.ONhap("", 28).goiY("lý do (ghi lịch sử)");
        chu.getDocument().addDocumentListener(new DocumentListener() {
            void k() {
                final String l = NguonCauHinh.kiem(m, chu.getText());
                kiem.setText(l == null ? "Hợp lệ" : "Không hợp lệ: " + l);
                kiem.setForeground(l == null ? GiaoDien.XANH_LA_DAM : GiaoDien.DO);
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                k();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                k();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                k();
            }
        });
        final String gioiHan = "so".equals(m.kieu) && m.min != Long.MIN_VALUE ? " · khoảng " + m.min + " … " + m.max : "";
        final JPanel p = HopThoai.luoi("Giá trị", o, "", kiem, "Lý do", lyDo, "Kiểu", ThanhPhan.nhanPhu(m.kieu + gioiHan
                + (m.macDinh.isEmpty() ? "" : " · mặc định " + m.macDinh)), "Áp dụng", new ThanhPhan.HuyHieu(m.apDungNgay
                ? "Nóng (≤10s)" : "Cần khởi động lại", m.apDungNgay ? GiaoDien.XANH_LA : GiaoDien.VANG_DAM));
        if (!HopThoai.bieuMau(this, "Sửa " + m.khoa, m.moTa.isEmpty() ? m.ten : m.moTa, p, "Lưu")) {
            return;
        }
        final String v = laChon ? String.valueOf(chon.getSelectedItem()) : chu.getText();
        final String loi = NguonCauHinh.kiem(m, v);
        if (loi != null) {
            HopThoai.ketQua(this, "Giá trị không hợp lệ", loi);
            return;
        }
        HopThoai.chay(this, "Đổi " + m.khoa + "\n" + (m.nhay ? "" : m.giaTri + " → ") + (m.nhay ? "(giá trị mới)" : v.trim())
                + (m.apDungNgay ? "" : "\n(Có hiệu lực sau khi khởi động lại.)"), () -> NguonCauHinh.ghi(m, v, lyDo.getText().trim()),
                k -> khiHien());
    }

    static String tenNguon(final String n) {
        switch (n) {
            case "db":
                return "Đã chỉnh";
            case "ini":
                return "Tệp khởi động";
            case "mac_dinh":
                return "Mặc định";
            case "giu_cu":
                return "Giữ giá trị cũ";
            case "co_dinh":
                return "Cố định";
            default:
                return n;
        }
    }

    static Component rong() {
        return ThanhPhan.trongRong("");
    }
}
