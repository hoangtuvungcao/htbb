/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

public class HideAttribute {
    public String name = "";
    public byte id;
    public byte maxPer;
    static byte[] pc = new byte[]{0, 1, 2, 3, 4, 5, 3, 5, 7, 9, 10, 11, 12};

    public HideAttribute(String name, int id, int maxpercent) {
        this.name = name;
        this.id = (byte)id;
        this.maxPer = (byte)maxpercent;
    }
}
