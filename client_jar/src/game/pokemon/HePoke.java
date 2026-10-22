package game.pokemon;

/**
 * He Pokemon phia client cho HIEU UNG / AM THANH (khong anh huong sat thuong - server tinh).
 * Server chi gui ngu hanh cu cho quai, nen client tu tra he theo so Pokedex:
 *  - DEX/HE/HE2: moi loai trong HTBB du_lieu/bang_quai.csv (he1, he2) + 5 dong khoi dau va
 *    nhanh tien hoa Eevee (LoaiPokemonClient). he = ky tu 'a' + so he (0 Thuong .. 17 Tien), '-' = khong co.
 *  - KHAC: bang khac he chuan (com.htbb.pokemon.HePokemon): '2' x2, '1' x1, 'h' x0.5, '0' x0.
 *    heSo() = he so DUNG TRONG GAME (HePokemon.heSoTrongGame: 200 150 100 70 50 30).
 *  - BOSS: id template quai phan_loai = boss.
 *  - MAP: loai ban do theo id cMap (thiet_ke_map.csv): t thi tran/trong nha/thanh, d duong/dong co/
 *    ho/bien/tau, r rung, h hang/nui, s hang cat (sa mac), c dau truong, '.' khong co trong bang.
 * Lop thuan du lieu (khong lcdui) de KiemPokeDuLieu kiem tren JDK.
 */
public final class HePoke {
    public static final int THUONG = 0, LUA = 1, NUOC = 2, DIEN = 3, CO = 4, BANG = 5, GIAC_DAU = 6, DOC = 7,
            DAT = 8, BAY = 9, SIEU_LINH = 10, BO = 11, DA = 12, MA = 13, RONG = 14, BONG_TOI = 15, THEP = 16,
            TIEN = 17;

    static final short[] DEX = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 19, 23, 24, 25, 26, 27, 37, 43, 44, 45, 46, 47, 48, 58, 59, 69, 72,
        76, 77, 78, 83, 85, 92, 93, 94, 99, 104, 105, 112, 123, 127, 128, 130, 133, 134, 135, 136, 145, 159, 160,
        168, 172, 185, 194, 195, 196, 197, 214, 217, 219, 244, 255, 256, 257, 269, 286, 319, 324, 336, 340, 342,
        353, 355, 356, 373, 382, 383, 384, 401, 402, 404, 405, 423, 428, 436, 437, 451, 453, 454, 460, 470, 471,
        477, 491, 496, 497, 498, 510, 536, 537, 543, 544, 545, 552, 559, 560, 562, 596, 607, 637, 667, 681, 700,
        709, 711, 739, 750, 781, 901
    };
    static final String HE = "eeebbbccclllahhddibeeelllbbecmbbaannnciiillacacdbdccldmcckplabbbbblecbhccnnnociollddcaqqhhheefnpeebpcclllippnlnlbqrnngini";
    static final String HE2 = "hhh--j----jh-------hhheeh--hhi--jjhhh---mj--j----j--h--ii--g-m--gghgp--ip---j--j----i-kklggf--------iihhhpgg-dbban-ee--ea";

    static final String[] KHAC = {
        "111111111111h011h1", "1hh122111112h1h121", "12h1h111211121h111", "112hh111021111h111",
        "1h21h11h2h1h21h1h1", "1hh12h1122111121h1", "2111121h1hhh20122h", "1111211hh111hh1102",
        "1212h112101h211121", "111h21211112h111h1", "1111112211h11110h1", "1h1121hh1h211h12hh",
        "121112h1h2121111h1", "011111111121121h11", "1111111111111121h0", "111111h11121121h1h",
        "1hhh121111112111h2", "1h11112h11111122h1"
    };

    static final short[] BOSS = {36, 37, 38, 39, 43, 46, 82, 90, 91, 92, 93, 94, 113, 115, 116, 117, 120, 129, 130, 138};

    static final String MAP = "tdddrdrdddrrthdd.hddrdddhcr.drrr........dhhtrtdtdtsthrtdthdttdt.......t.ttt...ttt.ttt...tttttttttttttrtttdtdtthhhhhhhhh...h.....................................hhhhhh...................................tccccttc................cccc...............................tttttttttttt........ctc";

    private HePoke() {
    }

    private static int tim(int dex) {
        int lo = 0, hi = DEX.length - 1;
        while (lo <= hi) {
            int m = (lo + hi) >>> 1;
            if (DEX[m] < dex) {
                lo = m + 1;
            } else if (DEX[m] > dex) {
                hi = m - 1;
            } else {
                return m;
            }
        }
        return -1;
    }

    /** He 1 cua loai (Thuong neu khong biet). */
    public static int he1(int dex) {
        int i = tim(dex);
        if (i < 0) {
            int b = BangLoai.he1(dex);   // 1.0: loai ngoai bang tinh -> bang loai may chu (-126)
            return b >= 0 && b < 18 ? b : THUONG;
        }
        return HE.charAt(i) - 'a';
    }

    /** He 2 cua loai, -1 neu chi co mot he / khong biet. */
    public static int he2(int dex) {
        int i = tim(dex);
        if (i < 0) {
            int b = BangLoai.he2(dex);
            return b >= 0 && b < 18 ? b : -1;
        }
        char c = HE2.charAt(i);
        return c == '-' ? -1 : c - 'a';
    }

    private static int heSoDon(int danh, int thu) {
        if (danh < 0 || danh >= 18 || thu < 0 || thu >= 18) {
            return 100;
        }
        switch (KHAC[danh].charAt(thu)) {
            case '2':
                return 200;
            case 'h':
                return 50;
            case '0':
                return 0;
            default:
                return 100;
        }
    }

    /** He so phan tram dung trong game (khop server HePokemon.heSoTrongGame): 200 150 100 70 50 30. */
    public static int heSo(int heDanh, int he1, int he2) {
        int kq = heSoDon(heDanh, he1);
        if (he2 >= 0 && he2 < 18 && he2 != he1) {
            kq = kq * heSoDon(heDanh, he2) / 100;
        }
        switch (kq) {
            case 400:
                return 200;
            case 200:
                return 150;
            case 50:
                return 70;
            case 25:
                return 50;
            case 0:
                return 30;
            default:
                return 100;
        }
    }

    /** He so khi he heDanh danh vao Pokemon so dex (0 / khong biet -> 100). */
    public static int heSoVaoDex(int heDanh, int dex) {
        if (dex <= 0 || heDanh < 0) {
            return 100;
        }
        return heSo(heDanh, he1(dex), he2(dex));
    }

    public static boolean laBoss(int idTemplate) {
        for (int i = 0; i < BOSS.length; i++) {
            if (BOSS[i] == idTemplate) {
                return true;
            }
        }
        return false;
    }

    /** Ky tu loai map (xem dau lop), '.' neu khong co trong bang. */
    public static char loaiMap(int idMap) {
        return idMap >= 0 && idMap < MAP.length() ? MAP.charAt(idMap) : '.';
    }
}
