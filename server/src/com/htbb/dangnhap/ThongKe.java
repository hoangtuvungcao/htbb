package com.htbb.dangnhap;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Bo dem cho dong [NHIP] cua login server: so yeu cau theo ket qua, tu choi, het han, do tre (tu luc doc xong khung
 * den luc ghi xong tra loi) theo bieu do bac. Moi chu ky {@link #tomTatVaXoa} doc va dat lai bo dem chu ky; tong tu
 * luc bat giu rieng.
 */
public final class ThongKe {

    /** Can tren cac bac do tre (ms); bac cuoi = vo cung. */
    static final long[] BAC_MS = {1, 2, 3, 5, 8, 12, 20, 30, 50, 80, 120, 200, 300, 500, 800, 1200, 2000, 3000, 5000,
            8000, 12000, 15000, 30000, Long.MAX_VALUE};

    private final AtomicLongArray bieuDo = new AtomicLongArray(BAC_MS.length);
    private final AtomicLong treMax = new AtomicLong();

    public final AtomicLong yeuCau = new AtomicLong();
    public final AtomicLong ok = new AtomicLong();
    public final AtomicLong sai = new AtomicLong();
    public final AtomicLong khoa = new AtomicLong();
    public final AtomicLong loi = new AtomicLong();
    /** Hang doi day / vuot gioi han IP -> tra -3 ngay. */
    public final AtomicLong tuChoi = new AtomicLong();
    /** Nam trong hang qua login.hanXuLyMs -> tra -3 khong truy van (game sap bo cho). */
    public final AtomicLong hetHan = new AtomicLong();
    public final AtomicLong khungHong = new AtomicLong();
    public final AtomicLong lenhLa = new AtomicLong();

    public final AtomicLong tongYeuCau = new AtomicLong();
    public final AtomicLong tongTuChoi = new AtomicLong();

    public void ghiKetQua(final int ma, final long treMs) {
        this.yeuCau.incrementAndGet();
        this.tongYeuCau.incrementAndGet();
        if (ma > 0) {
            this.ok.incrementAndGet();
        } else if (ma == XacThuc.SAI) {
            this.sai.incrementAndGet();
        } else if (ma == XacThuc.BI_KHOA || ma == XacThuc.CHUA_KICH_HOAT) {
            this.khoa.incrementAndGet();
        } else {
            this.loi.incrementAndGet();
        }
        this.ghiTre(treMs);
    }

    public void ghiTuChoi(final long treMs) {
        this.yeuCau.incrementAndGet();
        this.tongYeuCau.incrementAndGet();
        this.tuChoi.incrementAndGet();
        this.tongTuChoi.incrementAndGet();
        this.ghiTre(treMs);
    }

    public void ghiHetHan(final long treMs) {
        this.yeuCau.incrementAndGet();
        this.tongYeuCau.incrementAndGet();
        this.hetHan.incrementAndGet();
        this.ghiTre(treMs);
    }

    void ghiTre(final long ms) {
        final long v = Math.max(0L, ms);
        int i = 0;
        while (v > BAC_MS[i]) {
            i++;
        }
        this.bieuDo.incrementAndGet(i);
        long m;
        while (v > (m = this.treMax.get()) && !this.treMax.compareAndSet(m, v)) {
            // lap lai
        }
    }

    /** Phan vi tu bieu do (tra can tren cua bac chua phan vi; bac cuoi -> max thuc). */
    static long phanVi(final long[] d, final double q, final long max) {
        long n = 0;
        for (final long x : d) {
            n += x;
        }
        if (n == 0) {
            return 0;
        }
        final long can = (long) Math.ceil(q * n);
        long cong = 0;
        for (int i = 0; i < d.length; i++) {
            cong += d[i];
            if (cong >= can) {
                return Math.min(BAC_MS[i], max);
            }
        }
        return max;
    }

    /** Dong tom tat chu ky (va dat lai bo dem chu ky). */
    public String tomTatVaXoa(final String them) {
        final long[] d = new long[BAC_MS.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = this.bieuDo.getAndSet(i, 0L);
        }
        final long max = this.treMax.getAndSet(0L);
        final StringBuilder b = new StringBuilder(220);
        b.append("dang_nhap yc=").append(this.yeuCau.getAndSet(0L));
        b.append(" ok=").append(this.ok.getAndSet(0L));
        b.append(" sai=").append(this.sai.getAndSet(0L));
        b.append(" khoa=").append(this.khoa.getAndSet(0L));
        b.append(" loi=").append(this.loi.getAndSet(0L));
        b.append(" tuChoi=").append(this.tuChoi.getAndSet(0L));
        b.append(" hetHan=").append(this.hetHan.getAndSet(0L));
        final long kh = this.khungHong.getAndSet(0L);
        final long ll = this.lenhLa.getAndSet(0L);
        if (kh > 0 || ll > 0) {
            b.append(" khungHong=").append(kh).append(" lenhLa=").append(ll);
        }
        b.append(" tre p50=").append(phanVi(d, 0.50, max)).append("ms p99=").append(phanVi(d, 0.99, max))
                .append("ms max=").append(max).append("ms");
        if (them != null && !them.isEmpty()) {
            b.append(' ').append(them);
        }
        return b.toString();
    }
}
