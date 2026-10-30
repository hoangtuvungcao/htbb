package com.htbb.pokemon;

import java.util.HashMap;
import java.util.Map;

/**
 * Bao hiem xui cho vat pham nhiem vu "gom N vat roi tu quai" (nhiem vu kieu 2, data_quest_moi idItemget / idMonskill).
 * Lop THUAN, kiem o server/test/KiemCanBangCapLop.
 *
 * Moi lan nguoi choi ha dung loai quai cua mot muc dang thieu, vat nhiem vu roi voi xac suat {@code tiLe} (phan trieu,
 * real.Monster: 150.000 + x2 / may man). Truoc 29/10/2026 khong co bao hiem: bot dong Lua o "Gom hàng rơi" (#23) con 3/5
 * sau ~50 Litwick (ky vong ~6,7 con / vat). Nay sau {@link #nguong} lan truot LIEN TIEP cho cung vat (theo nhan vat +
 * nhiem vu + vat) thi lan ke tiep chac chan roi; roi (ngau nhien hay bao hiem) thi dat lai dem. Do roi thuong khong doi.
 */
public final class BaoHiemRoiNhiemVu {

    /** Ty le roi goc cua vat nhiem vu (phan trieu) trong real.Monster. */
    public static final int TI_LE_GOC = 150000;

    private final Map<String, Integer> truot = new HashMap<String, Integer>();

    /** So lan truot lien tiep toi da = 2 x so con ky vong moi vat (1e6 / tiLe), it nhat 1. Ti le goc 15% -> 13. */
    public static int nguong(final int tiLePhanTrieu) {
        if (tiLePhanTrieu <= 0) {
            return 1;
        }
        if (tiLePhanTrieu >= 1000000) {
            return 1;
        }
        return Math.max(1, (int) Math.round(2000000.0 / tiLePhanTrieu));
    }

    public static String khoa(final int mainSub, final int idNhiemVu, final int idVat) {
        return mainSub + ":" + idNhiemVu + ":" + idVat;
    }

    /**
     * Mot lan ha quai dung loai cho vat {@code khoa}: {@code trung} = lan quay ngau nhien da trung. true = vat roi (dat lai
     * dem), false = truot (cong dem).
     */
    public synchronized boolean roi(final String khoa, final boolean trung, final int nguong) {
        final Integer c = this.truot.get(khoa);
        final int da = c == null ? 0 : c.intValue();
        if (trung || da + 1 >= nguong) {
            this.truot.remove(khoa);
            return true;
        }
        this.truot.put(khoa, Integer.valueOf(da + 1));
        return false;
    }

    /** So lan truot lien tiep dang dem (0 = chua / vua roi). */
    public synchronized int dangTruot(final String khoa) {
        final Integer c = this.truot.get(khoa);
        return c == null ? 0 : c.intValue();
    }
}
