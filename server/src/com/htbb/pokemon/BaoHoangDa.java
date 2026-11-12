package com.htbb.pokemon;

/**
 * Thong bao + "giu cho nguoi choi" Pokemon hoang da (bao loi 12/11/2026 "Ad check map Mahogany, báo ra Pokémon để bắt
 * mà tìm chả thấy con nào"). Lop THUAN (test: test/KiemBaoHoangDa); noi game o real.LoaiQuaiGame / DanhGiuGame /
 * BatPokemonGame / TuDongGame.
 *
 * Truoc day tin chi ghi "... vừa xuất hiện ở Mahogany": ban do chia 5-10 khu (moi khu, moi phe mot bo quai rieng), tin
 * T5 toan may chu khong noi khu nao, khong loc phe, va con Pokemon bien mat sau 4 phut neu khong ai danh - nguoi choi
 * toi noi thi da het hoac dang o khu khac. Them nua ~100-400 Huan luyen vien AI (bot) treo o cac khu tu dong danh / bat
 * moi con hoang da (bat duoc 4.546 con so voi 907 cua nguoi that, 12/11/2026).
 *
 * Luat moi:
 * <ul>
 * <li>Tin ghi ro noi: "Mahogany khu 3, phía đông bắc" ({@link #noi}, {@link #huong}).</li>
 * <li>Con "danh cho nguoi choi" ({@link #danhChoNguoi}): bac &gt;= {@link #GIU_BAC} (T4 Rất hiếm) hoac sac mau - bot
 * HLV AI khong danh / bat / gay trang thai duoc, Tu dong danh khong tu chon no (tru khi no dang danh minh), song lau hon
 * ({@link #phutBienMat}: {@link #PHUT_BIEN_MAT_HIEM} phut thay vi {@link #PHUT_BIEN_MAT}).</li>
 * </ul>
 */
public final class BaoHoangDa {

    /** Bac tu do Pokemon hoang da "danh cho nguoi choi" (T3 tro xuong bot van bat / danh nhu cu). */
    public static final int GIU_BAC = LoaiDuLieu.T4;
    /** Phut bien mat khi khong ai giao chien - con thuong (ChonLoaiHoangDa.ThamSo.phutBienMat, 0.9.8). */
    public static final int PHUT_BIEN_MAT = 4;
    /** Phut bien mat cua con danh cho nguoi choi - du thoi gian di toi + tim dung khu. */
    public static final int PHUT_BIEN_MAT_HIEM = 10;

    static final String[] HUONG = {"phía tây bắc", "phía bắc", "phía đông bắc", "phía tây", "giữa bản đồ", "phía đông",
        "phía tây nam", "phía nam", "phía đông nam"};

    private BaoHoangDa() {
    }

    /** Con hoang da (dex rieng &gt; 0) bac {@code bac}, sac mau {@code shiny} chi de nguoi choi that danh / bat. */
    public static boolean danhChoNguoi(final int dex, final int bac, final boolean shiny, final int giuBac) {
        if (dex <= 0) {
            return false;
        }
        return shiny || giuBac > 0 && bac >= giuBac;
    }

    /** So phut con hoang da khong ai giao chien thi bien mat. */
    public static int phutBienMat(final boolean danhChoNguoi, final int phutThuong, final int phutHiem) {
        final int t = Math.max(1, phutThuong);
        return danhChoNguoi ? Math.max(t, phutHiem) : t;
    }

    /**
     * Huong cua diem (x, y) px tren ban do {@code wO} x {@code hO} o (o = 16 px), chia luoi 3 x 3. "" neu khong ro kich
     * thuoc ban do.
     */
    public static String huong(final int x, final int y, final int wO, final int hO) {
        if (wO <= 0 || hO <= 0) {
            return "";
        }
        final int cot = Math.max(0, Math.min(2, x * 3 / (wO * 16)));
        final int hang = Math.max(0, Math.min(2, y * 3 / (hO * 16)));
        return HUONG[hang * 3 + cot];
    }

    /**
     * Noi xuat hien: "Mahogany khu 3, phía đông bắc". {@code khu} tinh tu 1 (khu &lt;= 0 = ban do khong chia khu: khong
     * ghi khu); {@code huong} rong = khong ghi.
     */
    public static String noi(final String tenMap, final int khu, final String huong) {
        final StringBuilder s = new StringBuilder(tenMap == null || tenMap.isEmpty() ? "bãi hoang dã" : tenMap);
        if (khu > 0) {
            s.append(" khu ").append(khu);
        }
        if (huong != null && !huong.isEmpty()) {
            s.append(", ").append(huong);
        }
        return s.toString();
    }

    /** "Một Dratini hoang dã (Rất hiếm) vừa xuất hiện ở Mahogany khu 3, phía đông bắc - bắt được bằng Poké Ball!" */
    public static String tinHoangDa(final String tenLoai, final String tenBac, final String noi) {
        return "Một " + tenLoai + " hoang dã (" + tenBac + ") vừa xuất hiện ở " + noi + " - bắt được bằng Poké Ball!";
    }

    /** "Một Dratini sắc màu vừa xuất hiện ở Mahogany khu 3, phía đông bắc!" */
    public static String tinShiny(final String tenLoai, final String noi) {
        return "Một " + tenLoai + " sắc màu vừa xuất hiện ở " + noi + "!";
    }
}
