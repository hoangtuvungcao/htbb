package com.htbb.pokemon;

/**
 * Tiền tệ trong CHỮ THUẦN gửi người chơi (thông báo, menu, chat - không vẽ được biểu tượng): tên đầy đủ "xu" /
 * "lượng" / "lượng khoá" (không viết tắt "$", "lk", "lg"), số có dấu chấm nghìn kiểu Việt Nam (1.250.000).
 * Cùng quy ước với client game.pokemon.TienTe.
 */
public final class TienTeChu {
    public static final int XU = 0, LUONG = 1, LUONG_KHOA = 2;
    private static final String[] TEN = {"xu", "lượng", "lượng khoá"};

    private TienTeChu() {
    }

    /** 1250000 -> "1.250.000". */
    public static String so(final long v) {
        final boolean am = v < 0;
        final String s = Long.toString(am ? -v : v);
        if (s.startsWith("-")) {
            return Long.toString(v);
        }
        final StringBuilder b = new StringBuilder();
        final int n = s.length();
        for (int i = 0; i < n; i++) {
            if (i > 0 && (n - i) % 3 == 0) {
                b.append('.');
            }
            b.append(s.charAt(i));
        }
        return (am ? "-" : "") + b;
    }

    public static String ten(final int loai) {
        return loai >= 0 && loai < TEN.length ? TEN[loai] : "";
    }

    /** "1.250 xu", "5 lượng", "20 lượng khoá". */
    public static String chu(final int loai, final long v) {
        return so(v) + " " + ten(loai);
    }
}
