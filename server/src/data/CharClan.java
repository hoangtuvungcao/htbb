/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

public class CharClan {
    public String charname = "";
    public byte rankClan = 3;
    public byte lv = 1;

    public CharClan(String name, int lv) {
        this.charname = name;
        this.lv = (byte)lv;
    }

    public CharClan() {
    }
}
