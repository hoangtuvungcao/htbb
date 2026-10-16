package com.kpah.pokemon;

/**
 * 18 he Pokemon va bang khac he (chuan Gen 6+).
 *
 * Lop THUAN: khong dung Char/Map/CSDL, de kiem thu o test/KiemPokemon.java.
 * Chua noi vao cong thuc sat thuong - viec do lam o giai doan 2 cua
 * docs/pokemon/KE_HOACH_CHUYEN_DOI.md (thay khoi ngu hanh CharChiSo.attackDam
 * va LiveActor.attackDam).
 *
 * Gia tri he la so 0..17 theo dung thu tu hang so ben duoi. KHONG doi thu tu:
 * so nay se luu xuong CSDL (data_monster.he, tob_char) va gui xuong client.
 */
public final class HePokemon {

    public static final byte THUONG = 0;
    public static final byte LUA = 1;
    public static final byte NUOC = 2;
    public static final byte DIEN = 3;
    public static final byte CO = 4;
    public static final byte BANG = 5;
    public static final byte GIAC_DAU = 6;
    public static final byte DOC = 7;
    public static final byte DAT = 8;
    public static final byte BAY = 9;
    public static final byte SIEU_LINH = 10;
    public static final byte BO = 11;
    public static final byte DA = 12;
    public static final byte MA = 13;
    public static final byte RONG = 14;
    public static final byte BONG_TOI = 15;
    public static final byte THEP = 16;
    public static final byte TIEN = 17;

    /** Dung cho he thu hai khi loai chi co mot he. */
    public static final byte KHONG_CO = -1;

    public static final int SO_HE = 18;

    public static final String[] TEN = {
        "Thường", "Lửa", "Nước", "Điện", "Cỏ", "Băng", "Giác đấu", "Độc", "Đất",
        "Bay", "Siêu linh", "Bọ", "Đá", "Ma", "Rồng", "Bóng tối", "Thép", "Tiên"
    };

    /** Ten tieng Anh - khop ten thu muc/ten chieu trong bo asset PMDCollab. */
    public static final String[] TEN_EN = {
        "Normal", "Fire", "Water", "Electric", "Grass", "Ice", "Fighting", "Poison", "Ground",
        "Flying", "Psychic", "Bug", "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy"
    };

    /** KHAC[congKich][phongThu] = he so phan tram: 200, 100, 50 hoac 0. */
    private static final int[][] KHAC = new int[SO_HE][SO_HE];

    static {
        for (int i = 0; i < SO_HE; i++) {
            for (int j = 0; j < SO_HE; j++) {
                KHAC[i][j] = 100;
            }
        }
        dat(THUONG, 50, DA, THEP);
        dat(THUONG, 0, MA);

        dat(LUA, 200, CO, BANG, BO, THEP);
        dat(LUA, 50, LUA, NUOC, DA, RONG);

        dat(NUOC, 200, LUA, DAT, DA);
        dat(NUOC, 50, NUOC, CO, RONG);

        dat(DIEN, 200, NUOC, BAY);
        dat(DIEN, 50, DIEN, CO, RONG);
        dat(DIEN, 0, DAT);

        dat(CO, 200, NUOC, DAT, DA);
        dat(CO, 50, LUA, CO, DOC, BAY, BO, RONG, THEP);

        dat(BANG, 200, CO, DAT, BAY, RONG);
        dat(BANG, 50, LUA, NUOC, BANG, THEP);

        dat(GIAC_DAU, 200, THUONG, BANG, DA, BONG_TOI, THEP);
        dat(GIAC_DAU, 50, DOC, BAY, SIEU_LINH, BO, TIEN);
        dat(GIAC_DAU, 0, MA);

        dat(DOC, 200, CO, TIEN);
        dat(DOC, 50, DOC, DAT, DA, MA);
        dat(DOC, 0, THEP);

        dat(DAT, 200, LUA, DIEN, DOC, DA, THEP);
        dat(DAT, 50, CO, BO);
        dat(DAT, 0, BAY);

        dat(BAY, 200, CO, GIAC_DAU, BO);
        dat(BAY, 50, DIEN, DA, THEP);

        dat(SIEU_LINH, 200, GIAC_DAU, DOC);
        dat(SIEU_LINH, 50, SIEU_LINH, THEP);
        dat(SIEU_LINH, 0, BONG_TOI);

        dat(BO, 200, CO, SIEU_LINH, BONG_TOI);
        dat(BO, 50, LUA, GIAC_DAU, DOC, BAY, MA, THEP, TIEN);

        dat(DA, 200, LUA, BANG, BAY, BO);
        dat(DA, 50, GIAC_DAU, DAT, THEP);

        dat(MA, 200, SIEU_LINH, MA);
        dat(MA, 50, BONG_TOI);
        dat(MA, 0, THUONG);

        dat(RONG, 200, RONG);
        dat(RONG, 50, THEP);
        dat(RONG, 0, TIEN);

        dat(BONG_TOI, 200, SIEU_LINH, MA);
        dat(BONG_TOI, 50, GIAC_DAU, BONG_TOI, TIEN);

        dat(THEP, 200, BANG, DA, TIEN);
        dat(THEP, 50, LUA, NUOC, DIEN, THEP);

        dat(TIEN, 200, GIAC_DAU, RONG, BONG_TOI);
        dat(TIEN, 50, LUA, DOC, THEP);
    }

    private HePokemon() {
    }

    private static void dat(final byte congKich, final int heSo, final byte... phongThu) {
        for (final byte p : phongThu) {
            KHAC[congKich][p] = heSo;
        }
    }

    public static boolean hopLe(final int he) {
        return he >= 0 && he < SO_HE;
    }

    /** He so phan tram khi he {@code congKich} danh vao mot he don. He khong hop le -> 100. */
    public static int heSoDon(final int congKich, final int phongThu) {
        if (!hopLe(congKich) || !hopLe(phongThu)) {
            return 100;
        }
        return KHAC[congKich][phongThu];
    }

    /**
     * He so phan tram CHINH XAC nhu game goc, tinh ca he thu hai:
     * 400, 200, 100, 50, 25 hoac 0. {@code he2} = KHONG_CO neu chi co mot he.
     */
    public static int heSoChinhXac(final int congKich, final int he1, final int he2) {
        int kq = heSoDon(congKich, he1);
        if (hopLe(he2) && he2 != he1) {
            kq = kq * heSoDon(congKich, he2) / 100;
        }
        return kq;
    }

    /**
     * He so phan tram DUNG TRONG GAME (da lam diu cho MMO danh thoi gian thuc).
     *
     * Ly do: nguoi choi KHONG doi duoc Pokemon giua tran nhu game goc, nen x0
     * (mien nhiem) se khien mot so quai/boss khong the danh noi, va x4 one-shot
     * trong PvP. Quyet dinh nay da chot trong ke hoach (muc "Quyet dinh da chot"),
     * muon doi thi sua o day VA cap nhat ke hoach cung luc.
     *
     *   chinh xac:  400  200  100   50   25    0
     *   trong game: 200  150  100   70   50   30
     */
    public static int heSoTrongGame(final int congKich, final int he1, final int he2) {
        switch (heSoChinhXac(congKich, he1, he2)) {
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

    public static String ten(final int he) {
        return hopLe(he) ? TEN[he] : "";
    }
}
