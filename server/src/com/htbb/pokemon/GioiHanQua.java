package com.htbb.pokemon;

import java.util.HashMap;
import java.util.Map;

/**
 * Bo dem gioi han qua (moi ngay + ca dot su kien) cho cac nguon xu / luong co tran: tui li xi, ma li xi, giai lon.
 * Mot "o dem" = (ma, khoa): ma = loai + dot (vd "lixi_tui_mo_2026"), khoa = ai (ten Pokemon, "u" + tai khoan,
 * "m:" + thiet bi, "i:" + IP, "*" = ca may chu).
 *
 * {@link #thu} la phep KIEM-VA-CONG NGUYEN TU: chi cong khi ca hai tran (ngay, ca dot) con du; tra false thi khong
 * doi gi. Ban CSDL: data.DatabaseGioiHanQua (bang tob_gioi_han_qua, mot cau UPDATE co dieu kien); ban bo nho
 * {@link BoNho} cho test va khi khong co CSDL.
 */
public final class GioiHanQua {

    private GioiHanQua() {
    }

    /** Khong gioi han. */
    public static final long VO_HAN = Long.MAX_VALUE / 4;

    public interface KhoDem {
        /**
         * Cong {@code them} vao o (ma, khoa) neu sau khi cong: so trong ngay {@code ngay} <= ngayToiDa VA tong ca dot
         * <= tongToiDa. @return true = da cong.
         */
        boolean thu(String ma, String khoa, long them, long ngayToiDa, long tongToiDa, String ngay);

        /** Tong ca dot hien tai cua o (0 neu chua co). */
        long tong(String ma, String khoa);

        /** Tru lai (hoan) {@code bot} vua cong - dung khi buoc sau that bai. */
        void hoan(String ma, String khoa, long bot, String ngay);
    }

    /** Ban bo nho (an toan luong). */
    public static final class BoNho implements KhoDem {
        private final Map<String, long[]> o = new HashMap<String, long[]>();
        private final Map<String, String> ngayCua = new HashMap<String, String>();

        @Override
        public synchronized boolean thu(final String ma, final String khoa, final long them, final long ngayToiDa,
                final long tongToiDa, final String ngay) {
            if (them <= 0L) {
                return false;
            }
            final String k = ma + "|" + khoa;
            long[] x = this.o.get(k);
            if (x == null) {
                x = new long[2];
                this.o.put(k, x);
            }
            final boolean cungNgay = ngay.equals(this.ngayCua.get(k));
            final long soNgay = (cungNgay ? x[0] : 0L) + them;
            final long tong = x[1] + them;
            if (soNgay > ngayToiDa || tong > tongToiDa) {
                return false;
            }
            x[0] = soNgay;
            x[1] = tong;
            this.ngayCua.put(k, ngay);
            return true;
        }

        @Override
        public synchronized long tong(final String ma, final String khoa) {
            final long[] x = this.o.get(ma + "|" + khoa);
            return x == null ? 0L : x[1];
        }

        @Override
        public synchronized void hoan(final String ma, final String khoa, final long bot, final String ngay) {
            final String k = ma + "|" + khoa;
            final long[] x = this.o.get(k);
            if (x == null) {
                return;
            }
            if (ngay.equals(this.ngayCua.get(k))) {
                x[0] = Math.max(0L, x[0] - bot);
            }
            x[1] = Math.max(0L, x[1] - bot);
        }
    }
}
