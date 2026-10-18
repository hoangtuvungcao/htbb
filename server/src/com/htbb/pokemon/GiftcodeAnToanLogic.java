package com.htbb.pokemon;

/**
 * Kiem tra phan thuong giftcode an toan (yeu cau 17/10/2026, GD2 5.4). Lop THUAN.
 *
 * Cu phap (giong DatabaseGiftCode.applyItemString nhung CHI cho phep 3 loai, deu KHOA):
 *   LUONGKHOA:n        luong khoa (gia tri n)
 *   GEM:id:n           ngoc/nguyen lieu, LUON dat khoa (-1) (gia tri n)
 *   ITEM:id:n          vat pham (Char.createItemSoLuong tao lock = 1) (gia tri 5 x n)
 * Cam: xu, luong (mo), DB/PET/... va moi khoa khac. Ma va phan thuong khong phan biet hoa/thuong.
 */
public final class GiftcodeAnToanLogic {

    public static final int GIA_TRI_ITEM = 5;
    /** Tran gia tri 1 ma (doc de: cau_hinh giftcode.gia_tri_toi_da). */
    public static final int GIA_TRI_TOI_DA = 30;
    /** Ngan sach toan may chu / thang (doc de: giftcode.ngan_sach_thang). */
    public static final int NGAN_SACH_THANG = 20000;
    /** So lan nhap sai lien tiep / tai khoan truoc khi khoa {@link #KHOA_MS}. */
    public static final int SAI_TOI_DA = 5;
    public static final long KHOA_MS = 10L * 60L * 1000L;

    private GiftcodeAnToanLogic() {
    }

    /** Ma chuan hoa: chu thuong, chi a-z 0-9 _ -, 3..32 ky tu; null neu khong hop le. */
    public static String chuanHoaMa(final String s) {
        if (s == null) {
            return null;
        }
        final String m = s.trim().toLowerCase();
        if (m.length() < 3 || m.length() > 32 || !m.matches("[a-z0-9_-]+")) {
            return null;
        }
        return m;
    }

    /**
     * Chuan hoa chuoi phan thuong; null neu co khoa bi cam / so sai. GEM luon ep khoa (-1).
     */
    public static String chuanHoaThuong(final String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        final StringBuilder b = new StringBuilder();
        for (final String phan : s.trim().split(",")) {
            final String[] t = phan.trim().split(":");
            try {
                final String k = t[0].trim().toUpperCase();
                if (k.equals("LUONGKHOA") && t.length == 2) {
                    final int n = Integer.parseInt(t[1].trim());
                    if (n <= 0) {
                        return null;
                    }
                    noi(b, "LUONGKHOA:" + n);
                } else if (k.equals("GEM") && (t.length == 3 || t.length == 4)) {
                    final int id = Integer.parseInt(t[1].trim());
                    final int n = Integer.parseInt(t[2].trim());
                    if (id < 0 || n <= 0) {
                        return null;
                    }
                    noi(b, "GEM:" + id + ":" + n + ":-1");
                } else if (k.equals("ITEM") && t.length == 3) {
                    final int id = Integer.parseInt(t[1].trim());
                    final int n = Integer.parseInt(t[2].trim());
                    if (id < 0 || n <= 0 || n > 10) {
                        return null;
                    }
                    noi(b, "ITEM:" + id + ":" + n);
                } else {
                    return null;
                }
            } catch (final RuntimeException e) {
                return null;
            }
        }
        return b.toString();
    }

    private static void noi(final StringBuilder b, final String s) {
        if (b.length() > 0) {
            b.append(',');
        }
        b.append(s);
    }

    /** Gia tri (don vi luong khoa) cua chuoi DA chuan hoa; -1 neu hong. */
    public static int giaTri(final String chuan) {
        if (chuan == null) {
            return -1;
        }
        long v = 0L;
        for (final String phan : chuan.split(",")) {
            final String[] t = phan.split(":");
            if (t[0].equals("LUONGKHOA")) {
                v += Long.parseLong(t[1]);
            } else if (t[0].equals("GEM")) {
                v += Long.parseLong(t[2]);
            } else if (t[0].equals("ITEM")) {
                v += GIA_TRI_ITEM * Long.parseLong(t[2]);
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, v);
    }

    /** null = hop le; nguoc lai ly do (dung chung cho web tao ma va server phat). */
    public static String kiem(final String phanThuong, final int tranMa) {
        final String c = chuanHoaThuong(phanThuong);
        if (c == null) {
            return "Phần thưởng chỉ được gồm LUONGKHOA:n, GEM:id:n, ITEM:id:n (vật phẩm khoá).";
        }
        if (giaTri(c) > tranMa) {
            return "Giá trị mã vượt trần " + tranMa + ".";
        }
        return null;
    }
}
