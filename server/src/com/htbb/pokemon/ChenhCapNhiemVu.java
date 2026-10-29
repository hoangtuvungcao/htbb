package com.htbb.pokemon;

/**
 * LUAT "HA QUAI CHENH KHONG QUA N CAP" (nhiem vu kieu 4 - chuoi chinh "Bài một: đánh đúng sức" #29 va cac buoc cung kieu,
 * su kien giet_lech_cap cua engine nhiem vu). Quyet dinh chu game 26/10/2026: giu thuong x3 EXP tan thu, nhung muc tieu
 * phai LUON lam duoc. Truoc: chi tinh quai |cap minh - cap quai| <= N. Huong dan + x3 EXP dua nguoi choi len cap 21 khi moi
 * mo toi Fuchsia (quai cap 15-20) -> moi ban do da mo deu thap hon qua N cap, buoc ket tu cap 21 toi 31 (bot-cap1-40).
 *
 * Nay tinh khi MOT trong hai:
 * <ol>
 *   <li>|cap minh - cap quai| <= N (nhu cu);</li>
 *   <li>quai bi ha o BAI LUYEN CAO NHAT nguoi choi dang vao duoc: ban do luyen (cho Tu dong danh) da mo khoa co quai
 *       thuong manh nhat (cap quai cao nhat cua ban do = cap cao nhat trong moi ban do luyen da mo).</li>
 * </ol>
 * Lop thuan (khong phu thuoc may chu) - real.ChenhCapNhiemVuGame cap cac so tu ban do dang chay.
 */
public final class ChenhCapNhiemVu {

    private ChenhCapNhiemVu() {
    }

    /** Duoi chu muc tieu kieu 4 (bang Nhiem vu, so tay, chu nhiem vu trong CSDL). */
    public static final String DUOI_CHU = " (hoặc ở bãi luyện cao nhất bạn đã mở)";

    /**
     * Quai co duoc tinh khong.
     *
     * @param capNguoi       cap nguoi choi
     * @param capQuai        cap quai bi ha
     * @param chenh          N (deltaLv, thuong 5)
     * @param capBanDoNay    cap quai thuong cao nhat cua ban do noi ha quai (0 = khong phai ban do luyen / khong ro)
     * @param capCaoNhatDaMo cap quai thuong cao nhat trong moi ban do luyen nguoi choi dang vao duoc (0 = khong ro)
     */
    public static boolean tinh(final int capNguoi, final int capQuai, final int chenh, final int capBanDoNay,
            final int capCaoNhatDaMo) {
        if (Math.abs(capNguoi - capQuai) <= chenh) {
            return true;
        }
        return capBanDoNay > 0 && capCaoNhatDaMo > 0 && capBanDoNay >= capCaoNhatDaMo;
    }
}
