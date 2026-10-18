package server.quantri;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Lay mau so lieu may chu cho bang dieu khien: MOT luong nen (do uu tien thap), 1 lan / giay,
 * khong dong CSDL, khong khoa luong game. Moi lan tao dung mot {@link Mau} nho (bat bien);
 * bieu do doc tu vong dem co dinh (180 diem x 10 giay = 30 phut) - khong cap phat theo thoi gian.
 *
 * Nguoi nghe (giao dien) duoc goi tren luong lay mau; giao dien tu chuyen sang EDT va chi cap
 * nhat khi cua so dang hien.
 */
public final class DuLieuTrucTiep {

    public static final int SO_DIEM = 180;
    public static final int GIAY_MOI_DIEM = 10;

    /** Mot anh chup so lieu. */
    public static final class Mau {
        public final long luc;
        public final int online, ketNoi, dinh;
        public final long chayMs;
        public final long heapDung, heapToiDa;
        public final int cpu, thread;
        public final double goiVaoGiay, goiRaGiay, catNgapPhut, loiPhut;
        public final long goiBoTong, catNgapTong, loiTong;
        public final int dbDangDung, dbTong, dbRanh;
        public int dbToiDa;
        public long chatChan, thaoTacChan;
        public final boolean baoTri, dungDangNhap, dangTat;

        Mau(final long luc, final int online, final int ketNoi, final int dinh, final long chayMs, final long heapDung,
                final long heapToiDa, final int cpu, final int thread, final double vao, final double ra, final double catPhut,
                final double loiPhut, final long goiBo, final long cat, final long loi, final int dbDung, final int dbTong,
                final int dbRanh, final boolean baoTri, final boolean dungDangNhap, final boolean dangTat) {
            this.luc = luc;
            this.online = online;
            this.ketNoi = ketNoi;
            this.dinh = dinh;
            this.chayMs = chayMs;
            this.heapDung = heapDung;
            this.heapToiDa = heapToiDa;
            this.cpu = cpu;
            this.thread = thread;
            this.goiVaoGiay = vao;
            this.goiRaGiay = ra;
            this.catNgapPhut = catPhut;
            this.loiPhut = loiPhut;
            this.goiBoTong = goiBo;
            this.catNgapTong = cat;
            this.loiTong = loi;
            this.dbDangDung = dbDung;
            this.dbTong = dbTong;
            this.dbRanh = dbRanh;
            this.baoTri = baoTri;
            this.dungDangNhap = dungDangNhap;
            this.dangTat = dangTat;
        }
    }

    public final BieuDo.VongDem online = new BieuDo.VongDem(SO_DIEM);
    public final BieuDo.VongDem heapMb = new BieuDo.VongDem(SO_DIEM);
    public final BieuDo.VongDem cpu = new BieuDo.VongDem(SO_DIEM);
    public final BieuDo.VongDem goiVao = new BieuDo.VongDem(SO_DIEM);
    public final BieuDo.VongDem goiRa = new BieuDo.VongDem(SO_DIEM);
    public final BieuDo.VongDem loi = new BieuDo.VongDem(SO_DIEM);
    public final BieuDo.VongDem thread = new BieuDo.VongDem(SO_DIEM);

    private final CopyOnWriteArrayList<Consumer<Mau>> nghe = new CopyOnWriteArrayList<Consumer<Mau>>();
    private ScheduledExecutorService luong;
    private volatile Mau moiNhat;
    private int dinh;

    // trang thai tinh toc do
    private long vao0 = -1, ra0, bo0, cat0, loi0, loiNhat0, luc0;
    private final double[] loiCuaSo = new double[60];
    private final double[] catCuaSo = new double[60];
    private int oCuaSo;
    // gom diem bieu do 10 giay
    private int demGom;
    private double gOnline, gHeap, gCpu, gVao, gRa, gLoi, gThread;

    public synchronized void batDau() {
        if (luong != null) {
            return;
        }
        luong = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "PANEL QUAN TRI - so lieu");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
        luong.scheduleAtFixedRate(() -> {
            try {
                final Mau m = layMau();
                moiNhat = m;
                for (final Consumer<Mau> c : nghe) {
                    c.accept(m);
                }
            } catch (final Throwable ignored) {
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public synchronized void dung() {
        if (luong != null) {
            luong.shutdownNow();
            luong = null;
        }
    }

    public void nghe(final Consumer<Mau> c) {
        nghe.add(c);
    }

    public Mau moiNhat() {
        return moiNhat;
    }

    /** Lay mot mau (goi tu luong nen; cong khai cho kiem thu). */
    public Mau layMau() {
        final long bay = System.currentTimeMillis();
        final int on = demOnline();
        final int kn = demKetNoi();
        dinh = Math.max(dinh, on);
        final MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        final int c = cpuTienTrinh();
        final int th = ManagementFactory.getThreadMXBean().getThreadCount();
        final long vao = server.ChiSoMayChu.GOI_VAO.get();
        final long ra = server.ChiSoMayChu.GOI_RA.sum();
        final long bo = server.ChiSoMayChu.GOI_BO.get();
        final long cat = server.ChiSoMayChu.CAT_NGAP.get();
        final long loiXL = server.ChiSoMayChu.LOI_XU_LY.get();
        long loiNhat = 0;
        try {
            loiNhat = com.htbb.log.Nhat.DONG_LOI.get();
        } catch (final Throwable ignored) {
        }
        double vaoGiay = 0, raGiay = 0;
        if (vao0 >= 0 && bay > luc0) {
            final double s = (bay - luc0) / 1000.0;
            vaoGiay = (vao - vao0) / s;
            raGiay = (ra - ra0) / s;
            loiCuaSo[oCuaSo] = (loiXL - loi0) + (loiNhat - loiNhat0);
            catCuaSo[oCuaSo] = (bo - bo0) + (cat - cat0);
            oCuaSo = (oCuaSo + 1) % loiCuaSo.length;
        }
        vao0 = vao;
        ra0 = ra;
        bo0 = bo;
        cat0 = cat;
        loi0 = loiXL;
        loiNhat0 = loiNhat;
        luc0 = bay;
        double loiPhut = 0, catPhut = 0;
        for (int i = 0; i < loiCuaSo.length; i++) {
            loiPhut += loiCuaSo[i];
            catPhut += catCuaSo[i];
        }
        int dbDung = -1, dbTong = -1, dbRanh = -1;
        try {
            final data.ConnPool p = data.Database.connPool;
            if (p != null) {
                dbTong = p.countConnection;
                dbRanh = p.pool == null ? 0 : p.pool.size();
                dbDung = Math.max(0, dbTong - dbRanh);
            }
        } catch (final Throwable ignored) {
        }
        boolean bt = false, dn = false, tat = false;
        try {
            bt = server.ChiSoMayChu.dangBaoTri();
            dn = real.cmd.LoginHandler.stopLogin;
            tat = real.AdminHandler.isStopServer;
        } catch (final Throwable ignored) {
        }
        final Mau m = new Mau(bay, on, kn, dinh, ManagementFactory.getRuntimeMXBean().getUptime(), heap.getUsed(),
                heap.getMax() > 0 ? heap.getMax() : heap.getCommitted(), c, th, vaoGiay, raGiay, catPhut, loiPhut, bo, cat,
                loiXL, dbDung, dbTong, dbRanh, bt, dn, tat);
        m.dbToiDa = toiDaPool();
        m.chatChan = server.ChiSoMayChu.CHAT_CHAN.get();
        m.thaoTacChan = server.ChiSoMayChu.THAO_TAC_CHAN.get();
        gom(m);
        return m;
    }

    private void gom(final Mau m) {
        gOnline = Math.max(gOnline, m.online);
        gHeap = Math.max(gHeap, m.heapDung >> 20);
        gCpu += Math.max(0, m.cpu);
        gVao += m.goiVaoGiay;
        gRa += m.goiRaGiay;
        gLoi = Math.max(gLoi, m.loiPhut);
        gThread = Math.max(gThread, m.thread);
        demGom++;
        if (demGom >= GIAY_MOI_DIEM || online.soDiem() == 0) {
            online.them(gOnline);
            heapMb.them(gHeap);
            cpu.them(gCpu / demGom);
            goiVao.them(gVao / demGom);
            goiRa.them(gRa / demGom);
            loi.them(gLoi);
            thread.them(gThread);
            demGom = 0;
            gOnline = gHeap = gCpu = gVao = gRa = gLoi = gThread = 0;
        }
    }

    private static java.lang.reflect.Field truongMax;

    /** ConnPool.maxConn (package-private trong lib) - doc qua phan chieu, -1 neu khong doc duoc. */
    static int toiDaPool() {
        try {
            if (truongMax == null) {
                final java.lang.reflect.Field f = data.ConnPool.class.getDeclaredField("maxConn");
                f.setAccessible(true);
                truongMax = f;
            }
            return data.Database.connPool == null ? -1 : truongMax.getInt(data.Database.connPool);
        } catch (final Throwable t) {
            return -1;
        }
    }

    static int demOnline() {
        try {
            return data.GhiSoOnline.demNhanVat();
        } catch (final Throwable t) {
            return 0;
        }
    }

    static int demKetNoi() {
        try {
            return data.GhiSoOnline.demKetNoi();
        } catch (final Throwable t) {
            return 0;
        }
    }

    static int cpuTienTrinh() {
        try {
            final java.lang.management.OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            if (os instanceof com.sun.management.OperatingSystemMXBean) {
                final double v = ((com.sun.management.OperatingSystemMXBean) os).getProcessCpuLoad();
                return v < 0 ? -1 : (int) Math.round(v * 100.0);
            }
        } catch (final Throwable ignored) {
        }
        return -1;
    }
}
