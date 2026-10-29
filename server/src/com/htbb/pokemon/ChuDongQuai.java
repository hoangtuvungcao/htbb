package com.htbb.pokemon;

/**
 * Luat quai hoang da CHU DONG danh nguoi choi (can bang cap 29/10/2026, bot 5 dong cap 1-40: guc don cuc o Fuchsia /
 * Cinnabar va tren duong di qua ban do quai thap - "don cuoi" la 4-6 con cung danh trong 1-2 giay). Lop THUAN, kiem o
 * server/test/KiemCanBangCapLop.
 *
 * <ul>
 *   <li><b>Quai xam</b>: quai thap hon nguoi choi tu {@code nguongXam} cap tro len KHONG tu lao vao (van danh tra khi bi
 *       danh - quai da mat mau). Tranh guc khi di ngang ban do cap thap (Combusken cap 18 / Butterfree cap 18 hoi dong
 *       nguoi cap 24-26 o Celadon / Saffron / Fuchsia).</li>
 *   <li><b>Toi da vay</b>: cung luc toi da {@code toiDaVay} quai CHUA bi danh duoc danh mot nguoi choi; con thu
 *       {@code toiDaVay}+1 tro di dung cho (giu muc tieu, khong danh) toi khi bot con. Quai da bi nguoi do danh (mat mau)
 *       luon danh tra. Truoc day "fp" (Region / MapVongLap) gan MOI quai trong 120 px vao nguoi dang bi danh.</li>
 * </ul>
 * Khoa cau_hinh (0 = tat luat): {@code quai.xam_cap} (mac dinh 4), {@code quai.toi_da_vay} (mac dinh 3).
 */
public final class ChuDongQuai {

    public static final int XAM_CAP_MAC_DINH = 4;
    public static final int TOI_DA_VAY_MAC_DINH = 3;
    /** Mot quai "dang danh" nguoi choi neu da ra don trong khoang nay (ms). */
    public static final long CUA_SO_VAY_MS = 3500L;

    private ChuDongQuai() {
    }

    /** Quai cap {@code capQuai} la "xam" voi nguoi cap {@code capNguoi} (khong tu lao vao). nguong <= 0 = tat. */
    public static boolean xam(final int capQuai, final int capNguoi, final int nguong) {
        return nguong > 0 && capQuai <= capNguoi - nguong;
    }

    /**
     * Quai duoc ra don vao nguoi choi khong.
     * @param biDanh quai da mat mau (nguoi choi da danh no - danh tra luon duoc)
     * @param soKhacDangDanh so quai KHAC (chua tinh con nay) da danh nguoi do trong {@link #CUA_SO_VAY_MS}
     */
    public static boolean duocDanh(final int capQuai, final int capNguoi, final boolean biDanh, final int soKhacDangDanh,
            final int nguongXam, final int toiDaVay) {
        if (biDanh) {
            return true;
        }
        if (xam(capQuai, capNguoi, nguongXam)) {
            return false;
        }
        return toiDaVay <= 0 || soKhacDangDanh < toiDaVay;
    }
}
