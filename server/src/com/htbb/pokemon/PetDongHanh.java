package com.htbb.pokemon;

import java.util.function.BooleanSupplier;
import java.util.function.IntPredicate;

/**
 * Pet dong hanh co ban cho moi nguoi choi (chu game 30/10/2026: "có" - lam pet de co hon de ai cung dung duoc Nha tre).
 * Lop THUAN (khong CSDL, khong mang) - noi game o HeSinhThaiMenu (muc Nha tre) + NhiemVuEngine.thuongKhac.
 *
 * Truoc: Nha tre chi nhan pet dong hanh (PetPokemon.BANG) nhung nguon duy nhat la trung pet roi ~1 / 2.500 quai (chenh
 * <= 5 cap, phai co Poke Ball trong tui, het han 3-15 ngay) + Cho sinh thai (0 tin ban) -> gan nhu khong ai gui duoc.
 *
 * Hai nguon moi (docs/thiet-ke-game.md muc 7.4):
 *   1. MIEN PHI, MOT LAN moi nhan vat: chon 1 trong {@link #LOAI} (pet cap 1, cung item nhu no tu trung - IV hoang da,
 *      khong them chi so nao). Nhan o Nha tre ("Nhận pet đồng hành đầu tiên (miễn phí, 1 lần)") hoac thuong buoc chinh
 *      C07.3 (cap 14, buoc dau dung He sinh thai) - CUNG mot co CSDL (tob_pet_dong_hanh, khoa id_char): danh dau
 *      (INSERT IGNORE) TRUOC, chi phat khi chinh lan nay chen duoc dong -> bam dup / dang nhap lai / nhan vat cu + buoc
 *      nhiem vu khong bao gio ra pet thu hai.
 *   2. MUA BANG XU (ho hut): cung 3 loai, gia {@link #GIA_PHAN_NGHIN}‰H = 1,5 gio thu nhap o cap nguoi mua (don vi
 *      chung cua He sinh thai). TRU TIEN TRUOC, chi phat khi tru duoc.
 * Pet nhan / mua vao tui KHOA (Char.createItemSoLuong lock = 1, nhu pet no tu trung): khong ky gui / giao dich duoc, ban
 * NPC 0 xu (gia mau 0) -> clone khong chuyen duoc gi; Dratini / Rufflet / Ponyta... van chi tu trung (giu do hiem).
 */
public final class PetDongHanh {

    private PetDongHanh() {
    }

    /** Item pet cap 1 cho chon (Hoothoot / Zubat / Cleffa) - 3 dong co ban, no nhanh (300-500 buoc), khong phai dong hiem. */
    public static final int[] LOAI = {689, 704, 699};

    /** Gia mua = 1.500‰ gio thu nhap o cap nguoi mua (cap 20 ~ 1,5 gio danh quai). */
    public static final int GIA_PHAN_NGHIN = 1500;

    /** Khoa thuong_khac cua buoc nhiem vu: "pet_dong_hanh:1" (mo menu chon pet mien phi neu chua nhan). */
    public static final String THUONG_KHAC = "pet_dong_hanh";

    /** Buoc chuoi chinh tang pet (nhan vat moi): C07.3 "Bánh mật ong thơm lừng" (cap 14, nau an - buoc He sinh thai dau). */
    public static final String BUOC_THUONG = "C07.3";

    /** Ket qua nhan / mua. */
    public static final int OK = 0, DA_NHAN = 1, SAI_LOAI = 2, THIEU_TIEN = 3, LOI_PHAT = 4;

    /** Item pet cua lua chon {@code op} (0..LOAI.length-1), -1 = ngoai danh sach. */
    public static int idTheoChon(final int op) {
        return op >= 0 && op < LOAI.length ? LOAI[op] : -1;
    }

    /** Ten loai (Hoothoot...) cua item pet {@code idItem}. */
    public static String ten(final int idItem) {
        return SuuTamSinhThai.tenLoai(PetPokemon.dex(idItem));
    }

    /** Nhan menu chon loai: "Hoothoot (nở trứng sau 400 bước)". */
    public static String nhanChon(final int idItem) {
        final int dex = PetPokemon.dex(idItem);
        return ten(idItem) + " cấp 1 (trứng nở sau " + NhaTre.buocNo(NhaTre.loaiGoc(dex)) + " bước)";
    }

    /** Gia mua theo xu / gio o cap nguoi mua (>= 1). */
    public static long gia(final long xuMoiGio) {
        return Math.max(1L, xuMoiGio * GIA_PHAN_NGHIN / 1000L);
    }

    /**
     * Nhan mien phi lua chon {@code op}. {@code danhDau}: ghi co MOT LAN (INSERT IGNORE ... id_char) - true chi khi lan nay
     * chen duoc dong; {@code phat}: dua item pet vao tui (true = da phat). Thu tu: kiem lua chon -> danh dau -> phat.
     */
    public static int nhanMienPhi(final int op, final BooleanSupplier danhDau, final IntPredicate phat) {
        final int id = idTheoChon(op);
        if (id < 0) {
            return SAI_LOAI;
        }
        if (!danhDau.getAsBoolean()) {
            return DA_NHAN;
        }
        return phat.test(id) ? OK : LOI_PHAT;
    }

    /** Mua lua chon {@code op}: {@code truTien} (tru du gia, nguyen tu) TRUOC, chi phat khi tru duoc. */
    public static int mua(final int op, final BooleanSupplier truTien, final IntPredicate phat) {
        final int id = idTheoChon(op);
        if (id < 0) {
            return SAI_LOAI;
        }
        if (!truTien.getAsBoolean()) {
            return THIEU_TIEN;
        }
        return phat.test(id) ? OK : LOI_PHAT;
    }

    /** thuong_khac co phan "pet_dong_hanh:..." khong. */
    public static boolean coThuong(final String thuongKhac) {
        if (thuongKhac == null) {
            return false;
        }
        for (final String phan : thuongKhac.split(";")) {
            if (phan.trim().startsWith(THUONG_KHAC + ":")) {
                return true;
            }
        }
        return false;
    }
}
