package com.htbb.pokemon;

/**
 * Qua hoat dong + lich dang nhap (yeu cau 17/10/2026; bat khi sv.lau_dai=1). Lop THUAN.
 *
 *  - Moc phut HOAT DONG trong ngay (khong tinh AFK: chi cong khoang giua hai hanh dong lien
 *    tiep <= 60 giay): 15 / 30 / 60 / 120 phut. Moi moc 1 lan / ngay / TAI KHOAN (chan acc phu).
 *  - Chuoi 7 ngay: luong khoa 3,4,5,6,8,10,15 (ThongSoLauDai.LICH_DANG_NHAP); nghi 1 ngay van giu
 *    chuoi (1 ngay an han), nghi tu 2 ngay lien tiep thi ve ngay 1.
 *  - Lich thang: du 20 ngay dang nhap trong thang -> 1 danh hieu trang tri 30 ngay (1 lan / thang).
 * Ngan sach: moc ngay toi da 5 luong khoa + 3 da khoa + 3 nguyen lieu khoa + 5% gio xu (THIET_KE 5.1
 * "ngay": 4 luong khoa/nhiem vu) - da dua vao MoPhongKinhTe.
 */
public final class QuaHoatDongLogic {

    public static final int[] MOC_PHUT = {15, 30, 60, 120};
    /** Luong khoa moi moc. */
    public static final int[] MOC_LUONG_KHOA = {0, 0, 2, 3};
    /** Da luyen kim khoa moi moc. */
    public static final int[] MOC_DA = {1, 1, 0, 1};
    /** % gio thu nhap xu moi moc. */
    public static final int[] MOC_XU_PHAN_TRAM_GIO = {0, 5, 0, 0};
    /** Nguyen lieu khoa ("hop Poke") moi moc. */
    public static final int[] MOC_NGUYEN_LIEU = {0, 0, 0, 3};
    public static final long KHOANG_TOI_DA_MS = 60000L;
    public static final int NGAY_THANG_CAN = 20;

    private QuaHoatDongLogic() {
    }

    /** Giay hoat dong cong them giua hai hanh dong (0 neu nghi qua 60 giay). */
    public static int congGiay(final long lanTruoc, final long bayGio) {
        if (lanTruoc <= 0L || bayGio <= lanTruoc) {
            return 0;
        }
        final long d = bayGio - lanTruoc;
        return d > KHOANG_TOI_DA_MS ? 0 : (int) (d / 1000L);
    }

    /** Moc (0..3) nhan duoc voi {@code giay} hoat dong va bitmask da nhan; -1 neu khong co. */
    public static int mocNhanDuoc(final int giay, final int daNhan) {
        for (int i = 0; i < MOC_PHUT.length; i++) {
            if ((daNhan & (1 << i)) == 0 && giay >= MOC_PHUT[i] * 60) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Ngay dang nhap moi: @return {buoc moi, luong khoa, chuoi bi reset (0/1)} - da nhan hom
     * nay thi {buoc, 0, 0}. {@code cach} = so ngay tu lan nhan truoc (0 = cung ngay).
     */
    public static int[] chuoi(final int buoc, final int cach) {
        if (cach == 0) {
            return new int[]{buoc, 0, 0};
        }
        final boolean reset = cach > 2;
        final int b = reset ? 0 : buoc;
        final int[] l = ThongSoLauDai.LICH_DANG_NHAP;
        return new int[]{(b + 1) % l.length, l[b % l.length], reset ? 1 : 0};
    }

    /** Chuoi phan thuong (DatabaseGiftCode.applyItemString) cua moc {@code i}, deu KHOA. */
    public static String thuongMoc(final int i, final int idDa, final int idNguyenLieu) {
        final StringBuilder b = new StringBuilder();
        if (MOC_LUONG_KHOA[i] > 0) {
            b.append("LUONGKHOA:").append(MOC_LUONG_KHOA[i]);
        }
        if (MOC_DA[i] > 0) {
            b.append(b.length() > 0 ? "," : "").append("GEM:").append(idDa).append(':').append(MOC_DA[i]).append(":-1");
        }
        if (MOC_NGUYEN_LIEU[i] > 0 && idNguyenLieu >= 0) {
            b.append(b.length() > 0 ? "," : "").append("GEM:").append(idNguyenLieu).append(':').append(MOC_NGUYEN_LIEU[i]).append(":-1");
        }
        return b.toString();
    }
}
