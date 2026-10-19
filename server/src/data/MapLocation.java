/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

public class MapLocation {
    public int toM;
    public int toX;
    public int toY;
    public int currentMap;
    public int xBoard;
    public int yBoard;
    public String mapName = "";
    public String belongClass = "";
    public static short[] mapID = new short[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 110, 111, 112, 106, 107};
    public static String[] name = new String[]{
        "Làng Sơn Nam",
        "Dao Châu",
        "Tiên Du",
        "Phù Liệt",
        "Kỳ Bố",
        "Hàm Tử",
        "Thạch Giang",
        "Đông Sơn",
        "Tử Quan",
        "Trường Giang",
        "Lộc Trĩ",
        "Sơn Lâm",
        "Hang động",
        "Hang mãng xà",
        "Hang thằn lằn",
        "Khu vực 1",
        "Khu vực 1"
    };

    public MapLocation(int m, int x, int y) {
        this.toM = m;
        this.toX = x;
        this.toY = y;
    }

    public String getMapName() {
        for (int i = 0; i < mapID.length; i++) {
            if (this.toM == mapID[i]) {
                return name[i];
            }
        }

        return null;
    }
}
