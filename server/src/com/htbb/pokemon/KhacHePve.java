package com.htbb.pokemon;

import com.htbb.cauhinh.CauHinhTrungTam;

/**
 * Tran he so khac he o PvE (quyet dinh chu game 31/10/2026 sau do TTK: Pichu chi co chieu Dien gay x0,3 vao quai Dat, Charmander
 * nhan x1,5 / x2 tu quai Nuoc / Da tren buoc giet bat buoc CA-09.1 / CA-10.1 / C42.3... khong di tiep duoc).
 * <ul>
 *   <li>nguoi choi danh quai / boss / truong Gym: he so (HePokemon.heSoTrongGame 30..200) kep vao
 *       [{@code chi_so.khac_he_pve_ra_toi_thieu}, {@code chi_so.khac_he_pve_ra_toi_da}] (mac dinh 60..150);</li>
 *   <li>quai / boss / truong Gym danh nguoi choi: he so &lt;= {@code chi_so.khac_he_pve_vao_toi_da} (mac dinh 130), san giu nhu cu
 *       (mien nhiem x0,3).</li>
 * </ul>
 * PvP (nguoi danh nguoi, dau truong) KHONG qua lop nay. Co -95 (khac / kem / gan mien) van theo he so goc de nguoi choi
 * thay khac he. Gia tri &lt;= 0 = tat dau do. Lop THUAN; noi vao game: real.CharChiSo, real.LiveActor, real.Boss*,
 * real.GymLauDai, TuDongKeHoach (chon chieu). Kiem: server/test/KiemTtkQuai.
 */
public final class KhacHePve {

    public static final String K_RA_TOI_THIEU = "chi_so.khac_he_pve_ra_toi_thieu";
    public static final String K_RA_TOI_DA = "chi_so.khac_he_pve_ra_toi_da";
    public static final String K_VAO_TOI_DA = "chi_so.khac_he_pve_vao_toi_da";
    public static final int RA_TOI_THIEU_MAC_DINH = 60;
    public static final int RA_TOI_DA_MAC_DINH = 150;
    public static final int VAO_TOI_DA_MAC_DINH = 130;

    private KhacHePve() {
    }

    /** He so {@code heSo} (%) kep vao [toiThieu, toiDa]; dau &lt;= 0 la tat. */
    public static int kep(final int heSo, final int toiThieu, final int toiDa) {
        int h = heSo;
        if (toiThieu > 0 && h < toiThieu) {
            h = toiThieu;
        }
        if (toiDa > 0 && h > toiDa) {
            h = toiDa;
        }
        return h;
    }

    public static int raToiThieu() {
        return (int) Math.max(0, Math.min(200, CauHinhTrungTam.so(K_RA_TOI_THIEU, RA_TOI_THIEU_MAC_DINH)));
    }

    public static int raToiDa() {
        return (int) Math.max(0, Math.min(1000, CauHinhTrungTam.so(K_RA_TOI_DA, RA_TOI_DA_MAC_DINH)));
    }

    public static int vaoToiDa() {
        return (int) Math.max(0, Math.min(1000, CauHinhTrungTam.so(K_VAO_TOI_DA, VAO_TOI_DA_MAC_DINH)));
    }

    /** He so nguoi choi danh quai (PvE) theo cau hinh hien tai. */
    public static int ra(final int heSo) {
        return kep(heSo, raToiThieu(), raToiDa());
    }

    /** He so quai danh nguoi choi (PvE) theo cau hinh hien tai (chi tran tren). */
    public static int vao(final int heSo) {
        return kep(heSo, 0, vaoToiDa());
    }

    /** Sat thuong {@code dam} da nhan he so goc {@code heGoc} -> doi sang he so PvE vao (chi giam khi he goc vuot tran). */
    public static int doiVao(final int dam, final int heGoc) {
        final int h = vao(heGoc);
        return h == heGoc || heGoc <= 0 ? dam : (int) Math.max(1L, (long) dam * h / heGoc);
    }
}
