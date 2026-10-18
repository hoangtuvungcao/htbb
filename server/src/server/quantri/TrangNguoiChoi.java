package server.quantri;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import real.Char;

import static server.quantri.GiaoDien.s;

/**
 * Trang 2 - Nguoi choi: bang online tim/sap xep duoc, the chi tiet, moi thao tac (kick, cam, cam chat,
 * ve thi tran, dich chuyen, thu, doi mat khau, tang qua an toan, giam level, lich su dang nhap),
 * tim offline qua CSDL, va "cong cu cu" (menu ButtonEditor: ruong, buff, mat khau ruong...).
 */
public final class TrangNguoiChoi extends Trang {

    static final String[] COT = {"", "Tên", "Loài", "Cấp", "Bản đồ", "IP", "Thiết bị", "Tài khoản", "Online"};

    private final server.AdminPanel khung;
    private final MoHinh moHinh = new MoHinh();
    private final JTable bang = new ThanhPhan.Bang(moHinh).rong("Chưa có người chơi nào online.");
    private final TableRowSorter<MoHinh> sapXep = new TableRowSorter<MoHinh>(moHinh);
    private final ThanhPhan.ONhap oTim = new ThanhPhan.ONhap("", 18).goiY("Tìm tên, tài khoản, IP, bản đồ, Pokémon…");
    private final JLabel demOnline = ThanhPhan.nhanPhu("");
    private final JPanel chiTiet = ThanhPhan.trongSuot(new BorderLayout());
    private final Map<Integer, ImageIcon> iconLoai = new HashMap<Integer, ImageIcon>();
    private String dangChon;
    private long lanLamMoi;
    private server.Panel.ButtonEditor congCuCu;

    // offline
    private final DefaultTableModel moHinhOff = new DefaultTableModel(new Object[]{"ID", "Pokémon", "Tài khoản", "Cấp", "Xu",
        "Lượng", "Lượng khoá", "Cấm", "Lần cuối", "Dex"}, 0) {
        @Override
        public boolean isCellEditable(final int r, final int c) {
            return false;
        }
    };
    private final JTable bangOff = new ThanhPhan.Bang(moHinhOff).rong("Nhập tên Pokémon hoặc tài khoản rồi bấm Tìm.");
    private final ThanhPhan.ONhap oTimOff = new ThanhPhan.ONhap("", 18).goiY("Tên Pokémon hoặc tài khoản (CSDL)");

    public TrangNguoiChoi(final server.AdminPanel khung) {
        super("Người chơi", "Danh sách online, chi tiết Pokémon và mọi thao tác quản lý - đều có xác nhận và ghi kiểm toán.");
        this.khung = khung;
        ThanhPhan.kieuBang(bang);
        bang.setRowHeight(s(36));
        bang.setRowSorter(sapXep);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getColumnModel().getColumn(0).setMaxWidth(s(44));
        bang.getColumnModel().getColumn(0).setMinWidth(s(44));
        bang.getColumnModel().getColumn(0).setCellRenderer(new OIcon());
        bang.getColumnModel().getColumn(1).setCellRenderer(new OTen());
        bang.getColumnModel().getColumn(3).setMaxWidth(s(56));
        final int[] rong = {44, 110, 120, 44, 80, 90, 120, 90, 76};
        for (int i = 1; i < rong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(s(rong[i]));
        }
        bang.getColumnModel().getColumn(8).setCellRenderer(new OThoiGian());
        sapXep.setSortable(0, false);
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                final int r = bang.getSelectedRow();
                if (r >= 0) {
                    dangChon = (String) bang.getValueAt(r, 1);
                    veChiTiet();
                }
            }
        });
        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.isPopupTrigger() || javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    final int r = bang.rowAtPoint(e.getPoint());
                    if (r >= 0) {
                        bang.setRowSelectionInterval(r, r);
                        moCongCuCu(bang, e.getX(), e.getY());
                    }
                }
            }
        });
        oTim.getDocument().addDocumentListener(new DocumentListener() {
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

        final ThanhPhan.Nut nutLamMoi = new ThanhPhan.Nut("Làm mới", ThanhPhan.Kieu.MO, TrangTongQuan.icon("Orb_LightBlue"));
        nutLamMoi.addActionListener(e -> lamMoi());
        final ThanhPhan.Nut nutGiamHangLoat = new ThanhPhan.Nut("Giảm level hàng loạt", ThanhPhan.Kieu.MO, TrangTongQuan.icon("Stone_Red"));
        nutGiamHangLoat.addActionListener(e -> giamLevelHangLoat());
        final ThanhPhan.Nut nutNgoc = new ThanhPhan.Nut("Công cụ ngọc", ThanhPhan.Kieu.MO, TrangTongQuan.icon("Crystal_Blue"));
        nutNgoc.addActionListener(e -> CongCuNgoc.hien(this, khung));
        final ThanhPhan.Nut nutCam = new ThanhPhan.Nut("Đang bị khoá", ThanhPhan.Kieu.MO, TrangTongQuan.icon("Key_Brown"));
        nutCam.addActionListener(e -> xemDsCam());
        final ThanhPhan.Nut nutMk = new ThanhPhan.Nut("Đổi mật khẩu TK", ThanhPhan.Kieu.MO, TrangTongQuan.icon("Key_White"));
        nutMk.addActionListener(e -> doiMatKhau(""));
        benPhai.add(nutMk);
        benPhai.add(nutCam);
        benPhai.add(nutNgoc);
        benPhai.add(nutGiamHangLoat);

        // ---- the danh sach online
        final ThanhPhan.The theDs = new ThanhPhan.The("Đang online", TrangTongQuan.icon("Heart_Pink")).nhan(GiaoDien.DO);
        final JPanel dauDs = ThanhPhan.trongSuot(new BorderLayout(s(8), 0));
        dauDs.add(oTim, BorderLayout.CENTER);
        dauDs.add(ThanhPhan.hang(FlowLayout.RIGHT, demOnline, nutLamMoi), BorderLayout.EAST);
        final JPanel thanDs = ThanhPhan.trongSuot(new BorderLayout(0, s(10)));
        thanDs.add(dauDs, BorderLayout.NORTH);
        final JScrollPane spDs = ThanhPhan.cuon(bang);
        spDs.setPreferredSize(new Dimension(s(600), s(340)));
        thanDs.add(spDs, BorderLayout.CENTER);
        thanDs.add(ThanhPhan.doanVan("Chuột phải một dòng: công cụ cũ (rương, buff xu / vật phẩm / EXP, mật khẩu rương).", true),
                BorderLayout.SOUTH);
        theDs.noiDung(thanDs);

        // ---- the chi tiet
        final ThanhPhan.The theCt = new ThanhPhan.The("Chi tiết Pokémon", TrangTongQuan.icon("Specs_Red")).nhan(GiaoDien.XANH_DUONG);
        chiTiet.add(ThanhPhan.trongRong("Chọn một người chơi trong bảng."), BorderLayout.CENTER);
        chiTiet.setPreferredSize(new Dimension(s(380), s(480)));
        theCt.noiDung(chiTiet);

        // ---- the offline
        final ThanhPhan.The theOff = new ThanhPhan.The("Tìm Pokémon / tài khoản (cả offline)", TrangTongQuan.icon("Paper_News"))
                .nhan(GiaoDien.VANG);
        ThanhPhan.kieuBang(bangOff);
        bangOff.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bangOff.setAutoCreateRowSorter(true);
        final ThanhPhan.Nut nutTim = new ThanhPhan.Nut("Tìm", ThanhPhan.Kieu.XANH);
        nutTim.addActionListener(e -> timOffline());
        oTimOff.addActionListener(e -> timOffline());
        final ThanhPhan.Nut oCam = new ThanhPhan.Nut("Khoá", ThanhPhan.Kieu.DO).nho();
        oCam.addActionListener(e -> {
            final String t = chonOff(1);
            if (t != null) {
                camNhanVat(t);
            }
        });
        final ThanhPhan.Nut oMo = new ThanhPhan.Nut("Mở khoá", ThanhPhan.Kieu.LA).nho();
        oMo.addActionListener(e -> {
            final String t = chonOff(1);
            if (t != null) {
                HopThoai.chay(this, "Mở khoá Pokémon " + t + "?", () -> ThaoTacMayChu.moCam(t), k -> timOffline());
            }
        });
        final ThanhPhan.Nut oThu = new ThanhPhan.Nut("Gửi thư", ThanhPhan.Kieu.MO).nho();
        oThu.addActionListener(e -> {
            final String t = chonOff(1);
            if (t != null) {
                guiThu(t);
            }
        });
        final ThanhPhan.Nut oLs = new ThanhPhan.Nut("Lịch sử đăng nhập", ThanhPhan.Kieu.MO).nho();
        oLs.addActionListener(e -> {
            final String t = chonOff(2);
            if (t != null) {
                lichSu(t);
            }
        });
        final ThanhPhan.Nut oMk = new ThanhPhan.Nut("Đổi mật khẩu", ThanhPhan.Kieu.MO).nho();
        oMk.addActionListener(e -> {
            final String t = chonOff(2);
            if (t != null) {
                doiMatKhau(t);
            }
        });
        final JPanel dauOff = ThanhPhan.trongSuot(new BorderLayout(s(8), 0));
        dauOff.add(oTimOff, BorderLayout.CENTER);
        dauOff.add(nutTim, BorderLayout.EAST);
        final JPanel thanOff = ThanhPhan.trongSuot(new BorderLayout(0, s(10)));
        thanOff.add(dauOff, BorderLayout.NORTH);
        final JScrollPane spOff = ThanhPhan.cuon(bangOff);
        spOff.setPreferredSize(new Dimension(s(600), s(220)));
        thanOff.add(spOff, BorderLayout.CENTER);
        thanOff.add(ThanhPhan.hang(FlowLayout.LEFT, oCam, oMo, oThu, oMk, oLs), BorderLayout.SOUTH);
        theOff.noiDung(thanOff);

        thanh.add(cot(tiLe(new double[]{1.65, 1}, theDs, theCt), theOff), BorderLayout.NORTH);
    }

    @Override
    public String ten() {
        return "Người chơi";
    }

    @Override
    public String icon() {
        return "Heart_Pink";
    }

    @Override
    public void khiHien() {
        lamMoi();
    }

    @Override
    public void nhip(final DuLieuTrucTiep.Mau m) {
        if (System.currentTimeMillis() - lanLamMoi >= 3000L) {
            lamMoi();
        }
    }

    // ================================================================== bang online

    static final class Dong {
        final String ten, loai, map, ip, thietBi, taiKhoan;
        final int dex, cap;
        final long luc;

        Dong(final Char c) {
            ten = c.charname;
            dex = c.pokeDex;
            loai = ThaoTacMayChu.tenLoai(c);
            cap = c.lvDetail == null ? 0 : c.lvDetail.lv;
            final String tm = ThaoTacMayChu.tenMap(c.mapID);
            map = tm.startsWith("Map ") ? "#" + c.mapID : tm + " #" + c.mapID;
            final io.Session ss = c.getSession();
            ip = ss == null ? "" : ss.ip;
            thietBi = ss == null ? "" : thietBi(ss);
            taiKhoan = ss == null ? "" : ss.username;
            luc = ss == null ? System.currentTimeMillis() : ss.connectTime;
        }
    }

    static String thietBi(final io.Session ss) {
        final StringBuilder b = new StringBuilder();
        b.append("v").append(ss.version);
        if (ss.w > 0 && ss.h > 0) {
            b.append(" · ").append(ss.w).append("×").append(ss.h);
        }
        if (ss.IMEI != null && !ss.IMEI.isEmpty()) {
            b.append(" · ").append(ss.IMEI.length() > 8 ? ss.IMEI.substring(0, 8) + "…" : ss.IMEI);
        }
        return b.toString();
    }

    final class MoHinh extends AbstractTableModel {
        List<Dong> dong = new ArrayList<Dong>();

        @Override
        public int getRowCount() {
            return dong.size();
        }

        @Override
        public int getColumnCount() {
            return COT.length;
        }

        @Override
        public String getColumnName(final int c) {
            return COT[c];
        }

        @Override
        public Class<?> getColumnClass(final int c) {
            return c == 0 ? Icon.class : c == 3 ? Integer.class : c == 8 ? Long.class : String.class;
        }

        @Override
        public Object getValueAt(final int r, final int c) {
            final Dong d = dong.get(r);
            switch (c) {
                case 0:
                    return iconLoai(d.dex);
                case 1:
                    return d.ten;
                case 2:
                    return d.loai.isEmpty() ? "—" : d.loai + " #" + d.dex;
                case 3:
                    return d.cap;
                case 4:
                    return d.map;
                case 5:
                    return d.ip;
                case 6:
                    return d.thietBi;
                case 7:
                    return d.taiKhoan;
                default:
                    return d.luc;
            }
        }
    }

    ImageIcon iconLoai(final int dex) {
        ImageIcon i = iconLoai.get(dex);
        if (i == null) {
            i = new ImageIcon(TrangTongQuan.avatar(dex, s(28)));
            iconLoai.put(dex, i);
        }
        return i;
    }

    void lamMoi() {
        lanLamMoi = System.currentTimeMillis();
        HopThoai.doc(() -> {
            final List<Dong> l = new ArrayList<Dong>();
            for (final Char c : ThaoTacMayChu.nguoiChoiOnline()) {
                try {
                    l.add(new Dong(c));
                } catch (final Throwable ignored) {
                }
            }
            return l;
        }, l -> {
            final String giu = dangChon;
            moHinh.dong = l;
            moHinh.fireTableDataChanged();
            demOnline.setText(l.size() + " người");
            if (giu != null) {
                for (int i = 0; i < bang.getRowCount(); i++) {
                    if (giu.equals(bang.getValueAt(i, 1))) {
                        bang.setRowSelectionInterval(i, i);
                        break;
                    }
                }
                veChiTiet();
            }
        });
    }

    void loc() {
        final String t = oTim.getText().trim();
        sapXep.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(t)));
    }

    // ================================================================== chi tiet

    void veChiTiet() {
        final Char c = ThaoTacMayChu.tim(dangChon);
        chiTiet.removeAll();
        if (c == null) {
            chiTiet.add(ThanhPhan.trongRong(dangChon == null ? "Chọn một người chơi trong bảng." : dangChon + " đã offline."),
                    BorderLayout.CENTER);
            chiTiet.revalidate();
            chiTiet.repaint();
            return;
        }
        final JPanel p = ThanhPhan.trongSuot(new BorderLayout(0, s(10)));
        // dau: avatar + ten
        final JPanel dau = ThanhPhan.trongSuot(new BorderLayout(s(12), 0));
        dau.add(new JLabel(new ImageIcon(TrangTongQuan.avatar(c.pokeDex, s(68)))), BorderLayout.WEST);
        final JPanel ten = ThanhPhan.trongSuot(new GridLayout(0, 1, 0, s(2)));
        ten.add(ThanhPhan.tieuDe(c.charname, 18));
        final String loai = ThaoTacMayChu.tenLoai(c);
        ten.add(ThanhPhan.nhanPhu((loai.isEmpty() ? "" : loai + " · ") + "Cấp " + (c.lvDetail == null ? "?" : c.lvDetail.lv
                + " + " + String.format("%.1f", c.lvDetail.percent / 10.0) + "%")));
        final JPanel hh = ThanhPhan.trongSuot(new FlowLayout(FlowLayout.LEFT, s(4), 0));
        if (c.isAdmin) {
            hh.add(new ThanhPhan.HuyHieu("GM", GiaoDien.TIM));
        }
        final Boolean tv = ThaoTacMayChu.laThanhVien(c.userID);
        if (tv != null) {
            hh.add(new ThanhPhan.HuyHieu(tv ? "Thành viên" : "Chưa là thành viên", tv ? GiaoDien.VANG_DAM : GiaoDien.chuMo));
        }
        if (ThaoTacMayChu.dangCamChat(c)) {
            hh.add(new ThanhPhan.HuyHieu("Cấm chat", GiaoDien.DO));
        }
        hh.add(new ThanhPhan.HuyHieu("Online", GiaoDien.XANH_LA));
        ten.add(hh);
        dau.add(ten, BorderLayout.CENTER);
        p.add(dau, BorderLayout.NORTH);

        // than: tien te + thong tin
        final JPanel giua = ThanhPhan.trongSuot(new BorderLayout(0, s(10)));
        final JPanel tien = ThanhPhan.trongSuot(new GridLayout(1, 3, s(8), 0));
        tien.add(oTien("Xu", c.getxu(), "Money_Yellow", GiaoDien.VANG_DAM));
        tien.add(oTien("Lượng", c.getLuong(), "Gummi_Blue", GiaoDien.XANH_DUONG));
        tien.add(oTien("Lượng khoá", c.getLuongLock(), "Gummi_Gray", GiaoDien.chuPhu));
        giua.add(tien, BorderLayout.NORTH);
        final io.Session ss = c.getSession();
        final String hst = hoSoSinhThai(c);
        final JPanel tt = HopThoai.luoi(
                nhanMo("Tài khoản"), ThanhPhan.nhan(ss == null ? "?" : ss.username + "  (id " + c.userID + ")"),
                nhanMo("IP / thiết bị"), ThanhPhan.nhan(ss == null ? "?" : ss.ip + " · " + thietBi(ss)),
                nhanMo("Bản đồ"), ThanhPhan.nhan(ThaoTacMayChu.tenMap(c.mapID) + " #" + c.mapID + " (" + c.x / 16 + ", " + c.y / 16 + ")"),
                nhanMo("Online"), ThanhPhan.nhan(ss == null ? "?" : DinhDang.thoiLuong(System.currentTimeMillis() - ss.connectTime)),
                nhanMo("Hành trang"), ThanhPhan.nhan(dem(c.iItems) + " món · túi " + dem(c.bagItems) + " · rương " + dem(c.tItems)
                + " · pet " + dem(c.pet) + " · thú cưỡi " + dem(c.animal)),
                nhanMo("Nhiệm vụ"), ThanhPhan.nhan(nhiemVu(c)),
                nhanMo("Sinh thái"), nhanBoc(hst == null ? "Chưa nạp hồ sơ (chưa dùng tính năng)" : hst));
        giua.add(tt, BorderLayout.CENTER);
        p.add(giua, BorderLayout.CENTER);

        // nut
        final JPanel nut = ThanhPhan.trongSuot(new GridLayout(0, 3, s(6), s(6)));
        final String t = c.charname;
        nut.add(nut("Kick", ThanhPhan.Kieu.DO, () -> {
            final String lyDo = hoiChu("Kick " + t, "Lý do (gửi cho người chơi, có thể bỏ trống):", "");
            if (lyDo != null) {
                HopThoai.chay(this, "Kick " + t + " khỏi máy chủ?", () -> ThaoTacMayChu.kick(t, lyDo), k -> lamMoi());
            }
        }));
        nut.add(nut("Khoá TK", ThanhPhan.Kieu.NGUY, () -> camNhanVat(t)));
        nut.add(nut(ThaoTacMayChu.dangCamChat(c) ? "Mở chat" : "Cấm chat", ThanhPhan.Kieu.VANG, () -> camChat(t)));
        nut.add(nut("Về thị trấn", ThanhPhan.Kieu.LA, () -> HopThoai.chay(this, "Đưa " + t + " về điểm an toàn ("
                + ThaoTacMayChu.tenMap(ThaoTacMayChu.MAP_AN_TOAN) + ")? Dùng khi Pokémon bị kẹt.", () -> ThaoTacMayChu.veAnToan(t),
                k -> veChiTiet())));
        nut.add(nut("Dịch chuyển", ThanhPhan.Kieu.XANH, () -> dichChuyen(t)));
        nut.add(nut("Gửi thư", ThanhPhan.Kieu.MO, () -> guiThu(t)));
        nut.add(nut("Nhắn riêng", ThanhPhan.Kieu.MO, () -> {
            final String nd = hoiChu("Thông báo riêng cho " + t, "Nội dung:", "");
            if (nd != null && !nd.isEmpty()) {
                HopThoai.chay(this, null, () -> ThaoTacMayChu.thongBaoRieng(t, nd), null);
            }
        }));
        nut.add(nut("Tặng quà an toàn", ThanhPhan.Kieu.TIM, () -> tangQua(t, c.lvDetail == null ? 1 : c.lvDetail.lv)));
        nut.add(nut("Giảm level", ThanhPhan.Kieu.MO, () -> giamLevel(t)));
        nut.add(nut("Đổi mật khẩu", ThanhPhan.Kieu.MO, () -> doiMatKhau(ss == null ? "" : ss.username)));
        nut.add(nut("Lịch sử ĐN", ThanhPhan.Kieu.MO, () -> lichSu(ss == null ? "" : ss.username)));
        final ThanhPhan.Nut cu = nut("Công cụ cũ…", ThanhPhan.Kieu.MO, null);
        cu.addActionListener(e -> moCongCuCu(cu, cu.getWidth() / 2, cu.getHeight()));
        nut.add(cu);
        p.add(nut, BorderLayout.SOUTH);
        chiTiet.add(p, BorderLayout.CENTER);
        GiaoDien.toCay(chiTiet);
        chiTiet.revalidate();
        chiTiet.repaint();
    }

    static JLabel nhanMo(final String s0) {
        final JLabel l = ThanhPhan.nhanPhu(s0);
        l.setFont(GiaoDien.dam(11.5f));
        return l;
    }

    static JLabel nhanBoc(final String s0) {
        return ThanhPhan.nhan("<html><body style='width:" + s(230) + "px'>" + TrangTongQuan.esc(s0) + "</body></html>");
    }

    static String hoSoSinhThai(final Char c) {
        try {
            return com.htbb.pokemon.QuanTriSinhThai.hoSo(c.charDBID);
        } catch (final Throwable t) {
            return null;
        }
    }

    static String nhiemVu(final Char c) {
        try {
            if (c.char_quest == null || c.char_quest.id_quest <= 0) {
                return "Không có nhiệm vụ đang nhận";
            }
            return "#" + c.char_quest.id_quest + (c.char_quest.finish != 0 ? " · đã xong, chờ trả" : " · đang làm");
        } catch (final Throwable t) {
            return "?";
        }
    }

    static int dem(final java.util.Collection<?> v) {
        return v == null ? 0 : v.size();
    }

    static JComponent oTien(final String ten, final long gt, final String icon, final Color m) {
        final JPanel p = new JPanel(new BorderLayout(s(6), 0)) {
            @Override
            protected void paintComponent(final java.awt.Graphics g0) {
                final java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
                GiaoDien.khuRang(g);
                g.setColor(GiaoDien.trong(m, GiaoDien.toi ? 40 : 22));
                g.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), s(12), s(12)));
                g.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(s(7), s(9), s(7), s(9)));
        p.add(new JLabel(ThanhPhan.iconPixel(AnhPokemon.vatPham(icon), s(24))), BorderLayout.WEST);
        final JPanel c = ThanhPhan.trongSuot(new GridLayout(0, 1));
        final JLabel n = ThanhPhan.nhanPhu(ten);
        n.setFont(GiaoDien.font(11));
        c.add(n);
        final JLabel v = ThanhPhan.tieuDe(gt < 10000000L ? DinhDang.so(gt) : DinhDang.gon(gt), 14);
        v.setToolTipText(DinhDang.so(gt));
        c.add(v);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    ThanhPhan.Nut nut(final String chu, final ThanhPhan.Kieu k, final Runnable r) {
        final ThanhPhan.Nut b = new ThanhPhan.Nut(chu, k).nho();
        if (r != null) {
            b.addActionListener(e -> r.run());
        }
        return b;
    }

    // ================================================================== thao tac (hop thoai)

    String hoiChu(final String tieuDe, final String nhan, final String macDinh) {
        final ThanhPhan.ONhap o = new ThanhPhan.ONhap(macDinh, 26);
        if (!HopThoai.bieuMau(this, tieuDe, nhan, o, "Tiếp tục")) {
            return null;
        }
        return o.getText().trim();
    }

    static JComboBox<String> chonThoiHan() {
        return new JComboBox<String>(new String[]{"30 phút", "2 giờ", "1 ngày", "3 ngày", "7 ngày", "30 ngày", "Vĩnh viễn"});
    }

    static int phutThoiHan(final int i) {
        final int[] p = {30, 120, 1440, 4320, 10080, 43200, 0};
        return p[Math.max(0, Math.min(p.length - 1, i))];
    }

    void camNhanVat(final String t) {
        final ThanhPhan.ONhap lyDo = new ThanhPhan.ONhap("", 26).goiY("vd: dùng phần mềm thứ ba");
        final JComboBox<String> han = chonThoiHan();
        han.setSelectedIndex(2);
        if (!HopThoai.bieuMau(this, "Khoá Pokémon " + t, "Chặn đăng nhập và đưa ra khỏi máy chủ nếu đang online. "
                + "Lệnh có thời hạn được bảng điều khiển tự mở khi hết hạn.", HopThoai.luoi("Lý do", lyDo, "Thời hạn", han), "Khoá")) {
            return;
        }
        if (lyDo.getText().trim().isEmpty()) {
            HopThoai.ketQua(this, "Thiếu lý do", "Cần nhập lý do khoá (ghi kiểm toán).");
            return;
        }
        final int phut = phutThoiHan(han.getSelectedIndex());
        HopThoai.chay(this, "Khoá " + t + " " + han.getSelectedItem() + "?\nLý do: " + lyDo.getText().trim(),
                () -> ThaoTacMayChu.cam(t, lyDo.getText().trim(), phut), k -> {
                    lamMoi();
                    if (moHinhOff.getRowCount() > 0) {
                        timOffline();
                    }
                });
    }

    void camChat(final String t) {
        final Char c = ThaoTacMayChu.tim(t);
        if (c != null && ThaoTacMayChu.dangCamChat(c)) {
            HopThoai.chay(this, "Mở chat cho " + t + "?", () -> ThaoTacMayChu.camChat(t, 0), k -> veChiTiet());
            return;
        }
        final JComboBox<String> han = new JComboBox<String>(new String[]{"10 phút", "30 phút", "1 giờ", "1 ngày", "7 ngày"});
        han.setSelectedIndex(1);
        if (!HopThoai.bieuMau(this, "Cấm chat " + t, "Dùng cờ cấm chat của lệnh GM (lưu theo Pokémon).",
                HopThoai.luoi("Thời hạn", han), "Cấm chat")) {
            return;
        }
        final int[] p = {10, 30, 60, 1440, 10080};
        final int phut = p[han.getSelectedIndex()];
        HopThoai.chay(this, null, () -> ThaoTacMayChu.camChat(t, phut), k -> veChiTiet());
    }

    void dichChuyen(final String t) {
        final JSpinner map = new JSpinner(new SpinnerNumberModel(ThaoTacMayChu.MAP_AN_TOAN, 0, 9999, 1));
        final JSpinner x = new JSpinner(new SpinnerNumberModel(ThaoTacMayChu.X_AN_TOAN, 0, 500, 1));
        final JSpinner y = new JSpinner(new SpinnerNumberModel(ThaoTacMayChu.Y_AN_TOAN, 0, 500, 1));
        final Char c = ThaoTacMayChu.tim(t);
        if (c != null) {
            map.setValue(c.mapID);
            x.setValue(Math.max(0, c.x / 16));
            y.setValue(Math.max(0, c.y / 16));
        }
        if (!HopThoai.bieuMau(this, "Dịch chuyển " + t, "Toạ độ tính theo ô bản đồ.",
                HopThoai.luoi("Bản đồ (id)", map, "X (ô)", x, "Y (ô)", y), "Dịch chuyển")) {
            return;
        }
        HopThoai.chay(this, null, () -> ThaoTacMayChu.dichChuyen(t, (Integer) map.getValue(), (Integer) x.getValue(),
                (Integer) y.getValue()), k -> veChiTiet());
    }

    void guiThu(final String t) {
        final JTextArea nd = ThanhPhan.vungChu(5, 30);
        if (!HopThoai.bieuMau(this, "Gửi thư cho " + t, "Thư vào hộp thư trong game (tob_inbox, người gửi \"Admin\").",
                ThanhPhan.cuon(nd), "Gửi thư")) {
            return;
        }
        HopThoai.chay(this, null, () -> ThaoTacMayChu.guiThu(t, nd.getText()), null);
    }

    void doiMatKhau(final String taiKhoan) {
        final ThanhPhan.ONhap tk = new ThanhPhan.ONhap(taiKhoan, 20);
        final javax.swing.JPasswordField m1 = new javax.swing.JPasswordField(20);
        final javax.swing.JPasswordField m2 = new javax.swing.JPasswordField(20);
        if (!HopThoai.bieuMau(this, "Đặt lại mật khẩu tài khoản", "Ghi account.team_user (PASSWORD() kiểu MySQL).",
                HopThoai.luoi("Tài khoản", tk, "Mật khẩu mới", m1, "Nhập lại", m2), "Đổi mật khẩu")) {
            return;
        }
        final String a = new String(m1.getPassword()), b = new String(m2.getPassword());
        if (!a.equals(b)) {
            HopThoai.ketQua(this, "Không khớp", "Mật khẩu nhập lại không khớp.");
            return;
        }
        final String ten = tk.getText().trim();
        HopThoai.chay(this, "Đặt lại mật khẩu cho tài khoản " + ten + "?", () -> ThaoTacMayChu.doiMatKhau(ten, a), null);
    }

    void tangQua(final String t, final int cap) {
        final ThanhPhan.ONhap qua = new ThanhPhan.ONhap("LUONGKHOA:5", 26);
        final ThanhPhan.ONhap lyDo = new ThanhPhan.ONhap("", 26).goiY("vd: đền bù sự cố 18/10");
        final JLabel kiem = ThanhPhan.nhanPhu(" ");
        final Runnable cap0 = () -> {
            final String l = ThaoTacMayChu.kiemQua(qua.getText(), cap);
            kiem.setText(l == null ? "Hợp lệ · giá trị " + com.htbb.pokemon.GiftcodeAnToanLogic.giaTri(
                    com.htbb.pokemon.GiftcodeAnToanLogic.chuanHoaThuong(qua.getText())) + " / trần " + ThaoTacMayChu.tranQua()
                    : "Không hợp lệ: " + l);
            kiem.setForeground(l == null ? GiaoDien.XANH_LA_DAM : GiaoDien.DO);
        };
        qua.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                cap0.run();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                cap0.run();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                cap0.run();
            }
        });
        cap0.run();
        if (!HopThoai.bieuMau(this, "Tặng quà an toàn cho " + t,
                "Chỉ qua bộ lọc giftcode an toàn: LUONGKHOA:n, GEM:id:n (luôn khoá), ITEM:id:n (khoá, trong danh sách trắng)."
                        + " Cấm xu / lượng mở. Nhiều phần ngăn bằng dấu phẩy.",
                HopThoai.luoi("Phần thưởng", qua, "", kiem, "Lý do", lyDo), "Tặng")) {
            return;
        }
        if (lyDo.getText().trim().isEmpty()) {
            HopThoai.ketQua(this, "Thiếu lý do", "Cần nhập lý do (ghi kiểm toán).");
            return;
        }
        HopThoai.chay(this, "Tặng " + qua.getText().trim() + " cho " + t + "?", () -> ThaoTacMayChu.tangQua(t, qua.getText(),
                lyDo.getText().trim()), k -> veChiTiet());
    }

    void giamLevel(final String t) {
        final JSpinner so = new JSpinner(new SpinnerNumberModel(1, 1, 200, 1));
        if (!HopThoai.bieuMau(this, "Giảm level " + t, "EXP về 0% cấp mới; giữ điểm đã nhận (không cộng lại khi lên lại).",
                HopThoai.luoi("Số level", so), "Giảm")) {
            return;
        }
        final int n = (Integer) so.getValue();
        HopThoai.chay(this, "Giảm " + n + " level của " + t + "?", () -> ThaoTacMayChu.giamLevel(t, n), k -> veChiTiet());
    }

    void giamLevelHangLoat() {
        final JTextArea ten = ThanhPhan.vungChu(6, 32);
        final JSpinner so = new JSpinner(new SpinnerNumberModel(1, 1, 200, 1));
        final JPanel p = ThanhPhan.trongSuot(new BorderLayout(0, s(8)));
        p.add(ThanhPhan.cuon(ten), BorderLayout.CENTER);
        p.add(HopThoai.luoi("Số level mỗi Pokémon", so), BorderLayout.SOUTH);
        if (!HopThoai.bieuMau(this, "Giảm level nhiều Pokémon", "Tên Pokémon online, mỗi dòng một tên (hoặc ngăn bằng , ;)."
                + " Bỏ qua nếu giảm xuống dưới cấp 1. Tên trùng chỉ xử lý một lần.", p, "Tiếp tục")) {
            return;
        }
        final Set<String> ds = new LinkedHashSet<String>();
        for (final String e : ten.getText().split("[,;\\r\\n]+")) {
            final String n = e.trim().toLowerCase();
            if (!n.isEmpty()) {
                ds.add(n);
            }
        }
        if (ds.isEmpty()) {
            return;
        }
        final int n = (Integer) so.getValue();
        HopThoai.chay(this, "Giảm " + n + " level cho " + ds.size() + " Pokémon?", () -> {
            final StringBuilder b = new StringBuilder();
            for (final String t : ds) {
                b.append(ThaoTacMayChu.giamLevel(t, n)).append('\n');
            }
            return b.toString().trim();
        }, k -> {
            final JTextArea a = ThanhPhan.vungChu(12, 60);
            a.setText(k);
            a.setEditable(false);
            HopThoai.xemNoiDung(this, "Kết quả giảm level - " + ds.size() + " Pokémon", ThanhPhan.cuon(a));
        });
    }

    void lichSu(final String taiKhoan) {
        if (taiKhoan == null || taiKhoan.isEmpty()) {
            return;
        }
        HopThoai.doc(() -> new Object[]{ThaoTacMayChu.lichSuDangNhap(taiKhoan), ThaoTacMayChu.nhanVatCuaTaiKhoan(taiKhoan)}, kq -> {
            @SuppressWarnings("unchecked")
            final List<Object[]> may = (List<Object[]>) kq[0];
            @SuppressWarnings("unchecked")
            final List<Object[]> nv = (List<Object[]>) kq[1];
            final PhanDoan pd = new PhanDoan();
            pd.them("Máy / IP đã đăng nhập", ThaoTacMayChu.loiTruyVan(may) != null || may.size() <= 1
                    ? ThanhPhan.trongRong("Chưa có lịch sử thiết bị (bản game của người chơi chưa gửi mã máy).")
                    : bangKetQua(may, new String[]{"Mã máy", "IP", "Lần đầu", "Lần cuối", "Số lần"}));
            pd.them("Lần vào game theo Pokémon", ThaoTacMayChu.loiTruyVan(nv) != null || nv.size() <= 1
                    ? ThanhPhan.trongRong("Tài khoản chưa có Pokémon.")
                    : bangKetQua(nv, new String[]{"Tên", "Cấp", "Lần cuối vào game", "Khoá", "Dex"}));
            pd.setPreferredSize(new Dimension(s(680), s(380)));
            HopThoai.xemNoiDung(this, "Lịch sử đăng nhập · " + taiKhoan, pd);
        });
    }

    void xemDsCam() {
        HopThoai.doc(ThaoTacMayChu::dsCam, l -> {
            final DefaultTableModel m = new DefaultTableModel(new Object[]{"Pokémon", "Lý do", "Hết hạn", "Lúc khoá"}, 0);
            for (final Object[] r : l) {
                m.addRow(r);
            }
            final JTable t = new ThanhPhan.Bang(m);
            ThanhPhan.kieuBang(t);
            final JPanel p = ThanhPhan.trongSuot(new BorderLayout(0, s(8)));
            p.add(ThanhPhan.cuon(t), BorderLayout.CENTER);
            final ThanhPhan.Nut mo = new ThanhPhan.Nut("Mở khoá dòng chọn", ThanhPhan.Kieu.LA).nho();
            mo.addActionListener(e -> {
                final int r = t.getSelectedRow();
                if (r >= 0) {
                    final String ten = String.valueOf(t.getValueAt(r, 0));
                    HopThoai.chay(p, "Mở khoá " + ten + "?", () -> ThaoTacMayChu.moCam(ten), k -> m.removeRow(r));
                }
            });
            p.add(ThanhPhan.hang(FlowLayout.LEFT, mo, ThanhPhan.nhanPhu(l.size() + " lệnh khoá do bảng điều khiển tạo")),
                    BorderLayout.SOUTH);
            p.setPreferredSize(new Dimension(s(680), s(360)));
            HopThoai.xemNoiDung(this, "Pokémon đang bị khoá", p);
        });
    }

    static JComponent bangKetQua(final List<Object[]> kq, final String[] ten) {
        final DefaultTableModel m = new DefaultTableModel(ten, 0);
        for (int i = 1; i < kq.size(); i++) {
            m.addRow(kq.get(i));
        }
        final JTable t = new ThanhPhan.Bang(m);
        ThanhPhan.kieuBang(t);
        t.setAutoCreateRowSorter(true);
        final JScrollPane sp = ThanhPhan.cuon(t);
        sp.setPreferredSize(new Dimension(s(680), s(340)));
        if (kq.size() <= 1) {
            return (JComponent) ThanhPhan.trongRong("Không có dữ liệu.");
        }
        return sp;
    }

    void timOffline() {
        final String k = oTimOff.getText().trim();
        if (k.length() < 2) {
            HopThoai.baoNhanh.accept("Nhập ít nhất 2 ký tự để tìm.");
            return;
        }
        HopThoai.doc(() -> ThaoTacMayChu.timOffline(k), kq -> {
            moHinhOff.setRowCount(0);
            final String loi = ThaoTacMayChu.loiTruyVan(kq);
            if (loi != null) {
                HopThoai.baoNhanh.accept("Lỗi tìm: " + loi);
                return;
            }
            for (int i = 1; i < kq.size(); i++) {
                final Object[] r = kq.get(i).clone();
                r[7] = r[7] != null && ((Number) r[7]).intValue() != 0 ? "Khoá" : "—";
                moHinhOff.addRow(r);
            }
            HopThoai.baoNhanh.accept("Tìm thấy " + (kq.size() - 1) + " Pokémon.");
        });
    }

    String chonOff(final int cot) {
        final int r = bangOff.getSelectedRow();
        if (r < 0) {
            HopThoai.baoNhanh.accept("Chọn một dòng trong kết quả tìm trước.");
            return null;
        }
        final Object v = bangOff.getValueAt(r, cot);
        return v == null ? null : String.valueOf(v);
    }

    /** Menu cong cu cu (server.Panel.ButtonEditor) cho dong dang chon - giu nguyen moi chuc nang cu. */
    void moCongCuCu(final Component neo, final int x, final int y) {
        final int r = bang.getSelectedRow();
        if (r < 0) {
            HopThoai.baoNhanh.accept("Chọn một người chơi trước.");
            return;
        }
        if (congCuCu == null) {
            congCuCu = new server.Panel.ButtonEditor(new javax.swing.JCheckBox(), bang, new ArrayList<Object[]>(), khung);
        }
        KiemToanQuanTri.ghi("CONG_CU_CU", String.valueOf(bang.getValueAt(r, 1)), "mo menu");
        congCuCu.moMenu(r, neo, x, y);
    }

    // ================================================================== o bang

    static final class OIcon extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(final JTable t, final Object v, final boolean chon, final boolean f,
                final int r, final int c) {
            super.getTableCellRendererComponent(t, "", chon, false, r, c);
            setIcon((Icon) v);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBackground(chon ? GiaoDien.chonDong : r % 2 == 0 ? GiaoDien.nenThe : GiaoDien.dongLe);
            return this;
        }
    }

    static final class OTen extends ThanhPhan.OBang {
        @Override
        public Component getTableCellRendererComponent(final JTable t, final Object v, final boolean chon, final boolean f,
                final int r, final int c) {
            super.getTableCellRendererComponent(t, v, chon, f, r, c);
            setFont(GiaoDien.dam(12.5f));
            return this;
        }
    }

    static final class OThoiGian extends ThanhPhan.OBang {
        @Override
        public Component getTableCellRendererComponent(final JTable t, final Object v, final boolean chon, final boolean f,
                final int r, final int c) {
            super.getTableCellRendererComponent(t, v instanceof Long
                    ? DinhDang.dongHo(System.currentTimeMillis() - (Long) v) : v, chon, f, r, c);
            setHorizontalAlignment(RIGHT);
            setForeground(GiaoDien.chuPhu);
            return this;
        }
    }

    /** Nut radio dung trong hop thoai (giu trong lop de cac trang khac dung). */
    static JPanel nhomRadio(final ButtonGroup g, final String... ten) {
        final JPanel p = ThanhPhan.trongSuot(new FlowLayout(FlowLayout.LEFT, s(6), 0));
        for (int i = 0; i < ten.length; i++) {
            final JRadioButton r = new JRadioButton(ten[i], i == 0);
            r.setOpaque(false);
            r.setFont(GiaoDien.font(12.5f));
            r.setForeground(GiaoDien.chu);
            r.setActionCommand(String.valueOf(i));
            g.add(r);
            p.add(r);
        }
        return p;
    }
}
