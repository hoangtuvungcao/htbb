import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * Máy quay cho video giới thiệu: chạy CLIENT PC THẬT (code game jar/APK, đường vẽ bản PC) không cửa sổ, nối máy chủ
 * thật, vẽ từng khung vào ảnh 1920x1080 (k = 3, khớp đúng khung trình bày) và đẩy khung thô vào ffmpeg ở 30 khung/giây
 * theo đồng hồ thật (game chạy nhịp 25/giây, vẽ nội suy như cửa sổ thật). Không có lớp FPS / đo khung.
 *
 * Quay.java = khung chung (khởi động client, vẽ, ghi, nhập giả, phản chiếu); KichBanVideo.java = kịch bản từng cảnh.
 * Chạy: tools/video/quay.sh (xem đầu tệp đó).
 */
public class Quay {
    public static int W = Integer.getInteger("quay.w", 1920), H = Integer.getInteger("quay.h", 1080), FPS = 30;
    public static File raThuMuc;

    // ---------------- luồng sự kiện ----------------

    /** Chạy r trên luồng sự kiện NenPc (như máy J2ME: vẽ / chạm / phím tuần tự), chờ xong. */
    public static void tren(final Runnable r) {
        final Object k = new Object();
        final boolean[] xong = {false};
        NenPc.luong().them(new Runnable() {
            public void run() {
                try {
                    r.run();
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    synchronized (k) {
                        xong[0] = true;
                        k.notifyAll();
                    }
                }
            }
        });
        synchronized (k) {
            long het = System.currentTimeMillis() + 8000;
            while (!xong[0] && System.currentTimeMillis() < het) {
                try {
                    k.wait(50);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T tren(final Callable<T> c) {
        final Object[] kq = {null};
        tren(new Runnable() {
            public void run() {
                try {
                    kq[0] = c.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return (T) kq[0];
    }

    public static void ngu(double giay) {
        try {
            Thread.sleep((long) (giay * 1000));
        } catch (InterruptedException e) {
            // bỏ
        }
    }

    public interface DieuKien {
        boolean dung() throws Exception;
    }

    /** chờ tới khi đk đúng (tối đa giay); trả đúng / sai */
    public static boolean cho(double giay, DieuKien dk) {
        long het = System.currentTimeMillis() + (long) (giay * 1000);
        while (System.currentTimeMillis() < het) {
            try {
                if (dk.dung()) return true;
            } catch (Throwable e) {
                // chưa
            }
            ngu(0.05);
        }
        return false;
    }

    // ---------------- phản chiếu ----------------

    public static Field timTruong(Class<?> c, String ten) {
        for (Class<?> x = c; x != null; x = x.getSuperclass()) {
            try {
                Field f = x.getDeclaredField(ten);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                // lớp cha
            }
        }
        throw new RuntimeException("khong co truong " + ten + " o " + c.getName());
    }

    public static Object lay(Object o, String ten) {
        try {
            Class<?> c = o instanceof Class ? (Class<?>) o : o.getClass();
            return timTruong(c, ten).get(o instanceof Class ? null : o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void dat(Object o, String ten, Object v) {
        try {
            Class<?> c = o instanceof Class ? (Class<?>) o : o.getClass();
            timTruong(c, ten).set(o instanceof Class ? null : o, v);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> lop(String ten) {
        try {
            return Class.forName(ten);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /** gọi phương thức (kể cả private, lớp cha) theo tên + số tham số; o = Class -> tĩnh */
    public static Object goi(Object o, String ten, Object... a) {
        Class<?> c0 = o instanceof Class ? (Class<?>) o : o.getClass();
        for (Class<?> c = c0; c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (!m.getName().equals(ten) || m.getParameterTypes().length != a.length) continue;
                if (!hop(m.getParameterTypes(), a)) continue;
                try {
                    m.setAccessible(true);
                    return m.invoke(o instanceof Class ? null : o, a);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw new RuntimeException(e.getCause());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new RuntimeException("khong co ham " + ten + "/" + a.length + " o " + c0.getName());
    }

    static boolean hop(Class<?>[] t, Object[] a) {
        for (int i = 0; i < t.length; i++) {
            if (a[i] == null) continue;
            Class<?> k = t[i];
            if (k.isPrimitive()) {
                if (k == int.class && !(a[i] instanceof Integer)) return false;
                if (k == boolean.class && !(a[i] instanceof Boolean)) return false;
                if (k == byte.class && !(a[i] instanceof Byte)) return false;
                if (k == short.class && !(a[i] instanceof Short)) return false;
                if (k == long.class && !(a[i] instanceof Long)) return false;
            } else if (!k.isInstance(a[i])) {
                return false;
            }
        }
        return true;
    }

    /** gọi trên luồng sự kiện */
    public static Object goiT(final Object o, final String ten, final Object... a) {
        return tren(new Callable<Object>() {
            public Object call() {
                return goi(o, ten, a);
            }
        });
    }

    // ---------------- nhập giả ----------------

    /** dấu chạm (vòng tròn nhỏ) hiện ở chỗ bấm - giống quay màn điện thoại "hiện chạm" */
    static final List<long[]> CHAM = new ArrayList<long[]>();
    public static boolean hienCham = !Boolean.getBoolean("quay.khongCham");

    /** Chạm / bấm chuột ở toạ độ LOGIC (640x360). */
    public static void bam(int x, int y) {
        bam(x, y, 0.09);
    }

    public static void bam(int x, int y, double giu) {
        int k = NenPc.heSo;
        int rx = x * k + k / 2, ry = y * k + k / 2;
        synchronized (CHAM) {
            CHAM.add(new long[]{System.currentTimeMillis(), rx, ry});
        }
        NenPc.cham(0, rx, ry);
        ngu(giu);
        NenPc.cham(2, rx, ry);
    }

    /** danh sách vùng chạm lớp trên cùng của khung vừa vẽ: "ten@x,y,w,h" */
    public static String vung() {
        return tren(new Callable<String>() {
            public String call() {
                StringBuilder b = new StringBuilder();
                int n = game.pokemon.VungCham.dem();
                int top = game.pokemon.VungCham.lopCaoNhat();
                for (int i = 0; i < n; i++) {
                    int[] v = game.pokemon.VungCham.vungVe(i);
                    b.append(game.pokemon.VungCham.lopVung(i) == top ? "" : "~").append(game.pokemon.VungCham.ten(i))
                            .append('@').append(v[0]).append(',').append(v[1]).append(',').append(v[2]).append(',').append(v[3]).append("  ");
                }
                return b.toString();
            }
        });
    }

    /** toạ độ tâm vùng chạm (lớp trên cùng) có tên chứa / bằng ten; null = không có */
    public static int[] timVung(final String ten) {
        return tren(new Callable<int[]>() {
            public int[] call() {
                int n = game.pokemon.VungCham.dem();
                int top = game.pokemon.VungCham.lopCaoNhat();
                int[] gan = null;
                for (int i = 0; i < n; i++) {
                    if (game.pokemon.VungCham.lopVung(i) != top) continue;
                    String t = game.pokemon.VungCham.ten(i);
                    if (t == null) continue;
                    int[] v = game.pokemon.VungCham.vungVe(i);
                    int[] c = {v[0] + v[2] / 2, v[1] + v[3] / 2};
                    if (t.equals(ten)) return c;
                    if (gan == null && t.contains(ten)) gan = c;
                }
                return gan;
            }
        });
    }

    /** bấm vùng chạm tên ten (chờ tối đa giay cho nó hiện); false = không thấy */
    public static boolean bamVung(final String ten, double giay) {
        final int[][] c = {null};
        boolean ok = cho(giay, new DieuKien() {
            public boolean dung() {
                veKhungNgam();
                c[0] = timVung(ten);
                return c[0] != null;
            }
        });
        if (!ok) {
            System.out.println("[quay] KHONG THAY vung '" + ten + "': " + vung());
            return false;
        }
        bam(c[0][0], c[0][1]);
        return true;
    }

    public static boolean bamVung(String ten) {
        return bamVung(ten, 3);
    }

    /** vẽ một khung không ghi (để vùng chạm cập nhật khi không đang ghi) */
    public static void veKhungNgam() {
        if (!dangGhi) veKhung();
    }

    /** kéo (logic) từ a tới b trong giay */
    public static void keo(int x0, int y0, int x1, int y1, double giay) {
        int k = NenPc.heSo;
        NenPc.cham(0, x0 * k, y0 * k);
        int n = Math.max(2, (int) (giay * 30));
        for (int i = 1; i <= n; i++) {
            int x = x0 + (x1 - x0) * i / n, y = y0 + (y1 - y0) * i / n;
            NenPc.cham(1, x * k, y * k);
            ngu(giay / n);
        }
        NenPc.cham(2, x1 * k, y1 * k);
    }

    /** phím J2ME */
    public static void phim(int ma) {
        phim(ma, 0.08);
    }

    public static void phim(int ma, double giu) {
        NenPc.phim(0, ma);
        ngu(giu);
        NenPc.phim(2, ma);
    }

    // ---------------- vẽ ----------------

    private static BufferedImage dem;
    private static Method veM;
    /** lớp phủ của video (chữ đề) vẽ sau game; null = không */
    public static volatile LopPhuVideo phu;

    public interface LopPhuVideo {
        void ve(Graphics2D g, int w, int h, long ms);
    }

    /** chạy trên luồng sự kiện ngay trước mỗi khung vẽ (vd: bỏ thẻ phản hồi lệnh GM dàn cảnh) */
    public static volatile Runnable truocVe;
    /** thay cho Canvas.paint (vd: cảnh kết vẽ phông nền + logo của game) - null = vẽ game */
    public static volatile java.util.function.Consumer<Graphics> veThay;

    /** Vẽ một khung của client (trên luồng sự kiện) vào bộ đệm BGR W x H. */
    private static BufferedImage demThat;

    static synchronized BufferedImage veKhung() {
        if (dem == null) dem = new BufferedImage(W, H, BufferedImage.TYPE_3BYTE_BGR);
        final boolean phong = NenPc.thatW != W || NenPc.thatH != H;
        if (phong && (demThat == null || demThat.getWidth() != NenPc.thatW || demThat.getHeight() != NenPc.thatH)) {
            demThat = new BufferedImage(NenPc.thatW, NenPc.thatH, BufferedImage.TYPE_3BYTE_BGR);
        }
        final BufferedImage dich = phong ? demThat : dem;
        tren(new Runnable() {
            public void run() {
                Runnable tv = truocVe;
                if (tv != null) {
                    try {
                        tv.run();
                    } catch (Throwable e) {
                        // bỏ
                    }
                }
                Graphics2D g2 = dich.createGraphics();
                g2.setColor(java.awt.Color.BLACK);
                g2.fillRect(0, 0, dich.getWidth(), dich.getHeight());
                Graphics2D gg = (Graphics2D) g2.create();
                int k = NenPc.heSo;
                gg.scale(k, k);
                gg.clipRect(0, 0, NenPc.rong, NenPc.cao);
                Graphics g = new Graphics(gg, NenPc.rong, NenPc.cao, k);
                try {
                    if (veM == null) {
                        veM = javax.microedition.lcdui.Canvas.class.getDeclaredMethod("paint", Graphics.class);
                        veM.setAccessible(true);
                    }
                    java.util.function.Consumer<Graphics> vt = veThay;
                    if (vt != null) vt.accept(g);
                    else veM.invoke(game.render.Canvas.instance, g);
                } catch (Throwable e) {
                    // khung lỗi: giữ phần đã vẽ
                }
                gg.dispose();
                try {
                    htbb.pc.HieuUngHD.chotKhung();
                } catch (Throwable e) {
                    // bỏ
                }
                try {
                    htbb.pc.LopPhu.ve(g2, dich.getWidth(), dich.getHeight());
                } catch (Throwable e) {
                    // bỏ
                }
                NenPc.soKhung++;
                g2.dispose();
            }
        });
        Graphics2D g = dem.createGraphics();
        if (phong) {
            // khung không khớp hệ số nguyên (vd 2400x1080 = logic x3.75): trình bày như cửa sổ thật (NenPc.khungHien)
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setColor(java.awt.Color.BLACK);
            g.fillRect(0, 0, W, H);
            g.drawImage(demThat, NenPc.hienX, NenPc.hienY, NenPc.hienW, NenPc.hienH, null);
        }
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        long now = System.currentTimeMillis();
        if (hienCham) {
            synchronized (CHAM) {
                for (int i = CHAM.size() - 1; i >= 0; i--) {
                    long[] c = CHAM.get(i);
                    long t = now - c[0];
                    if (t > 450) {
                        CHAM.remove(i);
                        continue;
                    }
                    float p = t / 450f;
                    int r = (int) (22 + 34 * p);
                    int a = (int) (200 * (1 - p));
                    g.setColor(new java.awt.Color(255, 255, 255, a));
                    g.setStroke(new java.awt.BasicStroke(5f));
                    g.drawOval((int) c[1] - r, (int) c[2] - r, 2 * r, 2 * r);
                    g.setColor(new java.awt.Color(255, 255, 255, a / 2));
                    g.fillOval((int) c[1] - 14, (int) c[2] - 14, 28, 28);
                }
            }
        }
        LopPhuVideo p = phu;
        if (p != null) p.ve(g, W, H, now);
        g.dispose();
        return dem;
    }

    public static void anh(String ten) {
        try {
            BufferedImage c;
            synchronized (Quay.class) {
                BufferedImage b = dangGhi && dem != null ? dem : veKhung();
                c = new BufferedImage(b.getWidth(), b.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                c.getRaster().setRect(b.getRaster());
            }
            ImageIO.write(c, "png", new File(raThuMuc, ten + ".png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- ghi ----------------

    static volatile Process ff;
    static volatile OutputStream ffVao;
    static volatile boolean dangGhi;
    static Thread luongGhi, luongViet;
    static volatile long msVeTong;
    public static volatile int khungGhi, khungLap;
    public static volatile long msVeMax;

    /** Bắt đầu ghi một đoạn vào <ra>/doan/<ten>.mp4 (chất lượng gần không mất; ghép + mã hoá cuối ở bước sau). */
    public static void ghi(String ten) {
        dung();
        try {
            File d = new File(raThuMuc, "doan");
            d.mkdirs();
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-f", "rawvideo",
                    "-pix_fmt", "bgr24", "-s", W + "x" + H, "-r", "" + FPS, "-i", "-", "-c:v", "libx264", "-preset", "ultrafast",
                    "-crf", System.getProperty("quay.crf", "10"), "-pix_fmt", System.getProperty("quay.pix", "yuv420p"), new File(d, ten + ".mp4").getAbsolutePath());
            pb.redirectErrorStream(true);
            pb.redirectOutput(new File(d, ten + ".ffmpeg.log"));
            ff = pb.start();
            ffVao = new java.io.BufferedOutputStream(ff.getOutputStream(), 1 << 22);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        khungGhi = 0;
        khungLap = 0;
        doanTen = ten;
        new File(new File(raThuMuc, "doan"), ten + ".moc").delete();
        msVeMax = 0;
        dangGhi = true;
        // vẽ (luồng này) và ghi vào ffmpeg (luồng riêng) chạy song song qua hàng đợi nhỏ + bộ đệm dùng lại
        final java.util.concurrent.ArrayBlockingQueue<Object[]> hang = new java.util.concurrent.ArrayBlockingQueue<Object[]>(3);
        final java.util.concurrent.ArrayBlockingQueue<byte[]> kho = new java.util.concurrent.ArrayBlockingQueue<byte[]>(5);
        for (int i = 0; i < 5; i++) kho.add(new byte[W * H * 3]);
        luongViet = new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        Object[] o = hang.take();
                        if (o.length == 0) break;
                        byte[] px = (byte[]) o[0];
                        int lan = (Integer) o[1];
                        for (int i = 0; i < lan; i++) ffVao.write(px);
                        kho.put(px);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "quay-viet");
        luongViet.start();
        luongGhi = new Thread(new Runnable() {
            public void run() {
                long t0 = System.nanoTime();
                long buoc = 1000000000L / FPS;
                long n = 0;
                try {
                    while (dangGhi) {
                        long dich = t0 + n * buoc;
                        long cho = dich - System.nanoTime();
                        if (cho > 0) Thread.sleep(cho / 1000000L, (int) (cho % 1000000L));
                        byte[] px = kho.take();
                        long a = System.nanoTime();
                        synchronized (Quay.class) {
                            BufferedImage b = veKhung();
                            System.arraycopy(((DataBufferByte) b.getRaster().getDataBuffer()).getData(), 0, px, 0, px.length);
                        }
                        long ms = (System.nanoTime() - a) / 1000000L;
                        if (ms > msVeMax) msVeMax = ms;
                        msVeTong += ms;
                        int lan = 1;
                        n++;
                        // trễ quá một khung: lặp khung vừa vẽ cho đúng đồng hồ thật
                        while (System.nanoTime() > t0 + (n + 1) * buoc) {
                            lan++;
                            n++;
                        }
                        khungGhi += lan;
                        khungLap += lan - 1;
                        hang.put(new Object[]{px, lan});
                    }
                    hang.put(new Object[0]);
                } catch (InterruptedException e) {
                    // dừng
                }
            }
        }, "quay-ghi");
        msVeTong = 0;
        luongGhi.start();
        System.out.println("[quay] ghi " + ten);
    }

    static String doanTen;
    static long doanT0;

    /** ghi mốc thời gian (giây từ đầu đoạn đang ghi) vào doan/<đoạn>.moc - dùng khi dựng */
    public static void moc(String ten) {
        if (!dangGhi) return;
        double t = khungGhi / (double) FPS;
        System.out.println("[moc] " + doanTen + " " + ten + " " + t);
        try (java.io.FileWriter w = new java.io.FileWriter(new File(new File(raThuMuc, "doan"), doanTen + ".moc"), true)) {
            w.write(ten + " " + String.format(java.util.Locale.ROOT, "%.2f", t) + "\n");
        } catch (Exception e) {
            // bỏ
        }
    }

    public static void dung() {
        if (!dangGhi) return;
        dangGhi = false;
        try {
            luongGhi.join();
            luongViet.join();
            ffVao.close();
            ff.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("[quay] xong: " + khungGhi + " khung (" + (khungGhi / (float) FPS) + " s), lap " + khungLap + ", ve max " + msVeMax + " ms, tb " + (msVeTong / Math.max(1, khungGhi - khungLap)) + " ms");
    }

    // ---------------- khởi động client ----------------

    /** Khởi động client PC (headless) nối máy chủ mayChu (host:cong), dữ liệu người chơi ở duLieu. */
    public static void khoiDong(String mayChu, File duLieu) {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("htbb.duLieu", duLieu.getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/Linux");
        System.setProperty("microedition.locale", "vi-VN");
        System.setProperty("microedition.encoding", "UTF-8");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.CaiDat.hienFps = false;
        htbb.pc.CaiDat.doKhung = false;
        htbb.pc.CaiDat.dev = false;
        htbb.pc.CaiDat.hieuUngCao = true;
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.ChuHD.bat = true;
        htbb.pc.GiaoDienHD.bat = true;
        htbb.pc.LopPhu.nap();
        MIDlet.GHI_DE.put("HTBB-May-Chu", mayChu);
        MIDlet.GHI_DE.put("HTBB-Ban-Thu", "0");
        NenPc.tinhKichThuoc(W, H);
        tren(new Runnable() {
            public void run() {
                try {
                    game.render.screen.TField.isQwerty = true;
                    MIDlet m = (MIDlet) Class.forName("game.GameMidlet").newInstance();
                    MIDlet.batDau(m);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static String trangThai() {
        Object man = game.render.Canvas.currentScreen;
        return (man == null ? "null" : man.getClass().getSimpleName()) + " dlg="
                + (game.render.Canvas.currentDialog == null ? "-" : game.render.Canvas.currentDialog.getClass().getSimpleName())
                + " tai=" + game.pokemon.ManTai.dangVao;
    }
}
