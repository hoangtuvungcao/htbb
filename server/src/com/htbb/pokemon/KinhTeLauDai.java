package com.htbb.pokemon;

/**
 * Gia, phi, thue va tien roi theo thiet ke dai han (THIET_KE_LAU_DAI.md muc 2). Lop THUAN.
 * Moi so doc qua {@link LauDai#so} nen chinh duoc bang bang cau_hinh khong can build.
 */
public final class KinhTeLauDai {

    private KinhTeLauDai() {
    }

    // ------------------------------------------------------------------ roi tu quai (2.2/2.3)

    /** Ti le (phan trieu, so voi Map.randomMillion) roi xu: mac dinh 30%. */
    public static int tiLeRoiXuPhanTrieu() {
        return Math.max(0, Math.min(100, LauDai.so(LauDai.K_ROI_XU, ThongSoLauDai.TI_LE_ROI_XU))) * 10000;
    }

    /**
     * So xu mot dong roi: XU_MOI_QUAI[cap quai] / 30% x lech cap x nang luong (deu %).
     * Ky vong moi quai = XU_MOI_QUAI x lech cap x nang luong.
     */
    public static int xuMotLanRoi(final int capQuai, final int heLech, final int nangLuong) {
        final int tiLe = Math.max(1, LauDai.so(LauDai.K_ROI_XU, ThongSoLauDai.TI_LE_ROI_XU));
        final long goc = (long) ThongSoLauDai.XU_MOI_QUAI[LauDai.kepCap(capQuai)] * 100L / tiLe;
        final long x = goc * heLech / 100L * nangLuong / 100L;
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, x));
    }

    /** Ti le roi da luyen kim (phan trieu): 1,5% khi |lech cap| <= 5, khac 0. */
    public static int tiLeRoiDaPhanTrieu(final int delta, final int nangLuong) {
        if (Math.abs(delta) > 5) {
            return 0;
        }
        return LauDai.so(LauDai.K_ROI_DA, ThongSoLauDai.DA_ROI_PHAN_NGHIN) * 1000 * nangLuong / 100;
    }

    // ------------------------------------------------------------------ thue, phi (2.4/2.7)

    /** Thue cho ky gui (dot): 6%. */
    public static long thueCho(final long gia) {
        return Math.max(0L, gia) * LauDai.so(LauDai.K_THUE_CHO, ThongSoLauDai.THUE_CHO_PHAN_TRAM) / 100L;
    }

    /** Phi dang ban (dot, khong hoan): 1%, toi thieu 1 khi gia > 0. */
    public static long phiDangBan(final long gia) {
        if (gia <= 0L) {
            return 0L;
        }
        return Math.max(1L, gia * LauDai.so(LauDai.K_PHI_DANG_BAN, ThongSoLauDai.PHI_DANG_BAN_PHAN_TRAM) / 100L);
    }

    /** Phi chuyen luong qua NPC: 5% (lam tron len, toi thieu 1 khi chuyen > 0). */
    public static long phiChuyenLuong(final long soLuong) {
        if (soLuong <= 0L) {
            return 0L;
        }
        final int pc = LauDai.so(LauDai.K_THUE_CHUYEN_LUONG, 5);
        return Math.max(pc > 0 ? 1L : 0L, (soLuong * pc + 99L) / 100L);
    }

    /** Tran chuyen xu truc tiep / ngay: 5 gio thu nhap o cap nguoi gui. */
    public static long tranChuyenXuNgay(final int cap) {
        return LauDai.xuMoiGio(cap) * LauDai.so(LauDai.K_TRAN_CHUYEN_XU, ThongSoLauDai.TRAN_GIAO_DICH_TRUC_TIEP_GIO);
    }

    /** Dich chuyen: 3% thu nhap 1 gio o cap nguoi dung. */
    public static long phiDichChuyen(final int cap) {
        return LauDai.phanTramGio(cap, LauDai.so(LauDai.K_DICH_CHUYEN, ThongSoLauDai.DICH_CHUYEN_PHAN_TRAM_GIO));
    }

    /** Hoi sinh: 5% thu nhap 1 gio. */
    public static long phiHoiSinh(final int cap) {
        return LauDai.phanTramGio(cap, LauDai.so(LauDai.K_HOI_SINH, 5));
    }

    /**
     * Sua do: giu cong thuc cu (tong price/10) nhung chan tren boi 8% thu nhap 1 gio x so
     * mon (de "price" cu cua do cao cap khong lam phi sua vo ly). {@code giaCu} = phi cu.
     */
    public static long phiSuaDo(final long giaCu, final int cap, final int soMon) {
        final long tran = LauDai.phanTramGio(cap, LauDai.so(LauDai.K_SUA_DO, ThongSoLauDai.SUA_DO_PHAN_TRAM)) * Math.max(1, soMon);
        return Math.max(0L, Math.min(giaCu, tran));
    }

    // ------------------------------------------------------------------ ban / phan ra (2.9)

    /** Ban do cho NPC: 5% gia (khong cong theo plus). */
    public static long giaBanNpc(final long price) {
        return Math.max(0L, price) * LauDai.so(LauDai.K_BAN_NPC, 5) / 100L;
    }

    /** Phan ra: {so da, xu} = {2, 10% gia}. */
    public static long[] phanRa(final long price) {
        return new long[]{ThongSoLauDai.DA_TU_PHAN_RA, Math.max(0L, price) / 10L};
    }

    // ------------------------------------------------------------------ ngoc he (2.13 / 3.3)

    /** Gia khảm ngọc hệ len cap {@code capNgoc} (1..10): 1 gio thu nhap x 1,45^(cap-1). */
    public static long giaNgocHe(final int capNguoi, final int capNgoc) {
        final int c = Math.max(1, Math.min(ThongSoLauDai.NGOC_HE_CAP_TOI_DA, capNgoc));
        double gia = LauDai.xuMoiGio(capNguoi) * ThongSoLauDai.NGOC_HE_GIA_GIO_PHAN_TRAM / 100.0;
        for (int i = 1; i < c; i++) {
            gia = gia * ThongSoLauDai.NGOC_HE_TANG_PHAN_TRAM / 100.0;
        }
        return Math.max(1L, Math.round(gia));
    }

    // ------------------------------------------------------------------ luong xep hang (2.12)

    /** Luong thuong theo hang tuan (1-based); ngoai top 1000 = 0. */
    public static int luongXepHangTuan(final int hang) {
        if (hang <= 0) {
            return 0;
        }
        for (final int[] b : ThongSoLauDai.LUONG_XEP_HANG_TUAN) {
            if (hang <= b[0]) {
                return b[1];
            }
        }
        return 0;
    }

    /** Lich dang nhap 7 ngay (luong khoa) cho buoc 0..6, lap lai. */
    public static int luongKhoaDangNhap(final int buoc) {
        final int[] l = ThongSoLauDai.LICH_DANG_NHAP;
        return l[((buoc % l.length) + l.length) % l.length];
    }
}
