package com.htbb.dangnhap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * LOGIN SERVER HTBB (thay CheckLoginSocket.jar, giao thuc va ngu nghia CSDL y het - docs/dang-nhap.md).
 *
 * <pre>
 *   luong nhan ket noi ──▶ moi ket noi (game server) 1 luong DOC: doc khung, kiem gioi han, xep viec
 *                                    │  (hang doi CHAN TREN login.hangDoi.toiDa; day -> tra -3 ngay)
 *                                    ▼
 *                         be luong xu ly co dinh (login.luong) ──▶ be ket noi CSDL (login.csdl.toiDa, cho co han)
 *                                    │
 *                                    ▼  ghi tra loi (khoa theo ket noi; thu tu tra loi co the khac thu tu gui)
 * </pre>
 *
 * Chay: {@code cd server/loginServer && java -jar htbb-login.jar} (doc ./server.ini; -Dhtbb.login.ini=duong_dan de
 * doi tep). Tat em: SIGTERM (systemctl stop) -> ngung nhan, xu ly not yeu cau da nhan (toi da login.dungChoMs), dong CSDL.
 */
public final class MayChuDangNhap {

    public static final String PHIEN_BAN = "1.0";

    private final CauHinh ch;
    private final XacThuc xacThuc;
    private final BeKetNoi be;
    private final ThongKe tk = new ThongKe();
    private final GioiHanKetNoi gioiHan;
    private final ThreadPoolExecutor tho;
    private final Set<KetNoi> ketNoi = ConcurrentHashMap.newKeySet();
    private final AtomicLong soKetNoiDaNhan = new AtomicLong();
    private ScheduledExecutorService nhip;
    /** Hen gio han xu ly tung yeu cau (login.hanXuLyMs). */
    private final java.util.concurrent.ScheduledThreadPoolExecutor canhGac;
    private ServerSocket ss;
    private Thread luongNhan;
    private volatile boolean dangChay;
    private volatile boolean daDung;

    /**
     * @param be be ket noi de dong luc tat va bao so ket noi trong [NHIP]; null duoc (bo kiem dung nguon gia).
     */
    public MayChuDangNhap(final CauHinh ch, final XacThuc xacThuc, final BeKetNoi be) {
        this.ch = ch;
        this.xacThuc = xacThuc;
        this.be = be;
        this.gioiHan = new GioiHanKetNoi(ch.ipChoPhep, ch.ketNoiToiDa, ch.ketNoiMoiIp, ch.yeuCauMoiGiayMoiIp);
        this.tho = new ThreadPoolExecutor(ch.luong, ch.luong, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(ch.hangDoiToiDa), luong("DangNhap-xuLy", false),
                new ThreadPoolExecutor.AbortPolicy());
        this.canhGac = new java.util.concurrent.ScheduledThreadPoolExecutor(1, luong("DangNhap-canhGac", true));
        this.canhGac.setRemoveOnCancelPolicy(true);
    }

    private static ThreadFactory luong(final String tien, final boolean daemon) {
        final AtomicInteger n = new AtomicInteger();
        return new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                final Thread t = new Thread(r, tien + "-" + n.incrementAndGet());
                t.setDaemon(daemon);
                return t;
            }
        };
    }

    public ThongKe thongKe() {
        return this.tk;
    }

    /** Cong dang nghe (sau {@link #batDau()}; cong=0 trong cau hinh -> cong ngau nhien, dung cho bo kiem). */
    public int cong() {
        return this.ss.getLocalPort();
    }

    public int soKetNoi() {
        return this.ketNoi.size();
    }

    public void batDau() throws IOException {
        this.ss = new ServerSocket();
        this.ss.setReuseAddress(true);
        final InetAddress dc = this.ch.diaChi.isEmpty() ? null : InetAddress.getByName(this.ch.diaChi);
        this.ss.bind(new InetSocketAddress(dc, this.ch.cong), 128);
        this.dangChay = true;
        this.luongNhan = new Thread(new Runnable() {
            @Override
            public void run() {
                vongNhan();
            }
        }, "DangNhap-nhan");
        this.luongNhan.start();
        if (this.ch.nhipGiay > 0) {
            this.nhip = Executors.newSingleThreadScheduledExecutor(luong("DangNhap-nhip", true));
            this.nhip.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        NhatKy.thongTin("NHIP", dongNhip());
                    } catch (final Throwable ignored) {
                    }
                }
            }, this.ch.nhipGiay, this.ch.nhipGiay, TimeUnit.SECONDS);
        }
        NhatKy.thongTin("KHOI_DONG", "login server HTBB " + PHIEN_BAN + " nghe cong " + this.cong() + " - " + this.ch.tomTat());
    }

    /** Noi dung dong [NHIP] (va dat lai bo dem chu ky). */
    public String dongNhip() {
        final StringBuilder t = new StringBuilder(96);
        t.append("hang=").append(this.tho.getQueue().size()).append('/').append(this.ch.hangDoiToiDa);
        t.append(" xuLy=").append(this.tho.getActiveCount());
        t.append(" ketNoi=").append(this.ketNoi.size());
        if (this.be != null) {
            t.append(" csdl=").append(this.be.dangMuon()).append('/').append(this.be.dangMo()).append('/')
                    .append(this.be.toiDa());
            final long hetCho = this.be.soHetCho.get();
            final long boHong = this.be.soBoHong.get();
            if (hetCho > 0 || boHong > 0) {
                t.append(" csdlHetCho=").append(hetCho).append(" csdlBoHong=").append(boHong);
            }
        }
        t.append(" tong=").append(this.tk.tongYeuCau.get());
        return this.tk.tomTatVaXoa(t.toString());
    }

    private void vongNhan() {
        while (this.dangChay) {
            final Socket s;
            try {
                s = this.ss.accept();
            } catch (final IOException e) {
                if (this.dangChay) {
                    NhatKy.loi("KET_NOI", "accept loi: " + e);
                    nghi(200L);
                }
                continue;
            }
            final String ip = s.getInetAddress().getHostAddress();
            final String tu = ip + ":" + s.getPort();
            final String lyDo = this.gioiHan.nhan(ip);
            if (lyDo != null) {
                NhatKy.canhBao("KET_NOI", "tu choi " + tu + ": " + lyDo);
                dongIm(s);
                continue;
            }
            try {
                s.setTcpNoDelay(true);
                s.setKeepAlive(true);
                final KetNoi k = new KetNoi(s, ip, tu);
                this.ketNoi.add(k);
                this.soKetNoiDaNhan.incrementAndGet();
                NhatKy.thongTin("KET_NOI", "nhan ket noi " + tu + " (dang mo " + this.ketNoi.size() + ")");
                final Thread t = new Thread(k, "DangNhap-doc-" + tu);
                t.setDaemon(true);
                t.start();
            } catch (final IOException | RuntimeException e) {
                this.gioiHan.nha(ip);
                NhatKy.loi("KET_NOI", "khong mo duoc ket noi " + tu + ": " + e);
                dongIm(s);
            }
        }
    }

    /** Mot ket noi tu game server. */
    private final class KetNoi implements Runnable {
        final Socket s;
        final String ip;
        final String tu;
        final InputStream vao;
        final OutputStream ra;
        final Object khoaGhi = new Object();
        volatile boolean hong;

        KetNoi(final Socket s, final String ip, final String tu) throws IOException {
            this.s = s;
            this.ip = ip;
            this.tu = tu;
            this.vao = new BufferedInputStream(s.getInputStream(), 8192);
            this.ra = new BufferedOutputStream(s.getOutputStream(), 1024);
        }

        @Override
        public void run() {
            String lyDo = "dong";
            try {
                while (true) {
                    final GiaoThuc.Khung k = GiaoThuc.docKhung(this.vao, MayChuDangNhap.this.ch.khungToiDa);
                    if (k == null) {
                        lyDo = "ben kia dong";
                        break;
                    }
                    final long nhanLuc = System.nanoTime();
                    if (k.lenh != GiaoThuc.LENH_KIEM) {
                        MayChuDangNhap.this.tk.lenhLa.incrementAndGet();
                        continue; // ban cu: bo qua lenh la, khong tra loi
                    }
                    xepViec(this, k.duLieu, nhanLuc);
                }
            } catch (final GiaoThuc.KhungQuaLon e) {
                MayChuDangNhap.this.tk.khungHong.incrementAndGet();
                lyDo = "khung qua lon (" + e.getMessage() + ") - cat ket noi";
            } catch (final SocketException e) {
                lyDo = MayChuDangNhap.this.dangChay ? "loi socket: " + e.getMessage() : "may chu tat";
            } catch (final IOException e) {
                lyDo = "doc loi: " + e;
            } catch (final Throwable e) {
                lyDo = "loi: " + e;
            } finally {
                MayChuDangNhap.this.gioiHan.nha(this.ip);
                if (MayChuDangNhap.this.dangChay) {
                    MayChuDangNhap.this.ketNoi.remove(this);
                    dongIm(this.s);
                } // dang tat: giu trong tap - dung() tra loi not roi moi dong socket
                NhatKy.thongTin("KET_NOI", "dong ket noi " + this.tu + ": " + lyDo + " (con " + MayChuDangNhap.this.ketNoi.size() + ")");
            }
        }

        void gui(final GiaoThuc.TraLoi t) {
            if (this.hong) {
                return;
            }
            final byte[] b = GiaoThuc.khungTraLoi(t);
            synchronized (this.khoaGhi) {
                try {
                    this.ra.write(b);
                    this.ra.flush();
                } catch (final IOException e) {
                    this.hong = true;
                    NhatKy.canhBao("KET_NOI", "khong gui duoc tra loi toi " + this.tu + " (phien " + t.phien + "): " + e);
                    dongIm(this.s);
                }
            }
        }

        /** Ngung doc (khong nhan yeu cau moi) nhung van ghi duoc tra loi. */
        void ngungDoc() {
            try {
                this.s.shutdownInput();
            } catch (final IOException ignored) {
            }
        }
    }

    private void xepViec(final KetNoi k, final byte[] duLieu, final long nhanLuc) {
        final GiaoThuc.YeuCau yc;
        try {
            yc = GiaoThuc.docYeuCau(duLieu);
        } catch (final IOException e) {
            this.tk.khungHong.incrementAndGet();
            final Long phien = GiaoThuc.docPhien(duLieu);
            NhatKy.canhBao("DANG_NHAP", "goi kiem hong tu " + k.tu + " (" + duLieu.length + " byte)"
                    + (phien == null ? " - khong co phien, bo" : " - tra -3 cho phien " + phien));
            if (phien != null) {
                k.gui(new GiaoThuc.TraLoi(phien, XacThuc.LOI, false, (byte) 0));
            }
            return;
        }
        if (!this.gioiHan.choYeuCau(k.ip, System.currentTimeMillis())) {
            this.tk.ghiTuChoi(tre(nhanLuc));
            k.gui(new GiaoThuc.TraLoi(yc.phien, XacThuc.LOI, false, (byte) 0));
            NhatKy.canhBao("GIOI_HAN", "vuot " + this.ch.yeuCauMoiGiayMoiIp + " yeu cau/s tu " + k.ip + ": "
                    + NhatKy.ten(yc.ten) + " -> -3");
            return;
        }
        final Viec v = new Viec(k, yc, nhanLuc);
        try {
            this.tho.execute(v);
        } catch (final RejectedExecutionException e) {
            if (v.traLoi(XacThuc.LOI, false, (byte) 0)) {
                this.tk.ghiTuChoi(tre(nhanLuc));
                NhatKy.canhBao("QUA_TAI", (this.dangChay ? "hang doi day (" + this.ch.hangDoiToiDa + ")" : "dang tat")
                        + ": " + NhatKy.ten(yc.ten) + " -> -3");
            }
            return;
        }
        // Canh gac han: du viec dang ket o dau (hang doi, cho ket noi, CSDL treo giua chung), toi han la tra -3 ngay -
        // game luon nhan tra loi truoc khi het 15 s cho cua no. Viec xong sau han thi tra loi do bi bo (da tra roi).
        try {
            v.henGio = this.canhGac.schedule(new Runnable() {
                @Override
                public void run() {
                    if (v.traLoi(XacThuc.LOI, false, (byte) 0)) {
                        final long t = tre(nhanLuc);
                        MayChuDangNhap.this.tk.ghiHetHan(t);
                        NhatKy.canhBao("QUA_TAI", "qua han " + t + " ms (" + (v.batDau ? "dang truy van CSDL" : "con trong hang")
                                + "): " + NhatKy.ten(yc.ten) + " -> -3");
                    }
                }
            }, this.ch.hanXuLyMs, TimeUnit.MILLISECONDS);
        } catch (final RejectedExecutionException ignored) {
            // dang tat
        }
    }

    /** Mot yeu cau dang xu ly: tra loi DUNG MOT lan (ket qua that hoac -3 khi qua han / tu choi). */
    private final class Viec implements Runnable {
        final KetNoi k;
        final GiaoThuc.YeuCau yc;
        final long nhanLuc;
        final java.util.concurrent.atomic.AtomicBoolean daTra = new java.util.concurrent.atomic.AtomicBoolean();
        volatile java.util.concurrent.ScheduledFuture<?> henGio;
        volatile boolean batDau;

        Viec(final KetNoi k, final GiaoThuc.YeuCau yc, final long nhanLuc) {
            this.k = k;
            this.yc = yc;
            this.nhanLuc = nhanLuc;
        }

        boolean traLoi(final int ma, final boolean vina, final byte fromgame) {
            if (!this.daTra.compareAndSet(false, true)) {
                return false;
            }
            final java.util.concurrent.ScheduledFuture<?> h = this.henGio;
            if (h != null) {
                h.cancel(false);
            }
            this.k.gui(new GiaoThuc.TraLoi(this.yc.phien, ma, vina, fromgame));
            return true;
        }

        @Override
        public void run() {
            if (this.daTra.get()) {
                return; // da tra -3 vi qua han trong luc nam hang: khong ton CSDL nua
            }
            this.batDau = true;
            xuLy(this);
        }
    }

    private void xuLy(final Viec v) {
        final GiaoThuc.YeuCau yc = v.yc;
        final long cho = tre(v.nhanLuc);
        final long hanChot = System.currentTimeMillis() + (this.ch.hanXuLyMs - cho);
        final XacThuc.KetQua kq = this.xacThuc.kiem(yc.ten, yc.matKhau, yc.maGame, hanChot);
        final long tong = tre(v.nhanLuc);
        if (!v.traLoi(kq.ma, kq.vinaphone, kq.fromgame)) {
            NhatKy.canhBao("QUA_TAI", NhatKy.ten(yc.ten) + " xong sau han (" + tong + " ms, kq=" + kq.ma
                    + ") - da tra -3 truoc do");
            return;
        }
        this.tk.ghiKetQua(kq.ma, tong);
        if (kq.loi != null) {
            NhatKy.loi("DANG_NHAP", NhatKy.ten(yc.ten) + " kq=-3 (" + kq.loi + ") cho=" + cho + "ms");
        } else if (this.ch.logMoiLan) {
            NhatKy.thongTin("DANG_NHAP", NhatKy.ten(yc.ten) + " kq=" + kq.ma + " tre=" + tong + "ms");
        }
    }

    private static long tre(final long nhanLucNano) {
        return (System.nanoTime() - nhanLucNano) / 1000000L;
    }

    /**
     * Tat em: ngung nhan ket noi + yeu cau moi, cho xu ly xong yeu cau da nhan (toi da {@code choMs}), roi dong ket
     * noi va CSDL. Goi nhieu lan khong sao.
     */
    public synchronized void dung(final long choMs) {
        if (this.daDung) {
            return;
        }
        this.daDung = true;
        this.dangChay = false;
        NhatKy.thongTin("TAT", "dang tat: ngung nhan, xu ly not " + this.tho.getQueue().size() + " yeu cau trong hang");
        dongIm(this.ss);
        for (final KetNoi k : this.ketNoi) {
            k.ngungDoc();
        }
        this.tho.shutdown();
        boolean xong;
        try {
            xong = this.tho.awaitTermination(Math.max(0L, choMs), TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            xong = false;
        }
        if (!xong) {
            final int con = this.tho.shutdownNow().size();
            NhatKy.canhBao("TAT", "het " + choMs + " ms cho - bo " + con + " yeu cau chua xu ly");
        }
        for (final KetNoi k : this.ketNoi) {
            dongIm(k.s);
        }
        this.canhGac.shutdownNow();
        if (this.nhip != null) {
            this.nhip.shutdownNow();
        }
        if (this.be != null) {
            this.be.close();
        }
        NhatKy.thongTin("TAT", "da tat - tong " + this.tk.tongYeuCau.get() + " yeu cau, tu choi "
                + this.tk.tongTuChoi.get() + ", " + this.soKetNoiDaNhan.get() + " ket noi tu luc bat. " + dongNhip());
        NhatKy.xa(2000L);
    }

    private static void dongIm(final java.io.Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (final IOException | RuntimeException ignored) {
        }
    }

    private static void dongIm(final Socket s) {
        try {
            s.close();
        } catch (final IOException | RuntimeException ignored) {
        }
    }

    private static void nghi(final long ms) {
        try {
            Thread.sleep(ms);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Doc server.ini (UTF-8, cung dinh dang Properties ban cu doc). */
    static Properties docTep(final File f) throws IOException {
        final Properties p = new Properties();
        try (InputStreamReader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            p.load(r);
        }
        return p;
    }

    public static void main(final String[] args) {
        final File tep = new File(System.getProperty("htbb.login.ini", "server.ini"));
        final CauHinh ch;
        try {
            ch = CauHinh.doc(docTep(tep));
        } catch (final IOException e) {
            NhatKy.loi("KHOI_DONG", "khong doc duoc " + tep.getAbsolutePath() + ": " + e
                    + " (mau: vps/loginServer.server.ini.example)");
            NhatKy.xa(2000L);
            System.exit(1);
            return;
        }
        if (!ch.loi.isEmpty()) {
            NhatKy.loi("KHOI_DONG", tep.getAbsolutePath() + ": " + ch.loi);
            NhatKy.xa(2000L);
            System.exit(1);
            return;
        }
        final BeKetNoi be = new BeKetNoi(
                NguonCsdl.nhaMay(ch.dbUrl, ch.dbUser, ch.dbMatKhau, ch.csdlKetNoiMs, ch.csdlSocketMs),
                ch.csdlToiDa, ch.csdlChoMuonMs, ch.csdlKiemSauRanhGiay * 1000L, ch.csdlTuoiToiDaPhut * 60000L);
        // Thu CSDL mot lan cho log ro rang (khong chan khoi dong: CSDL len cham thi yeu cau dau tra -3, sau do tu noi lai).
        try {
            final BeKetNoi.Muon m = be.muon();
            be.tra(m, false);
            NhatKy.thongTin("CSDL", "ket noi CSDL tai khoan ok (" + CauHinh.anMatKhauUrl(ch.dbUrl) + ")");
        } catch (final Exception e) {
            NhatKy.loi("CSDL", "chua ket noi duoc CSDL tai khoan (" + CauHinh.anMatKhauUrl(ch.dbUrl) + "): " + e
                    + " - van nghe cong, yeu cau se tra -3 toi khi CSDL len");
        }
        final MayChuDangNhap may = new MayChuDangNhap(ch, new XacThuc(new NguonCsdl(be, ch.csdlTruyVanGiay, ch.csdlSocketMs)), be);
        try {
            may.batDau();
        } catch (final BindException e) {
            NhatKy.loi("KHOI_DONG", "cong " + ch.cong + " dang bi chiem: " + e.getMessage());
            NhatKy.xa(2000L);
            System.exit(1);
            return;
        } catch (final IOException e) {
            NhatKy.loi("KHOI_DONG", "khong mo duoc cong " + ch.cong + ": " + e);
            NhatKy.xa(2000L);
            System.exit(1);
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                may.dung(ch.dungChoMs);
            }
        }, "DangNhap-tat"));
    }
}
