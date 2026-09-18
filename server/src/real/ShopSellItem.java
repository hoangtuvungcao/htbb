package real;

import java.util.Hashtable;
import java.util.Vector;

// Tach tu lib/NQSH_5h.jar (19/09/2026) cung dot voi CharSell/SellerTemplate/GemSellEntry
// de ho tro ban/mua ngoc theo SO LUONG. Phan trang bi (type==0, cac ham lien quan Item)
// giu nguyen y het ban goc, khong dung toi trong tinh nang nay.
public class ShopSellItem {
    public static byte MAX_USER = (byte) 100;
    public static byte MAX_ITEM_SELL = (byte) 20;
    public Char[] arrCharSell = new Char[MAX_USER];
    public Item[][] arrItemSell = new Item[MAX_USER][MAX_ITEM_SELL];
    public byte[] nItemSell = new byte[MAX_USER];
    public Hashtable<Short, Char> user = new Hashtable<>();
    public Vector<CharSell> listCharSell = new Vector<>();
    public int id;

    public ShopSellItem(int id) {
        this.id = id;
    }

    public Vector<CharSell> getCharSell() {
        return this.listCharSell;
    }

    public void addCharSell(Char p) {
        if (this.listCharSell.size() > MAX_USER) {
            return;
        }
        for (int i = 0; i < this.listCharSell.size(); i++) {
            CharSell cs = this.listCharSell.get(i);
            if (cs.charname.toLowerCase().equals(p.charname.toLowerCase())) {
                return;
            }
        }
        this.listCharSell.add(new CharSell(p));
    }

    public void removeCharSell(int id) {
        for (int i = 0; i < this.listCharSell.size(); i++) {
            if (this.listCharSell.get(i).idChar == id) {
                this.listCharSell.remove(i);
            }
        }
    }

    public boolean playerSellItem(Char p, Item it) {
        this.addCharSell(p);
        for (int i = 0; i < this.listCharSell.size(); i++) {
            CharSell c = this.listCharSell.get(i);
            if (c.charname.toLowerCase().equals(p.charname.toLowerCase())) {
                return c.addItemSell(it);
            }
        }
        return false;
    }

    public int countGemPlayerSell(Char p, short idgemtemplate) {
        for (int i = 0; i < this.listCharSell.size(); i++) {
            CharSell c = this.listCharSell.get(i);
            if (c.charname.toLowerCase().equals(p.charname.toLowerCase())) {
                return c.countGemSellByType(idgemtemplate, p);
            }
        }
        return 0;
    }

    public int[] totalGemPlayerSell(Char p) {
        for (int i = 0; i < this.listCharSell.size(); i++) {
            CharSell c = this.listCharSell.get(i);
            if (c.charname.toLowerCase().equals(p.charname.toLowerCase())) {
                return c.countGemSellByType();
            }
        }
        return new int[Map.gemTemplate.length];
    }

    /** Ban gop N vien ngoc CUNG loai thanh 1 dong niem yet. Tra false neu het slot hoac khong du ngoc chua niem yet. */
    public boolean playerSellGemBulk(Char p, short idGemTemplate, int soLuong, int price, String charCanBuy) {
        this.addCharSell(p);
        for (int i = 0; i < this.listCharSell.size(); i++) {
            CharSell c = this.listCharSell.get(i);
            if (c.charname.toLowerCase().equals(p.charname.toLowerCase())) {
                if (c.isFull()) {
                    return false;
                }
                int daNiemYet = c.countGemSellByType(idGemTemplate, p);
                if (p.listGemitem[idGemTemplate] - daNiemYet >= soLuong) {
                    return c.addGemSellBulk(p, idGemTemplate, soLuong, price, charCanBuy);
                }
                return false;
            }
        }
        return false;
    }

    public Item get1ItemOfCharSell(short charID, short itemID) {
        for (int i = 0; i < this.listCharSell.size(); i++) {
            CharSell c = this.listCharSell.get(i);
            if (c.idChar == charID) {
                Item it = c.getItemSell(itemID);
                if (c.itemSell.size() == 0 && c.gemSell.size() == 0) {
                    this.listCharSell.remove(c);
                }
                return it;
            }
        }
        return null;
    }

    /** Nguoi mua chot mua CA dong niem yet (khong mua mot phan). */
    public Actor get1GemItemOfCharSell(Char p, short itemID) {
        for (int i = 0; i < this.listCharSell.size(); i++) {
            CharSell c = this.listCharSell.get(i);
            if (c.idChar == p.id) {
                Actor it = c.getGemItemSell(itemID, p);
                if (c.gemSell.size() == 0 && c.itemSell.size() == 0) {
                    this.listCharSell.remove(c);
                }
                return it;
            }
        }
        return null;
    }

    public Actor check1ItemOfCharSell(short charID, short itemID) {
        for (int i = 0; i < this.listCharSell.size(); i++) {
            CharSell c = this.listCharSell.get(i);
            if (c.idChar == charID) {
                return c.checkItemSell(itemID);
            }
        }
        return null;
    }

    public Actor check1GemItemOfCharSell(short charID, short itemID) {
        for (int i = 0; i < this.listCharSell.size(); i++) {
            CharSell c = this.listCharSell.get(i);
            if (c.idChar == charID) {
                return c.checkGemItemSell(itemID);
            }
        }
        return null;
    }

    public Vector<GemSellEntry> getListGemItemSell(int charID) {
        for (int i = 0; i < this.listCharSell.size(); i++) {
            CharSell c = this.listCharSell.get(i);
            if (c.idChar == charID) {
                if (c.gemSell.size() == 0 && c.itemSell.size() == 0) {
                    this.removeCharSell(charID);
                } else {
                    return c.gemSell;
                }
            }
        }
        return new Vector<>();
    }

    public Vector<Item> getListItemSell(int charID) {
        for (int i = 0; i < this.listCharSell.size(); i++) {
            CharSell c = this.listCharSell.get(i);
            if (c.idChar == charID) {
                if (c.itemSell.size() == 0 && c.gemSell.size() == 0) {
                    this.removeCharSell(charID);
                } else {
                    return c.itemSell;
                }
            }
        }
        return new Vector<>();
    }

    public void setListItem(Char p, Vector<Item> it) {
        for (int i = 0; i < this.listCharSell.size(); i++) {
            CharSell c = this.listCharSell.get(i);
            if (c.idChar == p.id && c.charname.toLowerCase().equals(p.charname.toLowerCase())) {
                c.setItemSell(it);
                return;
            }
        }
    }

    public boolean removeItem(Char p, Item item) {
        for (int i = 0; i < this.listCharSell.size(); i++) {
            CharSell c = this.listCharSell.get(i);
            if (c.idChar == p.id) {
                boolean result = c.removeItemSell(item);
                if (c.itemSell.size() == 0 && c.gemSell.size() == 0) {
                    this.removeCharSell(p.id);
                }
                return result;
            }
        }
        return false;
    }

    public boolean removeItem(Char p, short gemID) {
        for (int i = 0; i < this.listCharSell.size(); i++) {
            CharSell c = this.listCharSell.get(i);
            if (c.idChar == p.id) {
                boolean result = c.removeGemSell(p, gemID);
                if (c.itemSell.size() == 0 && c.gemSell.size() == 0) {
                    this.removeCharSell(p.id);
                }
                return result;
            }
        }
        return false;
    }
}
