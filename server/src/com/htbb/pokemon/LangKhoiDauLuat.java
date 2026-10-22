package com.htbb.pokemon;

/**
 * LANG KHOI DAU theo VUNG chon luc tao nhan vat (nhanh sua-tuyen) - luat thuan, khong I/O. Lop noi game: real.LangKhoiDau.
 *
 * <pre>
 *  Vung (myCountry, client goi o CreateCharScr "Vùng: Kanto / Johto"): 0 Kanto, 1 Johto, 2 Hoenn (dong luc tao nhan vat).
 *  Lang (thi tran khoi dau, ban do nap + ban sao):  Pewter 0 / 301 (Kanto), Blackthorn 70 / 1701 (Johto), Cianwood 80 / 1901.
 *
 *  BANG CU (ma goc, moi noi viet cung {{70,1701},{0,301},{80,1901}} chi so theo myCountry): Kanto -> Blackthorn, Johto ->
 *  Pewter - NGUOC. Bang DUNG: Kanto -> Pewter, Johto -> Blackthorn, Hoenn -> Cianwood (Hoenn chua mo khi tao nhan vat:
 *  CreateCharHandler tu choi vung 2; giu Cianwood nhu cu).
 *  Nhan vat tao TU ban sua (co dong tob_lang_khoi_dau) dung bang dung; nhan vat cu giu bang cu - khong ai bi doi cho.
 * </pre>
 * Kiem thu: test/KiemSuaTuyen.
 */
public final class LangKhoiDauLuat {

    public static final int KANTO = 0;
    public static final int JOHTO = 1;
    public static final int HOENN = 2;
    public static final String[] TEN_VUNG = {"Kanto", "Johto", "Hoenn"};

    /** Ban do nap cua ba lang. */
    public static final int PEWTER = 0;
    public static final int BLACKTHORN = 70;
    public static final int CIANWOOD = 80;

    /** Bang goc (chi so = vung, NGUOC cho Kanto / Johto). */
    static final int[][] BANG_CU = {{70, 1701}, {0, 301}, {80, 1901}};
    /** Bang dung (chi so = vung). */
    static final int[][] BANG_DUNG = {{0, 301}, {70, 1701}, {80, 1901}};
    /** O cong (Map.getLocaltion) theo lang: 0 = cong lang Blackthorn, 1 = Pewter, 2 = Cianwood (chi so goc). */
    static final int[] O_CONG_DUNG = {1, 0, 2};

    /** Diem xuat hien trong lang {x0,y0,...} (4 diem, o) theo ban do nap cua lang. */
    static final int[] VI_TRI_BLACKTHORN = {10, 23, 14, 38, 30, 35, 21, 49};
    static final int[] VI_TRI_PEWTER = {22, 41, 27, 32, 8, 30, 18, 11};
    static final int[] VI_TRI_CIANWOOD = {10, 23, 14, 38, 30, 35, 21, 49};

    private LangKhoiDauLuat() {
    }

    /** Bang lang (chi so = vung) cua mot nhan vat; dungVung = nhan vat tao tu ban sua. Tra ve ban sao (an toan sua). */
    public static int[][] bang(final boolean dungVung) {
        final int[][] g = dungVung ? BANG_DUNG : BANG_CU;
        final int[][] kq = new int[g.length][];
        for (int i = 0; i < g.length; i++) {
            kq[i] = g[i].clone();
        }
        return kq;
    }

    /** Ban do nap cua lang (vd 0 Pewter) cho vung {@code vung}. */
    public static int langCua(final int vung, final boolean dungVung) {
        final int[][] g = dungVung ? BANG_DUNG : BANG_CU;
        return g[kep(vung)][0];
    }

    /** Chi so goc dung cho Map.getLocaltion / getNameMapLocation (cong cac map ve lang). */
    public static int oCong(final int vung, final boolean dungVung) {
        final int v = kep(vung);
        return dungVung ? O_CONG_DUNG[v] : v;
    }

    /** Vung dia ly cua lang (ban do nap): Pewter Kanto, Blackthorn / Cianwood Johto; -1 khong phai lang. */
    public static int vungCuaLang(final int banDoNap) {
        if (banDoNap == PEWTER) {
            return KANTO;
        }
        if (banDoNap == BLACKTHORN || banDoNap == CIANWOOD) {
            return JOHTO;
        }
        return -1;
    }

    /** Ban do nap cua mot id ban do lang (ban sao 301..304 / 1701..1704 / 1901..1904); -1 khong phai lang. */
    public static int napCuaLang(final int mapId) {
        if (mapId == 0 || mapId >= 301 && mapId <= 304) {
            return PEWTER;
        }
        if (mapId == 70 || mapId >= 1701 && mapId <= 1704) {
            return BLACKTHORN;
        }
        if (mapId == 80 || mapId >= 1901 && mapId <= 1904) {
            return CIANWOOD;
        }
        return -1;
    }

    /** Diem xuat hien {x0,y0,..} trong lang cua ban do {@code mapId} (ca ban sao); null = khong phai lang. */
    public static int[] viTri(final int mapId) {
        switch (napCuaLang(mapId)) {
            case PEWTER:
                return VI_TRI_PEWTER.clone();
            case BLACKTHORN:
                return VI_TRI_BLACKTHORN.clone();
            case CIANWOOD:
                return VI_TRI_CIANWOOD.clone();
            default:
                return null;
        }
    }

    /** Bang diem xuat hien (chi so = vung) khop {@link #bang(boolean)}. */
    public static int[][] bangViTri(final boolean dungVung) {
        final int[][] g = bang(dungVung);
        final int[][] kq = new int[g.length][];
        for (int i = 0; i < g.length; i++) {
            kq[i] = viTri(g[i][0]);
        }
        return kq;
    }

    static int kep(final int vung) {
        return Math.max(0, Math.min(2, vung));
    }
}
