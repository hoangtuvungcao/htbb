package com.htbb.pokemon;

/**
 * LUAT DIEM LEN CAP (thuan, khong CSDL) - sua bao loi nguoi choi 23/10/2026 "len cap (nhiem vu / goi qua) khong co diem
 * tiem nang". Noi cong diem luc choi la real.MapXuLyLenh.thuongLenCap (qua HocChieuGame.congLenCap); lop nay chi tinh
 * "dang le phai co bao nhieu" de bu cho nhan vat cu luc nap (real.ThuongLenCapNap).
 *
 * <pre>
 * Luat cu (huan_luyen tat), moi cap tu 2 tro len: +5 diem tiem nang tu do, +1 moi chi so (5 chi so), +1 diem ky nang.
 *   Tong diem tiem nang o moc lastLV:   T = 10 x (lastLV - 1)   (= LevelDetail.getBasicPoint(lastLV) + 5 x (lastLV - 1),
 *                                                                cung so voi chot chong sua chi so CharNapDuLieu)
 *   Da co:                              C = (SM - goc) + (KL - goc) + (TT - goc) + (SK - goc) + (MM - goc) + basepoint
 *   Thieu tiem nang:                    max(0, T - C)           -> cong vao basepoint (diem tu do)
 *   Tong diem ky nang:                  K = lastLV - 1
 *   Da tieu:                            S = tong max(0, cap chieu o 0..14) - (chieu 0 da hoc ? 1 : 0)
 *                                           (cap 1 chieu 0 tang qua nhiem vu; hoc o NPC -1 -> 0 khong ton diem)
 *   Thieu ky nang:                      max(0, K - S - skillpoint)
 * </pre>
 * Chi CONG, khong bao gio tru (C &gt; T vi vat pham / do cu: giu nguyen). Bu xong thi lan nap sau thieu = 0 (khong cong
 * lan hai). Luat moi (Diem huan luyen + hoc chieu theo loai): tong suy tu lastLV nen khong the thieu - khong bu.
 */
public final class ThuongLenCap {

    /** Diem tiem nang tu do moi cap (luat cu). */
    public static final int TIEM_NANG_MOI_CAP = 5;
    /** So chi so tu +1 moi cap (SM, KL, TT, SK, MM). */
    public static final int SO_CHI_SO = 5;

    private ThuongLenCap() {
    }

    /** Tong diem tiem nang (tu do + tu cong vao chi so) o moc cap da thuong. */
    public static int tongTiemNang(final int lastLV) {
        return (TIEM_NANG_MOI_CAP + SO_CHI_SO) * Math.max(0, lastLV - 1);
    }

    /** Diem tiem nang con thieu (&gt;= 0). chiSo/goc: 5 chi so SM, KL, TT, SK, MM. */
    public static int thieuTiemNang(final int lastLV, final int[] chiSo, final int[] goc, final int basepoint) {
        if (chiSo == null || goc == null || chiSo.length < SO_CHI_SO || goc.length < SO_CHI_SO) {
            return 0;
        }
        long co = Math.max(0, basepoint);
        for (int i = 0; i < SO_CHI_SO; i++) {
            co += chiSo[i] - goc[i];
        }
        return (int) Math.max(0L, Math.min(Short.MAX_VALUE, tongTiemNang(lastLV) - co));
    }

    /** Diem ky nang da tieu (luat cu). */
    public static int daTieuKyNang(final byte[] skill) {
        if (skill == null) {
            return 0;
        }
        int s = 0;
        for (int j = 0; j < skill.length && j < 15; j++) {
            s += Math.max(0, skill[j]);
        }
        if (skill.length > 0 && skill[0] >= 1) {
            s--; // cap 1 chieu dau tang qua nhiem vu / tu mo
        }
        return Math.max(0, s);
    }

    /** Diem ky nang con thieu (&gt;= 0), luat cu. */
    public static int thieuKyNang(final int lastLV, final byte[] skill, final int skillpoint) {
        final int k = Math.max(0, lastLV - 1);
        return Math.max(0, Math.min(Short.MAX_VALUE, k - daTieuKyNang(skill) - Math.max(0, skillpoint)));
    }
}
