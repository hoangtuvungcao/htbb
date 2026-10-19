/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class XaphuTemplate {
    public short[] mapID;
    public short[] x;
    public short[] y;
    public short[] price;

    public XaphuTemplate(short[] mapID, short[] x, short[] y, short[] price) {
        this.mapID = mapID;
        this.x = x;
        this.y = y;
        this.price = price;
    }
}
