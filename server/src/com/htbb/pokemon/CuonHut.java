package com.htbb.pokemon;

/**
 * Cac co che cuon hut nho (docs/thiet-ke-game.md): shiny + pity, Ham nguc bi an
 * (hat giong theo ngay), su pham, lich dang nhap, thoi tiet. Lop THUAN.
 */
public final class CuonHut {

    /** Shiny: 1/4096 lan ha. */
    public static final int SHINY_MAU = 4096;
    /** Lan ha thu 2000 khong ra thi chac chan ra. */
    public static final int SHINY_PITY = 2000;

    /** Su pham: thay cap >= 30, tro thap hon >= 20 cap; tro +20% EXP khi chung doi. */
    public static final int SU_PHAM_CAP_THAY = 30;
    public static final int SU_PHAM_CACH_CAP = 20;
    public static final int SU_PHAM_EXP_PHAN_TRAM = 20;
    public static final int[] SU_PHAM_MOC = {20, 40};

    /** Ham nguc: so tang toi da, 1 luot thuong / ngay. */
    public static final int HAM_NGUC_TANG_TOI_DA = 30;

    private CuonHut() {
    }

    // ------------------------------------------------------------------ shiny (7.3)

    /**
     * Lan ha nay co ra shiny khong. {@code dem} = so lan ha tu shiny truoc (truoc lan nay),
     * {@code roll} = so ngau nhien 0..mau-1, {@code nhan} = he so su kien (tuan shiny x10,
     * ngay cong dong x5; 1 = binh thuong).
     */
    public static boolean shiny(final int dem, final int roll, final int nhan, final int mau, final int pity) {
        if (pity > 0 && dem + 1 >= pity) {
            return true;
        }
        return roll < Math.max(1, nhan);
    }

    // ------------------------------------------------------------------ ham nguc (7.4)

    /** Hat giong cua ngay yyyyMMdd (moi nguoi cung ban do trong ngay). */
    public static long hatGiongNgay(final int ngayYmd) {
        long x = ngayYmd * 0x9E3779B97F4A7C15L + 0x2545F4914F6CDD1DL;
        x ^= x >>> 33;
        x *= 0xff51afd7ed558ccdL;
        x ^= x >>> 33;
        return x;
    }

    /** SplitMix64 buoc tiep. */
    public static long tiep(final long s) {
        long z = s + 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /**
     * Tang {@code tang} (1..) cua ngay: {idQuai (chi so trong {@code quai}), soQuai, bac
     * thuong (0 thuong, 1 mon tang 5/10/15...)}. Tat dinh theo (ngay, tang).
     */
    public static int[] tangHamNguc(final int ngayYmd, final int tang, final int[] quai) {
        long s = hatGiongNgay(ngayYmd);
        for (int i = 0; i < tang; i++) {
            s = tiep(s);
        }
        final int idx = quai.length == 0 ? 0 : (int) Math.floorMod(s, (long) quai.length);
        final int so = 3 + (int) Math.floorMod(s >>> 17, 3L) + tang / 5;
        final int bac = tang % 5 == 0 ? 1 : 0;
        return new int[]{quai.length == 0 ? -1 : quai[idx], so, bac};
    }

    /** Cap quai o tang (cap nguoi lam goc, +1 moi 3 tang, toi da +10). */
    public static int capQuaiTang(final int capNguoi, final int tang) {
        return capNguoi + Math.min(10, tang / 3);
    }

    // ------------------------------------------------------------------ su pham (7.5)

    public static boolean ghepSuPham(final int capThay, final int capTro) {
        return capThay >= SU_PHAM_CAP_THAY && capThay - capTro >= SU_PHAM_CACH_CAP;
    }

    /** Moc su pham vua dat (cap cu < moc <= cap moi), -1 neu khong. */
    public static int mocSuPham(final int capCu, final int capMoi) {
        for (final int m : SU_PHAM_MOC) {
            if (capCu < m && capMoi >= m) {
                return m;
            }
        }
        return -1;
    }

    // ------------------------------------------------------------------ lich dang nhap (7.1)

    /**
     * Ngay dang nhap moi {@code hom} (yyyyMMdd). @return {buoc moi, luong khoa thuong} - da
     * nhan hom nay thi {buoc cu, 0}. Nghi khong mat chuoi (chi dung lai).
     */
    public static int[] dangNhap(final int buocCu, final int ngayCuoi, final int hom) {
        if (ngayCuoi == hom) {
            return new int[]{buocCu, 0};
        }
        final int luong = KinhTeLauDai.luongKhoaDangNhap(buocCu);
        return new int[]{(buocCu + 1) % ThongSoLauDai.LICH_DANG_NHAP.length, luong};
    }

    // ------------------------------------------------------------------ thoi tiet (7.5)

    public static final String[] THOI_TIET = {"Nắng", "Mưa", "Tuyết", "Bão cát", "Quang đãng"};
    /** He duoc +10% theo thoi tiet (index THOI_TIET), -1 = khong. */
    public static final int[] HE_THOI_TIET = {HePokemon.LUA, HePokemon.NUOC, HePokemon.BANG, HePokemon.DA, -1};

    public static int thoiTiet(final int ngayYmd) {
        return (int) Math.floorMod(hatGiongNgay(ngayYmd) >>> 7, (long) THOI_TIET.length);
    }
}
