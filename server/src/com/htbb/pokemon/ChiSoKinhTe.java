package com.htbb.pokemon;

import java.util.Arrays;

/**
 * Chi so kinh te hang dem (docs/thiet-ke-game.md) - ghi vao tob_kinh_te_ngay. Lop THUAN:
 * data.DatabaseKinhTe doc tob_char (nguoi hoat dong 7 ngay) roi goi {@link #tinh}.
 */
public final class ChiSoKinhTe {

    public int soNguoi;
    public int capTrungVi;
    /** Chi so gia = xuMoiGio(cap trung vi). */
    public long chiSoGia;
    public long tongXu;
    public long tongLuong;
    public long tongLuongKhoa;
    /** Cung tien thuc (gio thu nhap / nguoi) x100. */
    public long cungTienThucX100;
    /** % xu nam o top 1% nguoi (x100). */
    public int phanTramTop1X100;
    /** Muc: 0 xanh, 1 vang, 2 do (theo cung tien thuc). */
    public int muc;

    private ChiSoKinhTe() {
    }

    /**
     * @param cap   cap tung nguoi hoat dong
     * @param xu    xu (tui + kho) tung nguoi
     * @param luong luong (tui + kho) tung nguoi
     * @param lk    luong khoa (tui + kho) tung nguoi
     */
    public static ChiSoKinhTe tinh(final int[] cap, final long[] xu, final long[] luong, final long[] lk) {
        final ChiSoKinhTe c = new ChiSoKinhTe();
        c.soNguoi = cap.length;
        if (cap.length == 0) {
            c.chiSoGia = LauDai.xuMoiGio(1);
            return c;
        }
        final int[] s = cap.clone();
        Arrays.sort(s);
        c.capTrungVi = s[s.length / 2];
        c.chiSoGia = LauDai.xuMoiGio(c.capTrungVi);
        final long[] x = xu.clone();
        Arrays.sort(x);
        for (int i = 0; i < cap.length; i++) {
            c.tongXu += Math.max(0L, xu[i]);
            c.tongLuong += Math.max(0L, luong[i]);
            c.tongLuongKhoa += Math.max(0L, lk[i]);
        }
        c.cungTienThucX100 = c.chiSoGia <= 0L ? 0L : c.tongXu * 100L / c.soNguoi / c.chiSoGia;
        final int top = Math.max(1, (int) Math.ceil(x.length / 100.0));
        long xuTop = 0L;
        for (int i = x.length - top; i < x.length; i++) {
            xuTop += Math.max(0L, x[i]);
        }
        c.phanTramTop1X100 = c.tongXu <= 0L ? 0 : (int) (xuTop * 10000L / c.tongXu);
        c.muc = c.cungTienThucX100 <= 1000L ? 0 : (c.cungTienThucX100 <= 2000L ? 1 : 2);
        return c;
    }
}
