package com.htbb.pokemon;

/**
 * Danh muc vat lieu cua he sinh thai (docs/pokemon/HE_SINH_THAI.md muc 2-4). Lop THUAN.
 *
 * Vat lieu nam trong "Tui sinh thai" cua nhan vat (luu o tob_hst_nhan_vat, KHONG phai item
 * data_item) - vi vay khong can them template item / goi tin moi cho client, va KHONG dung dai
 * id data_item ma nhom noi dung dang them. Thanh pham cuoi (thuoc, da tien hoa, pet) moi doi ra
 * vat pham that bang chuoi thuong (DatabaseGiftCode.applyItemString).
 *
 * Moi chong vat lieu co BAC 1..5 (theo cap noi lay duoc). Gia tham chieu cua 1 cai =
 * GIA[ma] ‰ x xuMoiGio(CAP_BAC[bac]) - "gio choi" nhu THIET_KE_LAU_DAI N1, khong so co dinh.
 * Khoa tui = ma * 10 + bac.
 *
 * MA KHONG BAO GIO DUNG LAI (da luu trong CSDL nguoi choi). Them moi thi them cuoi.
 */
public final class VatLieuSinhThai {

    private VatLieuSinhThai() {
    }

    public static final int L_RAC = 0, L_CA = 1, L_BERRY = 2, L_KHOANG = 3, L_HOA_THACH = 4, L_MON_AN = 5,
            L_THUOC = 6, L_TRUNG = 7, L_TRANG_TRI = 8;
    public static final String[] TEN_LOAI = {"Tạp vật", "Cá", "Berry", "Khoáng", "Hoá thạch", "Món ăn", "Thuốc",
        "Trứng", "Trang trí"};

    /** Cap tham chieu cua bac 1..5 (chi so 0 khong dung). */
    public static final int[] CAP_BAC = {0, 15, 35, 55, 75, 90};

    /** Bac theo cap nguoi choi: 1-19 -> 1, 20-39 -> 2, ... 80+ -> 5. */
    public static int bacTheoCap(final int cap) {
        return Math.max(1, Math.min(5, 1 + cap / 20));
    }

    // ma: {loai, gia phan nghin gio}; ten rieng o TEN. Ma 0 khong dung.
    static final int[][] DU_LIEU = new int[120][];
    static final String[] TEN = new String[120];

    private static void dn(final int ma, final String ten, final int loai, final int giaPhanNghin) {
        DU_LIEU[ma] = new int[]{loai, giaPhanNghin};
        TEN[ma] = ten;
    }

    // ---- ma co dinh (dung trong code) ----
    public static final int RAC = 1;
    public static final int BANH_POKE = 80, SUP_BERRY = 81, CA_RI = 82, MOI_THOM = 83;
    public static final int TRUNG = 100;

    static {
        // Cau ca (1-29)
        dn(1, "Rác trôi", L_RAC, 1);
        dn(2, "Vỏ sò", L_RAC, 8);
        dn(3, "Rong biển", L_RAC, 6);
        dn(4, "Ngọc trai nhỏ", L_KHOANG, 40);
        dn(5, "Ngọc trai lớn", L_KHOANG, 150);
        dn(6, "Vảy Magikarp", L_CA, 6);
        dn(7, "Vảy Goldeen", L_CA, 8);
        dn(8, "Vảy Poliwag", L_CA, 8);
        dn(9, "Xúc tu Tentacool", L_CA, 10);
        dn(10, "Vảy Horsea", L_CA, 20);
        dn(11, "Vỏ Shellder", L_CA, 20);
        dn(12, "Lõi sao Staryu", L_CA, 25);
        dn(13, "Càng Krabby", L_CA, 15);
        dn(14, "Đèn Chinchou", L_CA, 30);
        dn(15, "Vảy Remoraid", L_CA, 15);
        dn(16, "Gai Qwilfish", L_CA, 30);
        dn(17, "San hô Corsola", L_CA, 40);
        dn(18, "Vảy Feebas", L_CA, 120);
        dn(19, "Vảy rồng Dratini", L_CA, 250);
        dn(20, "Mai Lapras", L_CA, 400);
        // Berry (30-39): vuon (trong trot) + cau/dao
        dn(30, "Berry Oran", L_BERRY, 10);
        dn(31, "Berry Pecha", L_BERRY, 10);
        dn(32, "Berry Rawst", L_BERRY, 10);
        dn(33, "Berry Cheri", L_BERRY, 10);
        dn(34, "Berry Chesto", L_BERRY, 12);
        dn(35, "Berry Leppa", L_BERRY, 20);
        dn(36, "Berry Sitrus", L_BERRY, 30);
        dn(37, "Berry Lum", L_BERRY, 40);
        // Khao co (50-59): manh da tien hoa (12 manh -> 1 da that), sao, xuong
        dn(50, "Đá cuội", L_RAC, 2);
        dn(51, "Mảnh Đá Nước", L_KHOANG, 60);
        dn(52, "Mảnh Đá Sấm", L_KHOANG, 60);
        dn(53, "Mảnh Đá Lửa", L_KHOANG, 60);
        dn(54, "Mảnh Đá Mặt Trời", L_KHOANG, 60);
        dn(55, "Mảnh Đá Mặt Trăng", L_KHOANG, 60);
        dn(56, "Mảnh Đá Lá", L_KHOANG, 60);
        dn(57, "Mảnh Đá Băng", L_KHOANG, 60);
        dn(58, "Mảnh sao", L_KHOANG, 120);
        dn(59, "Vảy tim", L_KHOANG, 50);
        // Hoa thach (60-69)
        dn(60, "Hoá thạch Vỏ ốc", L_HOA_THACH, 300);
        dn(61, "Hoá thạch Mai", L_HOA_THACH, 300);
        dn(62, "Hổ phách cổ", L_HOA_THACH, 600);
        dn(63, "Hoá thạch Rễ", L_HOA_THACH, 300);
        dn(64, "Hoá thạch Vuốt", L_HOA_THACH, 300);
        dn(65, "Xương cổ", L_RAC, 20);
        // Mon an (80-89) - nau an
        dn(80, "Bánh Poké", L_MON_AN, 40);
        dn(81, "Súp berry", L_MON_AN, 30);
        dn(82, "Cà ri cá", L_MON_AN, 60);
        dn(83, "Mồi thơm", L_MON_AN, 30);
        // Thuoc (90-99) - che thuoc; DUNG -> doi ra binh thuoc that (Char.potions)
        dn(90, "Thuốc xịt (10 bình HP nhỏ)", L_THUOC, 25);
        dn(91, "Siêu thuốc (10 bình HP vừa)", L_THUOC, 50);
        dn(92, "Thuốc cao cấp (10 bình HP lớn)", L_THUOC, 90);
        dn(93, "Nước tinh khiết (10 bình MP nhỏ)", L_THUOC, 25);
        dn(94, "Nước khoáng (10 bình MP vừa)", L_THUOC, 50);
        // Trung (100): moi qua trung co du lieu rieng (NhaTre.Trung), khong chong
        dn(100, "Trứng Pokémon", L_TRUNG, 400);
        // Trang tri vuon (110-119): chi trang tri, khong chi so
        dn(110, "Chậu hoa berry", L_TRANG_TRI, 200);
        dn(111, "Hồ cá mini", L_TRANG_TRI, 400);
        dn(112, "Tượng đá Onix", L_TRANG_TRI, 800);
        dn(113, "Đèn lồng Chinchou", L_TRANG_TRI, 600);
        dn(114, "Bộ sưu tập hoá thạch", L_TRANG_TRI, 1500);
    }

    public static boolean coMa(final int ma) {
        return ma > 0 && ma < DU_LIEU.length && DU_LIEU[ma] != null;
    }

    public static String ten(final int ma) {
        return coMa(ma) ? TEN[ma] : ("#" + ma);
    }

    public static int loai(final int ma) {
        return coMa(ma) ? DU_LIEU[ma][0] : -1;
    }

    /** Gia tham chieu ‰ gio. */
    public static int giaPhanNghin(final int ma) {
        return coMa(ma) ? DU_LIEU[ma][1] : 0;
    }

    /** Gia tham chieu bang xu cua 1 cai o bac {@code bac}. */
    public static long giaXu(final int ma, final int bac) {
        final int b = Math.max(1, Math.min(5, bac));
        return Math.max(1L, ThongSoLauDai.xuMoiGio(CAP_BAC[b]) * giaPhanNghin(ma) / 1000L);
    }

    /** Gia NPC mua lai (san). */
    public static long giaNpc(final int ma, final int bac) {
        if (loai(ma) == L_TRANG_TRI || loai(ma) == L_TRUNG) {
            return 0L; // NPC khong mua trang tri / trung (chong rua tien)
        }
        return giaXu(ma, bac) * HeSinhThai.SAN_NPC_PHAN_TRAM / 100L;
    }

    public static int khoa(final int ma, final int bac) {
        return ma * 10 + Math.max(1, Math.min(5, bac));
    }

    public static int maCuaKhoa(final int khoa) {
        return khoa / 10;
    }

    public static int bacCuaKhoa(final int khoa) {
        return khoa % 10;
    }

    public static String tenKhoa(final int khoa) {
        return ten(maCuaKhoa(khoa)) + " (bậc " + bacCuaKhoa(khoa) + ")";
    }

    /** Danh sach moi ma hop le (cho test / menu). */
    public static int[] tatCa() {
        int n = 0;
        for (int i = 1; i < DU_LIEU.length; i++) {
            if (DU_LIEU[i] != null) {
                n++;
            }
        }
        final int[] r = new int[n];
        n = 0;
        for (int i = 1; i < DU_LIEU.length; i++) {
            if (DU_LIEU[i] != null) {
                r[n++] = i;
            }
        }
        return r;
    }

    /** Manh da tien hoa (51..58) -> id item da that (DaTienHoa.ID_ITEM), -1 = khong phai manh. */
    public static int daTuManh(final int ma) {
        if (ma >= 51 && ma <= 57) {
            // 51 Nuoc, 52 Sam, 53 Lua, 54 Mat Troi, 55 Mat Trang, 56 La, 57 Bang = DaTienHoa 0..6
            return DaTienHoa.ID_ITEM[ma - 51];
        }
        return -1;
    }
}
