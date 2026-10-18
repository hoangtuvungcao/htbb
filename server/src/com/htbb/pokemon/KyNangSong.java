package com.htbb.pokemon;

import java.util.Random;

/**
 * Ky nang song (docs/thiet-ke-game.md). Lop THUAN.
 *
 *   0 Cau ca      : CHI o ho (BanDoSinhThai.nhanHo: vung nuoc kin, khong cham mep ban do) -> ca Pokemon
 *                   (gap + vay), ngoc trai, rong, berry troi. Ao nho: chi ca thuong; ho lon: them ca hiem.
 *   1 Trong trot  : vuon berry (dung lai he thong trong cay san co, doi ten thanh berry).
 *   2 Khao co     : dao o diem dao trong hang / nui / sa mac / tuyet -> manh da tien hoa, hoa thach;
 *                   phuc hoi hoa thach -> pet hiem; ghep 12 manh -> da tien hoa that; do trang tri.
 *   3 Nau an      : berry + ca -> banh Poke (hoi the luc), ca ri, moi thom.
 *   4 Che thuoc   : berry + nguyen lieu -> thuoc HP/MP that (gioi han me/ngay).
 *
 * Moi ky nang cap 1..50. Moi lan lam ton "the luc sinh hoat" (100/ngay, tich toi da 200) - gioi han
 * ngay qua he nang luong rieng, KHONG an vao nang luong huan luyen (danh quai).
 */
public final class KyNangSong {

    private KyNangSong() {
    }

    public static final int CAU_CA = 0, TRONG_TROT = 1, KHAO_CO = 2, NAU_AN = 3, CHE_THUOC = 4, SO_KY_NANG = 5;
    public static final String[] TEN = {"Câu cá", "Trồng trọt", "Khảo cổ", "Nấu ăn", "Chế thuốc"};

    // ------------------------------------------------------------------ cap / exp

    /** EXP tu cap L len L+1 (L = 1..49): 40 x 1,105^L, lam tron. */
    public static int expLenCap(final int cap) {
        if (cap < 1 || cap >= HeSinhThai.CAP_KY_NANG_TOI_DA) {
            return 0;
        }
        return (int) Math.round(40 * Math.pow(1.105, cap));
    }

    /** Tong EXP tu cap 1 len cap {@code cap}. */
    public static long tongExpDenCap(final int cap) {
        long s = 0;
        for (int l = 1; l < Math.min(cap, HeSinhThai.CAP_KY_NANG_TOI_DA); l++) {
            s += expLenCap(l);
        }
        return s;
    }

    /** Cap tuong ung tong EXP. */
    public static int capTuExp(final long exp) {
        int cap = 1;
        long con = exp;
        while (cap < HeSinhThai.CAP_KY_NANG_TOI_DA && con >= expLenCap(cap)) {
            con -= expLenCap(cap);
            cap++;
        }
        return cap;
    }

    // ------------------------------------------------------------------ the luc sinh hoat

    /**
     * The luc dau ngay moi: phan chua dung hom truoc cong vao, tran THE_LUC_TOI_DA.
     * Nghi tu 2 ngay tro len thi day tran (khong ep dang nhap moi ngay - giong nang luong huan luyen).
     */
    public static int theLucNgayMoi(final int conLai, final int soNgayCach) {
        if (soNgayCach <= 0) {
            return conLai;
        }
        if (soNgayCach >= 2) {
            return HeSinhThai.THE_LUC_TOI_DA;
        }
        return Math.min(HeSinhThai.THE_LUC_TOI_DA, Math.max(0, conLai) + HeSinhThai.THE_LUC_NGAY);
    }

    // ------------------------------------------------------------------ bang cau ca

    /** Ket qua mot lan cau / dao: ma vat lieu, so luong, dex Pokemon gap (0 = khong). */
    public static final class KetQua {
        public final int ma, soLuong, dex;

        public KetQua(final int ma, final int soLuong, final int dex) {
            this.ma = ma;
            this.soLuong = soLuong;
            this.dex = dex;
        }
    }

    public static final int NUOC_NGOT = 1, NUOC_MAN = 2;

    /** {ma vat lieu, dex ca (0 = khong phai ca), trong so, cap ky nang toi thieu, mat na nuoc (1 ngot, 2 man)}. */
    public static final int[][] BANG_CAU_CA = {
        {1, 0, 250, 1, 3},
        {2, 0, 60, 1, 3},   // vo so: co ca trai nuoc ngot (nhiem vu cau ca o ho can)
        {3, 0, 60, 1, 3},
        {30, 0, 40, 1, 1},
        {6, 129, 300, 1, 3},
        {7, 118, 120, 1, 1},
        {8, 60, 100, 1, 1},
        {9, 72, 120, 1, 2},
        {13, 98, 80, 5, 2},
        {15, 223, 60, 10, 2},
        {10, 116, 50, 10, 2},
        {11, 90, 50, 15, 2},
        {12, 120, 50, 15, 2},
        {14, 170, 40, 20, 3},
        {16, 211, 30, 20, 2},
        {17, 222, 20, 25, 2},
        {4, 0, 30, 10, 3},
        {5, 0, 6, 30, 2},
        {18, 349, 8, 30, 1},
        {19, 147, 3, 40, 1},
        {20, 131, 2, 45, 2},
    };

    /** {ma vat lieu, trong so, cap ky nang toi thieu} - dao khao co. */
    public static final int[][] BANG_DAO = {
        {50, 400, 1},
        {65, 150, 1},
        {51, 25, 1}, {52, 25, 1}, {53, 25, 1}, {54, 25, 1}, {55, 25, 1}, {56, 25, 1}, {57, 25, 1},
        {59, 30, 5},
        {58, 20, 10},
        {60, 6, 10}, {61, 6, 10},
        {63, 4, 20}, {64, 4, 20},
        {62, 2, 30},
    };

    /** Vat lieu dang gia (>= 100‰) duoc cong trong so theo cap: x(100 + 4 x cap)/100. */
    static int trongSoTheoCap(final int trongSo, final int gia, final int cap, final boolean moiThom) {
        long w = trongSo;
        if (gia >= 100) {
            w = w * (100 + 4L * cap) / 100;
        }
        if (moiThom && gia >= 30) {
            w *= 2;
        }
        return (int) w;
    }

    /** Mot lan cau o ho lon (giu cho test / mo phong). */
    public static KetQua cauCa(final int nuoc, final int capKyNang, final boolean moiThom, final Random r) {
        return cauCa(nuoc, capKyNang, moiThom, true, r);
    }

    /** Dong co the ra o loai ho nay: ao nho (khong phai ho lon) chi co vat lieu gia < 100‰. */
    static boolean coO(final int[] d, final int nuoc, final int cap, final boolean hoLon) {
        return (d[4] & nuoc) != 0 && cap >= d[3] && (hoLon || VatLieuSinhThai.giaPhanNghin(d[0]) < 100);
    }

    /**
     * Mot lan cau. {@code nuoc} = mat na NUOC_NGOT / NUOC_MAN (ho tren map bien = dam pha: ca hai);
     * {@code hoLon} = ho >= BanDoSinhThai.HO_LON o (ao nho khong co ca hiem); {@code moiThom} = buff.
     */
    public static KetQua cauCa(final int nuoc, final int capKyNang, final boolean moiThom, final boolean hoLon, final Random r) {
        long tong = 0;
        for (final int[] d : BANG_CAU_CA) {
            if (coO(d, nuoc, capKyNang, hoLon)) {
                tong += trongSoTheoCap(d[2], VatLieuSinhThai.giaPhanNghin(d[0]), capKyNang, moiThom);
            }
        }
        long x = (long) (r.nextDouble() * tong);
        for (final int[] d : BANG_CAU_CA) {
            if (coO(d, nuoc, capKyNang, hoLon)) {
                x -= trongSoTheoCap(d[2], VatLieuSinhThai.giaPhanNghin(d[0]), capKyNang, moiThom);
                if (x < 0) {
                    return new KetQua(d[0], 1, d[1]);
                }
            }
        }
        return new KetQua(VatLieuSinhThai.RAC, 1, 0);
    }

    /** Mot lan dao. */
    public static KetQua dao(final int capKyNang, final boolean mayMan, final Random r) {
        long tong = 0;
        for (final int[] d : BANG_DAO) {
            if (capKyNang >= d[2]) {
                tong += trongSoTheoCap(d[1], VatLieuSinhThai.giaPhanNghin(d[0]), capKyNang, mayMan);
            }
        }
        long x = (long) (r.nextDouble() * tong);
        for (final int[] d : BANG_DAO) {
            if (capKyNang >= d[2]) {
                x -= trongSoTheoCap(d[1], VatLieuSinhThai.giaPhanNghin(d[0]), capKyNang, mayMan);
                if (x < 0) {
                    final int so = d[0] == 50 ? 1 + r.nextInt(3) : 1;
                    return new KetQua(d[0], so, 0);
                }
            }
        }
        return new KetQua(50, 1, 0);
    }

    /** Gia tri ky vong (‰ gio) cua 1 lan cau - de test / mo phong. */
    public static double kyVongCauCa(final int nuoc, final int cap, final boolean moiThom) {
        double tong = 0, gt = 0;
        for (final int[] d : BANG_CAU_CA) {
            if ((d[4] & nuoc) != 0 && cap >= d[3]) {
                final int w = trongSoTheoCap(d[2], VatLieuSinhThai.giaPhanNghin(d[0]), cap, moiThom);
                tong += w;
                gt += (double) w * VatLieuSinhThai.giaPhanNghin(d[0]);
            }
        }
        return tong == 0 ? 0 : gt / tong;
    }

    /** Gia tri ky vong (‰ gio) cua 1 lan dao. */
    public static double kyVongDao(final int cap) {
        double tong = 0, gt = 0;
        for (final int[] d : BANG_DAO) {
            if (cap >= d[2]) {
                final int w = trongSoTheoCap(d[1], VatLieuSinhThai.giaPhanNghin(d[0]), cap, false);
                tong += w;
                gt += (double) w * VatLieuSinhThai.giaPhanNghin(d[0]) * (d[0] == 50 ? 2 : 1);
            }
        }
        return tong == 0 ? 0 : gt / tong;
    }

    /** Trung bac khi gap hiem: cap ky nang cang cao, ti le "cau duoc 2" cang lon (toi da 25%). */
    public static int soLuongThuong(final int capKyNang, final Random r) {
        return r.nextInt(100) < Math.min(25, capKyNang / 2) ? 2 : 1;
    }

    // ------------------------------------------------------------------ cong thuc

    /** Cong thuc: ky nang, cap toi thieu, nguyen lieu {ma, so}..., ket qua (ma tui / chuoi thuong), so luong. */
    public static final class CongThuc {
        public final int id, kyNang, capToiThieu;
        public final int[][] nguyenLieu;
        /** Ma vat lieu ra tui (>0) hoac 0 neu ra vat pham that qua {@link #thuong}. */
        public final int ra, soRa;
        /** Chuoi thuong (DatabaseGiftCode.applyItemString) khi {@link #ra} = 0. */
        public final String thuong;
        public final String ten;

        CongThuc(final int id, final String ten, final int kyNang, final int capToiThieu, final int[][] nguyenLieu,
                final int ra, final int soRa, final String thuong) {
            this.id = id;
            this.ten = ten;
            this.kyNang = kyNang;
            this.capToiThieu = capToiThieu;
            this.nguyenLieu = nguyenLieu;
            this.ra = ra;
            this.soRa = soRa;
            this.thuong = thuong;
        }
    }

    /** Id cong thuc = chi so trong mang (KHONG doi thu tu, chi them cuoi). */
    public static final CongThuc[] CONG_THUC = {
        // Nau an
        new CongThuc(0, "Bánh Poké", NAU_AN, 1, new int[][]{{30, 2}, {31, 1}}, VatLieuSinhThai.BANH_POKE, 1, null),
        new CongThuc(1, "Mồi thơm (x3)", NAU_AN, 5, new int[][]{{3, 1}, {15, 1}}, VatLieuSinhThai.MOI_THOM, 3, null),
        new CongThuc(2, "Súp berry", NAU_AN, 10, new int[][]{{32, 3}, {3, 1}}, VatLieuSinhThai.SUP_BERRY, 1, null),
        new CongThuc(3, "Cà ri cá", NAU_AN, 20, new int[][]{{6, 2}, {7, 1}, {36, 1}}, VatLieuSinhThai.CA_RI, 1, null),
        // Che thuoc (ra tui, DUNG moi doi binh thuoc that)
        new CongThuc(4, "Thuốc xịt", CHE_THUOC, 1, new int[][]{{30, 2}, {3, 1}}, 90, 1, null),
        new CongThuc(5, "Nước tinh khiết", CHE_THUOC, 1, new int[][]{{34, 2}, {2, 1}}, 93, 1, null),
        new CongThuc(6, "Siêu thuốc", CHE_THUOC, 10, new int[][]{{36, 2}, {8, 1}}, 91, 1, null),
        new CongThuc(7, "Nước khoáng", CHE_THUOC, 15, new int[][]{{35, 2}, {12, 1}}, 94, 1, null),
        new CongThuc(8, "Thuốc cao cấp", CHE_THUOC, 25, new int[][]{{37, 1}, {36, 2}, {4, 1}}, 92, 1, null),
        // Khao co: ghep manh -> da tien hoa that (id item DaTienHoa.ID_ITEM)
        new CongThuc(9, "Đá Nước", KHAO_CO, 20, new int[][]{{51, 12}}, 0, 1, "ITEM:960:1"),
        new CongThuc(10, "Đá Sấm", KHAO_CO, 20, new int[][]{{52, 12}}, 0, 1, "ITEM:961:1"),
        new CongThuc(11, "Đá Lửa", KHAO_CO, 20, new int[][]{{53, 12}}, 0, 1, "ITEM:962:1"),
        new CongThuc(12, "Đá Mặt Trời", KHAO_CO, 20, new int[][]{{54, 12}}, 0, 1, "ITEM:963:1"),
        new CongThuc(13, "Đá Mặt Trăng", KHAO_CO, 20, new int[][]{{55, 12}}, 0, 1, "ITEM:964:1"),
        new CongThuc(14, "Đá Lá", KHAO_CO, 20, new int[][]{{56, 12}}, 0, 1, "ITEM:965:1"),
        new CongThuc(15, "Đá Băng", KHAO_CO, 20, new int[][]{{57, 12}}, 0, 1, "ITEM:966:1"),
        // Khao co: phuc hoi hoa thach -> pet (id item pet trong PetPokemon.BANG). Nhom noi dung them
        // pet Omanyte/Kabuto/Aerodactyl/Lileep/Anorith thi doi chuoi thuong o day.
        new CongThuc(16, "Phục hồi Hoá thạch Vỏ ốc", KHAO_CO, 10, new int[][]{{60, 1}, {65, 3}}, 0, 1, "ITEM:694:1"),
        new CongThuc(17, "Phục hồi Hoá thạch Mai", KHAO_CO, 10, new int[][]{{61, 1}, {65, 3}}, 0, 1, "ITEM:704:1"),
        new CongThuc(18, "Phục hồi Hoá thạch Rễ", KHAO_CO, 20, new int[][]{{63, 1}, {65, 3}}, 0, 1, "ITEM:689:1"),
        new CongThuc(19, "Phục hồi Hoá thạch Vuốt", KHAO_CO, 20, new int[][]{{64, 1}, {65, 3}}, 0, 1, "ITEM:699:1"),
        new CongThuc(20, "Phục hồi Hổ phách cổ", KHAO_CO, 30, new int[][]{{62, 1}, {65, 5}}, 0, 1, "ITEM:709:1"),
        // Trang tri vuon (chi trang tri)
        new CongThuc(21, "Chậu hoa berry", KHAO_CO, 5, new int[][]{{30, 3}, {33, 2}, {50, 3}}, 110, 1, null),
        new CongThuc(22, "Hồ cá mini", KHAO_CO, 15, new int[][]{{6, 6}, {7, 4}, {2, 5}, {4, 2}}, 111, 1, null),
        new CongThuc(23, "Tượng đá Onix", KHAO_CO, 25, new int[][]{{50, 30}, {65, 5}}, 112, 1, null),
        new CongThuc(24, "Đèn lồng Chinchou", KHAO_CO, 30, new int[][]{{14, 5}, {4, 2}}, 113, 1, null),
        new CongThuc(25, "Bộ sưu tập hoá thạch", KHAO_CO, 40, new int[][]{{60, 1}, {61, 1}, {63, 1}, {64, 1}}, 114, 1, null),
        // Cong dung cho vat lieu cau / dao truoc chi ban NPC (luat "khong item mo coi", tools/pokemon/kiem_item_mo_coi.py).
        // Gia tri dau ra <= tong gia tri nguyen lieu (VatLieuSinhThai ‰ gio): khong tao gia tri moi.
        new CongThuc(26, "Lẩu hải sản (Cà ri cá)", NAU_AN, 25, new int[][]{{9, 2}, {10, 1}, {11, 1}, {13, 1}}, VatLieuSinhThai.CA_RI, 1, null),
        new CongThuc(27, "Mồi thơm biển", NAU_AN, 15, new int[][]{{16, 1}, {1, 3}}, VatLieuSinhThai.MOI_THOM, 1, null),
        new CongThuc(28, "Bể san hô", KHAO_CO, 35, new int[][]{{17, 3}, {5, 1}, {1, 5}}, 115, 1, null),
        new CongThuc(29, "Tượng rồng Dratini", KHAO_CO, 45, new int[][]{{19, 1}, {18, 1}, {20, 1}, {58, 2}, {59, 2}}, 116, 1, null),
    };

    /** Che thuoc toi da bao nhieu me / ngay (thuoc that la hang tieu hao re, tranh thay het ho hut thuoc NPC). */
    public static final int ME_THUOC_TOI_DA_NGAY = 5;

    /** Doi vat lieu thuoc (90..94) -> chuoi thuong binh thuoc that (Char.potions: HP 1/2/3, MP 4/5). */
    public static String thuongDungThuoc(final int ma) {
        switch (ma) {
            case 90:
                return "PT:1:10";
            case 91:
                return "PT:2:10";
            case 92:
                return "PT:3:10";
            case 93:
                return "PT:4:10";
            case 94:
                return "PT:5:10";
            default:
                return null;
        }
    }

    /** Bac cua thanh pham = bac thap nhat trong nguyen lieu da dung (khong "nang bac" vat lieu). */
    public static int bacThanhPham(final int[] bacNguyenLieu) {
        int b = 5;
        for (final int x : bacNguyenLieu) {
            b = Math.min(b, Math.max(1, x));
        }
        return b;
    }

    /** EXP ky nang nhan duoc khi lam: co ban x (1 + gia tri thanh pham/100‰), tran x4. */
    public static int expKhiLam(final int giaPhanNghin) {
        return HeSinhThai.EXP_MOI_LAN * Math.min(4, 1 + giaPhanNghin / 100);
    }
}
