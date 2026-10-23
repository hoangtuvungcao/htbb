package com.htbb.pokemon;

/**
 * O THU (thu cuoi + Pokemon di theo) - ham THUAN, khong cham Char / CSDL (de test). Lop noi vao game: real.QuanLyThuGame.
 *
 * <p>Thu cuoi (Char.animal) va Pokemon di theo (Char.pet) la HAI vector o may chu nhung client hien CHUNG mot danh sach
 * "Thu" (tab Thu va Trang bi &gt; Thu cuoi, cung goi 16 loai 2), nen dung MOT suc chua chung: {@link #sucChua} = o co ban
 * + o da mo bang ve, khong vuot o toi da. Moi con dang so huu deu tinh (ca con dang cuoi / dang di theo).
 *
 * <p>Day o: KHONG nhan them thu (bat ky duong nao - mua, no trung, su kien, tang). Nhan vat cu da vuot o giu nguyen
 * (khong xoa), chi khong nhan them cho toi khi tha bot xuong duoi suc chua.
 *
 * <p>Tha ve tu nhien: tra mot it xu ({@link #xuTha}) - ho hut an toan: chi thu VINH VIEN moi co xu (thu co han = 0 xu,
 * chan cay thu su kien mien phi ra xu), muc rat nho (mac dinh 20 + 2 / cap, nhan bac hiem, tran 300 xu / con; gio thu
 * nhap cap 20 ~ 9.000 xu) - cung tinh than luat ban NPC 1/4 gia cho do khoa.
 */
public final class OThu {

    /** Khoa cau hinh (cau_hinh, nhom kinh_te). */
    public static final String K_O_CO_BAN = "thu.o_co_ban";
    public static final String K_O_TOI_DA = "thu.o_toi_da";
    public static final String K_THA_XU_CO_BAN = "thu.tha_xu_co_ban";
    public static final String K_THA_XU_MOI_CAP = "thu.tha_xu_moi_cap";
    public static final String K_THA_XU_TOI_DA = "thu.tha_xu_toi_da";
    /** Tran xu tha / nhan vat / ngay lich (qua tran: van tha duoc, 0 xu). */
    public static final String K_THA_XU_NGAY = "thu.tha_xu_ngay";
    public static final int THA_XU_NGAY = 2000;

    public static final int O_CO_BAN = 20;
    public static final int O_TOI_DA = 60;
    /** Tran cung cua o toi da: goi 16 ghi so dong bang byte (toi da 127 moi loai). */
    public static final int O_TOI_DA_CUNG = 120;
    public static final int THA_XU_CO_BAN = 20;
    public static final int THA_XU_MOI_CAP = 2;
    public static final int THA_XU_TOI_DA = 300;
    /** So o moi ve. */
    public static final int O_MOI_VE = 1;
    /** Cap / bac hiem tinh xu toi da (chong so lieu la). */
    public static final int CAP_TINH_TOI_DA = 100;
    public static final int HIEM_TOI_DA = 5;

    private OThu() {
    }

    /** Kep o co ban / toi da tu cau hinh: 1 &lt;= coBan &lt;= toiDa &lt;= O_TOI_DA_CUNG. */
    public static int coBan(final int coBan) {
        return Math.max(1, Math.min(O_TOI_DA_CUNG, coBan));
    }

    public static int toiDa(final int coBan, final int toiDa) {
        return Math.max(coBan(coBan), Math.min(O_TOI_DA_CUNG, toiDa));
    }

    /** Suc chua: o co ban + o da mo, khong vuot o toi da. */
    public static int sucChua(final int coBan, final int toiDa, final int oMo) {
        return Math.min(toiDa(coBan, toiDa), coBan(coBan) + Math.max(0, oMo));
    }

    /** So o con mo duoc bang ve (0 = da toi da, ve bi tu choi KHONG tru). */
    public static int oConMoDuoc(final int coBan, final int toiDa, final int oMo) {
        return Math.max(0, toiDa(coBan, toiDa) - sucChua(coBan, toiDa, oMo));
    }

    /** Kep o da mo doc tu CSDL. */
    public static int kepOMo(final int oMo) {
        return Math.max(0, Math.min(O_TOI_DA_CUNG, oMo));
    }

    /** Day o: khong nhan them (ke ca khi da vuot - nhan vat cu). */
    public static boolean day(final int soThu, final int sucChua) {
        return soThu >= sucChua;
    }

    /**
     * Xu khi tha mot con: thu co han -&gt; 0; con lai (coBan + moiCap x cap) x bac hiem, tran {@code toiDa}.
     * cap kep [1, 100], hiem kep [1, 5].
     */
    public static long xuTha(final int cap, final int hiem, final boolean coHan, final int coBan, final int moiCap,
            final int toiDa) {
        if (coHan) {
            return 0L;
        }
        final long c = Math.max(1, Math.min(CAP_TINH_TOI_DA, cap));
        final long h = Math.max(1, Math.min(HIEM_TOI_DA, hiem));
        final long x = (Math.max(0, coBan) + Math.max(0, moiCap) * c) * h;
        return Math.max(0L, Math.min(Math.max(0, toiDa), x));
    }

    /**
     * Ban trung: moi loai giu MOT con cap cao nhat (bang cap: uu tien con khong tha duoc - dang dung / khoa - roi con
     * dung truoc), con lai la ung vien neu tha duoc. Tra ve mang danh dau ung vien.
     *
     * @param loai     khoa loai (cung loai = cung so)
     * @param cap      cap tung con
     * @param thaDuoc  con do tha duoc khong (khong dang dung / khoa / giao dich)
     */
    public static boolean[] banTrung(final int[] loai, final int[] cap, final boolean[] thaDuoc) {
        final int n = loai.length;
        final boolean[] ra = new boolean[n];
        final boolean[] xet = new boolean[n];
        for (int i = 0; i < n; i++) {
            if (xet[i]) {
                continue;
            }
            int giu = i;
            for (int j = i; j < n; j++) {
                if (loai[j] != loai[i]) {
                    continue;
                }
                xet[j] = true;
                if (cap[j] > cap[giu] || (cap[j] == cap[giu] && !thaDuoc[j] && thaDuoc[giu])) {
                    giu = j;
                }
            }
            for (int j = i; j < n; j++) {
                if (loai[j] == loai[i] && j != giu && thaDuoc[j]) {
                    ra[j] = true;
                }
            }
        }
        return ra;
    }

    /** Xu con nhan duoc hom nay: tran - da nhan (ngay khac = 0 da nhan). */
    public static long xuConNgay(final int tranNgay, final int daNhan, final int ngayDaNhan, final int homNay) {
        return Math.max(0L, (long) Math.max(0, tranNgay) - (ngayDaNhan == homNay ? Math.max(0, daNhan) : 0));
    }

    /** Ve mua them con dung duoc khong: o da mo + ve dang giu &lt; so o con thieu toi toi da. */
    public static boolean muaVeDuoc(final int coBan, final int toiDa, final int oMo, final int soVe) {
        return Math.max(0, soVe) * O_MOI_VE < oConMoDuoc(coBan, toiDa, oMo);
    }
}
