/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.Database;
import data.NewClan;
import java.util.Hashtable;
import java.util.Vector;

public class Market {
    static int[] level = new int[]{
        1,
        4,
        6,
        9,
        11,
        14,
        16,
        19,
        20,
        21,
        24,
        25,
        26,
        29,
        30,
        31,
        34,
        35,
        36,
        39,
        40,
        41,
        44,
        45,
        46,
        49,
        50,
        51,
        54,
        55,
        56,
        59,
        60,
        61,
        64,
        65,
        66,
        69,
        70,
        71,
        74,
        75,
        76,
        79,
        80,
        81,
        85,
        86,
        90,
        91,
        95,
        96,
        100
    };
    public static Hashtable<Integer, ItemMarket> ALL_ITEM = new Hashtable<>();
    public static Hashtable<Integer, ItemMarket> ALL_GEM = new Hashtable<>();
    public static final byte MAX_ITEM_SELL = 10;
    public static final byte MAX_ITEM_BID = 20;

    public static void init() {
        for (int i = 0; i < level.length; i++) {
            ALL_ITEM.put(level[i], new ItemMarket());
        }
    }

    public static CharSellItem getCharSell(String charname) {
        CharSellItem cs = ItemMarket.ALL_CHAR_SELL.get(charname.toLowerCase().trim());
        if (cs == null) {
            cs = new CharSellItem(charname.toLowerCase().trim());
            ItemMarket.ALL_CHAR_SELL.put(charname.toLowerCase().trim(), cs);
        }

        return cs;
    }

    public static synchronized Vector<ItemSell> getListItemSearch(int level, int type, int colorItem, int pham, int cong, int country) {
        return !AdminHandler.isStopServer ? ALL_ITEM.get(level).getListItemSell(type, colorItem, pham, cong, country) : new Vector<>();
    }

    public static synchronized boolean isItemExpired(ItemSell item) {
        if (!item.isExpire()) {
            return false;
        } else {
            if (item.priceBid > 0 && !item.charBid.equals("")) {
                CharSellItem buy = getCharSell(item.charBid);
                buy.addItemBack(item.item);
                CharSellItem sell = getCharSell(item.charSell);
                NewClan clan = NewClan.getClan(Map.idClanTown[item.country]);
                long tax = 0L;
                if (clan != null) {
                    if (Map.taxOfClan[item.country] < 0) {
                        Map.taxOfClan[item.country] = 0;
                    }

                    long money = item.priceBid;
                    tax = money * Map.taxOfClan[item.country] / 100L;
                    if (tax < 0L) {
                        tax = 0L;
                    }

                    clan.addMoney2Clan(tax - tax * 10L / 100L);
                } else {
                    long money = item.priceBid;
                    tax = money * 10L / 100L;
                }

                sell.addMoney(item.priceBid - tax);
                removeItemSell(item, item.charSell);
                Database.instance
                    .saveOrtherLog(
                        "",
                        item.charBid,
                        " bid ok "
                            + item.item.getName()
                            + "_"
                            + item.item.plus
                            + " cua "
                            + item.charSell
                            + "|"
                            + item.item.getDBInfo()
                            + "|"
                            + item.item.getAttribute()
                            + "_"
                            + item.priceBid
                            + "_mnkho="
                            + getCharSell(item.charBid).money,
                        "buybid"
                    );
                Database.instance
                    .saveOrtherLog(
                        "",
                        item.charSell,
                        "ban bid ok "
                            + item.item.getName()
                            + "_"
                            + item.item.plus
                            + " cho "
                            + item.charBid
                            + "|"
                            + item.item.getDBInfo()
                            + "|"
                            + item.item.getAttribute()
                            + "_"
                            + item.priceBid
                            + "_mnkho="
                            + getCharSell(item.charSell).money,
                        "sellbid"
                    );
                Database.instance.saveAllCharSell(buy);
                Database.instance.saveAllCharSell(sell);
            } else {
                CharSellItem sell = getCharSell(item.charSell);
                removeItemSell(item, item.charSell);
                sell.addItemBack(item.item);
                sell.removeItem(item);
                Database.instance.saveAllCharSell(sell);

                try {
                    Database.instance.saveOrtherLog("", item.charSell, item.item.getName() + "_" + item.item.getInfoSave(), "hethan");
                } catch (Exception var8) {
                }
            }

            return true;
        }
    }

    public static synchronized void removeItemSell(ItemSell item, String charname) {
        byte var10003 = item.item.hangItem == -1 ? 0 : item.item.hangItem;
        Vector<ItemSell> listItem = getListItemSearch(item.item.level, item.item.getTemplate().type, item.item.colorName, var10003, item.item.plus, item.country);
        listItem.remove(item);
        ItemMarket.removeItem(item);
        ItemMarket.HASH_ITEM_SELL.remove(item.id);
    }

    public static synchronized boolean setBid(ItemSell item, Char player, int priceBid) {
        byte var10003 = item.item.hangItem == -1 ? 0 : item.item.hangItem;
        Vector<ItemSell> listItem = getListItemSearch(item.item.level, item.item.getTemplate().type, item.item.colorName, var10003, item.item.plus, item.country);
        if (listItem.contains(item)) {
            CharSellItem cs = getCharSell(player.charname);
            if (!item.canBid()) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Đã hết thời gian bid cho vật phẩm này", ""));
                Map.createListItemMarket(player, player.getAllListItemBid(), 4);
                Map.createListItemMarket(player, player.getAllistNewItemSell(), 0);
                return false;
            } else if (!cs.hadBitItem(item) && cs.countItemBit() >= 20) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể bid tối đa 20 vật phẩm", ""));
                return false;
            } else if (player.getxu() < priceBid) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                return false;
            } else if (priceBid > item.priceBid && priceBid < item.priceSell) {
                if (!item.charBid.trim().equals("")) {
                    ItemMarket.addMoneyCharSell(item.charBid.trim(), item.priceBid);
                    Database.instance
                        .saveOrtherLog(
                            "",
                            item.charBid,
                            item.item.getTemplate().name
                                + "_"
                                + item.item.plus
                                + "_"
                                + item.item.getDBInfo()
                                + "|"
                                + item.item.getAttribute()
                                + " giá "
                                + item.priceBid
                                + "_"
                                + item.charSell
                                + "_mnkho="
                                + getCharSell(item.charBid).money,
                            "tratienbid"
                        );
                }

                item.charBid = player.charname;
                item.priceBid = priceBid;
                player.subXu(priceBid, false, "maket 1");
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                if (!cs.hadBitItem(item)) {
                    cs.addItem(item);
                    Database.instance.saveAllCharSell(cs);
                }

                Database.instance
                    .saveOrtherLog(
                        "", player.charname, item.item.getName() + "_" + item.item.plus + "|" + item.item.getAttribute() + "_" + item.item.getDBInfo(), "bid"
                    );
                return true;
            } else {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể bid với giá này", ""));
                return false;
            }
        } else {
            System.out.println("KHONG TIM THAY ITEM BID");
            return false;
        }
    }

    public static synchronized boolean buyItem(ItemSell item, Char player) {
        byte var10003 = item.item.hangItem == -1 ? 0 : item.item.hangItem;
        Vector<ItemSell> listItem = getListItemSearch(item.item.level, item.item.getTemplate().type, item.item.colorName, var10003, item.item.plus, item.country);
        if (listItem.contains(item)) {
            if (player.getxu() < item.priceSell) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                return false;
            } else if (item.isExpire()) {
                isItemExpired(item);
                player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm đã hết thời gian đăng bán", ""));
                return false;
            } else {
                NewClan clan = NewClan.getClan(Map.idClanTown[item.country]);
                long tax = 0L;
                if (clan != null) {
                    if (Map.taxOfClan[item.country] < 0) {
                        Map.taxOfClan[item.country] = 0;
                    }

                    long money = item.priceSell;
                    tax = money * Map.taxOfClan[item.country] / 100L;
                    if (tax < 0L) {
                        tax = 0L;
                    }

                    clan.addMoney2Clan(tax - tax * 20L / 100L);
                } else {
                    long money = item.priceSell;
                    tax = money * 10L / 100L;
                }

                Item it = item.item.cloneItem();
                it.id = player.getIDItem();
                player.iItems.add(it);
                player.subXu(item.priceSell, false, "maket 2");
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã mua được " + it.getName(), ""));
                ItemMarket.addMoneyCharSell(item.charSell, item.priceSell - tax);
                ItemMarket.removeItem(item);
                listItem.remove(item);
                ItemMarket.HASH_ITEM_SELL.remove(item.id);
                Database.instance.saveAllCharSell(getCharSell(item.charSell.toLowerCase().trim()));
                if (!item.charBid.equals("")) {
                    ItemMarket.addMoneyCharSell(item.charBid, item.priceBid);
                    Database.instance.saveAllCharSell(getCharSell(item.charBid.toLowerCase().trim()));
                    Database.instance
                        .saveOrtherLog(
                            "",
                            item.charBid,
                            it.getTemplate().name
                                + "_"
                                + it.plus
                                + "_"
                                + it.getDBInfo()
                                + "|"
                                + it.getAttribute()
                                + " giá "
                                + item.priceBid
                                + "_"
                                + item.charSell
                                + "_mnkho="
                                + getCharSell(item.charBid).money,
                            "tratienbid"
                        );
                }

                Database.instance
                    .saveLogSellItem(
                        item.charSell,
                        player.charname,
                        it.getTemplate().name
                            + "_"
                            + it.plus
                            + "_"
                            + it.getDBInfo()
                            + "|"
                            + it.getAttribute()
                            + " giá "
                            + item.priceSell
                            + "_mnkho="
                            + getCharSell(item.charSell).money
                    );
                return true;
            }
        } else {
            player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm đã được bán cho người khác", ""));
            return false;
        }
    }

    public static ItemSell sellItem(Item item, String charsell, int price, int priceBid, int country) {
        return !AdminHandler.isStopServer ? ALL_ITEM.get(Integer.valueOf(item.level)).sellItem(item, charsell, price, priceBid, country) : null;
    }

    public static Vector<ItemSell> getListItemSearchByName(String charname) {
        return getCharSell(charname.toLowerCase()).getAllItems();
    }

    public static boolean checkCanSellBidItem(String charname) {
        return getCharSell(charname).canSell_Item();
    }
}
