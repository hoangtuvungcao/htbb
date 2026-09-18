package real;

import java.util.Vector;

// Tach tu lib/NQSH_5h.jar (19/09/2026) de doi gemSell sang niem yet MANG THEO SO LUONG
// (GemSellEntry) thay vi 1 dong = 1 vien (GemItem) nhu ban goc. Phan itemSell (trang bi,
// type==0) giu y nguyen, khong dung toi trong tinh nang nay.
public class CharSell {
    public short idChar = 0;
    public String charname = "";
    public Vector<Item> itemSell = new Vector<>();
    public Vector<GemSellEntry> gemSell = new Vector<>();
    public static byte MAX_ITEM_SELL = (byte) 20;

    public CharSell(Char p) {
        this.idChar = p.id;
        this.charname = p.getName();
    }

    public boolean isFull() {
        return this.itemSell.size() + this.gemSell.size() >= MAX_ITEM_SELL;
    }

    public boolean addItemSell(Item it) {
        if (this.isFull()) {
            return false;
        }
        if (!this.itemSell.contains(it)) {
            this.itemSell.add(it);
            return true;
        }
        return false;
    }

    /** Tong so ngoc CUNG loai dang niem yet (cong don moi dong), dung de chan ban qua so dang co. */
    public int countGemSellByType(int idGemtemplate, Char p) {
        return p.listGemitemSell[idGemtemplate];
    }

    public int[] countGemSellByType() {
        int[] result = new int[Map.gemTemplate.length];
        for (int i = 0; i < this.gemSell.size(); i++) {
            GemSellEntry e = this.gemSell.get(i);
            result[e.idGemTemplate] += e.soLuong;
        }
        return result;
    }

    /** Ban gop nhieu vien ngoc CUNG loai, CUNG gia thanh 1 dong niem yet mang so luong. */
    public boolean addGemSellBulk(Char p, short idGemTemplate, int soLuong, int price, String charCanBuy) {
        if (this.isFull()) {
            return false;
        }
        GemSellEntry e = new GemSellEntry();
        e.idChar = p.id;
        e.idGemTemplate = idGemTemplate;
        e.soLuong = soLuong;
        e.prizeSell = price;
        e.charCanBuy = charCanBuy;
        e.realId = p.getIDItem();
        this.gemSell.add(e);
        int[] listGemitemSell = p.listGemitemSell;
        listGemitemSell[idGemTemplate] += soLuong;
        return true;
    }

    public Item getItemSell(short itemid) {
        for (int i = 0; i < this.itemSell.size(); i++) {
            if (this.itemSell.get(i).id == itemid) {
                return this.itemSell.remove(i);
            }
        }
        return null;
    }

    public Actor checkItemSell(short itemid) {
        for (int i = 0; i < this.itemSell.size(); i++) {
            if (this.itemSell.get(i).id == itemid) {
                return this.itemSell.get(i);
            }
        }
        return null;
    }

    public Actor checkGemItemSell(short itemid) {
        for (int i = 0; i < this.gemSell.size(); i++) {
            if (this.gemSell.get(i).realId == itemid) {
                return this.gemSell.get(i);
            }
        }
        return null;
    }

    /** Nguoi mua da chot mua CA dong - go dong khoi gian hang va tra ve de OfflineMap xu ly tien/ngoc. */
    public GemSellEntry getGemItemSell(short itemid, Char p) {
        for (int i = 0; i < this.gemSell.size(); i++) {
            GemSellEntry g = this.gemSell.get(i);
            if (g.realId == itemid) {
                int[] listGemitemSell = p.listGemitemSell;
                listGemitemSell[g.idGemTemplate] -= g.soLuong;
                if (listGemitemSell[g.idGemTemplate] < 0) {
                    listGemitemSell[g.idGemTemplate] = 0;
                }
                return this.gemSell.remove(i);
            }
        }
        return null;
    }

    /** Nguoi ban tu huy dong niem yet cua minh (tra lai quyen "chua niem yet" cho so luong do). */
    public boolean removeGemSell(Char p, short gemid) {
        for (int i = 0; i < this.gemSell.size(); i++) {
            GemSellEntry g = this.gemSell.get(i);
            if (g.realId == gemid) {
                this.gemSell.remove(i);
                int[] listGemitemSell = p.listGemitemSell;
                listGemitemSell[g.idGemTemplate] -= g.soLuong;
                if (listGemitemSell[g.idGemTemplate] < 0) {
                    listGemitemSell[g.idGemTemplate] = 0;
                }
                return true;
            }
        }
        return false;
    }

    public boolean removeItemSell(Item it) {
        for (int i = 0; i < this.itemSell.size(); i++) {
            if (this.itemSell.get(i).id == it.id) {
                this.itemSell.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean removeItemByID(short id) {
        for (int i = 0; i < this.itemSell.size(); i++) {
            if (this.itemSell.get(i).id == id) {
                return this.itemSell.remove(i) != null;
            }
        }
        return false;
    }

    public void setItemSell(Vector<Item> it) {
        for (int i = 0; i < it.size(); i++) {
            for (int j = 0; j < this.itemSell.size(); j++) {
                if (it.get(i).id == this.itemSell.get(j).id) {
                    it.get(i).isSelling = true;
                    it.get(i).prizeSell = this.itemSell.get(j).prizeSell;
                    break;
                }
            }
        }
    }
}
