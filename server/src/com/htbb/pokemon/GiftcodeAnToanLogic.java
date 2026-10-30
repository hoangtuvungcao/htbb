package com.htbb.pokemon;

/**
 * Kiem tra phan thuong giftcode an toan (yeu cau 17/10/2026, GD2 5.4). Lop THUAN.
 *
 * Cu phap (giong DatabaseGiftCode.applyItemString nhung CHI cho phep 4 loai):
 *   LUONGKHOA:n        luong khoa (gia tri n)
 *   GEM:id:n           ngoc/nguyen lieu, LUON dat khoa (-1) (gia tri n)
 *   ITEM:id:n          vat pham (Char.createItemSoLuong tao lock = 1) (gia tri 5 x n, n &lt;= 10)
 *                      - ngoai le: "Ve treo" {@link #ID_VE_TREO} (xep chong, 1 o): n &lt;= {@link #VE_TREO_TOI_DA},
 *                        gia tri {@link #GIA_TRI_VE_TREO} / ve (chi them gio Tu dong danh luc ONLINE, van chiu nang
 *                        luong huan luyen - khong sinh them EXP/xu/do ngoai cach choi tay; gia 0, khoa)
 *   XU:n               xu (31/10/2026, yeu cau chu game cho ma TREO400): 1..{@link #XU_TOI_DA} / ma, gia tri
 *                      ceil(n / {@link #XU_MOI_GIA_TRI}); dem xu_vao kinh te (real.GiftcodeAnToan phat, kieu XU_DEM)
 * Cam: luong (mo), DB/PET/... va moi khoa khac. Ma va phan thuong khong phan biet hoa/thuong.
 * Vi du TREO400 "XU:1000,ITEM:1260:40" = 1 + 40 = 41 (&lt;= giftcode.gia_tri_toi_da tren may chu that = 150).
 */
public final class GiftcodeAnToanLogic {

    public static final int GIA_TRI_ITEM = 5;
    /** Ve treo N gio (com.htbb.pokemon.TuDong.ID_VE_TREO) - xep chong nen phat duoc nhieu hon 10 / ma. */
    public static final int ID_VE_TREO = TuDong.ID_VE_TREO;
    public static final int VE_TREO_TOI_DA = 50;
    public static final int GIA_TRI_VE_TREO = 1;
    /** XU:n - tran moi ma va ty le quy doi gia tri (1 diem / 1.000 xu, lam tron len). */
    public static final int XU_TOI_DA = 50000;
    public static final int XU_MOI_GIA_TRI = 1000;
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
                if (k.equals("XU") && t.length == 2) {
                    final int n = Integer.parseInt(t[1].trim());
                    if (n <= 0 || n > XU_TOI_DA) {
                        return null;
                    }
                    noi(b, "XU:" + n);
                } else if (k.equals("LUONGKHOA") && t.length == 2) {
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
                    if (id < 0 || n <= 0 || n > (id == ID_VE_TREO ? VE_TREO_TOI_DA : 10)) {
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

    /**
     * So o hanh trang TRONG can de nhan du phan thuong (chuoi DA chuan hoa): moi ITEM:id:n la n mon rieng (n o), moi
     * GEM:id chua co trong tui la 1 o (da co thi cong don), LUONGKHOA khong can o. {@code daCoNgoc}: id ngoc -&gt; nguoi
     * nhan da co loai ngoc khoa do chua. Server kiem TRUOC khi danh dau ma (tui day = khong tru luot, khong mat qua).
     */
    public static int soOCan(final String chuan, final java.util.function.IntPredicate daCoNgoc) {
        return soOCan(chuan, daCoNgoc, null);
    }

    /**
     * Nhu tren, them ve treo xep chong: {@code oVe}(n) = so o MOI can de nhan n ve (0 neu don vao chong san co); null =
     * 1 o cho moi ITEM ve treo (chua biet tui).
     */
    public static int soOCan(final String chuan, final java.util.function.IntPredicate daCoNgoc,
            final java.util.function.IntUnaryOperator oVe) {
        if (chuan == null) {
            return 0;
        }
        long o = 0L;
        final java.util.Set<Integer> ngocMoi = new java.util.HashSet<Integer>();
        for (final String phan : chuan.split(",")) {
            final String[] t = phan.split(":");
            try {
                if (t[0].equals("ITEM") && Integer.parseInt(t[1]) == ID_VE_TREO) {
                    o += oVe == null ? 1 : Math.max(0, oVe.applyAsInt(Integer.parseInt(t[2])));
                } else if (t[0].equals("ITEM")) {
                    o += Math.max(0L, Long.parseLong(t[2]));
                } else if (t[0].equals("GEM")) {
                    final int id = Integer.parseInt(t[1]);
                    if ((daCoNgoc == null || !daCoNgoc.test(id)) && ngocMoi.add(Integer.valueOf(id))) {
                        o++;
                    }
                }
            } catch (final RuntimeException ignored) {
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, o);
    }

    /**
     * [chong-vat-pham] Nhu tren nhung MOI ITEM tinh qua {@code oItem}(id, n) = so o MOI can de nhan n mon id (vat pham
     * type 28 don vao chong san co / gop chung mot o - ChongVatPham.soOCan; mon khong chong = n). Cac dong ITEM cung id
     * duoc CONG lai roi hoi mot lan (hai dong cung loai khong cung "vua" mot cho trong chong). null = cach cu (n o).
     * Truoc 31/10/2026 giftcode "ITEM:714:2" (Keo Poke type 28) doi 2 o trong du qua chi vao 1 chong.
     */
    public static int soOCanChong(final String chuan, final java.util.function.IntPredicate daCoNgoc,
            final java.util.function.IntBinaryOperator oItem) {
        if (oItem == null) {
            return soOCan(chuan, daCoNgoc, null);
        }
        if (chuan == null) {
            return 0;
        }
        long o = 0L;
        final java.util.Set<Integer> ngocMoi = new java.util.HashSet<Integer>();
        final java.util.Map<Integer, Long> item = new java.util.LinkedHashMap<Integer, Long>();
        for (final String phan : chuan.split(",")) {
            final String[] t = phan.split(":");
            try {
                if (t[0].equals("ITEM")) {
                    final long n = Math.max(0L, Long.parseLong(t[2]));
                    item.merge(Integer.valueOf(Integer.parseInt(t[1])), Long.valueOf(n), Long::sum);
                } else if (t[0].equals("GEM")) {
                    final int id = Integer.parseInt(t[1]);
                    if ((daCoNgoc == null || !daCoNgoc.test(id)) && ngocMoi.add(Integer.valueOf(id))) {
                        o++;
                    }
                }
            } catch (final RuntimeException ignored) {
            }
        }
        for (final java.util.Map.Entry<Integer, Long> e : item.entrySet()) {
            final int n = (int) Math.min(Integer.MAX_VALUE, e.getValue().longValue());
            o += Math.max(0, oItem.applyAsInt(e.getKey().intValue(), n));
        }
        return (int) Math.min(Integer.MAX_VALUE, o);
    }

    /** Tong XU trong chuoi DA chuan hoa. */
    public static long tongXu(final String chuan) {
        long x = 0L;
        for (final String phan : chuan == null ? new String[0] : chuan.split(",")) {
            final String[] t = phan.split(":");
            if (t[0].equals("XU") && t.length == 2) {
                x += Long.parseLong(t[1]);
            }
        }
        return x;
    }

    /** Tong so ve treo ({@link #ID_VE_TREO}) trong chuoi DA chuan hoa. */
    public static int soVeTreo(final String chuan) {
        int n = 0;
        for (final String phan : chuan == null ? new String[0] : chuan.split(",")) {
            final String[] t = phan.split(":");
            if (t[0].equals("ITEM") && t.length == 3 && Integer.parseInt(t[1]) == ID_VE_TREO) {
                n += Integer.parseInt(t[2]);
            }
        }
        return n;
    }

    /**
     * Phan phat qua DatabaseGiftCode.applyItemString (bo XU va ve treo - noi phat tu xu ly rieng: cong xu va xep
     * chong ve). "" neu khong con gi.
     */
    public static String phanThuongThuong(final String chuan) {
        final StringBuilder b = new StringBuilder();
        for (final String phan : chuan == null ? new String[0] : chuan.split(",")) {
            final String[] t = phan.split(":");
            if (t[0].equals("XU") || t[0].equals("ITEM") && t.length == 3 && Integer.parseInt(t[1]) == ID_VE_TREO) {
                continue;
            }
            noi(b, phan);
        }
        return b.toString();
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
            } else if (t[0].equals("XU")) {
                v += (Long.parseLong(t[1]) + XU_MOI_GIA_TRI - 1) / XU_MOI_GIA_TRI;
            } else if (t[0].equals("GEM")) {
                v += Long.parseLong(t[2]);
            } else if (t[0].equals("ITEM") && Integer.parseInt(t[1]) == ID_VE_TREO) {
                v += GIA_TRI_VE_TREO * Long.parseLong(t[2]);
            } else if (t[0].equals("ITEM")) {
                v += GIA_TRI_ITEM * Long.parseLong(t[2]);
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, v);
    }

    /**
     * Danh sach trang (GiftcodeAnToanDuLieu, sinh tu HTBB-Web/shared/data/giftcode-an-toan.json): moi ITEM phai la
     * tieu hao / nguyen lieu / tin vat (dai VAT_PHAM) hoac trang bi catalog co cap mon <= {@code capNguoiNhan}
     * (web: cap_toi_thieu cua ma; server: cap nhan vat); moi GEM phai co that va khong ban bang luong.
     * {@code capMon}: id item -> cap (data_item.level), -1 neu khong co. Chuoi phai DA chuan hoa.
     * Ban TS: gcKiemVatPham (HTBB-Web/server/utils/giftcodeAnToan.ts). @return null = hop le, khac = ly do.
     */
    public static String kiemVatPham(final String chuan, final int capNguoiNhan, final java.util.function.IntUnaryOperator capMon) {
        if (chuan == null) {
            return "Phần thưởng không hợp lệ.";
        }
        for (final String phan : chuan.split(",")) {
            final String[] t = phan.split(":");
            final int id = t.length > 1 ? Integer.parseInt(t[1]) : -1;
            if (t[0].equals("ITEM")) {
                if (trongDai(id)) {
                    continue;
                }
                if (java.util.Arrays.binarySearch(GiftcodeAnToanDuLieu.TRANG_BI, id) >= 0) {
                    final int cap = capMon.applyAsInt(id);
                    if (cap < 0) {
                        return "Vật phẩm #" + id + " không tồn tại.";
                    }
                    if (cap > capNguoiNhan) {
                        return "Trang bị #" + id + " (cấp " + cap + ") cao hơn cấp người nhận (" + capNguoiNhan + ").";
                    }
                    continue;
                }
                return "Vật phẩm #" + id + " không được phát bằng giftcode (chỉ tiêu hao, nguyên liệu, tín vật, trang bị thường).";
            } else if (t[0].equals("GEM")) {
                if (id < 0 || id > GiftcodeAnToanDuLieu.NGOC_TOI_DA || java.util.Arrays.binarySearch(GiftcodeAnToanDuLieu.NGOC_CAM, id) >= 0) {
                    return "Ngọc #" + id + " không được phát bằng giftcode (không tồn tại hoặc bán bằng lượng).";
                }
            }
        }
        return null;
    }

    static boolean trongDai(final int id) {
        for (final int[] d : GiftcodeAnToanDuLieu.VAT_PHAM) {
            if (id >= d[0] && id <= d[1]) {
                return true;
            }
        }
        return false;
    }

    /** null = hop le; nguoc lai ly do (dung chung cho web tao ma va server phat). */
    public static String kiem(final String phanThuong, final int tranMa) {
        final String c = chuanHoaThuong(phanThuong);
        if (c == null) {
            return "Phần thưởng chỉ được gồm LUONGKHOA:n, XU:n (≤ " + XU_TOI_DA + "), GEM:id:n, ITEM:id:n (vật phẩm khoá, n ≤ 10; vé treo "
                    + ID_VE_TREO + " n ≤ " + VE_TREO_TOI_DA + ").";
        }
        if (giaTri(c) > tranMa) {
            return "Giá trị mã vượt trần " + tranMa + ".";
        }
        return null;
    }
}
