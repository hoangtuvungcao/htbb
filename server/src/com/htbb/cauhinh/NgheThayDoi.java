package com.htbb.cauhinh;

import java.util.Set;

/** Ham goi khi cac khoa cau hinh doi (xem {@link CauHinhTrungTam#dangKyNghe}). */
public interface NgheThayDoi {

    /**
     * @param khoaDoi khoa da doi (chi cac khoa khop mau da dang ky)
     * @param moi     anh chup cau hinh moi (da ap dung) - doc gia tri qua {@code moi.chuoi(k)} /
     *                {@code moi.thanhProperties()}
     */
    void khiDoi(Set<String> khoaDoi, CauHinhTrungTam.AnhChup moi);
}
