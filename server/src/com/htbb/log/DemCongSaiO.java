package com.htbb.log;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Xin qua cong khi may chu thay nhan vat KHONG dung o cong (MapXuLyLenh.doChangeMap, nhanh "map 5" / "map 6").
 *
 * Truoc 25/10/2026 may chu KICK ngay lan dau. Bot hoi quy: client vua bi keo ve (lenh 4 tra vi tri, tre mang) van con
 * dung o cong tren man hinh -> xin qua cong -> bi ngat. Nay: tu choi + nap lai ban do o vi tri may chu; chi kick khi lap
 * lai ro rang (qua {@code nguong} lan trong {@code cuaSoMs}, cua so TRUOT). Log [CONG_SAI_O] co gioi han: lan dau cua moi
 * dot, roi toi da 1 dong / {@code logCachMs} / nhan vat, va luon ghi luc kick.
 *
 * Thoi gian truyen tu ngoai vao (kiem thu dieu khien dong ho). Thread-safe. So khoa chan tren {@code soKhoaToiDa}.
 */
public final class DemCongSaiO {

    /** 10 lan trong 30 giay van chi tu choi; lan thu 11 trong 30 giay -> kick. */
    public static final int NGUONG = 10;
    public static final long CUA_SO_MS = 30000L;
    public static final long LOG_CACH_MS = 10000L;

    public static final DemCongSaiO CHUNG = new DemCongSaiO(NGUONG, CUA_SO_MS, LOG_CACH_MS, 4096);

    /** Ket qua mot lan ghi nhan. */
    public static final class KetQua {
        /** So lan trong cua so (ke ca lan nay). */
        public final int lan;
        public final boolean kick;
        /** Co nen ghi dong log [CONG_SAI_O] cho lan nay khong (gioi han tan suat). */
        public final boolean ghiLog;

        KetQua(final int lan, final boolean kick, final boolean ghiLog) {
            this.lan = lan;
            this.kick = kick;
            this.ghiLog = ghiLog;
        }
    }

    private static final class Dem {
        final ArrayDeque<Long> moc = new ArrayDeque<Long>();
        long logLuc = Long.MIN_VALUE / 2;
    }

    private final int nguong;
    private final long cuaSoMs;
    private final long logCachMs;
    private final int soKhoaToiDa;
    private final LinkedHashMap<String, Dem> bang = new LinkedHashMap<String, Dem>(64, 0.75f, true);

    public DemCongSaiO(final int nguong, final long cuaSoMs, final long logCachMs, final int soKhoaToiDa) {
        this.nguong = Math.max(1, nguong);
        this.cuaSoMs = Math.max(1L, cuaSoMs);
        this.logCachMs = Math.max(0L, logCachMs);
        this.soKhoaToiDa = Math.max(16, soKhoaToiDa);
    }

    /** Ghi nhan mot lan xin qua cong sai o cua {@code ten} (ten nhan vat). */
    public synchronized KetQua ghiNhan(final String ten, final long bayGio) {
        final String k = ten == null ? "" : ten;
        Dem d = this.bang.get(k);
        if (d == null) {
            if (this.bang.size() >= this.soKhoaToiDa) {
                final Iterator<Map.Entry<String, Dem>> it = this.bang.entrySet().iterator();
                it.next();
                it.remove();
            }
            d = new Dem();
            this.bang.put(k, d);
        }
        while (!d.moc.isEmpty() && bayGio - d.moc.peekFirst().longValue() >= this.cuaSoMs) {
            d.moc.pollFirst();
        }
        final boolean dauDot = d.moc.isEmpty();
        d.moc.addLast(Long.valueOf(bayGio));
        final int lan = d.moc.size();
        final boolean kick = lan > this.nguong;
        boolean log = kick || dauDot || bayGio - d.logLuc >= this.logCachMs;
        if (log) {
            d.logLuc = bayGio;
        }
        if (kick) {
            this.bang.remove(k); // dang nhap lai bat dau lai tu 0
        }
        return new KetQua(lan, kick, log);
    }

    /** Xoa bo dem cua mot nhan vat (vd qua cong dung). */
    public synchronized void xoa(final String ten) {
        this.bang.remove(ten == null ? "" : ten);
    }

    public synchronized int soKhoa() {
        return this.bang.size();
    }
}
