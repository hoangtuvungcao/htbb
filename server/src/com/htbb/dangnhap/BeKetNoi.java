package com.htbb.dangnhap;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Be ket noi CSDL nho cho login server: toi da {@code toiDa} ket noi dang muon cung luc, muon phai cho qua
 * {@code choMuonMs} thi nem {@link SQLTimeoutException} (khong treo vo han). Ket noi ranh lau hon
 * {@code kiemSauRanhMs} duoc kiem {@code isValid} truoc khi dua ra (MariaDB wait_timeout cat ket noi dem khuya),
 * ket noi gia hon {@code tuoiToiDaMs} bi dong va mo moi.
 *
 * Khac ban cu (server.ConnPool trong CheckLoginSocket.jar): ban cu nem "Limited Connection" ngay khi du 8 ket noi,
 * CUOP ket noi da muon qua 30 s, va khong bao gio kiem ket noi da chet truoc khi dung.
 */
public final class BeKetNoi implements AutoCloseable {

    /** Mo ket noi moi. */
    public interface NhaMay {
        Connection mo() throws SQLException;
    }

    /** Mot ket noi dang muon. Tra bang {@link BeKetNoi#tra(Muon, boolean)}. */
    public static final class Muon {
        public final Connection ketNoi;
        final long taoLuc;
        long traLuc;

        Muon(final Connection c, final long taoLuc) {
            this.ketNoi = c;
            this.taoLuc = taoLuc;
            this.traLuc = taoLuc;
        }
    }

    private final NhaMay nhaMay;
    private final int toiDa;
    private final long choMuonMs;
    private final long kiemSauRanhMs;
    private final long tuoiToiDaMs;
    private final Semaphore cho;
    private final ConcurrentLinkedDeque<Muon> ranh = new ConcurrentLinkedDeque<>();
    private final AtomicInteger dangMo = new AtomicInteger();
    private volatile boolean dong;

    public final AtomicLong soMoMoi = new AtomicLong();
    public final AtomicLong soBoHong = new AtomicLong();
    public final AtomicLong soHetCho = new AtomicLong();

    public BeKetNoi(final NhaMay nhaMay, final int toiDa, final long choMuonMs, final long kiemSauRanhMs,
            final long tuoiToiDaMs) {
        if (toiDa < 1) {
            throw new IllegalArgumentException("toiDa < 1");
        }
        this.nhaMay = nhaMay;
        this.toiDa = toiDa;
        this.choMuonMs = choMuonMs;
        this.kiemSauRanhMs = kiemSauRanhMs;
        this.tuoiToiDaMs = tuoiToiDaMs;
        this.cho = new Semaphore(toiDa, true);
    }

    public Muon muon() throws SQLException {
        return muon(this.choMuonMs);
    }

    /** Muon, cho toi da min(choMuonMs, choMs) ms. */
    public Muon muon(final long choMs) throws SQLException {
        final long cho = Math.max(0L, Math.min(this.choMuonMs, choMs));
        if (this.dong) {
            throw new SQLException("be ket noi da dong");
        }
        boolean duoc;
        try {
            duoc = this.cho.tryAcquire(cho, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("bi ngat khi cho ket noi");
        }
        if (!duoc) {
            this.soHetCho.incrementAndGet();
            throw new SQLTimeoutException("het " + cho + " ms cho ket noi CSDL (" + this.toiDa + " dang dung)");
        }
        try {
            final long bay = System.currentTimeMillis();
            Muon m;
            while ((m = this.ranh.pollFirst()) != null) {
                if (bay - m.taoLuc > this.tuoiToiDaMs) {
                    this.huy(m);
                    continue;
                }
                if (bay - m.traLuc >= this.kiemSauRanhMs && !this.conSong(m.ketNoi)) {
                    this.soBoHong.incrementAndGet();
                    this.huy(m);
                    continue;
                }
                return m;
            }
            final Connection c = this.nhaMay.mo();
            this.dangMo.incrementAndGet();
            this.soMoMoi.incrementAndGet();
            return new Muon(c, bay);
        } catch (final SQLException | RuntimeException e) {
            this.cho.release();
            throw e;
        }
    }

    /** Tra ket noi; {@code hong} = da gap loi CSDL -> dong luon, lan sau mo moi. */
    public void tra(final Muon m, final boolean hong) {
        if (m == null) {
            return;
        }
        try {
            boolean bo = hong || this.dong;
            if (!bo) {
                try {
                    bo = m.ketNoi.isClosed();
                } catch (final SQLException e) {
                    bo = true;
                }
            }
            if (bo) {
                if (hong) {
                    this.soBoHong.incrementAndGet();
                }
                this.huy(m);
            } else {
                m.traLuc = System.currentTimeMillis();
                this.ranh.addFirst(m); // LIFO: ket noi nong dung lai, ket noi lanh nam cuoi va gia di
            }
        } finally {
            this.cho.release();
        }
    }

    private boolean conSong(final Connection c) {
        try {
            return !c.isClosed() && c.isValid(2);
        } catch (final SQLException | RuntimeException e) {
            return false;
        }
    }

    private void huy(final Muon m) {
        this.dangMo.decrementAndGet();
        try {
            m.ketNoi.close();
        } catch (final SQLException | RuntimeException ignored) {
        }
    }

    public int dangMo() {
        return this.dangMo.get();
    }

    public int dangMuon() {
        return this.toiDa - this.cho.availablePermits();
    }

    public int toiDa() {
        return this.toiDa;
    }

    /** Dong moi ket noi ranh; ket noi dang muon bi dong khi tra. */
    @Override
    public void close() {
        this.dong = true;
        Muon m;
        while ((m = this.ranh.pollFirst()) != null) {
            this.huy(m);
        }
    }
}
