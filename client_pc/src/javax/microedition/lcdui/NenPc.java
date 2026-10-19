package javax.microedition.lcdui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayDeque;

/**
 * Cầu nối MIDP <-> máy tính (Java SE / AWT) cho bản PC Huyền Thoại Bảo Bối (code game = code bản jar J2ME),
 * cùng kiểu NenAndroid của bản APK:
 *
 * - Độ phân giải logic CỐ ĐỊNH 640x360 (mọi cửa sổ thấy cùng vùng bản đồ - phóng to không thấy thêm), vẽ vào
 *   bộ đệm 640k x 360k (k nguyên), trình bày vừa cửa sổ + viền (khungHien; ManHinh vẽ, đổi toạ độ chuột).
 *   Game vẽ thẳng vào bộ đệm cỡ thật với biến đổi phóng k: ảnh pixel láng giềng gần nhất (sắc nét, không méo),
 *   chữ + khung HD khử răng cưa ở độ phân giải thật. Phần dư (< k) là viền.
 * - Một luồng sự kiện (LuongSuKien) chạy tuần tự: chuột, phím, callSerially, paint - giống máy J2ME.
 * - Sau khi game vẽ: lớp phủ HD của bản PC (htbb.pc.LopPhu: gợi ý phím, cài đặt, hộp nhập...) ở toạ độ thật.
 */
public final class NenPc {
    /** CŨ (cài đặt "Cỡ giao diện" đã bỏ - vùng chơi cố định): không còn tác dụng */
    public static volatile int canhNgan = 320;

    public static volatile int rong = 640;
    public static volatile int cao = 360;
    public static volatile int heSo = 2;
    public static volatile int lechX;
    public static volatile int lechY;
    public static volatile int thatW = 1280;
    public static volatile int thatH = 720;

    private static LuongSuKien luong;
    /** số khung đã vẽ (đo FPS) */
    public static volatile long soKhung;

    private NenPc() {
    }

    public static synchronized LuongSuKien luong() {
        if (luong == null) {
            luong = new LuongSuKien();
            luong.start();
        }
        return luong;
    }

    // ---------------- kích thước ----------------

    /** Vùng chơi CỐ ĐỊNH (logic) - mọi cửa sổ / màn hình thấy đúng chừng này (không lợi thế khi phóng to). */
    public static final int LOGIC_W = 640;
    public static final int LOGIC_H = 360;

    /**
     * Khung hiển thị cho vùng vẽ (cửa sổ) W x H: {k, hienX, hienY, hienW, hienH}. Game vẽ vào bộ đệm 640k x 360k
     * (k nguyên: ảnh pixel sắc nét, chữ / khung HD ở độ phân giải thật). Trình bày:
     * - k = phần nguyên của tỉ lệ vừa nhất nếu phủ >= 80% (1280x720 -> x2 vừa khít, 1493x731 -> x2 + viền);
     * - không thì vẽ k = làm tròn lên rồi thu nhỏ vừa cửa sổ (1280x646 -> vẽ x2, thu 0,897; nhỏ hơn 640x360 -> x1 thu nhỏ).
     * Phần dư là viền (tô nền, không đen). Toạ độ chuột đổi ngược qua cùng khung (vaoBoDemX/Y).
     */
    public static int[] khungHien(int W, int H) {
        float vua = Math.min(W / (float) LOGIC_W, H / (float) LOGIC_H);
        int k;
        float f;
        if (vua < 1f) {
            k = 1;
            f = vua;
        } else {
            int kn = (int) Math.floor(vua);
            if (kn / vua >= 0.8f) {
                k = kn;
                f = 1f;
            } else {
                k = (int) Math.ceil(vua);
                f = vua / k;
            }
        }
        if (k > 8) {
            f = f * k / 8f;
            k = 8;
        }
        int hw = Math.max(1, Math.round(LOGIC_W * k * f));
        int hh = Math.max(1, Math.round(LOGIC_H * k * f));
        if (f == 1f) {
            hw = LOGIC_W * k;
            hh = LOGIC_H * k;
        }
        return new int[]{k, (W - hw) / 2, (H - hh) / 2, hw, hh};
    }

    /** khung trình bày trên cửa sổ (toạ độ cửa sổ): bộ đệm thatW x thatH vẽ vào hienX, hienY, hienW x hienH */
    public static volatile int hienX, hienY, hienW = 1280, hienH = 720;
    public static volatile int cuaSoW = 1280, cuaSoH = 720;

    /**
     * Cỡ vùng vẽ (cửa sổ) đổi - phóng to / thu nhỏ / toàn màn: logic LUÔN 640x360 (mọi người thấy cùng vùng bản
     * đồ), bộ đệm game 640k x 360k, khung trình bày khungHien. true = lần đầu (báo game sizeChanged).
     */
    public static synchronized boolean tinhKichThuoc(int w, int h) {
        if (w <= 0 || h <= 0) return false;
        int[] kh = khungHien(w, h);
        int s = kh[0];
        cuaSoW = w;
        cuaSoH = h;
        hienX = kh[1];
        hienY = kh[2];
        hienW = kh[3];
        hienH = kh[4];
        thatW = LOGIC_W * s;
        thatH = LOGIC_H * s;
        lechX = 0;
        lechY = 0;
        heSo = s;
        boolean doi = rong != LOGIC_W || cao != LOGIC_H;
        rong = LOGIC_W;
        cao = LOGIC_H;
        return doi;
    }

    /** Toạ độ cửa sổ -> toạ độ bộ đệm game (thật). */
    public static int vaoBoDemX(int x) {
        return (int) Math.floor((x - hienX) * (double) thatW / Math.max(1, hienW));
    }

    public static int vaoBoDemY(int y) {
        return (int) Math.floor((y - hienY) * (double) thatH / Math.max(1, hienH));
    }

    /** Toạ độ bộ đệm nằm trong khung game. */
    public static boolean trongKhung(int x, int y) {
        return x >= 0 && y >= 0 && x < thatW && y < thatH;
    }

    /** Tô viền quanh khung game (nền xanh đêm chuyển sắc + chỉ vàng mảnh quanh khung) - không để khoảng đen. */
    public static void veVien(Graphics2D g2, int W, int H, int lx, int ly, int fw, int fh) {
        java.awt.Paint cu = g2.getPaint();
        g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(0x0B1630), 0, Math.max(1, H), new Color(0x1B2F5C)));
        if (lx > 0) {
            g2.fillRect(0, 0, lx, H);
            g2.fillRect(lx + fw, 0, W - lx - fw, H);
        }
        if (ly > 0) {
            g2.fillRect(0, 0, W, ly);
            g2.fillRect(0, ly + fh, W, H - ly - fh);
        }
        if (lx > 2 || ly > 2) {
            g2.setPaint(new Color(0xE8B83A));
            g2.drawRect(lx - 1, ly - 1, fw + 1, fh + 1);
            g2.setPaint(new Color(0, 0, 0, 120));
            g2.drawRect(lx - 2, ly - 2, fw + 3, fh + 3);
        }
        g2.setPaint(cu);
    }

    /** Báo đổi cỡ màn cho Canvas đang hiện (trên luồng sự kiện). */
    public static void baoDoiCo() {
        luong().them(new Runnable() {
            public void run() {
                Canvas c = Display.canvasCuoi;
                if (c != null) {
                    c.sizeChanged(rong, cao);
                    c.repaint();
                }
            }
        });
    }

    /** Toạ độ thật -> logic (kẹp trong màn). */
    public static int logicX(int x) {
        int lx = (x - lechX) / heSo;
        return lx < 0 ? 0 : (lx >= rong ? rong - 1 : lx);
    }

    public static int logicY(int y) {
        int ly = (y - lechY) / heSo;
        return ly < 0 ? 0 : (ly >= cao ? cao - 1 : ly);
    }

    // ---------------- nhập ----------------

    /** nhịp game lúc nhấn chuột (thả sớm hơn 2 nhịp thì hoãn: code game cần thấy "đang chạm" ít nhất 1 nhịp) */
    private static volatile int nhipNhan;
    private static volatile long lucNhan;

    /** Chuột: kieu 0 = nhấn, 1 = kéo, 2 = thả. Toạ độ thật. */
    public static void cham(final int kieu, int x, int y) {
        final int fx = logicX(x);
        final int fy = logicY(y);
        if (kieu == 0) {
            nhipNhan = game.render.Canvas.gameTick;
            lucNhan = System.currentTimeMillis();
        } else if (kieu == 2 && game.render.Canvas.gameTick - nhipNhan < 2 && game.render.Canvas.gameTick >= nhipNhan) {
            // cú bấm chuột rất nhanh (< 1 nhịp game 40 ms): thả muộn một chút như ngón tay chạm thật
            Thread t = new Thread(new Runnable() {
                public void run() {
                    long het = lucNhan + 200;
                    while (game.render.Canvas.gameTick - nhipNhan < 2 && game.render.Canvas.gameTick >= nhipNhan
                            && System.currentTimeMillis() < het) {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    guiCham(2, fx, fy);
                }
            }, "HTBB-tha-chuot");
            t.setDaemon(true);
            t.start();
            return;
        }
        guiCham(kieu, fx, fy);
    }

    private static void guiCham(final int kieu, final int fx, final int fy) {
        luong().them(new Runnable() {
            public void run() {
                Displayable d = Display.hienTai;
                if (!(d instanceof Canvas)) return;
                Canvas c = (Canvas) d;
                if (kieu == 0) c.pointerPressed(fx, fy);
                else if (kieu == 1) c.pointerDragged(fx, fy);
                else c.pointerReleased(fx, fy);
            }
        });
    }

    /** Phím J2ME: kieu 0 = nhấn, 1 = giữ lặp, 2 = thả. */
    public static void phim(final int kieu, final int ma) {
        luong().them(new Runnable() {
            public void run() {
                Displayable d = Display.hienTai;
                if (!(d instanceof Canvas)) return;
                Canvas c = (Canvas) d;
                if (kieu == 0) c.keyPressed(ma);
                else if (kieu == 1) c.keyRepeated(ma);
                else c.keyReleased(ma);
            }
        });
    }

    public static void anHien(final boolean hien) {
        luong().them(new Runnable() {
            public void run() {
                Canvas c = Display.canvasCuoi;
                if (c == null) return;
                if (hien) {
                    c.showNotify();
                    c.repaint();
                } else {
                    c.hideNotify();
                }
            }
        });
    }

    static boolean rung(int ms) {
        return false;
    }

    // ---------------- hộp nhập chữ (TextBox) ----------------

    static void hienNhap(TextBox tb) {
        htbb.pc.HopNhap.mo(tb);
    }

    static void anNhap() {
        htbb.pc.HopNhap.dong();
    }

    /** Lệnh của TextBox (OK / Huỷ) - gọi trên luồng sự kiện. */
    public static void goiLenh(final Displayable d, final Command c) {
        luong().them(new Runnable() {
            public void run() {
                if (c != null) {
                    d.goiLenh(c);
                } else if (Display.canvasCuoi != null && Display.hienTai == d) {
                    Display.getDisplay(null).setCurrent(Display.canvasCuoi);
                }
            }
        });
    }

    /** Các lệnh gắn vào Displayable (TextBox: OK / Huỷ). */
    public static java.util.Vector lenh(Displayable d) {
        return d.lenh;
    }

    public static Displayable hienTai() {
        return Display.hienTai;
    }

    // ---------------- luồng sự kiện + vẽ ----------------

    public static final class LuongSuKien extends Thread {
        private final ArrayDeque<Runnable> hang = new ArrayDeque<Runnable>();
        private boolean choVe;
        private long soYeuCau;
        private long soDaVe;
        private int loiVe;

        LuongSuKien() {
            super("HTBB-su-kien");
            setDaemon(true);
        }

        public void them(Runnable r) {
            synchronized (this) {
                hang.addLast(r);
                notifyAll();
            }
        }

        public void yeuCauVe() {
            synchronized (this) {
                if (!choVe) {
                    choVe = true;
                    soYeuCau++;
                }
                notifyAll();
            }
        }

        /** serviceRepaints: chờ khung đang yêu cầu vẽ xong (tối đa 250 ms để không treo game). */
        void choVe() {
            if (Thread.currentThread() == this) {
                boolean can;
                synchronized (this) {
                    can = choVe;
                    choVe = false;
                }
                if (can) ve();
                return;
            }
            synchronized (this) {
                if (!choVe) return;
                long dich = soYeuCau;
                long het = System.currentTimeMillis() + 250;
                while (soDaVe < dich) {
                    long con = het - System.currentTimeMillis();
                    if (con <= 0) break;
                    try {
                        wait(con);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }

        public void run() {
            while (true) {
                Runnable r = null;
                long dich = 0;
                synchronized (this) {
                    while (hang.isEmpty() && !choVe) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    if (!hang.isEmpty()) {
                        r = hang.pollFirst();
                    } else {
                        choVe = false;
                        dich = soYeuCau;
                    }
                }
                if (r != null) {
                    try {
                        r.run();
                    } catch (Throwable e) {
                        htbb.pc.NhatKy.loi("loi su kien", e);
                    }
                } else {
                    try {
                        ve();
                    } catch (Throwable e) {
                        htbb.pc.NhatKy.loi("loi ve", e);
                    }
                    synchronized (this) {
                        if (soDaVe < dich) soDaVe = dich;
                        notifyAll();
                    }
                }
            }
        }

        private void ve() {
            htbb.pc.ManHinh mh = htbb.pc.ManHinh.me;
            if (mh == null) return;
            int w = rong;
            int h = cao;
            int k = heSo;
            int lx = lechX;
            int ly = lechY;
            int W = thatW;
            int H = thatH;
            Graphics2D g2 = mh.batDauVe(W, H);
            if (g2 == null) return;
            try {
                // viền ngoài khung game cố định 640x360 x k
                veVien(g2, W, H, lx, ly, w * k, h * k);
                Canvas c = Display.canvasCuoi;
                if (c != null) {
                    Graphics2D gg = (Graphics2D) g2.create();
                    gg.translate(lx, ly);
                    gg.scale(k, k);
                    gg.clipRect(0, 0, w, h);
                    Graphics g = new Graphics(gg, w, h, k);
                    try {
                        c.paint(g);
                    } catch (Throwable e) {
                        if (loiVe++ < 20) htbb.pc.NhatKy.loi("loi paint", e);
                    }
                    gg.dispose();
                }
                htbb.pc.HieuUngHD.chotKhung();
            } finally {
                g2.dispose();
            }
            mh.xongVe();
            soKhung++;
        }
    }
}
