package htbb.pc;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;

/**
 * Cửa sổ game (AWT Frame + Canvas, vẽ chủ động bằng BufferStrategy): cỡ đổi được (mặc định 1280x720,
 * 16:9 / 21:9 / 4:3...), toàn màn hình (F11 / Alt+Enter), bộ đệm cỡ thật (VolatileImage - Windows/macOS tăng tốc
 * phần cứng), nhận phím / chuột / con lăn -> BanPhim / LopPhu / NenPc.
 */
public final class ManHinh extends Canvas implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    public static volatile ManHinh me;

    final Frame khung;
    private VolatileImage demV;
    private BufferedImage demB;
    private boolean dungVolatile = !"false".equals(System.getProperty("htbb.volatile"));
    private volatile boolean toanMan;
    private Rectangle cuaSoCu;
    /** chuột thật (cho lớp phủ di chuột) */
    volatile int chuotX = -1;
    volatile int chuotY = -1;
    private volatile boolean dangKeo;
    private volatile boolean chuotTrongLopPhu;
    /** yêu cầu chụp màn (F12 / --chup) */
    volatile String chupVao;

    ManHinh(String tieuDe, List<Image> bieuTuong) {
        khung = new Frame(tieuDe);
        if (bieuTuong != null && !bieuTuong.isEmpty()) khung.setIconImages(bieuTuong);
        khung.setBackground(Color.BLACK);
        setBackground(Color.BLACK);
        setIgnoreRepaint(true);
        khung.setIgnoreRepaint(true);
        setFocusTraversalKeysEnabled(false); // Tab = đổi mục tiêu
        khung.setFocusTraversalKeysEnabled(false);
        enableInputMethods(true);            // bộ gõ tiếng Việt (KEY_TYPED)
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        setPreferredSize(new Dimension(CaiDat.cuaSoRong, CaiDat.cuaSoCao));
        khung.add(this);
        khung.setMinimumSize(new Dimension(640, 400));
        khung.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Chay.thoat();
            }

            public void windowIconified(WindowEvent e) {
                NenPc.anHien(false);
            }

            public void windowDeiconified(WindowEvent e) {
                NenPc.anHien(true);
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                doiCo();
            }
        });
        addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                NenPc.luong().them(new Runnable() {
                    public void run() {
                        BanPhim.thaHet();
                    }
                });
            }
        });
        me = this;
    }

    void hien() {
        khung.pack();
        khung.setLocationRelativeTo(null);
        khung.setVisible(true);
        createBufferStrategy(2);
        requestFocus();
        if (CaiDat.toanMan) datToanMan(true);
        doiCo();
    }

    /** Cửa sổ đổi cỡ -> tính lại độ phân giải logic, báo game. */
    void doiCo() {
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());
        if (NenPc.tinhKichThuoc(w, h)) NenPc.baoDoiCo();
        if (!toanMan && w > 1) {
            CaiDat.cuaSoRong = w;
            CaiDat.cuaSoCao = h;
        }
    }

    /** Đặt cỡ vùng chơi (cài đặt "Độ phân giải"). */
    public void datCo(final int w, final int h) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (toanMan) datToanMan(false);
                setPreferredSize(new Dimension(w, h));
                khung.pack();
                Rectangle man = khung.getGraphicsConfiguration().getBounds();
                Rectangle r = khung.getBounds();
                if (r.width > man.width || r.height > man.height) {
                    Insets in = khung.getInsets();
                    khung.setSize(Math.min(r.width, man.width), Math.min(r.height, man.height - in.top));
                }
                khung.setLocationRelativeTo(null);
                doiCo();
            }
        });
    }

    public boolean laToanMan() {
        return toanMan;
    }

    /** Toàn màn hình kiểu "cửa sổ không viền phủ màn" (đổi qua lại nhanh, không đổi chế độ màn). */
    public void datToanMan(final boolean bat) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (bat == toanMan) return;
                GraphicsDevice gd = khung.getGraphicsConfiguration().getDevice();
                khung.dispose();
                if (bat) {
                    cuaSoCu = khung.getBounds();
                    khung.setUndecorated(true);
                    khung.setBounds(gd.getDefaultConfiguration().getBounds());
                } else {
                    khung.setUndecorated(false);
                    if (cuaSoCu != null) khung.setBounds(cuaSoCu);
                    else khung.pack();
                }
                toanMan = bat;
                CaiDat.toanMan = bat;
                khung.setVisible(true);
                createBufferStrategy(2);
                requestFocus();
                doiCo();
            }
        });
    }

    // ---------------------------------------------------------------- vẽ (luồng sự kiện NenPc)

    /** Graphics2D của bộ đệm cỡ W x H (tạo lại khi đổi cỡ / mất nội dung). null = chưa vẽ được. */
    public Graphics2D batDauVe(int W, int H) {
        if (!isDisplayable()) return null;
        if (dungVolatile) {
            try {
                GraphicsConfiguration gc = getGraphicsConfiguration();
                if (gc == null) return null;
                if (demV == null || demV.getWidth() != W || demV.getHeight() != H) {
                    if (demV != null) demV.flush();
                    demV = createVolatileImage(W, H);
                    if (demV == null) return null;
                    Graphics2D g = demV.createGraphics();
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, W, H);
                    g.dispose();
                } else if (demV.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
                    demV = createVolatileImage(W, H);
                }
                return demV.createGraphics();
            } catch (Throwable e) {
                NhatKy.loi("VolatileImage - dung BufferedImage", e);
                dungVolatile = false;
            }
        }
        if (demB == null || demB.getWidth() != W || demB.getHeight() != H) {
            demB = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        }
        return demB.createGraphics();
    }

    /** Bộ đệm sau (khung game đang vẽ) - Zoom.phuLe đọc lại. */
    public Image demSau() {
        return dungVolatile ? demV : demB;
    }

    // ---- bộ đệm trước (khung game xong gần nhất) + luồng trình bày 60 Hz ----
    private final Object khoaTruoc = new Object();
    private VolatileImage truocV;
    private BufferedImage truocB;
    private volatile boolean khungMoi;
    private Thread luongTrinhBay;

    private Image truoc() {
        return dungVolatile ? truocV : truocB;
    }

    /** Game vẽ xong một khung (luồng sự kiện): chép bộ đệm sau -> trước cho luồng trình bày. */
    public void xongVe() {
        Image sau = dungVolatile ? demV : demB;
        if (sau == null) return;
        int W = sau.getWidth(null), H = sau.getHeight(null);
        synchronized (khoaTruoc) {
            try {
                if (dungVolatile) {
                    GraphicsConfiguration gc = getGraphicsConfiguration();
                    if (truocV == null || truocV.getWidth() != W || truocV.getHeight() != H
                            || (gc != null && truocV.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE)) {
                        if (truocV != null) truocV.flush();
                        truocV = createVolatileImage(W, H);
                    }
                    if (truocV == null) return;
                    Graphics2D g = truocV.createGraphics();
                    g.drawImage(sau, 0, 0, null);
                    g.dispose();
                } else {
                    if (truocB == null || truocB.getWidth() != W || truocB.getHeight() != H) {
                        truocB = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
                    }
                    Graphics2D g = truocB.createGraphics();
                    g.drawImage(sau, 0, 0, null);
                    g.dispose();
                }
            } catch (Throwable e) {
                return;
            }
            khungMoi = true;
            khoaTruoc.notifyAll();
        }
        batLuongTrinhBay();
    }

    private synchronized void batLuongTrinhBay() {
        if (luongTrinhBay != null) return;
        luongTrinhBay = new Thread(new Runnable() {
            public void run() {
                vongTrinhBay();
            }
        }, "HTBB-trinh-bay");
        luongTrinhBay.setDaemon(true);
        luongTrinhBay.setPriority(Thread.NORM_PRIORITY + 1);
        luongTrinhBay.start();
    }

    /** số khung trình bày (đo FPS hiển thị) */
    public static volatile long soTrinhBay;

    /**
     * Luồng trình bày: hiệu ứng cao = 60 khung/giây (khung game 25/giây + hiệu ứng HD, chuyển cảnh, lớp phủ mượt
     * theo thời gian thật); hiệu ứng thấp = chỉ khi game có khung mới.
     */
    private void vongTrinhBay() {
        long ke = System.nanoTime();
        final long buoc = 1000000000L / 60;
        while (true) {
            try {
                if (CaiDat.hieuUngCao) {
                    long cho = ke - System.nanoTime();
                    if (cho > 0) Thread.sleep(cho / 1000000L, (int) (cho % 1000000L));
                    ke += buoc;
                    long tre = System.nanoTime() - ke;
                    if (tre > buoc * 4) ke = System.nanoTime();
                } else {
                    synchronized (khoaTruoc) {
                        if (!khungMoi) khoaTruoc.wait(100);
                    }
                    ke = System.nanoTime();
                }
                trinhBay();
            } catch (InterruptedException e) {
                return;
            } catch (Throwable e) {
                NhatKy.loi("trinh bay", e);
            }
        }
    }

    /** Vẽ khung game + hiệu ứng HD + lớp phủ lên g (toạ độ thật). */
    private void hopThanh(Graphics2D g, int W, int H) {
        synchronized (khoaTruoc) {
            Image t = truoc();
            if (t != null) g.drawImage(t, 0, 0, null);
            khungMoi = false;
        }
        HieuUngHD.ve(g, W, H);
        LopPhu.ve(g, W, H);
    }

    /** Đưa khung lên cửa sổ (luồng trình bày). */
    private void trinhBay() {
        int W = javax.microedition.lcdui.NenPc.thatW, H = javax.microedition.lcdui.NenPc.thatH;
        String chup = chupVao;
        if (chup != null) {
            BufferedImage b = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = b.createGraphics();
            hopThanh(g, W, H);
            g.dispose();
            luuAnh(b, chup);
            chupVao = null;
        }
        BufferStrategy bs = getBufferStrategy();
        if (bs == null || !isDisplayable()) return;
        try {
            do {
                do {
                    Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                    hopThanh(g, W, H);
                    g.dispose();
                } while (bs.contentsRestored());
                bs.show();
            } while (bs.contentsLost());
            Toolkit.getDefaultToolkit().sync();
            soTrinhBay++;
        } catch (Throwable e) {
            // cửa sổ đang đổi chế độ
        }
    }

    private void luuAnh(Image dem, String tep) {
        try {
            int w = dem.getWidth(null);
            int h = dem.getHeight(null);
            BufferedImage b = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = b.createGraphics();
            g.drawImage(dem, 0, 0, null);
            g.dispose();
            File f = new File(tep);
            if (f.getParentFile() != null) f.getParentFile().mkdirs();
            ImageIO.write(b, "png", f);
            NhatKy.ghi("chup man -> " + f.getAbsolutePath());
            if (baoChup) LopPhu.bao("Đã chụp màn hình: " + f.getName());
            baoChup = false;
        } catch (Throwable e) {
            NhatKy.loi("chup man", e);
        }
    }

    /** F12: chụp màn vào <thư mục dữ liệu>/anh_chup/. */
    private volatile boolean baoChup;

    void chupMan() {
        baoChup = true;
        File d = new File(CaiDat.thuMucDuLieu(), "anh_chup");
        chupVao = new File(d, "HTBB-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".png").getPath();
    }

    // ---------------------------------------------------------------- phím
    static final boolean GHI_NHAP = Boolean.getBoolean("htbb.ghiNhap");

    public void keyPressed(KeyEvent e) {
        final int vk = e.getKeyCode();
        if (GHI_NHAP) NhatKy.ghi("phim " + KeyEvent.getKeyText(vk) + " (" + vk + ")");
        final int mod = e.getModifiersEx();
        if (vk == KeyEvent.VK_ENTER && (mod & KeyEvent.ALT_DOWN_MASK) != 0) {
            datToanMan(!toanMan);
            e.consume();
            return;
        }
        final int hd = BanPhim.hanhDong(vk);
        if (hd == BanPhim.TOAN_MAN) {
            datToanMan(!toanMan);
            return;
        }
        if (hd == BanPhim.CHUP) {
            chupMan();
            return;
        }
        e.consume();
        NenPc.luong().them(new Runnable() {
            boolean lap = BanPhimGiu.nhan(vk);

            public void run() {
                if (LopPhu.phim(vk, mod)) return;
                if (hd == BanPhim.CAI_DAT && !lap) {
                    LopPhu.moCaiDat();
                    return;
                }
                BanPhim.nhanPhim(vk, mod, lap);
            }
        });
    }

    public void keyReleased(KeyEvent e) {
        final int vk = e.getKeyCode();
        BanPhimGiu.tha(vk);
        NenPc.luong().them(new Runnable() {
            public void run() {
                BanPhim.thaPhim(vk);
            }
        });
    }

    public void keyTyped(KeyEvent e) {
        final char c = e.getKeyChar();
        if (c == KeyEvent.CHAR_UNDEFINED) return;
        NenPc.luong().them(new Runnable() {
            public void run() {
                if (LopPhu.goChu(c)) return;
                BanPhim.goChu(c);
            }
        });
    }

    /** Phím đang giữ (phân biệt nhấn lần đầu / tự lặp của hệ điều hành). */
    static final class BanPhimGiu {
        private static final java.util.HashSet<Integer> GIU = new java.util.HashSet<Integer>();

        static synchronized boolean nhan(int vk) {
            return !GIU.add(vk);
        }

        static synchronized void tha(int vk) {
            GIU.remove(vk);
        }
    }

    // ---------------------------------------------------------------- chuột
    public void mousePressed(MouseEvent e) {
        requestFocus();
        if (GHI_NHAP) NhatKy.ghi("chuot " + e.getButton() + " " + e.getX() + "," + e.getY() + " focus=" + isFocusOwner());
        if (e.getButton() != MouseEvent.BUTTON1) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                final int x = e.getX(), y = e.getY();
                NenPc.luong().them(new Runnable() {
                    public void run() {
                        if (!LopPhu.chuotPhai(x, y)) BanPhim.nhanPhim(KeyEvent.VK_ESCAPE, 0, false);
                    }
                });
            }
            return;
        }
        final int x = e.getX(), y = e.getY();
        chuotX = x;
        chuotY = y;
        if (LopPhu.chuot(0, x, y)) {
            chuotTrongLopPhu = true;
            return;
        }
        chuotTrongLopPhu = false;
        dangKeo = true;
        NenPc.cham(0, x, y);
    }

    public void mouseReleased(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) return;
        int x = e.getX(), y = e.getY();
        if (chuotTrongLopPhu) {
            LopPhu.chuot(2, x, y);
            chuotTrongLopPhu = false;
            return;
        }
        if (dangKeo) {
            dangKeo = false;
            NenPc.cham(2, x, y);
        }
    }

    public void mouseDragged(MouseEvent e) {
        chuotX = e.getX();
        chuotY = e.getY();
        if (chuotTrongLopPhu) {
            LopPhu.chuot(1, chuotX, chuotY);
            return;
        }
        if (dangKeo) NenPc.cham(1, chuotX, chuotY);
    }

    public void mouseMoved(MouseEvent e) {
        chuotX = e.getX();
        chuotY = e.getY();
        setCursor(LopPhu.diQua(chuotX, chuotY) ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        final int n = e.getWheelRotation();
        final int x = e.getX(), y = e.getY();
        if (n == 0) return;
        NenPc.luong().them(new Runnable() {
            public void run() {
                if (LopPhu.cuon(n, x, y)) return;
                if (BanPhim.trongTheGioi()) {
                    Zoom.doi(n < 0 ? 1 : -1);   // con lăn trong màn chơi = thu phóng bản đồ
                    return;
                }
                // danh sách / menu của game: con lăn = mũi tên lên / xuống
                int vk = n < 0 ? KeyEvent.VK_UP : KeyEvent.VK_DOWN;
                for (int i = 0; i < Math.min(3, Math.abs(n)); i++) {
                    if (!BanPhim.trongTheGioi()) {
                        BanPhim.nhanPhim(vk, 0, false);
                        BanPhim.thaPhim(vk);
                    }
                }
            }
        });
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        chuotX = -1;
        chuotY = -1;
    }

    static List<Image> napBieuTuong() {
        List<Image> ds = new ArrayList<Image>();
        int[] co = {16, 24, 32, 48, 64, 128, 256};
        for (int i = 0; i < co.length; i++) {
            try {
                java.io.InputStream in = ManHinh.class.getResourceAsStream("/htbb-pc/icon_" + co[i] + ".png");
                if (in != null) {
                    ds.add(ImageIO.read(in));
                    in.close();
                }
            } catch (Throwable e) {
                // bỏ qua
            }
        }
        return ds;
    }

    static boolean coManHinh() {
        return !GraphicsEnvironment.isHeadless();
    }

}
