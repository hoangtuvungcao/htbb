package com.kpah.pokemon;

/**
 * Nhan khac he Pokemon vao sat thuong - thay khoi ngu hanh "(he + 2) % 5" cu
 * (CharChiSo.attackDam, LiveActor.attackDam va cac Boss*.attackDam) khi bat
 * CheDoPokemon. Lop THUAN, kiem thu o test/KiemPokemonServer.java.
 *
 * Giai doan 2 (S4): he tan cong = he 1 cua ke danh. Giai doan 3 (K2) se doi
 * thanh he cua chieu - chi can doi tham so goi vao, ham nay giu nguyen.
 */
public final class SatThuongHe {

    private SatThuongHe() {
    }

    /** He so phan tram (30..200) khi he {@code heTanCong} danh vao (he1, he2). */
    public static int heSo(final int heTanCong, final int he1, final int he2) {
        return HePokemon.heSoTrongGame(heTanCong, he1, he2);
    }

    /**
     * Sat thuong {@code dam} sau khi nhan he so khac he. Tinh bang long de dam lon
     * (boss hang trieu) nhan 200 khong tran int; ket qua chan trong mien int.
     */
    public static int apDung(final int dam, final int heTanCong, final int he1, final int he2) {
        final long kq = (long) dam * heSo(heTanCong, he1, he2) / 100L;
        if (kq > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (kq < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) kq;
    }
}
