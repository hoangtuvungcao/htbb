package com.htbb.dangnhap;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Nhat ky login server: cung dang dong voi game (com.htbb.log.Nhat) -
 * {@code yyyy-MM-dd HH:mm:ss.SSS <E|W|I> [LOAI] noi dung}. Ghi BAT DONG BO qua hang doi chan tren: luong xu ly
 * khong bao gio cho I/O cua stdout; hang day thi bo dong (dem, bao lai mot dong). Noi dung duoc lam sach (xuong dong,
 * ky tu dieu khien) de ten tai khoan khong gia mao them dong log. KHONG BAO GIO dua mat khau vao day.
 */
public final class NhatKy {

    private static final ThreadLocal<SimpleDateFormat> GIO = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
    };
    private static final ArrayBlockingQueue<String> HANG = new ArrayBlockingQueue<>(20000);
    private static final AtomicLong BO = new AtomicLong();
    private static volatile PrintStream ra = taoRa();
    private static final Thread GHI;

    static {
        GHI = new Thread(new Runnable() {
            @Override
            public void run() {
                vongGhi();
            }
        }, "DangNhap-nhatKy");
        GHI.setDaemon(true);
        GHI.start();
    }

    private NhatKy() {
    }

    private static PrintStream taoRa() {
        try {
            return new PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out), true, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            return System.out;
        }
    }

    /** Bo kiem: ghi vao luong khac (vd ByteArrayOutputStream). */
    static void datRa(final PrintStream p) {
        ra = p;
    }

    public static void thongTin(final String loai, final String tin) {
        dua('I', loai, tin);
    }

    public static void canhBao(final String loai, final String tin) {
        dua('W', loai, tin);
    }

    public static void loi(final String loai, final String tin) {
        dua('E', loai, tin);
    }

    private static void dua(final char muc, final String loai, final String tin) {
        final String dong = GIO.get().format(new Date()) + " " + muc + " [" + loai + "] " + sach(tin);
        if (!HANG.offer(dong)) {
            BO.incrementAndGet();
        }
    }

    private static void vongGhi() {
        while (true) {
            try {
                final String d = HANG.poll(1, TimeUnit.SECONDS);
                final long bo = BO.getAndSet(0L);
                if (bo > 0) {
                    ra.println(GIO.get().format(new Date()) + " W [NHAT_KY] hang log day - bo " + bo + " dong");
                }
                if (d != null) {
                    ra.println(d);
                }
            } catch (final InterruptedException e) {
                return;
            } catch (final Throwable ignored) {
            }
        }
    }

    /** Doi cho hang log ghi het (toi da ms) - goi luc tat. */
    public static void xa(final long ms) {
        final long het = System.currentTimeMillis() + ms;
        while (!HANG.isEmpty() && System.currentTimeMillis() < het) {
            try {
                Thread.sleep(20L);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        ra.flush();
    }

    /** Thay ky tu dieu khien (xuong dong, tab, ESC...) bang '?', cat chuoi qua dai. */
    public static String sach(final String s) {
        if (s == null) {
            return "null";
        }
        final int n = Math.min(s.length(), 2000);
        StringBuilder b = null;
        for (int i = 0; i < n; i++) {
            final char ch = s.charAt(i);
            if (ch < 0x20 || ch == 0x7F || (ch >= 0x80 && ch < 0xA0) || ch == 0x2028 || ch == 0x2029) {
                if (b == null) {
                    b = new StringBuilder(n);
                    b.append(s, 0, i);
                }
                b.append('?');
            } else if (b != null) {
                b.append(ch);
            }
        }
        final String r = b == null ? (n == s.length() ? s : s.substring(0, n)) : b.toString();
        return n < s.length() ? r + "...(" + s.length() + ")" : r;
    }

    /** Ten tai khoan cho log: lam sach + cat 40 ky tu. */
    public static String ten(final String s) {
        if (s == null) {
            return "null";
        }
        return sach(s.length() > 40 ? s.substring(0, 40) + "~" : s);
    }
}
