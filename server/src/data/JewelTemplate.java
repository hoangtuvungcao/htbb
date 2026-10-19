/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

public class JewelTemplate {
    public byte id;
    public byte[] typeItem;
    public String name = "";
    public String info = "";

    public JewelTemplate(int id, String name, String info, byte[] typeItem) {
        this.id = (byte)id;
        this.name = name;
        this.info = info;
        this.typeItem = typeItem;
    }
}
