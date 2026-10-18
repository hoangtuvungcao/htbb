package com.htbb.pokemon;

/**
 * Bang loai Pokemon NGUOI CHOI dieu khien (giai doan 1: 5 dong khoi dau).
 *
 * Moi class cu cua game goc (CharManager.KIEM..CUNG_THU) anh xa sang mot dong
 * Pokemon khoi dau, nho vay KHONG phai doi cot `class` trong tob_char hay
 * CreateCharHandler (chi co bytecode trong lib/htbb-core.jar, khong co source).
 * Chi tiet ly do: docs/kien-truc.md, bang "Class -> Pokemon".
 *
 * So hieu loai = so Pokedex quoc gia = ten thu muc sprite PMDCollab
 * (sprite/0004/...). Lop THUAN, kiem thu o test/KiemPokemon.java.
 */
public final class LoaiPokemon {

    /** Khong tien hoa tiep. */
    public static final int KHONG = -1;

    /** Cap Eevee duoc chon nhanh tien hoa (bang vat pham da tien hoa). */
    public static final int CAP_NHANH_EEVEE = 25;

    public static final int EEVEE = 133;

    /** Cac nhanh Eevee nguoi choi duoc chon. */
    public static final int[] NHANH_EEVEE = {134, 135, 136, 196, 197, 470, 471, 700};

    /** Pokemon khoi dau theo class cu: index = CharManager.KIEM(0)..CUNG_THU(4). */
    public static final int[] KHOI_DAU_THEO_CLASS = {4, 1, 133, 7, 172};

    private static final byte K = HePokemon.KHONG_CO;

    /** {dex, he1, he2, capTienHoa, dexSau}. capTienHoa = 0 khi la dang cuoi. */
    private static final int[][] DU_LIEU = {
        // Kiem khach -> Charmander
        {4, HePokemon.LUA, K, 16, 5},
        {5, HePokemon.LUA, K, 36, 6},
        {6, HePokemon.LUA, HePokemon.BAY, 0, KHONG},
        // Chien binh -> Bulbasaur
        {1, HePokemon.CO, HePokemon.DOC, 16, 2},
        {2, HePokemon.CO, HePokemon.DOC, 32, 3},
        {3, HePokemon.CO, HePokemon.DOC, 0, KHONG},
        // Dau si -> Squirtle
        {7, HePokemon.NUOC, K, 16, 8},
        {8, HePokemon.NUOC, K, 36, 9},
        {9, HePokemon.NUOC, K, 0, KHONG},
        // Cung thu -> Pichu
        {172, HePokemon.DIEN, K, 10, 25},
        {25, HePokemon.DIEN, K, 30, 26},
        {26, HePokemon.DIEN, K, 0, KHONG},
        // Phap su -> Eevee (nhanh: xem NHANH_EEVEE, khong tu tien hoa theo cap)
        {133, HePokemon.THUONG, K, 0, KHONG},
        {134, HePokemon.NUOC, K, 0, KHONG},
        {135, HePokemon.DIEN, K, 0, KHONG},
        {136, HePokemon.LUA, K, 0, KHONG},
        {196, HePokemon.SIEU_LINH, K, 0, KHONG},
        {197, HePokemon.BONG_TOI, K, 0, KHONG},
        {470, HePokemon.CO, K, 0, KHONG},
        {471, HePokemon.BANG, K, 0, KHONG},
        {700, HePokemon.TIEN, K, 0, KHONG},
    };

    private static final String[][] TEN = {
        {"4", "Charmander"}, {"5", "Charmeleon"}, {"6", "Charizard"},
        {"1", "Bulbasaur"}, {"2", "Ivysaur"}, {"3", "Venusaur"},
        {"7", "Squirtle"}, {"8", "Wartortle"}, {"9", "Blastoise"},
        {"172", "Pichu"}, {"25", "Pikachu"}, {"26", "Raichu"},
        {"133", "Eevee"}, {"134", "Vaporeon"}, {"135", "Jolteon"}, {"136", "Flareon"},
        {"196", "Espeon"}, {"197", "Umbreon"}, {"470", "Leafeon"}, {"471", "Glaceon"},
        {"700", "Sylveon"},
    };

    private LoaiPokemon() {
    }

    private static int[] dong(final int dex) {
        for (final int[] d : DU_LIEU) {
            if (d[0] == dex) {
                return d;
            }
        }
        return null;
    }

    public static boolean coLoai(final int dex) {
        return dong(dex) != null;
    }

    /** Pokemon khoi dau cua class cu, KHONG neu class ngoai 0..4. */
    public static int khoiDau(final int charClass) {
        if (charClass < 0 || charClass >= KHOI_DAU_THEO_CLASS.length) {
            return KHONG;
        }
        return KHOI_DAU_THEO_CLASS[charClass];
    }

    public static int he1(final int dex) {
        final int[] d = dong(dex);
        return d == null ? HePokemon.THUONG : d[1];
    }

    public static int he2(final int dex) {
        final int[] d = dong(dex);
        return d == null ? HePokemon.KHONG_CO : d[2];
    }

    /**
     * Loai ma {@code dex} tien hoa thanh khi dat {@code capDo}, hoac KHONG.
     * Eevee luon tra ve KHONG: nhanh do nguoi choi chon, xem laNhanhEevee.
     */
    public static int tienHoaKeTiep(final int dex, final int capDo) {
        final int[] d = dong(dex);
        if (d == null || d[3] <= 0 || capDo < d[3]) {
            return KHONG;
        }
        return d[4];
    }

    /**
     * Dang tien hoa dung voi cap do hien tai, di tu dang dau qua nhieu nac.
     * Dung khi nap nhan vat tu CSDL, de cap do nhay vot (GM set level) van ra dung dang.
     */
    public static int dangTheoCap(final int dexDau, final int capDo) {
        int dex = dexDau;
        for (int i = 0; i < 5; i++) {
            final int sau = tienHoaKeTiep(dex, capDo);
            if (sau == KHONG) {
                break;
            }
            dex = sau;
        }
        return dex;
    }

    public static boolean laNhanhEevee(final int dex) {
        for (final int n : NHANH_EEVEE) {
            if (n == dex) {
                return true;
            }
        }
        return false;
    }

    public static String ten(final int dex) {
        final String s = String.valueOf(dex);
        for (final String[] t : TEN) {
            if (t[0].equals(s)) {
                return t[1];
            }
        }
        return "";
    }

    /** So loai trong bang - test dung de bat viec them dong ma quen them ten. */
    public static int soLoai() {
        return DU_LIEU.length;
    }

    public static int soTen() {
        return TEN.length;
    }

    /** Danh sach so dex cua moi loai trong bang (de tool tai sprite doi chieu). */
    public static int[] tatCaDex() {
        final int[] kq = new int[DU_LIEU.length];
        for (int i = 0; i < DU_LIEU.length; i++) {
            kq[i] = DU_LIEU[i][0];
        }
        return kq;
    }

    /** B1: phan tram cong them HP/tan cong theo giai doan tien hoa. */
    public static final int PT_GIAI_DOAN_2 = 8;
    public static final int PT_GIAI_DOAN_3 = 16;
    public static final int PT_NHANH_EEVEE = 12;

    /** Giai doan tien hoa 1..3 cua loai trong bang (nhanh Eevee = 2); ngoai bang = 1. */
    public static int giaiDoan(final int dex) {
        if (laNhanhEevee(dex)) {
            return 2;
        }
        int gd = 1;
        int hienTai = dex;
        for (int lan = 0; lan < 3; lan++) {
            boolean coTruoc = false;
            for (final int[] d : DU_LIEU) {
                if (d[4] == hienTai) {
                    hienTai = d[0];
                    coTruoc = true;
                    break;
                }
            }
            if (!coTruoc) {
                break;
            }
            gd++;
        }
        return gd;
    }

    /**
     * Phan tram cong HP/tan cong cho loai {@code dex} (B1 - can bang theo tien hoa):
     * dang dau 0, dang 2 +8%, dang 3 +16%, nhanh Eevee +12%.
     */
    public static int phanTramGiaiDoan(final int dex) {
        if (laNhanhEevee(dex)) {
            return PT_NHANH_EEVEE;
        }
        switch (giaiDoan(dex)) {
            case 2:
                return PT_GIAI_DOAN_2;
            case 3:
                return PT_GIAI_DOAN_3;
            default:
                return 0;
        }
    }
}
