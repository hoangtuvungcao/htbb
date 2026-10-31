package real;

import io.Message;

/** Cua so test cho giao dich xu (test/KiemGiaoDichXu): goi thang MapMuaBan.doTrade (goi 66) va dat luong. */
public final class GiaoDichXuKiem {

    private GiaoDichXuKiem() {
    }

    public static void goi66(final Map map, final Char p, final Message m) {
        MapMuaBan.doTrade(map, p, m, 0);
    }

    public static void datLuong(final Char p, final int luong, final int khoa) {
        p.luong = luong;
        p.lockLuong = khoa;
    }

    public static int luong(final Char p) {
        return p.luong;
    }

    public static int luongKhoa(final Char p) {
        return p.lockLuong;
    }
}
