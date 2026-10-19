/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

public class SellerInfo {
    public short sellID;
    public short itemID;
    public short npcID;
    public short shopID;
    public byte typeItem = 0;
    public long priceSell = 0L;

    public SellerInfo(int sellID, int itemID, int npcID, int shopID, int type) {
        this.sellID = (short)sellID;
        this.itemID = (short)itemID;
        this.npcID = (short)npcID;
        this.typeItem = (byte)type;
        this.shopID = (short)shopID;
    }
}
