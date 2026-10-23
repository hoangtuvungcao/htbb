package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lich su kien 52 tuan (docs/thiet-ke-game.md, tools/luu_tru_sinh_du_lieu/du_lieu/su_kien_nam.csv). Lop THUAN.
 *
 * Tuan theo LICH (tuan ISO cua nam: tuan 1 = Tet duong lich), mo luc thu Hai 05:00 cua tuan,
 * dong sau {@code so_ngay}. tob_su_kien chi co MOT cap mo_luc/dong_luc moi ma, nen moi ma chi
 * hen LAN KE TIEP (lan dang dien ra hoac sap toi gan nhat) - job dem goi lai de hen lan sau.
 *
 * LICH nap tu bang data_su_kien_nam (NGUON DUY NHAT, sinh boi tools/luu_tru_sinh_du_lieu/pokemon/sinh_sql_lau_dai.py) qua
 * com.htbb.dulieu.DuLieuSql luc khoi dong; test nap tu sql/htbb.sql (test/KiemDuLieuSql ghim van tay
 * cua ban chep Java cu).
 */
public final class SuKienLauDai {

    /** {tuan (Integer), so ngay (Integer), ma (String, co the nhieu ma ngan boi dau phay)}; rong cho toi khi nap. */
    public static volatile Object[][] LICH = new Object[0][];

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
