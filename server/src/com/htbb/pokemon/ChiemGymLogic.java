package com.htbb.pokemon;

/**
 * Thuong chiem thanh tuan (yeu cau 17/10/2026; bat khi sv.lau_dai=1). Lop THUAN, so lieu nam trong
 * ngan sach THIET_KE 2.1 / 6 ("the chien vung" 1 gio thu nhap / tuan):
 *   - bang thang: danh hieu trang tri 7 ngay (moi thanh vien luc chiem), +10% EXP 7 ngay (trong
 *     tran buff +100%), chia 20% QUY BANG (tien da thu tu thue cho - chia lai, khong in them) cho
 *     thanh vien CO dong gop, moi nguoi toi da 1 gio thu nhap;
 *   - luong khoa theo dong gop (chong AFK: phai >= 1% dong gop cao nhat va >= DONG_GOP_TOI_THIEU):
 *     bang thang 20, bang khac 5, top 10 toan tran +5. Tran 25 / nguoi / tuan.
 */
public final class ChiemGymLogic {

    public static final int LK_THANG = 20;
    public static final int LK_THAM_GIA = 5;
    public static final int LK_TOP10 = 5;
    public static final int LK_TRAN = 25;
    public static final int PHAN_TRAM_QUY = 20;
    public static final int BUFF_EXP = 10;
    public static final long BUFF_MS = 7L * 86400000L;
    public static final long DONG_GOP_TOI_THIEU = 1000L;
    public static final int DANH_HIEU_PHUT = 7 * 24 * 60;

    private ChiemGymLogic() {
    }

    /** Du dieu kien nhan thuong dong gop (chong AFK). */
    public static boolean duDongGop(final long diem, final long cao) {
        return diem >= DONG_GOP_TOI_THIEU && diem * 100L >= cao;
    }

    /** Luong khoa cho mot nguoi. */
    public static int luongKhoa(final boolean bangThang, final boolean duDongGop, final int hang) {
        if (!duDongGop) {
            return 0;
        }
        int lk = bangThang ? LK_THANG : LK_THAM_GIA;
        if (hang >= 1 && hang <= 10) {
            lk += LK_TOP10;
        }
        return Math.min(LK_TRAN, lk);
    }

    /** Phan quy bang moi nguoi: 20% quy / so nguoi du, toi da 1 gio thu nhap o cap nguoi do. */
    public static long phanQuy(final long quy, final int soNguoi, final long tranMotGio) {
        if (quy <= 0L || soNguoi <= 0) {
            return 0L;
        }
        return Math.max(0L, Math.min(quy * PHAN_TRAM_QUY / 100L / soNguoi, tranMotGio));
    }
}
