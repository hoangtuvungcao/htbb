package com.htbb.pokemon;

/**
 * Nang cap +1..+15 theo thiet ke dai han (docs/thiet-ke-game.md). Lop THUAN.
 * Dung chung cho CA HAI duong nang cap (MapEpNgoc.doFinishImbue - tay, CharCapNhat.doAutoImbue
 * - tu dong) qua real.NangCapLauDaiGame.
 */
public final class NangCapLauDai {

    private NangCapLauDai() {
    }

    /** Bac trang bi theo cap do (BAC_TB_CAP: 1,10,20..98). */
    public static int bacTheoCap(final int capDo) {
        int b = 0;
        for (int i = 0; i < ThongSoLauDai.BAC_TB_CAP.length; i++) {
            if (capDo >= ThongSoLauDai.BAC_TB_CAP[i]) {
                b = i;
            }
        }
        return b;
    }

    /** Xu cho MOT lan thu len +k (k = 1..15) voi do o cap {@code capDo}. */
    public static long chiPhi(final int capDo, final int k) {
        if (k < 1 || k > 15) {
            return 0L;
        }
        return ThongSoLauDai.CHI_PHI_NC[bacTheoCap(capDo)][k];
    }

    /** So da luyen kim can cho lan thu len +k. */
    public static int da(final int k) {
        return k < 1 || k > 15 ? 0 : ThongSoLauDai.DA_NC[k - 1];
    }

    /** Luong khoa cho bua bao ho len +k (0 = khong can / khong dung duoc). */
    public static int bua(final int k) {
        return k < 1 || k > 15 ? 0 : ThongSoLauDai.BUA_BAO_HO_LUONG_KHOA[k - 1];
    }

    /** Ti le (%) len +k sau {@code truot} lan truot lien tiep. */
    public static int tiLe(final int k, final int truot) {
        if (k < 1 || k > 15) {
            return 0;
        }
        return ThongSoLauDai.tiLeNangCap(k, Math.max(0, truot));
    }

    /** Thanh cong khi roll (0..99) < ti le. Dung "<" (khong "<=" nhu ban cu, xem GD2 1.4). */
    public static boolean thanhCong(final int roll, final int tiLe) {
        return roll < tiLe;
    }

    /**
     * Cap sau khi TRUOT lan thu len +k tu +{@code hienTai}: tu +7 tro len mat 1 cap tru
     * khi co bua; duoi +7 giu nguyen. Khong bao gio vo do.
     */
    public static int capSauTruot(final int hienTai, final int k, final boolean coBua) {
        if (k >= ThongSoLauDai.MAT_CAP_TU && !coBua && hienTai > 0) {
            return hienTai - 1;
        }
        return hienTai;
    }

    // ------------------------------------------------------------------ moc neo (thiet ke can bang 4.4, de xuat 6)

    /** Moc neo mac dinh +7 / +10 / +13 (bang data_nang_cap_moc). */
    public static final int[] MOC_NEO_MAC_DINH = {7, 10, 13};
    /** He so xu buoc +11..+15 (phan nghin) bu phan ky vong giam do moc neo (spec: +6%). */
    public static final int HE_SO_XU_BAC_CAO_MAC_DINH = 1060;

    /** Moc neo cao nhat &lt;= {@code hienTai} (0 = chua dat moc nao). */
    public static int mocGiu(final int hienTai, final int[] moc) {
        int m = 0;
        if (moc != null) {
            for (final int x : moc) {
                if (x > 0 && x <= hienTai && x > m) {
                    m = x;
                }
            }
        }
        return m;
    }

    /**
     * Cap sau khi TRUOT co moc neo: luat cu (-1 tu +7, giu cap neu co bua) nhung KHONG duoi moc neo cao nhat da dat
     * (dang o +8 truot len +9 -&gt; giu +7; dang o +10 truot len +11 -&gt; giu +10). {@code moc} null/rong = luat cu.
     */
    public static int capSauTruot(final int hienTai, final int k, final boolean coBua, final int[] moc) {
        final int cu = capSauTruot(hienTai, k, coBua);
        return Math.max(cu, Math.min(hienTai, mocGiu(hienTai, moc)));
    }

    /** Xu len +k co he so buoc cao (+11..+15 x heSo/1000). */
    public static long chiPhi(final int capDo, final int k, final int heSoBacCaoPhanNghin) {
        final long goc = chiPhi(capDo, k);
        if (k < 11 || heSoBacCaoPhanNghin <= 0) {
            return goc;
        }
        return Math.max(goc, goc * Math.min(2000, heSoBacCaoPhanNghin) / 1000L);
    }

    /** Diem % pity dang cong them vao ti le len +k sau {@code truot} lan truot. */
    public static int pityCong(final int k, final int truot) {
        return Math.max(0, tiLe(k, truot) - tiLe(k, 0));
    }

    /** % chi so mon tang khi len +k (3%/cap toi +10, 5%/cap tu +11). */
    public static int chiSoPhanTram(final int k) {
        return k < 1 || k > 15 ? 0 : ThongSoLauDai.CHI_SO_NC_PHAN_TRAM[k - 1];
    }

    /** Tong % chi so o muc +p (cong don). */
    public static int tongChiSoPhanTram(final int p) {
        int t = 0;
        for (int k = 1; k <= Math.min(15, p); k++) {
            t += chiSoPhanTram(k);
        }
        return t;
    }

    /**
     * Luong cong (tuyet doi) cho mot chi so goc {@code goc} khi len +k. Toi thieu 1 neu
     * goc > 0 (do cap thap van thay tang).
     */
    public static int congChiSo(final int goc, final int k) {
        if (goc <= 0) {
            return 0;
        }
        return Math.max(1, goc * chiSoPhanTram(k) / 100);
    }
}
