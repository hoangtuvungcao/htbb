package game.pokemon;

/**
 * Vùng ĐỊA LÝ của bản đồ (Kanto / Johto / Hoenn) theo id map - nhãn vùng trên bản đồ nhỏ.
 *
 * Khác "lãnh thổ" (Char.inCountry / nationID): lãnh thổ là phe song song của game gốc (mỗi map có bản sao ở
 * cả hai lãnh thổ), không phải nơi map nằm - nên "Đông Pewter" từng hiện nhãn "Johto". Bảng này theo
 * du_lieu/vung_map.csv (= du_lieu/thiet_ke_map.csv: tên địa danh Pokémon của từng map); client_jar/tools/
 * KiemPokeDuLieu đối chiếu hai bảng và tên địa danh. Map chung (đấu trường, hang lạ, sân khởi đầu...) = -1:
 * không ghi nhãn vùng.
 */
public final class VungMap {
    public static final int KANTO = 0;
    public static final int JOHTO = 1;
    public static final int HOENN = 2;
    public static final String[] TEN = {"Kanto", "Johto", "Hoenn"};

    /** {id map, vùng} - giữ thứ tự id tăng dần. */
    public static final short[][] BANG = {
        {0, KANTO}, {1, KANTO}, {2, KANTO}, {3, KANTO}, {4, KANTO}, {5, KANTO}, {6, KANTO}, {7, KANTO}, {8, KANTO},
        {9, KANTO}, {10, JOHTO}, {11, KANTO}, {13, KANTO}, {14, KANTO}, {15, JOHTO}, {18, JOHTO}, {19, JOHTO},
        {20, JOHTO}, {22, KANTO}, {23, JOHTO}, {24, JOHTO}, {28, JOHTO}, {29, JOHTO}, {30, JOHTO}, {31, JOHTO},
        {43, HOENN}, {44, HOENN}, {45, HOENN}, {46, HOENN}, {47, HOENN}, {48, HOENN}, {49, HOENN}, {50, HOENN},
        {51, HOENN}, {52, HOENN}, {53, HOENN}, {54, HOENN}, {55, HOENN}, {56, HOENN}, {57, HOENN}, {58, HOENN},
        {59, HOENN}, {60, HOENN}, {61, HOENN}, {62, HOENN},
        {70, JOHTO}, {72, JOHTO}, {73, JOHTO}, {74, JOHTO}, {78, JOHTO}, {79, JOHTO},
        {80, JOHTO}, {82, JOHTO}, {83, JOHTO}, {84, JOHTO}, {88, JOHTO}, {89, JOHTO},
        {100, KANTO}, {102, KANTO}, {103, KANTO}, {104, KANTO}, {113, JOHTO}, {114, JOHTO},
        {160, HOENN}, {161, HOENN}, {162, HOENN}, {163, HOENN}, {164, HOENN}, {165, HOENN},
        {201, KANTO}, {260, KANTO}, {261, KANTO}, {262, KANTO}, {263, KANTO}, {264, KANTO}, {265, KANTO},
        {266, KANTO}, {267, KANTO}, {268, KANTO}, {269, KANTO}, {270, KANTO}, {271, KANTO},
        {280, HOENN}, {281, HOENN}, {282, HOENN},
    };

    private VungMap() {
    }

    /** Vùng của map (KANTO / JOHTO / HOENN), -1 = map chung / không rõ. */
    public static int vung(int idMap) {
        int lo = 0, hi = BANG.length - 1;
        while (lo <= hi) {
            int m = (lo + hi) >>> 1;
            int v = BANG[m][0];
            if (v == idMap) {
                return BANG[m][1];
            }
            if (v < idMap) {
                lo = m + 1;
            } else {
                hi = m - 1;
            }
        }
        return -1;
    }

    /** Tên vùng của map, null = không ghi nhãn. */
    public static String ten(int idMap) {
        int v = vung(idMap);
        return v < 0 ? null : TEN[v];
    }
}
