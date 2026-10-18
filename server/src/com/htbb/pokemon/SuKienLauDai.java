package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lich su kien 52 tuan (docs/thiet-ke-game.md, du_lieu/su_kien_nam.csv). Lop THUAN.
 *
 * Tuan theo LICH (tuan ISO cua nam: tuan 1 = Tet duong lich), mo luc thu Hai 05:00 cua tuan,
 * dong sau {@code so_ngay}. tob_su_kien chi co MOT cap mo_luc/dong_luc moi ma, nen moi ma chi
 * hen LAN KE TIEP (lan dang dien ra hoac sap toi gan nhat) - job dem goi lai de hen lan sau.
 *
 * Bang duoi CHEP tu su_kien_nam.csv (test/KiemLauDaiGd2 doi chieu tung dong).
 */
public final class SuKienLauDai {

    /** {tuan, so ngay, ma (co the nhieu ma ngan boi dau phay)}. */
    public static final Object[][] LICH = {
        {1, 7, "tetduonglich"}, {2, 7, "moi:tuanhe"}, {3, 7, "moi:congdong"}, {4, 7, "tet2017"},
        {5, 14, "tet2017,tet123"}, {6, 7, "tet2017,tet123"}, {7, 7, "moi:shiny"}, {8, 7, "moi:valentine"},
        {9, 7, "moi:dotkich"}, {10, 7, "quocte83"}, {11, 7, "moi:tuanhe"}, {12, 7, "moi:congdong"},
        {13, 7, "moi:muapvp"}, {14, 7, "moi:pokedex"}, {15, 7, "gioto"}, {16, 7, "moi:chienvung"},
        {17, 7, "moi:3004"}, {18, 7, "moi:tuanhe"}, {19, 7, "moi:congdong"}, {20, 7, "moi:dotkich"},
        {21, 7, "moi:bungphat"}, {22, 7, "moi:thieunhi"}, {23, 14, "he2017"}, {24, 7, "he2017"},
        {25, 7, "moi:congdong"}, {26, 7, "moi:muapvp"}, {27, 7, "moi:tuanhe"}, {28, 7, "moi:dotkich"},
        {29, 7, "moi:hamngucmua"}, {30, 7, "moi:chienvung"}, {31, 7, "moi:congdong"}, {32, 7, "vulan"},
        {33, 7, "moi:tuanhe"}, {34, 7, "moi:shiny"}, {35, 7, "moi:quockhanh"}, {36, 7, "moi:dotkich"},
        {37, 14, "trungthu"}, {38, 7, "trungthu"}, {39, 7, "moi:muapvp"}, {40, 7, "moi:congdong"},
        {41, 7, "moi:2010"}, {42, 7, "moi:tuanhe"}, {43, 14, "halloween,demhalloween"},
        {44, 7, "halloween,demhalloween"}, {45, 7, "moi:pokedex"}, {46, 7, "sinhnhat2011"},
        {47, 7, "blackfriday"}, {48, 7, "moi:dotkich"}, {49, 7, "moi:chienvung"}, {50, 7, "moi:congdong"},
        {51, 7, "noel,demnoel"}, {52, 7, "moi:tongket"},
    };

    /** He cua "tuan he" theo tuan (THIET_KE muc 6: 2 Bang, 11 Co, 18 Lua, 27 Nuoc, 33 Dien, 42 Sieu linh). */
    public static final int[][] HE_TUAN = {
        {2, HePokemon.BANG}, {11, HePokemon.CO}, {18, HePokemon.LUA}, {27, HePokemon.NUOC},
        {33, HePokemon.DIEN}, {42, HePokemon.SIEU_LINH},
    };

    /** Huyen thoai dot kich theo tuan (template boss dung lai: Zapdos/Entei/Darkrai...). */
    public static final String[][] DOT_KICH = {
        {"9", "Zapdos"}, {"20", "Entei"}, {"28", "Darkrai"}, {"36", "Rayquaza"}, {"48", "Groudon và Kyogre"},
    };

    /** Ma moi can them vao SuKien.DANH_MUC (GD2 buoc 5.1). */
    public static final String[] MA_MOI = {
        "moi:congdong", "moi:shiny", "moi:dotkich", "moi:tuanhe", "moi:pokedex", "moi:bungphat",
        "moi:chienvung", "moi:muapvp", "moi:valentine", "moi:3004", "moi:thieunhi", "moi:quockhanh",
        "moi:2010", "moi:hamngucmua", "moi:tongket",
    };

    private SuKienLauDai() {
    }

    /** Millis thu Hai 05:00 cua tuan ISO {@code tuan} nam {@code nam} (gio may). */
    public static long moLuc(final int nam, final int tuan) {
        final Calendar c = com.htbb.pokemon.DongHo.lich();
        c.clear();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setMinimalDaysInFirstWeek(4);
        c.set(Calendar.YEAR, nam);
        c.set(Calendar.WEEK_OF_YEAR, tuan);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 5);
        return c.getTimeInMillis();
    }

    /**
     * Lan ke tiep cua MOI ma: lan co dong_luc > bayGio va mo_luc som nhat (lan dang dien ra
     * truoc lan sap toi). Xet nam nay va nam sau.
     * @return ma -> {mo_luc ms, dong_luc ms}
     */
    public static Map<String, long[]> lanKeTiep(final long bayGio) {
        final Calendar c = com.htbb.pokemon.DongHo.lich();
        c.setTimeInMillis(bayGio);
        final int nam = c.get(Calendar.YEAR);
        final Map<String, long[]> kq = new LinkedHashMap<String, long[]>();
        for (int n = nam - 1; n <= nam + 1; n++) {
            for (final Object[] d : LICH) {
                final long mo = moLuc(n, ((Integer) d[0]).intValue());
                final long dong = mo + ((Integer) d[1]).intValue() * 86400000L;
                if (dong <= bayGio) {
                    continue;
                }
                for (final String ma : ((String) d[2]).split(",")) {
                    final long[] cu = kq.get(ma);
                    if (cu == null || mo < cu[0]) {
                        kq.put(ma, new long[]{mo, dong});
                    }
                }
            }
        }
        return kq;
    }

    /** Tuan ISO cua thoi diem. */
    public static int tuanIso(final long ms) {
        final Calendar c = com.htbb.pokemon.DongHo.lich();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setMinimalDaysInFirstWeek(4);
        c.setTimeInMillis(ms - 5L * 3600000L);
        return c.get(Calendar.WEEK_OF_YEAR);
    }

    /** He cua tuan he dang dien ra (theo tuan ISO), -1 neu tuan nay khong phai tuan he. */
    public static int heTuan(final int tuan) {
        for (final int[] h : HE_TUAN) {
            if (h[0] == tuan) {
                return h[1];
            }
        }
        return -1;
    }

    /** He so nhan shiny theo su kien: tuan shiny x10, ngay cong dong x5. */
    public static int nhanShiny(final boolean tuanShiny, final boolean congDong) {
        if (tuanShiny) {
            return 10;
        }
        return congDong ? 5 : 1;
    }

    /** Tong ngan sach (gio thu nhap) cua cac tuan - de test tran 70. */
    public static List<Integer> tuanCoMa(final String ma) {
        final List<Integer> kq = new ArrayList<Integer>();
        for (final Object[] d : LICH) {
            for (final String m : ((String) d[2]).split(",")) {
                if (m.equals(ma)) {
                    kq.add((Integer) d[0]);
                }
            }
        }
        return kq;
    }
}
