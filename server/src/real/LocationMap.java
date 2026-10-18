/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class LocationMap {
    int x;
    int y;
    int tilex;
    int tiley;
    int mapout;
    int xout;
    int yout;

    public LocationMap(int x, int y, int tilex, int tiley, int mapout, int xout, int yout) {
        this.x = x;
        this.y = y;
        this.tilex = tilex;
        this.tiley = tiley;
        this.mapout = mapout;
        this.xout = xout;
        this.yout = yout;
    }

    public boolean checkCanChangeMap(int xc, int yc, int xout, int yout, int mapout) {
        return xout == this.xout && yout == this.yout
            ? xc >= this.x * 16 && xc <= (this.x + this.tilex) * 16 && yc >= this.y * 16 && yc <= (this.y + this.tiley) * 16 && this.mapout == mapout
            : false;
    }
}
