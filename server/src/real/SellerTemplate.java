package real;

import java.util.Vector;

// Tach tu lib/NQSH_5h.jar (19/09/2026) cung dot voi CharSell/ShopSellItem/GemSellEntry.
public class SellerTemplate {
    public int idSeller;
    public byte[] typeItem;
    public byte kindSeller = 0;
    public ShopSellItem[] shop = new ShopSellItem[3];

    public SellerTemplate(int id, byte[] type, int kind) {
        this.kindSeller = (byte) kind;
        this.typeItem = type;
        this.idSeller = id;
        for (int i = 0; i < 3; i++) {
            this.shop[i] = new ShopSellItem(i);
        }
    }

    public boolean canSellItem(int idTemplate) {
        for (int i = 0; i < this.typeItem.length; i++) {
            if (this.typeItem[i] == idTemplate) {
                return true;
            }
        }
        return false;
    }

    public String infoItemSell() {
        if (this.kindSeller == 0) {
            return "Cửa hàng chỉ bán giáp";
        }
        if (this.kindSeller == 1) {
            return "Cửa hàng chỉ bán vũ khí";
        }
        if (this.idSeller == 2) {
            return "Cửa hàng chỉ bán trang sức, trứng thú cưng, tụ hồn đan, huyết bổ đề, huyết linh thảo, sách kỹ năng pet và các vật phẩm khác: luyện kim dược, ngọc khảm...";
        }
        return "Cửa hàng chỉ bán trang sức, xa tiêu lệnh, lệnh bài trấn yêu và các vật phẩm khác: luyện kim dược, ngọc khảm,...";
    }

    public Vector<CharSell> getListeUserShop(short shopID) {
        return this.shop[shopID].getCharSell();
    }

    public boolean addItem2Shop(Char p, int shopID, Actor item, int type, int price) {
        if (type == 0) {
            return this.shop[shopID].playerSellItem(p, (Item) item);
        }
        return false;
    }

    /** Ban gop nhieu vien ngoc cung loai thanh 1 dong niem yet mang theo so luong. */
    public boolean addGemItemSellBulk(Char p, int shopID, short idGemTemplate, int quantity, int price, String charCanBuy) {
        return this.shop[shopID].playerSellGemBulk(p, idGemTemplate, quantity, price, charCanBuy);
    }

    public Vector<Item> getListItemUser(int charID, int shopid) {
        return this.shop[shopid].getListItemSell(charID);
    }

    public int[] totalGemPlayerSell(int shopID, Char p) {
        return this.shop[shopID].totalGemPlayerSell(p);
    }

    public Actor getOneItemOfUser(int shopID, Char p, short itID, int type) {
        if (type == 1) {
            return this.shop[shopID].get1GemItemOfCharSell(p, itID);
        }
        return this.shop[shopID].get1ItemOfCharSell(p.id, itID);
    }

    public Actor checkItemSell(int shopID, short charID, short itID, int type) {
        if (type == 1) {
            return this.shop[shopID].check1GemItemOfCharSell(charID, itID);
        }
        return this.shop[shopID].check1ItemOfCharSell(charID, itID);
    }

    public boolean removeItem(int shopID, Char p, Actor it, int type, short gemid) {
        if (type == 1) {
            return this.shop[shopID].removeItem(p, gemid);
        }
        return this.shop[shopID].removeItem(p, (Item) it);
    }

    public void removeAllUser(short p) {
        this.shop[0].removeCharSell(p);
        this.shop[1].removeCharSell(p);
        this.shop[2].removeCharSell(p);
    }

    public void removeUserSell(short p, int shopID) {
        this.shop[shopID].removeCharSell(p);
    }
}
