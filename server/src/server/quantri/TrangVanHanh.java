package server.quantri;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import real.AdminHandler;
import real.cmd.LoginHandler;

import static server.quantri.GiaoDien.s;

/**
 * Trang 5 - Bao tri & Van hanh: bao_tri.txt (+ danh sach cho phep), tat / khoi dong lai co dem nguoc
 * (giu dung kich ban cua nut "Bao Tri" cu), luu tat ca, don bo nho, dung/mo dang nhap, nap lai cau hinh,
 * sao luu CSDL, xem tep nhat ky.
 */
public final class TrangVanHanh extends Trang {

    private final ThanhPhan.HuyHieu hhDangNhap = new ThanhPhan.HuyHieu("", GiaoDien.XANH_LA);
    private final ThanhPhan.HuyHieu hhBaoTri = new ThanhPhan.HuyHieu("", GiaoDien.XANH_LA);
    private final ThanhPhan.HuyHieu hhDem = new ThanhPhan.HuyHieu("", GiaoDien.XANH_LA);
    private final JLabel gioHangNgay = ThanhPhan.nhan("");
    private final ThanhPhan.ONhap cauBaoTri = new ThanhPhan.ONhap("", 30).goiY(server.ChiSoMayChu.CAU_BAO_TRI_MAC_DINH);
    private final ThanhPhan.ONhap choPhep = new ThanhPhan.ONhap("", 30).goiY("tài khoản GM vẫn vào được, cách nhau bởi dấu cách");
    private final ThanhPhan.Nut nutBaoTri = new ThanhPhan.Nut("Bật bảo trì", ThanhPhan.Kieu.DO);
    private final ThanhPhan.Nut nutDangNhap = new ThanhPhan.Nut("", ThanhPhan.Kieu.VANG);
    private final JSpinner phut = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
    private final ButtonGroup kieuTat = new ButtonGroup();
    private final ThanhPhan.Nut nutDem = new ThanhPhan.Nut("Bắt đầu đếm ngược", ThanhPhan.Kieu.NGUY);
    private final JLabel conLai = ThanhPhan.tieuDe("—", 30);
    private final DefaultTableModel mhTep = new DefaultTableModel(new Object[]{"Tệp", "Loại", "Dung lượng", "Sửa lúc"}, 0) {
        @Override
        public boolean isCellEditable(final int r, final int c) {
            return false;
        }
    };
    private final JTable bangTep = new ThanhPhan.Bang(mhTep).rong("Không tìm thấy tệp nhật ký.");
    private final List<File> dsTep = new ArrayList<File>();
    private Consumer<File> moTep = f -> { };

    /** Dem nguoc tat may (giay con lai, -1 = khong dem). */
    private Timer dem;
    private int giayConLai = -1;
    private boolean khoiDongLai;

    public TrangVanHanh() {
        super("Bảo trì & Vận hành", "Mọi thao tác đều hỏi xác nhận, chạy ngoài luồng giao diện và ghi kiểm toán.");
        // ---- trang thai
        final ThanhPhan.The theTt = new ThanhPhan.The("Trạng thái", TrangTongQuan.icon("Machine_Green")).nhan(GiaoDien.XANH_DUONG);
        final JPanel tt = danhSach();
        tt.add(dong("Đăng nhập mới", hhDangNhap));
        tt.add(dong("Chế độ bảo trì", hhBaoTri));
        tt.add(dong("Đếm ngược tắt máy", hhDem));
        final JPanel g = ThanhPhan.trongSuot(new BorderLayout());
        g.add(ThanhPhan.nhan("Bảo trì tự động"), BorderLayout.CENTER);
        g.add(gioHangNgay, BorderLayout.EAST);
        tt.add(g);
        theTt.noiDung(tt);

        // ---- thao tac nhanh
        final ThanhPhan.The theNhanh = new ThanhPhan.The("Thao tác nhanh", TrangTongQuan.icon("Wand_Blue")).nhan(GiaoDien.VANG);
        final JPanel nh = ThanhPhan.trongSuot(new GridLayout(0, 2, s(8), s(8)));
        nh.add(nutViec("Lưu tất cả Pokémon", ThanhPhan.Kieu.LA, "Lưu ngay mọi Pokémon đang chơi và các đội?",
                ThaoTacMayChu::luuTatCa));
        nh.add(nutViec("Dọn bộ nhớ (GC)", ThanhPhan.Kieu.XANH, null, ThaoTacMayChu::donBoNho));
        nutDangNhap.addActionListener(e -> {
            final boolean dung = !LoginHandler.stopLogin;
            HopThoai.chay(this, dung ? "Tắt đăng nhập mới? Người đang chơi không bị ảnh hưởng." : "Cho phép đăng nhập lại?",
                    () -> ThaoTacMayChu.datDungDangNhap(dung), k -> capNhat());
        });
        nh.add(nutDangNhap);
        nh.add(nutViec("Nạp lại cấu hình", ThanhPhan.Kieu.MO, null, ThaoTacMayChu::napLaiCauHinh));
        nh.add(nutViec("Sao lưu CSDL ngay", ThanhPhan.Kieu.TIM, "Sao lưu cơ sở dữ liệu game bây giờ?",
                ThaoTacMayChu::saoLuu));
        final ThanhPhan.Nut moThuMuc = new ThanhPhan.Nut("Mở thư mục làm việc", ThanhPhan.Kieu.MO);
        moThuMuc.addActionListener(e -> {
            try {
                java.awt.Desktop.getDesktop().open(new File(".").getAbsoluteFile());
            } catch (final Throwable t) {
                HopThoai.baoNhanh.accept("Không mở được: " + t.getMessage());
            }
        });
        nh.add(moThuMuc);
        theNhanh.noiDung(nh);

        // ---- bao tri bang tep
        final ThanhPhan.The theBt = new ThanhPhan.The("Chế độ bảo trì", TrangTongQuan.icon("Key_Brown")).nhan(GiaoDien.DO);
        nutBaoTri.addActionListener(e -> doiBaoTri());
        final JPanel bt = ThanhPhan.trongSuot(new BorderLayout(0, s(10)));
        bt.add(ThanhPhan.doanVan("Bật bảo trì: trong vài giây máy chủ từ chối đăng nhập mới; người đang chơi không bị kick."
                + " Tài khoản trong ô Cho phép vẫn vào được để kiểm tra.",
                true), BorderLayout.NORTH);
        bt.add(HopThoai.luoi("Câu thông báo", cauBaoTri, "Cho phép (tài khoản)", choPhep), BorderLayout.CENTER);
        bt.add(ThanhPhan.hang(FlowLayout.LEFT, nutBaoTri), BorderLayout.SOUTH);
        theBt.noiDung(bt);

        // ---- dem nguoc
        final ThanhPhan.The theDem = new ThanhPhan.The("Tắt / khởi động lại có đếm ngược", TrangTongQuan.icon("Flame_Orb"))
                .nhan(GiaoDien.DO_DAM);
        final JPanel radio = TrangNguoiChoi.nhomRadio(kieuTat, "Khởi động lại", "Tắt hẳn");
        nutDem.addActionListener(e -> {
            if (giayConLai >= 0) {
                huyDem();
            } else if (dangKdl()) {
                HopThoai.chay(this, "Huỷ yêu cầu khởi động lại an toàn?", () -> {
                    final Boolean ok = NguonCauHinh.huyKhoiDongLai();
                    return ok == null ? "Không gọi được dịch vụ cấu hình." : ok ? "Đã huỷ yêu cầu khởi động lại." : "Không có yêu cầu để huỷ.";
                }, k -> docKdl());
            } else {
                batDauDem();
            }
        });
        final JPanel dm = ThanhPhan.trongSuot(new BorderLayout(s(16), s(8)));
        dm.add(ThanhPhan.doanVan("Khởi động lại: máy chủ tự đếm ngược, báo người chơi, lưu toàn bộ rồi bật lại."
                + " Tắt hẳn: báo mỗi phút, còn 2 phút ngừng đăng nhập, còn 1 phút khoá thao tác, hết giờ lưu toàn bộ rồi tắt.", true),
                BorderLayout.NORTH);
        final JPanel trai = ThanhPhan.trongSuot(new GridLayout(0, 1, 0, s(8)));
        trai.add(radio);
        trai.add(ThanhPhan.hang(FlowLayout.LEFT, ThanhPhan.nhan("Sau"), phut, ThanhPhan.nhan("phút"), nutDem));
        dm.add(trai, BorderLayout.CENTER);
        final JPanel phai = new JPanel(new BorderLayout(0, s(2))) {
            @Override
            protected void paintComponent(final java.awt.Graphics g0) {
                final java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
                GiaoDien.khuRang(g);
                g.setColor(GiaoDien.nenThe2);
                g.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), s(14), s(14)));
                g.dispose();
            }
        };
        phai.setOpaque(false);
        phai.setBorder(javax.swing.BorderFactory.createEmptyBorder(s(8), s(16), s(8), s(16)));
        phai.setPreferredSize(new java.awt.Dimension(s(190), s(84)));
        final JLabel nhanCon = ThanhPhan.nhanPhu("Còn lại");
        nhanCon.setHorizontalAlignment(JLabel.CENTER);
        conLai.setHorizontalAlignment(JLabel.CENTER);
        phai.add(nhanCon, BorderLayout.NORTH);
        phai.add(conLai, BorderLayout.CENTER);
        dm.add(phai, BorderLayout.EAST);
        theDem.noiDung(dm);

        // ---- tep nhat ky
        final ThanhPhan.The theTep = new ThanhPhan.The("Tệp nhật ký", TrangTongQuan.icon("Paper_News")).nhan(GiaoDien.XANH_NUOC);
        ThanhPhan.kieuBang(bangTep);
        final ThanhPhan.Nut xem = new ThanhPhan.Nut("Xem trực tiếp", ThanhPhan.Kieu.XANH).nho();
        xem.addActionListener(e -> {
            final int r = bangTep.getSelectedRow();
            if (r >= 0) {
                moTep.accept(dsTep.get(bangTep.convertRowIndexToModel(r)));
            }
        });
        final ThanhPhan.Nut tai = new ThanhPhan.Nut("Tải lại", ThanhPhan.Kieu.MO).nho();
        tai.addActionListener(e -> docTep());
        final JPanel tp = ThanhPhan.trongSuot(new BorderLayout(0, s(8)));
        final JScrollPane sp = ThanhPhan.cuon(bangTep);
        sp.setPreferredSize(new Dimension(s(500), s(220)));
        tp.add(sp, BorderLayout.CENTER);
        tp.add(ThanhPhan.hang(FlowLayout.LEFT, xem, tai), BorderLayout.SOUTH);
        theTep.noiDung(tp);

        thanh.add(cot(tiLe(new double[]{1, 1.3}, theTt, theNhanh), tiLe(new double[]{1, 1}, theBt, theDem), theTep), BorderLayout.NORTH);
        capNhat();
    }

    public void khiMoTep(final Consumer<File> c) {
        moTep = c;
    }

    @Override
    public String ten() {
        return "Vận hành";
    }

    @Override
    public String icon() {
        return "Machine_Green";
    }

    @Override
    public void khiHien() {
        capNhat();
        docTep();
        HopThoai.doc(ThaoTacMayChu::docBaoTri, b -> {
            if (b != null) {
                cauBaoTri.setText(b[0]);
                choPhep.setText(b[1]);
            }
        });
    }

    @Override
    public void nhip(final DuLieuTrucTiep.Mau m) {
        if (System.currentTimeMillis() - lanDocKdl > 3000L) {
            docKdl();
        }
        capNhat();
    }

    // ------------------------------------------------------------------ khoi dong lai an toan (com.htbb.cauhinh)

    /** {trang_thai, giay, ly_do, bat_dau_luc} cua cau_hinh_khoi_dong_lai (dich vu cau hinh trung tam). */
    private volatile Object[] kdl;
    private long lanDocKdl;
    private volatile boolean dangDocKdl;

    void docKdl() {
        lanDocKdl = System.currentTimeMillis();
        if (dangDocKdl) {
            return;
        }
        dangDocKdl = true;
        HopThoai.doc(NguonCauHinh::trangThaiKhoiDongLai, r -> {
            dangDocKdl = false;
            kdl = r;
            capNhat();
        });
    }

    boolean dangKdl() {
        final Object[] k = kdl;
        if (k == null) {
            return false;
        }
        final String t = String.valueOf(k[0]);
        return t.equals("cho") || t.equals("dang_dem") || t.equals("huy") || t.equals("dang_thoat");
    }

    String moTaKdl() {
        final Object[] k = kdl;
        final String t = String.valueOf(k[0]);
        if (t.equals("dang_dem") && ((Long) k[3]) > 0) {
            final long con = Math.max(0, (Integer) k[1] * 1000L - (System.currentTimeMillis() - (Long) k[3]));
            return "Khởi động lại sau " + DinhDang.dongHo(con);
        }
        return t.equals("cho") ? "Chờ máy chủ nhận yêu cầu" : t.equals("huy") ? "Đang huỷ" : t.equals("dang_thoat") ? "Đang lưu và thoát"
                : t;
    }

    static JPanel dong(final String ten, final ThanhPhan.HuyHieu h) {
        final JPanel p = ThanhPhan.trongSuot(new BorderLayout());
        p.add(ThanhPhan.nhan(ten), BorderLayout.CENTER);
        p.add(h, BorderLayout.EAST);
        return p;
    }

    ThanhPhan.Nut nutViec(final String chu, final ThanhPhan.Kieu k, final String hoi, final java.util.function.Supplier<String> viec) {
        final ThanhPhan.Nut b = new ThanhPhan.Nut(chu, k);
        b.addActionListener(e -> HopThoai.chay(this, hoi, viec, x -> capNhat()));
        return b;
    }

    void capNhat() {
        final boolean dung = LoginHandler.stopLogin;
        hhDangNhap.dat(dung ? "Đang tắt" : "Đang mở", dung ? GiaoDien.DO : GiaoDien.XANH_LA);
        nutDangNhap.setText(dung ? "Cho phép đăng nhập" : "Dừng đăng nhập mới");
        nutDangNhap.datKieu(dung ? ThanhPhan.Kieu.LA : ThanhPhan.Kieu.VANG);
        final boolean bt = ThaoTacMayChu.tepBaoTri().isFile() || server.ChiSoMayChu.dangBaoTri();
        hhBaoTri.dat(bt ? "Đang bảo trì" : "Tắt", bt ? GiaoDien.DO : GiaoDien.XANH_LA);
        nutBaoTri.setText(bt ? "Tắt bảo trì (mở đăng nhập)" : "Bật bảo trì");
        nutBaoTri.datKieu(bt ? ThanhPhan.Kieu.LA : ThanhPhan.Kieu.DO);
        if (giayConLai < 0 && dangKdl()) {
            hhDem.dat(moTaKdl(), GiaoDien.DO);
        } else {
            hhDem.dat(giayConLai >= 0 ? (khoiDongLai ? "Khởi động lại sau " : "Tắt sau ") + DinhDang.dongHo(giayConLai * 1000L)
                    : AdminHandler.isStopServer ? "Đang tắt máy" : "Không", giayConLai >= 0 || AdminHandler.isStopServer ? GiaoDien.DO
                    : GiaoDien.XANH_LA);
        }
        gioHangNgay.setText(ThaoTacMayChu.gioBaoTriHangNgay());
        final boolean kdlChay = giayConLai < 0 && dangKdl();
        conLai.setText(giayConLai >= 0 ? DinhDang.dongHo(giayConLai * 1000L) : kdlChay ? moTaKdl().replaceAll("^[^0-9]*", "") : "—");
        if (kdlChay && conLai.getText().isEmpty()) {
            conLai.setText("…");
        }
        conLai.setForeground(giayConLai >= 0 || kdlChay ? GiaoDien.DO : GiaoDien.chuMo);
        nutDem.setText(giayConLai >= 0 || kdlChay ? "Huỷ đếm ngược" : "Bắt đầu đếm ngược");
        nutDem.datKieu(giayConLai >= 0 || kdlChay ? ThanhPhan.Kieu.LA : ThanhPhan.Kieu.NGUY);
    }

    void doiBaoTri() {
        if (ThaoTacMayChu.tepBaoTri().isFile() || server.ChiSoMayChu.dangBaoTri()) {
            HopThoai.chay(this, "Tắt bảo trì và mở lại đăng nhập?", () -> {
                final StringBuilder b = new StringBuilder();
                if (NguonCauHinh.coTrungTam()) {
                    final java.util.Map<String, String> m = new java.util.LinkedHashMap<String, String>();
                    m.put("bao_tri.bat", "0");
                    b.append(NguonCauHinh.ghiNhieu(m, "tat bao tri tu bang dieu khien")).append(' ');
                }
                if (ThaoTacMayChu.tepBaoTri().isFile()) {
                    b.append(ThaoTacMayChu.tatBaoTri());
                }
                return b.length() == 0 ? "Đã tắt bảo trì." : b.toString().trim();
            }, k -> capNhat());
            return;
        }
        final List<String> cho = new ArrayList<String>(Arrays.asList(choPhep.getText().trim().split("[,\\s]+")));
        cho.removeIf(String::isEmpty);
        final String cau = cauBaoTri.getText().trim();
        HopThoai.chay(this, "Bật bảo trì? Đăng nhập mới bị từ chối" + (cho.isEmpty() ? "" : " (trừ " + cho + ")") + ".",
                () -> {
                    if (NguonCauHinh.coTrungTam()) {
                        // Duong chinh: khoa cau_hinh bao_tri.* (dich vu trung tam - lich su, ap dung <= 10 giay).
                        final java.util.Map<String, String> m = new java.util.LinkedHashMap<String, String>();
                        m.put("bao_tri.bat", "1");
                        m.put("bao_tri.cau", cau);
                        m.put("bao_tri.cho", String.join(",", cho).toLowerCase());
                        final String kq = NguonCauHinh.ghiNhieu(m, "bat bao tri tu bang dieu khien");
                        if (kq != null && kq.startsWith("OK")) {
                            return "Đã bật bảo trì. " + kq.substring(2).trim();
                        }
                    }
                    return ThaoTacMayChu.batBaoTri(cau, cho);
                }, k -> capNhat());
    }

    // ------------------------------------------------------------------ dem nguoc (kich ban nut "Bao Tri" cu)

    void batDauDem() {
        final int p = (Integer) phut.getValue();
        khoiDongLai = kieuTat.getSelection() == null || "0".equals(kieuTat.getSelection().getActionCommand());
        if (khoiDongLai && NguonCauHinh.coTrungTam()) {
            // Khoi dong lai AN TOAN cua dich vu cau hinh trung tam: may chu tu dem nguoc, bao nguoi choi, luu het, thoat;
            // supervisor (systemd Restart=always) bat lai. Huy duoc trong luc dem.
            final ThanhPhan.ONhap lyDo = new ThanhPhan.ONhap("", 26).goiY("vd: cập nhật bản vá");
            if (!HopThoai.bieuMau(this, "Khởi động lại an toàn", "Máy chủ đếm ngược " + p + " phút, báo người chơi, lưu toàn bộ rồi thoát;"
                    + " dịch vụ quản lý máy chủ tự bật lại. Huỷ được trong lúc đếm.", HopThoai.luoi("Lý do", lyDo), "Gửi yêu cầu")) {
                return;
            }
            final String ld = lyDo.getText().trim().isEmpty() ? "bang dieu khien" : lyDo.getText().trim();
            HopThoai.chay(this, "Khởi động lại máy chủ sau " + p + " phút?", () -> {
                final Boolean ok = NguonCauHinh.yeuCauKhoiDongLai(p * 60, ld);
                return ok == null ? "Không gọi được dịch vụ cấu hình." : ok ? "Đã gửi yêu cầu khởi động lại an toàn (" + p + " phút)."
                        : "Đang có một yêu cầu khởi động lại khác.";
            }, k -> docKdl());
            return;
        }
        if (!HopThoai.xacNhan(this, khoiDongLai ? "Khởi động lại máy chủ" : "Tắt máy chủ", (khoiDongLai ? "Khởi động lại" : "Tắt hẳn")
                + " máy chủ sau " + p + " phút? Người chơi được thông báo mỗi phút; hết giờ máy chủ lưu toàn bộ rồi thoát.",
                "Bắt đầu đếm ngược")) {
            return;
        }
        giayConLai = p * 60;
        KiemToanQuanTri.ghi(khoiDongLai ? "DEM_NGUOC_KHOI_DONG_LAI" : "DEM_NGUOC_TAT", "", p + " phut");
        bao("Hệ thống sẽ bảo trì sau " + p + " phút.");
        dem = new Timer(1000, e -> nhipDem());
        dem.start();
        capNhat();
    }

    void nhipDem() {
        giayConLai--;
        if (giayConLai > 0 && giayConLai % 60 == 0) {
            final int con = giayConLai / 60;
            if (con == 2) {
                LoginHandler.stopLogin = true;
            }
            if (con <= 1) {
                AdminHandler.isStopServer = true;
                bao("Hệ thống sẽ bảo trì sau 1 phút. Vui lòng thoát game để tránh mất dữ liệu!");
            } else {
                bao("Hệ thống sẽ bảo trì sau " + con + " phút.");
            }
        }
        if (giayConLai <= 0) {
            dem.stop();
            giayConLai = 0;
            capNhat();
            final boolean kdl = khoiDongLai;
            final Thread t = new Thread(() -> ThaoTacMayChu.tatMayChu(kdl), "PANEL QUAN TRI - tat may");
            t.start();
            return;
        }
        capNhat();
    }

    void huyDem() {
        if (!HopThoai.xacNhan(this, "Huỷ đếm ngược", "Huỷ lịch tắt máy và mở lại đăng nhập?", "Huỷ lịch")) {
            return;
        }
        if (dem != null) {
            dem.stop();
        }
        giayConLai = -1;
        AdminHandler.isStopServer = false;
        LoginHandler.stopLogin = false;
        KiemToanQuanTri.ghi("DEM_NGUOC_HUY", "", "");
        bao("Thông báo: Lịch bảo trì đã được hủy bỏ.");
        capNhat();
    }

    void bao(final String s0) {
        HopThoai.chay(this, null, () -> {
            try {
                final io.Message m = real.MessageCreator.createthongbao(s0);
                for (int j = 0; j < real.CharManager.instance.vChars.size(); ++j) {
                    real.CharManager.instance.vChars.elementAt(j).sendMessage(m);
                }
                return "Đã báo: " + s0;
            } catch (final Exception e) {
                return "Lỗi gửi thông báo bảo trì: " + e.getMessage();
            }
        }, null);
    }

    // ------------------------------------------------------------------ tep nhat ky

    /** Thu muc nhat ky: sv.pathLog (+ nhat_ky/), thu muc lam viec (*.txt, *.log). */
    static List<File> timTepNhatKy() {
        final List<File> l = new ArrayList<File>();
        final String pl = ThaoTacMayChu.docServerIni().getProperty("sv.pathLog", "").trim();
        final List<File> thuMuc = new ArrayList<File>();
        if (!pl.isEmpty()) {
            thuMuc.add(new File(pl, "nhat_ky"));
            thuMuc.add(new File(pl));
        }
        thuMuc.add(new File("log"));
        thuMuc.add(new File("."));
        for (final File d : thuMuc) {
            final File[] ds = d.listFiles();
            if (ds == null) {
                continue;
            }
            for (final File f : ds) {
                final String n = f.getName().toLowerCase();
                if (f.isFile() && (n.endsWith(".log") || n.endsWith(".out") || (d.getPath().equals(".") && n.endsWith(".txt")))) {
                    boolean co = false;
                    for (final File x : l) {
                        if (x.getAbsoluteFile().equals(f.getAbsoluteFile())) {
                            co = true;
                        }
                    }
                    if (!co) {
                        l.add(f);
                    }
                }
            }
        }
        l.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        return l;
    }

    static String loaiTep(final String ten) {
        final String t = ten.toLowerCase();
        if (t.startsWith("may_chu")) {
            return "Nhật ký máy chủ";
        }
        if (t.startsWith("kiem_toan") || t.startsWith("adminpanel")) {
            return "Kiểm toán";
        }
        if (t.startsWith("game")) {
            return "Đầu ra máy chủ";
        }
        if (t.startsWith("login")) {
            return "Máy chủ đăng nhập";
        }
        return "Khác";
    }

    void docTep() {
        HopThoai.doc(TrangVanHanh::timTepNhatKy, l -> {
            dsTep.clear();
            dsTep.addAll(l);
            mhTep.setRowCount(0);
            for (final File f : l) {
                mhTep.addRow(new Object[]{f.getName(), loaiTep(f.getName()), DinhDang.so(Math.max(1, f.length() / 1024)) + " KB",
                    DinhDang.ngayGio(f.lastModified())});
            }
        });
    }
}
