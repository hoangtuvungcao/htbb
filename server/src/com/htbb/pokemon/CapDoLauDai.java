package com.htbb.pokemon;

/**
 * EXP va cap theo thiet ke dai han (THIET_KE_LAU_DAI.md muc 1). Lop THUAN, khong cham
 * real.* - real.LevelDetail / real.ExpLauDai goi vao day khi {@link LauDai#bat()}.
 *
 * KHONG nham voi real.LevelDetail.expMain (bang DANG chay khi tat co).
 */
public final class CapDoLauDai {

    private CapDoLauDai() {
    }

    /**
     * Cap cao nhat bang moi dung that trong nam 1. Bang thiet ke co toi 100 nhung
     * LevelDetail.allSkillPointLV chi co 90 o (getBasicPoint(lv) doc [lv-1]) va cot
     * tob_char.lastLv / client doc cap 1 byte: mo 92..100 la viec nam 2 (GD2 buoc 3.8).
     */
    public static final int CAP_TOI_DA_NAM_1 = 91;

    /**
     * So cap cua bang EXP moi nap vao LevelDetail (GD2b: mo 92..100 cho nam 2). Tran theo mua
     * (91 tu ngay 240, 95 tu ngay 365, 100 tu ngay 545) chan viec len cap that.
     */
    public static final int CAP_BANG = ThongSoLauDai.CAP_TOI_DA;

    /**
     * expMain moi: [i] = EXP tich luy de DAT cap i+1 (cung y nghia LevelDetail.expMain).
     * Do dai {@code soCap}: [0] = 0 (cap 1), [soCap-1] = EXP dat cap soCap.
     */
    public static long[] expMain(final int soCap) {
        final long[] m = new long[soCap];
        long tong = 0L;
        for (int i = 1; i < soCap; i++) {
            tong += ThongSoLauDai.EXP_LEN_CAP[i];
            m[i] = tong;
        }
        return m;
    }

    /** expRequire moi: [L] = EXP tu L len L+1 (cung y nghia LevelDetail.expRequire), do dai soCap + 1. */
    public static long[] expRequire(final int soCap) {
        final long[] r = new long[soCap + 1];
        for (int L = 1; L <= soCap && L < ThongSoLauDai.EXP_LEN_CAP.length; L++) {
            r[L] = ThongSoLauDai.EXP_LEN_CAP[L];
        }
        // O cuoi (cap toi da) phai > 0: LevelDetail chia cho expRequire[lv] khi tinh %.
        if (r[soCap] <= 0L) {
            r[soCap] = r[soCap - 1] > 0L ? r[soCap - 1] : 1L;
        }
        return r;
    }

    /** Cap tu tong EXP theo bang {@code main} (giong LevelDetail.getLevelFromExp). */
    public static int capTuExp(final long exp, final long[] main) {
        int lv = 0;
        for (int i = 0; i < main.length && exp >= main[i]; ++i) {
            ++lv;
        }
        return Math.max(1, lv);
    }

    /**
     * CHUYEN DOI nhan vat cu sang bang moi (mot lan, xem docs/pokemon/KE_HOACH_CHUYEN_DOI.md
     * muc "Giai doan 2"): GIU CAP, giu ti le % trong cap (ti le tuyen tinh).
     *   exp moi = mainMoi[L-1] + (exp cu - mainCu[L-1]) / reqCu[L] * reqMoi[L]
     * Cap cu vuot cap cao nhat cua bang moi thi ve cap cao nhat (0% trong cap).
     */
    public static long chuyenExp(final long expCu, final long[] mainCu, final long[] reqCu,
            final long[] mainMoi, final long[] reqMoi) {
        if (expCu <= 0L) {
            return 0L;
        }
        int L = capTuExp(expCu, mainCu);
        if (L > mainMoi.length) {
            return mainMoi[mainMoi.length - 1];
        }
        final long trongCap = expCu - mainCu[L - 1];
        long phanNghin = 0L;
        if (L < reqCu.length && reqCu[L] > 0L && L < mainCu.length) {
            phanNghin = Math.max(0L, Math.min(999L, trongCap * 1000L / reqCu[L]));
        }
        final long req = L < reqMoi.length ? reqMoi[L] : 0L;
        long moi = mainMoi[L - 1] + req * phanNghin / 1000L;
        // khong duoc lot len cap sau do lam tron
        if (L < mainMoi.length && moi >= mainMoi[L]) {
            moi = mainMoi[L] - 1L;
        }
        return moi;
    }

    /**
     * Cat EXP theo tran cap. @return {exp moi, exp thua} - exp thua (vuot tran) chuyen
     * thanh tinh thong. Nhan vat DA tren tran (vd tran 60 nhung nhan vat cu cap 85) thi
     * giu nguyen exp, moi exp them deu thua.
     */
    public static long[] catTheoTran(final long expHienTai, final long them, final int tran, final long[] main) {
        if (them <= 0L) {
            return new long[]{expHienTai, 0L};
        }
        if (tran >= main.length) {
            final long tran2 = main[main.length - 1];
            final long moi = Math.min(tran2, expHienTai + them);
            return new long[]{Math.max(moi, expHienTai), Math.max(0L, expHienTai + them - tran2)};
        }
        final long tranExp = main[tran] - 1L; // EXP lon nhat ma van o cap {tran}
        if (expHienTai >= tranExp) {
            return new long[]{expHienTai, them};
        }
        final long moi = expHienTai + them;
        if (moi <= tranExp) {
            return new long[]{moi, 0L};
        }
        return new long[]{tranExp, moi - tranExp};
    }

    /** Tran cap hien tai (theo ngay tu khai truong), kep vao bang dang dung. */
    public static int tranCap(final int ngay, final int soCapBang) {
        return Math.min(soCapBang, ThongSoLauDai.tranCap(ngay));
    }

    // ------------------------------------------------------------------ tinh thong (1.6)

    /** EXP cho 1 diem tinh thong o tran {@code tran}: 50% EXP len cap (tran - 1). */
    public static long expMoiDiemTinhThong(final int tran) {
        final int L = Math.max(1, Math.min(ThongSoLauDai.EXP_LEN_CAP.length - 2, tran - 1));
        return Math.max(1L, ThongSoLauDai.EXP_LEN_CAP[L] * ThongSoLauDai.TINH_THONG_PHAN_TRAM_CAP / 100L);
    }

    /** Diem tinh thong toi da da mo o mua {@code mua} (0 = S1): 10/mua, tran 50. */
    public static int tinhThongToiDa(final int mua) {
        return Math.min(ThongSoLauDai.TINH_THONG_TOI_DA, ThongSoLauDai.TINH_THONG_DIEM_MOI_MUA * (mua + 1));
    }

    /**
     * Cong exp thua vao quy tinh thong. @return {diem moi, quy con lai}. Da du diem toi
     * da cua mua thi quy khong tang (khong tich vo han de doi mua sau).
     */
    public static long[] congTinhThong(final int diem, final long quy, final long thua, final int tran, final int mua) {
        final int toiDa = tinhThongToiDa(mua);
        if (diem >= toiDa) {
            return new long[]{diem, 0L};
        }
        final long buoc = expMoiDiemTinhThong(tran);
        long q = quy + Math.max(0L, thua);
        int d = diem;
        while (q >= buoc && d < toiDa) {
            q -= buoc;
            d++;
        }
        if (d >= toiDa) {
            q = 0L;
        }
        return new long[]{d, q};
    }

    /** % cong (phan nghin) tan cong hoac HP tu diem tinh thong: 0,2%/diem, toi da 10%. */
    public static int tinhThongPhanNghin(final int diem) {
        return Math.min(100, Math.max(0, diem) * ThongSoLauDai.TINH_THONG_PHAN_NGHIN_MOI_DIEM);
    }

    // ------------------------------------------------------------------ EXP ha quai (1.2)

    /**
     * He so lech cap (%) - CHUNG cho EXP, xu, da va do (muc 1.2 / 2.3).
     * {@code delta} = cap nguoi - cap quai.
     *   nguoi cao hon 0-3 -> 100, 4-6 -> 80, 7-9 -> 50, 10-14 -> 20, >= 15 -> 5
     *   quai cao hon: +5%/cap toi +25% (<= 5 cap), 6-8 cap -> 125, quai cao hon > 8 -> 0
     */
    public static int heSoLechCap(final int delta) {
        if (delta >= 0) {
            if (delta <= 3) {
                return 100;
            }
            if (delta <= 6) {
                return 80;
            }
            if (delta <= 9) {
                return 50;
            }
            if (delta <= 14) {
                return 20;
            }
            return 5;
        }
        final int cao = -delta;
        if (cao > 8) {
            return 0;
        }
        return 100 + 5 * Math.min(5, cao);
    }

    /** Thuong to doi (%): 100 x (1 + 10% x (so nguoi - 1)), toi da +40% khi du 5 nguoi. */
    public static int heSoToDoi(final int soNguoiGan) {
        final int n = Math.max(1, Math.min(5, soNguoiGan));
        return 100 + 10 * (n - 1);
    }

    /** Buff % cong don, tran +100%. */
    public static int kepBuff(final int buffPhanTram) {
        return Math.max(0, Math.min(100, buffPhanTram));
    }

    /** Bat kip (%): cap < trung vi - 5 va (vao sau ngay 30 hoac vang >= 14 ngay) -> 150, khac 100. */
    public static int heSoBatKip(final int cap, final int capTrungVi, final boolean duDieuKien) {
        if (!duDieuKien || capTrungVi <= 0) {
            return 100;
        }
        return cap < capTrungVi - ThongSoLauDai.KHOANG_BAT_KIP ? ThongSoLauDai.HE_SO_BAT_KIP : 100;
    }

    /**
     * EXP 1 lan ha quai cho 1 nguoi: raw x lech cap x (1 + buff) x nang luong x bat kip x to doi.
     * Cac he so la %. Tran Integer.MAX.
     */
    public static int expHaQuai(final long raw, final int heLech, final int buff, final int nangLuong,
            final int batKip, final int toDoi) {
        if (raw <= 0L || heLech <= 0 || nangLuong <= 0) {
            return 0;
        }
        double x = raw;
        x = x * heLech / 100.0;
        x = x * (100 + kepBuff(buff)) / 100.0;
        x = x * nangLuong / 100.0;
        x = x * batKip / 100.0;
        x = x * toDoi / 100.0;
        return (int) Math.max(1L, Math.min((long) Integer.MAX_VALUE, Math.round(x)));
    }
}
