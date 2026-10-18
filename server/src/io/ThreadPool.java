package io;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ghi de io.ThreadPool trong lib/htbb-core.jar (cung ten lop, cung truong cong khai: instant, executor,
 * workList, time - cac lop trong jar va src van goi {@code ThreadPool.instant.workList.add(...)}).
 *
 * Ban jar: Executors.newFixedThreadPool(350) khong ten. Viec trong hang doi gan nhu toan la LUU CSDL
 * (saveCharAuto/saveChar). Do 18/10/2026 (docs/pokemon/TOI_UU_HIEU_NANG.md muc 4): khi nhieu nhan vat
 * den han tu luu cung luc, toi 350 luong cung lay ket noi -> pool CSDL 100 ket noi CAN SACH moi ~60 s,
 * ConnPool.getConFromPool goi initPool() mo them 100 ket noi moi (roi addConToPool dong bot phan
 * thua) - mot con bao tao/dong ket noi lien tuc. Nay: pool co tran, co ten "HTBB-VIEC-n" (mac dinh 32,
 * -Dhtbb.viec.luong=N), luon NHO HON pool CSDL nen khong vet can duoc. Vong chuyen viec giu nguyen
 * (lay het workList roi ngu 100 ms), giu ca dong "Thread Snap" moi 200 viec nhu cu.
 */
public class ThreadPool {

    public static final int SO_LUONG = Math.max(4, Integer.getInteger("htbb.viec.luong", 32));

    public static ThreadPool instant = new ThreadPool();

    public ExecutorService executor;

    public Vector<Runnable> workList;

    public long time;

    public ThreadPool() {
        final AtomicInteger dem = new AtomicInteger();
        this.executor = new ThreadPoolExecutor(SO_LUONG, SO_LUONG, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(final Runnable r) {
                        final Thread t = new Thread(r, "HTBB-VIEC-" + dem.incrementAndGet());
                        t.setDaemon(false); // nhu Executors.defaultThreadFactory cu: luu do dang khong bi cat khi tat
                        return t;
                    }
                });
        this.workList = new Vector<Runnable>();
        this.time = System.currentTimeMillis();
        final Thread chuyen = new Thread(new Runnable() {
            @Override
            public void run() {
                ThreadPool.this.chuyenViec();
            }
        }, "HTBB-VIEC-CHUYEN");
        chuyen.start();
    }

    private void chuyenViec() {
        int i = 0;
        while (true) {
            try {
                while (this.workList.size() > 0) {
                    if (++i % 200 == 0) {
                        System.out.println("===>Thread Snap===Works Remain=" + this.workList.size() + "===200 works in "
                                + (System.currentTimeMillis() - this.time) + " ms");
                        this.time = System.currentTimeMillis();
                        if (i > 1000) {
                            i = 0;
                        }
                        Thread.sleep(100L);
                    }
                    try {
                        this.executor.execute((Runnable) this.workList.remove(0));
                    } catch (Exception ignored) {
                    }
                }
                try {
                    Thread.sleep(100L);
                } catch (Exception ignored) {
                }
            } catch (Exception ignored) {
            }
        }
    }
}
