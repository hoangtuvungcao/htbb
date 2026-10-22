package com.htbb.pokemon;

import com.htbb.cauhinh.CauHinhTrungTam;

/**
 * Vat pham / nguyen lieu KHOA (0.9.8, chu game duyet): ban cho NPC o GIA THAP + huy vat pham khoa.
 *
 * <ul>
 *   <li>Do khoa (phan thuong khoa, mua bang luong khoa...) ban NPC duoc voi {@link #K_PHAN_TRAM} % gia NPC thuong (mac
 *       dinh 25% = 1/4, khop client TheVatPham.giaBan chia 4; tran {@link #TRAN_PHAN_TRAM} = 25%): gia NPC thuong da la 5%
 *       gia mau (phi.ban_npc_phan_tram) nen do khoa chi con ~1,25% gia mau - voi xu rat nho, khong mo duong doi luong khoa -> xu (chong clone). Van KHONG giao dich /
 *       vut cho nguoi khac duoc. Do khoa vi da mac (Item.laKhoaDoMac) giu gia NPC thuong nhu truoc (luat khoa-khi-mac).</li>
 *   <li>Binh thuoc van KHONG ban NPC (khong them voi xu) - client ghi ro ly do.</li>
 *   <li>Vat pham nhiem vu khong o hanh trang / nguyen lieu nen khong ban / huy qua duong nay.</li>
 *   <li>Huy: goi 61 (vut vat pham) + byte cuoi 1 = huy do khoa (client 0.9.8; client cu khong gui byte -> van "khong bo
 *       duoc"); goi -23 nguyen lieu co co khoa = huy theo so luong. Tru do thue / dang rao ban; ghi nhat ky.</li>
 * </ul>
 * {@link #K_PHAN_TRAM} = 0: tat ban do khoa (nhu 0.9.7). Lop THUAN (tru doc cau hinh): test/KiemBanDoKhoa.
 */
public final class BanDoKhoa {

    /** % gia NPC thuong khi ban do / nguyen lieu khoa (lop co_dinh, an tren web). */
    public static final String K_PHAN_TRAM = "chong_clone.ban_npc_khoa_phan_tram";
    public static final int MAC_DINH_PHAN_TRAM = 25;
    /** Tran cung: du cau hinh ghi gi cung khong vuot (ngan sach voi xu). */
    public static final int TRAN_PHAN_TRAM = 25;

    /** Loai nhat ky (tob_log_other_item / saveLogItemSellShop). */
    public static final String LOG_BAN = "KHOA_BAN_NPC", LOG_HUY = "KHOA_HUY";

    private BanDoKhoa() {
    }

    /** Kep % ve [0, TRAN_PHAN_TRAM]. */
    public static int kep(final long phanTram) {
        return (int) Math.max(0L, Math.min(TRAN_PHAN_TRAM, phanTram));
    }

    /** % dang ap dung (cau hinh trung tam, mac dinh 25 = 1/4). */
    public static int phanTram() {
        try {
            return kep(CauHinhTrungTam.so(K_PHAN_TRAM, MAC_DINH_PHAN_TRAM));
        } catch (final Throwable t) {
            return MAC_DINH_PHAN_TRAM;
        }
    }

    /** Ban do khoa cho NPC duoc khong (dang bat). */
    public static boolean bat() {
        return phanTram() > 0;
    }

    /** Gia ban do khoa tu gia NPC thuong {@code giaThuong} (lam tron xuong, khong am, khong vuot giaThuong). */
    public static long gia(final long giaThuong, final int phanTram) {
        if (giaThuong <= 0) {
            return 0L;
        }
        return Math.min(giaThuong, giaThuong * kep(phanTram) / 100L);
    }

    public static long gia(final long giaThuong) {
        return gia(giaThuong, phanTram());
    }

    /** Huy duoc vat pham khoa khong: do thue (ndayLoan > 0) / dang rao ban thi khong. */
    public static boolean huyDuoc(final boolean khoa, final boolean doThue, final boolean dangBan) {
        return khoa && !doThue && !dangBan;
    }
}
