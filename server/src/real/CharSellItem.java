/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.Database;
import java.util.Vector;

public class CharSellItem {
    public String charname = "";
    public long money = 0L;
    public ItemSell[] items = new ItemSell[10];
    public Item[] itemBack = new Item[30];
    public int[] lisetItemBid = new int[10];

    public CharSellItem(String charname) {
        this.charname = charname.toLowerCase();
    }

    public void addItemBack(Item item) {
        for (short i = 0; i < this.itemBack.length; i++) {
            if (this.itemBack[i] == null) {
                this.itemBack[i] = item;
                Database.instance.saveOrtherLog("", this.charname, item.getName() + "_" + item.getInfoSave(), "boitemvaokho");
                return;
            }
        }

        Database.instance.saveOrtherLog("", this.charname, item.getName() + "_" + item.getInfoSave(), "boitemvaokho_hetcho");
    }

    public void addItem(ItemSell item) {
        for (short i = 0; i < this.items.length; i++) {
            if (this.items[i] == null) {
                this.items[i] = item;
                break;
            }
        }
    }

    public void removeItem(ItemSell item) {
        Vector<ItemSell> allitem = new Vector<>();

        for (int i = 0; i < this.items.length; i++) {
            if (this.items[i] != null && this.items[i].id == item.id) {
                this.items[i] = null;
            } else if (this.items[i] != null) {
                allitem.add(this.items[i]);
            }
        }

        this.items = new ItemSell[10];

        for (int ix = 0; ix < allitem.size(); ix++) {
            this.items[ix] = allitem.get(ix);
        }
    }

    public int countItemBit() {
        int count = 0;

        for (int i = 0; i < this.lisetItemBid.length; i++) {
            if (this.lisetItemBid[i] > -1) {
                count++;
            }
        }

        return count;
    }

    public void addItemBit(ItemSell item) {
        for (int i = 0; i < this.lisetItemBid.length; i++) {
            if (this.lisetItemBid[i] == -1) {
                this.lisetItemBid[i] = item.id;
                break;
            }
        }
    }

    public boolean hadBitItem(ItemSell item) {
        for (int i = 0; i < this.lisetItemBid.length; i++) {
            if (this.lisetItemBid[i] > -1 && this.lisetItemBid[i] == item.id) {
                return true;
            }
        }

        return false;
    }

    public void addItemBit(int id) {
        for (int i = 0; i < this.lisetItemBid.length; i++) {
            if (this.lisetItemBid[i] == -1) {
                this.lisetItemBid[i] = id;
                break;
            }
        }
    }

    public boolean hadBitItem(int id) {
        for (int i = 0; i < this.lisetItemBid.length; i++) {
            if (this.lisetItemBid[i] > -1 && this.lisetItemBid[i] == id) {
                return true;
            }
        }

        return false;
    }

    public String getAllItemSell() {
        String info = "";

        for (int i = 0; i < this.items.length; i++) {
            ItemSell item = this.items[i];
            if (item != null) {
                info = info
                    + item.id
                    + ","
                    + item.priceSell
                    + ","
                    + item.charBid
                    + ","
                    + item.priceBid
                    + ","
                    + item.timeSell
                    + ","
                    + item.timeEnd
                    + ","
                    + item.country
                    + ","
                    + item.kindItem
                    + "|";
                info = info + item.item.getInfoSave() + ">";
            }
        }

        if (!info.equals("")) {
            info = info.substring(0, info.length() - 1);
        }

        return info;
    }

    public String getAllItemExpireSave() {
        String info = "";

        for (int i = 0; i < this.itemBack.length; i++) {
            Item item = this.itemBack[i];
            if (item != null) {
                info = info + item.getInfoSave() + ">";
            }
        }

        if (!info.equals("")) {
            info = info.substring(0, info.length() - 1);
        }

        return info;
    }

    public String getListItemBidSave() {
        String info = "";

        for (int i = 0; i < this.lisetItemBid.length; i++) {
            int id = this.lisetItemBid[i];
            if (id > -1) {
                info = info + id + ",";
            }
        }

        if (!info.equals("")) {
            info = info.substring(0, info.length() - 1);
        }

        return info;
    }

    public void addMoney(long mn) {
        this.money += mn;
    }

    public Vector<ItemSell> getAllItemExpire() {
        Vector<ItemSell> it = new Vector<>();

        for (int i = 0; i < this.itemBack.length; i++) {
            if (this.itemBack[i] != null) {
                ItemSell item = new ItemSell();
                item.item = this.itemBack[i];
                it.add(item);
            }
        }

        return it;
    }

    public Vector<ItemSell> getAllItems() {
        Vector<ItemSell> it = new Vector<>();
        Vector<ItemSell> itexpire = new Vector<>();

        for (int i = 0; i < this.items.length; i++) {
            if (this.items[i] != null) {
                ItemSell item = this.items[i];
                if (item.isExpire()) {
                    itexpire.add(item);
                } else {
                    it.add(item);
                }
            }
        }

        while (itexpire.size() > 0) {
            ItemSell item = itexpire.remove(0);
            if (ItemMarket.HASH_ITEM_SELL.get(item.id) != null) {
                Market.isItemExpired(item);
            } else {
                this.removeItem(item);
            }
        }

        return it;
    }

    public void getItemBack(Char p, int index) {
        try {
            if (index >= this.itemBack.length || this.itemBack[index] == null) {
                return;
            }

            if (this.itemBack[index] != null) {
                Item item = this.itemBack[index];
                item.owner = p.charDBID;
                item.id = p.getIDItem();
                p.iItems.add(item);
                this.itemBack[index] = null;
                Vector<Item> allitem = new Vector<>();

                for (int i = 0; i < this.itemBack.length; i++) {
                    if (this.itemBack[i] != null) {
                        allitem.add(this.itemBack[i].cloneItem());
                    }
                }

                this.itemBack = new Item[30];

                for (int ix = 0; ix < allitem.size(); ix++) {
                    this.itemBack[ix] = allitem.get(ix);
                }

                Database.instance.saveOrtherLog("", p.charname, item.getName() + "_" + item.getInfoSave(), "getitemkho");
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                Database.instance.saveAllCharSell(this);
            }
        } catch (Exception var6) {
            var6.printStackTrace();
            System.out.println(index);
        }
    }

    public void getMoney(Char p) {
        p.addXu(this.money, "charsellitem getMoney");
        this.money = 0L;
        Database.instance.saveAllCharSell(this);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        Map.createListItemMarket(p, p.getAllistItemExpire(), 3);
    }

    public Vector<ItemSell> getListItemBid() {
        Vector<ItemSell> it = new Vector<>();
        Vector<Integer> removeid = new Vector<>();

        for (int i = 0; i < this.lisetItemBid.length; i++) {
            if (this.lisetItemBid[i] > -1) {
                ItemSell item = ItemMarket.HASH_ITEM_SELL.get(this.lisetItemBid[i]);
                if (item != null) {
                    if (Market.isItemExpired(item)) {
                        removeid.add(this.lisetItemBid[i]);
                    } else {
                        it.add(item);
                    }

                    if (item.isExpire()) {
                        removeid.add(this.lisetItemBid[i]);
                        Market.removeItemSell(item, item.charSell);
                    } else {
                        it.add(item);
                    }
                } else {
                    this.lisetItemBid[i] = -1;
                }
            }
        }

        if (removeid.size() > 0) {
            Database.instance.saveAllCharSell(this);
        }

        return it;
    }

    public boolean canSell_Item() {
        int count = 0;

        for (int i = 0; i < this.itemBack.length; i++) {
            if (this.itemBack[i] != null) {
                count++;
            }
        }

        for (int ix = 0; ix < this.items.length; ix++) {
            if (this.items[ix] != null) {
                count++;
            }
        }

        return count < 20;
    }
}
