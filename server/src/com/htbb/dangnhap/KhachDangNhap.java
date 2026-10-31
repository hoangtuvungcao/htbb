package com.htbb.dangnhap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PHIA GAME cua giao thuc game <-> login server (docs/dang-nhap.md muc 2): MOT ket noi TCP ben, tu noi lai, va KHONG
 * lam mat yeu cau khi login server khoi dong lai.
 *
 * <p>Ban cu (io.LogInController + io.Session) kiem "con noi" roi moi cho hang; login server rot trong luc cho thi yeu
 * cau ke tiep bi ghi vao socket chet va mat - nguoi choi cho 15 s roi thay "Máy chủ đăng nhập chưa phản hồi". Nay:
 * <ul>
 *   <li>moi yeu cau nam trong {@link #dangCho} tu luc gui toi luc co tra loi / het han - gui lai duoc;</li>
 *   <li>luong gui cho hang co han (500 ms) nen phat hien rot ket noi ngay ca khi khong co yeu cau moi; noi lai xong
 *       (moi "the he" ket noi) thi GUI LAI moi yeu cau con cho chua gui tren the he nay, theo thu tu phien;</li>
 *   <li>tra loi CHI duoc giao khi {@code dangCho.remove(phien)} thanh cong - het han va tra loi den muon / tra loi
 *       trung (gui lai ma ban cu da xu ly) khong bao gio cho nguoi choi hai ket qua;</li>
 *   <li>han cho {@code choMs} (15 s) tinh tu lan gui DAU, khong lam moi khi gui lai.</li>
 * </ul>
 */
public final class KhachDangNhap {

    /** Nhan ket qua. Goi tu luong doc / luong het han cua khach - KHONG duoc chan lau. */
    public interface Nghe {
        void traLoi(long phien, int ketQua, boolean vinaphone, byte region);

        /** Qua choMs chua co tra loi. {@code tinhTrang}: so cho / hang / ket noi - de ghi log. */
        void hetHan(long phien, long daChoMs, String tinhTrang);

        /** Su kien ket noi (de ghi log). */
        void suKien(String chu);
    }

    /** Dia chi login server (doc moi lan noi lai: server.ini co the doi som). */
    public interface DiaChi {
        String host();

        int cong();
    }

    private static final class YeuCau {
        final long phien;
        final byte[] khung;
        final long lucGui = System.currentTimeMillis();
        volatile long theHeGui = -1L;

        YeuCau(final long phien, final byte[] khung) {
            this.phien = phien;
            this.khung = khung;
        }
    }

    private final DiaChi diaChi;
    private final Nghe nghe;
    private final long choMs;
    private final ConcurrentHashMap<Long, YeuCau> dangCho = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<Long> hang = new LinkedBlockingQueue<>();
    private volatile Socket socket;
    private volatile OutputStream ra;
    private volatile long theHe;
    private volatile boolean dung;
    public final AtomicLong soGuiLai = new AtomicLong();
    public final AtomicLong soNoiLai = new AtomicLong();

    public KhachDangNhap(final DiaChi diaChi, final Nghe nghe, final long choMs) {
        this.diaChi = diaChi;
        this.nghe = nghe;
        this.choMs = choMs;
    }

    public void batDau() {
        final Thread g = new Thread(new Runnable() {
            @Override
            public void run() {
                vongGui();
            }
        }, "LoginGui");
        g.setDaemon(true);
        g.start();
        final Thread h = new Thread(new Runnable() {
            @Override
            public void run() {
                vongHetHan();
            }
        }, "LoginHetHan");
        h.setDaemon(true);
        h.start();
    }

    /** Xep mot yeu cau. Phien phai duy nhat. */
    public void gui(final long phien, final String ten, final String matKhau, final byte maGame) {
        this.dangCho.put(phien, new YeuCau(phien, GiaoThuc.khungYeuCau(phien, ten, matKhau, maGame)));
        this.hang.add(phien);
    }

    /** So yeu cau chua gui di (game tu choi "quá tải" khi > 50, nhu ban cu). */
    public int hangGui() {
        return this.hang.size();
    }

    public int soDangCho() {
        return this.dangCho.size();
    }

    public boolean conNoi() {
        final Socket s = this.socket;
        return s != null && !s.isClosed();
    }

    /** Dung khach (bo kiem). */
    public void dung() {
        this.dung = true;
        dong(this.socket);
    }

    private String tinhTrang() {
        return "dang cho " + this.dangCho.size() + ", hang gui " + this.hang.size() + ", ket noi login "
                + (conNoi() ? "con" : "MAT");
    }

    // ------------------------------------------------------------------ gui + noi lai

    private void vongGui() {
        boolean daBaoLoi = false;
        while (!this.dung) {
            try {
                if (!conNoi()) {
                    if (!noiLai()) {
                        if (!daBaoLoi) {
                            this.nghe.suKien("CONNECT TO SERVER LOG IN FAIL !!!!!!!!");
                            daBaoLoi = true;
                        }
                        Thread.sleep(1000L);
                        continue;
                    }
                    daBaoLoi = false;
                    guiLaiDangCho();
                }
                final Long id = this.hang.poll(500L, TimeUnit.MILLISECONDS);
                if (id == null) {
                    continue;
                }
                final YeuCau y = this.dangCho.get(id);
                if (y == null || y.theHeGui == this.theHe) {
                    continue; // da tra loi / het han / da gui lai tren ket noi nay
                }
                ghi(y);
            } catch (final InterruptedException e) {
                return;
            } catch (final Throwable t) {
                this.nghe.suKien("loi luong gui login: " + t);
            }
        }
    }

    private boolean noiLai() {
        final Socket s = new Socket();
        try {
            s.connect(new InetSocketAddress(this.diaChi.host(), this.diaChi.cong()), 3000);
            s.setTcpNoDelay(true);
            s.setKeepAlive(true);
            final InputStream vao = new BufferedInputStream(s.getInputStream(), 4096);
            this.ra = new BufferedOutputStream(s.getOutputStream(), 4096);
            final long the = this.theHe + 1;
            this.theHe = the;
            this.socket = s;
            this.soNoiLai.incrementAndGet();
            final Thread d = new Thread(new Runnable() {
                @Override
                public void run() {
                    vongDoc(s, vao, the);
                }
            }, "LoginDoc-" + the);
            d.setDaemon(true);
            d.start();
            this.nghe.suKien("CONNECTED TO SERVER LOGIN");
            return true;
        } catch (final IOException | RuntimeException e) {
            dong(s);
            return false;
        }
    }

    /** Ket noi moi: gui lai (theo thu tu phien) moi yeu cau con cho ma chua gui tren ket noi nay. */
    private void guiLaiDangCho() {
        final List<YeuCau> ds = new ArrayList<>(this.dangCho.values());
        Collections.sort(ds, (a, b) -> Long.compare(a.phien, b.phien));
        int n = 0;
        for (final YeuCau y : ds) {
            // ca yeu cau ghi loi luc truoc (theHeGui = -1, da roi khoi hang); yeu cau con trong hang gui o day thi
            // vong gui bo qua sau (theHeGui == theHe)
            if (y.theHeGui != this.theHe && this.dangCho.containsKey(y.phien)) {
                if (!ghi(y)) {
                    return;
                }
                n++;
            }
        }
        if (n > 0 && this.theHe > 1) {
            this.soGuiLai.addAndGet(n);
            this.nghe.suKien("noi lai login server: gui lai " + n + " yeu cau dang cho");
        }
    }

    /** Ghi mot yeu cau; loi -> dong ket noi (vong gui se noi lai va gui lai). */
    private boolean ghi(final YeuCau y) {
        final OutputStream o = this.ra;
        final Socket s = this.socket;
        if (o == null || s == null) {
            return false;
        }
        try {
            o.write(y.khung);
            o.flush();
            y.theHeGui = this.theHe;
            return true;
        } catch (final IOException e) {
            this.nghe.suKien("gui yeu cau login loi (" + e + ") - noi lai va gui lai");
            dong(s);
            return false;
        }
    }

    // ------------------------------------------------------------------ doc

    private void vongDoc(final Socket s, final InputStream vao, final long the) {
        try {
            while (true) {
                final GiaoThuc.Khung k = GiaoThuc.docKhung(vao, 1024);
                if (k == null) {
                    break;
                }
                if (k.lenh != GiaoThuc.LENH_KIEM) {
                    continue;
                }
                final GiaoThuc.TraLoi t = GiaoThuc.docTraLoi(k.duLieu);
                if (this.dangCho.remove(t.phien) != null) {
                    this.nghe.traLoi(t.phien, t.ketQua, t.vinaphone, t.fromgame);
                } else {
                    this.nghe.suKien("login server tra loi phien " + t.phien
                            + " da het han cho (> " + this.choMs + " ms) hoac da tra loi - bo qua");
                }
            }
        } catch (final IOException | RuntimeException e) {
            // rot ket noi
        } finally {
            dong(s);
            if (!this.dung && the == this.theHe) {
                this.nghe.suKien("LOG IN SERVER DISCONNECTED - noi lai va gui lai " + this.dangCho.size() + " yeu cau dang cho");
            }
        }
    }

    // ------------------------------------------------------------------ het han

    private void vongHetHan() {
        while (!this.dung) {
            try {
                Thread.sleep(200L);
                final long bay = System.currentTimeMillis();
                for (final YeuCau y : new ArrayList<>(this.dangCho.values())) {
                    if (bay - y.lucGui >= this.choMs && this.dangCho.remove(y.phien) != null) {
                        this.nghe.hetHan(y.phien, bay - y.lucGui, tinhTrang());
                    }
                }
            } catch (final InterruptedException e) {
                return;
            } catch (final Throwable t) {
                this.nghe.suKien("kiem het han cho login loi: " + t);
            }
        }
    }

    private static void dong(final Socket s) {
        if (s != null) {
            try {
                s.close();
            } catch (final IOException ignored) {
            }
        }
    }
}
