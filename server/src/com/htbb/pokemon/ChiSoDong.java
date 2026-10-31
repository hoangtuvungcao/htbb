package com.htbb.pokemon;

/**
 * Chi so goc cua 5 dong Pokemon theo bang thiet ke (docs/thiet-ke-game.md muc 2.1): HP / Tan cong / Thu tuong doi
 * Charmander 100/105/90, Bulbasaur 115/90/115, Eevee 90/100/95, Squirtle 120/85/125, Pichu 85/110/85.
 * <p>
 * Truoc 31/10/2026 (quyet dinh chu game sau cong kiem bot cb2) chi so lay tu cong thuc cu theo dong trong
 * real.CharChiSo: he so HP 80/70/60/70/60, tan cong theo chi so khac nhau (x4/3, x11/10 x1,5, x5/3...), phong thu = Kheo
 * leo (Charmander / Eevee khong tang Kheo leo theo cap), Squirtle cong phong thu va tan cong HAI lan, Pichu x11/10 thu -
 * o cap 30 tich HP x Tan cong x Thu lech toi 11 lan, Charmander / Eevee khong ha noi quai cung cap tu cap 30.
 * <p>
 * Nay phan goc (nhan vat, khong tinh do / pet / thu cuoi / thoi trang...) cua moi dong = mot cong thuc trung tinh theo cap
 * nhan ti le bang thiet ke; do va moi % (tien hoa, huan luyen, bo...) giu nguyen va nhu nhau cho moi dong.
 */
public final class ChiSoDong {

    /** {HP, tan cong, thu} tuong doi theo dong (0 Charmander, 1 Bulbasaur, 2 Eevee, 3 Squirtle, 4 Pichu). */
    public static final int[][] TI_LE = {{100, 105, 90}, {115, 90, 115}, {90, 100, 95}, {120, 85, 125}, {85, 110, 85}};
    /** HP = don vi HP x he so nay (moi dong nhu nhau; truoc 80/70/60/70/60). */
    public static final int HE_SO_HP = 70;
    /** Tan cong = don vi tan cong x 4/3 (moi dong nhu nhau). */
    public static final int TC_TU = 4, TC_MAU = 3;

    private ChiSoDong() {
    }

    static int[] tiLe(final int dong) {
        return dong >= 0 && dong < TI_LE.length ? TI_LE[dong] : TI_LE[0];
    }

    static int cap(final int cap) {
        return Math.max(1, Math.min(255, cap));
    }

    /** Don vi HP goc (truoc x {@link #HE_SO_HP}): 20 + 2 moi cap (trung binh cac dong cu), x ti le dong. */
    public static int donViHp(final int dong, final int cap) {
        return (int) ((long) (20 + 2 * (cap(cap) - 1)) * tiLe(dong)[0] / 100L);
    }

    /** Don vi tan cong goc (truoc x 4/3): 20 + 5 moi cap (chi so tan cong chinh cu: 1 + 4 moi cap), x ti le dong. */
    public static int donViTanCong(final int dong, final int cap) {
        return (int) ((long) (20 + 5 * (cap(cap) - 1)) * tiLe(dong)[1] / 100L);
    }

    /** Phong thu goc: 20 + 2,5 moi cap (trung binh Kheo leo cac dong cu), x ti le dong. */
    public static int thu(final int dong, final int cap) {
        return (int) ((long) (40 + 5 * (cap(cap) - 1)) * tiLe(dong)[2] / 200L);
    }

    /** HP goc (khong do / % nao). */
    public static int hp(final int dong, final int cap) {
        return donViHp(dong, cap) * HE_SO_HP;
    }

    /** Tan cong goc (khong do / % nao). */
    public static int tanCong(final int dong, final int cap) {
        return donViTanCong(dong, cap) * TC_TU / TC_MAU;
    }

    /** Cong % tiem nang (skin thu cuoi - SkinThuCuoi.pcTiemNang) nhu Char.tnSucKhoe(): goc + goc x pc / 100. */
    public static int voiTiemNang(final int goc, final int pc) {
        return goc + (int) ((long) goc * pc / 100L);
    }

    // ------------------------------------------------------------------ vu khi trung tinh

    /** cap bac vu khi tiem -> {atb0 theo dong 0..4} (tu bang mau vat pham luc dau), null = chua nap. */
    private static volatile java.util.TreeMap<Integer, int[]> VU_KHI;

    /**
     * Vu khi tiem moi dong co tan cong goc khac nhau o cung bac (bac 16: 90/110/130/150/80 - Squirtle +60, Pichu -10 so
     * trung binh). Do lech de cong vao atb0 cua vu khi dong {@code dong} cap {@code capMon} cho bang trung binh cac dong
     * (nang cap + giu nguyen). 0 neu khong phai vu khi / chua co bang.
     */
    public static int lechVuKhi(final int type, final int capMon, final int dong) {
        if (type < 3 || type > 7 || dong < 0 || dong > 4) {
            return 0;
        }
        java.util.TreeMap<Integer, int[]> b = VU_KHI;
        if (b == null) {
            b = napVuKhi();
            if (b == null) {
                return 0;
            }
        }
        final java.util.Map.Entry<Integer, int[]> e = b.floorEntry(Integer.valueOf(capMon));
        if (e == null) {
            return 0;
        }
        final int[] a = e.getValue();
        long tong = 0;
        for (final int v : a) {
            tong += v;
        }
        return (int) (tong / a.length) - a[dong];
    }

    /** Test: dat bang vu khi {cap -> atb0 theo dong}. */
    public static void datVuKhi(final java.util.TreeMap<Integer, int[]> b) {
        VU_KHI = b;
    }

    static java.util.TreeMap<Integer, int[]> napVuKhi() {
        try {
            if (real.Map.itemTemplates == null || real.Map.itemTemplates.isEmpty()) {
                return null;
            }
            final java.util.Hashtable<Integer, real.ItemTemplates> t = real.Map.itemTemplates.get(0);
            final java.util.TreeMap<Integer, int[]> b = new java.util.TreeMap<Integer, int[]>();
            final java.util.TreeMap<Integer, int[]> dem = new java.util.TreeMap<Integer, int[]>();
            for (final real.ItemTemplates it : t.values()) {
                final boolean tiem = it.id >= 79 && it.id <= 113 || it.id >= 174 && it.id <= 213;
                if (!tiem || it.type < 3 || it.type > 7) {
                    continue;
                }
                final int d = it.type - 3;
                int[] a = b.get(Integer.valueOf(it.level));
                if (a == null) {
                    b.put(Integer.valueOf(it.level), a = new int[5]);
                    dem.put(Integer.valueOf(it.level), new int[5]);
                }
                a[d] = it.atb[0];
                dem.get(Integer.valueOf(it.level))[d]++;
            }
            // chi giu bac co du 5 dong
            final java.util.Iterator<java.util.Map.Entry<Integer, int[]>> i = dem.entrySet().iterator();
            while (i.hasNext()) {
                final java.util.Map.Entry<Integer, int[]> e = i.next();
                for (final int c : e.getValue()) {
                    if (c == 0) {
                        b.remove(e.getKey());
                        break;
                    }
                }
            }
            if (b.isEmpty()) {
                return null;
            }
            VU_KHI = b;
            return b;
        } catch (final Exception e) {
            return null;
        }
    }
}
