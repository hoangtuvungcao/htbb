package com.htbb.pokemon;

/**
 * Nang luong huan luyen (THIET_KE_LAU_DAI.md muc 1.3). Lop THUAN.
 *
 *   180 phut dau (+ tich luy tu hom truoc, toi da them 180) -> 100%
 *   180 phut tiep -> 50%, 240 phut tiep -> 20%, sau do 0% (EXP, xu, da tu quai).
 *
 * "Thoi gian danh quai" = tong cac khoang giua hai lan ha quai lien tiep, moi khoang
 * toi da {@link #KHOANG_TOI_DA_MS} (dung yen / di cho / AFK khong bi tinh).
 */
public final class NangLuong {

    /** Khoang lon nhat giua hai lan ha quai van coi la "dang danh lien tuc". */
    public static final long KHOANG_TOI_DA_MS = 60000L;

    private NangLuong() {
    }

    /** yyyyMMdd cua thoi diem (gio may). Doi ngay luc 00:00 nhu CharCapNhat. */
    public static int ngay(final long ms) {
        final java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(ms);
        return c.get(java.util.Calendar.YEAR) * 10000 + (c.get(java.util.Calendar.MONTH) + 1) * 100
                + c.get(java.util.Calendar.DAY_OF_MONTH);
    }

    /** So ngay giua hai yyyyMMdd (b - a), >= 0. */
    public static int cachNgay(final int a, final int b) {
        if (a <= 0 || b <= 0) {
            return 0;
        }
        final java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.clear();
        ca.set(a / 10000, (a / 100) % 100 - 1, a % 100, 12, 0, 0);
        final java.util.Calendar cb = java.util.Calendar.getInstance();
        cb.clear();
        cb.set(b / 10000, (b / 100) % 100 - 1, b % 100, 12, 0, 0);
        return (int) Math.max(0L, Math.round((cb.getTimeInMillis() - ca.getTimeInMillis()) / 86400000.0));
    }

    /**
     * Phut 100% cua ngay moi: 180 + phan 100% chua dung (toi da 180). Nghi tu 2 ngay tro
     * len thi tich du 180 (tong 6 gio) - khong ep dang nhap moi ngay.
     */
    public static int phut100NgayMoi(final int phut100Cu, final long msDungCu, final int soNgayCach,
            final int phutNgay, final int phutTichToiDa) {
        final int tichToiDa = Math.max(0, phutTichToiDa - phutNgay);
        if (soNgayCach <= 0) {
            return phut100Cu;
        }
        final int du;
        if (soNgayCach >= 2) {
            du = tichToiDa;
        } else {
            du = (int) Math.max(0L, phut100Cu - msDungCu / 60000L);
        }
        return phutNgay + Math.min(tichToiDa, du);
    }

    /** He so (%) khi da dung {@code msDung} ms trong ngay co {@code phut100} phut 100%. */
    public static int heSo(final long msDung, final int phut100) {
        final long phut = msDung / 60000L;
        if (phut < phut100) {
            return 100;
        }
        return ThongSoLauDai.heSoMet((int) Math.min(Integer.MAX_VALUE, phut - phut100));
    }

    /**
     * Ghi nhan mot lan ha quai luc {@code bayGio} va tra ve he so (%) ap cho lan do.
     * Doi ngay tu dong (theo gio may).
     */
    public static int ghiNhan(final HoSoLauDai h, final long bayGio, final int phutNgay, final int phutTichToiDa) {
        synchronized (h) {
            final int hn = ngay(bayGio);
            if (h.nlNgay != hn) {
                final int cach = h.nlNgay <= 0 ? 2 : cachNgay(h.nlNgay, hn);
                h.nlPhut100 = h.nlNgay <= 0 ? phutNgay : phut100NgayMoi(h.nlPhut100, h.nlMsDung, cach, phutNgay, phutTichToiDa);
                h.nlNgay = hn;
                h.nlMsDung = 0L;
                h.nlLanCuoi = 0L;
            }
            if (h.nlLanCuoi > 0L && bayGio > h.nlLanCuoi) {
                h.nlMsDung += Math.min(KHOANG_TOI_DA_MS, bayGio - h.nlLanCuoi);
            }
            h.nlLanCuoi = bayGio;
            h.ban = true;
            return heSo(h.nlMsDung, h.nlPhut100);
        }
    }

    /** He so hien tai (khong ghi nhan) - de hien cho nguoi choi. */
    public static int heSoHienTai(final HoSoLauDai h, final long bayGio) {
        synchronized (h) {
            if (h.nlNgay != ngay(bayGio)) {
                return 100;
            }
            return heSo(h.nlMsDung, h.nlPhut100);
        }
    }
}
