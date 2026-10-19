package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.List;

/**
 * NGAN SACH CHI SO TRANG BI theo o / cap / muc nang cap (docs/thiet-ke-game.md).
 * Lop THUAN. Dung chung cho: kep chi so luc tinh (real.ChiSoTrangBiGame, chi khi sv.lau_dai), bo kiem
 * du lieu (test/KiemChiSo, tools/pokemon/MoPhongChiSo) va sinh SQL (tools/pokemon/SinhSqlChiSo).
 *
 * CHUAN = chi so (don vi engine, cot data_item.atb0..6) cua mon TRANG +0 trong danh muc 11 bac
 * (data_noi_dung_trang_bi -> data_item, tools/luu_tru_sinh_du_lieu/du_lieu/noi_dung_trang_bi.csv), noi suy tuyen tinh theo cap.
 * Hai dong khan quang bac/vang trong danh muc tro nham vao khung che tao (id 268, 272: thu 75/85, cao hon
 * ca bac bach kim 55) -> chuan dung dong ban thuong cung cap (31 / 41), xem tai lieu muc 1.6.
 *
 * Tran mot mon = CHUAN x 1,30 (Map.rand10 cong toi +25% luc tao + du) x (1 + % nang cap o +p) + 2.
 * Dong ngoai o (o khong co chi so do trong chuan, vd thu tren do cam tay): (3 + cap/10) x (1 + % nang cap).
 * Do bac cu (bac mon &lt; bac theo cap nhan vat): them tran 1,30 x 1,25 x CHUAN(bac hien tai) + 2, tuc mon
 * bac cu nang toi da KHONG BAO GIO hon mon bac hien tai +0 qua 30% (= mon moi o khoang +10).
 */
public final class NganSachTrangBi {

    private NganSachTrangBi() {
    }

    /** So chi so co ban kep (atb0..6: tan cong, thu, ne, chinh xac, chi mang, HP, thu dac biet). */
    public static final int SO_CHI_SO = 7;
    /** He so tran mot mon so voi chuan (phan tram). */
    public static final int HE_SO_MON = 130;
    /** Tran do bac cu so voi mon bac hien tai +0 (phan tram) - "thiet ke" cho muc 3.6. */
    public static final int TRAN_BAC_CU = 130;
    /** Bien do ngau nhien luc tao (Map.rand10: +0..+25%). */
    public static final int NGAU_NHIEN_TOI_DA = 125;
    public static final int DU = 2;

    /** Cap moc cua 11 bac theo o (type data_item). */
    private static final int[] CAP_GIAP = {1, 14, 20, 30, 44, 54, 64, 74, 85, 90, 98};
    private static final int[] CAP_GIAP2 = {1, 14, 24, 34, 44, 54, 64, 74, 85, 90, 98};
    private static final int[] CAP_VU_KHI = {1, 11, 21, 31, 41, 51, 61, 71, 81, 90, 98};
    private static final int[] CAP_PHU_KIEN = {4, 14, 24, 34, 44, 54, 64, 74, 85, 90, 98};
    private static final int[] CAP_PHIEN_DA = {9, 14, 24, 34, 44, 54, 64, 74, 85, 90, 98};
    private static final int[] CAP_RUY_BANG = {1, 10, 20, 35, 40, 50, 60, 70, 80, 90, 98};

    /** Moi o: {cap moc[], {chi so -> 11 gia tri}} . Thu tu chi so: 0 tc, 1 thu, 2 ne, 3 cx, 4 cm, 5 HP, 6 thu db. */
    private static final int[][] CAP = new int[20][];
    private static final int[][][] GIA_TRI = new int[20][][];

    private static void dat(final int type, final int[] cap, final int[]... cot) {
        CAP[type] = cap;
        final int[][] g = new int[SO_CHI_SO][];
        for (int i = 0; i < SO_CHI_SO; i++) {
            g[i] = i < cot.length && cot[i] != null ? cot[i] : null;
        }
        GIA_TRI[type] = g;
    }

    private static int[] v(final int... x) {
        return x;
    }

    static {
        // 0 Khan quang (ao): thu, ne, chinh xac
        dat(0, CAP_GIAP, null, v(10, 25, 31, 41, 55, 65, 75, 85, 95, 99, 105), v(1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                v(1, 3, 3, 4, 4, 5, 5, 5, 5, 6, 6));
        // 1 Dai (quan), 2 Bang do (non): thu, chinh xac
        dat(1, CAP_GIAP2, null, v(7, 22, 32, 42, 52, 62, 72, 82, 90, 98, 106), null, v(0, 2, 2, 3, 3, 4, 4, 4, 4, 5, 5));
        dat(2, CAP_GIAP2, null, v(5, 20, 30, 40, 50, 60, 70, 80, 90, 96, 104), null, v(1, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5));
        // 3..7 Do cam tay theo dong: tan cong, chinh xac (+ chi mang voi Nam cham)
        dat(3, CAP_VU_KHI, v(30, 70, 110, 150, 200, 240, 280, 320, 360, 443, 477), null, null,
                v(1, 4, 8, 8, 8, 8, 8, 8, 8, 8, 8));
        dat(4, CAP_VU_KHI, v(50, 90, 130, 170, 210, 250, 290, 330, 370, 446, 478), null, null,
                v(1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 4));
        dat(5, CAP_VU_KHI, v(70, 110, 150, 190, 225, 265, 305, 345, 385, 459, 490), null, null,
                v(0, 1, 2, 3, 3, 3, 3, 3, 3, 3, 3));
        dat(6, CAP_VU_KHI, v(90, 130, 170, 210, 250, 290, 330, 370, 401, 480, 511), null, null,
                v(0, 1, 2, 3, 3, 3, 3, 3, 3, 3, 3));
        dat(7, CAP_VU_KHI, v(20, 60, 100, 120, 170, 210, 240, 280, 320, 398, 429), null, null,
                v(5, 7, 10, 12, 12, 12, 12, 12, 12, 12, 12), v(1, 3, 5, 7, 7, 7, 7, 7, 7, 7, 7));
        // 8 Huy hieu: tan cong, chinh xac
        dat(8, CAP_PHU_KIEN, v(5, 15, 25, 35, 45, 55, 65, 75, 85, 91, 99), null, null, v(1, 2, 3, 4, 5, 6, 6, 6, 6, 7, 8));
        // 9 Vong co: tan cong, chi mang
        dat(9, CAP_PHU_KIEN, v(7, 17, 27, 37, 47, 57, 67, 77, 85, 92, 100), null, null, null,
                v(1, 2, 3, 4, 4, 5, 5, 5, 5, 6, 6));
        // 10 Long vu toc do: thu, ne
        dat(10, CAP_PHU_KIEN, null, v(12, 22, 32, 42, 52, 62, 72, 82, 87, 97, 104), v(2, 3, 4, 5, 6, 7, 7, 7, 7, 8, 9));
        // 11 Gang dam: thu, chinh xac, chi mang
        dat(11, CAP_PHU_KIEN, null, v(10, 20, 30, 40, 50, 60, 70, 80, 90, 96, 104), null, v(1, 2, 3, 4, 5, 6, 6, 6, 6, 7, 8),
                v(1, 2, 2, 3, 4, 4, 5, 5, 5, 6, 6));
        // 12 Phien da he: chinh xac, chi mang, HP
        dat(12, CAP_PHIEN_DA, null, null, null, v(1, 2, 3, 4, 5, 6, 7, 8, 8, 9, 10), v(2, 2, 3, 4, 4, 5, 6, 7, 7, 8, 8),
                v(2, 3, 5, 7, 9, 11, 13, 14, 15, 17, 19)); // danh muc: chuyen gia 15 > tinh anh 13 -> lam don dieu
        // 19 Ruy bang: thu (va thu dac biet cung muc - ruy bang dac biet doi cho atb1/atb6)
        dat(19, CAP_RUY_BANG, null, v(32, 50, 70, 100, 110, 130, 150, 170, 190, 210, 226));
        // Phong thu dac biet (atb6) cung ngan sach voi phong thu (atb1) tren moi o giap: do "dac biet"
        // (magic_physic 0, do che tao ARMOR_POINT, ruy bang dac biet) dat chi so vao atb6 thay atb1.
        for (final int t : new int[]{0, 1, 2, 10, 11, 19}) {
            GIA_TRI[t][6] = GIA_TRI[t][1];
        }
    }

    /** O trang bi nhan vat co ngan sach (0..12 va 19). Do thu cuoi (14..18), thoi trang, vat pham khac: khong. */
    public static boolean coNganSach(final int type) {
        return type >= 0 && type < CAP.length && CAP[type] != null;
    }

    /** Bac (0..10) cua mot cap theo cap moc cua o {@code type}. */
    public static int bac(final int type, final int cap) {
        if (!coNganSach(type)) {
            return 0;
        }
        final int[] c = CAP[type];
        int b = 0;
        for (int i = 0; i < c.length; i++) {
            if (cap >= c[i]) {
                b = i;
            }
        }
        return b;
    }

    /** Chi so chuan (Trang +0) cua o/chi so o cap {@code cap} - noi suy, ngoai suy deu theo doan cuoi. 0 neu ngoai o. */
    public static double chuan(final int type, final int idx, final int cap) {
        if (!coNganSach(type) || idx < 0 || idx >= SO_CHI_SO || GIA_TRI[type][idx] == null) {
            return 0.0;
        }
        final int[] c = CAP[type];
        final int[] g = GIA_TRI[type][idx];
        if (cap <= c[0]) {
            return g[0];
        }
        for (int i = 1; i < c.length; i++) {
            if (cap <= c[i]) {
                return g[i - 1] + (g[i] - g[i - 1]) * (double) (cap - c[i - 1]) / (c[i] - c[i - 1]);
            }
        }
        final int n = c.length - 1;
        final double doc = (g[n] - g[n - 1]) / (double) (c[n] - c[n - 1]);
        return g[n] + doc * (Math.min(100, cap) - c[n]);
    }

    /** Chi so nay co nam trong ngan sach cua o (gia tri chuan &gt; 0 o it nhat mot bac) khong. */
    public static boolean trongO(final int type, final int idx) {
        if (!coNganSach(type) || idx < 0 || idx >= SO_CHI_SO || GIA_TRI[type][idx] == null) {
            return false;
        }
        for (final int x : GIA_TRI[type][idx]) {
            if (x > 0) {
                return true;
            }
        }
        return false;
    }

    /** He so nang cap (phan tram, 100 = +0). */
    public static int heSoNangCap(final int plus) {
        return 100 + NangCapLauDai.tongChiSoPhanTram(Math.max(0, Math.min(15, plus)));
    }

    /** Tran cua MOT mon (don vi engine). Integer.MAX_VALUE neu o khong co ngan sach. */
    public static int tranMon(final int type, final int idx, final int cap, final int plus) {
        if (!coNganSach(type) || idx < 0 || idx >= SO_CHI_SO) {
            return Integer.MAX_VALUE;
        }
        final double nc = heSoNangCap(plus) / 100.0;
        final double c = chuan(type, idx, cap);
        if (c <= 0.0) {
            // dong ngoai o: cho mot luong nho (thu tren do cam tay, chinh xac tren non...)
            return (int) Math.ceil((3 + Math.max(0, cap) / 10.0) * nc);
        }
        return (int) Math.ceil(c * HE_SO_MON / 100.0 * nc) + DU;
    }

    /** Tran mon bac cu khi nhan vat o cap {@code capNhanVat}. Integer.MAX_VALUE neu mon khong phai bac cu. */
    public static int tranBacCu(final int type, final int idx, final int capMon, final int capNhanVat) {
        if (!coNganSach(type) || bac(type, capMon) >= bac(type, capNhanVat)) {
            return Integer.MAX_VALUE;
        }
        final double moi = chuan(type, idx, CAP[type][bac(type, capNhanVat)]);
        if (moi <= 0.0) {
            return Integer.MAX_VALUE; // ngoai o: da co tranMon
        }
        return (int) Math.ceil(moi * TRAN_BAC_CU / 100.0 * NGAU_NHIEN_TOI_DA / 100.0) + DU;
    }

    /** Gia tri duoc tinh cua chi so {@code giaTri} tren mon (kep ca hai tran). */
    public static int kep(final int type, final int idx, final int capMon, final int plus, final int capNhanVat,
            final int giaTri) {
        if (giaTri <= 0) {
            return giaTri;
        }
        return Math.min(giaTri, Math.min(tranMon(type, idx, capMon, plus), tranBacCu(type, idx, capMon, capNhanVat)));
    }

    /**
     * Kiem MOT dong data_item (mau +0). @return danh sach loi "atbI=gia_tri &gt; tran" (rong = dat).
     * {@code atb9 == 1} (thoi trang: atb4..8 la so hieu hinh) va khung che tao (moi atb0..5 &lt;= 1) bo qua.
     */
    public static List<String> kiemMau(final int type, final int cap, final int[] atb) {
        final List<String> loi = new ArrayList<String>();
        if (!coNganSach(type) || atb == null || atb.length < SO_CHI_SO) {
            return loi;
        }
        if (atb.length > 9 && atb[9] != 0) {
            return loi;
        }
        boolean khung = true;
        for (int i = 0; i < 6; i++) {
            khung &= atb[i] <= 1;
        }
        if (khung) {
            return loi;
        }
        for (int i = 0; i < SO_CHI_SO; i++) {
            final int t = tranMon(type, i, cap, 0);
            if (atb[i] > t) {
                loi.add("atb" + i + "=" + atb[i] + ">" + t);
            }
        }
        return loi;
    }

    /**
     * Ti le (phan tram) mon bac b-1 o +15 (+ ngoc he 10%) so voi mon bac b +0, cung cap moc cua bac b,
     * TRUOC khi kep bac cu - de bao cao (MoPhongChiSo). Chi so chinh cua o (cot co gia tri lon nhat).
     */
    public static double tiLeBacCuChuaKep(final int type, final int b) {
        if (!coNganSach(type) || b < 1 || b >= CAP[type].length) {
            return 0.0;
        }
        final int idx = chiSoChinh(type);
        final double cu = GIA_TRI[type][idx][b - 1] * heSoNangCap(15) / 100.0 * 1.10;
        final double moi = GIA_TRI[type][idx][b];
        return moi <= 0 ? 0.0 : cu / moi;
    }

    /** Nhu tren nhung SAU khi kep bac cu (mon cu dung o cap moc cua bac b). Chi so hien thi duoc tinh tren gia tri trang +0. */
    public static double tiLeBacCuSauKep(final int type, final int b) {
        if (!coNganSach(type) || b < 1 || b >= CAP[type].length) {
            return 0.0;
        }
        final int idx = chiSoChinh(type);
        final int capMoi = CAP[type][b];
        final int cu = (int) Math.round(GIA_TRI[type][idx][b - 1] * heSoNangCap(15) / 100.0 * 1.10);
        final int ke = kep(type, idx, CAP[type][b - 1], 15, capMoi, cu) - DU; // bo du lam tron (+2)
        final double moi = GIA_TRI[type][idx][b];
        // so voi mon moi CUNG do ngau nhien toi da (tran bac cu tinh tren 1,25 x chuan)
        return moi <= 0 ? 0.0 : ke / (moi * NGAU_NHIEN_TOI_DA / 100.0);
    }

    /** Chi so chinh cua o: cot co gia tri chuan lon nhat o bac cuoi. */
    public static int chiSoChinh(final int type) {
        int best = 0;
        int bv = -1;
        for (int i = 0; i < SO_CHI_SO; i++) {
            final int[] g = GIA_TRI[type][i];
            if (g != null && g[g.length - 1] > bv) {
                bv = g[g.length - 1];
                best = i;
            }
        }
        return best;
    }

    /** Cap moc cua bac {@code b} theo o (cho mo phong / bao cao). */
    public static int capMoc(final int type, final int b) {
        return coNganSach(type) && b >= 0 && b < CAP[type].length ? CAP[type][b] : 0;
    }

    /** Gia tri chuan o bac {@code b} (cho bao cao / test). */
    public static int giaTriMoc(final int type, final int idx, final int b) {
        if (!coNganSach(type) || GIA_TRI[type][idx] == null || b < 0 || b >= CAP[type].length) {
            return 0;
        }
        return GIA_TRI[type][idx][b];
    }

    /** Cac o co ngan sach. */
    public static int[] cacO() {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 19};
    }
}
