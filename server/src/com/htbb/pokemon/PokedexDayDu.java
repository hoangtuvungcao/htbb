package com.htbb.pokemon;

import java.util.List;

/**
 * Pokedex day du 1.025 loai (he_thong_day_du.md 3.4): tom tat chu cho moi client (menu NPC Pokedex) va co gap / ha /
 * bat cho goi -126 kieu 3. Loai cua the he chua mo hien "Chưa xuất hiện". Lop THUAN (test: test/KiemLoaiDayDu).
 */
public final class PokedexDayDu {

    private PokedexDayDu() {
    }

    private static volatile java.lang.reflect.Method mDaBat;
    private static volatile boolean daTim;

    /**
     * Da bat (thu phuc) loai {@code dex} chua: dung Pokedex.daBat(int) cua nhanh bat-trade neu co (doc bang phan chieu
     * de lop nay khong phu thuoc nhanh); chua co thu phuc thi luon false.
     */
    public static boolean daBat(final Pokedex dx, final int dex) {
        if (!daTim) {
            try {
                mDaBat = Pokedex.class.getMethod("daBat", int.class);
            } catch (final Exception e) {
                mDaBat = null;
            }
            daTim = true;
        }
        if (mDaBat == null || dx == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(mDaBat.invoke(dx, Integer.valueOf(dex)));
        } catch (final Exception e) {
            return false;
        }
    }

    /** Co cua mot loai: bit0 da gap, bit1 da ha, bit2 da bat; 0 = chua gap. */
    public static int co(final Pokedex dx, final int dex) {
        if (dx == null || !dx.daGap(dex)) {
            return 0;
        }
        return 1 | (dx.soHa(dex) > 0 ? 2 : 0) | (daBat(dx, dex) ? 4 : 0);
    }

    static String so(final int n) {
        final String s = String.valueOf(n);
        if (n < 1000) {
            return s;
        }
        return s.substring(0, s.length() - 3) + "." + s.substring(s.length() - 3);
    }

    /** Tom tat chu (Pokedex cua NPC, moi client). */
    public static String tomTat(final Pokedex dx, final LoaiDuLieu d) {
        final int tong = d.soLoai();
        int gap = 0, ha = 0, bat = 0;
        final int[] tongGen = new int[10], gapGen = new int[10], haGen = new int[10], moGen = new int[10];
        for (final LoaiDuLieu.Loai l : d.tatCa()) {
            final int g = Math.max(0, Math.min(9, l.theHe));
            tongGen[g]++;
            if (l.daMo) {
                moGen[g]++;
            }
            final int c = co(dx, l.dex);
            if ((c & 1) != 0) {
                gap++;
                gapGen[g]++;
            }
            if ((c & 2) != 0) {
                ha++;
                haGen[g]++;
            }
            if ((c & 4) != 0) {
                bat++;
            }
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("Pokédex · ").append(so(tong)).append(" loài\n");
        sb.append("Đã gặp ").append(gap).append(" · đã hạ ").append(ha);
        if (bat > 0) {
            sb.append(" · đã bắt ").append(bat);
        }
        sb.append('\n');
        int dauChua = 0, cuoiChua = 0, soChua = 0;
        int dex = 1;
        for (int g = 1; g <= 9; g++) {
            if (tongGen[g] == 0) {
                continue;
            }
            final int tu = dex, den = dex + tongGen[g] - 1;
            dex = den + 1;
            if (moGen[g] == 0) {
                if (dauChua == 0) {
                    dauChua = g;
                }
                cuoiChua = g;
                soChua += tongGen[g];
                continue;
            }
            sb.append(LoaiDuLieu.tenTheHe(g)).append(" (#").append(tu).append('–').append(den).append("): gặp ")
                    .append(gapGen[g]).append('/').append(tongGen[g]).append(" · hạ ").append(haGen[g]);
            if (moGen[g] < tongGen[g]) {
                sb.append(" · mở ").append(moGen[g]);
            }
            sb.append('\n');
        }
        if (soChua > 0) {
            sb.append(LoaiDuLieu.tenTheHe(dauChua));
            if (cuoiChua != dauChua) {
                sb.append(" … ").append(LoaiDuLieu.tenTheHe(cuoiChua));
            }
            sb.append(" (").append(so(soChua)).append(" loài): Chưa xuất hiện\n");
        }
        final List<int[]> top = dx == null ? java.util.Collections.<int[]>emptyList() : dx.top(3);
        if (!top.isEmpty()) {
            sb.append("Hạ nhiều nhất:");
            for (final int[] t : top) {
                final String ten = d.ten(t[0]);
                sb.append(' ').append(ten.isEmpty() ? "#" + t[0] : ten).append(" (").append(t[1]).append(')');
            }
        }
        return sb.toString().trim();
    }
}
