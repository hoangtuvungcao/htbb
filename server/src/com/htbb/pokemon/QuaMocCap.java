package com.htbb.pokemon;

/**
 * Qua moc cap (chu game duyet 19/10/2026): bo gia tri thoi thu nghiem. Qua NPC cap 35 (real.MapXuLyLenh.thuongLenCap35)
 * truoc la 10.000.000 xu (~580 gio thu nhap o cap 35, ~1.300 lan so xu trung vi nguoi choi cap 35 dang giu trong
 * du_lieu/mo_phong_kinh_te.csv). Nay mot gia tri cho moi che do, khop cac moc cap khac (thanh tuu cap trong
 * du_lieu/nhiem_vu_moi.csv: 0 xu, 1..15 luong khoa). Test: test/KiemThuongTanThuVaLenCap35.java.
 */
public final class QuaMocCap {

    private QuaMocCap() {
    }

    /** Xu = von khoi dau (data.Database.XU_KHOI_DAU), ~0,3 gio thu nhap o cap 35 (17.270 xu/gio). */
    public static final long XU_CAP_35 = 5000L;
    /** Luong khoa: giua thanh tuu cap 30 (5) va cap 40 (6). */
    public static final int LUONG_KHOA_CAP_35 = 6;
    /** Nguyen lieu KHOA moi loai cap 6 (nlsc + nlcc), giu nhu thiet ke GD2 2.4. */
    public static final int NGUYEN_LIEU_CAP_35 = 10;
}
