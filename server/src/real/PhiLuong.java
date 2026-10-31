package real;

import java.util.function.BooleanSupplier;

/**
 * Phi LUONG cua cac menu cu (thu cuoi, cong hien, nang cap thoi trang, tinh luyen, gia han than binh, ao hoa Hoa Ky
 * Lan...). Chu game 31/10/2026: luong (nap) la tien TOT NHAT - gia giu nguyen bang luong. Lop nay chi lam cho moi phep
 * tru la KIEM-VA-TRU MOT BUOC duoi Char.KHOA_TIEN: truoc day "getLuong() &lt; gia" -&gt; lam viec -&gt; subLuong(gia)
 * (subLuong kep ve 0) nen hai lenh xen nhau mua duoc khi chi du tien cho mot lan.
 */
public final class PhiLuong {

    /** Ket qua {@link #lamRoiTru}. */
    public static final int XONG = 0;
    public static final int THIEU = 1;
    public static final int KHONG_LAM_DUOC = 2;

    private PhiLuong() {
    }

    /** "200 lượng". */
    public static String gia(final long n) {
        return n + " lượng";
    }

    /** "Không đủ lượng: cần 200, đang có 12." */
    public static String khongDu(final Char p, final long n) {
        return "Không đủ lượng: cần " + n + ", đang có " + Math.max(0, p.getLuong()) + ".";
    }

    /** Kiem-va-tru nguyen tu. */
    public static boolean tru(final Char p, final long n) {
        if (n < 0L) {
            return false;
        }
        return n == 0L || p.truLuongNeuDu(n);
    }

    /**
     * Du luong -&gt; lam {@code viec} -&gt; viec thanh cong moi tru, tat ca trong MOT khoi Char.truNeuDu (khong ai tieu luong
     * xen giua). Viec that bai (vd thu cuoi da max) thi khong tru gi.
     */
    public static int lamRoiTru(final Char p, final long n, final BooleanSupplier viec) {
        if (n < 0L) {
            return THIEU;
        }
        final boolean[] thieu = {false};
        final boolean ok = p.truNeuDu(() -> {
            if ((long) p.getLuong() < n) {
                thieu[0] = true;
                return false;
            }
            return viec.getAsBoolean();
        }, () -> {
            if (n > 0L) {
                p.subLuong(n);
            }
        });
        return ok ? XONG : thieu[0] ? THIEU : KHONG_LAM_DUOC;
    }
}
