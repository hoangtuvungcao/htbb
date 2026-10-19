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
    /**
     * 0.9.5: bộ đệm vẽ game = ảnh BỘ NHỚ (BufferedImage). Vẽ khử răng cưa (bảng, chữ HD, khung) vào VolatileImage
     * (bộ nhớ đồ hoạ) bắt Java2D đọc ngược điểm ảnh từ GPU cho mỗi hình -> mở bảng còn ~3 khung/giây trên Windows (D3D),
     * ~13 trên Linux. Ảnh bộ nhớ: vẽ phần mềm nhanh, chỉ chép lên GPU một lần mỗi khung trình bày.
     * -Dhtbb.volatile=true để thử lại đường cũ.
     */
    private boolean dungVolatile = "true".equals(System.getProperty("htbb.volatile"));
    private volatile boolean toanMan;
    private Rectangle cuaSoCu;
    /** cửa sổ đang phóng to trước khi vào toàn màn (ra toàn màn thì phóng to lại) */
    private boolean phongToCu;
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
        enableInputMethods(true);            // bộ gõ tiếng Việt (KEY_TYPED / InputMethodEvent)
        addKeyListener(this);
        // bộ gõ (ibus / fcitx / Unikey...): chữ đã chốt có thể đến bằng InputMethodEvent thay vì KEY_TYPED
        addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent e) {
                String s = NhapChu.chuDaChot(e);
                if (s.length() > 0) goChuoi(s);
                e.consume();
            }

            public void caretPositionChanged(java.awt.event.InputMethodEvent e) {
            }
        });
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        setPreferredSize(new Dimension(CaiDat.cuaSoRong, CaiDat.cuaSoCao));
        setMinimumSize(new Dimension(NenPc.LOGIC_W, NenPc.LOGIC_H));
        khung.add(this);
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
        // phóng to / thu nhỏ (trình quản lý cửa sổ có thể báo cỡ muộn): tính lại + lấy lại focus gõ phím
        khung.addWindowStateListener(new java.awt.event.WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                if (!toanMan) CaiDat.phongTo = (e.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
                doiCoMuon();
            }
        });
        khung.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                requestFocusInWindow();
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                doiCo();
            }

            public void componentShown(ComponentEvent e) {
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
        Insets in = khung.getInsets();
        khung.setMinimumSize(new Dimension(NenPc.LOGIC_W + in.left + in.right, NenPc.LOGIC_H + in.top + in.bottom));
        khung.setLocationRelativeTo(null);
        khung.setVisible(true);
        if (CaiDat.phongTo && !CaiDat.toanMan) khung.setExtendedState(Frame.MAXIMIZED_BOTH);
        createBufferStrategy(2);
        requestFocus();
        if (CaiDat.toanMan) datToanMan(true);
        doiCo();
    }

    /** Cỡ đổi theo trạng thái cửa sổ: tính ngay + lại sau 150 / 600 ms (trình quản lý cửa sổ báo cỡ muộn). */
    void doiCoMuon() {
        doiCo();
        final int[] cho = {150, 600};
        for (int i = 0; i < cho.length; i++) {
            javax.swing.Timer t = new javax.swing.Timer(cho[i], new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doiCo();
                    requestFocusInWindow();
                }
            });
            t.setRepeats(false);
            t.start();
        }
    }

    /**
     * Cửa sổ đổi cỡ -> tính lại hệ số phóng + viền (vùng chơi logic CỐ ĐỊNH 640x360), vẽ lại ngay. Lưu cỡ cửa sổ
     * chỉ khi ở trạng thái thường (không phóng to / toàn màn) - mở lại đúng cỡ người chơi kéo.
     */
    void doiCo() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;
        if (NenPc.tinhKichThuoc(w, h)) NenPc.baoDoiCo();
        else NenPc.luong().yeuCauVe();
        if (!toanMan && (khung.getExtendedState() & Frame.MAXIMIZED_BOTH) == 0) {
            CaiDat.cuaSoRong = w;
            CaiDat.cuaSoCao = h;
        }
    }

    /** Đặt cỡ vùng chơi (cài đặt "Độ phân giải"). */
    public void datCo(final int w, final int h) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (toanMan) datToanMan(false);
                // đang phóng to: về cửa sổ thường trước (pack() không đổi cỡ cửa sổ phóng to -> lệch cỡ / đen)
                if ((khung.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0) khung.setExtendedState(Frame.NORMAL);
                CaiDat.phongTo = false;
                setPreferredSize(new Dimension(w, h));
                khung.pack();
                Rectangle man = khung.getGraphicsConfiguration().getBounds();
                Rectangle r = khung.getBounds();
                if (r.width > man.width || r.height > man.height) {
                    Insets in = khung.getInsets();
                    khung.setSize(Math.min(r.width, man.width), Math.min(r.height, man.height - in.top));
                }
                khung.setLocationRelativeTo(null);
                doiCoMuon();
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
                GraphicsConfiguration gcf = khung.getGraphicsConfiguration();
                Rectangle manHinh = gcf != null ? gcf.getBounds() : new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                int trangThai = khung.getExtendedState();
                synchronized (khoaTruoc) {
                    khung.dispose();
                }
                if (bat) {
                    phongToCu = (trangThai & Frame.MAXIMIZED_BOTH) != 0;
                    if (!phongToCu) cuaSoCu = khung.getBounds();
                    khung.setExtendedState(Frame.NORMAL);
                    khung.setUndecorated(true);
                    khung.setBounds(manHinh);
                } else {
                    khung.setUndecorated(false);
                    if (cuaSoCu != null) khung.setBounds(cuaSoCu);
                    else khung.pack();
                }
                toanMan = bat;
                CaiDat.toanMan = bat;
                khung.setVisible(true);
                if (!bat && phongToCu) khung.setExtendedState(Frame.MAXIMIZED_BOTH);
                createBufferStrategy(2);
                requestFocus();
                doiCoMuon();
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
            int tw = t == null ? 0 : t.getWidth(null), th = t == null ? 0 : t.getHeight(null);
            if (t == null) {
                g.setPaint(new java.awt.GradientPaint(0, 0, new Color(0x0B1630), 0, Math.max(1, H), new Color(0x1B2F5C)));
                g.fillRect(0, 0, W, H);
            } else if (tw == W && th == H) {
                g.drawImage(t, 0, 0, null);
            } else {
                // vừa đổi hệ số (khung game cũ khác cỡ): phóng khung cũ cho kín tới khi có khung mới - không đen / rác
                g.drawImage(t, 0, 0, W, H, null);
            }
            khungMoi = false;
        }
        HieuUngHD.ve(g, W, H);
        LopPhu.ve(g, W, H);
    }

    /** Đưa khung lên cửa sổ (luồng trình bày). */
    /** ảnh hợp thành (khung game + hiệu ứng HD + lớp phủ) cỡ bộ đệm, rồi phóng / thu vào khung trình bày */
    private VolatileImage hopV;
    /** 0.9.5: ảnh hợp thành trong bộ nhớ (lớp phủ khử răng cưa vẽ phần mềm, một lần chép lên GPU) */
    private BufferedImage hopB;
    /** thời gian trình bày trung bình (µs, cửa sổ 1 giây) - bộ đo khung */
    public static volatile long tbTrinhBay;
    private long tbCong, tbDem, tbMoc;

    private void trinhBay() {
        int W = getWidth(), H = getHeight();
        if (W <= 0 || H <= 0) return;
        if (W != NenPc.cuaSoW || H != NenPc.cuaSoH) {
            // cỡ đổi mà chưa có sự kiện (trình quản lý cửa sổ báo muộn): tự tính lại
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    doiCo();
                }
            });
        }
        int bw = NenPc.thatW, bh = NenPc.thatH;
        int hx = NenPc.hienX, hy = NenPc.hienY, hw = NenPc.hienW, hh = NenPc.hienH;
        String chup = chupVao;
        if (chup != null) {
            BufferedImage b = new BufferedImage(bw, bh, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = b.createGraphics();
            hopThanh(g, bw, bh);
            g.dispose();
            luuAnh(b, chup);
            chupVao = null;
        }
        BufferStrategy bs = getBufferStrategy();
        if (bs == null || !isDisplayable()) return;
        boolean thang = hw == bw && hh == bh;   // vừa khít hệ số nguyên: chép thẳng, không phóng
        long t0 = System.nanoTime();
        try {
            Image hop = null;
            if (!dungVolatile) {
                // 0.9.5: hợp thành trong ảnh bộ nhớ (vẽ khử răng cưa nhanh), rồi chép / phóng lên bộ đệm cửa sổ một lần
                if (hopB == null || hopB.getWidth() != bw || hopB.getHeight() != bh) {
                    hopB = new BufferedImage(bw, bh, BufferedImage.TYPE_INT_RGB);
                }
                Graphics2D g = hopB.createGraphics();
                hopThanh(g, bw, bh);
                g.dispose();
                hop = hopB;
                thang = false;
            } else if (!thang) {
                GraphicsConfiguration gc = getGraphicsConfiguration();
                if (hopV == null || hopV.getWidth() != bw || hopV.getHeight() != bh
                        || (gc != null && hopV.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE)) {
                    if (hopV != null) hopV.flush();
                    hopV = createVolatileImage(bw, bh);
                }
                if (hopV != null) {
                    Graphics2D g = hopV.createGraphics();
                    hopThanh(g, bw, bh);
                    g.dispose();
                    hop = hopV;
                }
            }
            do {
                do {
                    Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                    NenPc.veVien(g, W, H, hx, hy, hw, hh);
                    if (thang) {
                        g.translate(hx, hy);
                        g.clipRect(0, 0, bw, bh);
                        hopThanh(g, bw, bh);
                    } else if (hop != null) {
                        if (hw == bw && hh == bh) {
                            g.drawImage(hop, hx, hy, null);
                        } else {
                            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                            g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                            g.drawImage(hop, hx, hy, hw, hh, null);
                        }
                    }
                    g.dispose();
                } while (bs.contentsRestored());
                bs.show();
            } while (bs.contentsLost());
            Toolkit.getDefaultToolkit().sync();
            soTrinhBay++;
            tbCong += System.nanoTime() - t0;
            tbDem++;
            long ms = System.currentTimeMillis();
            if (ms - tbMoc >= 1000) {
                tbTrinhBay = tbDem == 0 ? 0 : tbCong / tbDem / 1000;
                tbCong = 0;
                tbDem = 0;
                tbMoc = ms;
            }
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
        if (!NhapChu.laChuGo(e)) return;   // Ctrl+V... là phím tắt, không phải chữ
        goChuoi(String.valueOf(e.getKeyChar()));
    }

    /** Chữ gõ (KEY_TYPED hoặc chữ bộ gõ đã chốt) -> lớp phủ (hộp nhập) hoặc ô nhập của game. */
    void goChuoi(final String s) {
        NenPc.luong().them(new Runnable() {
            public void run() {
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (LopPhu.goChu(c)) continue;
                    BanPhim.goChu(c);
                }
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
                final int x = NenPc.vaoBoDemX(e.getX()), y = NenPc.vaoBoDemY(e.getY());
                NenPc.luong().them(new Runnable() {
                    public void run() {
                        if (LopPhu.chuotPhai(x, y)) return;
                        // chuột phải = menu ngữ cảnh TUỲ CHỌN (người chơi: giao dịch / tổ đội / nhắn); trống = Esc
                        boolean xong = false;
                        if (NenPc.trongKhung(x, y) && BanPhim.trongTheGioi()) {
                            try {
                                xong = game.pokemon.HanhDong.chuotPhai(NenPc.logicX(x), NenPc.logicY(y));
                            } catch (Throwable t) {
                                NhatKy.loi("chuot phai", t);
                            }
                        }
                        if (!xong) BanPhim.nhanPhim(KeyEvent.VK_ESCAPE, 0, false);
                    }
                });
            }
            return;
        }
        final int x = NenPc.vaoBoDemX(e.getX()), y = NenPc.vaoBoDemY(e.getY());
        chuotX = x;
        chuotY = y;
        if (LopPhu.chuot(0, x, y)) {
            chuotTrongLopPhu = true;
            return;
        }
        chuotTrongLopPhu = false;
        if (!NenPc.trongKhung(x, y)) return;   // bấm vào viền ngoài khung game: bỏ qua
        dangKeo = true;
        NenPc.cham(0, x, y);
    }

    public void mouseReleased(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) return;
        int x = NenPc.vaoBoDemX(e.getX()), y = NenPc.vaoBoDemY(e.getY());
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
        chuotX = NenPc.vaoBoDemX(e.getX());
        chuotY = NenPc.vaoBoDemY(e.getY());
        if (chuotTrongLopPhu) {
            LopPhu.chuot(1, chuotX, chuotY);
            return;
        }
        if (dangKeo) NenPc.cham(1, chuotX, chuotY);
    }

    public void mouseMoved(MouseEvent e) {
        chuotX = NenPc.vaoBoDemX(e.getX());
        chuotY = NenPc.vaoBoDemY(e.getY());
        boolean bam = LopPhu.diQua(chuotX, chuotY);
        if (!bam && NenPc.trongKhung(chuotX, chuotY)) {
            try {
                bam = game.pokemon.HanhDong.laVungBam(NenPc.logicX(chuotX), NenPc.logicY(chuotY));
            } catch (Throwable t) {
                bam = false;
            }
        }
        Cursor c = bam ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor();
        if (getCursor() != c) setCursor(c);
        if (!NenPc.trongKhung(chuotX, chuotY) && !bam) {
            chuotX = -1;   // trên viền: không di chuột lớp phủ
            chuotY = -1;
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        final int n = e.getWheelRotation();
        final int x = NenPc.vaoBoDemX(e.getX()), y = NenPc.vaoBoDemY(e.getY());
        if (n == 0) return;
        NenPc.luong().them(new Runnable() {
            public void run() {
                if (LopPhu.cuon(n, x, y)) return;
                if (BanPhim.trongTheGioi()) return;   // bản đồ không thu phóng (vùng nhìn cố định)
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
