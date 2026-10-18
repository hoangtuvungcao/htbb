package com.htbb.pokemon;

/**
 * Trang thai (status) do server tinh, client chi ve (docs/thiet-ke-game.md). Lop THUAN.
 *
 * | Trang thai | Hieu ung                                  | Thoi gian | Khang                        |
 * | 1 Bong     | 3% HP toi da / 2 giay, -10% tan cong      | 6 s       | he Lua mien                  |
 * | 2 Doc      | 2% -> 4% -> 6% HP / 2 giay (chong 3 lan)   | 8 s       | he Doc / Thep mien           |
 * | 3 Te liet  | -30% toc do, 15% mat luot                  | 5 s       | he Dien mien                 |
 * | 4 Ngu      | khong hanh dong, tinh khi trung don       | 3 s       | sau do mien ngu 10 s         |
 * | 5 Dong bang| khong hanh dong                           | 2 s       | he Bang mien; sau do mien 10s|
 * PvP: thoi luong x0,5. Boss: 30% thoi luong, khong ngu va khong dong bang.
 */
public final class TrangThaiPokemon {

    public static final int KHONG = 0;
    public static final int BONG = 1;
    public static final int DOC = 2;
    public static final int TE = 3;
    public static final int NGU = 4;
    public static final int BANG = 5;

    public static final int[] THOI_GIAN_MS = {0, 6000, 8000, 5000, 3000, 2000};
    public static final int NHIP_MS = 2000;
    public static final int MIEN_SAU_MS = 10000;
    /** % kich hoat khi chieu cung he trung don (khong ap cho don thuong). */
    public static final int TI_LE_KICH_HOAT = 10;

    /** Trang thai dang co tren mot actor. */
    public int loai;
    public int chong;
    public long het;
    /** Mien ngu/bang toi luc nay. */
    public long mienToi;
    public long nhipTiepTheo;

    private TrangThaiPokemon() {
    }

    public static TrangThaiPokemon moi() {
        return new TrangThaiPokemon();
    }

    /** Trang thai ma chieu he {@code heChieu} gay ra (0 neu he do khong gay trang thai). */
    public static int tuHe(final int heChieu) {
        switch (heChieu) {
            case HePokemon.LUA:
                return BONG;
            case HePokemon.DOC:
                return DOC;
            case HePokemon.DIEN:
                return TE;
            case HePokemon.SIEU_LINH:
            case HePokemon.CO:
                return NGU;
            case HePokemon.BANG:
                return BANG;
            default:
                return KHONG;
        }
    }

    /** Muc tieu (he1, he2) co mien trang thai {@code tt} khong. */
    public static boolean mien(final int tt, final int he1, final int he2) {
        switch (tt) {
            case BONG:
                return co(HePokemon.LUA, he1, he2);
            case DOC:
                return co(HePokemon.DOC, he1, he2) || co(HePokemon.THEP, he1, he2);
            case TE:
                return co(HePokemon.DIEN, he1, he2);
            case BANG:
                return co(HePokemon.BANG, he1, he2);
            default:
                return false;
        }
    }

    private static boolean co(final int he, final int he1, final int he2) {
        return he1 == he || he2 == he;
    }

    /** Thoi luong (ms) theo bo canh: PvP x0,5; boss 30%, boss khong ngu/bang (0). */
    public static int thoiGian(final int tt, final boolean pvp, final boolean boss) {
        if (tt <= 0 || tt >= THOI_GIAN_MS.length) {
            return 0;
        }
        if (boss && (tt == NGU || tt == BANG)) {
            return 0;
        }
        int ms = THOI_GIAN_MS[tt];
        if (pvp) {
            ms /= 2;
        }
        if (boss) {
            ms = ms * 30 / 100;
        }
        return ms;
    }

    /**
     * Ap trang thai {@code tt} luc {@code bayGio}. @return true neu ap duoc (khong mien,
     * khong dang trong thoi gian mien ngu/bang). Doc cong don toi 3; cung loai khac thi lam
     * moi thoi gian.
     */
    public boolean ap(final int tt, final int ms, final long bayGio) {
        if (tt <= 0 || ms <= 0) {
            return false;
        }
        if ((tt == NGU || tt == BANG) && bayGio < mienToi) {
            return false;
        }
        if (loai == tt && het > bayGio) {
            if (tt == DOC) {
                chong = Math.min(3, chong + 1);
            }
        } else {
            loai = tt;
            chong = 1;
            nhipTiepTheo = bayGio + NHIP_MS;
        }
        het = bayGio + ms;
        return true;
    }

    public boolean dangCo(final long bayGio) {
        return loai != KHONG && bayGio < het;
    }

    /** Khong hanh dong duoc (ngu / dong bang). */
    public boolean khoa(final long bayGio) {
        return dangCo(bayGio) && (loai == NGU || loai == BANG);
    }

    /** Trung don khi dang ngu thi tinh day. */
    public void trungDon(final long bayGio) {
        if (dangCo(bayGio) && loai == NGU) {
            go(bayGio);
        }
    }

    /** Go trang thai (het han hoac tinh day). Ngu/bang de lai mien 10 giay. */
    public void go(final long bayGio) {
        if (loai == NGU || loai == BANG) {
            mienToi = bayGio + MIEN_SAU_MS;
        }
        loai = KHONG;
        chong = 0;
        het = 0L;
    }

    /**
     * Sat thuong nhip (bong/doc) neu da toi nhip. Goi moi vong cap nhat.
     * @return HP mat o nhip nay (0 neu chua toi nhip hoac khong co DoT)
     */
    public int nhip(final long bayGio, final int hpToiDa) {
        if (!dangCo(bayGio)) {
            return 0;
        }
        if (bayGio < nhipTiepTheo) {
            return 0;
        }
        nhipTiepTheo = bayGio + NHIP_MS;
        if (loai == BONG) {
            return Math.max(1, hpToiDa * 3 / 100);
        }
        if (loai == DOC) {
            return Math.max(1, hpToiDa * 2 * Math.max(1, chong) / 100);
        }
        return 0;
    }

    /** % tan cong con lai (bong -10%). */
    public int tanCongPhanTram(final long bayGio) {
        return dangCo(bayGio) && loai == BONG ? 90 : 100;
    }

    /** Te liet: 15% mat luot. {@code roll} 0..99. */
    public boolean matLuot(final long bayGio, final int roll) {
        return dangCo(bayGio) && loai == TE && roll < 15;
    }

    /** ms con lai (u16 cho goi -97). */
    public int msConLai(final long bayGio) {
        if (!dangCo(bayGio)) {
            return 0;
        }
        return (int) Math.min(65535L, het - bayGio);
    }
}
