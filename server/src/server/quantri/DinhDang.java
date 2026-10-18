package server.quantri;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Dinh dang so / thoi gian cho bang dieu khien (ham thuan, kiem thu duoc). */
public final class DinhDang {

    private DinhDang() {
    }

    /** 1234567 -> "1.234.567" (kieu Viet Nam). */
    public static String so(final long n) {
        final String s = Long.toString(Math.abs(n));
        final StringBuilder b = new StringBuilder(s.length() + 6);
        final int dau = s.length() % 3;
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (i - dau) % 3 == 0) {
                b.append('.');
            }
            b.append(s.charAt(i));
        }
        return (n < 0 ? "-" : "") + b;
    }

    /** So gon: 1.2K, 3.4M, 5.6B. */
    public static String gon(final double n) {
        final double a = Math.abs(n);
        if (a >= 1e9) {
            return mot(n / 1e9) + "B";
        }
        if (a >= 1e6) {
            return mot(n / 1e6) + "M";
        }
        if (a >= 1e4) {
            return mot(n / 1e3) + "K";
        }
        if (a == Math.rint(a)) {
            return so((long) n);
        }
        return mot(n);
    }

    static String mot(final double v) {
        final long x10 = Math.round(v * 10);
        return (x10 % 10 == 0) ? Long.toString(x10 / 10) : (x10 / 10) + "," + Math.abs(x10 % 10);
    }

    /** 3725000 ms -> "1 giờ 2 phút". */
    public static String thoiLuong(final long ms) {
        long giay = Math.max(0, ms / 1000);
        final long ngay = giay / 86400;
        giay %= 86400;
        final long gio = giay / 3600;
        giay %= 3600;
        final long phut = giay / 60;
        giay %= 60;
        if (ngay > 0) {
            return ngay + " ngày" + (gio > 0 ? " " + gio + " giờ" : "");
        }
        if (gio > 0) {
            return gio + " giờ" + (phut > 0 ? " " + phut + " phút" : "");
        }
        if (phut > 0) {
            return phut + " phút" + (giay > 0 ? " " + giay + " giây" : "");
        }
        return giay + " giây";
    }

    /** 3725000 -> "01:02:05". */
    public static String dongHo(final long ms) {
        long g = Math.max(0, ms / 1000);
        final long h = g / 3600;
        g %= 3600;
        return String.format("%02d:%02d:%02d", h, g / 60, g % 60);
    }

    public static String gio(final long ms) {
        return new SimpleDateFormat("HH:mm:ss").format(new Date(ms));
    }

    public static String ngayGio(final long ms) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(ms));
    }

    public static String mb(final long byteSo) {
        return so(byteSo >> 20) + " MB";
    }
}
