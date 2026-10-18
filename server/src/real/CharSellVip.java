/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import java.util.Hashtable;

public class CharSellVip {
    public static final byte MAX_ITEM = 50;
    public int id;
    public Hashtable<Integer, Item> itemSell = new Hashtable<>();

    public CharSellVip(int id) {
        this.id = id;
    }

    public Item getItem(int idItem) {
        synchronized (this.itemSell) {
            return this.itemSell.get(idItem);
        }
    }

    public boolean putItem(Item it) {
        if (this.itemSell.size() < 50) {
            synchronized (this.itemSell) {
                if (!this.itemSell.contains(it.dbid)) {
                    this.itemSell.put(it.dbid, it);
                    return true;
                }
            }
        }

        return false;
    }
}
