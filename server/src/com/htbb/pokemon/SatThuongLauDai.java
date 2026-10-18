package com.htbb.pokemon;

/**
 * Cong thuc sat thuong moi (THIET_KE_LAU_DAI.md muc 4.1). Lop THUAN.
 *
 * <pre>
 * co ban  = max(5% atk, atk - def)
 * dam     = co ban x %chieu x he x STAB x (1 + do he%) x chi mang x (0,95..1,05)
 * he      = HePokemon.heSoTrongGame, AP SAU khi tru thu
 * STAB    = x1,2 khi he chieu trung he 1/he 2
 * chi mang= x1,5
 * PvP     = dam x 0,30, tran 8% HP toi da muc tieu moi don
 * </pre>
 *
 * Co cua goi -95 (bit): 0 chi mang, 1 ne, 2 khac >= x1,5, 3 kem <= x0,7, 4 gan mien x0,3,
 * 5 STAB, 6 trung trang thai.
 */
public final class SatThuongLauDai {

    public static final int CO_CHI_MANG = 1;
    public static final int CO_NE = 2;
    public static final int CO_KHAC = 4;
    public static final int CO_KEM = 8;
    public static final int CO_GAN_MIEN = 16;
    public static final int CO_STAB = 32;
    public static final int CO_TRANG_THAI = 64;

    public static final int CHI_MANG_PHAN_TRAM = 150;
    public static final int PVP_PHAN_TRAM = 30;
    public static final int PVP_TRAN_PHAN_TRAM_HP = 8;
    public static final int SAN_PHAN_TRAM_ATK = 5;

    private SatThuongLauDai() {
    }

    /** Sat thuong co ban sau thu, san 5% tan cong (quai thu cao van tray). */
    public static long coBan(final long atk, final long def) {
        final long a = Math.max(0L, atk);
        return Math.max(a * SAN_PHAN_TRAM_ATK / 100L, a - Math.max(0L, def));
    }

    /**
     * Ket qua mot don. {@code dao} = 95..105 (bien dong, test truyen 100).
     * @return {dam, co}
     */
    public static int[] tinh(final long atk, final long def, final int phanTramChieu, final int heChieu,
            final int heDanh1, final int heDanh2, final int heThu1, final int heThu2,
            final int doHePhanTram, final boolean chiMang, final int dao) {
        int co = 0;
        double d = coBan(atk, def);
        d = d * Math.max(0, phanTramChieu) / 100.0;
        if (heChieu >= 0 && heChieu < HePokemon.SO_HE) {
            final int he = HePokemon.heSoTrongGame(heChieu, heThu1, heThu2);
            d = d * he / 100.0;
            if (he >= 150) {
                co |= CO_KHAC;
            } else if (he <= 30) {
                co |= CO_GAN_MIEN;
            } else if (he <= 70) {
                co |= CO_KEM;
            }
            if (ChieuPokemon.coStab(heChieu, heDanh1, heDanh2)) {
                d = d * ChieuPokemon.PT_STAB / 100.0;
                co |= CO_STAB;
            }
        }
        d = d * (100 + Math.max(0, Math.min(20, doHePhanTram))) / 100.0;
        if (chiMang) {
            d = d * CHI_MANG_PHAN_TRAM / 100.0;
            co |= CO_CHI_MANG;
        }
        d = d * Math.max(95, Math.min(105, dao)) / 100.0;
        final long kq = Math.max(1L, Math.min((long) Integer.MAX_VALUE, Math.round(d)));
        return new int[]{(int) kq, co};
    }

    /** PvP: x0,30, tran 8% HP toi da cua muc tieu (toi thieu 1). */
    public static int pvp(final int dam, final int hpToiDaMucTieu) {
        final long x = (long) Math.max(0, dam) * PVP_PHAN_TRAM / 100L;
        final long tran = Math.max(1L, (long) Math.max(0, hpToiDaMucTieu) * PVP_TRAN_PHAN_TRAM_HP / 100L);
        return (int) Math.max(dam > 0 ? 1L : 0L, Math.min(x, tran));
    }

    /** Do he: +10% o bac 0, +1%/bac, toi da +20% (muc 3.4). */
    public static int doHePhanTram(final int bacDo, final boolean cungHe) {
        if (!cungHe) {
            return 0;
        }
        return Math.min(20, 10 + Math.max(0, bacDo));
    }
}
