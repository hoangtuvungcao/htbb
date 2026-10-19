package com.htbb.pokemon;

/**
 * Am lich Viet Nam (mui gio +7), thuat toan thien van cua Ho Ngoc Duc (cong khai, dung rong rai cho lich Viet):
 * trang moi theo Meeus, trung khi theo kinh do mat troi, thang nhuan = thang dau tien khong chua trung khi.
 *
 * Lop THUAN, khong phu thuoc gio may - test/KiemLichNam.java doi chieu Tet / Gio To / Vu Lan / Trung Thu 2026-2035
 * voi bang ngay da cong bo. Dung cho lich su kien nam ({@link LichNam}).
 */
public final class AmLich {

    /** Mui gio Viet Nam (gio). */
    public static final double MUI_GIO = 7.0;

    private AmLich() {
    }

    // ------------------------------------------------------------------ ngay Julius

    public static int jdTuNgay(final int dd, final int mm, final int yy) {
        final int a = (14 - mm) / 12;
        final int y = yy + 4800 - a;
        final int m = mm + 12 * a - 3;
        int jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045;
        if (jd < 2299161) {
            jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083;
        }
        return jd;
    }

    /** {ngay, thang, nam} duong lich cua so ngay Julius. */
    public static int[] ngayTuJd(final int jd) {
        final int b;
        final int c;
        if (jd > 2299160) {
            final int a = jd + 32044;
            b = (4 * a + 3) / 146097;
            c = a - (b * 146097) / 4;
        } else {
            b = 0;
            c = jd + 32082;
        }
        final int d = (4 * c + 3) / 1461;
        final int e = c - (1461 * d) / 4;
        final int m = (5 * e + 2) / 153;
        final int ngay = e - (153 * m + 2) / 5 + 1;
        final int thang = m + 3 - 12 * (m / 10);
        final int nam = b * 100 + d - 4800 + m / 10;
        return new int[]{ngay, thang, nam};
    }

    // ------------------------------------------------------------------ thien van

    static double trangMoi(final int k) {
        final double t = k / 1236.85;
        final double t2 = t * t;
        final double t3 = t2 * t;
        final double dr = Math.PI / 180;
        double jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3;
        jd1 = jd1 + 0.00033 * Math.sin((166.56 + 132.87 * t - 0.009173 * t2) * dr);
        final double m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3;
        final double mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3;
        final double f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3;
        double c1 = (0.1734 - 0.000393 * t) * Math.sin(m * dr) + 0.0021 * Math.sin(2 * dr * m);
        c1 = c1 - 0.4068 * Math.sin(mpr * dr) + 0.0161 * Math.sin(dr * 2 * mpr);
        c1 = c1 - 0.0004 * Math.sin(dr * 3 * mpr);
        c1 = c1 + 0.0104 * Math.sin(dr * 2 * f) - 0.0051 * Math.sin(dr * (m + mpr));
        c1 = c1 - 0.0074 * Math.sin(dr * (m - mpr)) + 0.0004 * Math.sin(dr * (2 * f + m));
        c1 = c1 - 0.0004 * Math.sin(dr * (2 * f - m)) - 0.0006 * Math.sin(dr * (2 * f + mpr));
        c1 = c1 + 0.0010 * Math.sin(dr * (2 * f - mpr)) + 0.0005 * Math.sin(dr * (2 * mpr + m));
        final double deltat;
        if (t < -11) {
            deltat = 0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3;
        } else {
            deltat = -0.000278 + 0.000265 * t + 0.000262 * t2;
        }
        return jd1 + c1 - deltat;
    }

    static int ngayTrangMoi(final int k, final double muiGio) {
        return (int) Math.floor(trangMoi(k) + 0.5 + muiGio / 24);
    }

    static double kinhDoMatTroi(final double jdn) {
        final double t = (jdn - 2451545.0) / 36525;
        final double t2 = t * t;
        final double dr = Math.PI / 180;
        final double m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2;
        final double l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2;
        double dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * Math.sin(dr * m);
        dl = dl + (0.019993 - 0.000101 * t) * Math.sin(dr * 2 * m) + 0.000290 * Math.sin(dr * 3 * m);
        double l = (l0 + dl) * dr;
        l = l - Math.PI * 2 * Math.floor(l / (Math.PI * 2));
        return l;
    }

    static int cungKinhDo(final int ngay, final double muiGio) {
        return (int) Math.floor(kinhDoMatTroi(ngay - 0.5 - muiGio / 24) / Math.PI * 6);
    }

    static int thang11(final int nam, final double muiGio) {
        final int off = jdTuNgay(31, 12, nam) - 2415021;
        final int k = (int) Math.floor(off / 29.530588853);
        int nm = ngayTrangMoi(k, muiGio);
        if (cungKinhDo(nm, muiGio) >= 9) {
            nm = ngayTrangMoi(k - 1, muiGio);
        }
        return nm;
    }

    static int lechThangNhuan(final int a11, final double muiGio) {
        final int k = (int) Math.floor((a11 - 2415021.076998695) / 29.530588853 + 0.5);
        int last;
        int i = 1;
        int arc = cungKinhDo(ngayTrangMoi(k + i, muiGio), muiGio);
        do {
            last = arc;
            i++;
            arc = cungKinhDo(ngayTrangMoi(k + i, muiGio), muiGio);
        } while (arc != last && i < 14);
        return i - 1;
    }

    // ------------------------------------------------------------------ doi lich

    /** Am -> duong: {ngay, thang, nam}; {0,0,0} neu thang nhuan khong ton tai. */
    public static int[] amSangDuong(final int ngayAm, final int thangAm, final int namAm, final boolean nhuan) {
        final double tz = MUI_GIO;
        final int a11;
        final int b11;
        if (thangAm < 11) {
            a11 = thang11(namAm - 1, tz);
            b11 = thang11(namAm, tz);
        } else {
            a11 = thang11(namAm, tz);
            b11 = thang11(namAm + 1, tz);
        }
        final int k = (int) Math.floor(0.5 + (a11 - 2415021.076998695) / 29.530588853);
        int off = thangAm - 11;
        if (off < 0) {
            off += 12;
        }
        if (b11 - a11 > 365) {
            final int leapOff = lechThangNhuan(a11, tz);
            int leapMonth = leapOff - 2;
            if (leapMonth < 0) {
                leapMonth += 12;
            }
            if (nhuan && thangAm != leapMonth) {
                return new int[]{0, 0, 0};
            } else if (nhuan || off >= leapOff) {
                off += 1;
            }
        }
        final int dauThang = ngayTrangMoi(k + off, tz);
        return ngayTuJd(dauThang + ngayAm - 1);
    }

    /** Duong -> am: {ngay, thang, nam, nhuan(0/1)}. */
    public static int[] duongSangAm(final int dd, final int mm, final int yy) {
        final double tz = MUI_GIO;
        final int soNgay = jdTuNgay(dd, mm, yy);
        final int k = (int) Math.floor((soNgay - 2415021.076998695) / 29.530588853);
        int dauThang = ngayTrangMoi(k + 1, tz);
        if (dauThang > soNgay) {
            dauThang = ngayTrangMoi(k, tz);
        }
        int a11 = thang11(yy, tz);
        int b11 = a11;
        int namAm;
        if (a11 >= dauThang) {
            namAm = yy;
            a11 = thang11(yy - 1, tz);
        } else {
            namAm = yy + 1;
            b11 = thang11(yy + 1, tz);
        }
        final int ngayAm = soNgay - dauThang + 1;
        final int diff = (int) Math.floor((dauThang - a11) / 29.0);
        int nhuan = 0;
        int thangAm = diff + 11;
        if (b11 - a11 > 365) {
            final int leapMonthDiff = lechThangNhuan(a11, tz);
            if (diff >= leapMonthDiff) {
                thangAm = diff + 10;
                if (diff == leapMonthDiff) {
                    nhuan = 1;
                }
            }
        }
        if (thangAm > 12) {
            thangAm = thangAm - 12;
        }
        if (thangAm >= 11 && diff < 4) {
            namAm -= 1;
        }
        return new int[]{ngayAm, thangAm, namAm, nhuan};
    }
}
