package com.htbb.pokemon;

import java.util.Map;
import java.util.TreeMap;

/**
 * Duong chi so quai thuong chuan theo cap: cap -&gt; {HP, tan cong, thu, EXP, xu} cua quai thuong dang chay (dong
 * data_monster_lau_dai + quai Hoenn nam 2 139..174 cua data_monster - nap trong data.Database.loadMonsterTemplate). Noi suy
 * tuyen tinh giua hai cap co quai (HP lam tron 10), ngoai hai dau lay cap gan nhat.
 * <p>
 * Dung cho quai hiem / bung phat / ngay cong dong (HeSinhThaiGame, chu game 31/10/2026): cap goc + 2, chi so + thuong theo
 * duong nay - KHONG theo chi so goc cu cua template loai do. Lop THUAN. Kiem: server/test/KiemDoiLoaiQuai.
 */
public final class DuongQuaiChuan {

    private static volatile TreeMap<Integer, int[]> DUONG = new TreeMap<Integer, int[]>();

    private DuongQuaiChuan() {
    }

    /** Dat duong (cap -&gt; {hp, tc, thu, exp, xu}); hai dong cung cap giu dong HP lon hon. */
    public static void dat(final Map<Integer, int[]> d) {
        DUONG = d == null ? new TreeMap<Integer, int[]>() : new TreeMap<Integer, int[]>(d);
    }

    /** Them mot dong vao ban dang dung (dung luc nap). */
    public static void them(final TreeMap<Integer, int[]> d, final int cap, final int hp, final int tc, final int thu,
            final int exp, final int xu) {
        final int[] cu = d.get(Integer.valueOf(cap));
        if (cu == null || hp > cu[0]) {
            d.put(Integer.valueOf(cap), new int[]{hp, tc, thu, exp, xu});
        }
    }

    public static boolean rong() {
        return DUONG.isEmpty();
    }

    /** {HP, tan cong, thu, EXP, xu} o cap {@code cap}; null neu chua nap. */
    public static int[] taiCap(final int cap) {
        final TreeMap<Integer, int[]> d = DUONG;
        if (d.isEmpty()) {
            return null;
        }
        final Map.Entry<Integer, int[]> a = d.floorEntry(Integer.valueOf(cap)), b = d.ceilingEntry(Integer.valueOf(cap));
        if (a == null) {
            return b.getValue().clone();
        }
        if (b == null || a.getKey().intValue() == b.getKey().intValue()) {
            return a.getValue().clone();
        }
        final double f = (cap - a.getKey().intValue()) / (double) (b.getKey().intValue() - a.getKey().intValue());
        final int[] x = a.getValue(), y = b.getValue(), r = new int[5];
        for (int i = 0; i < 5; i++) {
            r[i] = (int) Math.round(x[i] + (y[i] - x[i]) * f);
        }
        r[0] = (int) (Math.round(r[0] / 10.0) * 10);
        return r;
    }
}
