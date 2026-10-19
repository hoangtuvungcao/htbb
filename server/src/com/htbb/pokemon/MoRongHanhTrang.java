package com.htbb.pokemon;

/**
 * Mo rong hanh trang theo O (ham THUAN, khong cham Char / CSDL - de test). Lop noi vao game: real.MoRongTuiGame.
 *
 * <p>Suc chua hanh trang = MAX_INVENTORY (42) x maxBag (so trang, mua bang luong o NPC -10 / cua hang luong) +
 * oMoRong (o le, cot tob_char.o_mo_rong). oMoRong chi tang qua:
 * <ul>
 *   <li>dung "Ve mo rong hanh trang" (+{@link #O_MOI_VE} o / ve; ve ban o cua hang luong = tien loi, khong chi so);</li>
 *   <li>mua thang bang xu (+{@link #O_MOI_VE} o, khong can ve) - gia TANG DAN theo so buoc da mo ({@link #giaXu}).</li>
 * </ul>
 * Ca hai chi dung duoc khi suc chua &lt; {@link #TRAN_VE} (= 3 x 42 trang khoi dau); buoc cuoi chi them phan con thieu
 * toi tran. Nguoi da vuot tran nho mua trang bang luong GIU NGUYEN suc chua, chi khong dung ve / xu duoc nua.
 *
 * <p>Bang gia xu (buoc = oMoRong / 10, buoc 0 la lan dau): 20.000 / 30.000 / 45.000 / 68.000 / 100.000 / 150.000 /
 * 230.000 / 340.000 / 510.000 (~x1,5 moi buoc; tong 9 buoc 1.493.000 xu). Theo docs thiet ke can bang (gio thu nhap
 * cap 20 ~ 9.000 xu, cap 35 ~ 17.000, cap 70 ~ 82.000): buoc dau ~2 gio thu nhap o cap 20, buoc cuoi ~6 gio o cap
 * 70 - mot ho hut xu co tran, khong lam phat (tien loi, khong suc manh).
 */
public final class MoRongHanhTrang {

    /** So o moi trang (Char.MAX_INVENTORY). */
    public static final int O_MOI_TRANG = 42;
    /** So o them moi ve / moi lan mua xu. */
    public static final int O_MOI_VE = 10;
    /** Tran suc chua mo bang ve / xu (3 x 42 trang khoi dau). */
    public static final int TRAN_VE = 126;
    /** oMoRong toi da hop le (nhan vat 1 trang: 42 + 84 = 126). Doc CSDL bi kep vao [0, O_LE_TOI_DA]. */
    public static final int O_LE_TOI_DA = TRAN_VE - O_MOI_TRANG;
    /** Gia xu theo buoc (oMoRong / 10). Buoc vuot bang: buoc cuoi x 1,5 moi buoc them. */
    public static final long[] GIA_XU = {20000L, 30000L, 45000L, 68000L, 100000L, 150000L, 230000L, 340000L, 510000L};

    private MoRongHanhTrang() {
    }

    /** Suc chua that cua hanh trang. */
    public static int sucChua(final int oMoiTrang, final int soTrang, final int oMoRong) {
        return Math.max(0, oMoiTrang) * Math.max(0, soTrang) + Math.max(0, oMoRong);
    }

    /** So o ma 1 ve / 1 lan mua xu se them (0 = da cham tran, khong dung duoc). */
    public static int soOThem(final int sucChua) {
        return sucChua >= TRAN_VE ? 0 : Math.min(O_MOI_VE, TRAN_VE - sucChua);
    }

    /** Tran hien cho client: 126, hoac suc chua that neu da vuot (mua trang bang luong). */
    public static int tranHienThi(final int sucChua) {
        return Math.max(TRAN_VE, sucChua);
    }

    /** Buoc mo rong da mua (ve + xu deu tinh). */
    public static int buoc(final int oMoRong) {
        return Math.max(0, oMoRong) / O_MOI_VE;
    }

    /** Gia xu cua buoc {@code buoc} (0 = lan dau). */
    public static long giaXu(final int buoc) {
        if (buoc < 0) {
            return GIA_XU[0];
        }
        if (buoc < GIA_XU.length) {
            return GIA_XU[buoc];
        }
        long g = GIA_XU[GIA_XU.length - 1];
        for (int i = GIA_XU.length; i <= buoc && g < 1000000000L; i++) {
            g = g * 3 / 2;
        }
        return g;
    }

    /** Gia xu lan mo ke tiep, -1 neu da cham tran. */
    public static long giaXuKeTiep(final int sucChua, final int oMoRong) {
        return soOThem(sucChua) <= 0 ? -1L : giaXu(buoc(oMoRong));
    }

    /** Kep gia tri doc tu CSDL. */
    public static int kep(final int oMoRong) {
        return Math.max(0, Math.min(O_LE_TOI_DA, oMoRong));
    }

    /** So trang client cu (chi biet trang 42 o) can de thay het do: lam tron len. */
    public static int soTrangHienThi(final int oMoiTrang, final int sucChua) {
        if (oMoiTrang <= 0) {
            return 1;
        }
        return Math.max(1, (sucChua + oMoiTrang - 1) / oMoiTrang);
    }
}
