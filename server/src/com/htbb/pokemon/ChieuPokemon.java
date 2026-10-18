package com.htbb.pokemon;

/**
 * He cua tung chieu (viec G7.D1) + STAB. Bang HE chep tay tu cot so_he cua
 * du_lieu/bang_chieu.csv (class x so_hieu_skill) - test/KiemLoiChoiPokemon.java doi
 * chieu TUNG dong CSV voi bang nay. Sua CSV (tools/pokemon/ap_ten_chieu.py) thi sua ca o day.
 *
 * Chi so skill = idSkill ma CharChiSo.attackDam nhan (cung chi so voi
 * CharManager.SKILL_DAM_PERCENT[charClass][idSkill]). Skill ngoai bang (vd skill 9..14 cua
 * class 0) -> KHONG_CO: noi goi giu hanh vi cu (he 1 cua nguoi danh, khong STAB).
 *
 * Lop THUAN (khong Char/Map/CSDL).
 */
public final class ChieuPokemon {

    /** Khong co trong bang. */
    public static final int KHONG_CO = -1;

    /** STAB (Same Type Attack Bonus): chieu cung he voi nguoi danh -> x1.2 (phan tram). */
    public static final int PT_STAB = 120;

    /** HE[class][so_hieu_skill] = he chieu 0..17 (HePokemon). */
    private static final int[][] HE = {
        // 0 Charmander: Cao, Than hong, Nanh lua, Xoay lua, ..., Rong (7)
        {0, 1, 1, 1, 0, 0, 1, 14, 1},
        // 1 Bulbasaur
        {0, 4, 4, 7, 0, 0, 4, 4, 4},
        // 2 Eevee (11 skill)
        {0, 0, 15, 0, 0, 10, 0, 13, 16, 0, 0},
        // 3 Squirtle
        {0, 2, 2, 2, 0, 2, 2, 2, 2},
        // 4 Pikachu
        {3, 3, 3, 3, 10, 3, 3, 3, 3},
    };

    private ChieuPokemon() {
    }

    /** So skill co trong bang cua {@code charClass} (0 neu class ngoai bang). */
    public static int soChieu(final int charClass) {
        if (charClass < 0 || charClass >= HE.length) {
            return 0;
        }
        return HE[charClass].length;
    }

    /** He cua skill {@code idSkill} thuoc {@code charClass}, KHONG_CO neu ngoai bang. */
    public static int heChieu(final int charClass, final int idSkill) {
        if (charClass < 0 || charClass >= HE.length || idSkill < 0 || idSkill >= HE[charClass].length) {
            return KHONG_CO;
        }
        return HE[charClass][idSkill];
    }

    /** Chieu he {@code heChieu} co cung he voi nguoi danh (he1/he2) khong. */
    public static boolean coStab(final int heChieu, final int he1, final int he2) {
        return heChieu >= 0 && (heChieu == he1 || heChieu == he2);
    }

    /** 120 neu co STAB, nguoc lai 100. */
    public static int heSoStab(final int heChieu, final int he1, final int he2) {
        return coStab(heChieu, he1, he2) ? PT_STAB : 100;
    }

    /**
     * Sat thuong sau khac he + STAB cua nhan vat danh bang skill {@code idSkill}.
     * Skill ngoai bang: dung he 1 cua nguoi danh, KHONG STAB (= dung hanh vi S4 cu).
     * Tinh bang long, chan trong mien int (dam boss rat lon).
     */
    public static int apDung(final int dam, final int charClass, final int idSkill,
            final int heDanh1, final int heDanh2, final int heThu1, final int heThu2) {
        final int heC = heChieu(charClass, idSkill);
        if (heC == KHONG_CO) {
            return SatThuongHe.apDung(dam, heDanh1, heThu1, heThu2);
        }
        final long kq = (long) dam * SatThuongHe.heSo(heC, heThu1, heThu2) / 100L
                * heSoStab(heC, heDanh1, heDanh2) / 100L;
        if (kq > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (kq < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) kq;
    }
}
