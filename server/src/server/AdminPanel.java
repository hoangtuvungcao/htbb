package server;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import server.quantri.AnhPokemon;
import server.quantri.DinhDang;
import server.quantri.DuLieuTrucTiep;
import server.quantri.GiaoDien;
import server.quantri.HopThoai;
import server.quantri.KiemToanQuanTri;
import server.quantri.ThanhPhan;
import server.quantri.ThaoTacMayChu;
import server.quantri.Trang;
import server.quantri.TrangCauHinh;
import server.quantri.TrangDichVu;
import server.quantri.TrangKinhTe;
import server.quantri.TrangNguoiChoi;
import server.quantri.TrangNhatKy;
import server.quantri.TrangSuKien;
import server.quantri.TrangTongQuan;
import server.quantri.TrangVanHanh;

import static server.quantri.GiaoDien.s;

/**
 * BANG DIEU KHIEN MAY CHU (cua so Swing) - mo khi server chay co man hinh (TeamServer.run, kiem headless).
 *
 * Giao dien Pokemon (dau trang Poke Ball, thanh ben co icon PMD, sang/toi, HiDPI) chia 8 trang:
 *   Tong quan · Nguoi choi · Su kien · Kinh te · Van hanh · Cau hinh · Dich vu · Nhat ky
 * (server.quantri.Trang*). Nghiep vu nam o server.quantri.ThaoTacMayChu - giu nguyen moi chuc nang cua
 * ban 12 nut cu (bao tri dem nguoc, dung dang nhap, don bo nho, thong bao 3 kieu, kick, khoa/mo khoa,
 * quan ly gem, danh sach online + menu "Chinh sua" cua server.Panel.ButtonEditor, doi mat khau, giam level).
 *
 * KHONG lam cham game: so lieu lay tren MOT luong nen do uu tien thap (1 lan/giay), trang chi cap nhat khi
 * dang hien va cua so khong thu nho, moi thao tac CSDL / hang loat chay trong SwingWorker, moi thao tac thay
 * doi trang thai deu hoi xac nhan va ghi kiem toan (AdminPanel_kiem_toan.txt + Nhat.kiemToan "ADMIN").
 */
public class AdminPanel extends JFrame {

    private final DuLieuTrucTiep du = new DuLieuTrucTiep();
    private final List<Trang> trang = new ArrayList<Trang>();
    private final JPanel than = new JPanel(new CardLayout());
    private ThanhBen thanhBen;
    private final DauTrang dauTrang;
    private final JLabel thongBaoNhanh = new JLabel(" ");
    private Timer nhip;
    private Trang hien;
    private long lanMoCam;
    private long lanKinhTe;
    private TrangNhatKy trangNhatKy;

    public AdminPanel() {
        super("Quản Lý Máy Chủ · " + tenGame());
        GiaoDien.khoiTao();
        try {
            GiaoDien.datToi(java.util.prefs.Preferences.userNodeForPackage(AdminPanel.class).getBoolean("toi", false));
        } catch (final Throwable ignored) {
        }
        dauTrang = new DauTrang();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        try {
            setIconImage(AnhPokemon.pokeBall(64));
        } catch (final Throwable ignored) {
        }
        final JPanel goc = noiDung(this);
        setContentPane(goc);
        GiaoDien.toCay(goc);
        final Dimension man = kichManHinh();
        final Dimension ep = kichThuocEp();
        if (ep != null) {
            setSize(ep);
        } else {
            setSize(Math.min(s(1440), man.width - 40), Math.min(s(900), man.height - 60));
        }
        setMinimumSize(new Dimension(Math.min(s(960), man.width - 40), Math.min(s(620), man.height - 60)));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                if (HopThoai.xacNhan(AdminPanel.this, "Đóng bảng điều khiển", "Đóng cửa sổ quản lý? Máy chủ game vẫn chạy tiếp"
                        + " (tắt máy chủ ở trang Vận hành).", "Đóng cửa sổ")) {
                    nhip.stop();
                    du.dung();
                    dispose();
                }
            }
        });
        HopThoai.baoNhanh = this::baoNhanh;
        KiemToanQuanTri.ghi("MO_BANG_DIEU_KHIEN", "", "cong=" + TeamServer.PORT + " font=" + GiaoDien.tenFont() + " tiLe=" + GiaoDien.tiLe);
        du.batDau();
        nhip = new Timer(1000, e -> moiGiay());
        nhip.setCoalesce(true);
        nhip.start();
        chon(0);
    }

    /** -Dhtbb.panel.kichThuoc=1280x720 (kiem thu bo cuc / man hinh nho). */
    static Dimension kichThuocEp() {
        try {
            final String v = System.getProperty("htbb.panel.kichThuoc", "").trim();
            if (v.matches("\\d+x\\d+")) {
                final String[] a = v.split("x");
                return new Dimension(Integer.parseInt(a[0]), Integer.parseInt(a[1]));
            }
        } catch (final Throwable ignored) {
        }
        return null;
    }

    /**
     * Co mo bang dieu khien khong: tat bang -Dhtbb.panel=0 hoac bien moi truong HTBB_PANEL=0 (vd may chu thu chay
     * tren may co man hinh nhung khong muon cua so hien len desktop). Mac dinh: Windows mo khi co man hinh;
     * Linux/macOS chi mo khi bat ro (-Dhtbb.panel=1 / HTBB_PANEL=1, vd chay_novadb/chay_lap.sh) de may chu
     * thu (bot, kiem thu) khong bat cua so len desktop cua nguoi dung.
     */
    public static boolean choPhepMo() {
        final String v = System.getProperty("htbb.panel", System.getenv("HTBB_PANEL"));
        if (v == null) {
            return System.getProperty("os.name", "").toLowerCase().startsWith("windows");
        }
        final String t = v.trim().toLowerCase();
        return !(t.equals("0") || t.equals("tat") || t.equals("off") || t.equals("false"));
    }

    static String tenGame() {
        try {
            return com.htbb.pokemon.ThuongHieu.TEN_GAME;
        } catch (final Throwable t) {
            return "Huyền Thoại Bảo Bối";
        }
    }

    static Dimension kichManHinh() {
        try {
            return java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        } catch (final Throwable t) {
            return new Dimension(1600, 1000);
        }
    }

    /**
     * Dung toan bo noi dung (dau trang + thanh ben + 8 trang + thanh trang thai) - khong can hien JFrame.
     * Kiem thu headless dung {@link #taoNoiDungThu()}.
     */
    JPanel noiDung(final AdminPanel khung) {
        final JPanel goc = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(final Graphics g) {
                g.setColor(GiaoDien.nen);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        final TrangTongQuan tq = new TrangTongQuan(du);
        final TrangVanHanh vh = new TrangVanHanh();
        trangNhatKy = new TrangNhatKy();
        trang.add(tq);
        trang.add(new TrangNguoiChoi(khung));
        trang.add(new TrangSuKien());
        trang.add(new TrangKinhTe());
        trang.add(vh);
        trang.add(new TrangCauHinh());
        trang.add(new TrangDichVu());
        trang.add(trangNhatKy);
        vh.khiMoTep(f -> {
            chon(trang.indexOf(trangNhatKy));
            trangNhatKy.moTep(f);
        });
        than.setOpaque(false);
        for (int i = 0; i < trang.size(); i++) {
            than.add(trang.get(i), String.valueOf(i));
        }
        thanhBen = new ThanhBen();
        goc.add(dauTrang, BorderLayout.NORTH);
        goc.add(thanhBen, BorderLayout.WEST);
        goc.add(than, BorderLayout.CENTER);
        final JPanel duoi = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(final Graphics g) {
                g.setColor(GiaoDien.nenThe);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(GiaoDien.vien);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        duoi.setBorder(new EmptyBorder(s(5), s(16), s(5), s(16)));
        thongBaoNhanh.setFont(GiaoDien.font(12));
        duoi.add(thongBaoNhanh, BorderLayout.CENTER);
        final JLabel phai = ThanhPhan.nhanPhu("Mọi thao tác đều được ghi nhật ký kiểm toán");
        phai.setToolTipText("Tệp kiểm toán: " + KiemToanQuanTri.tep().getAbsolutePath() + " · Java "
                + System.getProperty("java.version") + " · phông " + GiaoDien.tenFont());
        duoi.add(phai, BorderLayout.EAST);
        goc.add(duoi, BorderLayout.SOUTH);
        return goc;
    }

    /** Mo bang dieu khien neu co man hinh; headless -> null, khong nem loi. */
    public static AdminPanel moNeuCoManHinh() {
        if (GraphicsEnvironment.isHeadless() || !choPhepMo()) {
            return null;
        }
        final AdminPanel p = new AdminPanel();
        p.setVisible(true);
        return p;
    }

    /**
     * Kiem thu: dung 8 trang + thanh phan (JPanel dung duoc khi headless) ma KHONG tao JFrame.
     * Tra ve so trang da dung.
     */
    public static int taoNoiDungThu() {
        GiaoDien.khoiTao();
        final DuLieuTrucTiep d = new DuLieuTrucTiep();
        final List<JComponent> l = new ArrayList<JComponent>();
        l.add(new TrangTongQuan(d));
        l.add(new TrangNguoiChoi(null));
        l.add(new TrangSuKien());
        l.add(new TrangKinhTe());
        l.add(new TrangVanHanh());
        l.add(new TrangCauHinh());
        l.add(new TrangDichVu());
        l.add(new TrangNhatKy());
        for (final JComponent c : l) {
            c.setSize(1200, 800);
            c.doLayout();
            GiaoDien.toCay(c);
        }
        GiaoDien.datToi(true);
        GiaoDien.datToi(false);
        return l.size();
    }

    void chon(final int i) {
        if (i < 0 || i >= trang.size()) {
            return;
        }
        for (final Trang t : trang) {
            Trang.datHien(t, false);
        }
        hien = trang.get(i);
        Trang.datHien(hien, true);
        ((CardLayout) than.getLayout()).show(than, String.valueOf(i));
        if (thanhBen != null) {
            thanhBen.chon(i);
        }
        try {
            hien.lenDau();
            hien.khiHien();
            final DuLieuTrucTiep.Mau m = du.moiNhat();
            if (m != null) {
                hien.nhip(m);
            }
        } catch (final Throwable t) {
            baoNhanh("Lỗi trang: " + t);
        }
        hien.revalidate();
    }

    void moiGiay() {
        if (!isShowing() || (getExtendedState() & ICONIFIED) != 0) {
            return; // cua so an / thu nho: khong ve gi
        }
        final DuLieuTrucTiep.Mau m = du.moiNhat();
        dauTrang.capNhat(m);
        if (hien != null && m != null) {
            try {
                hien.nhip(m);
            } catch (final Throwable t) {
                baoNhanh("Lỗi cập nhật: " + t);
            }
        }
        if (System.currentTimeMillis() - lanKinhTe > 600000L) {
            // mau kinh te moi gio (tob_kinh_te_gio) cho bieu do Kinh te - chi doc tong, ngoai EDT
            lanKinhTe = System.currentTimeMillis();
            HopThoai.doc(server.quantri.KinhTeTrucTiep::ghiNeuCan, a -> {
            });
        }
        if (System.currentTimeMillis() - lanMoCam > 60000L) {
            lanMoCam = System.currentTimeMillis();
            HopThoai.doc(ThaoTacMayChu::moCamHetHan, n -> {
                if (n != null && n > 0) {
                    baoNhanh("Đã tự mở " + n + " lệnh khoá hết hạn.");
                }
            });
        }
    }

    void baoNhanh(final String s0) {
        if (s0 == null) {
            return;
        }
        final Runnable r = () -> {
            thongBaoNhanh.setText(DinhDang.gio(System.currentTimeMillis()) + "  ·  " + s0.replace('\n', ' '));
            thongBaoNhanh.setForeground(s0.startsWith("Lỗi") || s0.startsWith("Từ chối") || s0.startsWith("Không")
                    ? GiaoDien.DO : GiaoDien.chu);
        };
        TrangNhatKy.hoatDong(s0);
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    /** Giu cho server.Panel.ButtonEditor (menu "Chinh sua" cu): dong hoat dong + kiem toan. */
    public void addLog(final String message) {
        KiemToanQuanTri.ghi("CONG_CU_CU", "", message);
        baoNhanh(message);
    }

    void doiCheDo() {
        GiaoDien.datToi(!GiaoDien.toi);
        try {
            java.util.prefs.Preferences.userNodeForPackage(AdminPanel.class).putBoolean("toi", GiaoDien.toi);
        } catch (final Throwable ignored) {
        }
        GiaoDien.toCay(getContentPane());
        repaint();
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(AdminPanel::moNeuCoManHinh);
    }

    // ================================================================== dau trang (Poke Ball)

    final class DauTrang extends JComponent {
        private final BufferedImage linhVat = AnhPokemon.khungDung(25);
        private final BufferedImage linhVat2 = AnhPokemon.chanDung(25);
        private String trangThai = "Đang khởi động";
        private Color mauTrangThai = GiaoDien.XANH_LA;
        private String online = "—";
        private final int[] nutCheDo = new int[4];

        DauTrang() {
            setPreferredSize(new Dimension(10, s(84)));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (trongNut(e)) {
                        doiCheDo();
                    }
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(final MouseEvent e) {
                    final boolean tren = trongNut(e);
                    setCursor(Cursor.getPredefinedCursor(tren ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    setToolTipText(tren ? (GiaoDien.toi ? "Chuyển sang giao diện sáng" : "Chuyển sang giao diện tối") : null);
                }
            });
        }

        boolean trongNut(final MouseEvent e) {
            return e.getX() >= nutCheDo[0] && e.getX() <= nutCheDo[0] + nutCheDo[2] && e.getY() >= nutCheDo[1]
                    && e.getY() <= nutCheDo[1] + nutCheDo[3];
        }

        void capNhat(final DuLieuTrucTiep.Mau m) {
            if (m == null) {
                return;
            }
            final String tt;
            final Color mc;
            if (m.dangTat) {
                tt = "Đang tắt máy";
                mc = GiaoDien.DO;
            } else if (m.baoTri) {
                tt = "Bảo trì";
                mc = GiaoDien.VANG;
            } else if (m.dungDangNhap) {
                tt = "Dừng đăng nhập";
                mc = GiaoDien.VANG;
            } else {
                tt = "Đang hoạt động";
                mc = GiaoDien.XANH_LA;
            }
            final String on = DinhDang.so(m.online);
            if (!tt.equals(trangThai) || !on.equals(online)) {
                trangThai = tt;
                mauTrangThai = mc;
                online = on;
                repaint();
            }
        }

        @Override
        protected void paintComponent(final Graphics g0) {
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            final int w = getWidth(), h = getHeight();
            final int vach = s(6);
            // nua tren do (than Poke Ball) + vach den
            g.setPaint(new GradientPaint(0, 0, GiaoDien.toi ? new Color(0xB8290A) : new Color(0xF0441C), 0, h - vach,
                    GiaoDien.toi ? new Color(0x861D06) : GiaoDien.DO_DAM));
            g.fillRect(0, 0, w, h - vach);
            AnhPokemon.vePokeBall(g, w * 0.52f, -s(40), s(150), true);
            AnhPokemon.vePokeBall(g, w * 0.70f, s(20), s(90), true);
            g.setColor(new Color(0x22252E));
            g.fillRect(0, h - vach, w, vach);
            g.setColor(new Color(255, 255, 255, 40));
            g.fillRect(0, h - vach - s(2), w, s(2));
            // bieu tuong Poke Ball lon
            final int d = s(58);
            final int bx = s(18), by = (h - vach - d) / 2;
            g.setColor(new Color(0, 0, 0, 50));
            g.fill(new Ellipse2D.Float(bx + s(2), by + s(3), d, d));
            AnhPokemon.vePokeBall(g, bx, by, d, false);
            // logo chu vang vien xanh
            final Font fLogo = GiaoDien.dam(25);
            ThanhPhan.veChuVien(g, tenGame(), fLogo, bx + d + s(16), by + s(30), GiaoDien.VANG, GiaoDien.XANH_DUONG_DAM, GiaoDien.sf(5f));
            g.setFont(GiaoDien.dam(12));
            g.setColor(new Color(255, 255, 255, 230));
            g.drawString("Bảng điều khiển máy chủ".toUpperCase() + "  ·  cổng " + TeamServer.PORT, bx + d + s(18), by + s(50));
            // ben phai: linh vat, nut sang/toi, chip online, chip trang thai
            int x = w - s(16);
            final BufferedImage lv = linhVat != null ? linhVat : linhVat2;
            if (lv != null) {
                final int lh = s(60);
                final int lw = lh * lv.getWidth() / Math.max(1, lv.getHeight());
                g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(lv, x - lw, (h - vach - lh) / 2, lw, lh, null);
                x -= lw + s(14);
            }
            final int ch = s(32), cy = (h - vach - ch) / 2;
            final int nw = s(36);
            x -= nw;
            nutCheDo[0] = x;
            nutCheDo[1] = cy;
            nutCheDo[2] = nw;
            nutCheDo[3] = ch;
            g.setColor(new Color(255, 255, 255, 46));
            g.fill(new RoundRectangle2D.Float(x, cy, nw, ch, ch, ch));
            veMatTrangMatTroi(g, x + nw / 2f, cy + ch / 2f, s(9), GiaoDien.toi);
            x -= s(10);
            x = chip(g, x, cy, ch, "Online " + online, GiaoDien.VANG, false);
            x -= s(8);
            chip(g, x, cy, ch, trangThai, mauTrangThai, true);
            g.dispose();
        }

        int chip(final Graphics2D g, final int phai, final int y, final int h, final String chu, final Color cham, final boolean coCham) {
            g.setFont(GiaoDien.dam(12.5f));
            final int tw = g.getFontMetrics().stringWidth(chu);
            final int w = tw + s(coCham ? 40 : 26);
            final int x = phai - w;
            g.setColor(new Color(0, 0, 0, 60));
            g.fill(new RoundRectangle2D.Float(x, y, w, h, h, h));
            g.setColor(new Color(255, 255, 255, 60));
            g.setStroke(new BasicStroke(1f));
            g.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, h, h));
            int tx = x + s(13);
            if (coCham) {
                g.setColor(GiaoDien.trong(cham, 90));
                g.fill(new Ellipse2D.Float(tx - s(2), y + h / 2f - s(7), s(14), s(14)));
                g.setColor(cham);
                g.fill(new Ellipse2D.Float(tx + s(1), y + h / 2f - s(4), s(8), s(8)));
                tx += s(17);
            }
            g.setColor(Color.WHITE);
            g.drawString(chu, tx, y + h / 2 + g.getFontMetrics().getAscent() / 2 - s(2));
            return x;
        }
    }

    static void veMatTrangMatTroi(final Graphics2D g, final float cx, final float cy, final float r, final boolean toi) {
        g.setColor(Color.WHITE);
        if (toi) {
            g.fill(new Ellipse2D.Float(cx - r * 0.55f, cy - r * 0.55f, r * 1.1f, r * 1.1f));
            g.setStroke(new BasicStroke(GiaoDien.sf(1.6f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 8; i++) {
                final double a = Math.PI / 4 * i;
                g.draw(new java.awt.geom.Line2D.Double(cx + Math.cos(a) * r * 0.8, cy + Math.sin(a) * r * 0.8, cx + Math.cos(a) * r * 1.15,
                        cy + Math.sin(a) * r * 1.15));
            }
        } else {
            final java.awt.geom.Area a = new java.awt.geom.Area(new Ellipse2D.Float(cx - r, cy - r, 2 * r, 2 * r));
            a.subtract(new java.awt.geom.Area(new Ellipse2D.Float(cx - r * 0.35f, cy - r * 1.25f, 2 * r, 2 * r)));
            g.fill(a);
        }
    }

    // ================================================================== thanh ben

    final class ThanhBen extends JPanel {
        private final List<MucBen> muc = new ArrayList<MucBen>();

        ThanhBen() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(s(16), s(10), s(14), s(10)));
            setPreferredSize(new Dimension(s(214), 10));
            putClientProperty("giuMau", Boolean.TRUE);
            for (int i = 0; i < trang.size(); i++) {
                final MucBen m = new MucBen(i, trang.get(i).ten(), trang.get(i).icon());
                muc.add(m);
                add(m);
                add(javax.swing.Box.createVerticalStrut(s(4)));
            }
            add(javax.swing.Box.createVerticalGlue());
            final JLabel ghi = new JLabel("<html><div style='text-align:center'>" + tenGame() + "<br>máy chủ · cổng "
                    + TeamServer.PORT + "</div></html>", JLabel.CENTER);
            ghi.setFont(GiaoDien.font(11));
            ghi.setForeground(new Color(255, 255, 255, 150));
            ghi.setAlignmentX(0f);
            add(ghi);
        }

        void chon(final int i) {
            for (final MucBen m : muc) {
                m.chon = m.i == i;
                m.repaint();
            }
        }

        @Override
        protected void paintComponent(final Graphics g0) {
            final Graphics2D g = (Graphics2D) g0.create();
            g.setPaint(new GradientPaint(0, 0, GiaoDien.benTren, 0, getHeight(), GiaoDien.benDuoi));
            g.fillRect(0, 0, getWidth(), getHeight());
            GiaoDien.khuRang(g);
            AnhPokemon.vePokeBall(g, -s(60), getHeight() - s(190), s(200), true);
            g.dispose();
        }
    }

    final class MucBen extends JComponent {
        final int i;
        final String ten;
        final BufferedImage icon;
        boolean chon, tren;

        MucBen(final int i, final String ten, final String icon) {
            this.i = i;
            this.ten = ten;
            this.icon = AnhPokemon.phongTo(AnhPokemon.vatPham(icon), s(24), s(24));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, s(44)));
            setPreferredSize(new Dimension(s(190), s(44)));
            setAlignmentX(0f);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    AdminPanel.this.chon(MucBen.this.i);
                }

                @Override
                public void mouseEntered(final MouseEvent e) {
                    tren = true;
                    repaint();
                }

                @Override
                public void mouseExited(final MouseEvent e) {
                    tren = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(final Graphics g0) {
            final Graphics2D g = (Graphics2D) g0.create();
            GiaoDien.khuRang(g);
            final int w = getWidth(), h = getHeight();
            if (chon) {
                g.setColor(GiaoDien.toi ? new Color(0x2B3560) : Color.WHITE);
                g.fill(new RoundRectangle2D.Float(0, 0, w, h, s(14), s(14)));
                g.setColor(GiaoDien.VANG);
                g.fill(new RoundRectangle2D.Float(0, s(9), s(5), h - s(18), s(5), s(5)));
            } else if (tren) {
                g.setColor(GiaoDien.chonBen);
                g.fill(new RoundRectangle2D.Float(0, 0, w, h, s(14), s(14)));
            }
            final int o = s(32);
            g.setColor(chon ? GiaoDien.trong(GiaoDien.VANG, 110) : new Color(255, 255, 255, 30));
            g.fill(new Ellipse2D.Float(s(12), (h - o) / 2f, o, o));
            g.drawImage(icon, s(12) + (o - s(24)) / 2, (h - s(24)) / 2, null);
            g.setFont(chon ? GiaoDien.dam(13.5f) : GiaoDien.font(13.5f));
            g.setColor(chon ? (GiaoDien.toi ? Color.WHITE : GiaoDien.XANH_DUONG_DAM) : GiaoDien.chuBen);
            g.drawString(ten, s(54), h / 2 + g.getFontMetrics().getAscent() / 2 - s(2));
            g.dispose();
        }
    }
}
