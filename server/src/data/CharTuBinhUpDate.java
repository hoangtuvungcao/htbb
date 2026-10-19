/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

public class CharTuBinhUpDate {
    public int[] data = new int[4];

    public String getinfo() {
        return this.data[0] + "," + this.data[1] + "," + this.data[2] + "," + this.data[3];
    }
}
