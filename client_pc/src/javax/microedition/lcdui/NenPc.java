package javax.microedition.lcdui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayDeque;

/**
 * Cầu nối MIDP <-> máy tính (Java SE / AWT) cho bản PC Huyền Thoại Bảo Bối (code game = code bản jar J2ME),
 * cùng kiểu NenAndroid của bản APK:
 *
 * - Độ phân giải logic: cửa sổ thật chia một HỆ SỐ NGUYÊN k (cạnh ngắn logic >= canhNgan, mặc định 320 px:
 *   1280x720 -> 640x360 x2, 1920x1080 -> 640x360 x3, 2560x1080 -> 853x360 x3, 1024x768 -> 512x384 x2).
 *   Game vẽ thẳng vào bộ đệm cỡ thật với biến đổi phóng k: ảnh pixel láng giềng gần nhất (sắc nét, không méo),
 *   chữ + khung HD khử răng cưa ở độ phân giải thật. Phần dư (< k) là viền.
 * - Một luồng sự kiện (LuongSuKien) chạy tuần tự: chuột, phím, callSerially, paint - giống máy J2ME.
 * - Sau khi game vẽ: lớp phủ HD của bản PC (htbb.pc.LopPhu: gợi ý phím, cài đặt, hộp nhập...) ở toạ độ thật.
 */
public final class NenPc {
    /** cạnh ngắn logic nhỏ nhất (px) - cài đặt "Cỡ giao diện" đổi giá trị này */
    public static volatile int canhNgan = 320;
    /** hệ số ép (0 = tự động theo canhNgan) */
    public static volatile int heSoEp = 0;

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

    /** Tính độ phân giải logic từ cỡ vùng vẽ thật. true = đổi cỡ logic so với trước. */
    public static synchronized boolean tinhKichThuoc(int w, int h) {
        if (w <= 0 || h <= 0) return false;
        int ngan = Math.min(w, h);
        int s = heSoEp > 0 ? heSoEp : Math.max(1, ngan / Math.max(160, canhNgan));
        // logic không nhỏ hơn 240 px cạnh ngắn (giao diện game cần chỗ)
        while (s > 1 && ngan / s < 240) s--;
        int lw = w / s;
        int lh = h / s;
        thatW = w;
        thatH = h;
        lechX = (w - lw * s) / 2;
        lechY = (h - lh * s) / 2;
        heSo = s;
        boolean doi = lw != rong || lh != cao;
        rong = lw;
        cao = lh;
        return doi;
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
                // viền ngoài vùng game (cửa sổ không chia hết hệ số)
                g2.setColor(Color.BLACK);
                if (lx > 0) {
                    g2.fillRect(0, 0, lx, H);
                    g2.fillRect(lx + w * k, 0, W - lx - w * k, H);
                }
                if (ly > 0) {
                    g2.fillRect(0, 0, W, ly);
                    g2.fillRect(0, ly + h * k, W, H - ly - h * k);
                }
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
