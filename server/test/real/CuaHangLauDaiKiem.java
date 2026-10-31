package real;

import java.util.List;

/** Cua so test vao CuaHangLauDai (test/KiemLoaiTienCuaHang): chi phi luong khoa tra bang luong khi thieu. */
public final class CuaHangLauDaiKiem {

    private CuaHangLauDaiKiem() {
    }

    public static List<long[]> docChiPhi(final String s) {
        return CuaHangLauDai.docChiPhi(s);
    }

    public static boolean du(final Char p, final List<long[]> chi, final boolean bangLuong) {
        return CuaHangLauDai.du(p, chi, bangLuong);
    }

    public static void tru(final Char p, final List<long[]> chi, final boolean bangLuong) {
        CuaHangLauDai.tru(p, chi, bangLuong);
    }
}
