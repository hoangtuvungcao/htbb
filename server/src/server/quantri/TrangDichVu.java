package server.quantri;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import static server.quantri.GiaoDien.s;

/** Trang 7 - Giftcode an toan, Thanh vien, Nap tien: tom tat chi-doc + thao tac da co san phia server (an toan). */
public final class TrangDichVu extends Trang {

    private final DefaultTableModel mhGc = new DefaultTableModel(new Object[]{"Mã", "Phần thưởng", "Hạn", "Lượt", "Đã dùng", "Cấp",
        "Trạng thái", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(final int r, final int c) {
            return false;
        }
    };
    private final JTable bangGc = new ThanhPhan.Bang(mhGc).rong("Chưa có giftcode nào — bấm \"Tạo mã an toàn…\" để tạo mã đầu tiên.");
    private final ThanhDo nganSach = new ThanhDo();
    private final JPanel tv = danhSach();
    private final JPanel nap = danhSach();
    private final DefaultTableModel mhNap = new DefaultTableModel(new Object[]{"Mã đơn", "Tài khoản", "Gói", "Số tiền", "Lượng",
        "Trạng thái", "Tạo lúc"}, 0) {
        @Override
        public boolean isCellEditable(final int r, final int c) {
            return false;
        }
    };
    private long lanDoc;

    public TrangDichVu() {
        super("Giftcode · Thành viên · Nạp tiền", "Tóm tắt chỉ đọc; thao tác chỉ đi qua đường an toàn sẵn có (giftcode chỉ vật phẩm khoá / lượng khoá).");
        // ---- giftcode
        final ThanhPhan.The theGc = new ThanhPhan.The("Giftcode an toàn", TrangTongQuan.icon("Box_Red")).nhan(GiaoDien.DO);
        ThanhPhan.kieuBang(bangGc);
        bangGc.setAutoCreateRowSorter(true);
        bangGc.getColumnModel().getColumn(6).setCellRenderer(ThanhPhan.oHuyHieu());
        final ThanhPhan.Nut tao = new ThanhPhan.Nut("Tạo mã an toàn…", ThanhPhan.Kieu.DO).nho();
        tao.addActionListener(e -> taoMa());
        final ThanhPhan.Nut bat = new ThanhPhan.Nut("Bật", ThanhPhan.Kieu.LA).nho();
        bat.addActionListener(e -> datMa(true));
        final ThanhPhan.Nut tat = new ThanhPhan.Nut("Tắt", ThanhPhan.Kieu.MO).nho();
        tat.addActionListener(e -> datMa(false));
        final JPanel gc = ThanhPhan.trongSuot(new BorderLayout(0, s(10)));
        gc.add(nganSach, BorderLayout.NORTH);
        final JScrollPane sp = ThanhPhan.cuon(bangGc);
        sp.setPreferredSize(new Dimension(s(700), s(190)));
        final int[] rongGc = {120, 180, 140, 70, 80, 60, 100, 220};
        for (int i = 0; i < rongGc.length; i++) {
            bangGc.getColumnModel().getColumn(i).setPreferredWidth(s(rongGc[i]));
        }
        gc.add(sp, BorderLayout.CENTER);
        final JPanel duoiGc = ThanhPhan.trongSuot(new BorderLayout(0, s(4)));
        duoiGc.add(ThanhPhan.hang(FlowLayout.LEFT, tao, bat, tat), BorderLayout.NORTH);
        duoiGc.add(ThanhPhan.doanVan("Mã do web hoặc bảng điều khiển tạo; người chơi nhập trong game, máy chủ kiểm lại mọi luật lúc phát.",
                true), BorderLayout.SOUTH);
        gc.add(duoiGc, BorderLayout.SOUTH);
        theGc.noiDung(gc);

        // ---- thanh vien
        final ThanhPhan.The theTv = new ThanhPhan.The("Thành viên HTBB", TrangTongQuan.icon("Crown_Yellow")).nhan(GiaoDien.VANG);
        theTv.noiDung(tv);

        // ---- nap tien
        final ThanhPhan.The theNap = new ThanhPhan.The("Nạp tiền", TrangTongQuan.icon("Money_Yellow")).nhan(GiaoDien.XANH_LA);
        final JTable bn = new ThanhPhan.Bang(mhNap).rong("Chưa có đơn nạp nào.");
        ThanhPhan.kieuBang(bn);
        bn.getColumnModel().getColumn(5).setCellRenderer(ThanhPhan.oHuyHieu());
        final JPanel np = ThanhPhan.trongSuot(new BorderLayout(0, s(10)));
        np.add(nap, BorderLayout.NORTH);
        final JScrollPane sp2 = ThanhPhan.cuon(bn);
        sp2.setPreferredSize(new Dimension(s(500), s(200)));
        np.add(sp2, BorderLayout.CENTER);
        np.add(ThanhPhan.doanVan("Duyệt / hoàn đơn làm trên trang quản trị web (có sổ cái, chống trùng).", true), BorderLayout.SOUTH);
        theNap.noiDung(np);

        thanh.add(cot(theGc, tiLe(new double[]{1, 1.5}, theTv, theNap)), BorderLayout.NORTH);
    }

    @Override
    public String ten() {
        return "Dịch vụ";
    }

    @Override
    public String icon() {
        return "Box_Red";
    }

    @Override
    public void khiHien() {
        doc();
    }

    @Override
    public void nhip(final DuLieuTrucTiep.Mau m) {
        if (System.currentTimeMillis() - lanDoc > 30000L) {
            doc();
        }
    }

    void doc() {
        lanDoc = System.currentTimeMillis();
        HopThoai.doc(() -> new Object[]{
            ThaoTacMayChu.dsGiftcode(), ThaoTacMayChu.nganSachGiftcode(), ThaoTacMayChu.coTinhNang(),
            ThaoTacMayChu.truyVan("SELECT COUNT(*), SUM(CASE WHEN het_han_ms = 0 OR het_han_ms > ? THEN 1 ELSE 0 END) FROM tv_thanh_vien",
                System.currentTimeMillis()),
            ThaoTacMayChu.truyVan("SELECT username, hanh_dong, ngay, luc FROM tv_nhat_ky ORDER BY id DESC LIMIT 5"),
            ThaoTacMayChu.truyVan("SELECT trang_thai, COUNT(*), COALESCE(SUM(so_tien),0) FROM nap_don WHERE tao_luc >= DATE_SUB(NOW(), INTERVAL 30 DAY)"
                + " GROUP BY trang_thai"),
            ThaoTacMayChu.truyVan("SELECT ma_don, username, ten_goi, so_tien, luong, trang_thai, tao_luc FROM nap_don ORDER BY id DESC LIMIT 20"),
            TrangKinhTe.luongNap()}, this::hien);
    }

    @SuppressWarnings("unchecked")
    void hien(final Object[] kq) {
        final java.util.Map<String, Boolean> co = (java.util.Map<String, Boolean>) kq[2];
        final List<Object[]> gc = (List<Object[]>) kq[0];
        mhGc.setRowCount(0);
        if (ThaoTacMayChu.loiTruyVan(gc) == null) {
            for (int i = 1; i < gc.size(); i++) {
                final Object[] r = gc.get(i);
                mhGc.addRow(new Object[]{r[0], r[1], r[2] == null ? "Không hạn" : TrangSuKien.gio(r[2]), r[3], r[4], r[5],
                    TrangSuKien.so(r[6]) == 1 ? (TrangKinhTe.so(r[4]) >= TrangKinhTe.so(r[3]) ? "Hết lượt" : "Bật") : "Tắt", r[7]});
            }
        }
        final long[] ns = (long[]) kq[1];
        nganSach.dat(ns[0], ns[1], "Ngân sách giftcode tháng này" + (Boolean.TRUE.equals(co.get("lau_dai")) ? "" : " (thiết kế dài hạn đang tắt: chưa nhập mã được)"));

        tv.removeAll();
        tv.add(TrangTongQuan.dongCo("thanh_vien.bat", co.get("thanh_vien")));
        final List<Object[]> dem = (List<Object[]>) kq[3];
        if (ThaoTacMayChu.loiTruyVan(dem) == null && dem.size() > 1) {
            tv.add(TrangTongQuan.dongNhan("Tài khoản thành viên còn hạn", DinhDang.so(TrangKinhTe.so(dem.get(1)[1])), GiaoDien.VANG_DAM));
            tv.add(TrangTongQuan.dongNhan("Tổng từng cấp", DinhDang.so(TrangKinhTe.so(dem.get(1)[0])), GiaoDien.XANH_DUONG));
        } else {
            tv.add(ThanhPhan.doanVan("Chưa cài đặt dữ liệu Thành viên.", true));
        }
        final List<Object[]> nk = (List<Object[]>) kq[4];
        if (ThaoTacMayChu.loiTruyVan(nk) == null) {
            for (int i = 1; i < nk.size(); i++) {
                tv.add(ThanhPhan.nhanPhu(nk.get(i)[3] + " · " + nk.get(i)[0] + " · " + nk.get(i)[1] + " " + nk.get(i)[2] + " ngày"));
            }
        }
        tv.add(ThanhPhan.doanVan("Cấp / thu hồi thành viên làm trên trang quản trị web (có sổ cái và nhật ký).", true));

        nap.removeAll();
        nap.add(TrangTongQuan.dongCo("nap_tien (cần cả lau_dai)", co.get("nap_tien")));
        final long[] lv = (long[]) kq[7];
        nap.add(TrangTongQuan.dongNhan("Lượng vào / tiêu từ lúc khởi động", DinhDang.gon(lv[0]) + " / " + DinhDang.gon(lv[1]), GiaoDien.XANH_DUONG));
        final List<Object[]> tt = (List<Object[]>) kq[5];
        if (ThaoTacMayChu.loiTruyVan(tt) == null) {
            if (tt.size() <= 1) {
                nap.add(ThanhPhan.nhanPhu("Không có đơn nạp trong 30 ngày."));
            }
            for (int i = 1; i < tt.size(); i++) {
                nap.add(TrangTongQuan.dongNhan("30 ngày · " + tenTrangThaiNap(String.valueOf(tt.get(i)[0])), DinhDang.so(TrangKinhTe.so(tt.get(i)[1])) + " đơn · "
                        + DinhDang.so(TrangKinhTe.so(tt.get(i)[2])) + "đ", "da_cong".equals(tt.get(i)[0]) ? GiaoDien.XANH_LA : GiaoDien.XANH_DUONG));
            }
        } else {
            nap.add(ThanhPhan.nhanPhu("Chưa cài đặt dữ liệu Nạp tiền."));
        }
        mhNap.setRowCount(0);
        final List<Object[]> dn = (List<Object[]>) kq[6];
        if (ThaoTacMayChu.loiTruyVan(dn) == null) {
            for (int i = 1; i < dn.size(); i++) {
                final Object[] r = dn.get(i).clone();
                r[5] = tenTrangThaiNap(String.valueOf(r[5]));
                r[6] = TrangSuKien.gio(r[6]);
                mhNap.addRow(r);
            }
        }
        GiaoDien.toCay(this);
        revalidate();
        repaint();
    }

    /** Ma trang thai don nap -> nhan de doc (giu ma goc trong ngoac neu la). */
    static String tenTrangThaiNap(final String ma) {
        switch (ma) {
            case "da_cong":
                return "Đã cộng";
            case "da_thanh_toan":
                return "Đã thanh toán";
            case "cho_thanh_toan":
                return "Chờ thanh toán";
            case "huy":
                return "Huỷ";
            case "het_han":
                return "Hết hạn";
            case "hoan_tien":
                return "Hoàn tiền";
            default:
                return ma;
        }
    }

    String maChon() {
        final int r = bangGc.getSelectedRow();
        if (r < 0) {
            HopThoai.baoNhanh.accept("Chọn một mã trong bảng.");
            return null;
        }
        return String.valueOf(bangGc.getValueAt(r, 0));
    }

    void datMa(final boolean b) {
        final String ma = maChon();
        if (ma != null) {
            HopThoai.chay(this, (b ? "Bật" : "Tắt") + " mã " + ma + "?", () -> ThaoTacMayChu.datGiftcode(ma, b), k -> doc());
        }
    }

    void taoMa() {
        final ThanhPhan.ONhap ma = new ThanhPhan.ONhap("", 20).goiY("a-z 0-9 _ - (3-32 ký tự)");
        final ThanhPhan.ONhap qua = new ThanhPhan.ONhap("LUONGKHOA:5", 24);
        final JSpinner luot = new JSpinner(new SpinnerNumberModel(100, 1, 1000000, 10));
        final JSpinner cap = new JSpinner(new SpinnerNumberModel(1, 1, 200, 1));
        final JSpinner ngay = new JSpinner(new SpinnerNumberModel(7, 0, 3650, 1));
        final ThanhPhan.ONhap ghiChu = new ThanhPhan.ONhap("", 24);
        if (!HopThoai.bieuMau(this, "Tạo giftcode an toàn", "Phần thưởng chỉ LUONGKHOA:n, GEM:id:n, ITEM:id:n - giá trị ≤ trần "
                + ThaoTacMayChu.tranQua() + ". Hạn 0 = không hạn.",
                HopThoai.luoi("Mã", ma, "Phần thưởng", qua, "Số lượt", luot, "Cấp tối thiểu", cap, "Hạn (ngày)", ngay, "Ghi chú", ghiChu), "Tạo mã")) {
            return;
        }
        HopThoai.chay(this, "Tạo mã " + ma.getText().trim() + " (" + qua.getText().trim() + ", " + luot.getValue() + " lượt)?",
                () -> ThaoTacMayChu.taoGiftcode(ma.getText(), qua.getText(), (Integer) luot.getValue(), (Integer) cap.getValue(),
                        (Integer) ngay.getValue(), ghiChu.getText()), k -> doc());
    }

    /** Thanh tien do ngan sach. */
    static final class ThanhDo extends JComponent {
        private long dung, tong;
        private String nhan = "";

        ThanhDo() {
            setPreferredSize(new Dimension(s(400), s(44)));
        }

        void dat(final long d, final long t, final String n) {
            dung = d;
            tong = t;
            nhan = n;
            repaint();
        }

        @Override
        protected void paintComponent(final Graphics g0) {
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            g.setFont(GiaoDien.dam(12));
            g.setColor(GiaoDien.chu);
            g.drawString(nhan, 0, s(14));
            final String so = DinhDang.so(dung) + " / " + DinhDang.so(tong);
            g.drawString(so, getWidth() - g.getFontMetrics().stringWidth(so), s(14));
            final float y = s(22), h = s(14);
            g.setColor(GiaoDien.nenThe2);
            g.fill(new RoundRectangle2D.Float(0, y, getWidth(), h, h, h));
            final float p = tong <= 0 ? 0 : Math.min(1f, dung / (float) tong);
            g.setColor(p > 0.9f ? GiaoDien.DO : p > 0.7f ? GiaoDien.VANG_DAM : GiaoDien.XANH_LA);
            g.fill(new RoundRectangle2D.Float(0, y, Math.max(h, getWidth() * p), h, h, h));
            g.setColor(GiaoDien.vien);
            g.setStroke(new BasicStroke(1f));
            g.draw(new RoundRectangle2D.Float(0.5f, y, getWidth() - 1, h, h, h));
            g.dispose();
        }
    }

    static JLabel l(final String s0) {
        return ThanhPhan.nhan(s0);
    }
}
