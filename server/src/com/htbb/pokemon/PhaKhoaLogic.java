package com.htbb.pokemon;

/**
 * [khoa-khi-mac] Pha khoa trang bi da mac (chu game 0.9.6.1: "do da mac bi khoa, khong giao dich duoc" + bo sung
 * "pha khoa duoc voi gia lon" - mot ho hut xu). Thiet ke can bang 4.3 ghi mon da mac KHONG BAO GIO giao dich lai;
 * chu game da ghi de bang tinh nang nay.
 *
 * Lop THUAN (khong DB / mang): bang gia mac dinh khop sql/sua_096_1_logic.sql khoi [khoa-khi-mac]
 * (data_pha_khoa_bac, data_pha_khoa_lan); real.PhaKhoaGame nap bang CSDL de de len qua {@link #napBang}.
 *
 *   xu = xuMoiGio(cap mon) x gio%(bac hiem) / 100 x (100 + plus x cong%(bac)) / 100 x heSo%(lan) / 100
 *   + so Da pha khoa theo lan. Toi da {@link #lanToiDa()} lan moi mon (so dong data_pha_khoa_lan).
 *
 * Chi khoa DO MAC moi pha duoc (Item.daMac == 1): mon khoa vi ly do khac (mua bang luong khoa, qua nhiem vu / su
 * kien, nguyen lieu khoa, tinh luyen) bi tu choi.
 */
public final class PhaKhoaLogic {

    /** data_item id cua "Đá phá khoá" (type 28) - PHAI khop sql/sua_096_1_logic.sql + sql/htbb.sql. */
    public static final int ID_DA_PHA_KHOA = 1295;

    /** {colorName, gio_phan_tram (100 = 1 gio thu nhap), cong_moi_cap_phan_tram}. */
    static final int[][] BAC_MAC_DINH = {
        {0, 50, 10},   // Thuong (trang)
        {1, 300, 10},  // Hoan my cu (tinh nhu Su thi)
        {2, 100, 10},  // Do cu (tinh nhu Hiem)
        {3, 200, 10},  // Quy (xanh duong)
        {4, 300, 10},  // Su thi (tim)
        {5, 50, 10},   // trang tri (vang)
        {6, 500, 10},  // Huyen thoai (cam)
        {7, 100, 10},  // Hiem (xanh la, ma moi)
    };

    /** {lan (1-based), he_so_phan_tram, so_da}. */
    static final int[][] LAN_MAC_DINH = {
        {1, 100, 1},
        {2, 200, 2},
        {3, 400, 3},
    };

    private static volatile int[][] bac = BAC_MAC_DINH;
    private static volatile int[][] lan = LAN_MAC_DINH;

    private PhaKhoaLogic() {
    }

    /** Thay bang (tu CSDL). Bang rong / null giu bang dang dung. */
    public static void napBang(final int[][] bacMoi, final int[][] lanMoi) {
        if (bacMoi != null && bacMoi.length > 0) {
            bac = bacMoi;
        }
        if (lanMoi != null && lanMoi.length > 0) {
            lan = sapTheoLan(lanMoi);
        }
    }

    /** Tro lai bang mac dinh (test). */
    public static void macDinh() {
        bac = BAC_MAC_DINH;
        lan = LAN_MAC_DINH;
    }

    static int[][] sapTheoLan(final int[][] v) {
        final int[][] r = v.clone();
        java.util.Arrays.sort(r, (a, b) -> Integer.compare(a[0], b[0]));
        return r;
    }

    /** So lan pha khoa toi da moi mon. */
    public static int lanToiDa() {
        return lan.length;
    }

    static int[] dongBac(final int colorName) {
        for (final int[] d : bac) {
            if (d[0] == colorName) {
                return d;
            }
        }
        return bac[0];
    }

    /** Dong cua lan pha thu {@code soLanDaPha + 1}; null neu da het. */
    static int[] dongLan(final int soLanDaPha) {
        return soLanDaPha < 0 || soLanDaPha >= lan.length ? null : lan[soLanDaPha];
    }

    /** Gia xu lan pha ke tiep; -1 neu da het so lan. */
    public static long giaXu(final int capMon, final int colorName, final int plus, final int soLanDaPha) {
        final int[] l = dongLan(soLanDaPha);
        if (l == null) {
            return -1L;
        }
        final int[] b = dongBac(colorName);
        final long gio = ThongSoLauDai.xuMoiGio(Math.max(1, Math.min(ThongSoLauDai.CAP_TOI_DA, capMon)));
        final long heSoPlus = 100L + (long) Math.max(0, plus) * b[2];
        final long gia = gio * b[1] * heSoPlus * l[1] / 1000000L;
        return Math.max(1L, gia);
    }

    /** So Da pha khoa can cho lan pha ke tiep; -1 neu da het so lan. */
    public static int soDa(final int soLanDaPha) {
        final int[] l = dongLan(soLanDaPha);
        return l == null ? -1 : l[2];
    }

    /**
     * Ly do KHONG pha khoa duoc, null = duoc.
     * @param lock Item.lock, @param daMac Item.daMac, @param soLanDaPha Item.soLanPhaKhoa
     */
    public static String lyDoKhongDuoc(final int lock, final int daMac, final int soLanDaPha, final boolean dangBan) {
        if (dangBan) {
            return "Vật phẩm đang bày bán, không phá khoá được.";
        }
        if (lock == 0) {
            return "Vật phẩm chưa khoá, không cần phá khoá.";
        }
        if (daMac != 1) {
            return "Vật phẩm khoá vì lý do khác (mua bằng lượng khoá, quà nhiệm vụ / sự kiện, nguyên liệu khoá, tinh luyện), không phá khoá được.";
        }
        if (soLanDaPha >= lanToiDa()) {
            return "Vật phẩm đã phá khoá tối đa " + lanToiDa() + " lần.";
        }
        return null;
    }

    /** Xu dinh dang 1.234.567. */
    public static String dinhDang(final long xu) {
        final String s = Long.toString(Math.abs(xu));
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) {
                b.append('.');
            }
            b.append(s.charAt(i));
        }
        return (xu < 0 ? "-" : "") + b;
    }
}
