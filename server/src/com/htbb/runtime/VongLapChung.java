package com.htbb.runtime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Vong lap DUNG CHUNG cho moi ban do / khu (Region) - thay "mot thread cho moi vong lap".
 *
 * Truoc day: moi Region tao 4 thread (tick phe 0, tick phe 1, 2 thread xu ly goi tin), moi Map
 * khong chia khu tao 4 thread (run/update, updateSand, 2 thread goi tin) -> ~2.970 thread khi
 * server RONG, ~12.000 lan danh thuc/giay (wait(200..300ms) + notify), 0,5-0,7 loi CPU nen.
 *
 * Nay:
 *  - {@link Nhip}: mot vong "tick" (than ham update cu) chay tren pool hen gio chung
 *    "HTBB-NHIP-n" (so luong = so loi x 2, -Dhtbb.nhip.luong). Chu ky giu NGUYEN nhu vong cu:
 *    chay than, xong moi hen lan sau sau {@code chuKy} ms (vong cu: update roi wait(DELAY) va chi
 *    update khi da qua DELAY ke tu lan truoc = fixed delay). Khi RealController.savingChar thi
 *    hoan 100 ms (vong cu ngu 100 ms). Tick co the "ranh" (khu khong co nguoi choi that): chu ky
 *    ranh {@link #CHU_KY_RANH_MS} va {@link Nhip#danhThuc()} (nguoi vao khu) chay lai NGAY.
 *  - {@link Lan}: vong xu ly goi tin noi tiep (khong bao gio chay song song voi chinh no) tren pool
 *    "HTBB-GOITIN-n". Chay khi co goi moi ({@link Lan#kich()}) - khong con ngu/thuc dinh ky.
 *
 * Moi vong deu bat Throwable: mot loi khong giet duoc vong (vong cu cung catch Exception).
 */
public final class VongLapChung {

    /** Chu ky tick cua khu khong co nguoi choi that (ms). Vong cu: 300 ms bat ke co ai hay khong. */
    public static final long CHU_KY_RANH_MS = Long.getLong("htbb.nhip.ranhMs", 2000L);

    public static final int SO_LUONG_NHIP = Math.max(2, Integer.getInteger("htbb.nhip.luong",
            Runtime.getRuntime().availableProcessors() * 2));
    /** Goi tin co the cham I/O (CSDL) hoac ngu (chat bang ngu 5 s) -> rong hon pool tick. */
    public static final int SO_LUONG_GOI_TIN = Math.max(4, Integer.getInteger("htbb.goitin.luong",
            Math.max(16, Runtime.getRuntime().availableProcessors() * 4)));

    private static final ScheduledThreadPoolExecutor NHIP;
    private static final ExecutorService GOI_TIN;

    static {
        NHIP = new ScheduledThreadPoolExecutor(SO_LUONG_NHIP, xuong("HTBB-NHIP-"));
        NHIP.setRemoveOnCancelPolicy(true);
        NHIP.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        GOI_TIN = new ThreadPoolExecutor(SO_LUONG_GOI_TIN, SO_LUONG_GOI_TIN, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), xuong("HTBB-GOITIN-"));
    }

    private VongLapChung() {
    }

    private static ThreadFactory xuong(final String ten) {
        final AtomicInteger dem = new AtomicInteger();
        return new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                final Thread t = new Thread(r, ten + dem.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        };
    }

    /** Dieu kien dung han (AdminHandler.isStopServer) - tiem vao de lop nay khong phu thuoc real.*. */
    public interface DieuKien {
        boolean dung();
    }

    /** Thong tin tu than vong: co nguoi choi that khong (de chon chu ky ranh). */
    public interface CoNguoi {
        boolean coNguoiThat();
    }

    private static volatile DieuKien dungServer = new DieuKien() {
        @Override
        public boolean dung() {
            return false;
        }
    };
    private static volatile DieuKien dangLuu = dungServer;

    /** Goi mot lan luc khoi dong (Region/Map lam qua RealController/AdminHandler). */
    public static void datDieuKien(final DieuKien dung, final DieuKien luu) {
        dungServer = dung;
        dangLuu = luu;
    }

    // ---------------------------------------------------------------- thong ke
    /** Bien do: [0]=<1ms [1]=<2 [2]=<5 [3]=<10 [4]=<20 [5]=<50 [6]=<100 [7]=<300 [8]=>=300. */
    private static final long[] MOC_MS = {1, 2, 5, 10, 20, 50, 100, 300};
    static final AtomicLongArray BIEU_DO_CHAY = new AtomicLongArray(MOC_MS.length + 1);
    static final AtomicLongArray BIEU_DO_TRE = new AtomicLongArray(MOC_MS.length + 1);
    static final AtomicLong SO_TICK = new AtomicLong();
    static final AtomicLong TICK_LAU_NHAT_US = new AtomicLong();
    static volatile String tenTickLauNhat = "";
    static final AtomicLong SO_GOI_LAN = new AtomicLong();
    public static final AtomicInteger SO_NHIP = new AtomicInteger();
    public static final AtomicInteger SO_NHIP_RANH = new AtomicInteger();
    public static final AtomicInteger SO_LAN = new AtomicInteger();

    private static int o(final long ms) {
        for (int i = 0; i < MOC_MS.length; i++) {
            if (ms < MOC_MS[i]) {
                return i;
            }
        }
        return MOC_MS.length;
    }

    /** Ghi mot tick: thoi gian chay va do tre (bat dau muon bao nhieu so voi han). Cong khai de ban do do. */
    public static void ghiTick(final String ten, final long chayNs, final long treMs) {
        SO_TICK.incrementAndGet();
        BIEU_DO_CHAY.incrementAndGet(o(chayNs / 1000000L));
        BIEU_DO_TRE.incrementAndGet(o(Math.max(0L, treMs)));
        final long us = chayNs / 1000L;
        long cu;
        while (us > (cu = TICK_LAU_NHAT_US.get())) {
            if (TICK_LAU_NHAT_US.compareAndSet(cu, us)) {
                tenTickLauNhat = ten;
                break;
            }
        }
    }

    /** Phan vi tu bieu do (tra moc tren cua o chua phan vi, ms). */
    private static String phanVi(final long[] bd, final double p) {
        long tong = 0;
        for (long v : bd) {
            tong += v;
        }
        if (tong == 0) {
            return "-";
        }
        final long can = (long) Math.ceil(tong * p);
        long cong = 0;
        for (int i = 0; i < bd.length; i++) {
            cong += bd[i];
            if (cong >= can) {
                return i < MOC_MS.length ? "<" + MOC_MS[i] : ">=300";
            }
        }
        return ">=300";
    }

    private static long[] layVaXoa(final AtomicLongArray a) {
        final long[] r = new long[a.length()];
        for (int i = 0; i < r.length; i++) {
            r[i] = a.getAndSet(i, 0L);
        }
        return r;
    }

    /** Tom tat tu lan goi truoc (va dat lai bo dem). Dung cho dong log [NHIP]. */
    public static String tomTatVaXoa(final long giay) {
        final long[] chay = layVaXoa(BIEU_DO_CHAY);
        final long[] tre = layVaXoa(BIEU_DO_TRE);
        final long n = SO_TICK.getAndSet(0L);
        final long lau = TICK_LAU_NHAT_US.getAndSet(0L);
        final String tenLau = tenTickLauNhat;
        final long goi = SO_GOI_LAN.getAndSet(0L);
        final StringBuilder sb = new StringBuilder(200);
        sb.append("tick=").append(giay > 0 ? n / giay : n).append("/s");
        sb.append(" chay p50=").append(phanVi(chay, 0.50)).append("ms p99=").append(phanVi(chay, 0.99))
                .append("ms max=").append(lau / 1000L).append('.').append((lau / 100L) % 10).append("ms(").append(tenLau).append(')');
        sb.append(" tre p50=").append(phanVi(tre, 0.50)).append("ms p99=").append(phanVi(tre, 0.99)).append("ms");
        sb.append(" nhip=").append(SO_NHIP.get()).append(" ranh=").append(SO_NHIP_RANH.get());
        sb.append(" lanGoiTin=").append(SO_LAN.get()).append(" chayLan=").append(goi);
        return sb.toString();
    }

    private static volatile boolean daBatLog = false;

    /** Moi {@code giay} giay in mot dong "[NHIP] ..." ra stdout (log game). Goi nhieu lan khong sao. */
    public static synchronized void batLogDinhKy(final long giay) {
        if (daBatLog || giay <= 0) {
            return;
        }
        daBatLog = true;
        NHIP.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("[NHIP] " + tomTatVaXoa(giay));
                } catch (Throwable ignored) {
                }
            }
        }, giay, giay, TimeUnit.SECONDS);
    }

    // ---------------------------------------------------------------- Nhip (tick)

    /** Mot vong tick tren pool hen gio chung. Khong bao gio chay song song voi chinh no. */
    public static final class Nhip implements Runnable {
        private final String ten;
        private final Runnable than;
        private final CoNguoi coNguoi;
        private volatile long chuKy;
        private volatile boolean dung = false;
        private volatile boolean ranh = false;
        private ScheduledFuture<?> sau;
        private long han;

        /**
         * @param chuKy   chu ky khi co nguoi (ms) - Map.DELAY_UPDATE_MAP
         * @param coNguoi null = luon chay theo chuKy (ban do co hen gio toan cuc)
         */
        public Nhip(final String ten, final long chuKy, final Runnable than, final CoNguoi coNguoi) {
            this.ten = ten;
            this.chuKy = chuKy;
            this.than = than;
            this.coNguoi = coNguoi;
        }

        public Nhip batDau(final long treDau) {
            SO_NHIP.incrementAndGet();
            synchronized (this) {
                this.han = System.currentTimeMillis() + treDau;
                this.sau = NHIP.schedule(this, treDau, TimeUnit.MILLISECONDS);
            }
            return this;
        }

        public void datChuKy(final long ms) {
            this.chuKy = ms;
        }

        public boolean dangRanh() {
            return this.ranh;
        }

        /** Co nguoi choi that vao: neu dang ngu chu ky ranh thi chay ngay. */
        public void danhThuc() {
            if (!this.ranh || this.dung) {
                return;
            }
            synchronized (this) {
                final ScheduledFuture<?> f = this.sau;
                if (f != null && f.getDelay(TimeUnit.MILLISECONDS) > 0 && f.cancel(false)) {
                    this.han = System.currentTimeMillis();
                    this.sau = NHIP.schedule(this, 0L, TimeUnit.MILLISECONDS);
                }
            }
        }

        public void dung() {
            if (this.dung) {
                return;
            }
            this.dung = true;
            SO_NHIP.decrementAndGet();
            if (this.ranh) {
                SO_NHIP_RANH.decrementAndGet();
            }
            synchronized (this) {
                if (this.sau != null) {
                    this.sau.cancel(false);
                }
            }
        }

        @Override
        public void run() {
            if (this.dung) {
                return;
            }
            if (dungServer.dung()) {
                this.dung();
                return;
            }
            long tre = this.chuKy;
            try {
                if (dangLuu.dung()) {
                    tre = 100L; // vong cu: while (savingChar) sleep(100)
                } else {
                    final long batDau = System.nanoTime();
                    final long treMs = System.currentTimeMillis() - this.han;
                    this.than.run();
                    ghiTick(this.ten, System.nanoTime() - batDau, treMs);
                    boolean ranhMoi = false;
                    if (this.coNguoi != null) {
                        try {
                            ranhMoi = !this.coNguoi.coNguoiThat();
                        } catch (Throwable t) {
                            ranhMoi = false;
                        }
                    }
                    if (ranhMoi != this.ranh) {
                        this.ranh = ranhMoi;
                        if (ranhMoi) {
                            SO_NHIP_RANH.incrementAndGet();
                        } else {
                            SO_NHIP_RANH.decrementAndGet();
                        }
                    }
                    if (ranhMoi) {
                        tre = Math.max(this.chuKy, CHU_KY_RANH_MS);
                    }
                }
            } catch (Throwable t) {
                // than ham da tu bao loi theo kieu cua no; o day chi giu vong song
                t.printStackTrace();
            }
            if (this.dung) {
                return;
            }
            synchronized (this) {
                if (!this.dung) {
                    this.han = System.currentTimeMillis() + tre;
                    try {
                        this.sau = NHIP.schedule(this, tre, TimeUnit.MILLISECONDS);
                    } catch (Throwable t) {
                        // pool da tat (server dang tat)
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------- Lan (goi tin)

    /**
     * Vong xu ly noi tiep: {@link #kich()} = "co viec" -> than chay tren pool goi tin, KHONG BAO GIO
     * hai lan cung luc; kich trong luc dang chay -> chay them mot luot ngay sau. Than phai tu don
     * het hang doi cua no (va tu gioi han so viec moi luot).
     */
    public static final class Lan implements Runnable {
        private static final int RANH = 0, CHO = 1, CHAY_LAI = 2;
        private final AtomicInteger trangThai = new AtomicInteger(RANH);
        private final Runnable than;
        private volatile boolean dung = false;
        private ScheduledFuture<?> dinhKy;

        public Lan(final Runnable than) {
            this.than = than;
            SO_LAN.incrementAndGet();
        }

        /** Them kich dinh ky (vd map 201 cap nhat NPC nhan the moi Map.timeDelay ms). */
        public Lan kichDinhKy(final long ms) {
            this.dinhKy = NHIP.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    Lan.this.kich();
                }
            }, ms, ms, TimeUnit.MILLISECONDS);
            return this;
        }

        public void kich() {
            if (this.dung) {
                return;
            }
            for (;;) {
                final int s = this.trangThai.get();
                if (s == RANH) {
                    if (this.trangThai.compareAndSet(RANH, CHO)) {
                        try {
                            GOI_TIN.execute(this);
                        } catch (Throwable t) {
                            this.trangThai.set(RANH);
                        }
                        return;
                    }
                } else if (s == CHO) {
                    if (this.trangThai.compareAndSet(CHO, CHAY_LAI)) {
                        return;
                    }
                } else {
                    return;
                }
            }
        }

        public void dung() {
            if (!this.dung) {
                this.dung = true;
                SO_LAN.decrementAndGet();
                if (this.dinhKy != null) {
                    this.dinhKy.cancel(false);
                }
            }
        }

        @Override
        public void run() {
            SO_GOI_LAN.incrementAndGet();
            try {
                if (!this.dung && !dungServer.dung()) {
                    this.than.run();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            // CHO -> RANH: xong. CHAY_LAI -> co kich trong luc chay: dat lai CHO va chay them luot nua
            // (qua pool, khong lap tai cho de khong giu luong lau).
            if (!this.trangThai.compareAndSet(CHO, RANH)) {
                this.trangThai.set(CHO);
                try {
                    GOI_TIN.execute(this);
                } catch (Throwable t) {
                    this.trangThai.set(RANH);
                }
            }
        }
    }

    /** Viec dinh ky nhe (vai micro giay) tren pool tick chung - thay "new Thread + while + sleep". */
    public static ScheduledFuture<?> henGio(final Runnable viec, final long chuKyMs) {
        return NHIP.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    viec.run();
                } catch (Throwable ignored) {
                }
            }
        }, chuKyMs, chuKyMs, TimeUnit.MILLISECONDS);
    }

    /** Cho kiem thu: so viec dang cho trong pool tick. */
    public static int soViecChoNhip() {
        return NHIP.getQueue().size();
    }
}
