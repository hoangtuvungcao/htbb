package com.htbb.pokemon;

/**
 * Boss theo pha (docs/thiet-ke-game.md). Lop THUAN: chi tinh pha / su kien, khong gui goi.
 *
 *   pha 1: 100-70% HP danh thuong + 1 chieu lon (bao truoc -96)
 *   pha 2: 70-40%  them chieu vung, goi de (adds) luc vao pha
 *   pha 3: < 40%   "no": +30% toc do danh, goi de lan 2
 *   No theo thoi gian: boss Gym 5 phut, boss the gioi 15 phut -> sat thuong x3.
 *   So de moi lan goi theo so nguoi (toi da 6).
 */
public final class GiaiDoanBoss {

    public static final int NGUONG_PHA_2 = 70;
    public static final int NGUONG_PHA_3 = 40;
    public static final long NO_GYM_MS = 5L * 60L * 1000L;
    public static final long NO_THE_GIOI_MS = 15L * 60L * 1000L;
    public static final int DE_TOI_DA = 6;
    public static final int NO_TOC_DO_PHAN_TRAM = 130;
    public static final int NO_THOI_GIAN_SAT_THUONG_PHAN_TRAM = 300;

    /** Su kien goi -98. */
    public static final int SK_VAO_PHA = 0;
    public static final int SK_NO = 1;
    public static final int SK_GOI_DE = 2;
    public static final int SK_KHIEN = 3;

    public final long batDau;
    public final long noSauMs;
    public int pha = 1;
    public boolean daNoThoiGian;
    /** Lan cuoi bao truoc chieu lon. */
    public long lanNiemCuoi;

    public GiaiDoanBoss(final long batDau, final boolean theGioi) {
        this.batDau = batDau;
        this.noSauMs = theGioi ? NO_THE_GIOI_MS : NO_GYM_MS;
    }

    /** Pha theo % HP (1..3). */
    public static int phaTheoHp(final long hp, final long hpToiDa) {
        if (hpToiDa <= 0L) {
            return 1;
        }
        // 100-70% (khong gom 70) pha 1; 70-40% pha 2; duoi 40% pha 3 (THIET_KE 4.6).
        final long x = Math.max(0L, hp) * 100L;
        if (x > (long) NGUONG_PHA_2 * hpToiDa) {
            return 1;
        }
        if (x >= (long) NGUONG_PHA_3 * hpToiDa) {
            return 2;
        }
        return 3;
    }

    /** So de goi theo so nguoi dang danh: 2..6 (khong co nguoi = 0). */
    public static int soDe(final int soNguoi) {
        if (soNguoi <= 0) {
            return 0;
        }
        return Math.max(2, Math.min(DE_TOI_DA, 1 + soNguoi));
    }

    /**
     * Cap nhat theo HP / thoi gian. @return danh sach su kien moi (ma SK_*), co the rong.
     * Vao pha 2 va 3 deu kem SK_GOI_DE; vao pha 3 kem SK_NO.
     */
    public int[] capNhat(final long hp, final long hpToiDa, final long bayGio) {
        final int[] tam = new int[4];
        int n = 0;
        final int moi = phaTheoHp(hp, hpToiDa);
        if (moi > pha) {
            pha = moi;
            tam[n++] = SK_VAO_PHA;
            tam[n++] = SK_GOI_DE;
            if (pha == 3) {
                tam[n++] = SK_NO;
            }
        }
        if (!daNoThoiGian && bayGio - batDau >= noSauMs) {
            daNoThoiGian = true;
            tam[n++] = SK_NO;
        }
        final int[] kq = new int[n];
        System.arraycopy(tam, 0, kq, 0, n);
        return kq;
    }

    /** Tran he so khac he (%) cua don truong Gym vao nguoi choi (cau_hinh gym.he_toi_da), can bang 29/10/2026. */
    public static final int GYM_HE_TOI_DA = 115;
    /** So de moi lan goi cua tran Gym 1 dau 1 (cau_hinh gym.so_de); boss the gioi van theo soDe. */
    public static final int GYM_SO_DE = 1;

    /**
     * Don truong Gym da nhan khac he {@code heSo}% (HePokemon.heSoTrongGame: 150 / 200 khi khac): ha ve {@code tran}%.
     * Yeu / gan mien (&lt;= tran) giu nguyen; tran &lt;= 0 = tat. Toi thieu 1.
     */
    public static int giamKhacHeGym(final int dam, final int heSo, final int tran) {
        if (dam <= 0 || tran <= 0 || heSo <= tran) {
            return dam;
        }
        return (int) Math.max(1L, (long) dam * tran / heSo);
    }

    /** San he so khac he (%) cua chieu NGUOI CHOI vao truong Gym (cau_hinh gym.he_ra_toi_thieu). */
    public static final int GYM_HE_RA_TOI_THIEU = 100;

    /**
     * HP truong Gym cho nguoi thach dau ma chieu danh tot nhat chi gay {@code heTotNhat}% (khac he): duoi san {@code san}% thi
     * HP x heTotNhat / san - bang voi viec chieu cua ho duoc tinh it nhat x0,7. Dong Dien (moi chieu danh deu he Dien) gap
     * Seismitoad / Golem (he Dat, gan mien x0,3) truoc day chi gay 30% - khong the ha trong 10 phut (do suc 29/10/2026).
     * 30/10/2026 (do suc chi vu khi shop, khong giap): san 70 -> 100 va chi tinh chieu he (o 1..3, khong tinh don Cao he
     * Thuong o 0) - dong Lua thua Seismitoad cap 40 / 42 (boss con 30% / 27%), Dien thua Celadon cap 37 (con 23%):
     * chieu chinh x0,7 ma san 70 khong bu. Khac he van thiet: boss danh manh hon (tran x1,15) va tran dai hon.
     * san <= 0 = tat; heTotNhat <= 0 (khong biet) giu nguyen.
     */
    public static int hpGymTheoHe(final int hp, final int heTotNhat, final int san) {
        if (hp <= 0 || san <= 0 || heTotNhat <= 0 || heTotNhat >= san) {
            return hp;
        }
        return (int) Math.max(1L, (long) hp * heTotNhat / san);
    }

    /** He so sat thuong boss (%): no theo thoi gian x3. */
    public int heSoSatThuong() {
        return daNoThoiGian ? NO_THOI_GIAN_SAT_THUONG_PHAN_TRAM : 100;
    }

    /** He so toc do danh (%): pha 3 +30%. */
    public int heSoTocDo() {
        return pha >= 3 ? NO_TOC_DO_PHAN_TRAM : 100;
    }

    /** Giay con lai toi khi no theo thoi gian (u16 goi -98). */
    public int giayDenNo(final long bayGio) {
        if (daNoThoiGian) {
            return 0;
        }
        return (int) Math.max(0L, Math.min(65535L, (batDau + noSauMs - bayGio) / 1000L));
    }

    /** Toi luc niem chieu lon chua: moi 12 giay pha 1, 9 giay pha 2, 6 giay pha 3. */
    public boolean denLucNiem(final long bayGio) {
        final long chuKy = pha == 1 ? 12000L : (pha == 2 ? 9000L : 6000L);
        if (bayGio - lanNiemCuoi >= chuKy) {
            lanNiemCuoi = bayGio;
            return true;
        }
        return false;
    }

    /** Thoi gian niem (ms) 1,5..3 giay: pha cao niem nhanh hon. */
    public int msNiem() {
        return pha == 1 ? 3000 : (pha == 2 ? 2200 : 1500);
    }

    // ------------------------------------------------------------------ vung canh bao

    /** Diem (px, py) co nam trong vung hinh {@code hinh} tam (x, y) khong. */
    public static boolean trongVung(final int hinh, final int x, final int y, final int r, final int rong,
            final int huongDo, final int px, final int py) {
        final long dx = px - x;
        final long dy = py - y;
        final long d2 = dx * dx + dy * dy;
        switch (hinh) {
            case 0: // tron ban kinh r
                return d2 <= (long) r * r;
            case 1: { // non: ban kinh r, goc mo "rong" do, huong huongDo
                if (d2 > (long) r * r) {
                    return false;
                }
                if (d2 == 0L) {
                    return true;
                }
                final double g = Math.toDegrees(Math.atan2(dy, dx));
                double lech = Math.abs(((g - huongDo) % 360 + 540) % 360 - 180);
                return lech <= rong / 2.0;
            }
            case 2: { // chu nhat: dai r theo huong, rong "rong"
                final double rad = Math.toRadians(huongDo);
                final double doc = dx * Math.cos(rad) + dy * Math.sin(rad);
                final double ngang = -dx * Math.sin(rad) + dy * Math.cos(rad);
                return doc >= 0 && doc <= r && Math.abs(ngang) <= rong / 2.0;
            }
            case 3: // vanh: r ngoai, rong = r trong
                return d2 <= (long) r * r && d2 >= (long) rong * rong;
            default:
                return false;
        }
    }

    // ------------------------------------------------------------------ dong gop (-99)

    /**
     * Phan nghin dong gop cua tung nguoi (sat thuong + hoi mau + chiu don quy ra diem).
     * @return mang cung do dai, tong <= 1000
     */
    public static int[] phanNghin(final long[] diem) {
        long tong = 0L;
        for (final long d : diem) {
            tong += Math.max(0L, d);
        }
        final int[] kq = new int[diem.length];
        if (tong <= 0L) {
            return kq;
        }
        for (int i = 0; i < diem.length; i++) {
            kq[i] = (int) (Math.max(0L, diem[i]) * 1000L / tong);
        }
        return kq;
    }

    /** Hang thuong boss the gioi: 2 = hop vang (top 10), 1 = hop tham gia (>= 0,5%), 0 = khong. */
    public static int bacThuong(final int hang, final int phanNghin) {
        if (hang >= 1 && hang <= 10 && phanNghin > 0) {
            return 2;
        }
        return phanNghin >= 5 ? 1 : 0;
    }
}
