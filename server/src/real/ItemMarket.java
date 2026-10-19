/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.Database;
import java.util.Hashtable;
import java.util.Vector;

public class ItemMarket {
    public Vector<Hashtable<Integer, Vector<Vector<Vector<ItemSell>>>>> ALL_ITEM_COLOR_THANH_LONG = new Vector<>();
    public Vector<Hashtable<Integer, Vector<Vector<Vector<ItemSell>>>>> ALL_ITEM_COLOR_HAC_HO = new Vector<>();
    public static Vector<ItemSell> ALL_ITEM_SELL_THANH_LONG = new Vector<>();
    public static Vector<ItemSell> ALL_ITEM_SELL_HAC_HO = new Vector<>();
    public static Hashtable<Integer, ItemSell> HASH_ITEM_SELL = new Hashtable<>();
    public static Hashtable<String, CharSellItem> ALL_CHAR_SELL = new Hashtable<>();

    public ItemMarket() {
        for (int m = 0; m < 20; m++) {
            Hashtable<Integer, Vector<Vector<Vector<ItemSell>>>> it = new Hashtable<>();

            for (int k = 0; k < 4; k++) {
                Vector<Vector<Vector<ItemSell>>> item_hang = new Vector<>();

                for (int i = 0; i < 6; i++) {
                    Vector<Vector<ItemSell>> item_cong = new Vector<>();

                    for (int j = 0; j <= 15; j++) {
                        item_cong.add(new Vector<>());
                    }

                    item_hang.add(item_cong);
                }

                it.put(k, item_hang);
            }

            this.ALL_ITEM_COLOR_THANH_LONG.add(it);
        }

        for (int m = 0; m < 20; m++) {
            Hashtable<Integer, Vector<Vector<Vector<ItemSell>>>> it = new Hashtable<>();

            for (int k = 0; k < 4; k++) {
                Vector<Vector<Vector<ItemSell>>> item_hang = new Vector<>();

                for (int i = 0; i < 6; i++) {
                    Vector<Vector<ItemSell>> item_cong = new Vector<>();

                    for (int j = 0; j <= 15; j++) {
                        item_cong.add(new Vector<>());
                    }

                    item_hang.add(item_cong);
                }

                it.put(k, item_hang);
            }

            this.ALL_ITEM_COLOR_HAC_HO.add(it);
        }
    }

    public Vector<ItemSell> getListItemSell(int type, int colorItem, int pham, int cong, int country) {
        return country == 0
            ? this.ALL_ITEM_COLOR_THANH_LONG.get(type).get(colorItem).get(pham).get(cong)
            : this.ALL_ITEM_COLOR_HAC_HO.get(type).get(colorItem).get(pham).get(cong);
    }

    public ItemSell sellGem(int idGem, String charsell, int price, int priceBid, int country) {
        ItemSell it = new ItemSell(idGem, charsell, country);
        it.priceSell = price;
        it.priceBid = priceBid;
        return it;
    }

    public ItemSell sellItem(Item item, String charsell, int price, int priceBid, int country) {
        ItemSell it = new ItemSell(item, charsell, country);
        it.priceSell = price;
        it.priceBid = priceBid;
        if (country == 0) {
            this.ALL_ITEM_COLOR_THANH_LONG
                .get(item.getTemplate().type)
                .get(Integer.valueOf(item.colorName))
                .get(item.hangItem == -1 ? 0 : item.hangItem)
                .get(item.plus)
                .add(0, it);
            ALL_ITEM_SELL_THANH_LONG.add(0, it);
        } else {
            this.ALL_ITEM_COLOR_HAC_HO
                .get(item.getTemplate().type)
                .get(Integer.valueOf(item.colorName))
                .get(item.hangItem == -1 ? 0 : item.hangItem)
                .get(item.plus)
                .add(0, it);
            ALL_ITEM_SELL_HAC_HO.add(0, it);
        }

        HASH_ITEM_SELL.put(it.id, it);
        CharSellItem vt = ALL_CHAR_SELL.get(charsell.toLowerCase());
        if (vt != null) {
            vt.addItem(it);
        } else {
            vt = new CharSellItem(charsell.toLowerCase());
            vt.addItem(it);
            ALL_CHAR_SELL.put(charsell.toLowerCase(), vt);
        }

        Database.instance.saveAllCharSell(vt);
        return it;
    }

    public static void setItemSell(String info, long money, String charsell, String itemsKho, String listIDItemBid) {
        if (!charsell.equals("") && charsell != null) {
            if (itemsKho == null) {
                itemsKho = "";
            }

            if (info == null) {
                info = "";
            }

            if (listIDItemBid == null) {
                listIDItemBid = "";
            }

            CharSellItem vt = ALL_CHAR_SELL.get(charsell.toLowerCase());
            if (vt == null) {
                vt = new CharSellItem(charsell.toLowerCase());
                ALL_CHAR_SELL.put(charsell.toLowerCase(), vt);
            }

            vt.money = money;
            if (!info.equals("")) {
                String[] data = Char.split(info, ">");

                for (int i = 0; i < data.length; i++) {
                    ItemSell item = new ItemSell();
                    item.charSell = charsell;
                    item.initInfoDB(data[i]);
                    if (!item.isExpire() && item.item.minuteExist <= 0) {
                        ItemMarket it = Market.ALL_ITEM.get(Integer.valueOf(item.item.level));
                        if (item.country == 0) {
                            it.ALL_ITEM_COLOR_THANH_LONG
                                .get(item.item.getTemplate().type)
                                .get(Integer.valueOf(item.item.colorName))
                                .get(item.item.hangItem == -1 ? 0 : item.item.hangItem)
                                .get(item.item.plus)
                                .add(0, item);
                            ALL_ITEM_SELL_THANH_LONG.add(0, item);
                        } else {
                            it.ALL_ITEM_COLOR_HAC_HO
                                .get(item.item.getTemplate().type)
                                .get(Integer.valueOf(item.item.colorName))
                                .get(item.item.hangItem == -1 ? 0 : item.item.hangItem)
                                .get(item.item.plus)
                                .add(0, item);
                            ALL_ITEM_SELL_HAC_HO.add(0, item);
                        }

                        HASH_ITEM_SELL.put(item.id, item);
                        vt.addItem(item);
                    } else {
                        Market.isItemExpired(item);
                    }
                }
            }

            if (!itemsKho.equals("")) {
                String[] alldata = Char.split(itemsKho, ">");

                for (int ix = 0; ix < alldata.length; ix++) {
                    try {
                        new Item();
                        String[] dataItem = Char.split(alldata[ix], "|");

                        try {
                            Item var21 = new Item();
                            var21.dbInfo = dataItem[0];
                            var21.initInfoFromDB();
                            var21.setTemplate(var21.tempateID, var21.clazz, var21.clazz, var21.tempateID);
                            String[] data = Char.split(dataItem[1], ",");

                            for (int j = 0; j < data.length; j++) {
                                try {
                                    if (j < 33) {
                                        var21.atb[j] = Short.parseShort(data[j].trim());
                                    } else if (j < 43) {
                                        var21.newAtb[j - 33] = Byte.parseByte(data[j].trim());
                                    } else if (j < 58) {
                                        var21.addMoreLevelSkill[j - 43] = Byte.parseByte(data[j].trim());
                                    } else if (j < 61) {
                                        var21.lockAtb[j - 58] = Byte.parseByte(data[j].trim());
                                    } else {
                                        var21.otherAtt[j - 61] = Byte.parseByte(data[j].trim());
                                    }
                                } catch (Exception var14) {
                                }
                            }

                            vt.addItemBack(var21);
                        } catch (Exception var15) {
                            var15.printStackTrace();
                        }
                    } catch (Exception var16) {
                        var16.printStackTrace();
                    }
                }
            }

            if (!listIDItemBid.equals("")) {
                String[] alldata = Char.split(listIDItemBid, ",");

                for (int ix = 0; ix < alldata.length; ix++) {
                    int id = Integer.parseInt(alldata[ix]);
                    if (!vt.hadBitItem(id)) {
                        vt.addItemBit(id);
                    }
                }
            }
        }
    }

    public static void removeItem(ItemSell item) {
        synchronized (ALL_CHAR_SELL) {
            ALL_CHAR_SELL.get(item.charSell.toLowerCase().trim()).removeItem(item);
        }

        if (item.country == 0) {
            synchronized (ALL_ITEM_SELL_THANH_LONG) {
                ALL_ITEM_SELL_THANH_LONG.remove(item);
            }
        } else {
            synchronized (ALL_ITEM_SELL_HAC_HO) {
                ALL_ITEM_SELL_HAC_HO.remove(item);
            }
        }
    }

    public static synchronized void getItemBack2Inventory(Char p, int index) {
        synchronized (ALL_CHAR_SELL) {
            ALL_CHAR_SELL.get(p.getName()).getItemBack(p, index);
        }
    }

    public static void getMoney2Inventory(Char p) {
        synchronized (ALL_CHAR_SELL) {
            ALL_CHAR_SELL.get(p.getName()).getMoney(p);
        }
    }

    public static synchronized void saveAllCharSell() {
        for (CharSellItem c : ALL_CHAR_SELL.values()) {
            try {
                Database.instance.saveAllCharSell(c);
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        }
    }

    public static void addMoneyCharSell(String charname, long mn) {
        if (!charname.equals("") && charname != null) {
            synchronized (ALL_CHAR_SELL) {
                CharSellItem vt = ALL_CHAR_SELL.get(charname.toLowerCase());
                if (vt == null) {
                    vt = new CharSellItem(charname.toLowerCase());
                }

                vt.addMoney(mn);
                Database.instance.saveAllCharSell(vt);
            }
        }
    }

    public static long getMoneyCharSell(String charname) {
        synchronized (ALL_CHAR_SELL) {
            CharSellItem vt = ALL_CHAR_SELL.get(charname.toLowerCase());
            if (vt == null) {
                vt = new CharSellItem(charname.toLowerCase());
                Database.instance.saveAllCharSell(vt);
            }

            return vt.money;
        }
    }

    public static void sortAllItemSell() {
        quickSort(ALL_ITEM_SELL_THANH_LONG);
        quickSort(ALL_ITEM_SELL_HAC_HO);
    }

    public static void quickSort(Vector<ItemSell> actors) {
        recQuickSort(actors, 0, actors.size() - 1);
    }

    private static void recQuickSort(Vector<ItemSell> actors, int left, int right) {
        try {
            if (right - left <= 0) {
                return;
            }

            long pivot = actors.elementAt(right).timeEnd;
            int partition = partitionIt(actors, left, right, pivot);
            recQuickSort(actors, left, partition - 1);
            recQuickSort(actors, partition + 1, right);
        } catch (Exception var6) {
        }
    }

    private static int partitionIt(Vector<ItemSell> actors, int left, int right, long pivot) {
        int leftPtr = left - 1;
        int rightPtr = right;

        try {
            while (true) {
                while (actors.elementAt(++leftPtr).timeEnd > pivot) {
                }

                while (rightPtr > 0) {
                    if (actors.elementAt(--rightPtr).timeEnd >= pivot) {
                        break;
                    }
                }

                if (leftPtr >= rightPtr) {
                    swap(actors, leftPtr, right);
                    break;
                }

                swap(actors, leftPtr, rightPtr);
            }
        } catch (Exception var8) {
        }

        return leftPtr;
    }

    private static void swap(Vector<ItemSell> actors, int dex1, int dex2) {
        ItemSell temp = actors.elementAt(dex2);
        if (actors.elementAt(dex2).timeEnd != actors.elementAt(dex1).timeEnd) {
            actors.setElementAt(actors.elementAt(dex1), dex2);
            actors.setElementAt(temp, dex1);
        }
    }
}
