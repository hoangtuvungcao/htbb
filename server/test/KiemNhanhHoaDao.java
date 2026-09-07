import java.util.Hashtable;
import java.util.Vector;
import real.Char;
import real.Item;
import real.ItemTemplates;
import real.Map;

/** Check the ITEM giftcode factory, including every copy in a multi-item reward. */
public final class KiemNhanhHoaDao {
    public static void chay() {
        Kiem.nhom("Nhanh hoa dao giftcode - max 24.1%, 50.1%, 29.0%");
        Vector<Hashtable<Integer, ItemTemplates>> saved = Map.itemTemplates;
        try {
            Map.loadGemTemplate();
            Map.itemTemplates = new Vector<Hashtable<Integer, ItemTemplates>>();
            for (int i = 0; i < 6; i++) Map.itemTemplates.add(new Hashtable<Integer, ItemTemplates>());
            template(835, 28);
            template(836, 28);
            template(90000, 3);
            Char player = new Char(null);
            Char.createItemSoLuong(835, 3, player);
            Kiem.bang("receive three separate branches", 3, player.iItems.size());
            for (Item item : player.iItems) {
                Kiem.bang("true damage 24.1%", 241, (int) item.otherAtt[54]);
                Kiem.bang("weapon bonus 50.1%", 501, (int) item.otherAtt[55]);
                Kiem.bang("true damage chance 29.0%", 290, (int) item.otherAtt[56]);
                Kiem.dung("can equip in fashion weapon slot", item.isVukhiThoiTrang());
                Kiem.bang("giftcode branch is locked", 1, (int) item.lock);
                Kiem.bang("giftcode branch is permanent", 0, item.minuteExist);
                Kiem.bang("all three stats sent in item details", 3, item.getInfoAtbItem().size());
            }
            Kiem.dung("copies have distinct IDs", player.iItems.get(0).id != player.iItems.get(1).id
                    && player.iItems.get(1).id != player.iItems.get(2).id);
            Item weapon = new Item(Map.itemTemplates.get(0).get(90000), false, 0, 0, 90000);
            Kiem.bang("weapon bonus applies 501 per thousand", 501,
                    weapon.getValueUpByVuKhiThoiTrang(1000, player.iItems.get(0)));
            Char.createItemSoLuong(836, 1, player);
            Item other = player.iItems.lastElement();
            Kiem.bang("other type-28 items have no injected stats", 0,
                    other.otherAtt[54] + other.otherAtt[55] + other.otherAtt[56]);
            Kiem.sai("other type-28 items are not fashion weapons", other.isVukhiThoiTrang());
        } catch (Throwable e) {
            e.printStackTrace();
            Kiem.dung("giftcode factory runs successfully: " + e, false);
        } finally {
            Map.itemTemplates = saved;
        }
    }

    private static void template(int id, int type) {
        ItemTemplates t = new ItemTemplates();
        t.id = id;
        t.type = (byte) type;
        t.name = "Test item " + id;
        t.level = 10;
        t.atb = new short[10];
        for (Hashtable<Integer, ItemTemplates> table : Map.itemTemplates) table.put(id, t);
    }
}
