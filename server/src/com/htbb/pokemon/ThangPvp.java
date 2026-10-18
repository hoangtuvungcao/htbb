package com.htbb.pokemon;

/**
 * Thang PvP mua (docs/thiet-ke-game.md): Elo, bac Poke -> Great -> Ultra -> Master,
 * ghep tran theo dai 10 cap, reset mem moi 13 tuan (giu 50% phan tren muc goc). Lop THUAN.
 */
public final class ThangPvp {

    public static final int ELO_BAN_DAU = 1000;
    public static final int K = 32;
    public static final int TUAN_MOI_MUA = 13;
    public static final String[] TEN_BAC = {"Poké Ball", "Great Ball", "Ultra Ball", "Master Ball"};
    /** Nguong Elo vao bac 1..3. */
    public static final int[] NGUONG_BAC = {1200, 1400, 1600};
    /** Luong thuong cuoi mua theo bac (Master 100, Ultra 40, Great 15). */
    public static final int[] LUONG_BAC = {0, 15, 40, 100};

    private ThangPvp() {
    }

    /** Ky vong thang cua A (phan nghin). */
    public static int kyVongPhanNghin(final int eloA, final int eloB) {
        return (int) Math.round(1000.0 / (1.0 + Math.pow(10.0, (eloB - eloA) / 400.0)));
    }

    /** Elo moi cua A sau tran voi B. {@code ketQua}: 1 thang, 0 thua. */
    public static int eloMoi(final int eloA, final int eloB, final int ketQua) {
        final int ky = kyVongPhanNghin(eloA, eloB);
        final int d = (int) Math.round(K * (ketQua * 1000 - ky) / 1000.0);
        return Math.max(0, eloA + d);
    }

    public static int bac(final int elo) {
        int b = 0;
        for (int i = 0; i < NGUONG_BAC.length; i++) {
            if (elo >= NGUONG_BAC[i]) {
                b = i + 1;
            }
        }
        return b;
    }

    /** Hai nguoi duoc ghep khi cung dai 10 cap (1-10, 11-20, ...). */
    public static boolean cungDai(final int capA, final int capB) {
        return (capA - 1) / 10 == (capB - 1) / 10;
    }

    /** Reset mem: giu 50% phan chenh so voi Elo goc. */
    public static int resetMem(final int elo) {
        return ELO_BAN_DAU + (elo - ELO_BAN_DAU) / 2;
    }

    /** Mua thu may (0..) theo ngay tu khai truong. */
    public static int mua(final int ngay) {
        return Math.max(0, ngay) / (TUAN_MOI_MUA * 7);
    }
}
