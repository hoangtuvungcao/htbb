/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import java.util.Vector;

public class CMLocation {
    public int toM;
    public int toX;
    public int toY;
    public int xOut;
    public int yOut;
    public int xtile;
    public int ytile;

    public CMLocation(int toM, int toX, int toY, int xOut, int yOut) {
        this.toM = toM;
        this.toX = toX;
        this.toY = toY;
        this.xOut = xOut;
        this.yOut = yOut;
    }

    public CMLocation() {
    }

    public static final CMLocation toOtherMapAt(int px, int py, int w, int[] type, Vector<CMLocation> mapchangeLocations) {
        if ((py >> 4) * w + (px >> 4) < type.length && (py >> 4) * w + (px >> 4) >= 0) {
            return type[(py >> 4) * w + (px >> 4)] >= 2000000000 ? mapchangeLocations.elementAt(type[(py >> 4) * w + (px >> 4)] - 2000000000) : null;
        } else {
            return null;
        }
    }
}
