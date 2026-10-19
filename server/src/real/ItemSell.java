/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class ItemSell {
    public static int ID_ITEM_SELL_AUTO_INCREMENT = 0;
    public int id;
    public short idGem = -1;
    public Item item;
    public String charSell = "";
    public String charBid = "";
    public String timeSell = "";
    public static int ALL_ID_ITEM_SELL;
    public int priceSell = 0;
    public int priceBid = 0;
    public long timeEnd = 0L;
    public byte country = 0;
    public byte kindItem = 0;

    public ItemSell(Item it, String charsell, int country) {
        this.item = it;
        if (it.idSellMarket == -1) {
            it.idSellMarket = genIDSELLAUTOOINCRE();
        }

        this.charSell = charsell;
        this.country = (byte)country;
        this.timeEnd = System.currentTimeMillis() + 14400000L;
        this.timeSell = Char.getDayTime(14400000L);
        this.setID();
    }

    public ItemSell() {
    }

    public ItemSell(int idGem, String charsell, int country) {
        this.charSell = charsell;
        this.country = (byte)country;
        this.idGem = (short)idGem;
        this.timeEnd = System.currentTimeMillis() + 14400000L;
        this.timeSell = Char.getDayTime(14400000L);
        this.setID();
    }

    public boolean isMyItem(String charname) {
        return this.charSell.equals(charname);
    }

    public void initInfoDB(String info) {
        try {
            String[] dataItem = Char.split(info, "|");
            String[] dt = Char.split(dataItem[0], ",");
            this.id = Integer.parseInt(dt[0]);
            if (this.id > ALL_ID_ITEM_SELL) {
                ALL_ID_ITEM_SELL = this.id;
            }

            this.priceSell = Integer.parseInt(dt[1]);
            this.charBid = dt[2];
            this.priceBid = Integer.parseInt(dt[3]);
            this.timeSell = dt[4];
            this.timeEnd = Long.parseLong(dt[5]);
            this.country = Byte.parseByte(dt[6]);

            try {
                this.item = new Item();
                this.item.dbInfo = dataItem[1];
                this.item.initInfoFromDB();
                this.item.setTemplate(this.item.tempateID, this.item.clazz, this.item.clazz, this.item.tempateID);
                String[] data = Char.split(dataItem[2], ",");

                for (int j = 0; j < data.length; j++) {
                    try {
                        if (j < 33) {
                            this.item.atb[j] = Short.parseShort(data[j].trim());
                        } else if (j < 43) {
                            this.item.newAtb[j - 33] = Byte.parseByte(data[j].trim());
                        } else if (j < 58) {
                            this.item.addMoreLevelSkill[j - 43] = Byte.parseByte(data[j].trim());
                        } else if (j < 61) {
                            this.item.lockAtb[j - 58] = Byte.parseByte(data[j].trim());
                        } else {
                            this.item.otherAtt[j - 61] = Short.parseShort(data[j].trim());
                        }
                    } catch (Exception var7) {
                    }
                }
            } catch (Exception var8) {
            }
        } catch (Exception var9) {
        }
    }

    public boolean isExpire() {
        return System.currentTimeMillis() - this.timeEnd > 0L;
    }

    public boolean canbuy(int incountry) {
        return this.country == incountry;
    }

    public boolean canBid() {
        if (this.isExpire()) {
            if (System.currentTimeMillis() - this.timeEnd < 10000L) {
                this.timeEnd = System.currentTimeMillis() + 10000L;
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void setID() {
        this.id = genID();
    }

    public static synchronized int genID() {
        return ALL_ID_ITEM_SELL = (ALL_ID_ITEM_SELL + 1) % 2000000000;
    }

    public boolean isMyItem(int chardbid, int id) {
        return id == id && chardbid == this.item.owner;
    }

    public static synchronized int genIDSELLAUTOOINCRE() {
        return ID_ITEM_SELL_AUTO_INCREMENT++;
    }
}
