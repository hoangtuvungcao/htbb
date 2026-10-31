package com.htbb.dangnhap;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gioi han theo IP cua BEN GOI (game server), khong phai IP nguoi choi: login server chi thay ket noi tu game server
 * (thuong mot ket noi tu 127.0.0.1). Gioi han dang nhap theo IP NGUOI CHOI van nam o game (sv.dangNhapIpNguong,
 * com.htbb.mang.GioiHanIp) nhu truoc.
 *
 * <ul>
 *   <li>danh sach IP duoc noi (login.ipChoPhep; rong = moi IP, nhu ban cu - ufw chan cong tu ngoai);</li>
 *   <li>so ket noi dong thoi toi da tong / moi IP (ban cu: khong gioi han, moi ket noi 2 luong khong bao gio thu hoi);</li>
 *   <li>so yeu cau / giay moi IP (xo token; 0 = tat, mac dinh). Vuot -> tra -3 ngay thay vi xep hang.</li>
 * </ul>
 */
public final class GioiHanKetNoi {

    private final List<String> choPhep;
    private final int toiDa;
    private final int moiIp;
    private final int yeuCauMoiGiay;
    private final AtomicInteger tong = new AtomicInteger();
    private final ConcurrentHashMap<String, AtomicInteger> theoIp = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Xo> xo = new ConcurrentHashMap<>();

    public GioiHanKetNoi(final List<String> choPhep, final int toiDa, final int moiIp, final int yeuCauMoiGiay) {
        this.choPhep = choPhep;
        this.toiDa = toiDa;
        this.moiIp = moiIp;
        this.yeuCauMoiGiay = yeuCauMoiGiay;
    }

    /** null = nhan; khac null = ly do tu choi. Nhan thi PHAI goi {@link #nha(String)} khi ket noi dong. */
    public String nhan(final String ip) {
        if (!this.choPhep.isEmpty() && !this.choPhep.contains(ip)) {
            return "IP khong nam trong login.ipChoPhep";
        }
        if (this.tong.incrementAndGet() > this.toiDa) {
            this.tong.decrementAndGet();
            return "qua " + this.toiDa + " ket noi";
        }
        AtomicInteger d = this.theoIp.get(ip);
        if (d == null) {
            final AtomicInteger moi = new AtomicInteger();
            d = this.theoIp.putIfAbsent(ip, moi);
            if (d == null) {
                d = moi;
            }
        }
        if (d.incrementAndGet() > this.moiIp) {
            d.decrementAndGet();
            this.tong.decrementAndGet();
            return "qua " + this.moiIp + " ket noi tu IP nay";
        }
        return null;
    }

    public void nha(final String ip) {
        this.tong.decrementAndGet();
        final AtomicInteger d = this.theoIp.get(ip);
        if (d != null && d.decrementAndGet() <= 0) {
            this.theoIp.remove(ip, d);
        }
    }

    public int soKetNoi() {
        return this.tong.get();
    }

    /** Mot yeu cau tu ip luc bayGio (ms): true = cho qua. */
    public boolean choYeuCau(final String ip, final long bayGio) {
        if (this.yeuCauMoiGiay <= 0) {
            return true;
        }
        Xo x = this.xo.get(ip);
        if (x == null) {
            final Xo moi = new Xo(this.yeuCauMoiGiay, bayGio);
            x = this.xo.putIfAbsent(ip, moi);
            if (x == null) {
                x = moi;
            }
        }
        return x.lay(this.yeuCauMoiGiay, bayGio);
    }

    /** Xo token: day toi da = so yeu cau / giay (cho phep dot ngan bang mot giay), nap deu. */
    static final class Xo {
        private double con;
        private long luc;

        Xo(final int day, final long bayGio) {
            this.con = day;
            this.luc = bayGio;
        }

        synchronized boolean lay(final int moiGiay, final long bayGio) {
            if (bayGio > this.luc) {
                this.con = Math.min(moiGiay, this.con + (bayGio - this.luc) * moiGiay / 1000.0);
                this.luc = bayGio;
            }
            if (this.con >= 1.0) {
                this.con -= 1.0;
                return true;
            }
            return false;
        }
    }
}
