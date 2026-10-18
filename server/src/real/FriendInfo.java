/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class FriendInfo {
    public String name;
    public int charID;
    public int level = 1;
    public int clazz = 0;

    public FriendInfo(String name, int charID, int level, int clazz) {
        this.name = name;
        this.charID = charID;
        this.level = level;
        this.clazz = clazz;
    }
}
